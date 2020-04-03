package models.masterTransaction

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.common.Serializable
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json
import slick.jdbc.JdbcProfile
import slick.lifted.TableQuery

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

case class IssueAssetRequest(id: String, ticketID: Option[String], pegHash: Option[String], accountID: String, documentHash: Option[String], assetType: String, quantityUnit: String, assetQuantity: Int, assetPrice: Int, takerAddress: Option[String], shipmentDetails: Serializable.ShipmentDetails, physicalDocumentsHandledVia: String, paymentTerms: String, completionStatus: Boolean, verificationStatus: Option[Boolean], comment: Option[String])

@Singleton
class IssueAssetRequests @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) {
  val databaseConfig = databaseConfigProvider.get[JdbcProfile]
  val db = databaseConfig.db
  private[models] val issueAssetRequestTable = TableQuery[IssueAssetRequestTable]

  private def serialize(issueAssetRequest: IssueAssetRequest): IssueAssetRequestSerialized = IssueAssetRequestSerialized(issueAssetRequest.id, issueAssetRequest.ticketID, issueAssetRequest.pegHash, issueAssetRequest.accountID, issueAssetRequest.documentHash, issueAssetRequest.assetType, issueAssetRequest.quantityUnit, issueAssetRequest.assetQuantity, issueAssetRequest.assetPrice, issueAssetRequest.takerAddress, Json.toJson(issueAssetRequest.shipmentDetails).toString, issueAssetRequest.physicalDocumentsHandledVia, issueAssetRequest.paymentTerms, issueAssetRequest.completionStatus, issueAssetRequest.verificationStatus, issueAssetRequest.comment)

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.MASTER_TRANSACTION_ISSUE_ASSET

  import databaseConfig.profile.api._

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

  private def updateCompletionStatusByID(id: String, completionStatus: Option[Boolean]): Future[Int] = db.run(issueAssetRequestTable.filter(_.id === id).map(issueAssetRequest => (issueAssetRequest.completionStatus.?)).update((completionStatus)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateVerificationStatusByID(ticketID: String,pegHash:Option[String], verificationStatus: Option[Boolean]): Future[Int] = db.run(issueAssetRequestTable.filter(_.ticketID === ticketID).map(issueAssetRequest => (issueAssetRequest.verificationStatus.?,issueAssetRequest.pegHash?)).update((verificationStatus,pegHash)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateVerificationStatusByTicketID(ticketID: String, verificationStatus: Option[Boolean]): Future[Int] = db.run(issueAssetRequestTable.filter(_.ticketID === ticketID).map(_.verificationStatus.?).update(verificationStatus ).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateStatusAndCommentByID(id: String, verificationStatus: Option[Boolean], comment: Option[String]) = db.run(issueAssetRequestTable.filter(_.id === id).map(issueAssetRequest => (issueAssetRequest.verificationStatus.?, issueAssetRequest.comment.?)).update((verificationStatus, comment)).asTry).map {
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

  private def unapprovedAssets(accountID:String): Future[Seq[IssueAssetRequestSerialized]]=db.run(issueAssetRequestTable.filter(_.accountID === accountID).filter(issueAsset=>issueAsset.verificationStatus.?.isEmpty).result)

  private def traderAssetList(accountID:String,bool:Option[Boolean])=db.run(issueAssetRequestTable.filter(_.accountID === accountID).filter(issueAsset=>issueAsset.verificationStatus.?.isEmpty || issueAsset.verificationStatus === Option(true) ).result)

  private def getIssueAssetsByAccountIds(accountIDs: Seq[String]): Future[Seq[IssueAssetRequestSerialized]]=db.run(issueAssetRequestTable.filter(_.accountID inSet accountIDs).result)

  private def getIssueAssetsByPegHashes(pegHashes:Seq[String]): Future[Seq[IssueAssetRequestSerialized]]=db.run(issueAssetRequestTable.filter(_.pegHash inSet pegHashes).result)

  private def deleteByID(id: String) = db.run(issueAssetRequestTable.filter(_.id === id).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getAccountIDByID(id: String): Future[String] = db.run(issueAssetRequestTable.filter(_.id === id).map(_.accountID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.info(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  case class IssueAssetRequestSerialized(id: String, ticketID: Option[String], pegHash: Option[String], accountID: String, documentHash: Option[String], assetType: String, quantityUnit: String, assetQuantity: Int, assetPrice: Int, takerAddress: Option[String], shipmentDetails: String, physicalDocumentsHandledVia: String, paymentTerms: String, completionStatus: Boolean, verificationStatus:Option[Boolean], comment: Option[String]) {
    def deSerialize: IssueAssetRequest = IssueAssetRequest(id, ticketID, pegHash, accountID, documentHash, assetType, quantityUnit, assetQuantity, assetPrice, takerAddress, utilities.JSON.convertJsonStringToObject[Serializable.ShipmentDetails](shipmentDetails), physicalDocumentsHandledVia, paymentTerms, completionStatus, verificationStatus, comment)
  }

  private[models] class IssueAssetRequestTable(tag: Tag) extends Table[IssueAssetRequestSerialized](tag, "IssueAssetRequest") {

    def * = (id, ticketID.?, pegHash.?, accountID, documentHash.?, assetType, quantityUnit, assetQuantity, assetPrice, takerAddress.?, shipmentDetails, physicalDocumentsHandledVia, paymentTerms, completionStatus,verificationStatus.?, comment.?) <> (IssueAssetRequestSerialized.tupled, IssueAssetRequestSerialized.unapply)

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

    def completionStatus = column[Boolean]("completionStatus")

    def verificationStatus = column[Boolean]("verificationStatus")

    def comment = column[String]("comment")

  }

  object Service {

    def create(id: String, ticketID: Option[String], pegHash: Option[String], accountID: String, documentHash: Option[String], assetType: String, assetPrice: Int, quantityUnit: String, assetQuantity: Int, takerAddress: Option[String], shipmentDetails: Serializable.ShipmentDetails, physicalDocumentsHandledVia: String, paymentTerms: String, completionStatus: Boolean, verificationStatus: Option[Boolean]): Future[String] =
      add(serialize(IssueAssetRequest(id = id, ticketID = ticketID, pegHash = pegHash, accountID = accountID, documentHash = documentHash, assetType = assetType, quantityUnit = quantityUnit, assetQuantity = assetQuantity, assetPrice = assetPrice, takerAddress = takerAddress, shipmentDetails = shipmentDetails, physicalDocumentsHandledVia = physicalDocumentsHandledVia, paymentTerms = paymentTerms,completionStatus = completionStatus, verificationStatus = verificationStatus, comment = null)))

    def insertOrUpdate(id: String, ticketID: Option[String], pegHash: Option[String], accountID: String, documentHash: Option[String], assetType: String, assetPrice: Int, quantityUnit: String, assetQuantity: Int, takerAddress: Option[String], shipmentDetails: Serializable.ShipmentDetails, physicalDocumentsHandledVia: String, paymentTerms: String, completionStatus: Boolean, verificationStatus: Option[Boolean]) =
      upsert(serialize(IssueAssetRequest(id = id, ticketID = ticketID, pegHash = pegHash, accountID = accountID, documentHash = documentHash, assetType = assetType, quantityUnit = quantityUnit, assetQuantity = assetQuantity, assetPrice = assetPrice, takerAddress = takerAddress, shipmentDetails = shipmentDetails, physicalDocumentsHandledVia = physicalDocumentsHandledVia, paymentTerms = paymentTerms, completionStatus = completionStatus, verificationStatus = verificationStatus, comment = null)))

    def reject(id: String, comment: String): Future[Int] = updateStatusAndCommentByID(id = id, verificationStatus = Option(false), comment = Option(comment))

    def updatePegHashAndVerificationStatus(ticketID: String, pegHash:String,verificationStatus:Boolean): Future[Int]=updateVerificationStatusByID(ticketID=ticketID,pegHash=Option(pegHash), verificationStatus=Option(verificationStatus))

    def updateCompletionStatus(id: String): Future[Int]= updateCompletionStatusByID(id = id, completionStatus = Option(true))

    def updateDocumentHash(id: String, documentHash: Option[String]): Future[Int] = updateDocumentHashByID(id = id, documentHash = documentHash)

    def updateTicketID(id: String, ticketID: String): Future[Int] = updateTicketIDByID(id = id, ticketID = ticketID)

    def markFailed(ticketID: String): Future[Int]=updateVerificationStatusByTicketID(ticketID=ticketID, verificationStatus = Option(false))

    def getPendingIssueAssetRequests(accountIDs: Seq[String]): Future[Seq[IssueAssetRequest]] = getIssueAssetsByAccountIds(accountIDs).map(_.filter(_.completionStatus).filter(_.verificationStatus.isEmpty).map(_.deSerialize))

    def getTraderAssetList(accountID: String): Future[Seq[IssueAssetRequest]] =traderAssetList(accountID,Option(false)).map(_.map(_.deSerialize))

    def getUnapprovedAssets(accountID: String): Future[Seq[IssueAssetRequest]]=unapprovedAssets(accountID).map(_.map(_.deSerialize))

    def getAssetsByPegHashes(pegHashes: Seq[String]): Future[Seq[IssueAssetRequest]]= getIssueAssetsByPegHashes(pegHashes).map(_.map(_.deSerialize))

    def getIssueAssetByID(id: String): Future[IssueAssetRequest] = findByID(id).map {
      _.deSerialize
    }

    def getIssueAssetByTicketID(ticketID: String) = findByTicketID(ticketID).map {
      _.deSerialize
    }

    def getAccountID(id: String): Future[String] = getAccountIDByID(id)

    def delete(id: String): Future[Int] = deleteByID(id)

    def verifyRequestedStatus(id:String): Future[Boolean]=findByID(id).map {issueAsset=>
      if(issueAsset.completionStatus && issueAsset.verificationStatus.isEmpty) true else throw new BaseException(constants.Response.REQUEST_ALREADY_APPROVED_OR_REJECTED)
    }
  }

}
