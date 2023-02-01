// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: ibc/core/channel/v1/query.proto

package com.ibc.core.channel.v1;

public interface QueryChannelConsensusStateRequestOrBuilder extends
    // @@protoc_insertion_point(interface_extends:ibc.core.channel.v1.QueryChannelConsensusStateRequest)
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
   * revision number of the consensus state
   * </pre>
   *
   * <code>uint64 revision_number = 3 [json_name = "revisionNumber"];</code>
   * @return The revisionNumber.
   */
  long getRevisionNumber();

  /**
   * <pre>
   * revision height of the consensus state
   * </pre>
   *
   * <code>uint64 revision_height = 4 [json_name = "revisionHeight"];</code>
   * @return The revisionHeight.
   */
  long getRevisionHeight();
}
