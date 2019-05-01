package controllers

import controllers.actions.{WithLoginAction, WithTraderLoginAction}
import exceptions.{BaseException, BlockChainException}
import javax.inject.{Inject, Singleton}
import models.blockchainTransaction.RedeemFiats
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}
import views.companion.blockchain.RedeemFiat
import views.companion.master

import scala.concurrent.ExecutionContext
import scala.util.Random

@Singleton
class RedeemFiatController @Inject()(messagesControllerComponents: MessagesControllerComponents, withLoginAction: WithLoginAction, withTraderLoginAction: WithTraderLoginAction, transactionsRedeemFiat: transactions.RedeemFiat, redeemFiats: RedeemFiats)(implicit exec: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private val kafkaEnabled = configuration.get[Boolean]("blockchain.kafka.enabled")

  def redeemFiatForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.redeemFiat(master.RedeemFiat.form))
  }

  def redeemFiat: Action[AnyContent] = withTraderLoginAction.authenticated { username =>
    implicit request =>
      master.RedeemFiat.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.redeemFiat(formWithErrors))
        },
        redeemFiatData => {
          try {
            if (kafkaEnabled) {
              val response = transactionsRedeemFiat.Service.kafkaPost(transactionsRedeemFiat.Request(from = username, to = redeemFiatData.to, password = redeemFiatData.password, redeemAmount = redeemFiatData.redeemAmount, gas = redeemFiatData.gas))
              redeemFiats.Service.addRedeemFiatKafka(from = username, to = redeemFiatData.to, redeemAmount = redeemFiatData.redeemAmount, gas = redeemFiatData.gas, null, null, ticketID = response.ticketID, null)
              Ok(views.html.index(success = response.ticketID))
            } else {
              val response = transactionsRedeemFiat.Service.post(transactionsRedeemFiat.Request(from = username, to = redeemFiatData.to, password = redeemFiatData.password, redeemAmount = redeemFiatData.redeemAmount, gas = redeemFiatData.gas))
              redeemFiats.Service.addRedeemFiat(from = username, to = redeemFiatData.to, redeemAmount = redeemFiatData.redeemAmount, gas = redeemFiatData.gas, null, txHash = Option(response.TxHash), ticketID = Random.nextString(32), null)
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

  def blockchainRedeemFiatForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.blockchain.redeemFiat(RedeemFiat.form))
  }

  def blockchainRedeemFiat: Action[AnyContent] = Action { implicit request =>
    RedeemFiat.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.component.blockchain.redeemFiat(formWithErrors))
      },
      redeemFiatData => {
        try {
          if (kafkaEnabled) {
            val response = transactionsRedeemFiat.Service.kafkaPost(transactionsRedeemFiat.Request(from = redeemFiatData.from, to = redeemFiatData.to, password = redeemFiatData.password, redeemAmount = redeemFiatData.redeemAmount, gas = redeemFiatData.gas))
            redeemFiats.Service.addRedeemFiatKafka(from = redeemFiatData.from, to = redeemFiatData.to, redeemAmount = redeemFiatData.redeemAmount, gas = redeemFiatData.gas, null, null, ticketID = response.ticketID, null)
            Ok(views.html.index(success = response.ticketID))
          } else {
            val response = transactionsRedeemFiat.Service.post(transactionsRedeemFiat.Request(from = redeemFiatData.from, to = redeemFiatData.to, password = redeemFiatData.password, redeemAmount = redeemFiatData.redeemAmount, gas = redeemFiatData.gas))
            redeemFiats.Service.addRedeemFiat(from = redeemFiatData.from, to = redeemFiatData.to, redeemAmount = redeemFiatData.redeemAmount, gas = redeemFiatData.gas, null, txHash = Option(response.TxHash), ticketID = Random.nextString(32), null)
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
