package utilities

import models.master

object Contact {

  def getWarnings(mobileNumber: Option[master.MobileNumber], emailAddress: Option[master.EmailAddress]): Seq[constants.Response.Warning] = {
//    contact match {
//      case Some(value) =>
//        if (!value.emailAddressVerified && !value.mobileNumberVerified) Seq(constants.Response.VERIFY_EMAIL_ADDRESS, constants.Response.VERIFY_MOBILE_NUMBER)
//        else if (!value.emailAddressVerified && value.mobileNumberVerified) Seq(constants.Response.VERIFY_EMAIL_ADDRESS)
//        else if (value.emailAddressVerified && !value.mobileNumberVerified) Seq(constants.Response.VERIFY_MOBILE_NUMBER)
//        else Seq()
//      case None => Seq(constants.Response.UPDATE_CONTACT_DETAILS)
//    }
    mobileNumber.map(number => if(!number.status) Seq(constants.Response.VERIFY_MOBILE_NUMBER) else Seq()).getOrElse(Seq(constants.Response.UPDATE_MOBILE_NUMBER)) ++
    emailAddress.map(address => if(!address.status)  Seq(constants.Response.VERIFY_EMAIL_ADDRESS) else Seq()).getOrElse(Seq(constants.Response.UPDATE_EMAIL_ADDRESS))

  }

  def getStatus(mobileNumber: Option[master.MobileNumber], emailAddress: Option[master.EmailAddress]): Seq[String] = {
    //    contact match {
    //      case Some(value) =>
    //        if (!value.emailAddressVerified && !value.mobileNumberVerified) constants.Status.Contact.CONTACT_UNVERIFIED
    //        else if (!value.emailAddressVerified && value.mobileNumberVerified) constants.Status.Contact.EMAIL_ADDRESS_UNVERIFIED
    //        else if (value.emailAddressVerified && !value.mobileNumberVerified) constants.Status.Contact.MOBILE_NUMBER_UNVERIFIED
    //        else constants.Status.Contact.COMPLETE
    //      case None => constants.Status.Contact.NO_CONTACT
    //    }
    //  }
    //USED AS seqA.sameElements(SeqB), note this matches the elements and their indexes as well
    Seq(mobileNumber.map(number => if (number.status) constants.Status.Contact.MOBILE_NUMBER_VERIFIED else constants.Status.Contact.MOBILE_NUMBER_UNVERIFIED).getOrElse(constants.Status.Contact.NO_MOBILE_NUMBER),
      emailAddress.map(address => if (address.status) constants.Status.Contact.EMAIL_ADDRESS_VERIFIED else constants.Status.Contact.EMAIL_ADDRESS_UNVERIFIED).getOrElse(constants.Status.Contact.NO_EMAIL_ADDRESS))
  }
}
