// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: modules/maintainers/internal/mappable/mappable.v1.proto

package com.maintainers;

/**
 * Protobuf type {@code maintainers.Mappable}
 */
public final class Mappable extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:maintainers.Mappable)
    MappableOrBuilder {
private static final long serialVersionUID = 0L;
  // Use Mappable.newBuilder() to construct.
  private Mappable(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private Mappable() {
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new Mappable();
  }

  @java.lang.Override
  public final com.google.protobuf.UnknownFieldSet
  getUnknownFields() {
    return this.unknownFields;
  }
  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return com.maintainers.MappableV1Proto.internal_static_maintainers_Mappable_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return com.maintainers.MappableV1Proto.internal_static_maintainers_Mappable_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            com.maintainers.Mappable.class, com.maintainers.Mappable.Builder.class);
  }

  public static final int MAINTAINER_FIELD_NUMBER = 1;
  private com.documents.Document maintainer_;
  /**
   * <code>.documents.Document maintainer = 1 [json_name = "maintainer"];</code>
   * @return Whether the maintainer field is set.
   */
  @java.lang.Override
  public boolean hasMaintainer() {
    return maintainer_ != null;
  }
  /**
   * <code>.documents.Document maintainer = 1 [json_name = "maintainer"];</code>
   * @return The maintainer.
   */
  @java.lang.Override
  public com.documents.Document getMaintainer() {
    return maintainer_ == null ? com.documents.Document.getDefaultInstance() : maintainer_;
  }
  /**
   * <code>.documents.Document maintainer = 1 [json_name = "maintainer"];</code>
   */
  @java.lang.Override
  public com.documents.DocumentOrBuilder getMaintainerOrBuilder() {
    return getMaintainer();
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
    if (maintainer_ != null) {
      output.writeMessage(1, getMaintainer());
    }
    getUnknownFields().writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (maintainer_ != null) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(1, getMaintainer());
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
    if (!(obj instanceof com.maintainers.Mappable)) {
      return super.equals(obj);
    }
    com.maintainers.Mappable other = (com.maintainers.Mappable) obj;

    if (hasMaintainer() != other.hasMaintainer()) return false;
    if (hasMaintainer()) {
      if (!getMaintainer()
          .equals(other.getMaintainer())) return false;
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
    if (hasMaintainer()) {
      hash = (37 * hash) + MAINTAINER_FIELD_NUMBER;
      hash = (53 * hash) + getMaintainer().hashCode();
    }
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static com.maintainers.Mappable parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.maintainers.Mappable parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.maintainers.Mappable parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.maintainers.Mappable parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.maintainers.Mappable parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.maintainers.Mappable parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.maintainers.Mappable parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.maintainers.Mappable parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.maintainers.Mappable parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static com.maintainers.Mappable parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.maintainers.Mappable parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.maintainers.Mappable parseFrom(
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
  public static Builder newBuilder(com.maintainers.Mappable prototype) {
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
   * Protobuf type {@code maintainers.Mappable}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:maintainers.Mappable)
      com.maintainers.MappableOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return com.maintainers.MappableV1Proto.internal_static_maintainers_Mappable_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return com.maintainers.MappableV1Proto.internal_static_maintainers_Mappable_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              com.maintainers.Mappable.class, com.maintainers.Mappable.Builder.class);
    }

    // Construct using com.maintainers.Mappable.newBuilder()
    private Builder() {

    }

    private Builder(
        com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
      super(parent);

    }
    @java.lang.Override
    public Builder clear() {
      super.clear();
      if (maintainerBuilder_ == null) {
        maintainer_ = null;
      } else {
        maintainer_ = null;
        maintainerBuilder_ = null;
      }
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return com.maintainers.MappableV1Proto.internal_static_maintainers_Mappable_descriptor;
    }

    @java.lang.Override
    public com.maintainers.Mappable getDefaultInstanceForType() {
      return com.maintainers.Mappable.getDefaultInstance();
    }

    @java.lang.Override
    public com.maintainers.Mappable build() {
      com.maintainers.Mappable result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public com.maintainers.Mappable buildPartial() {
      com.maintainers.Mappable result = new com.maintainers.Mappable(this);
      if (maintainerBuilder_ == null) {
        result.maintainer_ = maintainer_;
      } else {
        result.maintainer_ = maintainerBuilder_.build();
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
      if (other instanceof com.maintainers.Mappable) {
        return mergeFrom((com.maintainers.Mappable)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(com.maintainers.Mappable other) {
      if (other == com.maintainers.Mappable.getDefaultInstance()) return this;
      if (other.hasMaintainer()) {
        mergeMaintainer(other.getMaintainer());
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
                  getMaintainerFieldBuilder().getBuilder(),
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

    private com.documents.Document maintainer_;
    private com.google.protobuf.SingleFieldBuilderV3<
        com.documents.Document, com.documents.Document.Builder, com.documents.DocumentOrBuilder> maintainerBuilder_;
    /**
     * <code>.documents.Document maintainer = 1 [json_name = "maintainer"];</code>
     * @return Whether the maintainer field is set.
     */
    public boolean hasMaintainer() {
      return maintainerBuilder_ != null || maintainer_ != null;
    }
    /**
     * <code>.documents.Document maintainer = 1 [json_name = "maintainer"];</code>
     * @return The maintainer.
     */
    public com.documents.Document getMaintainer() {
      if (maintainerBuilder_ == null) {
        return maintainer_ == null ? com.documents.Document.getDefaultInstance() : maintainer_;
      } else {
        return maintainerBuilder_.getMessage();
      }
    }
    /**
     * <code>.documents.Document maintainer = 1 [json_name = "maintainer"];</code>
     */
    public Builder setMaintainer(com.documents.Document value) {
      if (maintainerBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        maintainer_ = value;
        onChanged();
      } else {
        maintainerBuilder_.setMessage(value);
      }

      return this;
    }
    /**
     * <code>.documents.Document maintainer = 1 [json_name = "maintainer"];</code>
     */
    public Builder setMaintainer(
        com.documents.Document.Builder builderForValue) {
      if (maintainerBuilder_ == null) {
        maintainer_ = builderForValue.build();
        onChanged();
      } else {
        maintainerBuilder_.setMessage(builderForValue.build());
      }

      return this;
    }
    /**
     * <code>.documents.Document maintainer = 1 [json_name = "maintainer"];</code>
     */
    public Builder mergeMaintainer(com.documents.Document value) {
      if (maintainerBuilder_ == null) {
        if (maintainer_ != null) {
          maintainer_ =
            com.documents.Document.newBuilder(maintainer_).mergeFrom(value).buildPartial();
        } else {
          maintainer_ = value;
        }
        onChanged();
      } else {
        maintainerBuilder_.mergeFrom(value);
      }

      return this;
    }
    /**
     * <code>.documents.Document maintainer = 1 [json_name = "maintainer"];</code>
     */
    public Builder clearMaintainer() {
      if (maintainerBuilder_ == null) {
        maintainer_ = null;
        onChanged();
      } else {
        maintainer_ = null;
        maintainerBuilder_ = null;
      }

      return this;
    }
    /**
     * <code>.documents.Document maintainer = 1 [json_name = "maintainer"];</code>
     */
    public com.documents.Document.Builder getMaintainerBuilder() {
      
      onChanged();
      return getMaintainerFieldBuilder().getBuilder();
    }
    /**
     * <code>.documents.Document maintainer = 1 [json_name = "maintainer"];</code>
     */
    public com.documents.DocumentOrBuilder getMaintainerOrBuilder() {
      if (maintainerBuilder_ != null) {
        return maintainerBuilder_.getMessageOrBuilder();
      } else {
        return maintainer_ == null ?
            com.documents.Document.getDefaultInstance() : maintainer_;
      }
    }
    /**
     * <code>.documents.Document maintainer = 1 [json_name = "maintainer"];</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
        com.documents.Document, com.documents.Document.Builder, com.documents.DocumentOrBuilder> 
        getMaintainerFieldBuilder() {
      if (maintainerBuilder_ == null) {
        maintainerBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
            com.documents.Document, com.documents.Document.Builder, com.documents.DocumentOrBuilder>(
                getMaintainer(),
                getParentForChildren(),
                isClean());
        maintainer_ = null;
      }
      return maintainerBuilder_;
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


    // @@protoc_insertion_point(builder_scope:maintainers.Mappable)
  }

  // @@protoc_insertion_point(class_scope:maintainers.Mappable)
  private static final com.maintainers.Mappable DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new com.maintainers.Mappable();
  }

  public static com.maintainers.Mappable getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<Mappable>
      PARSER = new com.google.protobuf.AbstractParser<Mappable>() {
    @java.lang.Override
    public Mappable parsePartialFrom(
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

  public static com.google.protobuf.Parser<Mappable> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<Mappable> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public com.maintainers.Mappable getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

