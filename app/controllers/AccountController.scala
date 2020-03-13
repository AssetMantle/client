package controllers

import actors.ShutdownActor
import controllers.actions.{LoginState, WithLoginAction, WithTraderLoginAction}
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.blockchain.{ACL, ACLAccount}
import models.master.{Organization, TraderRelation, Zone}
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
                                 )
                                 (implicit
                                  executionContext: ExecutionContext,
                                  configuration: Configuration,
                                  wsClient: WSClient,
                                 ) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val module: String = constants.Module.CONTROLLER_ACCOUNT

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
          Ok(views.html.indexVersion3(successes = Seq(constants.Response.SIGNED_UP)))
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
          } yield utilitiesNotification.send(loginData.username, constants.Notification.LOGIN, loginData.username)
        }

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
            case constants.User.USER => withUsernameToken.Ok(views.html.userIndex(warnings = contactWarnings))
            case constants.User.UNKNOWN => withUsernameToken.Ok(views.html.anonymousIndex(warnings = contactWarnings))
            case constants.User.WITHOUT_LOGIN => val updateUserType = masterAccounts.Service.updateUserType(loginData.username, constants.User.USER)
              for {
                _ <- updateUserType
                result <- withUsernameToken.Ok(views.html.anonymousIndex(warnings = contactWarnings))
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
          case baseException: BaseException => InternalServerError(views.html.indexVersion3(failures = Seq(baseException.failure)))
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
            Ok(views.html.indexVersion3(successes = Seq(constants.Response.LOGGED_OUT))).withNewSession
          }

          (for {
            _ <- pushNotificationTokenDelete
            _ <- transactionSessionTokensDelete
          } yield shutdownActorsAndGetResult).recover {
            case baseException: BaseException => InternalServerError(views.html.indexVersion3(failures = Seq(baseException.failure)))
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

  def traderRelationList(): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.traderRelationList(acceptedTraderRelationListRoute = utilities.String.getJsRouteFunction(routes.javascript.AccountController.acceptedTraderRelationList), pendingTraderRelationListRoute = utilities.String.getJsRouteFunction(routes.javascript.AccountController.pendingTraderRelationList)))
  }

  def acceptedTraderRelationList(): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val acceptedTraderRelations: Future[Seq[TraderRelation]] = masterTraderRelations.Service.getAllAcceptedTraderRelation(loginState.username)
      (for {
        acceptedTraderRelations <- acceptedTraderRelations
        result <- withUsernameToken.Ok(views.html.component.master.acceptedTraderRelationList(acceptedTraderRelationList = acceptedTraderRelations))
      } yield result).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def pendingTraderRelationList(): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val receivedPendingTraderRelations: Future[Seq[TraderRelation]] = masterTraderRelations.Service.getAllReceivedPendingTraderRelation(loginState.username)
      val sentPendingTraderRelations: Future[Seq[TraderRelation]] = masterTraderRelations.Service.getAllSentPendingTraderRelation(loginState.username)
      (for {
        receivedPendingTraderRelations <- receivedPendingTraderRelations
        sentPendingTraderRelations <- sentPendingTraderRelations
        result <- withUsernameToken.Ok(views.html.component.master.pendingTraderRelationList(sentPendingTraderRelations = sentPendingTraderRelations, receivedPendingTraderRelations = receivedPendingTraderRelations))
      } yield result).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def acceptedTraderRelation(toID: String): Action[AnyContent] = withTraderLoginAction.authenticated {
    implicit loginState =>
      implicit request =>
        val traderName = masterTraders.Service.getTraderName(toID)
        val organizationID = masterTraders.Service.getOrganizationID(toID)

        def getOrganizationName(organizationID: String): Future[String] = masterOrganizations.Service.getNameByID(organizationID)

        (for {
          traderName <- traderName
          organizationID <- organizationID
          organizationName <- getOrganizationName(organizationID)
          result <- Future(Ok(views.html.component.master.acceptedTraderRelation(toID = toID, traderName = traderName, organizationName = organizationName)))
        } yield result).recover {
          case baseException: BaseException => InternalServerError(views.html.component.master.profile(failures = Seq(baseException.failure)))
        }
  }

  def pendingSentTraderRelation(toID: String): Action[AnyContent] = withTraderLoginAction.authenticated {
    implicit loginState =>
      implicit request =>
        val traderName = masterTraders.Service.getTraderName(toID)
        val organizationID = masterTraders.Service.getOrganizationID(toID)

        def getOrganizationName(organizationID: String): Future[String] = masterOrganizations.Service.getNameByID(organizationID)

        (for {
          traderName <- traderName
          organizationID <- organizationID
          organizationName <- getOrganizationName(organizationID)
          result <- Future(Ok(views.html.component.master.pendingSentTraderRelation(toID = toID, traderName = traderName, organizationName = organizationName)))
        } yield result).recover {
          case baseException: BaseException => InternalServerError(views.html.component.master.profile(failures = Seq(baseException.failure)))
        }
  }

  def pendingReceivedTraderRelation(toID: String): Action[AnyContent] = withTraderLoginAction.authenticated {
    implicit loginState =>
      implicit request =>
        val traderRelation = masterTraderRelations.Service.get(fromID = loginState.username, toID = toID)
        val traderName = masterTraders.Service.getTraderName(toID)
        val organizationID = masterTraders.Service.getOrganizationID(toID)

        def getOrganizationName(organizationID: String): Future[String] = masterOrganizations.Service.getNameByID(organizationID)

        (for {
          traderRelation <- traderRelation
          traderName <- traderName
          organizationID <- organizationID
          organizationName <- getOrganizationName(organizationID)
          result <- Future(Ok(views.html.component.master.pendingReceivedTraderRelation(traderRelation = traderRelation, traderName = traderName, organizationName = organizationName)))
        } yield result).recover {
          case baseException: BaseException => InternalServerError(views.html.component.master.profile(failures = Seq(baseException.failure)))
        }
  }

  def traderRelationRequestForm(): Action[AnyContent] = Action {
    implicit request =>
      Ok(views.html.component.master.traderRelationRequest())
  }

  def traderRelationRequest(): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.TraderRelationRequest.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.traderRelationRequest(formWithErrors)))
        },
        traderRelationRequestData => {
          val status = masterTraders.Service.getVerificationStatus(traderRelationRequestData.to)

          def create(status: Boolean): Future[String] = if (status) {
            masterTraderRelations.Service.create(fromID = loginState.username, toID = traderRelationRequestData.to)
          } else {
            throw new BaseException(constants.Response.UNVERIFIED_TRADER)
          }

          (for {
            status <- status
            _ <- create(status)
            result <- withUsernameToken.Ok(views.html.component.master.profile(successes = Seq(constants.Response.COUNTERPARTY_REQUEST_SEND_SUCCESSFULLY)))
          } yield result).recover {
            case baseException: BaseException => InternalServerError(views.html.component.master.profile(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def acceptOrRejectTraderRelationForm(fromID: String, toID: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val traderRelation = masterTraderRelations.Service.get(fromID = fromID, toID = toID)
      (for {
        traderRelation <- traderRelation
        result <- Future(Ok(views.html.component.master.acceptOrRejectTraderRelation(traderRelation = traderRelation)))
      } yield result).recover {
        case baseException: BaseException => InternalServerError(views.html.component.master.profile(failures = Seq(baseException.failure)))
      }
  }

  def acceptOrRejectTraderRelation(): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.AcceptOrRejectTraderRelation.form.bindFromRequest().fold(
        formWithErrors => {
          val traderRelation = masterTraderRelations.Service.get(fromID = formWithErrors(constants.FormField.FROM.name).value.get, toID = formWithErrors(constants.FormField.TO.name).value.get)
          (for {
            traderRelation <- traderRelation
          } yield BadRequest(views.html.component.master.acceptOrRejectTraderRelation(formWithErrors, traderRelation = traderRelation))
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        },
        acceptOrRejectTraderRelationData => {
          val updateStatus: Future[Int] = if (acceptOrRejectTraderRelationData.status) {
            masterTraderRelations.Service.markAccepted(fromID = acceptOrRejectTraderRelationData.from, toID = acceptOrRejectTraderRelationData.to)
          } else {
            masterTraderRelations.Service.markRejected(fromID = acceptOrRejectTraderRelationData.from, toID = acceptOrRejectTraderRelationData.to)
          }
          val traderRelation = masterTraderRelations.Service.get(fromID = acceptOrRejectTraderRelationData.from, toID = acceptOrRejectTraderRelationData.to)
          (for {
            _ <- updateStatus
            traderRelation <- traderRelation
            result <- withUsernameToken.PartialContent(views.html.component.master.acceptOrRejectTraderRelation(traderRelation = traderRelation))
          } yield result).recover {
            case baseException: BaseException => InternalServerError(views.html.component.master.profile(failures = Seq(baseException.failure)))
          }
        }
      )
  }
}
