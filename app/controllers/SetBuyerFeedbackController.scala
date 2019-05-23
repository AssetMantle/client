package controllers

import controllers.actions.WithTraderLoginAction
import exceptions.{BaseException, BlockChainException}
import javax.inject.{Inject, Singleton}
import models.blockchainTransaction.SetBuyerFeedbacks
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}
import views.companion.blockchain.SetBuyerFeedback
import views.companion.master

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

@Singleton
class SetBuyerFeedbackController @Inject()(messagesControllerComponents: MessagesControllerComponents, withTraderLoginAction: WithTraderLoginAction, transactionsSetBuyerFeedback: transactions.SetBuyerFeedback, setBuyerFeedbacks: SetBuyerFeedbacks)(implicit exec: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private val kafkaEnabled = configuration.get[Boolean]("blockchain.kafka.enabled")

  def setBuyerFeedbackForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.setBuyerFeedback(master.SetBuyerFeedback.form))
  }

  def setBuyerFeedback: Action[AnyContent] = withTraderLoginAction.authenticated { username =>
    implicit request =>
      master.SetBuyerFeedback.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.setBuyerFeedback(formWithErrors))
        },
        setBuyerFeedbackData => {
          val ticketID = if (kafkaEnabled) transactionsSetBuyerFeedback.Service.kafkaPost(transactionsSetBuyerFeedback.Request(from = username, to = setBuyerFeedbackData.sellerAddress, password = setBuyerFeedbackData.password, pegHash = setBuyerFeedbackData.pegHash, rating = setBuyerFeedbackData.rating, gas = setBuyerFeedbackData.gas)).ticketID else Random.nextString(32)
          setBuyerFeedbacks.Service.addSetBuyerFeedback(from = username, to = setBuyerFeedbackData.sellerAddress, pegHash = setBuyerFeedbackData.pegHash, rating = setBuyerFeedbackData.rating, gas = setBuyerFeedbackData.gas, null, null, ticketID = ticketID, null)
          try {
            if (!kafkaEnabled) {
              Future {
                try {
                  setBuyerFeedbacks.Utility.onSuccess(ticketID, transactionsSetBuyerFeedback.Service.post(transactionsSetBuyerFeedback.Request(from = username, to = setBuyerFeedbackData.sellerAddress, password = setBuyerFeedbackData.password, pegHash = setBuyerFeedbackData.pegHash, rating = setBuyerFeedbackData.rating, gas = setBuyerFeedbackData.gas)))
                } catch {
                  case baseException: BaseException => logger.error(constants.Response.BASE_EXCEPTION.message, baseException)
                  case blockChainException: BlockChainException => logger.error(blockChainException.failure.message, blockChainException)
                    setBuyerFeedbacks.Utility.onFailure(ticketID, blockChainException.failure.message)
                }
              }
            }
            Ok(views.html.index(successes = Seq(new Success(ticketID))))
          }
          catch {
            case baseException: BaseException => Ok(views.html.index(failures = Seq(baseException.failure)))
            case blockChainException: BlockChainException => Ok(views.html.index(failures = Seq(blockChainException.failure)))

          }
        }
      )
  }

  def blockchainSetBuyerFeedbackForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.blockchain.setBuyerFeedback(SetBuyerFeedback.form))
  }

  def blockchainSetBuyerFeedback: Action[AnyContent] = Action { implicit request =>
    SetBuyerFeedback.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.component.blockchain.setBuyerFeedback(formWithErrors))
      },
      setBuyerFeedbackData => {
        try {
          if (kafkaEnabled) {
            Ok(views.html.index(successes = transactionsSetBuyerFeedback.Service.kafkaPost(transactionsSetBuyerFeedback.Request(from = setBuyerFeedbackData.from, to = setBuyerFeedbackData.to, password = setBuyerFeedbackData.password, pegHash = setBuyerFeedbackData.pegHash, rating = setBuyerFeedbackData.rating, gas = setBuyerFeedbackData.gas)).ticketID))
          } else {
            Ok(views.html.index(successes = transactionsSetBuyerFeedback.Service.post(transactionsSetBuyerFeedback.Request(from = setBuyerFeedbackData.from, to = setBuyerFeedbackData.to, password = setBuyerFeedbackData.password, pegHash = setBuyerFeedbackData.pegHash, rating = setBuyerFeedbackData.rating, gas = setBuyerFeedbackData.gas)).TxHash))
          }
        }
        catch {
          case baseException: BaseException => Ok(views.html.index(failures = Seq(baseException.failure)))
          case blockChainException: BlockChainException => Ok(views.html.index(failures = Seq(blockChainException.failure)))

        }
      }
    )
  }
}
