// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: modules/assets/internal/transactions/burn/service.v1.proto

package com.assets.transactions.burn;

public final class ServiceV1Proto {
  private ServiceV1Proto() {}
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
      "\n:modules/assets/internal/transactions/b" +
      "urn/service.v1.proto\022\030assets.transaction" +
      "s.burn\032\034google/api/annotations.proto\032:mo" +
      "dules/assets/internal/transactions/burn/" +
      "message.v1.proto\032Fmodules/assets/interna" +
      "l/transactions/burn/transactionResponse." +
      "v1.proto2\202\001\n\007Service\022w\n\006Handle\022!.assets." +
      "transactions.burn.Message\032-.assets.trans" +
      "actions.burn.TransactionResponse\"\033\202\323\344\223\002\025" +
      "\"\023/mantle/assets/burnB\372\001\n\034com.assets.tra" +
      "nsactions.burnB\016ServiceV1ProtoP\001ZHgithub" +
      ".com/AssetMantle/modules/modules/assets/" +
      "internal/transactions/burn\242\002\003ATB\252\002\030Asset" +
      "s.Transactions.Burn\312\002\030Assets\\Transaction" +
      "s\\Burn\342\002$Assets\\Transactions\\Burn\\GPBMet" +
      "adata\352\002\032Assets::Transactions::Burnb\006prot" +
      "o3"
    };
    descriptor = com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
          com.google.api.AnnotationsProto.getDescriptor(),
          com.assets.transactions.burn.MessageV1Proto.getDescriptor(),
          com.assets.transactions.burn.TransactionResponseV1Proto.getDescriptor(),
        });
    com.google.protobuf.ExtensionRegistry registry =
        com.google.protobuf.ExtensionRegistry.newInstance();
    registry.add(com.google.api.AnnotationsProto.http);
    com.google.protobuf.Descriptors.FileDescriptor
        .internalUpdateFileDescriptor(descriptor, registry);
    com.google.api.AnnotationsProto.getDescriptor();
    com.assets.transactions.burn.MessageV1Proto.getDescriptor();
    com.assets.transactions.burn.TransactionResponseV1Proto.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}
