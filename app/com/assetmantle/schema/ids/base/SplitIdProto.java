// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: ids/base/split_id.proto

package com.assetmantle.schema.ids.base;

public final class SplitIdProto {
  private SplitIdProto() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_assetmantle_schema_ids_base_SplitID_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_assetmantle_schema_ids_base_SplitID_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\027ids/base/split_id.proto\022\033assetmantle.s" +
      "chema.ids.base\032\032ids/base/identity_id.pro" +
      "to\032\035ids/base/any_ownable_id.proto\032\024gogop" +
      "roto/gogo.proto\"\237\001\n\007SplitID\022C\n\towner_i_d" +
      "\030\001 \001(\0132\'.assetmantle.schema.ids.base.Ide" +
      "ntityIDR\007ownerID\022I\n\013ownable_i_d\030\002 \001(\0132)." +
      "assetmantle.schema.ids.base.AnyOwnableID" +
      "R\townableID:\004\210\240\037\000B\352\001\n\037com.assetmantle.sc" +
      "hema.ids.baseB\014SplitIdProtoP\001Z)github.co" +
      "m/AssetMantle/schema/go/ids/base\242\002\004ASIB\252" +
      "\002\033Assetmantle.Schema.Ids.Base\312\002\033Assetman" +
      "tle\\Schema\\Ids\\Base\342\002\'Assetmantle\\Schema" +
      "\\Ids\\Base\\GPBMetadata\352\002\036Assetmantle::Sch" +
      "ema::Ids::Baseb\006proto3"
    };
    descriptor = com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
          com.assetmantle.schema.ids.base.IdentityIdProto.getDescriptor(),
          com.assetmantle.schema.ids.base.AnyOwnableIdProto.getDescriptor(),
          com.gogoproto.GogoProto.getDescriptor(),
        });
    internal_static_assetmantle_schema_ids_base_SplitID_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_assetmantle_schema_ids_base_SplitID_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_assetmantle_schema_ids_base_SplitID_descriptor,
        new java.lang.String[] { "OwnerID", "OwnableID", });
    com.google.protobuf.ExtensionRegistry registry =
        com.google.protobuf.ExtensionRegistry.newInstance();
    registry.add(com.gogoproto.GogoProto.goprotoGetters);
    com.google.protobuf.Descriptors.FileDescriptor
        .internalUpdateFileDescriptor(descriptor, registry);
    com.assetmantle.schema.ids.base.IdentityIdProto.getDescriptor();
    com.assetmantle.schema.ids.base.AnyOwnableIdProto.getDescriptor();
    com.gogoproto.GogoProto.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}