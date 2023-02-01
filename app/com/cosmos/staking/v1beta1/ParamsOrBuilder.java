// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: cosmos/staking/v1beta1/staking.proto

package com.cosmos.staking.v1beta1;

public interface ParamsOrBuilder extends
    // @@protoc_insertion_point(interface_extends:cosmos.staking.v1beta1.Params)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   * unbonding_time is the time duration of unbonding.
   * </pre>
   *
   * <code>.google.protobuf.Duration unbonding_time = 1 [json_name = "unbondingTime", (.gogoproto.nullable) = false, (.gogoproto.moretags) = "yaml:&#92;"unbonding_time&#92;"", (.gogoproto.stdduration) = true];</code>
   * @return Whether the unbondingTime field is set.
   */
  boolean hasUnbondingTime();
  /**
   * <pre>
   * unbonding_time is the time duration of unbonding.
   * </pre>
   *
   * <code>.google.protobuf.Duration unbonding_time = 1 [json_name = "unbondingTime", (.gogoproto.nullable) = false, (.gogoproto.moretags) = "yaml:&#92;"unbonding_time&#92;"", (.gogoproto.stdduration) = true];</code>
   * @return The unbondingTime.
   */
  com.google.protobuf.Duration getUnbondingTime();
  /**
   * <pre>
   * unbonding_time is the time duration of unbonding.
   * </pre>
   *
   * <code>.google.protobuf.Duration unbonding_time = 1 [json_name = "unbondingTime", (.gogoproto.nullable) = false, (.gogoproto.moretags) = "yaml:&#92;"unbonding_time&#92;"", (.gogoproto.stdduration) = true];</code>
   */
  com.google.protobuf.DurationOrBuilder getUnbondingTimeOrBuilder();

  /**
   * <pre>
   * max_validators is the maximum number of validators.
   * </pre>
   *
   * <code>uint32 max_validators = 2 [json_name = "maxValidators", (.gogoproto.moretags) = "yaml:&#92;"max_validators&#92;""];</code>
   * @return The maxValidators.
   */
  int getMaxValidators();

  /**
   * <pre>
   * max_entries is the max entries for either unbonding delegation or redelegation (per pair/trio).
   * </pre>
   *
   * <code>uint32 max_entries = 3 [json_name = "maxEntries", (.gogoproto.moretags) = "yaml:&#92;"max_entries&#92;""];</code>
   * @return The maxEntries.
   */
  int getMaxEntries();

  /**
   * <pre>
   * historical_entries is the number of historical entries to persist.
   * </pre>
   *
   * <code>uint32 historical_entries = 4 [json_name = "historicalEntries", (.gogoproto.moretags) = "yaml:&#92;"historical_entries&#92;""];</code>
   * @return The historicalEntries.
   */
  int getHistoricalEntries();

  /**
   * <pre>
   * bond_denom defines the bondable coin denomination.
   * </pre>
   *
   * <code>string bond_denom = 5 [json_name = "bondDenom", (.gogoproto.moretags) = "yaml:&#92;"bond_denom&#92;""];</code>
   * @return The bondDenom.
   */
  java.lang.String getBondDenom();
  /**
   * <pre>
   * bond_denom defines the bondable coin denomination.
   * </pre>
   *
   * <code>string bond_denom = 5 [json_name = "bondDenom", (.gogoproto.moretags) = "yaml:&#92;"bond_denom&#92;""];</code>
   * @return The bytes for bondDenom.
   */
  com.google.protobuf.ByteString
      getBondDenomBytes();
}
