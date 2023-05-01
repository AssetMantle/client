package schema.constants

import schema.data.Data
import schema.data.base._
import schema.id.base._
import schema.property.base.MetaProperty
import schema.types.Height

object Properties {

  val Mint: StringID = StringID("mint")
  val Burn: StringID = StringID("burn")
  val Renumerate: StringID = StringID("renumerate")
  val Add: StringID = StringID("add")
  val Remove: StringID = StringID("remove")
  val Mutate: StringID = StringID("mutate")

  val AuthenticationProperty: MetaProperty = MetaProperty(id = PropertyID(keyID = StringID("authentication"), typeID = constants.Data.ListDataTypeID), data = ListData(Seq[Data]()))
  val BondAmountProperty: MetaProperty = MetaProperty(id = PropertyID(keyID = StringID("bondAmount"), typeID = constants.Data.NumberDataTypeID), data = NumberData(0))
  val BondRateProperty: MetaProperty = MetaProperty(id = PropertyID(keyID = StringID("bondRate"), typeID = constants.Data.NumberDataTypeID), data = NumberData(0))
  val BurnHeightProperty: MetaProperty = MetaProperty(id = PropertyID(keyID = StringID("burnHeight"), typeID = constants.Data.HeightDataTypeID), data = HeightData(Height(-1)))
  val CreationHeightProperty: MetaProperty = MetaProperty(id = PropertyID(keyID = StringID("creationHeight"), typeID = constants.Data.HeightDataTypeID), data = HeightData(Height(-1)))
  val ExpiryHeightProperty: MetaProperty = MetaProperty(id = PropertyID(keyID = StringID("expiryHeight"), typeID = constants.Data.HeightDataTypeID), data = HeightData(Height(-1)))
  val ExchangeRateProperty: MetaProperty = MetaProperty(id = PropertyID(keyID = StringID("exchangeRate"), typeID = constants.Data.DecDataTypeID), data = DecData(schema.constants.Data.ZeroDec))
  val IdentityIDProperty: MetaProperty = MetaProperty(id = PropertyID(keyID = StringID("identityID"), typeID = constants.Data.IDDataTypeID), data = IDData(IdentityID(HashID(Array[Byte]()))))
  val LockProperty: MetaProperty = MetaProperty(id = PropertyID(keyID = StringID("lock"), typeID = constants.Data.HeightDataTypeID), data = HeightData(Height(-1)))
  val MaintainedPropertiesProperty: MetaProperty = MetaProperty(id = PropertyID(keyID = StringID("maintainedProperties"), typeID = constants.Data.ListDataTypeID), data = ListData(Seq[Data]()))
  val MaintainedClassificationIDProperty: MetaProperty = MetaProperty(id = PropertyID(keyID = StringID("maintainedClassificationID"), typeID = constants.Data.IDDataTypeID), data = IDData(ClassificationID(HashID(Array[Byte]()))))
  val MakerIDProperty: MetaProperty = MetaProperty(id = PropertyID(keyID = StringID("makerID"), typeID = constants.Data.IDDataTypeID), data = IDData(IdentityID(HashID(Array[Byte]()))))
  val MakerOwnableIDProperty: MetaProperty = MetaProperty(id = PropertyID(keyID = StringID("makerOwnableID"), typeID = constants.Data.IDDataTypeID), data = IDData(AssetID(HashID(Array[Byte]()))))
  val MakerOwnableSplitProperty: MetaProperty = MetaProperty(id = PropertyID(keyID = StringID("makerOwnableSplit"), typeID = constants.Data.NumberDataTypeID), data = NumberData(1))
  val MaxOrderLifeProperty: MetaProperty = MetaProperty(id = PropertyID(keyID = StringID("maxOrderLife"), typeID = constants.Data.HeightDataTypeID), data = HeightData(Height(-1)))
  val MaxPropertyCountProperty: MetaProperty = MetaProperty(id = PropertyID(keyID = StringID("maxPropertyCount"), typeID = constants.Data.NumberDataTypeID), data = NumberData(0))
  val MaxProvisionAddressCountProperty: MetaProperty = MetaProperty(id = PropertyID(keyID = StringID("maxProvisionAddressCount"), typeID = constants.Data.NumberDataTypeID), data = NumberData(0))
  val NubProperty: MetaProperty = MetaProperty(id = PropertyID(keyID = StringID("nubID"), typeID = constants.Data.IDDataTypeID), data = IDData(StringID("")))
  val PermissionsProperty: MetaProperty = MetaProperty(id = PropertyID(keyID = StringID("permissions"), typeID = constants.Data.ListDataTypeID), data = ListData(Seq[Data]()))
  val SupplyProperty: MetaProperty = MetaProperty(id = PropertyID(keyID = StringID("supply"), typeID = constants.Data.NumberDataTypeID), data = NumberData(1))
  val TakerOwnableIDProperty: MetaProperty = MetaProperty(id = PropertyID(keyID = StringID("takerOwnableID"), typeID = constants.Data.IDDataTypeID), data = IDData(AssetID(HashID(Array[Byte]()))))
  val TakerIDProperty: MetaProperty = MetaProperty(id = PropertyID(keyID = StringID("takerID"), typeID = constants.Data.IDDataTypeID), data = IDData(IdentityID(HashID(Array[Byte]()))))
  val WrapAllowedCoinsProperty: MetaProperty = MetaProperty(id = PropertyID(keyID = StringID("wrapAllowedCoins"), typeID = constants.Data.ListDataTypeID), data = ListData(Seq[Data]()))

}
