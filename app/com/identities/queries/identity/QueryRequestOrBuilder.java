// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: modules/identities/internal/queries/identity/queryRequest.proto

package com.identities.queries.identity;

public interface QueryRequestOrBuilder extends
    // @@protoc_insertion_point(interface_extends:identities.queries.identity.QueryRequest)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>.ids.IdentityID identity_i_d = 1 [json_name = "identityID"];</code>
   * @return Whether the identityID field is set.
   */
  boolean hasIdentityID();
  /**
   * <code>.ids.IdentityID identity_i_d = 1 [json_name = "identityID"];</code>
   * @return The identityID.
   */
  com.ids.IdentityID getIdentityID();
  /**
   * <code>.ids.IdentityID identity_i_d = 1 [json_name = "identityID"];</code>
   */
  com.ids.IdentityIDOrBuilder getIdentityIDOrBuilder();
}
