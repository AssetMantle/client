// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: ibc/core/connection/v1/query.proto

package com.ibc.core.connection.v1;

/**
 * <pre>
 * QueryConnectionResponse is the response type for the Query/Connection RPC
 * method. Besides the connection end, it includes a proof and the height from
 * which the proof was retrieved.
 * </pre>
 *
 * Protobuf type {@code ibc.core.connection.v1.QueryConnectionResponse}
 */
public final class QueryConnectionResponse extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:ibc.core.connection.v1.QueryConnectionResponse)
    QueryConnectionResponseOrBuilder {
private static final long serialVersionUID = 0L;
  // Use QueryConnectionResponse.newBuilder() to construct.
  private QueryConnectionResponse(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private QueryConnectionResponse() {
    proof_ = com.google.protobuf.ByteString.EMPTY;
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new QueryConnectionResponse();
  }

  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return com.ibc.core.connection.v1.QueryProto.internal_static_ibc_core_connection_v1_QueryConnectionResponse_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return com.ibc.core.connection.v1.QueryProto.internal_static_ibc_core_connection_v1_QueryConnectionResponse_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            com.ibc.core.connection.v1.QueryConnectionResponse.class, com.ibc.core.connection.v1.QueryConnectionResponse.Builder.class);
  }

  public static final int CONNECTION_FIELD_NUMBER = 1;
  private com.ibc.core.connection.v1.ConnectionEnd connection_;
  /**
   * <pre>
   * connection associated with the request identifier
   * </pre>
   *
   * <code>.ibc.core.connection.v1.ConnectionEnd connection = 1 [json_name = "connection"];</code>
   * @return Whether the connection field is set.
   */
  @java.lang.Override
  public boolean hasConnection() {
    return connection_ != null;
  }
  /**
   * <pre>
   * connection associated with the request identifier
   * </pre>
   *
   * <code>.ibc.core.connection.v1.ConnectionEnd connection = 1 [json_name = "connection"];</code>
   * @return The connection.
   */
  @java.lang.Override
  public com.ibc.core.connection.v1.ConnectionEnd getConnection() {
    return connection_ == null ? com.ibc.core.connection.v1.ConnectionEnd.getDefaultInstance() : connection_;
  }
  /**
   * <pre>
   * connection associated with the request identifier
   * </pre>
   *
   * <code>.ibc.core.connection.v1.ConnectionEnd connection = 1 [json_name = "connection"];</code>
   */
  @java.lang.Override
  public com.ibc.core.connection.v1.ConnectionEndOrBuilder getConnectionOrBuilder() {
    return connection_ == null ? com.ibc.core.connection.v1.ConnectionEnd.getDefaultInstance() : connection_;
  }

  public static final int PROOF_FIELD_NUMBER = 2;
  private com.google.protobuf.ByteString proof_ = com.google.protobuf.ByteString.EMPTY;
  /**
   * <pre>
   * merkle proof of existence
   * </pre>
   *
   * <code>bytes proof = 2 [json_name = "proof"];</code>
   * @return The proof.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString getProof() {
    return proof_;
  }

  public static final int PROOF_HEIGHT_FIELD_NUMBER = 3;
  private com.ibc.core.client.v1.Height proofHeight_;
  /**
   * <pre>
   * height at which the proof was retrieved
   * </pre>
   *
   * <code>.ibc.core.client.v1.Height proof_height = 3 [json_name = "proofHeight", (.gogoproto.nullable) = false];</code>
   * @return Whether the proofHeight field is set.
   */
  @java.lang.Override
  public boolean hasProofHeight() {
    return proofHeight_ != null;
  }
  /**
   * <pre>
   * height at which the proof was retrieved
   * </pre>
   *
   * <code>.ibc.core.client.v1.Height proof_height = 3 [json_name = "proofHeight", (.gogoproto.nullable) = false];</code>
   * @return The proofHeight.
   */
  @java.lang.Override
  public com.ibc.core.client.v1.Height getProofHeight() {
    return proofHeight_ == null ? com.ibc.core.client.v1.Height.getDefaultInstance() : proofHeight_;
  }
  /**
   * <pre>
   * height at which the proof was retrieved
   * </pre>
   *
   * <code>.ibc.core.client.v1.Height proof_height = 3 [json_name = "proofHeight", (.gogoproto.nullable) = false];</code>
   */
  @java.lang.Override
  public com.ibc.core.client.v1.HeightOrBuilder getProofHeightOrBuilder() {
    return proofHeight_ == null ? com.ibc.core.client.v1.Height.getDefaultInstance() : proofHeight_;
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
    if (connection_ != null) {
      output.writeMessage(1, getConnection());
    }
    if (!proof_.isEmpty()) {
      output.writeBytes(2, proof_);
    }
    if (proofHeight_ != null) {
      output.writeMessage(3, getProofHeight());
    }
    getUnknownFields().writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (connection_ != null) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(1, getConnection());
    }
    if (!proof_.isEmpty()) {
      size += com.google.protobuf.CodedOutputStream
        .computeBytesSize(2, proof_);
    }
    if (proofHeight_ != null) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(3, getProofHeight());
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
    if (!(obj instanceof com.ibc.core.connection.v1.QueryConnectionResponse)) {
      return super.equals(obj);
    }
    com.ibc.core.connection.v1.QueryConnectionResponse other = (com.ibc.core.connection.v1.QueryConnectionResponse) obj;

    if (hasConnection() != other.hasConnection()) return false;
    if (hasConnection()) {
      if (!getConnection()
          .equals(other.getConnection())) return false;
    }
    if (!getProof()
        .equals(other.getProof())) return false;
    if (hasProofHeight() != other.hasProofHeight()) return false;
    if (hasProofHeight()) {
      if (!getProofHeight()
          .equals(other.getProofHeight())) return false;
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
    if (hasConnection()) {
      hash = (37 * hash) + CONNECTION_FIELD_NUMBER;
      hash = (53 * hash) + getConnection().hashCode();
    }
    hash = (37 * hash) + PROOF_FIELD_NUMBER;
    hash = (53 * hash) + getProof().hashCode();
    if (hasProofHeight()) {
      hash = (37 * hash) + PROOF_HEIGHT_FIELD_NUMBER;
      hash = (53 * hash) + getProofHeight().hashCode();
    }
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static com.ibc.core.connection.v1.QueryConnectionResponse parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.ibc.core.connection.v1.QueryConnectionResponse parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.ibc.core.connection.v1.QueryConnectionResponse parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.ibc.core.connection.v1.QueryConnectionResponse parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.ibc.core.connection.v1.QueryConnectionResponse parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.ibc.core.connection.v1.QueryConnectionResponse parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.ibc.core.connection.v1.QueryConnectionResponse parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.ibc.core.connection.v1.QueryConnectionResponse parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.ibc.core.connection.v1.QueryConnectionResponse parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static com.ibc.core.connection.v1.QueryConnectionResponse parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.ibc.core.connection.v1.QueryConnectionResponse parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.ibc.core.connection.v1.QueryConnectionResponse parseFrom(
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
  public static Builder newBuilder(com.ibc.core.connection.v1.QueryConnectionResponse prototype) {
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
   * QueryConnectionResponse is the response type for the Query/Connection RPC
   * method. Besides the connection end, it includes a proof and the height from
   * which the proof was retrieved.
   * </pre>
   *
   * Protobuf type {@code ibc.core.connection.v1.QueryConnectionResponse}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:ibc.core.connection.v1.QueryConnectionResponse)
      com.ibc.core.connection.v1.QueryConnectionResponseOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return com.ibc.core.connection.v1.QueryProto.internal_static_ibc_core_connection_v1_QueryConnectionResponse_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return com.ibc.core.connection.v1.QueryProto.internal_static_ibc_core_connection_v1_QueryConnectionResponse_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              com.ibc.core.connection.v1.QueryConnectionResponse.class, com.ibc.core.connection.v1.QueryConnectionResponse.Builder.class);
    }

    // Construct using com.ibc.core.connection.v1.QueryConnectionResponse.newBuilder()
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
      connection_ = null;
      if (connectionBuilder_ != null) {
        connectionBuilder_.dispose();
        connectionBuilder_ = null;
      }
      proof_ = com.google.protobuf.ByteString.EMPTY;
      proofHeight_ = null;
      if (proofHeightBuilder_ != null) {
        proofHeightBuilder_.dispose();
        proofHeightBuilder_ = null;
      }
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return com.ibc.core.connection.v1.QueryProto.internal_static_ibc_core_connection_v1_QueryConnectionResponse_descriptor;
    }

    @java.lang.Override
    public com.ibc.core.connection.v1.QueryConnectionResponse getDefaultInstanceForType() {
      return com.ibc.core.connection.v1.QueryConnectionResponse.getDefaultInstance();
    }

    @java.lang.Override
    public com.ibc.core.connection.v1.QueryConnectionResponse build() {
      com.ibc.core.connection.v1.QueryConnectionResponse result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public com.ibc.core.connection.v1.QueryConnectionResponse buildPartial() {
      com.ibc.core.connection.v1.QueryConnectionResponse result = new com.ibc.core.connection.v1.QueryConnectionResponse(this);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartial0(com.ibc.core.connection.v1.QueryConnectionResponse result) {
      int from_bitField0_ = bitField0_;
      if (((from_bitField0_ & 0x00000001) != 0)) {
        result.connection_ = connectionBuilder_ == null
            ? connection_
            : connectionBuilder_.build();
      }
      if (((from_bitField0_ & 0x00000002) != 0)) {
        result.proof_ = proof_;
      }
      if (((from_bitField0_ & 0x00000004) != 0)) {
        result.proofHeight_ = proofHeightBuilder_ == null
            ? proofHeight_
            : proofHeightBuilder_.build();
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
      if (other instanceof com.ibc.core.connection.v1.QueryConnectionResponse) {
        return mergeFrom((com.ibc.core.connection.v1.QueryConnectionResponse)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(com.ibc.core.connection.v1.QueryConnectionResponse other) {
      if (other == com.ibc.core.connection.v1.QueryConnectionResponse.getDefaultInstance()) return this;
      if (other.hasConnection()) {
        mergeConnection(other.getConnection());
      }
      if (other.getProof() != com.google.protobuf.ByteString.EMPTY) {
        setProof(other.getProof());
      }
      if (other.hasProofHeight()) {
        mergeProofHeight(other.getProofHeight());
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
                  getConnectionFieldBuilder().getBuilder(),
                  extensionRegistry);
              bitField0_ |= 0x00000001;
              break;
            } // case 10
            case 18: {
              proof_ = input.readBytes();
              bitField0_ |= 0x00000002;
              break;
            } // case 18
            case 26: {
              input.readMessage(
                  getProofHeightFieldBuilder().getBuilder(),
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

    private com.ibc.core.connection.v1.ConnectionEnd connection_;
    private com.google.protobuf.SingleFieldBuilderV3<
        com.ibc.core.connection.v1.ConnectionEnd, com.ibc.core.connection.v1.ConnectionEnd.Builder, com.ibc.core.connection.v1.ConnectionEndOrBuilder> connectionBuilder_;
    /**
     * <pre>
     * connection associated with the request identifier
     * </pre>
     *
     * <code>.ibc.core.connection.v1.ConnectionEnd connection = 1 [json_name = "connection"];</code>
     * @return Whether the connection field is set.
     */
    public boolean hasConnection() {
      return ((bitField0_ & 0x00000001) != 0);
    }
    /**
     * <pre>
     * connection associated with the request identifier
     * </pre>
     *
     * <code>.ibc.core.connection.v1.ConnectionEnd connection = 1 [json_name = "connection"];</code>
     * @return The connection.
     */
    public com.ibc.core.connection.v1.ConnectionEnd getConnection() {
      if (connectionBuilder_ == null) {
        return connection_ == null ? com.ibc.core.connection.v1.ConnectionEnd.getDefaultInstance() : connection_;
      } else {
        return connectionBuilder_.getMessage();
      }
    }
    /**
     * <pre>
     * connection associated with the request identifier
     * </pre>
     *
     * <code>.ibc.core.connection.v1.ConnectionEnd connection = 1 [json_name = "connection"];</code>
     */
    public Builder setConnection(com.ibc.core.connection.v1.ConnectionEnd value) {
      if (connectionBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        connection_ = value;
      } else {
        connectionBuilder_.setMessage(value);
      }
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * connection associated with the request identifier
     * </pre>
     *
     * <code>.ibc.core.connection.v1.ConnectionEnd connection = 1 [json_name = "connection"];</code>
     */
    public Builder setConnection(
        com.ibc.core.connection.v1.ConnectionEnd.Builder builderForValue) {
      if (connectionBuilder_ == null) {
        connection_ = builderForValue.build();
      } else {
        connectionBuilder_.setMessage(builderForValue.build());
      }
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * connection associated with the request identifier
     * </pre>
     *
     * <code>.ibc.core.connection.v1.ConnectionEnd connection = 1 [json_name = "connection"];</code>
     */
    public Builder mergeConnection(com.ibc.core.connection.v1.ConnectionEnd value) {
      if (connectionBuilder_ == null) {
        if (((bitField0_ & 0x00000001) != 0) &&
          connection_ != null &&
          connection_ != com.ibc.core.connection.v1.ConnectionEnd.getDefaultInstance()) {
          getConnectionBuilder().mergeFrom(value);
        } else {
          connection_ = value;
        }
      } else {
        connectionBuilder_.mergeFrom(value);
      }
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * connection associated with the request identifier
     * </pre>
     *
     * <code>.ibc.core.connection.v1.ConnectionEnd connection = 1 [json_name = "connection"];</code>
     */
    public Builder clearConnection() {
      bitField0_ = (bitField0_ & ~0x00000001);
      connection_ = null;
      if (connectionBuilder_ != null) {
        connectionBuilder_.dispose();
        connectionBuilder_ = null;
      }
      onChanged();
      return this;
    }
    /**
     * <pre>
     * connection associated with the request identifier
     * </pre>
     *
     * <code>.ibc.core.connection.v1.ConnectionEnd connection = 1 [json_name = "connection"];</code>
     */
    public com.ibc.core.connection.v1.ConnectionEnd.Builder getConnectionBuilder() {
      bitField0_ |= 0x00000001;
      onChanged();
      return getConnectionFieldBuilder().getBuilder();
    }
    /**
     * <pre>
     * connection associated with the request identifier
     * </pre>
     *
     * <code>.ibc.core.connection.v1.ConnectionEnd connection = 1 [json_name = "connection"];</code>
     */
    public com.ibc.core.connection.v1.ConnectionEndOrBuilder getConnectionOrBuilder() {
      if (connectionBuilder_ != null) {
        return connectionBuilder_.getMessageOrBuilder();
      } else {
        return connection_ == null ?
            com.ibc.core.connection.v1.ConnectionEnd.getDefaultInstance() : connection_;
      }
    }
    /**
     * <pre>
     * connection associated with the request identifier
     * </pre>
     *
     * <code>.ibc.core.connection.v1.ConnectionEnd connection = 1 [json_name = "connection"];</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
        com.ibc.core.connection.v1.ConnectionEnd, com.ibc.core.connection.v1.ConnectionEnd.Builder, com.ibc.core.connection.v1.ConnectionEndOrBuilder> 
        getConnectionFieldBuilder() {
      if (connectionBuilder_ == null) {
        connectionBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
            com.ibc.core.connection.v1.ConnectionEnd, com.ibc.core.connection.v1.ConnectionEnd.Builder, com.ibc.core.connection.v1.ConnectionEndOrBuilder>(
                getConnection(),
                getParentForChildren(),
                isClean());
        connection_ = null;
      }
      return connectionBuilder_;
    }

    private com.google.protobuf.ByteString proof_ = com.google.protobuf.ByteString.EMPTY;
    /**
     * <pre>
     * merkle proof of existence
     * </pre>
     *
     * <code>bytes proof = 2 [json_name = "proof"];</code>
     * @return The proof.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString getProof() {
      return proof_;
    }
    /**
     * <pre>
     * merkle proof of existence
     * </pre>
     *
     * <code>bytes proof = 2 [json_name = "proof"];</code>
     * @param value The proof to set.
     * @return This builder for chaining.
     */
    public Builder setProof(com.google.protobuf.ByteString value) {
      if (value == null) { throw new NullPointerException(); }
      proof_ = value;
      bitField0_ |= 0x00000002;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * merkle proof of existence
     * </pre>
     *
     * <code>bytes proof = 2 [json_name = "proof"];</code>
     * @return This builder for chaining.
     */
    public Builder clearProof() {
      bitField0_ = (bitField0_ & ~0x00000002);
      proof_ = getDefaultInstance().getProof();
      onChanged();
      return this;
    }

    private com.ibc.core.client.v1.Height proofHeight_;
    private com.google.protobuf.SingleFieldBuilderV3<
        com.ibc.core.client.v1.Height, com.ibc.core.client.v1.Height.Builder, com.ibc.core.client.v1.HeightOrBuilder> proofHeightBuilder_;
    /**
     * <pre>
     * height at which the proof was retrieved
     * </pre>
     *
     * <code>.ibc.core.client.v1.Height proof_height = 3 [json_name = "proofHeight", (.gogoproto.nullable) = false];</code>
     * @return Whether the proofHeight field is set.
     */
    public boolean hasProofHeight() {
      return ((bitField0_ & 0x00000004) != 0);
    }
    /**
     * <pre>
     * height at which the proof was retrieved
     * </pre>
     *
     * <code>.ibc.core.client.v1.Height proof_height = 3 [json_name = "proofHeight", (.gogoproto.nullable) = false];</code>
     * @return The proofHeight.
     */
    public com.ibc.core.client.v1.Height getProofHeight() {
      if (proofHeightBuilder_ == null) {
        return proofHeight_ == null ? com.ibc.core.client.v1.Height.getDefaultInstance() : proofHeight_;
      } else {
        return proofHeightBuilder_.getMessage();
      }
    }
    /**
     * <pre>
     * height at which the proof was retrieved
     * </pre>
     *
     * <code>.ibc.core.client.v1.Height proof_height = 3 [json_name = "proofHeight", (.gogoproto.nullable) = false];</code>
     */
    public Builder setProofHeight(com.ibc.core.client.v1.Height value) {
      if (proofHeightBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        proofHeight_ = value;
      } else {
        proofHeightBuilder_.setMessage(value);
      }
      bitField0_ |= 0x00000004;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * height at which the proof was retrieved
     * </pre>
     *
     * <code>.ibc.core.client.v1.Height proof_height = 3 [json_name = "proofHeight", (.gogoproto.nullable) = false];</code>
     */
    public Builder setProofHeight(
        com.ibc.core.client.v1.Height.Builder builderForValue) {
      if (proofHeightBuilder_ == null) {
        proofHeight_ = builderForValue.build();
      } else {
        proofHeightBuilder_.setMessage(builderForValue.build());
      }
      bitField0_ |= 0x00000004;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * height at which the proof was retrieved
     * </pre>
     *
     * <code>.ibc.core.client.v1.Height proof_height = 3 [json_name = "proofHeight", (.gogoproto.nullable) = false];</code>
     */
    public Builder mergeProofHeight(com.ibc.core.client.v1.Height value) {
      if (proofHeightBuilder_ == null) {
        if (((bitField0_ & 0x00000004) != 0) &&
          proofHeight_ != null &&
          proofHeight_ != com.ibc.core.client.v1.Height.getDefaultInstance()) {
          getProofHeightBuilder().mergeFrom(value);
        } else {
          proofHeight_ = value;
        }
      } else {
        proofHeightBuilder_.mergeFrom(value);
      }
      bitField0_ |= 0x00000004;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * height at which the proof was retrieved
     * </pre>
     *
     * <code>.ibc.core.client.v1.Height proof_height = 3 [json_name = "proofHeight", (.gogoproto.nullable) = false];</code>
     */
    public Builder clearProofHeight() {
      bitField0_ = (bitField0_ & ~0x00000004);
      proofHeight_ = null;
      if (proofHeightBuilder_ != null) {
        proofHeightBuilder_.dispose();
        proofHeightBuilder_ = null;
      }
      onChanged();
      return this;
    }
    /**
     * <pre>
     * height at which the proof was retrieved
     * </pre>
     *
     * <code>.ibc.core.client.v1.Height proof_height = 3 [json_name = "proofHeight", (.gogoproto.nullable) = false];</code>
     */
    public com.ibc.core.client.v1.Height.Builder getProofHeightBuilder() {
      bitField0_ |= 0x00000004;
      onChanged();
      return getProofHeightFieldBuilder().getBuilder();
    }
    /**
     * <pre>
     * height at which the proof was retrieved
     * </pre>
     *
     * <code>.ibc.core.client.v1.Height proof_height = 3 [json_name = "proofHeight", (.gogoproto.nullable) = false];</code>
     */
    public com.ibc.core.client.v1.HeightOrBuilder getProofHeightOrBuilder() {
      if (proofHeightBuilder_ != null) {
        return proofHeightBuilder_.getMessageOrBuilder();
      } else {
        return proofHeight_ == null ?
            com.ibc.core.client.v1.Height.getDefaultInstance() : proofHeight_;
      }
    }
    /**
     * <pre>
     * height at which the proof was retrieved
     * </pre>
     *
     * <code>.ibc.core.client.v1.Height proof_height = 3 [json_name = "proofHeight", (.gogoproto.nullable) = false];</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
        com.ibc.core.client.v1.Height, com.ibc.core.client.v1.Height.Builder, com.ibc.core.client.v1.HeightOrBuilder> 
        getProofHeightFieldBuilder() {
      if (proofHeightBuilder_ == null) {
        proofHeightBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
            com.ibc.core.client.v1.Height, com.ibc.core.client.v1.Height.Builder, com.ibc.core.client.v1.HeightOrBuilder>(
                getProofHeight(),
                getParentForChildren(),
                isClean());
        proofHeight_ = null;
      }
      return proofHeightBuilder_;
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


    // @@protoc_insertion_point(builder_scope:ibc.core.connection.v1.QueryConnectionResponse)
  }

  // @@protoc_insertion_point(class_scope:ibc.core.connection.v1.QueryConnectionResponse)
  private static final com.ibc.core.connection.v1.QueryConnectionResponse DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new com.ibc.core.connection.v1.QueryConnectionResponse();
  }

  public static com.ibc.core.connection.v1.QueryConnectionResponse getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<QueryConnectionResponse>
      PARSER = new com.google.protobuf.AbstractParser<QueryConnectionResponse>() {
    @java.lang.Override
    public QueryConnectionResponse parsePartialFrom(
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

  public static com.google.protobuf.Parser<QueryConnectionResponse> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<QueryConnectionResponse> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public com.ibc.core.connection.v1.QueryConnectionResponse getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

