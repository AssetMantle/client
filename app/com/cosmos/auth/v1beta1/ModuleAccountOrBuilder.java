// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: cosmos/auth/v1beta1/auth.proto

package com.cosmos.auth.v1beta1;

public interface ModuleAccountOrBuilder extends
    // @@protoc_insertion_point(interface_extends:cosmos.auth.v1beta1.ModuleAccount)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>.cosmos.auth.v1beta1.BaseAccount base_account = 1 [json_name = "baseAccount", (.gogoproto.embed) = true, (.gogoproto.moretags) = "yaml:&#92;"base_account&#92;""];</code>
   * @return Whether the baseAccount field is set.
   */
  boolean hasBaseAccount();
  /**
   * <code>.cosmos.auth.v1beta1.BaseAccount base_account = 1 [json_name = "baseAccount", (.gogoproto.embed) = true, (.gogoproto.moretags) = "yaml:&#92;"base_account&#92;""];</code>
   * @return The baseAccount.
   */
  com.cosmos.auth.v1beta1.BaseAccount getBaseAccount();
  /**
   * <code>.cosmos.auth.v1beta1.BaseAccount base_account = 1 [json_name = "baseAccount", (.gogoproto.embed) = true, (.gogoproto.moretags) = "yaml:&#92;"base_account&#92;""];</code>
   */
  com.cosmos.auth.v1beta1.BaseAccountOrBuilder getBaseAccountOrBuilder();

  /**
   * <code>string name = 2 [json_name = "name"];</code>
   * @return The name.
   */
  java.lang.String getName();
  /**
   * <code>string name = 2 [json_name = "name"];</code>
   * @return The bytes for name.
   */
  com.google.protobuf.ByteString
      getNameBytes();

  /**
   * <code>repeated string permissions = 3 [json_name = "permissions"];</code>
   * @return A list containing the permissions.
   */
  java.util.List<java.lang.String>
      getPermissionsList();
  /**
   * <code>repeated string permissions = 3 [json_name = "permissions"];</code>
   * @return The count of permissions.
   */
  int getPermissionsCount();
  /**
   * <code>repeated string permissions = 3 [json_name = "permissions"];</code>
   * @param index The index of the element to return.
   * @return The permissions at the given index.
   */
  java.lang.String getPermissions(int index);
  /**
   * <code>repeated string permissions = 3 [json_name = "permissions"];</code>
   * @param index The index of the value to return.
   * @return The bytes of the permissions at the given index.
   */
  com.google.protobuf.ByteString
      getPermissionsBytes(int index);
}
