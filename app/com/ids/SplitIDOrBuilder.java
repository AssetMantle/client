// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: schema/ids/base/splitID.proto

package com.ids;

public interface SplitIDOrBuilder extends
    // @@protoc_insertion_point(interface_extends:ids.SplitID)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>.ids.IdentityID owner_i_d = 1 [json_name = "ownerID"];</code>
   * @return Whether the ownerID field is set.
   */
  boolean hasOwnerID();
  /**
   * <code>.ids.IdentityID owner_i_d = 1 [json_name = "ownerID"];</code>
   * @return The ownerID.
   */
  com.ids.IdentityID getOwnerID();
  /**
   * <code>.ids.IdentityID owner_i_d = 1 [json_name = "ownerID"];</code>
   */
  com.ids.IdentityIDOrBuilder getOwnerIDOrBuilder();

  /**
   * <code>.ids.AnyOwnableID ownable_i_d = 2 [json_name = "ownableID"];</code>
   * @return Whether the ownableID field is set.
   */
  boolean hasOwnableID();
  /**
   * <code>.ids.AnyOwnableID ownable_i_d = 2 [json_name = "ownableID"];</code>
   * @return The ownableID.
   */
  com.ids.AnyOwnableID getOwnableID();
  /**
   * <code>.ids.AnyOwnableID ownable_i_d = 2 [json_name = "ownableID"];</code>
   */
  com.ids.AnyOwnableIDOrBuilder getOwnableIDOrBuilder();
}
