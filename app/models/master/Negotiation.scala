package models.master

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.common.Serializable.{AssetOtherDetails, DocumentList, PaymentTerms}
import org.postgresql.util.PSQLException
import play.api.{Configuration, Logger}
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json
import slick.jdbc.JdbcProfile
import java.sql.Date

import play.api.libs.json.{JsValue, Json}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Negotiation(id: String, negotiationID: Option[String] = None, buyerTraderID: String, sellerTraderID: String, assetID: String, assetDescription: String, price: Int, quantity: Int, quantityUnit: String, assetOtherDetails: AssetOtherDetails, buyerAcceptedAssetDescription: Boolean = false, buyerAcceptedPrice: Boolean = false, buyerAcceptedQuantity: Boolean = false, buyerAcceptedAssetOtherDetails: Boolean = false, time: Option[Int] = None, paymentTerms: PaymentTerms, buyerAcceptedPaymentTerms: Boolean = false, documentList: DocumentList, buyerAcceptedDocumentList: Boolean = false, chatID: Option[String] = None, status: String, comment: Option[String] = None)

@Singleton
class Negotiations @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.MASTER_NEGOTIATION

  import databaseConfig.profile.api._

  private[models] val negotiationTable = TableQuery[NegotiationTable]

  case class NegotiationSerializable(id: String, negotiationID: Option[String], buyerTraderID: String, sellerTraderID: String, assetID: String, assetDescription: String, price: Int, quantity: Int, quantityUnit: String, assetOtherDetails: String, buyerAcceptedAssetDescription: Boolean, buyerAcceptedPrice: Boolean, buyerAcceptedQuantity: Boolean, buyerAcceptedAssetOtherDetails: Boolean, time: Option[Int], paymentTerms: String, buyerAcceptedPaymentTerms: Boolean, documentList: String, buyerAcceptedDocumentList: Boolean, chatID: Option[String], status: String, comment: Option[String]) {
    def deserialize: Negotiation = Negotiation(id = id, negotiationID = negotiationID, buyerTraderID = buyerTraderID, sellerTraderID = sellerTraderID, assetID = assetID, assetDescription = assetDescription, price = price, quantity = quantity, quantityUnit = quantityUnit, assetOtherDetails = utilities.JSON.convertJsonStringToObject[AssetOtherDetails](assetOtherDetails), buyerAcceptedAssetDescription = buyerAcceptedAssetDescription, buyerAcceptedPrice = buyerAcceptedPrice, buyerAcceptedQuantity = buyerAcceptedQuantity, buyerAcceptedAssetOtherDetails = buyerAcceptedAssetOtherDetails, time = time, paymentTerms = utilities.JSON.convertJsonStringToObject[PaymentTerms](paymentTerms), buyerAcceptedPaymentTerms = buyerAcceptedPaymentTerms, documentList = utilities.JSON.convertJsonStringToObject[DocumentList](documentList), buyerAcceptedDocumentList = buyerAcceptedDocumentList, chatID = chatID, status = status, comment = comment)
  }

  def serialize(negotiation: Negotiation): NegotiationSerializable = NegotiationSerializable(id = negotiation.id, negotiationID = negotiation.negotiationID, buyerTraderID = negotiation.buyerTraderID, sellerTraderID = negotiation.sellerTraderID, assetID = negotiation.assetID, assetDescription = negotiation.assetDescription, price = negotiation.price, quantity = negotiation.quantity, quantityUnit = negotiation.quantityUnit, assetOtherDetails = Json.toJson(negotiation.assetOtherDetails).toString(), buyerAcceptedAssetDescription = negotiation.buyerAcceptedAssetDescription, buyerAcceptedPrice = negotiation.buyerAcceptedPrice, buyerAcceptedQuantity = negotiation.buyerAcceptedQuantity, buyerAcceptedAssetOtherDetails = negotiation.buyerAcceptedAssetOtherDetails, time = negotiation.time, paymentTerms = Json.toJson(negotiation.paymentTerms).toString(), buyerAcceptedPaymentTerms = negotiation.buyerAcceptedPaymentTerms, documentList = Json.toJson(negotiation.documentList).toString(), buyerAcceptedDocumentList = negotiation.buyerAcceptedDocumentList, chatID = negotiation.chatID, status = negotiation.status, comment = negotiation.comment)

  private def add(negotiationSerializable: NegotiationSerializable): Future[String] = db.run((negotiationTable returning negotiationTable.map(_.id) += negotiationSerializable).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def find(id: String): Future[NegotiationSerializable] = db.run(negotiationTable.filter(_.id === id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def findByBuyerSellerTraderIDAndAssetID(buyerTraderID: String, sellerTraderID: String, assetID: String): Future[NegotiationSerializable] = db.run(negotiationTable.filter(_.buyerTraderID === buyerTraderID).filter(_.sellerTraderID === sellerTraderID).filter(_.assetID === assetID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def findNegotiationID(id: String): Future[String] = db.run(negotiationTable.filter(_.id === id).map(_.negotiationID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def findPaymentTermsByID(id: String): Future[String] = db.run(negotiationTable.filter(_.id === id).map(_.paymentTerms).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def findDocumentListByID(id: String): Future[String] = db.run(negotiationTable.filter(_.id === id).map(_.documentList).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateStatusByID(id: String, status: String): Future[Int] = db.run(negotiationTable.filter(_.id === id).map(_.status).update(status).asTry).map {
    case Success(result) => result match {
      case 0 => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      case _ => result
    }
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def updateStatusByNegotiationID(negotiationID: String, status: String): Future[Int] = db.run(negotiationTable.filter(_.negotiationID === negotiationID).map(_.status).update(status).asTry).map {
    case Success(result) => result match {
      case 0 => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      case _ => result
    }
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def updateAssetTermsAndBuyerAcceptedTermsByID(id: String, description: String, price: Int, quantity: Int, quantityUnit: String, buyerAcceptedAssetDescription: Boolean, buyerAcceptedPrice: Boolean, buyerAcceptedQuantity: Boolean): Future[Int] = db.run(negotiationTable.filter(_.id === id).map(x => (x.assetDescription, x.price, x.quantity, x.quantityUnit, x.buyerAcceptedAssetDescription, x.buyerAcceptedPrice, x.buyerAcceptedQuantity)).update((description, price, quantity, quantityUnit, buyerAcceptedAssetDescription, buyerAcceptedPrice, buyerAcceptedQuantity)).asTry).map {
    case Success(result) => result match {
      case 0 => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      case _ => result
    }
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def updateAssetOtherDetailsByID(id: String, assetOtherDetails: String, buyerAcceptedAssetOtherDetails: Boolean): Future[Int] = db.run(negotiationTable.filter(_.id === id).map(x => (x.assetOtherDetails, x.buyerAcceptedAssetOtherDetails)).update((assetOtherDetails, buyerAcceptedAssetOtherDetails)).asTry).map {
    case Success(result) => result match {
      case 0 => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      case _ => result
    }
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def updatePaymentTermsAndBuyerAcceptedPaymentTermsByID(id: String, paymentTerms: String, buyerAcceptedPaymentTerms: Boolean): Future[Int] = db.run(negotiationTable.filter(_.id === id).map(x => (x.paymentTerms, x.buyerAcceptedPaymentTerms)).update((paymentTerms, buyerAcceptedPaymentTerms)).asTry).map {
    case Success(result) => result match {
      case 0 => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      case _ => result
    }
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def updateDocumentListAndBuyerAcceptedDocumentListByID(id: String, documentList: String, buyerAcceptedDocumentList: Boolean): Future[Int] = db.run(negotiationTable.filter(_.id === id).map(x => (x.documentList, x.buyerAcceptedDocumentList)).update((documentList, buyerAcceptedDocumentList)).asTry).map {
    case Success(result) => result match {
      case 0 => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      case _ => result
    }
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def updateAssetDescriptionByID(id: String, assetDescription: String, buyerAcceptedAssetDescription: Boolean): Future[Int] = db.run(negotiationTable.filter(_.id === id).map(x => (x.assetDescription, x.buyerAcceptedAssetDescription)).update((assetDescription, buyerAcceptedAssetDescription)).asTry).map {
    case Success(result) => result match {
      case 0 => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      case _ => result
    }
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def updatePriceByID(id: String, price: Int, buyerAcceptedPrice: Boolean): Future[Int] = db.run(negotiationTable.filter(_.id === id).map(x => (x.price, x.buyerAcceptedPrice)).update((price, buyerAcceptedPrice)).asTry).map {
    case Success(result) => result match {
      case 0 => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      case _ => result
    }
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def updateQuantityByID(id: String, quantity: Int, buyerAcceptedQuantity: Boolean): Future[Int] = db.run(negotiationTable.filter(_.id === id).map(x => (x.quantity, x.buyerAcceptedQuantity)).update((quantity, buyerAcceptedQuantity)).asTry).map {
    case Success(result) => result match {
      case 0 => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      case _ => result
    }
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def updateStatusAndCommentByID(id: String, status: String, comment: Option[String]): Future[Int] = db.run(negotiationTable.filter(_.id === id).map(negotiation => (negotiation.status, negotiation.comment.?)).update((status, comment)).asTry).map {
    case Success(result) => result match {
      case 0 => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      case _ => result
    }
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def updateChatIDByID(id: String, chatID: String): Future[Int] = db.run(negotiationTable.filter(_.id === id).map(_.chatID).update(chatID).asTry).map {
    case Success(result) => result match {
      case 0 => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      case _ => result
    }
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def updateTimeByID(id: String, time: Option[Int]): Future[Int] = db.run(negotiationTable.filter(_.id === id).map(_.time.?).update(time).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateNegotiationIDAndStatusByID(id: String, negotiationID: String, status: String): Future[Int] = db.run(negotiationTable.filter(_.id === id).map(x => (x.negotiationID, x.status)).update((negotiationID, status)).asTry).map {
    case Success(result) => result match {
      case 0 => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      case _ => result
    }
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def deleteByStatusAndID(id: String, status: String): Future[Int] = db.run(negotiationTable.filter(_.id === id).filter(_.status === status).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getStatusByID(id: String): Future[String] = db.run(negotiationTable.filter(_.id === id).map(_.status).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def findAllNegotiationsByTraderIDsAndStatuses(traderIDs: Seq[String], statuses: String*): Future[Seq[NegotiationSerializable]] = db.run((negotiationTable.filter(_.buyerTraderID inSet traderIDs) union negotiationTable.filter(_.sellerTraderID inSet traderIDs)).filter(_.status.inSet(statuses)).result)

  private def tryGetBuyerTraderIDByID(id: String): Future[String] = db.run(negotiationTable.filter(_.id === id).map(_.buyerTraderID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def tryGetSellerTraderIDByID(id: String): Future[String] = db.run(negotiationTable.filter(_.id === id).map(_.sellerTraderID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def findAllNegotiationsByBuyerTraderIDAndStatuses(traderID: String, statuses: String*): Future[Seq[NegotiationSerializable]] = db.run(negotiationTable.filter(_.buyerTraderID === traderID).filter(_.status.inSet(statuses)).result)

  private def findAllNegotiationsBySellerTraderIDAndStatuses(traderID: String, statuses: String*): Future[Seq[NegotiationSerializable]] = db.run(negotiationTable.filter(_.sellerTraderID === traderID).filter(_.status.inSet(statuses)).result)

  private def findAllNegotiationsByTraderIDAndStatus(traderID: String, status: String): Future[Seq[NegotiationSerializable]] = db.run(negotiationTable.filter(x => x.sellerTraderID === traderID || x.buyerTraderID === traderID).filter(_.status === status).result)

  private def findAllNegotiationsByBuyerTraderIDsAndStatuses(traderIDs: Seq[String], statuses: String*): Future[Seq[NegotiationSerializable]] = db.run(negotiationTable.filter(_.buyerTraderID.inSet(traderIDs)).filter(_.status.inSet(statuses)).result)

  private def findAllNegotiationsBySellerTraderIDsAndStatuses(traderIDs: Seq[String], statuses: String*): Future[Seq[NegotiationSerializable]] = db.run(negotiationTable.filter(_.sellerTraderID.inSet(traderIDs)).filter(_.status.inSet(statuses)).result)

  private def findAllByAssetID(assetID: String): Future[Seq[NegotiationSerializable]] = db.run(negotiationTable.filter(_.assetID === assetID).result)

  private def updateBuyerAcceptedAssetDescriptionByID(id: String, buyerAcceptedAssetDescription: Boolean): Future[Int] = db.run(negotiationTable.filter(_.id === id).map(_.buyerAcceptedAssetDescription).update(buyerAcceptedAssetDescription).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateBuyerAcceptedPriceByID(id: String, buyerAcceptedPrice: Boolean): Future[Int] = db.run(negotiationTable.filter(_.id === id).map(_.buyerAcceptedPrice).update(buyerAcceptedPrice).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateBuyerAcceptedQuantityByID(id: String, buyerAcceptedQuantity: Boolean): Future[Int] = db.run(negotiationTable.filter(_.id === id).map(_.buyerAcceptedQuantity).update(buyerAcceptedQuantity).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateBuyerAcceptedAssetOtherDetailsByID(id: String, buyerAcceptedAssetOtherDetails: Boolean): Future[Int] = db.run(negotiationTable.filter(_.id === id).map(_.buyerAcceptedAssetOtherDetails).update(buyerAcceptedAssetOtherDetails).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateBuyerAcceptedPaymentTermsByID(id: String, buyerAcceptedPaymentTerms: Boolean): Future[Int] = db.run(negotiationTable.filter(_.id === id).map(_.buyerAcceptedPaymentTerms).update(buyerAcceptedPaymentTerms).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateBuyerAcceptedDocumentListByID(id: String, buyerAcceptedDocumentList: Boolean): Future[Int] = db.run(negotiationTable.filter(_.id === id).map(_.buyerAcceptedDocumentList).update(buyerAcceptedDocumentList).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def checkByIDAndTraderID(id: String, traderID: String): Future[Boolean] = db.run(negotiationTable.filter(_.id === id).filter(x => x.buyerTraderID === traderID || x.sellerTraderID === traderID).exists.result)

  private[models] class NegotiationTable(tag: Tag) extends Table[NegotiationSerializable](tag, "Negotiation") {

    def * = (id, negotiationID.?, buyerTraderID, sellerTraderID, assetID, assetDescription, price, quantity, quantityUnit, assetOtherDetails, buyerAcceptedAssetDescription, buyerAcceptedPrice, buyerAcceptedQuantity, buyerAcceptedAssetOtherDetails, time.?, paymentTerms, buyerAcceptedPaymentTerms, documentList, buyerAcceptedDocumentList, chatID.?, status, comment.?) <> (NegotiationSerializable.tupled, NegotiationSerializable.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def negotiationID = column[String]("negotiationID")

    def buyerTraderID = column[String]("buyerTraderID")

    def sellerTraderID = column[String]("sellerTraderID")

    def assetID = column[String]("assetID")

    def assetDescription = column[String]("assetDescription")

    def price = column[Int]("price")

    def quantity = column[Int]("quantity")

    def quantityUnit = column[String]("quantityUnit")

    def assetOtherDetails = column[String]("assetOtherDetails")

    def buyerAcceptedAssetDescription = column[Boolean]("buyerAcceptedAssetDescription")

    def buyerAcceptedPrice = column[Boolean]("buyerAcceptedPrice")

    def buyerAcceptedQuantity = column[Boolean]("buyerAcceptedQuantity")

    def buyerAcceptedAssetOtherDetails = column[Boolean]("buyerAcceptedAssetOtherDetails")

    def time = column[Int]("time")

    def paymentTerms = column[String]("paymentTerms")

    def buyerAcceptedPaymentTerms = column[Boolean]("buyerAcceptedPaymentTerms")

    def documentList = column[String]("documentList")

    def buyerAcceptedDocumentList = column[Boolean]("buyerAcceptedDocumentList")

    def chatID = column[String]("chatID")

    def status = column[String]("status")

    def comment = column[String]("comment")

  }

  object Service {

    def createWithIssueAssetPending(buyerTraderID: String, sellerTraderID: String, assetID: String, description: String, price: Int, quantity: Int, quantityUnit: String, assetOtherDetails: AssetOtherDetails): Future[String] = add(serialize(Negotiation(id = utilities.IDGenerator.requestID(), buyerTraderID = buyerTraderID, sellerTraderID = sellerTraderID, assetID = assetID, assetDescription = description, price = price, quantity = quantity, quantityUnit = quantityUnit, assetOtherDetails = assetOtherDetails, paymentTerms = PaymentTerms(), documentList = DocumentList(Seq[String](), Seq[String]()), status = constants.Status.Negotiation.ISSUE_ASSET_PENDING)))

    def createWithFormIncomplete(buyerTraderID: String, sellerTraderID: String, assetID: String, description: String, price: Int, quantity: Int, quantityUnit: String, assetOtherDetails: AssetOtherDetails): Future[String] = add(serialize(Negotiation(id = utilities.IDGenerator.requestID(), buyerTraderID = buyerTraderID, sellerTraderID = sellerTraderID, assetID = assetID, assetDescription = description, price = price, quantity = quantity, quantityUnit = quantityUnit, assetOtherDetails = assetOtherDetails, paymentTerms = PaymentTerms(), documentList = DocumentList(Seq[String](), Seq[String]()), status = constants.Status.Negotiation.FORM_INCOMPLETE)))

    def tryGet(id: String): Future[Negotiation] = find(id).map(serializedNegotiation => serializedNegotiation.deserialize)

    def tryGetByBuyerSellerTraderIDAndAssetID(buyerTraderID: String, sellerTraderID: String, assetID: String): Future[Negotiation] = findByBuyerSellerTraderIDAndAssetID(buyerTraderID = buyerTraderID, sellerTraderID = sellerTraderID, assetID = assetID).map(serializedNegotiation => serializedNegotiation.deserialize)

    def tryGetNegotiationIDByID(id: String): Future[String] = findNegotiationID(id)

    def tryGetPaymentTerms(id: String): Future[PaymentTerms] = findPaymentTermsByID(id).map(paymentTerms => utilities.JSON.convertJsonStringToObject[PaymentTerms](paymentTerms))

    def updatePaymentTerms(id: String, paymentTerms: PaymentTerms): Future[Int] = updatePaymentTermsAndBuyerAcceptedPaymentTermsByID(id = id, paymentTerms = Json.toJson(paymentTerms).toString(), buyerAcceptedPaymentTerms = false)

    def tryGetDocumentList(id: String): Future[DocumentList] = findDocumentListByID(id).map(documentList => utilities.JSON.convertJsonStringToObject[DocumentList](documentList))

    def updateDocumentList(id: String, documentList: DocumentList): Future[Int] = updateDocumentListAndBuyerAcceptedDocumentListByID(id = id, documentList = Json.toJson(documentList).toString(), buyerAcceptedDocumentList = false)

    def updateAssetTerms(id: String, description: String, price: Int, quantity: Int, quantityUnit: String): Future[Int] = updateAssetTermsAndBuyerAcceptedTermsByID(id = id, description = description, price = price, quantity = quantity, quantityUnit = quantityUnit, buyerAcceptedAssetDescription = false, buyerAcceptedPrice = false, buyerAcceptedQuantity = false)

    def updateAssetOtherDetails(id: String, assetOtherDetails: AssetOtherDetails): Future[Int] = updateAssetOtherDetailsByID(id = id, assetOtherDetails = Json.toJson(assetOtherDetails).toString(), buyerAcceptedAssetOtherDetails = false)

    def tryGetStatus(id: String): Future[String] = getStatusByID(id)

    def markStatusRequestSent(id: String): Future[Int] = updateStatusByID(id = id, status = constants.Status.Negotiation.REQUEST_SENT)

    def markStatusIssueAssetRequestFailed(id: String): Future[Int] = updateStatusByID(id = id, status = constants.Status.Negotiation.ISSUE_ASSET_FAILED)

    def markStatusIssueAssetPending(id: String): Future[Int] = updateStatusByID(id = id, status = constants.Status.Negotiation.ISSUE_ASSET_PENDING)

    def markTradeCompletedByNegotiationID(negotiationID: String): Future[Int] = updateStatusByNegotiationID(negotiationID = negotiationID, status = constants.Status.Negotiation.TRADE_COMPLETED)

    def markRequestRejected(id: String, comment: Option[String]): Future[Int] = updateStatusAndCommentByID(id = id, status = constants.Status.Negotiation.REJECTED, comment = comment)

    def markAcceptedAndUpdateNegotiationID(id: String, negotiationID: String): Future[Int] = updateNegotiationIDAndStatusByID(id = id, negotiationID = negotiationID, status = constants.Status.Negotiation.NEGOTIATION_STARTED)

    def getAllByAssetID(assetID: String): Future[Seq[Negotiation]] = findAllByAssetID(assetID).map(serializedNegotiations => serializedNegotiations.map(_.deserialize))

    def getAllAcceptedNegotiationListByTraderIDs(traderIDs: Seq[String]): Future[Seq[Negotiation]] = findAllNegotiationsByTraderIDsAndStatuses(traderIDs = traderIDs, constants.Status.Negotiation.NEGOTIATION_STARTED, constants.Status.Negotiation.BUYER_ACCEPTED_ALL_NEGOTIATION_TERMS, constants.Status.Negotiation.BUYER_CONFIRMED_PRICE_SELLER_PENDING, constants.Status.Negotiation.SELLER_CONFIRMED_PRICE_BUYER_PENDING, constants.Status.Negotiation.BOTH_PARTY_CONFIRMED_PRICE).map(serializedNegotiations => serializedNegotiations.map(_.deserialize))

    def getAllAcceptedBuyNegotiationListByTraderID(traderID: String): Future[Seq[Negotiation]] = findAllNegotiationsByBuyerTraderIDAndStatuses(traderID = traderID, statuses = constants.Status.Negotiation.NEGOTIATION_STARTED, constants.Status.Negotiation.BUYER_ACCEPTED_ALL_NEGOTIATION_TERMS, constants.Status.Negotiation.BUYER_ACCEPTED_ALL_NEGOTIATION_TERMS, constants.Status.Negotiation.BUYER_CONFIRMED_PRICE_SELLER_PENDING, constants.Status.Negotiation.SELLER_CONFIRMED_PRICE_BUYER_PENDING, constants.Status.Negotiation.BOTH_PARTY_CONFIRMED_PRICE).map(serializedNegotiations => serializedNegotiations.map(_.deserialize))

    def getAllAcceptedSellNegotiationListByTraderID(traderID: String): Future[Seq[Negotiation]] = findAllNegotiationsBySellerTraderIDAndStatuses(traderID = traderID, statuses = constants.Status.Negotiation.NEGOTIATION_STARTED, constants.Status.Negotiation.BUYER_ACCEPTED_ALL_NEGOTIATION_TERMS, constants.Status.Negotiation.BUYER_ACCEPTED_ALL_NEGOTIATION_TERMS, constants.Status.Negotiation.BUYER_CONFIRMED_PRICE_SELLER_PENDING, constants.Status.Negotiation.SELLER_CONFIRMED_PRICE_BUYER_PENDING, constants.Status.Negotiation.BOTH_PARTY_CONFIRMED_PRICE).map(serializedNegotiations => serializedNegotiations.map(_.deserialize))

    def getAllRejectedNegotiationListByBuyerTraderID(traderID: String): Future[Seq[Negotiation]] = findAllNegotiationsByBuyerTraderIDAndStatuses(traderID = traderID, statuses = constants.Status.Negotiation.REJECTED).map(serializedNegotiations => serializedNegotiations.map(_.deserialize))

    def getAllRejectedNegotiationListBySellerTraderID(traderID: String): Future[Seq[Negotiation]] = findAllNegotiationsBySellerTraderIDAndStatuses(traderID = traderID, statuses = constants.Status.Negotiation.REJECTED).map(serializedNegotiations => serializedNegotiations.map(_.deserialize))

    def getAllFailedNegotiationListBySellerTraderID(traderID: String): Future[Seq[Negotiation]] = findAllNegotiationsBySellerTraderIDAndStatuses(traderID = traderID, statuses = constants.Status.Negotiation.ISSUE_ASSET_FAILED, constants.Status.Negotiation.TIMED_OUT).map(serializedNegotiations => serializedNegotiations.map(_.deserialize))

    def getAllReceivedNegotiationListByTraderID(traderID: String): Future[Seq[Negotiation]] = findAllNegotiationsByBuyerTraderIDAndStatuses(traderID = traderID, statuses = constants.Status.Negotiation.REQUEST_SENT).map(serializedNegotiations => serializedNegotiations.map(_.deserialize))

    def getAllSentNegotiationRequestListByTraderID(traderID: String): Future[Seq[Negotiation]] = findAllNegotiationsBySellerTraderIDAndStatuses(traderID = traderID, statuses = constants.Status.Negotiation.REQUEST_SENT).map(serializedNegotiations => serializedNegotiations.map(_.deserialize))

    def getAllIncompleteNegotiationListByTraderID(traderID: String): Future[Seq[Negotiation]] = findAllNegotiationsBySellerTraderIDAndStatuses(traderID = traderID, statuses = constants.Status.Negotiation.FORM_INCOMPLETE, constants.Status.Negotiation.ISSUE_ASSET_PENDING).map(serializedNegotiations => serializedNegotiations.map(_.deserialize))

    def getAllConfirmedNegotiationListByTraderID(traderID: String): Future[Seq[Negotiation]] = findAllNegotiationsByTraderIDAndStatus(traderID = traderID, status = constants.Status.Negotiation.BOTH_PARTY_CONFIRMED_PRICE).map(serializedNegotiations => serializedNegotiations.map(_.deserialize))

    def getAllTradeCompletedBuyNegotiationListByTraderID(traderID: String): Future[Seq[Negotiation]] = findAllNegotiationsByBuyerTraderIDAndStatuses(traderID = traderID, constants.Status.Negotiation.TRADE_COMPLETED).map(serializedNegotiations => serializedNegotiations.map(_.deserialize))

    def getAllTradeCompletedSellNegotiationListByTraderID(traderID: String): Future[Seq[Negotiation]] = findAllNegotiationsBySellerTraderIDAndStatuses(traderID = traderID, constants.Status.Negotiation.TRADE_COMPLETED).map(serializedNegotiations => serializedNegotiations.map(_.deserialize))

    def getAllTradeCompletedBuyNegotiationListByTraderIDs(traderIDs: Seq[String]): Future[Seq[Negotiation]] = findAllNegotiationsByBuyerTraderIDsAndStatuses(traderIDs = traderIDs, constants.Status.Negotiation.TRADE_COMPLETED).map(serializedNegotiations => serializedNegotiations.map(_.deserialize))

    def getAllTradeCompletedSellNegotiationListByTraderIDs(traderIDs: Seq[String]): Future[Seq[Negotiation]] = findAllNegotiationsBySellerTraderIDsAndStatuses(traderIDs = traderIDs, constants.Status.Negotiation.TRADE_COMPLETED).map(serializedNegotiations => serializedNegotiations.map(_.deserialize))

    def insertChatID(id: String, chatID: String): Future[Int] = updateChatIDByID(id = id, chatID = chatID)

    def updateTime(id: String, time: Int): Future[Int] = updateTimeByID(id = id, time = Option(time))

    def updateBuyerAcceptedAssetDescription(id: String, buyerAcceptedAssetDescription: Boolean): Future[Int] = updateBuyerAcceptedAssetDescriptionByID(id = id, buyerAcceptedAssetDescription = buyerAcceptedAssetDescription)

    def updateBuyerAcceptedPrice(id: String, buyerAcceptedPrice: Boolean): Future[Int] = updateBuyerAcceptedPriceByID(id = id, buyerAcceptedPrice = buyerAcceptedPrice)

    def updateBuyerAcceptedQuantity(id: String, buyerAcceptedQuantity: Boolean): Future[Int] = updateBuyerAcceptedQuantityByID(id = id, buyerAcceptedQuantity = buyerAcceptedQuantity)

    def updateBuyerAcceptedAssetOtherDetails(id: String, buyerAcceptedAssetOtherDetails: Boolean): Future[Int] = updateBuyerAcceptedAssetOtherDetailsByID(id = id, buyerAcceptedAssetOtherDetails = buyerAcceptedAssetOtherDetails)

    def updateBuyerAcceptedPaymentTerms(id: String, buyerAcceptedPaymentTerms: Boolean): Future[Int] = updateBuyerAcceptedPaymentTermsByID(id = id, buyerAcceptedPaymentTerms = buyerAcceptedPaymentTerms)

    def updateBuyerAcceptedDocumentList(id: String, buyerAcceptedDocumentList: Boolean): Future[Int] = updateBuyerAcceptedDocumentListByID(id = id, buyerAcceptedDocumentList = buyerAcceptedDocumentList)

    def checkTraderNegotiationExists(id: String, traderID: String): Future[Boolean] = checkByIDAndTraderID(id = id, traderID = traderID)

    def tryGetBuyerTraderID(id: String): Future[String] = tryGetBuyerTraderIDByID(id)

    def tryGetSellerTraderID(id: String): Future[String] = tryGetSellerTraderIDByID(id)

    def updateAssetDescription(id: String, assetDescription: String) = updateAssetDescriptionByID(id, assetDescription, false)

    def updateQuantity(id: String, quantity: Int): Future[Int] = updateQuantityByID(id, quantity, false)

    def updatePrice(id: String, price: Int): Future[Int] = updatePriceByID(id, price, false)

    def getAllAcceptedBuyNegotiationListByTraderIDs(traderIDs: Seq[String]): Future[Seq[Negotiation]] = findAllNegotiationsByBuyerTraderIDsAndStatuses(traderIDs = traderIDs, constants.Status.Negotiation.NEGOTIATION_STARTED, constants.Status.Negotiation.BUYER_ACCEPTED_ALL_NEGOTIATION_TERMS, constants.Status.Negotiation.BUYER_CONFIRMED_PRICE_SELLER_PENDING, constants.Status.Negotiation.SELLER_CONFIRMED_PRICE_BUYER_PENDING, constants.Status.Negotiation.BOTH_PARTY_CONFIRMED_PRICE).map(_.map(_.deserialize))

    def getAllAcceptedSellNegotiationListByTraderIDs(traderIDs: Seq[String]): Future[Seq[Negotiation]] = findAllNegotiationsBySellerTraderIDsAndStatuses(traderIDs = traderIDs, constants.Status.Negotiation.NEGOTIATION_STARTED, constants.Status.Negotiation.BUYER_ACCEPTED_ALL_NEGOTIATION_TERMS, constants.Status.Negotiation.BUYER_CONFIRMED_PRICE_SELLER_PENDING, constants.Status.Negotiation.SELLER_CONFIRMED_PRICE_BUYER_PENDING, constants.Status.Negotiation.BOTH_PARTY_CONFIRMED_PRICE).map(_.map(_.deserialize))

    def getAllRejectedNegotiationListByBuyerTraderIDs(traderIDs: Seq[String]): Future[Seq[Negotiation]] = findAllNegotiationsByBuyerTraderIDsAndStatuses(traderIDs = traderIDs, constants.Status.Negotiation.REJECTED).map(_.map(_.deserialize))

    def getAllRejectedNegotiationListBySellerTraderIDs(traderIDs: Seq[String]): Future[Seq[Negotiation]] = findAllNegotiationsBySellerTraderIDsAndStatuses(traderIDs = traderIDs, constants.Status.Negotiation.REJECTED).map(_.map(_.deserialize))

    def getAllFailedNegotiationListBySellerTraderIDs(traderIDs: Seq[String]): Future[Seq[Negotiation]] = findAllNegotiationsBySellerTraderIDsAndStatuses(traderIDs = traderIDs, constants.Status.Negotiation.ISSUE_ASSET_FAILED, constants.Status.Negotiation.TIMED_OUT).map(_.map(_.deserialize))

    def getAllReceivedNegotiationListByTraderIDs(traderIDs: Seq[String]): Future[Seq[Negotiation]] = findAllNegotiationsByBuyerTraderIDsAndStatuses(traderIDs = traderIDs, constants.Status.Negotiation.REQUEST_SENT).map(_.map(_.deserialize))

    def getAllSentNegotiationRequestListByTraderIDs(traderIDs: Seq[String]): Future[Seq[Negotiation]] = findAllNegotiationsBySellerTraderIDsAndStatuses(traderIDs = traderIDs, constants.Status.Negotiation.REQUEST_SENT).map(_.map(_.deserialize))

    def getAllIncompleteNegotiationListByTraderIDs(traderIDs: Seq[String]): Future[Seq[Negotiation]] = findAllNegotiationsBySellerTraderIDsAndStatuses(traderIDs = traderIDs, constants.Status.Negotiation.FORM_INCOMPLETE, constants.Status.Negotiation.ISSUE_ASSET_PENDING).map(_.map(_.deserialize))

    def markBuyerAcceptedAllNegotiationTerms(id: String) = updateStatusByID(id = id, status = constants.Status.Negotiation.BUYER_ACCEPTED_ALL_NEGOTIATION_TERMS)
  }

}

