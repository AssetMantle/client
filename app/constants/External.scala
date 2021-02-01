package constants

object External {

  object KeyBase {
    val VALIDATOR_KEY_BASE_ACCOUNT = "VALIDATOR_KEY_BASE_ACCOUNT"
  }

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

}
