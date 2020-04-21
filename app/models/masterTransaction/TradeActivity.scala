package models.masterTransaction

import java.sql.Timestamp

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Trait.Logged
import models.common.Node
import models.common.Serializable.TradeActivityTemplate
import org.postgresql.util.PSQLException
import play.api.{Configuration, Logger}
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class TradeActivity(id: String, negotiationID: String, tradeActivityTemplate: TradeActivityTemplate, read: Boolean = false, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimezone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged[TradeActivity] {
  val title: String = Seq(constants.TradeActivity.PREFIX, tradeActivityTemplate.template, constants.TradeActivity.TITLE_SUFFIX).mkString(".")

  val template: String = Seq(constants.TradeActivity.PREFIX, tradeActivityTemplate.template, constants.TradeActivity.MESSAGE_SUFFIX).mkString(".")

  def createLog()(implicit node: Node): TradeActivity = copy(createdBy = Option(node.id), createdOn = Option(new Timestamp(System.currentTimeMillis())), createdOnTimezone = Option(node.timeZone))

  def updateLog()(implicit node: Node): TradeActivity = copy(updatedBy = Option(node.id), updatedOn = Option(new Timestamp(System.currentTimeMillis())), updatedOnTimeZone = Option(node.timeZone))

}

@Singleton
class TradeActivities @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider, configuration: Configuration)(implicit executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.MASTER_TRANSACTION_TRADE_ACTIVITY

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  import databaseConfig.profile.api._

  private implicit val node: Node = Node(id = configuration.get[String]("node.id"), timeZone = configuration.get[String]("node.timeZone"))

  private val notificationsPerPage = configuration.get[Int]("notifications.perPage")

  case class TradeActivitySerializable(id: String, negotiationID: String, tradeActivityTemplateJson: String, read: Boolean, createdBy: Option[String], createdOn: Option[Timestamp], createdOnTimezone: Option[String], updatedBy: Option[String], updatedOn: Option[Timestamp], updatedOnTimeZone: Option[String]) {
    def deserialize(): TradeActivity = TradeActivity(id = id, negotiationID = negotiationID, tradeActivityTemplate = utilities.JSON.convertJsonStringToObject[TradeActivityTemplate](tradeActivityTemplateJson), read = read, createdBy = createdBy, createdOn = createdOn, createdOnTimezone = createdOnTimezone, updatedBy = updatedBy, updatedOn = updatedOn, updatedOnTimeZone = updatedOnTimeZone)
  }

  def serialize(tradeActivity: TradeActivity): TradeActivitySerializable = TradeActivitySerializable(id = tradeActivity.id, negotiationID = tradeActivity.negotiationID, tradeActivityTemplateJson = Json.toJson(tradeActivity.tradeActivityTemplate).toString, read = tradeActivity.read, createdBy = tradeActivity.createdBy, createdOn = tradeActivity.createdOn, createdOnTimezone = tradeActivity.createdOnTimezone, updatedBy = tradeActivity.updatedBy, updatedOn = tradeActivity.updatedOn, updatedOnTimeZone = tradeActivity.updatedOnTimeZone)

  private[models] val tradeActivityTable = TableQuery[TradeActivityTable]

  private def add(tradeActivity: TradeActivity): Future[String] = db.run((tradeActivityTable returning tradeActivityTable.map(_.id) += serialize(tradeActivity.createLog())).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def findAllByNegotiationID(negotiationID: String, offset: Int, limit: Int): Future[Seq[TradeActivitySerializable]] = db.run(tradeActivityTable.filter(_.negotiationID === negotiationID).sortBy(_.createdOn.desc).drop(offset).take(limit).result)

  private def deleteById(negotiationID: String): Future[Int] = db.run(tradeActivityTable.filter(_.negotiationID === negotiationID).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private[models] class TradeActivityTable(tag: Tag) extends Table[TradeActivitySerializable](tag, "TradeActivity") {

    def * = (id, negotiationID, tradeActivityTemplateJson, read, createdBy.?, createdOn.?, createdOnTimezone.?, updatedBy.?, updatedOn.?, updatedOnTimezone.?) <> (TradeActivitySerializable.tupled, TradeActivitySerializable.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def negotiationID = column[String]("negotiationID")

    def tradeActivityTemplateJson = column[String]("tradeActivityTemplateJson")

    def read = column[Boolean]("read")

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimezone = column[String]("createdOnTimezone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimezone = column[String]("updatedOnTimezone")

  }

  object Service {
    def create(negotiationID: String, tradeActivity: constants.TradeActivity, parameters: String*): Future[String] = add(TradeActivity(id = utilities.IDGenerator.hexadecimal, negotiationID = negotiationID, tradeActivityTemplate = TradeActivityTemplate(template = tradeActivity.template, parameters = parameters)))

    def getAllTradeActivities(negotiationID: String, pageNumber: Int): Future[Seq[TradeActivity]] = findAllByNegotiationID(negotiationID = negotiationID, offset = (pageNumber - 1) * notificationsPerPage, limit = notificationsPerPage).map(serializedTradeActivities => serializedTradeActivities.map(_.deserialize()))
  }

}
