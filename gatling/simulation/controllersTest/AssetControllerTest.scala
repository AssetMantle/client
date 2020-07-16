package controllersTest

import constants.{Form, Test}
import controllers.routes
import feeders._
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef.jdbcFeeder

import scala.util.Random

object AssetControllerTest {

  val moderatedIssueAssetRequestScenario: ScenarioBuilder = scenario("IssueAssetRequest")
    .feed(AssetDetailFeeder.assetDetailFeed)
    .feed(ShippingDetailsFeeder.shippingDetailsFeeder)
    .exec(http("ModeratedIssueAssetRequestForm_GET")
      .get(routes.AssetController.issueForm().url)
      .check(status.is(200))
      .check(css("legend:contains(Add Commodity)").exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN))
    )
    .pause(Test.REQUEST_DELAY)
    .exec(http("ModeratedIssueAssetRequest_POST")
      .post(routes.AssetController.issue().url)
      .formParamMap(Map(
        constants.FormField.ASSET_TYPE.name -> "${%s}".format(Test.TEST_ASSET_TYPE),
        constants.FormField.ASSET_DESCRIPTION.name -> "${%s}".format(Test.TEST_ASSET_DESCRIPTION),
        constants.FormField.ASSET_QUANTITY.name -> "${%s}".format(Test.TEST_ASSET_QUANTITY),
        constants.FormField.QUANTITY_UNIT.name -> "${%s}".format(Test.TEST_QUANTITY_UNIT),
        constants.FormField.ASSET_PRICE_PER_UNIT.name -> "${%s}".format(Test.TEST_ASSET_PRICE_PER_UNIT),
        constants.FormField.SHIPPING_PERIOD.name -> "${%s}".format(Test.TEST_SHIPPING_PERIOD),
        constants.FormField.PORT_OF_LOADING.name -> "${%s}".format(Test.TEST_PORT_OF_LOADING),
        constants.FormField.PORT_OF_DISCHARGE.name -> "${%s}".format(Test.TEST_PORT_OF_DISCHARGE),
        constants.FormField.MODERATED.name -> true,
        constants.FormField.GAS.name -> "",
        constants.FormField.PASSWORD.name -> "",
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
      .check(status.is(206))
      .check(css("legend:contains(Create Sales Quote)").exists)
    )
    .pause(Test.REQUEST_DELAY)

  val unmoderatedIssueAssetRequestScenario: ScenarioBuilder = scenario("IssueAssetRequest")
    .feed(AssetDetailFeeder.assetDetailFeed)
    .feed(GasFeeder.gasFeed)
    .feed(ShippingDetailsFeeder.shippingDetailsFeeder)
    .exec(http("UnmoderatedIssueAssetRequestForm_GET")
      .get(routes.AssetController.issueForm().url)
      .check(status.is(200))
      .check(css("legend:contains(Add Commodity)").exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN))
    )
    .pause(Test.REQUEST_DELAY)
    .exec(http("UnmoderatedIssueAssetRequest_POST")
      .post(routes.AssetController.issue().url)
      .formParamMap(Map(
        constants.FormField.ASSET_TYPE.name -> "${%s}".format(Test.TEST_ASSET_TYPE),
        constants.FormField.ASSET_DESCRIPTION.name -> "${%s}".format(Test.TEST_ASSET_DESCRIPTION),
        constants.FormField.ASSET_QUANTITY.name -> "${%s}".format(Test.TEST_ASSET_QUANTITY),
        constants.FormField.QUANTITY_UNIT.name -> "${%s}".format(Test.TEST_QUANTITY_UNIT),
        constants.FormField.ASSET_PRICE_PER_UNIT.name -> "${%s}".format(Test.TEST_ASSET_PRICE_PER_UNIT),
        constants.FormField.SHIPPING_PERIOD.name -> "${%s}".format(Test.TEST_SHIPPING_PERIOD),
        constants.FormField.PORT_OF_LOADING.name -> "${%s}".format(Test.TEST_PORT_OF_LOADING),
        constants.FormField.PORT_OF_DISCHARGE.name -> "${%s}".format(Test.TEST_PORT_OF_DISCHARGE),
        constants.FormField.MODERATED.name -> false,
        constants.FormField.GAS.name -> "${%s}".format(Test.TEST_GAS),
        constants.FormField.PASSWORD.name -> "${%s}".format(Test.TEST_PASSWORD),
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
      .check(status.is(206))
      .check(css("legend:contains(Create Sales Quote)").exists)
    )
    .pause(Test.REQUEST_DELAY)

  val uploadAssetDocuments: ScenarioBuilder = scenario("uploadAssetDocuments")
    .foreach(constants.File.ASSET_DOCUMENTS, Test.TEST_DOCUMENT_TYPE) {
      feed(ImageFeeder.imageFeed)
        .exec(http("Asset_Document_Upload_" + "${%s}".format(Test.TEST_DOCUMENT_TYPE) + "_FORM")
          .get(session => routes.FileController.uploadAssetForm(session(Test.TEST_DOCUMENT_TYPE).as[String], session(Test.TEST_ASSET_ID).as[String]).url)
          .check(css("button:contains(Browse)").exists)
          .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN))
        )
        .pause(Test.REQUEST_DELAY)
        .exec(http("Asset_Document_Upload_" + "${%s}".format(Test.TEST_DOCUMENT_TYPE))
          .post(session => routes.FileController.uploadAsset(session(Test.TEST_DOCUMENT_TYPE).as[String]).url)
          .formParamMap(Map(
            Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN),
            Form.RESUMABLE_CHUNK_NUMBER -> "1",
            Form.RESUMABLE_CHUNK_SIZE -> "1048576",
            Form.RESUMABLE_TOTAL_SIZE -> "${%s}".format(Test.TEST_FILE_SIZE),
            Form.RESUMABLE_IDENTIFIER -> "document",
            Form.RESUMABLE_FILE_NAME -> "${%s}".format(Test.TEST_FILE_NAME)))
          .bodyPart(RawFileBodyPart("file", Test.IMAGE_FILE_FEED + "${%s}".format(Test.TEST_FILE_NAME))
            .transferEncoding("binary")).asMultipartForm)
        .exec(
          http("Store_Asset_Document_" + "${%s}".format(Test.TEST_DOCUMENT_TYPE))
            .get(session => routes.FileController.storeAsset(session(Test.TEST_FILE_NAME).as[String], session(Test.TEST_DOCUMENT_TYPE).as[String], session(Test.TEST_NEGOTIATION_ID).as[String]).url)
        )
        .pause(Test.REQUEST_DELAY)
    }

  val updateAssetDocuments: ScenarioBuilder = scenario("updateAssetDocuments")
    .foreach(constants.File.ASSET_DOCUMENTS, Test.TEST_DOCUMENT_TYPE) {
      feed(ImageFeeder.imageFeed)
        .exec(http("Asset_Document_Update_" + "${%s}".format(Test.TEST_DOCUMENT_TYPE) + "_FORM")
          .get(session => routes.FileController.updateAssetForm(session(Test.TEST_DOCUMENT_TYPE).as[String], session(Test.TEST_ASSET_ID).as[String]).url)
          .check(css("button:contains(Browse)").exists)
          .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN))
        )
        .pause(Test.REQUEST_DELAY)
        .exec(http("Asset_Document_Upload_" + "${%s}".format(Test.TEST_DOCUMENT_TYPE))
          .post(session => routes.FileController.uploadAsset(session(Test.TEST_DOCUMENT_TYPE).as[String]).url)
          .formParamMap(Map(
            Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN),
            Form.RESUMABLE_CHUNK_NUMBER -> "1",
            Form.RESUMABLE_CHUNK_SIZE -> "1048576",
            Form.RESUMABLE_TOTAL_SIZE -> "${%s}".format(Test.TEST_FILE_SIZE),
            Form.RESUMABLE_IDENTIFIER -> "document",
            Form.RESUMABLE_FILE_NAME -> "${%s}".format(Test.TEST_FILE_NAME)))
          .bodyPart(RawFileBodyPart("file", Test.IMAGE_FILE_FEED + "${%s}".format(Test.TEST_FILE_NAME))
            .transferEncoding("binary")).asMultipartForm)
        .exec(
          http("Update_Asset_Document_" + "${%s}".format(Test.TEST_DOCUMENT_TYPE))
            .get(session => routes.FileController.updateAsset(session(Test.TEST_FILE_NAME).as[String], session(Test.TEST_DOCUMENT_TYPE).as[String], session(Test.TEST_NEGOTIATION_ID).as[String]).url)
        )
        .pause(Test.REQUEST_DELAY)
    }

  val addBillOfLading: ScenarioBuilder = scenario("AddBillOfLading")
    .feed(OBLFeeder.oblFeed)
    .feed(AssetDetailFeeder.assetDetailFeed)
    .feed(ShippingDetailsFeeder.shippingDetailsFeeder)
    .exec(http("AddBillOfLadingForm_GET")
      .get(session => routes.AssetController.addBillOfLadingForm(session(Test.TEST_NEGOTIATION_ID).as[String]).url)
      .check(status.is(200))
      .check(css("legend:contains(Add Bill Of Lading)").exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN)))
    .pause(Test.REQUEST_DELAY)
    .exec(http("AddBillOfLading_POST")
      .post(routes.AssetController.addBillOfLading().url)
      .formParamMap(Map(
        constants.FormField.NEGOTIATION_ID.name -> "${%s}".format(Test.TEST_NEGOTIATION_ID),
        constants.FormField.BILL_OF_LADING_NUMBER.name -> "${%s}".format(Test.TEST_BILL_OF_LADING_NUMBER),
        constants.FormField.CONSIGNEE_TO.name -> "${%s}".format(Test.TEST_CONSIGNEE_TO),
        constants.FormField.VESSEL_NAME.name -> "${%s}".format(Test.TEST_VESSEL_NAME),
        constants.FormField.PORT_OF_LOADING.name -> "${%s}".format(Test.TEST_PORT_OF_LOADING),
        constants.FormField.PORT_OF_DISCHARGE.name -> "${%s}".format(Test.TEST_PORT_OF_DISCHARGE),
        constants.FormField.SHIPPER_NAME.name -> "${%s}".format(Test.TEST_SHIPPER_NAME),
        constants.FormField.SHIPPER_ADDRESS.name -> "${%s}".format(Test.TEST_SHIPPER_ADDRESS),
        constants.FormField.NOTIFY_PARTY_NAME.name -> "${%s}".format(Test.TEST_NOTIFY_PARTY_NAME),
        constants.FormField.NOTIFY_PARTY_ADDRESS.name -> "${%s}".format(Test.TEST_NOTIFY_PARTY_ADDRESS),
        constants.FormField.SHIPMENT_DATE.name -> "${%s}".format(Test.TEST_SHIPMENT_DATE),
        constants.FormField.DELIVERY_TERM.name -> "${%s}".format(Test.TEST_DELIVERY_TERM),
        constants.FormField.ASSET_DESCRIPTION.name -> "${%s}".format(Test.TEST_ASSET_DESCRIPTION),
        constants.FormField.ASSET_QUANTITY.name -> "${%s}".format(Test.TEST_ASSET_QUANTITY),
        constants.FormField.QUANTITY_UNIT.name -> "${%s}".format(Test.TEST_QUANTITY_UNIT),
        constants.FormField.ASSET_PRICE_PER_UNIT.name -> "${%s}".format(Test.TEST_ASSET_PRICE_PER_UNIT),
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
      .check(status.is(206))
      .check(substring("Upload Documents").exists)
      .check(substring("Commodity Documents").exists)
      .check(substring("Trade Documents").exists)
    )
    .pause(Test.REQUEST_DELAY)

  val acceptBillOfLading: ScenarioBuilder = scenario("AcceptBillOfLading")
    .exec(http("AcceptOrRejectAssetDocumentForm_GET")
      .get(session => routes.AssetController.acceptOrRejectAssetDocumentForm(session(Test.TEST_NEGOTIATION_ID).as[String], constants.File.Asset.BILL_OF_LADING).url)
      .check(status.is(200))
      .check(css("button:contains(Approve)").exists)
      .check(css("button:contains(Reject)").exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN))
    )
    .pause(Test.REQUEST_DELAY)
    .exec(http("AcceptOrRejectAssetDocument_POST")
      .post(routes.AssetController.acceptOrRejectAssetDocument().url)
      .formParamMap(Map(
        constants.FormField.NEGOTIATION_ID.name -> "${%s}".format(Test.TEST_NEGOTIATION_ID),
        constants.FormField.DOCUMENT_TYPE.name -> constants.File.Asset.BILL_OF_LADING,
        constants.FormField.STATUS.name -> true,
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
      .check(status.is(206))
      .check(css("[id=%s]".format(constants.FormField.STATUS.name), "value").is("true"))
      .check(css("button:contains(Reject)").exists)
      .check(css("button:contains(Accept)").notExists)
    )
    .pause(Test.REQUEST_DELAY)

  val rejectBillOfLading: ScenarioBuilder = scenario("RejectBillOfLading")
    .exec(http("AcceptOrRejectAssetDocumentForm_GET")
      .get(session => routes.AssetController.acceptOrRejectAssetDocumentForm(session(Test.TEST_NEGOTIATION_ID).as[String], constants.File.Asset.BILL_OF_LADING).url)
      .check(status.is(200))
      .check(css("button:contains(Approve)").exists)
      .check(css("button:contains(Reject)").exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN))
    )
    .pause(Test.REQUEST_DELAY)
    .exec(http("AcceptOrRejectAssetDocument_POST")
      .post(routes.AssetController.acceptOrRejectAssetDocument().url)
      .formParamMap(Map(
        constants.FormField.NEGOTIATION_ID.name -> "${%s}".format(Test.TEST_NEGOTIATION_ID),
        constants.FormField.DOCUMENT_TYPE.name -> constants.File.Asset.BILL_OF_LADING,
        constants.FormField.STATUS.name -> false,
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
      .check(status.is(206))
      .check(css("[id=%s]".format(constants.FormField.STATUS.name), "value").is("true"))
      .check(css("button:contains(Accept)").exists)
      .check(css("button:contains(Reject)").notExists)
    )
    .pause(Test.REQUEST_DELAY)

  val sendAsset: ScenarioBuilder = scenario("SendAsset")
    .exec(http("SendAssetForm_GET")
      .get(session => routes.AssetController.sendForm(session(Test.TEST_NEGOTIATION_ID).as[String]).url)
      .check(status.is(200))
      .check(css("legend:contains(Confirm Trade)").exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN))
    )
    .pause(Test.REQUEST_DELAY)
    .exec(http("SendAsset_POST")
      .post(routes.AssetController.send().url)
      .formParamMap(Map(
        constants.FormField.NEGOTIATION_ID.name -> "${%s}".format(Test.TEST_NEGOTIATION_ID),
        constants.FormField.GAS.name -> "${%s}".format(Test.TEST_GAS),
        constants.FormField.PASSWORD.name -> "${%s}".format(Test.TEST_PASSWORD),
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
      .check(status.is(200))
      .check(substring("Trade Confirmed").exists)
    )
    .pause(Test.REQUEST_DELAY)

  val releaseAsset: ScenarioBuilder = scenario("ReleaseAsset")
    .exec(http("ReleaseAssetForm_GET")
      .get(session => routes.AssetController.releaseForm(session(Test.TEST_ASSET_ID).as[String]).url)
      .check(status.is(200))
      .check(css("legend:contains(Release Asset)").exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN))
    )
    .pause(Test.REQUEST_DELAY)
    .exec(http("ReleaseAsset_POST")
      .post(routes.AssetController.release().url)
      .formParamMap(Map(
        constants.FormField.ASSET_ID.name -> "${%s}".format(Test.TEST_ASSET_ID),
        constants.FormField.GAS.name -> "${%s}".format(Test.TEST_GAS),
        constants.FormField.PASSWORD.name -> "${%s}".format(Test.TEST_PASSWORD),
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
      .check(status.is(200))
      .check(substring("Asset Released").exists)
    )
    .pause(Test.REQUEST_DELAY)

  val redeemAsset: ScenarioBuilder = scenario("RedeemAsset")
    .exec(http("RedeemAssetForm_GET")
      .get(session => routes.AssetController.redeemForm(session(Test.TEST_ASSET_ID).as[String]).url)
      .check(status.is(200))
      .check(css("legend:contains(Redeem Asset)").exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN))
    )
    .pause(Test.REQUEST_DELAY)
    .exec(http("RedeemAsset_POST")
      .post(routes.AssetController.redeem().url)
      .formParamMap(Map(
        constants.FormField.ASSET_ID.name -> "${%s}".format(Test.TEST_ASSET_ID),
        constants.FormField.GAS.name -> "${%s}".format(Test.TEST_GAS),
        constants.FormField.PASSWORD.name -> "${%s}".format(Test.TEST_PASSWORD),
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
      .check(status.is(200))
      .check(substring("Trade Complete").exists)
    )


}
