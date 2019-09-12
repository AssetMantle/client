package models.master

import models.Abstract.BaseCaseClass
import play.api.libs.json.{Json, OWrites, Reads}

object utils {

  case class UBO(personName: String, sharePercentage: Double, relationship: String, title: String) extends BaseCaseClass

  implicit val uboWrites: OWrites[UBO] = Json.writes[UBO]

  implicit val uboReads: Reads[UBO] = Json.reads[UBO]

  case class UBOs(ubos: Seq[UBO] = Seq()) extends BaseCaseClass

  implicit val ubosWrites: OWrites[UBOs] = Json.writes[UBOs]

  implicit val ubosReads: Reads[UBOs] = Json.reads[UBOs]

  case class Address(addressLine1: String, addressLine2: String, landmark: Option[String] = None, city: String, country: String, zipCode: String, phone: String) extends BaseCaseClass

  implicit val addressWrites: OWrites[Address] = Json.writes[Address]

  implicit val addressReads: Reads[Address] = Json.reads[Address]

}
