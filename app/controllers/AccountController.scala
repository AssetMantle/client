package controllers

import actors.ShutdownActor
import controllers.actions.{LoginState, WithLoginAction}
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.{blockchain, master, masterTransaction}
import play.api.i18n.I18nSupport
import play.api.libs.ws.WSClient
import play.api.mvc._
import play.api.{Configuration, Logger}
import views.companion.master.{Login, Logout, NoteNewKeyDetails, SignUp}

import scala.concurrent.{ExecutionContext, Future}

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
                                   masterTransactionAccountTokens: masterTransaction.AccountTokens,
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
    Ok(views.html.component.master.signUp(SignUp.form))
  }

  def signUp: Action[AnyContent] = Action.async { implicit request =>
    SignUp.form.bindFromRequest().fold(
      formWithErrors => {
        Future{BadRequest(views.html.component.master.signUp(formWithErrors))}
      },
      signUpData => {

        val addKeyResponse=transactionAddKey.Service.post(transactionAddKey.Request(signUpData.username, signUpData.password))
        def addLogin(addKeyResponse: transactionAddKey.Response)=masterAccounts.Service.addLogin(signUpData.username, signUpData.password, blockchainAccounts.Service.create(address = addKeyResponse.address, pubkey = addKeyResponse.pubkey), request.lang.toString.stripPrefix("Lang(").stripSuffix(")").trim.split("_")(0))

        (for{
          addKeyResponse<- addKeyResponse
          _<- addLogin(addKeyResponse)
        }yield PartialContent(views.html.component.master.noteNewKeyDetails(NoteNewKeyDetails.form, addKeyResponse.name, addKeyResponse.address, addKeyResponse.pubkey, addKeyResponse.mnemonic))
        ).recover{
          case  baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
        }
      }
    )
  }


  def loginForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.login(Login.form))
  }

  def login: Action[AnyContent] = Action.async { implicit request =>
    Login.form.bindFromRequest().fold(
      formWithErrors => {
        Future{BadRequest(views.html.component.master.login(formWithErrors))}
      },
      loginData => {

          val userTypeFuture = masterAccounts.Service.getUserType(loginData.username)
          val addressFuture = masterAccounts.Service.getAddress(loginData.username)
          val status=masterAccounts.Service.validateLoginAndGetStatus(loginData.username, loginData.password)

        (for{
            userType<- userTypeFuture
            address<- addressFuture
            status<- status
            aclHash<- blockchainAclAccounts.Service.getACLHash(address)
            acl<-blockchainAclHashes.Service.getACL(aclHash)
          }yield {
          implicit val loginState: LoginState = LoginState(loginData.username, userType, address, if (userType == constants.User.TRADER) Option(acl) else None)
          utilitiesNotification.registerNotificationToken(loginData.username, loginData.notificationToken)
          utilitiesNotification.send(loginData.username, constants.Notification.LOGIN, loginData.username)
          val contactWarnings=utilities.Contact.getWarnings(status)
          loginState.userType match {
            case constants.User.GENESIS =>
              withUsernameToken.Ok(views.html.genesisIndex(warnings = contactWarnings))
            case constants.User.ZONE =>
              withUsernameToken.Ok(views.html.zoneIndex(zone = masterZones.Service.get(blockchainZones.Service.getID(loginState.address)), warnings = contactWarnings))
            case constants.User.ORGANIZATION =>
              withUsernameToken.Ok(views.html.organizationIndex(organization = masterOrganizations.Service.get(blockchainOrganizations.Service.getID(loginState.address)), warnings = contactWarnings))
            case constants.User.TRADER =>
              val aclAccount = blockchainAclAccounts.Service.get(loginState.address)
              withUsernameToken.Ok(views.html.traderIndex(totalFiat = blockchainFiats.Service.getFiatPegWallet(loginState.address).map(_.transactionAmount.toInt).sum, zone = masterZones.Service.get(aclAccount.zoneID), organization = masterOrganizations.Service.get(aclAccount.organizationID), warnings = contactWarnings))
            case constants.User.USER =>
              withUsernameToken.Ok(views.html.userIndex(warnings = contactWarnings))
            case constants.User.UNKNOWN =>
              withUsernameToken.Ok(views.html.anonymousIndex(warnings = contactWarnings))
            case constants.User.WITHOUT_LOGIN =>
              masterAccounts.Service.updateUserType(loginData.username, constants.User.UNKNOWN)
              withUsernameToken.Ok(views.html.anonymousIndex(warnings = contactWarnings))
          }
        }
          ).recover{
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }

      }
    )
  }


  def logoutForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.logout(Logout.form))
  }

  def logout: Action[AnyContent] = withLoginAction.authenticated { loginState =>
    
    implicit request =>
      Logout.form.bindFromRequest().fold(
        formWithErrors => {
          Future{BadRequest(views.html.component.master.logout(formWithErrors))}
        },
        loginData => {

          val deleteOrResetToken=if (!loginData.receiveNotifications) {
            masterTransactionAccountTokens.Service.deleteToken(loginState.username)
          } else {
            masterTransactionAccountTokens.Service.resetSessionTokenTime(loginState.username)
          }

          def shutdownActorAndGetResult= {
            shutdownActor.onLogOut(constants.Module.ACTOR_MAIN_ACCOUNT, loginState.username)
            masterAccounts.Service.getUserType(loginState.username).map { userType =>
              if (userType == constants.User.TRADER) {
                shutdownActor.onLogOut(constants.Module.ACTOR_MAIN_ASSET, loginState.username)
                shutdownActor.onLogOut(constants.Module.ACTOR_MAIN_FIAT, loginState.username)
                shutdownActor.onLogOut(constants.Module.ACTOR_MAIN_NEGOTIATION, loginState.username)
                shutdownActor.onLogOut(constants.Module.ACTOR_MAIN_ORDER, loginState.username)
              }
              Ok(views.html.index(successes = Seq(constants.Response.LOGGED_OUT))).withNewSession
            }
          }

          (for{
            _<- deleteOrResetToken
            result<-shutdownActorAndGetResult
          }yield result).recover{
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )

  }


  def changePasswordForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.changePassword(views.companion.master.ChangePassword.form))
  }

  def changePassword: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.ChangePassword.form.bindFromRequest().fold(
        formWithErrors => {
          Future{BadRequest(views.html.component.master.changePassword(formWithErrors))}
        },
        changePasswordData => {

          val validLogin=masterAccounts.Service.validateLogin(loginState.username, changePasswordData.oldPassword)
          def updateAndGetResult(validLogin:Boolean)= if(validLogin){
            val postRequest=transactionChangePassword.Service.post(username = loginState.username, transactionChangePassword.Request(oldPassword = changePasswordData.oldPassword, newPassword = changePasswordData.newPassword, confirmNewPassword = changePasswordData.confirmNewPassword))
            val updatePassword=masterAccounts.Service.updatePassword(username = loginState.username, newPassword = changePasswordData.newPassword)
            for{
              _<- postRequest
              _<- updatePassword
            }yield Ok(views.html.index(successes = Seq(constants.Response.PASSWORD_UPDATED)))
          }else{
            Future{BadRequest(views.html.index(failures = Seq(constants.Response.INVALID_PASSWORD)))}
          }

          (for{
            validLogin<- validLogin
            result<- updateAndGetResult(validLogin)
          }yield result).recover{
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))

          }
        }
      )
  }


  def emailOTPForgotPasswordForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.emailOTPForgotPassword(views.companion.master.EmailOTPForgotPassword.form))
  }

  def emailOTPForgotPassword: Action[AnyContent] = Action.async { implicit request =>
    views.companion.master.EmailOTPForgotPassword.form.bindFromRequest().fold(
      formWithErrors => {
        Future{BadRequest(views.html.component.master.emailOTPForgotPassword(formWithErrors))}
      },
      emailOTPForgotPasswordData => {

        val otp=masterTransactionEmailOTP.Service.sendOTP(emailOTPForgotPasswordData.username)

        (for{
          otp<-otp
        }yield {
          utilitiesNotification.send(accountID = emailOTPForgotPasswordData.username, notification = constants.Notification.FORGOT_PASSWORD_OTP, otp)
          PartialContent(views.html.component.master.forgotPassword(views.companion.master.ForgotPassword.form, emailOTPForgotPasswordData.username))
        }).recover{
          case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
        }
      }
    )
  }


  def forgotPasswordForm(username: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.forgotPassword(views.companion.master.ForgotPassword.form, username))
  }

  def forgotPassword(username: String): Action[AnyContent] = Action.async { implicit request =>
    views.companion.master.ForgotPassword.form.bindFromRequest().fold(
      formWithErrors => {
        Future{BadRequest(views.html.component.master.forgotPassword(formWithErrors, username))}
      },
      forgotPasswordData => {

        val validOTP =masterTransactionEmailOTP.Service.verifyOTP(username, forgotPasswordData.otp)
        def updateAndGetResult(validOTP: Boolean)={
          if(validOTP){
            val postRequest=transactionForgotPassword.Service.post(username = username, transactionForgotPassword.Request(seed = forgotPasswordData.mnemonic, newPassword = forgotPasswordData.newPassword, confirmNewPassword = forgotPasswordData.confirmNewPassword))
            val updatePassword=masterAccounts.Service.updatePassword(username = username, newPassword = forgotPasswordData.newPassword)
            for{
              _<- postRequest
              _<- updatePassword
            }yield Ok(views.html.index(successes = Seq(constants.Response.PASSWORD_UPDATED)))
          }else{
            Future{BadRequest(views.html.index(failures = Seq(constants.Response.INVALID_PASSWORD)))}
          }
        }
        (for{
          validOTP <- validOTP
          result <- updateAndGetResult(validOTP)
        }yield result).recover{
          case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
        }


      }
    )
  }


  def checkUsernameAvailable(username: String): Action[AnyContent] = Action { implicit request =>
    if (masterAccounts.Service.checkUsernameAvailable(username)) Ok else NoContent
  }

  def noteNewKeyDetails(name: String, blockchainAddress: String, publicKey: String, seed: String): Action[AnyContent] = Action { implicit request =>
    views.companion.master.NoteNewKeyDetails.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.component.master.noteNewKeyDetails(formWithErrors, name, blockchainAddress, publicKey, seed))
      },
      noteNewKeyDetailsData => {
        if (noteNewKeyDetailsData.confirmNoteNewKeyDetails) {
          Ok(views.html.index(successes = Seq(constants.Response.SIGNED_UP)))
        }
        else {
          BadRequest(views.html.component.master.noteNewKeyDetails(NoteNewKeyDetails.form, name, blockchainAddress, publicKey, seed))
        }
      })
  }
}
