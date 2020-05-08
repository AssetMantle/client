package models.westernUnion

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class FiatRequest(id: String, traderID: String, transactionAmount: Int, status: String)

@Singleton
class FiatRequests @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.WESTERN_UNION_FIAT_REQUEST

  import databaseConfig.profile.api._

  private[models] val fiatRequestTable = TableQuery[FiatRequestTable]

  private def add(fiatRequest: FiatRequest): Future[String] = db.run((fiatRequestTable returning fiatRequestTable.map(_.id) += fiatRequest).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def findByID(id: String): Future[FiatRequest] = db.run(fiatRequestTable.filter(_.id === id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def findAllByID(traderID: String): Future[Seq[FiatRequest]]= db.run(fiatRequestTable.filter(_.traderID === traderID).result)

  private def findAllByTraderIDs(traderIDs: Seq[String]): Future[Seq[FiatRequest]]= db.run(fiatRequestTable.filter(_.traderID inSet traderIDs).result)

  private def updateStatusByID(id: String, status: String): Future[Int] = db.run(fiatRequestTable.filter(_.id === id).map(_.status).update(status).asTry).map {
    case Success(result) => if (result > 0) {
      result
    } else {
      logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, new NoSuchElementException("ID NOT FOUND, NO ROW UPDATED FOR TRANSACTION ID = " + id))
      throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def deleteByID(id: String): Future[Int] = db.run(fiatRequestTable.filter(_.id === id).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getStatusByID(id: String): Future[String] = db.run(fiatRequestTable.filter(_.id === id).map(_.status).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }


  private[models] class FiatRequestTable(tag: Tag) extends Table[FiatRequest](tag, "FiatRequest") {

    def * = (id, traderID, transactionAmount, status) <> (FiatRequest.tupled, FiatRequest.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def traderID = column[String]("traderID")

    def transactionAmount = column[Int]("transactionAmount")

    def status = column[String]("status")

  }

  object Service {

    def create(traderID: String, transactionAmount: Int): Future[String] = add(FiatRequest(id = utilities.IDGenerator.requestID(length = 30), traderID = traderID, transactionAmount = transactionAmount, status = constants.Status.IssueFiat.REQUEST_INITIATED))

    def tryGetByID(id: String): Future[FiatRequest] = findByID(id)

    def getStatus(id: String): Future[String] = getStatusByID(id)

    def getAll(traderID:String)= findAllByID(traderID)

    def getAllByTraderIDs(traderIDs:Seq[String])= findAllByTraderIDs(traderIDs)

    def markRTCBReceived(id: String, amountRequested: Int, totalRTCBAmount: Int): Future[Int] = {
      if (amountRequested == totalRTCBAmount) {
        updateStatusByID(id, constants.Status.IssueFiat.FULLY_PAID)
      } else if (amountRequested < totalRTCBAmount) {
        updateStatusByID(id, constants.Status.IssueFiat.OVER_PAID)
      } else {
        updateStatusByID(id, constants.Status.IssueFiat.PARTIALLY_PAID)
      }
    }
  }

}
