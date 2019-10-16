package controllers

import controllers.actions.{WithGenesisLoginAction, WithUserLoginAction}
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.{blockchain, blockchainTransaction, master}
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AddZoneController @Inject()(messagesControllerComponents: MessagesControllerComponents, transaction: utilities.Transaction, utilitiesNotification: utilities.Notification, blockchainAccounts: blockchain.Accounts, masterZoneKYCs: master.ZoneKYCs, masterOrganizations: master.Organizations, transactionsAddZone: transactions.AddZone, blockchainZones: models.blockchain.Zones, blockchainTransactionAddZones: blockchainTransaction.AddZones, masterAccounts: master.Accounts, masterZones: master.Zones, withUserLoginAction: WithUserLoginAction, withGenesisLoginAction: WithGenesisLoginAction, withUsernameToken: WithUsernameToken)(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_ADD_ZONE

  def addZoneForm(): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.addZone(views.companion.master.AddZone.form))
  }

  def addZone(): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.AddZone.form.bindFromRequest().fold(
        formWithErrors => {
          Future {
            BadRequest(views.html.component.master.addZone(formWithErrors))
          }
        },
        addZoneData => {

          val create = masterZones.Service.create(accountID = loginState.username, name = addZoneData.name, currency = addZoneData.currency)
          (for {
            _ <- create
          } yield withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.ZONE_REQUEST_SENT)))
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def verifyZoneForm(zoneID: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.verifyZone(views.companion.master.VerifyZone.form, zoneID))
  }

  def verifyZone: Action[AnyContent] = withGenesisLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.VerifyZone.form.bindFromRequest().fold(
        formWithErrors => {
          Future {
            BadRequest(views.html.component.master.verifyZone(formWithErrors, formWithErrors.data(constants.Form.ZONE_ID)))
          }
        },
        verifyZoneData => {
          /*  try {
              val zoneAccountAddress = masterAccounts.Service.getAddress(masterZones.Service.getAccountId(verifyZoneData.zoneID))
              transaction.process[blockchainTransaction.AddZone, transactionsAddZone.Request](
                entity = blockchainTransaction.AddZone(from = loginState.address, to = zoneAccountAddress, zoneID = verifyZoneData.zoneID, gas = verifyZoneData.gas, ticketID = "", mode = transactionMode),
                blockchainTransactionCreate = blockchainTransactionAddZones.Service.create,
                request = transactionsAddZone.Request(transactionsAddZone.BaseReq(from = loginState.address, gas = verifyZoneData.gas.toString), to = zoneAccountAddress, zoneID = verifyZoneData.zoneID, password = verifyZoneData.password, mode = transactionMode),
                action = transactionsAddZone.Service.post,
                onSuccess = blockchainTransactionAddZones.Utility.onSuccess,
                onFailure = blockchainTransactionAddZones.Utility.onFailure,
                updateTransactionHash = blockchainTransactionAddZones.Service.updateTransactionHash
              )
              withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.ZONE_VERIFIED)))
            }
            catch {
              case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
            }*/

          val accountID = masterZones.Service.getAccountId(verifyZoneData.zoneID)

          def zoneAccountAddress(accountID: String) = masterAccounts.Service.getAddress(accountID)

          def transactionProcess(zoneAccountAddress: String) = transaction.process[blockchainTransaction.AddZone, transactionsAddZone.Request](
            entity = blockchainTransaction.AddZone(from = loginState.address, to = zoneAccountAddress, zoneID = verifyZoneData.zoneID, gas = verifyZoneData.gas, ticketID = "", mode = transactionMode),
            blockchainTransactionCreate = blockchainTransactionAddZones.Service.create,
            request = transactionsAddZone.Request(transactionsAddZone.BaseReq(from = loginState.address, gas = verifyZoneData.gas.toString), to = zoneAccountAddress, zoneID = verifyZoneData.zoneID, password = verifyZoneData.password, mode = transactionMode),
            action = transactionsAddZone.Service.post,
            onSuccess = blockchainTransactionAddZones.Utility.onSuccess,
            onFailure = blockchainTransactionAddZones.Utility.onFailure,
            updateTransactionHash = blockchainTransactionAddZones.Service.updateTransactionHash
          )

          (for {
            accountID <- accountID
            zoneAccountAddress <- zoneAccountAddress(accountID)
            _ <- transactionProcess(zoneAccountAddress)
          } yield withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.ZONE_VERIFIED)))
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }


  def viewPendingVerifyZoneRequests: Action[AnyContent] = withGenesisLoginAction.authenticated { implicit loginState =>
    implicit request =>

      val verifyZoneRequests = masterZones.Service.getVerifyZoneRequests
      (for {
        verifyZoneRequests <- verifyZoneRequests
      } yield withUsernameToken.Ok(views.html.component.master.viewPendingVerifyZoneRequests(verifyZoneRequests))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }


  def viewKycDocuments(accountID: String): Action[AnyContent] = withGenesisLoginAction.authenticated { implicit loginState =>
    implicit request =>

      val allDocuments = masterZoneKYCs.Service.getAllDocuments(accountID)
      (for {
        allDocuments <- allDocuments
      } yield withUsernameToken.Ok(views.html.component.master.viewVerificationZoneKycDouments(allDocuments))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def verifyKycDocument(accountID: String, documentType: String): Action[AnyContent] = withGenesisLoginAction.authenticated { implicit loginState =>
    implicit request =>

      val verify = masterZoneKYCs.Service.verify(id = accountID, documentType = documentType)
      (for {
        _ <- verify
      } yield {
        utilitiesNotification.send(accountID, constants.Notification.SUCCESS, Messages(constants.Response.DOCUMENT_APPROVED.message))
        withUsernameToken.Ok(Messages(constants.Response.SUCCESS.message))
      }).recover {
        case baseException: BaseException => InternalServerError(Messages(baseException.failure.message))
      }
  }

  def rejectKycDocument(accountID: String, documentType: String): Action[AnyContent] = withGenesisLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val reject = masterZoneKYCs.Service.reject(id = accountID, documentType = documentType)
      (for {
        _ <- reject
      } yield {
        utilitiesNotification.send(accountID, constants.Notification.FAILURE, Messages(constants.Response.DOCUMENT_REJECTED.message))
        withUsernameToken.Ok(Messages(constants.Response.SUCCESS.message))
      }).recover {
        case baseException: BaseException => InternalServerError(Messages(baseException.failure.message))
      }

  }

  def rejectVerifyZoneRequestForm(zoneID: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.rejectVerifyZoneRequest(views.companion.master.RejectVerifyZoneRequest.form, zoneID))
  }

  def rejectVerifyZoneRequest: Action[AnyContent] = withGenesisLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.RejectVerifyZoneRequest.form.bindFromRequest().fold(
        formWithErrors => {
          Future {
            BadRequest(views.html.component.master.rejectVerifyZoneRequest(formWithErrors, formWithErrors.data(constants.Form.ZONE_ID)))
          }
        },
        rejectVerifyZoneRequestData => {

          val rejectZone = masterZones.Service.rejectZone(rejectVerifyZoneRequestData.zoneID)
          val accountID = masterZones.Service.getAccountId(rejectVerifyZoneRequestData.zoneID)
          def rejectAll(accountID: String) = masterZoneKYCs.Service.rejectAll(accountID)

          (for {
            _ <- rejectZone
            accountID <- accountID
            _ <- rejectAll(accountID)
          } yield withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.VERIFY_ZONE_REJECTED)))
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }


  def viewZonesInGenesis: Action[AnyContent] = withGenesisLoginAction.authenticated { implicit loginState =>
    implicit request =>

      val allZones = masterZones.Service.getAll
      (for {
        allZones <- allZones
      } yield withUsernameToken.Ok(views.html.component.master.viewZonesInGenesis(allZones))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def blockchainAddZoneForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.blockchain.addZone(views.companion.blockchain.AddZone.form))
  }

  def blockchainAddZone: Action[AnyContent] = Action.async { implicit request =>
    views.companion.blockchain.AddZone.form.bindFromRequest().fold(
      formWithErrors => {
        Future {
          BadRequest(views.html.component.blockchain.addZone(formWithErrors))
        }
      },
      addZoneData => {

        val post = transactionsAddZone.Service.post(transactionsAddZone.Request(transactionsAddZone.BaseReq(from = addZoneData.from, gas = addZoneData.gas.toString), to = addZoneData.to, zoneID = addZoneData.zoneID, password = addZoneData.password, mode = transactionMode))
        (for {
          _ <- post
        } yield Ok(views.html.index(successes = Seq(constants.Response.ZONE_ADDED)))
          ).recover {
          case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
        }
      }
    )
  }
}
