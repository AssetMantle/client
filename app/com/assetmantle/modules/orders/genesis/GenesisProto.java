// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: orders/genesis/genesis.proto

package com.assetmantle.modules.orders.genesis;

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
    internal_static_assetmantle_modules_orders_genesis_Genesis_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_assetmantle_modules_orders_genesis_Genesis_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\034orders/genesis/genesis.proto\022\"assetman" +
      "tle.modules.orders.genesis\032\024gogoproto/go" +
      "go.proto\032\036orders/mappable/mappable.proto" +
      "\032$parameters/base/parameter_list.proto\"\266" +
      "\001\n\007Genesis\022K\n\tmappables\030\001 \003(\0132-.assetman" +
      "tle.modules.orders.mappable.MappableR\tma" +
      "ppables\022X\n\016parameter_list\030\002 \001(\01321.assetm" +
      "antle.schema.parameters.base.ParameterLi" +
      "stR\rparameterList:\004\210\240\037\000B\223\002\n&com.assetman" +
      "tle.modules.orders.genesisB\014GenesisProto" +
      "P\001Z/github.com/AssetMantle/modules/x/ord" +
      "ers/genesis\242\002\004AMOG\252\002\"Assetmantle.Modules" +
      ".Orders.Genesis\312\002\"Assetmantle\\Modules\\Or" +
      "ders\\Genesis\342\002.Assetmantle\\Modules\\Order" +
      "s\\Genesis\\GPBMetadata\352\002%Assetmantle::Mod" +
      "ules::Orders::Genesisb\006proto3"
    };
    descriptor = com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
          com.gogoproto.GogoProto.getDescriptor(),
          com.assetmantle.modules.orders.mappable.MappableProto.getDescriptor(),
          com.assetmantle.schema.parameters.base.ParameterListProto.getDescriptor(),
        });
    internal_static_assetmantle_modules_orders_genesis_Genesis_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_assetmantle_modules_orders_genesis_Genesis_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_assetmantle_modules_orders_genesis_Genesis_descriptor,
        new java.lang.String[] { "Mappables", "ParameterList", });
    com.google.protobuf.ExtensionRegistry registry =
        com.google.protobuf.ExtensionRegistry.newInstance();
    registry.add(com.gogoproto.GogoProto.goprotoGetters);
    com.google.protobuf.Descriptors.FileDescriptor
        .internalUpdateFileDescriptor(descriptor, registry);
    com.gogoproto.GogoProto.getDescriptor();
    com.assetmantle.modules.orders.mappable.MappableProto.getDescriptor();
    com.assetmantle.schema.parameters.base.ParameterListProto.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}