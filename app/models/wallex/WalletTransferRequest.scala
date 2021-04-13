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

case class WalletTransferRequest(
    negotiationId: String,
    zoneID: String,
    organizationID: String,
    traderId: String,
    onBehalfOf: String,
    receiverAccountId: String,
    amount: Double,
    currency: String,
    purposeOfTransfer: String,
    reference: String,
    remarks: String,
    status: String,
    createdBy: Option[String] = None,
    createdOn: Option[Timestamp] = None,
    createdOnTimeZone: Option[String] = None,
    updatedBy: Option[String] = None,
    updatedOn: Option[Timestamp] = None,
    updatedOnTimeZone: Option[String] = None
) extends Logged

@Singleton
class WalletTransferRequests @Inject() (
    protected val databaseConfigProvider: DatabaseConfigProvider
)(implicit executionContext: ExecutionContext) {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String =
    constants.Module.WALLEX_WALLET_TRANSFER_REQUEST

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val walletTranfserRequestTable =
    TableQuery[WalletTransferRequestTable]

  private def add(
      walletTransferRequest: WalletTransferRequest
  ): Future[String] =
    db.run(
        (walletTranfserRequestTable returning walletTranfserRequestTable
          .map(_.negotiationId) += walletTransferRequest).asTry
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
      walletTransferRequest: WalletTransferRequest
  ): Future[Int] =
    db.run(
        walletTranfserRequestTable
          .insertOrUpdate(walletTransferRequest)
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
      negotiationId: String
  ): Future[Option[WalletTransferRequest]] =
    db.run(
      walletTranfserRequestTable
        .filter(_.negotiationId === negotiationId)
        .result
        .headOption
    )

  private def findAllByOrganizationID(
      organizationID: String
  ): Future[Seq[WalletTransferRequest]] =
    db.run(
      walletTranfserRequestTable
        .filter(_.organizationID === organizationID)
        .result
    )
  private def findAllByTraderID(
      traderID: String
  ): Future[Seq[WalletTransferRequest]] =
    db.run(
      walletTranfserRequestTable
        .filter(_.traderId === traderID)
        .result
    )

  private def findAllByZoneID(
      zoneID: String
  ): Future[Seq[WalletTransferRequest]] =
    db.run(
      walletTranfserRequestTable
        .filter(_.zoneID === zoneID)
        .result
    )

  private def findPendingByZoneID(
      zoneID: String
  ): Future[Seq[WalletTransferRequest]] =
    db.run(
      walletTranfserRequestTable
        .filter(_.zoneID === zoneID)
        .filter(_.status === constants.Status.SendWalletTransfer.ZONE_APPROVAL)
        .result
    )

  private def updateStatusById(
      negotiationId: String,
      status: String
  ): Future[Int] =
    db.run(
        walletTranfserRequestTable
          .filter(_.negotiationId === negotiationId)
          .map(_.status)
          .update(status)
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
            case noSuchElementException: NoSuchElementException =>
              throw new BaseException(
                constants.Response.NO_SUCH_ELEMENT_EXCEPTION,
                noSuchElementException
              )
          }
      }

  private[models] class WalletTransferRequestTable(tag: Tag)
      extends Table[WalletTransferRequest](
        tag,
        "WalletTransferRequest"
      ) {

    override def * =
      (
        negotiationId,
        zoneID,
        organizationID,
        traderId,
        onBehalfOf,
        receiverAccountId,
        amount,
        currency,
        purposeOfTransfer,
        reference,
        remarks,
        status,
        createdBy.?,
        createdOn.?,
        createdOnTimeZone.?,
        updatedBy.?,
        updatedOn.?,
        updatedOnTimeZone.?
      ) <> (WalletTransferRequest.tupled, WalletTransferRequest.unapply)

    def negotiationId = column[String]("negotiationId", O.PrimaryKey)

    def zoneID = column[String]("zoneID", O.PrimaryKey)

    def organizationID = column[String]("organizationID")

    def traderId = column[String]("traderId")

    def onBehalfOf = column[String]("onBehalfOf")

    def receiverAccountId = column[String]("receiverAccountId")

    def amount = column[Double]("amount")

    def currency = column[String]("currency")

    def purposeOfTransfer = column[String]("purposeOfTransfer")

    def reference = column[String]("reference")

    def remarks = column[String]("remarks")

    def status = column[String]("status")

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")
  }

  object Service {
    def create(
        negotiationId: String,
        zoneID: String,
        organizationID: String,
        traderId: String,
        onBehalfOf: String,
        receiverAccountId: String,
        amount: Double,
        currency: String,
        purposeOfTransfer: String,
        reference: String,
        remarks: String,
        status: String
    ): Future[String] =
      add(
        WalletTransferRequest(
          negotiationId = negotiationId,
          zoneID = zoneID,
          organizationID = organizationID,
          traderId = traderId,
          onBehalfOf = onBehalfOf,
          receiverAccountId = receiverAccountId,
          amount = amount,
          currency = currency,
          purposeOfTransfer = purposeOfTransfer,
          reference = reference,
          remarks = remarks,
          status = status
        )
      )

    def insertOrUpdate(
        negotiationId: String,
        zoneID: String,
        organizationID: String,
        traderId: String,
        onBehalfOf: String,
        receiverAccountId: String,
        amount: Double,
        currency: String,
        purposeOfTransfer: String,
        reference: String,
        remarks: String,
        status: String
    ): Future[Int] =
      upsert(
        WalletTransferRequest(
          negotiationId = negotiationId,
          zoneID = zoneID,
          organizationID = organizationID,
          traderId = traderId,
          onBehalfOf = onBehalfOf,
          receiverAccountId = receiverAccountId,
          amount = amount,
          currency = currency,
          purposeOfTransfer = purposeOfTransfer,
          reference = reference,
          remarks = remarks,
          status = status
        )
      )

    def tryGet(negotiationId: String): Future[WalletTransferRequest] =
      findById(negotiationId).map { detail =>
        detail.getOrElse(
          throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
        )
      }

    def get(negotiationId: String): Future[Option[WalletTransferRequest]] =
      findById(negotiationId)

    def tryGetByZoneID(
        zoneID: String
    ): Future[Seq[WalletTransferRequest]] =
      findAllByZoneID(zoneID)

    def tryGetPendingByZoneID(
        zoneID: String
    ): Future[Seq[WalletTransferRequest]] =
      findPendingByZoneID(zoneID)

    def updateZoneApprovalStatus(
        negotiationId: String,
        status: String
    ): Future[Int] =
      updateStatusById(negotiationId, status)

    def getAllByTraderID(traderID: String): Future[Seq[WalletTransferRequest]] =
      findAllByTraderID(traderID)

    def getAllByOrganizationID(organizationID: String): Future[Seq[WalletTransferRequest]] =
      findAllByOrganizationID(organizationID)
  }

}
