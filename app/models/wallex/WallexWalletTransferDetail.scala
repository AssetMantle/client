package models.wallex

import exceptions.BaseException
import models.Trait.Logged
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import java.sql.Timestamp
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class WallexWalletTransferDetail(
    id: String,
    organizationID: String,
    zoneID: String,
    wallexId: String,
    senderAccountId: String,
    receiverAccountId: String,
    amount: Double,
    currency: String,
    purposesOfTransfer: String,
    reference: String,
    remarks: String,
    status: String,
    createdAt: String,
    `type`: String,
    createdBy: Option[String] = None,
    createdOn: Option[Timestamp] = None,
    createdOnTimeZone: Option[String] = None,
    updatedBy: Option[String] = None,
    updatedOn: Option[Timestamp] = None,
    updatedOnTimeZone: Option[String] = None
) extends Logged

@Singleton
class WallexWalletTransferDetails @Inject() (
    protected val databaseConfigProvider: DatabaseConfigProvider
)(implicit executionContext: ExecutionContext) {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String =
    constants.Module.WALLEX_WALLET_TRANSFER

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val wallexWalletTransferDetailTable =
    TableQuery[WallexWalletTransferDetailTable]

  private def add(
      wallexWalletTransferDetail: WallexWalletTransferDetail
  ): Future[String] =
    db.run(
        (wallexWalletTransferDetailTable returning wallexWalletTransferDetailTable
          .map(_.id) += wallexWalletTransferDetail).asTry
      )
      .map {
        case Success(result) => result
        case Failure(exception) =>
          exception match {
            case psqlException: PSQLException =>
              throw new BaseException(
                constants.Response.PSQL_EXCEPTION,
                psqlException
              )
          }
      }

  private def upsert(
      wallexWalletTransferDetail: WallexWalletTransferDetail
  ): Future[Int] =
    db.run(
        wallexWalletTransferDetailTable
          .insertOrUpdate(wallexWalletTransferDetail)
          .asTry
      )
      .map {
        case Success(result) => result
        case Failure(exception) =>
          exception match {
            case psqlException: PSQLException =>
              throw new BaseException(
                constants.Response.PSQL_EXCEPTION,
                psqlException
              )
          }
      }

  private def findById(
      id: String
  ): Future[Option[WallexWalletTransferDetail]] =
    db.run(
      wallexWalletTransferDetailTable
        .filter(_.id === id)
        .result
        .headOption
    )

  /* private def findByStatus(
      status: String
  ): Future[Seq[Option[WallexWalletTransferDetail]]]=
    db.run(
      wallexWalletTransferDetailTable
        .filter(_.status === status)
        .result
    )
   */
  private[models] class WallexWalletTransferDetailTable(tag: Tag)
      extends Table[WallexWalletTransferDetail](
        tag,
        "WallexWalletTransfer"
      ) {

    override def * =
      (
        id,
        organizationID,
        zoneID,
        wallexId,
        senderAccountId,
        receiverAccountId,
        amount,
        currency,
        purposesOfTransfer,
        reference,
        remarks,
        status,
        createdAt,
        `type`,
        createdBy.?,
        createdOn.?,
        createdOnTimeZone.?,
        updatedBy.?,
        updatedOn.?,
        updatedOnTimeZone.?
      ) <> (WallexWalletTransferDetail.tupled, WallexWalletTransferDetail.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def organizationID = column[String]("organizationID")

    def zoneID = column[String]("zoneID")

    def wallexId = column[String]("wallexId", O.PrimaryKey)

    def senderAccountId = column[String]("senderAccountId")

    def receiverAccountId = column[String]("receiverAccountId")

    def amount = column[Double]("amount")

    def currency = column[String]("currency")

    def purposesOfTransfer = column[String]("purposesOfTransfer")

    def reference = column[String]("reference")

    def remarks = column[String]("remarks")

    def status = column[String]("status")

    def createdAt = column[String]("createdAt")

    def `type` = column[String]("type")

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")
  }

  object Service {
    def create(
        id: String,
        organizationID: String,
        zoneID: String,
        wallexId: String,
        senderAccountId: String,
        receiverAccountId: String,
        amount: Double,
        currency: String,
        purposesOfTransfer: String,
        reference: String,
        remarks: String,
        status: String,
        createdAt: String,
        `type`: String
    ): Future[String] =
      add(
        WallexWalletTransferDetail(
          id = id,
          organizationID = organizationID,
          zoneID = zoneID,
          wallexId = wallexId,
          senderAccountId = senderAccountId,
          receiverAccountId = receiverAccountId,
          amount = amount,
          currency = currency,
          purposesOfTransfer = purposesOfTransfer,
          reference = reference,
          remarks = remarks,
          status = status,
          createdAt = createdAt,
          `type` = `type`
        )
      )

    def insertOrUpdate(
        id: String,
        organizationID: String,
        zoneID: String,
        wallexId: String,
        senderAccountId: String,
        receiverAccountId: String,
        amount: Double,
        currency: String,
        purposesOfTransfer: String,
        reference: String,
        remarks: String,
        status: String,
        createdAt: String,
        `type`: String
    ): Future[Int] =
      upsert(
        WallexWalletTransferDetail(
          id = id,
          organizationID = organizationID,
          zoneID = zoneID,
          wallexId = wallexId,
          senderAccountId = senderAccountId,
          receiverAccountId = receiverAccountId,
          amount = amount,
          currency = currency,
          purposesOfTransfer = purposesOfTransfer,
          reference = reference,
          remarks = remarks,
          status = status,
          createdAt = createdAt,
          `type` = `type`
        )
      )

    def tryGet(organizationID: String): Future[WallexWalletTransferDetail] =
      findById(organizationID).map { detail =>
        detail.getOrElse(
          throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
        )
      }

    def get(organizationID: String): Future[Option[WallexWalletTransferDetail]] =
      findById(organizationID)
  }
}
