// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: ibc/lightclients/solomachine/v1/solomachine.proto

package com.ibc.lightclients.solomachine.v1;

public interface ClientStateDataOrBuilder extends
    // @@protoc_insertion_point(interface_extends:ibc.lightclients.solomachine.v1.ClientStateData)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>bytes path = 1 [json_name = "path"];</code>
   * @return The path.
   */
  com.google.protobuf.ByteString getPath();

  /**
   * <code>.google.protobuf.Any client_state = 2 [json_name = "clientState", (.gogoproto.moretags) = "yaml:&#92;"client_state&#92;""];</code>
   * @return Whether the clientState field is set.
   */
  boolean hasClientState();
  /**
   * <code>.google.protobuf.Any client_state = 2 [json_name = "clientState", (.gogoproto.moretags) = "yaml:&#92;"client_state&#92;""];</code>
   * @return The clientState.
   */
  com.google.protobuf.Any getClientState();
  /**
   * <code>.google.protobuf.Any client_state = 2 [json_name = "clientState", (.gogoproto.moretags) = "yaml:&#92;"client_state&#92;""];</code>
   */
  com.google.protobuf.AnyOrBuilder getClientStateOrBuilder();
}
