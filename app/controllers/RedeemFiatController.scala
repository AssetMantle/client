package controllers

import controllers.actions.WithTraderLoginAction
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.{blockchain, blockchainTransaction, master}
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RedeemFiatController @Inject()(messagesControllerComponents: MessagesControllerComponents, transaction: utilities.Transaction, masterTraders: master.Traders, blockchainZones: blockchain.Zones, blockchainACLAccounts: blockchain.ACLAccounts, withTraderLoginAction: WithTraderLoginAction, transactionsRedeemFiat: transactions.RedeemFiat, blockchainTransactionRedeemFiats: blockchainTransaction.RedeemFiats, withUsernameToken: WithUsernameToken)(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private implicit val module: String = constants.Module.CONTROLLERS_REDEEM_FIAT

  //TODO Shall we fetch username from login state using withTraderLoginAction?
  def redeemFiatForm(username: String): Action[AnyContent] = Action.async { implicit request =>
    val zoneID = masterTraders.Service.tryGetZoneIDByAccountID(username)
    (for {
      zoneID <- zoneID
    } yield Ok(views.html.component.master.redeemFiat(zoneID = zoneID))
      ).recover {
      case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
    }
  }


  def redeemFiat: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.RedeemFiat.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.redeemFiat(formWithErrors, formWithErrors.data(constants.FormField.ZONE_ID.name))))
        },
        redeemFiatData => {
          val toAddress = blockchainZones.Service.getAddress(redeemFiatData.zoneID)

          def transactionProcess(toAddress: String): Future[String] = transaction.process[blockchainTransaction.RedeemFiat, transactionsRedeemFiat.Request](
            entity = blockchainTransaction.RedeemFiat(from = loginState.address, to = toAddress, redeemAmount = redeemFiatData.redeemAmount, gas = redeemFiatData.gas, ticketID = "", mode = transactionMode),
            blockchainTransactionCreate = blockchainTransactionRedeemFiats.Service.create,
            request = transactionsRedeemFiat.Request(transactionsRedeemFiat.BaseReq(from = loginState.address, gas = redeemFiatData.gas.toString), to = toAddress, password = redeemFiatData.password, redeemAmount = redeemFiatData.redeemAmount.toString, mode = transactionMode),
            action = transactionsRedeemFiat.Service.post,
            onSuccess = blockchainTransactionRedeemFiats.Utility.onSuccess,
            onFailure = blockchainTransactionRedeemFiats.Utility.onFailure,
            updateTransactionHash = blockchainTransactionRedeemFiats.Service.updateTransactionHash
          )

          (for {
            toAddress <- toAddress
            _ <- transactionProcess(toAddress)
            result <- withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.FIAT_REDEEMED)))
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
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
