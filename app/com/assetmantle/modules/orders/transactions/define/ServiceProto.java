// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: orders/transactions/define/service.proto

package com.assetmantle.modules.orders.transactions.define;

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
      "\n(orders/transactions/define/service.pro" +
      "to\022.assetmantle.modules.orders.transacti" +
      "ons.define\032\034google/api/annotations.proto" +
      "\032(orders/transactions/define/message.pro" +
      "to\0325orders/transactions/define/transacti" +
      "on_response.proto2\261\001\n\007Service\022\245\001\n\006Handle" +
      "\0227.assetmantle.modules.orders.transactio" +
      "ns.define.Message\032C.assetmantle.modules." +
      "orders.transactions.define.TransactionRe" +
      "sponse\"\035\202\323\344\223\002\027\"\025/mantle/orders/defineB\335\002" +
      "\n2com.assetmantle.modules.orders.transac" +
      "tions.defineB\014ServiceProtoP\001Z;github.com" +
      "/AssetMantle/modules/x/orders/transactio" +
      "ns/define\242\002\005AMOTD\252\002.Assetmantle.Modules." +
      "Orders.Transactions.Define\312\002.Assetmantle" +
      "\\Modules\\Orders\\Transactions\\Define\342\002:As" +
      "setmantle\\Modules\\Orders\\Transactions\\De" +
      "fine\\GPBMetadata\352\0022Assetmantle::Modules:" +
      ":Orders::Transactions::Defineb\006proto3"
    };
    descriptor = com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
          com.google.api.AnnotationsProto.getDescriptor(),
          com.assetmantle.modules.orders.transactions.define.MessageProto.getDescriptor(),
          com.assetmantle.modules.orders.transactions.define.TransactionResponseProto.getDescriptor(),
        });
    com.google.protobuf.ExtensionRegistry registry =
        com.google.protobuf.ExtensionRegistry.newInstance();
    registry.add(com.google.api.AnnotationsProto.http);
    com.google.protobuf.Descriptors.FileDescriptor
        .internalUpdateFileDescriptor(descriptor, registry);
    com.google.api.AnnotationsProto.getDescriptor();
    com.assetmantle.modules.orders.transactions.define.MessageProto.getDescriptor();
    com.assetmantle.modules.orders.transactions.define.TransactionResponseProto.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}
