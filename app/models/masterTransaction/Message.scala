package models.masterTransaction

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import org.postgresql.util.PSQLException
import play.api.{Configuration, Logger}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import java.sql.Timestamp
import java.text.SimpleDateFormat

import scala.concurrent.duration._
import actors.{MainMessageActor, ShutdownActor}
import akka.actor.{ActorRef, ActorSystem}
import akka.stream.scaladsl.Source
import akka.stream.{ActorMaterializer, OverflowStrategy}
import play.api.libs.json.{JsString, JsValue, Json, OWrites, Reads, Writes}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Message(id: String, fromAccountID: String, chatID: String, text: String, replyToID: Option[String], createdAt: Timestamp)

case class MessageCometMessage(username: String, message: JsValue)

@Singleton
class Messages @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider, actorSystem: ActorSystem, shutdownActors: ShutdownActor)(implicit executionContext: ExecutionContext, configuration: Configuration) {

  private implicit val module: String = constants.Module.MASTER_TRANSACTION_MESSAGE
  private val logger: Logger = Logger(this.getClass)

  private implicit val materializer: ActorMaterializer = ActorMaterializer()(actorSystem)
  private val cometActorSleepTime = configuration.get[Long]("akka.actors.cometActorSleepTime")
  private val actorTimeout = configuration.get[Int]("akka.actors.timeout").seconds
  private val schedulerExecutionContext: ExecutionContext = actorSystem.dispatchers.lookup("akka.actors.scheduler-dispatcher")
  implicit val timeWrites = new Writes[Timestamp] {
    override def writes(t: Timestamp): JsValue = JsString(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(t))
  }
  implicit val messageWrites: OWrites[Message] = Json.writes[Message]

  val mainMessageActor: ActorRef = actorSystem.actorOf(props = MainMessageActor.props(actorTimeout, actorSystem), name = constants.Module.ACTOR_MAIN_MESSAGE)

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db


  import databaseConfig.profile.api._

  private[models] val messageTable = TableQuery[MessageTable]

  private def add(message: Message): Future[String] = db.run((messageTable returning messageTable.map(_.id) += message).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def upsert(message: Message): Future[Int] = db.run(messageTable.insertOrUpdate(message).asTry).map {
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

  private def findAllChatIDsByChatWindowID(chatID: String): Future[Seq[String]] = db.run(messageTable.filter(_.chatID === chatID).sortBy(_.createdAt.desc).map(_.id).result.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }


  private def findChatsByChatWindowID(chatID: String, offset: Int, limit: Int): Future[Seq[Message]] = db.run(messageTable.filter(_.chatID === chatID).sortBy(_.createdAt.desc).drop(offset).take(limit).result.asTry).map {
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

    def * = (id, fromAccountID, chatID, text, replyToID.?, createdAt) <> (Message.tupled, Message.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def fromAccountID = column[String]("fromAccountID")

    def chatID = column[String]("chatID")

    def text = column[String]("text")

    def replyToID = column[String]("replyToID")

    def createdAt = column[Timestamp]("createdAt")

  }

  object Service {
    def create(fromAccountID: String, chatID: String, text: String, replyToID: Option[String]): Future[Message] = {
      val message = Message(utilities.IDGenerator.hexadecimal, fromAccountID, chatID, text, replyToID, new Timestamp(System.currentTimeMillis))
      for {
        _ <- add(message)
      } yield message
    }

    def get(chatID: String, offset: Int, limit: Int): Future[Seq[Message]] = findChatsByChatWindowID(chatID = chatID, offset = offset, limit = limit)

    def get(chatID: String, messageID: String): Future[Message] = findChatByChatWindowID(chatID, messageID)

    def getChatIDs(chatID: String): Future[Seq[String]] = findAllChatIDsByChatWindowID(chatID)

    def messageCometSource(username: String) = {
      shutdownActors.shutdown(constants.Module.ACTOR_MAIN_MESSAGE, username)
      Thread.sleep(cometActorSleepTime)
      val (systemUserActor, source) = Source.actorRef[JsValue](0, OverflowStrategy.dropHead).preMaterialize()
      mainMessageActor ! actors.CreateMessageChildActorMessage(username = username, actorRef = systemUserActor)
      source
    }

    def sendMessageToChatActors(participants: Seq[String], message: Message): Unit = participants.foreach(x => mainMessageActor ! MessageCometMessage(x, Json.toJson(message)))
  }

}
