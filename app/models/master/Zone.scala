package models.master

import exceptions.BaseException
import javax.inject.Inject
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Random, Success}

case class Zone(id: String, accountID: String, name: String, currency: String, status: Option[Boolean])

class Zones @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.MASTER_ZONE

  import databaseConfig.profile.api._

  private[models] val zoneTable = TableQuery[ZoneTable]

  private def add(zone: Zone)(implicit executionContext: ExecutionContext): Future[String] = db.run((zoneTable returning zoneTable.map(_.id) += zone).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
    }
  }

  private def findById(id: String)(implicit executionContext: ExecutionContext): Future[Zone] = db.run(zoneTable.filter(_.id === id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }



  private def deleteById(id: String)(implicit executionContext: ExecutionContext) = db.run(zoneTable.filter(_.id === id).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getAccountIdById(id: String)(implicit executionContext: ExecutionContext): Future[String] = db.run(zoneTable.filter(_.id === id).map(_.accountID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def verifyZoneOnID(id: String, status: Boolean)(implicit executionContext: ExecutionContext) = db.run(zoneTable.filter(_.id === id).map(_.status.?).update(Option(status)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
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

    def addZone(accountID: String, name: String, currency: String)(implicit executionContext: ExecutionContext): String = Await.result(add(Zone(Random.nextInt.toHexString.toUpperCase, accountID, name, currency, null)), Duration.Inf)

    def getZone(id: String)(implicit executionContext: ExecutionContext): Zone = Await.result(findById(id), Duration.Inf)

    def verifyZone(id: String, status: Boolean)(implicit executionContext: ExecutionContext): Boolean = if (Await.result(verifyZoneOnID(id, status), Duration.Inf) == 1) true else false

    def getAccountId(id: String)(implicit executionContext: ExecutionContext): String = Await.result(getAccountIdById(id), Duration.Inf)
  }

}