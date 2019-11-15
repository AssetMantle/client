package models.masterTransaction

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

case class NegotiationRequest(id: String, negotiationID: Option[String], buyerAccountID: String, sellerAccountID: String, pegHash: String, amount: Int, status: String, comment: Option[String])

@Singleton
class NegotiationRequests @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.MASTER_TRANSACTION_NEGOTIATION_REQUEST

  import databaseConfig.profile.api._

  private[models] val negotiationRequestTable = TableQuery[NegotiationRequestTable]

  private def add(negotiationRequest: NegotiationRequest): Future[String] = db.run((negotiationRequestTable returning negotiationRequestTable.map(_.id) += negotiationRequest).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def upsert(negotiationRequest: NegotiationRequest): Future[Int] = db.run(negotiationRequestTable.insertOrUpdate(negotiationRequest).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def find(id: String): Future[NegotiationRequest] = db.run(negotiationRequestTable.filter(_.id === id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def findNegotiationID(id: String): Future[String] = db.run(negotiationRequestTable.filter(_.id === id).map(_.negotiationID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateNegotiationIDByBuyerAccountIDAndPegHash(negotiationID: Option[String], buyerAccountID: String, pegHash: String): Future[Int] = db.run(negotiationRequestTable.filter(_.buyerAccountID === buyerAccountID).filter(_.pegHash === pegHash).map(_.negotiationID.?).update(negotiationID).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def updateAmountByNegotiationID(negotiationID: String, amount: Int): Future[Int] = db.run(negotiationRequestTable.filter(_.negotiationID === Option(negotiationID)).map(_.amount).update(amount).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def updateAmountByID(id: String, amount: Int): Future[Int] = db.run(negotiationRequestTable.filter(_.id === id).map(_.amount).update(amount).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def findNegotiationByPegHashAndBuyerAccountID(pegHash: String, buyerAccountID: String): Future[Option[NegotiationRequest]] = db.run(negotiationRequestTable.filter(_.pegHash === pegHash).filter(_.buyerAccountID === buyerAccountID).result.head.asTry).map {
    case Success(result) => Option(result)
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.info(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        None
    }
  }

  private def findIDByPegHashAndBuyerAccountID(pegHash: String, buyerAccountID: String): Future[Option[String]] = db.run(negotiationRequestTable.filter(_.pegHash === pegHash).filter(_.buyerAccountID === buyerAccountID).map(_.id).result.head.asTry).map {
    case Success(result) => Option(result)
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.info(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        None
    }
  }

  private def findNegotiationByPegHashBuyerAccountIDAndSellerAccountID(pegHash: String, buyerAccountID: String, sellerAccountID: String): Future[NegotiationRequest] = db.run(negotiationRequestTable.filter(_.pegHash === pegHash).filter(_.buyerAccountID === buyerAccountID).filter(_.sellerAccountID === sellerAccountID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def findIDByPegHashBuyerAccountIDAndSellerAccountID(pegHash: String, buyerAccountID: String, sellerAccountID: String): Future[String] = db.run(negotiationRequestTable.filter(_.pegHash === pegHash).filter(_.buyerAccountID === buyerAccountID).filter(_.sellerAccountID === sellerAccountID).map(_.id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def findIDByNegotiationID(negotiationID: String): Future[String] = db.run(negotiationRequestTable.filter(_.negotiationID.? === negotiationID).map(_.id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def checkByIDAndAccountID(id: String, accountID: String) = db.run(negotiationRequestTable.filter(_.id === id).filter(negotiationRequest => negotiationRequest.buyerAccountID === accountID || negotiationRequest.sellerAccountID === accountID).exists.result)

  private def updateStatusAndCommentByID(id: String, status: Option[String], comment: String) = db.run(negotiationRequestTable.filter(_.id === id).map(negotiation => (negotiation.status.?, negotiation.comment)).update((status, comment)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def deleteByID(id: String) = db.run(negotiationRequestTable.filter(_.id === id).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getStatusByID(id: String): Future[Option[String]] = db.run(negotiationRequestTable.filter(_.id === id).map(_.status.?).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getSellerAccountIDByID(id: String): Future[String] = db.run(negotiationRequestTable.filter(_.id === id).map(_.sellerAccountID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getBuyerAccountIDByID(id: String): Future[String] = db.run(negotiationRequestTable.filter(_.id === id).map(_.buyerAccountID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private[models] class NegotiationRequestTable(tag: Tag) extends Table[NegotiationRequest](tag, "NegotiationRequest") {

    def * = (id, negotiationID.?, buyerAccountID, sellerAccountID, pegHash, amount, status, comment.?) <> (NegotiationRequest.tupled, NegotiationRequest.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def negotiationID = column[String]("negotiationID")

    def buyerAccountID = column[String]("buyerAccountID")

    def sellerAccountID = column[String]("sellerAccountID")

    def pegHash = column[String]("pegHash")

    def amount = column[Int]("amount")

    def status = column[String]("status")

    def comment = column[String]("comment")

  }

  object Service {

    def create(id: String, negotiationID: String, amount: Int): String = Await.result(add(NegotiationRequest(utilities.IDGenerator.requestID(), null, null, null, null, amount, null, null)), Duration.Inf)

    def insertOrUpdate(requestID: String, buyerAccountID: String, sellerAccountID: String, pegHash: String, amount: Int): Future[Int] = upsert(NegotiationRequest(requestID, None, buyerAccountID, sellerAccountID, pegHash, amount, constants.Status.Asset.UNDER_NEGOTIATION, None))

    def getNegotiationByID(id: String): Future[NegotiationRequest] = find(id)

    def getNegotiationIDByID(id: String): Future[String] = findNegotiationID(id)

    def updateNegotiationID(negotiationID: String, buyerAccountID: String, pegHash: String): Future[Int] = updateNegotiationIDByBuyerAccountIDAndPegHash(Option(negotiationID), buyerAccountID, pegHash)

    def updateAmountForNegotiationID(negotiationID: String, amount: Int): Future[Int] = updateAmountByNegotiationID(negotiationID, amount)

    def updateAmountForID(id: String, amount: Int): Future[Int] = updateAmountByID(id, amount)

    def delete(id: String): Int = Await.result(deleteByID(id), Duration.Inf)

    def getStatus(id: String): Option[String] = Await.result(getStatusByID(id), Duration.Inf)

    def checkNegotiationAndAccountIDExists(id: String, accountID: String): Future[Boolean] = checkByIDAndAccountID(id, accountID)

    def getNegotiationByPegHashAndBuyerAccountID(pegHash: String, buyerAccountID: String): Future[Option[NegotiationRequest]] = findNegotiationByPegHashAndBuyerAccountID(pegHash, buyerAccountID)

    def getIDByPegHashAndBuyerAccountID(pegHash: String, buyerAccountID: String): Future[Option[String]] = findIDByPegHashAndBuyerAccountID(pegHash, buyerAccountID)

    def getNegotiationByPegHashBuyerAccountIDAndSellerAccountID(pegHash: String, buyerAccountID: String, sellerAccountID: String): Future[NegotiationRequest] = findNegotiationByPegHashBuyerAccountIDAndSellerAccountID(pegHash, buyerAccountID, sellerAccountID)

    def getIDByPegHashBuyerAccountIDAndSellerAccountID(pegHash: String, buyerAccountID: String, sellerAccountID: String): Future[String] = findIDByPegHashBuyerAccountIDAndSellerAccountID(pegHash, buyerAccountID, sellerAccountID)

    def getIDByNegotiationID(negotiationID: String): Future[String] = findIDByNegotiationID(negotiationID)

    def getSellerAccountID(id: String): Future[String] = getSellerAccountIDByID(id)

    def getBuyerAccountID(id: String): Future[String] = getBuyerAccountIDByID(id)
  }

}
