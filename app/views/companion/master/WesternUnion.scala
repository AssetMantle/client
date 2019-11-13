package views.companion.master

import constants.XMLTag._

object WesternUnion {

  case class Request(id: String, reference: String, externalReference: String, invoiceNumber: String,
                   buyerBusinessId: String, buyerFirstName: String, buyerLastName: String, createdDate: String,
                   lastUpdatedDate: String, status: String, dealType: String, paymentTypeId: String,
                   paidOutAmount: String, requestSignature: String) {

    def toXml = {
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
      new Request((xmlRequest \ "id").text, (xmlRequest \ "reference").text, (xmlRequest \ "externalReference").text,
        (xmlRequest \ "invoiceNumber").text, (xmlRequest \ "buyerBusinessId").text, (xmlRequest \ "buyerFirstName").text,
        (xmlRequest \ "buyerLastName").text, (xmlRequest \ "createdDate").text, (xmlRequest \ "lastUpdatedDate").text,
        (xmlRequest \ "status").text, (xmlRequest \ "dealType").text, (xmlRequest \ "paymentTypeId").text,
        (xmlRequest \ "paidOutAmount").text, (xmlRequest \ "requestSignature").text)
    }

  }
}
