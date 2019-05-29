package models.masterTransaction

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Random, Success}

case class EmailOTP(id: String, secretHash: String)

@Singleton
class EmailOTPs @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider) {

  private implicit val module: String = constants.Module.MASTER_TRANSACTION_EMAIL_OTP

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]
  val db = databaseConfig.db

  private val logger: Logger = Logger(this.getClass)

  import databaseConfig.profile.api._

  private[models] val emailOTPTable = TableQuery[EmailOTPTable]

  private def add(emailOTP: EmailOTP): Future[String] = db.run(emailOTPTable returning emailOTPTable.map(_.id) += emailOTP)

  private def update(emailOTP: EmailOTP): Future[Int] = db.run(emailOTPTable.insertOrUpdate(emailOTP))

  private def findById(id: String)(implicit executionContext: ExecutionContext): Future[EmailOTP] = db.run(emailOTPTable.filter(_.id === id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def deleteById(id: String) = db.run(emailOTPTable.filter(_.id === id).delete)

  private[models] class EmailOTPTable(tag: Tag) extends Table[EmailOTP](tag, "EmailOTP") {

    def * = (id, secretHash) <> (EmailOTP.tupled, EmailOTP.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def secretHash = column[String]("secretHash")

    def ? = (id.?, secretHash.?).shaped.<>({ r => import r._; _1.map(_ => EmailOTP.tupled((_1.get, _2.get))) }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))
  }

  object Service {

    def sendOTP(id: String) = {
      val otp = (Random.nextInt(899999) + 100000).toString;
      if (Await.result(update(new EmailOTP(id, util.hashing.MurmurHash3.stringHash(otp).toString)), Duration.Inf) == 0) throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      otp
    }

    def verifyOTP(id: String, otp: String)(implicit executionContext: ExecutionContext): Boolean = Await.result(findById(id), Duration.Inf).secretHash == util.hashing.MurmurHash3.stringHash(otp).toString
  }

}
