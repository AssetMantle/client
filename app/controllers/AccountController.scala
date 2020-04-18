package controllers

import controllers.actions.{LoginState, WithLoginAction, WithTraderLoginAction}
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.blockchain.ACL
import models.common.Serializable.Address
import models.master.{Account, Identification}
import models.{blockchain, master, masterTransaction}
import play.api.i18n.I18nSupport
import play.api.libs.ws.WSClient
import play.api.mvc._
import play.api.{Configuration, Logger}
import services.SFTPScheduler
import views.companion.master.AddIdentification.AddressData
import views.companion.master.{Login, Logout, SignUp}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AccountController @Inject()(
                                   utilitiesNotification: utilities.Notification,
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
        val status = masterAccounts.Service.validateLoginAndGetStatus(username = loginData.username, password = loginData.password)

        def getAccount: Future[Account] = masterAccounts.Service.tryGet(loginData.username)

        def firstLoginUserTypeUpdate(oldUserType: String): Future[String] = if (oldUserType == constants.User.WITHOUT_LOGIN) {
          val markUserTypeUser = masterAccounts.Service.markUserTypeUser(id = loginData.username)
          for {
            _ <- markUserTypeUser
          } yield constants.User.USER
        } else Future(oldUserType)

        def getLoginState(address: String, userType: String): Future[LoginState] = {
          if (userType == constants.User.TRADER) {
            val aclHash = blockchainAclAccounts.Service.getACLHash(address)

            def acl(aclHash: String): Future[ACL] = blockchainAclHashes.Service.getACL(aclHash)

            for {
              aclHash <- aclHash
              acl <- acl(aclHash)
            } yield LoginState(username = loginData.username, userType = userType, address = address, acl = Option(acl))
          } else Future(LoginState(username = loginData.username, userType = userType, address = address, acl = None))
        }

        def sendNotification(loginState: LoginState): Future[Unit] = {
          val pushNotificationTokenUpdate = masterTransactionPushNotificationTokens.Service.update(id = loginState.username, token = loginData.pushNotificationToken)
          for {
            _ <- pushNotificationTokenUpdate
            _ <- utilitiesNotification.send(loginData.username, constants.Notification.LOGIN, loginData.username)
          } yield Unit
        }

        def getResult(status: String)(implicit loginState: LoginState): Future[Result] = {
          val contactWarnings = utilities.Contact.getWarnings(status)
          loginState.userType match {
            case constants.User.USER => withUsernameToken.Ok(views.html.profile(warnings = contactWarnings))
            case _ => withUsernameToken.Ok(views.html.dashboard(warnings = contactWarnings))
          }
        }

        (for {
          status <- status
          account <- getAccount
          userType <- firstLoginUserTypeUpdate(account.userType)
          loginState <- getLoginState(address = account.accountAddress, userType = userType)
          _ <- sendNotification(loginState)
          result <- getResult(status = status)(loginState)
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
            actors.Service.Comet.shutdownUserActor(loginState.username)
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

  def addIdentificationForm: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val identification = masterIdentifications.Service.get(loginState.username)

      def getResult(identification: Option[Identification]): Future[Result] = identification match {
        case Some(identity) => withUsernameToken.Ok(views.html.component.master.addIdentification(views.companion.master.AddIdentification.form.fill(views.companion.master.AddIdentification.Data(firstName = identity.firstName, lastName = identity.lastName, dateOfBirth = utilities.Date.sqlDateToUtilDate(identity.dateOfBirth), idNumber = identity.idNumber, idType = identity.idType, address = AddressData(identity.address.addressLine1, identity.address.addressLine2, identity.address.landmark, identity.address.city, identity.address.country, identity.address.zipCode, identity.address.phone)))))
        case None => withUsernameToken.Ok(views.html.component.master.addIdentification())
      }

      (for {
        identification <- identification
        result <- getResult(identification)
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.profile(failures = Seq(baseException.failure)))
      }
  }

  def addIdentification(): Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.AddIdentification.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.addIdentification(formWithErrors)))
        },
        addIdentificationData => {
          val add = masterIdentifications.Service.insertOrUpdate(loginState.username, addIdentificationData.firstName, addIdentificationData.lastName, utilities.Date.utilDateToSQLDate(addIdentificationData.dateOfBirth), addIdentificationData.idNumber, addIdentificationData.idType, Address(addressLine1 = addIdentificationData.address.addressLine1, addressLine2 = addIdentificationData.address.addressLine2, landmark = addIdentificationData.address.landmark, city = addIdentificationData.address.city, country = addIdentificationData.address.country, zipCode = addIdentificationData.address.zipCode, phone = addIdentificationData.address.phone))

          def accountKYC(): Future[Option[models.master.AccountKYC]] = masterAccountKYCs.Service.get(loginState.username, constants.File.IDENTIFICATION)

          (for {
            _ <- add
            accountKYC <- accountKYC()
            result <- withUsernameToken.PartialContent(views.html.component.master.userViewUploadOrUpdateIdentification(accountKYC, constants.File.IDENTIFICATION))
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def userViewUploadOrUpdateIdentification: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val accountKYC = masterAccountKYCs.Service.get(loginState.username, constants.File.IDENTIFICATION)
      for {
        accountKYC <- accountKYC
        result <- withUsernameToken.Ok(views.html.component.master.userViewUploadOrUpdateIdentification(accountKYC, constants.File.IDENTIFICATION))
      } yield result
  }

  def userReviewIdentificationForm: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val identification = masterIdentifications.Service.get(loginState.username)
      val accountKYC = masterAccountKYCs.Service.get(loginState.username, constants.File.IDENTIFICATION)
      (for {
        identification <- identification
        accountKYC <- accountKYC
        result <- withUsernameToken.Ok(views.html.component.master.userReviewIdentification(identification = identification.getOrElse(throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)), accountKYC = accountKYC.getOrElse(throw new BaseException(constants.Response.NO_SUCH_FILE_EXCEPTION))))
      } yield result).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def userReviewIdentification: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.UserReviewIdentification.form.bindFromRequest().fold(
        formWithErrors => {
          val identification = masterIdentifications.Service.get(loginState.username)
          val accountKYC = masterAccountKYCs.Service.get(loginState.username, constants.File.IDENTIFICATION)
          (for {
            identification <- identification
            accountKYC <- accountKYC
          } yield BadRequest(views.html.component.master.userReviewIdentification(formWithErrors, identification = identification.getOrElse(throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)), accountKYC = accountKYC.getOrElse(throw new BaseException(constants.Response.NO_SUCH_FILE_EXCEPTION))))
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
              val identification = masterIdentifications.Service.get(loginState.username)
              val accountKYC = masterAccountKYCs.Service.get(loginState.username, constants.File.IDENTIFICATION)
              for {
                identification <- identification
                accountKYC <- accountKYC
              } yield BadRequest(views.html.component.master.userReviewIdentification(identification = identification.getOrElse(throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)), accountKYC = accountKYC.getOrElse(throw new BaseException(constants.Response.NO_SUCH_FILE_EXCEPTION))))
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
