// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: cosmos/base/tendermint/v1beta1/query.proto

package com.cosmos.base.tendermint.v1beta1;

/**
 * <pre>
 * GetLatestValidatorSetResponse is the response type for the Query/GetValidatorSetByHeight RPC method.
 * </pre>
 *
 * Protobuf type {@code cosmos.base.tendermint.v1beta1.GetLatestValidatorSetResponse}
 */
public final class GetLatestValidatorSetResponse extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:cosmos.base.tendermint.v1beta1.GetLatestValidatorSetResponse)
    GetLatestValidatorSetResponseOrBuilder {
private static final long serialVersionUID = 0L;
  // Use GetLatestValidatorSetResponse.newBuilder() to construct.
  private GetLatestValidatorSetResponse(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private GetLatestValidatorSetResponse() {
    validators_ = java.util.Collections.emptyList();
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new GetLatestValidatorSetResponse();
  }

  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return com.cosmos.base.tendermint.v1beta1.QueryProto.internal_static_cosmos_base_tendermint_v1beta1_GetLatestValidatorSetResponse_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return com.cosmos.base.tendermint.v1beta1.QueryProto.internal_static_cosmos_base_tendermint_v1beta1_GetLatestValidatorSetResponse_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            com.cosmos.base.tendermint.v1beta1.GetLatestValidatorSetResponse.class, com.cosmos.base.tendermint.v1beta1.GetLatestValidatorSetResponse.Builder.class);
  }

  public static final int BLOCK_HEIGHT_FIELD_NUMBER = 1;
  private long blockHeight_ = 0L;
  /**
   * <code>int64 block_height = 1 [json_name = "blockHeight"];</code>
   * @return The blockHeight.
   */
  @java.lang.Override
  public long getBlockHeight() {
    return blockHeight_;
  }

  public static final int VALIDATORS_FIELD_NUMBER = 2;
  @SuppressWarnings("serial")
  private java.util.List<com.cosmos.base.tendermint.v1beta1.Validator> validators_;
  /**
   * <code>repeated .cosmos.base.tendermint.v1beta1.Validator validators = 2 [json_name = "validators"];</code>
   */
  @java.lang.Override
  public java.util.List<com.cosmos.base.tendermint.v1beta1.Validator> getValidatorsList() {
    return validators_;
  }
  /**
   * <code>repeated .cosmos.base.tendermint.v1beta1.Validator validators = 2 [json_name = "validators"];</code>
   */
  @java.lang.Override
  public java.util.List<? extends com.cosmos.base.tendermint.v1beta1.ValidatorOrBuilder> 
      getValidatorsOrBuilderList() {
    return validators_;
  }
  /**
   * <code>repeated .cosmos.base.tendermint.v1beta1.Validator validators = 2 [json_name = "validators"];</code>
   */
  @java.lang.Override
  public int getValidatorsCount() {
    return validators_.size();
  }
  /**
   * <code>repeated .cosmos.base.tendermint.v1beta1.Validator validators = 2 [json_name = "validators"];</code>
   */
  @java.lang.Override
  public com.cosmos.base.tendermint.v1beta1.Validator getValidators(int index) {
    return validators_.get(index);
  }
  /**
   * <code>repeated .cosmos.base.tendermint.v1beta1.Validator validators = 2 [json_name = "validators"];</code>
   */
  @java.lang.Override
  public com.cosmos.base.tendermint.v1beta1.ValidatorOrBuilder getValidatorsOrBuilder(
      int index) {
    return validators_.get(index);
  }

  public static final int PAGINATION_FIELD_NUMBER = 3;
  private com.cosmos.base.query.v1beta1.PageResponse pagination_;
  /**
   * <pre>
   * pagination defines an pagination for the response.
   * </pre>
   *
   * <code>.cosmos.base.query.v1beta1.PageResponse pagination = 3 [json_name = "pagination"];</code>
   * @return Whether the pagination field is set.
   */
  @java.lang.Override
  public boolean hasPagination() {
    return pagination_ != null;
  }
  /**
   * <pre>
   * pagination defines an pagination for the response.
   * </pre>
   *
   * <code>.cosmos.base.query.v1beta1.PageResponse pagination = 3 [json_name = "pagination"];</code>
   * @return The pagination.
   */
  @java.lang.Override
  public com.cosmos.base.query.v1beta1.PageResponse getPagination() {
    return pagination_ == null ? com.cosmos.base.query.v1beta1.PageResponse.getDefaultInstance() : pagination_;
  }
  /**
   * <pre>
   * pagination defines an pagination for the response.
   * </pre>
   *
   * <code>.cosmos.base.query.v1beta1.PageResponse pagination = 3 [json_name = "pagination"];</code>
   */
  @java.lang.Override
  public com.cosmos.base.query.v1beta1.PageResponseOrBuilder getPaginationOrBuilder() {
    return pagination_ == null ? com.cosmos.base.query.v1beta1.PageResponse.getDefaultInstance() : pagination_;
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
    if (blockHeight_ != 0L) {
      output.writeInt64(1, blockHeight_);
    }
    for (int i = 0; i < validators_.size(); i++) {
      output.writeMessage(2, validators_.get(i));
    }
    if (pagination_ != null) {
      output.writeMessage(3, getPagination());
    }
    getUnknownFields().writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (blockHeight_ != 0L) {
      size += com.google.protobuf.CodedOutputStream
        .computeInt64Size(1, blockHeight_);
    }
    for (int i = 0; i < validators_.size(); i++) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(2, validators_.get(i));
    }
    if (pagination_ != null) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(3, getPagination());
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
    if (!(obj instanceof com.cosmos.base.tendermint.v1beta1.GetLatestValidatorSetResponse)) {
      return super.equals(obj);
    }
    com.cosmos.base.tendermint.v1beta1.GetLatestValidatorSetResponse other = (com.cosmos.base.tendermint.v1beta1.GetLatestValidatorSetResponse) obj;

    if (getBlockHeight()
        != other.getBlockHeight()) return false;
    if (!getValidatorsList()
        .equals(other.getValidatorsList())) return false;
    if (hasPagination() != other.hasPagination()) return false;
    if (hasPagination()) {
      if (!getPagination()
          .equals(other.getPagination())) return false;
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
    hash = (37 * hash) + BLOCK_HEIGHT_FIELD_NUMBER;
    hash = (53 * hash) + com.google.protobuf.Internal.hashLong(
        getBlockHeight());
    if (getValidatorsCount() > 0) {
      hash = (37 * hash) + VALIDATORS_FIELD_NUMBER;
      hash = (53 * hash) + getValidatorsList().hashCode();
    }
    if (hasPagination()) {
      hash = (37 * hash) + PAGINATION_FIELD_NUMBER;
      hash = (53 * hash) + getPagination().hashCode();
    }
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static com.cosmos.base.tendermint.v1beta1.GetLatestValidatorSetResponse parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.cosmos.base.tendermint.v1beta1.GetLatestValidatorSetResponse parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.cosmos.base.tendermint.v1beta1.GetLatestValidatorSetResponse parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.cosmos.base.tendermint.v1beta1.GetLatestValidatorSetResponse parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.cosmos.base.tendermint.v1beta1.GetLatestValidatorSetResponse parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.cosmos.base.tendermint.v1beta1.GetLatestValidatorSetResponse parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.cosmos.base.tendermint.v1beta1.GetLatestValidatorSetResponse parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.cosmos.base.tendermint.v1beta1.GetLatestValidatorSetResponse parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.cosmos.base.tendermint.v1beta1.GetLatestValidatorSetResponse parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static com.cosmos.base.tendermint.v1beta1.GetLatestValidatorSetResponse parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.cosmos.base.tendermint.v1beta1.GetLatestValidatorSetResponse parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.cosmos.base.tendermint.v1beta1.GetLatestValidatorSetResponse parseFrom(
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
  public static Builder newBuilder(com.cosmos.base.tendermint.v1beta1.GetLatestValidatorSetResponse prototype) {
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
   * GetLatestValidatorSetResponse is the response type for the Query/GetValidatorSetByHeight RPC method.
   * </pre>
   *
   * Protobuf type {@code cosmos.base.tendermint.v1beta1.GetLatestValidatorSetResponse}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:cosmos.base.tendermint.v1beta1.GetLatestValidatorSetResponse)
      com.cosmos.base.tendermint.v1beta1.GetLatestValidatorSetResponseOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return com.cosmos.base.tendermint.v1beta1.QueryProto.internal_static_cosmos_base_tendermint_v1beta1_GetLatestValidatorSetResponse_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return com.cosmos.base.tendermint.v1beta1.QueryProto.internal_static_cosmos_base_tendermint_v1beta1_GetLatestValidatorSetResponse_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              com.cosmos.base.tendermint.v1beta1.GetLatestValidatorSetResponse.class, com.cosmos.base.tendermint.v1beta1.GetLatestValidatorSetResponse.Builder.class);
    }

    // Construct using com.cosmos.base.tendermint.v1beta1.GetLatestValidatorSetResponse.newBuilder()
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
      blockHeight_ = 0L;
      if (validatorsBuilder_ == null) {
        validators_ = java.util.Collections.emptyList();
      } else {
        validators_ = null;
        validatorsBuilder_.clear();
      }
      bitField0_ = (bitField0_ & ~0x00000002);
      pagination_ = null;
      if (paginationBuilder_ != null) {
        paginationBuilder_.dispose();
        paginationBuilder_ = null;
      }
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return com.cosmos.base.tendermint.v1beta1.QueryProto.internal_static_cosmos_base_tendermint_v1beta1_GetLatestValidatorSetResponse_descriptor;
    }

    @java.lang.Override
    public com.cosmos.base.tendermint.v1beta1.GetLatestValidatorSetResponse getDefaultInstanceForType() {
      return com.cosmos.base.tendermint.v1beta1.GetLatestValidatorSetResponse.getDefaultInstance();
    }

    @java.lang.Override
    public com.cosmos.base.tendermint.v1beta1.GetLatestValidatorSetResponse build() {
      com.cosmos.base.tendermint.v1beta1.GetLatestValidatorSetResponse result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public com.cosmos.base.tendermint.v1beta1.GetLatestValidatorSetResponse buildPartial() {
      com.cosmos.base.tendermint.v1beta1.GetLatestValidatorSetResponse result = new com.cosmos.base.tendermint.v1beta1.GetLatestValidatorSetResponse(this);
      buildPartialRepeatedFields(result);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartialRepeatedFields(com.cosmos.base.tendermint.v1beta1.GetLatestValidatorSetResponse result) {
      if (validatorsBuilder_ == null) {
        if (((bitField0_ & 0x00000002) != 0)) {
          validators_ = java.util.Collections.unmodifiableList(validators_);
          bitField0_ = (bitField0_ & ~0x00000002);
        }
        result.validators_ = validators_;
      } else {
        result.validators_ = validatorsBuilder_.build();
      }
    }

    private void buildPartial0(com.cosmos.base.tendermint.v1beta1.GetLatestValidatorSetResponse result) {
      int from_bitField0_ = bitField0_;
      if (((from_bitField0_ & 0x00000001) != 0)) {
        result.blockHeight_ = blockHeight_;
      }
      if (((from_bitField0_ & 0x00000004) != 0)) {
        result.pagination_ = paginationBuilder_ == null
            ? pagination_
            : paginationBuilder_.build();
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
      if (other instanceof com.cosmos.base.tendermint.v1beta1.GetLatestValidatorSetResponse) {
        return mergeFrom((com.cosmos.base.tendermint.v1beta1.GetLatestValidatorSetResponse)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(com.cosmos.base.tendermint.v1beta1.GetLatestValidatorSetResponse other) {
      if (other == com.cosmos.base.tendermint.v1beta1.GetLatestValidatorSetResponse.getDefaultInstance()) return this;
      if (other.getBlockHeight() != 0L) {
        setBlockHeight(other.getBlockHeight());
      }
      if (validatorsBuilder_ == null) {
        if (!other.validators_.isEmpty()) {
          if (validators_.isEmpty()) {
            validators_ = other.validators_;
            bitField0_ = (bitField0_ & ~0x00000002);
          } else {
            ensureValidatorsIsMutable();
            validators_.addAll(other.validators_);
          }
          onChanged();
        }
      } else {
        if (!other.validators_.isEmpty()) {
          if (validatorsBuilder_.isEmpty()) {
            validatorsBuilder_.dispose();
            validatorsBuilder_ = null;
            validators_ = other.validators_;
            bitField0_ = (bitField0_ & ~0x00000002);
            validatorsBuilder_ = 
              com.google.protobuf.GeneratedMessageV3.alwaysUseFieldBuilders ?
                 getValidatorsFieldBuilder() : null;
          } else {
            validatorsBuilder_.addAllMessages(other.validators_);
          }
        }
      }
      if (other.hasPagination()) {
        mergePagination(other.getPagination());
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
              blockHeight_ = input.readInt64();
              bitField0_ |= 0x00000001;
              break;
            } // case 8
            case 18: {
              com.cosmos.base.tendermint.v1beta1.Validator m =
                  input.readMessage(
                      com.cosmos.base.tendermint.v1beta1.Validator.parser(),
                      extensionRegistry);
              if (validatorsBuilder_ == null) {
                ensureValidatorsIsMutable();
                validators_.add(m);
              } else {
                validatorsBuilder_.addMessage(m);
              }
              break;
            } // case 18
            case 26: {
              input.readMessage(
                  getPaginationFieldBuilder().getBuilder(),
                  extensionRegistry);
              bitField0_ |= 0x00000004;
              break;
            } // case 26
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

    private long blockHeight_ ;
    /**
     * <code>int64 block_height = 1 [json_name = "blockHeight"];</code>
     * @return The blockHeight.
     */
    @java.lang.Override
    public long getBlockHeight() {
      return blockHeight_;
    }
    /**
     * <code>int64 block_height = 1 [json_name = "blockHeight"];</code>
     * @param value The blockHeight to set.
     * @return This builder for chaining.
     */
    public Builder setBlockHeight(long value) {

      blockHeight_ = value;
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <code>int64 block_height = 1 [json_name = "blockHeight"];</code>
     * @return This builder for chaining.
     */
    public Builder clearBlockHeight() {
      bitField0_ = (bitField0_ & ~0x00000001);
      blockHeight_ = 0L;
      onChanged();
      return this;
    }

    private java.util.List<com.cosmos.base.tendermint.v1beta1.Validator> validators_ =
      java.util.Collections.emptyList();
    private void ensureValidatorsIsMutable() {
      if (!((bitField0_ & 0x00000002) != 0)) {
        validators_ = new java.util.ArrayList<com.cosmos.base.tendermint.v1beta1.Validator>(validators_);
        bitField0_ |= 0x00000002;
       }
    }

    private com.google.protobuf.RepeatedFieldBuilderV3<
        com.cosmos.base.tendermint.v1beta1.Validator, com.cosmos.base.tendermint.v1beta1.Validator.Builder, com.cosmos.base.tendermint.v1beta1.ValidatorOrBuilder> validatorsBuilder_;

    /**
     * <code>repeated .cosmos.base.tendermint.v1beta1.Validator validators = 2 [json_name = "validators"];</code>
     */
    public java.util.List<com.cosmos.base.tendermint.v1beta1.Validator> getValidatorsList() {
      if (validatorsBuilder_ == null) {
        return java.util.Collections.unmodifiableList(validators_);
      } else {
        return validatorsBuilder_.getMessageList();
      }
    }
    /**
     * <code>repeated .cosmos.base.tendermint.v1beta1.Validator validators = 2 [json_name = "validators"];</code>
     */
    public int getValidatorsCount() {
      if (validatorsBuilder_ == null) {
        return validators_.size();
      } else {
        return validatorsBuilder_.getCount();
      }
    }
    /**
     * <code>repeated .cosmos.base.tendermint.v1beta1.Validator validators = 2 [json_name = "validators"];</code>
     */
    public com.cosmos.base.tendermint.v1beta1.Validator getValidators(int index) {
      if (validatorsBuilder_ == null) {
        return validators_.get(index);
      } else {
        return validatorsBuilder_.getMessage(index);
      }
    }
    /**
     * <code>repeated .cosmos.base.tendermint.v1beta1.Validator validators = 2 [json_name = "validators"];</code>
     */
    public Builder setValidators(
        int index, com.cosmos.base.tendermint.v1beta1.Validator value) {
      if (validatorsBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureValidatorsIsMutable();
        validators_.set(index, value);
        onChanged();
      } else {
        validatorsBuilder_.setMessage(index, value);
      }
      return this;
    }
    /**
     * <code>repeated .cosmos.base.tendermint.v1beta1.Validator validators = 2 [json_name = "validators"];</code>
     */
    public Builder setValidators(
        int index, com.cosmos.base.tendermint.v1beta1.Validator.Builder builderForValue) {
      if (validatorsBuilder_ == null) {
        ensureValidatorsIsMutable();
        validators_.set(index, builderForValue.build());
        onChanged();
      } else {
        validatorsBuilder_.setMessage(index, builderForValue.build());
      }
      return this;
    }
    /**
     * <code>repeated .cosmos.base.tendermint.v1beta1.Validator validators = 2 [json_name = "validators"];</code>
     */
    public Builder addValidators(com.cosmos.base.tendermint.v1beta1.Validator value) {
      if (validatorsBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureValidatorsIsMutable();
        validators_.add(value);
        onChanged();
      } else {
        validatorsBuilder_.addMessage(value);
      }
      return this;
    }
    /**
     * <code>repeated .cosmos.base.tendermint.v1beta1.Validator validators = 2 [json_name = "validators"];</code>
     */
    public Builder addValidators(
        int index, com.cosmos.base.tendermint.v1beta1.Validator value) {
      if (validatorsBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureValidatorsIsMutable();
        validators_.add(index, value);
        onChanged();
      } else {
        validatorsBuilder_.addMessage(index, value);
      }
      return this;
    }
    /**
     * <code>repeated .cosmos.base.tendermint.v1beta1.Validator validators = 2 [json_name = "validators"];</code>
     */
    public Builder addValidators(
        com.cosmos.base.tendermint.v1beta1.Validator.Builder builderForValue) {
      if (validatorsBuilder_ == null) {
        ensureValidatorsIsMutable();
        validators_.add(builderForValue.build());
        onChanged();
      } else {
        validatorsBuilder_.addMessage(builderForValue.build());
      }
      return this;
    }
    /**
     * <code>repeated .cosmos.base.tendermint.v1beta1.Validator validators = 2 [json_name = "validators"];</code>
     */
    public Builder addValidators(
        int index, com.cosmos.base.tendermint.v1beta1.Validator.Builder builderForValue) {
      if (validatorsBuilder_ == null) {
        ensureValidatorsIsMutable();
        validators_.add(index, builderForValue.build());
        onChanged();
      } else {
        validatorsBuilder_.addMessage(index, builderForValue.build());
      }
      return this;
    }
    /**
     * <code>repeated .cosmos.base.tendermint.v1beta1.Validator validators = 2 [json_name = "validators"];</code>
     */
    public Builder addAllValidators(
        java.lang.Iterable<? extends com.cosmos.base.tendermint.v1beta1.Validator> values) {
      if (validatorsBuilder_ == null) {
        ensureValidatorsIsMutable();
        com.google.protobuf.AbstractMessageLite.Builder.addAll(
            values, validators_);
        onChanged();
      } else {
        validatorsBuilder_.addAllMessages(values);
      }
      return this;
    }
    /**
     * <code>repeated .cosmos.base.tendermint.v1beta1.Validator validators = 2 [json_name = "validators"];</code>
     */
    public Builder clearValidators() {
      if (validatorsBuilder_ == null) {
        validators_ = java.util.Collections.emptyList();
        bitField0_ = (bitField0_ & ~0x00000002);
        onChanged();
      } else {
        validatorsBuilder_.clear();
      }
      return this;
    }
    /**
     * <code>repeated .cosmos.base.tendermint.v1beta1.Validator validators = 2 [json_name = "validators"];</code>
     */
    public Builder removeValidators(int index) {
      if (validatorsBuilder_ == null) {
        ensureValidatorsIsMutable();
        validators_.remove(index);
        onChanged();
      } else {
        validatorsBuilder_.remove(index);
      }
      return this;
    }
    /**
     * <code>repeated .cosmos.base.tendermint.v1beta1.Validator validators = 2 [json_name = "validators"];</code>
     */
    public com.cosmos.base.tendermint.v1beta1.Validator.Builder getValidatorsBuilder(
        int index) {
      return getValidatorsFieldBuilder().getBuilder(index);
    }
    /**
     * <code>repeated .cosmos.base.tendermint.v1beta1.Validator validators = 2 [json_name = "validators"];</code>
     */
    public com.cosmos.base.tendermint.v1beta1.ValidatorOrBuilder getValidatorsOrBuilder(
        int index) {
      if (validatorsBuilder_ == null) {
        return validators_.get(index);  } else {
        return validatorsBuilder_.getMessageOrBuilder(index);
      }
    }
    /**
     * <code>repeated .cosmos.base.tendermint.v1beta1.Validator validators = 2 [json_name = "validators"];</code>
     */
    public java.util.List<? extends com.cosmos.base.tendermint.v1beta1.ValidatorOrBuilder> 
         getValidatorsOrBuilderList() {
      if (validatorsBuilder_ != null) {
        return validatorsBuilder_.getMessageOrBuilderList();
      } else {
        return java.util.Collections.unmodifiableList(validators_);
      }
    }
    /**
     * <code>repeated .cosmos.base.tendermint.v1beta1.Validator validators = 2 [json_name = "validators"];</code>
     */
    public com.cosmos.base.tendermint.v1beta1.Validator.Builder addValidatorsBuilder() {
      return getValidatorsFieldBuilder().addBuilder(
          com.cosmos.base.tendermint.v1beta1.Validator.getDefaultInstance());
    }
    /**
     * <code>repeated .cosmos.base.tendermint.v1beta1.Validator validators = 2 [json_name = "validators"];</code>
     */
    public com.cosmos.base.tendermint.v1beta1.Validator.Builder addValidatorsBuilder(
        int index) {
      return getValidatorsFieldBuilder().addBuilder(
          index, com.cosmos.base.tendermint.v1beta1.Validator.getDefaultInstance());
    }
    /**
     * <code>repeated .cosmos.base.tendermint.v1beta1.Validator validators = 2 [json_name = "validators"];</code>
     */
    public java.util.List<com.cosmos.base.tendermint.v1beta1.Validator.Builder> 
         getValidatorsBuilderList() {
      return getValidatorsFieldBuilder().getBuilderList();
    }
    private com.google.protobuf.RepeatedFieldBuilderV3<
        com.cosmos.base.tendermint.v1beta1.Validator, com.cosmos.base.tendermint.v1beta1.Validator.Builder, com.cosmos.base.tendermint.v1beta1.ValidatorOrBuilder> 
        getValidatorsFieldBuilder() {
      if (validatorsBuilder_ == null) {
        validatorsBuilder_ = new com.google.protobuf.RepeatedFieldBuilderV3<
            com.cosmos.base.tendermint.v1beta1.Validator, com.cosmos.base.tendermint.v1beta1.Validator.Builder, com.cosmos.base.tendermint.v1beta1.ValidatorOrBuilder>(
                validators_,
                ((bitField0_ & 0x00000002) != 0),
                getParentForChildren(),
                isClean());
        validators_ = null;
      }
      return validatorsBuilder_;
    }

    private com.cosmos.base.query.v1beta1.PageResponse pagination_;
    private com.google.protobuf.SingleFieldBuilderV3<
        com.cosmos.base.query.v1beta1.PageResponse, com.cosmos.base.query.v1beta1.PageResponse.Builder, com.cosmos.base.query.v1beta1.PageResponseOrBuilder> paginationBuilder_;
    /**
     * <pre>
     * pagination defines an pagination for the response.
     * </pre>
     *
     * <code>.cosmos.base.query.v1beta1.PageResponse pagination = 3 [json_name = "pagination"];</code>
     * @return Whether the pagination field is set.
     */
    public boolean hasPagination() {
      return ((bitField0_ & 0x00000004) != 0);
    }
    /**
     * <pre>
     * pagination defines an pagination for the response.
     * </pre>
     *
     * <code>.cosmos.base.query.v1beta1.PageResponse pagination = 3 [json_name = "pagination"];</code>
     * @return The pagination.
     */
    public com.cosmos.base.query.v1beta1.PageResponse getPagination() {
      if (paginationBuilder_ == null) {
        return pagination_ == null ? com.cosmos.base.query.v1beta1.PageResponse.getDefaultInstance() : pagination_;
      } else {
        return paginationBuilder_.getMessage();
      }
    }
    /**
     * <pre>
     * pagination defines an pagination for the response.
     * </pre>
     *
     * <code>.cosmos.base.query.v1beta1.PageResponse pagination = 3 [json_name = "pagination"];</code>
     */
    public Builder setPagination(com.cosmos.base.query.v1beta1.PageResponse value) {
      if (paginationBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        pagination_ = value;
      } else {
        paginationBuilder_.setMessage(value);
      }
      bitField0_ |= 0x00000004;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * pagination defines an pagination for the response.
     * </pre>
     *
     * <code>.cosmos.base.query.v1beta1.PageResponse pagination = 3 [json_name = "pagination"];</code>
     */
    public Builder setPagination(
        com.cosmos.base.query.v1beta1.PageResponse.Builder builderForValue) {
      if (paginationBuilder_ == null) {
        pagination_ = builderForValue.build();
      } else {
        paginationBuilder_.setMessage(builderForValue.build());
      }
      bitField0_ |= 0x00000004;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * pagination defines an pagination for the response.
     * </pre>
     *
     * <code>.cosmos.base.query.v1beta1.PageResponse pagination = 3 [json_name = "pagination"];</code>
     */
    public Builder mergePagination(com.cosmos.base.query.v1beta1.PageResponse value) {
      if (paginationBuilder_ == null) {
        if (((bitField0_ & 0x00000004) != 0) &&
          pagination_ != null &&
          pagination_ != com.cosmos.base.query.v1beta1.PageResponse.getDefaultInstance()) {
          getPaginationBuilder().mergeFrom(value);
        } else {
          pagination_ = value;
        }
      } else {
        paginationBuilder_.mergeFrom(value);
      }
      bitField0_ |= 0x00000004;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * pagination defines an pagination for the response.
     * </pre>
     *
     * <code>.cosmos.base.query.v1beta1.PageResponse pagination = 3 [json_name = "pagination"];</code>
     */
    public Builder clearPagination() {
      bitField0_ = (bitField0_ & ~0x00000004);
      pagination_ = null;
      if (paginationBuilder_ != null) {
        paginationBuilder_.dispose();
        paginationBuilder_ = null;
      }
      onChanged();
      return this;
    }
    /**
     * <pre>
     * pagination defines an pagination for the response.
     * </pre>
     *
     * <code>.cosmos.base.query.v1beta1.PageResponse pagination = 3 [json_name = "pagination"];</code>
     */
    public com.cosmos.base.query.v1beta1.PageResponse.Builder getPaginationBuilder() {
      bitField0_ |= 0x00000004;
      onChanged();
      return getPaginationFieldBuilder().getBuilder();
    }
    /**
     * <pre>
     * pagination defines an pagination for the response.
     * </pre>
     *
     * <code>.cosmos.base.query.v1beta1.PageResponse pagination = 3 [json_name = "pagination"];</code>
     */
    public com.cosmos.base.query.v1beta1.PageResponseOrBuilder getPaginationOrBuilder() {
      if (paginationBuilder_ != null) {
        return paginationBuilder_.getMessageOrBuilder();
      } else {
        return pagination_ == null ?
            com.cosmos.base.query.v1beta1.PageResponse.getDefaultInstance() : pagination_;
      }
    }
    /**
     * <pre>
     * pagination defines an pagination for the response.
     * </pre>
     *
     * <code>.cosmos.base.query.v1beta1.PageResponse pagination = 3 [json_name = "pagination"];</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
        com.cosmos.base.query.v1beta1.PageResponse, com.cosmos.base.query.v1beta1.PageResponse.Builder, com.cosmos.base.query.v1beta1.PageResponseOrBuilder> 
        getPaginationFieldBuilder() {
      if (paginationBuilder_ == null) {
        paginationBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
            com.cosmos.base.query.v1beta1.PageResponse, com.cosmos.base.query.v1beta1.PageResponse.Builder, com.cosmos.base.query.v1beta1.PageResponseOrBuilder>(
                getPagination(),
                getParentForChildren(),
                isClean());
        pagination_ = null;
      }
      return paginationBuilder_;
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


    // @@protoc_insertion_point(builder_scope:cosmos.base.tendermint.v1beta1.GetLatestValidatorSetResponse)
  }

  // @@protoc_insertion_point(class_scope:cosmos.base.tendermint.v1beta1.GetLatestValidatorSetResponse)
  private static final com.cosmos.base.tendermint.v1beta1.GetLatestValidatorSetResponse DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new com.cosmos.base.tendermint.v1beta1.GetLatestValidatorSetResponse();
  }

  public static com.cosmos.base.tendermint.v1beta1.GetLatestValidatorSetResponse getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<GetLatestValidatorSetResponse>
      PARSER = new com.google.protobuf.AbstractParser<GetLatestValidatorSetResponse>() {
    @java.lang.Override
    public GetLatestValidatorSetResponse parsePartialFrom(
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

  public static com.google.protobuf.Parser<GetLatestValidatorSetResponse> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<GetLatestValidatorSetResponse> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public com.cosmos.base.tendermint.v1beta1.GetLatestValidatorSetResponse getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}
