package models.blockchainTransaction

import akka.actor.ActorSystem
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.blockchain.ACL
import models.{blockchain, master}
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import queries.responses.TraderReputationResponse
import slick.jdbc.JdbcProfile
import transactions.GetResponse
import transactions.responses.TransactionResponse.Response
import utilities.PushNotification

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

case class SetACL(from: String, aclAddress: String, organizationID: String, zoneID: String, aclHash: String, status: Option[Boolean], txHash: Option[String], ticketID: String, responseCode: Option[String])

@Singleton
class SetACLs @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider, blockchainTransactionFeedbacks: blockchain.TransactionFeedbacks, transactionSetACL: transactions.SetACL, getResponse: GetResponse, actorSystem: ActorSystem, blockchainAccounts: blockchain.Accounts, blockchainAclHashes: blockchain.ACLHashes, blockchainAclAccounts: blockchain.ACLAccounts, pushNotification: PushNotification, masterAccounts: master.Accounts)(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.BLOCKCHAIN_TRANSACTION_SET_ACL

  private implicit val logger: Logger = Logger(this.getClass)

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val setACLTable = TableQuery[SetACLTable]

  private val schedulerInitialDelay = configuration.get[Int]("blockchain.kafka.transactionIterator.initialDelay").seconds
  private val schedulerInterval = configuration.get[Int]("blockchain.kafka.transactionIterator.interval").seconds
  private val kafkaEnabled = configuration.get[Boolean]("blockchain.kafka.enabled")

  private def add(setACL: SetACL)(implicit executionContext: ExecutionContext): Future[String] = db.run((setACLTable returning setACLTable.map(_.ticketID) += setACL).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
    }
  }

  private def upsert(setACL: SetACL)(implicit executionContext: ExecutionContext): Future[Int] = db.run(setACLTable.insertOrUpdate(setACL).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def findByTicketID(ticketID: String)(implicit executionContext: ExecutionContext): Future[SetACL] = db.run(setACLTable.filter(_.ticketID === ticketID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateTxHashStatusAndResponseCodeOnTicketID(ticketID: String, txHash: String, status: Boolean, responseCode: String): Future[Int] = db.run(setACLTable.filter(_.ticketID === ticketID).map(x => (x.txHash, x.status, x.responseCode)).update(txHash, status, responseCode).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateStatusAndResponseOnTicketID(ticketID: String, status: Boolean, responseCode: String): Future[Int] = db.run(setACLTable.filter(_.ticketID === ticketID).map(x => (x.status, x.responseCode)).update((status, responseCode)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getTicketIDsWithNullStatus()(implicit executionContext: ExecutionContext): Future[Seq[String]] = db.run(setACLTable.filter(_.status.?.isEmpty).map(_.ticketID).result)

  private def deleteByTicketID(ticketID: String)(implicit executionContext: ExecutionContext) = db.run(setACLTable.filter(_.ticketID === ticketID).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private[models] class SetACLTable(tag: Tag) extends Table[SetACL](tag, "SetACL") {

    def * = (from, aclAddress, organizationID, zoneID, aclHash, status.?, txHash.?, ticketID, responseCode.?) <> (SetACL.tupled, SetACL.unapply)

    def from = column[String]("from")

    def aclAddress = column[String]("aclAddress")

    def organizationID = column[String]("organizationID")

    def zoneID = column[String]("zoneID")

    def aclHash = column[String]("aclHash")

    def status = column[Boolean]("status")

    def txHash = column[String]("txHash")

    def ticketID = column[String]("ticketID", O.PrimaryKey)

    def responseCode = column[String]("responseCode")
  }

  object Service {

    def addSetACL(from: String, aclAddress: String, organizationID: String, zoneID: String, acl: ACL, status: Option[Boolean], txHash: Option[String], ticketID: String, responseCode: Option[String])(implicit executionContext: ExecutionContext): String = Await.result(add(SetACL(from, aclAddress, organizationID, zoneID, util.hashing.MurmurHash3.stringHash(acl.toString).toString, status, txHash, ticketID, responseCode)), Duration.Inf)

    def markTransactionSuccessful(ticketID: String, txHash: String, responseCode: String): Int = Await.result(updateTxHashStatusAndResponseCodeOnTicketID(ticketID, txHash, status = true, responseCode), Duration.Inf)

    def markTransactionFailed(ticketID: String, responseCode: String): Int = Await.result(updateStatusAndResponseOnTicketID(ticketID, status = false, responseCode), Duration.Inf)

    def getSetACL(ticketID: String)(implicit executionContext: ExecutionContext): SetACL = Await.result(findByTicketID(ticketID), Duration.Inf)

    def getTicketIDsOnStatus(): Seq[String] = Await.result(getTicketIDsWithNullStatus(), Duration.Inf)
  }

  object Utility {
    def onSuccess(ticketID: String, response: Response): Future[Unit] = Future {
      try {
        Service.markTransactionSuccessful(ticketID, response.TxHash, response.Code)
        val setACL = Service.getSetACL(ticketID)
        blockchainAclAccounts.Service.insertOrUpdate(setACL.aclAddress, setACL.zoneID, setACL.organizationID, blockchainAclHashes.Service.getACL(setACL.aclHash), dirtyBit = true)
        masterAccounts.Service.updateUserTypeOnAddress(setACL.aclAddress, constants.User.TRADER)
        blockchainAccounts.Service.markDirty(masterAccounts.Service.getAddress(setACL.from))
        blockchainTransactionFeedbacks.Service.insertOrUpdate(setACL.aclAddress, TraderReputationResponse.TransactionFeedbackResponse("0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0"), dirtyBit = true)
        pushNotification.sendNotification(masterAccounts.Service.getId(setACL.aclAddress), constants.Notification.SUCCESS, response.TxHash)
        pushNotification.sendNotification(setACL.from, constants.Notification.SUCCESS, response.TxHash)
      } catch {
        case baseException: BaseException => logger.error(constants.Error.BASE_EXCEPTION, baseException)
          throw new BaseException(constants.Error.PSQL_EXCEPTION)
      }
    }

    def onFailure(ticketID: String, message: String): Future[Unit] = Future {
      try {
        Service.markTransactionFailed(ticketID, message)
        val setACL = Service.getSetACL(ticketID)
        pushNotification.sendNotification(masterAccounts.Service.getId(setACL.aclAddress), constants.Notification.FAILURE, message)
        pushNotification.sendNotification(setACL.from, constants.Notification.FAILURE, message)
      } catch {
        case baseException: BaseException => logger.error(constants.Error.BASE_EXCEPTION, baseException)
      }
    }
  }


  if (kafkaEnabled) {
    actorSystem.scheduler.schedule(initialDelay = schedulerInitialDelay, interval = schedulerInterval) {
      utilities.TicketUpdater.start(Service.getTicketIDsOnStatus, transactionSetACL.Service.getTxFromWSResponse, Utility.onSuccess, Utility.onFailure)
    }
  }
}