package models.masterTransaction

import java.sql.Timestamp

import models.Trait
import javax.inject.{Inject, Singleton}
import models.Trait.HistoryLogged
import play.api.db.slick.DatabaseConfigProvider
import play.api.{Configuration, Logger}
import slick.jdbc.JdbcProfile
import utilities.MicroNumber

import scala.concurrent.{ExecutionContext, Future}

case class ReceiveFiatHistory(id: String, traderID: String, orderID: String, amount: MicroNumber, status: String, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None, deletedBy: String, deletedOn: Timestamp, deletedOnTimeZone: String) extends Trait.ReceiveFiat with HistoryLogged {
  def convertToReceiveFiat = ReceiveFiat(this.id, this.traderID, this.orderID, this.amount, this.status, this.createdBy, this.createdOn, this.createdOnTimeZone, this.updatedBy, this.updatedOn, this.updatedOnTimeZone)
}

@Singleton
class ReceiveFiatHistories @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) {

  case class ReceiveFiatHistorySerialized(id: String, traderID: String, orderID: String, amount: String, status: String, createdBy: Option[String], createdOn: Option[Timestamp], createdOnTimeZone: Option[String], updatedBy: Option[String], updatedOn: Option[Timestamp], updatedOnTimeZone: Option[String], deletedBy: String, deletedOn: Timestamp, deletedOnTimeZone: String) {
    def deserialize: ReceiveFiatHistory = ReceiveFiatHistory(id = id, traderID = traderID, orderID = orderID, amount = new MicroNumber(BigInt(amount)), status = status, createdBy = createdBy, createdOn = createdOn, createdOnTimeZone = createdOnTimeZone, updatedBy = updatedBy, updatedOn = updatedOn, updatedOnTimeZone = updatedOnTimeZone, deletedBy = deletedBy, deletedOn = deletedOn, deletedOnTimeZone = deletedOnTimeZone)
  }

  def serialize(receiveFiatHistory: ReceiveFiatHistory): ReceiveFiatHistorySerialized = ReceiveFiatHistorySerialized(id = receiveFiatHistory.id, traderID = receiveFiatHistory.traderID, orderID = receiveFiatHistory.orderID, amount = receiveFiatHistory.amount.toMicroString, status = receiveFiatHistory.status, createdOn = receiveFiatHistory.createdOn, createdBy = receiveFiatHistory.createdBy, createdOnTimeZone = receiveFiatHistory.createdOnTimeZone, updatedBy = receiveFiatHistory.updatedBy, updatedOn = receiveFiatHistory.updatedOn, updatedOnTimeZone = receiveFiatHistory.updatedOnTimeZone, deletedBy = receiveFiatHistory.deletedBy, deletedOn = receiveFiatHistory.deletedOn, deletedOnTimeZone = receiveFiatHistory.deletedOnTimeZone)

  private implicit val module: String = constants.Module.MASTER_TRANSACTION_RECEIVE_FIAT_HISTORY

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  import databaseConfig.profile.api._

  private[models] val receiveFiatHistoryTable = TableQuery[ReceiveFiatHistoryTable]

  private def getByTraderIDsAndStatuses(traderIDs: Seq[String], status: Seq[String]): Future[Seq[ReceiveFiatHistorySerialized]] = db.run(receiveFiatHistoryTable.filter(_.traderID inSet traderIDs).filter(_.status inSet status).result)

  private def getByTraderIDAndStatuses(traderID: String, status: Seq[String]): Future[Seq[ReceiveFiatHistorySerialized]] = db.run(receiveFiatHistoryTable.filter(_.traderID === traderID).filter(_.status inSet status).result)

  private[models] class ReceiveFiatHistoryTable(tag: Tag) extends Table[ReceiveFiatHistorySerialized](tag, "ReceiveFiat_History") {

    def * = (id, traderID, orderID, amount, status, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?, deletedBy, deletedOn, deletedOnTimeZone) <> (ReceiveFiatHistorySerialized.tupled, ReceiveFiatHistorySerialized.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def traderID = column[String]("traderID")

    def orderID = column[String]("orderID")

    def amount = column[String]("amount")

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
    def get(traderID: String): Future[Seq[ReceiveFiatHistory]] = getByTraderIDAndStatuses(traderID, Seq(constants.Status.ReceiveFiat.ORDER_COMPLETION_FIAT, constants.Status.ReceiveFiat.ORDER_REVERSED_FIAT)).map(_.map(_.deserialize))

    def get(traderIDs: Seq[String]): Future[Seq[ReceiveFiatHistory]] = getByTraderIDsAndStatuses(traderIDs, Seq(constants.Status.ReceiveFiat.ORDER_COMPLETION_FIAT, constants.Status.ReceiveFiat.ORDER_REVERSED_FIAT)).map(_.map(_.deserialize))
  }

}

