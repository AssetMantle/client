// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: ibc/core/connection/v1/tx.proto

package com.ibc.core.connection.v1;

public final class TxProto {
  private TxProto() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_ibc_core_connection_v1_MsgConnectionOpenInit_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_ibc_core_connection_v1_MsgConnectionOpenInit_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_ibc_core_connection_v1_MsgConnectionOpenInitResponse_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_ibc_core_connection_v1_MsgConnectionOpenInitResponse_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_ibc_core_connection_v1_MsgConnectionOpenTry_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_ibc_core_connection_v1_MsgConnectionOpenTry_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_ibc_core_connection_v1_MsgConnectionOpenTryResponse_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_ibc_core_connection_v1_MsgConnectionOpenTryResponse_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_ibc_core_connection_v1_MsgConnectionOpenAck_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_ibc_core_connection_v1_MsgConnectionOpenAck_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_ibc_core_connection_v1_MsgConnectionOpenAckResponse_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_ibc_core_connection_v1_MsgConnectionOpenAckResponse_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_ibc_core_connection_v1_MsgConnectionOpenConfirm_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_ibc_core_connection_v1_MsgConnectionOpenConfirm_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_ibc_core_connection_v1_MsgConnectionOpenConfirmResponse_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_ibc_core_connection_v1_MsgConnectionOpenConfirmResponse_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\037ibc/core/connection/v1/tx.proto\022\026ibc.c" +
      "ore.connection.v1\032\024gogoproto/gogo.proto\032" +
      "\031google/protobuf/any.proto\032\037ibc/core/cli" +
      "ent/v1/client.proto\032\'ibc/core/connection" +
      "/v1/connection.proto\"\263\002\n\025MsgConnectionOp" +
      "enInit\0221\n\tclient_id\030\001 \001(\tB\024\362\336\037\020yaml:\"cli" +
      "ent_id\"R\010clientId\022N\n\014counterparty\030\002 \001(\0132" +
      "$.ibc.core.connection.v1.CounterpartyB\004\310" +
      "\336\037\000R\014counterparty\0229\n\007version\030\003 \001(\0132\037.ibc" +
      ".core.connection.v1.VersionR\007version\022:\n\014" +
      "delay_period\030\004 \001(\004B\027\362\336\037\023yaml:\"delay_peri" +
      "od\"R\013delayPeriod\022\026\n\006signer\030\005 \001(\tR\006signer" +
      ":\010\210\240\037\000\350\240\037\000\"\037\n\035MsgConnectionOpenInitRespo" +
      "nse\"\225\007\n\024MsgConnectionOpenTry\0221\n\tclient_i" +
      "d\030\001 \001(\tB\024\362\336\037\020yaml:\"client_id\"R\010clientId\022" +
      "W\n\026previous_connection_id\030\002 \001(\tB!\362\336\037\035yam" +
      "l:\"previous_connection_id\"R\024previousConn" +
      "ectionId\022P\n\014client_state\030\003 \001(\0132\024.google." +
      "protobuf.AnyB\027\362\336\037\023yaml:\"client_state\"R\013c" +
      "lientState\022N\n\014counterparty\030\004 \001(\0132$.ibc.c" +
      "ore.connection.v1.CounterpartyB\004\310\336\037\000R\014co" +
      "unterparty\022:\n\014delay_period\030\005 \001(\004B\027\362\336\037\023ya" +
      "ml:\"delay_period\"R\013delayPeriod\022v\n\025counte" +
      "rparty_versions\030\006 \003(\0132\037.ibc.core.connect" +
      "ion.v1.VersionB \362\336\037\034yaml:\"counterparty_v" +
      "ersions\"R\024counterpartyVersions\022Z\n\014proof_" +
      "height\030\007 \001(\0132\032.ibc.core.client.v1.Height" +
      "B\033\310\336\037\000\362\336\037\023yaml:\"proof_height\"R\013proofHeig" +
      "ht\0224\n\nproof_init\030\010 \001(\014B\025\362\336\037\021yaml:\"proof_" +
      "init\"R\tproofInit\022:\n\014proof_client\030\t \001(\014B\027" +
      "\362\336\037\023yaml:\"proof_client\"R\013proofClient\022C\n\017" +
      "proof_consensus\030\n \001(\014B\032\362\336\037\026yaml:\"proof_c" +
      "onsensus\"R\016proofConsensus\022f\n\020consensus_h" +
      "eight\030\013 \001(\0132\032.ibc.core.client.v1.HeightB" +
      "\037\310\336\037\000\362\336\037\027yaml:\"consensus_height\"R\017consen" +
      "susHeight\022\026\n\006signer\030\014 \001(\tR\006signer:\010\210\240\037\000\350" +
      "\240\037\000\"\036\n\034MsgConnectionOpenTryResponse\"\341\005\n\024" +
      "MsgConnectionOpenAck\022=\n\rconnection_id\030\001 " +
      "\001(\tB\030\362\336\037\024yaml:\"connection_id\"R\014connectio" +
      "nId\022c\n\032counterparty_connection_id\030\002 \001(\tB" +
      "%\362\336\037!yaml:\"counterparty_connection_id\"R\030" +
      "counterpartyConnectionId\0229\n\007version\030\003 \001(" +
      "\0132\037.ibc.core.connection.v1.VersionR\007vers" +
      "ion\022P\n\014client_state\030\004 \001(\0132\024.google.proto" +
      "buf.AnyB\027\362\336\037\023yaml:\"client_state\"R\013client" +
      "State\022Z\n\014proof_height\030\005 \001(\0132\032.ibc.core.c" +
      "lient.v1.HeightB\033\310\336\037\000\362\336\037\023yaml:\"proof_hei" +
      "ght\"R\013proofHeight\0221\n\tproof_try\030\006 \001(\014B\024\362\336" +
      "\037\020yaml:\"proof_try\"R\010proofTry\022:\n\014proof_cl" +
      "ient\030\007 \001(\014B\027\362\336\037\023yaml:\"proof_client\"R\013pro" +
      "ofClient\022C\n\017proof_consensus\030\010 \001(\014B\032\362\336\037\026y" +
      "aml:\"proof_consensus\"R\016proofConsensus\022f\n" +
      "\020consensus_height\030\t \001(\0132\032.ibc.core.clien" +
      "t.v1.HeightB\037\310\336\037\000\362\336\037\027yaml:\"consensus_hei" +
      "ght\"R\017consensusHeight\022\026\n\006signer\030\n \001(\tR\006s" +
      "igner:\010\210\240\037\000\350\240\037\000\"\036\n\034MsgConnectionOpenAckR" +
      "esponse\"\212\002\n\030MsgConnectionOpenConfirm\022=\n\r" +
      "connection_id\030\001 \001(\tB\030\362\336\037\024yaml:\"connectio" +
      "n_id\"R\014connectionId\0221\n\tproof_ack\030\002 \001(\014B\024" +
      "\362\336\037\020yaml:\"proof_ack\"R\010proofAck\022Z\n\014proof_" +
      "height\030\003 \001(\0132\032.ibc.core.client.v1.Height" +
      "B\033\310\336\037\000\362\336\037\023yaml:\"proof_height\"R\013proofHeig" +
      "ht\022\026\n\006signer\030\004 \001(\tR\006signer:\010\210\240\037\000\350\240\037\000\"\"\n " +
      "MsgConnectionOpenConfirmResponse2\371\003\n\003Msg" +
      "\022z\n\022ConnectionOpenInit\022-.ibc.core.connec" +
      "tion.v1.MsgConnectionOpenInit\0325.ibc.core" +
      ".connection.v1.MsgConnectionOpenInitResp" +
      "onse\022w\n\021ConnectionOpenTry\022,.ibc.core.con" +
      "nection.v1.MsgConnectionOpenTry\0324.ibc.co" +
      "re.connection.v1.MsgConnectionOpenTryRes" +
      "ponse\022w\n\021ConnectionOpenAck\022,.ibc.core.co" +
      "nnection.v1.MsgConnectionOpenAck\0324.ibc.c" +
      "ore.connection.v1.MsgConnectionOpenAckRe" +
      "sponse\022\203\001\n\025ConnectionOpenConfirm\0220.ibc.c" +
      "ore.connection.v1.MsgConnectionOpenConfi" +
      "rm\0328.ibc.core.connection.v1.MsgConnectio" +
      "nOpenConfirmResponseB\344\001\n\032com.ibc.core.co" +
      "nnection.v1B\007TxProtoP\001ZBgithub.com/Asset" +
      "Mantle/modules/ibc/core/connection/v1;co" +
      "nnectionv1\242\002\003ICC\252\002\026Ibc.Core.Connection.V" +
      "1\312\002\026Ibc\\Core\\Connection\\V1\342\002\"Ibc\\Core\\Co" +
      "nnection\\V1\\GPBMetadata\352\002\031Ibc::Core::Con" +
      "nection::V1b\006proto3"
    };
    descriptor = com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
          com.gogoproto.GogoProto.getDescriptor(),
          com.google.protobuf.AnyProto.getDescriptor(),
          com.ibc.core.client.v1.ClientProto.getDescriptor(),
          com.ibc.core.connection.v1.ConnectionProto.getDescriptor(),
        });
    internal_static_ibc_core_connection_v1_MsgConnectionOpenInit_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_ibc_core_connection_v1_MsgConnectionOpenInit_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_ibc_core_connection_v1_MsgConnectionOpenInit_descriptor,
        new java.lang.String[] { "ClientId", "Counterparty", "Version", "DelayPeriod", "Signer", });
    internal_static_ibc_core_connection_v1_MsgConnectionOpenInitResponse_descriptor =
      getDescriptor().getMessageTypes().get(1);
    internal_static_ibc_core_connection_v1_MsgConnectionOpenInitResponse_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_ibc_core_connection_v1_MsgConnectionOpenInitResponse_descriptor,
        new java.lang.String[] { });
    internal_static_ibc_core_connection_v1_MsgConnectionOpenTry_descriptor =
      getDescriptor().getMessageTypes().get(2);
    internal_static_ibc_core_connection_v1_MsgConnectionOpenTry_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_ibc_core_connection_v1_MsgConnectionOpenTry_descriptor,
        new java.lang.String[] { "ClientId", "PreviousConnectionId", "ClientState", "Counterparty", "DelayPeriod", "CounterpartyVersions", "ProofHeight", "ProofInit", "ProofClient", "ProofConsensus", "ConsensusHeight", "Signer", });
    internal_static_ibc_core_connection_v1_MsgConnectionOpenTryResponse_descriptor =
      getDescriptor().getMessageTypes().get(3);
    internal_static_ibc_core_connection_v1_MsgConnectionOpenTryResponse_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_ibc_core_connection_v1_MsgConnectionOpenTryResponse_descriptor,
        new java.lang.String[] { });
    internal_static_ibc_core_connection_v1_MsgConnectionOpenAck_descriptor =
      getDescriptor().getMessageTypes().get(4);
    internal_static_ibc_core_connection_v1_MsgConnectionOpenAck_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_ibc_core_connection_v1_MsgConnectionOpenAck_descriptor,
        new java.lang.String[] { "ConnectionId", "CounterpartyConnectionId", "Version", "ClientState", "ProofHeight", "ProofTry", "ProofClient", "ProofConsensus", "ConsensusHeight", "Signer", });
    internal_static_ibc_core_connection_v1_MsgConnectionOpenAckResponse_descriptor =
      getDescriptor().getMessageTypes().get(5);
    internal_static_ibc_core_connection_v1_MsgConnectionOpenAckResponse_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_ibc_core_connection_v1_MsgConnectionOpenAckResponse_descriptor,
        new java.lang.String[] { });
    internal_static_ibc_core_connection_v1_MsgConnectionOpenConfirm_descriptor =
      getDescriptor().getMessageTypes().get(6);
    internal_static_ibc_core_connection_v1_MsgConnectionOpenConfirm_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_ibc_core_connection_v1_MsgConnectionOpenConfirm_descriptor,
        new java.lang.String[] { "ConnectionId", "ProofAck", "ProofHeight", "Signer", });
    internal_static_ibc_core_connection_v1_MsgConnectionOpenConfirmResponse_descriptor =
      getDescriptor().getMessageTypes().get(7);
    internal_static_ibc_core_connection_v1_MsgConnectionOpenConfirmResponse_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_ibc_core_connection_v1_MsgConnectionOpenConfirmResponse_descriptor,
        new java.lang.String[] { });
    com.google.protobuf.ExtensionRegistry registry =
        com.google.protobuf.ExtensionRegistry.newInstance();
    registry.add(com.gogoproto.GogoProto.equal);
    registry.add(com.gogoproto.GogoProto.goprotoGetters);
    registry.add(com.gogoproto.GogoProto.moretags);
    registry.add(com.gogoproto.GogoProto.nullable);
    com.google.protobuf.Descriptors.FileDescriptor
        .internalUpdateFileDescriptor(descriptor, registry);
    com.gogoproto.GogoProto.getDescriptor();
    com.google.protobuf.AnyProto.getDescriptor();
    com.ibc.core.client.v1.ClientProto.getDescriptor();
    com.ibc.core.connection.v1.ConnectionProto.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}
