/*
package controllersTest

import constants.{Form, Test}
import controllers.routes
import controllersTest.addOrganizationControllerTest.getOrganizationID
import controllersTest.addZoneControllerTest.{getZoneID, getZoneStatus}
import controllersTest.changeBuyerBidControllerTest.getAddressFromAccountID
import controllersTest.issueAssetControllerTest.{allIssueAssetDocumentType, getAssetPriceForIssueAsset, getAssetQuantityForIssueAsset, getAssetTypeForIssueAsset, getDocumentHashForIssueAsset, getQuantityUnitForIssueAsset}
import controllersTest.issueFiatControllerTest.{getTransactionAmountForIssueFiat, getTransactionIDForIssueFiat}
//import controllersTest.changeBuyerBidControllerTest.getBuyerAddress
import controllersTest.changeSellerBidControllerTest.getSellerAddress
import controllersTest.issueAssetControllerTest.{getPegHashByOwnerAddress, getRequestIDForIssueAsset}
import controllersTest.issueFiatControllerTest.getRequestIDForIssueFiat
import controllersTest.sendCoinControllerTest.getRequestIDForFaucetRequest
import controllersTest.setACLControllerTest._
import feeders._
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._

/*object setACLPrivileges {
  val issueAsset = true
  val issueFiat = true
  val sendAsset = true
  val sendFiat = true
  val redeemAsset = true
  val redeemFiat = true
  val sellerExecuteOrder = true
  val buyerExecuteOrder = true
  val changeBuyerBid = true
  val changeSellerBid = true
  val confirmBuyerBid = true
  val confirmSellerBid = true
  val negotiation = true
  val releaseAsset = true
}*/

object intParameters {
  val transactionAmount = 10000
  val assetQuantity = 2
  val assetPrice = 1000
}

class MasterTest2 extends Simulation {

    val oneCompleteScenario=scenario("ONE_COMPLETE_TRANSACTION")
      .exec(zoneSignUp2.masterZoneSignUp)
      .exec(zoneFaucetRequest2.masterLoginAndRequestCoinZone)
      .exec(loginMainApproveFaucetZone2.masterLoginMainAndApproveFaucetRequestZone)
      .exec(zoneLoginAddRequest2.masterZoneLoginAddRequest)
      .exec(loginMainAndApproveZone2.masterLoginMainAndApproveZone)
      .exec(organizationSignUp2.masterOrganizationSignUp)
      .exec(organizationFaucetRequest2.masterLoginAndRequestCoinOrganization)
      .exec(loginMainApproveFaucetOrganization.masterLoginMainAndApproveFaucetRequestOrganization)
      .exec(OrganizationLoginAddRequest.masterOrganizationLoginAddRequest)
      .exec(loginZoneAndApproveOrganization.masterLoginZoneAndApproveOrganization)
      .exec(SignUpAndLoginSellerAndCreateFaucetRequest.signUpAndLoginSellerAndCreateFaucetRequest)
      .exec(ApproveFaucetRequestSeller.approveFaucetRequestSeller)
      .exec(AddSellerTraderRequest.addSellerTraderRequest)
      .exec(ApproveSellerTraderACL.approveSellerTraderACL)
      .exec(SignUpAndLoginBuyerAndCreateFaucetRequest.signUpAndLoginBuyerAndCreateFaucetRequest)
      .exec(ApproveFaucetRequestBuyer.approveFaucetRequestBuyer)
      .exec(AddBuyerTraderRequest.addBuyerTraderRequest)
      .exec(ApproveBuyerTraderACL.approveBuyerTraderACL)
      .exec(IssueAssetRequestForSeller.issueAssetRequestForSeller)
      .exec(ApproveIssueAsset.approveIssueAsset)
      .exec(IssueFiatRequestBuyer.issueFiatRequest)
      .exec(IssueFiat.issueFiat)
      .exec(ChangeBuyerBid.changeBuyerBid)
      .exec(ChangeSellerBid.changeSellerBid)
      .exec(ConfirmBuyerBid.confirmBuyerBid)
      .exec(ConfirmSellerBid.confirmSellerBid)
      .exec(SendFiat.sendFiat)
      .exec(ReleaseAsset.releaseAsset)
      .exec(SendAsset.sendAsset)
      .exec(BuyerAndSellerExecuteOrder.buyerAndSellerExecuteOrder)

  setUp(
    oneCompleteScenario.inject(atOnceUsers(1))
  )
    .maxDuration(1000)
    .protocols( )
}


object zoneSignUp2 {
  val masterZoneSignUp: ScenarioBuilder = scenario("masterZoneSignUp")
    .feed(ZoneLoginFeeder.zoneLoginFeed)
    .exec(http("ZoneSignUp_GET")
      .get(routes.AccountController.signUpForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(5)
    .exec(http("ZoneSignUp_POST")
      .post(routes.AccountController.signUp().url)
      .formParamMap(Map(
        Form.USERNAME -> "${%s}".format(Test.TEST_ZONE_USERNAME),
        Form.USERNAME_AVAILABLE -> true,
        Form.PASSWORD -> "${%s}".format(Test.TEST_ZONE_PASSWORD),
        Form.CONFIRM_PASSWORD -> "${%s}".format(Test.TEST_ZONE_PASSWORD),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
    .pause(2)

}

object zoneFaucetRequest2 {
  val masterLoginAndRequestCoinZone: ScenarioBuilder = scenario("masterLoginAndRequestCoinZone")
    .exec(http("LoginZone_GET")
      .get(routes.AccountController.loginForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .exec(http("LoginZone_POST")
      .post(routes.AccountController.login().url)
      .formParamMap(Map(
        Form.USERNAME ->  "${%s}".format(Test.TEST_ZONE_USERNAME),
        Form.PASSWORD ->  "${%s}".format(Test.TEST_ZONE_PASSWORD),
        Form.PUSH_NOTIFICATION_TOKEN -> "",
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
    .pause(5)
    .feed(CouponFeeder.couponFeed)
    .exec(http("RequestCoinZone_GET")
      .get(routes.SendCoinController.faucetRequestForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("RequestCoinZone_POST")
      .post(routes.SendCoinController.faucetRequest().url)
      .formParamMap(Map(
        Form.COUPON -> "${%s}".format(Test.TEST_COUPON),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
    .pause(2)

}


object loginMainApproveFaucetZone2 {

  val masterLoginMainAndApproveFaucetRequestZone: ScenarioBuilder = scenario("masterLoginMainAndApproveFaucetRequestZone")
    .feed(GenesisFeeder.genesisFeed)
    .feed(GasFeeder.gasFeed)
    .exec(http("MainLogin_GET")
      .get(routes.AccountController.loginForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .exec(http("MainLogin_POST")
      .post(routes.AccountController.login().url)
      .formParamMap(Map(
        Form.USERNAME -> "${%s}".format(Test.TEST_MAIN_USERNAME),
        Form.PASSWORD -> "${%s}".format(Test.TEST_MAIN_PASSWORD),
        Form.PUSH_NOTIFICATION_TOKEN -> "",
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
    .pause(2)
    .exec { session => session.set(Test.TEST_REQUEST_ID, getRequestIDForFaucetRequest(session(Test.TEST_ZONE_USERNAME).as[String])) }
    .doIf(session=> session(Test.TEST_REQUEST_ID).as[String] == "0") {
      asLongAs(session=> session(Test.TEST_REQUEST_ID).as[String] =="0") {
        pause(1)
          .exec { session => session.set(Test.TEST_REQUEST_ID, getRequestIDForFaucetRequest(session(Test.TEST_ZONE_USERNAME).as[String])) }
      }
    }
    .exec(http("ApproveFaucetRequestZone_GET")
      .get("/master/approveFaucetRequests?requestID=" + "${%s}".format(Test.TEST_REQUEST_ID) + "&accountID=" + "${%s}".format(Test.TEST_ZONE_USERNAME))
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("ApproveFaucetRequestZone_POST")
      .post(routes.SendCoinController.approveFaucetRequests().url)
      .formParamMap(Map(
        Form.REQUEST_ID -> "${%s}".format(Test.TEST_REQUEST_ID),
        Form.ACCOUNT_ID -> "${%s}".format(Test.TEST_ZONE_USERNAME),
        Form.PASSWORD -> "${%s}".format(Test.TEST_MAIN_PASSWORD),
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
    .pause(5)
    .exec{session => session.set(Test.USER_TYPE, sendCoinControllerTest.getUserType(session(Test.TEST_ZONE_USERNAME).as[String])) }
    .doIf(session=> session(Test.USER_TYPE).as[String] != "USER") {
      asLongAs(session=> session(Test.USER_TYPE).as[String] != "USER") {
        pause(1)
          .exec { session =>session.set(Test.USER_TYPE, sendCoinControllerTest.getUserType(session(Test.TEST_ZONE_USERNAME).as[String])) }
      }
    }
}

object zoneLoginAddRequest2 {

  val masterZoneLoginAddRequest: ScenarioBuilder = scenario("masterZoneLoginAddRequest")
    .exec(http("ZoneLogin_GET")
      .get(routes.AccountController.loginForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .exec(http("ZoneLogin_POST")
      .post(routes.AccountController.login().url)
      .formParamMap(Map(
        Form.USERNAME -> "${%s}".format(Test.TEST_ZONE_USERNAME),
        Form.PASSWORD ->"${%s}".format(Test.TEST_ZONE_PASSWORD),
        Form.PUSH_NOTIFICATION_TOKEN -> "",
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
    .pause(2)
    .feed(NameFeeder.nameFeed)
    .feed(CurrencyFeeder.currencyFeed)
    .feed(AddressDataFeeder.addressDataFeed)
    .exec(http("AddZone_GET")
      .get(routes.AddZoneController.addZoneForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("AddZone_POST")
      .post(routes.AddZoneController.addZone().url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        Form.NAME -> "${%s}".format(Test.TEST_NAME),
        Form.CURRENCY -> "${%s}".format(Test.TEST_CURRENCY),
        Form.ADDRESS_ADDRESS_LINE_1 -> "${%s}".format(Test.TEST_ADDRESS_LINE_1),
        Form.ADDRESS_ADDRESS_LINE_2 -> "${%s}".format(Test.TEST_ADDRESS_LINE_2),
        Form.ADDRESS_LANDMARK -> "${%s}".format(Test.TEST_LANDMARK),
        Form.ADDRESS_CITY -> "${%s}".format(Test.TEST_CITY),
        Form.ADDRESS_COUNTRY ->"${%s}".format(Test.TEST_COUNTRY),
        Form.ADDRESS_ZIP_CODE -> "${%s}".format(Test.TEST_ZIP_CODE),
        Form.ADDRESS_PHONE -> "${%s}".format(Test.TEST_PHONE)
      )))
    .pause(2)
    .foreach(addZoneControllerTest.documentTypeSeq,"documentType"){
      feed(ImageFeeder.imageFeed)
        .exec(http("ZoneKYC_Upload_"+"${documentType}"+"_Form_Get")
          .get("/userUpload/zoneKYC?documentType="+"${documentType}")
          .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN))
        )
        .pause(3)
        .exec(http("ZoneKYC_Upload_"+"${documentType}")
          .post("/userUpload/userZoneKYCDocument?documentType="+"${documentType}")
          .formParamMap(Map(
            Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
            Form.RESUMABLE_CHUNK_NUMBER -> "1",
            Form.RESUMABLE_CHUNK_SIZE  -> "1048576",
            Form.RESUMABLE_TOTAL_SIZE -> "${%s}".format(Test.TEST_FILE_SIZE),
            Form.RESUMABLE_IDENTIFIER -> "document",
            Form.RESUMABLE_FILE_NAME -> "${%s}".format(Test.TEST_FILE_NAME)))
          .bodyPart(RawFileBodyPart("file", "/root/IdeaProjects/commitCentral/gatling/simulation/images/"+"${%s}".format(Test.TEST_FILE_NAME))
            .transferEncoding("binary")).asMultipartForm)
        .exec(
          http("StoreZoneKYC_"+"${documentType}")
            .get("/userUpload/storeUserZoneKYCDocument?name="+"${%s}".format(Test.TEST_FILE_NAME)+"&documentType="+"${documentType}")
        )
        .pause(2)
    }
    .pause(1)
    .exec(http("User_Review_Add_Zone_Request_Form")
      .get(routes.AddZoneController.userReviewAddZoneRequestForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN))
    )
    .pause(1)
    .exec(http("User_Review_Add_Zone_Request")
      .post(routes.AddZoneController.userReviewAddZoneRequest().url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        Form.COMPLETION -> true
      ))
    )
}

object loginMainAndApproveZone2 {

  val masterLoginMainAndApproveZone: ScenarioBuilder = scenario("masterLoginMainAndApproveZone")
    .feed(GenesisFeeder.genesisFeed)
    .exec(http("MainLogin_GET")
      .get(routes.AccountController.loginForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .exec(http("MainLogin_POST")
      .post(routes.AccountController.login().url)
      .formParamMap(Map(
        Form.USERNAME -> "${%s}".format(Test.TEST_MAIN_USERNAME),
        Form.PASSWORD -> "${%s}".format(Test.TEST_MAIN_PASSWORD),
        Form.PUSH_NOTIFICATION_TOKEN -> "",
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
    .pause(2)
    .exec { session => session.set(Test.TEST_ZONE_ID, getZoneID(session(Test.TEST_ZONE_USERNAME).as[String]))}
    .doIf(session=> session(Test.TEST_ZONE_ID).as[String] == "0") {
      asLongAs(session=> session(Test.TEST_ZONE_ID).as[String] == "0") {
        pause(1)
          .exec { session => session.set(Test.TEST_ZONE_ID, getZoneID(session(Test.TEST_ZONE_USERNAME).as[String]))}
      }
    }
    .exec(http("Get_Pending_Verify_Zone_Request")
      .get(routes.AddZoneController.viewPendingVerifyZoneRequests().url)
    )
    .pause(1)
    .foreach(addZoneControllerTest.documentTypeSeq,"documentType"){
      exec(http("Zone_KYC_update_Status"+"${documentType}")
        .post(routes.AddZoneController.updateZoneKYCDocumentStatus().url)
        .formParamMap(Map(
          Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
          Form.ZONE_ID -> "${%s}".format(Test.TEST_ZONE_ID),
          Form.DOCUMENT_TYPE -> "${documentType}",
          Form.STATUS -> true
        ))
      )
        .pause(1)
    }
    .exec(http("VerifyZone_GET")
      .get(routes.AddZoneController.verifyZoneForm("${%s}".format(Test.TEST_ZONE_ID)).url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .feed(GasFeeder.gasFeed)
    .exec(http("VerifyZone_POST")
      .post(routes.AddZoneController.verifyZone().url)
      .formParamMap(Map(
        Form.ZONE_ID -> "${%s}".format(Test.TEST_ZONE_ID),
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Form.PASSWORD -> "${%s}".format(Test.TEST_MAIN_PASSWORD),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
    .pause(5)
}

object organizationSignUp2 {

  val masterOrganizationSignUp: ScenarioBuilder = scenario("masterOrganizationSignUp")
    .feed(OrganizationLoginFeeder.organizationLoginFeed)
    .exec(http("OrganizationSignUp_GET")
      .get(routes.AccountController.signUpForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(5)
    .exec(http("OrganizationSignUp_POST")
      .post(routes.AccountController.signUp().url)
      .formParamMap(Map(
        Form.USERNAME -> "${%s}".format(Test.TEST_ORGANIZATION_USERNAME),
        Form.USERNAME_AVAILABLE -> true,
        Form.PASSWORD -> "${%s}".format(Test.TEST_ORGANIZATION_PASSWORD),
        Form.CONFIRM_PASSWORD ->"${%s}".format(Test.TEST_ORGANIZATION_PASSWORD),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
    .pause(3)

}

object organizationFaucetRequest2 {

  val masterLoginAndRequestCoinOrganization: ScenarioBuilder = scenario("masterLoginAndRequestCoinOrganization")
    .exec(http("LoginOrganization_GET")
      .get(routes.AccountController.loginForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .exec(http("LoginOrganization_POST")
      .post(routes.AccountController.login().url)
      .formParamMap(Map(
        Form.USERNAME -> "${%s}".format(Test.TEST_ORGANIZATION_USERNAME),
        Form.PASSWORD ->"${%s}".format(Test.TEST_ORGANIZATION_PASSWORD),
        Form.PUSH_NOTIFICATION_TOKEN -> "",
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
    .pause(5)
    .feed(CouponFeeder.couponFeed)
    .exec(http("RequestCoinOrganization_GET")
      .get(routes.SendCoinController.faucetRequestForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("RequestCoinOrganization_POST")
      .post(routes.SendCoinController.faucetRequest().url)
      .formParamMap(Map(
        Form.COUPON -> "${%s}".format(Test.TEST_COUPON),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
    .pause(2)
}


object loginMainApproveFaucetOrganization {

  val masterLoginMainAndApproveFaucetRequestOrganization: ScenarioBuilder = scenario("masterLoginMainAndApproveFaucetRequestOrganization")
    .feed(GenesisFeeder.genesisFeed)
    .feed(GasFeeder.gasFeed)
    .exec(http("MainLogin_GET")
      .get(routes.AccountController.loginForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .exec(http("MainLogin_POST")
      .post(routes.AccountController.login().url)
      .formParamMap(Map(
        Form.USERNAME -> "${%s}".format(Test.TEST_MAIN_USERNAME),
        Form.PASSWORD -> "${%s}".format(Test.TEST_MAIN_PASSWORD),
        Form.PUSH_NOTIFICATION_TOKEN -> "",
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
    .pause(2)
    .exec { session => session.set(Test.TEST_REQUEST_ID, getRequestIDForFaucetRequest(session(Test.TEST_ORGANIZATION_USERNAME).as[String])) }
    .doIf(session=> session(Test.TEST_REQUEST_ID).as[String] == "0") {
      asLongAs(session=> session(Test.TEST_REQUEST_ID).as[String] == "0") {
        pause(1)
          .exec { session => session.set(Test.TEST_REQUEST_ID,getRequestIDForFaucetRequest(session(Test.TEST_ORGANIZATION_USERNAME).as[String])) }
      }
    }
    .exec(http("ApproveFaucetRequest_GET")
      .get(routes.SendCoinController.approveFaucetRequestsForm("${%s}".format(Test.TEST_REQUEST_ID),"${%s}".format(Test.TEST_ORGANIZATION_USERNAME)).url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("ApproveFaucetRequest_POST")
      .post(routes.SendCoinController.approveFaucetRequests().url)
      .formParamMap(Map(
        Form.REQUEST_ID -> "${%s}".format(Test.TEST_REQUEST_ID),
        Form.ACCOUNT_ID -> "${%s}".format(Test.TEST_ORGANIZATION_USERNAME),
        Form.PASSWORD -> "${%s}".format(Test.TEST_MAIN_PASSWORD),
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
    .pause(2)
    .exec{session => session.set(Test.USER_TYPE, sendCoinControllerTest.getUserType(session(Test.TEST_ORGANIZATION_USERNAME).as[String])) }
    .doIf(session=> session(Test.USER_TYPE).as[String] != "USER") {
      asLongAs(session=> session(Test.USER_TYPE).as[String] != "USER") {
        pause(1)
          .exec { session =>session.set(Test.USER_TYPE, sendCoinControllerTest.getUserType(session(Test.TEST_ORGANIZATION_USERNAME).as[String])) }
      }
    }
}


object OrganizationLoginAddRequest {
  val masterOrganizationLoginAddRequest: ScenarioBuilder = scenario("masterOrganizationLoginAddRequest")
    .exec(http("OrganizationLogin_GET")
      .get(routes.AccountController.loginForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .exec(http("OrganizationLogin_POST")
      .post(routes.AccountController.login().url)
      .formParamMap(Map(
        Form.USERNAME -> "${%s}".format(Test.TEST_ORGANIZATION_USERNAME),
        Form.PASSWORD -> "${%s}".format(Test.TEST_ORGANIZATION_PASSWORD),
        Form.PUSH_NOTIFICATION_TOKEN -> "",
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
    .pause(5)
    .feed(NameFeeder.nameFeed)
    .feed(AddressFeeder.addressFeed)
    .feed(EmailAddressFeeder.emailAddressFeed)
    .feed(AddressDataFeeder.addressDataFeed)
    .exec(http("AddOrganization_GET")
      .get(routes.AddOrganizationController.addOrganizationForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("AddOrganization_POST")
      .post(routes.AddOrganizationController.addOrganizationForm().url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        Form.ZONE_ID -> "${%s}".format(Test.TEST_ZONE_ID),
        Form.NAME -> "${%s}".format(Test.TEST_NAME),
        Form.ABBREVIATION -> "${%s}".format(Test.TEST_NAME),
        Form.ESTABLISHMENT_DATE -> "2019-11-11",
        Form.EMAIL_ADDRESS -> "absd@wfkawf.vom",
        Form.REGISTERED_ADDRESS_LINE_1 -> "${%s}".format(Test.TEST_ADDRESS_LINE_1),
        Form.REGISTERED_ADDRESS_LINE_2 -> "${%s}".format(Test.TEST_ADDRESS_LINE_2),
        Form.REGISTERED_LANDMARK -> "${%s}".format(Test.TEST_LANDMARK),
        Form.REGISTERED_CITY -> "${%s}".format(Test.TEST_CITY),
        Form.REGISTERED_COUNTRY -> "${%s}".format(Test.TEST_COUNTRY),
        Form.REGISTERED_ZIP_CODE -> "${%s}".format(Test.TEST_ZIP_CODE),
        Form.REGISTERED_PHONE ->"1234567890",
        Form.POSTAL_ADDRESS_LINE_1 ->"${%s}".format(Test.TEST_ADDRESS_LINE_1),
        Form.POSTAL_ADDRESS_LINE_2 ->"${%s}".format(Test.TEST_ADDRESS_LINE_2),
        Form.POSTAL_LANDMARK ->"${%s}".format(Test.TEST_LANDMARK),
        Form.POSTAL_CITY -> "${%s}".format(Test.TEST_CITY),
        Form.POSTAL_COUNTRY -> "${%s}".format(Test.TEST_COUNTRY),
        Form.POSTAL_ZIP_CODE -> "${%s}".format(Test.TEST_ZIP_CODE),
        Form.POSTAL_PHONE -> "${%s}".format(Test.TEST_PHONE)
      )))
    .pause(2)
    .exec(http("Get_Organization_Bank_Account_Detail_Form")
      .get(routes.AddOrganizationController.organizationBankAccountDetailForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN))
    )
    .pause(2)
    .feed(AccountNumberFeeder.accountNumberFeed)
    .feed(SwiftCodeFeeder.swiftCodeFeed)
    .exec(http("Organization_Bank_Account_Detail")
      .post(routes.AddOrganizationController.organizationBankAccountDetail().url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        Form.ACCOUNT_HOLDER_NAME -> "${%s}".format(Test.TEST_NAME),
        Form.NICK_NAME -> "${%s}".format(Test.TEST_NAME),
        Form.ACCOUNT_NUMBER -> "${%s}".format(Test.TEST_ACCOUNT_NUMBER),
        Form.BANK_NAME -> "${%s}".format(Test.TEST_NAME),
        Form.SWIFT_CODE -> "${%s}".format(Test.TEST_SWIFT_CODE),
        Form.STREET_ADDRESS -> "${%s}".format(Test.TEST_NAME),
        Form.COUNTRY -> "${%s}".format(Test.TEST_COUNTRY),
        Form.ZIP_CODE -> "${%s}".format(Test.TEST_ZIP_CODE)
      ))
    )
    .pause(2)
    .foreach(addOrganizationControllerTest.documentTypeSeq,"documentType"){
      feed(ImageFeeder.imageFeed)
        .exec(http("ORGANIZATION_KYC_UPLOAD_"+"${documentType}"+"_FORM")
          .get("/master/userUploadOrganizationKYC?documentType="+"${documentType}")
          .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN))
        )
        .pause(2)
        .exec(http("ORGANIZATION_KYC_UPLOAD_"+"${documentType}")
          .post("/master/userUploadOrganizationKYCDocument?documentType="+"${documentType}")
          .formParamMap(Map(
            Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
            Form.RESUMABLE_CHUNK_NUMBER -> "1",
            Form.RESUMABLE_CHUNK_SIZE  -> "1048576",
            Form.RESUMABLE_TOTAL_SIZE -> "${%s}".format(Test.TEST_FILE_SIZE),
            Form.RESUMABLE_IDENTIFIER -> "document",
            Form.RESUMABLE_FILE_NAME -> "${%s}".format(Test.TEST_FILE_NAME)))
          .bodyPart(RawFileBodyPart("file", "/root/IdeaProjects/commitCentral/gatling/simulation/images/"+"${%s}".format(Test.TEST_FILE_NAME))
            .transferEncoding("binary")).asMultipartForm)
        .exec(
          http("STORE_ORGANIZATION_"+"${documentType}")
            .get("/master/storeUserOrganizationKYCDocument?name="+"${%s}".format(Test.TEST_FILE_NAME)+"&documentType="+"${documentType}")
        )
        .pause(2)
    }
    .exec(http("USER_REVIEW_ADD_ORGANIZATION_REQUEST_FORM")
      .get(routes.AddOrganizationController.userReviewAddOrganizationRequestForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN))
    )
    .pause(1)
    .exec(http("USER_REVIEW_ADD_ORGANIZATION_REQUEST")
      .post(routes.AddOrganizationController.userReviewAddOrganizationRequest().url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        Form.COMPLETION -> true
      ))
    )
}

object loginZoneAndApproveOrganization {

  val masterLoginZoneAndApproveOrganization: ScenarioBuilder = scenario("masterLoginZoneAndApproveOrganization")
    .feed(OrganizationIDFeeder.organizationIDFeed)
    .exec(http("VerifiedZoneLogin_GET")
      .get(routes.AccountController.loginForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .exec(http("VerifiedZoneLogin_POST")
      .post(routes.AccountController.login().url)
      .formParamMap(Map(
        Form.USERNAME -> "${%s}".format(Test.TEST_ZONE_USERNAME),
        Form.PASSWORD -> "${%s}".format(Test.TEST_ZONE_PASSWORD),
        Form.PUSH_NOTIFICATION_TOKEN -> "",
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
    .pause(2)
    .exec { session => session.set(Test.TEST_ORGANIZATION_ID, getOrganizationID(session(Test.TEST_ORGANIZATION_USERNAME).as[String]))}
    .doIf(session=> session(Test.TEST_ORGANIZATION_ID).as[String] == "0") {
      asLongAs(session=> session(Test.TEST_ORGANIZATION_ID).as[String] == "0") {
        pause(1)
          .exec { session => session.set(Test.TEST_ORGANIZATION_ID, getOrganizationID(session(Test.TEST_ORGANIZATION_USERNAME).as[String]))}
      }
    }
    .exec(http("Get_Pending_Verify_Organization_Request")
      .get(routes.AddOrganizationController.viewPendingVerifyOrganizationRequests().url)
    )
    .pause(2)
    .foreach(addOrganizationControllerTest.documentTypeSeq,"documentType"){
      exec(http("Organization_KYC_update_Status"+"${documentType}")
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
    .exec(http("verify_Organization_Get")
      .get(routes.AddOrganizationController.verifyOrganizationForm("${%s}".format(Test.TEST_ORGANIZATION_ID),"${%s}".format(Test.TEST_ZONE_ID)).url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN))
    )
    .pause(2)
    .feed(GasFeeder.gasFeed)
    .exec(http("verify_Organization_Post")
      .post(routes.AddOrganizationController.verifyOrganization().url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        Form.ORGANIZATION_ID -> "${%s}".format(Test.TEST_ORGANIZATION_ID),
        Form.ZONE_ID -> "${%s}".format(Test.TEST_ZONE_ID),
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Form.PASSWORD -> "${%s}".format(Test.TEST_ZONE_PASSWORD),
      ))
    ).pause(2)
}


object SignUpAndLoginSellerAndCreateFaucetRequest{

  val signUpAndLoginSellerAndCreateFaucetRequest=scenario("createSeller")
    .feed(SellerFeeder.sellerFeed)
    .exec(http("SellerSignUp_GET")
      .get(routes.AccountController.signUpForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(5)
    .exec(http("SellerSignUp_POST")
      .post(routes.AccountController.signUp().url)
      .formParamMap(Map(
        Form.USERNAME -> "${%s}".format(Test.TEST_SELLER_USERNAME),
        Form.USERNAME_AVAILABLE -> true,
        Form.PASSWORD -> "${%s}".format(Test.TEST_SELLER_PASSWORD),
        Form.CONFIRM_PASSWORD -> "${%s}".format(Test.TEST_SELLER_PASSWORD),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
    .pause(2)
    .exec(http("LoginSeller_GET")
      .get(routes.AccountController.loginForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .exec(http("LoginSeller_POST")
      .post(routes.AccountController.login().url)
      .formParamMap(Map(
        Form.USERNAME -> "${%s}".format(Test.TEST_SELLER_USERNAME),
        Form.PASSWORD -> "${%s}".format(Test.TEST_SELLER_PASSWORD),
        Form.PUSH_NOTIFICATION_TOKEN -> "",
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
    .pause(5)
    .feed(CouponFeeder.couponFeed)
    .exec(http("RequestCoinSeller_GET")
      .get(routes.SendCoinController.faucetRequestForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("RequestCoinSeller_POST")
      .post(routes.SendCoinController.faucetRequest().url)
      .formParamMap(Map(
        Form.COUPON -> "${%s}".format(Test.TEST_COUPON),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
    .pause(2)
}

object ApproveFaucetRequestSeller{

  val approveFaucetRequestSeller=scenario("masterLoginMainAndApproveFaucetRequestSeller")
    .feed(GenesisFeeder.genesisFeed)
    .feed(GasFeeder.gasFeed)
    .exec(http("MainLogin_GET")
      .get(routes.AccountController.loginForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .exec(http("MainLogin_POST")
      .post(routes.AccountController.login().url)
      .formParamMap(Map(
        Form.USERNAME -> "${%s}".format(Test.TEST_MAIN_USERNAME),
        Form.PASSWORD -> "${%s}".format(Test.TEST_MAIN_PASSWORD),
        Form.PUSH_NOTIFICATION_TOKEN -> "",
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
    .pause(2)
    .exec { session => session.set(Test.TEST_REQUEST_ID, getRequestIDForFaucetRequest(session(Test.TEST_SELLER_USERNAME).as[String])) }
    .doIf(session=> session(Test.TEST_REQUEST_ID).as[String] == "0") {
      asLongAs(session=> session(Test.TEST_REQUEST_ID).as[String] =="0") {
        pause(1)
          .exec { session => session.set(Test.TEST_REQUEST_ID, getRequestIDForFaucetRequest(session(Test.TEST_SELLER_USERNAME).as[String])) }
      }
    }
    .exec(http("ApproveFaucetRequestSeller_GET")
      .get("/master/approveFaucetRequests?requestID=" + "${%s}".format(Test.TEST_REQUEST_ID) + "&accountID=" + "${%s}".format(Test.TEST_SELLER_USERNAME))
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("ApproveFaucetRequestZone_POST")
      .post(routes.SendCoinController.approveFaucetRequests().url)
      .formParamMap(Map(
        Form.REQUEST_ID -> "${%s}".format(Test.TEST_REQUEST_ID),
        Form.ACCOUNT_ID -> "${%s}".format(Test.TEST_SELLER_USERNAME),
        Form.PASSWORD -> "${%s}".format(Test.TEST_MAIN_PASSWORD),
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
    .pause(5)
    .exec{session => session.set(Test.USER_TYPE, sendCoinControllerTest.getUserType(session(Test.TEST_SELLER_USERNAME).as[String])) }
    .doIf(session=> session(Test.USER_TYPE).as[String] != "USER") {
      asLongAs(session=> session(Test.USER_TYPE).as[String] != "USER") {
        pause(1)
          .exec { session =>session.set(Test.USER_TYPE, sendCoinControllerTest.getUserType(session(Test.TEST_SELLER_USERNAME).as[String])) }
      }
    }
}

object AddSellerTraderRequest{

  val addSellerTraderRequest=scenario("addSelleTraderRequest")
    .exec(http("LoginSeller_GET")
      .get(routes.AccountController.loginForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .exec(http("LoginSeller_POST")
      .post(routes.AccountController.login().url)
      .formParamMap(Map(
        Form.USERNAME -> "${%s}".format(Test.TEST_SELLER_USERNAME),
        Form.PASSWORD -> "${%s}".format(Test.TEST_SELLER_PASSWORD),
        Form.PUSH_NOTIFICATION_TOKEN -> "",
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
    .pause(3)
    .exec(http("Add_Trader_Form_GET")
      .get(routes.SetACLController.addTraderForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN))
    )
    .pause(2)
    .feed(NameFeeder.nameFeed)
    .exec(http("Add_Seller_Trader_Form_POST")
      .post(routes.SetACLController.addTrader().url)
      .formParamMap(Map(
        Form.CSRF_TOKEN-> "${%s}".format(Form.CSRF_TOKEN),
        Form.ZONE_ID-> "${%s}".format(Test.TEST_ZONE_ID),
        Form.ORGANIZATION_ID -> "${%s}".format(Test.TEST_ORGANIZATION_ID),
        Form.NAME -> "${%s}".format(Test.TEST_NAME)
      ))
    )
    .pause(2)
    .foreach(setACLControllerTest.documentTypeSeq,"documentType"){
      feed(ImageFeeder.imageFeed)
        .exec(http("SELLER_TRADER_KYC_UPLOAD_"+"${documentType}"+"_FORM")
          .get("/master/userUploadTraderKYCForm?documentType="+"${documentType}")
          .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN))
        )
        .pause(3)
        .exec(http("SELLER_TRADER_KYC_UPLOAD_"+"${documentType}")
          .post("/master/userUploadTraderKYC?documentType="+"${documentType}")
          .formParamMap(Map(
            Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
            "resumableChunkNumber" -> "1",
            "resumableChunkSize" -> "1048576",
            "resumableTotalSize" -> "${%s}".format(Test.TEST_FILE_SIZE),
            "resumableIdentifier" -> "document",
            "resumableFilename" -> "${%s}".format(Test.TEST_FILE_NAME)))
          .bodyPart(RawFileBodyPart("file", "/root/IdeaProjects/commitCentral/gatling/simulation/images/"+"${%s}".format(Test.TEST_FILE_NAME))
            .transferEncoding("binary")).asMultipartForm)
        .exec(
          http("STORE_SELLER_TRADER_KYC_"+"${documentType}")
            .get("/master/userStoreTraderKYC?name="+"${%s}".format(Test.TEST_FILE_NAME)+"&documentType="+"${documentType}")
        )
        .pause(2)
    }
    .exec(http("USER_REVIEW_ADD_SELLER_TRADER_REQUEST_FORM")
      .get(routes.SetACLController.userReviewAddTraderRequestForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN))
    )
    .pause(1)
    .exec(http("USER_REVIEW_ADD_SELLER_TRADER_REQUEST")
      .post(routes.SetACLController.userReviewAddTraderRequest().url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        Form.COMPLETION -> true
      ))
    )
}

object ApproveSellerTraderACL {

  val approveSellerTraderACL = scenario("approveSellerTraderACL")
    .exec(http("LoginZone_GET")
      .get(routes.AccountController.loginForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .exec(http("LoginZone_POST")
      .post(routes.AccountController.login().url)
      .formParamMap(Map(
        Form.USERNAME ->  "${%s}".format(Test.TEST_ZONE_USERNAME),
        Form.PASSWORD ->  "${%s}".format(Test.TEST_ZONE_PASSWORD),
        Form.PUSH_NOTIFICATION_TOKEN -> "",
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
    .pause(3)
    .exec { session => session.set(Test.TEST_TRADER_ID, setACLControllerTest.getTraderID(session(Test.TEST_SELLER_USERNAME).as[String])) }
    .doIf(session => session(Test.TEST_TRADER_ID).as[String] == "0") {
      asLongAs(session => session(Test.TEST_TRADER_ID).as[String] == "0") {
        pause(1)
          .exec { session => session.set(Test.TEST_TRADER_ID, setACLControllerTest.getTraderID(session(Test.TEST_SELLER_USERNAME).as[String])) }
      }
    }
    .exec(http("Get_Zone_Pending_Verify_Trader_Request")
      .get(routes.SetACLController.zoneViewPendingVerifyTraderRequests().url)
    )
    .foreach(setACLControllerTest.documentTypeSeq,"documentType"){
      exec(http("Zone_Trader_KYC_update_Status"+"${documentType}")
        .post(routes.SetACLController.updateTraderKYCDocumentZoneStatus().url)
        .formParamMap(Map(
          Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
          Form.TRADER_ID -> "${%s}".format(Test.TEST_TRADER_ID),
          Form.DOCUMENT_TYPE -> "${documentType}",
          Form.STATUS -> true
        ))
      )
        .pause(1)
    }
    .exec(http("ZoneVerifySellerTrader_GET")
      .get(routes.SetACLController.zoneVerifyTraderForm("${%s}".format(Test.TEST_SELLER_USERNAME),"${%s}".format(Test.TEST_ORGANIZATION_ID)).url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .feed(GasFeeder.gasFeed)
    .exec(http("ZoneVerifySellerTrader_POST")
      .post(routes.SetACLController.zoneVerifyTrader().url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        Form.ACCOUNT_ID-> "${%s}".format(Test.TEST_SELLER_USERNAME),
        Form.ORGANIZATION_ID -> "${%s}".format(Test.TEST_ORGANIZATION_ID),
        Form.ISSUE_ASSET_ACL -> setACLPrivileges.issueAsset, Form.ISSUE_FIAT_ACL -> setACLPrivileges.issueFiat, Form.SEND_ASSET_ACL -> setACLPrivileges.sendAsset, Form.SEND_FIAT_ACL -> setACLPrivileges.sendFiat, Form.REDEEM_ASSET_ACL -> setACLPrivileges.redeemAsset, Form.REDEEM_FIAT_ACL -> setACLPrivileges.redeemFiat, Form.SELLER_EXECUTE_ORDER_ACL -> setACLPrivileges.sellerExecuteOrder, Form.BUYER_EXECUTE_ORDER_ACL -> setACLPrivileges.buyerExecuteOrder, Form.CHANGE_BUYER_BID_ACL -> setACLPrivileges.changeBuyerBid, Form.CHANGE_SELLER_BID_ACL -> setACLPrivileges.changeSellerBid, Form.CONFIRM_BUYER_BID_ACL -> setACLPrivileges.confirmBuyerBid, Form.CONFIRM_SELLER_BID_ACL -> setACLPrivileges.confirmSellerBid, Form.NEGOTIATION_ACL -> setACLPrivileges.negotiation, Form.RELEASE_ASSET_ACL -> setACLPrivileges.releaseAsset,
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Form.PASSWORD -> "${%s}".format(Test.TEST_ZONE_PASSWORD)
      ))
      .check(status.is(412))
    )
    .pause(2)
    .exec(http("LoginOrganization_GET")
      .get(routes.AccountController.loginForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .exec(http("LoginOrganization_POST")
      .post(routes.AccountController.login().url)
      .formParamMap(Map(
        Form.USERNAME -> "${%s}".format(Test.TEST_ORGANIZATION_USERNAME),
        Form.PASSWORD -> "${%s}".format(Test.TEST_ORGANIZATION_PASSWORD),
        Form.PUSH_NOTIFICATION_TOKEN -> "",
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
    .pause(3)
    .exec(http("Get_Organization_Pending_Verify_Trader_Request")
      .get(routes.SetACLController.organizationViewPendingVerifyTraderRequests().url)
    )
    .foreach(setACLControllerTest.documentTypeSeq,"documentType"){
      exec(http("Organization_Trader_KYC_update_Status"+"${documentType}")
        .post(routes.SetACLController.updateTraderKYCDocumentOrganizationStatus().url)
        .formParamMap(Map(
          Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
          Form.TRADER_ID -> "${%s}".format(Test.TEST_TRADER_ID),
          Form.DOCUMENT_TYPE -> "${documentType}",
          Form.STATUS -> true
        ))
      )
        .pause(1)
    }
    .exec(http("Organization_Verify_Trader_GET")
      .get(routes.SetACLController.organizationVerifyTraderForm("${%s}".format(Test.TEST_SELLER_USERNAME),"${%s}".format(Test.TEST_ORGANIZATION_ID)).url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .feed(GasFeeder.gasFeed)
    .exec(http("Organization_Verify_Trader_POST")
      .post(routes.SetACLController.organizationVerifyTrader().url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        Form.ACCOUNT_ID-> "${%s}".format(Test.TEST_SELLER_USERNAME),
        Form.ORGANIZATION_ID -> "${%s}".format(Test.TEST_ORGANIZATION_ID),
        Form.ISSUE_ASSET_ACL -> setACLPrivileges.issueAsset, Form.ISSUE_FIAT_ACL -> setACLPrivileges.issueFiat, Form.SEND_ASSET_ACL -> setACLPrivileges.sendAsset, Form.SEND_FIAT_ACL -> setACLPrivileges.sendFiat, Form.REDEEM_ASSET_ACL -> setACLPrivileges.redeemAsset, Form.REDEEM_FIAT_ACL -> setACLPrivileges.redeemFiat, Form.SELLER_EXECUTE_ORDER_ACL -> setACLPrivileges.sellerExecuteOrder, Form.BUYER_EXECUTE_ORDER_ACL -> setACLPrivileges.buyerExecuteOrder, Form.CHANGE_BUYER_BID_ACL -> setACLPrivileges.changeBuyerBid, Form.CHANGE_SELLER_BID_ACL -> setACLPrivileges.changeSellerBid, Form.CONFIRM_BUYER_BID_ACL -> setACLPrivileges.confirmBuyerBid, Form.CONFIRM_SELLER_BID_ACL -> setACLPrivileges.confirmSellerBid, Form.NEGOTIATION_ACL -> setACLPrivileges.negotiation, Form.RELEASE_ASSET_ACL -> setACLPrivileges.releaseAsset,
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Form.PASSWORD -> "${%s}".format(Test.TEST_ORGANIZATION_PASSWORD)
      )))
    .pause(2)
}

object SignUpAndLoginBuyerAndCreateFaucetRequest{

  val signUpAndLoginBuyerAndCreateFaucetRequest=scenario("createBuyer")
    .feed(BuyerFeeder.buyerFeed)
    .exec(http("BuyerSignUp_GET")
      .get(routes.AccountController.signUpForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(5)
    .exec(http("BuyerSignUp_POST")
      .post(routes.AccountController.signUp().url)
      .formParamMap(Map(
        Form.USERNAME -> "${%s}".format(Test.TEST_BUYER_USERNAME),
        Form.USERNAME_AVAILABLE -> true,
        Form.PASSWORD -> "${%s}".format(Test.TEST_BUYER_PASSWORD),
        Form.CONFIRM_PASSWORD -> "${%s}".format(Test.TEST_BUYER_PASSWORD),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
    .pause(2)
    .exec(http("LoginBuyer_GET")
      .get(routes.AccountController.loginForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .exec(http("LoginBuyer_POST")
      .post(routes.AccountController.login().url)
      .formParamMap(Map(
        Form.USERNAME -> "${%s}".format(Test.TEST_BUYER_USERNAME),
        Form.PASSWORD -> "${%s}".format(Test.TEST_BUYER_PASSWORD),
        Form.PUSH_NOTIFICATION_TOKEN -> "",
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
    .pause(5)
    .feed(CouponFeeder.couponFeed)
    .exec(http("RequestCoinBuyer_GET")
      .get(routes.SendCoinController.faucetRequestForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("RequestCoinBuyer_POST")
      .post(routes.SendCoinController.faucetRequest().url)
      .formParamMap(Map(
        Form.COUPON -> "${%s}".format(Test.TEST_COUPON),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
    .pause(2)
}

object ApproveFaucetRequestBuyer{

  val approveFaucetRequestBuyer=scenario("masterLoginMainAndApproveFaucetRequestBuyer")
    .feed(GenesisFeeder.genesisFeed)
    .feed(GasFeeder.gasFeed)
    .exec(http("MainLogin_GET")
      .get(routes.AccountController.loginForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .exec(http("MainLogin_POST")
      .post(routes.AccountController.login().url)
      .formParamMap(Map(
        Form.USERNAME -> "${%s}".format(Test.TEST_MAIN_USERNAME),
        Form.PASSWORD -> "${%s}".format(Test.TEST_MAIN_PASSWORD),
        Form.PUSH_NOTIFICATION_TOKEN -> "",
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
    .pause(2)
    .exec { session => session.set(Test.TEST_REQUEST_ID, getRequestIDForFaucetRequest(session(Test.TEST_BUYER_USERNAME).as[String])) }
    .doIf(session=> session(Test.TEST_REQUEST_ID).as[String] == "0") {
      asLongAs(session=> session(Test.TEST_REQUEST_ID).as[String] =="0") {
        pause(1)
          .exec { session => session.set(Test.TEST_REQUEST_ID, getRequestIDForFaucetRequest(session(Test.TEST_BUYER_USERNAME).as[String])) }
      }
    }
    .exec(http("ApproveFaucetRequestBuyerTrader_GET")
      .get("/master/approveFaucetRequests?requestID=" + "${%s}".format(Test.TEST_REQUEST_ID) + "&accountID=" + Test.TEST_BUYER_USERNAME)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("ApproveFaucetRequestBuyerTrader_POST")
      .post(routes.SendCoinController.approveFaucetRequests().url)
      .formParamMap(Map(
        Form.REQUEST_ID -> "${%s}".format(Test.TEST_REQUEST_ID),
        Form.ACCOUNT_ID -> "${%s}".format(Test.TEST_BUYER_USERNAME),
        Form.PASSWORD -> "${%s}".format(Test.TEST_MAIN_PASSWORD),
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
    .pause(5)
    .exec{session => session.set(Test.USER_TYPE, sendCoinControllerTest.getUserType(session(Test.TEST_BUYER_USERNAME).as[String])) }
    .doIf(session=> session(Test.USER_TYPE).as[String] != "USER") {
      asLongAs(session=> session(Test.USER_TYPE).as[String] != "USER") {
        pause(1)
          .exec { session =>session.set(Test.USER_TYPE, sendCoinControllerTest.getUserType(session(Test.TEST_BUYER_USERNAME).as[String])) }
      }
    }
}

object AddBuyerTraderRequest{

  val addBuyerTraderRequest=scenario("addBuyerTraderRequest")
    .exec(http("LoginBuyer_GET")
      .get(routes.AccountController.loginForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .exec(http("LoginBuyer_POST")
      .post(routes.AccountController.login().url)
      .formParamMap(Map(
        Form.USERNAME -> "${%s}".format(Test.TEST_BUYER_USERNAME),
        Form.PASSWORD -> "${%s}".format(Test.TEST_BUYER_PASSWORD),
        Form.PUSH_NOTIFICATION_TOKEN -> "",
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
    .pause(3)
    .exec(http("Add_Buyer_Trader_Form_GET")
      .get(routes.SetACLController.addTraderForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN))
    )
    .pause(2)
    .feed(NameFeeder.nameFeed)
    .exec(http("Add_Buyer_Trader_Form_POST")
      .post(routes.SetACLController.addTrader().url)
      .formParamMap(Map(
        Form.CSRF_TOKEN-> "${%s}".format(Form.CSRF_TOKEN),
        Form.ZONE_ID-> "${%s}".format(Test.TEST_ZONE_ID),
        Form.ORGANIZATION_ID -> "${%s}".format(Test.TEST_ORGANIZATION_ID),
        Form.NAME -> "${%s}".format(Test.TEST_NAME)
      ))
    )
    .pause(2)
    .foreach(setACLControllerTest.documentTypeSeq,"documentType"){
      feed(ImageFeeder2.imageFeed2)
        .exec(http("Buyer_TRADER_KYC_UPLOAD_"+"${documentType}"+"_FORM")
          .get("/master/userUploadTraderKYCForm?documentType="+"${documentType}")
          .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN))
        )
        .pause(3)
        .exec(http("Buyer_TRADER_KYC_UPLOAD_"+"${documentType}")
          .post("/master/userUploadTraderKYC?documentType="+"${documentType}")
          .formParamMap(Map(
            Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
            "resumableChunkNumber" -> "1",
            "resumableChunkSize" -> "1048576",
            "resumableTotalSize" -> "${%s}".format(Test.TEST_FILE_SIZE),
            "resumableIdentifier" -> "document",
            "resumableFilename" -> "${%s}".format(Test.TEST_FILE_NAME)))
          .bodyPart(RawFileBodyPart("file", "/root/IdeaProjects/commitCentral/gatling/simulation/images/"+"${%s}".format(Test.TEST_FILE_NAME))
            .transferEncoding("binary")).asMultipartForm)
        .exec(
          http("STORE_Buyer_TRADER_KYC_"+"${documentType}")
            .get("/master/userStoreTraderKYC?name="+"${%s}".format(Test.TEST_FILE_NAME)+"&documentType="+"${documentType}")
        )
        .pause(2)
    }
    .exec(http("USER_REVIEW_ADD_Buyer_TRADER_REQUEST_FORM")
      .get(routes.SetACLController.userReviewAddTraderRequestForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN))
    )
    .pause(1)
    .exec(http("USER_REVIEW_ADD_Buyer_TRADER_REQUEST")
      .post(routes.SetACLController.userReviewAddTraderRequest().url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        Form.COMPLETION -> true
      ))
    )
}

object ApproveBuyerTraderACL {

  val approveBuyerTraderACL = scenario("approveBuyerTraderACL")
    .exec(http("LoginZone_GET")
      .get(routes.AccountController.loginForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .exec(http("LoginZone_POST")
      .post(routes.AccountController.login().url)
      .formParamMap(Map(
        Form.USERNAME ->  "${%s}".format(Test.TEST_ZONE_USERNAME),
        Form.PASSWORD ->  "${%s}".format(Test.TEST_ZONE_PASSWORD),
        Form.PUSH_NOTIFICATION_TOKEN -> "",
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
    .pause(3)
    .exec { session => session.set(Test.TEST_TRADER_ID, setACLControllerTest.getTraderID(session(Test.TEST_BUYER_USERNAME).as[String])) }
    .doIf(session => session(Test.TEST_TRADER_ID).as[String] == "0") {
      asLongAs(session => session(Test.TEST_TRADER_ID).as[String] == "0") {
        pause(1)
          .exec { session => session.set(Test.TEST_TRADER_ID, setACLControllerTest.getTraderID(session(Test.TEST_BUYER_USERNAME).as[String])) }
      }
    }
    .exec(http("Get_Zone_Pending_Verify_Buyer_Trader_Request")
      .get(routes.SetACLController.zoneViewPendingVerifyTraderRequests().url)
    )
    .foreach(setACLControllerTest.documentTypeSeq,"documentType"){
      exec(http("Zone_Buyer_Trader_KYC_update_Status"+"${documentType}")
        .post(routes.SetACLController.updateTraderKYCDocumentZoneStatus().url)
        .formParamMap(Map(
          Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
          Form.TRADER_ID -> "${%s}".format(Test.TEST_TRADER_ID),
          Form.DOCUMENT_TYPE -> "${documentType}",
          Form.STATUS -> true
        ))
      )
        .pause(1)
    }
    .exec(http("ZoneVerifyBuyerTrader_GET")
      .get(routes.SetACLController.zoneVerifyTraderForm("${%s}".format(Test.TEST_BUYER_USERNAME),"${%s}".format(Test.TEST_ORGANIZATION_ID)).url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .feed(GasFeeder.gasFeed)
    .exec(http("ZoneVerifyBuyerTrader_POST")
      .post(routes.SetACLController.zoneVerifyTrader().url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        Form.ACCOUNT_ID-> "${%s}".format(Test.TEST_BUYER_USERNAME),
        Form.ORGANIZATION_ID -> "${%s}".format(Test.TEST_ORGANIZATION_ID),
        Form.ISSUE_ASSET_ACL -> setACLPrivileges.issueAsset, Form.ISSUE_FIAT_ACL -> setACLPrivileges.issueFiat, Form.SEND_ASSET_ACL -> setACLPrivileges.sendAsset, Form.SEND_FIAT_ACL -> setACLPrivileges.sendFiat, Form.REDEEM_ASSET_ACL -> setACLPrivileges.redeemAsset, Form.REDEEM_FIAT_ACL -> setACLPrivileges.redeemFiat, Form.SELLER_EXECUTE_ORDER_ACL -> setACLPrivileges.sellerExecuteOrder, Form.BUYER_EXECUTE_ORDER_ACL -> setACLPrivileges.buyerExecuteOrder, Form.CHANGE_BUYER_BID_ACL -> setACLPrivileges.changeBuyerBid, Form.CHANGE_SELLER_BID_ACL -> setACLPrivileges.changeSellerBid, Form.CONFIRM_BUYER_BID_ACL -> setACLPrivileges.confirmBuyerBid, Form.CONFIRM_SELLER_BID_ACL -> setACLPrivileges.confirmSellerBid, Form.NEGOTIATION_ACL -> setACLPrivileges.negotiation, Form.RELEASE_ASSET_ACL -> setACLPrivileges.releaseAsset,
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Form.PASSWORD -> "${%s}".format(Test.TEST_ZONE_PASSWORD)
      ))
      .check(status.is(412))
    )
    .pause(2)
    .exec(http("LoginOrganization_GET")
      .get(routes.AccountController.loginForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .exec(http("LoginOrganization_POST")
      .post(routes.AccountController.login().url)
      .formParamMap(Map(
        Form.USERNAME -> "${%s}".format(Test.TEST_ORGANIZATION_USERNAME),
        Form.PASSWORD ->"${%s}".format(Test.TEST_ORGANIZATION_PASSWORD),
        Form.PUSH_NOTIFICATION_TOKEN -> "",
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
    .pause(3)
    .exec(http("Get_Organization_Pending_Verify_Buyer_Trader_Request")
      .get(routes.SetACLController.organizationViewPendingVerifyTraderRequests().url)
    )
    .foreach(setACLControllerTest.documentTypeSeq,"documentType"){
      exec(http("Organization_Buyer_Trader_KYC_update_Status"+"${documentType}")
        .post(routes.SetACLController.updateTraderKYCDocumentOrganizationStatus().url)
        .formParamMap(Map(
          Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
          Form.TRADER_ID -> "${%s}".format(Test.TEST_TRADER_ID),
          Form.DOCUMENT_TYPE -> "${documentType}",
          Form.STATUS -> true
        ))
      )
        .pause(1)
    }
    .exec(http("Organization_Verify_Buyer_Trader_GET")
      .get(routes.SetACLController.organizationVerifyTraderForm("${%s}".format(Test.TEST_BUYER_USERNAME),"${%s}".format(Test.TEST_ORGANIZATION_ID)).url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .feed(GasFeeder.gasFeed)
    .exec(http("Organization_Verify_Buyer_Trader_POST")
      .post(routes.SetACLController.organizationVerifyTrader().url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        Form.ACCOUNT_ID-> "${%s}".format(Test.TEST_BUYER_USERNAME),
        Form.ORGANIZATION_ID -> "${%s}".format(Test.TEST_ORGANIZATION_ID),
        Form.ISSUE_ASSET_ACL -> setACLPrivileges.issueAsset, Form.ISSUE_FIAT_ACL -> setACLPrivileges.issueFiat, Form.SEND_ASSET_ACL -> setACLPrivileges.sendAsset, Form.SEND_FIAT_ACL -> setACLPrivileges.sendFiat, Form.REDEEM_ASSET_ACL -> setACLPrivileges.redeemAsset, Form.REDEEM_FIAT_ACL -> setACLPrivileges.redeemFiat, Form.SELLER_EXECUTE_ORDER_ACL -> setACLPrivileges.sellerExecuteOrder, Form.BUYER_EXECUTE_ORDER_ACL -> setACLPrivileges.buyerExecuteOrder, Form.CHANGE_BUYER_BID_ACL -> setACLPrivileges.changeBuyerBid, Form.CHANGE_SELLER_BID_ACL -> setACLPrivileges.changeSellerBid, Form.CONFIRM_BUYER_BID_ACL -> setACLPrivileges.confirmBuyerBid, Form.CONFIRM_SELLER_BID_ACL -> setACLPrivileges.confirmSellerBid, Form.NEGOTIATION_ACL -> setACLPrivileges.negotiation, Form.RELEASE_ASSET_ACL -> setACLPrivileges.releaseAsset,
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Form.PASSWORD -> "${%s}".format(Test.TEST_ORGANIZATION_PASSWORD)
      )))
    .pause(2)
}

object IssueAssetRequestForSeller{

  val issueAssetRequestForSeller=scenario("issueAssetRequestForSeller")
    .exec{session => session.set(Test.USER_TYPE, sendCoinControllerTest.getUserType(session(Test.TEST_SELLER_USERNAME).as[String])) }
    .doIf(session=> session(Test.USER_TYPE).as[String] != "TRADER") {
      asLongAs(session=> session(Test.USER_TYPE).as[String] != "TRADER") {
        pause(1)
          .exec { session =>session.set(Test.USER_TYPE, sendCoinControllerTest.getUserType(session(Test.TEST_SELLER_USERNAME).as[String]))}
      }
    }
    .exec(http("LoginSeller_GET")
      .get(routes.AccountController.loginForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .exec(http("LoginSeller_POST")
      .post(routes.AccountController.login().url)
      .formParamMap(Map(
        Form.USERNAME -> "${%s}".format(Test.TEST_SELLER_USERNAME),
        Form.PASSWORD -> "${%s}".format(Test.TEST_SELLER_PASSWORD),
        Form.PUSH_NOTIFICATION_TOKEN -> "",
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
    .pause(3)
    .feed(AssetTypeFeeder.assetTypeFeed)
    .feed(AssetPriceFeeder.assetPriceFeed)
    .feed(QuantityUnitFeeder.quantityUnitFeed)
    .feed(AssetQuantityFeeder.assetQuantityFeed)
    .exec(http("IssueAssetRequest_GET")
      .get(routes.IssueAssetController.issueAssetDetailForm(None).url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("IssueAssetRequest_POST")
      .post(routes.IssueAssetController.issueAssetDetail().url)
      .formParamMap(Map(
        Form.REQUEST_ID -> "",
        constants.FormField.ASSET_TYPE.name -> "${%s}".format(Test.TEST_ASSET_TYPE),
        constants.FormField.QUANTITY_UNIT.name -> "${%s}".format(Test.TEST_QUANTITY_UNIT),
        constants.FormField.ASSET_QUANTITY.name -> "${%s}".format(Test.TEST_ASSET_QUANTITY),
        constants.FormField.ASSET_PRICE.name -> "${%s}".format(Test.TEST_ASSET_PRICE),
        constants.FormField.TAKER_ADDRESS.name -> "",
        constants.FormField.COMMODITY_NAME.name -> "AEFRGEAR",
        constants.FormField.QUALITY.name -> "A",
        constants.FormField.DELIVERY_TERM.name -> "FOB",
        constants.FormField.TRADE_TYPE.name -> "POST TRADE",
        constants.FormField.PORT_OF_LOADING.name -> "mumbai",
        constants.FormField.PORT_OF_DISCHARGE.name -> "shanghai",
        constants.FormField.SHIPMENT_DATE.name -> "2019-11-11",
        constants.FormField.PHYSICAL_DOCUMENTS_HANDLED_VIA.name -> "COMDEX",
        constants.FormField.COMDEX_PAYMENT_TERMS.name -> "BOTH_PARTIES",
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
    .pause(3)
    .exec{session=> session.set(Test.TEST_REQUEST_ID, getRequestIDForIssueAsset(session(Test.TEST_SELLER_USERNAME).as[String]))}
    .exec(http("Upload OBL Form")
      .get(routes.FileController.uploadTraderAssetForm("OBL","${TEST_REQUEST_ID}").url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN))
    )
    .pause(2)
    .feed(ImageFeeder3.imageFeed3)
    .exec(http("Issue Asset Upload OBL")
      .post(routes.FileController.uploadTraderAsset("OBL").url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        "resumableChunkNumber" -> "1",
        "resumableChunkSize" -> "1048576",
        "resumableTotalSize" -> "${%s}".format(Test.TEST_FILE_SIZE),
        "resumableIdentifier" -> "document",
        "resumableFilename" -> "${%s}".format(Test.TEST_FILE_NAME)))
      .bodyPart(RawFileBodyPart("file", "/root/IdeaProjects/commitCentral/gatling/simulation/images/"+"${%s}".format(Test.TEST_FILE_NAME))
        .transferEncoding("binary")).asMultipartForm)
    .exec(
      http("STORE_AssetDocument_OBL")
        .get("/traderUpload/storeAssetDocument?name="+"${%s}".format(Test.TEST_FILE_NAME)+"&documentType="+"OBL&issueAssetRequestID="+"${%s}".format(Test.TEST_REQUEST_ID))
        .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN))
    )
    .pause(2)
    .exec(http("issue Asset OBL Post")
      .post(routes.IssueAssetController.issueAssetOBL().url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        constants.FormField.REQUEST_ID.name -> "${%s}".format(Test.TEST_REQUEST_ID),
        constants.FormField.BILL_OF_LADING_NUMBER.name -> "fghfg",
        constants.FormField.PORT_OF_LOADING.name -> "dgdgd",
        constants.FormField.SHIPPER_NAME.name -> "srgsdg",
        constants.FormField.SHIPPER_ADDRESS.name -> "srgsdg",
        constants.FormField.NOTIFY_PARTY_NAME.name ->"srgsdg",
        constants.FormField.NOTIFY_PARTY_ADDRESS.name -> "srgsdg",
        constants.FormField.SHIPMENT_DATE.name ->"2019-11-11",
        constants.FormField.DELIVERY_TERM.name -> "FOB",
        constants.FormField.ASSET_QUANTITY.name -> "123",
        constants.FormField.ASSET_PRICE.name -> "123",
      ))
    )
    .pause(2)
    .feed(ImageFeeder2.imageFeed2)
    .exec(http("issue Asset Upload Invoice")
      .post(routes.FileController.uploadTraderAsset("INVOICE").url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        "resumableChunkNumber" -> "1",
        "resumableChunkSize" -> "1048576",
        "resumableTotalSize" -> "${%s}".format(Test.TEST_FILE_SIZE),
        "resumableIdentifier" -> "document",
        "resumableFilename" -> "${%s}".format(Test.TEST_FILE_NAME)))
      .bodyPart(RawFileBodyPart("file", "/root/IdeaProjects/commitCentral/gatling/simulation/images/"+"${%s}".format(Test.TEST_FILE_NAME))
        .transferEncoding("binary")).asMultipartForm)
    .exec(
      http("STORE_AssetDocument_INVOICE")
        .get("/traderUpload/storeAssetDocument?name="+"${%s}".format(Test.TEST_FILE_NAME)+"&documentType="+"INVOICE&"+"issueAssetRequestID"+"="+"${%s}".format(Test.TEST_REQUEST_ID))
        .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN))
    )
    .pause(2)
    .exec(http("issue Asset Invoice post")
      .post(routes.IssueAssetController.issueAssetInvoice().url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        constants.FormField.REQUEST_ID.name -> "${%s}".format(Test.TEST_REQUEST_ID),
        constants.FormField.INVOICE_NUMBER.name ->"sdfgsdvsrdvsrev",
        constants.FormField.INVOICE_DATE.name -> "2019-11-11",
      ))
    )
    .foreach(issueAssetControllerTest.issueAssetDocumentType,"documentType"){
      feed(ImageFeeder.imageFeed)
        .exec(http("IssueAsset_UPLOAD_"+"${documentType}"+"_FORM")
          .get("/traderUpload/assetForm?documentType="+"${documentType}"+"&issueAssetRequestID="+"${%s}".format(Test.TEST_REQUEST_ID))
          .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN))
        )
        .pause(3)
        .exec(http("IssueAsset_UPLOAD_"+"${documentType}")
          .post("/traderUpload/assetDocument?documentType="+"${documentType}")
          .formParamMap(Map(
            Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
            "resumableChunkNumber" -> "1",
            "resumableChunkSize" -> "1048576",
            "resumableTotalSize" -> "${%s}".format(Test.TEST_FILE_SIZE),
            "resumableIdentifier" -> "document",
            "resumableFilename" -> "${%s}".format(Test.TEST_FILE_NAME)))
          .bodyPart(RawFileBodyPart("file", "/root/IdeaProjects/commitCentral/gatling/simulation/images/"+"${%s}".format(Test.TEST_FILE_NAME))
            .transferEncoding("binary")).asMultipartForm)
        .exec(
          http("STORE_AssetDocument_"+"${documentType}")
            .get("/traderUpload/storeAssetDocument?name="+"${%s}".format(Test.TEST_FILE_NAME)+"&documentType="+"${documentType}"+"&issueAssetRequestID="+"${%s}".format(Test.TEST_REQUEST_ID))
        )
        .pause(2)
    }
    .pause(1)
    .exec(http("issue_Asset_Request_Form")
      .get("/master/issueAssetRequest?id="+"${%s}".format(Test.TEST_REQUEST_ID))
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN))
    )
    .pause(1)
    .exec(http("issue_Asset_Request_Submit")
      .post(routes.IssueAssetController.issueAssetRequest().url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        Form.REQUEST_ID -> "${%s}".format(Test.TEST_REQUEST_ID),
      ))
    )
    .pause(2)
}

object ApproveIssueAsset{

  val approveIssueAsset=scenario("approveIssueAsset")
    .exec(http("LoginZone_GET")
      .get(routes.AccountController.loginForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .exec(http("LoginZone_POST")
      .post(routes.AccountController.login().url)
      .formParamMap(Map(
        Form.USERNAME ->  "${%s}".format(Test.TEST_ZONE_USERNAME),
        Form.PASSWORD ->  "${%s}".format(Test.TEST_ZONE_PASSWORD),
        Form.PUSH_NOTIFICATION_TOKEN -> "",
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
    .pause(3)
    .exec(http("Get_Pending_Issue_Asset_Request")
      .get(routes.IssueAssetController.viewPendingIssueAssetRequests().url)
    )
    .pause(2)
    .foreach(allIssueAssetDocumentType,"documentType"){
      exec(http("update_Asset_Document_Status_"+"${documentType}")
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
    .exec(session=> session.set(Test.TEST_DOCUMENT_HASH,getDocumentHashForIssueAsset(session(Test.TEST_SELLER_USERNAME).as[String])).set(Test.TEST_ASSET_TYPE,getAssetTypeForIssueAsset(session(Test.TEST_SELLER_USERNAME).as[String])).set(Test.TEST_ASSET_PRICE,getAssetPriceForIssueAsset(session(Test.TEST_SELLER_USERNAME).as[String])).set(Test.TEST_QUANTITY_UNIT,getQuantityUnitForIssueAsset(session(Test.TEST_SELLER_USERNAME).as[String])).set(Test.TEST_ASSET_QUANTITY,getAssetQuantityForIssueAsset(session(Test.TEST_SELLER_USERNAME).as[String])))

    .exec(http("Issue Asset Form")
      .get("/master/issueAsset?requestID="+"${%s}".format(Test.TEST_REQUEST_ID)+"&accountID="+"${%s}".format(Test.TEST_SELLER_USERNAME)+"&documentHash="+"${%s}".format(Test.TEST_DOCUMENT_HASH)+"&assetType="+"${%s}".format(Test.TEST_ASSET_TYPE)+"&assetPrice="+"${%s}".format(Test.TEST_ASSET_PRICE)+"&quantityUnit="+"${%s}".format(Test.TEST_QUANTITY_UNIT)+"&assetQuantity="+"${%s}".format(Test.TEST_ASSET_QUANTITY))
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN))
      //.get(routes.IssueAssetController.issueAssetForm("${%s}".format(Test.TEST_REQUEST_ID),"${%s}".format(Test.TEST_USERNAME),"${%s}".format(Test.TEST_DOCUMENT_HASH),"${%s}".format(Test.TEST_ASSET_TYPE),("${%s}".format(Test.TEST_ASSET_PRICE)).toInt,"${%s}".format(Test.TEST_QUANTITY_UNIT),"${%s}".format(Test.TEST_ASSET_QUANTITY),None).url)
    )
    .pause(2)
    .feed(GasFeeder.gasFeed)
    .exec(http("issueAsset")
      .post(routes.IssueAssetController.issueAsset().url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        Form.REQUEST_ID -> "${%s}".format(Test.TEST_REQUEST_ID),
        Form.ACCOUNT_ID ->"${%s}".format(Test.TEST_SELLER_USERNAME),
        Form.DOCUMENT_HASH -> "${%s}".format(Test.TEST_DOCUMENT_HASH),
        constants.FormField.ASSET_TYPE.name -> "${%s}".format(Test.TEST_ASSET_TYPE),
        constants.FormField.ASSET_PRICE.name -> "${%s}".format(Test.TEST_ASSET_PRICE),
        constants.FormField.QUANTITY_UNIT.name -> "${%s}".format(Test.TEST_QUANTITY_UNIT),
        constants.FormField.ASSET_QUANTITY.name -> "${%s}".format(Test.TEST_ASSET_QUANTITY),
        Form.TAKER_ADDRESS-> "",
        Form.GAS->"${%s}".format(Test.TEST_GAS),
        Form.PASSWORD -> "${%s}".format(Test.TEST_ZONE_PASSWORD)
      ))
    )
    .pause(2)
}

object IssueFiatRequestBuyer{

  val issueFiatRequest=scenario("IssueFiatRequest")
    .exec{session => session.set(Test.USER_TYPE, sendCoinControllerTest.getUserType(session(Test.TEST_BUYER_USERNAME).as[String])) }
    .doIf(session=> session(Test.USER_TYPE).as[String] != "TRADER") {
      asLongAs(session=> session(Test.USER_TYPE).as[String] != "TRADER") {
        pause(1)
          .exec { session =>session.set(Test.USER_TYPE, sendCoinControllerTest.getUserType(session(Test.TEST_BUYER_USERNAME).as[String])) }
      }
    }
    .exec(http("LoginBuyer_GET")
      .get(routes.AccountController.loginForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .exec(http("LoginBuyer_POST")
      .post(routes.AccountController.login().url)
      .formParamMap(Map(
        Form.USERNAME -> "${%s}".format(Test.TEST_BUYER_USERNAME),
        Form.PASSWORD -> "${%s}".format(Test.TEST_BUYER_PASSWORD),
        Form.PUSH_NOTIFICATION_TOKEN -> "",
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
    .pause(3)
    .feed(TransactionIDFeeder.transactionIDFeed)
    .feed(TransactionAmountFeeder.transactionAmountFeed)
    .exec(http("IssueFiatRequest_GET")
      .get(routes.IssueFiatController.issueFiatRequestForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("IssueFiatRequest_POST")
      .post(routes.IssueFiatController.issueFiatRequest().url)
      .formParamMap(Map(
        Form.TRANSACTION_ID -> "${%s}".format(Test.TEST_TRANSACTION_ID),
        Form.TRANSACTION_AMOUNT -> "${%s}".format(Test.TEST_TRANSACTION_AMOUNT),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
    .pause(2)
}

object IssueFiat{
  val issueFiat=scenario("issueFiat")
    .exec(http("LoginZone_GET")
      .get(routes.AccountController.loginForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .exec(http("LoginZone_POST")
      .post(routes.AccountController.login().url)
      .formParamMap(Map(
        Form.USERNAME ->  "${%s}".format(Test.TEST_ZONE_USERNAME),
        Form.PASSWORD ->  "${%s}".format(Test.TEST_ZONE_PASSWORD),
        Form.PUSH_NOTIFICATION_TOKEN -> "",
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
    .pause(3)
    .feed(GasFeeder.gasFeed)
    .exec(session=> session.set(Test.TEST_REQUEST_ID,getRequestIDForIssueFiat(session(Test.TEST_BUYER_USERNAME).as[String])).set(Test.TEST_TRANSACTION_ID,getTransactionIDForIssueFiat(session(Test.TEST_BUYER_USERNAME).as[String])).set(Test.TEST_TRANSACTION_AMOUNT,getTransactionAmountForIssueFiat(session(Test.TEST_BUYER_USERNAME).as[String])))
    .exec{session=> println(session)
      session}
    .exec(http("IssueFiat_GET")
      .get("/master/issueFiat?requestID="+"${%s}".format(Test.TEST_REQUEST_ID)+"&accountID=testBuyer1515"+"&transactionID="+"${%s}".format(Test.TEST_TRANSACTION_ID)+"&transactionAmount="+"${%s}".format(Test.TEST_TRANSACTION_AMOUNT))
      // .get(routes.IssueFiatController.issueFiatForm("${%s}".format(Test.TEST_REQUEST_ID), "testSeller1515", "${%s}".format(Test.TEST_TRANSACTION_ID), "${%s}".format(Test.TEST_TRANSACTION_AMOUNT).toInt).url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("IssueFiat_POST")
      .post(routes.IssueFiatController.issueFiat().url)
      .formParamMap(Map(
        Form.REQUEST_ID -> "${%s}".format(Test.TEST_REQUEST_ID),
        Form.ACCOUNT_ID -> "${%s}".format(Test.TEST_BUYER_USERNAME),
        Form.TRANSACTION_ID -> "${%s}".format(Test.TEST_TRANSACTION_ID),
        Form.TRANSACTION_AMOUNT -> "${%s}".format(Test.TEST_TRANSACTION_AMOUNT),
        Form.PASSWORD -> "${%s}".format(Test.TEST_ZONE_PASSWORD),
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
    .pause(2)
}

object ChangeBuyerBid{

  val changeBuyerBid=scenario("changeBuyerBid")
    .exec(http("LoginBuyer_GET")
      .get(routes.AccountController.loginForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .exec(http("LoginBuyer_POST")
      .post(routes.AccountController.login().url)
      .formParamMap(Map(
        Form.USERNAME -> "${%s}".format(Test.TEST_BUYER_USERNAME),
        Form.PASSWORD -> "${%s}".format(Test.TEST_BUYER_PASSWORD),
        Form.PUSH_NOTIFICATION_TOKEN -> "",
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
    .pause(3)
    .feed(BuyerBidFeeder.buyerBidFeed)
    .feed(TimeFeeder.timeFeed)
    .feed(GasFeeder.gasFeed)
    .exec(session=> session.set(Test.TEST_SELLER_ADDRESS,getAddressFromAccountID(session(Test.TEST_SELLER_USERNAME).as[String])))
    .exec{session=>session.set(Test.TEST_PEG_HASH,issueAssetControllerTest.getPegHashByOwnerAddress(session(Test.TEST_SELLER_USERNAME).as[String]))}
    .doIf(session=> session(Test.TEST_PEG_HASH).as[String] == "0") {
      asLongAs(session=> session(Test.TEST_PEG_HASH).as[String] == "0") {
        pause(1)
          .exec { session => session.set(Test.TEST_PEG_HASH,issueAssetControllerTest.getPegHashByOwnerAddress(session(Test.TEST_SELLER_USERNAME).as[String]))}
      }
    }
    .exec{session=> println(session)
      session}
    .exec(http("ChangeBuyerBid_GET")
      .get("/master/changeBuyerBid?sellerAddress="+"${%s}".format(Test.TEST_SELLER_ADDRESS)+"&pegHash="+"${%s}".format(Test.TEST_PEG_HASH))
      //.get(routes.ChangeBuyerBidController.changeBuyerBidForm("${%s}".format(Test.TEST_TO),"${%s}".format(Test.TEST_PEG_HASH)).url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN))
      .check(css("[name=%s]".format(Form.REQUEST_ID), "value").saveAs(Form.REQUEST_ID))
    )
    .pause(2)
    .exec(http("ChangeBuyerBid_POST")
      .post(routes.ChangeBuyerBidController.changeBuyerBid().url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        Form.REQUEST_ID-> "${%s}".format(Form.REQUEST_ID),
        Form.SELLER_ADDRESS -> "${%s}".format(Test.TEST_SELLER_ADDRESS),
        Form.BID -> "${%s}".format(Test.TEST_BUYER_BID),
        Form.TIME -> "${%s}".format(Test.TEST_TIME),
        Form.PEG_HASH -> "${%s}".format(Test.TEST_PEG_HASH),
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Form.PASSWORD -> "${%s}".format(Test.TEST_BUYER_PASSWORD)))
    )
    .pause(2)
}

object ChangeSellerBid{

  val changeSellerBid=scenario("changeSellerBid")
    .feed(SellerBidFeeder.sellerBidFeed)
    .feed(TimeFeeder.timeFeed)
    .feed(GasFeeder.gasFeed)
    .exec(http("LoginSeller_GET")
      .get(routes.AccountController.loginForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("LoginSeller_POST")
      .post(routes.AccountController.login().url)
      .formParamMap(Map(
        Form.USERNAME -> "${%s}".format(Test.TEST_SELLER_USERNAME),
        Form.PASSWORD -> "${%s}".format(Test.TEST_SELLER_PASSWORD),
        Form.PUSH_NOTIFICATION_TOKEN -> "",
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
    .pause(3)
    .exec(session=> session.set(Test.TEST_BUYER_ADDRESS,getAddressFromAccountID(session(Test.TEST_BUYER_USERNAME).as[String])))
    .exec{session=> println(session)
      session}
    .exec(http("ChangeSellerBid_GET")
      .get("/master/changeSellerBid?buyerAddress="+"${%s}".format(Test.TEST_BUYER_ADDRESS)+"&pegHash="+"${%s}".format(Test.TEST_PEG_HASH))
      //.get(routes.ChangeSellerBidController.changeSellerBidForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN))
      .check(css("[name=%s]".format(Form.REQUEST_ID), "value").saveAs(Form.REQUEST_ID))
    )
    .pause(2)
    .exec(http("ChangeSellerBid_POST")
      .post(routes.ChangeSellerBidController.changeSellerBid().url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        Form.REQUEST_ID -> "${%s}".format(Form.REQUEST_ID),
        Form.BUYER_ADDRESS -> "${%s}".format(Test.TEST_BUYER_ADDRESS),
        Form.BID -> "${%s}".format(Test.TEST_SELLER_BID),
        Form.TIME -> "${%s}".format(Test.TEST_TIME),
        Form.PEG_HASH -> "${%s}".format(Test.TEST_PEG_HASH),
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Form.PASSWORD -> "${%s}".format(Test.TEST_SELLER_PASSWORD))))
    .pause(2)
}

object ConfirmBuyerBid{
  val confirmBuyerBid=scenario("confirmBuyerBid")
    .exec(http("LoginBuyer_GET")
      .get(routes.AccountController.loginForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("LoginBuyer_POST")
      .post(routes.AccountController.login().url)
      .formParamMap(Map(
        Form.USERNAME -> "${%s}".format(Test.TEST_BUYER_USERNAME),
        Form.PASSWORD -> "${%s}".format(Test.TEST_BUYER_PASSWORD),
        Form.PUSH_NOTIFICATION_TOKEN -> "",
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
    .pause(3)
    .exec(http("ConfirmBuyerBidDetail_GET")
      .get("/master/confirmBuyerBidDetail?sellerAddress="+"${%s}".format(Test.TEST_SELLER_ADDRESS)+"&pegHash="+"${%s}".format(Test.TEST_PEG_HASH)+"&bid="+"950")
      //.get(routes.ConfirmBuyerBidController.confirmBuyerBidForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN))
      .check(css("[name=%s]".format(Form.REQUEST_ID), "value").saveAs(Form.REQUEST_ID))
    )
    .pause(2)
    .exec(http("ConfirmBuyerBidDetail_POST")
      .post(routes.ConfirmBuyerBidController.confirmBuyerBidDetail().url)
      .formParamMap(Map(
        Form.REQUEST_ID-> "${%s}".format(Form.REQUEST_ID),
        Form.SELLER_ADDRESS -> "${%s}".format(Test.TEST_SELLER_ADDRESS),
        Form.BID -> "950",
        Form.PEG_HASH -> "${%s}".format(Test.TEST_PEG_HASH),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
    .pause(2)
    //.exec(session=> session.set(Test.TEST_NEGOTIATION_REQUEST_ID,getNegotiationIDFromSellerAccountID("testSeller1515")))
    .exec { session =>
      println(session)
      session
    }
    .feed(ImageFeeder.imageFeed)
    .exec(http("Trader Upload_BuyerContract_Negotiation Form ")
      .get("/traderUpload/negotiationForm?documentType=BUYER_CONTRACT&negotiationRequestID="+ "${%s}".format(Form.REQUEST_ID))
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN))
    )
    .pause(2)
    .exec(http("Trader Upload Buyer_Contract")
      .post(routes.FileController.uploadTraderNegotiation("BUYER_CONTRACT").url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        "resumableChunkNumber" -> "1",
        "resumableChunkSize" -> "1048576",
        "resumableTotalSize" -> "${%s}".format(Test.TEST_FILE_SIZE),
        "resumableIdentifier" -> "document",
        "resumableFilename" -> "${%s}".format(Test.TEST_FILE_NAME)))
      .bodyPart(RawFileBodyPart("file", "/root/IdeaProjects/commitCentral/gatling/simulation/images/"+"${%s}".format(Test.TEST_FILE_NAME))
        .transferEncoding("binary")).asMultipartForm)
    .exec(
      http("store_Negotiation_Buyer_Contract_Hash")
        .get("/traderUpload/storeNegotiationDocument?name="+"${%s}".format(Test.TEST_FILE_NAME)+"&documentType="+"BUYER_CONTRACT"+"&negotiationRequestID="+ "${%s}".format(Form.REQUEST_ID))
    )
    .pause(2)
    .exec(http("Get_Confirm_Buyer_Bid_Form")
      .get("/master/confirmBuyerBid?requestID="+ "${%s}".format(Form.REQUEST_ID))
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN))
    )
    .pause(1)
    .exec(http("Conform_buyer_Bid_POST")
      .post(routes.ConfirmBuyerBidController.confirmBuyerBid().url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        Form.REQUEST_ID-> "${%s}".format(Form.REQUEST_ID),
        Form.TIME -> "${%s}".format(Test.TEST_TIME),
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Form.PASSWORD -> "${%s}".format(Test.TEST_BUYER_PASSWORD)
      ))
    )
    .pause(3)
}

object ConfirmSellerBid{
  val confirmSellerBid=scenario("confirmSellerBid")
    .exec(http("LoginSeller_GET")
      .get(routes.AccountController.loginForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .exec(http("LoginSeller_POST")
      .post(routes.AccountController.login().url)
      .formParamMap(Map(
        Form.USERNAME -> "${%s}".format(Test.TEST_SELLER_USERNAME),
        Form.PASSWORD -> "${%s}".format(Test.TEST_SELLER_PASSWORD),
        Form.PUSH_NOTIFICATION_TOKEN -> "",
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
    .pause(3)
    .feed(SellerBidFeeder.sellerBidFeed)
    .feed(TimeFeeder.timeFeed)
    .feed(GasFeeder.gasFeed)
    .exec(http("ConfirmSellerBidDetail_GET")
      .get("/master/confirmSellerBidDetail?buyerAddress="+"${%s}".format(Test.TEST_BUYER_ADDRESS)+"&pegHash="+"${%s}".format(Test.TEST_PEG_HASH)+"&bid="+"950")
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN))
      .check(css("[name=%s]".format(Form.REQUEST_ID), "value").saveAs(Form.REQUEST_ID))
    )
    .pause(2)
    .exec(http("ConfirmSellerBidDetail_POST")
      .post(routes.ConfirmSellerBidController.confirmSellerBidDetail().url)
      .formParamMap(Map(
        Form.REQUEST_ID-> "${%s}".format(Form.REQUEST_ID),
        Form.BUYER_ADDRESS -> "${%s}".format(Test.TEST_BUYER_ADDRESS),
        Form.BID -> "950",
        Form.PEG_HASH -> "${%s}".format(Test.TEST_PEG_HASH),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
    .pause(2)
    .feed(ImageFeeder2.imageFeed2)
    .exec(http("Trader Upload_Seller_Contract_Negotiation Form")
      .get("/traderUpload/negotiationForm?documentType=SELLER_CONTRACT&negotiationRequestID="+ "${%s}".format(Form.REQUEST_ID))
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN))
    )
    .pause(2)
    .exec(http("Trader Upload Seller_Contract")
      .post(routes.FileController.uploadTraderNegotiation("SELLER_CONTRACT").url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        "resumableChunkNumber" -> "1",
        "resumableChunkSize" -> "1048576",
        "resumableTotalSize" -> "${%s}".format(Test.TEST_FILE_SIZE),
        "resumableIdentifier" -> "document",
        "resumableFilename" -> "${%s}".format(Test.TEST_FILE_NAME)))
      .bodyPart(RawFileBodyPart("file", "/root/IdeaProjects/commitCentral/gatling/simulation/images/"+"${%s}".format(Test.TEST_FILE_NAME))
        .transferEncoding("binary")).asMultipartForm)
    .exec(
      http("store_Negotiation_Seller_Contract_Hash")
        .get("/traderUpload/storeNegotiationDocument?name="+"${%s}".format(Test.TEST_FILE_NAME)+"&documentType="+"SELLER_CONTRACT"+"&negotiationRequestID="+ "${%s}".format(Form.REQUEST_ID))
    )
    .pause(2)
    .exec(http("Get_Confirm_Seller_Bid_Form")
      .get("/master/confirmSellerBid?requestID="+ "${%s}".format(Form.REQUEST_ID))
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN))
    )
    .pause(1)
    .exec(http("Conform_Seller_Bid_POST")
      .post(routes.ConfirmSellerBidController.confirmSellerBid().url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        Form.REQUEST_ID-> "${%s}".format(Form.REQUEST_ID),
        Form.TIME -> "${%s}".format(Test.TEST_TIME),
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Form.PASSWORD -> "${%s}".format(Test.TEST_SELLER_PASSWORD)
      ))
    )
    .pause(3)
}

object SendFiat{

  val sendFiat=scenario("sendFiat")
    .exec(http("LoginBuyer_GET")
      .get(routes.AccountController.loginForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("LoginBuyer_POST")
      .post(routes.AccountController.login().url)
      .formParamMap(Map(
        Form.USERNAME -> "${%s}".format(Test.TEST_BUYER_USERNAME),
        Form.PASSWORD -> "${%s}".format(Test.TEST_BUYER_PASSWORD),
        Form.PUSH_NOTIFICATION_TOKEN -> "",
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
    .pause(3)
    .feed(AmountFeeder.amountFeed)
    .feed(GasFeeder.gasFeed)
    .exec(http("SendFiatForm_GET")
      .get("/master/sendFiat?sellerAddress="+"${%s}".format(Test.TEST_SELLER_ADDRESS)+"&pegHash="+"${%s}".format(Test.TEST_PEG_HASH)+"&amount="+"${%s}".format(Test.TEST_AMOUNT))
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("SendFiat_POST")
      .post(routes.SendFiatController.sendFiat().url)
      .formParamMap(Map(
        Form.PASSWORD -> "${%s}".format(Test.TEST_BUYER_PASSWORD),
        Form.SELLER_ADDRESS -> "${%s}".format(Test.TEST_SELLER_ADDRESS),
        Form.AMOUNT -> "${%s}".format(Test.TEST_AMOUNT),
        Form.PEG_HASH -> "${%s}".format(Test.TEST_PEG_HASH),
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN)
      )))
    .pause(3)

}

object ReleaseAsset{

  val releaseAsset=scenario("releaseAsset")
    .exec(http("LoginZone_GET")
      .get(routes.AccountController.loginForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .exec(http("LoginZone_POST")
      .post(routes.AccountController.login().url)
      .formParamMap(Map(
        Form.USERNAME ->  "${%s}".format(Test.TEST_ZONE_USERNAME),
        Form.PASSWORD ->  "${%s}".format(Test.TEST_ZONE_PASSWORD),
        Form.PUSH_NOTIFICATION_TOKEN -> "",
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
    .pause(5)
    .exec(http("ReleaseAssetForm_GET")
      .get("/master/releaseAsset?blockchainAddress="+"${%s}".format(Test.TEST_SELLER_ADDRESS)+"&pegHash="+"${%s}".format(Test.TEST_PEG_HASH))
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("ReleaseAsset_POST")
      .post(routes.ReleaseAssetController.releaseAsset().url)
      .formParamMap(Map(
        Form.BLOCKCHAIN_ADDRESS -> "${%s}".format(Test.TEST_SELLER_ADDRESS),
        Form.PEG_HASH -> "${%s}".format(Test.TEST_PEG_HASH),
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Form.PASSWORD -> "${%s}".format(Test.TEST_ZONE_PASSWORD),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
    .pause(7)
}

object SendAsset{

  val sendAsset=scenario("sendAsset")
    .exec(http("LoginBuyer_GET")
      .get(routes.AccountController.loginForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .exec(http("LoginBuyer_POST")
      .post(routes.AccountController.login().url)
      .formParamMap(Map(
        Form.USERNAME -> "${%s}".format(Test.TEST_SELLER_USERNAME),
        Form.PASSWORD -> "${%s}".format(Test.TEST_SELLER_PASSWORD),
        Form.PUSH_NOTIFICATION_TOKEN -> "",
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
    .pause(3)
    .exec(http("SendAsset_GET")
      .get("/master/sendAsset?buyerAddress="+"${%s}".format(Test.TEST_BUYER_ADDRESS)+"&pegHash="+"${%s}".format(Test.TEST_PEG_HASH))
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("SendAsset_POST")
      .post(routes.SendAssetController.sendAsset().url)
      .formParamMap(Map(
        Form.PASSWORD -> "${%s}".format(Test.TEST_SELLER_PASSWORD),
        Form.BUYER_ADDRESS -> "${%s}".format(Test.TEST_BUYER_ADDRESS),
        Form.PEG_HASH -> "${%s}".format(Test.TEST_PEG_HASH),
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
    .pause(5)

}

object BuyerAndSellerExecuteOrder{

  val buyerAndSellerExecuteOrder=scenario("buyerAndSellerExecuteOrder")
    .exec(http("LoginZone_GET")
      .get(routes.AccountController.loginForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("LoginZone_POST")
      .post(routes.AccountController.login().url)
      .formParamMap(Map(
        Form.USERNAME ->  "${%s}".format(Test.TEST_ZONE_USERNAME),
        Form.PASSWORD ->  "${%s}".format(Test.TEST_ZONE_PASSWORD),
        Form.PUSH_NOTIFICATION_TOKEN -> "",
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
    .pause(3)
    .exec(session=> session.set(Test.TEST_NEGOTIATION_REQUEST_ID,confirmBuyerBidControllerTest.getNegotiationIDFromSellerAccountID(session(Test.TEST_SELLER_USERNAME).as[String])))
    .exec(http("moderatedBuyerExecuteOrderDocumentUpload_GetForm")
      .get("/master/moderatedBuyerExecuteOrderDocument?buyerAddress="+"${%s}".format(Test.TEST_BUYER_ADDRESS)+"&sellerAddress="+"${%s}".format(Test.TEST_SELLER_ADDRESS)+"&pegHash="+"${%s}".format(Test.TEST_PEG_HASH))
    )
    .pause(1)
    .exec(http("zoneUploadNegotiationForm_FIAT_Proof")
      .get("/zoneUpload/negotiationForm?documentType=FIAT_PROOF&negotiationRequestID="+"${%s}".format(Test.TEST_NEGOTIATION_REQUEST_ID))
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN))
    )
    .pause(2)
    .feed(ImageFeeder3.imageFeed3)
    .feed(GasFeeder.gasFeed)
    .exec(http("zoneUploadNegotiation_FIAT_Proof")
      .post(routes.FileController.uploadZoneNegotiation("FIAT_PROOF").url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        "resumableChunkNumber" -> "1",
        "resumableChunkSize" -> "1048576",
        "resumableTotalSize" -> "${%s}".format(Test.TEST_FILE_SIZE),
        "resumableIdentifier" -> "document",
        "resumableFilename" -> "${%s}".format(Test.TEST_FILE_NAME)))
      .bodyPart(RawFileBodyPart("file", "/root/IdeaProjects/commitCentral/gatling/simulation/images/"+"${%s}".format(Test.TEST_FILE_NAME))
        .transferEncoding("binary")).asMultipartForm
    )
    .exec(
      http("storeNegotiationDocument_FIAT_Proof")
        .get("/zoneUpload/storeNegotiationDocument?name="+"${%s}".format(Test.TEST_FILE_NAME)+"&documentType=FIAT_PROOF&negotiationRequestID="+"${%s}".format(Test.TEST_NEGOTIATION_REQUEST_ID))
    )
    .pause(2)
    .exec(http("moderatedBuyerExecuteForm_Get")
      .get("/master/moderatedBuyerExecuteOrder?requestID="+"${%s}".format(Test.TEST_NEGOTIATION_REQUEST_ID))
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN))
      .check(css("[name=%s]".format(Form.FIAT_PROOF_HASH), "value").saveAs(Form.FIAT_PROOF_HASH))
    )
    .pause(2)
    .exec(http("moderatedBuyerExecute_POST")
      .post(routes.BuyerExecuteOrderController.moderatedBuyerExecuteOrder().url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        Form.SELLER_ADDRESS -> "${%s}".format(Test.TEST_SELLER_ADDRESS),
        Form.BUYER_ADDRESS -> "${%s}".format(Test.TEST_BUYER_ADDRESS),
        Form.FIAT_PROOF_HASH -> "${%s}".format(Form.FIAT_PROOF_HASH),
        Form.PEG_HASH ->  "${%s}".format(Test.TEST_PEG_HASH),
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Form.PASSWORD -> "${%s}".format(Test.TEST_ZONE_PASSWORD)
      ))
    )
    .pause(3)
    .exec(http("moderatedSellerExecuteOrderDocumentUpload_GetForm")
      .get("/master/moderatedSellerExecuteOrderDocument?buyerAddress="+"${%s}".format(Test.TEST_BUYER_ADDRESS)+"&sellerAddress="+"${%s}".format(Test.TEST_SELLER_ADDRESS)+"&pegHash="+"${%s}".format(Test.TEST_PEG_HASH))
    )
    .pause(1)
    .exec(http("zoneUploadNegotiationForm_AWB_Proof")
      .get("/zoneUpload/negotiationForm?documentType=AWB_PROOF&negotiationRequestID="+"${%s}".format(Test.TEST_NEGOTIATION_REQUEST_ID))
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN))
    )
    .pause(2)
    .feed(ImageFeeder4.imageFeed4)
    .feed(GasFeeder.gasFeed)
    .exec(http("zoneUploadNegotiation_AWB_Proof")
      .post(routes.FileController.uploadZoneNegotiation("AWB_PROOF").url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        "resumableChunkNumber" -> "1",
        "resumableChunkSize" -> "1048576",
        "resumableTotalSize" -> "${%s}".format(Test.TEST_FILE_SIZE),
        "resumableIdentifier" -> "document",
        "resumableFilename" -> "${%s}".format(Test.TEST_FILE_NAME)))
      .bodyPart(RawFileBodyPart("file", "/root/IdeaProjects/commitCentral/gatling/simulation/images/"+"${%s}".format(Test.TEST_FILE_NAME))
        .transferEncoding("binary")).asMultipartForm
    )
    .exec(
      http("storeNegotiationDocument_AWBProof")
        .get("/zoneUpload/storeNegotiationDocument?name="+"${%s}".format(Test.TEST_FILE_NAME)+"&documentType=AWB_PROOF&negotiationRequestID="+"${%s}".format(Test.TEST_NEGOTIATION_REQUEST_ID))
    )
    .pause(2)
    .exec(http("moderatedSellerExecuteForm_Get")
      .get("/master/moderatedSellerExecuteOrder?requestID="+"${%s}".format(Test.TEST_NEGOTIATION_REQUEST_ID))
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN))
      .check(css("[name=%s]".format(Form.AWB_PROOF_HASH), "value").saveAs(Form.AWB_PROOF_HASH))
    )
    .pause(2)
    .exec(http("moderatedSellerExecute_POST")
      .post(routes.SellerExecuteOrderController.moderatedSellerExecuteOrder().url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        Form.BUYER_ADDRESS -> "${%s}".format(Test.TEST_BUYER_ADDRESS),
        Form.SELLER_ADDRESS -> "${%s}".format(Test.TEST_SELLER_ADDRESS),
        Form.AWB_PROOF_HASH -> "${%s}".format(Form.AWB_PROOF_HASH),
        Form.PEG_HASH ->  "${%s}".format(Test.TEST_PEG_HASH),
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Form.PASSWORD ->  "${%s}".format(Test.TEST_ZONE_PASSWORD)
      ))
    )
}
*/
