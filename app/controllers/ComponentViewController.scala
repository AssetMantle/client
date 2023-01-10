package controllers

import constants.AppConfig._
import controllers.actions._
import controllers.results.WithUsernameToken
import exceptions.BaseException
import models._
import models.blockchain._
import models.common.ID
import models.common.ID.IdentityID
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
                                         analyticTransactionCounters: analytic.TransactionCounters,
                                         analyticMessageCounters: analytic.MessageCounters,
                                         blockchainAccounts: blockchain.Accounts,
                                         blockchainAssets: blockchain.Assets,
                                         blockchainBalances: blockchain.Balances,
                                         blockchainDelegations: blockchain.Delegations,
                                         blockchainClassifications: blockchain.Classifications,
                                         blockchainIdentities: blockchain.Identities,
                                         blockchainIdentityProvisions: blockchain.IdentityProvisions,
                                         blockchainIdentityUnprovisions: blockchain.IdentityUnprovisions,
                                         blockchainMaintainers: blockchain.Maintainers,
                                         blockchainMetas: blockchain.Metas,
                                         blockchainOrders: blockchain.Orders,
                                         blockchainSplits: blockchain.Splits,
                                         blockchainUndelegations: blockchain.Undelegations,
                                         blockchainRedelegations: blockchain.Redelegations,
                                         blockchainBlocks: blockchain.Blocks,
                                         blockchainWithdrawAddresses: blockchain.WithdrawAddresses,
                                         blockchainTransactions: blockchain.Transactions,
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

  def wallet(address: String): EssentialAction = cached.apply(req => req.path + "/" + address, constants.AppConfig.CacheDuration) {
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

  def block(height: Int): EssentialAction = cached.apply(req => req.path + "/" + height.toString, constants.AppConfig.CacheDuration) {
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

  def transaction(txHash: String): EssentialAction = cached.apply(req => req.path + "/" + txHash, constants.AppConfig.CacheDuration) {
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

  def validator(address: String): EssentialAction = cached.apply(req => req.path + "/" + address, constants.AppConfig.CacheDuration) {
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

  def proposal(id: Int): EssentialAction = cached.apply(req => req.path + "/" + id.toString, constants.AppConfig.CacheDuration) {
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
            withUsernameToken.Ok(views.html.component.blockchain.dashboard.dashboard())
          }
          case None => Future(Ok(views.html.component.blockchain.dashboard.dashboard()))
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
        } yield Ok(views.html.component.blockchain.votingPowers(sortedVotingPowerMap = ListMap(getVotingPowerMaps(allSortedValidators.filter(x => x.status == constants.Blockchain.ValidatorStatus.BONDED)): _*), totalActiveValidators = allSortedValidators.count(x => x.status == constants.Blockchain.ValidatorStatus.BONDED), totalValidators = allSortedValidators.length))
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
        } yield Ok(views.html.component.blockchain.dashboard.tokensPrices(tokenPrices, constants.Blockchain.StakingDenom, tokenTickers))
          ).recover {
          case baseException: BaseException => InternalServerError(baseException.failure.message)
        }
    }
  }

  def transactionStatistics(): EssentialAction = cached.apply(req => req.path, constants.AppConfig.CacheDuration) {
    withoutLoginActionAsync { implicit loginState =>
      implicit request =>
        val dayEpoch: Long = 24 * 60 * 60
        val totalAccounts = blockchainBalances.Service.getTotalAccounts
        val latestBlock = blockchainBlocks.Service.getLatestBlock
        val totalTxs = blockchainTransactions.Service.getTotalTransactions

        def getTxData(latestHeightEpoch: Long) = {
          val endEpoch = (latestHeightEpoch / dayEpoch + 1) * dayEpoch
          val startEpoch = endEpoch - 10 * dayEpoch
          analyticTransactionCounters.Utility.getTransactionStatisticsData(startEpoch = startEpoch, endEpoch = endEpoch)
        }

        (for {
          totalAccounts <- totalAccounts
          latestBlock <- latestBlock
          totalTxs <- totalTxs
          txStatisticsData <- getTxData(latestBlock.time.unix)
        } yield Ok(views.html.component.blockchain.dashboard.transactionStatistics(
          totalAccounts = totalAccounts,
          totalTxs = totalTxs,
          txData = txStatisticsData))
          ).recover {
          case baseException: BaseException => InternalServerError(baseException.failure.message)
        }
    }
  }

  def transactionMessagesStatistics(): EssentialAction = cached.apply(req => req.path, constants.AppConfig.CacheDuration) {
    withoutLoginActionAsync { implicit loginState =>
      implicit request =>
        val messagesData = analyticMessageCounters.Utility.getMessagesStatistics
        val ibcTxsCount = analyticMessageCounters.Service.getByMessageTypes(
          Seq(constants.View.TxMessagesMap.getOrElse(constants.Blockchain.TransactionMessage.TRANSFER, constants.Blockchain.TransactionMessage.TRANSFER),
            constants.View.TxMessagesMap.getOrElse(constants.Blockchain.TransactionMessage.RECV_PACKET, constants.Blockchain.TransactionMessage.RECV_PACKET),
            constants.View.TxMessagesMap.getOrElse(constants.Blockchain.TransactionMessage.RECV_PACKET, constants.Blockchain.TransactionMessage.RECV_PACKET),
            constants.View.TxMessagesMap.getOrElse(constants.Blockchain.TransactionMessage.DELEGATE, constants.Blockchain.TransactionMessage.DELEGATE),
            constants.View.TxMessagesMap.getOrElse(constants.Blockchain.TransactionMessage.EXECUTE_AUTHORIZATION, constants.Blockchain.TransactionMessage.EXECUTE_AUTHORIZATION),
          ))

        (for {
          messagesData <- messagesData
          ibcTxsCount <- ibcTxsCount
        } yield Ok(views.html.component.blockchain.dashboard.transactionMessagesStatistics(
          ibcIn = ibcTxsCount.find(_.messageType == constants.View.TxMessagesMap.getOrElse(constants.Blockchain.TransactionMessage.RECV_PACKET, constants.Blockchain.TransactionMessage.RECV_PACKET)).fold(0)(_.counter),
          ibcOut = ibcTxsCount.find(_.messageType == constants.View.TxMessagesMap.getOrElse(constants.Blockchain.TransactionMessage.TRANSFER, constants.Blockchain.TransactionMessage.TRANSFER)).fold(0)(_.counter),
          delegate = ibcTxsCount.find(_.messageType == constants.View.TxMessagesMap.getOrElse(constants.Blockchain.TransactionMessage.DELEGATE, constants.Blockchain.TransactionMessage.DELEGATE)).fold(0)(_.counter),
          executeAuthorization = ibcTxsCount.find(_.messageType == constants.View.TxMessagesMap.getOrElse(constants.Blockchain.TransactionMessage.EXECUTE_AUTHORIZATION, constants.Blockchain.TransactionMessage.EXECUTE_AUTHORIZATION)).fold(0)(_.counter),
          messagesData = messagesData))
          ).recover {
          case baseException: BaseException => InternalServerError(baseException.failure.message)
        }
    }
  }

  def accountWallet(address: String): EssentialAction = cached.apply(req => req.path + "/" + address, constants.AppConfig.CacheDuration) {
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

        def getTokenPrice = masterTransactionTokenPrices.Service.getLatestTokenPrice(denom = constants.Blockchain.StakingDenom)

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

  def accountDelegations(address: String): EssentialAction = cached.apply(req => req.path + "/" + address, constants.AppConfig.CacheDuration) {
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

  def accountTransactions(address: String): EssentialAction = cached.apply(req => req.path + "/" + address, constants.AppConfig.CacheDuration) {
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
          }.toMap
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

  def blockDetails(height: Int): EssentialAction = cached.apply(req => req.path + "/" + height.toString, constants.AppConfig.CacheDuration) {
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

  def blockTransactions(height: Int): EssentialAction = cached.apply(req => req.path + "/" + height.toString, constants.AppConfig.CacheDuration) {
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

  def transactionDetails(txHash: String): EssentialAction = cached.apply(req => req.path + "/" + txHash, constants.AppConfig.CacheDuration) {
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

  def withdrawRewardAmount(txHash: String, msgIndex: Int): EssentialAction = cached.apply(req => req.path + "/" + txHash + "/" + msgIndex.toString, constants.AppConfig.CacheDuration) {
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

  def transactionMessages(txHash: String): EssentialAction = cached.apply(req => req.path + "/" + txHash, constants.AppConfig.CacheDuration) {
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

  def proposalDetails(id: Int): EssentialAction = cached.apply(req => req.path + "/" + id.toString, constants.AppConfig.CacheDuration) {
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

  def proposalDeposits(id: Int): EssentialAction = cached.apply(req => req.path + "/" + id.toString, constants.AppConfig.CacheDuration) {
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

  def proposalVotes(id: Int): EssentialAction = cached.apply(req => req.path + "/" + id.toString, constants.AppConfig.CacheDuration) {
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

  def validatorDetails(address: String): EssentialAction = cached.apply(req => req.path + "/" + address, constants.AppConfig.CacheDuration) {
    withoutLoginActionAsync { implicit loginState =>
      implicit request =>
        val validator = blockchainValidators.Service.tryGet(address)
        val totalBondedAmount = blockchainTokens.Service.getTotalBondedAmount

        def keyBaseValidator(address: String): Future[Option[ValidatorAccount]] = keyBaseValidatorAccounts.Service.get(address)

        (for {
          validator <- validator
          totalBondedAmount <- totalBondedAmount
          keyBaseValidator <- keyBaseValidator(validator.operatorAddress)
        } yield Ok(views.html.component.blockchain.validator.validatorDetails(validator, utilities.Bech32.convertOperatorAddressToAccountAddress(validator.operatorAddress), (validator.tokens * 100 / totalBondedAmount).toRoundedOffString(), constants.Blockchain.ValidatorStatus.BONDED, keyBaseValidator))
          ).recover {
          case baseException: BaseException => InternalServerError(baseException.failure.message)
        }
    }
  }

  def validatorUptime(address: String, n: Int): EssentialAction = cached.apply(req => req.path + "/" + address + "/" + n.toString, constants.AppConfig.CacheDuration) {
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

  def validatorDelegations(address: String): EssentialAction = cached.apply(req => req.path + "/" + address, constants.AppConfig.CacheDuration) {
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

  def validatorTransactions(address: String): EssentialAction = cached.apply(req => req.path + "/" + address, constants.AppConfig.CacheDuration) {
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

  def classification(id: String): EssentialAction = cached.apply(req => req.path + "/" + id, constants.AppConfig.CacheDuration) {
    withoutLoginActionAsync { implicit loginState =>
      implicit request => {
        val classificationID = ID.getClassificationID(id)
        if (classificationID.nonEmpty) {
          val classification = blockchainClassifications.Service.tryGet(classificationID.get)
          val maintainers = blockchainMaintainers.Service.getByClassificationID(classificationID.get)
          (for {
            classification <- classification
            maintainers <- maintainers
          } yield Ok(views.html.component.blockchain.classification.classification(classification = classification, maintainers = maintainers))
            ).recover {
            case baseException: BaseException => InternalServerError(baseException.failure.message)
          }
        } else Future(BadRequest(views.html.index(failures = Seq(constants.Response.INVALID_CLASSIFICATION_ID))))
      }
    }
  }

  def identity(id: String): EssentialAction = cached.apply(req => req.path + "/" + id, constants.AppConfig.CacheDuration) {
    withoutLoginActionAsync { implicit loginState =>
      implicit request => {
        val identityID = ID.getIdentityID(id)
        if (identityID.nonEmpty) {
          val identity = blockchainIdentities.Service.tryGet(identityID.get)
          val provisioned = blockchainIdentityProvisions.Service.getAllProvisionAddresses(identityID.get.asString)
          val unprovisioned = blockchainIdentityUnprovisions.Service.getAllUnprovisionAddresses(identityID.get.asString)
          (for {
            identity <- identity
            provisioned <- provisioned
            unprovisioned <- unprovisioned
          } yield Ok(views.html.component.blockchain.identity.identity(identity = identity, provisioned = provisioned, unprovisioned = unprovisioned))
            ).recover {
            case baseException: BaseException => InternalServerError(baseException.failure.message)
          }
        } else Future(BadRequest(views.html.index(failures = Seq(constants.Response.INVALID_IDENTITY_ID))))
      }
    }
  }

  def asset(id: String): EssentialAction = cached.apply(req => req.path + "/" + id, constants.AppConfig.CacheDuration) {
    withoutLoginActionAsync { implicit loginState =>
      implicit request => {
        val assetID = ID.getAssetID(id)
        if (assetID.nonEmpty) {
          val asset = blockchainAssets.Service.tryGet(assetID.get)
          val split = blockchainSplits.Service.tryGetByOwnable(assetID.get.asString)
          (for {
            asset <- asset
            split <- split
          } yield Ok(views.html.component.blockchain.asset.asset(asset = asset, owner = IdentityID(split.ownerID)))
            ).recover {
            case baseException: BaseException => InternalServerError(baseException.failure.message)
          }
        } else Future(BadRequest(views.html.index(failures = Seq(constants.Response.INVALID_ASSET_ID))))
      }
    }
  }

  def order(id: String): EssentialAction = cached.apply(req => req.path + "/" + id, constants.AppConfig.CacheDuration) {
    withoutLoginActionAsync { implicit loginState =>
      implicit request => {
        val orderID = ID.getOrderID(id)
        if (orderID.nonEmpty) {
          val order = blockchainOrders.Service.tryGet(orderID.get)
          (for {
            order <- order
          } yield Ok(views.html.component.blockchain.order.order(order = order))
            ).recover {
            case baseException: BaseException => InternalServerError(baseException.failure.message)
          }
        } else Future(BadRequest(views.html.index(failures = Seq(constants.Response.INVALID_ORDER_ID))))
      }
    }
  }

  def meta(id: String): EssentialAction = cached.apply(req => req.path + "/" + id, constants.AppConfig.CacheDuration) {
    withoutLoginActionAsync { implicit loginState =>
      implicit request => {
        val metaID = ID.getMetaID(id)
        if (metaID.nonEmpty) {
          val meta = blockchainMetas.Service.tryGet(metaID.get)
          (for {
            meta <- meta
          } yield Ok(views.html.component.blockchain.meta.meta(meta = meta))
            ).recover {
            case baseException: BaseException => InternalServerError(baseException.failure.message)
          }
        } else Future(BadRequest(views.html.index(failures = Seq(constants.Response.INVALID_ORDER_ID))))
      }
    }
  }

  def maintainer(id: String): EssentialAction = cached.apply(req => req.path + "/" + id, constants.AppConfig.CacheDuration) {
    withoutLoginActionAsync { implicit loginState =>
      implicit request => {
        val maintainerID = ID.getMaintainerID(id)
        if (maintainerID.nonEmpty) {
          val maintainer = blockchainMaintainers.Service.tryGet(maintainerID.get)
          (for {
            maintainer <- maintainer
          } yield Ok(views.html.component.blockchain.maintainer.maintainer(maintainer = maintainer))
            ).recover {
            case baseException: BaseException => InternalServerError(baseException.failure.message)
          }
        } else Future(BadRequest(views.html.index(failures = Seq(constants.Response.INVALID_ORDER_ID))))
      }
    }
  }

}

