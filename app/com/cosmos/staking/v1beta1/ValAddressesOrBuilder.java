// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: cosmos/staking/v1beta1/staking.proto

package com.cosmos.staking.v1beta1;

public interface ValAddressesOrBuilder extends
    // @@protoc_insertion_point(interface_extends:cosmos.staking.v1beta1.ValAddresses)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>repeated string addresses = 1 [json_name = "addresses"];</code>
   * @return A list containing the addresses.
   */
  java.util.List<java.lang.String>
      getAddressesList();
  /**
   * <code>repeated string addresses = 1 [json_name = "addresses"];</code>
   * @return The count of addresses.
   */
  int getAddressesCount();
  /**
   * <code>repeated string addresses = 1 [json_name = "addresses"];</code>
   * @param index The index of the element to return.
   * @return The addresses at the given index.
   */
  java.lang.String getAddresses(int index);
  /**
   * <code>repeated string addresses = 1 [json_name = "addresses"];</code>
   * @param index The index of the value to return.
   * @return The bytes of the addresses at the given index.
   */
  com.google.protobuf.ByteString
      getAddressesBytes(int index);
}