// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: modules/assets/internal/queries/asset/service.proto

package com.assets.queries.asset;

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
      "\n3modules/assets/internal/queries/asset/" +
      "service.proto\022\024assets.queries.asset\032\034goo" +
      "gle/api/annotations.proto\0328modules/asset" +
      "s/internal/queries/asset/queryRequest.pr" +
      "oto\0329modules/assets/internal/queries/ass" +
      "et/queryResponse.proto2\202\001\n\007Service\022w\n\006Ha" +
      "ndle\022\".assets.queries.asset.QueryRequest" +
      "\032#.assets.queries.asset.QueryResponse\"$\202" +
      "\323\344\223\002\036\022\034/mantle/assets/v1beta1/assetB\340\001\n\030" +
      "com.assets.queries.assetB\014ServiceProtoP\001" +
      "ZDgithub.com/AssetMantle/modules/modules" +
      "/assets/internal/queries/asset\242\002\003AQA\252\002\024A" +
      "ssets.Queries.Asset\312\002\024Assets\\Queries\\Ass" +
      "et\342\002 Assets\\Queries\\Asset\\GPBMetadata\352\002\026" +
      "Assets::Queries::Assetb\006proto3"
    };
    descriptor = com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
          com.google.api.AnnotationsProto.getDescriptor(),
          com.assets.queries.asset.QueryRequestProto.getDescriptor(),
          com.assets.queries.asset.QueryResponseProto.getDescriptor(),
        });
    com.google.protobuf.ExtensionRegistry registry =
        com.google.protobuf.ExtensionRegistry.newInstance();
    registry.add(com.google.api.AnnotationsProto.http);
    com.google.protobuf.Descriptors.FileDescriptor
        .internalUpdateFileDescriptor(descriptor, registry);
    com.google.api.AnnotationsProto.getDescriptor();
    com.assets.queries.asset.QueryRequestProto.getDescriptor();
    com.assets.queries.asset.QueryResponseProto.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}
