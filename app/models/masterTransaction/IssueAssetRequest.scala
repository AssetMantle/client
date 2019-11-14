package models.masterTransaction

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json
import slick.jdbc.JdbcProfile
import models.common.Serializable
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

case class IssueAssetRequest(id: String, ticketID: Option[String], pegHash: Option[String], accountID: String, documentHash: Option[String], assetType: String, quantityUnit: String, assetQuantity: Int, assetPrice: Int, takerAddress: Option[String], shipmentDetails: Serializable.ShipmentDetails, physicalDocumentsHandledVia: String, paymentTerms: String, status: String, comment: Option[String])

@Singleton
class IssueAssetRequests @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) {

  private def serialize(issueAssetRequest: IssueAssetRequest): IssueAssetRequestSerialized = IssueAssetRequestSerialized(issueAssetRequest.id, issueAssetRequest.ticketID, issueAssetRequest.pegHash, issueAssetRequest.accountID, issueAssetRequest.documentHash, issueAssetRequest.assetType, issueAssetRequest.quantityUnit, issueAssetRequest.assetQuantity, issueAssetRequest.assetPrice, issueAssetRequest.takerAddress, Json.toJson(issueAssetRequest.shipmentDetails).toString, issueAssetRequest.physicalDocumentsHandledVia, issueAssetRequest.paymentTerms, issueAssetRequest.status, issueAssetRequest.comment)

  case class IssueAssetRequestSerialized(id: String, ticketID: Option[String], pegHash: Option[String], accountID: String, documentHash: Option[String], assetType: String, quantityUnit: String, assetQuantity: Int, assetPrice: Int, takerAddress: Option[String], shipmentDetails: String, physicalDocumentsHandledVia: String, paymentTerms: String, status: String, comment: Option[String]){
    def deSerialize: IssueAssetRequest = IssueAssetRequest( id, ticketID, pegHash, accountID, documentHash, assetType, quantityUnit, assetQuantity, assetPrice, takerAddress, utilities.JSON.convertJsonStringToObject[Serializable.ShipmentDetails](shipmentDetails), physicalDocumentsHandledVia, paymentTerms, status, comment)
  }

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.MASTER_TRANSACTION_ISSUE_ASSET_REQUESTS

  import databaseConfig.profile.api._

  private[models] val issueAssetRequestTable = TableQuery[IssueAssetRequestTable]

  private def add(issueAssetRequest: IssueAssetRequestSerialized): Future[String] = db.run((issueAssetRequestTable returning issueAssetRequestTable.map(_.id) += issueAssetRequest).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def upsert(issueAssetRequest: IssueAssetRequestSerialized): Future[Int] = db.run(issueAssetRequestTable.insertOrUpdate(issueAssetRequest).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def findByID(id: String): Future[IssueAssetRequestSerialized] = db.run(issueAssetRequestTable.filter(_.id === id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def findByTicketID(id: String): Future[IssueAssetRequestSerialized] = db.run(issueAssetRequestTable.filter(_.ticketID === id).result.head.asTry).map {
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

  private def updateStatusAndCommentByID(id: String, status: String, comment: Option[String]) = db.run(issueAssetRequestTable.filter(_.id === id).map(issueAssetRequest => (issueAssetRequest.status, issueAssetRequest.comment.?)).update((status, comment)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateDocumentHashByID(id: String, documentHash: Option[String]) = db.run(issueAssetRequestTable.filter(_.id === id).map(_.documentHash.?).update(documentHash).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateTicketIDByID(id: String, ticketID: String) = db.run(issueAssetRequestTable.filter(_.id === id).map(_.ticketID).update(ticketID).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updatePegHashAndStatusWithTicketID(ticketID: String, status: String, pegHash: Option[String]): Future[Int] = db.run(issueAssetRequestTable.filter(_.ticketID === ticketID).map(issueAssetRequest => (issueAssetRequest.status, issueAssetRequest.pegHash.?)).update((status, pegHash)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateStatusByPegHash(pegHash: Option[String], status: String): Future[Int] = db.run(issueAssetRequestTable.filter(_.pegHash === pegHash).map(_.status).update(status).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateStatusAndAccountIDByPegHash(pegHash: Option[String], accountID: String, status: String): Future[Int] = db.run(issueAssetRequestTable.filter(_.pegHash === pegHash).map(issueAssetRequest => (issueAssetRequest.status, issueAssetRequest.accountID)).update((status, accountID)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getIssueAssetRequestsWithStatus(accountIDs: Seq[String], status: String): Future[Seq[IssueAssetRequestSerialized]] = db.run(issueAssetRequestTable.filter(_.accountID.inSet(accountIDs)).filter(_.status === status).result)

  private def getAssetsByStatuses(statuses: Seq[String]): Future[Seq[IssueAssetRequestSerialized]] = db.run(issueAssetRequestTable.filter(_.status inSet statuses).result)

  private def findIssueAssetsByAccountIDAndStatus(accountID: String, statuses: Seq[String]): Future[Seq[IssueAssetRequestSerialized]] = db.run(issueAssetRequestTable.filter(_.accountID === accountID).filter(_.status inSet statuses).result)

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

  private def verifyStatusByID(id: String, status: String): Future[Boolean] = db.run(issueAssetRequestTable.filter(_.id === id).filter(_.status === status).exists.result)

  private def getAccountIDByID(id: String): Future[String] = db.run(issueAssetRequestTable.filter(_.id === id).map(_.accountID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.info(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private[models] class IssueAssetRequestTable(tag: Tag) extends Table[IssueAssetRequestSerialized](tag, "IssueAssetRequest") {

    def * = (id, ticketID.?, pegHash.?, accountID, documentHash.?, assetType, quantityUnit, assetQuantity, assetPrice, takerAddress.?, shipmentDetails, physicalDocumentsHandledVia, paymentTerms, status, comment.?) <> (IssueAssetRequestSerialized.tupled, IssueAssetRequestSerialized.unapply)

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

    def create(id: String, ticketID: Option[String], pegHash: Option[String], accountID: String, documentHash: Option[String], assetType: String, assetPrice: Int, quantityUnit: String, assetQuantity: Int, takerAddress: Option[String], shipmentDetails: Serializable.ShipmentDetails, physicalDocumentsHandledVia: String, paymentTerms: String, status: String): String =
      Await.result(add(serialize(IssueAssetRequest(id = id, ticketID = ticketID, pegHash = pegHash, accountID = accountID, documentHash = documentHash, assetType = assetType, quantityUnit = quantityUnit, assetQuantity = assetQuantity, assetPrice = assetPrice, takerAddress = takerAddress, shipmentDetails = shipmentDetails, physicalDocumentsHandledVia = physicalDocumentsHandledVia, paymentTerms = paymentTerms, status = status, comment = null))), Duration.Inf)

    def insertOrUpdate(id: String, ticketID: Option[String], pegHash: Option[String], accountID: String, documentHash: Option[String], assetType: String, assetPrice: Int, quantityUnit: String, assetQuantity: Int, takerAddress: Option[String], shipmentDetails: Serializable.ShipmentDetails, physicalDocumentsHandledVia: String, paymentTerms: String, status: String) =
     upsert(serialize(IssueAssetRequest(id = id, ticketID = ticketID, pegHash = pegHash, accountID = accountID, documentHash = documentHash, assetType = assetType, quantityUnit = quantityUnit, assetQuantity = assetQuantity, assetPrice = assetPrice, takerAddress = takerAddress, shipmentDetails = shipmentDetails, physicalDocumentsHandledVia = physicalDocumentsHandledVia, paymentTerms = paymentTerms, status = status, comment = null)))

    def accept(id: String, ticketID: String): Int = Await.result(updateTicketIDAndStatusByID(id, ticketID, status = constants.Status.Asset.LISTED_FOR_TRADE), Duration.Inf)

    def reject(id: String, comment: String): Future[Int] = updateStatusAndCommentByID(id = id, status = constants.Status.Asset.REJECTED, comment = Option(comment))

    def updateStatusAndComment(id: String, status: String, comment: Option[String] = None) :Future[Int] =updateStatusAndCommentByID(id = id, status = status, comment = comment)

    def updateDocumentHash(id: String, documentHash: Option[String]) :Future[Int] = updateDocumentHashByID(id = id, documentHash = documentHash)

    def updateTicketID(id: String, ticketID: String) :Future[Int] = updateTicketIDByID(id = id, ticketID = ticketID)

    def markFailed(ticketID: String) :Future[Int] = updatePegHashAndStatusWithTicketID(ticketID = ticketID, pegHash = None, status = constants.Status.Asset.FAILED)

    def markListedForTrade(ticketID: String, peghash: Option[String]) :Int = Await.result(updatePegHashAndStatusWithTicketID(ticketID = ticketID, pegHash = peghash, status = constants.Status.Asset.LISTED_FOR_TRADE), Duration.Inf)

    def markRedeemed(peghash: Option[String]) :Future[Int] = updateStatusByPegHash(pegHash = peghash, status = constants.Status.Asset.REDEEMED)

    def updateAccountIDAndMarkListedForTradeByPegHash(pegHash: Option[String], accountID: String) :Future[Int] = updateStatusAndAccountIDByPegHash(pegHash = pegHash, accountID = accountID, status = constants.Status.Asset.LISTED_FOR_TRADE)

    def updateAccountIDAndMarkTradeCompleted(pegHash: Option[String], accountID: String) :Future[Int] = updateStatusAndAccountIDByPegHash(pegHash = pegHash, accountID = accountID, status = constants.Status.Asset.COMPLETED_TRADE)

    def getPendingIssueAssetRequests(accountIDs: Seq[String], status: String): Future[Seq[IssueAssetRequest]] = getIssueAssetRequestsWithStatus(accountIDs, status).map(_.map(_.deSerialize))

    def getTraderAssetList(accountID: String): Future[Seq[IssueAssetRequest]] = findIssueAssetsByAccountIDAndStatus(accountID, Seq(constants.Status.Asset.INCOMPLETE_DETAILS, constants.Status.Asset.REQUESTED, constants.Status.Asset.LISTED_FOR_TRADE, constants.Status.Asset.UNDER_NEGOTIATION,constants.Status.Asset.COMPLETED_TRADE)).map(_.map(_.deSerialize))

    def getMarketAssets(): Future[Seq[IssueAssetRequest]] =getAssetsByStatuses(Seq(constants.Status.Asset.LISTED_FOR_TRADE, constants.Status.Asset.UNDER_NEGOTIATION)).map(_.map(_.deSerialize))

    def getIssueAssetByID(id: String): Future[IssueAssetRequest] = findByID(id).map{_.deSerialize}

    def getIssueAssetByTicketID(ticketID: String)=findByTicketID(ticketID).map{_.deSerialize}

    def getAccountID(id: String): Future[String] = getAccountIDByID(id)

    def delete(id: String): Int = Await.result(deleteByID(id), Duration.Inf)

    def getStatus(id: String): String = Await.result(getStatusByID(id), Duration.Inf)

    def verifyRequestedStatus(id: String): Future[Boolean] = verifyStatusByID(id, constants.Status.Asset.REQUESTED).map{if(_)true else throw new BaseException(constants.Response.REQUEST_ALREADY_APPROVED_OR_REJECTED)}

  }

}
