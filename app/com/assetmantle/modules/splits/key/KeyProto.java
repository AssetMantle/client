// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: splits/key/key.proto

package com.assetmantle.modules.splits.key;

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
    internal_static_assetmantle_modules_splits_key_Key_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_assetmantle_modules_splits_key_Key_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\024splits/key/key.proto\022\036assetmantle.modu" +
      "les.splits.key\032\027ids/base/split_id.proto\"" +
      "G\n\003Key\022@\n\tsplit_i_d\030\001 \001(\0132$.assetmantle." +
      "schema.ids.base.SplitIDR\007splitIDB\367\001\n\"com" +
      ".assetmantle.modules.splits.keyB\010KeyProt" +
      "oP\001Z+github.com/AssetMantle/modules/x/sp" +
      "lits/key\242\002\004AMSK\252\002\036Assetmantle.Modules.Sp" +
      "lits.Key\312\002\036Assetmantle\\Modules\\Splits\\Ke" +
      "y\342\002*Assetmantle\\Modules\\Splits\\Key\\GPBMe" +
      "tadata\352\002!Assetmantle::Modules::Splits::K" +
      "eyb\006proto3"
    };
    descriptor = com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
          com.assetmantle.schema.ids.base.SplitIdProto.getDescriptor(),
        });
    internal_static_assetmantle_modules_splits_key_Key_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_assetmantle_modules_splits_key_Key_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_assetmantle_modules_splits_key_Key_descriptor,
        new java.lang.String[] { "SplitID", });
    com.assetmantle.schema.ids.base.SplitIdProto.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}
