package views.companion.master

import java.util.Date

import play.api.data.Form
import play.api.data.Forms._

object IssueAssetOBL {

  val form = Form(
    mapping(
      constants.FormField.ISSUE_ASSET_REQUEST_ID.name -> constants.FormField.ISSUE_ASSET_REQUEST_ID.field,
      constants.FormField.BILL_OF_LADING_NUMBER.name -> constants.FormField.BILL_OF_LADING_NUMBER.field,
      constants.FormField.PORT_OF_LOADING.name -> constants.FormField.PORT_OF_LOADING.field,
      constants.FormField.SHIPPER_NAME.name -> constants.FormField.SHIPPER_NAME.field,
      constants.FormField.SHIPPER_ADDRESS.name -> constants.FormField.SHIPPER_ADDRESS.field,
      constants.FormField.SHIPMENT_DATE.name -> constants.FormField.SHIPMENT_DATE.field,
      constants.FormField.NOTIFY_PARTY_NAME.name -> constants.FormField.NOTIFY_PARTY_NAME.field,
      constants.FormField.NOTIFY_PARTY_ADDRESS.name -> constants.FormField.NOTIFY_PARTY_ADDRESS.field,
      constants.FormField.DELIVERY_TERM.name -> constants.FormField.DELIVERY_TERM.field,
      constants.FormField.ASSET_QUANTITY.name -> constants.FormField.ASSET_QUANTITY.field,
      constants.FormField.ASSET_PRICE.name -> constants.FormField.ASSET_PRICE.field,
    )(Data.apply)(Data.unapply)
  )

  case class Data(requestID: String, billOfLadingNumber: String, portOfLoading: String, shipperName: String, shipperAddress: String, shipmentDate: Date, notifyPartyName: String, notifyPartyAddress: String, deliveryTerm: String, assetQuantity: Int, assetPrice: Int)

}

