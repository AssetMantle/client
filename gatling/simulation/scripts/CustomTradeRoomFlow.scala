package scripts

import constants.Test
import controllers.routes
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import controllersTest._
import feeders.JDBCFeeder._

class CustomTradeRoomFlow extends Simulation {

  val customFlow = scenario("sfsdfs")
    /*.exec(CreateZone.createZone)
    .exec(CreateSellerOrganization.createSellerOrganization)
    .exec(CreateBuyerOrganization.createBuyerOrganization)
    .exec(CreateSeller.createSeller)
    .exec(CreateBuyer.createBuyer)
    .exec(AddCounterParty.addCounterParty)
    .exec(IssueFiat.issueFiat)*/
    .exec(IssueAssetWithConstraintCheck.issueAssetWithConstraintCheck)
    .exec(CreateSalesQuoteWithConstraintCheck.createSalesQuoteWithConstraintCheck)
    .exec(AcceptSalesQuote.acceptSalesQuote)
    .exec(UpdateTradeTerms.updateTradeTermsScenario)
    .exec(AcceptAllTerms.acceptAllTradeTerms)
    .exec(UploadTradeDocuments.uploadTradeDocuments)
    .exec(RejectBillOfLading.rejectBillOfLading)
    .exec(UpdateTradeDocuments.updateTradeDocuments)
    .exec(AcceptBillOfLading.acceptBillOfLading)
    .exec(BuyerConfirmNegotiation.buyerConfirmNegotiation)
    .exec(SellerConfirmNegotiation.sellerConfirmNegotiation)
    .exec(VesselCheckAndReleaseAsset.vesselCheckAndReleaseAsset)
    .exec(SendFiat.sendFiat)
    .exec(SendAsset.sendAsset)
    .exec(ModeratedBuyerAndSellerExecuteOrder.moderatedBuyerAndSellerExecuteOrder)
    .exec(RedeemAsset.redeemAsset)
    .exec(RedeemFiat.redeemFiat)


  setUp(
    customFlow.inject(atOnceUsers(1))
  ).protocols(http.baseUrl(Test.BASE_URL))
}

object IssueAssetWithConstraintCheck {

  val issueAssetWithConstraintCheck = scenario("SignUp")
    .exec(session => session.set(Test.TEST_SELLER_USERNAME, "SELL10Ri6CUYBE").set(Test.TEST_SELLER_PASSWORD,"SELL10Ri6CUYBE"))
    .exec(session => session.set(Test.TEST_BUYER_USERNAME, "BUY10ATk6b5t7").set(Test.TEST_BUYER_PASSWORD, "BUY10ATk6b5t7"))
    .exec { session => session.set(Test.TEST_SELLER_TRADER_ID, getTraderID(session(Test.TEST_SELLER_USERNAME).as[String])) }
    .exec { session => session.set(Test.TEST_BUYER_TRADER_ID, getTraderID(session(Test.TEST_BUYER_USERNAME).as[String])) }
    .exec { session => session.set(Test.TEST_COUNTER_PARTY, session(Test.TEST_BUYER_TRADER_ID).as[String]) }
    .exec(session => session.set(Test.TEST_USERNAME, session(Test.TEST_SELLER_USERNAME).as[String]).set(Test.TEST_PASSWORD, session(Test.TEST_SELLER_PASSWORD).as[String]))
    .exec(AccountControllerTest.loginScenario)
    .exec(ConstraintTest.IssueAsset.gasMissing)
    .exec(ConstraintTest.IssueAsset.passwordMissing)
    .exec(AssetControllerTest.moderatedIssueAssetRequestScenario)
    .exec(AccountControllerTest.logoutScenario)
    .pause(Test.BLOCKCHAIN_TRANSACTION_DELAY)
    .exec { session => session.set(Test.TEST_ASSET_ID, getAssetID(session(Test.TEST_SELLER_TRADER_ID).as[String], session(Test.TEST_ASSET_TYPE).as[String], session(Test.TEST_ASSET_DESCRIPTION).as[String])) }

}

object CreateSalesQuoteWithConstraintCheck {

  val createSalesQuoteWithConstraintCheck = scenario("createSalesQuoteWithConstraintCheck")
    .exec { session => session.set(Test.TEST_COUNTER_PARTY, session(Test.TEST_BUYER_TRADER_ID).as[String]) }
    .exec(session => session.set(Test.TEST_USERNAME, session(Test.TEST_SELLER_USERNAME).as[String]).set(Test.TEST_PASSWORD, session(Test.TEST_SELLER_PASSWORD).as[String]))
    .exec(AccountControllerTest.loginScenario)
    .exec(NegotiationControllerTest.negotiationRequestScenario)
    .exec(ConstraintTest.PaymentTerms.invalidAdvanceAndCreditInput)
    .exec(ConstraintTest.PaymentTerms.bothTenureAndTentaiveInput)
    .exec(ConstraintTest.PaymentTerms.refrenceMissing)
    .exec(ConstraintTest.PaymentTerms.refrenceNotRequired)
    .exec(NegotiationControllerTest.addPaymentTerms)
    .exec(ConstraintTest.DocumentListConstraint.physicalDocumentsHandledViaRequired)
    .exec(NegotiationControllerTest.addDocumentList)
    .exec(NegotiationControllerTest.reviewNegotiationRequest)
    .exec(AccountControllerTest.logoutScenario)

}

object AcceptSalesQuote {

  val acceptSalesQuote = scenario("AcceptSalesQuote")
    .exec(session => session.set(Test.TEST_USERNAME, session(Test.TEST_BUYER_USERNAME).as[String]).set(Test.TEST_PASSWORD, session(Test.TEST_BUYER_PASSWORD).as[String]))
    .exec(AccountControllerTest.loginScenario)
    .exec(NegotiationControllerTest.acceptNegotiationRequest)
    .pause(Test.BLOCKCHAIN_TRANSACTION_DELAY)
    /* .exec { session => session.set(Test.TEST_NEGOTIATION_STATUS, NegotiationControllerTest.getNegotiationStatus(session(Test.TEST_NEGOTIATION_ID).as[String])) }
     .doIf(session => session(Test.TEST_NEGOTIATION_STATUS).as[String] != constants.Status.Negotiation.STARTED) {
       asLongAsDuring(session => session(Test.TEST_NEGOTIATION_STATUS).as[String] != constants.Status.Negotiation.STARTED, Duration.create(80, "seconds")) {
          .pause(Test.LOOP_DELAY)
           .exec { session => session.set(Test.TEST_NEGOTIATION_STATUS, NegotiationControllerTest.getNegotiationStatus(session(Test.TEST_NEGOTIATION_ID).as[String])) }
       }
     }*/
    .exec(AccountControllerTest.logoutScenario)
}

object UpdateTradeTerms {

  val updateTradeTermsScenario: ScenarioBuilder = scenario("updateTradeTerms")
    .exec(session => session.set(Test.TEST_USERNAME, session(Test.TEST_SELLER_USERNAME).as[String]).set(Test.TEST_PASSWORD, session(Test.TEST_SELLER_PASSWORD).as[String]))
    .exec(AccountControllerTest.loginScenario)
    .exec(NegotiationControllerTest.updateAssetTerms)
    .exec(NegotiationControllerTest.updateAssetOtherDetails)
    .exec(NegotiationControllerTest.updatePaymentTerms)
    .exec(NegotiationControllerTest.updateDocumentList)
    .exec(AccountControllerTest.logoutScenario)
}

object AcceptAllTerms {
  val acceptAllTradeTerms = scenario("AcceptAllTradeTerms")
    .exec(session => session.set(Test.TEST_USERNAME, session(Test.TEST_BUYER_USERNAME).as[String]).set(Test.TEST_PASSWORD, session(Test.TEST_BUYER_PASSWORD).as[String]))
    .exec(AccountControllerTest.loginScenario)
    .exec(NegotiationControllerTest.acceptNegotiationTerms)
    .exec(NegotiationControllerTest.confirmAllNegotiationTerms)
    .exec(AccountControllerTest.logoutScenario)
}

object UploadTradeDocuments {

  val uploadTradeDocuments = scenario("UploadTradeDocuments")
    .exec(session => session.set(Test.TEST_USERNAME, session(Test.TEST_SELLER_USERNAME).as[String]).set(Test.TEST_PASSWORD, session(Test.TEST_SELLER_PASSWORD).as[String]))
    .exec(AccountControllerTest.loginScenario)
    .exec(NegotiationControllerTest.uploadContract)
    .exec(NegotiationControllerTest.addContract)
    .exec(NegotiationControllerTest.updateContract)
    .exec(NegotiationControllerTest.addContract)
    .exec(NegotiationControllerTest.updateContractSigned)
    .exec(AssetControllerTest.uploadAssetDocuments)
    .exec(AssetControllerTest.addBillOfLading)
    .exec(NegotiationControllerTest.uploadNegotiationDocuments)
    .exec(NegotiationControllerTest.addInvoice)
    .exec(AccountControllerTest.logoutScenario)

}

object RejectBillOfLading {

  val rejectBillOfLading = scenario("RejectBillOfLading")
    .exec(session => session.set(Test.TEST_USERNAME, session(Test.TEST_BUYER_USERNAME).as[String]).set(Test.TEST_PASSWORD, session(Test.TEST_BUYER_PASSWORD).as[String]))
    .exec(AccountControllerTest.loginScenario)
    .exec(AssetControllerTest.updateBillOfLadingStatus(false))
    .exec(AccountControllerTest.logoutScenario)
}

object UpdateTradeDocuments {

  val updateTradeDocuments = scenario("updateTradeDocuments")
    .exec(session => session.set(Test.TEST_USERNAME, session(Test.TEST_SELLER_USERNAME).as[String]).set(Test.TEST_PASSWORD, session(Test.TEST_SELLER_PASSWORD).as[String]))
    .exec(AccountControllerTest.loginScenario)
    .exec(AssetControllerTest.updateAssetDocuments)
    .exec(AssetControllerTest.addBillOfLading)
    .exec(NegotiationControllerTest.updateNegotiationDocuments)
    .exec(NegotiationControllerTest.addInvoice)
    .exec(AccountControllerTest.logoutScenario)

}





