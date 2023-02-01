// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: cosmos/authz/v1beta1/genesis.proto

package com.cosmos.authz.v1beta1;

/**
 * <pre>
 * GenesisState defines the authz module's genesis state.
 * </pre>
 *
 * Protobuf type {@code cosmos.authz.v1beta1.GenesisState}
 */
public final class GenesisState extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:cosmos.authz.v1beta1.GenesisState)
    GenesisStateOrBuilder {
private static final long serialVersionUID = 0L;
  // Use GenesisState.newBuilder() to construct.
  private GenesisState(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private GenesisState() {
    authorization_ = java.util.Collections.emptyList();
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new GenesisState();
  }

  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return com.cosmos.authz.v1beta1.GenesisProto.internal_static_cosmos_authz_v1beta1_GenesisState_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return com.cosmos.authz.v1beta1.GenesisProto.internal_static_cosmos_authz_v1beta1_GenesisState_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            com.cosmos.authz.v1beta1.GenesisState.class, com.cosmos.authz.v1beta1.GenesisState.Builder.class);
  }

  public static final int AUTHORIZATION_FIELD_NUMBER = 1;
  @SuppressWarnings("serial")
  private java.util.List<com.cosmos.authz.v1beta1.GrantAuthorization> authorization_;
  /**
   * <code>repeated .cosmos.authz.v1beta1.GrantAuthorization authorization = 1 [json_name = "authorization", (.gogoproto.nullable) = false];</code>
   */
  @java.lang.Override
  public java.util.List<com.cosmos.authz.v1beta1.GrantAuthorization> getAuthorizationList() {
    return authorization_;
  }
  /**
   * <code>repeated .cosmos.authz.v1beta1.GrantAuthorization authorization = 1 [json_name = "authorization", (.gogoproto.nullable) = false];</code>
   */
  @java.lang.Override
  public java.util.List<? extends com.cosmos.authz.v1beta1.GrantAuthorizationOrBuilder> 
      getAuthorizationOrBuilderList() {
    return authorization_;
  }
  /**
   * <code>repeated .cosmos.authz.v1beta1.GrantAuthorization authorization = 1 [json_name = "authorization", (.gogoproto.nullable) = false];</code>
   */
  @java.lang.Override
  public int getAuthorizationCount() {
    return authorization_.size();
  }
  /**
   * <code>repeated .cosmos.authz.v1beta1.GrantAuthorization authorization = 1 [json_name = "authorization", (.gogoproto.nullable) = false];</code>
   */
  @java.lang.Override
  public com.cosmos.authz.v1beta1.GrantAuthorization getAuthorization(int index) {
    return authorization_.get(index);
  }
  /**
   * <code>repeated .cosmos.authz.v1beta1.GrantAuthorization authorization = 1 [json_name = "authorization", (.gogoproto.nullable) = false];</code>
   */
  @java.lang.Override
  public com.cosmos.authz.v1beta1.GrantAuthorizationOrBuilder getAuthorizationOrBuilder(
      int index) {
    return authorization_.get(index);
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
    for (int i = 0; i < authorization_.size(); i++) {
      output.writeMessage(1, authorization_.get(i));
    }
    getUnknownFields().writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    for (int i = 0; i < authorization_.size(); i++) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(1, authorization_.get(i));
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
    if (!(obj instanceof com.cosmos.authz.v1beta1.GenesisState)) {
      return super.equals(obj);
    }
    com.cosmos.authz.v1beta1.GenesisState other = (com.cosmos.authz.v1beta1.GenesisState) obj;

    if (!getAuthorizationList()
        .equals(other.getAuthorizationList())) return false;
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
    if (getAuthorizationCount() > 0) {
      hash = (37 * hash) + AUTHORIZATION_FIELD_NUMBER;
      hash = (53 * hash) + getAuthorizationList().hashCode();
    }
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static com.cosmos.authz.v1beta1.GenesisState parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.cosmos.authz.v1beta1.GenesisState parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.cosmos.authz.v1beta1.GenesisState parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.cosmos.authz.v1beta1.GenesisState parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.cosmos.authz.v1beta1.GenesisState parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.cosmos.authz.v1beta1.GenesisState parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.cosmos.authz.v1beta1.GenesisState parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.cosmos.authz.v1beta1.GenesisState parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.cosmos.authz.v1beta1.GenesisState parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static com.cosmos.authz.v1beta1.GenesisState parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.cosmos.authz.v1beta1.GenesisState parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.cosmos.authz.v1beta1.GenesisState parseFrom(
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
  public static Builder newBuilder(com.cosmos.authz.v1beta1.GenesisState prototype) {
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
   * GenesisState defines the authz module's genesis state.
   * </pre>
   *
   * Protobuf type {@code cosmos.authz.v1beta1.GenesisState}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:cosmos.authz.v1beta1.GenesisState)
      com.cosmos.authz.v1beta1.GenesisStateOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return com.cosmos.authz.v1beta1.GenesisProto.internal_static_cosmos_authz_v1beta1_GenesisState_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return com.cosmos.authz.v1beta1.GenesisProto.internal_static_cosmos_authz_v1beta1_GenesisState_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              com.cosmos.authz.v1beta1.GenesisState.class, com.cosmos.authz.v1beta1.GenesisState.Builder.class);
    }

    // Construct using com.cosmos.authz.v1beta1.GenesisState.newBuilder()
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
      if (authorizationBuilder_ == null) {
        authorization_ = java.util.Collections.emptyList();
      } else {
        authorization_ = null;
        authorizationBuilder_.clear();
      }
      bitField0_ = (bitField0_ & ~0x00000001);
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return com.cosmos.authz.v1beta1.GenesisProto.internal_static_cosmos_authz_v1beta1_GenesisState_descriptor;
    }

    @java.lang.Override
    public com.cosmos.authz.v1beta1.GenesisState getDefaultInstanceForType() {
      return com.cosmos.authz.v1beta1.GenesisState.getDefaultInstance();
    }

    @java.lang.Override
    public com.cosmos.authz.v1beta1.GenesisState build() {
      com.cosmos.authz.v1beta1.GenesisState result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public com.cosmos.authz.v1beta1.GenesisState buildPartial() {
      com.cosmos.authz.v1beta1.GenesisState result = new com.cosmos.authz.v1beta1.GenesisState(this);
      buildPartialRepeatedFields(result);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartialRepeatedFields(com.cosmos.authz.v1beta1.GenesisState result) {
      if (authorizationBuilder_ == null) {
        if (((bitField0_ & 0x00000001) != 0)) {
          authorization_ = java.util.Collections.unmodifiableList(authorization_);
          bitField0_ = (bitField0_ & ~0x00000001);
        }
        result.authorization_ = authorization_;
      } else {
        result.authorization_ = authorizationBuilder_.build();
      }
    }

    private void buildPartial0(com.cosmos.authz.v1beta1.GenesisState result) {
      int from_bitField0_ = bitField0_;
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
      if (other instanceof com.cosmos.authz.v1beta1.GenesisState) {
        return mergeFrom((com.cosmos.authz.v1beta1.GenesisState)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(com.cosmos.authz.v1beta1.GenesisState other) {
      if (other == com.cosmos.authz.v1beta1.GenesisState.getDefaultInstance()) return this;
      if (authorizationBuilder_ == null) {
        if (!other.authorization_.isEmpty()) {
          if (authorization_.isEmpty()) {
            authorization_ = other.authorization_;
            bitField0_ = (bitField0_ & ~0x00000001);
          } else {
            ensureAuthorizationIsMutable();
            authorization_.addAll(other.authorization_);
          }
          onChanged();
        }
      } else {
        if (!other.authorization_.isEmpty()) {
          if (authorizationBuilder_.isEmpty()) {
            authorizationBuilder_.dispose();
            authorizationBuilder_ = null;
            authorization_ = other.authorization_;
            bitField0_ = (bitField0_ & ~0x00000001);
            authorizationBuilder_ = 
              com.google.protobuf.GeneratedMessageV3.alwaysUseFieldBuilders ?
                 getAuthorizationFieldBuilder() : null;
          } else {
            authorizationBuilder_.addAllMessages(other.authorization_);
          }
        }
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
              com.cosmos.authz.v1beta1.GrantAuthorization m =
                  input.readMessage(
                      com.cosmos.authz.v1beta1.GrantAuthorization.parser(),
                      extensionRegistry);
              if (authorizationBuilder_ == null) {
                ensureAuthorizationIsMutable();
                authorization_.add(m);
              } else {
                authorizationBuilder_.addMessage(m);
              }
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

    private java.util.List<com.cosmos.authz.v1beta1.GrantAuthorization> authorization_ =
      java.util.Collections.emptyList();
    private void ensureAuthorizationIsMutable() {
      if (!((bitField0_ & 0x00000001) != 0)) {
        authorization_ = new java.util.ArrayList<com.cosmos.authz.v1beta1.GrantAuthorization>(authorization_);
        bitField0_ |= 0x00000001;
       }
    }

    private com.google.protobuf.RepeatedFieldBuilderV3<
        com.cosmos.authz.v1beta1.GrantAuthorization, com.cosmos.authz.v1beta1.GrantAuthorization.Builder, com.cosmos.authz.v1beta1.GrantAuthorizationOrBuilder> authorizationBuilder_;

    /**
     * <code>repeated .cosmos.authz.v1beta1.GrantAuthorization authorization = 1 [json_name = "authorization", (.gogoproto.nullable) = false];</code>
     */
    public java.util.List<com.cosmos.authz.v1beta1.GrantAuthorization> getAuthorizationList() {
      if (authorizationBuilder_ == null) {
        return java.util.Collections.unmodifiableList(authorization_);
      } else {
        return authorizationBuilder_.getMessageList();
      }
    }
    /**
     * <code>repeated .cosmos.authz.v1beta1.GrantAuthorization authorization = 1 [json_name = "authorization", (.gogoproto.nullable) = false];</code>
     */
    public int getAuthorizationCount() {
      if (authorizationBuilder_ == null) {
        return authorization_.size();
      } else {
        return authorizationBuilder_.getCount();
      }
    }
    /**
     * <code>repeated .cosmos.authz.v1beta1.GrantAuthorization authorization = 1 [json_name = "authorization", (.gogoproto.nullable) = false];</code>
     */
    public com.cosmos.authz.v1beta1.GrantAuthorization getAuthorization(int index) {
      if (authorizationBuilder_ == null) {
        return authorization_.get(index);
      } else {
        return authorizationBuilder_.getMessage(index);
      }
    }
    /**
     * <code>repeated .cosmos.authz.v1beta1.GrantAuthorization authorization = 1 [json_name = "authorization", (.gogoproto.nullable) = false];</code>
     */
    public Builder setAuthorization(
        int index, com.cosmos.authz.v1beta1.GrantAuthorization value) {
      if (authorizationBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureAuthorizationIsMutable();
        authorization_.set(index, value);
        onChanged();
      } else {
        authorizationBuilder_.setMessage(index, value);
      }
      return this;
    }
    /**
     * <code>repeated .cosmos.authz.v1beta1.GrantAuthorization authorization = 1 [json_name = "authorization", (.gogoproto.nullable) = false];</code>
     */
    public Builder setAuthorization(
        int index, com.cosmos.authz.v1beta1.GrantAuthorization.Builder builderForValue) {
      if (authorizationBuilder_ == null) {
        ensureAuthorizationIsMutable();
        authorization_.set(index, builderForValue.build());
        onChanged();
      } else {
        authorizationBuilder_.setMessage(index, builderForValue.build());
      }
      return this;
    }
    /**
     * <code>repeated .cosmos.authz.v1beta1.GrantAuthorization authorization = 1 [json_name = "authorization", (.gogoproto.nullable) = false];</code>
     */
    public Builder addAuthorization(com.cosmos.authz.v1beta1.GrantAuthorization value) {
      if (authorizationBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureAuthorizationIsMutable();
        authorization_.add(value);
        onChanged();
      } else {
        authorizationBuilder_.addMessage(value);
      }
      return this;
    }
    /**
     * <code>repeated .cosmos.authz.v1beta1.GrantAuthorization authorization = 1 [json_name = "authorization", (.gogoproto.nullable) = false];</code>
     */
    public Builder addAuthorization(
        int index, com.cosmos.authz.v1beta1.GrantAuthorization value) {
      if (authorizationBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureAuthorizationIsMutable();
        authorization_.add(index, value);
        onChanged();
      } else {
        authorizationBuilder_.addMessage(index, value);
      }
      return this;
    }
    /**
     * <code>repeated .cosmos.authz.v1beta1.GrantAuthorization authorization = 1 [json_name = "authorization", (.gogoproto.nullable) = false];</code>
     */
    public Builder addAuthorization(
        com.cosmos.authz.v1beta1.GrantAuthorization.Builder builderForValue) {
      if (authorizationBuilder_ == null) {
        ensureAuthorizationIsMutable();
        authorization_.add(builderForValue.build());
        onChanged();
      } else {
        authorizationBuilder_.addMessage(builderForValue.build());
      }
      return this;
    }
    /**
     * <code>repeated .cosmos.authz.v1beta1.GrantAuthorization authorization = 1 [json_name = "authorization", (.gogoproto.nullable) = false];</code>
     */
    public Builder addAuthorization(
        int index, com.cosmos.authz.v1beta1.GrantAuthorization.Builder builderForValue) {
      if (authorizationBuilder_ == null) {
        ensureAuthorizationIsMutable();
        authorization_.add(index, builderForValue.build());
        onChanged();
      } else {
        authorizationBuilder_.addMessage(index, builderForValue.build());
      }
      return this;
    }
    /**
     * <code>repeated .cosmos.authz.v1beta1.GrantAuthorization authorization = 1 [json_name = "authorization", (.gogoproto.nullable) = false];</code>
     */
    public Builder addAllAuthorization(
        java.lang.Iterable<? extends com.cosmos.authz.v1beta1.GrantAuthorization> values) {
      if (authorizationBuilder_ == null) {
        ensureAuthorizationIsMutable();
        com.google.protobuf.AbstractMessageLite.Builder.addAll(
            values, authorization_);
        onChanged();
      } else {
        authorizationBuilder_.addAllMessages(values);
      }
      return this;
    }
    /**
     * <code>repeated .cosmos.authz.v1beta1.GrantAuthorization authorization = 1 [json_name = "authorization", (.gogoproto.nullable) = false];</code>
     */
    public Builder clearAuthorization() {
      if (authorizationBuilder_ == null) {
        authorization_ = java.util.Collections.emptyList();
        bitField0_ = (bitField0_ & ~0x00000001);
        onChanged();
      } else {
        authorizationBuilder_.clear();
      }
      return this;
    }
    /**
     * <code>repeated .cosmos.authz.v1beta1.GrantAuthorization authorization = 1 [json_name = "authorization", (.gogoproto.nullable) = false];</code>
     */
    public Builder removeAuthorization(int index) {
      if (authorizationBuilder_ == null) {
        ensureAuthorizationIsMutable();
        authorization_.remove(index);
        onChanged();
      } else {
        authorizationBuilder_.remove(index);
      }
      return this;
    }
    /**
     * <code>repeated .cosmos.authz.v1beta1.GrantAuthorization authorization = 1 [json_name = "authorization", (.gogoproto.nullable) = false];</code>
     */
    public com.cosmos.authz.v1beta1.GrantAuthorization.Builder getAuthorizationBuilder(
        int index) {
      return getAuthorizationFieldBuilder().getBuilder(index);
    }
    /**
     * <code>repeated .cosmos.authz.v1beta1.GrantAuthorization authorization = 1 [json_name = "authorization", (.gogoproto.nullable) = false];</code>
     */
    public com.cosmos.authz.v1beta1.GrantAuthorizationOrBuilder getAuthorizationOrBuilder(
        int index) {
      if (authorizationBuilder_ == null) {
        return authorization_.get(index);  } else {
        return authorizationBuilder_.getMessageOrBuilder(index);
      }
    }
    /**
     * <code>repeated .cosmos.authz.v1beta1.GrantAuthorization authorization = 1 [json_name = "authorization", (.gogoproto.nullable) = false];</code>
     */
    public java.util.List<? extends com.cosmos.authz.v1beta1.GrantAuthorizationOrBuilder> 
         getAuthorizationOrBuilderList() {
      if (authorizationBuilder_ != null) {
        return authorizationBuilder_.getMessageOrBuilderList();
      } else {
        return java.util.Collections.unmodifiableList(authorization_);
      }
    }
    /**
     * <code>repeated .cosmos.authz.v1beta1.GrantAuthorization authorization = 1 [json_name = "authorization", (.gogoproto.nullable) = false];</code>
     */
    public com.cosmos.authz.v1beta1.GrantAuthorization.Builder addAuthorizationBuilder() {
      return getAuthorizationFieldBuilder().addBuilder(
          com.cosmos.authz.v1beta1.GrantAuthorization.getDefaultInstance());
    }
    /**
     * <code>repeated .cosmos.authz.v1beta1.GrantAuthorization authorization = 1 [json_name = "authorization", (.gogoproto.nullable) = false];</code>
     */
    public com.cosmos.authz.v1beta1.GrantAuthorization.Builder addAuthorizationBuilder(
        int index) {
      return getAuthorizationFieldBuilder().addBuilder(
          index, com.cosmos.authz.v1beta1.GrantAuthorization.getDefaultInstance());
    }
    /**
     * <code>repeated .cosmos.authz.v1beta1.GrantAuthorization authorization = 1 [json_name = "authorization", (.gogoproto.nullable) = false];</code>
     */
    public java.util.List<com.cosmos.authz.v1beta1.GrantAuthorization.Builder> 
         getAuthorizationBuilderList() {
      return getAuthorizationFieldBuilder().getBuilderList();
    }
    private com.google.protobuf.RepeatedFieldBuilderV3<
        com.cosmos.authz.v1beta1.GrantAuthorization, com.cosmos.authz.v1beta1.GrantAuthorization.Builder, com.cosmos.authz.v1beta1.GrantAuthorizationOrBuilder> 
        getAuthorizationFieldBuilder() {
      if (authorizationBuilder_ == null) {
        authorizationBuilder_ = new com.google.protobuf.RepeatedFieldBuilderV3<
            com.cosmos.authz.v1beta1.GrantAuthorization, com.cosmos.authz.v1beta1.GrantAuthorization.Builder, com.cosmos.authz.v1beta1.GrantAuthorizationOrBuilder>(
                authorization_,
                ((bitField0_ & 0x00000001) != 0),
                getParentForChildren(),
                isClean());
        authorization_ = null;
      }
      return authorizationBuilder_;
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


    // @@protoc_insertion_point(builder_scope:cosmos.authz.v1beta1.GenesisState)
  }

  // @@protoc_insertion_point(class_scope:cosmos.authz.v1beta1.GenesisState)
  private static final com.cosmos.authz.v1beta1.GenesisState DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new com.cosmos.authz.v1beta1.GenesisState();
  }

  public static com.cosmos.authz.v1beta1.GenesisState getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<GenesisState>
      PARSER = new com.google.protobuf.AbstractParser<GenesisState>() {
    @java.lang.Override
    public GenesisState parsePartialFrom(
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

  public static com.google.protobuf.Parser<GenesisState> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<GenesisState> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public com.cosmos.authz.v1beta1.GenesisState getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

