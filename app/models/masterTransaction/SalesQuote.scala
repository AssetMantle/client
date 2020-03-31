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

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class SalesQuote(id: String, accountID: String, assetType: String, assetDescription: String, assetQuantity: Int, assetPrice: Int, shippingDetails: Option[Serializable.ShippingDetails], paymentTerms: Option[Serializable.PaymentTerms], salesQuoteDocuments: Option[Serializable.SalesQuoteDocuments], buyerAccountID: Option[String], completionStatus: Boolean, invitationStatus: Option[Boolean])

@Singleton
class SalesQuotes @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) {
  val databaseConfig = databaseConfigProvider.get[JdbcProfile]
  val db = databaseConfig.db
  private[models] val salesQuoteTable = TableQuery[SalesQuoteTable]

  private def serialize(salesQuote: SalesQuote): SalesQuoteSerialized = SalesQuoteSerialized(salesQuote.id, salesQuote.accountID, salesQuote.assetType, salesQuote.assetDescription, salesQuote.assetQuantity, salesQuote.assetPrice, if (salesQuote.shippingDetails.isDefined) Some(Json.toJson(salesQuote.shippingDetails.get).toString) else None, if (salesQuote.paymentTerms.isDefined) Some(Json.toJson(salesQuote.paymentTerms.get).toString) else None, if (salesQuote.salesQuoteDocuments.isDefined) Some(Json.toJson(salesQuote.salesQuoteDocuments.get).toString) else None, salesQuote.buyerAccountID, salesQuote.completionStatus, salesQuote.invitationStatus)

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.MASTER_TRANSACTION_ISSUE_ASSET_REQUESTS

  import databaseConfig.profile.api._

  private def add(salesQuote: SalesQuoteSerialized): Future[String] = db.run((salesQuoteTable returning salesQuoteTable.map(_.id) += salesQuote).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def upsert(salesQuote: SalesQuoteSerialized): Future[Int] = db.run(salesQuoteTable.insertOrUpdate(salesQuote).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def findByID(id: String): Future[SalesQuoteSerialized] = db.run(salesQuoteTable.filter(_.id === id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getAccountIDByID(id: String): Future[String] = db.run(salesQuoteTable.filter(_.id === id).map(_.accountID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.info(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateCommodityDetailsByID(id: String, assetType: String, assetDescription: String, assetPrice: Int, assetQuantity: Int) = db.run(salesQuoteTable.filter(_.id === id).map(x => (x.assetType, x.assetPrice, x.assetQuantity)).update((assetType, assetPrice, assetQuantity)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateShippingDetailsByID(id: String, shippingDetailsSerialized: Option[String]) = db.run(salesQuoteTable.filter(_.id === id).map(x => x.shippingDetails.?).update(shippingDetailsSerialized).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updatePaymentTermsByID(id: String, paymentTermsSerialized: Option[String]) = db.run(salesQuoteTable.filter(_.id === id).map(x => x.paymentTerms.?).update(paymentTermsSerialized).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateSalesQuoteDocumentsByID(id: String, salesQuoteDocumentsSerialized: Option[String]) = db.run(salesQuoteTable.filter(_.id === id).map(x => x.salesQuoteDocuments.?).update(salesQuoteDocumentsSerialized).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateCompletionStatusByID(id: String, completionStatus: Boolean) = db.run(salesQuoteTable.filter(_.id === id).map(x => x.completionStatus).update(completionStatus).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateSalesQuoteBuyer(id: String, buyerAccountID: String) = db.run(salesQuoteTable.filter(_.id === id).map(_.buyerAccountID).update(buyerAccountID).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateInvitationStatus(id: String, invitationStatus: Option[Boolean]) = db.run(salesQuoteTable.filter(_.id === id).map(_.invitationStatus.?).update(invitationStatus).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getSalesQuoteBuyer(id: String) = db.run(salesQuoteTable.filter(_.id === id).map(_.buyerAccountID.?).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.info(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def getSalesQuotesByAccountID(accountID: String) = db.run(salesQuoteTable.filter(_.accountID === accountID).result)

  private def getSellSalesQuoteList(accountID: String, removeBool: Option[Boolean]) = db.run(salesQuoteTable.filter(_.accountID === accountID).filter(x => x.invitationStatus.?.isEmpty || x.invitationStatus.? === false).result)

  private def getBuySalesQuoteList(accountID: String) = db.run(salesQuoteTable.filter(_.buyerAccountID === accountID).filter(x => x.invitationStatus.?.isEmpty || x.invitationStatus.? === false).result)

  case class SalesQuoteSerialized(id: String, accountID: String, assetType: String, assetDescription: String, assetQuantity: Int, assetPrice: Int, shippingDetails: Option[String], paymentTerms: Option[String], salesQuoteDocuments: Option[String], buyerAccountID: Option[String], completionStatus: Boolean, invitationStatus: Option[Boolean]) {
    def deSerialize: SalesQuote = SalesQuote(id, accountID, assetType, assetDescription, assetQuantity, assetPrice, if (shippingDetails.isDefined) Option(utilities.JSON.convertJsonStringToObject[Serializable.ShippingDetails](shippingDetails.get)) else None, if (paymentTerms.isDefined) Option(utilities.JSON.convertJsonStringToObject[Serializable.PaymentTerms](paymentTerms.get)) else None, if (salesQuoteDocuments.isDefined) Option(utilities.JSON.convertJsonStringToObject[Serializable.SalesQuoteDocuments](salesQuoteDocuments.get)) else None, buyerAccountID, completionStatus, invitationStatus)
  }

  private[models] class SalesQuoteTable(tag: Tag) extends Table[SalesQuoteSerialized](tag, "SalesQuote") {

    def * = (id, accountID, assetType, assetDescription, assetQuantity, assetPrice, shippingDetails.?, paymentTerms.?, salesQuoteDocuments.?, buyerAccountID.?, completionStatus, invitationStatus.?) <> (SalesQuoteSerialized.tupled, SalesQuoteSerialized.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def accountID = column[String]("accountID")

    def assetType = column[String]("assetType")

    def assetDescription = column[String]("assetDescription")

    def assetQuantity = column[Int]("assetQuantity")

    def assetPrice = column[Int]("assetPrice")

    def shippingDetails = column[String]("shippingDetails")

    def paymentTerms = column[String]("paymentTerms")

    def salesQuoteDocuments = column[String]("documents")

    def buyerAccountID = column[String]("buyerAccountID")

    def completionStatus = column[Boolean]("completionStatus")

    def invitationStatus = column[Boolean]("invitationStatus")
  }

  object Service {

    def insertOrUpdate(id: String, accountID: String, assetType: String, assetDescription: String, assetPrice: Int, assetQuantity: Int, shippingDetails: Option[Serializable.ShippingDetails], paymentTerms: Option[Serializable.PaymentTerms], salesQuoteDocuments: Option[Serializable.SalesQuoteDocuments], buyerAccountID: Option[String], completionStatus: Boolean, invitationStatus: Option[Boolean]) = upsert(serialize(SalesQuote(id = id, accountID = accountID, assetType = assetType, assetDescription = assetDescription, assetQuantity = assetQuantity, assetPrice = assetPrice, shippingDetails = shippingDetails, paymentTerms = paymentTerms, salesQuoteDocuments = salesQuoteDocuments, buyerAccountID = buyerAccountID, completionStatus = completionStatus, invitationStatus = invitationStatus)))

    def get(id: String) = findByID(id).map(_.deSerialize)

    def updateCommodityDetails(id: String, assetType: String, assetDescription: String, assetPrice: Int, assetQuantity: Int) = updateCommodityDetailsByID(id, assetType, assetDescription, assetPrice, assetQuantity)

    def updateShippingDetails(id: String, shippingDetails: Serializable.ShippingDetails) = updateShippingDetailsByID(id, Some(Json.toJson(shippingDetails).toString))

    def updatePaymentTerms(id: String, paymentTerms: Serializable.PaymentTerms) = updatePaymentTermsByID(id, Some(Json.toJson(paymentTerms).toString))

    def updateSalesQuoteDocuments(id: String, salesQuoteDocuments: Serializable.SalesQuoteDocuments) = updateSalesQuoteDocumentsByID(id, Some(Json.toJson(salesQuoteDocuments).toString))

    def updateCompletionStatus(id: String) = updateCompletionStatusByID(id, true)

    def updateBuyer(id: String, buyerAccountID: String) = updateSalesQuoteBuyer(id, buyerAccountID)

    def getSalesQuotes(accountID: String) = getSalesQuotesByAccountID(accountID).map(_.map(_.deSerialize))

    def markAccepted(id: String) = updateInvitationStatus(id = id, invitationStatus = Some(true))

    def markRejected(id: String) = updateInvitationStatus(id = id, invitationStatus = Some(false))

    def sellSalesQuotes(accountID: String) = getSellSalesQuoteList(accountID, Some(true)).map(_.map(_.deSerialize))

    def buySalesQuotes(accountID: String) = getBuySalesQuoteList(accountID).map(_.map(_.deSerialize))

    def getBuyer(id: String) = getSalesQuoteBuyer(id)
  }

}
