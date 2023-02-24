// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: ibc/core/client/v1/genesis.proto

package com.ibc.core.client.v1;

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
    internal_static_ibc_core_client_v1_GenesisState_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_ibc_core_client_v1_GenesisState_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_ibc_core_client_v1_GenesisMetadata_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_ibc_core_client_v1_GenesisMetadata_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_ibc_core_client_v1_IdentifiedGenesisMetadata_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_ibc_core_client_v1_IdentifiedGenesisMetadata_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n ibc/core/client/v1/genesis.proto\022\022ibc." +
      "core.client.v1\032\037ibc/core/client/v1/clien" +
      "t.proto\032\024gogoproto/gogo.proto\"\330\004\n\014Genesi" +
      "sState\022c\n\007clients\030\001 \003(\0132).ibc.core.clien" +
      "t.v1.IdentifiedClientStateB\036\310\336\037\000\252\337\037\026Iden" +
      "tifiedClientStatesR\007clients\022\222\001\n\021clients_" +
      "consensus\030\002 \003(\0132).ibc.core.client.v1.Cli" +
      "entConsensusStatesB:\310\336\037\000\362\336\037\030yaml:\"client" +
      "s_consensus\"\252\337\037\026ClientsConsensusStatesR\020" +
      "clientsConsensus\022y\n\020clients_metadata\030\003 \003" +
      "(\0132-.ibc.core.client.v1.IdentifiedGenesi" +
      "sMetadataB\037\310\336\037\000\362\336\037\027yaml:\"clients_metadat" +
      "a\"R\017clientsMetadata\0228\n\006params\030\004 \001(\0132\032.ib" +
      "c.core.client.v1.ParamsB\004\310\336\037\000R\006params\022F\n" +
      "\020create_localhost\030\005 \001(\010B\033\362\336\037\027yaml:\"creat" +
      "e_localhost\"R\017createLocalhost\022Q\n\024next_cl" +
      "ient_sequence\030\006 \001(\004B\037\362\336\037\033yaml:\"next_clie" +
      "nt_sequence\"R\022nextClientSequence\"?\n\017Gene" +
      "sisMetadata\022\020\n\003key\030\001 \001(\014R\003key\022\024\n\005value\030\002" +
      " \001(\014R\005value:\004\210\240\037\000\"\274\001\n\031IdentifiedGenesisM" +
      "etadata\0221\n\tclient_id\030\001 \001(\tB\024\362\336\037\020yaml:\"cl" +
      "ient_id\"R\010clientId\022l\n\017client_metadata\030\002 " +
      "\003(\0132#.ibc.core.client.v1.GenesisMetadata" +
      "B\036\310\336\037\000\362\336\037\026yaml:\"client_metadata\"R\016client" +
      "MetadataB\315\001\n\026com.ibc.core.client.v1B\014Gen" +
      "esisProtoP\001Z:github.com/AssetMantle/modu" +
      "les/ibc/core/client/v1;clientv1\242\002\003ICC\252\002\022" +
      "Ibc.Core.Client.V1\312\002\022Ibc\\Core\\Client\\V1\342" +
      "\002\036Ibc\\Core\\Client\\V1\\GPBMetadata\352\002\025Ibc::" +
      "Core::Client::V1b\006proto3"
    };
    descriptor = com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
          com.ibc.core.client.v1.ClientProto.getDescriptor(),
          com.gogoproto.GogoProto.getDescriptor(),
        });
    internal_static_ibc_core_client_v1_GenesisState_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_ibc_core_client_v1_GenesisState_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_ibc_core_client_v1_GenesisState_descriptor,
        new java.lang.String[] { "Clients", "ClientsConsensus", "ClientsMetadata", "Params", "CreateLocalhost", "NextClientSequence", });
    internal_static_ibc_core_client_v1_GenesisMetadata_descriptor =
      getDescriptor().getMessageTypes().get(1);
    internal_static_ibc_core_client_v1_GenesisMetadata_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_ibc_core_client_v1_GenesisMetadata_descriptor,
        new java.lang.String[] { "Key", "Value", });
    internal_static_ibc_core_client_v1_IdentifiedGenesisMetadata_descriptor =
      getDescriptor().getMessageTypes().get(2);
    internal_static_ibc_core_client_v1_IdentifiedGenesisMetadata_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_ibc_core_client_v1_IdentifiedGenesisMetadata_descriptor,
        new java.lang.String[] { "ClientId", "ClientMetadata", });
    com.google.protobuf.ExtensionRegistry registry =
        com.google.protobuf.ExtensionRegistry.newInstance();
    registry.add(com.gogoproto.GogoProto.castrepeated);
    registry.add(com.gogoproto.GogoProto.goprotoGetters);
    registry.add(com.gogoproto.GogoProto.moretags);
    registry.add(com.gogoproto.GogoProto.nullable);
    com.google.protobuf.Descriptors.FileDescriptor
        .internalUpdateFileDescriptor(descriptor, registry);
    com.ibc.core.client.v1.ClientProto.getDescriptor();
    com.gogoproto.GogoProto.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}