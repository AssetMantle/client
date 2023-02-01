// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: tendermint/types/validator.proto

package com.tendermint.types;

/**
 * Protobuf type {@code tendermint.types.ValidatorSet}
 */
public final class ValidatorSet extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:tendermint.types.ValidatorSet)
    ValidatorSetOrBuilder {
private static final long serialVersionUID = 0L;
  // Use ValidatorSet.newBuilder() to construct.
  private ValidatorSet(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private ValidatorSet() {
    validators_ = java.util.Collections.emptyList();
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new ValidatorSet();
  }

  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return com.tendermint.types.ValidatorProto.internal_static_tendermint_types_ValidatorSet_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return com.tendermint.types.ValidatorProto.internal_static_tendermint_types_ValidatorSet_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            com.tendermint.types.ValidatorSet.class, com.tendermint.types.ValidatorSet.Builder.class);
  }

  public static final int VALIDATORS_FIELD_NUMBER = 1;
  @SuppressWarnings("serial")
  private java.util.List<com.tendermint.types.Validator> validators_;
  /**
   * <code>repeated .tendermint.types.Validator validators = 1 [json_name = "validators"];</code>
   */
  @java.lang.Override
  public java.util.List<com.tendermint.types.Validator> getValidatorsList() {
    return validators_;
  }
  /**
   * <code>repeated .tendermint.types.Validator validators = 1 [json_name = "validators"];</code>
   */
  @java.lang.Override
  public java.util.List<? extends com.tendermint.types.ValidatorOrBuilder> 
      getValidatorsOrBuilderList() {
    return validators_;
  }
  /**
   * <code>repeated .tendermint.types.Validator validators = 1 [json_name = "validators"];</code>
   */
  @java.lang.Override
  public int getValidatorsCount() {
    return validators_.size();
  }
  /**
   * <code>repeated .tendermint.types.Validator validators = 1 [json_name = "validators"];</code>
   */
  @java.lang.Override
  public com.tendermint.types.Validator getValidators(int index) {
    return validators_.get(index);
  }
  /**
   * <code>repeated .tendermint.types.Validator validators = 1 [json_name = "validators"];</code>
   */
  @java.lang.Override
  public com.tendermint.types.ValidatorOrBuilder getValidatorsOrBuilder(
      int index) {
    return validators_.get(index);
  }

  public static final int PROPOSER_FIELD_NUMBER = 2;
  private com.tendermint.types.Validator proposer_;
  /**
   * <code>.tendermint.types.Validator proposer = 2 [json_name = "proposer"];</code>
   * @return Whether the proposer field is set.
   */
  @java.lang.Override
  public boolean hasProposer() {
    return proposer_ != null;
  }
  /**
   * <code>.tendermint.types.Validator proposer = 2 [json_name = "proposer"];</code>
   * @return The proposer.
   */
  @java.lang.Override
  public com.tendermint.types.Validator getProposer() {
    return proposer_ == null ? com.tendermint.types.Validator.getDefaultInstance() : proposer_;
  }
  /**
   * <code>.tendermint.types.Validator proposer = 2 [json_name = "proposer"];</code>
   */
  @java.lang.Override
  public com.tendermint.types.ValidatorOrBuilder getProposerOrBuilder() {
    return proposer_ == null ? com.tendermint.types.Validator.getDefaultInstance() : proposer_;
  }

  public static final int TOTAL_VOTING_POWER_FIELD_NUMBER = 3;
  private long totalVotingPower_ = 0L;
  /**
   * <code>int64 total_voting_power = 3 [json_name = "totalVotingPower"];</code>
   * @return The totalVotingPower.
   */
  @java.lang.Override
  public long getTotalVotingPower() {
    return totalVotingPower_;
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
    for (int i = 0; i < validators_.size(); i++) {
      output.writeMessage(1, validators_.get(i));
    }
    if (proposer_ != null) {
      output.writeMessage(2, getProposer());
    }
    if (totalVotingPower_ != 0L) {
      output.writeInt64(3, totalVotingPower_);
    }
    getUnknownFields().writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    for (int i = 0; i < validators_.size(); i++) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(1, validators_.get(i));
    }
    if (proposer_ != null) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(2, getProposer());
    }
    if (totalVotingPower_ != 0L) {
      size += com.google.protobuf.CodedOutputStream
        .computeInt64Size(3, totalVotingPower_);
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
    if (!(obj instanceof com.tendermint.types.ValidatorSet)) {
      return super.equals(obj);
    }
    com.tendermint.types.ValidatorSet other = (com.tendermint.types.ValidatorSet) obj;

    if (!getValidatorsList()
        .equals(other.getValidatorsList())) return false;
    if (hasProposer() != other.hasProposer()) return false;
    if (hasProposer()) {
      if (!getProposer()
          .equals(other.getProposer())) return false;
    }
    if (getTotalVotingPower()
        != other.getTotalVotingPower()) return false;
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
    if (getValidatorsCount() > 0) {
      hash = (37 * hash) + VALIDATORS_FIELD_NUMBER;
      hash = (53 * hash) + getValidatorsList().hashCode();
    }
    if (hasProposer()) {
      hash = (37 * hash) + PROPOSER_FIELD_NUMBER;
      hash = (53 * hash) + getProposer().hashCode();
    }
    hash = (37 * hash) + TOTAL_VOTING_POWER_FIELD_NUMBER;
    hash = (53 * hash) + com.google.protobuf.Internal.hashLong(
        getTotalVotingPower());
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static com.tendermint.types.ValidatorSet parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.tendermint.types.ValidatorSet parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.tendermint.types.ValidatorSet parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.tendermint.types.ValidatorSet parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.tendermint.types.ValidatorSet parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.tendermint.types.ValidatorSet parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.tendermint.types.ValidatorSet parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.tendermint.types.ValidatorSet parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.tendermint.types.ValidatorSet parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static com.tendermint.types.ValidatorSet parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.tendermint.types.ValidatorSet parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.tendermint.types.ValidatorSet parseFrom(
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
  public static Builder newBuilder(com.tendermint.types.ValidatorSet prototype) {
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
   * Protobuf type {@code tendermint.types.ValidatorSet}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:tendermint.types.ValidatorSet)
      com.tendermint.types.ValidatorSetOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return com.tendermint.types.ValidatorProto.internal_static_tendermint_types_ValidatorSet_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return com.tendermint.types.ValidatorProto.internal_static_tendermint_types_ValidatorSet_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              com.tendermint.types.ValidatorSet.class, com.tendermint.types.ValidatorSet.Builder.class);
    }

    // Construct using com.tendermint.types.ValidatorSet.newBuilder()
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
      if (validatorsBuilder_ == null) {
        validators_ = java.util.Collections.emptyList();
      } else {
        validators_ = null;
        validatorsBuilder_.clear();
      }
      bitField0_ = (bitField0_ & ~0x00000001);
      proposer_ = null;
      if (proposerBuilder_ != null) {
        proposerBuilder_.dispose();
        proposerBuilder_ = null;
      }
      totalVotingPower_ = 0L;
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return com.tendermint.types.ValidatorProto.internal_static_tendermint_types_ValidatorSet_descriptor;
    }

    @java.lang.Override
    public com.tendermint.types.ValidatorSet getDefaultInstanceForType() {
      return com.tendermint.types.ValidatorSet.getDefaultInstance();
    }

    @java.lang.Override
    public com.tendermint.types.ValidatorSet build() {
      com.tendermint.types.ValidatorSet result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public com.tendermint.types.ValidatorSet buildPartial() {
      com.tendermint.types.ValidatorSet result = new com.tendermint.types.ValidatorSet(this);
      buildPartialRepeatedFields(result);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartialRepeatedFields(com.tendermint.types.ValidatorSet result) {
      if (validatorsBuilder_ == null) {
        if (((bitField0_ & 0x00000001) != 0)) {
          validators_ = java.util.Collections.unmodifiableList(validators_);
          bitField0_ = (bitField0_ & ~0x00000001);
        }
        result.validators_ = validators_;
      } else {
        result.validators_ = validatorsBuilder_.build();
      }
    }

    private void buildPartial0(com.tendermint.types.ValidatorSet result) {
      int from_bitField0_ = bitField0_;
      if (((from_bitField0_ & 0x00000002) != 0)) {
        result.proposer_ = proposerBuilder_ == null
            ? proposer_
            : proposerBuilder_.build();
      }
      if (((from_bitField0_ & 0x00000004) != 0)) {
        result.totalVotingPower_ = totalVotingPower_;
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
      if (other instanceof com.tendermint.types.ValidatorSet) {
        return mergeFrom((com.tendermint.types.ValidatorSet)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(com.tendermint.types.ValidatorSet other) {
      if (other == com.tendermint.types.ValidatorSet.getDefaultInstance()) return this;
      if (validatorsBuilder_ == null) {
        if (!other.validators_.isEmpty()) {
          if (validators_.isEmpty()) {
            validators_ = other.validators_;
            bitField0_ = (bitField0_ & ~0x00000001);
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
            bitField0_ = (bitField0_ & ~0x00000001);
            validatorsBuilder_ = 
              com.google.protobuf.GeneratedMessageV3.alwaysUseFieldBuilders ?
                 getValidatorsFieldBuilder() : null;
          } else {
            validatorsBuilder_.addAllMessages(other.validators_);
          }
        }
      }
      if (other.hasProposer()) {
        mergeProposer(other.getProposer());
      }
      if (other.getTotalVotingPower() != 0L) {
        setTotalVotingPower(other.getTotalVotingPower());
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
              com.tendermint.types.Validator m =
                  input.readMessage(
                      com.tendermint.types.Validator.parser(),
                      extensionRegistry);
              if (validatorsBuilder_ == null) {
                ensureValidatorsIsMutable();
                validators_.add(m);
              } else {
                validatorsBuilder_.addMessage(m);
              }
              break;
            } // case 10
            case 18: {
              input.readMessage(
                  getProposerFieldBuilder().getBuilder(),
                  extensionRegistry);
              bitField0_ |= 0x00000002;
              break;
            } // case 18
            case 24: {
              totalVotingPower_ = input.readInt64();
              bitField0_ |= 0x00000004;
              break;
            } // case 24
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

    private java.util.List<com.tendermint.types.Validator> validators_ =
      java.util.Collections.emptyList();
    private void ensureValidatorsIsMutable() {
      if (!((bitField0_ & 0x00000001) != 0)) {
        validators_ = new java.util.ArrayList<com.tendermint.types.Validator>(validators_);
        bitField0_ |= 0x00000001;
       }
    }

    private com.google.protobuf.RepeatedFieldBuilderV3<
        com.tendermint.types.Validator, com.tendermint.types.Validator.Builder, com.tendermint.types.ValidatorOrBuilder> validatorsBuilder_;

    /**
     * <code>repeated .tendermint.types.Validator validators = 1 [json_name = "validators"];</code>
     */
    public java.util.List<com.tendermint.types.Validator> getValidatorsList() {
      if (validatorsBuilder_ == null) {
        return java.util.Collections.unmodifiableList(validators_);
      } else {
        return validatorsBuilder_.getMessageList();
      }
    }
    /**
     * <code>repeated .tendermint.types.Validator validators = 1 [json_name = "validators"];</code>
     */
    public int getValidatorsCount() {
      if (validatorsBuilder_ == null) {
        return validators_.size();
      } else {
        return validatorsBuilder_.getCount();
      }
    }
    /**
     * <code>repeated .tendermint.types.Validator validators = 1 [json_name = "validators"];</code>
     */
    public com.tendermint.types.Validator getValidators(int index) {
      if (validatorsBuilder_ == null) {
        return validators_.get(index);
      } else {
        return validatorsBuilder_.getMessage(index);
      }
    }
    /**
     * <code>repeated .tendermint.types.Validator validators = 1 [json_name = "validators"];</code>
     */
    public Builder setValidators(
        int index, com.tendermint.types.Validator value) {
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
     * <code>repeated .tendermint.types.Validator validators = 1 [json_name = "validators"];</code>
     */
    public Builder setValidators(
        int index, com.tendermint.types.Validator.Builder builderForValue) {
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
     * <code>repeated .tendermint.types.Validator validators = 1 [json_name = "validators"];</code>
     */
    public Builder addValidators(com.tendermint.types.Validator value) {
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
     * <code>repeated .tendermint.types.Validator validators = 1 [json_name = "validators"];</code>
     */
    public Builder addValidators(
        int index, com.tendermint.types.Validator value) {
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
     * <code>repeated .tendermint.types.Validator validators = 1 [json_name = "validators"];</code>
     */
    public Builder addValidators(
        com.tendermint.types.Validator.Builder builderForValue) {
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
     * <code>repeated .tendermint.types.Validator validators = 1 [json_name = "validators"];</code>
     */
    public Builder addValidators(
        int index, com.tendermint.types.Validator.Builder builderForValue) {
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
     * <code>repeated .tendermint.types.Validator validators = 1 [json_name = "validators"];</code>
     */
    public Builder addAllValidators(
        java.lang.Iterable<? extends com.tendermint.types.Validator> values) {
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
     * <code>repeated .tendermint.types.Validator validators = 1 [json_name = "validators"];</code>
     */
    public Builder clearValidators() {
      if (validatorsBuilder_ == null) {
        validators_ = java.util.Collections.emptyList();
        bitField0_ = (bitField0_ & ~0x00000001);
        onChanged();
      } else {
        validatorsBuilder_.clear();
      }
      return this;
    }
    /**
     * <code>repeated .tendermint.types.Validator validators = 1 [json_name = "validators"];</code>
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
     * <code>repeated .tendermint.types.Validator validators = 1 [json_name = "validators"];</code>
     */
    public com.tendermint.types.Validator.Builder getValidatorsBuilder(
        int index) {
      return getValidatorsFieldBuilder().getBuilder(index);
    }
    /**
     * <code>repeated .tendermint.types.Validator validators = 1 [json_name = "validators"];</code>
     */
    public com.tendermint.types.ValidatorOrBuilder getValidatorsOrBuilder(
        int index) {
      if (validatorsBuilder_ == null) {
        return validators_.get(index);  } else {
        return validatorsBuilder_.getMessageOrBuilder(index);
      }
    }
    /**
     * <code>repeated .tendermint.types.Validator validators = 1 [json_name = "validators"];</code>
     */
    public java.util.List<? extends com.tendermint.types.ValidatorOrBuilder> 
         getValidatorsOrBuilderList() {
      if (validatorsBuilder_ != null) {
        return validatorsBuilder_.getMessageOrBuilderList();
      } else {
        return java.util.Collections.unmodifiableList(validators_);
      }
    }
    /**
     * <code>repeated .tendermint.types.Validator validators = 1 [json_name = "validators"];</code>
     */
    public com.tendermint.types.Validator.Builder addValidatorsBuilder() {
      return getValidatorsFieldBuilder().addBuilder(
          com.tendermint.types.Validator.getDefaultInstance());
    }
    /**
     * <code>repeated .tendermint.types.Validator validators = 1 [json_name = "validators"];</code>
     */
    public com.tendermint.types.Validator.Builder addValidatorsBuilder(
        int index) {
      return getValidatorsFieldBuilder().addBuilder(
          index, com.tendermint.types.Validator.getDefaultInstance());
    }
    /**
     * <code>repeated .tendermint.types.Validator validators = 1 [json_name = "validators"];</code>
     */
    public java.util.List<com.tendermint.types.Validator.Builder> 
         getValidatorsBuilderList() {
      return getValidatorsFieldBuilder().getBuilderList();
    }
    private com.google.protobuf.RepeatedFieldBuilderV3<
        com.tendermint.types.Validator, com.tendermint.types.Validator.Builder, com.tendermint.types.ValidatorOrBuilder> 
        getValidatorsFieldBuilder() {
      if (validatorsBuilder_ == null) {
        validatorsBuilder_ = new com.google.protobuf.RepeatedFieldBuilderV3<
            com.tendermint.types.Validator, com.tendermint.types.Validator.Builder, com.tendermint.types.ValidatorOrBuilder>(
                validators_,
                ((bitField0_ & 0x00000001) != 0),
                getParentForChildren(),
                isClean());
        validators_ = null;
      }
      return validatorsBuilder_;
    }

    private com.tendermint.types.Validator proposer_;
    private com.google.protobuf.SingleFieldBuilderV3<
        com.tendermint.types.Validator, com.tendermint.types.Validator.Builder, com.tendermint.types.ValidatorOrBuilder> proposerBuilder_;
    /**
     * <code>.tendermint.types.Validator proposer = 2 [json_name = "proposer"];</code>
     * @return Whether the proposer field is set.
     */
    public boolean hasProposer() {
      return ((bitField0_ & 0x00000002) != 0);
    }
    /**
     * <code>.tendermint.types.Validator proposer = 2 [json_name = "proposer"];</code>
     * @return The proposer.
     */
    public com.tendermint.types.Validator getProposer() {
      if (proposerBuilder_ == null) {
        return proposer_ == null ? com.tendermint.types.Validator.getDefaultInstance() : proposer_;
      } else {
        return proposerBuilder_.getMessage();
      }
    }
    /**
     * <code>.tendermint.types.Validator proposer = 2 [json_name = "proposer"];</code>
     */
    public Builder setProposer(com.tendermint.types.Validator value) {
      if (proposerBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        proposer_ = value;
      } else {
        proposerBuilder_.setMessage(value);
      }
      bitField0_ |= 0x00000002;
      onChanged();
      return this;
    }
    /**
     * <code>.tendermint.types.Validator proposer = 2 [json_name = "proposer"];</code>
     */
    public Builder setProposer(
        com.tendermint.types.Validator.Builder builderForValue) {
      if (proposerBuilder_ == null) {
        proposer_ = builderForValue.build();
      } else {
        proposerBuilder_.setMessage(builderForValue.build());
      }
      bitField0_ |= 0x00000002;
      onChanged();
      return this;
    }
    /**
     * <code>.tendermint.types.Validator proposer = 2 [json_name = "proposer"];</code>
     */
    public Builder mergeProposer(com.tendermint.types.Validator value) {
      if (proposerBuilder_ == null) {
        if (((bitField0_ & 0x00000002) != 0) &&
          proposer_ != null &&
          proposer_ != com.tendermint.types.Validator.getDefaultInstance()) {
          getProposerBuilder().mergeFrom(value);
        } else {
          proposer_ = value;
        }
      } else {
        proposerBuilder_.mergeFrom(value);
      }
      bitField0_ |= 0x00000002;
      onChanged();
      return this;
    }
    /**
     * <code>.tendermint.types.Validator proposer = 2 [json_name = "proposer"];</code>
     */
    public Builder clearProposer() {
      bitField0_ = (bitField0_ & ~0x00000002);
      proposer_ = null;
      if (proposerBuilder_ != null) {
        proposerBuilder_.dispose();
        proposerBuilder_ = null;
      }
      onChanged();
      return this;
    }
    /**
     * <code>.tendermint.types.Validator proposer = 2 [json_name = "proposer"];</code>
     */
    public com.tendermint.types.Validator.Builder getProposerBuilder() {
      bitField0_ |= 0x00000002;
      onChanged();
      return getProposerFieldBuilder().getBuilder();
    }
    /**
     * <code>.tendermint.types.Validator proposer = 2 [json_name = "proposer"];</code>
     */
    public com.tendermint.types.ValidatorOrBuilder getProposerOrBuilder() {
      if (proposerBuilder_ != null) {
        return proposerBuilder_.getMessageOrBuilder();
      } else {
        return proposer_ == null ?
            com.tendermint.types.Validator.getDefaultInstance() : proposer_;
      }
    }
    /**
     * <code>.tendermint.types.Validator proposer = 2 [json_name = "proposer"];</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
        com.tendermint.types.Validator, com.tendermint.types.Validator.Builder, com.tendermint.types.ValidatorOrBuilder> 
        getProposerFieldBuilder() {
      if (proposerBuilder_ == null) {
        proposerBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
            com.tendermint.types.Validator, com.tendermint.types.Validator.Builder, com.tendermint.types.ValidatorOrBuilder>(
                getProposer(),
                getParentForChildren(),
                isClean());
        proposer_ = null;
      }
      return proposerBuilder_;
    }

    private long totalVotingPower_ ;
    /**
     * <code>int64 total_voting_power = 3 [json_name = "totalVotingPower"];</code>
     * @return The totalVotingPower.
     */
    @java.lang.Override
    public long getTotalVotingPower() {
      return totalVotingPower_;
    }
    /**
     * <code>int64 total_voting_power = 3 [json_name = "totalVotingPower"];</code>
     * @param value The totalVotingPower to set.
     * @return This builder for chaining.
     */
    public Builder setTotalVotingPower(long value) {

      totalVotingPower_ = value;
      bitField0_ |= 0x00000004;
      onChanged();
      return this;
    }
    /**
     * <code>int64 total_voting_power = 3 [json_name = "totalVotingPower"];</code>
     * @return This builder for chaining.
     */
    public Builder clearTotalVotingPower() {
      bitField0_ = (bitField0_ & ~0x00000004);
      totalVotingPower_ = 0L;
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


    // @@protoc_insertion_point(builder_scope:tendermint.types.ValidatorSet)
  }

  // @@protoc_insertion_point(class_scope:tendermint.types.ValidatorSet)
  private static final com.tendermint.types.ValidatorSet DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new com.tendermint.types.ValidatorSet();
  }

  public static com.tendermint.types.ValidatorSet getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<ValidatorSet>
      PARSER = new com.google.protobuf.AbstractParser<ValidatorSet>() {
    @java.lang.Override
    public ValidatorSet parsePartialFrom(
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

  public static com.google.protobuf.Parser<ValidatorSet> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<ValidatorSet> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public com.tendermint.types.ValidatorSet getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

