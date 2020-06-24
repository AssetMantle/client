package controllers

import controllers.actions.{WithLoginAction, WithUnknownLoginAction, WithUserLoginAction, WithoutLoginAction, WithoutLoginActionAsync}
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.{blockchain, blockchainTransaction, master, masterTransaction}
import play.api.i18n.I18nSupport
import play.api.mvc._
import play.api.{Configuration, Logger}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SendCoinController @Inject()(
                                    messagesControllerComponents: MessagesControllerComponents,
                                    transaction: utilities.Transaction,
                                    withLoginAction: WithLoginAction,
                                    withUnknownLoginAction: WithUnknownLoginAction,
                                    transactionsSendCoin: transactions.SendCoin,
                                    blockchainTransactionSendCoins: blockchainTransaction.SendCoins,
                                    withUserLoginAction: WithUserLoginAction,
                                    withUsernameToken: WithUsernameToken,
                                    withoutLoginAction: WithoutLoginAction,
                                    withoutLoginActionAsync: WithoutLoginActionAsync
                                  )(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_SEND_COIN

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private val denom = configuration.get[String]("blockchain.denom")

  def sendCoinForm: Action[AnyContent] = withoutLoginAction { implicit request =>
    Ok(views.html.component.master.sendCoin())
  }

  def sendCoin: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.SendCoin.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.sendCoin(formWithErrors)))
        },
        sendCoinData => {
          val transactionProcess = transaction.process[blockchainTransaction.SendCoin, transactionsSendCoin.Request](
            entity = blockchainTransaction.SendCoin(from = loginState.address, to = sendCoinData.to, amount = sendCoinData.amount, gas = sendCoinData.gas, ticketID = "", mode = transactionMode),
            blockchainTransactionCreate = blockchainTransactionSendCoins.Service.create,
            request = transactionsSendCoin.Request(transactionsSendCoin.BaseReq(from = loginState.address, gas = sendCoinData.gas.toString), password = sendCoinData.password, to = sendCoinData.to, amount = Seq(transactionsSendCoin.Amount(denom, sendCoinData.amount.toString)), mode = transactionMode),
            action = transactionsSendCoin.Service.post,
            onSuccess = blockchainTransactionSendCoins.Utility.onSuccess,
            onFailure = blockchainTransactionSendCoins.Utility.onFailure,
            updateTransactionHash = blockchainTransactionSendCoins.Service.updateTransactionHash
          )
          (for {
            _ <- transactionProcess
            result <- withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.COINS_SENT)))
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.profile(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def blockchainSendCoinForm: Action[AnyContent] = withoutLoginAction { implicit request =>
    Ok(views.html.component.blockchain.sendCoin())
  }

  def blockchainSendCoin: Action[AnyContent] = withoutLoginActionAsync { implicit request =>
    views.companion.blockchain.SendCoin.form.bindFromRequest().fold(
      formWithErrors => {
        Future(BadRequest(views.html.component.blockchain.sendCoin(formWithErrors)))
      },
      sendCoinData => {
        val postRequest = transactionsSendCoin.Service.post(transactionsSendCoin.Request(transactionsSendCoin.BaseReq(from = sendCoinData.from, gas = sendCoinData.gas.toString), password = sendCoinData.password, to = sendCoinData.to, amount = Seq(transactionsSendCoin.Amount(denom, sendCoinData.amount.toString)), mode = sendCoinData.mode))
        (for {
          _ <- postRequest
        } yield Ok(views.html.index(successes = Seq(constants.Response.COINS_SENT)))
          ).recover {
          case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
        }
      }
    )
  }
}
