package services

import akka.Done
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.ws.{Message, TextMessage, WebSocketRequest, WebSocketUpgradeResponse}
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.blockchain.{Token, Transaction}
import models.{blockchain, keyBase}
import play.api.{Configuration, Logger}
import play.libs.Json
import queries._
import queries.responses.BlockCommitResponse
import queries.responses.CommunityPoolResponse.{Response => CommunityPoolResponse}
import queries.responses.GenesisResponse._
import queries.responses.MintingInflationResponse.{Response => MintingInflationResponse}
import queries.responses.StakingPoolResponse.{Response => StakingPoolResponse}
import queries.responses.TotalSupplyResponse.{Response => TotalSupplyResponse}
import queries.responses.WSClientBlockResponse.{NewBlockEvents, Response => WSClientBlockResponse}
import queries.responses.common.Validator.{Result => ValidatorResult}
import queries.responses.common.{Account, Header}
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
                         blockchainRedelegations: blockchain.Redelegations,
                         blockchainUndelegations: blockchain.Undelegations,
                         blockchainWithdrawAddresses: blockchain.WithdrawAddresses,
                         keyBaseValidatorAccounts: keyBase.ValidatorAccounts,
                         getBondedValidators: GetBondedValidators,
                         getUnbondedValidators: GetUnbondedValidators,
                         getUnbondingValidators: GetUnbondingValidators,
                         getTotalSupply: GetTotalSupply,
                         getStakingPool: GetStakingPool,
                         getMintingInflation: GetMintingInflation,
                         getCommunityPool: GetCommunityPool,
                         utilitiesOperations: utilities.Operations
                       )(implicit exec: ExecutionContext, configuration: Configuration) {

  private implicit val module: String = constants.Module.SERVICES_STARTUP

  private implicit val logger: Logger = Logger(this.getClass)

  private val schedulerExecutionContext: ExecutionContext = actors.Service.actorSystem.dispatchers.lookup("akka.actor.scheduler-dispatcher")

  private val genesisFilePath = configuration.get[String]("blockchain.genesisFilePath")

  private val stakingTokenSymbol = configuration.get[String]("blockchain.token.stakingSymbol")

  private def initialize(): Future[Unit] = {
    println("starting intialization")
    val genesis = Future {
      val genesisSource = ScalaSource.fromFile(genesisFilePath)
      val genesis = utilities.JSON.convertJsonStringToObject[Genesis](genesisSource.mkString)
      genesisSource.close()
      genesis
    }

    val latestBlockHeight = blockchainBlocks.Service.getLatestBlockHeight

    def insertAccounts(latestBlockHeight: Int, genesis: Genesis): Future[Seq[Unit]] = insertAccountsOnStart(latestBlockHeight, genesis.app_state.auth.accounts)

    def updateStaking(latestBlockHeight: Int, genesis: Genesis): Future[Unit] = updateStakingOnStart(latestBlockHeight, genesis.app_state.staking)

    def insertGenesisTransactions(latestBlockHeight: Int, genesis: Genesis): Future[Unit] = insertGenesisTransactionsOnStart(latestBlockHeight, genesis.app_state.genutil.gentxs.getOrElse(Seq.empty))

    def updateDistribution(latestBlockHeight: Int, genesis: Genesis): Future[Unit] = updateDistributionOnStart(latestBlockHeight, genesis.app_state.distribution)

    def insertAllTokens(latestBlockHeight: Int): Future[Unit] = insertAllTokensOnStart(latestBlockHeight)

    def insertBlocks(latestBlockHeight: Int): Future[Unit] = insertBlocksOnStart(latestBlockHeight)

    (for {
      genesis <- genesis
      latestBlockHeight <- latestBlockHeight
      _ <- insertAccounts(latestBlockHeight, genesis)
      _ <- updateStaking(latestBlockHeight, genesis)
      _ <- insertGenesisTransactions(latestBlockHeight, genesis)
      _ <- updateDistribution(latestBlockHeight, genesis)
      _ <- insertAllTokens(latestBlockHeight)
      _ <- insertBlocks(latestBlockHeight)
      _ <- WebSocketBlockchainClient.start()
    } yield ()
      ).recoverWith {
      case baseException: BaseException => new BaseException(constants.Response.BLOCKCHAIN_CONNECTION_LOST, baseException.exception)
        onLosingConnection()
    }
  }

  private def runOnStartup(): Future[Unit] = {
    for {
      _ <- initialize()
    } yield ()
  }

  private def insertBlocksOnStart(latestBlockHeight: Int): Future[Unit] = Future {
    try {
      var blockHeight = latestBlockHeight + 1
      while (true) {
        println("adding block-----"+blockHeight)
        val blockCommitResponse = blocksServices.insertOnBlock(blockHeight)

        def transactions: Future[Seq[Transaction]] = blocksServices.insertTransactionsOnBlock(blockHeight)

        def avgBlockTime(blockCommitResponse: BlockCommitResponse.Response): Future[Double] = blocksServices.setAverageBlockTime(blockCommitResponse.result.signed_header.header)

        def checksAndUpdatesOnBlock(blockCommitResponse: BlockCommitResponse.Response): Future[Unit] = blocksServices.checksAndUpdatesOnBlock(blockCommitResponse.result.signed_header.header)

        def sendWebSocketMessage(blockCommitResponse: BlockCommitResponse.Response, transactions: Seq[Transaction], avgBlockTime: Double) = blocksServices.sendNewBlockWebSocketMessage(blockCommitResponse = blockCommitResponse, transactions = transactions, averageBlockTime = avgBlockTime)

        val forComplete = for {
          blockCommitResponse <- blockCommitResponse
          transactions <- transactions
          avgBlockTime <- avgBlockTime(blockCommitResponse)
          _ <- checksAndUpdatesOnBlock(blockCommitResponse)
          _ <- sendWebSocketMessage(blockCommitResponse, transactions, avgBlockTime)
        } yield ()
        Await.result(forComplete, Duration.Inf)
        blockHeight = blockHeight + 1
      }
    } catch {
      case baseException: BaseException => if (baseException.failure != constants.Response.BLOCK_QUERY_FAILED) {
        throw baseException
      } else ()
    }
  }

  private def insertAccountsOnStart(latestBlockHeight: Int, accounts: Seq[Account.Result]): Future[Seq[Unit]] = if (latestBlockHeight == 0) {
    utilitiesOperations.traverse(accounts) { account =>
      val upsert = blockchainAccounts.Utility.insertOrUpdateAccountBalance(address = account.value.address)
      (for {
        _ <- upsert
      } yield ()
        ).recover {
        case baseException: BaseException => throw baseException
      }
    }
  } else Future(Seq.empty)

  private def updateStakingOnStart(latestBlockHeight: Int, staking: Staking): Future[Unit] = if (latestBlockHeight == 0) {
    val insertAllSigningInfos = blockchainSigningInfos.Utility.insertAll()
    val insertAllValidators = blockchainValidators.Service.insertMultiple(staking.validators.getOrElse(Seq.empty).map(_.toValidator))
    //      val insertKeyBaseAccount = Future.traverse(staking.validators.map(_.toValidator))(validator => keyBaseValidatorAccounts.Utility.insertOrUpdateKeyBaseAccount(validator.operatorAddress, validator.description.identity))

    def updateDelegations(): Future[Unit] = {
      val insertAllDelegations = blockchainDelegations.Service.insertMultiple(staking.delegations.getOrElse(Seq.empty).map(_.toDelegation))
      val insertAllRedelegations = blockchainRedelegations.Service.insertMultiple(staking.redelegations.getOrElse(Seq.empty).map(_.toRedelegation))
      val insertAllUndelegations = blockchainUndelegations.Service.insertMultiple(staking.unbonding_delegations.getOrElse(Seq.empty).map(_.toUndelegation))

      for {
        _ <- insertAllDelegations
        _ <- insertAllRedelegations
        _ <- insertAllUndelegations
      } yield ()
    }

    (for {
      _ <- insertAllValidators
      _ <- updateDelegations()
      _ <- insertAllSigningInfos
      //          _ <- insertKeyBaseAccount
    } yield ()).recover {
      case baseException: BaseException => throw baseException
    }
  } else Future()

  private def updateDistributionOnStart(latestBlockHeight: Int, distribution: Distribution): Future[Unit] = if (latestBlockHeight == 0) {
    val insertAllWithdrawAddresses = blockchainWithdrawAddresses.Service.insertMultiple(distribution.delegator_withdraw_infos.getOrElse(Seq.empty).map(_.toWithdrawAddress))

    (for {
      _ <- insertAllWithdrawAddresses
    } yield ()).recover {
      case baseException: BaseException => throw baseException
    }
  } else Future()

  private def insertGenesisTransactionsOnStart(latestBlockHeight: Int, genTxs: Seq[GenTx]): Future[Unit] = if (latestBlockHeight == 0) {
    val updateTxs = Future.traverse(genTxs) { genTx =>
      val updateTx = Future.traverse(genTx.value.msg)(txMsg => blocksServices.actionOnTxMessages(txMsg.toStdMsg, 0))
      for {
        _ <- updateTx
      } yield ()
    }

    (for {
      _ <- updateTxs
    } yield ()).recover {
      case baseException: BaseException => throw baseException
    }
  } else Future()

  private def insertOrUpdateAllValidators(latestBlockHeight: Int): Future[Unit] = if (latestBlockHeight == 0) {
    val bondedValidators = getBondedValidators.Service.get()
    val unbondedValidators = getUnbondedValidators.Service.get()
    val unbondingValidators = getUnbondingValidators.Service.get()
    val insertSigningInfos = blockchainSigningInfos.Utility.insertAll()

    def insert(validatorResults: Seq[ValidatorResult]): Future[Unit] = {
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

    for {
      bondedValidators <- bondedValidators
      unbondedValidators <- unbondedValidators
      unbondingValidators <- unbondingValidators
      _ <- insert(bondedValidators.result ++ unbondedValidators.result ++ unbondingValidators.result)
      _ <- insertSigningInfos
    } yield ()
  } else Future()

  private def insertAllTokensOnStart(latestBlockHeight: Int): Future[Unit] = if (latestBlockHeight == 0) {
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

  def onLosingConnection(): Future[Unit] = {
    actors.Service.appWebSocketActor ! Json.toJson(actors.Message.WebSocket.BlockchainConnectionLost(true)).toString
    println("entered onLosingConnection")
    Thread.sleep(7000)
    println("onLosingConnection wait over")
    for {
      _ <- initialize()
    } yield ()
  }

  object WebSocketBlockchainClient {

    def start():Future[Unit] = {

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
      println("creating websocket connection...")
      def runWebSocketConnection(flow: Flow[Message, Message, Future[Done]]): (Future[WebSocketUpgradeResponse], Future[Done]) = {
        Http().singleWebSocketRequest(WebSocketRequest(wsURL), flow)
      }

      val (upgradeResponse, closed) = runWebSocketConnection(flow)

      val connected = upgradeResponse.flatMap { upgrade =>
        if (upgrade.response.status == StatusCodes.SwitchingProtocols) {
          Future(logger.info("Connection upgraded to websocket. StatusCode: " + upgrade.response.status.toString))(ec)
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

      closed.flatMap(_ => {
        logger.error("Websocket connection to blockchain closed.")
        println("connection was interrrupted-------------what hashakdhasd happendned")
        onLosingConnection()
      })(ec)
    }

    private def onNewBlock(newBlock: WSClientBlockResponse): Future[Unit] = {
      val latestExplorerHeight = blockchainBlocks.Service.getLatestBlockHeight
      val newEventsActions = actionsOnNewBlockEvents(newBlock.result.events)

      def insertBlocksOnNewBlock(latestExplorerHeight: Int, newIncomingHeight: Int): Unit = {
        (latestExplorerHeight + 1).until(newIncomingHeight).foreach { height =>
          val blockCommitResponse = blocksServices.insertOnBlock(height)

          def avgBlockTime(blockHeader: Header): Future[Double] = blocksServices.setAverageBlockTime(blockHeader)

          def insertTransactions(): Future[Seq[Transaction]] = blocksServices.insertTransactionsOnBlock(height)

          def checksAndUpdatesOnNewBlock(blockHeader: Header): Future[Unit] = blocksServices.checksAndUpdatesOnBlock(blockHeader)

          (for {
            blockCommitResponse <- blockCommitResponse
            transactions <- insertTransactions()
            avgBlockTime <- avgBlockTime(blockCommitResponse.result.signed_header.header)
            _ <- checksAndUpdatesOnNewBlock(blockCommitResponse.result.signed_header.header)
            _ <- blocksServices.sendNewBlockWebSocketMessage(blockCommitResponse = blockCommitResponse, transactions = transactions, averageBlockTime = avgBlockTime)
          } yield ()).recover {
            case baseException: BaseException => logger.error(baseException.failure.message)
          }
        }
      }

      (for {
        latestExplorerHeight <- latestExplorerHeight
        _ <- newEventsActions
      } yield insertBlocksOnNewBlock(latestExplorerHeight = latestExplorerHeight, newIncomingHeight = newBlock.result.data.value.block.header.height)
        ).recover {
        case baseException: BaseException => logger.error(baseException.failure.message)
      }
    }

    private def actionsOnNewBlockEvents(newBlockEvents: NewBlockEvents): Future[Unit] = {
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

    private def getBlock(message: String): WSClientBlockResponse = utilities.JSON.convertJsonStringToObject[WSClientBlockResponse](message)

    private def onStrictMessage(message: String): Unit = {
      val forComplete = (for {
        blockResponse <- Future(getBlock(message))
        _ <- onNewBlock(blockResponse)
      } yield ()).recover {
        case baseException: BaseException => logger.error(baseException.failure.logMessage)
      }
      Await.result(forComplete, Duration.Inf)
    }

    private def onStreamedMessage(message: Future[String]): Unit = {
      val forComplete = (for {
        message <- message
        blockResponse <- Future(getBlock(message))
        _ <- onNewBlock(blockResponse)
      } yield ()
        ).recover {
        case baseException: BaseException => logger.error(baseException.failure.logMessage)
      }
      Await.result(forComplete, Duration.Inf)
    }
  }

  private val initializeRunnable = new Runnable {
    def run(): Unit = runOnStartup()
  }

  actors.Service.actorSystem.scheduler.scheduleOnce(1000.millisecond, initializeRunnable)(schedulerExecutionContext)

}