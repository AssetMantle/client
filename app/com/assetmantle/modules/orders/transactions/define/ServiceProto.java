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
      "on_response.proto2\255\001\n\003Msg\022\245\001\n\006Handle\0227.a" +
      "ssetmantle.modules.orders.transactions.d" +
      "efine.Message\032C.assetmantle.modules.orde" +
      "rs.transactions.define.TransactionRespon" +
      "se\"\035\202\323\344\223\002\027\"\025/mantle/orders/defineB\335\002\n2co" +
      "m.assetmantle.modules.orders.transaction" +
      "s.defineB\014ServiceProtoP\001Z;github.com/Ass" +
      "etMantle/modules/x/orders/transactions/d" +
      "efine\242\002\005AMOTD\252\002.Assetmantle.Modules.Orde" +
      "rs.Transactions.Define\312\002.Assetmantle\\Mod" +
      "ules\\Orders\\Transactions\\Define\342\002:Assetm" +
      "antle\\Modules\\Orders\\Transactions\\Define" +
      "\\GPBMetadata\352\0022Assetmantle::Modules::Ord" +
      "ers::Transactions::Defineb\006proto3"
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
