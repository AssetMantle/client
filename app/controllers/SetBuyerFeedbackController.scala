package controllers

import controllers.actions.WithTraderLoginAction
import exceptions.{BaseException, BlockChainException}
import javax.inject.{Inject, Singleton}
import models.blockchainTransaction
import models.blockchain
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

@Singleton
class SetBuyerFeedbackController @Inject()(messagesControllerComponents: MessagesControllerComponents, withTraderLoginAction: WithTraderLoginAction, transactionsSetBuyerFeedback: transactions.SetBuyerFeedback, blockchainTransactionSetBuyerFeedbacks: blockchainTransaction.SetBuyerFeedbacks, blockchainTraderFeedbackHistories: blockchain.TraderFeedbackHistories)(implicit exec: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private val kafkaEnabled = configuration.get[Boolean]("blockchain.kafka.enabled")

  def setBuyerFeedbackForm(sellerAddress:String, pegHash:String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.setBuyerFeedback(views.companion.master.SetBuyerFeedback.form, sellerAddress, pegHash))
  }

  def setBuyerFeedback: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.SetBuyerFeedback.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.setBuyerFeedback(formWithErrors, formWithErrors.data(constants.Form.SELLER_ADDRESS), formWithErrors.data(constants.Form.PEG_HASH)))
        },
        setBuyerFeedbackData => {

          val ticketID = if (kafkaEnabled) transactionsSetBuyerFeedback.Service.kafkaPost(transactionsSetBuyerFeedback.Request(from = loginState.username, to = setBuyerFeedbackData.sellerAddress, password = setBuyerFeedbackData.password, pegHash = setBuyerFeedbackData.pegHash, rating = setBuyerFeedbackData.rating, gas = setBuyerFeedbackData.gas)).ticketID else Random.nextString(32)
          blockchainTransactionSetBuyerFeedbacks.Service.create(from = loginState.username, to = setBuyerFeedbackData.sellerAddress, pegHash = setBuyerFeedbackData.pegHash, rating = setBuyerFeedbackData.rating, gas = setBuyerFeedbackData.gas, null, null, ticketID = ticketID, null)
          try {
            if (!kafkaEnabled) {
              Future {
                try {
                  blockchainTransactionSetBuyerFeedbacks.Utility.onSuccess(ticketID, transactionsSetBuyerFeedback.Service.post(transactionsSetBuyerFeedback.Request(from = loginState.username, to = setBuyerFeedbackData.sellerAddress, password = setBuyerFeedbackData.password, pegHash = setBuyerFeedbackData.pegHash, rating = setBuyerFeedbackData.rating, gas = setBuyerFeedbackData.gas)))
                } catch {
                  case baseException: BaseException => logger.error(baseException.failure.message, baseException)
                  case blockChainException: BlockChainException => logger.error(blockChainException.failure.message, blockChainException)
                    blockchainTransactionSetBuyerFeedbacks.Utility.onFailure(ticketID, blockChainException.failure.message)
                }
              }
            }
            Ok(views.html.index(successes = Seq(constants.Response.BUYER_FEEDBACK_SET)))
          }
          catch {
            case baseException: BaseException => Ok(views.html.index(failures = Seq(baseException.failure)))
            case blockChainException: BlockChainException => Ok(views.html.index(failures = Seq(blockChainException.failure)))

          }
        }
      )
  }

  def buyerFeedbackList: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        Ok(views.html.component.master.setBuyerFeedbackList(blockchainTraderFeedbackHistories.Service.getNullRatingsForBuyerFeedback(loginState.address)))
      } catch {
        case baseException: BaseException => Ok(views.html.index(failures = Seq(baseException.failure)))
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
          if (kafkaEnabled) {
            transactionsSetBuyerFeedback.Service.kafkaPost(transactionsSetBuyerFeedback.Request(from = setBuyerFeedbackData.from, to = setBuyerFeedbackData.to, password = setBuyerFeedbackData.password, pegHash = setBuyerFeedbackData.pegHash, rating = setBuyerFeedbackData.rating, gas = setBuyerFeedbackData.gas))
          } else {
            transactionsSetBuyerFeedback.Service.post(transactionsSetBuyerFeedback.Request(from = setBuyerFeedbackData.from, to = setBuyerFeedbackData.to, password = setBuyerFeedbackData.password, pegHash = setBuyerFeedbackData.pegHash, rating = setBuyerFeedbackData.rating, gas = setBuyerFeedbackData.gas))
          }
          Ok(views.html.index(successes = Seq(constants.Response.BUYER_FEEDBACK_SET)))
        }
        catch {
          case baseException: BaseException => Ok(views.html.index(failures = Seq(baseException.failure)))
          case blockChainException: BlockChainException => Ok(views.html.index(failures = Seq(blockChainException.failure)))

        }
      }
    )
  }
}
