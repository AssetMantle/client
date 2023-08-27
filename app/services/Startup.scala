package services

import akka.actor.Cancellable
import exceptions.BaseException
import models.Abstract.Parameter
import models.blockchain.{Token, Validator, Transaction => blockchainTransaction}
import models.{blockchain, keyBase}
import play.api.{Configuration, Logger}
import queries.Abstract.Account
import queries.blockchain._
import queries.responses.blockchain.BlockCommitResponse.{Response => BlockCommitResponse}
import queries.responses.blockchain.BlockResultResponse.{Response => BlockResultResponse}
import queries.responses.blockchain.CommunityPoolResponse.{Response => CommunityPoolResponse}
import queries.responses.blockchain.GenesisResponse.Bank.BankBalance
import queries.responses.blockchain.GenesisResponse._
import queries.responses.blockchain.MintingInflationResponse.{Response => MintingInflationResponse}
import queries.responses.blockchain.StakingPoolResponse.{Response => StakingPoolResponse}
import queries.responses.blockchain.TotalSupplyResponse.{Response => TotalSupplyResponse}
import queries.responses.blockchain.common._
import queries.responses.common.Header
import utilities.Date.RFC3339
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
                         blockchainAuthorizations: blockchain.Authorizations,
                         blockchainFeeGrants: blockchain.FeeGrants,
                         blocksServices: Block,
                         blockchainTokens: blockchain.Tokens,
                         blockchainRedelegations: blockchain.Redelegations,
                         blockchainUndelegations: blockchain.Undelegations,
                         blockchainWithdrawAddresses: blockchain.WithdrawAddresses,
                         blockchainAssets: blockchain.Assets,
                         blockchainClassifications: blockchain.Classifications,
                         blockchainIdentities: blockchain.Identities,
                         blockchainOrders: blockchain.Orders,
                         blockchainMaintainers: blockchain.Maintainers,
                         blockchainSplits: blockchain.Splits,
                         blockchainMetas: blockchain.Metas,
                         keyBaseValidatorAccounts: keyBase.ValidatorAccounts,
                         getABCIInfo: GetABCIInfo,
                         getBlockResults: GetBlockResults,
                         getTotalSupply: GetTotalSupply,
                         getStakingPool: GetStakingPool,
                         getMintingInflation: GetMintingInflation,
                         getCommunityPool: GetCommunityPool,
                         utilitiesOperations: utilities.Operations,
                         archiving: Archiving,
                       )(implicit exec: ExecutionContext, configuration: Configuration) {

  private implicit val module: String = constants.Module.SERVICES_STARTUP

  private implicit val logger: Logger = Logger(this.getClass)

  private val schedulerExecutionContext: ExecutionContext = actors.Service.actorSystem.dispatchers.lookup("akka.actor.scheduler-dispatcher")

  private val genesisFilePath = configuration.get[String]("blockchain.genesisFilePath")

  private val blockchainStartHeight = configuration.get[Int]("blockchain.startHeight")

  private val explorerInitialDelay = configuration.get[Int]("blockchain.explorer.initialDelay").millis

  private val explorerFixedDelay = configuration.get[Int]("blockchain.explorer.fixedDelay").millis

  archiving.setLastArchiveHeight()

  private def onGenesis(): Future[Unit] = {
    val genesis = Future {
      val genesisSource = ScalaSource.fromFile(genesisFilePath)
      val jsonString = try genesisSource.mkString finally genesisSource.close()
      utilities.JSON.convertJsonStringToObject[Genesis](jsonString)
    }

    (for {
      genesis <- genesis
      _ <- insertParametersOnStart(Seq(genesis.app_state.auth.params.toParameter, genesis.app_state.bank.params.toParameter, genesis.app_state.distribution.params.toParameter, genesis.app_state.gov.toParameter, genesis.app_state.mint.params.toParameter, genesis.app_state.slashing.params.toParameter, genesis.app_state.staking.params.toParameter) ++ Seq(genesis.app_state.assets.map(_.parameter_list.getAssetParameter), genesis.app_state.classifications.map(_.parameter_list.getClassificationParameter), genesis.app_state.identities.map(_.parameter_list.getIdentityParameter), genesis.app_state.maintainers.map(_.parameter_list.getMaintainerParameter), genesis.app_state.metas.map(_.parameter_list.getMetaParameter), genesis.app_state.orders.map(_.parameter_list.getOrderParameter), genesis.app_state.splits.map(_.parameter_list.getSplitParameter)).flatten)
      _ <- insertAccountsOnStart(genesis.app_state.auth.accounts)
      _ <- insertBalancesOnStart(genesis.app_state.bank.balances)
      _ <- updateStakingOnStart(genesis.app_state.staking)
      _ <- insertGenesisTransactionsOnStart(genesis.app_state.genutil.gen_txs, chainID = genesis.chain_id, initialHeight = genesis.initial_height.toInt, genesisTime = genesis.genesis_time)
      _ <- updateDistributionOnStart(genesis.app_state.distribution)
      _ <- insertAllTokensOnStart()
      _ <- insertAuthorizationsOnStart(genesis.app_state.authz.authorization)
      _ <- insertFeeGrantsOnStart(genesis.app_state.feegrant.allowances)
      _ <- if (genesis.app_state.metas.isDefined) insertMetasOnStart(genesis.app_state.metas.get.records.map(_.mappable)) else Future()
      _ <- if (genesis.app_state.classifications.isDefined) insertClassificationsOnStart(genesis.app_state.classifications.get.records.map(_.mappable)) else Future()
      _ <- if (genesis.app_state.maintainers.isDefined) insertMaintainersOnStart(genesis.app_state.maintainers.get.records.map(_.mappable)) else Future()
      _ <- if (genesis.app_state.assets.isDefined) insertAssetsOnStart(genesis.app_state.assets.get.records.map(_.mappable)) else Future()
      _ <- if (genesis.app_state.identities.isDefined) insertIdentitiesOnStart(genesis.app_state.identities.get.records.map(_.mappable)) else Future()
      _ <- if (genesis.app_state.splits.isDefined) insertSplitsOnStart(genesis.app_state.splits.get.records.map(_.mappable)) else Future()
      _ <- if (genesis.app_state.orders.isDefined) insertOrdersOnStart(genesis.app_state.orders.get.records.map(_.mappable)) else Future()
    } yield ()
      ).recover {
      case exception: Exception => logger.error(exception.getLocalizedMessage)
        constants.Response.NO_RESPONSE.throwBaseException(exception)
    }
  }

  private def insertAccountsOnStart(accounts: Seq[Account]): Future[Seq[Unit]] = {
    val bcAccountsCount = Await.result(blockchainAccounts.Service.getTotalAccounts, Duration.Inf)
    if (accounts.length > bcAccountsCount) {
      val allAddresses = Await.result(blockchainAccounts.Service.getAllAddressess, Duration.Inf)
      utilitiesOperations.traverse(accounts.map(_.address).diff(allAddresses)) { address =>
        val upsertAccount = blockchainAccounts.Utility.insertOrUpdateAccount(address)

        for {
          _ <- upsertAccount
        } yield ()
      }
    } else Future(Seq())
  }

  private def insertBalancesOnStart(balances: Seq[BankBalance]): Future[Seq[Unit]] = {
    val bcAccountsCount = Await.result(blockchainBalances.Service.getTotalAccounts, Duration.Inf)
    if (balances.length > bcAccountsCount) {
      val allAddresses = Await.result(blockchainBalances.Service.getAllAddressess, Duration.Inf)
      utilitiesOperations.traverse(balances.map(_.address).diff(allAddresses)) { address =>
        val upsertAccount = blockchainBalances.Utility.insertOrUpdateBalance(address)

        for {
          _ <- upsertAccount
        } yield ()
      }
    } else Future(Seq())
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

    for {
      _ <- insertAllValidators
      _ <- updateDelegations()
    } yield ()
  }

  private def updateDistributionOnStart(distribution: Distribution.Module): Future[Unit] = {
    val insertAllWithdrawAddresses = blockchainWithdrawAddresses.Service.insertMultiple(distribution.delegator_withdraw_infos.map(_.toWithdrawAddress))

    for {
      _ <- insertAllWithdrawAddresses
    } yield ()
  }

  // IMPORTANT: Assuming all GenTxs are valid txs and successfully goes through
  private def insertGenesisTransactionsOnStart(genTxs: Seq[GenTx], chainID: String, initialHeight: Int, genesisTime: RFC3339): Future[Unit] = {
    val updateTxs = utilitiesOperations.traverse(genTxs) { genTx =>
      val updateTx = utilitiesOperations.traverse(genTx.body.messages)(msg => blockchainValidators.Utility.insertOrUpdateValidator(msg.validator_address))

      def insertDelegation() = utilitiesOperations.traverse(genTx.body.messages)(msg => blockchainDelegations.Utility.upsertOrDelete(delegatorAddress = msg.delegator_address, validatorAddress = msg.validator_address))

      def insertKeyBaseAccount(validators: Seq[Validator]) = utilitiesOperations.traverse(validators)(validator => keyBaseValidatorAccounts.Utility.insertOrUpdateKeyBaseAccount(validator.operatorAddress, validator.description.identity))

      def updateAccount(signers: Seq[String]) = utilitiesOperations.traverse(signers)(signer => blockchainAccounts.Utility.insertOrUpdateAccount(signer))

      // Should always be called after messages are processed, otherwise can create conflict
      def updateBalance(signers: Seq[String]) = utilitiesOperations.traverse(signers)(signer => blockchainBalances.Utility.insertOrUpdateBalance(signer))

      for {
        validators <- updateTx
        _ <- insertDelegation()
        _ <- updateAccount(genTx.body.messages.map(_.delegator_address))
        _ <- updateBalance(genTx.body.messages.map(_.delegator_address))
        _ <- insertKeyBaseAccount(validators)
      } yield ()
    }

    for {
      _ <- updateTxs
    } yield ()
  }

  private def insertAllTokensOnStart(): Future[Unit] = {
    val totalSupplyResponse = getTotalSupply.Service.get
    val mintingInflationResponse = getMintingInflation.Service.get
    val stakingPoolResponse = getStakingPool.Service.get
    val communityPoolResponse = getCommunityPool.Service.get

    def insert(totalSupplyResponse: TotalSupplyResponse, mintingInflationResponse: MintingInflationResponse, stakingPoolResponse: StakingPoolResponse, communityPoolResponse: CommunityPoolResponse) = {
      blockchainTokens.Service.insertMultiple(totalSupplyResponse.supply.map(x => Token(denom = x.denom, totalSupply = x.amount,
        bondedAmount = if (x.denom == constants.Blockchain.StakingDenom) stakingPoolResponse.pool.bonded_tokens else MicroNumber.zero,
        notBondedAmount = if (x.denom == constants.Blockchain.StakingDenom) stakingPoolResponse.pool.not_bonded_tokens else MicroNumber.zero,
        communityPool = communityPoolResponse.pool.find(_.denom == x.denom).fold(MicroNumber.zero)(_.amount),
        inflation = if (x.denom == constants.Blockchain.StakingDenom) BigDecimal(mintingInflationResponse.inflation) else BigDecimal(0.0),
        totalLocked = MicroNumber.zero,
        totalBurnt = MicroNumber.zero
      )))
    }

    for {
      totalSupplyResponse <- totalSupplyResponse
      mintingInflationResponse <- mintingInflationResponse
      stakingPoolResponse <- stakingPoolResponse
      communityPoolResponse <- communityPoolResponse
      _ <- insert(totalSupplyResponse, mintingInflationResponse, stakingPoolResponse, communityPoolResponse)
    } yield ()
  }

  private def insertParametersOnStart(parameters: Seq[Parameter]) = {
    utilitiesOperations.traverse(parameters)(parameter => {
      val insert = blockchainParameters.Service.insertOrUpdate(blockchain.Parameter(parameterType = parameter.parameterType, value = parameter))

      for {
        _ <- insert
      } yield ()
    })
  }

  def insertAuthorizationsOnStart(authorizations: Seq[Authz.Authorization]): Future[Seq[Unit]] = utilitiesOperations.traverse(authorizations)(authorization => {
    val insert = blockchainAuthorizations.Service.insertOrUpdate(blockchain.Authorization(granter = authorization.granter, grantee = authorization.grantee, msgTypeURL = authorization.authorization.value.toSerializable.getMsgTypeURL, grantedAuthorization = authorization.authorization.toSerializable.toAnyProto.toByteString.toByteArray, expiration = authorization.expiration.epoch))

    for {
      _ <- insert
    } yield ()
  })

  def insertFeeGrantsOnStart(allowances: Seq[FeeGrant.Allowance]): Future[Seq[Unit]] = utilitiesOperations.traverse(allowances)(allowance => {
    val insert = blockchainFeeGrants.Service.insertOrUpdate(blockchain.FeeGrant(granter = allowance.granter, grantee = allowance.grantee, allowance = allowance.allowance.value.toSerializable.toProto.toByteString.toByteArray))
    for {
      _ <- insert
    } yield ()
  })

  def insertMetasOnStart(metas: Seq[Meta.Mappable]): Future[Int] = blockchainMetas.Service.add(metas.map(_.data.toData))

  def insertClassificationsOnStart(classifications: Seq[Classification.Mappable]): Future[Int] = blockchainClassifications.Service.add(classifications.map(_.classification.toClassification))

  def insertMaintainersOnStart(maintainers: Seq[Maintainer.Mappable]): Future[Int] = blockchainMaintainers.Service.add(maintainers.map(_.maintainer.toMaintainer))

  def insertAssetsOnStart(assets: Seq[Asset.Mappable]): Future[Int] = blockchainAssets.Service.add(assets.map(_.asset.toAsset))

  def insertIdentitiesOnStart(identities: Seq[Identity.Mappable]): Future[Int] = blockchainIdentities.Service.add(identities.map(_.identity.toIdentity))

  def insertSplitsOnStart(splits: Seq[Split.Mappable]): Future[Int] = blockchainSplits.Service.add(splits.map(_.split.toSplit))

  def insertOrdersOnStart(orders: Seq[Order.Mappable]): Future[Int] = blockchainOrders.Service.add(orders.map(_.order.toOrder))

  private def insertBlock(height: Int): Future[Unit] = {
    val blockCommitResponse = blocksServices.insertOnBlock(height)
    val blockResultResponse = getBlockResults.Service.get(height)

    def insertTransactions(blockHeader: Header): Future[Seq[blockchainTransaction]] = blocksServices.insertTransactionsOnBlock(blockHeader)

    def getAverageBlockTime(blockHeader: Header): Future[Double] = blockchainBlocks.Utility.getAverageBlockTime(fromBlock = Option(blockHeader.height))

    def checksAndUpdatesOnNewBlock(blockHeader: Header): Future[Unit] = blocksServices.checksAndUpdatesOnNewBlock(blockHeader)

    def sendNewBlockWebSocketMessage(blockCommitResponse: BlockCommitResponse, transactions: Seq[blockchainTransaction], averageBlockTime: Double) = blocksServices.sendNewBlockWebSocketMessage(blockCommitResponse = blockCommitResponse, transactions = transactions, averageBlockTime = averageBlockTime)

    for {
      blockCommitResponse <- blockCommitResponse
      transactions <- insertTransactions(blockCommitResponse.result.signed_header.header)
      averageBlockTime <- getAverageBlockTime(blockCommitResponse.result.signed_header.header)
      _ <- sendNewBlockWebSocketMessage(blockCommitResponse = blockCommitResponse, transactions = transactions, averageBlockTime = averageBlockTime)
      blockResultResponse <- blockResultResponse
      _ <- actionsOnEvents(blockResultResponse = blockResultResponse, currentBlockTimeStamp = blockCommitResponse.result.signed_header.header.time)
      _ <- checksAndUpdatesOnNewBlock(blockCommitResponse.result.signed_header.header)
    } yield ()
  }

  private def actionsOnEvents(blockResultResponse: BlockResultResponse, currentBlockTimeStamp: RFC3339): Future[Unit] = {
    val slashing = blocksServices.onSlashingEvents(blockResultResponse.result.getSlashingEvents, blockResultResponse.result.height.toInt)
    val missedBlock = blocksServices.onMissedBlockEvents(blockResultResponse.result.getLivenessEvents, blockResultResponse.result.height.toInt)
    val unbondingCompletion = blocksServices.onUnbondingCompletionEvents(unbondingCompletionEvents = blockResultResponse.result.end_block_events.getOrElse(Seq()).filter(_.`type` == schema.constants.Event.CompleteUnbonding).map(_.decode), currentBlockTimeStamp = currentBlockTimeStamp)
    val redelegationCompletion = blocksServices.onRedelegationCompletionEvents(redelegationCompletionEvents = blockResultResponse.result.end_block_events.getOrElse(Seq()).filter(_.`type` == schema.constants.Event.CompleteRedelegation).map(_.decode), currentBlockTimeStamp = currentBlockTimeStamp)
    val proposal = blocksServices.onProposalEvents(blockResultResponse.result.getActiveInactiveProposalEvents, blockResultResponse.result.txs_results.getOrElse(Seq()))

    for {
      _ <- slashing
      _ <- missedBlock
      _ <- unbondingCompletion
      _ <- redelegationCompletion
      _ <- proposal
    } yield ()
  }

  private val explorerRunnable = new Runnable {
    def run(): Unit = if (!utilities.Scheduler.getSignalReceived) {
      //TODO Bug Source: Continuously emits sometimes when app starts - queries.blockchain.GetABCIInfo in application-akka.actor.default-dispatcher-66  - LOG.ILLEGAL_STATE_EXCEPTION
      //TODO java.lang.IllegalStateException: Closed
      //TODO (Runtime Exception) Explorer keeps on working fine
      //TODO (also may be akka.dispatch.TaskInvocation)
      //TODO ILLEGAL_STATE_EXCEPTION comes only once at start if the GET request is done Await.result(getABCIInfo.Service.get(), Duration, Inf)
      //TODO Tried changing explorerInitialDelay, explorerFixedDelay, actorSytem
      val abciInfo = getABCIInfo.Service.get()

      def latestExplorerBlockHeight = blockchainBlocks.Service.getLatestBlockHeight

      def checkAndInsertBlock(latestChainHeight: Int, latestExplorerBlockHeight: Int) = if (latestExplorerBlockHeight == 0) {
        for {
          _ <- onGenesis()
          _ <- insertBlock(blockchainStartHeight)
        } yield ()
      } else {
        val archive = if (latestExplorerBlockHeight > 0 && latestExplorerBlockHeight / 10000 == 0) archiving.checkAndUpdate(latestExplorerBlockHeight) else Future(0)

        val updateBlockHeight = latestExplorerBlockHeight + 1

        val processBlock = if (latestChainHeight > updateBlockHeight) {
          for {
            _ <- insertBlock(updateBlockHeight)
          } yield {
            if (latestChainHeight - latestExplorerBlockHeight > 10) blocksServices.setSyncing(true) else blocksServices.setSyncing(false)
          }
        } else Future()

        for {
          _ <- archive
          _ <- processBlock
        } yield ()
      }

      val forComplete = (for {
        abciInfo <- abciInfo
        latestExplorerBlockHeight <- latestExplorerBlockHeight
        _ <- checkAndInsertBlock(abciInfo.result.response.last_block_height.toInt, latestExplorerBlockHeight)
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
    } else utilities.Scheduler.shutdownThread()
  }

  //Needs to be called via function otherwise as soon as Startup gets injected, this runs (when without function) and probably INSERT_OR_UPDATE_TRIGGER doesnt work.
  def start(): Cancellable = actors.Service.actorSystem.scheduler.scheduleWithFixedDelay(initialDelay = explorerInitialDelay, delay = explorerFixedDelay)(explorerRunnable)(schedulerExecutionContext)

}