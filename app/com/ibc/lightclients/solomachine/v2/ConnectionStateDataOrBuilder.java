// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: ibc/lightclients/solomachine/v2/solomachine.proto

package com.ibc.lightclients.solomachine.v2;

public interface ConnectionStateDataOrBuilder extends
    // @@protoc_insertion_point(interface_extends:ibc.lightclients.solomachine.v2.ConnectionStateData)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>bytes path = 1 [json_name = "path"];</code>
   * @return The path.
   */
  com.google.protobuf.ByteString getPath();

  /**
   * <code>.ibc.core.connection.v1.ConnectionEnd connection = 2 [json_name = "connection"];</code>
   * @return Whether the connection field is set.
   */
  boolean hasConnection();
  /**
   * <code>.ibc.core.connection.v1.ConnectionEnd connection = 2 [json_name = "connection"];</code>
   * @return The connection.
   */
  com.ibc.core.connection.v1.ConnectionEnd getConnection();
  /**
   * <code>.ibc.core.connection.v1.ConnectionEnd connection = 2 [json_name = "connection"];</code>
   */
  com.ibc.core.connection.v1.ConnectionEndOrBuilder getConnectionOrBuilder();
}
