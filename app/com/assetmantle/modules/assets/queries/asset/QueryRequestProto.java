// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: assets/queries/asset/query_request.proto

package com.assetmantle.modules.assets.queries.asset;

public final class QueryRequestProto {
  private QueryRequestProto() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_assetmantle_modules_assets_queries_asset_QueryRequest_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_assetmantle_modules_assets_queries_asset_QueryRequest_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n(assets/queries/asset/query_request.pro" +
      "to\022(assetmantle.modules.assets.queries.a" +
      "sset\032\027ids/base/asset_id.proto\"P\n\014QueryRe" +
      "quest\022@\n\tasset_i_d\030\001 \001(\0132$.assetmantle.s" +
      "chema.ids.base.AssetIDR\007assetIDB\276\002\n,com." +
      "assetmantle.modules.assets.queries.asset" +
      "B\021QueryRequestProtoP\001Z5github.com/AssetM" +
      "antle/modules/x/assets/queries/asset\242\002\005A" +
      "MAQA\252\002(Assetmantle.Modules.Assets.Querie" +
      "s.Asset\312\002(Assetmantle\\Modules\\Assets\\Que" +
      "ries\\Asset\342\0024Assetmantle\\Modules\\Assets\\" +
      "Queries\\Asset\\GPBMetadata\352\002,Assetmantle:" +
      ":Modules::Assets::Queries::Assetb\006proto3"
    };
    descriptor = com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
          com.assetmantle.schema.ids.base.AssetIdProto.getDescriptor(),
        });
    internal_static_assetmantle_modules_assets_queries_asset_QueryRequest_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_assetmantle_modules_assets_queries_asset_QueryRequest_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_assetmantle_modules_assets_queries_asset_QueryRequest_descriptor,
        new java.lang.String[] { "AssetID", });
    com.assetmantle.schema.ids.base.AssetIdProto.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}
