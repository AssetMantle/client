package models.masterTransaction

import java.sql.Timestamp

import javax.inject.{Inject, Singleton}
import models.Trait.Logged
import models.common.Serializable.TradeActivityTemplate
import play.api.{Configuration, Logger}
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

case class TradeActivityHistory(id: String, negotiationID: String, tradeActivityTemplate: TradeActivityTemplate, read: Boolean = false, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged {
  val title: String = Seq(constants.TradeActivity.PREFIX, tradeActivityTemplate.template, constants.TradeActivity.TITLE_SUFFIX).mkString(".")

  val template: String = Seq(constants.TradeActivity.PREFIX, tradeActivityTemplate.template, constants.TradeActivity.MESSAGE_SUFFIX).mkString(".")

}

@Singleton
class TradeActivityHistories @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider, configuration: Configuration)(implicit executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.MASTER_TRANSACTION_TRADE_ACTIVITY_HISTORY

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  import databaseConfig.profile.api._

  private val notificationsPerPage = configuration.get[Int]("notifications.perPage")

  case class TradeActivityHistorySerializable(id: String, negotiationID: String, tradeActivityTemplateJson: String, read: Boolean, createdBy: Option[String], createdOn: Option[Timestamp], createdOnTimeZone: Option[String], updatedBy: Option[String], updatedOn: Option[Timestamp], updatedOnTimeZone: Option[String]) {
    def deserialize(): TradeActivityHistory = TradeActivityHistory(id = id, negotiationID = negotiationID, tradeActivityTemplate = utilities.JSON.convertJsonStringToObject[TradeActivityTemplate](tradeActivityTemplateJson), read = read, createdBy = createdBy, createdOn = createdOn, createdOnTimeZone = createdOnTimeZone, updatedBy = updatedBy, updatedOn = updatedOn, updatedOnTimeZone = updatedOnTimeZone)
  }

  def serialize(tradeActivityHistory: TradeActivityHistory): TradeActivityHistorySerializable = TradeActivityHistorySerializable(id = tradeActivityHistory.id, negotiationID = tradeActivityHistory.negotiationID, tradeActivityTemplateJson = Json.toJson(tradeActivityHistory.tradeActivityTemplate).toString, read = tradeActivityHistory.read, createdBy = tradeActivityHistory.createdBy, createdOn = tradeActivityHistory.createdOn, createdOnTimeZone = tradeActivityHistory.createdOnTimeZone, updatedBy = tradeActivityHistory.updatedBy, updatedOn = tradeActivityHistory.updatedOn, updatedOnTimeZone = tradeActivityHistory.updatedOnTimeZone)

  private[models] val tradeActivityHistoryTable = TableQuery[TradeActivityHistoryTable]


  private def findAllByNegotiationID(negotiationID: String, offset: Int, limit: Int): Future[Seq[TradeActivityHistorySerializable]] = db.run(tradeActivityHistoryTable.filter(_.negotiationID === negotiationID).sortBy(_.createdOn.desc).drop(offset).take(limit).result)


  private[models] class TradeActivityHistoryTable(tag: Tag) extends Table[TradeActivityHistorySerializable](tag, "TradeActivity_History") {

    def * = (id, negotiationID, tradeActivityTemplateJson, read, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (TradeActivityHistorySerializable.tupled, TradeActivityHistorySerializable.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def negotiationID = column[String]("negotiationID")

    def tradeActivityTemplateJson = column[String]("tradeActivityTemplateJson")

    def read = column[Boolean]("read")

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")

  }

  object Service {
    def getAllTradeActivities(negotiationID: String, pageNumber: Int): Future[Seq[TradeActivityHistory]] = findAllByNegotiationID(negotiationID = negotiationID, offset = (pageNumber - 1) * notificationsPerPage, limit = notificationsPerPage).map(serializedTradeActivities => serializedTradeActivities.map(_.deserialize()))
  }

}
