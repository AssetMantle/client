// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: ibc/core/client/v1/query.proto

package com.ibc.core.client.v1;

public interface QueryUpgradedClientStateResponseOrBuilder extends
    // @@protoc_insertion_point(interface_extends:ibc.core.client.v1.QueryUpgradedClientStateResponse)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   * client state associated with the request identifier
   * </pre>
   *
   * <code>.google.protobuf.Any upgraded_client_state = 1 [json_name = "upgradedClientState"];</code>
   * @return Whether the upgradedClientState field is set.
   */
  boolean hasUpgradedClientState();
  /**
   * <pre>
   * client state associated with the request identifier
   * </pre>
   *
   * <code>.google.protobuf.Any upgraded_client_state = 1 [json_name = "upgradedClientState"];</code>
   * @return The upgradedClientState.
   */
  com.google.protobuf.Any getUpgradedClientState();
  /**
   * <pre>
   * client state associated with the request identifier
   * </pre>
   *
   * <code>.google.protobuf.Any upgraded_client_state = 1 [json_name = "upgradedClientState"];</code>
   */
  com.google.protobuf.AnyOrBuilder getUpgradedClientStateOrBuilder();
}
