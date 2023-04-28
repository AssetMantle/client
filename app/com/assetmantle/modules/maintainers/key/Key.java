// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: maintainers/key/key.proto

package com.assetmantle.modules.maintainers.key;

/**
 * Protobuf type {@code assetmantle.modules.maintainers.key.Key}
 */
public final class Key extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:assetmantle.modules.maintainers.key.Key)
    KeyOrBuilder {
private static final long serialVersionUID = 0L;
  // Use Key.newBuilder() to construct.
  private Key(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private Key() {
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new Key();
  }

  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return com.assetmantle.modules.maintainers.key.KeyProto.internal_static_assetmantle_modules_maintainers_key_Key_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return com.assetmantle.modules.maintainers.key.KeyProto.internal_static_assetmantle_modules_maintainers_key_Key_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            com.assetmantle.modules.maintainers.key.Key.class, com.assetmantle.modules.maintainers.key.Key.Builder.class);
  }

  public static final int MAINTAINER_I_D_FIELD_NUMBER = 1;
  private com.assetmantle.schema.ids.base.MaintainerID maintainerID_;
  /**
   * <code>.assetmantle.schema.ids.base.MaintainerID maintainer_i_d = 1 [json_name = "maintainerID"];</code>
   * @return Whether the maintainerID field is set.
   */
  @java.lang.Override
  public boolean hasMaintainerID() {
    return maintainerID_ != null;
  }
  /**
   * <code>.assetmantle.schema.ids.base.MaintainerID maintainer_i_d = 1 [json_name = "maintainerID"];</code>
   * @return The maintainerID.
   */
  @java.lang.Override
  public com.assetmantle.schema.ids.base.MaintainerID getMaintainerID() {
    return maintainerID_ == null ? com.assetmantle.schema.ids.base.MaintainerID.getDefaultInstance() : maintainerID_;
  }
  /**
   * <code>.assetmantle.schema.ids.base.MaintainerID maintainer_i_d = 1 [json_name = "maintainerID"];</code>
   */
  @java.lang.Override
  public com.assetmantle.schema.ids.base.MaintainerIDOrBuilder getMaintainerIDOrBuilder() {
    return maintainerID_ == null ? com.assetmantle.schema.ids.base.MaintainerID.getDefaultInstance() : maintainerID_;
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
    if (maintainerID_ != null) {
      output.writeMessage(1, getMaintainerID());
    }
    getUnknownFields().writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (maintainerID_ != null) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(1, getMaintainerID());
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
    if (!(obj instanceof com.assetmantle.modules.maintainers.key.Key)) {
      return super.equals(obj);
    }
    com.assetmantle.modules.maintainers.key.Key other = (com.assetmantle.modules.maintainers.key.Key) obj;

    if (hasMaintainerID() != other.hasMaintainerID()) return false;
    if (hasMaintainerID()) {
      if (!getMaintainerID()
          .equals(other.getMaintainerID())) return false;
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
    if (hasMaintainerID()) {
      hash = (37 * hash) + MAINTAINER_I_D_FIELD_NUMBER;
      hash = (53 * hash) + getMaintainerID().hashCode();
    }
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static com.assetmantle.modules.maintainers.key.Key parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.assetmantle.modules.maintainers.key.Key parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.assetmantle.modules.maintainers.key.Key parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.assetmantle.modules.maintainers.key.Key parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.assetmantle.modules.maintainers.key.Key parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.assetmantle.modules.maintainers.key.Key parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.assetmantle.modules.maintainers.key.Key parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.assetmantle.modules.maintainers.key.Key parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.assetmantle.modules.maintainers.key.Key parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static com.assetmantle.modules.maintainers.key.Key parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.assetmantle.modules.maintainers.key.Key parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.assetmantle.modules.maintainers.key.Key parseFrom(
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
  public static Builder newBuilder(com.assetmantle.modules.maintainers.key.Key prototype) {
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
   * Protobuf type {@code assetmantle.modules.maintainers.key.Key}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:assetmantle.modules.maintainers.key.Key)
      com.assetmantle.modules.maintainers.key.KeyOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return com.assetmantle.modules.maintainers.key.KeyProto.internal_static_assetmantle_modules_maintainers_key_Key_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return com.assetmantle.modules.maintainers.key.KeyProto.internal_static_assetmantle_modules_maintainers_key_Key_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              com.assetmantle.modules.maintainers.key.Key.class, com.assetmantle.modules.maintainers.key.Key.Builder.class);
    }

    // Construct using com.assetmantle.modules.maintainers.key.Key.newBuilder()
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
      maintainerID_ = null;
      if (maintainerIDBuilder_ != null) {
        maintainerIDBuilder_.dispose();
        maintainerIDBuilder_ = null;
      }
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return com.assetmantle.modules.maintainers.key.KeyProto.internal_static_assetmantle_modules_maintainers_key_Key_descriptor;
    }

    @java.lang.Override
    public com.assetmantle.modules.maintainers.key.Key getDefaultInstanceForType() {
      return com.assetmantle.modules.maintainers.key.Key.getDefaultInstance();
    }

    @java.lang.Override
    public com.assetmantle.modules.maintainers.key.Key build() {
      com.assetmantle.modules.maintainers.key.Key result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public com.assetmantle.modules.maintainers.key.Key buildPartial() {
      com.assetmantle.modules.maintainers.key.Key result = new com.assetmantle.modules.maintainers.key.Key(this);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartial0(com.assetmantle.modules.maintainers.key.Key result) {
      int from_bitField0_ = bitField0_;
      if (((from_bitField0_ & 0x00000001) != 0)) {
        result.maintainerID_ = maintainerIDBuilder_ == null
            ? maintainerID_
            : maintainerIDBuilder_.build();
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
      if (other instanceof com.assetmantle.modules.maintainers.key.Key) {
        return mergeFrom((com.assetmantle.modules.maintainers.key.Key)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(com.assetmantle.modules.maintainers.key.Key other) {
      if (other == com.assetmantle.modules.maintainers.key.Key.getDefaultInstance()) return this;
      if (other.hasMaintainerID()) {
        mergeMaintainerID(other.getMaintainerID());
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
                  getMaintainerIDFieldBuilder().getBuilder(),
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

    private com.assetmantle.schema.ids.base.MaintainerID maintainerID_;
    private com.google.protobuf.SingleFieldBuilderV3<
        com.assetmantle.schema.ids.base.MaintainerID, com.assetmantle.schema.ids.base.MaintainerID.Builder, com.assetmantle.schema.ids.base.MaintainerIDOrBuilder> maintainerIDBuilder_;
    /**
     * <code>.assetmantle.schema.ids.base.MaintainerID maintainer_i_d = 1 [json_name = "maintainerID"];</code>
     * @return Whether the maintainerID field is set.
     */
    public boolean hasMaintainerID() {
      return ((bitField0_ & 0x00000001) != 0);
    }
    /**
     * <code>.assetmantle.schema.ids.base.MaintainerID maintainer_i_d = 1 [json_name = "maintainerID"];</code>
     * @return The maintainerID.
     */
    public com.assetmantle.schema.ids.base.MaintainerID getMaintainerID() {
      if (maintainerIDBuilder_ == null) {
        return maintainerID_ == null ? com.assetmantle.schema.ids.base.MaintainerID.getDefaultInstance() : maintainerID_;
      } else {
        return maintainerIDBuilder_.getMessage();
      }
    }
    /**
     * <code>.assetmantle.schema.ids.base.MaintainerID maintainer_i_d = 1 [json_name = "maintainerID"];</code>
     */
    public Builder setMaintainerID(com.assetmantle.schema.ids.base.MaintainerID value) {
      if (maintainerIDBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        maintainerID_ = value;
      } else {
        maintainerIDBuilder_.setMessage(value);
      }
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <code>.assetmantle.schema.ids.base.MaintainerID maintainer_i_d = 1 [json_name = "maintainerID"];</code>
     */
    public Builder setMaintainerID(
        com.assetmantle.schema.ids.base.MaintainerID.Builder builderForValue) {
      if (maintainerIDBuilder_ == null) {
        maintainerID_ = builderForValue.build();
      } else {
        maintainerIDBuilder_.setMessage(builderForValue.build());
      }
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <code>.assetmantle.schema.ids.base.MaintainerID maintainer_i_d = 1 [json_name = "maintainerID"];</code>
     */
    public Builder mergeMaintainerID(com.assetmantle.schema.ids.base.MaintainerID value) {
      if (maintainerIDBuilder_ == null) {
        if (((bitField0_ & 0x00000001) != 0) &&
          maintainerID_ != null &&
          maintainerID_ != com.assetmantle.schema.ids.base.MaintainerID.getDefaultInstance()) {
          getMaintainerIDBuilder().mergeFrom(value);
        } else {
          maintainerID_ = value;
        }
      } else {
        maintainerIDBuilder_.mergeFrom(value);
      }
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <code>.assetmantle.schema.ids.base.MaintainerID maintainer_i_d = 1 [json_name = "maintainerID"];</code>
     */
    public Builder clearMaintainerID() {
      bitField0_ = (bitField0_ & ~0x00000001);
      maintainerID_ = null;
      if (maintainerIDBuilder_ != null) {
        maintainerIDBuilder_.dispose();
        maintainerIDBuilder_ = null;
      }
      onChanged();
      return this;
    }
    /**
     * <code>.assetmantle.schema.ids.base.MaintainerID maintainer_i_d = 1 [json_name = "maintainerID"];</code>
     */
    public com.assetmantle.schema.ids.base.MaintainerID.Builder getMaintainerIDBuilder() {
      bitField0_ |= 0x00000001;
      onChanged();
      return getMaintainerIDFieldBuilder().getBuilder();
    }
    /**
     * <code>.assetmantle.schema.ids.base.MaintainerID maintainer_i_d = 1 [json_name = "maintainerID"];</code>
     */
    public com.assetmantle.schema.ids.base.MaintainerIDOrBuilder getMaintainerIDOrBuilder() {
      if (maintainerIDBuilder_ != null) {
        return maintainerIDBuilder_.getMessageOrBuilder();
      } else {
        return maintainerID_ == null ?
            com.assetmantle.schema.ids.base.MaintainerID.getDefaultInstance() : maintainerID_;
      }
    }
    /**
     * <code>.assetmantle.schema.ids.base.MaintainerID maintainer_i_d = 1 [json_name = "maintainerID"];</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
        com.assetmantle.schema.ids.base.MaintainerID, com.assetmantle.schema.ids.base.MaintainerID.Builder, com.assetmantle.schema.ids.base.MaintainerIDOrBuilder> 
        getMaintainerIDFieldBuilder() {
      if (maintainerIDBuilder_ == null) {
        maintainerIDBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
            com.assetmantle.schema.ids.base.MaintainerID, com.assetmantle.schema.ids.base.MaintainerID.Builder, com.assetmantle.schema.ids.base.MaintainerIDOrBuilder>(
                getMaintainerID(),
                getParentForChildren(),
                isClean());
        maintainerID_ = null;
      }
      return maintainerIDBuilder_;
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


    // @@protoc_insertion_point(builder_scope:assetmantle.modules.maintainers.key.Key)
  }

  // @@protoc_insertion_point(class_scope:assetmantle.modules.maintainers.key.Key)
  private static final com.assetmantle.modules.maintainers.key.Key DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new com.assetmantle.modules.maintainers.key.Key();
  }

  public static com.assetmantle.modules.maintainers.key.Key getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<Key>
      PARSER = new com.google.protobuf.AbstractParser<Key>() {
    @java.lang.Override
    public Key parsePartialFrom(
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

  public static com.google.protobuf.Parser<Key> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<Key> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public com.assetmantle.modules.maintainers.key.Key getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

