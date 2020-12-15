package services

import akka.actor.Cancellable
import exceptions.BaseException
import models.blockchain.{Parameter, Token, Transaction => blockchainTransaction}
import models.common.Parameters._
import models.{blockchain, keyBase}
import play.api.{Configuration, Logger}
import play.libs.Json
import queries._
import queries.responses.ABCIInfoResponse.{Response => ABCIInfoResponse}
import queries.responses.BlockCommitResponse.{Response => BlockCommitResponse}
import queries.responses.BlockResultResponse.{Response => BlockResultResponse}
import queries.responses.CommunityPoolResponse.{Response => CommunityPoolResponse}
import queries.responses.GenesisResponse._
import queries.responses.MintingInflationResponse.{Response => MintingInflationResponse}
import queries.responses.StakingPoolResponse.{Response => StakingPoolResponse}
import queries.responses.TotalSupplyResponse.{Response => TotalSupplyResponse}
import queries.responses.common.Validator.{Result => ValidatorResult}
import queries.responses.common.{Account, Header}
import utilities.MicroNumber

import javax.inject.{Inject, Singleton}
import scala.concurrent.duration.{Duration, DurationInt}
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.io.{Source => ScalaSource}

@Singleton
class Startup @Inject()(
                         blockchainAccounts: blockchain.Accounts,
                         blockchainBlocks: blockchain.Blocks,
                         blockchainParameters: blockchain.Parameters,
                         blockchainDelegations: blockchain.Delegations,
                         blockchainSigningInfos: blockchain.SigningInfos,
                         blockchainValidators: blockchain.Validators,
                         blocksServices: Block,
                         blockchainTokens: blockchain.Tokens,
                         blockchainRedelegations: blockchain.Redelegations,
                         blockchainUndelegations: blockchain.Undelegations,
                         blockchainWithdrawAddresses: blockchain.WithdrawAddresses,
                         keyBaseValidatorAccounts: keyBase.ValidatorAccounts,
                         getABCIInfo: GetABCIInfo,
                         getBlockResults: GetBlockResults,
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

  private val stakingDenom = configuration.get[String]("blockchain.stakingDenom")

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
      _ <- insertAccountsOnStart(genesis.app_state.auth.accounts)
      _ <- updateStakingOnStart(genesis.app_state.staking)
      _ <- insertGenesisTransactionsOnStart(genesis.app_state.genutil.gentxs.getOrElse(Seq.empty))
      _ <- updateDistributionOnStart(genesis.app_state.distribution)
      _ <- updateSlashingOnStart(genesis.app_state.slashing)
      _ <- insertAllTokensOnStart()
    } yield ()
      ).recover {
      case baseException: BaseException => throw baseException
    }
  }

  //  private def insertBlocksOnStart(latestBlockHeight: Int): Future[Unit] = Future {
  //    try {
  //      var blockHeight = latestBlockHeight + 1
  //      while (true) {
  //        val blockCommitResponse = blocksServices.insertOnBlock(blockHeight)
  //
  //        def transactions: Future[Seq[Transaction]] = blocksServices.insertTransactionsOnBlock(blockHeight)
  //
  //        def avgBlockTime(blockCommitResponse: BlockCommitResponse.Response): Future[Double] = blocksServices.setAverageBlockTime(blockCommitResponse.result.signed_header.header)
  //
  //        def checksAndUpdatesOnBlock(blockCommitResponse: BlockCommitResponse.Response): Future[Unit] = blocksServices.checksAndUpdatesOnBlock(blockCommitResponse.result.signed_header.header)
  //
  //        def sendWebSocketMessage(blockCommitResponse: BlockCommitResponse.Response, transactions: Seq[Transaction], avgBlockTime: Double) = blocksServices.sendNewBlockWebSocketMessage(blockCommitResponse = blockCommitResponse, transactions = transactions, averageBlockTime = avgBlockTime)
  //
  //        val forComplete = for {
  //          blockCommitResponse <- blockCommitResponse
  //          transactions <- transactions
  //          avgBlockTime <- avgBlockTime(blockCommitResponse)
  //          _ <- checksAndUpdatesOnBlock(blockCommitResponse)
  //          _ <- sendWebSocketMessage(blockCommitResponse, transactions, avgBlockTime)
  //        } yield ()
  //        Await.result(forComplete, Duration.Inf)
  //        blockHeight = blockHeight + 1
  //      }
  //    } catch {
  //      case baseException: BaseException => if (baseException.failure != constants.Response.BLOCK_QUERY_FAILED) {
  //        throw baseException
  //      } else ()
  //    }
  //  }

  private def insertAccountsOnStart(accounts: Seq[Account.Result]): Future[Seq[Unit]] = {
    utilitiesOperations.traverse(accounts) { account =>
      val upsert = blockchainAccounts.Utility.insertOrUpdateAccountBalance(address = account.value.address)
      (for {
        _ <- upsert
      } yield ()
        ).recover {
        case baseException: BaseException => throw baseException
      }
    }
  }

  private def updateStakingOnStart(staking: Staking): Future[Unit] = {
    val insertAllSigningInfos = blockchainSigningInfos.Utility.insertAll()
    val insertAllValidators = blockchainValidators.Service.insertMultiple(staking.validators.getOrElse(Seq.empty).map(_.toValidator))
    val insertStakingParameters = blockchainParameters.Service.insertOrUpdate(Parameter(parameterType = constants.Blockchain.ParameterType.STAKING, value = StakingParameter(unbondingTime = BigInt(staking.params.unbonding_time), maxValidators = staking.params.max_validators, maxEntries = staking.params.max_entries, historicalEntries = staking.params.historical_entries, bondDenom = staking.params.bond_denom)))
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
      _ <- insertStakingParameters
      //          _ <- insertKeyBaseAccount
    } yield ()).recover {
      case baseException: BaseException => throw baseException
    }
  }

  private def updateDistributionOnStart(distribution: Distribution): Future[Unit] = {
    val insertAllWithdrawAddresses = blockchainWithdrawAddresses.Service.insertMultiple(distribution.delegator_withdraw_infos.getOrElse(Seq.empty).map(_.toWithdrawAddress))
    val insertDistributionParameters = blockchainParameters.Service.insertOrUpdate(Parameter(parameterType = constants.Blockchain.ParameterType.DISTRIBUTION, value = DistributionParameter(communityTax = distribution.params.community_tax, baseProposerReward = distribution.params.base_proposer_reward, bonusProposerReward = distribution.params.bonus_proposer_reward, withdrawAddrEnabled = distribution.params.withdraw_addr_enabled)))
    (for {
      _ <- insertAllWithdrawAddresses
      _ <- insertDistributionParameters
    } yield ()).recover {
      case baseException: BaseException => throw baseException
    }
  }

  private def updateSlashingOnStart(slashing: Slashing): Future[Unit] = {
    val insertSlashingParameters = blockchainParameters.Service.insertOrUpdate(Parameter(parameterType = constants.Blockchain.ParameterType.SLASHING, value = SlashingParameter(signedBlocksWindow = slashing.params.signed_blocks_window.toInt, minSignedPerWindow = slashing.params.min_signed_per_window, downtimeJailDuration = BigInt(slashing.params.downtime_jail_duration), slashFractionDoubleSign = slashing.params.slash_fraction_double_sign, slashFractionDowntime = slashing.params.slash_fraction_downtime)))
    (for {
      _ <- insertSlashingParameters
    } yield ()).recover {
      case baseException: BaseException => throw baseException
    }
  }

  private def insertGenesisTransactionsOnStart(genTxs: Seq[GenTx]): Future[Unit] = {
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
  }

  private def insertOrUpdateAllValidators(): Future[Unit] = {
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
        case baseException: BaseException => throw baseException
      }
    }

    for {
      bondedValidators <- bondedValidators
      unbondedValidators <- unbondedValidators
      unbondingValidators <- unbondingValidators
      _ <- insert(bondedValidators.result ++ unbondedValidators.result ++ unbondingValidators.result)
      _ <- insertSigningInfos
    } yield ()
  }

  private def insertAllTokensOnStart(): Future[Unit] = {
    val totalSupplyResponse = getTotalSupply.Service.get
    val mintingInflationResponse = getMintingInflation.Service.get
    val stakingPoolResponse = getStakingPool.Service.get
    val communityPoolResponse = getCommunityPool.Service.get

    def insert(totalSupplyResponse: TotalSupplyResponse, mintingInflationResponse: MintingInflationResponse, stakingPoolResponse: StakingPoolResponse, communityPoolResponse: CommunityPoolResponse) = {
      blockchainTokens.Service.insertMultiple(totalSupplyResponse.result.map(x => Token(denom = x.denom, totalSupply = x.amount,
        bondedAmount = if (x.denom == stakingDenom) stakingPoolResponse.result.bonded_tokens else MicroNumber.zero,
        notBondedAmount = if (x.denom == stakingDenom) stakingPoolResponse.result.not_bonded_tokens else MicroNumber.zero,
        communityPool = communityPoolResponse.result.find(_.denom == x.denom).fold(MicroNumber.zero)(_.amount),
        inflation = if (x.denom == stakingDenom) mintingInflationResponse.result else BigDecimal(0.0)
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

  private def insertBlock(height: Int): Future[Unit] = {
    val blockCommitResponse = blocksServices.insertOnBlock(height)
    val blockResultResponse = getBlockResults.Service.get(height)

    def insertTransactions(): Future[Seq[blockchainTransaction]] = blocksServices.insertTransactionsOnBlock(height)

    def averageBlockTime(blockHeader: Header): Future[Double] = blocksServices.setAverageBlockTime(blockHeader)

    def checksAndUpdatesOnNewBlock(blockHeader: Header): Future[Unit] = blocksServices.checksAndUpdatesOnBlock(blockHeader)

    def sendNewBlockWebSocketMessage(blockCommitResponse: BlockCommitResponse, transactions: Seq[blockchainTransaction], averageBlockTime: Double) = blocksServices.sendNewBlockWebSocketMessage(blockCommitResponse = blockCommitResponse, transactions = transactions, averageBlockTime = averageBlockTime)

    (for {
      blockCommitResponse <- blockCommitResponse
      transactions <- insertTransactions()
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
    val slashing = blocksServices.onSlashingEvent(blockResultResponse.result.begin_block_events.filter(_.eventType == constants.Blockchain.Event.Slash).map(_.decode), blockResultResponse.result.height.toInt)
    val missedBlock = blocksServices.onMissedBlockEvent(blockResultResponse.result.begin_block_events.filter(_.eventType == constants.Blockchain.Event.Liveness).map(_.decode), blockResultResponse.result.height.toInt)
    (for {
      _ <- slashing
      _ <- missedBlock
    } yield ()
      ).recover {
      case baseException: BaseException => throw baseException
    }
  }

  private val explorerRunnable = new Runnable {
    def run(): Unit = {
      //TODO Bug Source: Continuously emits sometimes when app starts - queries.GetABCIInfo in application-akka.actor.default-dispatcher-66  - LOG.ILLEGAL_STATE_EXCEPTION
      //TODO java.lang.IllegalStateException: Closed
      //TODO (Runtime Exception) Explorer keeps on working fine
      //TODO (also may be akka.dispatch.TaskInvocation)
      //TODO ILLEGAL_STATE_EXCEPTION comes only once at start if the GET request is done Await.result(getABCIInfo.Service.get(), Duration, Inf)
      //TODO Tried changing explorerInitialDelay, explorerFixedDelay, actorSytem
      val abciInfo = Await.result(getABCIInfo.Service.get(), Duration.Inf)

      def latestExplorerHeight = blockchainBlocks.Service.getLatestBlockHeight

      def checkAndInsertBlock(abciInfo: ABCIInfoResponse, latestExplorerHeight: Int) = if (latestExplorerHeight == 0) {
        for {
          _ <- onGenesis()
          _ <- insertBlock(1)
        } yield ()
      } else {
        val updateBlockHeight = latestExplorerHeight + 1
        if (abciInfo.result.response.last_block_height.toInt > updateBlockHeight) insertBlock(updateBlockHeight)
        else Future()
      }

      val forComplete = (for {
//        abciInfo <- abciInfo
        latestExplorerHeight <- latestExplorerHeight
        _ <- checkAndInsertBlock(abciInfo, latestExplorerHeight)
      } yield ()
        ).recover {
        case baseException: BaseException => if (baseException.failure == constants.Response.CONNECT_EXCEPTION) {
          actors.Service.appWebSocketActor ! Json.toJson(actors.Message.WebSocket.BlockchainConnectionLost(true)).toString
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