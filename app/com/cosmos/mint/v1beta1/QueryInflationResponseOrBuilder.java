// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: cosmos/mint/v1beta1/query.proto

package com.cosmos.mint.v1beta1;

public interface QueryInflationResponseOrBuilder extends
    // @@protoc_insertion_point(interface_extends:cosmos.mint.v1beta1.QueryInflationResponse)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   * inflation is the current minting inflation value.
   * </pre>
   *
   * <code>bytes inflation = 1 [json_name = "inflation", (.gogoproto.nullable) = false, (.gogoproto.customtype) = "github.com/cosmos/cosmos-sdk/types.Dec"];</code>
   * @return The inflation.
   */
  com.google.protobuf.ByteString getInflation();
}
