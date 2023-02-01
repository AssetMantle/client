// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: cosmos/distribution/v1beta1/query.proto

package com.cosmos.distribution.v1beta1;

public interface QueryDelegationTotalRewardsResponseOrBuilder extends
    // @@protoc_insertion_point(interface_extends:cosmos.distribution.v1beta1.QueryDelegationTotalRewardsResponse)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   * rewards defines all the rewards accrued by a delegator.
   * </pre>
   *
   * <code>repeated .cosmos.distribution.v1beta1.DelegationDelegatorReward rewards = 1 [json_name = "rewards", (.gogoproto.nullable) = false];</code>
   */
  java.util.List<com.cosmos.distribution.v1beta1.DelegationDelegatorReward> 
      getRewardsList();
  /**
   * <pre>
   * rewards defines all the rewards accrued by a delegator.
   * </pre>
   *
   * <code>repeated .cosmos.distribution.v1beta1.DelegationDelegatorReward rewards = 1 [json_name = "rewards", (.gogoproto.nullable) = false];</code>
   */
  com.cosmos.distribution.v1beta1.DelegationDelegatorReward getRewards(int index);
  /**
   * <pre>
   * rewards defines all the rewards accrued by a delegator.
   * </pre>
   *
   * <code>repeated .cosmos.distribution.v1beta1.DelegationDelegatorReward rewards = 1 [json_name = "rewards", (.gogoproto.nullable) = false];</code>
   */
  int getRewardsCount();
  /**
   * <pre>
   * rewards defines all the rewards accrued by a delegator.
   * </pre>
   *
   * <code>repeated .cosmos.distribution.v1beta1.DelegationDelegatorReward rewards = 1 [json_name = "rewards", (.gogoproto.nullable) = false];</code>
   */
  java.util.List<? extends com.cosmos.distribution.v1beta1.DelegationDelegatorRewardOrBuilder> 
      getRewardsOrBuilderList();
  /**
   * <pre>
   * rewards defines all the rewards accrued by a delegator.
   * </pre>
   *
   * <code>repeated .cosmos.distribution.v1beta1.DelegationDelegatorReward rewards = 1 [json_name = "rewards", (.gogoproto.nullable) = false];</code>
   */
  com.cosmos.distribution.v1beta1.DelegationDelegatorRewardOrBuilder getRewardsOrBuilder(
      int index);

  /**
   * <pre>
   * total defines the sum of all the rewards.
   * </pre>
   *
   * <code>repeated .cosmos.base.v1beta1.DecCoin total = 2 [json_name = "total", (.gogoproto.nullable) = false, (.gogoproto.castrepeated) = "github.com/cosmos/cosmos-sdk/types.DecCoins"];</code>
   */
  java.util.List<com.cosmos.base.v1beta1.DecCoin> 
      getTotalList();
  /**
   * <pre>
   * total defines the sum of all the rewards.
   * </pre>
   *
   * <code>repeated .cosmos.base.v1beta1.DecCoin total = 2 [json_name = "total", (.gogoproto.nullable) = false, (.gogoproto.castrepeated) = "github.com/cosmos/cosmos-sdk/types.DecCoins"];</code>
   */
  com.cosmos.base.v1beta1.DecCoin getTotal(int index);
  /**
   * <pre>
   * total defines the sum of all the rewards.
   * </pre>
   *
   * <code>repeated .cosmos.base.v1beta1.DecCoin total = 2 [json_name = "total", (.gogoproto.nullable) = false, (.gogoproto.castrepeated) = "github.com/cosmos/cosmos-sdk/types.DecCoins"];</code>
   */
  int getTotalCount();
  /**
   * <pre>
   * total defines the sum of all the rewards.
   * </pre>
   *
   * <code>repeated .cosmos.base.v1beta1.DecCoin total = 2 [json_name = "total", (.gogoproto.nullable) = false, (.gogoproto.castrepeated) = "github.com/cosmos/cosmos-sdk/types.DecCoins"];</code>
   */
  java.util.List<? extends com.cosmos.base.v1beta1.DecCoinOrBuilder> 
      getTotalOrBuilderList();
  /**
   * <pre>
   * total defines the sum of all the rewards.
   * </pre>
   *
   * <code>repeated .cosmos.base.v1beta1.DecCoin total = 2 [json_name = "total", (.gogoproto.nullable) = false, (.gogoproto.castrepeated) = "github.com/cosmos/cosmos-sdk/types.DecCoins"];</code>
   */
  com.cosmos.base.v1beta1.DecCoinOrBuilder getTotalOrBuilder(
      int index);
}
