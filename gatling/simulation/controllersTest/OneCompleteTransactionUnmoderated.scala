package controllersTest

import constants.{Form, Test}
import controllers.routes
import controllersTest.addOrganizationControllerTest.getOrganizationID
import controllersTest.addZoneControllerTest.{getZoneID, getZoneStatus}
import controllersTest.changeBuyerBidControllerTest.getAddressFromAccountID
import controllersTest.issueAssetControllerTest.{allIssueAssetDocumentType, getAssetPriceForIssueAsset, getAssetQuantityForIssueAsset, getAssetTypeForIssueAsset, getDocumentHashForIssueAsset, getQuantityUnitForIssueAsset}
import controllersTest.issueFiatControllerTest.{getTransactionAmountForIssueFiat, getTransactionIDForIssueFiat}
//import controllersTest.changeBuyerBidControllerTest.getBuyerAddress
import controllersTest.issueAssetControllerTest.{getPegHashByOwnerAddress, getRequestIDForIssueAsset}
import controllersTest.issueFiatControllerTest.getRequestIDForIssueFiat
import controllersTest.sendCoinControllerTest.getRequestIDForFaucetRequest
import controllersTest.setACLControllerTest._
import feeders._
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._


class OneCompleteTransactionUnmoderated extends Simulation {

/*  val oneCompleteTransactionUnmoderated=scenario("ONE_COMPLETE_TRANSACTION_UNMODERATED")
    .exec(ZoneSignUp.masterZoneSignUp)
    .exec(ZoneFaucetRequest.masterLoginAndFaucetRequestZone)
    .exec(loginMainApproveFaucetZone.masterLoginMainAndApproveFaucetRequestZone)
    .exec(ZoneLoginAddRequest.masterLoginAddZoneRequest)
    .exec(LoginMainAndApproveZone.masterLoginMainAndApproveZone)
    .exec(OrganizationSignUp.masterOrganizationSignUp)
    .exec(OrganizationFaucetRequest.masterLoginAndFaucetRequestOrganization)
    .exec(LoginMainApproveFaucetOrganization.masterLoginMainAndApproveFaucetRequestOrganization)
    .exec(OrganizationLoginAddRequest.masterOrganizationLoginAddRequest)
    .exec(LoginZoneAndApproveOrganization.masterLoginZoneAndApproveOrganization)
    .exec(SignUpAndLoginSellerAndCreateFaucetRequest.signUpAndLoginSellerAndCreateFaucetRequest)
    .exec(ApproveFaucetRequestSeller.approveFaucetRequestSeller)
    .exec(AddSellerTraderRequest.addSellerTraderRequest)
    .exec(ApproveSellerTraderACL.approveSellerTraderACL)
    .exec(SignUpAndLoginBuyerAndCreateFaucetRequest.signUpAndLoginBuyerAndCreateFaucetRequest)
    .exec(ApproveFaucetRequestBuyer.approveFaucetRequestBuyer)
    .exec(AddBuyerTraderRequest.addBuyerTraderRequest)
    .exec(ApproveBuyerTraderACL.approveBuyerTraderACL)
    .exec(UnmoderatedIssueAssetRequestForSeller.unmoderatedIssueAssetRequestForSeller)
    .exec(IssueFiatRequestBuyer.issueFiatRequest)
    .exec(IssueFiat.issueFiat)
    .exec(ChangeBuyerBid.changeBuyerBid)
    .exec(ChangeSellerBid.changeSellerBid)
    .exec(ConfirmBuyerBid.confirmBuyerBid)
    .exec(ConfirmSellerBid.confirmSellerBid)
    .exec(SendFiat.sendFiat)
    .exec(SendAsset.sendAsset)
    .exec(UnmoderatedBuyerAndSellerExecuteOrder.unmoderatedBuyerAndSellerExecuteOrder)
    .exec(SetBuyerFeedback.setBuyerFeedback)
    .exec(SetSellerFeedback.setSellerFeedback)
    .exec(RedeemAsset.redeemAsset)
    .exec(RedeemFiat.redeemFiat)*/

  val oneCompleteUnmoderated=scenario("ONE_COMPLETE_UNMODERATED")
    .exec(CreateZone.createZone)
    .exec(CreateOrganization.createOrganization)
    .exec(CreateSeller.createSeller)
    .exec(CreateBuyer.createBuyer)
    .exec(IssueAssetUnmoderated.issueAssetUnmoderated)
    .exec(IssueFiat.issueFiat)
    .exec(ChangeBuyerBid.changeBuyerBid)
    .exec(ChangeSellerBid.changeSellerBid)
    .exec(ConfirmBuyerBid.confirmBuyerBid)
    .exec(ConfirmSellerBid.confirmSellerBid)
    .exec(SendFiat.sendFiat)
    .exec(SendAsset.sendAsset)
    .exec(BuyerExecuteOrderUnmoderated.buyerExecuteOrderUnmoderated)
    .exec(SellerExecuteOrderUnmoderated.sellerExecuteOrderUnmoderated)
    .exec(SetBuyerFeedback.setBuyerFeedback)
    .exec(SetSellerFeedback.setSellerFeedback)
    .exec(RedeemAsset.redeemAsset)
    .exec(RedeemFiat.redeemFiat)

  setUp(
    oneCompleteUnmoderated.inject(atOnceUsers(1))
  )
    .maxDuration(1000)
    .protocols(http.baseUrl(Test.BASE_URL))
}

object IssueAssetUnmoderated{

  val issueAssetUnmoderated=scenario("IssueAssetUnmoderated")
    .exec(session=>session.set(Test.TEST_USERNAME,session(Test.TEST_SELLER_USERNAME).as[String]).set(Test.TEST_PASSWORD,session(Test.TEST_SELLER_PASSWORD).as[String]))
    .exec(loginControllerTest.loginScenario)
    .exec(issueAssetControllerTest.unmoderatedIssueAssetRequestScenario)
    .exec(logoutControllerTest.logoutScenario)

}

object SendAssetAndFiatUnmoderated{

  val sendAssetAndFiatUnmoderated=scenario("SendFiatAsset")
    .exec(session=>session.set(Test.TEST_USERNAME,session(Test.TEST_BUYER_USERNAME).as[String]).set(Test.TEST_PASSWORD,session(Test.TEST_BUYER_PASSWORD).as[String]))
    .exec(loginControllerTest.loginScenario)
    .exec(sendFiatControllerTest.sendFiatScenario)
    .exec(logoutControllerTest.logoutScenario)
    .exec(session=>session.set(Test.TEST_USERNAME,session(Test.TEST_SELLER_USERNAME).as[String]).set(Test.TEST_PASSWORD,session(Test.TEST_SELLER_PASSWORD).as[String]))
    .exec(loginControllerTest.loginScenario)
    .exec(sendAssetControllerTest.sendAssetScenario)
    .exec(logoutControllerTest.logoutScenario)
}

object BuyerExecuteOrderUnmoderated{

    val buyerExecuteOrderUnmoderated=scenario("BuyerExecuteOrderUnmoderated")
      .exec(session=> session.set(Test.TEST_NEGOTIATION_REQUEST_ID,confirmBuyerBidControllerTest.getNegotiationRequestIDFromSellerAccountID(session(Test.TEST_SELLER_USERNAME).as[String])))
      .exec(session=> session.set(Test.TEST_NEGOTIATION_ID,confirmBuyerBidControllerTest.getNegotiationIDFromSellerAccountID(session(Test.TEST_SELLER_USERNAME).as[String])))
      .exec(session=>session.set(Test.TEST_USERNAME,session(Test.TEST_BUYER_USERNAME).as[String]).set(Test.TEST_PASSWORD,session(Test.TEST_BUYER_PASSWORD).as[String]))
      .exec(loginControllerTest.loginScenario)
      .exec(buyerExecuteOrderControllerTest.unmoderatedBuyerExecuteOrderScenario)
      .exec(logoutControllerTest.logoutScenario)


}

object SellerExecuteOrderUnmoderated{

  val sellerExecuteOrderUnmoderated=scenario("SellerExecuteOrderUnmoderated")
    .exec(session=>session.set(Test.TEST_USERNAME,session(Test.TEST_SELLER_USERNAME).as[String]).set(Test.TEST_PASSWORD,session(Test.TEST_SELLER_PASSWORD).as[String]))
    .exec(loginControllerTest.loginScenario)
    .exec(sellerExecuteOrderControllerTest.unmoderatedSellerExecuteOrder)
    .exec(logoutControllerTest.logoutScenario)
}

object UnmoderatedIssueAssetRequestForSeller{

  val unmoderatedIssueAssetRequestForSeller=scenario("unmoderatedIssueAssetRequestForSeller")
    .exec{session => session.set(Test.USER_TYPE, sendCoinControllerTest.getUserType(session(Test.TEST_SELLER_USERNAME).as[String])) }
    .doIf(session=> session(Test.USER_TYPE).as[String] != "TRADER") {
      asLongAs(session=> session(Test.USER_TYPE).as[String] != "TRADER") {
        pause(1)
          .exec { session =>session.set(Test.USER_TYPE, sendCoinControllerTest.getUserType(session(Test.TEST_SELLER_USERNAME).as[String]))}
      }
    }
    .exec(http("LoginSeller_GET")
      .get(routes.AccountController.loginForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .exec(http("LoginSeller_POST")
      .post(routes.AccountController.login().url)
      .formParamMap(Map(
        Form.USERNAME -> "${%s}".format(Test.TEST_SELLER_USERNAME),
        Form.PASSWORD -> "${%s}".format(Test.TEST_SELLER_PASSWORD),
        Form.PUSH_NOTIFICATION_TOKEN -> "",
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
    .pause(3)
    .feed(AssetTypeFeeder.assetTypeFeed)
    .feed(AssetPriceFeeder.assetPriceFeed)
    .feed(QuantityUnitFeeder.quantityUnitFeed)
    .feed(AssetQuantityFeeder.assetQuantityFeed)
    .exec(http("IssueAssetRequest_GET")
      .get(routes.IssueAssetController.issueAssetDetailForm(None).url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("IssueAssetRequest_POST")
      .post(routes.IssueAssetController.issueAssetDetail().url)
      .formParamMap(Map(
        Form.REQUEST_ID -> "",
        constants.FormField.ASSET_TYPE.name -> "${%s}".format(Test.TEST_ASSET_TYPE),
        constants.FormField.QUANTITY_UNIT.name -> "${%s}".format(Test.TEST_QUANTITY_UNIT),
        constants.FormField.ASSET_QUANTITY.name -> "${%s}".format(Test.TEST_ASSET_QUANTITY),
        constants.FormField.ASSET_PRICE.name -> "${%s}".format(Test.TEST_ASSET_PRICE),
        constants.FormField.TAKER_ADDRESS.name -> "",
        constants.FormField.COMMODITY_NAME.name -> "AEFRGEAR",
        constants.FormField.QUALITY.name -> "A",
        constants.FormField.DELIVERY_TERM.name -> "FOB",
        constants.FormField.TRADE_TYPE.name -> "POST TRADE",
        constants.FormField.PORT_OF_LOADING.name -> "mumbai",
        constants.FormField.PORT_OF_DISCHARGE.name -> "shanghai",
        constants.FormField.SHIPMENT_DATE.name -> "2019-11-11",
        constants.FormField.PHYSICAL_DOCUMENTS_HANDLED_VIA.name -> "BANK",
        constants.FormField.COMDEX_PAYMENT_TERMS.name -> "BOTH_PARTIES",
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
    .pause(3)
    .exec{session=> session.set(Test.TEST_REQUEST_ID, getRequestIDForIssueAsset(session(Test.TEST_SELLER_USERNAME).as[String]))}
    .exec(http("Upload OBL Form")
      .get(routes.FileController.uploadTraderAssetForm("OBL","${TEST_REQUEST_ID}").url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN))
    )
    .pause(2)
    .feed(ImageFeeder3.imageFeed3)
    .exec(http("Issue Asset Upload OBL")
      .post(routes.FileController.uploadTraderAsset("OBL").url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        Form.RESUMABLE_CHUNK_NUMBER -> "1",
        Form.RESUMABLE_CHUNK_SIZE  -> "1048576",
        Form.RESUMABLE_TOTAL_SIZE -> "${%s}".format(Test.TEST_FILE_SIZE),
        Form.RESUMABLE_IDENTIFIER -> "document",
        Form.RESUMABLE_FILE_NAME -> "${%s}".format(Test.TEST_FILE_NAME)))
      .bodyPart(RawFileBodyPart("file", "/root/IdeaProjects/commitCentral/gatling/simulation/images/"+"${%s}".format(Test.TEST_FILE_NAME))
        .transferEncoding("binary")).asMultipartForm)
    .exec(
      http("STORE_AssetDocument_OBL")
        .get("/traderUpload/storeAssetDocument?name="+"${%s}".format(Test.TEST_FILE_NAME)+"&documentType="+"OBL&issueAssetRequestID="+"${%s}".format(Test.TEST_REQUEST_ID))
        .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN))
    )
    .pause(2)
    .exec(http("issue Asset OBL Post")
      .post(routes.IssueAssetController.issueAssetOBL().url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        constants.FormField.REQUEST_ID.name -> "${%s}".format(Test.TEST_REQUEST_ID),
        constants.FormField.BILL_OF_LADING_NUMBER.name -> "fghfg",
        constants.FormField.PORT_OF_LOADING.name -> "dgdgd",
        constants.FormField.SHIPPER_NAME.name -> "srgsdg",
        constants.FormField.SHIPPER_ADDRESS.name -> "srgsdg",
        constants.FormField.NOTIFY_PARTY_NAME.name ->"srgsdg",
        constants.FormField.NOTIFY_PARTY_ADDRESS.name -> "srgsdg",
        constants.FormField.SHIPMENT_DATE.name ->"2019-11-11",
        constants.FormField.DELIVERY_TERM.name -> "FOB",
        constants.FormField.ASSET_QUANTITY.name -> "123",
        constants.FormField.ASSET_PRICE.name -> "123",
      ))
    )
    .pause(2)
    .feed(ImageFeeder2.imageFeed2)
    .exec(http("issue Asset Upload Invoice")
      .post(routes.FileController.uploadTraderAsset("INVOICE").url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        Form.RESUMABLE_CHUNK_NUMBER -> "1",
        Form.RESUMABLE_CHUNK_SIZE  -> "1048576",
        Form.RESUMABLE_TOTAL_SIZE -> "${%s}".format(Test.TEST_FILE_SIZE),
        Form.RESUMABLE_IDENTIFIER -> "document",
        Form.RESUMABLE_FILE_NAME -> "${%s}".format(Test.TEST_FILE_NAME)))
      .bodyPart(RawFileBodyPart("file", "/root/IdeaProjects/commitCentral/gatling/simulation/images/"+"${%s}".format(Test.TEST_FILE_NAME))
        .transferEncoding("binary")).asMultipartForm)
    .exec(
      http("STORE_AssetDocument_INVOICE")
        .get("/traderUpload/storeAssetDocument?name="+"${%s}".format(Test.TEST_FILE_NAME)+"&documentType="+"INVOICE&"+"issueAssetRequestID"+"="+"${%s}".format(Test.TEST_REQUEST_ID))
        .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN))
    )
    .pause(2)
    .exec(http("issue Asset Invoice post")
      .post(routes.IssueAssetController.issueAssetInvoice().url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        constants.FormField.REQUEST_ID.name -> "${%s}".format(Test.TEST_REQUEST_ID),
        constants.FormField.INVOICE_NUMBER.name ->"sdfgsdvsrdvsrev",
        constants.FormField.INVOICE_DATE.name -> "2019-11-11",
      ))
    )
    .foreach(issueAssetControllerTest.issueAssetDocumentType,"documentType"){
      feed(ImageFeeder.imageFeed)
        .exec(http("IssueAsset_UPLOAD_"+"${documentType}"+"_FORM")
          .get("/traderUpload/assetForm?documentType="+"${documentType}"+"&issueAssetRequestID="+"${%s}".format(Test.TEST_REQUEST_ID))
          .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN))
        )
        .pause(3)
        .exec(http("IssueAsset_UPLOAD_"+"${documentType}")
          .post("/traderUpload/assetDocument?documentType="+"${documentType}")
          .formParamMap(Map(
            Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
            Form.RESUMABLE_CHUNK_NUMBER -> "1",
            Form.RESUMABLE_CHUNK_SIZE  -> "1048576",
            Form.RESUMABLE_TOTAL_SIZE -> "${%s}".format(Test.TEST_FILE_SIZE),
            Form.RESUMABLE_IDENTIFIER -> "document",
            Form.RESUMABLE_FILE_NAME -> "${%s}".format(Test.TEST_FILE_NAME)))
          .bodyPart(RawFileBodyPart("file", "/root/IdeaProjects/commitCentral/gatling/simulation/images/"+"${%s}".format(Test.TEST_FILE_NAME))
            .transferEncoding("binary")).asMultipartForm)
        .exec(
          http("STORE_AssetDocument_"+"${documentType}")
            .get("/traderUpload/storeAssetDocument?name="+"${%s}".format(Test.TEST_FILE_NAME)+"&documentType="+"${documentType}"+"&issueAssetRequestID="+"${%s}".format(Test.TEST_REQUEST_ID))
        )
        .pause(2)
    }
    .pause(1)
    .exec(http("issue_Asset_Request_Form")
      .get("/master/issueAssetRequest?id="+"${%s}".format(Test.TEST_REQUEST_ID))
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN))
    )
    .pause(1)
    .exec(http("issue_Asset_Request_Submit")
      .post(routes.IssueAssetController.issueAssetRequest().url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        Form.REQUEST_ID -> "${%s}".format(Test.TEST_REQUEST_ID),
      ))
    )
    .pause(2)
}

object UnmoderatedBuyerAndSellerExecuteOrder{

  val unmoderatedBuyerAndSellerExecuteOrder=scenario("unmoderatedBuyerAndSellerExecuteOrder")
    .exec(http("LoginBuyer_GET")
      .get(routes.AccountController.loginForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("LoginBuyer_POST")
      .post(routes.AccountController.login().url)
      .formParamMap(Map(
        Form.USERNAME ->  "${%s}".format(Test.TEST_BUYER_USERNAME),
        Form.PASSWORD -> "${%s}".format(Test.TEST_BUYER_PASSWORD),
        Form.PUSH_NOTIFICATION_TOKEN -> "",
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
    .pause(3)
    .exec(session=> session.set(Test.TEST_NEGOTIATION_REQUEST_ID,confirmBuyerBidControllerTest.getNegotiationRequestIDFromSellerAccountID(session(Test.TEST_SELLER_USERNAME).as[String])).set(Test.TEST_NEGOTIATION_ID,confirmBuyerBidControllerTest.getNegotiationIDFromSellerAccountID(session(Test.TEST_SELLER_USERNAME).as[String])))
    .exec(http("BuyerExecuteOrderDocumentUpload_GetForm")
      .get("/master/buyerExecuteOrderDocument?orderID="+"${%s}".format(Test.TEST_NEGOTIATION_ID))
    )
    .pause(1)
    .exec(http("traderUploadNegotiationForm_FIAT_Proof")
      .get("/traderUpload/negotiationForm?documentType=FIAT_PROOF&negotiationRequestID="+"${%s}".format(Test.TEST_NEGOTIATION_REQUEST_ID))
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN))
    )
    .pause(2)
    .feed(ImageFeeder3.imageFeed3)
    .feed(GasFeeder.gasFeed)
    .exec(http("TraderUploadNegotiation_FIAT_Proof")
      .post(routes.FileController.uploadTraderNegotiation("FIAT_PROOF").url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        Form.RESUMABLE_CHUNK_NUMBER -> "1",
        Form.RESUMABLE_CHUNK_SIZE  -> "1048576",
        Form.RESUMABLE_TOTAL_SIZE -> "${%s}".format(Test.TEST_FILE_SIZE),
        Form.RESUMABLE_IDENTIFIER -> "document",
        Form.RESUMABLE_FILE_NAME -> "${%s}".format(Test.TEST_FILE_NAME)))
      .bodyPart(RawFileBodyPart("file", "/root/IdeaProjects/commitCentral/gatling/simulation/images/"+"${%s}".format(Test.TEST_FILE_NAME))
        .transferEncoding("binary")).asMultipartForm
    )
    .exec(
      http("storeNegotiationDocument_FIAT_PROOF")
        .get("/traderUpload/storeNegotiationDocument?name="+"${%s}".format(Test.TEST_FILE_NAME)+"&documentType=FIAT_PROOF&negotiationRequestID="+"${%s}".format(Test.TEST_NEGOTIATION_REQUEST_ID))
    )
    .pause(2)
    .exec(http("BuyerExecuteForm_Get")
      .get("/master/buyerExecuteOrder?requestID="+"${%s}".format(Test.TEST_NEGOTIATION_REQUEST_ID))
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN))
      .check(css("[name=%s]".format(Form.FIAT_PROOF_HASH), "value").saveAs(Form.FIAT_PROOF_HASH))
    )
    .pause(2)
    .exec(http("BuyerExecute_POST")
      .post(routes.BuyerExecuteOrderController.buyerExecuteOrder().url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        Form.SELLER_ADDRESS -> "${%s}".format(Test.TEST_SELLER_ADDRESS),
        Form.FIAT_PROOF_HASH -> "${%s}".format(Form.FIAT_PROOF_HASH),
        Form.PEG_HASH ->  "${%s}".format(Test.TEST_PEG_HASH),
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Form.PASSWORD -> "${%s}".format(Test.TEST_BUYER_PASSWORD)
      ))
    )
    .pause(3)
    .exec(http("SellerExecuteOrderDocumentUpload_GetForm")
      .get("/master/sellerExecuteOrderDocument?orderID="+"${%s}".format(Test.TEST_NEGOTIATION_ID))
    )
    .pause(1)
    .exec(http("traderUploadNegotiationForm_AWB_Proof")
      .get("/traderUpload/negotiationForm?documentType=AWB_PROOF&negotiationRequestID="+"${%s}".format(Test.TEST_NEGOTIATION_REQUEST_ID))
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN))
    )
    .pause(2)
    .feed(ImageFeeder4.imageFeed4)
    .feed(GasFeeder.gasFeed)
    .exec(http("traderUploadNegotiation_AWB_Proof")
      .post(routes.FileController.uploadZoneNegotiation("AWB_PROOF").url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        Form.RESUMABLE_CHUNK_NUMBER -> "1",
        Form.RESUMABLE_CHUNK_SIZE  -> "1048576",
        Form.RESUMABLE_TOTAL_SIZE -> "${%s}".format(Test.TEST_FILE_SIZE),
        Form.RESUMABLE_IDENTIFIER -> "document",
        Form.RESUMABLE_FILE_NAME -> "${%s}".format(Test.TEST_FILE_NAME)))
      .bodyPart(RawFileBodyPart("file", "/root/IdeaProjects/commitCentral/gatling/simulation/images/"+"${%s}".format(Test.TEST_FILE_NAME))
        .transferEncoding("binary")).asMultipartForm
    )
    .exec(
      http("storeNegotiationDocument_AWBProof")
        .get("/traderUpload/storeNegotiationDocument?name="+"${%s}".format(Test.TEST_FILE_NAME)+"&documentType=AWB_PROOF&negotiationRequestID="+"${%s}".format(Test.TEST_NEGOTIATION_REQUEST_ID))
    )
    .pause(2)
    .exec(http("moderatedSellerExecuteForm_Get")
      .get("/master/moderatedSellerExecuteOrder?requestID="+"${%s}".format(Test.TEST_NEGOTIATION_REQUEST_ID))
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN))
      .check(css("[name=%s]".format(Form.AWB_PROOF_HASH), "value").saveAs(Form.AWB_PROOF_HASH))
    )
    .pause(2)
    .exec(http("moderatedSellerExecute_POST")
      .post(routes.SellerExecuteOrderController.moderatedSellerExecuteOrder().url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        Form.BUYER_ADDRESS -> "${%s}".format(Test.TEST_BUYER_ADDRESS),
        Form.AWB_PROOF_HASH -> "${%s}".format(Form.AWB_PROOF_HASH),
        Form.PEG_HASH ->  "${%s}".format(Test.TEST_PEG_HASH),
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Form.PASSWORD ->  "${%s}".format(Test.TEST_SELLER_PASSWORD)
      ))
    )
}
