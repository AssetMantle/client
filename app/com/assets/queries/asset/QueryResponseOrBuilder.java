// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: modules/assets/internal/queries/asset/queryResponse.v1.proto

package com.assets.queries.asset;

public interface QueryResponseOrBuilder extends
    // @@protoc_insertion_point(interface_extends:assets.queries.asset.QueryResponse)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>bool success = 1 [json_name = "success"];</code>
   * @return The success.
   */
  boolean getSuccess();

  /**
   * <code>string error = 2 [json_name = "error"];</code>
   * @return The error.
   */
  java.lang.String getError();
  /**
   * <code>string error = 2 [json_name = "error"];</code>
   * @return The bytes for error.
   */
  com.google.protobuf.ByteString
      getErrorBytes();

  /**
   * <code>repeated .assets.Mappable list = 3 [json_name = "list"];</code>
   */
  java.util.List<com.assets.Mappable> 
      getListList();
  /**
   * <code>repeated .assets.Mappable list = 3 [json_name = "list"];</code>
   */
  com.assets.Mappable getList(int index);
  /**
   * <code>repeated .assets.Mappable list = 3 [json_name = "list"];</code>
   */
  int getListCount();
  /**
   * <code>repeated .assets.Mappable list = 3 [json_name = "list"];</code>
   */
  java.util.List<? extends com.assets.MappableOrBuilder> 
      getListOrBuilderList();
  /**
   * <code>repeated .assets.Mappable list = 3 [json_name = "list"];</code>
   */
  com.assets.MappableOrBuilder getListOrBuilder(
      int index);
}