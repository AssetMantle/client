package controllers

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.blockchain.ACLAccounts
import models.{blockchain, blockchainTransaction, master, masterTransaction}
import play.api.i18n.I18nSupport
import play.api.libs.json.{JsError, JsPath, JsSuccess, JsValue, Json, OWrites, Reads}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}
import queries.GetAccount
import queries.responses.MemberCheckCorporateScanResponse.{ScanEntity, ScanInputParam, ScanResult}
import transactions.responses.MemberCheckCorporateScanResponse._
import queries.responses.MemberCheckCorporateScanResponse._
import transactions.Abstract.BaseResponse
import transactions.responses.TransactionResponse._
import utilities.JSON.convertJsonStringToObject

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random
import transactions.AddKey
import queries.responses.AccountResponse.Response._
import queries.responses.AccountResponse.Value
import queries.responses.TraderReputationResponse

@Singleton
class LoopBackController @Inject()(
                                    messagesControllerComponents: MessagesControllerComponents,
                                    transactionsAddKey: transactions.AddKey,
                                    transactionsIssueAsset: transactions.IssueAsset,
                                    transactionsIssueFiat: transactions.IssueFiat,
                                    transactionsChangeBuyerBid: transactions.ChangeBuyerBid,
                                    transactionsChangeSellerBid: transactions.ChangeSellerBid,
                                    transactionsConfirmBuyerBid: transactions.ConfirmBuyerBid,
                                    transactionsConfirmSellerBid: transactions.ConfirmSellerBid,
                                    transactionsReleaseAsset: transactions.ReleaseAsset,
                                    transactionsSendAsset: transactions.SendAsset,
                                    transactionsSendFiat: transactions.SendFiat,
                                    transactionsBuyerExecuteOrder: transactions.BuyerExecuteOrder,
                                    transactionsSellerExecuteOrder: transactions.SellerExecuteOrder,
                                    transactionsRedeemAsset: transactions.RedeemAsset,
                                    transactionsSendCoin: transactions.SendCoin,
                                    blockchainAccounts: blockchain.Accounts,
                                    blockchainACLAccounts: blockchain.ACLAccounts,
                                    blockchainACLHashes: blockchain.ACLHashes,
                                    blockchainAssets: blockchain.Assets,
                                    blockchainFiats: blockchain.Fiats,
                                    blockchainTransactionIssueFiats: blockchainTransaction.IssueFiats,
                                    blockchainTransactionIssueAssets: blockchainTransaction.IssueAssets,
                                    masterOrganizations: master.Organizations,
                                    masterTraders: master.Traders,
                                  )(implicit configuration: Configuration, executionContext: ExecutionContext) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_VIEW

  private val mnemonicSampleElements = Seq("crush", "spin", "banana", "cushion", "danger", "lunar", "earn", "unique", "problem", "crack", "coral", "mirror", "battle", "wreck", "abandon", "clarify", "push", "evil", "embody", "insane", "gravity", "gain", "table", "kangaroo", "slim", "regular", "index", "buddy", "dad", "recycle", "suspect", "pair", "cram", "fold", "seven", "host", "palm", "lawsuit", "rocket", "region", "habit", "produce", "blossom", "mosquito", "daring", "twin", "isolate", "surround", "drip", "health", "stem", "sure", "coast", "breeze", "smart", "husband", "soup", "memory", "drill", "giggle", "ritual", "mechanic", "march", "potato", "until", "short", "animal", "only", "prison", "token", "illness", "subway", "pudding", "balance", "useless", "aspect", "view", "vital", "bamboo", "have", "release", "recipe", "subject", "envelope", "avoid", "duck", "host", "category", "mystery", "chapter", "card", "model", "diet", "mail", "unaware", "mistake")
  private val kafkaEnabled = configuration.get[Boolean]("blockchain.kafka.enabled")
  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")
  private val denom = configuration.get[String]("blockchain.denom")

  case class Asset(pegHash: String, documentHash: String, assetType: String, assetQuantity: String, assetPrice: String, quantityUnit: String, var ownerAddress: String, var locked: Boolean, moderated: Boolean, takerAddress: Option[String])

  case class Fiat(pegHash: String, var ownerAddress: String, transactionID: String, var transactionAmount: String, redeemedAmount: String)

  case class Negotiation(id: String, buyerAddress: String, sellerAddress: String, assetPegHash: String, var bid: String, time: String, var buyerSignature: Option[String] = None, var sellerSignature: Option[String] = None, buyerBlockHeight: Option[String] = None, sellerBlockHeight: Option[String] = None, var buyerContractHash: Option[String] = None, var sellerContractHash: Option[String] = None)

  case class Order(id: String, var fiatProofHash: Option[String], var awbProofHash: Option[String])

  private var assetList = Seq[Asset]()
  private var fiatList = Seq[Fiat]()
  private var negotiationList = Seq[Negotiation]()
  private var orderList = Seq[Order]()


  val transactionModeBasedResponse = transactionMode match {
    case constants.Transactions.BLOCK_MODE => Ok(Json.toJson(BlockResponse(height = Random.nextInt(99999).toString, txhash = Random.alphanumeric.filter(c => c.isDigit || c.isUpper).take(64).mkString, gas_wanted = "999999", gas_used = "888888", code = None)))
    case constants.Transactions.SYNC_MODE => Ok(Json.toJson(SyncResponse(height = Random.nextInt(99999).toString, txhash = Random.alphanumeric.filter(c => c.isDigit || c.isUpper).take(64).mkString)))
    case constants.Transactions.ASYNC_MODE => Ok(Json.toJson(AsyncResponse(height = Random.nextInt(99999).toString, txhash = Random.alphanumeric.filter(c => c.isDigit || c.isUpper).take(64).mkString)))
  }


  def memberCheckCorporateScan = Action {
    Ok(Json.toJson(transactions.responses.MemberCheckCorporateScanResponse.Response(Random.alphanumeric.filter(_.isDigit).take(4).mkString.toInt, Random.alphanumeric.take(10).mkString, Random.alphanumeric.filter(_.isDigit).take(4).mkString.toInt, None)).toString())
  }

  def memberCheckCorporateScanInfo(request: String) = Action {
    val scanParam = ScanInputParam(Random.alphanumeric.take(10).mkString, Random.alphanumeric.take(10).mkString, Random.alphanumeric.take(10).mkString, Random.alphanumeric.take(10).mkString, Random.alphanumeric.take(10).mkString, Random.alphanumeric.take(10).mkString, true)
    val scanResult = ScanResult(request.toInt, Random.alphanumeric.take(10).mkString, Random.alphanumeric.filter(_.isDigit).take(4).mkString.toInt, None)
    Ok(Json.toJson(queries.responses.MemberCheckCorporateScanResponse.Response(scanParam, scanResult)).toString())
  }

  def sendEmail = Action {
    Ok
  }

  def sendSMS = Action {
    Ok
  }

  def mnemonic = Action {
    Ok(Random.shuffle(mnemonicSampleElements).take(24).mkString(" "))
  }

  def addKey = Action { implicit request =>

    implicit val requestReads = transactionsAddKey.requestReads
    implicit val responseWrites = transactionsAddKey.responseWrites

    val addKey = request.body.asJson.map { requestBody =>
      convertJsonStringToObject[transactionsAddKey.Request](requestBody.toString())
    }.getOrElse(throw new BaseException(constants.Response.FAILURE))
    Ok(Json.toJson(transactionsAddKey.Response(addKey.name, "commit1" + Random.alphanumeric.filter(c => c.isDigit || c.isLower).take(38).mkString, "commitpub1addwnpepq" + Random.alphanumeric.filter(c => c.isDigit || c.isLower).take(58).mkString, addKey.seed)))
  }

  def sendCoin = Action { implicit request =>
    if (kafkaEnabled) {
      val response = Json.toJson(KafkaResponse(ticketID = "SECO" + Random.alphanumeric.filter(_.isDigit).take(18).mkString))
      Ok(response)
    } else {
      transactionModeBasedResponse
    }
  }

  def addZone = Action {
    if (kafkaEnabled) {
      Ok(Json.toJson(KafkaResponse(ticketID = "DEZO" + Random.alphanumeric.filter(_.isDigit).take(18).mkString)))
    } else {
      transactionModeBasedResponse
    }
  }

  def getZone(zoneID: String) = Action {
    Ok("\"commit1" + Random.alphanumeric.filter(c => c.isDigit || c.isLower).take(38).mkString + "\"")
  }

  def getResponse(ticketID: String) = Action {
    transactionModeBasedResponse
  }

  def getTxHashResponse(txHash: String) = Action {
    Ok(Json.toJson(BlockResponse(height = Random.nextInt(99999).toString, txhash = txHash, gas_wanted = "999999", gas_used = "888888", code = None)))
  }

  def addOrganization = Action {
    if (kafkaEnabled) {
      Ok(Json.toJson(KafkaResponse(ticketID = "DEOR" + Random.alphanumeric.filter(_.isDigit).take(18).mkString)))
    } else {
      transactionModeBasedResponse
    }
  }

  def getOrganization(organizationID: String) = Action.async {
    val organization = masterOrganizations.Service.tryGet(organizationID)
    for {
      organization <- organization
    } yield Ok(Json.toJson(queries.responses.OrganizationResponse.Response("commit1" + Random.alphanumeric.filter(c => c.isDigit || c.isLower).take(38).mkString, organization.zoneID)))
  }

  def setACL = Action {
    if (kafkaEnabled) {
      Ok(Json.toJson(KafkaResponse(ticketID = "DEAC" + Random.alphanumeric.filter(_.isDigit).take(18).mkString)))
    } else {
      transactionModeBasedResponse
    }
  }

  def issueAsset = Action { implicit request =>

    implicit val requestReads = transactionsIssueAsset.requestReads
    // implicit val responseReads = transactionsAddKey.responseWrites

    val issueAsset = request.body.asJson.map { requestBody =>
      convertJsonStringToObject[transactionsIssueAsset.Request](requestBody.toString())
    }.getOrElse(throw new BaseException(constants.Response.FAILURE))

    assetList = assetList :+ Asset(Random.alphanumeric.filter(_.isDigit).take(5).mkString, issueAsset.documentHash, issueAsset.assetType, issueAsset.assetQuantity, issueAsset.assetPrice, issueAsset.quantityUnit, issueAsset.to, if (issueAsset.moderated) true else false, issueAsset.moderated, if (issueAsset.takerAddress == "") None else Some(issueAsset.takerAddress))
    assetList.map { asset => println("Assset---------------------------------" + asset) }
    println(assetList.toString())
    println("assetListlength" + assetList.length)
    if (kafkaEnabled) {
      Ok(Json.toJson(KafkaResponse(ticketID = "ISAS" + Random.alphanumeric.filter(_.isDigit).take(18).mkString)))
    } else {
      transactionModeBasedResponse
    }
  }

  def issueFiat = Action { implicit request =>
    implicit val requestReads = transactionsIssueFiat.requestReads

    val issueFiat = request.body.asJson.map { requestBody =>
      convertJsonStringToObject[transactionsIssueFiat.Request](requestBody.toString())
    }.getOrElse(throw new BaseException(constants.Response.FAILURE))

    fiatList = fiatList :+ Fiat(Random.alphanumeric.filter(_.isDigit).take(5).mkString, issueFiat.to, issueFiat.transactionID, issueFiat.transactionAmount, "")
    println(fiatList.toString())

    if (kafkaEnabled) {
      Ok(Json.toJson(KafkaResponse(ticketID = "ISFI" + Random.alphanumeric.filter(_.isDigit).take(18).mkString)))
    } else {
      transactionModeBasedResponse
    }
  }

  def getAccount(address: String) = Action {

    val assetPegWallet = assetList.filter(_.ownerAddress == address).map(asset => queries.responses.AccountResponse.Asset(asset.pegHash, asset.documentHash, asset.assetType, asset.assetQuantity, asset.assetPrice, asset.quantityUnit, asset.ownerAddress, asset.locked, asset.moderated, asset.takerAddress.getOrElse("")))
    val fiatPegWallet = fiatList.filter(_.ownerAddress == address).map(fiat => queries.responses.AccountResponse.Fiat(fiat.pegHash, fiat.transactionID, fiat.transactionAmount, fiat.redeemedAmount, None))
    val response = queries.responses.AccountResponse.Response(value = Value(address = address, coins = Some(Seq(queries.responses.AccountResponse.Coins(denom, "2000"))), asset_peg_wallet = Some(assetPegWallet), fiat_peg_wallet = Some(fiatPegWallet), account_number = Random.alphanumeric.filter(_.isDigit).take(3).mkString, sequence = Random.alphanumeric.filter(_.isDigit).take(3).mkString))
    Ok(Json.toJson(response))
  }

  def changeBuyerBid = Action { implicit request =>
    implicit val requestReads = transactionsChangeBuyerBid.requestReads

    val changeBuyerBid = request.body.asJson.map { requestBody =>
      convertJsonStringToObject[transactionsChangeBuyerBid.Request](requestBody.toString())
    }.getOrElse(throw new BaseException(constants.Response.FAILURE))

    val negotiation = negotiationList.find(negotiation => negotiation.buyerAddress == changeBuyerBid.base_req.from && negotiation.sellerAddress == changeBuyerBid.to && negotiation.assetPegHash == changeBuyerBid.pegHash)
    negotiation match {
      case Some(negotiation) =>
        println("1-----------------------------Negotiation Found")
        negotiationList.find(negotiation => negotiation.buyerAddress == changeBuyerBid.base_req.from && negotiation.sellerAddress == changeBuyerBid.to && negotiation.assetPegHash == changeBuyerBid.pegHash).map { negotiation =>
          negotiation.bid = changeBuyerBid.bid
        }
        println(negotiationList)
      case None =>
        println("1-----------------------------No Negotiation Found")
        negotiationList = negotiationList :+ Negotiation(Random.alphanumeric.filter(c => c.isDigit || c.isUpper).take(84).mkString, changeBuyerBid.base_req.from, changeBuyerBid.to, changeBuyerBid.pegHash, changeBuyerBid.bid, changeBuyerBid.time, None, None, None, None, None, None)
        println("negotiationList.length---------" + negotiationList.length)
        println(negotiationList.toString)
    }
    if (kafkaEnabled) {
      Ok(Json.toJson(KafkaResponse(ticketID = "ISFI" + Random.alphanumeric.filter(_.isDigit).take(18).mkString)))
    } else {
      transactionModeBasedResponse
    }
  }

  def changeSellerBid = Action { implicit request =>
    implicit val requestReads = transactionsChangeSellerBid.requestReads

    val changeSellerBid = request.body.asJson.map { requestBody =>
      convertJsonStringToObject[transactionsChangeSellerBid.Request](requestBody.toString())
    }.getOrElse(throw new BaseException(constants.Response.FAILURE))

    val negotiation = negotiationList.find(negotiation => negotiation.buyerAddress == changeSellerBid.to && negotiation.sellerAddress == changeSellerBid.base_req.from && negotiation.assetPegHash == changeSellerBid.pegHash)
    negotiation match {
      case Some(negotiation) => negotiationList.find(negotiation => negotiation.buyerAddress == changeSellerBid.base_req.from && negotiation.sellerAddress == changeSellerBid.to && negotiation.assetPegHash == changeSellerBid.pegHash).map { negotiation =>
        negotiation.bid = changeSellerBid.bid
      }
      case None => negotiationList :+ Negotiation(Random.alphanumeric.filter(c => c.isDigit || c.isUpper).take(84).mkString, changeSellerBid.base_req.from, changeSellerBid.to, changeSellerBid.pegHash, changeSellerBid.bid, changeSellerBid.time, None, None, None, None, None, None)
    }

    if (kafkaEnabled) {
      Ok(Json.toJson(KafkaResponse(ticketID = "ISFI" + Random.alphanumeric.filter(_.isDigit).take(18).mkString)))
    } else {
      transactionModeBasedResponse
    }
  }

  def confirmBuyerBid = Action { implicit request =>
    implicit val requestReads = transactionsConfirmBuyerBid.requestReads

    val confirmBuyerBid = request.body.asJson.map { requestBody =>
      convertJsonStringToObject[transactionsConfirmBuyerBid.Request](requestBody.toString())
    }.getOrElse(throw new BaseException(constants.Response.FAILURE))

    negotiationList.find(negotiation => negotiation.buyerAddress == confirmBuyerBid.base_req.from && negotiation.sellerAddress == confirmBuyerBid.to && negotiation.assetPegHash == confirmBuyerBid.pegHash).map { negotiation =>
      negotiation.bid = confirmBuyerBid.bid
      negotiation.buyerContractHash = Some(confirmBuyerBid.buyerContractHash)
    }
    if (kafkaEnabled) {
      Ok(Json.toJson(KafkaResponse(ticketID = "ISFI" + Random.alphanumeric.filter(_.isDigit).take(18).mkString)))
    } else {
      transactionModeBasedResponse
    }
  }

  def confirmSellerBid = Action { implicit request =>
    implicit val requestReads = transactionsConfirmSellerBid.requestReads

    val confirmSellerBid = request.body.asJson.map { requestBody =>
      convertJsonStringToObject[transactionsConfirmSellerBid.Request](requestBody.toString())
    }.getOrElse(throw new BaseException(constants.Response.FAILURE))

    negotiationList.find(negotiation => negotiation.buyerAddress == confirmSellerBid.to && negotiation.sellerAddress == confirmSellerBid.base_req.from && negotiation.assetPegHash == confirmSellerBid.pegHash).map { negotiation =>

      negotiation.bid = confirmSellerBid.bid
      negotiation.sellerContractHash = Some(confirmSellerBid.sellerContractHash)
    }.getOrElse(throw new BaseException(constants.Response.NEGOTIATION_NOT_FOUND))

    if (kafkaEnabled) {
      Ok(Json.toJson(KafkaResponse(ticketID = "ISFI" + Random.alphanumeric.filter(_.isDigit).take(18).mkString)))
    } else {
      transactionModeBasedResponse
    }
  }

  def releaseAsset = Action { implicit request =>
    implicit val requestReads = transactionsReleaseAsset.requestReads

    val releaseAsset = request.body.asJson.map { requestBody =>
      convertJsonStringToObject[transactionsReleaseAsset.Request](requestBody.toString())
    }.getOrElse(throw new BaseException(constants.Response.FAILURE))

    assetList.find(asset => asset.ownerAddress == releaseAsset.to && asset.pegHash == releaseAsset.pegHash).map { asset =>
      asset.locked = false
    }.getOrElse(throw new BaseException(constants.Response.ASSET_NOT_FOUND))

    if (kafkaEnabled) {
      Ok(Json.toJson(KafkaResponse(ticketID = "ISFI" + Random.alphanumeric.filter(_.isDigit).take(18).mkString)))
    } else {
      transactionModeBasedResponse
    }
  }

  def sendAsset = Action { implicit request =>
    implicit val requestReads = transactionsSendAsset.requestReads

    val sendAsset = request.body.asJson.map { requestBody =>
      convertJsonStringToObject[transactionsSendAsset.Request](requestBody.toString())
    }.getOrElse(throw new BaseException(constants.Response.FAILURE))

    val negotiation = negotiationList.find(negotiation => negotiation.buyerAddress == sendAsset.to && negotiation.sellerAddress == sendAsset.base_req.from && negotiation.assetPegHash == sendAsset.pegHash).getOrElse(throw new BaseException(constants.Response.NEGOTIATION_NOT_FOUND))
    orderList.find(_.id == negotiation.id) match {
      case Some(order) =>
      case None => orderList :+ Order(negotiation.id, None, None)
    }
    assetList.find(_.pegHash == negotiation.assetPegHash).map { asset =>
      asset.ownerAddress = negotiation.id
    }.getOrElse(throw new BaseException(constants.Response.ASSET_NOT_FOUND))

    if (kafkaEnabled) {
      Ok(Json.toJson(KafkaResponse(ticketID = "ISFI" + Random.alphanumeric.filter(_.isDigit).take(18).mkString)))
    } else {
      transactionModeBasedResponse
    }
  }

  def sendFiat = Action { implicit request =>
    implicit val requestReads = transactionsSendFiat.requestReads

    val sendFiat = request.body.asJson.map { requestBody =>
      convertJsonStringToObject[transactionsSendFiat.Request](requestBody.toString())
    }.getOrElse(throw new BaseException(constants.Response.FAILURE))

    fiatList.find(fiat => fiat.ownerAddress == sendFiat.base_req.from && fiat.transactionAmount == sendFiat.amount).map { fiat =>
      fiatList

      fiat.transactionAmount = (fiat.transactionAmount.toInt - sendFiat.amount.toInt).toString
      fiatList = fiatList :+ Fiat(Random.alphanumeric.filter(_.isDigit).take(5).mkString, sendFiat.to, "", sendFiat.amount, "")
    }.getOrElse(throw new BaseException(constants.Response.FIAT_PEG_NOT_FOUND))

    val negotiation = negotiationList.find(negotiation => negotiation.buyerAddress == sendFiat.base_req.from && negotiation.sellerAddress == sendFiat.to && negotiation.assetPegHash == sendFiat.pegHash).getOrElse(throw new BaseException(constants.Response.NEGOTIATION_NOT_FOUND))

    fiatList.find(fiat => fiat.ownerAddress == sendFiat.base_req.from).map { fiat =>
      fiat.transactionAmount = (fiat.transactionAmount.toInt - sendFiat.amount.toInt).toString
      fiatList = fiatList :+ Fiat(fiat.pegHash, negotiation.id, "", sendFiat.amount, "")
    }

    orderList.find(_.id == negotiation.id) match {
      case Some(order) =>
      case None => orderList :+ Order(negotiation.id, None, None)
    }

    if (kafkaEnabled) {
      Ok(Json.toJson(KafkaResponse(ticketID = "ISFI" + Random.alphanumeric.filter(_.isDigit).take(18).mkString)))
    } else {
      transactionModeBasedResponse
    }
  }

  def buyerExecuteOrder = Action { implicit request =>
    implicit val requestReads = transactionsBuyerExecuteOrder.requestReads

    val buyerExecuteOrder = request.body.asJson.map { requestBody =>
      convertJsonStringToObject[transactionsBuyerExecuteOrder.Request](requestBody.toString())
    }.getOrElse(throw new BaseException(constants.Response.FAILURE))

    negotiationList.find(negotiation => negotiation.buyerAddress == buyerExecuteOrder.buyerAddress && negotiation.sellerAddress == buyerExecuteOrder.sellerAddress && negotiation.assetPegHash == buyerExecuteOrder.pegHash).map { negotiation =>
      orderList.find(_.id == negotiation.id) match {
        case Some(order) => order.fiatProofHash = Some(buyerExecuteOrder.fiatProofHash)
          if (order.fiatProofHash.isDefined && order.awbProofHash.isDefined) {
            assetList.find(_.pegHash == buyerExecuteOrder.pegHash).map { asset =>
              asset.ownerAddress == buyerExecuteOrder.buyerAddress
            }.getOrElse(throw new BaseException(constants.Response.ASSET_NOT_FOUND))
          }
        case None => throw new BaseException(constants.Response.FAILURE)
      }
    }.getOrElse(throw new BaseException(constants.Response.NEGOTIATION_NOT_FOUND))

    if (kafkaEnabled) {
      Ok(Json.toJson(KafkaResponse(ticketID = "ISFI" + Random.alphanumeric.filter(_.isDigit).take(18).mkString)))
    } else {
      transactionModeBasedResponse
    }
  }

  def sellerExecuteOrder = Action { implicit request =>
    implicit val requestReads = transactionsSellerExecuteOrder.requestReads

    val sellerExecuteOrder = request.body.asJson.map { requestBody =>
      convertJsonStringToObject[transactionsSellerExecuteOrder.Request](requestBody.toString())
    }.getOrElse(throw new BaseException(constants.Response.FAILURE))

    negotiationList.find(negotiation => negotiation.buyerAddress == sellerExecuteOrder.buyerAddress && negotiation.sellerAddress == sellerExecuteOrder.sellerAddress && negotiation.assetPegHash == sellerExecuteOrder.pegHash).map { negotiation =>
      orderList.find(_.id == negotiation.id) match {
        case Some(order) => order.awbProofHash = Some(sellerExecuteOrder.awbProofHash)
          if (order.fiatProofHash.isDefined && order.awbProofHash.isDefined) {
            assetList.find(_.pegHash == sellerExecuteOrder.pegHash).map { asset =>
              asset.ownerAddress == sellerExecuteOrder.buyerAddress
            }.getOrElse(throw new BaseException(constants.Response.ASSET_NOT_FOUND))

            fiatList.find(_.ownerAddress == order.id).map { fiat =>
              fiat.ownerAddress = sellerExecuteOrder.sellerAddress
            }.getOrElse(throw new BaseException(constants.Response.FAILURE))
          }
        case None => throw new BaseException(constants.Response.FAILURE)
      }
    }.getOrElse(throw new BaseException(constants.Response.NEGOTIATION_NOT_FOUND))

    if (kafkaEnabled) {
      Ok(Json.toJson(KafkaResponse(ticketID = "ISFI" + Random.alphanumeric.filter(_.isDigit).take(18).mkString)))
    } else {
      transactionModeBasedResponse
    }
  }

  def redeemAsset = Action { implicit request =>
    implicit val requestReads = transactionsRedeemAsset.requestReads

    val redeemAsset = request.body.asJson.map { requestBody =>
      convertJsonStringToObject[transactionsRedeemAsset.Request](requestBody.toString())
    }.getOrElse(throw new BaseException(constants.Response.FAILURE))

    assetList.filter(_.pegHash == redeemAsset.pegHash)

    if (kafkaEnabled) {
      Ok(Json.toJson(KafkaResponse(ticketID = "ISFI" + Random.alphanumeric.filter(_.isDigit).take(18).mkString)))
    } else {
      transactionModeBasedResponse
    }
  }

  def getNegotiation(negotiationID: String) = Action {

    val response = negotiationList.filter(_.id == negotiationID).map { negotiation =>
      queries.responses.NegotiationResponse.Response(queries.responses.NegotiationResponse.Value(negotiationID, negotiation.buyerAddress, negotiation.sellerAddress, negotiation.assetPegHash, negotiation.bid, negotiation.time, Some(negotiation.buyerSignature.getOrElse("")), Some(negotiation.sellerSignature.getOrElse("")), Some(negotiation.buyerBlockHeight.getOrElse("")), Some(negotiation.sellerBlockHeight.getOrElse("")), Some(negotiation.buyerContractHash.getOrElse("")), Some(negotiation.sellerContractHash.getOrElse(""))))
    }.headOption.getOrElse(throw new BaseException(constants.Response.NEGOTIATION_NOT_FOUND))

    Ok(Json.toJson(response))
  }

  def getNegotiationID(buyerAddress: String, sellerAddress: String, pegHash: String) = Action {
    negotiationList.map { negotiation => println(negotiation) }
    val negotiationID = negotiationList.filter(negotiation => negotiation.buyerAddress == buyerAddress && negotiation.sellerAddress == sellerAddress && negotiation.assetPegHash == pegHash).map {
      _.id
    }.headOption.getOrElse(throw new BaseException(constants.Response.FAILURE))

    Ok(Json.toJson(queries.responses.NegotiationIdResponse.Response(negotiationID)))
  }

  def getOrder(orderID: String) = Action {
    val assetPegWallet = assetList.filter(_.ownerAddress == orderID).map(asset => queries.responses.AccountResponse.Asset(asset.pegHash, asset.documentHash, asset.assetType, asset.assetQuantity, asset.assetPrice, asset.quantityUnit, asset.ownerAddress, false, asset.moderated, asset.takerAddress.getOrElse("")))
    val fiatPegWallet = fiatList.filter(_.ownerAddress == orderID).map(fiat => queries.responses.AccountResponse.Fiat(fiat.pegHash, fiat.transactionID, fiat.transactionAmount, fiat.redeemedAmount, None))

    val response = orderList.filter(_.id == orderID).map { order =>
      queries.responses.OrderResponse.Response(queries.responses.OrderResponse.Value(orderID, order.fiatProofHash.getOrElse(""), order.awbProofHash.getOrElse(""), Some(fiatPegWallet), Some(assetPegWallet)))
    }
    Ok(Json.toJson(response))
  }

  def getACL(address: String) = Action.async {

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

  def getTraderReputation(address: String) = Action {
    Ok(Json.toJson(queries.responses.TraderReputationResponse.Response(queries.responses.TraderReputationResponse.Value(address, TraderReputationResponse.TransactionFeedbackResponse("0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0"), None))))
  }

}
