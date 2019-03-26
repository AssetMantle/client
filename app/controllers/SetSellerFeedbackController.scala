package controllers

import controllers.actions.WithLoginAction
import exceptions.{BaseException, BlockChainException}
import javax.inject.Inject
import models.blockchainTransaction.SetSellerFeedbacks
import play.api.Configuration
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import views.companion.blockchain.SetSellerFeedback
import views.companion.master

import scala.concurrent.ExecutionContext
import scala.util.Random

class SetSellerFeedbackController @Inject()(messagesControllerComponents: MessagesControllerComponents, withLoginAction: WithLoginAction, transactionSetSellerFeedback: transactions.SetSellerFeedback, setSellerFeedbacks: SetSellerFeedbacks)(implicit exec: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  def setSellerFeedbackForm: Action[AnyContent] = withLoginAction { implicit request =>
    Ok(views.html.component.master.setSellerFeedback(master.SetSellerFeedback.form))
  }

  def setSellerFeedback: Action[AnyContent] = withLoginAction { implicit request =>
    master.SetSellerFeedback.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.component.master.setSellerFeedback(formWithErrors))
      },
      setSellerFeedbackData => {
        try {
          if (configuration.get[Boolean]("blockchain.kafka.enabled")) {
            val response = transactionSetSellerFeedback.Service.kafkaPost( transactionSetSellerFeedback.Request(from = request.session.get(constants.Security.USERNAME).get, to = setSellerFeedbackData.to, password = setSellerFeedbackData.password, pegHash = setSellerFeedbackData.pegHash, rating = setSellerFeedbackData.rating, gas = setSellerFeedbackData.gas))
            setSellerFeedbacks.Service.addSetSellerFeedbackKafka(from = request.session.get(constants.Security.USERNAME).get, to = setSellerFeedbackData.to, pegHash = setSellerFeedbackData.pegHash, rating = setSellerFeedbackData.rating, gas = setSellerFeedbackData.gas, null, null, ticketID = response.ticketID, null)
            Ok(views.html.index(success = response.ticketID))
          } else {
            val response = transactionSetSellerFeedback.Service.post( transactionSetSellerFeedback.Request(from = request.session.get(constants.Security.USERNAME).get, to = setSellerFeedbackData.to, password = setSellerFeedbackData.password, pegHash = setSellerFeedbackData.pegHash, rating = setSellerFeedbackData.rating, gas = setSellerFeedbackData.gas))
            setSellerFeedbacks.Service.addSetSellerFeedback(from = request.session.get(constants.Security.USERNAME).get, to = setSellerFeedbackData.to, pegHash = setSellerFeedbackData.pegHash, rating = setSellerFeedbackData.rating, gas = setSellerFeedbackData.gas, null, txHash = Option(response.TxHash), ticketID = (Random.nextInt(899999999) + 100000000).toString, null)
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
          if (configuration.get[Boolean]("blockchain.kafka.enabled")) {
            val response = transactionSetSellerFeedback.Service.kafkaPost( transactionSetSellerFeedback.Request(from = setSellerFeedbackData.from, to = setSellerFeedbackData.to, password = setSellerFeedbackData.password, pegHash = setSellerFeedbackData.pegHash, rating = setSellerFeedbackData.rating, gas = setSellerFeedbackData.gas))
            setSellerFeedbacks.Service.addSetSellerFeedbackKafka(from = setSellerFeedbackData.from, to = setSellerFeedbackData.to, pegHash = setSellerFeedbackData.pegHash, rating = setSellerFeedbackData.rating, gas = setSellerFeedbackData.gas, null, null, ticketID = response.ticketID, null)
            Ok(views.html.index(success = response.ticketID))
          } else {
            val response = transactionSetSellerFeedback.Service.post( transactionSetSellerFeedback.Request(from = setSellerFeedbackData.from, to = setSellerFeedbackData.to, password = setSellerFeedbackData.password, pegHash = setSellerFeedbackData.pegHash, rating = setSellerFeedbackData.rating, gas = setSellerFeedbackData.gas))
            setSellerFeedbacks.Service.addSetSellerFeedback(from = setSellerFeedbackData.from, to = setSellerFeedbackData.to, pegHash = setSellerFeedbackData.pegHash, rating = setSellerFeedbackData.rating, gas = setSellerFeedbackData.gas, null, txHash = Option(response.TxHash), ticketID = (Random.nextInt(899999999) + 100000000).toString, null)
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
