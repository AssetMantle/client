// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: schema/ids/base/propertyID.v1.proto

package com.ids;

public interface PropertyIDOrBuilder extends
    // @@protoc_insertion_point(interface_extends:ids.PropertyID)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>.ids.StringID key_i_d = 1 [json_name = "keyID"];</code>
   * @return Whether the keyID field is set.
   */
  boolean hasKeyID();
  /**
   * <code>.ids.StringID key_i_d = 1 [json_name = "keyID"];</code>
   * @return The keyID.
   */
  com.ids.StringID getKeyID();
  /**
   * <code>.ids.StringID key_i_d = 1 [json_name = "keyID"];</code>
   */
  com.ids.StringIDOrBuilder getKeyIDOrBuilder();

  /**
   * <code>.ids.StringID type_i_d = 2 [json_name = "typeID"];</code>
   * @return Whether the typeID field is set.
   */
  boolean hasTypeID();
  /**
   * <code>.ids.StringID type_i_d = 2 [json_name = "typeID"];</code>
   * @return The typeID.
   */
  com.ids.StringID getTypeID();
  /**
   * <code>.ids.StringID type_i_d = 2 [json_name = "typeID"];</code>
   */
  com.ids.StringIDOrBuilder getTypeIDOrBuilder();
}
