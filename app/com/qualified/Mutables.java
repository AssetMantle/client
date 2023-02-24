// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: schema/qualified/base/mutables.v1.proto

package com.qualified;

/**
 * Protobuf type {@code qualified.Mutables}
 */
public final class Mutables extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:qualified.Mutables)
    MutablesOrBuilder {
private static final long serialVersionUID = 0L;
  // Use Mutables.newBuilder() to construct.
  private Mutables(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private Mutables() {
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new Mutables();
  }

  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return com.qualified.MutablesV1Proto.internal_static_qualified_Mutables_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return com.qualified.MutablesV1Proto.internal_static_qualified_Mutables_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            com.qualified.Mutables.class, com.qualified.Mutables.Builder.class);
  }

  public static final int PROPERTY_LIST_FIELD_NUMBER = 1;
  private com.lists.PropertyList propertyList_;
  /**
   * <code>.lists.PropertyList property_list = 1 [json_name = "propertyList"];</code>
   * @return Whether the propertyList field is set.
   */
  @java.lang.Override
  public boolean hasPropertyList() {
    return propertyList_ != null;
  }
  /**
   * <code>.lists.PropertyList property_list = 1 [json_name = "propertyList"];</code>
   * @return The propertyList.
   */
  @java.lang.Override
  public com.lists.PropertyList getPropertyList() {
    return propertyList_ == null ? com.lists.PropertyList.getDefaultInstance() : propertyList_;
  }
  /**
   * <code>.lists.PropertyList property_list = 1 [json_name = "propertyList"];</code>
   */
  @java.lang.Override
  public com.lists.PropertyListOrBuilder getPropertyListOrBuilder() {
    return propertyList_ == null ? com.lists.PropertyList.getDefaultInstance() : propertyList_;
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
    if (propertyList_ != null) {
      output.writeMessage(1, getPropertyList());
    }
    getUnknownFields().writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (propertyList_ != null) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(1, getPropertyList());
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
    if (!(obj instanceof com.qualified.Mutables)) {
      return super.equals(obj);
    }
    com.qualified.Mutables other = (com.qualified.Mutables) obj;

    if (hasPropertyList() != other.hasPropertyList()) return false;
    if (hasPropertyList()) {
      if (!getPropertyList()
          .equals(other.getPropertyList())) return false;
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
    if (hasPropertyList()) {
      hash = (37 * hash) + PROPERTY_LIST_FIELD_NUMBER;
      hash = (53 * hash) + getPropertyList().hashCode();
    }
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static com.qualified.Mutables parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.qualified.Mutables parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.qualified.Mutables parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.qualified.Mutables parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.qualified.Mutables parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.qualified.Mutables parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.qualified.Mutables parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.qualified.Mutables parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.qualified.Mutables parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static com.qualified.Mutables parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.qualified.Mutables parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.qualified.Mutables parseFrom(
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
  public static Builder newBuilder(com.qualified.Mutables prototype) {
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
   * Protobuf type {@code qualified.Mutables}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:qualified.Mutables)
      com.qualified.MutablesOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return com.qualified.MutablesV1Proto.internal_static_qualified_Mutables_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return com.qualified.MutablesV1Proto.internal_static_qualified_Mutables_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              com.qualified.Mutables.class, com.qualified.Mutables.Builder.class);
    }

    // Construct using com.qualified.Mutables.newBuilder()
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
      propertyList_ = null;
      if (propertyListBuilder_ != null) {
        propertyListBuilder_.dispose();
        propertyListBuilder_ = null;
      }
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return com.qualified.MutablesV1Proto.internal_static_qualified_Mutables_descriptor;
    }

    @java.lang.Override
    public com.qualified.Mutables getDefaultInstanceForType() {
      return com.qualified.Mutables.getDefaultInstance();
    }

    @java.lang.Override
    public com.qualified.Mutables build() {
      com.qualified.Mutables result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public com.qualified.Mutables buildPartial() {
      com.qualified.Mutables result = new com.qualified.Mutables(this);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartial0(com.qualified.Mutables result) {
      int from_bitField0_ = bitField0_;
      if (((from_bitField0_ & 0x00000001) != 0)) {
        result.propertyList_ = propertyListBuilder_ == null
            ? propertyList_
            : propertyListBuilder_.build();
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
      if (other instanceof com.qualified.Mutables) {
        return mergeFrom((com.qualified.Mutables)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(com.qualified.Mutables other) {
      if (other == com.qualified.Mutables.getDefaultInstance()) return this;
      if (other.hasPropertyList()) {
        mergePropertyList(other.getPropertyList());
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
                  getPropertyListFieldBuilder().getBuilder(),
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

    private com.lists.PropertyList propertyList_;
    private com.google.protobuf.SingleFieldBuilderV3<
        com.lists.PropertyList, com.lists.PropertyList.Builder, com.lists.PropertyListOrBuilder> propertyListBuilder_;
    /**
     * <code>.lists.PropertyList property_list = 1 [json_name = "propertyList"];</code>
     * @return Whether the propertyList field is set.
     */
    public boolean hasPropertyList() {
      return ((bitField0_ & 0x00000001) != 0);
    }
    /**
     * <code>.lists.PropertyList property_list = 1 [json_name = "propertyList"];</code>
     * @return The propertyList.
     */
    public com.lists.PropertyList getPropertyList() {
      if (propertyListBuilder_ == null) {
        return propertyList_ == null ? com.lists.PropertyList.getDefaultInstance() : propertyList_;
      } else {
        return propertyListBuilder_.getMessage();
      }
    }
    /**
     * <code>.lists.PropertyList property_list = 1 [json_name = "propertyList"];</code>
     */
    public Builder setPropertyList(com.lists.PropertyList value) {
      if (propertyListBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        propertyList_ = value;
      } else {
        propertyListBuilder_.setMessage(value);
      }
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <code>.lists.PropertyList property_list = 1 [json_name = "propertyList"];</code>
     */
    public Builder setPropertyList(
        com.lists.PropertyList.Builder builderForValue) {
      if (propertyListBuilder_ == null) {
        propertyList_ = builderForValue.build();
      } else {
        propertyListBuilder_.setMessage(builderForValue.build());
      }
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <code>.lists.PropertyList property_list = 1 [json_name = "propertyList"];</code>
     */
    public Builder mergePropertyList(com.lists.PropertyList value) {
      if (propertyListBuilder_ == null) {
        if (((bitField0_ & 0x00000001) != 0) &&
          propertyList_ != null &&
          propertyList_ != com.lists.PropertyList.getDefaultInstance()) {
          getPropertyListBuilder().mergeFrom(value);
        } else {
          propertyList_ = value;
        }
      } else {
        propertyListBuilder_.mergeFrom(value);
      }
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <code>.lists.PropertyList property_list = 1 [json_name = "propertyList"];</code>
     */
    public Builder clearPropertyList() {
      bitField0_ = (bitField0_ & ~0x00000001);
      propertyList_ = null;
      if (propertyListBuilder_ != null) {
        propertyListBuilder_.dispose();
        propertyListBuilder_ = null;
      }
      onChanged();
      return this;
    }
    /**
     * <code>.lists.PropertyList property_list = 1 [json_name = "propertyList"];</code>
     */
    public com.lists.PropertyList.Builder getPropertyListBuilder() {
      bitField0_ |= 0x00000001;
      onChanged();
      return getPropertyListFieldBuilder().getBuilder();
    }
    /**
     * <code>.lists.PropertyList property_list = 1 [json_name = "propertyList"];</code>
     */
    public com.lists.PropertyListOrBuilder getPropertyListOrBuilder() {
      if (propertyListBuilder_ != null) {
        return propertyListBuilder_.getMessageOrBuilder();
      } else {
        return propertyList_ == null ?
            com.lists.PropertyList.getDefaultInstance() : propertyList_;
      }
    }
    /**
     * <code>.lists.PropertyList property_list = 1 [json_name = "propertyList"];</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
        com.lists.PropertyList, com.lists.PropertyList.Builder, com.lists.PropertyListOrBuilder> 
        getPropertyListFieldBuilder() {
      if (propertyListBuilder_ == null) {
        propertyListBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
            com.lists.PropertyList, com.lists.PropertyList.Builder, com.lists.PropertyListOrBuilder>(
                getPropertyList(),
                getParentForChildren(),
                isClean());
        propertyList_ = null;
      }
      return propertyListBuilder_;
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


    // @@protoc_insertion_point(builder_scope:qualified.Mutables)
  }

  // @@protoc_insertion_point(class_scope:qualified.Mutables)
  private static final com.qualified.Mutables DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new com.qualified.Mutables();
  }

  public static com.qualified.Mutables getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<Mutables>
      PARSER = new com.google.protobuf.AbstractParser<Mutables>() {
    @java.lang.Override
    public Mutables parsePartialFrom(
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

  public static com.google.protobuf.Parser<Mutables> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<Mutables> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public com.qualified.Mutables getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}
