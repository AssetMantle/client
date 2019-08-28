package controllers

import controllers.actions.WithTraderLoginAction
import controllers.results.WithUsernameToken
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
class RedeemFiatController @Inject()(messagesControllerComponents: MessagesControllerComponents, transaction: utilities.Transaction, blockchainZones: blockchain.Zones, blockchainACLAccounts: blockchain.ACLAccounts, withTraderLoginAction: WithTraderLoginAction, transactionsRedeemFiat: transactions.RedeemFiat, blockchainTransactionRedeemFiats: blockchainTransaction.RedeemFiats, withUsernameToken: WithUsernameToken)(implicit exec: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

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
              entity = blockchainTransaction.RedeemFiat(from = loginState.address, to = toAddress, redeemAmount = redeemFiatData.redeemAmount, null, null, ticketID = "", mode = transactionMode, null),
              blockchainTransactionCreate = blockchainTransactionRedeemFiats.Service.create,
              request = transactionsRedeemFiat.Request(transactionsRedeemFiat.BaseRequest(from = loginState.address), to = toAddress, password = redeemFiatData.password, redeemAmount = redeemFiatData.redeemAmount.toString, mode = transactionMode),
              action = transactionsRedeemFiat.Service.post,
              onSuccess = blockchainTransactionRedeemFiats.Utility.onSuccess,
              onFailure = blockchainTransactionRedeemFiats.Utility.onFailure,
              updateTransactionHash = blockchainTransactionRedeemFiats.Service.updateTransactionHash
            )
            withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.FIAT_REDEEMED)))
          }
          catch {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
            case blockChainException: BlockChainException => InternalServerError(views.html.index(failures = Seq(blockChainException.failure)))
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
          transactionsRedeemFiat.Service.post(transactionsRedeemFiat.Request(transactionsRedeemFiat.BaseRequest(from = redeemFiatData.from), to = redeemFiatData.to, password = redeemFiatData.password, redeemAmount = redeemFiatData.redeemAmount.toString, mode = redeemFiatData.mode))
          Ok(views.html.index(successes = Seq(constants.Response.FIAT_REDEEMED)))
        }
        catch {
          case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          case blockChainException: BlockChainException => InternalServerError(views.html.index(failures = Seq(blockChainException.failure)))
        }
      }
    )
  }
}
