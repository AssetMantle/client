// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: cosmos/vesting/v1beta1/tx.proto

package com.cosmos.vesting.v1beta1;

public interface MsgCreateVestingAccountOrBuilder extends
    // @@protoc_insertion_point(interface_extends:cosmos.vesting.v1beta1.MsgCreateVestingAccount)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>string from_address = 1 [json_name = "fromAddress", (.gogoproto.moretags) = "yaml:&#92;"from_address&#92;""];</code>
   * @return The fromAddress.
   */
  java.lang.String getFromAddress();
  /**
   * <code>string from_address = 1 [json_name = "fromAddress", (.gogoproto.moretags) = "yaml:&#92;"from_address&#92;""];</code>
   * @return The bytes for fromAddress.
   */
  com.google.protobuf.ByteString
      getFromAddressBytes();

  /**
   * <code>string to_address = 2 [json_name = "toAddress", (.gogoproto.moretags) = "yaml:&#92;"to_address&#92;""];</code>
   * @return The toAddress.
   */
  java.lang.String getToAddress();
  /**
   * <code>string to_address = 2 [json_name = "toAddress", (.gogoproto.moretags) = "yaml:&#92;"to_address&#92;""];</code>
   * @return The bytes for toAddress.
   */
  com.google.protobuf.ByteString
      getToAddressBytes();

  /**
   * <code>repeated .cosmos.base.v1beta1.Coin amount = 3 [json_name = "amount", (.gogoproto.nullable) = false, (.gogoproto.castrepeated) = "github.com/cosmos/cosmos-sdk/types.Coins"];</code>
   */
  java.util.List<com.cosmos.base.v1beta1.Coin> 
      getAmountList();
  /**
   * <code>repeated .cosmos.base.v1beta1.Coin amount = 3 [json_name = "amount", (.gogoproto.nullable) = false, (.gogoproto.castrepeated) = "github.com/cosmos/cosmos-sdk/types.Coins"];</code>
   */
  com.cosmos.base.v1beta1.Coin getAmount(int index);
  /**
   * <code>repeated .cosmos.base.v1beta1.Coin amount = 3 [json_name = "amount", (.gogoproto.nullable) = false, (.gogoproto.castrepeated) = "github.com/cosmos/cosmos-sdk/types.Coins"];</code>
   */
  int getAmountCount();
  /**
   * <code>repeated .cosmos.base.v1beta1.Coin amount = 3 [json_name = "amount", (.gogoproto.nullable) = false, (.gogoproto.castrepeated) = "github.com/cosmos/cosmos-sdk/types.Coins"];</code>
   */
  java.util.List<? extends com.cosmos.base.v1beta1.CoinOrBuilder> 
      getAmountOrBuilderList();
  /**
   * <code>repeated .cosmos.base.v1beta1.Coin amount = 3 [json_name = "amount", (.gogoproto.nullable) = false, (.gogoproto.castrepeated) = "github.com/cosmos/cosmos-sdk/types.Coins"];</code>
   */
  com.cosmos.base.v1beta1.CoinOrBuilder getAmountOrBuilder(
      int index);

  /**
   * <code>int64 end_time = 4 [json_name = "endTime", (.gogoproto.moretags) = "yaml:&#92;"end_time&#92;""];</code>
   * @return The endTime.
   */
  long getEndTime();

  /**
   * <code>bool delayed = 5 [json_name = "delayed"];</code>
   * @return The delayed.
   */
  boolean getDelayed();
}
