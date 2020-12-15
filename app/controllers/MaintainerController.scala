package controllers

import constants.Response.Success
import controllers.actions._
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.common.Serializable.{Properties, Property}
import models.{blockchainTransaction, master}
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}
import views.companion.{blockchain => blockchainCompanion}
import views.html.component.blockchain.{txForms => blockchainForms}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class MaintainerController @Inject()(
                                      messagesControllerComponents: MessagesControllerComponents,
                                      transaction: utilities.Transaction,
                                      masterAccounts: master.Accounts,
                                      withLoginAction: WithLoginAction,
                                      withUnknownLoginAction: WithUnknownLoginAction,
                                      transactionsMaintainerDeputize: transactions.blockchain.MaintainerDeputize,
                                      blockchainTransactionMaintainerDeputizes: blockchainTransaction.MaintainerDeputizes,
                                      withUserLoginAction: WithUserLoginAction,
                                      withUsernameToken: WithUsernameToken,
                                      withoutLoginAction: WithoutLoginAction,
                                      withoutLoginActionAsync: WithoutLoginActionAsync
                                    )(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_MAINTAINER

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private def getNumberOfFields(addField: Boolean, currentNumber: Int) = if (addField) currentNumber + 1 else currentNumber

  def deputizeForm: Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
    Ok(blockchainForms.maintainerDeputize())
  }

  def deputize: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      blockchainCompanion.MaintainerDeputize.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(blockchainForms.maintainerDeputize(formWithErrors)))
        },
        deputizeData => {
          if (deputizeData.addMaintainedTraits) {
            Future(PartialContent(views.html.component.blockchain.txForms.maintainerDeputize(
              maintainerDeputizeForm = views.companion.blockchain.MaintainerDeputize.form.fill(deputizeData.copy(addMaintainedTraits = false)),
              maintainedTraitsForm = getNumberOfFields(deputizeData.addMaintainedTraits, deputizeData.maintainedTraits.fold(0)(_.flatten.length)))))
          } else {
            val verifyPassword = masterAccounts.Service.validateUsernamePassword(username = loginState.username, password = deputizeData.password.getOrElse(""))

            def broadcastTx = transaction.process[blockchainTransaction.MaintainerDeputize, transactionsMaintainerDeputize.Request](
              entity = blockchainTransaction.MaintainerDeputize(from = loginState.address, fromID = deputizeData.fromID, toID = deputizeData.toID, classificationID = deputizeData.classificationID, maintainedTraits = Properties(deputizeData.maintainedTraits.fold[Seq[Property]](Seq.empty)(_.flatten.map(_.toProperty))), addMaintainer = deputizeData.addMaintainer, mutateMaintainer = deputizeData.mutateMaintainer, removeMaintainer = deputizeData.removeMaintainer, gas = deputizeData.gas, ticketID = "", mode = transactionMode),
              blockchainTransactionCreate = blockchainTransactionMaintainerDeputizes.Service.create,
              request = transactionsMaintainerDeputize.Request(transactionsMaintainerDeputize.Message(transactionsMaintainerDeputize.BaseReq(from = loginState.address, gas = deputizeData.gas), fromID = deputizeData.fromID, toID = deputizeData.toID, classificationID = deputizeData.classificationID, maintainedTraits = deputizeData.maintainedTraits.getOrElse(Seq.empty).flatten, addMaintainer = deputizeData.addMaintainer, mutateMaintainer = deputizeData.mutateMaintainer, removeMaintainer = deputizeData.removeMaintainer)),
              action = transactionsMaintainerDeputize.Service.post,
              onSuccess = blockchainTransactionMaintainerDeputizes.Utility.onSuccess,
              onFailure = blockchainTransactionMaintainerDeputizes.Utility.onFailure,
              updateTransactionHash = blockchainTransactionMaintainerDeputizes.Service.updateTransactionHash
            )

            def broadcastTxAndGetResult(verifyPassword: Boolean) = if (verifyPassword) {
              for {
                ticketID <- broadcastTx
                result <- withUsernameToken.Ok(views.html.dashboard(successes = Seq(new Success(ticketID))))
              } yield result
            } else Future(BadRequest(blockchainForms.maintainerDeputize(blockchainCompanion.MaintainerDeputize.form.fill(deputizeData).withError(constants.FormField.PASSWORD.name, constants.Response.INCORRECT_PASSWORD.message))))

            (for {
              verifyPassword <- verifyPassword
              result <- broadcastTxAndGetResult(verifyPassword)
            } yield result
              ).recover {
              case baseException: BaseException => InternalServerError(views.html.dashboard(failures = Seq(baseException.failure)))
            }
          }
        }
      )
  }

}
