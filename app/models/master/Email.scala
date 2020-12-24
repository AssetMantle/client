package models.master

import java.sql.Timestamp

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Trait.Logged
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Email(id: String, emailAddress: String, status: Boolean = false, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged

@Singleton
class Emails @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.MASTER_EMAIL_ADDRESS

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private implicit val logger: Logger = Logger(this.getClass)

  private[models] val emailTable = TableQuery[EmailTable]

  private def add(email: Email): Future[String] = db.run((emailTable returning emailTable.map(_.id) += email).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def getByID(id: String): Future[Option[Email]] = db.run(emailTable.filter(_.id === id).result.headOption)

  private def tryGetByID(id: String): Future[Email] = db.run(emailTable.filter(_.id === id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def tryGetEmailAddressByIDAndStatus(id: String, status: Boolean): Future[String] = db.run(emailTable.filter(x => x.id === id && x.status === status).map(_.emailAddress).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def getEmailAddressById(id: String, status: Boolean): Future[Option[String]] = db.run(emailTable.filter(x => x.id === id && x.status === status).map(_.emailAddress).result.headOption)

  private def getAccountIDByEmailAddress(emailAddress: String): Future[Option[String]] = db.run(emailTable.filter(_.emailAddress === emailAddress).map(_.id).result.headOption)

  private def deleteById(id: String) = db.run(emailTable.filter(_.id === id).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def updateEmailAddressVerificationStatusOnId(id: String, verificationStatus: Boolean): Future[Int] = db.run(emailTable.filter(_.id === id).map(_.status).update(verificationStatus).asTry).map {
    case Success(result) => result match {
      case 0 => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      case _ => result
    }
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def updateEmailAddressVerificationStatusOnEmailAddress(emailAddress: String, verificationStatus: Boolean): Future[Int] = db.run(emailTable.filter(_.emailAddress === emailAddress).map(_.status).update(verificationStatus).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def updateEmailAddressAndStatusByID(id: String, emailAddress: String, status: Boolean): Future[Int] = db.run(emailTable.filter(_.id === id).map(x => (x.emailAddress, x.status)).update((emailAddress, status)).asTry).map {
    case Success(result) => result match {
      case 0 => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      case _ => result
    }
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private[models] class EmailTable(tag: Tag) extends Table[Email](tag, "Email") {

    def * = (id, emailAddress, status, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (Email.tupled, Email.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def emailAddress = column[String]("emailAddress")

    def status = column[Boolean]("status")

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")

  }

  object Service {

    def get(id: String): Future[Option[Email]] = getByID(id)

    def tryGet(id: String): Future[Email] = tryGetByID(id)

    def create(id: String, emailAddress: String): Future[String] = add(Email(id = id, emailAddress = emailAddress))

    def unVerifyOldEmailAddresses(emailAddress: String): Future[Int] = updateEmailAddressVerificationStatusOnEmailAddress(emailAddress, verificationStatus = false)

    def verifyEmailAddress(id: String): Future[Int] = {
      def verify: Future[Int] = updateEmailAddressVerificationStatusOnId(id, verificationStatus = true)

      for {
        email <- tryGet(id)
        _ <- unVerifyOldEmailAddresses(email.emailAddress)
        verify <- verify
      } yield verify
    }

    def tryGetVerifiedEmailAddress(id: String): Future[String] = tryGetEmailAddressByIDAndStatus(id = id, status = true)

    def getVerifiedEmailAddress(id: String): Future[Option[String]] = getEmailAddressById(id = id, status = true)

    def tryGetUnverifiedEmailAddress(id: String): Future[String] = tryGetEmailAddressByIDAndStatus(id = id, status = false)

    def getEmailAddressAccount(emailAddress: String): Future[Option[String]] = getAccountIDByEmailAddress(emailAddress)

    def updateEmailAddress(id: String, emailAddress: String): Future[Int] = updateEmailAddressAndStatusByID(id = id, emailAddress = emailAddress, status = false)
  }

}

