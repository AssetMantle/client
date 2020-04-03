package models.master

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import java.sql.Date
import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.internal.util.Statistics.Quantity
import scala.util.{Failure, Success}

case class Negotiation(id: String, negotiationID: Option[String] = None, ticketID: Option[String] = None, buyerTraderID: String, sellerTraderID: String, assetID: String, price: Int, quantity: Int, quantityUnit: String, time: Option[Int] = None, advancePayment: Option[Boolean] = None, advancePercentage: Option[Double] = None, credit: Option[Boolean] = None, tenure: Option[Int] = None, tentativeDate: Option[Date] = None, reference: Option[String] = None, billOfExchange: Option[Boolean] = None, coo: Option[Boolean] = None, coa: Option[Boolean] = None, otherDocuments: Option[String] = None, status: String, comment: Option[String] = None)

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

  private def updateStatusIDByID(id: String, status: String): Future[Int] = db.run(negotiationTable.filter(_.id === id).map(_.status).update(status).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def updatePaymentTermsByID(id: String, advancePayment: Option[Boolean], advancePercentage: Option[Double], credit: Option[Boolean], tenure: Option[Int], tentativeDate: Option[Date], refrence: Option[String]): Future[Int] = db.run(negotiationTable.filter(_.id === id).map(x => (x.advancePayment.?, x.advancePercentage.?, x.credit.?, x.tenure.?, x.tentativeDate.?, x.reference.?)).update((advancePayment, advancePercentage, credit, tenure, tentativeDate, refrence)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def updateDocumentsCheckListByID(id: String, billOfExchange: Option[Boolean], coo: Option[Boolean], coa: Option[Boolean], otherDocuments: Option[String]): Future[Int] = db.run(negotiationTable.filter(_.id === id).map(x => (x.billOfExchange.?, x.coo.?, x.coa.?, x.otherDocuments.?)).update((billOfExchange, coo, coa, otherDocuments)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
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

  private def updateTicketIDByID(id: String, ticketID: Option[String]): Future[Int] = db.run(negotiationTable.filter(_.id === id).map(_.ticketID.?).update(ticketID).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateNegotiationIDByID(id: String, negotiationID: Option[String]): Future[Int] = db.run(negotiationTable.filter(_.id === id).map(_.negotiationID.?).update(negotiationID).asTry).map {
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

  private def deleteByID(id: String) = db.run(negotiationTable.filter(_.id === id).delete.asTry).map {
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

  private def findAllNegotiationsByBuyerTraderIDAndStatuses(traderID: String, statuses: Seq[String]): Future[Seq[Negotiation]] = db.run(negotiationTable.filter(_.buyerTraderID === traderID).filter(_.status.inSet(statuses)).result)

  private def findAllNegotiationsBySellerTraderIDAndStatuses(traderID: String, statuses: Seq[String]): Future[Seq[Negotiation]] = db.run(negotiationTable.filter(_.sellerTraderID === traderID).filter(_.status.inSet(statuses)).result)

  private def findAllByAssetID(assetID: String): Future[Seq[Negotiation]] = db.run(negotiationTable.filter(_.assetID === assetID).result)

  private[models] class NegotiationTable(tag: Tag) extends Table[Negotiation](tag, "Negotiation") {

    def * = (id, negotiationID.?, ticketID.?, buyerTraderID, sellerTraderID, assetID, price, quantity, quantityUnit, time.?, advancePayment.?, advancePercentage.?, credit.?, tenure.?, tentativeDate.?, reference.?, billOfExchange.?, coo.?, coa.?, otherDocuments.?, status, comment.?) <> (Negotiation.tupled, Negotiation.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def negotiationID = column[String]("negotiationID")

    def ticketID = column[String]("ticketID")

    def buyerTraderID = column[String]("buyerTraderID")

    def sellerTraderID = column[String]("sellerTraderID")

    def assetID = column[String]("assetID")

    def price = column[Int]("price")

    def quantity = column[Int]("quantity")

    def quantityUnit = column[String]("quantityUnit")

    def time = column[Int]("time")

    def advancePayment = column[Boolean]("advancePayment")

    def advancePercentage = column[Double]("advancePercentage")

    def credit = column[Boolean]("credit")

    def tenure = column[Int]("tenure")

    def tentativeDate = column[Date]("tentativeDate")

    def reference = column[String]("reference")

    def billOfExchange = column[Boolean]("billOfExchange")

    def coo = column[Boolean]("coo")

    def coa = column[Boolean]("coa")

    def otherDocuments = column[String]("otherDocuments")

    def status = column[String]("status")

    def comment = column[String]("comment")

  }

  object Service {

    def create(buyerTraderID: String, sellerTraderID: String, assetID: String, price: Int, quantity: Int, quantityUnit: String, status: String): Future[String] = add(Negotiation(id = utilities.IDGenerator.requestID(), buyerTraderID = buyerTraderID, sellerTraderID = sellerTraderID, assetID = assetID, price = price, quantity = quantity, quantityUnit = quantityUnit, status = status))

    def tryGet(id: String): Future[Negotiation] = find(id)

    def tryGetByNegotiationID(negotiationID: String): Future[Negotiation] = findByNegotiationID(negotiationID)

    def tryGetByTicketID(ticketID: String): Future[Negotiation] = findByTicketID(ticketID)

    def tryGetNegotiationIDByID(id: String): Future[String] = findNegotiationID(id)

    def updatePaymentTerms(id: String, advancePayment: Boolean, advancePercentage: Option[Double], credit: Boolean, tenure: Option[Int], tentativeDate: Option[Date], refrence: Option[String]): Future[Int] = updatePaymentTermsByID(id = id, advancePayment = Option(advancePayment), advancePercentage = advancePercentage, credit = Option(credit), tenure = tenure, tentativeDate = tentativeDate, refrence = refrence)

    def updateDocumentsCheckList(id: String, billOfExchange: Boolean, coo: Boolean, coa: Boolean, otherDocuments: Option[String]): Future[Int] = updateDocumentsCheckListByID(id = id, billOfExchange = Option(billOfExchange), coo = Option(coo), coa = Option(coa), otherDocuments = otherDocuments)

    def markStatusRequestSent(id: String): Future[Int] = updateStatusIDByID(id = id, status = constants.Status.Negotiation.REQUEST_SENT)

    def markStatusIssueAssetRequestFailed(id: String): Future[Int] = updateStatusIDByID(id = id, status = constants.Status.Negotiation.ISSUE_ASSET_FAILED)

    def markStatusIssueAssetPendingRequestSent(id: String): Future[Int] = updateStatusIDByID(id = id, status = constants.Status.Negotiation.REQUEST_SENDING_WAITING_FOR_ISSUE_ASSET)

    def tryGetStatus(id: String): Future[String] = getStatusByID(id)

    def getAllBuyerOnGoingNegotiationsByTraderID(traderID: String): Future[Seq[Negotiation]] = findAllNegotiationsByBuyerTraderIDAndStatuses(traderID = traderID, statuses = Seq(constants.Status.Negotiation.NEGOTIATION_STARTED, constants.Status.Negotiation.BUYER_CONFIRMED_BID_SELLER_PENDING, constants.Status.Negotiation.SELLER_CONFIRMED_BID_BUYER_PENDING, constants.Status.Negotiation.BOTH_PARTY_CONFIRMED_BID))

    def getAllSellerOnGoingNegotiationsByTraderID(traderID: String): Future[Seq[Negotiation]] = findAllNegotiationsBySellerTraderIDAndStatuses(traderID = traderID, statuses = Seq(constants.Status.Negotiation.NEGOTIATION_STARTED, constants.Status.Negotiation.BUYER_CONFIRMED_BID_SELLER_PENDING, constants.Status.Negotiation.SELLER_CONFIRMED_BID_BUYER_PENDING, constants.Status.Negotiation.BOTH_PARTY_CONFIRMED_BID))

    def getAllRejectedNegotiationListByBuyerTraderID(traderID: String): Future[Seq[Negotiation]] = findAllNegotiationsByBuyerTraderIDAndStatuses(traderID = traderID, statuses = Seq(constants.Status.Negotiation.REJECTED))

    def getAllRejectedNegotiationListBySellerTraderID(traderID: String): Future[Seq[Negotiation]] = findAllNegotiationsBySellerTraderIDAndStatuses(traderID = traderID, statuses = Seq(constants.Status.Negotiation.REJECTED))

    def getAllFailedNegotiationListBySellerTraderID(traderID: String): Future[Seq[Negotiation]] = findAllNegotiationsBySellerTraderIDAndStatuses(traderID = traderID, statuses = Seq(constants.Status.Negotiation.ISSUE_ASSET_FAILED, constants.Status.Negotiation.TIMED_OUT))

    def getAllBuyerNewIncomingNegotiationListByTraderID(traderID: String): Future[Seq[Negotiation]] = findAllNegotiationsByBuyerTraderIDAndStatuses(traderID = traderID, statuses = Seq(constants.Status.Negotiation.REQUEST_SENT))

    def getAllSellerSentNegotiationRequestListByTraderID(traderID: String): Future[Seq[Negotiation]] = findAllNegotiationsBySellerTraderIDAndStatuses(traderID = traderID, statuses = Seq(constants.Status.Negotiation.REQUEST_SENT))

    def getAllSellerIncompleteNegotiationListByTraderID(traderID: String): Future[Seq[Negotiation]] = findAllNegotiationsBySellerTraderIDAndStatuses(traderID = traderID, statuses = Seq(constants.Status.Negotiation.FORM_INCOMPLETE, constants.Status.Negotiation.ISSUE_ASSET_PENDING))

    def getAllByAssetID(assetID: String): Future[Seq[Negotiation]] = findAllByAssetID(assetID)

    def updateTicketID(id: String, ticketID: String): Future[Int] = updateTicketIDByID(id = id, ticketID = Option(ticketID))

    def updateNegotiationID(id: String, negotiationID: String): Future[Int] = updateNegotiationIDByID(id = id, negotiationID = Option(negotiationID))

    def updatePriceAndQuantity(id: String, price: Int, quantity: Int): Future[Int] = updatePriceAndQuantityByID(id = id, price = price, quantity = quantity)

    def markRequestRejected(id: String, comment: Option[String]): Future[Int] = updateStatusAndCommentByID(id = id, status = constants.Status.Negotiation.REJECTED, comment = comment)

  }

}
