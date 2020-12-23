package controllers

import controllers.actions._
import exceptions.BaseException
import models.blockchain._
import models.masterTransaction.TokenPrice
import models.{blockchain, master, masterTransaction}
import play.api.i18n.I18nSupport
import play.api.mvc._
import play.api.{Configuration, Logger}
import queries.{GetDelegatorRewards, GetValidatorSelfBondAndCommissionRewards}
import utilities.MicroNumber

import javax.inject.{Inject, Singleton}
import scala.collection.immutable.ListMap
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ComponentViewController @Inject()(
                                         blockchainAccounts: blockchain.Accounts,
                                         blockchainAssets: blockchain.Assets,
                                         blockchainDelegations: blockchain.Delegations,
                                         blockchainIdentities: blockchain.Identities,
                                         blockchainOrders: blockchain.Orders,
                                         blockchainRedelegations: blockchain.Redelegations,
                                         blockchainSplits: blockchain.Splits,
                                         blockchainUndelegations: blockchain.Undelegations,
                                         blockchainBlocks: blockchain.Blocks,
                                         blockchainAverageBlockTimes: blockchain.AverageBlockTimes,
                                         blockchainTransactions: blockchain.Transactions,
                                         blockchainTokens: blockchain.Tokens,
                                         blockchainValidators: blockchain.Validators,
                                         getDelegatorRewards: GetDelegatorRewards,
                                         getValidatorSelfBondAndCommissionRewards: GetValidatorSelfBondAndCommissionRewards,
                                         masterTransactionTokenPrices: masterTransaction.TokenPrices,
                                         withoutLoginAction: WithoutLoginAction,
                                         withoutLoginActionAsync: WithoutLoginActionAsync,
                                         messagesControllerComponents: MessagesControllerComponents,
                                         masterAccountFiles: master.AccountFiles,
                                         masterAccountKYCs: master.AccountKYCs,
                                         masterAssets: master.Assets,
                                         masterOrders: master.Orders,
                                         masterClassifications: master.Classifications,
                                         masterIdentities: master.Identities,
                                         masterSplits: master.Splits,
                                         masterIdentifications: master.Identifications,
                                         masterProperties: master.Properties,
                                         withLoginAction: WithLoginAction,
                                       )(implicit configuration: Configuration, executionContext: ExecutionContext) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_COMPONENT_VIEW

  private val bondedStatus = configuration.get[Int]("blockchain.validator.status.bonded")

  private val stakingDenom = configuration.get[String]("blockchain.stakingDenom")

  private val chainID = configuration.get[String]("blockchain.chainID")

  def commonHome: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      Future(Ok(views.html.component.master.commonHome()))
  }

  def recentActivities: Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
      Ok(views.html.component.master.recentActivities())
  }

  def profilePicture(): Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val profilePicture = masterAccountFiles.Service.getProfilePicture(loginState.username)
      (for {
        profilePicture <- profilePicture
      } yield Ok(views.html.profilePicture(profilePicture))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def identification: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val accountKYC = masterAccountKYCs.Service.get(loginState.username, constants.File.AccountKYC.IDENTIFICATION)
      val identification = masterIdentifications.Service.get(loginState.username)
      (for {
        accountKYC <- accountKYC
        identification <- identification
      } yield Ok(views.html.component.master.identification(identification = identification, accountKYC = accountKYC))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def latestBlockHeight(): Action[AnyContent] = withoutLoginActionAsync { implicit loginState =>
    implicit request =>
      val latestBlock = blockchainBlocks.Service.getLatestBlock
      val averageBlockTime = blockchainAverageBlockTimes.Service.get

      def getProposer(proposerAddress: String) = blockchainValidators.Service.tryGetProposerName(proposerAddress)

      (for {
        latestBlock <- latestBlock
        averageBlockTime <- averageBlockTime
        proposer <- getProposer(latestBlock.proposerAddress)
      } yield Ok(views.html.component.blockchain.latestBlockHeight(blockHeight = latestBlock.height, proposer = proposer, time = latestBlock.time, averageBlockTime = averageBlockTime, chainID = chainID))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def tokensStatistics(): Action[AnyContent] = withoutLoginActionAsync { implicit loginState =>
    implicit request =>
      val tokens = blockchainTokens.Service.getAll
      (for {
        tokens <- tokens
      } yield Ok(views.html.component.blockchain.tokensStatistics(tokens = tokens, stakingDenom = stakingDenom))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def votingPowers(): Action[AnyContent] = withoutLoginActionAsync { implicit loginState =>
    implicit request =>
      val allValidators = blockchainValidators.Service.getAll

      def getVotingPowerMap(validators: Seq[Validator]): ListMap[String, Double] = validators.map(validator => validator.description.moniker.getOrElse("") -> validator.tokens.toDouble)(collection.breakOut)

      (for {
        allValidators <- allValidators
      } yield Ok(views.html.component.blockchain.votingPowers(votingPowerMap = getVotingPowerMap(allValidators.filter(x => x.status == bondedStatus)), totalActiveValidators = allValidators.count(x => x.status == bondedStatus), totalValidators = allValidators.length))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def tokensPrices(): Action[AnyContent] = withoutLoginActionAsync { implicit loginState =>
    implicit request =>
      val allDenoms = blockchainTokens.Service.getAllDenoms

      def allTokenPrices(allDenoms: Seq[String]) = masterTransactionTokenPrices.Service.getLatest(n = 5, totalTokens = allDenoms.length)

      def getTokenPricesMap(allTokenPrices: Seq[TokenPrice], allDenoms: Seq[String]): Map[String, ListMap[String, Double]] = allDenoms.map(denom => denom -> ListMap(allTokenPrices.filter(_.denom == denom).map(tokenPrice => (tokenPrice.createdOn.getOrElse(throw new BaseException(constants.Response.TIME_NOT_FOUND)).toString, tokenPrice.price)): _*))(collection.breakOut)

      (for {
        allDenoms <- allDenoms
        allTokenPrices <- allTokenPrices(allDenoms)
      } yield Ok(views.html.component.blockchain.tokensPrices(getTokenPricesMap(allTokenPrices, allDenoms)))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def accountWallet(address: String): Action[AnyContent] = withoutLoginActionAsync { implicit loginState =>
    implicit request =>
      val operatorAddress = utilities.Bech32.convertAccountAddressToOperatorAddress(address)
      val isValidator = blockchainValidators.Service.exists(operatorAddress)
      val account = blockchainAccounts.Service.tryGet(address)
      val delegations = blockchainDelegations.Service.getAllForDelegator(address)
      val undelegations = blockchainUndelegations.Service.getAllByDelegator(address)
      val allDenoms = blockchainTokens.Service.getAllDenoms

      def getRewards(isValidator: Boolean): Future[(MicroNumber, MicroNumber)] = if (isValidator) {
        getValidatorSelfBondAndCommissionRewards.Service.get(operatorAddress).map(x => (x.result.self_bond_rewards.fold(MicroNumber.zero)(x => x.headOption.fold(MicroNumber.zero)(_.amount)), x.result.val_commission.fold(MicroNumber.zero)(x => x.headOption.fold(MicroNumber.zero)(_.amount))))
      } else getDelegatorRewards.Service.get(address).map(x => (x.result.total.fold(MicroNumber.zero)(_.headOption.fold(MicroNumber.zero)(_.amount)), MicroNumber.zero))

      def getValidatorsDelegated(operatorAddresses: Seq[String]): Future[Seq[Validator]] = blockchainValidators.Service.getByOperatorAddresses(operatorAddresses)

      def getDelegatedAmount(delegations: Seq[Delegation], validators: Seq[Validator]): MicroNumber = delegations.map(delegation => utilities.Delegations.getTokenAmountFromShares(validator = validators.find(_.operatorAddress == delegation.validatorAddress).getOrElse(throw new BaseException(constants.Response.VALIDATOR_NOT_FOUND)), shares = delegation.shares)).sum

      def getUndelegatingAmount(undelegations: Seq[Undelegation]): MicroNumber = undelegations.map(_.entries.map(_.balance).sum).sum

      (for {
        isValidator <- isValidator
        account <- account
        (delegationRewards, commissionRewards) <- getRewards(isValidator)
        delegations <- delegations
        undelegations <- undelegations
        validators <- getValidatorsDelegated(delegations.map(_.validatorAddress))
        allDenoms <- allDenoms
      } yield Ok(views.html.component.blockchain.accountWallet(address = address, accountBalances = account.coins, delegatedAmount = getDelegatedAmount(delegations, validators), undelegatingAmount = getUndelegatingAmount(undelegations), delegationRewards = delegationRewards, isValidator = isValidator, commissionRewards = commissionRewards, stakingDenom = stakingDenom, totalTokens = allDenoms.length))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def accountDelegations(address: String): Action[AnyContent] = withoutLoginActionAsync { implicit loginState =>
    implicit request =>
      val delegations = blockchainDelegations.Service.getAllForDelegator(address)
      val undelegations = blockchainUndelegations.Service.getAllByDelegator(address)
      val validators = blockchainValidators.Service.getAll

      def getDelegationsMap(delegations: Seq[Delegation], validators: Seq[Validator]) = ListMap(delegations.map(delegation => delegation.validatorAddress -> utilities.Delegations.getTokenAmountFromShares(validator = validators.find(_.operatorAddress == delegation.validatorAddress).getOrElse(throw new BaseException(constants.Response.VALIDATOR_NOT_FOUND)), shares = delegation.shares)): _*)

      def getUndelegationsMap(undelegations: Seq[Undelegation]) = ListMap(undelegations.map(undelegation => undelegation.validatorAddress -> undelegation.entries): _*)

      def getValidatorsMoniker(validators: Seq[Validator]) = Map(validators.map(validator => validator.operatorAddress -> validator.description.moniker.getOrElse(validator.operatorAddress)): _*)

      (for {
        delegations <- delegations
        undelegations <- undelegations
        validators <- validators
      } yield Ok(views.html.component.blockchain.accountDelegations(delegations = getDelegationsMap(delegations, validators), undelegations = getUndelegationsMap(undelegations), validatorsMoniker = getValidatorsMoniker(validators)))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def accountTransactions(address: String): Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
      Ok(views.html.component.blockchain.accountTransactions(address))
  }

  def accountTransactionsPerPage(address: String, page: Int): Action[AnyContent] = withoutLoginActionAsync { implicit loginState =>
    implicit request =>
      val transactions = blockchainTransactions.Service.getTransactionsPerPageByAddress(address, page)

      (for {
        transactions <- transactions
      } yield Ok(views.html.component.blockchain.accountTransactionsPerPage(transactions))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def blockList(): Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
      Ok(views.html.component.blockchain.blockList())
  }

  def blockListPage(pageNumber: Int = 1): Action[AnyContent] = withoutLoginActionAsync { implicit loginState =>
    implicit request =>

      val result = if (pageNumber <= 0) Future(BadRequest) else {
        val blocks = blockchainBlocks.Service.getBlocksPerPage(pageNumber)
        val validators = blockchainValidators.Service.getAll

        def getNumberOfTransactions(blockHeights: Seq[Int]) = blockchainTransactions.Service.getNumberOfTransactions(blockHeights)

        def getProposers(blocks: Seq[Block], validators: Seq[Validator]): Future[Map[Int, String]] = Future {
          blocks.map { block =>
            val validator = validators.find(_.hexAddress == block.proposerAddress).getOrElse(throw new BaseException(constants.Response.VALIDATOR_NOT_FOUND))
            block.height -> validator.description.moniker.getOrElse(validator.operatorAddress)
          }(collection.breakOut)
        }

        for {
          blocks <- blocks
          validators <- validators
          proposers <- getProposers(blocks, validators)
          numberOfTxs <- getNumberOfTransactions(blocks.map(_.height))
        } yield Ok(views.html.component.blockchain.blockListPage(blocks, numberOfTxs, proposers))
      }

      (for {
        result <- result
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def blockDetails(height: Int): Action[AnyContent] = withoutLoginActionAsync { implicit loginState =>
    implicit request =>
      val block = blockchainBlocks.Service.tryGet(height)
      val numTxs = blockchainTransactions.Service.getNumberOfTransactions(height)

      def getProposer(hexAddress: String) = blockchainValidators.Service.tryGetProposerName(hexAddress)

      (for {
        block <- block
        numTxs <- numTxs
        proposer <- getProposer(block.proposerAddress)
      } yield Ok(views.html.component.blockchain.blockDetails(block, proposer, numTxs))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def blockTransactions(height: Int): Action[AnyContent] = withoutLoginActionAsync { implicit loginState =>
    implicit request =>
      val transactions = blockchainTransactions.Service.getTransactions(height)

      (for {
        transactions <- transactions
      } yield Ok(views.html.component.blockchain.blockTransactions(height, transactions))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def transactionList(): Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
      Ok(views.html.component.blockchain.transactionList())
  }

  def transactionListPage(pageNumber: Int = 1): Action[AnyContent] = withoutLoginActionAsync { implicit loginState =>
    implicit request =>

      def getResult = if (pageNumber <= 0) Future(BadRequest) else {
        val transactions = blockchainTransactions.Service.getTransactionsPerPage(pageNumber)

        for {
          transactions <- transactions
        } yield Ok(views.html.component.blockchain.transactionListPage(transactions))
      }

      (for {
        result <- getResult
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def transactionDetails(txHash: String): Action[AnyContent] = withoutLoginActionAsync { implicit loginState =>
    implicit request =>
      val transaction = blockchainTransactions.Service.tryGet(txHash)

      (for {
        transaction <- transaction
      } yield Ok(views.html.component.blockchain.transactionDetails(transaction))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def transactionMessages(txHash: String): Action[AnyContent] = withoutLoginActionAsync { implicit loginState =>
    implicit request =>
      val messages = blockchainTransactions.Service.tryGetMessages(txHash)

      (for {
        messages <- messages
      } yield Ok(views.html.component.blockchain.transactionMessages(txHash, messages))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def validatorList(): Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
      Ok(views.html.component.blockchain.validatorList())
  }

  def activeValidatorList(): Action[AnyContent] = withoutLoginActionAsync { implicit loginState =>
    implicit request =>
      val validators = blockchainValidators.Service.getAllActiveValidatorList
      (for {
        validators <- validators
      } yield Ok(views.html.component.blockchain.activeValidatorList(validators, validators.map(_.tokens).sum))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def inactiveValidatorList(): Action[AnyContent] = withoutLoginActionAsync { implicit loginState =>
    implicit request =>
      val validators = blockchainValidators.Service.getAllInactiveValidatorList
      (for {
        validators <- validators
      } yield Ok(views.html.component.blockchain.inactiveValidatorList(validators))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def validatorDetails(address: String): Action[AnyContent] = withoutLoginActionAsync { implicit loginState =>
    implicit request =>
      val validator = blockchainValidators.Service.tryGet(address)
      val totalBondedAmount = blockchainTokens.Service.getTotalBondedAmount
      (for {
        validator <- validator
        totalBondedAmount <- totalBondedAmount
      } yield Ok(views.html.component.blockchain.validatorDetails(validator, utilities.Bech32.convertOperatorAddressToAccountAddress(validator.operatorAddress), (validator.tokens * 100 / totalBondedAmount).toRoundedOffString(), bondedStatus))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def validatorUptime(address: String, n: Int): Action[AnyContent] = withoutLoginActionAsync { implicit loginState =>
    implicit request =>
      val hexAddress = if (utilities.Validator.isHexAddress(address)) Future(address) else blockchainValidators.Service.tryGetHexAddress(address)
      val lastNBlocks = blockchainBlocks.Service.getLastNBlocks(n)

      def getUptime(lastNBlocks: Seq[Block], validatorHexAddress: String): Double = (lastNBlocks.count(block => block.validators.contains(validatorHexAddress)) * 100.0) / n

      def getUptimeMap(lastNBlocks: Seq[Block], validatorHexAddress: String): ListMap[Int, Boolean] = ListMap(lastNBlocks.map(block => block.height -> block.validators.contains(validatorHexAddress)): _*)

      (for {
        hexAddress <- hexAddress
        lastNBlocks <- lastNBlocks
      } yield Ok(views.html.component.blockchain.validatorUptime(uptime = getUptime(lastNBlocks, hexAddress), uptimeMap = getUptimeMap(lastNBlocks, hexAddress), hexAddress = hexAddress))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def validatorDelegations(address: String): Action[AnyContent] = withoutLoginActionAsync { implicit loginState =>
    implicit request =>
      val operatorAddress = if (utilities.Validator.isHexAddress(address)) blockchainValidators.Service.tryGetOperatorAddress(address) else Future(address)
      val validator = blockchainValidators.Service.tryGet(address)

      def getDelegations(operatorAddress: String) = blockchainDelegations.Service.getAllForValidator(operatorAddress)

      def getDelegationsMap(delegations: Seq[Delegation], validator: Validator) = Future {
        val selfDelegated = delegations.find(x => x.delegatorAddress == utilities.Bech32.convertOperatorAddressToAccountAddress(x.validatorAddress)).fold(BigDecimal(0.0))(_.shares)
        val othersDelegated = validator.delegatorShares - selfDelegated
        val delegationsMap = ListMap(constants.View.SELF_DELEGATED -> selfDelegated.toDouble, constants.View.OTHERS_DELEGATED -> othersDelegated.toDouble)
        (delegationsMap, (selfDelegated * 100.0 / validator.delegatorShares).toDouble, (othersDelegated * 100.0 / validator.delegatorShares).toDouble)
      }

      (for {
        operatorAddress <- operatorAddress
        validator <- validator
        delegations <- getDelegations(operatorAddress)
        (delegationsMap, selfDelegatedPercentage, othersDelegatedPercentage) <- getDelegationsMap(delegations, validator)
      } yield Ok(views.html.component.blockchain.validatorDelegations(delegationsMap = delegationsMap, selfDelegatedPercentage = selfDelegatedPercentage, othersDelegatedPercentage = othersDelegatedPercentage))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def validatorTransactions(address: String): Action[AnyContent] = withoutLoginActionAsync { implicit loginState =>
    implicit request =>
      val operatorAddress = if (utilities.Validator.isHexAddress(address)) blockchainValidators.Service.tryGetOperatorAddress(address) else Future(address)
      (for {
        operatorAddress <- operatorAddress
      } yield Ok(views.html.component.blockchain.validatorTransactions(operatorAddress))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def validatorTransactionsPerPage(address: String, page: Int): Action[AnyContent] = withoutLoginActionAsync { implicit loginState =>
    implicit request =>
      val transactions = blockchainTransactions.Service.getTransactionsPerPageByAddress(address, page)

      (for {
        transactions <- transactions
      } yield Ok(views.html.component.blockchain.validatorTransactionsPerPage(transactions))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def identitiesDefinition(): Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val identityIDs = blockchainIdentities.Service.getAllIDsByProvisioned(loginState.address)

      def getIdentitiesDefined(identityIDs: Seq[String]) = masterClassifications.Service.getIdentityDefinitionsByIdentityIDs(identityIDs)

      (for {
        identityIDs <- identityIDs
        classifications <- getIdentitiesDefined(identityIDs)
      } yield Ok(views.html.component.master.identitiesDefinition(classifications))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def identitiesProvisioned(): Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val identityIDs = blockchainIdentities.Service.getAllIDsByProvisioned(loginState.address)

      def getIdentitiesIssued(identityIDs: Seq[String]) = masterIdentities.Service.getAllByIDs(identityIDs)

      (for {
        identityIDs <- identityIDs
        identities <- getIdentitiesIssued(identityIDs)
      } yield Ok(views.html.component.master.identitiesProvisioned(identities))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def identitiesUnprovisioned(): Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val identityIDs = blockchainIdentities.Service.getAllIDsByUnprovisioned(loginState.address)

      def getIdentitiesIssued(identityIDs: Seq[String]) = masterIdentities.Service.getAllByIDs(identityIDs)

      (for {
        identityIDs <- identityIDs
        identities <- getIdentitiesIssued(identityIDs)
      } yield Ok(views.html.component.master.identitiesUnprovisioned(identities))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def assetsDefinition(): Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val identityIDs = blockchainIdentities.Service.getAllIDsByProvisioned(loginState.address)

      def getAssetsDefined(identityIDs: Seq[String]) = masterClassifications.Service.getAssetDefinitionsByIdentityIDs(identityIDs)

      (for {
        identityIDs <- identityIDs
        classifications <- getAssetsDefined(identityIDs)
      } yield Ok(views.html.component.master.assetsDefinition(classifications))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def assetsMinted(): Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val identityIDs = blockchainIdentities.Service.getAllIDsByProvisioned(loginState.address)

      def getAssetSplits(identityIDs: Seq[String]) = masterSplits.Service.getAllAssetsByOwnerIDs(identityIDs)

      def getAssets(assetIDs: Seq[String]) = masterAssets.Service.getAllByIDs(assetIDs)

      (for {
        identityIDs <- identityIDs
        splits <- getAssetSplits(identityIDs)
        assets <- getAssets(splits.map(_.ownableID))
      } yield Ok(views.html.component.master.assetsMinted(assets, splits))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def ordersDefinition(): Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val identityIDs = blockchainIdentities.Service.getAllIDsByProvisioned(loginState.address)

      def getOrdersDefined(identityIDs: Seq[String]) = masterClassifications.Service.getOrderDefinitionsByIdentityIDs(identityIDs)

      (for {
        identityIDs <- identityIDs
        classifications <- getOrdersDefined(identityIDs)
      } yield Ok(views.html.component.master.ordersDefinition(classifications))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def ordersMade(): Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val identityIDs = blockchainIdentities.Service.getAllIDsByProvisioned(loginState.address)

      def getOrdersMade(identityIDs: Seq[String]) = masterOrders.Service.getAllByMakerIDs(identityIDs)

      (for {
        identityIDs <- identityIDs
        orders <- getOrdersMade(identityIDs)
      } yield Ok(views.html.component.master.ordersMade(orders))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def ordersTake(): Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val identityIDs = blockchainIdentities.Service.getAllIDsByProvisioned(loginState.address)
      val publicTakeOrderIDs = blockchainOrders.Service.getAllPublicOrderIDs

      def getPublicOrders(publicOrderIDs: Seq[String]) = masterOrders.Service.getAllByIDs(publicOrderIDs)

      def getPrivateOrderIDs(identityIDs: Seq[String]) = masterProperties.Service.getAllPrivateOrderIDs(identityIDs)

      def getPrivateOrders(privateOrderIDs: Seq[String]) = masterOrders.Service.getAllByIDs(privateOrderIDs)

      (for {
        identityIDs <- identityIDs
        publicOrderIDs <- publicTakeOrderIDs
        privateOrderIDs <- getPrivateOrderIDs(identityIDs)
        publicOrders <- getPublicOrders(publicOrderIDs)
        privateOrders <- getPrivateOrders(privateOrderIDs)
      } yield Ok(views.html.component.master.ordersTake(publicOrders = publicOrders, privateOrders = privateOrders))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def accountSplits(): Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val identityIDs = blockchainIdentities.Service.getAllIDsByProvisioned(loginState.address)
      val allDenoms = blockchainTokens.Service.getAllDenoms

      def getBlockchainSplits(identityIDs: Seq[String]) = blockchainSplits.Service.getByOwnerIDs(identityIDs)

      def getAssets(splitIDs: Seq[String]) = masterAssets.Service.getAllByIDs(splitIDs)

      (for {
        identityIDs <- identityIDs
        splits <- getBlockchainSplits(identityIDs)
        allDenoms <- allDenoms
        masterAssets <- getAssets(splits.filterNot(x => allDenoms.contains(x.ownableID)).map(_.ownableID))
      } yield Ok(views.html.component.master.accountSplits(splits, allDenoms, masterAssets))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def entityProperties(entityID: String, entityType: String): Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val identityIDs = blockchainIdentities.Service.getAllIDsByProvisioned(loginState.address)
      val properties = masterProperties.Service.getAll(entityID = entityID, entityType = entityType)

      def verifyOwner(identityIDs: Seq[String]): Future[Boolean] = {
        entityType match {
          case constants.Blockchain.Entity.IDENTITY_DEFINITION | constants.Blockchain.Entity.ASSET_DEFINITION | constants.Blockchain.Entity.ORDER_DEFINITION =>
            val fromID = masterClassifications.Service.tryGetMaintainerID(id = entityID)
            for {
              fromID <- fromID
            } yield identityIDs.contains(fromID)
          case constants.Blockchain.Entity.IDENTITY => Future(identityIDs.contains(entityID))
          case constants.Blockchain.Entity.ASSET | constants.Blockchain.Entity.WRAPPED_COIN =>
            val ownerID = masterSplits.Service.tryGetOwnerID(ownableID = entityID)
            for {
              ownerID <- ownerID
            } yield identityIDs.contains(ownerID)
          case constants.Blockchain.Entity.ORDER =>
            val ownerID = masterOrders.Service.tryGetMakerID(entityID)
            for {
              ownerID <- ownerID
            } yield identityIDs.contains(ownerID)
          case _ => Future(false)
        }
      }

      (for {
        identityIDs <- identityIDs
        isOwner <- verifyOwner(identityIDs)
        properties <- properties
      } yield {
        if (isOwner) Ok(views.html.component.master.viewEntityProperties(properties))
        else throw new BaseException(constants.Response.UNAUTHORIZED)
      }).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def viewIdentityInfo(identityID: String) = withLoginAction.authenticated { implicit loginState =>
    implicit request =>

      val identity = blockchainIdentities.Service.get(identityID)
      (for (
        identity <- identity
      ) yield {
        identity match {
          case Some(identity) => Ok(views.html.component.blockchain.identity(identity))
          case None => throw new BaseException(constants.Response.IDENTITY_NOT_FOUND)
        }
      }).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }

  }

}