// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: cosmos/base/store/v1beta1/commit_info.proto

package com.cosmos.base.store.v1beta1;

public interface CommitInfoOrBuilder extends
    // @@protoc_insertion_point(interface_extends:cosmos.base.store.v1beta1.CommitInfo)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>int64 version = 1 [json_name = "version"];</code>
   * @return The version.
   */
  long getVersion();

  /**
   * <code>repeated .cosmos.base.store.v1beta1.StoreInfo store_infos = 2 [json_name = "storeInfos", (.gogoproto.nullable) = false];</code>
   */
  java.util.List<com.cosmos.base.store.v1beta1.StoreInfo> 
      getStoreInfosList();
  /**
   * <code>repeated .cosmos.base.store.v1beta1.StoreInfo store_infos = 2 [json_name = "storeInfos", (.gogoproto.nullable) = false];</code>
   */
  com.cosmos.base.store.v1beta1.StoreInfo getStoreInfos(int index);
  /**
   * <code>repeated .cosmos.base.store.v1beta1.StoreInfo store_infos = 2 [json_name = "storeInfos", (.gogoproto.nullable) = false];</code>
   */
  int getStoreInfosCount();
  /**
   * <code>repeated .cosmos.base.store.v1beta1.StoreInfo store_infos = 2 [json_name = "storeInfos", (.gogoproto.nullable) = false];</code>
   */
  java.util.List<? extends com.cosmos.base.store.v1beta1.StoreInfoOrBuilder> 
      getStoreInfosOrBuilderList();
  /**
   * <code>repeated .cosmos.base.store.v1beta1.StoreInfo store_infos = 2 [json_name = "storeInfos", (.gogoproto.nullable) = false];</code>
   */
  com.cosmos.base.store.v1beta1.StoreInfoOrBuilder getStoreInfosOrBuilder(
      int index);
}
