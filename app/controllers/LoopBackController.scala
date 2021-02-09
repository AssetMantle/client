package controllers

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.masterTransaction.{EmailOTP, SMSOTP}
import models.{blockchain, blockchainTransaction, master, masterTransaction}
import play.api.i18n.I18nSupport
import play.api.libs.json.{JsError, JsPath, JsSuccess, JsValue, Json, OWrites, Reads}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents, Result}
import play.api.{Configuration, Logger}
import queries.responses.memberCheck.CorporateScanResponse.{ScanEntity, ScanInputParam, ScanResult}
import transactions.responses.MemberCheckCorporateScanResponse._
import queries.responses.memberCheck.CorporateScanResponse._
import transactions.responses.TransactionResponse._
import utilities.JSON.convertJsonStringToObject

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random
import queries.responses.AccountResponse.Value
import queries.responses.TraderReputationResponse
import transactions.blockchain.{BuyerExecuteOrder, ChangeBuyerBid, ChangeSellerBid, ConfirmBuyerBid, ConfirmSellerBid, IssueAsset, IssueFiat, RedeemAsset, RedeemFiat, ReleaseAsset, SellerExecuteOrder, SendAsset, SendFiat}
import utilities.MicroNumber

@Singleton
class LoopBackController @Inject()(
                                    messagesControllerComponents: MessagesControllerComponents,
                                    transactionsAddKey: transactions.blockchain.AddKey,
                                    transactionsIssueAsset: IssueAsset,
                                    transactionsIssueFiat: IssueFiat,
                                    transactionsChangeBuyerBid: ChangeBuyerBid,
                                    transactionsChangeSellerBid: ChangeSellerBid,
                                    transactionsConfirmBuyerBid: ConfirmBuyerBid,
                                    transactionsConfirmSellerBid: ConfirmSellerBid,
                                    transactionsReleaseAsset: ReleaseAsset,
                                    transactionsSendAsset: SendAsset,
                                    transactionsSendFiat: SendFiat,
                                    transactionsBuyerExecuteOrder: BuyerExecuteOrder,
                                    transactionsSellerExecuteOrder: SellerExecuteOrder,
                                    transactionsRedeemAsset: RedeemAsset,
                                    transactionsRedeemFiat: RedeemFiat,
                                    transactionsChangePassword: transactions.blockchain.ChangePassword,
                                    transactionsForgotPassword: transactions.blockchain.ForgotPassword,
                                    blockchainAccounts: blockchain.Accounts,
                                    blockchainACLAccounts: blockchain.ACLAccounts,
                                    blockchainACLHashes: blockchain.ACLHashes,
                                    masterOrganizations: master.Organizations,
                                    masterTraders: master.Traders,
                                    masterEmails: master.Emails,
                                    masterMobiles: master.Mobiles,
                                    masterTransactionEmailOTPs: masterTransaction.EmailOTPs,
                                    masterTransactionSMSOTPs: masterTransaction.SMSOTPs,
                                  )(implicit configuration: Configuration, executionContext: ExecutionContext) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_VIEW

  private val mnemonicSampleElements = Seq("crush", "spin", "banana", "cushion", "danger", "lunar", "earn", "unique", "problem", "crack", "coral", "mirror", "battle", "wreck", "abandon", "clarify", "push", "evil", "embody", "insane", "gravity", "gain", "table", "kangaroo", "slim", "regular", "index", "buddy", "dad", "recycle", "suspect", "pair", "cram", "fold", "seven", "host", "palm", "lawsuit", "rocket", "region", "habit", "produce", "blossom", "mosquito", "daring", "twin", "isolate", "surround", "drip", "health", "stem", "sure", "coast", "breeze", "smart", "husband", "soup", "memory", "drill", "giggle", "ritual", "mechanic", "march", "potato", "until", "short", "animal", "only", "prison", "token", "illness", "subway", "pudding", "balance", "useless", "aspect", "view", "vital", "bamboo", "have", "release", "recipe", "subject", "envelope", "avoid", "duck", "host", "category", "mystery", "chapter", "card", "model", "diet", "mail", "unaware", "mistake")
  private val kafkaEnabled = configuration.get[Boolean]("blockchain.kafka.enabled")
  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")
  private val denom = configuration.get[String]("blockchain.denom")

  case class Asset(pegHash: String, documentHash: String, assetType: String, assetQuantity: MicroNumber, assetPrice: MicroNumber, quantityUnit: String, var ownerAddress: String, var locked: Boolean, moderated: Boolean, takerAddress: Option[String])

  case class Fiat(pegHash: String, var ownerAddress: String, transactionID: String, var transactionAmount: MicroNumber, var redeemedAmount: MicroNumber)

  case class Negotiation(id: String, buyerAddress: String, sellerAddress: String, assetPegHash: String, var bid: MicroNumber, time: String, var buyerSignature: Option[String] = None, var sellerSignature: Option[String] = None, buyerBlockHeight: Option[String] = None, sellerBlockHeight: Option[String] = None, var buyerContractHash: Option[String] = None, var sellerContractHash: Option[String] = None)

  case class Order(id: String, var fiatProofHash: Option[String], var awbProofHash: Option[String])

  private var assetList = Seq[Asset]()
  private var fiatList = Seq[Fiat]()
  private var negotiationList = Seq[Negotiation]()
  private var orderList = Seq[Order]()

  def transactionModeBasedResponse: Result = transactionMode match {
    case constants.Transactions.BLOCK_MODE => Ok(Json.toJson(BlockResponse(height = Random.nextInt(9999999).toString, txhash = Random.alphanumeric.filter(c => c.isDigit || c.isUpper).take(64).mkString, gas_wanted = constants.FormField.GAS.maximumValue.toMicroString, gas_used = constants.FormField.GAS.maximumValue.toMicroString, code = None)))
    case constants.Transactions.SYNC_MODE => Ok(Json.toJson(SyncResponse(height = Random.nextInt(9999999).toString, txhash = Random.alphanumeric.filter(c => c.isDigit || c.isUpper).take(64).mkString)))
    case constants.Transactions.ASYNC_MODE => Ok(Json.toJson(AsyncResponse(height = Random.nextInt(9999999).toString, txhash = Random.alphanumeric.filter(c => c.isDigit || c.isUpper).take(64).mkString)))
  }

  def memberCheckCorporateScan: Action[AnyContent] = Action {
    Ok(Json.toJson(transactions.responses.MemberCheckCorporateScanResponse.Response(Random.alphanumeric.filter(_.isDigit).take(9).mkString.toInt, Random.alphanumeric.take(10).mkString, Random.alphanumeric.filter(_.isDigit).take(6).mkString.toInt, None)).toString())
  }

  def memberCheckCorporateScanInfo(request: String): Action[AnyContent] = Action {
    val scanParam = ScanInputParam(Random.alphanumeric.take(10).mkString, Random.alphanumeric.take(10).mkString, Random.alphanumeric.take(10).mkString, Random.alphanumeric.take(10).mkString, Random.alphanumeric.take(10).mkString, Random.alphanumeric.take(10).mkString, true)
    val scanResult = ScanResult(request.toInt, Random.alphanumeric.take(10).mkString, Random.alphanumeric.filter(_.isDigit).take(4).mkString.toInt, None)
    Ok(Json.toJson(queries.responses.memberCheck.CorporateScanResponse.Response(scanParam, scanResult)).toString())
  }

  def sendEmail(emailAddress: String): Action[AnyContent] = Action.async {
    val emailAddressAccount = masterEmails.Service.getEmailAddressAccount(emailAddress)

    def updateOTP(accountID: Option[String]): Future[Int] = {
      accountID match {
        case Some(accountID) => masterTransactionEmailOTPs.Service.insertOrUpdate(EmailOTP(accountID, util.hashing.MurmurHash3.stringHash(constants.Test.OTP).toString))
        case None => Future(0)
      }
    }

    for {
      emailAddressAccount <- emailAddressAccount
      _ <- updateOTP(emailAddressAccount)
    } yield Ok
  }

  def sendSMS(mobileNumber: String): Action[AnyContent] = Action.async {
    val username = masterMobiles.Service.getUsernameByMobileNumber(mobileNumber)

    def updateOTP(accountID: Option[String]): Future[Int] = {
      accountID match {
        case Some(accountID) => masterTransactionSMSOTPs.Service.insertOrUpdate(SMSOTP(accountID, util.hashing.MurmurHash3.stringHash(constants.Test.OTP).toString))
        case None => Future(0)
      }
    }

    for {
      username <- username
      _ <- updateOTP(username)
    } yield Ok
  }

  def mnemonic: Action[AnyContent] = Action {
    Ok(Random.shuffle(mnemonicSampleElements).take(24).mkString(" "))
  }
/*
  def addKey: Action[AnyContent] = Action { implicit request =>
    implicit val requestReads = transactionsAddKey.requestReads
    implicit val responseWrites = transactionsAddKey.responseWrites

    val addKeyRequest = request.body.asJson.map { requestBody =>
      convertJsonStringToObject[transactionsAddKey.Request](requestBody.toString())
    }.getOrElse(throw new BaseException(constants.Response.FAILURE))
    Ok(Json.toJson(transactionsAddKey.Response(addKeyRequest.name, constants.Test.BLOCKCHAIN_ADDRESS_PREFIX + Random.alphanumeric.filter(c => c.isDigit || c.isLower).take(38).mkString, "commitpub1addwnpepq" + Random.alphanumeric.filter(c => c.isDigit || c.isLower).take(58).mkString, addKeyRequest.seed)))
  }*/

  def sendCoin(to: String): Action[AnyContent] = Action { implicit request =>
    if (kafkaEnabled) {
      val response = Json.toJson(KafkaResponse(ticketID = constants.Test.TEST_TICKET_ID_PREFIX.SEND_COIN + Random.alphanumeric.filter(_.isDigit).take(18).mkString))
      Ok(response)
    } else {
      transactionModeBasedResponse
    }
  }

  def addZone: Action[AnyContent] = Action {
    if (kafkaEnabled) {
      Ok(Json.toJson(KafkaResponse(ticketID = constants.Test.TEST_TICKET_ID_PREFIX.ADD_ZONE + Random.alphanumeric.filter(_.isDigit).take(18).mkString)))
    } else {
      transactionModeBasedResponse
    }
  }

  def zone(zoneID: String): Action[AnyContent] = Action {
    Ok("\"" + constants.Test.BLOCKCHAIN_ADDRESS_PREFIX + Random.alphanumeric.filter(c => c.isDigit || c.isLower).take(38).mkString + "\"")
  }

  def response(ticketID: String): Action[AnyContent] = Action {
    transactionModeBasedResponse
  }

  def txHashResponse(txHash: String): Action[AnyContent] = Action {
    Ok(Json.toJson(BlockResponse(height = Random.nextInt(99999).toString, txhash = txHash, gas_wanted = constants.FormField.GAS.maximumValue.toMicroString, gas_used = constants.FormField.GAS.maximumValue.toMicroString, code = None)))
  }

  def addOrganization: Action[AnyContent] = Action {
    if (kafkaEnabled) {
      Ok(Json.toJson(KafkaResponse(ticketID = constants.Test.TEST_TICKET_ID_PREFIX.ADD_ORGANIZATION + Random.alphanumeric.filter(_.isDigit).take(18).mkString)))
    } else {
      transactionModeBasedResponse
    }
  }

  def organization(organizationID: String): Action[AnyContent] = Action.async {
    val organization = masterOrganizations.Service.tryGet(organizationID)
    (for {
      organization <- organization
    } yield Ok(Json.toJson(queries.responses.OrganizationResponse.Response(constants.Test.BLOCKCHAIN_ADDRESS_PREFIX + Random.alphanumeric.filter(c => c.isDigit || c.isLower).take(38).mkString, organization.zoneID)))
      ).recover {
      case baseException: BaseException => InternalServerError
    }
  }

  def setACL: Action[AnyContent] = Action {
    if (kafkaEnabled) {
      Ok(Json.toJson(KafkaResponse(ticketID = constants.Test.TEST_TICKET_ID_PREFIX.SET_ACL + Random.alphanumeric.filter(_.isDigit).take(18).mkString)))
    } else {
      transactionModeBasedResponse
    }
  }

  def issueAssetUpdate(asset: Asset) = synchronized {
    assetList = assetList :+ asset
  }

  def issueAsset: Action[AnyContent] = Action { implicit request =>
    try {
      implicit val requestReads = transactionsIssueAsset.requestReads
      val issueAssetRequest = request.body.asJson.map { requestBody => convertJsonStringToObject[transactionsIssueAsset.Request](requestBody.toString()) }.getOrElse(throw new BaseException(constants.Response.FAILURE))
      issueAssetUpdate(Asset(Random.alphanumeric.filter(_.isDigit).take(10).mkString, issueAssetRequest.documentHash, issueAssetRequest.assetType, issueAssetRequest.assetQuantity, issueAssetRequest.assetPrice, issueAssetRequest.quantityUnit, issueAssetRequest.to, if (issueAssetRequest.moderated) true else false, issueAssetRequest.moderated, if (issueAssetRequest.takerAddress == "") None else Some(issueAssetRequest.takerAddress)))

      if (kafkaEnabled) {
        Ok(Json.toJson(KafkaResponse(ticketID = constants.Test.TEST_TICKET_ID_PREFIX.ISSUE_ASSET + Random.alphanumeric.filter(_.isDigit).take(18).mkString)))
      } else {
        transactionModeBasedResponse
      }
    } catch {
      case baseException: BaseException => InternalServerError(Json.toJson(ErrorResponse(baseException.failure.message)))
    }
  }

  def issueFiatUpdate(fiat: Fiat) = synchronized {
    fiatList = fiatList :+ fiat
  }

  def issueFiat: Action[AnyContent] = Action { implicit request =>
    try {
      implicit val requestReads = transactionsIssueFiat.requestReads
      val issueFiatRequest = request.body.asJson.map { requestBody => convertJsonStringToObject[transactionsIssueFiat.Request](requestBody.toString()) }.getOrElse(throw new BaseException(constants.Response.FAILURE))
      issueFiatUpdate(Fiat(Random.alphanumeric.filter(_.isDigit).take(10).mkString, issueFiatRequest.to, issueFiatRequest.transactionID, issueFiatRequest.transactionAmount, new MicroNumber(0)))

      if (kafkaEnabled) {
        Ok(Json.toJson(KafkaResponse(ticketID = constants.Test.TEST_TICKET_ID_PREFIX.ISSUE_FIAT + Random.alphanumeric.filter(_.isDigit).take(18).mkString)))
      } else {
        transactionModeBasedResponse
      }
    } catch {
      case baseException: BaseException => InternalServerError(Json.toJson(ErrorResponse(baseException.failure.message)))
    }
  }

  def account(address: String): Action[AnyContent] = Action {
    val assetPegWallet = assetList.filter(_.ownerAddress == address).map(asset => queries.responses.AccountResponse.Asset(asset.pegHash, asset.documentHash, asset.assetType, asset.assetQuantity, asset.assetPrice, asset.quantityUnit, asset.ownerAddress, asset.locked, asset.moderated, asset.takerAddress.getOrElse("")))
    val fiatPegWallet = fiatList.filter(_.ownerAddress == address).map(fiat => queries.responses.AccountResponse.Fiat(fiat.pegHash, fiat.transactionID, fiat.transactionAmount, fiat.redeemedAmount, None))
    Ok(Json.toJson(queries.responses.AccountResponse.Response(value = Value(address = address, coins = Some(Seq(queries.responses.AccountResponse.Coin(denom, "2000"))), asset_peg_wallet = Some(assetPegWallet), fiat_peg_wallet = Some(fiatPegWallet), account_number = Random.alphanumeric.filter(_.isDigit).take(3).mkString, sequence = Random.alphanumeric.filter(_.isDigit).take(3).mkString))))
  }

  def changeBuyerBidUpdate(changeBuyerBidRequest: transactionsChangeBuyerBid.Request) = synchronized {
    negotiationList.find(negotiation => negotiation.buyerAddress == changeBuyerBidRequest.base_req.from && negotiation.sellerAddress == changeBuyerBidRequest.to && negotiation.assetPegHash == changeBuyerBidRequest.pegHash) match {
      case Some(negotiation) => negotiationList.find(negotiation => negotiation.buyerAddress == changeBuyerBidRequest.base_req.from && negotiation.sellerAddress == changeBuyerBidRequest.to && negotiation.assetPegHash == changeBuyerBidRequest.pegHash).map { negotiation =>
        negotiation.bid = changeBuyerBidRequest.bid
      }
      case None => negotiationList = negotiationList :+ Negotiation(Random.alphanumeric.filter(c => c.isDigit || c.isUpper).take(84).mkString, changeBuyerBidRequest.base_req.from, changeBuyerBidRequest.to, changeBuyerBidRequest.pegHash, changeBuyerBidRequest.bid, changeBuyerBidRequest.time, None, None, None, None, None, None)
    }
  }

  def changeBuyerBid: Action[AnyContent] = Action { implicit request =>
    try {
      implicit val requestReads = transactionsChangeBuyerBid.requestReads
      val changeBuyerBidRequest = request.body.asJson.map { requestBody => convertJsonStringToObject[transactionsChangeBuyerBid.Request](requestBody.toString()) }.getOrElse(throw new BaseException(constants.Response.FAILURE))
      changeBuyerBidUpdate(changeBuyerBidRequest)
      if (kafkaEnabled) {
        Ok(Json.toJson(KafkaResponse(ticketID = constants.Test.TEST_TICKET_ID_PREFIX.CHANGE_BUYER_BID + Random.alphanumeric.filter(_.isDigit).take(18).mkString)))
      } else {
        transactionModeBasedResponse
      }
    } catch {
      case baseException: BaseException => InternalServerError(Json.toJson(ErrorResponse(baseException.failure.message)))
    }
  }

  def changeSellerBidUpdate(changeSellerBidRequest: transactionsChangeSellerBid.Request) = synchronized {
    negotiationList.find(negotiation => negotiation.buyerAddress == changeSellerBidRequest.to && negotiation.sellerAddress == changeSellerBidRequest.base_req.from && negotiation.assetPegHash == changeSellerBidRequest.pegHash) match {
      case Some(negotiation) => negotiationList.find(negotiation => negotiation.buyerAddress == changeSellerBidRequest.base_req.from && negotiation.sellerAddress == changeSellerBidRequest.to && negotiation.assetPegHash == changeSellerBidRequest.pegHash).map { negotiation =>
        negotiation.bid = changeSellerBidRequest.bid
      }
      case None => negotiationList :+ Negotiation(Random.alphanumeric.filter(c => c.isDigit || c.isUpper).take(84).mkString, changeSellerBidRequest.base_req.from, changeSellerBidRequest.to, changeSellerBidRequest.pegHash, changeSellerBidRequest.bid, changeSellerBidRequest.time, None, None, None, None, None, None)
    }
  }

  def changeSellerBid: Action[AnyContent] = Action { implicit request =>
    try {
      implicit val requestReads = transactionsChangeSellerBid.requestReads
      val changeSellerBidRequest = request.body.asJson.map { requestBody => convertJsonStringToObject[transactionsChangeSellerBid.Request](requestBody.toString()) }.getOrElse(throw new BaseException(constants.Response.FAILURE))
      changeSellerBidUpdate(changeSellerBidRequest)
      if (kafkaEnabled) {
        Ok(Json.toJson(KafkaResponse(ticketID = constants.Test.TEST_TICKET_ID_PREFIX.CHANGE_SELLER_BID + Random.alphanumeric.filter(_.isDigit).take(18).mkString)))
      } else {
        transactionModeBasedResponse
      }
    } catch {
      case baseException: BaseException => InternalServerError(Json.toJson(ErrorResponse(baseException.failure.message)))
    }
  }

  def confirmBuyerBidUpdate(confirmBuyerBidRequest: transactionsConfirmBuyerBid.Request) = synchronized {
    negotiationList.find(negotiation => negotiation.buyerAddress == confirmBuyerBidRequest.base_req.from && negotiation.sellerAddress == confirmBuyerBidRequest.to && negotiation.assetPegHash == confirmBuyerBidRequest.pegHash).map { negotiation =>
      negotiation.bid = confirmBuyerBidRequest.bid
      negotiation.buyerContractHash = Some(confirmBuyerBidRequest.buyerContractHash)
    }.getOrElse(throw new BaseException(constants.Response.FAILURE))
  }

  def confirmBuyerBid: Action[AnyContent] = Action { implicit request =>
    try {
      implicit val requestReads = transactionsConfirmBuyerBid.requestReads
      val confirmBuyerBidRequest = request.body.asJson.map { requestBody => convertJsonStringToObject[transactionsConfirmBuyerBid.Request](requestBody.toString()) }.getOrElse(throw new BaseException(constants.Response.FAILURE))
      confirmBuyerBidUpdate(confirmBuyerBidRequest)
      if (kafkaEnabled) {
        Ok(Json.toJson(KafkaResponse(ticketID = constants.Test.TEST_TICKET_ID_PREFIX.CONFIRM_BUYER_BID + Random.alphanumeric.filter(_.isDigit).take(18).mkString)))
      } else {
        transactionModeBasedResponse
      }
    } catch {
      case baseException: BaseException => InternalServerError(Json.toJson(ErrorResponse(baseException.failure.message)))
    }
  }

  def confirmSellerBidUpdate(confirmSellerBidRequest: transactionsConfirmSellerBid.Request) = synchronized {
    negotiationList.find(negotiation => negotiation.buyerAddress == confirmSellerBidRequest.to && negotiation.sellerAddress == confirmSellerBidRequest.base_req.from && negotiation.assetPegHash == confirmSellerBidRequest.pegHash).map { negotiation =>
      negotiation.bid = confirmSellerBidRequest.bid
      negotiation.sellerContractHash = Some(confirmSellerBidRequest.sellerContractHash)
    }.getOrElse(throw new BaseException(constants.Response.NEGOTIATION_NOT_FOUND))
  }

  def confirmSellerBid: Action[AnyContent] = Action { implicit request =>
    try {
      implicit val requestReads = transactionsConfirmSellerBid.requestReads
      val confirmSellerBidRequest = request.body.asJson.map { requestBody => convertJsonStringToObject[transactionsConfirmSellerBid.Request](requestBody.toString()) }.getOrElse(throw new BaseException(constants.Response.FAILURE))
      confirmSellerBidUpdate(confirmSellerBidRequest)
      if (kafkaEnabled) {
        Ok(Json.toJson(KafkaResponse(ticketID = constants.Test.TEST_TICKET_ID_PREFIX.CONFIRM_SELLER_BID + Random.alphanumeric.filter(_.isDigit).take(18).mkString)))
      } else {
        transactionModeBasedResponse
      }
    } catch {
      case baseException: BaseException => InternalServerError(Json.toJson(ErrorResponse(baseException.failure.message)))
    }
  }

  def releaseAssetUpdate(releaseAssetRequest: transactionsReleaseAsset.Request) = synchronized {
    assetList.find(asset => asset.ownerAddress == releaseAssetRequest.to && asset.pegHash == releaseAssetRequest.pegHash).map { asset =>
      asset.locked = false
    }.getOrElse(throw new BaseException(constants.Response.ASSET_NOT_FOUND))
  }

  def releaseAsset: Action[AnyContent] = Action { implicit request =>
    try {
      implicit val requestReads = transactionsReleaseAsset.requestReads
      val releaseAssetRequest = request.body.asJson.map { requestBody => convertJsonStringToObject[transactionsReleaseAsset.Request](requestBody.toString()) }.getOrElse(throw new BaseException(constants.Response.FAILURE))
      releaseAssetUpdate(releaseAssetRequest)
      if (kafkaEnabled) {
        Ok(Json.toJson(KafkaResponse(ticketID = constants.Test.TEST_TICKET_ID_PREFIX.RELEASE_ASSET + Random.alphanumeric.filter(_.isDigit).take(18).mkString)))
      } else {
        transactionModeBasedResponse
      }
    } catch {
      case baseException: BaseException => InternalServerError(Json.toJson(ErrorResponse(baseException.failure.message)))
    }
  }

  def sendAssetUpdate(sendAssetRequest: transactionsSendAsset.Request) = synchronized {
    val negotiation = negotiationList.find(negotiation => negotiation.buyerAddress == sendAssetRequest.to && negotiation.sellerAddress == sendAssetRequest.base_req.from && negotiation.assetPegHash == sendAssetRequest.pegHash).getOrElse(throw new BaseException(constants.Response.NEGOTIATION_NOT_FOUND))
    if (!orderList.exists(_.id == negotiation.id)) orderList = orderList :+ Order(negotiation.id, None, None)
    assetList.find(_.pegHash == negotiation.assetPegHash).map { asset =>asset.ownerAddress = negotiation.id}.getOrElse(throw new BaseException(constants.Response.ASSET_NOT_FOUND))
  }

  def sendAsset: Action[AnyContent] = Action { implicit request =>
    try {
      implicit val requestReads = transactionsSendAsset.requestReads
      val sendAssetRequest = request.body.asJson.map { requestBody => convertJsonStringToObject[transactionsSendAsset.Request](requestBody.toString()) }.getOrElse(throw new BaseException(constants.Response.FAILURE))
      sendAssetUpdate(sendAssetRequest)
      if (kafkaEnabled) {
        Ok(Json.toJson(KafkaResponse(ticketID = constants.Test.TEST_TICKET_ID_PREFIX.SEND_ASSET + Random.alphanumeric.filter(_.isDigit).take(18).mkString)))
      } else {
        transactionModeBasedResponse
      }
    } catch {
      case baseException: BaseException => InternalServerError(Json.toJson(ErrorResponse(baseException.failure.message)))
    }
  }

  def sendFiatUpdate(sendFiatRequest: transactionsSendFiat.Request) = synchronized {
    val negotiation = negotiationList.find(negotiation => negotiation.buyerAddress == sendFiatRequest.base_req.from && negotiation.sellerAddress == sendFiatRequest.to && negotiation.assetPegHash == sendFiatRequest.pegHash).getOrElse(throw new BaseException(constants.Response.NEGOTIATION_NOT_FOUND))

    fiatList.find(fiat => fiat.ownerAddress == sendFiatRequest.base_req.from).map { fiat =>
      fiat.transactionAmount = (fiat.transactionAmount.toInt - sendFiatRequest.amount.toInt).toString
      fiatList = fiatList :+ Fiat(fiat.pegHash, negotiation.id, Random.alphanumeric.take(10).mkString, sendFiatRequest.amount, new MicroNumber(0))
    }.getOrElse(throw new BaseException(constants.Response.FIAT_PEG_NOT_FOUND))

    if (!orderList.exists(_.id == negotiation.id)) orderList = orderList :+ Order(negotiation.id, None, None)
  }


  def sendFiat: Action[AnyContent] = Action { implicit request =>
    try {
      implicit val requestReads = transactionsSendFiat.requestReads
      val sendFiatRequest = request.body.asJson.map { requestBody => convertJsonStringToObject[transactionsSendFiat.Request](requestBody.toString()) }.getOrElse(throw new BaseException(constants.Response.FAILURE))
      sendFiatUpdate(sendFiatRequest)

      if (kafkaEnabled) {
        Ok(Json.toJson(KafkaResponse(ticketID = constants.Test.TEST_TICKET_ID_PREFIX.SEND_FIAT + Random.alphanumeric.filter(_.isDigit).take(18).mkString)))
      } else {
        transactionModeBasedResponse
      }
    } catch {
      case baseException: BaseException => InternalServerError(Json.toJson(ErrorResponse(baseException.failure.message)))
    }
  }

  def buyerExecuteOrderUpdate(buyerExecuteOrderRequest: transactionsBuyerExecuteOrder.Request) = synchronized {
    negotiationList.find(negotiation => negotiation.buyerAddress == buyerExecuteOrderRequest.buyerAddress && negotiation.sellerAddress == buyerExecuteOrderRequest.sellerAddress && negotiation.assetPegHash == buyerExecuteOrderRequest.pegHash).map { negotiation =>
      orderList.find(_.id == negotiation.id) match {
        case Some(order) => order.fiatProofHash = Some(buyerExecuteOrderRequest.fiatProofHash)
          if (order.fiatProofHash.isDefined && order.awbProofHash.isDefined) {
            assetList.find(_.pegHash == buyerExecuteOrderRequest.pegHash).map { asset => asset.ownerAddress == buyerExecuteOrderRequest.buyerAddress }.getOrElse(throw new BaseException(constants.Response.ASSET_NOT_FOUND))
          }
        case None => throw new BaseException(constants.Response.FAILURE)
      }
    }.getOrElse(throw new BaseException(constants.Response.NEGOTIATION_NOT_FOUND))
  }

  def buyerExecuteOrder: Action[AnyContent] = Action { implicit request =>
    try {
      implicit val requestReads = transactionsBuyerExecuteOrder.requestReads
      val buyerExecuteOrderRequest = request.body.asJson.map { requestBody => convertJsonStringToObject[transactionsBuyerExecuteOrder.Request](requestBody.toString()) }.getOrElse(throw new BaseException(constants.Response.FAILURE))
      buyerExecuteOrderUpdate(buyerExecuteOrderRequest)

      if (kafkaEnabled) {
        Ok(Json.toJson(KafkaResponse(ticketID = constants.Test.TEST_TICKET_ID_PREFIX.BUYER_EXECUTE_ORDER + Random.alphanumeric.filter(_.isDigit).take(18).mkString)))
      } else {
        transactionModeBasedResponse
      }
    } catch {
      case baseException: BaseException => InternalServerError(Json.toJson(ErrorResponse(baseException.failure.message)))
    }
  }

  def sellerExecuteOrderUpdate(sellerExecuteOrderRequest: transactionsSellerExecuteOrder.Request) = synchronized {
    negotiationList.find(negotiation => negotiation.buyerAddress == sellerExecuteOrderRequest.buyerAddress && negotiation.sellerAddress == sellerExecuteOrderRequest.sellerAddress && negotiation.assetPegHash == sellerExecuteOrderRequest.pegHash).map { negotiation =>
      orderList.find(_.id == negotiation.id) match {
        case Some(order) => order.awbProofHash = Some(sellerExecuteOrderRequest.awbProofHash)
          if (order.fiatProofHash.isDefined && order.awbProofHash.isDefined) {
            assetList.find(_.pegHash == sellerExecuteOrderRequest.pegHash).map { asset =>
              if (asset.moderated) {
                fiatList.find(_.ownerAddress == order.id).map { fiat =>fiat.ownerAddress = sellerExecuteOrderRequest.sellerAddress}.getOrElse(throw new BaseException(constants.Response.FAILURE))
              }
              asset.ownerAddress == sellerExecuteOrderRequest.buyerAddress
            }.getOrElse(throw new BaseException(constants.Response.ASSET_NOT_FOUND))
          }
        case None => throw new BaseException(constants.Response.FAILURE)
      }
    }.getOrElse(throw new BaseException(constants.Response.NEGOTIATION_NOT_FOUND))
  }

  def sellerExecuteOrder: Action[AnyContent] = Action { implicit request =>
    try {
      implicit val requestReads = transactionsSellerExecuteOrder.requestReads
      val sellerExecuteOrderRequest = request.body.asJson.map { requestBody => convertJsonStringToObject[transactionsSellerExecuteOrder.Request](requestBody.toString()) }.getOrElse(throw new BaseException(constants.Response.FAILURE))
      sellerExecuteOrderUpdate(sellerExecuteOrderRequest)

      if (kafkaEnabled) {
        Ok(Json.toJson(KafkaResponse(ticketID = constants.Test.TEST_TICKET_ID_PREFIX.SELLER_EXECUTE_ORDER + Random.alphanumeric.filter(_.isDigit).take(18).mkString)))
      } else {
        transactionModeBasedResponse
      }
    } catch {
      case baseException: BaseException => InternalServerError(Json.toJson(ErrorResponse(baseException.failure.message)))
    }
  }

  def redeemAssetUpdate(redeemAssetRequest: transactionsRedeemAsset.Request) = synchronized {
    assetList = assetList.filterNot(_.pegHash == redeemAssetRequest.pegHash)
  }

  def redeemAsset: Action[AnyContent] = Action { implicit request =>
    try {
      implicit val requestReads = transactionsRedeemAsset.requestReads
      val redeemAssetRequest = request.body.asJson.map { requestBody => convertJsonStringToObject[transactionsRedeemAsset.Request](requestBody.toString()) }.getOrElse(throw new BaseException(constants.Response.FAILURE))
      redeemAssetUpdate(redeemAssetRequest)

      if (kafkaEnabled) {
        Ok(Json.toJson(KafkaResponse(ticketID = constants.Test.TEST_TICKET_ID_PREFIX.RELEASE_ASSET + Random.alphanumeric.filter(_.isDigit).take(18).mkString)))
      } else {
        transactionModeBasedResponse
      }
    } catch {
      case baseException: BaseException => InternalServerError(Json.toJson(ErrorResponse(baseException.failure.message)))
    }
  }

  def redeemFiatUpdate(redeemFiatRequest: transactionsRedeemFiat.Request) = synchronized {
    fiatList.find(fiat => fiat.ownerAddress == redeemFiatRequest.base_req.from).map { fiat =>
      fiat.transactionAmount = fiat.transactionAmount - redeemFiatRequest.redeemAmount
      fiat.redeemedAmount = redeemFiatRequest.redeemAmount
    }.getOrElse(throw new BaseException(constants.Response.FIAT_PEG_NOT_FOUND))
  }

  def redeemFiat: Action[AnyContent] = Action { implicit request =>
    try {
      implicit val requestReads = transactionsRedeemFiat.requestReads
      val redeemFiatRequest = request.body.asJson.map { requestBody => convertJsonStringToObject[transactionsRedeemFiat.Request](requestBody.toString()) }.getOrElse(throw new BaseException(constants.Response.FAILURE))
      redeemFiatUpdate(redeemFiatRequest)

      if (kafkaEnabled) {
        Ok(Json.toJson(KafkaResponse(ticketID = constants.Test.TEST_TICKET_ID_PREFIX.REDEEM_FIAT + Random.alphanumeric.filter(_.isDigit).take(18).mkString)))
      } else {
        transactionModeBasedResponse
      }
    } catch {
      case baseException: BaseException => InternalServerError(Json.toJson(ErrorResponse(baseException.failure.message)))
    }
  }

  def negotiation(negotiationID: String): Action[AnyContent] = Action {
    val response = negotiationList.filter(_.id == negotiationID).map { negotiation =>
      queries.responses.NegotiationResponse.Response(queries.responses.NegotiationResponse.Value(negotiationID, negotiation.buyerAddress, negotiation.sellerAddress, negotiation.assetPegHash, negotiation.bid, negotiation.time, Some(negotiation.buyerSignature.getOrElse("")), Some(negotiation.sellerSignature.getOrElse("")), Some(negotiation.buyerBlockHeight.getOrElse("")), Some(negotiation.sellerBlockHeight.getOrElse("")), Some(negotiation.buyerContractHash.getOrElse("")), Some(negotiation.sellerContractHash.getOrElse(""))))
    }.headOption.getOrElse(throw new BaseException(constants.Response.NEGOTIATION_NOT_FOUND))

    Ok(Json.toJson(response))
  }

  def negotiationID(buyerAddress: String, sellerAddress: String, pegHash: String): Action[AnyContent] = Action {
    val negotiationID = negotiationList.filter(negotiation => negotiation.buyerAddress == buyerAddress && negotiation.sellerAddress == sellerAddress && negotiation.assetPegHash == pegHash).map(_.id).headOption.getOrElse(throw new BaseException(constants.Response.FAILURE))
    Ok(Json.toJson(queries.responses.NegotiationIdResponse.Response(negotiationID)))
  }

  def order(orderID: String): Action[AnyContent] = Action {
    val assetPegWallet = assetList.filter(_.ownerAddress == orderID).map(asset => queries.responses.AccountResponse.Asset(asset.pegHash, asset.documentHash, asset.assetType, asset.assetQuantity, asset.assetPrice, asset.quantityUnit, asset.ownerAddress, asset.locked, asset.moderated, asset.takerAddress.getOrElse("")))
    val fiatPegWallet = fiatList.filter(_.ownerAddress == orderID).map(fiat => queries.responses.AccountResponse.Fiat(fiat.pegHash, fiat.transactionID, fiat.transactionAmount, fiat.redeemedAmount, None))

    val response = orderList.filter(_.id == orderID).map { order =>
      queries.responses.OrderResponse.Response(queries.responses.OrderResponse.Value(orderID, order.fiatProofHash.getOrElse(""), order.awbProofHash.getOrElse(""), Some(fiatPegWallet), Some(assetPegWallet)))
    }.headOption.getOrElse(throw new BaseException(constants.Response.FAILURE))

    Ok(Json.toJson(response))
  }

  def acl(address: String): Action[AnyContent] = Action.async {
    val aclAccount = blockchainACLAccounts.Service.tryGet(address)

    def getACL(hash: String) = blockchainACLHashes.Service.tryGetACL(hash)

    (for {
      aclAccount <- aclAccount
      acl <- getACL(aclAccount.aclHash)
    } yield Ok(Json.toJson(queries.responses.ACLResponse.Response(queries.responses.ACLResponse.Value(address, aclAccount.zoneID, aclAccount.organizationID, acl))))
      ).recover {
      case baseException: BaseException => throw baseException
    }
  }

  def traderReputation(address: String): Action[AnyContent] = Action {
    Ok(Json.toJson(queries.responses.TraderReputationResponse.Response(queries.responses.TraderReputationResponse.Value(address, TraderReputationResponse.TransactionFeedbackResponse("0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0"), None))))
  }

  def updatePassword(username: String): Action[AnyContent] = Action {
    implicit val responseWrites = transactionsChangePassword.responseWrites

    Ok(Json.toJson(transactionsChangePassword.Response(false, constants.Response.PASSWORD_UPDATED.message)))
  }

  def forgotPassword(username: String): Action[AnyContent] = Action {
    implicit val responseWrites = transactionsForgotPassword.responseWrites
    Ok(Json.toJson(transactionsForgotPassword.Response(false, constants.Response.PASSWORD_UPDATED.message)))
  }

  def pushNotification: Action[AnyContent] = Action { implicit request =>
    Ok
  }

}
