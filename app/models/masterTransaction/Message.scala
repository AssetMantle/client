package models.masterTransaction

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import org.postgresql.util.PSQLException
import play.api.{Configuration, Logger}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import java.sql.Timestamp
import java.text.SimpleDateFormat

import models.Trait.Logged
import models.common.Node
import play.api.libs.json.{JsString, JsValue, Json, OWrites, Reads, Writes}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Message(id: String, fromAccountID: String, chatID: String, text: String, replyToID: Option[String], createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged[Message] {
  def createLog()(implicit node: Node): Message = copy(createdBy = Option(node.id), createdOn = Option(new Timestamp(System.currentTimeMillis())), createdOnTimeZone = Option(node.timeZone))

  def updateLog()(implicit node: Node): Message = copy(updatedBy = Option(node.id), updatedOn = Option(new Timestamp(System.currentTimeMillis())), updatedOnTimeZone = Option(node.timeZone))

}

@Singleton
class Messages @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider, configuration: Configuration)(implicit executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.MASTER_TRANSACTION_MESSAGE

  private implicit val logger: Logger = Logger(this.getClass)

  implicit val timeWrites: Writes[Timestamp] = (t: Timestamp) => JsString(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(t))

  implicit val messageWrites: OWrites[Message] = Json.writes[Message]

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val messageTable = TableQuery[MessageTable]

  private implicit val node: Node = Node(id = configuration.get[String]("node.id"), timeZone = configuration.get[String]("node.timeZone"))

  private def add(message: Message): Future[String] = db.run((messageTable returning messageTable.map(_.id) += message.createLog()).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def findById(id: String): Future[Message] = db.run(messageTable.filter(_.id === id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def findAllChatIDsByChatWindowID(chatID: String): Future[Seq[String]] = db.run(messageTable.filter(_.chatID === chatID).sortBy(_.createdOn.desc).map(_.id).result.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }


  private def findChatsByChatWindowID(chatID: String, offset: Int, limit: Int): Future[Seq[Message]] = db.run(messageTable.filter(_.chatID === chatID).sortBy(_.createdOn.desc).drop(offset).take(limit).result.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def findChatByChatWindowID(chatID: String, messageID: String): Future[Message] = db.run(messageTable.filter(x => x.id === messageID && x.chatID === chatID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def deleteById(id: String): Future[Int] = db.run(messageTable.filter(_.id === id).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private[models] class MessageTable(tag: Tag) extends Table[Message](tag, "Message") {

    def * = (id, fromAccountID, chatID, text, replyToID.?, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (Message.tupled, Message.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def fromAccountID = column[String]("fromAccountID")

    def chatID = column[String]("chatID")

    def text = column[String]("text")

    def replyToID = column[String]("replyToID")

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")

  }

  object Service {
    def create(fromAccountID: String, chatID: String, text: String, replyToID: Option[String]): Future[Message] = {
      val message = Message(utilities.IDGenerator.hexadecimal, fromAccountID, chatID, text, replyToID)
      for {
        _ <- add(message)
      } yield message
    }

    def get(chatID: String, offset: Int, limit: Int): Future[Seq[Message]] = findChatsByChatWindowID(chatID = chatID, offset = offset, limit = limit)

    def get(chatID: String, messageID: String): Future[Message] = findChatByChatWindowID(chatID, messageID)

    def getChatIDs(chatID: String): Future[Seq[String]] = findAllChatIDsByChatWindowID(chatID)

    def sendMessageToChatActors(participants: Seq[String], message: Message): Unit = participants.foreach(x => actors.Service.cometActor ! actors.Message.makeCometMessage(x, constants.Comet.CHAT, message))
  }

}
