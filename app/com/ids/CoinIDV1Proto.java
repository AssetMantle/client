// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: schema/ids/base/coinID.v1.proto

package com.ids;

public final class CoinIDV1Proto {
  private CoinIDV1Proto() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_ids_CoinID_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_ids_CoinID_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\037schema/ids/base/coinID.v1.proto\022\003ids\032!" +
      "schema/ids/base/stringID.v1.proto\032\024gogop" +
      "roto/gogo.proto\";\n\006CoinID\022+\n\nstring_i_d\030" +
      "\001 \001(\0132\r.ids.StringIDR\010stringID:\004\210\240\037\000Bt\n\007" +
      "com.idsB\rCoinIDV1ProtoP\001Z.github.com/Ass" +
      "etMantle/modules/schema/ids/base\242\002\003IXX\252\002" +
      "\003Ids\312\002\003Ids\342\002\017Ids\\GPBMetadata\352\002\003Idsb\006prot" +
      "o3"
    };
    descriptor = com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
          com.ids.StringIDV1Proto.getDescriptor(),
          com.gogoproto.GogoProto.getDescriptor(),
        });
    internal_static_ids_CoinID_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_ids_CoinID_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_ids_CoinID_descriptor,
        new java.lang.String[] { "StringID", });
    com.google.protobuf.ExtensionRegistry registry =
        com.google.protobuf.ExtensionRegistry.newInstance();
    registry.add(com.gogoproto.GogoProto.goprotoGetters);
    com.google.protobuf.Descriptors.FileDescriptor
        .internalUpdateFileDescriptor(descriptor, registry);
    com.ids.StringIDV1Proto.getDescriptor();
    com.gogoproto.GogoProto.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}
