package models.master

import java.sql.{Date, Timestamp}

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Trait.Logged
import models.common.Serializable._
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json
import slick.jdbc.JdbcProfile
import slick.lifted.TableQuery

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Organization(id: String, zoneID: String, accountID: String, name: String, abbreviation: Option[String] = None, establishmentDate: Date, email: String, registeredAddress: Address, postalAddress: Address, completionStatus: Boolean = false, verificationStatus: Option[Boolean] = None, comment: Option[String] = None, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged

@Singleton
class Organizations @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private[models] val organizationTable = TableQuery[OrganizationTable]

  private def serialize(organization: Organization): OrganizationSerialized = OrganizationSerialized(id = organization.id, zoneID = organization.zoneID, accountID = organization.accountID, name = organization.name, abbreviation = organization.abbreviation, establishmentDate = organization.establishmentDate, email = organization.email, registeredAddress = Json.toJson(organization.registeredAddress).toString, postalAddress = Json.toJson(organization.postalAddress).toString, completionStatus = organization.completionStatus, verificationStatus = organization.verificationStatus, comment = organization.comment, createdBy = organization.createdBy, createdOn = organization.createdOn, createdOnTimeZone = organization.createdOnTimeZone, updatedBy = organization.updatedBy, updatedOn = organization.updatedOn, updatedOnTimeZone = organization.updatedOnTimeZone)

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.MASTER_ORGANIZATION

  import databaseConfig.profile.api._

  private def add(organizationSerialized: OrganizationSerialized): Future[String] = db.run((organizationTable returning organizationTable.map(_.id) += organizationSerialized).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def upsert(organizationSerialized: OrganizationSerialized): Future[Int] = db.run(organizationTable.insertOrUpdate(organizationSerialized).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def findById(id: String): Future[OrganizationSerialized] = db.run(organizationTable.filter(_.id === id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def findOrNoneByID(id: String): Future[Option[OrganizationSerialized]] = db.run(organizationTable.filter(_.id === id).result.headOption)

  private def findByAccountID(accountID: String): Future[OrganizationSerialized] = db.run(organizationTable.filter(_.accountID === accountID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def findOrNoneByAccountID(accountID: String): Future[Option[OrganizationSerialized]] = db.run(organizationTable.filter(_.accountID === accountID).result.headOption)

  private def getAccountIDByID(id: String): Future[String] = db.run(organizationTable.filter(_.id === id).map(_.accountID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def getIDByAccountID(accountID: String) = db.run(organizationTable.filter(_.accountID === accountID).map(_.id).result.headOption)

  private def getIDByAccountIDAndVerificationStatus(accountID: String, verificationStatus: Option[Boolean]) = db.run(organizationTable.filter(_.accountID === accountID).filter(_.verificationStatus.? === verificationStatus).map(_.id).result.headOption)

  private def findNameByAccountID(accountID: String): Future[String] = db.run(organizationTable.filter(_.accountID === accountID).map(_.name).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.info(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def findNameByID(id: String): Future[String] = db.run(organizationTable.filter(_.id === id).map(_.name).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.info(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def getZoneIDByID(id: String): Future[String] = db.run(organizationTable.filter(_.id === id).map(_.zoneID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def getZoneIDOnAccountID(accountID: String): Future[String] = db.run(organizationTable.filter(_.accountID === accountID).map(_.zoneID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def getVerificationStatusById(id: String): Future[Option[Boolean]] = db.run(organizationTable.filter(_.id === id).map(_.verificationStatus.?).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def getVerificationStatusByAccountID(accountID: String): Future[Option[Boolean]] = db.run(organizationTable.filter(_.accountID === accountID).map(_.verificationStatus.?).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def deleteById(id: String) = db.run(organizationTable.filter(_.id === id).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def getOrganizationsByCompletionStatusVerificationStatusAndZoneID(zoneID: String, completionStatus: Boolean, verificationStatus: Option[Boolean]): Future[Seq[OrganizationSerialized]] = db.run(organizationTable.filter(_.zoneID === zoneID).filter(_.completionStatus === completionStatus).filter(_.verificationStatus.? === verificationStatus).sortBy(x => x.updatedOn.ifNull(x.createdOn).desc).result)

  private def getOrganizationsByIDs(organizationIDs: Seq[String]): Future[Seq[OrganizationSerialized]] = db.run(organizationTable.filter(_.id inSet (organizationIDs)).result)

  private def updateVerificationStatusAndCommentOnID(id: String, verificationStatus: Option[Boolean], comment: Option[String]): Future[Int] = db.run(organizationTable.filter(_.id === id).map(x => (x.verificationStatus.?, x.comment.?)).update((verificationStatus, comment)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def updateCompletionStatusOnID(id: String, completionStatus: Boolean): Future[Int] = db.run(organizationTable.filter(_.id === id).map(_.completionStatus).update(completionStatus).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }


  case class OrganizationSerialized(id: String, zoneID: String, accountID: String, name: String, abbreviation: Option[String], establishmentDate: Date, email: String, registeredAddress: String, postalAddress: String, completionStatus: Boolean, verificationStatus: Option[Boolean], comment: Option[String], createdBy: Option[String], createdOn: Option[Timestamp], createdOnTimeZone: Option[String], updatedBy: Option[String], updatedOn: Option[Timestamp], updatedOnTimeZone: Option[String]) {

    def deserialize: Organization = Organization(id = id, zoneID = zoneID, accountID = accountID, name = name, abbreviation = abbreviation, establishmentDate = establishmentDate, email = email, registeredAddress = utilities.JSON.convertJsonStringToObject[Address](registeredAddress), postalAddress = utilities.JSON.convertJsonStringToObject[Address](postalAddress), completionStatus = completionStatus, verificationStatus = verificationStatus, comment = comment, createdBy = createdBy, createdOn = createdOn, createdOnTimeZone = createdOnTimeZone, updatedBy = updatedBy, updatedOn = updatedOn, updatedOnTimeZone = updatedOnTimeZone)

  }

  private[models] class OrganizationTable(tag: Tag) extends Table[OrganizationSerialized](tag, "Organization") {

    def * = (id, zoneID, accountID, name, abbreviation.?, establishmentDate, email, registeredAddress, postalAddress, completionStatus, verificationStatus.?, comment.?, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (OrganizationSerialized.tupled, OrganizationSerialized.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def zoneID = column[String]("zoneID")

    def accountID = column[String]("accountID")

    def name = column[String]("name")

    def abbreviation = column[String]("abbreviation")

    def establishmentDate = column[Date]("establishmentDate")

    def email = column[String]("email")

    def registeredAddress = column[String]("registeredAddress")

    def postalAddress = column[String]("postalAddress")

    def completionStatus = column[Boolean]("completionStatus")

    def verificationStatus = column[Boolean]("verificationStatus")

    def comment = column[String]("comment")

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")

  }

  object Service {

    def create(zoneID: String, accountID: String, name: String, abbreviation: Option[String], establishmentDate: Date, email: String, registeredAddress: Address, postalAddress: Address): Future[String] = add(serialize(Organization(id = utilities.IDGenerator.hexadecimal, zoneID = zoneID, accountID = accountID, name = name, abbreviation = abbreviation, establishmentDate = establishmentDate, registeredAddress = registeredAddress, postalAddress = postalAddress, email = email)))

    def insertOrUpdate(id:String, zoneID: String, accountID: String, name: String, abbreviation: Option[String], establishmentDate: Date, email: String, registeredAddress: Address, postalAddress: Address): Future[Int] =upsert(serialize(Organization(id = id, zoneID = zoneID, accountID = accountID, name = name, abbreviation = abbreviation, establishmentDate = establishmentDate, registeredAddress = registeredAddress, postalAddress = postalAddress, email = email)))

   /* {
      val id = getIDByAccountID(accountID).map(_.getOrElse(utilities.IDGenerator.hexadecimal))

      def upsertOrganization(id: String) = upsert(serialize(Organization(id = id, zoneID = zoneID, accountID = accountID, name = name, abbreviation = abbreviation, establishmentDate = establishmentDate, registeredAddress = registeredAddress, postalAddress = postalAddress, email = email)))

      for {
        id <- id
        _ <- upsertOrganization(id)
      } yield id
    }*/

    def getID(accountID: String): Future[Option[String]] = getIDByAccountID(accountID)

    def tryGetID(accountID: String): Future[String] = getIDByAccountID(accountID).map { id => id.getOrElse(throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)) }

    def tryGetVerifiedOrganizationID(accountID: String): Future[String] = getIDByAccountIDAndVerificationStatus(accountID = accountID, verificationStatus = Option(true)).map { id => id.getOrElse(throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)) }

    def getNameByID(id: String): Future[String] = findNameByID(id)

    def getNameByAccountID(accountID: String): Future[String] = findNameByAccountID(accountID)

    def tryGet(id: String): Future[Organization] = findById(id).map { organizationSerialized => organizationSerialized.deserialize }

    def getOrNone(id: String): Future[Option[Organization]] = findOrNoneByID(id).map(_.map(_.deserialize))

    def tryGetByAccountID(accountID: String): Future[Organization] = findByAccountID(accountID).map { organizationSerialized => organizationSerialized.deserialize }

    def getByAccountID(accountID: String): Future[Option[Organization]] = findOrNoneByAccountID(accountID).map(_.map(_.deserialize))

    def tryGetZoneID(id: String): Future[String] = getZoneIDByID(id)

    def getZoneIDByAccountID(accountID: String): Future[String] = getZoneIDOnAccountID(accountID)

    def markRejected(id: String, comment: Option[String]): Future[Int] = updateVerificationStatusAndCommentOnID(id = id, verificationStatus = Option(false), comment = comment)

    def markAccepted(id: String): Future[Int] = updateVerificationStatusAndCommentOnID(id = id, verificationStatus = Option(true), comment = None)

    def tryGetAccountID(id: String): Future[String] = getAccountIDByID(id)

    def getZoneAcceptedOrganizationList(zoneID: String): Future[Seq[Organization]] = getOrganizationsByCompletionStatusVerificationStatusAndZoneID(zoneID = zoneID, completionStatus = true, verificationStatus = Option(true)).map { organizations => organizations.map(_.deserialize) }

    def getZonePendingOrganizationRequestList(zoneID: String): Future[Seq[Organization]] = getOrganizationsByCompletionStatusVerificationStatusAndZoneID(zoneID = zoneID, completionStatus = true, verificationStatus = null).map { organizations => organizations.map(_.deserialize) }

    def getZoneRejectedOrganizationRequestList(zoneID: String): Future[Seq[Organization]] = getOrganizationsByCompletionStatusVerificationStatusAndZoneID(zoneID = zoneID, completionStatus = true, verificationStatus = Option(false)).map { organizations => organizations.map(_.deserialize) }

    def getVerificationStatus(id: String): Future[Boolean] = getVerificationStatusById(id).map {
      _.getOrElse(false)
    }

    def checkVerificationStatusByAccountID(accountID: String): Future[Boolean] = getVerificationStatusByAccountID(accountID).map {
      _.getOrElse(false)
    }

    def tryGetVerificationStatus(id: String): Future[Boolean] = {
      val verificationStatus = getVerificationStatusById(id)
      for {
        verificationStatus <- verificationStatus
      } yield {
        if (!verificationStatus.getOrElse(false)) throw new BaseException(constants.Response.UNVERIFIED_ORGANIZATION) else true
      }
    }

    def markOrganizationFormCompleted(id: String): Future[Int] = updateCompletionStatusOnID(id = id, completionStatus = true)

    def getOrganizations(organizationIDs: Seq[String]): Future[Seq[Organization]] = getOrganizationsByIDs(organizationIDs).map(_.map(_.deserialize))
  }

}