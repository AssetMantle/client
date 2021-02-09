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
  val EXTRA_INFO = new Property(Blockchain.Parameters.EXTRA_INFO, constants.Blockchain.DataType.STRING_DATA)
  val ACCOUNT_ID = new Property(Blockchain.Parameters.ACCOUNT_ID, constants.Blockchain.DataType.STRING_DATA)

}