// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: identities/queries/identity/query_response.proto

package com.assetmantle.modules.identities.queries.identity;

public interface QueryResponseOrBuilder extends
    // @@protoc_insertion_point(interface_extends:assetmantle.modules.identities.queries.identity.QueryResponse)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>repeated .assetmantle.modules.identities.mappable.Mappable list = 1 [json_name = "list"];</code>
   */
  java.util.List<com.assetmantle.modules.identities.mappable.Mappable> 
      getListList();
  /**
   * <code>repeated .assetmantle.modules.identities.mappable.Mappable list = 1 [json_name = "list"];</code>
   */
  com.assetmantle.modules.identities.mappable.Mappable getList(int index);
  /**
   * <code>repeated .assetmantle.modules.identities.mappable.Mappable list = 1 [json_name = "list"];</code>
   */
  int getListCount();
  /**
   * <code>repeated .assetmantle.modules.identities.mappable.Mappable list = 1 [json_name = "list"];</code>
   */
  java.util.List<? extends com.assetmantle.modules.identities.mappable.MappableOrBuilder> 
      getListOrBuilderList();
  /**
   * <code>repeated .assetmantle.modules.identities.mappable.Mappable list = 1 [json_name = "list"];</code>
   */
  com.assetmantle.modules.identities.mappable.MappableOrBuilder getListOrBuilder(
      int index);
}
