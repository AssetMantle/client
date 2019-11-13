package controllers

import controllers.actions.{WithGenesisLoginAction, WithLoginAction, WithUnknownLoginAction, WithUserLoginAction}
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.{blockchain, blockchainTransaction, master, masterTransaction}
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SendCoinController @Inject()(messagesControllerComponents: MessagesControllerComponents, transaction: utilities.Transaction, masterAccounts: master.Accounts, withLoginAction: WithLoginAction, withGenesisLoginAction: WithGenesisLoginAction, blockchainAccounts: blockchain.Accounts, masterTransactionFaucetRequests: masterTransaction.FaucetRequests, withUnknownLoginAction: WithUnknownLoginAction, transactionsSendCoin: transactions.SendCoin, blockchainTransactionSendCoins: blockchainTransaction.SendCoins, withUserLoginAction: WithUserLoginAction, withUsernameToken: WithUsernameToken)(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_SEND_COIN

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private val defaultFaucetToken = configuration.get[Int]("blockchain.defaultFaucetToken")

  private val denominationOfGasToken = configuration.get[String]("blockchain.denom.gas")

  def sendCoinForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.sendCoin())
  }

  def sendCoin: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.SendCoin.form.bindFromRequest().fold(
        formWithErrors => {
          Future{BadRequest(views.html.component.master.sendCoin(formWithErrors))}
        },
        sendCoinData => {
          val transactionProcess=transaction.process[blockchainTransaction.SendCoin, transactionsSendCoin.Request](
            entity = blockchainTransaction.SendCoin(from = loginState.address, to = sendCoinData.to, amount = sendCoinData.amount, gas = sendCoinData.gas, ticketID = "", mode = transactionMode),
            blockchainTransactionCreate = blockchainTransactionSendCoins.Service.create,
            request = transactionsSendCoin.Request(transactionsSendCoin.BaseReq(from = loginState.address, gas = sendCoinData.gas.toString), password = sendCoinData.password, to = sendCoinData.to, amount = Seq(transactionsSendCoin.Amount(denominationOfGasToken, sendCoinData.amount.toString)), mode = transactionMode),
            action = transactionsSendCoin.Service.post,
            onSuccess = blockchainTransactionSendCoins.Utility.onSuccess,
            onFailure = blockchainTransactionSendCoins.Utility.onFailure,
            updateTransactionHash = blockchainTransactionSendCoins.Service.updateTransactionHash
          )
          (for{
            _<-transactionProcess
          }yield withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.COINS_SENT)))
            ).recover{
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
          }

      )
  }

  def faucetRequestForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.faucetRequest())
  }

  def faucetRequest: Action[AnyContent] = withUnknownLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.FaucetRequest.form.bindFromRequest().fold(
        formWithErrors => {
          Future{BadRequest(views.html.component.master.faucetRequest(formWithErrors))}
        },
        faucetRequestFormData => {
          val create=masterTransactionFaucetRequests.Service.create(loginState.username, defaultFaucetToken)
          (for{
            _<-create
          }yield withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.COINS_REQUESTED)))
            ).recover{
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def viewPendingFaucetRequests: Action[AnyContent] = withGenesisLoginAction.authenticated { implicit loginState =>
    implicit request =>

    val pendingFaucetRequests=masterTransactionFaucetRequests.Service.getPendingFaucetRequests
      (for{
        pendingFaucetRequests<-pendingFaucetRequests
    }yield  Ok(views.html.component.master.viewPendingFaucetRequests(pendingFaucetRequests))
        ).recover{
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def rejectFaucetRequestForm(requestID: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.rejectFaucetRequest(requestID = requestID))
  }

  def rejectFaucetRequest: Action[AnyContent] = withGenesisLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.RejectFaucetRequest.form.bindFromRequest().fold(
        formWithErrors => {
          Future{BadRequest(views.html.component.master.rejectFaucetRequest(formWithErrors, formWithErrors.data(constants.FormField.REQUEST_ID.name)))}
        },
        rejectFaucetRequestData => {

          val reject=masterTransactionFaucetRequests.Service.reject(rejectFaucetRequestData.requestID, comment = rejectFaucetRequestData.comment)
          (for{
            _<-reject
          }yield withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.FAUCET_REQUEST_REJECTED)))
            ).recover{
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def approveFaucetRequestsForm(requestID: String, accountID: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.approveFaucetRequests(requestID = requestID, accountID = accountID))
  }

  def approveFaucetRequests: Action[AnyContent] = withGenesisLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.ApproveFaucetRequest.form.bindFromRequest().fold(
        formWithErrors => {
          Future{BadRequest(views.html.component.master.approveFaucetRequests(formWithErrors, formWithErrors.data(constants.FormField.REQUEST_ID.name), formWithErrors.data(constants.FormField.ACCOUNT_ID.name)))}
        },
        approveFaucetRequestFormData => {

          val status=masterTransactionFaucetRequests.Service.getStatus(approveFaucetRequestFormData.requestID)
          def getResult(status:Option[Boolean])={
            if(status.isEmpty){
              val toAddress = masterAccounts.Service.getAddress(approveFaucetRequestFormData.accountID)
              def ticketID(toAddress:String)=transaction.process[blockchainTransaction.SendCoin, transactionsSendCoin.Request](
                entity = blockchainTransaction.SendCoin(from = loginState.address, to = toAddress, amount = defaultFaucetToken, gas = approveFaucetRequestFormData.gas, ticketID = "", mode = transactionMode),
                blockchainTransactionCreate = blockchainTransactionSendCoins.Service.create,
                request = transactionsSendCoin.Request(transactionsSendCoin.BaseReq(from = loginState.address, gas = approveFaucetRequestFormData.gas.toString), password = approveFaucetRequestFormData.password, to = toAddress, amount = Seq(transactionsSendCoin.Amount(denominationOfGasToken, defaultFaucetToken.toString)), mode = transactionMode),
                action = transactionsSendCoin.Service.post,
                onSuccess = blockchainTransactionSendCoins.Utility.onSuccess,
                onFailure = blockchainTransactionSendCoins.Utility.onFailure,
                updateTransactionHash = blockchainTransactionSendCoins.Service.updateTransactionHash
              )
              def accept(ticketID:String)=masterTransactionFaucetRequests.Service.accept(requestID = approveFaucetRequestFormData.requestID, ticketID = ticketID, gas = approveFaucetRequestFormData.gas)
              for{
              toAddress<-toAddress
              ticketID<-ticketID(toAddress)
              _<-accept(ticketID)
            }yield withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.FAUCET_REQUEST_APPROVED)))

            }else{
              Future{Unauthorized(views.html.index(failures = Seq(constants.Response.REQUEST_ALREADY_APPROVED_OR_REJECTED)))}
            }
          }
          (for{
            status<-status
            result<-getResult(status)
          }yield result
            ).recover{
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def blockchainSendCoinForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.blockchain.sendCoin())
  }

  def blockchainSendCoin: Action[AnyContent] = Action.async { implicit request =>
    views.companion.blockchain.SendCoin.form.bindFromRequest().fold(
      formWithErrors => {
        Future{BadRequest(views.html.component.blockchain.sendCoin(formWithErrors))}
      },
      sendCoinData => {
        val postRequest=transactionsSendCoin.Service.post(transactionsSendCoin.Request(transactionsSendCoin.BaseReq(from = sendCoinData.from, gas = sendCoinData.gas.toString), password = sendCoinData.password, to = sendCoinData.to, amount = Seq(transactionsSendCoin.Amount(denominationOfGasToken, sendCoinData.amount.toString)), mode = sendCoinData.mode))
        (for{
          _<-postRequest
        }yield Ok(views.html.index(successes = Seq(constants.Response.COINS_SENT)))
          ).recover{
          case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
        }
      }
    )
  }

}
