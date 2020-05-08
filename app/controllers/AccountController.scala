package controllers

import controllers.actions.{LoginState, WithLoginAction}
import controllers.logging.{WithActionAsyncLoggingFilter, WithActionLoggingFilter}
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
import views.companion.master.AddIdentification.AddressData
import views.companion.master.{Login, Logout, SignUp}
import org.slf4j.MDC

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AccountController @Inject()(
                                   utilitiesNotification: utilities.Notification,
                                   withLoginAction: WithLoginAction,
                                   withUsernameToken: WithUsernameToken,
                                   blockchainAccounts: blockchain.Accounts,
                                   blockchainAclHashes: blockchain.ACLHashes,
                                   blockchainAclAccounts: blockchain.ACLAccounts,
                                   masterAccounts: master.Accounts,
                                   masterTransactionEmailOTP: masterTransaction.EmailOTPs,
                                   masterTransactionSessionTokens: masterTransaction.SessionTokens,
                                   masterTransactionPushNotificationTokens: masterTransaction.PushNotificationTokens,
                                   queriesMnemonic: queries.GetMnemonic,
                                   transactionAddKey: transactions.AddKey,
                                   transactionForgotPassword: transactions.ForgotPassword,
                                   transactionChangePassword: transactions.ChangePassword,
                                   messagesControllerComponents: MessagesControllerComponents,
                                   masterEmails: master.Emails,
                                   masterMobiles: master.Mobiles,
                                   masterIdentifications: master.Identifications,
                                   masterAccountKYCs: master.AccountKYCs,
                                   withActionAsyncLoggingFilter: WithActionAsyncLoggingFilter,
                                   withActionLoggingFilter: WithActionLoggingFilter
                                 )
                                 (implicit
                                  executionContext: ExecutionContext,
                                  configuration: Configuration,
                                  wsClient: WSClient,
                                 ) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val module: String = constants.Module.CONTROLLERS_ACCOUNT

  private implicit val logger: Logger = Logger(this.getClass)

  private val userMnemonicShown = 3

  def signUpForm(): Action[AnyContent] = withActionLoggingFilter.next { implicit request =>
    Ok(views.html.component.master.signUp())
  }

  def signUp: Action[AnyContent] = withActionAsyncLoggingFilter.next { implicit request =>
    SignUp.form.bindFromRequest().fold(
      formWithErrors => {
        Future(BadRequest(views.html.component.master.signUp(formWithErrors)))
      },
      signUpData => {
        val mnemonics = queriesMnemonic.Service.get().map(_.body.split(" "))

        def addLogin(mnemonics: Seq[String]): Future[String] = masterAccounts.Service.addLogin(username = signUpData.username, password = signUpData.password, mnemonics = mnemonics.take(mnemonics.length - userMnemonicShown), language = request.lang)

        (for {
          mnemonics <- mnemonics
          _ <- addLogin(mnemonics)
        } yield {
          logger.info(mnemonics.toString)
          PartialContent(views.html.component.master.createWallet(username = signUpData.username, mnemonics = mnemonics.takeRight(userMnemonicShown)))
        }
          ).recover {
          case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
        }
      }
    )
  }

  def createWalletForm(username: String): Action[AnyContent] = withActionAsyncLoggingFilter.next { implicit request =>
    val bcAccountExists = blockchainAccounts.Service.checkAccountExists(username)

    def getMnemonics(bcAccountExists: Boolean): Future[Seq[String]] = if (!bcAccountExists) queriesMnemonic.Service.get().map(_.body.split(" ")) else throw new BaseException(constants.Response.UNAUTHORIZED)

    def updatePartialMnemonic(mnemonics: Seq[String], bcAccountExists: Boolean) = if (!bcAccountExists) {
      masterAccounts.Service.updatePartialMnemonic(id = username, partialMnemonic = mnemonics.take(mnemonics.length - userMnemonicShown))
    } else throw new BaseException(constants.Response.UNAUTHORIZED)

    (for {
      bcAccountExists <- bcAccountExists
      mnemonics <- getMnemonics(bcAccountExists)
      - <- updatePartialMnemonic(mnemonics, bcAccountExists)
    } yield Ok(views.html.component.master.createWallet(username = username, mnemonics = mnemonics.takeRight(userMnemonicShown)))
      ).recover {
      case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
    }
  }

  def createWallet(): Action[AnyContent] = withActionAsyncLoggingFilter.next { implicit request =>
    views.companion.master.CreateWallet.form.bindFromRequest().fold(
      formWithErrors => {
        Future(BadRequest(views.html.component.master.createWallet(formWithErrors, formWithErrors.data(constants.FormField.USERNAME.name), formWithErrors.data(constants.FormField.MNEMONICS.name).split(" "))))
      },
      createWalletData => {
        val validateUsernamePassword = masterAccounts.Service.validateUsernamePassword(username = createWalletData.username, password = createWalletData.password)
        val masterAccount = masterAccounts.Service.tryGet(createWalletData.username)

        def createAccountAndGetResult(validateUsernamePassword: Boolean, masterAccount: Account): Future[Result] = if (validateUsernamePassword) {
          val addKeyResponse = transactionAddKey.Service.post(transactionAddKey.Request(name = createWalletData.username, password = createWalletData.password, seed = Seq(masterAccount.partialMnemonic.mkString(" "), createWalletData.mnemonics).mkString(" ")))

          def createAccount(addKeyResponse: transactionAddKey.Response): Future[String] = blockchainAccounts.Service.create(address = addKeyResponse.address, username = createWalletData.username, pubkey = addKeyResponse.pubkey)

          for {
            addKeyResponse <- addKeyResponse
            _ <- createAccount(addKeyResponse)
          } yield Ok(views.html.index(successes = Seq(constants.Response.ACCOUNT_CREATED)))
        } else Future(BadRequest(views.html.component.master.createWallet(views.companion.master.CreateWallet.form.withGlobalError(constants.Response.INCORRECT_PASSWORD.message), username = createWalletData.username, mnemonics = createWalletData.mnemonics.split(" "))))

        (for {
          validateUsernamePassword <- validateUsernamePassword
          masterAccount <- masterAccount
          result <- createAccountAndGetResult(validateUsernamePassword, masterAccount)
        } yield result)
          .recover {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
      }
    )
  }

  def loginForm: Action[AnyContent] = withActionLoggingFilter.next { implicit request =>
    Ok(views.html.component.master.login())
  }

  def login: Action[AnyContent] = withActionAsyncLoggingFilter.next { implicit request =>
    Login.form.bindFromRequest().fold(
      formWithErrors => {
        Future(BadRequest(views.html.component.master.login(formWithErrors)))
      },
      loginData => {
        val validateUsernamePassword = masterAccounts.Service.validateUsernamePassword(username = loginData.username, password = loginData.password)
        val bcAccountExists = blockchainAccounts.Service.checkAccountExists(loginData.username)

        def getAccount: Future[Account] = masterAccounts.Service.tryGet(loginData.username)

        def getAddress: Future[String] = blockchainAccounts.Service.tryGetAddress(loginData.username)

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

        def getContactWarnings: Future[Seq[constants.Response.Warning]] = {
          val email = masterEmails.Service.get(loginData.username)
          val mobile = masterMobiles.Service.get(loginData.username)
          for {
            email <- email
            mobile <- mobile
          } yield utilities.Contact.getWarnings(mobile, email)
        }

        def getResult(warnings: Seq[constants.Response.Warning])(implicit loginState: LoginState): Future[Result] = {
          loginState.userType match {
            case constants.User.USER => withUsernameToken.Ok(views.html.profile(warnings = warnings))
            case _ => withUsernameToken.Ok(views.html.dashboard(warnings = warnings))
          }
        }

        def checkLoginAndGetResult(validateUsernamePassword: Boolean, bcAccountExists: Boolean): Future[Result] = {
          if (validateUsernamePassword) {
            if (bcAccountExists) {
              for {
                account <- getAccount
                address <- getAddress
                userType <- firstLoginUserTypeUpdate(account.userType)
                loginState <- getLoginState(address = address, userType = userType)
                _ <- sendNotification(loginState)
                contactWarnings <- getContactWarnings
                result <- getResult(contactWarnings)(loginState)
              } yield result
            } else {
              val mnemonics = queriesMnemonic.Service.get().map(_.body.split(" "))

              def updatePartialMnemonic(mnemonics: Seq[String]) = masterAccounts.Service.updatePartialMnemonic(id = loginData.username, partialMnemonic = mnemonics.take(mnemonics.length - userMnemonicShown))

              for {
                mnemonics <- mnemonics
                _ <- updatePartialMnemonic(mnemonics)
              } yield PartialContent(views.html.component.master.createWallet(username = loginData.username, mnemonics = mnemonics.takeRight(userMnemonicShown)))
            }
          } else {
            Future(BadRequest(views.html.component.master.login(views.companion.master.Login.form.fill(loginData).withGlobalError(constants.Response.USERNAME_OR_PASSWORD_INCORRECT.message))))
          }
        }

        (for {
          validateUsernamePassword <- validateUsernamePassword
          bcAccountExists <- bcAccountExists
          result <- checkLoginAndGetResult(validateUsernamePassword = validateUsernamePassword, bcAccountExists = bcAccountExists)
        } yield result
          ).recover {
          case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
        }
      }
    )
  }

  def logoutForm: Action[AnyContent] = withActionLoggingFilter.next { implicit request =>
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

  def changePasswordForm: Action[AnyContent] = withActionLoggingFilter.next { implicit request =>
    Ok(views.html.component.master.changePassword())
  }

  def changePassword: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.ChangePassword.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.changePassword(formWithErrors)))
        },
        changePasswordData => {
          val validateUsernamePassword = masterAccounts.Service.validateUsernamePassword(loginState.username, changePasswordData.oldPassword)

          def updateAndGetResult(validateUsernamePassword: Boolean): Future[Result] = if (validateUsernamePassword) {
            val postRequest = transactionChangePassword.Service.post(username = loginState.username, transactionChangePassword.Request(oldPassword = changePasswordData.oldPassword, newPassword = changePasswordData.newPassword, confirmNewPassword = changePasswordData.confirmNewPassword))

            def updatePassword(): Future[Int] = masterAccounts.Service.updatePassword(username = loginState.username, newPassword = changePasswordData.newPassword)

            for {
              _ <- postRequest
              _ <- updatePassword()
              result <- withUsernameToken.Ok(views.html.profile(successes = Seq(constants.Response.PASSWORD_UPDATED)))
            } yield result
          } else {
            Future(BadRequest(views.html.component.master.changePassword(views.companion.master.ChangePassword.form.fill(value = views.companion.master.ChangePassword.Data(changePasswordData.oldPassword, changePasswordData.newPassword, changePasswordData.confirmNewPassword)).withError(constants.FormField.OLD_PASSWORD.name, constants.Response.INVALID_PASSWORD.message))))
          }

          (for {
            validateUsernamePassword <- validateUsernamePassword
            result <- updateAndGetResult(validateUsernamePassword)
          } yield result).recover {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def emailOTPForgotPasswordForm: Action[AnyContent] = withActionLoggingFilter.next { implicit request =>
    Ok(views.html.component.master.emailOTPForgotPassword())
  }

  def emailOTPForgotPassword: Action[AnyContent] = withActionAsyncLoggingFilter.next { implicit request =>
    views.companion.master.EmailOTPForgotPassword.form.bindFromRequest().fold(
      formWithErrors => {
        Future(BadRequest(views.html.component.master.emailOTPForgotPassword(formWithErrors)))
      },
      emailOTPForgotPasswordData => {
        val otp = masterTransactionEmailOTP.Service.get(emailOTPForgotPasswordData.username)
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
            val partialMnemonic = masterAccounts.Service.tryGetPartialMnemonic(forgotPasswordData.username)

            def post(partialMnemonic: Seq[String]) = transactionForgotPassword.Service.post(username = forgotPasswordData.username, transactionForgotPassword.Request(seed = Seq(partialMnemonic.mkString(" "), forgotPasswordData.mnemonic).mkString(" "), newPassword = forgotPasswordData.newPassword, confirmNewPassword = forgotPasswordData.confirmNewPassword))

            def updatePassword(): Future[Int] = masterAccounts.Service.updatePassword(username = forgotPasswordData.username, newPassword = forgotPasswordData.newPassword)

            for {
              partialMnemonic <- partialMnemonic
              _ <- post(partialMnemonic)
              _ <- updatePassword()
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

  def checkUsernameAvailable(username: String): Action[AnyContent] = withActionAsyncLoggingFilter.next { implicit request =>
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

          def getAccountKYC: Future[Option[models.master.AccountKYC]] = masterAccountKYCs.Service.get(loginState.username, constants.File.AccountKYC.IDENTIFICATION)

          (for {
            _ <- add
            accountKYC <- getAccountKYC
            result <- withUsernameToken.PartialContent(views.html.component.master.userViewUploadOrUpdateIdentification(accountKYC, constants.File.AccountKYC.IDENTIFICATION))
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def userViewUploadOrUpdateIdentification: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val accountKYC = masterAccountKYCs.Service.get(loginState.username, constants.File.AccountKYC.IDENTIFICATION)
      for {
        accountKYC <- accountKYC
        result <- withUsernameToken.Ok(views.html.component.master.userViewUploadOrUpdateIdentification(accountKYC, constants.File.AccountKYC.IDENTIFICATION))
      } yield result
  }

  def userReviewIdentificationForm: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val identification = masterIdentifications.Service.get(loginState.username)
      val accountKYC = masterAccountKYCs.Service.get(loginState.username, constants.File.AccountKYC.IDENTIFICATION)
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
          val accountKYC = masterAccountKYCs.Service.get(loginState.username, constants.File.AccountKYC.IDENTIFICATION)
          (for {
            identification <- identification
            accountKYC <- accountKYC
          } yield BadRequest(views.html.component.master.userReviewIdentification(formWithErrors, identification = identification.getOrElse(throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)), accountKYC = accountKYC.getOrElse(throw new BaseException(constants.Response.NO_SUCH_FILE_EXCEPTION))))
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        },
        userReviewAddZoneRequestData => {
          val identificationFileExists = masterAccountKYCs.Service.checkFileExists(id = loginState.username, documentType = constants.File.AccountKYC.IDENTIFICATION)

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
              val accountKYC = masterAccountKYCs.Service.get(loginState.username, constants.File.AccountKYC.IDENTIFICATION)
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
