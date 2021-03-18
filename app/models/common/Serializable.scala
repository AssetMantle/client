package models.common

import exceptions.BaseException
import models.Abstract.{AssetDocumentContent, DataValue, NegotiationDocumentContent, TransactionMessage, TransactionValue}
import models.common.DataValue._
import models.common.TransactionMessages._
import play.api.Logger
import play.api.libs.json.{Json, OWrites, Reads, Writes}
import utilities.MicroNumber
import play.api.libs.functional.syntax._
import play.api.libs.json._
import utilities.MicroNumber
import java.sql.Date

import blockchain.common.Account.SinglePublicKey

object Serializable {

  private implicit val module: String = constants.Module.MODELS_COMMON_SERIALIZABLE

  private implicit val logger: Logger = Logger(this.getClass)

  case class Address(addressLine1: String, addressLine2: String, landmark: Option[String] = None, city: String, country: String, zipCode: String, phone: String)

  implicit val addressWrites: OWrites[Address] = Json.writes[Address]

  implicit val addressReads: Reads[Address] = Json.reads[Address]

  case class ShippingDetails(shippingPeriod: Int, portOfLoading: String, portOfDischarge: String)

  implicit val shippingDetailsReads: Reads[ShippingDetails] = Json.reads[ShippingDetails]

  implicit val shippingDetailsWrites: OWrites[ShippingDetails] = Json.writes[ShippingDetails]

  case class AssetOtherDetails(shippingDetails: ShippingDetails)

  implicit val assetOtherDetailsReads: Reads[AssetOtherDetails] = Json.reads[AssetOtherDetails]

  implicit val assetOtherDetailsWrites: OWrites[AssetOtherDetails] = Json.writes[AssetOtherDetails]

  case class Credit(tenure: Option[Int] = None, tentativeDate: Option[Date] = None, reference: Option[String])

  implicit val creditReads: Reads[Credit] = Json.reads[Credit]

  implicit val creditWrites: OWrites[Credit] = Json.writes[Credit]

  case class PaymentTerms(advancePercentage: Double = 0.0, credit: Option[Credit] = None)

  implicit val paymentTermsReads: Reads[PaymentTerms] = Json.reads[PaymentTerms]

  implicit val paymentTermsWrites: OWrites[PaymentTerms] = Json.writes[PaymentTerms]

  case class DocumentList(assetDocuments: Seq[String], negotiationDocuments: Seq[String])

  implicit val documentListReads: Reads[DocumentList] = Json.reads[DocumentList]

  implicit val documentListWrites: OWrites[DocumentList] = Json.writes[DocumentList]

  case class ValidatorDescription(moniker: Option[String], identity: Option[String], website: Option[String], securityContact: Option[String], details: Option[String])

  implicit val validatorDescriptionWrites: OWrites[ValidatorDescription] = Json.writes[ValidatorDescription]

  implicit val validatorDescriptionReads: Reads[ValidatorDescription] = Json.reads[ValidatorDescription]

  case class CommissionRates(rate: BigDecimal, maxRate: BigDecimal, maxChangeRate: BigDecimal)

  implicit val commissionRatesWrites: OWrites[CommissionRates] = Json.writes[CommissionRates]

  implicit val commissionRatesReads: Reads[CommissionRates] = Json.reads[CommissionRates]

  case class Commission(commissionRates: CommissionRates, updateTime: String)

  implicit val commissionWrites: OWrites[Commission] = Json.writes[Commission]

  implicit val commissionReads: Reads[Commission] = Json.reads[Commission]

  case class Coin(denom: String, amount: MicroNumber)

  def coinApply(denom: String, amount: String): Coin = Coin(denom = denom, amount = MicroNumber(BigInt(amount)))

  implicit val coinReads: Reads[Coin] = (
    (JsPath \ "denom").read[String] and
      (JsPath \ "amount").read[String]
    ) (coinApply _)

  implicit val coinWrites: Writes[Coin] = (coin: Coin) => Json.obj(
    "denom" -> coin.denom,
    "amount" -> coin.amount.toMicroString
  )

  case class Fee(amount: Seq[Coin], gas: String)

  implicit val feeReads: Reads[Fee] = Json.reads[Fee]

  implicit val feeWrites: OWrites[Fee] = Json.writes[Fee]

  case class NotificationTemplate(template: String, parameters: Seq[String])

  implicit val notificationTemplateReads: Reads[NotificationTemplate] = Json.reads[NotificationTemplate]

  implicit val notificationTemplateWrites: OWrites[NotificationTemplate] = Json.writes[NotificationTemplate]


  case class RedelegationEntry(creationHeight: Int, completionTime: String, initialBalance: MicroNumber, sharesDestination: BigDecimal)

  implicit val redelegationEntryReads: Reads[RedelegationEntry] = Json.reads[RedelegationEntry]

  implicit val redelegationEntryWrites: OWrites[RedelegationEntry] = Json.writes[RedelegationEntry]

  case class UndelegationEntry(creationHeight: Int, completionTime: String, initialBalance: MicroNumber, balance: MicroNumber)

  implicit val undelegationEntryReads: Reads[UndelegationEntry] = Json.reads[UndelegationEntry]

  implicit val undelegationEntryWrites: OWrites[UndelegationEntry] = Json.writes[UndelegationEntry]

  case class ID(idString: String)

  implicit val idReads: Reads[ID] = Json.reads[ID]

  implicit val idWrites: OWrites[ID] = Json.writes[ID]

  case class Data(dataType: String, value: DataValue)

  implicit val dataReads: Reads[Data] = (
    (JsPath \ "dataType").read[String] and
      (JsPath \ "value").read[JsObject]
    ) (dataValueApply _)

  implicit val dataWrites: OWrites[Data] = Json.writes[Data]

  case class Fact(factType: String, hash: String)

  def NewFact(factType: String, dataValue: DataValue): Fact = Fact(factType = factType, hash = dataValue.generateHash)

  implicit val factReads: Reads[Fact] = Json.reads[Fact]

  implicit val factWrites: OWrites[Fact] = Json.writes[Fact]

  case class Property(id: String, fact: Fact)

  implicit val propertyReads: Reads[Property] = Json.reads[Property]

  implicit val propertyWrites: OWrites[Property] = Json.writes[Property]

  case class Properties(propertyList: Seq[Property]) {
    def mutate(property: Property): Properties = Properties(propertyList.filterNot(_.id == property.id) :+ property)
  }

  implicit val propertiesReads: Reads[Properties] = Json.reads[Properties]

  implicit val propertiesWrites: OWrites[Properties] = Json.writes[Properties]

  case class MetaFact(data: Data) {
    def getHash: String = data.value.generateHash

    def removeData(): Fact = NewFact(DataValue.getFactTypeFromDataType(data.dataType), data.value)
  }

  implicit val metaFactReads: Reads[MetaFact] = Json.reads[MetaFact]

  implicit val metaFactWrites: OWrites[MetaFact] = Json.writes[MetaFact]

  case class MetaProperty(id: String, metaFact: MetaFact) {
    def removeData(): Property = Property(id = id, fact = metaFact.removeData())
  }

  implicit val metaPropertyReads: Reads[MetaProperty] = Json.reads[MetaProperty]

  implicit val metaPropertyWrites: OWrites[MetaProperty] = Json.writes[MetaProperty]

  case class MetaProperties(metaPropertyList: Seq[MetaProperty]) {
    def removeData(): Properties = Properties(metaPropertyList.map(_.removeData()))

    def mutate(metaProperty: MetaProperty): MetaProperties = MetaProperties(metaPropertyList.filterNot(_.id == metaProperty.id) :+ metaProperty)
  }

  implicit val metaPropertiesReads: Reads[MetaProperties] = Json.reads[MetaProperties]

  implicit val metaPropertiesWrites: OWrites[MetaProperties] = Json.writes[MetaProperties]

  case class Mutables(properties: Properties) {
    def mutate(mutatingProperties: Seq[Property]): Mutables = {
      val mutatingPropertiesID = mutatingProperties.map(_.id)
      Mutables(Properties(properties.propertyList.filterNot(x => mutatingPropertiesID.contains(x.id)) ++ mutatingProperties))
    }
  }

  implicit val mutablesReads: Reads[Mutables] = Json.reads[Mutables]

  implicit val mutablesWrites: OWrites[Mutables] = Json.writes[Mutables]

  case class Immutables(properties: Properties) {
    def getHashID: String = utilities.Hash.getHash(properties.propertyList.map(_.fact.hash): _*)
  }

  implicit val immutablesReads: Reads[Immutables] = Json.reads[Immutables]

  implicit val immutablesWrites: OWrites[Immutables] = Json.writes[Immutables]

  case class StdMsg(messageType: String, message: TransactionMessage)

  implicit val msgReads: Reads[StdMsg] = (
    (JsPath \ "messageType").read[String] and
      (JsPath \ "message").read[JsObject]
    ) (stdMsgApply _)

  implicit val msgWrites: OWrites[StdMsg] = Json.writes[StdMsg]

  case class BaseProperty(dataType: String, dataName: String, dataValue: Option[String]) {
    def toRequestString: String = utilities.String.getPropertyRequestWithValue(dataNameWithType = utilities.String.getPropertyRequestNameAndType(dataType = dataType, dataName = dataName), dataValue = dataValue)

    def toMetaProperty: MetaProperty = MetaProperty(id = dataName, metaFact = MetaFact(DataValue.getData(dataType = dataType, dataValue = dataValue)))

    def toProperty: Property = Property(id = dataName, fact = NewFact(factType = DataValue.getFactTypeFromDataType(dataType), dataValue = DataValue.getDataValue(dataType = dataType, dataValue = dataValue)))

    def withValue(value: String) = this.copy(dataValue = Some(value))
  }

  implicit val basePropertyReads: Reads[BaseProperty] = Json.reads[BaseProperty]

  implicit val basePropertyWrites: OWrites[BaseProperty] = Json.writes[BaseProperty]


  case class TradeActivityTemplate(template: String, parameters: Seq[String])

  implicit val tradeActivityTemplateReads: Reads[TradeActivityTemplate] = Json.reads[TradeActivityTemplate]

  implicit val tradeActivityTemplateWrites: OWrites[TradeActivityTemplate] = Json.writes[TradeActivityTemplate]

  case class BillOfLading(id: String, consigneeTo: String, vesselName: String, portOfLoading: String, portOfDischarge: String, shipperName: String, shipperAddress: String, notifyPartyName: String, notifyPartyAddress: String, dateOfShipping: Date, deliveryTerm: String, assetDescription: String, assetQuantity: MicroNumber, quantityUnit: String, declaredAssetValue: MicroNumber) extends AssetDocumentContent

  implicit val assetDocumentContentWrites: Writes[AssetDocumentContent] = {
    case billOfLading: BillOfLading => Json.toJson(billOfLading)(Json.writes[BillOfLading])
    case _ => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
  }

  implicit val assetDocumentContentReads: Reads[AssetDocumentContent] = {
    Json.format[BillOfLading].map(x => x: AssetDocumentContent)
  }

  case class Invoice(invoiceNumber: String, invoiceAmount: MicroNumber, invoiceDate: Date) extends NegotiationDocumentContent

  case class Contract(contractNumber: String) extends NegotiationDocumentContent

  implicit val negotiationDocumentContentWrites: Writes[NegotiationDocumentContent] = {
    case invoice: Invoice => Json.toJson(invoice)(Json.writes[Invoice])
    case contract: Contract => Json.toJson(contract)(Json.writes[Contract])
    case _ => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
  }

  implicit val negotiationDocumentContentReads: Reads[NegotiationDocumentContent] = {
    Json.format[Invoice].map(x => x: NegotiationDocumentContent) or
      Json.format[Contract].map(x => x: NegotiationDocumentContent)
  }


}
