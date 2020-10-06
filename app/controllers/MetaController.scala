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
class MetaController @Inject()(
                                messagesControllerComponents: MessagesControllerComponents,
                                transaction: utilities.Transaction,
                                withLoginAction: WithLoginAction,
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

  def revealForm: Action[AnyContent] = withoutLoginAction { implicit request =>
    Ok(views.html.component.blockchain.txForms.metaReveal())
  }

  def reveal: Action[AnyContent] = withoutLoginActionAsync { implicit request =>
    views.companion.blockchain.MetaReveal.form.bindFromRequest().fold(
      formWithErrors => {
        Future(BadRequest(views.html.component.blockchain.txForms.metaReveal(formWithErrors)))
      },
      revealData => {
        val ticketID = transaction.process[blockchainTransaction.MetaReveal, transactionsMetaReveal.Request](
          entity = blockchainTransaction.MetaReveal(from = revealData.from, metaFact = revealData.revealFact.toMetaFact, gas = revealData.gas, ticketID = "", mode = transactionMode),
          blockchainTransactionCreate = blockchainTransactionMetaReveals.Service.create,
          request = transactionsMetaReveal.Request(transactionsMetaReveal.Message(transactionsMetaReveal.BaseReq(from = revealData.from, gas = revealData.gas), metaFact = revealData.revealFact)),
          action = transactionsMetaReveal.Service.post,
          onSuccess = blockchainTransactionMetaReveals.Utility.onSuccess,
          onFailure = blockchainTransactionMetaReveals.Utility.onFailure,
          updateTransactionHash = blockchainTransactionMetaReveals.Service.updateTransactionHash
        )
        (for {
          ticketID <- ticketID
        } yield Ok(views.html.dashboard(successes = Seq(new Success(ticketID))))
          ).recover {
          case baseException: BaseException => InternalServerError(views.html.dashboard(failures = Seq(baseException.failure)))
        }
      }
    )
  }

}
