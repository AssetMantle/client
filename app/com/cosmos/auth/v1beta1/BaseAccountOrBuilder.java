// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: cosmos/auth/v1beta1/auth.proto

package com.cosmos.auth.v1beta1;

public interface BaseAccountOrBuilder extends
    // @@protoc_insertion_point(interface_extends:cosmos.auth.v1beta1.BaseAccount)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>string address = 1 [json_name = "address"];</code>
   * @return The address.
   */
  java.lang.String getAddress();
  /**
   * <code>string address = 1 [json_name = "address"];</code>
   * @return The bytes for address.
   */
  com.google.protobuf.ByteString
      getAddressBytes();

  /**
   * <code>.google.protobuf.Any pub_key = 2 [json_name = "pubKey", (.gogoproto.jsontag) = "public_key,omitempty", (.gogoproto.moretags) = "yaml:&#92;"public_key&#92;""];</code>
   * @return Whether the pubKey field is set.
   */
  boolean hasPubKey();
  /**
   * <code>.google.protobuf.Any pub_key = 2 [json_name = "pubKey", (.gogoproto.jsontag) = "public_key,omitempty", (.gogoproto.moretags) = "yaml:&#92;"public_key&#92;""];</code>
   * @return The pubKey.
   */
  com.google.protobuf.Any getPubKey();
  /**
   * <code>.google.protobuf.Any pub_key = 2 [json_name = "pubKey", (.gogoproto.jsontag) = "public_key,omitempty", (.gogoproto.moretags) = "yaml:&#92;"public_key&#92;""];</code>
   */
  com.google.protobuf.AnyOrBuilder getPubKeyOrBuilder();

  /**
   * <code>uint64 account_number = 3 [json_name = "accountNumber", (.gogoproto.moretags) = "yaml:&#92;"account_number&#92;""];</code>
   * @return The accountNumber.
   */
  long getAccountNumber();

  /**
   * <code>uint64 sequence = 4 [json_name = "sequence"];</code>
   * @return The sequence.
   */
  long getSequence();
}
