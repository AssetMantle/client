// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: cosmos/slashing/v1beta1/genesis.proto

package com.cosmos.slashing.v1beta1;

public interface SigningInfoOrBuilder extends
    // @@protoc_insertion_point(interface_extends:cosmos.slashing.v1beta1.SigningInfo)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   * address is the validator address.
   * </pre>
   *
   * <code>string address = 1 [json_name = "address"];</code>
   * @return The address.
   */
  java.lang.String getAddress();
  /**
   * <pre>
   * address is the validator address.
   * </pre>
   *
   * <code>string address = 1 [json_name = "address"];</code>
   * @return The bytes for address.
   */
  com.google.protobuf.ByteString
      getAddressBytes();

  /**
   * <pre>
   * validator_signing_info represents the signing info of this validator.
   * </pre>
   *
   * <code>.cosmos.slashing.v1beta1.ValidatorSigningInfo validator_signing_info = 2 [json_name = "validatorSigningInfo", (.gogoproto.nullable) = false, (.gogoproto.moretags) = "yaml:&#92;"validator_signing_info&#92;""];</code>
   * @return Whether the validatorSigningInfo field is set.
   */
  boolean hasValidatorSigningInfo();
  /**
   * <pre>
   * validator_signing_info represents the signing info of this validator.
   * </pre>
   *
   * <code>.cosmos.slashing.v1beta1.ValidatorSigningInfo validator_signing_info = 2 [json_name = "validatorSigningInfo", (.gogoproto.nullable) = false, (.gogoproto.moretags) = "yaml:&#92;"validator_signing_info&#92;""];</code>
   * @return The validatorSigningInfo.
   */
  com.cosmos.slashing.v1beta1.ValidatorSigningInfo getValidatorSigningInfo();
  /**
   * <pre>
   * validator_signing_info represents the signing info of this validator.
   * </pre>
   *
   * <code>.cosmos.slashing.v1beta1.ValidatorSigningInfo validator_signing_info = 2 [json_name = "validatorSigningInfo", (.gogoproto.nullable) = false, (.gogoproto.moretags) = "yaml:&#92;"validator_signing_info&#92;""];</code>
   */
  com.cosmos.slashing.v1beta1.ValidatorSigningInfoOrBuilder getValidatorSigningInfoOrBuilder();
}
