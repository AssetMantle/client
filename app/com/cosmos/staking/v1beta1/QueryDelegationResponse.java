// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: cosmos/staking/v1beta1/query.proto

package com.cosmos.staking.v1beta1;

/**
 * <pre>
 * QueryDelegationResponse is response type for the Query/Delegation RPC method.
 * </pre>
 *
 * Protobuf type {@code cosmos.staking.v1beta1.QueryDelegationResponse}
 */
public final class QueryDelegationResponse extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:cosmos.staking.v1beta1.QueryDelegationResponse)
    QueryDelegationResponseOrBuilder {
private static final long serialVersionUID = 0L;
  // Use QueryDelegationResponse.newBuilder() to construct.
  private QueryDelegationResponse(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private QueryDelegationResponse() {
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new QueryDelegationResponse();
  }

  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return com.cosmos.staking.v1beta1.QueryProto.internal_static_cosmos_staking_v1beta1_QueryDelegationResponse_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return com.cosmos.staking.v1beta1.QueryProto.internal_static_cosmos_staking_v1beta1_QueryDelegationResponse_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            com.cosmos.staking.v1beta1.QueryDelegationResponse.class, com.cosmos.staking.v1beta1.QueryDelegationResponse.Builder.class);
  }

  public static final int DELEGATION_RESPONSE_FIELD_NUMBER = 1;
  private com.cosmos.staking.v1beta1.DelegationResponse delegationResponse_;
  /**
   * <pre>
   * delegation_responses defines the delegation info of a delegation.
   * </pre>
   *
   * <code>.cosmos.staking.v1beta1.DelegationResponse delegation_response = 1 [json_name = "delegationResponse"];</code>
   * @return Whether the delegationResponse field is set.
   */
  @java.lang.Override
  public boolean hasDelegationResponse() {
    return delegationResponse_ != null;
  }
  /**
   * <pre>
   * delegation_responses defines the delegation info of a delegation.
   * </pre>
   *
   * <code>.cosmos.staking.v1beta1.DelegationResponse delegation_response = 1 [json_name = "delegationResponse"];</code>
   * @return The delegationResponse.
   */
  @java.lang.Override
  public com.cosmos.staking.v1beta1.DelegationResponse getDelegationResponse() {
    return delegationResponse_ == null ? com.cosmos.staking.v1beta1.DelegationResponse.getDefaultInstance() : delegationResponse_;
  }
  /**
   * <pre>
   * delegation_responses defines the delegation info of a delegation.
   * </pre>
   *
   * <code>.cosmos.staking.v1beta1.DelegationResponse delegation_response = 1 [json_name = "delegationResponse"];</code>
   */
  @java.lang.Override
  public com.cosmos.staking.v1beta1.DelegationResponseOrBuilder getDelegationResponseOrBuilder() {
    return delegationResponse_ == null ? com.cosmos.staking.v1beta1.DelegationResponse.getDefaultInstance() : delegationResponse_;
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
    if (delegationResponse_ != null) {
      output.writeMessage(1, getDelegationResponse());
    }
    getUnknownFields().writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (delegationResponse_ != null) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(1, getDelegationResponse());
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
    if (!(obj instanceof com.cosmos.staking.v1beta1.QueryDelegationResponse)) {
      return super.equals(obj);
    }
    com.cosmos.staking.v1beta1.QueryDelegationResponse other = (com.cosmos.staking.v1beta1.QueryDelegationResponse) obj;

    if (hasDelegationResponse() != other.hasDelegationResponse()) return false;
    if (hasDelegationResponse()) {
      if (!getDelegationResponse()
          .equals(other.getDelegationResponse())) return false;
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
    if (hasDelegationResponse()) {
      hash = (37 * hash) + DELEGATION_RESPONSE_FIELD_NUMBER;
      hash = (53 * hash) + getDelegationResponse().hashCode();
    }
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static com.cosmos.staking.v1beta1.QueryDelegationResponse parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.cosmos.staking.v1beta1.QueryDelegationResponse parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.cosmos.staking.v1beta1.QueryDelegationResponse parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.cosmos.staking.v1beta1.QueryDelegationResponse parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.cosmos.staking.v1beta1.QueryDelegationResponse parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.cosmos.staking.v1beta1.QueryDelegationResponse parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.cosmos.staking.v1beta1.QueryDelegationResponse parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.cosmos.staking.v1beta1.QueryDelegationResponse parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.cosmos.staking.v1beta1.QueryDelegationResponse parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static com.cosmos.staking.v1beta1.QueryDelegationResponse parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.cosmos.staking.v1beta1.QueryDelegationResponse parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.cosmos.staking.v1beta1.QueryDelegationResponse parseFrom(
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
  public static Builder newBuilder(com.cosmos.staking.v1beta1.QueryDelegationResponse prototype) {
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
   * QueryDelegationResponse is response type for the Query/Delegation RPC method.
   * </pre>
   *
   * Protobuf type {@code cosmos.staking.v1beta1.QueryDelegationResponse}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:cosmos.staking.v1beta1.QueryDelegationResponse)
      com.cosmos.staking.v1beta1.QueryDelegationResponseOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return com.cosmos.staking.v1beta1.QueryProto.internal_static_cosmos_staking_v1beta1_QueryDelegationResponse_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return com.cosmos.staking.v1beta1.QueryProto.internal_static_cosmos_staking_v1beta1_QueryDelegationResponse_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              com.cosmos.staking.v1beta1.QueryDelegationResponse.class, com.cosmos.staking.v1beta1.QueryDelegationResponse.Builder.class);
    }

    // Construct using com.cosmos.staking.v1beta1.QueryDelegationResponse.newBuilder()
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
      delegationResponse_ = null;
      if (delegationResponseBuilder_ != null) {
        delegationResponseBuilder_.dispose();
        delegationResponseBuilder_ = null;
      }
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return com.cosmos.staking.v1beta1.QueryProto.internal_static_cosmos_staking_v1beta1_QueryDelegationResponse_descriptor;
    }

    @java.lang.Override
    public com.cosmos.staking.v1beta1.QueryDelegationResponse getDefaultInstanceForType() {
      return com.cosmos.staking.v1beta1.QueryDelegationResponse.getDefaultInstance();
    }

    @java.lang.Override
    public com.cosmos.staking.v1beta1.QueryDelegationResponse build() {
      com.cosmos.staking.v1beta1.QueryDelegationResponse result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public com.cosmos.staking.v1beta1.QueryDelegationResponse buildPartial() {
      com.cosmos.staking.v1beta1.QueryDelegationResponse result = new com.cosmos.staking.v1beta1.QueryDelegationResponse(this);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartial0(com.cosmos.staking.v1beta1.QueryDelegationResponse result) {
      int from_bitField0_ = bitField0_;
      if (((from_bitField0_ & 0x00000001) != 0)) {
        result.delegationResponse_ = delegationResponseBuilder_ == null
            ? delegationResponse_
            : delegationResponseBuilder_.build();
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
      if (other instanceof com.cosmos.staking.v1beta1.QueryDelegationResponse) {
        return mergeFrom((com.cosmos.staking.v1beta1.QueryDelegationResponse)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(com.cosmos.staking.v1beta1.QueryDelegationResponse other) {
      if (other == com.cosmos.staking.v1beta1.QueryDelegationResponse.getDefaultInstance()) return this;
      if (other.hasDelegationResponse()) {
        mergeDelegationResponse(other.getDelegationResponse());
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
                  getDelegationResponseFieldBuilder().getBuilder(),
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

    private com.cosmos.staking.v1beta1.DelegationResponse delegationResponse_;
    private com.google.protobuf.SingleFieldBuilderV3<
        com.cosmos.staking.v1beta1.DelegationResponse, com.cosmos.staking.v1beta1.DelegationResponse.Builder, com.cosmos.staking.v1beta1.DelegationResponseOrBuilder> delegationResponseBuilder_;
    /**
     * <pre>
     * delegation_responses defines the delegation info of a delegation.
     * </pre>
     *
     * <code>.cosmos.staking.v1beta1.DelegationResponse delegation_response = 1 [json_name = "delegationResponse"];</code>
     * @return Whether the delegationResponse field is set.
     */
    public boolean hasDelegationResponse() {
      return ((bitField0_ & 0x00000001) != 0);
    }
    /**
     * <pre>
     * delegation_responses defines the delegation info of a delegation.
     * </pre>
     *
     * <code>.cosmos.staking.v1beta1.DelegationResponse delegation_response = 1 [json_name = "delegationResponse"];</code>
     * @return The delegationResponse.
     */
    public com.cosmos.staking.v1beta1.DelegationResponse getDelegationResponse() {
      if (delegationResponseBuilder_ == null) {
        return delegationResponse_ == null ? com.cosmos.staking.v1beta1.DelegationResponse.getDefaultInstance() : delegationResponse_;
      } else {
        return delegationResponseBuilder_.getMessage();
      }
    }
    /**
     * <pre>
     * delegation_responses defines the delegation info of a delegation.
     * </pre>
     *
     * <code>.cosmos.staking.v1beta1.DelegationResponse delegation_response = 1 [json_name = "delegationResponse"];</code>
     */
    public Builder setDelegationResponse(com.cosmos.staking.v1beta1.DelegationResponse value) {
      if (delegationResponseBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        delegationResponse_ = value;
      } else {
        delegationResponseBuilder_.setMessage(value);
      }
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * delegation_responses defines the delegation info of a delegation.
     * </pre>
     *
     * <code>.cosmos.staking.v1beta1.DelegationResponse delegation_response = 1 [json_name = "delegationResponse"];</code>
     */
    public Builder setDelegationResponse(
        com.cosmos.staking.v1beta1.DelegationResponse.Builder builderForValue) {
      if (delegationResponseBuilder_ == null) {
        delegationResponse_ = builderForValue.build();
      } else {
        delegationResponseBuilder_.setMessage(builderForValue.build());
      }
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * delegation_responses defines the delegation info of a delegation.
     * </pre>
     *
     * <code>.cosmos.staking.v1beta1.DelegationResponse delegation_response = 1 [json_name = "delegationResponse"];</code>
     */
    public Builder mergeDelegationResponse(com.cosmos.staking.v1beta1.DelegationResponse value) {
      if (delegationResponseBuilder_ == null) {
        if (((bitField0_ & 0x00000001) != 0) &&
          delegationResponse_ != null &&
          delegationResponse_ != com.cosmos.staking.v1beta1.DelegationResponse.getDefaultInstance()) {
          getDelegationResponseBuilder().mergeFrom(value);
        } else {
          delegationResponse_ = value;
        }
      } else {
        delegationResponseBuilder_.mergeFrom(value);
      }
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * delegation_responses defines the delegation info of a delegation.
     * </pre>
     *
     * <code>.cosmos.staking.v1beta1.DelegationResponse delegation_response = 1 [json_name = "delegationResponse"];</code>
     */
    public Builder clearDelegationResponse() {
      bitField0_ = (bitField0_ & ~0x00000001);
      delegationResponse_ = null;
      if (delegationResponseBuilder_ != null) {
        delegationResponseBuilder_.dispose();
        delegationResponseBuilder_ = null;
      }
      onChanged();
      return this;
    }
    /**
     * <pre>
     * delegation_responses defines the delegation info of a delegation.
     * </pre>
     *
     * <code>.cosmos.staking.v1beta1.DelegationResponse delegation_response = 1 [json_name = "delegationResponse"];</code>
     */
    public com.cosmos.staking.v1beta1.DelegationResponse.Builder getDelegationResponseBuilder() {
      bitField0_ |= 0x00000001;
      onChanged();
      return getDelegationResponseFieldBuilder().getBuilder();
    }
    /**
     * <pre>
     * delegation_responses defines the delegation info of a delegation.
     * </pre>
     *
     * <code>.cosmos.staking.v1beta1.DelegationResponse delegation_response = 1 [json_name = "delegationResponse"];</code>
     */
    public com.cosmos.staking.v1beta1.DelegationResponseOrBuilder getDelegationResponseOrBuilder() {
      if (delegationResponseBuilder_ != null) {
        return delegationResponseBuilder_.getMessageOrBuilder();
      } else {
        return delegationResponse_ == null ?
            com.cosmos.staking.v1beta1.DelegationResponse.getDefaultInstance() : delegationResponse_;
      }
    }
    /**
     * <pre>
     * delegation_responses defines the delegation info of a delegation.
     * </pre>
     *
     * <code>.cosmos.staking.v1beta1.DelegationResponse delegation_response = 1 [json_name = "delegationResponse"];</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
        com.cosmos.staking.v1beta1.DelegationResponse, com.cosmos.staking.v1beta1.DelegationResponse.Builder, com.cosmos.staking.v1beta1.DelegationResponseOrBuilder> 
        getDelegationResponseFieldBuilder() {
      if (delegationResponseBuilder_ == null) {
        delegationResponseBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
            com.cosmos.staking.v1beta1.DelegationResponse, com.cosmos.staking.v1beta1.DelegationResponse.Builder, com.cosmos.staking.v1beta1.DelegationResponseOrBuilder>(
                getDelegationResponse(),
                getParentForChildren(),
                isClean());
        delegationResponse_ = null;
      }
      return delegationResponseBuilder_;
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


    // @@protoc_insertion_point(builder_scope:cosmos.staking.v1beta1.QueryDelegationResponse)
  }

  // @@protoc_insertion_point(class_scope:cosmos.staking.v1beta1.QueryDelegationResponse)
  private static final com.cosmos.staking.v1beta1.QueryDelegationResponse DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new com.cosmos.staking.v1beta1.QueryDelegationResponse();
  }

  public static com.cosmos.staking.v1beta1.QueryDelegationResponse getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<QueryDelegationResponse>
      PARSER = new com.google.protobuf.AbstractParser<QueryDelegationResponse>() {
    @java.lang.Override
    public QueryDelegationResponse parsePartialFrom(
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

  public static com.google.protobuf.Parser<QueryDelegationResponse> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<QueryDelegationResponse> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public com.cosmos.staking.v1beta1.QueryDelegationResponse getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

