package models.masterTransaction

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Random, Success}

case class SMSOTP(id: String, secretHash: String)

@Singleton
class SMSOTPs @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider) {

  private implicit val module: String = constants.Module.MASTER_TRANSACTION_SMS_OTP

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]
  val db = databaseConfig.db

  private val logger: Logger = Logger(this.getClass)

  import databaseConfig.profile.api._

  private[models] val smsOTPTable = TableQuery[SMSOTPTable]

  private def add(smsOTP: SMSOTP): Future[String] = db.run(smsOTPTable returning smsOTPTable.map(_.id) += smsOTP)

  private def update(smsOTP: SMSOTP): Future[Int] = db.run(smsOTPTable.insertOrUpdate(smsOTP))


  private def findById(id: String)(implicit executionContext: ExecutionContext): Future[SMSOTP] = db.run(smsOTPTable.filter(_.id === id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def deleteById(id: String) = db.run(smsOTPTable.filter(_.id === id).delete)

  private[models] class SMSOTPTable(tag: Tag) extends Table[SMSOTP](tag, "SMSOTP") {

    def * = (id, secretHash) <> (SMSOTP.tupled, SMSOTP.unapply)

    def ? = (id.?, secretHash.?).shaped.<>({ r => import r._; _1.map(_ => SMSOTP.tupled((_1.get, _2.get))) }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))

    def id = column[String]("id", O.PrimaryKey)

    def secretHash = column[String]("secretHash")
  }

  object Service {

    def sendOTP(id: String): String = {
      val otp = (Random.nextInt(899999) + 100000).toString
      if (Await.result(update(SMSOTP(id, util.hashing.MurmurHash3.stringHash(otp).toString)), Duration.Inf) == 0) throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      otp
    }

    def verifyOTP(id: String, otp: String)(implicit executionContext: ExecutionContext): Boolean = {
      if (Await.result(findById(id), Duration.Inf).secretHash != util.hashing.MurmurHash3.stringHash(otp).toString) throw new BaseException(constants.Response.INVALID_OTP)
      true
    }

  }

}
