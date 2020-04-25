package controllers

import controllers.actions.{WithTraderLoginAction, WithZoneLoginAction}
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.WesternUnion.{FiatRequest, FiatRequests}
import models.{blockchain, blockchainTransaction, master, masterTransaction}
import play.api.i18n.I18nSupport
import play.api.mvc._
import play.api.{Configuration, Logger}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class IssueFiatController @Inject()(messagesControllerComponents: MessagesControllerComponents,
                                    transaction: utilities.Transaction,
                                    withZoneLoginAction: WithZoneLoginAction,
                                    masterTransactionIssueFiatRequests: FiatRequests,
                                    blockchainAclAccounts: blockchain.ACLAccounts,
                                    masterZones: master.Zones,
                                    masterTraders: master.Traders,
                                    withTraderLoginAction: WithTraderLoginAction,
                                    masterAccounts: master.Accounts,
                                    masterFiats: master.Fiats,
                                    transactionsIssueFiat: transactions.IssueFiat,
                                    blockchainTransactionIssueFiats: blockchainTransaction.IssueFiats,
                                    withUsernameToken: WithUsernameToken)(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_ISSUE_FIAT

  def issueFiatRequestForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.issueFiatRequest())
  }

  def issueFiatForm(requestID: String, accountID: String, transactionID: String, transactionAmount: Int): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.issueFiat(views.companion.master.IssueFiat.form.fill(views.companion.master.IssueFiat.Data(requestID = requestID, accountID = accountID, transactionID = transactionID, transactionAmount = transactionAmount, gas = constants.FormField.GAS.minimumValue, password = ""))))
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
              val toAddress = masterAccounts.Service.getAddress(issueFiatData.accountID)

              def ticketID(toAddress: String): Future[String] = transaction.process[blockchainTransaction.IssueFiat, transactionsIssueFiat.Request](
                entity = blockchainTransaction.IssueFiat(from = loginState.address, to = toAddress, transactionID = issueFiatData.transactionID, transactionAmount = issueFiatData.transactionAmount, gas = issueFiatData.gas, ticketID = "", mode = transactionMode),
                blockchainTransactionCreate = blockchainTransactionIssueFiats.Service.create,
                request = transactionsIssueFiat.Request(transactionsIssueFiat.BaseReq(from = loginState.address, gas = issueFiatData.gas.toString), to = toAddress, password = issueFiatData.password, transactionID = issueFiatData.transactionID, transactionAmount = issueFiatData.transactionAmount.toString, mode = transactionMode),
                action = transactionsIssueFiat.Service.post,
                onSuccess = blockchainTransactionIssueFiats.Utility.onSuccess,
                onFailure = blockchainTransactionIssueFiats.Utility.onFailure,
                updateTransactionHash = blockchainTransactionIssueFiats.Service.updateTransactionHash
              )

              val traderID = masterTraders.Service.tryGetID(issueFiatData.accountID)
              def create(traderID: String) = masterFiats.Service.create(traderID, issueFiatData.transactionID, issueFiatData.transactionAmount, 0)

              for {
                toAddress <- toAddress
                ticketID <- ticketID(toAddress)
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
            result <- getResult(None)
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def blockchainIssueFiatForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.blockchain.issueFiat())
  }

  def blockchainIssueFiat: Action[AnyContent] = Action.async { implicit request =>
    views.companion.blockchain.IssueFiat.form.bindFromRequest().fold(
      formWithErrors => {
        Future(BadRequest(views.html.component.blockchain.issueFiat(formWithErrors)))
      },
      issueFiatData => {
        val post = transactionsIssueFiat.Service.post(transactionsIssueFiat.Request(transactionsIssueFiat.BaseReq(from = issueFiatData.from, gas = issueFiatData.gas.toString), to = issueFiatData.to, password = issueFiatData.password, transactionID = issueFiatData.transactionID, transactionAmount = issueFiatData.transactionAmount.toString, mode = issueFiatData.mode))
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
