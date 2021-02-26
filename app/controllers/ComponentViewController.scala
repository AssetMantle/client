package controllers

import java.text.DecimalFormat
import java.time.Year
import java.util.Calendar

import controllers.actions._
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models._
import models.common.Serializable._
import models.master._
import models.masterTransaction.{SendFiatRequest, _}
import play.api.http.ContentTypes
import models.blockchain._
import models.masterTransaction.TokenPrice
import models.{blockchain, blockchainTransaction, master, masterTransaction}
import play.api.i18n.I18nSupport
import play.api.libs.Comet
import play.api.mvc._
import play.api.{Configuration, Logger}
import play.twirl.api.Html
import utilities.MicroNumber
import utilities.MicroNumber._
import queries.blockchain.{GetDelegatorRewards, GetValidatorSelfBondAndCommissionRewards}
import utilities.MicroNumber
import javax.inject.{Inject, Singleton}
import models.master.Negotiation
import models.master.Order
import models.master.Zone
import models.master.Organization
import scala.collection.immutable.ListMap
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ComponentViewController @Inject()(
                                         blockchainAccounts: blockchain.Accounts,
                                         blockchainAssets: blockchain.Assets,
                                         blockchainClassifications: blockchain.Classifications,
                                         blockchainDelegations: blockchain.Delegations,
                                         blockchainIdentities: blockchain.Identities,
                                         blockchainOrders: blockchain.Orders,
                                         blockchainRedelegations: blockchain.Redelegations,
                                         blockchainSplits: blockchain.Splits,
                                         blockchainMetas: blockchain.Metas,
                                         blockchainUndelegations: blockchain.Undelegations,
                                         blockchainBlocks: blockchain.Blocks,
                                         blockchainAverageBlockTimes: blockchain.AverageBlockTimes,
                                         blockchainTransactions: blockchain.Transactions,
                                         blockchainTransactionsIdentityDefines: blockchainTransaction.IdentityDefines,
                                         blockchainTransactionsIdentityNubs: blockchainTransaction.IdentityNubs,
                                         blockchainTransactionsIdentityIssues: blockchainTransaction.IdentityIssues,
                                         blockchainTransactionsIdentityProvisions: blockchainTransaction.IdentityProvisions,
                                         blockchainTransactionsIdentityUnprovisions: blockchainTransaction.IdentityUnprovisions,
                                         blockchainTransactionsAssetDefines: blockchainTransaction.AssetDefines,
                                         blockchainTransactionsAssetMints: blockchainTransaction.AssetMints,
                                         blockchainTransactionsAssetMutates: blockchainTransaction.AssetMutates,
                                         blockchainTransactionsAssetBurns: blockchainTransaction.AssetBurns,
                                         blockchainTransactionsOrderDefines: blockchainTransaction.OrderDefines,
                                         blockchainTransactionsOrderMakes: blockchainTransaction.OrderMakes,
                                         blockchainTransactionsOrderTakes: blockchainTransaction.OrderTakes,
                                         blockchainTransactionsOrderCancels: blockchainTransaction.OrderCancels,
                                         blockchainTokens: blockchain.Tokens,
                                         blockchainValidators: blockchain.Validators,
                                         getDelegatorRewards: GetDelegatorRewards,
                                         getValidatorSelfBondAndCommissionRewards: GetValidatorSelfBondAndCommissionRewards,
                                         masterTransactionTokenPrices: masterTransaction.TokenPrices,
                                         messagesControllerComponents: MessagesControllerComponents,
                                         blockchainFiats: blockchain.Fiats,
                                         masterAssets: master.Assets,
                                         masterAssetHistories: master.AssetHistories,
                                         masterAccountFiles: master.AccountFiles,
                                         masterAccountKYCs: master.AccountKYCs,
                                         masterOrders: master.Orders,
                                         masterTransactionAssetFiles: masterTransaction.AssetFiles,
                                         masterTransactionAssetFileHistories: masterTransaction.AssetFileHistories,
                                         docusignEnvelopes: docusign.Envelopes,
                                         masterTransactionNegotiationFiles: masterTransaction.NegotiationFiles,
                                         masterTransactionNegotiationFileHistories: masterTransaction.NegotiationFileHistories,
                                         masterTransactionReceiveFiats: masterTransaction.ReceiveFiats,
                                         masterTransactionRedeemFiatRequests: masterTransaction.RedeemFiatRequests,
                                         masterTransactionSendFiatRequests: masterTransaction.SendFiatRequests,
                                         masterTransactionSendFiatRequestHistories: masterTransaction.SendFiatRequestHistories,
                                         memberCheckVesselScanDecisions: memberCheck.VesselScanDecisions,
                                         masterTransactionReceiveFiatHistories: masterTransaction.ReceiveFiatHistories,
                                         westernUnionFiatRequests: westernUnion.FiatRequests,
                                         westernUnionRTCBs: westernUnion.RTCBs,
                                         withLoginAction: WithLoginActionAsync,
                                         withOrganizationLoginAction: WithOrganizationLoginAction,
                                         withTraderLoginAction: WithTraderLoginAction,
                                         withUserLoginAction: WithUserLoginAction,
                                         withZoneLoginAction: WithZoneLoginAction,
                                         withoutLoginAction: WithoutLoginAction,
                                         withoutLoginActionAsync: WithoutLoginActionAsync,
                                         masterClassifications: master.Classifications,
                                         masterIdentities: master.Identities,
                                         masterSplits: master.Splits,
                                         masterFiats: master.Fiats,
                                         masterEmails: master.Emails,
                                         masterMobiles: master.Mobiles,
                                         masterTraderRelations: master.TraderRelations,
                                         masterOrganizationKYCs: master.OrganizationKYCs,
                                         masterOrganizations:master.Organizations,
                                         masterNegotiations: master.Negotiations,
                                         masterNegotiationHistories: master.NegotiationHistories,
                                         masterOrganizationBankAccountDetails: master.OrganizationBankAccountDetails,
                                         masterTraders: master.Traders,
                                         masterIdentifications: master.Identifications,
                                         masterProperties: master.Properties,
                                         masterZones:master.Zones,
                                         masterOrganizationUBOs: master.OrganizationUBOs,
                                         withLoginActionAsync: WithLoginActionAsync,
                                       )(implicit configuration: Configuration, executionContext: ExecutionContext) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_COMPONENT_VIEW

  private val bondedStatus = configuration.get[Int]("blockchain.validator.status.bonded")

  private val stakingDenom = configuration.get[String]("blockchain.stakingDenom")

  private val chainID = configuration.get[String]("blockchain.chainID")


  def commonHome: Action[AnyContent] = withLoginActionAsync { implicit loginState =>
    implicit request =>
      Future(Ok(views.html.component.master.commonHome()))
  }

  def fiatList: Action[AnyContent] = withTraderLoginAction { implicit loginState =>
    implicit request =>
      val fiatPegWallet = blockchainFiats.Service.getFiatPegWallet(loginState.address)
      (for {
        fiatPegWallet <- fiatPegWallet
      } yield Ok(views.html.component.master.fiatList(fiatPegWallet))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def traderFinancials: Action[AnyContent] = withTraderLoginAction { implicit loginState =>
    implicit request =>
      val traderID = masterTraders.Service.tryGetID(loginState.username)

      def getFiatPegWallet(traderID: String) = masterFiats.Service.getFiatPegWallet(traderID)

      def getNegotiationList(traderID: String): Future[Seq[Negotiation]] = masterNegotiations.Service.getAllConfirmedNegotiationListByTraderID(traderID)

      def getIncompleteNegotiationList(negotiations: Seq[Negotiation], completedOrders: Seq[Order]) = negotiations.filterNot(completedOrders.map(_.id) contains _.orderID.getOrElse(""))

      def getCompletedOrderList(orderIDs: Seq[String]) = masterOrders.Service.getCompletedOrdersByOrderIDs(orderIDs)

      def getFiatsInOrderList(masterNegotiationIDs: Seq[String]) = masterTransactionSendFiatRequests.Service.getFiatRequestsInOrders(masterNegotiationIDs)

      def getPayable(traderID: String, fiatsInOrders: Seq[SendFiatRequest], completedOrders: Seq[Order], negotiations: Seq[Negotiation]): MicroNumber = {
        getIncompleteNegotiationList(negotiations.filter(_.buyerTraderID == traderID), completedOrders).map(_.price).sum - fiatsInOrders.filter(_.traderID == traderID).map(_.amount).sum
      }

      def getReceivable(incompleteNegotiations: Seq[Negotiation], traderID: String): MicroNumber = incompleteNegotiations.filter(_.sellerTraderID == traderID).map(_.price).sum

      (for {
        traderID <- traderID
        fiatPegWallet <- getFiatPegWallet(traderID)
        negotiationList <- getNegotiationList(traderID)
        completedOrderList <- getCompletedOrderList(negotiationList.map(_.orderID.getOrElse("")))
        fiatsInOrderList <- getFiatsInOrderList(getIncompleteNegotiationList(negotiationList, completedOrderList).map(_.id))
      } yield Ok(views.html.component.master.traderFinancials(walletBalance = fiatPegWallet.map(_.transactionAmount).sum, payable = getPayable(traderID, fiatsInOrderList, completedOrderList, negotiationList), receivable = getReceivable(getIncompleteNegotiationList(negotiationList, completedOrderList), traderID)))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def organizationFinancial: Action[AnyContent] = withOrganizationLoginAction { implicit loginState =>
    implicit request =>
      val organizationID = masterOrganizations.Service.tryGetID(loginState.username)

      def getTraderIDList(organizationID: String) = masterTraders.Service.getVerifiedTraderIDsByOrganizationID(organizationID)

      def getFiatPegWallet(traderIDs: Seq[String]) = masterFiats.Service.getFiatPegWallet(traderIDs)

      def getNegotiationList(traderIDs: Seq[String]) = masterNegotiations.Service.getAllConfirmedNegotiationListByTraderIDs(traderIDs)

      def getIncompleteNegotiationList(negotiations: Seq[Negotiation], completedOrders: Seq[Order]) = negotiations.filterNot(completedOrders.map(_.id) contains _.orderID.getOrElse(""))

      def getCompletedOrderList(orderIDs: Seq[String]) = masterOrders.Service.getCompletedOrdersByOrderIDs(orderIDs)

      def getFiatsInOrderList(masterNegotiationIDs: Seq[String]) = masterTransactionSendFiatRequests.Service.getFiatRequestsInOrders(masterNegotiationIDs)

      def getPayable(traderIDs: Seq[String], fiatsInOrders: Seq[SendFiatRequest], completedOrders: Seq[Order], negotiations: Seq[Negotiation]) = {
        val buyingNegotiations = negotiations.filter(traderIDs contains _.buyerTraderID)
        getIncompleteNegotiationList(buyingNegotiations, completedOrders).map(_.price).sum - fiatsInOrders.filter(buyingNegotiations.map(_.id) contains _.negotiationID).filter(traderIDs contains _.traderID).map(_.amount).sum
      }

      def getReceivable(incompleteNegotiations: Seq[Negotiation], traderIDs: Seq[String]) = incompleteNegotiations.filter(traderIDs contains _.sellerTraderID).map(_.price).sum

      (for {
        organizationID <- organizationID
        traderIDList <- getTraderIDList(organizationID)
        fiatPegWallet <- getFiatPegWallet(traderIDList)
        negotiationList <- getNegotiationList(traderIDList)
        completedOrderList <- getCompletedOrderList(negotiationList.map(_.orderID.getOrElse("")))
        fiatsInOrderList <- getFiatsInOrderList(getIncompleteNegotiationList(negotiationList, completedOrderList).map(_.id))
      } yield Ok(views.html.component.master.organizationFinancial(walletBalance = fiatPegWallet.map(_.transactionAmount).sum, payable = getPayable(traderIDList, fiatsInOrderList, completedOrderList, negotiationList), receivable = getReceivable(getIncompleteNegotiationList(negotiationList, completedOrderList), traderIDList)))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def zoneFinancial: Action[AnyContent] = withZoneLoginAction { implicit loginState =>
    implicit request =>
      val zoneID = masterZones.Service.tryGetID(loginState.username)

      def getTraderIDList(zoneID: String) = masterTraders.Service.getVerifiedTraderIDsByZoneID(zoneID)

      def getFiatPegWallet(traderIDs: Seq[String]) = masterFiats.Service.getFiatPegWallet(traderIDs)

      def getNegotiationList(traderIDs: Seq[String]) = masterNegotiations.Service.getAllConfirmedNegotiationListByTraderIDs(traderIDs)

      def getIncompleteNegotiationList(negotiations: Seq[Negotiation], completedOrders: Seq[Order]) = negotiations.filterNot(completedOrders.map(_.id) contains _.orderID.getOrElse(""))

      def getCompletedOrderList(orderIDs: Seq[String]) = masterOrders.Service.getCompletedOrdersByOrderIDs(orderIDs)

      def getFiatsInOrderList(masterNegotiationIDs: Seq[String]) = masterTransactionSendFiatRequests.Service.getFiatRequestsInOrders(masterNegotiationIDs)

      def getPayable(traderIDs: Seq[String], fiatsInOrders: Seq[SendFiatRequest], completedOrders: Seq[Order], negotiations: Seq[Negotiation]) = {
        val buyingNegotiations = negotiations.filter(traderIDs contains _.buyerTraderID)
        getIncompleteNegotiationList(buyingNegotiations, completedOrders).map(_.price).sum - fiatsInOrders.filter(buyingNegotiations.map(_.id) contains _.negotiationID).filter(traderIDs contains _.traderID).map(_.amount).sum
      }

      def getReceivable(incompleteNegotiations: Seq[Negotiation], traderIDs: Seq[String]) = incompleteNegotiations.filter(traderIDs contains _.sellerTraderID).map(_.price).sum

      (for {
        zoneID <- zoneID
        traderIDList <- getTraderIDList(zoneID)
        fiatPegWallet <- getFiatPegWallet(traderIDList)
        negotiationList <- getNegotiationList(traderIDList)
        completedOrderList <- getCompletedOrderList(negotiationList.map(_.orderID.getOrElse("")))
        fiatsInOrderList <- getFiatsInOrderList(getIncompleteNegotiationList(negotiationList, completedOrderList).map(_.id))
      } yield Ok(views.html.component.master.zoneFinancial(walletBalance = fiatPegWallet.map(_.transactionAmount).sum, payable = getPayable(traderIDList, fiatsInOrderList, completedOrderList, negotiationList), receivable = getReceivable(getIncompleteNegotiationList(negotiationList, completedOrderList), traderIDList)))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def traderViewAcceptedNegotiationList: Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
    Ok(views.html.component.master.traderViewAcceptedNegotiationList())
  }

  def traderViewAcceptedBuyNegotiationList: Action[AnyContent] = withTraderLoginAction { implicit loginState =>
    implicit request =>
      val traderID = masterTraders.Service.tryGetID(loginState.username)

      def getBuyNegotiationList(traderID: String): Future[Seq[Negotiation]] = masterNegotiations.Service.getAllAcceptedBuyNegotiationListByTraderID(traderID)

      def getCounterPartyTraderList(traderIDs: Seq[String]): Future[Seq[Trader]] = masterTraders.Service.getTraders(traderIDs)

      def getCounterPartyOrganizationList(organizationIDs: Seq[String]) = masterOrganizations.Service.getOrganizations(organizationIDs)

      def getAssetList(assetIDs: Seq[String])= masterProperties.Service.getPropertyListMap(assetIDs)

      (for {
        traderID <- traderID
        buyNegotiationList <- getBuyNegotiationList(traderID)
        counterPartyTraderList <- getCounterPartyTraderList(buyNegotiationList.map(_.sellerTraderID))
        counterPartyOrganizationList <- getCounterPartyOrganizationList(counterPartyTraderList.map(_.organizationID))
        assetList <- getAssetList(buyNegotiationList.map(_.assetID))
      } yield Ok(views.html.component.master.traderViewAcceptedBuyNegotiationList(buyNegotiationList = buyNegotiationList, counterPartyTraderList = counterPartyTraderList, counterPartyOrganizationList = counterPartyOrganizationList, assetList = assetList))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def traderViewAcceptedSellNegotiationList: Action[AnyContent] = withTraderLoginAction { implicit loginState =>
    implicit request =>
      val traderID = masterTraders.Service.tryGetID(loginState.username)

      def getSellNegotiationList(traderID: String): Future[Seq[Negotiation]] = masterNegotiations.Service.getAllAcceptedSellNegotiationListByTraderID(traderID)

      def getCounterPartyTraderList(traderIDs: Seq[String]): Future[Seq[Trader]] = masterTraders.Service.getTraders(traderIDs)

      def getCounterPartyOrganizationList(organizationIDs: Seq[String]) = masterOrganizations.Service.getOrganizations(organizationIDs)

      def getAssetList(assetIDs: Seq[String])= masterProperties.Service.getPropertyListMap(assetIDs)

      (for {
        traderID <- traderID
        sellNegotiationList <- getSellNegotiationList(traderID)
        counterPartyTraderList <- getCounterPartyTraderList(sellNegotiationList.map(_.buyerTraderID))
        counterPartyOrganizationList <- getCounterPartyOrganizationList(counterPartyTraderList.map(_.organizationID))
        assetList <- getAssetList(sellNegotiationList.map(_.assetID))
      } yield Ok(views.html.component.master.traderViewAcceptedSellNegotiationList(sellNegotiationList = sellNegotiationList, counterPartyTraderList = counterPartyTraderList, counterPartyOrganizationList = counterPartyOrganizationList, assetList = assetList))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def traderViewCompletedNegotiationList: Action[AnyContent] = withTraderLoginAction { implicit loginState =>
    implicit request =>
      val traderID = masterTraders.Service.tryGetID(loginState.username)

      def getCompletedNegotiationList(traderID: String): Future[Seq[NegotiationHistory]] = masterNegotiationHistories.Service.getAllCompletedNegotiationListByTraderID(traderID)

      def getCounterPartyTraderList(traderIDs: Seq[String]): Future[Seq[Trader]] = masterTraders.Service.getTraders(traderIDs)

      def getCounterPartyOrganizationList(organizationIDs: Seq[String]) = masterOrganizations.Service.getOrganizations(organizationIDs)

      def getAssetList(assetIDs: Seq[String]) = masterProperties.Service.getPropertyListMap(assetIDs)

      (for {
        traderID <- traderID
        completedNegotiationList <- getCompletedNegotiationList(traderID)
        counterPartyTraderList <- getCounterPartyTraderList(completedNegotiationList.map(negotiation => if (negotiation.sellerTraderID == traderID) negotiation.buyerTraderID else negotiation.sellerTraderID))
        counterPartyOrganizationList <- getCounterPartyOrganizationList(counterPartyTraderList.map(_.organizationID))
        assetList <- getAssetList(completedNegotiationList.map(_.assetID))
      } yield Ok(views.html.component.master.traderViewCompletedNegotiationList(traderID = traderID, completedNegotiationList = completedNegotiationList, counterPartyTraderList = counterPartyTraderList, counterPartyOrganizationList = counterPartyOrganizationList, assetList = assetList))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def traderViewSentReceivedIncompleteRejectedFailedNegotiationList: Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
    Ok(views.html.component.master.traderViewSentReceivedIncompleteRejectedFailedNegotiationList())
  }

  def traderViewSentNegotiationRequestList: Action[AnyContent] = withTraderLoginAction { implicit loginState =>
    implicit request =>
      val traderID = masterTraders.Service.tryGetID(loginState.username)

      def getSentNegotiationRequestList(traderID: String): Future[Seq[Negotiation]] = masterNegotiations.Service.getAllSentNegotiationRequestListByTraderID(traderID)

      def getCounterPartyTraderList(traderIDs: Seq[String]): Future[Seq[Trader]] = masterTraders.Service.getTraders(traderIDs)

      def getCounterPartyOrganizationList(organizationIDs: Seq[String]) = masterOrganizations.Service.getOrganizations(organizationIDs)

      def getAssetList(assetIDs: Seq[String])= masterProperties.Service.getPropertyListMap(assetIDs)

      (for {
        traderID <- traderID
        sentNegotiationRequestList <- getSentNegotiationRequestList(traderID)
        counterPartyTraderList <- getCounterPartyTraderList(sentNegotiationRequestList.map(_.buyerTraderID))
        counterPartyOrganizationList <- getCounterPartyOrganizationList(counterPartyTraderList.map(_.organizationID))
        assetList <- getAssetList(sentNegotiationRequestList.map(_.assetID))
      } yield Ok(views.html.component.master.traderViewSentNegotiationRequestList(sentNegotiationRequestList = sentNegotiationRequestList, counterPartyTraderList = counterPartyTraderList, counterPartyOrganizationList = counterPartyOrganizationList, assetList = assetList))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def traderViewReceivedNegotiationRequestList: Action[AnyContent] = withTraderLoginAction { implicit loginState =>
    implicit request =>
      val traderID = masterTraders.Service.tryGetID(loginState.username)

      def getReceivedNegotiationList(traderID: String): Future[Seq[Negotiation]] = masterNegotiations.Service.getAllReceivedNegotiationListByTraderID(traderID)

      def getCounterPartyTraderList(traderIDs: Seq[String]): Future[Seq[Trader]] = masterTraders.Service.getTraders(traderIDs)

      def getCounterPartyOrganizationList(organizationIDs: Seq[String]) = masterOrganizations.Service.getOrganizations(organizationIDs)

      def getAssetList(assetIDs: Seq[String])= masterProperties.Service.getPropertyListMap(assetIDs)

      (for {
        traderID <- traderID
        receivedNegotiationList <- getReceivedNegotiationList(traderID)
        counterPartyTraderList <- getCounterPartyTraderList(receivedNegotiationList.map(_.sellerTraderID))
        counterPartyOrganizationList <- getCounterPartyOrganizationList(counterPartyTraderList.map(_.organizationID))
        assetList <- getAssetList(receivedNegotiationList.map(_.assetID))
      } yield Ok(views.html.component.master.traderViewReceivedNegotiationRequestList(receivedNegotiationRequestList = receivedNegotiationList, counterPartyTraderList = counterPartyTraderList, counterPartyOrganizationList = counterPartyOrganizationList, assetList = assetList))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def traderViewIncompleteNegotiationList: Action[AnyContent] = withTraderLoginAction { implicit loginState =>
    implicit request =>
      val traderID = masterTraders.Service.tryGetID(loginState.username)

      def getIncompleteNegotiationList(traderID: String): Future[Seq[Negotiation]] = masterNegotiations.Service.getAllIncompleteNegotiationListByTraderID(traderID)

      def getCounterPartyTraderList(traderIDs: Seq[String]): Future[Seq[Trader]] = masterTraders.Service.getTraders(traderIDs)

      def getCounterPartyOrganizationList(organizationIDs: Seq[String]) = masterOrganizations.Service.getOrganizations(organizationIDs)

      def getAssetList(assetIDs: Seq[String])= masterProperties.Service.getPropertyListMap(assetIDs)

      (for {
        traderID <- traderID
        incompleteNegotiationList <- getIncompleteNegotiationList(traderID)
        counterPartyTraderList <- getCounterPartyTraderList(incompleteNegotiationList.map(_.buyerTraderID))
        counterPartyOrganizationList <- getCounterPartyOrganizationList(counterPartyTraderList.map(_.organizationID))
        assetList <- getAssetList(incompleteNegotiationList.map(_.assetID))
      } yield Ok(views.html.component.master.traderViewIncompleteNegotiationList(incompleteNegotiationList = incompleteNegotiationList, counterPartyTraderList = counterPartyTraderList, counterPartyOrganizationList = counterPartyOrganizationList, assetList = assetList))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def traderViewRejectedAndFailedNegotiationList: Action[AnyContent] = withTraderLoginAction { implicit loginState =>
    implicit request =>
      val traderID = masterTraders.Service.tryGetID(loginState.username)

      def getRejectedReceivedNegotiationList(traderID: String): Future[Seq[Negotiation]] = masterNegotiations.Service.getAllRejectedNegotiationListByBuyerTraderID(traderID)

      def getRejectedSentNegotiationList(traderID: String): Future[Seq[Negotiation]] = masterNegotiations.Service.getAllRejectedNegotiationListBySellerTraderID(traderID)

      def getFailedNegotiationList(traderID: String): Future[Seq[Negotiation]] = masterNegotiations.Service.getAllFailedNegotiationListBySellerTraderID(traderID)

      def getCounterPartyTraderList(traderIDs: Seq[String]): Future[Seq[Trader]] = masterTraders.Service.getTraders(traderIDs)

      def getCounterPartyOrganizationList(organizationIDs: Seq[String]) = masterOrganizations.Service.getOrganizations(organizationIDs)

      def getAssetList(assetIDs: Seq[String])= masterProperties.Service.getPropertyListMap(assetIDs)

      (for {
        traderID <- traderID
        rejectedReceivedNegotiationList <- getRejectedReceivedNegotiationList(traderID)
        rejectedSentNegotiationList <- getRejectedSentNegotiationList(traderID)
        failedNegotiationList <- getFailedNegotiationList(traderID)
        counterPartyTraderList <- getCounterPartyTraderList(rejectedReceivedNegotiationList.map(_.sellerTraderID) ++ rejectedSentNegotiationList.map(_.buyerTraderID) ++ failedNegotiationList.map(_.buyerTraderID))
        counterPartyOrganizationList <- getCounterPartyOrganizationList(counterPartyTraderList.map(_.organizationID))
        assetList <- getAssetList((rejectedReceivedNegotiationList ++ rejectedSentNegotiationList ++ failedNegotiationList).map(_.assetID))
      } yield Ok(views.html.component.master.traderViewRejectedAndFailedNegotiationList(rejectedReceivedNegotiationList = rejectedReceivedNegotiationList, rejectedSentNegotiationList = rejectedSentNegotiationList, failedNegotiationList = failedNegotiationList, counterPartyTraderList = counterPartyTraderList, counterPartyOrganizationList = counterPartyOrganizationList, assetList = assetList))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def organizationViewAcceptedNegotiationList: Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
    Ok(views.html.component.master.organizationViewAcceptedNegotiationList())
  }

  def organizationViewAcceptedBuyNegotiationList: Action[AnyContent] = withOrganizationLoginAction { implicit loginState =>
    implicit request =>
      val organizationID = masterOrganizations.Service.tryGetID(loginState.username)

      def getOrganizationTradersIDList(organizationID: String) = masterTraders.Service.getTraderIDsByOrganizationID(organizationID)

      def getBuyNegotiationList(traderIDs: Seq[String]): Future[Seq[Negotiation]] = masterNegotiations.Service.getAllAcceptedBuyNegotiationListByTraderIDs(traderIDs)

      def getTraderList(traderIDs: Seq[String]) = masterTraders.Service.getTraders(traderIDs)

      def getCounterPartyTraderList(traderIDs: Seq[String]): Future[Seq[Trader]] = masterTraders.Service.getTraders(traderIDs)

      def getCounterPartyOrganizationList(organizationIDs: Seq[String]) = masterOrganizations.Service.getOrganizations(organizationIDs)

      def getAssetList(assetIDs: Seq[String])= masterProperties.Service.getPropertyListMap(assetIDs)

      (for {
        organizationID <- organizationID
        organizationTradersIDList <- getOrganizationTradersIDList(organizationID)
        buyNegotiationList <- getBuyNegotiationList(organizationTradersIDList)
        traderList <- getTraderList(organizationTradersIDList)
        counterPartyTraderList <- getCounterPartyTraderList(buyNegotiationList.map(_.sellerTraderID))
        counterPartyOrganizationList <- getCounterPartyOrganizationList(counterPartyTraderList.map(_.organizationID))
        assetList <- getAssetList(buyNegotiationList.map(_.assetID))
      } yield Ok(views.html.component.master.organizationViewAcceptedBuyNegotiationList(buyNegotiationList = buyNegotiationList, traderList = traderList, counterPartyTraderList = counterPartyTraderList, counterPartyOrganizationList = counterPartyOrganizationList, assetList = assetList))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def organizationViewAcceptedSellNegotiationList: Action[AnyContent] = withOrganizationLoginAction { implicit loginState =>
    implicit request =>
      val organizationID = masterOrganizations.Service.tryGetID(loginState.username)

      def getOrganizationTradersIDList(organizationID: String) = masterTraders.Service.getTraderIDsByOrganizationID(organizationID)

      def getSellNegotiationList(traderIDs: Seq[String]): Future[Seq[Negotiation]] = masterNegotiations.Service.getAllAcceptedSellNegotiationListByTraderIDs(traderIDs)

      def getTraderList(traderIDs: Seq[String]) = masterTraders.Service.getTraders(traderIDs)

      def getCounterPartyTraderList(traderIDs: Seq[String]): Future[Seq[Trader]] = masterTraders.Service.getTraders(traderIDs)

      def getCounterPartyOrganizationList(organizationIDs: Seq[String]) = masterOrganizations.Service.getOrganizations(organizationIDs)

      def getAssetList(assetIDs: Seq[String])= masterProperties.Service.getPropertyListMap(assetIDs)

      (for {
        organizationID <- organizationID
        organizationTradersIDList <- getOrganizationTradersIDList(organizationID)
        sellNegotiationList <- getSellNegotiationList(organizationTradersIDList)
        traderList <- getTraderList(organizationTradersIDList)
        counterPartyTraderList <- getCounterPartyTraderList(sellNegotiationList.map(_.buyerTraderID))
        counterPartyOrganizationList <- getCounterPartyOrganizationList(counterPartyTraderList.map(_.organizationID))
        assetList <- getAssetList(sellNegotiationList.map(_.assetID))
      } yield Ok(views.html.component.master.organizationViewAcceptedSellNegotiationList(sellNegotiationList = sellNegotiationList, traderList = traderList, counterPartyTraderList = counterPartyTraderList, counterPartyOrganizationList = counterPartyOrganizationList, assetList = assetList))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def organizationViewCompletedNegotiationList: Action[AnyContent] = withOrganizationLoginAction { implicit loginState =>
    implicit request =>
      val organizationID = masterOrganizations.Service.tryGetID(loginState.username)

      def getOrganizationTradersIDList(organizationID: String) = masterTraders.Service.getTraderIDsByOrganizationID(organizationID)

      def getCompletedNegotiationList(traderIDs: Seq[String]): Future[Seq[NegotiationHistory]] = masterNegotiationHistories.Service.getAllCompletedNegotiationListByTraderIDs(traderIDs)

      def getTraderList(traderIDs: Seq[String]) = masterTraders.Service.getTraders(traderIDs)

      def getCounterPartyTraderList(traderIDs: Seq[String]): Future[Seq[Trader]] = masterTraders.Service.getTraders(traderIDs)

      def getCounterPartyOrganizationList(organizationIDs: Seq[String]) = masterOrganizations.Service.getOrganizations(organizationIDs)

      def getAssetList(assetIDs: Seq[String]) = masterProperties.Service.getPropertyListMap(assetIDs)

      (for {
        organizationID <- organizationID
        organizationTradersIDList <- getOrganizationTradersIDList(organizationID)
        completedNegotiationList <- getCompletedNegotiationList(organizationTradersIDList)
        traderList <- getTraderList(organizationTradersIDList)
        counterPartyTraderList <- getCounterPartyTraderList(completedNegotiationList.map(negotiation => if (traderList.map(_.id) contains negotiation.sellerTraderID) negotiation.buyerTraderID else negotiation.sellerTraderID))
        counterPartyOrganizationList <- getCounterPartyOrganizationList(counterPartyTraderList.map(_.organizationID))
        assetList <- getAssetList(completedNegotiationList.map(_.assetID))
      } yield Ok(views.html.component.master.organizationViewCompletedNegotiationList(completedNegotiationList = completedNegotiationList, traderList = traderList, counterPartyTraderList = counterPartyTraderList, counterPartyOrganizationList = counterPartyOrganizationList, assetList = assetList))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def organizationViewSentReceivedIncompleteRejectedFailedNegotiationList: Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
    Ok(views.html.component.master.organizationViewSentReceivedIncompleteRejectedFailedNegotiationList())
  }

  def organizationViewSentNegotiationRequestList: Action[AnyContent] = withOrganizationLoginAction { implicit loginState =>
    implicit request =>

      val organizationID = masterOrganizations.Service.tryGetID(loginState.username)

      def getOrganizationTradersIDList(organizationID: String) = masterTraders.Service.getTraderIDsByOrganizationID(organizationID)

      def getSentNegotiationRequestList(traderIDs: Seq[String]): Future[Seq[Negotiation]] = masterNegotiations.Service.getAllSentNegotiationRequestListByTraderIDs(traderIDs)

      def getTraderList(traderIDs: Seq[String]) = masterTraders.Service.getTraders(traderIDs)

      def getCounterPartyTraderList(traderIDs: Seq[String]): Future[Seq[Trader]] = masterTraders.Service.getTraders(traderIDs)

      def getCounterPartyOrganizationList(organizationIDs: Seq[String]) = masterOrganizations.Service.getOrganizations(organizationIDs)

      def getAssetList(assetIDs: Seq[String])= masterProperties.Service.getPropertyListMap(assetIDs)

      (for {
        organizationID <- organizationID
        organizationTradersIDList <- getOrganizationTradersIDList(organizationID)
        sentNegotiationRequestList <- getSentNegotiationRequestList(organizationTradersIDList)
        traderList <- getTraderList(organizationTradersIDList)
        counterPartyTraderList <- getCounterPartyTraderList(sentNegotiationRequestList.map(_.buyerTraderID))
        counterPartyOrganizationList <- getCounterPartyOrganizationList(counterPartyTraderList.map(_.organizationID))
        assetList <- getAssetList(sentNegotiationRequestList.map(_.assetID))
      } yield Ok(views.html.component.master.organizationViewSentNegotiationRequestList(sentNegotiationRequestList = sentNegotiationRequestList, traderList = traderList, counterPartyTraderList = counterPartyTraderList, counterPartyOrganizationList = counterPartyOrganizationList, assetList = assetList))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def organizationViewReceivedNegotiationList: Action[AnyContent] = withOrganizationLoginAction { implicit loginState =>
    implicit request =>
      val organizationID = masterOrganizations.Service.tryGetID(loginState.username)

      def getOrganizationTradersIDList(organizationID: String) = masterTraders.Service.getTraderIDsByOrganizationID(organizationID)

      def getReceivedNegotiationList(traderIDs: Seq[String]): Future[Seq[Negotiation]] = masterNegotiations.Service.getAllReceivedNegotiationListByTraderIDs(traderIDs)

      def getTraderList(traderIDs: Seq[String]) = masterTraders.Service.getTraders(traderIDs)

      def getCounterPartyTraderList(traderIDs: Seq[String]): Future[Seq[Trader]] = masterTraders.Service.getTraders(traderIDs)

      def getCounterPartyOrganizationList(organizationIDs: Seq[String]) = masterOrganizations.Service.getOrganizations(organizationIDs)

      def getAssetList(assetIDs: Seq[String])= masterProperties.Service.getPropertyListMap(assetIDs)

      (for {
        organizationID <- organizationID
        organizationTradersIDList <- getOrganizationTradersIDList(organizationID)
        receivedNegotiationList <- getReceivedNegotiationList(organizationTradersIDList)
        traderList <- getTraderList(organizationTradersIDList)
        counterPartyTraderList <- getCounterPartyTraderList(receivedNegotiationList.map(_.sellerTraderID))
        counterPartyOrganizationList <- getCounterPartyOrganizationList(counterPartyTraderList.map(_.organizationID))
        assetList <- getAssetList(receivedNegotiationList.map(_.assetID))
      } yield Ok(views.html.component.master.organizationViewReceivedNegotiationList(receivedNegotiationList = receivedNegotiationList, traderList = traderList, counterPartyTraderList = counterPartyTraderList, counterPartyOrganizationList = counterPartyOrganizationList, assetList = assetList))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def organizationViewIncompleteNegotiationList: Action[AnyContent] = withOrganizationLoginAction { implicit loginState =>
    implicit request =>
      val organizationID = masterOrganizations.Service.tryGetID(loginState.username)

      def getOrganizationTradersIDList(organizationID: String) = masterTraders.Service.getTraderIDsByOrganizationID(organizationID)

      def getIncompleteNegotiationList(traderIDs: Seq[String]): Future[Seq[Negotiation]] = masterNegotiations.Service.getAllIncompleteNegotiationListByTraderIDs(traderIDs)

      def getTraderList(traderIDs: Seq[String]) = masterTraders.Service.getTraders(traderIDs)

      def getCounterPartyTraderList(traderIDs: Seq[String]): Future[Seq[Trader]] = masterTraders.Service.getTraders(traderIDs)

      def getCounterPartyOrganizationList(organizationIDs: Seq[String]) = masterOrganizations.Service.getOrganizations(organizationIDs)

      def getAssetList(assetIDs: Seq[String])= masterProperties.Service.getPropertyListMap(assetIDs)

      (for {
        organizationID <- organizationID
        organizationTradersIDList <- getOrganizationTradersIDList(organizationID)
        incompleteNegotiationList <- getIncompleteNegotiationList(organizationTradersIDList)
        traderList <- getTraderList(organizationTradersIDList)
        counterPartyTraderList <- getCounterPartyTraderList(incompleteNegotiationList.map(_.buyerTraderID))
        counterPartyOrganizationList <- getCounterPartyOrganizationList(counterPartyTraderList.map(_.organizationID))
        assetList <- getAssetList(incompleteNegotiationList.map(_.assetID))
      } yield Ok(views.html.component.master.organizationViewIncompleteNegotiationList(incompleteNegotiationList = incompleteNegotiationList, traderList = traderList, counterPartyTraderList = counterPartyTraderList, counterPartyOrganizationList = counterPartyOrganizationList, assetList = assetList))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def organizationViewRejectedAndFailedNegotiationList: Action[AnyContent] = withOrganizationLoginAction { implicit loginState =>
    implicit request =>
      val organizationID = masterOrganizations.Service.tryGetID(loginState.username)

      def getOrganizationTradersIDList(organizationID: String) = masterTraders.Service.getTraderIDsByOrganizationID(organizationID)

      def getRejectedReceivedNegotiationList(traderIDs: Seq[String]): Future[Seq[Negotiation]] = masterNegotiations.Service.getAllRejectedNegotiationListByBuyerTraderIDs(traderIDs)

      def getRejectedSentNegotiationList(traderIDs: Seq[String]): Future[Seq[Negotiation]] = masterNegotiations.Service.getAllRejectedNegotiationListBySellerTraderIDs(traderIDs)

      def getFailedNegotiationList(traderIDs: Seq[String]): Future[Seq[Negotiation]] = masterNegotiations.Service.getAllFailedNegotiationListBySellerTraderIDs(traderIDs)

      def getTraderList(traderIDs: Seq[String]): Future[Seq[Trader]] = masterTraders.Service.getTraders(traderIDs)

      def getCounterPartyTraderList(traderIDs: Seq[String]): Future[Seq[Trader]] = masterTraders.Service.getTraders(traderIDs)

      def getCounterPartyOrganizationList(organizationIDs: Seq[String]) = masterOrganizations.Service.getOrganizations(organizationIDs)

      def getAssetList(assetIDs: Seq[String])= masterProperties.Service.getPropertyListMap(assetIDs)

      (for {
        organizationID <- organizationID
        organizationTradersIDList <- getOrganizationTradersIDList(organizationID)
        rejectedReceivedNegotiationList <- getRejectedReceivedNegotiationList(organizationTradersIDList)
        rejectedSentNegotiationList <- getRejectedSentNegotiationList(organizationTradersIDList)
        failedNegotiationList <- getFailedNegotiationList(organizationTradersIDList)
        traderList <- getTraderList(organizationTradersIDList)
        counterPartyTraderList <- getCounterPartyTraderList(rejectedReceivedNegotiationList.map(_.sellerTraderID) ++ rejectedSentNegotiationList.map(_.buyerTraderID) ++ failedNegotiationList.map(_.buyerTraderID))
        counterPartyOrganizationList <- getCounterPartyOrganizationList(counterPartyTraderList.map(_.organizationID))
        assetList <- getAssetList((rejectedReceivedNegotiationList ++ rejectedSentNegotiationList ++ failedNegotiationList).map(_.assetID))
      } yield Ok(views.html.component.master.organizationViewRejectedAndFailedNegotiationList(rejectedReceivedNegotiationList = rejectedReceivedNegotiationList, rejectedSentNegotiationList = rejectedSentNegotiationList, failedNegotiationList = failedNegotiationList, traderList = traderList, counterPartyTraderList = counterPartyTraderList, counterPartyOrganizationList = counterPartyOrganizationList, assetList = assetList))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def recentActivities: Action[AnyContent] = withLoginActionAsync { implicit loginState =>
    implicit request =>
      Future(Ok(views.html.component.master.recentActivities()))
  }

  def tradeActivities(negotiationID: String): Action[AnyContent] = withLoginActionAsync { implicit loginState =>
    implicit request =>
      Future(Ok(views.html.component.master.tradeActivities(negotiationID)))
  }

  def completedTradeActivities(negotiationID: String): Action[AnyContent] = withLoginActionAsync { implicit loginState =>
    implicit request =>
      Future(Ok(views.html.component.master.completedTradeActivities(negotiationID)))
  }

  def profilePicture(): Action[AnyContent] = withLoginActionAsync { implicit loginState =>
    implicit request =>
      val profilePicture = masterAccountFiles.Service.getProfilePicture(loginState.username)
      (for {
        profilePicture <- profilePicture
      } yield Ok(views.html.profilePicture(profilePicture))
        ).recover {
        case _: BaseException => InternalServerError(views.html.profilePicture())
      }
  }

  def organizationViewTraderAccountList(): Action[AnyContent] = withOrganizationLoginAction { implicit loginState =>
    implicit request =>
      Future(Ok(views.html.component.master.organizationViewTraderAccountList()))
  }

  def organizationViewAcceptedTraderAccountList(): Action[AnyContent] = withOrganizationLoginAction { implicit loginState =>
    implicit request =>
      val organizationID = masterOrganizations.Service.tryGetVerifiedOrganizationID(loginState.username)

      def acceptedTraders(organizationID: String): Future[Seq[Trader]] = masterTraders.Service.getOrganizationAcceptedTraderList(organizationID)

      (for {
        organizationID <- organizationID
        acceptedTraders <- acceptedTraders(organizationID)
      } yield Ok(views.html.component.master.organizationViewAcceptedTraderAccountList(acceptedTraders))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def organizationViewAcceptedTraderAccount(traderID: String): Action[AnyContent] = withOrganizationLoginAction { implicit loginState =>
    implicit request =>
      val organizationID = masterOrganizations.Service.tryGetVerifiedOrganizationID(loginState.username)
      val trader = masterTraders.Service.tryGet(traderID)

      (for {
        organizationID <- organizationID
        trader <- trader
      } yield Ok(views.html.component.master.organizationViewAcceptedTraderAccount(trader = trader))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def organizationViewPendingTraderRequestList(): Action[AnyContent] = withOrganizationLoginAction { implicit loginState =>
    implicit request =>
      val organizationID = masterOrganizations.Service.tryGetID(loginState.username)

      def pendingTraderRequests(organizationID: String): Future[Seq[Trader]] = masterTraders.Service.getOrganizationPendingTraderRequestList(organizationID)

      (for {
        organizationID <- organizationID
        pendingTraderRequests <- pendingTraderRequests(organizationID)
      } yield Ok(views.html.component.master.organizationViewPendingTraderRequestList(pendingTraderRequests))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def organizationViewPendingTraderRequest(traderID: String): Action[AnyContent] = withOrganizationLoginAction { implicit loginState =>
    implicit request =>
      val organizationID = masterOrganizations.Service.tryGetVerifiedOrganizationID(loginState.username)
      val trader = masterTraders.Service.tryGet(traderID)

      (for {
        organizationID <- organizationID
        trader <- trader
      } yield Ok(views.html.component.master.organizationViewPendingTraderRequest(trader = trader))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def organizationViewRejectedTraderRequestList(): Action[AnyContent] = withOrganizationLoginAction { implicit loginState =>
    implicit request =>
      val organizationID = masterOrganizations.Service.tryGetID(loginState.username)

      def rejectedTraderRequests(organizationID: String): Future[Seq[Trader]] = masterTraders.Service.getOrganizationRejectedTraderRequestList(organizationID)

      (for {
        organizationID <- organizationID
        rejectedTraderRequests <- rejectedTraderRequests(organizationID)
      } yield Ok(views.html.component.master.organizationViewRejectedTraderRequestList(rejectedTraderRequests))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def organizationViewRejectedTraderRequest(traderID: String): Action[AnyContent] = withOrganizationLoginAction { implicit loginState =>
    implicit request =>
      val organizationID = masterOrganizations.Service.tryGetVerifiedOrganizationID(loginState.username)
      val trader = masterTraders.Service.tryGet(traderID)

      (for {
        organizationID <- organizationID
        trader <- trader
      } yield Ok(views.html.component.master.organizationViewRejectedTraderRequest(trader = trader))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def zoneViewTraderAccountList(): Action[AnyContent] = withZoneLoginAction { implicit loginState =>
    implicit request =>
      Future(Ok(views.html.component.master.zoneViewTraderAccountList()))
  }

  def zoneViewAcceptedTraderAccountList(): Action[AnyContent] = withZoneLoginAction { implicit loginState =>
    implicit request =>
      val zoneID = masterZones.Service.tryGetID(loginState.username)

      def acceptedTraders(zoneID: String): Future[Seq[Trader]] = masterTraders.Service.getZoneAcceptedTraderList(zoneID)

      (for {
        zoneID <- zoneID
        acceptedTraders <- acceptedTraders(zoneID)
      } yield Ok(views.html.component.master.zoneViewAcceptedTraderAccountList(acceptedTraders))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def zoneViewAcceptedTraderAccount(traderID: String): Action[AnyContent] = withZoneLoginAction { implicit loginState =>
    implicit request =>
      val zoneID = masterZones.Service.tryGetID(loginState.username)
      val trader = masterTraders.Service.tryGet(traderID)

      def organization(organizationID: String): Future[Organization] = masterOrganizations.Service.tryGet(organizationID)

      (for {
        zoneID <- zoneID
        trader <- trader
        organization <- organization(trader.organizationID)
      } yield Ok(views.html.component.master.zoneViewAcceptedTraderAccount(trader = trader, organization = organization))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def zoneViewPendingTraderRequestList(): Action[AnyContent] = withZoneLoginAction { implicit loginState =>
    implicit request =>
      val zoneID = masterZones.Service.tryGetID(loginState.username)

      def pendingTraderRequests(zoneID: String): Future[Seq[Trader]] = masterTraders.Service.getZonePendingTraderRequestList(zoneID)

      (for {
        zoneID <- zoneID
        pendingTraderRequests <- pendingTraderRequests(zoneID)
      } yield Ok(views.html.component.master.zoneViewPendingTraderRequestList(pendingTraderRequests))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def zoneViewPendingTraderRequest(traderID: String): Action[AnyContent] = withZoneLoginAction { implicit loginState =>
    implicit request =>
      val zoneID = masterZones.Service.tryGetID(loginState.username)
      val trader = masterTraders.Service.tryGet(traderID)

      def organization(organizationID: String): Future[Organization] = masterOrganizations.Service.tryGet(organizationID)

      (for {
        zoneID <- zoneID
        trader <- trader
        organization <- organization(trader.organizationID)
      } yield Ok(views.html.component.master.zoneViewPendingTraderRequest(trader = trader, organization = organization))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def zoneViewRejectedTraderRequestList(): Action[AnyContent] = withZoneLoginAction { implicit loginState =>
    implicit request =>
      val zoneID = masterZones.Service.tryGetID(loginState.username)

      def rejectedTraderRequests(zoneID: String): Future[Seq[Trader]] = masterTraders.Service.getZoneRejectedTraderRequestList(zoneID)

      (for {
        zoneID <- zoneID
        rejectedTraderRequests <- rejectedTraderRequests(zoneID)
      } yield Ok(views.html.component.master.zoneViewRejectedTraderRequestList(rejectedTraderRequests))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def zoneViewRejectedTraderRequest(traderID: String): Action[AnyContent] = withZoneLoginAction { implicit loginState =>
    implicit request =>
      val zoneID = masterZones.Service.tryGetID(loginState.username)
      val trader = masterTraders.Service.tryGet(traderID)

      def organization(organizationID: String): Future[Organization] = masterOrganizations.Service.tryGet(organizationID)

      (for {
        zoneID <- zoneID
        trader <- trader
        organization <- organization(trader.organizationID)
      } yield Ok(views.html.component.master.zoneViewRejectedTraderRequest(trader = trader, organization = organization))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def zoneViewOrganizationAccountList(): Action[AnyContent] = withZoneLoginAction { implicit loginState =>
    implicit request =>
      Future(Ok(views.html.component.master.zoneViewOrganizationAccountList()))
  }

  def zoneViewAcceptedOrganizationAccountList(): Action[AnyContent] = withZoneLoginAction { implicit loginState =>
    implicit request =>
      val zoneID = masterZones.Service.tryGetID(loginState.username)

      def acceptedOrganizations(zoneID: String): Future[Seq[Organization]] = masterOrganizations.Service.getZoneAcceptedOrganizationList(zoneID)

      (for {
        zoneID <- zoneID
        acceptedOrganizations <- acceptedOrganizations(zoneID)
      } yield Ok(views.html.component.master.zoneViewAcceptedOrganizationAccountList(acceptedOrganizations))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def zoneViewAcceptedOrganizationAccount(organizationID: String): Action[AnyContent] = withZoneLoginAction { implicit loginState =>
    implicit request =>
      val zoneID = masterZones.Service.tryGetID(loginState.username)
      val organization = masterOrganizations.Service.tryGet(organizationID)
      val ubos = masterOrganizationUBOs.Service.getUBOs(organizationID)

      def getOrganizationKYCs(zoneID: String, organization: Organization): Future[Seq[OrganizationKYC]] = if (organization.zoneID == zoneID) {
        masterOrganizationKYCs.Service.getAllDocuments(organization.id)
      } else {
        throw new BaseException(constants.Response.UNAUTHORIZED)
      }

      (for {
        zoneID <- zoneID
        organization <- organization
        ubos <- ubos
        organizationKYCs <- getOrganizationKYCs(zoneID = zoneID, organization = organization)
      } yield Ok(views.html.component.master.zoneViewAcceptedOrganizationAccount(organization = organization, organizationKYCs = organizationKYCs, ubos = ubos))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def zoneViewPendingOrganizationRequestList(): Action[AnyContent] = withZoneLoginAction { implicit loginState =>
    implicit request =>
      val zoneID = masterZones.Service.tryGetID(loginState.username)

      def pendingOrganizationRequests(zoneID: String): Future[Seq[Organization]] = masterOrganizations.Service.getZonePendingOrganizationRequestList(zoneID)

      (for {
        zoneID <- zoneID
        pendingOrganizationRequests <- pendingOrganizationRequests(zoneID)
      } yield Ok(views.html.component.master.zoneViewPendingOrganizationRequestList(pendingOrganizationRequests))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def zoneViewPendingOrganizationRequest(organizationID: String): Action[AnyContent] = withZoneLoginAction { implicit loginState =>
    implicit request =>
      val zoneID = masterZones.Service.tryGetID(loginState.username)
      val organization = masterOrganizations.Service.tryGet(organizationID)
      val ubos = masterOrganizationUBOs.Service.getUBOs(organizationID)

      def getOrganizationKYCs(zoneID: String, organization: Organization): Future[Seq[OrganizationKYC]] = if (organization.zoneID == zoneID) {
        masterOrganizationKYCs.Service.getAllDocuments(organization.id)
      } else {
        throw new BaseException(constants.Response.UNAUTHORIZED)
      }

      (for {
        zoneID <- zoneID
        organization <- organization
        ubos <- ubos
        organizationKYCs <- getOrganizationKYCs(zoneID = zoneID, organization = organization)
      } yield Ok(views.html.component.master.zoneViewPendingOrganizationRequest(organization = organization, organizationKYCs = organizationKYCs, ubos = ubos))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def zoneViewRejectedOrganizationRequestList(): Action[AnyContent] = withZoneLoginAction { implicit loginState =>
    implicit request =>
      val zoneID = masterZones.Service.tryGetID(loginState.username)

      def rejectedOrganizationRequests(zoneID: String): Future[Seq[Organization]] = masterOrganizations.Service.getZoneRejectedOrganizationRequestList(zoneID)

      (for {
        zoneID <- zoneID
        rejectedOrganizationRequests <- rejectedOrganizationRequests(zoneID)
      } yield Ok(views.html.component.master.zoneViewRejectedOrganizationRequestList(rejectedOrganizationRequests))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def zoneViewRejectedOrganizationRequest(organizationID: String): Action[AnyContent] = withZoneLoginAction { implicit loginState =>
    implicit request =>
      val zoneID = masterZones.Service.tryGetID(loginState.username)
      val organization = masterOrganizations.Service.tryGet(organizationID)

      def ubos(organizationID: String) = masterOrganizationUBOs.Service.getUBOs(organizationID)

      def getOrganizationKYCs(zoneID: String, organization: Organization): Future[Seq[OrganizationKYC]] = if (organization.zoneID == zoneID) {
        masterOrganizationKYCs.Service.getAllDocuments(organization.id)
      } else {
        throw new BaseException(constants.Response.UNAUTHORIZED)
      }

      (for {
        zoneID <- zoneID
        organization <- organization
        organizationKYCs <- getOrganizationKYCs(zoneID = zoneID, organization = organization)
        ubos <- ubos(organization.id)
      } yield Ok(views.html.component.master.zoneViewRejectedOrganizationRequest(organization = organization, organizationKYCs = organizationKYCs, ubos = ubos))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def organizationSubscription(): Action[AnyContent] = withOrganizationLoginAction { implicit loginState =>
    implicit request =>
      Future(Ok(views.html.component.master.organizationSubscription()))
  }

  def traderSubscription(): Action[AnyContent] = withTraderLoginAction { implicit loginState =>
    implicit request =>
      Future(Ok(views.html.component.master.traderSubscription()))
  }

  def identification: Action[AnyContent] = withLoginActionAsync { implicit loginState =>
    implicit request =>
      val accountKYC = masterAccountKYCs.Service.get(loginState.username, constants.File.AccountKYC.IDENTIFICATION)
      val identification = masterIdentifications.Service.get(loginState.username)
      for {
        accountKYC <- accountKYC
        identification <- identification
      } yield Ok(views.html.component.master.identification(identification = identification, accountKYC = accountKYC))
  }

  def userViewPendingRequests: Action[AnyContent] = withUserLoginAction { implicit loginState =>
    implicit request =>
      val mobile: Future[Option[Mobile]] = masterMobiles.Service.get(loginState.username)
      val email: Future[Option[Email]] = masterEmails.Service.get(loginState.username)
      val identification: Future[Option[Identification]] = masterIdentifications.Service.get(loginState.username)

      def getOrganizationZone(organization: Option[Organization]): Future[Option[Zone]] = if (organization.isDefined) masterZones.Service.get(organization.get.zoneID) else Future(None)

      def getTraderOrganization(trader: Option[Trader]): Future[Option[Organization]] = if (trader.isDefined) masterOrganizations.Service.getOrNone(trader.get.organizationID) else Future(None)

      def getOrganizationKYCsByOrganization(organization: Option[Organization]): Future[Seq[OrganizationKYC]] = if (organization.isDefined) masterOrganizationKYCs.Service.getAllDocuments(organization.get.id) else Future(Seq[OrganizationKYC]())

      def getTraderByAccountID(accountID: String): Future[Option[Trader]] = masterTraders.Service.getByAccountID(accountID)

      def getOrganizationOrNoneByAccountID(accountID: String): Future[Option[Organization]] = masterOrganizations.Service.getByAccountID(accountID)

      def getUserResult(identification: Option[Identification], contactStatus: Seq[String]): Future[Result] = {
        val identificationStatus = if (identification.isDefined) identification.get.verificationStatus.getOrElse(false) else false
        for {
          trader <- getTraderByAccountID(loginState.username)
          traderOrganization <- getTraderOrganization(trader)
          organization <- getOrganizationOrNoneByAccountID(loginState.username)
          organizationZone <- getOrganizationZone(organization)
          organizationKYCs <- getOrganizationKYCsByOrganization(organization)
        } yield Ok(views.html.component.master.userViewPendingRequests(identification = identification, contactStatus = contactStatus, organizationZone = organizationZone, organization = organization, organizationKYCs = organizationKYCs, traderOrganization = traderOrganization, trader = trader))
      }

      (for {
        mobile <- mobile
        email <- email
        identification <- identification
        result <- getUserResult(identification, utilities.Contact.getStatus(mobile, email))
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def traderViewOrganization: Action[AnyContent] = withTraderLoginAction { implicit loginState =>
    implicit request =>
      val trader: Future[Trader] = masterTraders.Service.tryGetByAccountID(loginState.username)

      def getOrganizationByID(id: String): Future[Organization] = masterOrganizations.Service.tryGet(id)

      (for {
        trader <- trader
        traderOrganization <- getOrganizationByID(trader.organizationID)
      } yield Ok(views.html.component.master.traderViewOrganization(traderOrganization))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def organization: Action[AnyContent] = withOrganizationLoginAction { implicit loginState =>
    implicit request =>
      val organization: Future[Organization] = masterOrganizations.Service.tryGetByAccountID(loginState.username)

      def getZone(zoneID: String): Future[Zone] = masterZones.Service.tryGet(zoneID)

      def getOrganizationKYCs(id: String): Future[Seq[OrganizationKYC]] = masterOrganizationKYCs.Service.getAllDocuments(id)

      (for {
        organization <- organization
        organizationZone <- getZone(organization.zoneID)
        organizationKYCs <- getOrganizationKYCs(organization.id)
      } yield Ok(views.html.component.master.organization(organizationZone = organizationZone, organization = organization, organizationKYCs = organizationKYCs))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def traderRelationList(): Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
    Ok(views.html.component.master.traderRelationList())
  }

  def acceptedTraderRelationList(): Action[AnyContent] = withTraderLoginAction { implicit loginState =>
    implicit request =>
      val traderID: Future[String] = masterTraders.Service.tryGetID(loginState.username)

      def acceptedTraderRelations(traderID: String): Future[Seq[TraderRelation]] = masterTraderRelations.Service.getAllAcceptedTraderRelation(traderID)

      (for {
        traderID <- traderID
        acceptedTraderRelations <- acceptedTraderRelations(traderID)
      } yield Ok(views.html.component.master.acceptedTraderRelationList(acceptedTraderRelationList = acceptedTraderRelations))).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def pendingTraderRelationList(): Action[AnyContent] = withTraderLoginAction { implicit loginState =>
    implicit request =>
      val traderID: Future[String] = masterTraders.Service.tryGetID(loginState.username)

      def receivedPendingTraderRelations(traderID: String): Future[Seq[TraderRelation]] = masterTraderRelations.Service.getAllReceivedPendingTraderRelation(traderID)

      def sentPendingTraderRelations(traderID: String): Future[Seq[TraderRelation]] = masterTraderRelations.Service.getAllSentPendingTraderRelation(traderID)

      (for {
        traderID <- traderID
        receivedPendingTraderRelations <- receivedPendingTraderRelations(traderID)
        sentPendingTraderRelations <- sentPendingTraderRelations(traderID)
      } yield Ok(views.html.component.master.pendingTraderRelationList(sentPendingTraderRelations = sentPendingTraderRelations, receivedPendingTraderRelations = receivedPendingTraderRelations))).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def acceptedTraderRelation(fromID: String, toID: String): Action[AnyContent] = withTraderLoginAction {
    implicit loginState =>
      implicit request =>
        val fromTrader = masterTraders.Service.tryGet(fromID)
        val toTrader = masterTraders.Service.tryGet(toID)

        def getResult(fromTrader: Trader, toTrader: Trader): Future[Result] = {
          def getOrganizationName(organizationID: String): Future[String] = masterOrganizations.Service.getNameByID(organizationID)

          loginState.username match {
            case fromTrader.accountID => {
              for {
                organizationName <- getOrganizationName(toTrader.organizationID)
              } yield Ok(views.html.component.master.acceptedTraderRelation(accountID = toTrader.accountID, organizationName = organizationName))
            }
            case toTrader.accountID => {
              for {
                organizationName <- getOrganizationName(fromTrader.organizationID)
              } yield Ok(views.html.component.master.acceptedTraderRelation(accountID = fromTrader.accountID, organizationName = organizationName))
            }
            case _ => throw new BaseException(constants.Response.UNAUTHORIZED)
          }
        }

        (for {
          fromTrader <- fromTrader
          toTrader <- toTrader
          result <- getResult(fromTrader = fromTrader, toTrader = toTrader)
        } yield result).recover {
          case baseException: BaseException => InternalServerError(baseException.failure.message)
        }
  }

  def pendingSentTraderRelation(toID: String): Action[AnyContent] = withTraderLoginAction {
    implicit loginState =>
      implicit request =>
        val trader = masterTraders.Service.tryGet(toID)

        def getOrganizationName(organizationID: String): Future[String] = masterOrganizations.Service.getNameByID(organizationID)

        (for {
          trader <- trader
          organizationName <- getOrganizationName(trader.organizationID)
        } yield Ok(views.html.component.master.pendingSentTraderRelation(accountID = trader.accountID, organizationName = organizationName))).recover {
          case baseException: BaseException => InternalServerError(baseException.failure.message)
        }
  }

  def pendingReceivedTraderRelation(fromID: String): Action[AnyContent] = withTraderLoginAction {
    implicit loginState =>
      implicit request =>
        val fromTrader = masterTraders.Service.tryGet(fromID)
        val toTrader = masterTraders.Service.tryGetByAccountID(loginState.username)

        def traderRelation(fromId: String, toId: String): Future[TraderRelation] = masterTraderRelations.Service.tryGet(fromID = fromId, toID = toId)

        def getOrganizationName(organizationID: String): Future[String] = masterOrganizations.Service.getNameByID(organizationID)

        (for {
          fromTrader <- fromTrader
          toTrader <- toTrader
          traderRelation <- traderRelation(fromId = fromTrader.id, toId = toTrader.id)
          organizationName <- getOrganizationName(fromTrader.organizationID)
        } yield Ok(views.html.component.master.pendingReceivedTraderRelation(traderRelation = traderRelation, counterPartyAccountID = fromTrader.accountID, organizationName = organizationName))).recover {
          case baseException: BaseException => ServiceUnavailable(Html(baseException.failure.message))
        }
  }

  def zoneViewOrganizationBankAccount(organizationID: String): Action[AnyContent] = withZoneLoginAction { implicit loginState =>
    implicit request =>
      val organizationZoneID = masterOrganizations.Service.tryGetZoneID(organizationID)
      val zoneID = masterZones.Service.tryGetID(loginState.username)

      def organizationBankAccountDetail(organizationZoneID: String, zoneID: String): Future[Option[OrganizationBankAccountDetail]] = if (organizationZoneID == zoneID) {
        masterOrganizationBankAccountDetails.Service.get(organizationID)
      } else {
        throw new BaseException(constants.Response.UNAUTHORIZED)
      }

      (for {
        organizationZoneID <- organizationZoneID
        zoneID <- zoneID
        organizationBankAccountDetail <- organizationBankAccountDetail(organizationZoneID = organizationZoneID, zoneID = zoneID)
      } yield Ok(views.html.component.master.zoneViewOrganizationBankAccount(organizationID, organizationBankAccountDetail))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def organizationBankAccount(): Action[AnyContent] = withOrganizationLoginAction { implicit loginState =>
    implicit request =>
      val organizationID = masterOrganizations.Service.tryGetID(loginState.username)

      def organizationBankAccountDetail(organizationID: String): Future[Option[OrganizationBankAccountDetail]] = masterOrganizationBankAccountDetails.Service.get(organizationID)

      (for {
        organizationID <- organizationID
        organizationBankAccountDetail <- organizationBankAccountDetail(organizationID)
      } yield Ok(views.html.component.master.organizationBankAccount(organizationBankAccountDetail))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def traderViewOrganizationBankAccount(): Action[AnyContent] = withTraderLoginAction { implicit loginState =>
    implicit request =>
      val organizationID = masterTraders.Service.getOrganizationIDByAccountID(loginState.username)

      def organizationBankAccountDetail(organizationID: String): Future[Option[OrganizationBankAccountDetail]] = masterOrganizationBankAccountDetails.Service.get(organizationID)

      (for {
        organizationID <- organizationID
        organizationBankAccountDetail <- organizationBankAccountDetail(organizationID)
      } yield Ok(views.html.component.master.traderViewOrganizationBankAccount(organizationBankAccountDetail))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def userViewOrganizationUBOs(): Action[AnyContent] = withUserLoginAction { implicit loginState =>
    implicit request =>
      val organization = masterOrganizations.Service.getByAccountID(loginState.username)

      def ubos(organization: Option[Organization]) = organization match {
        case Some(organization) => masterOrganizationUBOs.Service.getUBOs(organization.id)
        case None => Future(Seq())
      }

      (for {
        organization <- organization
        ubos <- ubos(organization)
      } yield Ok(views.html.component.master.userViewOrganizationUBOs(organization, ubos))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def viewOrganizationUBOs(): Action[AnyContent] = withOrganizationLoginAction { implicit loginState =>
    implicit request =>
      val organizationID = masterOrganizations.Service.tryGetID(loginState.username)

      def ubos(organizationID: String) = masterOrganizationUBOs.Service.getUBOs(organizationID)

      (for {
        organizationID <- organizationID
        ubos <- ubos(organizationID)
      } yield Ok(views.html.component.master.viewOrganizationUBOs(ubos))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def traderViewAcceptedNegotiation(negotiationID: String): Action[AnyContent] = withTraderLoginAction { implicit loginState =>
    implicit request =>
      val traderID = masterTraders.Service.tryGetID(loginState.username)
      val negotiation = masterNegotiations.Service.tryGet(negotiationID)
      for {
        traderID <- traderID
        negotiation <- negotiation
      } yield {
        if (negotiation.sellerTraderID == traderID || negotiation.buyerTraderID == traderID) {
          Ok(views.html.component.master.traderViewAcceptedNegotiation(traderID = traderID, negotiation = negotiation))
        } else {
          throw new BaseException(constants.Response.UNAUTHORIZED)
        }
      }
  }

  def traderViewAcceptedNegotiationTerms(negotiationID: String): Action[AnyContent] = withTraderLoginAction { implicit loginState =>
    implicit request =>
      val traderID = masterTraders.Service.tryGetID(loginState.username)
      val negotiation = masterNegotiations.Service.tryGet(negotiationID)

      def getAsset(assetID: String) = masterProperties.Service.getPropertyMap(assetID)

      def getOrder(negotiationID: String): Future[Option[Order]] = masterOrders.Service.get(negotiationID)

      def getCounterPartyTrader(traderID: String, negotiation: Negotiation) = masterTraders.Service.tryGet(if (traderID == negotiation.buyerTraderID) negotiation.sellerTraderID else negotiation.buyerTraderID)

      def getBuyerAddress(traderID: String, negotiation: Negotiation, counterPartyAccountID: String) = if (traderID == negotiation.buyerTraderID) Future(loginState.address) else blockchainAccounts.Service.tryGetAddress(counterPartyAccountID)

      def getSellerAddress(traderID: String, negotiation: Negotiation, counterPartyAccountID: String) = if (traderID == negotiation.sellerTraderID) Future(loginState.address) else blockchainAccounts.Service.tryGetAddress(counterPartyAccountID)

      def getResult(traderID: String, negotiation: Negotiation, order: Option[Order], assetProperties: Map[String,Option[String]], counterPartyTrader: Trader) = {
        if (traderID == negotiation.buyerTraderID || traderID == negotiation.sellerTraderID) {
          val counterPartyOrganization = masterOrganizations.Service.tryGet(counterPartyTrader.organizationID)
          for {
            counterPartyOrganization <- counterPartyOrganization
          } yield Ok(views.html.component.master.traderViewAcceptedNegotiationTerms(traderID = traderID, counterPartyTrader = counterPartyTrader, counterPartyOrganization = counterPartyOrganization, negotiation = negotiation, order = order, assetProperties = assetProperties))
        } else {
          throw new BaseException(constants.Response.UNAUTHORIZED)
        }
      }

      (for {
        traderID <- traderID
        negotiation <- negotiation
        asset <- getAsset(negotiation.assetID)
        order <- getOrder(negotiation.id)
        counterPartyTrader <- getCounterPartyTrader(traderID, negotiation)
        buyerAddress <- getBuyerAddress(traderID, negotiation, counterPartyTrader.accountID)
        sellerAddress <- getSellerAddress(traderID, negotiation, counterPartyTrader.accountID)
        result <- getResult(traderID, negotiation, order, asset, counterPartyTrader)
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def traderViewCompletedNegotiation(negotiationID: String): Action[AnyContent] = withTraderLoginAction { implicit loginState =>
    implicit request =>
      val traderID = masterTraders.Service.tryGetID(loginState.username)
      val negotiationHistory = masterNegotiationHistories.Service.tryGet(negotiationID)
      for {
        traderID <- traderID
        negotiationHistory <- negotiationHistory
      } yield {
        if (negotiationHistory.sellerTraderID == traderID || negotiationHistory.buyerTraderID == traderID) {
          Ok(views.html.component.master.traderViewCompletedNegotiation(negotiationHistory = negotiationHistory))
        } else {
          throw new BaseException(constants.Response.UNAUTHORIZED)
        }
      }
  }

  def traderViewCompletedNegotiationTerms(negotiationID: String): Action[AnyContent] = withTraderLoginAction { implicit loginState =>
    implicit request =>
      val traderID = masterTraders.Service.tryGetID(loginState.username)
      val negotiationHistory = masterNegotiationHistories.Service.tryGet(negotiationID)

      def getResult(traderID: String, negotiationHistory: NegotiationHistory) = {
        if (traderID == negotiationHistory.buyerTraderID || traderID == negotiationHistory.sellerTraderID) {
          val assetHistory = masterProperties.Service.getPropertyMap(negotiationHistory.assetID)
          val counterPartyTrader = masterTraders.Service.tryGet(if (traderID == negotiationHistory.buyerTraderID) negotiationHistory.sellerTraderID else negotiationHistory.buyerTraderID)

          def getCounterPartyOrganization(organizationID: String) = masterOrganizations.Service.tryGet(organizationID)

          for {
            assetHistory <- assetHistory
            counterPartyTrader <- counterPartyTrader
            counterPartyOrganization <- getCounterPartyOrganization(counterPartyTrader.organizationID)
          } yield Ok(views.html.component.master.traderViewCompletedNegotiationTerms(counterPartyTrader = counterPartyTrader, counterPartyOrganization = counterPartyOrganization, negotiationHistory = negotiationHistory, assetHistory = assetHistory))
        } else {
          throw new BaseException(constants.Response.UNAUTHORIZED)
        }
      }

      (for {
        traderID <- traderID
        negotiationHistory <- negotiationHistory
        result <- getResult(traderID, negotiationHistory)
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def organizationViewAcceptedNegotiation(negotiationID: String): Action[AnyContent] = withOrganizationLoginAction { implicit loginState =>
    implicit request =>
      Future(Ok(views.html.component.master.organizationViewAcceptedNegotiation(negotiationID = negotiationID)))
  }

  def organizationViewAcceptedNegotiationTerms(negotiationID: String): Action[AnyContent] = withOrganizationLoginAction { implicit loginState =>
    implicit request =>
      val organizationID = masterOrganizations.Service.tryGetID(loginState.username)
      val negotiation = masterNegotiations.Service.tryGet(negotiationID)

      def getTraderList(traderIDs: Seq[String]) = masterTraders.Service.getTraders(traderIDs)

      def getResult(traderList: Seq[Trader], negotiation: Negotiation, organizationID: String) = {
        if (traderList.map(_.organizationID) contains organizationID) {
          val asset = masterProperties.Service.getPropertyMap(negotiation.assetID)
          val counterPartyOrganization = masterOrganizations.Service.tryGet(traderList.find(_.organizationID != organizationID).map(_.organizationID).getOrElse(""))
          for {
            asset <- asset
            counterPartyOrganization <- counterPartyOrganization
          } yield Ok(views.html.component.master.organizationViewAcceptedNegotiationTerms(traderList = traderList, counterPartyOrganization = counterPartyOrganization, negotiation = negotiation, asset = asset))
        } else {
          throw new BaseException(constants.Response.UNAUTHORIZED)
        }
      }

      (for {
        organizationID <- organizationID
        negotiation <- negotiation
        traderList <- getTraderList(Seq(negotiation.sellerTraderID, negotiation.buyerTraderID))
        result <- getResult(traderList, negotiation, organizationID)
      } yield result).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def organizationViewCompletedNegotiation(negotiationID: String): Action[AnyContent] = withOrganizationLoginAction { implicit loginState =>
    implicit request =>
      Future(Ok(views.html.component.master.organizationViewCompletedNegotiation(negotiationID = negotiationID)))
  }

  def organizationViewCompletedNegotiationTerms(negotiationID: String): Action[AnyContent] = withOrganizationLoginAction { implicit loginState =>
    implicit request =>
      val organizationID = masterOrganizations.Service.tryGetID(loginState.username)
      val negotiationHistory = masterNegotiationHistories.Service.tryGet(negotiationID)

      def getTraderList(traderIDs: Seq[String]) = masterTraders.Service.getTraders(traderIDs)

      def getResult(traderList: Seq[Trader], negotiationHistory: NegotiationHistory, organizationID: String) = {
        if (traderList.map(_.organizationID) contains organizationID) {
          val assetHistory = masterProperties.Service.getPropertyMap(negotiationHistory.assetID)
          val counterPartyOrganization = masterOrganizations.Service.tryGet(traderList.find(_.organizationID != organizationID).map(_.organizationID).getOrElse(""))
          for {
            assetHistory <- assetHistory
            counterPartyOrganization <- counterPartyOrganization
          } yield Ok(views.html.component.master.organizationViewCompletedNegotiationTerms(traderList = traderList, counterPartyOrganization = counterPartyOrganization, negotiationHistory = negotiationHistory, assetHistory = assetHistory))
        } else {
          throw new BaseException(constants.Response.UNAUTHORIZED)
        }
      }

      (for {
        organizationID <- organizationID
        negotiationHistory <- negotiationHistory
        traderList <- getTraderList(Seq(negotiationHistory.sellerTraderID, negotiationHistory.buyerTraderID))
        result <- getResult(traderList, negotiationHistory, organizationID)
      } yield result).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def traderViewNegotiationDocumentList(negotiationID: String): Action[AnyContent] = withTraderLoginAction { implicit loginState =>
    implicit request =>
      val traderID = masterTraders.Service.tryGetID(loginState.username)
      val negotiation = masterNegotiations.Service.tryGet(negotiationID)

      def getResult(traderID: String, negotiation: Negotiation) = {
        if (negotiation.sellerTraderID == traderID || negotiation.buyerTraderID == traderID) {
          val negotiationFileList = masterTransactionNegotiationFiles.Service.getAllDocuments(negotiationID)
          val assetFileList = masterTransactionAssetFiles.Service.getAllDocuments(negotiation.assetID)
          val negotiationEnvelopeList = docusignEnvelopes.Service.getAll(negotiationID)
          for {
            negotiationFileList <- negotiationFileList
            assetFileList <- assetFileList
            negotiationEnvelopeList <- negotiationEnvelopeList
          } yield {
            Ok(views.html.component.master.traderViewNegotiationDocumentList(traderID = traderID, negotiation = negotiation, assetFileList = assetFileList, negotiationFileList = negotiationFileList, negotiationEnvelopeList = negotiationEnvelopeList))
          }
        } else {
          throw new BaseException(constants.Response.UNAUTHORIZED)
        }
      }

      (for {
        traderID <- traderID
        negotiation <- negotiation
        result <- getResult(traderID, negotiation)
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def organizationViewNegotiationDocumentList(negotiationID: String): Action[AnyContent] = withOrganizationLoginAction { implicit loginState =>
    implicit request =>
      val organizationID = masterOrganizations.Service.tryGetID(loginState.username)
      val negotiation = masterNegotiations.Service.tryGet(negotiationID)

      def getTradersOrganizationIDList(traderIDs: Seq[String]) = masterTraders.Service.tryGetOrganizationIDs(traderIDs)

      def getResult(traderOrganizationIDs: Seq[String], negotiation: Negotiation, organizationID: String) = {
        if (traderOrganizationIDs contains organizationID) {
          val negotiationFileList = masterTransactionNegotiationFiles.Service.getAllDocuments(negotiationID)
          val assetFileList = masterTransactionAssetFiles.Service.getAllDocuments(negotiation.assetID)
          for {
            negotiationFileList <- negotiationFileList
            assetFileList <- assetFileList
          } yield Ok(views.html.component.master.organizationViewNegotiationDocumentList(negotiation = negotiation, assetFileList = assetFileList, negotiationFileList = negotiationFileList))
        } else {
          throw new BaseException(constants.Response.UNAUTHORIZED)
        }
      }

      (for {
        organizationID <- organizationID
        negotiation <- negotiation
        traderOrganizationIDs <- getTradersOrganizationIDList(Seq(negotiation.sellerTraderID, negotiation.buyerTraderID))
        result <- getResult(traderOrganizationIDs, negotiation, organizationID)
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def zoneViewNegotiationDocumentList(negotiationID: String): Action[AnyContent] = withZoneLoginAction { implicit loginState =>
    implicit request =>
      val negotiation = masterNegotiations.Service.tryGet(negotiationID)
      val zoneID = masterZones.Service.tryGetID(loginState.username)

      def getTradersZoneIDList(traderIDs: Seq[String]) = masterTraders.Service.tryGetZoneIDs(traderIDs)

      def getResult(traderZoneIDs: Seq[String], negotiation: Negotiation, zoneID: String) = {
        if (traderZoneIDs contains zoneID) {
          val negotiationFileList = masterTransactionNegotiationFiles.Service.getAllDocuments(negotiationID)
          val assetFileList = masterTransactionAssetFiles.Service.getAllDocuments(negotiation.assetID)
          for {
            negotiationFileList <- negotiationFileList
            assetFileList <- assetFileList
          } yield Ok(views.html.component.master.zoneViewNegotiationDocumentList(negotiation = negotiation, assetFileList = assetFileList, negotiationFileList = negotiationFileList))
        } else {
          throw new BaseException(constants.Response.UNAUTHORIZED)
        }
      }

      (for {
        negotiation <- negotiation
        zoneID <- zoneID
        traderZoneIDs <- getTradersZoneIDList(Seq(negotiation.sellerTraderID, negotiation.buyerTraderID))
        result <- getResult(traderZoneIDs, negotiation, zoneID)
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def traderViewNegotiationDocument(negotiationID: String, documentType: Option[String] = None): Action[AnyContent] = withTraderLoginAction { implicit loginState =>
    implicit request =>
      val traderID = masterTraders.Service.tryGetID(loginState.username)
      val negotiation = masterNegotiations.Service.tryGet(negotiationID)

      def getResult(negotiation: Negotiation, traderID: String) = if (negotiation.sellerTraderID == traderID || negotiation.buyerTraderID == traderID) {
        documentType match {
          case Some(documentType) =>
            documentType match {
              case constants.File.Asset.BILL_OF_LADING | constants.File.Asset.COO | constants.File.Asset.COA =>
                val assetFile = masterTransactionAssetFiles.Service.get(negotiation.assetID, documentType)
                for {
                  assetFile <- assetFile
                } yield Ok(views.html.component.master.traderViewNegotiationDocument(negotiationID, assetFile))
              case _ =>
                val negotiationFile = masterTransactionNegotiationFiles.Service.get(negotiationID, documentType)
                for {
                  negotiationFile <- negotiationFile
                } yield Ok(views.html.component.master.traderViewNegotiationDocument(negotiationID, negotiationFile))
            }
          case None =>
            val assetFileList = masterTransactionAssetFiles.Service.getAllDocuments(negotiation.assetID)
            val negotiationFileList = masterTransactionNegotiationFiles.Service.getAllDocuments(negotiationID)
            for {
              assetFileList <- assetFileList
              negotiationFileList <- negotiationFileList
            } yield Ok(views.html.component.master.traderViewNegotiationDocument(negotiationID, (assetFileList ++ negotiationFileList).headOption))
        }
      }
      else {
        throw new BaseException(constants.Response.UNAUTHORIZED)
      }

      (for {
        traderID <- traderID
        negotiation <- negotiation
        result <- getResult(negotiation, traderID)
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def organizationViewNegotiationDocument(negotiationID: String, documentTypeOrNone: Option[String]): Action[AnyContent] = withOrganizationLoginAction { implicit loginState =>
    implicit request =>
      val organizationID = masterOrganizations.Service.tryGetID(loginState.username)
      val negotiation = masterNegotiations.Service.tryGet(negotiationID)

      def getTradersOrganizationIDList(traderIDs: Seq[String]) = masterTraders.Service.tryGetOrganizationIDs(traderIDs)

      def getResult(traderOrganizationIDs: Seq[String], negotiation: Negotiation, organizationID: String) = {
        if (traderOrganizationIDs contains organizationID) {
          documentTypeOrNone match {
            case Some(documentType) =>
              documentType match {
                case constants.File.Asset.BILL_OF_LADING | constants.File.Asset.COO | constants.File.Asset.COA =>
                  val assetFile = masterTransactionAssetFiles.Service.get(negotiation.assetID, documentType)
                  for {
                    assetFile <- assetFile
                  } yield Ok(views.html.component.master.organizationViewNegotiationDocument(negotiationID, assetFile))
                case _ =>
                  val negotiationFile = masterTransactionNegotiationFiles.Service.get(negotiationID, documentType)
                  for {
                    negotiationFile <- negotiationFile
                  } yield Ok(views.html.component.master.organizationViewNegotiationDocument(negotiationID, negotiationFile))
              }
            case None =>
              val assetFileList = masterTransactionAssetFiles.Service.getAllDocuments(negotiation.assetID)
              val negotiationFileList = masterTransactionNegotiationFiles.Service.getAllDocuments(negotiationID)
              for {
                assetFileList <- assetFileList
                negotiationFileList <- negotiationFileList
              } yield Ok(views.html.component.master.organizationViewNegotiationDocument(negotiationID, (assetFileList ++ negotiationFileList).headOption))
          }
        } else {
          throw new BaseException(constants.Response.UNAUTHORIZED)
        }
      }

      (for {
        organizationID <- organizationID
        negotiation <- negotiation
        traderOrganizationIDs <- getTradersOrganizationIDList(Seq(negotiation.sellerTraderID, negotiation.buyerTraderID))
        result <- getResult(traderOrganizationIDs, negotiation, organizationID)
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def zoneViewNegotiationDocument(negotiationID: String, documentType: Option[String]): Action[AnyContent] = withZoneLoginAction { implicit loginState =>
    implicit request =>
      val zoneID = masterZones.Service.tryGetID(loginState.username)
      val negotiation = masterNegotiations.Service.tryGet(negotiationID)

      def getTradersZoneIDList(traderIDs: Seq[String]) = masterTraders.Service.tryGetZoneIDs(traderIDs)

      def getResult(traderZoneIDs: Seq[String], negotiation: Negotiation, zoneID: String) = {
        if (traderZoneIDs contains zoneID) {
          documentType match {
            case Some(documentType) =>
              documentType match {
                case constants.File.Asset.BILL_OF_LADING | constants.File.Asset.COO | constants.File.Asset.COA =>
                  val assetFile = masterTransactionAssetFiles.Service.get(negotiation.assetID, documentType)
                  for {
                    assetFile <- assetFile
                  } yield Ok(views.html.component.master.zoneViewNegotiationDocument(negotiationID, assetFile))
                case _ =>
                  val negotiationFile = masterTransactionNegotiationFiles.Service.get(negotiationID, documentType)
                  for {
                    negotiationFile <- negotiationFile
                  } yield Ok(views.html.component.master.zoneViewNegotiationDocument(negotiationID, negotiationFile))
              }
            case None =>
              val negotiationFileList = masterTransactionNegotiationFiles.Service.getAllDocuments(negotiationID)
              val assetFileList = masterTransactionAssetFiles.Service.getAllDocuments(negotiation.assetID)
              for {
                negotiationFileList <- negotiationFileList
                assetFileList <- assetFileList
              } yield Ok(views.html.component.master.zoneViewNegotiationDocument(negotiationID, (assetFileList ++ negotiationFileList).headOption))
          }
        } else {
          throw new BaseException(constants.Response.UNAUTHORIZED)
        }
      }

      (for {
        zoneID <- zoneID
        negotiation <- negotiation
        traderZoneIDs <- getTradersZoneIDList(Seq(negotiation.buyerTraderID, negotiation.sellerTraderID))
        result <- getResult(traderZoneIDs, negotiation, zoneID)
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def traderViewCompletedNegotiationDocument(negotiationID: String, documentType: Option[String] = None): Action[AnyContent] = withTraderLoginAction { implicit loginState =>
    implicit request =>
      val traderID = masterTraders.Service.tryGetID(loginState.username)
      val negotiationHistory = masterNegotiationHistories.Service.tryGet(negotiationID)

      def getResult(negotiationHistory: NegotiationHistory, traderID: String) = if (negotiationHistory.sellerTraderID == traderID || negotiationHistory.buyerTraderID == traderID) {
        documentType match {
          case Some(documentType) =>
            documentType match {
              case constants.File.Asset.BILL_OF_LADING | constants.File.Asset.COO | constants.File.Asset.COA =>
                val assetFile = masterTransactionAssetFileHistories.Service.get(negotiationHistory.assetID, documentType)
                for {
                  assetFile <- assetFile
                } yield Ok(views.html.component.master.traderViewNegotiationDocument(negotiationID, assetFile))
              case _ =>
                val negotiationFile = masterTransactionNegotiationFileHistories.Service.get(negotiationID, documentType)
                for {
                  negotiationFile <- negotiationFile
                } yield Ok(views.html.component.master.traderViewNegotiationDocument(negotiationID, negotiationFile))
            }
          case None =>
            val assetFileList = masterTransactionAssetFileHistories.Service.getAllDocuments(negotiationHistory.assetID)
            val negotiationFileList = masterTransactionNegotiationFileHistories.Service.getAllDocuments(negotiationID)
            for {
              assetFileList <- assetFileList
              negotiationFileList <- negotiationFileList
            } yield Ok(views.html.component.master.traderViewNegotiationDocument(negotiationID, (assetFileList ++ negotiationFileList).headOption))
        }
      }
      else {
        throw new BaseException(constants.Response.UNAUTHORIZED)
      }

      (for {
        traderID <- traderID
        negotiationHistory <- negotiationHistory
        result <- getResult(negotiationHistory, traderID)
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def organizationViewCompletedNegotiationDocument(negotiationID: String, documentTypeOrNone: Option[String]): Action[AnyContent] = withOrganizationLoginAction { implicit loginState =>
    implicit request =>
      val organizationID = masterOrganizations.Service.tryGetID(loginState.username)
      val negotiationHistory = masterNegotiationHistories.Service.tryGet(negotiationID)

      def getTradersOrganizationIDList(traderIDs: Seq[String]) = masterTraders.Service.tryGetOrganizationIDs(traderIDs)

      def getResult(traderOrganizationIDs: Seq[String], negotiationHistory: NegotiationHistory, organizationID: String) = {
        if (traderOrganizationIDs contains organizationID) {
          documentTypeOrNone match {
            case Some(documentType) =>
              documentType match {
                case constants.File.Asset.BILL_OF_LADING | constants.File.Asset.COO | constants.File.Asset.COA =>
                  val assetFile = masterTransactionAssetFileHistories.Service.get(negotiationHistory.assetID, documentType)
                  for {
                    assetFile <- assetFile
                  } yield Ok(views.html.component.master.organizationViewNegotiationDocument(negotiationID, assetFile))
                case _ =>
                  val negotiationFile = masterTransactionNegotiationFileHistories.Service.get(negotiationID, documentType)
                  for {
                    negotiationFile <- negotiationFile
                  } yield Ok(views.html.component.master.organizationViewNegotiationDocument(negotiationID, negotiationFile))
              }
            case None =>
              val assetFileList = masterTransactionAssetFileHistories.Service.getAllDocuments(negotiationHistory.assetID)
              val negotiationFileList = masterTransactionNegotiationFileHistories.Service.getAllDocuments(negotiationID)
              for {
                assetFileList <- assetFileList
                negotiationFileList <- negotiationFileList
              } yield Ok(views.html.component.master.organizationViewNegotiationDocument(negotiationID, (assetFileList ++ negotiationFileList).headOption))
          }
        } else {
          throw new BaseException(constants.Response.UNAUTHORIZED)
        }
      }

      (for {
        organizationID <- organizationID
        negotiationHistory <- negotiationHistory
        traderOrganizationIDs <- getTradersOrganizationIDList(Seq(negotiationHistory.sellerTraderID, negotiationHistory.buyerTraderID))
        result <- getResult(traderOrganizationIDs, negotiationHistory, organizationID)
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def zoneViewCompletedNegotiationDocument(negotiationID: String, documentType: Option[String]): Action[AnyContent] = withZoneLoginAction { implicit loginState =>
    implicit request =>
      val zoneID = masterZones.Service.tryGetID(loginState.username)
      val negotiationHistory = masterNegotiationHistories.Service.tryGet(negotiationID)

      def getTradersZoneIDList(traderIDs: Seq[String]) = masterTraders.Service.tryGetZoneIDs(traderIDs)

      def getResult(traderZoneIDs: Seq[String], negotiationHistory: NegotiationHistory, zoneID: String) = {
        if (traderZoneIDs contains zoneID) {
          documentType match {
            case Some(documentType) =>
              documentType match {
                case constants.File.Asset.BILL_OF_LADING | constants.File.Asset.COO | constants.File.Asset.COA =>
                  val assetFile = masterTransactionAssetFileHistories.Service.get(negotiationHistory.assetID, documentType)
                  for {
                    assetFile <- assetFile
                  } yield Ok(views.html.component.master.zoneViewNegotiationDocument(negotiationID, assetFile))
                case _ =>
                  val negotiationFile = masterTransactionNegotiationFileHistories.Service.get(negotiationID, documentType)
                  for {
                    negotiationFile <- negotiationFile
                  } yield Ok(views.html.component.master.zoneViewNegotiationDocument(negotiationID, negotiationFile))
              }
            case None =>
              val negotiationFileList = masterTransactionNegotiationFiles.Service.getAllDocuments(negotiationID)
              val assetFileList = masterTransactionAssetFiles.Service.getAllDocuments(negotiationHistory.assetID)
              for {
                negotiationFileList <- negotiationFileList
                assetFileList <- assetFileList
              } yield Ok(views.html.component.master.zoneViewNegotiationDocument(negotiationID, (assetFileList ++ negotiationFileList).headOption))
          }
        } else {
          throw new BaseException(constants.Response.UNAUTHORIZED)
        }
      }

      (for {
        zoneID <- zoneID
        negotiationHistory <- negotiationHistory
        traderZoneIDs <- getTradersZoneIDList(Seq(negotiationHistory.buyerTraderID, negotiationHistory.sellerTraderID))
        result <- getResult(traderZoneIDs, negotiationHistory, zoneID)
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def tradeDocuments(negotiationID: String): Action[AnyContent] = withTraderLoginAction { implicit loginState =>
    implicit request =>
      val traderID = masterTraders.Service.tryGetID(loginState.username)
      val negotiation = masterNegotiations.Service.tryGet(negotiationID)

      def getResult(negotiation: Negotiation, traderID: String) = {
        if (negotiation.sellerTraderID == traderID || negotiation.buyerTraderID == traderID) {
          val negotiationFileList = masterTransactionNegotiationFiles.Service.getAllDocuments(negotiationID)
          val assetFileList = masterTransactionAssetFiles.Service.getAllDocuments(negotiation.assetID)
          val negotiationEnvelopeList = docusignEnvelopes.Service.getAll(negotiationID)
          for {
            negotiationFileList <- negotiationFileList
            assetFileList <- assetFileList
            negotiationEnvelopeList <- negotiationEnvelopeList
          } yield Ok(views.html.component.master.tradeDocuments(negotiation, assetFileList, negotiationFileList, negotiationEnvelopeList))
        } else {
          throw new BaseException(constants.Response.UNAUTHORIZED)
        }
      }

      (for {
        traderID <- traderID
        negotiation <- negotiation
        result <- getResult(negotiation, traderID)
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def traderViewAcceptedNegotiationDocumentList(negotiationID: String): Action[AnyContent] = withTraderLoginAction { implicit loginState =>
    implicit request =>
      val traderID = masterTraders.Service.tryGetID(loginState.username)
      val negotiation = masterNegotiations.Service.tryGet(negotiationID)
      for {
        traderID <- traderID
        negotiation <- negotiation
      } yield Ok(views.html.component.master.traderViewAcceptedNegotiationDocumentList(negotiationID, traderID, negotiation))
  }

  def traderViewCompletedNegotiationDocumentList(negotiationID: String): Action[AnyContent] = withTraderLoginAction { implicit loginState =>
    implicit request =>
      val traderID = masterTraders.Service.tryGetID(loginState.username)
      val negotiationHistory = masterNegotiationHistories.Service.tryGet(negotiationID)

      def getResult(traderID: String, negotiationHistory: NegotiationHistory) = {
        if (negotiationHistory.sellerTraderID == traderID || negotiationHistory.buyerTraderID == traderID) {
          val negotiationFileHistoryList = masterTransactionNegotiationFileHistories.Service.getAllDocuments(negotiationID)
          val assetFileHistoryList = masterTransactionAssetFileHistories.Service.getAllDocuments(negotiationHistory.assetID)
          for {
            negotiationFileHistoryList <- negotiationFileHistoryList
            assetFileHistoryList <- assetFileHistoryList
          } yield {
            Ok(views.html.component.master.traderViewCompletedNegotiationDocumentList(negotiationHistory = negotiationHistory, assetFileHistoryList = assetFileHistoryList, negotiationFileHistoryList = negotiationFileHistoryList))
          }
        } else {
          throw new BaseException(constants.Response.UNAUTHORIZED)
        }
      }

      (for {
        traderID <- traderID
        negotiationHistory <- negotiationHistory
        result <- getResult(traderID, negotiationHistory)
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def organizationViewCompletedNegotiationDocumentList(negotiationID: String): Action[AnyContent] = withOrganizationLoginAction { implicit loginState =>
    implicit request =>
      val organizationID = masterOrganizations.Service.tryGetID(loginState.username)
      val negotiationHistory = masterNegotiationHistories.Service.tryGet(negotiationID)

      def getTradersOrganizationIDList(traderIDs: Seq[String]) = masterTraders.Service.tryGetOrganizationIDs(traderIDs)

      def getResult(traderOrganizationIDs: Seq[String], negotiationHistory: NegotiationHistory, organizationID: String) = {
        if (traderOrganizationIDs contains organizationID) {
          val negotiationFileHistoryList = masterTransactionNegotiationFileHistories.Service.getAllDocuments(negotiationID)
          val assetFileHistoryList = masterTransactionAssetFileHistories.Service.getAllDocuments(negotiationHistory.assetID)
          for {
            negotiationFileHistoryList <- negotiationFileHistoryList
            assetFileHistoryList <- assetFileHistoryList
          } yield Ok(views.html.component.master.organizationViewCompletedNegotiationDocumentList(negotiationHistory = negotiationHistory, assetFileHistoryList = assetFileHistoryList, negotiationFileHistoryList = negotiationFileHistoryList))
        } else {
          throw new BaseException(constants.Response.UNAUTHORIZED)
        }
      }

      (for {
        organizationID <- organizationID
        negotiationHistory <- negotiationHistory
        traderOrganizationIDs <- getTradersOrganizationIDList(Seq(negotiationHistory.sellerTraderID, negotiationHistory.buyerTraderID))
        result <- getResult(traderOrganizationIDs, negotiationHistory, organizationID)
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def zoneViewCompletedNegotiationDocumentList(negotiationID: String): Action[AnyContent] = withZoneLoginAction { implicit loginState =>
    implicit request =>
      val zoneID = masterZones.Service.tryGetID(loginState.username)
      val negotiationHistory = masterNegotiationHistories.Service.tryGet(negotiationID)

      def getTradersZoneIDList(traderIDs: Seq[String]) = masterTraders.Service.tryGetZoneIDs(traderIDs)

      def getResult(traderZoneIDs: Seq[String], negotiationHistory: NegotiationHistory, zoneID: String) = {
        if (traderZoneIDs contains zoneID) {
          val negotiationFileHistoryList = masterTransactionNegotiationFileHistories.Service.getAllDocuments(negotiationID)
          val assetFileHistoryList = masterTransactionAssetFileHistories.Service.getAllDocuments(negotiationHistory.assetID)
          for {
            negotiationFileHistoryList <- negotiationFileHistoryList
            assetFileHistoryList <- assetFileHistoryList
          } yield Ok(views.html.component.master.zoneViewCompletedNegotiationDocumentList(negotiationHistory = negotiationHistory, assetFileHistoryList = assetFileHistoryList, negotiationFileHistoryList = negotiationFileHistoryList))
        } else {
          throw new BaseException(constants.Response.UNAUTHORIZED)
        }
      }

      (for {
        zoneID <- zoneID
        negotiationHistory <- negotiationHistory
        traderZoneIDs <- getTradersZoneIDList(Seq(negotiationHistory.sellerTraderID, negotiationHistory.buyerTraderID))
        result <- getResult(traderZoneIDs, negotiationHistory, zoneID)
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def organizationViewAcceptedNegotiationDocumentList(negotiationID: String): Action[AnyContent] = withOrganizationLoginAction { implicit loginState =>
    implicit request =>
      Future(Ok(views.html.component.master.organizationViewAcceptedNegotiationDocumentList(negotiationID)))
  }

  def zoneViewAcceptedNegotiationDocumentList(negotiationID: String): Action[AnyContent] = withZoneLoginAction { implicit loginState =>
    implicit request =>
      Future(Ok(views.html.component.master.zoneViewAcceptedNegotiationDocumentList(negotiationID)))
  }

  //Dashboard Cards

  def organizationTradeStatistics(): Action[AnyContent] = withOrganizationLoginAction { implicit loginState =>
    implicit request =>
      val organizationID = masterOrganizations.Service.tryGetID(loginState.username)

      def getTraders(organizationID: String): Future[Seq[Trader]] = masterTraders.Service.getOrganizationAcceptedTraderList(organizationID)

      def getSellTradeList(traderIDs: Seq[String]) = masterNegotiations.Service.getAllContractSignedNegotiationListBySellerTraderIDs(traderIDs)

      def getSellTradeHistoryList(traderIDs: Seq[String]) = masterNegotiationHistories.Service.getAllCompletedNegotiationListBySellerTraderIDs(traderIDs)

      def getBuyTradeList(traderIDs: Seq[String]) = masterNegotiations.Service.getAllContractSignedNegotiationListByBuyerTraderIDs(traderIDs)

      def getBuyTradeHistoryList(traderIDs: Seq[String]) = masterNegotiationHistories.Service.getAllCompletedNegotiationListByBuyerTraderIDs(traderIDs)

      (for {
        organizationID <- organizationID
        traderList <- getTraders(organizationID)
        sellTradeList <- getSellTradeList(traderList.map(_.id))
        sellTradeHistoryList <- getSellTradeHistoryList(traderList.map(_.id))
        buyTraderList <- getBuyTradeList(traderList.map(_.id))
        buyTradeHistoryList <- getBuyTradeHistoryList(traderList.map(_.id))
      } yield {
        val currentYear = Year.now().getValue
        val currentMonth = Calendar.getInstance.get(Calendar.MONTH)
        val timePeriods = (1 to 12).map { x => Seq(if (x - 1 <= currentMonth) currentYear else currentYear - 1, new DecimalFormat("00").format(x)).mkString("-") }.sorted

        val sellTradesMonthly = timePeriods.map { timePeriod =>
          sellTradeList.count(x => x.updatedOn.getOrElse("").toString.slice(0, 7) == timePeriod) + sellTradeHistoryList.count(x => x.updatedOn.getOrElse(x.createdOn.getOrElse("")).toString.slice(0, 7) == timePeriod)
        }
        val buyTradesMonthly = timePeriods.map { timePeriod =>
          buyTraderList.count(x => x.updatedOn.getOrElse("").toString.slice(0, 7) == timePeriod) + buyTradeHistoryList.count(x => x.updatedOn.getOrElse(x.createdOn.getOrElse("")).toString.slice(0, 7) == timePeriod)
        }
        Ok(views.html.component.master.organizationTradeStatistics(timePeriods, buyTradesMonthly, sellTradesMonthly))
      }).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def traderTradeStatistics(): Action[AnyContent] = withTraderLoginAction { implicit loginState =>
    implicit request =>

      val traderID = masterTraders.Service.tryGetID(loginState.username)

      def getSellTradeList(traderID: String) = masterNegotiations.Service.getAllContractSignedNegotiationListBySellerTraderID(traderID)

      def getSellTradeHistoryList(traderID: String) = masterNegotiationHistories.Service.getAllCompletedNegotiationListBySellerTraderID(traderID)

      def getBuyTradeList(traderID: String) = masterNegotiations.Service.getAllContractSignedNegotiationListByBuyerTraderID(traderID)

      def getBuyTradeHistoryList(traderID: String) = masterNegotiationHistories.Service.getAllCompletedNegotiationListByBuyerTraderID(traderID)

      (for {
        traderID <- traderID
        sellTradeList <- getSellTradeList(traderID)
        sellTradeHistoryList <- getSellTradeHistoryList(traderID)
        buyTraderList <- getBuyTradeList(traderID)
        buyTradeHistoryList <- getBuyTradeHistoryList(traderID)
      } yield {
        val currentYear = Year.now().getValue
        val currentMonth = Calendar.getInstance.get(Calendar.MONTH)
        val timePeriods = (1 to 12).map { x => Seq(if (x - 1 <= currentMonth) currentYear else currentYear - 1, new DecimalFormat("00").format(x)).mkString("-") }.sorted

        val sellTradesMonthly = timePeriods.map { timePeriod =>
          sellTradeList.count(x => x.updatedOn.getOrElse("").toString.slice(0, 7) == timePeriod) + sellTradeHistoryList.count(x => x.updatedOn.getOrElse(x.createdOn.getOrElse("")).toString.slice(0, 7) == timePeriod)
        }
        val buyTradesMonthly = timePeriods.map { timePeriod =>
          buyTraderList.count(x => x.updatedOn.getOrElse("").toString.slice(0, 7) == timePeriod) + buyTradeHistoryList.count(x => x.updatedOn.getOrElse(x.createdOn.getOrElse("")).toString.slice(0, 7) == timePeriod)
        }
        Ok(views.html.component.master.traderTradeStatistics(timePeriods, buyTradesMonthly, sellTradesMonthly))
      }).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def zoneTradeStatistics(): Action[AnyContent] = withZoneLoginAction { implicit loginState =>
    implicit request =>
      val zoneID = masterZones.Service.tryGetID(loginState.username)

      def getTraders(zoneID: String): Future[Seq[Trader]] = masterTraders.Service.getZoneAcceptedTraderList(zoneID)

      def getSellTradeList(traderIDs: Seq[String]) = masterNegotiations.Service.getAllContractSignedNegotiationListBySellerTraderIDs(traderIDs)

      def getSellTradeHistoryList(traderIDs: Seq[String]) = masterNegotiationHistories.Service.getAllCompletedNegotiationListBySellerTraderIDs(traderIDs)

      def getBuyTradeList(traderIDs: Seq[String]) = masterNegotiations.Service.getAllContractSignedNegotiationListByBuyerTraderIDs(traderIDs)

      def getBuyTradeHistoryList(traderIDs: Seq[String]) = masterNegotiationHistories.Service.getAllCompletedNegotiationListByBuyerTraderIDs(traderIDs)

      (for {
        zoneID <- zoneID
        traderList <- getTraders(zoneID)
        sellTradeList <- getSellTradeList(traderList.map(_.id))
        sellTradeHistoryList <- getSellTradeHistoryList(traderList.map(_.id))
        buyTraderList <- getBuyTradeList(traderList.map(_.id))
        buyTradeHistoryList <- getBuyTradeHistoryList(traderList.map(_.id))
      } yield {
        val currentYear = Year.now().getValue
        val currentMonth = Calendar.getInstance.get(Calendar.MONTH)
        val timePeriods = (1 to 12).map { x => Seq(if (x - 1 <= currentMonth) currentYear else currentYear - 1, new DecimalFormat("00").format(x)).mkString("-") }.sorted

        val sellTradesMonthly = timePeriods.map { timePeriod =>
          sellTradeList.count(x => x.updatedOn.getOrElse("").toString.slice(0, 7) == timePeriod) + sellTradeHistoryList.count(x => x.updatedOn.getOrElse(x.createdOn.getOrElse("")).toString.slice(0, 7) == timePeriod)
        }
        val buyTradesMonthly = timePeriods.map { timePeriod =>
          buyTraderList.count(x => x.updatedOn.getOrElse("").toString.slice(0, 7) == timePeriod) + buyTradeHistoryList.count(x => x.updatedOn.getOrElse(x.createdOn.getOrElse("")).toString.slice(0, 7) == timePeriod)
        }
        Ok(views.html.component.master.zoneTradeStatistics(timePeriods, buyTradesMonthly, sellTradesMonthly))
      }
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def organizationDeclarations(): Action[AnyContent] = withOrganizationLoginAction { implicit loginState =>
    implicit request =>
      Future(Ok(views.html.component.master.organizationDeclarations()))
  }

  // zone trades cards

  def zoneViewActiveNegotiationList: Action[AnyContent] = withZoneLoginAction { implicit loginState =>
    implicit request =>
      val zoneID = masterZones.Service.tryGetID(loginState.username)

      def getTraderList(zoneID: String): Future[Seq[Trader]] = masterTraders.Service.getTradersByZoneID(zoneID)

      def getAcceptedNegotiationList(traderIDs: Seq[String]) = masterNegotiations.Service.getAllAcceptedNegotiationListByTraderIDs(traderIDs)

      def getCounterPartyTraderList(traderIDs: Seq[String]) = masterTraders.Service.getTraders(traderIDs)

      def getAssetList(assetIDs: Seq[String]) = masterProperties.Service.getPropertyListMap(assetIDs)

      (for {
        zoneID <- zoneID
        traderList <- getTraderList(zoneID)
        acceptedNegotiationList <- getAcceptedNegotiationList(traderList.map(_.id))
        counterPartyTraderList <- getCounterPartyTraderList(traderList.map(_.id))
        assetList <- getAssetList(acceptedNegotiationList.map(_.assetID))
      } yield Ok(views.html.component.master.zoneViewActiveNegotiationList(acceptedNegotiationList, counterPartyTraderList, assetList))).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def zoneViewAcceptedNegotiationList: Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
    Ok(views.html.component.master.zoneViewAcceptedNegotiationList())
  }

  def zoneViewCompletedNegotiationList: Action[AnyContent] = withZoneLoginAction { implicit loginState =>
    implicit request =>
      val zoneID = masterZones.Service.tryGetID(loginState.username)

      def getTraderList(zoneID: String): Future[Seq[Trader]] = masterTraders.Service.getTradersByZoneID(zoneID)

      def getCompletedNegotiationList(traderIDs: Seq[String]): Future[Seq[NegotiationHistory]] = masterNegotiationHistories.Service.getAllCompletedNegotiationListByTraderIDs(traderIDs)

      def getCounterPartyTraderList(traderIDs: Seq[String]) = masterTraders.Service.getTraders(traderIDs)

      def getAssetList(assetIDs: Seq[String]) = masterProperties.Service.getPropertyListMap(assetIDs)

      (for {
        zoneID <- zoneID
        traderList <- getTraderList(zoneID)
        completedNegotiationList <- getCompletedNegotiationList(traderList.map(_.id))
        counterPartyTraderList <- getCounterPartyTraderList(completedNegotiationList.map(negotiation => if (traderList.map(_.id) contains negotiation.sellerTraderID) negotiation.buyerTraderID else negotiation.sellerTraderID))
        assetList <- getAssetList(completedNegotiationList.map(_.assetID))
      } yield Ok(views.html.component.master.zoneViewCompletedNegotiationList(completedNegotiationList, counterPartyTraderList, assetList))).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def zoneViewAcceptedNegotiation(negotiationID: String): Action[AnyContent] = withZoneLoginAction { implicit loginState =>
    implicit request =>
      Future(Ok(views.html.component.master.zoneViewAcceptedNegotiation(negotiationID = negotiationID)))
  }

  def zoneViewAcceptedNegotiationTerms(negotiationID: String): Action[AnyContent] = withZoneLoginAction { implicit loginState =>
    implicit request =>
      val zoneID = masterZones.Service.tryGetID(loginState.username)
      val negotiation = masterNegotiations.Service.tryGet(negotiationID)

      def getTraderList(traderIDs: Seq[String]) = masterTraders.Service.getTraders(traderIDs)

      def getResult(zoneID: String, traderList: Seq[Trader], negotiation: Negotiation) = if (traderList.map(_.zoneID) contains zoneID) {
        val asset = masterProperties.Service.getPropertyMap(negotiation.assetID)
        val organizationList = masterOrganizations.Service.getOrganizations(traderList.map(_.organizationID))
        for {
          asset <- asset
          organizationList <- organizationList
        } yield Ok(views.html.component.master.zoneViewAcceptedNegotiationTerms(traderList = traderList, organizationList = organizationList, negotiation = negotiation, asset = asset))
      } else {
        throw new BaseException(constants.Response.UNAUTHORIZED)
      }

      (for {
        zoneID <- zoneID
        negotiation <- negotiation
        traderList <- getTraderList(Seq(negotiation.buyerTraderID, negotiation.sellerTraderID))
        result <- getResult(zoneID = zoneID, traderList = traderList, negotiation = negotiation)
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def zoneViewCompletedNegotiation(negotiationID: String): Action[AnyContent] = withZoneLoginAction { implicit loginState =>
    implicit request =>
      Future(Ok(views.html.component.master.zoneViewCompletedNegotiation(negotiationID = negotiationID)))
  }

  def zoneViewCompletedNegotiationTerms(negotiationID: String): Action[AnyContent] = withZoneLoginAction { implicit loginState =>
    implicit request =>
      val zoneID = masterZones.Service.tryGetID(loginState.username)
      val negotiationHistory = masterNegotiationHistories.Service.tryGet(negotiationID)

      def getTraderList(traderIDs: Seq[String]) = masterTraders.Service.getTraders(traderIDs)

      def getResult(zoneID: String, traderList: Seq[Trader], negotiationHistory: NegotiationHistory): Future[Result] = if (traderList.map(_.zoneID) contains zoneID) {
        val assetHistory = masterProperties.Service.getPropertyMap(negotiationHistory.assetID)
        val organizationList = masterOrganizations.Service.getOrganizations(traderList.map(_.organizationID))
        for {
          assetHistory <- assetHistory
          organizationList <- organizationList
        } yield Ok(views.html.component.master.zoneViewCompletedNegotiationTerms(traderList = traderList, organizationList = organizationList, negotiationHistory = negotiationHistory, assetHistory = assetHistory))
      } else {
        throw new BaseException(constants.Response.UNAUTHORIZED)
      }

      (for {
        zoneID <- zoneID
        negotiationHistory <- negotiationHistory
        traderList <- getTraderList(Seq(negotiationHistory.buyerTraderID, negotiationHistory.sellerTraderID))
        result <- getResult(zoneID = zoneID, traderList = traderList, negotiationHistory = negotiationHistory)
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  //TODO: Whoever did this correct it
  def zoneViewTradeRoomFinancialAndChecks(negotiationID: String): Action[AnyContent] = withZoneLoginAction { implicit loginState =>
    implicit request =>
      Future(Ok(views.html.component.master.zoneViewTradeRoomFinancialAndChecks(negotiationID)))
  }

  def zoneViewTradeRoomFinancial(negotiationID: String): Action[AnyContent] = withZoneLoginAction { implicit loginState =>
    implicit request =>
      val negotiation = masterNegotiations.Service.tryGet(negotiationID)
      val fiatsInOrder = masterTransactionSendFiatRequests.Service.getFiatsInOrder(negotiationID)

      (for {
        negotiation <- negotiation
        fiatsInOrder <- fiatsInOrder
      } yield Ok(views.html.component.master.zoneViewTradeRoomFinancial(fiatsInOrder, 0, negotiation.price - fiatsInOrder))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def zoneViewCompletedTradeRoomFinancialAndChecks(negotiationID: String): Action[AnyContent] = withZoneLoginAction { implicit loginState =>
    implicit request =>
      Future(Ok(views.html.component.master.zoneViewCompletedTradeRoomFinancialAndChecks(negotiationID)))
  }

  def zoneViewCompletedTradeRoomFinancial(negotiationID: String): Action[AnyContent] = withZoneLoginAction { implicit loginState =>
    implicit request =>
      val negotiationHistory = masterNegotiationHistories.Service.tryGet(negotiationID)
      val fiatsInOrderHistory = masterTransactionSendFiatRequestHistories.Service.getFiatsInOrder(negotiationID)
      val zoneID = masterZones.Service.tryGetID(loginState.username)

      def getTraderIDList(zoneID: String) = masterTraders.Service.getVerifiedTraderIDsByZoneID(zoneID)

      def getFiatPegWallet(traderIDs: Seq[String]) = masterFiats.Service.getFiatPegWallet(traderIDs)

      (for {
        negotiationHistory <- negotiationHistory
        fiatsInOrderHistory <- fiatsInOrderHistory
        zoneID <- zoneID
        traderIDList <- getTraderIDList(zoneID)
        fiatPegWallet <- getFiatPegWallet(traderIDList)
      } yield Ok(views.html.component.master.zoneViewCompletedTradeRoomFinancial(fiatPegWallet.map(_.transactionAmount).sum, 0, negotiationHistory.price - fiatsInOrderHistory))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  //TODO: Whoever did this correct it
  def zoneViewTradeRoomChecks(negotiationID: String): Action[AnyContent] = withZoneLoginAction { implicit loginState =>
    implicit request =>
      val negotiation = masterNegotiations.Service.tryGet(negotiationID)

      def getBillOfLading(assetID: String): Future[Option[AssetFile]] = masterTransactionAssetFiles.Service.get(assetID, constants.File.Asset.BILL_OF_LADING)

      (for {
        negotiation <- negotiation
        billOfLading <- getBillOfLading(negotiation.assetID)
      } yield Ok(views.html.component.master.zoneViewTradeRoomChecks(negotiation.assetID, billOfLading.map(document => document.documentContent.map(_.asInstanceOf[BillOfLading].vesselName))))).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def zoneViewCompletedTradeRoomChecks(negotiationID: String): Action[AnyContent] = withZoneLoginAction { implicit loginState =>
    implicit request =>
      val negotiationHistory = masterNegotiationHistories.Service.tryGet(negotiationID)

      def billOfLading(assetID: String): Future[Option[AssetFileHistory]] = masterTransactionAssetFileHistories.Service.get(assetID, constants.File.Asset.BILL_OF_LADING)

      (for {
        negotiationHistory <- negotiationHistory
        billOfLading <- billOfLading(negotiationHistory.assetID)
      } yield Ok(views.html.component.master.zoneViewCompletedTradeRoomChecks(negotiationHistory.assetID, billOfLading.map(document => document.documentContent.map(_.asInstanceOf[BillOfLading].vesselName))))).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  //TODO: Whoever did this correct it
  def organizationViewTradeRoomFinancialAndChecks(negotiationID: String): Action[AnyContent] = withOrganizationLoginAction { implicit loginState =>
    implicit request =>
      Future(Ok(views.html.component.master.organizationViewTradeRoomFinancialAndChecks(negotiationID)))
  }

  def organizationViewCompletedTradeRoomFinancialAndChecks(negotiationID: String): Action[AnyContent] = withOrganizationLoginAction { implicit loginState =>
    implicit request =>
      Future(Ok(views.html.component.master.organizationViewCompletedTradeRoomFinancialAndChecks(negotiationID)))
  }

  def organizationViewTradeRoomFinancial(negotiationID: String): Action[AnyContent] = withOrganizationLoginAction { implicit loginState =>
    implicit request =>
      val negotiation = masterNegotiations.Service.tryGet(negotiationID)
      val fiatsInOrder = masterTransactionSendFiatRequests.Service.getFiatsInOrder(negotiationID)
      val organizationID = masterOrganizations.Service.tryGetID(loginState.username)

      def getTraderIDList(organizationID: String) = masterTraders.Service.getVerifiedTraderIDsByOrganizationID(organizationID)

      def getFiatPegWallet(traderIDs: Seq[String]) = masterFiats.Service.getFiatPegWallet(traderIDs)

      (for {
        negotiation <- negotiation
        fiatsInOrder <- fiatsInOrder
        organizationID <- organizationID
        traderIDList <- getTraderIDList(organizationID)
        fiatPegWallet <- getFiatPegWallet(traderIDList)
      } yield Ok(views.html.component.master.organizationViewTradeRoomFinancial(fiatPegWallet.map(_.transactionAmount).sum, 0, negotiation.price - fiatsInOrder))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def organizationViewCompletedTradeRoomFinancial(negotiationID: String): Action[AnyContent] = withOrganizationLoginAction { implicit loginState =>
    implicit request =>
      val negotiationHistory = masterNegotiationHistories.Service.tryGet(negotiationID)
      val fiatsInOrderHistory = masterTransactionSendFiatRequestHistories.Service.getFiatsInOrder(negotiationID)
      val organizationID = masterOrganizations.Service.tryGetID(loginState.username)

      def getTraderIDList(organizationID: String) = masterTraders.Service.getVerifiedTraderIDsByOrganizationID(organizationID)

      def getFiatPegWallet(traderIDs: Seq[String]) = masterFiats.Service.getFiatPegWallet(traderIDs)

      (for {
        negotiationHistory <- negotiationHistory
        fiatsInOrderHistory <- fiatsInOrderHistory
        organizationID <- organizationID
        traderIDList <- getTraderIDList(organizationID)
        fiatPegWallet <- getFiatPegWallet(traderIDList)
      } yield Ok(views.html.component.master.organizationViewTradeRoomFinancial(fiatPegWallet.map(_.transactionAmount).sum, 0, negotiationHistory.price - fiatsInOrderHistory))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  //TODO: Whoever did this correct it
  def organizationViewTradeRoomChecks(negotiationID: String): Action[AnyContent] = withOrganizationLoginAction { implicit loginState =>
    implicit request =>
      Future(Ok(views.html.component.master.organizationViewTradeRoomChecks()))
  }

  def organizationViewCompletedTradeRoomChecks(negotiationID: String): Action[AnyContent] = withOrganizationLoginAction { implicit loginState =>
    implicit request =>
      Future(Ok(views.html.component.master.organizationViewCompletedTradeRoomChecks()))
  }

  //TODO: Whoever did this correct it
  def traderViewTradeRoomFinancialAndChecks(negotiationID: String): Action[AnyContent] = withTraderLoginAction { implicit loginState =>
    implicit request =>
      Future(Ok(views.html.component.master.traderViewTradeRoomFinancialAndChecks(negotiationID)))
  }


  def traderViewTradeRoomFinancial(negotiationID: String): Action[AnyContent] = withTraderLoginAction { implicit loginState =>
    implicit request =>
      val traderID = masterTraders.Service.tryGetID(loginState.username)
      val negotiation = masterNegotiations.Service.tryGet(negotiationID)
      val fiatsInOrder = masterTransactionSendFiatRequests.Service.getFiatsInOrder(negotiationID)

      def getFiatPegWallet(traderID: String) = masterFiats.Service.getFiatPegWallet(traderID)

      def getAsset(assetID: String) = masterProperties.Service.getPropertyMap(assetID)

      (for {
        traderID <- traderID
        negotiation <- negotiation
        fiatsInOrder <- fiatsInOrder
        fiatPegWallet <- getFiatPegWallet(traderID)
        asset <- getAsset(negotiation.assetID)
      } yield Ok(views.html.component.master.traderViewTradeRoomFinancial(walletBalance = fiatPegWallet.map(_.transactionAmount).sum, amountPaid = 0, amountPending = (negotiation.price - fiatsInOrder), traderID = traderID, moderated = if(asset.getOrElse(constants.Property.MODERATED.dataName, throw new BaseException(constants.Response.FAILURE)).getOrElse("") == "true") true else false))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def traderViewCompletedTradeRoomFinancialAndChecks(negotiationID: String): Action[AnyContent] = withTraderLoginAction { implicit loginState =>
    implicit request =>
      Future(Ok(views.html.component.master.traderViewCompletedTradeRoomFinancialAndChecks(negotiationID)))
  }

  def traderViewCompletedTradeRoomFinancial(negotiationID: String): Action[AnyContent] = withTraderLoginAction { implicit loginState =>
    implicit request =>
      val traderID = masterTraders.Service.tryGetID(loginState.username)
      val negotiation = masterNegotiationHistories.Service.tryGet(negotiationID)
      val fiatsInOrderHistory = masterTransactionSendFiatRequestHistories.Service.getFiatsInOrder(negotiationID)

      def getFiatPegWallet(traderID: String) = masterFiats.Service.getFiatPegWallet(traderID)

      def getAsset(assetID: String) = masterProperties.Service.getPropertyMap(assetID)

      (for {
        traderID <- traderID
        negotiation <- negotiation
        fiatsInOrderHistory <- fiatsInOrderHistory
        fiatPegWallet <- getFiatPegWallet(traderID)
        asset <- getAsset(negotiation.assetID)
      } yield Ok(views.html.component.master.traderViewCompletedTradeRoomFinancial(fiatPegWallet.map(_.transactionAmount).sum, 0, negotiation.price - fiatsInOrderHistory, traderID, (asset.getOrElse(constants.Property.MODERATED.dataName,Some("")).getOrElse("") == constants.Boolean.TRUE)))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  //TODO: Whoever did this correct it
  def traderViewTradeRoomChecks(negotiationID: String): Action[AnyContent] = withTraderLoginAction { implicit loginState =>
    implicit request =>
      Future(Ok(views.html.component.master.traderViewTradeRoomChecks()))
  }

  def traderViewCompletedTradeRoomChecks(negotiationID: String): Action[AnyContent] = withTraderLoginAction { implicit loginState =>
    implicit request =>
      Future(Ok(views.html.component.master.traderViewCompletedTradeRoomChecks()))
  }

  def zoneOrderActions(negotiationID: String): Action[AnyContent] = withZoneLoginAction { implicit loginState =>
    implicit request =>
      val zoneID = masterZones.Service.tryGetID(loginState.username)
      val negotiation = masterNegotiations.Service.tryGet(negotiationID)

      def getTraderList(traderIDs: Seq[String]) = masterTraders.Service.getTraders(traderIDs)

      def getResult(zoneID: String, traderList: Seq[Trader], negotiation: Negotiation): Future[Result] = {
        if (traderList.map(_.zoneID) contains zoneID) {
          val asset = masterProperties.Service.getPropertyMap(negotiation.assetID)
          val order = masterOrders.Service.get(negotiationID)
          for {
            asset <- asset
            order <- order
          } yield Ok(views.html.component.master.zoneViewOrderActions(negotiation, asset, order))
        } else {
          Future(Unauthorized(views.html.trades(failures = Seq(constants.Response.UNAUTHORIZED))))
        }
      }

      (for {
        zoneID <- zoneID
        negotiation <- negotiation
        traderList <- getTraderList(Seq(negotiation.buyerTraderID, negotiation.sellerTraderID))
        result <- getResult(zoneID, traderList, negotiation)
      } yield result).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def traderViewNegotiationDocumentContent(negotiationID: String, documentType: String): Action[AnyContent] = withTraderLoginAction { implicit loginState =>
    implicit request =>
      val document = masterTransactionNegotiationFiles.Service.get(negotiationID, documentType)
      val negotiation = masterNegotiations.Service.tryGet(negotiationID)

      def getTraderList(traderIDs: Seq[String]) = masterTraders.Service.getTraders(traderIDs)

      def getOrganizationList(organizationIDs: Seq[String]) = masterOrganizations.Service.getOrganizations(organizationIDs)

      def getDocumentContent(document: Option[NegotiationFile]) = {
        if (document.isDefined) {
          Future(document.get.documentContent)
        } else {
          masterTransactionNegotiationFileHistories.Service.getDocumentContent(negotiationID, documentType)
        }
      }

      (for {
        document <- document
        negotiation <- negotiation
        traderList <- getTraderList(Seq(negotiation.sellerTraderID, negotiation.buyerTraderID))
        organizationList <- getOrganizationList(traderList.map(_.organizationID))
        documentContent <- getDocumentContent(document)
      } yield Ok(views.html.component.master.viewNegotiationDocumentContent(documentType, documentContent, negotiation, traderList, organizationList))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def traderViewAssetDocumentContent(assetID: String, documentType: String): Action[AnyContent] = withTraderLoginAction { implicit loginState =>
    implicit request =>
      val document = masterTransactionAssetFiles.Service.get(assetID, documentType)

      def getDocumentContent(document: Option[AssetFile]) = {
        if (document.isDefined) {
          Future(document.get.documentContent)
        } else {
          masterTransactionAssetFileHistories.Service.getDocumentContent(assetID, documentType)
        }
      }

      (for {
        document <- document
        documentContent <- getDocumentContent(document)
      } yield Ok(views.html.component.master.viewAssetDocumentContent(documentType, documentContent))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def organizationViewNegotiationDocumentContent(negotiationID: String, documentType: String): Action[AnyContent] = withOrganizationLoginAction { implicit loginState =>
    implicit request =>
      val document = masterTransactionNegotiationFiles.Service.get(negotiationID, documentType)
      val negotiation = masterNegotiations.Service.tryGet(negotiationID)

      def getTraderList(traderIDs: Seq[String]) = masterTraders.Service.getTraders(traderIDs)

      def getOrganizationList(organizationIDs: Seq[String]) = masterOrganizations.Service.getOrganizations(organizationIDs)

      def getDocumentContent(document: Option[NegotiationFile]) = {
        if (document.isDefined) {
          Future(document.get.documentContent)
        } else {
          masterTransactionNegotiationFileHistories.Service.getDocumentContent(negotiationID, documentType)
        }
      }

      (for {
        document <- document
        negotiation <- negotiation
        traderList <- getTraderList(Seq(negotiation.sellerTraderID, negotiation.buyerTraderID))
        organizationList <- getOrganizationList(traderList.map(_.organizationID))
        documentContent <- getDocumentContent(document)
      } yield Ok(views.html.component.master.viewNegotiationDocumentContent(documentType, documentContent, negotiation, traderList, organizationList))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def organizationViewAssetDocumentContent(assetID: String, documentType: String): Action[AnyContent] = withOrganizationLoginAction { implicit loginState =>
    implicit request =>
      val document = masterTransactionAssetFiles.Service.get(assetID, documentType)

      def getDocumentContent(document: Option[AssetFile]) = {
        if (document.isDefined) {
          Future(document.get.documentContent)
        } else {
          masterTransactionAssetFileHistories.Service.getDocumentContent(assetID, documentType)
        }
      }

      (for {
        document <- document
        documentContent <- getDocumentContent(document)
      } yield Ok(views.html.component.master.viewAssetDocumentContent(documentType, documentContent))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def zoneViewNegotiationDocumentContent(negotiationID: String, documentType: String): Action[AnyContent] = withZoneLoginAction { implicit loginState =>
    implicit request =>
      val document = masterTransactionNegotiationFiles.Service.get(negotiationID, documentType)
      val negotiation = masterNegotiations.Service.tryGet(negotiationID)

      def getTraderList(traderIDs: Seq[String]) = masterTraders.Service.getTraders(traderIDs)

      def getOrganizationList(organizationIDs: Seq[String]) = masterOrganizations.Service.getOrganizations(organizationIDs)

      def getDocumentContent(document: Option[NegotiationFile]) = {
        if (document.isDefined) {
          Future(document.get.documentContent)
        } else {
          masterTransactionNegotiationFileHistories.Service.getDocumentContent(negotiationID, documentType)
        }
      }

      (for {
        document <- document
        negotiation <- negotiation
        traderList <- getTraderList(Seq(negotiation.sellerTraderID, negotiation.buyerTraderID))
        organizationList <- getOrganizationList(traderList.map(_.organizationID))
        documentContent <- getDocumentContent(document)
      } yield Ok(views.html.component.master.viewNegotiationDocumentContent(documentType, documentContent, negotiation, traderList, organizationList))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def zoneViewAssetDocumentContent(assetID: String, documentType: String): Action[AnyContent] = withZoneLoginAction { implicit loginState =>
    implicit request =>
      val document = masterTransactionAssetFiles.Service.get(assetID, documentType)

      def getDocumentContent(document: Option[AssetFile]) = {
        if (document.isDefined) {
          Future(document.get.documentContent)
        } else {
          masterTransactionAssetFileHistories.Service.getDocumentContent(assetID, documentType)
        }
      }

      (for {
        document <- document
        documentContent <- getDocumentContent(document)
      } yield Ok(views.html.component.master.viewAssetDocumentContent(documentType, documentContent))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def traderViewIssueFiatRequestList: Action[AnyContent] = withTraderLoginAction { implicit loginState =>
    implicit request =>
      val traderID = masterTraders.Service.tryGetID(loginState.username)

      def getIssueFiatRequestList(traderID: String) = westernUnionFiatRequests.Service.getAll(traderID)

      def getRTCBList(issueFiatRequestIDs: Seq[String]) = westernUnionRTCBs.Service.getAll(issueFiatRequestIDs)

      (for {
        traderID <- traderID
        issueFiatRequestList <- getIssueFiatRequestList(traderID)
        rtcbList <- getRTCBList(issueFiatRequestList.map(_.id))
      } yield Ok(views.html.component.master.traderViewIssueFiatRequestList(issueFiatRequestList, rtcbList))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def organizationViewIssueFiatRequestList: Action[AnyContent] = withOrganizationLoginAction { implicit loginState =>
    implicit request =>
      val organizationID = masterOrganizations.Service.tryGetID(loginState.username)

      def getTraderList(organizationID: String) = masterTraders.Service.getOrganizationAcceptedTraderList(organizationID)

      def getIssueFiatRequestList(traderIDs: Seq[String]) = westernUnionFiatRequests.Service.getAllByTraderIDs(traderIDs)

      def getRTCBList(issueFiatRequestIDs: Seq[String]) = westernUnionRTCBs.Service.getAll(issueFiatRequestIDs)

      (for {
        organizationID <- organizationID
        traderList <- getTraderList(organizationID)
        issueFiatRequestList <- getIssueFiatRequestList(traderList.map(_.id))
        rtcbList <- getRTCBList(issueFiatRequestList.map(_.id))
      } yield Ok(views.html.component.master.organizationViewIssueFiatRequestList(traderList, issueFiatRequestList, rtcbList))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def zoneViewIssueFiatRequestList: Action[AnyContent] = withZoneLoginAction { implicit loginState =>
    implicit request =>

      val zoneID = masterZones.Service.tryGetID(loginState.username)

      def getOrganizationList(zoneID: String) = masterOrganizations.Service.getZoneAcceptedOrganizationList(zoneID)

      def getTraderList(zoneID: String) = masterTraders.Service.getZoneAcceptedTraderList(zoneID)

      def getIssueFiatRequestList(traderIDs: Seq[String]) = westernUnionFiatRequests.Service.getAllByTraderIDs(traderIDs)

      def getRTCBList(issueFiatRequestIDs: Seq[String]) = westernUnionRTCBs.Service.getAll(issueFiatRequestIDs)

      (for {
        zoneID <- zoneID
        organizationList <- getOrganizationList(zoneID)
        traderList <- getTraderList(zoneID)
        issueFiatRequestList <- getIssueFiatRequestList(traderList.map(_.id))
        rtcbList <- getRTCBList(issueFiatRequestList.map(_.id))
      } yield Ok(views.html.component.master.zoneViewIssueFiatRequestList(traderList, organizationList, issueFiatRequestList, rtcbList))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  //Transaction View Cards
  def zoneViewSendFiatRequests: Action[AnyContent] = withZoneLoginAction { implicit loginState =>
    implicit request =>
      Future(Ok(views.html.component.master.zoneViewSendFiatRequests()))
  }

  def zoneViewPendingSendFiatRequestList: Action[AnyContent] = withZoneLoginAction { implicit loginState =>
    implicit request =>
      val zoneID = masterZones.Service.tryGetID(loginState.username)

      def organizations(zoneID: String): Future[Seq[Organization]] = masterOrganizations.Service.getZoneAcceptedOrganizationList(zoneID)

      def traders(zoneID: String): Future[Seq[Trader]] = masterTraders.Service.getZoneAcceptedTraderList(zoneID)

      def sendFiatRequestList(traderIDs: Seq[String]): Future[Seq[masterTransaction.SendFiatRequest]] = masterTransactionSendFiatRequests.Service.getPendingSendFiatRequests(traderIDs)

      def sendFiatRequestHistoryList(traderIDs: Seq[String]): Future[Seq[masterTransaction.SendFiatRequest]] = masterTransactionSendFiatRequestHistories.Service.getPendingSendFiatRequests(traderIDs).map(_.map(_.convertToSendFiatRequest))

      (for {
        zoneID <- zoneID
        organizations <- organizations(zoneID)
        traders <- traders(zoneID)
        sendFiatRequestList <- sendFiatRequestList(traders.map(_.id))
        sendFiatRequestHistoryList <- sendFiatRequestHistoryList(traders.map(_.id))
      } yield Ok(views.html.component.master.zoneViewPendingSendFiatRequestList(sendFiatRequestList, sendFiatRequestHistoryList, organizations, traders))).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def zoneViewCompleteSendFiatRequestList: Action[AnyContent] = withZoneLoginAction { implicit loginState =>
    implicit request =>
      val zoneID = masterZones.Service.tryGetID(loginState.username)

      def organizations(zoneID: String): Future[Seq[Organization]] = masterOrganizations.Service.getZoneAcceptedOrganizationList(zoneID)

      def traders(zoneID: String): Future[Seq[Trader]] = masterTraders.Service.getZoneAcceptedTraderList(zoneID)

      def sendFiatRequestList(traderIDs: Seq[String]): Future[Seq[masterTransaction.SendFiatRequest]] = masterTransactionSendFiatRequests.Service.getCompleteSendFiatRequests(traderIDs)

      def sendFiatRequestHistoryList(traderIDs: Seq[String]): Future[Seq[masterTransaction.SendFiatRequest]] = masterTransactionSendFiatRequestHistories.Service.getCompleteSendFiatRequests(traderIDs).map(_.map(_.convertToSendFiatRequest))

      (for {
        zoneID <- zoneID
        organizations <- organizations(zoneID)
        traders <- traders(zoneID)
        sendFiatRequestList <- sendFiatRequestList(traders.map(_.id))
        sendFiatRequestHistoryList <- sendFiatRequestHistoryList(traders.map(_.id))
      } yield Ok(views.html.component.master.zoneViewSendFiatRequestList(sendFiatRequestList, sendFiatRequestHistoryList, organizations, traders))).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def zoneViewFailedSendFiatRequestList: Action[AnyContent] = withZoneLoginAction { implicit loginState =>
    implicit request =>
      val zoneID = masterZones.Service.tryGetID(loginState.username)

      def organizations(zoneID: String): Future[Seq[Organization]] = masterOrganizations.Service.getZoneAcceptedOrganizationList(zoneID)

      def traders(zoneID: String): Future[Seq[Trader]] = masterTraders.Service.getZoneAcceptedTraderList(zoneID)

      def sendFiatRequestList(traderIDs: Seq[String]): Future[Seq[masterTransaction.SendFiatRequest]] = masterTransactionSendFiatRequests.Service.getFailedSendFiatRequests(traderIDs)

      def sendFiatRequestHistoryList(traderIDs: Seq[String]): Future[Seq[masterTransaction.SendFiatRequest]] = masterTransactionSendFiatRequestHistories.Service.getFailedSendFiatRequests(traderIDs).map(_.map(_.convertToSendFiatRequest))

      (for {
        zoneID <- zoneID
        organizations <- organizations(zoneID)
        traders <- traders(zoneID)
        sendFiatRequestList <- sendFiatRequestList(traders.map(_.id))
        sendFiatRequestHistoryList <- sendFiatRequestHistoryList(traders.map(_.id))
      } yield Ok(views.html.component.master.zoneViewSendFiatRequestList(sendFiatRequestList, sendFiatRequestHistoryList, organizations, traders))).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def zoneViewRedeemFiatRequests: Action[AnyContent] = withZoneLoginAction { implicit loginState =>
    implicit request =>
      Future(Ok(views.html.component.master.zoneViewRedeemFiatRequests()))
  }

  def zoneViewPendingRedeemFiatRequestList: Action[AnyContent] = withZoneLoginAction { implicit loginState =>
    implicit request =>
      val zoneID = masterZones.Service.tryGetID(loginState.username)

      def organizations(zoneID: String): Future[Seq[Organization]] = masterOrganizations.Service.getZoneAcceptedOrganizationList(zoneID)

      def traders(zoneID: String): Future[Seq[Trader]] = masterTraders.Service.getZoneAcceptedTraderList(zoneID)

      def redeemFiatRequestList(traderIDs: Seq[String]): Future[Seq[masterTransaction.RedeemFiatRequest]] = masterTransactionRedeemFiatRequests.Service.getPendingRedeemFiatRequests(traderIDs)

      (for {
        zoneID <- zoneID
        organizations <- organizations(zoneID)
        traders <- traders(zoneID)
        redeemFiatRequestList <- redeemFiatRequestList(traders.map(_.id))
      } yield Ok(views.html.component.master.zoneViewPendingRedeemFiatRequestList(redeemFiatRequestList, organizations, traders))).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }


  def zoneViewCompleteRedeemFiatRequestList: Action[AnyContent] = withZoneLoginAction { implicit loginState =>
    implicit request =>
      val zoneID = masterZones.Service.tryGetID(loginState.username)

      def organizations(zoneID: String): Future[Seq[Organization]] = masterOrganizations.Service.getZoneAcceptedOrganizationList(zoneID)

      def traders(zoneID: String): Future[Seq[Trader]] = masterTraders.Service.getZoneAcceptedTraderList(zoneID)

      def redeemFiatRequestList(traderIDs: Seq[String]): Future[Seq[masterTransaction.RedeemFiatRequest]] = masterTransactionRedeemFiatRequests.Service.getCompleteRedeemFiatRequests(traderIDs)

      (for {
        zoneID <- zoneID
        organizations <- organizations(zoneID)
        traders <- traders(zoneID)
        redeemFiatRequestList <- redeemFiatRequestList(traders.map(_.id))
      } yield Ok(views.html.component.master.zoneViewRedeemFiatRequestList(redeemFiatRequestList, organizations, traders))).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }


  def zoneViewFailedRedeemFiatRequestList: Action[AnyContent] = withZoneLoginAction { implicit loginState =>
    implicit request =>
      val zoneID = masterZones.Service.tryGetID(loginState.username)

      def organizations(zoneID: String): Future[Seq[Organization]] = masterOrganizations.Service.getZoneAcceptedOrganizationList(zoneID)

      def traders(zoneID: String): Future[Seq[Trader]] = masterTraders.Service.getZoneAcceptedTraderList(zoneID)

      def redeemFiatRequestList(traderIDs: Seq[String]): Future[Seq[masterTransaction.RedeemFiatRequest]] = masterTransactionRedeemFiatRequests.Service.getFailedRedeemFiatRequests(traderIDs)

      (for {
        zoneID <- zoneID
        organizations <- organizations(zoneID)
        traders <- traders(zoneID)
        redeemFiatRequestList <- redeemFiatRequestList(traders.map(_.id))
      } yield Ok(views.html.component.master.zoneViewRedeemFiatRequestList(redeemFiatRequestList, organizations, traders))).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def zoneViewReceiveFiatList: Action[AnyContent] = withZoneLoginAction { implicit loginState =>
    implicit request =>
      val zoneID = masterZones.Service.tryGetID(loginState.username)

      def organizations(zoneID: String): Future[Seq[Organization]] = masterOrganizations.Service.getZoneAcceptedOrganizationList(zoneID)

      def traders(zoneID: String): Future[Seq[Trader]] = masterTraders.Service.getZoneAcceptedTraderList(zoneID)

      def receiveFiatList(traderIDs: Seq[String]): Future[Seq[masterTransaction.ReceiveFiat]] = masterTransactionReceiveFiats.Service.get(traderIDs)

      def receiveFiatHistoryList(traderIDs: Seq[String]): Future[Seq[masterTransaction.ReceiveFiat]] = masterTransactionReceiveFiatHistories.Service.get(traderIDs).map(_.map(_.convertToReceiveFiat))

      (for {
        zoneID <- zoneID
        organizations <- organizations(zoneID)
        traders <- traders(zoneID)
        receiveFiatList <- receiveFiatList(traders.map(_.id))
        receiveFiatHistoryList <- receiveFiatHistoryList(traders.map(_.id))
      } yield Ok(views.html.component.master.zoneViewReceiveFiatList(receiveFiatList, receiveFiatHistoryList, organizations, traders))).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  //Transactions organization
  def organizationViewSendFiatRequests: Action[AnyContent] = withOrganizationLoginAction { implicit loginState =>
    implicit request =>
      Future(Ok(views.html.component.master.organizationViewSendFiatRequests()))
  }

  def organizationViewPendingSendFiatRequestList: Action[AnyContent] = withOrganizationLoginAction { implicit loginState =>
    implicit request =>
      val organizationID = masterOrganizations.Service.tryGetID(loginState.username)

      def traders(organizationID: String): Future[Seq[Trader]] = masterTraders.Service.getOrganizationAcceptedTraderList(organizationID)

      def sendFiatRequestList(traderIDs: Seq[String]): Future[Seq[masterTransaction.SendFiatRequest]] = masterTransactionSendFiatRequests.Service.getPendingSendFiatRequests(traderIDs)

      def sendFiatRequestHistoryList(traderIDs: Seq[String]): Future[Seq[masterTransaction.SendFiatRequest]] = masterTransactionSendFiatRequestHistories.Service.getPendingSendFiatRequests(traderIDs).map(_.map(_.convertToSendFiatRequest))

      (for {
        organizationID <- organizationID
        traders <- traders(organizationID)
        sendFiatRequestList <- sendFiatRequestList(traders.map(_.id))
        sendFiatRequestHistoryList <- sendFiatRequestHistoryList(traders.map(_.id))
      } yield Ok(views.html.component.master.organizationViewSendFiatRequestList(sendFiatRequestList, sendFiatRequestHistoryList, traders))).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def organizationViewCompleteSendFiatRequestList: Action[AnyContent] = withOrganizationLoginAction { implicit loginState =>
    implicit request =>
      val organizationID = masterOrganizations.Service.tryGetID(loginState.username)

      def traders(organizationID: String): Future[Seq[Trader]] = masterTraders.Service.getOrganizationAcceptedTraderList(organizationID)

      def sendFiatRequestList(traderIDs: Seq[String]): Future[Seq[masterTransaction.SendFiatRequest]] = masterTransactionSendFiatRequests.Service.getCompleteSendFiatRequests(traderIDs)

      def sendFiatRequestHistoryList(traderIDs: Seq[String]): Future[Seq[masterTransaction.SendFiatRequest]] = masterTransactionSendFiatRequestHistories.Service.getCompleteSendFiatRequests(traderIDs).map(_.map(_.convertToSendFiatRequest))

      (for {
        organizationID <- organizationID
        traders <- traders(organizationID)
        sendFiatRequestList <- sendFiatRequestList(traders.map(_.id))
        sendFiatRequestHistoryList <- sendFiatRequestHistoryList(traders.map(_.id))
      } yield Ok(views.html.component.master.organizationViewSendFiatRequestList(sendFiatRequestList, sendFiatRequestHistoryList, traders))).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def organizationViewFailedSendFiatRequestList: Action[AnyContent] = withOrganizationLoginAction { implicit loginState =>
    implicit request =>
      val organizationID = masterOrganizations.Service.tryGetID(loginState.username)

      def traders(organizationID: String): Future[Seq[Trader]] = masterTraders.Service.getOrganizationAcceptedTraderList(organizationID)

      def sendFiatRequestList(traderIDs: Seq[String]): Future[Seq[masterTransaction.SendFiatRequest]] = masterTransactionSendFiatRequests.Service.getFailedSendFiatRequests(traderIDs)

      def sendFiatRequestHistoryList(traderIDs: Seq[String]): Future[Seq[masterTransaction.SendFiatRequest]] = masterTransactionSendFiatRequestHistories.Service.getFailedSendFiatRequests(traderIDs).map(_.map(_.convertToSendFiatRequest))

      (for {
        organizationID <- organizationID
        traders <- traders(organizationID)
        sendFiatRequestList <- sendFiatRequestList(traders.map(_.id))
        sendFiatRequestHistoryList <- sendFiatRequestHistoryList(traders.map(_.id))
      } yield Ok(views.html.component.master.organizationViewSendFiatRequestList(sendFiatRequestList, sendFiatRequestHistoryList, traders))).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def organizationViewRedeemFiatRequests: Action[AnyContent] = withOrganizationLoginAction { implicit loginState =>
    implicit request =>
      Future(Ok(views.html.component.master.organizationViewRedeemFiatRequests()))
  }

  def organizationViewPendingRedeemFiatRequestList: Action[AnyContent] = withOrganizationLoginAction { implicit loginState =>
    implicit request =>
      val organizationID = masterOrganizations.Service.tryGetID(loginState.username)

      def traders(organizationID: String): Future[Seq[Trader]] = masterTraders.Service.getOrganizationAcceptedTraderList(organizationID)

      def redeemFiatRequestList(traderIDs: Seq[String]): Future[Seq[masterTransaction.RedeemFiatRequest]] = masterTransactionRedeemFiatRequests.Service.getPendingRedeemFiatRequests(traderIDs)

      (for {
        organizationID <- organizationID
        traders <- traders(organizationID)
        redeemFiatRequestList <- redeemFiatRequestList(traders.map(_.id))
      } yield Ok(views.html.component.master.organizationViewRedeemFiatRequestList(redeemFiatRequestList, traders))).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def organizationViewCompleteRedeemFiatRequestList: Action[AnyContent] = withOrganizationLoginAction { implicit loginState =>
    implicit request =>
      val organizationID = masterOrganizations.Service.tryGetID(loginState.username)

      def traders(organizationID: String): Future[Seq[Trader]] = masterTraders.Service.getOrganizationAcceptedTraderList(organizationID)

      def redeemFiatRequestList(traderIDs: Seq[String]): Future[Seq[masterTransaction.RedeemFiatRequest]] = masterTransactionRedeemFiatRequests.Service.getCompleteRedeemFiatRequests(traderIDs)

      (for {
        organizationID <- organizationID
        traders <- traders(organizationID)
        redeemFiatRequestList <- redeemFiatRequestList(traders.map(_.id))
      } yield Ok(views.html.component.master.organizationViewRedeemFiatRequestList(redeemFiatRequestList, traders))).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def organizationViewFailedRedeemFiatRequestList: Action[AnyContent] = withOrganizationLoginAction { implicit loginState =>
    implicit request =>
      val organizationID = masterOrganizations.Service.tryGetID(loginState.username)

      def traders(organizationID: String): Future[Seq[Trader]] = masterTraders.Service.getOrganizationAcceptedTraderList(organizationID)

      def redeemFiatRequestList(traderIDs: Seq[String]): Future[Seq[masterTransaction.RedeemFiatRequest]] = masterTransactionRedeemFiatRequests.Service.getFailedRedeemFiatRequests(traderIDs)

      (for {
        organizationID <- organizationID
        traders <- traders(organizationID)
        redeemFiatRequestList <- redeemFiatRequestList(traders.map(_.id))
      } yield Ok(views.html.component.master.organizationViewRedeemFiatRequestList(redeemFiatRequestList, traders))).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def organizationViewReceiveFiatList: Action[AnyContent] = withOrganizationLoginAction { implicit loginState =>
    implicit request =>
      val organizationID = masterOrganizations.Service.tryGetID(loginState.username)

      def traders(organizationID: String): Future[Seq[Trader]] = masterTraders.Service.getOrganizationAcceptedTraderList(organizationID)

      def receiveFiatList(traderIDs: Seq[String]): Future[Seq[masterTransaction.ReceiveFiat]] = masterTransactionReceiveFiats.Service.get(traderIDs)

      def receiveFiatHistoryList(traderIDs: Seq[String]): Future[Seq[masterTransaction.ReceiveFiat]] = masterTransactionReceiveFiatHistories.Service.get(traderIDs).map(_.map(_.convertToReceiveFiat))

      (for {
        organizationID <- organizationID
        traders <- traders(organizationID)
        receiveFiatList <- receiveFiatList(traders.map(_.id))
        receiveFiatHistoryList <- receiveFiatHistoryList(traders.map(_.id))
      } yield Ok(views.html.component.master.organizationViewReceiveFiatList(receiveFiatList, receiveFiatHistoryList, traders))).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  //Transactions Trader
  def traderViewSendFiatRequests: Action[AnyContent] = withTraderLoginAction { implicit loginState =>
    implicit request =>
      Future(Ok(views.html.component.master.traderViewSendFiatRequests()))
  }

  def traderViewPendingSendFiatRequestList: Action[AnyContent] = withTraderLoginAction { implicit loginState =>
    implicit request =>
      val trader = masterTraders.Service.tryGetByAccountID(loginState.username)

      def sendFiatRequestList(traderID: String): Future[Seq[masterTransaction.SendFiatRequest]] = masterTransactionSendFiatRequests.Service.getPendingSendFiatRequests(traderID)

      def sendFiatRequestHistoryList(traderID: String): Future[Seq[masterTransaction.SendFiatRequest]] = masterTransactionSendFiatRequestHistories.Service.getPendingSendFiatRequests(traderID).map(_.map(_.convertToSendFiatRequest))

      (for {
        trader <- trader
        sendFiatRequestList <- sendFiatRequestList(trader.id)
        sendFiatRequestHistoryList <- sendFiatRequestHistoryList(trader.id)
      } yield Ok(views.html.component.master.traderViewSendFiatRequestList(sendFiatRequestList, sendFiatRequestHistoryList))).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def traderViewCompleteSendFiatRequestList: Action[AnyContent] = withTraderLoginAction { implicit loginState =>
    implicit request =>
      val trader = masterTraders.Service.tryGetByAccountID(loginState.username)

      def sendFiatRequestList(traderID: String): Future[Seq[masterTransaction.SendFiatRequest]] = masterTransactionSendFiatRequests.Service.getCompleteSendFiatRequests(traderID)

      def sendFiatRequestHistoryList(traderID: String): Future[Seq[masterTransaction.SendFiatRequest]] = masterTransactionSendFiatRequestHistories.Service.getCompleteSendFiatRequests(traderID).map(_.map(_.convertToSendFiatRequest))

      (for {
        trader <- trader
        sendFiatRequestList <- sendFiatRequestList(trader.id)
        sendFiatRequestHistoryList <- sendFiatRequestHistoryList(trader.id)
      } yield Ok(views.html.component.master.traderViewSendFiatRequestList(sendFiatRequestList, sendFiatRequestHistoryList))).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def traderViewFailedSendFiatRequestList: Action[AnyContent] = withTraderLoginAction { implicit loginState =>
    implicit request =>
      val trader = masterTraders.Service.tryGetByAccountID(loginState.username)

      def sendFiatRequestList(traderID: String): Future[Seq[masterTransaction.SendFiatRequest]] = masterTransactionSendFiatRequests.Service.getFailedSendFiatRequests(traderID)

      def sendFiatRequestHistoryList(traderID: String): Future[Seq[masterTransaction.SendFiatRequest]] = masterTransactionSendFiatRequestHistories.Service.getFailedSendFiatRequests(traderID).map(_.map(_.convertToSendFiatRequest))

      (for {
        trader <- trader
        sendFiatRequestList <- sendFiatRequestList(trader.id)
        sendFiatRequestHistoryList <- sendFiatRequestHistoryList(trader.id)
      } yield Ok(views.html.component.master.traderViewSendFiatRequestList(sendFiatRequestList, sendFiatRequestHistoryList))).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def traderViewRedeemFiatRequests: Action[AnyContent] = withTraderLoginAction { implicit loginState =>
    implicit request =>
      Future(Ok(views.html.component.master.traderViewRedeemFiatRequests()))
  }

  def traderViewPendingRedeemFiatRequestList: Action[AnyContent] = withTraderLoginAction { implicit loginState =>
    implicit request =>
      val trader = masterTraders.Service.tryGetByAccountID(loginState.username)

      def redeemFiatRequestList(traderID: String): Future[Seq[masterTransaction.RedeemFiatRequest]] = masterTransactionRedeemFiatRequests.Service.getPendingRedeemFiatRequests(traderID)

      (for {
        trader <- trader
        redeemFiatRequestList <- redeemFiatRequestList(trader.id)
      } yield Ok(views.html.component.master.traderViewRedeemFiatRequestList(redeemFiatRequestList))).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def traderViewCompleteRedeemFiatRequestList: Action[AnyContent] = withTraderLoginAction { implicit loginState =>
    implicit request =>
      val trader = masterTraders.Service.tryGetByAccountID(loginState.username)

      def redeemFiatRequestList(traderID: String): Future[Seq[masterTransaction.RedeemFiatRequest]] = masterTransactionRedeemFiatRequests.Service.getCompleteRedeemFiatRequests(traderID)

      (for {
        trader <- trader
        redeemFiatRequestList <- redeemFiatRequestList(trader.id)
      } yield Ok(views.html.component.master.traderViewRedeemFiatRequestList(redeemFiatRequestList))).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def traderViewFailedRedeemFiatRequestList: Action[AnyContent] = withTraderLoginAction { implicit loginState =>
    implicit request =>
      val trader = masterTraders.Service.tryGetByAccountID(loginState.username)

      def redeemFiatRequestList(traderID: String): Future[Seq[masterTransaction.RedeemFiatRequest]] = masterTransactionRedeemFiatRequests.Service.getFailedRedeemFiatRequests(traderID)

      (for {
        trader <- trader
        redeemFiatRequestList <- redeemFiatRequestList(trader.id)
      } yield Ok(views.html.component.master.traderViewRedeemFiatRequestList(redeemFiatRequestList))).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def traderViewReceiveFiatList: Action[AnyContent] = withTraderLoginAction { implicit loginState =>
    implicit request =>
      val trader = masterTraders.Service.tryGetByAccountID(loginState.username)

      def receiveFiatList(traderID: String): Future[Seq[masterTransaction.ReceiveFiat]] = masterTransactionReceiveFiats.Service.get(traderID)

      def receiveFiatHistoryList(traderID: String): Future[Seq[masterTransaction.ReceiveFiat]] = masterTransactionReceiveFiatHistories.Service.get(traderID).map(_.map(_.convertToReceiveFiat))

      (for {
        trader <- trader
        receiveFiatList <- receiveFiatList(trader.id)
        receiveFiatHistoryList <- receiveFiatHistoryList(trader.id)
      } yield Ok(views.html.component.master.traderViewReceiveFiatList(receiveFiatList, receiveFiatHistoryList))).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def traderViewNegotiation(negotiationID: String): Action[AnyContent] = withTraderLoginAction { implicit loginState =>
    implicit request =>
      val trader = masterTraders.Service.tryGetByAccountID(loginState.username)
      val negotiation = masterNegotiations.Service.tryGet(negotiationID)

      def getResult(trader: Trader, negotiation: Negotiation): Future[Result] = {
        if (trader.id == negotiation.buyerTraderID || trader.id == negotiation.sellerTraderID) {
          val assetProperties = masterProperties.Service.getPropertyMap(negotiation.assetID)
          val counterPartyTrader = masterTraders.Service.tryGet(if (trader.id == negotiation.sellerTraderID) negotiation.buyerTraderID else negotiation.sellerTraderID)

          def getCounterPartyOrganization(organizationID: String): Future[Organization] = masterOrganizations.Service.tryGet(organizationID)

          for {
            assetProperties <- assetProperties
            counterPartyTrader <- counterPartyTrader
            counterPartyOrganization <- getCounterPartyOrganization(counterPartyTrader.organizationID)
          } yield Ok(views.html.component.master.traderViewNegotiation(negotiation = negotiation, assetProperties = assetProperties, counterPartyTrader = counterPartyTrader, counterPartyOrganization = counterPartyOrganization))
        } else {
          throw new BaseException(constants.Response.UNAUTHORIZED)
        }
      }

      (for {
        trader <- trader
        negotiation <- negotiation
        result <- getResult(trader, negotiation)
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def organizationViewNegotiation(negotiationID: String): Action[AnyContent] = withOrganizationLoginAction { implicit loginState =>
    implicit request =>
      val organizationID = masterOrganizations.Service.tryGetID(loginState.username)
      val negotiation = masterNegotiations.Service.tryGet(negotiationID)

      def getNegotiationTraders(traderIDs: Seq[String]): Future[Seq[Trader]] = masterTraders.Service.getTraders(traderIDs)

      def getResult(organizationID: String, negotiationTraders: Seq[Trader], negotiation: Negotiation): Future[Result] = {
        if (negotiationTraders.map(_.organizationID) contains organizationID) {
          val asset = masterProperties.Service.getPropertyMap(negotiation.assetID)
          val counterPartyOrganization = masterOrganizations.Service.tryGet(negotiationTraders.map(_.organizationID).filterNot(_ == organizationID).headOption.getOrElse(""))

          for {
            asset <- asset
            counterPartyOrganization <- counterPartyOrganization
          } yield Ok(views.html.component.master.organizationViewNegotiation(negotiation = negotiation, asset = asset, trader = negotiationTraders.find(_.organizationID == organizationID), counterPartyTrader = negotiationTraders.filterNot(_.organizationID == organizationID).headOption, counterPartyOrganization))
        } else {
          throw new BaseException(constants.Response.UNAUTHORIZED)
        }
      }

      (for {
        organizationID <- organizationID
        negotiation <- negotiation
        negotiationTraders <- getNegotiationTraders(Seq(negotiation.sellerTraderID, negotiation.buyerTraderID))
        result <- getResult(organizationID, negotiationTraders, negotiation)
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def latestBlockHeight(): Action[AnyContent] = withoutLoginActionAsync { implicit loginState =>
    implicit request =>
      val latestBlock = blockchainBlocks.Service.getLatestBlock
      val averageBlockTime = blockchainAverageBlockTimes.Service.get

      def getProposer(proposerAddress: String) = blockchainValidators.Service.tryGetProposerName(proposerAddress)

      (for {
        latestBlock <- latestBlock
        averageBlockTime <- averageBlockTime
        proposer <- getProposer(latestBlock.proposerAddress)
      } yield Ok(views.html.component.blockchain.latestBlockHeight(blockHeight = latestBlock.height, proposer = proposer, time = latestBlock.time, averageBlockTime = averageBlockTime, chainID = chainID))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def tokensStatistics(): Action[AnyContent] = withoutLoginActionAsync { implicit loginState =>
    implicit request =>
      val tokens = blockchainTokens.Service.getAll
      (for {
        tokens <- tokens
      } yield Ok(views.html.component.blockchain.tokensStatistics(tokens = tokens, stakingDenom = stakingDenom))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def votingPowers(): Action[AnyContent] = withoutLoginActionAsync { implicit loginState =>
    implicit request =>
      val allValidators = blockchainValidators.Service.getAll

      def getVotingPowerMap(validators: Seq[Validator]): ListMap[String, Double] = validators.map(validator => validator.description.moniker.getOrElse("") -> validator.tokens.toDouble)(collection.breakOut)

      (for {
        allValidators <- allValidators
      } yield Ok(views.html.component.blockchain.votingPowers(votingPowerMap = getVotingPowerMap(allValidators.filter(x => x.status == bondedStatus)), totalActiveValidators = allValidators.count(x => x.status == bondedStatus), totalValidators = allValidators.length))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def tokensPrices(): Action[AnyContent] = withoutLoginActionAsync { implicit loginState =>
    implicit request =>
      val allDenoms = blockchainTokens.Service.getAllDenoms

      def allTokenPrices(allDenoms: Seq[String]) = masterTransactionTokenPrices.Service.getLatest(n = 5, totalTokens = allDenoms.length)

      def getTokenPricesMap(allTokenPrices: Seq[TokenPrice], allDenoms: Seq[String]): Map[String, ListMap[String, Double]] = allDenoms.map(denom => denom -> ListMap(allTokenPrices.filter(_.denom == denom).map(tokenPrice => (tokenPrice.createdOn.getOrElse(throw new BaseException(constants.Response.TIME_NOT_FOUND)).toString, tokenPrice.price)): _*))(collection.breakOut)

      (for {
        allDenoms <- allDenoms
        allTokenPrices <- allTokenPrices(allDenoms)
      } yield Ok(views.html.component.blockchain.tokensPrices(getTokenPricesMap(allTokenPrices, allDenoms)))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def accountWallet(address: String): Action[AnyContent] = withoutLoginActionAsync { implicit loginState =>
    implicit request =>
      val operatorAddress = utilities.Bech32.convertAccountAddressToOperatorAddress(address)
      val isValidator = blockchainValidators.Service.exists(operatorAddress)
      val account = blockchainAccounts.Service.tryGet(address)
      val delegations = blockchainDelegations.Service.getAllForDelegator(address)
      val undelegations = blockchainUndelegations.Service.getAllByDelegator(address)
      val allDenoms = blockchainTokens.Service.getAllDenoms

      def getRewards(isValidator: Boolean): Future[(MicroNumber, MicroNumber)] = if (isValidator) {
        getValidatorSelfBondAndCommissionRewards.Service.get(operatorAddress).map(x => (x.result.self_bond_rewards.fold(MicroNumber.zero)(x => x.headOption.fold(MicroNumber.zero)(_.amount)), x.result.val_commission.fold(MicroNumber.zero)(x => x.headOption.fold(MicroNumber.zero)(_.amount))))
      } else getDelegatorRewards.Service.get(address).map(x => (x.result.total.fold(MicroNumber.zero)(_.headOption.fold(MicroNumber.zero)(_.amount)), MicroNumber.zero))

      def getValidatorsDelegated(operatorAddresses: Seq[String]): Future[Seq[Validator]] = blockchainValidators.Service.getByOperatorAddresses(operatorAddresses)

      def getDelegatedAmount(delegations: Seq[Delegation], validators: Seq[Validator]): MicroNumber = delegations.map(delegation => utilities.Delegations.getTokenAmountFromShares(validator = validators.find(_.operatorAddress == delegation.validatorAddress).getOrElse(throw new BaseException(constants.Response.VALIDATOR_NOT_FOUND)), shares = delegation.shares)).sum

      def getUndelegatingAmount(undelegations: Seq[Undelegation]): MicroNumber = undelegations.map(_.entries.map(_.balance).sum).sum

      (for {
        isValidator <- isValidator
        account <- account
        (delegationRewards, commissionRewards) <- getRewards(isValidator)
        delegations <- delegations
        undelegations <- undelegations
        validators <- getValidatorsDelegated(delegations.map(_.validatorAddress))
        allDenoms <- allDenoms
      } yield Ok(views.html.component.blockchain.accountWallet(address = address, accountBalances = account.coins, delegatedAmount = getDelegatedAmount(delegations, validators), undelegatingAmount = getUndelegatingAmount(undelegations), delegationRewards = delegationRewards, isValidator = isValidator, commissionRewards = commissionRewards, stakingDenom = stakingDenom, totalTokens = allDenoms.length))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def accountDelegations(address: String): Action[AnyContent] = withoutLoginActionAsync { implicit loginState =>
    implicit request =>
      val delegations = blockchainDelegations.Service.getAllForDelegator(address)
      val undelegations = blockchainUndelegations.Service.getAllByDelegator(address)
      val validators = blockchainValidators.Service.getAll

      def getDelegationsMap(delegations: Seq[Delegation], validators: Seq[Validator]) = ListMap(delegations.map(delegation => delegation.validatorAddress -> utilities.Delegations.getTokenAmountFromShares(validator = validators.find(_.operatorAddress == delegation.validatorAddress).getOrElse(throw new BaseException(constants.Response.VALIDATOR_NOT_FOUND)), shares = delegation.shares)): _*)

      def getUndelegationsMap(undelegations: Seq[Undelegation]) = ListMap(undelegations.map(undelegation => undelegation.validatorAddress -> undelegation.entries): _*)

      def getValidatorsMoniker(validators: Seq[Validator]) = Map(validators.map(validator => validator.operatorAddress -> validator.description.moniker.getOrElse(validator.operatorAddress)): _*)

      (for {
        delegations <- delegations
        undelegations <- undelegations
        validators <- validators
      } yield Ok(views.html.component.blockchain.accountDelegations(delegations = getDelegationsMap(delegations, validators), undelegations = getUndelegationsMap(undelegations), validatorsMoniker = getValidatorsMoniker(validators)))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def accountTransactions(address: String): Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
      Ok(views.html.component.blockchain.accountTransactions(address))
  }

  def accountTransactionsPerPage(address: String, page: Int): Action[AnyContent] = withoutLoginActionAsync { implicit loginState =>
    implicit request =>
      val transactions = blockchainTransactions.Service.getTransactionsPerPageByAddress(address, page)
      (for {
        transactions <- transactions
      } yield Ok(views.html.component.blockchain.accountTransactionsPerPage(transactions))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def blockList(): Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
      Ok(views.html.component.blockchain.blockList())
  }

  def blockListPage(pageNumber: Int = 1): Action[AnyContent] = withoutLoginActionAsync { implicit loginState =>
    implicit request =>

      val result = if (pageNumber <= 0) Future(BadRequest) else {
        val blocks = blockchainBlocks.Service.getBlocksPerPage(pageNumber)
        val validators = blockchainValidators.Service.getAll

        def getNumberOfTransactions(blockHeights: Seq[Int]) = blockchainTransactions.Service.getNumberOfTransactions(blockHeights)

        def getProposers(blocks: Seq[Block], validators: Seq[Validator]): Future[Map[Int, String]] = Future {
          blocks.map { block =>
            val validator = validators.find(_.hexAddress == block.proposerAddress).getOrElse(throw new BaseException(constants.Response.VALIDATOR_NOT_FOUND))
            block.height -> validator.description.moniker.getOrElse(validator.operatorAddress)
          }(collection.breakOut)
        }

        for {
          blocks <- blocks
          validators <- validators
          proposers <- getProposers(blocks, validators)
          numberOfTxs <- getNumberOfTransactions(blocks.map(_.height))
        } yield Ok(views.html.component.blockchain.blockListPage(blocks, numberOfTxs, proposers))
      }

      (for {
        result <- result
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def blockDetails(height: Int): Action[AnyContent] = withoutLoginActionAsync { implicit loginState =>
    implicit request =>
      val block = blockchainBlocks.Service.tryGet(height)
      val numTxs = blockchainTransactions.Service.getNumberOfTransactions(height)

      def getProposer(hexAddress: String) = blockchainValidators.Service.tryGetProposerName(hexAddress)

      (for {
        block <- block
        numTxs <- numTxs
        proposer <- getProposer(block.proposerAddress)
      } yield Ok(views.html.component.blockchain.blockDetails(block, proposer, numTxs))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def blockTransactions(height: Int): Action[AnyContent] = withoutLoginActionAsync { implicit loginState =>
    implicit request =>
      val transactions = blockchainTransactions.Service.getTransactions(height)

      (for {
        transactions <- transactions
      } yield Ok(views.html.component.blockchain.blockTransactions(height, transactions))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def transactionList(): Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
      Ok(views.html.component.blockchain.transactionList())
  }

  def transactionListPage(pageNumber: Int = 1): Action[AnyContent] = withoutLoginActionAsync { implicit loginState =>
    implicit request =>

      (if (pageNumber <= 0) Future(BadRequest)
      else {
        val transactions = blockchainTransactions.Service.getTransactionsPerPage(pageNumber)
        for {
          transactions <- transactions
        } yield Ok(views.html.component.blockchain.transactionListPage(transactions))
      }).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def transactionDetails(txHash: String): Action[AnyContent] = withoutLoginActionAsync { implicit loginState =>
    implicit request =>
      val transaction = blockchainTransactions.Service.tryGet(txHash)

      (for {
        transaction <- transaction
      } yield Ok(views.html.component.blockchain.transactionDetails(transaction))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def transactionMessages(txHash: String): Action[AnyContent] = withoutLoginActionAsync { implicit loginState =>
    implicit request =>
      val messages = blockchainTransactions.Service.tryGetMessages(txHash)

      (for {
        messages <- messages
      } yield Ok(views.html.component.blockchain.transactionMessages(txHash, messages))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def validatorList(): Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
      Ok(views.html.component.blockchain.validatorList())
  }

  def activeValidatorList(): Action[AnyContent] = withoutLoginActionAsync { implicit loginState =>
    implicit request =>
      val validators = blockchainValidators.Service.getAllActiveValidatorList
      (for {
        validators <- validators
      } yield Ok(views.html.component.blockchain.activeValidatorList(validators, validators.map(_.tokens).sum))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def inactiveValidatorList(): Action[AnyContent] = withoutLoginActionAsync { implicit loginState =>
    implicit request =>
      val validators = blockchainValidators.Service.getAllInactiveValidatorList
      (for {
        validators <- validators
      } yield Ok(views.html.component.blockchain.inactiveValidatorList(validators))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def validatorDetails(address: String): Action[AnyContent] = withoutLoginActionAsync { implicit loginState =>
    implicit request =>
      val validator = blockchainValidators.Service.tryGet(address)
      val totalBondedAmount = blockchainTokens.Service.getTotalBondedAmount
      (for {
        validator <- validator
        totalBondedAmount <- totalBondedAmount
      } yield Ok(views.html.component.blockchain.validatorDetails(validator, utilities.Bech32.convertOperatorAddressToAccountAddress(validator.operatorAddress), (validator.tokens * 100 / totalBondedAmount).toRoundedOffString(), bondedStatus))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def validatorUptime(address: String, n: Int): Action[AnyContent] = withoutLoginActionAsync { implicit loginState =>
    implicit request =>
      val hexAddress = if (utilities.Validator.isHexAddress(address)) Future(address) else blockchainValidators.Service.tryGetHexAddress(address)
      val lastNBlocks = blockchainBlocks.Service.getLastNBlocks(n)

      def getUptime(lastNBlocks: Seq[Block], validatorHexAddress: String): Double = (lastNBlocks.count(block => block.validators.contains(validatorHexAddress)) * 100.0) / n

      def getUptimeMap(lastNBlocks: Seq[Block], validatorHexAddress: String): ListMap[Int, Boolean] = ListMap(lastNBlocks.map(block => block.height -> block.validators.contains(validatorHexAddress)): _*)

      (for {
        hexAddress <- hexAddress
        lastNBlocks <- lastNBlocks
      } yield Ok(views.html.component.blockchain.validatorUptime(uptime = getUptime(lastNBlocks, hexAddress), uptimeMap = getUptimeMap(lastNBlocks, hexAddress), hexAddress = hexAddress))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def validatorDelegations(address: String): Action[AnyContent] = withoutLoginActionAsync { implicit loginState =>
    implicit request =>
      val operatorAddress = if (utilities.Validator.isHexAddress(address)) blockchainValidators.Service.tryGetOperatorAddress(address) else Future(address)
      val validator = blockchainValidators.Service.tryGet(address)

      def getDelegations(operatorAddress: String) = blockchainDelegations.Service.getAllForValidator(operatorAddress)

      def getDelegationsMap(delegations: Seq[Delegation], validator: Validator) = Future {
        val selfDelegated = delegations.find(x => x.delegatorAddress == utilities.Bech32.convertOperatorAddressToAccountAddress(x.validatorAddress)).fold(BigDecimal(0.0))(_.shares)
        val othersDelegated = validator.delegatorShares - selfDelegated
        val delegationsMap = ListMap(constants.View.SELF_DELEGATED -> selfDelegated.toDouble, constants.View.OTHERS_DELEGATED -> othersDelegated.toDouble)
        (delegationsMap, (selfDelegated * 100.0 / validator.delegatorShares).toDouble, (othersDelegated * 100.0 / validator.delegatorShares).toDouble)
      }

      (for {
        operatorAddress <- operatorAddress
        validator <- validator
        delegations <- getDelegations(operatorAddress)
        (delegationsMap, selfDelegatedPercentage, othersDelegatedPercentage) <- getDelegationsMap(delegations, validator)
      } yield Ok(views.html.component.blockchain.validatorDelegations(delegationsMap = delegationsMap, selfDelegatedPercentage = selfDelegatedPercentage, othersDelegatedPercentage = othersDelegatedPercentage))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def validatorTransactions(address: String): Action[AnyContent] = withoutLoginActionAsync { implicit loginState =>
    implicit request =>
      val operatorAddress = if (utilities.Validator.isHexAddress(address)) blockchainValidators.Service.tryGetOperatorAddress(address) else Future(address)
      (for {
        operatorAddress <- operatorAddress
      } yield Ok(views.html.component.blockchain.validatorTransactions(operatorAddress))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def validatorTransactionsPerPage(address: String, page: Int): Action[AnyContent] = withoutLoginActionAsync { implicit loginState =>
    implicit request =>
      val transactions = blockchainTransactions.Service.getTransactionsPerPageByAddress(address, page)

      (for {
        transactions <- transactions
      } yield Ok(views.html.component.blockchain.validatorTransactionsPerPage(transactions))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def identitiesDefinition(): Action[AnyContent] = withLoginActionAsync { implicit loginState =>
    implicit request =>
      val identityIDs = blockchainIdentities.Service.getAllIDsByProvisioned(loginState.address)

      def getIdentitiesDefined(identityIDs: Seq[String]) = masterClassifications.Service.getIdentityDefinitionsByIdentityIDs(identityIDs)

      (for {
        identityIDs <- identityIDs
        classifications <- getIdentitiesDefined(identityIDs)
      } yield Ok(views.html.component.master.identitiesDefinition(classifications))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def identitiesProvisioned(): Action[AnyContent] = withLoginActionAsync { implicit loginState =>
    implicit request =>
      val identityIDs = blockchainIdentities.Service.getAllIDsByProvisioned(loginState.address)

      def getIdentitiesIssued(identityIDs: Seq[String]) = masterIdentities.Service.getAllByIDs(identityIDs)

      (for {
        identityIDs <- identityIDs
        identities <- getIdentitiesIssued(identityIDs)
      } yield Ok(views.html.component.master.identitiesProvisioned(identities))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def identitiesUnprovisioned(): Action[AnyContent] = withLoginActionAsync { implicit loginState =>
    implicit request =>
      val identityIDs = blockchainIdentities.Service.getAllIDsByUnprovisioned(loginState.address)

      def getIdentitiesIssued(identityIDs: Seq[String]) = masterIdentities.Service.getAllByIDs(identityIDs)

      (for {
        identityIDs <- identityIDs
        identities <- getIdentitiesIssued(identityIDs)
      } yield Ok(views.html.component.master.identitiesUnprovisioned(identities))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def assetsDefinition(): Action[AnyContent] = withLoginActionAsync { implicit loginState =>
    implicit request =>
      val identityIDs = blockchainIdentities.Service.getAllIDsByProvisioned(loginState.address)

      def getAssetsDefined(identityIDs: Seq[String]) = masterClassifications.Service.getAssetDefinitionsByIdentityIDs(identityIDs)

      (for {
        identityIDs <- identityIDs
        classifications <- getAssetsDefined(identityIDs)
      } yield Ok(views.html.component.master.assetsDefinition(classifications))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def assetsMinted(): Action[AnyContent] = withLoginActionAsync { implicit loginState =>
    implicit request =>
      val identityIDs = blockchainIdentities.Service.getAllIDsByProvisioned(loginState.address)

      def getAssetSplits(identityIDs: Seq[String]) = masterSplits.Service.getAllAssetsByOwnerIDs(identityIDs)

      def getAssets(assetIDs: Seq[String]) = masterAssets.Service.getAllByIDs(assetIDs)

      (for {
        identityIDs <- identityIDs
        splits <- getAssetSplits(identityIDs)
        assets <- getAssets(splits.map(_.ownableID))
      } yield Ok(views.html.component.master.assetsMinted(assets, splits))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def ordersDefinition(): Action[AnyContent] = withLoginActionAsync { implicit loginState =>
    implicit request =>
      val identityIDs = blockchainIdentities.Service.getAllIDsByProvisioned(loginState.address)

      def getOrdersDefined(identityIDs: Seq[String]) = masterClassifications.Service.getOrderDefinitionsByIdentityIDs(identityIDs)

      (for {
        identityIDs <- identityIDs
        classifications <- getOrdersDefined(identityIDs)
      } yield Ok(views.html.component.master.ordersDefinition(classifications))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def ordersMade(): Action[AnyContent] = withLoginActionAsync { implicit loginState =>
    implicit request =>
      val identityIDs = blockchainIdentities.Service.getAllIDsByProvisioned(loginState.address)

      def getOrdersMade(identityIDs: Seq[String]) = masterOrders.Service.getAllByMakerIDs(identityIDs)

      (for {
        identityIDs <- identityIDs
        orders <- getOrdersMade(identityIDs)
      } yield Ok(views.html.component.master.ordersMade(orders))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def ordersTake(): Action[AnyContent] = withLoginActionAsync { implicit loginState =>
    implicit request =>
      Future(Ok(views.html.component.master.ordersTake()))
  }

  def ordersTakePublic(): Action[AnyContent] = withLoginActionAsync { implicit loginState =>
    implicit request =>
      val publicTakeOrderIDs = blockchainOrders.Service.getAllPublicOrderIDs

      def getPublicOrders(publicOrderIDs: Seq[String]) = masterOrders.Service.getAllByIDs(publicOrderIDs)

      (for {
        publicOrderIDs <- publicTakeOrderIDs
        publicOrders <- getPublicOrders(publicOrderIDs)
      } yield Ok(views.html.component.master.ordersTakePublic(publicOrders = publicOrders))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def ordersTakePrivate(): Action[AnyContent] = withLoginActionAsync { implicit loginState =>
    implicit request =>
      val identityIDs = blockchainIdentities.Service.getAllIDsByProvisioned(loginState.address)

      def getPrivateOrderIDs(identityIDs: Seq[String]) = blockchainOrders.Service.getAllPrivateOrderIDs(identityIDs)

      def getPrivateOrders(privateOrderIDs: Seq[String]) = masterOrders.Service.getAllByIDs(privateOrderIDs)

      (for {
        identityIDs <- identityIDs
        privateOrderIDs <- getPrivateOrderIDs(identityIDs)
        privateOrders <- getPrivateOrders(privateOrderIDs)
      } yield Ok(views.html.component.master.ordersTakePrivate(privateOrders = privateOrders))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def accountSplits(): Action[AnyContent] = withLoginActionAsync { implicit loginState =>
    implicit request =>
      val identityIDs = blockchainIdentities.Service.getAllIDsByProvisioned(loginState.address)
      val allDenoms = blockchainTokens.Service.getAllDenoms

      def getBlockchainSplits(identityIDs: Seq[String]) = blockchainSplits.Service.getByOwnerIDs(identityIDs)

      def getAssets(splitIDs: Seq[String]) = masterAssets.Service.getAllByIDs(splitIDs)

      (for {
        identityIDs <- identityIDs
        splits <- getBlockchainSplits(identityIDs)
        allDenoms <- allDenoms
        masterAssets <- getAssets(splits.filterNot(x => allDenoms.contains(x.ownableID)).map(_.ownableID))
      } yield Ok(views.html.component.master.accountSplits(splits, allDenoms, masterAssets))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def provisionedAddresses(identityID: String): Action[AnyContent] = withoutLoginActionAsync { implicit loginState =>
    implicit request =>
      val provisionedAddresses = blockchainIdentities.Service.getAllProvisionAddresses(identityID)
      (for (
        provisionedAddresses <- provisionedAddresses
      ) yield Ok(views.html.component.blockchain.provisionedAddresses(identityID, provisionedAddresses))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def unprovisionedAddresses(identityID: String): Action[AnyContent] = withoutLoginActionAsync { implicit loginState =>
    implicit request =>
      val unprovisionedAddresses = blockchainIdentities.Service.getAllUnprovisionAddresses(identityID)
      (for (
        unprovisionedAddresses <- unprovisionedAddresses
      ) yield Ok(views.html.component.blockchain.unprovisionedAddresses(identityID, unprovisionedAddresses))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def getTransaction(transactionType: String): Action[AnyContent] = withLoginActionAsync { implicit loginState =>
    implicit request =>

      transactionType match {
        case constants.Blockchain.TransactionRequest.IDENTITY_NUB => {
          val nubTransactions = blockchainTransactionsIdentityNubs.Service.getTransactionList(loginState.address)
          for {
            nubTransactions <- nubTransactions
          } yield Ok(views.html.component.blockchain.identityNubTransactions(nubTransactions))
        }
        case constants.Blockchain.TransactionRequest.IDENTITY_DEFINE => {
          val identityDefinitionTxs = blockchainTransactionsIdentityDefines.Service.getTransactionList(loginState.address)
          for {
            identityDefinitionTxs <- identityDefinitionTxs
          } yield Ok(views.html.component.blockchain.identityDefineTransactions(identityDefinitionTxs))
        }
        case constants.Blockchain.TransactionRequest.IDENTITY_ISSUE => {
          val issueTransaction = blockchainTransactionsIdentityIssues.Service.getTransactionList(loginState.address)
          for {
            issueTransaction <- issueTransaction
          } yield Ok(views.html.component.blockchain.identityIssueTransactions(issueTransaction))
        }
        case constants.Blockchain.TransactionRequest.IDENTITY_PROVISION => {
          val provisionTransaction = blockchainTransactionsIdentityProvisions.Service.getTransactionList(loginState.address)
          for {
            provisionTransaction <- provisionTransaction
          } yield Ok(views.html.component.blockchain.identityProvisionTransactions(provisionTransaction))
        }
        case constants.Blockchain.TransactionRequest.IDENTITY_UNPROVISION => {
          val unprovisionTransaction = blockchainTransactionsIdentityUnprovisions.Service.getTransactionList(loginState.address)
          for {
            unprovisionTransaction <- unprovisionTransaction
          } yield Ok(views.html.component.blockchain.identityUnprovisionTransactions(unprovisionTransaction))
        }
        case constants.Blockchain.TransactionRequest.ASSET_DEFINE => {
          val assetDefineTransactions = blockchainTransactionsAssetDefines.Service.getTransactionList(loginState.address)
          for {
            assetDefineTransactions <- assetDefineTransactions
          } yield Ok(views.html.component.blockchain.assetDefineTransactions(assetDefineTransactions))
        }
        case constants.Blockchain.TransactionRequest.ASSET_MINT => {
          val assetMintTransactions = blockchainTransactionsAssetMints.Service.getTransactionList(loginState.address)
          for {
            assetMintTransactions <- assetMintTransactions
          } yield Ok(views.html.component.blockchain.assetMintTransactions(assetMintTransactions))
        }
        case constants.Blockchain.TransactionRequest.ASSET_MUTATE => {
          val assetMutateTransactions = blockchainTransactionsAssetMutates.Service.getTransactionList(loginState.address)
          for {
            assetMutateTransactions <- assetMutateTransactions
          } yield Ok(views.html.component.blockchain.assetMutateTransactions(assetMutateTransactions))
        }
        case constants.Blockchain.TransactionRequest.ASSET_BURN => {
          val assetBurnTransactions = blockchainTransactionsAssetBurns.Service.getTransactionList(loginState.address)
          for {
            assetBurnTransactions <- assetBurnTransactions
          } yield Ok(views.html.component.blockchain.assetBurnTransactions(assetBurnTransactions))
        }
        case constants.Blockchain.TransactionRequest.ORDER_DEFINE => {
          val orderDefineTransactions = blockchainTransactionsOrderDefines.Service.getTransactionList(loginState.address)
          for {
            orderDefineTransactions <- orderDefineTransactions
          } yield Ok(views.html.component.blockchain.orderDefineTransactions(orderDefineTransactions))
        }
        case constants.Blockchain.TransactionRequest.ORDER_MAKE => {
          val orderMakeTransactions = blockchainTransactionsOrderMakes.Service.getTransactionList(loginState.address)
          for {
            orderMakeTransactions <- orderMakeTransactions
          } yield Ok(views.html.component.blockchain.orderMakeTransactions(orderMakeTransactions))
        }
        case constants.Blockchain.TransactionRequest.ORDER_TAKE => {
          val orderTakeTransactions = blockchainTransactionsOrderTakes.Service.getTransactionList(loginState.address)
          for {
            orderTakeTransactions <- orderTakeTransactions
          } yield Ok(views.html.component.blockchain.orderTakeTransactions(orderTakeTransactions))
        }
        case constants.Blockchain.TransactionRequest.ORDER_CANCEL => {
          val orderCancelsTransactions = blockchainTransactionsOrderCancels.Service.getTransactionList(loginState.address)
          for {
            orderCancelsTransactions <- orderCancelsTransactions
          } yield Ok(views.html.component.blockchain.orderCancelTransaction(orderCancelsTransactions))
        }
        case _ => Future(throw new BaseException(constants.Response.TRANSACTION_NOT_FOUND))
      }
  }

  def moduleTransactions(currentModule: String): Action[AnyContent] = withLoginActionAsync { implicit loginState =>
    implicit request =>
      currentModule match {
        case constants.View.IDENTITY => Future(Ok(views.html.component.blockchain.identityTransactions()))
        case constants.View.ASSET => Future(Ok(views.html.component.blockchain.assetTransactions()))
        case constants.View.ORDER => Future(Ok(views.html.component.blockchain.orderTransactions()))
        case _ => Future(throw new BaseException(constants.Response.TRANSACTION_NOT_FOUND))
      }
  }

}