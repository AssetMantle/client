// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: cosmos/base/reflection/v2alpha1/reflection.proto

package com.cosmos.base.reflection.v2alpha1;

/**
 * <pre>
 * QueryServicesDescriptor contains the list of cosmos-sdk queriable services
 * </pre>
 *
 * Protobuf type {@code cosmos.base.reflection.v2alpha1.QueryServicesDescriptor}
 */
public final class QueryServicesDescriptor extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:cosmos.base.reflection.v2alpha1.QueryServicesDescriptor)
    QueryServicesDescriptorOrBuilder {
private static final long serialVersionUID = 0L;
  // Use QueryServicesDescriptor.newBuilder() to construct.
  private QueryServicesDescriptor(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private QueryServicesDescriptor() {
    queryServices_ = java.util.Collections.emptyList();
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new QueryServicesDescriptor();
  }

  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return com.cosmos.base.reflection.v2alpha1.ReflectionProto.internal_static_cosmos_base_reflection_v2alpha1_QueryServicesDescriptor_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return com.cosmos.base.reflection.v2alpha1.ReflectionProto.internal_static_cosmos_base_reflection_v2alpha1_QueryServicesDescriptor_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            com.cosmos.base.reflection.v2alpha1.QueryServicesDescriptor.class, com.cosmos.base.reflection.v2alpha1.QueryServicesDescriptor.Builder.class);
  }

  public static final int QUERY_SERVICES_FIELD_NUMBER = 1;
  @SuppressWarnings("serial")
  private java.util.List<com.cosmos.base.reflection.v2alpha1.QueryServiceDescriptor> queryServices_;
  /**
   * <pre>
   * query_services is a list of cosmos-sdk QueryServiceDescriptor
   * </pre>
   *
   * <code>repeated .cosmos.base.reflection.v2alpha1.QueryServiceDescriptor query_services = 1 [json_name = "queryServices"];</code>
   */
  @java.lang.Override
  public java.util.List<com.cosmos.base.reflection.v2alpha1.QueryServiceDescriptor> getQueryServicesList() {
    return queryServices_;
  }
  /**
   * <pre>
   * query_services is a list of cosmos-sdk QueryServiceDescriptor
   * </pre>
   *
   * <code>repeated .cosmos.base.reflection.v2alpha1.QueryServiceDescriptor query_services = 1 [json_name = "queryServices"];</code>
   */
  @java.lang.Override
  public java.util.List<? extends com.cosmos.base.reflection.v2alpha1.QueryServiceDescriptorOrBuilder> 
      getQueryServicesOrBuilderList() {
    return queryServices_;
  }
  /**
   * <pre>
   * query_services is a list of cosmos-sdk QueryServiceDescriptor
   * </pre>
   *
   * <code>repeated .cosmos.base.reflection.v2alpha1.QueryServiceDescriptor query_services = 1 [json_name = "queryServices"];</code>
   */
  @java.lang.Override
  public int getQueryServicesCount() {
    return queryServices_.size();
  }
  /**
   * <pre>
   * query_services is a list of cosmos-sdk QueryServiceDescriptor
   * </pre>
   *
   * <code>repeated .cosmos.base.reflection.v2alpha1.QueryServiceDescriptor query_services = 1 [json_name = "queryServices"];</code>
   */
  @java.lang.Override
  public com.cosmos.base.reflection.v2alpha1.QueryServiceDescriptor getQueryServices(int index) {
    return queryServices_.get(index);
  }
  /**
   * <pre>
   * query_services is a list of cosmos-sdk QueryServiceDescriptor
   * </pre>
   *
   * <code>repeated .cosmos.base.reflection.v2alpha1.QueryServiceDescriptor query_services = 1 [json_name = "queryServices"];</code>
   */
  @java.lang.Override
  public com.cosmos.base.reflection.v2alpha1.QueryServiceDescriptorOrBuilder getQueryServicesOrBuilder(
      int index) {
    return queryServices_.get(index);
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
    for (int i = 0; i < queryServices_.size(); i++) {
      output.writeMessage(1, queryServices_.get(i));
    }
    getUnknownFields().writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    for (int i = 0; i < queryServices_.size(); i++) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(1, queryServices_.get(i));
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
    if (!(obj instanceof com.cosmos.base.reflection.v2alpha1.QueryServicesDescriptor)) {
      return super.equals(obj);
    }
    com.cosmos.base.reflection.v2alpha1.QueryServicesDescriptor other = (com.cosmos.base.reflection.v2alpha1.QueryServicesDescriptor) obj;

    if (!getQueryServicesList()
        .equals(other.getQueryServicesList())) return false;
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
    if (getQueryServicesCount() > 0) {
      hash = (37 * hash) + QUERY_SERVICES_FIELD_NUMBER;
      hash = (53 * hash) + getQueryServicesList().hashCode();
    }
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static com.cosmos.base.reflection.v2alpha1.QueryServicesDescriptor parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.cosmos.base.reflection.v2alpha1.QueryServicesDescriptor parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.cosmos.base.reflection.v2alpha1.QueryServicesDescriptor parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.cosmos.base.reflection.v2alpha1.QueryServicesDescriptor parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.cosmos.base.reflection.v2alpha1.QueryServicesDescriptor parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.cosmos.base.reflection.v2alpha1.QueryServicesDescriptor parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.cosmos.base.reflection.v2alpha1.QueryServicesDescriptor parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.cosmos.base.reflection.v2alpha1.QueryServicesDescriptor parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.cosmos.base.reflection.v2alpha1.QueryServicesDescriptor parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static com.cosmos.base.reflection.v2alpha1.QueryServicesDescriptor parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.cosmos.base.reflection.v2alpha1.QueryServicesDescriptor parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.cosmos.base.reflection.v2alpha1.QueryServicesDescriptor parseFrom(
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
  public static Builder newBuilder(com.cosmos.base.reflection.v2alpha1.QueryServicesDescriptor prototype) {
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
   * <pre>
   * QueryServicesDescriptor contains the list of cosmos-sdk queriable services
   * </pre>
   *
   * Protobuf type {@code cosmos.base.reflection.v2alpha1.QueryServicesDescriptor}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:cosmos.base.reflection.v2alpha1.QueryServicesDescriptor)
      com.cosmos.base.reflection.v2alpha1.QueryServicesDescriptorOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return com.cosmos.base.reflection.v2alpha1.ReflectionProto.internal_static_cosmos_base_reflection_v2alpha1_QueryServicesDescriptor_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return com.cosmos.base.reflection.v2alpha1.ReflectionProto.internal_static_cosmos_base_reflection_v2alpha1_QueryServicesDescriptor_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              com.cosmos.base.reflection.v2alpha1.QueryServicesDescriptor.class, com.cosmos.base.reflection.v2alpha1.QueryServicesDescriptor.Builder.class);
    }

    // Construct using com.cosmos.base.reflection.v2alpha1.QueryServicesDescriptor.newBuilder()
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
      if (queryServicesBuilder_ == null) {
        queryServices_ = java.util.Collections.emptyList();
      } else {
        queryServices_ = null;
        queryServicesBuilder_.clear();
      }
      bitField0_ = (bitField0_ & ~0x00000001);
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return com.cosmos.base.reflection.v2alpha1.ReflectionProto.internal_static_cosmos_base_reflection_v2alpha1_QueryServicesDescriptor_descriptor;
    }

    @java.lang.Override
    public com.cosmos.base.reflection.v2alpha1.QueryServicesDescriptor getDefaultInstanceForType() {
      return com.cosmos.base.reflection.v2alpha1.QueryServicesDescriptor.getDefaultInstance();
    }

    @java.lang.Override
    public com.cosmos.base.reflection.v2alpha1.QueryServicesDescriptor build() {
      com.cosmos.base.reflection.v2alpha1.QueryServicesDescriptor result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public com.cosmos.base.reflection.v2alpha1.QueryServicesDescriptor buildPartial() {
      com.cosmos.base.reflection.v2alpha1.QueryServicesDescriptor result = new com.cosmos.base.reflection.v2alpha1.QueryServicesDescriptor(this);
      buildPartialRepeatedFields(result);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartialRepeatedFields(com.cosmos.base.reflection.v2alpha1.QueryServicesDescriptor result) {
      if (queryServicesBuilder_ == null) {
        if (((bitField0_ & 0x00000001) != 0)) {
          queryServices_ = java.util.Collections.unmodifiableList(queryServices_);
          bitField0_ = (bitField0_ & ~0x00000001);
        }
        result.queryServices_ = queryServices_;
      } else {
        result.queryServices_ = queryServicesBuilder_.build();
      }
    }

    private void buildPartial0(com.cosmos.base.reflection.v2alpha1.QueryServicesDescriptor result) {
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
      if (other instanceof com.cosmos.base.reflection.v2alpha1.QueryServicesDescriptor) {
        return mergeFrom((com.cosmos.base.reflection.v2alpha1.QueryServicesDescriptor)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(com.cosmos.base.reflection.v2alpha1.QueryServicesDescriptor other) {
      if (other == com.cosmos.base.reflection.v2alpha1.QueryServicesDescriptor.getDefaultInstance()) return this;
      if (queryServicesBuilder_ == null) {
        if (!other.queryServices_.isEmpty()) {
          if (queryServices_.isEmpty()) {
            queryServices_ = other.queryServices_;
            bitField0_ = (bitField0_ & ~0x00000001);
          } else {
            ensureQueryServicesIsMutable();
            queryServices_.addAll(other.queryServices_);
          }
          onChanged();
        }
      } else {
        if (!other.queryServices_.isEmpty()) {
          if (queryServicesBuilder_.isEmpty()) {
            queryServicesBuilder_.dispose();
            queryServicesBuilder_ = null;
            queryServices_ = other.queryServices_;
            bitField0_ = (bitField0_ & ~0x00000001);
            queryServicesBuilder_ = 
              com.google.protobuf.GeneratedMessageV3.alwaysUseFieldBuilders ?
                 getQueryServicesFieldBuilder() : null;
          } else {
            queryServicesBuilder_.addAllMessages(other.queryServices_);
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
              com.cosmos.base.reflection.v2alpha1.QueryServiceDescriptor m =
                  input.readMessage(
                      com.cosmos.base.reflection.v2alpha1.QueryServiceDescriptor.parser(),
                      extensionRegistry);
              if (queryServicesBuilder_ == null) {
                ensureQueryServicesIsMutable();
                queryServices_.add(m);
              } else {
                queryServicesBuilder_.addMessage(m);
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

    private java.util.List<com.cosmos.base.reflection.v2alpha1.QueryServiceDescriptor> queryServices_ =
      java.util.Collections.emptyList();
    private void ensureQueryServicesIsMutable() {
      if (!((bitField0_ & 0x00000001) != 0)) {
        queryServices_ = new java.util.ArrayList<com.cosmos.base.reflection.v2alpha1.QueryServiceDescriptor>(queryServices_);
        bitField0_ |= 0x00000001;
       }
    }

    private com.google.protobuf.RepeatedFieldBuilderV3<
        com.cosmos.base.reflection.v2alpha1.QueryServiceDescriptor, com.cosmos.base.reflection.v2alpha1.QueryServiceDescriptor.Builder, com.cosmos.base.reflection.v2alpha1.QueryServiceDescriptorOrBuilder> queryServicesBuilder_;

    /**
     * <pre>
     * query_services is a list of cosmos-sdk QueryServiceDescriptor
     * </pre>
     *
     * <code>repeated .cosmos.base.reflection.v2alpha1.QueryServiceDescriptor query_services = 1 [json_name = "queryServices"];</code>
     */
    public java.util.List<com.cosmos.base.reflection.v2alpha1.QueryServiceDescriptor> getQueryServicesList() {
      if (queryServicesBuilder_ == null) {
        return java.util.Collections.unmodifiableList(queryServices_);
      } else {
        return queryServicesBuilder_.getMessageList();
      }
    }
    /**
     * <pre>
     * query_services is a list of cosmos-sdk QueryServiceDescriptor
     * </pre>
     *
     * <code>repeated .cosmos.base.reflection.v2alpha1.QueryServiceDescriptor query_services = 1 [json_name = "queryServices"];</code>
     */
    public int getQueryServicesCount() {
      if (queryServicesBuilder_ == null) {
        return queryServices_.size();
      } else {
        return queryServicesBuilder_.getCount();
      }
    }
    /**
     * <pre>
     * query_services is a list of cosmos-sdk QueryServiceDescriptor
     * </pre>
     *
     * <code>repeated .cosmos.base.reflection.v2alpha1.QueryServiceDescriptor query_services = 1 [json_name = "queryServices"];</code>
     */
    public com.cosmos.base.reflection.v2alpha1.QueryServiceDescriptor getQueryServices(int index) {
      if (queryServicesBuilder_ == null) {
        return queryServices_.get(index);
      } else {
        return queryServicesBuilder_.getMessage(index);
      }
    }
    /**
     * <pre>
     * query_services is a list of cosmos-sdk QueryServiceDescriptor
     * </pre>
     *
     * <code>repeated .cosmos.base.reflection.v2alpha1.QueryServiceDescriptor query_services = 1 [json_name = "queryServices"];</code>
     */
    public Builder setQueryServices(
        int index, com.cosmos.base.reflection.v2alpha1.QueryServiceDescriptor value) {
      if (queryServicesBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureQueryServicesIsMutable();
        queryServices_.set(index, value);
        onChanged();
      } else {
        queryServicesBuilder_.setMessage(index, value);
      }
      return this;
    }
    /**
     * <pre>
     * query_services is a list of cosmos-sdk QueryServiceDescriptor
     * </pre>
     *
     * <code>repeated .cosmos.base.reflection.v2alpha1.QueryServiceDescriptor query_services = 1 [json_name = "queryServices"];</code>
     */
    public Builder setQueryServices(
        int index, com.cosmos.base.reflection.v2alpha1.QueryServiceDescriptor.Builder builderForValue) {
      if (queryServicesBuilder_ == null) {
        ensureQueryServicesIsMutable();
        queryServices_.set(index, builderForValue.build());
        onChanged();
      } else {
        queryServicesBuilder_.setMessage(index, builderForValue.build());
      }
      return this;
    }
    /**
     * <pre>
     * query_services is a list of cosmos-sdk QueryServiceDescriptor
     * </pre>
     *
     * <code>repeated .cosmos.base.reflection.v2alpha1.QueryServiceDescriptor query_services = 1 [json_name = "queryServices"];</code>
     */
    public Builder addQueryServices(com.cosmos.base.reflection.v2alpha1.QueryServiceDescriptor value) {
      if (queryServicesBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureQueryServicesIsMutable();
        queryServices_.add(value);
        onChanged();
      } else {
        queryServicesBuilder_.addMessage(value);
      }
      return this;
    }
    /**
     * <pre>
     * query_services is a list of cosmos-sdk QueryServiceDescriptor
     * </pre>
     *
     * <code>repeated .cosmos.base.reflection.v2alpha1.QueryServiceDescriptor query_services = 1 [json_name = "queryServices"];</code>
     */
    public Builder addQueryServices(
        int index, com.cosmos.base.reflection.v2alpha1.QueryServiceDescriptor value) {
      if (queryServicesBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureQueryServicesIsMutable();
        queryServices_.add(index, value);
        onChanged();
      } else {
        queryServicesBuilder_.addMessage(index, value);
      }
      return this;
    }
    /**
     * <pre>
     * query_services is a list of cosmos-sdk QueryServiceDescriptor
     * </pre>
     *
     * <code>repeated .cosmos.base.reflection.v2alpha1.QueryServiceDescriptor query_services = 1 [json_name = "queryServices"];</code>
     */
    public Builder addQueryServices(
        com.cosmos.base.reflection.v2alpha1.QueryServiceDescriptor.Builder builderForValue) {
      if (queryServicesBuilder_ == null) {
        ensureQueryServicesIsMutable();
        queryServices_.add(builderForValue.build());
        onChanged();
      } else {
        queryServicesBuilder_.addMessage(builderForValue.build());
      }
      return this;
    }
    /**
     * <pre>
     * query_services is a list of cosmos-sdk QueryServiceDescriptor
     * </pre>
     *
     * <code>repeated .cosmos.base.reflection.v2alpha1.QueryServiceDescriptor query_services = 1 [json_name = "queryServices"];</code>
     */
    public Builder addQueryServices(
        int index, com.cosmos.base.reflection.v2alpha1.QueryServiceDescriptor.Builder builderForValue) {
      if (queryServicesBuilder_ == null) {
        ensureQueryServicesIsMutable();
        queryServices_.add(index, builderForValue.build());
        onChanged();
      } else {
        queryServicesBuilder_.addMessage(index, builderForValue.build());
      }
      return this;
    }
    /**
     * <pre>
     * query_services is a list of cosmos-sdk QueryServiceDescriptor
     * </pre>
     *
     * <code>repeated .cosmos.base.reflection.v2alpha1.QueryServiceDescriptor query_services = 1 [json_name = "queryServices"];</code>
     */
    public Builder addAllQueryServices(
        java.lang.Iterable<? extends com.cosmos.base.reflection.v2alpha1.QueryServiceDescriptor> values) {
      if (queryServicesBuilder_ == null) {
        ensureQueryServicesIsMutable();
        com.google.protobuf.AbstractMessageLite.Builder.addAll(
            values, queryServices_);
        onChanged();
      } else {
        queryServicesBuilder_.addAllMessages(values);
      }
      return this;
    }
    /**
     * <pre>
     * query_services is a list of cosmos-sdk QueryServiceDescriptor
     * </pre>
     *
     * <code>repeated .cosmos.base.reflection.v2alpha1.QueryServiceDescriptor query_services = 1 [json_name = "queryServices"];</code>
     */
    public Builder clearQueryServices() {
      if (queryServicesBuilder_ == null) {
        queryServices_ = java.util.Collections.emptyList();
        bitField0_ = (bitField0_ & ~0x00000001);
        onChanged();
      } else {
        queryServicesBuilder_.clear();
      }
      return this;
    }
    /**
     * <pre>
     * query_services is a list of cosmos-sdk QueryServiceDescriptor
     * </pre>
     *
     * <code>repeated .cosmos.base.reflection.v2alpha1.QueryServiceDescriptor query_services = 1 [json_name = "queryServices"];</code>
     */
    public Builder removeQueryServices(int index) {
      if (queryServicesBuilder_ == null) {
        ensureQueryServicesIsMutable();
        queryServices_.remove(index);
        onChanged();
      } else {
        queryServicesBuilder_.remove(index);
      }
      return this;
    }
    /**
     * <pre>
     * query_services is a list of cosmos-sdk QueryServiceDescriptor
     * </pre>
     *
     * <code>repeated .cosmos.base.reflection.v2alpha1.QueryServiceDescriptor query_services = 1 [json_name = "queryServices"];</code>
     */
    public com.cosmos.base.reflection.v2alpha1.QueryServiceDescriptor.Builder getQueryServicesBuilder(
        int index) {
      return getQueryServicesFieldBuilder().getBuilder(index);
    }
    /**
     * <pre>
     * query_services is a list of cosmos-sdk QueryServiceDescriptor
     * </pre>
     *
     * <code>repeated .cosmos.base.reflection.v2alpha1.QueryServiceDescriptor query_services = 1 [json_name = "queryServices"];</code>
     */
    public com.cosmos.base.reflection.v2alpha1.QueryServiceDescriptorOrBuilder getQueryServicesOrBuilder(
        int index) {
      if (queryServicesBuilder_ == null) {
        return queryServices_.get(index);  } else {
        return queryServicesBuilder_.getMessageOrBuilder(index);
      }
    }
    /**
     * <pre>
     * query_services is a list of cosmos-sdk QueryServiceDescriptor
     * </pre>
     *
     * <code>repeated .cosmos.base.reflection.v2alpha1.QueryServiceDescriptor query_services = 1 [json_name = "queryServices"];</code>
     */
    public java.util.List<? extends com.cosmos.base.reflection.v2alpha1.QueryServiceDescriptorOrBuilder> 
         getQueryServicesOrBuilderList() {
      if (queryServicesBuilder_ != null) {
        return queryServicesBuilder_.getMessageOrBuilderList();
      } else {
        return java.util.Collections.unmodifiableList(queryServices_);
      }
    }
    /**
     * <pre>
     * query_services is a list of cosmos-sdk QueryServiceDescriptor
     * </pre>
     *
     * <code>repeated .cosmos.base.reflection.v2alpha1.QueryServiceDescriptor query_services = 1 [json_name = "queryServices"];</code>
     */
    public com.cosmos.base.reflection.v2alpha1.QueryServiceDescriptor.Builder addQueryServicesBuilder() {
      return getQueryServicesFieldBuilder().addBuilder(
          com.cosmos.base.reflection.v2alpha1.QueryServiceDescriptor.getDefaultInstance());
    }
    /**
     * <pre>
     * query_services is a list of cosmos-sdk QueryServiceDescriptor
     * </pre>
     *
     * <code>repeated .cosmos.base.reflection.v2alpha1.QueryServiceDescriptor query_services = 1 [json_name = "queryServices"];</code>
     */
    public com.cosmos.base.reflection.v2alpha1.QueryServiceDescriptor.Builder addQueryServicesBuilder(
        int index) {
      return getQueryServicesFieldBuilder().addBuilder(
          index, com.cosmos.base.reflection.v2alpha1.QueryServiceDescriptor.getDefaultInstance());
    }
    /**
     * <pre>
     * query_services is a list of cosmos-sdk QueryServiceDescriptor
     * </pre>
     *
     * <code>repeated .cosmos.base.reflection.v2alpha1.QueryServiceDescriptor query_services = 1 [json_name = "queryServices"];</code>
     */
    public java.util.List<com.cosmos.base.reflection.v2alpha1.QueryServiceDescriptor.Builder> 
         getQueryServicesBuilderList() {
      return getQueryServicesFieldBuilder().getBuilderList();
    }
    private com.google.protobuf.RepeatedFieldBuilderV3<
        com.cosmos.base.reflection.v2alpha1.QueryServiceDescriptor, com.cosmos.base.reflection.v2alpha1.QueryServiceDescriptor.Builder, com.cosmos.base.reflection.v2alpha1.QueryServiceDescriptorOrBuilder> 
        getQueryServicesFieldBuilder() {
      if (queryServicesBuilder_ == null) {
        queryServicesBuilder_ = new com.google.protobuf.RepeatedFieldBuilderV3<
            com.cosmos.base.reflection.v2alpha1.QueryServiceDescriptor, com.cosmos.base.reflection.v2alpha1.QueryServiceDescriptor.Builder, com.cosmos.base.reflection.v2alpha1.QueryServiceDescriptorOrBuilder>(
                queryServices_,
                ((bitField0_ & 0x00000001) != 0),
                getParentForChildren(),
                isClean());
        queryServices_ = null;
      }
      return queryServicesBuilder_;
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


    // @@protoc_insertion_point(builder_scope:cosmos.base.reflection.v2alpha1.QueryServicesDescriptor)
  }

  // @@protoc_insertion_point(class_scope:cosmos.base.reflection.v2alpha1.QueryServicesDescriptor)
  private static final com.cosmos.base.reflection.v2alpha1.QueryServicesDescriptor DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new com.cosmos.base.reflection.v2alpha1.QueryServicesDescriptor();
  }

  public static com.cosmos.base.reflection.v2alpha1.QueryServicesDescriptor getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<QueryServicesDescriptor>
      PARSER = new com.google.protobuf.AbstractParser<QueryServicesDescriptor>() {
    @java.lang.Override
    public QueryServicesDescriptor parsePartialFrom(
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

  public static com.google.protobuf.Parser<QueryServicesDescriptor> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<QueryServicesDescriptor> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public com.cosmos.base.reflection.v2alpha1.QueryServicesDescriptor getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

