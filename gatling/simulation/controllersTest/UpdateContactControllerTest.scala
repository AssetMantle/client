package controllersTest

import constants.{Form, Test}
import controllers.routes
import feeders._
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._

class UpdateContactControllerTest extends Simulation {

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
      .check(css("legend:contains(%s)".format(constants.Form.UPDATE_CONTACT.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("UpdateContact_POST")
      .post(routes.ContactController.updateContact().url)
      .formParamMap(Map(
        Form.MOBILE_NUMBER -> "${%s}".format(Test.TEST_MOBILE_NUMBER),
        Form.EMAIL_ADDRESS -> "${%s}".format(Test.TEST_EMAIL_ADDRESS),
        Form.COUNTRY_CODE -> "${%s}".format(Test.TEST_COUNTRY_CODE),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
}
