package models.blockchain

import exceptions.BaseException
import javax.inject.Inject
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

case class ACLHash(issueAsset: Boolean, issueFiat: Boolean, sendAsset: Boolean, sendFiat: Boolean, redeemAsset: Boolean, redeemFiat: Boolean, sellerExecuteOrder: Boolean, buyerExecuteOrder: Boolean, changeBuyerBid: Boolean, changeSellerBid: Boolean, confirmBuyerBid: Boolean, confirmSellerBid: Boolean, negotiation: Boolean, releaseAssets: Boolean, hash: String)

class ACLHashs @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider) {

  private implicit val module: String = constants.Module.BLOCKCHAIN_ACLHASH

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private val logger: Logger = Logger(this.getClass)

  private[models] val aclTable = TableQuery[ACLHashTable]

  private def add(aclHash: ACLHash)(implicit executionContext: ExecutionContext): Future[String] = db.run((aclTable returning aclTable.map(_.hash) += aclHash).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        aclHash.hash
    }
  }

  private def findByAddress(hash: String): Future[ACLHash] = db.run(aclTable.filter(_.hash === hash).result.head)

  private def deleteByAddress(hash: String) = db.run(aclTable.filter(_.hash === hash).delete)

  private[models] class ACLHashTable(tag: Tag) extends Table[ACLHash](tag, "ACLHash_BC") {

    def * = (issueAsset, issueFiat, sendAsset, sendFiat, redeemAsset, redeemFiat, sellerExecuteOrder, buyerExecuteOrder, changeBuyerBid, changeSellerBid, confirmBuyerBid, confirmSellerBid, negotiation, releaseAsset, hash) <> (ACLHash.tupled, ACLHash.unapply)

    def issueAsset = column[Boolean]("issueAsset")

    def issueFiat = column[Boolean]("issueFiat")

    def sendAsset = column[Boolean]("sendAsset")

    def sendFiat = column[Boolean]("sendFiat")

    def redeemAsset = column[Boolean]("redeemAsset")

    def redeemFiat = column[Boolean]("redeemFiat")

    def sellerExecuteOrder = column[Boolean]("sellerExecuteOrder")

    def buyerExecuteOrder = column[Boolean]("buyerExecuteOrder")

    def changeBuyerBid = column[Boolean]("changeBuyerBid")

    def changeSellerBid = column[Boolean]("changeSellerBid")

    def confirmBuyerBid = column[Boolean]("confirmBuyerBid")

    def confirmSellerBid = column[Boolean]("confirmSellerBid")

    def negotiation = column[Boolean]("negotiation")

    def releaseAsset = column[Boolean]("releaseAsset")

    def hash = column[String]("hash", O.PrimaryKey)

  }

  object Service {
    def addACLHash(acl: ACL)(implicit executionContext: ExecutionContext): String = {
      Await.result(add(ACLHash(acl.issueAsset, acl.issueFiat, acl.sendAsset, acl.sendFiat, acl.redeemAsset, acl.redeemFiat, acl.sellerExecuteOrder, acl.buyerExecuteOrder, acl.changeBuyerBid, acl.changeSellerBid, acl.confirmBuyerBid, acl.confirmSellerBid, acl.negotiation, acl.releaseAsset, util.hashing.MurmurHash3.stringHash(acl.toString).toString)), Duration.Inf)
    }
  }
}