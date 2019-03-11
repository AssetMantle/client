package controllers

import constants.Security
import controllers.actions.WithLoginAction
import exceptions.{BaseException, BlockChainException}
import javax.inject.Inject
import models.master.Contacts
import models.masterTransaction.SMSOTPs
import play.api.Configuration
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import utilities.PushNotifications
import views.companion.master.VerifyMobileNumber

import scala.concurrent.ExecutionContext

class VerifyMobileNumberController @Inject()(messagesControllerComponents: MessagesControllerComponents, smsOTPs: SMSOTPs, contacts: Contacts, withLoginAction: WithLoginAction, pushNotifications: PushNotifications)(implicit exec: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val module: String = constants.Module.MASTER_ACCOUNT

  def verifyMobileNumberForm: Action[AnyContent] = withLoginAction { implicit request =>
    val sendOTPRequest = smsOTPs.Service.sendOTP(request.session.get(Security.USERNAME).get)
    try {
      pushNotifications.Push.sendNotification(request.session.get(Security.USERNAME).get, "sendOTP", sendOTPRequest)
      Ok(views.html.component.master.verifyMobileNumber(VerifyMobileNumber.form))
    }
    catch {
      case baseException: BaseException => Ok(views.html.index(failure = Messages(baseException.message)))
      case blockChainException: BlockChainException => Ok(views.html.index(failure = blockChainException.message))
    }
  }

  def verifyMobileNumber: Action[AnyContent] = withLoginAction { implicit request =>
    VerifyMobileNumber.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.component.master.verifyMobileNumber(formWithErrors))
      },
      verifyMobileNumberData => {
        try {
          if (!smsOTPs.Service.verifyOTP(request.session.get(Security.USERNAME).get, verifyMobileNumberData.otp)) throw new BaseException(constants.Error.INVALID_OTP)
          if (contacts.Service.verifyMobileNumber(request.session.get(Security.USERNAME).get) != 1) throw new BaseException(constants.Error.MOBILE_NUMBER_NOT_FOUND)
          Ok(views.html.index(success = "Mobile Number Updated"))
        }
        catch {
          case baseException: BaseException => Ok(views.html.index(failure = Messages(baseException.message)))
          case blockChainException: BlockChainException => Ok(views.html.index(failure = blockChainException.message))
        }
      })
  }
}