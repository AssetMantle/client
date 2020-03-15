package controllers

import controllers.actions.WithLoginAction
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.master
import models.master.{Identification, Organization, OrganizationKYC, Trader, TraderKYC, Zone}
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents, Result}
import play.api.{Configuration, Logger}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ProfileController @Inject()(messagesControllerComponents: MessagesControllerComponents, masterOrganizationKYCs: master.OrganizationKYCs, masterTraderKYCs: master.TraderKYCs, masterAccountKYCs: master.AccountKYCs, masterContacts: master.Contacts, masterOrganizations: master.Organizations, masterIdentifications: master.Identifications, masterZones: master.Zones, masterTraders: master.Traders, withLoginAction: WithLoginAction, masterAccounts: master.Accounts, withUsernameToken: WithUsernameToken)(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_PROFILE

  def identificationDetails: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val accountKYC = masterAccountKYCs.Service.get(loginState.username, constants.File.IDENTIFICATION)
      val identification = masterIdentifications.Service.getOrNoneByAccountID(loginState.username)
      for {
        accountKYC <- accountKYC
        identification <- identification
      } yield Ok(views.html.component.master.identificationDetails(identification = identification, accountKYC = accountKYC))
  }

  def identificationForm: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val identification = masterIdentifications.Service.getOrNoneByAccountID(loginState.username)

      def getResult(identification: Option[Identification]): Future[Result] = identification match {
        case Some(identity) => withUsernameToken.Ok(views.html.component.master.identification(views.companion.master.Identification.form.fill(views.companion.master.Identification.Data(firstName = identity.firstName, lastName = identity.lastName, dateOfBirth = utilities.Date.sqlDateToUtilDate(identity.dateOfBirth), idNumber = identity.idNumber, idType = identity.idType))))
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
          val add = masterIdentifications.Service.insertOrUpdate(loginState.username, identificationData.firstName, identificationData.lastName, utilities.Date.utilDateToSQLDate(identificationData.dateOfBirth), identificationData.idNumber, identificationData.idType)

          def accountKYC(): Future[Option[models.master.AccountKYC]] = masterAccountKYCs.Service.get(loginState.username, constants.File.IDENTIFICATION)

          (for {
            _ <- add
            //TODO: Remove this when Trulioo is integrated
            _ <- masterIdentifications.Service.markVerified(loginState.username)
            accountKYC <- accountKYC()
            result <- withUsernameToken.PartialContent(views.html.component.master.userUploadOrUpdateIdentificationView(accountKYC, constants.File.IDENTIFICATION))
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def userUploadOrUpdateIdentificationView: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val accountKYC = masterAccountKYCs.Service.get(loginState.username, constants.File.IDENTIFICATION)
      for {
        accountKYC <- accountKYC
        result <- withUsernameToken.Ok(views.html.component.master.userUploadOrUpdateIdentificationView(accountKYC, constants.File.IDENTIFICATION))
      } yield result
  }

  def userReviewIdentificationDetailsForm: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val identification = masterIdentifications.Service.getOrNoneByAccountID(loginState.username)
      val accountKYC = masterAccountKYCs.Service.get(loginState.username, constants.File.IDENTIFICATION)
      (for {
        identification <- identification
        accountKYC <- accountKYC
        result <- withUsernameToken.Ok(views.html.component.master.userReviewIdentificationDetails(identification = identification.getOrElse(throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)), accountKYC = accountKYC.getOrElse(throw new BaseException(constants.Response.NO_SUCH_FILE_EXCEPTION))))
      } yield result).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def userReviewIdentificationDetails: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.UserReviewIdentificationDetails.form.bindFromRequest().fold(
        formWithErrors => {
          val identification = masterIdentifications.Service.getOrNoneByAccountID(loginState.username)
          val accountKYC = masterAccountKYCs.Service.get(loginState.username, constants.File.IDENTIFICATION)
          (for {
            identification <- identification
            accountKYC <- accountKYC
          } yield BadRequest(views.html.component.master.userReviewIdentificationDetails(formWithErrors, identification = identification.getOrElse(throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)), accountKYC = accountKYC.getOrElse(throw new BaseException(constants.Response.NO_SUCH_FILE_EXCEPTION))))
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        },
        userReviewAddZoneRequestData => {
          val identificationFileExists = masterAccountKYCs.Service.checkFileExists(id = loginState.username, documentType = constants.File.IDENTIFICATION)

          def markIdentificationFormCompletedAndGetResult(identificationFileExists: Boolean): Future[Result] = {
            if (identificationFileExists && userReviewAddZoneRequestData.completionStatus) {
              val updateCompletionStatus = masterIdentifications.Service.markIdentificationFormCompleted(loginState.username)
              for {
                _ <- updateCompletionStatus
                result <- withUsernameToken.Ok(views.html.component.master.profile(successes = Seq(constants.Response.IDENTIFICATION_ADDED_FOR_VERIFICATION)))
              } yield result
            } else {
              val identification = masterIdentifications.Service.getOrNoneByAccountID(loginState.username)
              val accountKYC = masterAccountKYCs.Service.get(loginState.username, constants.File.IDENTIFICATION)
              for {
                identification <- identification
                accountKYC <- accountKYC
              } yield BadRequest(views.html.component.master.userReviewIdentificationDetails(identification = identification.getOrElse(throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)), accountKYC = accountKYC.getOrElse(throw new BaseException(constants.Response.NO_SUCH_FILE_EXCEPTION))))
            }
          }

          (for {
            identificationFileExists <- identificationFileExists
            result <- markIdentificationFormCompletedAndGetResult(identificationFileExists)
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def userTypeDetails: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>

      def getZone(zoneID: String): Future[Zone] = masterZones.Service.get(zoneID)

      def getOrganizationByID(id: String): Future[Organization] = masterOrganizations.Service.get(id)

      def getOrganizationByAccountID(accountID: String): Future[Organization] = masterOrganizations.Service.getByAccountID(accountID)

      def getTraderByAccountID(accountID: String): Future[Trader] = masterTraders.Service.getByAccountID(accountID)

      def getOrganizationKYCs(id: String): Future[Seq[OrganizationKYC]] = masterOrganizationKYCs.Service.getAllDocuments(id)

      def getTraderKYCs(id: String): Future[Seq[TraderKYC]] = masterTraderKYCs.Service.getAllDocuments(id)

      def getTraderOrNoneByAccountID(accountID: String): Future[Option[Trader]] = masterTraders.Service.getOrNoneByAccountID(accountID)

      def getOrganizationOrNoneByAccountID(accountID: String): Future[Option[Organization]] = masterOrganizations.Service.getOrNoneByAccountID(accountID)

      val result: Future[Result] = {
        loginState.userType match {
          case constants.User.ORGANIZATION => {
            for {
              organization <- getOrganizationByAccountID(loginState.username)
              organizationZone <- getZone(organization.zoneID)
              organizationKYCs <- getOrganizationKYCs(organization.id)
              result <- withUsernameToken.Ok(views.html.component.master.userTypeDetails(organizationZone = Option(organizationZone), organization = Option(organization), organizationKYCs = organizationKYCs))
            } yield result
          }
          case constants.User.TRADER => {
            for {
              trader <- getTraderByAccountID(loginState.username)
              traderOrganization <- getOrganizationByID(trader.organizationID)
              traderZone <- getZone(trader.zoneID)
              result <- withUsernameToken.Ok(views.html.component.master.userTypeDetails(traderZone = Option(traderZone), traderOrganization = Option(traderOrganization)))
            } yield result
          }
          case constants.User.USER => {
            def identification(accountID: String): Future[Option[Identification]] = masterIdentifications.Service.getOrNoneByAccountID(accountID)

            val accountStatus: Future[String] = masterAccounts.Service.getStatus(loginState.username)

            def getZoneByOrganization(organization: Option[Organization]): Future[Option[Zone]] = if (organization.isDefined) masterZones.Service.getOrNone(organization.get.zoneID) else Future(None)

            def getOrganizationOrNoneByTrader(trader: Option[Trader]): Future[Option[Organization]] = if (trader.isDefined) masterOrganizations.Service.getOrNone(trader.get.organizationID) else Future(None)

            def getTraderKYCsByTrader(trader: Option[Trader]): Future[Seq[TraderKYC]] = if (trader.isDefined) getTraderKYCs(trader.get.id) else Future(Seq[TraderKYC]())

            def getOrganizationKYCsByOrganization(organization: Option[Organization]): Future[Seq[OrganizationKYC]] = if (organization.isDefined) getOrganizationKYCs(organization.get.id) else Future(Seq[OrganizationKYC]())

            def getUserResult(identification: Option[Identification], accountStatus: String): Future[Result] = {
              val identificationStatus = if (identification.isDefined) identification.get.verificationStatus.getOrElse(false) else false
              if (identificationStatus && accountStatus == constants.Status.Account.COMPLETE) {
                for {
                  trader <- getTraderOrNoneByAccountID(loginState.username)
                  traderOrganization <- getOrganizationOrNoneByTrader(trader)
                  traderZone <- getZoneByOrganization(traderOrganization)
                  traderKYCs <- getTraderKYCsByTrader(trader)
                  organization <- getOrganizationOrNoneByAccountID(loginState.username)
                  organizationZone <- getZoneByOrganization(organization)
                  organizationKYCs <- getOrganizationKYCsByOrganization(organization)
                  result <- withUsernameToken.Ok(views.html.component.master.userTypeDetails(identification = identification, accountStatus = accountStatus, organizationZone = organizationZone, organization = organization, organizationKYCs = organizationKYCs, traderOrganization = traderOrganization, traderZone = traderZone, trader = trader, traderKYCs = traderKYCs))
                } yield result
              } else {
                withUsernameToken.Ok(views.html.component.master.userTypeDetails(identification = identification, accountStatus = accountStatus))
              }
            }

            for {
              identification <- identification(loginState.username)
              accountStatus <- accountStatus
              result <- getUserResult(identification, accountStatus)
            } yield result

          }
          case _ => {
            withUsernameToken.Ok(views.html.component.master.userTypeDetails())
          }
        }
      }

      (for {
        result <- result
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }
}