// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: cosmos/vesting/v1beta1/vesting.proto

package com.cosmos.vesting.v1beta1;

/**
 * <pre>
 * DelayedVestingAccount implements the VestingAccount interface. It vests all
 * coins after a specific time, but non prior. In other words, it keeps them
 * locked until a specified time.
 * </pre>
 *
 * Protobuf type {@code cosmos.vesting.v1beta1.DelayedVestingAccount}
 */
public final class DelayedVestingAccount extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:cosmos.vesting.v1beta1.DelayedVestingAccount)
    DelayedVestingAccountOrBuilder {
private static final long serialVersionUID = 0L;
  // Use DelayedVestingAccount.newBuilder() to construct.
  private DelayedVestingAccount(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private DelayedVestingAccount() {
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new DelayedVestingAccount();
  }

  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return com.cosmos.vesting.v1beta1.VestingProto.internal_static_cosmos_vesting_v1beta1_DelayedVestingAccount_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return com.cosmos.vesting.v1beta1.VestingProto.internal_static_cosmos_vesting_v1beta1_DelayedVestingAccount_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            com.cosmos.vesting.v1beta1.DelayedVestingAccount.class, com.cosmos.vesting.v1beta1.DelayedVestingAccount.Builder.class);
  }

  public static final int BASE_VESTING_ACCOUNT_FIELD_NUMBER = 1;
  private com.cosmos.vesting.v1beta1.BaseVestingAccount baseVestingAccount_;
  /**
   * <code>.cosmos.vesting.v1beta1.BaseVestingAccount base_vesting_account = 1 [json_name = "baseVestingAccount", (.gogoproto.embed) = true];</code>
   * @return Whether the baseVestingAccount field is set.
   */
  @java.lang.Override
  public boolean hasBaseVestingAccount() {
    return baseVestingAccount_ != null;
  }
  /**
   * <code>.cosmos.vesting.v1beta1.BaseVestingAccount base_vesting_account = 1 [json_name = "baseVestingAccount", (.gogoproto.embed) = true];</code>
   * @return The baseVestingAccount.
   */
  @java.lang.Override
  public com.cosmos.vesting.v1beta1.BaseVestingAccount getBaseVestingAccount() {
    return baseVestingAccount_ == null ? com.cosmos.vesting.v1beta1.BaseVestingAccount.getDefaultInstance() : baseVestingAccount_;
  }
  /**
   * <code>.cosmos.vesting.v1beta1.BaseVestingAccount base_vesting_account = 1 [json_name = "baseVestingAccount", (.gogoproto.embed) = true];</code>
   */
  @java.lang.Override
  public com.cosmos.vesting.v1beta1.BaseVestingAccountOrBuilder getBaseVestingAccountOrBuilder() {
    return baseVestingAccount_ == null ? com.cosmos.vesting.v1beta1.BaseVestingAccount.getDefaultInstance() : baseVestingAccount_;
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
    if (baseVestingAccount_ != null) {
      output.writeMessage(1, getBaseVestingAccount());
    }
    getUnknownFields().writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (baseVestingAccount_ != null) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(1, getBaseVestingAccount());
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
    if (!(obj instanceof com.cosmos.vesting.v1beta1.DelayedVestingAccount)) {
      return super.equals(obj);
    }
    com.cosmos.vesting.v1beta1.DelayedVestingAccount other = (com.cosmos.vesting.v1beta1.DelayedVestingAccount) obj;

    if (hasBaseVestingAccount() != other.hasBaseVestingAccount()) return false;
    if (hasBaseVestingAccount()) {
      if (!getBaseVestingAccount()
          .equals(other.getBaseVestingAccount())) return false;
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
    if (hasBaseVestingAccount()) {
      hash = (37 * hash) + BASE_VESTING_ACCOUNT_FIELD_NUMBER;
      hash = (53 * hash) + getBaseVestingAccount().hashCode();
    }
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static com.cosmos.vesting.v1beta1.DelayedVestingAccount parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.cosmos.vesting.v1beta1.DelayedVestingAccount parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.cosmos.vesting.v1beta1.DelayedVestingAccount parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.cosmos.vesting.v1beta1.DelayedVestingAccount parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.cosmos.vesting.v1beta1.DelayedVestingAccount parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.cosmos.vesting.v1beta1.DelayedVestingAccount parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.cosmos.vesting.v1beta1.DelayedVestingAccount parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.cosmos.vesting.v1beta1.DelayedVestingAccount parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.cosmos.vesting.v1beta1.DelayedVestingAccount parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static com.cosmos.vesting.v1beta1.DelayedVestingAccount parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.cosmos.vesting.v1beta1.DelayedVestingAccount parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.cosmos.vesting.v1beta1.DelayedVestingAccount parseFrom(
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
  public static Builder newBuilder(com.cosmos.vesting.v1beta1.DelayedVestingAccount prototype) {
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
   * DelayedVestingAccount implements the VestingAccount interface. It vests all
   * coins after a specific time, but non prior. In other words, it keeps them
   * locked until a specified time.
   * </pre>
   *
   * Protobuf type {@code cosmos.vesting.v1beta1.DelayedVestingAccount}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:cosmos.vesting.v1beta1.DelayedVestingAccount)
      com.cosmos.vesting.v1beta1.DelayedVestingAccountOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return com.cosmos.vesting.v1beta1.VestingProto.internal_static_cosmos_vesting_v1beta1_DelayedVestingAccount_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return com.cosmos.vesting.v1beta1.VestingProto.internal_static_cosmos_vesting_v1beta1_DelayedVestingAccount_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              com.cosmos.vesting.v1beta1.DelayedVestingAccount.class, com.cosmos.vesting.v1beta1.DelayedVestingAccount.Builder.class);
    }

    // Construct using com.cosmos.vesting.v1beta1.DelayedVestingAccount.newBuilder()
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
      baseVestingAccount_ = null;
      if (baseVestingAccountBuilder_ != null) {
        baseVestingAccountBuilder_.dispose();
        baseVestingAccountBuilder_ = null;
      }
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return com.cosmos.vesting.v1beta1.VestingProto.internal_static_cosmos_vesting_v1beta1_DelayedVestingAccount_descriptor;
    }

    @java.lang.Override
    public com.cosmos.vesting.v1beta1.DelayedVestingAccount getDefaultInstanceForType() {
      return com.cosmos.vesting.v1beta1.DelayedVestingAccount.getDefaultInstance();
    }

    @java.lang.Override
    public com.cosmos.vesting.v1beta1.DelayedVestingAccount build() {
      com.cosmos.vesting.v1beta1.DelayedVestingAccount result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public com.cosmos.vesting.v1beta1.DelayedVestingAccount buildPartial() {
      com.cosmos.vesting.v1beta1.DelayedVestingAccount result = new com.cosmos.vesting.v1beta1.DelayedVestingAccount(this);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartial0(com.cosmos.vesting.v1beta1.DelayedVestingAccount result) {
      int from_bitField0_ = bitField0_;
      if (((from_bitField0_ & 0x00000001) != 0)) {
        result.baseVestingAccount_ = baseVestingAccountBuilder_ == null
            ? baseVestingAccount_
            : baseVestingAccountBuilder_.build();
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
      if (other instanceof com.cosmos.vesting.v1beta1.DelayedVestingAccount) {
        return mergeFrom((com.cosmos.vesting.v1beta1.DelayedVestingAccount)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(com.cosmos.vesting.v1beta1.DelayedVestingAccount other) {
      if (other == com.cosmos.vesting.v1beta1.DelayedVestingAccount.getDefaultInstance()) return this;
      if (other.hasBaseVestingAccount()) {
        mergeBaseVestingAccount(other.getBaseVestingAccount());
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
                  getBaseVestingAccountFieldBuilder().getBuilder(),
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

    private com.cosmos.vesting.v1beta1.BaseVestingAccount baseVestingAccount_;
    private com.google.protobuf.SingleFieldBuilderV3<
        com.cosmos.vesting.v1beta1.BaseVestingAccount, com.cosmos.vesting.v1beta1.BaseVestingAccount.Builder, com.cosmos.vesting.v1beta1.BaseVestingAccountOrBuilder> baseVestingAccountBuilder_;
    /**
     * <code>.cosmos.vesting.v1beta1.BaseVestingAccount base_vesting_account = 1 [json_name = "baseVestingAccount", (.gogoproto.embed) = true];</code>
     * @return Whether the baseVestingAccount field is set.
     */
    public boolean hasBaseVestingAccount() {
      return ((bitField0_ & 0x00000001) != 0);
    }
    /**
     * <code>.cosmos.vesting.v1beta1.BaseVestingAccount base_vesting_account = 1 [json_name = "baseVestingAccount", (.gogoproto.embed) = true];</code>
     * @return The baseVestingAccount.
     */
    public com.cosmos.vesting.v1beta1.BaseVestingAccount getBaseVestingAccount() {
      if (baseVestingAccountBuilder_ == null) {
        return baseVestingAccount_ == null ? com.cosmos.vesting.v1beta1.BaseVestingAccount.getDefaultInstance() : baseVestingAccount_;
      } else {
        return baseVestingAccountBuilder_.getMessage();
      }
    }
    /**
     * <code>.cosmos.vesting.v1beta1.BaseVestingAccount base_vesting_account = 1 [json_name = "baseVestingAccount", (.gogoproto.embed) = true];</code>
     */
    public Builder setBaseVestingAccount(com.cosmos.vesting.v1beta1.BaseVestingAccount value) {
      if (baseVestingAccountBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        baseVestingAccount_ = value;
      } else {
        baseVestingAccountBuilder_.setMessage(value);
      }
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <code>.cosmos.vesting.v1beta1.BaseVestingAccount base_vesting_account = 1 [json_name = "baseVestingAccount", (.gogoproto.embed) = true];</code>
     */
    public Builder setBaseVestingAccount(
        com.cosmos.vesting.v1beta1.BaseVestingAccount.Builder builderForValue) {
      if (baseVestingAccountBuilder_ == null) {
        baseVestingAccount_ = builderForValue.build();
      } else {
        baseVestingAccountBuilder_.setMessage(builderForValue.build());
      }
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <code>.cosmos.vesting.v1beta1.BaseVestingAccount base_vesting_account = 1 [json_name = "baseVestingAccount", (.gogoproto.embed) = true];</code>
     */
    public Builder mergeBaseVestingAccount(com.cosmos.vesting.v1beta1.BaseVestingAccount value) {
      if (baseVestingAccountBuilder_ == null) {
        if (((bitField0_ & 0x00000001) != 0) &&
          baseVestingAccount_ != null &&
          baseVestingAccount_ != com.cosmos.vesting.v1beta1.BaseVestingAccount.getDefaultInstance()) {
          getBaseVestingAccountBuilder().mergeFrom(value);
        } else {
          baseVestingAccount_ = value;
        }
      } else {
        baseVestingAccountBuilder_.mergeFrom(value);
      }
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <code>.cosmos.vesting.v1beta1.BaseVestingAccount base_vesting_account = 1 [json_name = "baseVestingAccount", (.gogoproto.embed) = true];</code>
     */
    public Builder clearBaseVestingAccount() {
      bitField0_ = (bitField0_ & ~0x00000001);
      baseVestingAccount_ = null;
      if (baseVestingAccountBuilder_ != null) {
        baseVestingAccountBuilder_.dispose();
        baseVestingAccountBuilder_ = null;
      }
      onChanged();
      return this;
    }
    /**
     * <code>.cosmos.vesting.v1beta1.BaseVestingAccount base_vesting_account = 1 [json_name = "baseVestingAccount", (.gogoproto.embed) = true];</code>
     */
    public com.cosmos.vesting.v1beta1.BaseVestingAccount.Builder getBaseVestingAccountBuilder() {
      bitField0_ |= 0x00000001;
      onChanged();
      return getBaseVestingAccountFieldBuilder().getBuilder();
    }
    /**
     * <code>.cosmos.vesting.v1beta1.BaseVestingAccount base_vesting_account = 1 [json_name = "baseVestingAccount", (.gogoproto.embed) = true];</code>
     */
    public com.cosmos.vesting.v1beta1.BaseVestingAccountOrBuilder getBaseVestingAccountOrBuilder() {
      if (baseVestingAccountBuilder_ != null) {
        return baseVestingAccountBuilder_.getMessageOrBuilder();
      } else {
        return baseVestingAccount_ == null ?
            com.cosmos.vesting.v1beta1.BaseVestingAccount.getDefaultInstance() : baseVestingAccount_;
      }
    }
    /**
     * <code>.cosmos.vesting.v1beta1.BaseVestingAccount base_vesting_account = 1 [json_name = "baseVestingAccount", (.gogoproto.embed) = true];</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
        com.cosmos.vesting.v1beta1.BaseVestingAccount, com.cosmos.vesting.v1beta1.BaseVestingAccount.Builder, com.cosmos.vesting.v1beta1.BaseVestingAccountOrBuilder> 
        getBaseVestingAccountFieldBuilder() {
      if (baseVestingAccountBuilder_ == null) {
        baseVestingAccountBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
            com.cosmos.vesting.v1beta1.BaseVestingAccount, com.cosmos.vesting.v1beta1.BaseVestingAccount.Builder, com.cosmos.vesting.v1beta1.BaseVestingAccountOrBuilder>(
                getBaseVestingAccount(),
                getParentForChildren(),
                isClean());
        baseVestingAccount_ = null;
      }
      return baseVestingAccountBuilder_;
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


    // @@protoc_insertion_point(builder_scope:cosmos.vesting.v1beta1.DelayedVestingAccount)
  }

  // @@protoc_insertion_point(class_scope:cosmos.vesting.v1beta1.DelayedVestingAccount)
  private static final com.cosmos.vesting.v1beta1.DelayedVestingAccount DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new com.cosmos.vesting.v1beta1.DelayedVestingAccount();
  }

  public static com.cosmos.vesting.v1beta1.DelayedVestingAccount getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<DelayedVestingAccount>
      PARSER = new com.google.protobuf.AbstractParser<DelayedVestingAccount>() {
    @java.lang.Override
    public DelayedVestingAccount parsePartialFrom(
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

  public static com.google.protobuf.Parser<DelayedVestingAccount> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<DelayedVestingAccount> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public com.cosmos.vesting.v1beta1.DelayedVestingAccount getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

