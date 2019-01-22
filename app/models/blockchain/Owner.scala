package models.blockchain

import javax.inject.Inject
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

case class Owner(pegHash: String, ownerAddress: String, amount: Int)

class Owners @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]
  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val ownerTable = TableQuery[OwnerTable]

  private def add(owner: Owner): Future[String] = db.run(ownerTable returning ownerTable.map(_.pegHash) += owner)

  private def findByPegHashOwnerAddress(pegHash: String, ownerAddress: String): Future[Owner] = db.run(ownerTable.filter(_.pegHash === pegHash).filter(_.ownerAddress === ownerAddress).result.head)

  private def deleteByPegHashOwnerAddress(pegHash: String, ownerAddress: String) = db.run(ownerTable.filter(_.pegHash === pegHash).filter(_.ownerAddress === ownerAddress).delete)


  private[models] class OwnerTable(tag: Tag) extends Table[Owner](tag, "Owner_BC") {

    def * = (pegHash, ownerAddress, amount) <> (Owner.tupled, Owner.unapply)

    def ? = (pegHash.?, ownerAddress.?, amount.?).shaped.<>({ r => import r._; _1.map(_ => Owner.tupled((_1.get, _2.get, _3.get))) }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))

    def pegHash = column[String]("pegHash", O.PrimaryKey)

    def ownerAddress = column[String]("ownerAddress", O.PrimaryKey)

    def amount = column[Int]("amount")


  }

}