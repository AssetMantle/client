// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: identities/genesis/genesis.proto

package com.assetmantle.modules.identities.genesis;

public final class GenesisProto {
  private GenesisProto() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_assetmantle_modules_identities_genesis_Genesis_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_assetmantle_modules_identities_genesis_Genesis_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n identities/genesis/genesis.proto\022&asse" +
      "tmantle.modules.identities.genesis\032\024gogo" +
      "proto/gogo.proto\032\"identities/mappable/ma" +
      "ppable.proto\032$parameters/base/parameter_" +
      "list.proto\"\272\001\n\007Genesis\022O\n\tmappables\030\001 \003(" +
      "\01321.assetmantle.modules.identities.mappa" +
      "ble.MappableR\tmappables\022X\n\016parameter_lis" +
      "t\030\002 \001(\01321.assetmantle.schema.parameters." +
      "base.ParameterListR\rparameterList:\004\210\240\037\000B" +
      "\253\002\n*com.assetmantle.modules.identities.g" +
      "enesisB\014GenesisProtoP\001Z3github.com/Asset" +
      "Mantle/modules/x/identities/genesis\242\002\004AM" +
      "IG\252\002&Assetmantle.Modules.Identities.Gene" +
      "sis\312\002&Assetmantle\\Modules\\Identities\\Gen" +
      "esis\342\0022Assetmantle\\Modules\\Identities\\Ge" +
      "nesis\\GPBMetadata\352\002)Assetmantle::Modules" +
      "::Identities::Genesisb\006proto3"
    };
    descriptor = com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
          com.gogoproto.GogoProto.getDescriptor(),
          com.assetmantle.modules.identities.mappable.MappableProto.getDescriptor(),
          com.assetmantle.schema.parameters.base.ParameterListProto.getDescriptor(),
        });
    internal_static_assetmantle_modules_identities_genesis_Genesis_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_assetmantle_modules_identities_genesis_Genesis_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_assetmantle_modules_identities_genesis_Genesis_descriptor,
        new java.lang.String[] { "Mappables", "ParameterList", });
    com.google.protobuf.ExtensionRegistry registry =
        com.google.protobuf.ExtensionRegistry.newInstance();
    registry.add(com.gogoproto.GogoProto.goprotoGetters);
    com.google.protobuf.Descriptors.FileDescriptor
        .internalUpdateFileDescriptor(descriptor, registry);
    com.gogoproto.GogoProto.getDescriptor();
    com.assetmantle.modules.identities.mappable.MappableProto.getDescriptor();
    com.assetmantle.schema.parameters.base.ParameterListProto.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}
