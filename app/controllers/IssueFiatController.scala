package controllers

import controllers.actions.WithLoginAction
import exceptions.{BaseException, BlockChainException}
import javax.inject.Inject
import models.blockchainTransaction.IssueFiats
import play.api.Configuration
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import transactions.IssueFiat
import views.companion.blockchain.IssueFiat
import views.companion.master

import scala.concurrent.ExecutionContext
import scala.util.Random

class IssueFiatController @Inject()(messagesControllerComponents: MessagesControllerComponents, withLoginAction: WithLoginAction, blockchainFiats: models.blockchain.Fiats, blockchainOwners: models.blockchain.Owners, transactionIssueFiat: transactions.IssueFiat, issueFiats: IssueFiats)(implicit exec: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  def issueFiatForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.issueFiat(master.IssueFiat.form))
  }

  def issueFiat: Action[AnyContent] = withLoginAction { implicit request =>
    master.IssueFiat.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.component.master.issueFiat(formWithErrors))
      },
      issueFiatData => {
        try {
          if (configuration.get[Boolean]("blockchain.kafka.enabled")) {
            val response = transactionIssueFiat.Service.kafkaPost( transactionIssueFiat.Request(from = request.session.get(constants.Security.USERNAME).get, to = issueFiatData.to, password = issueFiatData.password, transactionID = issueFiatData.transactionID, transactionAmount= issueFiatData.transactionAmount, gas = issueFiatData.gas))
            issueFiats.Service.addIssueFiatKafka(from = request.session.get(constants.Security.USERNAME).get, to = issueFiatData.to, transactionID = issueFiatData.transactionID, transactionAmount= issueFiatData.transactionAmount, gas = issueFiatData.gas, null, null, ticketID = response.ticketID, null)
            Ok(views.html.index(success = response.ticketID))
          } else {
            val response = transactionIssueFiat.Service.post( transactionIssueFiat.Request(from = request.session.get(constants.Security.USERNAME).get, to = issueFiatData.to, password = issueFiatData.password, transactionID = issueFiatData.transactionID, transactionAmount= issueFiatData.transactionAmount, gas = issueFiatData.gas))
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
    Ok(views.html.component.blockchain.issueFiat(IssueFiat.form))
  }

  def blockchainIssueFiat: Action[AnyContent] = Action { implicit request =>
    IssueFiat.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.component.blockchain.issueFiat(formWithErrors))
      },
      issueFiatData => {
        try {
          if (configuration.get[Boolean]("blockchain.kafka.enabled")) {
            val response = transactionIssueFiat.Service.kafkaPost( transactionIssueFiat.Request(from = issueFiatData.from, to = issueFiatData.to, password = issueFiatData.password, transactionID = issueFiatData.transactionID, transactionAmount= issueFiatData.transactionAmount, gas = issueFiatData.gas))
            issueFiats.Service.addIssueFiatKafka(from = issueFiatData.from, to = issueFiatData.to, transactionID = issueFiatData.transactionID, transactionAmount= issueFiatData.transactionAmount, gas = issueFiatData.gas, null, null, ticketID = response.ticketID, null)
            Ok(views.html.index(success = response.ticketID))
          } else {
            val response = transactionIssueFiat.Service.post( transactionIssueFiat.Request(from = issueFiatData.from, to = issueFiatData.to, password = issueFiatData.password, transactionID = issueFiatData.transactionID, transactionAmount= issueFiatData.transactionAmount, gas = issueFiatData.gas))
            issueFiats.Service.addIssueFiat(from = issueFiatData.from, to = issueFiatData.to, transactionID = issueFiatData.transactionID, transactionAmount= issueFiatData.transactionAmount, gas = issueFiatData.gas, null, txHash = Option(response.TxHash), ticketID = (Random.nextInt(899999999) + 100000000).toString, null)
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
