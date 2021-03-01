package constants

import models.common.Serializable.BaseProperty


object Property {

  val USER_TYPE: BaseProperty = BaseProperty(Blockchain.Parameters.USER_TYPE, constants.Blockchain.DataType.STRING_DATA, None)
  val ZONE_ID: BaseProperty = BaseProperty(Blockchain.Parameters.ZONE_ID, constants.Blockchain.DataType.STRING_DATA, None)
  val ORGANIZATION_ID: BaseProperty = BaseProperty(Blockchain.Parameters.ORGANIZATION_ID, constants.Blockchain.DataType.STRING_DATA, None)
  val CURRENCY: BaseProperty = BaseProperty(Blockchain.Parameters.CURRENCY, constants.Blockchain.DataType.STRING_DATA, None)
  val NAME: BaseProperty = BaseProperty(Blockchain.Parameters.NAME, constants.Blockchain.DataType.STRING_DATA, None)
  val ADDRESS: BaseProperty = BaseProperty(Blockchain.Parameters.ADDRESS, constants.Blockchain.DataType.STRING_DATA, None)
  val ACCOUNT_ID: BaseProperty = BaseProperty(Blockchain.Parameters.ACCOUNT_ID, constants.Blockchain.DataType.STRING_DATA, None)

  val ASSET_TYPE: BaseProperty = BaseProperty(Blockchain.Parameters.ASSET_TYPE, constants.Blockchain.DataType.STRING_DATA, None)
  val ASSET_DESCRIPTION: BaseProperty = BaseProperty(Blockchain.Parameters.ASSET_DESCRIPTION, constants.Blockchain.DataType.STRING_DATA, None)
  val ASSET_PRICE_PER_UNIT: BaseProperty = BaseProperty(Blockchain.Parameters.ASSET_PRICE_PER_UNIT, constants.Blockchain.DataType.STRING_DATA, None)
  val ASSET_QUANTITY: BaseProperty = BaseProperty(Blockchain.Parameters.ASSET_QUANTITY, constants.Blockchain.DataType.STRING_DATA, None)
  val SHIPPING_PERIOD: BaseProperty = BaseProperty(Blockchain.Parameters.SHIPPING_PERIOD, constants.Blockchain.DataType.STRING_DATA, None)
  val PORT_OF_LOADING: BaseProperty = BaseProperty(Blockchain.Parameters.PORT_OF_LOADING, constants.Blockchain.DataType.STRING_DATA, None)
  val PORT_OF_DISCHARGE: BaseProperty = BaseProperty(Blockchain.Parameters.PORT_OF_DISCHARGE, constants.Blockchain.DataType.STRING_DATA, None)
  val LOCKED: BaseProperty = BaseProperty(Blockchain.Parameters.LOCKED, constants.Blockchain.DataType.STRING_DATA, None)
  val MODERATED: BaseProperty = BaseProperty(Blockchain.Parameters.MODERATED, constants.Blockchain.DataType.STRING_DATA, None)

  val QUANTITY_UNIT: BaseProperty = BaseProperty(Blockchain.Parameters.QUANTITY_UNIT, constants.Blockchain.DataType.STRING_DATA, None)
  val ORDER_TYPE: BaseProperty = BaseProperty(Blockchain.Parameters.ORDER_TYPE, constants.Blockchain.DataType.STRING_DATA, None)
  val FIAT_PROOF: BaseProperty = BaseProperty(Blockchain.Parameters.FIAT_PROOF, constants.Blockchain.DataType.STRING_DATA, None)
  val BUYER_ACCOUNT_ID: BaseProperty = BaseProperty(Blockchain.Parameters.BUYER_ACCOUNT_ID, constants.Blockchain.DataType.STRING_DATA, None)
  val SELLER_ACCOUNT_ID: BaseProperty = BaseProperty(Blockchain.Parameters.SELLER_ACCOUNT_ID, constants.Blockchain.DataType.STRING_DATA, None)
  val EXCHANGE_RATE: BaseProperty = BaseProperty(Blockchain.Parameters.EXCHANGE_RATE, constants.Blockchain.DataType.DEC_DATA, None)
  val MAKER_OWNABLE_SPLIT: BaseProperty = BaseProperty(Blockchain.Parameters.MAKER_OWNABLE_SPLIT, constants.Blockchain.DataType.DEC_DATA, None)

  val PRICE: BaseProperty = BaseProperty(Blockchain.Parameters.PRICE, constants.Blockchain.DataType.DEC_DATA, None)
  val QUANTITY: BaseProperty = BaseProperty(Blockchain.Parameters.QUANTITY, constants.Blockchain.DataType.DEC_DATA, None)
  val TAKER_ID: BaseProperty = BaseProperty(Blockchain.Parameters.TAKER_ID, constants.Blockchain.DataType.ID_DATA, None)

  val TYPE: BaseProperty = BaseProperty(Blockchain.Parameters.TYPE, constants.Blockchain.DataType.STRING_DATA, None)
  val AMOUNT: BaseProperty = BaseProperty(Blockchain.Parameters.AMOUNT, constants.Blockchain.DataType.DEC_DATA, None)
  val NEGOTIATION_ID: BaseProperty = BaseProperty(Blockchain.Parameters.NEGOTIATION_ID, constants.Blockchain.DataType.STRING_DATA, None)
  val EXTRA_INFO: BaseProperty = BaseProperty(Blockchain.Parameters.EXTRA_INFO, constants.Blockchain.DataType.STRING_DATA, None)
}