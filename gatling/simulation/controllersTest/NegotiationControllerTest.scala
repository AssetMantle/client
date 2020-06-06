package controllersTest

import constants.{Form, Test}
import controllers.routes
import feeders._
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef.jdbcFeeder

import scala.util.Random

class NegotiationControllerTest extends Simulation {

  val scenarioBuilder: ScenarioBuilder = negotiationControllerTest.negotiationRequestScenario
  setUp(scenarioBuilder.inject(atOnceUsers(10))).protocols(http.baseUrl(Test.BASE_URL))
}

object negotiationControllerTest {

  val negotiationTermList = Seq("ASSET_DESCRIPTION", "PRICE", "QUANTITY", "ASSET_OTHER_DETAILS", "PAYMENT_TERMS", "DOCUMENT_LIST")

  val negotiationRequestScenario: ScenarioBuilder = scenario("NegotiationRequest")
    .exec(http("NegotiationRequestForm_GET")
      .get(routes.NegotiationController.requestForm().url)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN)))
    .pause(2)
    .exec(http("NegotiationRequest_POST")
      .post(routes.NegotiationController.request().url)
      .formParamMap(Map(
        constants.FormField.ASSET_ID.name -> "${%s}".format(Test.TEST_ASSET_ID),
        constants.FormField.COUNTER_PARTY.name -> "${%s}".format(Test.TEST_COUNTER_PARTY),
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
      .check(bodyString.saveAs("BODY"))
     .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN))
      .check(css("[name=%s]".format(Test.ID), "value").saveAs(Test.TEST_NEGOTIATION_ID))
    )
    .pause(2)
    .exec(http("PaymentTerms_POST")
      .post(routes.NegotiationController.paymentTerms().url)
      .formParamMap(Map(
        constants.FormField.ID.name -> "${%s}".format(Test.TEST_NEGOTIATION_ID),
        constants.FormField.ADVANCE_PERCENTAGE.name -> "50.0",
        Test.CREDIT_TENTATIVE_DATE -> "2019-11-11",
        Test.CREDIT_TENURE -> "",
        Test.CREDIT_REFRENCE -> "",
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN))
    )
    .pause(2)
    .exec(http("DocumentList_POST")
      .post(routes.NegotiationController.documentList().url)
      .formParamMap(Map(
        constants.FormField.ID.name -> "${%s}".format(Test.TEST_NEGOTIATION_ID),
        "DOCUMENT_LIST[0]" -> constants.File.Asset.BILL_OF_LADING,
        "DOCUMENT_LIST[1]" -> constants.File.Asset.COO,
        "DOCUMENT_LIST[2]" -> constants.File.Asset.COA,
        "DOCUMENT_LIST[3]" -> constants.File.Negotiation.BILL_OF_EXCHANGE,
        "DOCUMENT_LIST[4]" -> constants.File.Negotiation.INVOICE,
        constants.FormField.PHYSICAL_DOCUMENTS_HANDLED_VIA.name -> "BANK",
        constants.FormField.DOCUMENT_LIST_COMPLETED.name -> true,
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN))
    )
    .pause(2)
    .exec(http("ReviewNegotiationRequest_POST")
      .post(routes.NegotiationController.reviewRequest().url)
      .formParamMap(Map(
        constants.FormField.ID.name -> "${%s}".format(Test.TEST_NEGOTIATION_ID),
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
    )
    .pause(3)

  val acceptNegotiationRequest: ScenarioBuilder = scenario("AcceptNegotiationRequest")
    .exec(http("AcceptNegotiationRequestForm_GET")
      .get(session => routes.NegotiationController.acceptRequestForm(session(Test.TEST_NEGOTIATION_ID).as[String]).url)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN)))
    .pause(2)
    .exec(http("AcceptNegotiationRequest_POST")
      .post(routes.NegotiationController.acceptRequest().url)
      .formParamMap(Map(
        constants.FormField.ID.name -> "${%s}".format(Test.TEST_NEGOTIATION_ID),
        constants.FormField.GAS.name -> "${%s}".format(Test.TEST_GAS),
        constants.FormField.PASSWORD.name -> "${%s}".format(Test.TEST_PASSWORD),
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
    )
    .pause(2)

  val rejectNegotiationRequest: ScenarioBuilder = scenario("RejectNegotiationRequest")
    .exec(http("RejectNegotiationRequestForm_GET")
      .get(session => routes.NegotiationController.rejectRequestForm(session(Test.TEST_NEGOTIATION_ID).as[String]).url)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN)))
    .pause(2)
    .exec(http("AcceptNegotiationRequest_POST")
      .post(routes.NegotiationController.rejectRequest().url)
      .formParamMap(Map(
        constants.FormField.ID.name -> "${%s}".format(Test.TEST_NEGOTIATION_ID),
        constants.FormField.COMMENT.name -> "",
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
      .check(substring("Document List").exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN))
    )

  val acceptNegotiationTerms: ScenarioBuilder = scenario("AcceptNegotiationTerms")
    .foreach(negotiationTermList, "termType") {
      exec(http("AcceptOrRejectNegotiationTermForm_GET")
        .get(session => routes.NegotiationController.acceptOrRejectNegotiationTermsForm(session(Test.TEST_NEGOTIATION_ID).as[String], session("termType").as[String]).url)
        .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN))
      )
        .pause(1)
        .exec(http("AcceptNegotiationTerm_POST")
          .post(routes.NegotiationController.acceptOrRejectNegotiationTerms().url)
          .formParamMap(Map(
            constants.FormField.ID.name -> "${%s}".format(Test.TEST_NEGOTIATION_ID),
            constants.FormField.TERM_TYPE.name -> "${termType}",
            constants.FormField.STATUS.name -> true,
            Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
        )
        .pause(1)
    }

  val confirmAllNegotiationTerms: ScenarioBuilder = scenario("ConfirmAllNegotiationTerms")
    .exec(http("ConfirmAllNegotiationTermsForm_GET")
      .get(session => routes.NegotiationController.confirmAllNegotiationTermsForm(session(Test.TEST_NEGOTIATION_ID).as[String]).url)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN))
    )
    .pause(2)
    .exec(http("ConfirmAllNegotiationTerms_POST")
      .post(routes.NegotiationController.confirmAllNegotiationTerms().url)
      .formParamMap(Map(
        constants.FormField.NEGOTIATION_ID.name -> "${%s}".format(Test.TEST_NEGOTIATION_ID),
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
    )
    .pause(2)

  val uploadContract: ScenarioBuilder = scenario("UploadContract")
    .feed(ImageFeeder.imageFeed)
    .exec(http("UploadContractForm" + "_GET")
      .get(session => routes.FileController.uploadNegotiationForm(session(Test.TEST_NEGOTIATION_ID).as[String], constants.File.Negotiation.CONTRACT).url)
      .check(substring("Browse").exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN))
    )
    .pause(2)
    .exec(http("UploadContract" + "_POST")
      .post(session => routes.FileController.uploadNegotiation(constants.File.Negotiation.CONTRACT).url)
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
      http("Store_Contract")
        .get(session => routes.FileController.storeNegotiation(session(Test.TEST_FILE_NAME).as[String], constants.File.Negotiation.CONTRACT, session(Test.TEST_NEGOTIATION_ID).as[String]).url)
    )
    .pause(2)

  val updateContractSigned: ScenarioBuilder = scenario("UpdateContractSigned")
    .exec(http("UpdateContractSignedForm_GET")
      .get(session => routes.NegotiationController.updateContractSignedForm(session(Test.TEST_NEGOTIATION_ID).as[String]).url)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN))
    )
    .pause(2)
    .exec(http("UpdateContractSigned_POST")
      .post(routes.NegotiationController.updateContractSigned().url)
      .formParamMap(Map(
        constants.FormField.NEGOTIATION_ID.name -> "${%s}".format(Test.TEST_NEGOTIATION_ID),
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
    )
    .pause(2)

  val uploadAssetDocuments: ScenarioBuilder = scenario("UpdateContractSigned")
    .foreach(constants.File.ASSET_DOCUMENTS, "documentType") {
      feed(ImageFeeder.imageFeed)
        .exec(http("Asset_Document_Upload_" + "${documentType}" + "_FORM")
          .get(session => routes.FileController.uploadAssetForm(session("documentType").as[String], session(Test.TEST_ASSET_ID).as[String]).url)
          .check(substring("Browse").exists)
          .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN))
        )
        .pause(2)
        .exec(http("Asset_Document_Upload_" + "${documentType}")
          .post(session => routes.FileController.uploadAsset(session("documentType").as[String]).url)
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
          http("Store_Asset_Document_" + "${documentType}")
            .get(session => routes.FileController.storeAsset(session(Test.TEST_FILE_NAME).as[String], session("documentType").as[String],session(Test.TEST_NEGOTIATION_ID).as[String]).url)
        )
        .pause(2)
    }

  val addBillOfLading: ScenarioBuilder = scenario("AddBillOfLading")
    .feed(IssueAssetOBLFeeder.issueAssetOBLFeeder)
    .feed(AssetDetailFeeder.assetDetailFeed)
    .feed(ShippingDetailsFeeder.shippingDetailsFeeder)
    .exec(http("AddBillOfLadingForm_GET")
      .get(session => routes.AssetController.addBillOfLadingForm(session(Test.TEST_NEGOTIATION_ID).as[String]).url)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN)))
    .pause(2)
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
    )
    .pause(2)

  val uploadNegotiationDocuments: ScenarioBuilder = scenario("uploadNegotiationDocuments")
    .foreach(constants.File.NEGOTIATION_DOCUMENTS.filterNot(_ == constants.File.Negotiation.CONTRACT), "documentType") {
      feed(ImageFeeder2.imageFeed2)
        .exec(http("Negotiation_Document_Upload_" + "${documentType}" + "_FORM")
          .get(session => routes.FileController.uploadNegotiationForm(session("documentType").as[String], session(Test.TEST_NEGOTIATION_ID).as[String]).url)
          .check(substring("Browse").exists)
          .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN))
        )
        .pause(2)
        .exec(http("Negotiation_Document_Upload_" + "${documentType}")
          .post(session => routes.FileController.uploadNegotiation(session("documentType").as[String]).url)
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
          http("Store_Negotiation_Document_" + "${documentType}")
            .get(session => routes.FileController.storeNegotiation(session(Test.TEST_FILE_NAME).as[String], session("documentType").as[String],session(Test.TEST_NEGOTIATION_ID).as[String]).url)
        )
        .pause(2)
    }

  val addInvoice: ScenarioBuilder = scenario("AddInvoice")
    .feed(IssueAssetOBLFeeder.issueAssetOBLFeeder)
    .exec(http("AddInvoiceForm_GET")
      .get(session => routes.NegotiationController.addInvoiceForm(session(Test.TEST_NEGOTIATION_ID).as[String]).url)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN)))
    .pause(2)
    .exec(http("AddInvoice_POST")
      .post(routes.NegotiationController.addInvoice().url)
      .formParamMap(Map(
        constants.FormField.ID.name -> "${%s}".format(Test.TEST_NEGOTIATION_ID),
        constants.FormField.GAS.name -> "${%s}".format(Test.TEST_GAS),
        constants.FormField.PASSWORD.name -> "${%s}".format(Test.TEST_PASSWORD),
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
      .check(substring("Document List").exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN))
    )
    .pause(2)

  val addContract: ScenarioBuilder = scenario("AddContract")
    .feed(IssueAssetOBLFeeder.issueAssetOBLFeeder)
    .exec(http("AddContractForm_GET")
      .get(session => routes.NegotiationController.addContractForm(session(Test.TEST_NEGOTIATION_ID).as[String]).url)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN)))
    .pause(2)
    .exec(http("AddContract_POST")
      .post(routes.NegotiationController.addContract().url)
      .formParamMap(Map(
        constants.FormField.ID.name -> "${%s}".format(Test.TEST_NEGOTIATION_ID),
        constants.FormField.GAS.name -> "${%s}".format(Test.TEST_GAS),
        constants.FormField.PASSWORD.name -> "${%s}".format(Test.TEST_PASSWORD),
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
      .check(substring("Document List").exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN))
    )
    .pause(2)

  val acceptBillOfLading: ScenarioBuilder = scenario("AcceptBillOfLading")
    .exec(http("AcceptOrRejectAssetDocumentForm_GET")
      .get(session => routes.AssetController.acceptOrRejectAssetDocumentForm(session(Test.TEST_NEGOTIATION_ID).as[String], constants.File.Asset.BILL_OF_LADING).url)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN))
    )
    .pause(2)
    .exec(http("AcceptOrRejectAssetDocument_POST")
      .post(routes.AssetController.acceptOrRejectAssetDocument().url)
      .formParamMap(Map(
        constants.FormField.NEGOTIATION_ID.name -> "${%s}".format(Test.TEST_NEGOTIATION_ID),
        constants.FormField.DOCUMENT_TYPE.name -> constants.File.Asset.BILL_OF_LADING,
        constants.FormField.STATUS.name -> true,
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
    )
    .pause(2)

  val buyerConfirmNegotiation: ScenarioBuilder = scenario("BuyerConfirmNegotiation")
    .exec(http("BuyerConfirmForm_GET")
      .get(session => routes.NegotiationController.buyerConfirmForm(session(Test.TEST_NEGOTIATION_ID).as[String]).url)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN))
    )
    .pause(2)
    .exec(http("BuyerConfirm_POST")
      .post(routes.NegotiationController.buyerConfirm().url)
      .formParamMap(Map(
        constants.FormField.ID.name -> "${%s}".format(Test.TEST_NEGOTIATION_ID),
        constants.FormField.GAS.name -> "${%s}".format(Test.TEST_GAS),
        constants.FormField.PASSWORD.name -> "${%s}".format(Test.TEST_PASSWORD),
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
    )
    .pause(5)

  val sellerConfirmNegotiation: ScenarioBuilder = scenario("SellerConfirmNegotiation")
    .exec(http("SellerConfirmForm_GET")
      .get(session => routes.NegotiationController.sellerConfirmForm(session(Test.TEST_NEGOTIATION_ID).as[String]).url)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN))
    )
    .pause(2)
    .exec(http("SellerConfirm_POST")
      .post(routes.NegotiationController.sellerConfirm().url)
      .formParamMap(Map(
        constants.FormField.ID.name -> "${%s}".format(Test.TEST_NEGOTIATION_ID),
        constants.FormField.GAS.name -> "${%s}".format(Test.TEST_GAS),
        constants.FormField.PASSWORD.name -> "${%s}".format(Test.TEST_PASSWORD),
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
    )
    .pause(5)

  def getNegotiationStatus(query:String)={
    val sqlQueryFeeder = jdbcFeeder("jdbc:postgresql://18.136.170.155:5432/commit", "commit", "commit",
      s"""SELECT COALESCE((SELECT "id" FROM master."Negotiation" WHERE "id" = '$query'),'0') AS "id";""")
    sqlQueryFeeder.apply().next()("id").toString
  }

  def getNegotiationID(query:String)={
    val sqlQueryFeeder = jdbcFeeder("jdbc:postgresql://18.136.170.155:5432/commit", "commit", "commit",
      s"""SELECT COALESCE((SELECT "status" FROM master."Negotiation" WHERE "assetID" = '$query'),'0') AS "id";""")
    sqlQueryFeeder.apply().next()("id").toString
  }
}
