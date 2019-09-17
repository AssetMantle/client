package models.common

import java.util.Date

import play.api.libs.json.{Json, OWrites, Reads}
import models.Trait.Context
object Serializable {

  case class ShipmentDetails(commodityName: String, quality: String, deliveryTerm: String, tradeType: String, portOfLoading: String, portOfDischarge: String, shipmentDate: Date)
  implicit val shipmentDetailsReads: Reads[ShipmentDetails] = Json.reads[ShipmentDetails]
  implicit val shipmentDetailsWrites: OWrites[ShipmentDetails] = Json.writes[ShipmentDetails]

  case class OBL(billOfLadingID: String, portOfLoading: String, shipperName: String, shipperAddress: String, notifyPartyName: String, notifyPartyAddress: String, dateOfShipping: Date, deliveryTerm: String, weightOfConsignment: Int, declaredAssetValue: Int) extends models.Trait.Context

  implicit val oblReads: Reads[OBL] = Json.reads[OBL]
  implicit val oblWrites: OWrites[OBL] = Json.writes[OBL]

  case class Invoice(invoiceNumber: String, invoiceDate: Date)

  implicit val invoiceReads: Reads[Invoice] = Json.reads[Invoice]
  implicit val invoiceWrites: OWrites[Invoice] = Json.writes[Invoice]

}
