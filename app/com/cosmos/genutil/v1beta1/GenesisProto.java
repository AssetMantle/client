// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: cosmos/genutil/v1beta1/genesis.proto

package com.cosmos.genutil.v1beta1;

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
    internal_static_cosmos_genutil_v1beta1_GenesisState_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_cosmos_genutil_v1beta1_GenesisState_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n$cosmos/genutil/v1beta1/genesis.proto\022\026" +
      "cosmos.genutil.v1beta1\032\024gogoproto/gogo.p" +
      "roto\"`\n\014GenesisState\022P\n\007gen_txs\030\001 \003(\014B7\352" +
      "\336\037\006gentxs\362\336\037\ryaml:\"gentxs\"\372\336\037\030encoding/j" +
      "son.RawMessageR\006genTxsB\352\001\n\032com.cosmos.ge" +
      "nutil.v1beta1B\014GenesisProtoP\001ZDgithub.co" +
      "m/AssetMantle/modules/cosmos/genutil/v1b" +
      "eta1;genutilv1beta1\242\002\003CGX\252\002\026Cosmos.Genut" +
      "il.V1beta1\312\002\026Cosmos\\Genutil\\V1beta1\342\002\"Co" +
      "smos\\Genutil\\V1beta1\\GPBMetadata\352\002\030Cosmo" +
      "s::Genutil::V1beta1b\006proto3"
    };
    descriptor = com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
          com.gogoproto.GogoProto.getDescriptor(),
        });
    internal_static_cosmos_genutil_v1beta1_GenesisState_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_cosmos_genutil_v1beta1_GenesisState_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_cosmos_genutil_v1beta1_GenesisState_descriptor,
        new java.lang.String[] { "GenTxs", });
    com.google.protobuf.ExtensionRegistry registry =
        com.google.protobuf.ExtensionRegistry.newInstance();
    registry.add(com.gogoproto.GogoProto.casttype);
    registry.add(com.gogoproto.GogoProto.jsontag);
    registry.add(com.gogoproto.GogoProto.moretags);
    com.google.protobuf.Descriptors.FileDescriptor
        .internalUpdateFileDescriptor(descriptor, registry);
    com.gogoproto.GogoProto.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}
