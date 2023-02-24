// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: cosmos/slashing/v1beta1/query.proto

package com.cosmos.slashing.v1beta1;

/**
 * <pre>
 * QuerySigningInfoRequest is the request type for the Query/SigningInfo RPC
 * method
 * </pre>
 *
 * Protobuf type {@code cosmos.slashing.v1beta1.QuerySigningInfoRequest}
 */
public final class QuerySigningInfoRequest extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:cosmos.slashing.v1beta1.QuerySigningInfoRequest)
    QuerySigningInfoRequestOrBuilder {
private static final long serialVersionUID = 0L;
  // Use QuerySigningInfoRequest.newBuilder() to construct.
  private QuerySigningInfoRequest(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private QuerySigningInfoRequest() {
    consAddress_ = "";
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new QuerySigningInfoRequest();
  }

  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return com.cosmos.slashing.v1beta1.QueryProto.internal_static_cosmos_slashing_v1beta1_QuerySigningInfoRequest_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return com.cosmos.slashing.v1beta1.QueryProto.internal_static_cosmos_slashing_v1beta1_QuerySigningInfoRequest_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            com.cosmos.slashing.v1beta1.QuerySigningInfoRequest.class, com.cosmos.slashing.v1beta1.QuerySigningInfoRequest.Builder.class);
  }

  public static final int CONS_ADDRESS_FIELD_NUMBER = 1;
  @SuppressWarnings("serial")
  private volatile java.lang.Object consAddress_ = "";
  /**
   * <pre>
   * cons_address is the address to query signing info of
   * </pre>
   *
   * <code>string cons_address = 1 [json_name = "consAddress"];</code>
   * @return The consAddress.
   */
  @java.lang.Override
  public java.lang.String getConsAddress() {
    java.lang.Object ref = consAddress_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      consAddress_ = s;
      return s;
    }
  }
  /**
   * <pre>
   * cons_address is the address to query signing info of
   * </pre>
   *
   * <code>string cons_address = 1 [json_name = "consAddress"];</code>
   * @return The bytes for consAddress.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getConsAddressBytes() {
    java.lang.Object ref = consAddress_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      consAddress_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
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
    if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(consAddress_)) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 1, consAddress_);
    }
    getUnknownFields().writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(consAddress_)) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(1, consAddress_);
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
    if (!(obj instanceof com.cosmos.slashing.v1beta1.QuerySigningInfoRequest)) {
      return super.equals(obj);
    }
    com.cosmos.slashing.v1beta1.QuerySigningInfoRequest other = (com.cosmos.slashing.v1beta1.QuerySigningInfoRequest) obj;

    if (!getConsAddress()
        .equals(other.getConsAddress())) return false;
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
    hash = (37 * hash) + CONS_ADDRESS_FIELD_NUMBER;
    hash = (53 * hash) + getConsAddress().hashCode();
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static com.cosmos.slashing.v1beta1.QuerySigningInfoRequest parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.cosmos.slashing.v1beta1.QuerySigningInfoRequest parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.cosmos.slashing.v1beta1.QuerySigningInfoRequest parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.cosmos.slashing.v1beta1.QuerySigningInfoRequest parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.cosmos.slashing.v1beta1.QuerySigningInfoRequest parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.cosmos.slashing.v1beta1.QuerySigningInfoRequest parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.cosmos.slashing.v1beta1.QuerySigningInfoRequest parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.cosmos.slashing.v1beta1.QuerySigningInfoRequest parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.cosmos.slashing.v1beta1.QuerySigningInfoRequest parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static com.cosmos.slashing.v1beta1.QuerySigningInfoRequest parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.cosmos.slashing.v1beta1.QuerySigningInfoRequest parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.cosmos.slashing.v1beta1.QuerySigningInfoRequest parseFrom(
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
  public static Builder newBuilder(com.cosmos.slashing.v1beta1.QuerySigningInfoRequest prototype) {
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
   * QuerySigningInfoRequest is the request type for the Query/SigningInfo RPC
   * method
   * </pre>
   *
   * Protobuf type {@code cosmos.slashing.v1beta1.QuerySigningInfoRequest}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:cosmos.slashing.v1beta1.QuerySigningInfoRequest)
      com.cosmos.slashing.v1beta1.QuerySigningInfoRequestOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return com.cosmos.slashing.v1beta1.QueryProto.internal_static_cosmos_slashing_v1beta1_QuerySigningInfoRequest_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return com.cosmos.slashing.v1beta1.QueryProto.internal_static_cosmos_slashing_v1beta1_QuerySigningInfoRequest_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              com.cosmos.slashing.v1beta1.QuerySigningInfoRequest.class, com.cosmos.slashing.v1beta1.QuerySigningInfoRequest.Builder.class);
    }

    // Construct using com.cosmos.slashing.v1beta1.QuerySigningInfoRequest.newBuilder()
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
      consAddress_ = "";
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return com.cosmos.slashing.v1beta1.QueryProto.internal_static_cosmos_slashing_v1beta1_QuerySigningInfoRequest_descriptor;
    }

    @java.lang.Override
    public com.cosmos.slashing.v1beta1.QuerySigningInfoRequest getDefaultInstanceForType() {
      return com.cosmos.slashing.v1beta1.QuerySigningInfoRequest.getDefaultInstance();
    }

    @java.lang.Override
    public com.cosmos.slashing.v1beta1.QuerySigningInfoRequest build() {
      com.cosmos.slashing.v1beta1.QuerySigningInfoRequest result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public com.cosmos.slashing.v1beta1.QuerySigningInfoRequest buildPartial() {
      com.cosmos.slashing.v1beta1.QuerySigningInfoRequest result = new com.cosmos.slashing.v1beta1.QuerySigningInfoRequest(this);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartial0(com.cosmos.slashing.v1beta1.QuerySigningInfoRequest result) {
      int from_bitField0_ = bitField0_;
      if (((from_bitField0_ & 0x00000001) != 0)) {
        result.consAddress_ = consAddress_;
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
      if (other instanceof com.cosmos.slashing.v1beta1.QuerySigningInfoRequest) {
        return mergeFrom((com.cosmos.slashing.v1beta1.QuerySigningInfoRequest)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(com.cosmos.slashing.v1beta1.QuerySigningInfoRequest other) {
      if (other == com.cosmos.slashing.v1beta1.QuerySigningInfoRequest.getDefaultInstance()) return this;
      if (!other.getConsAddress().isEmpty()) {
        consAddress_ = other.consAddress_;
        bitField0_ |= 0x00000001;
        onChanged();
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
              consAddress_ = input.readStringRequireUtf8();
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

    private java.lang.Object consAddress_ = "";
    /**
     * <pre>
     * cons_address is the address to query signing info of
     * </pre>
     *
     * <code>string cons_address = 1 [json_name = "consAddress"];</code>
     * @return The consAddress.
     */
    public java.lang.String getConsAddress() {
      java.lang.Object ref = consAddress_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        consAddress_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <pre>
     * cons_address is the address to query signing info of
     * </pre>
     *
     * <code>string cons_address = 1 [json_name = "consAddress"];</code>
     * @return The bytes for consAddress.
     */
    public com.google.protobuf.ByteString
        getConsAddressBytes() {
      java.lang.Object ref = consAddress_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        consAddress_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <pre>
     * cons_address is the address to query signing info of
     * </pre>
     *
     * <code>string cons_address = 1 [json_name = "consAddress"];</code>
     * @param value The consAddress to set.
     * @return This builder for chaining.
     */
    public Builder setConsAddress(
        java.lang.String value) {
      if (value == null) { throw new NullPointerException(); }
      consAddress_ = value;
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * cons_address is the address to query signing info of
     * </pre>
     *
     * <code>string cons_address = 1 [json_name = "consAddress"];</code>
     * @return This builder for chaining.
     */
    public Builder clearConsAddress() {
      consAddress_ = getDefaultInstance().getConsAddress();
      bitField0_ = (bitField0_ & ~0x00000001);
      onChanged();
      return this;
    }
    /**
     * <pre>
     * cons_address is the address to query signing info of
     * </pre>
     *
     * <code>string cons_address = 1 [json_name = "consAddress"];</code>
     * @param value The bytes for consAddress to set.
     * @return This builder for chaining.
     */
    public Builder setConsAddressBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) { throw new NullPointerException(); }
      checkByteStringIsUtf8(value);
      consAddress_ = value;
      bitField0_ |= 0x00000001;
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


    // @@protoc_insertion_point(builder_scope:cosmos.slashing.v1beta1.QuerySigningInfoRequest)
  }

  // @@protoc_insertion_point(class_scope:cosmos.slashing.v1beta1.QuerySigningInfoRequest)
  private static final com.cosmos.slashing.v1beta1.QuerySigningInfoRequest DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new com.cosmos.slashing.v1beta1.QuerySigningInfoRequest();
  }

  public static com.cosmos.slashing.v1beta1.QuerySigningInfoRequest getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<QuerySigningInfoRequest>
      PARSER = new com.google.protobuf.AbstractParser<QuerySigningInfoRequest>() {
    @java.lang.Override
    public QuerySigningInfoRequest parsePartialFrom(
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

  public static com.google.protobuf.Parser<QuerySigningInfoRequest> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<QuerySigningInfoRequest> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public com.cosmos.slashing.v1beta1.QuerySigningInfoRequest getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}
