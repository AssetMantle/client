// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: schema/data/base/decData.v1.proto

package com.data;

public final class DecDataV1Proto {
  private DecDataV1Proto() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_data_DecData_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_data_DecData_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n!schema/data/base/decData.v1.proto\022\004dat" +
      "a\032\024gogoproto/gogo.proto\"%\n\007DecData\022\024\n\005va" +
      "lue\030\001 \001(\tR\005value:\004\210\240\037\000B{\n\010com.dataB\016DecD" +
      "ataV1ProtoP\001Z/github.com/AssetMantle/mod" +
      "ules/schema/data/base\242\002\003DXX\252\002\004Data\312\002\004Dat" +
      "a\342\002\020Data\\GPBMetadata\352\002\004Datab\006proto3"
    };
    descriptor = com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
          com.gogoproto.GogoProto.getDescriptor(),
        });
    internal_static_data_DecData_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_data_DecData_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_data_DecData_descriptor,
        new java.lang.String[] { "Value", });
    com.google.protobuf.ExtensionRegistry registry =
        com.google.protobuf.ExtensionRegistry.newInstance();
    registry.add(com.gogoproto.GogoProto.goprotoGetters);
    com.google.protobuf.Descriptors.FileDescriptor
        .internalUpdateFileDescriptor(descriptor, registry);
    com.gogoproto.GogoProto.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}
