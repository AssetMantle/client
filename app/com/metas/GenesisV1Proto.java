// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: modules/metas/internal/genesis/genesis.v1.proto

package com.metas;

public final class GenesisV1Proto {
  private GenesisV1Proto() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_metas_Genesis_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_metas_Genesis_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n/modules/metas/internal/genesis/genesis" +
      ".v1.proto\022\005metas\032\024gogoproto/gogo.proto\0321" +
      "modules/metas/internal/mappable/mappable" +
      ".v1.proto\032-schema/parameters/base/parame" +
      "terList.v1.proto\"\177\n\007Genesis\022-\n\tmappables" +
      "\030\001 \003(\0132\017.metas.MappableR\tmappables\022?\n\rpa" +
      "rameterList\030\002 \001(\0132\031.parameters.Parameter" +
      "ListR\rparameterList:\004\210\240\037\000B\216\001\n\tcom.metasB" +
      "\016GenesisV1ProtoP\001Z=github.com/AssetMantl" +
      "e/modules/modules/metas/internal/genesis" +
      "\242\002\003MXX\252\002\005Metas\312\002\005Metas\342\002\021Metas\\GPBMetada" +
      "ta\352\002\005Metasb\006proto3"
    };
    descriptor = com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
          com.gogoproto.GogoProto.getDescriptor(),
          com.metas.MappableV1Proto.getDescriptor(),
          com.parameters.ParameterListV1Proto.getDescriptor(),
        });
    internal_static_metas_Genesis_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_metas_Genesis_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_metas_Genesis_descriptor,
        new java.lang.String[] { "Mappables", "ParameterList", });
    com.google.protobuf.ExtensionRegistry registry =
        com.google.protobuf.ExtensionRegistry.newInstance();
    registry.add(com.gogoproto.GogoProto.goprotoGetters);
    com.google.protobuf.Descriptors.FileDescriptor
        .internalUpdateFileDescriptor(descriptor, registry);
    com.gogoproto.GogoProto.getDescriptor();
    com.metas.MappableV1Proto.getDescriptor();
    com.parameters.ParameterListV1Proto.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}
