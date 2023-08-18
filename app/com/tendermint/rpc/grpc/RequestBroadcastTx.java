// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: tendermint/rpc/grpc/types.proto

package com.tendermint.rpc.grpc;

/**
 * Protobuf type {@code tendermint.rpc.grpc.RequestBroadcastTx}
 */
public final class RequestBroadcastTx extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:tendermint.rpc.grpc.RequestBroadcastTx)
    RequestBroadcastTxOrBuilder {
private static final long serialVersionUID = 0L;
  // Use RequestBroadcastTx.newBuilder() to construct.
  private RequestBroadcastTx(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private RequestBroadcastTx() {
    tx_ = com.google.protobuf.ByteString.EMPTY;
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new RequestBroadcastTx();
  }

  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return com.tendermint.rpc.grpc.TypesProto.internal_static_tendermint_rpc_grpc_RequestBroadcastTx_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return com.tendermint.rpc.grpc.TypesProto.internal_static_tendermint_rpc_grpc_RequestBroadcastTx_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            com.tendermint.rpc.grpc.RequestBroadcastTx.class, com.tendermint.rpc.grpc.RequestBroadcastTx.Builder.class);
  }

  public static final int TX_FIELD_NUMBER = 1;
  private com.google.protobuf.ByteString tx_ = com.google.protobuf.ByteString.EMPTY;
  /**
   * <code>bytes tx = 1 [json_name = "tx"];</code>
   * @return The tx.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString getTx() {
    return tx_;
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
    if (!tx_.isEmpty()) {
      output.writeBytes(1, tx_);
    }
    getUnknownFields().writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (!tx_.isEmpty()) {
      size += com.google.protobuf.CodedOutputStream
        .computeBytesSize(1, tx_);
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
    if (!(obj instanceof com.tendermint.rpc.grpc.RequestBroadcastTx)) {
      return super.equals(obj);
    }
    com.tendermint.rpc.grpc.RequestBroadcastTx other = (com.tendermint.rpc.grpc.RequestBroadcastTx) obj;

    if (!getTx()
        .equals(other.getTx())) return false;
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
    hash = (37 * hash) + TX_FIELD_NUMBER;
    hash = (53 * hash) + getTx().hashCode();
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static com.tendermint.rpc.grpc.RequestBroadcastTx parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.tendermint.rpc.grpc.RequestBroadcastTx parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.tendermint.rpc.grpc.RequestBroadcastTx parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.tendermint.rpc.grpc.RequestBroadcastTx parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.tendermint.rpc.grpc.RequestBroadcastTx parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.tendermint.rpc.grpc.RequestBroadcastTx parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.tendermint.rpc.grpc.RequestBroadcastTx parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.tendermint.rpc.grpc.RequestBroadcastTx parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.tendermint.rpc.grpc.RequestBroadcastTx parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static com.tendermint.rpc.grpc.RequestBroadcastTx parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.tendermint.rpc.grpc.RequestBroadcastTx parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.tendermint.rpc.grpc.RequestBroadcastTx parseFrom(
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
  public static Builder newBuilder(com.tendermint.rpc.grpc.RequestBroadcastTx prototype) {
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
   * Protobuf type {@code tendermint.rpc.grpc.RequestBroadcastTx}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:tendermint.rpc.grpc.RequestBroadcastTx)
      com.tendermint.rpc.grpc.RequestBroadcastTxOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return com.tendermint.rpc.grpc.TypesProto.internal_static_tendermint_rpc_grpc_RequestBroadcastTx_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return com.tendermint.rpc.grpc.TypesProto.internal_static_tendermint_rpc_grpc_RequestBroadcastTx_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              com.tendermint.rpc.grpc.RequestBroadcastTx.class, com.tendermint.rpc.grpc.RequestBroadcastTx.Builder.class);
    }

    // Construct using com.tendermint.rpc.grpc.RequestBroadcastTx.newBuilder()
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
      tx_ = com.google.protobuf.ByteString.EMPTY;
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return com.tendermint.rpc.grpc.TypesProto.internal_static_tendermint_rpc_grpc_RequestBroadcastTx_descriptor;
    }

    @java.lang.Override
    public com.tendermint.rpc.grpc.RequestBroadcastTx getDefaultInstanceForType() {
      return com.tendermint.rpc.grpc.RequestBroadcastTx.getDefaultInstance();
    }

    @java.lang.Override
    public com.tendermint.rpc.grpc.RequestBroadcastTx build() {
      com.tendermint.rpc.grpc.RequestBroadcastTx result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public com.tendermint.rpc.grpc.RequestBroadcastTx buildPartial() {
      com.tendermint.rpc.grpc.RequestBroadcastTx result = new com.tendermint.rpc.grpc.RequestBroadcastTx(this);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartial0(com.tendermint.rpc.grpc.RequestBroadcastTx result) {
      int from_bitField0_ = bitField0_;
      if (((from_bitField0_ & 0x00000001) != 0)) {
        result.tx_ = tx_;
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
      if (other instanceof com.tendermint.rpc.grpc.RequestBroadcastTx) {
        return mergeFrom((com.tendermint.rpc.grpc.RequestBroadcastTx)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(com.tendermint.rpc.grpc.RequestBroadcastTx other) {
      if (other == com.tendermint.rpc.grpc.RequestBroadcastTx.getDefaultInstance()) return this;
      if (other.getTx() != com.google.protobuf.ByteString.EMPTY) {
        setTx(other.getTx());
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
              tx_ = input.readBytes();
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

    private com.google.protobuf.ByteString tx_ = com.google.protobuf.ByteString.EMPTY;
    /**
     * <code>bytes tx = 1 [json_name = "tx"];</code>
     * @return The tx.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString getTx() {
      return tx_;
    }
    /**
     * <code>bytes tx = 1 [json_name = "tx"];</code>
     * @param value The tx to set.
     * @return This builder for chaining.
     */
    public Builder setTx(com.google.protobuf.ByteString value) {
      if (value == null) { throw new NullPointerException(); }
      tx_ = value;
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <code>bytes tx = 1 [json_name = "tx"];</code>
     * @return This builder for chaining.
     */
    public Builder clearTx() {
      bitField0_ = (bitField0_ & ~0x00000001);
      tx_ = getDefaultInstance().getTx();
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


    // @@protoc_insertion_point(builder_scope:tendermint.rpc.grpc.RequestBroadcastTx)
  }

  // @@protoc_insertion_point(class_scope:tendermint.rpc.grpc.RequestBroadcastTx)
  private static final com.tendermint.rpc.grpc.RequestBroadcastTx DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new com.tendermint.rpc.grpc.RequestBroadcastTx();
  }

  public static com.tendermint.rpc.grpc.RequestBroadcastTx getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<RequestBroadcastTx>
      PARSER = new com.google.protobuf.AbstractParser<RequestBroadcastTx>() {
    @java.lang.Override
    public RequestBroadcastTx parsePartialFrom(
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

  public static com.google.protobuf.Parser<RequestBroadcastTx> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<RequestBroadcastTx> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public com.tendermint.rpc.grpc.RequestBroadcastTx getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

