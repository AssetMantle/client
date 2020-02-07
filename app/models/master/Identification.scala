package models.master

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.common.Serializable
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json
import slick.jdbc.JdbcProfile
import slick.lifted.TableQuery

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Identification(accountID: String, firstName: String, lastName: String, idNumber: String, idType: String, status: Option[Boolean] = None)

@Singleton
class Identifications @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) {
  val databaseConfig = databaseConfigProvider.get[JdbcProfile]
  val db = databaseConfig.db
  private[models] val identificationTable = TableQuery[IdentificationTable]

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.MASTER_ASSET

  import databaseConfig.profile.api._

  private def add(identification: Identification): Future[String] = db.run((identificationTable returning identificationTable.map(_.accountID) += identification).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def upsert(identification: Identification): Future[Int] = db.run(identificationTable.insertOrUpdate(identification).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def getIdentificationByAccountID(accountID: String): Future[Option[Identification]] = db.run(identificationTable.filter(_.accountID === accountID).result.head.asTry).map {
    case Success(result) => Option(result)
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        None
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private[models] class IdentificationTable(tag: Tag) extends Table[Identification](tag, "Identification") {

    def * = (accountID, firstName, lastName, idNumber, idType, status.?) <> (Identification.tupled, Identification.unapply)

    def accountID = column[String]("accountID", O.PrimaryKey)

    def firstName = column[String]("firstName")

    def lastName = column[String]("lastName")

    def idNumber = column[String]("idNumber")

    def idType = column[String]("idType")

    def status = column[Boolean]("status")

  }

  object Service {

    def create(accountID: String, firstName: String, lastName: String, idNumber: String, idType: String, status: Option[Boolean] = None): Future[String] = add(Identification(accountID, firstName, lastName, idNumber, idType, status))

    def insertOrUpdate(accountID: String, firstName: String, lastName: String, idNumber: String, idType: String, status: Option[Boolean]): Future[Int] = upsert(Identification(accountID, firstName, lastName, idNumber, idType, status))

    def getByAccountID(accountID: String): Future[Option[Identification]] = getIdentificationByAccountID(accountID)


  }

}
