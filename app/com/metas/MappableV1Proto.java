// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: modules/metas/internal/mappable/mappable.v1.proto

package com.metas;

public final class MappableV1Proto {
  private MappableV1Proto() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_metas_Mappable_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_metas_Mappable_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n1modules/metas/internal/mappable/mappab" +
      "le.v1.proto\022\005metas\032!schema/data/base/any" +
      "Data.v1.proto\032\024gogoproto/gogo.proto\"3\n\010M" +
      "appable\022!\n\004data\030\001 \001(\0132\r.data.AnyDataR\004da" +
      "ta:\004\210\240\037\000B\220\001\n\tcom.metasB\017MappableV1ProtoP" +
      "\001Z>github.com/AssetMantle/modules/module" +
      "s/metas/internal/mappable\242\002\003MXX\252\002\005Metas\312" +
      "\002\005Metas\342\002\021Metas\\GPBMetadata\352\002\005Metasb\006pro" +
      "to3"
    };
    descriptor = com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
          com.data.AnyDataV1Proto.getDescriptor(),
          com.gogoproto.GogoProto.getDescriptor(),
        });
    internal_static_metas_Mappable_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_metas_Mappable_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_metas_Mappable_descriptor,
        new java.lang.String[] { "Data", });
    com.google.protobuf.ExtensionRegistry registry =
        com.google.protobuf.ExtensionRegistry.newInstance();
    registry.add(com.gogoproto.GogoProto.goprotoGetters);
    com.google.protobuf.Descriptors.FileDescriptor
        .internalUpdateFileDescriptor(descriptor, registry);
    com.data.AnyDataV1Proto.getDescriptor();
    com.gogoproto.GogoProto.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}
