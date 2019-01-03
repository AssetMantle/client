package models.blockchain

import javax.inject.Inject
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

case class Fiat(pegHash: String, transactionID: String, transactionAmount: Int, redeemedAmount: Int)

class Fiats @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]
  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val fiatTable = TableQuery[FiatTable]

  def add(fiat: Fiat): Future[String] = db.run(fiatTable returning fiatTable.map(_.pegHash) += fiat)

  def findBypegHash(pegHash: String): Future[Fiat] = db.run(fiatTable.filter(_.pegHash === pegHash).result.head)

  def deleteBypegHash(pegHash: String) = db.run(fiatTable.filter(_.pegHash === pegHash).delete)

  private[models] class FiatTable(tag: Tag) extends Table[Fiat](tag, "Fiat_BC") {

    def * = (pegHash, transactionID, transactionAmount, redeemedAmount) <> (Fiat.tupled, Fiat.unapply)

    def pegHash = column[String]("pegHash", O.PrimaryKey)

    def transactionID = column[String]("transactionID")

    def transactionAmount = column[Int]("transactionAmount")

    def redeemedAmount = column[Int]("redeemedAmount")

    def ? = (pegHash.?, transactionID.?, transactionAmount.?, redeemedAmount.?).shaped.<>({ r => import r._; _1.map(_ => Fiat.tupled((_1.get, _2.get, _3.get, _4.get))) }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))
  }

}