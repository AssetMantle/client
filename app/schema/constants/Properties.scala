package schema.constants

import schema.data.base._
import schema.id.base._
import schema.property.base.MetaProperty
import schema.types.Height

object Properties {

  val AuthenticationProperty: MetaProperty = MetaProperty(keyID = StringID("authentication"), data = ListData(Seq()))
  val BondAmountProperty: MetaProperty = MetaProperty(keyID = StringID("bondAmount"), data = NumberData(0))
  val BondRateProperty: MetaProperty = MetaProperty(keyID = StringID("bondRate"), data = NumberData(0))
  val BurnEnabledProperty: MetaProperty = MetaProperty(keyID = StringID("burnEnabled"), data = BooleanData(false))
  val BurnHeightProperty: MetaProperty = MetaProperty(keyID = StringID("burnHeight"), data = HeightData(Height(-1)))
  val CreationHeightProperty: MetaProperty = MetaProperty(keyID = StringID("creationHeight"), data = HeightData(Height(-1)))
  val DenomProperty: MetaProperty = MetaProperty(keyID = StringID("denom"), data = StringData(""))
  val DefineEnabledProperty: MetaProperty = MetaProperty(keyID = StringID("defineEnabled"), data = BooleanData(false))
  val DeputizeAllowedProperty: MetaProperty = MetaProperty(keyID = StringID("deputizeAllowed"), data = BooleanData(false))
  val ExchangeRateProperty: MetaProperty = MetaProperty(keyID = StringID("exchangeRate"), data = DecData(0))
  val ExpiryHeightProperty: MetaProperty = MetaProperty(keyID = StringID("expiryHeight"), data = HeightData(Height(-1)))
  val ExecutionHeightProperty: MetaProperty = MetaProperty(keyID = StringID("executionHeight"), data = HeightData(Height(-1)))
  val IdentityIDProperty: MetaProperty = MetaProperty(keyID = StringID("identityID"), data = IDData(IdentityID(HashID(Array[Byte]()))))
  val IssueEnabledProperty: MetaProperty = MetaProperty(keyID = StringID("issueEnabled"), data = BooleanData(false))
  val LockHeightProperty: MetaProperty = MetaProperty(keyID = StringID("lockHeight"), data = HeightData(Height(-1)))
  val MaintainedPropertiesProperty: MetaProperty = MetaProperty(keyID = StringID("maintainedProperties"), data = ListData(Seq()))
  val MaintainedClassificationIDProperty: MetaProperty = MetaProperty(keyID = StringID("maintainedClassificationID"), data = IDData(ClassificationID(HashID(Array[Byte]()))))
  val MakerIDProperty: MetaProperty = MetaProperty(keyID = StringID("makerID"), data = IDData(IdentityID(HashID(Array[Byte]()))))
  val MakerAssetIDProperty: MetaProperty = MetaProperty(keyID = StringID("makerAssetID"), data = IDData(AssetID(HashID(Array[Byte]()))))
  val MakerSplitProperty: MetaProperty = MetaProperty(keyID = StringID("makerSplit"), data = NumberData(0))
  val MaxOrderLifeProperty: MetaProperty = MetaProperty(keyID = StringID("maxOrderLife"), data = HeightData(Height(-1)))
  val MaxPropertyCountProperty: MetaProperty = MetaProperty(keyID = StringID("maxPropertyCount"), data = NumberData(0))
  val MaxProvisionAddressCountProperty: MetaProperty = MetaProperty(keyID = StringID("maxProvisionAddressCount"), data = NumberData(0))
  val MintEnabledProperty: MetaProperty = MetaProperty(keyID = StringID("mintEnabled"), data = BooleanData(false))
  val ModuleNameProperty: MetaProperty = MetaProperty(keyID = StringID("moduleName"), data = IDData(StringID("")))
  val NameProperty: MetaProperty = MetaProperty(keyID = StringID("name"), data = IDData(StringID("")))
  val PermissionsProperty: MetaProperty = MetaProperty(keyID = StringID("permissions"), data = ListData(Seq()))
  val PutEnabledProperty: MetaProperty = MetaProperty(keyID = StringID("putEnabled"), data = BooleanData(false))
  val QuashEnabledProperty: MetaProperty = MetaProperty(keyID = StringID("quashEnabled"), data = BooleanData(false))
  val RenumerateEnabledProperty: MetaProperty = MetaProperty(keyID = StringID("renumerateEnabled"), data = BooleanData(false))
  val RevealEnabledProperty: MetaProperty = MetaProperty(keyID = StringID("revealEnabled"), data = BooleanData(false))
  val SupplyProperty: MetaProperty = MetaProperty(keyID = StringID("supply"), data = NumberData(1))
  val TakerAssetIDProperty: MetaProperty = MetaProperty(keyID = StringID("takerAssetID"), data = IDData(AssetID(HashID(Array[Byte]()))))
  val TakerSplitProperty: MetaProperty = MetaProperty(keyID = StringID("takerSplit"), data = NumberData(0))
  val TakerIDProperty: MetaProperty = MetaProperty(keyID = StringID("takerID"), data = IDData(IdentityID(HashID(Array[Byte]()))))
  val TransferEnabledProperty: MetaProperty = MetaProperty(keyID = StringID("transferEnabled"), data = BooleanData(false))
  val WrapAllowedCoinsProperty: MetaProperty = MetaProperty(keyID = StringID("wrapAllowedCoins"), data = ListData(Seq()))
  val UnwrapAllowedCoinsProperty: MetaProperty = MetaProperty(keyID = StringID("unwrapAllowedCoins"), data = ListData(Seq()))
}
