package controllers

import controllers.actions.{WithTraderLoginAction, WithoutLoginAction, WithoutLoginActionAsync}
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.{blockchain, blockchainTransaction}
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}
import transactions.blockchain.SetBuyerFeedback

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SetBuyerFeedbackController @Inject()(messagesControllerComponents: MessagesControllerComponents, transaction: utilities.Transaction, withTraderLoginAction: WithTraderLoginAction, transactionsSetBuyerFeedback: SetBuyerFeedback, blockchainTransactionSetBuyerFeedbacks: blockchainTransaction.SetBuyerFeedbacks, blockchainTraderFeedbackHistories: blockchain.TraderFeedbackHistories, withUsernameToken: WithUsernameToken, withoutLoginAction: WithoutLoginAction, withoutLoginActionAsync: WithoutLoginActionAsync)(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private implicit val module: String = constants.Module.CONTROLLERS_SET_BUYER_FEEDBACK

  def setBuyerFeedbackForm(sellerAddress: String, pegHash: String): Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
    Ok(views.html.component.master.setBuyerFeedback(sellerAddress = sellerAddress, pegHash = pegHash))
  }

  def setBuyerFeedback(): Action[AnyContent] = withTraderLoginAction { implicit loginState =>
    implicit request =>
      views.companion.master.SetBuyerFeedback.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.setBuyerFeedback(formWithErrors, sellerAddress = formWithErrors.data(constants.FormField.SELLER_ADDRESS.name), pegHash = formWithErrors.data(constants.FormField.PEG_HASH.name))))
        },
        setBuyerFeedbackData => {
          val transactionProcess = transaction.process[blockchainTransaction.SetBuyerFeedback, transactionsSetBuyerFeedback.Request](
            entity = blockchainTransaction.SetBuyerFeedback(from = loginState.address, to = setBuyerFeedbackData.sellerAddress, pegHash = setBuyerFeedbackData.pegHash, rating = setBuyerFeedbackData.rating, gas = setBuyerFeedbackData.gas, ticketID = "", mode = transactionMode),
            blockchainTransactionCreate = blockchainTransactionSetBuyerFeedbacks.Service.create,
            request = transactionsSetBuyerFeedback.Request(transactionsSetBuyerFeedback.BaseReq(from = loginState.address, gas = setBuyerFeedbackData.gas), to = setBuyerFeedbackData.sellerAddress, password = setBuyerFeedbackData.password, pegHash = setBuyerFeedbackData.pegHash, rating = setBuyerFeedbackData.rating.toString, mode = transactionMode),
            action = transactionsSetBuyerFeedback.Service.post,
            onSuccess = blockchainTransactionSetBuyerFeedbacks.Utility.onSuccess,
            onFailure = blockchainTransactionSetBuyerFeedbacks.Utility.onFailure,
            updateTransactionHash = blockchainTransactionSetBuyerFeedbacks.Service.updateTransactionHash
          )
          (for {
            _ <- transactionProcess
            result <- withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.BUYER_FEEDBACK_SET)))
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def buyerFeedbackList: Action[AnyContent] = withTraderLoginAction { implicit loginState =>
    implicit request =>
      val nullRatingsForBuyerFeedback = blockchainTraderFeedbackHistories.Service.getNullRatingsForBuyerFeedback(loginState.address)
      (for {
        nullRatingsForBuyerFeedback <- nullRatingsForBuyerFeedback
      } yield Ok(views.html.component.master.setBuyerFeedbackList(nullRatingsForBuyerFeedback))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def blockchainSetBuyerFeedbackForm: Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
    Ok(views.html.component.blockchain.setBuyerFeedback())
  }

  def blockchainSetBuyerFeedback: Action[AnyContent] = withoutLoginActionAsync { implicit loginState =>
    implicit request =>
    views.companion.blockchain.SetBuyerFeedback.form.bindFromRequest().fold(
      formWithErrors => {
        Future(BadRequest(views.html.component.blockchain.setBuyerFeedback(formWithErrors)))
      },
      setBuyerFeedbackData => {
        val postRequest = transactionsSetBuyerFeedback.Service.post(transactionsSetBuyerFeedback.Request(transactionsSetBuyerFeedback.BaseReq(from = setBuyerFeedbackData.from, gas = setBuyerFeedbackData.gas), to = setBuyerFeedbackData.to, password = setBuyerFeedbackData.password, pegHash = setBuyerFeedbackData.pegHash, rating = setBuyerFeedbackData.rating.toString, mode = setBuyerFeedbackData.mode))
        (for {
          _ <- postRequest
        } yield Ok(views.html.index(successes = Seq(constants.Response.BUYER_FEEDBACK_SET)))
          ).recover {
          case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
        }
      }
    )
  }
}
