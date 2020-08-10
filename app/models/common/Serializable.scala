package models.common

import models.`abstract`.TransactionMessage
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

  implicit val coinReads: Reads[Coin] = Json.reads[Coin]

  implicit val coinWrites: OWrites[Coin] = Json.writes[Coin]

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

  case class Fact(hash: String, meta: Boolean)

  def newFact(fact: String): Fact = Fact(hash = utilities.Hash.getHash(fact), meta = false)

  implicit val factReads: Reads[Fact] = Json.reads[Fact]

  implicit val factWrites: OWrites[Fact] = Json.writes[Fact]

  case class Property(id: String, fact: Fact)

  implicit val propertyReads: Reads[Property] = Json.reads[Property]

  implicit val propertyWrites: OWrites[Property] = Json.writes[Property]

  case class Properties(propertyList: Seq[Property])

  implicit val propertiesReads: Reads[Properties] = Json.reads[Properties]

  implicit val propertiesWrites: OWrites[Properties] = Json.writes[Properties]

  case class Mutables(properties: Properties, maintainersID: String)

  implicit val mutablesReads: Reads[Mutables] = Json.reads[Mutables]

  implicit val mutablesWrites: OWrites[Mutables] = Json.writes[Mutables]

  case class Immutables(properties: Properties) {
    //apache-codec Base64 encoder gives Sw4O7BtF/0y7TclwCDzQArfGe7I= instead of Sw4O7BtF_0y7TclwCDzQArfGe7I= So use java.util.Base64
    def getHashID: String = utilities.Hash.getHash(properties.propertyList.map(_.fact.hash): _*)
  }

  implicit val immutablesReads: Reads[Immutables] = Json.reads[Immutables]

  implicit val immutablesWrites: OWrites[Immutables] = Json.writes[Immutables]

  case class Trait(id: String, property: Property, mutable: Boolean)

  implicit val traitReads: Reads[Trait] = Json.reads[Trait]

  implicit val traitWrites: OWrites[Trait] = Json.writes[Trait]

  case class StdMsg(messageType: String, message: TransactionMessage)

  implicit val msgReads: Reads[StdMsg] = (
    (JsPath \ "messageType").read[String] and
      (JsPath \ "message").read[JsObject]
    ) (stdMsgApply _)

  implicit val msgWrites: OWrites[StdMsg] = Json.writes[StdMsg]

}
