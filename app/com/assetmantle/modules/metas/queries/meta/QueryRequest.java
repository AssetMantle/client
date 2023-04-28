// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: metas/queries/meta/query_request.proto

package com.assetmantle.modules.metas.queries.meta;

/**
 * Protobuf type {@code assetmantle.modules.metas.queries.meta.QueryRequest}
 */
public final class QueryRequest extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:assetmantle.modules.metas.queries.meta.QueryRequest)
    QueryRequestOrBuilder {
private static final long serialVersionUID = 0L;
  // Use QueryRequest.newBuilder() to construct.
  private QueryRequest(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private QueryRequest() {
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new QueryRequest();
  }

  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return com.assetmantle.modules.metas.queries.meta.QueryRequestProto.internal_static_assetmantle_modules_metas_queries_meta_QueryRequest_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return com.assetmantle.modules.metas.queries.meta.QueryRequestProto.internal_static_assetmantle_modules_metas_queries_meta_QueryRequest_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            com.assetmantle.modules.metas.queries.meta.QueryRequest.class, com.assetmantle.modules.metas.queries.meta.QueryRequest.Builder.class);
  }

  public static final int DATA_I_D_FIELD_NUMBER = 1;
  private com.assetmantle.schema.ids.base.DataID dataID_;
  /**
   * <code>.assetmantle.schema.ids.base.DataID data_i_d = 1 [json_name = "dataID"];</code>
   * @return Whether the dataID field is set.
   */
  @java.lang.Override
  public boolean hasDataID() {
    return dataID_ != null;
  }
  /**
   * <code>.assetmantle.schema.ids.base.DataID data_i_d = 1 [json_name = "dataID"];</code>
   * @return The dataID.
   */
  @java.lang.Override
  public com.assetmantle.schema.ids.base.DataID getDataID() {
    return dataID_ == null ? com.assetmantle.schema.ids.base.DataID.getDefaultInstance() : dataID_;
  }
  /**
   * <code>.assetmantle.schema.ids.base.DataID data_i_d = 1 [json_name = "dataID"];</code>
   */
  @java.lang.Override
  public com.assetmantle.schema.ids.base.DataIDOrBuilder getDataIDOrBuilder() {
    return dataID_ == null ? com.assetmantle.schema.ids.base.DataID.getDefaultInstance() : dataID_;
  }

  private byte memoizedIsInitialized = -1;
  @java.lang.Override
  public final boolean isInitialized() {
    byte isInitialized = memoizedIsInitialized;
    if (isInitialized == 1) return true;
    if (isInitialized == 0) return false;

    memoizedIsInitialized = 1;
    return true;
  }

  @java.lang.Override
  public void writeTo(com.google.protobuf.CodedOutputStream output)
                      throws java.io.IOException {
    if (dataID_ != null) {
      output.writeMessage(1, getDataID());
    }
    getUnknownFields().writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (dataID_ != null) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(1, getDataID());
    }
    size += getUnknownFields().getSerializedSize();
    memoizedSize = size;
    return size;
  }

  @java.lang.Override
  public boolean equals(final java.lang.Object obj) {
    if (obj == this) {
     return true;
    }
    if (!(obj instanceof com.assetmantle.modules.metas.queries.meta.QueryRequest)) {
      return super.equals(obj);
    }
    com.assetmantle.modules.metas.queries.meta.QueryRequest other = (com.assetmantle.modules.metas.queries.meta.QueryRequest) obj;

    if (hasDataID() != other.hasDataID()) return false;
    if (hasDataID()) {
      if (!getDataID()
          .equals(other.getDataID())) return false;
    }
    if (!getUnknownFields().equals(other.getUnknownFields())) return false;
    return true;
  }

  @java.lang.Override
  public int hashCode() {
    if (memoizedHashCode != 0) {
      return memoizedHashCode;
    }
    int hash = 41;
    hash = (19 * hash) + getDescriptor().hashCode();
    if (hasDataID()) {
      hash = (37 * hash) + DATA_I_D_FIELD_NUMBER;
      hash = (53 * hash) + getDataID().hashCode();
    }
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static com.assetmantle.modules.metas.queries.meta.QueryRequest parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.assetmantle.modules.metas.queries.meta.QueryRequest parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.assetmantle.modules.metas.queries.meta.QueryRequest parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.assetmantle.modules.metas.queries.meta.QueryRequest parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.assetmantle.modules.metas.queries.meta.QueryRequest parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.assetmantle.modules.metas.queries.meta.QueryRequest parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.assetmantle.modules.metas.queries.meta.QueryRequest parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.assetmantle.modules.metas.queries.meta.QueryRequest parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.assetmantle.modules.metas.queries.meta.QueryRequest parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static com.assetmantle.modules.metas.queries.meta.QueryRequest parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.assetmantle.modules.metas.queries.meta.QueryRequest parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.assetmantle.modules.metas.queries.meta.QueryRequest parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  @java.lang.Override
  public Builder newBuilderForType() { return newBuilder(); }
  public static Builder newBuilder() {
    return DEFAULT_INSTANCE.toBuilder();
  }
  public static Builder newBuilder(com.assetmantle.modules.metas.queries.meta.QueryRequest prototype) {
    return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
  }
  @java.lang.Override
  public Builder toBuilder() {
    return this == DEFAULT_INSTANCE
        ? new Builder() : new Builder().mergeFrom(this);
  }

  @java.lang.Override
  protected Builder newBuilderForType(
      com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
    Builder builder = new Builder(parent);
    return builder;
  }
  /**
   * Protobuf type {@code assetmantle.modules.metas.queries.meta.QueryRequest}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:assetmantle.modules.metas.queries.meta.QueryRequest)
      com.assetmantle.modules.metas.queries.meta.QueryRequestOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return com.assetmantle.modules.metas.queries.meta.QueryRequestProto.internal_static_assetmantle_modules_metas_queries_meta_QueryRequest_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return com.assetmantle.modules.metas.queries.meta.QueryRequestProto.internal_static_assetmantle_modules_metas_queries_meta_QueryRequest_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              com.assetmantle.modules.metas.queries.meta.QueryRequest.class, com.assetmantle.modules.metas.queries.meta.QueryRequest.Builder.class);
    }

    // Construct using com.assetmantle.modules.metas.queries.meta.QueryRequest.newBuilder()
    private Builder() {

    }

    private Builder(
        com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
      super(parent);

    }
    @java.lang.Override
    public Builder clear() {
      super.clear();
      bitField0_ = 0;
      dataID_ = null;
      if (dataIDBuilder_ != null) {
        dataIDBuilder_.dispose();
        dataIDBuilder_ = null;
      }
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return com.assetmantle.modules.metas.queries.meta.QueryRequestProto.internal_static_assetmantle_modules_metas_queries_meta_QueryRequest_descriptor;
    }

    @java.lang.Override
    public com.assetmantle.modules.metas.queries.meta.QueryRequest getDefaultInstanceForType() {
      return com.assetmantle.modules.metas.queries.meta.QueryRequest.getDefaultInstance();
    }

    @java.lang.Override
    public com.assetmantle.modules.metas.queries.meta.QueryRequest build() {
      com.assetmantle.modules.metas.queries.meta.QueryRequest result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public com.assetmantle.modules.metas.queries.meta.QueryRequest buildPartial() {
      com.assetmantle.modules.metas.queries.meta.QueryRequest result = new com.assetmantle.modules.metas.queries.meta.QueryRequest(this);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartial0(com.assetmantle.modules.metas.queries.meta.QueryRequest result) {
      int from_bitField0_ = bitField0_;
      if (((from_bitField0_ & 0x00000001) != 0)) {
        result.dataID_ = dataIDBuilder_ == null
            ? dataID_
            : dataIDBuilder_.build();
      }
    }

    @java.lang.Override
    public Builder clone() {
      return super.clone();
    }
    @java.lang.Override
    public Builder setField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        java.lang.Object value) {
      return super.setField(field, value);
    }
    @java.lang.Override
    public Builder clearField(
        com.google.protobuf.Descriptors.FieldDescriptor field) {
      return super.clearField(field);
    }
    @java.lang.Override
    public Builder clearOneof(
        com.google.protobuf.Descriptors.OneofDescriptor oneof) {
      return super.clearOneof(oneof);
    }
    @java.lang.Override
    public Builder setRepeatedField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        int index, java.lang.Object value) {
      return super.setRepeatedField(field, index, value);
    }
    @java.lang.Override
    public Builder addRepeatedField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        java.lang.Object value) {
      return super.addRepeatedField(field, value);
    }
    @java.lang.Override
    public Builder mergeFrom(com.google.protobuf.Message other) {
      if (other instanceof com.assetmantle.modules.metas.queries.meta.QueryRequest) {
        return mergeFrom((com.assetmantle.modules.metas.queries.meta.QueryRequest)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(com.assetmantle.modules.metas.queries.meta.QueryRequest other) {
      if (other == com.assetmantle.modules.metas.queries.meta.QueryRequest.getDefaultInstance()) return this;
      if (other.hasDataID()) {
        mergeDataID(other.getDataID());
      }
      this.mergeUnknownFields(other.getUnknownFields());
      onChanged();
      return this;
    }

    @java.lang.Override
    public final boolean isInitialized() {
      return true;
    }

    @java.lang.Override
    public Builder mergeFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      if (extensionRegistry == null) {
        throw new java.lang.NullPointerException();
      }
      try {
        boolean done = false;
        while (!done) {
          int tag = input.readTag();
          switch (tag) {
            case 0:
              done = true;
              break;
            case 10: {
              input.readMessage(
                  getDataIDFieldBuilder().getBuilder(),
                  extensionRegistry);
              bitField0_ |= 0x00000001;
              break;
            } // case 10
            default: {
              if (!super.parseUnknownField(input, extensionRegistry, tag)) {
                done = true; // was an endgroup tag
              }
              break;
            } // default:
          } // switch (tag)
        } // while (!done)
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        throw e.unwrapIOException();
      } finally {
        onChanged();
      } // finally
      return this;
    }
    private int bitField0_;

    private com.assetmantle.schema.ids.base.DataID dataID_;
    private com.google.protobuf.SingleFieldBuilderV3<
        com.assetmantle.schema.ids.base.DataID, com.assetmantle.schema.ids.base.DataID.Builder, com.assetmantle.schema.ids.base.DataIDOrBuilder> dataIDBuilder_;
    /**
     * <code>.assetmantle.schema.ids.base.DataID data_i_d = 1 [json_name = "dataID"];</code>
     * @return Whether the dataID field is set.
     */
    public boolean hasDataID() {
      return ((bitField0_ & 0x00000001) != 0);
    }
    /**
     * <code>.assetmantle.schema.ids.base.DataID data_i_d = 1 [json_name = "dataID"];</code>
     * @return The dataID.
     */
    public com.assetmantle.schema.ids.base.DataID getDataID() {
      if (dataIDBuilder_ == null) {
        return dataID_ == null ? com.assetmantle.schema.ids.base.DataID.getDefaultInstance() : dataID_;
      } else {
        return dataIDBuilder_.getMessage();
      }
    }
    /**
     * <code>.assetmantle.schema.ids.base.DataID data_i_d = 1 [json_name = "dataID"];</code>
     */
    public Builder setDataID(com.assetmantle.schema.ids.base.DataID value) {
      if (dataIDBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        dataID_ = value;
      } else {
        dataIDBuilder_.setMessage(value);
      }
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <code>.assetmantle.schema.ids.base.DataID data_i_d = 1 [json_name = "dataID"];</code>
     */
    public Builder setDataID(
        com.assetmantle.schema.ids.base.DataID.Builder builderForValue) {
      if (dataIDBuilder_ == null) {
        dataID_ = builderForValue.build();
      } else {
        dataIDBuilder_.setMessage(builderForValue.build());
      }
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <code>.assetmantle.schema.ids.base.DataID data_i_d = 1 [json_name = "dataID"];</code>
     */
    public Builder mergeDataID(com.assetmantle.schema.ids.base.DataID value) {
      if (dataIDBuilder_ == null) {
        if (((bitField0_ & 0x00000001) != 0) &&
          dataID_ != null &&
          dataID_ != com.assetmantle.schema.ids.base.DataID.getDefaultInstance()) {
          getDataIDBuilder().mergeFrom(value);
        } else {
          dataID_ = value;
        }
      } else {
        dataIDBuilder_.mergeFrom(value);
      }
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <code>.assetmantle.schema.ids.base.DataID data_i_d = 1 [json_name = "dataID"];</code>
     */
    public Builder clearDataID() {
      bitField0_ = (bitField0_ & ~0x00000001);
      dataID_ = null;
      if (dataIDBuilder_ != null) {
        dataIDBuilder_.dispose();
        dataIDBuilder_ = null;
      }
      onChanged();
      return this;
    }
    /**
     * <code>.assetmantle.schema.ids.base.DataID data_i_d = 1 [json_name = "dataID"];</code>
     */
    public com.assetmantle.schema.ids.base.DataID.Builder getDataIDBuilder() {
      bitField0_ |= 0x00000001;
      onChanged();
      return getDataIDFieldBuilder().getBuilder();
    }
    /**
     * <code>.assetmantle.schema.ids.base.DataID data_i_d = 1 [json_name = "dataID"];</code>
     */
    public com.assetmantle.schema.ids.base.DataIDOrBuilder getDataIDOrBuilder() {
      if (dataIDBuilder_ != null) {
        return dataIDBuilder_.getMessageOrBuilder();
      } else {
        return dataID_ == null ?
            com.assetmantle.schema.ids.base.DataID.getDefaultInstance() : dataID_;
      }
    }
    /**
     * <code>.assetmantle.schema.ids.base.DataID data_i_d = 1 [json_name = "dataID"];</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
        com.assetmantle.schema.ids.base.DataID, com.assetmantle.schema.ids.base.DataID.Builder, com.assetmantle.schema.ids.base.DataIDOrBuilder> 
        getDataIDFieldBuilder() {
      if (dataIDBuilder_ == null) {
        dataIDBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
            com.assetmantle.schema.ids.base.DataID, com.assetmantle.schema.ids.base.DataID.Builder, com.assetmantle.schema.ids.base.DataIDOrBuilder>(
                getDataID(),
                getParentForChildren(),
                isClean());
        dataID_ = null;
      }
      return dataIDBuilder_;
    }
    @java.lang.Override
    public final Builder setUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.setUnknownFields(unknownFields);
    }

    @java.lang.Override
    public final Builder mergeUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.mergeUnknownFields(unknownFields);
    }


    // @@protoc_insertion_point(builder_scope:assetmantle.modules.metas.queries.meta.QueryRequest)
  }

  // @@protoc_insertion_point(class_scope:assetmantle.modules.metas.queries.meta.QueryRequest)
  private static final com.assetmantle.modules.metas.queries.meta.QueryRequest DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new com.assetmantle.modules.metas.queries.meta.QueryRequest();
  }

  public static com.assetmantle.modules.metas.queries.meta.QueryRequest getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<QueryRequest>
      PARSER = new com.google.protobuf.AbstractParser<QueryRequest>() {
    @java.lang.Override
    public QueryRequest parsePartialFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      Builder builder = newBuilder();
      try {
        builder.mergeFrom(input, extensionRegistry);
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        throw e.setUnfinishedMessage(builder.buildPartial());
      } catch (com.google.protobuf.UninitializedMessageException e) {
        throw e.asInvalidProtocolBufferException().setUnfinishedMessage(builder.buildPartial());
      } catch (java.io.IOException e) {
        throw new com.google.protobuf.InvalidProtocolBufferException(e)
            .setUnfinishedMessage(builder.buildPartial());
      }
      return builder.buildPartial();
    }
  };

  public static com.google.protobuf.Parser<QueryRequest> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<QueryRequest> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public com.assetmantle.modules.metas.queries.meta.QueryRequest getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

