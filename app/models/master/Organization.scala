package models.master

import java.sql.Date

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.common.Serializable._
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json
import slick.jdbc.JdbcProfile
import slick.lifted.TableQuery

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Organization(id: String, zoneID: String, accountID: String, name: String, abbreviation: Option[String] = None, establishmentDate: Date, email: String, registeredAddress: Address, postalAddress: Address, ubos: UBOs, completionStatus: Boolean = false, verificationStatus: Option[Boolean] = None)

@Singleton
class Organizations @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) {
  val databaseConfig = databaseConfigProvider.get[JdbcProfile]
  val db = databaseConfig.db
  private[models] val organizationTable = TableQuery[OrganizationTable]

  private def serialize(organization: Organization): OrganizationSerialized = OrganizationSerialized(id = organization.id, zoneID = organization.zoneID, accountID = organization.accountID, name = organization.name, abbreviation = organization.abbreviation, establishmentDate = organization.establishmentDate, email = organization.email, registeredAddress = Json.toJson(organization.registeredAddress).toString, postalAddress = Json.toJson(organization.postalAddress).toString, ubos = Option(Json.toJson(organization.ubos).toString), completionStatus = organization.completionStatus, verificationStatus = organization.verificationStatus)

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.MASTER_ORGANIZATION

  import databaseConfig.profile.api._

  private def add(organizationSerialized: OrganizationSerialized): Future[String] = db.run((organizationTable returning organizationTable.map(_.id) += organizationSerialized).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def upsert(organizationSerialized: OrganizationSerialized): Future[Int] = db.run(organizationTable.insertOrUpdate(organizationSerialized).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def findById(id: String): Future[OrganizationSerialized] = db.run(organizationTable.filter(_.id === id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def findOrNoneByID(id: String): Future[Option[OrganizationSerialized]] = db.run(organizationTable.filter(_.id === id).result.headOption)

  private def findByAccountID(accountID: String): Future[OrganizationSerialized] = db.run(organizationTable.filter(_.accountID === accountID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def findOrNoneByAccountID(accountID: String): Future[Option[OrganizationSerialized]] = db.run(organizationTable.filter(_.accountID === accountID).result.headOption)

  private def getAccountIDByID(id: String): Future[String] = db.run(organizationTable.filter(_.id === id).map(_.accountID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getIDByAccountID(accountID: String) = db.run(organizationTable.filter(_.accountID === accountID).map(_.id.?).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.info(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        None
    }
  }

  private def findNameByAccountID(accountID: String): Future[String] = db.run(organizationTable.filter(_.accountID === accountID).map(_.name).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.info(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def findNameByID(id: String): Future[String] = db.run(organizationTable.filter(_.id === id).map(_.name).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.info(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
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

  private def getZoneIDOnAccountID(accountID: String): Future[String] = db.run(organizationTable.filter(_.accountID === accountID).map(_.zoneID).result.head.asTry).map {
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

  private def getVerificationStatusByAccountID(accountID: String): Future[Option[Boolean]] = db.run(organizationTable.filter(_.accountID === accountID).map(_.verificationStatus.?).result.head.asTry).map {
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

  private def getOrganizationsByCompletionStatusVerificationStatusAndZoneID(zoneID: String, completionStatus: Boolean, verificationStatus: Option[Boolean]): Future[Seq[OrganizationSerialized]] = db.run(organizationTable.filter(_.zoneID === zoneID).filter(_.completionStatus === completionStatus).filter(_.verificationStatus.? === verificationStatus).result)

  private def getOrganizationsByZoneID(zoneID: String): Future[Seq[OrganizationSerialized]] = db.run(organizationTable.filter(_.zoneID === zoneID).filter(_.verificationStatus.? === Option(true)).result)

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

  private def getUBOsOnID(id: String): Future[Option[String]] = db.run(organizationTable.filter(_.id === id).map(_.ubos.?).result.head.asTry).map {
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

  case class OrganizationSerialized(id: String, zoneID: String, accountID: String, name: String, abbreviation: Option[String] = None, establishmentDate: Date, email: String, registeredAddress: String, postalAddress: String, ubos: Option[String] = None, completionStatus: Boolean, verificationStatus: Option[Boolean]) {

    def deserialize: Organization = Organization(id = id, zoneID = zoneID, accountID = accountID, name = name, abbreviation = abbreviation, establishmentDate = establishmentDate, email = email, registeredAddress = utilities.JSON.convertJsonStringToObject[Address](registeredAddress), postalAddress = utilities.JSON.convertJsonStringToObject[Address](postalAddress), ubos = utilities.JSON.convertJsonStringToObject[UBOs](ubos.getOrElse(Json.toJson(UBOs(Seq())).toString)), completionStatus = completionStatus, verificationStatus = verificationStatus)

  }

  private[models] class OrganizationTable(tag: Tag) extends Table[OrganizationSerialized](tag, "Organization") {

    def * = (id, zoneID, accountID, name, abbreviation.?, establishmentDate, email, registeredAddress, postalAddress, ubos.?, completionStatus, verificationStatus.?) <> (OrganizationSerialized.tupled, OrganizationSerialized.unapply)

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

    def create(zoneID: String, accountID: String, name: String, abbreviation: Option[String], establishmentDate: Date, email: String, registeredAddress: Address, postalAddress: Address, ubos: UBOs): Future[String] = add(serialize(Organization(id = utilities.IDGenerator.hexadecimal, zoneID = zoneID, accountID = accountID, name = name, abbreviation = abbreviation, establishmentDate = establishmentDate, registeredAddress = registeredAddress, postalAddress = postalAddress, email = email, ubos = ubos)))

    def insertOrUpdateWithoutUBOs(zoneID: String, accountID: String, name: String, abbreviation: Option[String], establishmentDate: Date, email: String, registeredAddress: Address, postalAddress: Address): Future[String] = {
      val id = getIDByAccountID(accountID).map {
        _.getOrElse(utilities.IDGenerator.hexadecimal)
      }

      def upsertOrganization(id: String, ubos: UBOs) = upsert(serialize(Organization(id = id, zoneID = zoneID, accountID = accountID, name = name, abbreviation = abbreviation, establishmentDate = establishmentDate, registeredAddress = registeredAddress, postalAddress = postalAddress, email = email, ubos = ubos)))

      for {
        id <- id
        ubos <- getUBOs(id)
        _ <- upsertOrganization(id, ubos)
      } yield id
    }

    def getUBOs(id: String): Future[UBOs] = getUBOsOnID(id).map(_.getOrElse(Json.toJson(UBOs(Seq())).toString)).map {
      utilities.JSON.convertJsonStringToObject[UBOs](_)
    }

    def getID(accountID: String): Future[Option[String]] = getIDByAccountID(accountID)

    def tryGetID(accountID: String): Future[String] = getIDByAccountID(accountID).map { id => id.getOrElse(throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)) }

    def getNameByID(id: String): Future[String] = findNameByID(id)

    def getNameByAccountID(accountID: String): Future[String] = findNameByAccountID(accountID)

    def get(id: String): Future[Organization] = findById(id).map { organizationSerialized => organizationSerialized.deserialize }

    def getOrNone(id: String): Future[Option[Organization]] = findOrNoneByID(id).map(_.map(_.deserialize))

    def tryGetByAccountID(accountID: String): Future[Organization] = findByAccountID(accountID).map { organizationSerialized => organizationSerialized.deserialize }

    def getByAccountID(accountID: String): Future[Option[Organization]] = findOrNoneByAccountID(accountID).map(_.map(_.deserialize))

    def getZoneID(id: String): Future[String] = getZoneIDByID(id)

    def getZoneIDByAccountID(accountID: String): Future[String] = getZoneIDOnAccountID(accountID)

    def rejectOrganization(id: String): Future[Int] = updateVerificationStatusOnID(id, Option(false))

    def verifyOrganization(id: String): Future[Int] = updateVerificationStatusOnID(id, Option(true))

    def getAccountId(id: String): Future[String] = getAccountIDByID(id)

    def getVerifyOrganizationRequests(zoneID: String): Future[Seq[Organization]] = getOrganizationsByCompletionStatusVerificationStatusAndZoneID(zoneID = zoneID, completionStatus = true, verificationStatus = null).map { organizations => organizations.map(_.deserialize) }

    def getOrganizationsInZone(zoneID: String): Future[Seq[Organization]] = getOrganizationsByZoneID(zoneID).map { organizations => organizations.map(_.deserialize) }

    def getVerificationStatus(id: String): Future[Boolean] = getVerificationStatusById(id).map {
      _.getOrElse(false)
    }

    def checkVerificationStatusByAccountID(accountID: String): Future[Boolean] = getVerificationStatusByAccountID(accountID).map {
      _.getOrElse(false)
    }

    def getVerificationStatusWithTry(id: String): Future[Boolean] = {
      val verificationStatus = getVerificationStatusById(id)
      for {
        verificationStatus <- verificationStatus
      } yield {
        if (!verificationStatus.getOrElse(false)) throw new BaseException(constants.Response.UNVERIFIED_ORGANIZATION) else true
      }
    }

    def markOrganizationFormCompleted(id: String): Future[Int] = updateCompletionStatusOnID(id = id, completionStatus = true)

    def updateUBOs(id: String, ubos: Seq[UBO]): Future[Int] = updateUBOsOnID(id, Option(Json.toJson(UBOs(data = ubos)).toString))
  }

}