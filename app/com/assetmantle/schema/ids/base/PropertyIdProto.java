// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: ids/base/property_id.proto

package com.assetmantle.schema.ids.base;

public final class PropertyIdProto {
  private PropertyIdProto() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_assetmantle_schema_ids_base_PropertyID_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_assetmantle_schema_ids_base_PropertyID_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\032ids/base/property_id.proto\022\033assetmantl" +
      "e.schema.ids.base\032\030ids/base/string_id.pr" +
      "oto\032\024gogoproto/gogo.proto\"\222\001\n\nPropertyID" +
      "\022=\n\007key_i_d\030\001 \001(\0132%.assetmantle.schema.i" +
      "ds.base.StringIDR\005keyID\022?\n\010type_i_d\030\002 \001(" +
      "\0132%.assetmantle.schema.ids.base.StringID" +
      "R\006typeID:\004\210\240\037\000B\355\001\n\037com.assetmantle.schem" +
      "a.ids.baseB\017PropertyIdProtoP\001Z)github.co" +
      "m/AssetMantle/schema/go/ids/base\242\002\004ASIB\252" +
      "\002\033Assetmantle.Schema.Ids.Base\312\002\033Assetman" +
      "tle\\Schema\\Ids\\Base\342\002\'Assetmantle\\Schema" +
      "\\Ids\\Base\\GPBMetadata\352\002\036Assetmantle::Sch" +
      "ema::Ids::Baseb\006proto3"
    };
    descriptor = com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
          com.assetmantle.schema.ids.base.StringIdProto.getDescriptor(),
          com.gogoproto.GogoProto.getDescriptor(),
        });
    internal_static_assetmantle_schema_ids_base_PropertyID_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_assetmantle_schema_ids_base_PropertyID_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_assetmantle_schema_ids_base_PropertyID_descriptor,
        new java.lang.String[] { "KeyID", "TypeID", });
    com.google.protobuf.ExtensionRegistry registry =
        com.google.protobuf.ExtensionRegistry.newInstance();
    registry.add(com.gogoproto.GogoProto.goprotoGetters);
    com.google.protobuf.Descriptors.FileDescriptor
        .internalUpdateFileDescriptor(descriptor, registry);
    com.assetmantle.schema.ids.base.StringIdProto.getDescriptor();
    com.gogoproto.GogoProto.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}