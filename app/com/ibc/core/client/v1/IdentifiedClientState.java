// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: ibc/core/client/v1/client.proto

package com.ibc.core.client.v1;

/**
 * <pre>
 * IdentifiedClientState defines a client state with an additional client
 * identifier field.
 * </pre>
 *
 * Protobuf type {@code ibc.core.client.v1.IdentifiedClientState}
 */
public final class IdentifiedClientState extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:ibc.core.client.v1.IdentifiedClientState)
    IdentifiedClientStateOrBuilder {
private static final long serialVersionUID = 0L;
  // Use IdentifiedClientState.newBuilder() to construct.
  private IdentifiedClientState(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private IdentifiedClientState() {
    clientId_ = "";
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new IdentifiedClientState();
  }

  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return com.ibc.core.client.v1.ClientProto.internal_static_ibc_core_client_v1_IdentifiedClientState_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return com.ibc.core.client.v1.ClientProto.internal_static_ibc_core_client_v1_IdentifiedClientState_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            com.ibc.core.client.v1.IdentifiedClientState.class, com.ibc.core.client.v1.IdentifiedClientState.Builder.class);
  }

  public static final int CLIENT_ID_FIELD_NUMBER = 1;
  @SuppressWarnings("serial")
  private volatile java.lang.Object clientId_ = "";
  /**
   * <pre>
   * client identifier
   * </pre>
   *
   * <code>string client_id = 1 [json_name = "clientId", (.gogoproto.moretags) = "yaml:&#92;"client_id&#92;""];</code>
   * @return The clientId.
   */
  @java.lang.Override
  public java.lang.String getClientId() {
    java.lang.Object ref = clientId_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      clientId_ = s;
      return s;
    }
  }
  /**
   * <pre>
   * client identifier
   * </pre>
   *
   * <code>string client_id = 1 [json_name = "clientId", (.gogoproto.moretags) = "yaml:&#92;"client_id&#92;""];</code>
   * @return The bytes for clientId.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getClientIdBytes() {
    java.lang.Object ref = clientId_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      clientId_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int CLIENT_STATE_FIELD_NUMBER = 2;
  private com.google.protobuf.Any clientState_;
  /**
   * <pre>
   * client state
   * </pre>
   *
   * <code>.google.protobuf.Any client_state = 2 [json_name = "clientState", (.gogoproto.moretags) = "yaml:&#92;"client_state&#92;""];</code>
   * @return Whether the clientState field is set.
   */
  @java.lang.Override
  public boolean hasClientState() {
    return clientState_ != null;
  }
  /**
   * <pre>
   * client state
   * </pre>
   *
   * <code>.google.protobuf.Any client_state = 2 [json_name = "clientState", (.gogoproto.moretags) = "yaml:&#92;"client_state&#92;""];</code>
   * @return The clientState.
   */
  @java.lang.Override
  public com.google.protobuf.Any getClientState() {
    return clientState_ == null ? com.google.protobuf.Any.getDefaultInstance() : clientState_;
  }
  /**
   * <pre>
   * client state
   * </pre>
   *
   * <code>.google.protobuf.Any client_state = 2 [json_name = "clientState", (.gogoproto.moretags) = "yaml:&#92;"client_state&#92;""];</code>
   */
  @java.lang.Override
  public com.google.protobuf.AnyOrBuilder getClientStateOrBuilder() {
    return clientState_ == null ? com.google.protobuf.Any.getDefaultInstance() : clientState_;
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
    if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(clientId_)) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 1, clientId_);
    }
    if (clientState_ != null) {
      output.writeMessage(2, getClientState());
    }
    getUnknownFields().writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(clientId_)) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(1, clientId_);
    }
    if (clientState_ != null) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(2, getClientState());
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
    if (!(obj instanceof com.ibc.core.client.v1.IdentifiedClientState)) {
      return super.equals(obj);
    }
    com.ibc.core.client.v1.IdentifiedClientState other = (com.ibc.core.client.v1.IdentifiedClientState) obj;

    if (!getClientId()
        .equals(other.getClientId())) return false;
    if (hasClientState() != other.hasClientState()) return false;
    if (hasClientState()) {
      if (!getClientState()
          .equals(other.getClientState())) return false;
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
    hash = (37 * hash) + CLIENT_ID_FIELD_NUMBER;
    hash = (53 * hash) + getClientId().hashCode();
    if (hasClientState()) {
      hash = (37 * hash) + CLIENT_STATE_FIELD_NUMBER;
      hash = (53 * hash) + getClientState().hashCode();
    }
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static com.ibc.core.client.v1.IdentifiedClientState parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.ibc.core.client.v1.IdentifiedClientState parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.ibc.core.client.v1.IdentifiedClientState parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.ibc.core.client.v1.IdentifiedClientState parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.ibc.core.client.v1.IdentifiedClientState parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.ibc.core.client.v1.IdentifiedClientState parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.ibc.core.client.v1.IdentifiedClientState parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.ibc.core.client.v1.IdentifiedClientState parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.ibc.core.client.v1.IdentifiedClientState parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static com.ibc.core.client.v1.IdentifiedClientState parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.ibc.core.client.v1.IdentifiedClientState parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.ibc.core.client.v1.IdentifiedClientState parseFrom(
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
  public static Builder newBuilder(com.ibc.core.client.v1.IdentifiedClientState prototype) {
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
   * IdentifiedClientState defines a client state with an additional client
   * identifier field.
   * </pre>
   *
   * Protobuf type {@code ibc.core.client.v1.IdentifiedClientState}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:ibc.core.client.v1.IdentifiedClientState)
      com.ibc.core.client.v1.IdentifiedClientStateOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return com.ibc.core.client.v1.ClientProto.internal_static_ibc_core_client_v1_IdentifiedClientState_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return com.ibc.core.client.v1.ClientProto.internal_static_ibc_core_client_v1_IdentifiedClientState_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              com.ibc.core.client.v1.IdentifiedClientState.class, com.ibc.core.client.v1.IdentifiedClientState.Builder.class);
    }

    // Construct using com.ibc.core.client.v1.IdentifiedClientState.newBuilder()
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
      clientId_ = "";
      clientState_ = null;
      if (clientStateBuilder_ != null) {
        clientStateBuilder_.dispose();
        clientStateBuilder_ = null;
      }
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return com.ibc.core.client.v1.ClientProto.internal_static_ibc_core_client_v1_IdentifiedClientState_descriptor;
    }

    @java.lang.Override
    public com.ibc.core.client.v1.IdentifiedClientState getDefaultInstanceForType() {
      return com.ibc.core.client.v1.IdentifiedClientState.getDefaultInstance();
    }

    @java.lang.Override
    public com.ibc.core.client.v1.IdentifiedClientState build() {
      com.ibc.core.client.v1.IdentifiedClientState result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public com.ibc.core.client.v1.IdentifiedClientState buildPartial() {
      com.ibc.core.client.v1.IdentifiedClientState result = new com.ibc.core.client.v1.IdentifiedClientState(this);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartial0(com.ibc.core.client.v1.IdentifiedClientState result) {
      int from_bitField0_ = bitField0_;
      if (((from_bitField0_ & 0x00000001) != 0)) {
        result.clientId_ = clientId_;
      }
      if (((from_bitField0_ & 0x00000002) != 0)) {
        result.clientState_ = clientStateBuilder_ == null
            ? clientState_
            : clientStateBuilder_.build();
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
      if (other instanceof com.ibc.core.client.v1.IdentifiedClientState) {
        return mergeFrom((com.ibc.core.client.v1.IdentifiedClientState)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(com.ibc.core.client.v1.IdentifiedClientState other) {
      if (other == com.ibc.core.client.v1.IdentifiedClientState.getDefaultInstance()) return this;
      if (!other.getClientId().isEmpty()) {
        clientId_ = other.clientId_;
        bitField0_ |= 0x00000001;
        onChanged();
      }
      if (other.hasClientState()) {
        mergeClientState(other.getClientState());
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
              clientId_ = input.readStringRequireUtf8();
              bitField0_ |= 0x00000001;
              break;
            } // case 10
            case 18: {
              input.readMessage(
                  getClientStateFieldBuilder().getBuilder(),
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

    private java.lang.Object clientId_ = "";
    /**
     * <pre>
     * client identifier
     * </pre>
     *
     * <code>string client_id = 1 [json_name = "clientId", (.gogoproto.moretags) = "yaml:&#92;"client_id&#92;""];</code>
     * @return The clientId.
     */
    public java.lang.String getClientId() {
      java.lang.Object ref = clientId_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        clientId_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <pre>
     * client identifier
     * </pre>
     *
     * <code>string client_id = 1 [json_name = "clientId", (.gogoproto.moretags) = "yaml:&#92;"client_id&#92;""];</code>
     * @return The bytes for clientId.
     */
    public com.google.protobuf.ByteString
        getClientIdBytes() {
      java.lang.Object ref = clientId_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        clientId_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <pre>
     * client identifier
     * </pre>
     *
     * <code>string client_id = 1 [json_name = "clientId", (.gogoproto.moretags) = "yaml:&#92;"client_id&#92;""];</code>
     * @param value The clientId to set.
     * @return This builder for chaining.
     */
    public Builder setClientId(
        java.lang.String value) {
      if (value == null) { throw new NullPointerException(); }
      clientId_ = value;
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * client identifier
     * </pre>
     *
     * <code>string client_id = 1 [json_name = "clientId", (.gogoproto.moretags) = "yaml:&#92;"client_id&#92;""];</code>
     * @return This builder for chaining.
     */
    public Builder clearClientId() {
      clientId_ = getDefaultInstance().getClientId();
      bitField0_ = (bitField0_ & ~0x00000001);
      onChanged();
      return this;
    }
    /**
     * <pre>
     * client identifier
     * </pre>
     *
     * <code>string client_id = 1 [json_name = "clientId", (.gogoproto.moretags) = "yaml:&#92;"client_id&#92;""];</code>
     * @param value The bytes for clientId to set.
     * @return This builder for chaining.
     */
    public Builder setClientIdBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) { throw new NullPointerException(); }
      checkByteStringIsUtf8(value);
      clientId_ = value;
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }

    private com.google.protobuf.Any clientState_;
    private com.google.protobuf.SingleFieldBuilderV3<
        com.google.protobuf.Any, com.google.protobuf.Any.Builder, com.google.protobuf.AnyOrBuilder> clientStateBuilder_;
    /**
     * <pre>
     * client state
     * </pre>
     *
     * <code>.google.protobuf.Any client_state = 2 [json_name = "clientState", (.gogoproto.moretags) = "yaml:&#92;"client_state&#92;""];</code>
     * @return Whether the clientState field is set.
     */
    public boolean hasClientState() {
      return ((bitField0_ & 0x00000002) != 0);
    }
    /**
     * <pre>
     * client state
     * </pre>
     *
     * <code>.google.protobuf.Any client_state = 2 [json_name = "clientState", (.gogoproto.moretags) = "yaml:&#92;"client_state&#92;""];</code>
     * @return The clientState.
     */
    public com.google.protobuf.Any getClientState() {
      if (clientStateBuilder_ == null) {
        return clientState_ == null ? com.google.protobuf.Any.getDefaultInstance() : clientState_;
      } else {
        return clientStateBuilder_.getMessage();
      }
    }
    /**
     * <pre>
     * client state
     * </pre>
     *
     * <code>.google.protobuf.Any client_state = 2 [json_name = "clientState", (.gogoproto.moretags) = "yaml:&#92;"client_state&#92;""];</code>
     */
    public Builder setClientState(com.google.protobuf.Any value) {
      if (clientStateBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        clientState_ = value;
      } else {
        clientStateBuilder_.setMessage(value);
      }
      bitField0_ |= 0x00000002;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * client state
     * </pre>
     *
     * <code>.google.protobuf.Any client_state = 2 [json_name = "clientState", (.gogoproto.moretags) = "yaml:&#92;"client_state&#92;""];</code>
     */
    public Builder setClientState(
        com.google.protobuf.Any.Builder builderForValue) {
      if (clientStateBuilder_ == null) {
        clientState_ = builderForValue.build();
      } else {
        clientStateBuilder_.setMessage(builderForValue.build());
      }
      bitField0_ |= 0x00000002;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * client state
     * </pre>
     *
     * <code>.google.protobuf.Any client_state = 2 [json_name = "clientState", (.gogoproto.moretags) = "yaml:&#92;"client_state&#92;""];</code>
     */
    public Builder mergeClientState(com.google.protobuf.Any value) {
      if (clientStateBuilder_ == null) {
        if (((bitField0_ & 0x00000002) != 0) &&
          clientState_ != null &&
          clientState_ != com.google.protobuf.Any.getDefaultInstance()) {
          getClientStateBuilder().mergeFrom(value);
        } else {
          clientState_ = value;
        }
      } else {
        clientStateBuilder_.mergeFrom(value);
      }
      bitField0_ |= 0x00000002;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * client state
     * </pre>
     *
     * <code>.google.protobuf.Any client_state = 2 [json_name = "clientState", (.gogoproto.moretags) = "yaml:&#92;"client_state&#92;""];</code>
     */
    public Builder clearClientState() {
      bitField0_ = (bitField0_ & ~0x00000002);
      clientState_ = null;
      if (clientStateBuilder_ != null) {
        clientStateBuilder_.dispose();
        clientStateBuilder_ = null;
      }
      onChanged();
      return this;
    }
    /**
     * <pre>
     * client state
     * </pre>
     *
     * <code>.google.protobuf.Any client_state = 2 [json_name = "clientState", (.gogoproto.moretags) = "yaml:&#92;"client_state&#92;""];</code>
     */
    public com.google.protobuf.Any.Builder getClientStateBuilder() {
      bitField0_ |= 0x00000002;
      onChanged();
      return getClientStateFieldBuilder().getBuilder();
    }
    /**
     * <pre>
     * client state
     * </pre>
     *
     * <code>.google.protobuf.Any client_state = 2 [json_name = "clientState", (.gogoproto.moretags) = "yaml:&#92;"client_state&#92;""];</code>
     */
    public com.google.protobuf.AnyOrBuilder getClientStateOrBuilder() {
      if (clientStateBuilder_ != null) {
        return clientStateBuilder_.getMessageOrBuilder();
      } else {
        return clientState_ == null ?
            com.google.protobuf.Any.getDefaultInstance() : clientState_;
      }
    }
    /**
     * <pre>
     * client state
     * </pre>
     *
     * <code>.google.protobuf.Any client_state = 2 [json_name = "clientState", (.gogoproto.moretags) = "yaml:&#92;"client_state&#92;""];</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
        com.google.protobuf.Any, com.google.protobuf.Any.Builder, com.google.protobuf.AnyOrBuilder> 
        getClientStateFieldBuilder() {
      if (clientStateBuilder_ == null) {
        clientStateBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
            com.google.protobuf.Any, com.google.protobuf.Any.Builder, com.google.protobuf.AnyOrBuilder>(
                getClientState(),
                getParentForChildren(),
                isClean());
        clientState_ = null;
      }
      return clientStateBuilder_;
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


    // @@protoc_insertion_point(builder_scope:ibc.core.client.v1.IdentifiedClientState)
  }

  // @@protoc_insertion_point(class_scope:ibc.core.client.v1.IdentifiedClientState)
  private static final com.ibc.core.client.v1.IdentifiedClientState DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new com.ibc.core.client.v1.IdentifiedClientState();
  }

  public static com.ibc.core.client.v1.IdentifiedClientState getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<IdentifiedClientState>
      PARSER = new com.google.protobuf.AbstractParser<IdentifiedClientState>() {
    @java.lang.Override
    public IdentifiedClientState parsePartialFrom(
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

  public static com.google.protobuf.Parser<IdentifiedClientState> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<IdentifiedClientState> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public com.ibc.core.client.v1.IdentifiedClientState getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}
