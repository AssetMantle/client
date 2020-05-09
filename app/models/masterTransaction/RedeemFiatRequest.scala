package models.masterTransaction

import java.sql.Timestamp
import models.common.Node
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Trait.Logged
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.{Configuration, Logger}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class RedeemFiatRequest(id: String, traderID: String, ticketID: String, amount: Int, status: String, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged[RedeemFiatRequest] {

  def createLog()(implicit node: Node): RedeemFiatRequest = copy(createdBy = Option(node.id), createdOn = Option(new Timestamp(System.currentTimeMillis())), createdOnTimeZone = Option(node.timeZone))

  def updateLog()(implicit node: Node): RedeemFiatRequest = copy(updatedBy = Option(node.id), updatedOn = Option(new Timestamp(System.currentTimeMillis())), updatedOnTimeZone = Option(node.timeZone))

}

@Singleton
class RedeemFiatRequests @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext, configuration: Configuration) {

  private implicit val module: String = constants.Module.MASTER_TRANSACTION_REDEEM_FIAT_REQUEST

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  import databaseConfig.profile.api._

  private implicit val node: Node = Node(id = configuration.get[String]("node.id"), timeZone = configuration.get[String]("node.timeZone"))

  private[models] val redeemFiatRequestTable = TableQuery[RedeemFiatRequestTable]

  private def add(redeemFiatRequest: RedeemFiatRequest): Future[String] = db.run((redeemFiatRequestTable returning redeemFiatRequestTable.map(_.id) += redeemFiatRequest.createLog()).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def findByID(id: String): Future[RedeemFiatRequest] = db.run(redeemFiatRequestTable.filter(_.id === id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getByTraderIDsAndStatus(traderIDs: Seq[String], status: String): Future[Seq[RedeemFiatRequest]] = db.run(redeemFiatRequestTable.filter(_.traderID inSet traderIDs).filter(_.status === status).result)

  private def getByTraderIDAndStatus(traderID: String, status: String): Future[Seq[RedeemFiatRequest]] = db.run(redeemFiatRequestTable.filter(_.traderID === traderID).filter(_.status === status).result)

  private def update(redeemFiatRequest: RedeemFiatRequest): Future[Int] = db.run(redeemFiatRequestTable.update(redeemFiatRequest.updateLog()).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def updateStatusByTicketIDAndStatus(ticketID: String, statusPrecondition: String, status: String): Future[Int] = db.run(redeemFiatRequestTable.filter(_.ticketID === ticketID).filter(_.status === statusPrecondition).map(_.status).update(status).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateStatusByIDAndStatus(id: String, statusPrecondition: String, status: String): Future[Int] = db.run(redeemFiatRequestTable.filter(_.id === id).filter(_.status === statusPrecondition).map(_.status).update(status).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def deleteByID(id: String): Future[Int] = db.run(redeemFiatRequestTable.filter(_.id === id).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private[models] class RedeemFiatRequestTable(tag: Tag) extends Table[RedeemFiatRequest](tag, "RedeemFiatRequest") {

    def * = (id, traderID, ticketID, amount, status, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (RedeemFiatRequest.tupled, RedeemFiatRequest.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def traderID = column[String]("traderID")

    def ticketID = column[String]("ticketID", O.Unique)

    def amount = column[Int]("amount")

    def status = column[String]("status")

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")
  }

  object Service {
    def create(traderID: String, ticketID: String, amount: Int): Future[String] = add(RedeemFiatRequest(id = utilities.IDGenerator.requestID(), traderID, ticketID, amount, status = constants.Status.RedeemFiat.AWAITING_BLOCKCHAIN_RESPONSE))

    def getPendingRedeemFiatRequests(traderIDs: Seq[String]): Future[Seq[RedeemFiatRequest]] = getByTraderIDsAndStatus(traderIDs, constants.Status.RedeemFiat.BLOCKCHAIN_SUCCESS)

    def getCompleteRedeemFiatRequests(traderIDs: Seq[String]): Future[Seq[RedeemFiatRequest]] = getByTraderIDsAndStatus(traderIDs, constants.Status.RedeemFiat.REDEEMED)

    def getFailedRedeemFiatRequests(traderIDs: Seq[String]): Future[Seq[RedeemFiatRequest]] = getByTraderIDsAndStatus(traderIDs, constants.Status.RedeemFiat.BLOCKCHAIN_FAILURE)

    def getPendingRedeemFiatRequests(traderID: String): Future[Seq[RedeemFiatRequest]] = getByTraderIDAndStatus(traderID, constants.Status.RedeemFiat.BLOCKCHAIN_SUCCESS)

    def getCompleteRedeemFiatRequests(traderID: String): Future[Seq[RedeemFiatRequest]] = getByTraderIDAndStatus(traderID, constants.Status.RedeemFiat.REDEEMED)

    def getFailedRedeemFiatRequests(traderID: String): Future[Seq[RedeemFiatRequest]] = getByTraderIDAndStatus(traderID, constants.Status.RedeemFiat.BLOCKCHAIN_FAILURE)

    def markBlockchainSuccess(ticketID: String): Future[Int] = updateStatusByTicketIDAndStatus(ticketID, constants.Status.RedeemFiat.AWAITING_BLOCKCHAIN_RESPONSE, constants.Status.RedeemFiat.BLOCKCHAIN_SUCCESS)

    def markBlockchainFailure(ticketID: String): Future[Int] = updateStatusByTicketIDAndStatus(ticketID, constants.Status.RedeemFiat.AWAITING_BLOCKCHAIN_RESPONSE, constants.Status.RedeemFiat.BLOCKCHAIN_FAILURE)

    def markRedeemed(id: String): Future[Int] = updateStatusByIDAndStatus(id, constants.Status.RedeemFiat.BLOCKCHAIN_SUCCESS, constants.Status.RedeemFiat.REDEEMED)

  }

}

