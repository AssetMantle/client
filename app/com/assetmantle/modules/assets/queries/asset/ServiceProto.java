// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: assets/queries/asset/service.proto

package com.assetmantle.modules.assets.queries.asset;

public final class ServiceProto {
  private ServiceProto() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\"assets/queries/asset/service.proto\022(as" +
      "setmantle.modules.assets.queries.asset\032\034" +
      "google/api/annotations.proto\032(assets/que" +
      "ries/asset/query_request.proto\032)assets/q" +
      "ueries/asset/query_response.proto2\253\001\n\007Se" +
      "rvice\022\237\001\n\006Handle\0226.assetmantle.modules.a" +
      "ssets.queries.asset.QueryRequest\0327.asset" +
      "mantle.modules.assets.queries.asset.Quer" +
      "yResponse\"$\202\323\344\223\002\036\022\034/mantle/assets/v1beta" +
      "1/assetB\271\002\n,com.assetmantle.modules.asse" +
      "ts.queries.assetB\014ServiceProtoP\001Z5github" +
      ".com/AssetMantle/modules/x/assets/querie" +
      "s/asset\242\002\005AMAQA\252\002(Assetmantle.Modules.As" +
      "sets.Queries.Asset\312\002(Assetmantle\\Modules" +
      "\\Assets\\Queries\\Asset\342\0024Assetmantle\\Modu" +
      "les\\Assets\\Queries\\Asset\\GPBMetadata\352\002,A" +
      "ssetmantle::Modules::Assets::Queries::As" +
      "setb\006proto3"
    };
    descriptor = com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
          com.google.api.AnnotationsProto.getDescriptor(),
          com.assetmantle.modules.assets.queries.asset.QueryRequestProto.getDescriptor(),
          com.assetmantle.modules.assets.queries.asset.QueryResponseProto.getDescriptor(),
        });
    com.google.protobuf.ExtensionRegistry registry =
        com.google.protobuf.ExtensionRegistry.newInstance();
    registry.add(com.google.api.AnnotationsProto.http);
    com.google.protobuf.Descriptors.FileDescriptor
        .internalUpdateFileDescriptor(descriptor, registry);
    com.google.api.AnnotationsProto.getDescriptor();
    com.assetmantle.modules.assets.queries.asset.QueryRequestProto.getDescriptor();
    com.assetmantle.modules.assets.queries.asset.QueryResponseProto.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}
