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
import actors.{MainChatActor, ShutdownActor}
import akka.actor.{ActorRef, ActorSystem}
import akka.stream.scaladsl.Source
import akka.stream.{ActorMaterializer, OverflowStrategy}
import play.api.libs.json.{JsString, JsValue, Json, OWrites, Reads, Writes}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Chat(id: String, fromAccountID: String, chatWindowID: String, message: String, replyToID: Option[String], createdAt: Timestamp)

case class ChatCometMessage(username: String, message: JsValue)

@Singleton
class Chats @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider, actorSystem: ActorSystem, shutdownActors: ShutdownActor)(implicit executionContext: ExecutionContext, configuration: Configuration) {

  private implicit val module: String = constants.Module.MASTER_TRANSACTION_CHAT
  private val logger: Logger = Logger(this.getClass)

  private implicit val materializer: ActorMaterializer = ActorMaterializer()(actorSystem)
  private val cometActorSleepTime = configuration.get[Long]("akka.actors.cometActorSleepTime")
  private val actorTimeout = configuration.get[Int]("akka.actors.timeout").seconds
  private val schedulerExecutionContext: ExecutionContext = actorSystem.dispatchers.lookup("akka.actors.scheduler-dispatcher")
  implicit val timeWrites = new Writes[Timestamp] {
    override def writes(t: Timestamp): JsValue = JsString(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(t))
  }
  implicit val chatWrites: OWrites[Chat] = Json.writes[Chat]

  val mainChatActor: ActorRef = actorSystem.actorOf(props = MainChatActor.props(actorTimeout, actorSystem), name = constants.Module.ACTOR_MAIN_CHAT)

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db


  import databaseConfig.profile.api._

  private[models] val chatTable = TableQuery[ChatTable]

  private def add(chat: Chat): Future[String] = db.run((chatTable returning chatTable.map(_.id) += chat).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def upsert(chat: Chat): Future[Int] = db.run(chatTable.insertOrUpdate(chat).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def findById(id: String): Future[Chat] = db.run(chatTable.filter(_.id === id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def findAllChatIDsByChatWindowID(chatWindowID: String): Future[Seq[String]] = db.run(chatTable.filter(_.chatWindowID === chatWindowID).sortBy(_.createdAt.desc).map(_.id).result.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }


  private def findChatsByChatWindowID(chatWindowID: String, offset: Int, limit: Int): Future[Seq[Chat]] = db.run(chatTable.filter(_.chatWindowID === chatWindowID).sortBy(_.createdAt.desc).drop(offset).take(limit).result.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def findChatByChatWindowID(chatWindowID: String, chatID: String): Future[Chat] = db.run(chatTable.filter(x => x.id === chatID && x.chatWindowID === chatWindowID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def deleteById(id: String): Future[Int] = db.run(chatTable.filter(_.id === id).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private[models] class ChatTable(tag: Tag) extends Table[Chat](tag, "Chat") {

    def * = (id, fromAccountID, chatWindowID, message, replyToID.?, createdAt) <> (Chat.tupled, Chat.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def fromAccountID = column[String]("fromAccountID")

    def chatWindowID = column[String]("chatWindowID")

    def message = column[String]("message")

    def replyToID = column[String]("replyToID")

    def createdAt = column[Timestamp]("createdAt")

  }

  object Service {
    def create(fromAccountID: String, chatWindowID: String, message: String, replyToID: Option[String]): Future[Chat] = {
      val chat = Chat(utilities.IDGenerator.hexadecimal, fromAccountID, chatWindowID, message, replyToID, new Timestamp(System.currentTimeMillis))
      for {
        _ <- add(chat)
      } yield chat
    }

    def get(chatWindowID: String, offset: Int, limit: Int): Future[Seq[Chat]] = findChatsByChatWindowID(chatWindowID = chatWindowID, offset = offset, limit = limit)

    def get(chatWindowID: String, chatID: String): Future[Chat] = findChatByChatWindowID(chatWindowID, chatID)

    def getChatIDs(chatWindowID: String): Future[Seq[String]] = findAllChatIDsByChatWindowID(chatWindowID)

    def chatCometSource(username: String) = {
      shutdownActors.shutdown(constants.Module.ACTOR_MAIN_CHAT, username)
      Thread.sleep(cometActorSleepTime)
      val (systemUserActor, source) = Source.actorRef[JsValue](0, OverflowStrategy.dropHead).preMaterialize()
      mainChatActor ! actors.CreateChatChildActorMessage(username = username, actorRef = systemUserActor)
      source
    }

    def sendMessageToChatActors(participants: Seq[String], chat: Chat): Unit = participants.foreach(x => mainChatActor ! ChatCometMessage(x, Json.toJson(chat)))
  }

}
