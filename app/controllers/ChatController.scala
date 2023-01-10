package controllers

import java.sql.Timestamp
import java.text.SimpleDateFormat

import controllers.actions.{WithLoginActionAsync, WithoutLoginAction}
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.masterTransaction.{Chat, Message, MessageRead}
import models.{master, masterTransaction}
import play.api.i18n.{I18nSupport, Messages}
import play.api.libs.json.{JsString, JsValue, Json, OWrites, Writes}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents, Result}
import play.api.{Configuration, Logger}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ChatController @Inject()(
                                messagesControllerComponents: MessagesControllerComponents,
                                withLoginActionAsync: WithLoginActionAsync,
                                withUsernameToken: WithUsernameToken,
                                masterTransactionChats: masterTransaction.Chats,
                                masterTransactionMessages: masterTransaction.Messages,
                                masterTransactionMessageReads: masterTransaction.MessageReads,
                                withoutLoginAction: WithoutLoginAction,
                              )(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_CHAT

  private val messagesPerPage = configuration.get[Int]("chatRoom.messagesPerPage")

  implicit val timeWrites: Writes[Timestamp] = new Writes[Timestamp] {
    override def writes(t: Timestamp): JsValue = JsString(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(t))
  }

  implicit val chatWrites: OWrites[Message] = Json.writes[Message]

  // gets all chatWindows in chatRoom, position - right bottom
  def chatRoom(negotiationID: String): Action[AnyContent] = withLoginActionAsync { implicit loginState =>
    implicit request =>
      val verifyChatRoomParticipants = true
      val chatID = "chatID"

      def getChats: Future[Seq[Chat]] = if (verifyChatRoomParticipants) {
        masterTransactionChats.Service.getAllChats(chatID)
      } else {
        throw new BaseException(constants.Response.UNAUTHORIZED)
      }

      (
        for {
          chats <- getChats
        } yield Ok(views.html.component.master.chat.chatRoom(chatID = chatID, chats = chats))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  // populates chatWindow in chatroom
  def chatWindow(chatID: String, pageNumber: Int = 0): Action[AnyContent] = withLoginActionAsync { implicit loginState =>
    implicit request =>
      val userIsParticipant = masterTransactionChats.Service.checkUserInChat(id = chatID, accountID = loginState.username)

      def getResult(userIsParticipant: Boolean): Future[Result] = {
        if (userIsParticipant) {
          val chatsInWindow = masterTransactionMessages.Service.get(chatID, pageNumber * messagesPerPage, messagesPerPage)

          def readChats(messageIDs: Seq[String]): Future[Seq[MessageRead]] = masterTransactionMessageReads.Service.getAllRead(messageIDs)

          for {
            chatsInWindow <- chatsInWindow
            readChats <- readChats(chatsInWindow.map(_.id))
          } yield Ok(views.html.component.master.chat.chatWindow(views.companion.master.SendMessage.form.fill(views.companion.master.SendMessage.Data(chatID, "", None)), chatsInWindow, readChats, chatID))
        } else {
          Future(Unauthorized)
        }
      }

      (for {
        userIsParticipant <- userIsParticipant
        result <- getResult(userIsParticipant)
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  // populates chatWindow in chatroom
  def loadMoreChats(chatID: String, pageNumber: Int = 0): Action[AnyContent] = withLoginActionAsync { implicit loginState =>
    implicit request =>
      val userIsParticipant = masterTransactionChats.Service.checkUserInChat(id = chatID, accountID = loginState.username)

      def getResult(userIsParticipant: Boolean): Future[Result] = {
        if (userIsParticipant) {
          val chatsInWindow = masterTransactionMessages.Service.get(chatID, pageNumber * messagesPerPage, messagesPerPage)

          def readChats(messageIDs: Seq[String]): Future[Seq[MessageRead]] = masterTransactionMessageReads.Service.getAllRead(messageIDs)

          for {
            chatsInWindow <- chatsInWindow
            readChats <- readChats(chatsInWindow.map(_.id))
          } yield Ok(views.html.component.master.chat.chatMessages(chatsInWindow, readChats, chatID))
        } else {
          Future(Unauthorized)
        }
      }

      (for {
        userIsParticipant <- userIsParticipant
        result <- getResult(userIsParticipant)
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  //send chat form
  def sendMessageForm: Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
      Ok(views.html.component.master.chat.sendMessage())
  }

  def sendMessage(): Action[AnyContent] = withLoginActionAsync { implicit loginState =>
    implicit request =>
      views.companion.master.SendMessage.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(Messages(constants.View.MESSAGE_NOT_SENT)))
        },
        sendMessageData => {
          val userIsParticipant = masterTransactionChats.Service.checkUserInChat(id = sendMessageData.chatID, accountID = loginState.username)

          def getResult(userIsParticipant: Boolean): Future[Result] = {
            if (userIsParticipant) {
              val message = masterTransactionMessages.Service.create(loginState.username, sendMessageData.chatID, sendMessageData.text, sendMessageData.replyToID)
              val chats = masterTransactionChats.Service.getAllChats(sendMessageData.chatID)

              def chatReceive(participants: Seq[String], chat: Message): Future[Unit] = {
                Future(
                  for (participant <- participants) {
                    val create = masterTransactionMessageReads.Service.create(chat.id, participant)
                    for {
                      _ <- create
                    } yield masterTransactionMessages.Service.sendMessageToChatActors(participants, chat)
                  }
                )
              }

              for {
                message <- message
                chats <- chats
                _ <- chatReceive(chats.filter(_.accountID != loginState.username).map(_.accountID), message)
                result <- withUsernameToken.Ok(views.html.component.master.chat.messageBox(message)())
              } yield {
                result
              }
            } else {
              Future(Unauthorized)
            }
          }

          (for {
            userIsParticipant <- userIsParticipant
            result <- getResult(userIsParticipant)
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(baseException.failure.message)
          }
        }
      )
  }

  // retrives a chat, that was part of a replied message
  def replyToMessage(chatID: String, messageID: String): Action[AnyContent] = withLoginActionAsync { implicit loginState =>
    implicit request =>
      val userIsParticipant = masterTransactionChats.Service.checkUserInChat(id = chatID, accountID = loginState.username)

      def getResult(userIsParticipant: Boolean) = {
        if (userIsParticipant) {
          val chat = masterTransactionMessages.Service.get(chatID, messageID)
          for {
            chat <- chat
          } yield Ok(Json.toJson(chat))
        } else {
          Future(Unauthorized)
        }

      }

      (for {
        userIsParticipant <- userIsParticipant
        result <- getResult(userIsParticipant)
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def markChatAsRead(chatWindowID: String): Action[AnyContent] = withLoginActionAsync { implicit loginState =>
    implicit request =>
      val userIsParticipant = masterTransactionChats.Service.checkUserInChat(id = chatWindowID, accountID = loginState.username)

      def getResult(userIsParticipant: Boolean): Future[Result] = {
        if (userIsParticipant) {
          val messageIDs = masterTransactionMessages.Service.getChatIDs(chatWindowID)

          def markRead(messageIDs: Seq[String]): Future[Int] = masterTransactionMessageReads.Service.markRead(messageIDs, loginState.username)

          for {
            messageIDs <- messageIDs
            _ <- markRead(messageIDs)
          } yield Ok(constants.Response.MESSAGE_READ.message)
        } else {
          Future(Unauthorized)
        }
      }

      (for {
        userIsParticipant <- userIsParticipant
        result <- getResult(userIsParticipant)
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }
}
