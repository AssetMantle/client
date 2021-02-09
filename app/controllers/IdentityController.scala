package controllers

import constants.Response.Success
import controllers.actions.{WithGenesisLoginAction, _}
import controllers.results.WithUsernameToken
import exceptions.BaseException
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
class IdentityController @Inject()(
                                    blockchainTransactionIdentityDefines: blockchainTransaction.IdentityDefines,
                                    blockchainTransactionIdentityNubs: blockchainTransaction.IdentityNubs,
                                    blockchainTransactionIdentityIssues: blockchainTransaction.IdentityIssues,
                                    blockchainTransactionIdentityProvisions: blockchainTransaction.IdentityProvisions,
                                    blockchainTransactionIdentityUnprovisions: blockchainTransaction.IdentityUnprovisions,
                                    blockchainMetas: blockchain.Metas,
                                    blockchainIdentities: blockchain.Identities,
                                    blockchainClassifications: blockchain.Classifications,
                                    messagesControllerComponents: MessagesControllerComponents,
                                    transaction: utilities.Transaction,
                                    withLoginActionAsync: WithLoginActionAsync,
                                    withUnknownLoginAction: WithUnknownLoginAction,
                                    transactionsIdentityNub: transactions.blockchain.IdentityNub,
                                    transactionsIdentityDefine: transactions.blockchain.IdentityDefine,
                                    transactionsIdentityIssue: transactions.blockchain.IdentityIssue,
                                    transactionsIdentityProvision: transactions.blockchain.IdentityProvision,
                                    transactionsIdentityUnprovision: transactions.blockchain.IdentityUnprovision,
                                    withUserLoginAction: WithUserLoginAction,
                                    withUsernameToken: WithUsernameToken,
                                    masterProperties: master.Properties,
                                    masterIdentities: master.Identities,
                                    masterClassifications: master.Classifications,
                                    masterAccounts: master.Accounts,
                                    withoutLoginAction: WithoutLoginAction,
                                    withoutLoginActionAsync: WithoutLoginActionAsync,
                                    withGenesisLoginAction: WithGenesisLoginAction
                                  )(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_IDENTITY

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private def getNumberOfFields(addField: Boolean, currentNumber: Int) = if (addField) currentNumber + 1 else currentNumber

  def nubForm: Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
      Ok(blockchainForms.identityNub(nubID=constants.Blockchain.Parameters.MAIN_NUB_ID))
  }

  def nub: Action[AnyContent] = withGenesisLoginAction { implicit loginState =>
    implicit request =>
      views.companion.blockchain.IdentityNub.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(blockchainForms.identityNub(formWithErrors,constants.Blockchain.Parameters.MAIN_NUB_ID)))
        },
        nubData => {
          val verifyPassword = masterAccounts.Service.validateUsernamePassword(username = loginState.username, password = nubData.password)

          def broadcastTxAndGetResult(verifyPassword: Boolean) = if (verifyPassword) {
            val broadcastTx = transaction.process[blockchainTransaction.IdentityNub, transactionsIdentityNub.Request](
              entity = blockchainTransaction.IdentityNub(from = loginState.address, nubID = nubData.nubID, gas = nubData.gas, ticketID = "", mode = transactionMode),
              blockchainTransactionCreate = blockchainTransactionIdentityNubs.Service.create,
              request = transactionsIdentityNub.Request(transactionsIdentityNub.Message(transactionsIdentityNub.BaseReq(from = loginState.address, gas = nubData.gas), nubID = nubData.nubID)),
              action = transactionsIdentityNub.Service.post,
              onSuccess = blockchainTransactionIdentityNubs.Utility.onSuccess,
              onFailure = blockchainTransactionIdentityNubs.Utility.onFailure,
              updateTransactionHash = blockchainTransactionIdentityNubs.Service.updateTransactionHash)
            for {
              ticketID <- broadcastTx
              result <- withUsernameToken.Ok(views.html.identity(successes = Seq(new Success(ticketID))))
            } yield result
          } else Future(BadRequest(blockchainForms.identityNub(blockchainCompanion.IdentityNub.form.fill(nubData).withError(constants.FormField.PASSWORD.name, constants.Response.INCORRECT_PASSWORD.message),constants.Blockchain.Parameters.MAIN_NUB_ID)))

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

  def defineForm: Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
      Ok(blockchainForms.identityDefine())
  }

  def define: Action[AnyContent] = withGenesisLoginAction { implicit loginState =>
    implicit request =>
      views.companion.blockchain.IdentityDefine.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(blockchainForms.identityDefine(formWithErrors)))
        },
        defineData => {
          if (defineData.addImmutableMetaField || defineData.addImmutableField || defineData.addMutableMetaField || defineData.addMutableField) {
            Future(PartialContent(blockchainForms.identityDefine(
              identityDefineForm = views.companion.blockchain.IdentityDefine.form.fill(defineData.copy(addImmutableMetaField = false, addImmutableField = false, addMutableMetaField = false, addMutableField = false)),
              numImmutableMetaForms = getNumberOfFields(defineData.addImmutableMetaField, defineData.immutableMetaTraits.fold(0)(_.flatten.length)),
              numImmutableForms = getNumberOfFields(defineData.addImmutableField, defineData.immutableTraits.fold(0)(_.flatten.length)),
              numMutableMetaForms = getNumberOfFields(defineData.addMutableMetaField, defineData.mutableMetaTraits.fold(0)(_.flatten.length)),
              numMutableForms = getNumberOfFields(defineData.addMutableField, defineData.mutableTraits.fold(0)(_.flatten.length)))))
          } else {
            val verifyPassword = masterAccounts.Service.validateUsernamePassword(username = loginState.username, password = defineData.password.getOrElse(""))

            val immutableMetas = defineData.immutableMetaTraits.getOrElse(Seq.empty).flatten.map(_.toBaseProperty)
            val immutables = defineData.immutableTraits.getOrElse(Seq.empty).flatten.map(_.toBaseProperty)
            val mutableMetas = defineData.mutableMetaTraits.getOrElse(Seq.empty).flatten.map(_.toBaseProperty)
            val mutables = defineData.mutableTraits.getOrElse(Seq.empty).flatten.map(_.toBaseProperty)

            def broadcastTxAndGetResult(verifyPassword: Boolean) = if (verifyPassword) {
              val broadcastTx = transaction.process[blockchainTransaction.IdentityDefine, transactionsIdentityDefine.Request](
                entity = blockchainTransaction.IdentityDefine(from = loginState.address, fromID = defineData.fromID, immutableMetaTraits = immutableMetas, immutableTraits = immutables, mutableMetaTraits = mutableMetas, mutableTraits = mutables, gas = defineData.gas, ticketID = "", mode = transactionMode),
                blockchainTransactionCreate = blockchainTransactionIdentityDefines.Service.create,
                request = transactionsIdentityDefine.Request(transactionsIdentityDefine.Message(transactionsIdentityDefine.BaseReq(from = loginState.address, gas = defineData.gas), fromID = defineData.fromID, immutableMetaTraits = immutableMetas, immutableTraits = immutables, mutableMetaTraits = mutableMetas, mutableTraits = mutables)),
                action = transactionsIdentityDefine.Service.post,
                onSuccess = blockchainTransactionIdentityDefines.Utility.onSuccess,
                onFailure = blockchainTransactionIdentityDefines.Utility.onFailure,
                updateTransactionHash = blockchainTransactionIdentityDefines.Service.updateTransactionHash)

              for {
                ticketID <- broadcastTx
                result <- withUsernameToken.Ok(views.html.identity(successes = Seq(new Success(ticketID))))
              } yield result
            } else Future(BadRequest(blockchainForms.identityDefine(blockchainCompanion.IdentityDefine.form.fill(defineData).withError(constants.FormField.PASSWORD.name, constants.Response.INCORRECT_PASSWORD.message))))

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

  def issueForm(classificationID: String): Action[AnyContent] = withLoginActionAsync { implicit loginState =>
    implicit request =>
      val properties = masterProperties.Service.getAll(entityID = classificationID, entityType = constants.Blockchain.Entity.IDENTITY_DEFINITION)
      val maintainerIDs = masterClassifications.Service.getMaintainerIDs(classificationID)
      val identityIDs = blockchainIdentities.Service.getAllIDsByProvisioned(loginState.address)

      (for {
        properties <- properties
        maintainerIDs <- maintainerIDs
        identityIDs <- identityIDs
      } yield {
        if (properties.nonEmpty && maintainerIDs.intersect(identityIDs).nonEmpty) {
          val immutableMetaProperties = Option(properties.filter(x => x.isMeta && !x.isMutable).map(x => Option(views.companion.common.Property.Data(dataType = x.dataType, dataName = x.name, dataValue = x.value))))
          val immutableProperties = Option(properties.filter(x => !x.isMeta && !x.isMutable).map(x => Option(views.companion.common.Property.Data(dataType = x.dataType, dataName = x.name, dataValue = x.value))))
          val mutableMetaProperties = Option(properties.filter(x => x.isMeta && x.isMutable).map(x => Option(views.companion.common.Property.Data(dataType = x.dataType, dataName = x.name, dataValue = x.value))))
          val mutableProperties = Option(properties.filter(x => !x.isMeta && x.isMutable).map(x => Option(views.companion.common.Property.Data(dataType = x.dataType, dataName = x.name, dataValue = x.value))))
          Ok(blockchainForms.identityIssue(blockchainCompanion.IdentityIssue.form.fill(blockchainCompanion.IdentityIssue.Data(fromID = maintainerIDs.intersect(identityIDs).headOption.getOrElse(""), classificationID = classificationID, to = "", immutableMetaProperties = immutableMetaProperties, addImmutableMetaField = false, immutableProperties = immutableProperties, addImmutableField = false, mutableMetaProperties = mutableMetaProperties, addMutableMetaField = false, mutableProperties = mutableProperties, addMutableField = false, gas = MicroNumber.zero, password = None)), classificationID = classificationID, numImmutableMetaForms = immutableMetaProperties.fold(0)(_.length), numImmutableForms = immutableProperties.fold(0)(_.length), numMutableMetaForms = mutableMetaProperties.fold(0)(_.length), numMutableForms = mutableProperties.fold(0)(_.length)))
        } else {
          Ok(blockchainForms.identityIssue(classificationID = classificationID))
        }
      }
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def issue: Action[AnyContent] = withLoginActionAsync { implicit loginState =>
    implicit request =>
      views.companion.blockchain.IdentityIssue.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(blockchainForms.identityIssue(formWithErrors, formWithErrors.data.getOrElse(constants.FormField.CLASSIFICATION_ID.name, ""))))
        },
        issueData => {
          if (issueData.addImmutableMetaField || issueData.addImmutableField || issueData.addMutableMetaField || issueData.addMutableField) {
            Future(PartialContent(blockchainForms.identityIssue(
              identityIssueForm = views.companion.blockchain.IdentityIssue.form.fill(issueData.copy(addImmutableMetaField = false, addImmutableField = false, addMutableMetaField = false, addMutableField = false)),
              classificationID = issueData.classificationID,
              numImmutableMetaForms = getNumberOfFields(issueData.addImmutableMetaField, issueData.immutableMetaProperties.fold(0)(_.flatten.length)),
              numImmutableForms = getNumberOfFields(issueData.addImmutableField, issueData.immutableProperties.fold(0)(_.flatten.length)),
              numMutableMetaForms = getNumberOfFields(issueData.addMutableMetaField, issueData.mutableMetaProperties.fold(0)(_.flatten.length)),
              numMutableForms = getNumberOfFields(issueData.addMutableField, issueData.mutableProperties.fold(0)(_.flatten.length)))))
          } else {
            val verifyPassword = masterAccounts.Service.validateUsernamePassword(username = loginState.username, password = issueData.password.getOrElse(""))
            val immutableMetas = issueData.immutableMetaProperties.getOrElse(Seq.empty).flatten.map(_.toBaseProperty)
            val immutables = issueData.immutableProperties.getOrElse(Seq.empty).flatten.map(_.toBaseProperty)
            val mutableMetas = issueData.mutableMetaProperties.getOrElse(Seq.empty).flatten.map(_.toBaseProperty)
            val mutables = issueData.mutableProperties.getOrElse(Seq.empty).flatten.map(_.toBaseProperty)

            def broadcastTxAndGetResult(verifyPassword: Boolean) = if (verifyPassword) {
              val broadcastTx = transaction.process[blockchainTransaction.IdentityIssue, transactionsIdentityIssue.Request](
                entity = blockchainTransaction.IdentityIssue(from = loginState.address, fromID = issueData.fromID, classificationID = issueData.classificationID, to = issueData.to, immutableMetaProperties = immutableMetas, immutableProperties = immutables, mutableMetaProperties = mutableMetas, mutableProperties = mutables, gas = issueData.gas, ticketID = "", mode = transactionMode),
                blockchainTransactionCreate = blockchainTransactionIdentityIssues.Service.create,
                request = transactionsIdentityIssue.Request(transactionsIdentityIssue.Message(transactionsIdentityIssue.BaseReq(from = loginState.address, gas = issueData.gas), fromID = issueData.fromID, classificationID = issueData.classificationID, to = issueData.to, immutableMetaProperties = immutableMetas, immutableProperties = immutables, mutableMetaProperties = mutableMetas, mutableProperties = mutables)),
                action = transactionsIdentityIssue.Service.post,
                onSuccess = blockchainTransactionIdentityIssues.Utility.onSuccess,
                onFailure = blockchainTransactionIdentityIssues.Utility.onFailure,
                updateTransactionHash = blockchainTransactionIdentityIssues.Service.updateTransactionHash)

              for {
                ticketID <- broadcastTx
                result <- withUsernameToken.Ok(views.html.identity(successes = Seq(new Success(ticketID))))
              } yield result
            } else Future(BadRequest(blockchainForms.identityIssue(blockchainCompanion.IdentityIssue.form.fill(issueData).withError(constants.FormField.PASSWORD.name, constants.Response.INCORRECT_PASSWORD.message), issueData.classificationID)))

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

  def provisionForm(identityID: String): Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
      Ok(blockchainForms.identityProvision(identityID = identityID))
  }

  def provision: Action[AnyContent] = withLoginActionAsync { implicit loginState =>
    implicit request =>
      views.companion.blockchain.IdentityProvision.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(blockchainForms.identityProvision(formWithErrors, formWithErrors.data.getOrElse(constants.FormField.IDENTITY_ID.name, ""))))
        },
        provisionData => {
          val verifyPassword = masterAccounts.Service.validateUsernamePassword(username = loginState.username, password = provisionData.password)

          def broadcastTxAndGetResult(verifyPassword: Boolean) = if (verifyPassword) {
            val broadcastTx = transaction.process[blockchainTransaction.IdentityProvision, transactionsIdentityProvision.Request](
              entity = blockchainTransaction.IdentityProvision(from = loginState.address, to = provisionData.to, identityID = provisionData.identityID, gas = provisionData.gas, ticketID = "", mode = transactionMode),
              blockchainTransactionCreate = blockchainTransactionIdentityProvisions.Service.create,
              request = transactionsIdentityProvision.Request(transactionsIdentityProvision.Message(transactionsIdentityProvision.BaseReq(from = loginState.address, gas = provisionData.gas), to = provisionData.to, identityID = provisionData.identityID)),
              action = transactionsIdentityProvision.Service.post,
              onSuccess = blockchainTransactionIdentityProvisions.Utility.onSuccess,
              onFailure = blockchainTransactionIdentityProvisions.Utility.onFailure,
              updateTransactionHash = blockchainTransactionIdentityProvisions.Service.updateTransactionHash
            )
            for {
              ticketID <- broadcastTx
              result <- withUsernameToken.Ok(views.html.identity(successes = Seq(new Success(ticketID))))
            } yield result
          } else Future(BadRequest(blockchainForms.identityProvision(blockchainCompanion.IdentityProvision.form.fill(provisionData).withError(constants.FormField.PASSWORD.name, constants.Response.INCORRECT_PASSWORD.message), provisionData.identityID)))

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

  def unprovisionForm(identityID: String, address: String): Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
      Ok(blockchainForms.identityUnprovision(identityID = identityID, address = address))
  }

  def unprovision: Action[AnyContent] = withLoginActionAsync { implicit loginState =>
    implicit request =>
      views.companion.blockchain.IdentityUnprovision.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(blockchainForms.identityUnprovision(formWithErrors, formWithErrors.data.getOrElse(constants.FormField.IDENTITY_ID.name, ""), formWithErrors.data.getOrElse(constants.FormField.TO.name, ""))))
        },
        unprovisionData => {
          val verifyPassword = masterAccounts.Service.validateUsernamePassword(username = loginState.username, password = unprovisionData.password)

          def broadcastTxAndGetResult(verifyPassword: Boolean) = if (verifyPassword) {
            val broadcastTx = transaction.process[blockchainTransaction.IdentityUnprovision, transactionsIdentityUnprovision.Request](
              entity = blockchainTransaction.IdentityUnprovision(from = loginState.address, to = unprovisionData.to, identityID = unprovisionData.identityID, gas = unprovisionData.gas, ticketID = "", mode = transactionMode),
              blockchainTransactionCreate = blockchainTransactionIdentityUnprovisions.Service.create,
              request = transactionsIdentityUnprovision.Request(transactionsIdentityUnprovision.Message(transactionsIdentityUnprovision.BaseReq(from = loginState.address, gas = unprovisionData.gas), to = unprovisionData.to, identityID = unprovisionData.identityID)),
              action = transactionsIdentityUnprovision.Service.post,
              onSuccess = blockchainTransactionIdentityUnprovisions.Utility.onSuccess,
              onFailure = blockchainTransactionIdentityUnprovisions.Utility.onFailure,
              updateTransactionHash = blockchainTransactionIdentityUnprovisions.Service.updateTransactionHash
            )
            for {
              ticketID <- broadcastTx
              result <- withUsernameToken.Ok(views.html.identity(successes = Seq(new Success(ticketID))))
            } yield result
          } else Future(BadRequest(blockchainForms.identityUnprovision(blockchainCompanion.IdentityUnprovision.form.fill(unprovisionData).withError(constants.FormField.PASSWORD.name, constants.Response.INCORRECT_PASSWORD.message), unprovisionData.identityID, unprovisionData.to)))

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
