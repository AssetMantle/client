package controllers

import controllers.actions.{WithTraderLoginAction, WithoutLoginAction, WithoutLoginActionAsync}
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.{blockchain, blockchainTransaction}
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}
import transactions.blockchain.SetSellerFeedback

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SetSellerFeedbackController @Inject()(messagesControllerComponents: MessagesControllerComponents, transaction: utilities.Transaction, withTraderLoginAction: WithTraderLoginAction, transactionsSetSellerFeedback: SetSellerFeedback, blockchainTransactionSetSellerFeedbacks: blockchainTransaction.SetSellerFeedbacks, blockchainTraderFeedbackHistories: blockchain.TraderFeedbackHistories, withUsernameToken: WithUsernameToken, withoutLoginAction: WithoutLoginAction, withoutLoginActionAsync: WithoutLoginActionAsync)(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private implicit val module: String = constants.Module.CONTROLLERS_SET_SELLER_FEEDBACK

  def setSellerFeedbackForm(buyerAddress: String, pegHash: String): Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
    Ok(views.html.component.master.setSellerFeedback(buyerAddress = buyerAddress, pegHash = pegHash))
  }

  def setSellerFeedback(): Action[AnyContent] = withTraderLoginAction { implicit loginState =>
    implicit request =>
      views.companion.master.SetSellerFeedback.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.setSellerFeedback(formWithErrors, buyerAddress = formWithErrors.data(constants.FormField.BUYER_ADDRESS.name), pegHash = formWithErrors.data(constants.FormField.PEG_HASH.name))))
        },
        setSellerFeedbackData => {
          val transactionProcess = transaction.process[blockchainTransaction.SetSellerFeedback, transactionsSetSellerFeedback.Request](
            entity = blockchainTransaction.SetSellerFeedback(from = loginState.address, to = setSellerFeedbackData.buyerAddress, pegHash = setSellerFeedbackData.pegHash, rating = setSellerFeedbackData.rating, gas = setSellerFeedbackData.gas, ticketID = "", mode = transactionMode),
            blockchainTransactionCreate = blockchainTransactionSetSellerFeedbacks.Service.create,
            request = transactionsSetSellerFeedback.Request(transactionsSetSellerFeedback.BaseReq(from = loginState.address, gas = setSellerFeedbackData.gas), to = setSellerFeedbackData.buyerAddress, password = setSellerFeedbackData.password, pegHash = setSellerFeedbackData.pegHash, rating = setSellerFeedbackData.rating.toString, mode = transactionMode),
            action = transactionsSetSellerFeedback.Service.post,
            onSuccess = blockchainTransactionSetSellerFeedbacks.Utility.onSuccess,
            onFailure = blockchainTransactionSetSellerFeedbacks.Utility.onFailure,
            updateTransactionHash = blockchainTransactionSetSellerFeedbacks.Service.updateTransactionHash
          )
          (for {
            _ <- transactionProcess
            result <- withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.SELLER_FEEDBACK_SET)))
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def sellerFeedbackList: Action[AnyContent] = withTraderLoginAction { implicit loginState =>
    implicit request =>
      val nullRatingsForSellerFeedback = blockchainTraderFeedbackHistories.Service.getNullRatingsForSellerFeedback(loginState.address)
      (for {
        nullRatingsForSellerFeedback <- nullRatingsForSellerFeedback
      } yield Ok(views.html.component.master.setSellerFeedbackList(nullRatingsForSellerFeedback))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def blockchainSetSellerFeedbackForm: Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
    Ok(views.html.component.blockchain.setSellerFeedback())
  }

  def blockchainSetSellerFeedback: Action[AnyContent] = withoutLoginActionAsync { implicit loginState =>
    implicit request =>
    views.companion.blockchain.SetSellerFeedback.form.bindFromRequest().fold(
      formWithErrors => {
        Future(BadRequest(views.html.component.blockchain.setSellerFeedback(formWithErrors)))
      },
      setSellerFeedbackData => {
        val postRequest = transactionsSetSellerFeedback.Service.post(transactionsSetSellerFeedback.Request(transactionsSetSellerFeedback.BaseReq(from = setSellerFeedbackData.from, gas = setSellerFeedbackData.gas), to = setSellerFeedbackData.to, password = setSellerFeedbackData.password, pegHash = setSellerFeedbackData.pegHash, rating = setSellerFeedbackData.rating.toString, mode = setSellerFeedbackData.mode))
        (for {
          _ <- postRequest
        } yield Ok(views.html.index(successes = Seq(constants.Response.SELLER_FEEDBACK_SET)))
          ).recover {
          case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
        }
      }
    )
  }
}
