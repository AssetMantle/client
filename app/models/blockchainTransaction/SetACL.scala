package models.blockchainTransaction

import akka.actor.ActorSystem
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.{blockchain, master}
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import queries.responses.TraderReputationResponse
import slick.jdbc.JdbcProfile
import transactions.responses.TransactionResponse.BlockResponse
import utilities.{PushNotification, TransactionEntity}

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

case class SetACL(from: String, aclAddress: String, organizationID: String, zoneID: String, aclHash: String, status: Option[Boolean], txHash: Option[String], ticketID: String, mode: String, code: Option[String]) extends TransactionEntity[SetACL] {
  def mutateTicketID(newTicketID: String): SetACL = SetACL(from = from, aclAddress = aclAddress, organizationID = organizationID, zoneID = zoneID, aclHash = aclHash, status = status, txHash = txHash, ticketID = newTicketID, mode = mode, code = code)
}

@Singleton
class SetACLs @Inject()(actorSystem: ActorSystem, transaction: utilities.Transaction, protected val databaseConfigProvider: DatabaseConfigProvider, blockchainTransactionFeedbacks: blockchain.TransactionFeedbacks, transactionSetACL: transactions.SetACL, blockchainAccounts: blockchain.Accounts, blockchainAclHashes: blockchain.ACLHashes, blockchainAclAccounts: blockchain.ACLAccounts, pushNotification: PushNotification, masterAccounts: master.Accounts, masterTraders: master.Traders, masterTraderKYCs: master.TraderKYCs)(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.BLOCKCHAIN_TRANSACTION_SET_ACL

  private implicit val logger: Logger = Logger(this.getClass)

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val setACLTable = TableQuery[SetACLTable]

  private val schedulerInitialDelay = configuration.get[Int]("blockchain.kafka.transactionIterator.initialDelay").seconds
  private val schedulerInterval = configuration.get[Int]("blockchain.kafka.transactionIterator.interval").seconds
  private val kafkaEnabled = configuration.get[Boolean]("blockchain.kafka.enabled")

  private def add(setACL: SetACL): Future[String] = db.run((setACLTable returning setACLTable.map(_.ticketID) += setACL).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def upsert(setACL: SetACL): Future[Int] = db.run(setACLTable.insertOrUpdate(setACL).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def findByTicketID(ticketID: String): Future[SetACL] = db.run(setACLTable.filter(_.ticketID === ticketID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateTxHashAndStatusOnTicketID(ticketID: String, txHash: Option[String], status: Option[Boolean]): Future[Int] = db.run(setACLTable.filter(_.ticketID === ticketID).map(x => (x.txHash.?, x.status.?)).update(txHash, status).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateStatusAndCodeOnTicketID(ticketID: String, status: Option[Boolean], code: String): Future[Int] = db.run(setACLTable.filter(_.ticketID === ticketID).map(x => (x.status.?, x.code)).update((status, code)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getTicketIDsWithNullStatus: Future[Seq[String]] = db.run(setACLTable.filter(_.status.?.isEmpty).map(_.ticketID).result)

  private def deleteByTicketID(ticketID: String)(implicit executionContext: ExecutionContext) = db.run(setACLTable.filter(_.ticketID === ticketID).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private[models] class SetACLTable(tag: Tag) extends Table[SetACL](tag, "SetACL") {

    def * = (from, aclAddress, organizationID, zoneID, aclHash, status.?, txHash.?, ticketID, mode, code.?) <> (SetACL.tupled, SetACL.unapply)

    def from = column[String]("from")

    def aclAddress = column[String]("aclAddress")

    def organizationID = column[String]("organizationID")

    def zoneID = column[String]("zoneID")

    def aclHash = column[String]("aclHash")

    def status = column[Boolean]("status")

    def txHash = column[String]("txHash")

    def ticketID = column[String]("ticketID", O.PrimaryKey)

    def mode = column[String]("mode")

    def code = column[String]("code")
  }

  object Service {

    def create(setACL: SetACL): String = Await.result(add(SetACL(from = setACL.from, aclAddress = setACL.aclAddress, organizationID = setACL.organizationID, zoneID = setACL.zoneID, aclHash = setACL.aclHash, status = setACL.status, txHash = setACL.txHash, ticketID = setACL.ticketID, mode = setACL.mode, code = setACL.code)), Duration.Inf)

    def markTransactionSuccessful(ticketID: String, txHash: String): Int = Await.result(updateTxHashAndStatusOnTicketID(ticketID, Option(txHash), status = Option(true)), Duration.Inf)

    def markTransactionFailed(ticketID: String, code: String): Int = Await.result(updateStatusAndCodeOnTicketID(ticketID, status = Option(false), code), Duration.Inf)

    def getTransaction(ticketID: String): SetACL = Await.result(findByTicketID(ticketID), Duration.Inf)

    def getTicketIDsOnStatus(): Seq[String] = Await.result(getTicketIDsWithNullStatus, Duration.Inf)

    def getTransactionHash(ticketID: String): Option[String] = Await.result(findByTicketID(ticketID), Duration.Inf).txHash
  }

  object Utility {
    def onSuccess(ticketID: String, blockResponse: BlockResponse): Future[Unit] = Future {
      try {
        Service.markTransactionSuccessful(ticketID, blockResponse.txhash)
        val setACL = Service.getTransaction(ticketID)
        val aclAccountID = masterAccounts.Service.getId(setACL.aclAddress)
        blockchainAclAccounts.Service.insertOrUpdate(setACL.aclAddress, setACL.zoneID, setACL.organizationID, blockchainAclHashes.Service.getACL(setACL.aclHash), dirtyBit = true)
        masterAccounts.Service.updateUserTypeOnAddress(setACL.aclAddress, constants.User.TRADER)
        masterTraders.Service.updateStatusByAccountID(aclAccountID, status = true)
        masterTraderKYCs.Service.organizationVerifyAll(setACL.organizationID)
        masterTraderKYCs.Service.zoneVerifyAll(setACL.zoneID)
        blockchainAccounts.Service.markDirty(masterAccounts.Service.getAddress(setACL.from))
        blockchainTransactionFeedbacks.Service.insertOrUpdate(setACL.aclAddress, TraderReputationResponse.TransactionFeedbackResponse("0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0"), dirtyBit = true)
        pushNotification.sendNotification(aclAccountID, constants.Notification.SUCCESS, blockResponse.txhash)
        pushNotification.sendNotification(setACL.from, constants.Notification.SUCCESS, blockResponse.txhash)
      } catch {
        case baseException: BaseException => logger.error(baseException.failure.message, baseException)
          throw new BaseException(constants.Response.PSQL_EXCEPTION)
      }
    }

    def onFailure(ticketID: String, message: String): Future[Unit] = Future {
      try {
        Service.markTransactionFailed(ticketID, message)
        val setACL = Service.getTransaction(ticketID)
        pushNotification.sendNotification(masterAccounts.Service.getId(setACL.aclAddress), constants.Notification.FAILURE, message)
        pushNotification.sendNotification(setACL.from, constants.Notification.FAILURE, message)
      } catch {
        case baseException: BaseException => logger.error(baseException.failure.message, baseException)
      }
    }
  }


  if (kafkaEnabled) {
    actorSystem.scheduler.schedule(initialDelay = schedulerInitialDelay, interval = schedulerInterval) {
      transaction.ticketUpdater(Service.getTicketIDsOnStatus, Service.getTransactionHash, Utility.onSuccess, Utility.onFailure)
    }
  }
}