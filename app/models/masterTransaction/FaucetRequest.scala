package models.masterTransaction

import exceptions.BaseException
import javax.inject.Inject
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Random, Success}

case class FaucetRequest(id: String, ticketID: Option[String], accountID: String, amount: Int, gas: Option[Int], status: Option[Boolean], comment: Option[String])

class FaucetRequests @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.MASTER_TRANSACTION_FAUCET_REQUESTS

  import databaseConfig.profile.api._

  private[models] val faucetRequestTable = TableQuery[FaucetRequestTable]

  private def add(faucetRequest: FaucetRequest)(implicit executionContext: ExecutionContext): Future[String] = db.run((faucetRequestTable returning faucetRequestTable.map(_.id) += faucetRequest).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
    }
  }

  private def findByAccountID(accountID: String)(implicit executionContext: ExecutionContext): Future[FaucetRequest] = db.run(faucetRequestTable.filter(_.accountID === accountID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getFaucetRequestsWithNullStatus()(implicit executionContext: ExecutionContext): Future[Seq[FaucetRequest]] = db.run(faucetRequestTable.filter(_.status.?.isEmpty).result.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateTicketIDByID(id: String, ticketID: String)(implicit executionContext: ExecutionContext): Future[Int] = db.run(faucetRequestTable.filter(_.id === id).map(_.ticketID).update(ticketID).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateStatusAndGasByID(id: String, status: Boolean, gas: Int)(implicit executionContext: ExecutionContext) = db.run(faucetRequestTable.filter(_.id === id).map(faucet => (faucet.status, faucet.gas)).update((status, gas)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateStatusAndGasByTicketID(ticketID: String, status: Boolean, gas: Int)(implicit executionContext: ExecutionContext) = db.run(faucetRequestTable.filter(_.ticketID === ticketID).map(faucet => (faucet.status, faucet.gas)).update((status, gas)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateStatusAndCommentByID(id: String, status: Boolean, comment: String)(implicit executionContext: ExecutionContext) = db.run(faucetRequestTable.filter(_.id === id).map(faucet => (faucet.status, faucet.comment)).update((status, comment)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateStatusByID(id: String, status: Boolean)(implicit executionContext: ExecutionContext) = db.run(faucetRequestTable.filter(_.id === id).map(_.status).update(status).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateCommentByID(id: String, comment: String)(implicit executionContext: ExecutionContext) = db.run(faucetRequestTable.filter(_.id === id).map(_.comment).update(comment).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def deleteByID(id: String)(implicit executionContext: ExecutionContext) = db.run(faucetRequestTable.filter(_.id === id).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getStatusByID(id: String)(implicit executionContext: ExecutionContext): Future[Option[Boolean]] = db.run(faucetRequestTable.filter(_.id === id).map(_.status.?).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def checkByTicketId(ticketID: String): Future[Boolean] = db.run(faucetRequestTable.filter(_.ticketID === ticketID).exists.result)

  private[models] class FaucetRequestTable(tag: Tag) extends Table[FaucetRequest](tag, "FaucetRequest") {

    def * = (id, ticketID.?, accountID, amount, gas.?, status.?, comment.?) <> (FaucetRequest.tupled, FaucetRequest.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def ticketID = column[String]("ticketID")

    def accountID = column[String]("accountID")

    def amount = column[Int]("amount")

    def gas = column[Int]("gas")

    def status = column[Boolean]("status")

    def comment = column[String]("comment")

  }

  object Service {

    def checkTicketID(ticketID: String): Boolean = {
      Await.result(checkByTicketId(ticketID), Duration.Inf)
    }

    def addFaucetRequest(accountID: String, amount: Int)(implicit executionContext: ExecutionContext): String = Await.result(add(FaucetRequest(Random.nextString(32), null, accountID, amount, null, null, null)), Duration.Inf)

    def getFaucetRequest(accountID: String)(implicit executionContext: ExecutionContext):FaucetRequest = Await.result(findByAccountID(accountID), Duration.Inf)

    def updateTicketID(requestID: String, ticketID: String)(implicit executionContext: ExecutionContext): Int = Await.result(updateTicketIDByID(requestID, ticketID), Duration.Inf)

    def updateStatusAndGas(id: String, status: Boolean, gas: Int)(implicit executionContext: ExecutionContext): Int = Await.result(updateStatusAndGasByID(id, status, gas), Duration.Inf)

    def updateStatusAndGasOnTicketID(ticketID: String, status: Boolean, gas: Int)(implicit executionContext: ExecutionContext): Int = Await.result(updateStatusAndGasByTicketID(ticketID, status, gas), Duration.Inf)

    def updateStatusAndComment(id: String, status: Boolean, comment: String)(implicit executionContext: ExecutionContext): Int = Await.result(updateStatusAndCommentByID(id, status, comment), Duration.Inf)

    def updateStatus(id: String, status: Boolean)(implicit executionContext: ExecutionContext): Int = Await.result(updateStatusByID(id, status), Duration.Inf)

    def updateComment(id: String, comment: String)(implicit executionContext: ExecutionContext): Int = Await.result(updateCommentByID(id, comment), Duration.Inf)

    def getPendingFaucetRequests()(implicit executionContext: ExecutionContext): Seq[FaucetRequest] = Await.result(getFaucetRequestsWithNullStatus(), Duration.Inf)

    def deleteFaucetRequest(id: String)(implicit executionContext: ExecutionContext): Int = Await.result(deleteByID(id), Duration.Inf)

    def getStatus(id: String)(implicit executionContext: ExecutionContext): Option[Boolean] = Await.result(getStatusByID(id), Duration.Inf)

  }

}
