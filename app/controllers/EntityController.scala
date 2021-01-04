package controllers

import controllers.actions._
import controllers.results.WithUsernameToken
import exceptions.BaseException
import models.common.Serializable.{Immutables, Mutables, Properties}
import models.{blockchain, master}
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}
import views.companion.{master => masterCompanion}
import views.html.component.{master => masterComponent}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class EntityController @Inject()(
                                  masterClassifications: master.Classifications,
                                  masterProperties: master.Properties,
                                  masterAssets: master.Assets,
                                  masterSplits: master.Splits,
                                  masterIdentities: master.Identities,
                                  masterOrders: master.Orders,
                                  blockchainIdentities: blockchain.Identities,
                                  blockchainClassifications: blockchain.Classifications,
                                  blockchainMetas: blockchain.Metas,
                                  blockchainAssets: blockchain.Assets,
                                  blockchainOrders: blockchain.Orders,
                                  messagesControllerComponents: MessagesControllerComponents,
                                  withLoginAction: WithLoginAction,
                                  withUnknownLoginAction: WithUnknownLoginAction,
                                  withUserLoginAction: WithUserLoginAction,
                                  withUsernameToken: WithUsernameToken,
                                  withoutLoginAction: WithoutLoginAction,
                                  withoutLoginActionAsync: WithoutLoginActionAsync
                                )(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_ENTITY

  def upsertLabelForm(entityID: String, entityType: String): Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
      Ok(masterComponent.upsertEntityLabel(entityID = entityID, entityType = entityType))
  }

  def upsertLabel(): Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      masterCompanion.UpsertEntityLabel.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(masterComponent.upsertEntityLabel(formWithErrors, formWithErrors.data.getOrElse(constants.FormField.ENTITY_ID.name, ""), formWithErrors.data.getOrElse(constants.FormField.ENTITY_TYPE.name, ""))))
        },
        upsertLabelData => {
          def getProvisionedAddress(maintainer: String) = blockchainIdentities.Service.getAllProvisionAddresses(maintainer)

          val verifyAndUpdate = upsertLabelData.entityType match {
            case constants.Blockchain.Entity.IDENTITY_DEFINITION | constants.Blockchain.Entity.ASSET_DEFINITION | constants.Blockchain.Entity.ORDER_DEFINITION =>
              val maintainerID = masterClassifications.Service.tryGetMaintainerID(id = upsertLabelData.entityID)

              def checkAndUpdate(maintainerID: String, provisionedAddresses: Seq[String]) = if (provisionedAddresses.contains(loginState.address)) masterClassifications.Service.updateLabel(id = upsertLabelData.entityID, maintainerID = maintainerID, label = upsertLabelData.label)
              else throw new BaseException(constants.Response.UNAUTHORIZED)

              for {
                maintainerID <- maintainerID
                provisionedAddresses <- getProvisionedAddress(maintainerID)
                _ <- checkAndUpdate(maintainerID, provisionedAddresses)
              } yield ()
            case constants.Blockchain.Entity.ASSET =>
              val ownerID = masterSplits.Service.tryGetOwnerID(upsertLabelData.entityID)

              def checkAndUpdate(provisionedAddresses: Seq[String]) = if (provisionedAddresses.contains(loginState.address)) masterAssets.Service.updateLabel(id = upsertLabelData.entityID, label = upsertLabelData.label)
              else throw new BaseException(constants.Response.UNAUTHORIZED)

              for {
                ownerID <- ownerID
                provisionedAddresses <- getProvisionedAddress(ownerID)
                _ <- checkAndUpdate(provisionedAddresses)
              } yield ()
            case constants.Blockchain.Entity.IDENTITY =>
              def checkAndUpdate(provisionedAddresses: Seq[String]) = if (provisionedAddresses.contains(loginState.address)) masterIdentities.Service.updateLabel(id = upsertLabelData.entityID, label = upsertLabelData.label)
              else throw new BaseException(constants.Response.UNAUTHORIZED)

              for {
                provisionedAddresses <- getProvisionedAddress(upsertLabelData.entityID)
                _ <- checkAndUpdate(provisionedAddresses)
              } yield ()
            case constants.Blockchain.Entity.ORDER =>
              val makerID = masterOrders.Service.tryGetMakerID(upsertLabelData.entityID)

              def checkAndUpdate(provisionedAddresses: Seq[String]) = if (provisionedAddresses.contains(loginState.address)) masterOrders.Service.updateLabel(id = upsertLabelData.entityID, label = upsertLabelData.label)
              else throw new BaseException(constants.Response.UNAUTHORIZED)

              for {
                makerID <- makerID
                provisionedAddresses <- getProvisionedAddress(makerID)
                _ <- checkAndUpdate(provisionedAddresses)
              } yield ()
            case _ => Future(throw new BaseException(constants.Response.UNAUTHORIZED))
          }

          (for {
            _ <- verifyAndUpdate
            result <- withUsernameToken.Ok(views.html.dashboard(successes = Seq(constants.Response.LABEL_UPDATED)))
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.dashboard(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def updatePrivatePropertyForm(entityID: String, entityType: String, name: String): Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
      Ok(masterComponent.updatePrivateProperty(entityID = entityID, entityType = entityType, name = name))
  }

  def updatePrivateProperty(): Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      masterCompanion.UpdatePrivatProperty.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(masterComponent.updatePrivateProperty(formWithErrors, formWithErrors.data.getOrElse(constants.FormField.ENTITY_ID.name, ""), formWithErrors.data.getOrElse(constants.FormField.ENTITY_TYPE.name, ""), formWithErrors.data.getOrElse(constants.FormField.NAME.name, ""))))
        },
        updatePrivatePropertyData => {
          val property = masterProperties.Service.tryGet(entityID = updatePrivatePropertyData.entityID, entityType = updatePrivatePropertyData.entityType, name = updatePrivatePropertyData.name)

          def getProvisionedAddress(maintainer: String) = blockchainIdentities.Service.getAllProvisionAddresses(maintainer)

          val verifiedOwner = updatePrivatePropertyData.entityType match {
            case constants.Blockchain.Entity.IDENTITY_DEFINITION | constants.Blockchain.Entity.ASSET_DEFINITION | constants.Blockchain.Entity.ORDER_DEFINITION =>
              val maintainerID = masterClassifications.Service.tryGetMaintainerID(id = updatePrivatePropertyData.entityID)
              for {
                maintainerID <- maintainerID
                provisionedAddresses <- getProvisionedAddress(maintainerID)
              } yield provisionedAddresses.contains(loginState.address)
            case constants.Blockchain.Entity.ASSET =>
              val ownerID = masterSplits.Service.tryGetOwnerID(ownableID = updatePrivatePropertyData.entityID)
              for {
                ownerID <- ownerID
                provisionedAddresses <- getProvisionedAddress(ownerID)
              } yield provisionedAddresses.contains(loginState.address)
            case constants.Blockchain.Entity.IDENTITY =>
              for {
                provisionedAddresses <- getProvisionedAddress(updatePrivatePropertyData.entityID)
              } yield provisionedAddresses.contains(loginState.address)
            case constants.Blockchain.Entity.ORDER =>
              val makerID = masterOrders.Service.tryGetMakerID(updatePrivatePropertyData.entityID)

              for {
                makerID <- makerID
                provisionedAddresses <- getProvisionedAddress(makerID)
              } yield provisionedAddresses.contains(loginState.address)
            case _ => Future(throw new BaseException(constants.Response.UNAUTHORIZED))
          }

          def checkAndUpdate(verifiedOwner: Boolean, property: master.Property) = if (!verifiedOwner) throw new BaseException(constants.Response.UNAUTHORIZED)
          else if (utilities.Hash.getHash(updatePrivatePropertyData.value) == property.hashID) throw new BaseException(constants.Response.INVALID_PROPERTY_VALUE)
          else masterProperties.Service.updateValue(entityID = updatePrivatePropertyData.entityID, entityType = updatePrivatePropertyData.entityType, name = updatePrivatePropertyData.name, value = updatePrivatePropertyData.value)

          (for {
            property <- property
            verifiedOwner <- verifiedOwner
            _ <- checkAndUpdate(verifiedOwner, property)
            result <- withUsernameToken.Ok(views.html.dashboard(successes = Seq(constants.Response.VALUE_UPDATED)))
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.dashboard(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def properties(entityID: String, entityType: String): Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      def getProvisionedAddresses(identityID: String) = blockchainIdentities.Service.getAllProvisionAddresses(identityID)

      val checkAndGet: Future[(Boolean, Boolean, Immutables, Mutables)] = entityType match {
        case constants.Blockchain.Entity.IDENTITY_DEFINITION | constants.Blockchain.Entity.ASSET_DEFINITION | constants.Blockchain.Entity.ORDER_DEFINITION =>
          val maintainerID = masterClassifications.Service.tryGetMaintainerID(id = entityID)
          val classification = blockchainClassifications.Service.tryGet(entityID)
          for {
            maintainerID <- maintainerID
            provisionedAddresses <- getProvisionedAddresses(maintainerID)
            classification <- classification
          } yield (provisionedAddresses.contains(loginState.address), provisionedAddresses.contains(loginState.address), classification.immutableTraits, classification.mutableTraits)
        case constants.Blockchain.Entity.IDENTITY =>
          val identity = blockchainIdentities.Service.tryGet(entityID)
          for {
            provisionedAddresses <- getProvisionedAddresses(entityID)
            identity <- identity
          } yield (provisionedAddresses.contains(loginState.address), provisionedAddresses.contains(loginState.address), identity.immutables, identity.mutables)
        case constants.Blockchain.Entity.ASSET =>
          val ownerID = masterSplits.Service.tryGetOwnerID(ownableID = entityID)
          val asset = blockchainAssets.Service.tryGet(entityID)
          for {
            ownerID <- ownerID
            provisionedAddresses <- getProvisionedAddresses(ownerID)
            asset <- asset
          } yield (provisionedAddresses.contains(loginState.address), provisionedAddresses.contains(loginState.address), asset.immutables, asset.mutables)
        case constants.Blockchain.Entity.ORDER =>
          val makerID = masterOrders.Service.tryGetMakerID(entityID)
          val order = blockchainOrders.Service.tryGet(entityID)
          val identityIDs = blockchainIdentities.Service.getAllIDsByProvisioned(loginState.address)
          for {
            makerID <- makerID
            order <- order
            identityIDs <- identityIDs
          } yield (identityIDs.contains(makerID), order.getTakerID.fact.hash == "" || identityIDs.map(utilities.Hash.getHash(_)).contains(order.getTakerID.fact.hash), order.immutables, order.mutables)
        case _ => Future((false, false, Immutables(Properties(Seq.empty)), Mutables(Properties(Seq.empty))))
      }
      val properties = masterProperties.Service.getAll(entityID = entityID, entityType = entityType)

      def getBlockchainMetas(hashIDs: Seq[String]) = blockchainMetas.Service.getList(hashIDs)

      (for {
        (isOwner, canSee, immutables, mutables) <- checkAndGet
        properties <- properties
        metas <- getBlockchainMetas(properties.map(_.hashID))
      } yield {
        if (isOwner || canSee) Ok(views.html.component.master.entityProperties(isOwner = isOwner, properties = properties, immutables = immutables, mutables = mutables, metas = metas))
        else throw new BaseException(constants.Response.UNAUTHORIZED)
      }).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

}
