package controllers

import java.nio.file.Files
import java.util.Base64

import blockchainTx.messages.Messages.SendCoin
import controllers.actions._
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.common.Serializable._
import models.master._
import models.masterTransaction.ZoneInvitation
import models.{blockchain, blockchainTransaction, master, masterTransaction}
import org.bitcoinj.core.ECKey
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{Action, _}
import play.api.{Configuration, Logger}
import transactions.request.Serializable.{Message, SignMeta, Tx}
import utilities.{KeyStore, MicroNumber}
import views.companion.master.FileUpload
import blockchainTx.common.Coin
import queries.blockchain.GetAccount

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AddZoneController @Inject()(
                                   messagesControllerComponents: MessagesControllerComponents,
                                   fileResourceManager: utilities.FileResourceManager,
                                   withZoneLoginAction: WithZoneLoginAction,
                                   transaction: utilities.Transaction,
                                   utilitiesNotification: utilities.Notification,
                                   masterZoneKYCs: master.ZoneKYCs,
                                   blockchainAccounts: blockchain.Accounts,
                                   transactionsIdentityIssue: transactions.blockchain.IdentityIssue,
                                   transactionsBroadcast: transactions.blockchain.Broadcast,
                                   blockchainTransactionIdentityIssues: blockchainTransaction.IdentityIssues,
                                   blockchainTransactionSendCoins: blockchainTransaction.SendCoins,
                                   masterZones: master.Zones,
                                   masterEmails: master.Emails,
                                   masterIdentities: master.Identities,
                                   masterProperties: master.Properties,
                                   masterClassifications: master.Classifications,
                                   masterMobiles: master.Mobiles,
                                   masterAccounts: master.Accounts,
                                   getAccount: GetAccount,
                                   withUserLoginAction: WithUserLoginAction,
                                   transactionsMaintainerDeputize: transactions.blockchain.MaintainerDeputize,
                                   blockchainTransactionMaintainerDeputizes: blockchainTransaction.MaintainerDeputizes,
                                   withGenesisLoginAction: WithGenesisLoginAction,
                                   withUsernameToken: WithUsernameToken,
                                   masterTransactionZoneInvitations: masterTransaction.ZoneInvitations,
                                   withoutLoginAction: WithoutLoginAction,
                                   withoutLoginActionAsync: WithoutLoginActionAsync,
                                   withLoginActionAsync: WithLoginActionAsync,
                                   keyStore: KeyStore
                                 )(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_ADD_ZONE

  private val comdexURL: String = configuration.get[String]("webApp.url")

  private val stakingDenom = configuration.get[String]("blockchain.stakingDenom")

  private val chainID = configuration.get[String]("blockchain.chainID")

  def inviteZoneForm(): Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
      Ok(views.html.component.master.inviteZone())
  }

  def inviteZone(): Action[AnyContent] = withGenesisLoginAction { implicit loginState =>
    implicit request =>
      views.companion.master.InviteZone.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.inviteZone(formWithErrors)))
        },
        inviteZoneData => {

          val token = masterTransactionZoneInvitations.Service.create(inviteZoneData.emailAddress)

          def sendEmailNotificationsAndGetResult(token: String): Future[Result] = {
            utilitiesNotification.sendEmailToEmailAddress(fromAccountID = loginState.username, emailAddress = inviteZoneData.emailAddress, email = constants.Notification.ZONE_INVITATION, comdexURL, token)
            utilitiesNotification.send(accountID = loginState.username, notification = constants.Notification.ZONE_INVITATION_SENT)()
            withUsernameToken.Ok(views.html.account(successes = Seq(constants.Response.ZONE_INVITATION_EMAIL_SENT)))
          }

          (for {
            token <- token
            result <- sendEmailNotificationsAndGetResult(token)
          } yield result).recover {
            case baseException: BaseException => InternalServerError(views.html.account(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def acceptInvitation(token: String): Action[AnyContent] = withUserLoginAction { implicit loginState =>
    implicit request =>
      val invitation = masterTransactionZoneInvitations.Service.tryGet(token)

      def updateStatus(invitation: ZoneInvitation): Future[Int] = {
        if (invitation.accountID.isDefined) {
          if (invitation.accountID.get != loginState.username) throw new BaseException(constants.Response.UNAUTHORIZED) else Future(0)
        } else {
          masterTransactionZoneInvitations.Service.markInvitationAccepted(id = token, accountID = loginState.username)
        }
      }

      (for {
        invitation <- invitation
        _ <- updateStatus(invitation)
        result <- withUsernameToken.Ok(views.html.component.master.userAcceptZoneInvitation())
      } yield result
        ).recoverWith {
        case baseException: BaseException => withUsernameToken.Ok(views.html.account(failures = Seq(baseException.failure)))
      }
  }

  def addZoneForm(): Action[AnyContent] = withUserLoginAction { implicit loginState =>
    implicit request =>
      val zone = masterZones.Service.getByAccountID(loginState.username)
      (for {
        zone <- zone
      } yield Ok(views.html.component.master.addZone(views.companion.master.AddZone.form.fill(views.companion.master.AddZone.Data(name = zone.name, currency = zone.currency, address = views.companion.master.AddZone.AddressData(addressLine1 = zone.address.addressLine1, addressLine2 = zone.address.addressLine2, landmark = zone.address.landmark, city = zone.address.city, country = zone.address.country, zipCode = zone.address.zipCode, phone = zone.address.phone)))))
        ).recoverWith {
        case _: BaseException => Future(Ok(views.html.component.master.addZone()))
      }
  }

  def addZone(): Action[AnyContent] = withUserLoginAction { implicit loginState =>
    implicit request =>
      views.companion.master.AddZone.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.addZone(formWithErrors)))
        },
        addZoneData => {
          val immutables = Seq(constants.Property.NAME.withValue(addZoneData.name))
          val immutableMetas = Seq(constants.Property.USER_TYPE.withValue(constants.User.ZONE))
          val invitationStatus = masterTransactionZoneInvitations.Service.getStatus(loginState.username)
          val email = masterEmails.Service.tryGet(loginState.username)
          val mobile = masterMobiles.Service.tryGet(loginState.username)

          def insertOrUpdate(invitationStatus: Option[Boolean], email: Email, mobile: Mobile) = if (invitationStatus.contains(true)) {
            if (!email.status || !mobile.status) throw new BaseException(constants.Response.CONTACT_VERIFICATION_PENDING)
            else {
              val identityID = utilities.IDGenerator.getIdentityID(constants.Blockchain.Classification.ZONE, Immutables(Properties(immutables.map(_.toProperty) ++ immutableMetas.map(_.toProperty))))
              val upsert = masterZones.Service.insertOrUpdate(id = identityID, accountID = loginState.username, name = addZoneData.name, currency = addZoneData.currency, address = Address(addressLine1 = addZoneData.address.addressLine1, addressLine2 = addZoneData.address.addressLine2, landmark = addZoneData.address.landmark, city = addZoneData.address.city, country = addZoneData.address.country, zipCode = addZoneData.address.zipCode, phone = addZoneData.address.phone))
              for (
                _ <- upsert
              ) yield identityID
            }
          } else throw new BaseException(constants.Response.UNAUTHORIZED)

          def zoneKYCs(id: String): Future[Seq[ZoneKYC]] = masterZoneKYCs.Service.getAllDocuments(id)

          (for {
            invitationStatus <- invitationStatus
            email <- email
            mobile <- mobile
            id <- insertOrUpdate(invitationStatus, email, mobile)
            zoneKYCs <- zoneKYCs(id)
            result <- withUsernameToken.PartialContent(views.html.component.master.userUploadOrUpdateZoneKYC(zoneKYCs))
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.account(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def userUploadOrUpdateZoneKYCView(): Action[AnyContent] = withUserLoginAction { implicit loginState =>
    implicit request =>
      val zoneID = masterZones.Service.tryGetID(loginState.username)

      def zoneKYCs(zoneID: String): Future[Seq[ZoneKYC]] = masterZoneKYCs.Service.getAllDocuments(zoneID)

      (for {
        zoneID <- zoneID
        zoneKYCs <- zoneKYCs(zoneID)
      } yield Ok(views.html.component.master.userUploadOrUpdateZoneKYC(zoneKYCs))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.account(failures = Seq(baseException.failure)))
      }
  }

  def userUploadZoneKYCForm(documentType: String): Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
      Ok(views.html.component.master.uploadFile(utilities.String.getJsRouteFunction(routes.javascript.AddZoneController.userUploadZoneKYC), utilities.String.getJsRouteFunction(routes.javascript.AddZoneController.userStoreZoneKYC), documentType))
  }

  def userUploadZoneKYC(documentType: String) = Action(parse.multipartFormData) { implicit request =>
    FileUpload.form.bindFromRequest.fold(
      formWithErrors => {
        BadRequest
      },
      fileUploadInfo => {
        try {
          request.body.file(constants.File.KEY_FILE) match {
            case None => BadRequest(views.html.account(failures = Seq(constants.Response.NO_FILE)))
            case Some(file) =>
              utilities.FileOperations.savePartialFile(Files.readAllBytes(file.ref.path), fileUploadInfo, fileResourceManager.getZoneKYCFilePath(documentType))
              Ok
          }
        }
        catch {
          case baseException: BaseException => InternalServerError(views.html.account(failures = Seq(baseException.failure)))
        }
      }
    )
  }

  def userStoreZoneKYC(name: String, documentType: String): Action[AnyContent] = withUserLoginAction { implicit loginState =>
    implicit request =>
      val id = masterZones.Service.tryGetID(loginState.username)

      def storeFile(id: String): Future[Unit] = fileResourceManager.storeFile[ZoneKYC](
        name = name,
        path = fileResourceManager.getZoneKYCFilePath(documentType),
        document = ZoneKYC(id = id, documentType = documentType, status = None, fileName = name, file = None),
        masterCreate = masterZoneKYCs.Service.create
      )

      def zoneKYCs(id: String): Future[Seq[ZoneKYC]] = masterZoneKYCs.Service.getAllDocuments(id)

      (for {
        id <- id
        _ <- storeFile(id)
        zoneKYCs <- zoneKYCs(id)
        result <- withUsernameToken.PartialContent(views.html.component.master.userUploadOrUpdateZoneKYC(zoneKYCs))
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.account(failures = Seq(baseException.failure)))
      }
  }

  def userUpdateZoneKYCForm(documentType: String): Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
      Ok(views.html.component.master.updateFile(utilities.String.getJsRouteFunction(routes.javascript.AddZoneController.userUploadZoneKYC), utilities.String.getJsRouteFunction(routes.javascript.AddZoneController.userUpdateZoneKYC), documentType))
  }

  def userUpdateZoneKYC(name: String, documentType: String): Action[AnyContent] = withUserLoginAction { implicit loginState =>
    implicit request =>
      val id = masterZones.Service.tryGetID(loginState.username)

      def getOldDocument(id: String): Future[ZoneKYC] = masterZoneKYCs.Service.tryGet(id = id, documentType = documentType)

      def updateFile(oldDocument: ZoneKYC): Future[Boolean] = fileResourceManager.updateFile[ZoneKYC](
        name = name,
        path = fileResourceManager.getZoneKYCFilePath(documentType),
        oldDocument = oldDocument,
        updateOldDocument = masterZoneKYCs.Service.updateOldDocument
      )

      def zoneKYCs(id: String): Future[Seq[ZoneKYC]] = masterZoneKYCs.Service.getAllDocuments(id)

      (for {
        id <- id
        oldDocument <- getOldDocument(id)
        _ <- updateFile(oldDocument)
        zoneKYCs <- zoneKYCs(id)
        result <- withUsernameToken.PartialContent(views.html.component.master.userUploadOrUpdateZoneKYC(zoneKYCs))
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.account(failures = Seq(baseException.failure)))
      }
  }

  def userReviewAddZoneRequestForm(): Action[AnyContent] = withUserLoginAction { implicit loginState =>
    implicit request =>
      val zone = masterZones.Service.getByAccountID(loginState.username)

      def zoneKYCs(id: String): Future[Seq[ZoneKYC]] = masterZoneKYCs.Service.getAllDocuments(id)

      (for {
        zone <- zone
        zoneKYCs <- zoneKYCs(zone.id)
        result <- withUsernameToken.Ok(views.html.component.master.userReviewAddZoneRequest(zone = zone, zoneKYCs = zoneKYCs))
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.account(failures = Seq(baseException.failure)))
      }
  }

  def userReviewAddZoneRequest(): Action[AnyContent] = withUserLoginAction { implicit loginState =>
    implicit request =>
      views.companion.master.ReviewAddZoneRequest.form.bindFromRequest().fold(
        formWithErrors => {
          val zone = masterZones.Service.getByAccountID(loginState.username)

          def zoneKYCs(id: String): Future[Seq[ZoneKYC]] = masterZoneKYCs.Service.getAllDocuments(id)

          (for {
            zone <- zone
            zoneKYCs <- zoneKYCs(zone.id)
          } yield BadRequest(views.html.component.master.userReviewAddZoneRequest(formWithErrors, zone = zone, zoneKYCs = zoneKYCs))
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.account(failures = Seq(baseException.failure)))
          }
        },
        userReviewAddZoneRequestData => {
          val validateUser = masterAccounts.Service.validateUsernamePassword(username = loginState.username, password = userReviewAddZoneRequestData.password)
          val zone = masterZones.Service.getByAccountID(loginState.username)

          def zoneKYCs(id: String): Future[Seq[ZoneKYC]] = masterZoneKYCs.Service.getAllDocuments(id)

          def completeAddZoneRequest(validateUser: Boolean, zone: Zone, zoneKYCs: Seq[ZoneKYC]): Future[Result] = if (validateUser) {
            def allKYCFileTypesExists(id: String): Future[Boolean] = masterZoneKYCs.Service.checkAllKYCFileTypesExists(id)

            def markZoneFormCompletedAndGetResult(id: String, allKYCFileTypesExists: Boolean): Future[Result] =
              if (allKYCFileTypesExists) {
                try {
                  keyStore.setPassphrase(alias = id, aliasValue = userReviewAddZoneRequestData.password)
                } catch {
                  case baseException: BaseException => throw baseException
                }

                def markZoneFormCompleted = masterZones.Service.markZoneFormCompleted(id)

                for {
                  _ <- markZoneFormCompleted
                  result <- withUsernameToken.Ok(views.html.account(successes = Seq(constants.Response.ZONE_ADDED_FOR_VERIFICATION)))
                } yield result
              } else throw new BaseException(constants.Response.ALL_KYC_FILES_NOT_FOUND)

            for {
              allKYCFileTypesExists <- allKYCFileTypesExists(zone.id)
              result <- markZoneFormCompletedAndGetResult(zone.id, allKYCFileTypesExists)
              _ <- utilitiesNotification.send(loginState.username, constants.Notification.ADD_ZONE_REQUESTED, loginState.username)()
            } yield result
          } else Future(BadRequest(views.html.component.master.userReviewAddZoneRequest(views.companion.master.ReviewAddZoneRequest.form.fill(userReviewAddZoneRequestData).withGlobalError(constants.Response.INCORRECT_PASSWORD.message), zone = zone, zoneKYCs = zoneKYCs)))

          (for {
            validateUser <- validateUser
            zone <- zone
            zoneKYCs <- zoneKYCs(zone.id)
            result <- completeAddZoneRequest(validateUser, zone, zoneKYCs)
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.account(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def verifyZoneForm(zoneID: String): Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
      Ok(views.html.component.master.verifyZone(zoneID = zoneID))
  }

  def verifyZone: Action[AnyContent] = withGenesisLoginAction { implicit loginState =>
    implicit request =>
      views.companion.master.VerifyZone.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.verifyZone(formWithErrors, zoneID = formWithErrors.data(constants.FormField.ZONE_ID.name))))
        },
        verifyZoneData => {
          val allKYCFilesVerified = masterZoneKYCs.Service.checkAllKYCFilesVerified(verifyZoneData.zoneID)

          def sendTransactionsAndGetResult(allKYCFilesVerified: Boolean): Future[Result] = {
            if (allKYCFilesVerified) {
              val zone = masterZones.Service.tryGet(verifyZoneData.zoneID)
              val mainAccount = masterAccounts.Service.tryGet(loginState.username)
              val mainBlockchainAccount = getAccount.Service.get(loginState.address)

              def zoneAccount(accountID: String): Future[models.blockchain.Account] = blockchainAccounts.Service.tryGetByUsername(accountID)

              def sendTransactions(mainAccount: Account, mainBlockchainAccount: queries.responses.blockchain.AccountResponse.Response, zoneAccount: models.blockchain.Account, zone: Zone) = {
                val immutables = Seq(constants.Property.NAME.withValue(zone.name))
                val immutableMetas = Seq(constants.Property.USER_TYPE.withValue(constants.User.ZONE))
                val mutableMetas = Seq(constants.Property.CURRENCY.withValue(zone.currency))
                val mutables = Seq(constants.Property.ADDRESS.withValue(zone.address.toString.filter(_.isLetter)))

                val sendCoin = transaction.process[blockchainTransaction.SendCoin, transactionsBroadcast.Request](
                  entity = blockchainTransaction.SendCoin(from = loginState.address, to = zoneAccount.address, amount = Seq(models.common.Serializable.Coin(stakingDenom, constants.Blockchain.DefaultZoneFaucetAmount)), gas = verifyZoneData.gas, ticketID = "", mode = transactionMode),
                  blockchainTransactionCreate = blockchainTransactionSendCoins.Service.create,
                  request = transactionsBroadcast.Request(utilities.SignTx.sign(Tx(Seq(Message(constants.Blockchain.TransactionMessage.SEND_COIN, SendCoin(from_address = loginState.address, to_address = zoneAccount.address, Seq(Coin(stakingDenom, constants.Blockchain.DefaultZoneFaucetAmount))))), Fee(Seq(), verifyZoneData.gas.toMicroString)), SignMeta(mainBlockchainAccount.result.value.accountNumber, chainID, mainBlockchainAccount.result.value.sequence), ECKey.fromPrivate(utilities.Crypto.decrypt(Base64.getDecoder.decode(mainAccount.privateKeyEncrypted.getOrElse(throw new BaseException(constants.Response.FAILURE))), verifyZoneData.password))), transactionMode),
                  action = transactionsBroadcast.Service.post,
                  onSuccess = blockchainTransactionSendCoins.Utility.onSuccess,
                  onFailure = blockchainTransactionSendCoins.Utility.onFailure,
                  updateTransactionHash = blockchainTransactionSendCoins.Service.updateTransactionHash)


                def issueIdentity = transaction.process[blockchainTransaction.IdentityIssue, transactionsIdentityIssue.Request](
                  entity = blockchainTransaction.IdentityIssue(from = loginState.address, fromID = constants.Blockchain.mainIdentityID, classificationID = constants.Blockchain.Classification.ZONE, to = zoneAccount.address, immutableMetaProperties = immutableMetas, immutableProperties = immutables, mutableMetaProperties = mutableMetas, mutableProperties = mutables, gas = verifyZoneData.gas, ticketID = "", mode = transactionMode),
                  blockchainTransactionCreate = blockchainTransactionIdentityIssues.Service.create,
                  request = transactionsIdentityIssue.Request(transactionsIdentityIssue.Message(transactionsIdentityIssue.BaseReq(from = loginState.address, gas = verifyZoneData.gas), fromID = constants.Blockchain.mainIdentityID, classificationID = constants.Blockchain.Classification.ZONE, to = zoneAccount.address, immutableMetaProperties = immutableMetas, immutableProperties = immutables, mutableMetaProperties = mutableMetas, mutableProperties = mutables)),
                  action = transactionsIdentityIssue.Service.post,
                  onSuccess = blockchainTransactionIdentityIssues.Utility.onSuccess,
                  onFailure = blockchainTransactionIdentityIssues.Utility.onFailure,
                  updateTransactionHash = blockchainTransactionIdentityIssues.Service.updateTransactionHash)

                for {
                  _ <- sendCoin
                  _ <- issueIdentity
                } yield ()
              }

              for {
                zone <- zone
                mainAccount <- mainAccount
                mainBlockchainAccount <- mainBlockchainAccount
                zoneAccount <- zoneAccount(zone.accountID)
                _ <- sendTransactions(mainAccount, mainBlockchainAccount, zoneAccount, zone)
                result <- withUsernameToken.Ok(views.html.account(successes = Seq(constants.Response.FIAT_SENT)))
                _ <- utilitiesNotification.send(zone.accountID, constants.Notification.ADD_ZONE_CONFIRMED, zone.accountID)()
              } yield {
                result
              }
            } else Future(PreconditionFailed(views.html.account(failures = Seq(constants.Response.ALL_KYC_FILES_NOT_VERIFIED))))
          }

          (for {
            allKYCFilesVerified <- allKYCFilesVerified
            result <- sendTransactionsAndGetResult(allKYCFilesVerified)
            _ <- utilitiesNotification.send(loginState.username, constants.Notification.ADD_ZONE_CONFIRMED, loginState.username)()
          } yield {
            result
          }).recover {
            case baseException: BaseException => InternalServerError(views.html.account(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def deputizeForm(zoneID: String): Action[AnyContent] = withGenesisLoginAction { implicit loginState =>
    implicit request =>
      Future(Ok(views.html.component.master.deputizeZone(zoneID = zoneID)))
  }

  def deputize: Action[AnyContent] = withGenesisLoginAction { implicit loginState =>
    implicit request =>
      views.companion.master.DeputizeZone.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.deputizeZone(formWithErrors, zoneID = formWithErrors.data(constants.FormField.ZONE_ID.name))))
        },
        deputizeZoneData => {
          val zone = masterZones.Service.tryGet(deputizeZoneData.zoneID)
          val zoneClassifications = masterClassifications.Service.getByIdentityIDs(Seq(deputizeZoneData.zoneID))

          def deputizeAndGetResult(zone: Zone, zoneClassifications: Seq[Classification]) = {
            if (!zone.deputizeStatus) {
              def deputizeOrganizationClassification = {
                //TODO Need to implement better solution than manual delays for deputize
                if (deputizeZoneData.addOrganization && !zoneClassifications.map(_.id).contains(constants.Blockchain.Classification.ORGANIZATION)) {
                  val classificationProperties = masterProperties.Service.getAll(constants.Blockchain.Classification.ORGANIZATION, constants.Blockchain.Entity.IDENTITY_DEFINITION)

                  def broadcastTx(classificationProperties: Seq[models.master.Property]) = transaction.process[blockchainTransaction.MaintainerDeputize, transactionsMaintainerDeputize.Request](
                    entity = blockchainTransaction.MaintainerDeputize(from = loginState.address, fromID = constants.Blockchain.mainIdentityID, toID = deputizeZoneData.zoneID, classificationID = constants.Blockchain.Classification.ORGANIZATION, maintainedTraits = classificationProperties.filter(_.isMutable).map(x => BaseProperty(x.dataType, x.name, None)), addMaintainer = true, mutateMaintainer = true, removeMaintainer = true, gas = deputizeZoneData.gas, ticketID = "", mode = transactionMode),
                    blockchainTransactionCreate = blockchainTransactionMaintainerDeputizes.Service.create,
                    request = transactionsMaintainerDeputize.Request(transactionsMaintainerDeputize.Message(transactionsMaintainerDeputize.BaseReq(from = loginState.address, gas = deputizeZoneData.gas), fromID = constants.Blockchain.mainIdentityID, toID = deputizeZoneData.zoneID, classificationID = constants.Blockchain.Classification.ORGANIZATION, maintainedTraits = classificationProperties.filter(_.isMutable).map(x => BaseProperty(x.dataType, x.name, None)), addMaintainer = true, mutateMaintainer = true, removeMaintainer = true)),
                    action = transactionsMaintainerDeputize.Service.post,
                    onSuccess = blockchainTransactionMaintainerDeputizes.Utility.onSuccess,
                    onFailure = blockchainTransactionMaintainerDeputizes.Utility.onFailure,
                    updateTransactionHash = blockchainTransactionMaintainerDeputizes.Service.updateTransactionHash
                  )

                  for {
                    classificationProperties <- classificationProperties
                    _ <- broadcastTx(classificationProperties)
                  } yield Thread.sleep(3000)

                } else {
                  Future()
                }
              }

              def deputizeTraderClassification = {
                if (deputizeZoneData.addTrader && !zoneClassifications.map(_.id).contains(constants.Blockchain.Classification.TRADER)) {
                  val classificationProperties = masterProperties.Service.getAll(constants.Blockchain.Classification.TRADER, constants.Blockchain.Entity.IDENTITY_DEFINITION)

                  def broadcastTx(classificationProperties: Seq[models.master.Property]) = transaction.process[blockchainTransaction.MaintainerDeputize, transactionsMaintainerDeputize.Request](
                    entity = blockchainTransaction.MaintainerDeputize(from = loginState.address, fromID = constants.Blockchain.mainIdentityID, toID = deputizeZoneData.zoneID, classificationID = constants.Blockchain.Classification.TRADER, maintainedTraits = classificationProperties.filter(_.isMutable).map(x => BaseProperty(x.dataType, x.name, x.value)), addMaintainer = true, mutateMaintainer = true, removeMaintainer = true, gas = deputizeZoneData.gas, ticketID = "", mode = transactionMode),
                    blockchainTransactionCreate = blockchainTransactionMaintainerDeputizes.Service.create,
                    request = transactionsMaintainerDeputize.Request(transactionsMaintainerDeputize.Message(transactionsMaintainerDeputize.BaseReq(from = loginState.address, gas = deputizeZoneData.gas), fromID = constants.Blockchain.mainIdentityID, toID = deputizeZoneData.zoneID, classificationID = constants.Blockchain.Classification.TRADER, maintainedTraits = classificationProperties.filter(_.isMutable).map(x => BaseProperty(x.dataType, x.name, x.value)), addMaintainer = true, mutateMaintainer = true, removeMaintainer = true)),
                    action = transactionsMaintainerDeputize.Service.post,
                    onSuccess = blockchainTransactionMaintainerDeputizes.Utility.onSuccess,
                    onFailure = blockchainTransactionMaintainerDeputizes.Utility.onFailure,
                    updateTransactionHash = blockchainTransactionMaintainerDeputizes.Service.updateTransactionHash
                  )

                  for {
                    classificationProperties <- classificationProperties
                    _ <- broadcastTx(classificationProperties)
                  } yield Thread.sleep(3000)
                } else {
                  Future()
                }
              }

              def deputizeModeratedAssetClassification = {
                if (deputizeZoneData.createModeratedAsset && !zoneClassifications.map(_.id).contains(constants.Blockchain.Classification.MODERATED_ASSET)) {
                  val classificationProperties = masterProperties.Service.getAll(constants.Blockchain.Classification.MODERATED_ASSET, constants.Blockchain.Entity.ASSET_DEFINITION)

                  def broadcastTx(classificationProperties: Seq[models.master.Property]) = transaction.process[blockchainTransaction.MaintainerDeputize, transactionsMaintainerDeputize.Request](
                    entity = blockchainTransaction.MaintainerDeputize(from = loginState.address, fromID = constants.Blockchain.mainIdentityID, toID = deputizeZoneData.zoneID, classificationID = constants.Blockchain.Classification.MODERATED_ASSET, maintainedTraits = classificationProperties.filter(_.isMutable).map(x => BaseProperty(x.dataType, x.name, None)), addMaintainer = true, mutateMaintainer = true, removeMaintainer = true, gas = deputizeZoneData.gas, ticketID = "", mode = transactionMode),
                    blockchainTransactionCreate = blockchainTransactionMaintainerDeputizes.Service.create,
                    request = transactionsMaintainerDeputize.Request(transactionsMaintainerDeputize.Message(transactionsMaintainerDeputize.BaseReq(from = loginState.address, gas = deputizeZoneData.gas), fromID = constants.Blockchain.mainIdentityID, toID = deputizeZoneData.zoneID, classificationID = constants.Blockchain.Classification.MODERATED_ASSET, maintainedTraits = classificationProperties.filter(_.isMutable).map(x => BaseProperty(x.dataType, x.name, None)), addMaintainer = true, mutateMaintainer = true, removeMaintainer = true)),
                    action = transactionsMaintainerDeputize.Service.post,
                    onSuccess = blockchainTransactionMaintainerDeputizes.Utility.onSuccess,
                    onFailure = blockchainTransactionMaintainerDeputizes.Utility.onFailure,
                    updateTransactionHash = blockchainTransactionMaintainerDeputizes.Service.updateTransactionHash
                  )

                  for {
                    classificationProperties <- classificationProperties
                    _ <- broadcastTx(classificationProperties)
                  } yield Thread.sleep(3000)

                } else {
                  Future()
                }
              }

              def deputizeUnmoderatedAssetClassification = {
                if (deputizeZoneData.createUnmoderatedAsset && !zoneClassifications.map(_.id).contains(constants.Blockchain.Classification.UNMODERATED_ASSET)) {
                  val classificationProperties = masterProperties.Service.getAll(constants.Blockchain.Classification.UNMODERATED_ASSET, constants.Blockchain.Entity.ASSET_DEFINITION)

                  def broadcastTx(classificationProperties: Seq[models.master.Property]) = transaction.process[blockchainTransaction.MaintainerDeputize, transactionsMaintainerDeputize.Request](
                    entity = blockchainTransaction.MaintainerDeputize(from = loginState.address, fromID = constants.Blockchain.mainIdentityID, toID = deputizeZoneData.zoneID, classificationID = constants.Blockchain.Classification.UNMODERATED_ASSET, maintainedTraits = classificationProperties.filter(_.isMutable).map(x => BaseProperty(x.dataType, x.name, x.value)), addMaintainer = true, mutateMaintainer = true, removeMaintainer = true, gas = deputizeZoneData.gas, ticketID = "", mode = transactionMode),
                    blockchainTransactionCreate = blockchainTransactionMaintainerDeputizes.Service.create,
                    request = transactionsMaintainerDeputize.Request(transactionsMaintainerDeputize.Message(transactionsMaintainerDeputize.BaseReq(from = loginState.address, gas = deputizeZoneData.gas), fromID = constants.Blockchain.mainIdentityID, toID = deputizeZoneData.zoneID, classificationID = constants.Blockchain.Classification.UNMODERATED_ASSET, maintainedTraits = classificationProperties.filter(_.isMutable).map(x => BaseProperty(x.dataType, x.name, x.value)), addMaintainer = true, mutateMaintainer = true, removeMaintainer = true)),
                    action = transactionsMaintainerDeputize.Service.post,
                    onSuccess = blockchainTransactionMaintainerDeputizes.Utility.onSuccess,
                    onFailure = blockchainTransactionMaintainerDeputizes.Utility.onFailure,
                    updateTransactionHash = blockchainTransactionMaintainerDeputizes.Service.updateTransactionHash
                  )

                  for {
                    classificationProperties <- classificationProperties
                    _ <- broadcastTx(classificationProperties)
                  } yield Thread.sleep(3000)

                } else {
                  Future()
                }
              }

              def deputizeFiatClassification = {
                if (deputizeZoneData.createFiat && !zoneClassifications.map(_.id).contains(constants.Blockchain.Classification.FIAT)) {
                  val classificationProperties = masterProperties.Service.getAll(constants.Blockchain.Classification.FIAT, constants.Blockchain.Entity.ASSET_DEFINITION)

                  def broadcastTx(classificationProperties: Seq[models.master.Property]) = transaction.process[blockchainTransaction.MaintainerDeputize, transactionsMaintainerDeputize.Request](
                    entity = blockchainTransaction.MaintainerDeputize(from = loginState.address, fromID = constants.Blockchain.mainIdentityID, toID = deputizeZoneData.zoneID, classificationID = constants.Blockchain.Classification.FIAT, maintainedTraits = classificationProperties.filter(_.isMutable).map(x => BaseProperty(x.dataType, x.name, x.value)), addMaintainer = true, mutateMaintainer = true, removeMaintainer = true, gas = deputizeZoneData.gas, ticketID = "", mode = transactionMode),
                    blockchainTransactionCreate = blockchainTransactionMaintainerDeputizes.Service.create,
                    request = transactionsMaintainerDeputize.Request(transactionsMaintainerDeputize.Message(transactionsMaintainerDeputize.BaseReq(from = loginState.address, gas = deputizeZoneData.gas), fromID = constants.Blockchain.mainIdentityID, toID = deputizeZoneData.zoneID, classificationID = constants.Blockchain.Classification.FIAT, maintainedTraits = classificationProperties.filter(_.isMutable).map(x => BaseProperty(x.dataType, x.name, x.value)), addMaintainer = true, mutateMaintainer = true, removeMaintainer = true)),
                    action = transactionsMaintainerDeputize.Service.post,
                    onSuccess = blockchainTransactionMaintainerDeputizes.Utility.onSuccess,
                    onFailure = blockchainTransactionMaintainerDeputizes.Utility.onFailure,
                    updateTransactionHash = blockchainTransactionMaintainerDeputizes.Service.updateTransactionHash
                  )

                  for {
                    classificationProperties <- classificationProperties
                    _ <- broadcastTx(classificationProperties)
                  } yield Thread.sleep(3000)
                } else {
                  Future()
                }
              }


              def deputizeOrderClassification = {
                if (deputizeZoneData.createOrder && !zoneClassifications.map(_.id).contains(constants.Blockchain.Classification.ORDER)) {

                  val classificationProperties = masterProperties.Service.getAll(constants.Blockchain.Classification.ORDER, constants.Blockchain.Entity.ORDER_DEFINITION)

                  def broadcastTx(classificationProperties: Seq[models.master.Property]) = transaction.process[blockchainTransaction.MaintainerDeputize, transactionsMaintainerDeputize.Request](
                    entity = blockchainTransaction.MaintainerDeputize(from = loginState.address, fromID = constants.Blockchain.mainIdentityID, toID = deputizeZoneData.zoneID, classificationID = constants.Blockchain.Classification.ORDER, maintainedTraits = classificationProperties.filter(_.isMutable).map(x => BaseProperty(x.dataType, x.name, x.value)), addMaintainer = true, mutateMaintainer = true, removeMaintainer = true, gas = deputizeZoneData.gas, ticketID = "", mode = transactionMode),
                    blockchainTransactionCreate = blockchainTransactionMaintainerDeputizes.Service.create,
                    request = transactionsMaintainerDeputize.Request(transactionsMaintainerDeputize.Message(transactionsMaintainerDeputize.BaseReq(from = loginState.address, gas = deputizeZoneData.gas), fromID = constants.Blockchain.mainIdentityID, toID = deputizeZoneData.zoneID, classificationID = constants.Blockchain.Classification.ORDER, maintainedTraits = classificationProperties.filter(_.isMutable).map(x => BaseProperty(x.dataType, x.name, x.value)), addMaintainer = true, mutateMaintainer = true, removeMaintainer = true)),
                    action = transactionsMaintainerDeputize.Service.post,
                    onSuccess = blockchainTransactionMaintainerDeputizes.Utility.onSuccess,
                    onFailure = blockchainTransactionMaintainerDeputizes.Utility.onFailure,
                    updateTransactionHash = blockchainTransactionMaintainerDeputizes.Service.updateTransactionHash
                  )

                  for {
                    classificationProperties <- classificationProperties
                    _ <- broadcastTx(classificationProperties)
                  } yield Thread.sleep(1000)
                } else {
                  Future()
                }
              }

              for {
                _ <- deputizeOrganizationClassification
                _ <- deputizeTraderClassification
                _ <- deputizeModeratedAssetClassification
                _ <- deputizeUnmoderatedAssetClassification
                _ <- deputizeFiatClassification
                _ <- deputizeOrderClassification
              } yield ()
            } else {
              throw new BaseException(constants.Response.FAILURE)
            }
          }

          (for {
            zone <- zone
            zoneClassifications <- zoneClassifications
            _ <- deputizeAndGetResult(zone, zoneClassifications)
            result <- withUsernameToken.Ok(views.html.account(successes = Seq(constants.Response.ZONE_DEPUTIZED)))
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.account(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def viewPendingVerifyZoneRequests: Action[AnyContent] = withGenesisLoginAction { implicit loginState =>
    implicit request =>
      val verifyZoneRequests = masterZones.Service.getVerifyZoneRequests
      (for {
        verifyZoneRequests <- verifyZoneRequests
      } yield Ok(views.html.component.master.viewPendingVerifyZoneRequests(verifyZoneRequests))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.account(failures = Seq(baseException.failure)))
      }
  }

  def viewKYCDocuments(accountID: String): Action[AnyContent] = withGenesisLoginAction { implicit loginState =>
    implicit request =>
      val zoneKYCs = masterZoneKYCs.Service.getAllDocuments(accountID)
      (for {
        zoneKYCs <- zoneKYCs
      } yield Ok(views.html.component.master.viewVerificationZoneKYCDouments(zoneKYCs))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.account(failures = Seq(baseException.failure)))
      }
  }

  def updateZoneKYCDocumentStatusForm(zoneID: String, documentType: String): Action[AnyContent] = withGenesisLoginAction { implicit loginState =>
    implicit request =>
      val zoneKYC = masterZoneKYCs.Service.tryGet(id = zoneID, documentType = documentType)
      (for {
        zoneKYC <- zoneKYC
      } yield Ok(views.html.component.master.updateZoneKYCDocumentStatus(zoneKYC = zoneKYC))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.account(failures = Seq(baseException.failure)))
      }
  }

  def updateZoneKYCDocumentStatus(): Action[AnyContent] = withGenesisLoginAction { implicit loginState =>
    implicit request =>
      views.companion.master.UpdateZoneKYCDocumentStatus.form.bindFromRequest().fold(
        formWithErrors => {
          val zoneKYC = masterZoneKYCs.Service.tryGet(id = formWithErrors.data(constants.FormField.ZONE_ID.name), documentType = formWithErrors.data(constants.FormField.DOCUMENT_TYPE.name))
          (for {
            zoneKYC <- zoneKYC
          } yield BadRequest(views.html.component.master.updateZoneKYCDocumentStatus(formWithErrors, zoneKYC))
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.account(failures = Seq(baseException.failure)))
          }
        },
        updateZoneKYCDocumentStatusData => {
          val verifyOrRejectAndSendNotification = if (updateZoneKYCDocumentStatusData.status) {
            val verify = masterZoneKYCs.Service.verify(id = updateZoneKYCDocumentStatusData.zoneID, documentType = updateZoneKYCDocumentStatusData.documentType)
            val zoneAccountID = masterZones.Service.tryGetAccountID(updateZoneKYCDocumentStatusData.zoneID)
            for {
              _ <- verify
              zoneAccountID <- zoneAccountID
              _ <- utilitiesNotification.send(zoneAccountID, constants.Notification.SUCCESS, Messages(constants.Response.DOCUMENT_APPROVED.message))()
            } yield {}
          } else {
            val reject = masterZoneKYCs.Service.reject(id = updateZoneKYCDocumentStatusData.zoneID, documentType = updateZoneKYCDocumentStatusData.documentType)
            val zoneAccountID = masterZones.Service.tryGetAccountID(updateZoneKYCDocumentStatusData.zoneID)
            for {
              _ <- reject
              zoneAccountID <- zoneAccountID
              _ <- utilitiesNotification.send(zoneAccountID, constants.Notification.FAILURE, Messages(constants.Response.DOCUMENT_REJECTED.message))()
            } yield {}
          }

          def zoneKYC: Future[ZoneKYC] = masterZoneKYCs.Service.tryGet(id = updateZoneKYCDocumentStatusData.zoneID, documentType = updateZoneKYCDocumentStatusData.documentType)

          (for {
            _ <- verifyOrRejectAndSendNotification
            zoneKYC <- zoneKYC
            result <- withUsernameToken.PartialContent(views.html.component.master.updateZoneKYCDocumentStatus(zoneKYC = zoneKYC))
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.account(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def rejectVerifyZoneRequestForm(zoneID: String): Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
      Ok(views.html.component.master.rejectVerifyZoneRequest(zoneID = zoneID))
  }

  def rejectVerifyZoneRequest: Action[AnyContent] = withGenesisLoginAction { implicit loginState =>
    implicit request =>
      views.companion.master.RejectVerifyZoneRequest.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.rejectVerifyZoneRequest(formWithErrors, zoneID = formWithErrors.data(constants.FormField.ZONE_ID.name))))
        },
        rejectVerifyZoneRequestData => {
          val rejectZone = masterZones.Service.rejectZone(rejectVerifyZoneRequestData.zoneID)
          val accountID = masterZones.Service.tryGetAccountID(rejectVerifyZoneRequestData.zoneID)

          (for {
            _ <- rejectZone
            accountID <- accountID
            result <- withUsernameToken.Ok(views.html.account(successes = Seq(constants.Response.VERIFY_ZONE_REJECTED)))
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.account(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def uploadZoneKYCForm(documentType: String): Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
      Ok(views.html.component.master.uploadFile(utilities.String.getJsRouteFunction(routes.javascript.AddZoneController.uploadZoneKYC), utilities.String.getJsRouteFunction(routes.javascript.AddZoneController.storeZoneKYC), documentType))
  }

  def uploadZoneKYC(documentType: String) = Action(parse.multipartFormData) { implicit request =>
    FileUpload.form.bindFromRequest.fold(
      formWithErrors => {
        BadRequest
      },
      fileUploadInfo => {
        try {
          request.body.file(constants.File.KEY_FILE) match {
            case None => BadRequest(views.html.account(failures = Seq(constants.Response.NO_FILE)))
            case Some(file) => utilities.FileOperations.savePartialFile(Files.readAllBytes(file.ref.path), fileUploadInfo, fileResourceManager.getZoneKYCFilePath(documentType))
              Ok
          }
        }
        catch {
          case baseException: BaseException => InternalServerError(views.html.account(failures = Seq(baseException.failure)))
        }
      }
    )
  }

  def storeZoneKYC(name: String, documentType: String): Action[AnyContent] = withZoneLoginAction { implicit loginState =>
    implicit request =>
      val id = masterZones.Service.tryGetID(loginState.username)

      def storeFile(id: String): Future[Unit] = fileResourceManager.storeFile[ZoneKYC](
        name = name,
        path = fileResourceManager.getZoneKYCFilePath(documentType),
        document = ZoneKYC(id = id, documentType = documentType, status = None, fileName = name, file = None),
        masterCreate = masterZoneKYCs.Service.create
      )

      (for {
        id <- id
        _ <- storeFile(id)
        result <- withUsernameToken.Ok(Messages(constants.Response.FILE_UPLOAD_SUCCESSFUL.message))
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.account(failures = Seq(baseException.failure)))
      }
  }

  def updateZoneKYCForm(documentType: String): Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
      Ok(views.html.component.master.updateFile(utilities.String.getJsRouteFunction(routes.javascript.AddZoneController.uploadZoneKYC), utilities.String.getJsRouteFunction(routes.javascript.AddZoneController.updateZoneKYC), documentType))
  }

  def updateZoneKYC(name: String, documentType: String): Action[AnyContent] = withZoneLoginAction { implicit loginState =>
    implicit request =>
      val id = masterZones.Service.tryGetID(loginState.username)

      def getOldDocument(id: String): Future[ZoneKYC] = masterZoneKYCs.Service.tryGet(id = id, documentType = documentType)

      def updateFile(oldDocument: ZoneKYC): Future[Boolean] = fileResourceManager.updateFile[ZoneKYC](
        name = name,
        path = fileResourceManager.getZoneKYCFilePath(documentType),
        oldDocument = oldDocument,
        updateOldDocument = masterZoneKYCs.Service.updateOldDocument
      )

      (for {
        id <- id
        oldDocument <- getOldDocument(id)
        _ <- updateFile(oldDocument)
        result <- withUsernameToken.Ok(Messages(constants.Response.FILE_UPDATE_SUCCESSFUL.message))
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.account(failures = Seq(baseException.failure)))
      }
  }

}
