package controllersTest

import constants.{Form, Test}
import controllers.routes
import feeders.JDBCFeeder._
import feeders._
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef.jdbcFeeder

import scala.util.Random

object addZoneControllerTest {

  val inviteZoneScenario: ScenarioBuilder = scenario("InviteZone")
    .feed(EmailAddressFeeder.emailAddressFeed)
    .exec(http("InviteZoneForm_GET")
      .get(routes.AddZoneController.inviteZoneForm().url)
      .check(status.is(200))
      .check(css("legend:contains(Invite Zone)").exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN))
    )
    .pause(Test.REQUEST_DELAY)
    .exec(http("InviteZone_POST")
      .post(routes.AddZoneController.inviteZone().url)
      .formParamMap(Map(
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN),
        constants.FormField.EMAIL_ADDRESS.name -> "${%s}".format(Test.TEST_EMAIL_ADDRESS)
      ))
      .check(status.is(200))
      .check(substring("Zone Invitation Sent").exists)
    )
    .pause(Test.REQUEST_DELAY)

  val acceptZoneInviteScenario: ScenarioBuilder = scenario("AcceptZoneInvite")
    .exec { session => session.set(Test.TEST_ZONE_INVITATION_ID, getZoneInvitationToken(session(Test.TEST_EMAIL_ADDRESS).as[String])) }
    .exec(http("AcceptZoneInvite")
      .get(session => routes.AddZoneController.acceptInvitation(session(Test.TEST_ZONE_INVITATION_ID).as[String]).url)
      .check(status.is(200))
    )

  val addZoneRequestScenario: ScenarioBuilder = scenario("AddZoneRequest")
    .exec(http("Add_Zone_Form_GET")
      .get(routes.AddZoneController.addZoneForm().url)
      .check(status.is(200))
      .check(css("legend:contains(%s)".format("Add Zone Details")).exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN)))
    .pause(Test.REQUEST_DELAY)
    .feed(NameFeeder.nameFeed)
    .feed(CurrencyFeeder.currencyFeed)
    .feed(AddressDataFeeder.addressDataFeed)
    .exec(http("Add_Zone_POST")
      .post(routes.AddZoneController.addZone().url)
      .formParamMap(Map(
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN),
        constants.FormField.NAME.name -> "${%s}".format(Test.TEST_NAME),
        constants.FormField.CURRENCY.name -> "${%s}".format(Test.TEST_CURRENCY),
        Form.ADDRESS_ADDRESS_LINE_1 -> "${%s}".format(Test.TEST_ADDRESS_LINE_1),
        Form.ADDRESS_ADDRESS_LINE_2 -> "${%s}".format(Test.TEST_ADDRESS_LINE_2),
        Form.ADDRESS_LANDMARK -> "${%s}".format(Test.TEST_LANDMARK),
        Form.ADDRESS_CITY -> "${%s}".format(Test.TEST_CITY),
        Form.ADDRESS_COUNTRY -> "${%s}".format(Test.TEST_COUNTRY),
        Form.ADDRESS_ZIP_CODE -> "${%s}".format(Test.TEST_ZIP_CODE),
        Form.ADDRESS_PHONE -> "${%s}".format(Test.TEST_PHONE)
      ))
      .check(substring("Zone KYC").exists)
      .check(status.is(206))
      .check(css("button:contains(Upload Identification)").exists)
      .check(css("button:contains(Upload Bank Account Detail)").exists)
    )
    .pause(Test.REQUEST_DELAY)
    .foreach(constants.File.ZONE_KYC_DOCUMENT_TYPES, Test.TEST_DOCUMENT_TYPE) {
      feed(ImageFeeder.imageFeed)
        .exec(http("ZoneKYC_Upload_" + "${%s}".format(Test.TEST_DOCUMENT_TYPE) + "_Form_GET")
          .get(session => routes.AddZoneController.userUploadZoneKYCForm(session(Test.TEST_DOCUMENT_TYPE).as[String]).url)
          .check(status.is(200))
          .check(css("button:contains(Browse)").exists)
          .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN))
        )
        .pause(Test.REQUEST_DELAY)
        .exec(http("ZoneKYC_Upload_" + "${%s}".format(Test.TEST_DOCUMENT_TYPE))
          .post(session => routes.AddZoneController.userUploadZoneKYC(session(Test.TEST_DOCUMENT_TYPE).as[String]).url)
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
          http("Store_ZoneKYC_" + "${%s}".format(Test.TEST_DOCUMENT_TYPE))
            .get(session => routes.AddZoneController.userStoreZoneKYC(session(Test.TEST_FILE_NAME).as[String], session(Test.TEST_DOCUMENT_TYPE).as[String]).url)
            .check(status.is(206))
            .check(substring("Zone KYC").exists)
        )
        .pause(Test.REQUEST_DELAY)
    }
    .pause(Test.REQUEST_DELAY)
    .exec(http("User_Review_Add_Zone_Request_Form")
      .get(routes.AddZoneController.userReviewAddZoneRequestForm().url)
      .check(status.is(200))
      .check(css("legend:contains(Review and Submit Zone details)").exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN))
    )
    .pause(Test.REQUEST_DELAY)
    .exec(http("User_Review_Add_Zone_Request")
      .post(routes.AddZoneController.userReviewAddZoneRequest().url)
      .formParamMap(Map(
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN),
        constants.FormField.PASSWORD.name -> "${%s}".format(Test.TEST_PASSWORD)
      ))
      .check(status.is(200))
      .check(substring("Zone details submitted for review").exists)
    )
    .pause(Test.REQUEST_DELAY)

  val verifyZoneScenario: ScenarioBuilder = scenario("VerifyZone")
    .foreach(constants.File.ZONE_KYC_DOCUMENT_TYPES, Test.TEST_DOCUMENT_TYPE) {
      exec(http("ZoneKYCUpdate" + "${%s}".format(Test.TEST_DOCUMENT_TYPE) + "StatusForm_GET")
        .get(session => routes.AddZoneController.updateZoneKYCDocumentStatusForm(session(Test.TEST_ZONE_ID).as[String], session(Test.TEST_DOCUMENT_TYPE).as[String]).url)
        .check(status.is(200))
        .check(css("[id=%s]".format(constants.FormField.ZONE_ID.name), "value").is("${%s}".format(Test.TEST_ZONE_ID)))
        .check(css("button:contains(Approve)").exists)
        .check(css("button:contains(Reject)").exists)
        .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN)))
        .pause(Test.REQUEST_DELAY)
        .exec(http("Zone_KYC_update_Status_" + "${%s}".format(Test.TEST_DOCUMENT_TYPE))
          .post(routes.AddZoneController.updateZoneKYCDocumentStatus().url)
          .formParamMap(Map(
            Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN),
            constants.FormField.ZONE_ID.name -> "${%s}".format(Test.TEST_ZONE_ID),
            constants.FormField.DOCUMENT_TYPE.name -> "${%s}".format(Test.TEST_DOCUMENT_TYPE),
            constants.FormField.STATUS.name -> true
          ))
          .check(status.is(206))
          .check(css("[id=%s]".format(constants.FormField.ZONE_ID.name), "value").is("${%s}".format(Test.TEST_ZONE_ID)))
          .check(css("button:contains(Reject)").exists)
        )
        .pause(Test.REQUEST_DELAY)
    }
    .exec(http("Verify_Zone_GET")
      .get(session => routes.AddZoneController.verifyZoneForm(session(Test.TEST_ZONE_ID).as[String]).url)
      .check(status.is(200))
      .check(css("legend:contains(Verify Zone)").exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN)))
    .pause(Test.REQUEST_DELAY)
    .feed(GasFeeder.gasFeed)
    .exec(http("Verify_Zone_POST")
      .post(routes.AddZoneController.verifyZone().url)
      .formParamMap(Map(
        constants.FormField.ZONE_ID.name -> "${%s}".format(Test.TEST_ZONE_ID),
        constants.FormField.GAS.name -> "${%s}".format(Test.TEST_GAS),
        constants.FormField.PASSWORD.name -> "${%s}".format(Test.TEST_MAIN_PASSWORD),
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
      .check(status.is(200))
      .check(substring("Zone approved successfully").exists)
    )
    .pause(Test.REQUEST_DELAY)


  val rejectVerifyZoneScenario: ScenarioBuilder = scenario("RejectVerifyZone")
    .exec(http("RejectVerifyZone_GET")
      .get(session => routes.AddZoneController.rejectVerifyZoneRequestForm(session(Test.TEST_ZONE_ID).as[String]).url)
      .check(status.is(200))
      .check(css("legend:contains(%s)".format(constants.Form.REJECT_VERIFY_ZONE_REQUEST.legend)).exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN)))
    .pause(Test.REQUEST_DELAY)
    .exec(http("RejectVerifyZone_POST")
      .post(routes.AddZoneController.rejectVerifyZoneRequest().url)
      .formParamMap(Map(
        constants.FormField.ZONE_ID.name -> "${%s}".format(Test.TEST_ZONE_ID),
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
      .check(status.is(200))
      .check(substring("SUCCESS VERIFY_ZONE_REJECTED").exists)
    )
    .pause(Test.REQUEST_DELAY)

}