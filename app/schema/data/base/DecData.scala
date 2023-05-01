package schema.data.base

import com.assetmantle.schema.data.base.{AnyData, DecData => protoDecData}
import schema.data.Data
import schema.id.base.{DataID, HashID, StringID}

import scala.util.Try

case class DecData(value: String) extends Data {

  require(Try(BigDecimal(value, schema.constants.Data.DecPrecisionContext)).isSuccess, "INVALID_STRING_FOR_DEC_DATA")

  def toPlainString: String = schema.constants.Data.DecStringFormat.format(this.value)

  def getType: StringID = schema.constants.Data.DecDataTypeID

  def getBondWeight: Int = schema.constants.Data.DecDataWeight

  def getValue: BigDecimal = BigDecimal(value, schema.constants.Data.DecPrecisionContext)

  def getDataID: DataID = DataID(typeID = schema.constants.Data.DecDataTypeID, hashID = this.generateHashID)

  def zeroValue: DecData = DecData(BigDecimal(0))

  def getBytes: Array[Byte] = this.toPlainString.getBytes

  def generateHashID: HashID = if (this.value == this.zeroValue.value) schema.utilities.ID.generateHashID() else schema.utilities.ID.generateHashID(this.getBytes)

  def asProtoDecData: protoDecData = protoDecData.newBuilder().setValue(this.toPlainString).build()

  def toAnyData: AnyData = AnyData.newBuilder().setDecData(this.asProtoDecData).build()

  def getProtoBytes: Array[Byte] = this.asProtoDecData.toByteString.toByteArray

  def viewString: String = this.value

  def validSortable: Boolean = this.getValue.abs <= schema.constants.Data.DecDataMaxValue

  def getSortableDecBytes: Array[Byte] = {
    if (!this.validSortable) throw new IllegalArgumentException("UNSORTABLE_ATTONUMBER")
    else {
      if (this.getValue == schema.constants.Data.DecDataMaxValue) "max".getBytes
      else if (this.getValue == (-1 * schema.constants.Data.DecDataMaxValue)) "--".getBytes
      else {
        val f = java.lang.String.format("%18s", this.getValue.abs.toString.split("\\.").head).replace(" ", "0")
        val l = java.lang.String.format("%-18s", this.getValue.abs.toString.split("\\.").last).replace(" ", "0")
        if (this.getValue < 0) "-".getBytes ++ (f + "." + l).getBytes
        else (f + "." + l).getBytes
      }
    }
  }

  def quotientTruncate(that: DecData): DecData = DecData(BigDecimal((this.getValue * schema.constants.Data.DecFactor).toBigInt / (that.getValue * schema.constants.Data.DecFactor).toBigInt) / schema.constants.Data.DecFactor)

  def multiplyTruncate(that: DecData): DecData = DecData(BigDecimal((this.getValue * schema.constants.Data.DecFactor).toBigInt * (that.getValue * schema.constants.Data.DecFactor).toBigInt) / schema.constants.Data.DecFactor)
}

object DecData {

  def apply(value: protoDecData): DecData = DecData(value.getValue)

  def apply(protoBytes: Array[Byte]): DecData = DecData(protoDecData.parseFrom(protoBytes))

  def apply(value: BigDecimal): DecData = DecData(schema.constants.Data.DecStringFormat.format(value))
}
