package utilities

import models.master

object Contact {

  def getWarnings(contact: Option[master.Contact]): Seq[constants.Response.Warning] = {
    contact match {
      case Some(value) =>
        if (!value.emailAddressVerified && !value.mobileNumberVerified) Seq(constants.Response.VERIFY_EMAIL_ADDRESS, constants.Response.VERIFY_MOBILE_NUMBER)
        else if (!value.emailAddressVerified && value.mobileNumberVerified) Seq(constants.Response.VERIFY_EMAIL_ADDRESS)
        else if (value.emailAddressVerified && !value.mobileNumberVerified) Seq(constants.Response.VERIFY_MOBILE_NUMBER)
        else Seq()
      case None => Seq(constants.Response.UPDATE_CONTACT_DETAILS)
    }
  }

  def getStatus(contact: Option[master.Contact]): String = {
    contact match {
      case Some(value) =>
        if (!value.emailAddressVerified && !value.mobileNumberVerified) constants.Status.Contact.CONTACT_UNVERIFIED
        else if (!value.emailAddressVerified && value.mobileNumberVerified) constants.Status.Contact.EMAIL_ADDRESS_UNVERIFIED
        else if (value.emailAddressVerified && !value.mobileNumberVerified) constants.Status.Contact.MOBILE_NUMBER_UNVERIFIED
        else constants.Status.Contact.COMPLETE
      case None => constants.Status.Contact.NO_CONTACT
    }
  }

}
