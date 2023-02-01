// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: ibc/core/client/v1/query.proto

package com.ibc.core.client.v1;

public interface QueryConsensusStatesRequestOrBuilder extends
    // @@protoc_insertion_point(interface_extends:ibc.core.client.v1.QueryConsensusStatesRequest)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   * client identifier
   * </pre>
   *
   * <code>string client_id = 1 [json_name = "clientId"];</code>
   * @return The clientId.
   */
  java.lang.String getClientId();
  /**
   * <pre>
   * client identifier
   * </pre>
   *
   * <code>string client_id = 1 [json_name = "clientId"];</code>
   * @return The bytes for clientId.
   */
  com.google.protobuf.ByteString
      getClientIdBytes();

  /**
   * <pre>
   * pagination request
   * </pre>
   *
   * <code>.cosmos.base.query.v1beta1.PageRequest pagination = 2 [json_name = "pagination"];</code>
   * @return Whether the pagination field is set.
   */
  boolean hasPagination();
  /**
   * <pre>
   * pagination request
   * </pre>
   *
   * <code>.cosmos.base.query.v1beta1.PageRequest pagination = 2 [json_name = "pagination"];</code>
   * @return The pagination.
   */
  com.cosmos.base.query.v1beta1.PageRequest getPagination();
  /**
   * <pre>
   * pagination request
   * </pre>
   *
   * <code>.cosmos.base.query.v1beta1.PageRequest pagination = 2 [json_name = "pagination"];</code>
   */
  com.cosmos.base.query.v1beta1.PageRequestOrBuilder getPaginationOrBuilder();
}
