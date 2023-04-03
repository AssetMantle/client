// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: modules/classifications/internal/mappable/mappable.proto

package com.classifications;

/**
 * Protobuf type {@code classifications.Mappable}
 */
public final class Mappable extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:classifications.Mappable)
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

  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return com.classifications.MappableProto.internal_static_classifications_Mappable_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return com.classifications.MappableProto.internal_static_classifications_Mappable_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            com.classifications.Mappable.class, com.classifications.Mappable.Builder.class);
  }

  public static final int CLASSIFICATION_FIELD_NUMBER = 1;
  private com.documents.Document classification_;
  /**
   * <code>.documents.Document classification = 1 [json_name = "classification"];</code>
   * @return Whether the classification field is set.
   */
  @java.lang.Override
  public boolean hasClassification() {
    return classification_ != null;
  }
  /**
   * <code>.documents.Document classification = 1 [json_name = "classification"];</code>
   * @return The classification.
   */
  @java.lang.Override
  public com.documents.Document getClassification() {
    return classification_ == null ? com.documents.Document.getDefaultInstance() : classification_;
  }
  /**
   * <code>.documents.Document classification = 1 [json_name = "classification"];</code>
   */
  @java.lang.Override
  public com.documents.DocumentOrBuilder getClassificationOrBuilder() {
    return classification_ == null ? com.documents.Document.getDefaultInstance() : classification_;
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
    if (classification_ != null) {
      output.writeMessage(1, getClassification());
    }
    getUnknownFields().writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (classification_ != null) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(1, getClassification());
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
    if (!(obj instanceof com.classifications.Mappable)) {
      return super.equals(obj);
    }
    com.classifications.Mappable other = (com.classifications.Mappable) obj;

    if (hasClassification() != other.hasClassification()) return false;
    if (hasClassification()) {
      if (!getClassification()
          .equals(other.getClassification())) return false;
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
    if (hasClassification()) {
      hash = (37 * hash) + CLASSIFICATION_FIELD_NUMBER;
      hash = (53 * hash) + getClassification().hashCode();
    }
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static com.classifications.Mappable parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.classifications.Mappable parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.classifications.Mappable parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.classifications.Mappable parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.classifications.Mappable parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.classifications.Mappable parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.classifications.Mappable parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.classifications.Mappable parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.classifications.Mappable parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static com.classifications.Mappable parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.classifications.Mappable parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.classifications.Mappable parseFrom(
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
  public static Builder newBuilder(com.classifications.Mappable prototype) {
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
   * Protobuf type {@code classifications.Mappable}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:classifications.Mappable)
      com.classifications.MappableOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return com.classifications.MappableProto.internal_static_classifications_Mappable_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return com.classifications.MappableProto.internal_static_classifications_Mappable_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              com.classifications.Mappable.class, com.classifications.Mappable.Builder.class);
    }

    // Construct using com.classifications.Mappable.newBuilder()
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
      classification_ = null;
      if (classificationBuilder_ != null) {
        classificationBuilder_.dispose();
        classificationBuilder_ = null;
      }
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return com.classifications.MappableProto.internal_static_classifications_Mappable_descriptor;
    }

    @java.lang.Override
    public com.classifications.Mappable getDefaultInstanceForType() {
      return com.classifications.Mappable.getDefaultInstance();
    }

    @java.lang.Override
    public com.classifications.Mappable build() {
      com.classifications.Mappable result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public com.classifications.Mappable buildPartial() {
      com.classifications.Mappable result = new com.classifications.Mappable(this);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartial0(com.classifications.Mappable result) {
      int from_bitField0_ = bitField0_;
      if (((from_bitField0_ & 0x00000001) != 0)) {
        result.classification_ = classificationBuilder_ == null
            ? classification_
            : classificationBuilder_.build();
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
      if (other instanceof com.classifications.Mappable) {
        return mergeFrom((com.classifications.Mappable)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(com.classifications.Mappable other) {
      if (other == com.classifications.Mappable.getDefaultInstance()) return this;
      if (other.hasClassification()) {
        mergeClassification(other.getClassification());
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
                  getClassificationFieldBuilder().getBuilder(),
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

    private com.documents.Document classification_;
    private com.google.protobuf.SingleFieldBuilderV3<
        com.documents.Document, com.documents.Document.Builder, com.documents.DocumentOrBuilder> classificationBuilder_;
    /**
     * <code>.documents.Document classification = 1 [json_name = "classification"];</code>
     * @return Whether the classification field is set.
     */
    public boolean hasClassification() {
      return ((bitField0_ & 0x00000001) != 0);
    }
    /**
     * <code>.documents.Document classification = 1 [json_name = "classification"];</code>
     * @return The classification.
     */
    public com.documents.Document getClassification() {
      if (classificationBuilder_ == null) {
        return classification_ == null ? com.documents.Document.getDefaultInstance() : classification_;
      } else {
        return classificationBuilder_.getMessage();
      }
    }
    /**
     * <code>.documents.Document classification = 1 [json_name = "classification"];</code>
     */
    public Builder setClassification(com.documents.Document value) {
      if (classificationBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        classification_ = value;
      } else {
        classificationBuilder_.setMessage(value);
      }
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <code>.documents.Document classification = 1 [json_name = "classification"];</code>
     */
    public Builder setClassification(
        com.documents.Document.Builder builderForValue) {
      if (classificationBuilder_ == null) {
        classification_ = builderForValue.build();
      } else {
        classificationBuilder_.setMessage(builderForValue.build());
      }
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <code>.documents.Document classification = 1 [json_name = "classification"];</code>
     */
    public Builder mergeClassification(com.documents.Document value) {
      if (classificationBuilder_ == null) {
        if (((bitField0_ & 0x00000001) != 0) &&
          classification_ != null &&
          classification_ != com.documents.Document.getDefaultInstance()) {
          getClassificationBuilder().mergeFrom(value);
        } else {
          classification_ = value;
        }
      } else {
        classificationBuilder_.mergeFrom(value);
      }
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <code>.documents.Document classification = 1 [json_name = "classification"];</code>
     */
    public Builder clearClassification() {
      bitField0_ = (bitField0_ & ~0x00000001);
      classification_ = null;
      if (classificationBuilder_ != null) {
        classificationBuilder_.dispose();
        classificationBuilder_ = null;
      }
      onChanged();
      return this;
    }
    /**
     * <code>.documents.Document classification = 1 [json_name = "classification"];</code>
     */
    public com.documents.Document.Builder getClassificationBuilder() {
      bitField0_ |= 0x00000001;
      onChanged();
      return getClassificationFieldBuilder().getBuilder();
    }
    /**
     * <code>.documents.Document classification = 1 [json_name = "classification"];</code>
     */
    public com.documents.DocumentOrBuilder getClassificationOrBuilder() {
      if (classificationBuilder_ != null) {
        return classificationBuilder_.getMessageOrBuilder();
      } else {
        return classification_ == null ?
            com.documents.Document.getDefaultInstance() : classification_;
      }
    }
    /**
     * <code>.documents.Document classification = 1 [json_name = "classification"];</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
        com.documents.Document, com.documents.Document.Builder, com.documents.DocumentOrBuilder> 
        getClassificationFieldBuilder() {
      if (classificationBuilder_ == null) {
        classificationBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
            com.documents.Document, com.documents.Document.Builder, com.documents.DocumentOrBuilder>(
                getClassification(),
                getParentForChildren(),
                isClean());
        classification_ = null;
      }
      return classificationBuilder_;
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


    // @@protoc_insertion_point(builder_scope:classifications.Mappable)
  }

  // @@protoc_insertion_point(class_scope:classifications.Mappable)
  private static final com.classifications.Mappable DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new com.classifications.Mappable();
  }

  public static com.classifications.Mappable getDefaultInstance() {
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
  public com.classifications.Mappable getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

