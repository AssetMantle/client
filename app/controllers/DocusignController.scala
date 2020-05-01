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
      val docusign = masterTransactionDocusignEnvelopes.Service.get(negotiationID)

      def getSenderViewURL(docusignEnvelope: Option[DocusignEnvelope]) = {
        docusignEnvelope match {
          case Some(docusignEnvelope) => {
            Future(utilitiesDocusign.createSenderViewURL(docusignEnvelope.envelopeID))
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

      (for {
        docusign <- docusign
        senderViewURL <- getSenderViewURL(docusign)
      } yield Ok(views.html.component.master.docusignView(senderViewURL))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.tradeRoom(negotiationID = negotiationID, failures = Seq(baseException.failure)))
      }
  }

  def docusignReturn(envelopeId: Option[String], event: Option[String]): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      envelopeId match {
        case Some(envelopeId) => {
          event match {
            case Some(event) => {
              val updateStatus = event match {
                case constants.View.DOCUSIGN_EVENT_SEND => {
                  masterTransactionDocusignEnvelopes.Service.markSent(envelopeId)
                }
                case constants.View.DOCUSIGN_EVENT_SEND_SIGNING_COMPLETE => {
                  val docusignEnvelope = masterTransactionDocusignEnvelopes.Service.tryGetByEnvelopeID(envelopeId)

                  def oldFile(negotiationID: String) = masterTransactionNegotiationFiles.Service.tryGet(negotiationID, constants.File.CONTRACT)

                  def getSignedDocument(fileName: String) = Future(utilitiesDocusign.updateSignedDOcuemnt(envelopeId, fileName))

                  def updateFile(negotiationFile: NegotiationFile, newFileNme: String) = masterTransactionNegotiationFiles.Service.updateOldDocument(negotiationFile.updateFileName(newFileNme).updateFile(None))

                  def markSigningComplete = masterTransactionDocusignEnvelopes.Service.markComplete(envelopeId)

                  def markContractSigned(id: String) = masterNegotiations.Service.markContractSigned(id)

                  for {
                    docusignEnvelope <- docusignEnvelope
                    oldFile <- oldFile(docusignEnvelope.id)
                    fileName <- getSignedDocument(oldFile.fileName)
                    _ <- updateFile(oldFile, fileName)
                    _ <- markSigningComplete
                    _ <- markContractSigned(docusignEnvelope.id)
                  } yield 0
                }
                case _ => Future(0)
              }

              for {
                _ <- updateStatus
              } yield Ok(views.html.component.master.documentSentForSignView(event))
            }
            case None => {
              Future(Ok(views.html.component.master.documentSentForSignView(errorMessage = constants.View.UNEXPECTED_EVENT)))
            }
          }
        }
        case None => {
          Future(Ok(views.html.component.master.documentSentForSignView(errorMessage = constants.View.ENVELOPE_ID_NOT_PRESENT)))
        }

      }
  }

  def sign(negotiationID: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val emailAddress = masterEmails.Service.tryGetVerifiedEmailAddress(loginState.username)
      val trader = masterTraders.Service.tryGetByAccountID(loginState.username)
      val envelopeID = masterTransactionDocusignEnvelopes.Service.tryGetEnvelopeID(negotiationID)
      (for {
        emailAddress <- emailAddress
        trader <- trader
        envelopeID <- envelopeID
      } yield Ok(views.html.component.master.docusignView(utilitiesDocusign.createRecipientView(envelopeID, emailAddress, trader)))
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

  def authorizationReturn(code: Option[String]) = withGenesisLoginAction.authenticated { implicit loginState =>
    implicit request =>
      (code match {
        case Some(code) => {
          val updateAccessToken = Future(utilitiesDocusign.updateAccessToken(code))
          for {
            _ <- updateAccessToken
          } yield Ok(views.html.account(successes = Seq(constants.Response.DOCUSIGN_AUTHORIZED, constants.Response.ACCESS_TOKEN_UPDATED)))
        }
        case None => {
          Future(Ok(views.html.account(failures = Seq(constants.Response.AUTHORISATION_CODE_NOT_FOUND))))
        }
      }).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }
}