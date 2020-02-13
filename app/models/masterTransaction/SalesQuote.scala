package models.masterTransaction

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.common.Serializable
import models.common.Serializable.{PaymentTerms, SalesQuoteDocuments, ShippingDetails}
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json
import slick.jdbc.JdbcProfile
import slick.lifted.TableQuery

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

case class SalesQuote(id: String, accountID: String, assetType: String, assetQuantity: Int, assetPrice: Int, shippingDetails: Option[Serializable.ShippingDetails], paymentTerms: Option[Serializable.PaymentTerms], salesQuoteDocuments: Option[Serializable.SalesQuoteDocuments], completionStatus: Boolean)

@Singleton
class SalesQuotes @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) {
  val databaseConfig = databaseConfigProvider.get[JdbcProfile]
  val db = databaseConfig.db
  private[models] val salesQuoteTable = TableQuery[SalesQuoteTable]

  private def serialize(salesQuote: SalesQuote): SalesQuoteSerialized = SalesQuoteSerialized(salesQuote.id, salesQuote.accountID, salesQuote.assetType, salesQuote.assetQuantity, salesQuote.assetPrice, if(salesQuote.shippingDetails.isDefined) Some(Json.toJson(salesQuote.shippingDetails.get).toString) else None,if(salesQuote.paymentTerms.isDefined) Some(Json.toJson(salesQuote.paymentTerms.get).toString) else None ,if(salesQuote.salesQuoteDocuments.isDefined) Some(Json.toJson(salesQuote.salesQuoteDocuments.get).toString) else None, salesQuote.completionStatus)

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

  private def updateCommodityDetailsByID(id:String, assetType:String, assetPrice:Int, assetQuantity:Int)=db.run(salesQuoteTable.filter(_.id === id).map(x => (x.assetType, x.assetPrice, x.assetQuantity)).update((assetType, assetPrice, assetQuantity)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateShippingDetailsByID(id:String, shippingDetailsSerialized:Option[String])=db.run(salesQuoteTable.filter(_.id === id).map(x => x.shippingDetails.?).update(shippingDetailsSerialized).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updatePaymentTermsByID(id:String, paymentTermsSerialized:Option[String])=db.run(salesQuoteTable.filter(_.id === id).map(x => x.paymentTerms.?).update(paymentTermsSerialized).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateSalesQuoteDocumentsByID(id:String, salesQuoteDocumentsSerialized:Option[String])=db.run(salesQuoteTable.filter(_.id === id).map(x => x.salesQuoteDocuments.?).update(salesQuoteDocumentsSerialized).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateCompletionStatusByID(id:String, completionStatus:Boolean)=db.run(salesQuoteTable.filter(_.id === id).map(x => x.completionStatus).update(completionStatus).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getSalesQuotesByAccountID(accountID:String)=db.run(salesQuoteTable.filter(_.accountID === accountID).result)

  case class SalesQuoteSerialized(id: String, accountID: String, assetType: String, assetQuantity: Int, assetPrice: Int, shippingDetails: Option[String], paymentTerms: Option[String], salesQuoteDocuments: Option[String], completionStatus: Boolean) {
    def deSerialize: SalesQuote = SalesQuote(id, accountID, assetType, assetQuantity, assetPrice, if(shippingDetails.isDefined) Option(utilities.JSON.convertJsonStringToObject[Serializable.ShippingDetails](shippingDetails.get)) else None, if(paymentTerms.isDefined) Option(utilities.JSON.convertJsonStringToObject[Serializable.PaymentTerms](paymentTerms.get)) else None , if(salesQuoteDocuments.isDefined) Option(utilities.JSON.convertJsonStringToObject[Serializable.SalesQuoteDocuments](salesQuoteDocuments.get)) else None ,completionStatus)
  }

  private[models] class SalesQuoteTable(tag: Tag) extends Table[SalesQuoteSerialized](tag, "SalesQuote") {

    def * = (id, accountID, assetType, assetQuantity, assetPrice, shippingDetails.?, paymentTerms.?, salesQuoteDocuments.?, completionStatus) <> (SalesQuoteSerialized.tupled, SalesQuoteSerialized.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def accountID = column[String]("accountID")

    def assetType = column[String]("assetType")

    def assetQuantity = column[Int]("assetQuantity")

    def assetPrice = column[Int]("assetPrice")

    def shippingDetails = column[String]("shippingDetails")

    def paymentTerms = column[String]("paymentTerms")

    def salesQuoteDocuments = column[String]("documents")

    def completionStatus = column[Boolean]("completionStatus")
  }

  object Service {

  /*  def create(id: String, ticketID: Option[String], pegHash: Option[String], accountID: String, documentHash: Option[String], assetType: String, assetPrice: Int, quantityUnit: String, assetQuantity: Int, takerAddress: Option[String], shippingDetails: Serializable.ShippingDetails, physicalDocumentsHandledVia: String, paymentTerms: String, completionStatus: Boolean, verificationStatus: Option[Boolean]): Future[String] =
      add(serialize(SalesQuote(id = id, ticketID = ticketID, pegHash = pegHash, accountID = accountID, documentHash = documentHash, assetType = assetType, quantityUnit = quantityUnit, assetQuantity = assetQuantity, assetPrice = assetPrice, takerAddress = takerAddress, shippingDetails = shippingDetails, physicalDocumentsHandledVia = physicalDocumentsHandledVia, paymentTerms = paymentTerms, completionStatus = completionStatus, verificationStatus = verificationStatus, comment = null)))
*/
    def insertOrUpdate(id: String, accountID: String, assetType: String, assetPrice: Int, assetQuantity: Int, shippingDetails: Option[Serializable.ShippingDetails], paymentTerms: Option[Serializable.PaymentTerms], salesQuoteDocuments: Option[Serializable.SalesQuoteDocuments], completionStatus: Boolean) = upsert(serialize(SalesQuote(id = id, accountID = accountID, assetType = assetType, assetQuantity = assetQuantity, assetPrice = assetPrice, shippingDetails = shippingDetails, paymentTerms = paymentTerms, salesQuoteDocuments = salesQuoteDocuments, completionStatus = completionStatus)))

    def get(id:String)=findByID(id).map(_.deSerialize)

    def updateCommodityDetails(id:String, assetType:String, assetPrice:Int, assetQuantity:Int)=updateCommodityDetailsByID(id,assetType,assetPrice,assetQuantity)

    def updateShippingDetails(id:String, shippingDetails: Serializable.ShippingDetails)=updateShippingDetailsByID(id,Some(Json.toJson(shippingDetails).toString))

    def updatePaymentTerms(id:String, paymentTerms: Serializable.PaymentTerms)= updatePaymentTermsByID(id, Some(Json.toJson(paymentTerms).toString))

    def updateSalesQuoteDocuments(id:String, salesQuoteDocuments: Serializable.SalesQuoteDocuments)=updateSalesQuoteDocumentsByID(id,Some(Json.toJson(salesQuoteDocuments).toString))

    def updateCompletionStatus(id:String)=updateCompletionStatusByID(id, true)

    def getSalesQuotes(accountID:String)=getSalesQuotesByAccountID(accountID).map(_.map(_.deSerialize))
  }

}
