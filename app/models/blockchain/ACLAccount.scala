package models.blockchain

import javax.inject.Inject
import models.blockchainTransaction.SetACLs
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

case class ACLAccount(address: String, zoneID: String, organizationID: String, aclHash: String)

case class ACL(issueAsset: Boolean, issueFiat: Boolean, sendAsset: Boolean, sendFiat: Boolean, redeemAsset: Boolean, redeemFiat: Boolean, sellerExecuteOrder: Boolean, buyerExecuteOrder: Boolean, changeBuyerBid: Boolean, changeSellerBid: Boolean, confirmBuyerBid: Boolean, confirmSellerBid: Boolean, negotiation: Boolean, releaseAsset: Boolean)

class ACLAccounts @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider, aclHashs: ACLHashs) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]
  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val aclTable = TableQuery[ACLTable]

  private def add(aclAccount: ACLAccount): Future[String] = db.run(aclTable returning aclTable.map(_.address) += aclAccount)

  private def findByAddress(address: String): Future[ACLAccount] = db.run(aclTable.filter(_.address === address).result.head)

  private def deleteByAddress(address: String) = db.run(aclTable.filter(_.address === address).delete)

  private[models] class ACLTable(tag: Tag) extends Table[ACLAccount](tag, "ACLAccount_BC") {

    def * = (address, zoneID, organizationID, aclHash) <> (ACLAccount.tupled, ACLAccount.unapply)

    def address = column[String]("address", O.PrimaryKey)

    def zoneID = column[String]("zoneID")

    def organizationID = column[String]("organizationID")

    def aclHash = column[String]("aclHash")
  }

  object Service {

    def addACLAccount(from: String, address: String, zoneID: String, organizationID: String, chainID: String, acl: ACL)(implicit executionContext: ExecutionContext): String = Await.result(add(ACLAccount(address, zoneID, organizationID, util.hashing.MurmurHash3.stringHash(acl.toString).toString)), Duration.Inf)

    def getACLAccount(address: String): ACLAccount = Await.result(findByAddress(address), Duration.Inf)
  }
}