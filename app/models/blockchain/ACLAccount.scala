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

case class ACLAccount(address: String, zoneID: String, organizationID: String, aclHash: String)

case class ACL(issueAssets: Boolean, issueFiats: Boolean, sendAssets: Boolean, sendFiats: Boolean, redeemAssets: Boolean, redeemFiats: Boolean, sellerExecuteOrder: Boolean, buyerExecuteOrder: Boolean, changeBuyerBid: Boolean, changeSellerBid: Boolean, confirmBuyerBid: Boolean, confirmSellerBid: Boolean, negotiation: Boolean, releaseAssets: Boolean)

class ACLAccounts @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider, aclHashes: ACLHashes) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.BLOCKCHAIN_ACL_ACCOUNT

  import databaseConfig.profile.api._

  private[models] val aclTable = TableQuery[ACLTable]

  private def add(aclAccount: ACLAccount)(implicit executionContext: ExecutionContext): Future[String] = db.run((aclTable returning aclTable.map(_.address) += aclAccount).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
    }
  }

  private def findByAddress(address: String)(implicit executionContext: ExecutionContext): Future[ACLAccount] = db.run(aclTable.filter(_.address === address).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getAddressesByZoneID(zoneID: String)(implicit executionContext: ExecutionContext): Future[Seq[String]] = db.run(aclTable.filter(_.zoneID === zoneID).map(_.address).result.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def addOrUpdate(aclAccount: ACLAccount)(implicit executionContext: ExecutionContext): Future[Int] = db.run(aclTable.insertOrUpdate(aclAccount))

  private def deleteByAddress(address: String)(implicit executionContext: ExecutionContext) = db.run(aclTable.filter(_.address === address).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private[models] class ACLTable(tag: Tag) extends Table[ACLAccount](tag, "ACLAccount_BC") {

    def * = (address, zoneID, organizationID, aclHash) <> (ACLAccount.tupled, ACLAccount.unapply)

    def address = column[String]("address", O.PrimaryKey)

    def zoneID = column[String]("zoneID")

    def organizationID = column[String]("organizationID")

    def aclHash = column[String]("aclHash")
  }

  object Service {

    def addACLAccount(from: String, address: String, zoneID: String, organizationID: String, acl: ACL)(implicit executionContext: ExecutionContext): String = Await.result(add(ACLAccount(address, zoneID, organizationID, util.hashing.MurmurHash3.stringHash(acl.toString).toString)), Duration.Inf)

    def addOrUpdateACLAccount(from: String, address: String, zoneID: String, organizationID: String, acl: ACL)(implicit executionContext: ExecutionContext) = Await.result(addOrUpdate(ACLAccount(address, zoneID, organizationID, util.hashing.MurmurHash3.stringHash(acl.toString).toString)), Duration.Inf)

    def getACLAccount(address: String)(implicit executionContext: ExecutionContext): ACLAccount = Await.result(findByAddress(address), Duration.Inf)

    def getAddressesUnderZone(zoneID: String)(implicit executionContext: ExecutionContext): Seq[String] = Await.result(getAddressesByZoneID(zoneID), Duration.Inf)

  }
}