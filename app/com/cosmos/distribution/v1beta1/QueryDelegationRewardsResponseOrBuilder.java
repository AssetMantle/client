// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: cosmos/distribution/v1beta1/query.proto

package com.cosmos.distribution.v1beta1;

public interface QueryDelegationRewardsResponseOrBuilder extends
    // @@protoc_insertion_point(interface_extends:cosmos.distribution.v1beta1.QueryDelegationRewardsResponse)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   * rewards defines the rewards accrued by a delegation.
   * </pre>
   *
   * <code>repeated .cosmos.base.v1beta1.DecCoin rewards = 1 [json_name = "rewards", (.gogoproto.nullable) = false, (.gogoproto.castrepeated) = "github.com/cosmos/cosmos-sdk/types.DecCoins"];</code>
   */
  java.util.List<com.cosmos.base.v1beta1.DecCoin> 
      getRewardsList();
  /**
   * <pre>
   * rewards defines the rewards accrued by a delegation.
   * </pre>
   *
   * <code>repeated .cosmos.base.v1beta1.DecCoin rewards = 1 [json_name = "rewards", (.gogoproto.nullable) = false, (.gogoproto.castrepeated) = "github.com/cosmos/cosmos-sdk/types.DecCoins"];</code>
   */
  com.cosmos.base.v1beta1.DecCoin getRewards(int index);
  /**
   * <pre>
   * rewards defines the rewards accrued by a delegation.
   * </pre>
   *
   * <code>repeated .cosmos.base.v1beta1.DecCoin rewards = 1 [json_name = "rewards", (.gogoproto.nullable) = false, (.gogoproto.castrepeated) = "github.com/cosmos/cosmos-sdk/types.DecCoins"];</code>
   */
  int getRewardsCount();
  /**
   * <pre>
   * rewards defines the rewards accrued by a delegation.
   * </pre>
   *
   * <code>repeated .cosmos.base.v1beta1.DecCoin rewards = 1 [json_name = "rewards", (.gogoproto.nullable) = false, (.gogoproto.castrepeated) = "github.com/cosmos/cosmos-sdk/types.DecCoins"];</code>
   */
  java.util.List<? extends com.cosmos.base.v1beta1.DecCoinOrBuilder> 
      getRewardsOrBuilderList();
  /**
   * <pre>
   * rewards defines the rewards accrued by a delegation.
   * </pre>
   *
   * <code>repeated .cosmos.base.v1beta1.DecCoin rewards = 1 [json_name = "rewards", (.gogoproto.nullable) = false, (.gogoproto.castrepeated) = "github.com/cosmos/cosmos-sdk/types.DecCoins"];</code>
   */
  com.cosmos.base.v1beta1.DecCoinOrBuilder getRewardsOrBuilder(
      int index);
}
