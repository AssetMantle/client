package models.master

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Random, Success}

case class Zone(id: String, accountID: String, name: String, currency: String, status: Option[Boolean])

@Singleton
class Zones @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.MASTER_ZONE

  import databaseConfig.profile.api._

  private[models] val zoneTable = TableQuery[ZoneTable]

  private def add(zone: Zone): Future[String] = db.run((zoneTable returning zoneTable.map(_.id) += zone).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def findById(id: String): Future[Zone] = db.run(zoneTable.filter(_.id === id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }
  
  private def findAll: Future[Seq[Zone]] = db.run(zoneTable.filter(_.status === true).result)

  private def deleteById(id: String) = db.run(zoneTable.filter(_.id === id).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getAccountIdById(id: String): Future[String] = db.run(zoneTable.filter(_.id === id).map(_.accountID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getZoneIdByAccountId(accountID: String): Future[String] = db.run(zoneTable.filter(_.accountID === accountID).map(_.id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getZonesWithNullStatus: Future[Seq[Zone]] = db.run(zoneTable.filter(_.status.?.isEmpty).result)
  
  private def updateStatusOnID(id: String, status: Boolean) = db.run(zoneTable.filter(_.id === id).map(_.status.?).update(Option(status)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getStatusByID(id: String): Future[Option[Boolean]] = db.run(zoneTable.filter(_.id === id).map(_.status.?).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }


  private[models] class ZoneTable(tag: Tag) extends Table[Zone](tag, "Zone") {

    def * = (id, accountID, name, currency, status.?) <> (Zone.tupled, Zone.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def accountID = column[String]("accountID")

    def name = column[String]("name")

    def currency = column[String]("currency")

    def status = column[Boolean]("status")

  }

  object Service {

    def create(accountID: String, name: String, currency: String): String = Await.result(add(Zone((-Math.abs(Random.nextInt)).toHexString.toUpperCase, accountID, name, currency, null)), Duration.Inf)

    def get(id: String): Zone = Await.result(findById(id), Duration.Inf)

    def getAll: Seq[Zone] = Await.result(findAll, Duration.Inf)

    def updateStatus(id: String, status: Boolean): Int = Await.result(updateStatusOnID(id, status), Duration.Inf)

    def getAccountId(id: String): String = Await.result(getAccountIdById(id), Duration.Inf)

    def getZoneId(accountID: String): String = Await.result(getZoneIdByAccountId(accountID), Duration.Inf)

    def getVerifyZoneRequests: Seq[Zone] = Await.result(getZonesWithNullStatus, Duration.Inf)

    def getStatus(id: String): Option[Boolean] = Await.result(getStatusByID(id), Duration.Inf)

  }

}