// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: modules/maintainers/internal/genesis/genesis.v1.proto

package com.maintainers;

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
    internal_static_maintainers_Genesis_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_maintainers_Genesis_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n5modules/maintainers/internal/genesis/g" +
      "enesis.v1.proto\022\013maintainers\032\024gogoproto/" +
      "gogo.proto\0327modules/maintainers/internal" +
      "/mappable/mappable.v1.proto\032)schema/para" +
      "meters/base/parameter.v1.proto\"{\n\007Genesi" +
      "s\0223\n\tmappables\030\001 \003(\0132\025.maintainers.Mappa" +
      "bleR\tmappables\0225\n\nparameters\030\002 \003(\0132\025.par" +
      "ameters.ParameterR\nparameters:\004\210\240\037\000B\262\001\n\017" +
      "com.maintainersB\016GenesisV1ProtoP\001ZCgithu" +
      "b.com/AssetMantle/modules/modules/mainta" +
      "iners/internal/genesis\242\002\003MXX\252\002\013Maintaine" +
      "rs\312\002\013Maintainers\342\002\027Maintainers\\GPBMetada" +
      "ta\352\002\013Maintainersb\006proto3"
    };
    descriptor = com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
          com.gogoproto.GogoProto.getDescriptor(),
          com.maintainers.MappableV1Proto.getDescriptor(),
          com.parameters.ParameterV1Proto.getDescriptor(),
        });
    internal_static_maintainers_Genesis_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_maintainers_Genesis_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_maintainers_Genesis_descriptor,
        new java.lang.String[] { "Mappables", "Parameters", });
    com.google.protobuf.ExtensionRegistry registry =
        com.google.protobuf.ExtensionRegistry.newInstance();
    registry.add(com.gogoproto.GogoProto.goprotoGetters);
    com.google.protobuf.Descriptors.FileDescriptor
        .internalUpdateFileDescriptor(descriptor, registry);
    com.gogoproto.GogoProto.getDescriptor();
    com.maintainers.MappableV1Proto.getDescriptor();
    com.parameters.ParameterV1Proto.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}