package models.masterTransaction

import java.util.Date

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.{Json, OWrites, Reads}
import slick.jdbc.JdbcProfile

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Random, Success}

case class IssueAssetRequest(id: String, ticketID: Option[String], pegHash: Option[String], accountID: String, documentHash: String, assetType: String, quantityUnit: String, assetQuantity: Int, assetPrice: Int, takerAddress: Option[String], shipmentDetails: String, physicalDocumentsHandledVia: Option[String], paymentTerms: Option[String], status: String, comment: Option[String])

object ShipmentDetails{

  case class ShipmentDetails(deliveryTerm: String, tradeType: String, portOfLoading: String, portOfDischarge: String, shipmentDate: Date)

  implicit val oblReads: Reads[ShipmentDetails] = Json.reads[ShipmentDetails]
  implicit val oblWrites: OWrites[ShipmentDetails] = Json.writes[ShipmentDetails]
}
@Singleton
class IssueAssetRequests @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.MASTER_TRANSACTION_ISSUE_ASSET_REQUESTS

  import databaseConfig.profile.api._

  private[models] val issueAssetRequestTable = TableQuery[IssueAssetRequestTable]

  private def add(issueAssetRequest: IssueAssetRequest): Future[String] = db.run((issueAssetRequestTable returning issueAssetRequestTable.map(_.id) += issueAssetRequest).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def findByID(id: String): Future[IssueAssetRequest] = db.run(issueAssetRequestTable.filter(_.id === id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateTicketIDAndStatusByID(id: String, ticketID: String, status: String) = db.run(issueAssetRequestTable.filter(_.id === id).map(faucet => (faucet.ticketID, faucet.status)).update((ticketID, status)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateStatusAndCommentByID(id: String, status: String, comment: String) = db.run(issueAssetRequestTable.filter(_.id === id).map(issueAssetRequest => (issueAssetRequest.status, issueAssetRequest.comment)).update((status, comment)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getIssueAssetRequestsWithNullStatus(accountIDs: Seq[String]): Future[Seq[IssueAssetRequest]] = db.run(issueAssetRequestTable.filter(_.accountID.inSet(accountIDs)).filter(_.status.?.isEmpty).result)

  private def deleteByID(id: String) = db.run(issueAssetRequestTable.filter(_.id === id).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getStatusByID(id: String): Future[String] = db.run(issueAssetRequestTable.filter(_.id === id).map(_.status).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.info(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private[models] class IssueAssetRequestTable(tag: Tag) extends Table[IssueAssetRequest](tag, "IssueAssetRequest") {

    def * = (id, ticketID.?, pegHash.?, accountID, documentHash, assetType, quantityUnit, assetQuantity, assetPrice, takerAddress.?, shipmentDetails, physicalDocumentsHandledVia.?, paymentTerms.?, status, comment.?) <> (IssueAssetRequest.tupled, IssueAssetRequest.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def ticketID = column[String]("ticketID")

    def pegHash = column[String]("pegHash")

    def accountID = column[String]("accountID")

    def documentHash = column[String]("documentHash")

    def assetType = column[String]("assetType")

    def quantityUnit = column[String]("quantityUnit")

    def assetQuantity = column[Int]("assetQuantity")

    def assetPrice = column[Int]("assetPrice")

    def takerAddress = column[String]("takerAddress")

    def shipmentDetails = column[String]("shipmentDetails")

    def physicalDocumentsHandledVia = column[String]("physicalDocumentsHandledVia")

    def paymentTerms = column[String]("paymentTerms")

    def status = column[String]("status")

    def comment = column[String]("comment")

  }

  object Service {

    def create(id: String, ticketID: Option[String], pegHash: Option[String], accountID: String, documentHash: String, assetType: String, assetPrice: Int, quantityUnit: String, assetQuantity: Int, takerAddress: Option[String], deliveryTerm:String, tradeType:String, portOfLoading: String, portOfDischarge: String, shipmentDate: Date, physicalDocumentsHandledVia: Option[String], paymentTerms: Option[String], status: String): String =
      Await.result(add(IssueAssetRequest(id = id, ticketID = ticketID, pegHash = pegHash, accountID = accountID, documentHash = documentHash, assetType = assetType, quantityUnit = quantityUnit, assetQuantity = assetQuantity, assetPrice = assetPrice, takerAddress = takerAddress, shipmentDetails = Json.toJson(ShipmentDetails.ShipmentDetails(deliveryTerm,tradeType,portOfLoading,portOfDischarge,shipmentDate)).toString(), physicalDocumentsHandledVia = physicalDocumentsHandledVia, paymentTerms = paymentTerms, status = status, comment = null)), Duration.Inf)

    def accept(id: String, ticketID: String): Int = Await.result(updateTicketIDAndStatusByID(id, ticketID, status = constants.Status.Asset.LISTED_FOR_TRADE), Duration.Inf)

    def reject(id: String, comment: String): Int = Await.result(updateStatusAndCommentByID(id = id, status = constants.Status.Asset.REJECTED, comment = comment), Duration.Inf)

    def getPendingIssueAssetRequests(accountIDs: Seq[String]): Seq[IssueAssetRequest] = Await.result(getIssueAssetRequestsWithNullStatus(accountIDs), Duration.Inf)

    def delete(id: String): Int = Await.result(deleteByID(id), Duration.Inf)

    def getStatus(id: String): String = Await.result(getStatusByID(id), Duration.Inf)

  }

}
