package controllers

import constants.Response.Success
import controllers.actions._
import controllers.results.WithUsernameToken
import controllers.view.OtherApp
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
class AssetController @Inject()(
                                 messagesControllerComponents: MessagesControllerComponents,
                                 transaction: utilities.Transaction,
                                 withLoginActionAsync: WithLoginActionAsync,
                                 transactionsAssetDefine: transactions.blockchain.AssetDefine,
                                 blockchainTransactionAssetDefines: blockchainTransaction.AssetDefines,
                                 transactionsAssetMint: transactions.blockchain.AssetMint,
                                 blockchainTransactionAssetMints: blockchainTransaction.AssetMints,
                                 transactionsAssetMutate: transactions.blockchain.AssetMutate,
                                 blockchainTransactionAssetMutates: blockchainTransaction.AssetMutates,
                                 transactionsAssetBurn: transactions.blockchain.AssetBurn,
                                 blockchainTransactionAssetBurns: blockchainTransaction.AssetBurns,
                                 masterAccounts: master.Accounts,
                                 masterProperties: master.Properties,
                                 masterSplits: master.Splits,
                                 masterClassifications: master.Classifications,
                                 blockchainIdentities: blockchain.Identities,
                                 withUserLoginAction: WithUserLoginAction,
                                 withUsernameToken: WithUsernameToken,
                                 withoutLoginAction: WithoutLoginAction,
                                 withoutLoginActionAsync: WithoutLoginActionAsync
                               )(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_ASSET

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private implicit val otherApps: Seq[OtherApp] = configuration.get[Seq[Configuration]]("webApp.otherApps").map { otherApp =>
    OtherApp(url = otherApp.get[String]("url"), name = otherApp.get[String]("name"))
  }

  private def getNumberOfFields(addField: Boolean, currentNumber: Int) = if (addField) currentNumber + 1 else currentNumber

  def defineForm: Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
      Ok(blockchainForms.assetDefine())
  }

  def define: Action[AnyContent] = withLoginActionAsync { implicit loginState =>
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
            val immutableMetas = defineData.immutableMetaTraits.getOrElse(Seq.empty).flatten.map(_.toBaseProperty)
            val immutables = defineData.immutableTraits.getOrElse(Seq.empty).flatten.map(_.toBaseProperty)
            val mutableMetas = defineData.mutableMetaTraits.getOrElse(Seq.empty).flatten.map(_.toBaseProperty)
            val mutables = defineData.mutableTraits.getOrElse(Seq.empty).flatten.map(_.toBaseProperty)

            def broadcastTxAndGetResult(verifyPassword: Boolean) = if (verifyPassword) {
              val broadcastTx = transaction.process[blockchainTransaction.AssetDefine, transactionsAssetDefine.Request](
                entity = blockchainTransaction.AssetDefine(from = loginState.address, fromID = defineData.fromID, immutableMetaTraits = immutableMetas, immutableTraits = immutables, mutableMetaTraits = mutableMetas, mutableTraits = mutables, gas = defineData.gas, ticketID = "", mode = transactionMode),
                blockchainTransactionCreate = blockchainTransactionAssetDefines.Service.create,
                request = transactionsAssetDefine.Request(transactionsAssetDefine.Message(transactionsAssetDefine.BaseReq(from = loginState.address, gas = defineData.gas), fromID = defineData.fromID, immutableMetaTraits = immutableMetas, immutableTraits = immutables, mutableMetaTraits = mutableMetas, mutableTraits = mutables)),
                action = transactionsAssetDefine.Service.post,
                onSuccess = blockchainTransactionAssetDefines.Utility.onSuccess,
                onFailure = blockchainTransactionAssetDefines.Utility.onFailure,
                updateTransactionHash = blockchainTransactionAssetDefines.Service.updateTransactionHash)

              for {
                ticketID <- broadcastTx
                result <- withUsernameToken.Ok(views.html.asset(successes = Seq(new Success(ticketID))))
              } yield result
            } else Future(BadRequest(blockchainForms.assetDefine(blockchainCompanion.AssetDefine.form.fill(defineData).withError(constants.FormField.PASSWORD.name, constants.Response.INCORRECT_PASSWORD.message))))

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

  def mintForm(classificationID: String): Action[AnyContent] = withLoginActionAsync { implicit loginState =>
    implicit request =>
      val properties = masterProperties.Service.getAll(entityID = classificationID, entityType = constants.Blockchain.Entity.ASSET_DEFINITION)
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
          Ok(blockchainForms.assetMint(blockchainCompanion.AssetMint.form.fill(blockchainCompanion.AssetMint.Data(fromID = maintainerIDs.intersect(identityIDs).headOption.getOrElse(""), classificationID = classificationID, toID = "", immutableMetaProperties = immutableMetaProperties, addImmutableMetaField = false, immutableProperties = immutableProperties, addImmutableField = false, mutableMetaProperties = mutableMetaProperties, addMutableMetaField = false, mutableProperties = mutableProperties, addMutableField = false, gas = MicroNumber.zero, password = None)), classificationID = classificationID, numImmutableMetaForms = immutableMetaProperties.fold(0)(_.length), numImmutableForms = immutableProperties.fold(0)(_.length), numMutableMetaForms = mutableMetaProperties.fold(0)(_.length), numMutableForms = mutableProperties.fold(0)(_.length)))
        } else {
          Ok(blockchainForms.assetMint(classificationID = classificationID))
        }
      }
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def mint: Action[AnyContent] = withLoginActionAsync { implicit loginState =>
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

            val immutableMetas = mintData.immutableMetaProperties.getOrElse(Seq.empty).flatten.map(_.toBaseProperty)
            val immutables = mintData.immutableProperties.getOrElse(Seq.empty).flatten.map(_.toBaseProperty)
            val mutableMetas = mintData.mutableMetaProperties.getOrElse(Seq.empty).flatten.map(_.toBaseProperty)
            val mutables = mintData.mutableProperties.getOrElse(Seq.empty).flatten.map(_.toBaseProperty)

            def broadcastTxAndGetResult(verifyPassword: Boolean) = if (verifyPassword) {
              val broadcastTx = transaction.process[blockchainTransaction.AssetMint, transactionsAssetMint.Request](
                entity = blockchainTransaction.AssetMint(from = loginState.address, fromID = mintData.fromID, toID = mintData.toID, classificationID = mintData.classificationID, immutableMetaProperties = immutableMetas, immutableProperties = immutables, mutableMetaProperties = mutableMetas, mutableProperties = mutables, gas = mintData.gas, ticketID = "", mode = transactionMode),
                blockchainTransactionCreate = blockchainTransactionAssetMints.Service.create,
                request = transactionsAssetMint.Request(transactionsAssetMint.Message(transactionsAssetMint.BaseReq(from = loginState.address, gas = mintData.gas), fromID = mintData.fromID, toID = mintData.toID, classificationID = mintData.classificationID, immutableMetaProperties = immutableMetas, immutableProperties = immutables, mutableMetaProperties = mutableMetas, mutableProperties = mutables)),
                action = transactionsAssetMint.Service.post,
                onSuccess = blockchainTransactionAssetMints.Utility.onSuccess,
                onFailure = blockchainTransactionAssetMints.Utility.onFailure,
                updateTransactionHash = blockchainTransactionAssetMints.Service.updateTransactionHash)

              for {
                ticketID <- broadcastTx
                result <- withUsernameToken.Ok(views.html.asset(successes = Seq(new Success(ticketID))))
              } yield result
            } else Future(BadRequest(blockchainForms.assetMint(blockchainCompanion.AssetMint.form.fill(mintData).withError(constants.FormField.PASSWORD.name, constants.Response.INCORRECT_PASSWORD.message), mintData.classificationID)))

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

  def mutateForm(assetID: String, fromID: String): Action[AnyContent] = withLoginActionAsync { implicit loginState =>
    implicit request =>
      val properties = masterProperties.Service.getAll(entityID = assetID, entityType = constants.Blockchain.Entity.ASSET)
      val ownerID = masterSplits.Service.tryGetOwnerID(assetID)

      def getProvisionedAddresses(ownerID: String) = blockchainIdentities.Service.getAllProvisionAddressesByIdentityWithActor(ownerID)

      (for {
        properties <- properties
        ownerID <- ownerID
        provisionedAddresses <- getProvisionedAddresses(ownerID)
      } yield {
        if (properties.nonEmpty && provisionedAddresses.contains(loginState.address)) {
          val mutableMetaProperties = Option(properties.filter(x => x.isMeta && x.isMutable).map(x => Option(views.companion.common.Property.Data(dataType = x.dataType, dataName = x.name, dataValue = x.value))))
          val mutableProperties = Option(properties.filter(x => !x.isMeta && x.isMutable).map(x => Option(views.companion.common.Property.Data(dataType = x.dataType, dataName = x.name, dataValue = x.value))))
          Ok(blockchainForms.assetMutate(blockchainCompanion.AssetMutate.form.fill(blockchainCompanion.AssetMutate.Data(fromID = fromID, assetID = assetID, mutableMetaProperties = mutableMetaProperties, addMutableMetaField = false, mutableProperties = mutableProperties, addMutableField = false, gas = MicroNumber.zero, password = None)), assetID = assetID, fromID = fromID, numMutableMetaForms = mutableMetaProperties.fold(0)(_.length), numMutableForms = mutableProperties.fold(0)(_.length)))
        } else {
          Ok(blockchainForms.assetMutate(assetID = assetID, fromID = fromID))
        }
      }).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def mutate: Action[AnyContent] = withLoginActionAsync { implicit loginState =>
    implicit request =>
      blockchainCompanion.AssetMutate.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(blockchainForms.assetMutate(formWithErrors, formWithErrors.data.getOrElse(constants.FormField.ASSET_ID.name, ""), formWithErrors.data.getOrElse(constants.FormField.FROM_ID.name, ""))))
        },
        mutateData => {
          if (mutateData.addMutableMetaField || mutateData.addMutableField) {
            Future(PartialContent(blockchainForms.assetMutate(
              assetMutateForm = blockchainCompanion.AssetMutate.form.fill(mutateData.copy(addMutableMetaField = false, addMutableField = false)),
              assetID = mutateData.assetID, fromID = mutateData.fromID,
              numMutableMetaForms = getNumberOfFields(mutateData.addMutableMetaField, mutateData.mutableMetaProperties.fold(0)(_.flatten.length)),
              numMutableForms = getNumberOfFields(mutateData.addMutableField, mutateData.mutableProperties.fold(0)(_.flatten.length)))))
          } else {
            val verifyPassword = masterAccounts.Service.validateUsernamePassword(username = loginState.username, password = mutateData.password.getOrElse(""))

            val mutableMetas = mutateData.mutableMetaProperties.getOrElse(Seq.empty).flatten.map(_.toBaseProperty)
            val mutables = mutateData.mutableProperties.getOrElse(Seq.empty).flatten.map(_.toBaseProperty)

            def broadcastTxAndGetResult(verifyPassword: Boolean) = if (verifyPassword) {
              val broadcastTx = transaction.process[blockchainTransaction.AssetMutate, transactionsAssetMutate.Request](
                entity = blockchainTransaction.AssetMutate(from = loginState.address, fromID = mutateData.fromID, assetID = mutateData.assetID, mutableMetaProperties = mutableMetas, mutableProperties = mutables, gas = mutateData.gas, ticketID = "", mode = transactionMode),
                blockchainTransactionCreate = blockchainTransactionAssetMutates.Service.create,
                request = transactionsAssetMutate.Request(transactionsAssetMutate.Message(transactionsAssetMutate.BaseReq(from = loginState.address, gas = mutateData.gas), fromID = mutateData.fromID, assetID = mutateData.assetID, mutableMetaProperties = mutableMetas, mutableProperties = mutables)),
                action = transactionsAssetMutate.Service.post,
                onSuccess = blockchainTransactionAssetMutates.Utility.onSuccess,
                onFailure = blockchainTransactionAssetMutates.Utility.onFailure,
                updateTransactionHash = blockchainTransactionAssetMutates.Service.updateTransactionHash)

              for {
                ticketID <- broadcastTx
                result <- withUsernameToken.Ok(views.html.asset(successes = Seq(new Success(ticketID))))
              } yield result
            } else Future(BadRequest(blockchainForms.assetMutate(blockchainCompanion.AssetMutate.form.fill(mutateData).withError(constants.FormField.PASSWORD.name, constants.Response.INCORRECT_PASSWORD.message), mutateData.assetID, mutateData.fromID)))

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

  def burnForm(assetID: String, fromID: String): Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
      Ok(blockchainForms.assetBurn(assetID = assetID, fromID = fromID))
  }

  def burn: Action[AnyContent] = withLoginActionAsync { implicit loginState =>
    implicit request =>
      blockchainCompanion.AssetBurn.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(blockchainForms.assetBurn(formWithErrors, formWithErrors.data.getOrElse(constants.FormField.ASSET_ID.name, ""), formWithErrors.data.getOrElse(constants.FormField.FROM_ID.name, ""))))
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
              result <- withUsernameToken.Ok(views.html.asset(successes = Seq(new Success(ticketID))))
            } yield result
          } else Future(BadRequest(blockchainForms.assetBurn(blockchainCompanion.AssetBurn.form.fill(burnData).withError(constants.FormField.PASSWORD.name, constants.Response.INCORRECT_PASSWORD.message), burnData.assetID, burnData.fromID)))

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
