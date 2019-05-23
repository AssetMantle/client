package constants

object Notification {

  lazy val PREFIX = "NOTIFICATION."
  lazy val TITLE_SUFFIX = ".TITLE"
  lazy val MESSAGE_SUFFIX = ".MESSAGE"

  val LOGIN = new Notification("LOGIN")
  val OTP = new Notification("OTP")
  val SUCCESS = new Notification("SUCCESS")
  val FAILURE = new Notification("FAILURE")

  class Notification(private val id: String) {
    val title: String = PREFIX + id + TITLE_SUFFIX
    val message: String = PREFIX + id + MESSAGE_SUFFIX
  }
}
