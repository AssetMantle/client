package controllers

import controllers.actions.{WithZoneLoginAction, WithoutLoginAction, WithoutLoginActionAsync}
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.westernUnion.FiatRequests
import models.{blockchain, blockchainTransaction, master}
import play.api.i18n.I18nSupport
import play.api.mvc._
import play.api.{Configuration, Logger}
import utilities.MicroNumber
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class IssueFiatController @Inject()(messagesControllerComponents: MessagesControllerComponents,
                                    blockchainTransactionIssueFiats: blockchainTransaction.IssueFiats,
                                    masterTraders: master.Traders,
                                    blockchainAccounts: blockchain.Accounts,
                                    masterFiats: master.Fiats,
                                    masterTransactionIssueFiatRequests: FiatRequests,
                                    transactionsIssueFiat: transactions.IssueFiat,
                                    transaction: utilities.Transaction,
                                    withZoneLoginAction: WithZoneLoginAction,
                                    withoutLoginAction: WithoutLoginAction,
                                    withoutLoginActionAsync: WithoutLoginActionAsync,
                                    withUsernameToken: WithUsernameToken)(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_ISSUE_FIAT

  def issueFiatRequestForm: Action[AnyContent] = withoutLoginAction { implicit request =>
    Ok(views.html.component.master.issueFiatRequest())
  }

  def issueFiatForm(requestID: String, accountID: String, transactionID: String, transactionAmount: Double): Action[AnyContent] = withoutLoginAction { implicit request =>
    Ok(views.html.component.master.issueFiat(views.companion.master.IssueFiat.form.fill(views.companion.master.IssueFiat.Data(requestID = requestID, accountID = accountID, transactionID = transactionID, transactionAmount = new MicroNumber(transactionAmount), gas = constants.FormField.GAS.maximumValue, password = ""))))
  }

  def issueFiat: Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.IssueFiat.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.issueFiat(formWithErrors)))
        },
        issueFiatData => {
          val status = masterTransactionIssueFiatRequests.Service.getStatus(issueFiatData.requestID)

          def getResult(status: Option[Boolean]): Future[Result] = {
            if (status.isEmpty) {
              val toAddress = blockchainAccounts.Service.tryGetAddress(issueFiatData.accountID)
              val traderID = masterTraders.Service.tryGetID(issueFiatData.accountID)

              def sendTransaction(toAddress: String): Future[String] = {
                if (loginState.acl.getOrElse(throw new BaseException(constants.Response.UNAUTHORIZED)).issueFiat) {
                  transaction.process[blockchainTransaction.IssueFiat, transactionsIssueFiat.Request](
                    entity = blockchainTransaction.IssueFiat(from = loginState.address, to = toAddress, transactionID = issueFiatData.transactionID, transactionAmount = issueFiatData.transactionAmount, gas = issueFiatData.gas, ticketID = "", mode = transactionMode),
                    blockchainTransactionCreate = blockchainTransactionIssueFiats.Service.create,
                    request = transactionsIssueFiat.Request(transactionsIssueFiat.BaseReq(from = loginState.address, gas = issueFiatData.gas), to = toAddress, password = issueFiatData.password, transactionID = issueFiatData.transactionID, transactionAmount = issueFiatData.transactionAmount, mode = transactionMode),
                    action = transactionsIssueFiat.Service.post,
                    onSuccess = blockchainTransactionIssueFiats.Utility.onSuccess,
                    onFailure = blockchainTransactionIssueFiats.Utility.onFailure,
                    updateTransactionHash = blockchainTransactionIssueFiats.Service.updateTransactionHash
                  )
                } else throw new BaseException(constants.Response.UNAUTHORIZED)
              }

              def create(traderID: String) = masterFiats.Service.create(traderID, issueFiatData.transactionID, issueFiatData.transactionAmount, new MicroNumber(0))

              for {
                toAddress <- toAddress
                ticketID <- sendTransaction(toAddress)
                traderID <- traderID
                _ <- create(traderID)
                result <- withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.FIAT_ISSUED)))
              } yield result
            } else {
              Future(Unauthorized(views.html.index(failures = Seq(constants.Response.REQUEST_ALREADY_APPROVED_OR_REJECTED))))
            }
          }

          (for {
            status <- status
            //TODO @puneet
            result <- getResult(None)
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def blockchainIssueFiatForm: Action[AnyContent] = withoutLoginAction { implicit request =>
    Ok(views.html.component.blockchain.issueFiat())
  }

  def blockchainIssueFiat: Action[AnyContent] = withoutLoginActionAsync { implicit request =>
    views.companion.blockchain.IssueFiat.form.bindFromRequest().fold(
      formWithErrors => {
        Future(BadRequest(views.html.component.blockchain.issueFiat(formWithErrors)))
      },
      issueFiatData => {
        val post = transactionsIssueFiat.Service.post(transactionsIssueFiat.Request(transactionsIssueFiat.BaseReq(from = issueFiatData.from, gas = issueFiatData.gas), to = issueFiatData.to, password = issueFiatData.password, transactionID = issueFiatData.transactionID, transactionAmount = issueFiatData.transactionAmount, mode = issueFiatData.mode))
        (for {
          _ <- post
        } yield Ok(views.html.index(successes = Seq(constants.Response.FIAT_ISSUED)))
          ).recover {
          case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
        }
      }
    )
  }
}
