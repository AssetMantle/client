package controllers

import controllers.actions.WithLoginAction
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.blockchain
import models.blockchain.{ACLAccount, ACLAccounts}
import models.master._
import models.masterTransaction.ChatRooms
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}
import queries.GetAccount

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TradeRoomController @Inject()(messagesControllerComponents: MessagesControllerComponents,
                                    chatRooms: ChatRooms,
                                    withLoginAction: WithLoginAction,
                                    withUsernameToken: WithUsernameToken)(implicit configuration: Configuration, executionContext: ExecutionContext) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_TRADE_ROOM

  private val chatsPerPage = configuration.get[Int]("chatRoom.chatsPerPage")

  // tradeRoom main view page skeleton
  def tradeRoom: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      Future(Ok(views.html.tradeRoom()))
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

  // populates chatRoom in the tradeRoom, position, right bottom
  def chatRoom(tradeRoomID: String = "chatRoomData.tradeRoomID", pageNumber: Int = 0): Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val chats = chatRooms.Service.get(tradeRoomID, pageNumber * chatsPerPage, chatsPerPage)
      (for {
        chats <- chats
      } yield Ok(views.html.component.master.chatRoom(views.companion.master.SendChat.form.fill(views.companion.master.SendChat.Data(tradeRoomID, "", false)), chats))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def sendChat(): Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.SendChat.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest("REQUEST NOT WELL FORMED"))
        },
        chatRoomData => {
          // check for buyer or seller sending message to mark read..
          val chat = chatRooms.Service.create(loginState.username, "chatRoomData.tradeRoomID", chatRoomData.chatContent, true, true, chatRoomData.financierVisibility, true)
          (for {
            chat <- chat
            result <- withUsernameToken.Ok(chat.chatContent)
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(baseException.failure.message)
          }
        }
      )
  }

  /*
    def notificationPage(pageNumber: Int = 0): Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
      implicit request =>
        val notification = notifications.Service.get(loginState.username, pageNumber * limit, limit)
        (for {
          notification <- notification
        } yield Ok(views.html.component.master.notifications(notification))
          ).recover {
          case baseException: BaseException => InternalServerError(baseException.failure.message)
        }
    }
  */
}
