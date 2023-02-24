// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: schema/parameters/base/parameter.v1.proto

package com.parameters;

/**
 * Protobuf type {@code parameters.Parameter}
 */
public final class Parameter extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:parameters.Parameter)
    ParameterOrBuilder {
private static final long serialVersionUID = 0L;
  // Use Parameter.newBuilder() to construct.
  private Parameter(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private Parameter() {
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new Parameter();
  }

  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return com.parameters.ParameterV1Proto.internal_static_parameters_Parameter_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return com.parameters.ParameterV1Proto.internal_static_parameters_Parameter_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            com.parameters.Parameter.class, com.parameters.Parameter.Builder.class);
  }

  public static final int META_PROPERTY_FIELD_NUMBER = 1;
  private com.properties.MetaProperty metaProperty_;
  /**
   * <code>.properties.MetaProperty meta_property = 1 [json_name = "metaProperty"];</code>
   * @return Whether the metaProperty field is set.
   */
  @java.lang.Override
  public boolean hasMetaProperty() {
    return metaProperty_ != null;
  }
  /**
   * <code>.properties.MetaProperty meta_property = 1 [json_name = "metaProperty"];</code>
   * @return The metaProperty.
   */
  @java.lang.Override
  public com.properties.MetaProperty getMetaProperty() {
    return metaProperty_ == null ? com.properties.MetaProperty.getDefaultInstance() : metaProperty_;
  }
  /**
   * <code>.properties.MetaProperty meta_property = 1 [json_name = "metaProperty"];</code>
   */
  @java.lang.Override
  public com.properties.MetaPropertyOrBuilder getMetaPropertyOrBuilder() {
    return metaProperty_ == null ? com.properties.MetaProperty.getDefaultInstance() : metaProperty_;
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
    if (metaProperty_ != null) {
      output.writeMessage(1, getMetaProperty());
    }
    getUnknownFields().writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (metaProperty_ != null) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(1, getMetaProperty());
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
    if (!(obj instanceof com.parameters.Parameter)) {
      return super.equals(obj);
    }
    com.parameters.Parameter other = (com.parameters.Parameter) obj;

    if (hasMetaProperty() != other.hasMetaProperty()) return false;
    if (hasMetaProperty()) {
      if (!getMetaProperty()
          .equals(other.getMetaProperty())) return false;
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
    if (hasMetaProperty()) {
      hash = (37 * hash) + META_PROPERTY_FIELD_NUMBER;
      hash = (53 * hash) + getMetaProperty().hashCode();
    }
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static com.parameters.Parameter parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.parameters.Parameter parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.parameters.Parameter parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.parameters.Parameter parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.parameters.Parameter parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.parameters.Parameter parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.parameters.Parameter parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.parameters.Parameter parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.parameters.Parameter parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static com.parameters.Parameter parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.parameters.Parameter parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.parameters.Parameter parseFrom(
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
  public static Builder newBuilder(com.parameters.Parameter prototype) {
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
   * Protobuf type {@code parameters.Parameter}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:parameters.Parameter)
      com.parameters.ParameterOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return com.parameters.ParameterV1Proto.internal_static_parameters_Parameter_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return com.parameters.ParameterV1Proto.internal_static_parameters_Parameter_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              com.parameters.Parameter.class, com.parameters.Parameter.Builder.class);
    }

    // Construct using com.parameters.Parameter.newBuilder()
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
      metaProperty_ = null;
      if (metaPropertyBuilder_ != null) {
        metaPropertyBuilder_.dispose();
        metaPropertyBuilder_ = null;
      }
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return com.parameters.ParameterV1Proto.internal_static_parameters_Parameter_descriptor;
    }

    @java.lang.Override
    public com.parameters.Parameter getDefaultInstanceForType() {
      return com.parameters.Parameter.getDefaultInstance();
    }

    @java.lang.Override
    public com.parameters.Parameter build() {
      com.parameters.Parameter result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public com.parameters.Parameter buildPartial() {
      com.parameters.Parameter result = new com.parameters.Parameter(this);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartial0(com.parameters.Parameter result) {
      int from_bitField0_ = bitField0_;
      if (((from_bitField0_ & 0x00000001) != 0)) {
        result.metaProperty_ = metaPropertyBuilder_ == null
            ? metaProperty_
            : metaPropertyBuilder_.build();
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
      if (other instanceof com.parameters.Parameter) {
        return mergeFrom((com.parameters.Parameter)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(com.parameters.Parameter other) {
      if (other == com.parameters.Parameter.getDefaultInstance()) return this;
      if (other.hasMetaProperty()) {
        mergeMetaProperty(other.getMetaProperty());
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
                  getMetaPropertyFieldBuilder().getBuilder(),
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

    private com.properties.MetaProperty metaProperty_;
    private com.google.protobuf.SingleFieldBuilderV3<
        com.properties.MetaProperty, com.properties.MetaProperty.Builder, com.properties.MetaPropertyOrBuilder> metaPropertyBuilder_;
    /**
     * <code>.properties.MetaProperty meta_property = 1 [json_name = "metaProperty"];</code>
     * @return Whether the metaProperty field is set.
     */
    public boolean hasMetaProperty() {
      return ((bitField0_ & 0x00000001) != 0);
    }
    /**
     * <code>.properties.MetaProperty meta_property = 1 [json_name = "metaProperty"];</code>
     * @return The metaProperty.
     */
    public com.properties.MetaProperty getMetaProperty() {
      if (metaPropertyBuilder_ == null) {
        return metaProperty_ == null ? com.properties.MetaProperty.getDefaultInstance() : metaProperty_;
      } else {
        return metaPropertyBuilder_.getMessage();
      }
    }
    /**
     * <code>.properties.MetaProperty meta_property = 1 [json_name = "metaProperty"];</code>
     */
    public Builder setMetaProperty(com.properties.MetaProperty value) {
      if (metaPropertyBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        metaProperty_ = value;
      } else {
        metaPropertyBuilder_.setMessage(value);
      }
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <code>.properties.MetaProperty meta_property = 1 [json_name = "metaProperty"];</code>
     */
    public Builder setMetaProperty(
        com.properties.MetaProperty.Builder builderForValue) {
      if (metaPropertyBuilder_ == null) {
        metaProperty_ = builderForValue.build();
      } else {
        metaPropertyBuilder_.setMessage(builderForValue.build());
      }
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <code>.properties.MetaProperty meta_property = 1 [json_name = "metaProperty"];</code>
     */
    public Builder mergeMetaProperty(com.properties.MetaProperty value) {
      if (metaPropertyBuilder_ == null) {
        if (((bitField0_ & 0x00000001) != 0) &&
          metaProperty_ != null &&
          metaProperty_ != com.properties.MetaProperty.getDefaultInstance()) {
          getMetaPropertyBuilder().mergeFrom(value);
        } else {
          metaProperty_ = value;
        }
      } else {
        metaPropertyBuilder_.mergeFrom(value);
      }
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <code>.properties.MetaProperty meta_property = 1 [json_name = "metaProperty"];</code>
     */
    public Builder clearMetaProperty() {
      bitField0_ = (bitField0_ & ~0x00000001);
      metaProperty_ = null;
      if (metaPropertyBuilder_ != null) {
        metaPropertyBuilder_.dispose();
        metaPropertyBuilder_ = null;
      }
      onChanged();
      return this;
    }
    /**
     * <code>.properties.MetaProperty meta_property = 1 [json_name = "metaProperty"];</code>
     */
    public com.properties.MetaProperty.Builder getMetaPropertyBuilder() {
      bitField0_ |= 0x00000001;
      onChanged();
      return getMetaPropertyFieldBuilder().getBuilder();
    }
    /**
     * <code>.properties.MetaProperty meta_property = 1 [json_name = "metaProperty"];</code>
     */
    public com.properties.MetaPropertyOrBuilder getMetaPropertyOrBuilder() {
      if (metaPropertyBuilder_ != null) {
        return metaPropertyBuilder_.getMessageOrBuilder();
      } else {
        return metaProperty_ == null ?
            com.properties.MetaProperty.getDefaultInstance() : metaProperty_;
      }
    }
    /**
     * <code>.properties.MetaProperty meta_property = 1 [json_name = "metaProperty"];</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
        com.properties.MetaProperty, com.properties.MetaProperty.Builder, com.properties.MetaPropertyOrBuilder> 
        getMetaPropertyFieldBuilder() {
      if (metaPropertyBuilder_ == null) {
        metaPropertyBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
            com.properties.MetaProperty, com.properties.MetaProperty.Builder, com.properties.MetaPropertyOrBuilder>(
                getMetaProperty(),
                getParentForChildren(),
                isClean());
        metaProperty_ = null;
      }
      return metaPropertyBuilder_;
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


    // @@protoc_insertion_point(builder_scope:parameters.Parameter)
  }

  // @@protoc_insertion_point(class_scope:parameters.Parameter)
  private static final com.parameters.Parameter DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new com.parameters.Parameter();
  }

  public static com.parameters.Parameter getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<Parameter>
      PARSER = new com.google.protobuf.AbstractParser<Parameter>() {
    @java.lang.Override
    public Parameter parsePartialFrom(
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

  public static com.google.protobuf.Parser<Parameter> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<Parameter> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public com.parameters.Parameter getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}
