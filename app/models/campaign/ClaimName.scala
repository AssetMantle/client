package models.campaign

import com.cosmos.bank.{v1beta1 => bankTx}
import com.google.protobuf.{Any => protoBufAny}
import constants.Scheduler
import exceptions.BaseException
import models.blockchain.Transaction
import models.common.Serializable.Coin
import models.traits._
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import play.twirl.api.TwirlHelperImports.twirlJavaCollectionToScala
import queries.blockchain.BroadcastTxSync
import queries.responses.blockchain.BroadcastTxSyncResponse
import slick.jdbc.H2Profile.api._

import javax.inject.{Inject, Singleton}
import scala.concurrent.duration.{Duration, DurationInt, FiniteDuration}
import scala.concurrent.{Await, ExecutionContext, Future}

case class ClaimName(claimTxHash: String, name: String, height: Int, address: String, transferTxHash: Option[String], transferStatus: Option[Boolean], timeoutHeight: Option[Int], createdBy: Option[String] = None, createdOnMillisEpoch: Option[Long] = None, updatedBy: Option[String] = None, updatedOnMillisEpoch: Option[Long] = None) extends Logging with Entity[String] {
  def id: String = claimTxHash
}

private[campaign] object ClaimNames {
  class ClaimNameTable(tag: Tag) extends Table[ClaimName](tag, "ClaimName") with ModelTable[String] {

    def * = (claimTxHash, name, height, address, transferTxHash.?, transferStatus.?, timeoutHeight.?, createdBy.?, createdOnMillisEpoch.?, updatedBy.?, updatedOnMillisEpoch.?) <> (ClaimName.tupled, ClaimName.unapply)

    def claimTxHash = column[String]("claimTxHash", O.PrimaryKey)

    def name = column[String]("name", O.Unique)

    def height = column[Int]("height")

    def address = column[String]("address")

    def transferTxHash = column[String]("transferTxHash")

    def transferStatus = column[Boolean]("transferStatus")

    def timeoutHeight = column[Int]("timeoutHeight")

    def createdBy = column[String]("createdBy")

    def createdOnMillisEpoch = column[Long]("createdOnMillisEpoch")

    def updatedBy = column[String]("updatedBy")

    def updatedOnMillisEpoch = column[Long]("updatedOnMillisEpoch")

    def id = claimTxHash
  }
}

@Singleton
class ClaimNames @Inject()(
                            broadcastTxSync: BroadcastTxSync,
                            revertClaimNames: RevertClaimNames,
                            utilitiesOperations: utilities.Operations,
                            utilitiesTransaction: utilities.Transaction,
                            protected val dbConfigProvider: DatabaseConfigProvider
                          )(implicit val executionContext: ExecutionContext)
  extends GenericDaoImpl[ClaimNames.ClaimNameTable, ClaimName, String]() {

  implicit val module: String = constants.Module.CAMPAIGN_CLAIM_NAME

  implicit val logger: Logger = Logger(this.getClass)

  val tableQuery = new TableQuery(tag => new ClaimNames.ClaimNameTable(tag))
  val CampaignAddress = ""

  object Service {

    def add(claimName: ClaimName): Future[String] = create(claimName).map(_.id)

    def add(claimNames: Seq[ClaimName]): Future[Int] = create(claimNames)

    def get(claimTxHash: String): Future[Option[ClaimName]] = getById(claimTxHash)

    def get(claimTxHashes: Seq[String]): Future[Seq[ClaimName]] = getByIds(claimTxHashes)

    def checkExistsByName(name: String): Future[Boolean] = filterAndExists(_.name === name)

    def tryGet(claimTxHash: String): Future[ClaimName] = tryGetById(claimTxHash)

    def update(claimName: ClaimName): Future[Unit] = updateById(claimName)

    def countAll: Future[Int] = countTotal()

    def getForTransfer: Future[Seq[ClaimName]] = {
      val booleanString: Option[String] = null
      filter(_.transferTxHash.? === booleanString).map(_.take(50))
    }

    def markTransferTxStatusSuccess(txHashes: Seq[String]): Future[Int] = customUpdate(tableQuery.filter(_.transferTxHash.inSet(txHashes)).map(_.transferStatus).update(true))

    def markTransferTxStatusFailed(txHashes: Seq[String]): Future[Int] = customUpdate(tableQuery.filter(_.transferTxHash.inSet(txHashes)).map(_.transferStatus).update(false))

    def markTransferTxStatusFailed(txHash: String): Future[Int] = customUpdate(tableQuery.filter(_.transferTxHash === txHash).map(_.transferStatus).update(false))

    def getWithNullStatus: Future[Seq[ClaimName]] = {
      val booleanNull: Option[Boolean] = null
      filter(_.transferStatus.? === booleanNull).map(_.take(50))
    }

    def getFailedTx: Future[Seq[ClaimName]] = filter(!_.transferStatus).map(_.take(50))

    def checkAnyPendingTx: Future[Boolean] = filterAndExists(_.transferStatus.?.isEmpty)

    def updateTransferTxHash(claimTxHashes: Seq[String], transferTxHash: String, timeoutHeight: Int): Future[Int] = customUpdate(tableQuery.filter(_.claimTxHash.inSet(claimTxHashes)).map(x => (x.transferTxHash, x.timeoutHeight)).update((transferTxHash, timeoutHeight)))

  }

  object Utility {

    private def onTransaction(tx: Transaction): Future[Unit] = if (tx.status) {
      val sendCoinMsg = tx.getMessages.filter(_.getTypeUrl == schema.constants.Messages.SEND_COIN)
        .map(x => bankTx.MsgSend.parseFrom(x.getValue))
        .find(_.getToAddress == CampaignAddress)
      if (sendCoinMsg.isDefined) {
        val transferredAmount = sendCoinMsg.get.getAmountList.toSeq.find(_.getDenom == "umntl").fold(BigInt(0))(x => BigInt(x.getAmount))
        val checkAlreadyTaken = Service.checkExistsByName(tx.getMemo)

        def checkAndAdd(checkAlreadyTaken: Boolean) = if (!checkAlreadyTaken && transferredAmount >= BigInt(1)) Service.add(ClaimName(claimTxHash = tx.hash, name = tx.getMemo, height = tx.height, address = sendCoinMsg.get.getFromAddress, transferTxHash = None, transferStatus = None))
        else revertSendCoin(claimTxHash = tx.hash, height = tx.height, coins = sendCoinMsg.get.getAmountList.toSeq.map(x => Coin(x)), address = sendCoinMsg.get.getFromAddress)

        for {
          checkAlreadyTaken <- checkAlreadyTaken
          _ <- checkAndAdd(checkAlreadyTaken)
        } yield ()
      } else Future()
    } else Future()

    def onTransactions(txs: Seq[Transaction]): Future[Unit] = {
      val process = utilitiesOperations.traverse(txs)(tx => onTransaction(tx))
      val markSuccess = Service.markTransferTxStatusSuccess(txs.filter(_.status).map(_.hash))
      val markFailed = Service.markTransferTxStatusFailed(txs.filter(!_.status).map(_.hash))

      (for {
        _ <- process
        _ <- markSuccess
        _ <- markFailed
      } yield ()).recover {
        case baseException: BaseException => logger.error(baseException.failure.message)
        case exception: Exception => logger.error(exception.getLocalizedMessage)
      }
    }

    private def processFailedTx(): Future[Unit] = {
      Future()
    }

    private def transferIdentity(): Future[Unit] = {
      val pendingTx = Service.checkAnyPendingTx

      def createMessages(toTransfer: Seq[ClaimName]) = toTransfer.flatMap(x => {
        val nameID = schema.document.NameIdentity.getNameIdentityID(x.name)
        val provision = utilities.BlockchainTransaction.getProvisionMsg(fromAddress = CampaignAddress, fromID = nameID, toAddress = x.address)
        val unprovision = utilities.BlockchainTransaction.getUnprovisionMsg(fromAddress = CampaignAddress, fromID = nameID, toAddress = CampaignAddress)
        Seq(provision, unprovision)
      })

      def getAndDoTx(pendingTx: Boolean) = if (!pendingTx) {
        val toTransfer = Service.getForTransfer
        for {
          toTransfer <- toTransfer
          _ <- doTx(messages = createMessages(toTransfer))
        } yield ()
      }

      for {
        pendingTx <- pendingTx
      } yield ()
    }

    private def revertSendCoin(claimTxHash: String, height: Int, coins: Seq[Coin], address: String) = {
      revertClaimNames.Service.add(RevertClaimName(claimTxHash = claimTxHash, height = height, address = address, coins = coins, returnTxHash = None, returnStatus = None, timeoutHeight = None))
    }

    private def broadcastTxAndUpdate(txRawBytes: Array[Byte], txHash: String) = {

      val broadcastTx = broadcastTxSync.Service.get(txRawBytes.map("%02x".format(_)).mkString.toUpperCase)

      def update(successResponse: Option[BroadcastTxSyncResponse.Response], errorResponse: Option[BroadcastTxSyncResponse.ErrorResponse]) = {
        val log = if (errorResponse.nonEmpty) Option(errorResponse.get.error.data)
        else if (successResponse.nonEmpty && successResponse.get.result.code != 0) Option(successResponse.get.result.log)
        else None

        val updateTx = if (log.nonEmpty) Service.markTransferTxStatusFailed(txHash) else Future()
        for {
          _ <- updateTx
        } yield ()
      }

      for {
        (successResponse, errorResponse) <- broadcastTx
        _ <- update(successResponse, errorResponse)
      } yield ()
    }

    private def doTx(messages: Seq[protoBufAny], claimNames: Seq[ClaimName]) = {
      val latestHeightAccountUnconfirmedTxs = utilitiesTransaction.getLatestHeightAccountAndUnconfirmedTxs(wallet.address)

      def checkMempoolAndAddTx(bcAccount: models.blockchain.Account, latestBlockHeight: Int, unconfirmedTxHashes: Seq[String]) = {
        val (timeoutHeight, txRawBytes, txHash) = utilitiesTransaction.getTx(latestBlockHeight = latestBlockHeight, messages = messages, gasPrice = 0.001, gasLimit = txUtil.gasLimit, bcAccount = bcAccount, ecKey = wallet.getECKey)

        if (!unconfirmedTxHashes.contains(txHash)) {
          val adminTx = Service.updateTransferTxHash(claimTxHashes = claimNames.map(_.claimTxHash), transferTxHash = txHash, timeoutHeight = timeoutHeight)

          def broadcastTxAndUpdate(txRawBytes: Array[Byte]) = adminTransactions.Utility.broadcastTxAndUpdate(adminTx, txRawBytes)

          for {
            adminTx <- adminTx
            masterTxValue <- addToMasterTransaction(txHash)
            updatedUserTransaction <- broadcastTxAndUpdate(adminTx, txRawBytes)
          } yield (updatedUserTransaction, masterTxValue)
        } else constants.Response.TRANSACTION_ALREADY_IN_MEMPOOL.throwBaseException()
      }

      (for {
        (latestHeight, bcAccount, unconfirmedTxs) <- latestHeightAccountUnconfirmedTxs
        pendingTx <- pendingTx
        (updatedUserTransaction, masterTxValue) <- checkMempoolAndAddTx(bcAccount, latestHeight, unconfirmedTxs.result.txs.map(x => utilities.Secrets.base64URLDecode(x).map("%02x".format(_)).mkString.toUpperCase))
      } yield (updatedUserTransaction, masterTxValue)
        ).recover {
        case exception: Exception => logger.error(exception.getLocalizedMessage)
      }
    }

    val scheduler: Scheduler = new Scheduler {
      val name: String = module
      val initialDelay: FiniteDuration = 10000.millis
      val fixedDelay: FiniteDuration = 6000.millis

      def runner(): Unit = {

        val forComplete = (for {
          _ <- processFailedTx()
          _ <- transferIdentity()
        } yield ()).recover {
          case baseException: BaseException => logger.error(baseException.failure.message)
          case exception: Exception => logger.error(exception.getLocalizedMessage)
        }
        Await.result(forComplete, Duration.Inf)
      }
    }
  }
}