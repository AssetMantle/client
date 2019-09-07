package controllers

import controllers.actions.WithTraderLoginAction
import controllers.results.WithUsernameToken
import exceptions.{BaseException, BlockChainException}
import javax.inject.{Inject, Singleton}
import models.{blockchain, blockchainTransaction}
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}

import scala.concurrent.ExecutionContext

@Singleton
class SetBuyerFeedbackController @Inject()(messagesControllerComponents: MessagesControllerComponents, transaction: utilities.Transaction, withTraderLoginAction: WithTraderLoginAction, transactionsSetBuyerFeedback: transactions.SetBuyerFeedback, blockchainTransactionSetBuyerFeedbacks: blockchainTransaction.SetBuyerFeedbacks, blockchainTraderFeedbackHistories: blockchain.TraderFeedbackHistories, withUsernameToken: WithUsernameToken)(implicit exec: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private implicit val module:String= constants.Module.CONTROLLERS_SET_BUYER_FEEDBACK

  def setBuyerFeedbackForm(sellerAddress: String, pegHash: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.setBuyerFeedback(views.companion.master.SetBuyerFeedback.form, sellerAddress, pegHash))
  }

  def setBuyerFeedback(): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.SetBuyerFeedback.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.setBuyerFeedback(formWithErrors, formWithErrors.data(constants.Form.SELLER_ADDRESS), formWithErrors.data(constants.Form.PEG_HASH)))
        },
        setBuyerFeedbackData => {
          try {
            transaction.process[blockchainTransaction.SetBuyerFeedback, transactionsSetBuyerFeedback.Request](
              entity = blockchainTransaction.SetBuyerFeedback(from = loginState.address, to = setBuyerFeedbackData.sellerAddress, pegHash = setBuyerFeedbackData.pegHash, rating = setBuyerFeedbackData.rating,gas=setBuyerFeedbackData.gas, status = null, txHash = null, ticketID = "", code = null, mode = transactionMode),
              blockchainTransactionCreate = blockchainTransactionSetBuyerFeedbacks.Service.create,
              request = transactionsSetBuyerFeedback.Request(transactionsSetBuyerFeedback.BaseRequest(from = loginState.address), to = setBuyerFeedbackData.sellerAddress, password = setBuyerFeedbackData.password, pegHash = setBuyerFeedbackData.pegHash, rating = setBuyerFeedbackData.rating.toString,gas=setBuyerFeedbackData.gas.toString, mode = transactionMode),
              action = transactionsSetBuyerFeedback.Service.post,
              onSuccess = blockchainTransactionSetBuyerFeedbacks.Utility.onSuccess,
              onFailure = blockchainTransactionSetBuyerFeedbacks.Utility.onFailure,
              updateTransactionHash = blockchainTransactionSetBuyerFeedbacks.Service.updateTransactionHash
            )
            withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.BUYER_FEEDBACK_SET)))
          }
          catch {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
            case blockChainException: BlockChainException => InternalServerError(views.html.index(failures = Seq(blockChainException.failure)))
          }
        }
      )
  }

  def buyerFeedbackList: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        val a=blockchainTraderFeedbackHistories.Service.getNullRatingsForBuyerFeedback(loginState.address)
        withUsernameToken.Ok(views.html.component.master.setBuyerFeedbackList(a))
      } catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def blockchainSetBuyerFeedbackForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.blockchain.setBuyerFeedback(views.companion.blockchain.SetBuyerFeedback.form))
  }

  def blockchainSetBuyerFeedback: Action[AnyContent] = Action { implicit request =>
    views.companion.blockchain.SetBuyerFeedback.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.component.blockchain.setBuyerFeedback(formWithErrors))
      },
      setBuyerFeedbackData => {
        try {
          transactionsSetBuyerFeedback.Service.post(transactionsSetBuyerFeedback.Request(transactionsSetBuyerFeedback.BaseRequest(from = setBuyerFeedbackData.from), to = setBuyerFeedbackData.to, password = setBuyerFeedbackData.password, pegHash = setBuyerFeedbackData.pegHash, rating = setBuyerFeedbackData.rating.toString,gas=setBuyerFeedbackData.gas.toString, mode = setBuyerFeedbackData.mode))
          Ok(views.html.index(successes = Seq(constants.Response.BUYER_FEEDBACK_SET)))
        }
        catch {
          case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          case blockChainException: BlockChainException => InternalServerError(views.html.index(failures = Seq(blockChainException.failure)))

        }
      }
    )
  }
}
