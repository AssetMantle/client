package models.campaign

import constants.Scheduler
import exceptions.BaseException
import models.common.Serializable.Coin
import models.traits._
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json
import slick.jdbc.H2Profile.api._

import javax.inject.{Inject, Singleton}
import scala.concurrent.duration.{Duration, DurationInt, FiniteDuration}
import scala.concurrent.{Await, ExecutionContext, Future}

case class RevertClaimName(claimTxHash: String, height: Int, address: String, coins: Seq[Coin], returnTxHash: Option[String], returnStatus: Option[Boolean], timeoutHeight: Option[Int], createdBy: Option[String] = None, createdOnMillisEpoch: Option[Long] = None, updatedBy: Option[String] = None, updatedOnMillisEpoch: Option[Long] = None) extends Logging {

  def serialize(): RevertClaimNames.RevertClaimNameSerializable = RevertClaimNames.RevertClaimNameSerializable(
    claimTxHash = this.claimTxHash,
    height = this.height,
    address = this.address,
    coins = Json.toJson(this.coins).toString(),
    returnTxHash = this.returnTxHash,
    returnStatus = this.returnStatus,
    timeoutHeight = this.timeoutHeight,
    createdBy = this.createdBy, createdOnMillisEpoch = this.createdOnMillisEpoch, updatedBy = this.updatedBy, updatedOnMillisEpoch = this.updatedOnMillisEpoch
  )
}

private[campaign] object RevertClaimNames {
  case class RevertClaimNameSerializable(claimTxHash: String, height: Int, address: String, coins: String, returnTxHash: Option[String], returnStatus: Option[Boolean], timeoutHeight: Option[Int], createdBy: Option[String] = None, createdOnMillisEpoch: Option[Long] = None, updatedBy: Option[String] = None, updatedOnMillisEpoch: Option[Long] = None) extends Logging with Entity[String] {
    def id: String = claimTxHash

    def deserialize()(implicit module: String, logger: Logger): RevertClaimName = RevertClaimName(
      claimTxHash = this.claimTxHash,
      height = this.height,
      address = this.address,
      coins = utilities.JSON.convertJsonStringToObject[Seq[Coin]](this.coins),
      returnTxHash = this.returnTxHash,
      returnStatus = this.returnStatus,
      timeoutHeight = this.timeoutHeight,
      createdBy = this.createdBy, createdOnMillisEpoch = this.createdOnMillisEpoch, updatedBy = this.updatedBy, updatedOnMillisEpoch = this.updatedOnMillisEpoch
    )
  }

  class RevertClaimNameTable(tag: Tag) extends Table[RevertClaimNameSerializable](tag, "RevertClaimName") with ModelTable[String] {

    def * = (claimTxHash, height, address, coins, returnTxHash.?, returnStatus.?, timeoutHeight.?, createdBy.?, createdOnMillisEpoch.?, updatedBy.?, updatedOnMillisEpoch.?) <> (RevertClaimNameSerializable.tupled, RevertClaimNameSerializable.unapply)

    def claimTxHash = column[String]("claimTxHash", O.PrimaryKey)

    def height = column[Int]("height")

    def address = column[String]("address")

    def coins = column[String]("coins")

    def returnTxHash = column[String]("returnTxHash")

    def returnStatus = column[Boolean]("returnStatus")

    def timeoutHeight = column[Int]("timeoutHeight")

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
  extends GenericDaoImpl[RevertClaimNames.RevertClaimNameTable, RevertClaimNames.RevertClaimNameSerializable, String]() {

  implicit val module: String = constants.Module.CAMPAIGN_REVERT_CLAIM_NAME

  implicit val logger: Logger = Logger(this.getClass)

  val tableQuery = new TableQuery(tag => new RevertClaimNames.RevertClaimNameTable(tag))

  object Service {

    def add(revertClaimName: RevertClaimName): Future[String] = create(revertClaimName.serialize()).map(_.id)

    def add(revertClaimNames: Seq[RevertClaimName]): Future[Int] = create(revertClaimNames.map(_.serialize()))

    def get(claimTxHash: String): Future[Option[RevertClaimName]] = getById(claimTxHash).map(_.map(_.deserialize()))

    def get(claimTxHashes: Seq[String]): Future[Seq[RevertClaimName]] = getByIds(claimTxHashes).map(_.map(_.deserialize()))

    def tryGet(claimTxHash: String): Future[RevertClaimName] = tryGetById(claimTxHash).map(_.deserialize())

    def update(revertClaimName: RevertClaimName): Future[Unit] = updateById(revertClaimName.serialize())

    def countAll: Future[Int] = countTotal()

    def markTransferTxStatusSuccess(txHashes: Seq[String]): Future[Int] = customUpdate(tableQuery.filter(_.returnTxHash.inSet(txHashes)).map(_.returnStatus).update(true))

    def markTransferTxStatusFailed(txHashes: Seq[String]): Future[Int] = customUpdate(tableQuery.filter(_.returnTxHash.inSet(txHashes)).map(_.returnStatus).update(false))

    def getWithNullStatus: Future[Seq[RevertClaimName]] = {
      val booleanNull: Option[Boolean] = null
      filter(_.returnStatus.? === booleanNull).map(_.take(50)).map(_.map(_.deserialize()))
    }

    def getFailedTx: Future[Seq[RevertClaimName]] = filter(!_.returnStatus).map(_.take(50)).map(_.map(_.deserialize()))


    def checkAnyPendingTx: Future[Boolean] = filterAndExists(_.returnStatus.?.isEmpty)
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