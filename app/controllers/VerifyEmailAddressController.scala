package controllers

import constants.Security
import controllers.actions.WithLoginAction
import exceptions.{BaseException, BlockChainException}
import javax.inject.Inject
import models.master.Contacts
import models.masterTransaction.EmailOTPs
import play.api.Configuration
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import utilities.PushNotifications
import utilities.Email
import views.companion.master.VerifyEmailAddress

import scala.concurrent.ExecutionContext

class VerifyEmailAddressController @Inject()(messagesControllerComponents: MessagesControllerComponents, emailOTPs: EmailOTPs, contacts: Contacts, withLoginAction: WithLoginAction, pushNotifications: PushNotifications, email: Email)(implicit exec: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val module: String = constants.Module.MASTER_ACCOUNT

  def verifyEmailAddressForm: Action[AnyContent] = withLoginAction { implicit request =>
    val otp = emailOTPs.Service.sendOTP(request.session.get(Security.USERNAME).get)
    try {
      pushNotifications.sendNotification(request.session.get(Security.USERNAME).get, constants.Notification.OTP , Seq(otp))
      email.sendEmail(request.session.get(Security.USERNAME).get, constants.Email.OTP, Seq(otp))
      Ok(views.html.component.master.verifyEmailAddress(VerifyEmailAddress.form))
    }
    catch {
      case baseException: BaseException => Ok(views.html.index(failure = Messages(baseException.message)))
      case blockChainException: BlockChainException => Ok(views.html.index(failure = blockChainException.message))
    }
  }

  def verifyEmailAddress: Action[AnyContent] = withLoginAction { implicit request =>
    VerifyEmailAddress.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.component.master.verifyEmailAddress(formWithErrors))
      },
      verifyEmailAddressData => {
        try {
          if (!emailOTPs.Service.verifyOTP(request.session.get(Security.USERNAME).get, verifyEmailAddressData.otp)) throw new BaseException(constants.Error.INVALID_OTP)
          if (contacts.Service.verifyEmailAddress(request.session.get(Security.USERNAME).get) != 1) throw new BaseException(constants.Error.EMAIL_NOT_FOUND)
          Ok(views.html.index(success = "Email Updated"))
        }
        catch {
          case baseException: BaseException => Ok(views.html.index(failure = Messages(baseException.message)))
          case blockChainException: BlockChainException => Ok(views.html.index(failure = blockChainException.message))
        }
      })
  }
}
