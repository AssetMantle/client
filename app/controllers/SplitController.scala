package controllers

import constants.Response.Success
import controllers.actions._
import controllers.results.WithUsernameToken
import constants.AppConfig._
import exceptions.BaseException
import models.{blockchainTransaction, master}
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}
import views.companion.{blockchain => blockchainCompanion}
import views.html.component.blockchain.{txForms => blockchainForms}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SplitController @Inject()(
                                 messagesControllerComponents: MessagesControllerComponents,
                                 transaction: utilities.Transaction,
                                 masterAccounts: master.Accounts,
                                 withLoginActionAsync: WithLoginActionAsync,
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

  def sendForm(ownableID: String, fromID: String): Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
      Ok(blockchainForms.splitSend(ownableID = ownableID, fromID = fromID))
  }

  def send: Action[AnyContent] = withLoginActionAsync { implicit loginState =>
    implicit request =>
      blockchainCompanion.SplitSend.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(blockchainForms.splitSend(formWithErrors, formWithErrors.data(constants.FormField.OWNABLE_ID.name), formWithErrors.data(constants.FormField.FROM_ID.name))))
        },
        sendData => {
          val verifyPassword = masterAccounts.Service.validateUsernamePassword(username = loginState.username, password = sendData.password)

          def broadcastTx = transaction.process[blockchainTransaction.SplitSend, transactionsSplitSend.Request](
            entity = blockchainTransaction.SplitSend(from = loginState.address, fromID = sendData.fromID, toID = sendData.toID, ownableID = sendData.ownableID, split = sendData.split, gas = sendData.gas, ticketID = "", mode = transactionMode),
            blockchainTransactionCreate = blockchainTransactionSplitSends.Service.create,
            request = transactionsSplitSend.Request(transactionsSplitSend.Message(transactionsSplitSend.BaseReq(from = loginState.address, gas = sendData.gas), fromID = sendData.fromID, toID = sendData.toID, ownableID = sendData.ownableID, split = sendData.split)),
            action = transactionsSplitSend.Service.post,
            onSuccess = blockchainTransactionSplitSends.Utility.onSuccess,
            onFailure = blockchainTransactionSplitSends.Utility.onFailure,
            updateTransactionHash = blockchainTransactionSplitSends.Service.updateTransactionHash
          )

          def broadcastTxAndGetResult(verifyPassword: Boolean) = if (verifyPassword) {
            for {
              ticketID <- broadcastTx
              result <- withUsernameToken.Ok(views.html.index(successes = Seq(new Success(ticketID))))
            } yield result
          } else Future(BadRequest(blockchainForms.splitSend(blockchainCompanion.SplitSend.form.fill(sendData).withError(constants.FormField.PASSWORD.name, constants.Response.INCORRECT_PASSWORD.message), sendData.ownableID, sendData.fromID)))

          (for {
            verifyPassword <- verifyPassword
            result <- broadcastTxAndGetResult(verifyPassword)
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def wrapForm: Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
      Ok(blockchainForms.splitWrap())
  }

  def wrap: Action[AnyContent] = withLoginActionAsync { implicit loginState =>
    implicit request =>
      blockchainCompanion.SplitWrap.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(blockchainForms.splitWrap(formWithErrors)))
        },
        wrapData => {
          if (wrapData.addField) {
            Future(PartialContent(blockchainForms.splitWrap(splitWrapForm = blockchainCompanion.SplitWrap.form.fill(wrapData.copy(addField = false)))))
          } else {
            val verifyPassword = masterAccounts.Service.validateUsernamePassword(username = loginState.username, password = wrapData.password)

            def broadcastTx = transaction.process[blockchainTransaction.SplitWrap, transactionsSplitWrap.Request](
              entity = blockchainTransaction.SplitWrap(from = loginState.address, fromID = wrapData.fromID, coins = wrapData.coins.flatten.map(_.toCoin), gas = wrapData.gas, ticketID = "", mode = transactionMode),
              blockchainTransactionCreate = blockchainTransactionSplitWraps.Service.create,
              request = transactionsSplitWrap.Request(transactionsSplitWrap.Message(transactionsSplitWrap.BaseReq(from = loginState.address, gas = wrapData.gas), fromID = wrapData.fromID, coins = wrapData.coins.flatten.map(_.toCoin))),
              action = transactionsSplitWrap.Service.post,
              onSuccess = blockchainTransactionSplitWraps.Utility.onSuccess,
              onFailure = blockchainTransactionSplitWraps.Utility.onFailure,
              updateTransactionHash = blockchainTransactionSplitWraps.Service.updateTransactionHash
            )

            def broadcastTxAndGetResult(verifyPassword: Boolean) = if (verifyPassword) {
              for {
                ticketID <- broadcastTx
                result <- withUsernameToken.Ok(views.html.index(successes = Seq(new Success(ticketID))))
              } yield result
            } else Future(BadRequest(blockchainForms.splitWrap(blockchainCompanion.SplitWrap.form.fill(wrapData).withError(constants.FormField.PASSWORD.name, constants.Response.INCORRECT_PASSWORD.message))))

            (for {
              verifyPassword <- verifyPassword
              result <- broadcastTxAndGetResult(verifyPassword)
            } yield result
              ).recover {
              case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
            }
          }
        }
      )
  }

  def unwrapForm(ownableID: String, fromID: String): Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
      Ok(blockchainForms.splitUnwrap(ownableID = ownableID, fromID = fromID))
  }

  def unwrap: Action[AnyContent] = withLoginActionAsync { implicit loginState =>
    implicit request =>
      blockchainCompanion.SplitUnwrap.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(blockchainForms.splitUnwrap(formWithErrors, formWithErrors.data(constants.FormField.OWNABLE_ID.name), formWithErrors.data(constants.FormField.FROM_ID.name))))
        },
        unwrapData => {
          val verifyPassword = masterAccounts.Service.validateUsernamePassword(username = loginState.username, password = unwrapData.password)

          def broadcastTx = transaction.process[blockchainTransaction.SplitUnwrap, transactionsSplitUnwrap.Request](
            entity = blockchainTransaction.SplitUnwrap(from = loginState.address, fromID = unwrapData.fromID, ownableID = unwrapData.ownableID, split = unwrapData.split, gas = unwrapData.gas, ticketID = "", mode = transactionMode),
            blockchainTransactionCreate = blockchainTransactionSplitUnwraps.Service.create,
            request = transactionsSplitUnwrap.Request(transactionsSplitUnwrap.Message(transactionsSplitUnwrap.BaseReq(from = loginState.address, gas = unwrapData.gas), fromID = unwrapData.fromID, ownableID = unwrapData.ownableID, split = unwrapData.split)),
            action = transactionsSplitUnwrap.Service.post,
            onSuccess = blockchainTransactionSplitUnwraps.Utility.onSuccess,
            onFailure = blockchainTransactionSplitUnwraps.Utility.onFailure,
            updateTransactionHash = blockchainTransactionSplitUnwraps.Service.updateTransactionHash
          )

          def broadcastTxAndGetResult(verifyPassword: Boolean) = if (verifyPassword) {
            for {
              ticketID <- broadcastTx
              result <- withUsernameToken.Ok(views.html.index(successes = Seq(new Success(ticketID))))
            } yield result
          } else Future(BadRequest(blockchainForms.splitUnwrap(blockchainCompanion.SplitUnwrap.form.fill(unwrapData).withError(constants.FormField.PASSWORD.name, constants.Response.INCORRECT_PASSWORD.message), unwrapData.ownableID, unwrapData.fromID)))

          (for {
            verifyPassword <- verifyPassword
            result <- broadcastTxAndGetResult(verifyPassword)
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

}
