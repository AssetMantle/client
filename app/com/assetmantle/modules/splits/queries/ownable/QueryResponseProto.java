// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: splits/queries/ownable/query_response.proto

package com.assetmantle.modules.splits.queries.ownable;

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
    internal_static_assetmantle_modules_splits_queries_ownable_QueryResponse_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_assetmantle_modules_splits_queries_ownable_QueryResponse_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n+splits/queries/ownable/query_response." +
      "proto\022*assetmantle.modules.splits.querie" +
      "s.ownable\032\024gogoproto/gogo.proto\"[\n\rQuery" +
      "Response\022\030\n\007success\030\001 \001(\010R\007success\022\024\n\005er" +
      "ror\030\002 \001(\tR\005error\022\024\n\005value\030\003 \001(\tR\005value:\004" +
      "\210\240\037\000B\313\002\n.com.assetmantle.modules.splits." +
      "queries.ownableB\022QueryResponseProtoP\001Z7g" +
      "ithub.com/AssetMantle/modules/x/splits/q" +
      "ueries/ownable\242\002\005AMSQO\252\002*Assetmantle.Mod" +
      "ules.Splits.Queries.Ownable\312\002*Assetmantl" +
      "e\\Modules\\Splits\\Queries\\Ownable\342\0026Asset" +
      "mantle\\Modules\\Splits\\Queries\\Ownable\\GP" +
      "BMetadata\352\002.Assetmantle::Modules::Splits" +
      "::Queries::Ownableb\006proto3"
    };
    descriptor = com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
          com.gogoproto.GogoProto.getDescriptor(),
        });
    internal_static_assetmantle_modules_splits_queries_ownable_QueryResponse_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_assetmantle_modules_splits_queries_ownable_QueryResponse_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_assetmantle_modules_splits_queries_ownable_QueryResponse_descriptor,
        new java.lang.String[] { "Success", "Error", "Value", });
    com.google.protobuf.ExtensionRegistry registry =
        com.google.protobuf.ExtensionRegistry.newInstance();
    registry.add(com.gogoproto.GogoProto.goprotoGetters);
    com.google.protobuf.Descriptors.FileDescriptor
        .internalUpdateFileDescriptor(descriptor, registry);
    com.gogoproto.GogoProto.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}