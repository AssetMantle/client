// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: cosmos/base/store/v1beta1/listening.proto

package com.cosmos.base.store.v1beta1;

public final class ListeningProto {
  private ListeningProto() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_cosmos_base_store_v1beta1_StoreKVPair_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_cosmos_base_store_v1beta1_StoreKVPair_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_cosmos_base_store_v1beta1_BlockMetadata_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_cosmos_base_store_v1beta1_BlockMetadata_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_cosmos_base_store_v1beta1_BlockMetadata_DeliverTx_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_cosmos_base_store_v1beta1_BlockMetadata_DeliverTx_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n)cosmos/base/store/v1beta1/listening.pr" +
      "oto\022\031cosmos.base.store.v1beta1\032\033tendermi" +
      "nt/abci/types.proto\"j\n\013StoreKVPair\022\033\n\tst" +
      "ore_key\030\001 \001(\tR\010storeKey\022\026\n\006delete\030\002 \001(\010R" +
      "\006delete\022\020\n\003key\030\003 \001(\014R\003key\022\024\n\005value\030\004 \001(\014" +
      "R\005value\"\203\005\n\rBlockMetadata\022R\n\023request_beg" +
      "in_block\030\001 \001(\0132\".tendermint.abci.Request" +
      "BeginBlockR\021requestBeginBlock\022U\n\024respons" +
      "e_begin_block\030\002 \001(\0132#.tendermint.abci.Re" +
      "sponseBeginBlockR\022responseBeginBlock\022S\n\013" +
      "deliver_txs\030\003 \003(\01322.cosmos.base.store.v1" +
      "beta1.BlockMetadata.DeliverTxR\ndeliverTx" +
      "s\022L\n\021request_end_block\030\004 \001(\0132 .tendermin" +
      "t.abci.RequestEndBlockR\017requestEndBlock\022" +
      "O\n\022response_end_block\030\005 \001(\0132!.tendermint" +
      ".abci.ResponseEndBlockR\020responseEndBlock" +
      "\022H\n\017response_commit\030\006 \001(\0132\037.tendermint.a" +
      "bci.ResponseCommitR\016responseCommit\032\210\001\n\tD" +
      "eliverTx\022;\n\007request\030\001 \001(\0132!.tendermint.a" +
      "bci.RequestDeliverTxR\007request\022>\n\010respons" +
      "e\030\002 \001(\0132\".tendermint.abci.ResponseDelive" +
      "rTxR\010responseB\340\001\n\035com.cosmos.base.store." +
      "v1beta1B\016ListeningProtoP\001Z(github.com/co" +
      "smos/cosmos-sdk/store/types\242\002\003CBS\252\002\031Cosm" +
      "os.Base.Store.V1beta1\312\002\031Cosmos\\Base\\Stor" +
      "e\\V1beta1\342\002%Cosmos\\Base\\Store\\V1beta1\\GP" +
      "BMetadata\352\002\034Cosmos::Base::Store::V1beta1" +
      "b\006proto3"
    };
    descriptor = com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
          com.tendermint.abci.TypesProto.getDescriptor(),
        });
    internal_static_cosmos_base_store_v1beta1_StoreKVPair_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_cosmos_base_store_v1beta1_StoreKVPair_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_cosmos_base_store_v1beta1_StoreKVPair_descriptor,
        new java.lang.String[] { "StoreKey", "Delete", "Key", "Value", });
    internal_static_cosmos_base_store_v1beta1_BlockMetadata_descriptor =
      getDescriptor().getMessageTypes().get(1);
    internal_static_cosmos_base_store_v1beta1_BlockMetadata_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_cosmos_base_store_v1beta1_BlockMetadata_descriptor,
        new java.lang.String[] { "RequestBeginBlock", "ResponseBeginBlock", "DeliverTxs", "RequestEndBlock", "ResponseEndBlock", "ResponseCommit", });
    internal_static_cosmos_base_store_v1beta1_BlockMetadata_DeliverTx_descriptor =
      internal_static_cosmos_base_store_v1beta1_BlockMetadata_descriptor.getNestedTypes().get(0);
    internal_static_cosmos_base_store_v1beta1_BlockMetadata_DeliverTx_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_cosmos_base_store_v1beta1_BlockMetadata_DeliverTx_descriptor,
        new java.lang.String[] { "Request", "Response", });
    com.tendermint.abci.TypesProto.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}
