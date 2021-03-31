package queries.responses.common

import models.common.Serializable
import play.api.Logger
import play.api.libs.json.{Json, Reads}
import utilities.MicroNumber

object IBC {

  case class ConnectionCounterparty(client_id: String, connection_id: String) {
    def toSerializableIBCConnectionCounterparty: Serializable.IBC.ConnectionCounterparty = Serializable.IBC.ConnectionCounterparty(connectionID = connection_id, clientID = client_id)
  }

  implicit val connectionCounterpartyReads: Reads[ConnectionCounterparty] = Json.reads[ConnectionCounterparty]

  case class ChannelCounterparty(port_id: String, channel_id: String) {
    def toSerializableIBCChannelCounterparty: Serializable.IBC.ChannelCounterparty = Serializable.IBC.ChannelCounterparty(portID = port_id, channelID = channel_id)
  }

  implicit val channelCounterpartyReads: Reads[ChannelCounterparty] = Json.reads[ChannelCounterparty]

  case class Version(identifier: String, features: Seq[String]) {
    def toSerializableIBCVersion: Serializable.IBC.Version = Serializable.IBC.Version(identifier = identifier, features = features)
  }

  implicit val versionReads: Reads[Version] = Json.reads[Version]

  case class ClientHeight(revision_number: String, revision_height: String) {
    def toSerializableIBCClientHeight: Serializable.IBC.ClientHeight = Serializable.IBC.ClientHeight(revisionNumber = revision_number.toInt, revisionHeight = revision_height.toInt)
  }

  implicit val clientHeightReads: Reads[ClientHeight] = Json.reads[ClientHeight]

  case class Channel(state: String, ordering: String, counterparty: ChannelCounterparty, connection_hops: Seq[String], version: String) {
    def toSerializableIBCChannel: Serializable.IBC.Channel = Serializable.IBC.Channel(state = state, ordering = ordering, counterparty = counterparty.toSerializableIBCChannelCounterparty, connectionHops = connection_hops, version = version)
  }

  implicit val channelReads: Reads[Channel] = Json.reads[Channel]

  case class FungibleTokenPacketData(denom: String, amount: String, sender: String, receiver: String) {
    def toSerializableIBCFungibleTokenPacketData: Serializable.IBC.FungibleTokenPacketData = Serializable.IBC.FungibleTokenPacketData(denom = denom, amount = MicroNumber(BigDecimal(amount).toBigInt), sender = sender, receiver = receiver)
  }

  implicit val fungibleTokenPacketDataReads: Reads[FungibleTokenPacketData] = Json.reads[FungibleTokenPacketData]

  case class Packet(sequence: String, source_port: String, source_channel: String, destination_port: String, destination_channel: String, data: String, timeout_height: ClientHeight, timeout_timestamp: String) {
    def toSerializableIBCPacket: Serializable.IBC.Packet = Serializable.IBC.Packet(sequence = sequence.toInt, sourcePort = source_port, sourceChannel = source_channel, destinationPort = destination_port, destinationChannel = destination_channel, data = utilities.JSON.convertJsonStringToObject[FungibleTokenPacketData](utilities.Hash.base64URLDecoder(data))(module = constants.Module.IBC_COMMON_RESPONSES, logger = Logger(constants.Module.IBC_COMMON_RESPONSES), reads = fungibleTokenPacketDataReads).toSerializableIBCFungibleTokenPacketData, timeoutHeight = timeout_height.toSerializableIBCClientHeight, timeoutTimestamp = timeout_timestamp.toInt)
  }

  implicit val packetReads: Reads[Packet] = Json.reads[Packet]

}