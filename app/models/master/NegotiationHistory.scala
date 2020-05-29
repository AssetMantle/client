package models.master

import java.sql.Timestamp

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Trait.HistoryLogged
import models.common.Serializable.{AssetOtherDetails, DocumentList, PaymentTerms}
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json
import play.api.{Configuration, Logger}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class NegotiationHistory(id: String, negotiationID: Option[String] = None, buyerTraderID: String, sellerTraderID: String, assetID: String, assetDescription: String, price: Int, quantity: Int, quantityUnit: String, buyerAcceptedAssetDescription: Boolean = false, buyerAcceptedPrice: Boolean = false, buyerAcceptedQuantity: Boolean = false, assetOtherDetails: AssetOtherDetails, buyerAcceptedAssetOtherDetails: Boolean = false, time: Option[Int] = None, paymentTerms: PaymentTerms, buyerAcceptedPaymentTerms: Boolean = false, documentList: DocumentList, buyerAcceptedDocumentList: Boolean = false, physicalDocumentsHandledVia: Option[String] = None, chatID: Option[String] = None, status: String, comment: Option[String] = None, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None, deletedBy: String, deletedOn: Timestamp, deletedOnTimeZone: String) extends HistoryLogged

@Singleton
class NegotiationHistories @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider, configuration: Configuration)(implicit executionContext: ExecutionContext) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.MASTER_NEGOTIATION_HISTORY

  import databaseConfig.profile.api._

  private[models] val negotiationHistoryTable = TableQuery[NegotiationHistoryTable]

  case class BuyerSellerAndAssetID(buyerTraderID: String, sellerTraderID: String, assetID: String)

  case class AssetAndBuyerAcceptedSerialize(assetDescription: String, price: Int, quantity: Int, quantityUnit: String, buyerAcceptedAssetDescription: Boolean, buyerAcceptedPrice: Boolean, buyerAcceptedQuantity: Boolean)

  case class AssetOtherDetailsAndBuyerAcceptedSerialize(assetOtherDetails: String, buyerAccepted: Boolean)

  case class PaymentTermsAndBuyerAcceptedSerialize(paymentTerms: String, buyerAccepted: Boolean)

  case class DocumentListAndBuyerAcceptedSerialize(documentList: String, buyerAccepted: Boolean)

  case class NegotiationHistorySerializable(id: String, negotiationID: Option[String], buyerSellerAndAssetID: BuyerSellerAndAssetID, assetAndBuyerAcceptedSerialize: AssetAndBuyerAcceptedSerialize, assetOtherDetailsAndBuyerAcceptedSerialize: AssetOtherDetailsAndBuyerAcceptedSerialize, time: Option[Int], paymentTermsAndBuyerAcceptedSerialize: PaymentTermsAndBuyerAcceptedSerialize, documentListAndBuyerAcceptedSerialize: DocumentListAndBuyerAcceptedSerialize, physicalDocumentsHandledVia: Option[String], chatID: Option[String], status: String, comment: Option[String], createdBy: Option[String], createdOn: Option[Timestamp], createdOnTimeZone: Option[String], updatedBy: Option[String], updatedOn: Option[Timestamp], updatedOnTimeZone: Option[String], deletedBy: String, deletedOn: Timestamp, deletedOnTimeZone: String) {
    def deserialize: NegotiationHistory = NegotiationHistory(id = id, negotiationID = negotiationID,
      buyerTraderID = buyerSellerAndAssetID.buyerTraderID, sellerTraderID = buyerSellerAndAssetID.sellerTraderID, assetID = buyerSellerAndAssetID.assetID,
      assetDescription = assetAndBuyerAcceptedSerialize.assetDescription, price = assetAndBuyerAcceptedSerialize.price, quantity = assetAndBuyerAcceptedSerialize.quantity, quantityUnit = assetAndBuyerAcceptedSerialize.quantityUnit,
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
      updatedBy = updatedBy, updatedOn = updatedOn, updatedOnTimeZone = updatedOnTimeZone,
      deletedBy = deletedBy, deletedOn = deletedOn, deletedOnTimeZone = deletedOnTimeZone)
  }

  def serialize(negotiationHistory: NegotiationHistory): NegotiationHistorySerializable = NegotiationHistorySerializable(id = negotiationHistory.id, negotiationID = negotiationHistory.negotiationID,
    buyerSellerAndAssetID = BuyerSellerAndAssetID(buyerTraderID = negotiationHistory.buyerTraderID, sellerTraderID = negotiationHistory.sellerTraderID, assetID = negotiationHistory.assetID),
    assetAndBuyerAcceptedSerialize = AssetAndBuyerAcceptedSerialize(assetDescription = negotiationHistory.assetDescription, price = negotiationHistory.price, quantity = negotiationHistory.quantity, quantityUnit = negotiationHistory.quantityUnit,
      buyerAcceptedAssetDescription = negotiationHistory.buyerAcceptedAssetDescription, buyerAcceptedPrice = negotiationHistory.buyerAcceptedPrice, buyerAcceptedQuantity = negotiationHistory.buyerAcceptedQuantity),
    assetOtherDetailsAndBuyerAcceptedSerialize = AssetOtherDetailsAndBuyerAcceptedSerialize(assetOtherDetails = Json.toJson(negotiationHistory.assetOtherDetails).toString(), buyerAccepted = negotiationHistory.buyerAcceptedPaymentTerms),
    time = negotiationHistory.time,
    paymentTermsAndBuyerAcceptedSerialize = PaymentTermsAndBuyerAcceptedSerialize(paymentTerms = Json.toJson(negotiationHistory.paymentTerms).toString(), buyerAccepted = negotiationHistory.buyerAcceptedPaymentTerms),
    documentListAndBuyerAcceptedSerialize = DocumentListAndBuyerAcceptedSerialize(documentList = Json.toJson(negotiationHistory.documentList).toString(), buyerAccepted = negotiationHistory.buyerAcceptedDocumentList),
    physicalDocumentsHandledVia = negotiationHistory.physicalDocumentsHandledVia, chatID = negotiationHistory.chatID, status = negotiationHistory.status, comment = negotiationHistory.comment,
    createdBy = negotiationHistory.createdBy, createdOn = negotiationHistory.createdOn, createdOnTimeZone = negotiationHistory.createdOnTimeZone,
    updatedBy = negotiationHistory.updatedBy, updatedOn = negotiationHistory.updatedOn, updatedOnTimeZone = negotiationHistory.updatedOnTimeZone,
    deletedBy = negotiationHistory.deletedBy, deletedOn = negotiationHistory.deletedOn, deletedOnTimeZone = negotiationHistory.deletedOnTimeZone)

  private def tryGetByID(id: String): Future[NegotiationHistorySerializable] = db.run(negotiationHistoryTable.filter(_.id === id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def tryGetByNegotiationID(negotiationID: String): Future[NegotiationHistorySerializable] = db.run(negotiationHistoryTable.filter(_.negotiationID === negotiationID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def findByBuyerSellerTraderIDAndAssetID(buyerTraderID: String, sellerTraderID: String, assetID: String): Future[NegotiationHistorySerializable] = db.run(negotiationHistoryTable.filter(_.buyerTraderID === buyerTraderID).filter(_.sellerTraderID === sellerTraderID).filter(_.assetID === assetID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def findNegotiationID(id: String): Future[String] = db.run(negotiationHistoryTable.filter(_.id === id).map(_.negotiationID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def trGetIDByNegotiationID(negotiationID: String): Future[String] = db.run(negotiationHistoryTable.filter(_.negotiationID === negotiationID).map(_.id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def findPaymentTermsByID(id: String): Future[String] = db.run(negotiationHistoryTable.filter(_.id === id).map(_.paymentTerms).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def findDocumentListByID(id: String): Future[String] = db.run(negotiationHistoryTable.filter(_.id === id).map(_.documentList).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def getStatusByID(id: String): Future[String] = db.run(negotiationHistoryTable.filter(_.id === id).map(_.status).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def findAllNegotiationsByTraderIDAndStatuses(traderID: String, statuses: String*): Future[Seq[NegotiationHistorySerializable]] = db.run((negotiationHistoryTable.filter(_.buyerTraderID === traderID) union negotiationHistoryTable.filter(_.sellerTraderID === traderID)).filter(_.status.inSet(statuses)).result)

  private def findAllNegotiationsByTraderIDsAndStatuses(traderIDs: Seq[String], statuses: String*): Future[Seq[NegotiationHistorySerializable]] = db.run((negotiationHistoryTable.filter(_.buyerTraderID inSet traderIDs) union negotiationHistoryTable.filter(_.sellerTraderID inSet traderIDs)).filter(_.status.inSet(statuses)).result)

  private def tryGetBuyerTraderIDByID(id: String): Future[String] = db.run(negotiationHistoryTable.filter(_.id === id).map(_.buyerTraderID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def tryGetSellerTraderIDByID(id: String): Future[String] = db.run(negotiationHistoryTable.filter(_.id === id).map(_.sellerTraderID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def findAllNegotiationsByBuyerTraderIDAndStatuses(traderID: String, statuses: String*): Future[Seq[NegotiationHistorySerializable]] = db.run(negotiationHistoryTable.filter(_.buyerTraderID === traderID).filter(_.status.inSet(statuses)).result)

  private def findAllNegotiationsBySellerTraderIDAndStatuses(traderID: String, statuses: String*): Future[Seq[NegotiationHistorySerializable]] = db.run(negotiationHistoryTable.filter(_.sellerTraderID === traderID).filter(_.status.inSet(statuses)).result)

  private def findAllNegotiationsByTraderIDAndStatus(traderID: String, status: String): Future[Seq[NegotiationHistorySerializable]] = db.run(negotiationHistoryTable.filter(x => x.sellerTraderID === traderID || x.buyerTraderID === traderID).filter(_.status === status).result)

  private def findAllNegotiationsByBuyerTraderIDsAndStatuses(traderIDs: Seq[String], statuses: String*): Future[Seq[NegotiationHistorySerializable]] = db.run(negotiationHistoryTable.filter(_.buyerTraderID.inSet(traderIDs)).filter(_.status.inSet(statuses)).result)

  private def findAllNegotiationsBySellerTraderIDsAndStatuses(traderIDs: Seq[String], statuses: String*): Future[Seq[NegotiationHistorySerializable]] = db.run(negotiationHistoryTable.filter(_.sellerTraderID.inSet(traderIDs)).filter(_.status.inSet(statuses)).result)

  private def findAllByAssetID(assetID: String): Future[Seq[NegotiationHistorySerializable]] = db.run(negotiationHistoryTable.filter(_.assetID === assetID).result)

  private[models] class NegotiationHistoryTable(tag: Tag) extends Table[NegotiationHistorySerializable](tag, "Negotiation_History") {

    def * = (id, negotiationID.?,
      (buyerTraderID, sellerTraderID, assetID),
      (assetDescription, price, quantity, quantityUnit, buyerAcceptedAssetDescription, buyerAcceptedPrice, buyerAcceptedQuantity),
      (assetOtherDetails, buyerAcceptedAssetOtherDetails),
      time.?,
      (paymentTerms, buyerAcceptedPaymentTerms),
      (documentList, buyerAcceptedDocumentList),
      physicalDocumentsHandledVia.?, chatID.?, status, comment.?,
      createdBy.?, createdOn.?, createdOnTimeZone.?,
      updatedBy.?, updatedOn.?, updatedOnTimeZone.?,
      deletedBy, deletedOn, deletedOnTimeZone).shaped <> ( {
      case (id, negotiationID, buyerSellerAndAssetID, assetAndBuyerAccepted, assetOtherDetailsAndBuyerAcceptedSerialize, time, paymentTermsAndBuyerAcceptedSerialize, documentListAndBuyerAcceptedSerialize, physicalDocumentsHandledVia, chatID, status, comment, createdBy, createdOn, createdOnTimeZone, updatedBy, updatedOn, updatedOnTimeZone, deletedBy, deletedOn, deletedOnTimeZone) =>
        NegotiationHistorySerializable(id = id, negotiationID = negotiationID,
          buyerSellerAndAssetID = BuyerSellerAndAssetID.tupled.apply(buyerSellerAndAssetID),
          assetAndBuyerAcceptedSerialize = AssetAndBuyerAcceptedSerialize.tupled.apply(assetAndBuyerAccepted),
          assetOtherDetailsAndBuyerAcceptedSerialize = AssetOtherDetailsAndBuyerAcceptedSerialize.tupled.apply(assetOtherDetailsAndBuyerAcceptedSerialize),
          time = time,
          paymentTermsAndBuyerAcceptedSerialize = PaymentTermsAndBuyerAcceptedSerialize.tupled.apply(paymentTermsAndBuyerAcceptedSerialize),
          documentListAndBuyerAcceptedSerialize = DocumentListAndBuyerAcceptedSerialize.tupled.apply(documentListAndBuyerAcceptedSerialize),
          physicalDocumentsHandledVia = physicalDocumentsHandledVia, chatID = chatID, status = status, comment = comment,
          createdBy = createdBy, createdOn = createdOn, createdOnTimeZone = createdOnTimeZone,
          updatedBy = updatedBy, updatedOn = updatedOn, updatedOnTimeZone = updatedOnTimeZone,
          deletedBy = deletedBy, deletedOn = deletedOn, deletedOnTimeZone = deletedOnTimeZone)
    }, { negotiationHistorySerializable: NegotiationHistorySerializable =>
      def f1(assetAndBuyerAcceptedSerialize: AssetAndBuyerAcceptedSerialize) = AssetAndBuyerAcceptedSerialize.unapply(assetAndBuyerAcceptedSerialize).get

      def f2(assetOtherDetailsAndBuyerAcceptedSerialize: AssetOtherDetailsAndBuyerAcceptedSerialize) = AssetOtherDetailsAndBuyerAcceptedSerialize.unapply(assetOtherDetailsAndBuyerAcceptedSerialize).get

      def f3(paymentTermsAndBuyerAcceptedSerialize: PaymentTermsAndBuyerAcceptedSerialize) = PaymentTermsAndBuyerAcceptedSerialize.unapply(paymentTermsAndBuyerAcceptedSerialize).get

      def f4(documentListAndBuyerAcceptedSerialize: DocumentListAndBuyerAcceptedSerialize) = DocumentListAndBuyerAcceptedSerialize.unapply(documentListAndBuyerAcceptedSerialize).get

      def f5(buyerSellerAndAssetID: BuyerSellerAndAssetID) = BuyerSellerAndAssetID.unapply(buyerSellerAndAssetID).get

      Some((negotiationHistorySerializable.id, negotiationHistorySerializable.negotiationID,
        f5(negotiationHistorySerializable.buyerSellerAndAssetID),
        f1(negotiationHistorySerializable.assetAndBuyerAcceptedSerialize),
        f2(negotiationHistorySerializable.assetOtherDetailsAndBuyerAcceptedSerialize),
        negotiationHistorySerializable.time,
        f3(negotiationHistorySerializable.paymentTermsAndBuyerAcceptedSerialize),
        f4(negotiationHistorySerializable.documentListAndBuyerAcceptedSerialize),
        negotiationHistorySerializable.physicalDocumentsHandledVia, negotiationHistorySerializable.chatID, negotiationHistorySerializable.status, negotiationHistorySerializable.comment,
        negotiationHistorySerializable.createdBy, negotiationHistorySerializable.createdOn, negotiationHistorySerializable.createdOnTimeZone,
        negotiationHistorySerializable.updatedBy, negotiationHistorySerializable.updatedOn, negotiationHistorySerializable.updatedOnTimeZone,
        negotiationHistorySerializable.deletedBy, negotiationHistorySerializable.deletedOn, negotiationHistorySerializable.deletedOnTimeZone))
    })

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

    def deletedBy = column[String]("deletedBy")

    def deletedOn = column[Timestamp]("deletedOn")

    def deletedOnTimeZone = column[String]("deletedOnTimeZone")

  }

  object Service {

    def tryGet(id: String): Future[NegotiationHistory] = tryGetByID(id).map(_.deserialize)

    def tryGetByBCNegotiationID(negotiationID: String): Future[NegotiationHistory] = tryGetByNegotiationID(negotiationID).map(_.deserialize)

    def tryGetByBuyerSellerTraderIDAndAssetID(buyerTraderID: String, sellerTraderID: String, assetID: String): Future[NegotiationHistory] = findByBuyerSellerTraderIDAndAssetID(buyerTraderID = buyerTraderID, sellerTraderID = sellerTraderID, assetID = assetID).map(_.deserialize)

    def tryGetNegotiationIDByID(id: String): Future[String] = findNegotiationID(id)

    def tryGetIDByNegotiationID(negotiationID: String): Future[String] = trGetIDByNegotiationID(negotiationID)

    def tryGetPaymentTerms(id: String): Future[PaymentTerms] = findPaymentTermsByID(id).map(paymentTerms => utilities.JSON.convertJsonStringToObject[PaymentTerms](paymentTerms))

    def tryGetDocumentList(id: String): Future[DocumentList] = findDocumentListByID(id).map(documentList => utilities.JSON.convertJsonStringToObject[DocumentList](documentList))

    def tryGetStatus(id: String): Future[String] = getStatusByID(id)

    def getAllByAssetID(assetID: String): Future[Seq[NegotiationHistory]] = findAllByAssetID(assetID).map(_.map(_.deserialize))

    def getAllCompletedNegotiationListByTraderID(traderID: String): Future[Seq[NegotiationHistory]] = findAllNegotiationsByTraderIDAndStatuses(traderID = traderID, constants.Status.Negotiation.BOTH_PARTIES_CONFIRMED).map(_.map(_.deserialize))

    def getAllCompletedNegotiationListByTraderIDs(traderIDs: Seq[String]): Future[Seq[NegotiationHistory]] = findAllNegotiationsByTraderIDsAndStatuses(traderIDs = traderIDs, constants.Status.Negotiation.BOTH_PARTIES_CONFIRMED).map(_.map(_.deserialize))

    def getAllAcceptedNegotiationListByTraderIDs(traderIDs: Seq[String]): Future[Seq[NegotiationHistory]] = findAllNegotiationsByTraderIDsAndStatuses(traderIDs = traderIDs, constants.Status.Negotiation.STARTED, constants.Status.Negotiation.BUYER_ACCEPTED_ALL_NEGOTIATION_TERMS, constants.Status.Negotiation.CONTRACT_SIGNED, constants.Status.Negotiation.BUYER_CONFIRMED_SELLER_PENDING, constants.Status.Negotiation.SELLER_CONFIRMED_BUYER_PENDING, constants.Status.Negotiation.BOTH_PARTIES_CONFIRMED).map(_.map(_.deserialize))

    def getAllAcceptedBuyNegotiationListByTraderID(traderID: String): Future[Seq[NegotiationHistory]] = findAllNegotiationsByBuyerTraderIDAndStatuses(traderID = traderID, statuses = constants.Status.Negotiation.STARTED, constants.Status.Negotiation.BUYER_ACCEPTED_ALL_NEGOTIATION_TERMS, constants.Status.Negotiation.CONTRACT_SIGNED, constants.Status.Negotiation.BUYER_ACCEPTED_ALL_NEGOTIATION_TERMS, constants.Status.Negotiation.BUYER_CONFIRMED_SELLER_PENDING, constants.Status.Negotiation.SELLER_CONFIRMED_BUYER_PENDING, constants.Status.Negotiation.BOTH_PARTIES_CONFIRMED).map(_.map(_.deserialize))

    def getAllAcceptedSellNegotiationListByTraderID(traderID: String): Future[Seq[NegotiationHistory]] = findAllNegotiationsBySellerTraderIDAndStatuses(traderID = traderID, statuses = constants.Status.Negotiation.STARTED, constants.Status.Negotiation.BUYER_ACCEPTED_ALL_NEGOTIATION_TERMS, constants.Status.Negotiation.CONTRACT_SIGNED, constants.Status.Negotiation.BUYER_CONFIRMED_SELLER_PENDING, constants.Status.Negotiation.SELLER_CONFIRMED_BUYER_PENDING, constants.Status.Negotiation.BOTH_PARTIES_CONFIRMED).map(_.map(_.deserialize))

    def getAllRejectedNegotiationListByBuyerTraderID(traderID: String): Future[Seq[NegotiationHistory]] = findAllNegotiationsByBuyerTraderIDAndStatuses(traderID = traderID, statuses = constants.Status.Negotiation.REJECTED).map(_.map(_.deserialize))

    def getAllRejectedNegotiationListBySellerTraderID(traderID: String): Future[Seq[NegotiationHistory]] = findAllNegotiationsBySellerTraderIDAndStatuses(traderID = traderID, statuses = constants.Status.Negotiation.REJECTED).map(_.map(_.deserialize))

    def getAllFailedNegotiationListBySellerTraderID(traderID: String): Future[Seq[NegotiationHistory]] = findAllNegotiationsBySellerTraderIDAndStatuses(traderID = traderID, statuses = constants.Status.Negotiation.ISSUE_ASSET_FAILED).map(_.map(_.deserialize))

    def getAllReceivedNegotiationListByTraderID(traderID: String): Future[Seq[NegotiationHistory]] = findAllNegotiationsByBuyerTraderIDAndStatuses(traderID = traderID, statuses = constants.Status.Negotiation.REQUEST_SENT).map(_.map(_.deserialize))

    def getAllSentNegotiationRequestListByTraderID(traderID: String): Future[Seq[NegotiationHistory]] = findAllNegotiationsBySellerTraderIDAndStatuses(traderID = traderID, statuses = constants.Status.Negotiation.REQUEST_SENT).map(_.map(_.deserialize))

    def getAllIncompleteNegotiationListByTraderID(traderID: String): Future[Seq[NegotiationHistory]] = findAllNegotiationsBySellerTraderIDAndStatuses(traderID = traderID, statuses = constants.Status.Negotiation.FORM_INCOMPLETE, constants.Status.Negotiation.ISSUE_ASSET_PENDING).map(_.map(_.deserialize))

    def getAllConfirmedNegotiationListByTraderID(traderID: String): Future[Seq[NegotiationHistory]] = findAllNegotiationsByTraderIDAndStatus(traderID = traderID, status = constants.Status.Negotiation.BOTH_PARTIES_CONFIRMED).map(_.map(_.deserialize))

    def getAllTradeCompletedBuyNegotiationListByTraderID(traderID: String): Future[Seq[NegotiationHistory]] = findAllNegotiationsByBuyerTraderIDAndStatuses(traderID = traderID, constants.Status.Negotiation.BOTH_PARTIES_CONFIRMED).map(_.map(_.deserialize))

    def getAllTradeCompletedSellNegotiationListByTraderID(traderID: String): Future[Seq[NegotiationHistory]] = findAllNegotiationsBySellerTraderIDAndStatuses(traderID = traderID, constants.Status.Negotiation.BOTH_PARTIES_CONFIRMED).map(_.map(_.deserialize))

    def getAllTradeCompletedBuyNegotiationListByTraderIDs(traderIDs: Seq[String]): Future[Seq[NegotiationHistory]] = findAllNegotiationsByBuyerTraderIDsAndStatuses(traderIDs = traderIDs, constants.Status.Negotiation.BOTH_PARTIES_CONFIRMED).map(_.map(_.deserialize))

    def getAllTradeCompletedSellNegotiationListByTraderIDs(traderIDs: Seq[String]): Future[Seq[NegotiationHistory]] = findAllNegotiationsBySellerTraderIDsAndStatuses(traderIDs = traderIDs, constants.Status.Negotiation.BOTH_PARTIES_CONFIRMED).map(_.map(_.deserialize))

    def tryGetBuyerTraderID(id: String): Future[String] = tryGetBuyerTraderIDByID(id)

    def tryGetSellerTraderID(id: String): Future[String] = tryGetSellerTraderIDByID(id)

    def getAllAcceptedBuyNegotiationListByTraderIDs(traderIDs: Seq[String]): Future[Seq[NegotiationHistory]] = findAllNegotiationsByBuyerTraderIDsAndStatuses(traderIDs = traderIDs, constants.Status.Negotiation.STARTED, constants.Status.Negotiation.CONTRACT_SIGNED, constants.Status.Negotiation.BUYER_ACCEPTED_ALL_NEGOTIATION_TERMS, constants.Status.Negotiation.BUYER_CONFIRMED_SELLER_PENDING, constants.Status.Negotiation.SELLER_CONFIRMED_BUYER_PENDING, constants.Status.Negotiation.BOTH_PARTIES_CONFIRMED).map(_.map(_.deserialize))

    def getAllAcceptedSellNegotiationListByTraderIDs(traderIDs: Seq[String]): Future[Seq[NegotiationHistory]] = findAllNegotiationsBySellerTraderIDsAndStatuses(traderIDs = traderIDs, constants.Status.Negotiation.STARTED, constants.Status.Negotiation.CONTRACT_SIGNED, constants.Status.Negotiation.BUYER_ACCEPTED_ALL_NEGOTIATION_TERMS, constants.Status.Negotiation.BUYER_CONFIRMED_SELLER_PENDING, constants.Status.Negotiation.SELLER_CONFIRMED_BUYER_PENDING, constants.Status.Negotiation.BOTH_PARTIES_CONFIRMED).map(_.map(_.deserialize))

    def getAllRejectedNegotiationListByBuyerTraderIDs(traderIDs: Seq[String]): Future[Seq[NegotiationHistory]] = findAllNegotiationsByBuyerTraderIDsAndStatuses(traderIDs = traderIDs, constants.Status.Negotiation.REJECTED).map(_.map(_.deserialize))

    def getAllRejectedNegotiationListBySellerTraderIDs(traderIDs: Seq[String]): Future[Seq[NegotiationHistory]] = findAllNegotiationsBySellerTraderIDsAndStatuses(traderIDs = traderIDs, constants.Status.Negotiation.REJECTED).map(_.map(_.deserialize))

    def getAllFailedNegotiationListBySellerTraderIDs(traderIDs: Seq[String]): Future[Seq[NegotiationHistory]] = findAllNegotiationsBySellerTraderIDsAndStatuses(traderIDs = traderIDs, constants.Status.Negotiation.ISSUE_ASSET_FAILED, constants.Status.Negotiation.TIMED_OUT).map(_.map(_.deserialize))

    def getAllReceivedNegotiationListByTraderIDs(traderIDs: Seq[String]): Future[Seq[NegotiationHistory]] = findAllNegotiationsByBuyerTraderIDsAndStatuses(traderIDs = traderIDs, constants.Status.Negotiation.REQUEST_SENT).map(_.map(_.deserialize))

    def getAllSentNegotiationRequestListByTraderIDs(traderIDs: Seq[String]): Future[Seq[NegotiationHistory]] = findAllNegotiationsBySellerTraderIDsAndStatuses(traderIDs = traderIDs, constants.Status.Negotiation.REQUEST_SENT).map(_.map(_.deserialize))

    def getAllIncompleteNegotiationListByTraderIDs(traderIDs: Seq[String]): Future[Seq[NegotiationHistory]] = findAllNegotiationsBySellerTraderIDsAndStatuses(traderIDs = traderIDs, constants.Status.Negotiation.FORM_INCOMPLETE, constants.Status.Negotiation.ISSUE_ASSET_PENDING).map(_.map(_.deserialize))

  }

}

