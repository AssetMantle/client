package controllers

import controllers.actions.{WithLoginAction, WithTraderLoginAction}
import exceptions.{BaseException, BlockChainException}
import javax.inject.Inject
import models.blockchainTransaction.SetBuyerFeedbacks
import play.api.Configuration
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import views.companion.blockchain.SetBuyerFeedback
import views.companion.master

import scala.concurrent.ExecutionContext
import scala.util.Random

class SetBuyerFeedbackController @Inject()(messagesControllerComponents: MessagesControllerComponents, withTraderLoginAction: WithTraderLoginAction, withLoginAction: WithLoginAction, transactionsSetBuyerFeedback: transactions.SetBuyerFeedback, setBuyerFeedbacks: SetBuyerFeedbacks)(implicit exec: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private val kafkaEnabled = configuration.get[Boolean]("blockchain.kafka.enabled")

  def setBuyerFeedbackForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.setBuyerFeedback(master.SetBuyerFeedback.form))
  }

  def setBuyerFeedback: Action[AnyContent] = withTraderLoginAction { implicit request =>
    master.SetBuyerFeedback.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.component.master.setBuyerFeedback(formWithErrors))
      },
      setBuyerFeedbackData => {
        try {
          if (kafkaEnabled) {
            val response = transactionsSetBuyerFeedback.Service.kafkaPost( transactionsSetBuyerFeedback.Request(from = request.session.get(constants.Security.USERNAME).get, to = setBuyerFeedbackData.to, password = setBuyerFeedbackData.password, pegHash = setBuyerFeedbackData.pegHash, rating = setBuyerFeedbackData.rating, gas = setBuyerFeedbackData.gas))
            setBuyerFeedbacks.Service.addSetBuyerFeedbackKafka(from = request.session.get(constants.Security.USERNAME).get, to = setBuyerFeedbackData.to, pegHash = setBuyerFeedbackData.pegHash, rating = setBuyerFeedbackData.rating, gas = setBuyerFeedbackData.gas, null, null, ticketID = response.ticketID, null)
            Ok(views.html.index(success = response.ticketID))
          } else {
            val response = transactionsSetBuyerFeedback.Service.post( transactionsSetBuyerFeedback.Request(from = request.session.get(constants.Security.USERNAME).get, to = setBuyerFeedbackData.to, password = setBuyerFeedbackData.password, pegHash = setBuyerFeedbackData.pegHash, rating = setBuyerFeedbackData.rating, gas = setBuyerFeedbackData.gas))
            setBuyerFeedbacks.Service.addSetBuyerFeedback(from = request.session.get(constants.Security.USERNAME).get, to = setBuyerFeedbackData.to, pegHash = setBuyerFeedbackData.pegHash, rating = setBuyerFeedbackData.rating, gas = setBuyerFeedbackData.gas, null, txHash = Option(response.TxHash), ticketID = Random.nextString(32), null)
            Ok(views.html.index(success = response.TxHash))
          }
        }
        catch {
          case baseException: BaseException => Ok(views.html.index(failure = Messages(baseException.message)))
          case blockChainException: BlockChainException => Ok(views.html.index(failure = blockChainException.message))

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
            val response = transactionsSetBuyerFeedback.Service.kafkaPost( transactionsSetBuyerFeedback.Request(from = setBuyerFeedbackData.from, to = setBuyerFeedbackData.to, password = setBuyerFeedbackData.password, pegHash = setBuyerFeedbackData.pegHash, rating = setBuyerFeedbackData.rating, gas = setBuyerFeedbackData.gas))
            setBuyerFeedbacks.Service.addSetBuyerFeedbackKafka(from = setBuyerFeedbackData.from, to = setBuyerFeedbackData.to, pegHash = setBuyerFeedbackData.pegHash, rating = setBuyerFeedbackData.rating, gas = setBuyerFeedbackData.gas, null, null, ticketID = response.ticketID, null)
            Ok(views.html.index(success = response.ticketID))
          } else {
            val response = transactionsSetBuyerFeedback.Service.post( transactionsSetBuyerFeedback.Request(from = setBuyerFeedbackData.from, to = setBuyerFeedbackData.to, password = setBuyerFeedbackData.password, pegHash = setBuyerFeedbackData.pegHash, rating = setBuyerFeedbackData.rating, gas = setBuyerFeedbackData.gas))
            setBuyerFeedbacks.Service.addSetBuyerFeedback(from = setBuyerFeedbackData.from, to = setBuyerFeedbackData.to, pegHash = setBuyerFeedbackData.pegHash, rating = setBuyerFeedbackData.rating, gas = setBuyerFeedbackData.gas, null, txHash = Option(response.TxHash), ticketID = Random.nextString(32), null)
            Ok(views.html.index(success = response.TxHash))
          }
        }
        catch {
          case baseException: BaseException => Ok(views.html.index(failure = Messages(baseException.message)))
          case blockChainException: BlockChainException => Ok(views.html.index(failure = blockChainException.message))

        }
      }
    )
  }
}
