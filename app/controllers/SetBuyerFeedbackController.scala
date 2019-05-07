package controllers

import controllers.actions.{WithLoginAction, WithTraderLoginAction}
import exceptions.{BaseException, BlockChainException}
import javax.inject.{Inject, Singleton}
import models.{blockchainTransaction, master}
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

@Singleton
class SetBuyerFeedbackController @Inject()(messagesControllerComponents: MessagesControllerComponents, masterAccounts: master.Accounts, withTraderLoginAction: WithTraderLoginAction, withLoginAction: WithLoginAction, transactionsSetBuyerFeedback: transactions.SetBuyerFeedback, blockchainTransactionSetBuyerFeedbacks: blockchainTransaction.SetBuyerFeedbacks)(implicit exec: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private val kafkaEnabled = configuration.get[Boolean]("blockchain.kafka.enabled")

  def setBuyerFeedbackForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.setBuyerFeedback(views.companion.master.SetBuyerFeedback.form))
  }

  def setBuyerFeedback: Action[AnyContent] = withTraderLoginAction.authenticated { username =>
    implicit request =>
      views.companion.master.SetBuyerFeedback.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.setBuyerFeedback(formWithErrors))
        },
        setBuyerFeedbackData => {
          try {
            val toAddress = masterAccounts.Service.getAddress(setBuyerFeedbackData.accountID)
            val ticketID: String = if (kafkaEnabled) transactionsSetBuyerFeedback.Service.kafkaPost(transactionsSetBuyerFeedback.Request(from = username, to = toAddress, password = setBuyerFeedbackData.password, pegHash = setBuyerFeedbackData.pegHash, rating = setBuyerFeedbackData.rating, gas = setBuyerFeedbackData.gas)).ticketID else Random.nextString(32)
            blockchainTransactionSetBuyerFeedbacks.Service.addSetBuyerFeedback(from = username, to = toAddress, pegHash = setBuyerFeedbackData.pegHash, rating = setBuyerFeedbackData.rating, gas = setBuyerFeedbackData.gas, null, null, ticketID = ticketID, null)
            if(!kafkaEnabled){
              Future{
                try {
                  blockchainTransactionSetBuyerFeedbacks.Utility.onSuccess(ticketID, transactionsSetBuyerFeedback.Service.post(transactionsSetBuyerFeedback.Request(from = username, to = toAddress, password = setBuyerFeedbackData.password,  pegHash = setBuyerFeedbackData.pegHash, rating = setBuyerFeedbackData.rating, gas = setBuyerFeedbackData.gas)))
                } catch {
                  case baseException: BaseException => logger.error(constants.Error.BASE_EXCEPTION, baseException)
                    blockchainTransactionSetBuyerFeedbacks.Utility.onFailure(ticketID, baseException.message)
                  case blockChainException: BlockChainException => logger.error(blockChainException.message, blockChainException)
                    blockchainTransactionSetBuyerFeedbacks.Utility.onFailure(ticketID, blockChainException.message)
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
            val response = transactionsSetBuyerFeedback.Service.kafkaPost(transactionsSetBuyerFeedback.Request(from = setBuyerFeedbackData.from, to = setBuyerFeedbackData.to, password = setBuyerFeedbackData.password, pegHash = setBuyerFeedbackData.pegHash, rating = setBuyerFeedbackData.rating, gas = setBuyerFeedbackData.gas))
            blockchainTransactionSetBuyerFeedbacks.Service.addSetBuyerFeedback(from = setBuyerFeedbackData.from, to = setBuyerFeedbackData.to, pegHash = setBuyerFeedbackData.pegHash, rating = setBuyerFeedbackData.rating, gas = setBuyerFeedbackData.gas, null, null, ticketID = response.ticketID, null)
            Ok(views.html.index(success = response.ticketID))
          } else {
            val response = transactionsSetBuyerFeedback.Service.post(transactionsSetBuyerFeedback.Request(from = setBuyerFeedbackData.from, to = setBuyerFeedbackData.to, password = setBuyerFeedbackData.password, pegHash = setBuyerFeedbackData.pegHash, rating = setBuyerFeedbackData.rating, gas = setBuyerFeedbackData.gas))
            blockchainTransactionSetBuyerFeedbacks.Service.addSetBuyerFeedback(from = setBuyerFeedbackData.from, to = setBuyerFeedbackData.to, pegHash = setBuyerFeedbackData.pegHash, rating = setBuyerFeedbackData.rating, gas = setBuyerFeedbackData.gas, null, txHash = Option(response.TxHash), ticketID = Random.nextString(32), null)
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
