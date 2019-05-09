package models.masterTransaction

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Random, Success}

case class IssueFiatRequest(id: String, accountID: String, transactionID: String, transactionAmount: Int, gas: Option[Int], status: Option[Boolean], ticketID: Option[String], comment: Option[String])

@Singleton
class IssueFiatRequests @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.MASTER_TRANSACTION_ISSUE_FIAT_REQUESTS

  import databaseConfig.profile.api._

  private[models] val issueFiatRequestTable = TableQuery[IssueFiatRequestTable]

  private def add(issueFiatRequest: IssueFiatRequest)(implicit executionContext: ExecutionContext): Future[String] = db.run((issueFiatRequestTable returning issueFiatRequestTable.map(_.id) += issueFiatRequest).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
    }
  }

  private def findByID(id: String)(implicit executionContext: ExecutionContext): Future[IssueFiatRequest] = db.run(issueFiatRequestTable.filter(_.id === id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def findByAccountID(accountID: String)(implicit executionContext: ExecutionContext): Future[IssueFiatRequest] = db.run(issueFiatRequestTable.filter(_.accountID === accountID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateStatusAndGasByID(id: String, status: Boolean, gas: Int)(implicit executionContext: ExecutionContext): Future[Int] = db.run(issueFiatRequestTable.filter(_.id === id).map(issueFiat => (issueFiat.status, issueFiat.gas)).update((status, gas)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateTicketIDStatusAndGasByID(id: String, ticketID: String, status: Boolean, gas: Int): Future[Int] = db.run(issueFiatRequestTable.filter(_.id === id).map(issueFiat => (issueFiat.ticketID, issueFiat.status, issueFiat.gas)).update(ticketID, status, gas).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }


  private def updateStatusAndCommentByID(id: String, status: Boolean, comment: String)(implicit executionContext: ExecutionContext) = db.run(issueFiatRequestTable.filter(_.id === id).map(issueFiatRequest => (issueFiatRequest.status, issueFiatRequest.comment)).update((status, comment)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateStatusByID(id: String, status: Boolean)(implicit executionContext: ExecutionContext) = db.run(issueFiatRequestTable.filter(_.id === id).map(_.status).update(status).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getIssueFiatRequestsWithNullStatus(accountIDs: Seq[String])(implicit executionContext: ExecutionContext): Future[Seq[IssueFiatRequest]] = db.run(issueFiatRequestTable.filter(_.accountID.inSet(accountIDs)).filter(_.status.?.isEmpty).result.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def deleteByID(id: String)(implicit executionContext: ExecutionContext) = db.run(issueFiatRequestTable.filter(_.id === id).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateCommentByID(id: String, comment: String)(implicit executionContext: ExecutionContext) = db.run(issueFiatRequestTable.filter(_.id === id).map(_.comment).update(comment).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getStatusByID(id: String)(implicit executionContext: ExecutionContext): Future[Option[Boolean]] = db.run(issueFiatRequestTable.filter(_.id === id).map(_.status.?).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private[models] class IssueFiatRequestTable(tag: Tag) extends Table[IssueFiatRequest](tag, "IssueFiatRequest") {

    def * = (id, accountID, transactionID, transactionAmount, gas.?, status.?, ticketID.?, comment.?) <> (IssueFiatRequest.tupled, IssueFiatRequest.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def accountID = column[String]("accountID")

    def transactionID = column[String]("transactionID")

    def transactionAmount = column[Int]("transactionAmount")

    def gas = column[Int]("gas")

    def status = column[Boolean]("status")

    def ticketID = column[String]("ticketID")

    def comment = column[String]("comment")

  }

  object Service {

    def addIssueFiatRequest(accountID: String, transactionID: String, transactionAmount: Int)(implicit executionContext: ExecutionContext): String = Await.result(add(IssueFiatRequest(id = Random.nextString(32), accountID = accountID, transactionID = transactionID, transactionAmount = transactionAmount, null, null, null, null)), Duration.Inf)

    def getIssueFiatRequest(accountID: String)(implicit executionContext: ExecutionContext): IssueFiatRequest = Await.result(findByAccountID(accountID), Duration.Inf)

    def updateStatusAndGas(id: String, status: Boolean, gas: Int)(implicit executionContext: ExecutionContext): Int = Await.result(updateStatusAndGasByID(id, status, gas), Duration.Inf)

    def updateStatusAndComment(id: String, status: Boolean, comment: String)(implicit executionContext: ExecutionContext): Int = Await.result(updateStatusAndCommentByID(id, status, comment), Duration.Inf)

    def updateTicketIDStatusAndGas(requestID: String, ticketID: String, status: Boolean, gas: Int)(implicit executionContext: ExecutionContext): Int = Await.result(updateTicketIDStatusAndGasByID(requestID, ticketID, status, gas), Duration.Inf)

    def updateStatus(id: String, status: Boolean)(implicit executionContext: ExecutionContext): Int = Await.result(updateStatusByID(id, status), Duration.Inf)

    def updateComment(id: String, comment: String)(implicit executionContext: ExecutionContext): Int = Await.result(updateCommentByID(id, comment), Duration.Inf)

    def getPendingIssueFiatRequests(accountIDs: Seq[String])(implicit executionContext: ExecutionContext): Seq[IssueFiatRequest] = Await.result(getIssueFiatRequestsWithNullStatus(accountIDs), Duration.Inf)

    def deleteIssueFiatRequest(id: String)(implicit executionContext: ExecutionContext): Int = Await.result(deleteByID(id), Duration.Inf)

    def getStatus(id: String)(implicit executionContext: ExecutionContext): Option[Boolean] = Await.result(getStatusByID(id), Duration.Inf)

  }

}
