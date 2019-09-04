package models.master

import models.Abstract.BaseCaseClass

case class Address(addressLine1: String, addressLine2: String, landmark: Option[String] = None, city: String, country: String, zipCode: String, phone: String) extends BaseCaseClass
