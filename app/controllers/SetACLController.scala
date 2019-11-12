package controllers

import java.nio.file.Files

import controllers.actions._
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.master.{Organization, Trader}
import models.masterTransaction.AddTraderRequest
import models.{blockchain, blockchainTransaction, master, masterTransaction}
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}
import views.companion.master.FileUpload

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SetACLController @Inject()(messagesControllerComponents: MessagesControllerComponents, masterTransactionAddTraderRequests: masterTransaction.AddTraderRequests, withTraderLoginAction: WithTraderLoginAction, transaction: utilities.Transaction, fileResourceManager: utilities.FileResourceManager, blockchainAccounts: blockchain.Accounts, masterZones: master.Zones, masterOrganizations: master.Organizations, masterTraders: master.Traders, masterTraderKYCs: master.TraderKYCs, withZoneLoginAction: WithZoneLoginAction, withOrganizationLoginAction: WithOrganizationLoginAction, withUserLoginAction: WithUserLoginAction, withGenesisLoginAction: WithGenesisLoginAction, masterAccounts: master.Accounts, transactionsSetACL: transactions.SetACL, blockchainAclAccounts: blockchain.ACLAccounts, blockchainTransactionSetACLs: blockchainTransaction.SetACLs, blockchainAclHashes: blockchain.ACLHashes, utilitiesNotification: utilities.Notification, withUsernameToken: WithUsernameToken)(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private implicit val module: String = constants.Module.CONTROLLERS_SET_ACL

  def inviteTraderForm(): Action[AnyContent] = Action {
    implicit request =>
      Ok(views.html.component.master.inviteTrader())
  }

  def inviteTrader(): Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.InviteTrader.form.bindFromRequest().fold(
        formWithErrors => {
          Future{BadRequest(views.html.component.master.inviteTrader(formWithErrors))}
        },
        addTraderRequestData => {

          val organizationID=masterOrganizations.Service.getID(loginState.username)
          def requestID(organizationID:String)=masterTransactionAddTraderRequests.Service.create(accountID = addTraderRequestData.accountID, organizationID = organizationID)
          for{
            organizationID<-organizationID
            requestID<-requestID(organizationID)
          }yield {
            utilitiesNotification.send(accountID = addTraderRequestData.accountID, notification = constants.Notification.TRADER_INVITATION, routes.SetACLController.addTraderRequestForm(requestID).url)
            withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.INVITATION_EMAIL_SENT)))
          }
        }
      )
  }

  def addTraderRequestForm(requestID: String): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>

      val addTraderRequest = masterTransactionAddTraderRequests.Service.get(requestID)

      def getResult(addTraderRequest:AddTraderRequest)={
        if(addTraderRequest.accountID == loginState.username){
          val zoneID=masterOrganizations.Service.getZoneID(addTraderRequest.organizationID)
          for{
            zoneID<-zoneID
          }yield{
            Ok(views.html.component.master.addTrader(views.companion.master.AddTrader.form.fill(views.companion.master.AddTrader.Data(zoneID = zoneID, organizationID = addTraderRequest.organizationID, name = ""))))
          }
        }else {
          Future{Unauthorized(views.html.index(failures = Seq(constants.Response.UNAUTHORIZED)))}
        }
      }
      (for{
        addTraderRequest<-addTraderRequest
        result<-getResult(addTraderRequest)
      }yield result
        ).recover{
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

 /* def addTraderRequest(): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.AddTrader.form.bindFromRequest().fold(
        formWithErrors => {
          Future{BadRequest(views.html.component.master.addTrader(formWithErrors))}
        },
        addTraderData => {
       /*   try {
            if (masterOrganizations.Service.getVerificationStatus(addTraderData.organizationID)) {
              val id = masterTraders.Service.insertOrUpdateTrader(zoneID = addTraderData.zoneID, organizationID = addTraderData.organizationID, accountID = loginState.username, name = addTraderData.name)
              PartialContent(views.html.component.master.userUploadOrUpdateTraderKYC(masterTraderKYCs.Service.getAllDocuments(id)))
            } else {
              Unauthorized(views.html.index(failures = Seq(constants.Response.UNVERIFIED_ORGANIZATION)))
            }
          }
          catch {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }*/
          val verificationStatus=masterOrganizations.Service.getVerificationStatus(addTraderData.organizationID)
          def getResult(verificationStatus:Boolean)={
            if (verificationStatus) {
              val id = masterTraders.Service.insertOrUpdateTrader(zoneID = addTraderData.zoneID, organizationID = addTraderData.organizationID, accountID = loginState.username, name = addTraderData.name)
              def allDocuments(id:String)=masterTraderKYCs.Service.getAllDocuments(id)
              for{
                id<-id
                allDocuments<-allDocuments(id)
              } yield PartialContent(views.html.component.master.userUploadOrUpdateTraderKYC(allDocuments))
            } else {
              Future{Unauthorized(views.html.index(failures = Seq(constants.Response.UNVERIFIED_ORGANIZATION)))}
            }
          }
          (for{
            verificationStatus<-verificationStatus
            result<-getResult(verificationStatus)
          }yield result
            ).recover{
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }*/
 //TODO Change form it should only contain organization ID
  def addTraderForm(): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>

      val trader = masterTraders.Service.getByAccountID(loginState.username)
      (for{
        trader<-trader
      }yield Ok(views.html.component.master.addTrader(views.companion.master.AddTrader.form.fill(views.companion.master.AddTrader.Data(zoneID = trader.zoneID, organizationID = trader.organizationID, name = trader.name))))
        ).recover{
        case _: BaseException => Ok(views.html.component.master.addTrader(views.companion.master.AddTrader.form))
      }
  }

  def addTrader(): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.AddTrader.form.bindFromRequest().fold(
        formWithErrors => {
          Future{BadRequest(views.html.component.master.addTrader(formWithErrors))}
        },
        addTraderData => {

          val verificationStatus=masterOrganizations.Service.getVerificationStatus(addTraderData.organizationID)
          def getResult(verificationStatus:Boolean)={
            if (verificationStatus) {
              val id = masterTraders.Service.insertOrUpdateTrader(zoneID = addTraderData.zoneID, organizationID = addTraderData.organizationID, accountID = loginState.username, name = addTraderData.name)
              def traderKYCs(id:String)=masterTraderKYCs.Service.getAllDocuments(id)
              for{
                id<-id
                traderKYCs<-traderKYCs(id)
              } yield PartialContent(views.html.component.master.userUploadOrUpdateTraderKYC(traderKYCs))
            } else {
              Future{Unauthorized(views.html.index(failures = Seq(constants.Response.UNVERIFIED_ORGANIZATION)))}
            }
          }
          (for{
            verificationStatus<-verificationStatus
            result<-getResult(verificationStatus)
          }yield result
            ).recover{
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def userUploadOrUpdateTraderKYCView(): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>

      val id=masterTraders.Service.getID(loginState.username)
      def traderKYCs(id:String)=masterTraderKYCs.Service.getAllDocuments(id)
      (for{
        id<-id
        traderKYCs<-traderKYCs(id)
      }yield Ok(views.html.component.master.userUploadOrUpdateTraderKYC(traderKYCs))
        ).recover{
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def userUploadTraderKYCForm(documentType: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.uploadFile(utilities.String.getJsRouteFunction(routes.javascript.SetACLController.userUploadTraderKYC), utilities.String.getJsRouteFunction(routes.javascript.SetACLController.userStoreTraderKYC), documentType))
  }

  def userUploadTraderKYC(documentType: String) = Action(parse.multipartFormData) { implicit request =>
    FileUpload.form.bindFromRequest.fold(
      formWithErrors => {
        BadRequest
      },
      fileUploadInfo => {
        try {
          request.body.file(constants.File.KEY_FILE) match {
            case None => BadRequest(views.html.index(failures = Seq(constants.Response.NO_FILE)))
            case Some(file) => utilities.FileOperations.savePartialFile(Files.readAllBytes(file.ref.path), fileUploadInfo, fileResourceManager.getTraderKYCFilePath(documentType))
              Ok
          }
        }
        catch {
          case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
        }
      }
    )
  }

  def userStoreTraderKYC(name: String, documentType: String): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>
     /* try {
        fileResourceManager.storeFile[master.TraderKYC](
          name = name,
          documentType = documentType,
          path = fileResourceManager.getTraderKYCFilePath(documentType),
          document = master.TraderKYC(id = masterTraders.Service.getID(loginState.username), documentType = documentType, fileName = name, file = None, zoneStatus = None, organizationStatus = None),
          masterCreate = masterTraderKYCs.Service.create
        )
        withUsernameToken.PartialContent(views.html.component.master.userUploadOrUpdateTraderKYC(masterTraderKYCs.Service.getAllDocuments(masterTraders.Service.getID(loginState.username))))
      } catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }*/
      val id=masterTraders.Service.getID(loginState.username)
      def storeFile(id:String)=fileResourceManager.storeFile[master.TraderKYC](
        name = name,
        documentType = documentType,
        path = fileResourceManager.getTraderKYCFilePath(documentType),
       document = master.TraderKYC(id = id, documentType = documentType, fileName = name, file = None, zoneStatus = None, organizationStatus = None),
       masterCreate = masterTraderKYCs.Service.create
    )
      def allDocuments(id:String)=masterTraderKYCs.Service.getAllDocuments(id)
      (for{
       id<-id
       _<-storeFile(id)
       allDocuments<-allDocuments(id)
     }yield PartialContent(views.html.component.master.userUploadOrUpdateTraderKYC(allDocuments))
        ).recover{
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def userUpdateTraderKYCForm(documentType: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.updateFile(utilities.String.getJsRouteFunction(routes.javascript.SetACLController.userUploadTraderKYC), utilities.String.getJsRouteFunction(routes.javascript.SetACLController.userUpdateTraderKYC), documentType))
  }

  def userUpdateTraderKYC(name: String, documentType: String): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>

      val id = masterTraders.Service.getID(loginState.username)
      def oldDocumentFileName(id:String)=masterTraderKYCs.Service.getFileName(id = id, documentType = documentType)
      def updateFile(id:String,oldDocumentFileName:String)=fileResourceManager.updateFile[master.TraderKYC](
        name = name,
        documentType = documentType,
        path = fileResourceManager.getTraderKYCFilePath(documentType),
        oldDocumentFileName = oldDocumentFileName,
        document = master.TraderKYC(id = id, documentType = documentType, fileName = name, file = None, zoneStatus = None, organizationStatus = None),
        updateOldDocument = masterTraderKYCs.Service.updateOldDocument
      )
      def allDocuments(id:String)=masterTraderKYCs.Service.getAllDocuments(id)
      (for{
        id<-id
        oldDocumentFileName<-oldDocumentFileName(id)
        _<-updateFile(id,oldDocumentFileName)
        allDocuments<-allDocuments(id)
      }yield PartialContent(views.html.component.master.userUploadOrUpdateTraderKYC(allDocuments))
        ).recover{
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def userReviewAddTraderRequestForm(): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>

      val trader = masterTraders.Service.getByAccountID(loginState.username)
      def getResult(trader:Trader)={
        val organization=masterOrganizations.Service.get(trader.organizationID)
        val zone=masterZones.Service.get(trader.zoneID)
        val traderKYCs=masterTraderKYCs.Service.getAllDocuments(trader.id)
        for{
          organization<-organization
          zone<-zone
          traderKYCs<-traderKYCs
        }yield Ok(views.html.component.master.userReviewAddTraderRequest( trader = trader, organization = organization, zone = zone, traderKYCs = traderKYCs))
      }
      (for{
      trader<-trader
      result<-getResult(trader)
    }yield result
        ).recover{
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def userReviewAddTraderRequest(): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.ReviewAddTraderRequest.form.bindFromRequest().fold(
        formWithErrors => {

          val trader = masterTraders.Service.getByAccountID(loginState.username)
          def getResult(trader:Trader)={
            val organization=masterOrganizations.Service.get(trader.organizationID)
            val zone=masterZones.Service.get(trader.zoneID)
            val traderKYCs=masterTraderKYCs.Service.getAllDocuments(trader.id)
            for{
              organization<-organization
              zone<-zone
              traderKYCs<-traderKYCs
            }yield BadRequest(views.html.component.master.userReviewAddTraderRequest(formWithErrors, trader = trader, organization = organization, zone = zone, traderKYCs = traderKYCs))
          }
          (for{
            trader<-trader
            result<-getResult(trader)
          }yield result
            ).recover{
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        },
        reviewTraderCompletionData => {
          val id = masterTraders.Service.getID(loginState.username)
          def allKYCFileTypesExists(id:String)=masterTraderKYCs.Service.checkAllKYCFileTypesExists(id)
          def getResult(id:String,allKYCFileTypesExists:Boolean)={
            if (reviewTraderCompletionData.completion && allKYCFileTypesExists) {
              val markTraderFormCompleted=masterTraders.Service.markTraderFormCompleted(id)
              for{
                _<-markTraderFormCompleted
              }yield withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.TRADER_ADDED_FOR_VERIFICATION)))
            } else {
              val trader = masterTraders.Service.getByAccountID(loginState.username)
              def getResult(trader:Trader)={
                val organization=masterOrganizations.Service.get(trader.organizationID)
                val zone=masterZones.Service.get(trader.zoneID)
                val traderKYCs=masterTraderKYCs.Service.getAllDocuments(trader.id)
                for{
                  organization<-organization
                  zone<-zone
                  traderKYCs<-traderKYCs
                }yield  BadRequest(views.html.component.master.userReviewAddTraderRequest(trader = trader, organization = organization, zone =zone, traderKYCs = traderKYCs))
              }
              for{
                trader<-trader
                result<-getResult(trader)
              }yield result
            }
          }
          (for{
            id<-id
            allKYCFileTypesExists<-allKYCFileTypesExists(id)
            result<-getResult(id,allKYCFileTypesExists)
          }yield result
            ).recover{
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def zoneVerifyTraderForm(accountID: String, organizationID: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.zoneVerifyTrader(views.companion.master.VerifyTrader.form.fill(views.companion.master.VerifyTrader.Data(accountID = accountID, organizationID = organizationID, gas = constants.FormField.GAS.minimumValue, password = ""))))
  }

  def zoneVerifyTrader: Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.VerifyTrader.form.bindFromRequest().fold(
        formWithErrors => {
          Future{BadRequest(views.html.component.master.zoneVerifyTrader(formWithErrors))}
        },
        verifyTraderData => {
          val verificationStatusWithTry=masterOrganizations.Service.getVerificationStatusWithTry(verifyTraderData.organizationID)
          val id=masterTraders.Service.getID(verifyTraderData.accountID)
          def checkAllKYCFilesVerified(id:String)=masterTraderKYCs.Service.checkAllKYCFilesVerified(id)
          def getResult(verificationStatusWithTry:Boolean,checkAllKYCFilesVerified:Boolean)={
            if(verificationStatusWithTry && checkAllKYCFilesVerified){
              val aclAddress = masterAccounts.Service.getAddress(verifyTraderData.accountID)
              val zoneID = masterOrganizations.Service.getZoneID(verifyTraderData.organizationID)
              val acl = blockchain.ACL(issueAsset = verifyTraderData.issueAsset, issueFiat = verifyTraderData.issueFiat, sendAsset = verifyTraderData.sendAsset, sendFiat = verifyTraderData.sendFiat, redeemAsset = verifyTraderData.redeemAsset, redeemFiat = verifyTraderData.redeemFiat, sellerExecuteOrder = verifyTraderData.sellerExecuteOrder, buyerExecuteOrder = verifyTraderData.buyerExecuteOrder, changeBuyerBid = verifyTraderData.changeBuyerBid, changeSellerBid = verifyTraderData.changeSellerBid, confirmBuyerBid = verifyTraderData.confirmBuyerBid, confirmSellerBid = verifyTraderData.changeSellerBid, negotiation = verifyTraderData.negotiation, releaseAsset = verifyTraderData.releaseAsset)
              val createACL=blockchainAclHashes.Service.create(acl)
              def transactionProcess(aclAddress:String,zoneID:String)=transaction.process[blockchainTransaction.SetACL, transactionsSetACL.Request](
                entity = blockchainTransaction.SetACL(from = loginState.address, aclAddress = aclAddress, organizationID = verifyTraderData.organizationID, zoneID = zoneID, aclHash = util.hashing.MurmurHash3.stringHash(acl.toString).toString, gas = verifyTraderData.gas, ticketID = "", mode = transactionMode),
                blockchainTransactionCreate = blockchainTransactionSetACLs.Service.create,
                request = transactionsSetACL.Request(transactionsSetACL.BaseReq(from = loginState.address, gas = verifyTraderData.gas.toString), password = verifyTraderData.password, aclAddress = aclAddress, organizationID = verifyTraderData.organizationID, zoneID = zoneID, issueAsset = verifyTraderData.issueAsset.toString, issueFiat = verifyTraderData.issueFiat.toString, sendAsset = verifyTraderData.sendAsset.toString, sendFiat = verifyTraderData.sendFiat.toString, redeemAsset = verifyTraderData.redeemAsset.toString, redeemFiat = verifyTraderData.redeemFiat.toString, sellerExecuteOrder = verifyTraderData.sellerExecuteOrder.toString, buyerExecuteOrder = verifyTraderData.buyerExecuteOrder.toString, changeBuyerBid = verifyTraderData.changeBuyerBid.toString, changeSellerBid = verifyTraderData.changeSellerBid.toString, confirmBuyerBid = verifyTraderData.confirmBuyerBid.toString, confirmSellerBid = verifyTraderData.confirmSellerBid.toString, negotiation = verifyTraderData.negotiation.toString, releaseAsset = verifyTraderData.releaseAsset.toString, mode = transactionMode),
                action = transactionsSetACL.Service.post,
                onSuccess = blockchainTransactionSetACLs.Utility.onSuccess,
                onFailure = blockchainTransactionSetACLs.Utility.onFailure,
                updateTransactionHash = blockchainTransactionSetACLs.Service.updateTransactionHash
              )
              for{
                aclAddress<-aclAddress
                zoneID<-zoneID
                _<-createACL
                _<-transactionProcess(aclAddress,zoneID)
              }yield withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.ACL_SET)))
            }else {
              Future{PreconditionFailed(views.html.index(failures = Seq(constants.Response.ALL_KYC_FILES_NOT_VERIFIED)))}
            }
          }
          (for{
            verificationStatusWithTry<-verificationStatusWithTry
            id<-id
            checkAllKYCFilesVerified<-checkAllKYCFilesVerified(id)
            result<-getResult(verificationStatusWithTry,checkAllKYCFilesVerified)
          }yield result
            ).recover{
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def zoneViewPendingVerifyTraderRequests: Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>

    val zoneID=masterZones.Service.getID(loginState.username)
    def getVerifyTraderRequestsForZone(zoneID:String)=masterTraders.Service.getVerifyTraderRequestsForZone(zoneID)
    for{
      zoneID<-zoneID
      getVerifyTraderRequestsForZone<-getVerifyTraderRequestsForZone(zoneID)
    }yield Ok(views.html.component.master.zoneViewPendingVerifyTraderRequests(getVerifyTraderRequestsForZone))
  }

  def zoneViewKYCDocuments(traderID: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>

      val traderKYCs=masterTraderKYCs.Service.getAllDocuments(traderID)
      (for{
        traderKYCs<-traderKYCs
      }yield withUsernameToken.Ok(views.html.component.master.zoneViewVerificationTraderKYCDouments(traderKYCs))
        ).recover{
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  /*def zoneVerifyKycDocument(traderID: String, documentType: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
     /* try {
        masterTraderKYCs.Service.zoneVerify(id = traderID, documentType = documentType)
        utilitiesNotification.send(accountID = masterTraders.Service.getAccountId(traderID), notification = constants.Notification.SUCCESS, Messages(constants.Response.DOCUMENT_APPROVED.message))
        withUsernameToken.Ok(Messages(constants.Response.SUCCESS.message))
      } catch {
        case baseException: BaseException => InternalServerError(Messages(baseException.failure.message))
      }*/
      val traderKYCsZoneVerify=masterTraderKYCs.Service.zoneVerify(id = traderID, documentType = documentType)
      def traderAccountID=masterTraders.Service.getAccountId(traderID)
      (for{
        _<-traderKYCsZoneVerify
        traderAccountID<-traderAccountID
      }yield {
        utilitiesNotification.send(accountID = traderAccountID, notification = constants.Notification.SUCCESS, Messages(constants.Response.DOCUMENT_APPROVED.message))
        withUsernameToken.Ok(Messages(constants.Response.SUCCESS.message))
      }).recover{
        case baseException: BaseException => InternalServerError(Messages(baseException.failure.message))
      }
  }*/

  def updateTraderKYCDocumentZoneStatusForm(traderID: String, documentType: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>

      val userZoneID=masterZones.Service.getID(loginState.username)
      val traderZoneID=masterTraders.Service.getZoneID(traderID)
      def getResult(userZoneID:String,traderZoneID:String)={
        if(userZoneID == traderZoneID){
          val traderKYC=masterTraderKYCs.Service.get(id = traderID, documentType = documentType)
          for{
            traderKYC <-traderKYC
          }yield  withUsernameToken.Ok(views.html.component.master.updateTraderKYCDocumentZoneStatus(traderKYC = traderKYC))

        }else Future{Unauthorized(views.html.index(failures = Seq(constants.Response.UNAUTHORIZED)))}

      }
      (for{
        userZoneID<-userZoneID
        traderZoneID<-traderZoneID
        result<-getResult(userZoneID,traderZoneID)
      } yield result
        ).recover{
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def updateTraderKYCDocumentZoneStatus(): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.UpdateTraderKYCDocumentZoneStatus.form.bindFromRequest().fold(
        formWithErrors => {

          val traderKYC=masterTraderKYCs.Service.get(id = formWithErrors(constants.FormField.TRADER_ID.name).value.get, documentType = formWithErrors(constants.FormField.DOCUMENT_TYPE.name).value.get)
          (for{
            traderKYC <-traderKYC
          }yield BadRequest(views.html.component.master.updateTraderKYCDocumentZoneStatus(formWithErrors, traderKYC))
            ).recover{
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        },
        updateTraderKYCDocumentZoneStatusData => {

          val userZoneID=masterZones.Service.getID(loginState.username)
          val traderZoneID= masterTraders.Service.getZoneID(updateTraderKYCDocumentZoneStatusData.traderID)
          def verifyOrReject(userZoneID:String,traderZoneID:String)={
            val traderID=masterTraders.Service.getAccountId(updateTraderKYCDocumentZoneStatusData.traderID)
            if(userZoneID == traderZoneID){
              if (updateTraderKYCDocumentZoneStatusData.zoneStatus) {
                val zoneVerify=masterTraderKYCs.Service.zoneVerify(id = updateTraderKYCDocumentZoneStatusData.traderID, documentType = updateTraderKYCDocumentZoneStatusData.documentType)
                for{
                  _<-zoneVerify
                  traderID<-traderID
                }yield utilitiesNotification.send(traderID, constants.Notification.SUCCESS, Messages(constants.Response.DOCUMENT_APPROVED.message))
              } else {
                val zoneReject=masterTraderKYCs.Service.zoneReject(id = updateTraderKYCDocumentZoneStatusData.traderID, documentType = updateTraderKYCDocumentZoneStatusData.documentType)
                for{
                  _<-zoneReject
                  traderID<-traderID
                }yield utilitiesNotification.send(traderID, constants.Notification.FAILURE, Messages(constants.Response.DOCUMENT_REJECTED.message))
              }
            }else {
              Future{Unauthorized(views.html.index(failures = Seq(constants.Response.UNAUTHORIZED)))}
            }
          }
          def traderKYC=masterTraderKYCs.Service.get(id = updateTraderKYCDocumentZoneStatusData.traderID, documentType = updateTraderKYCDocumentZoneStatusData.documentType)
          (for{
            userZoneID<-userZoneID
            traderZoneID<-traderZoneID
            _<-verifyOrReject(userZoneID,traderZoneID)
            traderKYC<-traderKYC
          }yield withUsernameToken.PartialContent(views.html.component.master.updateTraderKYCDocumentZoneStatus(traderKYC = traderKYC))
            ).recover{
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def zoneRejectVerifyTraderRequestForm(traderID: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.zoneRejectVerifyTraderRequest(views.companion.master.RejectVerifyTraderRequest.form.fill(views.companion.master.RejectVerifyTraderRequest.Data(traderID = traderID))))
  }

  def zoneRejectVerifyTraderRequest: Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.RejectVerifyTraderRequest.form.bindFromRequest().fold(
        formWithErrors => {
          Future{BadRequest(views.html.component.master.zoneRejectVerifyTraderRequest(formWithErrors))}
        },
        rejectVerifyTraderRequestData => {

          val rejectTrader=masterTraders.Service.rejectTrader(rejectVerifyTraderRequestData.traderID)
          val zoneAccountID=masterZones.Service.getAccountId(rejectVerifyTraderRequestData.traderID)
          def zoneRejectAll(zoneAccountID:String)=masterTraderKYCs.Service.zoneRejectAll(zoneAccountID)
          (for{
            _<-rejectTrader
            zoneAccountID<-zoneAccountID
            _<-zoneRejectAll(zoneAccountID)
          }yield withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.VERIFY_TRADER_REQUEST_REJECTED)))
            ).recover{
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

 /* def zoneViewPendingVerifyTraderRequests: Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      /*try {
        withUsernameToken.Ok(views.html.component.master.zoneViewPendingVerifyTraderRequests(masterTraders.Service.getVerifyTraderRequestsForZone(masterZones.Service.getZoneId(loginState.username))))
      }
      catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }*/
      val zoneID=masterZones.Service.getZoneId(loginState.username)
      def verifyTraderRequestsForZone(zoneID:String)=masterTraders.Service.getVerifyTraderRequestsForZone(zoneID)
      for{
        zoneID<-zoneID
        verifyTraderRequestsForZone<-verifyTraderRequestsForZone(zoneID)
      }yield withUsernameToken.Ok(views.html.component.master.zoneViewPendingVerifyTraderRequests(verifyTraderRequestsForZone))
  }*/

  def organizationVerifyTraderForm(accountID: String, organizationID: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.organizationVerifyTrader(views.companion.master.VerifyTrader.form.fill(views.companion.master.VerifyTrader.Data(accountID = accountID, organizationID = organizationID, gas = constants.FormField.GAS.minimumValue, password = ""))))
  }

  def organizationVerifyTrader: Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.VerifyTrader.form.bindFromRequest().fold(
        formWithErrors => {
          Future{BadRequest(views.html.component.master.organizationVerifyTrader(formWithErrors))}
        },
        verifyTraderData => {

          val id=masterTraders.Service.getID(verifyTraderData.accountID)
          def checkAllKYCFilesVerified(id:String)= masterTraderKYCs.Service.checkAllKYCFilesVerified(id)
          def getResult(checkAllKYCFilesVerified:Boolean)={
            if(checkAllKYCFilesVerified){
              val zoneID = masterOrganizations.Service.getZoneID(verifyTraderData.organizationID)
              val aclAddress = masterAccounts.Service.getAddress(verifyTraderData.accountID)
              val acl = blockchain.ACL(issueAsset = verifyTraderData.issueAsset, issueFiat = verifyTraderData.issueFiat, sendAsset = verifyTraderData.sendAsset, sendFiat = verifyTraderData.sendFiat, redeemAsset = verifyTraderData.redeemAsset, redeemFiat = verifyTraderData.redeemFiat, sellerExecuteOrder = verifyTraderData.sellerExecuteOrder, buyerExecuteOrder = verifyTraderData.buyerExecuteOrder, changeBuyerBid = verifyTraderData.changeBuyerBid, changeSellerBid = verifyTraderData.changeSellerBid, confirmBuyerBid = verifyTraderData.confirmBuyerBid, confirmSellerBid = verifyTraderData.changeSellerBid, negotiation = verifyTraderData.negotiation, releaseAsset = verifyTraderData.releaseAsset)
              val createACL=blockchainAclHashes.Service.create(acl)
              def transactionProcess(aclAddress:String,zoneID:String)=transaction.process[blockchainTransaction.SetACL, transactionsSetACL.Request](
                entity = blockchainTransaction.SetACL(from = loginState.address, aclAddress = aclAddress, organizationID = verifyTraderData.organizationID, zoneID = zoneID, aclHash = util.hashing.MurmurHash3.stringHash(acl.toString).toString, gas = verifyTraderData.gas, ticketID = "", mode = transactionMode),
                blockchainTransactionCreate = blockchainTransactionSetACLs.Service.create,
                request = transactionsSetACL.Request(transactionsSetACL.BaseReq(from = loginState.address, gas = verifyTraderData.gas.toString), password = verifyTraderData.password, aclAddress = aclAddress, organizationID = verifyTraderData.organizationID, zoneID = zoneID, issueAsset = verifyTraderData.issueAsset.toString, issueFiat = verifyTraderData.issueFiat.toString, sendAsset = verifyTraderData.sendAsset.toString, sendFiat = verifyTraderData.sendFiat.toString, redeemAsset = verifyTraderData.redeemAsset.toString, redeemFiat = verifyTraderData.redeemFiat.toString, sellerExecuteOrder = verifyTraderData.sellerExecuteOrder.toString, buyerExecuteOrder = verifyTraderData.buyerExecuteOrder.toString, changeBuyerBid = verifyTraderData.changeBuyerBid.toString, changeSellerBid = verifyTraderData.changeSellerBid.toString, confirmBuyerBid = verifyTraderData.confirmBuyerBid.toString, confirmSellerBid = verifyTraderData.confirmSellerBid.toString, negotiation = verifyTraderData.negotiation.toString, releaseAsset = verifyTraderData.releaseAsset.toString, mode = transactionMode),
                action = transactionsSetACL.Service.post,
                onSuccess = blockchainTransactionSetACLs.Utility.onSuccess,
                onFailure = blockchainTransactionSetACLs.Utility.onFailure,
                updateTransactionHash = blockchainTransactionSetACLs.Service.updateTransactionHash
              )
              for{
                aclAddress<-aclAddress
                zoneID<-zoneID
                _<-createACL
                _<-transactionProcess(aclAddress,zoneID)
              }yield withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.ACL_SET)))
            }else {
              Future{PreconditionFailed(views.html.index(failures = Seq(constants.Response.ALL_KYC_FILES_NOT_VERIFIED)))}
            }
          }
          (for{
            id<-id
            checkAllKYCFilesVerified<-checkAllKYCFilesVerified(id)
            result<-getResult(checkAllKYCFilesVerified)
          }yield result
            ).recover{
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def organizationModifyTraderForm(accountID: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.organizationModifyTrader(views.companion.master.ModifyTrader.form.fill(views.companion.master.ModifyTrader.Data(accountID = accountID, gas = constants.FormField.GAS.minimumValue, password = ""))))
  }

  def organizationModifyTrader: Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.ModifyTrader.form.bindFromRequest().fold(
        formWithErrors => {
          Future{BadRequest(views.html.component.master.organizationModifyTrader(formWithErrors))}
        },
        verifyTraderData => {

          val id=masterTraders.Service.getID(verifyTraderData.accountID)
          def checkAllKYCFilesVerified(id:String)= masterTraderKYCs.Service.checkAllKYCFilesVerified(id)
          def getResult(checkAllKYCFilesVerified:Boolean)={
            if(checkAllKYCFilesVerified){
              val zoneID = masterOrganizations.Service.getZoneIDByAccountID(loginState.username)
              val organizationID = masterOrganizations.Service.getID(loginState.username)
              val aclAddress = masterAccounts.Service.getAddress(verifyTraderData.accountID)
              val acl = blockchain.ACL(issueAsset = verifyTraderData.issueAsset, issueFiat = verifyTraderData.issueFiat, sendAsset = verifyTraderData.sendAsset, sendFiat = verifyTraderData.sendFiat, redeemAsset = verifyTraderData.redeemAsset, redeemFiat = verifyTraderData.redeemFiat, sellerExecuteOrder = verifyTraderData.sellerExecuteOrder, buyerExecuteOrder = verifyTraderData.buyerExecuteOrder, changeBuyerBid = verifyTraderData.changeBuyerBid, changeSellerBid = verifyTraderData.changeSellerBid, confirmBuyerBid = verifyTraderData.confirmBuyerBid, confirmSellerBid = verifyTraderData.changeSellerBid, negotiation = verifyTraderData.negotiation, releaseAsset = verifyTraderData.releaseAsset)
              val createACL=blockchainAclHashes.Service.create(acl)
              def transactionProcess(aclAddress:String,zoneID:String,organizationID:String)= transaction.process[blockchainTransaction.SetACL, transactionsSetACL.Request](
                entity = blockchainTransaction.SetACL(from = loginState.address, aclAddress = aclAddress, organizationID = organizationID, zoneID = zoneID, aclHash = util.hashing.MurmurHash3.stringHash(acl.toString).toString, gas = verifyTraderData.gas, ticketID = "", mode = transactionMode),
                blockchainTransactionCreate = blockchainTransactionSetACLs.Service.create,
                request = transactionsSetACL.Request(transactionsSetACL.BaseReq(from = loginState.address, gas = verifyTraderData.gas.toString), password = verifyTraderData.password, aclAddress = aclAddress, organizationID = organizationID, zoneID = zoneID, issueAsset = verifyTraderData.issueAsset.toString, issueFiat = verifyTraderData.issueFiat.toString, sendAsset = verifyTraderData.sendAsset.toString, sendFiat = verifyTraderData.sendFiat.toString, redeemAsset = verifyTraderData.redeemAsset.toString, redeemFiat = verifyTraderData.redeemFiat.toString, sellerExecuteOrder = verifyTraderData.sellerExecuteOrder.toString, buyerExecuteOrder = verifyTraderData.buyerExecuteOrder.toString, changeBuyerBid = verifyTraderData.changeBuyerBid.toString, changeSellerBid = verifyTraderData.changeSellerBid.toString, confirmBuyerBid = verifyTraderData.confirmBuyerBid.toString, confirmSellerBid = verifyTraderData.confirmSellerBid.toString, negotiation = verifyTraderData.negotiation.toString, releaseAsset = verifyTraderData.releaseAsset.toString, mode = transactionMode),
                action = transactionsSetACL.Service.post,
                onSuccess = blockchainTransactionSetACLs.Utility.onSuccess,
                onFailure = blockchainTransactionSetACLs.Utility.onFailure,
                updateTransactionHash = blockchainTransactionSetACLs.Service.updateTransactionHash
              )
              for{
                aclAddress<-aclAddress
                zoneID<-zoneID
                organizationID<-organizationID
                _<-createACL
                _<-transactionProcess(aclAddress,zoneID,organizationID)
              }yield withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.ACL_SET)))
            }else {
              Future{PreconditionFailed(views.html.index(failures = Seq(constants.Response.ALL_KYC_FILES_NOT_VERIFIED)))}
            }
          }
          (for{
            id<-id
            checkAllKYCFilesVerified<-checkAllKYCFilesVerified(id)
            result<-getResult(checkAllKYCFilesVerified)
          }yield result
            ).recover{
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def organizationViewKYCDocuments(traderID: String): Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>

      val traderKYCs=masterTraderKYCs.Service.getAllDocuments(traderID)
      (for{
        traderKYCs<-traderKYCs
      }yield withUsernameToken.Ok(views.html.component.master.organizationViewVerificationTraderKYCDouments(traderKYCs))
        ).recover{
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def updateTraderKYCDocumentOrganizationStatusForm(traderID: String, documentType: String): Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>

      val userOrganizationID=masterOrganizations.Service.getID(loginState.username)
      val traderOrganizationID=masterTraders.Service.getOrganizationID(traderID)
      def getResult(userOrganizationID:String,traderOrganizationID:String)={
        if(userOrganizationID == traderOrganizationID){
          val traderKYC=masterTraderKYCs.Service.get(id = traderID, documentType = documentType)
          for{
            traderKYC <-traderKYC
          }yield withUsernameToken.Ok(views.html.component.master.updateTraderKYCDocumentOrganizationStatus(traderKYC = traderKYC))

        }else Future{Unauthorized(views.html.index(failures = Seq(constants.Response.UNAUTHORIZED)))}

      }
      (for{
        userOrganizationID<-userOrganizationID
        traderOrganizationID<-traderOrganizationID
        result<-getResult(userOrganizationID,traderOrganizationID)
      } yield result
        ).recover{
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def updateTraderKYCDocumentOrganizationStatus(): Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.UpdateTraderKYCDocumentOrganizationStatus.form.bindFromRequest().fold(
        formWithErrors => {
          val traderKYC=masterTraderKYCs.Service.get(id = formWithErrors(constants.FormField.TRADER_ID.name).value.get, documentType = formWithErrors(constants.FormField.DOCUMENT_TYPE.name).value.get)
          (for{
            traderKYC <-traderKYC
          }yield BadRequest(views.html.component.master.updateTraderKYCDocumentOrganizationStatus(formWithErrors, traderKYC))
            ).recover{
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        },
        updateTraderKYCDocumentOrganizationStatusData => {

          val userOrganizationID=masterOrganizations.Service.getID(loginState.username)
          val traderOrganizationID= masterTraders.Service.getOrganizationID(updateTraderKYCDocumentOrganizationStatusData.traderID)
          def getResult(userOrganizationID:String,traderOrganizationID:String)={
            val traderID=masterTraders.Service.getAccountId(updateTraderKYCDocumentOrganizationStatusData.traderID)

            if(userOrganizationID == traderOrganizationID){
              val verifyOrReject=if (updateTraderKYCDocumentOrganizationStatusData.organizationStatus) {
                val organizationVerify=masterTraderKYCs.Service.organizationVerify(id = updateTraderKYCDocumentOrganizationStatusData.traderID, documentType = updateTraderKYCDocumentOrganizationStatusData.documentType)
                for{
                  _<-organizationVerify
                  traderID<-traderID
                }yield utilitiesNotification.send(traderID, constants.Notification.SUCCESS, Messages(constants.Response.DOCUMENT_APPROVED.message))
              } else {
                val organizationReject=masterTraderKYCs.Service.organizationReject(id = updateTraderKYCDocumentOrganizationStatusData.traderID, documentType = updateTraderKYCDocumentOrganizationStatusData.documentType)
                for{
                  _<-organizationReject
                  traderID<-traderID
                }yield utilitiesNotification.send(traderID, constants.Notification.FAILURE, Messages(constants.Response.DOCUMENT_REJECTED.message))
              }
              def traderKYC=masterTraderKYCs.Service.get(id = updateTraderKYCDocumentOrganizationStatusData.traderID, documentType = updateTraderKYCDocumentOrganizationStatusData.documentType)
              for{
                _<-verifyOrReject
                traderKYC<-traderKYC
              } yield withUsernameToken.PartialContent(views.html.component.master.updateTraderKYCDocumentOrganizationStatus(traderKYC = traderKYC))

            }else {
              Future{Unauthorized(views.html.index(failures = Seq(constants.Response.UNAUTHORIZED)))}
            }
          }
           (for{
            userOrganizationID<-userOrganizationID
            traderOrganizationID<-traderOrganizationID
            result<-getResult(userOrganizationID,traderOrganizationID)
          }yield result
             ).recover{
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def organizationRejectVerifyTraderRequestForm(traderID: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.organizationRejectVerifyTraderRequest(views.companion.master.RejectVerifyTraderRequest.form.fill(views.companion.master.RejectVerifyTraderRequest.Data(traderID = traderID))))
  }

  def organizationRejectVerifyTraderRequest: Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.RejectVerifyTraderRequest.form.bindFromRequest().fold(
        formWithErrors => {
          Future{BadRequest(views.html.component.master.organizationRejectVerifyTraderRequest(formWithErrors))}
        },
        rejectVerifyTraderRequestData => {

          val rejectTrader=masterTraders.Service.rejectTrader(rejectVerifyTraderRequestData.traderID)
          val organizationAccountID=masterOrganizations.Service.getAccountId(rejectVerifyTraderRequestData.traderID)
          def organizationRejectAll(organizationAccountID:String)= masterTraderKYCs.Service.organizationRejectAll(organizationAccountID)
          (for{
            _<-rejectTrader
            organizationAccountID<-organizationAccountID
            _<- organizationRejectAll(organizationAccountID)
          }yield withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.VERIFY_TRADER_REQUEST_REJECTED)))
            ).recover{
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def organizationViewPendingVerifyTraderRequests: Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>

      val organizationAccount=masterOrganizations.Service.getByAccountID(loginState.username)
      def verifyTraderRequestsForOrganization(organizationAccount:Organization)=masterTraders.Service.getVerifyTraderRequestsForOrganization(organizationAccount.id)
      (for{
        organizationAccount<-organizationAccount
        verifyTraderRequestsForOrganization<-verifyTraderRequestsForOrganization(organizationAccount)
      }yield withUsernameToken.Ok(views.html.component.master.organizationViewPendingVerifyTraderRequests(verifyTraderRequestsForOrganization))
        ).recover{
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def uploadTraderKYCForm(documentType: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.uploadFile(utilities.String.getJsRouteFunction(routes.javascript.SetACLController.uploadTraderKYC), utilities.String.getJsRouteFunction(routes.javascript.SetACLController.storeTraderKYC), documentType))
  }

  def uploadTraderKYC(documentType: String) = Action(parse.multipartFormData) { implicit request =>
    FileUpload.form.bindFromRequest.fold(
      formWithErrors => {
        BadRequest
      },
      fileUploadInfo => {
        try {
          request.body.file(constants.File.KEY_FILE) match {
            case None => BadRequest(views.html.index(failures = Seq(constants.Response.NO_FILE)))
            case Some(file) => utilities.FileOperations.savePartialFile(Files.readAllBytes(file.ref.path), fileUploadInfo, fileResourceManager.getTraderKYCFilePath(documentType))
              Ok
          }
        }
        catch {
          case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
        }
      }
    )
  }

  def storeTraderKYC(name: String, documentType: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      /*try {
        fileResourceManager.storeFile[master.TraderKYC](
          name = name,
          documentType = documentType,
          path = fileResourceManager.getTraderKYCFilePath(documentType),
          document = master.TraderKYC(id = masterTraders.Service.getID(loginState.username), documentType = documentType, fileName = name, file = None, zoneStatus = None, organizationStatus = None),
          masterCreate = masterTraderKYCs.Service.create
        )
        withUsernameToken.Ok(Messages(constants.Response.FILE_UPLOAD_SUCCESSFUL.message))
      } catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }*/
        val traderID=masterTraders.Service.getID(loginState.username)
        def storeFile(traderID:String)=fileResourceManager.storeFile[master.TraderKYC](
          name = name,
          documentType = documentType,
          path = fileResourceManager.getTraderKYCFilePath(documentType),
          document = master.TraderKYC(id = traderID, documentType = documentType, fileName = name, file = None, zoneStatus = None, organizationStatus = None),
          masterCreate = masterTraderKYCs.Service.create
        )
      (for{
      traderID<-traderID
      _<-storeFile(traderID)
    }yield withUsernameToken.Ok(Messages(constants.Response.FILE_UPLOAD_SUCCESSFUL.message))
        ).recover{
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def updateTraderKYCForm(documentType: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.updateFile(utilities.String.getJsRouteFunction(routes.javascript.SetACLController.uploadTraderKYC), utilities.String.getJsRouteFunction(routes.javascript.SetACLController.updateTraderKYC), documentType))
  }

  def updateTraderKYC(name: String, documentType: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      /*try {
        val id = masterTraders.Service.getID(loginState.username)
        fileResourceManager.updateFile[master.TraderKYC](
          name = name,
          documentType = documentType,
          path = fileResourceManager.getTraderKYCFilePath(documentType),
          oldDocumentFileName = masterTraderKYCs.Service.getFileName(id = id, documentType = documentType),
          document = master.TraderKYC(id = id, documentType = documentType, fileName = name, file = None, zoneStatus = None, organizationStatus = None),
          updateOldDocument = masterTraderKYCs.Service.updateOldDocument
        )
        withUsernameToken.Ok(Messages(constants.Response.FILE_UPDATE_SUCCESSFUL.message))
      } catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }*/
      val id = masterTraders.Service.getID(loginState.username)
      def oldDocumentFileName(id:String)= masterTraderKYCs.Service.getFileName(id = id, documentType = documentType)
      def updateFile(id:String,oldDocumentFileName:String)=fileResourceManager.updateFile[master.TraderKYC](
        name = name,
        documentType = documentType,
        path = fileResourceManager.getTraderKYCFilePath(documentType),
        oldDocumentFileName =oldDocumentFileName,
        document = master.TraderKYC(id = id, documentType = documentType, fileName = name, file = None, zoneStatus = None, organizationStatus = None),
        updateOldDocument = masterTraderKYCs.Service.updateOldDocument
      )
      (for{
      id<-id
      oldDocumentFileName<-oldDocumentFileName(id)
      _<-updateFile(id,oldDocumentFileName)
    }yield withUsernameToken.Ok(Messages(constants.Response.FILE_UPDATE_SUCCESSFUL.message))
        ).recover{
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def viewTradersInOrganization: Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>

      val organizationAccount=masterOrganizations.Service.getByAccountID(loginState.username)
      def verifiedTradersForOrganization(organizationAccount:Organization)=masterTraders.Service.getVerifiedTradersForOrganization(organizationAccount.id)
      (for{
        organizationAccount<-organizationAccount
        verifiedTradersForOrganization<-verifiedTradersForOrganization(organizationAccount)
      }yield withUsernameToken.Ok(views.html.component.master.viewTradersInOrganizationForOrganization(verifiedTradersForOrganization))
        ).recover{
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def viewTradersInOrganizationForZone(organizationID: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>

      val organizationZoneID=masterOrganizations.Service.getZoneID(organizationID)
      val zoneID=masterZones.Service.getID(loginState.username)
      def getResult(organizationZoneID:String,zoneID:String)={
        if (organizationZoneID == zoneID) {
          //withUsernameToken.Ok(views.html.component.master.viewTradersInOrganization(masterTraders.Service.getVerifiedTradersForOrganization(organizationID)))
          val verifiedTradersForOrganization=masterTraders.Service.getVerifiedTradersForOrganization(organizationID)
          for{
            verifiedTradersForOrganization<-verifiedTradersForOrganization
          }yield withUsernameToken.Ok(views.html.component.master.viewTradersInOrganization(verifiedTradersForOrganization))
        } else {
          Future{Unauthorized(views.html.index(failures = Seq(constants.Response.UNAUTHORIZED)))}
        }
      }
      (for{
      organizationZoneID<-organizationZoneID
      zoneID<-zoneID
      result<-getResult(organizationZoneID,zoneID)
    }yield result).recover{
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def viewTradersInOrganizationForGenesis(organizationID: String): Action[AnyContent] = withGenesisLoginAction.authenticated { implicit loginState =>
    implicit request =>

     val verifiedTradersForOrganization=masterTraders.Service.getVerifiedTradersForOrganization(organizationID)
      for{
        verifiedTradersForOrganization<-verifiedTradersForOrganization
      }yield  withUsernameToken.Ok(views.html.component.master.viewTradersInOrganization(verifiedTradersForOrganization))
  }

  def blockchainSetACLForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.blockchain.setACL())
  }

  def blockchainSetACL: Action[AnyContent] = Action.async { implicit request =>
    views.companion.blockchain.SetACL.form.bindFromRequest().fold(
      formWithErrors => {
        Future{BadRequest(views.html.component.blockchain.setACL(formWithErrors))}
      },
      setACLData => {
        val post=transactionsSetACL.Service.post(transactionsSetACL.Request(transactionsSetACL.BaseReq(from = setACLData.from, gas = setACLData.gas.toString), password = setACLData.password, aclAddress = setACLData.aclAddress, organizationID = setACLData.organizationID, zoneID = setACLData.zoneID, issueAsset = setACLData.issueAsset.toString, issueFiat = setACLData.issueFiat.toString, sendAsset = setACLData.sendAsset.toString, sendFiat = setACLData.sendFiat.toString, redeemAsset = setACLData.redeemAsset.toString, redeemFiat = setACLData.redeemFiat.toString, sellerExecuteOrder = setACLData.sellerExecuteOrder.toString, buyerExecuteOrder = setACLData.buyerExecuteOrder.toString, changeBuyerBid = setACLData.changeBuyerBid.toString, changeSellerBid = setACLData.changeSellerBid.toString, confirmBuyerBid = setACLData.confirmBuyerBid.toString, confirmSellerBid = setACLData.confirmSellerBid.toString, negotiation = setACLData.negotiation.toString, releaseAsset = setACLData.releaseAsset.toString, mode = transactionMode))
        (for{
          _<-post
        }yield Ok(views.html.index(successes = Seq(constants.Response.ACL_SET)))
          ).recover{
          case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
        }
      }
    )
  }
}