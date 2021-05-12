package controllers

import controllers.actions._
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.common.Serializable.Coin
import models.master._
import models.{blockchain, blockchainTransaction, master, masterTransaction}
import play.api.i18n.I18nSupport
import play.api.mvc._
import play.api.{Configuration, Logger}

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
                                  masterIdentifications: master.Identifications,
                                  masterTraders: master.Traders,
                                  masterMobiles: master.Mobiles,
                                  withZoneLoginAction: WithZoneLoginAction,
                                  withOrganizationLoginAction: WithOrganizationLoginAction,
                                  withUserLoginAction: WithUserLoginAction,
                                  withGenesisLoginAction: WithGenesisLoginAction,
                                  withoutLoginAction: WithoutLoginAction,
                                  withoutLoginActionAsync: WithoutLoginActionAsync,
                                  masterAccounts: master.Accounts,
                                  transactionsSetACL: transactions.SetACL,
                                  transactionsSendCoin: transactions.SendCoin,
                                  blockchainTransactionSetACLs: blockchainTransaction.SetACLs,
                                  blockchainTransactionSendCoins: blockchainTransaction.SendCoins,
                                  blockchainAclHashes: blockchain.ACLHashes,
                                  utilitiesNotification: utilities.Notification,
                                  withUsernameToken: WithUsernameToken)(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private val denom = configuration.get[String]("blockchain.denom")

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
                _ <- utilitiesNotification.send(accountID = organization.accountID, notification = constants.Notification.ORGANIZATION_TRADER_INVITATION)()
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

              def addTrader(zoneID: String, email: Email, mobile: Mobile): Future[String] =
                if (!email.status || !mobile.status) throw new BaseException(constants.Response.CONTACT_VERIFICATION_PENDING)
                else masterTraders.Service.insertOrUpdate(zoneID, addTraderData.organizationID, loginState.username)

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
                val acl = blockchain.ACL(issueAsset = verifyTraderData.issueAsset, issueFiat = verifyTraderData.issueFiat, sendAsset = verifyTraderData.sendAsset, sendFiat = verifyTraderData.sendFiat, redeemAsset = verifyTraderData.redeemAsset, redeemFiat = verifyTraderData.redeemFiat, sellerExecuteOrder = verifyTraderData.sellerExecuteOrder, buyerExecuteOrder = verifyTraderData.buyerExecuteOrder, changeBuyerBid = verifyTraderData.changeBuyerBid, changeSellerBid = verifyTraderData.changeSellerBid, confirmBuyerBid = verifyTraderData.confirmBuyerBid, confirmSellerBid = verifyTraderData.changeSellerBid, negotiation = verifyTraderData.negotiation, releaseAsset = verifyTraderData.releaseAsset)
                val createACL = blockchainAclHashes.Service.create(acl)

                def sendCoinTransaction(aclAddress: String): Future[String] = transaction.process[blockchainTransaction.SendCoin, transactionsSendCoin.Request](
                  entity = blockchainTransaction.SendCoin(from = loginState.address, to = aclAddress, amount =Seq(Coin(denom, constants.Blockchain.DefaultTraderFaucetAmount)) , gas = verifyTraderData.gas, ticketID = "", mode = transactionMode),
                  blockchainTransactionCreate = blockchainTransactionSendCoins.Service.create,
                  request = transactionsSendCoin.Request(transactionsSendCoin.BaseReq(from = loginState.address, gas = verifyTraderData.gas), to = aclAddress, amount = Seq(transactionsSendCoin.Amount(denom, constants.Blockchain.DefaultTraderFaucetAmount)), password = verifyTraderData.password, mode = transactionMode),
                  action = transactionsSendCoin.Service.post,
                  onSuccess = blockchainTransactionSendCoins.Utility.onSuccess,
                  onFailure = blockchainTransactionSendCoins.Utility.onFailure,
                  updateTransactionHash = blockchainTransactionSendCoins.Service.updateTransactionHash
                )

                def sendSetACLTransaction(aclAddress: String, zoneID: String): Future[String] = transaction.process[blockchainTransaction.SetACL, transactionsSetACL.Request](
                  entity = blockchainTransaction.SetACL(from = loginState.address, aclAddress = aclAddress, organizationID = verifyTraderData.organizationID, zoneID = zoneID, aclHash = util.hashing.MurmurHash3.stringHash(acl.toString).toString, gas = verifyTraderData.gas, ticketID = "", mode = transactionMode),
                  blockchainTransactionCreate = blockchainTransactionSetACLs.Service.create,
                  request = transactionsSetACL.Request(transactionsSetACL.BaseReq(from = loginState.address, gas = verifyTraderData.gas), password = verifyTraderData.password, aclAddress = aclAddress, organizationID = verifyTraderData.organizationID, zoneID = zoneID, issueAsset = verifyTraderData.issueAsset.toString, issueFiat = verifyTraderData.issueFiat.toString, sendAsset = verifyTraderData.sendAsset.toString, sendFiat = verifyTraderData.sendFiat.toString, redeemAsset = verifyTraderData.redeemAsset.toString, redeemFiat = verifyTraderData.redeemFiat.toString, sellerExecuteOrder = verifyTraderData.sellerExecuteOrder.toString, buyerExecuteOrder = verifyTraderData.buyerExecuteOrder.toString, changeBuyerBid = verifyTraderData.changeBuyerBid.toString, changeSellerBid = verifyTraderData.changeSellerBid.toString, confirmBuyerBid = verifyTraderData.confirmBuyerBid.toString, confirmSellerBid = verifyTraderData.confirmSellerBid.toString, negotiation = verifyTraderData.negotiation.toString, releaseAsset = verifyTraderData.releaseAsset.toString, mode = transactionMode),
                  action = transactionsSetACL.Service.post,
                  onSuccess = blockchainTransactionSetACLs.Utility.onSuccess,
                  onFailure = blockchainTransactionSetACLs.Utility.onFailure,
                  updateTransactionHash = blockchainTransactionSetACLs.Service.updateTransactionHash
                )

                for {
                  aclAddress <- aclAddress
                  _ <- createACL
                  _ <- sendCoinTransaction(aclAddress)
                  ticketID <- sendSetACLTransaction(aclAddress = aclAddress, zoneID = trader.zoneID)
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
                val acl = blockchain.ACL(issueAsset = verifyTraderData.issueAsset, issueFiat = verifyTraderData.issueFiat, sendAsset = verifyTraderData.sendAsset, sendFiat = verifyTraderData.sendFiat, redeemAsset = verifyTraderData.redeemAsset, redeemFiat = verifyTraderData.redeemFiat, sellerExecuteOrder = verifyTraderData.sellerExecuteOrder, buyerExecuteOrder = verifyTraderData.buyerExecuteOrder, changeBuyerBid = verifyTraderData.changeBuyerBid, changeSellerBid = verifyTraderData.changeSellerBid, confirmBuyerBid = verifyTraderData.confirmBuyerBid, confirmSellerBid = verifyTraderData.changeSellerBid, negotiation = verifyTraderData.negotiation, releaseAsset = verifyTraderData.releaseAsset)
                val createACL = blockchainAclHashes.Service.create(acl)

                def sendCoinTransaction(aclAddress: String): Future[String] = transaction.process[blockchainTransaction.SendCoin, transactionsSendCoin.Request](
                  entity = blockchainTransaction.SendCoin(from = loginState.address, to = aclAddress, amount =Seq(Coin(denom, constants.Blockchain.DefaultTraderFaucetAmount)) , gas = verifyTraderData.gas, ticketID = "", mode = transactionMode),
                  blockchainTransactionCreate = blockchainTransactionSendCoins.Service.create,
                  request = transactionsSendCoin.Request(transactionsSendCoin.BaseReq(from = loginState.address, gas = verifyTraderData.gas), to = aclAddress, amount = Seq(transactionsSendCoin.Amount(denom, constants.Blockchain.DefaultTraderFaucetAmount)), password = verifyTraderData.password, mode = transactionMode),
                  action = transactionsSendCoin.Service.post,
                  onSuccess = blockchainTransactionSendCoins.Utility.onSuccess,
                  onFailure = blockchainTransactionSendCoins.Utility.onFailure,
                  updateTransactionHash = blockchainTransactionSendCoins.Service.updateTransactionHash
                )

                def sendSetACLTransaction(aclAddress: String, zoneID: String): Future[String] = transaction.process[blockchainTransaction.SetACL, transactionsSetACL.Request](
                  entity = blockchainTransaction.SetACL(from = loginState.address, aclAddress = aclAddress, organizationID = verifyTraderData.organizationID, zoneID = zoneID, aclHash = util.hashing.MurmurHash3.stringHash(acl.toString).toString, gas = verifyTraderData.gas, ticketID = "", mode = transactionMode),
                  blockchainTransactionCreate = blockchainTransactionSetACLs.Service.create,
                  request = transactionsSetACL.Request(transactionsSetACL.BaseReq(from = loginState.address, gas = verifyTraderData.gas), password = verifyTraderData.password, aclAddress = aclAddress, organizationID = verifyTraderData.organizationID, zoneID = zoneID, issueAsset = verifyTraderData.issueAsset.toString, issueFiat = verifyTraderData.issueFiat.toString, sendAsset = verifyTraderData.sendAsset.toString, sendFiat = verifyTraderData.sendFiat.toString, redeemAsset = verifyTraderData.redeemAsset.toString, redeemFiat = verifyTraderData.redeemFiat.toString, sellerExecuteOrder = verifyTraderData.sellerExecuteOrder.toString, buyerExecuteOrder = verifyTraderData.buyerExecuteOrder.toString, changeBuyerBid = verifyTraderData.changeBuyerBid.toString, changeSellerBid = verifyTraderData.changeSellerBid.toString, confirmBuyerBid = verifyTraderData.confirmBuyerBid.toString, confirmSellerBid = verifyTraderData.confirmSellerBid.toString, negotiation = verifyTraderData.negotiation.toString, releaseAsset = verifyTraderData.releaseAsset.toString, mode = transactionMode),
                  action = transactionsSetACL.Service.post,
                  onSuccess = blockchainTransactionSetACLs.Utility.onSuccess,
                  onFailure = blockchainTransactionSetACLs.Utility.onFailure,
                  updateTransactionHash = blockchainTransactionSetACLs.Service.updateTransactionHash
                )

                for {
                  aclAddress <- aclAddress
                  _ <- createACL
                  _ <- sendCoinTransaction(aclAddress)
                  _ <- sendSetACLTransaction(aclAddress, trader.zoneID)
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

  def blockchainSetACLForm: Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
    Ok(views.html.component.blockchain.setACL())
  }

  def blockchainSetACL: Action[AnyContent] = withoutLoginActionAsync { implicit loginState =>
    implicit request =>
    views.companion.blockchain.SetACL.form.bindFromRequest().fold(
      formWithErrors => {
        Future(BadRequest(views.html.component.blockchain.setACL(formWithErrors)))
      },
      setACLData => {
        val postRequest = transactionsSetACL.Service.post(transactionsSetACL.Request(transactionsSetACL.BaseReq(from = setACLData.from, gas = setACLData.gas), password = setACLData.password, aclAddress = setACLData.aclAddress, organizationID = setACLData.organizationID, zoneID = setACLData.zoneID, issueAsset = setACLData.issueAsset.toString, issueFiat = setACLData.issueFiat.toString, sendAsset = setACLData.sendAsset.toString, sendFiat = setACLData.sendFiat.toString, redeemAsset = setACLData.redeemAsset.toString, redeemFiat = setACLData.redeemFiat.toString, sellerExecuteOrder = setACLData.sellerExecuteOrder.toString, buyerExecuteOrder = setACLData.buyerExecuteOrder.toString, changeBuyerBid = setACLData.changeBuyerBid.toString, changeSellerBid = setACLData.changeSellerBid.toString, confirmBuyerBid = setACLData.confirmBuyerBid.toString, confirmSellerBid = setACLData.confirmSellerBid.toString, negotiation = setACLData.negotiation.toString, releaseAsset = setACLData.releaseAsset.toString, mode = transactionMode))
        (for {
          _ <- postRequest
        } yield Ok(views.html.index(successes = Seq(constants.Response.ACL_SET)))
          ).recover {
          case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
        }
      }
    )
  }
}