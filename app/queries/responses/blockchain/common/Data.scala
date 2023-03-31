package queries.responses.blockchain.common

import com.data
import play.api.libs.json.{Json, Reads}
import queries.responses.blockchain.common.ID._
import schema.data.{base => baseSchemaData}
import schema.types.{Height => baseHeight}
import utilities.AttoNumber

object Data {

  case class AccAddressData(value: Option[String]) {
    def toAccAddressData: baseSchemaData.AccAddressData = if (this.value.isDefined) {
      baseSchemaData.AccAddressData(utilities.Secrets.base64Decoder(this.value.get))
    } else baseSchemaData.AccAddressData(Array[Byte]())
  }

  implicit val AccAddressDataReads: Reads[AccAddressData] = Json.reads[AccAddressData]

  case class BooleanData(value: Boolean) {
    def toBooleanData: baseSchemaData.BooleanData = baseSchemaData.BooleanData(this.value)
  }

  implicit val BooleanDataReads: Reads[BooleanData] = Json.reads[BooleanData]

  case class DecData(value: String) {
    def toDecData: baseSchemaData.DecData = baseSchemaData.DecData(AttoNumber(BigDecimal(this.value)))
  }

  implicit val DecDataReads: Reads[DecData] = Json.reads[DecData]

  case class Height(value: String) {
    def toHeight: baseHeight = baseHeight(this.value.toLong)
  }

  implicit val HeightReads: Reads[Height] = Json.reads[Height]

  case class HeightData(value: Height) {
    def toHeightData: baseSchemaData.HeightData = baseSchemaData.HeightData(this.value.toHeight)
  }

  implicit val HeightDataReads: Reads[HeightData] = Json.reads[HeightData]

  case class IDData(value: AnyID) {
    def toIDData: baseSchemaData.IDData = baseSchemaData.IDData(this.value.toID.toAnyID)
  }

  implicit val IDDataReads: Reads[IDData] = Json.reads[IDData]

  case class StringData(value: String) {
    def toStringData: baseSchemaData.StringData = baseSchemaData.StringData(this.value)
  }

  implicit val StringDataReads: Reads[StringData] = Json.reads[StringData]

  case class NumberData(value: String) {
    def toNumberData: baseSchemaData.NumberData = baseSchemaData.NumberData(this.value.toLong)
  }

  implicit val NumberDataReads: Reads[NumberData] = Json.reads[NumberData]

  case class AnyDataWithoutListData(
                                     acc_address_data: Option[AccAddressData],
                                     boolean_data: Option[BooleanData],
                                     dec_data: Option[DecData],
                                     height_data: Option[HeightData],
                                     i_d_data: Option[IDData],
                                     string_data: Option[StringData],
                                     number_data: Option[NumberData]
                                   ) {

    def toAnyData: data.AnyData = {
      val schemaData: schema.data.Data = if (this.acc_address_data.isDefined) this.acc_address_data.get.toAccAddressData
      else if (this.boolean_data.isDefined) this.boolean_data.get.toBooleanData
      else if (this.dec_data.isDefined) this.dec_data.get.toDecData
      else if (this.height_data.isDefined) this.height_data.get.toHeightData
      else if (this.i_d_data.isDefined) this.i_d_data.get.toIDData
      else if (this.string_data.isDefined) this.string_data.get.toStringData
      else this.number_data.get.toNumberData
      schemaData.toAnyData
    }

  }

  implicit val AnyDataWithoutListDataReads: Reads[AnyDataWithoutListData] = Json.reads[AnyDataWithoutListData]

  case class ListData(data_list: Seq[AnyDataWithoutListData]) {
    def toListData: baseSchemaData.ListData = baseSchemaData.ListData(this.data_list.map(_.toAnyData))
  }

  implicit val ListDataReads: Reads[ListData] = Json.reads[ListData]

  case class AnyData(
                      acc_address_data: Option[AccAddressData],
                      boolean_data: Option[BooleanData],
                      dec_data: Option[DecData],
                      height_data: Option[HeightData],
                      i_d_data: Option[IDData],
                      string_data: Option[StringData],
                      number_data: Option[NumberData],
                      list_data: Option[ListData]
                    ) {
    def toData: schema.data.Data = {
      if (this.acc_address_data.isDefined) this.acc_address_data.get.toAccAddressData
      else if (this.boolean_data.isDefined) this.boolean_data.get.toBooleanData
      else if (this.dec_data.isDefined) this.dec_data.get.toDecData
      else if (this.height_data.isDefined) this.height_data.get.toHeightData
      else if (this.i_d_data.isDefined) this.i_d_data.get.toIDData
      else if (this.string_data.isDefined) this.string_data.get.toStringData
      else if (this.number_data.isDefined) this.number_data.get.toNumberData
      else this.list_data.get.toListData
    }
  }

  implicit val AnyDataReads: Reads[AnyData] = Json.reads[AnyData]
}
