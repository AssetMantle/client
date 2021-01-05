package controllers

import actors.Message.WebSocket.RemovePrivateActor
import controllers.actions._
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.common.Serializable.Address
import models.master.{Account, Identification}
import models.{blockchain, master, masterTransaction}
import play.api.i18n.I18nSupport
import play.api.libs.ws.WSClient
import play.api.mvc._
import play.api.{Configuration, Logger}
import transactions.responses.KeyResponse
import utilities.KeyStore
import views.companion.master.AddIdentification.AddressData
import views.companion.master.{ImportWallet, Login, Logout, SignUp}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AccountController @Inject()(
                                   utilitiesNotification: utilities.Notification,
                                   withLoginAction: WithLoginAction,
                                   withUserLoginAction: WithUserLoginAction,
                                   withUsernameToken: WithUsernameToken,
                                   blockchainAccounts: blockchain.Accounts,
                                   masterAccounts: master.Accounts,
                                   masterTransactionEmailOTP: masterTransaction.EmailOTPs,
                                   masterTransactionSessionTokens: masterTransaction.SessionTokens,
                                   masterTransactionPushNotificationTokens: masterTransaction.PushNotificationTokens,
                                   transactionAddKey: transactions.AddKey,
                                   transactionForgotPassword: transactions.ForgotPassword,
                                   transactionChangePassword: transactions.ChangePassword,
                                   messagesControllerComponents: MessagesControllerComponents,
                                   masterEmails: master.Emails,
                                   masterMobiles: master.Mobiles,
                                   masterIdentifications: master.Identifications,
                                   masterAccountKYCs: master.AccountKYCs,
                                   withoutLoginAction: WithoutLoginAction,
                                   withoutLoginActionAsync: WithoutLoginActionAsync,
                                   keyStore: KeyStore
                                 )
                                 (implicit
                                  executionContext: ExecutionContext,
                                  configuration: Configuration,
                                  wsClient: WSClient,
                                 ) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val module: String = constants.Module.CONTROLLERS_ACCOUNT

  private implicit val logger: Logger = Logger(this.getClass)

  def signUpForm(): Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
      Ok(views.html.component.master.signUp())
  }

  def signUp: Action[AnyContent] = withoutLoginActionAsync { implicit loginState =>
    implicit request =>
      SignUp.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.signUp(formWithErrors)))
        },
        signUpData => {
          val mnemonics = utilities.Bip39.getMnemonics()

          def addLogin(mnemonics: Seq[String]): Future[String] = masterAccounts.Service.addLogin(username = signUpData.username, password = signUpData.password, mnemonics = mnemonics.take(mnemonics.length - constants.Blockchain.MnemonicShown), language = request.lang)

          (for {
            _ <- addLogin(mnemonics)
          } yield PartialContent(views.html.component.master.createWallet(username = signUpData.username, mnemonics = mnemonics.takeRight(constants.Blockchain.MnemonicShown)))
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def createWalletForm(username: String): Action[AnyContent] = withoutLoginActionAsync { implicit loginState =>
    implicit request =>
      val blockchainAccountExists = blockchainAccounts.Service.checkAccountExists(username)

      def getMnemonics(blockchainAccountExists: Boolean): Future[Seq[String]] = if (!blockchainAccountExists) Future(utilities.Bip39.getMnemonics()) else throw new BaseException(constants.Response.UNAUTHORIZED)

      def updatePartialMnemonic(mnemonics: Seq[String], blockchainAccountExists: Boolean) = if (!blockchainAccountExists) {
        masterAccounts.Service.updatePartialMnemonic(id = username, partialMnemonic = mnemonics.take(mnemonics.length - constants.Blockchain.MnemonicShown))
      } else throw new BaseException(constants.Response.UNAUTHORIZED)

      (for {
        blockchainAccountExists <- blockchainAccountExists
        mnemonics <- getMnemonics(blockchainAccountExists)
        _ <- updatePartialMnemonic(mnemonics, blockchainAccountExists)
      } yield Ok(views.html.component.master.createWallet(username = username, mnemonics = mnemonics.takeRight(constants.Blockchain.MnemonicShown)))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.dashboard(failures = Seq(baseException.failure)))
      }
  }

  def createWallet(): Action[AnyContent] = withoutLoginActionAsync { implicit loginState =>
    implicit request =>
      views.companion.master.CreateWallet.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.createWallet(formWithErrors, formWithErrors.data(constants.FormField.USERNAME.name), formWithErrors.data(constants.FormField.MNEMONICS.name).split(" "))))
        },
        createWalletData => {
          val validateUsernamePassword = masterAccounts.Service.validateUsernamePassword(username = createWalletData.username, password = createWalletData.password)
          val masterAccount = masterAccounts.Service.tryGet(createWalletData.username)

          def createAccountAndGetResult(validateUsernamePassword: Boolean, masterAccount: Account): Future[Result] = if (validateUsernamePassword) {
            val addKeyResponse = transactionAddKey.Service.post(transactionAddKey.Request(name = createWalletData.username, mnemonic = Seq(masterAccount.partialMnemonic.getOrElse(throw new BaseException(constants.Response.MNEMONIC_NOT_FOUND)).mkString(" "), createWalletData.mnemonics).mkString(" ")))

            def createAccount(addKeyResponse: KeyResponse.Response): Future[String] = blockchainAccounts.Service.create(address = addKeyResponse.result.keyOutput.address, username = createWalletData.username, publicKey = addKeyResponse.result.keyOutput.pubkey)

            for {
              addKeyResponse <- addKeyResponse
              _ <- createAccount(addKeyResponse)
            } yield Ok(views.html.index(successes = Seq(constants.Response.ACCOUNT_CREATED)))
          } else Future(BadRequest(views.html.component.master.createWallet(views.companion.master.CreateWallet.form.withError(constants.FormField.PASSWORD.name, constants.Response.INCORRECT_PASSWORD.message), username = createWalletData.username, mnemonics = createWalletData.mnemonics.split(" "))))

          (for {
            validateUsernamePassword <- validateUsernamePassword
            masterAccount <- masterAccount
            result <- createAccountAndGetResult(validateUsernamePassword, masterAccount)
          } yield result)
            .recover {
              case baseException: BaseException => InternalServerError(views.html.dashboard(failures = Seq(baseException.failure)))
            }
        }
      )
  }

  def checkMnemonics(mnemonics: String): Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
      if (utilities.Bip39.check(mnemonics)) Ok else NoContent
  }

  def importWalletForm: Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
      Ok(views.html.component.master.importWallet())
  }

  def importWallet: Action[AnyContent] = withoutLoginActionAsync { implicit loginState =>
    implicit request =>
      ImportWallet.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.importWallet(formWithErrors)))
        },
        importWalletData => {
          val validMnemonics = utilities.Bip39.check(importWalletData.mnemonics)

          val createAccountAndGetResult: Future[Result] = if (validMnemonics && importWalletData.password == importWalletData.confirmPassword) {
            //TODO Stop creating a key on BC node
            //TODO Possible Solution: 1. Generate address locally and check, 2. Blockchain should prohibit addition of keys with same address and different name
            val addKeyResponse = transactionAddKey.Service.post(transactionAddKey.Request(name = importWalletData.username, mnemonic = importWalletData.mnemonics))

            def getOldUsername(addKeyResponse: KeyResponse.Response) = blockchainAccounts.Service.getUsername(addKeyResponse.result.keyOutput.address)

            def checkAccountExists(oldUsername: Option[String]) = oldUsername.fold(Future(false))(username => masterAccounts.Service.checkAccountExists(username))

            def checkAndUpdate(addKeyResponse: KeyResponse.Response, accountExists: Boolean) = if (!accountExists) {
              val createBCAccount: Future[Int] = blockchainAccounts.Service.insertOrUpdate(blockchain.Account(address = addKeyResponse.result.keyOutput.address, username = importWalletData.username, publicKey = addKeyResponse.result.keyOutput.pubkey, coins = Seq.empty, accountNumber = "", sequence = ""))
              val createMasterAccount = {
                val mnemonicList = importWalletData.mnemonics.split(constants.Bip39.EnglishWordList.delimiter)
                masterAccounts.Service.addLogin(username = importWalletData.username, password = importWalletData.password, language = request.lang, mnemonics = mnemonicList.take(mnemonicList.length - constants.Blockchain.MnemonicShown))
              }

              def updateBCAccount(addKeyResponse: KeyResponse.Response) = blockchainAccounts.Utility.insertOrUpdateAccountBalance(addKeyResponse.result.keyOutput.address)

              for {
                _ <- createBCAccount
                _ <- createMasterAccount
                _ <- updateBCAccount(addKeyResponse)
              } yield ()
            } else throw new BaseException(constants.Response.ACCOUNT_ALREADY_EXISTS)

            for {
              addKeyResponse <- addKeyResponse
              oldUsername <- getOldUsername(addKeyResponse)
              accountExists <- checkAccountExists(oldUsername)
              _ <- checkAndUpdate(addKeyResponse, accountExists)
            } yield Ok(views.html.dashboard(successes = Seq(constants.Response.ACCOUNT_CREATED)))
          } else if (!validMnemonics) Future(BadRequest(views.html.component.master.importWallet(ImportWallet.form.fill(importWalletData).withGlobalError(constants.Response.INVALID_MNEMONICS.message))))
          else Future(BadRequest(views.html.component.master.importWallet(ImportWallet.form.fill(importWalletData).withGlobalError(constants.Response.PASSWORDS_DO_NOT_MATCH.message))))

          (for {
            result <- createAccountAndGetResult
          } yield result).recover {
            case baseException: BaseException => InternalServerError(views.html.dashboard(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def loginForm: Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
      Ok(views.html.component.master.login())
  }

  def login: Action[AnyContent] = withoutLoginActionAsync { implicit loginState =>
    implicit request =>
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

          def sendNotification(username: String): Future[Unit] = {
            val pushNotificationTokenUpdate = masterTransactionPushNotificationTokens.Service.update(id = username, token = loginData.pushNotificationToken)
            for {
              _ <- pushNotificationTokenUpdate
              _ <- utilitiesNotification.send(loginData.username, constants.Notification.LOGIN, loginData.username)()
            } yield ()
          }

          def getContactWarnings: Future[Seq[constants.Response.Warning]] = {
            val email = masterEmails.Service.get(loginData.username)
            val mobile = masterMobiles.Service.get(loginData.username)
            for {
              email <- email
              mobile <- mobile
            } yield utilities.Contact.getWarnings(mobile, email)
          }

          def getResult(warnings: Seq[constants.Response.Warning])(implicit loginState: LoginState): Future[Result] = withUsernameToken.Ok(views.html.account(warnings = warnings))

          def checkLoginAndGetResult(validateUsernamePassword: Boolean, bcAccountExists: Boolean): Future[Result] = {
            if (validateUsernamePassword) {
              if (bcAccountExists) {
                for {
                  account <- getAccount
                  address <- getAddress
                  userType <- firstLoginUserTypeUpdate(account.userType)
                  _ <- sendNotification(loginData.username)
                  contactWarnings <- getContactWarnings
                  result <- getResult(contactWarnings)(LoginState(username = loginData.username, userType = userType, address = address))
                } yield result
              } else {
                val mnemonics = utilities.Bip39.getMnemonics()

                def updatePartialMnemonic(mnemonics: Seq[String]) = masterAccounts.Service.updatePartialMnemonic(id = loginData.username, partialMnemonic = mnemonics.take(mnemonics.length - constants.Blockchain.MnemonicShown))

                for {
                  _ <- updatePartialMnemonic(mnemonics)
                } yield PartialContent(views.html.component.master.createWallet(username = loginData.username, mnemonics = mnemonics.takeRight(constants.Blockchain.MnemonicShown)))
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
            case baseException: BaseException => InternalServerError(views.html.dashboard(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def logoutForm: Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
      Ok(views.html.component.master.logout())
  }

  def logout: Action[AnyContent] = withLoginAction.authenticated { loginState =>
    implicit request =>
      Logout.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.logout(formWithErrors)))
        },
        logoutData => {
          val pushNotificationTokenDelete = if (!logoutData.receiveNotifications) masterTransactionPushNotificationTokens.Service.delete(loginState.username) else Future(0)

          def transactionSessionTokensDelete: Future[Int] = masterTransactionSessionTokens.Service.delete(loginState.username)

          def shutdownActorsAndGetResult = {
            actors.Service.appWebSocketActor ! RemovePrivateActor(loginState.username)
            Ok(views.html.dashboard(successes = Seq(constants.Response.LOGGED_OUT))).withNewSession
          }

          (for {
            _ <- pushNotificationTokenDelete
            _ <- transactionSessionTokensDelete
            _ <- utilitiesNotification.send(loginState.username, constants.Notification.LOG_OUT, loginState.username)()
          } yield shutdownActorsAndGetResult
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.dashboard(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def changePasswordForm: Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
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
            case baseException: BaseException => InternalServerError(views.html.dashboard(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def emailOTPForgotPasswordForm: Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
      Ok(views.html.component.master.emailOTPForgotPassword())
  }

  def emailOTPForgotPassword: Action[AnyContent] = withoutLoginActionAsync { implicit loginState =>
    implicit request =>
      views.companion.master.EmailOTPForgotPassword.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.emailOTPForgotPassword(formWithErrors)))
        },
        emailOTPForgotPasswordData => {
          val otp = masterTransactionEmailOTP.Service.get(emailOTPForgotPasswordData.username)
          (for {
            otp <- otp
            _ <- utilitiesNotification.send(accountID = emailOTPForgotPasswordData.username, notification = constants.Notification.FORGOT_PASSWORD_OTP, otp)()
          } yield PartialContent(views.html.component.master.forgotPassword(views.companion.master.ForgotPassword.form, emailOTPForgotPasswordData.username))
          ).recover {
            case baseException: BaseException => InternalServerError(views.html.dashboard(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def forgotPasswordForm(username: String): Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
      Ok(views.html.component.master.forgotPassword(username = username))
  }

  def forgotPassword: Action[AnyContent] = withoutLoginActionAsync { implicit loginState =>
    implicit request =>
      views.companion.master.ForgotPassword.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.forgotPassword(formWithErrors, formWithErrors(constants.FormField.USERNAME.name).value.getOrElse(""))))
        },
        forgotPasswordData => {
          val validOTP = masterTransactionEmailOTP.Service.verifyOTP(forgotPasswordData.username, forgotPasswordData.otp)

          def updateAndGetResult(validOTP: Boolean): Future[Result] = {
            if (validOTP) {
              val account = masterAccounts.Service.tryGet(forgotPasswordData.username)

              def post(partialMnemonic: Option[Seq[String]]) = partialMnemonic.fold(throw new BaseException(constants.Response.MNEMONIC_NOT_FOUND))(x => transactionForgotPassword.Service.post(username = forgotPasswordData.username, transactionForgotPassword.Request(seed = Seq(x.mkString(" "), forgotPasswordData.mnemonic).mkString(" "), newPassword = forgotPasswordData.newPassword, confirmNewPassword = forgotPasswordData.confirmNewPassword)))

              def updatePassword(): Future[Int] = masterAccounts.Service.updatePassword(username = forgotPasswordData.username, newPassword = forgotPasswordData.newPassword)

              for {
                account <- account
                _ <- post(account.partialMnemonic)
                _ <- updatePassword()
              } yield Ok(views.html.dashboard(successes = Seq(constants.Response.PASSWORD_UPDATED)))
            } else {
              Future(BadRequest(views.html.dashboard(failures = Seq(constants.Response.INVALID_PASSWORD))))
            }
          }

          (for {
            validOTP <- validOTP
            result <- updateAndGetResult(validOTP)
          } yield result).recover {
            case baseException: BaseException => InternalServerError(views.html.dashboard(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def checkUsernameAvailable(username: String): Action[AnyContent] = withoutLoginActionAsync { implicit loginState =>
    implicit request =>
      val checkUsernameAvailable = masterAccounts.Service.checkUsernameAvailable(username)
      for {
        checkUsernameAvailable <- checkUsernameAvailable
      } yield if (checkUsernameAvailable) Ok else NoContent
  }

  def addIdentificationForm(): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
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

  def addIdentification(): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
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
            case baseException: BaseException => InternalServerError(views.html.dashboard(failures = Seq(baseException.failure)))
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
        case baseException: BaseException => InternalServerError(views.html.dashboard(failures = Seq(baseException.failure)))
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
            case baseException: BaseException => InternalServerError(views.html.dashboard(failures = Seq(baseException.failure)))
          }
        },
        userReviewAddZoneRequestData => {
          val identificationFileExists = masterAccountKYCs.Service.checkFileExists(id = loginState.username, documentType = constants.File.AccountKYC.IDENTIFICATION)

          def markIdentificationFormCompletedAndGetResult(identificationFileExists: Boolean): Future[Result] = {
            if (identificationFileExists && userReviewAddZoneRequestData.completionStatus) {
              val updateCompletionStatus = masterIdentifications.Service.markIdentificationFormCompleted(loginState.username)
              for {
                _ <- updateCompletionStatus
                //TODO: Remove this when Trulioo is integrated
                _ <- masterIdentifications.Service.markVerified(loginState.username)
                _ <- utilitiesNotification.send(loginState.username, constants.Notification.USER_REVIEWED_IDENTIFICATION_DETAILS)()
                result <- withUsernameToken.Ok(views.html.profile(successes = Seq(constants.Response.IDENTIFICATION_ADDED_FOR_VERIFICATION)))
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
            case baseException: BaseException => InternalServerError(views.html.dashboard(failures = Seq(baseException.failure)))
          }
        }
      )
  }
}
