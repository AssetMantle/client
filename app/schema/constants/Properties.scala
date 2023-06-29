package schema.constants

import schema.data.base._
import schema.id.base._
import schema.list.PropertyList
import schema.property.base.MetaProperty
import schema.qualified.{Immutables, Mutables}
import schema.types.Height

object Properties {

  val Mint: StringID = StringID("mint")
  val Burn: StringID = StringID("burn")
  val Renumerate: StringID = StringID("renumerate")
  val Add: StringID = StringID("add")
  val Remove: StringID = StringID("remove")
  val Mutate: StringID = StringID("mutate")

  val AuthenticationProperty: MetaProperty = MetaProperty(keyID = StringID("authentication"), data = ListData(Seq()))
  val BondAmountProperty: MetaProperty = MetaProperty(keyID = StringID("bondAmount"), data = NumberData(0))
  val BondRateProperty: MetaProperty = MetaProperty(keyID = StringID("bondRate"), data = NumberData(0))
  val BurnHeightProperty: MetaProperty = MetaProperty(keyID = StringID("burnHeight"), data = HeightData(Height(-1)))
  val CreationHeightProperty: MetaProperty = MetaProperty(keyID = StringID("creationHeight"), data = HeightData(Height(-1)))
  val ExpiryHeightProperty: MetaProperty = MetaProperty(keyID = StringID("expiryHeight"), data = HeightData(Height(-1)))
  val ExchangeRateProperty: MetaProperty = MetaProperty(keyID = StringID("exchangeRate"), data = DecData(0))
  val IdentityIDProperty: MetaProperty = MetaProperty(keyID = StringID("identityID"), data = IDData(IdentityID(HashID(Array[Byte]()))))
  val LockProperty: MetaProperty = MetaProperty(keyID = StringID("lock"), data = HeightData(Height(-1)))
  val MaintainedPropertiesProperty: MetaProperty = MetaProperty(keyID = StringID("maintainedProperties"), data = ListData(Seq()))
  val MaintainedClassificationIDProperty: MetaProperty = MetaProperty(keyID = StringID("maintainedClassificationID"), data = IDData(ClassificationID(HashID(Array[Byte]()))))
  val MakerIDProperty: MetaProperty = MetaProperty(keyID = StringID("makerID"), data = IDData(IdentityID(HashID(Array[Byte]()))))
  val MakerOwnableIDProperty: MetaProperty = MetaProperty(keyID = StringID("makerOwnableID"), data = IDData(AssetID(HashID(Array[Byte]()))))
  val MakerOwnableSplitProperty: MetaProperty = MetaProperty(keyID = StringID("makerOwnableSplit"), data = NumberData(1))
  val MaxOrderLifeProperty: MetaProperty = MetaProperty(keyID = StringID("maxOrderLife"), data = HeightData(Height(-1)))
  val MaxPropertyCountProperty: MetaProperty = MetaProperty(keyID = StringID("maxPropertyCount"), data = NumberData(0))
  val MaxProvisionAddressCountProperty: MetaProperty = MetaProperty(keyID = StringID("maxProvisionAddressCount"), data = NumberData(0))
  val NubProperty: MetaProperty = MetaProperty(keyID = StringID("nubID"), data = IDData(StringID("")))
  val PermissionsProperty: MetaProperty = MetaProperty(keyID = StringID("permissions"), data = ListData(Seq()))
  val SupplyProperty: MetaProperty = MetaProperty(keyID = StringID("supply"), data = NumberData(1))
  val TakerOwnableIDProperty: MetaProperty = MetaProperty(keyID = StringID("takerOwnableID"), data = IDData(AssetID(HashID(Array[Byte]()))))
  val TakerIDProperty: MetaProperty = MetaProperty(keyID = StringID("takerID"), data = IDData(IdentityID(HashID(Array[Byte]()))))
  val WrapAllowedCoinsProperty: MetaProperty = MetaProperty(keyID = StringID("wrapAllowedCoins"), data = ListData(Seq()))

  val MaintainerClassificationImmutables: Immutables = Immutables(PropertyList(Seq(IdentityIDProperty, MaintainedClassificationIDProperty)))
  val MaintainerClassificationMutables: Mutables = Mutables(PropertyList(Seq(MaintainedPropertiesProperty, PermissionsProperty)))

  val NubImmutables: Immutables = Immutables(PropertyList(Seq(NubProperty)))
  val NubMutables: Mutables = Mutables(PropertyList(Seq(AuthenticationProperty)))
}
