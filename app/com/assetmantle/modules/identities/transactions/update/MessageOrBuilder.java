// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: identities/transactions/update/message.proto

package com.assetmantle.modules.identities.transactions.update;

public interface MessageOrBuilder extends
    // @@protoc_insertion_point(interface_extends:assetmantle.modules.identities.transactions.update.Message)
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
   * <code>.assetmantle.schema.ids.base.IdentityID identity_i_d = 3 [json_name = "identityID"];</code>
   * @return Whether the identityID field is set.
   */
  boolean hasIdentityID();
  /**
   * <code>.assetmantle.schema.ids.base.IdentityID identity_i_d = 3 [json_name = "identityID"];</code>
   * @return The identityID.
   */
  com.assetmantle.schema.ids.base.IdentityID getIdentityID();
  /**
   * <code>.assetmantle.schema.ids.base.IdentityID identity_i_d = 3 [json_name = "identityID"];</code>
   */
  com.assetmantle.schema.ids.base.IdentityIDOrBuilder getIdentityIDOrBuilder();

  /**
   * <code>.assetmantle.schema.lists.base.PropertyList mutable_meta_properties = 4 [json_name = "mutableMetaProperties"];</code>
   * @return Whether the mutableMetaProperties field is set.
   */
  boolean hasMutableMetaProperties();
  /**
   * <code>.assetmantle.schema.lists.base.PropertyList mutable_meta_properties = 4 [json_name = "mutableMetaProperties"];</code>
   * @return The mutableMetaProperties.
   */
  com.assetmantle.schema.lists.base.PropertyList getMutableMetaProperties();
  /**
   * <code>.assetmantle.schema.lists.base.PropertyList mutable_meta_properties = 4 [json_name = "mutableMetaProperties"];</code>
   */
  com.assetmantle.schema.lists.base.PropertyListOrBuilder getMutableMetaPropertiesOrBuilder();

  /**
   * <code>.assetmantle.schema.lists.base.PropertyList mutable_properties = 5 [json_name = "mutableProperties"];</code>
   * @return Whether the mutableProperties field is set.
   */
  boolean hasMutableProperties();
  /**
   * <code>.assetmantle.schema.lists.base.PropertyList mutable_properties = 5 [json_name = "mutableProperties"];</code>
   * @return The mutableProperties.
   */
  com.assetmantle.schema.lists.base.PropertyList getMutableProperties();
  /**
   * <code>.assetmantle.schema.lists.base.PropertyList mutable_properties = 5 [json_name = "mutableProperties"];</code>
   */
  com.assetmantle.schema.lists.base.PropertyListOrBuilder getMutablePropertiesOrBuilder();
}
