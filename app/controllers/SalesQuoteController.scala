package controllers

import controllers.actions.{WithTraderLoginAction, WithZoneLoginAction}
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.blockchain.Negotiation
import models.common.Serializable
import models.master.Accounts
import models.masterTransaction.{NegotiationFile, SalesQuote}
import models.{blockchain, blockchainTransaction, master, masterTransaction}
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SalesQuoteController @Inject()(messagesControllerComponents: MessagesControllerComponents, transaction: utilities.Transaction, masterAccounts: master.Accounts, masterTransactionSalesQuotes: masterTransaction.SalesQuotes, withTraderLoginAction: WithTraderLoginAction, withZoneLoginAction: WithZoneLoginAction, transactionsSellerExecuteOrder: transactions.SellerExecuteOrder, blockchainTransactionSellerExecuteOrders: blockchainTransaction.SellerExecuteOrders, accounts: Accounts, blockchainACLAccounts: blockchain.ACLAccounts, blockchainZones: blockchain.Zones, blockchainNegotiations: blockchain.Negotiations, withUsernameToken: WithUsernameToken)(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_SALES_QUOTE

  def commodityDetailsForm(id: Option[String]): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      (id match {
        case Some(requestID) => {
          val salesQuote = masterTransactionSalesQuotes.Service.get(requestID)

          def getResult(salesQuote: SalesQuote) = {
            if (salesQuote.accountID == loginState.username) {
              withUsernameToken.Ok(views.html.component.master.commodityDetails(views.companion.master.CommodityDetails.form.fill(views.companion.master.CommodityDetails.Data(Option(salesQuote.id), salesQuote.assetType, salesQuote.assetPrice, salesQuote.assetQuantity))))
            } else {
              Future(Unauthorized(views.html.index(failures = Seq(constants.Response.UNAUTHORIZED))))
            }
          }

          for {
            salesQuote <- salesQuote
            result <- getResult(salesQuote)
          } yield result
        }
        case None => withUsernameToken.Ok(views.html.component.master.commodityDetails())
      }).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def commodityDetails(): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.CommodityDetails.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.commodityDetails(formWithErrors)))
        },
        commodityDetailsData => {

          (commodityDetailsData.requestID match {
            case Some(id) => {
              val updateCommodityDetails = masterTransactionSalesQuotes.Service.updateCommodityDetails(id = id, assetType = commodityDetailsData.assetType, assetQuantity = commodityDetailsData.assetQuantity, assetPrice = commodityDetailsData.assetPrice)
              val salesQuote = masterTransactionSalesQuotes.Service.get(id)

              def getResult(salesQuote: SalesQuote) = {
                salesQuote.shippingDetails match {
                  case Some(shippingDetails) => withUsernameToken.PartialContent(views.html.component.master.shippingDetails(views.companion.master.ShippingDetails.form.fill(views.companion.master.ShippingDetails.Data(salesQuote.id, shippingDetails.shippingPeriod, shippingDetails.portOfLoading, shippingDetails.portOfDischarge)), id))
                  case None => withUsernameToken.PartialContent(views.html.component.master.shippingDetails(requestID = salesQuote.id))
                }
              }

              for {
                _ <- updateCommodityDetails
                salesQuote <- salesQuote
                result <- getResult(salesQuote)
              } yield result

            }
            case None => {
              val requestID = utilities.IDGenerator.requestID()
              val insertOrUpdate = masterTransactionSalesQuotes.Service.insertOrUpdate(requestID, accountID = loginState.username, assetType = commodityDetailsData.assetType, assetQuantity = commodityDetailsData.assetQuantity, assetPrice = commodityDetailsData.assetPrice, shippingDetails = None, paymentTerms = None, salesQuoteDocuments = None, completionStatus = false)
              for {
                _ <- insertOrUpdate
                result <- withUsernameToken.PartialContent(views.html.component.master.shippingDetails(requestID = requestID))
              } yield result
            }
          }).recover{
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def shippingDetailsForm(id: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val salesQuote = masterTransactionSalesQuotes.Service.get(id)

      def getResult(salesQuote: SalesQuote) = {
        salesQuote.shippingDetails match {
          case Some(shippingDetails: Serializable.ShippingDetails) => withUsernameToken.Ok(views.html.component.master.shippingDetails(views.companion.master.ShippingDetails.form.fill(views.companion.master.ShippingDetails.Data(id, shippingDetails.shippingPeriod, shippingDetails.portOfLoading, shippingDetails.portOfDischarge)), requestID = id))
          case None => withUsernameToken.Ok(views.html.component.master.shippingDetails(requestID = id))
        }
      }

      (for {
        salesQuote <- salesQuote
        result <- getResult(salesQuote)
      } yield result
        ).recover{
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def shippingDetails: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.ShippingDetails.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.shippingDetails(formWithErrors, formWithErrors.data(constants.FormField.REQUEST_ID.name))))
        },
        shippingDetailsData => {
          val updateShippingDetails = masterTransactionSalesQuotes.Service.updateShippingDetails(shippingDetailsData.requestID, Serializable.ShippingDetails(shippingDetailsData.shippingPeriod, shippingDetailsData.portOfLoading, shippingDetailsData.portOfDischarge))
          val salesQuote = masterTransactionSalesQuotes.Service.get(shippingDetailsData.requestID)

          def getResult(salesQuote: SalesQuote) = {
            salesQuote.paymentTerms match {
              case Some(paymentTerms: Serializable.PaymentTerms) => withUsernameToken.PartialContent(views.html.component.master.paymentTerms(views.companion.master.PaymentTerms.form.fill(views.companion.master.PaymentTerms.Data(shippingDetailsData.requestID, paymentTerms.advancePayment, paymentTerms.advancePercentage, paymentTerms.credit, paymentTerms.tenure, paymentTerms.tentativeDate, paymentTerms.refrence)), requestID = shippingDetailsData.requestID))
              case None => withUsernameToken.PartialContent(views.html.component.master.paymentTerms(requestID = shippingDetailsData.requestID))
            }
          }

          (for {
            _ <- updateShippingDetails
            salesQuote <- salesQuote
            result <- getResult(salesQuote)
          } yield result
            ).recover{
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        })
  }

  def paymentTermsForm(id: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val salesQuote = masterTransactionSalesQuotes.Service.get(id)

      def getResult(salesQuote: SalesQuote) = {
        salesQuote.paymentTerms match {
          case Some(paymentTerms: Serializable.PaymentTerms) => withUsernameToken.Ok(views.html.component.master.paymentTerms(views.companion.master.PaymentTerms.form.fill(views.companion.master.PaymentTerms.Data(id, paymentTerms.advancePayment, paymentTerms.advancePercentage, paymentTerms.credit, paymentTerms.tenure, paymentTerms.tentativeDate, paymentTerms.refrence)), requestID = id))
          case None => withUsernameToken.Ok(views.html.component.master.paymentTerms(requestID = id))
        }
      }

      (for {
        salesQuote <- salesQuote
        result <- getResult(salesQuote)
      } yield result
        ).recover{
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def paymentTerms: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.PaymentTerms.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.paymentTerms(formWithErrors, formWithErrors.data(constants.FormField.REQUEST_ID.name))))
        },
        paymentTermsData => {
          val updatePaymentTerms = masterTransactionSalesQuotes.Service.updatePaymentTerms(paymentTermsData.requestID, Serializable.PaymentTerms(paymentTermsData.advancePayment, paymentTermsData.advancePercentage, paymentTermsData.credit, paymentTermsData.tenure, paymentTermsData.tentativeDate, paymentTermsData.refrence))
          val salesQuote = masterTransactionSalesQuotes.Service.get(paymentTermsData.requestID)

          def getResult(salesQuote: SalesQuote) = {
            salesQuote.salesQuoteDocuments match {
              case Some(salesQuoteDocuments: Serializable.SalesQuoteDocuments) => withUsernameToken.PartialContent(views.html.component.master.salesQuoteDocuments(views.companion.master.SalesQuoteDocuments.form.fill(views.companion.master.SalesQuoteDocuments.Data(paymentTermsData.requestID, salesQuoteDocuments.obl, salesQuoteDocuments.invoice, salesQuoteDocuments.coa, salesQuoteDocuments.coo, salesQuoteDocuments.otherDocuments)), requestID = paymentTermsData.requestID))
              case None => withUsernameToken.PartialContent(views.html.component.master.salesQuoteDocuments(requestID = paymentTermsData.requestID))
            }
          }

          (for {
            _ <- updatePaymentTerms
            salesQuote <- salesQuote
            result <- getResult(salesQuote)
          } yield result
            ).recover{
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        })
  }

  def salesQuoteDocumentsForm(id: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val salesQuote = masterTransactionSalesQuotes.Service.get(id)

      def getResult(salesQuote: SalesQuote) = {
        salesQuote.salesQuoteDocuments match {
          case Some(salesQuoteDocuments: Serializable.SalesQuoteDocuments) => withUsernameToken.Ok(views.html.component.master.salesQuoteDocuments(views.companion.master.SalesQuoteDocuments.form.fill(views.companion.master.SalesQuoteDocuments.Data(id, salesQuoteDocuments.obl, salesQuoteDocuments.invoice, salesQuoteDocuments.coa, salesQuoteDocuments.coo, salesQuoteDocuments.otherDocuments)), requestID = id))
          case None => withUsernameToken.Ok(views.html.component.master.shippingDetails(requestID = id))
        }
      }

      (for {
        salesQuote <- salesQuote
        result <- getResult(salesQuote)
      } yield result
        ).recover{
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def salesQuoteDocuments: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.SalesQuoteDocuments.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.salesQuoteDocuments(formWithErrors, formWithErrors.data(constants.FormField.REQUEST_ID.name))))
        },
        salesQuoteDocumentsData => {
          val updateSalesQuoteDocuments = masterTransactionSalesQuotes.Service.updateSalesQuoteDocuments(salesQuoteDocumentsData.requestID, Serializable.SalesQuoteDocuments(salesQuoteDocumentsData.obl, salesQuoteDocumentsData.invoice, salesQuoteDocumentsData.COA, salesQuoteDocumentsData.COO, salesQuoteDocumentsData.otherDocuments))
          def salesQuote: Future[SalesQuote]= masterTransactionSalesQuotes.Service.get(salesQuoteDocumentsData.requestID)
          (for {
            _ <- updateSalesQuoteDocuments
            salesQuote<-salesQuote
            result <- withUsernameToken.PartialContent(views.html.component.master.traderReviewSalesQuoteDetails(requestID = salesQuoteDocumentsData.requestID, salesQuote= salesQuote))
          } yield result
            ).recover{
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def traderReviewSalesQuoteDetailsForm(id:String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val salesQuote = masterTransactionSalesQuotes.Service.get(id)
      (for{
        salesQuote<-salesQuote
        result<-withUsernameToken.Ok(views.html.component.master.traderReviewSalesQuoteDetails(requestID = id, salesQuote= salesQuote))
      }yield result
        ).recover{
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }
  def traderReviewSalesQuoteDetails: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.TraderReviewSalesQuoteDetails.form.bindFromRequest().fold(
        formWithErrors => {

          val salesQuote= masterTransactionSalesQuotes.Service.get(formWithErrors.data(constants.FormField.REQUEST_ID.name))
          (for{
            salesQuote<-salesQuote
          }yield BadRequest(views.html.component.master.traderReviewSalesQuoteDetails(formWithErrors, formWithErrors.data(constants.FormField.REQUEST_ID.name),salesQuote))
            ).recover{
            case baseException: BaseException => InternalServerError(views.html.indexVersion3(failures = Seq(baseException.failure)))
          }
        },
        traderReviewSalesQuoteDetailsData => {
          (if(traderReviewSalesQuoteDetailsData.completion){
              val updateCompletionStatus: Future[Int] = masterTransactionSalesQuotes.Service.updateCompletionStatus(traderReviewSalesQuoteDetailsData.requestID)
              for{
                _<-updateCompletionStatus
                result <- withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.SALES_QUOTE_CREATED)))
              }yield result
            }else{
              val salesQuote= masterTransactionSalesQuotes.Service.get(traderReviewSalesQuoteDetailsData.requestID)
              for{
                salesQuote<-salesQuote
              }yield BadRequest(views.html.component.master.traderReviewSalesQuoteDetails(requestID=traderReviewSalesQuoteDetailsData.requestID,salesQuote=salesQuote))
            }).recover{
            case baseException: BaseException => InternalServerError(views.html.indexVersion3(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def salesQuoteList: Action[AnyContent]= withTraderLoginAction.authenticated{implicit loginState =>
    implicit request =>
    val salesQuotes= masterTransactionSalesQuotes.Service.getSalesQuotes(loginState.username)
      (for{
      salesQuotes<-salesQuotes
    }yield Ok(views.html.component.master.salesQuotesList(salesQuotes))
        ).recover{
        case baseException: BaseException =>
          println(baseException.failure.message)
          InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }
}
