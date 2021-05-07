package scenarios

import constants.{Form, Test}
import controllers.routes
import feeders._
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef.jdbcFeeder
import java.util.Date

import feeders.JDBCFeeder.getUBOID

object AddOrganizationControllerTest {

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
        exec(AssetControllerTest.imageFeed)
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
          .bodyPart(RawFileBodyPart("file", Test.TEST_IMAGE_PATH + "${%s}".format(Test.TEST_FILE_NAME))
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
          .check(css("button:contains(Approve)").notExists)
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
        constants.FormField.PASSWORD.name -> "${%s}".format(Test.TEST_PASSWORD),
      ))
      .check(status.is(200))
      .check(substring("Organization Approved").exists)
    )
    .pause(Test.REQUEST_DELAY)

  val rejectOrganizationRequestScenario: ScenarioBuilder = scenario("RejectOrganizationRequest")
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
            constants.FormField.STATUS.name -> false
          ))
          .check(status.is(206))
          .check(css("[id=%s]".format(constants.FormField.ORGANIZATION_ID.name), "value").is("${%s}".format(Test.TEST_ORGANIZATION_ID)))
          .check(css("button:contains(Approve)").exists)
          .check(css("button:contains(Reject)").notExists)
        )
        .pause(Test.REQUEST_DELAY)
    }
    .exec(http("Reject_Organization_Request_Form_GET")
      .get(session=>routes.AddOrganizationController.rejectRequestForm(session(Test.TEST_ORGANIZATION_ID).as[String]).url)
      .check(status.is(200))
      .check(css("legend:contains(Reject Organization)").exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN)))
    .pause(Test.REQUEST_DELAY)
    .exec(http("Reject_Organization_Request_POST")
      .post(routes.AddOrganizationController.rejectRequest().url)
      .formParamMap(Map(
        constants.FormField.ORGANIZATION_ID.name -> "${%s}".format(Test.TEST_ORGANIZATION_ID),
        constants.FormField.COMMENT.name -> "",
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
      .check(status.is(200))
      .check(substring("Organization Rejected").exists)
    )

  val addOrUpdateOrganizationBankAccount: ScenarioBuilder = scenario("AddOrUpdateOrganizationBankAccount")
    .feed(BankAccountDetailFeeder.bankAccountDetailFeeder)
    .exec(http("Add_Or_Update_Organization_Bank_Account_Form_GET")
      .get(routes.AddOrganizationController.addOrUpdateOrganizationBankAccountForm().url)
      .check(status.is(200))
      .check(css("legend:contains(Add/Update Bank Account Details)").exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN)))
    .pause(Test.REQUEST_DELAY)
    .exec(http("Add_Or_Update_Organization_Bank_Account_POST")
      .post(routes.AddOrganizationController.addOrUpdateOrganizationBankAccount().url)
      .formParamMap(Map(
        constants.FormField.ACCOUNT_HOLDER_NAME.name ->  "${%s}".format(Test.TEST_ACCOUNT_HOLDER_NAME),
        constants.FormField.NICK_NAME.name ->  "${%s}".format(Test.TEST_NICK_NAME),
        constants.FormField.ACCOUNT_NUMBER.name ->  "${%s}".format(Test.TEST_ACCOUNT_NUMBER),
        constants.FormField.BANK_NAME.name ->  "${%s}".format(Test.TEST_BANK_NAME),
        constants.FormField.SWIFT_CODE.name -> "${%s}".format(Test.TEST_SWIFT_CODE),
        constants.FormField.STREET_ADDRESS.name -> "${%s}".format(Test.TEST_STREET_ADDRESS),
        constants.FormField.COUNTRY.name -> "${%s}".format(Test.TEST_COUNTRY),
        constants.FormField.ZIP_CODE.name -> "${%s}".format(Test.TEST_ZIP_CODE),
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
      .check(status.is(200))
      .check(substring("Bank Account Details Updated Successfully").exists)
    )

  val userAddUBO: ScenarioBuilder = scenario("UserAddUBO")
    .feed(UBOFeeder.uboFeed)
    .exec(http("User_Add_UBO_Form_GET")
      .get(routes.AddOrganizationController.userAddUBOForm().url)
      .check(status.is(200))
      .check(css("legend:contains(Add UBO Details)").exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN)))
    .pause(Test.REQUEST_DELAY)
    .exec(http("User_Add_UBO_POST")
      .post(routes.AddOrganizationController.userAddUBO().url)
      .formParamMap(Map(
        constants.FormField.PERSON_FIRST_NAME.name -> "${%s}".format(Test.TEST_PERSON_FIRST_NAME),
        constants.FormField.PERSON_LAST_NAME.name -> "${%s}".format(Test.TEST_PERSON_LAST_NAME),
        constants.FormField.SHARE_PERCENTAGE.name -> "${%s}".format(Test.TEST_SHARE_PERCENTAGE),
        constants.FormField.RELATIONSHIP.name -> "${%s}".format(Test.TEST_RELATIONSHIP),
        constants.FormField.TITLE.name -> "${%s}".format(Test.TEST_TITLE),
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
      .check(status.is(200))
      .check(substring("UBO Details added successfully").exists)
    )

  val userDeleteUBO: ScenarioBuilder = scenario("UserDeleteUBO")
    .exec { session => session.set(Test.TEST_UBO_ID, getUBOID(session(Test.TEST_ORGANIZATION_ID).as[String],session(Test.TEST_PERSON_FIRST_NAME).as[String],session(Test.TEST_PERSON_LAST_NAME).as[String])) }
    .exec(http("User_Delete_UBO_Form_GET")
      .get(session=>routes.AddOrganizationController.userDeleteUBOForm(session(Test.TEST_UBO_ID).as[String]).url)
      .check(status.is(200))
      .check(css("legend:contains(Delete UBO)").exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN)))
    .pause(Test.REQUEST_DELAY)
    .exec(http("User_Delete_UBO_POST")
      .post(routes.AddOrganizationController.userDeleteUBO().url)
      .formParamMap(Map(
        constants.FormField.ID.name -> "${%s}".format(Test.TEST_UBO_ID),
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
      .check(status.is(200))
      .check(substring("UBO Deleted").exists)
    )

  val addUBO: ScenarioBuilder = scenario("AddUBO")
    .feed(UBOFeeder.uboFeed)
    .exec(http("Add_UBO_Form_GET")
      .get(routes.AddOrganizationController.addUBOForm().url)
      .check(status.is(200))
      .check(css("legend:contains(Add UBO Details)").exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN)))
    .pause(Test.REQUEST_DELAY)
    .exec(http("Add_UBO_POST")
      .post(routes.AddOrganizationController.addUBO().url)
      .formParamMap(Map(
        constants.FormField.PERSON_FIRST_NAME.name -> "${%s}".format(Test.TEST_PERSON_FIRST_NAME),
        constants.FormField.PERSON_LAST_NAME.name -> "${%s}".format(Test.TEST_PERSON_LAST_NAME),
        constants.FormField.SHARE_PERCENTAGE.name -> "${%s}".format(Test.TEST_SHARE_PERCENTAGE),
        constants.FormField.RELATIONSHIP.name -> "${%s}".format(Test.TEST_RELATIONSHIP),
        constants.FormField.TITLE.name -> "${%s}".format(Test.TEST_TITLE),
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
      .check(status.is(200))
      .check(substring("UBO Details added successfully").exists)
    )

  val deleteUBO: ScenarioBuilder = scenario("deleteUBO")
    .exec { session => session.set(Test.TEST_UBO_ID, getUBOID(session(Test.TEST_ORGANIZATION_ID).as[String],session(Test.TEST_PERSON_FIRST_NAME).as[String],session(Test.TEST_PERSON_LAST_NAME).as[String])) }
    .exec(http("Delete_UBO_Form_GET")
      .get(session=>routes.AddOrganizationController.deleteUBOForm(session(Test.TEST_UBO_ID).as[String]).url)
      .check(status.is(200))
      .check(css("legend:contains(Delete UBO)").exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN)))
    .pause(Test.REQUEST_DELAY)
    .exec(http("Delete_UBO_POST")
      .post(routes.AddOrganizationController.deleteUBO().url)
      .formParamMap(Map(
        constants.FormField.ID.name -> "${%s}".format(Test.TEST_UBO_ID),
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
      .check(status.is(200))
      .check(substring("UBO Deleted").exists)
    )
}