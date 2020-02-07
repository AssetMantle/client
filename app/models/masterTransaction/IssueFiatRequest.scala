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

case class IssueFiatRequest(id: String, accountID: String, transactionID: String, transactionAmount: Int, gas: Option[Int] = None, status: Option[Boolean] = None, rtcbStatus: Boolean = false, ticketID: Option[String] = None, comment: Option[String] = None)

@Singleton
class IssueFiatRequests @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.MASTER_TRANSACTION_ISSUE_FIAT_REQUESTS

  import databaseConfig.profile.api._

  private[models] val issueFiatRequestTable = TableQuery[IssueFiatRequestTable]

  private def add(issueFiatRequest: IssueFiatRequest): Future[String] = db.run((issueFiatRequestTable returning issueFiatRequestTable.map(_.id) += issueFiatRequest).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def findByID(id: String): Future[IssueFiatRequest] = db.run(issueFiatRequestTable.filter(_.id === id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateTicketIDGasAndStatusByID(id: String, ticketID: String, gas: Option[Int], status: Option[Boolean]): Future[Int] = db.run(issueFiatRequestTable.filter(_.id === id).map(issueFiat => (issueFiat.ticketID, issueFiat.gas.?, issueFiat.status.?)).update(ticketID, gas, status).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }


  private def updateStatusAndCommentByID(id: String, status: Option[Boolean], comment: String) = db.run(issueFiatRequestTable.filter(_.id === id).map(issueFiatRequest => (issueFiatRequest.status.?, issueFiatRequest.comment)).update((status, comment)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateRTCBStatusByTransactionID(transactionID: String, rtcbStatus: Boolean): Future[Int] = db.run(issueFiatRequestTable.filter(_.transactionID === transactionID).map(_.rtcbStatus).update(rtcbStatus).asTry).map {
    case Success(result) => if (result > 0) {
      result
    } else {
        logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, new NoSuchElementException("ID NOT FOUND, NO ROW UPDATED FOR TRANSACTION ID = "+ transactionID))
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def getIssueFiatRequestsWithNullStatusAndTrueRTCBStatus(accountIDs: Seq[String]): Future[Seq[IssueFiatRequest]] = db.run(issueFiatRequestTable.filter(_.accountID.inSet(accountIDs)).filter(_.status.?.isEmpty).filter(_.rtcbStatus === true).result)

  private def deleteByID(id: String): Future[Int] = db.run(issueFiatRequestTable.filter(_.id === id).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getStatusByID(id: String): Future[Option[Boolean]] = db.run(issueFiatRequestTable.filter(_.id === id).map(_.status.?).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }


  private[models] class IssueFiatRequestTable(tag: Tag) extends Table[IssueFiatRequest](tag, "IssueFiatRequest") {

    def * = (id, accountID, transactionID, transactionAmount, gas.?, status.?, rtcbStatus, ticketID.?, comment.?) <> (IssueFiatRequest.tupled, IssueFiatRequest.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def accountID = column[String]("accountID")

    def transactionID = column[String]("transactionID")

    def transactionAmount = column[Int]("transactionAmount")

    def gas = column[Int]("gas")

    def status = column[Boolean]("status")

    def rtcbStatus = column[Boolean]("rtcbStatus")

    def ticketID = column[String]("ticketID")

    def comment = column[String]("comment")

  }

  object Service {

    def create(accountID: String, transactionID: String, transactionAmount: Int): Future[String] = add(IssueFiatRequest(id = utilities.IDGenerator.requestID, accountID = accountID, transactionID = transactionID, transactionAmount = transactionAmount))

    def reject(id: String, comment: String): Future[Int] = updateStatusAndCommentByID(id = id, status = Option(false), comment = comment)

    def accept(requestID: String, ticketID: String, gas: Int): Future[Int] = updateTicketIDGasAndStatusByID(requestID, ticketID, gas = Option(gas), status = Option(true))

    def getPendingIssueFiatRequests(accountIDs: Seq[String]): Future[Seq[IssueFiatRequest]] = getIssueFiatRequestsWithNullStatusAndTrueRTCBStatus(accountIDs)

    def delete(id: String): Future[Int] = deleteByID(id)

    def getStatus(id: String): Future[Option[Boolean]] = getStatusByID(id)

    def markRTCBReceived(transactionID: String): Future[Int] = updateRTCBStatusByTransactionID(transactionID, rtcbStatus = true)
  }

}
