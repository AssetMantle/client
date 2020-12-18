package controllers

import constants.Response.Success
import controllers.actions._
import controllers.results.WithUsernameToken
import exceptions.BaseException
import models.common.Serializable._
import models.{blockchain, blockchainTransaction, master}
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}
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
                                    blockchainIdentities: blockchain.Identities,
                                    blockchainClassifications: blockchain.Classifications,
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
                                    masterProperties: master.Properties,
                                    masterIdentities: master.Identities,
                                    masterClassifications: master.Classifications,
                                    masterAccounts: master.Accounts,
                                    withoutLoginAction: WithoutLoginAction,
                                    withoutLoginActionAsync: WithoutLoginActionAsync
                                  )(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_IDENTITY

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private def getNumberOfFields(addField: Boolean, currentNumber: Int) = if (addField) currentNumber + 1 else currentNumber

  def nubForm: Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
      Ok(blockchainForms.identityNub())
  }

  def nub: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.blockchain.IdentityNub.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(blockchainForms.identityNub(formWithErrors)))
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
              result <- withUsernameToken.Ok(views.html.dashboard(successes = Seq(new Success(ticketID))))
            } yield result
          } else Future(BadRequest(blockchainForms.identityNub(blockchainCompanion.IdentityNub.form.fill(nubData).withError(constants.FormField.PASSWORD.name, constants.Response.INCORRECT_PASSWORD.message))))

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

  def define: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
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

            val immutableMetas = defineData.immutableMetaTraits.getOrElse(Seq.empty).flatten
            val immutables = defineData.immutableTraits.getOrElse(Seq.empty).flatten
            val mutableMetas = defineData.mutableMetaTraits.getOrElse(Seq.empty).flatten
            val mutables = defineData.mutableTraits.getOrElse(Seq.empty).flatten

            val entityID = blockchainClassifications.Utility.getID(immutables = Immutables(Properties((immutableMetas ++ immutables).map(_.toProperty))), mutables = Mutables(Properties((mutableMetas ++ mutables).map(_.toProperty))))

            def insertAndBroadcast(classificationExists: Boolean) = if (!classificationExists) {
              val insertProperties = masterProperties.Utilities.upsertProperties(entityID = entityID, entityType = constants.Blockchain.Entity.IDENTITY_DEFINITION, immutableMetas = immutableMetas, immutables = immutables, mutableMetas = mutableMetas, mutables = mutables)
              val create = masterClassifications.Service.create(id = entityID, entityType = constants.Blockchain.Entity.IDENTITY_DEFINITION, fromID = defineData.fromID, label = Option(defineData.label), status = None)

              def broadcastTx = transaction.process[blockchainTransaction.IdentityDefine, transactionsIdentityDefine.Request](
                entity = blockchainTransaction.IdentityDefine(from = loginState.address, fromID = defineData.fromID, immutableMetaTraits = MetaProperties(immutableMetas.map(_.toMetaProperty)), immutableTraits = Properties(immutables.map(_.toProperty)), mutableMetaTraits = MetaProperties(mutableMetas.map(_.toMetaProperty)), mutableTraits = Properties(mutables.map(_.toProperty)), gas = defineData.gas, ticketID = "", mode = transactionMode),
                blockchainTransactionCreate = blockchainTransactionIdentityDefines.Service.create,
                request = transactionsIdentityDefine.Request(transactionsIdentityDefine.Message(transactionsIdentityDefine.BaseReq(from = loginState.address, gas = defineData.gas), fromID = defineData.fromID, immutableMetaTraits = immutableMetas, immutableTraits = immutables, mutableMetaTraits = mutableMetas, mutableTraits = mutables)),
                action = transactionsIdentityDefine.Service.post,
                onSuccess = blockchainTransactionIdentityDefines.Utility.onSuccess,
                onFailure = blockchainTransactionIdentityDefines.Utility.onFailure,
                updateTransactionHash = blockchainTransactionIdentityDefines.Service.updateTransactionHash
              )

              (for {
                _ <- insertProperties
                _ <- create
                ticketID <- broadcastTx
              } yield ticketID
                ).recoverWith {
                case baseException: BaseException => masterProperties.Service.deleteAll(entityID = entityID, entityType = constants.Blockchain.Entity.IDENTITY_DEFINITION)
                  masterClassifications.Service.delete(entityID)
                  throw baseException
              }
            } else Future(throw new BaseException(constants.Response.CLASSIFICATION_ALREADY_EXISTS))

            def broadcastTxAndGetResult(verifyPassword: Boolean) = if (verifyPassword) {
              val classificationExists = blockchainClassifications.Service.checkExists(entityID)

              for {
                classificationExists <- classificationExists
                ticketID <- insertAndBroadcast(classificationExists)
                result <- withUsernameToken.Ok(views.html.dashboard(successes = Seq(new Success(ticketID))))
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

  def issueForm(classificationID: String): Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
      Ok(blockchainForms.identityIssue(classificationID = classificationID))
  }

  def issue: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
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
            val immutableMetas = issueData.immutableMetaProperties.getOrElse(Seq.empty).flatten
            val immutables = issueData.immutableProperties.getOrElse(Seq.empty).flatten
            val mutableMetas = issueData.mutableMetaProperties.getOrElse(Seq.empty).flatten
            val mutables = issueData.mutableProperties.getOrElse(Seq.empty).flatten
            val entityID = blockchainIdentities.Utility.getID(classificationID = issueData.classificationID, immutables = Immutables(Properties((immutableMetas ++ immutables).map(_.toProperty))))

            def insertAndBroadcast(classificationExists: Boolean, identityExists: Boolean) = if (classificationExists && !identityExists) {
              val insertProperties = masterProperties.Utilities.upsertProperties(entityID = entityID, entityType = constants.Blockchain.Entity.IDENTITY, immutableMetas = immutableMetas, immutables = immutables, mutableMetas = mutableMetas, mutables = mutables)
              val create = masterIdentities.Service.create(master.Identity(id = entityID, label = Option(issueData.label), status = None))

              def broadcastTx = transaction.process[blockchainTransaction.IdentityIssue, transactionsIdentityIssue.Request](
                entity = blockchainTransaction.IdentityIssue(from = loginState.address, fromID = issueData.fromID, classificationID = issueData.classificationID, to = issueData.to, immutableMetaProperties = MetaProperties(immutableMetas.map(_.toMetaProperty)), immutableProperties = Properties(immutables.map(_.toProperty)), mutableMetaProperties = MetaProperties(mutableMetas.map(_.toMetaProperty)), mutableProperties = Properties(mutables.map(_.toProperty)), gas = issueData.gas, ticketID = "", mode = transactionMode),
                blockchainTransactionCreate = blockchainTransactionIdentityIssues.Service.create,
                request = transactionsIdentityIssue.Request(transactionsIdentityIssue.Message(transactionsIdentityIssue.BaseReq(from = loginState.address, gas = issueData.gas), fromID = issueData.fromID, classificationID = issueData.classificationID, to = issueData.to, immutableMetaProperties = immutableMetas, immutableProperties = immutables, mutableMetaProperties = mutableMetas, mutableProperties = mutables)),
                action = transactionsIdentityIssue.Service.post,
                onSuccess = blockchainTransactionIdentityIssues.Utility.onSuccess,
                onFailure = blockchainTransactionIdentityIssues.Utility.onFailure,
                updateTransactionHash = blockchainTransactionIdentityIssues.Service.updateTransactionHash
              )

              for {
                _ <- insertProperties
                _ <- create
                ticketID <- broadcastTx
              } yield ticketID
            } else if (!classificationExists) Future(throw new BaseException(constants.Response.CLASSIFICATION_NOT_FOUND))
            else Future(throw new BaseException(constants.Response.IDENTITY_ALREADY_EXISTS))

            def broadcastTxAndGetResult(verifyPassword: Boolean) = if (verifyPassword) {
              val classificationExists = blockchainClassifications.Service.checkExists(issueData.classificationID)
              val identityExists = blockchainIdentities.Service.checkExists(entityID)

              for {
                classificationExists <- classificationExists
                identityExists <- identityExists
                ticketID <- insertAndBroadcast(classificationExists = classificationExists, identityExists = identityExists)
                result <- withUsernameToken.Ok(views.html.dashboard(successes = Seq(new Success(ticketID))))
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

  def provision: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
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
              result <- withUsernameToken.Ok(views.html.dashboard(successes = Seq(new Success(ticketID))))
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

  def unprovisionForm(identityID: String): Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
      Ok(blockchainForms.identityUnprovision(identityID = identityID))
  }

  def unprovision: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.blockchain.IdentityUnprovision.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(blockchainForms.identityUnprovision(formWithErrors, formWithErrors.data.getOrElse(constants.FormField.IDENTITY_ID.name, ""))))
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
              result <- withUsernameToken.Ok(views.html.dashboard(successes = Seq(new Success(ticketID))))
            } yield result
          } else Future(BadRequest(blockchainForms.identityUnprovision(blockchainCompanion.IdentityUnprovision.form.fill(unprovisionData).withError(constants.FormField.PASSWORD.name, constants.Response.INCORRECT_PASSWORD.message), unprovisionData.identityID)))

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
