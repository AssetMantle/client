// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: classifications/record/record.proto

package com.assetmantle.modules.classifications.record;

public final class RecordProto {
  private RecordProto() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_assetmantle_modules_classifications_record_Record_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_assetmantle_modules_classifications_record_Record_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n#classifications/record/record.proto\022*a" +
      "ssetmantle.modules.classifications.recor" +
      "d\032\035classifications/key/key.proto\032\'classi" +
      "fications/mappable/mappable.proto\032\024gogop" +
      "roto/gogo.proto\"\242\001\n\006Record\022>\n\003key\030\001 \001(\0132" +
      ",.assetmantle.modules.classifications.ke" +
      "y.KeyR\003key\022R\n\010mappable\030\002 \001(\01326.assetmant" +
      "le.modules.classifications.mappable.Mapp" +
      "ableR\010mappable:\004\210\240\037\000B\302\002\n.com.assetmantle" +
      ".modules.classifications.recordB\013RecordP" +
      "rotoP\001Z7github.com/AssetMantle/modules/x" +
      "/classifications/record\242\002\004AMCR\252\002*Assetma" +
      "ntle.Modules.Classifications.Record\312\002*As" +
      "setmantle\\Modules\\Classifications\\Record" +
      "\342\0026Assetmantle\\Modules\\Classifications\\R" +
      "ecord\\GPBMetadata\352\002-Assetmantle::Modules" +
      "::Classifications::Recordb\006proto3"
    };
    descriptor = com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
          com.assetmantle.modules.classifications.key.KeyProto.getDescriptor(),
          com.assetmantle.modules.classifications.mappable.MappableProto.getDescriptor(),
          com.gogoproto.GogoProto.getDescriptor(),
        });
    internal_static_assetmantle_modules_classifications_record_Record_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_assetmantle_modules_classifications_record_Record_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_assetmantle_modules_classifications_record_Record_descriptor,
        new java.lang.String[] { "Key", "Mappable", });
    com.google.protobuf.ExtensionRegistry registry =
        com.google.protobuf.ExtensionRegistry.newInstance();
    registry.add(com.gogoproto.GogoProto.goprotoGetters);
    com.google.protobuf.Descriptors.FileDescriptor
        .internalUpdateFileDescriptor(descriptor, registry);
    com.assetmantle.modules.classifications.key.KeyProto.getDescriptor();
    com.assetmantle.modules.classifications.mappable.MappableProto.getDescriptor();
    com.gogoproto.GogoProto.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}