///*
//package controllersTest
//import constants.{Form, Test}
//import feeders._
//
//import scala.concurrent.duration._
//import controllers.routes
//import controllersTest.setACLControllerTest.{addTraderRequest, verifyTraderAndSetACLScenario}
//import io.gatling.core.Predef._
//import io.gatling.http.Predef._
//import io.gatling.jdbc.Predef._
//
//import scala.util.Random
//
//class RandomTest2 extends Simulation{
//
//// /* val httpProtocol = http
////    .baseUrl("http://localhost:9000")
////    .inferHtmlResources()
////    .acceptHeader("*/*")
////    .acceptEncodingHeader("gzip, deflate")
////    .acceptLanguageHeader("en-GB,en-US;q=0.9,en;q=0.8")
////    .userAgentHeader("Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/77.0.3865.120 Mobile Safari/537.36")
////
////  val ultimateHeader=Map(
////    "Accept" -> "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3",
////    "Content-Type" -> "multipart/form-data; boundary=----WebKitFormBoundaryfvUeSXMUIwkqwWI8",
////    "Origin" -> "http://localhost:9000",
////    "Proxy-Connection" -> "keep-alive",
////    "X-Requested-With" -> "XMLHttpRequest",
////    "Upgrade-Insecure-Requests" -> "1"
////  )
//////
//////  val headers_0 = Map(
//////    "Accept" -> "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3",
//////    "Proxy-Connection" -> "keep-alive",
//////    "Upgrade-Insecure-Requests" -> "1")
//////
//////  val headers_1 = Map(
//////    "Proxy-Connection" -> "keep-alive",
//////    "X-Requested-With" -> "XMLHttpRequest")
//////
//////  val headers_5 = Map(
//////    "Content-Type" -> "multipart/form-data; boundary=----WebKitFormBoundaryfvUeSXMUIwkqwWI8",
//////    "Origin" -> "http://localhost:9000",
//////    "Proxy-Connection" -> "keep-alive")
//////
//////  val headers_12 = Map(
//////    "Origin" -> "http://localhost:9000",
//////    "Proxy-Connection" -> "keep-alive",
//////    "X-Requested-With" -> "XMLHttpRequest")
//////
//////  val headers_18 = Map("Proxy-Connection" -> "keep-alive")
//////
//////  val headers_19 = Map(
//////    "Content-Type" -> "multipart/form-data; boundary=----WebKitFormBoundaryBXAgpeCZegxu0WAS",
//////    "Origin" -> "http://localhost:9000",
//////    "Proxy-Connection" -> "keep-alive")
//////
////
////    val headers_0 = Map(
////    "Accept" -> "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3",
////    "Proxy-Connection" -> "keep-alive",
////    "Upgrade-Insecure-Requests" -> "1")
////
////    val headers_1 = Map("Proxy-Connection" -> "keep-alive")
////
////    val headers_10 = Map(
////    "Accept" -> "text/css,*/*;q=0.1",
////    "Proxy-Connection" -> "keep-alive")
////
////    val headers_19 = Map(
////    "Accept" -> "image/webp,image/apng,image/*,*/*;q=0.8",
////    "Proxy-Connection" -> "keep-alive")
////
////    val headers_39 = Map(
////    "Proxy-Connection" -> "keep-alive",
////    "X-Requested-With" -> "XMLHttpRequest")
////
////    val headers_49 = Map(
////    "Accept" -> "image/webp,image/apng,image/*,*/*;q=0.8",
////    "Pragma" -> "no-cache",
////    "Proxy-Connection" -> "keep-alive")
////
////    val headers_51 = Map(
////    "Origin" -> "http://192.168.0.197:9000",
////    "Proxy-Connection" -> "keep-alive",
////    "X-Requested-With" -> "XMLHttpRequest")
////
////    val headers_58 = Map(
////    "Content-Type" -> "multipart/form-data; boundary=----WebKitFormBoundaryiCbqnwIGl0ATOHlF",
////    "Origin" -> "http://192.168.0.197:9000",
////    "Proxy-Connection" -> "keep-alive")
////
////    val headers_62 = Map(
////    "Content-Type" -> "multipart/form-data; boundary=----WebKitFormBoundaryTYGPASzl5s7tRPSi",
////    "Origin" -> "http://192.168.0.197:9000",
////    "Proxy-Connection" -> "keep-alive")
////
////
////  val scn=scenario("fileUpload")
////    .exec(http("request_1")
////      .post(routes.AddZoneController.userUploadZoneKYC("BANK_ACCOUNT_DETAIL").url)
////      .headers(headers_0)
////      .headers(headers_19)
////      .formParamMap(Map(
////        "resumableChunkNumber" -> "1",
////        "resumableChunkSize" -> "1048576",
////        "resumableCurrentChunkSize" -> "1122",
////        "resumableTotalSize" -> "1122",
////        "resumableType" -> "application/pdf",
////        "resumableIdentifier" -> "VerifyMobileNumberControllerTest.scala",
////        "resumableFilename" -> "VerifyMobileNumberControllerTest.scala",
////        "resumableRelativePath" -> "VerifyMobileNumberControllerTest.scala",
////        "resumableTotalChunks" -> "1",
////        "csrf" -> "5d94f2d1ce3a6d92be91cc82ee3a885d7cb5fb47-1574150103344-2c9df3e5769148c6f529495c"
////      ))
////    )
////
////  val scn2=scenario("test")
////    .exec(http("Login_GET")
////      .get(routes.AccountController.loginForm().url)
////      .headers(headers_39)
////      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN))
////    )
////    .exec(http("Login_POST")
////      .post(routes.AccountController.login().url)
////      .headers(headers_51)
////      .formParamMap(Map(
////        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
////        Form.USERNAME -> "testUser3000",
////        Form.PASSWORD -> "qwerty1234567890",
////        Form.PUSH_NOTIFICATION_TOKEN -> ""
////        )))
////    .pause(5)
////    .exec(http("AddZone_GET")
////      .get(routes.AddZoneController.addZoneForm().url)
////      .headers(headers_39)
////      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
////    .pause(2)
////    .feed(NameFeeder.nameFeed)
////    .feed(CurrencyFeeder.currencyFeed)
////    .exec(http("AddZone_POST")
////      .post(routes.AddZoneController.addZone().url)
////      .headers(headers_51)
////      .formParamMap(Map(
////        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
////        Form.NAME -> "${%s}".format(Test.TEST_NAME),
////        Form.CURRENCY -> "zfdbvd",
////        "ADDRESS.ADDRESS_LINE_1" -> Random.alphanumeric.take(8).mkString,
////        "ADDRESS.ADDRESS_LINE_2" -> Random.alphanumeric.take(8).mkString,
////        "ADDRESS.LANDMARK" -> Random.alphanumeric.take(8).mkString,
////        "ADDRESS.CITY" -> Random.alphanumeric.take(8).mkString,
////        "ADDRESS.COUNTRY" -> Random.alphanumeric.take(8).mkString,
////        "ADDRESS.ZIP_CODE" -> "123456",
////        "ADDRESS.PHONE" -> "1234567890"
////        ))
////      /*.formParam(Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))
////      .formParam("NAME", "sfdgh")
////      .formParam("CURRENCY", "dgg")
////      .formParam("ADDRESS.ADDRESS_LINE_1", "grgr")
////      .formParam("ADDRESS.ADDRESS_LINE_2", "grtg")
////      .formParam("ADDRESS.LANDMARK", "rtgsrtg")
////      .formParam("ADDRESS.CITY", "srtgrgr")
////      .formParam("ADDRESS.COUNTRY", "srtgrstgsr")
////      .formParam("ADDRESS.ZIP_CODE", "1234566")
////      .formParam("ADDRESS.PHONE", "1234567890")*/
////    )
//
//  val scn3=scenario("testUpload")
//    .feed(ImageFeeder.imageFeed)
//    .exec(http("form")
//      .get(routes.AddZoneController.userUploadZoneKYCForm("IDENTIFICATION").url)
//      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN))
//    )
//    .pause(3)
//    .feed(UsernameFeeder.usernameFeed)
//    .exec{session=> println(session)
//      session
//    }
//    .feed(UsernameFeeder.usernameFeed)
//    .exec{session=> println(session)
//      session
//    }
//    .feed(UsernameFeeder.usernameFeed)
//    .exec{session=> println(session)
//      session
//    }
//    .feed(UsernameFeeder.usernameFeed)
//    .exec{session=> println(session)
//      session
//    }
//    .exec(http("upload_KYC")
//      .post("/userUpload/userZoneKYCDocument?documentType=IDENTIFICATION")
//     // .headers(headers_5)
//      .formParamMap(Map(
//        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
//        "resumableChunkNumber" -> "1",
//        "resumableChunkSize" -> "1048576",
//        "resumableTotalSize" -> "${%s}".format(Test.TEST_FILE_SIZE),
//        "resumableIdentifier" -> "document",
//        "resumableFilename" -> "${%s}".format(Test.TEST_FILE_NAME)))
//      .bodyPart(RawFileBodyPart("file", Test.IMAGE_FILE_FEED+"${%s}".format(Test.TEST_FILE_NAME))
//        .transferEncoding("binary")).asMultipartForm)
//    /*.pause(1)
//    .exec(
//      http("request_1")
//        .get("/userUpload/storeUserZoneKYCDocument?name="+"${%s}".format(Test.TEST_FILE_NAME)+"&documentType=IDENTIFICATION")
//        .headers(headers_1)
//    )*/
//  val scn4=scenario("testUpload")
//      .exec(http("newTest").get(routes.AddZoneController.userUploadZoneKYCForm("IDENTIFICATION").url))
//
//  val addAccountScenario=scenario("addAccount")
//      .exec(loginControllerTest.loginAfterSignUpScenario)
//      .exec(sendCoinControllerTest.FaucetRequestScenario)
//      .exec(sendCoinControllerTest.approveFaucetRequestScenario)
//
//  val addTrader=scenario("addTrader")
//    .exec(loginControllerTest.loginAfterSignUpScenario)
//    .exec(sendCoinControllerTest.FaucetRequestScenario)
//    .exec(sendCoinControllerTest.approveFaucetRequestScenario)
//    .pause(40)
//    .exec(setACLControllerTest.addTraderRequest)
//    .exec(setACLControllerTest.verifyTraderAndSetACLScenario)
//
//  setUp(scn3.inject(atOnceUsers(1))).protocols(http.baseUrl(Test.BASE_URL))
//}
//
//object ImageFilePath  {
//  val imageFilePath = "/root/testImages/"
//}*/
