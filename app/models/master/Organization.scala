package models.master

import java.sql.Date

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Random, Success}

case class Organization(id: String, zoneID: String, accountID: String, name: String, abbreviation: Option[String] = None, establishmentDate: Date, registeredAddress: String, postalAddress: String, email: String, ubo: Option[String] = None, status: Option[Boolean] = None)

@Singleton
class Organizations @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.MASTER_ORGANIZATION

  import databaseConfig.profile.api._

  private[models] val organizationTable = TableQuery[OrganizationTable]

  private def add(organization: Organization): Future[String] = db.run((organizationTable returning organizationTable.map(_.id) += organization).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def upsert(organization: Organization): Future[Int] = db.run(organizationTable.insertOrUpdate(organization).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def findById(id: String): Future[Organization] = db.run(organizationTable.filter(_.id === id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def findByAccountID(accountID: String): Future[Organization] = db.run(organizationTable.filter(_.accountID === accountID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getAccountIdById(id: String): Future[String] = db.run(organizationTable.filter(_.id === id).map(_.accountID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getIDByAccountID(accountID: String): Future[String] = db.run(organizationTable.filter(_.accountID === accountID).map(_.id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getStatusById(id: String): Future[Option[Boolean]] = db.run(organizationTable.filter(_.id === id).map(_.status.?).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def deleteById(id: String) = db.run(organizationTable.filter(_.id === id).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getOrganizationsWithNullStatusByZoneID(zoneID: String): Future[Seq[Organization]] = db.run(organizationTable.filter(_.zoneID === zoneID).filter(_.status.?.isEmpty).result)

  private def getOrganizationsByZoneID(zoneID: String): Future[Seq[Organization]] = db.run(organizationTable.filter(_.zoneID === zoneID).filter(_.status===true).result)

  private def updateStatusOnID(id: String, status: Boolean) = db.run(organizationTable.filter(_.id === id).map(_.status.?).update(Option(status)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private[models] class OrganizationTable(tag: Tag) extends Table[Organization](tag, "Organization") {

    def * = (id, zoneID, accountID, name, abbreviation.?, establishmentDate, registeredAddress, postalAddress, email, ubo.?, status.?) <> (Organization.tupled, Organization.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def zoneID = column[String]("zoneID")

    def accountID = column[String]("accountID")

    def name = column[String]("name")

    def abbreviation = column[String]("abbreviation")

    def establishmentDate = column[Date]("establishmentDate")

    def registeredAddress = column[String]("registeredAddress")

    def postalAddress = column[String]("postalAddress")

    def email = column[String]("email")

    def ubo = column[String]("ubo")

    def status = column[Boolean]("status")

  }

  object Service {

    def create(zoneID: String, accountID: String, name: String, abbreviation: Option[String], establishmentDate: Date, registeredAddress: String, postalAddress: String, email: String, ubo: Option[String]): String = Await.result(add(Organization(id = (-Math.abs(Random.nextInt)).toHexString.toUpperCase, zoneID = zoneID, accountID = accountID, name = name, abbreviation = abbreviation, establishmentDate = establishmentDate, registeredAddress = registeredAddress, postalAddress = postalAddress, email = email, ubo = ubo)), Duration.Inf)

    def insertOrUpdate(zoneID: String, accountID: String, name: String, abbreviation: Option[String], establishmentDate: Date, registeredAddress: String, postalAddress: String, email: String, ubo: Option[String]): String = {
      val id = try{
        getID(accountID)
      } catch {
        case baseException: BaseException => if (baseException.failure == constants.Response.NO_SUCH_ELEMENT_EXCEPTION){
          (-Math.abs(Random.nextInt)).toHexString.toUpperCase
        } else {
          throw new BaseException(baseException.failure)
        }
      }
      Await.result(upsert(Organization(id = id, zoneID = zoneID, accountID = accountID, name = name, abbreviation = abbreviation, establishmentDate = establishmentDate, registeredAddress = registeredAddress, postalAddress = postalAddress, email = email, ubo = ubo)), Duration.Inf)
      id
    }

    def get(id: String): Organization = Await.result(findById(id), Duration.Inf)

    def getByAccountID(accountID: String): Organization = Await.result(findByAccountID(accountID), Duration.Inf)

    def getID(accountID: String): String = Await.result(getIDByAccountID(accountID), Duration.Inf)

    def updateStatus(id: String, status: Boolean): Int = Await.result(updateStatusOnID(id, status), Duration.Inf)

    def getAccountId(id: String): String = Await.result(getAccountIdById(id), Duration.Inf)

    def getVerifyOrganizationRequests(zoneID: String): Seq[Organization] = Await.result(getOrganizationsWithNullStatusByZoneID(zoneID), Duration.Inf)

    def getOrganizationsInZone(zoneID: String): Seq[Organization] = Await.result(getOrganizationsByZoneID(zoneID), Duration.Inf)

    def getStatus(id: String): Option[Boolean] = Await.result(getStatusById(id), Duration.Inf)

  }

}