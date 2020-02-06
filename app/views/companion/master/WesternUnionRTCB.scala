package views.companion.master

import constants.XMLTag._

import scala.xml.Elem

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

  object Request {

    def fromXml(xmlRequest: scala.xml.NodeSeq): Request = {
      new Request((xmlRequest \ "id").text.trim, (xmlRequest \ "reference").text.trim, (xmlRequest \ "externalReference").text.trim,
        (xmlRequest \ "invoiceNumber").text.trim, (xmlRequest \ "buyerBusinessId").text.trim, (xmlRequest \ "buyerFirstName").text.trim,
        (xmlRequest \ "buyerLastName").text.trim, (xmlRequest \ "createdDate").text.trim, (xmlRequest \ "lastUpdatedDate").text.trim,
        (xmlRequest \ "status").text.trim, (xmlRequest \ "dealType").text.trim, (xmlRequest \ "paymentTypeId").text.trim,
        (xmlRequest \ "paidOutAmount").text.trim, (xmlRequest \ "requestSignature").text.trim)
    }

  }
}
