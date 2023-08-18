package schema.utilities

import schema.constants.Permissions._
import schema.id.base.StringID
import schema.list.IDList

object Permissions {

  def getAssetsPermissions(canMint: Boolean, canBurn: Boolean, canRenumerate: Boolean): Seq[StringID] = {
    var permissions = Seq[StringID]()
    if (canMint) permissions = permissions :+ CanMintAssetPermission
    if (canBurn) permissions = permissions :+ CanBurnAssetPermission
    if (canRenumerate) permissions = permissions :+ CanRenumerateAssetPermission
    IDList(permissions).sort.idList.map(_.asInstanceOf[StringID])
  }

  def getIdentitiesPermissions(canIssue: Boolean, canQuash: Boolean): Seq[StringID] = {
    var permissions = Seq[StringID]()
    if (canIssue) permissions = permissions :+ CanIssueIdentityPermission
    if (canQuash) permissions = permissions :+ CanQuashIdentityPermission
    IDList(permissions).sort.idList.map(_.asInstanceOf[StringID])
  }

  def getMaintainersPermissions(canAdd: Boolean, canMutate: Boolean, canRemove: Boolean): Seq[StringID] = {
    var permissions = Seq[StringID]()
    if (canAdd) permissions = permissions :+ CanAddMaintainerPermission
    if (canMutate) permissions = permissions :+ CanMutateMaintainerPermission
    if (canRemove) permissions = permissions :+ CanRemoveMaintainerPermission
    IDList(permissions).sort.idList.map(_.asInstanceOf[StringID])
  }

  def getOrdersPermissions(canMake: Boolean, canCancel: Boolean): Seq[StringID] = {
    var permissions = Seq[StringID]()
    if (canMake) permissions = permissions :+ CanMakeOrderPermission
    if (canCancel) permissions = permissions :+ CanCancelOrderPermission
    IDList(permissions).sort.idList.map(_.asInstanceOf[StringID])
  }
}
