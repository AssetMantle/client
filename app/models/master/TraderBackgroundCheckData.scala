package models.master

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

case class TraderBackgroundCheckData(accountID: String, details: String)

@Singleton
class TraderBackgroundCheckDatas @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.MASTER_TRANSACTION_WURTCB_REQUEST

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private val logger: Logger = Logger(this.getClass)

  import databaseConfig.profile.api._

  private[models] val traderBackgroundCheckDataTable = TableQuery[TraderBackgroundCheckDataTable]

  private def add(traderBackgroundCheckData: TraderBackgroundCheckData): Future[String] = db.run((traderBackgroundCheckDataTable returning traderBackgroundCheckDataTable.map(_.accountID) += traderBackgroundCheckData).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def upsert(traderBackgroundCheckData: TraderBackgroundCheckData): Future[Int] = db.run(traderBackgroundCheckDataTable.insertOrUpdate(traderBackgroundCheckData).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def findById(accountID: String): Future[TraderBackgroundCheckData] = db.run(traderBackgroundCheckDataTable.filter(_.accountID === accountID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def deleteById(accountID: String): Future[Int] = db.run(traderBackgroundCheckDataTable.filter(_.accountID === accountID).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private[models] class TraderBackgroundCheckDataTable(tag: Tag) extends Table[TraderBackgroundCheckData](tag, "TraderBackgroundCheckData") {

    def * = (accountID, details) <> (TraderBackgroundCheckData.tupled, TraderBackgroundCheckData.unapply)

    def accountID = column[String]("accountID", O.PrimaryKey)

    def details = column[String]("details")

  }

  object Service {

    def create(accountID: String, details: String): Future[String] = add(TraderBackgroundCheckData(accountID , details))

    def get(accountID: String) : Future[TraderBackgroundCheckData] = findById(accountID)
  }

}
