package models.blockchain

import javax.inject.Inject
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

case class Negotiation(id: String, buyerAddress: String, sellerAddress: String, assetPegHash: String, bid: Int, time: Int, buyerSignature: String, sellerSignature: String)

class Negotiations @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]
  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val negotiationTable = TableQuery[NegotiationTable]

  def add(negotiation: Negotiation): Future[String] = db.run(negotiationTable returning negotiationTable.map(_.id) += negotiation)

  def findById(id: String): Future[Negotiation] = db.run(negotiationTable.filter(_.id === id).result.head)

  def deleteById(id: String) = db.run(negotiationTable.filter(_.id === id).delete)

  private[models] class NegotiationTable(tag: Tag) extends Table[Negotiation](tag, "Negotiation_BC") {

    def * = (id, buyerAddress, sellerAddress, assetPegHash, bid, time, buyerSignature, sellerSignature) <> (Negotiation.tupled, Negotiation.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def buyerAddress = column[String]("buyerAddress")

    def sellerAddress = column[String]("sellerAddress")

    def assetPegHash = column[String]("assetPegHash")

    def bid = column[Int]("bid")

    def time = column[Int]("time")

    def buyerSignature = column[String]("buyerSignature")

    def sellerSignature = column[String]("sellerSignature")

    def ? = (id.?, buyerAddress.?, sellerAddress.?, assetPegHash.?, bid.?, time.?, buyerSignature.?, sellerSignature.?).shaped.<>({ r => import r._; _1.map(_ => Negotiation.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get))) }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))


  }

}