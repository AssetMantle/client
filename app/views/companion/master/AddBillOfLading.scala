package views.companion.master

import java.util.Date

import play.api.data.Form
import play.api.data.Forms._

object AddBillOfLading {

  val form = Form(
    mapping(
      constants.FormField.NEGOTIATION_ID.name -> constants.FormField.NEGOTIATION_ID.field,
      constants.FormField.BILL_OF_LADING_NUMBER.name -> constants.FormField.BILL_OF_LADING_NUMBER.field,
      constants.FormField.VESSEL_NAME.name -> constants.FormField.VESSEL_NAME.field,
      constants.FormField.PORT_OF_LOADING.name -> constants.FormField.PORT_OF_LOADING.field,
      constants.FormField.SHIPPER_NAME.name -> constants.FormField.SHIPPER_NAME.field,
      constants.FormField.SHIPPER_ADDRESS.name -> constants.FormField.SHIPPER_ADDRESS.field,
      constants.FormField.NOTIFY_PARTY_NAME.name -> constants.FormField.NOTIFY_PARTY_NAME.field,
      constants.FormField.NOTIFY_PARTY_ADDRESS.name -> constants.FormField.NOTIFY_PARTY_ADDRESS.field,
      constants.FormField.SHIPMENT_DATE.name -> constants.FormField.SHIPMENT_DATE.field,
      constants.FormField.DELIVERY_TERM.name -> constants.FormField.DELIVERY_TERM.field,
      constants.FormField.ASSET_DESCRIPTION.name -> constants.FormField.ASSET_DESCRIPTION.field,
      constants.FormField.ASSET_QUANTITY.name -> constants.FormField.ASSET_QUANTITY.field,
      constants.FormField.ASSET_PRICE.name -> constants.FormField.ASSET_PRICE.field,
    )(Data.apply)(Data.unapply)
  )

  case class Data(negotiationID: String, billOfLadingNumber: String, vesselName: String, portOfLoading: String, shipperName: String, shipperAddress: String, notifyPartyName: String, notifyPartyAddress: String, shipmentDate: Date, deliveryTerm: String, assetDescription: String, assetQuantity: Int, assetPrice: Int)

}
