// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: google/api/http.proto

package com.google.api;

public final class HttpProto {
  private HttpProto() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_google_api_Http_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_google_api_Http_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_google_api_HttpRule_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_google_api_HttpRule_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_google_api_CustomHttpPattern_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_google_api_CustomHttpPattern_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\025google/api/http.proto\022\ngoogle.api\"y\n\004H" +
      "ttp\022*\n\005rules\030\001 \003(\0132\024.google.api.HttpRule" +
      "R\005rules\022E\n\037fully_decode_reserved_expansi" +
      "on\030\002 \001(\010R\034fullyDecodeReservedExpansion\"\332" +
      "\002\n\010HttpRule\022\032\n\010selector\030\001 \001(\tR\010selector\022" +
      "\022\n\003get\030\002 \001(\tH\000R\003get\022\022\n\003put\030\003 \001(\tH\000R\003put\022" +
      "\024\n\004post\030\004 \001(\tH\000R\004post\022\030\n\006delete\030\005 \001(\tH\000R" +
      "\006delete\022\026\n\005patch\030\006 \001(\tH\000R\005patch\0227\n\006custo" +
      "m\030\010 \001(\0132\035.google.api.CustomHttpPatternH\000" +
      "R\006custom\022\022\n\004body\030\007 \001(\tR\004body\022#\n\rresponse" +
      "_body\030\014 \001(\tR\014responseBody\022E\n\023additional_" +
      "bindings\030\013 \003(\0132\024.google.api.HttpRuleR\022ad" +
      "ditionalBindingsB\t\n\007pattern\";\n\021CustomHtt" +
      "pPattern\022\022\n\004kind\030\001 \001(\tR\004kind\022\022\n\004path\030\002 \001" +
      "(\tR\004pathB\222\001\n\016com.google.apiB\tHttpProtoP\001" +
      "Z)github.com/AssetMantle/modules/google/" +
      "api\370\001\001\242\002\003GAX\252\002\nGoogle.Api\312\002\nGoogle\\Api\342\002" +
      "\026Google\\Api\\GPBMetadata\352\002\013Google::Apib\006p" +
      "roto3"
    };
    descriptor = com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
        });
    internal_static_google_api_Http_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_google_api_Http_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_google_api_Http_descriptor,
        new java.lang.String[] { "Rules", "FullyDecodeReservedExpansion", });
    internal_static_google_api_HttpRule_descriptor =
      getDescriptor().getMessageTypes().get(1);
    internal_static_google_api_HttpRule_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_google_api_HttpRule_descriptor,
        new java.lang.String[] { "Selector", "Get", "Put", "Post", "Delete", "Patch", "Custom", "Body", "ResponseBody", "AdditionalBindings", "Pattern", });
    internal_static_google_api_CustomHttpPattern_descriptor =
      getDescriptor().getMessageTypes().get(2);
    internal_static_google_api_CustomHttpPattern_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_google_api_CustomHttpPattern_descriptor,
        new java.lang.String[] { "Kind", "Path", });
  }

  // @@protoc_insertion_point(outer_class_scope)
}
