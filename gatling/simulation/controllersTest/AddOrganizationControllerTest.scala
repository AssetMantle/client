package controllersTest

import constants.{Form, Test}
import controllers.routes
import feeders._
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef.jdbcFeeder
import java.util.Date

class AddOrganizationControllerTest extends Simulation {

  val scenarioBuilder: ScenarioBuilder = addOrganizationControllerTest.blockchainAddOrganizationScenario
  setUp(scenarioBuilder.inject(atOnceUsers(1))).protocols(http.baseUrl(Test.BASE_URL))
}

object addOrganizationControllerTest {

  val organizationKYCs=constants.File.ORGANIZATION_KYC_DOCUMENT_TYPES

  val addOrganizationRequestScenario: ScenarioBuilder = scenario("AddOrganization")
    .feed(NameFeeder.nameFeed)
    .feed(EmailAddressFeeder.emailAddressFeed)
    .feed(AddressDataFeeder.addressDataFeed)
    .exec(http("Add_Organization_GET")
      .get(routes.AddOrganizationController.addOrganizationForm().url)
      .check(css("legend:contains(%s)".format("Add Organization")).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("Add_Organization_POST")
      .post(routes.AddOrganizationController.addOrganization().url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        Form.ZONE_ID -> "${%s}".format(Test.TEST_ZONE_ID),
        Form.NAME -> "${%s}".format(Test.TEST_NAME),
        Form.ABBREVIATION -> "${%s}".format(Test.TEST_NAME),
        Form.ESTABLISHMENT_DATE -> "2019-11-11",
        Form.EMAIL_ADDRESS -> "${%s}".format(Test.TEST_EMAIL_ADDRESS),
        Form.REGISTERED_ADDRESS_LINE_1 -> "${%s}".format(Test.TEST_ADDRESS_LINE_1),
        Form.REGISTERED_ADDRESS_LINE_2 -> "${%s}".format(Test.TEST_ADDRESS_LINE_2),
        Form.REGISTERED_LANDMARK -> "${%s}".format(Test.TEST_LANDMARK),
        Form.REGISTERED_CITY -> "${%s}".format(Test.TEST_CITY),
        Form.REGISTERED_COUNTRY -> "${%s}".format(Test.TEST_COUNTRY),
        Form.REGISTERED_ZIP_CODE -> "${%s}".format(Test.TEST_ZIP_CODE),
        Form.REGISTERED_PHONE ->"${%s}".format(Test.TEST_PHONE),
        Form.POSTAL_ADDRESS_LINE_1 ->"${%s}".format(Test.TEST_ADDRESS_LINE_1),
        Form.POSTAL_ADDRESS_LINE_2 ->"${%s}".format(Test.TEST_ADDRESS_LINE_2),
        Form.POSTAL_LANDMARK ->"${%s}".format(Test.TEST_LANDMARK),
        Form.POSTAL_CITY -> "${%s}".format(Test.TEST_CITY),
        Form.POSTAL_COUNTRY -> "${%s}".format(Test.TEST_COUNTRY),
        Form.POSTAL_ZIP_CODE -> "${%s}".format(Test.TEST_ZIP_CODE),
        Form.POSTAL_PHONE -> "${%s}".format(Test.TEST_PHONE)
        ))
      .check(substring("Organization KYC Files").exists)
    )
    .pause(2)
    .foreach(organizationKYCs,"documentType"){
      feed(ImageFeeder.imageFeed)
        .exec(http("Organization_KYC_Upload_"+"${documentType}"+"_FORM")
          .get(session=> routes.AddOrganizationController.userUploadOrganizationKYCForm(session("documentType").as[String]).url )
          .check(substring("BROWSE").exists)
          .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN))
        )
        .pause(2)
        .exec(http("Organization_KYC_Upload_"+"${documentType}")
          .post(session=> routes.AddOrganizationController.userUploadOrganizationKYC(session("documentType").as[String]).url)
          .formParamMap(Map(
            Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
            Form.RESUMABLE_CHUNK_NUMBER -> "1",
            Form.RESUMABLE_CHUNK_SIZE  -> "1048576",
            Form.RESUMABLE_TOTAL_SIZE -> "${%s}".format(Test.TEST_FILE_SIZE),
            Form.RESUMABLE_IDENTIFIER -> "document",
            Form.RESUMABLE_FILE_NAME -> "${%s}".format(Test.TEST_FILE_NAME)))
          .bodyPart(RawFileBodyPart("file", Test.IMAGE_FILE_FEED+"${%s}".format(Test.TEST_FILE_NAME))
            .transferEncoding("binary")).asMultipartForm)
        .exec(
          http("Store_Organization_KYC_"+"${documentType}")
            .get(session=>routes.AddOrganizationController.userStoreOrganizationKYC(session(Test.TEST_FILE_NAME).as[String],session("documentType").as[String]).url)
            .check(substring("Organization KYC Files").exists)
        )
        .pause(2)
    }
    .exec(http("User_Review_Add_Organization_Request_Form_GET")
      .get(routes.AddOrganizationController.userReviewAddOrganizationRequestForm().url)
      .check(css("legend:contains(%s)".format("User Review Add Organization Request")).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN))
    )
    .pause(1)
    .exec(http("User_Review_Add_Organization_Request_POST")
      .post(routes.AddOrganizationController.userReviewAddOrganizationRequest().url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        Form.COMPLETION -> true
      ))
      .check(substring("Organization Added For Verification").exists)
    )
    .pause(3)

  val verifyOrganizationScenario: ScenarioBuilder = scenario("VerifyOrganization")
    .feed(GasFeeder.gasFeed)
    .exec(http("Get_Pending_Verify_Organization_Request")
      .get(routes.AddOrganizationController.viewPendingVerifyOrganizationRequests().url)
      .check(substring("${%s}".format(Test.TEST_ORGANIZATION_ID)).exists)
    )
    .pause(2)

    .foreach(organizationKYCs,"documentType"){
      exec(http("Organization_KYC_Update_Status"+"${documentType}")
        .get(session=>routes.AddOrganizationController.updateOrganizationKYCDocumentStatusForm(session(Test.TEST_ORGANIZATION_ID).as[String],session("documentType").as[String]).url)
        .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN))
      )
        .pause(2)
        .exec(http("Organization_KYC_Update_Status"+"${documentType}")
        .post(routes.AddOrganizationController.updateOrganizationKYCDocumentStatus().url)
        .formParamMap(Map(
          Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
          Form.ORGANIZATION_ID -> "${%s}".format(Test.TEST_ORGANIZATION_ID),
          Form.DOCUMENT_TYPE -> "${documentType}",
          Form.STATUS -> true
        ))
      )
        .pause(1)
    }
    .pause(2)
    .feed(GasFeeder.gasFeed)
    .exec(http("Verify_Organization_Form_GET")
      .get(session=>routes.AddOrganizationController.acceptRequestForm(session(Test.TEST_ORGANIZATION_ID).as[String]).url)
      .check(css("legend:contains(%s)".format("Verify Organization")).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("Verify_Organization_POST")
      .post(routes.AddOrganizationController.acceptRequest().url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        Form.ORGANIZATION_ID -> "${%s}".format(Test.TEST_ORGANIZATION_ID),
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Form.PASSWORD -> "${%s}".format(Test.TEST_ZONE_PASSWORD),
      ))
      .check(substring("Organization Verified").exists)
    )
    .pause(3)

  val rejectVerifyOrganizationScenario: ScenarioBuilder = scenario("RejectVerifyOrganization")
    .exec(http("RejectVerifyOrganization_GET")
      .get(session=>routes.AddOrganizationController.rejectVerifyOrganizationRequestForm(session(Test.TEST_ORGANIZATION_ID).as[String]).url)
      .check(css("legend:contains(%s)".format(constants.Form.REJECT_ORGANIZATION_REQUEST.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("RejectVerifyOrganization_POST")
      .post(routes.AddOrganizationController.rejectVerifyOrganizationRequest().url)
      .formParamMap(Map(
        Form.ORGANIZATION_ID -> "${%s}".format(Test.TEST_ORGANIZATION_ID),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN)))
      .check(substring("SUCCESS VERIFY_ORGANIZATION_REQUEST_REJECTED").exists)
    )


  val blockchainAddOrganizationScenario: ScenarioBuilder = scenario("BlockchainAddOrganization")
    .feed(FromFeeder.fromFeed)
    .feed(ToFeeder.toFeed)
    .feed(OrganizationIDFeeder.organizationIDFeed)
    .feed(ZoneIDFeeder.zoneIDFeed)
    .feed(GasFeeder.gasFeed)
    .feed(ModeFeeder.modeFeed)
    .feed(PasswordFeeder.passwordFeed)
    .exec(http("BlockchainAddOrganization_GET")
      .get(routes.AddOrganizationController.blockchainAddOrganizationForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.BLOCKCHAIN_ADD_ORGANIZATION.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("BlockchainAddOrganization_POST")
      .post(routes.AddOrganizationController.blockchainAddOrganization().url)
      .formParamMap(Map(
        Form.FROM -> "${%s}".format(Test.TEST_FROM),
        Form.TO -> "${%s}".format(Test.TEST_TO),
        Form.ORGANIZATION_ID -> "${%s}".format(Test.TEST_ORGANIZATION_ID),
        Form.ZONE_ID -> "${%s}".format(Test.TEST_ZONE_ID),
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Form.MODE ->"${%s}".format(Test.TEST_MODE),
        Form.PASSWORD -> "${%s}".format(Test.TEST_PASSWORD),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN)))
      .check(substring("SUCCESS ORGANIZATION_ADDED").exists)
    )

  def getOrganizationID(query: String) = {
    val sqlQueryFeeder = jdbcFeeder("jdbc:postgresql://localhost:5432/commit", "commit", "commit",
      s"""SELECT COALESCE((SELECT "id" FROM master."Organization" WHERE "accountID" = '$query'),'0') AS "id";""")
    sqlQueryFeeder.apply().next()("id").toString
  }
}