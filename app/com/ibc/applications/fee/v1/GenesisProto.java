// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: ibc/applications/fee/v1/genesis.proto

package com.ibc.applications.fee.v1;

public final class GenesisProto {
  private GenesisProto() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_ibc_applications_fee_v1_GenesisState_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_ibc_applications_fee_v1_GenesisState_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_ibc_applications_fee_v1_FeeEnabledChannel_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_ibc_applications_fee_v1_FeeEnabledChannel_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_ibc_applications_fee_v1_RegisteredPayee_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_ibc_applications_fee_v1_RegisteredPayee_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_ibc_applications_fee_v1_RegisteredCounterpartyPayee_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_ibc_applications_fee_v1_RegisteredCounterpartyPayee_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_ibc_applications_fee_v1_ForwardRelayerAddress_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_ibc_applications_fee_v1_ForwardRelayerAddress_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n%ibc/applications/fee/v1/genesis.proto\022" +
      "\027ibc.applications.fee.v1\032\024gogoproto/gogo" +
      ".proto\032!ibc/applications/fee/v1/fee.prot" +
      "o\032!ibc/core/channel/v1/channel.proto\"\253\005\n" +
      "\014GenesisState\022v\n\017identified_fees\030\001 \003(\0132-" +
      ".ibc.applications.fee.v1.IdentifiedPacke" +
      "tFeesB\036\310\336\037\000\362\336\037\026yaml:\"identified_fees\"R\016i" +
      "dentifiedFees\022\201\001\n\024fee_enabled_channels\030\002" +
      " \003(\0132*.ibc.applications.fee.v1.FeeEnable" +
      "dChannelB#\310\336\037\000\362\336\037\033yaml:\"fee_enabled_chan" +
      "nels\"R\022feeEnabledChannels\022w\n\021registered_" +
      "payees\030\003 \003(\0132(.ibc.applications.fee.v1.R" +
      "egisteredPayeeB \310\336\037\000\362\336\037\030yaml:\"registered" +
      "_payees\"R\020registeredPayees\022\251\001\n\036registere" +
      "d_counterparty_payees\030\004 \003(\01324.ibc.applic" +
      "ations.fee.v1.RegisteredCounterpartyPaye" +
      "eB-\310\336\037\000\362\336\037%yaml:\"registered_counterparty" +
      "_payees\"R\034registeredCounterpartyPayees\022z" +
      "\n\020forward_relayers\030\005 \003(\0132..ibc.applicati" +
      "ons.fee.v1.ForwardRelayerAddressB\037\310\336\037\000\362\336" +
      "\037\027yaml:\"forward_relayers\"R\017forwardRelaye" +
      "rs\"v\n\021FeeEnabledChannel\022+\n\007port_id\030\001 \001(\t" +
      "B\022\362\336\037\016yaml:\"port_id\"R\006portId\0224\n\nchannel_" +
      "id\030\002 \001(\tB\025\362\336\037\021yaml:\"channel_id\"R\tchannel" +
      "Id\"w\n\017RegisteredPayee\0224\n\nchannel_id\030\001 \001(" +
      "\tB\025\362\336\037\021yaml:\"channel_id\"R\tchannelId\022\030\n\007r" +
      "elayer\030\002 \001(\tR\007relayer\022\024\n\005payee\030\003 \001(\tR\005pa" +
      "yee\"\273\001\n\033RegisteredCounterpartyPayee\0224\n\nc" +
      "hannel_id\030\001 \001(\tB\025\362\336\037\021yaml:\"channel_id\"R\t" +
      "channelId\022\030\n\007relayer\030\002 \001(\tR\007relayer\022L\n\022c" +
      "ounterparty_payee\030\003 \001(\tB\035\362\336\037\031yaml:\"count" +
      "erparty_payee\"R\021counterpartyPayee\"\207\001\n\025Fo" +
      "rwardRelayerAddress\022\030\n\007address\030\001 \001(\tR\007ad" +
      "dress\022T\n\tpacket_id\030\002 \001(\0132\035.ibc.core.chan" +
      "nel.v1.PacketIdB\030\310\336\037\000\362\336\037\020yaml:\"packet_id" +
      "\"R\010packetIdB\341\001\n\033com.ibc.applications.fee" +
      ".v1B\014GenesisProtoP\001Z5github.com/cosmos/i" +
      "bc-go/v4/modules/apps/29-fee/types\242\002\003IAF" +
      "\252\002\027Ibc.Applications.Fee.V1\312\002\027Ibc\\Applica" +
      "tions\\Fee\\V1\342\002#Ibc\\Applications\\Fee\\V1\\G" +
      "PBMetadata\352\002\032Ibc::Applications::Fee::V1b" +
      "\006proto3"
    };
    descriptor = com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
          com.gogoproto.GogoProto.getDescriptor(),
          com.ibc.applications.fee.v1.FeeProto.getDescriptor(),
          com.ibc.core.channel.v1.ChannelProto.getDescriptor(),
        });
    internal_static_ibc_applications_fee_v1_GenesisState_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_ibc_applications_fee_v1_GenesisState_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_ibc_applications_fee_v1_GenesisState_descriptor,
        new java.lang.String[] { "IdentifiedFees", "FeeEnabledChannels", "RegisteredPayees", "RegisteredCounterpartyPayees", "ForwardRelayers", });
    internal_static_ibc_applications_fee_v1_FeeEnabledChannel_descriptor =
      getDescriptor().getMessageTypes().get(1);
    internal_static_ibc_applications_fee_v1_FeeEnabledChannel_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_ibc_applications_fee_v1_FeeEnabledChannel_descriptor,
        new java.lang.String[] { "PortId", "ChannelId", });
    internal_static_ibc_applications_fee_v1_RegisteredPayee_descriptor =
      getDescriptor().getMessageTypes().get(2);
    internal_static_ibc_applications_fee_v1_RegisteredPayee_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_ibc_applications_fee_v1_RegisteredPayee_descriptor,
        new java.lang.String[] { "ChannelId", "Relayer", "Payee", });
    internal_static_ibc_applications_fee_v1_RegisteredCounterpartyPayee_descriptor =
      getDescriptor().getMessageTypes().get(3);
    internal_static_ibc_applications_fee_v1_RegisteredCounterpartyPayee_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_ibc_applications_fee_v1_RegisteredCounterpartyPayee_descriptor,
        new java.lang.String[] { "ChannelId", "Relayer", "CounterpartyPayee", });
    internal_static_ibc_applications_fee_v1_ForwardRelayerAddress_descriptor =
      getDescriptor().getMessageTypes().get(4);
    internal_static_ibc_applications_fee_v1_ForwardRelayerAddress_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_ibc_applications_fee_v1_ForwardRelayerAddress_descriptor,
        new java.lang.String[] { "Address", "PacketId", });
    com.google.protobuf.ExtensionRegistry registry =
        com.google.protobuf.ExtensionRegistry.newInstance();
    registry.add(com.gogoproto.GogoProto.moretags);
    registry.add(com.gogoproto.GogoProto.nullable);
    com.google.protobuf.Descriptors.FileDescriptor
        .internalUpdateFileDescriptor(descriptor, registry);
    com.gogoproto.GogoProto.getDescriptor();
    com.ibc.applications.fee.v1.FeeProto.getDescriptor();
    com.ibc.core.channel.v1.ChannelProto.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}
