package controllers

import controllers.actions.{WithLoginAction, WithoutLoginActionAsync}
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.{docusign, master, masterTransaction, blockchain}
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}
import models.master.Email
import models.master.AccountKYC

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DocusignController @Inject()(messagesControllerComponents: MessagesControllerComponents,
                                   utilitiesDocusign: utilities.Docusign,
                                   utilitiesNotification: utilities.Notification,
                                   masterEmails: master.Emails,
                                   withLoginAction: WithLoginAction,
                                   masterAccountKYCs: master.AccountKYCs,
                                   docusignEnvelopes: docusign.Envelopes,
                                   blockchainAccounts: blockchain.Accounts,
                                   withoutLoginActionAsync: WithoutLoginActionAsync)(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_DOCUSIGN

  def send(id: String, documentType: String): Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val verifyDocumentOwnership = true
      val envelope = docusignEnvelopes.Service.get(id, documentType)

      def getSenderViewURL(envelope: Option[docusign.Envelope]): Future[String] = {
        if (verifyDocumentOwnership) {
          envelope match {
            case Some(envelope) => if (envelope.status == constants.External.Docusign.Status.CREATED) utilitiesDocusign.createSenderViewURL(envelope.envelopeID) else throw new BaseException(constants.Response.UNAUTHORIZED)
            case None => {
              val file = masterAccountKYCs.Service.tryGet(id, documentType)
              val counterPartyAddress = "counterPartyAddress"
              val counterParty = blockchainAccounts.Service.tryGet(counterPartyAddress)

              def getBuyerEmail(accountID: String): Future[Email] = masterEmails.Service.tryGet(accountID)

              def create(negotiationID: String, envelopeID: String): Future[String] = docusignEnvelopes.Service.create(negotiationID, envelopeID, documentType)

              for {
                file <- file
                counterParty <- counterParty
                counterPartyEmail <- getBuyerEmail(counterParty.username)
                envelopeID <- utilitiesDocusign.createEnvelope(Seq(counterPartyEmail), Seq(file), Seq(counterParty))
                _ <- create(id, envelopeID)
                senderViewURL <- utilitiesDocusign.createSenderViewURL(envelopeID)
              } yield senderViewURL
            }
          }
        }
        else {
          throw new BaseException(constants.Response.UNAUTHORIZED)
        }
      }

      (for {
        envelope <- envelope
        senderViewURL <- getSenderViewURL(envelope)
      } yield Ok(views.html.component.master.docusignView(senderViewURL))
        ).recover {
        case baseException: BaseException => InternalServerError
      }
  }

  def callBack(id: String, event: String): Action[AnyContent] = withoutLoginActionAsync { implicit request =>
    val envelope = docusignEnvelopes.Service.tryGetByEnvelopeID(id)

    val account = blockchainAccounts.Service.tryGet("accountAddress")
    val counterPartyAccount = blockchainAccounts.Service.tryGet("counterPartyAccountAddress")

    def updateStatus(docusignEnvelope: docusign.Envelope, accountIDs: Seq[String]) = event match {
      case constants.External.Docusign.SEND => {
        docusignEnvelopes.Service.markSent(id)
      }
      case constants.External.Docusign.SIGNING_COMPLETE => {
        val oldFile = masterAccountKYCs.Service.tryGet(docusignEnvelope.id, docusignEnvelope.documentType)
        val signedFileNameList = utilitiesDocusign.updateSignedDocumentList(id, Seq(docusignEnvelope.documentType))

        def updateFile(accountKYC: AccountKYC, newFileNme: Seq[String]) = masterAccountKYCs.Service.updateOldDocument(accountKYC.updateFileName(newFileNme.headOption.getOrElse(throw new BaseException(constants.Response.FAILED_TO_FETCH_SIGNED_DOCUMENT))))

        def markSigningComplete = docusignEnvelopes.Service.markComplete(id)

        def updateStatus(id: String, documentType: String) = masterAccountKYCs.Service.verify(id, documentType)

        for {
          oldFile <- oldFile
          signedFileNameList <- signedFileNameList
          _ <- updateFile(oldFile, signedFileNameList)
          _ <- markSigningComplete
          _ <- updateStatus(docusignEnvelope.id, docusignEnvelope.documentType)
          _ <- utilitiesNotification.send(accountIDs(0), constants.Notification.CONTRACT_SIGNED, docusignEnvelope.id)()
          _ <- utilitiesNotification.send(accountIDs(1), constants.Notification.CONTRACT_SIGNED, docusignEnvelope.id)()
        } yield 0
      }
      case _ => throw new BaseException(constants.Response.UNEXPECTED_EVENT)
    }

    (for {
      envelope <- envelope
      account <- account
      counterPartyAccount <- counterPartyAccount
      _ <- updateStatus(envelope, Seq(account.username, counterPartyAccount.username))
    } yield {
      actors.Service.cometActor ! actors.Message.makeCometMessage(username = account.username, messageType = constants.Comet.NEGOTIATION, messageContent = actors.Message.Negotiation(envelope.id))
      actors.Service.cometActor ! actors.Message.makeCometMessage(username = counterPartyAccount.username, messageType = constants.Comet.NEGOTIATION, messageContent = actors.Message.Negotiation(envelope.id))
      Ok(views.html.component.master.docusignCallBackView(event))
    }).recover {
      case baseException: BaseException => InternalServerError(views.html.component.master.docusignCallBackView(constants.View.UNEXPECTED_EVENT))
    }
  }

  def sign(id: String, documentType: String): Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val emailAddress = masterEmails.Service.tryGetVerifiedEmailAddress(loginState.username)
      val envelope = docusignEnvelopes.Service.tryGet(id, documentType)
      val account = blockchainAccounts.Service.tryGet(loginState.address)
      (for {
        emailAddress <- emailAddress
        envelope <- envelope
        account <- account
        recepientViewURL <- utilitiesDocusign.createRecipientView(envelope.envelopeID, emailAddress, account)
      } yield {
        if (envelope.status == constants.External.Docusign.Status.SENT) {
          Ok(views.html.component.master.docusignView(recepientViewURL))
        } else {
          throw new BaseException(constants.Response.UNAUTHORIZED)
        }
      }).recover {
        case baseException: BaseException => InternalServerError
      }
  }

  def authorization: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      Future(Redirect(utilitiesDocusign.getAuthorizationURI)).recover {
        case baseException: BaseException => InternalServerError(views.html.account(address = loginState.address, failures = Seq(baseException.failure)))
      }
  }

  def authorizationCallBack(code: String): Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val updateAccessToken = Future(utilitiesDocusign.updateAccessToken(code))
      (for {
        _ <- updateAccessToken
      } yield Ok(views.html.account(address = loginState.address, successes = Seq(constants.Response.DOCUSIGN_AUTHORIZED, constants.Response.ACCESS_TOKEN_UPDATED)))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }
}