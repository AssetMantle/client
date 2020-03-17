package models.master

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Contact(id: String, mobileNumber: String, mobileNumberVerified: Boolean, emailAddress: String, emailAddressVerified: Boolean)

@Singleton
class Contacts @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.MASTER_CONTACT

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private val logger: Logger = Logger(this.getClass)

  private[models] val contactTable = TableQuery[ContactTable]

  private def add(contact: Contact): Future[String] = db.run((contactTable returning contactTable.map(_.id) += contact).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def findById(id: String): Future[Option[Contact]] = db.run(contactTable.filter(_.id === id).result.headOption.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def findByEmail(emailAddress: String): Future[Option[Contact]] = db.run(contactTable.filter(_.emailAddress === emailAddress).result.head.asTry).map {
    case Success(result) => Option(result)
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.info(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        None
    }
  }

  private def getEmailAddressById(id: String, emailAddressVerified: Option[Boolean]): Future[String] = db.run(contactTable.filter(_.id === id).filter(_.emailAddressVerified.? === emailAddressVerified).map(_.emailAddress).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def findOrNoneEmailAddressById(id: String, emailAddressVerified: Option[Boolean]): Future[Option[String]] = db.run(contactTable.filter(_.id === id).filter(_.emailAddressVerified.? === emailAddressVerified).map(_.emailAddress).result.head.asTry).map {
    case Success(result) => Option(result)
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.info(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        None
    }
  }

  private def findMobileNumberById(id: String): Future[String] = db.run(contactTable.filter(_.id === id).map(_.mobileNumber).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def findAccountIDByEmailAddress(emailAddress: String): Future[String] = db.run(contactTable.filter(_.emailAddress === emailAddress).map(_.id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def deleteById(id: String) = db.run(contactTable.filter(_.id === id).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def upsert(contact: Contact): Future[Int] = db.run(contactTable.insertOrUpdate(contact).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def updateMobileNumberVerificationStatusOnId(id: String, verificationStatus: Boolean): Future[Int] = db.run(contactTable.filter(_.id === id).map(_.mobileNumberVerified).update(verificationStatus).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateEmailVerificationStatusOnId(id: String, verificationStatus: Boolean): Future[Int] = db.run(contactTable.filter(_.id === id).map(_.emailAddressVerified).update(verificationStatus).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def checkByEmail(email: String): Future[Boolean] = db.run(contactTable.filter(_.emailAddress === email).exists.result)

  private def checkByMobileNumber(mobileNumber: String): Future[Boolean] = db.run(contactTable.filter(_.mobileNumber === mobileNumber).exists.result)

  private def getAllEmailIDsForAccountIDs(accountIDs: Seq[String]) = db.run(contactTable.filter(contacts => contacts.id inSet accountIDs).map(_.emailAddress).result)

  private def getAllContactsForAccountIDs(accountIDs: Seq[String]) = db.run(contactTable.filter(contacts => contacts.id inSet accountIDs).result)

  private def getContactByEmailId( email: String) = db.run(contactTable.filter(_.emailAddress === email).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def getContactsByEmailAddresses(emailIDs:Seq[String])=db.run(contactTable.filter(_.emailAddress inSet emailIDs).result)

  private[models] class ContactTable(tag: Tag) extends Table[Contact](tag, "Contact") {

    def * = (id, mobileNumber, mobileNumberVerified, emailAddress, emailAddressVerified) <> (Contact.tupled, Contact.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def mobileNumber = column[String]("mobileNumber")

    def mobileNumberVerified = column[Boolean]("mobileNumberVerified")

    def emailAddress = column[String]("emailAddress")

    def emailAddressVerified = column[Boolean]("emailAddressVerified")

  }

  object Service {

    def getOrNoneContact(id: String): Future[Option[Contact]] = findById(id)

    def getOrNoneContactByEmail(email: String): Future[Option[Contact]] = findByEmail(email)

    def insertOrUpdateContact(id: String, mobileNumber: String, emailAddress: String): Future[Boolean] = upsert(Contact(id, mobileNumber, mobileNumberVerified = false, emailAddress, emailAddressVerified = false)).map { value => if (value > 0) true else false }

    def verifyMobileNumber(id: String): Future[Int] = updateMobileNumberVerificationStatusOnId(id, verificationStatus = true)

    def verifyEmailAddress(id: String): Future[Int] = updateEmailVerificationStatusOnId(id, verificationStatus = true)

    def getVerifiedEmailAddress(id: String): Future[String] = getEmailAddressById(id = id, emailAddressVerified = Option(true))

    def getOrNoneVerifiedEmailAddress(id: String): Future[Option[String]] = findOrNoneEmailAddressById(id = id, emailAddressVerified = Option(true))

    def getUnverifiedEmailAddress(id: String): Future[String] = getEmailAddressById(id = id, emailAddressVerified = Option(false))

    def getMobileNumber(id: String): Future[String] = findMobileNumberById(id)

    def checkEmailPresence(email: String): Future[Boolean] = checkByEmail(email)

    def checkMobileNumberPresence(mobileNumber: String): Future[Boolean] = checkByMobileNumber(mobileNumber)

    def getAccountIDByEmailAddress(emailAddress: String): Future[String] = findAccountIDByEmailAddress(emailAddress)

  }

}

