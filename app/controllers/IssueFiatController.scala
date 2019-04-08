package controllers

import controllers.actions.WithTraderLoginAction
import exceptions.{BaseException, BlockChainException}
import javax.inject.Inject
import models.{blockchainTransaction, master}
import play.api.Configuration
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}

import scala.concurrent.ExecutionContext
import scala.util.Random

class IssueFiatController @Inject()(messagesControllerComponents: MessagesControllerComponents, withTraderLoginAction: WithTraderLoginAction, masterAccounts: master.Accounts, blockchainFiats: models.blockchain.Fiats, blockchainOwners: models.blockchain.Owners, transactionsIssueFiat: transactions.IssueFiat, blockchainTransactionIssueFiats: blockchainTransaction.IssueFiats)(implicit exec: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private val kafkaEnabled = configuration.get[Boolean]("blockchain.kafka.enabled")

  def issueFiatForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.issueFiat(views.companion.master.IssueFiat.form))
  }

  def issueFiat: Action[AnyContent] = withTraderLoginAction { implicit request =>
    views.companion.master.IssueFiat.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.component.master.issueFiat(formWithErrors))
      },
      issueFiatData => {
        try {
          if (kafkaEnabled) {
            val response = transactionsIssueFiat.Service.kafkaPost(transactionsIssueFiat.Request(from = request.session.get(constants.Security.USERNAME).get, to = issueFiatData.to, password = issueFiatData.password, transactionID = issueFiatData.transactionID, transactionAmount = issueFiatData.transactionAmount, gas = issueFiatData.gas))
            blockchainTransactionIssueFiats.Service.addIssueFiatKafka(from = request.session.get(constants.Security.USERNAME).get, to = issueFiatData.to, transactionID = issueFiatData.transactionID, transactionAmount = issueFiatData.transactionAmount, gas = issueFiatData.gas, null, null, ticketID = response.ticketID, null)
            Ok(views.html.index(success = response.ticketID))
          } else {
            val response = transactionsIssueFiat.Service.post(transactionsIssueFiat.Request(from = request.session.get(constants.Security.USERNAME).get, to = issueFiatData.to, password = issueFiatData.password, transactionID = issueFiatData.transactionID, transactionAmount = issueFiatData.transactionAmount, gas = issueFiatData.gas))
            blockchainTransactionIssueFiats.Service.addIssueFiat(from = request.session.get(constants.Security.USERNAME).get, to = issueFiatData.to, transactionID = issueFiatData.transactionID, transactionAmount = issueFiatData.transactionAmount, gas = issueFiatData.gas, null, txHash = Option(response.TxHash), ticketID = (Random.nextInt(899999999) + 100000000).toString, null)
            for (tag <- response.Tags) {
              if (tag.Key == constants.Response.KEY_FIAT) {
                blockchainFiats.Service.addFiat(pegHash = tag.Value, transactionID = issueFiatData.transactionID, transactionAmount = issueFiatData.transactionAmount, redeemedAmount = 0)
                blockchainOwners.Service.addOwner(pegHash = tag.Value, ownerAddress = issueFiatData.to, amount = issueFiatData.transactionAmount)
              }
            }
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

  def blockchainIssueFiatForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.blockchain.issueFiat(views.companion.blockchain.IssueFiat.form))
  }

  def blockchainIssueFiat: Action[AnyContent] = Action { implicit request =>
    views.companion.blockchain.IssueFiat.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.component.blockchain.issueFiat(formWithErrors))
      },
      issueFiatData => {
        try {
          if (kafkaEnabled) {
            val response = transactionsIssueFiat.Service.kafkaPost(transactionsIssueFiat.Request(from = issueFiatData.from, to = issueFiatData.to, password = issueFiatData.password, transactionID = issueFiatData.transactionID, transactionAmount = issueFiatData.transactionAmount, gas = issueFiatData.gas))
            blockchainTransactionIssueFiats.Service.addIssueFiatKafka(from = issueFiatData.from, to = issueFiatData.to, transactionID = issueFiatData.transactionID, transactionAmount = issueFiatData.transactionAmount, gas = issueFiatData.gas, null, null, ticketID = response.ticketID, null)
            Ok(views.html.index(success = response.ticketID))
          } else {
            val response = transactionsIssueFiat.Service.post(transactionsIssueFiat.Request(from = issueFiatData.from, to = issueFiatData.to, password = issueFiatData.password, transactionID = issueFiatData.transactionID, transactionAmount = issueFiatData.transactionAmount, gas = issueFiatData.gas))
            blockchainTransactionIssueFiats.Service.addIssueFiat(from = issueFiatData.from, to = issueFiatData.to, transactionID = issueFiatData.transactionID, transactionAmount = issueFiatData.transactionAmount, gas = issueFiatData.gas, null, txHash = Option(response.TxHash), ticketID = (Random.nextInt(899999999) + 100000000).toString, null)
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
