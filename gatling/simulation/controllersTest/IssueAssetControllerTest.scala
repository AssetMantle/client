package controllersTest

import constants.{Form, Test}
import controllers.routes
import feeders._
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef.jdbcFeeder

import scala.util.Random

class IssueAssetControllerTest extends Simulation {

  val scenarioBuilder: ScenarioBuilder = issueAssetControllerTest.issueAssetScenario
  setUp(scenarioBuilder.inject(atOnceUsers(1))).protocols(http.baseUrl(Test.BASE_URL))
}

object issueAssetControllerTest {

  val issueAssetDocumentType = Seq("CONTRACT", "PACKING_LIST", "COO", "COA", "OTHER")

  val allIssueAssetDocumentType = Seq("CONTRACT", "PACKING_LIST", "COO", "COA", "OTHER", "OBL", "INVOICE")

  val issueAssetRequestScenario: ScenarioBuilder = scenario("IssueAssetRequest")
    .feed(AssetTypeFeeder.assetTypeFeed)
    .feed(AssetPriceFeeder.assetPriceFeed)
    .feed(QuantityUnitFeeder.quantityUnitFeed)
    .feed(AssetQuantityFeeder.assetQuantityFeed)
    .feed(GasFeeder.gasFeed)
    .feed(IssueAssetDetailFeeder.issueAssetDetailFeeder)
    .exec(http("IssueAssetDetailForm_GET")
      .get(routes.IssueAssetController.issueAssetDetailForm(None).url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("IssueAssetDetail_POST")
      .post(routes.IssueAssetController.issueAssetDetail().url)
      .formParamMap(Map(
        Form.REQUEST_ID -> "",
        Form.ASSET_TYPE -> "${%s}".format(Test.TEST_ASSET_TYPE),
        Form.QUANTITY_UNIT -> "${%s}".format(Test.TEST_QUANTITY_UNIT),
        Form.ASSET_QUANTITY -> "${%s}".format(Test.TEST_ASSET_QUANTITY),
        Form.ASSET_PRICE -> "${%s}".format(Test.TEST_ASSET_PRICE),
        Form.TAKER_ADDRESS -> "",
        Form.COMMODITY_NAME -> "${%s}".format(Test.TEST_COMMODITY_NAME),
        Form.QUALITY -> "${%s}".format(Test.TEST_QUALITY),
        Form.DELIVERY_TERM -> "${%s}".format(Test.TEST_DELIVERY_TERM),
        Form.TRADE_TYPE -> "${%s}".format(Test.TEST_TRADE_TYPE),
        Form.PORT_OF_LOADING -> "${%s}".format(Test.TEST_PORT_OF_LOADING),
        Form.PORT_OF_DISCHARGE -> "${%s}".format(Test.TEST_PORT_OF_DISCHARGE),
        Form.SHIPMENT_DATE -> "${%s}".format(Test.TEST_SHIPMENT_DATE),
        Form.PHYSICAL_DOCUMENTS_HANDLED_VIA -> "COMDEX",
        Form.COMDEX_PAYMENT_TERMS -> "${%s}".format(Test.TEST_COMDEX_PAYMENT_TERMS),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN)))
      .check(substring("UPLOAD OBL").exists)
    )
    .pause(3)
    .exec { session => session.set(Test.TEST_REQUEST_ID, getRequestIDForIssueAsset(session(Test.TEST_SELLER_USERNAME).as[String])) }
    .exec(http("Upload OBL Form")
      .get(session => routes.FileController.uploadTraderAssetForm("OBL", session(Test.TEST_REQUEST_ID).as[String]).url)
      .check(substring("BROWSE").exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN))
    )
    .pause(2)
    .feed(ImageFeeder3.imageFeed3)
    .exec(http("Issue_Asset_Upload_OBL")
      .post(routes.FileController.uploadTraderAsset("OBL").url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        Form.RESUMABLE_CHUNK_NUMBER -> "1",
        Form.RESUMABLE_CHUNK_SIZE -> "1048576",
        Form.RESUMABLE_TOTAL_SIZE -> "${%s}".format(Test.TEST_FILE_SIZE),
        Form.RESUMABLE_IDENTIFIER -> "document",
        Form.RESUMABLE_FILE_NAME -> "${%s}".format(Test.TEST_FILE_NAME)))
      .bodyPart(RawFileBodyPart("file", Test.IMAGE_FILE_FEED + "${%s}".format(Test.TEST_FILE_NAME))
        .transferEncoding("binary")).asMultipartForm)
    .exec(
      http("Store_AssetDocument_OBL")
        .get(session => routes.FileController.storeTraderAsset(session(Test.TEST_FILE_NAME).as[String], "OBL", session(Test.TEST_REQUEST_ID).as[String]).url)
        .check(css("legend:contains(%s)".format(constants.Form.ISSUE_ASSET_OBL.legend)).exists)
        .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN))
    )
    .pause(2)
    .feed(IssueAssetOBLFeeder.issueAssetOBLFeeder)
    .exec(http("issue_Asset_OBL_Post")
      .post(routes.IssueAssetController.issueAssetOBL().url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        Form.REQUEST_ID -> "${%s}".format(Test.TEST_REQUEST_ID),
        Form.BILL_OF_LADING_NUMBER -> "${%s}".format(Test.TEST_BILL_OF_LADING_NUMBER),
        Form.PORT_OF_LOADING -> "${%s}".format(Test.TEST_PORT_OF_LOADING),
        Form.SHIPPER_NAME -> "${%s}".format(Test.TEST_SHIPPER_NAME),
        Form.SHIPPER_ADDRESS -> "${%s}".format(Test.TEST_SHIPPER_ADDRESS),
        Form.NOTIFY_PARTY_NAME -> "${%s}".format(Test.TEST_NOTIFY_PARTY_NAME),
        Form.NOTIFY_PARTY_ADDRESS -> "${%s}".format(Test.TEST_NOTIFY_PARTY_ADDRESS),
        Form.SHIPMENT_DATE -> "${%s}".format(Test.TEST_SHIPMENT_DATE),
        Form.DELIVERY_TERM -> "${%s}".format(Test.TEST_DELIVERY_TERM),
        Form.ASSET_QUANTITY -> "${%s}".format(Test.TEST_ASSET_QUANTITY),
        Form.ASSET_PRICE -> "${%s}".format(Test.TEST_ASSET_PRICE),
      ))
      .check(substring("UPLOAD INVOICE").exists)
    )
    .pause(2)
    .feed(ImageFeeder2.imageFeed2)
    .exec(http("Issue_Asset_Upload_Invoice")
      .post(routes.FileController.uploadTraderAsset("INVOICE").url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        Form.RESUMABLE_CHUNK_NUMBER -> "1",
        Form.RESUMABLE_CHUNK_SIZE -> "1048576",
        Form.RESUMABLE_TOTAL_SIZE -> "${%s}".format(Test.TEST_FILE_SIZE),
        Form.RESUMABLE_IDENTIFIER -> "document",
        Form.RESUMABLE_FILE_NAME -> "${%s}".format(Test.TEST_FILE_NAME)))
      .bodyPart(RawFileBodyPart("file", Test.IMAGE_FILE_FEED + "${%s}".format(Test.TEST_FILE_NAME))
        .transferEncoding("binary")).asMultipartForm)
    .exec(
      http("STORE_AssetDocument_INVOICE")
        .get(session => routes.FileController.storeTraderAsset(session(Test.TEST_FILE_NAME).as[String], "INVOICE", session(Test.TEST_REQUEST_ID).as[String]).url)
        .check(css("legend:contains(%s)".format(constants.Form.ISSUE_ASSET_INVOICE.legend)).exists)
        .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN))
    )
    .pause(2)
    .exec(http("Issue_Asset_Invoice_POST")
      .post(routes.IssueAssetController.issueAssetInvoice().url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        constants.FormField.REQUEST_ID.name -> "${%s}".format(Test.TEST_REQUEST_ID),
        constants.FormField.INVOICE_NUMBER.name -> "sdfgsdvsrdvsrev",
        constants.FormField.INVOICE_DATE.name -> "2019-11-11",
      ))
      .check(substring("UPLOAD CONTRACT").exists)
      .check(substring("UPLOAD PACKING_LIST").exists)
      .check(substring("UPLOAD COO").exists)
      .check(substring("UPLOAD COA").exists)
      .check(substring("UPLOAD OTHER").exists)
    )
    .pause(2)
    .foreach(issueAssetDocumentType, "documentType") {
      feed(ImageFeeder.imageFeed)
        .exec(http("Issue_Asset_Upload_" + "${documentType}" + "_FORM")
          .get(session => routes.FileController.uploadTraderAssetForm(session("documentType").as[String], session(Test.TEST_REQUEST_ID).as[String]).url)
          .check(substring("BROWSE").exists)
          .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN))
        )
        .pause(3)
        .exec(http("Issue_Asset_Upload_" + "${documentType}")
          .post(session => routes.FileController.uploadTraderAsset(session("documentType").as[String]).url)
          .formParamMap(Map(
            Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
            Form.RESUMABLE_CHUNK_NUMBER -> "1",
            Form.RESUMABLE_CHUNK_SIZE -> "1048576",
            Form.RESUMABLE_TOTAL_SIZE -> "${%s}".format(Test.TEST_FILE_SIZE),
            Form.RESUMABLE_IDENTIFIER -> "document",
            Form.RESUMABLE_FILE_NAME -> "${%s}".format(Test.TEST_FILE_NAME)))
          .bodyPart(RawFileBodyPart("file", Test.IMAGE_FILE_FEED + "${%s}".format(Test.TEST_FILE_NAME))
            .transferEncoding("binary")).asMultipartForm)
        .exec(
          http("Store_Asset_Document_" + "${documentType}")
            .get(session => routes.FileController.storeTraderAsset(session(Test.TEST_FILE_NAME).as[String], session("documentType").as[String], session(Test.TEST_REQUEST_ID).as[String]).url)
        )
        .pause(2)
    }
    .pause(1)
    .exec(http("Issue_Asset_Request_Form")
      .get(session => routes.IssueAssetController.issueAssetRequestForm(session(Test.TEST_REQUEST_ID).as[String]).url)
      .check(css("legend:contains(%s)".format(constants.Form.ISSUE_ASSET_REQUEST.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN))
    )
    .pause(1)
    .exec(http("Issue_Asset_Request")
      .post(routes.IssueAssetController.issueAssetRequest().url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        Form.REQUEST_ID -> "${%s}".format(Test.TEST_REQUEST_ID),
      ))
      .check(substring("SUCCESS ISSUE_ASSET_REQUEST_SENT").exists )
    )
    .pause(3)

  val unmoderatedIssueAssetRequestScenario=scenario("UnmoderatedIssueAssetRequestScenario")
    .feed(AssetTypeFeeder.assetTypeFeed)
    .feed(AssetPriceFeeder.assetPriceFeed)
    .feed(QuantityUnitFeeder.quantityUnitFeed)
    .feed(AssetQuantityFeeder.assetQuantityFeed)
    .feed(GasFeeder.gasFeed)
    .feed(IssueAssetDetailFeeder.issueAssetDetailFeeder)
    .exec(http("IssueAssetDetailForm_GET")
      .get(routes.IssueAssetController.issueAssetDetailForm(None).url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("IssueAssetDetail_POST")
      .post(routes.IssueAssetController.issueAssetDetail().url)
      .formParamMap(Map(
        Form.REQUEST_ID -> "",
        Form.ASSET_TYPE -> "${%s}".format(Test.TEST_ASSET_TYPE),
        Form.QUANTITY_UNIT -> "${%s}".format(Test.TEST_QUANTITY_UNIT),
        Form.ASSET_QUANTITY -> "${%s}".format(Test.TEST_ASSET_QUANTITY),
        Form.ASSET_PRICE -> "${%s}".format(Test.TEST_ASSET_PRICE),
        Form.TAKER_ADDRESS -> "",
        Form.COMMODITY_NAME -> "${%s}".format(Test.TEST_COMMODITY_NAME),
        Form.QUALITY -> "${%s}".format(Test.TEST_QUALITY),
        Form.DELIVERY_TERM -> "${%s}".format(Test.TEST_DELIVERY_TERM),
        Form.TRADE_TYPE -> "${%s}".format(Test.TEST_TRADE_TYPE),
        Form.PORT_OF_LOADING -> "${%s}".format(Test.TEST_PORT_OF_LOADING),
        Form.PORT_OF_DISCHARGE -> "${%s}".format(Test.TEST_PORT_OF_DISCHARGE),
        Form.SHIPMENT_DATE -> "${%s}".format(Test.TEST_SHIPMENT_DATE),
        Form.PHYSICAL_DOCUMENTS_HANDLED_VIA -> "BANK",
        Form.COMDEX_PAYMENT_TERMS -> "${%s}".format(Test.TEST_COMDEX_PAYMENT_TERMS),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN)))
      .check(substring("UPLOAD OBL").exists)
    )
    .pause(3)
    .exec { session => session.set(Test.TEST_REQUEST_ID, getRequestIDForIssueAsset(session(Test.TEST_SELLER_USERNAME).as[String])) }
    .exec(http("Upload OBL Form")
      .get(session => routes.FileController.uploadTraderAssetForm("OBL", session(Test.TEST_REQUEST_ID).as[String]).url)
      .check(substring("BROWSE").exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN))
    )
    .pause(2)
    .feed(ImageFeeder3.imageFeed3)
    .exec(http("Issue_Asset_Upload_OBL")
      .post(routes.FileController.uploadTraderAsset("OBL").url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        Form.RESUMABLE_CHUNK_NUMBER -> "1",
        Form.RESUMABLE_CHUNK_SIZE -> "1048576",
        Form.RESUMABLE_TOTAL_SIZE -> "${%s}".format(Test.TEST_FILE_SIZE),
        Form.RESUMABLE_IDENTIFIER -> "document",
        Form.RESUMABLE_FILE_NAME -> "${%s}".format(Test.TEST_FILE_NAME)))
      .bodyPart(RawFileBodyPart("file", Test.IMAGE_FILE_FEED + "${%s}".format(Test.TEST_FILE_NAME))
        .transferEncoding("binary")).asMultipartForm)
    .exec(
      http("Store_AssetDocument_OBL")
        .get(session => routes.FileController.storeTraderAsset(session(Test.TEST_FILE_NAME).as[String], "OBL", session(Test.TEST_REQUEST_ID).as[String]).url)
        .check(css("legend:contains(%s)".format(constants.Form.ISSUE_ASSET_OBL.legend)).exists)
        .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN))
    )
    .pause(2)
    .feed(IssueAssetOBLFeeder.issueAssetOBLFeeder)
    .exec(http("issue_Asset_OBL_Post")
      .post(routes.IssueAssetController.issueAssetOBL().url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        Form.REQUEST_ID -> "${%s}".format(Test.TEST_REQUEST_ID),
        Form.BILL_OF_LADING_NUMBER -> "${%s}".format(Test.TEST_BILL_OF_LADING_NUMBER),
        Form.PORT_OF_LOADING -> "${%s}".format(Test.TEST_PORT_OF_LOADING),
        Form.SHIPPER_NAME -> "${%s}".format(Test.TEST_SHIPPER_NAME),
        Form.SHIPPER_ADDRESS -> "${%s}".format(Test.TEST_SHIPPER_ADDRESS),
        Form.NOTIFY_PARTY_NAME -> "${%s}".format(Test.TEST_NOTIFY_PARTY_NAME),
        Form.NOTIFY_PARTY_ADDRESS -> "${%s}".format(Test.TEST_NOTIFY_PARTY_ADDRESS),
        Form.SHIPMENT_DATE -> "${%s}".format(Test.TEST_SHIPMENT_DATE),
        Form.DELIVERY_TERM -> "${%s}".format(Test.TEST_DELIVERY_TERM),
        Form.ASSET_QUANTITY -> "${%s}".format(Test.TEST_ASSET_QUANTITY),
        Form.ASSET_PRICE -> "${%s}".format(Test.TEST_ASSET_PRICE),
      ))
      .check(substring("UPLOAD INVOICE").exists)
    )
    .pause(2)
    .feed(ImageFeeder2.imageFeed2)
    .exec(http("Issue_Asset_Upload_Invoice")
      .post(routes.FileController.uploadTraderAsset("INVOICE").url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        Form.RESUMABLE_CHUNK_NUMBER -> "1",
        Form.RESUMABLE_CHUNK_SIZE -> "1048576",
        Form.RESUMABLE_TOTAL_SIZE -> "${%s}".format(Test.TEST_FILE_SIZE),
        Form.RESUMABLE_IDENTIFIER -> "document",
        Form.RESUMABLE_FILE_NAME -> "${%s}".format(Test.TEST_FILE_NAME)))
      .bodyPart(RawFileBodyPart("file", Test.IMAGE_FILE_FEED + "${%s}".format(Test.TEST_FILE_NAME))
        .transferEncoding("binary")).asMultipartForm)
    .exec(
      http("STORE_AssetDocument_INVOICE")
        .get(session => routes.FileController.storeTraderAsset(session(Test.TEST_FILE_NAME).as[String], "INVOICE", session(Test.TEST_REQUEST_ID).as[String]).url)
        .check(css("legend:contains(%s)".format(constants.Form.ISSUE_ASSET_INVOICE.legend)).exists)
        .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN))
    )
    .pause(2)
    .exec(http("Issue_Asset_Invoice_POST")
      .post(routes.IssueAssetController.issueAssetInvoice().url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        constants.FormField.REQUEST_ID.name -> "${%s}".format(Test.TEST_REQUEST_ID),
        constants.FormField.INVOICE_NUMBER.name -> "sdfgsdvsrdvsrev",
        constants.FormField.INVOICE_DATE.name -> "2019-11-11",
      ))
      .check(substring("UPLOAD CONTRACT").exists)
      .check(substring("UPLOAD PACKING_LIST").exists)
      .check(substring("UPLOAD COO").exists)
      .check(substring("UPLOAD COA").exists)
      .check(substring("UPLOAD OTHER").exists)
    )
    .pause(2)
    .foreach(issueAssetDocumentType, "documentType") {
      feed(ImageFeeder.imageFeed)
        .exec(http("Issue_Asset_Upload_" + "${documentType}" + "_FORM")
          .get(session => routes.FileController.uploadTraderAssetForm(session("documentType").as[String], session(Test.TEST_REQUEST_ID).as[String]).url)
          .check(substring("BROWSE").exists)
          .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN))
        )
        .pause(3)
        .exec(http("Issue_Asset_Upload_" + "${documentType}")
          .post(session => routes.FileController.uploadTraderAsset(session("documentType").as[String]).url)
          .formParamMap(Map(
            Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
            Form.RESUMABLE_CHUNK_NUMBER -> "1",
            Form.RESUMABLE_CHUNK_SIZE -> "1048576",
            Form.RESUMABLE_TOTAL_SIZE -> "${%s}".format(Test.TEST_FILE_SIZE),
            Form.RESUMABLE_IDENTIFIER -> "document",
            Form.RESUMABLE_FILE_NAME -> "${%s}".format(Test.TEST_FILE_NAME)))
          .bodyPart(RawFileBodyPart("file", Test.IMAGE_FILE_FEED + "${%s}".format(Test.TEST_FILE_NAME))
            .transferEncoding("binary")).asMultipartForm)
        .exec(
          http("Store_Asset_Document_" + "${documentType}")
            .get(session => routes.FileController.storeTraderAsset(session(Test.TEST_FILE_NAME).as[String], session("documentType").as[String], session(Test.TEST_REQUEST_ID).as[String]).url)
        )
        .pause(2)
    }
    .pause(1)
    .exec(http("Issue_Asset_Request_Form")
      .get(session => routes.IssueAssetController.issueAssetRequestForm(session(Test.TEST_REQUEST_ID).as[String]).url)
      .check(css("legend:contains(%s)".format(constants.Form.ISSUE_ASSET_REQUEST.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN))
    )
    .pause(1)
    .exec(http("Issue_Asset_Request")
      .post(routes.IssueAssetController.issueAssetRequest().url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        Form.GAS ->"${%s}".format(Test.TEST_GAS),
        Form.PASSWORD->"${%s}".format(Test.TEST_PASSWORD),
        Form.REQUEST_ID -> "${%s}".format(Test.TEST_REQUEST_ID),
      ))
      .check(substring("SUCCESS ASSET_ISSUED").exists )
    )
    .pause(3)

  val issueAssetScenario = scenario("Issue Asset")
    .exec(http("Get_Pending_Issue_Asset_Request")
      .get(routes.IssueAssetController.viewPendingIssueAssetRequests().url)
      .check(substring("${%s}".format(Test.TEST_SELLER_USERNAME)).exists)
    )
    .exec { session => session.set(Test.TEST_REQUEST_ID, getRequestIDForIssueAsset(session(Test.TEST_SELLER_USERNAME).as[String])) }
    .pause(2)
    .foreach(allIssueAssetDocumentType, "documentType") {
      exec(http("Update_Asset_Document_Status_" + "${documentType}")
        .post(routes.IssueAssetController.updateAssetDocumentStatus().url)
        .formParamMap(Map(
          Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
          Form.REQUEST_ID -> "${%s}".format(Test.TEST_REQUEST_ID),
          Form.DOCUMENT_TYPE -> "${documentType}",
          Form.STATUS -> true
        ))
      )
        .pause(1)
    }
    .exec(session => session.set(Test.TEST_DOCUMENT_HASH, getDocumentHashForIssueAsset(session(Test.TEST_SELLER_USERNAME).as[String])).set(Test.TEST_ASSET_TYPE, getAssetTypeForIssueAsset(session(Test.TEST_SELLER_USERNAME).as[String])).set(Test.TEST_ASSET_PRICE, getAssetPriceForIssueAsset(session(Test.TEST_SELLER_USERNAME).as[String])).set(Test.TEST_QUANTITY_UNIT, getQuantityUnitForIssueAsset(session(Test.TEST_SELLER_USERNAME).as[String])).set(Test.TEST_ASSET_QUANTITY, getAssetQuantityForIssueAsset(session(Test.TEST_SELLER_USERNAME).as[String])))
    .exec(http("Issue_Asset_Form")
      .get(session => routes.IssueAssetController.issueAssetForm(session(Test.TEST_REQUEST_ID).as[String], session(Test.TEST_SELLER_USERNAME).as[String], session(Test.TEST_DOCUMENT_HASH).as[String], session(Test.TEST_ASSET_TYPE).as[String], session(Test.TEST_ASSET_PRICE).as[Int], session(Test.TEST_QUANTITY_UNIT).as[String], session(Test.TEST_ASSET_QUANTITY).as[Int], None).url)
      .check(css("legend:contains(%s)".format(constants.Form.ISSUE_ASSET.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN))
    )
    .pause(2)
    .feed(GasFeeder.gasFeed)
    .exec(http("IssueAsset")
      .post(routes.IssueAssetController.issueAsset().url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        Form.REQUEST_ID -> "${%s}".format(Test.TEST_REQUEST_ID),
        Form.ACCOUNT_ID -> "${%s}".format(Test.TEST_SELLER_USERNAME),
        Form.DOCUMENT_HASH -> "${%s}".format(Test.TEST_DOCUMENT_HASH),
        Form.ASSET_TYPE -> "${%s}".format(Test.TEST_ASSET_TYPE),
        Form.ASSET_PRICE -> "${%s}".format(Test.TEST_ASSET_PRICE),
        Form.QUANTITY_UNIT -> "${%s}".format(Test.TEST_QUANTITY_UNIT),
        Form.ASSET_QUANTITY -> "${%s}".format(Test.TEST_ASSET_QUANTITY),
        Form.TAKER_ADDRESS -> "",
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Form.PASSWORD -> "${%s}".format(Test.TEST_ZONE_PASSWORD)
      ))
      .check(substring("SUCCESS ASSET_ISSUED").exists)
    )
    .pause(3)


  val rejectIssueAssetScenario: ScenarioBuilder = scenario("RejectIssueAsset")
    .exec(http("RejectIssueAsset_GET")
      .get(routes.IssueAssetController.rejectIssueAssetRequestForm(Test.TEST_REQUEST_ID).url)
      .check(css("legend:contains(%s)".format(constants.Form.REJECT_ISSUE_ASSET_REQUEST.legend)).exists)
      .check(css("[name=%s]".format(Form.REQUEST_ID), "value").saveAs(Test.TEST_REQUEST_ID))
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("RejectIssueAsset_POST")
      .post(routes.IssueAssetController.issueAssetRequest().url)
      .formParamMap(Map(
        Form.REQUEST_ID -> "${%s}".format(Test.TEST_REQUEST_ID),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN)))
      .check(substring("SUCCESS ISSUE_ASSET_REQUEST_REJECTED").exists)
    )
    .pause(5)

  val blockchainIssueAssetScenario: ScenarioBuilder = scenario("BlockchainIssueAsset")
    .feed(FromFeeder.fromFeed)
    .feed(ToFeeder.toFeed)
    .feed(DocumentHashFeeder.documentHashFeed)
    .feed(PasswordFeeder.passwordFeed)
    .feed(GasFeeder.gasFeed)
    .feed(ModeFeeder.modeFeed)
    .feed(IssueAssetDetailFeeder.issueAssetDetailFeeder)
    .exec(http("BlockchainIssueAsset_GET")
      .get(routes.IssueAssetController.blockchainIssueAssetForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.BLOCKCHAIN_ISSUE_ASSET.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("BlockchainIssueAsset_POST")
      .post(routes.IssueAssetController.blockchainIssueAsset().url)
      .formParamMap(Map(
        Form.FROM -> "${%s}".format(Test.TEST_FROM),
        Form.TO -> "${%s}".format(Test.TEST_TO),
        Form.DOCUMENT_HASH -> "${%s}".format(Test.TEST_DOCUMENT_HASH),
        Form.ASSET_TYPE -> "${%s}".format(Test.TEST_ASSET_TYPE),
        Form.ASSET_PRICE -> "${%s}".format(Test.TEST_ASSET_PRICE),
        Form.QUANTITY_UNIT -> "${%s}".format(Test.TEST_QUANTITY_UNIT),
        Form.ASSET_QUANTITY -> "${%s}".format(Test.TEST_ASSET_QUANTITY),
        Form.MODERATED -> Random.nextBoolean(),
        Form.TAKER_ADDRESS -> "${%s}".format(Test.TEST_TO),
        Form.PASSWORD -> "${%s}".format(Test.TEST_PASSWORD),
        Form.MODE ->"${%s}".format(Test.TEST_MODE),
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))

  def getRequestIDForIssueAsset(query: String): String = {
    val sqlQueryFeeder = jdbcFeeder("jdbc:postgresql://localhost:5432/commit", "commit", "commit",
      s"""SELECT COALESCE((SELECT "id" FROM master_transaction."IssueAssetRequest" WHERE "accountID" = '$query'),'0') AS "id";""")
    sqlQueryFeeder.apply().next()("id").toString
  }

  def getDocumentHashForIssueAsset(query: String): String = {
    val sqlQueryFeeder = jdbcFeeder("jdbc:postgresql://localhost:5432/commit", "commit", "commit",
      s"""SELECT COALESCE((SELECT "documentHash" FROM master_transaction."IssueAssetRequest" WHERE "accountID" = '$query'),'0') AS "id";""")
    sqlQueryFeeder.apply().next()("id").toString
  }

  def getAssetTypeForIssueAsset(query: String): String = {
    val sqlQueryFeeder = jdbcFeeder("jdbc:postgresql://localhost:5432/commit", "commit", "commit",
      s"""SELECT COALESCE((SELECT "assetType" FROM master_transaction."IssueAssetRequest" WHERE "accountID" = '$query'),'0') AS "id";""")
    sqlQueryFeeder.apply().next()("id").toString
  }

  def getAssetPriceForIssueAsset(query: String): String = {
    val sqlQueryFeeder = jdbcFeeder("jdbc:postgresql://localhost:5432/commit", "commit", "commit",
      s"""SELECT COALESCE((SELECT "assetPrice" FROM master_transaction."IssueAssetRequest" WHERE "accountID" = '$query'),'0') AS "id";""")
    sqlQueryFeeder.apply().next()("id").toString
  }

  def getQuantityUnitForIssueAsset(query: String): String = {
    val sqlQueryFeeder = jdbcFeeder("jdbc:postgresql://localhost:5432/commit", "commit", "commit",
      s"""SELECT COALESCE((SELECT "quantityUnit" FROM master_transaction."IssueAssetRequest" WHERE "accountID" = '$query'),'0') AS "id";""")
    sqlQueryFeeder.apply().next()("id").toString
  }

  def getAssetQuantityForIssueAsset(query: String): String = {
    val sqlQueryFeeder = jdbcFeeder("jdbc:postgresql://localhost:5432/commit", "commit", "commit",
      s"""SELECT COALESCE((SELECT "assetQuantity" FROM master_transaction."IssueAssetRequest" WHERE "accountID" = '$query'),'0') AS "id";""")
    sqlQueryFeeder.apply().next()("id").toString
  }

  def getPegHashByOwnerAddress(accountID: String): String = {
    println(accountID)
    val sqlQueryFeeder = jdbcFeeder("jdbc:postgresql://localhost:5432/commit", "commit", "commit",
      s"""SELECT COALESCE((SELECT "pegHash" FROM master_transaction."IssueAssetRequest" WHERE "accountID" = '$accountID'),'0') AS "pegHash";""")
    sqlQueryFeeder.apply().next()("pegHash").toString
  }
}
