package controllers

import controllers.actions.{WithGenesisLoginAction, WithLoginAction, WithTraderLoginAction}
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.{master, masterTransaction}
import play.api.i18n.I18nSupport
import play.api.libs.json.{Json, OWrites}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents, Result}
import play.api.{Configuration, Logger}
import com.docusign.esign.api._
import play.api.libs.Files
import java.io.File
import java.util.List

import javax.ws.rs.core.UriBuilderException
import com.sun.jersey.core.util.Base64
import com.docusign.esign.model.Document
import com.docusign.esign.model._
import java.util.Arrays

import com.docusign.esign.client.ApiClient
import models.master.Negotiation
import models.masterTransaction.{DocusignEnvelope, NegotiationFile}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DocusignController @Inject()(messagesControllerComponents: MessagesControllerComponents,
                                   utilitiesNotification: utilities.Notification,
                                   fileResourceManager: utilities.FileResourceManager,
                                   utilitiesDocusign: utilities.Docusign,
                                   masterEmails: master.Emails,
                                   masterNegotiations: master.Negotiations,
                                   withLoginAction: WithLoginAction,
                                   withTraderLoginAction: WithTraderLoginAction,
                                   withGenesisLoginAction: WithGenesisLoginAction,
                                   masterAccounts: master.Accounts,
                                   masterTraders: master.Traders,
                                   masterTransactionNegotiationFiles: masterTransaction.NegotiationFiles,
                                   masterTransactionDocusignEnvelopes: masterTransaction.DocusignEnvelopes,
                                   withUsernameToken: WithUsernameToken)(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_CONTACT

  def send(negotiationID: String, documentType: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val traderID = masterTraders.Service.tryGetID(loginState.username)
      val negotiation = masterNegotiations.Service.tryGet(negotiationID)
      val docusign = masterTransactionDocusignEnvelopes.Service.get(negotiationID)

      def getSenderViewURL(docusignEnvelope: Option[DocusignEnvelope], traderID: String, negotiation: Negotiation) = {

        if (negotiation.sellerTraderID == traderID && negotiation.status == constants.Status.Negotiation.BUYER_ACCEPTED_ALL_NEGOTIATION_TERMS) {
          docusignEnvelope match {
            case Some(docusignEnvelope) => {
              if (docusignEnvelope.status == constants.Status.DocuSignEnvelopeStatus.CREATED) {
                Future(utilitiesDocusign.createSenderViewURL(docusignEnvelope.envelopeID))
              } else {
                throw new BaseException(constants.Response.UNAUTHORIZED)
              }
            }
            case None => {
              val buyerTraderID = masterNegotiations.Service.tryGetBuyerTraderID(negotiationID)

              def buyerTrader(traderID: String) = masterTraders.Service.tryGet(traderID)

              def emailAddress(accountID: String) = masterEmails.Service.tryGetVerifiedEmailAddress(accountID)

              val file = masterTransactionNegotiationFiles.Service.tryGet(negotiationID, documentType)

              def create(id: String, envelopeID: String) = masterTransactionDocusignEnvelopes.Service.create(id, envelopeID, documentType)

              for {
                buyerTraderID <- buyerTraderID
                buyerTrader <- buyerTrader(buyerTraderID)
                emailAddress <- emailAddress(buyerTrader.accountID)
                file <- file
                envelopeID <- utilitiesDocusign.createEnvelope(emailAddress, file, buyerTrader)
                _ <- create(negotiationID, envelopeID)
              } yield utilitiesDocusign.createSenderViewURL(envelopeID)
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
        docusign <- docusign
        senderViewURL <- getSenderViewURL(docusign, traderID, negotiation)
      } yield Ok(views.html.component.master.docusignView(senderViewURL))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.tradeRoom(negotiationID = negotiationID, failures = Seq(baseException.failure)))
      }
  }

  def docusignReturn(envelopeId: String, event: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val docusignEnvelope = masterTransactionDocusignEnvelopes.Service.tryGetByEnvelopeID(envelopeId)

      def negotiation(negotiationID: String) = masterNegotiations.Service.tryGet(negotiationID)

      def traders(traderIDs: Seq[String]) = masterTraders.Service.getTraders(traderIDs)

      def updateStatus(docusignEnvelope: DocusignEnvelope) = event match {
        case constants.View.DOCUSIGN_EVENT_SEND => {
          masterTransactionDocusignEnvelopes.Service.markSent(envelopeId)
        }
        case constants.View.DOCUSIGN_EVENT_SEND_SIGNING_COMPLETE => {

          val oldFile = masterTransactionNegotiationFiles.Service.tryGet(docusignEnvelope.id, docusignEnvelope.documentType)

          val signedDocument = Future(utilitiesDocusign.updateSignedDOcuemnt(envelopeId, docusignEnvelope.documentType))

          def updateFile(negotiationFile: NegotiationFile, newFileNme: String) = masterTransactionNegotiationFiles.Service.updateOldDocument(negotiationFile.updateFileName(newFileNme))

          def markSigningComplete = masterTransactionDocusignEnvelopes.Service.markComplete(envelopeId)

          def markContractSigned(id: String) = masterNegotiations.Service.markContractSigned(id)

          for {
            oldFile <- oldFile
            fileName <- signedDocument
            _ <- updateFile(oldFile, fileName)
            _ <- markSigningComplete
            _ <- markContractSigned(docusignEnvelope.id)
          } yield 0
        }
        case _ => Future(0)
      }

      for {
        docusignEnvelope <- docusignEnvelope
        negotiation <- negotiation(docusignEnvelope.id)
        traders <- traders(Seq(negotiation.sellerTraderID, negotiation.buyerTraderID))
        _ <- updateStatus(docusignEnvelope)
      } yield {
        actors.Service.cometActor ! actors.Message.makeCometMessage(username = traders(0).accountID, messageType = constants.Comet.NEGOTIATION, messageContent = actors.Message.Negotiation(Option(docusignEnvelope.id)))
        actors.Service.cometActor ! actors.Message.makeCometMessage(username = traders(1).accountID, messageType = constants.Comet.NEGOTIATION, messageContent = actors.Message.Negotiation(Option(docusignEnvelope.id)))
        Ok(views.html.component.master.documentSentForSignView(event))
      }
  }

  def sign(negotiationID: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val emailAddress = masterEmails.Service.tryGetVerifiedEmailAddress(loginState.username)
      val trader = masterTraders.Service.tryGetByAccountID(loginState.username)
      val negotiation = masterNegotiations.Service.tryGet(negotiationID)
      val envelopeID = masterTransactionDocusignEnvelopes.Service.tryGetEnvelopeID(negotiationID)
      (for {
        emailAddress <- emailAddress
        trader <- trader
        negotiation <- negotiation
        envelopeID <- envelopeID
      } yield {
        if (negotiation.buyerTraderID == trader.id && negotiation.status == constants.Status.Negotiation.BUYER_ACCEPTED_ALL_NEGOTIATION_TERMS) {
          Ok(views.html.component.master.docusignView(utilitiesDocusign.createRecipientView(envelopeID, emailAddress, trader)))
        } else {
          throw new BaseException(constants.Response.UNAUTHORIZED)
        }
      }
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.tradeRoom(negotiationID = negotiationID, failures = Seq(baseException.failure)))
      }
  }

  def authorization = withGenesisLoginAction.authenticated { implicit loginState =>
    implicit request =>
      Future(Redirect(utilitiesDocusign.getAuthorizationUri)).recover {
        case baseException: BaseException => InternalServerError(views.html.account(failures = Seq(baseException.failure)))
      }
  }

  def authorizationReturn(code: String) = withGenesisLoginAction.authenticated { implicit loginState =>
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