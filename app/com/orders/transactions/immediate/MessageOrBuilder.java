// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: modules/orders/internal/transactions/immediate/message.v1.proto

package com.orders.transactions.immediate;

public interface MessageOrBuilder extends
    // @@protoc_insertion_point(interface_extends:orders.transactions.immediate.Message)
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
   * <code>.ids.IdentityID from_i_d = 2 [json_name = "fromID"];</code>
   * @return Whether the fromID field is set.
   */
  boolean hasFromID();
  /**
   * <code>.ids.IdentityID from_i_d = 2 [json_name = "fromID"];</code>
   * @return The fromID.
   */
  com.ids.IdentityID getFromID();
  /**
   * <code>.ids.IdentityID from_i_d = 2 [json_name = "fromID"];</code>
   */
  com.ids.IdentityIDOrBuilder getFromIDOrBuilder();

  /**
   * <code>.ids.ClassificationID classification_i_d = 3 [json_name = "classificationID"];</code>
   * @return Whether the classificationID field is set.
   */
  boolean hasClassificationID();
  /**
   * <code>.ids.ClassificationID classification_i_d = 3 [json_name = "classificationID"];</code>
   * @return The classificationID.
   */
  com.ids.ClassificationID getClassificationID();
  /**
   * <code>.ids.ClassificationID classification_i_d = 3 [json_name = "classificationID"];</code>
   */
  com.ids.ClassificationIDOrBuilder getClassificationIDOrBuilder();

  /**
   * <code>.ids.IdentityID taker_i_d = 4 [json_name = "takerID"];</code>
   * @return Whether the takerID field is set.
   */
  boolean hasTakerID();
  /**
   * <code>.ids.IdentityID taker_i_d = 4 [json_name = "takerID"];</code>
   * @return The takerID.
   */
  com.ids.IdentityID getTakerID();
  /**
   * <code>.ids.IdentityID taker_i_d = 4 [json_name = "takerID"];</code>
   */
  com.ids.IdentityIDOrBuilder getTakerIDOrBuilder();

  /**
   * <code>.ids.AnyOwnableID maker_ownable_i_d = 5 [json_name = "makerOwnableID"];</code>
   * @return Whether the makerOwnableID field is set.
   */
  boolean hasMakerOwnableID();
  /**
   * <code>.ids.AnyOwnableID maker_ownable_i_d = 5 [json_name = "makerOwnableID"];</code>
   * @return The makerOwnableID.
   */
  com.ids.AnyOwnableID getMakerOwnableID();
  /**
   * <code>.ids.AnyOwnableID maker_ownable_i_d = 5 [json_name = "makerOwnableID"];</code>
   */
  com.ids.AnyOwnableIDOrBuilder getMakerOwnableIDOrBuilder();

  /**
   * <code>.ids.AnyOwnableID taker_ownable_i_d = 6 [json_name = "takerOwnableID"];</code>
   * @return Whether the takerOwnableID field is set.
   */
  boolean hasTakerOwnableID();
  /**
   * <code>.ids.AnyOwnableID taker_ownable_i_d = 6 [json_name = "takerOwnableID"];</code>
   * @return The takerOwnableID.
   */
  com.ids.AnyOwnableID getTakerOwnableID();
  /**
   * <code>.ids.AnyOwnableID taker_ownable_i_d = 6 [json_name = "takerOwnableID"];</code>
   */
  com.ids.AnyOwnableIDOrBuilder getTakerOwnableIDOrBuilder();

  /**
   * <code>.types.Height expires_in = 7 [json_name = "expiresIn"];</code>
   * @return Whether the expiresIn field is set.
   */
  boolean hasExpiresIn();
  /**
   * <code>.types.Height expires_in = 7 [json_name = "expiresIn"];</code>
   * @return The expiresIn.
   */
  com.types.Height getExpiresIn();
  /**
   * <code>.types.Height expires_in = 7 [json_name = "expiresIn"];</code>
   */
  com.types.HeightOrBuilder getExpiresInOrBuilder();

  /**
   * <code>string maker_ownable_split = 8 [json_name = "makerOwnableSplit"];</code>
   * @return The makerOwnableSplit.
   */
  java.lang.String getMakerOwnableSplit();
  /**
   * <code>string maker_ownable_split = 8 [json_name = "makerOwnableSplit"];</code>
   * @return The bytes for makerOwnableSplit.
   */
  com.google.protobuf.ByteString
      getMakerOwnableSplitBytes();

  /**
   * <code>string taker_ownable_split = 9 [json_name = "takerOwnableSplit"];</code>
   * @return The takerOwnableSplit.
   */
  java.lang.String getTakerOwnableSplit();
  /**
   * <code>string taker_ownable_split = 9 [json_name = "takerOwnableSplit"];</code>
   * @return The bytes for takerOwnableSplit.
   */
  com.google.protobuf.ByteString
      getTakerOwnableSplitBytes();

  /**
   * <code>.lists.PropertyList immutable_meta_properties = 10 [json_name = "immutableMetaProperties"];</code>
   * @return Whether the immutableMetaProperties field is set.
   */
  boolean hasImmutableMetaProperties();
  /**
   * <code>.lists.PropertyList immutable_meta_properties = 10 [json_name = "immutableMetaProperties"];</code>
   * @return The immutableMetaProperties.
   */
  com.lists.PropertyList getImmutableMetaProperties();
  /**
   * <code>.lists.PropertyList immutable_meta_properties = 10 [json_name = "immutableMetaProperties"];</code>
   */
  com.lists.PropertyListOrBuilder getImmutableMetaPropertiesOrBuilder();

  /**
   * <code>.lists.PropertyList immutable_properties = 11 [json_name = "immutableProperties"];</code>
   * @return Whether the immutableProperties field is set.
   */
  boolean hasImmutableProperties();
  /**
   * <code>.lists.PropertyList immutable_properties = 11 [json_name = "immutableProperties"];</code>
   * @return The immutableProperties.
   */
  com.lists.PropertyList getImmutableProperties();
  /**
   * <code>.lists.PropertyList immutable_properties = 11 [json_name = "immutableProperties"];</code>
   */
  com.lists.PropertyListOrBuilder getImmutablePropertiesOrBuilder();

  /**
   * <code>.lists.PropertyList mutable_meta_properties = 12 [json_name = "mutableMetaProperties"];</code>
   * @return Whether the mutableMetaProperties field is set.
   */
  boolean hasMutableMetaProperties();
  /**
   * <code>.lists.PropertyList mutable_meta_properties = 12 [json_name = "mutableMetaProperties"];</code>
   * @return The mutableMetaProperties.
   */
  com.lists.PropertyList getMutableMetaProperties();
  /**
   * <code>.lists.PropertyList mutable_meta_properties = 12 [json_name = "mutableMetaProperties"];</code>
   */
  com.lists.PropertyListOrBuilder getMutableMetaPropertiesOrBuilder();

  /**
   * <code>.lists.PropertyList mutable_properties = 13 [json_name = "mutableProperties"];</code>
   * @return Whether the mutableProperties field is set.
   */
  boolean hasMutableProperties();
  /**
   * <code>.lists.PropertyList mutable_properties = 13 [json_name = "mutableProperties"];</code>
   * @return The mutableProperties.
   */
  com.lists.PropertyList getMutableProperties();
  /**
   * <code>.lists.PropertyList mutable_properties = 13 [json_name = "mutableProperties"];</code>
   */
  com.lists.PropertyListOrBuilder getMutablePropertiesOrBuilder();
}
