package models.masterTransaction

import java.sql.Timestamp

import javax.inject.{Inject, Singleton}
import models.Trait.HistoryLogged
import play.api.db.slick.DatabaseConfigProvider
import play.api.{Configuration, Logger}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

case class SendFiatRequestHistory(id: String, traderID: String, ticketID: String, negotiationID: String, amount: Int, status: String, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None, deletedBy: String, deletedOn: Timestamp, deletedOnTimeZone: String) extends HistoryLogged

@Singleton
class SendFiatRequestHistories @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider, configuration: Configuration)(implicit executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.MASTER_TRANSACTION_SEND_FIAT_REQUEST_HISTORY

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  import databaseConfig.profile.api._

  private[models] val sendFiatRequestHistoryTable = TableQuery[SendFiatRequestHistoryTable]

  private def getByTraderIDsAndStatus(traderIDs: Seq[String], status: String): Future[Seq[SendFiatRequestHistory]] = db.run(sendFiatRequestHistoryTable.filter(_.traderID inSet traderIDs).filter(_.status === status).result)

  private def getByTraderIDAndStatus(traderID: String, status: String): Future[Seq[SendFiatRequestHistory]] = db.run(sendFiatRequestHistoryTable.filter(_.traderID === traderID).filter(_.status === status).result)

  private def getAmountByNegotiationIDAndStatuses(negotiationID: String, statuses: Seq[String]): Future[Int] = db.run(sendFiatRequestHistoryTable.filter(_.negotiationID === negotiationID).filter(_.status inSet statuses).map(_.amount).sum.getOrElse(0).result)

  private def getByNegotiationIDsAndStatuses(negotiationIDs: Seq[String], statuses: Seq[String]): Future[Seq[SendFiatRequestHistory]] = db.run(sendFiatRequestHistoryTable.filter(_.negotiationID inSet negotiationIDs).filter(_.status inSet statuses).result)

  private[models] class SendFiatRequestHistoryTable(tag: Tag) extends Table[SendFiatRequestHistory](tag, "SendFiatRequest_History") {

    def * = (id, traderID, ticketID, negotiationID, amount, status, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?, deletedBy, deletedOn, deletedOnTimeZone) <> (SendFiatRequestHistory.tupled, SendFiatRequestHistory.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def traderID = column[String]("traderID")

    def ticketID = column[String]("ticketID")

    def negotiationID = column[String]("negotiationID")

    def amount = column[Int]("amount")

    def status = column[String]("status")

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")

    def deletedBy = column[String]("deletedBy")

    def deletedOn = column[Timestamp]("deletedOn")

    def deletedOnTimeZone = column[String]("deletedOnTimeZone")
  }

  object Service {
    def getFiatsInOrder(negotiationID: String): Future[Int] = getAmountByNegotiationIDAndStatuses(negotiationID, Seq(constants.Status.SendFiat.BLOCKCHAIN_SUCCESS, constants.Status.SendFiat.SENT))

    def getFiatRequestsInOrders(negotiationIDs: Seq[String]): Future[Seq[SendFiatRequestHistory]] = getByNegotiationIDsAndStatuses(negotiationIDs, Seq(constants.Status.SendFiat.BLOCKCHAIN_SUCCESS, constants.Status.SendFiat.SENT))

    def getPendingSendFiatRequests(traderIDs: Seq[String]): Future[Seq[SendFiatRequestHistory]] = getByTraderIDsAndStatus(traderIDs, constants.Status.SendFiat.BLOCKCHAIN_SUCCESS)

    def getCompleteSendFiatRequests(traderIDs: Seq[String]): Future[Seq[SendFiatRequestHistory]] = getByTraderIDsAndStatus(traderIDs, constants.Status.SendFiat.SENT)

    def getFailedSendFiatRequests(traderIDs: Seq[String]): Future[Seq[SendFiatRequestHistory]] = getByTraderIDsAndStatus(traderIDs, constants.Status.SendFiat.BLOCKCHAIN_FAILURE)

    def getPendingSendFiatRequests(traderID: String): Future[Seq[SendFiatRequestHistory]] = getByTraderIDAndStatus(traderID, constants.Status.SendFiat.BLOCKCHAIN_SUCCESS)

    def getCompleteSendFiatRequests(traderID: String): Future[Seq[SendFiatRequestHistory]] = getByTraderIDAndStatus(traderID, constants.Status.SendFiat.SENT)

    def getFailedSendFiatRequests(traderID: String): Future[Seq[SendFiatRequestHistory]] = getByTraderIDAndStatus(traderID, constants.Status.SendFiat.BLOCKCHAIN_FAILURE)

  }

}

