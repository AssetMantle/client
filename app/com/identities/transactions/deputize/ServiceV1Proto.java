// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: modules/identities/internal/transactions/deputize/service.v1.proto

package com.identities.transactions.deputize;

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
      "\nBmodules/identities/internal/transactio" +
      "ns/deputize/service.v1.proto\022 identities" +
      ".transactions.deputize\032\034google/api/annot" +
      "ations.proto\032Bmodules/identities/interna" +
      "l/transactions/deputize/message.v1.proto" +
      "\032Cmodules/identities/internal/transactio" +
      "ns/deputize/response.v1.proto2\220\001\n\007Servic" +
      "e\022\204\001\n\006Handle\022).identities.transactions.d" +
      "eputize.Message\032*.identities.transaction" +
      "s.deputize.Response\"#\202\323\344\223\002\035\"\033/mantle/ide" +
      "ntities/deputizeB\252\002\n$com.identities.tran" +
      "sactions.deputizeB\016ServiceV1ProtoP\001ZPgit" +
      "hub.com/AssetMantle/modules/modules/iden" +
      "tities/internal/transactions/deputize\242\002\003" +
      "ITD\252\002 Identities.Transactions.Deputize\312\002" +
      " Identities\\Transactions\\Deputize\342\002,Iden" +
      "tities\\Transactions\\Deputize\\GPBMetadata" +
      "\352\002\"Identities::Transactions::Deputizeb\006p" +
      "roto3"
    };
    descriptor = com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
          com.google.api.AnnotationsProto.getDescriptor(),
          com.identities.transactions.deputize.MessageV1Proto.getDescriptor(),
          com.identities.transactions.deputize.ResponseV1Proto.getDescriptor(),
        });
    com.google.protobuf.ExtensionRegistry registry =
        com.google.protobuf.ExtensionRegistry.newInstance();
    registry.add(com.google.api.AnnotationsProto.http);
    com.google.protobuf.Descriptors.FileDescriptor
        .internalUpdateFileDescriptor(descriptor, registry);
    com.google.api.AnnotationsProto.getDescriptor();
    com.identities.transactions.deputize.MessageV1Proto.getDescriptor();
    com.identities.transactions.deputize.ResponseV1Proto.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}