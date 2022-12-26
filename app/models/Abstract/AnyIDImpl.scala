package models.Abstract

import exceptions.BaseException
import models.common.BaseID._
import play.api.Logger
import play.api.libs.functional.syntax.toAlternativeOps
import play.api.libs.json.{Json, Reads, Writes}

abstract class AnyIDImpl

object AnyIDImpl {

  private implicit val module: String = constants.Module.ANY_ID_IMPL

  private implicit val logger: Logger = Logger(this.getClass)

  implicit val anyIDImplWrites: Writes[AnyIDImpl] = {
    case hashID: HashID => Json.toJson(hashID)
    case stringID: StringID => Json.toJson(stringID)
    case dataID: DataID => Json.toJson(dataID)
    case classificationID: ClassificationID => Json.toJson(classificationID)
    case assetID: AssetID => Json.toJson(assetID)
    case identityID: IdentityID => Json.toJson(identityID)
    case maintainerID: MaintainerID => Json.toJson(maintainerID)
    case orderID: OrderID => Json.toJson(orderID)
    case ownableID: OwnableID => Json.toJson(ownableID)
    case propertyID: PropertyID => Json.toJson(propertyID)
    case splitID: SplitID => Json.toJson(splitID)
    case _ => throw new BaseException(constants.Response.NO_SUC_ANY_DATA_IMPL_FOUND)
  }

  implicit val anyIDImplReads: Reads[AnyIDImpl] = {
    Json.format[HashID].map(x => x: HashID) or
      Json.format[StringID].map(x => x: StringID) or
      Json.format[DataID].map(x => x: DataID) or
      Json.format[ClassificationID].map(x => x: ClassificationID) or
      Json.format[AssetID].map(x => x: AssetID) or
      Json.format[IdentityID].map(x => x: IdentityID) or
      Json.format[MaintainerID].map(x => x: MaintainerID) or
      Json.format[OrderID].map(x => x: OrderID) or
      Json.format[OwnableID].map(x => x: OwnableID) or
      Json.format[PropertyID].map(x => x: PropertyID) or
      Json.format[SplitID].map(x => x: SplitID)
  }
}
