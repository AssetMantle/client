// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: cosmos/auth/v1beta1/query.proto

package com.cosmos.auth.v1beta1;

/**
 * <pre>
 * QueryAccountResponse is the response type for the Query/Account RPC method.
 * </pre>
 *
 * Protobuf type {@code cosmos.auth.v1beta1.QueryAccountResponse}
 */
public final class QueryAccountResponse extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:cosmos.auth.v1beta1.QueryAccountResponse)
    QueryAccountResponseOrBuilder {
private static final long serialVersionUID = 0L;
  // Use QueryAccountResponse.newBuilder() to construct.
  private QueryAccountResponse(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private QueryAccountResponse() {
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new QueryAccountResponse();
  }

  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return com.cosmos.auth.v1beta1.QueryProto.internal_static_cosmos_auth_v1beta1_QueryAccountResponse_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return com.cosmos.auth.v1beta1.QueryProto.internal_static_cosmos_auth_v1beta1_QueryAccountResponse_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            com.cosmos.auth.v1beta1.QueryAccountResponse.class, com.cosmos.auth.v1beta1.QueryAccountResponse.Builder.class);
  }

  public static final int ACCOUNT_FIELD_NUMBER = 1;
  private com.google.protobuf.Any account_;
  /**
   * <pre>
   * account defines the account of the corresponding address.
   * </pre>
   *
   * <code>.google.protobuf.Any account = 1 [json_name = "account", (.cosmos_proto.accepts_interface) = "AccountI"];</code>
   * @return Whether the account field is set.
   */
  @java.lang.Override
  public boolean hasAccount() {
    return account_ != null;
  }
  /**
   * <pre>
   * account defines the account of the corresponding address.
   * </pre>
   *
   * <code>.google.protobuf.Any account = 1 [json_name = "account", (.cosmos_proto.accepts_interface) = "AccountI"];</code>
   * @return The account.
   */
  @java.lang.Override
  public com.google.protobuf.Any getAccount() {
    return account_ == null ? com.google.protobuf.Any.getDefaultInstance() : account_;
  }
  /**
   * <pre>
   * account defines the account of the corresponding address.
   * </pre>
   *
   * <code>.google.protobuf.Any account = 1 [json_name = "account", (.cosmos_proto.accepts_interface) = "AccountI"];</code>
   */
  @java.lang.Override
  public com.google.protobuf.AnyOrBuilder getAccountOrBuilder() {
    return account_ == null ? com.google.protobuf.Any.getDefaultInstance() : account_;
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
    if (account_ != null) {
      output.writeMessage(1, getAccount());
    }
    getUnknownFields().writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (account_ != null) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(1, getAccount());
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
    if (!(obj instanceof com.cosmos.auth.v1beta1.QueryAccountResponse)) {
      return super.equals(obj);
    }
    com.cosmos.auth.v1beta1.QueryAccountResponse other = (com.cosmos.auth.v1beta1.QueryAccountResponse) obj;

    if (hasAccount() != other.hasAccount()) return false;
    if (hasAccount()) {
      if (!getAccount()
          .equals(other.getAccount())) return false;
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
    if (hasAccount()) {
      hash = (37 * hash) + ACCOUNT_FIELD_NUMBER;
      hash = (53 * hash) + getAccount().hashCode();
    }
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static com.cosmos.auth.v1beta1.QueryAccountResponse parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.cosmos.auth.v1beta1.QueryAccountResponse parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.cosmos.auth.v1beta1.QueryAccountResponse parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.cosmos.auth.v1beta1.QueryAccountResponse parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.cosmos.auth.v1beta1.QueryAccountResponse parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.cosmos.auth.v1beta1.QueryAccountResponse parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.cosmos.auth.v1beta1.QueryAccountResponse parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.cosmos.auth.v1beta1.QueryAccountResponse parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.cosmos.auth.v1beta1.QueryAccountResponse parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static com.cosmos.auth.v1beta1.QueryAccountResponse parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.cosmos.auth.v1beta1.QueryAccountResponse parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.cosmos.auth.v1beta1.QueryAccountResponse parseFrom(
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
  public static Builder newBuilder(com.cosmos.auth.v1beta1.QueryAccountResponse prototype) {
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
   * QueryAccountResponse is the response type for the Query/Account RPC method.
   * </pre>
   *
   * Protobuf type {@code cosmos.auth.v1beta1.QueryAccountResponse}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:cosmos.auth.v1beta1.QueryAccountResponse)
      com.cosmos.auth.v1beta1.QueryAccountResponseOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return com.cosmos.auth.v1beta1.QueryProto.internal_static_cosmos_auth_v1beta1_QueryAccountResponse_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return com.cosmos.auth.v1beta1.QueryProto.internal_static_cosmos_auth_v1beta1_QueryAccountResponse_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              com.cosmos.auth.v1beta1.QueryAccountResponse.class, com.cosmos.auth.v1beta1.QueryAccountResponse.Builder.class);
    }

    // Construct using com.cosmos.auth.v1beta1.QueryAccountResponse.newBuilder()
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
      account_ = null;
      if (accountBuilder_ != null) {
        accountBuilder_.dispose();
        accountBuilder_ = null;
      }
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return com.cosmos.auth.v1beta1.QueryProto.internal_static_cosmos_auth_v1beta1_QueryAccountResponse_descriptor;
    }

    @java.lang.Override
    public com.cosmos.auth.v1beta1.QueryAccountResponse getDefaultInstanceForType() {
      return com.cosmos.auth.v1beta1.QueryAccountResponse.getDefaultInstance();
    }

    @java.lang.Override
    public com.cosmos.auth.v1beta1.QueryAccountResponse build() {
      com.cosmos.auth.v1beta1.QueryAccountResponse result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public com.cosmos.auth.v1beta1.QueryAccountResponse buildPartial() {
      com.cosmos.auth.v1beta1.QueryAccountResponse result = new com.cosmos.auth.v1beta1.QueryAccountResponse(this);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartial0(com.cosmos.auth.v1beta1.QueryAccountResponse result) {
      int from_bitField0_ = bitField0_;
      if (((from_bitField0_ & 0x00000001) != 0)) {
        result.account_ = accountBuilder_ == null
            ? account_
            : accountBuilder_.build();
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
      if (other instanceof com.cosmos.auth.v1beta1.QueryAccountResponse) {
        return mergeFrom((com.cosmos.auth.v1beta1.QueryAccountResponse)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(com.cosmos.auth.v1beta1.QueryAccountResponse other) {
      if (other == com.cosmos.auth.v1beta1.QueryAccountResponse.getDefaultInstance()) return this;
      if (other.hasAccount()) {
        mergeAccount(other.getAccount());
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
                  getAccountFieldBuilder().getBuilder(),
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

    private com.google.protobuf.Any account_;
    private com.google.protobuf.SingleFieldBuilderV3<
        com.google.protobuf.Any, com.google.protobuf.Any.Builder, com.google.protobuf.AnyOrBuilder> accountBuilder_;
    /**
     * <pre>
     * account defines the account of the corresponding address.
     * </pre>
     *
     * <code>.google.protobuf.Any account = 1 [json_name = "account", (.cosmos_proto.accepts_interface) = "AccountI"];</code>
     * @return Whether the account field is set.
     */
    public boolean hasAccount() {
      return ((bitField0_ & 0x00000001) != 0);
    }
    /**
     * <pre>
     * account defines the account of the corresponding address.
     * </pre>
     *
     * <code>.google.protobuf.Any account = 1 [json_name = "account", (.cosmos_proto.accepts_interface) = "AccountI"];</code>
     * @return The account.
     */
    public com.google.protobuf.Any getAccount() {
      if (accountBuilder_ == null) {
        return account_ == null ? com.google.protobuf.Any.getDefaultInstance() : account_;
      } else {
        return accountBuilder_.getMessage();
      }
    }
    /**
     * <pre>
     * account defines the account of the corresponding address.
     * </pre>
     *
     * <code>.google.protobuf.Any account = 1 [json_name = "account", (.cosmos_proto.accepts_interface) = "AccountI"];</code>
     */
    public Builder setAccount(com.google.protobuf.Any value) {
      if (accountBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        account_ = value;
      } else {
        accountBuilder_.setMessage(value);
      }
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * account defines the account of the corresponding address.
     * </pre>
     *
     * <code>.google.protobuf.Any account = 1 [json_name = "account", (.cosmos_proto.accepts_interface) = "AccountI"];</code>
     */
    public Builder setAccount(
        com.google.protobuf.Any.Builder builderForValue) {
      if (accountBuilder_ == null) {
        account_ = builderForValue.build();
      } else {
        accountBuilder_.setMessage(builderForValue.build());
      }
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * account defines the account of the corresponding address.
     * </pre>
     *
     * <code>.google.protobuf.Any account = 1 [json_name = "account", (.cosmos_proto.accepts_interface) = "AccountI"];</code>
     */
    public Builder mergeAccount(com.google.protobuf.Any value) {
      if (accountBuilder_ == null) {
        if (((bitField0_ & 0x00000001) != 0) &&
          account_ != null &&
          account_ != com.google.protobuf.Any.getDefaultInstance()) {
          getAccountBuilder().mergeFrom(value);
        } else {
          account_ = value;
        }
      } else {
        accountBuilder_.mergeFrom(value);
      }
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * account defines the account of the corresponding address.
     * </pre>
     *
     * <code>.google.protobuf.Any account = 1 [json_name = "account", (.cosmos_proto.accepts_interface) = "AccountI"];</code>
     */
    public Builder clearAccount() {
      bitField0_ = (bitField0_ & ~0x00000001);
      account_ = null;
      if (accountBuilder_ != null) {
        accountBuilder_.dispose();
        accountBuilder_ = null;
      }
      onChanged();
      return this;
    }
    /**
     * <pre>
     * account defines the account of the corresponding address.
     * </pre>
     *
     * <code>.google.protobuf.Any account = 1 [json_name = "account", (.cosmos_proto.accepts_interface) = "AccountI"];</code>
     */
    public com.google.protobuf.Any.Builder getAccountBuilder() {
      bitField0_ |= 0x00000001;
      onChanged();
      return getAccountFieldBuilder().getBuilder();
    }
    /**
     * <pre>
     * account defines the account of the corresponding address.
     * </pre>
     *
     * <code>.google.protobuf.Any account = 1 [json_name = "account", (.cosmos_proto.accepts_interface) = "AccountI"];</code>
     */
    public com.google.protobuf.AnyOrBuilder getAccountOrBuilder() {
      if (accountBuilder_ != null) {
        return accountBuilder_.getMessageOrBuilder();
      } else {
        return account_ == null ?
            com.google.protobuf.Any.getDefaultInstance() : account_;
      }
    }
    /**
     * <pre>
     * account defines the account of the corresponding address.
     * </pre>
     *
     * <code>.google.protobuf.Any account = 1 [json_name = "account", (.cosmos_proto.accepts_interface) = "AccountI"];</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
        com.google.protobuf.Any, com.google.protobuf.Any.Builder, com.google.protobuf.AnyOrBuilder> 
        getAccountFieldBuilder() {
      if (accountBuilder_ == null) {
        accountBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
            com.google.protobuf.Any, com.google.protobuf.Any.Builder, com.google.protobuf.AnyOrBuilder>(
                getAccount(),
                getParentForChildren(),
                isClean());
        account_ = null;
      }
      return accountBuilder_;
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


    // @@protoc_insertion_point(builder_scope:cosmos.auth.v1beta1.QueryAccountResponse)
  }

  // @@protoc_insertion_point(class_scope:cosmos.auth.v1beta1.QueryAccountResponse)
  private static final com.cosmos.auth.v1beta1.QueryAccountResponse DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new com.cosmos.auth.v1beta1.QueryAccountResponse();
  }

  public static com.cosmos.auth.v1beta1.QueryAccountResponse getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<QueryAccountResponse>
      PARSER = new com.google.protobuf.AbstractParser<QueryAccountResponse>() {
    @java.lang.Override
    public QueryAccountResponse parsePartialFrom(
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

  public static com.google.protobuf.Parser<QueryAccountResponse> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<QueryAccountResponse> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public com.cosmos.auth.v1beta1.QueryAccountResponse getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

