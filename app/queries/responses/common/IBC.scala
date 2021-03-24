package queries.responses.common

import models.common.Serializable
import play.api.libs.json.{Json, Reads}
import utilities.MicroNumber

object IBC {

  case class Counterparty(client_id: String, connection_id: String) {
    def toSerializableIBCCounterparty: Serializable.IBC.Counterparty = Serializable.IBC.Counterparty(connectionID = connection_id, clientID = client_id)
  }

  implicit val counterpartyReads: Reads[Counterparty] = Json.reads[Counterparty]

  case class Version(identifier: String, features: Seq[String]) {
    def toSerializableIBCVersion: Serializable.IBC.Version = Serializable.IBC.Version(identifier = identifier, features = features)
  }

  implicit val versionReads: Reads[Version] = Json.reads[Version]

  case class ClientHeight(revision_number: String, revision_height: String) {
    def toSerializableIBCClientHeight: Serializable.IBC.ClientHeight = Serializable.IBC.ClientHeight(revisionNumber = revision_number.toInt, revisionHeight = revision_height.toInt)
  }

  implicit val clientHeightReads: Reads[ClientHeight] = Json.reads[ClientHeight]

  case class Channel(state: String, ordering: String, counterparty: Counterparty, connection_hops: Seq[String], version: String) {
    def toSerializableIBCChannel: Serializable.IBC.Channel = Serializable.IBC.Channel(state = state, ordering = ordering, counterparty = counterparty.toSerializableIBCCounterparty, connectionHops = connection_hops, version = version)
  }

  implicit val channelReads: Reads[Channel] = Json.reads[Channel]

  case class FungibleTokenPacketData(denom: String, amount: String, sender: String, receiver: String) {
    def toSerializableIBCFungibleTokenPacketData: Serializable.IBC.FungibleTokenPacketData = Serializable.IBC.FungibleTokenPacketData(denom = denom, amount = MicroNumber(BigDecimal(amount).toBigInt), sender = sender, receiver = receiver)
  }

  implicit val fungibleTokenPacketDataReads: Reads[FungibleTokenPacketData] = Json.reads[FungibleTokenPacketData]

  case class Packet(sequence: String, source_port: String, source_channel: String, destination_port: String, destination_channel: String, data: FungibleTokenPacketData, timeout_height: ClientHeight, timeout_timestamp: String) {
    def toSerializableIBCPacket: Serializable.IBC.Packet = Serializable.IBC.Packet(sequence = sequence.toInt, sourcePort = source_port, sourceChannel = source_channel, destinationPort = destination_port, destinationChannel = destination_channel, data = data.toSerializableIBCFungibleTokenPacketData, timeoutHeight = timeout_height.toSerializableIBCClientHeight, timeoutTimestamp = timeout_timestamp.toInt)
  }

  implicit val packetReads: Reads[Packet] = Json.reads[Packet]

}
