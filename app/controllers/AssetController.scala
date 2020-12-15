package controllers

import constants.Response.Success
import controllers.actions._
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.common.Serializable._
import models.master.{Asset => masterAsset, Split => masterSplit}
import models.{blockchain, blockchainTransaction, master}
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}
import views.companion.{blockchain => blockchainCompanion}
import views.html.component.blockchain.{txForms => blockchainForms}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AssetController @Inject()(
                                 blockchainClassifications: blockchain.Classifications,
                                 blockchainAssets: blockchain.Assets,
                                 masterClassifications: master.Classifications,
                                 masterProperties: master.Properties,
                                 masterAssets: master.Assets,
                                 masterSplits: master.Splits,
                                 messagesControllerComponents: MessagesControllerComponents,
                                 transaction: utilities.Transaction,
                                 withLoginAction: WithLoginAction,
                                 withUnknownLoginAction: WithUnknownLoginAction,
                                 transactionsAssetDefine: transactions.blockchain.AssetDefine,
                                 blockchainTransactionAssetDefines: blockchainTransaction.AssetDefines,
                                 transactionsAssetMint: transactions.blockchain.AssetMint,
                                 blockchainTransactionAssetMints: blockchainTransaction.AssetMints,
                                 transactionsAssetMutate: transactions.blockchain.AssetMutate,
                                 blockchainTransactionAssetMutates: blockchainTransaction.AssetMutates,
                                 transactionsAssetBurn: transactions.blockchain.AssetBurn,
                                 blockchainTransactionAssetBurns: blockchainTransaction.AssetBurns,
                                 masterAccounts: master.Accounts,
                                 withUserLoginAction: WithUserLoginAction,
                                 withUsernameToken: WithUsernameToken,
                                 withoutLoginAction: WithoutLoginAction,
                                 withoutLoginActionAsync: WithoutLoginActionAsync
                               )(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_ASSET

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private def getNumberOfFields(addField: Boolean, currentNumber: Int) = if (addField) currentNumber + 1 else currentNumber

  def defineForm: Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
    Ok(blockchainForms.assetDefine())
  }

  def define: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      blockchainCompanion.AssetDefine.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(blockchainForms.assetDefine(formWithErrors)))
        },
        defineData => {
          if (defineData.addImmutableMetaField || defineData.addImmutableField || defineData.addMutableMetaField || defineData.addMutableField) {
            Future(PartialContent(blockchainForms.assetDefine(
              assetDefineForm = blockchainCompanion.AssetDefine.form.fill(defineData.copy(addImmutableMetaField = false, addImmutableField = false, addMutableMetaField = false, addMutableField = false)),
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
              val insertProperties = masterProperties.Utilities.upsertProperties(entityID = entityID, entityType = constants.Blockchain.Entity.ASSET_DEFINITION, immutableMetas = immutableMetas, immutables = immutables, mutableMetas = mutableMetas, mutables = mutables)
              val create = masterClassifications.Service.create(id = entityID, entityType = constants.Blockchain.Entity.ASSET_DEFINITION, fromID = defineData.fromID, label = Option(defineData.label), status = None)

              def broadcastTx = transaction.process[blockchainTransaction.AssetDefine, transactionsAssetDefine.Request](
                entity = blockchainTransaction.AssetDefine(from = loginState.address, fromID = defineData.fromID, immutableMetaTraits = MetaProperties(immutableMetas.map(_.toMetaProperty)), immutableTraits = Properties(immutables.map(_.toProperty)), mutableMetaTraits = MetaProperties(mutableMetas.map(_.toMetaProperty)), mutableTraits = Properties(mutables.map(_.toProperty)), gas = defineData.gas, ticketID = "", mode = transactionMode),
                blockchainTransactionCreate = blockchainTransactionAssetDefines.Service.create,
                request = transactionsAssetDefine.Request(transactionsAssetDefine.Message(transactionsAssetDefine.BaseReq(from = loginState.address, gas = defineData.gas), fromID = defineData.fromID, immutableMetaTraits = immutableMetas, immutableTraits = immutables, mutableMetaTraits = mutableMetas, mutableTraits = mutables)),
                action = transactionsAssetDefine.Service.post,
                onSuccess = blockchainTransactionAssetDefines.Utility.onSuccess,
                onFailure = blockchainTransactionAssetDefines.Utility.onFailure,
                updateTransactionHash = blockchainTransactionAssetDefines.Service.updateTransactionHash
              )

              (for {
                _ <- insertProperties
                _ <- create
                ticketID <- broadcastTx
              } yield ticketID
                ).recoverWith {
                case baseException: BaseException => masterProperties.Service.deleteAll(entityID = entityID, entityType = constants.Blockchain.Entity.ASSET_DEFINITION)
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
            } else Future(BadRequest(blockchainForms.assetDefine(blockchainCompanion.AssetDefine.form.fill(defineData).withError(constants.FormField.PASSWORD.name, constants.Response.INCORRECT_PASSWORD.message))))

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

  def mintForm(classificationID: String): Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
    Ok(blockchainForms.assetMint(classificationID = classificationID))
  }

  def mint: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      blockchainCompanion.AssetMint.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(blockchainForms.assetMint(formWithErrors, formWithErrors.data.getOrElse(constants.FormField.CLASSIFICATION_ID.name, ""))))
        },
        mintData => {
          if (mintData.addImmutableMetaField || mintData.addImmutableField || mintData.addMutableMetaField || mintData.addMutableField) {
            Future(PartialContent(blockchainForms.assetMint(
              assetMintForm = blockchainCompanion.AssetMint.form.fill(mintData.copy(addImmutableMetaField = false, addImmutableField = false, addMutableMetaField = false, addMutableField = false)),
              classificationID = mintData.classificationID,
              numImmutableMetaForms = getNumberOfFields(mintData.addImmutableMetaField, mintData.immutableMetaProperties.fold(0)(_.flatten.length)),
              numImmutableForms = getNumberOfFields(mintData.addImmutableField, mintData.immutableProperties.fold(0)(_.flatten.length)),
              numMutableMetaForms = getNumberOfFields(mintData.addMutableMetaField, mintData.mutableMetaProperties.fold(0)(_.flatten.length)),
              numMutableForms = getNumberOfFields(mintData.addMutableField, mintData.mutableProperties.fold(0)(_.flatten.length)))))
          } else {
            val verifyPassword = masterAccounts.Service.validateUsernamePassword(username = loginState.username, password = mintData.password.getOrElse(""))

            val immutableMetas = mintData.immutableMetaProperties.getOrElse(Seq.empty).flatten
            val immutables = mintData.immutableProperties.getOrElse(Seq.empty).flatten
            val mutableMetas = mintData.mutableMetaProperties.getOrElse(Seq.empty).flatten
            val mutables = mintData.mutableProperties.getOrElse(Seq.empty).flatten
            val entityID = blockchainAssets.Utility.getID(classificationID = mintData.classificationID, immutables = Immutables(Properties((immutableMetas ++ immutables).map(_.toProperty))))

            def insertAndBroadcast(classificationExists: Boolean, assetExists: Boolean) = if (classificationExists && !assetExists) {
              val insertProperties = masterProperties.Utilities.upsertProperties(entityID = entityID, entityType = constants.Blockchain.Entity.ASSET, immutableMetas = immutableMetas, immutables = immutables, mutableMetas = mutableMetas, mutables = mutables)
              val createAsset = masterAssets.Service.create(masterAsset(id = entityID, label = Option(mintData.label), ownerID = mintData.toID, status = None))
              val createSplit = masterSplits.Service.create(masterSplit(entityID = entityID, ownerID = mintData.toID, entityType = constants.Blockchain.Entity.ASSET, label = Option(mintData.label), status = None))

              def broadcastTx = transaction.process[blockchainTransaction.AssetMint, transactionsAssetMint.Request](
                entity = blockchainTransaction.AssetMint(from = loginState.address, fromID = mintData.fromID, toID = mintData.toID, classificationID = mintData.classificationID, immutableMetaProperties = MetaProperties(mintData.immutableMetaProperties.fold[Seq[MetaProperty]](Seq.empty)(_.flatten.map(_.toMetaProperty))), immutableProperties = Properties(mintData.immutableProperties.fold[Seq[Property]](Seq.empty)(_.flatten.map(_.toProperty))), mutableMetaProperties = MetaProperties(mintData.mutableMetaProperties.fold[Seq[MetaProperty]](Seq.empty)(_.flatten.map(_.toMetaProperty))), mutableProperties = Properties(mintData.mutableProperties.fold[Seq[Property]](Seq.empty)(_.flatten.map(_.toProperty))), gas = mintData.gas, ticketID = "", mode = transactionMode),
                blockchainTransactionCreate = blockchainTransactionAssetMints.Service.create,
                request = transactionsAssetMint.Request(transactionsAssetMint.Message(transactionsAssetMint.BaseReq(from = loginState.address, gas = mintData.gas), fromID = mintData.fromID, toID = mintData.toID, classificationID = mintData.classificationID, immutableMetaProperties = mintData.immutableMetaProperties.getOrElse(Seq.empty).flatten, immutableProperties = mintData.immutableProperties.getOrElse(Seq.empty).flatten, mutableMetaProperties = mintData.mutableMetaProperties.getOrElse(Seq.empty).flatten, mutableProperties = mintData.mutableProperties.getOrElse(Seq.empty).flatten)),
                action = transactionsAssetMint.Service.post,
                onSuccess = blockchainTransactionAssetMints.Utility.onSuccess,
                onFailure = blockchainTransactionAssetMints.Utility.onFailure,
                updateTransactionHash = blockchainTransactionAssetMints.Service.updateTransactionHash
              )

              for {
                _ <- insertProperties
                _ <- createAsset
                _ <- createSplit
                ticketID <- broadcastTx
              } yield ticketID
            } else if (!classificationExists) Future(throw new BaseException(constants.Response.CLASSIFICATION_NOT_FOUND))
            else Future(throw new BaseException(constants.Response.ASSET_ALREADY_EXISTS))

            def broadcastTxAndGetResult(verifyPassword: Boolean) = if (verifyPassword) {
              val classificationExists = blockchainClassifications.Service.checkExists(mintData.classificationID)
              val assetExists = blockchainAssets.Service.checkExists(entityID)
              for {
                classificationExists <- classificationExists
                assetExists <- assetExists
                ticketID <- insertAndBroadcast(classificationExists = classificationExists, assetExists = assetExists)
                result <- withUsernameToken.Ok(views.html.dashboard(successes = Seq(new Success(ticketID))))
              } yield result
            } else Future(BadRequest(blockchainForms.assetMint(blockchainCompanion.AssetMint.form.fill(mintData).withError(constants.FormField.PASSWORD.name, constants.Response.INCORRECT_PASSWORD.message), mintData.classificationID)))

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

  def mutateForm(assetID: String): Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
    Ok(blockchainForms.assetMutate(assetID = assetID))
  }

  def mutate: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      blockchainCompanion.AssetMutate.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(blockchainForms.assetMutate(formWithErrors, formWithErrors.data.getOrElse(constants.FormField.ASSET_ID.name, ""))))
        },
        mutateData => {
          if (mutateData.addMutableMetaField || mutateData.addMutableField) {
            Future(PartialContent(blockchainForms.assetMutate(
              assetMutateForm = blockchainCompanion.AssetMutate.form.fill(mutateData.copy(addMutableMetaField = false, addMutableField = false)),
              assetID = mutateData.assetID,
              numMutableMetaForms = getNumberOfFields(mutateData.addMutableMetaField, mutateData.mutableMetaProperties.fold(0)(_.flatten.length)),
              numMutableForms = getNumberOfFields(mutateData.addMutableField, mutateData.mutableProperties.fold(0)(_.flatten.length)))))
          } else {
            val verifyPassword = masterAccounts.Service.validateUsernamePassword(username = loginState.username, password = mutateData.password.getOrElse(""))

            val mutableMetas = mutateData.mutableMetaProperties.getOrElse(Seq.empty).flatten
            val mutables = mutateData.mutableProperties.getOrElse(Seq.empty).flatten

            def updateAndBroadcast(assetExists: Boolean) = if (assetExists) {
              val updateProperties = masterProperties.Utilities.updateProperties(entityID = mutateData.assetID, entityType = constants.Blockchain.Entity.ASSET, mutableMetas = mutableMetas, mutables = mutables)

              def broadcastTx = transaction.process[blockchainTransaction.AssetMutate, transactionsAssetMutate.Request](
                entity = blockchainTransaction.AssetMutate(from = loginState.address, fromID = mutateData.fromID, assetID = mutateData.assetID, mutableMetaProperties = MetaProperties(mutableMetas.map(_.toMetaProperty)), mutableProperties = Properties(mutables.map(_.toProperty)), gas = mutateData.gas, ticketID = "", mode = transactionMode),
                blockchainTransactionCreate = blockchainTransactionAssetMutates.Service.create,
                request = transactionsAssetMutate.Request(transactionsAssetMutate.Message(transactionsAssetMutate.BaseReq(from = loginState.address, gas = mutateData.gas), fromID = mutateData.fromID, assetID = mutateData.assetID, mutableMetaProperties = mutableMetas, mutableProperties = mutables)),
                action = transactionsAssetMutate.Service.post,
                onSuccess = blockchainTransactionAssetMutates.Utility.onSuccess,
                onFailure = blockchainTransactionAssetMutates.Utility.onFailure,
                updateTransactionHash = blockchainTransactionAssetMutates.Service.updateTransactionHash
              )

              for {
                _ <- updateProperties
                ticketID <- broadcastTx
              } yield ticketID
            } else Future(throw new BaseException(constants.Response.ASSET_NOT_FOUND))

            def broadcastTxAndGetResult(verifyPassword: Boolean) = if (verifyPassword) {
              val assetExists = blockchainAssets.Service.checkExists(mutateData.assetID)
              for {
                assetExists <- assetExists
                ticketID <- updateAndBroadcast(assetExists)
                result <- withUsernameToken.Ok(views.html.dashboard(successes = Seq(new Success(ticketID))))
              } yield result
            } else Future(BadRequest(blockchainForms.assetMutate(blockchainCompanion.AssetMutate.form.fill(mutateData).withError(constants.FormField.PASSWORD.name, constants.Response.INCORRECT_PASSWORD.message), mutateData.assetID)))

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

  def burnForm(assetID: String): Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
    Ok(blockchainForms.assetBurn(assetID = assetID))
  }

  def burn: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      blockchainCompanion.AssetBurn.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(blockchainForms.assetBurn(formWithErrors, formWithErrors.data.getOrElse(constants.FormField.ASSET_ID.name, ""))))
        },
        burnData => {
          val verifyPassword = masterAccounts.Service.validateUsernamePassword(username = loginState.username, password = burnData.password)

          def broadcastTx = transaction.process[blockchainTransaction.AssetBurn, transactionsAssetBurn.Request](
            entity = blockchainTransaction.AssetBurn(from = loginState.address, fromID = burnData.fromID, assetID = burnData.assetID, gas = burnData.gas, ticketID = "", mode = transactionMode),
            blockchainTransactionCreate = blockchainTransactionAssetBurns.Service.create,
            request = transactionsAssetBurn.Request(transactionsAssetBurn.Message(transactionsAssetBurn.BaseReq(from = loginState.address, gas = burnData.gas), fromID = burnData.fromID, assetID = burnData.assetID)),
            action = transactionsAssetBurn.Service.post,
            onSuccess = blockchainTransactionAssetBurns.Utility.onSuccess,
            onFailure = blockchainTransactionAssetBurns.Utility.onFailure,
            updateTransactionHash = blockchainTransactionAssetBurns.Service.updateTransactionHash
          )

          def broadcastTxAndGetResult(verifyPassword: Boolean) = if (verifyPassword) {
            for {
              ticketID <- broadcastTx
              result <- withUsernameToken.Ok(views.html.dashboard(successes = Seq(new Success(ticketID))))
            } yield result
          } else Future(BadRequest(blockchainForms.assetBurn(blockchainCompanion.AssetBurn.form.fill(burnData).withError(constants.FormField.PASSWORD.name, constants.Response.INCORRECT_PASSWORD.message), burnData.assetID)))

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
