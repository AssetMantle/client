package utilities

import play.api.mvc.Results

object XMLRestResponse {

  //Success
  val TRANSACTION_UPDATE_SUCCESSFUL = new XmlResponse(200,"SUCCESS","Transaction update successful.")

  //Failure
  val REQUEST_NOT_WELL_FORMED = new XmlResponse(400, "BAD_REQUEST", "Request is not well-formed and cannot be understood.")
  val INVALID_REQUEST_SIGNATURE = new XmlResponse(403,"FORBIDDEN","Validation failure – invalid request signature")
  val VALIDATION_FAILURE = new XmlResponse(500, "INTERVAL_SERVER_ERROR", "Validation failure")


  class XmlResponse(code: Int, status: String, message: String) {
    val response = <response>
      <code>{code}</code>
      <status>{status}</status>
      <message>{message}</message>
    </response>

    def result = Results.Status(code)(response).as("application/xml")
  }

}
