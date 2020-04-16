package models.master

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Contact(id: String, mobileNumber: String, mobileNumberVerified: Boolean = false, emailAddress: String, emailAddressVerified: Boolean = false)

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

  private def getByID(id: String): Future[Option[Contact]] = db.run(contactTable.filter(_.id === id).result.headOption)

  private def tryGet(id: String): Future[Contact] = db.run(contactTable.filter(_.id === id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def tryGetEmailAddressByIDAndStatus(id: String, emailAddressVerified: Boolean): Future[String] = db.run(contactTable.filter(_.id === id).filter(_.emailAddressVerified === emailAddressVerified).map(_.emailAddress).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getEmailAddressById(id: String, emailAddressVerified: Boolean): Future[Option[String]] = db.run(contactTable.filter(_.id === id).filter(_.emailAddressVerified === emailAddressVerified).map(_.emailAddress).result.headOption)

  private def tryGetMobileNumberByIDAndStatus(id: String, mobileNumberVerified: Boolean): Future[String] = db.run(contactTable.filter(_.id === id).filter(_.mobileNumberVerified === mobileNumberVerified).map(_.mobileNumber).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getAccountIDByEmailAddress(emailAddress: String): Future[Option[String]] = db.run(contactTable.filter(_.emailAddress === emailAddress).map(_.id).result.headOption)

  private def getAccountIDByMobileNumber(mobileNumber: String): Future[Option[String]] = db.run(contactTable.filter(_.mobileNumber === mobileNumber).map(_.id).result.headOption)

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
    case Success(result) => result match {
      case 0 => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      case _ => result
    }
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def updateEmailAddressVerificationStatusOnId(id: String, verificationStatus: Boolean): Future[Int] = db.run(contactTable.filter(_.id === id).map(_.emailAddressVerified).update(verificationStatus).asTry).map {
    case Success(result) => result match {
      case 0 => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      case _ => result
    }
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def updateEmailAddressAndStatusByID(id: String, emailAddress: String, emailAddressVerified: Boolean): Future[Int] = db.run(contactTable.filter(_.id === id).map(x => (x.emailAddress, x.emailAddressVerified)).update((emailAddress, emailAddressVerified)).asTry).map {
    case Success(result) => result match {
      case 0 => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      case _ => result
    }
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def updateMobileNumberAndStatusByID(id: String, mobileNumber: String, mobileNumberVerified: Boolean): Future[Int] = db.run(contactTable.filter(_.id === id).map(x => (x.mobileNumber, x.mobileNumberVerified)).update((mobileNumber, mobileNumberVerified)).asTry).map {
    case Success(result) => result match {
      case 0 => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      case _ => result
    }
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private[models] class ContactTable(tag: Tag) extends Table[Contact](tag, "Contact") {

    def * = (id, mobileNumber, mobileNumberVerified, emailAddress, emailAddressVerified) <> (Contact.tupled, Contact.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def mobileNumber = column[String]("mobileNumber")

    def mobileNumberVerified = column[Boolean]("mobileNumberVerified")

    def emailAddress = column[String]("emailAddress")

    def emailAddressVerified = column[Boolean]("emailAddressVerified")

  }

  object Service {

    def get(id: String): Future[Option[Contact]] = getByID(id)

    def tryGet(id: String): Future[Contact] = tryGet(id)

    def create(id: String, mobileNumber: String, emailAddress: String): Future[String] = add(Contact(id = id, mobileNumber = mobileNumber, emailAddress = emailAddress))

    def updateEmailAddressVerificationStatus(id: String, emailAddressVerificationStatus: Boolean): Future[Int] = updateEmailAddressVerificationStatusOnId(id, emailAddressVerificationStatus)

    def updateMobileNumberVerificationStatus(id: String, mobileNumberVerificationStatus: Boolean): Future[Int] = updateMobileNumberVerificationStatusOnId(id, mobileNumberVerificationStatus)

    def verifyMobileNumber(id: String): Future[Int] = updateMobileNumberVerificationStatusOnId(id, verificationStatus = true)

    def verifyEmailAddress(id: String): Future[Int] = updateEmailAddressVerificationStatusOnId(id, verificationStatus = true)

    def tryGetVerifiedEmailAddress(id: String): Future[String] = tryGetEmailAddressByIDAndStatus(id = id, emailAddressVerified = true)

    def getVerifiedEmailAddress(id: String): Future[Option[String]] = getEmailAddressById(id = id, emailAddressVerified = true)

    def tryGetUnverifiedEmailAddress(id: String): Future[String] = tryGetEmailAddressByIDAndStatus(id = id, emailAddressVerified = false)

    def tryGetVerifiedMobileNumber(id: String): Future[String] = tryGetMobileNumberByIDAndStatus(id, mobileNumberVerified = true)

    def tryGetUnverifiedMobileNumber(id: String): Future[String] = tryGetMobileNumberByIDAndStatus(id, mobileNumberVerified = false)

    def getEmailAddressAccount(emailAddress: String): Future[Option[String]] = getAccountIDByEmailAddress(emailAddress)

    def getMobileNumberAccount(mobileNumber: String): Future[Option[String]] = getAccountIDByMobileNumber(mobileNumber)

    def updateEmailAddress(id: String, emailAddress: String): Future[Int] = updateEmailAddressAndStatusByID(id = id, emailAddress = emailAddress, emailAddressVerified = false)

    def updateMobileNumber(id: String, mobileNumber: String): Future[Int] = updateMobileNumberAndStatusByID(id = id, mobileNumber = mobileNumber, mobileNumberVerified = false)
  }

}

