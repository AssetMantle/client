package models.master

import java.sql.Date

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.common.Entity.Address
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.{Json, OWrites, Reads}
import slick.jdbc.JdbcProfile

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

case class UBO(personName: String, sharePercentage: Double, relationship: String, title: String)

case class UBOs(data: Seq[UBO])

case class Organization(id: String, zoneID: String, accountID: String, name: String, abbreviation: Option[String] = None, establishmentDate: Date, email: String, registeredAddress: Address, postalAddress: Address, ubos: UBOs, completionStatus: Boolean = false, verificationStatus: Option[Boolean] = None)

@Singleton
class Organizations @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) {

  private implicit val uboReads: Reads[UBO] = Json.reads[UBO]

  private implicit val ubosReads: Reads[UBOs] = Json.reads[UBOs]

  private implicit val uboWrites: OWrites[UBO] = Json.writes[UBO]

  private implicit val ubosWrites: OWrites[UBOs] = Json.writes[UBOs]

  case class OrganizationSerialized(id: String, zoneID: String, accountID: String, name: String, abbreviation: Option[String] = None, establishmentDate: Date, email: String, registeredAddress: String, postalAddress: String, ubos: Option[String] = None, completionStatus: Boolean = false, verificationStatus: Option[Boolean] = None) {

    def deserialize: Organization = Organization(id = id, zoneID = zoneID, accountID = accountID, name = name, abbreviation = abbreviation, establishmentDate = establishmentDate, email = email, registeredAddress = utilities.JSON.convertJsonStringToObject[Address](registeredAddress), postalAddress = utilities.JSON.convertJsonStringToObject[Address](postalAddress), ubos = utilities.JSON.convertJsonStringToObject[UBOs](ubos.getOrElse(Json.toJson(UBOs(Seq())).toString)), completionStatus = completionStatus, verificationStatus = verificationStatus)

  }

  private def serialize(organization: Organization): OrganizationSerialized = OrganizationSerialized(id = organization.id, zoneID = organization.zoneID, accountID = organization.accountID, name = organization.name, abbreviation = organization.abbreviation, establishmentDate = organization.establishmentDate, email = organization.email, registeredAddress = Json.toJson(organization.registeredAddress).toString, postalAddress = Json.toJson(organization.postalAddress).toString, ubos = Option(Json.toJson(organization.ubos).toString), completionStatus = organization.completionStatus, verificationStatus = organization.verificationStatus)

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.MASTER_ORGANIZATION

  import databaseConfig.profile.api._

  private[models] val organizationTable = TableQuery[OrganizationTable]

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

  private def findByAccountID(accountID: String): Future[OrganizationSerialized] = db.run(organizationTable.filter(_.accountID === accountID).result.head.asTry).map {
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

  private def getIDByAccountID(accountID: String): Future[Option[String]] = db.run(organizationTable.filter(_.accountID === accountID).map(_.id.?).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.info(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        None
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

  private def getUBOsOnID(id: String): Future[String] = db.run(organizationTable.filter(_.id === id).map(_.ubos.?).result.head.asTry).map {
    case Success(result) => result.getOrElse(Json.toJson(UBOs(Seq())).toString)
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
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

    def create(zoneID: String, accountID: String, name: String, abbreviation: Option[String], establishmentDate: Date, email: String, registeredAddress: Address, postalAddress: Address, ubos: UBOs): String = Await.result(add(serialize(Organization(id = utilities.IDGenerator.hexadecimal, zoneID = zoneID, accountID = accountID, name = name, abbreviation = abbreviation, establishmentDate = establishmentDate, registeredAddress = registeredAddress, postalAddress = postalAddress, email = email, ubos = ubos))), Duration.Inf)

    def insertOrUpdateOrganizationDetails(zoneID: String, accountID: String, name: String, abbreviation: Option[String], establishmentDate: Date, email: String, registeredAddress: Address, postalAddress: Address): String = {
      val id = Await.result(getIDByAccountID(accountID), Duration.Inf).getOrElse(utilities.IDGenerator.hexadecimal)
      Await.result(upsert(serialize(Organization(id = id, zoneID = zoneID, accountID = accountID, name = name, abbreviation = abbreviation, establishmentDate = establishmentDate, registeredAddress = registeredAddress, postalAddress = postalAddress, email = email, ubos = getUBOs(id)))), Duration.Inf)
      id
    }

    def getID(accountID: String): String = Await.result(getIDByAccountID(accountID), Duration.Inf).getOrElse(throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION))

    def getUBOs(id: String): UBOs = utilities.JSON.convertJsonStringToObject[UBOs](Await.result(getUBOsOnID(id), Duration.Inf))

    def get(id: String): Organization = Await.result(findById(id), Duration.Inf).deserialize

    def getByAccountID(accountID: String): Organization = Await.result(findByAccountID(accountID), Duration.Inf).deserialize

    def getZoneID(id: String): String = Await.result(getZoneIDByID(id), Duration.Inf)

    def rejectOrganization(id: String): Int = Await.result(updateVerificationStatusOnID(id, Option(false)), Duration.Inf)

    def verifyOrganization(id: String): Int = Await.result(updateVerificationStatusOnID(id, Option(true)), Duration.Inf)

    def getAccountId(id: String): String = Await.result(getAccountIdById(id), Duration.Inf)

    def getVerifyOrganizationRequests(zoneID: String): Seq[Organization] = Await.result(getOrganizationsByCompletionStatusVerificationStatusAndZoneID(zoneID = zoneID, completionStatus = true, verificationStatus = None), Duration.Inf).map(organizationSerialized => organizationSerialized.deserialize)

    def getOrganizationsInZone(zoneID: String): Seq[Organization] = Await.result(getOrganizationsByZoneID(zoneID), Duration.Inf).map(organizationSerialized => organizationSerialized.deserialize)

    def getVerificationStatus(id: String): Boolean = Await.result(getVerificationStatusById(id), Duration.Inf).getOrElse(false)

    def markOrganizationFormCompleted(id: String): Int = Await.result(updateCompletionStatusOnID(id = id, completionStatus = true), Duration.Inf)

    def updateUBOs(id: String, ubos: Seq[UBO]): Int = Await.result(updateUBOsOnID(id, Option(Json.toJson(UBOs(data = ubos)).toString)), Duration.Inf)

  }

}