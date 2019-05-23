package controllers

import controllers.actions.{WithLoginAction, WithTraderLoginAction}
import exceptions.{BaseException, BlockChainException}
import javax.inject.{Inject, Singleton}
import models.blockchainTransaction
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}
import views.companion.blockchain.SetSellerFeedback
import views.companion.master

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

@Singleton
class SetSellerFeedbackController @Inject()(messagesControllerComponents: MessagesControllerComponents, withTraderLoginAction: WithTraderLoginAction, withLoginAction: WithLoginAction, transactionsSetSellerFeedback: transactions.SetSellerFeedback, blockchainTransactionSetSellerFeedbacks: blockchainTransaction.SetSellerFeedbacks)(implicit exec: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private val kafkaEnabled = configuration.get[Boolean]("blockchain.kafka.enabled")

  def setSellerFeedbackForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.setSellerFeedback(master.SetSellerFeedback.form))
  }

  def setSellerFeedback: Action[AnyContent] = withTraderLoginAction.authenticated { username =>
    implicit request =>
      master.SetSellerFeedback.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.setSellerFeedback(formWithErrors))
        },
        setSellerFeedbackData => {
          try {
            val ticketID = if (kafkaEnabled) transactionsSetSellerFeedback.Service.kafkaPost(transactionsSetSellerFeedback.Request(from = username, to = setSellerFeedbackData.to, password = setSellerFeedbackData.password, pegHash = setSellerFeedbackData.pegHash, rating = setSellerFeedbackData.rating, gas = setSellerFeedbackData.gas)).ticketID else Random.nextString(32)
            blockchainTransactionSetSellerFeedbacks.Service.addSetSellerFeedback(from = username, to = setSellerFeedbackData.to, pegHash = setSellerFeedbackData.pegHash, rating = setSellerFeedbackData.rating, gas = setSellerFeedbackData.gas, null, null, ticketID = ticketID, null)

            if (!kafkaEnabled) {
              Future {
                try {
                  blockchainTransactionSetSellerFeedbacks.Utility.onSuccess(ticketID, transactionsSetSellerFeedback.Service.post(transactionsSetSellerFeedback.Request(from = username, to = setSellerFeedbackData.to, password = setSellerFeedbackData.password, pegHash = setSellerFeedbackData.pegHash, rating = setSellerFeedbackData.rating, gas = setSellerFeedbackData.gas)))
                } catch {
                  case baseException: BaseException => logger.error(constants.Response.BASE_EXCEPTION.message, baseException)
                  case blockChainException: BlockChainException => logger.error(blockChainException.failure.message, blockChainException)
                    blockchainTransactionSetSellerFeedbacks.Utility.onFailure(ticketID, blockChainException.failure.message)
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
          if (kafkaEnabled) {
            Ok(views.html.index(successes = transactionsSetSellerFeedback.Service.kafkaPost(transactionsSetSellerFeedback.Request(from = setSellerFeedbackData.from, to = setSellerFeedbackData.to, password = setSellerFeedbackData.password, pegHash = setSellerFeedbackData.pegHash, rating = setSellerFeedbackData.rating, gas = setSellerFeedbackData.gas)).ticketID))
          } else {
            Ok(views.html.index(successes = transactionsSetSellerFeedback.Service.post(transactionsSetSellerFeedback.Request(from = setSellerFeedbackData.from, to = setSellerFeedbackData.to, password = setSellerFeedbackData.password, pegHash = setSellerFeedbackData.pegHash, rating = setSellerFeedbackData.rating, gas = setSellerFeedbackData.gas)).TxHash))
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
