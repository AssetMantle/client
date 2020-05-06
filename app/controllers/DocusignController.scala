package controllers

import controllers.actions.{WithGenesisLoginAction, WithLoginAction, WithTraderLoginAction}
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.{docusign, master, masterTransaction}
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}
import models.master.{Negotiation, Trader}
import models.masterTransaction.NegotiationFile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DocusignController @Inject()(messagesControllerComponents: MessagesControllerComponents,
                                   utilitiesDocusign: utilities.Docusign,
                                   utilitiesNotification: utilities.Notification,
                                   masterEmails: master.Emails,
                                   masterNegotiations: master.Negotiations,
                                   withTraderLoginAction: WithTraderLoginAction,
                                   withGenesisLoginAction: WithGenesisLoginAction,
                                   masterTraders: master.Traders,
                                   masterTransactionNegotiationFiles: masterTransaction.NegotiationFiles,
                                   docusignEnvelopes: docusign.Envelopes)(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_DOCUSIGN

  def send(negotiationID: String, documentType: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val traderID = masterTraders.Service.tryGetID(loginState.username)
      val negotiation = masterNegotiations.Service.tryGet(negotiationID)
      val envelope = docusignEnvelopes.Service.get(negotiationID, documentType)

      def getSenderViewURL(envelope: Option[docusign.Envelope], traderID: String, negotiation: Negotiation): Future[String] = {
        if (negotiation.sellerTraderID == traderID && negotiation.status == constants.Status.Negotiation.BUYER_ACCEPTED_ALL_NEGOTIATION_TERMS) {
          envelope match {
            case Some(envelope) => if (envelope.status == constants.Status.DocuSignEnvelope.CREATED) utilitiesDocusign.createSenderViewURL(envelope.envelopeID) else throw new BaseException(constants.Response.UNAUTHORIZED)
            case None => {
              val file = masterTransactionNegotiationFiles.Service.tryGet(negotiationID, documentType)
              val buyerTrader = masterTraders.Service.tryGet(negotiation.buyerTraderID)

              def getBuyerEmailAddress(accountID: String): Future[String] = masterEmails.Service.tryGetVerifiedEmailAddress(accountID)

              def create(negotiationID: String, envelopeID: String): Future[String] = docusignEnvelopes.Service.create(negotiationID, envelopeID, documentType)

              for {
                file <- file
                buyerTrader <- buyerTrader
                buyerEmailAddress <- getBuyerEmailAddress(buyerTrader.accountID)
                envelopeID <- utilitiesDocusign.createEnvelope(buyerEmailAddress, file, buyerTrader)
                _ <- create(negotiationID, envelopeID)
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
        traderID <- traderID
        negotiation <- negotiation
        envelope <- envelope
        senderViewURL <- getSenderViewURL(envelope, traderID, negotiation)
      } yield Ok(views.html.component.master.docusignView(senderViewURL))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.tradeRoom(negotiationID = negotiationID, failures = Seq(baseException.failure)))
      }
  }

  def callBack(envelopeId: String, event: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val envelope = docusignEnvelopes.Service.tryGetByEnvelopeID(envelopeId)

      def getNegotiation(negotiationID: String): Future[Negotiation] = masterNegotiations.Service.tryGet(negotiationID)

      def getTraders(traderIDs: Seq[String]): Future[Seq[Trader]] = masterTraders.Service.getTraders(traderIDs)

      def updateStatus(docusignEnvelope: docusign.Envelope, traderAccountIDs: Seq[String]) = event match {
        case constants.Docusign.SEND => {
          docusignEnvelopes.Service.markSent(envelopeId)
        }
        case constants.Docusign.SIGNING_COMPLETE => {
          val oldFile = masterTransactionNegotiationFiles.Service.tryGet(docusignEnvelope.id, docusignEnvelope.documentType)
          val signedDocument = utilitiesDocusign.updateSignedDocuemnt(envelopeId, docusignEnvelope.documentType)

          def updateFile(negotiationFile: NegotiationFile, newFileNme: String) = masterTransactionNegotiationFiles.Service.updateOldDocument(negotiationFile.updateFileName(newFileNme))

          def markSigningComplete = docusignEnvelopes.Service.markComplete(envelopeId)

          def markContractSigned(negotiationID: String) = masterNegotiations.Service.markContractSigned(negotiationID)

          def updateStatus(negotiationID: String, documentType: String) = masterTransactionNegotiationFiles.Service.accept(negotiationID, documentType)

          for {
            oldFile <- oldFile
            fileName <- signedDocument
            _ <- updateFile(oldFile, fileName)
            _ <- markSigningComplete
            _ <- markContractSigned(docusignEnvelope.id)
            _ <- updateStatus(docusignEnvelope.id, docusignEnvelope.documentType)
            _ <- utilitiesNotification.send(traderAccountIDs(0), constants.Notification.CONTRACT_SIGNED, docusignEnvelope.id)
            _ <- utilitiesNotification.send(traderAccountIDs(1), constants.Notification.CONTRACT_SIGNED, docusignEnvelope.id)
          } yield 0
        }
        case _ => Future(0)
      }

      (for {
        envelope <- envelope
        negotiation <- getNegotiation(envelope.id)
        traders <- getTraders(Seq(negotiation.sellerTraderID, negotiation.buyerTraderID))
        _ <- updateStatus(envelope, traders.map(_.accountID))
      } yield {
        actors.Service.cometActor ! actors.Message.makeCometMessage(username = traders(0).accountID, messageType = constants.Comet.NEGOTIATION, messageContent = actors.Message.Negotiation(Option(envelope.id)))
        actors.Service.cometActor ! actors.Message.makeCometMessage(username = traders(1).accountID, messageType = constants.Comet.NEGOTIATION, messageContent = actors.Message.Negotiation(Option(envelope.id)))
        Ok(views.html.component.master.docusignCallBackView(event))
      }).recover {
        case _: BaseException => InternalServerError(views.html.component.master.docusignCallBackView(constants.View.UNEXPECTED_EVENT))
      }
  }

  def sign(negotiationID: String, documentType: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val emailAddress = masterEmails.Service.tryGetVerifiedEmailAddress(loginState.username)
      val trader = masterTraders.Service.tryGetByAccountID(loginState.username)
      val negotiation = masterNegotiations.Service.tryGet(negotiationID)
      val envelope = docusignEnvelopes.Service.tryGet(negotiationID, documentType)
      (for {
        emailAddress <- emailAddress
        trader <- trader
        negotiation <- negotiation
        envelope <- envelope
        recepientViewURL <- utilitiesDocusign.createRecipientView(envelope.envelopeID, emailAddress, trader)
      } yield {
        if (negotiation.buyerTraderID == trader.id && negotiation.status == constants.Status.Negotiation.BUYER_ACCEPTED_ALL_NEGOTIATION_TERMS && envelope.status == constants.Status.DocuSignEnvelope.SENT) {
          Ok(views.html.component.master.docusignView(recepientViewURL))
        } else {
          throw new BaseException(constants.Response.UNAUTHORIZED)
        }
      }).recover {
        case baseException: BaseException => InternalServerError(views.html.tradeRoom(negotiationID = negotiationID, failures = Seq(baseException.failure)))
      }
  }

  def authorization = withGenesisLoginAction.authenticated { implicit loginState =>
    implicit request =>
      Future(Redirect(utilitiesDocusign.getAuthorizationURI)).recover {
        case baseException: BaseException => InternalServerError(views.html.account(failures = Seq(baseException.failure)))
      }
  }

  def authorizationCallBack(code: String) = withGenesisLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val updateAccessToken = Future(utilitiesDocusign.updateAccessToken(code))
      (for {
        _ <- updateAccessToken
      } yield Ok(views.html.account(successes = Seq(constants.Response.DOCUSIGN_AUTHORIZED, constants.Response.ACCESS_TOKEN_UPDATED)))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }
}