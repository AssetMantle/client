package models.common

import com.cosmos.base.v1beta1.{Coin => protoCoin}
import com.google.protobuf.{Any => protoAny}
import exceptions.BaseException
import play.api.Logger
import play.api.libs.functional.syntax._
import play.api.libs.json._
import utilities.Date.RFC3339
import utilities.MicroNumber

object Serializable {

  private implicit val module: String = constants.Module.SERIALIZABLE

  private implicit val logger: Logger = Logger(this.getClass)

  case class SocialProfile(platform: String, username: String, url: String)

  implicit val socialProfileWrites: OWrites[SocialProfile] = Json.writes[SocialProfile]

  implicit val socialProfileReads: Reads[SocialProfile] = Json.reads[SocialProfile]

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

    case class Commission(commissionRates: CommissionRates, updateTime: RFC3339)

    implicit val commissionWrites: OWrites[Commission] = Json.writes[Commission]

    implicit val commissionReads: Reads[Commission] = Json.reads[Commission]
  }

  case class Coin(denom: String, amount: MicroNumber) {

    def isIBCDenom: Boolean = denom.startsWith("ibc/") && denom.length == 68

    def ibcDenomName: String = constants.Blockchain.IBCDenoms.find(_.hash == denom).fold(denom)(_.name)

    def normalizeDenom: String = if (denom.startsWith("u")) denom.substring(1).toUpperCase()
    else if (isIBCDenom && ibcDenomName.startsWith("u")) ibcDenomName.substring(1).toUpperCase()
    else denom.toUpperCase()

    def getAmountWithNormalizedDenom(formatted: Boolean = true): String = if (formatted) s"${utilities.NumericOperation.formatNumber(amount)} $normalizeDenom" else s"${amount.toString} $normalizeDenom"

    def getMicroAmountWithDenom: String = s"${utilities.NumericOperation.formatNumber(number = amount, normalize = false)} $denom"

    def isNegative: Boolean = amount < 0

    def isZero: Boolean = amount == MicroNumber.zero

    def add(coin: Coin): Coin = {
      if (coin.denom != denom) {
        throw new BaseException(constants.Response.ARITHMETIC_OPERATION_ON_DIFFERENT_COIN)
      }
      Coin(denom = denom, amount = amount + coin.amount)
    }

    def subtract(coin: Coin): Coin = {
      val result = add(coin.copy(amount = amount * -1))
      if (result.isNegative) {
        throw new BaseException(constants.Response.COIN_AMOUNT_NEGATIVE)
      }
      result
    }

    def toProtoCoin: protoCoin = protoCoin.newBuilder().setDenom(this.denom).setAmount(this.amount.toMicroString).build()
  }

  object Coin {
    def apply(coinProto: protoCoin): Coin = Coin(denom = coinProto.getDenom, amount = MicroNumber(BigInt(coinProto.getAmount)))

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

  case class RedelegationEntry(creationHeight: Int, completionTime: RFC3339, initialBalance: MicroNumber, sharesDestination: BigDecimal) {
    def isMature(currentTime: RFC3339): Boolean = !this.completionTime.isAfter(currentTime)
  }

  implicit val redelegationEntryReads: Reads[RedelegationEntry] = Json.reads[RedelegationEntry]

  implicit val redelegationEntryWrites: OWrites[RedelegationEntry] = Json.writes[RedelegationEntry]

  case class UndelegationEntry(creationHeight: Int, completionTime: RFC3339, initialBalance: MicroNumber, balance: MicroNumber) {
    def isMature(currentTime: RFC3339): Boolean = !this.completionTime.isAfter(currentTime)
  }

  implicit val undelegationEntryReads: Reads[UndelegationEntry] = Json.reads[UndelegationEntry]

  implicit val undelegationEntryWrites: OWrites[UndelegationEntry] = Json.writes[UndelegationEntry]

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

    case class ConnectionCounterparty(clientID: String, connectionID: String)

    implicit val connectionCounterpartyReads: Reads[ConnectionCounterparty] = Json.reads[ConnectionCounterparty]

    case class ChannelCounterparty(portID: String, channelID: String)

    implicit val channelCounterpartyReads: Reads[ChannelCounterparty] = Json.reads[ChannelCounterparty]

    case class Version(identifier: String, features: Seq[String])

    implicit val versionReads: Reads[Version] = Json.reads[Version]

    case class ClientHeight(revisionNumber: Int, revisionHeight: Int)

    implicit val clientHeightReads: Reads[ClientHeight] = Json.reads[ClientHeight]

    case class Channel(state: String, ordering: String, counterparty: ChannelCounterparty, connectionHops: Seq[String], version: String)

    implicit val channelReads: Reads[Channel] = Json.reads[Channel]

    abstract class PacketData {

      def getType: String

      def getAffectedWalletAddresses: Seq[String]
    }

    case class FungibleTokenPacketData(denom: String, amount: MicroNumber, sender: String, receiver: String) extends PacketData {
      def getType: String = constants.Blockchain.IBC.PacketDataType.FUNGIBLE_TOKEN_PACKET_DATA

      def getAffectedWalletAddresses: Seq[String] = Seq(sender, receiver)
    }

    implicit val fungibleTokenPacketDataReads: Reads[FungibleTokenPacketData] = Json.reads[FungibleTokenPacketData]

    case class InterChainAccountPacketData(`type`: String, data: Array[Byte], memo: String) extends PacketData {

      // TODO Implement
      def getTxMessages: Seq[protoAny] = Seq()

      def getAffectedWalletAddresses: Seq[String] = Seq()

      def getType: String = constants.Blockchain.IBC.PacketDataType.INTER_CHAIN_ACCOUNT_PACKET_DATA
    }

    implicit val interChainAccountPacketDataReads: Reads[InterChainAccountPacketData] = Json.reads[InterChainAccountPacketData]

    implicit val packetDataReads: Reads[PacketData] = {
      Json.format[FungibleTokenPacketData].map(x => x: FungibleTokenPacketData) or
        Json.format[InterChainAccountPacketData].map(x => x: InterChainAccountPacketData)
    }

    case class Packet(sequence: String, sourcePort: String, sourceChannel: String, destinationPort: String, destinationChannel: String, data: PacketData, timeoutHeight: ClientHeight, timeoutTimestamp: String)

    implicit val packetReads: Reads[Packet] = Json.reads[Packet]

  }

}
