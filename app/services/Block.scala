package services

import actors.{Message => actorsMessage}
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.blockchain.{Validator, Transaction => blockchainTransaction}
import models.common.Parameters.SlashingParameter
import models.common.Serializable.StdMsg
import models.common.TransactionMessages._
import models.{blockchain, keyBase, masterTransaction}
import play.api.{Configuration, Logger}
import play.libs.Json
import queries._
import queries.responses.BlockCommitResponse.{Response => BlockCommitResponse}
import queries.responses.TransactionResponse.{Response => TransactionResponse}
import queries.responses.common.{Event, Header => BlockHeader}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class Block @Inject()(
                       blockchainBlocks: blockchain.Blocks,
                       blockchainAccounts: blockchain.Accounts,
                       blockchainAverageBlockTimes: blockchain.AverageBlockTimes,
                       blockchainAssets: blockchain.Assets,
                       blockchainClassifications: blockchain.Classifications,
                       blockchainDelegations: blockchain.Delegations,
                       blockchainIdentities: blockchain.Identities,
                       blockchainMetas: blockchain.Metas,
                       blockchainParameters: blockchain.Parameters,
                       blockchainMaintainers: blockchain.Maintainers,
                       blockchainOrders: blockchain.Orders,
                       blockchainRedelegations: blockchain.Redelegations,
                       blockchainSigningInfos: blockchain.SigningInfos,
                       blockchainSplits: blockchain.Splits,
                       blockchainTransactions: blockchain.Transactions,
                       blockchainTokens: blockchain.Tokens,
                       blockchainUndelegations: blockchain.Undelegations,
                       blockchainValidators: blockchain.Validators,
                       blockchainWithdrawAddresses: blockchain.WithdrawAddresses,
                       keyBaseValidatorAccounts: keyBase.ValidatorAccounts,
                       getBlockCommit: GetBlockCommit,
                       getTransaction: GetTransaction,
                       getTransactionsByHeight: GetTransactionsByHeight,
                       masterTransactionNotifications: masterTransaction.Notifications,
                     )(implicit exec: ExecutionContext, configuration: Configuration) {

  private implicit val module: String = constants.Module.SERVICES_BLOCK

  private implicit val logger: Logger = Logger(this.getClass)

  def insertOnBlock(height: Int): Future[BlockCommitResponse] = {
    val blockCommitResponse = getBlockCommit.Service.get(height)

    def insertBlock(blockCommitResponse: BlockCommitResponse): Future[Int] = blockchainBlocks.Service.insertOrUpdate(height = blockCommitResponse.result.signed_header.header.height, time = blockCommitResponse.result.signed_header.header.time, proposerAddress = blockCommitResponse.result.signed_header.header.proposer_address, validators = blockCommitResponse.result.signed_header.commit.signatures.flatten.map(_.validator_address))

    (for {
      blockCommitResponse <- blockCommitResponse
      _ <- insertBlock(blockCommitResponse)
    } yield blockCommitResponse
      ).recover {
      case baseException: BaseException => throw baseException
    }
  }

  def insertTransactionsOnBlock(height: Int): Future[Seq[blockchainTransaction]] = {
    val transactionsByHeightResponse = getTransactionsByHeight.Service.get(height)

    def insertTransactions(transactionsHash: Seq[String]): Future[Seq[blockchainTransaction]] = if (transactionsHash.nonEmpty) {
      val transactionResponses = Future.traverse(transactionsHash)(txHash => getTransaction.Service.get(txHash))

      def getTransactions(transactionResponses: Seq[TransactionResponse]): Future[Seq[blockchainTransaction]] = Future(transactionResponses.map(_.toTransaction))

      def insertTxs(transactions: Seq[blockchainTransaction]): Future[Seq[Int]] = blockchainTransactions.Service.insertMultiple(transactions)

      for {
        transactionResponses <- transactionResponses
        transactions <- getTransactions(transactionResponses)
        _ <- actionsOnTransactions(transactions, height)
        _ <- insertTxs(transactions)
      } yield transactions
    } else Future(Seq.empty)

    (for {
      transactionsByHeightResponse <- transactionsByHeightResponse
      transactions <- insertTransactions(transactionsByHeightResponse.result.txs.map(_.hash))
    } yield transactions
      ).recover {
      case baseException: BaseException => if (baseException.failure == constants.Response.JSON_UNMARSHALLING_ERROR) {
        logger.error(baseException.failure.message)
        Seq.empty
      } else throw baseException
    }
  }

  def setAverageBlockTime(blockHeader: BlockHeader): Future[Double] = blockchainAverageBlockTimes.Service.set(blockHeader)

  def getAverageBlockTime: Future[Double] = blockchainAverageBlockTimes.Service.get

  //Should not be called at the same time as when processing txs as it can lead race to update same db table.
  def checksAndUpdatesOnBlock(blockHeader: BlockHeader): Future[Unit] = {
    val undelegations = blockchainUndelegations.Utility.onNewBlock(blockHeader.time)
    val redelegations = blockchainRedelegations.Utility.onNewBlock(blockHeader.time)
    val tokens = blockchainTokens.Utility.updateAll()

    (for {
      _ <- undelegations
      _ <- redelegations
      _ <- tokens
    } yield ()
      ).recover {
      case baseException: BaseException => throw baseException
    }
  }

  def sendNewBlockWebSocketMessage(blockCommitResponse: BlockCommitResponse, transactions: Seq[blockchainTransaction], averageBlockTime: Double): Future[Unit] = {
    val proposer = blockchainValidators.Service.tryGetProposerName(blockCommitResponse.result.signed_header.header.proposer_address)

    def getWebSocketNewBlock(proposer: String): actorsMessage.WebSocket.NewBlock = actorsMessage.WebSocket.NewBlock(
      block = actorsMessage.WebSocket.Block(height = blockCommitResponse.result.signed_header.header.height, time = utilities.Date.bcTimestampToString(blockCommitResponse.result.signed_header.header.time), proposer = proposer),
      txs = transactions.map(tx => actorsMessage.WebSocket.Tx(hash = tx.hash, status = tx.status, numMsgs = tx.messages.length, fees = tx.fee)),
      averageBlockTime = averageBlockTime,
      validators = blockCommitResponse.result.signed_header.commit.signatures.flatten.map(_.validator_address)
    )

    (for {
      proposer <- proposer
    } yield actors.Service.appWebSocketActor ! Json.toJson(getWebSocketNewBlock(proposer)).toString
      ).recover {
      case baseException: BaseException => throw baseException
    }
  }

  private def actionsOnTransactions(transactions: Seq[blockchainTransaction], height: Int): Future[Seq[Seq[Unit]]] = Future.traverse(transactions) { transaction =>
    if (transaction.status) Future.traverse(transaction.messages)(stdMsg => actionOnTxMessages(stdMsg = stdMsg, height = height))
    else Future(Seq.empty)
  }

  def actionOnTxMessages(stdMsg: StdMsg, height: Int): Future[Unit] = {
    try {
      stdMsg.messageType match {
        //bank
        case constants.Blockchain.TransactionMessage.SEND_COIN => blockchainAccounts.Utility.onSendCoin(stdMsg.message.asInstanceOf[SendCoin])
        case constants.Blockchain.TransactionMessage.MULTI_SEND => blockchainAccounts.Utility.onMultiSend(stdMsg.message.asInstanceOf[MultiSend])
        //slashing
        case constants.Blockchain.TransactionMessage.UNJAIL => blockchainValidators.Utility.onUnjail(stdMsg.message.asInstanceOf[Unjail])
        //staking
        case constants.Blockchain.TransactionMessage.CREATE_VALIDATOR => blockchainValidators.Utility.onCreateValidator(stdMsg.message.asInstanceOf[CreateValidator])
        case constants.Blockchain.TransactionMessage.EDIT_VALIDATOR => blockchainValidators.Utility.onEditValidator(stdMsg.message.asInstanceOf[EditValidator])
        case constants.Blockchain.TransactionMessage.DELEGATE => blockchainValidators.Utility.onDelegation(stdMsg.message.asInstanceOf[Delegate])
        case constants.Blockchain.TransactionMessage.REDELEGATE => blockchainRedelegations.Utility.onRedelegation(stdMsg.message.asInstanceOf[Redelegate])
        case constants.Blockchain.TransactionMessage.UNDELEGATE => blockchainUndelegations.Utility.onUndelegation(stdMsg.message.asInstanceOf[Undelegate])
        case constants.Blockchain.TransactionMessage.SET_WITHDRAW_ADDRESS => blockchainWithdrawAddresses.Utility.onSetWithdrawAddress(stdMsg.message.asInstanceOf[SetWithdrawAddress])
        case constants.Blockchain.TransactionMessage.WITHDRAW_DELEGATOR_REWARD => blockchainValidators.Utility.onWithdrawDelegationReward(stdMsg.message.asInstanceOf[WithdrawDelegatorReward])
        case constants.Blockchain.TransactionMessage.WITHDRAW_VALIDATOR_COMMISSION => blockchainValidators.Utility.onWithdrawValidatorCommission(stdMsg.message.asInstanceOf[WithdrawValidatorCommission])
        //Asset
        case constants.Blockchain.TransactionMessage.ASSET_DEFINE => blockchainAssets.Utility.onDefine(stdMsg.message.asInstanceOf[AssetDefine])
        case constants.Blockchain.TransactionMessage.ASSET_MINT => blockchainAssets.Utility.onMint(stdMsg.message.asInstanceOf[AssetMint])
        case constants.Blockchain.TransactionMessage.ASSET_MUTATE => blockchainAssets.Utility.onMutate(stdMsg.message.asInstanceOf[AssetMutate])
        case constants.Blockchain.TransactionMessage.ASSET_BURN => blockchainAssets.Utility.onBurn(stdMsg.message.asInstanceOf[AssetBurn])
        //Identity
        case constants.Blockchain.TransactionMessage.IDENTITY_DEFINE => blockchainIdentities.Utility.onDefine(stdMsg.message.asInstanceOf[IdentityDefine])
        case constants.Blockchain.TransactionMessage.IDENTITY_ISSUE => blockchainIdentities.Utility.onIssue(stdMsg.message.asInstanceOf[IdentityIssue])
        case constants.Blockchain.TransactionMessage.IDENTITY_PROVISION => blockchainIdentities.Utility.onProvision(stdMsg.message.asInstanceOf[IdentityProvision])
        case constants.Blockchain.TransactionMessage.IDENTITY_UNPROVISION => blockchainIdentities.Utility.onUnprovision(stdMsg.message.asInstanceOf[IdentityUnprovision])
        case constants.Blockchain.TransactionMessage.IDENTITY_NUB => blockchainIdentities.Utility.onNub(stdMsg.message.asInstanceOf[IdentityNub])
        //Split
        case constants.Blockchain.TransactionMessage.SPLIT_SEND => blockchainSplits.Utility.onSend(stdMsg.message.asInstanceOf[SplitSend])
        case constants.Blockchain.TransactionMessage.SPLIT_WRAP => blockchainSplits.Utility.onWrap(stdMsg.message.asInstanceOf[SplitWrap])
        case constants.Blockchain.TransactionMessage.SPLIT_UNWRAP => blockchainSplits.Utility.onUnwrap(stdMsg.message.asInstanceOf[SplitUnwrap])
        //Order
        case constants.Blockchain.TransactionMessage.ORDER_DEFINE => blockchainOrders.Utility.onDefine(stdMsg.message.asInstanceOf[OrderDefine])
        case constants.Blockchain.TransactionMessage.ORDER_MAKE => blockchainOrders.Utility.onMake(stdMsg.message.asInstanceOf[OrderMake], height)
        case constants.Blockchain.TransactionMessage.ORDER_TAKE => blockchainOrders.Utility.onTake(stdMsg.message.asInstanceOf[OrderTake])
        case constants.Blockchain.TransactionMessage.ORDER_CANCEL => blockchainOrders.Utility.onCancel(stdMsg.message.asInstanceOf[OrderCancel])
        //meta
        case constants.Blockchain.TransactionMessage.META_REVEAL => blockchainMetas.Utility.onReveal(stdMsg.message.asInstanceOf[MetaReveal])
        //maintainer
        case constants.Blockchain.TransactionMessage.MAINTAINER_DEPUTIZE => blockchainMaintainers.Utility.onDeputize(stdMsg.message.asInstanceOf[MaintainerDeputize])
        case _ => Future(logger.error(constants.Response.TRANSACTION_TYPE_NOT_FOUND.logMessage + ": " + stdMsg.messageType))
      }
    } catch {
      case _: Exception => Future(logger.error(constants.Response.TRANSACTION_TYPE_NOT_FOUND.logMessage + ": " + stdMsg.messageType))
    }
  }

  def onSlashingEvent(slashingEvents: Seq[Event], height: Int): Future[Unit] = if (slashingEvents.nonEmpty) {
    val hexAddresses = slashingEvents.flatMap(_.attributes.find(x => constants.Blockchain.Event.Attribute.Address == x.key && x.value.isDefined).map(x => utilities.Bech32.convertConsensusAddressToHexAddress(x.value.get))).distinct
    val validators = blockchainValidators.Service.getAllByHexAddresses(hexAddresses)
    val slashing = blockchainTokens.Utility.onSlashing

    def updateValidatorAndDelegations(validators: Seq[Validator]): Future[Seq[Unit]] = Future.traverse(validators) { validator =>
      val updatedValidator = blockchainValidators.Utility.insertOrUpdateValidator(validator.operatorAddress)
      val updateDelegation = blockchainRedelegations.Utility.onSlashingEvent(validator.operatorAddress)
      val updateUnbonding = blockchainUndelegations.Utility.onSlashingEvent(validator.operatorAddress)
      val updateSigningInfo = blockchainSigningInfos.Utility.insertOrUpdate(validator.consensusPublicKey)

      for {
        _ <- updatedValidator
        _ <- updateDelegation
        _ <- updateUnbonding
        _ <- updateSigningInfo
      } yield ()
    }

    def addEvents(validators: Seq[Validator]): Future[Seq[Unit]] = Future.traverse(slashingEvents) { event =>
      val hexAddress = event.attributes.find(x => x.key == constants.Blockchain.Event.Attribute.Address && x.value.isDefined).fold("")(x => utilities.Bech32.convertConsensusAddressToHexAddress(x.value.get))
      val insertNotification = validators.find(x => x.hexAddress == hexAddress).fold(Future(""))(validator =>
        utilities.Validator.getSlashingReason(event.attributes.find(x => constants.Blockchain.Event.Attribute.Reason == x.key).fold("")(_.value.getOrElse(""))) match {
          case constants.View.MISSING_SIGNATURE => masterTransactionNotifications.Service.create(constants.Notification.VALIDATOR_MISSING_SIGNATURE_SLASHING, validator.description.moniker.getOrElse(validator.operatorAddress), height.toString)(s"'${validator.operatorAddress}'")
          case constants.View.DOUBLE_SIGNING => masterTransactionNotifications.Service.create(constants.Notification.VALIDATOR_DOUBLE_SIGNING_SLASHING, validator.description.moniker.getOrElse(validator.operatorAddress), height.toString)(s"'${validator.operatorAddress}'")
          case _ => Future("")
        })

      (for {
        _ <- insertNotification
      } yield ()
        ).recover {
        case baseException: BaseException => throw baseException
      }
    }

    (for {
      validators <- validators
      _ <- updateValidatorAndDelegations(validators)
      _ <- slashing
      _ <- addEvents(validators)
    } yield ()
      ).recover {
      case baseException: BaseException => throw baseException
    }
  } else Future()

  def onMissedBlockEvent(livenessEvents: Seq[Event], height: Int): Future[Unit] = if (livenessEvents.nonEmpty) {

    val slashingParameter = blockchainParameters.Service.tryGetSlashingParameter

    def addEvent(validator: Validator, missedBlockCounter: Int, height: Int, slashingParameter: SlashingParameter): Future[String] = {
      //TODO criteria needs to be set to send notification
      //TODO In future if private notifications is asked for missing blocks then it needs to be done from here.
      if (missedBlockCounter % (slashingParameter.minSignedPerWindow * BigDecimal(slashingParameter.signedBlocksWindow) / 10) == 0) {
        masterTransactionNotifications.Service.create(constants.Notification.VALIDATOR_MISSED_BLOCKS, validator.description.moniker.getOrElse(validator.operatorAddress), missedBlockCounter.toString, height.toString)(validator.operatorAddress)
      } else Future("")
    }

    def update(slashingParameter: SlashingParameter) = Future.traverse(livenessEvents) {
      event =>
        val consensusAddress = event.attributes.find(x => x.key == constants.Blockchain.Event.Attribute.Address).fold("")(_.value.getOrElse(""))
        val missedBlocks = event.attributes.find(x => x.key == constants.Blockchain.Event.Attribute.MissedBlocks).fold(0)(_.value.fold(0)(_.toInt))
        val validator = blockchainValidators.Service.tryGetByHexAddress(utilities.Bech32.convertConsensusAddressToHexAddress(consensusAddress))

        def updateSigningInfo(validator: Validator) = blockchainSigningInfos.Utility.insertOrUpdate(validator.consensusPublicKey)

        (for {
          validator <- validator
          _ <- updateSigningInfo(validator)
          _ <- addEvent(validator = validator, missedBlockCounter = missedBlocks, height = height, slashingParameter = slashingParameter)
        } yield ()
          ).recover {
          case baseException: BaseException => throw baseException
        }
    }

    (for {
      slashingParameter <- slashingParameter
      _ <- update(slashingParameter)
    } yield ()
      ).recover {
      case baseException: BaseException => throw baseException
    }
  } else Future()
}
