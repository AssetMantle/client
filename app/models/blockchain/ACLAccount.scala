package models.blockchain

import akka.actor.ActorSystem
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.{Configuration, Logger}
import queries.GetACL
import queries.responses.ACLResponse.Response
import slick.jdbc.JdbcProfile

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class ACLAccount(address: String, zoneID: String, organizationID: String, aclHash: String, dirtyBit: Boolean)

case class ACL(issueAsset: Boolean, issueFiat: Boolean, sendAsset: Boolean, sendFiat: Boolean, redeemAsset: Boolean, redeemFiat: Boolean, sellerExecuteOrder: Boolean, buyerExecuteOrder: Boolean, changeBuyerBid: Boolean, changeSellerBid: Boolean, confirmBuyerBid: Boolean, confirmSellerBid: Boolean, negotiation: Boolean, releaseAsset: Boolean)

@Singleton
class ACLAccounts @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider, actorSystem: ActorSystem, aclHashes: ACLHashes, getACL: GetACL)(implicit executionContext: ExecutionContext, configuration: Configuration) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private val schedulerExecutionContext: ExecutionContext = actorSystem.dispatchers.lookup("akka.actor.scheduler-dispatcher")

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.BLOCKCHAIN_ACL_ACCOUNT

  import databaseConfig.profile.api._

  private[models] val aclTable = TableQuery[ACLTable]

  private val schedulerInitialDelay = configuration.get[Int]("blockchain.kafka.transactionIterator.initialDelay").seconds
  private val schedulerInterval = configuration.get[Int]("blockchain.kafka.transactionIterator.interval").seconds
  private val sleepTime = configuration.get[Long]("blockchain.entityIterator.threadSleep")

  private def getAddressesByDirtyBit(dirtyBit: Boolean): Future[Seq[String]] = db.run(aclTable.filter(_.dirtyBit === dirtyBit).map(_.address).result)

  private def upsert(aclAccount: ACLAccount): Future[Int] = db.run(aclTable.insertOrUpdate(aclAccount).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def add(aclAccount: ACLAccount): Future[String] = db.run((aclTable returning aclTable.map(_.address) += aclAccount).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def tryGetByAddress(address: String): Future[ACLAccount] = db.run(aclTable.filter(_.address === address).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def tryGetACLHashByAddress(address: String): Future[String] = db.run(aclTable.filter(_.address === address).map(_.aclHash).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getAddressesByZoneID(zoneID: String): Future[Seq[String]] = db.run(aclTable.filter(_.zoneID === zoneID).map(_.address).result)

  private def deleteByAddress(address: String) = db.run(aclTable.filter(_.address === address).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateDirtyBitByAddress(address: String, dirtyBit: Boolean): Future[Int] = db.run(aclTable.filter(_.address === address).map(_.dirtyBit).update(dirtyBit).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private[models] class ACLTable(tag: Tag) extends Table[ACLAccount](tag, "ACLAccount_BC") {

    def * = (address, zoneID, organizationID, aclHash, dirtyBit) <> (ACLAccount.tupled, ACLAccount.unapply)

    def address = column[String]("address", O.PrimaryKey)

    def zoneID = column[String]("zoneID")

    def organizationID = column[String]("organizationID")

    def aclHash = column[String]("aclHash")

    def dirtyBit = column[Boolean]("dirtyBit")
  }

  object Service {

    def create(address: String, zoneID: String, organizationID: String, acl: ACL, dirtyBit: Boolean): Future[String] = add(ACLAccount(address, zoneID, organizationID, util.hashing.MurmurHash3.stringHash(acl.toString).toString, dirtyBit))

    def insertOrUpdate(address: String, zoneID: String, organizationID: String, acl: ACL, dirtyBit: Boolean): Future[Int] = upsert(ACLAccount(address, zoneID, organizationID, util.hashing.MurmurHash3.stringHash(acl.toString).toString, dirtyBit))

    def tryGet(address: String): Future[ACLAccount] = tryGetByAddress(address)

    def tryGetACLHash(address: String): Future[String] = tryGetACLHashByAddress(address)

    def getAddressesUnderZone(zoneID: String): Future[Seq[String]] = getAddressesByZoneID(zoneID)

    def getDirtyAddresses: Future[Seq[String]] = getAddressesByDirtyBit(dirtyBit = true)

    def markDirty(address: String): Future[Int] = updateDirtyBitByAddress(address, dirtyBit = true)
  }

  object Utility {
    def dirtyEntityUpdater(): Future[Unit] = {
      val dirtyAddresses = Service.getDirtyAddresses
      Thread.sleep(sleepTime)

      def insertOrUpdateAll(dirtyAddresses: Seq[String]): Future[Seq[Unit]] = {
        Future.sequence {
          dirtyAddresses.map { dirtyAddress =>
            val responseAccount = getACL.Service.get(dirtyAddress)

            def insertOrUpdate(responseAccount: Response): Future[Int] = Service.insertOrUpdate(responseAccount.value.address, responseAccount.value.zoneID, responseAccount.value.organizationID, responseAccount.value.acl, dirtyBit = false)

            for {
              responseAccount <- responseAccount
              _ <- insertOrUpdate(responseAccount)
            } yield {}
          }
        }
      }

      (for {
        dirtyAddresses <- dirtyAddresses
        _ <- insertOrUpdateAll(dirtyAddresses)
      } yield ()).recover {
        case baseException: BaseException => logger.error(baseException.failure.message, baseException)
      }
    }
  }

  actorSystem.scheduler.schedule(initialDelay = schedulerInitialDelay, interval = schedulerInterval) {
    Utility.dirtyEntityUpdater()
  }(schedulerExecutionContext)
}