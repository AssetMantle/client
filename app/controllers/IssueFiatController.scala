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
class IssueFiatController @Inject()(messagesControllerComponents: MessagesControllerComponents, transaction: utilities.Transaction, withZoneLoginAction: WithZoneLoginAction, masterTransactionIssueFiatRequests: masterTransaction.IssueFiatRequests, blockchainAclAccounts: blockchain.ACLAccounts, masterZones: master.Zones, blockchainAccounts: blockchain.Accounts, withTraderLoginAction: WithTraderLoginAction, masterAccounts: master.Accounts, blockchainFiats: models.blockchain.Fiats, transactionsIssueFiat: transactions.IssueFiat, blockchainTransactionIssueFiats: blockchainTransaction.IssueFiats, withUsernameToken: WithUsernameToken)(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_ISSUE_FIAT

  def issueFiatRequestForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.issueFiatRequest())
  }

  def issueFiatRequest: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.IssueFiatRequest.form.bindFromRequest().fold(
        formWithErrors => {
          Future{BadRequest(views.html.component.master.issueFiatRequest(formWithErrors))}
        },
        issueFiatRequestData => {
          /*try {
            masterTransactionIssueFiatRequests.Service.create(accountID = loginState.username, transactionID = issueFiatRequestData.transactionID, transactionAmount = issueFiatRequestData.transactionAmount)
            withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.ISSUE_FIAT_REQUEST_SENT)))
          }
          catch {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }*/
          val create=masterTransactionIssueFiatRequests.Service.create(accountID = loginState.username, transactionID = issueFiatRequestData.transactionID, transactionAmount = issueFiatRequestData.transactionAmount)
          (for{
            _<-create
          }yield withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.ISSUE_FIAT_REQUEST_SENT)))
            ).recover{
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def viewPendingIssueFiatRequests: Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      /*try {
        withUsernameToken.Ok(views.html.component.master.viewPendingIssueFiatRequests(masterTransactionIssueFiatRequests.Service.getPendingIssueFiatRequests(masterAccounts.Service.getIDsForAddresses(blockchainAclAccounts.Service.getAddressesUnderZone(masterZones.Service.getZoneId(loginState.username))))))
      }
      catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }*/
      val zoneID=masterZones.Service.getID(loginState.username)
      def addressesUnderZone(zoneID:String)= blockchainAclAccounts.Service.getAddressesUnderZone(zoneID)
      def iDsForAddresses(addresses:Seq[String])=  masterAccounts.Service.getIDsForAddresses(addresses)
      def pendingIssueFiatRequests(iDs:Seq[String])=masterTransactionIssueFiatRequests.Service.getPendingIssueFiatRequests(iDs)
      (for{
        zoneID<-zoneID
        addressesUnderZone<-addressesUnderZone(zoneID)
        iDsForAddresses<-iDsForAddresses(addressesUnderZone)
        pendingIssueFiatRequests<-pendingIssueFiatRequests(iDsForAddresses)
      }yield withUsernameToken.Ok(views.html.component.master.viewPendingIssueFiatRequests(pendingIssueFiatRequests))
        ).recover{
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def rejectIssueFiatRequestForm(requestID: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.rejectIssueFiatRequest(requestID = requestID))
  }

  def rejectIssueFiatRequest: Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.RejectIssueFiatRequest.form.bindFromRequest().fold(
        formWithErrors => {
          Future{BadRequest(views.html.component.master.rejectIssueFiatRequest(formWithErrors, formWithErrors.data(constants.Form.REQUEST_ID)))}
        },
        rejectIssueFiatRequestData => {
          /*try {
            masterTransactionIssueFiatRequests.Service.reject(id = rejectIssueFiatRequestData.requestID, comment = rejectIssueFiatRequestData.comment)
            withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.ISSUE_FIAT_REQUEST_REJECTED)))
          }
          catch {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }*/
          val reject=masterTransactionIssueFiatRequests.Service.reject(id = rejectIssueFiatRequestData.requestID, comment = rejectIssueFiatRequestData.comment)
          (for{
            _<-reject
          }yield withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.ISSUE_FIAT_REQUEST_REJECTED)))
            ).recover{
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def issueFiatForm(requestID: String, accountID: String, transactionID: String, transactionAmount: Int): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.issueFiat(views.companion.master.IssueFiat.form.fill(views.companion.master.IssueFiat.Data(requestID = requestID, accountID = accountID, transactionID = transactionID, transactionAmount = transactionAmount, gas = constants.FormField.GAS.minimumValue, password = ""))))
  }

  def issueFiat: Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.IssueFiat.form.bindFromRequest().fold(
        formWithErrors => {
          Future{BadRequest(views.html.component.master.issueFiat(formWithErrors))}
        },
        issueFiatData => {
          val status=masterTransactionIssueFiatRequests.Service.getStatus(issueFiatData.requestID)
          def getResult(status:Option[Boolean])={
            if(status.isEmpty){
              val toAddress = masterAccounts.Service.getAddress(issueFiatData.accountID)
              def ticketID(toAddress:String) = transaction.process[blockchainTransaction.IssueFiat, transactionsIssueFiat.Request](
                entity = blockchainTransaction.IssueFiat(from = loginState.address, to = toAddress, transactionID = issueFiatData.transactionID, transactionAmount = issueFiatData.transactionAmount, gas = issueFiatData.gas, ticketID = "", mode = transactionMode),
                blockchainTransactionCreate = blockchainTransactionIssueFiats.Service.create,
                request = transactionsIssueFiat.Request(transactionsIssueFiat.BaseReq(from = loginState.address, gas = issueFiatData.gas.toString), to = toAddress, password = issueFiatData.password, transactionID = issueFiatData.transactionID, transactionAmount = issueFiatData.transactionAmount.toString, mode = transactionMode),
                action = transactionsIssueFiat.Service.post,
                onSuccess = blockchainTransactionIssueFiats.Utility.onSuccess,
                onFailure = blockchainTransactionIssueFiats.Utility.onFailure,
                updateTransactionHash = blockchainTransactionIssueFiats.Service.updateTransactionHash
              )
              def accept(ticketID:String)=masterTransactionIssueFiatRequests.Service.accept(requestID = issueFiatData.requestID, ticketID = ticketID, gas = issueFiatData.gas)
              for{
                toAddress<-toAddress
                ticketID<-ticketID(toAddress)
                _<-accept(ticketID)
              }yield withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.FIAT_ISSUED)))
            }else{
              Future{Unauthorized(views.html.index(failures = Seq(constants.Response.REQUEST_ALREADY_APPROVED_OR_REJECTED)))}
            }
          }
          (for{
            status<-status
            result<-getResult(status)
          } yield result
            ).recover{
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
        Future{BadRequest(views.html.component.blockchain.issueFiat(formWithErrors))}
      },
      issueFiatData => {

        val post= transactionsIssueFiat.Service.post(transactionsIssueFiat.Request(transactionsIssueFiat.BaseReq(from = issueFiatData.from, gas = issueFiatData.gas.toString), to = issueFiatData.to, password = issueFiatData.password, transactionID = issueFiatData.transactionID, transactionAmount = issueFiatData.transactionAmount.toString, mode = issueFiatData.mode))
        (for{
          _<-post
        }yield Ok(views.html.index(successes = Seq(constants.Response.FIAT_ISSUED)))
          ).recover{
          case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
        }
      }
    )
  }
}
