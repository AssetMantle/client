// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: assets/transactions/mutate/service.proto

package com.assetmantle.modules.assets.transactions.mutate;

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
      "\n(assets/transactions/mutate/service.pro" +
      "to\022.assetmantle.modules.assets.transacti" +
      "ons.mutate\032\034google/api/annotations.proto" +
      "\032(assets/transactions/mutate/message.pro" +
      "to\0325assets/transactions/mutate/transacti" +
      "on_response.proto2\255\001\n\003Msg\022\245\001\n\006Handle\0227.a" +
      "ssetmantle.modules.assets.transactions.m" +
      "utate.Message\032C.assetmantle.modules.asse" +
      "ts.transactions.mutate.TransactionRespon" +
      "se\"\035\202\323\344\223\002\027\"\025/mantle/assets/mutateB\335\002\n2co" +
      "m.assetmantle.modules.assets.transaction" +
      "s.mutateB\014ServiceProtoP\001Z;github.com/Ass" +
      "etMantle/modules/x/assets/transactions/m" +
      "utate\242\002\005AMATM\252\002.Assetmantle.Modules.Asse" +
      "ts.Transactions.Mutate\312\002.Assetmantle\\Mod" +
      "ules\\Assets\\Transactions\\Mutate\342\002:Assetm" +
      "antle\\Modules\\Assets\\Transactions\\Mutate" +
      "\\GPBMetadata\352\0022Assetmantle::Modules::Ass" +
      "ets::Transactions::Mutateb\006proto3"
    };
    descriptor = com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
          com.google.api.AnnotationsProto.getDescriptor(),
          com.assetmantle.modules.assets.transactions.mutate.MessageProto.getDescriptor(),
          com.assetmantle.modules.assets.transactions.mutate.TransactionResponseProto.getDescriptor(),
        });
    com.google.protobuf.ExtensionRegistry registry =
        com.google.protobuf.ExtensionRegistry.newInstance();
    registry.add(com.google.api.AnnotationsProto.http);
    com.google.protobuf.Descriptors.FileDescriptor
        .internalUpdateFileDescriptor(descriptor, registry);
    com.google.api.AnnotationsProto.getDescriptor();
    com.assetmantle.modules.assets.transactions.mutate.MessageProto.getDescriptor();
    com.assetmantle.modules.assets.transactions.mutate.TransactionResponseProto.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}
