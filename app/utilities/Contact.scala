package utilities

import models.master

object Contact {

  def getWarnings(mobile: Option[master.Mobile], email: Option[master.Email]): Seq[constants.Response.Warning] = {
    mobile.map(number => if(!number.status) Seq(constants.Response.VERIFY_MOBILE_NUMBER) else Seq()).getOrElse(Seq(constants.Response.UPDATE_MOBILE_NUMBER)) ++
    email.map(address => if(!address.status)  Seq(constants.Response.VERIFY_EMAIL_ADDRESS) else Seq()).getOrElse(Seq(constants.Response.UPDATE_EMAIL_ADDRESS))

  }

  def getStatus(mobile: Option[master.Mobile], email: Option[master.Email]): Seq[String] = {

    //USED AS seqA.sameElements(SeqB), note this matches the elements and their indexes as well
    Seq(mobile.map(number => if (number.status) constants.Status.Contact.MOBILE_NUMBER_VERIFIED else constants.Status.Contact.MOBILE_NUMBER_UNVERIFIED).getOrElse(constants.Status.Contact.NO_MOBILE_NUMBER),
      email.map(address => if (address.status) constants.Status.Contact.EMAIL_ADDRESS_VERIFIED else constants.Status.Contact.EMAIL_ADDRESS_UNVERIFIED).getOrElse(constants.Status.Contact.NO_EMAIL_ADDRESS))
  }
}
