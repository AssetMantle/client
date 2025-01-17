// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: tendermint/abci/types.proto

package com.tendermint.abci;

/**
 * Protobuf type {@code tendermint.abci.RequestInfo}
 */
public final class RequestInfo extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:tendermint.abci.RequestInfo)
    RequestInfoOrBuilder {
private static final long serialVersionUID = 0L;
  // Use RequestInfo.newBuilder() to construct.
  private RequestInfo(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private RequestInfo() {
    version_ = "";
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new RequestInfo();
  }

  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return com.tendermint.abci.TypesProto.internal_static_tendermint_abci_RequestInfo_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return com.tendermint.abci.TypesProto.internal_static_tendermint_abci_RequestInfo_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            com.tendermint.abci.RequestInfo.class, com.tendermint.abci.RequestInfo.Builder.class);
  }

  public static final int VERSION_FIELD_NUMBER = 1;
  @SuppressWarnings("serial")
  private volatile java.lang.Object version_ = "";
  /**
   * <code>string version = 1 [json_name = "version"];</code>
   * @return The version.
   */
  @java.lang.Override
  public java.lang.String getVersion() {
    java.lang.Object ref = version_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      version_ = s;
      return s;
    }
  }
  /**
   * <code>string version = 1 [json_name = "version"];</code>
   * @return The bytes for version.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getVersionBytes() {
    java.lang.Object ref = version_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      version_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int BLOCK_VERSION_FIELD_NUMBER = 2;
  private long blockVersion_ = 0L;
  /**
   * <code>uint64 block_version = 2 [json_name = "blockVersion"];</code>
   * @return The blockVersion.
   */
  @java.lang.Override
  public long getBlockVersion() {
    return blockVersion_;
  }

  public static final int P2P_VERSION_FIELD_NUMBER = 3;
  private long p2PVersion_ = 0L;
  /**
   * <code>uint64 p2p_version = 3 [json_name = "p2pVersion"];</code>
   * @return The p2pVersion.
   */
  @java.lang.Override
  public long getP2PVersion() {
    return p2PVersion_;
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
    if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(version_)) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 1, version_);
    }
    if (blockVersion_ != 0L) {
      output.writeUInt64(2, blockVersion_);
    }
    if (p2PVersion_ != 0L) {
      output.writeUInt64(3, p2PVersion_);
    }
    getUnknownFields().writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(version_)) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(1, version_);
    }
    if (blockVersion_ != 0L) {
      size += com.google.protobuf.CodedOutputStream
        .computeUInt64Size(2, blockVersion_);
    }
    if (p2PVersion_ != 0L) {
      size += com.google.protobuf.CodedOutputStream
        .computeUInt64Size(3, p2PVersion_);
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
    if (!(obj instanceof com.tendermint.abci.RequestInfo)) {
      return super.equals(obj);
    }
    com.tendermint.abci.RequestInfo other = (com.tendermint.abci.RequestInfo) obj;

    if (!getVersion()
        .equals(other.getVersion())) return false;
    if (getBlockVersion()
        != other.getBlockVersion()) return false;
    if (getP2PVersion()
        != other.getP2PVersion()) return false;
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
    hash = (37 * hash) + VERSION_FIELD_NUMBER;
    hash = (53 * hash) + getVersion().hashCode();
    hash = (37 * hash) + BLOCK_VERSION_FIELD_NUMBER;
    hash = (53 * hash) + com.google.protobuf.Internal.hashLong(
        getBlockVersion());
    hash = (37 * hash) + P2P_VERSION_FIELD_NUMBER;
    hash = (53 * hash) + com.google.protobuf.Internal.hashLong(
        getP2PVersion());
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static com.tendermint.abci.RequestInfo parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.tendermint.abci.RequestInfo parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.tendermint.abci.RequestInfo parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.tendermint.abci.RequestInfo parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.tendermint.abci.RequestInfo parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.tendermint.abci.RequestInfo parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.tendermint.abci.RequestInfo parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.tendermint.abci.RequestInfo parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.tendermint.abci.RequestInfo parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static com.tendermint.abci.RequestInfo parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.tendermint.abci.RequestInfo parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.tendermint.abci.RequestInfo parseFrom(
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
  public static Builder newBuilder(com.tendermint.abci.RequestInfo prototype) {
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
   * Protobuf type {@code tendermint.abci.RequestInfo}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:tendermint.abci.RequestInfo)
      com.tendermint.abci.RequestInfoOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return com.tendermint.abci.TypesProto.internal_static_tendermint_abci_RequestInfo_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return com.tendermint.abci.TypesProto.internal_static_tendermint_abci_RequestInfo_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              com.tendermint.abci.RequestInfo.class, com.tendermint.abci.RequestInfo.Builder.class);
    }

    // Construct using com.tendermint.abci.RequestInfo.newBuilder()
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
      version_ = "";
      blockVersion_ = 0L;
      p2PVersion_ = 0L;
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return com.tendermint.abci.TypesProto.internal_static_tendermint_abci_RequestInfo_descriptor;
    }

    @java.lang.Override
    public com.tendermint.abci.RequestInfo getDefaultInstanceForType() {
      return com.tendermint.abci.RequestInfo.getDefaultInstance();
    }

    @java.lang.Override
    public com.tendermint.abci.RequestInfo build() {
      com.tendermint.abci.RequestInfo result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public com.tendermint.abci.RequestInfo buildPartial() {
      com.tendermint.abci.RequestInfo result = new com.tendermint.abci.RequestInfo(this);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartial0(com.tendermint.abci.RequestInfo result) {
      int from_bitField0_ = bitField0_;
      if (((from_bitField0_ & 0x00000001) != 0)) {
        result.version_ = version_;
      }
      if (((from_bitField0_ & 0x00000002) != 0)) {
        result.blockVersion_ = blockVersion_;
      }
      if (((from_bitField0_ & 0x00000004) != 0)) {
        result.p2PVersion_ = p2PVersion_;
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
      if (other instanceof com.tendermint.abci.RequestInfo) {
        return mergeFrom((com.tendermint.abci.RequestInfo)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(com.tendermint.abci.RequestInfo other) {
      if (other == com.tendermint.abci.RequestInfo.getDefaultInstance()) return this;
      if (!other.getVersion().isEmpty()) {
        version_ = other.version_;
        bitField0_ |= 0x00000001;
        onChanged();
      }
      if (other.getBlockVersion() != 0L) {
        setBlockVersion(other.getBlockVersion());
      }
      if (other.getP2PVersion() != 0L) {
        setP2PVersion(other.getP2PVersion());
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
              version_ = input.readStringRequireUtf8();
              bitField0_ |= 0x00000001;
              break;
            } // case 10
            case 16: {
              blockVersion_ = input.readUInt64();
              bitField0_ |= 0x00000002;
              break;
            } // case 16
            case 24: {
              p2PVersion_ = input.readUInt64();
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

    private java.lang.Object version_ = "";
    /**
     * <code>string version = 1 [json_name = "version"];</code>
     * @return The version.
     */
    public java.lang.String getVersion() {
      java.lang.Object ref = version_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        version_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <code>string version = 1 [json_name = "version"];</code>
     * @return The bytes for version.
     */
    public com.google.protobuf.ByteString
        getVersionBytes() {
      java.lang.Object ref = version_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        version_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <code>string version = 1 [json_name = "version"];</code>
     * @param value The version to set.
     * @return This builder for chaining.
     */
    public Builder setVersion(
        java.lang.String value) {
      if (value == null) { throw new NullPointerException(); }
      version_ = value;
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <code>string version = 1 [json_name = "version"];</code>
     * @return This builder for chaining.
     */
    public Builder clearVersion() {
      version_ = getDefaultInstance().getVersion();
      bitField0_ = (bitField0_ & ~0x00000001);
      onChanged();
      return this;
    }
    /**
     * <code>string version = 1 [json_name = "version"];</code>
     * @param value The bytes for version to set.
     * @return This builder for chaining.
     */
    public Builder setVersionBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) { throw new NullPointerException(); }
      checkByteStringIsUtf8(value);
      version_ = value;
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }

    private long blockVersion_ ;
    /**
     * <code>uint64 block_version = 2 [json_name = "blockVersion"];</code>
     * @return The blockVersion.
     */
    @java.lang.Override
    public long getBlockVersion() {
      return blockVersion_;
    }
    /**
     * <code>uint64 block_version = 2 [json_name = "blockVersion"];</code>
     * @param value The blockVersion to set.
     * @return This builder for chaining.
     */
    public Builder setBlockVersion(long value) {

      blockVersion_ = value;
      bitField0_ |= 0x00000002;
      onChanged();
      return this;
    }
    /**
     * <code>uint64 block_version = 2 [json_name = "blockVersion"];</code>
     * @return This builder for chaining.
     */
    public Builder clearBlockVersion() {
      bitField0_ = (bitField0_ & ~0x00000002);
      blockVersion_ = 0L;
      onChanged();
      return this;
    }

    private long p2PVersion_ ;
    /**
     * <code>uint64 p2p_version = 3 [json_name = "p2pVersion"];</code>
     * @return The p2pVersion.
     */
    @java.lang.Override
    public long getP2PVersion() {
      return p2PVersion_;
    }
    /**
     * <code>uint64 p2p_version = 3 [json_name = "p2pVersion"];</code>
     * @param value The p2pVersion to set.
     * @return This builder for chaining.
     */
    public Builder setP2PVersion(long value) {

      p2PVersion_ = value;
      bitField0_ |= 0x00000004;
      onChanged();
      return this;
    }
    /**
     * <code>uint64 p2p_version = 3 [json_name = "p2pVersion"];</code>
     * @return This builder for chaining.
     */
    public Builder clearP2PVersion() {
      bitField0_ = (bitField0_ & ~0x00000004);
      p2PVersion_ = 0L;
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


    // @@protoc_insertion_point(builder_scope:tendermint.abci.RequestInfo)
  }

  // @@protoc_insertion_point(class_scope:tendermint.abci.RequestInfo)
  private static final com.tendermint.abci.RequestInfo DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new com.tendermint.abci.RequestInfo();
  }

  public static com.tendermint.abci.RequestInfo getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<RequestInfo>
      PARSER = new com.google.protobuf.AbstractParser<RequestInfo>() {
    @java.lang.Override
    public RequestInfo parsePartialFrom(
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

  public static com.google.protobuf.Parser<RequestInfo> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<RequestInfo> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public com.tendermint.abci.RequestInfo getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

