package controllers

import controllers.actions.WithLoginAction
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.blockchain
import models.blockchain.{ACLAccount, ACLAccounts}
import models.master._
import java.sql.Timestamp
import java.text.SimpleDateFormat

import actors.MainChatActor
import akka.actor.{ActorRef, ActorSystem}
import akka.stream.{ActorMaterializer, Materializer}
import akka.stream.scaladsl.Source
import play.api.http.ContentTypes
import play.api.libs.Comet
import play.api.libs.json._
import play.api.mvc._
import models.masterTransaction.{Chat, ChatCometMessage, ChatReceive, ChatReceives, ChatWindow, ChatWindowParticipant, ChatWindowParticipants, ChatWindows, Chats}
import play.api.i18n.{I18nSupport, Messages}
import play.api.libs.json.{JsString, JsValue, Json, OWrites, Reads, Writes}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}
import queries.GetAccount

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._

@Singleton
class TradeRoomController @Inject()(messagesControllerComponents: MessagesControllerComponents,
                                    actorSystem: ActorSystem,
                                    chatWindows: ChatWindows,
                                    chatWindowParticipants: ChatWindowParticipants,
                                    chats: Chats,
                                    chatReceives: ChatReceives,
                                    withLoginAction: WithLoginAction,
                                    withUsernameToken: WithUsernameToken)(implicit configuration: Configuration, executionContext: ExecutionContext) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_TRADE_ROOM

  private val chatsPerPage = configuration.get[Int]("chatRoom.chatsPerPage")

  implicit val timeWrites = new Writes[Timestamp] {
    override def writes(t: Timestamp): JsValue = JsString(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(t))
  }
  implicit val chatWrites: OWrites[Chat] = Json.writes[Chat]

  // tradeRoom main view page skeleton
  def tradeRoom: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      Future(Ok(views.html.tradeRoom("tradeID")))
  }

  // populates the trade Financials in the tradeRoom page, position- left top
  def tradeFinances: Action[AnyContent] = Action { implicit request =>
    Ok
  }

  // populates the document list in tradeRoom page, position- left bottom
  def documentList: Action[AnyContent] = Action { implicit request =>
    Ok
  }

  // populates the document upload history in tradeRoom, position- left bottom
  def documentUploadHistory: Action[AnyContent] = Action { implicit request =>
    Ok
  }

  // populates the terms view component in the tradeRoom, position- center
  def termsView: Action[AnyContent] = Action { implicit request =>
    Ok
  }

  // check and uncheck logic.

  // populates the document view component in the tradeRoom, position- center
  def documentView: Action[AnyContent] = Action { implicit request =>
    Ok
  }

  // populates the recent activity section in the traderoom,  right top
  def recentActivity: Action[AnyContent] = Action { implicit request =>
    Ok
  }

  // gets all chatWindows in chatRoom, position - right bottom
  //TODO: change LOGIN ACTIONS
  def chatRoom(tradeRoomID: String): Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      //TODO: check if trader is in tradeRoom
      val allChatWindows = chatWindows.Service.getAllChatWindows(tradeRoomID)

      def allChatWindowsParticipants(chatWindowIDs: Seq[String]): Future[Seq[ChatWindowParticipant]] = chatWindowParticipants.Service.getParticipants(chatWindowIDs)

      (for {
        allChatWindows <- allChatWindows
        allChatWindowsParticipants <- allChatWindowsParticipants(allChatWindows.map(_.id))
        result <- withUsernameToken.Ok(views.html.component.master.chatRoom(allChatWindows, allChatWindowsParticipants))
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  // populates chatWindow in chatroom
  //TODO: change LOGIN ACTIONS
  def chatWindow(chatWindowID: String, pageNumber: Int = 0): Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val userIsParticipant = chatWindowParticipants.Service.checkUserInChatWindow(loginState.username, chatWindowID)

      def getResult(userIsParticipant: Boolean): Future[Result] = {
        if (userIsParticipant) {
          val chatsInWindow = chats.Service.get(chatWindowID, pageNumber * chatsPerPage, chatsPerPage)

          def readChats(chatIDs: Seq[String]): Future[Seq[ChatReceive]] = chatReceives.Service.getAllRead(chatIDs)

          for {
            chatsInWindow <- chatsInWindow
            readChats <- readChats(chatsInWindow.map(_.id))
            result <- withUsernameToken.Ok(views.html.component.master.chatWindow(views.companion.master.SendChat.form.fill(views.companion.master.SendChat.Data(chatWindowID, "", None)), chatsInWindow, readChats, chatWindowID))
          } yield result
        } else {
          Future(Unauthorized(views.html.index(failures = Seq(constants.Response.UNAUTHORIZED))))
        }
      }

      (for {
        userIsParticipant <- userIsParticipant
        result <- getResult(userIsParticipant)
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  // populates chatWindow in chatroom
  //TODO: change LOGIN ACTIONS
  def loadMoreChats(chatWindowID: String, pageNumber: Int = 0): Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val userIsParticipant = chatWindowParticipants.Service.checkUserInChatWindow(loginState.username, chatWindowID)

      def getResult(userIsParticipant: Boolean): Future[Result] = {
        if (userIsParticipant) {
          val chatsInWindow = chats.Service.get(chatWindowID, pageNumber * chatsPerPage, chatsPerPage)

          def readChats(chatIDs: Seq[String]): Future[Seq[ChatReceive]] = chatReceives.Service.getAllRead(chatIDs)

          for {
            chatsInWindow <- chatsInWindow
            readChats <- readChats(chatsInWindow.map(_.id))
            result <- withUsernameToken.Ok(views.html.component.master.chatMessages(chatsInWindow, readChats, chatWindowID))
          } yield result
        } else {
          Future(Unauthorized(views.html.index(failures = Seq(constants.Response.UNAUTHORIZED))))
        }
      }

      (for {
        userIsParticipant <- userIsParticipant
        result <- getResult(userIsParticipant)
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  //send chat form
  //TODO: change LOGIN ACTIONS
  def sendChat(): Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.SendChat.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(Messages("error in message, not sent")))
        },
        sendChatData => {
          //TODO: check sender belongs to chatwindowID
          val chat = chats.Service.create(loginState.username, sendChatData.chatWindowID, sendChatData.message, sendChatData.replyToID)
          val participants = chatWindowParticipants.Service.getParticipants(sendChatData.chatWindowID)

          def chatReceive(participants: Seq[String], chat: Chat): Future[Unit] = {
            Future(
              for (participant <- participants) {
                val create = chatReceives.Service.create(chat.id, participant)
                for {
                  _ <- create
                } yield chats.Service.sendMessageToChatActors(participants, chat)
              }
            )
          }

          (for {
            chat <- chat
            participants <- participants
            _ <- chatReceive(participants.filter(_.accountID != loginState.username).map(_.accountID), chat)
            result <- withUsernameToken.Ok(Json.toJson(chat))
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(baseException.failure.message)
          }
        }
      )
  }

  // retrives a chat, that was part of a replied message
  //TODO: change LOGIN ACTIONS
  def replyToChat(chatWindowID: String, chatID: String): Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      //TODO: check if trader is part of the chatWindow.
      val chat = chats.Service.get(chatWindowID, chatID)
      (for {
        chat <- chat
        result <- withUsernameToken.Ok(Json.toJson(chat))
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  //TODO: change LOGIN ACTIONS
  def markChatAsRead(chatWindowID: String): Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      //TODO: check if trader is part of the chatWindow.
      val chatIDs = chats.Service.getChatIDs(chatWindowID)

      def markRead(chatIDs: Seq[String]): Future[Int] = chatReceives.Service.markRead(chatIDs, loginState.username)

      (for {
        chatIDs <- chatIDs
        _ <- markRead(chatIDs)
        result <- withUsernameToken.Ok(constants.Response.MESSAGE_READ.message)
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  //send chat to other person in real time
  //TODO: change LOGIN ACTIONS
  def sendChatComet(chatWindowID: String): Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
//      implicit val materializer: ActorMaterializer = ActorMaterializer()(actorSystem)

//      def jsonSource: Source[JsValue, _] = Source(List(JsString("jsonString")))

      Future(Ok.chunked(chats.Service.chatCometSource(loginState.username).via(Comet.json("parent.chatCometMessage"))).as(ContentTypes.HTML))
  }

}
