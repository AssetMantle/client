package models.campaign

import constants.Scheduler
import exceptions.BaseException
import models.traits._
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.H2Profile.api._

import javax.inject.{Inject, Singleton}
import scala.concurrent.duration.{Duration, DurationInt, FiniteDuration}
import scala.concurrent.{Await, ExecutionContext, Future}

case class RevertClaimName(claimTxHash: String, fromAddress: String, returnTxHash: Option[String], returnStatus: Option[Boolean], createdBy: Option[String] = None, createdOnMillisEpoch: Option[Long] = None, updatedBy: Option[String] = None, updatedOnMillisEpoch: Option[Long] = None) extends Logging with Entity[String] {
  def id: String = claimTxHash
}

private[campaign] object RevertClaimNames {
  class RevertClaimNameTable(tag: Tag) extends Table[RevertClaimName](tag, "RevertClaimName") with ModelTable[String] {

    def * = (claimTxHash, name, fromAddress, returnTxHash.?, returnStatus.?, createdBy.?, createdOnMillisEpoch.?, updatedBy.?, updatedOnMillisEpoch.?) <> (RevertClaimName.tupled, RevertClaimName.unapply)

    def claimTxHash = column[String]("claimTxHash", O.PrimaryKey)

    def name = column[String]("name", O.Unique)

    def fromAddress = column[String]("fromAddress")

    def returnTxHash = column[String]("returnTxHash")

    def returnStatus = column[Boolean]("returnStatus")

    def createdBy = column[String]("createdBy")

    def createdOnMillisEpoch = column[Long]("createdOnMillisEpoch")

    def updatedBy = column[String]("updatedBy")

    def updatedOnMillisEpoch = column[Long]("updatedOnMillisEpoch")

    def id = claimTxHash
  }
}

@Singleton
class RevertClaimNames @Inject()(
                                  utilitiesOperations: utilities.Operations,
                                  protected val dbConfigProvider: DatabaseConfigProvider
                                )(implicit val executionContext: ExecutionContext)
  extends GenericDaoImpl[RevertClaimNames.RevertClaimNameTable, RevertClaimName, String]() {

  implicit val module: String = constants.Module.CAMPAIGN_REVERT_CLAIM_NAME

  implicit val logger: Logger = Logger(this.getClass)

  val tableQuery = new TableQuery(tag => new RevertClaimNames.RevertClaimNameTable(tag))

  object Service {

    def add(revertClaimName: RevertClaimName): Future[String] = create(revertClaimName).map(_.id)

    def add(revertClaimNames: Seq[RevertClaimName]): Future[Int] = create(revertClaimNames)

    def get(claimTxHash: String): Future[Option[RevertClaimName]] = getById(claimTxHash)

    def get(claimTxHashes: Seq[String]): Future[Seq[RevertClaimName]] = getByIds(claimTxHashes)

    def checkExistsByName(name: String): Future[Boolean] = filterAndExists(_.name === name)

    def tryGet(claimTxHash: String): Future[RevertClaimName] = tryGetById(claimTxHash)

    def update(revertClaimName: RevertClaimName): Future[Unit] = updateById(revertClaimName)

    def countAll: Future[Int] = countTotal()

    def markTransferTxStatusSuccess(txHashes: Seq[String]): Future[Int] = customUpdate(tableQuery.filter(_.returnTxHash.inSet(txHashes)).map(_.returnStatus).update(true))

    def markTransferTxStatusFailed(txHashes: Seq[String]): Future[Int] = customUpdate(tableQuery.filter(_.returnTxHash.inSet(txHashes)).map(_.returnStatus).update(false))

    def getWithNullStatus: Future[Seq[RevertClaimName]] = {
      val booleanNull: Option[Boolean] = null
      filter(_.returnStatus.? === booleanNull).map(_.take(50))
    }

    def getFailedTx: Future[Seq[RevertClaimName]] = filter(!_.returnStatus).map(_.take(50))

  }

  object Utility {

    private def processFailedTx(): Future[Unit] = {
      Future()
    }

    private def revertTx(): Future[Unit] = {
      Future()
    }

    val scheduler: Scheduler = new Scheduler {
      val name: String = module
      val initialDelay: FiniteDuration = 10000.millis
      val fixedDelay: FiniteDuration = 6000.millis

      def runner(): Unit = {

        val forComplete = (for {
          _ <- processFailedTx()
          _ <- revertTx()
        } yield ()).recover {
          case baseException: BaseException => logger.error(baseException.failure.message)
          case exception: Exception => logger.error(exception.getLocalizedMessage)
        }
        Await.result(forComplete, Duration.Inf)
      }
    }
  }
}