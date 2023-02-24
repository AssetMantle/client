// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: ibc/core/channel/v1/query.proto

package com.ibc.core.channel.v1;

public interface QueryUnreceivedAcksRequestOrBuilder extends
    // @@protoc_insertion_point(interface_extends:ibc.core.channel.v1.QueryUnreceivedAcksRequest)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   * port unique identifier
   * </pre>
   *
   * <code>string port_id = 1 [json_name = "portId"];</code>
   * @return The portId.
   */
  java.lang.String getPortId();
  /**
   * <pre>
   * port unique identifier
   * </pre>
   *
   * <code>string port_id = 1 [json_name = "portId"];</code>
   * @return The bytes for portId.
   */
  com.google.protobuf.ByteString
      getPortIdBytes();

  /**
   * <pre>
   * channel unique identifier
   * </pre>
   *
   * <code>string channel_id = 2 [json_name = "channelId"];</code>
   * @return The channelId.
   */
  java.lang.String getChannelId();
  /**
   * <pre>
   * channel unique identifier
   * </pre>
   *
   * <code>string channel_id = 2 [json_name = "channelId"];</code>
   * @return The bytes for channelId.
   */
  com.google.protobuf.ByteString
      getChannelIdBytes();

  /**
   * <pre>
   * list of acknowledgement sequences
   * </pre>
   *
   * <code>repeated uint64 packet_ack_sequences = 3 [json_name = "packetAckSequences"];</code>
   * @return A list containing the packetAckSequences.
   */
  java.util.List<java.lang.Long> getPacketAckSequencesList();
  /**
   * <pre>
   * list of acknowledgement sequences
   * </pre>
   *
   * <code>repeated uint64 packet_ack_sequences = 3 [json_name = "packetAckSequences"];</code>
   * @return The count of packetAckSequences.
   */
  int getPacketAckSequencesCount();
  /**
   * <pre>
   * list of acknowledgement sequences
   * </pre>
   *
   * <code>repeated uint64 packet_ack_sequences = 3 [json_name = "packetAckSequences"];</code>
   * @param index The index of the element to return.
   * @return The packetAckSequences at the given index.
   */
  long getPacketAckSequences(int index);
}