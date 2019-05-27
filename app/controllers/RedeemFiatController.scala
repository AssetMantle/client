package controllers

import controllers.actions.{WithLoginAction, WithTraderLoginAction}
import exceptions.{BaseException, BlockChainException}
import javax.inject.{Inject, Singleton}
import models.{blockchain, blockchainTransaction}
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}
import views.companion.blockchain.RedeemFiat
import views.companion.master

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

@Singleton
class RedeemFiatController @Inject()(messagesControllerComponents: MessagesControllerComponents, blockchainZones: blockchain.Zones, withLoginAction: WithLoginAction, withTraderLoginAction: WithTraderLoginAction, transactionsRedeemFiat: transactions.RedeemFiat, blockchainTransactionRedeemFiats: blockchainTransaction.RedeemFiats)(implicit exec: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

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
            val toAddress = blockchainZones.Service.getAddress(redeemFiatData.zoneID)
            val ticketID = if (kafkaEnabled) transactionsRedeemFiat.Service.kafkaPost(transactionsRedeemFiat.Request(from = username, to = toAddress, password = redeemFiatData.password, redeemAmount = redeemFiatData.redeemAmount, gas = redeemFiatData.gas)).ticketID else Random.nextString(32)
            blockchainTransactionRedeemFiats.Service.create(from = username, to = toAddress, redeemAmount = redeemFiatData.redeemAmount, gas = redeemFiatData.gas, null, null, ticketID = ticketID, null)
            if (!kafkaEnabled) {
              Future {
                try {
                  blockchainTransactionRedeemFiats.Utility.onSuccess(ticketID, transactionsRedeemFiat.Service.post(transactionsRedeemFiat.Request(from = username, to = toAddress, password = redeemFiatData.password, redeemAmount = redeemFiatData.redeemAmount, gas = redeemFiatData.gas)))
                } catch {
                  case baseException: BaseException => logger.error(constants.Error.BASE_EXCEPTION, baseException)
                  case blockChainException: BlockChainException => logger.error(blockChainException.message, blockChainException)
                    blockchainTransactionRedeemFiats.Utility.onFailure(ticketID, blockChainException.message)
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
            Ok(views.html.index(success = transactionsRedeemFiat.Service.kafkaPost(transactionsRedeemFiat.Request(from = redeemFiatData.from, to = redeemFiatData.to, password = redeemFiatData.password, redeemAmount = redeemFiatData.redeemAmount, gas = redeemFiatData.gas)).ticketID))
          } else {
            Ok(views.html.index(success = transactionsRedeemFiat.Service.post(transactionsRedeemFiat.Request(from = redeemFiatData.from, to = redeemFiatData.to, password = redeemFiatData.password, redeemAmount = redeemFiatData.redeemAmount, gas = redeemFiatData.gas)).TxHash))
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
