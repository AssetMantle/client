// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: identities/transactions/nub/message.proto

package com.assetmantle.modules.identities.transactions.nub;

/**
 * Protobuf type {@code assetmantle.modules.identities.transactions.nub.Message}
 */
public final class Message extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:assetmantle.modules.identities.transactions.nub.Message)
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

  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return com.assetmantle.modules.identities.transactions.nub.MessageProto.internal_static_assetmantle_modules_identities_transactions_nub_Message_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return com.assetmantle.modules.identities.transactions.nub.MessageProto.internal_static_assetmantle_modules_identities_transactions_nub_Message_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            com.assetmantle.modules.identities.transactions.nub.Message.class, com.assetmantle.modules.identities.transactions.nub.Message.Builder.class);
  }

  public static final int FROM_FIELD_NUMBER = 1;
  @SuppressWarnings("serial")
  private volatile java.lang.Object from_ = "";
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

  public static final int NUB_I_D_FIELD_NUMBER = 2;
  private com.assetmantle.schema.ids.base.StringID nubID_;
  /**
   * <code>.assetmantle.schema.ids.base.StringID nub_i_d = 2 [json_name = "nubID"];</code>
   * @return Whether the nubID field is set.
   */
  @java.lang.Override
  public boolean hasNubID() {
    return nubID_ != null;
  }
  /**
   * <code>.assetmantle.schema.ids.base.StringID nub_i_d = 2 [json_name = "nubID"];</code>
   * @return The nubID.
   */
  @java.lang.Override
  public com.assetmantle.schema.ids.base.StringID getNubID() {
    return nubID_ == null ? com.assetmantle.schema.ids.base.StringID.getDefaultInstance() : nubID_;
  }
  /**
   * <code>.assetmantle.schema.ids.base.StringID nub_i_d = 2 [json_name = "nubID"];</code>
   */
  @java.lang.Override
  public com.assetmantle.schema.ids.base.StringIDOrBuilder getNubIDOrBuilder() {
    return nubID_ == null ? com.assetmantle.schema.ids.base.StringID.getDefaultInstance() : nubID_;
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
    if (nubID_ != null) {
      output.writeMessage(2, getNubID());
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
    if (nubID_ != null) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(2, getNubID());
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
    if (!(obj instanceof com.assetmantle.modules.identities.transactions.nub.Message)) {
      return super.equals(obj);
    }
    com.assetmantle.modules.identities.transactions.nub.Message other = (com.assetmantle.modules.identities.transactions.nub.Message) obj;

    if (!getFrom()
        .equals(other.getFrom())) return false;
    if (hasNubID() != other.hasNubID()) return false;
    if (hasNubID()) {
      if (!getNubID()
          .equals(other.getNubID())) return false;
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
    if (hasNubID()) {
      hash = (37 * hash) + NUB_I_D_FIELD_NUMBER;
      hash = (53 * hash) + getNubID().hashCode();
    }
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static com.assetmantle.modules.identities.transactions.nub.Message parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.assetmantle.modules.identities.transactions.nub.Message parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.assetmantle.modules.identities.transactions.nub.Message parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.assetmantle.modules.identities.transactions.nub.Message parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.assetmantle.modules.identities.transactions.nub.Message parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.assetmantle.modules.identities.transactions.nub.Message parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.assetmantle.modules.identities.transactions.nub.Message parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.assetmantle.modules.identities.transactions.nub.Message parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.assetmantle.modules.identities.transactions.nub.Message parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static com.assetmantle.modules.identities.transactions.nub.Message parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.assetmantle.modules.identities.transactions.nub.Message parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.assetmantle.modules.identities.transactions.nub.Message parseFrom(
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
  public static Builder newBuilder(com.assetmantle.modules.identities.transactions.nub.Message prototype) {
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
   * Protobuf type {@code assetmantle.modules.identities.transactions.nub.Message}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:assetmantle.modules.identities.transactions.nub.Message)
      com.assetmantle.modules.identities.transactions.nub.MessageOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return com.assetmantle.modules.identities.transactions.nub.MessageProto.internal_static_assetmantle_modules_identities_transactions_nub_Message_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return com.assetmantle.modules.identities.transactions.nub.MessageProto.internal_static_assetmantle_modules_identities_transactions_nub_Message_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              com.assetmantle.modules.identities.transactions.nub.Message.class, com.assetmantle.modules.identities.transactions.nub.Message.Builder.class);
    }

    // Construct using com.assetmantle.modules.identities.transactions.nub.Message.newBuilder()
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
      from_ = "";
      nubID_ = null;
      if (nubIDBuilder_ != null) {
        nubIDBuilder_.dispose();
        nubIDBuilder_ = null;
      }
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return com.assetmantle.modules.identities.transactions.nub.MessageProto.internal_static_assetmantle_modules_identities_transactions_nub_Message_descriptor;
    }

    @java.lang.Override
    public com.assetmantle.modules.identities.transactions.nub.Message getDefaultInstanceForType() {
      return com.assetmantle.modules.identities.transactions.nub.Message.getDefaultInstance();
    }

    @java.lang.Override
    public com.assetmantle.modules.identities.transactions.nub.Message build() {
      com.assetmantle.modules.identities.transactions.nub.Message result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public com.assetmantle.modules.identities.transactions.nub.Message buildPartial() {
      com.assetmantle.modules.identities.transactions.nub.Message result = new com.assetmantle.modules.identities.transactions.nub.Message(this);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartial0(com.assetmantle.modules.identities.transactions.nub.Message result) {
      int from_bitField0_ = bitField0_;
      if (((from_bitField0_ & 0x00000001) != 0)) {
        result.from_ = from_;
      }
      if (((from_bitField0_ & 0x00000002) != 0)) {
        result.nubID_ = nubIDBuilder_ == null
            ? nubID_
            : nubIDBuilder_.build();
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
      if (other instanceof com.assetmantle.modules.identities.transactions.nub.Message) {
        return mergeFrom((com.assetmantle.modules.identities.transactions.nub.Message)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(com.assetmantle.modules.identities.transactions.nub.Message other) {
      if (other == com.assetmantle.modules.identities.transactions.nub.Message.getDefaultInstance()) return this;
      if (!other.getFrom().isEmpty()) {
        from_ = other.from_;
        bitField0_ |= 0x00000001;
        onChanged();
      }
      if (other.hasNubID()) {
        mergeNubID(other.getNubID());
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
              bitField0_ |= 0x00000001;
              break;
            } // case 10
            case 18: {
              input.readMessage(
                  getNubIDFieldBuilder().getBuilder(),
                  extensionRegistry);
              bitField0_ |= 0x00000002;
              break;
            } // case 18
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
      if (value == null) { throw new NullPointerException(); }
      from_ = value;
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <code>string from = 1 [json_name = "from"];</code>
     * @return This builder for chaining.
     */
    public Builder clearFrom() {
      from_ = getDefaultInstance().getFrom();
      bitField0_ = (bitField0_ & ~0x00000001);
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
      if (value == null) { throw new NullPointerException(); }
      checkByteStringIsUtf8(value);
      from_ = value;
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }

    private com.assetmantle.schema.ids.base.StringID nubID_;
    private com.google.protobuf.SingleFieldBuilderV3<
        com.assetmantle.schema.ids.base.StringID, com.assetmantle.schema.ids.base.StringID.Builder, com.assetmantle.schema.ids.base.StringIDOrBuilder> nubIDBuilder_;
    /**
     * <code>.assetmantle.schema.ids.base.StringID nub_i_d = 2 [json_name = "nubID"];</code>
     * @return Whether the nubID field is set.
     */
    public boolean hasNubID() {
      return ((bitField0_ & 0x00000002) != 0);
    }
    /**
     * <code>.assetmantle.schema.ids.base.StringID nub_i_d = 2 [json_name = "nubID"];</code>
     * @return The nubID.
     */
    public com.assetmantle.schema.ids.base.StringID getNubID() {
      if (nubIDBuilder_ == null) {
        return nubID_ == null ? com.assetmantle.schema.ids.base.StringID.getDefaultInstance() : nubID_;
      } else {
        return nubIDBuilder_.getMessage();
      }
    }
    /**
     * <code>.assetmantle.schema.ids.base.StringID nub_i_d = 2 [json_name = "nubID"];</code>
     */
    public Builder setNubID(com.assetmantle.schema.ids.base.StringID value) {
      if (nubIDBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        nubID_ = value;
      } else {
        nubIDBuilder_.setMessage(value);
      }
      bitField0_ |= 0x00000002;
      onChanged();
      return this;
    }
    /**
     * <code>.assetmantle.schema.ids.base.StringID nub_i_d = 2 [json_name = "nubID"];</code>
     */
    public Builder setNubID(
        com.assetmantle.schema.ids.base.StringID.Builder builderForValue) {
      if (nubIDBuilder_ == null) {
        nubID_ = builderForValue.build();
      } else {
        nubIDBuilder_.setMessage(builderForValue.build());
      }
      bitField0_ |= 0x00000002;
      onChanged();
      return this;
    }
    /**
     * <code>.assetmantle.schema.ids.base.StringID nub_i_d = 2 [json_name = "nubID"];</code>
     */
    public Builder mergeNubID(com.assetmantle.schema.ids.base.StringID value) {
      if (nubIDBuilder_ == null) {
        if (((bitField0_ & 0x00000002) != 0) &&
          nubID_ != null &&
          nubID_ != com.assetmantle.schema.ids.base.StringID.getDefaultInstance()) {
          getNubIDBuilder().mergeFrom(value);
        } else {
          nubID_ = value;
        }
      } else {
        nubIDBuilder_.mergeFrom(value);
      }
      bitField0_ |= 0x00000002;
      onChanged();
      return this;
    }
    /**
     * <code>.assetmantle.schema.ids.base.StringID nub_i_d = 2 [json_name = "nubID"];</code>
     */
    public Builder clearNubID() {
      bitField0_ = (bitField0_ & ~0x00000002);
      nubID_ = null;
      if (nubIDBuilder_ != null) {
        nubIDBuilder_.dispose();
        nubIDBuilder_ = null;
      }
      onChanged();
      return this;
    }
    /**
     * <code>.assetmantle.schema.ids.base.StringID nub_i_d = 2 [json_name = "nubID"];</code>
     */
    public com.assetmantle.schema.ids.base.StringID.Builder getNubIDBuilder() {
      bitField0_ |= 0x00000002;
      onChanged();
      return getNubIDFieldBuilder().getBuilder();
    }
    /**
     * <code>.assetmantle.schema.ids.base.StringID nub_i_d = 2 [json_name = "nubID"];</code>
     */
    public com.assetmantle.schema.ids.base.StringIDOrBuilder getNubIDOrBuilder() {
      if (nubIDBuilder_ != null) {
        return nubIDBuilder_.getMessageOrBuilder();
      } else {
        return nubID_ == null ?
            com.assetmantle.schema.ids.base.StringID.getDefaultInstance() : nubID_;
      }
    }
    /**
     * <code>.assetmantle.schema.ids.base.StringID nub_i_d = 2 [json_name = "nubID"];</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
        com.assetmantle.schema.ids.base.StringID, com.assetmantle.schema.ids.base.StringID.Builder, com.assetmantle.schema.ids.base.StringIDOrBuilder> 
        getNubIDFieldBuilder() {
      if (nubIDBuilder_ == null) {
        nubIDBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
            com.assetmantle.schema.ids.base.StringID, com.assetmantle.schema.ids.base.StringID.Builder, com.assetmantle.schema.ids.base.StringIDOrBuilder>(
                getNubID(),
                getParentForChildren(),
                isClean());
        nubID_ = null;
      }
      return nubIDBuilder_;
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


    // @@protoc_insertion_point(builder_scope:assetmantle.modules.identities.transactions.nub.Message)
  }

  // @@protoc_insertion_point(class_scope:assetmantle.modules.identities.transactions.nub.Message)
  private static final com.assetmantle.modules.identities.transactions.nub.Message DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new com.assetmantle.modules.identities.transactions.nub.Message();
  }

  public static com.assetmantle.modules.identities.transactions.nub.Message getDefaultInstance() {
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
  public com.assetmantle.modules.identities.transactions.nub.Message getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

