// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: orders/transactions/modify/message.proto

package com.assetmantle.modules.orders.transactions.modify;

public interface MessageOrBuilder extends
    // @@protoc_insertion_point(interface_extends:assetmantle.modules.orders.transactions.modify.Message)
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
   * <code>.assetmantle.schema.ids.base.OrderID order_i_d = 3 [json_name = "orderID"];</code>
   * @return Whether the orderID field is set.
   */
  boolean hasOrderID();
  /**
   * <code>.assetmantle.schema.ids.base.OrderID order_i_d = 3 [json_name = "orderID"];</code>
   * @return The orderID.
   */
  com.assetmantle.schema.ids.base.OrderID getOrderID();
  /**
   * <code>.assetmantle.schema.ids.base.OrderID order_i_d = 3 [json_name = "orderID"];</code>
   */
  com.assetmantle.schema.ids.base.OrderIDOrBuilder getOrderIDOrBuilder();

  /**
   * <code>string maker_split = 4 [json_name = "makerSplit"];</code>
   * @return The makerSplit.
   */
  java.lang.String getMakerSplit();
  /**
   * <code>string maker_split = 4 [json_name = "makerSplit"];</code>
   * @return The bytes for makerSplit.
   */
  com.google.protobuf.ByteString
      getMakerSplitBytes();

  /**
   * <code>string taker_split = 5 [json_name = "takerSplit"];</code>
   * @return The takerSplit.
   */
  java.lang.String getTakerSplit();
  /**
   * <code>string taker_split = 5 [json_name = "takerSplit"];</code>
   * @return The bytes for takerSplit.
   */
  com.google.protobuf.ByteString
      getTakerSplitBytes();

  /**
   * <code>.assetmantle.schema.types.base.Height expires_in = 6 [json_name = "expiresIn"];</code>
   * @return Whether the expiresIn field is set.
   */
  boolean hasExpiresIn();
  /**
   * <code>.assetmantle.schema.types.base.Height expires_in = 6 [json_name = "expiresIn"];</code>
   * @return The expiresIn.
   */
  com.assetmantle.schema.types.base.Height getExpiresIn();
  /**
   * <code>.assetmantle.schema.types.base.Height expires_in = 6 [json_name = "expiresIn"];</code>
   */
  com.assetmantle.schema.types.base.HeightOrBuilder getExpiresInOrBuilder();

  /**
   * <code>.assetmantle.schema.lists.base.PropertyList mutable_meta_properties = 7 [json_name = "mutableMetaProperties"];</code>
   * @return Whether the mutableMetaProperties field is set.
   */
  boolean hasMutableMetaProperties();
  /**
   * <code>.assetmantle.schema.lists.base.PropertyList mutable_meta_properties = 7 [json_name = "mutableMetaProperties"];</code>
   * @return The mutableMetaProperties.
   */
  com.assetmantle.schema.lists.base.PropertyList getMutableMetaProperties();
  /**
   * <code>.assetmantle.schema.lists.base.PropertyList mutable_meta_properties = 7 [json_name = "mutableMetaProperties"];</code>
   */
  com.assetmantle.schema.lists.base.PropertyListOrBuilder getMutableMetaPropertiesOrBuilder();

  /**
   * <code>.assetmantle.schema.lists.base.PropertyList mutable_properties = 8 [json_name = "mutableProperties"];</code>
   * @return Whether the mutableProperties field is set.
   */
  boolean hasMutableProperties();
  /**
   * <code>.assetmantle.schema.lists.base.PropertyList mutable_properties = 8 [json_name = "mutableProperties"];</code>
   * @return The mutableProperties.
   */
  com.assetmantle.schema.lists.base.PropertyList getMutableProperties();
  /**
   * <code>.assetmantle.schema.lists.base.PropertyList mutable_properties = 8 [json_name = "mutableProperties"];</code>
   */
  com.assetmantle.schema.lists.base.PropertyListOrBuilder getMutablePropertiesOrBuilder();
}
