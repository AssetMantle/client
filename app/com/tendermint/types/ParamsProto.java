// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: tendermint/types/params.proto

package com.tendermint.types;

public final class ParamsProto {
  private ParamsProto() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_tendermint_types_ConsensusParams_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_tendermint_types_ConsensusParams_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_tendermint_types_BlockParams_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_tendermint_types_BlockParams_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_tendermint_types_EvidenceParams_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_tendermint_types_EvidenceParams_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_tendermint_types_ValidatorParams_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_tendermint_types_ValidatorParams_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_tendermint_types_VersionParams_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_tendermint_types_VersionParams_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_tendermint_types_HashedParams_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_tendermint_types_HashedParams_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\035tendermint/types/params.proto\022\020tenderm" +
      "int.types\032\024gogoproto/gogo.proto\032\036google/" +
      "protobuf/duration.proto\"\230\002\n\017ConsensusPar" +
      "ams\0229\n\005block\030\001 \001(\0132\035.tendermint.types.Bl" +
      "ockParamsB\004\310\336\037\000R\005block\022B\n\010evidence\030\002 \001(\013" +
      "2 .tendermint.types.EvidenceParamsB\004\310\336\037\000" +
      "R\010evidence\022E\n\tvalidator\030\003 \001(\0132!.tendermi" +
      "nt.types.ValidatorParamsB\004\310\336\037\000R\tvalidato" +
      "r\022?\n\007version\030\004 \001(\0132\037.tendermint.types.Ve" +
      "rsionParamsB\004\310\336\037\000R\007version\"e\n\013BlockParam" +
      "s\022\033\n\tmax_bytes\030\001 \001(\003R\010maxBytes\022\027\n\007max_ga" +
      "s\030\002 \001(\003R\006maxGas\022 \n\014time_iota_ms\030\003 \001(\003R\nt" +
      "imeIotaMs\"\251\001\n\016EvidenceParams\022+\n\022max_age_" +
      "num_blocks\030\001 \001(\003R\017maxAgeNumBlocks\022M\n\020max" +
      "_age_duration\030\002 \001(\0132\031.google.protobuf.Du" +
      "rationB\010\310\336\037\000\230\337\037\001R\016maxAgeDuration\022\033\n\tmax_" +
      "bytes\030\003 \001(\003R\010maxBytes\"?\n\017ValidatorParams" +
      "\022\"\n\rpub_key_types\030\001 \003(\tR\013pubKeyTypes:\010\270\240" +
      "\037\001\350\240\037\001\":\n\rVersionParams\022\037\n\013app_version\030\001" +
      " \001(\004R\nappVersion:\010\270\240\037\001\350\240\037\001\"Z\n\014HashedPara" +
      "ms\022&\n\017block_max_bytes\030\001 \001(\003R\rblockMaxByt" +
      "es\022\"\n\rblock_max_gas\030\002 \001(\003R\013blockMaxGasB\301" +
      "\001\n\024com.tendermint.typesB\013ParamsProtoP\001Z7" +
      "github.com/tendermint/tendermint/proto/t" +
      "endermint/types\242\002\003TTX\252\002\020Tendermint.Types" +
      "\312\002\020Tendermint\\Types\342\002\034Tendermint\\Types\\G" +
      "PBMetadata\352\002\021Tendermint::Types\250\342\036\001b\006prot" +
      "o3"
    };
    descriptor = com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
          com.gogoproto.GogoProto.getDescriptor(),
          com.google.protobuf.DurationProto.getDescriptor(),
        });
    internal_static_tendermint_types_ConsensusParams_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_tendermint_types_ConsensusParams_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_tendermint_types_ConsensusParams_descriptor,
        new java.lang.String[] { "Block", "Evidence", "Validator", "Version", });
    internal_static_tendermint_types_BlockParams_descriptor =
      getDescriptor().getMessageTypes().get(1);
    internal_static_tendermint_types_BlockParams_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_tendermint_types_BlockParams_descriptor,
        new java.lang.String[] { "MaxBytes", "MaxGas", "TimeIotaMs", });
    internal_static_tendermint_types_EvidenceParams_descriptor =
      getDescriptor().getMessageTypes().get(2);
    internal_static_tendermint_types_EvidenceParams_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_tendermint_types_EvidenceParams_descriptor,
        new java.lang.String[] { "MaxAgeNumBlocks", "MaxAgeDuration", "MaxBytes", });
    internal_static_tendermint_types_ValidatorParams_descriptor =
      getDescriptor().getMessageTypes().get(3);
    internal_static_tendermint_types_ValidatorParams_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_tendermint_types_ValidatorParams_descriptor,
        new java.lang.String[] { "PubKeyTypes", });
    internal_static_tendermint_types_VersionParams_descriptor =
      getDescriptor().getMessageTypes().get(4);
    internal_static_tendermint_types_VersionParams_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_tendermint_types_VersionParams_descriptor,
        new java.lang.String[] { "AppVersion", });
    internal_static_tendermint_types_HashedParams_descriptor =
      getDescriptor().getMessageTypes().get(5);
    internal_static_tendermint_types_HashedParams_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_tendermint_types_HashedParams_descriptor,
        new java.lang.String[] { "BlockMaxBytes", "BlockMaxGas", });
    com.google.protobuf.ExtensionRegistry registry =
        com.google.protobuf.ExtensionRegistry.newInstance();
    registry.add(com.gogoproto.GogoProto.equal);
    registry.add(com.gogoproto.GogoProto.equalAll);
    registry.add(com.gogoproto.GogoProto.nullable);
    registry.add(com.gogoproto.GogoProto.populate);
    registry.add(com.gogoproto.GogoProto.stdduration);
    com.google.protobuf.Descriptors.FileDescriptor
        .internalUpdateFileDescriptor(descriptor, registry);
    com.gogoproto.GogoProto.getDescriptor();
    com.google.protobuf.DurationProto.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}
