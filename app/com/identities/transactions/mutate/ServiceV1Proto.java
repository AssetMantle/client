// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: modules/identities/internal/transactions/mutate/service.v1.proto

package com.identities.transactions.mutate;

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
      "\n@modules/identities/internal/transactio" +
      "ns/mutate/service.v1.proto\022\036identities.t" +
      "ransactions.mutate\032\034google/api/annotatio" +
      "ns.proto\032@modules/identities/internal/tr" +
      "ansactions/mutate/message.v1.proto\032Lmodu" +
      "les/identities/internal/transactions/mut" +
      "ate/transactionResponse.v1.proto2\225\001\n\007Ser" +
      "vice\022\211\001\n\006Handle\022\'.identities.transaction" +
      "s.mutate.Message\0323.identities.transactio" +
      "ns.mutate.TransactionResponse\"!\202\323\344\223\002\033\"\031/" +
      "mantle/identities/mutateB\236\002\n\"com.identit" +
      "ies.transactions.mutateB\016ServiceV1ProtoP" +
      "\001ZNgithub.com/AssetMantle/modules/module" +
      "s/identities/internal/transactions/mutat" +
      "e\242\002\003ITM\252\002\036Identities.Transactions.Mutate" +
      "\312\002\036Identities\\Transactions\\Mutate\342\002*Iden" +
      "tities\\Transactions\\Mutate\\GPBMetadata\352\002" +
      " Identities::Transactions::Mutateb\006proto" +
      "3"
    };
    descriptor = com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
          com.google.api.AnnotationsProto.getDescriptor(),
          com.identities.transactions.mutate.MessageV1Proto.getDescriptor(),
          com.identities.transactions.mutate.TransactionResponseV1Proto.getDescriptor(),
        });
    com.google.protobuf.ExtensionRegistry registry =
        com.google.protobuf.ExtensionRegistry.newInstance();
    registry.add(com.google.api.AnnotationsProto.http);
    com.google.protobuf.Descriptors.FileDescriptor
        .internalUpdateFileDescriptor(descriptor, registry);
    com.google.api.AnnotationsProto.getDescriptor();
    com.identities.transactions.mutate.MessageV1Proto.getDescriptor();
    com.identities.transactions.mutate.TransactionResponseV1Proto.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}
