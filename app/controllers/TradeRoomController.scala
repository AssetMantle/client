package controllers

import java.sql.Timestamp
import java.text.SimpleDateFormat

import controllers.actions.{WithTraderLoginAction}
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.masterTransaction.{Chat, ChatReceive, ChatReceives, ChatWindowParticipant, ChatWindowParticipants, ChatWindows, Chats, EmailOTPs}
import models.{master, masterTransaction}
import play.api.http.ContentTypes
import play.api.i18n.{I18nSupport, Messages}
import play.api.libs.Comet
import play.api.libs.json.{JsString, JsValue, Json, OWrites, Writes}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents, Result}
import play.api.{Configuration, Logger}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TradeRoomController @Inject()(messagesControllerComponents: MessagesControllerComponents,
                                    chatWindows: ChatWindows,
                                    chatWindowParticipants: ChatWindowParticipants,
                                    chats: Chats,
                                    chatReceives: ChatReceives,
                                    masterAccounts: master.Accounts, emailOTPs: EmailOTPs, masterContacts: master.Contacts, masterTransactionSalesQuotes: masterTransaction.SalesQuotes, masterTradeRooms: master.TradeRooms, masterTransactionTradeTerms: masterTransaction.TradeTerms, withTraderLoginAction: WithTraderLoginAction, utilitiesNotification: utilities.Notification, withUsernameToken: WithUsernameToken)(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val module: String = constants.Module.CONTROLLERS_TRADE_ROOM

  private implicit val logger: Logger = Logger(this.getClass)

  private val chatsPerPage = configuration.get[Int]("chatRoom.chatsPerPage")

  implicit val timeWrites = new Writes[Timestamp] {
    override def writes(t: Timestamp): JsValue = JsString(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(t))
  }
  implicit val chatWrites: OWrites[Chat] = Json.writes[Chat]


  def tradeRoom(tradeRoomID: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      //TODO: create chatRoom and add chatParticipants ( after invite trader in sales quote is complete)
      withUsernameToken.Ok(views.html.tradeRoom(tradeRoomID = tradeRoomID))
  }

  def tradeTerms(tradeRoomID: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val tradeTerms = masterTransactionTradeTerms.Service.get(tradeRoomID)
      (for {
        tradeTerms <- tradeTerms
      } yield Ok(views.html.component.master.tradeTermsView(tradeTerms))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def tradeFinancials: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      Future(Ok(views.html.component.master.tradeFinancials()))
  }

  def documentList: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      Future(Ok(views.html.component.master.tradeDocumentList()))
  }

  def documentView(fileName: String, documentType: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      Future(Ok(views.html.component.master.tradeDocumentView(fileName, documentType)))
  }

  def recentActivity: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      Future(Ok(views.html.component.master.recentActivity()))
  }

  def updateTermStatus(tradeID: String, element: String, value: Boolean): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>

      val updateStatus = element match {
        case constants.View.DESCRIPTION => masterTransactionTradeTerms.Service.updateAssetDescriptionStatus(tradeID, value)
        case constants.View.QUANTITY => masterTransactionTradeTerms.Service.updateAssetQuantityStatus(tradeID, value)
        case constants.View.CONTRACT_PRICE => masterTransactionTradeTerms.Service.updateAssetPriceStatus(tradeID, value)
        case constants.View.SHIPMENT_PERIOD => masterTransactionTradeTerms.Service.updateShipmentPeriodStatus(tradeID, value)
        case constants.View.LOAD_PORT => masterTransactionTradeTerms.Service.updatePortOfLoadingStatus(tradeID, value)
        case constants.View.DISCHARGE_PORT => masterTransactionTradeTerms.Service.updatePortOfDischargeStatus(tradeID, value)
        case constants.View.ADVANCE_PAYMENT => masterTransactionTradeTerms.Service.updateAdvancePaymentStatus(tradeID, value)
        case constants.View.CREDIT_TERMS => masterTransactionTradeTerms.Service.updateCreditTermsStatus(tradeID, value)
        case constants.View.BILL_OF_EXCHANGE_REQUIRED => masterTransactionTradeTerms.Service.updateBillOfExchangeRequiredStatus(tradeID, value)
        case constants.View.PRIMARY_DOCUMENTS => masterTransactionTradeTerms.Service.updatePrimaryDocumentsStatus(tradeID, value)
      }
      (for {
        _ <- updateStatus
      } yield Ok
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def updateTradeTermStatus: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.UpdateTradeTermStatus.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.updateTradeTermStatus(formWithErrors, formWithErrors.data(constants.FormField.TRADE_ID.name), formWithErrors.data(constants.FormField.TERM_TYPE.name), formWithErrors.data(constants.FormField.STATUS.name).toBoolean)))
        },
        updateTradeTermStatusData => {
          println("Form Working")
          val tradeID = updateTradeTermStatusData.tradeID
          val status = updateTradeTermStatusData.status
          println(tradeID, status, updateTradeTermStatusData.termType)
          val updateStatus = updateTradeTermStatusData.termType match {
            case constants.View.DESCRIPTION => masterTransactionTradeTerms.Service.updateAssetDescriptionStatus(tradeID, status)
            case constants.View.QUANTITY => masterTransactionTradeTerms.Service.updateAssetQuantityStatus(tradeID, status)
            case constants.View.CONTRACT_PRICE => masterTransactionTradeTerms.Service.updateAssetPriceStatus(tradeID, status)
            case constants.View.SHIPMENT_PERIOD => masterTransactionTradeTerms.Service.updateShipmentPeriodStatus(tradeID, status)
            case constants.View.LOAD_PORT => masterTransactionTradeTerms.Service.updatePortOfLoadingStatus(tradeID, status)
            case constants.View.DISCHARGE_PORT => masterTransactionTradeTerms.Service.updatePortOfDischargeStatus(tradeID, status)
            case constants.View.ADVANCE_PAYMENT => masterTransactionTradeTerms.Service.updateAdvancePaymentStatus(tradeID, status)
            case constants.View.CREDIT_TERMS => masterTransactionTradeTerms.Service.updateCreditTermsStatus(tradeID, status)
            case constants.View.BILL_OF_EXCHANGE_REQUIRED => masterTransactionTradeTerms.Service.updateBillOfExchangeRequiredStatus(tradeID, status)
            case constants.View.PRIMARY_DOCUMENTS => masterTransactionTradeTerms.Service.updatePrimaryDocumentsStatus(tradeID, status)
          }

          (for {
            _ <- updateStatus
            result <- withUsernameToken.PartialContent(views.html.component.master.updateTradeTermStatus(tradeID = tradeID, termType = updateTradeTermStatusData.termType, status = status))
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        })
  }

  def tradeList: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val tradeList = masterTradeRooms.Service.tradeListByAccountID(loginState.username)
      (for {
        tradeList <- tradeList
      } yield Ok(views.html.component.master.tradeList(tradeList))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  // gets all chatWindows in chatRoom, position - right bottom
  def chatRoom(tradeRoomID: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      //TODO: check if trader is in tradeRoom
      val allChatWindows = chatWindows.Service.getAllChatWindows(tradeRoomID)

      def allChatWindowsParticipants(chatWindowIDs: Seq[String]): Future[Seq[ChatWindowParticipant]] = chatWindowParticipants.Service.getParticipants(chatWindowIDs)

      (for {
        allChatWindows <- allChatWindows
        allChatWindowsParticipants <- allChatWindowsParticipants(allChatWindows.map(_.id))
      } yield Ok(views.html.component.master.chatRoom(allChatWindows, allChatWindowsParticipants))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  // populates chatWindow in chatroom
  def chatWindow(chatWindowID: String, pageNumber: Int = 0): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val userIsParticipant = chatWindowParticipants.Service.checkUserInChatWindow(loginState.username, chatWindowID)

      def getResult(userIsParticipant: Boolean): Future[Result] = {
        if (userIsParticipant) {
          val chatsInWindow = chats.Service.get(chatWindowID, pageNumber * chatsPerPage, chatsPerPage)

          def readChats(chatIDs: Seq[String]): Future[Seq[ChatReceive]] = chatReceives.Service.getAllRead(chatIDs)

          for {
            chatsInWindow <- chatsInWindow
            readChats <- readChats(chatsInWindow.map(_.id))
          } yield Ok(views.html.component.master.chatWindow(views.companion.master.SendChat.form.fill(views.companion.master.SendChat.Data(chatWindowID, "", None)), chatsInWindow, readChats, chatWindowID))
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
  def loadMoreChats(chatWindowID: String, pageNumber: Int = 0): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val userIsParticipant = chatWindowParticipants.Service.checkUserInChatWindow(loginState.username, chatWindowID)

      def getResult(userIsParticipant: Boolean): Future[Result] = {
        if (userIsParticipant) {
          val chatsInWindow = chats.Service.get(chatWindowID, pageNumber * chatsPerPage, chatsPerPage)

          def readChats(chatIDs: Seq[String]): Future[Seq[ChatReceive]] = chatReceives.Service.getAllRead(chatIDs)

          for {
            chatsInWindow <- chatsInWindow
            readChats <- readChats(chatsInWindow.map(_.id))
          } yield Ok(views.html.component.master.chatMessages(chatsInWindow, readChats, chatWindowID))
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
  def sendChat(): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.SendChat.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(Messages("error in message, not sent")))
        },
        sendChatData => {
          val userIsParticipant = chatWindowParticipants.Service.checkUserInChatWindow(loginState.username, sendChatData.chatWindowID)

          def getResult(userIsParticipant: Boolean) = {
            if (userIsParticipant) {
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

              for {
                chat <- chat
                participants <- participants
                _ <- chatReceive(participants.filter(_.accountID != loginState.username).map(_.accountID), chat)
                result <- withUsernameToken.Ok(Json.toJson(chat))
              } yield {
                result
              }
            } else {
              Future(Unauthorized(views.html.index(failures = Seq(constants.Response.UNAUTHORIZED))))
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
  def replyToChat(chatWindowID: String, chatID: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val userIsParticipant = chatWindowParticipants.Service.checkUserInChatWindow(loginState.username, chatWindowID)

      def getResult(userIsParticipant: Boolean) = {
        if (userIsParticipant) {
          val chat = chats.Service.get(chatWindowID, chatID)
          for {
            chat <- chat
          } yield Ok(Json.toJson(chat))
        } else {
          Future(Unauthorized(views.html.index(failures = Seq(constants.Response.UNAUTHORIZED))))
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
      val userIsParticipant = chatWindowParticipants.Service.checkUserInChatWindow(loginState.username, chatWindowID)

      def getResult(userIsParticipant: Boolean) = {
        if(userIsParticipant){
          val chatIDs = chats.Service.getChatIDs(chatWindowID)

          def markRead(chatIDs: Seq[String]): Future[Int] = chatReceives.Service.markRead(chatIDs, loginState.username)

          for {
            chatIDs <- chatIDs
            _ <- markRead(chatIDs)
          } yield Ok(constants.Response.MESSAGE_READ.message)
        }else{
          Future(Unauthorized(views.html.index(failures = Seq(constants.Response.UNAUTHORIZED))))
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
  def sendChatComet(chatWindowID: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val userIsParticipant = chatWindowParticipants.Service.checkUserInChatWindow(loginState.username, chatWindowID)
      def getResult(userIsParticipant: Boolean)= {
        if(userIsParticipant){
          Future(Ok.chunked(chats.Service.chatCometSource(loginState.username).via(Comet.json("parent.chatCometMessage"))).as(ContentTypes.HTML))
        }else{
          Future(Unauthorized(views.html.index(failures = Seq(constants.Response.UNAUTHORIZED))))
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
