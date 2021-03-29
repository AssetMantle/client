package services

import akka.actor.Cancellable
import exceptions.BaseException
import models.Abstract.Parameter
import models.blockchain.{Token, Transaction => blockchainTransaction}
import models.common.Parameters._
import models.{blockchain, keyBase}
import play.api.{Configuration, Logger}
import queries.Abstract.Account
import queries.blockchain.{GetABCIInfo, GetBlockResults, GetCommunityPool, GetMintingInflation, GetStakingPool, GetTotalSupply, GetUnbondedValidators, GetUnbondingValidators}
import queries.responses.blockchain.ABCIInfoResponse.{Response => ABCIInfoResponse}
import queries.responses.blockchain.BlockCommitResponse.{Response => BlockCommitResponse}
import queries.responses.blockchain.BlockResultResponse.{Response => BlockResultResponse}
import queries.responses.blockchain.CommunityPoolResponse.{Response => CommunityPoolResponse}
import queries.responses.blockchain.GenesisResponse.Bank.BankBalance
import queries.responses.blockchain.GenesisResponse._
import queries.responses.blockchain.MintingInflationResponse.{Response => MintingInflationResponse}
import queries.responses.blockchain.StakingPoolResponse.{Response => StakingPoolResponse}
import queries.responses.blockchain.TotalSupplyResponse.{Response => TotalSupplyResponse}
import queries.responses.common.Header
import utilities.MicroNumber

import javax.inject.{Inject, Singleton}
import scala.concurrent.duration.{Duration, DurationInt}
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.io.{Source => ScalaSource}

@Singleton
class Startup @Inject()(
                         blockchainAccounts: blockchain.Accounts,
                         blockchainBlocks: blockchain.Blocks,
                         blockchainBalances: blockchain.Balances,
                         blockchainParameters: blockchain.Parameters,
                         blockchainDelegations: blockchain.Delegations,
                         blockchainValidators: blockchain.Validators,
                         blocksServices: Block,
                         blockchainTokens: blockchain.Tokens,
                         blockchainRedelegations: blockchain.Redelegations,
                         blockchainUndelegations: blockchain.Undelegations,
                         blockchainWithdrawAddresses: blockchain.WithdrawAddresses,
                         keyBaseValidatorAccounts: keyBase.ValidatorAccounts,
                         getABCIInfo: GetABCIInfo,
                         getBlockResults: GetBlockResults,
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

  private val stakingDenom = configuration.get[String]("blockchain.stakingDenom")

  private val blockchainStartHeight = configuration.get[Int]("blockchain.startHeight")

  private val explorerInitialDelay = configuration.get[Int]("blockchain.explorer.initialDelay").millis

  private val explorerFixedDelay = configuration.get[Int]("blockchain.explorer.fixedDelay").millis

  private def onGenesis(): Future[Unit] = {
    val genesis = Future {
      val genesisSource = ScalaSource.fromFile(genesisFilePath)
      val genesis = utilities.JSON.convertJsonStringToObject[Genesis](genesisSource.mkString)
      genesisSource.close()
      genesis
    }

    (for {
      genesis <- genesis
      _ <- insertParametersOnStart(genesis.app_state.auth.params.toParameter, genesis.app_state.bank.params.toParameter, genesis.app_state.distribution.params.toParameter, genesis.app_state.gov.toParameter, genesis.app_state.halving.params.toParameter, genesis.app_state.mint.params.toParameter, genesis.app_state.slashing.params.toParameter, genesis.app_state.staking.params.toParameter)
      _ <- insertAccountsOnStart(genesis.app_state.auth.accounts)
      _ <- insertBalancesOnStart(genesis.app_state.bank.balances)
      _ <- updateStakingOnStart(genesis.app_state.staking)
      _ <- insertGenesisTransactionsOnStart(genesis.app_state.genutil.gen_txs, chainID = genesis.chain_id, initialHeight = genesis.initial_height.toInt, genesisTime = genesis.genesis_time)
      _ <- updateDistributionOnStart(genesis.app_state.distribution)
      _ <- insertAllTokensOnStart()
    } yield ()
      ).recover {
      case baseException: BaseException => throw baseException
    }
  }

  private def insertAccountsOnStart(accounts: Seq[Account]): Future[Seq[Unit]] = {
    utilitiesOperations.traverse(accounts) { account =>
      val upsertAccount = blockchainAccounts.Utility.insertOrUpdateAccountWithoutAnyTx(account.address)
      (for {
        _ <- upsertAccount
      } yield ()
        ).recover {
        case baseException: BaseException => throw baseException
      }
    }
  }

  private def insertBalancesOnStart(balances: Seq[BankBalance]): Future[Seq[Unit]] = {
    utilitiesOperations.traverse(balances) { balance =>
      val upsertAccount = blockchainBalances.Utility.insertOrUpdateBalance(balance.address)
      (for {
        _ <- upsertAccount
      } yield ()
        ).recover {
        case baseException: BaseException => throw baseException
      }
    }
  }

  private def updateStakingOnStart(staking: Staking.Module): Future[Unit] = {
    val insertAllValidators = blockchainValidators.Service.insertMultiple(staking.validators.map(_.toValidator))

    def updateDelegations(): Future[Unit] = {
      val insertAllDelegations = blockchainDelegations.Service.insertMultiple(staking.delegations.map(_.toDelegation))
      val insertAllRedelegations = blockchainRedelegations.Service.insertMultiple(staking.redelegations.map(_.toRedelegation))
      val insertAllUndelegations = blockchainUndelegations.Service.insertMultiple(staking.unbonding_delegations.map(_.toUndelegation))

      for {
        _ <- insertAllDelegations
        _ <- insertAllRedelegations
        _ <- insertAllUndelegations
      } yield ()
    }

    (for {
      _ <- insertAllValidators
      _ <- updateDelegations()
    } yield ()).recover {
      case baseException: BaseException => throw baseException
    }
  }

  private def updateDistributionOnStart(distribution: Distribution.Module): Future[Unit] = {
    val insertAllWithdrawAddresses = blockchainWithdrawAddresses.Service.insertMultiple(distribution.delegator_withdraw_infos.map(_.toWithdrawAddress))
    (for {
      _ <- insertAllWithdrawAddresses
    } yield ()).recover {
      case baseException: BaseException => throw baseException
    }
  }

  // IMPORTANT: Assuming all GenTxs are valid txs and successfully goes through
  private def insertGenesisTransactionsOnStart(genTxs: Seq[GenTx], chainID: String, initialHeight: Int, genesisTime: String): Future[Unit] = {
    val updateTxs = utilitiesOperations.traverse(genTxs) { genTx =>
      val updateTx = utilitiesOperations.traverse(genTx.body.messages)(txMsg => blocksServices.actionOnTxMessages(txMsg.toStdMsg)(Header(chain_id = chainID, height = initialHeight, time = genesisTime, data_hash = "", evidence_hash = "", validators_hash = "", proposer_address = "")))
      val updateAccount = utilitiesOperations.traverse(genTx.getSigners)(signer => blockchainAccounts.Utility.incrementSequence(signer))

      // Should always be called after messages are processed, otherwise can create conflict
      def updateBalance() = blockchainBalances.Utility.insertOrUpdateBalance(genTx.getFeePayer)

      for {
        _ <- updateTx
        _ <- updateAccount
        _ <- updateBalance()
      } yield ()
    }

    (for {
      _ <- updateTxs
    } yield ()).recover {
      case baseException: BaseException => throw baseException
    }
  }

  private def insertAllTokensOnStart(): Future[Unit] = {
    val totalSupplyResponse = getTotalSupply.Service.get
    val mintingInflationResponse = getMintingInflation.Service.get
    val stakingPoolResponse = getStakingPool.Service.get
    val communityPoolResponse = getCommunityPool.Service.get

    def insert(totalSupplyResponse: TotalSupplyResponse, mintingInflationResponse: MintingInflationResponse, stakingPoolResponse: StakingPoolResponse, communityPoolResponse: CommunityPoolResponse) = {
      blockchainTokens.Service.insertMultiple(totalSupplyResponse.supply.map(x => Token(denom = x.denom, totalSupply = x.amount,
        bondedAmount = if (x.denom == stakingDenom) stakingPoolResponse.pool.bonded_tokens else MicroNumber.zero,
        notBondedAmount = if (x.denom == stakingDenom) stakingPoolResponse.pool.not_bonded_tokens else MicroNumber.zero,
        communityPool = communityPoolResponse.pool.find(_.denom == x.denom).fold(MicroNumber.zero)(_.amount),
        inflation = if (x.denom == stakingDenom) BigDecimal(mintingInflationResponse.inflation) else BigDecimal(0.0)
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
      case baseException: BaseException => throw baseException
    }
  }

  private def insertParametersOnStart(parameters: Parameter*) = utilitiesOperations.traverse(parameters)(parameter => {
    val insert = blockchainParameters.Service.insertOrUpdate(blockchain.Parameter(parameterType = parameter.`type`, value = parameter))

    (for {
      _ <- insert
    } yield ()
      ).recover {
      case baseException: BaseException => throw baseException
    }
  })

  private def insertBlock(height: Int): Future[Unit] = {
    val blockCommitResponse = blocksServices.insertOnBlock(height)
    val blockResultResponse = getBlockResults.Service.get(height)

    def insertTransactions(blockHeader: Header): Future[Seq[blockchainTransaction]] = blocksServices.insertTransactionsOnBlock(blockHeader)

    def averageBlockTime(blockHeader: Header): Future[Double] = blocksServices.setAverageBlockTime(blockHeader)

    def checksAndUpdatesOnNewBlock(blockHeader: Header): Future[Unit] = blocksServices.checksAndUpdatesOnNewBlock(blockHeader)

    def sendNewBlockWebSocketMessage(blockCommitResponse: BlockCommitResponse, transactions: Seq[blockchainTransaction], averageBlockTime: Double) = blocksServices.sendNewBlockWebSocketMessage(blockCommitResponse = blockCommitResponse, transactions = transactions, averageBlockTime = averageBlockTime)

    (for {
      blockCommitResponse <- blockCommitResponse
      transactions <- insertTransactions(blockCommitResponse.result.signed_header.header)
      averageBlockTime <- averageBlockTime(blockCommitResponse.result.signed_header.header)
      _ <- checksAndUpdatesOnNewBlock(blockCommitResponse.result.signed_header.header)
      blockResultResponse <- blockResultResponse
      _ <- actionsOnEvents(blockResultResponse)
      _ <- sendNewBlockWebSocketMessage(blockCommitResponse = blockCommitResponse, transactions = transactions, averageBlockTime = averageBlockTime)
    } yield ()).recover {
      case baseException: BaseException => throw baseException
    }
  }

  private def actionsOnEvents(blockResultResponse: BlockResultResponse): Future[Unit] = {
    val slashing = blocksServices.onSlashingEvents(blockResultResponse.result.begin_block_events.filter(_.`type` == constants.Blockchain.Event.Slash).map(_.decode), blockResultResponse.result.height.toInt)
    val missedBlock = blocksServices.onMissedBlockEvents(blockResultResponse.result.begin_block_events.filter(_.`type` == constants.Blockchain.Event.Liveness).map(_.decode), blockResultResponse.result.height.toInt)
    val proposal = blocksServices.onProposalEvents(blockResultResponse.result.end_block_events.getOrElse(Seq()).filter(x => x.`type` == constants.Blockchain.Event.InactiveProposal || x.`type` == constants.Blockchain.Event.ActiveProposal).map(_.decode))
    val unbondingCompletion = blocksServices.onUnbondingCompletionEvents(blockResultResponse.result.begin_block_events.filter(_.`type` == constants.Blockchain.Event.CompleteUnbonding).map(_.decode))
    val redelegationCompletion = blocksServices.onRedelegationCompletionEvents(blockResultResponse.result.begin_block_events.filter(_.`type` == constants.Blockchain.Event.CompleteRedelegation).map(_.decode))
    (for {
      _ <- slashing
      _ <- missedBlock
      _ <- proposal
      _ <- unbondingCompletion
      _ <- redelegationCompletion
    } yield ()
      ).recover {
      case baseException: BaseException => throw baseException
    }
  }

  private val explorerRunnable = new Runnable {
    def run(): Unit = {
      //TODO Bug Source: Continuously emits sometimes when app starts - queries.blockchain.GetABCIInfo in application-akka.actor.default-dispatcher-66  - LOG.ILLEGAL_STATE_EXCEPTION
      //TODO java.lang.IllegalStateException: Closed
      //TODO (Runtime Exception) Explorer keeps on working fine
      //TODO (also may be akka.dispatch.TaskInvocation)
      //TODO ILLEGAL_STATE_EXCEPTION comes only once at start if the GET request is done Await.result(getABCIInfo.Service.get(), Duration, Inf)
      //TODO Tried changing explorerInitialDelay, explorerFixedDelay, actorSytem
      val abciInfo = getABCIInfo.Service.get()

      def latestExplorerHeight = blockchainBlocks.Service.getLatestBlockHeight

      def checkAndInsertBlock(abciInfo: ABCIInfoResponse, latestExplorerHeight: Int) = if (latestExplorerHeight == 0) {
        for {
          _ <- onGenesis()
          _ <- insertBlock(blockchainStartHeight)
        } yield ()
      } else {
        val updateBlockHeight = latestExplorerHeight + 1
        if (abciInfo.result.response.last_block_height.toInt > updateBlockHeight) insertBlock(updateBlockHeight)
        else Future()
      }

      val forComplete = (for {
        abciInfo <- abciInfo
        latestExplorerHeight <- latestExplorerHeight
        _ <- checkAndInsertBlock(abciInfo, latestExplorerHeight)
      } yield ()
        ).recover {
        case baseException: BaseException => if (baseException.failure == constants.Response.CONNECT_EXCEPTION) {
          actors.Service.appWebSocketActor ! actors.Message.WebSocket.BlockchainConnectionLost(true)
        } else {
          val latestExplorerHeight = blockchainBlocks.Service.getLatestBlockHeight
          for {
            latestExplorerHeight <- latestExplorerHeight
          } yield {
            logger.error(constants.Log.Messages.blockchainHeightUpdateFailed(latestExplorerHeight + 1))
          }
        }
        case exception: Exception => logger.error(exception.getLocalizedMessage)
      }
      //This Await ensures next app doesn't starts updating next block without completing the current one.
      Await.result(forComplete, Duration.Inf)
    }
  }

  //Needs to be called via function otherwise as soon as Startup gets injected, this runs (when without function) and probably INSERT_OR_UPDATE_TRIGGER doesnt work.
  def start(): Cancellable = actors.Service.actorSystem.scheduler.scheduleWithFixedDelay(initialDelay = explorerInitialDelay, delay = explorerFixedDelay)(explorerRunnable)(schedulerExecutionContext)

}