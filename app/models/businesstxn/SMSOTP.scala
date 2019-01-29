package models.businesstxn

import javax.inject.Inject
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.util.Random

case class SMSOTP(id: String, secretHash: String)

class SMSOTPs @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]
  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val smsOTPTable = TableQuery[SMSOTPTable]

  private def add(smsOTP: SMSOTP): Future[String] = db.run(smsOTPTable returning smsOTPTable.map(_.id) += smsOTP)

  private def update(smsOTP: SMSOTP): Future[Int] = db.run(smsOTPTable.insertOrUpdate(smsOTP))

  private def findById(id: String): Future[SMSOTP] = db.run(smsOTPTable.filter(_.id === id).result.head)

  private def deleteById(id: String) = db.run(smsOTPTable.filter(_.id === id).delete)

  private[models] class SMSOTPTable(tag: Tag) extends Table[SMSOTP](tag, "SMSOTP") {

    def * = (id, secretHash) <> (SMSOTP.tupled, SMSOTP.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def secretHash = column[String]("secretHash")

    def ? = (id.?, secretHash.?).shaped.<>({ r => import r._; _1.map(_ => SMSOTP.tupled((_1.get, _2.get))) }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))
  }

  object Service {

    def sendOTP(id: String): Int = Await.result(update(new SMSOTP(id, util.hashing.MurmurHash3.stringHash((Random.nextInt(899999) + 100000).toString).toString)), Duration.Inf)

    def verifyOTP(id: String, otp: String): Boolean = Await.result(findById(id), Duration.Inf).secretHash == util.hashing.MurmurHash3.stringHash(otp).toString
  }

}
