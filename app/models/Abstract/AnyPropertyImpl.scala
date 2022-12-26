package models.Abstract

import exceptions.BaseException
import models.common.BaseProperty._
import play.api.Logger
import play.api.libs.functional.syntax.toAlternativeOps
import play.api.libs.json.{Json, Reads, Writes}

abstract class AnyPropertyImpl {

}

object AnyPropertyImpl {

  private implicit val module: String = constants.Module.ANY_PROPERTY_IMPL

  private implicit val logger: Logger = Logger(this.getClass)


  implicit val anyPropertyImplWrites: Writes[AnyPropertyImpl] = {
    case metaProperty: MetaProperty => Json.toJson(metaProperty)
    case mesaProperty: MesaProperty => Json.toJson(mesaProperty)
    case _ => throw new BaseException(constants.Response.NO_SUC_ANY_PROPERTY_IMPL_FOUND)
  }

  implicit val anyPropertyReads: Reads[AnyPropertyImpl] = {
    Json.format[MetaProperty].map(x => x: MetaProperty) or
      Json.format[MesaProperty].map(x => x: MesaProperty)
  }
}
