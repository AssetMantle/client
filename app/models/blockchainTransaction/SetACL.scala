package models.blockchainTransaction

import akka.actor.ActorSystem
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Abstract.BaseTransaction
import models.blockchain.ACL
import models.master.{Organization, Trader}
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
class SetACLs @Inject()(
                         actorSystem: ActorSystem,
                         transaction: utilities.Transaction,
                         protected val databaseConfigProvider: DatabaseConfigProvider,
                         blockchainTransactionFeedbacks: blockchain.TransactionFeedbacks,
                         transactionSetACL: transactions.SetACL,
                         blockchainAccounts: blockchain.Accounts,
                         blockchainAclHashes: blockchain.ACLHashes,
                         blockchainAclAccounts: blockchain.ACLAccounts,
                         utilitiesNotification: utilities.Notification,
                         masterAccounts: master.Accounts,
                         masterTraders: master.Traders,
                         masterOrganizations: master.Organizations,
                         masterTraderKYCs: master.TraderKYCs
                       )(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.BLOCKCHAIN_TRANSACTION_SET_ACL

  private implicit val logger: Logger = Logger(this.getClass)
  val databaseConfig = databaseConfigProvider.get[JdbcProfile]
  val db = databaseConfig.db
  private val schedulerExecutionContext: ExecutionContext = actorSystem.dispatchers.lookup("akka.actor.scheduler-dispatcher")

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

  private def updateStatusByTicketID(ticketID: String, status: Option[Boolean]): Future[Int] = db.run(setACLTable.filter(_.ticketID === ticketID).map(_.status.?).update(status).asTry).map {
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

    def resetTransactionStatus(ticketID: String): Future[Int] = updateStatusByTicketID(ticketID, status = null)

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

      def getAccountID(address: String): Future[String] = masterAccounts.Service.getId(address)

      def getTrader(accountID: String): Future[Trader] = masterTraders.Service.tryGetByAccountID(accountID)

      def getAcl(aclHash: String): Future[ACL] = blockchainAclHashes.Service.getACL(aclHash)

      def aclAccountInsertOrUpdate(setACL: SetACL, acl: ACL): Future[Int] = blockchainAclAccounts.Service.insertOrUpdate(setACL.aclAddress, setACL.zoneID, setACL.organizationID, acl, dirtyBit = true)

      def markUserTypeTrader(accountID: String): Future[Int] = masterAccounts.Service.markUserTypeTrader(accountID)

      def markAccepted(traderID: String): Future[Int] = masterTraders.Service.markAccepted(traderID)

      def markDirty(fromAddress: String): Future[Int] = blockchainAccounts.Service.markDirty(fromAddress)

      def transactionFeedbacksInsertOrUpdate(setACL: SetACL): Future[Int] = blockchainTransactionFeedbacks.Service.insertOrUpdate(setACL.aclAddress, TraderReputationResponse.TransactionFeedbackResponse("0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0"), dirtyBit = true)

      def getTraderOrganization(organizationID: String): Future[Organization] = masterOrganizations.Service.tryGet(organizationID)

      (for {
        _ <- markTransactionSuccessful
        setACL <- setACL
        traderAccountID <- getAccountID(setACL.aclAddress)
        trader <- getTrader(traderAccountID)
        acl <- getAcl(setACL.aclHash)
        _ <- aclAccountInsertOrUpdate(setACL, acl)
        _ <- markUserTypeTrader(traderAccountID)
        _ <- markAccepted(trader.id)
        _ <- markDirty(setACL.from)
        _ <- transactionFeedbacksInsertOrUpdate(setACL)
        fromAccountID <- getAccountID(setACL.from)
        organization <- getTraderOrganization(trader.organizationID)
        _ <- utilitiesNotification.send(traderAccountID, constants.Notification.TRADER_REGISTRATION_SUCCESSFUL, blockResponse.txhash)
        _ <- utilitiesNotification.send(organization.accountID, constants.Notification.ORGANIZATION_TRADER_REGISTRATION_SUCCESSFUL, blockResponse.txhash, trader.name)
        _ <- utilitiesNotification.send(fromAccountID, constants.Notification.BLOCKCHAIN_TRANSACTION_ADD_TRADER_SUCCESSFUL, blockResponse.txhash, trader.accountID, trader.name, organization.name)
      } yield ()).recover {
        case baseException: BaseException => logger.error(baseException.failure.message, baseException)
          if (baseException.failure == constants.Response.CONNECT_EXCEPTION) {
            (for {
              _ <- Service.resetTransactionStatus(ticketID)
            } yield ()
              ).recover {
              case baseException: BaseException => logger.error(baseException.failure.message, baseException)
                throw baseException
            }
          }
          throw baseException
      }
    }

    def onFailure(ticketID: String, message: String): Future[Unit] = {
      val markTransactionFailed = Service.markTransactionFailed(ticketID, message)
      val setACL = Service.getTransaction(ticketID)

      def getAccountID(address: String): Future[String] = masterAccounts.Service.getId(address)

      def getTrader(accountID: String): Future[Trader] = masterTraders.Service.tryGetByAccountID(accountID)

      def getTraderOrganization(organizationID: String): Future[Organization] = masterOrganizations.Service.tryGet(organizationID)

      (for {
        _ <- markTransactionFailed
        setACL <- setACL
        traderAccountID <- getAccountID(setACL.aclAddress)
        zoneAccountID <- getAccountID(setACL.from)
        trader <- getTrader(traderAccountID)
        organization <- getTraderOrganization(trader.organizationID)
        _ <- utilitiesNotification.send(traderAccountID, constants.Notification.TRADER_REGISTRATION_FAILED, message)
        _ <- utilitiesNotification.send(organization.accountID, constants.Notification.ORGANIZATION_TRADER_REGISTRATION_FAILED, message, trader.name)
        _ <- utilitiesNotification.send(zoneAccountID, constants.Notification.BLOCKCHAIN_TRANSACTION_ADD_TRADER_FAILED, message, trader.accountID, trader.name, organization.name)
      } yield ()).recover {
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