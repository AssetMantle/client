package controllers

import controllers.actions.WithLoginAction
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.master.Contacts
import models.masterTransaction.SMSOTPs
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}
import utilities.{PushNotifications, SMS}
import views.companion.master.VerifyMobileNumber

import scala.concurrent.ExecutionContext

@Singleton
class VerifyMobileNumberController @Inject()(messagesControllerComponents: MessagesControllerComponents, smsOTPs: SMSOTPs, contacts: Contacts, withLoginAction: WithLoginAction, pushNotifications: PushNotifications, SMS: SMS)(implicit exec: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val module: String = constants.Module.MASTER_ACCOUNT

  private implicit val logger: Logger = Logger(this.getClass)

  def verifyMobileNumberForm: Action[AnyContent] = withLoginAction.authenticated { username =>
    implicit request =>
      try {
        val otp = smsOTPs.Service.sendOTP(username)
        SMS.sendSMS(username, constants.SMS.OTP, Seq(otp))
        Ok(views.html.component.master.verifyMobileNumber(VerifyMobileNumber.form))
      }
      catch {
        case baseException: BaseException => Ok(views.html.index(failure = Messages(baseException.message)))
      }
  }

  def verifyMobileNumber: Action[AnyContent] = withLoginAction.authenticated { username =>
    implicit request =>
      VerifyMobileNumber.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.verifyMobileNumber(formWithErrors))
        },
        verifyMobileNumberData => {
          try {
            if (!smsOTPs.Service.verifyOTP(username, verifyMobileNumberData.otp)) throw new BaseException(constants.Error.INVALID_OTP)
            if (contacts.Service.verifyMobileNumber(username) != 1) throw new BaseException(constants.Error.MOBILE_NUMBER_NOT_FOUND)
            Ok(views.html.index(success = Messages(constants.Flash.SUCCESS)))
          }
          catch {
            case baseException: BaseException => Ok(views.html.index(failure = Messages(baseException.message)))
          }
        })
  }
}