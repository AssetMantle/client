package views.companion.master

import play.api.data.Form
import play.api.data.Forms.{mapping, optional, seq}
import utilities.MicroNumber

object DeputizeOrganization {

  val form = Form(
    mapping(
      constants.FormField.ORGANIZATION_ID.name -> constants.FormField.ORGANIZATION_ID.field,
      constants.FormField.ADD_TRADER.name -> constants.FormField.ADD_TRADER.field,
      constants.FormField.CREATE_MODERATED_ASSET.name -> constants.FormField.CREATE_MODERATED_ASSET.field,
      constants.FormField.CREATE_UNMODERATED_ASSET.name -> constants.FormField.CREATE_UNMODERATED_ASSET.field,
      constants.FormField.CREATE_ORDER.name -> constants.FormField.CREATE_ORDER.field,
      constants.FormField.GAS.name -> constants.FormField.GAS.field,
      constants.FormField.PASSWORD.name -> constants.FormField.PASSWORD.field,
    )(Data.apply)(Data.unapply)
  )

  case class Data(organizationID: String, addTrader:Boolean, createModeratedAsset:Boolean, createUnmoderatedAsset:Boolean, createOrder:Boolean, gas:MicroNumber, password: String)

}