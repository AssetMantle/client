package controllers

import controllers.actions.{WithTraderLoginAction, WithZoneLoginAction}
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.common.Serializable
import models.masterTransaction.SalesQuote
import models.{blockchain, blockchainTransaction, master, masterTransaction}
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SalesQuoteController @Inject()(messagesControllerComponents: MessagesControllerComponents, transaction: utilities.Transaction, masterAccounts: master.Accounts, masterTransactionSalesQuotes: masterTransaction.SalesQuotes, masterTradeRelations: master.TraderRelations, masterTradeRooms: master.TradeRooms, masterTransactionTradeTerms: masterTransaction.TradeTerms, withTraderLoginAction: WithTraderLoginAction, withZoneLoginAction: WithZoneLoginAction, transactionsSellerExecuteOrder: transactions.SellerExecuteOrder, blockchainTransactionSellerExecuteOrders: blockchainTransaction.SellerExecuteOrders, accounts: master.Accounts, masterTraders: master.Traders, blockchainACLAccounts: blockchain.ACLAccounts, blockchainZones: blockchain.Zones, blockchainNegotiations: blockchain.Negotiations, withUsernameToken: WithUsernameToken)(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_SALES_QUOTE

  def commodityDetailsForm(id: Option[String]): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      (id match {
        case Some(requestID) => {
          val salesQuote = masterTransactionSalesQuotes.Service.get(requestID)

          def getResult(salesQuote: SalesQuote) = {
            if (salesQuote.accountID == loginState.username) {
              withUsernameToken.Ok(views.html.component.master.commodityDetails(views.companion.master.CommodityDetails.form.fill(views.companion.master.CommodityDetails.Data(Option(salesQuote.id), salesQuote.assetType, salesQuote.assetDescription, salesQuote.assetPrice, salesQuote.assetQuantity))))
            } else {
              Future(Unauthorized(views.html.trades(failures = Seq(constants.Response.UNAUTHORIZED))))
            }
          }

          for {
            salesQuote <- salesQuote
            result <- getResult(salesQuote)
          } yield result
        }
        case None => withUsernameToken.Ok(views.html.component.master.commodityDetails())
      }).recover {
        case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
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
              val updateCommodityDetails = masterTransactionSalesQuotes.Service.updateCommodityDetails(id = id, assetType = commodityDetailsData.assetType, assetDescription = commodityDetailsData.assetDescription, assetQuantity = commodityDetailsData.assetQuantity, assetPrice = commodityDetailsData.assetPrice)
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
              val insertOrUpdate = masterTransactionSalesQuotes.Service.insertOrUpdate(requestID, accountID = loginState.username, assetType = commodityDetailsData.assetType, assetDescription = commodityDetailsData.assetDescription, assetQuantity = commodityDetailsData.assetQuantity, assetPrice = commodityDetailsData.assetPrice, shippingDetails = None, paymentTerms = None, salesQuoteDocuments = None, buyerAccountID = None, completionStatus = false, invitationStatus = None)
              for {
                _ <- insertOrUpdate
                result <- withUsernameToken.PartialContent(views.html.component.master.shippingDetails(requestID = requestID))
              } yield result
            }
          }).recover {
            case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
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
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
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
            ).recover {
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
        ).recover {
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
              case Some(salesQuoteDocuments: Serializable.SalesQuoteDocuments) => withUsernameToken.PartialContent(views.html.component.master.salesQuoteDocuments(views.companion.master.SalesQuoteDocuments.form.fill(views.companion.master.SalesQuoteDocuments.Data(paymentTermsData.requestID, salesQuoteDocuments.billOfExchangeRequired, salesQuoteDocuments.obl, salesQuoteDocuments.invoice, salesQuoteDocuments.coa, salesQuoteDocuments.coo, salesQuoteDocuments.otherDocuments)), requestID = paymentTermsData.requestID))
              case None => withUsernameToken.PartialContent(views.html.component.master.salesQuoteDocuments(requestID = paymentTermsData.requestID))
            }
          }

          (for {
            _ <- updatePaymentTerms
            salesQuote <- salesQuote
            result <- getResult(salesQuote)
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        })
  }

  def salesQuoteDocumentsForm(id: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val salesQuote = masterTransactionSalesQuotes.Service.get(id)

      def getResult(salesQuote: SalesQuote) = {
        salesQuote.salesQuoteDocuments match {
          case Some(salesQuoteDocuments: Serializable.SalesQuoteDocuments) => withUsernameToken.Ok(views.html.component.master.salesQuoteDocuments(views.companion.master.SalesQuoteDocuments.form.fill(views.companion.master.SalesQuoteDocuments.Data(id, salesQuoteDocuments.billOfExchangeRequired, salesQuoteDocuments.obl, salesQuoteDocuments.invoice, salesQuoteDocuments.coa, salesQuoteDocuments.coo, salesQuoteDocuments.otherDocuments)), requestID = id))
          case None => withUsernameToken.Ok(views.html.component.master.shippingDetails(requestID = id))
        }
      }

      (for {
        salesQuote <- salesQuote
        result <- getResult(salesQuote)
      } yield result
        ).recover {
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
          val updateSalesQuoteDocuments = masterTransactionSalesQuotes.Service.updateSalesQuoteDocuments(salesQuoteDocumentsData.requestID, Serializable.SalesQuoteDocuments(salesQuoteDocumentsData.billOfExchangeRequired, salesQuoteDocumentsData.obl, salesQuoteDocumentsData.invoice, salesQuoteDocumentsData.COA, salesQuoteDocumentsData.COO, salesQuoteDocumentsData.otherDocuments))

          val traderID = masterTraders.Service.tryGetID(loginState.username)

          def getCounterPartyList(traderID: String) = masterTradeRelations.Service.getAllCounterPartyIDs(traderID)

          def getTraderCounterPartyDetails(traderIDs: Seq[String]) = masterTraders.Service.getTraders(traderIDs)

          (for {
            _ <- updateSalesQuoteDocuments
            traderID <- traderID
            counterPartyList <- getCounterPartyList(traderID)
            traderCounterPartyDetails <- getTraderCounterPartyDetails(counterPartyList)
            result <- withUsernameToken.PartialContent(views.html.component.master.inviteSalesQuoteBuyer(requestID = salesQuoteDocumentsData.requestID, traders = traderCounterPartyDetails))
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def traderReviewSalesQuoteDetailsForm(id: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val salesQuote = masterTransactionSalesQuotes.Service.get(id)
      (for {
        salesQuote <- salesQuote
        result <- withUsernameToken.Ok(views.html.component.master.traderReviewSalesQuoteDetails(requestID = id, salesQuote = salesQuote))
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def traderReviewSalesQuoteDetails: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.TraderReviewSalesQuoteDetails.form.bindFromRequest().fold(
        formWithErrors => {

          val salesQuote = masterTransactionSalesQuotes.Service.get(formWithErrors.data(constants.FormField.REQUEST_ID.name))
          (for {
            salesQuote <- salesQuote
          } yield BadRequest(views.html.component.master.traderReviewSalesQuoteDetails(formWithErrors, formWithErrors.data(constants.FormField.REQUEST_ID.name), salesQuote))
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        },
        traderReviewSalesQuoteDetailsData => {
          (if (traderReviewSalesQuoteDetailsData.completion) {
            val updateCompletionStatus: Future[Int] = masterTransactionSalesQuotes.Service.updateCompletionStatus(traderReviewSalesQuoteDetailsData.requestID)
            for {
              _ <- updateCompletionStatus
              result <- withUsernameToken.Ok(views.html.trades(successes = Seq(constants.Response.SALES_QUOTE_INVITATION_SENT)))
            } yield result
          } else {
            val salesQuote = masterTransactionSalesQuotes.Service.get(traderReviewSalesQuoteDetailsData.requestID)
            for {
              salesQuote <- salesQuote
            } yield BadRequest(views.html.component.master.traderReviewSalesQuoteDetails(requestID = traderReviewSalesQuoteDetailsData.requestID, salesQuote = salesQuote))
          }).recover {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def inviteSalesQuoteBuyerForm(requestID: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>

      val traderID = masterTraders.Service.tryGetID(loginState.username)

      def getCounterPartyList(traderID: String) = masterTradeRelations.Service.getAllCounterPartyIDs(traderID)

      def getTraderCounterPartyDetails(traderIDs: Seq[String]) = masterTraders.Service.getTraders(traderIDs)

      (for {
        traderID <- traderID
        counterPartyList <- getCounterPartyList(traderID)
        traderCounterPartyDetails <- getTraderCounterPartyDetails(counterPartyList)
        result <- withUsernameToken.Ok(views.html.component.master.inviteSalesQuoteBuyer(requestID = requestID, traders = traderCounterPartyDetails))
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
      }
  }

  def inviteSalesQuoteBuyer: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.InviteSalesQuoteBuyer.form.bindFromRequest().fold(
        formWithErrors => {
          val traderID = masterTraders.Service.tryGetID(loginState.username)

          def getCounterPartyList(traderID: String) = masterTradeRelations.Service.getAllCounterPartyIDs(traderID)

          def getTraderCounterPartyDetails(traderIDs: Seq[String]) = masterTraders.Service.getTraders(traderIDs)

          (for {
            traderID <- traderID
            counterPartyList <- getCounterPartyList(traderID)
            traderCounterPartyDetails <- getTraderCounterPartyDetails(counterPartyList)
          } yield BadRequest(views.html.component.master.inviteSalesQuoteBuyer(formWithErrors, formWithErrors.data(constants.FormField.COUNTER_PARTY.name), traders = traderCounterPartyDetails))
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
          }
        },
        traderReviewSalesQuoteDetailsData => {
          val counterPartyTraderID = masterTraders.Service.tryGetID(traderReviewSalesQuoteDetailsData.counterParty)
          val sellerTraderID = masterTraders.Service.tryGetID(loginState.username)

          def checkIfBuyerPresentAsCounterParty(counterPartyTraderID: String, sellerTraderID: String) = masterTradeRelations.Service.get(counterPartyTraderID, sellerTraderID).map(_ => true).recover {
            case baseException: BaseException => {
              if (baseException.failure == constants.Response.NO_SUCH_ELEMENT_EXCEPTION) false else throw baseException
            }
          }

          def updateAndGetResult(buyerPresentAsCounterParty: Boolean) = {
            if (buyerPresentAsCounterParty) {
              val updateSalesQuoteBuyer = masterTransactionSalesQuotes.Service.updateBuyer(traderReviewSalesQuoteDetailsData.requestID, traderReviewSalesQuoteDetailsData.counterParty)

              def salesQuote = masterTransactionSalesQuotes.Service.get(traderReviewSalesQuoteDetailsData.requestID)

              for {
                _ <- updateSalesQuoteBuyer
                salesQuote <- salesQuote
                result <- withUsernameToken.PartialContent(views.html.component.master.traderReviewSalesQuoteDetails(requestID = traderReviewSalesQuoteDetailsData.requestID, salesQuote = salesQuote))
              } yield result
            } else {
              val traderID = masterTraders.Service.tryGetID(loginState.username)

              def getCounterPartyList(traderID: String) = masterTradeRelations.Service.getAllCounterPartyIDs(traderID)

              def getTraderCounterPartyDetails(traderIDs: Seq[String]) = masterTraders.Service.getTraders(traderIDs)

              for {
                traderID <- traderID
                counterPartyList <- getCounterPartyList(traderID)
                traderCounterPartyDetails <- getTraderCounterPartyDetails(counterPartyList)
              } yield BadRequest(views.html.component.master.inviteSalesQuoteBuyer(views.companion.master.InviteSalesQuoteBuyer.form.fill(views.companion.master.InviteSalesQuoteBuyer.Data(traderReviewSalesQuoteDetailsData.requestID, traderReviewSalesQuoteDetailsData.counterParty)).withError(constants.FormField.COUNTER_PARTY.name, constants.Response.NOT_PRESENT_AS_COUNTERPARTY.message), requestID = traderReviewSalesQuoteDetailsData.requestID, traders = traderCounterPartyDetails))
            }
          }

          (for {
            counterPartyTraderID <- counterPartyTraderID
            sellerTraderID <- sellerTraderID
            buyerPresentAsCounterParty <- checkIfBuyerPresentAsCounterParty(counterPartyTraderID, sellerTraderID)
            result <- updateAndGetResult(buyerPresentAsCounterParty)
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def acceptOrRejectSalesQuoteForm(id: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val salesQuote = masterTransactionSalesQuotes.Service.get(id)
      (for {
        salesQuote <- salesQuote
      } yield Ok(views.html.component.master.acceptOrRejectSalesQuote(salesQuote = salesQuote))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.profile(failures = Seq(baseException.failure)))
      }
  }

  def acceptOrRejectSalesQuote: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.AcceptOrRejectSalesQuote.form.bindFromRequest().fold(
        formWithErrors => {
          val salesQuote = masterTransactionSalesQuotes.Service.get(formWithErrors.data(constants.FormField.SALES_QUOTE_ID.name))
          (for {
            salesQuote <- salesQuote
          } yield BadRequest(views.html.component.master.acceptOrRejectSalesQuote(formWithErrors, salesQuote = salesQuote))
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        },
        acceptOrRejectSalesQuoteData => {

          val updateStatusAndCreateTradeRoom = if (acceptOrRejectSalesQuoteData.status) {

            val markAccepted = masterTransactionSalesQuotes.Service.markAccepted(acceptOrRejectSalesQuoteData.salesQuoteID)

            def salesQuote = masterTransactionSalesQuotes.Service.get(acceptOrRejectSalesQuoteData.salesQuoteID)
            //TODO: trade Room Status to be defined
            def createTradeRoom(buyerAccountID: String, sellerAccountId: String) = masterTradeRooms.Service.create(acceptOrRejectSalesQuoteData.salesQuoteID, buyerAccountID, sellerAccountId, None, "UnderNegotiation")

            def createTradeTerms(tradeRoomID: String, salesQuote: SalesQuote) = masterTransactionTradeTerms.Service.create(tradeRoomID, salesQuote.assetType, salesQuote.assetDescription, salesQuote.assetQuantity, salesQuote.assetPrice, salesQuote.shippingDetails.get.shippingPeriod, salesQuote.shippingDetails.get.portOfLoading, salesQuote.shippingDetails.get.portOfDischarge, salesQuote.paymentTerms.get.advancePayment, salesQuote.paymentTerms.get.advancePercentage, salesQuote.paymentTerms.get.credit, salesQuote.paymentTerms.get.tenure, if (salesQuote.paymentTerms.get.tentativeDate.isDefined) Some(utilities.Date.utilDateToSQLDate(salesQuote.paymentTerms.get.tentativeDate.get)) else None, salesQuote.paymentTerms.get.refrence, salesQuote.salesQuoteDocuments.get.billOfExchangeRequired, salesQuote.salesQuoteDocuments.get.obl, salesQuote.salesQuoteDocuments.get.invoice, salesQuote.salesQuoteDocuments.get.coo, salesQuote.salesQuoteDocuments.get.coa, salesQuote.salesQuoteDocuments.get.otherDocuments)

            for {
              _ <- markAccepted
              salesQuote <- salesQuote
              tradeRoomID <- createTradeRoom(salesQuote.buyerAccountID.get, salesQuote.accountID)
              _ <- createTradeTerms(tradeRoomID, salesQuote)
            } yield salesQuote
          } else {
            val markRejected = masterTransactionSalesQuotes.Service.markRejected(acceptOrRejectSalesQuoteData.salesQuoteID)

            def salesQuote = masterTransactionSalesQuotes.Service.get(acceptOrRejectSalesQuoteData.salesQuoteID)

            for {
              _ <- markRejected
              salesQuote <- salesQuote
            } yield salesQuote
          }

          (for {
            salesQuote <- updateStatusAndCreateTradeRoom
            result <- withUsernameToken.PartialContent(views.html.component.master.acceptOrRejectSalesQuote(salesQuote = salesQuote))
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
          }
        }
      )
  }
}
