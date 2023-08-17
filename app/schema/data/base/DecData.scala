package schema.data.base

import com.assetmantle.schema.data.base.{AnyData, AnyListableData, DecData => protoDecData}
import schema.data._
import schema.id.base.{HashID, StringID}

case class DecData(value: BigDecimal) extends ListableData {

  def toPlainString: String = constants.DecStringFormat.format(this.value)

  def getType: StringID = constants.DecDataTypeID

  def getBondWeight: Int = constants.DecDataWeight

  def getValue: BigDecimal = this.value

  def compare(listableData: ListableData): Int = {
    val difference = this.value - listableData.asInstanceOf[DecData].value
    if (difference == 0) 0
    else if (difference > 0) 1
    else -1
  }

  def zeroValue: DecData = DecData(BigDecimal(0))

  def getBytes: Array[Byte] = this.getSortableDecBytes

  def generateHashID: HashID = if (this.value == this.zeroValue.value) schema.utilities.ID.generateHashID() else schema.utilities.ID.generateHashID(this.getBytes)

  def asProtoDecData: protoDecData = protoDecData.newBuilder().setValue(this.toPlainString).build()

  def toAnyData: AnyData = AnyData.newBuilder().setDecData(this.asProtoDecData).build()

  def toAnyListableData: AnyListableData = AnyListableData.newBuilder().setDecData(this.asProtoDecData).build()

  def getProtoBytes: Array[Byte] = this.asProtoDecData.toByteString.toByteArray

  def viewString: String = "Decimal: " + this.toPlainString

  def validSortable: Boolean = this.getValue.abs <= constants.DecDataMaxValue

  def getSortableDecBytes: Array[Byte] = {
    if (!this.validSortable) throw new IllegalArgumentException("INVALID_DEC_DATA_FOR_SORTED_BYTES")
    else {
      if (this.getValue == constants.DecDataMaxValue) "max".getBytes
      else if (this.getValue == (-1 * constants.DecDataMaxValue)) "--".getBytes
      else {
        val f = java.lang.String.format("%18s", this.getValue.abs.toString.split("\\.").head).replace(" ", "0")
        val l = java.lang.String.format("%-18s", this.getValue.abs.toString.split("\\.").last).replace(" ", "0")
        if (this.getValue < 0) "-".getBytes ++ (f + "." + l).getBytes
        else (f + "." + l).getBytes
      }
    }
  }

  def quotientTruncate(that: DecData): DecData = DecData(BigDecimal(((this.getValue * constants.DecFactor) / that.getValue).toBigInt) / constants.DecFactor)

  def multiplyTruncate(that: DecData): DecData = DecData(BigDecimal((this.getValue * constants.DecFactor * that.getValue).toBigInt) / constants.DecFactor)
}

object DecData {

  def apply(value: protoDecData): DecData = DecData(BigDecimal(value.getValue))

  def apply(protoBytes: Array[Byte]): DecData = DecData(protoDecData.parseFrom(protoBytes))

  def apply(value: String): DecData = DecData(BigDecimal(value))

}
