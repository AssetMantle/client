package controllers

import constants.AppConfig._
import controllers.actions._
import controllers.results.WithUsernameToken
import exceptions.BaseException
import models._
import models.blockchain._
import models.common.Serializable.Coin
import models.keyBase.ValidatorAccount
import play.api.cache.Cached
import play.api.i18n.I18nSupport
import play.api.mvc._
import play.api.{Configuration, Logger}
import queries.blockchain.{GetDelegatorRewards, GetValidatorCommission}
import queries.responses.common.EventWrapper
import utilities.MicroNumber

import javax.inject.{Inject, Singleton}
import scala.collection.immutable.ListMap
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ComponentViewController @Inject()(
                                         blockchainAccounts: blockchain.Accounts,
                                         blockchainBalances: blockchain.Balances,
                                         blockchainDelegations: blockchain.Delegations,
                                         blockchainIdentities: blockchain.Identities,
                                         blockchainOrders: blockchain.Orders,
                                         blockchainSplits: blockchain.Splits,
                                         blockchainUndelegations: blockchain.Undelegations,
                                         blockchainRedelegations: blockchain.Redelegations,
                                         blockchainBlocks: blockchain.Blocks,
                                         blockchainWithdrawAddresses: blockchain.WithdrawAddresses,
                                         blockchainTransactions: blockchain.Transactions,
                                         blockchainTransactionsIdentityDefines: blockchainTransaction.IdentityDefines,
                                         blockchainTransactionsIdentityNubs: blockchainTransaction.IdentityNubs,
                                         blockchainTransactionsIdentityIssues: blockchainTransaction.IdentityIssues,
                                         blockchainTransactionsIdentityProvisions: blockchainTransaction.IdentityProvisions,
                                         blockchainTransactionsIdentityUnprovisions: blockchainTransaction.IdentityUnprovisions,
                                         blockchainTransactionsAssetDefines: blockchainTransaction.AssetDefines,
                                         blockchainTransactionsAssetMints: blockchainTransaction.AssetMints,
                                         blockchainTransactionsAssetMutates: blockchainTransaction.AssetMutates,
                                         blockchainTransactionsAssetBurns: blockchainTransaction.AssetBurns,
                                         blockchainTransactionsOrderDefines: blockchainTransaction.OrderDefines,
                                         blockchainTransactionsOrderMakes: blockchainTransaction.OrderMakes,
                                         blockchainTransactionsOrderTakes: blockchainTransaction.OrderTakes,
                                         blockchainTransactionsOrderCancels: blockchainTransaction.OrderCancels,
                                         blockchainTokens: blockchain.Tokens,
                                         blockchainProposals: blockchain.Proposals,
                                         blockchainProposalDeposits: blockchain.ProposalDeposits,
                                         blockchainProposalVotes: blockchain.ProposalVotes,
                                         blockchainValidators: blockchain.Validators,
                                         cached: Cached,
                                         getDelegatorRewards: GetDelegatorRewards,
                                         getValidatorCommission: GetValidatorCommission,
                                         masterTransactionTokenPrices: masterTransaction.TokenPrices,
                                         keyBaseValidatorAccounts: keyBase.ValidatorAccounts,
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
                                         withLoginActionAsync: WithLoginActionAsync,
                                         withUsernameToken: WithUsernameToken,
                                       )(implicit configuration: Configuration, executionContext: ExecutionContext) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_COMPONENT_VIEW

  private val priceChartDataPoints = configuration.get[Int]("blockchain.token.priceChartDataPoints")

  private val transactionsStatisticsBinWidth = configuration.get[Int]("statistics.transactions.binWidth")

  def commonHome: Action[AnyContent] = withLoginActionAsync { implicit loginState =>
    implicit request =>
      Future(Ok(views.html.component.master.commonHome()))
  }

  def recentActivities: EssentialAction = cached.apply(req => req.path, constants.AppConfig.CacheDuration) {
    withoutLoginAction { implicit loginState =>
      implicit request =>
        Ok(views.html.component.master.notification.recentActivities())
    }
  }

  def profilePicture(): Action[AnyContent] = withLoginActionAsync { implicit loginState =>
    implicit request =>
      val profilePicture = masterAccountFiles.Service.getProfilePicture(loginState.username)
      (for {
        profilePicture <- profilePicture
      } yield Ok(views.html.assetMantle.profilePicture(profilePicture))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def wallet(address: String): EssentialAction = cached.apply(req => req.path, constants.AppConfig.CacheDuration) {
    withoutLoginActionAsync { implicit loginState =>
      implicit request =>
        loginState match {
          case Some(loginState) => {
            implicit val loginStateImplicit: LoginState = loginState
            withUsernameToken.Ok(views.html.assetMantle.account())
          }
          case None => Future(Ok(views.html.component.blockchain.account.wallet(address)))
        }
    }
  }

  def block(height: Int): EssentialAction = cached.apply(req => req.path, constants.AppConfig.CacheDuration) {
    withoutLoginActionAsync { implicit loginState =>
      implicit request =>
        loginState match {
          case Some(loginState) => {
            implicit val loginStateImplicit: LoginState = loginState
            withUsernameToken.Ok(views.html.component.blockchain.block.block(height))
          }
          case None => Future(Ok(views.html.component.blockchain.block.block(height)))
        }
    }
  }

  def transaction(txHash: String): EssentialAction = cached.apply(req => req.path, constants.AppConfig.CacheDuration) {
    withoutLoginActionAsync { implicit loginState =>
      implicit request =>
        loginState match {
          case Some(loginState) => {
            implicit val loginStateImplicit: LoginState = loginState
            withUsernameToken.Ok(views.html.component.blockchain.transaction.transaction(txHash))
          }
          case None => Future(Ok(views.html.component.blockchain.transaction.transaction(txHash)))
        }
    }
  }

  def validator(address: String): EssentialAction = cached.apply(req => req.path, constants.AppConfig.CacheDuration) {
    withoutLoginActionAsync { implicit loginState =>
      implicit request =>
        loginState match {
          case Some(loginState) => {
            implicit val loginStateImplicit: LoginState = loginState
            withUsernameToken.Ok(views.html.component.blockchain.validator.validator(address))
          }
          case None => Future(Ok(views.html.component.blockchain.validator.validator(address)))
        }
    }
  }

  def proposal(id: Int): EssentialAction = cached.apply(req => req.path, constants.AppConfig.CacheDuration) {
    withoutLoginActionAsync { implicit loginState =>
      implicit request =>
        loginState match {
          case Some(loginState) => {
            implicit val loginStateImplicit: LoginState = loginState
            withUsernameToken.Ok(views.html.component.blockchain.proposal.proposal(id))
          }
          case None => Future(Ok(views.html.component.blockchain.proposal.proposal(id)))
        }
    }
  }

  def dashboard: EssentialAction = cached.apply(req => req.path, constants.AppConfig.CacheDuration) {
    withoutLoginActionAsync { implicit loginState =>
      implicit request =>
        loginState match {
          case Some(loginState) => {
            implicit val loginStateImplicit: LoginState = loginState
            withUsernameToken.Ok(views.html.component.blockchain.dashboard())
          }
          case None => Future(Ok(views.html.component.blockchain.dashboard()))
        }
    }
  }

  def latestBlockHeight(): EssentialAction = cached.apply(req => req.path, constants.AppConfig.CacheDuration) {
    withoutLoginActionAsync { implicit loginState =>
      implicit request =>
        val latestBlock = blockchainBlocks.Service.getLatestBlock

        def getAverageBlockTime(latestBlock: Block) = blockchainBlocks.Utility.getAverageBlockTime(fromBlock = Option(latestBlock.height))

        def getProposer(proposerAddress: String) = blockchainValidators.Service.tryGetProposerName(proposerAddress)

        (for {
          latestBlock <- latestBlock
          averageBlockTime <- getAverageBlockTime(latestBlock)
          proposer <- getProposer(latestBlock.proposerAddress)
        } yield Ok(views.html.component.blockchain.latestBlockHeight(blockHeight = latestBlock.height, proposer = proposer, time = latestBlock.time, averageBlockTime = averageBlockTime, chainID = constants.Blockchain.ChainID))
          ).recover {
          case baseException: BaseException => InternalServerError(baseException.failure.message)
        }
    }
  }

  def tokensStatistics(): EssentialAction = cached.apply(req => req.path, constants.AppConfig.CacheDuration) {
    withoutLoginActionAsync { implicit loginState =>
      implicit request =>
        val token = blockchainTokens.Service.getStakingToken
        (for {
          token <- token
        } yield Ok(views.html.component.blockchain.tokensStatistics(token = token, tokenTickers = tokenTickers))
          ).recover {
          case baseException: BaseException => InternalServerError(baseException.failure.message)
        }
    }
  }

  def votingPowers(): EssentialAction = cached.apply(req => req.path, constants.AppConfig.CacheDuration) {
    withoutLoginActionAsync { implicit loginState =>
      implicit request =>
        val allSortedValidators = blockchainValidators.Service.getAll.map(_.sortBy(_.tokens).reverse)

        def getVotingPowerMaps(sortedBondedValidators: Seq[Validator]): Seq[(String, Double)] = {
          val totalTokens = sortedBondedValidators.map(_.tokens).sum
          var countedToken = MicroNumber.zero
          val bottomValidators = sortedBondedValidators.reverse.takeWhile(validator => {
            countedToken = countedToken + validator.tokens
            countedToken <= totalTokens / 3
          })
          val bottomValidatorAddresses = bottomValidators.map(_.operatorAddress)
          sortedBondedValidators.filterNot(x => bottomValidatorAddresses.contains(x.operatorAddress))
            .map(validator => validator.description.moniker -> validator.tokens.toDouble) :+ (constants.View.OTHERS -> bottomValidators.map(_.tokens).sum.toDouble)
        }

        (for {
          allSortedValidators <- allSortedValidators
        } yield Ok(views.html.component.blockchain.votingPowers(sortedVotingPowerMap = ListMap(getVotingPowerMaps(allSortedValidators.filter(x => x.status == constants.Blockchain.ValidatorStatus.BONED)): _*), totalActiveValidators = allSortedValidators.count(x => x.status == constants.Blockchain.ValidatorStatus.BONED), totalValidators = allSortedValidators.length))
          ).recover {
          case baseException: BaseException => InternalServerError(baseException.failure.message)
        }
    }
  }

  def tokensPrices(): EssentialAction = cached.apply(req => req.path, constants.AppConfig.CacheDuration) {
    withoutLoginActionAsync { implicit loginState =>
      implicit request =>

        def getTokenPrices = masterTransactionTokenPrices.Service.getLatestByToken(denom = constants.Blockchain.StakingDenom, n = priceChartDataPoints)

        (for {
          tokenPrices <- getTokenPrices
        } yield Ok(views.html.component.blockchain.tokensPrices(tokenPrices, constants.Blockchain.StakingDenom, tokenTickers))
          ).recover {
          case baseException: BaseException => InternalServerError(baseException.failure.message)
        }
    }
  }

  def transactionStatistics(): EssentialAction = cached.apply(req => req.path, constants.AppConfig.CacheDuration) {
    withoutLoginActionAsync { implicit loginState =>
      implicit request =>
        val totalAccounts = blockchainBalances.Service.getTotalAccounts
        val totalTxs = blockchainTransactions.Service.getTotalTransactions
        val latestHeight = blockchainBlocks.Service.getLatestBlockHeight

        def getTxData(latestHeight: Int) = blockchainTransactions.Service.getTransactionStatisticsData(latestHeight)

        (for {
          totalAccounts <- totalAccounts
          totalTxs <- totalTxs
          latestHeight <- latestHeight
          txData <- getTxData(latestHeight)
        } yield Ok(views.html.component.blockchain.transactionStatistics(totalAccounts = totalAccounts, totalTxs = totalTxs, txData = txData, binWidth = transactionsStatisticsBinWidth))
          ).recover {
          case baseException: BaseException => InternalServerError(baseException.failure.message)
        }
    }
  }

  def accountWallet(address: String): EssentialAction = cached.apply(req => req.path, constants.AppConfig.CacheDuration) {
    withoutLoginActionAsync { implicit loginState =>
      implicit request =>
        val operatorAddress = Future(utilities.Bech32.convertAccountAddressToOperatorAddress(address))
        val balances = blockchainBalances.Service.get(address)
        val delegations = blockchainDelegations.Service.getAllForDelegator(address)
        val undelegations = blockchainUndelegations.Service.getAllByDelegator(address)
        val allDenoms = blockchainTokens.Service.getAllDenoms
        val delegationRewards = getDelegatorRewards.Service.get(address)
        val withdrawAddress = blockchainWithdrawAddresses.Service.get(address)

        def isValidator(operatorAddress: String) = blockchainValidators.Service.exists(operatorAddress)

        def getValidatorCommissionRewards(operatorAddress: String, isValidator: Boolean): Future[Coin] = if (isValidator) {
          getValidatorCommission.Service.get(operatorAddress).map(x => x.commission.commission.headOption.fold(MicroNumber.zero)(_.amount)).map(x => Coin(constants.Blockchain.StakingDenom, x))
        } else Future(Coin(constants.Blockchain.StakingDenom, MicroNumber.zero))

        def getValidatorsDelegated(operatorAddresses: Seq[String]): Future[Seq[Validator]] = blockchainValidators.Service.getByOperatorAddresses(operatorAddresses)

        def getDelegatedAmount(delegations: Seq[Delegation], validators: Seq[Validator]): Coin = Coin(constants.Blockchain.StakingDenom, delegations.map(x => validators.find(_.operatorAddress == x.validatorAddress).fold(throw new BaseException(constants.Response.VALIDATOR_NOT_FOUND))(_.getTokensFromShares(x.shares))).sum)

        def getUndelegatingAmount(undelegations: Seq[Undelegation]): Coin = Coin(constants.Blockchain.StakingDenom, undelegations.map(_.entries.map(_.balance).sum).sum)

        def getTokenPrice = masterTransactionTokenPrices.Service.getLatestByTokenPrice(denom = constants.Blockchain.StakingDenom)

        (for {
          operatorAddress <- operatorAddress
          isValidator <- isValidator(operatorAddress)
          balances <- balances
          delegationRewards <- delegationRewards
          withdrawAddress <- withdrawAddress
          commissionRewards <- getValidatorCommissionRewards(operatorAddress, isValidator)
          delegations <- delegations
          undelegations <- undelegations
          validators <- getValidatorsDelegated((delegations.map(_.validatorAddress) ++ delegationRewards.rewards.map(_.validator_address)).distinct)
          allDenoms <- allDenoms
          tokenPrice <- getTokenPrice

        } yield Ok(views.html.component.blockchain.account.accountWallet(
          address = address,
          accountBalances = balances.fold[Seq[Coin]](Seq())(_.coins),
          delegated = getDelegatedAmount(delegations, validators),
          undelegating = getUndelegatingAmount(undelegations),
          delegationTotalRewards = delegationRewards.total.headOption.fold(Coin(constants.Blockchain.StakingDenom, MicroNumber.zero))(_.toCoin),
          isValidator = isValidator,
          commissionRewards = commissionRewards,
          stakingDenom = constants.Blockchain.StakingDenom,
          totalTokens = allDenoms.length,
          validatorRewards = ListMap(delegationRewards.rewards.map(reward => reward.validator_address -> reward.reward.headOption.fold(Coin(constants.Blockchain.StakingDenom, MicroNumber.zero))(_.toCoin)): _*),
          validatorsMap = validators.map(x => x.operatorAddress -> x.description.moniker).toMap,
          withdrawAddress = withdrawAddress,
          tokenPrice = tokenPrice
        ))
          ).recover {
          case baseException: BaseException => InternalServerError(baseException.failure.message)
        }
    }
  }

  def accountDelegations(address: String): EssentialAction = cached.apply(req => req.path, constants.AppConfig.CacheDuration) {
    withoutLoginActionAsync { implicit loginState =>
      implicit request =>
        val delegations = blockchainDelegations.Service.getAllForDelegator(address)
        val undelegations = blockchainUndelegations.Service.getAllByDelegator(address)
        val redelegations = blockchainRedelegations.Service.getAllByDelegator(address)
        val validators = blockchainValidators.Service.getAll

        def getDelegationsMap(delegations: Seq[Delegation], validators: Seq[Validator]) = ListMap(delegations.map(delegation => delegation.validatorAddress -> validators.find(_.operatorAddress == delegation.validatorAddress).getOrElse(throw new BaseException(constants.Response.VALIDATOR_NOT_FOUND)).getTokensFromShares(delegation.shares)): _*)

        def getUndelegationsMap(undelegations: Seq[Undelegation]) = ListMap(undelegations.map(undelegation => undelegation.validatorAddress -> undelegation.entries): _*)

        def getValidatorsMoniker(validators: Seq[Validator]) = Map(validators.map(validator => validator.operatorAddress -> validator.description.moniker): _*)

        def getRedelegationsMap(redelegations: Seq[Redelegation]) = ListMap(redelegations.map(redelegation => (redelegation.validatorSourceAddress, redelegation.validatorDestinationAddress) -> redelegation.entries): _*)

        (for {
          delegations <- delegations
          undelegations <- undelegations
          validators <- validators
          redelegations <- redelegations
        } yield Ok(views.html.component.blockchain.account.accountDelegations(delegations = getDelegationsMap(delegations, validators), undelegations = getUndelegationsMap(undelegations), validatorsMoniker = getValidatorsMoniker(validators), redelegations = getRedelegationsMap(redelegations)))
          ).recover {
          case baseException: BaseException => InternalServerError(baseException.failure.message)
        }
    }
  }

  def accountTransactions(address: String): EssentialAction = cached.apply(req => req.path, constants.AppConfig.CacheDuration) {
    withoutLoginAction { implicit loginState =>
      implicit request =>
        Ok(views.html.component.blockchain.account.accountTransactions(address))
    }
  }

  def accountTransactionsPerPage(address: String, page: Int): Action[AnyContent] = withoutLoginActionAsync { implicit loginState =>
    implicit request =>
      val transactions = blockchainTransactions.Service.getTransactionsPerPageByAddress(address, page)
      (for {
        transactions <- transactions
      } yield Ok(views.html.component.blockchain.account.accountTransactionsPerPage(transactions))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def blockList(): EssentialAction = cached.apply(req => req.path, constants.AppConfig.CacheDuration) {
    withoutLoginAction { implicit loginState =>
      implicit request =>
        Ok(views.html.component.blockchain.block.blockList())
    }
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
            block.height -> validator.description.moniker
          }(collection.breakOut)
        }

        for {
          blocks <- blocks
          validators <- validators
          proposers <- getProposers(blocks, validators)
          numberOfTxs <- getNumberOfTransactions(blocks.map(_.height))
        } yield Ok(views.html.component.blockchain.block.blockListPage(blocks, numberOfTxs, proposers))
      }

      (for {
        result <- result
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def blockDetails(height: Int): EssentialAction = cached.apply(req => req.path, constants.AppConfig.CacheDuration) {
    withoutLoginActionAsync { implicit loginState =>
      implicit request =>
        val block = blockchainBlocks.Service.tryGet(height)
        val numTxs = blockchainTransactions.Service.getNumberOfTransactions(height)

        def getProposer(hexAddress: String) = blockchainValidators.Service.tryGetProposerName(hexAddress)

        (for {
          block <- block
          numTxs <- numTxs
          proposer <- getProposer(block.proposerAddress)
        } yield Ok(views.html.component.blockchain.block.blockDetails(block, proposer, numTxs))
          ).recover {
          case baseException: BaseException => InternalServerError(baseException.failure.message)
        }
    }
  }

  def blockTransactions(height: Int): EssentialAction = cached.apply(req => req.path, constants.AppConfig.CacheDuration) {
    withoutLoginActionAsync { implicit loginState =>
      implicit request =>
        val transactions = blockchainTransactions.Service.getTransactions(height)

        (for {
          transactions <- transactions
        } yield Ok(views.html.component.blockchain.block.blockTransactions(height, transactions))
          ).recover {
          case baseException: BaseException => InternalServerError(baseException.failure.message)
        }
    }
  }

  def transactionList(): EssentialAction = cached.apply(req => req.path, constants.AppConfig.CacheDuration) {
    withoutLoginAction { implicit loginState =>
      implicit request =>
        Ok(views.html.component.blockchain.transaction.transactionList())
    }
  }

  def transactionListPage(pageNumber: Int = 1): Action[AnyContent] = withoutLoginActionAsync { implicit loginState =>
    implicit request =>

      (if (pageNumber <= 0) Future(BadRequest)
      else {
        val transactions = blockchainTransactions.Service.getTransactionsPerPage(pageNumber)
        for {
          transactions <- transactions
        } yield Ok(views.html.component.blockchain.transaction.transactionListPage(transactions))
      }).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def transactionDetails(txHash: String): EssentialAction = cached.apply(req => req.path, constants.AppConfig.CacheDuration) {
    withoutLoginActionAsync { implicit loginState =>
      implicit request =>
        val transaction = blockchainTransactions.Service.tryGet(txHash)
        (for {
          transaction <- transaction
        }
        yield Ok(views.html.component.blockchain.transaction.transactionDetails(transaction))
          ).recover {
          case baseException: BaseException => InternalServerError(baseException.failure.message)
        }
    }
  }

  def withdrawRewardAmount(txHash: String, msgIndex: Int): EssentialAction = cached.apply(req => req.path, constants.AppConfig.CacheDuration) {
    withoutLoginActionAsync { implicit loginState =>
      implicit request =>
        val transaction = blockchainTransactions.Service.tryGet(txHash)
        (for {
          transaction <- transaction
        } yield {
          if (transaction.status) {
            val coinArray = utilities.JSON.convertJsonStringToObject[Seq[EventWrapper]](transaction.rawLog)
              .find(_.msg_index.getOrElse(0) == msgIndex)
              .fold(MicroNumber.zero + constants.Blockchain.StakingDenom)(_.events.find(_.`type` == constants.Blockchain.Event.WithdrawRewards).fold(MicroNumber.zero + constants.Blockchain.StakingDenom)(_.attributes.find(_.key == constants.Blockchain.Event.Attribute.Amount).fold(MicroNumber.zero + constants.Blockchain.StakingDenom)(_.value.getOrElse(MicroNumber.zero + constants.Blockchain.StakingDenom))))
              .split(constants.RegularExpression.NUMERIC_AND_STRING_SEPARATOR).filter(_.nonEmpty).toList
            Ok(Coin(coinArray.tail.head, coinArray.head.toDouble / 1000000).getAmountWithNormalizedDenom())
          } else {
            Ok(Coin(constants.Blockchain.StakingDenom, MicroNumber.zero).getAmountWithNormalizedDenom())
          }
        }
          ).recover {
          case baseException: BaseException => InternalServerError(baseException.failure.message)
        }
    }
  }

  def transactionMessages(txHash: String): EssentialAction = cached.apply(req => req.path, constants.AppConfig.CacheDuration) {
    withoutLoginActionAsync { implicit loginState =>
      implicit request =>
        val messages = blockchainTransactions.Service.tryGetMessages(txHash)

        (for {
          messages <- messages
        } yield Ok(views.html.component.blockchain.transaction.transactionMessages(txHash, messages))
          ).recover {
          case baseException: BaseException => InternalServerError(baseException.failure.message)
        }
    }
  }

  def proposalList(): EssentialAction = cached.apply(req => req.path, constants.AppConfig.CacheDuration) {
    withoutLoginActionAsync { implicit loginState =>
      implicit request =>
        val proposals = blockchainProposals.Service.get()
        (for {
          proposals <- proposals
        } yield Ok(views.html.component.blockchain.proposal.proposalList(proposals))
          ).recover {
          case baseException: BaseException => InternalServerError(baseException.failure.message)
        }
    }
  }

  def proposalDetails(id: Int): EssentialAction = cached.apply(req => req.path, constants.AppConfig.CacheDuration) {
    withoutLoginActionAsync { implicit loginState =>
      implicit request =>
        val proposal = blockchainProposals.Service.tryGet(id)
        (for {
          proposal <- proposal
        } yield Ok(views.html.component.blockchain.proposal.proposalDetails(proposal))
          ).recover {
          case baseException: BaseException => InternalServerError(baseException.failure.message)
        }
    }
  }

  def proposalDeposits(id: Int): EssentialAction = cached.apply(req => req.path, constants.AppConfig.CacheDuration) {
    withoutLoginActionAsync { implicit loginState =>
      implicit request =>
        val proposalDeposits = blockchainProposalDeposits.Service.getByProposalID(id)
        (for {
          proposalDeposits <- proposalDeposits
        } yield Ok(views.html.component.blockchain.proposal.proposalDeposits(proposalDeposits))
          ).recover {
          case baseException: BaseException => InternalServerError(baseException.failure.message)
        }
    }
  }

  def proposalVotes(id: Int): EssentialAction = cached.apply(req => req.path, constants.AppConfig.CacheDuration) {
    withoutLoginActionAsync { implicit loginState =>
      implicit request =>
        val proposalVotes = blockchainProposalVotes.Service.getAllByID(id)
        (for {
          proposalVotes <- proposalVotes
        } yield Ok(views.html.component.blockchain.proposal.proposalVotes(proposalVotes))
          ).recover {
          case baseException: BaseException => InternalServerError(baseException.failure.message)
        }
    }
  }

  def validatorList(): EssentialAction = cached.apply(req => req.path, constants.AppConfig.CacheDuration) {
    withoutLoginAction { implicit loginState =>
      implicit request =>
        Ok(views.html.component.blockchain.validator.validatorList())
    }
  }

  def activeValidatorList(): EssentialAction = cached.apply(req => req.path, constants.AppConfig.CacheDuration) {
    withoutLoginActionAsync { implicit loginState =>
      implicit request =>
        val validators = blockchainValidators.Service.getAllActiveValidatorList

        def keyBaseValidators(addresses: Seq[String]): Future[Seq[ValidatorAccount]] = keyBaseValidatorAccounts.Service.get(addresses)

        (for {
          validators <- validators
          keyBaseValidators <- keyBaseValidators(validators.map(_.operatorAddress))
        } yield Ok(views.html.component.blockchain.validator.activeValidatorList(validators.sortBy(_.tokens).reverse, validators.map(_.tokens).sum, keyBaseValidators))
          ).recover {
          case baseException: BaseException => InternalServerError(baseException.failure.message)
        }
    }
  }

  def inactiveValidatorList(): EssentialAction = cached.apply(req => req.path, constants.AppConfig.CacheDuration) {
    withoutLoginActionAsync { implicit loginState =>
      implicit request =>
        val validators = blockchainValidators.Service.getAllInactiveValidatorList

        def keyBaseValidators(addresses: Seq[String]): Future[Seq[ValidatorAccount]] = keyBaseValidatorAccounts.Service.get(addresses)

        (for {
          validators <- validators
          keyBaseValidators <- keyBaseValidators(validators.map(_.operatorAddress))
        } yield Ok(views.html.component.blockchain.validator.inactiveValidatorList(validators, keyBaseValidators))
          ).recover {
          case baseException: BaseException => InternalServerError(baseException.failure.message)
        }
    }
  }

  def validatorDetails(address: String): EssentialAction = cached.apply(req => req.path, constants.AppConfig.CacheDuration) {
    withoutLoginActionAsync { implicit loginState =>
      implicit request =>
        val validator = blockchainValidators.Service.tryGet(address)
        val totalBondedAmount = blockchainTokens.Service.getTotalBondedAmount

        def keyBaseValidator(address: String): Future[Option[ValidatorAccount]] = keyBaseValidatorAccounts.Service.get(address)

        (for {
          validator <- validator
          totalBondedAmount <- totalBondedAmount
          keyBaseValidator <- keyBaseValidator(validator.operatorAddress)
        } yield Ok(views.html.component.blockchain.validator.validatorDetails(validator, utilities.Bech32.convertOperatorAddressToAccountAddress(validator.operatorAddress), (validator.tokens * 100 / totalBondedAmount).toRoundedOffString(), constants.Blockchain.ValidatorStatus.BONED, keyBaseValidator))
          ).recover {
          case baseException: BaseException => InternalServerError(baseException.failure.message)
        }
    }
  }

  def validatorUptime(address: String, n: Int): EssentialAction = cached.apply(req => req.path, constants.AppConfig.CacheDuration) {
    withoutLoginActionAsync { implicit loginState =>
      implicit request =>
        val hexAddress = if (utilities.Validator.isHexAddress(address)) Future(address) else blockchainValidators.Service.tryGetHexAddress(address)
        val lastNBlocks = blockchainBlocks.Service.getLastNBlocks(n)

        def getUptime(lastNBlocks: Seq[Block], validatorHexAddress: String): Double = (lastNBlocks.count(block => block.validators.contains(validatorHexAddress)) * 100.0) / n

        def getUptimeMap(lastNBlocks: Seq[Block], validatorHexAddress: String): ListMap[Int, Boolean] = ListMap(lastNBlocks.map(block => block.height -> block.validators.contains(validatorHexAddress)): _*)

        (for {
          hexAddress <- hexAddress
          lastNBlocks <- lastNBlocks
        } yield Ok(views.html.component.blockchain.validator.validatorUptime(uptime = getUptime(lastNBlocks, hexAddress), uptimeMap = getUptimeMap(lastNBlocks, hexAddress), hexAddress = hexAddress))
          ).recover {
          case baseException: BaseException => InternalServerError(baseException.failure.message)
        }
    }
  }

  def validatorDelegations(address: String): EssentialAction = cached.apply(req => req.path, constants.AppConfig.CacheDuration) {
    withoutLoginActionAsync { implicit loginState =>
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
        } yield Ok(views.html.component.blockchain.validator.validatorDelegations(delegationsMap = delegationsMap, selfDelegatedPercentage = selfDelegatedPercentage, othersDelegatedPercentage = othersDelegatedPercentage))
          ).recover {
          case baseException: BaseException => InternalServerError(baseException.failure.message)
        }
    }
  }

  def validatorTransactions(address: String): EssentialAction = cached.apply(req => req.path, constants.AppConfig.CacheDuration) {
    withoutLoginActionAsync { implicit loginState =>
      implicit request =>
        val operatorAddress = if (utilities.Validator.isHexAddress(address)) blockchainValidators.Service.tryGetOperatorAddress(address) else Future(address)
        (for {
          operatorAddress <- operatorAddress
        } yield Ok(views.html.component.blockchain.validator.validatorTransactions(operatorAddress))
          ).recover {
          case baseException: BaseException => InternalServerError(baseException.failure.message)
        }
    }
  }

  def validatorTransactionsPerPage(address: String, page: Int): Action[AnyContent] = withoutLoginActionAsync { implicit loginState =>
    implicit request =>
      val transactions = blockchainTransactions.Service.getTransactionsPerPageByAddress(address, page)

      (for {
        transactions <- transactions
      } yield Ok(views.html.component.blockchain.validator.validatorTransactionsPerPage(transactions))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def identitiesDefinition(): Action[AnyContent] = withLoginActionAsync { implicit loginState =>
    implicit request =>
      val identityIDs = blockchainIdentities.Service.getAllIDsByProvisioned(loginState.address)

      def getIdentitiesDefined(identityIDs: Seq[String]) = masterClassifications.Service.getIdentityDefinitionsByIdentityIDs(identityIDs)

      (for {
        identityIDs <- identityIDs
        classifications <- getIdentitiesDefined(identityIDs)
      } yield Ok(views.html.component.master.identity.identitiesDefinition(classifications))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def identitiesProvisioned(): Action[AnyContent] = withLoginActionAsync { implicit loginState =>
    implicit request =>
      val identityIDs = blockchainIdentities.Service.getAllIDsByProvisioned(loginState.address)

      def getIdentitiesIssued(identityIDs: Seq[String]) = masterIdentities.Service.getAllByIDs(identityIDs)

      (for {
        identityIDs <- identityIDs
        identities <- getIdentitiesIssued(identityIDs)
      } yield Ok(views.html.component.master.identity.identitiesProvisioned(identities))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def identitiesUnprovisioned(): Action[AnyContent] = withLoginActionAsync { implicit loginState =>
    implicit request =>
      val identityIDs = blockchainIdentities.Service.getAllIDsByUnprovisioned(loginState.address)

      def getIdentitiesIssued(identityIDs: Seq[String]) = masterIdentities.Service.getAllByIDs(identityIDs)

      (for {
        identityIDs <- identityIDs
        identities <- getIdentitiesIssued(identityIDs)
      } yield Ok(views.html.component.master.identity.identitiesUnprovisioned(identities))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def assetsDefinition(): Action[AnyContent] = withLoginActionAsync { implicit loginState =>
    implicit request =>
      val identityIDs = blockchainIdentities.Service.getAllIDsByProvisioned(loginState.address)

      def getAssetsDefined(identityIDs: Seq[String]) = masterClassifications.Service.getAssetDefinitionsByIdentityIDs(identityIDs)

      (for {
        identityIDs <- identityIDs
        classifications <- getAssetsDefined(identityIDs)
      } yield Ok(views.html.component.master.asset.assetsDefinition(classifications))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def assetsMinted(): Action[AnyContent] = withLoginActionAsync { implicit loginState =>
    implicit request =>
      val identityIDs = blockchainIdentities.Service.getAllIDsByProvisioned(loginState.address)

      def getAssetSplits(identityIDs: Seq[String]) = masterSplits.Service.getAllAssetsByOwnerIDs(identityIDs)

      def getAssets(assetIDs: Seq[String]) = masterAssets.Service.getAllByIDs(assetIDs)

      (for {
        identityIDs <- identityIDs
        splits <- getAssetSplits(identityIDs)
        assets <- getAssets(splits.map(_.ownableID))
      } yield Ok(views.html.component.master.asset.assetsMinted(assets, splits))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def ordersDefinition(): Action[AnyContent] = withLoginActionAsync { implicit loginState =>
    implicit request =>
      val identityIDs = blockchainIdentities.Service.getAllIDsByProvisioned(loginState.address)

      def getOrdersDefined(identityIDs: Seq[String]) = masterClassifications.Service.getOrderDefinitionsByIdentityIDs(identityIDs)

      (for {
        identityIDs <- identityIDs
        classifications <- getOrdersDefined(identityIDs)
      } yield Ok(views.html.component.master.order.ordersDefinition(classifications))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def ordersMade(): Action[AnyContent] = withLoginActionAsync { implicit loginState =>
    implicit request =>
      val identityIDs = blockchainIdentities.Service.getAllIDsByProvisioned(loginState.address)

      def getOrdersMade(identityIDs: Seq[String]) = masterOrders.Service.getAllByMakerIDs(identityIDs)

      (for {
        identityIDs <- identityIDs
        orders <- getOrdersMade(identityIDs)
      } yield Ok(views.html.component.master.order.ordersMade(orders))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def ordersTake(): Action[AnyContent] = withLoginActionAsync { implicit loginState =>
    implicit request =>
      Future(Ok(views.html.component.master.order.ordersTake()))
  }

  def ordersTakePublic(): Action[AnyContent] = withLoginActionAsync { implicit loginState =>
    implicit request =>
      val publicTakeOrderIDs = blockchainOrders.Service.getAllPublicOrderIDs

      def getPublicOrders(publicOrderIDs: Seq[String]) = masterOrders.Service.getAllByIDs(publicOrderIDs)

      (for {
        publicOrderIDs <- publicTakeOrderIDs
        publicOrders <- getPublicOrders(publicOrderIDs)
      } yield Ok(views.html.component.master.order.ordersTakePublic(publicOrders = publicOrders))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def ordersTakePrivate(): Action[AnyContent] = withLoginActionAsync { implicit loginState =>
    implicit request =>
      val identityIDs = blockchainIdentities.Service.getAllIDsByProvisioned(loginState.address)

      def getPrivateOrderIDs(identityIDs: Seq[String]) = blockchainOrders.Service.getAllPrivateOrderIDs(identityIDs)

      def getPrivateOrders(privateOrderIDs: Seq[String]) = masterOrders.Service.getAllByIDs(privateOrderIDs)

      (for {
        identityIDs <- identityIDs
        privateOrderIDs <- getPrivateOrderIDs(identityIDs)
        privateOrders <- getPrivateOrders(privateOrderIDs)
      } yield Ok(views.html.component.master.order.ordersTakePrivate(privateOrders = privateOrders))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def accountSplits(): Action[AnyContent] = withLoginActionAsync { implicit loginState =>
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

  def provisionedAddresses(identityID: String): EssentialAction = cached.apply(req => req.path, constants.AppConfig.CacheDuration) {
    withoutLoginActionAsync { implicit loginState =>
      implicit request =>
        val provisionedAddresses = blockchainIdentities.Service.getAllProvisionAddresses(identityID)
        (for (
          provisionedAddresses <- provisionedAddresses
        ) yield Ok(views.html.component.blockchain.identity.provisionedAddresses(identityID, provisionedAddresses))
          ).recover {
          case baseException: BaseException => InternalServerError(baseException.failure.message)
        }
    }
  }

  def unprovisionedAddresses(identityID: String): EssentialAction = cached.apply(req => req.path, constants.AppConfig.CacheDuration) {
    withoutLoginActionAsync { implicit loginState =>
      implicit request =>
        val unprovisionedAddresses = blockchainIdentities.Service.getAllUnprovisionAddresses(identityID)
        (for (
          unprovisionedAddresses <- unprovisionedAddresses
        ) yield Ok(views.html.component.blockchain.identity.unprovisionedAddresses(identityID, unprovisionedAddresses))
          ).recover {
          case baseException: BaseException => InternalServerError(baseException.failure.message)
        }
    }
  }

  def getTransaction(transactionType: String): Action[AnyContent] = withLoginActionAsync { implicit loginState =>
    implicit request =>

      transactionType match {
        case constants.Blockchain.TransactionRequest.IDENTITY_NUB =>
          val nubTransactions = blockchainTransactionsIdentityNubs.Service.getTransactionList(loginState.address)
          for {
            nubTransactions <- nubTransactions
          } yield Ok(views.html.component.blockchain.identity.identityNubTransactions(nubTransactions))
        case constants.Blockchain.TransactionRequest.IDENTITY_DEFINE =>
          val identityDefinitionTxs = blockchainTransactionsIdentityDefines.Service.getTransactionList(loginState.address)
          for {
            identityDefinitionTxs <- identityDefinitionTxs
          } yield Ok(views.html.component.blockchain.identity.identityDefineTransactions(identityDefinitionTxs))
        case constants.Blockchain.TransactionRequest.IDENTITY_ISSUE =>
          val issueTransaction = blockchainTransactionsIdentityIssues.Service.getTransactionList(loginState.address)
          for {
            issueTransaction <- issueTransaction
          } yield Ok(views.html.component.blockchain.identity.identityIssueTransactions(issueTransaction))
        case constants.Blockchain.TransactionRequest.IDENTITY_PROVISION =>
          val provisionTransaction = blockchainTransactionsIdentityProvisions.Service.getTransactionList(loginState.address)
          for {
            provisionTransaction <- provisionTransaction
          } yield Ok(views.html.component.blockchain.identity.identityProvisionTransactions(provisionTransaction))
        case constants.Blockchain.TransactionRequest.IDENTITY_UNPROVISION =>
          val unprovisionTransaction = blockchainTransactionsIdentityUnprovisions.Service.getTransactionList(loginState.address)
          for {
            unprovisionTransaction <- unprovisionTransaction
          } yield Ok(views.html.component.blockchain.identity.identityUnprovisionTransactions(unprovisionTransaction))
        case constants.Blockchain.TransactionRequest.ASSET_DEFINE =>
          val assetDefineTransactions = blockchainTransactionsAssetDefines.Service.getTransactionList(loginState.address)
          for {
            assetDefineTransactions <- assetDefineTransactions
          } yield Ok(views.html.component.blockchain.asset.assetDefineTransactions(assetDefineTransactions))
        case constants.Blockchain.TransactionRequest.ASSET_MINT =>
          val assetMintTransactions = blockchainTransactionsAssetMints.Service.getTransactionList(loginState.address)
          for {
            assetMintTransactions <- assetMintTransactions
          } yield Ok(views.html.component.blockchain.asset.assetMintTransactions(assetMintTransactions))
        case constants.Blockchain.TransactionRequest.ASSET_MUTATE =>
          val assetMutateTransactions = blockchainTransactionsAssetMutates.Service.getTransactionList(loginState.address)
          for {
            assetMutateTransactions <- assetMutateTransactions
          } yield Ok(views.html.component.blockchain.asset.assetMutateTransactions(assetMutateTransactions))
        case constants.Blockchain.TransactionRequest.ASSET_BURN =>
          val assetBurnTransactions = blockchainTransactionsAssetBurns.Service.getTransactionList(loginState.address)
          for {
            assetBurnTransactions <- assetBurnTransactions
          } yield Ok(views.html.component.blockchain.asset.assetBurnTransactions(assetBurnTransactions))
        case constants.Blockchain.TransactionRequest.ORDER_DEFINE =>
          val orderDefineTransactions = blockchainTransactionsOrderDefines.Service.getTransactionList(loginState.address)
          for {
            orderDefineTransactions <- orderDefineTransactions
          } yield Ok(views.html.component.blockchain.order.orderDefineTransactions(orderDefineTransactions))
        case constants.Blockchain.TransactionRequest.ORDER_MAKE =>
          val orderMakeTransactions = blockchainTransactionsOrderMakes.Service.getTransactionList(loginState.address)
          for {
            orderMakeTransactions <- orderMakeTransactions
          } yield Ok(views.html.component.blockchain.order.orderMakeTransactions(orderMakeTransactions))
        case constants.Blockchain.TransactionRequest.ORDER_TAKE =>
          val orderTakeTransactions = blockchainTransactionsOrderTakes.Service.getTransactionList(loginState.address)
          for {
            orderTakeTransactions <- orderTakeTransactions
          } yield Ok(views.html.component.blockchain.order.orderTakeTransactions(orderTakeTransactions))
        case constants.Blockchain.TransactionRequest.ORDER_CANCEL =>
          val orderCancelsTransactions = blockchainTransactionsOrderCancels.Service.getTransactionList(loginState.address)
          for {
            orderCancelsTransactions <- orderCancelsTransactions
          } yield Ok(views.html.component.blockchain.order.orderCancelTransaction(orderCancelsTransactions))
        case _ => Future(throw new BaseException(constants.Response.TRANSACTION_NOT_FOUND))
      }
  }

  def moduleTransactions(currentModule: String): Action[AnyContent] = withLoginActionAsync { implicit loginState =>
    implicit request =>
      currentModule match {
        case constants.View.IDENTITY => Future(Ok(views.html.component.blockchain.identity.identityTransactions()))
        case constants.View.ASSET => Future(Ok(views.html.component.blockchain.asset.assetTransactions()))
        case constants.View.ORDER => Future(Ok(views.html.component.blockchain.order.orderTransactions()))
        case _ => Future(throw new BaseException(constants.Response.TRANSACTION_NOT_FOUND))
      }
  }

}