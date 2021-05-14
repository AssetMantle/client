package views.companion.master

import play.api.data.Form
import play.api.data.Forms.{mapping, optional, seq}
import utilities.MicroNumber

object DeputizeZone {

  val form = Form(
    mapping(
      constants.FormField.ZONE_ID.name -> constants.FormField.ZONE_ID.field,
      constants.FormField.ADD_ORGANIZATION.name -> constants.FormField.ADD_ORGANIZATION.field,
      constants.FormField.ADD_TRADER.name -> constants.FormField.ADD_TRADER.field,
      constants.FormField.CREATE_MODERATED_ASSET.name -> constants.FormField.CREATE_MODERATED_ASSET.field,
      constants.FormField.CREATE_UNMODERATED_ASSET.name -> constants.FormField.CREATE_UNMODERATED_ASSET.field,
      constants.FormField.CREATE_FIAT.name -> constants.FormField.CREATE_FIAT.field,
      constants.FormField.CREATE_ORDER.name -> constants.FormField.CREATE_ORDER.field,
      constants.FormField.GAS.name -> constants.FormField.GAS.field,
      constants.FormField.PASSWORD.name -> constants.FormField.PASSWORD.field,
    )(Data.apply)(Data.unapply)
  )

  case class Data(zoneID: String, addOrganization: Boolean, addTrader:Boolean, createModeratedAsset:Boolean, createUnmoderatedAsset:Boolean, createFiat:Boolean, createOrder:Boolean, gas:MicroNumber, password: String)

}