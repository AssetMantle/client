package controllers

import java.util.Base64

import blockchainTx.common.Coin
import blockchainTx.messages.Messages.SendCoin
import controllers.actions._
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.common.Serializable.{BaseProperty, Fee, Immutables, Properties}
import models.master._
import models.{blockchain, blockchainTransaction, master, masterTransaction}
import org.bitcoinj.core.ECKey
import play.api.i18n.I18nSupport
import play.api.mvc._
import play.api.{Configuration, Logger}
import queries.blockchain.GetAccount
import transactions.blockchain.SetACL
import transactions.request.Serializable.{Message, SignMeta, Tx}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SetACLController @Inject()(
                                  blockchainAccounts: blockchain.Accounts,
                                  messagesControllerComponents: MessagesControllerComponents,
                                  transaction: utilities.Transaction,
                                  masterTransactionTraderInvitations: masterTransaction.TraderInvitations,
                                  masterEmails: master.Emails,
                                  masterZones: master.Zones,
                                  masterOrganizations: master.Organizations,
                                  masterClassifications: master.Classifications,
                                  masterIdentifications: master.Identifications,
                                  masterTraders: master.Traders,
                                  masterMobiles: master.Mobiles,
                                  masterProperties: master.Properties,
                                  withZoneLoginAction: WithZoneLoginAction,
                                  withOrganizationLoginAction: WithOrganizationLoginAction,
                                  withUserLoginAction: WithUserLoginAction,
                                  withGenesisLoginAction: WithGenesisLoginAction,
                                  withoutLoginAction: WithoutLoginAction,
                                  withoutLoginActionAsync: WithoutLoginActionAsync,
                                  getAccount: GetAccount,
                                  masterAccounts: master.Accounts,
                                  transactionsSetACL: SetACL,
                                  transactionsSendCoin: transactions.blockchain.SendCoin,
                                  transactionsIdentityIssue: transactions.blockchain.IdentityIssue,
                                  transactionsMaintainerDeputize: transactions.blockchain.MaintainerDeputize,
                                  blockchainTransactionMaintainerDeputizes: blockchainTransaction.MaintainerDeputizes,
                                  blockchainTransactionIdentityIssues: blockchainTransaction.IdentityIssues,
                                  transactionsBroadcast: transactions.blockchain.Broadcast,
                                  blockchainTransactionSendCoins: blockchainTransaction.SendCoins,
                                  utilitiesNotification: utilities.Notification,
                                  withUsernameToken: WithUsernameToken)(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private val stakingDenom = configuration.get[String]("blockchain.stakingDenom")

  private val chainID = configuration.get[String]("blockchain.chainID")

  private implicit val module: String = constants.Module.CONTROLLERS_SET_ACL

  private val comdexURL: String = configuration.get[String]("webApp.url")

  def inviteTraderForm(): Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
      Ok(views.html.component.master.inviteTrader())
  }

  def inviteTrader(): Action[AnyContent] = withOrganizationLoginAction { implicit loginState =>
    implicit request =>
      views.companion.master.InviteTrader.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.inviteTrader(formWithErrors)))
        },
        inviteTraderData => {
          val emailAddressAccount: Future[Option[String]] = masterEmails.Service.getEmailAddressAccount(inviteTraderData.emailAddress)

          def inviteeUserType(emailAddressAccount: Option[String]): Future[String] = emailAddressAccount match {
            case Some(emailAddressAccount) => masterAccounts.Service.getUserType(emailAddressAccount)
            case None => Future(constants.User.USER)
          }

          def createSendInvitationAndGetResult(inviteeUserType: String): Future[Result] = {
            if (inviteeUserType != constants.User.USER) {
              Future(BadRequest(views.html.component.master.inviteTrader(views.companion.master.InviteTrader.form.fill(inviteTraderData).withGlobalError(constants.Response.EMAIL_ADDRESS_TAKEN.message))))
            } else {
              val organization = masterOrganizations.Service.tryGetByAccountID(loginState.username)
              val identification = masterIdentifications.Service.tryGet(loginState.username)

              def createInvitation(organization: Organization): Future[String] = masterTransactionTraderInvitations.Service.create(organizationID = organization.id, inviteeEmailAddress = inviteTraderData.emailAddress)

              def sendEmailAndGetResult(organization: Organization, identification: Identification): Future[Result] = {
                utilitiesNotification.sendEmailToEmailAddress(fromAccountID = loginState.username, emailAddress = inviteTraderData.emailAddress, email = constants.Notification.TRADER_INVITATION, inviteTraderData.name, Seq(identification.firstName, identification.lastName).mkString(" "), organization.name, comdexURL, organization.id)
                withUsernameToken.Ok(views.html.account(successes = Seq(constants.Response.TRADER_INVITATION_EMAIL_SENT)))
              }

              for {
                organization <- organization
                identification <- identification
                _ <- createInvitation(organization)
                _ <- utilitiesNotification.send(accountID = organization.accountID, notification = constants.Notification.ORGANIZATION_TRADER_INVITATION, inviteTraderData.emailAddress)()
                result <- sendEmailAndGetResult(organization, identification)
              } yield result
            }

          }

          (for {
            emailAddressAccount <- emailAddressAccount
            inviteeUserType <- inviteeUserType(emailAddressAccount)
            result <- createSendInvitationAndGetResult(inviteeUserType)
          } yield result).recover {
            case baseException: BaseException => InternalServerError(views.html.account(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def addTraderForm(): Action[AnyContent] = withUserLoginAction { implicit loginState =>
    implicit request =>
      val trader = masterTraders.Service.getByAccountID(loginState.username)

      def getResult(trader: Option[Trader]) = if (trader.isDefined) {
        Ok(views.html.component.master.addTrader(views.companion.master.AddTrader.form.fill(views.companion.master.AddTrader.Data(organizationID = trader.get.organizationID))))
      } else {
        Ok(views.html.component.master.addTrader())
      }

      (for {
        trader <- trader
      } yield getResult(trader)
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.profile(failures = Seq(baseException.failure)))
      }
  }

  def addTrader(): Action[AnyContent] = withUserLoginAction { implicit loginState =>
    implicit request =>
      views.companion.master.AddTrader.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.addTrader(formWithErrors)))
        },
        addTraderData => {

          val status = masterOrganizations.Service.getVerificationStatus(addTraderData.organizationID)
          val email = masterEmails.Service.tryGet(loginState.username)
          val mobile = masterMobiles.Service.tryGet(loginState.username)

          def insertOrUpdateAndGetResult(status: Boolean): Future[Result] = {
            if (status) {
              val organization = masterOrganizations.Service.tryGet(addTraderData.organizationID)

              def addTrader(zoneID: String, email: Email, mobile: Mobile) =
                if (!email.status || !mobile.status) throw new BaseException(constants.Response.CONTACT_VERIFICATION_PENDING)
                else {
                  val immutables = Seq(constants.Property.ACCOUNT_ID.withValue(loginState.username))
                  val immutableMetas = Seq(constants.Property.USER_TYPE.withValue(constants.User.TRADER))

                  def insertOrUpdate = masterTraders.Service.insertOrUpdate(utilities.IDGenerator.getIdentityID(constants.Blockchain.Classification.TRADER, Immutables(Properties((immutableMetas ++ immutables).map(_.toProperty)))), zoneID, addTraderData.organizationID, loginState.username)

                  for {
                    _ <- insertOrUpdate
                  } yield ()
                }

              val emailAddress: Future[Option[String]] = masterEmails.Service.getVerifiedEmailAddress(loginState.username)

              def updateInvitationStatus(emailAddress: Option[String]): Future[Int] = if (emailAddress.isDefined) {
                masterTransactionTraderInvitations.Service.updateStatusByEmailAddress(organizationID = addTraderData.organizationID, emailAddress = emailAddress.get, status = constants.Status.TraderInvitation.TRADER_ADDED_FOR_VERIFICATION)
              } else {
                Future(0)
              }

              for {
                organization <- organization
                email <- email
                mobile <- mobile
                _ <- addTrader(zoneID = organization.zoneID, email = email, mobile = mobile)
                emailAddress <- emailAddress
                _ <- updateInvitationStatus(emailAddress)
                result <- withUsernameToken.Ok(views.html.profile(successes = Seq(constants.Response.TRADER_ADDED_FOR_VERIFICATION)))
              } yield result
            } else {
              Future(Unauthorized(views.html.profile(failures = Seq(constants.Response.UNVERIFIED_ORGANIZATION))))
            }
          }

          (for {
            status <- status
            result <- insertOrUpdateAndGetResult(status)
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.profile(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def zoneVerifyTraderForm(traderID: String): Action[AnyContent] = withZoneLoginAction { implicit loginState =>
    implicit request =>
      val trader = masterTraders.Service.tryGet(traderID)
      val zoneID = masterZones.Service.tryGetID(loginState.username)
      (for {
        trader <- trader
        zoneID <- zoneID
      } yield if (trader.zoneID == zoneID) {
        Ok(views.html.component.master.zoneVerifyTrader(views.companion.master.VerifyTrader.form, trader))
      } else {
        Unauthorized(views.html.account(failures = Seq(constants.Response.UNAUTHORIZED)))
      }
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.account(failures = Seq(baseException.failure)))
      }
  }

  def zoneVerifyTrader: Action[AnyContent] = withZoneLoginAction { implicit loginState =>
    implicit request =>
      views.companion.master.VerifyTrader.form.bindFromRequest().fold(
        formWithErrors => {
          val trader = masterTraders.Service.tryGetByAccountID(formWithErrors.data(constants.FormField.ACCOUNT_ID.name))
          (for {
            trader <- trader
          } yield if (trader.organizationID == formWithErrors.data(constants.FormField.ORGANIZATION_ID.name)) {
            BadRequest(views.html.component.master.zoneVerifyTrader(formWithErrors, trader))
          } else {
            Unauthorized(views.html.account(failures = Seq(constants.Response.UNAUTHORIZED)))
          }
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.account(failures = Seq(baseException.failure)))
          }
        },
        verifyTraderData => {
          val organizationVerificationStatus = masterOrganizations.Service.tryGetVerificationStatus(verifyTraderData.organizationID)
          val zoneID = masterZones.Service.tryGetID(loginState.username)
          val trader = masterTraders.Service.tryGetByAccountID(verifyTraderData.accountID)
          val validateUsernamePassword = masterAccounts.Service.validateUsernamePassword(username = loginState.username, password = verifyTraderData.password)

          def sendTransactionsAndGetResult(validateUsernamePassword: Boolean, trader: Trader, organizationVerificationStatus: Boolean, zoneID: String): Future[Result] = {
            if (trader.zoneID != zoneID) throw new BaseException(constants.Response.UNAUTHORIZED)
            else if (trader.organizationID != verifyTraderData.organizationID) throw new BaseException(constants.Response.ORGANIZATION_ID_MISMATCH)
            else if (organizationVerificationStatus) {
              if (validateUsernamePassword) {
                val aclAddress = blockchainAccounts.Service.tryGetAddress(verifyTraderData.accountID)
                val zoneAccount = masterAccounts.Service.tryGet(loginState.username)
                val zoneBlockchainAccount = getAccount.Service.get(loginState.address)

                def issueIdentityTransaction(zoneAccount: Account, zoneBlockchainAccount: queries.responses.blockchain.AccountResponse.Response, aclAddress: String) = {

                  val immutables = Seq(constants.Property.ACCOUNT_ID.withValue(trader.accountID))
                  val immutableMetas = Seq(constants.Property.USER_TYPE.withValue(constants.User.TRADER))
                  val mutableMetas = Seq(constants.Property.ORGANIZATION_ID.withValue(utilities.String.removeUnacceptableCharacterFromID(verifyTraderData.organizationID)))
                  val mutables = Seq(constants.Property.ZONE_ID.withValue(utilities.String.removeUnacceptableCharacterFromID(trader.zoneID)))

                  val sendCoin = transaction.process[blockchainTransaction.SendCoin, transactionsBroadcast.Request](
                    entity = blockchainTransaction.SendCoin(from = loginState.address, to = aclAddress, amount = Seq(models.common.Serializable.Coin(stakingDenom, constants.Blockchain.DefaultZoneFaucetAmount)), gas = verifyTraderData.gas, ticketID = "", mode = transactionMode),
                    blockchainTransactionCreate = blockchainTransactionSendCoins.Service.create,
                    request = transactionsBroadcast.Request(utilities.SignTx.sign(Tx(Seq(Message(constants.Blockchain.TransactionMessage.SEND_COIN, SendCoin(from_address = loginState.address, to_address = aclAddress, Seq(Coin(stakingDenom, constants.Blockchain.DefaultOrganizationFaucetAmount))))), Fee(Seq(), verifyTraderData.gas.toMicroString)), SignMeta(zoneBlockchainAccount.result.value.accountNumber, chainID, zoneBlockchainAccount.result.value.sequence), ECKey.fromPrivate(utilities.Crypto.decrypt(Base64.getDecoder.decode(zoneAccount.privateKeyEncrypted.getOrElse(throw new BaseException(constants.Response.FAILURE))), verifyTraderData.password))), transactionMode),
                    action = transactionsBroadcast.Service.post,
                    onSuccess = blockchainTransactionSendCoins.Utility.onSuccess,
                    onFailure = blockchainTransactionSendCoins.Utility.onFailure,
                    updateTransactionHash = blockchainTransactionSendCoins.Service.updateTransactionHash)


                  def issueIdentity = transaction.process[blockchainTransaction.IdentityIssue, transactionsIdentityIssue.Request](
                    entity = blockchainTransaction.IdentityIssue(from = loginState.address, fromID = trader.zoneID, classificationID = constants.Blockchain.Classification.TRADER, to = aclAddress, immutableMetaProperties = immutableMetas, immutableProperties = immutables, mutableMetaProperties = mutableMetas, mutableProperties = mutables, gas = verifyTraderData.gas, ticketID = "", mode = transactionMode),
                    blockchainTransactionCreate = blockchainTransactionIdentityIssues.Service.create,
                    request = transactionsIdentityIssue.Request(transactionsIdentityIssue.Message(transactionsIdentityIssue.BaseReq(from = loginState.address, gas = verifyTraderData.gas), fromID = trader.zoneID, classificationID = constants.Blockchain.Classification.TRADER, to = aclAddress, immutableMetaProperties = immutableMetas, immutableProperties = immutables, mutableMetaProperties = mutableMetas, mutableProperties = mutables)),
                    action = transactionsIdentityIssue.Service.post,
                    onSuccess = blockchainTransactionIdentityIssues.Utility.onSuccess,
                    onFailure = blockchainTransactionIdentityIssues.Utility.onFailure,
                    updateTransactionHash = blockchainTransactionIdentityIssues.Service.updateTransactionHash)

                  for {
                    _ <- sendCoin
                    ticketID <- issueIdentity
                  } yield ticketID
                }


                for {
                  aclAddress <- aclAddress
                  zoneAccount <- zoneAccount
                  zoneBlockchainAccount <- zoneBlockchainAccount
                  ticketID <- issueIdentityTransaction(zoneAccount, zoneBlockchainAccount, aclAddress)
                  result <- withUsernameToken.Ok(views.html.account(successes = Seq(constants.Response.ACL_SET)))
                } yield result
              } else Future(BadRequest(views.html.component.master.zoneVerifyTrader(views.companion.master.VerifyTrader.form.fill(verifyTraderData), trader)))
            } else {
              Future(PreconditionFailed(views.html.account(failures = Seq(constants.Response.ORGANIZATION_NOT_VERIFIED))))
            }
          }

          (for {
            organizationVerificationStatus <- organizationVerificationStatus
            zoneID <- zoneID
            trader <- trader
            validateUsernamePassword <- validateUsernamePassword
            result <- sendTransactionsAndGetResult(validateUsernamePassword = validateUsernamePassword, trader = trader, organizationVerificationStatus = organizationVerificationStatus, zoneID = zoneID)
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.account(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def organizationVerifyTraderForm(traderID: String): Action[AnyContent] = withOrganizationLoginAction { implicit loginState =>
    implicit request =>
      val trader = masterTraders.Service.tryGet(traderID)
      val organizationID = masterOrganizations.Service.tryGetID(loginState.username)

      (for {
        trader <- trader
        organizationID <- organizationID
      } yield if (trader.organizationID == organizationID) {
        Ok(views.html.component.master.organizationVerifyTrader(views.companion.master.VerifyTrader.form, trader))
      } else {
        Unauthorized(views.html.account(failures = Seq(constants.Response.UNAUTHORIZED)))
      }
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.account(failures = Seq(baseException.failure)))
      }
  }

  def organizationVerifyTrader: Action[AnyContent] = withOrganizationLoginAction { implicit loginState =>
    implicit request =>
      views.companion.master.VerifyTrader.form.bindFromRequest().fold(
        formWithErrors => {
          val trader = masterTraders.Service.tryGetByAccountID(formWithErrors.data(constants.FormField.ACCOUNT_ID.name))

          (for {
            trader <- trader
          } yield if (trader.organizationID == formWithErrors.data(constants.FormField.ORGANIZATION_ID.name)) {
            BadRequest(views.html.component.master.organizationVerifyTrader(formWithErrors, trader))
          } else {
            Unauthorized(views.html.account(failures = Seq(constants.Response.UNAUTHORIZED)))
          }).recover {
            case baseException: BaseException => InternalServerError(views.html.account(failures = Seq(baseException.failure)))
          }
        },
        verifyTraderData => {
          val trader = masterTraders.Service.tryGetByAccountID(verifyTraderData.accountID)
          val traderOrganization = masterOrganizations.Service.tryGet(verifyTraderData.organizationID)
          val organization = masterOrganizations.Service.tryGetByAccountID(loginState.username)
          val validateUsernamePassword = masterAccounts.Service.validateUsernamePassword(username = loginState.username, password = verifyTraderData.password)

          def getResult(validateUsernamePassword: Boolean, trader: Trader, traderOrganization: Organization, organization: Organization): Future[Result] = {
            if (trader.organizationID != verifyTraderData.organizationID || traderOrganization.id != organization.id) throw new BaseException(constants.Response.UNAUTHORIZED)
            else if (trader.zoneID != traderOrganization.zoneID) throw new BaseException(constants.Response.ZONE_ID_MISMATCH)
            else {
              if (validateUsernamePassword) {
                val aclAddress = blockchainAccounts.Service.tryGetAddress(verifyTraderData.accountID)
                val organizationAccount = masterAccounts.Service.tryGet(loginState.username)
                val organizationBlockchainAccount = getAccount.Service.get(loginState.address)

                def issueIdentityTransaction(organizationAccount: Account, organizationBlockchainAccount: queries.responses.blockchain.AccountResponse.Response, aclAddress: String) = {

                  val immutables = Seq(constants.Property.ACCOUNT_ID.withValue(trader.accountID))
                  val immutableMetas = Seq(constants.Property.USER_TYPE.withValue(constants.User.TRADER))
                  val mutableMetas = Seq(constants.Property.ORGANIZATION_ID.withValue(utilities.String.removeUnacceptableCharacterFromID(verifyTraderData.organizationID)))
                  val mutables = Seq(constants.Property.ZONE_ID.withValue(utilities.String.removeUnacceptableCharacterFromID(trader.zoneID)))

                  val sendCoin = transaction.process[blockchainTransaction.SendCoin, transactionsBroadcast.Request](
                    entity = blockchainTransaction.SendCoin(from = loginState.address, to = aclAddress, amount = Seq(models.common.Serializable.Coin(stakingDenom, constants.Blockchain.DefaultZoneFaucetAmount)), gas = verifyTraderData.gas, ticketID = "", mode = transactionMode),
                    blockchainTransactionCreate = blockchainTransactionSendCoins.Service.create,
                    request = transactionsBroadcast.Request(utilities.SignTx.sign(Tx(Seq(Message(constants.Blockchain.TransactionMessage.SEND_COIN, SendCoin(from_address = loginState.address, to_address = aclAddress, Seq(Coin(stakingDenom, constants.Blockchain.DefaultOrganizationFaucetAmount))))), Fee(Seq(), verifyTraderData.gas.toMicroString)), SignMeta(organizationBlockchainAccount.result.value.accountNumber, chainID, organizationBlockchainAccount.result.value.sequence), ECKey.fromPrivate(utilities.Crypto.decrypt(Base64.getDecoder.decode(organizationAccount.privateKeyEncrypted.getOrElse(throw new BaseException(constants.Response.FAILURE))), verifyTraderData.password))), transactionMode),
                    action = transactionsBroadcast.Service.post,
                    onSuccess = blockchainTransactionSendCoins.Utility.onSuccess,
                    onFailure = blockchainTransactionSendCoins.Utility.onFailure,
                    updateTransactionHash = blockchainTransactionSendCoins.Service.updateTransactionHash)

                  def issueIdentity = transaction.process[blockchainTransaction.IdentityIssue, transactionsIdentityIssue.Request](
                    entity = blockchainTransaction.IdentityIssue(from = loginState.address, fromID = organization.id, classificationID = constants.Blockchain.Classification.TRADER, to = aclAddress, immutableMetaProperties = immutableMetas, immutableProperties = immutables, mutableMetaProperties = mutableMetas, mutableProperties = mutables, gas = verifyTraderData.gas, ticketID = "", mode = transactionMode),
                    blockchainTransactionCreate = blockchainTransactionIdentityIssues.Service.create,
                    request = transactionsIdentityIssue.Request(transactionsIdentityIssue.Message(transactionsIdentityIssue.BaseReq(from = loginState.address, gas = verifyTraderData.gas), fromID = organization.zoneID, classificationID = constants.Blockchain.Classification.TRADER, to = aclAddress, immutableMetaProperties = immutableMetas, immutableProperties = immutables, mutableMetaProperties = mutableMetas, mutableProperties = mutables)),
                    action = transactionsIdentityIssue.Service.post,
                    onSuccess = blockchainTransactionIdentityIssues.Utility.onSuccess,
                    onFailure = blockchainTransactionIdentityIssues.Utility.onFailure,
                    updateTransactionHash = blockchainTransactionIdentityIssues.Service.updateTransactionHash)

                  for {
                    _ <- sendCoin
                    ticketID <- issueIdentity
                  } yield ticketID
                }

                for {
                  aclAddress <- aclAddress
                  organizationAccount <- organizationAccount
                  organizationBlockchainAccount <- organizationBlockchainAccount
                  _ <- issueIdentityTransaction(organizationAccount, organizationBlockchainAccount, aclAddress)
                  result <- withUsernameToken.Ok(views.html.account(successes = Seq(constants.Response.ACL_SET)))
                } yield result
              } else Future(BadRequest(views.html.component.master.organizationVerifyTrader(views.companion.master.VerifyTrader.form.fill(verifyTraderData).withGlobalError(constants.Response.INCORRECT_PASSWORD.message), trader = trader)))
            }
          }

          (for {
            validateUsernamePassword <- validateUsernamePassword
            trader <- trader
            traderOrganization <- traderOrganization
            organization <- organization
            result <- getResult(validateUsernamePassword = validateUsernamePassword, trader = trader, traderOrganization = traderOrganization, organization = organization)
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.account(failures = Seq(baseException.failure)))
          }
        }
      )
  }


  def deputizeForm(traderID: String): Action[AnyContent] = withOrganizationLoginAction { implicit loginState =>
    implicit request =>
      Future(Ok(views.html.component.master.deputizeTrader(traderID = traderID)))
  }

  def deputize: Action[AnyContent] = withOrganizationLoginAction { implicit loginState =>
    implicit request =>
      views.companion.master.DeputizeTrader.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.deputizeTrader(formWithErrors, traderID = formWithErrors.data(constants.FormField.TRADER_ID.name))))
        },
        deputizeTraderData => {
          val trader = masterTraders.Service.tryGet(deputizeTraderData.traderID)
          val traderClassifications = masterClassifications.Service.getByIdentityIDs(Seq(deputizeTraderData.traderID))

          def deputizeAndGetResult(trader: Trader, traderClassifications: Seq[Classification]) = {
            if (!trader.deputizeStatus) {
              //TODO Need to implement better solution than manual delays for deputize
              def deputizeUnmoderatedAssetClassification = {
                if (deputizeTraderData.createUnmoderatedAsset && !traderClassifications.map(_.id).contains(constants.Blockchain.Classification.UNMODERATED_ASSET)) {
                  val classificationProperties = masterProperties.Service.getAll(constants.Blockchain.Classification.UNMODERATED_ASSET, constants.Blockchain.Entity.ASSET_DEFINITION)

                  def broadcastTx(classificationProperties: Seq[models.master.Property]) = transaction.process[blockchainTransaction.MaintainerDeputize, transactionsMaintainerDeputize.Request](
                    entity = blockchainTransaction.MaintainerDeputize(from = loginState.address, fromID = trader.organizationID, toID = deputizeTraderData.traderID, classificationID = constants.Blockchain.Classification.UNMODERATED_ASSET, maintainedTraits = classificationProperties.filter(_.isMutable).map(x => BaseProperty(x.dataType, x.name, x.value)), addMaintainer = true, mutateMaintainer = true, removeMaintainer = true, gas = deputizeTraderData.gas, ticketID = "", mode = transactionMode),
                    blockchainTransactionCreate = blockchainTransactionMaintainerDeputizes.Service.create,
                    request = transactionsMaintainerDeputize.Request(transactionsMaintainerDeputize.Message(transactionsMaintainerDeputize.BaseReq(from = loginState.address, gas = deputizeTraderData.gas), fromID = trader.organizationID, toID = deputizeTraderData.traderID, classificationID = constants.Blockchain.Classification.UNMODERATED_ASSET, maintainedTraits = classificationProperties.filter(_.isMutable).map(x => BaseProperty(x.dataType, x.name, x.value)), addMaintainer = true, mutateMaintainer = true, removeMaintainer = true)),
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
                if (deputizeTraderData.createFiat && !traderClassifications.map(_.id).contains(constants.Blockchain.Classification.FIAT)) {
                  val classificationProperties = masterProperties.Service.getAll(constants.Blockchain.Classification.FIAT, constants.Blockchain.Entity.ASSET_DEFINITION)

                  def broadcastTx(classificationProperties: Seq[models.master.Property]) = transaction.process[blockchainTransaction.MaintainerDeputize, transactionsMaintainerDeputize.Request](
                    entity = blockchainTransaction.MaintainerDeputize(from = loginState.address, fromID = trader.organizationID, toID = deputizeTraderData.traderID, classificationID = constants.Blockchain.Classification.FIAT, maintainedTraits = classificationProperties.filter(_.isMutable).map(x => BaseProperty(x.dataType, x.name, x.value)), addMaintainer = true, mutateMaintainer = true, removeMaintainer = true, gas = deputizeTraderData.gas, ticketID = "", mode = transactionMode),
                    blockchainTransactionCreate = blockchainTransactionMaintainerDeputizes.Service.create,
                    request = transactionsMaintainerDeputize.Request(transactionsMaintainerDeputize.Message(transactionsMaintainerDeputize.BaseReq(from = loginState.address, gas = deputizeTraderData.gas), fromID = trader.organizationID, toID = deputizeTraderData.traderID, classificationID = constants.Blockchain.Classification.FIAT, maintainedTraits = classificationProperties.filter(_.isMutable).map(x => BaseProperty(x.dataType, x.name, x.value)), addMaintainer = true, mutateMaintainer = true, removeMaintainer = true)),
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
                if (deputizeTraderData.createOrder && !traderClassifications.map(_.id).contains(constants.Blockchain.Classification.ORDER)) {
                  val classificationProperties = masterProperties.Service.getAll(constants.Blockchain.Classification.ORDER, constants.Blockchain.Entity.ORDER_DEFINITION)

                  def broadcastTx(classificationProperties: Seq[models.master.Property]) = transaction.process[blockchainTransaction.MaintainerDeputize, transactionsMaintainerDeputize.Request](
                    entity = blockchainTransaction.MaintainerDeputize(from = loginState.address, fromID = trader.organizationID, toID = deputizeTraderData.traderID, classificationID = constants.Blockchain.Classification.ORDER, maintainedTraits = classificationProperties.filter(_.isMutable).map(x => BaseProperty(x.dataType, x.name, x.value)), addMaintainer = true, mutateMaintainer = true, removeMaintainer = true, gas = deputizeTraderData.gas, ticketID = "", mode = transactionMode),
                    blockchainTransactionCreate = blockchainTransactionMaintainerDeputizes.Service.create,
                    request = transactionsMaintainerDeputize.Request(transactionsMaintainerDeputize.Message(transactionsMaintainerDeputize.BaseReq(from = loginState.address, gas = deputizeTraderData.gas), fromID = trader.organizationID, toID = deputizeTraderData.traderID, classificationID = constants.Blockchain.Classification.ORDER, maintainedTraits = classificationProperties.filter(_.isMutable).map(x => BaseProperty(x.dataType, x.name, x.value)), addMaintainer = true, mutateMaintainer = true, removeMaintainer = true)),
                    action = transactionsMaintainerDeputize.Service.post,
                    onSuccess = blockchainTransactionMaintainerDeputizes.Utility.onSuccess,
                    onFailure = blockchainTransactionMaintainerDeputizes.Utility.onFailure,
                    updateTransactionHash = blockchainTransactionMaintainerDeputizes.Service.updateTransactionHash
                  )

                  for {
                    classificationProperties <- classificationProperties
                    _ <- broadcastTx(classificationProperties)
                  } yield ()
                } else {
                  Future()
                }
              }

              for {
                _ <- deputizeUnmoderatedAssetClassification
                _ <- deputizeFiatClassification
                _ <- deputizeOrderClassification
              } yield ()
            } else {
              throw new BaseException(constants.Response.FAILURE)
            }
          }

          (for {
            trader <- trader
            traderClassifications <- traderClassifications
            _ <- deputizeAndGetResult(trader, traderClassifications)
            result <- withUsernameToken.Ok(views.html.account(successes = Seq(constants.Response.TRADER_DEPUTIZED)))
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.account(failures = Seq(baseException.failure)))
          }
        }
      )
  }

}