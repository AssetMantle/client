package models.blockchain

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

case class ACLHash(issueAsset: Boolean, issueFiat: Boolean, sendAsset: Boolean, sendFiat: Boolean, redeemAsset: Boolean, redeemFiat: Boolean, sellerExecuteOrder: Boolean, buyerExecuteOrder: Boolean, changeBuyerBid: Boolean, changeSellerBid: Boolean, confirmBuyerBid: Boolean, confirmSellerBid: Boolean, negotiation: Boolean, releaseAsset: Boolean, hash: String)

@Singleton
class ACLHashes @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.BLOCKCHAIN_ACL_HASH

  private[models] val aclTable = TableQuery[ACLHashTable]

  private def add(aclHash: ACLHash)(implicit executionContext: ExecutionContext): Future[String] = db.run((aclTable returning aclTable.map(_.hash) += aclHash).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.info(constants.Response.PSQL_EXCEPTION, psqlException)
        aclHash.hash
    }
  }

  private def findByHash(hash: String)(implicit executionContext: ExecutionContext): Future[ACLHash] = db.run(aclTable.filter(_.hash === hash).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def deleteByHash(hash: String)(implicit executionContext: ExecutionContext) = db.run(aclTable.filter(_.hash === hash).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private[models] class ACLHashTable(tag: Tag) extends Table[ACLHash](tag, "ACLHash_BC") {

    def * = (issueAssets, issueFiats, sendAssets, sendFiats, redeemAssets, redeemFiats, sellerExecuteOrder, buyerExecuteOrder, changeBuyerBid, changeSellerBid, confirmBuyerBid, confirmSellerBid, negotiation, releaseAssets, hash) <> (ACLHash.tupled, ACLHash.unapply)

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

  }

  object Service {
    def addACLHash(acl: ACL)(implicit executionContext: ExecutionContext): String = Await.result(add(ACLHash(acl.issueAsset, acl.issueFiat, acl.sendAsset, acl.sendFiat, acl.redeemAsset, acl.redeemFiat, acl.sellerExecuteOrder, acl.buyerExecuteOrder, acl.changeBuyerBid, acl.changeSellerBid, acl.confirmBuyerBid, acl.confirmSellerBid, acl.negotiation, acl.releaseAsset, util.hashing.MurmurHash3.stringHash(acl.toString).toString)), Duration.Inf)

    def getACLHash(hash: String)(implicit executionContext: ExecutionContext): ACLHash = Await.result(findByHash(hash), Duration.Inf)

    def getACL(hash: String): ACL = {
      val aclHash = Await.result(findByHash(hash), Duration.Inf)
      ACL(aclHash.issueAsset, aclHash.issueFiat, aclHash.sendAsset, aclHash.sendFiat, aclHash.redeemAsset, aclHash.redeemFiat, aclHash.sellerExecuteOrder, aclHash.buyerExecuteOrder, aclHash.changeBuyerBid, aclHash.changeSellerBid, aclHash.confirmBuyerBid, aclHash.confirmSellerBid, aclHash.negotiation, aclHash.releaseAsset)
    }
  }
}