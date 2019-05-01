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

case class ACLHash(issueAssets: Boolean, issueFiats: Boolean, sendAssets: Boolean, sendFiats: Boolean, redeemAssets: Boolean, redeemFiats: Boolean, sellerExecuteOrder: Boolean, buyerExecuteOrder: Boolean, changeBuyerBid: Boolean, changeSellerBid: Boolean, confirmBuyerBid: Boolean, confirmSellerBid: Boolean, negotiation: Boolean, releaseAssets: Boolean, hash: String)

@Singleton
class ACLHashes @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.BLOCKCHAIN_ACL_HASH

  private[models] val aclTable = TableQuery[ACLHashTable]

  private def add(aclHash: ACLHash)(implicit executionContext: ExecutionContext): Future[String] = db.run((aclTable returning aclTable.map(_.hash) += aclHash).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.info(constants.Error.PSQL_EXCEPTION, psqlException)
        aclHash.hash
    }
  }

  private def findByHash(hash: String)(implicit executionContext: ExecutionContext): Future[ACLHash] = db.run(aclTable.filter(_.hash === hash).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def deleteByHash(hash: String)(implicit executionContext: ExecutionContext) = db.run(aclTable.filter(_.hash === hash).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
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
    def addACLHash(acl: ACL)(implicit executionContext: ExecutionContext): String = Await.result(add(ACLHash(acl.issueAssets, acl.issueFiats, acl.sendAssets, acl.sendFiats, acl.redeemAssets, acl.redeemFiats, acl.sellerExecuteOrder, acl.buyerExecuteOrder, acl.changeBuyerBid, acl.changeSellerBid, acl.confirmBuyerBid, acl.confirmSellerBid, acl.negotiation, acl.releaseAssets, util.hashing.MurmurHash3.stringHash(acl.toString).toString)), Duration.Inf)

    def getACLHash(hash: String)(implicit executionContext: ExecutionContext): ACLHash = Await.result(findByHash(hash), Duration.Inf)

  }
}