package views.companion.common

import models.common.Serializable
import play.api.data.Forms.mapping
import play.api.data.Mapping
import utilities.MicroNumber

object Coin {

  val subFormMapping: Mapping[Data] = mapping(
    constants.FormField.DENOM.name -> constants.FormField.DENOM.field,
    constants.FormField.AMOUNT.name -> constants.FormField.AMOUNT.field,
  )(Data.apply)(Data.unapply)

  case class Data(denom: String, amount: MicroNumber) {
    def toCoin: Serializable.Coin = Serializable.Coin(denom = denom, amount = amount)
  }

}
