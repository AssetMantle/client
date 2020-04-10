package models.common

import play.api.libs.functional.syntax._
import java.util.Date

import models.Trait.DocumentContent
import play.api.libs.json.{JsResult, JsValue, Json, OWrites, Reads, Writes}

object Serializable {

  case class Address(addressLine1: String, addressLine2: String, landmark: Option[String] = None, city: String, country: String, zipCode: String, phone: String)

  implicit val addressWrites: OWrites[Address] = Json.writes[Address]

  implicit val addressReads: Reads[Address] = Json.reads[Address]

  case class UBO(personName: String, sharePercentage: Double, relationship: String, title: String)

  implicit val uboReads: Reads[UBO] = Json.reads[UBO]

  implicit val ubosReads: Reads[UBOs] = Json.reads[UBOs]

  case class UBOs(data: Seq[UBO])

  implicit val uboWrites: OWrites[UBO] = Json.writes[UBO]

  implicit val ubosWrites: OWrites[UBOs] = Json.writes[UBOs]

  case class ShipmentDetails(commodityName: String, quality: String, deliveryTerm: String, tradeType: String, portOfLoading: String, portOfDischarge: String, shipmentDate: Date)

  implicit val shipmentDetailsReads: Reads[ShipmentDetails] = Json.reads[ShipmentDetails]

  implicit val shipmentDetailsWrites: OWrites[ShipmentDetails] = Json.writes[ShipmentDetails]

  case class TradeActivityMessage(header: String, parameters: Seq[String])

  implicit val tradeActivityMessageReads: Reads[TradeActivityMessage] = Json.reads[TradeActivityMessage]

  implicit val tradeActivityMessageWrites: OWrites[TradeActivityMessage] = Json.writes[TradeActivityMessage]

  case class OBL(billOfLadingID: String, portOfLoading: String, shipperName: String, shipperAddress: String, notifyPartyName: String, notifyPartyAddress: String, dateOfShipping: Date, deliveryTerm: String, weightOfConsignment: Int, declaredAssetValue: Int) extends DocumentContent

  case class Invoice(invoiceNumber: String, invoiceDate: Date) extends DocumentContent

  implicit val documentContentWrites = new Writes[DocumentContent] {
    override def writes(documentContent: DocumentContent): JsValue = documentContent match {
      case obl: OBL => Json.toJson(obl)(Json.writes[OBL])
      case invoice: Invoice => Json.toJson(invoice)(Json.writes[Invoice])
    }
  }

  implicit val documentContentReads: Reads[DocumentContent] =
    Json.format[OBL].map(x => x: DocumentContent) or
      Json.format[Invoice].map(x => x: DocumentContent)

  case class DocumentBlockchainDetails(documentType: String, documentHash: String)

  implicit val documentBlockchainDetailsReads: Reads[DocumentBlockchainDetails] = Json.reads[DocumentBlockchainDetails]
  implicit val documentBlockchainDetailsWrites: OWrites[DocumentBlockchainDetails] = Json.writes[DocumentBlockchainDetails]
}
