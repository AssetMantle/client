// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: cosmos/distribution/v1beta1/genesis.proto

package com.cosmos.distribution.v1beta1;

/**
 * <pre>
 * ValidatorAccumulatedCommissionRecord is used for import / export via genesis
 * json.
 * </pre>
 *
 * Protobuf type {@code cosmos.distribution.v1beta1.ValidatorAccumulatedCommissionRecord}
 */
public final class ValidatorAccumulatedCommissionRecord extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:cosmos.distribution.v1beta1.ValidatorAccumulatedCommissionRecord)
    ValidatorAccumulatedCommissionRecordOrBuilder {
private static final long serialVersionUID = 0L;
  // Use ValidatorAccumulatedCommissionRecord.newBuilder() to construct.
  private ValidatorAccumulatedCommissionRecord(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private ValidatorAccumulatedCommissionRecord() {
    validatorAddress_ = "";
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new ValidatorAccumulatedCommissionRecord();
  }

  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return com.cosmos.distribution.v1beta1.GenesisProto.internal_static_cosmos_distribution_v1beta1_ValidatorAccumulatedCommissionRecord_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return com.cosmos.distribution.v1beta1.GenesisProto.internal_static_cosmos_distribution_v1beta1_ValidatorAccumulatedCommissionRecord_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            com.cosmos.distribution.v1beta1.ValidatorAccumulatedCommissionRecord.class, com.cosmos.distribution.v1beta1.ValidatorAccumulatedCommissionRecord.Builder.class);
  }

  public static final int VALIDATOR_ADDRESS_FIELD_NUMBER = 1;
  @SuppressWarnings("serial")
  private volatile java.lang.Object validatorAddress_ = "";
  /**
   * <pre>
   * validator_address is the address of the validator.
   * </pre>
   *
   * <code>string validator_address = 1 [json_name = "validatorAddress", (.gogoproto.moretags) = "yaml:&#92;"validator_address&#92;""];</code>
   * @return The validatorAddress.
   */
  @java.lang.Override
  public java.lang.String getValidatorAddress() {
    java.lang.Object ref = validatorAddress_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      validatorAddress_ = s;
      return s;
    }
  }
  /**
   * <pre>
   * validator_address is the address of the validator.
   * </pre>
   *
   * <code>string validator_address = 1 [json_name = "validatorAddress", (.gogoproto.moretags) = "yaml:&#92;"validator_address&#92;""];</code>
   * @return The bytes for validatorAddress.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getValidatorAddressBytes() {
    java.lang.Object ref = validatorAddress_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      validatorAddress_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int ACCUMULATED_FIELD_NUMBER = 2;
  private com.cosmos.distribution.v1beta1.ValidatorAccumulatedCommission accumulated_;
  /**
   * <pre>
   * accumulated is the accumulated commission of a validator.
   * </pre>
   *
   * <code>.cosmos.distribution.v1beta1.ValidatorAccumulatedCommission accumulated = 2 [json_name = "accumulated", (.gogoproto.nullable) = false, (.gogoproto.moretags) = "yaml:&#92;"accumulated&#92;""];</code>
   * @return Whether the accumulated field is set.
   */
  @java.lang.Override
  public boolean hasAccumulated() {
    return accumulated_ != null;
  }
  /**
   * <pre>
   * accumulated is the accumulated commission of a validator.
   * </pre>
   *
   * <code>.cosmos.distribution.v1beta1.ValidatorAccumulatedCommission accumulated = 2 [json_name = "accumulated", (.gogoproto.nullable) = false, (.gogoproto.moretags) = "yaml:&#92;"accumulated&#92;""];</code>
   * @return The accumulated.
   */
  @java.lang.Override
  public com.cosmos.distribution.v1beta1.ValidatorAccumulatedCommission getAccumulated() {
    return accumulated_ == null ? com.cosmos.distribution.v1beta1.ValidatorAccumulatedCommission.getDefaultInstance() : accumulated_;
  }
  /**
   * <pre>
   * accumulated is the accumulated commission of a validator.
   * </pre>
   *
   * <code>.cosmos.distribution.v1beta1.ValidatorAccumulatedCommission accumulated = 2 [json_name = "accumulated", (.gogoproto.nullable) = false, (.gogoproto.moretags) = "yaml:&#92;"accumulated&#92;""];</code>
   */
  @java.lang.Override
  public com.cosmos.distribution.v1beta1.ValidatorAccumulatedCommissionOrBuilder getAccumulatedOrBuilder() {
    return accumulated_ == null ? com.cosmos.distribution.v1beta1.ValidatorAccumulatedCommission.getDefaultInstance() : accumulated_;
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
    if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(validatorAddress_)) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 1, validatorAddress_);
    }
    if (accumulated_ != null) {
      output.writeMessage(2, getAccumulated());
    }
    getUnknownFields().writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(validatorAddress_)) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(1, validatorAddress_);
    }
    if (accumulated_ != null) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(2, getAccumulated());
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
    if (!(obj instanceof com.cosmos.distribution.v1beta1.ValidatorAccumulatedCommissionRecord)) {
      return super.equals(obj);
    }
    com.cosmos.distribution.v1beta1.ValidatorAccumulatedCommissionRecord other = (com.cosmos.distribution.v1beta1.ValidatorAccumulatedCommissionRecord) obj;

    if (!getValidatorAddress()
        .equals(other.getValidatorAddress())) return false;
    if (hasAccumulated() != other.hasAccumulated()) return false;
    if (hasAccumulated()) {
      if (!getAccumulated()
          .equals(other.getAccumulated())) return false;
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
    hash = (37 * hash) + VALIDATOR_ADDRESS_FIELD_NUMBER;
    hash = (53 * hash) + getValidatorAddress().hashCode();
    if (hasAccumulated()) {
      hash = (37 * hash) + ACCUMULATED_FIELD_NUMBER;
      hash = (53 * hash) + getAccumulated().hashCode();
    }
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static com.cosmos.distribution.v1beta1.ValidatorAccumulatedCommissionRecord parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.cosmos.distribution.v1beta1.ValidatorAccumulatedCommissionRecord parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.cosmos.distribution.v1beta1.ValidatorAccumulatedCommissionRecord parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.cosmos.distribution.v1beta1.ValidatorAccumulatedCommissionRecord parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.cosmos.distribution.v1beta1.ValidatorAccumulatedCommissionRecord parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.cosmos.distribution.v1beta1.ValidatorAccumulatedCommissionRecord parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.cosmos.distribution.v1beta1.ValidatorAccumulatedCommissionRecord parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.cosmos.distribution.v1beta1.ValidatorAccumulatedCommissionRecord parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.cosmos.distribution.v1beta1.ValidatorAccumulatedCommissionRecord parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static com.cosmos.distribution.v1beta1.ValidatorAccumulatedCommissionRecord parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.cosmos.distribution.v1beta1.ValidatorAccumulatedCommissionRecord parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.cosmos.distribution.v1beta1.ValidatorAccumulatedCommissionRecord parseFrom(
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
  public static Builder newBuilder(com.cosmos.distribution.v1beta1.ValidatorAccumulatedCommissionRecord prototype) {
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
   * ValidatorAccumulatedCommissionRecord is used for import / export via genesis
   * json.
   * </pre>
   *
   * Protobuf type {@code cosmos.distribution.v1beta1.ValidatorAccumulatedCommissionRecord}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:cosmos.distribution.v1beta1.ValidatorAccumulatedCommissionRecord)
      com.cosmos.distribution.v1beta1.ValidatorAccumulatedCommissionRecordOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return com.cosmos.distribution.v1beta1.GenesisProto.internal_static_cosmos_distribution_v1beta1_ValidatorAccumulatedCommissionRecord_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return com.cosmos.distribution.v1beta1.GenesisProto.internal_static_cosmos_distribution_v1beta1_ValidatorAccumulatedCommissionRecord_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              com.cosmos.distribution.v1beta1.ValidatorAccumulatedCommissionRecord.class, com.cosmos.distribution.v1beta1.ValidatorAccumulatedCommissionRecord.Builder.class);
    }

    // Construct using com.cosmos.distribution.v1beta1.ValidatorAccumulatedCommissionRecord.newBuilder()
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
      validatorAddress_ = "";
      accumulated_ = null;
      if (accumulatedBuilder_ != null) {
        accumulatedBuilder_.dispose();
        accumulatedBuilder_ = null;
      }
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return com.cosmos.distribution.v1beta1.GenesisProto.internal_static_cosmos_distribution_v1beta1_ValidatorAccumulatedCommissionRecord_descriptor;
    }

    @java.lang.Override
    public com.cosmos.distribution.v1beta1.ValidatorAccumulatedCommissionRecord getDefaultInstanceForType() {
      return com.cosmos.distribution.v1beta1.ValidatorAccumulatedCommissionRecord.getDefaultInstance();
    }

    @java.lang.Override
    public com.cosmos.distribution.v1beta1.ValidatorAccumulatedCommissionRecord build() {
      com.cosmos.distribution.v1beta1.ValidatorAccumulatedCommissionRecord result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public com.cosmos.distribution.v1beta1.ValidatorAccumulatedCommissionRecord buildPartial() {
      com.cosmos.distribution.v1beta1.ValidatorAccumulatedCommissionRecord result = new com.cosmos.distribution.v1beta1.ValidatorAccumulatedCommissionRecord(this);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartial0(com.cosmos.distribution.v1beta1.ValidatorAccumulatedCommissionRecord result) {
      int from_bitField0_ = bitField0_;
      if (((from_bitField0_ & 0x00000001) != 0)) {
        result.validatorAddress_ = validatorAddress_;
      }
      if (((from_bitField0_ & 0x00000002) != 0)) {
        result.accumulated_ = accumulatedBuilder_ == null
            ? accumulated_
            : accumulatedBuilder_.build();
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
      if (other instanceof com.cosmos.distribution.v1beta1.ValidatorAccumulatedCommissionRecord) {
        return mergeFrom((com.cosmos.distribution.v1beta1.ValidatorAccumulatedCommissionRecord)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(com.cosmos.distribution.v1beta1.ValidatorAccumulatedCommissionRecord other) {
      if (other == com.cosmos.distribution.v1beta1.ValidatorAccumulatedCommissionRecord.getDefaultInstance()) return this;
      if (!other.getValidatorAddress().isEmpty()) {
        validatorAddress_ = other.validatorAddress_;
        bitField0_ |= 0x00000001;
        onChanged();
      }
      if (other.hasAccumulated()) {
        mergeAccumulated(other.getAccumulated());
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
              validatorAddress_ = input.readStringRequireUtf8();
              bitField0_ |= 0x00000001;
              break;
            } // case 10
            case 18: {
              input.readMessage(
                  getAccumulatedFieldBuilder().getBuilder(),
                  extensionRegistry);
              bitField0_ |= 0x00000002;
              break;
            } // case 18
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

    private java.lang.Object validatorAddress_ = "";
    /**
     * <pre>
     * validator_address is the address of the validator.
     * </pre>
     *
     * <code>string validator_address = 1 [json_name = "validatorAddress", (.gogoproto.moretags) = "yaml:&#92;"validator_address&#92;""];</code>
     * @return The validatorAddress.
     */
    public java.lang.String getValidatorAddress() {
      java.lang.Object ref = validatorAddress_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        validatorAddress_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <pre>
     * validator_address is the address of the validator.
     * </pre>
     *
     * <code>string validator_address = 1 [json_name = "validatorAddress", (.gogoproto.moretags) = "yaml:&#92;"validator_address&#92;""];</code>
     * @return The bytes for validatorAddress.
     */
    public com.google.protobuf.ByteString
        getValidatorAddressBytes() {
      java.lang.Object ref = validatorAddress_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        validatorAddress_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <pre>
     * validator_address is the address of the validator.
     * </pre>
     *
     * <code>string validator_address = 1 [json_name = "validatorAddress", (.gogoproto.moretags) = "yaml:&#92;"validator_address&#92;""];</code>
     * @param value The validatorAddress to set.
     * @return This builder for chaining.
     */
    public Builder setValidatorAddress(
        java.lang.String value) {
      if (value == null) { throw new NullPointerException(); }
      validatorAddress_ = value;
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * validator_address is the address of the validator.
     * </pre>
     *
     * <code>string validator_address = 1 [json_name = "validatorAddress", (.gogoproto.moretags) = "yaml:&#92;"validator_address&#92;""];</code>
     * @return This builder for chaining.
     */
    public Builder clearValidatorAddress() {
      validatorAddress_ = getDefaultInstance().getValidatorAddress();
      bitField0_ = (bitField0_ & ~0x00000001);
      onChanged();
      return this;
    }
    /**
     * <pre>
     * validator_address is the address of the validator.
     * </pre>
     *
     * <code>string validator_address = 1 [json_name = "validatorAddress", (.gogoproto.moretags) = "yaml:&#92;"validator_address&#92;""];</code>
     * @param value The bytes for validatorAddress to set.
     * @return This builder for chaining.
     */
    public Builder setValidatorAddressBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) { throw new NullPointerException(); }
      checkByteStringIsUtf8(value);
      validatorAddress_ = value;
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }

    private com.cosmos.distribution.v1beta1.ValidatorAccumulatedCommission accumulated_;
    private com.google.protobuf.SingleFieldBuilderV3<
        com.cosmos.distribution.v1beta1.ValidatorAccumulatedCommission, com.cosmos.distribution.v1beta1.ValidatorAccumulatedCommission.Builder, com.cosmos.distribution.v1beta1.ValidatorAccumulatedCommissionOrBuilder> accumulatedBuilder_;
    /**
     * <pre>
     * accumulated is the accumulated commission of a validator.
     * </pre>
     *
     * <code>.cosmos.distribution.v1beta1.ValidatorAccumulatedCommission accumulated = 2 [json_name = "accumulated", (.gogoproto.nullable) = false, (.gogoproto.moretags) = "yaml:&#92;"accumulated&#92;""];</code>
     * @return Whether the accumulated field is set.
     */
    public boolean hasAccumulated() {
      return ((bitField0_ & 0x00000002) != 0);
    }
    /**
     * <pre>
     * accumulated is the accumulated commission of a validator.
     * </pre>
     *
     * <code>.cosmos.distribution.v1beta1.ValidatorAccumulatedCommission accumulated = 2 [json_name = "accumulated", (.gogoproto.nullable) = false, (.gogoproto.moretags) = "yaml:&#92;"accumulated&#92;""];</code>
     * @return The accumulated.
     */
    public com.cosmos.distribution.v1beta1.ValidatorAccumulatedCommission getAccumulated() {
      if (accumulatedBuilder_ == null) {
        return accumulated_ == null ? com.cosmos.distribution.v1beta1.ValidatorAccumulatedCommission.getDefaultInstance() : accumulated_;
      } else {
        return accumulatedBuilder_.getMessage();
      }
    }
    /**
     * <pre>
     * accumulated is the accumulated commission of a validator.
     * </pre>
     *
     * <code>.cosmos.distribution.v1beta1.ValidatorAccumulatedCommission accumulated = 2 [json_name = "accumulated", (.gogoproto.nullable) = false, (.gogoproto.moretags) = "yaml:&#92;"accumulated&#92;""];</code>
     */
    public Builder setAccumulated(com.cosmos.distribution.v1beta1.ValidatorAccumulatedCommission value) {
      if (accumulatedBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        accumulated_ = value;
      } else {
        accumulatedBuilder_.setMessage(value);
      }
      bitField0_ |= 0x00000002;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * accumulated is the accumulated commission of a validator.
     * </pre>
     *
     * <code>.cosmos.distribution.v1beta1.ValidatorAccumulatedCommission accumulated = 2 [json_name = "accumulated", (.gogoproto.nullable) = false, (.gogoproto.moretags) = "yaml:&#92;"accumulated&#92;""];</code>
     */
    public Builder setAccumulated(
        com.cosmos.distribution.v1beta1.ValidatorAccumulatedCommission.Builder builderForValue) {
      if (accumulatedBuilder_ == null) {
        accumulated_ = builderForValue.build();
      } else {
        accumulatedBuilder_.setMessage(builderForValue.build());
      }
      bitField0_ |= 0x00000002;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * accumulated is the accumulated commission of a validator.
     * </pre>
     *
     * <code>.cosmos.distribution.v1beta1.ValidatorAccumulatedCommission accumulated = 2 [json_name = "accumulated", (.gogoproto.nullable) = false, (.gogoproto.moretags) = "yaml:&#92;"accumulated&#92;""];</code>
     */
    public Builder mergeAccumulated(com.cosmos.distribution.v1beta1.ValidatorAccumulatedCommission value) {
      if (accumulatedBuilder_ == null) {
        if (((bitField0_ & 0x00000002) != 0) &&
          accumulated_ != null &&
          accumulated_ != com.cosmos.distribution.v1beta1.ValidatorAccumulatedCommission.getDefaultInstance()) {
          getAccumulatedBuilder().mergeFrom(value);
        } else {
          accumulated_ = value;
        }
      } else {
        accumulatedBuilder_.mergeFrom(value);
      }
      bitField0_ |= 0x00000002;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * accumulated is the accumulated commission of a validator.
     * </pre>
     *
     * <code>.cosmos.distribution.v1beta1.ValidatorAccumulatedCommission accumulated = 2 [json_name = "accumulated", (.gogoproto.nullable) = false, (.gogoproto.moretags) = "yaml:&#92;"accumulated&#92;""];</code>
     */
    public Builder clearAccumulated() {
      bitField0_ = (bitField0_ & ~0x00000002);
      accumulated_ = null;
      if (accumulatedBuilder_ != null) {
        accumulatedBuilder_.dispose();
        accumulatedBuilder_ = null;
      }
      onChanged();
      return this;
    }
    /**
     * <pre>
     * accumulated is the accumulated commission of a validator.
     * </pre>
     *
     * <code>.cosmos.distribution.v1beta1.ValidatorAccumulatedCommission accumulated = 2 [json_name = "accumulated", (.gogoproto.nullable) = false, (.gogoproto.moretags) = "yaml:&#92;"accumulated&#92;""];</code>
     */
    public com.cosmos.distribution.v1beta1.ValidatorAccumulatedCommission.Builder getAccumulatedBuilder() {
      bitField0_ |= 0x00000002;
      onChanged();
      return getAccumulatedFieldBuilder().getBuilder();
    }
    /**
     * <pre>
     * accumulated is the accumulated commission of a validator.
     * </pre>
     *
     * <code>.cosmos.distribution.v1beta1.ValidatorAccumulatedCommission accumulated = 2 [json_name = "accumulated", (.gogoproto.nullable) = false, (.gogoproto.moretags) = "yaml:&#92;"accumulated&#92;""];</code>
     */
    public com.cosmos.distribution.v1beta1.ValidatorAccumulatedCommissionOrBuilder getAccumulatedOrBuilder() {
      if (accumulatedBuilder_ != null) {
        return accumulatedBuilder_.getMessageOrBuilder();
      } else {
        return accumulated_ == null ?
            com.cosmos.distribution.v1beta1.ValidatorAccumulatedCommission.getDefaultInstance() : accumulated_;
      }
    }
    /**
     * <pre>
     * accumulated is the accumulated commission of a validator.
     * </pre>
     *
     * <code>.cosmos.distribution.v1beta1.ValidatorAccumulatedCommission accumulated = 2 [json_name = "accumulated", (.gogoproto.nullable) = false, (.gogoproto.moretags) = "yaml:&#92;"accumulated&#92;""];</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
        com.cosmos.distribution.v1beta1.ValidatorAccumulatedCommission, com.cosmos.distribution.v1beta1.ValidatorAccumulatedCommission.Builder, com.cosmos.distribution.v1beta1.ValidatorAccumulatedCommissionOrBuilder> 
        getAccumulatedFieldBuilder() {
      if (accumulatedBuilder_ == null) {
        accumulatedBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
            com.cosmos.distribution.v1beta1.ValidatorAccumulatedCommission, com.cosmos.distribution.v1beta1.ValidatorAccumulatedCommission.Builder, com.cosmos.distribution.v1beta1.ValidatorAccumulatedCommissionOrBuilder>(
                getAccumulated(),
                getParentForChildren(),
                isClean());
        accumulated_ = null;
      }
      return accumulatedBuilder_;
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


    // @@protoc_insertion_point(builder_scope:cosmos.distribution.v1beta1.ValidatorAccumulatedCommissionRecord)
  }

  // @@protoc_insertion_point(class_scope:cosmos.distribution.v1beta1.ValidatorAccumulatedCommissionRecord)
  private static final com.cosmos.distribution.v1beta1.ValidatorAccumulatedCommissionRecord DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new com.cosmos.distribution.v1beta1.ValidatorAccumulatedCommissionRecord();
  }

  public static com.cosmos.distribution.v1beta1.ValidatorAccumulatedCommissionRecord getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<ValidatorAccumulatedCommissionRecord>
      PARSER = new com.google.protobuf.AbstractParser<ValidatorAccumulatedCommissionRecord>() {
    @java.lang.Override
    public ValidatorAccumulatedCommissionRecord parsePartialFrom(
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

  public static com.google.protobuf.Parser<ValidatorAccumulatedCommissionRecord> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<ValidatorAccumulatedCommissionRecord> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public com.cosmos.distribution.v1beta1.ValidatorAccumulatedCommissionRecord getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

