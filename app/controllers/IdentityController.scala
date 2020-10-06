package controllers

import constants.Response.Success
import controllers.actions._
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.blockchainTransaction
import models.common.Serializable.{MetaProperties, MetaProperty, Properties, Property}
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class IdentityController @Inject()(
                                    blockchainTransactionIdentityDefines: blockchainTransaction.IdentityDefines,
                                    blockchainTransactionIdentityNubs: blockchainTransaction.IdentityNubs,
                                    blockchainTransactionIdentityIssues: blockchainTransaction.IdentityIssues,
                                    blockchainTransactionIdentityProvisions: blockchainTransaction.IdentityProvisions,
                                    blockchainTransactionIdentityUnprovisions: blockchainTransaction.IdentityUnprovisions,
                                    messagesControllerComponents: MessagesControllerComponents,
                                    transaction: utilities.Transaction,
                                    withLoginAction: WithLoginAction,
                                    withUnknownLoginAction: WithUnknownLoginAction,
                                    transactionsIdentityNub: transactions.blockchain.IdentityNub,
                                    transactionsIdentityDefine: transactions.blockchain.IdentityDefine,
                                    transactionsIdentityIssue: transactions.blockchain.IdentityIssue,
                                    transactionsIdentityProvision: transactions.blockchain.IdentityProvision,
                                    transactionsIdentityUnprovision: transactions.blockchain.IdentityUnprovision,
                                    withUserLoginAction: WithUserLoginAction,
                                    withUsernameToken: WithUsernameToken,
                                    withoutLoginAction: WithoutLoginAction,
                                    withoutLoginActionAsync: WithoutLoginActionAsync
                                  )(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_IDENTITY

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private def getNumberOfFields(addField: Boolean, currentNumber: Int) = if (addField) currentNumber + 1 else currentNumber

  def nubForm: Action[AnyContent] = withoutLoginAction { implicit request =>
    Ok(views.html.component.blockchain.txForms.identityNub())
  }

  def nub: Action[AnyContent] = withoutLoginActionAsync { implicit request =>
    views.companion.blockchain.IdentityNub.form.bindFromRequest().fold(
      formWithErrors => {
        Future(BadRequest(views.html.component.blockchain.txForms.identityNub(formWithErrors)))
      },
      nubData => {
        val ticketID = transaction.process[blockchainTransaction.IdentityNub, transactionsIdentityNub.Request](
          entity = blockchainTransaction.IdentityNub(from = nubData.from, nubID = nubData.nubID, gas = nubData.gas, ticketID = "", mode = transactionMode),
          blockchainTransactionCreate = blockchainTransactionIdentityNubs.Service.create,
          request = transactionsIdentityNub.Request(transactionsIdentityNub.Message(transactionsIdentityNub.BaseReq(from = nubData.from, gas = nubData.gas), nubID = nubData.nubID)),
          action = transactionsIdentityNub.Service.post,
          onSuccess = blockchainTransactionIdentityNubs.Utility.onSuccess,
          onFailure = blockchainTransactionIdentityNubs.Utility.onFailure,
          updateTransactionHash = blockchainTransactionIdentityNubs.Service.updateTransactionHash
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

  def defineForm: Action[AnyContent] = withoutLoginAction { implicit request =>
    Ok(views.html.component.blockchain.txForms.identityDefine())
  }

  def define: Action[AnyContent] = withoutLoginActionAsync { implicit request =>
    views.companion.blockchain.IdentityDefine.form.bindFromRequest().fold(
      formWithErrors => {
        Future(BadRequest(views.html.component.blockchain.txForms.identityDefine(formWithErrors)))
      },
      defineData => {
        if (defineData.addImmutableMetaField || defineData.addImmutableField || defineData.addMutableMetaField || defineData.addMutableField) {
          Future(PartialContent(views.html.component.blockchain.txForms.identityDefine(
            identityDefineForm = views.companion.blockchain.IdentityDefine.form.fill(defineData.copy(addImmutableMetaField = false, addImmutableField = false, addMutableMetaField = false, addMutableField = false)),
            numImmutableMetaForms = getNumberOfFields(defineData.addImmutableMetaField, defineData.immutableMetaTraits.fold(0)(_.flatten.length)),
            numImmutableForms = getNumberOfFields(defineData.addImmutableField, defineData.immutableTraits.fold(0)(_.flatten.length)),
            numMutableMetaForms = getNumberOfFields(defineData.addMutableMetaField, defineData.mutableMetaTraits.fold(0)(_.flatten.length)),
            numMutableForms = getNumberOfFields(defineData.addMutableField, defineData.mutableTraits.fold(0)(_.flatten.length)))))
        } else {
          val ticketID = transaction.process[blockchainTransaction.IdentityDefine, transactionsIdentityDefine.Request](
            entity = blockchainTransaction.IdentityDefine(from = defineData.from, fromID = defineData.fromID, immutableMetaTraits = MetaProperties(defineData.immutableMetaTraits.fold[Seq[MetaProperty]](Seq.empty)(_.flatten.map(_.toMetaProperty))), immutableTraits = Properties(defineData.immutableTraits.fold[Seq[Property]](Seq.empty)(_.flatten.map(_.toProperty))), mutableMetaTraits = MetaProperties(defineData.mutableMetaTraits.fold[Seq[MetaProperty]](Seq.empty)(_.flatten.map(_.toMetaProperty))), mutableTraits = Properties(defineData.mutableTraits.fold[Seq[Property]](Seq.empty)(_.flatten.map(_.toProperty))), gas = defineData.gas, ticketID = "", mode = transactionMode),
            blockchainTransactionCreate = blockchainTransactionIdentityDefines.Service.create,
            request = transactionsIdentityDefine.Request(transactionsIdentityDefine.Message(transactionsIdentityDefine.BaseReq(from = defineData.from, gas = defineData.gas), fromID = defineData.fromID, immutableMetaTraits = defineData.immutableMetaTraits.getOrElse(Seq.empty).flatten, immutableTraits = defineData.immutableTraits.getOrElse(Seq.empty).flatten, mutableMetaTraits = defineData.mutableMetaTraits.getOrElse(Seq.empty).flatten, mutableTraits = defineData.mutableTraits.getOrElse(Seq.empty).flatten)),
            action = transactionsIdentityDefine.Service.post,
            onSuccess = blockchainTransactionIdentityDefines.Utility.onSuccess,
            onFailure = blockchainTransactionIdentityDefines.Utility.onFailure,
            updateTransactionHash = blockchainTransactionIdentityDefines.Service.updateTransactionHash
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

  def issueForm: Action[AnyContent] = withoutLoginAction { implicit request =>
    Ok(views.html.component.blockchain.txForms.identityIssue())
  }

  def issue: Action[AnyContent] = withoutLoginActionAsync { implicit request =>
    views.companion.blockchain.IdentityIssue.form.bindFromRequest().fold(
      formWithErrors => {
        Future(BadRequest(views.html.component.blockchain.txForms.identityIssue(formWithErrors)))
      },
      issueData => {
        if (issueData.addImmutableMetaField || issueData.addImmutableField || issueData.addMutableMetaField || issueData.addMutableField) {
          Future(PartialContent(views.html.component.blockchain.txForms.identityIssue(
            identityIssueForm = views.companion.blockchain.IdentityIssue.form.fill(issueData.copy(addImmutableMetaField = false, addImmutableField = false, addMutableMetaField = false, addMutableField = false)),
            numImmutableMetaForms = getNumberOfFields(issueData.addImmutableMetaField, issueData.immutableMetaProperties.fold(0)(_.flatten.length)),
            numImmutableForms = getNumberOfFields(issueData.addImmutableField, issueData.immutableProperties.fold(0)(_.flatten.length)),
            numMutableMetaForms = getNumberOfFields(issueData.addMutableMetaField, issueData.mutableMetaProperties.fold(0)(_.flatten.length)),
            numMutableForms = getNumberOfFields(issueData.addMutableField, issueData.mutableProperties.fold(0)(_.flatten.length)))))
        } else {
          val ticketID = transaction.process[blockchainTransaction.IdentityIssue, transactionsIdentityIssue.Request](
            entity = blockchainTransaction.IdentityIssue(from = issueData.from, fromID = issueData.fromID, classificationID = issueData.classificationID, to = issueData.to, immutableMetaTraits = MetaProperties(issueData.immutableMetaProperties.fold[Seq[MetaProperty]](Seq.empty)(_.flatten.map(_.toMetaProperty))), immutableTraits = Properties(issueData.immutableProperties.fold[Seq[Property]](Seq.empty)(_.flatten.map(_.toProperty))), mutableMetaTraits = MetaProperties(issueData.mutableMetaProperties.fold[Seq[MetaProperty]](Seq.empty)(_.flatten.map(_.toMetaProperty))), mutableTraits = Properties(issueData.mutableProperties.fold[Seq[Property]](Seq.empty)(_.flatten.map(_.toProperty))), gas = issueData.gas, ticketID = "", mode = transactionMode),
            blockchainTransactionCreate = blockchainTransactionIdentityIssues.Service.create,
            request = transactionsIdentityIssue.Request(transactionsIdentityIssue.Message(transactionsIdentityIssue.BaseReq(from = issueData.from, gas = issueData.gas), fromID = issueData.fromID, classificationID = issueData.classificationID, to = issueData.to, immutableMetaProperties = issueData.immutableMetaProperties.getOrElse(Seq.empty).flatten, immutableProperties = issueData.immutableProperties.getOrElse(Seq.empty).flatten, mutableMetaProperties = issueData.mutableMetaProperties.getOrElse(Seq.empty).flatten, mutableProperties = issueData.mutableProperties.getOrElse(Seq.empty).flatten)),
            action = transactionsIdentityIssue.Service.post,
            onSuccess = blockchainTransactionIdentityIssues.Utility.onSuccess,
            onFailure = blockchainTransactionIdentityIssues.Utility.onFailure,
            updateTransactionHash = blockchainTransactionIdentityIssues.Service.updateTransactionHash
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

  def provisionForm: Action[AnyContent] = withoutLoginAction { implicit request =>
    Ok(views.html.component.blockchain.txForms.identityProvision())
  }

  def provision: Action[AnyContent] = withoutLoginActionAsync { implicit request =>
    views.companion.blockchain.IdentityProvision.form.bindFromRequest().fold(
      formWithErrors => {
        Future(BadRequest(views.html.component.blockchain.txForms.identityProvision(formWithErrors)))
      },
      provisionData => {
        val ticketID = transaction.process[blockchainTransaction.IdentityProvision, transactionsIdentityProvision.Request](
          entity = blockchainTransaction.IdentityProvision(from = provisionData.from, to = provisionData.to, identityID = provisionData.identityID, gas = provisionData.gas, ticketID = "", mode = transactionMode),
          blockchainTransactionCreate = blockchainTransactionIdentityProvisions.Service.create,
          request = transactionsIdentityProvision.Request(transactionsIdentityProvision.Message(transactionsIdentityProvision.BaseReq(from = provisionData.from, gas = provisionData.gas), to = provisionData.to, identityID = provisionData.identityID)),
          action = transactionsIdentityProvision.Service.post,
          onSuccess = blockchainTransactionIdentityProvisions.Utility.onSuccess,
          onFailure = blockchainTransactionIdentityProvisions.Utility.onFailure,
          updateTransactionHash = blockchainTransactionIdentityProvisions.Service.updateTransactionHash
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

  def unprovisionForm: Action[AnyContent] = withoutLoginAction { implicit request =>
    Ok(views.html.component.blockchain.txForms.identityUnprovision())
  }

  def unprovision: Action[AnyContent] = withoutLoginActionAsync { implicit request =>
    views.companion.blockchain.IdentityUnprovision.form.bindFromRequest().fold(
      formWithErrors => {
        Future(BadRequest(views.html.component.blockchain.txForms.identityUnprovision(formWithErrors)))
      },
      unprovisionData => {
        val ticketID = transaction.process[blockchainTransaction.IdentityUnprovision, transactionsIdentityUnprovision.Request](
          entity = blockchainTransaction.IdentityUnprovision(from = unprovisionData.from, to = unprovisionData.to, identityID = unprovisionData.identityID, gas = unprovisionData.gas, ticketID = "", mode = transactionMode),
          blockchainTransactionCreate = blockchainTransactionIdentityUnprovisions.Service.create,
          request = transactionsIdentityUnprovision.Request(transactionsIdentityUnprovision.Message(transactionsIdentityUnprovision.BaseReq(from = unprovisionData.from, gas = unprovisionData.gas), to = unprovisionData.to, identityID = unprovisionData.identityID)),
          action = transactionsIdentityUnprovision.Service.post,
          onSuccess = blockchainTransactionIdentityUnprovisions.Utility.onSuccess,
          onFailure = blockchainTransactionIdentityUnprovisions.Utility.onFailure,
          updateTransactionHash = blockchainTransactionIdentityUnprovisions.Service.updateTransactionHash
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
