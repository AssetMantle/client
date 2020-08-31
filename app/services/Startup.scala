package services

import akka.Done
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.ws.{Message, TextMessage, WebSocketRequest, WebSocketUpgradeResponse}
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.blockchain.Token
import models.{blockchain, keyBase}
import play.api.{Configuration, Logger}
import play.libs.Json
import queries._
import queries.responses.CommunityPoolResponse.{Response => CommunityPoolResponse}
import queries.responses.GenesisResponse.{AccountValue, Genesis}
import queries.responses.MintingInflationResponse.{Response => MintingInflationResponse}
import queries.responses.StakingPoolResponse.{Response => StakingPoolResponse}
import queries.responses.TotalSupplyResponse.{Response => TotalSupplyResponse}
import queries.responses.TransactionResponse.Msg
import queries.responses.ValidatorResponse.{Result => ValidatorResult}
import queries.responses.WSClientBlockResponse.{NewBlockEvents, Response => WSClientBlockResponse}
import utilities.MicroNumber

import scala.concurrent.duration.{Duration, DurationInt}
import scala.concurrent.{Await, ExecutionContext, Future, Promise}
import scala.io.{Source => ScalaSource}
import scala.util.{Failure, Success}

@Singleton
class Startup @Inject()(
                         blockchainAccounts: blockchain.Accounts,
                         blockchainBlocks: blockchain.Blocks,
                         blockchainDelegations: blockchain.Delegations,
                         blockchainSigningInfos: blockchain.SigningInfos,
                         blockchainValidators: blockchain.Validators,
                         blocksServices: Block,
                         blockchainTokens: blockchain.Tokens,
                         blockchainUndelegations: blockchain.Undelegations,
                         keyBaseValidatorAccounts: keyBase.ValidatorAccounts,
                         getBondedValidators: GetBondedValidators,
                         getUnbondedValidators: GetUnbondedValidators,
                         getUnbondingValidators: GetUnbondingValidators,
                         getTotalSupply: GetTotalSupply,
                         getStakingPool: GetStakingPool,
                         getMintingInflation: GetMintingInflation,
                         getCommunityPool: GetCommunityPool,
                         getAsset: GetAsset,
                         getGenesis: GetGenesis,
                       )(implicit exec: ExecutionContext, configuration: Configuration) {

  private implicit val module: String = constants.Module.SERVICES_STARTUP

  private implicit val logger: Logger = Logger(this.getClass)

  private val schedulerExecutionContext: ExecutionContext = actors.Service.actorSystem.dispatchers.lookup("akka.actor.scheduler-dispatcher")

  private val genesisFilePath = configuration.get[String]("blockchain.genesisFilePath")

  private def initialize(): Unit = {
    try {
      val genesisSource = ScalaSource.fromFile(genesisFilePath)
      val genesis = utilities.JSON.convertJsonStringToObject[Genesis](genesisSource.mkString)
      genesisSource.close()
      Await.result(insertOrUpdateAccountBalances(genesis.app_state.auth.accounts.map(_.value)), Duration.Inf)
      val latestBlockHeight = Await.result(blockchainBlocks.Service.getLatestBlockHeight, Duration.Inf)
      val missingBlockHeights = Await.result(blockchainBlocks.Service.getMissingBlocks(1, latestBlockHeight), Duration.Inf)
      Await.result(insertOrUpdateAllValidators(latestBlockHeight), Duration.Inf)
      Await.result(insertAllTokens(latestBlockHeight), Duration.Inf)
      Await.result(Future.traverse(missingBlockHeights)(height => blocksServices.insertOnBlock(height)), Duration.Inf)
      Await.result(insertBlocksOnStartup(latestBlockHeight), Duration.Inf)
    } catch {
      case baseException: BaseException => new BaseException(constants.Response.BLOCKCHAIN_CONNECTION_LOST, baseException.exception)
        onLosingConnection()
    }
  }

  private def runOnStartup(): Unit = {
    initialize()
    WebSocketBlockchainClient.start()
  }

  private def insertBlocksOnStartup(latestBlockHeight: Int): Future[Unit] = Future {
    var blockHeight = latestBlockHeight + 1
    try {
      while (true) {
        val blockCommitResponse = Await.result(blocksServices.insertOnBlock(blockHeight), Duration.Inf)
        val transactions = Await.result(blocksServices.insertTransactionsOnBlock(blockHeight), Duration.Inf)
        val avgBlockTime = Await.result(blocksServices.setAverageBlockTime(blockCommitResponse.result.signed_header.header), Duration.Inf)
        Await.result(blockchainUndelegations.Utility.updateOnNewBlock(blockCommitResponse.result.signed_header.header.time), Duration.Inf)
        blocksServices.sendNewBlockWebSocketMessage(blockCommitResponse = blockCommitResponse, transactions = transactions, averageBlockTime = avgBlockTime)
        blockHeight = blockHeight + 1
      }
    } catch {
      case baseException: BaseException => if (baseException.failure != constants.Response.BLOCK_NOT_FOUND) {
        throw baseException
      } else Unit
    }
  }

  private def insertOrUpdateAccountBalances(accounts: Seq[AccountValue]) = {
    val upsert = Future.traverse(accounts)(account => blockchainAccounts.Utility.insertOrUpdateAccountBalance(address = account.address))
    (for {
      _ <- upsert
    } yield ()
      ).recover {
      case baseException: BaseException => throw baseException
    }
  }

  private def insertGenesisTransactions(msgs: Seq[Msg]) = Future.traverse(msgs)(msg => blocksServices.actionOnTxMessages(msg.toStdMsg, 0))

  private def insertOrUpdateAllValidators(latestBlockHeight: Int): Future[Unit] = {
    if (latestBlockHeight == 0) {
      val bondedValidators = getBondedValidators.Service.get()
      val unbondedValidators = getUnbondedValidators.Service.get()
      val unbondingValidators = getUnbondingValidators.Service.get()
      val insertSigningInfos = blockchainSigningInfos.Utility.insertAll()

      def insert(validatorResults: Seq[ValidatorResult]) = {
        val insertValidator = blockchainValidators.Service.insertMultiple(validatorResults.map(_.toValidator))
        val insertDelegations = Future.traverse(validatorResults)(validatorResult => blockchainDelegations.Utility.insertOrUpdate(delegatorAddress = utilities.Bech32.convertOperatorAddressToAccountAddress(validatorResult.operator_address), validatorAddress = validatorResult.operator_address))
//        val insertKeyBaseAccount = Future.traverse(validatorResults)(validator => keyBaseValidatorAccounts.Utility.insertOrUpdateKeyBaseAccount(validator.operator_address, validator.description.identity))
        (for {
          _ <- insertValidator
          _ <- insertDelegations
//          _ <- insertKeyBaseAccount
        } yield ()).recover {
          case baseException: BaseException => logger.error(baseException.getLocalizedMessage)
        }
      }

      (for {
        bondedValidators <- bondedValidators
        unbondedValidators <- unbondedValidators
        unbondingValidators <- unbondingValidators
        _ <- insert(bondedValidators.result ++ unbondedValidators.result ++ unbondingValidators.result)
        _ <- insertSigningInfos
      } yield ()
        ).recover {
        case baseException: BaseException => throw baseException
      }
    } else Future()
  }

  private def insertAllTokens(latestBlockHeight: Int): Future[Unit] = if (latestBlockHeight == 0) {
    val stakingTokenSymbol = configuration.get[String]("blockchain.token.stakingSymbol")
    val totalSupplyResponse = getTotalSupply.Service.get
    val mintingInflationResponse = getMintingInflation.Service.get
    val stakingPoolResponse = getStakingPool.Service.get
    val communityPoolResponse = getCommunityPool.Service.get

    def insert(totalSupplyResponse: TotalSupplyResponse, mintingInflationResponse: MintingInflationResponse, stakingPoolResponse: StakingPoolResponse, communityPoolResponse: CommunityPoolResponse) = {
      blockchainTokens.Service.insertMultiple(totalSupplyResponse.result.map(x => Token(symbol = x.denom, totalSupply = x.amount,
        bondedAmount = if (x.denom == stakingTokenSymbol) stakingPoolResponse.result.bonded_tokens else MicroNumber.zero,
        notBondedAmount = if (x.denom == stakingTokenSymbol) stakingPoolResponse.result.not_bonded_tokens else MicroNumber.zero,
        communityPool = communityPoolResponse.result.find(_.denom == x.denom).fold(MicroNumber.zero)(_.amount),
        inflation = if (x.denom == stakingTokenSymbol) mintingInflationResponse.result else BigDecimal(0.0)
      )))
    }

    (for {
      totalSupplyResponse <- totalSupplyResponse
      mintingInflationResponse <- mintingInflationResponse
      stakingPoolResponse <- stakingPoolResponse
      communityPoolResponse <- communityPoolResponse
      _ <- insert(totalSupplyResponse, mintingInflationResponse, stakingPoolResponse, communityPoolResponse)
    } yield ()
      ).recover {
      case baseException: BaseException => logger.error(baseException.failure.message, baseException)
        throw baseException
    }
  } else Future()

  def onLosingConnection(): Unit = {
    actors.Service.appWebSocketActor ! Json.toJson(actors.Message.WebSocket.BlockchainConnectionLost(true)).toString
    Thread.sleep(7000)
    initialize()
  }

  object WebSocketBlockchainClient {

    //TODO Check for latestBlockHeight + 1 before start
    def start(): Unit = {

      import actors.Service._

      val wsURL = configuration.get[String]("blockchain.main.wsURL")

      case class BlockRequest(method: String, id: String, jsonrpc: String, params: List[String])

      implicit val ec: ExecutionContext = actors.Service.actorSystem.dispatchers.lookup("akka.actor.scheduler-dispatcher")

      def convertTextStreamToString(source: Source[String, _]): Future[String] = {
        val sink = Sink.fold[String, String]("") { case (acc, str) =>
          acc + str
        }
        source.runWith[Future[String]](sink)
      }

      val sink: Sink[Message, Future[Done]] =
        Sink.foreach[Message] {
          case strictMessage: TextMessage.Strict => onStrictMessage(strictMessage.text)
          case streamedMessage: TextMessage.Streamed => onStreamedMessage(convertTextStreamToString(streamedMessage.textStream))
        }

      val source: Source[Message, Promise[Option[Message]]] = Source(List(TextMessage(Json.toJson(BlockRequest(method = "subscribe", id = "dontcare", jsonrpc = "2.0", params = List("tm.event='NewBlock'"))).toString))).concatMat(Source.maybe[Message])(Keep.right)

      val flow: Flow[Message, Message, Future[Done]] = Flow.fromSinkAndSourceMat(sink, source)(Keep.left)

      def runWebSocketConnection(flow: Flow[Message, Message, Future[Done]]): (Future[WebSocketUpgradeResponse], Future[Done]) = {
        Http().singleWebSocketRequest(WebSocketRequest(wsURL), flow)
      }

      val (upgradeResponse, closed) = runWebSocketConnection(flow)

      val connected = upgradeResponse.map { upgrade =>
        if (upgrade.response.status == StatusCodes.SwitchingProtocols) {
          logger.info("Connection upgraded to websocket. StatusCode: " + upgrade.response.status.toString)
        } else {
          logger.error("Connection upgrade to websocket failed. StatusCode: " + upgrade.response.status.toString)
          onLosingConnection()
        }
      }(ec)

      connected.onComplete {
        case Success(_) => logger.info("Websocket connection to blockchain success.")
        case Failure(exception) => logger.error(exception.toString)
          logger.error("Websocket connection to blockchain failed.")
          onLosingConnection()
      }(ec)

      closed.foreach(_ => {
        logger.error("Websocket connection to blockchain closed.")
        onLosingConnection()
      })(ec)

    }

    private def onNewBlock(newBlock: WSClientBlockResponse): Future[Unit] = {
      val blockCommitResponse = blocksServices.insertOnBlock(newBlock.result.data.value.block.header.height)
      val avgBlockTime = blocksServices.setAverageBlockTime(newBlock.result.data.value.block.header)
      val checksAndUpdatesOnNewBlock = blocksServices.checksAndUpdatesOnNewBlock(newBlock)
      val newEventsActions = actionsOnNewBlockEvents(newBlock.result.events)

      (for {
        blockCommitResponse <- blockCommitResponse
        transactions <- blocksServices.insertTransactionsOnBlock(newBlock.result.data.value.block.header.height)
        avgBlockTime <- avgBlockTime
        _ <- checksAndUpdatesOnNewBlock
        _ <- newEventsActions
        _ <- blocksServices.sendNewBlockWebSocketMessage(blockCommitResponse = blockCommitResponse, transactions = transactions, averageBlockTime = avgBlockTime)
      } yield ()
        ).recover {
        case baseException: BaseException => logger.error(baseException.failure.message)
      }
    }

    private def actionsOnNewBlockEvents(newBlockEvents: NewBlockEvents) = {
      val slashing = blocksServices.onSlashingEvent(slashAddresses = newBlockEvents.slashAddress.getOrElse(Seq.empty), slashReasons = newBlockEvents.slashReason.getOrElse(Seq.empty), slashJailed = newBlockEvents.slashJailed.getOrElse(Seq.empty))
      val missedBlock = blocksServices.onMissedBlockEvent(livenessAddresses = newBlockEvents.livenessAddress.getOrElse(Seq.empty), livenessHeights = newBlockEvents.livenessHeight.getOrElse(Seq.empty), livenessMissedBlocksCounter = newBlockEvents.livenessMissedBlocksCounter.getOrElse(Seq.empty))

      (for {
        _ <- slashing
        _ <- missedBlock
      } yield ()
        ).recover {
        case baseException: BaseException => logger.error(baseException.failure.logMessage)
      }
    }

    def getBlock(message: String): WSClientBlockResponse = utilities.JSON.convertJsonStringToObject[WSClientBlockResponse](message)

    def onStrictMessage(message: String): Unit = {
      try {
        onNewBlock(getBlock(message))
      } catch {
        case baseException: BaseException => logger.error(baseException.failure.logMessage)
      }
    }

    private def onStreamedMessage(message: Future[String]): Unit = {
      (for {
        message <- message
      } yield onNewBlock(getBlock(message))
        ).recover {
        case baseException: BaseException => logger.error(baseException.failure.logMessage)
      }
    }
  }

  private val initializeRunnable = new Runnable {
    def run(): Unit = runOnStartup()
  }

  actors.Service.actorSystem.scheduler.scheduleOnce(500.millisecond, initializeRunnable)(schedulerExecutionContext)

}
