package controllers

import controllers.actions.WithLoginAction
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.{master, masterTransaction}
import models.master.{Identification, Organization}
import play.api.i18n.I18nSupport
import play.api.libs.json.{Json, OWrites}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents, Result}
import play.api.{Configuration, Logger}


import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ProfileController @Inject()(messagesControllerComponents: MessagesControllerComponents, masterAccountKYCs: master.AccountKYCs, masterContacts: master.Contacts, masterOrganizations: master.Organizations, masterIdentifications: master.Identifications, masterZones: master.Zones, masterTraders: master.Traders, withLoginAction: WithLoginAction, masterAccounts: master.Accounts, masterTransactionAddTraderRequests: masterTransaction.AddTraderRequests, withUsernameToken: WithUsernameToken)(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_PROFILE

  def identificationDocument: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val accountKYC = masterAccountKYCs.Service.get(loginState.username, constants.File.IDENTIFICATION)
      for {
        accountKYC <- accountKYC
        result <- withUsernameToken.Ok(views.html.component.master.identificationDocument(accountKYC, constants.File.IDENTIFICATION))
      } yield result
  }

  def identificationForm: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val identification = masterIdentifications.Service.getOrNoneByAccountID(loginState.username)

      def getResult(identification: Option[Identification]) = {
        identification match {
          case Some(identification) => withUsernameToken.Ok(views.html.component.master.identification(views.companion.master.Identification.form.fill(value = views.companion.master.Identification.Data(firstName = identification.firstName, lastName = identification.lastName, dateOfBirth = utilities.Date.sqlDateToUtilDate(identification.dateOfBirth), idNumber = identification.idNumber, idType = identification.idType))))
          case None => withUsernameToken.Ok(views.html.component.master.identification())
        }
      }
      (for {
        identification <- identification
        result <- getResult(identification)
      } yield result
        ).recover{
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def identification: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.Identification.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.identification(formWithErrors)))
        },
        identificationData => {
          val add = masterIdentifications.Service.insertOrUpdate(loginState.username, identificationData.firstName, identificationData.lastName, utilities.Date.utilDateToSQLDate(identificationData.dateOfBirth), identificationData.idNumber, identificationData.idType, None)
          (for {
            _ <- add
            result <- withUsernameToken.Ok(views.html.profile(successes = Seq(constants.Response.IDENTIFICATION_ADDED)))
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def identificationDetails: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val accountKYC = masterAccountKYCs.Service.get(loginState.username, constants.File.IDENTIFICATION)
      val identification = masterIdentifications.Service.getOrNoneByAccountID(loginState.username)
      (for {
        accountKYC <- accountKYC
        identification <- identification
      } yield Ok(views.html.component.master.identificationDetails(accountKYC, identification))
        ).recover{
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def organizationDetails = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      if (loginState.userType == constants.User.ZONE) Future(Ok) else {
        val identificationStatus = masterIdentifications.Service.getVerificationStatus(loginState.username).map { _.getOrElse(throw new BaseException(constants.Response.UNVERIFIED_IDENTIFICATION))}
        val contact = masterContacts.Service.getContact(loginState.username).map { _.getOrElse(throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION))}

        def getResult(contact: models.master.Contact, identificationStatus: Boolean): Future[Result] = {
          if (!contact.emailAddressVerified || !contact.mobileNumberVerified || !identificationStatus) {
            Future(Ok)
          } else {
            val optionOrganizationDetail = masterOrganizations.Service.getOrNoneByAccountID(loginState.username)

            def getOrganization(optionOrganizationDetail: Option[Organization]): Future[Option[Organization]] = optionOrganizationDetail match {
              case Some(value) => Future(Option(value))
              case None =>
                val organizationID = masterTraders.Service.getOrganizationIDByAccountID(loginState.username)

                def getOrganizationByID(organizationID: String): Future[Organization] = masterOrganizations.Service.get(organizationID)

                (for {
                  organizationID <- organizationID
                  org <- getOrganizationByID(organizationID)
                } yield Option(org)
                  ).recover {
                  case _: BaseException => None

                }
            }

            for {
              optionOrganizationDetail <- optionOrganizationDetail
              organization <- getOrganization(optionOrganizationDetail)
            } yield Ok(views.html.component.master.organization(organization))
          }
        }

        (for {
          identificationStatus <- identificationStatus
          contact <- contact
          result <- getResult(contact, identificationStatus)
        } yield result
          ).recover {
          case _: BaseException => Ok
        }
      }
  }
}