package constants

import models.common.Serializable.BaseProperty

class Property(val dataName: String, val dataType: String) {

  def getBaseProperty(value: String) = BaseProperty(dataType, dataName, Some(value))

}

object Property {

  val USER_TYPE = new Property(Blockchain.Parameters.USER_TYPE, constants.Blockchain.DataType.STRING_DATA)
  val ZONE_ID = new Property(Blockchain.Parameters.ZONE_ID, constants.Blockchain.DataType.STRING_DATA)
  val ORGANIZATION_ID = new Property(Blockchain.Parameters.ORGANIZATION_ID, constants.Blockchain.DataType.ID_DATA)
  val CURRENCY = new Property(Blockchain.Parameters.CURRENCY, constants.Blockchain.DataType.STRING_DATA)
  val NAME = new Property(Blockchain.Parameters.NAME, constants.Blockchain.DataType.STRING_DATA)
  val ADDRESS = new Property(Blockchain.Parameters.ADDRESS, constants.Blockchain.DataType.STRING_DATA)
  val ACCOUNT_ID = new Property(Blockchain.Parameters.ACCOUNT_ID, constants.Blockchain.DataType.STRING_DATA)

  val ASSET_TYPE = new Property(Blockchain.Parameters.ASSET_TYPE,constants.Blockchain.DataType.STRING_DATA)
  val ASSET_DESCRIPTION = new Property(Blockchain.Parameters.ASSET_DESCRIPTION,constants.Blockchain.DataType.STRING_DATA)
  val ASSET_PRICE_PER_UNIT = new Property(Blockchain.Parameters.ASSET_PRICE_PER_UNIT,constants.Blockchain.DataType.STRING_DATA)
  val ASSET_QUANTITY = new Property(Blockchain.Parameters.ASSET_QUANTITY,constants.Blockchain.DataType.STRING_DATA)
  val SHIPPING_PERIOD = new Property(Blockchain.Parameters.SHIPPING_PERIOD,constants.Blockchain.DataType.STRING_DATA)
  val PORT_OF_LOADING = new Property(Blockchain.Parameters.PORT_OF_LOADING,constants.Blockchain.DataType.STRING_DATA)
  val PORT_OF_DISCHARGE = new Property(Blockchain.Parameters.PORT_OF_DISCHARGE,constants.Blockchain.DataType.STRING_DATA)
  val QUANTITY_UNIT = new Property(Blockchain.Parameters.QUANTITY_UNIT,constants.Blockchain.DataType.STRING_DATA)

}