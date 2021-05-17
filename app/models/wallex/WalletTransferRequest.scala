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
    negotiationID: String,
    organizationID: String,
    traderID: String,
    onBehalfOf: String,
    receiverAccountID: String,
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
          .map(_.negotiationID) += walletTransferRequest).asTry
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
      negotiationID: String
  ): Future[Option[WalletTransferRequest]] =
    db.run(
      walletTranfserRequestTable
        .filter(_.negotiationID === negotiationID)
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

  private def findAllByOrganizationIDs(
      organizationIDs: Seq[String]
  ): Future[Seq[WalletTransferRequest]] =
    db.run(
      walletTranfserRequestTable
        .filter(_.organizationID inSet organizationIDs)
        .result
    )
  private def findAllByTraderID(
      traderID: String
  ): Future[Seq[WalletTransferRequest]] =
    db.run(
      walletTranfserRequestTable
        .filter(_.traderID === traderID)
        .result
    )

  private def findPendingRequests(
      organizationIDs: Seq[String],
      status: String
  ): Future[Seq[WalletTransferRequest]] =
    db.run(
      walletTranfserRequestTable
        .filter(_.organizationID inSet organizationIDs)
        .filter(_.status === status)
        .result
    )

  private def updateStatusById(
      negotiationID: String,
      status: String
  ): Future[Int] =
    db.run(
        walletTranfserRequestTable
          .filter(_.negotiationID === negotiationID)
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
        negotiationID,
        organizationID,
        traderID,
        onBehalfOf,
        receiverAccountID,
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

    def negotiationID = column[String]("negotiationID", O.PrimaryKey)

    def organizationID = column[String]("organizationID", O.PrimaryKey)

    def traderID = column[String]("traderID")

    def onBehalfOf = column[String]("onBehalfOf")

    def receiverAccountID = column[String]("receiverAccountID")

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
        negotiationID: String,
        organizationID: String,
        traderID: String,
        onBehalfOf: String,
        receiverAccountID: String,
        amount: Double,
        currency: String,
        purposeOfTransfer: String,
        reference: String,
        remarks: String,
        status: String
    ): Future[String] =
      add(
        WalletTransferRequest(
          negotiationID = negotiationID,
          organizationID = organizationID,
          traderID = traderID,
          onBehalfOf = onBehalfOf,
          receiverAccountID = receiverAccountID,
          amount = amount,
          currency = currency,
          purposeOfTransfer = purposeOfTransfer,
          reference = reference,
          remarks = remarks,
          status = status
        )
      )

    def insertOrUpdate(
        negotiationID: String,
        organizationID: String,
        traderID: String,
        onBehalfOf: String,
        receiverAccountID: String,
        amount: Double,
        currency: String,
        purposeOfTransfer: String,
        reference: String,
        remarks: String,
        status: String
    ): Future[Int] =
      upsert(
        WalletTransferRequest(
          negotiationID = negotiationID,
          organizationID = organizationID,
          traderID = traderID,
          onBehalfOf = onBehalfOf,
          receiverAccountID = receiverAccountID,
          amount = amount,
          currency = currency,
          purposeOfTransfer = purposeOfTransfer,
          reference = reference,
          remarks = remarks,
          status = status
        )
      )

    def tryGet(negotiationID: String): Future[WalletTransferRequest] =
      findById(negotiationID).map { detail =>
        detail.getOrElse(
          throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
        )
      }

    def get(negotiationID: String): Future[Option[WalletTransferRequest]] =
      findById(negotiationID)

    def tryGetPendingRequests(
        organizationIDs: Seq[String]
    ): Future[Seq[WalletTransferRequest]] =
      findPendingRequests(
        organizationIDs,
        constants.Status.SendWalletTransfer.ZONE_APPROVAL
      )

    def updateZoneApprovalStatus(
        negotiationID: String,
        status: String
    ): Future[Int] =
      updateStatusById(negotiationID, status)

    def getAllByTraderID(traderID: String): Future[Seq[WalletTransferRequest]] =
      findAllByTraderID(traderID)

    def getAllByOrganizationID(
        organizationID: String
    ): Future[Seq[WalletTransferRequest]] =
      findAllByOrganizationID(organizationID)

    def getAllByOrganizationIDs(
        organizationIDs: Seq[String]
    ): Future[Seq[WalletTransferRequest]] =
      findAllByOrganizationIDs(organizationIDs)
  }

}
