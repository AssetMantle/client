package controllers

import controllers.actions.{WithGenesisLoginAction, WithLoginAction, WithTraderLoginAction}
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.{master, masterTransaction}
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}
import models.master.Negotiation
import models.masterTransaction.{DocusignEnvelope, NegotiationFile}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DocusignController @Inject()(messagesControllerComponents: MessagesControllerComponents,
                                   utilitiesDocusign: utilities.Docusign,
                                   masterEmails: master.Emails,
                                   masterNegotiations: master.Negotiations,
                                   withTraderLoginAction: WithTraderLoginAction,
                                   withGenesisLoginAction: WithGenesisLoginAction,
                                   masterTraders: master.Traders,
                                   masterTransactionNegotiationFiles: masterTransaction.NegotiationFiles,
                                   masterTransactionDocusignEnvelopes: masterTransaction.DocusignEnvelopes)(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_CONTACT

  def send(negotiationID: String, documentType: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val traderID = masterTraders.Service.tryGetID(loginState.username)
      val negotiation = masterNegotiations.Service.tryGet(negotiationID)
      val docusignEnvelope = masterTransactionDocusignEnvelopes.Service.get(negotiationID, documentType)

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
              val file = masterTransactionNegotiationFiles.Service.tryGet(negotiationID, documentType)
              val buyerTrader = masterTraders.Service.tryGet(negotiation.buyerTraderID)

              def getBuyerEmailAddress(accountID: String) = masterEmails.Service.tryGetVerifiedEmailAddress(accountID)

              def createDocusignEnvelope(negotiationID: String, envelopeID: String) = masterTransactionDocusignEnvelopes.Service.create(negotiationID, envelopeID, documentType)

              for {
                file <- file
                buyerTrader <- buyerTrader
                buyerEmailAddress <- getBuyerEmailAddress(buyerTrader.accountID)
                envelopeID <- utilitiesDocusign.createEnvelope(buyerEmailAddress, file, buyerTrader)
                _ <- createDocusignEnvelope(negotiationID, envelopeID)
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
        docusignEnvelope <- docusignEnvelope
        senderViewURL <- getSenderViewURL(docusignEnvelope, traderID, negotiation)
      } yield Ok(views.html.component.master.docusignView(senderViewURL))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.tradeRoom(negotiationID = negotiationID, failures = Seq(baseException.failure)))
      }
  }

  def docusignReturn(envelopeId: String, event: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val docusignEnvelope = masterTransactionDocusignEnvelopes.Service.tryGetByEnvelopeID(envelopeId)

      def getNegotiation(negotiationID: String) = masterNegotiations.Service.tryGet(negotiationID)

      def getTraders(traderIDs: Seq[String]) = masterTraders.Service.getTraders(traderIDs)

      def updateStatus(docusignEnvelope: DocusignEnvelope) = event match {
        case constants.View.DOCUSIGN_EVENT_SEND => {
          masterTransactionDocusignEnvelopes.Service.markSent(envelopeId)
        }
        case constants.View.DOCUSIGN_EVENT_SEND_SIGNING_COMPLETE => {
          val oldFile = masterTransactionNegotiationFiles.Service.tryGet(docusignEnvelope.id, docusignEnvelope.documentType)
          val signedDocument = Future(utilitiesDocusign.updateSignedDocuemnt(envelopeId, docusignEnvelope.documentType))

          def updateFile(negotiationFile: NegotiationFile, newFileNme: String) = masterTransactionNegotiationFiles.Service.updateOldDocument(negotiationFile.updateFileName(newFileNme))

          def markSigningComplete = masterTransactionDocusignEnvelopes.Service.markComplete(envelopeId)

          def markContractSigned(negotiationID: String) = masterNegotiations.Service.markContractSigned(negotiationID)

          def updateStatus(negotiationID: String, documentType: String) = masterTransactionNegotiationFiles.Service.accept(negotiationID, documentType)

          for {
            oldFile <- oldFile
            fileName <- signedDocument
            _ <- updateFile(oldFile, fileName)
            _ <- markSigningComplete
            _ <- markContractSigned(docusignEnvelope.id)
            _ <- updateStatus(docusignEnvelope.id, docusignEnvelope.documentType)
          } yield 0
        }
        case _ => Future(0)
      }

      (for {
        docusignEnvelope <- docusignEnvelope
        negotiation <- getNegotiation(docusignEnvelope.id)
        traders <- getTraders(Seq(negotiation.sellerTraderID, negotiation.buyerTraderID))
        _ <- updateStatus(docusignEnvelope)
      } yield {
        actors.Service.cometActor ! actors.Message.makeCometMessage(username = traders(0).accountID, messageType = constants.Comet.NEGOTIATION, messageContent = actors.Message.Negotiation(Option(docusignEnvelope.id)))
        actors.Service.cometActor ! actors.Message.makeCometMessage(username = traders(1).accountID, messageType = constants.Comet.NEGOTIATION, messageContent = actors.Message.Negotiation(Option(docusignEnvelope.id)))
        Ok(views.html.component.master.docusignReturnView(event))
      }).recover {
        case _: BaseException => InternalServerError(views.html.component.master.docusignReturnView(constants.View.UNEXPECTED_EVENT))
      }
  }

  def sign(negotiationID: String, documentType: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val emailAddress = masterEmails.Service.tryGetVerifiedEmailAddress(loginState.username)
      val trader = masterTraders.Service.tryGetByAccountID(loginState.username)
      val negotiation = masterNegotiations.Service.tryGet(negotiationID)
      val docusignEnvelope = masterTransactionDocusignEnvelopes.Service.tryGet(negotiationID, documentType)
      (for {
        emailAddress <- emailAddress
        trader <- trader
        negotiation <- negotiation
        docusignEnvelope <- docusignEnvelope
      } yield {
        if (negotiation.buyerTraderID == trader.id && negotiation.status == constants.Status.Negotiation.BUYER_ACCEPTED_ALL_NEGOTIATION_TERMS && docusignEnvelope.status == constants.Status.DocuSignEnvelopeStatus.SENT) {
          Ok(views.html.component.master.docusignView(utilitiesDocusign.createRecipientView(docusignEnvelope.envelopeID, emailAddress, trader)))
        } else {
          throw new BaseException(constants.Response.UNAUTHORIZED)
        }
      }).recover {
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