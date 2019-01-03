package models.blockchain

import javax.inject.Inject
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

case class ACL(address: String, zoneID: String, organizationID: String, transaction: String)

class ACLs @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]
  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val aclTable = TableQuery[ACLTable]

  def add(acl: ACL): Future[String] = db.run(aclTable returning aclTable.map(_.address) += acl)

  def findByAddress(address: String): Future[ACL] = db.run(aclTable.filter(_.address === address).result.head)

  def deleteByAddress(address: String) = db.run(aclTable.filter(_.address === address).delete)

  private[models] class ACLTable(tag: Tag) extends Table[ACL](tag, "ACL_BC") {

    def * = (address, zoneID, organizationID, transactions) <> (ACL.tupled, ACL.unapply)

    def ? = (address.?, zoneID.?, organizationID.?, transactions.?).shaped.<>({ r => import r._; _1.map(_ => ACL.tupled((_1.get, _2.get, _3.get, _4.get))) }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))

    def address = column[String]("address", O.PrimaryKey)

    def zoneID = column[String]("zoneID")

    def organizationID = column[String]("organizationID")

    def transactions = column[String]("transactions")
  }

}