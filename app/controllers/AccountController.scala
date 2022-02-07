package controllers

import constants.AppConfig._
import controllers.actions._
import controllers.results.WithUsernameToken
import exceptions.BaseException
import models.common.Serializable.Address
import models.master.Identification
import models.{blockchain, master, masterTransaction}
import play.api.i18n.I18nSupport
import play.api.libs.ws.WSClient
import play.api.mvc._
import play.api.{Configuration, Logger}
import transactions.blockchain.{AddKey, ChangePassword, ForgotPassword}
import utilities.KeyStore
import views.companion.master.AddIdentification.AddressData
import views.companion.master.{SignIn, SignOut, SignUp}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AccountController @Inject()(
                                   utilitiesNotification: utilities.Notification,
                                   withLoginActionAsync: WithLoginActionAsync,
                                   withUserLoginAction: WithUserLoginAction,
                                   withUsernameToken: WithUsernameToken,
                                   blockchainAccounts: blockchain.Accounts,
                                   masterAccounts: master.Accounts,
                                   masterTransactionEmailOTP: masterTransaction.EmailOTPs,
                                   masterTransactionSessionTokens: masterTransaction.SessionTokens,
                                   masterTransactionPushNotificationTokens: masterTransaction.PushNotificationTokens,
                                   transactionAddKey: AddKey,
                                   transactionForgotPassword: ForgotPassword,
                                   transactionChangePassword: ChangePassword,
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
      Ok(views.html.component.master.account.signUp())
  }

  def signUp: Action[AnyContent] = withoutLoginActionAsync { implicit loginState =>
    implicit request =>
      SignUp.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.account.signUp(formWithErrors)))
        },
        signUpData => {
          val address = Future(utilities.Bech32.convertAccountPublicKeyToAccountAddress(pubkey = signUpData.publicKey))

          def getBCAccount(address: String) = blockchainAccounts.Service.get(address)

          def validateSignature(address: String) = Future(utilities.Blockchain.verifySecp256k1Signature(publicKey = signUpData.publicKey, data = utilities.Keplr.newArbitraryData(data = signUpData.username, signer = address).getSHA256, signature = signUpData.signature))

          def updateAccountsAndGetResult(validSignature: Boolean, optionalBCAccount: Option[blockchain.Account], address: String) = if (validSignature) {
            val logInState = optionalBCAccount.fold {
              val upsertBCAccount = blockchainAccounts.Utility.onKeplrSignUp(address = address, username = signUpData.username, publicKey = signUpData.publicKey)

              def addToMaster() = masterAccounts.Service.upsertOnKeplrSignUp(username = signUpData.username, language = request.lang)

              for {
                _ <- upsertBCAccount
                userType <- addToMaster()
              } yield LoginState(username = signUpData.username, userType = userType, address = address)
            } { bcAccount =>
              if (bcAccount.username == signUpData.username) {
                val masterAccount = masterAccounts.Service.tryGet(signUpData.username)
                for {
                  masterAccount <- masterAccount
                } yield LoginState(username = signUpData.username, userType = masterAccount.userType, address = address)
              } else throw new BaseException(constants.Response.INCORRECT_USERNAME_OR_WALLET_ADDRESS)
            }
            val contactWarnings: Future[Seq[constants.Response.Warning]] = {
              val email = masterEmails.Service.get(signUpData.username)
              val mobile = masterMobiles.Service.get(signUpData.username)
              for {
                email <- email
                mobile <- mobile
              } yield utilities.Contact.getWarnings(mobile, email)
            }

            def sendNotification = {
              val pushNotificationTokenUpdate = masterTransactionPushNotificationTokens.Service.update(id = signUpData.username, token = signUpData.pushNotificationToken)
              for {
                _ <- pushNotificationTokenUpdate
                _ <- utilitiesNotification.send(signUpData.username, constants.Notification.LOGIN, signUpData.username)()
              } yield ()
            }

            def getResult(warnings: Seq[constants.Response.Warning])(implicit loginState: LoginState): Future[Result] = withUsernameToken.Ok(views.html.assetMantle.profile(warnings = warnings))

            (for {
              logInState <- logInState
              contactWarnings <- contactWarnings
              _ <- sendNotification
              result <- getResult(contactWarnings)(logInState)
            } yield result
              ).recover {
              case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
            }
          } else Future(InternalServerError(views.html.index(failures = Seq(constants.Response.INVALID_SIGNATURE))))

          (for {
            address <- address
            bcAccount <- getBCAccount(address)
            validSignature <- validateSignature(address)
            result <- updateAccountsAndGetResult(validSignature, bcAccount, address)
          } yield result).recover {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def signInForm(): Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
      Ok(views.html.component.master.account.signIn())
  }

  def signIn: Action[AnyContent] = withoutLoginActionAsync { implicit loginState =>
    implicit request =>
      SignIn.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.account.signIn(formWithErrors)))
        },
        signInData => {
          val address = Future(utilities.Bech32.convertAccountPublicKeyToAccountAddress(pubkey = signInData.publicKey))
          val bcAccount = blockchainAccounts.Service.tryGetByUsername(signInData.username)
          val masterAccount = masterAccounts.Service.tryGet(signInData.username)

          def validateSignature(address: String) = Future(utilities.Blockchain.verifySecp256k1Signature(publicKey = signInData.publicKey, data = utilities.Keplr.newArbitraryData(data = signInData.username, signer = address).getSHA256, signature = signInData.signature))

          def getResult(validSignature: Boolean, address: String, masterAccount: master.Account) = if (validSignature) {
            val logInState = LoginState(username = masterAccount.id, userType = masterAccount.userType, address = address)
            val contactWarnings: Future[Seq[constants.Response.Warning]] = {
              val email = masterEmails.Service.get(masterAccount.id)
              val mobile = masterMobiles.Service.get(masterAccount.id)
              for {
                email <- email
                mobile <- mobile
              } yield utilities.Contact.getWarnings(mobile, email)
            }

            def sendNotification = {
              val pushNotificationTokenUpdate = masterTransactionPushNotificationTokens.Service.update(id = signInData.username, token = signInData.pushNotificationToken)
              for {
                _ <- pushNotificationTokenUpdate
                _ <- utilitiesNotification.send(signInData.username, constants.Notification.LOGIN, signInData.username)()
              } yield ()
            }

            def getResult(warnings: Seq[constants.Response.Warning])(implicit loginState: LoginState): Future[Result] = withUsernameToken.Ok(views.html.assetMantle.profile(warnings = warnings))

            (for {
              contactWarnings <- contactWarnings
              _ <- sendNotification
              result <- getResult(contactWarnings)(logInState)
            } yield result
              ).recover {
              case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
            }
          } else Future(InternalServerError(views.html.index(failures = Seq(constants.Response.INVALID_SIGNATURE))))

          (for {
            address <- address
            bcAccount <- bcAccount
            masterAccount <- masterAccount
            validSignature <- validateSignature(address)
            result <- getResult(validSignature, address, masterAccount)
          } yield result).recover {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def signOutForm: Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
      Ok(views.html.component.master.account.signOut())
  }

  def signOut: Action[AnyContent] = withLoginActionAsync { loginState =>
    implicit request =>
      SignOut.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.account.signOut(formWithErrors)))
        },
        signOutData => {
          val pushNotificationTokenDelete = if (!signOutData.receiveNotifications) masterTransactionPushNotificationTokens.Service.delete(loginState.username) else Future(0)
          val deleteSessionToken = masterTransactionSessionTokens.Service.delete(loginState.username)

          (for {
            _ <- pushNotificationTokenDelete
            _ <- deleteSessionToken
            _ <- utilitiesNotification.send(loginState.username, constants.Notification.LOG_OUT, loginState.username)()
          } yield Ok(views.html.index(successes = Seq(constants.Response.LOGGED_OUT))).withNewSession
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
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
        case Some(identity) => withUsernameToken.Ok(views.html.component.master.account.addIdentification(views.companion.master.AddIdentification.form.fill(views.companion.master.AddIdentification.Data(firstName = identity.firstName, lastName = identity.lastName, dateOfBirth = utilities.Date.sqlDateToUtilDate(identity.dateOfBirth), idNumber = identity.idNumber, idType = identity.idType, address = AddressData(identity.address.addressLine1, identity.address.addressLine2, identity.address.landmark, identity.address.city, identity.address.country, identity.address.zipCode, identity.address.phone)))))
        case None => withUsernameToken.Ok(views.html.component.master.account.addIdentification())
      }

      (for {
        identification <- identification
        result <- getResult(identification)
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.assetMantle.profile(failures = Seq(baseException.failure)))
      }
  }

  def addIdentification(): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.AddIdentification.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.account.addIdentification(formWithErrors)))
        },
        addIdentificationData => {
          val add = masterIdentifications.Service.insertOrUpdate(loginState.username, addIdentificationData.firstName, addIdentificationData.lastName, utilities.Date.utilDateToSQLDate(addIdentificationData.dateOfBirth), addIdentificationData.idNumber, addIdentificationData.idType, Address(addressLine1 = addIdentificationData.address.addressLine1, addressLine2 = addIdentificationData.address.addressLine2, landmark = addIdentificationData.address.landmark, city = addIdentificationData.address.city, country = addIdentificationData.address.country, zipCode = addIdentificationData.address.zipCode, phone = addIdentificationData.address.phone))

          def getAccountKYC: Future[Option[models.master.AccountKYC]] = masterAccountKYCs.Service.get(loginState.username, constants.File.AccountKYC.IDENTIFICATION)

          (for {
            _ <- add
            accountKYC <- getAccountKYC
            result <- withUsernameToken.PartialContent(views.html.component.master.account.userViewUploadOrUpdateIdentification(accountKYC, constants.File.AccountKYC.IDENTIFICATION))
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def userViewUploadOrUpdateIdentification: Action[AnyContent] = withLoginActionAsync { implicit loginState =>
    implicit request =>
      val accountKYC = masterAccountKYCs.Service.get(loginState.username, constants.File.AccountKYC.IDENTIFICATION)
      for {
        accountKYC <- accountKYC
        result <- withUsernameToken.Ok(views.html.component.master.account.userViewUploadOrUpdateIdentification(accountKYC, constants.File.AccountKYC.IDENTIFICATION))
      } yield result
  }

  def userReviewIdentificationForm: Action[AnyContent] = withLoginActionAsync { implicit loginState =>
    implicit request =>
      val identification = masterIdentifications.Service.get(loginState.username)
      val accountKYC = masterAccountKYCs.Service.get(loginState.username, constants.File.AccountKYC.IDENTIFICATION)
      (for {
        identification <- identification
        accountKYC <- accountKYC
        result <- withUsernameToken.Ok(views.html.component.master.account.userReviewIdentification(identification = identification.getOrElse(throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)), accountKYC = accountKYC.getOrElse(throw new BaseException(constants.Response.NO_SUCH_FILE_EXCEPTION))))
      } yield result).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def userReviewIdentification: Action[AnyContent] = withLoginActionAsync { implicit loginState =>
    implicit request =>
      views.companion.master.UserReviewIdentification.form.bindFromRequest().fold(
        formWithErrors => {
          val identification = masterIdentifications.Service.get(loginState.username)
          val accountKYC = masterAccountKYCs.Service.get(loginState.username, constants.File.AccountKYC.IDENTIFICATION)
          (for {
            identification <- identification
            accountKYC <- accountKYC
          } yield BadRequest(views.html.component.master.account.userReviewIdentification(formWithErrors, identification = identification.getOrElse(throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)), accountKYC = accountKYC.getOrElse(throw new BaseException(constants.Response.NO_SUCH_FILE_EXCEPTION))))
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
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
                result <- withUsernameToken.Ok(views.html.assetMantle.profile(successes = Seq(constants.Response.IDENTIFICATION_ADDED_FOR_VERIFICATION)))
              } yield result
            } else {
              val identification = masterIdentifications.Service.get(loginState.username)
              val accountKYC = masterAccountKYCs.Service.get(loginState.username, constants.File.AccountKYC.IDENTIFICATION)
              for {
                identification <- identification
                accountKYC <- accountKYC
              } yield BadRequest(views.html.component.master.account.userReviewIdentification(identification = identification.getOrElse(throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)), accountKYC = accountKYC.getOrElse(throw new BaseException(constants.Response.NO_SUCH_FILE_EXCEPTION))))
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
