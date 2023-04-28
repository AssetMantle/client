package schema.data.base

import com.assetmantle.schema.data.base.{AnyData, DecData => protoDecData}
import schema.data.Data
import schema.id.base.{DataID, HashID, StringID}

import scala.util.Try

case class DecData(value: String) extends Data {

  require(Try(BigDecimal(value, schema.constants.Data.DecPrecisionContext)).isSuccess, "INVALID_STRING_FOR_DEC_DATA")

  def toPlainString: String = schema.constants.Data.DecStringFormat.format(this.value)

  def getType: StringID = constants.Data.DecDataTypeID

  def getBondWeight: Int = constants.Data.DecDataWeight

  def getValue: BigDecimal = BigDecimal(value, schema.constants.Data.DecPrecisionContext)

  def getDataID: DataID = DataID(typeID = constants.Data.DecDataTypeID, hashID = this.generateHashID)

  def zeroValue: DecData = DecData(BigDecimal(0))

  def getBytes: Array[Byte] = this.toPlainString.getBytes

  def generateHashID: HashID = if (this.value == this.zeroValue.value) utilities.ID.generateHashID() else utilities.ID.generateHashID(this.getBytes)

  def asProtoDecData: protoDecData = protoDecData.newBuilder().setValue(this.toPlainString).build()

  def toAnyData: AnyData = AnyData.newBuilder().setDecData(this.asProtoDecData).build()

  def getProtoBytes: Array[Byte] = this.asProtoDecData.toByteString.toByteArray

  def viewString: String = this.value
}

object DecData {

  def apply(value: protoDecData): DecData = DecData(value.getValue)

  def apply(protoBytes: Array[Byte]): DecData = DecData(protoDecData.parseFrom(protoBytes))

  def apply(value: BigDecimal): DecData = DecData(schema.constants.Data.DecStringFormat.format(value))
}
