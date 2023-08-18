package schema.constants

import schema.id.base.StringID

object Permissions {

  // Assets
  val CanMintAssetPermission: StringID = StringID("mint")
  val CanBurnAssetPermission: StringID = StringID("burn")
  val CanRenumerateAssetPermission: StringID = StringID("renumerate")

  // Identities
  val CanIssueIdentityPermission: StringID = StringID("issue")
  val CanQuashIdentityPermission: StringID = StringID("quash")

  // Maintainers
  val CanAddMaintainerPermission: StringID = StringID("add")
  val CanMutateMaintainerPermission: StringID = StringID("mutate")
  val CanRemoveMaintainerPermission: StringID = StringID("remove")

  // Orders
  val CanMakeOrderPermission: StringID = StringID("make")
  val CanCancelOrderPermission: StringID = StringID("cancel")

}
