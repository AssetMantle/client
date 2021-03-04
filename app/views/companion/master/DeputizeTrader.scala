package views.companion.master

import play.api.data.Form
import play.api.data.Forms.mapping
import utilities.MicroNumber

object DeputizeTrader {

  val form = Form(
    mapping(
      constants.FormField.TRADE_ID.name -> constants.FormField.TRADE_ID.field,
      constants.FormField.CREATE_UNMODERATED_ASSET.name -> constants.FormField.CREATE_UNMODERATED_ASSET.field,
      constants.FormField.CREATE_FIAT.name -> constants.FormField.CREATE_FIAT.field,
      constants.FormField.CREATE_ORDER.name -> constants.FormField.CREATE_ORDER.field,
      constants.FormField.GAS.name -> constants.FormField.GAS.field,
      constants.FormField.PASSWORD.name -> constants.FormField.PASSWORD.field,
    )(Data.apply)(Data.unapply)
  )

  case class Data(traderID: String, createUnmoderatedAsset: Boolean, createFiat:Boolean, createOrder: Boolean, gas: MicroNumber, password: String)

}