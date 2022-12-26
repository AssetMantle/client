package models.common

import play.api.libs.json._
import utilities.MicroNumber

object IBC {

  case class ConnectionCounterparty(clientID: String, connectionID: String)

  implicit val connectionCounterpartyReads: Reads[ConnectionCounterparty] = Json.reads[ConnectionCounterparty]

  implicit val connectionCounterpartyWrites: OWrites[ConnectionCounterparty] = Json.writes[ConnectionCounterparty]

  case class ChannelCounterparty(portID: String, channelID: String)

  implicit val channelCounterpartyReads: Reads[ChannelCounterparty] = Json.reads[ChannelCounterparty]

  implicit val channelCounterpartyWrites: OWrites[ChannelCounterparty] = Json.writes[ChannelCounterparty]

  case class Version(identifier: String, features: Seq[String])

  implicit val versionReads: Reads[Version] = Json.reads[Version]

  implicit val versionWrites: OWrites[Version] = Json.writes[Version]

  case class ClientHeight(revisionNumber: Int, revisionHeight: Int)

  implicit val clientHeightReads: Reads[ClientHeight] = Json.reads[ClientHeight]

  implicit val clientHeightWrites: OWrites[ClientHeight] = Json.writes[ClientHeight]

  case class Channel(state: String, ordering: String, counterparty: ChannelCounterparty, connectionHops: Seq[String], version: String)

  implicit val channelReads: Reads[Channel] = Json.reads[Channel]

  implicit val channelWrites: OWrites[Channel] = Json.writes[Channel]

  case class FungibleTokenPacketData(denom: String, amount: MicroNumber, sender: String, receiver: String)

  implicit val fungibleTokenPacketDataReads: Reads[FungibleTokenPacketData] = Json.reads[FungibleTokenPacketData]

  implicit val fungibleTokenPacketDataWrites: OWrites[FungibleTokenPacketData] = Json.writes[FungibleTokenPacketData]

  case class Packet(sequence: String, sourcePort: String, sourceChannel: String, destinationPort: String, destinationChannel: String, data: FungibleTokenPacketData, timeoutHeight: ClientHeight, timeoutTimestamp: String)

  implicit val packetReads: Reads[Packet] = Json.reads[Packet]

  implicit val packetWrites: OWrites[Packet] = Json.writes[Packet]


}
