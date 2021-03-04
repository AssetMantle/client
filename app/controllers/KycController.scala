package controllers

import controllers.actions.{WithOrganizationLoginAction, WithoutLoginAction}
import controllers.results.WithUsernameToken
import exceptions.BaseException
import models.kycCheck.worldcheck.WorldCheckKYCs
import play.api.i18n.I18nSupport
import play.api.mvc.{
  AbstractController,
  Action,
  AnyContent,
  MessagesControllerComponents
}
import play.api.{Configuration, Logger}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class KycController @Inject() (
    withOrganizationLoginAction: WithOrganizationLoginAction,
    withoutLoginAction: WithoutLoginAction,
    worldCheckKYCs: WorldCheckKYCs,
    withUsernameToken: WithUsernameToken,
    messagesControllerComponents: MessagesControllerComponents
)(implicit
    executionContext: ExecutionContext,
    configuration: Configuration
) extends AbstractController(messagesControllerComponents)
    with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.KYC_CONTROLLER

  def request(): Action[AnyContent] =
    withOrganizationLoginAction.authenticated {
      implicit loginState => implicit request =>
        views.companion.master.KycRequest.form
          .bindFromRequest()
          .fold(
            formWithErrors => {
              Future(
                BadRequest(
                  views.html.component.master.kycRequest(formWithErrors)
                )
              )
            },
            kycRequestData => {
              def add(emailId: String, counterParty: String, requester: String)
                  : Future[String] = {
                worldCheckKYCs.Service.create(emailId, counterParty, requester)
              }
              (for {
                id <- add(
                  emailId = kycRequestData.eMailId,
                  counterParty = kycRequestData.counterParty,
                  requester = loginState.username
                )
                result <- withUsernameToken.PartialContent(views.html.account())
              } yield result).recover {
                case baseException: BaseException =>
                  InternalServerError(
                    views.html.account(failures = Seq(baseException.failure))
                  )
              }
            }
          )
    }

  def requestForm(): Action[AnyContent] = {
    withoutLoginAction { implicit request =>
      Ok(views.html.component.master.kycRequest())
    }
  }

  def requestServiceCheck(): Action[AnyContent] = {
    withoutLoginAction { implicit request =>
      Ok(views.html.component.master.svcCheckRequest())//To change TO MAKE A POST CALL FOR VESSEL CHECK
    }
  }
  def requestServiceCheckForm(): Action[AnyContent] = {
    withoutLoginAction { implicit request =>
      Ok(views.html.component.master.svcCheckRequest())
    }
  }

}
