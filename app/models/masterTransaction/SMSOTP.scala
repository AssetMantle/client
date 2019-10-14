package models.masterTransaction

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Random, Success}

case class SMSOTP(id: String, secretHash: String)

@Singleton
class SMSOTPs @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.MASTER_TRANSACTION_SMS_OTP

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]
  val db = databaseConfig.db

  private val logger: Logger = Logger(this.getClass)

  import databaseConfig.profile.api._

  private[models] val smsOTPTable = TableQuery[SMSOTPTable]

  private def add(smsOTP: SMSOTP): Future[String] = db.run((smsOTPTable returning smsOTPTable.map(_.id) += smsOTP).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def upsert(smsOTP: SMSOTP): Future[Int] = db.run(smsOTPTable.insertOrUpdate(smsOTP).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def findById(id: String): Future[SMSOTP] = db.run(smsOTPTable.filter(_.id === id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def deleteById(id: String) = db.run(smsOTPTable.filter(_.id === id).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private[models] class SMSOTPTable(tag: Tag) extends Table[SMSOTP](tag, "SMSOTP") {

    def * = (id, secretHash) <> (SMSOTP.tupled, SMSOTP.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def secretHash = column[String]("secretHash")
  }

  object Service {

    def sendOTP(id: String): Future[String] = {
      val otp = (Random.nextInt(899999) + 100000).toString
      val upsertOtp =upsert(SMSOTP(id, util.hashing.MurmurHash3.stringHash(otp).toString))
      for{
        _<-upsertOtp
      } yield otp

    }

    def verifyOTP(id: String, otp: String): Future[Boolean] = {
      findById(id).map{smsOTP=>
        if(smsOTP.secretHash!=util.hashing.MurmurHash3.stringHash(otp).toString)throw new BaseException(constants.Response.INVALID_OTP) else true
      }
    }

  }

}
