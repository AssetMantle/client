// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: cosmos/base/abci/v1beta1/abci.proto

package com.cosmos.base.abci.v1beta1;

public interface MsgDataOrBuilder extends
    // @@protoc_insertion_point(interface_extends:cosmos.base.abci.v1beta1.MsgData)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>string msg_type = 1 [json_name = "msgType"];</code>
   * @return The msgType.
   */
  java.lang.String getMsgType();
  /**
   * <code>string msg_type = 1 [json_name = "msgType"];</code>
   * @return The bytes for msgType.
   */
  com.google.protobuf.ByteString
      getMsgTypeBytes();

  /**
   * <code>bytes data = 2 [json_name = "data"];</code>
   * @return The data.
   */
  com.google.protobuf.ByteString getData();
}
