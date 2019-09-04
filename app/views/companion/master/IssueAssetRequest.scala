package views.companion.master

import java.util.Date

import play.api.data.Form
import play.api.data.Forms._

object IssueAssetRequest {

  val form = Form(
    mapping(
      constants.FormField.DOCUMENT_HASH.name -> constants.FormField.DOCUMENT_HASH.field,
      constants.FormField.ASSET_TYPE.name -> constants.FormField.ASSET_TYPE.field,
      constants.FormField.QUANTITY_UNIT.name -> constants.FormField.QUANTITY_UNIT.field,
      constants.FormField.ASSET_QUANTITY.name -> constants.FormField.ASSET_QUANTITY.field,
      constants.FormField.ASSET_PRICE.name -> constants.FormField.ASSET_PRICE.field,
      constants.Form.MODERATED -> boolean,
      constants.FormField.GAS.name -> constants.FormField.GAS.field,
      constants.FormField.TAKER_ADDRESS.name -> constants.FormField.TAKER_ADDRESS.field,
      constants.FormField.COMMODITY_NAME.name -> constants.FormField.COMMODITY_NAME.field,
      constants.FormField.QUALITY.name -> constants.FormField.QUALITY.field,
      constants.FormField.DELIVERY_TERM.name -> constants.FormField.DELIVERY_TERM.field,
      constants.FormField.TRADE_TYPE.name -> constants.FormField.TRADE_TYPE.field,
      constants.FormField.PORT_OF_LOADING.name -> constants.FormField.PORT_OF_LOADING.field,
      constants.FormField.PORT_OF_DISCHARGE.name -> constants.FormField.PORT_OF_DISCHARGE.field,
      constants.FormField.SHIPMENT_DATE.name -> constants.FormField.SHIPMENT_DATE.field,
      constants.FormField.PHYSICAL_DOCUMENTS_HANDLED_VIA.name -> constants.FormField.PHYSICAL_DOCUMENTS_HANDLED_VIA.field,
      constants.FormField.COMDEX_PAYMENT_TERMS.name -> constants.FormField.COMDEX_PAYMENT_TERMS.field,
      constants.FormField.PASSWORD.name -> constants.FormField.PASSWORD.field,
    )(Data.apply)(Data.unapply)
  )

  case class Data(documentHash: String, assetType: String, quantityUnit: String, assetQuantity: Int, assetPrice: Int, moderated: Boolean, gas: Int, takerAddress: String, commodityName: String, quality: String, deliveryTerm: String, tradeType: String, portOfLoading: String, portOfDischarge: String, shipmentDate: Date, physicalDocumentsHandledVia: String, comdexPaymentTerms: String, password: String)

}

