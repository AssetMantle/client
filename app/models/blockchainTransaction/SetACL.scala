package models.blockchainTransaction

import javax.inject.Inject
import models.blockchain.ACL
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json
import slick.jdbc.JdbcProfile

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration.Duration

case class SetACL(from: String, aclAddress: String, organizationID: String, zoneID: String, chainID: String, aclHash: String,  status: Option[Boolean], txHash: Option[String], ticketID: String, responseCode: Option[String])

class SetACLs @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val setACLTable = TableQuery[SetACLTable]

  private def add(setACL: SetACL): Future[String] = db.run(setACLTable returning setACLTable.map(_.ticketID) += setACL)

  private def update(setACL: SetACL): Future[Int] = db.run(setACLTable.insertOrUpdate(setACL))

  private def findByTicketID(ticketID: String): Future[SetACL] = db.run(setACLTable.filter(_.ticketID === ticketID).result.head)

  private def deleteByTicketID(ticketID: String) = db.run(setACLTable.filter(_.ticketID === ticketID).delete)

  private[models] class SetACLTable(tag: Tag) extends Table[SetACL](tag, "SetACL") {

    def * = (from, aclAddress, organizationID, zoneID, chainID, aclHash, status.?, txHash.?, ticketID, responseCode.?) <> (SetACL.tupled, SetACL.unapply)

    def from = column[String]("from")

    def aclAddress = column[String]("aclAddress")

    def organizationID = column[String]("organizationID")

    def zoneID = column[String]("zoneID")

    def chainID = column[String]("chainID")

    def aclHash = column[String]("aclHash")

    def status = column[Boolean]("status")

    def txHash = column[String]("txHash")

    def ticketID = column[String]("ticketID", O.PrimaryKey)

    def responseCode = column[String]("responseCode")
  }

  object Service {

    def addSetACL(from: String, aclAddress: String, organizationID: String, zoneID: String, chainID: String, acl: ACL,  status: Option[Boolean], txHash: Option[String], ticketID: String, responseCode: Option[String])(implicit executionContext: ExecutionContext): String = Await.result(add(SetACL(from, aclAddress, organizationID, zoneID, chainID, util.hashing.MurmurHash3.stringHash(acl.toString).toString, status, txHash, ticketID, responseCode)), Duration.Inf)

    def getSetACL(ticketID: String) (implicit executionContext: ExecutionContext): SetACL = Await.result(findByTicketID(ticketID), Duration.Inf)
  }
}