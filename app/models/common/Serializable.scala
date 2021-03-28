package models.common

import models.Abstract.{DataValue, ProposalContent, TransactionMessage}
import models.common.DataValue._
import models.common.TransactionMessages._
import play.api.Logger
import play.api.libs.functional.syntax._
import play.api.libs.json._
import utilities.MicroNumber

object Serializable {

  private implicit val module: String = constants.Module.SERIALIZABLE

  private implicit val logger: Logger = Logger(this.getClass)

  case class Address(addressLine1: String, addressLine2: String, landmark: Option[String] = None, city: String, country: String, zipCode: String, phone: String)

  implicit val addressWrites: OWrites[Address] = Json.writes[Address]

  implicit val addressReads: Reads[Address] = Json.reads[Address]

  object Validator {

    case class Description(moniker: String, identity: String, website: String, securityContact: String, details: String)

    implicit val descriptionWrites: OWrites[Description] = Json.writes[Description]

    implicit val descriptionReads: Reads[Description] = Json.reads[Description]

    case class CommissionRates(rate: BigDecimal, maxRate: BigDecimal, maxChangeRate: BigDecimal)

    implicit val commissionRatesWrites: OWrites[CommissionRates] = Json.writes[CommissionRates]

    implicit val commissionRatesReads: Reads[CommissionRates] = Json.reads[CommissionRates]

    case class Commission(commissionRates: CommissionRates, updateTime: String)

    implicit val commissionWrites: OWrites[Commission] = Json.writes[Commission]

    implicit val commissionReads: Reads[Commission] = Json.reads[Commission]
  }

  case class Coin(denom: String, amount: MicroNumber) {
    def normalizeDenom: String = if (denom(0) == 'u') denom.split("u")(1).toUpperCase() else denom.toUpperCase()

    def getAmountWithNormalizedDenom(formatted: Boolean = true): String = if (formatted) s"${utilities.NumericOperation.formatNumber(amount)} ${normalizeDenom}" else s"${amount.toString} ${normalizeDenom}"

    def getMicroAmountWithDenom: String = s"${utilities.NumericOperation.formatNumber(number = amount, normalize = false)} ${denom}"

  }

  def coinApply(denom: String, amount: String): Coin = Coin(denom = denom, amount = MicroNumber(BigInt(amount)))

  implicit val coinReads: Reads[Coin] = (
    (JsPath \ "denom").read[String] and
      (JsPath \ "amount").read[String]
    ) (coinApply _)

  implicit val coinWrites: Writes[Coin] = (coin: Coin) => Json.obj(
    "denom" -> coin.denom,
    "amount" -> coin.amount.toMicroString
  )

  case class Fee(amount: Seq[Coin], gasLimit: String, payer: String, granter: String)

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

  case class StdMsg(messageType: String, message: TransactionMessage) {
    def getSigners: Seq[String] = message.getSigners
  }

  implicit val msgReads: Reads[StdMsg] = (
    (JsPath \ "messageType").read[String] and
      (JsPath \ "message").read[JsObject]
    ) (stdMsgApply _)

  implicit val msgWrites: OWrites[StdMsg] = Json.writes[StdMsg]

  case class BaseProperty(dataType: String, dataName: String, dataValue: Option[String]) {
    def toRequestString: String = utilities.String.getPropertyRequestWithValue(dataNameWithType = utilities.String.getPropertyRequestNameAndType(dataType = dataType, dataName = dataName), dataValue = dataValue)

    def toMetaProperty: MetaProperty = MetaProperty(id = dataName, metaFact = MetaFact(DataValue.getData(dataType = dataType, dataValue = dataValue)))

    def toProperty: Property = Property(id = dataName, fact = NewFact(factType = DataValue.getFactTypeFromDataType(dataType), dataValue = DataValue.getDataValue(dataType = dataType, dataValue = dataValue)))

  }

  implicit val basePropertyReads: Reads[BaseProperty] = Json.reads[BaseProperty]

  implicit val basePropertyWrites: OWrites[BaseProperty] = Json.writes[BaseProperty]

  case class FinalTallyResult(yes: BigDecimal, abstain: BigDecimal, no: BigDecimal, noWithVeto: BigDecimal)

  implicit val finalTallyResultReads: Reads[FinalTallyResult] = Json.reads[FinalTallyResult]

  implicit val finalTallyResultWrites: OWrites[FinalTallyResult] = Json.writes[FinalTallyResult]

  object Vesting {

    case class VestingPeriod(length: String, amount: Seq[Coin])

    implicit val vestingPeriodReads: Reads[VestingPeriod] = Json.reads[VestingPeriod]

    implicit val vestingPeriodWrites: OWrites[VestingPeriod] = Json.writes[VestingPeriod]

    case class VestingParameters(originalVesting: Seq[Coin], delegatedFree: Seq[Coin], delegatedVesting: Seq[Coin], endTime: String, startTime: Option[String], vestingPeriods: Seq[VestingPeriod])

    implicit val vestingParametersReads: Reads[VestingParameters] = Json.reads[VestingParameters]

    implicit val vestingParametersWrites: OWrites[VestingParameters] = Json.writes[VestingParameters]

  }

  object IBC {

    case class Counterparty(clientID: String, connectionID: String)

    implicit val counterpartyReads: Reads[Counterparty] = Json.reads[Counterparty]

    implicit val counterpartyWrites: OWrites[Counterparty] = Json.writes[Counterparty]

    case class Version(identifier: String, features: Seq[String])

    implicit val versionReads: Reads[Version] = Json.reads[Version]

    implicit val versionWrites: OWrites[Version] = Json.writes[Version]

    case class ClientHeight(revisionNumber: Int, revisionHeight: Int)

    implicit val clientHeightReads: Reads[ClientHeight] = Json.reads[ClientHeight]

    implicit val clientHeightWrites: OWrites[ClientHeight] = Json.writes[ClientHeight]

    case class Channel(state: String, ordering: String, counterparty: Counterparty, connectionHops: Seq[String], version: String)

    implicit val channelReads: Reads[Channel] = Json.reads[Channel]

    implicit val channelWrites: OWrites[Channel] = Json.writes[Channel]

    case class FungibleTokenPacketData(denom: String, amount: MicroNumber, sender: String, receiver: String)

    implicit val fungibleTokenPacketDataReads: Reads[FungibleTokenPacketData] = Json.reads[FungibleTokenPacketData]

    implicit val fungibleTokenPacketDataWrites: OWrites[FungibleTokenPacketData] = Json.writes[FungibleTokenPacketData]

    case class Packet(sequence: Int, sourcePort: String, sourceChannel: String, destinationPort: String, destinationChannel: String, data: FungibleTokenPacketData, timeoutHeight: ClientHeight, timeoutTimestamp: Int)

    implicit val packetReads: Reads[Packet] = Json.reads[Packet]

    implicit val packetWrites: OWrites[Packet] = Json.writes[Packet]

  }

}
