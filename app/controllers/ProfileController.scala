package controllers

import controllers.actions.WithLoginAction
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.master
import models.master.Identification
import play.api.i18n.I18nSupport
import play.api.libs.json.{Json, OWrites}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}


import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ProfileController @Inject()(messagesControllerComponents: MessagesControllerComponents, masterAccountKYCs: master.AccountKYCs, masterIdentifications: master.Identifications, withLoginAction: WithLoginAction, masterAccounts: master.Accounts, withUsernameToken: WithUsernameToken)(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_PROFILE

  def identificationDocument = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val accountKYC = masterAccountKYCs.Service.get(loginState.username, constants.File.IDENTIFICATION)
      for {
        accountKYC <- accountKYC
        result <- withUsernameToken.Ok(views.html.component.master.identificationDocument(accountKYC, constants.File.IDENTIFICATION))
      } yield result
  }

  def identificationForm = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val identification = masterIdentifications.Service.getByAccountID(loginState.username)

      def getResult(identification: Option[Identification]) = identification match {
        case Some(identification) => withUsernameToken.Ok(views.html.component.master.identification(views.companion.master.Identification.form.fill(value = views.companion.master.Identification.Data(firstName = identification.firstName, lastName = identification.lastName, idNumber = identification.idNumber, idType = identification.idType))))
        case None => withUsernameToken.Ok(views.html.component.master.identification())
      }

      for {
        identification <- identification
        result <- getResult(identification)
      } yield result

  }

  def identification: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.Identification.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.identification(formWithErrors)))
        },
        identificationData => {
          val add = masterIdentifications.Service.insertOrUpdate(loginState.username, identificationData.firstName, identificationData.lastName, identificationData.idNumber, identificationData.idType, None)
          (for {
            _ <- add
            result <- withUsernameToken.Ok(views.html.component.master.profile(successes = Seq(constants.Response.IDENTIFICATION_ADDED)))

              //withUsernameToken.Ok(views.html.indexVersion3(successes = Seq(constants.Response.SUCCESS)))
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def identificationDetails = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val accountKYC = masterAccountKYCs.Service.get(loginState.username, constants.File.IDENTIFICATION)
      val identification = masterIdentifications.Service.getByAccountID(loginState.username)
      for {
        accountKYC <- accountKYC
        identification <- identification
      } yield Ok(views.html.component.master.identificationDetails(accountKYC, identification))
  }

}