// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: cosmos/distribution/v1beta1/query.proto

package com.cosmos.distribution.v1beta1;

/**
 * <pre>
 * QueryValidatorCommissionResponse is the response type for the
 * Query/ValidatorCommission RPC method
 * </pre>
 *
 * Protobuf type {@code cosmos.distribution.v1beta1.QueryValidatorCommissionResponse}
 */
public final class QueryValidatorCommissionResponse extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:cosmos.distribution.v1beta1.QueryValidatorCommissionResponse)
    QueryValidatorCommissionResponseOrBuilder {
private static final long serialVersionUID = 0L;
  // Use QueryValidatorCommissionResponse.newBuilder() to construct.
  private QueryValidatorCommissionResponse(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private QueryValidatorCommissionResponse() {
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new QueryValidatorCommissionResponse();
  }

  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return com.cosmos.distribution.v1beta1.QueryProto.internal_static_cosmos_distribution_v1beta1_QueryValidatorCommissionResponse_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return com.cosmos.distribution.v1beta1.QueryProto.internal_static_cosmos_distribution_v1beta1_QueryValidatorCommissionResponse_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            com.cosmos.distribution.v1beta1.QueryValidatorCommissionResponse.class, com.cosmos.distribution.v1beta1.QueryValidatorCommissionResponse.Builder.class);
  }

  public static final int COMMISSION_FIELD_NUMBER = 1;
  private com.cosmos.distribution.v1beta1.ValidatorAccumulatedCommission commission_;
  /**
   * <pre>
   * commission defines the commision the validator received.
   * </pre>
   *
   * <code>.cosmos.distribution.v1beta1.ValidatorAccumulatedCommission commission = 1 [json_name = "commission", (.gogoproto.nullable) = false];</code>
   * @return Whether the commission field is set.
   */
  @java.lang.Override
  public boolean hasCommission() {
    return commission_ != null;
  }
  /**
   * <pre>
   * commission defines the commision the validator received.
   * </pre>
   *
   * <code>.cosmos.distribution.v1beta1.ValidatorAccumulatedCommission commission = 1 [json_name = "commission", (.gogoproto.nullable) = false];</code>
   * @return The commission.
   */
  @java.lang.Override
  public com.cosmos.distribution.v1beta1.ValidatorAccumulatedCommission getCommission() {
    return commission_ == null ? com.cosmos.distribution.v1beta1.ValidatorAccumulatedCommission.getDefaultInstance() : commission_;
  }
  /**
   * <pre>
   * commission defines the commision the validator received.
   * </pre>
   *
   * <code>.cosmos.distribution.v1beta1.ValidatorAccumulatedCommission commission = 1 [json_name = "commission", (.gogoproto.nullable) = false];</code>
   */
  @java.lang.Override
  public com.cosmos.distribution.v1beta1.ValidatorAccumulatedCommissionOrBuilder getCommissionOrBuilder() {
    return commission_ == null ? com.cosmos.distribution.v1beta1.ValidatorAccumulatedCommission.getDefaultInstance() : commission_;
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
    if (commission_ != null) {
      output.writeMessage(1, getCommission());
    }
    getUnknownFields().writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (commission_ != null) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(1, getCommission());
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
    if (!(obj instanceof com.cosmos.distribution.v1beta1.QueryValidatorCommissionResponse)) {
      return super.equals(obj);
    }
    com.cosmos.distribution.v1beta1.QueryValidatorCommissionResponse other = (com.cosmos.distribution.v1beta1.QueryValidatorCommissionResponse) obj;

    if (hasCommission() != other.hasCommission()) return false;
    if (hasCommission()) {
      if (!getCommission()
          .equals(other.getCommission())) return false;
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
    if (hasCommission()) {
      hash = (37 * hash) + COMMISSION_FIELD_NUMBER;
      hash = (53 * hash) + getCommission().hashCode();
    }
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static com.cosmos.distribution.v1beta1.QueryValidatorCommissionResponse parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.cosmos.distribution.v1beta1.QueryValidatorCommissionResponse parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.cosmos.distribution.v1beta1.QueryValidatorCommissionResponse parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.cosmos.distribution.v1beta1.QueryValidatorCommissionResponse parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.cosmos.distribution.v1beta1.QueryValidatorCommissionResponse parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.cosmos.distribution.v1beta1.QueryValidatorCommissionResponse parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.cosmos.distribution.v1beta1.QueryValidatorCommissionResponse parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.cosmos.distribution.v1beta1.QueryValidatorCommissionResponse parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.cosmos.distribution.v1beta1.QueryValidatorCommissionResponse parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static com.cosmos.distribution.v1beta1.QueryValidatorCommissionResponse parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.cosmos.distribution.v1beta1.QueryValidatorCommissionResponse parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.cosmos.distribution.v1beta1.QueryValidatorCommissionResponse parseFrom(
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
  public static Builder newBuilder(com.cosmos.distribution.v1beta1.QueryValidatorCommissionResponse prototype) {
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
   * QueryValidatorCommissionResponse is the response type for the
   * Query/ValidatorCommission RPC method
   * </pre>
   *
   * Protobuf type {@code cosmos.distribution.v1beta1.QueryValidatorCommissionResponse}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:cosmos.distribution.v1beta1.QueryValidatorCommissionResponse)
      com.cosmos.distribution.v1beta1.QueryValidatorCommissionResponseOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return com.cosmos.distribution.v1beta1.QueryProto.internal_static_cosmos_distribution_v1beta1_QueryValidatorCommissionResponse_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return com.cosmos.distribution.v1beta1.QueryProto.internal_static_cosmos_distribution_v1beta1_QueryValidatorCommissionResponse_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              com.cosmos.distribution.v1beta1.QueryValidatorCommissionResponse.class, com.cosmos.distribution.v1beta1.QueryValidatorCommissionResponse.Builder.class);
    }

    // Construct using com.cosmos.distribution.v1beta1.QueryValidatorCommissionResponse.newBuilder()
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
      commission_ = null;
      if (commissionBuilder_ != null) {
        commissionBuilder_.dispose();
        commissionBuilder_ = null;
      }
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return com.cosmos.distribution.v1beta1.QueryProto.internal_static_cosmos_distribution_v1beta1_QueryValidatorCommissionResponse_descriptor;
    }

    @java.lang.Override
    public com.cosmos.distribution.v1beta1.QueryValidatorCommissionResponse getDefaultInstanceForType() {
      return com.cosmos.distribution.v1beta1.QueryValidatorCommissionResponse.getDefaultInstance();
    }

    @java.lang.Override
    public com.cosmos.distribution.v1beta1.QueryValidatorCommissionResponse build() {
      com.cosmos.distribution.v1beta1.QueryValidatorCommissionResponse result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public com.cosmos.distribution.v1beta1.QueryValidatorCommissionResponse buildPartial() {
      com.cosmos.distribution.v1beta1.QueryValidatorCommissionResponse result = new com.cosmos.distribution.v1beta1.QueryValidatorCommissionResponse(this);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartial0(com.cosmos.distribution.v1beta1.QueryValidatorCommissionResponse result) {
      int from_bitField0_ = bitField0_;
      if (((from_bitField0_ & 0x00000001) != 0)) {
        result.commission_ = commissionBuilder_ == null
            ? commission_
            : commissionBuilder_.build();
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
      if (other instanceof com.cosmos.distribution.v1beta1.QueryValidatorCommissionResponse) {
        return mergeFrom((com.cosmos.distribution.v1beta1.QueryValidatorCommissionResponse)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(com.cosmos.distribution.v1beta1.QueryValidatorCommissionResponse other) {
      if (other == com.cosmos.distribution.v1beta1.QueryValidatorCommissionResponse.getDefaultInstance()) return this;
      if (other.hasCommission()) {
        mergeCommission(other.getCommission());
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
                  getCommissionFieldBuilder().getBuilder(),
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

    private com.cosmos.distribution.v1beta1.ValidatorAccumulatedCommission commission_;
    private com.google.protobuf.SingleFieldBuilderV3<
        com.cosmos.distribution.v1beta1.ValidatorAccumulatedCommission, com.cosmos.distribution.v1beta1.ValidatorAccumulatedCommission.Builder, com.cosmos.distribution.v1beta1.ValidatorAccumulatedCommissionOrBuilder> commissionBuilder_;
    /**
     * <pre>
     * commission defines the commision the validator received.
     * </pre>
     *
     * <code>.cosmos.distribution.v1beta1.ValidatorAccumulatedCommission commission = 1 [json_name = "commission", (.gogoproto.nullable) = false];</code>
     * @return Whether the commission field is set.
     */
    public boolean hasCommission() {
      return ((bitField0_ & 0x00000001) != 0);
    }
    /**
     * <pre>
     * commission defines the commision the validator received.
     * </pre>
     *
     * <code>.cosmos.distribution.v1beta1.ValidatorAccumulatedCommission commission = 1 [json_name = "commission", (.gogoproto.nullable) = false];</code>
     * @return The commission.
     */
    public com.cosmos.distribution.v1beta1.ValidatorAccumulatedCommission getCommission() {
      if (commissionBuilder_ == null) {
        return commission_ == null ? com.cosmos.distribution.v1beta1.ValidatorAccumulatedCommission.getDefaultInstance() : commission_;
      } else {
        return commissionBuilder_.getMessage();
      }
    }
    /**
     * <pre>
     * commission defines the commision the validator received.
     * </pre>
     *
     * <code>.cosmos.distribution.v1beta1.ValidatorAccumulatedCommission commission = 1 [json_name = "commission", (.gogoproto.nullable) = false];</code>
     */
    public Builder setCommission(com.cosmos.distribution.v1beta1.ValidatorAccumulatedCommission value) {
      if (commissionBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        commission_ = value;
      } else {
        commissionBuilder_.setMessage(value);
      }
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * commission defines the commision the validator received.
     * </pre>
     *
     * <code>.cosmos.distribution.v1beta1.ValidatorAccumulatedCommission commission = 1 [json_name = "commission", (.gogoproto.nullable) = false];</code>
     */
    public Builder setCommission(
        com.cosmos.distribution.v1beta1.ValidatorAccumulatedCommission.Builder builderForValue) {
      if (commissionBuilder_ == null) {
        commission_ = builderForValue.build();
      } else {
        commissionBuilder_.setMessage(builderForValue.build());
      }
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * commission defines the commision the validator received.
     * </pre>
     *
     * <code>.cosmos.distribution.v1beta1.ValidatorAccumulatedCommission commission = 1 [json_name = "commission", (.gogoproto.nullable) = false];</code>
     */
    public Builder mergeCommission(com.cosmos.distribution.v1beta1.ValidatorAccumulatedCommission value) {
      if (commissionBuilder_ == null) {
        if (((bitField0_ & 0x00000001) != 0) &&
          commission_ != null &&
          commission_ != com.cosmos.distribution.v1beta1.ValidatorAccumulatedCommission.getDefaultInstance()) {
          getCommissionBuilder().mergeFrom(value);
        } else {
          commission_ = value;
        }
      } else {
        commissionBuilder_.mergeFrom(value);
      }
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * commission defines the commision the validator received.
     * </pre>
     *
     * <code>.cosmos.distribution.v1beta1.ValidatorAccumulatedCommission commission = 1 [json_name = "commission", (.gogoproto.nullable) = false];</code>
     */
    public Builder clearCommission() {
      bitField0_ = (bitField0_ & ~0x00000001);
      commission_ = null;
      if (commissionBuilder_ != null) {
        commissionBuilder_.dispose();
        commissionBuilder_ = null;
      }
      onChanged();
      return this;
    }
    /**
     * <pre>
     * commission defines the commision the validator received.
     * </pre>
     *
     * <code>.cosmos.distribution.v1beta1.ValidatorAccumulatedCommission commission = 1 [json_name = "commission", (.gogoproto.nullable) = false];</code>
     */
    public com.cosmos.distribution.v1beta1.ValidatorAccumulatedCommission.Builder getCommissionBuilder() {
      bitField0_ |= 0x00000001;
      onChanged();
      return getCommissionFieldBuilder().getBuilder();
    }
    /**
     * <pre>
     * commission defines the commision the validator received.
     * </pre>
     *
     * <code>.cosmos.distribution.v1beta1.ValidatorAccumulatedCommission commission = 1 [json_name = "commission", (.gogoproto.nullable) = false];</code>
     */
    public com.cosmos.distribution.v1beta1.ValidatorAccumulatedCommissionOrBuilder getCommissionOrBuilder() {
      if (commissionBuilder_ != null) {
        return commissionBuilder_.getMessageOrBuilder();
      } else {
        return commission_ == null ?
            com.cosmos.distribution.v1beta1.ValidatorAccumulatedCommission.getDefaultInstance() : commission_;
      }
    }
    /**
     * <pre>
     * commission defines the commision the validator received.
     * </pre>
     *
     * <code>.cosmos.distribution.v1beta1.ValidatorAccumulatedCommission commission = 1 [json_name = "commission", (.gogoproto.nullable) = false];</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
        com.cosmos.distribution.v1beta1.ValidatorAccumulatedCommission, com.cosmos.distribution.v1beta1.ValidatorAccumulatedCommission.Builder, com.cosmos.distribution.v1beta1.ValidatorAccumulatedCommissionOrBuilder> 
        getCommissionFieldBuilder() {
      if (commissionBuilder_ == null) {
        commissionBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
            com.cosmos.distribution.v1beta1.ValidatorAccumulatedCommission, com.cosmos.distribution.v1beta1.ValidatorAccumulatedCommission.Builder, com.cosmos.distribution.v1beta1.ValidatorAccumulatedCommissionOrBuilder>(
                getCommission(),
                getParentForChildren(),
                isClean());
        commission_ = null;
      }
      return commissionBuilder_;
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


    // @@protoc_insertion_point(builder_scope:cosmos.distribution.v1beta1.QueryValidatorCommissionResponse)
  }

  // @@protoc_insertion_point(class_scope:cosmos.distribution.v1beta1.QueryValidatorCommissionResponse)
  private static final com.cosmos.distribution.v1beta1.QueryValidatorCommissionResponse DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new com.cosmos.distribution.v1beta1.QueryValidatorCommissionResponse();
  }

  public static com.cosmos.distribution.v1beta1.QueryValidatorCommissionResponse getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<QueryValidatorCommissionResponse>
      PARSER = new com.google.protobuf.AbstractParser<QueryValidatorCommissionResponse>() {
    @java.lang.Override
    public QueryValidatorCommissionResponse parsePartialFrom(
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

  public static com.google.protobuf.Parser<QueryValidatorCommissionResponse> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<QueryValidatorCommissionResponse> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public com.cosmos.distribution.v1beta1.QueryValidatorCommissionResponse getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

