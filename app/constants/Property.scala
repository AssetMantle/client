package constants

import models.common.Serializable.BaseProperty


object Property {

  val USER_TYPE: BaseProperty = BaseProperty(constants.Blockchain.DataType.STRING_DATA, Blockchain.Parameters.USER_TYPE, None)
  val ZONE_ID: BaseProperty = BaseProperty(constants.Blockchain.DataType.STRING_DATA, Blockchain.Parameters.ZONE_ID, None)
  val ORGANIZATION_ID: BaseProperty = BaseProperty(constants.Blockchain.DataType.STRING_DATA, Blockchain.Parameters.ORGANIZATION_ID, None)
  val CURRENCY: BaseProperty = BaseProperty(constants.Blockchain.DataType.STRING_DATA, Blockchain.Parameters.CURRENCY, None)
  val NAME: BaseProperty = BaseProperty(constants.Blockchain.DataType.STRING_DATA, Blockchain.Parameters.NAME, None)
  val ADDRESS: BaseProperty = BaseProperty(constants.Blockchain.DataType.STRING_DATA, Blockchain.Parameters.ADDRESS, None)
  val ACCOUNT_ID: BaseProperty = BaseProperty(constants.Blockchain.DataType.STRING_DATA, Blockchain.Parameters.ACCOUNT_ID, None)

  val ASSET_TYPE: BaseProperty = BaseProperty(constants.Blockchain.DataType.STRING_DATA, Blockchain.Parameters.ASSET_TYPE, None)
  val ASSET_DESCRIPTION: BaseProperty = BaseProperty(constants.Blockchain.DataType.STRING_DATA, Blockchain.Parameters.ASSET_DESCRIPTION, None)
  val ASSET_PRICE_PER_UNIT: BaseProperty = BaseProperty(constants.Blockchain.DataType.STRING_DATA, Blockchain.Parameters.ASSET_PRICE_PER_UNIT, None)
  val ASSET_QUANTITY: BaseProperty = BaseProperty(constants.Blockchain.DataType.STRING_DATA, Blockchain.Parameters.ASSET_QUANTITY, None)
  val SHIPPING_PERIOD: BaseProperty = BaseProperty(constants.Blockchain.DataType.STRING_DATA, Blockchain.Parameters.SHIPPING_PERIOD, None)
  val PORT_OF_LOADING: BaseProperty = BaseProperty(constants.Blockchain.DataType.STRING_DATA, Blockchain.Parameters.PORT_OF_LOADING, None)
  val PORT_OF_DISCHARGE: BaseProperty = BaseProperty(constants.Blockchain.DataType.STRING_DATA, Blockchain.Parameters.PORT_OF_DISCHARGE, None)
  val LOCKED: BaseProperty = BaseProperty(constants.Blockchain.DataType.STRING_DATA, Blockchain.Parameters.LOCKED, None)
  val MODERATED: BaseProperty = BaseProperty(constants.Blockchain.DataType.STRING_DATA, Blockchain.Parameters.MODERATED, None)

  val QUANTITY_UNIT: BaseProperty = BaseProperty(constants.Blockchain.DataType.STRING_DATA, Blockchain.Parameters.QUANTITY_UNIT, None)
  val ORDER_TYPE: BaseProperty = BaseProperty(constants.Blockchain.DataType.STRING_DATA, Blockchain.Parameters.ORDER_TYPE, None)
  val FIAT_PROOF: BaseProperty = BaseProperty(constants.Blockchain.DataType.STRING_DATA, Blockchain.Parameters.FIAT_PROOF, None)
  val BUYER_ACCOUNT_ID: BaseProperty = BaseProperty(constants.Blockchain.DataType.STRING_DATA, Blockchain.Parameters.BUYER_ACCOUNT_ID, None)
  val SELLER_ACCOUNT_ID: BaseProperty = BaseProperty(constants.Blockchain.DataType.STRING_DATA, Blockchain.Parameters.SELLER_ACCOUNT_ID, None)
  val EXCHANGE_RATE: BaseProperty = BaseProperty(constants.Blockchain.DataType.DEC_DATA, Blockchain.Parameters.EXCHANGE_RATE, None)
  val MAKER_OWNABLE_SPLIT: BaseProperty = BaseProperty(constants.Blockchain.DataType.DEC_DATA, Blockchain.Parameters.MAKER_OWNABLE_SPLIT, None)

  val PRICE: BaseProperty = BaseProperty(constants.Blockchain.DataType.DEC_DATA, Blockchain.Parameters.PRICE, None)
  val QUANTITY: BaseProperty = BaseProperty(constants.Blockchain.DataType.DEC_DATA, Blockchain.Parameters.QUANTITY, None)
  val TAKER_ID: BaseProperty = BaseProperty(constants.Blockchain.DataType.ID_DATA, Blockchain.Parameters.TAKER_ID, None)

  val TYPE: BaseProperty = BaseProperty(constants.Blockchain.DataType.STRING_DATA, Blockchain.Parameters.TYPE, None)
  val AMOUNT: BaseProperty = BaseProperty(constants.Blockchain.DataType.DEC_DATA, Blockchain.Parameters.AMOUNT, None)
  val NEGOTIATION_ID: BaseProperty = BaseProperty(constants.Blockchain.DataType.STRING_DATA, Blockchain.Parameters.NEGOTIATION_ID, None)
  val EXTRA_INFO: BaseProperty = BaseProperty(constants.Blockchain.DataType.STRING_DATA, Blockchain.Parameters.EXTRA_INFO, None)
}