// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: cosmos/upgrade/v1beta1/query.proto

package com.cosmos.upgrade.v1beta1;

public final class QueryProto {
  private QueryProto() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_cosmos_upgrade_v1beta1_QueryCurrentPlanRequest_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_cosmos_upgrade_v1beta1_QueryCurrentPlanRequest_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_cosmos_upgrade_v1beta1_QueryCurrentPlanResponse_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_cosmos_upgrade_v1beta1_QueryCurrentPlanResponse_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_cosmos_upgrade_v1beta1_QueryAppliedPlanRequest_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_cosmos_upgrade_v1beta1_QueryAppliedPlanRequest_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_cosmos_upgrade_v1beta1_QueryAppliedPlanResponse_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_cosmos_upgrade_v1beta1_QueryAppliedPlanResponse_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_cosmos_upgrade_v1beta1_QueryUpgradedConsensusStateRequest_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_cosmos_upgrade_v1beta1_QueryUpgradedConsensusStateRequest_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_cosmos_upgrade_v1beta1_QueryUpgradedConsensusStateResponse_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_cosmos_upgrade_v1beta1_QueryUpgradedConsensusStateResponse_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_cosmos_upgrade_v1beta1_QueryModuleVersionsRequest_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_cosmos_upgrade_v1beta1_QueryModuleVersionsRequest_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_cosmos_upgrade_v1beta1_QueryModuleVersionsResponse_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_cosmos_upgrade_v1beta1_QueryModuleVersionsResponse_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\"cosmos/upgrade/v1beta1/query.proto\022\026co" +
      "smos.upgrade.v1beta1\032\031google/protobuf/an" +
      "y.proto\032\034google/api/annotations.proto\032$c" +
      "osmos/upgrade/v1beta1/upgrade.proto\"\031\n\027Q" +
      "ueryCurrentPlanRequest\"L\n\030QueryCurrentPl" +
      "anResponse\0220\n\004plan\030\001 \001(\0132\034.cosmos.upgrad" +
      "e.v1beta1.PlanR\004plan\"-\n\027QueryAppliedPlan" +
      "Request\022\022\n\004name\030\001 \001(\tR\004name\"2\n\030QueryAppl" +
      "iedPlanResponse\022\026\n\006height\030\001 \001(\003R\006height\"" +
      "I\n\"QueryUpgradedConsensusStateRequest\022\037\n" +
      "\013last_height\030\001 \001(\003R\nlastHeight:\002\030\001\"i\n#Qu" +
      "eryUpgradedConsensusStateResponse\0228\n\030upg" +
      "raded_consensus_state\030\002 \001(\014R\026upgradedCon" +
      "sensusState:\002\030\001J\004\010\001\020\002\"=\n\032QueryModuleVers" +
      "ionsRequest\022\037\n\013module_name\030\001 \001(\tR\nmodule" +
      "Name\"m\n\033QueryModuleVersionsResponse\022N\n\017m" +
      "odule_versions\030\001 \003(\0132%.cosmos.upgrade.v1" +
      "beta1.ModuleVersionR\016moduleVersions2\334\005\n\005" +
      "Query\022\236\001\n\013CurrentPlan\022/.cosmos.upgrade.v" +
      "1beta1.QueryCurrentPlanRequest\0320.cosmos." +
      "upgrade.v1beta1.QueryCurrentPlanResponse" +
      "\",\202\323\344\223\002&\022$/cosmos/upgrade/v1beta1/curren" +
      "t_plan\022\245\001\n\013AppliedPlan\022/.cosmos.upgrade." +
      "v1beta1.QueryAppliedPlanRequest\0320.cosmos" +
      ".upgrade.v1beta1.QueryAppliedPlanRespons" +
      "e\"3\202\323\344\223\002-\022+/cosmos/upgrade/v1beta1/appli" +
      "ed_plan/{name}\022\334\001\n\026UpgradedConsensusStat" +
      "e\022:.cosmos.upgrade.v1beta1.QueryUpgraded" +
      "ConsensusStateRequest\032;.cosmos.upgrade.v" +
      "1beta1.QueryUpgradedConsensusStateRespon" +
      "se\"I\210\002\001\202\323\344\223\002@\022>/cosmos/upgrade/v1beta1/u" +
      "pgraded_consensus_state/{last_height}\022\252\001" +
      "\n\016ModuleVersions\0222.cosmos.upgrade.v1beta" +
      "1.QueryModuleVersionsRequest\0323.cosmos.up" +
      "grade.v1beta1.QueryModuleVersionsRespons" +
      "e\"/\202\323\344\223\002)\022\'/cosmos/upgrade/v1beta1/modul" +
      "e_versionsB\350\001\n\032com.cosmos.upgrade.v1beta" +
      "1B\nQueryProtoP\001ZDgithub.com/AssetMantle/" +
      "modules/cosmos/upgrade/v1beta1;upgradev1" +
      "beta1\242\002\003CUX\252\002\026Cosmos.Upgrade.V1beta1\312\002\026C" +
      "osmos\\Upgrade\\V1beta1\342\002\"Cosmos\\Upgrade\\V" +
      "1beta1\\GPBMetadata\352\002\030Cosmos::Upgrade::V1" +
      "beta1b\006proto3"
    };
    descriptor = com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
          com.google.protobuf.AnyProto.getDescriptor(),
          com.google.api.AnnotationsProto.getDescriptor(),
          com.cosmos.upgrade.v1beta1.UpgradeProto.getDescriptor(),
        });
    internal_static_cosmos_upgrade_v1beta1_QueryCurrentPlanRequest_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_cosmos_upgrade_v1beta1_QueryCurrentPlanRequest_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_cosmos_upgrade_v1beta1_QueryCurrentPlanRequest_descriptor,
        new java.lang.String[] { });
    internal_static_cosmos_upgrade_v1beta1_QueryCurrentPlanResponse_descriptor =
      getDescriptor().getMessageTypes().get(1);
    internal_static_cosmos_upgrade_v1beta1_QueryCurrentPlanResponse_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_cosmos_upgrade_v1beta1_QueryCurrentPlanResponse_descriptor,
        new java.lang.String[] { "Plan", });
    internal_static_cosmos_upgrade_v1beta1_QueryAppliedPlanRequest_descriptor =
      getDescriptor().getMessageTypes().get(2);
    internal_static_cosmos_upgrade_v1beta1_QueryAppliedPlanRequest_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_cosmos_upgrade_v1beta1_QueryAppliedPlanRequest_descriptor,
        new java.lang.String[] { "Name", });
    internal_static_cosmos_upgrade_v1beta1_QueryAppliedPlanResponse_descriptor =
      getDescriptor().getMessageTypes().get(3);
    internal_static_cosmos_upgrade_v1beta1_QueryAppliedPlanResponse_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_cosmos_upgrade_v1beta1_QueryAppliedPlanResponse_descriptor,
        new java.lang.String[] { "Height", });
    internal_static_cosmos_upgrade_v1beta1_QueryUpgradedConsensusStateRequest_descriptor =
      getDescriptor().getMessageTypes().get(4);
    internal_static_cosmos_upgrade_v1beta1_QueryUpgradedConsensusStateRequest_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_cosmos_upgrade_v1beta1_QueryUpgradedConsensusStateRequest_descriptor,
        new java.lang.String[] { "LastHeight", });
    internal_static_cosmos_upgrade_v1beta1_QueryUpgradedConsensusStateResponse_descriptor =
      getDescriptor().getMessageTypes().get(5);
    internal_static_cosmos_upgrade_v1beta1_QueryUpgradedConsensusStateResponse_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_cosmos_upgrade_v1beta1_QueryUpgradedConsensusStateResponse_descriptor,
        new java.lang.String[] { "UpgradedConsensusState", });
    internal_static_cosmos_upgrade_v1beta1_QueryModuleVersionsRequest_descriptor =
      getDescriptor().getMessageTypes().get(6);
    internal_static_cosmos_upgrade_v1beta1_QueryModuleVersionsRequest_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_cosmos_upgrade_v1beta1_QueryModuleVersionsRequest_descriptor,
        new java.lang.String[] { "ModuleName", });
    internal_static_cosmos_upgrade_v1beta1_QueryModuleVersionsResponse_descriptor =
      getDescriptor().getMessageTypes().get(7);
    internal_static_cosmos_upgrade_v1beta1_QueryModuleVersionsResponse_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_cosmos_upgrade_v1beta1_QueryModuleVersionsResponse_descriptor,
        new java.lang.String[] { "ModuleVersions", });
    com.google.protobuf.ExtensionRegistry registry =
        com.google.protobuf.ExtensionRegistry.newInstance();
    registry.add(com.google.api.AnnotationsProto.http);
    com.google.protobuf.Descriptors.FileDescriptor
        .internalUpdateFileDescriptor(descriptor, registry);
    com.google.protobuf.AnyProto.getDescriptor();
    com.google.api.AnnotationsProto.getDescriptor();
    com.cosmos.upgrade.v1beta1.UpgradeProto.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}
