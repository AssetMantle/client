package controllers

import controllers.actions._
import controllers.results.WithUsernameToken
import utilities.Configuration.OtherApp
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
                                  withLoginActionAsync: WithLoginActionAsync,
                                  withUnknownLoginAction: WithUnknownLoginAction,
                                  withUserLoginAction: WithUserLoginAction,
                                  withUsernameToken: WithUsernameToken,
                                  withoutLoginAction: WithoutLoginAction,
                                  withoutLoginActionAsync: WithoutLoginActionAsync
                                )(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_ENTITY

  private implicit val otherApps: Seq[OtherApp] = configuration.get[Seq[Configuration]]("webApp.otherApps").map { otherApp =>
    OtherApp(url = otherApp.get[String]("url"), name = otherApp.get[String]("name"))
  }

  def upsertLabelForm(entityID: String, entityType: String): Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
      Ok(masterComponent.upsertEntityLabel(entityID = entityID, entityType = entityType))
  }

  def upsertLabel(): Action[AnyContent] = withLoginActionAsync { implicit loginState =>
    implicit request =>
      masterCompanion.UpsertEntityLabel.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(masterComponent.upsertEntityLabel(formWithErrors, formWithErrors.data.getOrElse(constants.FormField.ENTITY_ID.name, ""), formWithErrors.data.getOrElse(constants.FormField.ENTITY_TYPE.name, ""))))
        },
        upsertLabelData => {
          def getProvisionedAddress(maintainer: String) = blockchainIdentities.Service.getAllProvisionAddressesByIdentityWithActor(maintainer)

          val verifyAndUpdate = upsertLabelData.entityType match {
            case constants.Blockchain.Entity.IDENTITY_DEFINITION | constants.Blockchain.Entity.ASSET_DEFINITION | constants.Blockchain.Entity.ORDER_DEFINITION =>
              val maintainerIDs = masterClassifications.Service.getMaintainerIDs(upsertLabelData.entityID)
              val identityIDs = blockchainIdentities.Service.getAllIDsByProvisioned(loginState.address)

              def checkAndUpdate(maintainerIDs: Seq[String], identityIDs: Seq[String]) = if (identityIDs.intersect(maintainerIDs).nonEmpty) masterClassifications.Service.updateLabel(id = upsertLabelData.entityID, maintainerID = identityIDs.intersect(maintainerIDs).headOption.getOrElse(""), label = upsertLabelData.label)
              else throw new BaseException(constants.Response.UNAUTHORIZED)

              for {
                maintainerIDs <- maintainerIDs
                identityIDs <- identityIDs
                _ <- checkAndUpdate(maintainerIDs, identityIDs)
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
            result <- withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.LABEL_UPDATED)))
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def properties(entityID: String, entityType: String): Action[AnyContent] = withLoginActionAsync { implicit loginState =>
    implicit request =>
      def getProvisionedAddresses(identityID: String) = blockchainIdentities.Service.getAllProvisionAddressesByIdentityWithActor(identityID)

      val checkAndGet: Future[(Boolean, Immutables, Mutables)] = entityType match {
        case constants.Blockchain.Entity.IDENTITY_DEFINITION | constants.Blockchain.Entity.ASSET_DEFINITION | constants.Blockchain.Entity.ORDER_DEFINITION =>
          val maintainerIDs = masterClassifications.Service.getMaintainerIDs(id = entityID)
          val identityIDs = blockchainIdentities.Service.getAllIDsByProvisioned(loginState.address)
          val classification = blockchainClassifications.Service.tryGetClassificationWithActor(entityID)
          for {
            maintainerIDs <- maintainerIDs
            identityIDs <- identityIDs
            classification <- classification
          } yield (identityIDs.intersect(maintainerIDs).nonEmpty, classification.immutableTraits, classification.mutableTraits)
        case constants.Blockchain.Entity.IDENTITY =>
          val identity = blockchainIdentities.Service.tryGetIdentityWithActor(entityID)
          for {
            provisionedAddresses <- getProvisionedAddresses(entityID)
            identity <- identity
          } yield (provisionedAddresses.contains(loginState.address), identity.immutables, identity.mutables)
        case constants.Blockchain.Entity.ASSET =>
          val ownerID = masterSplits.Service.tryGetOwnerID(ownableID = entityID)
          val asset = blockchainAssets.Service.tryGetAssetWithActor(entityID)
          for {
            ownerID <- ownerID
            provisionedAddresses <- getProvisionedAddresses(ownerID)
            asset <- asset
          } yield (provisionedAddresses.contains(loginState.address), asset.immutables, asset.mutables)
        case constants.Blockchain.Entity.ORDER =>
          val makerID = masterOrders.Service.tryGetMakerID(entityID)
          val order = blockchainOrders.Service.tryGetOrderWithActor(entityID)
          val identityIDs = blockchainIdentities.Service.getAllIDsByProvisioned(loginState.address)
          for {
            makerID <- makerID
            order <- order
            identityIDs <- identityIDs
          } yield (identityIDs.contains(makerID), order.immutables, order.mutables)
        case _ => Future((false, Immutables(Properties(Seq.empty)), Mutables(Properties(Seq.empty))))
      }
      val properties = masterProperties.Service.getAll(entityID = entityID, entityType = entityType)

      def getBlockchainMetas(hashIDs: Seq[String]) = blockchainMetas.Service.getListWithActor(hashIDs)

      (for {
        (isOwner, immutables, mutables) <- checkAndGet
        properties <- properties
        metas <- getBlockchainMetas(properties.map(_.hashID))
      } yield {
        if (isOwner) Ok(views.html.component.master.entityProperties(isOwner = isOwner, properties = properties, immutables = immutables, mutables = mutables, metas = metas))
        else throw new BaseException(constants.Response.UNAUTHORIZED)
      }).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

}
