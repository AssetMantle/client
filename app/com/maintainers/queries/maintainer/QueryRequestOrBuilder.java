// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: modules/maintainers/internal/queries/maintainer/queryRequest.v1.proto

package com.maintainers.queries.maintainer;

public interface QueryRequestOrBuilder extends
    // @@protoc_insertion_point(interface_extends:maintainers.queries.maintainer.QueryRequest)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>.ids.MaintainerID maintainer_i_d = 1 [json_name = "maintainerID"];</code>
   * @return Whether the maintainerID field is set.
   */
  boolean hasMaintainerID();
  /**
   * <code>.ids.MaintainerID maintainer_i_d = 1 [json_name = "maintainerID"];</code>
   * @return The maintainerID.
   */
  com.ids.MaintainerID getMaintainerID();
  /**
   * <code>.ids.MaintainerID maintainer_i_d = 1 [json_name = "maintainerID"];</code>
   */
  com.ids.MaintainerIDOrBuilder getMaintainerIDOrBuilder();
}
