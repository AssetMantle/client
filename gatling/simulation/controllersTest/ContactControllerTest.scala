package controllersTest

import constants.{Form, Test}
import controllers.routes
import feeders._
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._

class ContactControllerTest extends Simulation {

  val scenarioBuilder: ScenarioBuilder = updateContactControllerTest.updateContactScenario
  setUp(scenarioBuilder.inject(atOnceUsers(Test.NUMBER_OF_USERS))).protocols(http.baseUrl(Test.BASE_URL))
}

object updateContactControllerTest {

  val updateContactScenario: ScenarioBuilder = scenario("UpdateContact")
    .feed(EmailAddressFeeder.emailAddressFeed)
    .feed(MobileNumberFeeder.mobileNumberFeed)
    .feed(CountryCodeFeeder.countryCodeFeed)
    .exec(http("UpdateContact_GET")
      .get(routes.ContactController.updateContactForm().url)
      .check(css("legend:contains(%s)".format("Update Contact")).exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN)))
    .pause(2)
    .exec(http("UpdateContact_POST")
      .post(routes.ContactController.updateContact().url)
      .formParamMap(Map(
        Form.MOBILE_NUMBER -> "${%s}".format(Test.TEST_MOBILE_NUMBER),
        Form.EMAIL_ADDRESS -> "${%s}".format(Test.TEST_EMAIL_ADDRESS),
        Form.COUNTRY_CODE -> "${%s}".format(Test.TEST_COUNTRY_CODE),
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN))))


  val addMobileNumberScenario: ScenarioBuilder = scenario("addMobileNumber")
    .feed(MobileNumberFeeder.mobileNumberFeed)
    .exec(http("AddMobileNumberForm_GET")
      .get(routes.ContactController.addOrUpdateMobileNumberForm().url)
      .check(css("legend:contains(%s)".format("Mobile Number")).exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN))
    )
    .pause(2)
    .exec(http("UpdateContact_POST")
      .post(routes.ContactController.addOrUpdateMobileNumber().url)
      .formParamMap(Map(
        constants.FormField.COUNTRY_CODE.name -> "${%s}".format(Test.TEST_COUNTRY_CODE),
        constants.FormField.MOBILE_NUMBER.name -> "${%s}".format(Test.TEST_MOBILE_NUMBER),
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN))
      )
      .check(substring("Mobile Number Updated").exists)
    )

  val verifyMobileNumberScenario: ScenarioBuilder = scenario("verifyMobileNumber")
    .feed(OTPFeeder.otpFeed)
    .exec(http("VerifyMobileNumberForm_GET")
      .get(routes.ContactController.verifyMobileNumberForm().url)
      .check(css("legend:contains(%s)".format("Verify Mobile Number")).exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN))
    )
    .pause(2)
    .exec(http("VerifyMobileNumber_POST")
      .post(routes.ContactController.verifyMobileNumber().url)
      .formParamMap(Map(
        constants.FormField.OTP.name -> "${%s}".format(Test.TEST_OTP),
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN))
      )
      .check(substring("Mobile Number Verified").exists)
    )

  val addEmailAddressScenario: ScenarioBuilder = scenario("addEmailAddress")
    .feed(EmailAddressFeeder.emailAddressFeed)
    .exec(http("AddEmailAddressForm_GET")
      .get(routes.ContactController.addOrUpdateEmailAddressForm().url)
      .check(css("legend:contains(%s)".format("Add Mobile Number")).exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN))
    )
    .pause(2)
    .exec(http("AddEmailAddress_POST")
      .post(routes.ContactController.addOrUpdateEmailAddress().url)
      .formParamMap(Map(
        constants.FormField.EMAIL_ADDRESS.name -> "${%s}".format(Test.TEST_EMAIL_ADDRESS),
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN))
      )
      .check(substring("Mobile Number Updated").exists)
    )

  val verifyEmailAddressScenario: ScenarioBuilder = scenario("verifyEmailAddress")
    .feed(OTPFeeder.otpFeed)
    .exec(http("VerifyEmailAddressForm_GET")
      .get(routes.ContactController.verifyEmailAddressForm().url)
      .check(css("legend:contains(%s)".format("Verify Email Address")).exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN))
    )
    .pause(2)
    .exec(http("VerifyEmailAddress_POST")
      .post(routes.ContactController.verifyEmailAddress().url)
      .formParamMap(Map(
        constants.FormField.OTP.name -> "${%s}".format(Test.TEST_OTP),
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN))
      )
      .check(substring("Email Address Verified").exists)
    )
}
