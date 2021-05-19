package constants

object External {

  object Docusign {
    //Docusign Inputs
    val REFRESH_TOKEN = "refresh_token"
    val SIGNATURE_SCOPE = "signature"
    val CODE = "code"

    //Docusign Events
    val SEND = "Send"
    val SIGNING_COMPLETE = "signing_complete"

    object Status {
      val CREATED = "created"
      val SENT = "sent"
      val COMPLETED = "completed"
    }
  }

  object Wallex {
    //wallex date format
    val DATE_FORMAT = "dd-MM-yyyy"
    val STANDARD = "STANDARD"
    val APPLICATION_TYPE = "application/pdf"
    val ON_BEHALF_OF = "onBehalfOf"

    object Status {
    val APPROVED = "approved"
    }
  }

}
