// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: modules/orders/internal/queries/order/service.proto

package com.orders.queries.order;

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
      "\n3modules/orders/internal/queries/order/" +
      "service.proto\022\024orders.queries.order\032\034goo" +
      "gle/api/annotations.proto\0328modules/order" +
      "s/internal/queries/order/queryRequest.pr" +
      "oto\0329modules/orders/internal/queries/ord" +
      "er/queryResponse.proto2\202\001\n\007Service\022w\n\006Ha" +
      "ndle\022\".orders.queries.order.QueryRequest" +
      "\032#.orders.queries.order.QueryResponse\"$\202" +
      "\323\344\223\002\036\022\034/mantle/orders/v1beta1/orderB\340\001\n\030" +
      "com.orders.queries.orderB\014ServiceProtoP\001" +
      "ZDgithub.com/AssetMantle/modules/modules" +
      "/orders/internal/queries/order\242\002\003OQO\252\002\024O" +
      "rders.Queries.Order\312\002\024Orders\\Queries\\Ord" +
      "er\342\002 Orders\\Queries\\Order\\GPBMetadata\352\002\026" +
      "Orders::Queries::Orderb\006proto3"
    };
    descriptor = com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
          com.google.api.AnnotationsProto.getDescriptor(),
          com.orders.queries.order.QueryRequestProto.getDescriptor(),
          com.orders.queries.order.QueryResponseProto.getDescriptor(),
        });
    com.google.protobuf.ExtensionRegistry registry =
        com.google.protobuf.ExtensionRegistry.newInstance();
    registry.add(com.google.api.AnnotationsProto.http);
    com.google.protobuf.Descriptors.FileDescriptor
        .internalUpdateFileDescriptor(descriptor, registry);
    com.google.api.AnnotationsProto.getDescriptor();
    com.orders.queries.order.QueryRequestProto.getDescriptor();
    com.orders.queries.order.QueryResponseProto.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}
