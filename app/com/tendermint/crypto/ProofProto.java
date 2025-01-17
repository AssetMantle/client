// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: tendermint/crypto/proof.proto

package com.tendermint.crypto;

public final class ProofProto {
  private ProofProto() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_tendermint_crypto_Proof_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_tendermint_crypto_Proof_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_tendermint_crypto_ValueOp_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_tendermint_crypto_ValueOp_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_tendermint_crypto_DominoOp_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_tendermint_crypto_DominoOp_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_tendermint_crypto_ProofOp_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_tendermint_crypto_ProofOp_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_tendermint_crypto_ProofOps_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_tendermint_crypto_ProofOps_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\035tendermint/crypto/proof.proto\022\021tenderm" +
      "int.crypto\032\024gogoproto/gogo.proto\"f\n\005Proo" +
      "f\022\024\n\005total\030\001 \001(\003R\005total\022\024\n\005index\030\002 \001(\003R\005" +
      "index\022\033\n\tleaf_hash\030\003 \001(\014R\010leafHash\022\024\n\005au" +
      "nts\030\004 \003(\014R\005aunts\"K\n\007ValueOp\022\020\n\003key\030\001 \001(\014" +
      "R\003key\022.\n\005proof\030\002 \001(\0132\030.tendermint.crypto" +
      ".ProofR\005proof\"J\n\010DominoOp\022\020\n\003key\030\001 \001(\tR\003" +
      "key\022\024\n\005input\030\002 \001(\tR\005input\022\026\n\006output\030\003 \001(" +
      "\tR\006output\"C\n\007ProofOp\022\022\n\004type\030\001 \001(\tR\004type" +
      "\022\020\n\003key\030\002 \001(\014R\003key\022\022\n\004data\030\003 \001(\014R\004data\">" +
      "\n\010ProofOps\0222\n\003ops\030\001 \003(\0132\032.tendermint.cry" +
      "pto.ProofOpB\004\310\336\037\000R\003opsB\302\001\n\025com.tendermin" +
      "t.cryptoB\nProofProtoP\001Z8github.com/tende" +
      "rmint/tendermint/proto/tendermint/crypto" +
      "\242\002\003TCX\252\002\021Tendermint.Crypto\312\002\021Tendermint\\" +
      "Crypto\342\002\035Tendermint\\Crypto\\GPBMetadata\352\002" +
      "\022Tendermint::Cryptob\006proto3"
    };
    descriptor = com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
          com.gogoproto.GogoProto.getDescriptor(),
        });
    internal_static_tendermint_crypto_Proof_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_tendermint_crypto_Proof_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_tendermint_crypto_Proof_descriptor,
        new java.lang.String[] { "Total", "Index", "LeafHash", "Aunts", });
    internal_static_tendermint_crypto_ValueOp_descriptor =
      getDescriptor().getMessageTypes().get(1);
    internal_static_tendermint_crypto_ValueOp_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_tendermint_crypto_ValueOp_descriptor,
        new java.lang.String[] { "Key", "Proof", });
    internal_static_tendermint_crypto_DominoOp_descriptor =
      getDescriptor().getMessageTypes().get(2);
    internal_static_tendermint_crypto_DominoOp_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_tendermint_crypto_DominoOp_descriptor,
        new java.lang.String[] { "Key", "Input", "Output", });
    internal_static_tendermint_crypto_ProofOp_descriptor =
      getDescriptor().getMessageTypes().get(3);
    internal_static_tendermint_crypto_ProofOp_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_tendermint_crypto_ProofOp_descriptor,
        new java.lang.String[] { "Type", "Key", "Data", });
    internal_static_tendermint_crypto_ProofOps_descriptor =
      getDescriptor().getMessageTypes().get(4);
    internal_static_tendermint_crypto_ProofOps_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_tendermint_crypto_ProofOps_descriptor,
        new java.lang.String[] { "Ops", });
    com.google.protobuf.ExtensionRegistry registry =
        com.google.protobuf.ExtensionRegistry.newInstance();
    registry.add(com.gogoproto.GogoProto.nullable);
    com.google.protobuf.Descriptors.FileDescriptor
        .internalUpdateFileDescriptor(descriptor, registry);
    com.gogoproto.GogoProto.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}
