// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: assets/queries/assets/query_response.proto

package com.assetmantle.modules.assets.queries.assets;

public final class QueryResponseProto {
  private QueryResponseProto() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_assetmantle_modules_assets_queries_assets_QueryResponse_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_assetmantle_modules_assets_queries_assets_QueryResponse_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n*assets/queries/assets/query_response.p" +
      "roto\022)assetmantle.modules.assets.queries" +
      ".assets\032\024gogoproto/gogo.proto\032\036assets/ma" +
      "ppable/mappable.proto\"X\n\rQueryResponse\022A" +
      "\n\004list\030\001 \003(\0132-.assetmantle.modules.asset" +
      "s.mappable.MappableR\004list:\004\210\240\037\000B\305\002\n-com." +
      "assetmantle.modules.assets.queries.asset" +
      "sB\022QueryResponseProtoP\001Z6github.com/Asse" +
      "tMantle/modules/x/assets/queries/assets\242" +
      "\002\005AMAQA\252\002)Assetmantle.Modules.Assets.Que" +
      "ries.Assets\312\002)Assetmantle\\Modules\\Assets" +
      "\\Queries\\Assets\342\0025Assetmantle\\Modules\\As" +
      "sets\\Queries\\Assets\\GPBMetadata\352\002-Assetm" +
      "antle::Modules::Assets::Queries::Assetsb" +
      "\006proto3"
    };
    descriptor = com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
          com.gogoproto.GogoProto.getDescriptor(),
          com.assetmantle.modules.assets.mappable.MappableProto.getDescriptor(),
        });
    internal_static_assetmantle_modules_assets_queries_assets_QueryResponse_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_assetmantle_modules_assets_queries_assets_QueryResponse_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_assetmantle_modules_assets_queries_assets_QueryResponse_descriptor,
        new java.lang.String[] { "List", });
    com.google.protobuf.ExtensionRegistry registry =
        com.google.protobuf.ExtensionRegistry.newInstance();
    registry.add(com.gogoproto.GogoProto.goprotoGetters);
    com.google.protobuf.Descriptors.FileDescriptor
        .internalUpdateFileDescriptor(descriptor, registry);
    com.gogoproto.GogoProto.getDescriptor();
    com.assetmantle.modules.assets.mappable.MappableProto.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}