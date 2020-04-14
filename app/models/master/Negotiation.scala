package models.master

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import java.sql.Date
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Negotiation(id: String, negotiationID: Option[String] = None, ticketID: Option[String] = None, buyerTraderID: String, sellerTraderID: String, assetID: String, assetDescription: String, price: Int, quantity: Int, quantityUnit: String, shippingPeriod: Int, time: Option[Int] = None, buyerAcceptedAssetDetails: BuyerAcceptedAssetDetails, paymentTerms: PaymentTerms, buyerAcceptedPaymentTerms: BuyerAcceptedPaymentTerms, documentsCheckList: DocumentsCheckList, buyerAcceptedDocumentsCheckList: BuyerAcceptedDocumentsCheckList, chatID: Option[String] = None, status: String, comment: Option[String] = None)

case class PaymentTerms(advancePayment: Option[Boolean] = None, advancePercentage: Option[Double] = None, credit: Option[Boolean] = None, tenure: Option[Int] = None, tentativeDate: Option[Date] = None, reference: Option[String] = None)

case class DocumentsCheckList(billOfExchange: Option[Boolean] = None, coo: Option[Boolean] = None, coa: Option[Boolean] = None, otherDocuments: Option[String] = None)

case class BuyerAcceptedAssetDetails(description: Boolean = false, price: Boolean = false, quantity: Boolean = false, shippingPeriod: Boolean = false)

case class BuyerAcceptedPaymentTerms(advancePayment: Boolean = false, credit: Boolean = false)

case class BuyerAcceptedDocumentsCheckList(billOfExchange: Boolean = false, coo: Boolean = false, coa: Boolean = false, otherDocuments: Boolean = false)

@Singleton
class Negotiations @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.MASTER_NEGOTIATION

  import databaseConfig.profile.api._

  private[models] val negotiationTable = TableQuery[NegotiationTable]

  private def add(negotiation: Negotiation): Future[String] = db.run((negotiationTable returning negotiationTable.map(_.id) += negotiation).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def find(id: String): Future[Negotiation] = db.run(negotiationTable.filter(_.id === id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def findByNegotiationID(negotiationID: String): Future[Negotiation] = db.run(negotiationTable.filter(_.negotiationID === negotiationID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def findByTicketID(ticketID: String): Future[Negotiation] = db.run(negotiationTable.filter(_.ticketID === ticketID).result.head.asTry).map {
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

  private def findPaymentTermsByID(id: String): Future[PaymentTerms] = db.run(negotiationTable.filter(_.id === id).result.head.asTry).map {
    case Success(result) => result.paymentTerms
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def findDocumentsCheckListByID(id: String): Future[DocumentsCheckList] = db.run(negotiationTable.filter(_.id === id).result.head.asTry).map {
    case Success(result) => result.documentsCheckList
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def findBuyerAcceptedAssetDetailsByID(id: String): Future[BuyerAcceptedAssetDetails] = db.run(negotiationTable.filter(_.id === id).result.head.asTry).map {
    case Success(result) => result.buyerAcceptedAssetDetails
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def findBuyerAcceptedPaymentTermsByID(id: String): Future[BuyerAcceptedPaymentTerms] = db.run(negotiationTable.filter(_.id === id).result.head.asTry).map {
    case Success(result) => result.buyerAcceptedPaymentTerms
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def findBuyerAcceptedDocumentsCheckListByID(id: String): Future[BuyerAcceptedDocumentsCheckList] = db.run(negotiationTable.filter(_.id === id).result.head.asTry).map {
    case Success(result) => result.buyerAcceptedDocumentsCheckList
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateStatusByID(id: String, status: String): Future[Int] = db.run(negotiationTable.filter(_.id === id).map(_.status).update(status).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateStatusByNegotiationID(negotiationID: String, status: String): Future[Int] = db.run(negotiationTable.filter(_.negotiationID === negotiationID).map(_.status).update(status).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updatePriceAndQuantityByID(id: String, price: Int, quantity: Int): Future[Int] = db.run(negotiationTable.filter(_.id === id).map(x => (x.price, x.quantity)).update((price, quantity)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateAssetTermsByID(id: String, description: String, price: Int, quantity: Int, shippingPeriod: Int, buyerAcceptedAssetDetails: BuyerAcceptedAssetDetails): Future[Int] = db.run(negotiationTable.filter(_.id === id).map(x => (x.assetDescription, x.price, x.quantity, x.shippingPeriod, x.buyerAcceptedAssetDescription, x.buyerAcceptedPrice, x.buyerAcceptedQuantity, x.buyerAcceptedShippingPeriod)).update((description, price, quantity, shippingPeriod, buyerAcceptedAssetDetails.description, buyerAcceptedAssetDetails.price, buyerAcceptedAssetDetails.quantity, buyerAcceptedAssetDetails.shippingPeriod)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updatePaymentTermsByID(id: String, advancePayment: Option[Boolean], advancePercentage: Option[Double], credit: Option[Boolean], tenure: Option[Int], tentativeDate: Option[Date], refrence: Option[String], buyerAcceptedPaymentTerms: BuyerAcceptedPaymentTerms): Future[Int] = db.run(negotiationTable.filter(_.id === id).map(x => (x.advancePayment.?, x.advancePercentage.?, x.credit.?, x.tenure.?, x.tentativeDate.?, x.reference.?, x.buyerAcceptedAdvancePayment, x.buyerAcceptedCredit)).update((advancePayment, advancePercentage, credit, tenure, tentativeDate, refrence, buyerAcceptedPaymentTerms.advancePayment, buyerAcceptedPaymentTerms.credit)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateDocumentsCheckListByID(id: String, billOfExchange: Option[Boolean], coo: Option[Boolean], coa: Option[Boolean], otherDocuments: Option[String], buyerAcceptedDocumentsCheckList: BuyerAcceptedDocumentsCheckList): Future[Int] = db.run(negotiationTable.filter(_.id === id).map(x => (x.billOfExchange.?, x.coo.?, x.coa.?, x.otherDocuments.?, x.buyerAcceptedBillOfExchange, x.buyerAcceptedCOO, x.buyerAcceptedCOA, x.buyerAcceptedOtherDocuments)).update((billOfExchange, coo, coa, otherDocuments, buyerAcceptedDocumentsCheckList.billOfExchange, buyerAcceptedDocumentsCheckList.coo, buyerAcceptedDocumentsCheckList.coa, buyerAcceptedDocumentsCheckList.otherDocuments)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateStatusAndCommentByID(id: String, status: String, comment: Option[String]): Future[Int] = db.run(negotiationTable.filter(_.id === id).map(negotiation => (negotiation.status, negotiation.comment.?)).update((status, comment)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateChatIDByID(id: String, chatID: String): Future[Int] = db.run(negotiationTable.filter(_.id === id).map(_.chatID).update(chatID).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateTicketIDByID(id: String, ticketID: Option[String]): Future[Int] = db.run(negotiationTable.filter(_.id === id).map(_.ticketID.?).update(ticketID).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateNegotiationIDAndStatusByID(id: String, negotiationID: String, status: String): Future[Int] = db.run(negotiationTable.filter(_.id === id).map(x => (x.negotiationID, x.status)).update((negotiationID, status)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
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
  private def findAllNegotiationsByTraderIDsAndStatuses(traderIDs: Seq[String], statuses: String*): Future[Seq[Negotiation]] = db.run((negotiationTable.filter(_.buyerTraderID inSet traderIDs) union negotiationTable.filter(_.sellerTraderID inSet traderIDs)).filter(_.status.inSet(statuses)).result)

  private def findAllNegotiationsByBuyerTraderIDAndStatuses(traderID: String, statuses: String*): Future[Seq[Negotiation]] = db.run(negotiationTable.filter(_.buyerTraderID === traderID).filter(_.status.inSet(statuses)).result)

  private def findAllNegotiationsBySellerTraderIDAndStatuses(traderID: String, statuses: String*): Future[Seq[Negotiation]] = db.run(negotiationTable.filter(_.sellerTraderID === traderID).filter(_.status.inSet(statuses)).result)

  private def findAllNegotiationsByTraderIDAndStatus(traderID: String, status: String): Future[Seq[Negotiation]] = db.run(negotiationTable.filter(x => x.sellerTraderID === traderID || x.buyerTraderID === traderID).filter(_.status === status).result)

  private def findAllNegotiationsByBuyerTraderIDsAndStatuses(traderIDs: Seq[String], statuses: String*): Future[Seq[Negotiation]] = db.run(negotiationTable.filter(_.buyerTraderID.inSet(traderIDs)).filter(_.status.inSet(statuses)).result)

  private def findAllNegotiationsBySellerTraderIDsAndStatuses(traderIDs: Seq[String], statuses: String*): Future[Seq[Negotiation]] = db.run(negotiationTable.filter(_.sellerTraderID.inSet(traderIDs)).filter(_.status.inSet(statuses)).result)

  private def findAllByAssetID(assetID: String): Future[Seq[Negotiation]] = db.run(negotiationTable.filter(_.assetID === assetID).result)

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

  private def updateBuyerAcceptedShippingPeriodByID(id: String, buyerAcceptedShippingPeriod: Boolean): Future[Int] = db.run(negotiationTable.filter(_.id === id).map(_.buyerAcceptedShippingPeriod).update(buyerAcceptedShippingPeriod).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateBuyerAcceptedAdvancePaymentByID(id: String, buyerAcceptedAdvancePayment: Boolean): Future[Int] = db.run(negotiationTable.filter(_.id === id).map(_.buyerAcceptedAdvancePayment).update(buyerAcceptedAdvancePayment).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateBuyerAcceptedCreditByID(id: String, buyerAcceptedCredit: Boolean): Future[Int] = db.run(negotiationTable.filter(_.id === id).map(_.buyerAcceptedCredit).update(buyerAcceptedCredit).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateBuyerAcceptedBillOfExchangeByID(id: String, buyerAcceptedBillOfExchange: Boolean): Future[Int] = db.run(negotiationTable.filter(_.id === id).map(_.buyerAcceptedBillOfExchange).update(buyerAcceptedBillOfExchange).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateBuyerAcceptedCOOByID(id: String, buyerAcceptedCOO: Boolean): Future[Int] = db.run(negotiationTable.filter(_.id === id).map(_.buyerAcceptedCOO).update(buyerAcceptedCOO).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateBuyerAcceptedCOAByID(id: String, buyerAcceptedCOA: Boolean): Future[Int] = db.run(negotiationTable.filter(_.id === id).map(_.buyerAcceptedCOA).update(buyerAcceptedCOA).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateBuyerAcceptedOtherDocumentsByID(id: String, buyerAcceptedOtherDocuments: Boolean): Future[Int] = db.run(negotiationTable.filter(_.id === id).map(_.buyerAcceptedOtherDocuments).update(buyerAcceptedOtherDocuments).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def checkByIDAndTraderID(id: String, traderID: String): Future[Boolean] = db.run(negotiationTable.filter(_.id === id).filter(x => x.buyerTraderID === traderID || x.sellerTraderID === traderID).exists.result)

  private[models] class NegotiationTable(tag: Tag) extends Table[Negotiation](tag, "Negotiation") {

    def * = (id, negotiationID.?, ticketID.?, buyerTraderID, sellerTraderID, assetID, assetDescription, price, quantity, quantityUnit, shippingPeriod, time.?, (buyerAcceptedAssetDescription, buyerAcceptedPrice, buyerAcceptedQuantity, buyerAcceptedShippingPeriod), (advancePayment.?, advancePercentage.?, credit.?, tenure.?, tentativeDate.?, reference.?), (buyerAcceptedAdvancePayment, buyerAcceptedCredit), (billOfExchange.?, coo.?, coa.?, otherDocuments.?), (buyerAcceptedBillOfExchange, buyerAcceptedCOO, buyerAcceptedCOA, buyerAcceptedOtherDocuments), chatID.?, status, comment.?).shaped <> ( {
      case (id, negotiationID, ticketID, buyerTraderID, sellerTraderID, assetID, assetDescription, price, quantity, quantityUnit, shippingPeriod, time, buyerAcceptedAssetDetails, paymentTerms, buyerAcceptedPaymentTerms, documentsCheckList, buyerAcceptedDocumentsCheckList, chatID, status, comment) => Negotiation(id = id, negotiationID = negotiationID, ticketID = ticketID, buyerTraderID = buyerTraderID, sellerTraderID = sellerTraderID, assetID = assetID, assetDescription = assetDescription, price = price, quantity = quantity, quantityUnit = quantityUnit, shippingPeriod = shippingPeriod, time = time, buyerAcceptedAssetDetails = BuyerAcceptedAssetDetails.tupled.apply(buyerAcceptedAssetDetails), paymentTerms = PaymentTerms.tupled.apply(paymentTerms), buyerAcceptedPaymentTerms = BuyerAcceptedPaymentTerms.tupled.apply(buyerAcceptedPaymentTerms), documentsCheckList = DocumentsCheckList.tupled.apply(documentsCheckList), buyerAcceptedDocumentsCheckList = BuyerAcceptedDocumentsCheckList.tupled.apply(buyerAcceptedDocumentsCheckList), chatID = chatID, status = status, comment = comment)
    }, { negotiation: Negotiation =>
      def f1(paymentTerms: PaymentTerms) = PaymentTerms.unapply(paymentTerms).get

      def f2(documentsCheckList: DocumentsCheckList) = DocumentsCheckList.unapply(documentsCheckList).get

      def f3(buyerAcceptedAssetDetails: BuyerAcceptedAssetDetails) = BuyerAcceptedAssetDetails.unapply(buyerAcceptedAssetDetails).get

      def f4(buyerAcceptedPaymentTerms: BuyerAcceptedPaymentTerms) = BuyerAcceptedPaymentTerms.unapply(buyerAcceptedPaymentTerms).get

      def f5(buyerAcceptedDocumentsCheckList: BuyerAcceptedDocumentsCheckList) = BuyerAcceptedDocumentsCheckList.unapply(buyerAcceptedDocumentsCheckList).get

      Some((negotiation.id, negotiation.negotiationID, negotiation.ticketID, negotiation.buyerTraderID, negotiation.sellerTraderID, negotiation.assetID, negotiation.assetDescription, negotiation.price, negotiation.quantity, negotiation.quantityUnit, negotiation.shippingPeriod, negotiation.time, f3(negotiation.buyerAcceptedAssetDetails), f1(negotiation.paymentTerms), f4(negotiation.buyerAcceptedPaymentTerms), f2(negotiation.documentsCheckList), f5(negotiation.buyerAcceptedDocumentsCheckList), negotiation.chatID, negotiation.status, negotiation.comment))
    })

    def id = column[String]("id", O.PrimaryKey)

    def negotiationID = column[String]("negotiationID")

    def ticketID = column[String]("ticketID")

    def buyerTraderID = column[String]("buyerTraderID")

    def sellerTraderID = column[String]("sellerTraderID")

    def assetID = column[String]("assetID")

    def assetDescription = column[String]("assetDescription")

    def price = column[Int]("price")

    def quantity = column[Int]("quantity")

    def quantityUnit = column[String]("quantityUnit")

    def shippingPeriod = column[Int]("shippingPeriod")

    def time = column[Int]("time")

    def buyerAcceptedAssetDescription = column[Boolean]("buyerAcceptedAssetDescription")

    def buyerAcceptedPrice = column[Boolean]("buyerAcceptedPrice")

    def buyerAcceptedQuantity = column[Boolean]("buyerAcceptedQuantity")

    def buyerAcceptedShippingPeriod = column[Boolean]("buyerAcceptedShippingPeriod")

    def advancePayment = column[Boolean]("advancePayment")

    def advancePercentage = column[Double]("advancePercentage")

    def credit = column[Boolean]("credit")

    def tenure = column[Int]("tenure")

    def tentativeDate = column[Date]("tentativeDate")

    def reference = column[String]("reference")

    def buyerAcceptedAdvancePayment = column[Boolean]("buyerAcceptedAdvancePayment")

    def buyerAcceptedCredit = column[Boolean]("buyerAcceptedCredit")

    def billOfExchange = column[Boolean]("billOfExchange")

    def coo = column[Boolean]("coo")

    def coa = column[Boolean]("coa")

    def otherDocuments = column[String]("otherDocuments")

    def buyerAcceptedBillOfExchange = column[Boolean]("buyerAcceptedBillOfExchange")

    def buyerAcceptedCOO = column[Boolean]("buyerAcceptedCOO")

    def buyerAcceptedCOA = column[Boolean]("buyerAcceptedCOA")

    def buyerAcceptedOtherDocuments = column[Boolean]("buyerAcceptedOtherDocuments")

    def chatID = column[String]("chatID")

    def status = column[String]("status")

    def comment = column[String]("comment")

  }

  object Service {

    def createWithIssueAssetPending(buyerTraderID: String, sellerTraderID: String, assetID: String, description: String, price: Int, quantity: Int, quantityUnit: String, shippingPeriod: Int): Future[String] = add(Negotiation(id = utilities.IDGenerator.requestID(), buyerTraderID = buyerTraderID, sellerTraderID = sellerTraderID, assetID = assetID, assetDescription = description, price = price, quantity = quantity, quantityUnit = quantityUnit, shippingPeriod = shippingPeriod, buyerAcceptedAssetDetails = BuyerAcceptedAssetDetails(), paymentTerms = PaymentTerms(), buyerAcceptedPaymentTerms = BuyerAcceptedPaymentTerms(), documentsCheckList = DocumentsCheckList(), buyerAcceptedDocumentsCheckList = BuyerAcceptedDocumentsCheckList(), status = constants.Status.Negotiation.ISSUE_ASSET_PENDING))

    def createWithFormIncomplete(buyerTraderID: String, sellerTraderID: String, assetID: String, description: String, price: Int, quantity: Int, quantityUnit: String, shippingPeriod: Int): Future[String] = add(Negotiation(id = utilities.IDGenerator.requestID(), buyerTraderID = buyerTraderID, sellerTraderID = sellerTraderID, assetID = assetID, assetDescription = description, price = price, quantity = quantity, quantityUnit = quantityUnit, shippingPeriod = shippingPeriod, buyerAcceptedAssetDetails = BuyerAcceptedAssetDetails(), paymentTerms = PaymentTerms(), buyerAcceptedPaymentTerms = BuyerAcceptedPaymentTerms(), documentsCheckList = DocumentsCheckList(), buyerAcceptedDocumentsCheckList = BuyerAcceptedDocumentsCheckList(), status = constants.Status.Negotiation.FORM_INCOMPLETE))

    def tryGet(id: String): Future[Negotiation] = find(id)

    def tryGetByNegotiationID(negotiationID: String): Future[Negotiation] = findByNegotiationID(negotiationID)

    def tryGetByTicketID(ticketID: String): Future[Negotiation] = findByTicketID(ticketID)

    def tryGetNegotiationIDByID(id: String): Future[String] = findNegotiationID(id)

    def tryGetPaymentTerms(id: String): Future[PaymentTerms] = findPaymentTermsByID(id)

    def updatePaymentTerms(id: String, advancePayment: Boolean, advancePercentage: Option[Double], credit: Boolean, tenure: Option[Int], tentativeDate: Option[Date], refrence: Option[String]): Future[Int] = updatePaymentTermsByID(id = id, advancePayment = Option(advancePayment), advancePercentage = advancePercentage, credit = Option(credit), tenure = tenure, tentativeDate = tentativeDate, refrence = refrence, buyerAcceptedPaymentTerms = BuyerAcceptedPaymentTerms())

    def tryGetDocumentsCheckList(id: String): Future[DocumentsCheckList] = findDocumentsCheckListByID(id)

    def updateDocumentsCheckList(id: String, billOfExchange: Boolean, coo: Boolean, coa: Boolean, otherDocuments: Option[String]): Future[Int] = updateDocumentsCheckListByID(id = id, billOfExchange = Option(billOfExchange), coo = Option(coo), coa = Option(coa), otherDocuments = otherDocuments, buyerAcceptedDocumentsCheckList = BuyerAcceptedDocumentsCheckList())

    def updatePriceAndQuantity(id: String, price: Int, quantity: Int): Future[Int] = updatePriceAndQuantityByID(id = id, price = price, quantity = quantity)

    def updateAssetTerms(id: String, description: String, price: Int, quantity: Int, shippingPeriod: Int): Future[Int] = updateAssetTermsByID(id = id, description = description, price = price, quantity = quantity, shippingPeriod = shippingPeriod, buyerAcceptedAssetDetails = BuyerAcceptedAssetDetails())

    def tryGetBuyerAcceptedAssetDetails(id: String): Future[BuyerAcceptedAssetDetails] = findBuyerAcceptedAssetDetailsByID(id)

    def tryGetBuyerAcceptedPaymentTerms(id: String): Future[BuyerAcceptedPaymentTerms] = findBuyerAcceptedPaymentTermsByID(id)

    def tryGetBuyerAcceptedDocumentsCheckList(id: String): Future[BuyerAcceptedDocumentsCheckList] = findBuyerAcceptedDocumentsCheckListByID(id)

    def updateTicketID(id: String, ticketID: String): Future[Int] = updateTicketIDByID(id = id, ticketID = Option(ticketID))

    def tryGetStatus(id: String): Future[String] = getStatusByID(id)

    def markStatusRequestSent(id: String): Future[Int] = updateStatusByID(id = id, status = constants.Status.Negotiation.REQUEST_SENT)

    def markStatusIssueAssetRequestFailed(id: String): Future[Int] = updateStatusByID(id = id, status = constants.Status.Negotiation.ISSUE_ASSET_FAILED)

    def markStatusIssueAssetPendingRequestSent(id: String): Future[Int] = updateStatusByID(id = id, status = constants.Status.Negotiation.REQUEST_SENDING_WAITING_FOR_ISSUE_ASSET)

    def markTradeCompletedByNegotiationID(negotiationID: String): Future[Int] = updateStatusByNegotiationID(negotiationID = negotiationID, status = constants.Status.Negotiation.TRADE_COMPLETED)

    def markRequestRejected(id: String, comment: Option[String]): Future[Int] = updateStatusAndCommentByID(id = id, status = constants.Status.Negotiation.REJECTED, comment = comment)

    def markAcceptedAndUpdateNegotiationID(id: String, negotiationID: String): Future[Int] = updateNegotiationIDAndStatusByID(id = id, negotiationID = negotiationID, status = constants.Status.Negotiation.NEGOTIATION_STARTED)

    def getAllByAssetID(assetID: String): Future[Seq[Negotiation]] = findAllByAssetID(assetID)

    def getAllAcceptedNegotiationListByTraderIDs(traderIDs: Seq[String]): Future[Seq[Negotiation]] = findAllNegotiationsByTraderIDsAndStatuses(traderIDs = traderIDs, constants.Status.Negotiation.NEGOTIATION_STARTED, constants.Status.Negotiation.BUYER_CONFIRMED_PRICE_SELLER_PENDING, constants.Status.Negotiation.SELLER_CONFIRMED_PRICE_BUYER_PENDING, constants.Status.Negotiation.BOTH_PARTY_CONFIRMED_PRICE)

    def getAllAcceptedBuyNegotiationListByTraderID(traderID: String): Future[Seq[Negotiation]] = findAllNegotiationsByBuyerTraderIDAndStatuses(traderID = traderID, constants.Status.Negotiation.NEGOTIATION_STARTED, constants.Status.Negotiation.BUYER_CONFIRMED_PRICE_SELLER_PENDING, constants.Status.Negotiation.SELLER_CONFIRMED_PRICE_BUYER_PENDING, constants.Status.Negotiation.BOTH_PARTY_CONFIRMED_PRICE)

    def getAllAcceptedSellNegotiationListByTraderID(traderID: String): Future[Seq[Negotiation]] = findAllNegotiationsBySellerTraderIDAndStatuses(traderID = traderID, constants.Status.Negotiation.NEGOTIATION_STARTED, constants.Status.Negotiation.BUYER_CONFIRMED_PRICE_SELLER_PENDING, constants.Status.Negotiation.SELLER_CONFIRMED_PRICE_BUYER_PENDING, constants.Status.Negotiation.BOTH_PARTY_CONFIRMED_PRICE)

    def getAllRejectedNegotiationListByBuyerTraderID(traderID: String): Future[Seq[Negotiation]] = findAllNegotiationsByBuyerTraderIDAndStatuses(traderID = traderID, constants.Status.Negotiation.REJECTED)

    def getAllRejectedNegotiationListBySellerTraderID(traderID: String): Future[Seq[Negotiation]] = findAllNegotiationsBySellerTraderIDAndStatuses(traderID = traderID, constants.Status.Negotiation.REJECTED)

    def getAllFailedNegotiationListBySellerTraderID(traderID: String): Future[Seq[Negotiation]] = findAllNegotiationsBySellerTraderIDAndStatuses(traderID = traderID, constants.Status.Negotiation.ISSUE_ASSET_FAILED, constants.Status.Negotiation.TIMED_OUT)

    def getAllReceivedNegotiationListByTraderID(traderID: String): Future[Seq[Negotiation]] = findAllNegotiationsByBuyerTraderIDAndStatuses(traderID = traderID, constants.Status.Negotiation.REQUEST_SENT)

    def getAllSentNegotiationRequestListByTraderID(traderID: String): Future[Seq[Negotiation]] = findAllNegotiationsBySellerTraderIDAndStatuses(traderID = traderID, constants.Status.Negotiation.REQUEST_SENT)

    def getAllIncompleteNegotiationListByTraderID(traderID: String): Future[Seq[Negotiation]] = findAllNegotiationsBySellerTraderIDAndStatuses(traderID = traderID, constants.Status.Negotiation.FORM_INCOMPLETE, constants.Status.Negotiation.ISSUE_ASSET_PENDING)

    def getAllConfirmedNegotiationListByTraderID(traderID: String): Future[Seq[Negotiation]] = findAllNegotiationsByTraderIDAndStatus(traderID = traderID, status = constants.Status.Negotiation.BOTH_PARTY_CONFIRMED_PRICE)

    def getAllTradeCompletedBuyNegotiationListByTraderID(traderID: String): Future[Seq[Negotiation]] = findAllNegotiationsByBuyerTraderIDAndStatuses(traderID = traderID, constants.Status.Negotiation.TRADE_COMPLETED)

    def getAllTradeCompletedSellNegotiationListByTraderID(traderID: String): Future[Seq[Negotiation]] = findAllNegotiationsBySellerTraderIDAndStatuses(traderID = traderID, constants.Status.Negotiation.TRADE_COMPLETED)

    def getAllTradeCompletedBuyNegotiationListByTraderIDs(traderIDs: Seq[String]): Future[Seq[Negotiation]] = findAllNegotiationsByBuyerTraderIDsAndStatuses(traderIDs = traderIDs, constants.Status.Negotiation.TRADE_COMPLETED)

    def getAllTradeCompletedSellNegotiationListByTraderIDs(traderIDs: Seq[String]): Future[Seq[Negotiation]] = findAllNegotiationsBySellerTraderIDsAndStatuses(traderIDs = traderIDs, constants.Status.Negotiation.TRADE_COMPLETED)

    def insertChatID(id: String, chatID: String): Future[Int] = updateChatIDByID(id = id, chatID = chatID)

    def updateBuyerAcceptedAssetDescription(id: String, buyerAcceptedAssetDescription: Boolean): Future[Int] = updateBuyerAcceptedAssetDescriptionByID(id = id, buyerAcceptedAssetDescription = buyerAcceptedAssetDescription)

    def updateBuyerAcceptedPrice(id: String, buyerAcceptedPrice: Boolean): Future[Int] = updateBuyerAcceptedPriceByID(id = id, buyerAcceptedPrice = buyerAcceptedPrice)

    def updateBuyerAcceptedQuantity(id: String, buyerAcceptedQuantity: Boolean): Future[Int] = updateBuyerAcceptedQuantityByID(id = id, buyerAcceptedQuantity = buyerAcceptedQuantity)

    def updateBuyerAcceptedShippingPeriod(id: String, buyerAcceptedShippingPeriod: Boolean): Future[Int] = updateBuyerAcceptedShippingPeriodByID(id = id, buyerAcceptedShippingPeriod = buyerAcceptedShippingPeriod)

    def updateBuyerAcceptedAdvancePayment(id: String, buyerAcceptedAdvancePayment: Boolean): Future[Int] = updateBuyerAcceptedAdvancePaymentByID(id = id, buyerAcceptedAdvancePayment = buyerAcceptedAdvancePayment)

    def updateBuyerAcceptedCredit(id: String, buyerAcceptedCredit: Boolean): Future[Int] = updateBuyerAcceptedCreditByID(id = id, buyerAcceptedCredit = buyerAcceptedCredit)

    def updateBuyerAcceptedBillOfExchange(id: String, buyerAcceptedBillOfExchange: Boolean): Future[Int] = updateBuyerAcceptedBillOfExchangeByID(id = id, buyerAcceptedBillOfExchange = buyerAcceptedBillOfExchange)

    def updateBuyerAcceptedCOO(id: String, buyerAcceptedCOO: Boolean): Future[Int] = updateBuyerAcceptedCOOByID(id = id, buyerAcceptedCOO = buyerAcceptedCOO)

    def updateBuyerAcceptedCOA(id: String, buyerAcceptedCOA: Boolean): Future[Int] = updateBuyerAcceptedCOAByID(id = id, buyerAcceptedCOA = buyerAcceptedCOA)

    def updateBuyerAcceptedOtherDocuments(id: String, buyerAcceptedOtherDocuments: Boolean): Future[Int] = updateBuyerAcceptedOtherDocumentsByID(id = id, buyerAcceptedOtherDocuments = buyerAcceptedOtherDocuments)

    def checkTraderNegotiationExists(id: String, traderID: String): Future[Boolean] = checkByIDAndTraderID(id = id, traderID = traderID)
  }

}

