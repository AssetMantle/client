// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: modules/splits/internal/transactions/unwrap/service.v1.proto

package com.splits.transactions.unwrap;

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
      "\n<modules/splits/internal/transactions/u" +
      "nwrap/service.v1.proto\022\032splits.transacti" +
      "ons.unwrap\032\034google/api/annotations.proto" +
      "\032<modules/splits/internal/transactions/u" +
      "nwrap/message.v1.proto\032=modules/splits/i" +
      "nternal/transactions/unwrap/response.v1." +
      "proto2}\n\007Service\022r\n\006Handle\022#.splits.tran" +
      "sactions.unwrap.Message\032$.splits.transac" +
      "tions.unwrap.Response\"\035\202\323\344\223\002\027\"\025/mantle/s" +
      "plits/unwrapB\206\002\n\036com.splits.transactions" +
      ".unwrapB\016ServiceV1ProtoP\001ZJgithub.com/As" +
      "setMantle/modules/modules/splits/interna" +
      "l/transactions/unwrap\242\002\003STU\252\002\032Splits.Tra" +
      "nsactions.Unwrap\312\002\032Splits\\Transactions\\U" +
      "nwrap\342\002&Splits\\Transactions\\Unwrap\\GPBMe" +
      "tadata\352\002\034Splits::Transactions::Unwrapb\006p" +
      "roto3"
    };
    descriptor = com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
          com.google.api.AnnotationsProto.getDescriptor(),
          com.splits.transactions.unwrap.MessageV1Proto.getDescriptor(),
          com.splits.transactions.unwrap.ResponseV1Proto.getDescriptor(),
        });
    com.google.protobuf.ExtensionRegistry registry =
        com.google.protobuf.ExtensionRegistry.newInstance();
    registry.add(com.google.api.AnnotationsProto.http);
    com.google.protobuf.Descriptors.FileDescriptor
        .internalUpdateFileDescriptor(descriptor, registry);
    com.google.api.AnnotationsProto.getDescriptor();
    com.splits.transactions.unwrap.MessageV1Proto.getDescriptor();
    com.splits.transactions.unwrap.ResponseV1Proto.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}
