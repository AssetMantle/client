package controllers

import actors.ShutdownActor
import controllers.actions.{LoginState, WithLoginAction}
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.blockchain.{ACL, ACLAccount}
import models.master.{Organization, Zone}
import models.{blockchain, master, masterTransaction}
import play.api.i18n.I18nSupport
import play.api.libs.ws.WSClient
import play.api.mvc._
import play.api.{Configuration, Logger}
import views.companion.master.{Login, Logout, SignUp, VerifyPassphrase}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

@Singleton
class AccountController @Inject()(
                                   utilitiesNotification: utilities.Notification,
                                   shutdownActor: ShutdownActor,
                                   withLoginAction: WithLoginAction,
                                   withUsernameToken: WithUsernameToken,
                                   queryGetAccount: queries.GetAccount,
                                   blockchainFiats: blockchain.Fiats,
                                   blockchainZones: blockchain.Zones,
                                   blockchainOrders: blockchain.Orders,
                                   blockchainAssets: blockchain.Assets,
                                   blockchainAccounts: blockchain.Accounts,
                                   blockchainAclHashes: blockchain.ACLHashes,
                                   blockchainAclAccounts: blockchain.ACLAccounts,
                                   blockchainNegotiations: blockchain.Negotiations,
                                   blockchainOrganizations: blockchain.Organizations,
                                   masterOrganizations: master.Organizations,
                                   masterZones: master.Zones,
                                   masterAccounts: master.Accounts,
                                   masterTransactionEmailOTP: masterTransaction.EmailOTPs,
                                   masterTransactionSessionTokens: masterTransaction.SessionTokens,
                                   masterTransactionPushNotificationTokens: masterTransaction.PushNotificationTokens,
                                   transactionAddKey: transactions.AddKey,
                                   transactionForgotPassword: transactions.ForgotPassword,
                                   transactionChangePassword: transactions.ChangePassword,
                                   messagesControllerComponents: MessagesControllerComponents,
                                 )
                                 (implicit
                                  executionContext: ExecutionContext,
                                  configuration: Configuration,
                                  wsClient: WSClient,
                                 ) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val module: String = constants.Module.CONTROLLER_ACCOUNT

  private implicit val logger: Logger = Logger(this.getClass)

  def signUpForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.signUp())
  }

  def signUp: Action[AnyContent] = Action.async { implicit request =>
    SignUp.form.bindFromRequest().fold(
      formWithErrors => {
        Future(BadRequest(views.html.component.master.signUp(formWithErrors)))
      },
      signUpData => {
        val addKeyResponse = transactionAddKey.Service.post(transactionAddKey.Request(signUpData.username, signUpData.password))

        def createAccount(addKeyResponse: transactionAddKey.Response): Future[String] = blockchainAccounts.Service.create(address = addKeyResponse.address, pubkey = addKeyResponse.pubkey)

        def addLogin(createAccount: String): Future[String] = masterAccounts.Service.addLogin(signUpData.username, signUpData.password, createAccount, request.lang.toString.stripPrefix("Lang(").stripSuffix(")").trim.split("_")(0))

        (for {
          addKeyResponse <- addKeyResponse
          createAccount <- createAccount(addKeyResponse)
          _ <- addLogin(createAccount)
        } yield {
          println(addKeyResponse.mnemonic)
          PartialContent(views.html.component.master.noteNewKeyDetails(seed = addKeyResponse.mnemonic))
        }).recover {
          case baseException: BaseException =>
            InternalServerError(views.html.index(failures = Seq(baseException.failure)))
        }
      }
    )
  }

  def noteNewKeyDetailsView( seed: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.noteNewKeyDetails( seed = seed))
  }

  def verifyPassphrase(): Action[AnyContent] = Action { implicit request =>
    VerifyPassphrase.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.component.master.verifyPassphrase(formWithErrors,seed=formWithErrors.data(constants.FormField.SEED.name),randomSeq=Seq(formWithErrors.data(constants.FormField.PASSPHRASE_ELEMENT_ID_1.name).toInt,formWithErrors.data(constants.FormField.PASSPHRASE_ELEMENT_ID_2.name).toInt,formWithErrors.data(constants.FormField.PASSPHRASE_ELEMENT_ID_3.name).toInt)))
      },
      verifyPassphraseData=> {
        val seedSeq = verifyPassphraseData.seed.split(" ")
        if(verifyPassphraseData.passphraseElement1 == seedSeq(verifyPassphraseData.passphraseElementID1) && verifyPassphraseData.passphraseElement2== seedSeq(verifyPassphraseData.passphraseElementID2) && verifyPassphraseData.passphraseElement3== seedSeq(verifyPassphraseData.passphraseElementID3)){
          Ok(views.html.indexVersion3(successes = Seq(constants.Response.SIGNED_UP)))
        }else BadRequest(views.html.component.master.verifyPassphrase(views.companion.master.VerifyPassphrase.form.fill(views.companion.master.VerifyPassphrase.Data(passphraseElement1 = verifyPassphraseData.passphraseElement1,passphraseElement2=verifyPassphraseData.passphraseElement2,passphraseElement3=verifyPassphraseData.passphraseElement3,seed=verifyPassphraseData.seed,passphraseElementID1 = verifyPassphraseData.passphraseElementID1,passphraseElementID2 =verifyPassphraseData.passphraseElementID2 ,passphraseElementID3 = verifyPassphraseData.passphraseElementID3)),verifyPassphraseData.seed,Seq(verifyPassphraseData.passphraseElementID1,verifyPassphraseData.passphraseElementID2,verifyPassphraseData.passphraseElementID3),"Incorrect Input"))
      }
    )
  }

  def loginForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.login())
  }

  def login: Action[AnyContent] = Action.async { implicit request =>
    Login.form.bindFromRequest().fold(
      formWithErrors => {
        Future(BadRequest(views.html.component.master.login(formWithErrors)))
      },
      loginData => {
        val userType = masterAccounts.Service.getUserType(loginData.username)
        val address = masterAccounts.Service.getAddress(loginData.username)
        val status = masterAccounts.Service.validateLoginAndGetStatus(loginData.username, loginData.password)

        def getLoginState(address: String, userType: String): Future[LoginState] = {
          if (userType == constants.User.TRADER) {
            val aclHash = blockchainAclAccounts.Service.getACLHash(address)

            def acl(aclHash: String): Future[ACL] = blockchainAclHashes.Service.getACL(aclHash)

            for {
              aclHash <- aclHash
              acl <- acl(aclHash)
            } yield LoginState(loginData.username, userType, address, Option(acl))
          } else Future(LoginState(loginData.username, userType, address, None))
        }

        def sendNotification(loginState: LoginState): Future[Unit] = {
          val pushNotificationTokenUpdate = masterTransactionPushNotificationTokens.Service.update(id = loginState.username, token = loginData.pushNotificationToken)
          for {
            _ <- pushNotificationTokenUpdate
          } yield utilitiesNotification.send(loginData.username, constants.Notification.LOGIN, loginData.username)
        }

        // Discuss loginStateVal
        def getResult(status: String, loginStateValue: LoginState): Future[Result] = {
          implicit val loginState = loginStateValue
          val contactWarnings = utilities.Contact.getWarnings(status)
          loginState.userType match {
            case constants.User.GENESIS => withUsernameToken.Ok(views.html.genesisIndex(warnings = contactWarnings))
            case constants.User.ZONE => val zoneID = blockchainZones.Service.getID(loginState.address)

              def zone(zoneID: String): Future[Zone] = masterZones.Service.get(zoneID)

              for {
                zoneID <- zoneID
                zone <- zone(zoneID)
                result <- withUsernameToken.Ok(views.html.zoneIndex(zone = zone, warnings = contactWarnings))
              } yield result
            case constants.User.ORGANIZATION => val organizationID = blockchainOrganizations.Service.getID(loginState.address)

              def organization(organizationID: String): Future[Organization] = masterOrganizations.Service.get(organizationID)

              for {
                organizationID <- organizationID
                organization <- organization(organizationID)
                result <- withUsernameToken.Ok(views.html.organizationIndex(organization = organization, warnings = contactWarnings))
              } yield result
            case constants.User.TRADER => val aclAccount = blockchainAclAccounts.Service.get(loginState.address)
              val fiatPegWallet = blockchainFiats.Service.getFiatPegWallet(loginState.address)

              def organization(aclAccount: ACLAccount): Future[Organization] = masterOrganizations.Service.get(aclAccount.organizationID)

              def zone(aclAccount: ACLAccount): Future[Zone] = masterZones.Service.get(aclAccount.zoneID)

              for {
                aclAccount <- aclAccount
                fiatPegWallet <- fiatPegWallet
                organization <- organization(aclAccount)
                zone <- zone(aclAccount)
                result <- withUsernameToken.Ok(views.html.traderIndex(totalFiat = fiatPegWallet.map(_.transactionAmount.toInt).sum, zone = zone, organization = organization, warnings = contactWarnings))
              } yield result
            case constants.User.USER => withUsernameToken.Ok(views.html.dashboard(warnings = contactWarnings))
            case constants.User.UNKNOWN => withUsernameToken.Ok(views.html.dashboard(warnings = contactWarnings))
            case constants.User.WITHOUT_LOGIN => val updateUserType = masterAccounts.Service.updateUserType(loginData.username, constants.User.UNKNOWN)
              for {
                _ <- updateUserType
                result <- withUsernameToken.Ok(views.html.dashboard(warnings = contactWarnings))
              } yield result
          }
        }

        (for {
          userType <- userType
          address <- address
          loginState <- getLoginState(address, userType)
          status <- status
          _ <- sendNotification(loginState)
          result <- getResult(status, loginState)
        } yield result
          ).recover {
          case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
        }
      }
    )
  }

  def logoutForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.logout())
  }

  def logout: Action[AnyContent] = withLoginAction.authenticated { loginState =>
    implicit request =>
      Logout.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.logout(formWithErrors)))
        },
        loginData => {
          val pushNotificationTokenDelete = if (!loginData.receiveNotifications) masterTransactionPushNotificationTokens.Service.delete(loginState.username) else Future(Unit)

          def transactionSessionTokensDelete: Future[Int] = masterTransactionSessionTokens.Service.delete(loginState.username)

          def shutdownActorsAndGetResult = {
            shutdownActor.onLogOut(constants.Module.ACTOR_MAIN_ACCOUNT, loginState.username)
            if (loginState.userType == constants.User.TRADER) {
              shutdownActor.onLogOut(constants.Module.ACTOR_MAIN_ASSET, loginState.username)
              shutdownActor.onLogOut(constants.Module.ACTOR_MAIN_FIAT, loginState.username)
              shutdownActor.onLogOut(constants.Module.ACTOR_MAIN_NEGOTIATION, loginState.username)
              shutdownActor.onLogOut(constants.Module.ACTOR_MAIN_ORDER, loginState.username)
            }
            Ok(views.html.index(successes = Seq(constants.Response.LOGGED_OUT))).withNewSession
          }

          (for {
            _ <- pushNotificationTokenDelete
            _ <- transactionSessionTokensDelete
          } yield shutdownActorsAndGetResult).recover {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def changePasswordForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.changePassword())
  }

  def changePassword: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.ChangePassword.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.changePassword(formWithErrors)))
        },
        changePasswordData => {
          val validLogin = masterAccounts.Service.validateLogin(loginState.username, changePasswordData.oldPassword)

          def updateAndGetResult(validLogin: Boolean): Future[Result] = if (validLogin) {
            val postRequest = transactionChangePassword.Service.post(username = loginState.username, transactionChangePassword.Request(oldPassword = changePasswordData.oldPassword, newPassword = changePasswordData.newPassword, confirmNewPassword = changePasswordData.confirmNewPassword))

            def updatePassword: Future[Int] = masterAccounts.Service.updatePassword(username = loginState.username, newPassword = changePasswordData.newPassword)

            for {
              _ <- postRequest
              _ <- updatePassword
              result <- withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.PASSWORD_UPDATED)))
            } yield result
          } else {
            Future(BadRequest(views.html.index(failures = Seq(constants.Response.INVALID_PASSWORD))))
          }

          (for {
            validLogin <- validLogin
            result <- updateAndGetResult(validLogin)
          } yield result).recover {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def emailOTPForgotPasswordForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.emailOTPForgotPassword())
  }

  def emailOTPForgotPassword: Action[AnyContent] = Action.async { implicit request =>
    views.companion.master.EmailOTPForgotPassword.form.bindFromRequest().fold(
      formWithErrors => {
        Future(BadRequest(views.html.component.master.emailOTPForgotPassword(formWithErrors)))
      },
      emailOTPForgotPasswordData => {
        val otp = masterTransactionEmailOTP.Service.sendOTP(emailOTPForgotPasswordData.username)
        (for {
          otp <- otp
        } yield {
          utilitiesNotification.send(accountID = emailOTPForgotPasswordData.username, notification = constants.Notification.FORGOT_PASSWORD_OTP, otp)
          PartialContent(views.html.component.master.forgotPassword(views.companion.master.ForgotPassword.form, emailOTPForgotPasswordData.username))
        }).recover {
          case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
        }
      }
    )
  }

  def forgotPasswordForm(username: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.forgotPassword(username = username))
  }

  def forgotPassword: Action[AnyContent] = Action.async { implicit request =>
    views.companion.master.ForgotPassword.form.bindFromRequest().fold(
      formWithErrors => {
        Future(BadRequest(views.html.component.master.forgotPassword(formWithErrors, formWithErrors(constants.FormField.USERNAME.name).value.getOrElse(""))))
      },
      forgotPasswordData => {
        val validOTP = masterTransactionEmailOTP.Service.verifyOTP(forgotPasswordData.username, forgotPasswordData.otp)

        def updateAndGetResult(validOTP: Boolean): Future[Result] = {
          if (validOTP) {
            val post = transactionForgotPassword.Service.post(username = forgotPasswordData.username, transactionForgotPassword.Request(seed = forgotPasswordData.mnemonic, newPassword = forgotPasswordData.newPassword, confirmNewPassword = forgotPasswordData.confirmNewPassword))
            val updatePassword = masterAccounts.Service.updatePassword(username = forgotPasswordData.username, newPassword = forgotPasswordData.newPassword)
            for {
              _ <- post
              _ <- updatePassword
            } yield Ok(views.html.index(successes = Seq(constants.Response.PASSWORD_UPDATED)))
          } else {
            Future(BadRequest(views.html.index(failures = Seq(constants.Response.INVALID_PASSWORD))))
          }
        }

        (for {
          validOTP <- validOTP
          result <- updateAndGetResult(validOTP)
        } yield result).recover {
          case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
        }
      }
    )
  }

  def checkUsernameAvailable(username: String): Action[AnyContent] = Action.async { implicit request =>
    val checkUsernameAvailable = masterAccounts.Service.checkUsernameAvailable(username)
    for {
      checkUsernameAvailable <- checkUsernameAvailable
    } yield if (checkUsernameAvailable) Ok else NoContent
  }

  //TODO Remove query parameters
  def noteNewKeyDetails(seed: String): Action[AnyContent] = Action { implicit request =>
    views.companion.master.NoteNewKeyDetails.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.component.master.noteNewKeyDetails(formWithErrors,seed))
      },
      noteNewKeyDetailsData => {
        if (noteNewKeyDetailsData.confirmNoteNewKeyDetails) {
          val randomSeq=Random.shuffle(seed.split(" ").indices.toList).take(3)
          PartialContent(views.html.component.master.verifyPassphrase(views.companion.master.VerifyPassphrase.form,seed,randomSeq))
        }
        else {
          BadRequest(views.html.component.master.noteNewKeyDetails( seed = seed))
        }
      }
    )
  }
}
