// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: cosmos/vesting/v1beta1/vesting.proto

package com.cosmos.vesting.v1beta1;

public interface BaseVestingAccountOrBuilder extends
    // @@protoc_insertion_point(interface_extends:cosmos.vesting.v1beta1.BaseVestingAccount)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>.cosmos.auth.v1beta1.BaseAccount base_account = 1 [json_name = "baseAccount", (.gogoproto.embed) = true];</code>
   * @return Whether the baseAccount field is set.
   */
  boolean hasBaseAccount();
  /**
   * <code>.cosmos.auth.v1beta1.BaseAccount base_account = 1 [json_name = "baseAccount", (.gogoproto.embed) = true];</code>
   * @return The baseAccount.
   */
  com.cosmos.auth.v1beta1.BaseAccount getBaseAccount();
  /**
   * <code>.cosmos.auth.v1beta1.BaseAccount base_account = 1 [json_name = "baseAccount", (.gogoproto.embed) = true];</code>
   */
  com.cosmos.auth.v1beta1.BaseAccountOrBuilder getBaseAccountOrBuilder();

  /**
   * <code>repeated .cosmos.base.v1beta1.Coin original_vesting = 2 [json_name = "originalVesting", (.gogoproto.nullable) = false, (.gogoproto.moretags) = "yaml:&#92;"original_vesting&#92;"", (.gogoproto.castrepeated) = "github.com/cosmos/cosmos-sdk/types.Coins"];</code>
   */
  java.util.List<com.cosmos.base.v1beta1.Coin> 
      getOriginalVestingList();
  /**
   * <code>repeated .cosmos.base.v1beta1.Coin original_vesting = 2 [json_name = "originalVesting", (.gogoproto.nullable) = false, (.gogoproto.moretags) = "yaml:&#92;"original_vesting&#92;"", (.gogoproto.castrepeated) = "github.com/cosmos/cosmos-sdk/types.Coins"];</code>
   */
  com.cosmos.base.v1beta1.Coin getOriginalVesting(int index);
  /**
   * <code>repeated .cosmos.base.v1beta1.Coin original_vesting = 2 [json_name = "originalVesting", (.gogoproto.nullable) = false, (.gogoproto.moretags) = "yaml:&#92;"original_vesting&#92;"", (.gogoproto.castrepeated) = "github.com/cosmos/cosmos-sdk/types.Coins"];</code>
   */
  int getOriginalVestingCount();
  /**
   * <code>repeated .cosmos.base.v1beta1.Coin original_vesting = 2 [json_name = "originalVesting", (.gogoproto.nullable) = false, (.gogoproto.moretags) = "yaml:&#92;"original_vesting&#92;"", (.gogoproto.castrepeated) = "github.com/cosmos/cosmos-sdk/types.Coins"];</code>
   */
  java.util.List<? extends com.cosmos.base.v1beta1.CoinOrBuilder> 
      getOriginalVestingOrBuilderList();
  /**
   * <code>repeated .cosmos.base.v1beta1.Coin original_vesting = 2 [json_name = "originalVesting", (.gogoproto.nullable) = false, (.gogoproto.moretags) = "yaml:&#92;"original_vesting&#92;"", (.gogoproto.castrepeated) = "github.com/cosmos/cosmos-sdk/types.Coins"];</code>
   */
  com.cosmos.base.v1beta1.CoinOrBuilder getOriginalVestingOrBuilder(
      int index);

  /**
   * <code>repeated .cosmos.base.v1beta1.Coin delegated_free = 3 [json_name = "delegatedFree", (.gogoproto.nullable) = false, (.gogoproto.moretags) = "yaml:&#92;"delegated_free&#92;"", (.gogoproto.castrepeated) = "github.com/cosmos/cosmos-sdk/types.Coins"];</code>
   */
  java.util.List<com.cosmos.base.v1beta1.Coin> 
      getDelegatedFreeList();
  /**
   * <code>repeated .cosmos.base.v1beta1.Coin delegated_free = 3 [json_name = "delegatedFree", (.gogoproto.nullable) = false, (.gogoproto.moretags) = "yaml:&#92;"delegated_free&#92;"", (.gogoproto.castrepeated) = "github.com/cosmos/cosmos-sdk/types.Coins"];</code>
   */
  com.cosmos.base.v1beta1.Coin getDelegatedFree(int index);
  /**
   * <code>repeated .cosmos.base.v1beta1.Coin delegated_free = 3 [json_name = "delegatedFree", (.gogoproto.nullable) = false, (.gogoproto.moretags) = "yaml:&#92;"delegated_free&#92;"", (.gogoproto.castrepeated) = "github.com/cosmos/cosmos-sdk/types.Coins"];</code>
   */
  int getDelegatedFreeCount();
  /**
   * <code>repeated .cosmos.base.v1beta1.Coin delegated_free = 3 [json_name = "delegatedFree", (.gogoproto.nullable) = false, (.gogoproto.moretags) = "yaml:&#92;"delegated_free&#92;"", (.gogoproto.castrepeated) = "github.com/cosmos/cosmos-sdk/types.Coins"];</code>
   */
  java.util.List<? extends com.cosmos.base.v1beta1.CoinOrBuilder> 
      getDelegatedFreeOrBuilderList();
  /**
   * <code>repeated .cosmos.base.v1beta1.Coin delegated_free = 3 [json_name = "delegatedFree", (.gogoproto.nullable) = false, (.gogoproto.moretags) = "yaml:&#92;"delegated_free&#92;"", (.gogoproto.castrepeated) = "github.com/cosmos/cosmos-sdk/types.Coins"];</code>
   */
  com.cosmos.base.v1beta1.CoinOrBuilder getDelegatedFreeOrBuilder(
      int index);

  /**
   * <code>repeated .cosmos.base.v1beta1.Coin delegated_vesting = 4 [json_name = "delegatedVesting", (.gogoproto.nullable) = false, (.gogoproto.moretags) = "yaml:&#92;"delegated_vesting&#92;"", (.gogoproto.castrepeated) = "github.com/cosmos/cosmos-sdk/types.Coins"];</code>
   */
  java.util.List<com.cosmos.base.v1beta1.Coin> 
      getDelegatedVestingList();
  /**
   * <code>repeated .cosmos.base.v1beta1.Coin delegated_vesting = 4 [json_name = "delegatedVesting", (.gogoproto.nullable) = false, (.gogoproto.moretags) = "yaml:&#92;"delegated_vesting&#92;"", (.gogoproto.castrepeated) = "github.com/cosmos/cosmos-sdk/types.Coins"];</code>
   */
  com.cosmos.base.v1beta1.Coin getDelegatedVesting(int index);
  /**
   * <code>repeated .cosmos.base.v1beta1.Coin delegated_vesting = 4 [json_name = "delegatedVesting", (.gogoproto.nullable) = false, (.gogoproto.moretags) = "yaml:&#92;"delegated_vesting&#92;"", (.gogoproto.castrepeated) = "github.com/cosmos/cosmos-sdk/types.Coins"];</code>
   */
  int getDelegatedVestingCount();
  /**
   * <code>repeated .cosmos.base.v1beta1.Coin delegated_vesting = 4 [json_name = "delegatedVesting", (.gogoproto.nullable) = false, (.gogoproto.moretags) = "yaml:&#92;"delegated_vesting&#92;"", (.gogoproto.castrepeated) = "github.com/cosmos/cosmos-sdk/types.Coins"];</code>
   */
  java.util.List<? extends com.cosmos.base.v1beta1.CoinOrBuilder> 
      getDelegatedVestingOrBuilderList();
  /**
   * <code>repeated .cosmos.base.v1beta1.Coin delegated_vesting = 4 [json_name = "delegatedVesting", (.gogoproto.nullable) = false, (.gogoproto.moretags) = "yaml:&#92;"delegated_vesting&#92;"", (.gogoproto.castrepeated) = "github.com/cosmos/cosmos-sdk/types.Coins"];</code>
   */
  com.cosmos.base.v1beta1.CoinOrBuilder getDelegatedVestingOrBuilder(
      int index);

  /**
   * <code>int64 end_time = 5 [json_name = "endTime", (.gogoproto.moretags) = "yaml:&#92;"end_time&#92;""];</code>
   * @return The endTime.
   */
  long getEndTime();
}