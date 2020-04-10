package controllers

import actors.ShutdownActor
import controllers.actions.{LoginState, WithLoginAction, WithTraderLoginAction}
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.blockchain.{ACL, ACLAccount}
import models.master.{Identification, Organization, Zone}
import models.{blockchain, master, masterTransaction}
import play.api.i18n.I18nSupport
import play.api.libs.ws.WSClient
import play.api.mvc._
import play.api.{Configuration, Logger}
import services.SFTPScheduler
import views.companion.master.{Login, Logout, SignUp}

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
                                   masterTransactionSessionTokens: masterTransaction.SessionTokens,
                                   masterTransactionPushNotificationTokens: masterTransaction.PushNotificationTokens,
                                   queriesMnemonic: queries.GetMnemonic,
                                   transactionAddKey: transactions.AddKey,
                                   transactionForgotPassword: transactions.ForgotPassword,
                                   transactionChangePassword: transactions.ChangePassword,
                                   sftpScheduler: SFTPScheduler,
                                   messagesControllerComponents: MessagesControllerComponents,
                                   withTraderLoginAction: WithTraderLoginAction,
                                   masterTraderRelations: master.TraderRelations,
                                   masterContacts: master.Contacts,
                                   masterTraders: master.Traders,
                                   masterIdentifications: master.Identifications,
                                   masterAccountKYCs: master.AccountKYCs
                                 )
                                 (implicit
                                  executionContext: ExecutionContext,
                                  configuration: Configuration,
                                  wsClient: WSClient,
                                 ) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val module: String = constants.Module.CONTROLLERS_ACCOUNT

  private implicit val logger: Logger = Logger(this.getClass)

  def signUpForm(mnemonic: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.signUp(views.companion.master.SignUp.form, mnemonic))
  }

  def signUp: Action[AnyContent] = Action.async { implicit request =>
    SignUp.form.bindFromRequest().fold(
      formWithErrors => {
        Future(BadRequest(views.html.component.master.signUp(formWithErrors, formWithErrors.data(constants.FormField.MNEMONIC.name))))
      },
      signUpData => {
        val addKeyResponse = transactionAddKey.Service.post(transactionAddKey.Request(signUpData.username, signUpData.password, signUpData.mnemonic))

        def createAccount(addKeyResponse: transactionAddKey.Response): Future[String] = blockchainAccounts.Service.create(address = addKeyResponse.address, pubkey = addKeyResponse.pubkey)

        def addLogin(createAccount: String): Future[String] = masterAccounts.Service.addLogin(signUpData.username, signUpData.password, createAccount, request.lang.toString.stripPrefix("Lang(").stripSuffix(")").trim.split("_")(0))

        (for {
          addKeyResponse <- addKeyResponse
          createAccount <- createAccount(addKeyResponse)
          _ <- addLogin(createAccount)
        } yield {
          Ok(views.html.index(successes = Seq(constants.Response.SIGNED_UP)))
        }).recover {
          case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
        }
      }
    )
  }

  def noteAndVerifyMnemonic: Action[AnyContent] = Action.async { implicit request =>
    val mnemonicResponse = queriesMnemonic.Service.get()
    (for {
      mnemonicResponse <- mnemonicResponse
    } yield Ok(views.html.component.master.noteAndVerifyMnemonic(mnemonic = mnemonicResponse.body))
      ).recover {
      case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
    }
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
            _ <- utilitiesNotification.send(loginData.username, constants.Notification.LOGIN, loginData.username)
          } yield {}
        }

        def getResult(status: String, loginStateValue: LoginState): Future[Result] = {
          implicit val loginState = loginStateValue
          val contactWarnings = utilities.Contact.getWarnings(status)
          loginState.userType match {
            case constants.User.WITHOUT_LOGIN => val updateUserType = masterAccounts.Service.updateUserType(loginData.username, constants.User.USER)
              for {
                _ <- updateUserType
                result <- withUsernameToken.Ok(views.html.profile(warnings = contactWarnings))
              } yield result
            case _ => withUsernameToken.Ok(views.html.profile(warnings = contactWarnings))
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
            _ <- utilitiesNotification.send(loginState.username, constants.Notification.LOG_OUT, loginState.username)
          } yield {
            shutdownActorsAndGetResult
          }).recover {
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
          _ <- utilitiesNotification.send(accountID = emailOTPForgotPasswordData.username, notification = constants.Notification.FORGOT_PASSWORD_OTP, otp)
        } yield {
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

  def identificationForm: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val identification = masterIdentifications.Service.getOrNoneByAccountID(loginState.username)

      def getResult(identification: Option[Identification]): Future[Result] = identification match {
        case Some(identity) => withUsernameToken.Ok(views.html.component.master.identification(views.companion.master.Identification.form.fill(views.companion.master.Identification.Data(firstName = identity.firstName, lastName = identity.lastName, dateOfBirth = utilities.Date.sqlDateToUtilDate(identity.dateOfBirth), idNumber = identity.idNumber, idType = identity.idType))))
        case None => withUsernameToken.Ok(views.html.component.master.identification())
      }

      for {
        identification <- identification
        result <- getResult(identification)
      } yield result
  }

  def identification: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.Identification.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.identification(formWithErrors)))
        },
        identificationData => {
          val add = masterIdentifications.Service.insertOrUpdate(loginState.username, identificationData.firstName, identificationData.lastName, utilities.Date.utilDateToSQLDate(identificationData.dateOfBirth), identificationData.idNumber, identificationData.idType)

          def accountKYC(): Future[Option[models.master.AccountKYC]] = masterAccountKYCs.Service.get(loginState.username, constants.File.IDENTIFICATION)

          (for {
            _ <- add
            accountKYC <- accountKYC()
            result <- withUsernameToken.PartialContent(views.html.component.master.userUploadOrUpdateIdentificationView(accountKYC, constants.File.IDENTIFICATION))
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def userUploadOrUpdateIdentificationView: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val accountKYC = masterAccountKYCs.Service.get(loginState.username, constants.File.IDENTIFICATION)
      for {
        accountKYC <- accountKYC
        result <- withUsernameToken.Ok(views.html.component.master.userUploadOrUpdateIdentificationView(accountKYC, constants.File.IDENTIFICATION))
      } yield result
  }

  def userReviewIdentificationDetailsForm: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val identification = masterIdentifications.Service.getOrNoneByAccountID(loginState.username)
      val accountKYC = masterAccountKYCs.Service.get(loginState.username, constants.File.IDENTIFICATION)
      (for {
        identification <- identification
        accountKYC <- accountKYC
        result <- withUsernameToken.Ok(views.html.component.master.userReviewIdentificationDetails(identification = identification.getOrElse(throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)), accountKYC = accountKYC.getOrElse(throw new BaseException(constants.Response.NO_SUCH_FILE_EXCEPTION))))
      } yield result).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def userReviewIdentificationDetails: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.UserReviewIdentificationDetails.form.bindFromRequest().fold(
        formWithErrors => {
          val identification = masterIdentifications.Service.getOrNoneByAccountID(loginState.username)
          val accountKYC = masterAccountKYCs.Service.get(loginState.username, constants.File.IDENTIFICATION)
          (for {
            identification <- identification
            accountKYC <- accountKYC
          } yield BadRequest(views.html.component.master.userReviewIdentificationDetails(formWithErrors, identification = identification.getOrElse(throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)), accountKYC = accountKYC.getOrElse(throw new BaseException(constants.Response.NO_SUCH_FILE_EXCEPTION))))
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        },
        userReviewAddZoneRequestData => {
          val identificationFileExists = masterAccountKYCs.Service.checkFileExists(id = loginState.username, documentType = constants.File.IDENTIFICATION)

          def markIdentificationFormCompletedAndGetResult(identificationFileExists: Boolean): Future[Result] = {
            if (identificationFileExists && userReviewAddZoneRequestData.completionStatus) {
              val updateCompletionStatus = masterIdentifications.Service.markIdentificationFormCompleted(loginState.username)

              def getResult: Future[Result] = {
                withUsernameToken.Ok(views.html.profile(successes = Seq(constants.Response.IDENTIFICATION_ADDED_FOR_VERIFICATION)))
              }

              for {
                _ <- updateCompletionStatus
                //TODO: Remove this when Trulioo is integrated
                _ <- masterIdentifications.Service.markVerified(loginState.username)
                _ <- utilitiesNotification.send(loginState.username, constants.Notification.USER_REVIEWED_IDENTIFICATION_DETAILS)
                result <- getResult
              } yield result
            } else {
              val identification = masterIdentifications.Service.getOrNoneByAccountID(loginState.username)
              val accountKYC = masterAccountKYCs.Service.get(loginState.username, constants.File.IDENTIFICATION)
              for {
                identification <- identification
                accountKYC <- accountKYC
              } yield BadRequest(views.html.component.master.userReviewIdentificationDetails(identification = identification.getOrElse(throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)), accountKYC = accountKYC.getOrElse(throw new BaseException(constants.Response.NO_SUCH_FILE_EXCEPTION))))
            }
          }

          (for {
            identificationFileExists <- identificationFileExists
            result <- markIdentificationFormCompletedAndGetResult(identificationFileExists)
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }
}
