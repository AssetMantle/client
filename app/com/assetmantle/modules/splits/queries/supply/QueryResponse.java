// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: splits/queries/supply/query_response.proto

package com.assetmantle.modules.splits.queries.supply;

/**
 * Protobuf type {@code assetmantle.modules.splits.queries.supply.QueryResponse}
 */
public final class QueryResponse extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:assetmantle.modules.splits.queries.supply.QueryResponse)
    QueryResponseOrBuilder {
private static final long serialVersionUID = 0L;
  // Use QueryResponse.newBuilder() to construct.
  private QueryResponse(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private QueryResponse() {
    supply_ = "";
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new QueryResponse();
  }

  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return com.assetmantle.modules.splits.queries.supply.QueryResponseProto.internal_static_assetmantle_modules_splits_queries_supply_QueryResponse_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return com.assetmantle.modules.splits.queries.supply.QueryResponseProto.internal_static_assetmantle_modules_splits_queries_supply_QueryResponse_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            com.assetmantle.modules.splits.queries.supply.QueryResponse.class, com.assetmantle.modules.splits.queries.supply.QueryResponse.Builder.class);
  }

  public static final int SUPPLY_FIELD_NUMBER = 1;
  @SuppressWarnings("serial")
  private volatile java.lang.Object supply_ = "";
  /**
   * <code>string supply = 1 [json_name = "supply"];</code>
   * @return The supply.
   */
  @java.lang.Override
  public java.lang.String getSupply() {
    java.lang.Object ref = supply_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      supply_ = s;
      return s;
    }
  }
  /**
   * <code>string supply = 1 [json_name = "supply"];</code>
   * @return The bytes for supply.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getSupplyBytes() {
    java.lang.Object ref = supply_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      supply_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
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
    if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(supply_)) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 1, supply_);
    }
    getUnknownFields().writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(supply_)) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(1, supply_);
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
    if (!(obj instanceof com.assetmantle.modules.splits.queries.supply.QueryResponse)) {
      return super.equals(obj);
    }
    com.assetmantle.modules.splits.queries.supply.QueryResponse other = (com.assetmantle.modules.splits.queries.supply.QueryResponse) obj;

    if (!getSupply()
        .equals(other.getSupply())) return false;
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
    hash = (37 * hash) + SUPPLY_FIELD_NUMBER;
    hash = (53 * hash) + getSupply().hashCode();
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static com.assetmantle.modules.splits.queries.supply.QueryResponse parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.assetmantle.modules.splits.queries.supply.QueryResponse parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.assetmantle.modules.splits.queries.supply.QueryResponse parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.assetmantle.modules.splits.queries.supply.QueryResponse parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.assetmantle.modules.splits.queries.supply.QueryResponse parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.assetmantle.modules.splits.queries.supply.QueryResponse parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.assetmantle.modules.splits.queries.supply.QueryResponse parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.assetmantle.modules.splits.queries.supply.QueryResponse parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.assetmantle.modules.splits.queries.supply.QueryResponse parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static com.assetmantle.modules.splits.queries.supply.QueryResponse parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.assetmantle.modules.splits.queries.supply.QueryResponse parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.assetmantle.modules.splits.queries.supply.QueryResponse parseFrom(
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
  public static Builder newBuilder(com.assetmantle.modules.splits.queries.supply.QueryResponse prototype) {
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
   * Protobuf type {@code assetmantle.modules.splits.queries.supply.QueryResponse}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:assetmantle.modules.splits.queries.supply.QueryResponse)
      com.assetmantle.modules.splits.queries.supply.QueryResponseOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return com.assetmantle.modules.splits.queries.supply.QueryResponseProto.internal_static_assetmantle_modules_splits_queries_supply_QueryResponse_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return com.assetmantle.modules.splits.queries.supply.QueryResponseProto.internal_static_assetmantle_modules_splits_queries_supply_QueryResponse_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              com.assetmantle.modules.splits.queries.supply.QueryResponse.class, com.assetmantle.modules.splits.queries.supply.QueryResponse.Builder.class);
    }

    // Construct using com.assetmantle.modules.splits.queries.supply.QueryResponse.newBuilder()
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
      supply_ = "";
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return com.assetmantle.modules.splits.queries.supply.QueryResponseProto.internal_static_assetmantle_modules_splits_queries_supply_QueryResponse_descriptor;
    }

    @java.lang.Override
    public com.assetmantle.modules.splits.queries.supply.QueryResponse getDefaultInstanceForType() {
      return com.assetmantle.modules.splits.queries.supply.QueryResponse.getDefaultInstance();
    }

    @java.lang.Override
    public com.assetmantle.modules.splits.queries.supply.QueryResponse build() {
      com.assetmantle.modules.splits.queries.supply.QueryResponse result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public com.assetmantle.modules.splits.queries.supply.QueryResponse buildPartial() {
      com.assetmantle.modules.splits.queries.supply.QueryResponse result = new com.assetmantle.modules.splits.queries.supply.QueryResponse(this);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartial0(com.assetmantle.modules.splits.queries.supply.QueryResponse result) {
      int from_bitField0_ = bitField0_;
      if (((from_bitField0_ & 0x00000001) != 0)) {
        result.supply_ = supply_;
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
      if (other instanceof com.assetmantle.modules.splits.queries.supply.QueryResponse) {
        return mergeFrom((com.assetmantle.modules.splits.queries.supply.QueryResponse)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(com.assetmantle.modules.splits.queries.supply.QueryResponse other) {
      if (other == com.assetmantle.modules.splits.queries.supply.QueryResponse.getDefaultInstance()) return this;
      if (!other.getSupply().isEmpty()) {
        supply_ = other.supply_;
        bitField0_ |= 0x00000001;
        onChanged();
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
              supply_ = input.readStringRequireUtf8();
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

    private java.lang.Object supply_ = "";
    /**
     * <code>string supply = 1 [json_name = "supply"];</code>
     * @return The supply.
     */
    public java.lang.String getSupply() {
      java.lang.Object ref = supply_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        supply_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <code>string supply = 1 [json_name = "supply"];</code>
     * @return The bytes for supply.
     */
    public com.google.protobuf.ByteString
        getSupplyBytes() {
      java.lang.Object ref = supply_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        supply_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <code>string supply = 1 [json_name = "supply"];</code>
     * @param value The supply to set.
     * @return This builder for chaining.
     */
    public Builder setSupply(
        java.lang.String value) {
      if (value == null) { throw new NullPointerException(); }
      supply_ = value;
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <code>string supply = 1 [json_name = "supply"];</code>
     * @return This builder for chaining.
     */
    public Builder clearSupply() {
      supply_ = getDefaultInstance().getSupply();
      bitField0_ = (bitField0_ & ~0x00000001);
      onChanged();
      return this;
    }
    /**
     * <code>string supply = 1 [json_name = "supply"];</code>
     * @param value The bytes for supply to set.
     * @return This builder for chaining.
     */
    public Builder setSupplyBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) { throw new NullPointerException(); }
      checkByteStringIsUtf8(value);
      supply_ = value;
      bitField0_ |= 0x00000001;
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


    // @@protoc_insertion_point(builder_scope:assetmantle.modules.splits.queries.supply.QueryResponse)
  }

  // @@protoc_insertion_point(class_scope:assetmantle.modules.splits.queries.supply.QueryResponse)
  private static final com.assetmantle.modules.splits.queries.supply.QueryResponse DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new com.assetmantle.modules.splits.queries.supply.QueryResponse();
  }

  public static com.assetmantle.modules.splits.queries.supply.QueryResponse getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<QueryResponse>
      PARSER = new com.google.protobuf.AbstractParser<QueryResponse>() {
    @java.lang.Override
    public QueryResponse parsePartialFrom(
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

  public static com.google.protobuf.Parser<QueryResponse> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<QueryResponse> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public com.assetmantle.modules.splits.queries.supply.QueryResponse getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}
