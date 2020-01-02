///*
//package controllersTest
//
//import scala.concurrent.duration._
//
//import io.gatling.core.Predef._
//import io.gatling.http.Predef._
//import io.gatling.jdbc.Predef._
//
//class RecordedSimulation9 extends Simulation {
//
//  val httpProtocol = http
//    .baseUrl("http://192.168.0.197:9000")
//    .inferHtmlResources()
//    .acceptHeader("*/*")
//    .acceptEncodingHeader("gzip, deflate")
//    .acceptLanguageHeader("en-GB,en-US;q=0.9,en;q=0.8")
//    .userAgentHeader("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/77.0.3865.120 Safari/537.36")
//
//  val headers_0 = Map(
//    "Accept" -> "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3",
//    "Proxy-Connection" -> "keep-alive",
//    "Upgrade-Insecure-Requests" -> "1")
//
//  val headers_1 = Map("Proxy-Connection" -> "keep-alive")
//
//  val headers_10 = Map(
//    "Accept" -> "text/css,*/*;q=0.1",
//    "Proxy-Connection" -> "keep-alive")
//
//  val headers_19 = Map(
//    "Accept" -> "image/webp,image/apng,image/*,*/*;q=0.8",
//    "Proxy-Connection" -> "keep-alive")
//
//  val headers_39 = Map(
//    "Proxy-Connection" -> "keep-alive",
//    "X-Requested-With" -> "XMLHttpRequest")
//
//  val headers_49 = Map(
//    "Accept" -> "image/webp,image/apng,image/*,*/*;q=0.8",
//    "Pragma" -> "no-cache",
//    "Proxy-Connection" -> "keep-alive")
//
//  val headers_51 = Map(
//    "Origin" -> "http://192.168.0.197:9000",
//    "Proxy-Connection" -> "keep-alive",
//    "X-Requested-With" -> "XMLHttpRequest")
//
//  val headers_58 = Map(
//    "Content-Type" -> "multipart/form-data; boundary=----WebKitFormBoundaryiCbqnwIGl0ATOHlF",
//    "Origin" -> "http://192.168.0.197:9000",
//    "Proxy-Connection" -> "keep-alive")
//
//  val headers_62 = Map(
//    "Content-Type" -> "multipart/form-data; boundary=----WebKitFormBoundaryTYGPASzl5s7tRPSi",
//    "Origin" -> "http://192.168.0.197:9000",
//    "Proxy-Connection" -> "keep-alive")
//
//
//
//  val scn = scenario("RecordedSimulation9")
//    .exec(http("request_50")
//      .get("/account/login")
//      .headers(headers_39))
//    .pause(5)
//    .exec(http("request_51")
//      .post("/account/login")
//      .headers(headers_51)
//      .formParam("csrfToken", "c44a1a960234821347837b9693db46aee6ce8d51-1574251962311-f5a95b5eafb5f676bc7b6119")
//      .formParam("USERNAME", "testUser099")
//      .formParam("PASSWORD", "qwerty1234567890")
//      .formParam("PUSH_NOTIFICATION_TOKEN", "")
//      )
//    .pause(1)
//    .exec(http("request_54")
//      .get("/master/addZone")
//      .headers(headers_39))
//    .pause(2)
//    .exec(http("request_55")
//      .post("/master/addZone")
//      .headers(headers_51)
//      .formParam("csrfToken", "487e052dfd557a59c19c4640334b640a2fc19ee1-1574251969682-f5a95b5eafb5f676bc7b6119")
//      .formParam("NAME", "sfdgh")
//      .formParam("CURRENCY", "dgg")
//      .formParam("ADDRESS.ADDRESS_LINE_1", "grgr")
//      .formParam("ADDRESS.ADDRESS_LINE_2", "grtg")
//      .formParam("ADDRESS.LANDMARK", "rtgsrtg")
//      .formParam("ADDRESS.CITY", "srtgrgr")
//      .formParam("ADDRESS.COUNTRY", "srtgrstgsr")
//      .formParam("ADDRESS.ZIP_CODE", "1234566")
//      .formParam("ADDRESS.PHONE", "1234567890"))
//    .pause(1)
//    .exec(http("request_56")
//      .get("/userUpdate/zoneKYC?documentType=IDENTIFICATION")
//      .headers(headers_39))
//    .pause(10)
//    .exec(
//      http("request_58")
//        .post("/userUpload/userZoneKYCDocument?documentType=IDENTIFICATION")
//        .headers(headers_58)
//        .body(RawFileBody("/recordedsimulation9/0058_request.dat"))
//      )
//    .exec(http("request_59")
//      .get("/userUpdate/updateUserZoneKYCDocument?name=Screenshot%20from%202019-11-20%2017-42-13.png&documentType=IDENTIFICATION")
//      .headers(headers_39))
//    .pause(2)
//    .exec(http("request_60")
//      .get("/userUpdate/zoneKYC?documentType=BANK_ACCOUNT_DETAIL")
//      .headers(headers_39))
//    .pause(8)
//    .exec(http("request_61")
//      .get("/userUpload/userZoneKYCDocument?documentType=BANK_ACCOUNT_DETAIL&csrfToken=1c6ede111a5ca8ce6f9470e31d714a36911bb37b-1574251987976-f5a95b5eafb5f676bc7b6119&resumableChunkNumber=1&resumableChunkSize=1048576&resumableCurrentChunkSize=286359&resumableTotalSize=286359&resumableType=image%2Fpng&resumableIdentifier=286359-Screenshotfrom2019-11-2017-42-16png&resumableFilename=Screenshot%20from%202019-11-20%2017-42-16.png&resumableRelativePath=Screenshot%20from%202019-11-20%2017-42-16.png&resumableTotalChunks=1")
//      .headers(headers_1)
//      .resources(http("request_62")
//        .post("/userUpload/userZoneKYCDocument?documentType=BANK_ACCOUNT_DETAIL")
//        .headers(headers_62)
//        .body(RawFileBody("/recordedsimulation9/0062_request.dat")))
//      )
//    .exec(
//      http("request_63")
//        .get("/userUpdate/updateUserZoneKYCDocument?name=Screenshot%20from%202019-11-20%2017-42-16.png&documentType=BANK_ACCOUNT_DETAIL")
//        .headers(headers_39))
//
//    .pause(7)
//    .exec(http("request_64")
//      .get("/master/userReviewAddZoneRequest")
//      .headers(headers_39))
//    .pause(2)
//    .exec(http("request_65")
//      .post("/master/userReviewAddZoneRequest")
//      .headers(headers_51)
//      .formParam("csrfToken", "6b84171f7cf6192c396cc07445a0e628c366cf2b-1574252005224-f5a95b5eafb5f676bc7b6119")
//      .formParam("COMPLETION", "true")
//      )
//    .pause(3)
//    .exec(http("request_69")
//      .get("/master/unreadNotificationCount")
//      .headers(headers_39)
//      .resources(http("request_70")
//        .get("/blockExplorer/blockDetails?minimumHeight=262493&maximumHeight=262503")
//        .headers(headers_39)))
//
//  setUp(scn.inject(atOnceUsers(1))).protocols(httpProtocol)
//}*/
