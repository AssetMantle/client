package models.masterTransaction

import javax.inject.Inject
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.util.Random

case class EmailOTP(id: String, secretHash: String)

class EmailOTPs @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]
  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val emailOTPTable = TableQuery[EmailOTPTable]

  private def add(emailOTP: EmailOTP): Future[String] = db.run(emailOTPTable returning emailOTPTable.map(_.id) += emailOTP)

  private def update(emailOTP: EmailOTP): Future[Int] = db.run(emailOTPTable.insertOrUpdate(emailOTP))

  private def findById(id: String): Future[EmailOTP] = db.run(emailOTPTable.filter(_.id === id).result.head)

  private def deleteById(id: String) = db.run(emailOTPTable.filter(_.id === id).delete)

  private[models] class EmailOTPTable(tag: Tag) extends Table[EmailOTP](tag, "EmailOTP") {

    def * = (id, secretHash) <> (EmailOTP.tupled, EmailOTP.unapply)

    def ? = (id.?, secretHash.?).shaped.<>({ r => import r._; _1.map(_ => EmailOTP.tupled((_1.get, _2.get))) }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))

    def id = column[String]("id", O.PrimaryKey)

    def secretHash = column[String]("secretHash")
  }

  object Service {

    def sendOTP(id: String): Int = {
      val a = (Random.nextInt(899999) + 100000);
      Await.result(update(new EmailOTP(id, util.hashing.MurmurHash3.stringHash(a.toString).toString)), Duration.Inf)
    }

    def verifyOTP(id: String, otp: String): Boolean = Await.result(findById(id), Duration.Inf).secretHash == util.hashing.MurmurHash3.stringHash(otp).toString
  }

}
