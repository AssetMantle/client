package controllers

import controllers.actions.WithTraderLoginAction
import exceptions.{BaseException, BlockChainException}
import javax.inject.{Inject, Singleton}
import models.{blockchain, blockchainTransaction}
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}
import views.companion.blockchain.RedeemFiat
import views.companion.master

import scala.concurrent.ExecutionContext

@Singleton
class RedeemFiatController @Inject()(messagesControllerComponents: MessagesControllerComponents, transaction: utilities.Transaction, blockchainZones: blockchain.Zones, blockchainACLAccounts: blockchain.ACLAccounts, withTraderLoginAction: WithTraderLoginAction, transactionsRedeemFiat: transactions.RedeemFiat, blockchainTransactionRedeemFiats: blockchainTransaction.RedeemFiats)(implicit exec: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private val kafkaEnabled = configuration.get[Boolean]("blockchain.kafka.enabled")

  def redeemFiatForm(ownerAddress: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.redeemFiat(master.RedeemFiat.form, blockchainACLAccounts.Service.get(ownerAddress).zoneID))
  }

  def redeemFiat: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      master.RedeemFiat.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.redeemFiat(formWithErrors, formWithErrors.data(constants.Form.ZONE_ID)))
        },
        redeemFiatData => {
          try {
            val toAddress = blockchainZones.Service.getAddress(redeemFiatData.zoneID)
            transaction.process[blockchainTransaction.RedeemFiat, transactionsRedeemFiat.Request](
              entity = blockchainTransaction.RedeemFiat(from = loginState.address, to = toAddress, redeemAmount = redeemFiatData.redeemAmount, gas = redeemFiatData.gas, null, null, ticketID = "", mode = transactionMode, null),
              blockchainTransactionCreate = blockchainTransactionRedeemFiats.Service.create,
              request = transactionsRedeemFiat.Request(transactionsRedeemFiat.BaseRequest(from = loginState.address), to = toAddress, password = redeemFiatData.password, redeemAmount = redeemFiatData.redeemAmount, gas = redeemFiatData.gas, mode = transactionMode),
              kafkaAction = transactionsRedeemFiat.Service.kafkaPost,
              blockAction = transactionsRedeemFiat.Service.blockPost,
              asyncAction = transactionsRedeemFiat.Service.asyncPost,
              syncAction = transactionsRedeemFiat.Service.syncPost,
              onSuccess = blockchainTransactionRedeemFiats.Utility.onSuccess,
              onFailure = blockchainTransactionRedeemFiats.Utility.onFailure,
              updateTransactionHash = blockchainTransactionRedeemFiats.Service.updateTransactionHash
            )
            Ok(views.html.index(successes = Seq(constants.Response.FIAT_REDEEMED)))
          }
          catch {
            case baseException: BaseException => Ok(views.html.index(failures = Seq(baseException.failure)))
            case blockChainException: BlockChainException => Ok(views.html.index(failures = Seq(blockChainException.failure)))

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
            transactionsRedeemFiat.Service.kafkaPost(transactionsRedeemFiat.Request(transactionsRedeemFiat.BaseRequest(from = redeemFiatData.from), to = redeemFiatData.to, password = redeemFiatData.password, redeemAmount = redeemFiatData.redeemAmount, gas = redeemFiatData.gas, mode = transactionMode))
          } else {
            transactionsRedeemFiat.Service.blockPost(transactionsRedeemFiat.Request(transactionsRedeemFiat.BaseRequest(from = redeemFiatData.from), to = redeemFiatData.to, password = redeemFiatData.password, redeemAmount = redeemFiatData.redeemAmount, gas = redeemFiatData.gas, mode = transactionMode))
          }
          Ok(views.html.index(successes = Seq(constants.Response.FIAT_REDEEMED)))
        }
        catch {
          case baseException: BaseException => Ok(views.html.index(failures = Seq(baseException.failure)))
          case blockChainException: BlockChainException => Ok(views.html.index(failures = Seq(blockChainException.failure)))

        }
      }
    )
  }
}
