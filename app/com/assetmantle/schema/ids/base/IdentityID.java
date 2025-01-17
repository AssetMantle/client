// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: ids/base/identity_id.proto

package com.assetmantle.schema.ids.base;

/**
 * Protobuf type {@code assetmantle.schema.ids.base.IdentityID}
 */
public final class IdentityID extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:assetmantle.schema.ids.base.IdentityID)
    IdentityIDOrBuilder {
private static final long serialVersionUID = 0L;
  // Use IdentityID.newBuilder() to construct.
  private IdentityID(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private IdentityID() {
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new IdentityID();
  }

  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return com.assetmantle.schema.ids.base.IdentityIdProto.internal_static_assetmantle_schema_ids_base_IdentityID_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return com.assetmantle.schema.ids.base.IdentityIdProto.internal_static_assetmantle_schema_ids_base_IdentityID_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            com.assetmantle.schema.ids.base.IdentityID.class, com.assetmantle.schema.ids.base.IdentityID.Builder.class);
  }

  public static final int HASH_I_D_FIELD_NUMBER = 1;
  private com.assetmantle.schema.ids.base.HashID hashID_;
  /**
   * <code>.assetmantle.schema.ids.base.HashID hash_i_d = 1 [json_name = "hashID"];</code>
   * @return Whether the hashID field is set.
   */
  @java.lang.Override
  public boolean hasHashID() {
    return hashID_ != null;
  }
  /**
   * <code>.assetmantle.schema.ids.base.HashID hash_i_d = 1 [json_name = "hashID"];</code>
   * @return The hashID.
   */
  @java.lang.Override
  public com.assetmantle.schema.ids.base.HashID getHashID() {
    return hashID_ == null ? com.assetmantle.schema.ids.base.HashID.getDefaultInstance() : hashID_;
  }
  /**
   * <code>.assetmantle.schema.ids.base.HashID hash_i_d = 1 [json_name = "hashID"];</code>
   */
  @java.lang.Override
  public com.assetmantle.schema.ids.base.HashIDOrBuilder getHashIDOrBuilder() {
    return hashID_ == null ? com.assetmantle.schema.ids.base.HashID.getDefaultInstance() : hashID_;
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
    if (hashID_ != null) {
      output.writeMessage(1, getHashID());
    }
    getUnknownFields().writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (hashID_ != null) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(1, getHashID());
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
    if (!(obj instanceof com.assetmantle.schema.ids.base.IdentityID)) {
      return super.equals(obj);
    }
    com.assetmantle.schema.ids.base.IdentityID other = (com.assetmantle.schema.ids.base.IdentityID) obj;

    if (hasHashID() != other.hasHashID()) return false;
    if (hasHashID()) {
      if (!getHashID()
          .equals(other.getHashID())) return false;
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
    if (hasHashID()) {
      hash = (37 * hash) + HASH_I_D_FIELD_NUMBER;
      hash = (53 * hash) + getHashID().hashCode();
    }
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static com.assetmantle.schema.ids.base.IdentityID parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.assetmantle.schema.ids.base.IdentityID parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.assetmantle.schema.ids.base.IdentityID parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.assetmantle.schema.ids.base.IdentityID parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.assetmantle.schema.ids.base.IdentityID parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.assetmantle.schema.ids.base.IdentityID parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.assetmantle.schema.ids.base.IdentityID parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.assetmantle.schema.ids.base.IdentityID parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.assetmantle.schema.ids.base.IdentityID parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static com.assetmantle.schema.ids.base.IdentityID parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.assetmantle.schema.ids.base.IdentityID parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.assetmantle.schema.ids.base.IdentityID parseFrom(
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
  public static Builder newBuilder(com.assetmantle.schema.ids.base.IdentityID prototype) {
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
   * Protobuf type {@code assetmantle.schema.ids.base.IdentityID}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:assetmantle.schema.ids.base.IdentityID)
      com.assetmantle.schema.ids.base.IdentityIDOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return com.assetmantle.schema.ids.base.IdentityIdProto.internal_static_assetmantle_schema_ids_base_IdentityID_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return com.assetmantle.schema.ids.base.IdentityIdProto.internal_static_assetmantle_schema_ids_base_IdentityID_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              com.assetmantle.schema.ids.base.IdentityID.class, com.assetmantle.schema.ids.base.IdentityID.Builder.class);
    }

    // Construct using com.assetmantle.schema.ids.base.IdentityID.newBuilder()
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
      hashID_ = null;
      if (hashIDBuilder_ != null) {
        hashIDBuilder_.dispose();
        hashIDBuilder_ = null;
      }
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return com.assetmantle.schema.ids.base.IdentityIdProto.internal_static_assetmantle_schema_ids_base_IdentityID_descriptor;
    }

    @java.lang.Override
    public com.assetmantle.schema.ids.base.IdentityID getDefaultInstanceForType() {
      return com.assetmantle.schema.ids.base.IdentityID.getDefaultInstance();
    }

    @java.lang.Override
    public com.assetmantle.schema.ids.base.IdentityID build() {
      com.assetmantle.schema.ids.base.IdentityID result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public com.assetmantle.schema.ids.base.IdentityID buildPartial() {
      com.assetmantle.schema.ids.base.IdentityID result = new com.assetmantle.schema.ids.base.IdentityID(this);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartial0(com.assetmantle.schema.ids.base.IdentityID result) {
      int from_bitField0_ = bitField0_;
      if (((from_bitField0_ & 0x00000001) != 0)) {
        result.hashID_ = hashIDBuilder_ == null
            ? hashID_
            : hashIDBuilder_.build();
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
      if (other instanceof com.assetmantle.schema.ids.base.IdentityID) {
        return mergeFrom((com.assetmantle.schema.ids.base.IdentityID)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(com.assetmantle.schema.ids.base.IdentityID other) {
      if (other == com.assetmantle.schema.ids.base.IdentityID.getDefaultInstance()) return this;
      if (other.hasHashID()) {
        mergeHashID(other.getHashID());
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
                  getHashIDFieldBuilder().getBuilder(),
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

    private com.assetmantle.schema.ids.base.HashID hashID_;
    private com.google.protobuf.SingleFieldBuilderV3<
        com.assetmantle.schema.ids.base.HashID, com.assetmantle.schema.ids.base.HashID.Builder, com.assetmantle.schema.ids.base.HashIDOrBuilder> hashIDBuilder_;
    /**
     * <code>.assetmantle.schema.ids.base.HashID hash_i_d = 1 [json_name = "hashID"];</code>
     * @return Whether the hashID field is set.
     */
    public boolean hasHashID() {
      return ((bitField0_ & 0x00000001) != 0);
    }
    /**
     * <code>.assetmantle.schema.ids.base.HashID hash_i_d = 1 [json_name = "hashID"];</code>
     * @return The hashID.
     */
    public com.assetmantle.schema.ids.base.HashID getHashID() {
      if (hashIDBuilder_ == null) {
        return hashID_ == null ? com.assetmantle.schema.ids.base.HashID.getDefaultInstance() : hashID_;
      } else {
        return hashIDBuilder_.getMessage();
      }
    }
    /**
     * <code>.assetmantle.schema.ids.base.HashID hash_i_d = 1 [json_name = "hashID"];</code>
     */
    public Builder setHashID(com.assetmantle.schema.ids.base.HashID value) {
      if (hashIDBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        hashID_ = value;
      } else {
        hashIDBuilder_.setMessage(value);
      }
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <code>.assetmantle.schema.ids.base.HashID hash_i_d = 1 [json_name = "hashID"];</code>
     */
    public Builder setHashID(
        com.assetmantle.schema.ids.base.HashID.Builder builderForValue) {
      if (hashIDBuilder_ == null) {
        hashID_ = builderForValue.build();
      } else {
        hashIDBuilder_.setMessage(builderForValue.build());
      }
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <code>.assetmantle.schema.ids.base.HashID hash_i_d = 1 [json_name = "hashID"];</code>
     */
    public Builder mergeHashID(com.assetmantle.schema.ids.base.HashID value) {
      if (hashIDBuilder_ == null) {
        if (((bitField0_ & 0x00000001) != 0) &&
          hashID_ != null &&
          hashID_ != com.assetmantle.schema.ids.base.HashID.getDefaultInstance()) {
          getHashIDBuilder().mergeFrom(value);
        } else {
          hashID_ = value;
        }
      } else {
        hashIDBuilder_.mergeFrom(value);
      }
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <code>.assetmantle.schema.ids.base.HashID hash_i_d = 1 [json_name = "hashID"];</code>
     */
    public Builder clearHashID() {
      bitField0_ = (bitField0_ & ~0x00000001);
      hashID_ = null;
      if (hashIDBuilder_ != null) {
        hashIDBuilder_.dispose();
        hashIDBuilder_ = null;
      }
      onChanged();
      return this;
    }
    /**
     * <code>.assetmantle.schema.ids.base.HashID hash_i_d = 1 [json_name = "hashID"];</code>
     */
    public com.assetmantle.schema.ids.base.HashID.Builder getHashIDBuilder() {
      bitField0_ |= 0x00000001;
      onChanged();
      return getHashIDFieldBuilder().getBuilder();
    }
    /**
     * <code>.assetmantle.schema.ids.base.HashID hash_i_d = 1 [json_name = "hashID"];</code>
     */
    public com.assetmantle.schema.ids.base.HashIDOrBuilder getHashIDOrBuilder() {
      if (hashIDBuilder_ != null) {
        return hashIDBuilder_.getMessageOrBuilder();
      } else {
        return hashID_ == null ?
            com.assetmantle.schema.ids.base.HashID.getDefaultInstance() : hashID_;
      }
    }
    /**
     * <code>.assetmantle.schema.ids.base.HashID hash_i_d = 1 [json_name = "hashID"];</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
        com.assetmantle.schema.ids.base.HashID, com.assetmantle.schema.ids.base.HashID.Builder, com.assetmantle.schema.ids.base.HashIDOrBuilder> 
        getHashIDFieldBuilder() {
      if (hashIDBuilder_ == null) {
        hashIDBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
            com.assetmantle.schema.ids.base.HashID, com.assetmantle.schema.ids.base.HashID.Builder, com.assetmantle.schema.ids.base.HashIDOrBuilder>(
                getHashID(),
                getParentForChildren(),
                isClean());
        hashID_ = null;
      }
      return hashIDBuilder_;
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


    // @@protoc_insertion_point(builder_scope:assetmantle.schema.ids.base.IdentityID)
  }

  // @@protoc_insertion_point(class_scope:assetmantle.schema.ids.base.IdentityID)
  private static final com.assetmantle.schema.ids.base.IdentityID DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new com.assetmantle.schema.ids.base.IdentityID();
  }

  public static com.assetmantle.schema.ids.base.IdentityID getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<IdentityID>
      PARSER = new com.google.protobuf.AbstractParser<IdentityID>() {
    @java.lang.Override
    public IdentityID parsePartialFrom(
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

  public static com.google.protobuf.Parser<IdentityID> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<IdentityID> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public com.assetmantle.schema.ids.base.IdentityID getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

