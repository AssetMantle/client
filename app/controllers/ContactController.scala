package controllers

import controllers.actions.WithLoginAction
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.master.{Email, Mobile}
import models.{master, masterTransaction}
import play.api.i18n.I18nSupport
import play.api.libs.json.{Json, OWrites}
import play.api.mvc._
import play.api.{Configuration, Logger}
import views.companion.master.{AddOrUpdateEmailAddress, AddOrUpdateMobileNumber}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ContactController @Inject()(messagesControllerComponents: MessagesControllerComponents,
                                  utilitiesNotification: utilities.Notification,
                                  masterEmails: master.Emails,
                                  masterMobiles: master.Mobiles,
                                  withLoginAction: WithLoginAction,
                                  masterTransactionEmailOTPs: masterTransaction.EmailOTPs,
                                  masterTransactionSMSOTPs: masterTransaction.SMSOTPs,
                                  withUsernameToken: WithUsernameToken)(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_CONTACT

  implicit val emailAddressWrites: OWrites[master.Email] = Json.writes[master.Email]
  implicit val mobileNumberWrites: OWrites[master.Mobile] = Json.writes[master.Mobile]

  def addOrUpdateEmailAddressForm(): Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val contact = masterEmails.Service.get(loginState.username)

      (for {
        contact <- contact
      } yield {
        contact match {
          case Some(contact) => Ok(views.html.component.master.addOrUpdateEmailAddress(views.companion.master.AddOrUpdateEmailAddress.form.fill(value = views.companion.master.AddOrUpdateEmailAddress.Data(emailAddress = contact.emailAddress))))
          case None => Ok(views.html.component.master.addOrUpdateEmailAddress())
        }
      }).recover {
        case baseException: BaseException => InternalServerError(views.html.profile(failures = Seq(baseException.failure)))
      }
  }

  def addOrUpdateEmailAddress(): Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      AddOrUpdateEmailAddress.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.addOrUpdateEmailAddress(formWithErrors)))
        },
        addOrUpdateEmailAddressData => {
          val emailAddress = masterEmails.Service.get(loginState.username)

          def addEmail: Future[String] = masterEmails.Service.create(loginState.username, addOrUpdateEmailAddressData.emailAddress)

          def updateEmail: Future[Int] = masterEmails.Service.updateEmailAddress(loginState.username, addOrUpdateEmailAddressData.emailAddress)

          def addOrUpdateEmailAddress(emailAddress: Option[Email]): Future[Unit] = {
            emailAddress match {
              case Some(email) => if (email.emailAddress != addOrUpdateEmailAddressData.emailAddress) {
                for {_ <- updateEmail} yield Unit
              } else Future(Unit)
              case None => for {_ <- addEmail} yield Unit
            }
          }

          (for {
            emailAddress <- emailAddress
            _ <- addOrUpdateEmailAddress(emailAddress)
            _ <- utilitiesNotification.send(loginState.username, constants.Notification.EMAIL_ADDRESS_UPDATED, loginState.username)
            result <- withUsernameToken.Ok(views.html.profile(successes = Seq(constants.Response.EMAIL_ADDRESS_UPDATED)))
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def addOrUpdateMobileNumberForm(): Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val contact = masterMobiles.Service.get(loginState.username)

      (for {
        contact <- contact
      } yield {
        contact match {
          case Some(contact) => Ok(views.html.component.master.addOrUpdateMobileNumber(views.companion.master.AddOrUpdateMobileNumber.form.fill(value = views.companion.master.AddOrUpdateMobileNumber.Data(mobileNumber = contact.mobileNumber.split("-")(1), countryCode = contact.mobileNumber.split("-")(0)))))
          case None => Ok(views.html.component.master.addOrUpdateMobileNumber())
        }
      }).recover {
        case baseException: BaseException => InternalServerError(views.html.profile(failures = Seq(baseException.failure)))
      }
  }

  def addOrUpdateMobileNumber(): Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      AddOrUpdateMobileNumber.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.addOrUpdateMobileNumber(formWithErrors)))
        },
        addOrUpdateMobileNumberData => {
          val mobileNumber = masterMobiles.Service.get(loginState.username)

          def addMobile: Future[String] = masterMobiles.Service.create(id = loginState.username, mobileNumber = Seq(addOrUpdateMobileNumberData.countryCode, addOrUpdateMobileNumberData.mobileNumber).mkString("-"))

          def updateMobile: Future[Int] = masterMobiles.Service.updateMobileNumber(id = loginState.username, mobileNumber = Seq(addOrUpdateMobileNumberData.countryCode, addOrUpdateMobileNumberData.mobileNumber).mkString("-"))

          def addOrUpdateMobileNumber(mobileNumber: Option[Mobile]): Future[Unit] = {
            mobileNumber match {
              case Some(mobile) => if (mobile.mobileNumber != Seq(addOrUpdateMobileNumberData.countryCode, addOrUpdateMobileNumberData.mobileNumber).mkString("-")) {
                for {_ <- updateMobile} yield Unit
              } else Future(Unit)
              case None => for {_ <- addMobile} yield Unit
            }
          }

          (for {
            mobileNumber <- mobileNumber
            _ <- addOrUpdateMobileNumber(mobileNumber)
            _ <- utilitiesNotification.send(loginState.username, constants.Notification.MOBILE_NUMBER_UPDATED, loginState.username)
            result <- withUsernameToken.Ok(views.html.profile(successes = Seq(constants.Response.MOBILE_NUMBER_UPDATED)))
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def contact: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val emailAddress = masterEmails.Service.get(loginState.username)
      val mobileNumber = masterMobiles.Service.get(loginState.username)
      (for {
        emailAddress <- emailAddress
        mobileNumber <- mobileNumber
      } yield Ok(views.html.component.master.contact(mobileNumber, emailAddress))
        ).recover {
        case _: BaseException => InternalServerError
      }
  }

  def verifyEmailAddressForm: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val emailAddress: Future[String] = masterEmails.Service.tryGetUnverifiedEmailAddress(loginState.username)

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

          def verifyEmailAddress(otpVerified: Boolean): Future[Int] = if (otpVerified) masterEmails.Service.verifyEmailAddress(loginState.username) else throw new BaseException(constants.Response.INVALID_OTP)

          (for {
            otpVerified <- verifyOTP
            _ <- verifyEmailAddress(otpVerified)
            result <- withUsernameToken.Ok(views.html.profile(successes = Seq(constants.Response.EMAIL_ADDRESS_VERIFIED)))
            _ <- utilitiesNotification.send(loginState.username, constants.Notification.EMAIL_VERIFIED, loginState.username)
          } yield {
            result
          }
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.profile(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def verifyMobileNumberForm: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val mobileNumber = masterMobiles.Service.tryGetUnverifiedMobileNumber(loginState.username)

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

          def verifyMobileNumber(otpVerified: Boolean): Future[Int] = if (otpVerified) masterMobiles.Service.verifyMobileNumber(loginState.username) else throw new BaseException(constants.Response.INVALID_OTP)

          (for {
            otpVerified <- verifyOTP
            _ <- verifyMobileNumber(otpVerified)
            result <- withUsernameToken.Ok(views.html.profile(successes = Seq(constants.Response.SUCCESS)))
            _ <- utilitiesNotification.send(loginState.username, constants.Notification.PHONE_VERIFIED, loginState.username)
          } yield {
            result
          }).recover {
            case baseException: BaseException => InternalServerError(views.html.profile(failures = Seq(baseException.failure)))
          }
        }
      )
  }
}
