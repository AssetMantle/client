package models.blockchain

import java.sql.Timestamp

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Trait.Logged
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class ACLHash(issueAsset: Boolean, issueFiat: Boolean, sendAsset: Boolean, sendFiat: Boolean, redeemAsset: Boolean, redeemFiat: Boolean, sellerExecuteOrder: Boolean, buyerExecuteOrder: Boolean, changeBuyerBid: Boolean, changeSellerBid: Boolean, confirmBuyerBid: Boolean, confirmSellerBid: Boolean, negotiation: Boolean, releaseAsset: Boolean, hash: String, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged

@Singleton
class ACLHashes @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.BLOCKCHAIN_ACL_HASH

  private[models] val aclTable = TableQuery[ACLHashTable]

  private def add(aclHash: ACLHash): Future[String] = db.run((aclTable returning aclTable.map(_.hash) += aclHash).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.info(constants.Response.PSQL_EXCEPTION.message, psqlException)
        aclHash.hash
    }
  }

  private def findByHash(hash: String): Future[ACLHash] = db.run(aclTable.filter(_.hash === hash).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def deleteByHash(hash: String): Future[Int] = db.run(aclTable.filter(_.hash === hash).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private[models] class ACLHashTable(tag: Tag) extends Table[ACLHash](tag, "ACLHash_BC") {

    def * = (issueAssets, issueFiats, sendAssets, sendFiats, redeemAssets, redeemFiats, sellerExecuteOrder, buyerExecuteOrder, changeBuyerBid, changeSellerBid, confirmBuyerBid, confirmSellerBid, negotiation, releaseAssets, hash, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (ACLHash.tupled, ACLHash.unapply)

    def issueAssets = column[Boolean]("issueAssets")

    def issueFiats = column[Boolean]("issueFiats")

    def sendAssets = column[Boolean]("sendAssets")

    def sendFiats = column[Boolean]("sendFiats")

    def redeemAssets = column[Boolean]("redeemAssets")

    def redeemFiats = column[Boolean]("redeemFiats")

    def sellerExecuteOrder = column[Boolean]("sellerExecuteOrder")

    def buyerExecuteOrder = column[Boolean]("buyerExecuteOrder")

    def changeBuyerBid = column[Boolean]("changeBuyerBid")

    def changeSellerBid = column[Boolean]("changeSellerBid")

    def confirmBuyerBid = column[Boolean]("confirmBuyerBid")

    def confirmSellerBid = column[Boolean]("confirmSellerBid")

    def negotiation = column[Boolean]("negotiation")

    def releaseAssets = column[Boolean]("releaseAssets")

    def hash = column[String]("hash", O.PrimaryKey)

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")

  }

  object Service {
    def create(acl: ACL): Future[String] = add(ACLHash(acl.issueAsset, acl.issueFiat, acl.sendAsset, acl.sendFiat, acl.redeemAsset, acl.redeemFiat, acl.sellerExecuteOrder, acl.buyerExecuteOrder, acl.changeBuyerBid, acl.changeSellerBid, acl.confirmBuyerBid, acl.confirmSellerBid, acl.negotiation, acl.releaseAsset, util.hashing.MurmurHash3.stringHash(acl.toString).toString))

    def get(hash: String): Future[ACLHash] = findByHash(hash)

    def tryGetACL(hash: String): Future[ACL] = findByHash(hash).map(aclHash => ACL(issueAsset = aclHash.issueAsset, issueFiat = aclHash.issueFiat, sendAsset = aclHash.sendAsset, sendFiat = aclHash.sendFiat, redeemAsset = aclHash.redeemAsset, redeemFiat = aclHash.redeemFiat, sellerExecuteOrder = aclHash.sellerExecuteOrder, buyerExecuteOrder = aclHash.buyerExecuteOrder, changeBuyerBid = aclHash.changeBuyerBid, changeSellerBid = aclHash.changeSellerBid, confirmBuyerBid = aclHash.confirmBuyerBid, confirmSellerBid = aclHash.confirmSellerBid, negotiation = aclHash.negotiation, releaseAsset = aclHash.releaseAsset))
  }

}