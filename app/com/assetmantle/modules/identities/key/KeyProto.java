// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: identities/key/key.proto

package com.assetmantle.modules.identities.key;

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
    internal_static_assetmantle_modules_identities_key_Key_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_assetmantle_modules_identities_key_Key_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\030identities/key/key.proto\022\"assetmantle." +
      "modules.identities.key\032\032ids/base/identit" +
      "y_id.proto\"P\n\003Key\022I\n\014identity_i_d\030\001 \001(\0132" +
      "\'.assetmantle.schema.ids.base.IdentityID" +
      "R\nidentityIDB\217\002\n&com.assetmantle.modules" +
      ".identities.keyB\010KeyProtoP\001Z/github.com/" +
      "AssetMantle/modules/x/identities/key\242\002\004A" +
      "MIK\252\002\"Assetmantle.Modules.Identities.Key" +
      "\312\002\"Assetmantle\\Modules\\Identities\\Key\342\002." +
      "Assetmantle\\Modules\\Identities\\Key\\GPBMe" +
      "tadata\352\002%Assetmantle::Modules::Identitie" +
      "s::Keyb\006proto3"
    };
    descriptor = com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
          com.assetmantle.schema.ids.base.IdentityIdProto.getDescriptor(),
        });
    internal_static_assetmantle_modules_identities_key_Key_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_assetmantle_modules_identities_key_Key_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_assetmantle_modules_identities_key_Key_descriptor,
        new java.lang.String[] { "IdentityID", });
    com.assetmantle.schema.ids.base.IdentityIdProto.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}
