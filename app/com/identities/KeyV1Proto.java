// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: modules/identities/internal/key/key.v1.proto

package com.identities;

public final class KeyV1Proto {
  private KeyV1Proto() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_identities_Key_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_identities_Key_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n,modules/identities/internal/key/key.v1" +
      ".proto\022\nidentities\032#schema/ids/base/iden" +
      "tityID.v1.proto\"8\n\003Key\0221\n\014identity_i_d\030\001" +
      " \001(\0132\017.ids.IdentityIDR\nidentityIDB\244\001\n\016co" +
      "m.identitiesB\nKeyV1ProtoP\001Z>github.com/A" +
      "ssetMantle/modules/modules/identities/in" +
      "ternal/key\242\002\003IXX\252\002\nIdentities\312\002\nIdentiti" +
      "es\342\002\026Identities\\GPBMetadata\352\002\nIdentities" +
      "b\006proto3"
    };
    descriptor = com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
          com.ids.IdentityIDV1Proto.getDescriptor(),
        });
    internal_static_identities_Key_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_identities_Key_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_identities_Key_descriptor,
        new java.lang.String[] { "IdentityID", });
    com.ids.IdentityIDV1Proto.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}