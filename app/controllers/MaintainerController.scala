package controllers

import constants.Response.Success
import controllers.actions._
import controllers.results.WithUsernameToken
import exceptions.BaseException
import models.common.Serializable.BaseProperty
import models.{blockchain, blockchainTransaction, master}
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}
import utilities.MicroNumber
import views.companion.{blockchain => blockchainCompanion}
import views.html.component.blockchain.{txForms => blockchainForms}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class MaintainerController @Inject()(
                                      messagesControllerComponents: MessagesControllerComponents,
                                      transaction: utilities.Transaction,
                                      masterAccounts: master.Accounts,
                                      masterProperties: master.Properties,
                                      masterClassifications: master.Classifications,
                                      withLoginActionAsync: WithLoginActionAsync,
                                      blockchainIdentities: blockchain.Identities,
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

  def deputizeForm(classificationID: String, entityType: String): Action[AnyContent] = withLoginActionAsync { implicit loginState =>
    implicit request =>
      val properties = masterProperties.Service.getAll(entityID = classificationID, entityType = entityType)
      val maintainerIDs = masterClassifications.Service.getMaintainerIDs(classificationID)
      val identityIDs = blockchainIdentities.Service.getAllIDsByProvisioned(loginState.address)

      (for {
        properties <- properties
        maintainerIDs <- maintainerIDs
        identityIDs <- identityIDs
      } yield {
        if (properties.nonEmpty && maintainerIDs.intersect(identityIDs).nonEmpty) {
          val mutables = Option(properties.filter(_.isMutable).map(x => Option(views.companion.common.Property.Data(dataType = x.dataType, dataName = x.name, dataValue = x.value))))
          Ok(blockchainForms.maintainerDeputize(blockchainCompanion.MaintainerDeputize.form.fill(blockchainCompanion.MaintainerDeputize.Data(fromID = maintainerIDs.intersect(identityIDs).headOption.getOrElse(""), classificationID = classificationID, toID = "", addMaintainer = false, removeMaintainer = false, mutateMaintainer = false, maintainedTraits = mutables, addMaintainedTraits = false, gas = MicroNumber.zero, password = None)), classificationID = classificationID, fromID = identityIDs.intersect(maintainerIDs).head, numMaintainedTraitsForm = mutables.fold(0)(_.length)))
        } else {
          Ok(blockchainForms.maintainerDeputize(classificationID = classificationID, fromID = maintainerIDs.intersect(identityIDs).headOption.getOrElse("")))
        }
      }).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def deputize: Action[AnyContent] = withLoginActionAsync { implicit loginState =>
    implicit request =>
      blockchainCompanion.MaintainerDeputize.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(blockchainForms.maintainerDeputize(formWithErrors, formWithErrors.data.getOrElse(constants.FormField.CLASSIFICATION_ID.name, ""), formWithErrors.data.getOrElse(constants.FormField.FROM_ID.name, ""), formWithErrors.value.fold(0)(x => x.maintainedTraits.fold(0)(x => x.flatten.length)))))
        },
        deputizeData => {
          if (deputizeData.addMaintainedTraits) {
            Future(PartialContent(views.html.component.blockchain.txForms.maintainerDeputize(
              maintainerDeputizeForm = views.companion.blockchain.MaintainerDeputize.form.fill(deputizeData.copy(addMaintainedTraits = false)),
              classificationID = deputizeData.classificationID,
              fromID = deputizeData.fromID,
              numMaintainedTraitsForm = getNumberOfFields(deputizeData.addMaintainedTraits, deputizeData.maintainedTraits.fold(0)(_.flatten.length)))))
          } else {
            val verifyPassword = masterAccounts.Service.validateUsernamePassword(username = loginState.username, password = deputizeData.password.getOrElse(""))

            def broadcastTx = transaction.process[blockchainTransaction.MaintainerDeputize, transactionsMaintainerDeputize.Request](
              entity = blockchainTransaction.MaintainerDeputize(from = loginState.address, fromID = deputizeData.fromID, toID = deputizeData.toID, classificationID = deputizeData.classificationID, maintainedTraits = deputizeData.maintainedTraits.fold[Seq[BaseProperty]](Seq.empty)(_.flatten.map(_.toBaseProperty)), addMaintainer = deputizeData.addMaintainer, mutateMaintainer = deputizeData.mutateMaintainer, removeMaintainer = deputizeData.removeMaintainer, gas = deputizeData.gas, ticketID = "", mode = transactionMode),
              blockchainTransactionCreate = blockchainTransactionMaintainerDeputizes.Service.create,
              request = transactionsMaintainerDeputize.Request(transactionsMaintainerDeputize.Message(transactionsMaintainerDeputize.BaseReq(from = loginState.address, gas = deputizeData.gas), fromID = deputizeData.fromID, toID = deputizeData.toID, classificationID = deputizeData.classificationID, maintainedTraits = deputizeData.maintainedTraits.getOrElse(Seq.empty).flatten.map(_.toBaseProperty), addMaintainer = deputizeData.addMaintainer, mutateMaintainer = deputizeData.mutateMaintainer, removeMaintainer = deputizeData.removeMaintainer)),
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
            } else Future(BadRequest(blockchainForms.maintainerDeputize(blockchainCompanion.MaintainerDeputize.form.fill(deputizeData).withError(constants.FormField.PASSWORD.name, constants.Response.INCORRECT_PASSWORD.message), deputizeData.classificationID, deputizeData.fromID, deputizeData.maintainedTraits.fold(0)(_.flatten.length))))

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
