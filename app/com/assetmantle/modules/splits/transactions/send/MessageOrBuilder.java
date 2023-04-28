// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: splits/transactions/send/message.proto

package com.assetmantle.modules.splits.transactions.send;

public interface MessageOrBuilder extends
    // @@protoc_insertion_point(interface_extends:assetmantle.modules.splits.transactions.send.Message)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>string from = 1 [json_name = "from"];</code>
   * @return The from.
   */
  java.lang.String getFrom();
  /**
   * <code>string from = 1 [json_name = "from"];</code>
   * @return The bytes for from.
   */
  com.google.protobuf.ByteString
      getFromBytes();

  /**
   * <code>.assetmantle.schema.ids.base.IdentityID from_i_d = 2 [json_name = "fromID"];</code>
   * @return Whether the fromID field is set.
   */
  boolean hasFromID();
  /**
   * <code>.assetmantle.schema.ids.base.IdentityID from_i_d = 2 [json_name = "fromID"];</code>
   * @return The fromID.
   */
  com.assetmantle.schema.ids.base.IdentityID getFromID();
  /**
   * <code>.assetmantle.schema.ids.base.IdentityID from_i_d = 2 [json_name = "fromID"];</code>
   */
  com.assetmantle.schema.ids.base.IdentityIDOrBuilder getFromIDOrBuilder();

  /**
   * <code>.assetmantle.schema.ids.base.IdentityID to_i_d = 3 [json_name = "toID"];</code>
   * @return Whether the toID field is set.
   */
  boolean hasToID();
  /**
   * <code>.assetmantle.schema.ids.base.IdentityID to_i_d = 3 [json_name = "toID"];</code>
   * @return The toID.
   */
  com.assetmantle.schema.ids.base.IdentityID getToID();
  /**
   * <code>.assetmantle.schema.ids.base.IdentityID to_i_d = 3 [json_name = "toID"];</code>
   */
  com.assetmantle.schema.ids.base.IdentityIDOrBuilder getToIDOrBuilder();

  /**
   * <code>.assetmantle.schema.ids.base.AnyOwnableID ownable_i_d = 4 [json_name = "ownableID"];</code>
   * @return Whether the ownableID field is set.
   */
  boolean hasOwnableID();
  /**
   * <code>.assetmantle.schema.ids.base.AnyOwnableID ownable_i_d = 4 [json_name = "ownableID"];</code>
   * @return The ownableID.
   */
  com.assetmantle.schema.ids.base.AnyOwnableID getOwnableID();
  /**
   * <code>.assetmantle.schema.ids.base.AnyOwnableID ownable_i_d = 4 [json_name = "ownableID"];</code>
   */
  com.assetmantle.schema.ids.base.AnyOwnableIDOrBuilder getOwnableIDOrBuilder();

  /**
   * <code>string value = 5 [json_name = "value"];</code>
   * @return The value.
   */
  java.lang.String getValue();
  /**
   * <code>string value = 5 [json_name = "value"];</code>
   * @return The bytes for value.
   */
  com.google.protobuf.ByteString
      getValueBytes();
}
