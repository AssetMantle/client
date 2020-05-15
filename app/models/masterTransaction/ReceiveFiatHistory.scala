package models.masterTransaction

import java.sql.Timestamp

import javax.inject.{Inject, Singleton}
import models.Trait.HistoryLogged
import play.api.db.slick.DatabaseConfigProvider
import play.api.{Configuration, Logger}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

case class ReceiveFiatHistory(id: String, traderID: String, orderID: String, amount: Int, status: String, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None, deletedBy: String, deletedOn: Timestamp, deletedOnTimeZone: String) extends HistoryLogged

@Singleton
class ReceiveFiatHistories @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.MASTER_TRANSACTION_RECEIVE_FIAT_HISTORY

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  import databaseConfig.profile.api._

  private[models] val receiveFiatHistoryTable = TableQuery[ReceiveFiatHistoryTable]

  private def getByTraderIDsAndStatuses(traderIDs: Seq[String], status: Seq[String]): Future[Seq[ReceiveFiatHistory]] = db.run(receiveFiatHistoryTable.filter(_.traderID inSet traderIDs).filter(_.status inSet status).result)

  private def getByTraderIDAndStatuses(traderID: String, status: Seq[String]): Future[Seq[ReceiveFiatHistory]] = db.run(receiveFiatHistoryTable.filter(_.traderID === traderID).filter(_.status inSet status).result)

  private[models] class ReceiveFiatHistoryTable(tag: Tag) extends Table[ReceiveFiatHistory](tag, "ReceiveFiat_History") {

    def * = (id, traderID, orderID, amount, status, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?, deletedBy, deletedOn, deletedOnTimeZone) <> (ReceiveFiatHistory.tupled, ReceiveFiatHistory.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def traderID = column[String]("traderID")

    def orderID = column[String]("orderID")

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
    def get(traderID: String): Future[Seq[ReceiveFiatHistory]] = getByTraderIDAndStatuses(traderID, Seq(constants.Status.ReceiveFiat.ORDER_COMPLETION_FIAT, constants.Status.ReceiveFiat.ORDER_REVERSED_FIAT))

    def get(traderIDs: Seq[String]): Future[Seq[ReceiveFiatHistory]] = getByTraderIDsAndStatuses(traderIDs, Seq(constants.Status.ReceiveFiat.ORDER_COMPLETION_FIAT, constants.Status.ReceiveFiat.ORDER_REVERSED_FIAT))
  }

}

