package controllers

import controllers.actions.WithTraderLoginAction
import controllers.results.WithUsernameToken
import exceptions.{BaseException, BlockChainException}
import javax.inject.{Inject, Singleton}
import models.{blockchain, blockchainTransaction}
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}
import views.companion.blockchain.SetSellerFeedback
import views.companion.master

import scala.concurrent.ExecutionContext

@Singleton
class SetSellerFeedbackController @Inject()(messagesControllerComponents: MessagesControllerComponents, transaction: utilities.Transaction, withTraderLoginAction: WithTraderLoginAction, transactionsSetSellerFeedback: transactions.SetSellerFeedback, blockchainTransactionSetSellerFeedbacks: blockchainTransaction.SetSellerFeedbacks, blockchainTraderFeedbackHistories: blockchain.TraderFeedbackHistories, withUsernameToken: WithUsernameToken)(implicit exec: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private implicit val module: String = constants.Module.CONTROLLERS_SET_SELLER_FEEDBACK

  def setSellerFeedbackForm(buyerAddress: String, pegHash: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.setSellerFeedback(master.SetSellerFeedback.form, buyerAddress, pegHash))
  }

  def setSellerFeedback(): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      master.SetSellerFeedback.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.setSellerFeedback(formWithErrors, formWithErrors.data(constants.Form.BUYER_ADDRESS), formWithErrors.data(constants.Form.PEG_HASH)))
        },
        setSellerFeedbackData => {
          try {
            transaction.process[blockchainTransaction.SetSellerFeedback, transactionsSetSellerFeedback.Request](
              entity = blockchainTransaction.SetSellerFeedback(from = loginState.address, to = setSellerFeedbackData.buyerAddress, pegHash = setSellerFeedbackData.pegHash, rating = setSellerFeedbackData.rating,gas=setSellerFeedbackData.gas, status = null, txHash = null, ticketID = "", code = null, mode = transactionMode),
              blockchainTransactionCreate = blockchainTransactionSetSellerFeedbacks.Service.create,
              request = transactionsSetSellerFeedback.Request(transactionsSetSellerFeedback.BaseRequest(from = loginState.address), to = setSellerFeedbackData.buyerAddress, password = setSellerFeedbackData.password, pegHash = setSellerFeedbackData.pegHash,gas=setSellerFeedbackData.gas.toString, rating = setSellerFeedbackData.rating.toString, mode = transactionMode),
              action = transactionsSetSellerFeedback.Service.post,
              onSuccess = blockchainTransactionSetSellerFeedbacks.Utility.onSuccess,
              onFailure = blockchainTransactionSetSellerFeedbacks.Utility.onFailure,
              updateTransactionHash = blockchainTransactionSetSellerFeedbacks.Service.updateTransactionHash
            )
            withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.SELLER_FEEDBACK_SET)))
          }
          catch {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
            case blockChainException: BlockChainException => InternalServerError(views.html.index(failures = Seq(blockChainException.failure)))
          }
        }
      )
  }

  def sellerFeedbackList: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        withUsernameToken.Ok(views.html.component.master.setSellerFeedbackList(blockchainTraderFeedbackHistories.Service.getNullRatingsForSellerFeedback(loginState.address)))
      } catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def blockchainSetSellerFeedbackForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.blockchain.setSellerFeedback(SetSellerFeedback.form))
  }

  def blockchainSetSellerFeedback: Action[AnyContent] = Action { implicit request =>
    SetSellerFeedback.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.component.blockchain.setSellerFeedback(formWithErrors))
      },
      setSellerFeedbackData => {
        try {
          transactionsSetSellerFeedback.Service.post(transactionsSetSellerFeedback.Request(transactionsSetSellerFeedback.BaseRequest(from = setSellerFeedbackData.from), to = setSellerFeedbackData.to, password = setSellerFeedbackData.password, pegHash = setSellerFeedbackData.pegHash, rating = setSellerFeedbackData.rating.toString,gas=setSellerFeedbackData.gas.toString, mode = setSellerFeedbackData.mode))
          Ok(views.html.index(successes = Seq(constants.Response.SELLER_FEEDBACK_SET)))
        }
        catch {
          case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          case blockChainException: BlockChainException => InternalServerError(views.html.index(failures = Seq(blockChainException.failure)))
        }
      }
    )
  }
}
