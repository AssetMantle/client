// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: metas/key/key.proto

package com.assetmantle.modules.metas.key;

public final class KeyProto {
  private KeyProto() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_assetmantle_modules_metas_key_Key_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_assetmantle_modules_metas_key_Key_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\023metas/key/key.proto\022\035assetmantle.modul" +
      "es.metas.key\032\026ids/base/data_id.proto\"D\n\003" +
      "Key\022=\n\010data_i_d\030\001 \001(\0132#.assetmantle.sche" +
      "ma.ids.base.DataIDR\006dataIDB\361\001\n!com.asset" +
      "mantle.modules.metas.keyB\010KeyProtoP\001Z*gi" +
      "thub.com/AssetMantle/modules/x/metas/key" +
      "\242\002\004AMMK\252\002\035Assetmantle.Modules.Metas.Key\312" +
      "\002\035Assetmantle\\Modules\\Metas\\Key\342\002)Assetm" +
      "antle\\Modules\\Metas\\Key\\GPBMetadata\352\002 As" +
      "setmantle::Modules::Metas::Keyb\006proto3"
    };
    descriptor = com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
          com.assetmantle.schema.ids.base.DataIdProto.getDescriptor(),
        });
    internal_static_assetmantle_modules_metas_key_Key_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_assetmantle_modules_metas_key_Key_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_assetmantle_modules_metas_key_Key_descriptor,
        new java.lang.String[] { "DataID", });
    com.assetmantle.schema.ids.base.DataIdProto.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}
