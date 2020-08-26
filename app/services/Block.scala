package services

import actors.{Message => actorsMessage}
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.blockchain.{Validator, Transaction => blockchainTransaction}
import models.common.TransactionMessages._
import models.{blockchain, keyBase, masterTransaction}
import play.api.{Configuration, Logger}
import play.libs.Json
import queries._
import queries.responses.BlockCommitResponse.{Response => BlockCommitResponse}
import queries.responses.TransactionByHeightResponse.{Response => TransactionByHeightResponse}
import queries.responses.TransactionResponse.{Response => TransactionResponse}
import queries.responses.WSClientBlockResponse.{Response => WSClientBlockResponse}
import queries.responses.common.{Header => BlockHeader}

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

    def insertBlock(blockCommitResponse: BlockCommitResponse): Future[Int] = blockchainBlocks.Service.insertOrUpdate(height = blockCommitResponse.result.signed_header.header.height, time = blockCommitResponse.result.signed_header.header.time, proposerAddress = blockCommitResponse.result.signed_header.header.proposer_address, validators = blockCommitResponse.result.signed_header.commit.signatures.map(_.validator_address))

    (for {
      blockCommitResponse <- blockCommitResponse
      _ <- insertBlock(blockCommitResponse)
    } yield blockCommitResponse
      ).recover {
      case baseException: BaseException => throw baseException
    }
  }

  def insertTransactionsOnBlock(height: Int): Future[Seq[blockchainTransaction]] = {
    println(height)
    val transactionsByHeightResponse = getTransactionsByHeight.Service.get(height)

    def getTransactionsHash(transactionsByHeightResponse: TransactionByHeightResponse) = transactionsByHeightResponse.result.txs.map(_.hash)

    def insertTransactions(transactionsHash: Seq[String]): Future[Seq[blockchainTransaction]] = if (transactionsHash.nonEmpty) {
      val transactionResponses = Future.traverse(transactionsHash)(txHash => getTransaction.Service.get(txHash))

      def getTransactions(transactionResponses: Seq[TransactionResponse]): Future[Seq[blockchainTransaction]] = Future(transactionResponses.map(_.toTransaction))

      def insertTxs(transactions: Seq[blockchainTransaction]) = blockchainTransactions.Service.insertMultiple(transactions)

      for {
        transactionResponses <- transactionResponses
        transactions <- getTransactions(transactionResponses)
        _ <- actionsOnTransactionsMessages(transactions, height)
        _ <- insertTxs(transactions)
      } yield transactions
    } else Future(Seq.empty)

    (for {
      transactionsByHeightResponse <- transactionsByHeightResponse
      transactions <- insertTransactions(getTransactionsHash(transactionsByHeightResponse))
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

  def checksAndUpdatesOnNewBlock(newBlock: WSClientBlockResponse): Future[Unit] = {
    val undelegations = blockchainUndelegations.Utility.updateOnNewBlock(newBlock.result.data.value.block.header.time)
    val redelegations = blockchainRedelegations.Utility.updateOnNewBlock(newBlock.result.data.value.block.header.time)
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

    def getWebSocketNewBlock(proposer: String) = actorsMessage.WebSocket.NewBlock(
      block = actorsMessage.WebSocket.Block(height = blockCommitResponse.result.signed_header.header.height, time = utilities.Date.bcTimestampToString(blockCommitResponse.result.signed_header.header.time), proposer = proposer),
      txs = transactions.map(tx => actorsMessage.WebSocket.Tx(hash = tx.hash, status = tx.status, numMsgs = tx.messages.length, fees = tx.fee)),
      averageBlockTime = averageBlockTime,
      validators = blockCommitResponse.result.signed_header.commit.signatures.map(_.validator_address)
    )

    (for {
      proposer <- proposer
    } yield actors.Service.appWebSocketActor ! Json.toJson(getWebSocketNewBlock(proposer)).toString
      ).recover {
      case baseException: BaseException => throw baseException
    }
  }

  private def actionsOnTransactionsMessages(transactions: Seq[blockchainTransaction], height: Int): Future[Unit] = {
    Future(transactions.foreach { transaction =>
      if (transaction.status) {
        transaction.messages.foreach { stdMsg =>
          try {
            stdMsg.messageType match {
              //bank
              case constants.Blockchain.TransactionMessage.SEND_COIN => blockchainAccounts.Utility.onSendCoin(stdMsg.message.asInstanceOf[SendCoin])
              //slashing
              case constants.Blockchain.TransactionMessage.UNJAIL => blockchainValidators.Utility.onUnjail(transaction.hash, stdMsg.message.asInstanceOf[Unjail])
              //staking
              case constants.Blockchain.TransactionMessage.CREATE_VALIDATOR => blockchainValidators.Utility.onCreateValidator(transaction.hash, stdMsg.message.asInstanceOf[CreateValidator])
              case constants.Blockchain.TransactionMessage.EDIT_VALIDATOR => blockchainValidators.Utility.onEditValidator(transaction.hash, stdMsg.message.asInstanceOf[EditValidator])
              case constants.Blockchain.TransactionMessage.DELEGATE => blockchainValidators.Utility.onDelegation(stdMsg.message.asInstanceOf[Delegate])
              case constants.Blockchain.TransactionMessage.REDELEGATE => blockchainRedelegations.Utility.onRedelegation(stdMsg.message.asInstanceOf[Redelegate])
              case constants.Blockchain.TransactionMessage.UNDELEGATE => blockchainUndelegations.Utility.onUndelegation(stdMsg.message.asInstanceOf[Undelegate])
              case constants.Blockchain.TransactionMessage.SET_WITHDRAW_ADDRESS => blockchainWithdrawAddresses.Utility.onSetWithdrawAddress(stdMsg.message.asInstanceOf[SetWithdrawAddress])
              //Asset
              case constants.Blockchain.TransactionMessage.ASSET_MINT => blockchainAssets.Utility.onMint(stdMsg.message.asInstanceOf[AssetMint])
              case constants.Blockchain.TransactionMessage.ASSET_MUTATE => blockchainAssets.Utility.onMutate(stdMsg.message.asInstanceOf[AssetMutate])
              case constants.Blockchain.TransactionMessage.ASSET_BURN => blockchainAssets.Utility.onBurn(stdMsg.message.asInstanceOf[AssetBurn])
              //Identity
              case constants.Blockchain.TransactionMessage.IDENTITY_ISSUE => blockchainIdentities.Utility.onIssue(stdMsg.message.asInstanceOf[IdentityIssue])
              case constants.Blockchain.TransactionMessage.IDENTITY_PROVISION => blockchainIdentities.Utility.onProvision(stdMsg.message.asInstanceOf[IdentityProvision])
              case constants.Blockchain.TransactionMessage.IDENTITY_UNPROVISION => blockchainIdentities.Utility.onUnprovision(stdMsg.message.asInstanceOf[IdentityUnprovision])
              //Split
              case constants.Blockchain.TransactionMessage.SPLIT_SEND => blockchainSplits.Utility.onSend(stdMsg.message.asInstanceOf[SplitSend])
              case constants.Blockchain.TransactionMessage.SPLIT_WRAP => blockchainSplits.Utility.onWrap(stdMsg.message.asInstanceOf[SplitWrap])
              case constants.Blockchain.TransactionMessage.SPLIT_UNWRAP => blockchainSplits.Utility.onUnwrap(stdMsg.message.asInstanceOf[SplitUnwrap])
              //Order
              case constants.Blockchain.TransactionMessage.ORDER_MAKE => blockchainOrders.Utility.onMake(stdMsg.message.asInstanceOf[OrderMake], height)
              case constants.Blockchain.TransactionMessage.ORDER_TAKE => blockchainOrders.Utility.onTake(stdMsg.message.asInstanceOf[OrderTake])
              case constants.Blockchain.TransactionMessage.ORDER_CANCEL => blockchainOrders.Utility.onCancel(stdMsg.message.asInstanceOf[OrderCancel])
              //classification
              case constants.Blockchain.TransactionMessage.CLASSIFICATION_DEFINE => blockchainClassifications.Utility.onDefine(stdMsg.message.asInstanceOf[ClassificationDefine])
              //meta
              case constants.Blockchain.TransactionMessage.META_REVEAL => blockchainMetas.Utility.onReveal(stdMsg.message.asInstanceOf[MetaReveal])
              //maintainer
              case constants.Blockchain.TransactionMessage.MAINTAINER_DEPUTIZE => blockchainMaintainers.Utility.onDeputize(stdMsg.message.asInstanceOf[MaintainerDeputize])
              case _ => logger.error(constants.Response.TRANSACTION_TYPE_NOT_FOUND.logMessage + ": " + stdMsg.messageType)
            }
          } catch {
            case exception: Exception => logger.error(exception.getLocalizedMessage)
          }
        }
      }
    })
  }

  def onSlashingEvent(slashAddresses: Seq[String], slashReasons: Seq[String], slashJailed: Seq[String]): Future[Unit] = {
    val hexAddresses = slashAddresses.union(slashJailed).diff(slashAddresses.intersect(slashJailed)).map(x => utilities.Bech32.convertConsensusAddressToHexAddress(x))
    val validators = blockchainValidators.Service.getAllByHexAddresses(hexAddresses)
    val slashing = blockchainTokens.Utility.onSlashing

    def updateValidatorAndDelegations(validators: Seq[Validator]) = Future.traverse(validators) { validator =>
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

    def addEvents(validators: Seq[Validator]) = Future.traverse(slashAddresses) { slashAddress =>
      val index = slashAddresses.indexOf(slashAddress)
      val validator = validators.find(x => x.hexAddress == utilities.Bech32.convertConsensusAddressToHexAddress(slashAddress)).getOrElse(throw new BaseException(constants.Response.VALIDATOR_NOT_FOUND))
      val addEvent = masterTransactionNotifications.Service.create(constants.Notification.VALIDATOR_SLASHED,
        validator.description.moniker.getOrElse(validator.operatorAddress),
        constants.Blockchain.Events.Slashing.getSlashingReason(if (index != -1) slashReasons(index) else ""))(s"'${validator.operatorAddress}'")
      (for {
        _ <- addEvent
      } yield ()
        ).recover {
        case indexOutOfBoundsException: IndexOutOfBoundsException => throw new BaseException(constants.Response.INDEX_OUT_OF_BOUND, indexOutOfBoundsException)
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
  }

  def onMissedBlockEvent(livenessAddresses: Seq[String], livenessMissedBlocksCounter: Seq[String], livenessHeights: Seq[String]): Future[Unit] = {

    def addEvent(validator: Validator, missedBlockCounter: String, height: String) = masterTransactionNotifications.Service.create(constants.Notification.VALIDATOR_MISSED_BLOCKS, validator.description.moniker.getOrElse(validator.operatorAddress), missedBlockCounter, height)(validator.operatorAddress)

    val update = Future.traverse(livenessAddresses) { consensusAddress =>
      val validator = blockchainValidators.Service.tryGetByHexAddress(utilities.Bech32.convertConsensusAddressToHexAddress(consensusAddress))
      val index = livenessAddresses.indexOf(consensusAddress)
      (for {
        validator <- validator
        _ <- addEvent(validator = validator, missedBlockCounter = livenessMissedBlocksCounter(index), height = livenessHeights(index))
      } yield ()
        ).recover {
        case indexOutOfBoundsException: IndexOutOfBoundsException => throw new BaseException(constants.Response.INDEX_OUT_OF_BOUND, indexOutOfBoundsException)
        case baseException: BaseException => throw baseException
      }
    }
    (for {
      _ <- update
    } yield ()
      ).recover {
      case baseException: BaseException => throw baseException
    }

  }
}
