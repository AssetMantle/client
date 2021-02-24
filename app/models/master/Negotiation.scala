package models.master

import java.sql.Timestamp

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Trait.Logged
import models.common.Serializable.{AssetOtherDetails, DocumentList, PaymentTerms}
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json
import play.api.{Configuration, Logger}
import slick.jdbc.JdbcProfile
import utilities.MicroNumber

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Negotiation(id: String, orderID: Option[String] = None, buyerTraderID: String, sellerTraderID: String, assetID: String, assetDescription: String, price: MicroNumber, quantity: MicroNumber, quantityUnit: String, buyerAcceptedAssetDescription: Boolean = false, buyerAcceptedPrice: Boolean = false, buyerAcceptedQuantity: Boolean = false, assetOtherDetails: AssetOtherDetails, buyerAcceptedAssetOtherDetails: Boolean = false, time: Option[Int] = None, paymentTerms: PaymentTerms, buyerAcceptedPaymentTerms: Boolean = false, documentList: DocumentList, buyerAcceptedDocumentList: Boolean = false, physicalDocumentsHandledVia: Option[String] = None, chatID: Option[String] = None, status: String, comment: Option[String] = None, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged

@Singleton
class Negotiations @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider, configuration: Configuration)(implicit executionContext: ExecutionContext) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.MASTER_NEGOTIATION

  import databaseConfig.profile.api._

  private[models] val negotiationTable = TableQuery[NegotiationTable]

  case class AssetAndBuyerAcceptedSerialize(assetDescription: String, price: String, quantity: String, quantityUnit: String, buyerAcceptedAssetDescription: Boolean, buyerAcceptedPrice: Boolean, buyerAcceptedQuantity: Boolean)

  case class AssetOtherDetailsAndBuyerAcceptedSerialize(assetOtherDetails: String, buyerAccepted: Boolean)

  case class PaymentTermsAndBuyerAcceptedSerialize(paymentTerms: String, buyerAccepted: Boolean)

  case class DocumentListAndBuyerAcceptedSerialize(documentList: String, buyerAccepted: Boolean)

  case class NegotiationSerializable(id: String, orderID: Option[String], buyerTraderID: String, sellerTraderID: String, assetID: String, assetAndBuyerAcceptedSerialize: AssetAndBuyerAcceptedSerialize, assetOtherDetailsAndBuyerAcceptedSerialize: AssetOtherDetailsAndBuyerAcceptedSerialize, time: Option[Int], paymentTermsAndBuyerAcceptedSerialize: PaymentTermsAndBuyerAcceptedSerialize, documentListAndBuyerAcceptedSerialize: DocumentListAndBuyerAcceptedSerialize, physicalDocumentsHandledVia: Option[String], chatID: Option[String], status: String, comment: Option[String], createdBy: Option[String], createdOn: Option[Timestamp], createdOnTimeZone: Option[String], updatedBy: Option[String], updatedOn: Option[Timestamp], updatedOnTimeZone: Option[String]) {
    def deserialize: Negotiation = Negotiation(id = id, orderID = orderID,
      buyerTraderID = buyerTraderID, sellerTraderID = sellerTraderID, assetID = assetID,
      assetDescription = assetAndBuyerAcceptedSerialize.assetDescription, price = new MicroNumber(BigInt(assetAndBuyerAcceptedSerialize.price)), quantity = new MicroNumber(BigInt(assetAndBuyerAcceptedSerialize.quantity)), quantityUnit = assetAndBuyerAcceptedSerialize.quantityUnit,
      buyerAcceptedAssetDescription = assetAndBuyerAcceptedSerialize.buyerAcceptedAssetDescription, buyerAcceptedPrice = assetAndBuyerAcceptedSerialize.buyerAcceptedPrice, buyerAcceptedQuantity = assetAndBuyerAcceptedSerialize.buyerAcceptedQuantity,
      assetOtherDetails = utilities.JSON.convertJsonStringToObject[AssetOtherDetails](assetOtherDetailsAndBuyerAcceptedSerialize.assetOtherDetails),
      buyerAcceptedAssetOtherDetails = assetOtherDetailsAndBuyerAcceptedSerialize.buyerAccepted,
      time = time,
      paymentTerms = utilities.JSON.convertJsonStringToObject[PaymentTerms](paymentTermsAndBuyerAcceptedSerialize.paymentTerms),
      buyerAcceptedPaymentTerms = paymentTermsAndBuyerAcceptedSerialize.buyerAccepted,
      documentList = utilities.JSON.convertJsonStringToObject[DocumentList](documentListAndBuyerAcceptedSerialize.documentList),
      buyerAcceptedDocumentList = documentListAndBuyerAcceptedSerialize.buyerAccepted,
      physicalDocumentsHandledVia = physicalDocumentsHandledVia, chatID = chatID, status = status, comment = comment,
      createdBy = createdBy, createdOn = createdOn, createdOnTimeZone = createdOnTimeZone,
      updatedBy = updatedBy, updatedOn = updatedOn, updatedOnTimeZone = updatedOnTimeZone)
  }

  def serialize(negotiation: Negotiation): NegotiationSerializable = NegotiationSerializable(id = negotiation.id, orderID = negotiation.orderID,
    buyerTraderID = negotiation.buyerTraderID, sellerTraderID = negotiation.sellerTraderID, assetID = negotiation.assetID,
    assetAndBuyerAcceptedSerialize = AssetAndBuyerAcceptedSerialize(assetDescription = negotiation.assetDescription, price = negotiation.price.toMicroString, quantity = negotiation.quantity.toMicroString, quantityUnit = negotiation.quantityUnit,
      buyerAcceptedAssetDescription = negotiation.buyerAcceptedAssetDescription, buyerAcceptedPrice = negotiation.buyerAcceptedPrice, buyerAcceptedQuantity = negotiation.buyerAcceptedQuantity),
    assetOtherDetailsAndBuyerAcceptedSerialize = AssetOtherDetailsAndBuyerAcceptedSerialize(assetOtherDetails = Json.toJson(negotiation.assetOtherDetails).toString(), buyerAccepted = negotiation.buyerAcceptedPaymentTerms),
    time = negotiation.time,
    paymentTermsAndBuyerAcceptedSerialize = PaymentTermsAndBuyerAcceptedSerialize(paymentTerms = Json.toJson(negotiation.paymentTerms).toString(), buyerAccepted = negotiation.buyerAcceptedPaymentTerms),
    documentListAndBuyerAcceptedSerialize = DocumentListAndBuyerAcceptedSerialize(documentList = Json.toJson(negotiation.documentList).toString(), buyerAccepted = negotiation.buyerAcceptedDocumentList),
    physicalDocumentsHandledVia = negotiation.physicalDocumentsHandledVia, chatID = negotiation.chatID, status = negotiation.status, comment = negotiation.comment,
    createdBy = negotiation.createdBy, createdOn = negotiation.createdOn, createdOnTimeZone = negotiation.createdOnTimeZone,
    updatedBy = negotiation.updatedBy, updatedOn = negotiation.updatedOn, updatedOnTimeZone = negotiation.updatedOnTimeZone)

  private def add(negotiation: Negotiation): Future[String] = db.run((negotiationTable returning negotiationTable.map(_.id) += serialize(negotiation)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def updateByID(negotiation: Negotiation): Future[Int] = db.run(negotiationTable.filter(_.id === negotiation.id).update(serialize(negotiation)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def getByID(id: String): Future[Option[NegotiationSerializable]] = db.run(negotiationTable.filter(_.id === id).result.headOption)

  private def tryGetByID(id: String): Future[NegotiationSerializable] = db.run(negotiationTable.filter(_.id === id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def tryGetByOrderID(orderID: String): Future[NegotiationSerializable] = db.run(negotiationTable.filter(_.orderID === orderID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def findByBuyerSellerTraderIDAndAssetID(buyerTraderID: String, sellerTraderID: String, assetID: String): Future[NegotiationSerializable] = db.run(negotiationTable.filter(_.buyerTraderID === buyerTraderID).filter(_.sellerTraderID === sellerTraderID).filter(_.assetID === assetID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def findOrderID(id: String): Future[String] = db.run(negotiationTable.filter(_.id === id).map(_.orderID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def trGetIDByOrderID(orderID: String): Future[String] = db.run(negotiationTable.filter(_.orderID === orderID).map(_.id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def findPaymentTermsByID(id: String): Future[String] = db.run(negotiationTable.filter(_.id === id).map(_.paymentTerms).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def findDocumentListByID(id: String): Future[String] = db.run(negotiationTable.filter(_.id === id).map(_.documentList).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def updateStatusByID(id: String, status: String): Future[Int] = db.run(negotiationTable.filter(_.id === id).map(_.status).update(status).asTry).map {
    case Success(result) => result match {
      case 0 => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      case _ => result
    }
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def updateStatusByOrderID(orderID: String, status: String): Future[Int] = db.run(negotiationTable.filter(_.orderID === orderID).map(_.status).update(status).asTry).map {
    case Success(result) => result match {
      case 0 => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      case _ => result
    }
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def updateAssetTermsAndBuyerAcceptedTermsByID(id: String, description: String, price: MicroNumber, quantity: MicroNumber, quantityUnit: String, buyerAcceptedAssetDescription: Boolean, buyerAcceptedPrice: Boolean, buyerAcceptedQuantity: Boolean): Future[Int] = db.run(negotiationTable.filter(_.id === id).map(x => (x.assetDescription, x.price, x.quantity, x.quantityUnit, x.buyerAcceptedAssetDescription, x.buyerAcceptedPrice, x.buyerAcceptedQuantity)).update((description, price.toMicroString, quantity.toMicroString, quantityUnit, buyerAcceptedAssetDescription, buyerAcceptedPrice, buyerAcceptedQuantity)).asTry).map {
    case Success(result) => result match {
      case 0 => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      case _ => result
    }
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def updateAssetOtherDetailsByID(id: String, assetOtherDetails: String, buyerAcceptedAssetOtherDetails: Boolean): Future[Int] = db.run(negotiationTable.filter(_.id === id).map(x => (x.assetOtherDetails, x.buyerAcceptedAssetOtherDetails)).update((assetOtherDetails, buyerAcceptedAssetOtherDetails)).asTry).map {
    case Success(result) => result match {
      case 0 => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      case _ => result
    }
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def updatePaymentTermsAndBuyerAcceptedPaymentTermsByID(id: String, paymentTerms: String, buyerAcceptedPaymentTerms: Boolean): Future[Int] = db.run(negotiationTable.filter(_.id === id).map(x => (x.paymentTerms, x.buyerAcceptedPaymentTerms)).update((paymentTerms, buyerAcceptedPaymentTerms)).asTry).map {
    case Success(result) => result match {
      case 0 => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      case _ => result
    }
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def updateDocumentListPhysicalDocumentsHandledViaAndBuyerAcceptedDocumentListByID(id: String, documentList: String, physicalDocumentsHandledVia: String, buyerAcceptedDocumentList: Boolean): Future[Int] = db.run(negotiationTable.filter(_.id === id).map(x => (x.documentList, x.physicalDocumentsHandledVia, x.buyerAcceptedDocumentList)).update((documentList, physicalDocumentsHandledVia, buyerAcceptedDocumentList)).asTry).map {
    case Success(result) => result match {
      case 0 => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      case _ => result
    }
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def updateAssetDescriptionByID(id: String, assetDescription: String, buyerAcceptedAssetDescription: Boolean): Future[Int] = db.run(negotiationTable.filter(_.id === id).map(x => (x.assetDescription, x.buyerAcceptedAssetDescription)).update((assetDescription, buyerAcceptedAssetDescription)).asTry).map {
    case Success(result) => result match {
      case 0 => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      case _ => result
    }
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def updatePriceByID(id: String, price: MicroNumber, buyerAcceptedPrice: Boolean): Future[Int] = db.run(negotiationTable.filter(_.id === id).map(x => (x.price, x.buyerAcceptedPrice)).update((price.toMicroString, buyerAcceptedPrice)).asTry).map {
    case Success(result) => result match {
      case 0 => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      case _ => result
    }
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def updateQuantityByID(id: String, quantity: MicroNumber, quantityUnit: String, buyerAcceptedQuantity: Boolean): Future[Int] = db.run(negotiationTable.filter(_.id === id).map(x => (x.quantity, x.quantityUnit, x.buyerAcceptedQuantity)).update((quantity.toMicroString, quantityUnit, buyerAcceptedQuantity)).asTry).map {
    case Success(result) => result match {
      case 0 => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      case _ => result
    }
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def updateStatusAndCommentByID(id: String, status: String, comment: Option[String]): Future[Int] = db.run(negotiationTable.filter(_.id === id).map(negotiation => (negotiation.status, negotiation.comment.?)).update((status, comment)).asTry).map {
    case Success(result) => result match {
      case 0 => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      case _ => result
    }
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def updateChatIDByID(id: String, chatID: String): Future[Int] = db.run(negotiationTable.filter(_.id === id).map(_.chatID).update(chatID).asTry).map {
    case Success(result) => result match {
      case 0 => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      case _ => result
    }
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def updateTimeByID(id: String, time: Option[Int]): Future[Int] = db.run(negotiationTable.filter(_.id === id).map(_.time.?).update(time).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def updateOrderIDAndStatusByID(id: String, orderID: String, status: String): Future[Int] = db.run(negotiationTable.filter(_.id === id).map(x => (x.orderID, x.status)).update((orderID, status)).asTry).map {
    case Success(result) => result match {
      case 0 => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      case _ => result
    }
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def deleteByStatusAndID(id: String, status: String): Future[Int] = db.run(negotiationTable.filter(_.id === id).filter(_.status === status).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def getStatusByID(id: String): Future[String] = db.run(negotiationTable.filter(_.id === id).map(_.status).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def findAllNegotiationsByTraderIDsAndStatuses(traderIDs: Seq[String], statuses: String*): Future[Seq[NegotiationSerializable]] = db.run((negotiationTable.filter(_.buyerTraderID inSet traderIDs) union negotiationTable.filter(_.sellerTraderID inSet traderIDs)).filter(_.status.inSet(statuses)).sortBy(x => x.updatedOn.ifNull(x.createdOn).desc).result)

  private def tryGetBuyerTraderIDByID(id: String): Future[String] = db.run(negotiationTable.filter(_.id === id).map(_.buyerTraderID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def tryGetSellerTraderIDByID(id: String): Future[String] = db.run(negotiationTable.filter(_.id === id).map(_.sellerTraderID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def findAllNegotiationsByBuyerTraderIDAndStatuses(traderID: String, statuses: String*): Future[Seq[NegotiationSerializable]] = db.run(negotiationTable.filter(_.buyerTraderID === traderID).filter(_.status.inSet(statuses)).sortBy(x => x.updatedOn.ifNull(x.createdOn).desc).result)

  private def findAllNegotiationsBySellerTraderIDAndStatuses(traderID: String, statuses: String*): Future[Seq[NegotiationSerializable]] = db.run(negotiationTable.filter(_.sellerTraderID === traderID).filter(_.status.inSet(statuses)).sortBy(x => x.updatedOn.ifNull(x.createdOn).desc).result)

  private def findAllNegotiationsByTraderIDAndStatus(traderID: String, status: String): Future[Seq[NegotiationSerializable]] = db.run(negotiationTable.filter(x => x.sellerTraderID === traderID || x.buyerTraderID === traderID).filter(_.status === status).sortBy(x => x.updatedOn.ifNull(x.createdOn).desc).result)

  private def findAllNegotiationsByBuyerTraderIDsAndStatuses(traderIDs: Seq[String], statuses: String*): Future[Seq[NegotiationSerializable]] = db.run(negotiationTable.filter(_.buyerTraderID.inSet(traderIDs)).filter(_.status.inSet(statuses)).sortBy(x => x.updatedOn.ifNull(x.createdOn).desc).result)

  private def findAllNegotiationsBySellerTraderIDsAndStatuses(traderIDs: Seq[String], statuses: String*): Future[Seq[NegotiationSerializable]] = db.run(negotiationTable.filter(_.sellerTraderID.inSet(traderIDs)).filter(_.status.inSet(statuses)).sortBy(x => x.updatedOn.ifNull(x.createdOn).desc).result)

  private def findAllByAssetID(assetID: String): Future[Seq[NegotiationSerializable]] = db.run(negotiationTable.filter(_.assetID === assetID).result)

  private def updateBuyerAcceptedAssetDescriptionByID(id: String, buyerAcceptedAssetDescription: Boolean): Future[Int] = db.run(negotiationTable.filter(_.id === id).map(_.buyerAcceptedAssetDescription).update(buyerAcceptedAssetDescription).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def updateBuyerAcceptedPriceByID(id: String, buyerAcceptedPrice: Boolean): Future[Int] = db.run(negotiationTable.filter(_.id === id).map(_.buyerAcceptedPrice).update(buyerAcceptedPrice).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def updateBuyerAcceptedQuantityByID(id: String, buyerAcceptedQuantity: Boolean): Future[Int] = db.run(negotiationTable.filter(_.id === id).map(_.buyerAcceptedQuantity).update(buyerAcceptedQuantity).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def updateBuyerAcceptedAssetOtherDetailsByID(id: String, buyerAcceptedAssetOtherDetails: Boolean): Future[Int] = db.run(negotiationTable.filter(_.id === id).map(_.buyerAcceptedAssetOtherDetails).update(buyerAcceptedAssetOtherDetails).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def updateBuyerAcceptedPaymentTermsByID(id: String, buyerAcceptedPaymentTerms: Boolean): Future[Int] = db.run(negotiationTable.filter(_.id === id).map(_.buyerAcceptedPaymentTerms).update(buyerAcceptedPaymentTerms).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def updateBuyerAcceptedDocumentListByID(id: String, buyerAcceptedDocumentList: Boolean): Future[Int] = db.run(negotiationTable.filter(_.id === id).map(_.buyerAcceptedDocumentList).update(buyerAcceptedDocumentList).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def checkByIDAndTraderID(id: String, traderID: String): Future[Boolean] = db.run(negotiationTable.filter(_.id === id).filter(x => x.buyerTraderID === traderID || x.sellerTraderID === traderID).exists.result)

  private[models] class NegotiationTable(tag: Tag) extends Table[NegotiationSerializable](tag, "Negotiation") {

    def * = (id, orderID.?, buyerTraderID, sellerTraderID, assetID,
      (assetDescription, price, quantity, quantityUnit, buyerAcceptedAssetDescription, buyerAcceptedPrice, buyerAcceptedQuantity),
      (assetOtherDetails, buyerAcceptedAssetOtherDetails),
      time.?,
      (paymentTerms, buyerAcceptedPaymentTerms),
      (documentList, buyerAcceptedDocumentList),
      physicalDocumentsHandledVia.?, chatID.?, status, comment.?,
      createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?).shaped <> ( {
      case (id, orderID, buyerTraderID, sellerTraderID, assetID, assetAndBuyerAccepted, assetOtherDetailsAndBuyerAcceptedSerialize, time, paymentTermsAndBuyerAcceptedSerialize, documentListAndBuyerAcceptedSerialize, physicalDocumentsHandledVia, chatID, status, comment, createdBy, createdOn, createdOnTimeZone, updatedBy, updatedOn, updatedOnTimeZone) =>
        NegotiationSerializable(id = id, orderID = orderID, buyerTraderID = buyerTraderID, sellerTraderID = sellerTraderID, assetID = assetID,
          assetAndBuyerAcceptedSerialize = AssetAndBuyerAcceptedSerialize.tupled.apply(assetAndBuyerAccepted),
          assetOtherDetailsAndBuyerAcceptedSerialize = AssetOtherDetailsAndBuyerAcceptedSerialize.tupled.apply(assetOtherDetailsAndBuyerAcceptedSerialize),
          time = time,
          paymentTermsAndBuyerAcceptedSerialize = PaymentTermsAndBuyerAcceptedSerialize.tupled.apply(paymentTermsAndBuyerAcceptedSerialize),
          documentListAndBuyerAcceptedSerialize = DocumentListAndBuyerAcceptedSerialize.tupled.apply(documentListAndBuyerAcceptedSerialize),
          physicalDocumentsHandledVia = physicalDocumentsHandledVia, chatID = chatID, status = status, comment = comment,
          createdBy = createdBy, createdOn = createdOn, createdOnTimeZone = createdOnTimeZone,
          updatedBy = updatedBy, updatedOn = updatedOn, updatedOnTimeZone = updatedOnTimeZone)
    }, { negotiationSerializable: NegotiationSerializable =>
      def f1(assetAndBuyerAcceptedSerialize: AssetAndBuyerAcceptedSerialize) = AssetAndBuyerAcceptedSerialize.unapply(assetAndBuyerAcceptedSerialize).get

      def f2(assetOtherDetailsAndBuyerAcceptedSerialize: AssetOtherDetailsAndBuyerAcceptedSerialize) = AssetOtherDetailsAndBuyerAcceptedSerialize.unapply(assetOtherDetailsAndBuyerAcceptedSerialize).get

      def f3(paymentTermsAndBuyerAcceptedSerialize: PaymentTermsAndBuyerAcceptedSerialize) = PaymentTermsAndBuyerAcceptedSerialize.unapply(paymentTermsAndBuyerAcceptedSerialize).get

      def f4(documentListAndBuyerAcceptedSerialize: DocumentListAndBuyerAcceptedSerialize) = DocumentListAndBuyerAcceptedSerialize.unapply(documentListAndBuyerAcceptedSerialize).get

      Some((negotiationSerializable.id, negotiationSerializable.orderID, negotiationSerializable.buyerTraderID, negotiationSerializable.sellerTraderID, negotiationSerializable.assetID,
        f1(negotiationSerializable.assetAndBuyerAcceptedSerialize),
        f2(negotiationSerializable.assetOtherDetailsAndBuyerAcceptedSerialize),
        negotiationSerializable.time,
        f3(negotiationSerializable.paymentTermsAndBuyerAcceptedSerialize),
        f4(negotiationSerializable.documentListAndBuyerAcceptedSerialize),
        negotiationSerializable.physicalDocumentsHandledVia, negotiationSerializable.chatID, negotiationSerializable.status, negotiationSerializable.comment,
        negotiationSerializable.createdBy, negotiationSerializable.createdOn, negotiationSerializable.createdOnTimeZone,
        negotiationSerializable.updatedBy, negotiationSerializable.updatedOn, negotiationSerializable.updatedOnTimeZone))
    })

    def id = column[String]("id", O.PrimaryKey)

    def orderID = column[String]("orderID")

    def buyerTraderID = column[String]("buyerTraderID")

    def sellerTraderID = column[String]("sellerTraderID")

    def assetID = column[String]("assetID")

    def assetDescription = column[String]("assetDescription")

    def price = column[String]("price")

    def quantity = column[String]("quantity")

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

    def physicalDocumentsHandledVia = column[String]("physicalDocumentsHandledVia")

    def chatID = column[String]("chatID")

    def status = column[String]("status")

    def comment = column[String]("comment")

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")

  }

  object Service {

    def create(buyerTraderID: String, sellerTraderID: String, assetID: String, description: String, price: MicroNumber, quantity: MicroNumber, quantityUnit: String, assetOtherDetails: AssetOtherDetails): Future[String] = add(Negotiation(id = utilities.IDGenerator.requestID(), buyerTraderID = buyerTraderID, sellerTraderID = sellerTraderID, assetID = assetID, assetDescription = description, price = price, quantity = quantity, quantityUnit = quantityUnit, assetOtherDetails = assetOtherDetails, paymentTerms = PaymentTerms(), documentList = DocumentList(Seq(constants.File.Asset.BILL_OF_LADING), Seq(constants.File.Negotiation.INVOICE)), status = constants.Status.Negotiation.FORM_INCOMPLETE))

    def update(negotiation: Negotiation): Future[Int] = updateByID(negotiation)

    def tryGet(id: String): Future[Negotiation] = tryGetByID(id).map(_.deserialize)

    def get(id: String): Future[Option[Negotiation]] = getByID(id).map(_.map(_.deserialize))

    def tryGetByBCNegotiationID(negotiationID: String): Future[Negotiation] = tryGetByOrderID(negotiationID).map(_.deserialize)

    def tryGetByBuyerSellerTraderIDAndAssetID(buyerTraderID: String, sellerTraderID: String, assetID: String): Future[Negotiation] = findByBuyerSellerTraderIDAndAssetID(buyerTraderID = buyerTraderID, sellerTraderID = sellerTraderID, assetID = assetID).map(_.deserialize)

    def tryGetNegotiationIDByID(id: String): Future[String] = findOrderID(id)

    def tryGetIDByNegotiationID(negotiationID: String): Future[String] = trGetIDByOrderID(negotiationID)

    def tryGetPaymentTerms(id: String): Future[PaymentTerms] = findPaymentTermsByID(id).map(paymentTerms => utilities.JSON.convertJsonStringToObject[PaymentTerms](paymentTerms))

    def updatePaymentTerms(id: String, paymentTerms: PaymentTerms): Future[Int] = updatePaymentTermsAndBuyerAcceptedPaymentTermsByID(id = id, paymentTerms = Json.toJson(paymentTerms).toString(), buyerAcceptedPaymentTerms = false)

    def tryGetDocumentList(id: String): Future[DocumentList] = findDocumentListByID(id).map(documentList => utilities.JSON.convertJsonStringToObject[DocumentList](documentList))

    def updateDocumentList(id: String, documentList: DocumentList, physicalDocumentsHandledVia: String): Future[Int] = updateDocumentListPhysicalDocumentsHandledViaAndBuyerAcceptedDocumentListByID(id = id, documentList = Json.toJson(documentList).toString(), physicalDocumentsHandledVia = physicalDocumentsHandledVia, buyerAcceptedDocumentList = false)

    def updateAssetTerms(id: String, description: String, price: MicroNumber, quantity: MicroNumber, quantityUnit: String, buyerAcceptedAssetDescription: Boolean , buyerAcceptedPrice: Boolean, buyerAcceptedQuantity: Boolean): Future[Int] = updateAssetTermsAndBuyerAcceptedTermsByID(id = id, description = description, price = price, quantity = quantity, quantityUnit = quantityUnit, buyerAcceptedAssetDescription = buyerAcceptedAssetDescription, buyerAcceptedPrice = buyerAcceptedPrice, buyerAcceptedQuantity = buyerAcceptedQuantity)

    def updateAssetOtherDetails(id: String, assetOtherDetails: AssetOtherDetails): Future[Int] = updateAssetOtherDetailsByID(id = id, assetOtherDetails = Json.toJson(assetOtherDetails).toString(), buyerAcceptedAssetOtherDetails = false)

    def tryGetStatus(id: String): Future[String] = getStatusByID(id)

    def markStatusRequestSent(id: String): Future[Int] = updateStatusByID(id = id, status = constants.Status.Negotiation.REQUEST_SENT)

    def markStatusIssueAssetRequestFailed(id: String): Future[Int] = updateStatusByID(id = id, status = constants.Status.Negotiation.ISSUE_ASSET_FAILED)

    def markStatusIssueAssetPending(id: String): Future[Int] = updateStatusByID(id = id, status = constants.Status.Negotiation.ISSUE_ASSET_PENDING)

    def markRequestRejected(id: String, comment: Option[String]): Future[Int] = updateStatusAndCommentByID(id = id, status = constants.Status.Negotiation.REJECTED, comment = comment)

    def markAccepted(id: String): Future[Int] = updateStatusByID(id = id, status = constants.Status.Negotiation.STARTED)

    def markBuyerAcceptedAllNegotiationTerms(id: String): Future[Int] = updateStatusByID(id = id, status = constants.Status.Negotiation.BUYER_ACCEPTED_ALL_NEGOTIATION_TERMS)

    def markContractSigned(id: String): Future[Int] = updateStatusByID(id = id, status = constants.Status.Negotiation.CONTRACT_SIGNED)

    def markCompleted(id: String): Future[Int] = updateStatusByID(id = id, status = constants.Status.Negotiation.COMPLETED)

    def markAssetAlreadyTraded(id: String): Future[Int] = updateStatusByID(id = id, status = constants.Status.Negotiation.ASSET_ALREADY_TRADED)

    def getAllByAssetID(assetID: String): Future[Seq[Negotiation]] = findAllByAssetID(assetID).map(_.map(_.deserialize))

    def getAllAcceptedNegotiationListByTraderIDs(traderIDs: Seq[String]): Future[Seq[Negotiation]] = findAllNegotiationsByTraderIDsAndStatuses(traderIDs = traderIDs, constants.Status.Negotiation.STARTED, constants.Status.Negotiation.BUYER_ACCEPTED_ALL_NEGOTIATION_TERMS, constants.Status.Negotiation.CONTRACT_SIGNED, constants.Status.Negotiation.BUYER_CONFIRMED_SELLER_PENDING, constants.Status.Negotiation.SELLER_CONFIRMED_BUYER_PENDING, constants.Status.Negotiation.BOTH_PARTIES_CONFIRMED, constants.Status.Negotiation.COMPLETED, constants.Status.Negotiation.ASSET_ALREADY_TRADED).map(_.map(_.deserialize))

    def getAllAcceptedBuyNegotiationListByTraderID(traderID: String): Future[Seq[Negotiation]] = findAllNegotiationsByBuyerTraderIDAndStatuses(traderID = traderID, statuses = constants.Status.Negotiation.STARTED, constants.Status.Negotiation.BUYER_ACCEPTED_ALL_NEGOTIATION_TERMS, constants.Status.Negotiation.CONTRACT_SIGNED, constants.Status.Negotiation.BUYER_ACCEPTED_ALL_NEGOTIATION_TERMS, constants.Status.Negotiation.BUYER_CONFIRMED_SELLER_PENDING, constants.Status.Negotiation.SELLER_CONFIRMED_BUYER_PENDING, constants.Status.Negotiation.BOTH_PARTIES_CONFIRMED, constants.Status.Negotiation.COMPLETED, constants.Status.Negotiation.ASSET_ALREADY_TRADED).map(_.map(_.deserialize))

    def getAllAcceptedSellNegotiationListByTraderID(traderID: String): Future[Seq[Negotiation]] = findAllNegotiationsBySellerTraderIDAndStatuses(traderID = traderID, statuses = constants.Status.Negotiation.STARTED, constants.Status.Negotiation.BUYER_ACCEPTED_ALL_NEGOTIATION_TERMS, constants.Status.Negotiation.CONTRACT_SIGNED, constants.Status.Negotiation.BUYER_CONFIRMED_SELLER_PENDING, constants.Status.Negotiation.SELLER_CONFIRMED_BUYER_PENDING, constants.Status.Negotiation.BOTH_PARTIES_CONFIRMED, constants.Status.Negotiation.COMPLETED, constants.Status.Negotiation.ASSET_ALREADY_TRADED).map(_.map(_.deserialize))

    def getAllRejectedNegotiationListByBuyerTraderID(traderID: String): Future[Seq[Negotiation]] = findAllNegotiationsByBuyerTraderIDAndStatuses(traderID = traderID, statuses = constants.Status.Negotiation.REJECTED).map(_.map(_.deserialize))

    def getAllRejectedNegotiationListBySellerTraderID(traderID: String): Future[Seq[Negotiation]] = findAllNegotiationsBySellerTraderIDAndStatuses(traderID = traderID, statuses = constants.Status.Negotiation.REJECTED).map(_.map(_.deserialize))

    def getAllContractSignedNegotiationListByBuyerTraderID(traderID: String): Future[Seq[Negotiation]]= findAllNegotiationsByBuyerTraderIDAndStatuses(traderID = traderID, constants.Status.Negotiation.CONTRACT_SIGNED, constants.Status.Negotiation.BUYER_CONFIRMED_SELLER_PENDING, constants.Status.Negotiation.SELLER_CONFIRMED_BUYER_PENDING, constants.Status.Negotiation.BOTH_PARTIES_CONFIRMED, constants.Status.Negotiation.COMPLETED, constants.Status.Negotiation.ASSET_ALREADY_TRADED).map(_.map(_.deserialize))

    def getAllContractSignedNegotiationListBySellerTraderID(traderID: String): Future[Seq[Negotiation]]= findAllNegotiationsBySellerTraderIDAndStatuses(traderID = traderID, constants.Status.Negotiation.CONTRACT_SIGNED, constants.Status.Negotiation.BUYER_CONFIRMED_SELLER_PENDING, constants.Status.Negotiation.SELLER_CONFIRMED_BUYER_PENDING, constants.Status.Negotiation.BOTH_PARTIES_CONFIRMED, constants.Status.Negotiation.COMPLETED, constants.Status.Negotiation.ASSET_ALREADY_TRADED).map(_.map(_.deserialize))

    def getAllContractSignedNegotiationListByBuyerTraderIDs(traderIDs: Seq[String]): Future[Seq[Negotiation]]= findAllNegotiationsByBuyerTraderIDsAndStatuses(traderIDs = traderIDs, constants.Status.Negotiation.CONTRACT_SIGNED, constants.Status.Negotiation.BUYER_CONFIRMED_SELLER_PENDING, constants.Status.Negotiation.SELLER_CONFIRMED_BUYER_PENDING, constants.Status.Negotiation.BOTH_PARTIES_CONFIRMED, constants.Status.Negotiation.COMPLETED, constants.Status.Negotiation.ASSET_ALREADY_TRADED).map(_.map(_.deserialize))

    def getAllContractSignedNegotiationListBySellerTraderIDs(traderIDs: Seq[String]): Future[Seq[Negotiation]]= findAllNegotiationsBySellerTraderIDsAndStatuses(traderIDs = traderIDs, constants.Status.Negotiation.CONTRACT_SIGNED, constants.Status.Negotiation.BUYER_CONFIRMED_SELLER_PENDING, constants.Status.Negotiation.SELLER_CONFIRMED_BUYER_PENDING, constants.Status.Negotiation.BOTH_PARTIES_CONFIRMED, constants.Status.Negotiation.COMPLETED, constants.Status.Negotiation.ASSET_ALREADY_TRADED).map(_.map(_.deserialize))

    def getAllFailedNegotiationListBySellerTraderID(traderID: String): Future[Seq[Negotiation]] = findAllNegotiationsBySellerTraderIDAndStatuses(traderID = traderID, statuses = constants.Status.Negotiation.ISSUE_ASSET_FAILED).map(_.map(_.deserialize))

    def getAllReceivedNegotiationListByTraderID(traderID: String): Future[Seq[Negotiation]] = findAllNegotiationsByBuyerTraderIDAndStatuses(traderID = traderID, statuses = constants.Status.Negotiation.REQUEST_SENT).map(_.map(_.deserialize))

    def getAllSentNegotiationRequestListByTraderID(traderID: String): Future[Seq[Negotiation]] = findAllNegotiationsBySellerTraderIDAndStatuses(traderID = traderID, statuses = constants.Status.Negotiation.REQUEST_SENT).map(_.map(_.deserialize))

    def getAllIncompleteNegotiationListByTraderID(traderID: String): Future[Seq[Negotiation]] = findAllNegotiationsBySellerTraderIDAndStatuses(traderID = traderID, statuses = constants.Status.Negotiation.FORM_INCOMPLETE, constants.Status.Negotiation.ISSUE_ASSET_PENDING).map(_.map(_.deserialize))

    def getAllConfirmedNegotiationListByTraderID(traderID: String): Future[Seq[Negotiation]] = findAllNegotiationsByTraderIDAndStatus(traderID = traderID, status = constants.Status.Negotiation.BOTH_PARTIES_CONFIRMED).map(_.map(_.deserialize))

    def getAllConfirmedNegotiationListByTraderIDs(traderIDs: Seq[String]): Future[Seq[Negotiation]] = findAllNegotiationsByTraderIDsAndStatuses(traderIDs = traderIDs, statuses = constants.Status.Negotiation.BOTH_PARTIES_CONFIRMED).map(serializedNegotiations => serializedNegotiations.map(_.deserialize))

    def getAllTradeCompletedBuyNegotiationListByTraderID(traderID: String): Future[Seq[Negotiation]] = findAllNegotiationsByBuyerTraderIDAndStatuses(traderID = traderID, constants.Status.Negotiation.COMPLETED, constants.Status.Negotiation.ASSET_ALREADY_TRADED).map(_.map(_.deserialize))

    def getAllTradeCompletedSellNegotiationListByTraderID(traderID: String): Future[Seq[Negotiation]] = findAllNegotiationsBySellerTraderIDAndStatuses(traderID = traderID, constants.Status.Negotiation.COMPLETED, constants.Status.Negotiation.ASSET_ALREADY_TRADED).map(_.map(_.deserialize))

    def getAllTradeCompletedBuyNegotiationListByTraderIDs(traderIDs: Seq[String]): Future[Seq[Negotiation]] = findAllNegotiationsByBuyerTraderIDsAndStatuses(traderIDs = traderIDs, constants.Status.Negotiation.COMPLETED, constants.Status.Negotiation.ASSET_ALREADY_TRADED).map(_.map(_.deserialize))

    def getAllTradeCompletedSellNegotiationListByTraderIDs(traderIDs: Seq[String]): Future[Seq[Negotiation]] = findAllNegotiationsBySellerTraderIDsAndStatuses(traderIDs = traderIDs, constants.Status.Negotiation.COMPLETED, constants.Status.Negotiation.ASSET_ALREADY_TRADED).map(_.map(_.deserialize))

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

    def updateAssetDescription(id: String, assetDescription: String, buyerAcceptedAssetDescription: Boolean): Future[Int] = updateAssetDescriptionByID(id = id, assetDescription = assetDescription, buyerAcceptedAssetDescription)

    def updateQuantity(id: String, quantity: MicroNumber, quantityUnit: String, buyerAcceptedQuantity: Boolean): Future[Int] = updateQuantityByID(id = id, quantity = quantity, quantityUnit = quantityUnit, buyerAcceptedQuantity)

    def updatePrice(id: String, price: MicroNumber): Future[Int] = updatePriceByID(id = id, price = price, buyerAcceptedPrice = false)

    def getAllAcceptedBuyNegotiationListByTraderIDs(traderIDs: Seq[String]): Future[Seq[Negotiation]] = findAllNegotiationsByBuyerTraderIDsAndStatuses(traderIDs = traderIDs, constants.Status.Negotiation.STARTED, constants.Status.Negotiation.CONTRACT_SIGNED, constants.Status.Negotiation.BUYER_ACCEPTED_ALL_NEGOTIATION_TERMS, constants.Status.Negotiation.BUYER_CONFIRMED_SELLER_PENDING, constants.Status.Negotiation.SELLER_CONFIRMED_BUYER_PENDING, constants.Status.Negotiation.BOTH_PARTIES_CONFIRMED).map(_.map(_.deserialize))

    def getAllAcceptedSellNegotiationListByTraderIDs(traderIDs: Seq[String]): Future[Seq[Negotiation]] = findAllNegotiationsBySellerTraderIDsAndStatuses(traderIDs = traderIDs, constants.Status.Negotiation.STARTED, constants.Status.Negotiation.CONTRACT_SIGNED, constants.Status.Negotiation.BUYER_ACCEPTED_ALL_NEGOTIATION_TERMS, constants.Status.Negotiation.BUYER_CONFIRMED_SELLER_PENDING, constants.Status.Negotiation.SELLER_CONFIRMED_BUYER_PENDING, constants.Status.Negotiation.BOTH_PARTIES_CONFIRMED).map(_.map(_.deserialize))

    def getAllRejectedNegotiationListByBuyerTraderIDs(traderIDs: Seq[String]): Future[Seq[Negotiation]] = findAllNegotiationsByBuyerTraderIDsAndStatuses(traderIDs = traderIDs, constants.Status.Negotiation.REJECTED).map(_.map(_.deserialize))

    def getAllRejectedNegotiationListBySellerTraderIDs(traderIDs: Seq[String]): Future[Seq[Negotiation]] = findAllNegotiationsBySellerTraderIDsAndStatuses(traderIDs = traderIDs, constants.Status.Negotiation.REJECTED).map(_.map(_.deserialize))

    def getAllFailedNegotiationListBySellerTraderIDs(traderIDs: Seq[String]): Future[Seq[Negotiation]] = findAllNegotiationsBySellerTraderIDsAndStatuses(traderIDs = traderIDs, constants.Status.Negotiation.ISSUE_ASSET_FAILED, constants.Status.Negotiation.TIMED_OUT).map(_.map(_.deserialize))

    def getAllReceivedNegotiationListByTraderIDs(traderIDs: Seq[String]): Future[Seq[Negotiation]] = findAllNegotiationsByBuyerTraderIDsAndStatuses(traderIDs = traderIDs, constants.Status.Negotiation.REQUEST_SENT).map(_.map(_.deserialize))

    def getAllSentNegotiationRequestListByTraderIDs(traderIDs: Seq[String]): Future[Seq[Negotiation]] = findAllNegotiationsBySellerTraderIDsAndStatuses(traderIDs = traderIDs, constants.Status.Negotiation.REQUEST_SENT).map(_.map(_.deserialize))

    def getAllIncompleteNegotiationListByTraderIDs(traderIDs: Seq[String]): Future[Seq[Negotiation]] = findAllNegotiationsBySellerTraderIDsAndStatuses(traderIDs = traderIDs, constants.Status.Negotiation.FORM_INCOMPLETE, constants.Status.Negotiation.ISSUE_ASSET_PENDING).map(_.map(_.deserialize))


  }

}

