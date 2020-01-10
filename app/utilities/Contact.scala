package utilities

object Contact {

  def getWarnings(status: String): Seq[constants.Response.Warning] = {
    status match {
      case constants.Status.Account.NO_CONTACT => Seq(constants.Response.UPDATE_CONTACT_DETAILS)
      case constants.Status.Account.CONTACT_UNVERIFIED => Seq(constants.Response.VERIFY_EMAIL_ADDRESS, constants.Response.VERIFY_MOBILE_NUMBER)
      case constants.Status.Account.EMAIL_ADDRESS_UNVERIFIED => Seq(constants.Response.VERIFY_EMAIL_ADDRESS)
      case constants.Status.Account.MOBILE_NUMBER_UNVERIFIED => Seq(constants.Response.VERIFY_MOBILE_NUMBER)
      case constants.Status.Account.COMPLETE => Seq()
    }
  }

}
