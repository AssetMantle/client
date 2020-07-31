package controllersTest

import constants.{Form, Test}
import controllers.routes
import controllersTest.AssetControllerTest.imageFeed
import feeders._
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._

import scala.util.Random


object NegotiationControllerTest {

  val negotiationTermList = Seq(constants.View.ASSET_DESCRIPTION, constants.View.PRICE, constants.View.QUANTITY, constants.View.ASSET_OTHER_DETAILS, constants.View.PAYMENT_TERMS, constants.View.DOCUMENT_LIST)

  val negotiationRequestScenario: ScenarioBuilder = scenario("NegotiationRequest")
    .exec(http("Negotiation_Request_Form_GET")
      .get(routes.NegotiationController.requestForm().url)
      .check(status.is(200))
      .check(css("legend:contains(Create Sales Quote)").exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN)))
    .pause(Test.REQUEST_DELAY)
    .exec(http("Negotiation_Request_POST")
      .post(routes.NegotiationController.request().url)
      .formParamMap(Map(
        constants.FormField.ASSET_ID.name -> "${%s}".format(Test.TEST_ASSET_ID),
        constants.FormField.COUNTER_PARTY.name -> "${%s}".format(Test.TEST_COUNTER_PARTY),
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
      .check(status.is(206))
      .check(css("legend:contains(Payment Terms)").exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN))
      .check(css("[name=%s]".format(Test.ID), "value").saveAs(Test.TEST_NEGOTIATION_ID))
    )
    .pause(Test.REQUEST_DELAY)

    val addPaymentTerms: ScenarioBuilder = scenario("AddPaymentTerms")
    .feed(PaymentTermsFeeder.paymentTermsFeed)
    .exec(http("Payment_Terms_Form_GET")
      .get(session => routes.NegotiationController.paymentTermsForm(session(Test.TEST_NEGOTIATION_ID).as[String]).url)
      .check(status.is(200))
      .check(css("legend:contains(Payment Terms)").exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN)))
    .pause(Test.REQUEST_DELAY)
    .exec(http("Payment_Terms_POST")
      .post(routes.NegotiationController.paymentTerms().url)
      .formParamMap(Map(
        constants.FormField.ID.name -> "${%s}".format(Test.TEST_NEGOTIATION_ID),
        constants.FormField.ADVANCE_PERCENTAGE.name ->"${%s}".format(Test.TEST_ADVANCE_PERCENTAGE),
        Test.CREDIT_TENTATIVE_DATE ->"${%s}".format(Test.TEST_TENTATIVE_DATE),
        Test.CREDIT_TENURE -> "${%s}".format(Test.TEST_TENURE),
        Test.CREDIT_REFERENCE -> "${%s}".format(Test.TEST_REFRENCE),
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
      .check(status.is(206))
      .check(css("legend:contains(Sales Quote Documents List)").exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN))
    )
    .pause(Test.REQUEST_DELAY)

  val addDocumentList: ScenarioBuilder = scenario("AddDocumentList")
    .exec(http("Document_List_Form_GET")
      .get(session => routes.NegotiationController.documentListForm(session(Test.TEST_NEGOTIATION_ID).as[String]).url)
      .check(status.is(200))
      .check(css("legend:contains(Sales Quote Documents List)").exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN)))
    .pause(Test.REQUEST_DELAY)
    .exec(http("Document_List_POST")
      .post(routes.NegotiationController.documentList().url)
      .formParamMap(Map(
        constants.FormField.ID.name -> "${%s}".format(Test.TEST_NEGOTIATION_ID),
        "DOCUMENT_LIST[0]" -> constants.File.Asset.BILL_OF_LADING,
        "DOCUMENT_LIST[1]" -> constants.File.Asset.COO,
        "DOCUMENT_LIST[2]" -> constants.File.Asset.COA,
        "DOCUMENT_LIST[3]" -> constants.File.Negotiation.BILL_OF_EXCHANGE,
        "DOCUMENT_LIST[4]" -> constants.File.Negotiation.INVOICE,
        constants.FormField.PHYSICAL_DOCUMENTS_HANDLED_VIA.name -> Random.shuffle(constants.SelectFieldOptions.PHYSICAL_DOCUMENTS_HANDLED_VIA).head,
        constants.FormField.DOCUMENT_LIST_COMPLETED.name -> true,
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
      .check(status.is(206))
      .check(css("legend:contains(Review Sales Quote)").exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN))
    )
    .pause(Test.REQUEST_DELAY)

  val reviewNegotiationRequest: ScenarioBuilder = scenario("ReviewNegotiationRequest")
    .exec(http("Review_Negotiation_Request_Form_GET")
      .get(session => routes.NegotiationController.reviewRequestForm(session(Test.TEST_NEGOTIATION_ID).as[String]).url)
      .check(status.is(200))
      .check(css("legend:contains(Review Sales Quote)").exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN)))
    .pause(Test.REQUEST_DELAY)
    .exec(http("Review_Negotiation_Request_POST")
      .post(routes.NegotiationController.reviewRequest().url)
      .formParamMap(Map(
        constants.FormField.ID.name -> "${%s}".format(Test.TEST_NEGOTIATION_ID),
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
      .check(status.is(200))
      .check(substring("Sales Quote Submitted").exists)
    )
    .pause(Test.REQUEST_DELAY)

  val acceptNegotiationRequest: ScenarioBuilder = scenario("AcceptNegotiationRequest")
    .feed(GasFeeder.gasFeed)
    .exec(http("Accept_Negotiation_Request_Form_GET")
      .get(session => routes.NegotiationController.acceptRequestForm(session(Test.TEST_NEGOTIATION_ID).as[String]).url)
      .check(status.is(200))
      .check(css("legend:contains(Accept Sales Quote)").exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN)))
    .pause(Test.REQUEST_DELAY)
    .exec(http("Accept_Negotiation_Request_POST")
      .post(routes.NegotiationController.acceptRequest().url)
      .formParamMap(Map(
        constants.FormField.ID.name -> "${%s}".format(Test.TEST_NEGOTIATION_ID),
        constants.FormField.GAS.name -> "${%s}".format(Test.TEST_GAS),
        constants.FormField.PASSWORD.name -> "${%s}".format(Test.TEST_PASSWORD),
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
      .check(status.is(200))
      .check(substring("Sales Quote Accepted").exists)
    )
    .pause(Test.REQUEST_DELAY)

  val rejectNegotiationRequest: ScenarioBuilder = scenario("RejectNegotiationRequest")
    .exec(http("Reject_Negotiation_Request_Form_GET")
      .get(session => routes.NegotiationController.rejectRequestForm(session(Test.TEST_NEGOTIATION_ID).as[String]).url)
      .check(status.is(200))
      .check(css("legend:contains(Reject Sales Quote)").exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN)))
    .pause(Test.REQUEST_DELAY)
    .exec(http("Reject_Negotiation_Request_POST")
      .post(routes.NegotiationController.rejectRequest().url)
      .formParamMap(Map(
        constants.FormField.ID.name -> "${%s}".format(Test.TEST_NEGOTIATION_ID),
        constants.FormField.COMMENT.name -> "",
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
      .check(status.is(200))
      .check(substring("Sales Quote Request Rejected").exists)
    )
    .pause(Test.REQUEST_DELAY)

  val updateAssetTerms: ScenarioBuilder = scenario("UpdateAssetTerms")
    .exec(http("Update_Asset_Terms_Form_GET")
      .get(session => routes.NegotiationController.updateAssetTermsForm(session(Test.TEST_NEGOTIATION_ID).as[String]).url)
      .check(css("legend:contains(Update Commodity Details)").exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN)))
    .pause(Test.REQUEST_DELAY)
    .exec(http("Update_Asset_Terms_POST")
      .post(routes.NegotiationController.updateAssetTerms().url)
      .formParamMap(Map(
        constants.FormField.ID.name -> "${%s}".format(Test.TEST_NEGOTIATION_ID),
        constants.FormField.ASSET_DESCRIPTION.name -> "${%s}".format(Test.TEST_ASSET_DESCRIPTION),
        constants.FormField.ASSET_PRICE_PER_UNIT.name -> "${%s}".format(Test.TEST_ASSET_PRICE_PER_UNIT),
        constants.FormField.ASSET_QUANTITY.name -> "${%s}".format(Test.TEST_ASSET_QUANTITY),
        constants.FormField.QUANTITY_UNIT.name -> "${%s}".format(Test.TEST_QUANTITY_UNIT),
        constants.FormField.GAS.name -> "${%s}".format(Test.TEST_GAS),
        constants.FormField.PASSWORD.name -> "${%s}".format(Test.TEST_PASSWORD),
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
      .check(substring("Commodity Details Updated").exists)
    )
    .pause(Test.REQUEST_DELAY)

  val updateAssetOtherDetails: ScenarioBuilder = scenario("UpdateAssetOtherDetails")
    .exec(http("Update_Asset_Other_Details_Form_GET")
      .get(session => routes.NegotiationController.updateAssetOtherDetailsForm(session(Test.TEST_NEGOTIATION_ID).as[String]).url)
      .check(css("legend:contains(Update Shipping Details)").exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN)))
    .pause(Test.REQUEST_DELAY)
    .exec(http("Update_Asset_Other_Details_POST")
      .post(routes.NegotiationController.updateAssetOtherDetails().url)
      .formParamMap(Map(
        constants.FormField.ID.name -> "${%s}".format(Test.TEST_NEGOTIATION_ID),
        constants.FormField.SHIPPING_PERIOD.name -> "${%s}".format(Test.TEST_SHIPPING_PERIOD),
        constants.FormField.PORT_OF_LOADING.name -> "${%s}".format(Test.TEST_PORT_OF_LOADING),
        constants.FormField.PORT_OF_DISCHARGE.name -> "${%s}".format(Test.TEST_PORT_OF_DISCHARGE),
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
      .check(substring("Commodity Details Updated").exists)
    )
    .pause(Test.REQUEST_DELAY)

  val updatePaymentTerms: ScenarioBuilder = scenario("updatePaymentTerms")
    .exec(http("Update_Payment_Terms_Form_GET")
      .get(session => routes.NegotiationController.updatePaymentTermsForm(session(Test.TEST_NEGOTIATION_ID).as[String]).url)
      .check(css("legend:contains(Update Payment Terms)").exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN)))
    .pause(Test.REQUEST_DELAY)
    .exec(http("Update_Payment_Terms_POST")
      .post(routes.NegotiationController.updatePaymentTerms().url)
      .formParamMap(Map(
        constants.FormField.ID.name -> "${%s}".format(Test.TEST_NEGOTIATION_ID),
        constants.FormField.ADVANCE_PERCENTAGE.name ->"${%s}".format(Test.TEST_ADVANCE_PERCENTAGE),
        Test.CREDIT_TENTATIVE_DATE ->"${%s}".format(Test.TEST_TENTATIVE_DATE),
        Test.CREDIT_TENURE -> "${%s}".format(Test.TEST_TENURE),
        Test.CREDIT_REFERENCE -> "${%s}".format(Test.TEST_REFRENCE),
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
      .check(substring("Payment Terms Updated").exists)
    )
    .pause(Test.REQUEST_DELAY)

  val updateDocumentList: ScenarioBuilder = scenario("updateDocumentList")
    .exec(http("Update_Document_List_Form_GET")
      .get(session => routes.NegotiationController.updateDocumentListForm(session(Test.TEST_NEGOTIATION_ID).as[String]).url)
      .check(css("legend:contains(Update Trade Documents)").exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN)))
    .pause(Test.REQUEST_DELAY)
    .exec(http("Update_Document_List_POST")
      .post(routes.NegotiationController.updateDocumentList().url)
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
      .check(substring("Trade Documents Updated").exists)
    )
    .pause(Test.REQUEST_DELAY)

  val acceptNegotiationTerms: ScenarioBuilder = scenario("AcceptNegotiationTerms")
    .foreach(negotiationTermList, "termType") {
      exec(http("Accept_Or_Reject_Negotiation_Term_Form_GET")
        .get(session => routes.NegotiationController.acceptOrRejectNegotiationTermsForm(session(Test.TEST_NEGOTIATION_ID).as[String], session("termType").as[String]).url)
        .check(status.is(200))
        .check(css("[id=%s]".format(constants.FormField.ID.name), "value").is("${%s}".format(Test.TEST_NEGOTIATION_ID)))
        .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN))
      )
        .pause(Test.REQUEST_DELAY)
        .exec(http("Accept_Negotiation_Term_POST")
          .post(routes.NegotiationController.acceptOrRejectNegotiationTerms().url)
          .formParamMap(Map(
            constants.FormField.ID.name -> "${%s}".format(Test.TEST_NEGOTIATION_ID),
            constants.FormField.TERM_TYPE.name -> "${termType}",
            constants.FormField.STATUS.name -> true,
            Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
          .check(status.is(206))
          .check(css("[id=%s]".format(constants.FormField.ID.name), "value").is("${%s}".format(Test.TEST_NEGOTIATION_ID)))
          .check(css("[id=%s]".format(constants.FormField.STATUS.name), "value").is("true"))
        )
        .pause(Test.REQUEST_DELAY)
    }

  val confirmAllNegotiationTerms: ScenarioBuilder = scenario("ConfirmAllNegotiationTerms")
    .exec(http("Confirm_All_Negotiation_Terms_Form_GET")
      .get(session => routes.NegotiationController.confirmAllNegotiationTermsForm(session(Test.TEST_NEGOTIATION_ID).as[String]).url)
      .check(status.is(200))
      .check(css("legend:contains(Trade Terms)").exists)
      .check(substring("Accept all terms?").exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN))
    )
    .pause(Test.REQUEST_DELAY)
    .exec(http("Confirm_All_Negotiation_Terms_POST")
      .post(routes.NegotiationController.confirmAllNegotiationTerms().url)
      .formParamMap(Map(
        constants.FormField.NEGOTIATION_ID.name -> "${%s}".format(Test.TEST_NEGOTIATION_ID),
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
      .check(status.is(200))
    )
    .pause(Test.REQUEST_DELAY)


  val uploadContract: ScenarioBuilder = scenario("UploadContract")
    .exec(imageFeed)
    .exec(http("Upload_Contract_Form" + "_GET")
      .get(session => routes.FileController.uploadNegotiationForm(session(Test.TEST_NEGOTIATION_ID).as[String], constants.File.Negotiation.CONTRACT).url)
      .check(status.is(200))
      .check(css("button:contains(Browse)").exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN))
    )
    .pause(Test.REQUEST_DELAY)
    .exec(http("Upload_Contract" + "_POST")
      .post(session => routes.FileController.uploadNegotiation(constants.File.Negotiation.CONTRACT).url)
      .formParamMap(Map(
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN),
        Form.RESUMABLE_CHUNK_NUMBER -> "1",
        Form.RESUMABLE_CHUNK_SIZE -> "1048576",
        Form.RESUMABLE_TOTAL_SIZE -> "${%s}".format(Test.TEST_FILE_SIZE),
        Form.RESUMABLE_IDENTIFIER -> "document",
        Form.RESUMABLE_FILE_NAME -> "${%s}".format(Test.TEST_FILE_NAME)))
      .bodyPart(RawFileBodyPart("file", Test.TEST_IMAGE_PATH + "${%s}".format(Test.TEST_FILE_NAME))
        .transferEncoding("binary")).asMultipartForm
      .check(status.is(200))
    )
    .exec(
      http("Store_Contract")
        .get(session => routes.FileController.storeNegotiation(session(Test.TEST_FILE_NAME).as[String], constants.File.Negotiation.CONTRACT, session(Test.TEST_NEGOTIATION_ID).as[String]).url)
        .check(status.is(206))
        .check(css("legend:contains(Add Contract Details)").exists)
    )
    .pause(Test.REQUEST_DELAY)

  val updateContract: ScenarioBuilder = scenario("UpdateContract")
    .exec(imageFeed)
    .exec(http("Update_Contract_Form" + "_GET")
      .get(session => routes.FileController.updateNegotiationForm(session(Test.TEST_NEGOTIATION_ID).as[String], constants.File.Negotiation.CONTRACT).url)
      .check(status.is(200))
      .check(css("button:contains(Browse)").exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN))
    )
    .pause(Test.REQUEST_DELAY)
    .exec(http("Upload_Contract" + "_POST")
      .post(session => routes.FileController.uploadNegotiation(constants.File.Negotiation.CONTRACT).url)
      .formParamMap(Map(
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN),
        Form.RESUMABLE_CHUNK_NUMBER -> "1",
        Form.RESUMABLE_CHUNK_SIZE -> "1048576",
        Form.RESUMABLE_TOTAL_SIZE -> "${%s}".format(Test.TEST_FILE_SIZE),
        Form.RESUMABLE_IDENTIFIER -> "document",
        Form.RESUMABLE_FILE_NAME -> "${%s}".format(Test.TEST_FILE_NAME)))
      .bodyPart(RawFileBodyPart("file", Test.TEST_IMAGE_PATH + "${%s}".format(Test.TEST_FILE_NAME))
        .transferEncoding("binary")).asMultipartForm
      .check(status.is(200))
    )
    .exec(
      http("Update_Contract")
        .get(session => routes.FileController.updateNegotiation(session(Test.TEST_FILE_NAME).as[String], constants.File.Negotiation.CONTRACT, session(Test.TEST_NEGOTIATION_ID).as[String]).url)
        .check(status.is(206))
        .check(css("legend:contains(Add Contract Details)").exists)
    )
    .pause(Test.REQUEST_DELAY)

  val addContract: ScenarioBuilder = scenario("AddContract")
    .feed(ContractDetails.contractDetailsFeed)
    .exec(http("Add_Contract_Form_GET")
      .get(session => routes.NegotiationController.addContractForm(session(Test.TEST_NEGOTIATION_ID).as[String]).url)
      .check(css("legend:contains(Add Contract Details)").exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN))
    )
    .pause(Test.REQUEST_DELAY)
    .exec(http("Add_Contract_POST")
      .post(routes.NegotiationController.addContract().url)
      .formParamMap(Map(
        constants.FormField.NEGOTIATION_ID.name -> "${%s}".format(Test.TEST_NEGOTIATION_ID),
        constants.FormField.CONTRACT_NUMBER.name -> "${%s}".format(Test.TEST_CONTRACT_NUMBER),
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
      .check(substring("Upload Documents").exists)
      .check(substring("Update Contract").exists)
    )
    .pause(Test.REQUEST_DELAY)

  val addInvoice: ScenarioBuilder = scenario("AddInvoice")
    .feed(InvoiceDetails.invoiceDetailsFeed)
    .exec(http("Add_Invoice_Form_GET")
      .get(session => routes.NegotiationController.addInvoiceForm(session(Test.TEST_NEGOTIATION_ID).as[String]).url)
      .check(css("legend:contains(Add Invoice)").exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN))
    )
    .pause(Test.REQUEST_DELAY)
    .exec(http("Add_Invoice_POST")
      .post(routes.NegotiationController.addInvoice().url)
      .formParamMap(Map(
        constants.FormField.NEGOTIATION_ID.name -> "${%s}".format(Test.TEST_NEGOTIATION_ID),
        constants.FormField.INVOICE_NUMBER.name -> "${%s}".format(Test.TEST_INVOICE_NUMBER),
        constants.FormField.INVOICE_AMOUNT.name -> "${%s}".format(Test.TEST_INVOICE_AMOUNT),
        constants.FormField.INVOICE_DATE.name -> "${%s}".format(Test.TEST_INVOICE_DATE),
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
      .check(substring("Commodity Documents").exists)
      .check(substring("Trade Documents").exists)
    )
    .pause(Test.REQUEST_DELAY)

  val updateContractSigned: ScenarioBuilder = scenario("UpdateContractSigned")
    .exec(http("Update_Contract_Signed_Form_GET")
      .get(session => routes.NegotiationController.updateContractSignedForm(session(Test.TEST_NEGOTIATION_ID).as[String]).url)
      .check(status.is(200))
      .check(css("legend:contains(Contract Signed)").exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN))
    )
    .pause(Test.REQUEST_DELAY)
    .exec(http("Update_Contract_Signed_POST")
      .post(routes.NegotiationController.updateContractSigned().url)
      .formParamMap(Map(
        constants.FormField.NEGOTIATION_ID.name -> "${%s}".format(Test.TEST_NEGOTIATION_ID),
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
      .check(status.is(200))
    )
    .pause(Test.REQUEST_DELAY)

  val uploadNegotiationDocuments: ScenarioBuilder = scenario("UploadNegotiationDocuments")
    .foreach(constants.File.NEGOTIATION_DOCUMENTS.filterNot(_ == constants.File.Negotiation.CONTRACT), Test.TEST_DOCUMENT_TYPE) {
      exec(imageFeed)
        .exec(http("Negotiation_Document_Upload_" + "${%s}".format(Test.TEST_DOCUMENT_TYPE) + "_FORM")
          .get(session => routes.FileController.uploadNegotiationForm(session(Test.TEST_DOCUMENT_TYPE).as[String], session(Test.TEST_NEGOTIATION_ID).as[String]).url)
          .check(status.is(200))
          .check(css("button:contains(Browse)").exists)
          .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN))
        )
        .pause(Test.REQUEST_DELAY)
        .exec(http("Negotiation_Document_Upload_" + "${%s}".format(Test.TEST_DOCUMENT_TYPE))
          .post(session => routes.FileController.uploadNegotiation(session(Test.TEST_DOCUMENT_TYPE).as[String]).url)
          .formParamMap(Map(
            Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN),
            Form.RESUMABLE_CHUNK_NUMBER -> "1",
            Form.RESUMABLE_CHUNK_SIZE -> "1048576",
            Form.RESUMABLE_TOTAL_SIZE -> "${%s}".format(Test.TEST_FILE_SIZE),
            Form.RESUMABLE_IDENTIFIER -> "document",
            Form.RESUMABLE_FILE_NAME -> "${%s}".format(Test.TEST_FILE_NAME)))
          .bodyPart(RawFileBodyPart("file", Test.TEST_IMAGE_PATH + "${%s}".format(Test.TEST_FILE_NAME))
            .transferEncoding("binary")).asMultipartForm
          .check(status.is(200))
        )
        .exec(
          http("Store_Negotiation_Document_" + "${%s}".format(Test.TEST_DOCUMENT_TYPE))
            .get(session => routes.FileController.storeNegotiation(session(Test.TEST_FILE_NAME).as[String], session(Test.TEST_DOCUMENT_TYPE).as[String], session(Test.TEST_NEGOTIATION_ID).as[String]).url)
            .check(status.is(206))
        )
        .pause(Test.REQUEST_DELAY)
    }

  val updateNegotiationDocuments: ScenarioBuilder = scenario("UpdateNegotiationDocuments")
    .foreach(constants.File.NEGOTIATION_DOCUMENTS.filterNot(_ == constants.File.Negotiation.CONTRACT), Test.TEST_DOCUMENT_TYPE) {
      exec(imageFeed)
        .exec(http("Negotiation_Document_Update_" + "${%s}".format(Test.TEST_DOCUMENT_TYPE) + "_FORM")
          .get(session => routes.FileController.updateNegotiationForm(session(Test.TEST_DOCUMENT_TYPE).as[String], session(Test.TEST_NEGOTIATION_ID).as[String]).url)
          .check(status.is(200))
          .check(css("button:contains(Browse)").exists)
          .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN))
        )
        .pause(Test.REQUEST_DELAY)
        .exec(http("Negotiation_Document_Upload_" + "${%s}".format(Test.TEST_DOCUMENT_TYPE))
          .post(session => routes.FileController.uploadNegotiation(session(Test.TEST_DOCUMENT_TYPE).as[String]).url)
          .formParamMap(Map(
            Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN),
            Form.RESUMABLE_CHUNK_NUMBER -> "1",
            Form.RESUMABLE_CHUNK_SIZE -> "1048576",
            Form.RESUMABLE_TOTAL_SIZE -> "${%s}".format(Test.TEST_FILE_SIZE),
            Form.RESUMABLE_IDENTIFIER -> "document",
            Form.RESUMABLE_FILE_NAME -> "${%s}".format(Test.TEST_FILE_NAME)))
          .bodyPart(RawFileBodyPart("file", Test.TEST_IMAGE_PATH + "${%s}".format(Test.TEST_FILE_NAME))
            .transferEncoding("binary")).asMultipartForm
          .check(status.is(200))
        )
        .exec(
          http("Update_Negotiation_Document_" + "${%s}".format(Test.TEST_DOCUMENT_TYPE))
            .get(session => routes.FileController.updateNegotiation(session(Test.TEST_FILE_NAME).as[String], session(Test.TEST_DOCUMENT_TYPE).as[String], session(Test.TEST_NEGOTIATION_ID).as[String]).url)
            .check(status.is(206))
        )
        .pause(Test.REQUEST_DELAY)
    }

  def buyerConfirmNegotiation(state:String): ScenarioBuilder = scenario("BuyerConfirmNegotiation")
    .exec(http("Buyer_Confirm_Form_GET")
      .get(session => routes.NegotiationController.buyerConfirmForm(session(Test.TEST_NEGOTIATION_ID).as[String]).url)
      .check(status.is(200))
      .check(css("legend:contains(Confirm Trade Documents)").exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN))
    )
    .pause(Test.REQUEST_DELAY)
    .exec(http("Buyer_Confirm_POST")
      .post(routes.NegotiationController.buyerConfirm().url)
      .formParamMap(Map(
        constants.FormField.ID.name -> "${%s}".format(Test.TEST_NEGOTIATION_ID),
        constants.FormField.GAS.name -> "${%s}".format(Test.TEST_GAS),
        constants.FormField.PASSWORD.name -> "${%s}".format(Test.TEST_PASSWORD),
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
        .check{
          state match{
            case constants.Test.TRADE_DOCUMENTS_NOT_UPLOADED=> status.is(500)
            case constants.Test.BILL_OF_LADING_NOT_ACCEPTED=>  status.is(500)
            case constants.Test.PRECONDITIONS_SATISFIED=> status.is(200)
          }
        }
      .check{
        state match{
          case constants.Test.TRADE_DOCUMENTS_NOT_UPLOADED=> substring("RESPONSE.FAILURE.ALL_TRADE_DOCUMENTS_NOT_UPLOADED").exists
          case constants.Test.BILL_OF_LADING_NOT_ACCEPTED=>  substring("Bill Of Lading Verification Status Pending").exists
          case constants.Test.PRECONDITIONS_SATISFIED=> substring("Trade Documents Confirmed").exists
        }
      }
    )
    .pause(Test.REQUEST_DELAY)

  def sellerConfirmNegotiation(state:String): ScenarioBuilder = scenario("SellerConfirmNegotiation")
    .exec(http("Seller_ConfirmForm_GET")
      .get(session => routes.NegotiationController.sellerConfirmForm(session(Test.TEST_NEGOTIATION_ID).as[String]).url)
      .check(status.is(200))
      .check(css("legend:contains(Confirm Trade Documents)").exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN))
    )
    .pause(Test.REQUEST_DELAY)
    .exec(http("Seller_Confirm_POST")
      .post(routes.NegotiationController.sellerConfirm().url)
      .formParamMap(Map(
        constants.FormField.ID.name -> "${%s}".format(Test.TEST_NEGOTIATION_ID),
        constants.FormField.GAS.name -> "${%s}".format(Test.TEST_GAS),
        constants.FormField.PASSWORD.name -> "${%s}".format(Test.TEST_PASSWORD),
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
      .check{
        state match{
          case constants.Test.TRADE_DOCUMENTS_NOT_UPLOADED=> status.is(500)
          case constants.Test.BILL_OF_LADING_NOT_ACCEPTED=>  status.is(500)
          case constants.Test.PRECONDITIONS_SATISFIED=> status.is(200)
        }
      }
      .check{
        state match{
          case constants.Test.TRADE_DOCUMENTS_NOT_UPLOADED=> substring("RESPONSE.FAILURE.ALL_TRADE_DOCUMENTS_NOT_UPLOADED").exists
          case constants.Test.BILL_OF_LADING_NOT_ACCEPTED=>  substring("Bill Of Lading Verification Status Pending").exists
          case constants.Test.PRECONDITIONS_SATISFIED=> substring("Trade Documents Confirmed").exists
        }
      }
    )
    .pause(Test.REQUEST_DELAY)

}
