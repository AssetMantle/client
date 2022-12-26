package models.Abstract

import constants.Blockchain.DataType
import exceptions.BaseException
import models.common.BaseData._
import play.api.Logger
import play.api.libs.functional.syntax.toAlternativeOps
import play.api.libs.json.{Json, Reads, Writes}

abstract class AnyDataImpl {
  def dataType: DataType

  def generateHash: String

  def asString: String

  def asDec: BigDecimal

  def asHeight: Int

  def asID: String

  def toFormattedString: String

}

object AnyDataImpl {

  private implicit val module: String = constants.Module.ANY_DATA_IMPL

  private implicit val logger: Logger = Logger(this.getClass)


  implicit val anyDataImplWrites: Writes[AnyDataImpl] = {
    case stringData: StringData => Json.toJson(stringData)
    case decData: DecData => Json.toJson(decData)
    case heightData: HeightData => Json.toJson(heightData)
    case idData: IDData => Json.toJson(idData)
    case _ => throw new BaseException(constants.Response.NO_SUC_ANY_DATA_IMPL_FOUND)
  }

  implicit val anyDataImplReads: Reads[AnyDataImpl] = {
    Json.format[StringData].map(x => x: StringData) or
      Json.format[DecData].map(x => x: DecData) or
      Json.format[HeightData].map(x => x: HeightData) or
      Json.format[IDData].map(x => x: IDData)
  }
}
