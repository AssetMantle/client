package controllers

import constants.Response.Success
import controllers.actions._
import controllers.results.WithUsernameToken
import utilities.Configuration.OtherApp
import exceptions.BaseException

import javax.inject.{Inject, Singleton}
import models.common.Serializable.Coin
import models.{blockchainTransaction, master}
import play.api.i18n.I18nSupport
import play.api.mvc._
import play.api.{Configuration, Logger}
import transactions.blockchain.SendCoin
import views.companion.{blockchain => blockchainCompanion}
import views.html.component.blockchain.{txForms => blockchainForms}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SendCoinController @Inject()(
                                    messagesControllerComponents: MessagesControllerComponents,
                                    transaction: utilities.Transaction,
                                    withLoginActionAsync: WithLoginActionAsync,
                                    masterAccounts: master.Accounts,
                                    withUnknownLoginAction: WithUnknownLoginAction,
                                    transactionsSendCoin: SendCoin,
                                    blockchainTransactionSendCoins: blockchainTransaction.SendCoins,
                                    withUserLoginAction: WithUserLoginAction,
                                    withUsernameToken: WithUsernameToken,
                                    withoutLoginAction: WithoutLoginAction,
                                    withoutLoginActionAsync: WithoutLoginActionAsync
                                  )(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_SEND_COIN

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private val denom = configuration.get[String]("blockchain.stakingDenom")

  private implicit val otherApps: Seq[OtherApp] = configuration.get[Seq[Configuration]]("webApp.otherApps").map { otherApp =>
    OtherApp(url = otherApp.get[String]("url"), name = otherApp.get[String]("name"))
  }

  def sendCoinForm: Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
      Ok(blockchainForms.sendCoin())
  }

  def sendCoin: Action[AnyContent] = withLoginActionAsync { implicit loginState =>
    implicit request =>
      blockchainCompanion.SendCoin.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(blockchainForms.sendCoin(formWithErrors)))
        },
        sendCoinData => {
          val verifyPassword = masterAccounts.Service.validateUsernamePassword(username = loginState.username, password = sendCoinData.password)

          def broadcastTx = transaction.process[blockchainTransaction.SendCoin, transactionsSendCoin.Request](
            entity = blockchainTransaction.SendCoin(from = loginState.address, to = sendCoinData.to, amount = Seq(Coin(denom, sendCoinData.amount)), gas = sendCoinData.gas, ticketID = "", mode = transactionMode),
            blockchainTransactionCreate = blockchainTransactionSendCoins.Service.create,
            request = transactionsSendCoin.Request(transactionsSendCoin.BaseReq(from = loginState.address, gas = sendCoinData.gas.toString), password = sendCoinData.password, to = sendCoinData.to, amount = Seq(transactionsSendCoin.Amount(denom, sendCoinData.amount.toString)), mode = transactionMode),
            action = transactionsSendCoin.Service.post,
            onSuccess = blockchainTransactionSendCoins.Utility.onSuccess,
            onFailure = blockchainTransactionSendCoins.Utility.onFailure,
            updateTransactionHash = blockchainTransactionSendCoins.Service.updateTransactionHash
          )

          def broadcastTxAndGetResult(verifyPassword: Boolean) = if (verifyPassword) {
            for {
              ticketID <- broadcastTx
              result <- withUsernameToken.Ok(views.html.index(successes = Seq(new Success(ticketID))))
            } yield result
          } else Future(BadRequest(blockchainForms.sendCoin(blockchainCompanion.SendCoin.form.fill(sendCoinData).withError(constants.FormField.PASSWORD.name, constants.Response.INCORRECT_PASSWORD.message))))

          (for {
            verifyPassword <- verifyPassword
            result <- broadcastTxAndGetResult(verifyPassword)
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.account(failures = Seq(baseException.failure)))
          }
        }
      )
  }
}
