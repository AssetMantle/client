// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: orders/queries/order/query_response.proto

package com.assetmantle.modules.orders.queries.order;

public final class QueryResponseProto {
  private QueryResponseProto() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_assetmantle_modules_orders_queries_order_QueryResponse_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_assetmantle_modules_orders_queries_order_QueryResponse_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n)orders/queries/order/query_response.pr" +
      "oto\022(assetmantle.modules.orders.queries." +
      "order\032\024gogoproto/gogo.proto\032\036orders/mapp" +
      "able/mappable.proto\"X\n\rQueryResponse\022A\n\004" +
      "list\030\001 \003(\0132-.assetmantle.modules.orders." +
      "mappable.MappableR\004list:\004\210\240\037\000B\277\002\n,com.as" +
      "setmantle.modules.orders.queries.orderB\022" +
      "QueryResponseProtoP\001Z5github.com/AssetMa" +
      "ntle/modules/x/orders/queries/order\242\002\005AM" +
      "OQO\252\002(Assetmantle.Modules.Orders.Queries" +
      ".Order\312\002(Assetmantle\\Modules\\Orders\\Quer" +
      "ies\\Order\342\0024Assetmantle\\Modules\\Orders\\Q" +
      "ueries\\Order\\GPBMetadata\352\002,Assetmantle::" +
      "Modules::Orders::Queries::Orderb\006proto3"
    };
    descriptor = com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
          com.gogoproto.GogoProto.getDescriptor(),
          com.assetmantle.modules.orders.mappable.MappableProto.getDescriptor(),
        });
    internal_static_assetmantle_modules_orders_queries_order_QueryResponse_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_assetmantle_modules_orders_queries_order_QueryResponse_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_assetmantle_modules_orders_queries_order_QueryResponse_descriptor,
        new java.lang.String[] { "List", });
    com.google.protobuf.ExtensionRegistry registry =
        com.google.protobuf.ExtensionRegistry.newInstance();
    registry.add(com.gogoproto.GogoProto.goprotoGetters);
    com.google.protobuf.Descriptors.FileDescriptor
        .internalUpdateFileDescriptor(descriptor, registry);
    com.gogoproto.GogoProto.getDescriptor();
    com.assetmantle.modules.orders.mappable.MappableProto.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}
