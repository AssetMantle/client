// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: cosmos/bank/v1beta1/query.proto

package com.cosmos.bank.v1beta1;

public interface QueryBalanceRequestOrBuilder extends
    // @@protoc_insertion_point(interface_extends:cosmos.bank.v1beta1.QueryBalanceRequest)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   * address is the address to query balances for.
   * </pre>
   *
   * <code>string address = 1 [json_name = "address"];</code>
   * @return The address.
   */
  java.lang.String getAddress();
  /**
   * <pre>
   * address is the address to query balances for.
   * </pre>
   *
   * <code>string address = 1 [json_name = "address"];</code>
   * @return The bytes for address.
   */
  com.google.protobuf.ByteString
      getAddressBytes();

  /**
   * <pre>
   * denom is the coin denom to query balances for.
   * </pre>
   *
   * <code>string denom = 2 [json_name = "denom"];</code>
   * @return The denom.
   */
  java.lang.String getDenom();
  /**
   * <pre>
   * denom is the coin denom to query balances for.
   * </pre>
   *
   * <code>string denom = 2 [json_name = "denom"];</code>
   * @return The bytes for denom.
   */
  com.google.protobuf.ByteString
      getDenomBytes();
}
