package controllers

import controllers.actions.{WithLoginAction, WithTraderLoginAction}
import exceptions.{BaseException, BlockChainException}
import javax.inject.{Inject, Singleton}
import models.blockchainTransaction
import models.master
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

@Singleton
class SetSellerFeedbackController @Inject()(messagesControllerComponents: MessagesControllerComponents, masterAccounts: master.Accounts, withTraderLoginAction: WithTraderLoginAction, withLoginAction: WithLoginAction, transactionsSetSellerFeedback: transactions.SetSellerFeedback, blockchainTransactionSetSellerFeedbacks: blockchainTransaction.SetSellerFeedbacks)(implicit exec: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private val kafkaEnabled = configuration.get[Boolean]("blockchain.kafka.enabled")

  def setSellerFeedbackForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.setSellerFeedback(views.companion.master.SetSellerFeedback.form))
  }

  def setSellerFeedback: Action[AnyContent] = withTraderLoginAction.authenticated { username =>
    implicit request =>
      views.companion.master.SetSellerFeedback.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.setSellerFeedback(formWithErrors))
        },
        setSellerFeedbackData => {
          try {
            val toAddress = masterAccounts.Service.getAddress(setSellerFeedbackData.accountID)
            val ticketID: String = if (kafkaEnabled) transactionsSetSellerFeedback.Service.kafkaPost(transactionsSetSellerFeedback.Request(from = username, to = toAddress, password = setSellerFeedbackData.password, pegHash = setSellerFeedbackData.pegHash, rating = setSellerFeedbackData.rating, gas = setSellerFeedbackData.gas)).ticketID else Random.nextString(32)
            blockchainTransactionSetSellerFeedbacks.Service.addSetSellerFeedback(from = username, to = toAddress, pegHash = setSellerFeedbackData.pegHash, rating = setSellerFeedbackData.rating, gas = setSellerFeedbackData.gas, null, null, ticketID = ticketID, null)
            if(!kafkaEnabled){
              Future{
                try {
                  blockchainTransactionSetSellerFeedbacks.Utility.onSuccess(ticketID, transactionsSetSellerFeedback.Service.post(transactionsSetSellerFeedback.Request(from = username, to = toAddress, password = setSellerFeedbackData.password,  pegHash = setSellerFeedbackData.pegHash, rating = setSellerFeedbackData.rating, gas = setSellerFeedbackData.gas)))
                } catch {
                  case baseException: BaseException => logger.error(constants.Error.BASE_EXCEPTION, baseException)
                    blockchainTransactionSetSellerFeedbacks.Utility.onFailure(ticketID, baseException.message)
                  case blockChainException: BlockChainException => logger.error(blockChainException.message, blockChainException)
                    blockchainTransactionSetSellerFeedbacks.Utility.onFailure(ticketID, blockChainException.message)
                }
              }
            }
            Ok(views.html.index(success = ticketID))
          }
          catch {
            case baseException: BaseException => Ok(views.html.index(failure = Messages(baseException.message)))
            case blockChainException: BlockChainException => Ok(views.html.index(failure = blockChainException.message))
          }
        }
      )
  }

  def blockchainSetSellerFeedbackForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.blockchain.setSellerFeedback(views.companion.blockchain.SetSellerFeedback.form))
  }

  def blockchainSetSellerFeedback: Action[AnyContent] = Action { implicit request =>
    views.companion.blockchain.SetSellerFeedback.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.component.blockchain.setSellerFeedback(formWithErrors))
      },
      setSellerFeedbackData => {
        try {
          if (kafkaEnabled) {
            val response = transactionsSetSellerFeedback.Service.kafkaPost(transactionsSetSellerFeedback.Request(from = setSellerFeedbackData.from, to = setSellerFeedbackData.to, password = setSellerFeedbackData.password, pegHash = setSellerFeedbackData.pegHash, rating = setSellerFeedbackData.rating, gas = setSellerFeedbackData.gas))
            blockchainTransactionSetSellerFeedbacks.Service.addSetSellerFeedback(from = setSellerFeedbackData.from, to = setSellerFeedbackData.to, pegHash = setSellerFeedbackData.pegHash, rating = setSellerFeedbackData.rating, gas = setSellerFeedbackData.gas, null, null, ticketID = response.ticketID, null)
            Ok(views.html.index(success = response.ticketID))
          } else {
            val response = transactionsSetSellerFeedback.Service.post(transactionsSetSellerFeedback.Request(from = setSellerFeedbackData.from, to = setSellerFeedbackData.to, password = setSellerFeedbackData.password, pegHash = setSellerFeedbackData.pegHash, rating = setSellerFeedbackData.rating, gas = setSellerFeedbackData.gas))
            blockchainTransactionSetSellerFeedbacks.Service.addSetSellerFeedback(from = setSellerFeedbackData.from, to = setSellerFeedbackData.to, pegHash = setSellerFeedbackData.pegHash, rating = setSellerFeedbackData.rating, gas = setSellerFeedbackData.gas, null, txHash = Option(response.TxHash), ticketID = Random.nextString(32), null)
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
