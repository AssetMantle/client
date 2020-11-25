package controllers

import controllers.actions._
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.{blockchain, blockchainTransaction, master}
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

  def addLabelForm(entityID: String, entityType: String): Action[AnyContent] = withoutLoginAction { implicit request =>
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

          def verifyAndUpdate(identityIDs: Seq[String]) = addLabelData.label match {
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
            case _ => throw new BaseException(constants.Response.UNAUTHORIZED)
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


}
