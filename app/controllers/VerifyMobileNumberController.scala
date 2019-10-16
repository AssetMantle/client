package controllers

import controllers.actions.WithLoginAction
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.master
import models.master.Contact
import models.masterTransaction.SMSOTPs
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}
import views.companion.master.VerifyMobileNumber

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class VerifyMobileNumberController @Inject()(messagesControllerComponents: MessagesControllerComponents, masterAccounts: master.Accounts, smsOTPs: SMSOTPs, masterContacts: master.Contacts, withLoginAction: WithLoginAction, utilitiesNotification: utilities.Notification, withUsernameToken: WithUsernameToken)(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val module: String = constants.Module.CONTROLLERS_SMS

  private implicit val logger: Logger = Logger(this.getClass)

  def verifyMobileNumberForm: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      /*try {
        val otp = smsOTPs.Service.sendOTP(loginState.username)
        utilitiesNotification.send(accountID = loginState.username, notification = constants.Notification.VERIFY_PHONE, otp)
        withUsernameToken.Ok(views.html.component.master.verifyMobileNumber(VerifyMobileNumber.form))
      }
      catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }*/
      val otp = smsOTPs.Service.sendOTP(loginState.username)
      for{
        otp<-otp
      }yield {
        utilitiesNotification.send(accountID = loginState.username, notification = constants.Notification.VERIFY_PHONE, otp)
        withUsernameToken.Ok(views.html.component.master.verifyMobileNumber(VerifyMobileNumber.form))
      }

  }

  def verifyMobileNumber: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      VerifyMobileNumber.form.bindFromRequest().fold(
        formWithErrors => {
          Future{BadRequest(views.html.component.master.verifyMobileNumber(formWithErrors))}
        },
        verifyMobileNumberData => {
          /*try {
            smsOTPs.Service.verifyOTP(loginState.username, verifyMobileNumberData.otp)
            masterContacts.Service.verifyMobileNumber(loginState.username)
            val contact = masterContacts.Service.getContact(loginState.username).getOrElse(throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION))
            if (contact.emailAddressVerified && contact.mobileNumberVerified) {
              masterAccounts.Service.updateStatusComplete(loginState.username)
            } else {
              masterAccounts.Service.updateStatusUnverifiedEmail(loginState.username)
            }
            withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.SUCCESS)))
          }
          catch {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }*/

          val verifyOTP=smsOTPs.Service.verifyOTP(loginState.username, verifyMobileNumberData.otp)
          val verifyMobileNumber=masterContacts.Service.verifyMobileNumber(loginState.username)
          val contact=masterContacts.Service.getContact(loginState.username).map{contactVal=> contactVal.getOrElse(throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)) }
          def updateStatus(contact:Contact)={
            if (contact.emailAddressVerified && contact.mobileNumberVerified) {
              masterAccounts.Service.updateStatusComplete(loginState.username)
            } else {
              masterAccounts.Service.updateStatusUnverifiedEmail(loginState.username)
            }
          }
          (for{
            _<-verifyOTP
            _<-verifyMobileNumber
            contact<-contact
            _<-updateStatus(contact)
          }yield withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.SUCCESS)))
            ).recover{
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }
}