package models.masterTransaction

import java.sql.Timestamp

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Trait.Logged
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Random, Success}

case class EmailOTP(id: String, secretHash: String, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged

@Singleton
class EmailOTPs @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.MASTER_TRANSACTION_EMAIL_OTP

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  import databaseConfig.profile.api._

  private[models] val emailOTPTable = TableQuery[EmailOTPTable]

  private def add(emailOTP: EmailOTP): Future[String] = db.run((emailOTPTable returning emailOTPTable.map(_.id) += emailOTP).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def upsert(emailOTP: EmailOTP): Future[Int] = db.run(emailOTPTable.insertOrUpdate(emailOTP).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def findById(id: String): Future[EmailOTP] = db.run(emailOTPTable.filter(_.id === id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def deleteById(id: String): Future[Int] = db.run(emailOTPTable.filter(_.id === id).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private[models] class EmailOTPTable(tag: Tag) extends Table[EmailOTP](tag, "EmailOTP") {

    def * = (id, secretHash, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (EmailOTP.tupled, EmailOTP.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def secretHash = column[String]("secretHash")

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")

  }

  object Service {
    def get(id: String): Future[String] = {
      //val otp = (Random.nextInt(899999) + 100000).toString\
      val otp = 999999.toString
      val upsertEmailOTP = upsert(EmailOTP(id, util.hashing.MurmurHash3.stringHash(otp).toString))
      for {
        _ <- upsertEmailOTP
      } yield otp
    }

    def verifyOTP(id: String, otp: String): Future[Boolean] = {
      val emailOTP = findById(id)
      for {
        emailOTP <- emailOTP
      } yield if (emailOTP.secretHash != util.hashing.MurmurHash3.stringHash(otp).toString) throw new BaseException(constants.Response.INVALID_OTP) else true
    }
  }

}
