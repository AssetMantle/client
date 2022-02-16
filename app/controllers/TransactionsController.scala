package controllers

import controllers.actions.{WithLoginActionAsync, WithUserLoginAction, WithoutLoginAction, WithoutLoginActionAsync}
import controllers.results.WithUsernameToken
import exceptions.BaseException
import models.{blockchain, master, masterTransaction}
import play.api.{Configuration, Logger}
import play.api.i18n.I18nSupport
import play.api.libs.ws.WSClient
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import transactions.blockchain.{AddKey, ChangePassword, ForgotPassword}
import utilities.KeyStore
import views.companion.blockchain.SendCoin

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TransactionsController @Inject()(
                                   utilitiesNotification: utilities.Notification,
                                   withLoginActionAsync: WithLoginActionAsync,
                                   withUserLoginAction: WithUserLoginAction,
                                   withUsernameToken: WithUsernameToken,
                                   blockchainBalances: blockchain.Balances,
                                   blockchainIdentityProvisions: blockchain.IdentityProvisions,
                                   masterAccounts: master.Accounts,
                                   masterProfiles: master.Profiles,
                                   masterTransactionEmailOTP: masterTransaction.EmailOTPs,
                                   masterTransactionSessionTokens: masterTransaction.SessionTokens,
                                   masterTransactionPushNotificationTokens: masterTransaction.PushNotificationTokens,
                                   transactionAddKey: AddKey,
                                   transactionForgotPassword: ForgotPassword,
                                   transactionChangePassword: ChangePassword,
                                   messagesControllerComponents: MessagesControllerComponents,
                                   masterEmails: master.Emails,
                                   masterMobiles: master.Mobiles,
                                   masterIdentifications: master.Identifications,
                                   masterAccountKYCs: master.AccountKYCs,
                                   withoutLoginAction: WithoutLoginAction,
                                   withoutLoginActionAsync: WithoutLoginActionAsync,
                                   keyStore: KeyStore
                                 )
                                 (implicit
                                  executionContext: ExecutionContext,
                                  configuration: Configuration,
                                  wsClient: WSClient,
                                 ) extends AbstractController(messagesControllerComponents) with I18nSupport {


  private implicit val module: String = constants.Module.CONTROLLERS_TRANSACTIONS

  private implicit val logger: Logger = Logger(this.getClass)

  def sendCoinForm: Action[AnyContent] = withLoginActionAsync { implicit loginState =>
    implicit request =>
      val balance = blockchainBalances.Service.get(loginState.address)

      (for {
        balance <- balance
      } yield Ok(views.html.component.blockchain.txForms.sendCoin(fromAddress = loginState.address, denoms = balance.fold(Seq(constants.Blockchain.StakingDenom))(_.coins.map(_.denom))))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def sendCoin: Action[AnyContent] = withLoginActionAsync { loginState =>
    implicit request =>
      SendCoin.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.blockchain.txForms.sendCoin(formWithErrors, loginState.address, Seq(constants.Blockchain.StakingDenom))))
        },
        sendCoinData => {
          Future(InternalServerError(views.html.index(failures = Seq(constants.Response.NOT_SUPPORTED))))
        }
      )
  }

}
