// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: schema/qualified/base/immutables.v1.proto

package com.qualified;

/**
 * Protobuf type {@code qualified.Immutables}
 */
public final class Immutables extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:qualified.Immutables)
    ImmutablesOrBuilder {
private static final long serialVersionUID = 0L;
  // Use Immutables.newBuilder() to construct.
  private Immutables(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private Immutables() {
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new Immutables();
  }

  @java.lang.Override
  public final com.google.protobuf.UnknownFieldSet
  getUnknownFields() {
    return this.unknownFields;
  }
  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return com.qualified.ImmutablesV1Proto.internal_static_qualified_Immutables_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return com.qualified.ImmutablesV1Proto.internal_static_qualified_Immutables_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            com.qualified.Immutables.class, com.qualified.Immutables.Builder.class);
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
    return getPropertyList();
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
    if (!(obj instanceof com.qualified.Immutables)) {
      return super.equals(obj);
    }
    com.qualified.Immutables other = (com.qualified.Immutables) obj;

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

  public static com.qualified.Immutables parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.qualified.Immutables parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.qualified.Immutables parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.qualified.Immutables parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.qualified.Immutables parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.qualified.Immutables parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.qualified.Immutables parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.qualified.Immutables parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.qualified.Immutables parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static com.qualified.Immutables parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.qualified.Immutables parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.qualified.Immutables parseFrom(
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
  public static Builder newBuilder(com.qualified.Immutables prototype) {
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
   * Protobuf type {@code qualified.Immutables}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:qualified.Immutables)
      com.qualified.ImmutablesOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return com.qualified.ImmutablesV1Proto.internal_static_qualified_Immutables_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return com.qualified.ImmutablesV1Proto.internal_static_qualified_Immutables_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              com.qualified.Immutables.class, com.qualified.Immutables.Builder.class);
    }

    // Construct using com.qualified.Immutables.newBuilder()
    private Builder() {

    }

    private Builder(
        com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
      super(parent);

    }
    @java.lang.Override
    public Builder clear() {
      super.clear();
      if (propertyListBuilder_ == null) {
        propertyList_ = null;
      } else {
        propertyList_ = null;
        propertyListBuilder_ = null;
      }
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return com.qualified.ImmutablesV1Proto.internal_static_qualified_Immutables_descriptor;
    }

    @java.lang.Override
    public com.qualified.Immutables getDefaultInstanceForType() {
      return com.qualified.Immutables.getDefaultInstance();
    }

    @java.lang.Override
    public com.qualified.Immutables build() {
      com.qualified.Immutables result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public com.qualified.Immutables buildPartial() {
      com.qualified.Immutables result = new com.qualified.Immutables(this);
      if (propertyListBuilder_ == null) {
        result.propertyList_ = propertyList_;
      } else {
        result.propertyList_ = propertyListBuilder_.build();
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
      if (other instanceof com.qualified.Immutables) {
        return mergeFrom((com.qualified.Immutables)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(com.qualified.Immutables other) {
      if (other == com.qualified.Immutables.getDefaultInstance()) return this;
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

    private com.lists.PropertyList propertyList_;
    private com.google.protobuf.SingleFieldBuilderV3<
        com.lists.PropertyList, com.lists.PropertyList.Builder, com.lists.PropertyListOrBuilder> propertyListBuilder_;
    /**
     * <code>.lists.PropertyList property_list = 1 [json_name = "propertyList"];</code>
     * @return Whether the propertyList field is set.
     */
    public boolean hasPropertyList() {
      return propertyListBuilder_ != null || propertyList_ != null;
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
        onChanged();
      } else {
        propertyListBuilder_.setMessage(value);
      }

      return this;
    }
    /**
     * <code>.lists.PropertyList property_list = 1 [json_name = "propertyList"];</code>
     */
    public Builder setPropertyList(
        com.lists.PropertyList.Builder builderForValue) {
      if (propertyListBuilder_ == null) {
        propertyList_ = builderForValue.build();
        onChanged();
      } else {
        propertyListBuilder_.setMessage(builderForValue.build());
      }

      return this;
    }
    /**
     * <code>.lists.PropertyList property_list = 1 [json_name = "propertyList"];</code>
     */
    public Builder mergePropertyList(com.lists.PropertyList value) {
      if (propertyListBuilder_ == null) {
        if (propertyList_ != null) {
          propertyList_ =
            com.lists.PropertyList.newBuilder(propertyList_).mergeFrom(value).buildPartial();
        } else {
          propertyList_ = value;
        }
        onChanged();
      } else {
        propertyListBuilder_.mergeFrom(value);
      }

      return this;
    }
    /**
     * <code>.lists.PropertyList property_list = 1 [json_name = "propertyList"];</code>
     */
    public Builder clearPropertyList() {
      if (propertyListBuilder_ == null) {
        propertyList_ = null;
        onChanged();
      } else {
        propertyList_ = null;
        propertyListBuilder_ = null;
      }

      return this;
    }
    /**
     * <code>.lists.PropertyList property_list = 1 [json_name = "propertyList"];</code>
     */
    public com.lists.PropertyList.Builder getPropertyListBuilder() {
      
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


    // @@protoc_insertion_point(builder_scope:qualified.Immutables)
  }

  // @@protoc_insertion_point(class_scope:qualified.Immutables)
  private static final com.qualified.Immutables DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new com.qualified.Immutables();
  }

  public static com.qualified.Immutables getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<Immutables>
      PARSER = new com.google.protobuf.AbstractParser<Immutables>() {
    @java.lang.Override
    public Immutables parsePartialFrom(
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

  public static com.google.protobuf.Parser<Immutables> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<Immutables> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public com.qualified.Immutables getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

