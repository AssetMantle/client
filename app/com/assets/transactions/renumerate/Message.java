// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: modules/assets/internal/transactions/renumerate/message.v1.proto

package com.assets.transactions.renumerate;

/**
 * Protobuf type {@code assets.transactions.renumerate.Message}
 */
public final class Message extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:assets.transactions.renumerate.Message)
    MessageOrBuilder {
private static final long serialVersionUID = 0L;
  // Use Message.newBuilder() to construct.
  private Message(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private Message() {
    from_ = "";
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new Message();
  }

  @java.lang.Override
  public final com.google.protobuf.UnknownFieldSet
  getUnknownFields() {
    return this.unknownFields;
  }
  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return com.assets.transactions.renumerate.MessageV1Proto.internal_static_assets_transactions_renumerate_Message_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return com.assets.transactions.renumerate.MessageV1Proto.internal_static_assets_transactions_renumerate_Message_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            com.assets.transactions.renumerate.Message.class, com.assets.transactions.renumerate.Message.Builder.class);
  }

  public static final int FROM_FIELD_NUMBER = 1;
  private volatile java.lang.Object from_;
  /**
   * <code>string from = 1 [json_name = "from"];</code>
   * @return The from.
   */
  @java.lang.Override
  public java.lang.String getFrom() {
    java.lang.Object ref = from_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      from_ = s;
      return s;
    }
  }
  /**
   * <code>string from = 1 [json_name = "from"];</code>
   * @return The bytes for from.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getFromBytes() {
    java.lang.Object ref = from_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      from_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int FROM_I_D_FIELD_NUMBER = 2;
  private com.ids.IdentityID fromID_;
  /**
   * <code>.ids.IdentityID from_i_d = 2 [json_name = "fromID"];</code>
   * @return Whether the fromID field is set.
   */
  @java.lang.Override
  public boolean hasFromID() {
    return fromID_ != null;
  }
  /**
   * <code>.ids.IdentityID from_i_d = 2 [json_name = "fromID"];</code>
   * @return The fromID.
   */
  @java.lang.Override
  public com.ids.IdentityID getFromID() {
    return fromID_ == null ? com.ids.IdentityID.getDefaultInstance() : fromID_;
  }
  /**
   * <code>.ids.IdentityID from_i_d = 2 [json_name = "fromID"];</code>
   */
  @java.lang.Override
  public com.ids.IdentityIDOrBuilder getFromIDOrBuilder() {
    return getFromID();
  }

  public static final int ASSET_I_D_FIELD_NUMBER = 3;
  private com.ids.AssetID assetID_;
  /**
   * <code>.ids.AssetID asset_i_d = 3 [json_name = "assetID"];</code>
   * @return Whether the assetID field is set.
   */
  @java.lang.Override
  public boolean hasAssetID() {
    return assetID_ != null;
  }
  /**
   * <code>.ids.AssetID asset_i_d = 3 [json_name = "assetID"];</code>
   * @return The assetID.
   */
  @java.lang.Override
  public com.ids.AssetID getAssetID() {
    return assetID_ == null ? com.ids.AssetID.getDefaultInstance() : assetID_;
  }
  /**
   * <code>.ids.AssetID asset_i_d = 3 [json_name = "assetID"];</code>
   */
  @java.lang.Override
  public com.ids.AssetIDOrBuilder getAssetIDOrBuilder() {
    return getAssetID();
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
    if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(from_)) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 1, from_);
    }
    if (fromID_ != null) {
      output.writeMessage(2, getFromID());
    }
    if (assetID_ != null) {
      output.writeMessage(3, getAssetID());
    }
    getUnknownFields().writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(from_)) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(1, from_);
    }
    if (fromID_ != null) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(2, getFromID());
    }
    if (assetID_ != null) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(3, getAssetID());
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
    if (!(obj instanceof com.assets.transactions.renumerate.Message)) {
      return super.equals(obj);
    }
    com.assets.transactions.renumerate.Message other = (com.assets.transactions.renumerate.Message) obj;

    if (!getFrom()
        .equals(other.getFrom())) return false;
    if (hasFromID() != other.hasFromID()) return false;
    if (hasFromID()) {
      if (!getFromID()
          .equals(other.getFromID())) return false;
    }
    if (hasAssetID() != other.hasAssetID()) return false;
    if (hasAssetID()) {
      if (!getAssetID()
          .equals(other.getAssetID())) return false;
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
    hash = (37 * hash) + FROM_FIELD_NUMBER;
    hash = (53 * hash) + getFrom().hashCode();
    if (hasFromID()) {
      hash = (37 * hash) + FROM_I_D_FIELD_NUMBER;
      hash = (53 * hash) + getFromID().hashCode();
    }
    if (hasAssetID()) {
      hash = (37 * hash) + ASSET_I_D_FIELD_NUMBER;
      hash = (53 * hash) + getAssetID().hashCode();
    }
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static com.assets.transactions.renumerate.Message parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.assets.transactions.renumerate.Message parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.assets.transactions.renumerate.Message parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.assets.transactions.renumerate.Message parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.assets.transactions.renumerate.Message parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.assets.transactions.renumerate.Message parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.assets.transactions.renumerate.Message parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.assets.transactions.renumerate.Message parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.assets.transactions.renumerate.Message parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static com.assets.transactions.renumerate.Message parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.assets.transactions.renumerate.Message parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.assets.transactions.renumerate.Message parseFrom(
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
  public static Builder newBuilder(com.assets.transactions.renumerate.Message prototype) {
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
   * Protobuf type {@code assets.transactions.renumerate.Message}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:assets.transactions.renumerate.Message)
      com.assets.transactions.renumerate.MessageOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return com.assets.transactions.renumerate.MessageV1Proto.internal_static_assets_transactions_renumerate_Message_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return com.assets.transactions.renumerate.MessageV1Proto.internal_static_assets_transactions_renumerate_Message_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              com.assets.transactions.renumerate.Message.class, com.assets.transactions.renumerate.Message.Builder.class);
    }

    // Construct using com.assets.transactions.renumerate.Message.newBuilder()
    private Builder() {

    }

    private Builder(
        com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
      super(parent);

    }
    @java.lang.Override
    public Builder clear() {
      super.clear();
      from_ = "";

      if (fromIDBuilder_ == null) {
        fromID_ = null;
      } else {
        fromID_ = null;
        fromIDBuilder_ = null;
      }
      if (assetIDBuilder_ == null) {
        assetID_ = null;
      } else {
        assetID_ = null;
        assetIDBuilder_ = null;
      }
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return com.assets.transactions.renumerate.MessageV1Proto.internal_static_assets_transactions_renumerate_Message_descriptor;
    }

    @java.lang.Override
    public com.assets.transactions.renumerate.Message getDefaultInstanceForType() {
      return com.assets.transactions.renumerate.Message.getDefaultInstance();
    }

    @java.lang.Override
    public com.assets.transactions.renumerate.Message build() {
      com.assets.transactions.renumerate.Message result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public com.assets.transactions.renumerate.Message buildPartial() {
      com.assets.transactions.renumerate.Message result = new com.assets.transactions.renumerate.Message(this);
      result.from_ = from_;
      if (fromIDBuilder_ == null) {
        result.fromID_ = fromID_;
      } else {
        result.fromID_ = fromIDBuilder_.build();
      }
      if (assetIDBuilder_ == null) {
        result.assetID_ = assetID_;
      } else {
        result.assetID_ = assetIDBuilder_.build();
      }
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
      if (other instanceof com.assets.transactions.renumerate.Message) {
        return mergeFrom((com.assets.transactions.renumerate.Message)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(com.assets.transactions.renumerate.Message other) {
      if (other == com.assets.transactions.renumerate.Message.getDefaultInstance()) return this;
      if (!other.getFrom().isEmpty()) {
        from_ = other.from_;
        onChanged();
      }
      if (other.hasFromID()) {
        mergeFromID(other.getFromID());
      }
      if (other.hasAssetID()) {
        mergeAssetID(other.getAssetID());
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
              from_ = input.readStringRequireUtf8();

              break;
            } // case 10
            case 18: {
              input.readMessage(
                  getFromIDFieldBuilder().getBuilder(),
                  extensionRegistry);

              break;
            } // case 18
            case 26: {
              input.readMessage(
                  getAssetIDFieldBuilder().getBuilder(),
                  extensionRegistry);

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

    private java.lang.Object from_ = "";
    /**
     * <code>string from = 1 [json_name = "from"];</code>
     * @return The from.
     */
    public java.lang.String getFrom() {
      java.lang.Object ref = from_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        from_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <code>string from = 1 [json_name = "from"];</code>
     * @return The bytes for from.
     */
    public com.google.protobuf.ByteString
        getFromBytes() {
      java.lang.Object ref = from_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        from_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <code>string from = 1 [json_name = "from"];</code>
     * @param value The from to set.
     * @return This builder for chaining.
     */
    public Builder setFrom(
        java.lang.String value) {
      if (value == null) {
    throw new NullPointerException();
  }
  
      from_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>string from = 1 [json_name = "from"];</code>
     * @return This builder for chaining.
     */
    public Builder clearFrom() {
      
      from_ = getDefaultInstance().getFrom();
      onChanged();
      return this;
    }
    /**
     * <code>string from = 1 [json_name = "from"];</code>
     * @param value The bytes for from to set.
     * @return This builder for chaining.
     */
    public Builder setFromBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) {
    throw new NullPointerException();
  }
  checkByteStringIsUtf8(value);
      
      from_ = value;
      onChanged();
      return this;
    }

    private com.ids.IdentityID fromID_;
    private com.google.protobuf.SingleFieldBuilderV3<
        com.ids.IdentityID, com.ids.IdentityID.Builder, com.ids.IdentityIDOrBuilder> fromIDBuilder_;
    /**
     * <code>.ids.IdentityID from_i_d = 2 [json_name = "fromID"];</code>
     * @return Whether the fromID field is set.
     */
    public boolean hasFromID() {
      return fromIDBuilder_ != null || fromID_ != null;
    }
    /**
     * <code>.ids.IdentityID from_i_d = 2 [json_name = "fromID"];</code>
     * @return The fromID.
     */
    public com.ids.IdentityID getFromID() {
      if (fromIDBuilder_ == null) {
        return fromID_ == null ? com.ids.IdentityID.getDefaultInstance() : fromID_;
      } else {
        return fromIDBuilder_.getMessage();
      }
    }
    /**
     * <code>.ids.IdentityID from_i_d = 2 [json_name = "fromID"];</code>
     */
    public Builder setFromID(com.ids.IdentityID value) {
      if (fromIDBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        fromID_ = value;
        onChanged();
      } else {
        fromIDBuilder_.setMessage(value);
      }

      return this;
    }
    /**
     * <code>.ids.IdentityID from_i_d = 2 [json_name = "fromID"];</code>
     */
    public Builder setFromID(
        com.ids.IdentityID.Builder builderForValue) {
      if (fromIDBuilder_ == null) {
        fromID_ = builderForValue.build();
        onChanged();
      } else {
        fromIDBuilder_.setMessage(builderForValue.build());
      }

      return this;
    }
    /**
     * <code>.ids.IdentityID from_i_d = 2 [json_name = "fromID"];</code>
     */
    public Builder mergeFromID(com.ids.IdentityID value) {
      if (fromIDBuilder_ == null) {
        if (fromID_ != null) {
          fromID_ =
            com.ids.IdentityID.newBuilder(fromID_).mergeFrom(value).buildPartial();
        } else {
          fromID_ = value;
        }
        onChanged();
      } else {
        fromIDBuilder_.mergeFrom(value);
      }

      return this;
    }
    /**
     * <code>.ids.IdentityID from_i_d = 2 [json_name = "fromID"];</code>
     */
    public Builder clearFromID() {
      if (fromIDBuilder_ == null) {
        fromID_ = null;
        onChanged();
      } else {
        fromID_ = null;
        fromIDBuilder_ = null;
      }

      return this;
    }
    /**
     * <code>.ids.IdentityID from_i_d = 2 [json_name = "fromID"];</code>
     */
    public com.ids.IdentityID.Builder getFromIDBuilder() {
      
      onChanged();
      return getFromIDFieldBuilder().getBuilder();
    }
    /**
     * <code>.ids.IdentityID from_i_d = 2 [json_name = "fromID"];</code>
     */
    public com.ids.IdentityIDOrBuilder getFromIDOrBuilder() {
      if (fromIDBuilder_ != null) {
        return fromIDBuilder_.getMessageOrBuilder();
      } else {
        return fromID_ == null ?
            com.ids.IdentityID.getDefaultInstance() : fromID_;
      }
    }
    /**
     * <code>.ids.IdentityID from_i_d = 2 [json_name = "fromID"];</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
        com.ids.IdentityID, com.ids.IdentityID.Builder, com.ids.IdentityIDOrBuilder> 
        getFromIDFieldBuilder() {
      if (fromIDBuilder_ == null) {
        fromIDBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
            com.ids.IdentityID, com.ids.IdentityID.Builder, com.ids.IdentityIDOrBuilder>(
                getFromID(),
                getParentForChildren(),
                isClean());
        fromID_ = null;
      }
      return fromIDBuilder_;
    }

    private com.ids.AssetID assetID_;
    private com.google.protobuf.SingleFieldBuilderV3<
        com.ids.AssetID, com.ids.AssetID.Builder, com.ids.AssetIDOrBuilder> assetIDBuilder_;
    /**
     * <code>.ids.AssetID asset_i_d = 3 [json_name = "assetID"];</code>
     * @return Whether the assetID field is set.
     */
    public boolean hasAssetID() {
      return assetIDBuilder_ != null || assetID_ != null;
    }
    /**
     * <code>.ids.AssetID asset_i_d = 3 [json_name = "assetID"];</code>
     * @return The assetID.
     */
    public com.ids.AssetID getAssetID() {
      if (assetIDBuilder_ == null) {
        return assetID_ == null ? com.ids.AssetID.getDefaultInstance() : assetID_;
      } else {
        return assetIDBuilder_.getMessage();
      }
    }
    /**
     * <code>.ids.AssetID asset_i_d = 3 [json_name = "assetID"];</code>
     */
    public Builder setAssetID(com.ids.AssetID value) {
      if (assetIDBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        assetID_ = value;
        onChanged();
      } else {
        assetIDBuilder_.setMessage(value);
      }

      return this;
    }
    /**
     * <code>.ids.AssetID asset_i_d = 3 [json_name = "assetID"];</code>
     */
    public Builder setAssetID(
        com.ids.AssetID.Builder builderForValue) {
      if (assetIDBuilder_ == null) {
        assetID_ = builderForValue.build();
        onChanged();
      } else {
        assetIDBuilder_.setMessage(builderForValue.build());
      }

      return this;
    }
    /**
     * <code>.ids.AssetID asset_i_d = 3 [json_name = "assetID"];</code>
     */
    public Builder mergeAssetID(com.ids.AssetID value) {
      if (assetIDBuilder_ == null) {
        if (assetID_ != null) {
          assetID_ =
            com.ids.AssetID.newBuilder(assetID_).mergeFrom(value).buildPartial();
        } else {
          assetID_ = value;
        }
        onChanged();
      } else {
        assetIDBuilder_.mergeFrom(value);
      }

      return this;
    }
    /**
     * <code>.ids.AssetID asset_i_d = 3 [json_name = "assetID"];</code>
     */
    public Builder clearAssetID() {
      if (assetIDBuilder_ == null) {
        assetID_ = null;
        onChanged();
      } else {
        assetID_ = null;
        assetIDBuilder_ = null;
      }

      return this;
    }
    /**
     * <code>.ids.AssetID asset_i_d = 3 [json_name = "assetID"];</code>
     */
    public com.ids.AssetID.Builder getAssetIDBuilder() {
      
      onChanged();
      return getAssetIDFieldBuilder().getBuilder();
    }
    /**
     * <code>.ids.AssetID asset_i_d = 3 [json_name = "assetID"];</code>
     */
    public com.ids.AssetIDOrBuilder getAssetIDOrBuilder() {
      if (assetIDBuilder_ != null) {
        return assetIDBuilder_.getMessageOrBuilder();
      } else {
        return assetID_ == null ?
            com.ids.AssetID.getDefaultInstance() : assetID_;
      }
    }
    /**
     * <code>.ids.AssetID asset_i_d = 3 [json_name = "assetID"];</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
        com.ids.AssetID, com.ids.AssetID.Builder, com.ids.AssetIDOrBuilder> 
        getAssetIDFieldBuilder() {
      if (assetIDBuilder_ == null) {
        assetIDBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
            com.ids.AssetID, com.ids.AssetID.Builder, com.ids.AssetIDOrBuilder>(
                getAssetID(),
                getParentForChildren(),
                isClean());
        assetID_ = null;
      }
      return assetIDBuilder_;
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


    // @@protoc_insertion_point(builder_scope:assets.transactions.renumerate.Message)
  }

  // @@protoc_insertion_point(class_scope:assets.transactions.renumerate.Message)
  private static final com.assets.transactions.renumerate.Message DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new com.assets.transactions.renumerate.Message();
  }

  public static com.assets.transactions.renumerate.Message getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<Message>
      PARSER = new com.google.protobuf.AbstractParser<Message>() {
    @java.lang.Override
    public Message parsePartialFrom(
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

  public static com.google.protobuf.Parser<Message> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<Message> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public com.assets.transactions.renumerate.Message getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

