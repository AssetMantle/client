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
import masterTransaction.requestState

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ProfileController @Inject()(messagesControllerComponents: MessagesControllerComponents, masterAccountKYCs: master.AccountKYCs, masterContacts: master.Contacts, masterOrganizations: master.Organizations, masterIdentifications: master.Identifications, masterZones: master.Zones, masterTraders: master.Traders, withLoginAction: WithLoginAction, masterAccounts: master.Accounts, masterTransactionAddTraderRequests: masterTransaction.AddTraderRequests, withUsernameToken: WithUsernameToken)(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

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
            result <- withUsernameToken.Ok(views.html.component.master.profile(successes = Seq(constants.Response.IDENTIFICATION_ADDED)))
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
        val identificationStatus = masterIdentifications.Service.getVerificationStatus(loginState.username).map { status => if (status.isEmpty) throw new BaseException(constants.Response.UNVERIFIED_IDENTIFICATION) else status.get }
        val contact = masterContacts.Service.getContact(loginState.username).map { contact => contact.getOrElse(throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)) }

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

  def organizationViewAllTraderList() = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val organizationID = masterOrganizations.Service.getID(loginState.username)

      val allTraderRequests = masterTransactionAddTraderRequests.Service.getAllTraderRequests(loginState.username)

      def allTraders(organizationID: String) = masterTraders.Service.getTradersListInOrganization(organizationID)

      def allTraderContacts(accountIDs: Seq[String]) = masterContacts.Service.getContacts(accountIDs)

      def requestedStatuses(traderEmails: Seq[String], traderRequests: Seq[models.masterTransaction.AddTraderRequest]) = Future.sequence {
        traderRequests.filterNot(traderRequest => traderEmails.contains(traderRequest.emailAddress)).map { traderRequest =>
          val contact = masterContacts.Service.contactByEmailId(traderRequest.emailAddress)
          (for {
            _ <- contact
          } yield requestState(traderRequest.emailAddress, traderRequest.name, constants.Status.TraderStatus.SIGNED_UP)
            ).recover {
            case _: BaseException => requestState(traderRequest.emailAddress, traderRequest.name,  constants.Status.TraderStatus.NOT_SIGNED_UP)
          }
        }
      }

      for {
        organizationID <- organizationID
        allTraders <- allTraders(organizationID)
        allTraderRequests <- allTraderRequests
        allTraderContacts <- allTraderContacts(allTraders.map(_.accountID))
        requestedStatuses <- requestedStatuses(allTraderContacts.map(_.emailAddress), allTraderRequests)
      } yield {
        val traderStatuses: Seq[requestState] = allTraders.map { trader =>
          val contact = allTraderContacts.find(_.id == trader.accountID).get
          if (!trader.completionStatus) {
            requestState(contact.emailAddress, trader.name,  constants.Status.TraderStatus.ADD_TRADER_FORM_INCOMPLETE)
          } else if (trader.completionStatus && trader.verificationStatus.isEmpty) {
            requestState(contact.emailAddress, trader.name,  constants.Status.TraderStatus.REQUESTED)
          } else if (trader.completionStatus && trader.verificationStatus.get) {
            requestState(contact.emailAddress, trader.name,  constants.Status.TraderStatus.APPROVED)
          } else {
            requestState(contact.emailAddress, trader.name,  constants.Status.TraderStatus.REJECTED)
          }
        }

        Ok(views.html.component.master.organizationTraderList(traderStatuses ++ requestedStatuses))
      }
  }

  def viewTraderRequests()=withLoginAction.authenticated{implicit loginState =>
    implicit request =>
    val allTraderRequests = masterTransactionAddTraderRequests.Service.getAllTraderRequests(loginState.username)
    def getContacts(emailIDs:Seq[String]) = masterContacts.Service.getContactsByEmailID(emailIDs)
    for{
      allTraderRequests<-allTraderRequests
      contacts<- getContacts(allTraderRequests.map(_.emailAddress))
    }yield {

      val statuses=allTraderRequests.map{traderRequest=> if(contacts.map(_.emailAddress) contains  traderRequest.emailAddress) {
        requestState(traderRequest.emailAddress, traderRequest.name, constants.Status.TraderStatus.SIGNED_UP)
      }
      else {
        requestState(traderRequest.emailAddress, traderRequest.name, constants.Status.TraderStatus.NOT_SIGNED_UP)
      }
      }
      Ok(views.html.component.master.organizationTraderList(statuses))
    }
  }

}