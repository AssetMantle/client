package models.masterTransaction

import java.sql.Timestamp

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Trait.Logged
import models.common.Serializable.TradeActivityMessage
import org.postgresql.util.PSQLException
import play.api.{Configuration, Logger}
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class TradeActivity(id: String, negotiationID: String, message: TradeActivityMessage, read: Boolean = false, createdBy: String, createdOn: Timestamp, createdOnTimezone: String, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged {
  val title: String = Seq(constants.TradeActivity.PREFIX, message.template, constants.TradeActivity.TITLE_SUFFIX).mkString(".")

  val messageTemplate: String = Seq(constants.TradeActivity.PREFIX, message.template, constants.TradeActivity.MESSAGE_SUFFIX).mkString(".")

  val messageParameters: Seq[String] = message.parameters
}


@Singleton
class TradeActivities @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider, configuration: Configuration)(implicit executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.MASTER_TRANSACTION_TRADE_ACTIVITY

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  import databaseConfig.profile.api._

  private val nodeID = configuration.get[String]("node.id")

  private val nodeTimezone = configuration.get[String]("node.timezone")

  private val notificationsPerPage = configuration.get[Int]("notifications.perPage")

  case class TradeActivitySerializable(id: String, negotiationID: String, message: String, read: Boolean, createdBy: String, createdOn: Timestamp, createdOnTimezone: String, updatedBy: Option[String], updatedOn: Option[Timestamp], updatedOnTimeZone: Option[String]) {
    def deserialize(): TradeActivity = TradeActivity(id = id, negotiationID = negotiationID, message = utilities.JSON.convertJsonStringToObject[TradeActivityMessage](message), read = read, createdBy = createdBy, createdOn = createdOn, createdOnTimezone = createdOnTimezone, updatedBy = updatedBy, updatedOn = updatedOn, updatedOnTimeZone = updatedOnTimeZone)
  }

  def serialize(tradeActivity: TradeActivity): TradeActivitySerializable = TradeActivitySerializable(id = tradeActivity.id, negotiationID = tradeActivity.negotiationID, message = Json.toJson(tradeActivity.message).toString(), read = tradeActivity.read, createdBy = tradeActivity.createdBy, createdOn = tradeActivity.createdOn, createdOnTimezone = tradeActivity.createdOnTimezone, updatedBy = tradeActivity.updatedBy, updatedOn = tradeActivity.updatedOn, updatedOnTimeZone = tradeActivity.updatedOnTimeZone)

  private[models] val tradeActivityTable = TableQuery[TradeActivityTable]

  private def add(negotiationID: String, template: String, parameters: String*): Future[String] = db.run((tradeActivityTable returning tradeActivityTable.map(_.id) += serialize(TradeActivity(id = utilities.IDGenerator.hexadecimal, negotiationID = negotiationID, message = TradeActivityMessage(template = template, parameters = parameters), createdBy = nodeID, createdOn = new Timestamp(System.currentTimeMillis()), createdOnTimezone = nodeTimezone))).asTry).map {
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

    def * = (id, negotiationID, message, read, createdBy, createdOn, createdOnTimezone, updatedBy.?, updatedOn.?, updatedOnTimezone.?) <> (TradeActivitySerializable.tupled, TradeActivitySerializable.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def negotiationID = column[String]("negotiationID")

    def message = column[String]("message")

    def read = column[Boolean]("read")

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimezone = column[String]("createdOnTimezone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimezone = column[String]("updatedOnTimezone")

  }

  object Service {
    def create(negotiationID: String, tradeActivity: constants.TradeActivity, parameters: String*): Future[String] = add(negotiationID = negotiationID, template = tradeActivity.template, parameters = parameters: _*)

    def getAllTradeActivities(negotiationID: String, pageNumber: Int): Future[Seq[TradeActivity]] = findAllByNegotiationID(negotiationID = negotiationID, offset = (pageNumber - 1) * notificationsPerPage, limit = notificationsPerPage).map(serializedTradeActivities => serializedTradeActivities.map(_.deserialize()))
  }

}
