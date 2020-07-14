package controllersTest

import constants.{Form, Test}
import controllers.routes
import feeders._
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef.jdbcFeeder
import java.util.Date

object addOrganizationControllerTest {

  val addOrganizationRequestScenario: ScenarioBuilder = scenario("AddOrganization")
    .feed(NameFeeder.nameFeed)
    .feed(EmailAddressFeeder.emailAddressFeed)
    .feed(AddressDataFeeder.addressDataFeed)
    .feed(DateFeeder.dateFeed)
    .exec(http("Add_Organization_GET")
      .get(routes.AddOrganizationController.addOrganizationForm().url)
      .check(status.is(200))
      .check(css("legend:contains(Register Organization)").exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN)))
    .pause(Test.REQUEST_DELAY)
    .exec(http("Add_Organization_POST")
      .post(routes.AddOrganizationController.addOrganization().url)
      .formParamMap(Map(
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN),
        constants.FormField.ZONE_ID.name -> "${%s}".format(Test.TEST_ZONE_ID),
        constants.FormField.NAME.name -> "${%s}".format(Test.TEST_NAME),
        constants.FormField.ABBREVIATION.name -> "${%s}".format(Test.TEST_NAME),
        constants.FormField.ESTABLISHMENT_DATE.name -> "${%s}".format(Test.TEST_DATE),
        constants.FormField.EMAIL_ADDRESS.name -> "${%s}".format(Test.TEST_EMAIL_ADDRESS),
        Test.REGISTERED_ADDRESS_LINE_1 -> "${%s}".format(Test.TEST_ADDRESS_LINE_1),
        Test.REGISTERED_ADDRESS_LINE_2 -> "${%s}".format(Test.TEST_ADDRESS_LINE_2),
        Test.REGISTERED_LANDMARK -> "${%s}".format(Test.TEST_LANDMARK),
        Test.REGISTERED_CITY -> "${%s}".format(Test.TEST_CITY),
        Test.REGISTERED_COUNTRY -> "${%s}".format(Test.TEST_COUNTRY),
        Test.REGISTERED_ZIP_CODE -> "${%s}".format(Test.TEST_ZIP_CODE),
        Test.REGISTERED_PHONE -> "${%s}".format(Test.TEST_PHONE),
        Test.POSTAL_ADDRESS_LINE_1 -> "${%s}".format(Test.TEST_ADDRESS_LINE_1),
        Test.POSTAL_ADDRESS_LINE_2 -> "${%s}".format(Test.TEST_ADDRESS_LINE_2),
        Test.POSTAL_LANDMARK -> "${%s}".format(Test.TEST_LANDMARK),
        Test.POSTAL_CITY -> "${%s}".format(Test.TEST_CITY),
        Test.POSTAL_COUNTRY -> "${%s}".format(Test.TEST_COUNTRY),
        Test.POSTAL_ZIP_CODE -> "${%s}".format(Test.TEST_ZIP_CODE),
        Test.POSTAL_PHONE -> "${%s}".format(Test.TEST_PHONE)
      ))
      .check(status.is(206))
      .check(substring("Organization KYC").exists)
      .check(css("button:contains(Upload ACRA)").exists)
      .check(css("button:contains(Upload Board Resolution)").exists)
    )
    .pause(Test.REQUEST_DELAY)
    .foreach(constants.File.ORGANIZATION_KYC_DOCUMENT_TYPES, Test.TEST_DOCUMENT_TYPE) {
      feed(ImageFeeder.imageFeed)
        .exec(http("Organization_KYC_Upload_" + "${%s}".format(Test.TEST_DOCUMENT_TYPE) + "_FORM")
          .get(session => routes.AddOrganizationController.userUploadOrganizationKYCForm(session(Test.TEST_DOCUMENT_TYPE).as[String]).url)
          .check(status.is(200))
          .check(css("button:contains(Browse)").exists)
          .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN))
        )
        .pause(Test.REQUEST_DELAY)
        .exec(http("Organization_KYC_Upload_" + "${%s}".format(Test.TEST_DOCUMENT_TYPE))
          .post(session => routes.AddOrganizationController.userUploadOrganizationKYC(session(Test.TEST_DOCUMENT_TYPE).as[String]).url)
          .formParamMap(Map(
            Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN),
            Form.RESUMABLE_CHUNK_NUMBER -> "1",
            Form.RESUMABLE_CHUNK_SIZE -> "1048576",
            Form.RESUMABLE_TOTAL_SIZE -> "${%s}".format(Test.TEST_FILE_SIZE),
            Form.RESUMABLE_IDENTIFIER -> "document",
            Form.RESUMABLE_FILE_NAME -> "${%s}".format(Test.TEST_FILE_NAME)))
          .bodyPart(RawFileBodyPart("file", Test.IMAGE_FILE_FEED + "${%s}".format(Test.TEST_FILE_NAME))
            .transferEncoding("binary")).asMultipartForm
          .check(status.is(200))
        )
        .exec(
          http("Store_Organization_KYC_" + "${%s}".format(Test.TEST_DOCUMENT_TYPE))
            .get(session => routes.AddOrganizationController.userStoreOrganizationKYC(session(Test.TEST_FILE_NAME).as[String], session(Test.TEST_DOCUMENT_TYPE).as[String]).url)
            .check(status.is(206))
            .check(substring("Organization KYC").exists)
        )
        .pause(Test.REQUEST_DELAY)
    }
    .exec(http("User_Review_Add_Organization_Request_Form_GET")
      .get(routes.AddOrganizationController.userReviewAddOrganizationRequestForm().url)
      .check(status.is(200))
      .check(css("legend:contains(Review & Submit Organization Details)").exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN))
    )
    .pause(Test.REQUEST_DELAY)
    .exec(http("User_Review_Add_Organization_Request_POST")
      .post(routes.AddOrganizationController.userReviewAddOrganizationRequest().url)
      .formParamMap(Map(
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN),
        Form.COMPLETION -> true
      ))
      .check(status.is(200))
      .check(substring("Organization details submitted for verification.").exists)
    )
    .pause(Test.REQUEST_DELAY)

  val verifyOrganizationScenario: ScenarioBuilder = scenario("VerifyOrganization")
    .foreach(constants.File.ORGANIZATION_KYC_DOCUMENT_TYPES, Test.TEST_DOCUMENT_TYPE) {
      exec(http("Organization_KYC_Update_Status" + "${%s}".format(Test.TEST_DOCUMENT_TYPE) + "_Form")
        .get(session => routes.AddOrganizationController.updateOrganizationKYCDocumentStatusForm(session(Test.TEST_ORGANIZATION_ID).as[String], session(Test.TEST_DOCUMENT_TYPE).as[String]).url)
        .check(status.is(200))
        .check(css("[id=%s]".format(constants.FormField.ORGANIZATION_ID.name), "value").is("${%s}".format(Test.TEST_ORGANIZATION_ID)))
        .check(css("button:contains(Approve)").exists)
        .check(css("button:contains(Reject)").exists)
        .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN))
      )
        .pause(Test.REQUEST_DELAY)
        .exec(http("Organization_KYC_Update_Status" + "${%s}".format(Test.TEST_DOCUMENT_TYPE))
          .post(routes.AddOrganizationController.updateOrganizationKYCDocumentStatus().url)
          .formParamMap(Map(
            Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN),
            constants.FormField.ORGANIZATION_ID.name -> "${%s}".format(Test.TEST_ORGANIZATION_ID),
            constants.FormField.DOCUMENT_TYPE.name -> "${%s}".format(Test.TEST_DOCUMENT_TYPE),
            constants.FormField.STATUS.name -> true
          ))
          .check(status.is(206))
          .check(css("[id=%s]".format(constants.FormField.ORGANIZATION_ID.name), "value").is("${%s}".format(Test.TEST_ORGANIZATION_ID)))
          .check(css("button:contains(Reject)").exists)
        )
        .pause(Test.REQUEST_DELAY)
    }
    .feed(GasFeeder.gasFeed)
    .exec(http("Verify_Organization_Form_GET")
      .get(session => routes.AddOrganizationController.acceptRequestForm(session(Test.TEST_ORGANIZATION_ID).as[String]).url)
      .check(status.is(200))
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN)))
    .pause(Test.REQUEST_DELAY)
    .exec(http("Verify_Organization_POST")
      .post(routes.AddOrganizationController.acceptRequest().url)
      .formParamMap(Map(
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN),
        constants.FormField.ORGANIZATION_ID.name -> "${%s}".format(Test.TEST_ORGANIZATION_ID),
        constants.FormField.GAS.name -> "${%s}".format(Test.TEST_GAS),
        constants.FormField.PASSWORD.name -> "${%s}".format(Test.TEST_ZONE_PASSWORD),
      ))
      .check(status.is(200))
      .check(substring("Organization Approved").exists)
    )
    .pause(Test.REQUEST_DELAY)

  val rejectVerifyOrganizationScenario: ScenarioBuilder = scenario("RejectVerifyOrganization")
    .exec(http("RejectVerifyOrganization_GET")
      .get(session => routes.AddOrganizationController.rejectRequestForm(session(Test.TEST_ORGANIZATION_ID).as[String]).url)
      .check(status.is(200))
      .check(css("legend:contains(%s)".format(constants.Form.REJECT_ORGANIZATION_REQUEST.legend)).exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN)))
    .pause(Test.REQUEST_DELAY)
    .exec(http("RejectVerifyOrganization_POST")
      .post(routes.AddOrganizationController.rejectRequest().url)
      .formParamMap(Map(
        constants.FormField.ORGANIZATION_ID.name -> "${%s}".format(Test.TEST_ORGANIZATION_ID),
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
      .check(status.is(200))
      .check(substring("SUCCESS VERIFY_ORGANIZATION_REQUEST_REJECTED").exists)
    )

}