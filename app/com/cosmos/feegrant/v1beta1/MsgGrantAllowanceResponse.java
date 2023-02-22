// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: cosmos/feegrant/v1beta1/tx.proto

package com.cosmos.feegrant.v1beta1;

/**
 * <pre>
 * MsgGrantAllowanceResponse defines the Msg/GrantAllowanceResponse response type.
 * </pre>
 *
 * Protobuf type {@code cosmos.feegrant.v1beta1.MsgGrantAllowanceResponse}
 */
public final class MsgGrantAllowanceResponse extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:cosmos.feegrant.v1beta1.MsgGrantAllowanceResponse)
    MsgGrantAllowanceResponseOrBuilder {
private static final long serialVersionUID = 0L;
  // Use MsgGrantAllowanceResponse.newBuilder() to construct.
  private MsgGrantAllowanceResponse(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private MsgGrantAllowanceResponse() {
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new MsgGrantAllowanceResponse();
  }

  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return com.cosmos.feegrant.v1beta1.TxProto.internal_static_cosmos_feegrant_v1beta1_MsgGrantAllowanceResponse_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return com.cosmos.feegrant.v1beta1.TxProto.internal_static_cosmos_feegrant_v1beta1_MsgGrantAllowanceResponse_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            com.cosmos.feegrant.v1beta1.MsgGrantAllowanceResponse.class, com.cosmos.feegrant.v1beta1.MsgGrantAllowanceResponse.Builder.class);
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
    getUnknownFields().writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    size += getUnknownFields().getSerializedSize();
    memoizedSize = size;
    return size;
  }

  @java.lang.Override
  public boolean equals(final java.lang.Object obj) {
    if (obj == this) {
     return true;
    }
    if (!(obj instanceof com.cosmos.feegrant.v1beta1.MsgGrantAllowanceResponse)) {
      return super.equals(obj);
    }
    com.cosmos.feegrant.v1beta1.MsgGrantAllowanceResponse other = (com.cosmos.feegrant.v1beta1.MsgGrantAllowanceResponse) obj;

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
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static com.cosmos.feegrant.v1beta1.MsgGrantAllowanceResponse parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.cosmos.feegrant.v1beta1.MsgGrantAllowanceResponse parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.cosmos.feegrant.v1beta1.MsgGrantAllowanceResponse parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.cosmos.feegrant.v1beta1.MsgGrantAllowanceResponse parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.cosmos.feegrant.v1beta1.MsgGrantAllowanceResponse parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.cosmos.feegrant.v1beta1.MsgGrantAllowanceResponse parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.cosmos.feegrant.v1beta1.MsgGrantAllowanceResponse parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.cosmos.feegrant.v1beta1.MsgGrantAllowanceResponse parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.cosmos.feegrant.v1beta1.MsgGrantAllowanceResponse parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static com.cosmos.feegrant.v1beta1.MsgGrantAllowanceResponse parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.cosmos.feegrant.v1beta1.MsgGrantAllowanceResponse parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.cosmos.feegrant.v1beta1.MsgGrantAllowanceResponse parseFrom(
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
  public static Builder newBuilder(com.cosmos.feegrant.v1beta1.MsgGrantAllowanceResponse prototype) {
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
   * MsgGrantAllowanceResponse defines the Msg/GrantAllowanceResponse response type.
   * </pre>
   *
   * Protobuf type {@code cosmos.feegrant.v1beta1.MsgGrantAllowanceResponse}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:cosmos.feegrant.v1beta1.MsgGrantAllowanceResponse)
      com.cosmos.feegrant.v1beta1.MsgGrantAllowanceResponseOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return com.cosmos.feegrant.v1beta1.TxProto.internal_static_cosmos_feegrant_v1beta1_MsgGrantAllowanceResponse_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return com.cosmos.feegrant.v1beta1.TxProto.internal_static_cosmos_feegrant_v1beta1_MsgGrantAllowanceResponse_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              com.cosmos.feegrant.v1beta1.MsgGrantAllowanceResponse.class, com.cosmos.feegrant.v1beta1.MsgGrantAllowanceResponse.Builder.class);
    }

    // Construct using com.cosmos.feegrant.v1beta1.MsgGrantAllowanceResponse.newBuilder()
    private Builder() {

    }

    private Builder(
        com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
      super(parent);

    }
    @java.lang.Override
    public Builder clear() {
      super.clear();
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return com.cosmos.feegrant.v1beta1.TxProto.internal_static_cosmos_feegrant_v1beta1_MsgGrantAllowanceResponse_descriptor;
    }

    @java.lang.Override
    public com.cosmos.feegrant.v1beta1.MsgGrantAllowanceResponse getDefaultInstanceForType() {
      return com.cosmos.feegrant.v1beta1.MsgGrantAllowanceResponse.getDefaultInstance();
    }

    @java.lang.Override
    public com.cosmos.feegrant.v1beta1.MsgGrantAllowanceResponse build() {
      com.cosmos.feegrant.v1beta1.MsgGrantAllowanceResponse result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public com.cosmos.feegrant.v1beta1.MsgGrantAllowanceResponse buildPartial() {
      com.cosmos.feegrant.v1beta1.MsgGrantAllowanceResponse result = new com.cosmos.feegrant.v1beta1.MsgGrantAllowanceResponse(this);
      onBuilt();
      return result;
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
      if (other instanceof com.cosmos.feegrant.v1beta1.MsgGrantAllowanceResponse) {
        return mergeFrom((com.cosmos.feegrant.v1beta1.MsgGrantAllowanceResponse)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(com.cosmos.feegrant.v1beta1.MsgGrantAllowanceResponse other) {
      if (other == com.cosmos.feegrant.v1beta1.MsgGrantAllowanceResponse.getDefaultInstance()) return this;
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


    // @@protoc_insertion_point(builder_scope:cosmos.feegrant.v1beta1.MsgGrantAllowanceResponse)
  }

  // @@protoc_insertion_point(class_scope:cosmos.feegrant.v1beta1.MsgGrantAllowanceResponse)
  private static final com.cosmos.feegrant.v1beta1.MsgGrantAllowanceResponse DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new com.cosmos.feegrant.v1beta1.MsgGrantAllowanceResponse();
  }

  public static com.cosmos.feegrant.v1beta1.MsgGrantAllowanceResponse getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<MsgGrantAllowanceResponse>
      PARSER = new com.google.protobuf.AbstractParser<MsgGrantAllowanceResponse>() {
    @java.lang.Override
    public MsgGrantAllowanceResponse parsePartialFrom(
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

  public static com.google.protobuf.Parser<MsgGrantAllowanceResponse> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<MsgGrantAllowanceResponse> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public com.cosmos.feegrant.v1beta1.MsgGrantAllowanceResponse getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

