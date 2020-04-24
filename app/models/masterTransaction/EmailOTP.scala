package models.masterTransaction

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Random, Success}

case class EmailOTP(id: String, secretHash: String)

@Singleton
class EmailOTPs @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.MASTER_TRANSACTION_EMAIL_OTP

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private val logger: Logger = Logger(this.getClass)

  import databaseConfig.profile.api._

  private[models] val emailOTPTable = TableQuery[EmailOTPTable]

  private def add(emailOTP: EmailOTP): Future[String] = db.run((emailOTPTable returning emailOTPTable.map(_.id) += emailOTP).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def upsert(emailOTP: EmailOTP): Future[Int] = db.run(emailOTPTable.insertOrUpdate(emailOTP).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def findById(id: String): Future[EmailOTP] = db.run(emailOTPTable.filter(_.id === id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def deleteById(id: String): Future[Int] = db.run(emailOTPTable.filter(_.id === id).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private[models] class EmailOTPTable(tag: Tag) extends Table[EmailOTP](tag, "EmailOTP") {

    def * = (id, secretHash) <> (EmailOTP.tupled, EmailOTP.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def secretHash = column[String]("secretHash")

  }

  object Service {
    def get(id: String): Future[String] = {
      val otp = (Random.nextInt(899999) + 100000).toString
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
