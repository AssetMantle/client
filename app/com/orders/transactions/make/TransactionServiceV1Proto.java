// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: modules/orders/internal/transactions/make/transactionService.v1.proto

package com.orders.transactions.make;

public final class TransactionServiceV1Proto {
  private TransactionServiceV1Proto() {}
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
      "\nEmodules/orders/internal/transactions/m" +
      "ake/transactionService.v1.proto\022\030orders." +
      "transactions.make\032\034google/api/annotation" +
      "s.proto\032:modules/orders/internal/transac" +
      "tions/make/message.v1.proto\032Fmodules/ord" +
      "ers/internal/transactions/make/transacti" +
      "onResponse.v1.proto2\203\001\n\013Transaction\022t\n\004M" +
      "ake\022!.orders.transactions.make.Message\032-" +
      ".orders.transactions.make.TransactionRes" +
      "ponse\"\032\202\323\344\223\002\024\"\022/mantle/metas/makeB\205\002\n\034co" +
      "m.orders.transactions.makeB\031TransactionS" +
      "erviceV1ProtoP\001ZHgithub.com/AssetMantle/" +
      "modules/modules/orders/internal/transact" +
      "ions/make\242\002\003OTM\252\002\030Orders.Transactions.Ma" +
      "ke\312\002\030Orders\\Transactions\\Make\342\002$Orders\\T" +
      "ransactions\\Make\\GPBMetadata\352\002\032Orders::T" +
      "ransactions::Makeb\006proto3"
    };
    descriptor = com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
          com.google.api.AnnotationsProto.getDescriptor(),
          com.orders.transactions.make.MessageV1Proto.getDescriptor(),
          com.orders.transactions.make.TransactionResponseV1Proto.getDescriptor(),
        });
    com.google.protobuf.ExtensionRegistry registry =
        com.google.protobuf.ExtensionRegistry.newInstance();
    registry.add(com.google.api.AnnotationsProto.http);
    com.google.protobuf.Descriptors.FileDescriptor
        .internalUpdateFileDescriptor(descriptor, registry);
    com.google.api.AnnotationsProto.getDescriptor();
    com.orders.transactions.make.MessageV1Proto.getDescriptor();
    com.orders.transactions.make.TransactionResponseV1Proto.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}
