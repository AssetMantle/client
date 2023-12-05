package models.campaign

import com.cosmos.bank.{v1beta1 => bankTx}
import com.google.protobuf.{Any => protoBufAny}
import constants.Scheduler
import exceptions.BaseException
import models.blockchain
import models.blockchain._
import models.common.Serializable.Coin
import models.traits._
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import play.twirl.api.TwirlHelperImports.twirlJavaCollectionToScala
import queries.blockchain.BroadcastTxSync
import queries.responses.blockchain.BroadcastTxSyncResponse
import queries.responses.common.Header
import slick.jdbc.H2Profile.api._

import javax.inject.{Inject, Singleton}
import scala.concurrent.duration.{Duration, DurationInt, FiniteDuration}
import scala.concurrent.{Await, ExecutionContext, Future}

case class ClaimName(claimTxHash: String, name: String, height: Int, address: String, transferTxHash: Option[String], transferStatus: Option[Boolean], timeoutHeight: Option[Int], createdBy: Option[String] = None, createdOnMillisEpoch: Option[Long] = None, updatedBy: Option[String] = None, updatedOnMillisEpoch: Option[Long] = None) extends Logging with Entity[String] {
  def id: String = claimTxHash
}

private[campaign] object ClaimNames {
  class ClaimNameTable(tag: Tag) extends Table[ClaimName](tag, Option("campaign"), "ClaimName") with ModelTable[String] {

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
                            blockchainIdentities: blockchain.Identities,
                            utilitiesOperations: utilities.Operations,
                            utilitiesTransaction: utilities.Transaction,
                            protected val dbConfigProvider: DatabaseConfigProvider
                          )(implicit val executionContext: ExecutionContext)
  extends GenericDaoImpl[ClaimNames.ClaimNameTable, ClaimName, String]() {

  implicit val module: String = constants.Module.CAMPAIGN_CLAIM_NAME

  implicit val logger: Logger = Logger(this.getClass)

  val tableQuery = new TableQuery(tag => new ClaimNames.ClaimNameTable(tag))
  val CampaignAddress = ""
  val Wallet = utilities.Wallet.getRandomWallet
  val ReceiveAmount: BigInt = BigInt(100000000)

  object Service {

    def add(claimName: ClaimName): Future[String] = create(claimName).map(_.id)

    def add(claimNames: Seq[ClaimName]): Future[Int] = create(claimNames)

    def get(claimTxHash: String): Future[Option[ClaimName]] = getById(claimTxHash)

    def get(claimTxHashes: Seq[String]): Future[Seq[ClaimName]] = getByIds(claimTxHashes)

    def checkExistsByName(name: String): Future[Boolean] = filterAndExists(_.name === name)

    def tryGet(claimTxHash: String): Future[ClaimName] = tryGetById(claimTxHash)

    def update(claimName: ClaimName): Future[Unit] = updateById(claimName)

    def countAll: Future[Int] = countTotal()

    def getForTransfer: Future[Option[ClaimName]] = {
      val nullString: Option[String] = null
      filter(_.transferTxHash.? === nullString).map(_.headOption)
    }

    def markTransferTxStatusSuccess(txHashes: Seq[String]): Future[Int] = customUpdate(tableQuery.filter(_.transferTxHash.inSet(txHashes)).map(_.transferStatus).update(true))

    def markTransferTxStatusFailed(txHashes: Seq[String]): Future[Int] = customUpdate(tableQuery.filter(_.transferTxHash.inSet(txHashes)).map(_.transferStatus).update(false))

    def markTransferTxStatusFailed(txHash: String): Future[Int] = customUpdate(tableQuery.filter(_.transferTxHash === txHash).map(_.transferStatus).update(false))

    def markTimedOutTransferTxStatusFailed(currentHeight: Int): Future[Int] = {
      val nullBool: Option[Boolean] = null
      customUpdate(tableQuery.filter(x => x.transferStatus.? === nullBool && x.timeoutHeight <= currentHeight).map(_.transferStatus).update(false))
    }

    def checkAnyPendingTx: Future[Boolean] = filterAndExists(_.transferStatus.?.isEmpty)

    def updateTransferTxHash(claimTxHash: String, transferTxHash: String, timeoutHeight: Int): Future[Int] = customUpdate(tableQuery.filter(_.claimTxHash === claimTxHash).map(x => (x.transferTxHash, x.timeoutHeight)).update((transferTxHash, timeoutHeight)))

    def markFailedForRetry: Future[Int] = customUpdate(tableQuery.filter(!_.transferStatus).map(x => (x.transferTxHash.?, x.transferStatus.?, x.timeoutHeight.?)).update((null, null, null)))

  }

  object Utility {

    private def onTransaction(tx: Transaction): Future[Unit] = if (tx.status) {
      val sendCoinMsg = tx.getMessages
        .filter(_.getTypeUrl == schema.constants.Messages.SEND_COIN)
        .map(x => bankTx.MsgSend.parseFrom(x.getValue))
        .find(_.getToAddress == CampaignAddress)
      if (sendCoinMsg.isDefined) {
        val alreadyTaken = Service.checkExistsByName(tx.getMemo)
        val identity = blockchainIdentities.Service.get(schema.document.NameIdentity.getNameIdentityID(tx.getMemo))
        val transferredAmount = sendCoinMsg.get.getAmountList.toSeq.find(_.getDenom == "umntl").fold(BigInt(0))(x => BigInt(x.getAmount))

        def checkAndAdd(alreadyTaken: Boolean, identity: Option[Identity]) = if (!alreadyTaken && tx.getMemo != "" && transferredAmount == ReceiveAmount && identity.isDefined && identity.get.isAuthenticated(CampaignAddress)) Service.add(ClaimName(claimTxHash = tx.hash, name = tx.getMemo, height = tx.height, address = sendCoinMsg.get.getFromAddress, transferTxHash = None, transferStatus = None, timeoutHeight = None))
        else revertSendCoin(claimTxHash = tx.hash, height = tx.height, coins = sendCoinMsg.get.getAmountList.toSeq.map(x => Coin(x)), address = sendCoinMsg.get.getFromAddress)

        for {
          alreadyTaken <- alreadyTaken
          identity <- identity
          _ <- checkAndAdd(alreadyTaken, identity)
        } yield ()
      } else Future()
    } else Future()

    def onNewBlock(header: Header, txs: Seq[Transaction]): Future[Unit] = {
      val markTimedOut = Service.markTimedOutTransferTxStatusFailed(header.height)

      def markSuccess = Service.markTransferTxStatusSuccess(txs.filter(_.status).map(_.hash))

      def markFailed = Service.markTransferTxStatusFailed(txs.filter(!_.status).map(_.hash))

      def process = utilitiesOperations.traverse(txs)(tx => onTransaction(tx))

      (for {
        _ <- markTimedOut
        _ <- markSuccess
        _ <- markFailed
        _ <- process
      } yield ()).recover {
        case baseException: BaseException => logger.error(baseException.failure.message)
        case exception: Exception => logger.error(exception.getLocalizedMessage)
      }
    }

    private def processFailedTx(): Future[Int] = Service.markFailedForRetry

    private def transferIdentity(): Future[Unit] = {
      val pendingTx = Service.checkAnyPendingTx
      val toTransfer = Service.getForTransfer

      def createMessage(toTransfer: ClaimName) = {
        val nameID = schema.document.NameIdentity.getNameIdentityID(toTransfer.name)
        val provision = utilities.BlockchainTransaction.getProvisionMsg(fromAddress = CampaignAddress, fromID = nameID, toAddress = toTransfer.address)
        val unprovision = utilities.BlockchainTransaction.getUnprovisionMsg(fromAddress = CampaignAddress, fromID = nameID, toAddress = CampaignAddress)
        Seq(provision, unprovision)
      }

      def getAndDoTx(pendingTx: Boolean, toTransfer: Option[ClaimName]) = if (!pendingTx && toTransfer.isDefined) {
        doTx(messages = createMessage(toTransfer.get), claimName = toTransfer.get)
      } else Future()

      for {
        pendingTx <- pendingTx
        toTransfer <- toTransfer
        _ <- getAndDoTx(pendingTx, toTransfer)
      } yield ()
    }

    private def revertSendCoin(claimTxHash: String, height: Int, coins: Seq[Coin], address: String) = {
      revertClaimNames.Service.add(RevertClaimName(claimTxHash = claimTxHash, height = height, address = address, coins = coins, returnTxHash = None, returnStatus = None, timeoutHeight = None))
    }

    private def broadcastTxAndUpdate(txRawBytes: Array[Byte]) = {

      val broadcastTx = broadcastTxSync.Service.get(txRawBytes.map("%02x".format(_)).mkString.toUpperCase)

      def update(successResponse: Option[BroadcastTxSyncResponse.Response], errorResponse: Option[BroadcastTxSyncResponse.ErrorResponse]) = {
        val log = if (errorResponse.nonEmpty) Option(errorResponse.get.error.data)
        else if (successResponse.nonEmpty && successResponse.get.result.code != 0) Option(successResponse.get.result.log)
        else None

        val updateTx = if (log.nonEmpty) Service.markTransferTxStatusFailed(utilities.Secrets.sha256HashHexString(txRawBytes)) else Future()
        for {
          _ <- updateTx
        } yield ()
      }

      for {
        (successResponse, errorResponse) <- broadcastTx
        _ <- update(successResponse, errorResponse)
      } yield ()
    }

    private def doTx(messages: Seq[protoBufAny], claimName: ClaimName) = {
      val latestHeightAccountUnconfirmedTxs = utilitiesTransaction.getLatestHeightAccountAndUnconfirmedTxs(Wallet.address)

      def checkMempoolAndAddTx(bcAccount: models.blockchain.Account, latestBlockHeight: Int, unconfirmedTxHashes: Seq[String]) = {
        val (timeoutHeight, txRawBytes, txHash) = utilitiesTransaction.getTx(latestBlockHeight = latestBlockHeight, messages = messages, gasPrice = constants.Campaign.ClaimNameGasPrice, gasLimit = constants.Campaign.ClaimNameGasLimit, bcAccount = bcAccount, ecKey = Wallet.getECKey, memo = claimName.claimTxHash)

        if (!unconfirmedTxHashes.contains(txHash)) {
          val updateTxHash = Service.updateTransferTxHash(claimTxHash = claimName.claimTxHash, transferTxHash = txHash, timeoutHeight = timeoutHeight)
          for {
            _ <- updateTxHash
            updatedTransaction <- broadcastTxAndUpdate(txRawBytes)
          } yield updatedTransaction
        } else constants.Response.TRANSACTION_ALREADY_IN_MEMPOOL.throwBaseException()
      }

      (for {
        (latestHeight, bcAccount, unconfirmedTxs) <- latestHeightAccountUnconfirmedTxs
        updatedTransaction <- checkMempoolAndAddTx(bcAccount, latestHeight, unconfirmedTxs.result.txs.map(x => utilities.Secrets.base64URLDecode(x).map("%02x".format(_)).mkString.toUpperCase))
      } yield updatedTransaction
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