package models.campaign

import com.cosmos.bank.{v1beta1 => bankTx}
import constants.Scheduler
import exceptions.BaseException
import models.blockchain.Transaction
import models.traits._
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import schema.id.base._
import slick.jdbc.H2Profile.api._

import javax.inject.{Inject, Singleton}
import scala.concurrent.duration.{Duration, DurationInt, FiniteDuration}
import scala.concurrent.{Await, ExecutionContext, Future}

case class ClaimName(claimTxHash: String, name: String, fromAddress: String, transferTxHash: Option[String], transferStatus: Option[Boolean], createdBy: Option[String] = None, createdOnMillisEpoch: Option[Long] = None, updatedBy: Option[String] = None, updatedOnMillisEpoch: Option[Long] = None) extends Logging with Entity[String] {
  def id: String = claimTxHash
}

private[campaign] object ClaimNames {
  class ClaimNameTable(tag: Tag) extends Table[ClaimName](tag, "ClaimName") with ModelTable[String] {

    def * = (claimTxHash, name, fromAddress, transferTxHash.?, transferStatus.?, createdBy.?, createdOnMillisEpoch.?, updatedBy.?, updatedOnMillisEpoch.?) <> (ClaimName.tupled, ClaimName.unapply)

    def claimTxHash = column[String]("claimTxHash", O.PrimaryKey)

    def name = column[String]("name", O.Unique)

    def fromAddress = column[String]("fromAddress")

    def transferTxHash = column[String]("transferTxHash")

    def transferStatus = column[Boolean]("transferStatus")

    def createdBy = column[String]("createdBy")

    def createdOnMillisEpoch = column[Long]("createdOnMillisEpoch")

    def updatedBy = column[String]("updatedBy")

    def updatedOnMillisEpoch = column[Long]("updatedOnMillisEpoch")

    def id = claimTxHash
  }
}

@Singleton
class ClaimNames @Inject()(
                            utilitiesOperations: utilities.Operations,
                            protected val dbConfigProvider: DatabaseConfigProvider
                          )(implicit val executionContext: ExecutionContext)
  extends GenericDaoImpl[ClaimNames.ClaimNameTable, ClaimName, String]() {

  implicit val module: String = constants.Module.CAMPAIGN_CLAIM_NAME

  implicit val logger: Logger = Logger(this.getClass)

  val tableQuery = new TableQuery(tag => new ClaimNames.ClaimNameTable(tag))

  val ClaimNameModuleIdentityID: IdentityID = schema.document.ModuleIdentity.getModuleIdentityID("claimNames")

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

    def markTransferTxStatusSuccess(txHashes: Seq[String]): Future[Int] = customUpdate(tableQuery.filter(_.transferTxHash.inSet(txHashes)).map(_.transferStatus).update(true))

    def markTransferTxStatusFailed(txHashes: Seq[String]): Future[Int] = customUpdate(tableQuery.filter(_.transferTxHash.inSet(txHashes)).map(_.transferStatus).update(false))

    def getWithNullStatus: Future[Seq[ClaimName]] = {
      val booleanNull: Option[Boolean] = null
      filter(_.transferStatus.? === booleanNull).map(_.take(50))
    }

    def getFailedTx: Future[Seq[ClaimName]] = filter(!_.transferStatus).map(_.take(50))

  }

  object Utility {

    private def onTransaction(tx: Transaction): Future[Unit] = if (tx.status) {
      val sendCoinMsg = tx.getMessages.filter(_.getTypeUrl == schema.constants.Messages.SEND_COIN)
        .map(x => bankTx.MsgSend.parseFrom(x.getValue))
        .find(_.getToAddress == CampaignAddress)
      if (sendCoinMsg.isDefined) {
        val checkAlreadyTaken = Service.checkExistsByName(tx.getMemo)

        def checkAndAdd(checkAlreadyTaken: Boolean) = if (!checkAlreadyTaken) Service.add(ClaimName(claimTxHash = tx.hash, name = tx.getMemo, fromAddress = sendCoinMsg.get.getFromAddress, transferTxHash = None, transferStatus = None))
        else revertSendCoin()

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
      Future()
    }

    private def revertSendCoin(): Future[Unit] = {
      Future()
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