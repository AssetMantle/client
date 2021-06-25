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

case class WalletTransfer(
    id: String,
    wallexID: String,
    senderAccountID: String,
    receiverAccountID: String,
    amount: String,
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
class WalletTransfers @Inject()(
    protected val databaseConfigProvider: DatabaseConfigProvider
)(implicit executionContext: ExecutionContext) {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String =
    constants.Module.WALLEX_WALLET_TRANSFER

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val walletTransferTable =
    TableQuery[WalletTransferTable]

  private def add(
      wallexWalletTransferDetail: WalletTransfer
  ): Future[String] =
    db.run(
        (walletTransferTable returning walletTransferTable
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
      wallexWalletTransferDetail: WalletTransfer
  ): Future[Int] =
    db.run(
        walletTransferTable
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
  ): Future[Option[WalletTransfer]] =
    db.run(
      walletTransferTable
        .filter(_.id === id)
        .result
        .headOption
    )

  private[models] class WalletTransferTable(tag: Tag)
      extends Table[WalletTransfer](
        tag,
        "WalletTransfer"
      ) {

    override def * =
      (
        id,
        wallexID,
        senderAccountID,
        receiverAccountID,
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
      ) <> (WalletTransfer.tupled, WalletTransfer.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def wallexID = column[String]("wallexID")

    def senderAccountID = column[String]("senderAccountID")

    def receiverAccountID = column[String]("receiverAccountID")

    def amount = column[String]("amount")

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
        wallexID: String,
        senderAccountID: String,
        receiverAccountID: String,
        amount: String,
        currency: String,
        purposesOfTransfer: String,
        reference: String,
        remarks: String,
        status: String,
        createdAt: String,
        `type`: String
    ): Future[String] =
      add(
        WalletTransfer(
          id = id,
          wallexID = wallexID,
          senderAccountID = senderAccountID,
          receiverAccountID = receiverAccountID,
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
        wallexID: String,
        senderAccountID: String,
        receiverAccountID: String,
        amount: String,
        currency: String,
        purposesOfTransfer: String,
        reference: String,
        remarks: String,
        status: String,
        createdAt: String,
        `type`: String
    ): Future[Int] =
      upsert(
        WalletTransfer(
          id = id,
          wallexID = wallexID,
          senderAccountID = senderAccountID,
          receiverAccountID = receiverAccountID,
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

    def tryGet(organizationID: String): Future[WalletTransfer] =
      findById(organizationID).map { detail =>
        detail.getOrElse(
          throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
        )
      }

    def get(organizationID: String): Future[Option[WalletTransfer]] =
      findById(organizationID)
  }
}
