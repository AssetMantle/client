package models.master

import java.sql.{Date, Timestamp}

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Trait.Logged
import models.common.Serializable.Address
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json
import slick.jdbc.JdbcProfile
import slick.lifted.TableQuery

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Identification(accountID: String, firstName: String, lastName: String, dateOfBirth: Date, idNumber: String, idType: String, address: Address, completionStatus: Boolean = false, verificationStatus: Option[Boolean] = None, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged

@Singleton
class Identifications @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private[models] val identificationTable = TableQuery[IdentificationTable]

  private def serialize(identification: Identification): IdentificationSerialized = IdentificationSerialized(accountID = identification.accountID, firstName = identification.firstName, lastName = identification.lastName, dateOfBirth = identification.dateOfBirth, idNumber = identification.idNumber, idType = identification.idType, address = Json.toJson(identification.address).toString, completionStatus = identification.completionStatus, verificationStatus = identification.verificationStatus, createdBy = identification.createdBy, createdOn = identification.createdOn, createdOnTimeZone = identification.createdOnTimeZone, updatedBy = identification.updatedBy, updatedOn = identification.updatedOn, updatedOnTimeZone = identification.updatedOnTimeZone)

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.MASTER_IDENTIFICATION

  import databaseConfig.profile.api._

  private def add(identificationSerialized: IdentificationSerialized): Future[String] = db.run((identificationTable returning identificationTable.map(_.accountID) += identificationSerialized).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def upsert(identificationSerialized: IdentificationSerialized): Future[Int] = db.run(identificationTable.insertOrUpdate(identificationSerialized).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def updateVerificationStatusByAccountID(accountID: String, verificationStatus: Option[Boolean]): Future[Int] = db.run(identificationTable.filter(_.accountID === accountID).map(_.verificationStatus.?).update(verificationStatus).asTry).map {
    case Success(result) => result match {
      case 0 => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      case _ => result
    }
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def updateCompletionStatusByAccountID(accountID: String, completionStatus: Boolean): Future[Int] = db.run(identificationTable.filter(_.accountID === accountID).map(_.completionStatus).update(completionStatus).asTry).map {
    case Success(result) => result match {
      case 0 => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      case _ => result
    }
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def getByAccountID(accountID: String): Future[Option[IdentificationSerialized]] = db.run(identificationTable.filter(_.accountID === accountID).result.headOption)

  private def tryGetByAccountID(accountID: String): Future[IdentificationSerialized] = db.run(identificationTable.filter(_.accountID === accountID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def tryGetVerificationStatusByAccountID(accountID: String): Future[Option[Boolean]] = db.run(identificationTable.filter(_.accountID === accountID).map(_.verificationStatus.?).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  case class IdentificationSerialized(accountID: String, firstName: String, lastName: String, dateOfBirth: Date, idNumber: String, idType: String, address: String, completionStatus: Boolean, verificationStatus: Option[Boolean], createdBy: Option[String], createdOn: Option[Timestamp], createdOnTimeZone: Option[String], updatedBy: Option[String], updatedOn: Option[Timestamp], updatedOnTimeZone: Option[String]) {

    def deserialize: Identification = Identification(accountID = accountID, firstName = firstName, lastName = lastName, dateOfBirth = dateOfBirth, idNumber = idNumber, idType = idType, address = utilities.JSON.convertJsonStringToObject[Address](address), completionStatus = completionStatus, verificationStatus = verificationStatus, createdBy = createdBy, createdOn = createdOn, createdOnTimeZone = createdOnTimeZone, updatedBy = updatedBy, updatedOn = updatedOn, updatedOnTimeZone = updatedOnTimeZone)

  }

  private[models] class IdentificationTable(tag: Tag) extends Table[IdentificationSerialized](tag, "Identification") {

    def * = (accountID, firstName, lastName, dateOfBirth, idNumber, idType, address, completionStatus, verificationStatus.?, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (IdentificationSerialized.tupled, IdentificationSerialized.unapply)

    def accountID = column[String]("accountID", O.PrimaryKey)

    def firstName = column[String]("firstName")

    def lastName = column[String]("lastName")

    def dateOfBirth = column[Date]("dateOfBirth")

    def idNumber = column[String]("idNumber")

    def idType = column[String]("idType")

    def address = column[String]("address")

    def completionStatus = column[Boolean]("completionStatus")

    def verificationStatus = column[Boolean]("verificationStatus")

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")

  }

  object Service {

    def create(accountID: String, firstName: String, lastName: String, dateOfBirth: Date, idNumber: String, idType: String, address: Address): Future[String] = add(serialize(Identification(accountID, firstName, lastName, dateOfBirth, idNumber, idType, address)))

    def insertOrUpdate(accountID: String, firstName: String, lastName: String, dateOfBirth: Date, idNumber: String, idType: String, address: Address): Future[Int] = upsert(serialize(Identification(accountID, firstName, lastName, dateOfBirth, idNumber, idType, address)))

    def markVerified(accountID: String): Future[Int] = updateVerificationStatusByAccountID(accountID = accountID, verificationStatus = Option(true))

    def markIdentificationFormCompleted(accountID: String): Future[Int] = updateCompletionStatusByAccountID(accountID = accountID, completionStatus = true)

    def tryGet(accountID: String): Future[Identification] = tryGetByAccountID(accountID).map(_.deserialize)

    def get(accountID: String): Future[Option[Identification]] = getByAccountID(accountID).map(_.map(_.deserialize))

    def tryGetName(accountID: String): Future[String] = tryGetByAccountID(accountID).map(id => id.firstName + " " + id.lastName)

    def getVerificationStatus(accountID: String): Future[Option[Boolean]] = tryGetVerificationStatusByAccountID(accountID)
  }

}
