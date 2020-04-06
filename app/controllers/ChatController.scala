package controllers

import java.sql.Timestamp
import java.text.SimpleDateFormat
import controllers.actions.{WithTraderLoginAction, WithZoneLoginAction}
import controllers.actions.{WithLoginAction, WithTraderLoginAction}
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.master.Negotiation
import models.masterTransaction.{Chat, Chats, Message, MessageReceive, MessageReceives}
import models.{blockchain, blockchainTransaction, master, masterTransaction}
import play.api.http.ContentTypes
import play.api.i18n.{I18nSupport, Messages}
import play.api.libs.Comet
import play.api.libs.json.{JsString, JsValue, Json, OWrites, Writes}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents, Result}
import play.api.{Configuration, Logger}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ChatController @Inject()(
                                messagesControllerComponents: MessagesControllerComponents,
                                transaction: utilities.Transaction,
                                masterAccounts: master.Accounts,
                                masterAssets: master.Assets,
                                masterTradeRelations: master.TraderRelations,
                                withTraderLoginAction: WithTraderLoginAction,
                                withZoneLoginAction: WithZoneLoginAction,
                                transactionsSellerExecuteOrder: transactions.SellerExecuteOrder,
                                blockchainTransactionSellerExecuteOrders: blockchainTransaction.SellerExecuteOrders,
                                accounts: master.Accounts,
                                masterTraders: master.Traders,
                                blockchainACLAccounts: blockchain.ACLAccounts,
                                blockchainZones: blockchain.Zones,
                                blockchainNegotiations: blockchain.Negotiations,
                                withUsernameToken: WithUsernameToken,
                                masterNegotiations: master.Negotiations,
                                blockchainAssets: blockchain.Assets,
                                transactionsChangeBuyerBid: transactions.ChangeBuyerBid,
                                blockchainTransactionChangeBuyerBids: blockchainTransaction.ChangeBuyerBids,
                                utilitiesNotification: utilities.Notification,
                                masterTransactionChats: masterTransaction.Chats,
                                masterTransactionMessages: masterTransaction.Messages,
                                masterTransactionMessageReceives: masterTransaction.MessageReceives,
                              )(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_CHAT

  private val messagesPerPage = configuration.get[Int]("chatRoom.messagesPerPage")

  implicit val timeWrites = new Writes[Timestamp] {
    override def writes(t: Timestamp): JsValue = JsString(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(t))
  }
  implicit val chatWrites: OWrites[Message] = Json.writes[Message]


  // gets all chatWindows in chatRoom, position - right bottom
  def chatRoom(negotiationID: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val traderID = masterTraders.Service.tryGetID(loginState.username)
      val negotiation = masterNegotiations.Service.tryGet(negotiationID)

      def getChats(traderID: String, negotiation: Negotiation): Future[Seq[Chat]] = if (traderID == negotiation.buyerTraderID || traderID == negotiation.sellerTraderID) {
        masterTransactionChats.Service.getAllChats(negotiation.chatID.getOrElse(throw new BaseException(constants.Response.CHAT_ROOM_NOT_FOUND)))
      } else {
        throw new BaseException(constants.Response.UNAUTHORIZED)
      }

      (
        for {
          traderID <- traderID
          negotiation <- negotiation
          chats <- getChats(traderID, negotiation)
        } yield Ok(views.html.component.master.chatRoom(chatID = negotiation.chatID.getOrElse(throw new BaseException(constants.Response.CHAT_ROOM_NOT_FOUND)), chats = chats))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
      }
  }

  // populates chatWindow in chatroom
  def chatWindow(chatID: String, pageNumber: Int = 0): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val userIsParticipant = masterTransactionChats.Service.checkUserInChat(id = chatID, accountID = loginState.username)

      def getResult(userIsParticipant: Boolean): Future[Result] = {
        if (userIsParticipant) {
          val chatsInWindow = masterTransactionMessages.Service.get(chatID, pageNumber * messagesPerPage, messagesPerPage)

          def readChats(messageIDs: Seq[String]): Future[Seq[MessageReceive]] = masterTransactionMessageReceives.Service.getAllRead(messageIDs)

          for {
            chatsInWindow <- chatsInWindow
            readChats <- readChats(chatsInWindow.map(_.id))
          } yield Ok(views.html.component.master.chatWindow(views.companion.master.SendMessage.form.fill(views.companion.master.SendMessage.Data(chatID, "", None)), chatsInWindow, readChats, chatID))
        } else {
          Future(Unauthorized(views.html.trades(failures = Seq(constants.Response.UNAUTHORIZED))))
        }
      }

      (for {
        userIsParticipant <- userIsParticipant
        result <- getResult(userIsParticipant)
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
      }
  }

  // populates chatWindow in chatroom
  def loadMoreChats(chatID: String, pageNumber: Int = 0): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val userIsParticipant = masterTransactionChats.Service.checkUserInChat(id = chatID, accountID = loginState.username)

      def getResult(userIsParticipant: Boolean): Future[Result] = {
        if (userIsParticipant) {
          val chatsInWindow = masterTransactionMessages.Service.get(chatID, pageNumber * messagesPerPage, messagesPerPage)

          def readChats(messageIDs: Seq[String]): Future[Seq[MessageReceive]] = masterTransactionMessageReceives.Service.getAllRead(messageIDs)

          for {
            chatsInWindow <- chatsInWindow
            readChats <- readChats(chatsInWindow.map(_.id))
          } yield Ok(views.html.component.master.chatMessages(chatsInWindow, readChats, chatID))
        } else {
          Future(Unauthorized(views.html.trades(failures = Seq(constants.Response.UNAUTHORIZED))))
        }
      }

      (for {
        userIsParticipant <- userIsParticipant
        result <- getResult(userIsParticipant)
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
      }
  }

  //send chat form
  def sendMessageForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.sendMessage())
  }

  def sendMessage(): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
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
                    val create = masterTransactionMessageReceives.Service.create(chat.id, participant)
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
                result <- withUsernameToken.Ok(views.html.component.master.messageBox(message)())
              } yield {
                result
              }
            } else {
              Future(Unauthorized(views.html.trades(failures = Seq(constants.Response.UNAUTHORIZED))))
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
  def replyToMessage(chatID: String, messageID: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val userIsParticipant = masterTransactionChats.Service.checkUserInChat(id = chatID, accountID = loginState.username)

      def getResult(userIsParticipant: Boolean) = {
        if (userIsParticipant) {
          val chat = masterTransactionMessages.Service.get(chatID, messageID)
          for {
            chat <- chat
          } yield Ok(Json.toJson(chat))
        } else {
          Future(Unauthorized(views.html.trades(failures = Seq(constants.Response.UNAUTHORIZED))))
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

  def markChatAsRead(chatWindowID: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val userIsParticipant = masterTransactionChats.Service.checkUserInChat(id = chatWindowID, accountID = loginState.username)

      def getResult(userIsParticipant: Boolean): Future[Result] = {
        if (userIsParticipant) {
          val messageIDs = masterTransactionMessages.Service.getChatIDs(chatWindowID)

          def markRead(messageIDs: Seq[String]): Future[Int] = masterTransactionMessageReceives.Service.markRead(messageIDs, loginState.username)

          for {
            messageIDs <- messageIDs
            _ <- markRead(messageIDs)
          } yield Ok(constants.Response.MESSAGE_READ.message)
        } else {
          Future(Unauthorized(views.html.trades(failures = Seq(constants.Response.UNAUTHORIZED))))
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

  //send chat to other person in real time
  def sendMessageComet(chatWindowID: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val userIsParticipant = masterTransactionChats.Service.checkUserInChat(id = chatWindowID, accountID = loginState.username)

      def getResult(userIsParticipant: Boolean) = {
        if (userIsParticipant) {
          Future(Ok.chunked(masterTransactionMessages.Service.messageCometSource(loginState.username).via(Comet.json("parent.chatCometMessage"))).as(ContentTypes.HTML))
        } else {
          Future(Unauthorized(views.html.trades(failures = Seq(constants.Response.UNAUTHORIZED))))
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
