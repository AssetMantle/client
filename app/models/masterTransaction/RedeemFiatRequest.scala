package models.masterTransaction

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.{Configuration, Logger}
import slick.jdbc.JdbcProfile
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class RedeemFiatRequest(id: String, traderID: String, ticketID: String, amount: Int, status: String)

@Singleton
class RedeemFiatRequests @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext, configuration: Configuration) {

  private implicit val module: String = constants.Module.MASTER_TRANSACTION_REDEEM_FIAT_REQUEST

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private val logger: Logger = Logger(this.getClass)

  import databaseConfig.profile.api._

  private[models] val redeemFiatTable = TableQuery[RedeemFiatTable]

  private def add(redeemFiat: RedeemFiatRequest): Future[String] = db.run((redeemFiatTable returning redeemFiatTable.map(_.id) += redeemFiat).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def findByID(id: String): Future[RedeemFiatRequest] = db.run(redeemFiatTable.filter(_.id === id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getByTraderIDs(traderIDs: Seq[String]): Future[Seq[RedeemFiatRequest]] = db.run(redeemFiatTable.filter(_.traderID inSet traderIDs).result)

  private def getByTraderID(traderID: String): Future[Seq[RedeemFiatRequest]] = db.run(redeemFiatTable.filter(_.traderID === traderID).result)

  private def update(redeemFiat: RedeemFiatRequest): Future[Int] = db.run(redeemFiatTable.update(redeemFiat).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def updateStatusByTicketIDAndStatus(ticketID: String, statusPrecondition: String, status: String): Future[Int] = db.run(redeemFiatTable.filter(_.ticketID === ticketID).filter(_.status === statusPrecondition).map(_.status).update(status).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateStatusByIDAndStatus(id: String, statusPrecondition: String, status: String): Future[Int] = db.run(redeemFiatTable.filter(_.id === id).filter(_.status === statusPrecondition).map(_.status).update(status).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def deleteByID(id: String): Future[Int] = db.run(redeemFiatTable.filter(_.id === id).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private[models] class RedeemFiatTable(tag: Tag) extends Table[RedeemFiatRequest](tag, "RedeemFiatRequest") {

    def * = (id, traderID, ticketID, amount, status) <> (RedeemFiatRequest.tupled, RedeemFiatRequest.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def traderID = column[String]("traderID")

    def ticketID = column[String]("ticketID", O.Unique)

    def amount = column[Int]("amount")

    def status = column[String]("status")

  }

  object Service {
    def create(traderID: String, ticketID: String, amount: Int): Future[String] = add(RedeemFiatRequest(id = utilities.IDGenerator.requestID(), traderID, ticketID, amount, status = constants.Status.RedeemFiat.AWAITING_BLOCKCHAIN_RESPONSE))

    def getRedeemFiatRequests(traderIDs: Seq[String]): Future[Seq[RedeemFiatRequest]] = getByTraderIDs(traderIDs)

    def getRedeemFiatRequests(traderID: String): Future[Seq[RedeemFiatRequest]] = getByTraderID(traderID)

    def markBlockchainSuccess(ticketID: String): Future[Int] = updateStatusByTicketIDAndStatus(ticketID, constants.Status.RedeemFiat.AWAITING_BLOCKCHAIN_RESPONSE, constants.Status.RedeemFiat.BLOCKCHAIN_SUCCESS)

    def markBlockchainFailure(ticketID: String): Future[Int] = updateStatusByTicketIDAndStatus(ticketID, constants.Status.RedeemFiat.AWAITING_BLOCKCHAIN_RESPONSE, constants.Status.RedeemFiat.BLOCKCHAIN_FAILURE)

    def markRedeemed(id: String): Future[Int] = updateStatusByIDAndStatus(id, constants.Status.RedeemFiat.BLOCKCHAIN_SUCCESS, constants.Status.RedeemFiat.REDEEMED)

  }

}

