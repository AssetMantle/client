package models.masterTransaction

import java.sql.Timestamp

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Trait.Logged
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import utilities.MicroNumber

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class FaucetRequest(id: String, ticketID: Option[String] = None, accountID: String, amount: MicroNumber, gas: Option[MicroNumber] = None, status: Option[Boolean] = None, comment: Option[String] = None, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged

@Singleton
class FaucetRequests @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.MASTER_TRANSACTION_FAUCET_REQUEST

  import databaseConfig.profile.api._

  private[models] val faucetRequestTable = TableQuery[FaucetRequestTable]

  def serialize(faucetRequest: FaucetRequest): FaucetRequestSerialized = FaucetRequestSerialized(id = faucetRequest.id, ticketID = faucetRequest.ticketID, accountID = faucetRequest.accountID, amount = faucetRequest.amount.toMicroString, gas = faucetRequest.gas.flatMap(x => Option(x.toMicroString)), status = faucetRequest.status, comment = faucetRequest.comment, createdBy = faucetRequest.createdBy, createdOn = faucetRequest.createdOn, createdOnTimeZone = faucetRequest.createdOnTimeZone, updatedBy = faucetRequest.updatedBy, updatedOn = faucetRequest.updatedOn, updatedOnTimeZone = faucetRequest.updatedOnTimeZone)

  case class FaucetRequestSerialized(id: String, ticketID: Option[String], accountID: String, amount: String, gas: Option[String], status: Option[Boolean], comment: Option[String], createdBy: Option[String], createdOn: Option[Timestamp], createdOnTimeZone: Option[String], updatedBy: Option[String], updatedOn: Option[Timestamp], updatedOnTimeZone: Option[String]) {
    def deserialize: FaucetRequest = FaucetRequest(id = id, ticketID = ticketID, accountID = accountID, amount = new MicroNumber(BigInt(amount)), gas = gas.flatMap(x => Option(new MicroNumber(BigInt(x)))), status = status, comment = comment, createdBy = createdBy, createdOn = createdOn, createdOnTimeZone = createdOnTimeZone, updatedBy = updatedBy, updatedOn = updatedOn, updatedOnTimeZone = updatedOnTimeZone)
  }

  private def add(faucetRequest: FaucetRequest): Future[String] = db.run((faucetRequestTable returning faucetRequestTable.map(_.id) += serialize(faucetRequest)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def findByAccountID(accountID: String): Future[FaucetRequestSerialized] = db.run(faucetRequestTable.filter(_.accountID === accountID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def getFaucetRequestsWithNullStatus: Future[Seq[FaucetRequestSerialized]] = db.run(faucetRequestTable.filter(_.status.?.isEmpty).result)

  private def updateTicketIDGasAndStatusByID(id: String, ticketID: String, gas: Option[MicroNumber], status: Option[Boolean]): Future[Int] = db.run(faucetRequestTable.filter(_.id === id).map(faucet => (faucet.ticketID, faucet.gas.?, faucet.status.?)).update(ticketID, gas.flatMap(x => Option(x.toMicroString)), status).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def updateStatusAndCommentByID(id: String, status: Option[Boolean], comment: String) = db.run(faucetRequestTable.filter(_.id === id).map(faucet => (faucet.status.?, faucet.comment)).update((status, comment)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def deleteByID(id: String) = db.run(faucetRequestTable.filter(_.id === id).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def getStatusByID(id: String): Future[Option[Boolean]] = db.run(faucetRequestTable.filter(_.id === id).map(_.status.?).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def verifyRequestById(id: String, accountID: String): Future[Boolean] = db.run(faucetRequestTable.filter(_.id === id).filter(_.accountID === accountID).exists.result)

  private[models] class FaucetRequestTable(tag: Tag) extends Table[FaucetRequestSerialized](tag, "FaucetRequest") {

    def * = (id, ticketID.?, accountID, amount, gas.?, status.?, comment.?, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (FaucetRequestSerialized.tupled, FaucetRequestSerialized.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def ticketID = column[String]("ticketID")

    def accountID = column[String]("accountID")

    def amount = column[String]("amount")

    def gas = column[String]("gas")

    def status = column[Boolean]("status")

    def comment = column[String]("comment")

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")

  }

  object Service {

    def create(accountID: String, amount: MicroNumber): Future[String] = add(FaucetRequest(id = utilities.IDGenerator.requestID(), accountID = accountID, amount = amount))

    def accept(requestID: String, gas: MicroNumber, ticketID: String): Future[Int] = updateTicketIDGasAndStatusByID(id = requestID, ticketID = ticketID, gas = Option(gas), status = Option(true))

    def reject(id: String, comment: String): Future[Int] = updateStatusAndCommentByID(id = id, status = Option(false), comment = comment)

    def getPendingFaucetRequests: Future[Seq[FaucetRequest]] = getFaucetRequestsWithNullStatus.map(_.map(_.deserialize))

    def delete(id: String): Future[Int] = deleteByID(id)

    def getStatus(id: String): Future[Option[Boolean]] = getStatusByID(id)

    def verifyRequest(id: String, accountID: String): Future[Boolean] = verifyRequestById(id = id, accountID = accountID)

  }

}
