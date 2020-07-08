package models.common

import play.api.libs.functional.syntax._
import java.sql.Date

import exceptions.BaseException
import models.Abstract.{AssetDocumentContent, NegotiationDocumentContent}
import play.api.Logger
import play.api.libs.json.{Json, OWrites, Reads, Writes}
import utilities.MicroNumber

object Serializable {

  private implicit val module: String = constants.Module.SERIALIZABLE

  private implicit val logger: Logger = Logger(this.getClass)

  case class Address(addressLine1: String, addressLine2: String, landmark: Option[String] = None, city: String, country: String, zipCode: String, phone: String)

  implicit val addressWrites: OWrites[Address] = Json.writes[Address]

  implicit val addressReads: Reads[Address] = Json.reads[Address]

  case class ShippingDetails(shippingPeriod: Int, portOfLoading: String, portOfDischarge: String)

  implicit val shippingDetailsReads: Reads[ShippingDetails] = Json.reads[ShippingDetails]

  implicit val shippingDetailsWrites: OWrites[ShippingDetails] = Json.writes[ShippingDetails]

  case class AssetOtherDetails(shippingDetails: ShippingDetails)

  implicit val assetOtherDetailsReads: Reads[AssetOtherDetails] = Json.reads[AssetOtherDetails]

  implicit val assetOtherDetailsWrites: OWrites[AssetOtherDetails] = Json.writes[AssetOtherDetails]

  case class Credit(tenure: Option[Int] = None, tentativeDate: Option[Date] = None, reference: Option[String])

  implicit val creditReads: Reads[Credit] = Json.reads[Credit]

  implicit val creditWrites: OWrites[Credit] = Json.writes[Credit]

  case class PaymentTerms(advancePercentage: Double = 0.0, credit: Option[Credit] = None)

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

  case class BillOfLading(id: String, consigneeTo: String, vesselName: String, portOfLoading: String, portOfDischarge: String, shipperName: String, shipperAddress: String, notifyPartyName: String, notifyPartyAddress: String, dateOfShipping: Date, deliveryTerm: String, assetDescription: String, assetQuantity: MicroNumber, quantityUnit: String, declaredAssetValue: MicroNumber) extends AssetDocumentContent

  implicit val assetDocumentContentWrites: Writes[AssetDocumentContent] = {
    case billOfLading: BillOfLading => Json.toJson(billOfLading)(Json.writes[BillOfLading])
    case _ => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
  }

  implicit val assetDocumentContentReads: Reads[AssetDocumentContent] = {
    Json.format[BillOfLading].map(x => x: AssetDocumentContent)
  }

  case class Invoice(invoiceNumber: String, invoiceAmount: MicroNumber, invoiceDate: Date) extends NegotiationDocumentContent

  case class Contract(contractNumber: String) extends NegotiationDocumentContent

  implicit val negotiationDocumentContentWrites: Writes[NegotiationDocumentContent] = {
    case invoice: Invoice => Json.toJson(invoice)(Json.writes[Invoice])
    case contract: Contract => Json.toJson(contract)(Json.writes[Contract])
    case _ => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
  }

  implicit val negotiationDocumentContentReads: Reads[NegotiationDocumentContent] = {
    Json.format[Invoice].map(x => x: NegotiationDocumentContent) or
      Json.format[Contract].map(x => x: NegotiationDocumentContent)
  }
}
