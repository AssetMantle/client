// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: ibc/core/connection/v1/connection.proto

package com.ibc.core.connection.v1;

/**
 * <pre>
 * Params defines the set of Connection parameters.
 * </pre>
 *
 * Protobuf type {@code ibc.core.connection.v1.Params}
 */
public final class Params extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:ibc.core.connection.v1.Params)
    ParamsOrBuilder {
private static final long serialVersionUID = 0L;
  // Use Params.newBuilder() to construct.
  private Params(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private Params() {
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new Params();
  }

  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return com.ibc.core.connection.v1.ConnectionProto.internal_static_ibc_core_connection_v1_Params_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return com.ibc.core.connection.v1.ConnectionProto.internal_static_ibc_core_connection_v1_Params_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            com.ibc.core.connection.v1.Params.class, com.ibc.core.connection.v1.Params.Builder.class);
  }

  public static final int MAX_EXPECTED_TIME_PER_BLOCK_FIELD_NUMBER = 1;
  private long maxExpectedTimePerBlock_ = 0L;
  /**
   * <pre>
   * maximum expected time per block (in nanoseconds), used to enforce block delay. This parameter should reflect the
   * largest amount of time that the chain might reasonably take to produce the next block under normal operating
   * conditions. A safe choice is 3-5x the expected time per block.
   * </pre>
   *
   * <code>uint64 max_expected_time_per_block = 1 [json_name = "maxExpectedTimePerBlock", (.gogoproto.moretags) = "yaml:&#92;"max_expected_time_per_block&#92;""];</code>
   * @return The maxExpectedTimePerBlock.
   */
  @java.lang.Override
  public long getMaxExpectedTimePerBlock() {
    return maxExpectedTimePerBlock_;
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
    if (maxExpectedTimePerBlock_ != 0L) {
      output.writeUInt64(1, maxExpectedTimePerBlock_);
    }
    getUnknownFields().writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (maxExpectedTimePerBlock_ != 0L) {
      size += com.google.protobuf.CodedOutputStream
        .computeUInt64Size(1, maxExpectedTimePerBlock_);
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
    if (!(obj instanceof com.ibc.core.connection.v1.Params)) {
      return super.equals(obj);
    }
    com.ibc.core.connection.v1.Params other = (com.ibc.core.connection.v1.Params) obj;

    if (getMaxExpectedTimePerBlock()
        != other.getMaxExpectedTimePerBlock()) return false;
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
    hash = (37 * hash) + MAX_EXPECTED_TIME_PER_BLOCK_FIELD_NUMBER;
    hash = (53 * hash) + com.google.protobuf.Internal.hashLong(
        getMaxExpectedTimePerBlock());
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static com.ibc.core.connection.v1.Params parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.ibc.core.connection.v1.Params parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.ibc.core.connection.v1.Params parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.ibc.core.connection.v1.Params parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.ibc.core.connection.v1.Params parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.ibc.core.connection.v1.Params parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.ibc.core.connection.v1.Params parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.ibc.core.connection.v1.Params parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.ibc.core.connection.v1.Params parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static com.ibc.core.connection.v1.Params parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.ibc.core.connection.v1.Params parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.ibc.core.connection.v1.Params parseFrom(
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
  public static Builder newBuilder(com.ibc.core.connection.v1.Params prototype) {
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
   * Params defines the set of Connection parameters.
   * </pre>
   *
   * Protobuf type {@code ibc.core.connection.v1.Params}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:ibc.core.connection.v1.Params)
      com.ibc.core.connection.v1.ParamsOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return com.ibc.core.connection.v1.ConnectionProto.internal_static_ibc_core_connection_v1_Params_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return com.ibc.core.connection.v1.ConnectionProto.internal_static_ibc_core_connection_v1_Params_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              com.ibc.core.connection.v1.Params.class, com.ibc.core.connection.v1.Params.Builder.class);
    }

    // Construct using com.ibc.core.connection.v1.Params.newBuilder()
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
      maxExpectedTimePerBlock_ = 0L;
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return com.ibc.core.connection.v1.ConnectionProto.internal_static_ibc_core_connection_v1_Params_descriptor;
    }

    @java.lang.Override
    public com.ibc.core.connection.v1.Params getDefaultInstanceForType() {
      return com.ibc.core.connection.v1.Params.getDefaultInstance();
    }

    @java.lang.Override
    public com.ibc.core.connection.v1.Params build() {
      com.ibc.core.connection.v1.Params result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public com.ibc.core.connection.v1.Params buildPartial() {
      com.ibc.core.connection.v1.Params result = new com.ibc.core.connection.v1.Params(this);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartial0(com.ibc.core.connection.v1.Params result) {
      int from_bitField0_ = bitField0_;
      if (((from_bitField0_ & 0x00000001) != 0)) {
        result.maxExpectedTimePerBlock_ = maxExpectedTimePerBlock_;
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
      if (other instanceof com.ibc.core.connection.v1.Params) {
        return mergeFrom((com.ibc.core.connection.v1.Params)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(com.ibc.core.connection.v1.Params other) {
      if (other == com.ibc.core.connection.v1.Params.getDefaultInstance()) return this;
      if (other.getMaxExpectedTimePerBlock() != 0L) {
        setMaxExpectedTimePerBlock(other.getMaxExpectedTimePerBlock());
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
              maxExpectedTimePerBlock_ = input.readUInt64();
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

    private long maxExpectedTimePerBlock_ ;
    /**
     * <pre>
     * maximum expected time per block (in nanoseconds), used to enforce block delay. This parameter should reflect the
     * largest amount of time that the chain might reasonably take to produce the next block under normal operating
     * conditions. A safe choice is 3-5x the expected time per block.
     * </pre>
     *
     * <code>uint64 max_expected_time_per_block = 1 [json_name = "maxExpectedTimePerBlock", (.gogoproto.moretags) = "yaml:&#92;"max_expected_time_per_block&#92;""];</code>
     * @return The maxExpectedTimePerBlock.
     */
    @java.lang.Override
    public long getMaxExpectedTimePerBlock() {
      return maxExpectedTimePerBlock_;
    }
    /**
     * <pre>
     * maximum expected time per block (in nanoseconds), used to enforce block delay. This parameter should reflect the
     * largest amount of time that the chain might reasonably take to produce the next block under normal operating
     * conditions. A safe choice is 3-5x the expected time per block.
     * </pre>
     *
     * <code>uint64 max_expected_time_per_block = 1 [json_name = "maxExpectedTimePerBlock", (.gogoproto.moretags) = "yaml:&#92;"max_expected_time_per_block&#92;""];</code>
     * @param value The maxExpectedTimePerBlock to set.
     * @return This builder for chaining.
     */
    public Builder setMaxExpectedTimePerBlock(long value) {

      maxExpectedTimePerBlock_ = value;
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * maximum expected time per block (in nanoseconds), used to enforce block delay. This parameter should reflect the
     * largest amount of time that the chain might reasonably take to produce the next block under normal operating
     * conditions. A safe choice is 3-5x the expected time per block.
     * </pre>
     *
     * <code>uint64 max_expected_time_per_block = 1 [json_name = "maxExpectedTimePerBlock", (.gogoproto.moretags) = "yaml:&#92;"max_expected_time_per_block&#92;""];</code>
     * @return This builder for chaining.
     */
    public Builder clearMaxExpectedTimePerBlock() {
      bitField0_ = (bitField0_ & ~0x00000001);
      maxExpectedTimePerBlock_ = 0L;
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


    // @@protoc_insertion_point(builder_scope:ibc.core.connection.v1.Params)
  }

  // @@protoc_insertion_point(class_scope:ibc.core.connection.v1.Params)
  private static final com.ibc.core.connection.v1.Params DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new com.ibc.core.connection.v1.Params();
  }

  public static com.ibc.core.connection.v1.Params getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<Params>
      PARSER = new com.google.protobuf.AbstractParser<Params>() {
    @java.lang.Override
    public Params parsePartialFrom(
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

  public static com.google.protobuf.Parser<Params> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<Params> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public com.ibc.core.connection.v1.Params getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

