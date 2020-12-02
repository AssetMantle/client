package constants

object Log {

  object Info {
    val CONTROLLERS_REQUEST = "CONTROLLERS_REQUEST"
    val CONTROLLERS_RESPONSE = "CONTROLLERS_RESPONSE"
    val STORE_FILE_ENTRY = "STORE_FILE_ENTRY"
    val STORE_FILE_EXIT = "STORE_FILE_EXIT"
  }

  object Messages {

    def blockchainHeightUpdateFailed(height: Int) = s"Blockchain height update failed: ${height}"

    object Websocket {
      val CONNECTION_UPGARDED = "Connection upgraded to websocket. StatusCode: "
      val CONNECTION_UPGARDE_FAILED = "Connection upgrade to websocket failed. StatusCode: "
      val CONNECTION_SUCCESS = "Websocket connection to blockchain success."
      val CONNECTION_FAILED = "Websocket connection to blockchain failed."
      val CONNECTION_CLOSED = "Websocket connection to blockchain closed."
    }


  }

}
