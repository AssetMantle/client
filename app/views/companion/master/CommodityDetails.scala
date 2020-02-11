package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object CommodityDetails {
  val form = Form(
    mapping(
      constants.FormField.REQUEST_ID.name -> optional(constants.FormField.REQUEST_ID.field),
      constants.FormField.ASSET_TYPE.name -> constants.FormField.ASSET_TYPE.field,
      constants.FormField.ASSET_QUANTITY.name -> constants.FormField.ASSET_QUANTITY.field,
      constants.FormField.ASSET_PRICE.name -> constants.FormField.ASSET_PRICE.field,
    )(Data.apply)(Data.unapply)
  )

  case class Data(requestID: Option[String], assetType: String, assetPrice: Int, assetQuantity: Int)

}
