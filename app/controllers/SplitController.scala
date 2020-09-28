package controllers

import constants.Response.Success
import controllers.actions._
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.blockchainTransaction
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SplitController @Inject()(
                                 messagesControllerComponents: MessagesControllerComponents,
                                 transaction: utilities.Transaction,
                                 withLoginAction: WithLoginAction,
                                 withUnknownLoginAction: WithUnknownLoginAction,
                                 transactionsSplitSend: transactions.blockchain.SplitSend,
                                 transactionsSplitWrap: transactions.blockchain.SplitWrap,
                                 transactionsSplitUnwrap: transactions.blockchain.SplitUnwrap,
                                 blockchainTransactionSplitSends: blockchainTransaction.SplitSends,
                                 blockchainTransactionSplitWraps: blockchainTransaction.SplitWraps,
                                 blockchainTransactionSplitUnwraps: blockchainTransaction.SplitUnwraps,
                                 withUserLoginAction: WithUserLoginAction,
                                 withUsernameToken: WithUsernameToken,
                                 withoutLoginAction: WithoutLoginAction,
                                 withoutLoginActionAsync: WithoutLoginActionAsync
                               )(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_SPLIT

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  def sendForm: Action[AnyContent] = withoutLoginAction { implicit request =>
    Ok(views.html.component.blockchain.txForms.splitSend())
  }

  def send: Action[AnyContent] = withoutLoginActionAsync { implicit request =>
    views.companion.blockchain.SplitSend.form.bindFromRequest().fold(
      formWithErrors => {
        Future(BadRequest(views.html.component.blockchain.txForms.splitSend(formWithErrors)))
      },
      sendData => {
        val ticketID = transaction.process[blockchainTransaction.SplitSend, transactionsSplitSend.Request](
          entity = blockchainTransaction.SplitSend(from = sendData.from, fromID = sendData.fromID, toID = sendData.toID, ownableID = sendData.ownableID, split = sendData.split, gas = sendData.gas, ticketID = "", mode = transactionMode),
          blockchainTransactionCreate = blockchainTransactionSplitSends.Service.create,
          request = transactionsSplitSend.Request(transactionsSplitSend.Message(transactionsSplitSend.BaseReq(from = sendData.from, gas = sendData.gas), fromID = sendData.fromID, toID = sendData.toID, ownableID = sendData.ownableID, split = sendData.split)),
          action = transactionsSplitSend.Service.post,
          onSuccess = blockchainTransactionSplitSends.Utility.onSuccess,
          onFailure = blockchainTransactionSplitSends.Utility.onFailure,
          updateTransactionHash = blockchainTransactionSplitSends.Service.updateTransactionHash
        )
        (for {
          ticketID <- ticketID
        } yield Ok(views.html.dashboard(successes = Seq(new Success(ticketID))))
          ).recover {
          case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
        }
      }
    )
  }

  def wrapForm: Action[AnyContent] = withoutLoginAction { implicit request =>
    Ok(views.html.component.blockchain.txForms.splitWrap())
  }

  def wrap: Action[AnyContent] = withoutLoginActionAsync { implicit request =>
    views.companion.blockchain.SplitWrap.form.bindFromRequest().fold(
      formWithErrors => {
        Future(BadRequest(views.html.component.blockchain.txForms.splitWrap(formWithErrors)))
      },
      wrapData => {
        if (wrapData.addField) {
          Future(PartialContent(views.html.component.blockchain.txForms.splitWrap(splitWrapForm = views.companion.blockchain.SplitWrap.form.fill(wrapData.copy(addField = false)))))
        } else {
          val ticketID = transaction.process[blockchainTransaction.SplitWrap, transactionsSplitWrap.Request](
            entity = blockchainTransaction.SplitWrap(from = wrapData.from, fromID = wrapData.fromID, coins = wrapData.coins.flatten.map(_.toCoin), gas = wrapData.gas, ticketID = "", mode = transactionMode),
            blockchainTransactionCreate = blockchainTransactionSplitWraps.Service.create,
            request = transactionsSplitWrap.Request(transactionsSplitWrap.Message(transactionsSplitWrap.BaseReq(from = wrapData.from, gas = wrapData.gas), fromID = wrapData.fromID, coins = wrapData.coins.flatten.map(_.toCoin))),
            action = transactionsSplitWrap.Service.post,
            onSuccess = blockchainTransactionSplitWraps.Utility.onSuccess,
            onFailure = blockchainTransactionSplitWraps.Utility.onFailure,
            updateTransactionHash = blockchainTransactionSplitWraps.Service.updateTransactionHash
          )
          (for {
            ticketID <- ticketID
          } yield Ok(views.html.dashboard(successes = Seq(new Success(ticketID))))
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      }
    )
  }

  def unwrapForm: Action[AnyContent] = withoutLoginAction { implicit request =>
    Ok(views.html.component.blockchain.txForms.splitUnwrap())
  }

  def unwrap: Action[AnyContent] = withoutLoginActionAsync { implicit request =>
    views.companion.blockchain.SplitUnwrap.form.bindFromRequest().fold(
      formWithErrors => {
        Future(BadRequest(views.html.component.blockchain.txForms.splitUnwrap(formWithErrors)))
      },
      unwrapData => {
        val ticketID = transaction.process[blockchainTransaction.SplitUnwrap, transactionsSplitUnwrap.Request](
          entity = blockchainTransaction.SplitUnwrap(from = unwrapData.from, fromID = unwrapData.fromID, ownableID = unwrapData.ownableID, split = unwrapData.split, gas = unwrapData.gas, ticketID = "", mode = transactionMode),
          blockchainTransactionCreate = blockchainTransactionSplitUnwraps.Service.create,
          request = transactionsSplitUnwrap.Request(transactionsSplitUnwrap.Message(transactionsSplitUnwrap.BaseReq(from = unwrapData.from, gas = unwrapData.gas), fromID = unwrapData.fromID, ownableID = unwrapData.ownableID, split = unwrapData.split)),
          action = transactionsSplitUnwrap.Service.post,
          onSuccess = blockchainTransactionSplitUnwraps.Utility.onSuccess,
          onFailure = blockchainTransactionSplitUnwraps.Utility.onFailure,
          updateTransactionHash = blockchainTransactionSplitUnwraps.Service.updateTransactionHash
        )
        (for {
          ticketID <- ticketID
        } yield Ok(views.html.dashboard(successes = Seq(new Success(ticketID))))
          ).recover {
          case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
        }
      }
    )
  }

}
