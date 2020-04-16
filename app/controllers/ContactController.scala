package controllers

import controllers.actions.WithLoginAction
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.{master, masterTransaction}
import models.master.Contact
import play.api.i18n.I18nSupport
import play.api.libs.json.{Json, OWrites}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents, Result}
import play.api.{Configuration, Logger}
import views.companion.master.AddOrUpdateContact

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ContactController @Inject()(messagesControllerComponents: MessagesControllerComponents,
                                  utilitiesNotification: utilities.Notification,
                                  masterContacts: master.Contacts,
                                  withLoginAction: WithLoginAction,
                                  masterAccounts: master.Accounts,
                                  masterTransactionEmailOTPs: masterTransaction.EmailOTPs,
                                  masterTransactionSMSOTPs: masterTransaction.SMSOTPs,
                                  withUsernameToken: WithUsernameToken)(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_CONTACT

  implicit val contactWrites: OWrites[master.Contact] = Json.writes[master.Contact]

  def addOrUpdateForm(): Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val contact = masterContacts.Service.get(loginState.username)

      (for {
        contact <- contact
      } yield {
        contact match {
          case Some(contact) => Ok(views.html.component.master.addOrUpdateContact(views.companion.master.AddOrUpdateContact.form.fill(value = views.companion.master.AddOrUpdateContact.Data(emailAddress = contact.emailAddress, mobileNumber = contact.mobileNumber.takeRight(10), countryCode = contact.mobileNumber.dropRight(10)))))
          case None => Ok(views.html.component.master.addOrUpdateContact())
        }
      }).recover {
        case baseException: BaseException => InternalServerError(views.html.profile(failures = Seq(baseException.failure)))
      }
  }

  def addOrUpdate(): Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      AddOrUpdateContact.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.addOrUpdateContact(formWithErrors)))
        },
        addOrUpdateData => {
          val contact = masterContacts.Service.get(loginState.username)
          val emailAddressAccount = masterContacts.Service.getEmailAddressAccount(addOrUpdateData.emailAddress)
          val mobileNumberAccount = masterContacts.Service.getMobileNumberAccount(Seq(addOrUpdateData.countryCode, addOrUpdateData.mobileNumber).mkString(""))

          def addOrUpdateContact(contact: Option[Contact], emailAddressAccount: Option[String], mobileNumberAccount: Option[String]): Future[Unit] = {

            def createContact: Future[String] = masterContacts.Service.create(id = loginState.username, mobileNumber = Seq(addOrUpdateData.countryCode, addOrUpdateData.mobileNumber).mkString(""), emailAddress = addOrUpdateData.emailAddress)

            def updateEmailAddress(): Future[Int] = if (emailAddressAccount.isEmpty) {
              masterContacts.Service.updateEmailAddress(id = loginState.username, emailAddress = addOrUpdateData.emailAddress)
            } else if (emailAddressAccount.get != loginState.username) throw new BaseException(constants.Response.EMAIL_ADDRESS_TAKEN) else Future(0)

            def updateMobileNumber(): Future[Int] = if (mobileNumberAccount.isEmpty) {
              masterContacts.Service.updateMobileNumber(id = loginState.username, mobileNumber = addOrUpdateData.countryCode + addOrUpdateData.mobileNumber)
            } else if (mobileNumberAccount.get != loginState.username) throw new BaseException(constants.Response.MOBILE_NUMBER_TAKEN) else Future(0)

            if (contact.isEmpty) {
              for {
                _ <- createContact
              } yield Unit
            } else {
              for {
                _ <- updateEmailAddress()
                _ <- updateMobileNumber()
              } yield Unit
            }
          }

          (for {
            contact <- contact
            emailAddressAccount <- emailAddressAccount
            mobileNumberAccount <- mobileNumberAccount
            _ <- addOrUpdateContact(contact = contact, emailAddressAccount = emailAddressAccount, mobileNumberAccount = mobileNumberAccount)
            _ <- utilitiesNotification.send(loginState.username, constants.Notification.CONTACT_UPDATED, loginState.username)
            result <- withUsernameToken.Ok(views.html.profile(successes = Seq(constants.Response.CONTACT_UPDATED)))
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def contact: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val contact = masterContacts.Service.get(loginState.username)
      (for {
        contact <- contact
      } yield Ok(views.html.component.master.contact(contact))
        ).recover {
        case _: BaseException => InternalServerError
      }
  }

  def verifyEmailAddressForm: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val emailAddress: Future[String] = masterContacts.Service.tryGetUnverifiedEmailAddress(loginState.username)

      def getOTP: Future[String] = masterTransactionEmailOTPs.Service.get(loginState.username)

      def sendOTPAndGetResult(emailAddress: String, otp: String): Future[Result] = {
        utilitiesNotification.sendEmailToEmailAddress(fromAccountID = loginState.username, emailAddress = emailAddress, email = constants.Notification.VERIFY_EMAIL.email.getOrElse(throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)), otp)
        withUsernameToken.Ok(views.html.component.master.verifyEmailAddress())
      }

      (for {
        emailAddress <- emailAddress
        otp <- getOTP
        result <- sendOTPAndGetResult(emailAddress = emailAddress, otp = otp)
      } yield result).recover {
        case baseException: BaseException => InternalServerError(views.html.profile(failures = Seq(baseException.failure)))
      }
  }

  def verifyEmailAddress: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.VerifyEmailAddress.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.verifyEmailAddress(formWithErrors)))
        },
        verifyEmailAddressData => {
          val verifyOTP = masterTransactionEmailOTPs.Service.verifyOTP(id = loginState.username, otp = verifyEmailAddressData.otp)

          def verifyEmailAddress(otpVerified: Boolean): Future[Int] = if (otpVerified) masterContacts.Service.verifyEmailAddress(loginState.username) else throw new BaseException(constants.Response.INVALID_OTP)

          (for {
            otpVerified <- verifyOTP
            _ <- verifyEmailAddress(otpVerified)
            result <- withUsernameToken.Ok(views.html.profile(successes = Seq(constants.Response.EMAIL_ADDRESS_VERIFIED)))
            _ <- utilitiesNotification.send(loginState.username, constants.Notification.EMAIL_VERIFIED, loginState.username)
          } yield {
            result
          }
            ).recover {
            case baseException: BaseException => if (baseException.failure == constants.Response.INVALID_OTP) BadRequest(views.html.component.master.verifyEmailAddress(views.companion.master.VerifyEmailAddress.form.withError(constants.FormField.OTP.name, constants.Response.INVALID_OTP.message))) else InternalServerError(views.html.profile(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def verifyMobileNumberForm: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val mobileNumber = masterContacts.Service.tryGetUnverifiedMobileNumber(loginState.username)

      def getOTP: Future[String] = masterTransactionSMSOTPs.Service.get(loginState.username)

      (for {
        mobileNumber <- mobileNumber
        otp <- getOTP
        _ <- utilitiesNotification.sendSMSToMobileNumber(fromAccountID = loginState.username, mobileNumber = mobileNumber, sms = constants.Notification.VERIFY_PHONE.sms.getOrElse(throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)), otp)
        result <- withUsernameToken.Ok(views.html.component.master.verifyMobileNumber())
      } yield result).recover {
        case baseException: BaseException => InternalServerError(views.html.profile(failures = Seq(baseException.failure)))
      }
  }

  def verifyMobileNumber: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.VerifyMobileNumber.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.verifyMobileNumber(formWithErrors)))
        },
        verifyMobileNumberData => {
          val verifyOTP = masterTransactionSMSOTPs.Service.verifyOTP(loginState.username, verifyMobileNumberData.otp)

          def verifyMobileNumber(otpVerified: Boolean): Future[Int] = if (otpVerified) masterContacts.Service.verifyMobileNumber(loginState.username) else throw new BaseException(constants.Response.INVALID_OTP)

          (for {
            otpVerified <- verifyOTP
            _ <- verifyMobileNumber(otpVerified)
            result <- withUsernameToken.Ok(views.html.profile(successes = Seq(constants.Response.SUCCESS)))
            _ <- utilitiesNotification.send(loginState.username, constants.Notification.PHONE_VERIFIED, loginState.username)
          } yield {
            result
          }).recover {
            case baseException: BaseException => if (baseException.failure == constants.Response.INVALID_OTP) BadRequest(views.html.component.master.verifyMobileNumber(views.companion.master.VerifyMobileNumber.form.withError(constants.FormField.OTP.name, constants.Response.INVALID_OTP.message))) else InternalServerError(views.html.profile(failures = Seq(baseException.failure)))
          }
        }
      )
  }
}