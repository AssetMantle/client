package models.common

import play.api.libs.functional.syntax._
import java.sql.Date

import exceptions.BaseException
import models.Abstract.{AssetDocumentContent, NegotiationDocumentContent}
import play.api.libs.json.{JsResult, JsValue, Json, OWrites, Reads, Writes}

object Serializable {

  private implicit val module: String = constants.Module.SERIALIZABLE

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

  case class ShippingDetails(shippingPeriod: Int, portOfLoading: String, portOfDischarge: String)

  implicit val shippingDetailsReads: Reads[ShippingDetails] = Json.reads[ShippingDetails]

  implicit val shippingDetailsWrites: OWrites[ShippingDetails] = Json.writes[ShippingDetails]

  case class AssetOtherDetails(shippingDetails: ShippingDetails)

  implicit val assetOtherDetailsReads: Reads[AssetOtherDetails] = Json.reads[AssetOtherDetails]

  implicit val assetOtherDetailsWrites: OWrites[AssetOtherDetails] = Json.writes[AssetOtherDetails]

  case class PaymentTerms(advancePayment: Boolean = false, advancePercentage: Option[Double] = None, credit: Boolean = false, tenure: Option[Int] = None, tentativeDate: Option[Date] = None, reference: Option[String] = None)

  implicit val paymentTermsReads: Reads[PaymentTerms] = Json.reads[PaymentTerms]

  implicit val paymentTermsWrites: OWrites[PaymentTerms] = Json.writes[PaymentTerms]

  case class DocumentList(assetDocuments: Seq[String], negotiationDocuments: Seq[String])

  implicit val documentListReads: Reads[DocumentList] = Json.reads[DocumentList]

  implicit val documentListWrites: OWrites[DocumentList] = Json.writes[DocumentList]

  case class NotificationTemplate(template: String, parameters: Seq[String])

  implicit val notificationTemplateReads: Reads[NotificationTemplate] = Json.reads[NotificationTemplate]

  implicit val notificationTemplateWrites: OWrites[NotificationTemplate] = Json.writes[NotificationTemplate]

  case class TradeActivityTemplate(template: String, parameters: Seq[String])

  implicit val tradeActivityTemplateReads: Reads[TradeActivityTemplate] = Json.reads[TradeActivityTemplate]

  implicit val tradeActivityTemplateWrites: OWrites[TradeActivityTemplate] = Json.writes[TradeActivityTemplate]

  case class OBL(billOfLadingID: String, portOfLoading: String, shipperName: String, shipperAddress: String, notifyPartyName: String, notifyPartyAddress: String, dateOfShipping: Date, deliveryTerm: String, weightOfConsignment: Int, declaredAssetValue: Int) extends AssetDocumentContent

  implicit val assetDocumentContentWrites: Writes[AssetDocumentContent] = {
    case obl: OBL => Json.toJson(obl)(Json.writes[OBL])
    case _ => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
  }

  implicit val assetDocumentContentReads: Reads[AssetDocumentContent] = {
    Json.format[OBL].map(x => x: AssetDocumentContent)
  }

  case class Invoice(invoiceNumber: String, invoiceDate: Date) extends NegotiationDocumentContent
  case class Contract(contractNumber: String) extends NegotiationDocumentContent

  implicit val negotiationDocumentContentWrites: Writes[NegotiationDocumentContent] = {
    case invoice: Invoice => Json.toJson(invoice)(Json.writes[Invoice])
    case contract: Contract => Json.toJson(contract)(Json.writes[Contract])
    case _ => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
  }

  implicit val negotiationDocumentContentReads: Reads[NegotiationDocumentContent] = {
    Json.format[Invoice].map(x => x: NegotiationDocumentContent)
  }
}
