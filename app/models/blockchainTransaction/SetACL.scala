package models.blockchainTransaction

import akka.actor.ActorSystem
import exceptions.BaseException
import javax.inject.Inject
import models.blockchain.ACL
import models.master.Accounts
import org.postgresql.util.PSQLException
import play.api.{Configuration, Logger}
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.ws.WSClient
import slick.jdbc.JdbcProfile
import transactions.GetResponse
import utilities.PushNotifications

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._
import scala.util.{Failure, Success}

case class SetACL(from: String, aclAddress: String, organizationID: String, zoneID: String, aclHash: String,  status: Option[Boolean], txHash: Option[String], ticketID: String, responseCode: Option[String])

class SetACLs @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider, transactionSetACL: transactions.SetACL, getResponse: GetResponse, actorSystem: ActorSystem, implicit val pushNotifications: PushNotifications, implicit val accounts: Accounts)(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext)  {

  private implicit val module: String = constants.Module.BLOCKCHAIN_TRANSACTION_SET_ACL

  private implicit val logger: Logger = Logger(this.getClass)

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val setACLTable = TableQuery[SetACLTable]

  private def add(setACL: SetACL)(implicit executionContext: ExecutionContext): Future[String] = db.run((setACLTable returning setACLTable.map(_.ticketID) += setACL).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
    }
  }

  private def update(setACL: SetACL)(implicit executionContext: ExecutionContext): Future[Int] = db.run(setACLTable.insertOrUpdate(setACL).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
    }
  }

  private def findByTicketID(ticketID: String)(implicit executionContext: ExecutionContext): Future[SetACL] = db.run(setACLTable.filter(_.ticketID === ticketID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
    }
  }

  private def updateTxHashOnTicketID(ticketID: String, txHash: Option[String])(implicit executionContext: ExecutionContext) = db.run(setACLTable.filter(_.ticketID === ticketID).map(_.txHash.?).update(txHash))

  private def updateResponseCodeOnTicketID(ticketID: String, responseCode: String)(implicit executionContext: ExecutionContext) = db.run(setACLTable.filter(_.ticketID === ticketID).map(_.responseCode.?).update(Option(responseCode)))

  private def updateStatusOnTicketID(ticketID: String, status: Boolean)(implicit executionContext: ExecutionContext) = db.run(setACLTable.filter(_.ticketID === ticketID).map(_.status.?).update(Option(status)))

  private def getTicketIDsWithEmptyTxHash()(implicit executionContext: ExecutionContext):Future[Seq[String]] = db.run(setACLTable.filter(_.txHash.?.isEmpty).map(_.ticketID).result)

  private def getAddressByTicketID(ticketID: String)(implicit executionContext: ExecutionContext): Future[String] = db.run(setACLTable.filter(_.ticketID === ticketID).map(_.aclAddress).result.head)

  private def deleteByTicketID(ticketID: String)(implicit executionContext: ExecutionContext) = db.run(setACLTable.filter(_.ticketID === ticketID).delete)

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

  if (configuration.get[Boolean]("blockchain.kafka.enabled")) {
    actorSystem.scheduler.schedule(initialDelay = configuration.get[Int]("blockchain.kafka.ticketIterator.initialDelay").seconds, interval = configuration.get[Int]("blockchain.kafka.ticketIterator.interval").second) {
      utilities.TicketIterator.start(Service.getTicketIDs, transactionSetACL.Service.getTxHashFromWSResponse, Service.updateTxHash, Service.getAddress)
    }
  }

  object Service {

    def addSetACL(from: String, aclAddress: String, organizationID: String, zoneID: String, acl: ACL,  status: Option[Boolean], txHash: Option[String], ticketID: String, responseCode: Option[String])(implicit executionContext: ExecutionContext): String = Await.result(add(SetACL(from, aclAddress, organizationID, zoneID, util.hashing.MurmurHash3.stringHash(acl.toString).toString, status, txHash, ticketID, responseCode)), Duration.Inf)

    def addSetACLKafka(from: String, aclAddress: String, organizationID: String, zoneID: String, acl: ACL,  status: Option[Boolean], txHash: Option[String], ticketID: String, responseCode: Option[String])(implicit executionContext: ExecutionContext): String = Await.result(add(SetACL(from, aclAddress, organizationID, zoneID, util.hashing.MurmurHash3.stringHash(acl.toString).toString, status, txHash, ticketID, responseCode)), Duration.Inf)

    def updateTxHash(ticketID: String, txHash: String) (implicit executionContext: ExecutionContext): Int = Await.result(updateTxHashOnTicketID(ticketID, Option(txHash)),Duration.Inf)

    def updateResponseCode(ticketID: String, responseCode: String) (implicit executionContext: ExecutionContext): Int = Await.result(updateResponseCodeOnTicketID(ticketID, responseCode), Duration.Inf)

    def updateStatus(ticketID: String, status: Boolean) (implicit executionContext: ExecutionContext): Int = Await.result(updateStatusOnTicketID(ticketID, status), Duration.Inf)

    def getSetACL(ticketID: String) (implicit executionContext: ExecutionContext): SetACL = Await.result(findByTicketID(ticketID), Duration.Inf)

    def getTicketIDs()(implicit executionContext: ExecutionContext): Seq[String] = Await.result(getTicketIDsWithEmptyTxHash(), Duration.Inf)

    def getAddress(ticketID: String)(implicit executionContext: ExecutionContext): String = Await.result(getAddressByTicketID(ticketID), Duration.Inf)
  }
}