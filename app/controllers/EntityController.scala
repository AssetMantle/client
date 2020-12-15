package controllers

import controllers.actions._
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.master.Property
import models.{blockchain, master}
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}
import views.companion.{master => masterCompanion}
import views.html.component.{master => masterComponent}

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

  def addLabelForm(entityID: String, entityType: String): Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
    Ok(masterComponent.addEntityLabel(entityID = entityID, entityType = entityType))
  }

  def addLabel(): Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      masterCompanion.AddEntityLabel.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(masterComponent.addEntityLabel(formWithErrors, formWithErrors.data.getOrElse(constants.FormField.ENTITY_ID.name, ""), formWithErrors.data.getOrElse(constants.FormField.ENTITY_TYPE.name, ""))))
        },
        addLabelData => {
          val identityIDs = blockchainIdentities.Service.getAllIDsByProvisioned(loginState.address)

          def verifyAndUpdate(identityIDs: Seq[String]) = addLabelData.entityType match {
            case constants.Blockchain.Entity.IDENTITY_DEFINITION | constants.Blockchain.Entity.ASSET_DEFINITION | constants.Blockchain.Entity.ORDER_DEFINITION =>
              val classificationFromID = masterClassifications.Service.tryGetFromID(id = addLabelData.entityID, entityType = addLabelData.entityType)

              def checkAndUpdate(classificationFromID: String) = if (identityIDs.contains(classificationFromID)) masterClassifications.Service.updateLabel(id = addLabelData.entityID, entityType = addLabelData.entityType, label = addLabelData.label)
              else throw new BaseException(constants.Response.UNAUTHORIZED)

              for {
                classificationFromID <- classificationFromID
                _ <- checkAndUpdate(classificationFromID)
              } yield ()
            case constants.Blockchain.Entity.ASSET =>
              val ownerID = masterAssets.Service.tryGetOwnerID(addLabelData.entityID)

              def checkAndUpdate(ownerID: String) = if (identityIDs.contains(ownerID)) {
                val updateAssetLabel = masterAssets.Service.updateLabel(id = addLabelData.entityID, label = addLabelData.label)
                val updateSplitLabel = masterSplits.Service.updateLabel(ownerID = addLabelData.entityID, entityType = addLabelData.entityType, label = addLabelData.label)
                for {
                  _ <- updateAssetLabel
                  _ <- updateSplitLabel
                } yield ()
              } else throw new BaseException(constants.Response.UNAUTHORIZED)

              for {
                ownerID <- ownerID
                _ <- checkAndUpdate(ownerID)
              } yield ()
            case constants.Blockchain.Entity.IDENTITY =>
              val checkAndUpdate = if (identityIDs.contains(addLabelData.entityID)) masterIdentities.Service.updateLabel(id = addLabelData.entityID, label = addLabelData.label)
              else throw new BaseException(constants.Response.UNAUTHORIZED)
              for {
                _ <- checkAndUpdate
              } yield ()
            case constants.Blockchain.Entity.ORDER =>
              val makerID = masterOrders.Service.tryGetMakerID(addLabelData.entityID)

              def checkAndUpdate(makerID: String) = if (identityIDs.contains(makerID)) masterOrders.Service.updateLabel(id = addLabelData.entityID, label = addLabelData.label)
              else throw new BaseException(constants.Response.UNAUTHORIZED)

              for {
                makerID <- makerID
                _ <- checkAndUpdate(makerID)
              } yield ()
            case constants.Blockchain.Entity.WRAPPED_COIN =>
              val ownerID = masterSplits.Service.tryGetOwnerID(entityID = addLabelData.entityID, entityType = addLabelData.entityType)

              def checkAndUpdate(ownerID: String) = if (identityIDs.contains(ownerID)) masterSplits.Service.updateLabel(ownerID = addLabelData.entityID, entityType = addLabelData.entityType, label = addLabelData.label)
              else throw new BaseException(constants.Response.UNAUTHORIZED)

              for {
                ownerID <- ownerID
                _ <- checkAndUpdate(ownerID)
              } yield ()
            case _ => Future(throw new BaseException(constants.Response.UNAUTHORIZED))
          }

          (for {
            identityIDs <- identityIDs
            _ <- verifyAndUpdate(identityIDs)
            result <- withUsernameToken.Ok(views.html.dashboard(successes = Seq(constants.Response.LABEL_UPDATED)))
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.dashboard(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def addPrivatePropertyForm(entityID: String, entityType: String, name: String): Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
    Ok(masterComponent.addPrivateProperty(entityID = entityID, entityType = entityType, name = name))
  }

  def addPrivateProperty(): Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      masterCompanion.AddPrivatProperty.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(masterComponent.addPrivateProperty(formWithErrors, formWithErrors.data.getOrElse(constants.FormField.ENTITY_ID.name, ""), formWithErrors.data.getOrElse(constants.FormField.ENTITY_TYPE.name, ""), formWithErrors.data.getOrElse(constants.FormField.NAME.name, ""))))
        },
        addPrivatePropertyData => {
          val property = masterProperties.Service.tryGet(entityID = addPrivatePropertyData.entityID, entityType = addPrivatePropertyData.entityType, name = addPrivatePropertyData.name)
          val identityIDs = blockchainIdentities.Service.getAllIDsByProvisioned(loginState.address)

          def verifyAndUpdate(identityIDs: Seq[String], property: Property) = {
            if (utilities.Hash.getHash(addPrivatePropertyData.value) == property.hashID) {
              val verifiedOwner = addPrivatePropertyData.entityType match {
                case constants.Blockchain.Entity.IDENTITY_DEFINITION | constants.Blockchain.Entity.ASSET_DEFINITION | constants.Blockchain.Entity.ORDER_DEFINITION =>
                  val classificationFromID = masterClassifications.Service.tryGetFromID(id = addPrivatePropertyData.entityID, entityType = addPrivatePropertyData.entityType)
                  for {
                    classificationFromID <- classificationFromID
                  } yield identityIDs.contains(classificationFromID)
                case constants.Blockchain.Entity.ASSET =>
                  val ownerID = masterAssets.Service.tryGetOwnerID(addPrivatePropertyData.entityID)
                  for {
                    ownerID <- ownerID
                  } yield identityIDs.contains(ownerID)
                case constants.Blockchain.Entity.IDENTITY => Future(identityIDs.contains(addPrivatePropertyData.entityID))
                case constants.Blockchain.Entity.ORDER =>
                  val makerID = masterOrders.Service.tryGetMakerID(addPrivatePropertyData.entityID)

                  for {
                    makerID <- makerID
                  } yield identityIDs.contains(makerID)
                case constants.Blockchain.Entity.WRAPPED_COIN =>
                  val ownerID = masterSplits.Service.tryGetOwnerID(entityID = addPrivatePropertyData.entityID, entityType = addPrivatePropertyData.entityType)

                  for {
                    ownerID <- ownerID
                  } yield identityIDs.contains(ownerID)
                case _ => Future(throw new BaseException(constants.Response.UNAUTHORIZED))
              }

              def checkAndUpdate(verifiedOwner: Boolean) = if (verifiedOwner) masterProperties.Service.updateValue(entityID = addPrivatePropertyData.entityID, entityType = addPrivatePropertyData.entityType, name = addPrivatePropertyData.name, value = addPrivatePropertyData.value)
              else Future(throw new BaseException(constants.Response.UNAUTHORIZED))

              for {
                verifiedOwner <- verifiedOwner
                _ <- checkAndUpdate(verifiedOwner)
                result <- withUsernameToken.Ok(views.html.dashboard(successes = Seq(constants.Response.VALUE_UPDATED)))
              } yield result
            } else Future(BadRequest(masterComponent.addPrivateProperty(masterCompanion.AddPrivatProperty.form.fill(addPrivatePropertyData).withGlobalError(constants.Response.INCORRECT_PROPERTY_VALUE.message), entityID = addPrivatePropertyData.entityID, entityType = addPrivatePropertyData.entityType, name = addPrivatePropertyData.name)))
          }

          (for {
            property <- property
            identityIDs <- identityIDs
            result <- verifyAndUpdate(identityIDs, property)
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.dashboard(failures = Seq(baseException.failure)))
          }
        }
      )
  }

}
