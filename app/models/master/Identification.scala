package models.master

import java.sql.Date

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.common.Serializable.Address
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json
import slick.jdbc.JdbcProfile
import slick.lifted.TableQuery

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Identification(accountID: String, firstName: String, lastName: String, dateOfBirth: Date, idNumber: String, idType: String, address: Address, completionStatus: Boolean = false, verificationStatus: Option[Boolean] = None)

@Singleton
class Identifications @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) {
  val databaseConfig = databaseConfigProvider.get[JdbcProfile]
  val db = databaseConfig.db
  private[models] val identificationTable = TableQuery[IdentificationTable]

  private def serialize(identification: Identification): IdentificationSerialized = IdentificationSerialized( accountID = identification.accountID, firstName = identification.firstName, lastName = identification.lastName, dateOfBirth = identification.dateOfBirth, idNumber = identification.idNumber, idType = identification.idType, address = Json.toJson(identification.address).toString, completionStatus = identification.completionStatus, verificationStatus = identification.verificationStatus)

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.MASTER_IDENTIFICATION

  import databaseConfig.profile.api._

  private def add(identificationSerialized: IdentificationSerialized): Future[String] = db.run((identificationTable returning identificationTable.map(_.accountID) += identificationSerialized).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def upsert(identificationSerialized: IdentificationSerialized): Future[Int] = db.run(identificationTable.insertOrUpdate(identificationSerialized).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def updateVerificationStatusByAccountID(accountID: String, verificationStatus: Option[Boolean]): Future[Int] = db.run(identificationTable.filter(_.accountID === accountID).map(_.verificationStatus.?).update(verificationStatus).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateCompletionStatusByAccountID(accountID: String, completionStatus: Boolean): Future[Int] = db.run(identificationTable.filter(_.accountID === accountID).map(_.completionStatus).update(completionStatus).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getIdentificationOrNoneByAccountID(accountID: String): Future[Option[IdentificationSerialized]] = db.run(identificationTable.filter(_.accountID === accountID).result.head.asTry).map {
    case Success(result) => Option(result)
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        None
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def getIdentificationByAccountID(accountID: String) = db.run(identificationTable.filter(_.accountID === accountID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def getVerificationStatusByAccountID(accountID: String): Future[Option[Boolean]] = db.run(identificationTable.filter(_.accountID === accountID).map(_.verificationStatus.?).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  case class IdentificationSerialized(accountID: String, firstName: String, lastName: String, dateOfBirth: Date, idNumber: String, idType: String, address: String, completionStatus: Boolean, verificationStatus: Option[Boolean]) {

    def deserialize: Identification = Identification(accountID = accountID, firstName=firstName, lastName=lastName, dateOfBirth=dateOfBirth, idNumber=idNumber,idType=idType, address = utilities.JSON.convertJsonStringToObject[Address](address), completionStatus = completionStatus, verificationStatus = verificationStatus)

  }

  private[models] class IdentificationTable(tag: Tag) extends Table[IdentificationSerialized](tag, "Identification") {

    def * = (accountID, firstName, lastName, dateOfBirth, idNumber, idType, address, completionStatus, verificationStatus.?) <> (IdentificationSerialized.tupled, IdentificationSerialized.unapply)

    def accountID = column[String]("accountID", O.PrimaryKey)

    def firstName = column[String]("firstName")

    def lastName = column[String]("lastName")

    def dateOfBirth = column[Date]("dateOfBirth")

    def idNumber = column[String]("idNumber")

    def idType = column[String]("idType")

    def address = column[String]("address")

    def completionStatus = column[Boolean]("completionStatus")

    def verificationStatus = column[Boolean]("verificationStatus")

  }

  object Service {

    def create(accountID: String, firstName: String, lastName: String, dateOfBirth: Date, idNumber: String, idType: String, address: Address): Future[String] = add(serialize(Identification(accountID, firstName, lastName, dateOfBirth, idNumber, idType,address)))

    def insertOrUpdate(accountID: String, firstName: String, lastName: String, dateOfBirth: Date, idNumber: String, idType: String, address: Address): Future[Int] = upsert(serialize(Identification(accountID, firstName, lastName, dateOfBirth, idNumber, idType, address)))

    def markVerified(accountID: String): Future[Int] = updateVerificationStatusByAccountID(accountID = accountID, verificationStatus = Option(true))

    def markIdentificationFormCompleted(accountID: String): Future[Int] = updateCompletionStatusByAccountID(accountID = accountID, completionStatus = true)

    def get(accountID: String): Future[Identification] = getIdentificationByAccountID(accountID).map(_.deserialize)

    def getOrNoneByAccountID(accountID: String): Future[Option[Identification]] = getIdentificationOrNoneByAccountID(accountID).map(_.map(_.deserialize))

    def getName(accountID: String): Future[String] = getIdentificationByAccountID(accountID).map(id => id.firstName + " " + id.lastName)

    def getVerificationStatus(accountID: String): Future[Option[Boolean]] = getVerificationStatusByAccountID(accountID)
  }

}