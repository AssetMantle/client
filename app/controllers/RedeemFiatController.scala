package controllers

import controllers.actions.{WithTraderLoginAction, WithZoneLoginAction}
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.{blockchain, blockchainTransaction, master, masterTransaction}
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RedeemFiatController @Inject()(messagesControllerComponents: MessagesControllerComponents,
                                     blockchainTransactionRedeemFiats: blockchainTransaction.RedeemFiats,
                                     blockchainAccounts: blockchain.Accounts,
                                     masterZones: master.Zones,
                                     masterTraders: master.Traders,
                                     masterTransactionRedeemFiatRequests: masterTransaction.RedeemFiatRequests,
                                     transactionsRedeemFiat: transactions.RedeemFiat,
                                     transaction: utilities.Transaction,
                                     withTraderLoginAction: WithTraderLoginAction,
                                     withZoneLoginAction: WithZoneLoginAction,
                                     withUsernameToken: WithUsernameToken)(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private implicit val module: String = constants.Module.CONTROLLERS_REDEEM_FIAT

  def redeemFiatForm: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      Future(Ok(views.html.component.master.redeemFiat()))

  }


  def redeemFiat: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.RedeemFiat.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.redeemFiat(formWithErrors)))
        },
        redeemFiatData => {
          val trader = masterTraders.Service.tryGetByAccountID(loginState.username)

          def zoneAccountID(zoneID: String): Future[String] = masterZones.Service.tryGetAccountID(zoneID)

          def zoneAddress(zoneAccountID: String): Future[String] = blockchainAccounts.Service.tryGetAddress(zoneAccountID)

          def transactionProcess(toAddress: String): Future[String] = transaction.process[blockchainTransaction.RedeemFiat, transactionsRedeemFiat.Request](
            entity = blockchainTransaction.RedeemFiat(from = loginState.address, to = toAddress, redeemAmount = redeemFiatData.redeemAmount, gas = redeemFiatData.gas, ticketID = "", mode = transactionMode),
            blockchainTransactionCreate = blockchainTransactionRedeemFiats.Service.create,
            request = transactionsRedeemFiat.Request(transactionsRedeemFiat.BaseReq(from = loginState.address, gas = redeemFiatData.gas.toString), to = toAddress, password = redeemFiatData.password, redeemAmount = redeemFiatData.redeemAmount.toString, mode = transactionMode),
            action = transactionsRedeemFiat.Service.post,
            onSuccess = blockchainTransactionRedeemFiats.Utility.onSuccess,
            onFailure = blockchainTransactionRedeemFiats.Utility.onFailure,
            updateTransactionHash = blockchainTransactionRedeemFiats.Service.updateTransactionHash
          )

          def createRedeemFiatRequests(traderID: String, ticketID: String): Future[String] = masterTransactionRedeemFiatRequests.Service.create(traderID, ticketID, redeemFiatData.redeemAmount)

          (for {
            trader <- trader
            zoneAccountID <- zoneAccountID(trader.zoneID)
            zoneAddress <- zoneAddress(zoneAccountID)
            ticketID <- transactionProcess(zoneAddress)
            _ <- createRedeemFiatRequests(trader.id, ticketID)
            result <- withUsernameToken.Ok(views.html.trades(successes = Seq(constants.Response.FIAT_REDEEMED)))
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def zoneRedeemFiatForm(id: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      Future(Ok(views.html.component.master.zoneRedeemFiat(id = id)))
  }

  def zoneRedeemFiat: Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.ZoneRedeemFiat.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.zoneRedeemFiat(formWithErrors, id = formWithErrors.data(constants.FormField.ID.name))))
        },
        redeemFiatData => {
          val markRedeemed = masterTransactionRedeemFiatRequests.Service.markRedeemed(redeemFiatData.id)
          (for {
            _ <- markRedeemed
          } yield Ok(views.html.transactions(successes = Seq(constants.Response.FIAT_REDEEMED)))
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.transactions(failures = Seq(baseException.failure)))
          }
        })
  }

  def blockchainRedeemFiatForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.blockchain.redeemFiat())
  }

  def blockchainRedeemFiat: Action[AnyContent] = Action.async { implicit request =>
    views.companion.blockchain.RedeemFiat.form.bindFromRequest().fold(
      formWithErrors => {
        Future(BadRequest(views.html.component.blockchain.redeemFiat(formWithErrors)))
      },
      redeemFiatData => {
        val post = transactionsRedeemFiat.Service.post(transactionsRedeemFiat.Request(transactionsRedeemFiat.BaseReq(from = redeemFiatData.from, gas = redeemFiatData.gas.toString), to = redeemFiatData.to, password = redeemFiatData.password, redeemAmount = redeemFiatData.redeemAmount.toString, mode = redeemFiatData.mode))
        (for {
          _ <- post
        } yield Ok(views.html.index(successes = Seq(constants.Response.FIAT_REDEEMED)))
          ).recover {
          case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
        }
      }
    )
  }
}
