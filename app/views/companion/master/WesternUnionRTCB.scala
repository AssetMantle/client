package views.companion.master

import play.api.libs.json.{Json, OWrites, Reads}
import scala.xml.Elem
import constants.Form

object WesternUnionRTCB {

  case class Request(id: String, reference: String, externalReference: String, invoiceNumber: String,
                   buyerBusinessId: String, buyerFirstName: String, buyerLastName: String, createdDate: String,
                   lastUpdatedDate: String, status: String, dealType: String, paymentTypeId: String,
                   paidOutAmount: String, requestSignature: String) {

    def toXml: Elem = {
      <request>
        <id>{id}</id>

        <reference>{reference}</reference>

        <externalReference>{externalReference}</externalReference>

        <invoiceNumber>{invoiceNumber}</invoiceNumber>

        <buyerBusinessId>[buyerBusinessId]</buyerBusinessId>

        <buyerFirstName>{buyerFirstName}</buyerFirstName>

        <buyerLastName>{buyerLastName}</buyerLastName>

        <createdDate>{createdDate}</createdDate>

        <lastUpdatedDate>{lastUpdatedDate}</lastUpdatedDate>

        <status>{status}</status>

        <dealType>{dealType}</dealType>

        <paymentTypeId>{paymentTypeId}</paymentTypeId>

        <paidOutAmount>{paidOutAmount}</paidOutAmount>

        <requestSignature>{requestSignature}</requestSignature>
      </request>
    }
  }

  implicit val westernUnionRTCBReads: Reads[Request] = Json.reads[Request]

  implicit val westernUnionRTCBWrites: OWrites[Request] = Json.writes[Request]

  def fromXml(xmlRequest: scala.xml.NodeSeq): Request = {
    Request((xmlRequest \ Form.WU_RTCB_ID).text.trim, (xmlRequest \ Form.REFERENCE).text.trim, (xmlRequest \ Form.EXTERNAL_REFERENCE).text.trim,
      (xmlRequest \ Form.INVOICE_NUMBER).text.trim, (xmlRequest \ Form.BUYER_BUSINESS_ID).text.trim, (xmlRequest \ Form.BUYER_FIRST_NAME).text.trim,
      (xmlRequest \ Form.BUYER_LAST_NAME).text.trim, (xmlRequest \ Form.CREATED_DATE).text.trim, (xmlRequest \ Form.LAST_UPDATED_DATE).text.trim,
      (xmlRequest \ Form.WU_RTCB_STATUS).text.trim, (xmlRequest \ Form.DEAL_TYPE).text.trim, (xmlRequest \ Form.PAYMENT_TYPE_ID).text.trim,
      (xmlRequest \ Form.PAID_OUT_AMOUNT).text.trim, (xmlRequest \ Form.REQUEST_SIGNATURE).text.trim)
  }
}
