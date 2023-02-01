// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: cosmos/feegrant/v1beta1/feegrant.proto

package com.cosmos.feegrant.v1beta1;

public interface AllowedMsgAllowanceOrBuilder extends
    // @@protoc_insertion_point(interface_extends:cosmos.feegrant.v1beta1.AllowedMsgAllowance)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   * allowance can be any of basic and filtered fee allowance.
   * </pre>
   *
   * <code>.google.protobuf.Any allowance = 1 [json_name = "allowance", (.cosmos_proto.accepts_interface) = "FeeAllowanceI"];</code>
   * @return Whether the allowance field is set.
   */
  boolean hasAllowance();
  /**
   * <pre>
   * allowance can be any of basic and filtered fee allowance.
   * </pre>
   *
   * <code>.google.protobuf.Any allowance = 1 [json_name = "allowance", (.cosmos_proto.accepts_interface) = "FeeAllowanceI"];</code>
   * @return The allowance.
   */
  com.google.protobuf.Any getAllowance();
  /**
   * <pre>
   * allowance can be any of basic and filtered fee allowance.
   * </pre>
   *
   * <code>.google.protobuf.Any allowance = 1 [json_name = "allowance", (.cosmos_proto.accepts_interface) = "FeeAllowanceI"];</code>
   */
  com.google.protobuf.AnyOrBuilder getAllowanceOrBuilder();

  /**
   * <pre>
   * allowed_messages are the messages for which the grantee has the access.
   * </pre>
   *
   * <code>repeated string allowed_messages = 2 [json_name = "allowedMessages"];</code>
   * @return A list containing the allowedMessages.
   */
  java.util.List<java.lang.String>
      getAllowedMessagesList();
  /**
   * <pre>
   * allowed_messages are the messages for which the grantee has the access.
   * </pre>
   *
   * <code>repeated string allowed_messages = 2 [json_name = "allowedMessages"];</code>
   * @return The count of allowedMessages.
   */
  int getAllowedMessagesCount();
  /**
   * <pre>
   * allowed_messages are the messages for which the grantee has the access.
   * </pre>
   *
   * <code>repeated string allowed_messages = 2 [json_name = "allowedMessages"];</code>
   * @param index The index of the element to return.
   * @return The allowedMessages at the given index.
   */
  java.lang.String getAllowedMessages(int index);
  /**
   * <pre>
   * allowed_messages are the messages for which the grantee has the access.
   * </pre>
   *
   * <code>repeated string allowed_messages = 2 [json_name = "allowedMessages"];</code>
   * @param index The index of the value to return.
   * @return The bytes of the allowedMessages at the given index.
   */
  com.google.protobuf.ByteString
      getAllowedMessagesBytes(int index);
}
