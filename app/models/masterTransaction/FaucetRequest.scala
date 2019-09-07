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

case class FaucetRequest(id: String, ticketID: Option[String], accountID: String, amount: Int,gas: Option[Int], status: Option[Boolean], comment: Option[String])

@Singleton
class FaucetRequests @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.MASTER_TRANSACTION_FAUCET_REQUESTS

  import databaseConfig.profile.api._

  private[models] val faucetRequestTable = TableQuery[FaucetRequestTable]

  private def add(faucetRequest: FaucetRequest): Future[String] = db.run((faucetRequestTable returning faucetRequestTable.map(_.id) += faucetRequest).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def findByAccountID(accountID: String): Future[FaucetRequest] = db.run(faucetRequestTable.filter(_.accountID === accountID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getFaucetRequestsWithNullStatus: Future[Seq[FaucetRequest]] = db.run(faucetRequestTable.filter(_.status.?.isEmpty).result)

  private def updateTicketIDAndStatusByID(id: String, ticketID: String, status: Option[Boolean]): Future[Int] = db.run(faucetRequestTable.filter(_.id === id).map(faucet => (faucet.ticketID, faucet.status.?)).update(ticketID, status).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateStatusAndCommentByID(id: String, status: Option[Boolean], comment: String) = db.run(faucetRequestTable.filter(_.id === id).map(faucet => (faucet.status.?, faucet.comment)).update((status, comment)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def deleteByID(id: String) = db.run(faucetRequestTable.filter(_.id === id).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getStatusByID(id: String): Future[Option[Boolean]] = db.run(faucetRequestTable.filter(_.id === id).map(_.status.?).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def verifyRequestById(id: String, accountID: String): Future[Boolean] = db.run(faucetRequestTable.filter(_.id === id).filter(_.accountID === accountID).exists.result)

  private[models] class FaucetRequestTable(tag: Tag) extends Table[FaucetRequest](tag, "FaucetRequest") {

    def * = (id, ticketID.?, accountID, amount, gas.?,status.?, comment.?) <> (FaucetRequest.tupled, FaucetRequest.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def ticketID = column[String]("ticketID")

    def accountID = column[String]("accountID")

    def amount = column[Int]("amount")

    def gas = column[Int]("gas")

    def status = column[Boolean]("status")

    def comment = column[String]("comment")

  }

  object Service {

    def create(accountID: String, amount: Int): String = Await.result(add(FaucetRequest(utilities.IDGenerator.requestID(), null, accountID, amount, null, null, null)), Duration.Inf)

    def accept(requestID: String, ticketID: String): Int = Await.result(updateTicketIDAndStatusByID(requestID, ticketID, status = Option(true)), Duration.Inf)

    def reject(id: String, comment: String): Int = Await.result(updateStatusAndCommentByID(id = id, status = Option(false), comment = comment), Duration.Inf)

    def getPendingFaucetRequests: Seq[FaucetRequest] = Await.result(getFaucetRequestsWithNullStatus, Duration.Inf)

    def delete(id: String): Int = Await.result(deleteByID(id), Duration.Inf)

    def getStatus(id: String): Option[Boolean] = Await.result(getStatusByID(id), Duration.Inf)

    def verifyREquest(id: String, accountID: String): Boolean = Await.result(verifyRequestById(id = id, accountID = accountID), Duration.Inf)

  }

}
