package models.blockchainTransaction

import akka.actor.ActorSystem
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Abstract.BaseTransaction
import models.blockchain.ACL
import models.{blockchain, master}
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import queries.responses.TraderReputationResponse
import slick.jdbc.JdbcProfile
import transactions.responses.TransactionResponse.BlockResponse

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class SetACL(from: String, aclAddress: String, organizationID: String, zoneID: String, aclHash: String, gas: Int, status: Option[Boolean] = None, txHash: Option[String] = None, ticketID: String, mode: String, code: Option[String] = None) extends BaseTransaction[SetACL] {
  def mutateTicketID(newTicketID: String): SetACL = SetACL(from = from, aclAddress = aclAddress, organizationID = organizationID, zoneID = zoneID, aclHash = aclHash, status = status, gas = gas, txHash = txHash, ticketID = newTicketID, mode = mode, code = code)
}

@Singleton
class SetACLs @Inject()(actorSystem: ActorSystem, transaction: utilities.Transaction, protected val databaseConfigProvider: DatabaseConfigProvider, blockchainTransactionFeedbacks: blockchain.TransactionFeedbacks, transactionSetACL: transactions.SetACL, blockchainAccounts: blockchain.Accounts, blockchainAclHashes: blockchain.ACLHashes, blockchainAclAccounts: blockchain.ACLAccounts, utilitiesNotification: utilities.Notification, masterAccounts: master.Accounts, masterTraders: master.Traders, masterTraderKYCs: master.TraderKYCs)(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.BLOCKCHAIN_TRANSACTION_SET_ACL

  private implicit val logger: Logger = Logger(this.getClass)
  val databaseConfig = databaseConfigProvider.get[JdbcProfile]
  val db = databaseConfig.db
  private val schedulerExecutionContext: ExecutionContext = actorSystem.dispatchers.lookup("akka.actors.scheduler-dispatcher")

  import databaseConfig.profile.api._
  private[models] val setACLTable = TableQuery[SetACLTable]

  private val schedulerInitialDelay = configuration.get[Int]("blockchain.kafka.transactionIterator.initialDelay").seconds
  private val schedulerInterval = configuration.get[Int]("blockchain.kafka.transactionIterator.interval").seconds
  private val kafkaEnabled = configuration.get[Boolean]("blockchain.kafka.enabled")
  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

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

  private def findTransactionHashByTicketID(ticketID: String): Future[Option[String]] = db.run(setACLTable.filter(_.ticketID === ticketID).map(_.txHash.?).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def findModeByTicketID(ticketID: String): Future[String] = db.run(setACLTable.filter(_.ticketID === ticketID).map(_.mode).result.head.asTry).map {
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

  private def deleteByTicketID(ticketID: String) = db.run(setACLTable.filter(_.ticketID === ticketID).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateTxHashOnTicketID(ticketID: String, txHash: Option[String]): Future[Int] = db.run(setACLTable.filter(_.ticketID === ticketID).map(x => x.txHash.?).update(txHash).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private[models] class SetACLTable(tag: Tag) extends Table[SetACL](tag, "SetACL") {

    def * = (from, aclAddress, organizationID, zoneID, aclHash, gas, status.?, txHash.?, ticketID, mode, code.?) <> (SetACL.tupled, SetACL.unapply)

    def from = column[String]("from")

    def aclAddress = column[String]("aclAddress")

    def organizationID = column[String]("organizationID")

    def zoneID = column[String]("zoneID")

    def aclHash = column[String]("aclHash")

    def gas = column[Int]("gas")

    def status = column[Boolean]("status")

    def txHash = column[String]("txHash")

    def ticketID = column[String]("ticketID", O.PrimaryKey)

    def mode = column[String]("mode")

    def code = column[String]("code")
  }

  object Service {

    def create(setACL: SetACL): Future[String] = add(SetACL(from = setACL.from, aclAddress = setACL.aclAddress, organizationID = setACL.organizationID, zoneID = setACL.zoneID, aclHash = setACL.aclHash, gas = setACL.gas, status = setACL.status, txHash = setACL.txHash, ticketID = setACL.ticketID, mode = setACL.mode, code = setACL.code))

    def markTransactionSuccessful(ticketID: String, txHash: String): Future[Int] = updateTxHashAndStatusOnTicketID(ticketID, Option(txHash), status = Option(true))

    def markTransactionFailed(ticketID: String, code: String): Future[Int] = updateStatusAndCodeOnTicketID(ticketID, status = Option(false), code)

    def getTransaction(ticketID: String): Future[SetACL] = findByTicketID(ticketID)

    def getTicketIDsOnStatus(): Future[Seq[String]] = getTicketIDsWithNullStatus

    def getTransactionHash(ticketID: String): Future[Option[String]] = findTransactionHashByTicketID(ticketID)

    def getMode(ticketID: String): Future[String] = findModeByTicketID(ticketID)

    def updateTransactionHash(ticketID: String, txHash: String): Future[Int] = updateTxHashOnTicketID(ticketID = ticketID, txHash = Option(txHash))

  }

  object Utility {
    def onSuccess(ticketID: String, blockResponse: BlockResponse): Future[Unit] = {
      val markTransactionSuccessful = Service.markTransactionSuccessful(ticketID, blockResponse.txhash)
      val setACL = Service.getTransaction(ticketID)

      def aclAccountID(aclAddress: String) = masterAccounts.Service.getId(aclAddress)

      def getAcl(aclHash: String) = blockchainAclHashes.Service.getACL(aclHash)

      def aclAccountInsertOrUpdate(setACL: SetACL, acl: ACL) = blockchainAclAccounts.Service.insertOrUpdate(setACL.aclAddress, setACL.zoneID, setACL.organizationID, acl, dirtyBit = true)

      def updateUserTypeOnAddress(setACL: SetACL) = masterAccounts.Service.updateUserTypeOnAddress(setACL.aclAddress, constants.User.TRADER)

      def verifyTraderByAccountID(aclAccountID: String) = masterTraders.Service.verifyTraderByAccountID(aclAccountID)

      def markDirty(fromAddress: String) = blockchainAccounts.Service.markDirty(fromAddress)

      def transactionFeedbacksInsertOrUpdate(setACL: SetACL) = blockchainTransactionFeedbacks.Service.insertOrUpdate(setACL.aclAddress, TraderReputationResponse.TransactionFeedbackResponse("0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0"), dirtyBit = true)

      def fromAccountID(fromAddress: String) = masterAccounts.Service.getId(fromAddress)

      (for {
        _ <- markTransactionSuccessful
        setACL <- setACL
        aclAccountID <- aclAccountID(setACL.aclAddress)
        acl <- getAcl(setACL.aclHash)
        _ <- aclAccountInsertOrUpdate(setACL, acl)
        _ <- updateUserTypeOnAddress(setACL)
        _ <- verifyTraderByAccountID(aclAccountID)
        _ <- markDirty(setACL.from)
        _ <- transactionFeedbacksInsertOrUpdate(setACL)
        fromAccountID <- fromAccountID(setACL.from)
      } yield {
        utilitiesNotification.send(aclAccountID, constants.Notification.SUCCESS, blockResponse.txhash)
        utilitiesNotification.send(fromAccountID, constants.Notification.SUCCESS, blockResponse.txhash)
      }).recover {
        case baseException: BaseException => logger.error(baseException.failure.message, baseException)
          throw new BaseException(constants.Response.PSQL_EXCEPTION)
      }
    }

    def onFailure(ticketID: String, message: String): Future[Unit] = {
      val markTransactionFailed = Service.markTransactionFailed(ticketID, message)
      val setACL = Service.getTransaction(ticketID)

      def getIDs(setACL: SetACL) = {
        val aclAddressID = masterAccounts.Service.getId(setACL.aclAddress)
        val fromID = masterAccounts.Service.getId(setACL.from)
        for {
          aclAddressID <- aclAddressID
          fromID <- fromID
        } yield (aclAddressID, fromID)
      }

      (for {
        _ <- markTransactionFailed
        setACL <- setACL
        (aclAddressID, fromID) <- getIDs(setACL)
      } yield {
        utilitiesNotification.send(aclAddressID, constants.Notification.FAILURE, message)
        utilitiesNotification.send(fromID, constants.Notification.FAILURE, message)
      }).recover {
        case baseException: BaseException => logger.error(baseException.failure.message, baseException)
      }
    }
  }

  if (kafkaEnabled || transactionMode != constants.Transactions.BLOCK_MODE) {
    actorSystem.scheduler.schedule(initialDelay = schedulerInitialDelay, interval = schedulerInterval) {
      transaction.ticketUpdater(Service.getTicketIDsOnStatus, Service.getTransactionHash, Service.getMode, Utility.onSuccess, Utility.onFailure)
    }(schedulerExecutionContext)
  }
}