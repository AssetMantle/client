package schema.id

import schema.id.base.StringID

object constants {

  val Separator = "."

  val AssetIDType: StringID = StringID("AI")
  val AnyIDType: StringID = StringID("I")
  val ClassificationIDType: StringID = StringID("CLI")
  val DataIDType: StringID = StringID("DI")
  val HashIDType: StringID = StringID("HI")
  val IdentityIDType: StringID = StringID("II")
  val MaintainerIDType: StringID = StringID("MI")
  val OrderIDType: StringID = StringID("OI")
  val PropertyIDType: StringID = StringID("PI")
  val SplitIDType: StringID = StringID("SPI")
  val StringIDType: StringID = StringID("SI")

  val AllIdTypes: Seq[StringID] = Seq(
    AssetIDType, AnyIDType,
    ClassificationIDType,
    DataIDType,
    HashIDType,
    IdentityIDType,
    MaintainerIDType,
    OrderIDType,
    PropertyIDType,
    SplitIDType, StringIDType
  )

}
