package controllers

import controllers.actions.WithLoginAction
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.master
import models.masterTransaction.SMSOTPs
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}
import utilities.SMS
import views.companion.master.VerifyMobileNumber

import scala.concurrent.ExecutionContext

@Singleton
class VerifyMobileNumberController @Inject()(messagesControllerComponents: MessagesControllerComponents, masterAccounts: master.Accounts, smsOTPs: SMSOTPs, masterContacts: master.Contacts, withLoginAction: WithLoginAction, SMS: SMS)(implicit exec: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val module: String = constants.Module.CONTROLLERS_SMS

  private implicit val logger: Logger = Logger(this.getClass)

  def verifyMobileNumberForm: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        val otp = smsOTPs.Service.sendOTP(loginState.username)
        SMS.sendSMS(loginState.username, constants.SMS.OTP, Seq(otp))
        Ok(views.html.component.master.verifyMobileNumber(VerifyMobileNumber.form))
      }
      catch {
        case baseException: BaseException => Ok(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def verifyMobileNumber: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      VerifyMobileNumber.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.verifyMobileNumber(formWithErrors))
        },
        verifyMobileNumberData => {
          try {
            smsOTPs.Service.verifyOTP(loginState.username, verifyMobileNumberData.otp)
            masterContacts.Service.verifyMobileNumber(loginState.username)
            val contact = masterContacts.Service.getContact(loginState.username)
            if (contact.emailAddressVerified && contact.mobileNumberVerified) {
              masterAccounts.Service.updateStatusComplete(loginState.username)
            } else {
              masterAccounts.Service.updateStatusUnverifiedEmail(loginState.username)
            }
            Ok(views.html.index(successes = Seq(constants.Response.SUCCESS)))
          }
          catch {
            case baseException: BaseException => Ok(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }
}