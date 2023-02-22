// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: cosmos/upgrade/v1beta1/query.proto

package com.cosmos.upgrade.v1beta1;

/**
 * <pre>
 * QueryAppliedPlanResponse is the response type for the Query/AppliedPlan RPC
 * method.
 * </pre>
 *
 * Protobuf type {@code cosmos.upgrade.v1beta1.QueryAppliedPlanResponse}
 */
public final class QueryAppliedPlanResponse extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:cosmos.upgrade.v1beta1.QueryAppliedPlanResponse)
    QueryAppliedPlanResponseOrBuilder {
private static final long serialVersionUID = 0L;
  // Use QueryAppliedPlanResponse.newBuilder() to construct.
  private QueryAppliedPlanResponse(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private QueryAppliedPlanResponse() {
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new QueryAppliedPlanResponse();
  }

  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return com.cosmos.upgrade.v1beta1.QueryProto.internal_static_cosmos_upgrade_v1beta1_QueryAppliedPlanResponse_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return com.cosmos.upgrade.v1beta1.QueryProto.internal_static_cosmos_upgrade_v1beta1_QueryAppliedPlanResponse_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            com.cosmos.upgrade.v1beta1.QueryAppliedPlanResponse.class, com.cosmos.upgrade.v1beta1.QueryAppliedPlanResponse.Builder.class);
  }

  public static final int HEIGHT_FIELD_NUMBER = 1;
  private long height_ = 0L;
  /**
   * <pre>
   * height is the block height at which the plan was applied.
   * </pre>
   *
   * <code>int64 height = 1 [json_name = "height"];</code>
   * @return The height.
   */
  @java.lang.Override
  public long getHeight() {
    return height_;
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
    if (height_ != 0L) {
      output.writeInt64(1, height_);
    }
    getUnknownFields().writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (height_ != 0L) {
      size += com.google.protobuf.CodedOutputStream
        .computeInt64Size(1, height_);
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
    if (!(obj instanceof com.cosmos.upgrade.v1beta1.QueryAppliedPlanResponse)) {
      return super.equals(obj);
    }
    com.cosmos.upgrade.v1beta1.QueryAppliedPlanResponse other = (com.cosmos.upgrade.v1beta1.QueryAppliedPlanResponse) obj;

    if (getHeight()
        != other.getHeight()) return false;
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
    hash = (37 * hash) + HEIGHT_FIELD_NUMBER;
    hash = (53 * hash) + com.google.protobuf.Internal.hashLong(
        getHeight());
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static com.cosmos.upgrade.v1beta1.QueryAppliedPlanResponse parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.cosmos.upgrade.v1beta1.QueryAppliedPlanResponse parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.cosmos.upgrade.v1beta1.QueryAppliedPlanResponse parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.cosmos.upgrade.v1beta1.QueryAppliedPlanResponse parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.cosmos.upgrade.v1beta1.QueryAppliedPlanResponse parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.cosmos.upgrade.v1beta1.QueryAppliedPlanResponse parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.cosmos.upgrade.v1beta1.QueryAppliedPlanResponse parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.cosmos.upgrade.v1beta1.QueryAppliedPlanResponse parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.cosmos.upgrade.v1beta1.QueryAppliedPlanResponse parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static com.cosmos.upgrade.v1beta1.QueryAppliedPlanResponse parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.cosmos.upgrade.v1beta1.QueryAppliedPlanResponse parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.cosmos.upgrade.v1beta1.QueryAppliedPlanResponse parseFrom(
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
  public static Builder newBuilder(com.cosmos.upgrade.v1beta1.QueryAppliedPlanResponse prototype) {
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
   * <pre>
   * QueryAppliedPlanResponse is the response type for the Query/AppliedPlan RPC
   * method.
   * </pre>
   *
   * Protobuf type {@code cosmos.upgrade.v1beta1.QueryAppliedPlanResponse}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:cosmos.upgrade.v1beta1.QueryAppliedPlanResponse)
      com.cosmos.upgrade.v1beta1.QueryAppliedPlanResponseOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return com.cosmos.upgrade.v1beta1.QueryProto.internal_static_cosmos_upgrade_v1beta1_QueryAppliedPlanResponse_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return com.cosmos.upgrade.v1beta1.QueryProto.internal_static_cosmos_upgrade_v1beta1_QueryAppliedPlanResponse_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              com.cosmos.upgrade.v1beta1.QueryAppliedPlanResponse.class, com.cosmos.upgrade.v1beta1.QueryAppliedPlanResponse.Builder.class);
    }

    // Construct using com.cosmos.upgrade.v1beta1.QueryAppliedPlanResponse.newBuilder()
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
      height_ = 0L;
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return com.cosmos.upgrade.v1beta1.QueryProto.internal_static_cosmos_upgrade_v1beta1_QueryAppliedPlanResponse_descriptor;
    }

    @java.lang.Override
    public com.cosmos.upgrade.v1beta1.QueryAppliedPlanResponse getDefaultInstanceForType() {
      return com.cosmos.upgrade.v1beta1.QueryAppliedPlanResponse.getDefaultInstance();
    }

    @java.lang.Override
    public com.cosmos.upgrade.v1beta1.QueryAppliedPlanResponse build() {
      com.cosmos.upgrade.v1beta1.QueryAppliedPlanResponse result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public com.cosmos.upgrade.v1beta1.QueryAppliedPlanResponse buildPartial() {
      com.cosmos.upgrade.v1beta1.QueryAppliedPlanResponse result = new com.cosmos.upgrade.v1beta1.QueryAppliedPlanResponse(this);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartial0(com.cosmos.upgrade.v1beta1.QueryAppliedPlanResponse result) {
      int from_bitField0_ = bitField0_;
      if (((from_bitField0_ & 0x00000001) != 0)) {
        result.height_ = height_;
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
      if (other instanceof com.cosmos.upgrade.v1beta1.QueryAppliedPlanResponse) {
        return mergeFrom((com.cosmos.upgrade.v1beta1.QueryAppliedPlanResponse)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(com.cosmos.upgrade.v1beta1.QueryAppliedPlanResponse other) {
      if (other == com.cosmos.upgrade.v1beta1.QueryAppliedPlanResponse.getDefaultInstance()) return this;
      if (other.getHeight() != 0L) {
        setHeight(other.getHeight());
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
            case 8: {
              height_ = input.readInt64();
              bitField0_ |= 0x00000001;
              break;
            } // case 8
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

    private long height_ ;
    /**
     * <pre>
     * height is the block height at which the plan was applied.
     * </pre>
     *
     * <code>int64 height = 1 [json_name = "height"];</code>
     * @return The height.
     */
    @java.lang.Override
    public long getHeight() {
      return height_;
    }
    /**
     * <pre>
     * height is the block height at which the plan was applied.
     * </pre>
     *
     * <code>int64 height = 1 [json_name = "height"];</code>
     * @param value The height to set.
     * @return This builder for chaining.
     */
    public Builder setHeight(long value) {

      height_ = value;
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * height is the block height at which the plan was applied.
     * </pre>
     *
     * <code>int64 height = 1 [json_name = "height"];</code>
     * @return This builder for chaining.
     */
    public Builder clearHeight() {
      bitField0_ = (bitField0_ & ~0x00000001);
      height_ = 0L;
      onChanged();
      return this;
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


    // @@protoc_insertion_point(builder_scope:cosmos.upgrade.v1beta1.QueryAppliedPlanResponse)
  }

  // @@protoc_insertion_point(class_scope:cosmos.upgrade.v1beta1.QueryAppliedPlanResponse)
  private static final com.cosmos.upgrade.v1beta1.QueryAppliedPlanResponse DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new com.cosmos.upgrade.v1beta1.QueryAppliedPlanResponse();
  }

  public static com.cosmos.upgrade.v1beta1.QueryAppliedPlanResponse getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<QueryAppliedPlanResponse>
      PARSER = new com.google.protobuf.AbstractParser<QueryAppliedPlanResponse>() {
    @java.lang.Override
    public QueryAppliedPlanResponse parsePartialFrom(
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

  public static com.google.protobuf.Parser<QueryAppliedPlanResponse> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<QueryAppliedPlanResponse> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public com.cosmos.upgrade.v1beta1.QueryAppliedPlanResponse getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

