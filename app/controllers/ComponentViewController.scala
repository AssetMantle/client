package controllers

import constants.AppConfig._
import controllers.actions._
import exceptions.BaseException
import models._
import models.blockchain._
import models.common.Serializable.Coin
import models.keyBase.ValidatorAccount
import play.api.cache.Cached
import play.api.i18n.I18nSupport
import play.api.mvc._
import play.api.{Configuration, Logger}
import queries.blockchain.{GetDelegatorRewards, GetTransactionsByHash, GetValidatorCommission}
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
                                         blockchainAuthorizations: blockchain.Authorizations,
                                         blockchainBalances: blockchain.Balances,
                                         blockchainDelegations: blockchain.Delegations,
                                         blockchainUndelegations: blockchain.Undelegations,
                                         blockchainRedelegations: blockchain.Redelegations,
                                         blockchainBlocks: blockchain.Blocks,
                                         blockchainWithdrawAddresses: blockchain.WithdrawAddresses,
                                         blockchainTransactions: blockchain.Transactions,
                                         blockchainTokens: blockchain.Tokens,
                                         blockchainProposals: blockchain.Proposals,
                                         blockchainParameters: blockchain.Parameters,
                                         blockchainProposalDeposits: blockchain.ProposalDeposits,
                                         blockchainProposalVotes: blockchain.ProposalVotes,
                                         blockchainValidators: blockchain.Validators,
                                         blockchainIdentities: blockchain.Identities,
                                         blockchainAssets: blockchain.Assets,
                                         blockchainOrders: blockchain.Orders,
                                         blockchainSplits: blockchain.Splits,
                                         blockchainMaintainers: blockchain.Maintainers,
                                         blockchainClassifications: blockchain.Classifications,
                                         cached: Cached,
                                         getDelegatorRewards: GetDelegatorRewards,
                                         getValidatorCommission: GetValidatorCommission,
                                         getTxByHash: GetTransactionsByHash,
                                         masterTransactionTokenPrices: masterTransaction.TokenPrices,
                                         masterTransactionWalletTransactions: masterTransaction.WalletTransactions,
                                         masterTransactionValidatorTransactions: masterTransaction.ValidatorTransactions,
                                         keyBaseValidatorAccounts: keyBase.ValidatorAccounts,
                                         withoutLoginAction: WithoutLoginAction,
                                         withoutLoginActionAsync: WithoutLoginActionAsync,
                                         messagesControllerComponents: MessagesControllerComponents,
                                       )(implicit configuration: Configuration, executionContext: ExecutionContext) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_COMPONENT_VIEW

  private val priceChartDataPoints = configuration.get[Int]("blockchain.token.priceChartDataPoints")

  private val transactionsStatisticsBinWidth = configuration.get[Int]("statistics.transactions.binWidth")

  def recentActivities: EssentialAction = cached.apply(req => req.path, constants.AppConfig.CacheDuration) {
    withoutLoginAction { implicit loginState =>
      implicit request =>
        Ok(views.html.component.master.notification.recentActivities())
    }
  }

  def wallet(address: String): EssentialAction = cached.apply(req => req.path + "/" + address, constants.AppConfig.CacheDuration) {
    withoutLoginActionAsync { implicit loginState =>
      implicit request =>
        Future(Ok(views.html.component.blockchain.account.wallet(address)))
    }
  }

  def document(id: String): EssentialAction = cached.apply(req => req.path + "/" + id, constants.AppConfig.CacheDuration) {
    withoutLoginActionAsync { implicit loginState =>
      implicit request =>
        val idBytes = try utilities.Secrets.base64URLDecode(id) catch {
          case _: Exception => try utilities.Secrets.base64Decoder(id) catch {
            case _: Exception => Array[Byte]()
          }
        }
        val classification = if (idBytes.nonEmpty) blockchainClassifications.Service.get(idBytes) else Future(None)
        val asset = if (idBytes.nonEmpty) blockchainAssets.Service.get(idBytes) else Future(None)
        val identity = if (idBytes.nonEmpty) blockchainIdentities.Service.get(idBytes) else Future(None)
        val order = if (idBytes.nonEmpty) blockchainOrders.Service.get(idBytes) else Future(None)
        val maintainer = if (idBytes.nonEmpty) blockchainMaintainers.Service.get(id) else Future(None)

        for {
          asset <- asset
          identity <- identity
          order <- order
          classification <- classification
          maintainer <- maintainer
        } yield {
          if (asset.isDefined)
            Ok(views.html.component.blockchain.document.document(id, Option(asset.get.getDocument), asset.get.getDocumentType))
          else if (identity.isDefined)
            Ok(views.html.component.blockchain.document.document(id, Option(identity.get.getDocument), identity.get.getDocumentType))
          else if (order.isDefined)
            Ok(views.html.component.blockchain.document.document(id, Option(order.get.getDocument), order.get.getDocumentType))
          else if (classification.isDefined)
            Ok(views.html.component.blockchain.document.document(id, Option(classification.get.getDocument), classification.get.getDocumentType))
          else if (maintainer.isDefined)
            Ok(views.html.component.blockchain.document.document(id, Option(maintainer.get.getDocument), maintainer.get.getDocumentType))
          else Ok(views.html.component.blockchain.document.document(id, None, ""))

        }
    }
  }

  def block(height: Int): EssentialAction = cached.apply(req => req.path + "/" + height.toString, constants.AppConfig.CacheDuration) {
    withoutLoginActionAsync { implicit loginState =>
      implicit request =>
        Future(Ok(views.html.component.blockchain.block.block(height)))
    }
  }

  def parameters: EssentialAction = cached.apply(req => req.path, 3600) {
    withoutLoginActionAsync { implicit loginState =>
      implicit request =>
        val parameters = blockchainParameters.Service.getAll

        for {
          parameters <- parameters
        } yield Ok(views.html.component.blockchain.parameters.paramaters(parameters))
    }
  }

  def transaction(txHash: String): EssentialAction = cached.apply(req => req.path + "/" + txHash, constants.AppConfig.CacheDuration) {
    withoutLoginActionAsync { implicit loginState =>
      implicit request =>
        Future(Ok(views.html.component.blockchain.transaction.transaction(txHash)))
    }
  }

  def validator(address: String): EssentialAction = cached.apply(req => req.path + "/" + address, constants.AppConfig.CacheDuration) {
    withoutLoginActionAsync { implicit loginState =>
      implicit request =>
        Future(Ok(views.html.component.blockchain.validator.validator(address)))
    }
  }

  def proposal(id: Int): EssentialAction = cached.apply(req => req.path + "/" + id.toString, constants.AppConfig.CacheDuration) {
    withoutLoginActionAsync { implicit loginState =>
      implicit request =>
        Future(Ok(views.html.component.blockchain.proposal.proposal(id)))
    }
  }

  def dashboard: EssentialAction = cached.apply(req => req.path, constants.AppConfig.CacheDuration) {
    withoutLoginActionAsync { implicit loginState =>
      implicit request =>
        Future(Ok(views.html.component.blockchain.dashboard.dashboard()))
    }
  }

  def latestBlockHeight(): EssentialAction = cached.apply(req => req.path, constants.AppConfig.CacheDuration) {
    withoutLoginActionAsync { implicit loginState =>
      implicit request =>
        val latestBlock = blockchainBlocks.Service.tryGetLatestBlock

        def getAverageBlockTime(latestBlock: Block) = blockchainBlocks.Utility.getAverageBlockTime(fromBlock = Option(latestBlock.height))

        def getProposer(proposerAddress: String) = blockchainValidators.Service.tryGetProposerName(proposerAddress)

        (for {
          latestBlock <- latestBlock
          averageBlockTime <- getAverageBlockTime(latestBlock)
          proposer <- getProposer(latestBlock.proposerAddress)
        } yield Ok(views.html.component.blockchain.latestBlockHeight(blockHeight = latestBlock.height, proposer = proposer, time = latestBlock.time, averageBlockTime = averageBlockTime))
          ).recover {
          case baseException: BaseException => InternalServerError(baseException.failure.message)
          case exception: Exception => logger.error(exception.getLocalizedMessage, exception)
            InternalServerError(exception.getLocalizedMessage)
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
        } yield Ok(views.html.component.blockchain.votingPowers(sortedVotingPowerMap = ListMap(getVotingPowerMaps(allSortedValidators.filter(x => x.status == schema.constants.Validator.Status.BONDED)): _*), totalActiveValidators = allSortedValidators.count(x => x.status == schema.constants.Validator.Status.BONDED), totalValidators = allSortedValidators.length))
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
        val latestBlock = blockchainBlocks.Service.tryGetLatestBlock
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
          txStatisticsData <- getTxData(latestBlock.time)
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

        (for {
          messagesData <- messagesData
        } yield Ok(views.html.component.blockchain.dashboard.transactionMessagesStatistics(messagesData = messagesData))
          ).recover {
          case baseException: BaseException => InternalServerError(baseException.failure.message)
        }
    }
  }

  def accountWallet(address: String): EssentialAction = cached.apply(req => req.path + "/" + address, constants.AppConfig.CacheDuration) {
    withoutLoginActionAsync { implicit loginState =>
      implicit request =>
        val operatorAddress = Future(utilities.Crypto.convertAccountAddressToOperatorAddress(address))
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

        def getDelegatedAmount(delegations: Seq[Delegation], validators: Seq[Validator]): Coin = Coin(constants.Blockchain.StakingDenom, delegations.map(x => validators.find(_.operatorAddress == x.validatorAddress).fold(constants.Response.VALIDATOR_NOT_FOUND.throwBaseException())(_.getTokensFromShares(x.shares))).sum)

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

        def getDelegationsMap(delegations: Seq[Delegation], validators: Seq[Validator]) = ListMap(delegations.sortBy(_.shares).reverse.map(delegation => delegation.validatorAddress -> validators.find(_.operatorAddress == delegation.validatorAddress).getOrElse(constants.Response.VALIDATOR_NOT_FOUND.throwBaseException()).getTokensFromShares(delegation.shares)): _*)

        def getUndelegationsMap(undelegations: Seq[Undelegation]) = ListMap(undelegations.map(undelegation => undelegation.validatorAddress -> undelegation.entries): _*)

        def getValidatorsMoniker(validators: Seq[Validator]) = Map(validators.map(validator => validator.operatorAddress -> validator.description.moniker): _*)

        def getRedelegationsMap(redelegations: Seq[Redelegation]) = ListMap(redelegations.map(redelegation => (redelegation.validatorSourceAddress, redelegation.validatorDestinationAddress) -> redelegation.entries): _*)

        (for {
          delegations <- delegations
          undelegations <- undelegations
          validators <- validators
          redelegations <- redelegations
        } yield Ok(views.html.component.blockchain.account.accountDelegations(delegations = getDelegationsMap(delegations, validators), undelegations = getUndelegationsMap(undelegations), validatorsMoniker = getValidatorsMoniker(validators), redelegations = getRedelegationsMap(redelegations), validators = validators))
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
      val walletTxs = masterTransactionWalletTransactions.Service.getTransactions(address, page)

      def getTransactions(txHashes: Seq[String]) = blockchainTransactions.Service.get(txHashes)

      def blocks(heights: Seq[Int]) = blockchainBlocks.Service.get(heights)

      (for {
        walletTxs <- walletTxs
        transactions <- getTransactions(walletTxs.map(_.txHash))
        blocks <- blocks(transactions.map(_.height))
      } yield Ok(views.html.component.blockchain.account.accountTransactionsPerPage(transactions.sortBy(_.height).reverse, blocks))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def accountAuthorizations(address: String): EssentialAction = cached.apply(req => req.path + "/" + address, constants.AppConfig.CacheDuration) {
    withoutLoginActionAsync { implicit loginState =>
      implicit request =>
        val granted = blockchainAuthorizations.Service.getListByGranter(address)
        val assigned = blockchainAuthorizations.Service.getListByGrantee(address)

        for {
          granted <- granted
          assigned <- assigned
        } yield Ok(views.html.component.blockchain.account.accountAuthorizations(granted = granted, assigned = assigned))
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
            val validator = validators.find(_.hexAddress == block.proposerAddress).getOrElse(constants.Response.VALIDATOR_NOT_FOUND.throwBaseException())
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

        def blocks(heights: Seq[Int]) = blockchainBlocks.Service.get(heights)

        for {
          transactions <- transactions
          blocks <- blocks(transactions.map(_.height))
        } yield Ok(views.html.component.blockchain.transaction.transactionListPage(transactions, blocks))
      }).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def transactionDetails(txHash: String): EssentialAction = cached.apply(req => req.path + "/" + txHash, constants.AppConfig.CacheDuration) {
    withoutLoginActionAsync { implicit loginState =>
      implicit request =>
        val transaction = blockchainTransactions.Service.tryGet(txHash)

        def block(height: Int) = blockchainBlocks.Service.tryGet(height)

        (for {
          transaction <- transaction
          block <- block(transaction.height)
        }
        yield Ok(views.html.component.blockchain.transaction.transactionDetails(transaction, block))
          ).recover {
          case baseException: BaseException => InternalServerError(baseException.failure.message)
        }
    }
  }

  def withdrawRewardAmount(txHash: String, msgIndex: Int): EssentialAction = cached.apply(req => req.path + "/" + txHash + "/" + msgIndex.toString, constants.AppConfig.CacheDuration) {
    withoutLoginActionAsync { implicit loginState =>
      implicit request =>
        val transaction = getTxByHash.Service.get(txHash).map(_.result.toTransactionWithLog)
        (for {
          transaction <- transaction
        } yield {
          if (transaction.status) {
            val coinArray = utilities.JSON.convertJsonStringToObject[Seq[EventWrapper]](transaction.log.get)
              .find(_.msg_index.getOrElse(0) == msgIndex)
              .fold(MicroNumber.zero + constants.Blockchain.StakingDenom)(_.events.find(_.`type` == schema.constants.Event.WithdrawRewards).fold(MicroNumber.zero + constants.Blockchain.StakingDenom)(_.attributes.find(_.key == schema.constants.Event.Attribute.Amount).fold(MicroNumber.zero + constants.Blockchain.StakingDenom)(_.value.getOrElse(MicroNumber.zero + constants.Blockchain.StakingDenom))))
              .split(constants.RegularExpression.NUMERIC_AND_STRING_SEPARATOR).filter(_.nonEmpty).toList
            Ok(Coin(coinArray.tail.head, coinArray.head.toDouble / 1000000).getAmountWithNormalizedDenom())
          } else Ok(Coin(constants.Blockchain.StakingDenom, MicroNumber.zero).getAmountWithNormalizedDenom())
        }
          ).recover {
          case baseException: BaseException => BadRequest("N/A")
        }
    }
  }

  def transactionMessages(txHash: String): EssentialAction = cached.apply(req => req.path + "/" + txHash, constants.AppConfig.CacheDuration) {
    withoutLoginActionAsync { implicit loginState =>
      implicit request =>
        val transaction = blockchainTransactions.Service.tryGet(txHash)

        (for {
          transaction <- transaction
        } yield Ok(views.html.component.blockchain.transaction.transactionMessages(txHash, transaction))
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
        val allValidators = blockchainValidators.Service.getAll

        def proposalVotes(allValidators: Seq[String]) = blockchainProposalVotes.Service.getAllByIDAndAddresses(id, allValidators)

        (for {
          allValidators <- allValidators
          proposalVotes <- proposalVotes(allValidators.map(_.getDelegatorAddress))
        } yield Ok(views.html.component.blockchain.proposal.proposalVotes(proposalVotes, allValidators.map(x => x.getDelegatorAddress -> x.description.moniker).toMap))
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
        } yield Ok(views.html.component.blockchain.validator.validatorDetails(validator, utilities.Crypto.convertOperatorAddressToAccountAddress(validator.operatorAddress), (validator.tokens * 100 / totalBondedAmount).toRoundedOffString(), schema.constants.Validator.Status.BONDED, keyBaseValidator))
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
          val selfDelegated = delegations.find(x => x.delegatorAddress == utilities.Crypto.convertOperatorAddressToAccountAddress(x.validatorAddress)).fold(BigDecimal(0.0))(_.shares)
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
      val validatorTxs = masterTransactionValidatorTransactions.Service.getTransactions(address, page)

      def getTransactions(txHashes: Seq[String]) = blockchainTransactions.Service.get(txHashes)

      def blocks(heights: Seq[Int]) = blockchainBlocks.Service.get(heights)

      (for {
        validatorTxs <- validatorTxs
        transactions <- getTransactions(validatorTxs.map(_.txHash))
        blocks <- blocks(transactions.map(_.height))
      } yield Ok(views.html.component.blockchain.validator.validatorTransactionsPerPage(transactions.sortBy(_.height).reverse, blocks))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def assetMantleStatistics: EssentialAction = cached.apply(req => req.path, constants.AppConfig.CacheDuration) {
    withoutLoginActionAsync { implicit loginState =>
      implicit request =>
        val identitiesIssued = blockchainIdentities.Service.countAll
        val namedIdentities = blockchainIdentities.Service.namedIdentities
        val identityClasses = blockchainClassifications.Service.countClasses(constants.Document.ClassificationType.IDENTITY)

        val assetsMinted = blockchainAssets.Service.countAll
        val assetsClasses = blockchainClassifications.Service.countClasses(constants.Document.ClassificationType.ASSET)

        val liveOrders = blockchainOrders.Service.countAll
        val orderClasses = blockchainClassifications.Service.countClasses(constants.Document.ClassificationType.ORDER)

        (for {
          identitiesIssued <- identitiesIssued
          namedIdentities <- namedIdentities
          identityClasses <- identityClasses
          assetsMinted <- assetsMinted
          assetsClasses <- assetsClasses
          liveOrders <- liveOrders
          orderClasses <- orderClasses
        } yield Ok(views.html.component.blockchain.dashboard.assetMantleStatistics(identitiesIssued = identitiesIssued, namedIdentities = namedIdentities, identityClasses = identityClasses, assetsMinted = assetsMinted, assetsClasses = assetsClasses, liveOrders = liveOrders, orderClasses = orderClasses))
          ).recover {
          case baseException: BaseException => InternalServerError(baseException.failure.message)
        }
    }
  }

}

