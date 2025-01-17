// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: tendermint/p2p/pex.proto

package com.tendermint.p2p;

/**
 * Protobuf type {@code tendermint.p2p.PexAddrs}
 */
public final class PexAddrs extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:tendermint.p2p.PexAddrs)
    PexAddrsOrBuilder {
private static final long serialVersionUID = 0L;
  // Use PexAddrs.newBuilder() to construct.
  private PexAddrs(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private PexAddrs() {
    addrs_ = java.util.Collections.emptyList();
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new PexAddrs();
  }

  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return com.tendermint.p2p.PexProto.internal_static_tendermint_p2p_PexAddrs_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return com.tendermint.p2p.PexProto.internal_static_tendermint_p2p_PexAddrs_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            com.tendermint.p2p.PexAddrs.class, com.tendermint.p2p.PexAddrs.Builder.class);
  }

  public static final int ADDRS_FIELD_NUMBER = 1;
  @SuppressWarnings("serial")
  private java.util.List<com.tendermint.p2p.NetAddress> addrs_;
  /**
   * <code>repeated .tendermint.p2p.NetAddress addrs = 1 [json_name = "addrs", (.gogoproto.nullable) = false];</code>
   */
  @java.lang.Override
  public java.util.List<com.tendermint.p2p.NetAddress> getAddrsList() {
    return addrs_;
  }
  /**
   * <code>repeated .tendermint.p2p.NetAddress addrs = 1 [json_name = "addrs", (.gogoproto.nullable) = false];</code>
   */
  @java.lang.Override
  public java.util.List<? extends com.tendermint.p2p.NetAddressOrBuilder> 
      getAddrsOrBuilderList() {
    return addrs_;
  }
  /**
   * <code>repeated .tendermint.p2p.NetAddress addrs = 1 [json_name = "addrs", (.gogoproto.nullable) = false];</code>
   */
  @java.lang.Override
  public int getAddrsCount() {
    return addrs_.size();
  }
  /**
   * <code>repeated .tendermint.p2p.NetAddress addrs = 1 [json_name = "addrs", (.gogoproto.nullable) = false];</code>
   */
  @java.lang.Override
  public com.tendermint.p2p.NetAddress getAddrs(int index) {
    return addrs_.get(index);
  }
  /**
   * <code>repeated .tendermint.p2p.NetAddress addrs = 1 [json_name = "addrs", (.gogoproto.nullable) = false];</code>
   */
  @java.lang.Override
  public com.tendermint.p2p.NetAddressOrBuilder getAddrsOrBuilder(
      int index) {
    return addrs_.get(index);
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
    for (int i = 0; i < addrs_.size(); i++) {
      output.writeMessage(1, addrs_.get(i));
    }
    getUnknownFields().writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    for (int i = 0; i < addrs_.size(); i++) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(1, addrs_.get(i));
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
    if (!(obj instanceof com.tendermint.p2p.PexAddrs)) {
      return super.equals(obj);
    }
    com.tendermint.p2p.PexAddrs other = (com.tendermint.p2p.PexAddrs) obj;

    if (!getAddrsList()
        .equals(other.getAddrsList())) return false;
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
    if (getAddrsCount() > 0) {
      hash = (37 * hash) + ADDRS_FIELD_NUMBER;
      hash = (53 * hash) + getAddrsList().hashCode();
    }
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static com.tendermint.p2p.PexAddrs parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.tendermint.p2p.PexAddrs parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.tendermint.p2p.PexAddrs parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.tendermint.p2p.PexAddrs parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.tendermint.p2p.PexAddrs parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.tendermint.p2p.PexAddrs parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.tendermint.p2p.PexAddrs parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.tendermint.p2p.PexAddrs parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.tendermint.p2p.PexAddrs parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static com.tendermint.p2p.PexAddrs parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.tendermint.p2p.PexAddrs parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.tendermint.p2p.PexAddrs parseFrom(
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
  public static Builder newBuilder(com.tendermint.p2p.PexAddrs prototype) {
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
   * Protobuf type {@code tendermint.p2p.PexAddrs}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:tendermint.p2p.PexAddrs)
      com.tendermint.p2p.PexAddrsOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return com.tendermint.p2p.PexProto.internal_static_tendermint_p2p_PexAddrs_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return com.tendermint.p2p.PexProto.internal_static_tendermint_p2p_PexAddrs_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              com.tendermint.p2p.PexAddrs.class, com.tendermint.p2p.PexAddrs.Builder.class);
    }

    // Construct using com.tendermint.p2p.PexAddrs.newBuilder()
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
      if (addrsBuilder_ == null) {
        addrs_ = java.util.Collections.emptyList();
      } else {
        addrs_ = null;
        addrsBuilder_.clear();
      }
      bitField0_ = (bitField0_ & ~0x00000001);
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return com.tendermint.p2p.PexProto.internal_static_tendermint_p2p_PexAddrs_descriptor;
    }

    @java.lang.Override
    public com.tendermint.p2p.PexAddrs getDefaultInstanceForType() {
      return com.tendermint.p2p.PexAddrs.getDefaultInstance();
    }

    @java.lang.Override
    public com.tendermint.p2p.PexAddrs build() {
      com.tendermint.p2p.PexAddrs result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public com.tendermint.p2p.PexAddrs buildPartial() {
      com.tendermint.p2p.PexAddrs result = new com.tendermint.p2p.PexAddrs(this);
      buildPartialRepeatedFields(result);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartialRepeatedFields(com.tendermint.p2p.PexAddrs result) {
      if (addrsBuilder_ == null) {
        if (((bitField0_ & 0x00000001) != 0)) {
          addrs_ = java.util.Collections.unmodifiableList(addrs_);
          bitField0_ = (bitField0_ & ~0x00000001);
        }
        result.addrs_ = addrs_;
      } else {
        result.addrs_ = addrsBuilder_.build();
      }
    }

    private void buildPartial0(com.tendermint.p2p.PexAddrs result) {
      int from_bitField0_ = bitField0_;
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
      if (other instanceof com.tendermint.p2p.PexAddrs) {
        return mergeFrom((com.tendermint.p2p.PexAddrs)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(com.tendermint.p2p.PexAddrs other) {
      if (other == com.tendermint.p2p.PexAddrs.getDefaultInstance()) return this;
      if (addrsBuilder_ == null) {
        if (!other.addrs_.isEmpty()) {
          if (addrs_.isEmpty()) {
            addrs_ = other.addrs_;
            bitField0_ = (bitField0_ & ~0x00000001);
          } else {
            ensureAddrsIsMutable();
            addrs_.addAll(other.addrs_);
          }
          onChanged();
        }
      } else {
        if (!other.addrs_.isEmpty()) {
          if (addrsBuilder_.isEmpty()) {
            addrsBuilder_.dispose();
            addrsBuilder_ = null;
            addrs_ = other.addrs_;
            bitField0_ = (bitField0_ & ~0x00000001);
            addrsBuilder_ = 
              com.google.protobuf.GeneratedMessageV3.alwaysUseFieldBuilders ?
                 getAddrsFieldBuilder() : null;
          } else {
            addrsBuilder_.addAllMessages(other.addrs_);
          }
        }
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
              com.tendermint.p2p.NetAddress m =
                  input.readMessage(
                      com.tendermint.p2p.NetAddress.parser(),
                      extensionRegistry);
              if (addrsBuilder_ == null) {
                ensureAddrsIsMutable();
                addrs_.add(m);
              } else {
                addrsBuilder_.addMessage(m);
              }
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

    private java.util.List<com.tendermint.p2p.NetAddress> addrs_ =
      java.util.Collections.emptyList();
    private void ensureAddrsIsMutable() {
      if (!((bitField0_ & 0x00000001) != 0)) {
        addrs_ = new java.util.ArrayList<com.tendermint.p2p.NetAddress>(addrs_);
        bitField0_ |= 0x00000001;
       }
    }

    private com.google.protobuf.RepeatedFieldBuilderV3<
        com.tendermint.p2p.NetAddress, com.tendermint.p2p.NetAddress.Builder, com.tendermint.p2p.NetAddressOrBuilder> addrsBuilder_;

    /**
     * <code>repeated .tendermint.p2p.NetAddress addrs = 1 [json_name = "addrs", (.gogoproto.nullable) = false];</code>
     */
    public java.util.List<com.tendermint.p2p.NetAddress> getAddrsList() {
      if (addrsBuilder_ == null) {
        return java.util.Collections.unmodifiableList(addrs_);
      } else {
        return addrsBuilder_.getMessageList();
      }
    }
    /**
     * <code>repeated .tendermint.p2p.NetAddress addrs = 1 [json_name = "addrs", (.gogoproto.nullable) = false];</code>
     */
    public int getAddrsCount() {
      if (addrsBuilder_ == null) {
        return addrs_.size();
      } else {
        return addrsBuilder_.getCount();
      }
    }
    /**
     * <code>repeated .tendermint.p2p.NetAddress addrs = 1 [json_name = "addrs", (.gogoproto.nullable) = false];</code>
     */
    public com.tendermint.p2p.NetAddress getAddrs(int index) {
      if (addrsBuilder_ == null) {
        return addrs_.get(index);
      } else {
        return addrsBuilder_.getMessage(index);
      }
    }
    /**
     * <code>repeated .tendermint.p2p.NetAddress addrs = 1 [json_name = "addrs", (.gogoproto.nullable) = false];</code>
     */
    public Builder setAddrs(
        int index, com.tendermint.p2p.NetAddress value) {
      if (addrsBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureAddrsIsMutable();
        addrs_.set(index, value);
        onChanged();
      } else {
        addrsBuilder_.setMessage(index, value);
      }
      return this;
    }
    /**
     * <code>repeated .tendermint.p2p.NetAddress addrs = 1 [json_name = "addrs", (.gogoproto.nullable) = false];</code>
     */
    public Builder setAddrs(
        int index, com.tendermint.p2p.NetAddress.Builder builderForValue) {
      if (addrsBuilder_ == null) {
        ensureAddrsIsMutable();
        addrs_.set(index, builderForValue.build());
        onChanged();
      } else {
        addrsBuilder_.setMessage(index, builderForValue.build());
      }
      return this;
    }
    /**
     * <code>repeated .tendermint.p2p.NetAddress addrs = 1 [json_name = "addrs", (.gogoproto.nullable) = false];</code>
     */
    public Builder addAddrs(com.tendermint.p2p.NetAddress value) {
      if (addrsBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureAddrsIsMutable();
        addrs_.add(value);
        onChanged();
      } else {
        addrsBuilder_.addMessage(value);
      }
      return this;
    }
    /**
     * <code>repeated .tendermint.p2p.NetAddress addrs = 1 [json_name = "addrs", (.gogoproto.nullable) = false];</code>
     */
    public Builder addAddrs(
        int index, com.tendermint.p2p.NetAddress value) {
      if (addrsBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureAddrsIsMutable();
        addrs_.add(index, value);
        onChanged();
      } else {
        addrsBuilder_.addMessage(index, value);
      }
      return this;
    }
    /**
     * <code>repeated .tendermint.p2p.NetAddress addrs = 1 [json_name = "addrs", (.gogoproto.nullable) = false];</code>
     */
    public Builder addAddrs(
        com.tendermint.p2p.NetAddress.Builder builderForValue) {
      if (addrsBuilder_ == null) {
        ensureAddrsIsMutable();
        addrs_.add(builderForValue.build());
        onChanged();
      } else {
        addrsBuilder_.addMessage(builderForValue.build());
      }
      return this;
    }
    /**
     * <code>repeated .tendermint.p2p.NetAddress addrs = 1 [json_name = "addrs", (.gogoproto.nullable) = false];</code>
     */
    public Builder addAddrs(
        int index, com.tendermint.p2p.NetAddress.Builder builderForValue) {
      if (addrsBuilder_ == null) {
        ensureAddrsIsMutable();
        addrs_.add(index, builderForValue.build());
        onChanged();
      } else {
        addrsBuilder_.addMessage(index, builderForValue.build());
      }
      return this;
    }
    /**
     * <code>repeated .tendermint.p2p.NetAddress addrs = 1 [json_name = "addrs", (.gogoproto.nullable) = false];</code>
     */
    public Builder addAllAddrs(
        java.lang.Iterable<? extends com.tendermint.p2p.NetAddress> values) {
      if (addrsBuilder_ == null) {
        ensureAddrsIsMutable();
        com.google.protobuf.AbstractMessageLite.Builder.addAll(
            values, addrs_);
        onChanged();
      } else {
        addrsBuilder_.addAllMessages(values);
      }
      return this;
    }
    /**
     * <code>repeated .tendermint.p2p.NetAddress addrs = 1 [json_name = "addrs", (.gogoproto.nullable) = false];</code>
     */
    public Builder clearAddrs() {
      if (addrsBuilder_ == null) {
        addrs_ = java.util.Collections.emptyList();
        bitField0_ = (bitField0_ & ~0x00000001);
        onChanged();
      } else {
        addrsBuilder_.clear();
      }
      return this;
    }
    /**
     * <code>repeated .tendermint.p2p.NetAddress addrs = 1 [json_name = "addrs", (.gogoproto.nullable) = false];</code>
     */
    public Builder removeAddrs(int index) {
      if (addrsBuilder_ == null) {
        ensureAddrsIsMutable();
        addrs_.remove(index);
        onChanged();
      } else {
        addrsBuilder_.remove(index);
      }
      return this;
    }
    /**
     * <code>repeated .tendermint.p2p.NetAddress addrs = 1 [json_name = "addrs", (.gogoproto.nullable) = false];</code>
     */
    public com.tendermint.p2p.NetAddress.Builder getAddrsBuilder(
        int index) {
      return getAddrsFieldBuilder().getBuilder(index);
    }
    /**
     * <code>repeated .tendermint.p2p.NetAddress addrs = 1 [json_name = "addrs", (.gogoproto.nullable) = false];</code>
     */
    public com.tendermint.p2p.NetAddressOrBuilder getAddrsOrBuilder(
        int index) {
      if (addrsBuilder_ == null) {
        return addrs_.get(index);  } else {
        return addrsBuilder_.getMessageOrBuilder(index);
      }
    }
    /**
     * <code>repeated .tendermint.p2p.NetAddress addrs = 1 [json_name = "addrs", (.gogoproto.nullable) = false];</code>
     */
    public java.util.List<? extends com.tendermint.p2p.NetAddressOrBuilder> 
         getAddrsOrBuilderList() {
      if (addrsBuilder_ != null) {
        return addrsBuilder_.getMessageOrBuilderList();
      } else {
        return java.util.Collections.unmodifiableList(addrs_);
      }
    }
    /**
     * <code>repeated .tendermint.p2p.NetAddress addrs = 1 [json_name = "addrs", (.gogoproto.nullable) = false];</code>
     */
    public com.tendermint.p2p.NetAddress.Builder addAddrsBuilder() {
      return getAddrsFieldBuilder().addBuilder(
          com.tendermint.p2p.NetAddress.getDefaultInstance());
    }
    /**
     * <code>repeated .tendermint.p2p.NetAddress addrs = 1 [json_name = "addrs", (.gogoproto.nullable) = false];</code>
     */
    public com.tendermint.p2p.NetAddress.Builder addAddrsBuilder(
        int index) {
      return getAddrsFieldBuilder().addBuilder(
          index, com.tendermint.p2p.NetAddress.getDefaultInstance());
    }
    /**
     * <code>repeated .tendermint.p2p.NetAddress addrs = 1 [json_name = "addrs", (.gogoproto.nullable) = false];</code>
     */
    public java.util.List<com.tendermint.p2p.NetAddress.Builder> 
         getAddrsBuilderList() {
      return getAddrsFieldBuilder().getBuilderList();
    }
    private com.google.protobuf.RepeatedFieldBuilderV3<
        com.tendermint.p2p.NetAddress, com.tendermint.p2p.NetAddress.Builder, com.tendermint.p2p.NetAddressOrBuilder> 
        getAddrsFieldBuilder() {
      if (addrsBuilder_ == null) {
        addrsBuilder_ = new com.google.protobuf.RepeatedFieldBuilderV3<
            com.tendermint.p2p.NetAddress, com.tendermint.p2p.NetAddress.Builder, com.tendermint.p2p.NetAddressOrBuilder>(
                addrs_,
                ((bitField0_ & 0x00000001) != 0),
                getParentForChildren(),
                isClean());
        addrs_ = null;
      }
      return addrsBuilder_;
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


    // @@protoc_insertion_point(builder_scope:tendermint.p2p.PexAddrs)
  }

  // @@protoc_insertion_point(class_scope:tendermint.p2p.PexAddrs)
  private static final com.tendermint.p2p.PexAddrs DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new com.tendermint.p2p.PexAddrs();
  }

  public static com.tendermint.p2p.PexAddrs getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<PexAddrs>
      PARSER = new com.google.protobuf.AbstractParser<PexAddrs>() {
    @java.lang.Override
    public PexAddrs parsePartialFrom(
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

  public static com.google.protobuf.Parser<PexAddrs> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<PexAddrs> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public com.tendermint.p2p.PexAddrs getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

