// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: modules/splits/internal/transactions/wrap/transactionRequest.v1.proto

package com.splits.transactions.wrap;

public final class TransactionRequestV1Proto {
  private TransactionRequestV1Proto() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_splits_transactions_wrap_TransactionRequest_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_splits_transactions_wrap_TransactionRequest_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\nEmodules/splits/internal/transactions/w" +
      "rap/transactionRequest.v1.proto\022\030splits." +
      "transactions.wrap\032\024gogoproto/gogo.proto\"" +
      "^\n\022TransactionRequest\022\022\n\004from\030\001 \001(\tR\004fro" +
      "m\022\030\n\010from_i_d\030\002 \001(\tR\006fromID\022\024\n\005coins\030\003 \001" +
      "(\tR\005coins:\004\210\240\037\000B\205\002\n\034com.splits.transacti" +
      "ons.wrapB\031TransactionRequestV1ProtoP\001ZHg" +
      "ithub.com/AssetMantle/modules/modules/sp" +
      "lits/internal/transactions/wrap\242\002\003STW\252\002\030" +
      "Splits.Transactions.Wrap\312\002\030Splits\\Transa" +
      "ctions\\Wrap\342\002$Splits\\Transactions\\Wrap\\G" +
      "PBMetadata\352\002\032Splits::Transactions::Wrapb" +
      "\006proto3"
    };
    descriptor = com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
          com.gogoproto.GogoProto.getDescriptor(),
        });
    internal_static_splits_transactions_wrap_TransactionRequest_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_splits_transactions_wrap_TransactionRequest_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_splits_transactions_wrap_TransactionRequest_descriptor,
        new java.lang.String[] { "From", "FromID", "Coins", });
    com.google.protobuf.ExtensionRegistry registry =
        com.google.protobuf.ExtensionRegistry.newInstance();
    registry.add(com.gogoproto.GogoProto.goprotoGetters);
    com.google.protobuf.Descriptors.FileDescriptor
        .internalUpdateFileDescriptor(descriptor, registry);
    com.gogoproto.GogoProto.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}