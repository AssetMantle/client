// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: cosmos/authz/v1beta1/tx.proto

package com.cosmos.authz.v1beta1;

public interface MsgExecOrBuilder extends
    // @@protoc_insertion_point(interface_extends:cosmos.authz.v1beta1.MsgExec)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>string grantee = 1 [json_name = "grantee"];</code>
   * @return The grantee.
   */
  java.lang.String getGrantee();
  /**
   * <code>string grantee = 1 [json_name = "grantee"];</code>
   * @return The bytes for grantee.
   */
  com.google.protobuf.ByteString
      getGranteeBytes();

  /**
   * <pre>
   * Authorization Msg requests to execute. Each msg must implement Authorization interface
   * The x/authz will try to find a grant matching (msg.signers[0], grantee, MsgTypeURL(msg))
   * triple and validate it.
   * </pre>
   *
   * <code>repeated .google.protobuf.Any msgs = 2 [json_name = "msgs", (.cosmos_proto.accepts_interface) = "sdk.Msg, authz.Authorization"];</code>
   */
  java.util.List<com.google.protobuf.Any> 
      getMsgsList();
  /**
   * <pre>
   * Authorization Msg requests to execute. Each msg must implement Authorization interface
   * The x/authz will try to find a grant matching (msg.signers[0], grantee, MsgTypeURL(msg))
   * triple and validate it.
   * </pre>
   *
   * <code>repeated .google.protobuf.Any msgs = 2 [json_name = "msgs", (.cosmos_proto.accepts_interface) = "sdk.Msg, authz.Authorization"];</code>
   */
  com.google.protobuf.Any getMsgs(int index);
  /**
   * <pre>
   * Authorization Msg requests to execute. Each msg must implement Authorization interface
   * The x/authz will try to find a grant matching (msg.signers[0], grantee, MsgTypeURL(msg))
   * triple and validate it.
   * </pre>
   *
   * <code>repeated .google.protobuf.Any msgs = 2 [json_name = "msgs", (.cosmos_proto.accepts_interface) = "sdk.Msg, authz.Authorization"];</code>
   */
  int getMsgsCount();
  /**
   * <pre>
   * Authorization Msg requests to execute. Each msg must implement Authorization interface
   * The x/authz will try to find a grant matching (msg.signers[0], grantee, MsgTypeURL(msg))
   * triple and validate it.
   * </pre>
   *
   * <code>repeated .google.protobuf.Any msgs = 2 [json_name = "msgs", (.cosmos_proto.accepts_interface) = "sdk.Msg, authz.Authorization"];</code>
   */
  java.util.List<? extends com.google.protobuf.AnyOrBuilder> 
      getMsgsOrBuilderList();
  /**
   * <pre>
   * Authorization Msg requests to execute. Each msg must implement Authorization interface
   * The x/authz will try to find a grant matching (msg.signers[0], grantee, MsgTypeURL(msg))
   * triple and validate it.
   * </pre>
   *
   * <code>repeated .google.protobuf.Any msgs = 2 [json_name = "msgs", (.cosmos_proto.accepts_interface) = "sdk.Msg, authz.Authorization"];</code>
   */
  com.google.protobuf.AnyOrBuilder getMsgsOrBuilder(
      int index);
}
