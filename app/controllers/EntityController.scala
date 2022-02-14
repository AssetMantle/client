package controllers

import controllers.actions._
import controllers.results.WithUsernameToken
import constants.AppConfig._
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
                                  masterOrders: master.Orders,
                                  blockchainIdentityProvisions: blockchain.IdentityProvisions,
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

  def properties(entityID: String, entityType: String): Action[AnyContent] = withLoginActionAsync { implicit loginState =>
    implicit request =>
      def getProvisionedAddresses(identityID: String) = blockchainIdentityProvisions.Service.getAllProvisionAddresses(identityID)

      val checkAndGet: Future[(Boolean, Immutables, Mutables)] = entityType match {
        case constants.Blockchain.Entity.IDENTITY_DEFINITION | constants.Blockchain.Entity.ASSET_DEFINITION | constants.Blockchain.Entity.ORDER_DEFINITION =>
          val maintainerIDs = masterClassifications.Service.getMaintainerIDs(id = entityID)
          val identityIDs = blockchainIdentityProvisions.Service.getAllIDsByProvisioned(loginState.address)
          val classification = blockchainClassifications.Service.tryGet(entityID)
          for {
            maintainerIDs <- maintainerIDs
            identityIDs <- identityIDs
            classification <- classification
          } yield (identityIDs.intersect(maintainerIDs).nonEmpty, classification.immutableTraits, classification.mutableTraits)
        case constants.Blockchain.Entity.IDENTITY =>
          val identity = blockchainIdentities.Service.tryGet(entityID)
          for {
            provisionedAddresses <- getProvisionedAddresses(entityID)
            identity <- identity
          } yield (provisionedAddresses.contains(loginState.address), identity.immutables, identity.mutables)
        case constants.Blockchain.Entity.ASSET =>
          val ownerID = masterSplits.Service.tryGetOwnerID(ownableID = entityID)
          val asset = blockchainAssets.Service.tryGet(entityID)
          for {
            ownerID <- ownerID
            provisionedAddresses <- getProvisionedAddresses(ownerID)
            asset <- asset
          } yield (provisionedAddresses.contains(loginState.address), asset.immutables, asset.mutables)
        case constants.Blockchain.Entity.ORDER =>
          val makerID = masterOrders.Service.tryGetMakerID(entityID)
          val order = blockchainOrders.Service.tryGet(entityID)
          val identityIDs = blockchainIdentityProvisions.Service.getAllIDsByProvisioned(loginState.address)
          for {
            makerID <- makerID
            order <- order
            identityIDs <- identityIDs
          } yield (identityIDs.contains(makerID), order.immutables, order.mutables)
        case _ => Future((false, Immutables(Properties(Seq.empty)), Mutables(Properties(Seq.empty))))
      }
      val properties = masterProperties.Service.getAll(entityID = entityID, entityType = entityType)

      def getBlockchainMetas(hashIDs: Seq[String]) = blockchainMetas.Service.getList(hashIDs)

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
