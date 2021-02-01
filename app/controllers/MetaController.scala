package controllers

import constants.Response.Success
import controllers.actions._
import controllers.results.WithUsernameToken
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
class MetaController @Inject()(
                                messagesControllerComponents: MessagesControllerComponents,
                                transaction: utilities.Transaction,
                                withLoginActionAsync: WithLoginActionAsync,
                                masterAccounts: master.Accounts,
                                withUnknownLoginAction: WithUnknownLoginAction,
                                transactionsMetaReveal: transactions.blockchain.MetaReveal,
                                blockchainTransactionMetaReveals: blockchainTransaction.MetaReveals,
                                withUserLoginAction: WithUserLoginAction,
                                withUsernameToken: WithUsernameToken,
                                withoutLoginAction: WithoutLoginAction,
                                withoutLoginActionAsync: WithoutLoginActionAsync
                              )(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_META

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  def revealForm(): Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
      Ok(blockchainForms.metaReveal())
  }

  def reveal: Action[AnyContent] = withLoginActionAsync { implicit loginState =>
    implicit request =>
      blockchainCompanion.MetaReveal.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(blockchainForms.metaReveal(formWithErrors)))
        },
        revealData => {
          val verifyPassword = masterAccounts.Service.validateUsernamePassword(username = loginState.username, password = revealData.password)

          def broadcastTx = transaction.process[blockchainTransaction.MetaReveal, transactionsMetaReveal.Request](
            entity = blockchainTransaction.MetaReveal(from = loginState.address, metaFact = revealData.revealFact.toMetaFact, gas = revealData.gas, ticketID = "", mode = transactionMode),
            blockchainTransactionCreate = blockchainTransactionMetaReveals.Service.create,
            request = transactionsMetaReveal.Request(transactionsMetaReveal.Message(transactionsMetaReveal.BaseReq(from = loginState.address, gas = revealData.gas), metaFact = revealData.revealFact)),
            action = transactionsMetaReveal.Service.post,
            onSuccess = blockchainTransactionMetaReveals.Utility.onSuccess,
            onFailure = blockchainTransactionMetaReveals.Utility.onFailure,
            updateTransactionHash = blockchainTransactionMetaReveals.Service.updateTransactionHash
          )

          def broadcastTxAndGetResult(verifyPassword: Boolean) = if (verifyPassword) {
            for {
              ticketID <- broadcastTx
              result <- withUsernameToken.Ok(views.html.dashboard(successes = Seq(new Success(ticketID))))
            } yield result
          } else Future(BadRequest(blockchainForms.metaReveal(blockchainCompanion.MetaReveal.form.fill(revealData).withError(constants.FormField.PASSWORD.name, constants.Response.INCORRECT_PASSWORD.message))))

          (for {
            verifyPassword <- verifyPassword
            result <- broadcastTxAndGetResult(verifyPassword)
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.dashboard(failures = Seq(baseException.failure)))
          }
        }
      )
  }

}
