// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: cosmos/staking/v1beta1/staking.proto

package com.cosmos.staking.v1beta1;

public interface CommissionRatesOrBuilder extends
    // @@protoc_insertion_point(interface_extends:cosmos.staking.v1beta1.CommissionRates)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   * rate is the commission rate charged to delegators, as a fraction.
   * </pre>
   *
   * <code>string rate = 1 [json_name = "rate", (.gogoproto.nullable) = false, (.gogoproto.customtype) = "github.com/cosmos/cosmos-sdk/types.Dec"];</code>
   * @return The rate.
   */
  java.lang.String getRate();
  /**
   * <pre>
   * rate is the commission rate charged to delegators, as a fraction.
   * </pre>
   *
   * <code>string rate = 1 [json_name = "rate", (.gogoproto.nullable) = false, (.gogoproto.customtype) = "github.com/cosmos/cosmos-sdk/types.Dec"];</code>
   * @return The bytes for rate.
   */
  com.google.protobuf.ByteString
      getRateBytes();

  /**
   * <pre>
   * max_rate defines the maximum commission rate which validator can ever charge, as a fraction.
   * </pre>
   *
   * <code>string max_rate = 2 [json_name = "maxRate", (.gogoproto.nullable) = false, (.gogoproto.customtype) = "github.com/cosmos/cosmos-sdk/types.Dec", (.gogoproto.moretags) = "yaml:&#92;"max_rate&#92;""];</code>
   * @return The maxRate.
   */
  java.lang.String getMaxRate();
  /**
   * <pre>
   * max_rate defines the maximum commission rate which validator can ever charge, as a fraction.
   * </pre>
   *
   * <code>string max_rate = 2 [json_name = "maxRate", (.gogoproto.nullable) = false, (.gogoproto.customtype) = "github.com/cosmos/cosmos-sdk/types.Dec", (.gogoproto.moretags) = "yaml:&#92;"max_rate&#92;""];</code>
   * @return The bytes for maxRate.
   */
  com.google.protobuf.ByteString
      getMaxRateBytes();

  /**
   * <pre>
   * max_change_rate defines the maximum daily increase of the validator commission, as a fraction.
   * </pre>
   *
   * <code>string max_change_rate = 3 [json_name = "maxChangeRate", (.gogoproto.nullable) = false, (.gogoproto.customtype) = "github.com/cosmos/cosmos-sdk/types.Dec", (.gogoproto.moretags) = "yaml:&#92;"max_change_rate&#92;""];</code>
   * @return The maxChangeRate.
   */
  java.lang.String getMaxChangeRate();
  /**
   * <pre>
   * max_change_rate defines the maximum daily increase of the validator commission, as a fraction.
   * </pre>
   *
   * <code>string max_change_rate = 3 [json_name = "maxChangeRate", (.gogoproto.nullable) = false, (.gogoproto.customtype) = "github.com/cosmos/cosmos-sdk/types.Dec", (.gogoproto.moretags) = "yaml:&#92;"max_change_rate&#92;""];</code>
   * @return The bytes for maxChangeRate.
   */
  com.google.protobuf.ByteString
      getMaxChangeRateBytes();
}
