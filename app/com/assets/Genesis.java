// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: modules/assets/internal/genesis/genesis.proto

package com.assets;

/**
 * Protobuf type {@code assets.Genesis}
 */
public final class Genesis extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:assets.Genesis)
    GenesisOrBuilder {
private static final long serialVersionUID = 0L;
  // Use Genesis.newBuilder() to construct.
  private Genesis(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private Genesis() {
    mappables_ = java.util.Collections.emptyList();
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new Genesis();
  }

  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return com.assets.GenesisProto.internal_static_assets_Genesis_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return com.assets.GenesisProto.internal_static_assets_Genesis_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            com.assets.Genesis.class, com.assets.Genesis.Builder.class);
  }

  public static final int MAPPABLES_FIELD_NUMBER = 1;
  @SuppressWarnings("serial")
  private java.util.List<com.assets.Mappable> mappables_;
  /**
   * <code>repeated .assets.Mappable mappables = 1 [json_name = "mappables"];</code>
   */
  @java.lang.Override
  public java.util.List<com.assets.Mappable> getMappablesList() {
    return mappables_;
  }
  /**
   * <code>repeated .assets.Mappable mappables = 1 [json_name = "mappables"];</code>
   */
  @java.lang.Override
  public java.util.List<? extends com.assets.MappableOrBuilder> 
      getMappablesOrBuilderList() {
    return mappables_;
  }
  /**
   * <code>repeated .assets.Mappable mappables = 1 [json_name = "mappables"];</code>
   */
  @java.lang.Override
  public int getMappablesCount() {
    return mappables_.size();
  }
  /**
   * <code>repeated .assets.Mappable mappables = 1 [json_name = "mappables"];</code>
   */
  @java.lang.Override
  public com.assets.Mappable getMappables(int index) {
    return mappables_.get(index);
  }
  /**
   * <code>repeated .assets.Mappable mappables = 1 [json_name = "mappables"];</code>
   */
  @java.lang.Override
  public com.assets.MappableOrBuilder getMappablesOrBuilder(
      int index) {
    return mappables_.get(index);
  }

  public static final int PARAMETERLIST_FIELD_NUMBER = 2;
  private com.parameters.ParameterList parameterList_;
  /**
   * <code>.parameters.ParameterList parameterList = 2 [json_name = "parameterList"];</code>
   * @return Whether the parameterList field is set.
   */
  @java.lang.Override
  public boolean hasParameterList() {
    return parameterList_ != null;
  }
  /**
   * <code>.parameters.ParameterList parameterList = 2 [json_name = "parameterList"];</code>
   * @return The parameterList.
   */
  @java.lang.Override
  public com.parameters.ParameterList getParameterList() {
    return parameterList_ == null ? com.parameters.ParameterList.getDefaultInstance() : parameterList_;
  }
  /**
   * <code>.parameters.ParameterList parameterList = 2 [json_name = "parameterList"];</code>
   */
  @java.lang.Override
  public com.parameters.ParameterListOrBuilder getParameterListOrBuilder() {
    return parameterList_ == null ? com.parameters.ParameterList.getDefaultInstance() : parameterList_;
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
    for (int i = 0; i < mappables_.size(); i++) {
      output.writeMessage(1, mappables_.get(i));
    }
    if (parameterList_ != null) {
      output.writeMessage(2, getParameterList());
    }
    getUnknownFields().writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    for (int i = 0; i < mappables_.size(); i++) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(1, mappables_.get(i));
    }
    if (parameterList_ != null) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(2, getParameterList());
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
    if (!(obj instanceof com.assets.Genesis)) {
      return super.equals(obj);
    }
    com.assets.Genesis other = (com.assets.Genesis) obj;

    if (!getMappablesList()
        .equals(other.getMappablesList())) return false;
    if (hasParameterList() != other.hasParameterList()) return false;
    if (hasParameterList()) {
      if (!getParameterList()
          .equals(other.getParameterList())) return false;
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
    if (getMappablesCount() > 0) {
      hash = (37 * hash) + MAPPABLES_FIELD_NUMBER;
      hash = (53 * hash) + getMappablesList().hashCode();
    }
    if (hasParameterList()) {
      hash = (37 * hash) + PARAMETERLIST_FIELD_NUMBER;
      hash = (53 * hash) + getParameterList().hashCode();
    }
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static com.assets.Genesis parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.assets.Genesis parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.assets.Genesis parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.assets.Genesis parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.assets.Genesis parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.assets.Genesis parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.assets.Genesis parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.assets.Genesis parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.assets.Genesis parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static com.assets.Genesis parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.assets.Genesis parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.assets.Genesis parseFrom(
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
  public static Builder newBuilder(com.assets.Genesis prototype) {
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
   * Protobuf type {@code assets.Genesis}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:assets.Genesis)
      com.assets.GenesisOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return com.assets.GenesisProto.internal_static_assets_Genesis_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return com.assets.GenesisProto.internal_static_assets_Genesis_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              com.assets.Genesis.class, com.assets.Genesis.Builder.class);
    }

    // Construct using com.assets.Genesis.newBuilder()
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
      if (mappablesBuilder_ == null) {
        mappables_ = java.util.Collections.emptyList();
      } else {
        mappables_ = null;
        mappablesBuilder_.clear();
      }
      bitField0_ = (bitField0_ & ~0x00000001);
      parameterList_ = null;
      if (parameterListBuilder_ != null) {
        parameterListBuilder_.dispose();
        parameterListBuilder_ = null;
      }
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return com.assets.GenesisProto.internal_static_assets_Genesis_descriptor;
    }

    @java.lang.Override
    public com.assets.Genesis getDefaultInstanceForType() {
      return com.assets.Genesis.getDefaultInstance();
    }

    @java.lang.Override
    public com.assets.Genesis build() {
      com.assets.Genesis result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public com.assets.Genesis buildPartial() {
      com.assets.Genesis result = new com.assets.Genesis(this);
      buildPartialRepeatedFields(result);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartialRepeatedFields(com.assets.Genesis result) {
      if (mappablesBuilder_ == null) {
        if (((bitField0_ & 0x00000001) != 0)) {
          mappables_ = java.util.Collections.unmodifiableList(mappables_);
          bitField0_ = (bitField0_ & ~0x00000001);
        }
        result.mappables_ = mappables_;
      } else {
        result.mappables_ = mappablesBuilder_.build();
      }
    }

    private void buildPartial0(com.assets.Genesis result) {
      int from_bitField0_ = bitField0_;
      if (((from_bitField0_ & 0x00000002) != 0)) {
        result.parameterList_ = parameterListBuilder_ == null
            ? parameterList_
            : parameterListBuilder_.build();
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
      if (other instanceof com.assets.Genesis) {
        return mergeFrom((com.assets.Genesis)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(com.assets.Genesis other) {
      if (other == com.assets.Genesis.getDefaultInstance()) return this;
      if (mappablesBuilder_ == null) {
        if (!other.mappables_.isEmpty()) {
          if (mappables_.isEmpty()) {
            mappables_ = other.mappables_;
            bitField0_ = (bitField0_ & ~0x00000001);
          } else {
            ensureMappablesIsMutable();
            mappables_.addAll(other.mappables_);
          }
          onChanged();
        }
      } else {
        if (!other.mappables_.isEmpty()) {
          if (mappablesBuilder_.isEmpty()) {
            mappablesBuilder_.dispose();
            mappablesBuilder_ = null;
            mappables_ = other.mappables_;
            bitField0_ = (bitField0_ & ~0x00000001);
            mappablesBuilder_ = 
              com.google.protobuf.GeneratedMessageV3.alwaysUseFieldBuilders ?
                 getMappablesFieldBuilder() : null;
          } else {
            mappablesBuilder_.addAllMessages(other.mappables_);
          }
        }
      }
      if (other.hasParameterList()) {
        mergeParameterList(other.getParameterList());
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
              com.assets.Mappable m =
                  input.readMessage(
                      com.assets.Mappable.parser(),
                      extensionRegistry);
              if (mappablesBuilder_ == null) {
                ensureMappablesIsMutable();
                mappables_.add(m);
              } else {
                mappablesBuilder_.addMessage(m);
              }
              break;
            } // case 10
            case 18: {
              input.readMessage(
                  getParameterListFieldBuilder().getBuilder(),
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

    private java.util.List<com.assets.Mappable> mappables_ =
      java.util.Collections.emptyList();
    private void ensureMappablesIsMutable() {
      if (!((bitField0_ & 0x00000001) != 0)) {
        mappables_ = new java.util.ArrayList<com.assets.Mappable>(mappables_);
        bitField0_ |= 0x00000001;
       }
    }

    private com.google.protobuf.RepeatedFieldBuilderV3<
        com.assets.Mappable, com.assets.Mappable.Builder, com.assets.MappableOrBuilder> mappablesBuilder_;

    /**
     * <code>repeated .assets.Mappable mappables = 1 [json_name = "mappables"];</code>
     */
    public java.util.List<com.assets.Mappable> getMappablesList() {
      if (mappablesBuilder_ == null) {
        return java.util.Collections.unmodifiableList(mappables_);
      } else {
        return mappablesBuilder_.getMessageList();
      }
    }
    /**
     * <code>repeated .assets.Mappable mappables = 1 [json_name = "mappables"];</code>
     */
    public int getMappablesCount() {
      if (mappablesBuilder_ == null) {
        return mappables_.size();
      } else {
        return mappablesBuilder_.getCount();
      }
    }
    /**
     * <code>repeated .assets.Mappable mappables = 1 [json_name = "mappables"];</code>
     */
    public com.assets.Mappable getMappables(int index) {
      if (mappablesBuilder_ == null) {
        return mappables_.get(index);
      } else {
        return mappablesBuilder_.getMessage(index);
      }
    }
    /**
     * <code>repeated .assets.Mappable mappables = 1 [json_name = "mappables"];</code>
     */
    public Builder setMappables(
        int index, com.assets.Mappable value) {
      if (mappablesBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureMappablesIsMutable();
        mappables_.set(index, value);
        onChanged();
      } else {
        mappablesBuilder_.setMessage(index, value);
      }
      return this;
    }
    /**
     * <code>repeated .assets.Mappable mappables = 1 [json_name = "mappables"];</code>
     */
    public Builder setMappables(
        int index, com.assets.Mappable.Builder builderForValue) {
      if (mappablesBuilder_ == null) {
        ensureMappablesIsMutable();
        mappables_.set(index, builderForValue.build());
        onChanged();
      } else {
        mappablesBuilder_.setMessage(index, builderForValue.build());
      }
      return this;
    }
    /**
     * <code>repeated .assets.Mappable mappables = 1 [json_name = "mappables"];</code>
     */
    public Builder addMappables(com.assets.Mappable value) {
      if (mappablesBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureMappablesIsMutable();
        mappables_.add(value);
        onChanged();
      } else {
        mappablesBuilder_.addMessage(value);
      }
      return this;
    }
    /**
     * <code>repeated .assets.Mappable mappables = 1 [json_name = "mappables"];</code>
     */
    public Builder addMappables(
        int index, com.assets.Mappable value) {
      if (mappablesBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureMappablesIsMutable();
        mappables_.add(index, value);
        onChanged();
      } else {
        mappablesBuilder_.addMessage(index, value);
      }
      return this;
    }
    /**
     * <code>repeated .assets.Mappable mappables = 1 [json_name = "mappables"];</code>
     */
    public Builder addMappables(
        com.assets.Mappable.Builder builderForValue) {
      if (mappablesBuilder_ == null) {
        ensureMappablesIsMutable();
        mappables_.add(builderForValue.build());
        onChanged();
      } else {
        mappablesBuilder_.addMessage(builderForValue.build());
      }
      return this;
    }
    /**
     * <code>repeated .assets.Mappable mappables = 1 [json_name = "mappables"];</code>
     */
    public Builder addMappables(
        int index, com.assets.Mappable.Builder builderForValue) {
      if (mappablesBuilder_ == null) {
        ensureMappablesIsMutable();
        mappables_.add(index, builderForValue.build());
        onChanged();
      } else {
        mappablesBuilder_.addMessage(index, builderForValue.build());
      }
      return this;
    }
    /**
     * <code>repeated .assets.Mappable mappables = 1 [json_name = "mappables"];</code>
     */
    public Builder addAllMappables(
        java.lang.Iterable<? extends com.assets.Mappable> values) {
      if (mappablesBuilder_ == null) {
        ensureMappablesIsMutable();
        com.google.protobuf.AbstractMessageLite.Builder.addAll(
            values, mappables_);
        onChanged();
      } else {
        mappablesBuilder_.addAllMessages(values);
      }
      return this;
    }
    /**
     * <code>repeated .assets.Mappable mappables = 1 [json_name = "mappables"];</code>
     */
    public Builder clearMappables() {
      if (mappablesBuilder_ == null) {
        mappables_ = java.util.Collections.emptyList();
        bitField0_ = (bitField0_ & ~0x00000001);
        onChanged();
      } else {
        mappablesBuilder_.clear();
      }
      return this;
    }
    /**
     * <code>repeated .assets.Mappable mappables = 1 [json_name = "mappables"];</code>
     */
    public Builder removeMappables(int index) {
      if (mappablesBuilder_ == null) {
        ensureMappablesIsMutable();
        mappables_.remove(index);
        onChanged();
      } else {
        mappablesBuilder_.remove(index);
      }
      return this;
    }
    /**
     * <code>repeated .assets.Mappable mappables = 1 [json_name = "mappables"];</code>
     */
    public com.assets.Mappable.Builder getMappablesBuilder(
        int index) {
      return getMappablesFieldBuilder().getBuilder(index);
    }
    /**
     * <code>repeated .assets.Mappable mappables = 1 [json_name = "mappables"];</code>
     */
    public com.assets.MappableOrBuilder getMappablesOrBuilder(
        int index) {
      if (mappablesBuilder_ == null) {
        return mappables_.get(index);  } else {
        return mappablesBuilder_.getMessageOrBuilder(index);
      }
    }
    /**
     * <code>repeated .assets.Mappable mappables = 1 [json_name = "mappables"];</code>
     */
    public java.util.List<? extends com.assets.MappableOrBuilder> 
         getMappablesOrBuilderList() {
      if (mappablesBuilder_ != null) {
        return mappablesBuilder_.getMessageOrBuilderList();
      } else {
        return java.util.Collections.unmodifiableList(mappables_);
      }
    }
    /**
     * <code>repeated .assets.Mappable mappables = 1 [json_name = "mappables"];</code>
     */
    public com.assets.Mappable.Builder addMappablesBuilder() {
      return getMappablesFieldBuilder().addBuilder(
          com.assets.Mappable.getDefaultInstance());
    }
    /**
     * <code>repeated .assets.Mappable mappables = 1 [json_name = "mappables"];</code>
     */
    public com.assets.Mappable.Builder addMappablesBuilder(
        int index) {
      return getMappablesFieldBuilder().addBuilder(
          index, com.assets.Mappable.getDefaultInstance());
    }
    /**
     * <code>repeated .assets.Mappable mappables = 1 [json_name = "mappables"];</code>
     */
    public java.util.List<com.assets.Mappable.Builder> 
         getMappablesBuilderList() {
      return getMappablesFieldBuilder().getBuilderList();
    }
    private com.google.protobuf.RepeatedFieldBuilderV3<
        com.assets.Mappable, com.assets.Mappable.Builder, com.assets.MappableOrBuilder> 
        getMappablesFieldBuilder() {
      if (mappablesBuilder_ == null) {
        mappablesBuilder_ = new com.google.protobuf.RepeatedFieldBuilderV3<
            com.assets.Mappable, com.assets.Mappable.Builder, com.assets.MappableOrBuilder>(
                mappables_,
                ((bitField0_ & 0x00000001) != 0),
                getParentForChildren(),
                isClean());
        mappables_ = null;
      }
      return mappablesBuilder_;
    }

    private com.parameters.ParameterList parameterList_;
    private com.google.protobuf.SingleFieldBuilderV3<
        com.parameters.ParameterList, com.parameters.ParameterList.Builder, com.parameters.ParameterListOrBuilder> parameterListBuilder_;
    /**
     * <code>.parameters.ParameterList parameterList = 2 [json_name = "parameterList"];</code>
     * @return Whether the parameterList field is set.
     */
    public boolean hasParameterList() {
      return ((bitField0_ & 0x00000002) != 0);
    }
    /**
     * <code>.parameters.ParameterList parameterList = 2 [json_name = "parameterList"];</code>
     * @return The parameterList.
     */
    public com.parameters.ParameterList getParameterList() {
      if (parameterListBuilder_ == null) {
        return parameterList_ == null ? com.parameters.ParameterList.getDefaultInstance() : parameterList_;
      } else {
        return parameterListBuilder_.getMessage();
      }
    }
    /**
     * <code>.parameters.ParameterList parameterList = 2 [json_name = "parameterList"];</code>
     */
    public Builder setParameterList(com.parameters.ParameterList value) {
      if (parameterListBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        parameterList_ = value;
      } else {
        parameterListBuilder_.setMessage(value);
      }
      bitField0_ |= 0x00000002;
      onChanged();
      return this;
    }
    /**
     * <code>.parameters.ParameterList parameterList = 2 [json_name = "parameterList"];</code>
     */
    public Builder setParameterList(
        com.parameters.ParameterList.Builder builderForValue) {
      if (parameterListBuilder_ == null) {
        parameterList_ = builderForValue.build();
      } else {
        parameterListBuilder_.setMessage(builderForValue.build());
      }
      bitField0_ |= 0x00000002;
      onChanged();
      return this;
    }
    /**
     * <code>.parameters.ParameterList parameterList = 2 [json_name = "parameterList"];</code>
     */
    public Builder mergeParameterList(com.parameters.ParameterList value) {
      if (parameterListBuilder_ == null) {
        if (((bitField0_ & 0x00000002) != 0) &&
          parameterList_ != null &&
          parameterList_ != com.parameters.ParameterList.getDefaultInstance()) {
          getParameterListBuilder().mergeFrom(value);
        } else {
          parameterList_ = value;
        }
      } else {
        parameterListBuilder_.mergeFrom(value);
      }
      bitField0_ |= 0x00000002;
      onChanged();
      return this;
    }
    /**
     * <code>.parameters.ParameterList parameterList = 2 [json_name = "parameterList"];</code>
     */
    public Builder clearParameterList() {
      bitField0_ = (bitField0_ & ~0x00000002);
      parameterList_ = null;
      if (parameterListBuilder_ != null) {
        parameterListBuilder_.dispose();
        parameterListBuilder_ = null;
      }
      onChanged();
      return this;
    }
    /**
     * <code>.parameters.ParameterList parameterList = 2 [json_name = "parameterList"];</code>
     */
    public com.parameters.ParameterList.Builder getParameterListBuilder() {
      bitField0_ |= 0x00000002;
      onChanged();
      return getParameterListFieldBuilder().getBuilder();
    }
    /**
     * <code>.parameters.ParameterList parameterList = 2 [json_name = "parameterList"];</code>
     */
    public com.parameters.ParameterListOrBuilder getParameterListOrBuilder() {
      if (parameterListBuilder_ != null) {
        return parameterListBuilder_.getMessageOrBuilder();
      } else {
        return parameterList_ == null ?
            com.parameters.ParameterList.getDefaultInstance() : parameterList_;
      }
    }
    /**
     * <code>.parameters.ParameterList parameterList = 2 [json_name = "parameterList"];</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
        com.parameters.ParameterList, com.parameters.ParameterList.Builder, com.parameters.ParameterListOrBuilder> 
        getParameterListFieldBuilder() {
      if (parameterListBuilder_ == null) {
        parameterListBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
            com.parameters.ParameterList, com.parameters.ParameterList.Builder, com.parameters.ParameterListOrBuilder>(
                getParameterList(),
                getParentForChildren(),
                isClean());
        parameterList_ = null;
      }
      return parameterListBuilder_;
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


    // @@protoc_insertion_point(builder_scope:assets.Genesis)
  }

  // @@protoc_insertion_point(class_scope:assets.Genesis)
  private static final com.assets.Genesis DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new com.assets.Genesis();
  }

  public static com.assets.Genesis getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<Genesis>
      PARSER = new com.google.protobuf.AbstractParser<Genesis>() {
    @java.lang.Override
    public Genesis parsePartialFrom(
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

  public static com.google.protobuf.Parser<Genesis> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<Genesis> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public com.assets.Genesis getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

