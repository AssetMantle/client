// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: modules/orders/internal/transactions/cancel/service.proto

package com.orders.transactions.cancel;

public final class ServiceProto {
  private ServiceProto() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n9modules/orders/internal/transactions/c" +
      "ancel/service.proto\022\032orders.transactions" +
      ".cancel\032\034google/api/annotations.proto\0329m" +
      "odules/orders/internal/transactions/canc" +
      "el/message.proto\032Emodules/orders/interna" +
      "l/transactions/cancel/transactionRespons" +
      "e.proto2\210\001\n\007Service\022}\n\006Handle\022#.orders.t" +
      "ransactions.cancel.Message\032/.orders.tran" +
      "sactions.cancel.TransactionResponse\"\035\202\323\344" +
      "\223\002\027\"\025/mantle/orders/cancelB\204\002\n\036com.order" +
      "s.transactions.cancelB\014ServiceProtoP\001ZJg" +
      "ithub.com/AssetMantle/modules/modules/or" +
      "ders/internal/transactions/cancel\242\002\003OTC\252" +
      "\002\032Orders.Transactions.Cancel\312\002\032Orders\\Tr" +
      "ansactions\\Cancel\342\002&Orders\\Transactions\\" +
      "Cancel\\GPBMetadata\352\002\034Orders::Transaction" +
      "s::Cancelb\006proto3"
    };
    descriptor = com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
          com.google.api.AnnotationsProto.getDescriptor(),
          com.orders.transactions.cancel.MessageProto.getDescriptor(),
          com.orders.transactions.cancel.TransactionResponseProto.getDescriptor(),
        });
    com.google.protobuf.ExtensionRegistry registry =
        com.google.protobuf.ExtensionRegistry.newInstance();
    registry.add(com.google.api.AnnotationsProto.http);
    com.google.protobuf.Descriptors.FileDescriptor
        .internalUpdateFileDescriptor(descriptor, registry);
    com.google.api.AnnotationsProto.getDescriptor();
    com.orders.transactions.cancel.MessageProto.getDescriptor();
    com.orders.transactions.cancel.TransactionResponseProto.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}
