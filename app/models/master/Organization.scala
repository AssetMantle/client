package models.master

import java.sql.Date

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json
import slick.jdbc.JdbcProfile

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Random, Success}

case class Organization(id: String, zoneID: String, accountID: String, name: String, abbreviation: Option[String] = None, establishmentDate: Date, email: String, registeredAddress: String, postalAddress: String, ubos: Option[String] = None, completionStatus: Boolean = false, verificationStatus: Option[Boolean] = None)

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

  private def getZoneIDByID(id: String): Future[String] = db.run(organizationTable.filter(_.id === id).map(_.zoneID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getVerificationStatusById(id: String): Future[Option[Boolean]] = db.run(organizationTable.filter(_.id === id).map(_.verificationStatus.?).result.head.asTry).map {
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

  private def getOrganizationsWithCompletedStatusAndNullVerificationStatusByZoneID(zoneID: String): Future[Seq[Organization]] = db.run(organizationTable.filter(_.zoneID === zoneID).filter(_.completionStatus === true).filter(_.verificationStatus.?.isEmpty).result)

  private def getOrganizationsByZoneID(zoneID: String): Future[Seq[Organization]] = db.run(organizationTable.filter(_.zoneID === zoneID).filter(_.verificationStatus.? === Option(true)).result)

  private def updateVerificationStatusOnID(id: String, verificationStatus: Option[Boolean]) = db.run(organizationTable.filter(_.id === id).map(_.verificationStatus.?).update(verificationStatus).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateCompletionStatusOnID(id: String, completionStatus: Boolean) = db.run(organizationTable.filter(_.id === id).map(_.completionStatus).update(completionStatus).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getUBOsOnID(id: String) = db.run(organizationTable.filter(_.id === id).map(_.ubos.?).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.info(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        None
    }
  }

  private def updateUBOsOnID(id: String, ubo: Option[String]) = db.run(organizationTable.filter(_.id === id).map(_.ubos.?).update(ubo).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private[models] class OrganizationTable(tag: Tag) extends Table[Organization](tag, "Organization") {

    def * = (id, zoneID, accountID, name, abbreviation.?, establishmentDate, email, registeredAddress, postalAddress, ubos.?, completionStatus, verificationStatus.?) <> (Organization.tupled, Organization.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def zoneID = column[String]("zoneID")

    def accountID = column[String]("accountID")

    def name = column[String]("name")

    def abbreviation = column[String]("abbreviation")

    def establishmentDate = column[Date]("establishmentDate")

    def email = column[String]("email")

    def registeredAddress = column[String]("registeredAddress")

    def postalAddress = column[String]("postalAddress")

    def ubos = column[String]("ubos")

    def completionStatus = column[Boolean]("completionStatus")

    def verificationStatus = column[Boolean]("verificationStatus")

  }

  object Service {

    def create(zoneID: String, accountID: String, name: String, abbreviation: Option[String], establishmentDate: Date, email: String, registeredAddress: String, postalAddress: String, ubos: Option[String]): String = Await.result(add(Organization(id = (-Math.abs(Random.nextInt)).toHexString.toUpperCase, zoneID = zoneID, accountID = accountID, name = name, abbreviation = abbreviation, establishmentDate = establishmentDate, registeredAddress = registeredAddress, postalAddress = postalAddress, email = email, ubos = ubos)), Duration.Inf)

    def insertOrUpdateOrganizationDetails(zoneID: String, accountID: String, name: String, abbreviation: Option[String], establishmentDate: Date, email: String, registeredAddress: String, postalAddress: String): String = {
      val id = try {
        getID(accountID)
      } catch {
        case baseException: BaseException => if (baseException.failure == constants.Response.NO_SUCH_ELEMENT_EXCEPTION) {
          (-Math.abs(Random.nextInt)).toHexString.toUpperCase
        } else {
          throw baseException
        }
      }
      Await.result(upsert(Organization(id = id, zoneID = zoneID, accountID = accountID, name = name, abbreviation = abbreviation, establishmentDate = establishmentDate, registeredAddress = registeredAddress, postalAddress = postalAddress, email = email, ubos = Option(Json.toJson(getUBOs(id)).toString))), Duration.Inf)
      id
    }

    def getID(accountID: String): String = Await.result(getIDByAccountID(accountID), Duration.Inf)

    def getUBOs(id: String): utils.UBOs = {
      try {
        val ubos = utilities.JSON.convertJsonStringToObject[utils.UBOs](Await.result(getUBOsOnID(id), Duration.Inf).getOrElse(Json.toJson[utils.UBOs](utils.UBOs()).toString))
        ubos
      } catch {
        case _: BaseException => utils.UBOs(Seq())
      }
    }

    def get(id: String): Organization = Await.result(findById(id), Duration.Inf)

    def getByAccountID(accountID: String): Organization = Await.result(findByAccountID(accountID), Duration.Inf)

    def getZoneID(id: String): String = Await.result(getZoneIDByID(id), Duration.Inf)

    def rejectOrganization(id: String): Int = Await.result(updateVerificationStatusOnID(id, Option(false)), Duration.Inf)

    def verifyOrganization(id: String): Int = Await.result(updateVerificationStatusOnID(id, Option(true)), Duration.Inf)

    def getAccountId(id: String): String = Await.result(getAccountIdById(id), Duration.Inf)

    def getVerifyOrganizationRequests(zoneID: String): Seq[Organization] = Await.result(getOrganizationsWithCompletedStatusAndNullVerificationStatusByZoneID(zoneID), Duration.Inf)

    def getOrganizationsInZone(zoneID: String): Seq[Organization] = Await.result(getOrganizationsByZoneID(zoneID), Duration.Inf)

    def getVerificationStatus(id: String): Boolean = Await.result(getVerificationStatusById(id), Duration.Inf).getOrElse(false)

    def markOrganizationFormCompleted(id: String): Int = Await.result(updateCompletionStatusOnID(id = id, completionStatus = true), Duration.Inf)

    def updateUBOs(id: String, ubos: Seq[utils.UBO]): Int = Await.result(updateUBOsOnID(id, Option(Json.toJson(utils.UBOs(ubos)).toString())), Duration.Inf)

    def updateUBO(id: String, ubo: utils.UBO): Int = Await.result(updateUBOsOnID(id, Option(Json.toJson(utils.UBOs(getUBOs(id).ubos :+ ubo)).toString())), Duration.Inf)

  }

}