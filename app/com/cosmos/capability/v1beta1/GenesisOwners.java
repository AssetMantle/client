// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: cosmos/capability/v1beta1/genesis.proto

package com.cosmos.capability.v1beta1;

/**
 * <pre>
 * GenesisOwners defines the capability owners with their corresponding index.
 * </pre>
 *
 * Protobuf type {@code cosmos.capability.v1beta1.GenesisOwners}
 */
public final class GenesisOwners extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:cosmos.capability.v1beta1.GenesisOwners)
    GenesisOwnersOrBuilder {
private static final long serialVersionUID = 0L;
  // Use GenesisOwners.newBuilder() to construct.
  private GenesisOwners(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private GenesisOwners() {
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new GenesisOwners();
  }

  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return com.cosmos.capability.v1beta1.GenesisProto.internal_static_cosmos_capability_v1beta1_GenesisOwners_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return com.cosmos.capability.v1beta1.GenesisProto.internal_static_cosmos_capability_v1beta1_GenesisOwners_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            com.cosmos.capability.v1beta1.GenesisOwners.class, com.cosmos.capability.v1beta1.GenesisOwners.Builder.class);
  }

  public static final int INDEX_FIELD_NUMBER = 1;
  private long index_ = 0L;
  /**
   * <pre>
   * index is the index of the capability owner.
   * </pre>
   *
   * <code>uint64 index = 1 [json_name = "index"];</code>
   * @return The index.
   */
  @java.lang.Override
  public long getIndex() {
    return index_;
  }

  public static final int INDEX_OWNERS_FIELD_NUMBER = 2;
  private com.cosmos.capability.v1beta1.CapabilityOwners indexOwners_;
  /**
   * <pre>
   * index_owners are the owners at the given index.
   * </pre>
   *
   * <code>.cosmos.capability.v1beta1.CapabilityOwners index_owners = 2 [json_name = "indexOwners", (.gogoproto.nullable) = false, (.gogoproto.moretags) = "yaml:&#92;"index_owners&#92;""];</code>
   * @return Whether the indexOwners field is set.
   */
  @java.lang.Override
  public boolean hasIndexOwners() {
    return indexOwners_ != null;
  }
  /**
   * <pre>
   * index_owners are the owners at the given index.
   * </pre>
   *
   * <code>.cosmos.capability.v1beta1.CapabilityOwners index_owners = 2 [json_name = "indexOwners", (.gogoproto.nullable) = false, (.gogoproto.moretags) = "yaml:&#92;"index_owners&#92;""];</code>
   * @return The indexOwners.
   */
  @java.lang.Override
  public com.cosmos.capability.v1beta1.CapabilityOwners getIndexOwners() {
    return indexOwners_ == null ? com.cosmos.capability.v1beta1.CapabilityOwners.getDefaultInstance() : indexOwners_;
  }
  /**
   * <pre>
   * index_owners are the owners at the given index.
   * </pre>
   *
   * <code>.cosmos.capability.v1beta1.CapabilityOwners index_owners = 2 [json_name = "indexOwners", (.gogoproto.nullable) = false, (.gogoproto.moretags) = "yaml:&#92;"index_owners&#92;""];</code>
   */
  @java.lang.Override
  public com.cosmos.capability.v1beta1.CapabilityOwnersOrBuilder getIndexOwnersOrBuilder() {
    return indexOwners_ == null ? com.cosmos.capability.v1beta1.CapabilityOwners.getDefaultInstance() : indexOwners_;
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
    if (index_ != 0L) {
      output.writeUInt64(1, index_);
    }
    if (indexOwners_ != null) {
      output.writeMessage(2, getIndexOwners());
    }
    getUnknownFields().writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (index_ != 0L) {
      size += com.google.protobuf.CodedOutputStream
        .computeUInt64Size(1, index_);
    }
    if (indexOwners_ != null) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(2, getIndexOwners());
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
    if (!(obj instanceof com.cosmos.capability.v1beta1.GenesisOwners)) {
      return super.equals(obj);
    }
    com.cosmos.capability.v1beta1.GenesisOwners other = (com.cosmos.capability.v1beta1.GenesisOwners) obj;

    if (getIndex()
        != other.getIndex()) return false;
    if (hasIndexOwners() != other.hasIndexOwners()) return false;
    if (hasIndexOwners()) {
      if (!getIndexOwners()
          .equals(other.getIndexOwners())) return false;
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
    hash = (37 * hash) + INDEX_FIELD_NUMBER;
    hash = (53 * hash) + com.google.protobuf.Internal.hashLong(
        getIndex());
    if (hasIndexOwners()) {
      hash = (37 * hash) + INDEX_OWNERS_FIELD_NUMBER;
      hash = (53 * hash) + getIndexOwners().hashCode();
    }
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static com.cosmos.capability.v1beta1.GenesisOwners parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.cosmos.capability.v1beta1.GenesisOwners parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.cosmos.capability.v1beta1.GenesisOwners parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.cosmos.capability.v1beta1.GenesisOwners parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.cosmos.capability.v1beta1.GenesisOwners parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.cosmos.capability.v1beta1.GenesisOwners parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.cosmos.capability.v1beta1.GenesisOwners parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.cosmos.capability.v1beta1.GenesisOwners parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.cosmos.capability.v1beta1.GenesisOwners parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static com.cosmos.capability.v1beta1.GenesisOwners parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.cosmos.capability.v1beta1.GenesisOwners parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.cosmos.capability.v1beta1.GenesisOwners parseFrom(
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
  public static Builder newBuilder(com.cosmos.capability.v1beta1.GenesisOwners prototype) {
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
   * GenesisOwners defines the capability owners with their corresponding index.
   * </pre>
   *
   * Protobuf type {@code cosmos.capability.v1beta1.GenesisOwners}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:cosmos.capability.v1beta1.GenesisOwners)
      com.cosmos.capability.v1beta1.GenesisOwnersOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return com.cosmos.capability.v1beta1.GenesisProto.internal_static_cosmos_capability_v1beta1_GenesisOwners_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return com.cosmos.capability.v1beta1.GenesisProto.internal_static_cosmos_capability_v1beta1_GenesisOwners_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              com.cosmos.capability.v1beta1.GenesisOwners.class, com.cosmos.capability.v1beta1.GenesisOwners.Builder.class);
    }

    // Construct using com.cosmos.capability.v1beta1.GenesisOwners.newBuilder()
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
      index_ = 0L;
      indexOwners_ = null;
      if (indexOwnersBuilder_ != null) {
        indexOwnersBuilder_.dispose();
        indexOwnersBuilder_ = null;
      }
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return com.cosmos.capability.v1beta1.GenesisProto.internal_static_cosmos_capability_v1beta1_GenesisOwners_descriptor;
    }

    @java.lang.Override
    public com.cosmos.capability.v1beta1.GenesisOwners getDefaultInstanceForType() {
      return com.cosmos.capability.v1beta1.GenesisOwners.getDefaultInstance();
    }

    @java.lang.Override
    public com.cosmos.capability.v1beta1.GenesisOwners build() {
      com.cosmos.capability.v1beta1.GenesisOwners result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public com.cosmos.capability.v1beta1.GenesisOwners buildPartial() {
      com.cosmos.capability.v1beta1.GenesisOwners result = new com.cosmos.capability.v1beta1.GenesisOwners(this);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartial0(com.cosmos.capability.v1beta1.GenesisOwners result) {
      int from_bitField0_ = bitField0_;
      if (((from_bitField0_ & 0x00000001) != 0)) {
        result.index_ = index_;
      }
      if (((from_bitField0_ & 0x00000002) != 0)) {
        result.indexOwners_ = indexOwnersBuilder_ == null
            ? indexOwners_
            : indexOwnersBuilder_.build();
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
      if (other instanceof com.cosmos.capability.v1beta1.GenesisOwners) {
        return mergeFrom((com.cosmos.capability.v1beta1.GenesisOwners)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(com.cosmos.capability.v1beta1.GenesisOwners other) {
      if (other == com.cosmos.capability.v1beta1.GenesisOwners.getDefaultInstance()) return this;
      if (other.getIndex() != 0L) {
        setIndex(other.getIndex());
      }
      if (other.hasIndexOwners()) {
        mergeIndexOwners(other.getIndexOwners());
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
            case 8: {
              index_ = input.readUInt64();
              bitField0_ |= 0x00000001;
              break;
            } // case 8
            case 18: {
              input.readMessage(
                  getIndexOwnersFieldBuilder().getBuilder(),
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

    private long index_ ;
    /**
     * <pre>
     * index is the index of the capability owner.
     * </pre>
     *
     * <code>uint64 index = 1 [json_name = "index"];</code>
     * @return The index.
     */
    @java.lang.Override
    public long getIndex() {
      return index_;
    }
    /**
     * <pre>
     * index is the index of the capability owner.
     * </pre>
     *
     * <code>uint64 index = 1 [json_name = "index"];</code>
     * @param value The index to set.
     * @return This builder for chaining.
     */
    public Builder setIndex(long value) {

      index_ = value;
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * index is the index of the capability owner.
     * </pre>
     *
     * <code>uint64 index = 1 [json_name = "index"];</code>
     * @return This builder for chaining.
     */
    public Builder clearIndex() {
      bitField0_ = (bitField0_ & ~0x00000001);
      index_ = 0L;
      onChanged();
      return this;
    }

    private com.cosmos.capability.v1beta1.CapabilityOwners indexOwners_;
    private com.google.protobuf.SingleFieldBuilderV3<
        com.cosmos.capability.v1beta1.CapabilityOwners, com.cosmos.capability.v1beta1.CapabilityOwners.Builder, com.cosmos.capability.v1beta1.CapabilityOwnersOrBuilder> indexOwnersBuilder_;
    /**
     * <pre>
     * index_owners are the owners at the given index.
     * </pre>
     *
     * <code>.cosmos.capability.v1beta1.CapabilityOwners index_owners = 2 [json_name = "indexOwners", (.gogoproto.nullable) = false, (.gogoproto.moretags) = "yaml:&#92;"index_owners&#92;""];</code>
     * @return Whether the indexOwners field is set.
     */
    public boolean hasIndexOwners() {
      return ((bitField0_ & 0x00000002) != 0);
    }
    /**
     * <pre>
     * index_owners are the owners at the given index.
     * </pre>
     *
     * <code>.cosmos.capability.v1beta1.CapabilityOwners index_owners = 2 [json_name = "indexOwners", (.gogoproto.nullable) = false, (.gogoproto.moretags) = "yaml:&#92;"index_owners&#92;""];</code>
     * @return The indexOwners.
     */
    public com.cosmos.capability.v1beta1.CapabilityOwners getIndexOwners() {
      if (indexOwnersBuilder_ == null) {
        return indexOwners_ == null ? com.cosmos.capability.v1beta1.CapabilityOwners.getDefaultInstance() : indexOwners_;
      } else {
        return indexOwnersBuilder_.getMessage();
      }
    }
    /**
     * <pre>
     * index_owners are the owners at the given index.
     * </pre>
     *
     * <code>.cosmos.capability.v1beta1.CapabilityOwners index_owners = 2 [json_name = "indexOwners", (.gogoproto.nullable) = false, (.gogoproto.moretags) = "yaml:&#92;"index_owners&#92;""];</code>
     */
    public Builder setIndexOwners(com.cosmos.capability.v1beta1.CapabilityOwners value) {
      if (indexOwnersBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        indexOwners_ = value;
      } else {
        indexOwnersBuilder_.setMessage(value);
      }
      bitField0_ |= 0x00000002;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * index_owners are the owners at the given index.
     * </pre>
     *
     * <code>.cosmos.capability.v1beta1.CapabilityOwners index_owners = 2 [json_name = "indexOwners", (.gogoproto.nullable) = false, (.gogoproto.moretags) = "yaml:&#92;"index_owners&#92;""];</code>
     */
    public Builder setIndexOwners(
        com.cosmos.capability.v1beta1.CapabilityOwners.Builder builderForValue) {
      if (indexOwnersBuilder_ == null) {
        indexOwners_ = builderForValue.build();
      } else {
        indexOwnersBuilder_.setMessage(builderForValue.build());
      }
      bitField0_ |= 0x00000002;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * index_owners are the owners at the given index.
     * </pre>
     *
     * <code>.cosmos.capability.v1beta1.CapabilityOwners index_owners = 2 [json_name = "indexOwners", (.gogoproto.nullable) = false, (.gogoproto.moretags) = "yaml:&#92;"index_owners&#92;""];</code>
     */
    public Builder mergeIndexOwners(com.cosmos.capability.v1beta1.CapabilityOwners value) {
      if (indexOwnersBuilder_ == null) {
        if (((bitField0_ & 0x00000002) != 0) &&
          indexOwners_ != null &&
          indexOwners_ != com.cosmos.capability.v1beta1.CapabilityOwners.getDefaultInstance()) {
          getIndexOwnersBuilder().mergeFrom(value);
        } else {
          indexOwners_ = value;
        }
      } else {
        indexOwnersBuilder_.mergeFrom(value);
      }
      bitField0_ |= 0x00000002;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * index_owners are the owners at the given index.
     * </pre>
     *
     * <code>.cosmos.capability.v1beta1.CapabilityOwners index_owners = 2 [json_name = "indexOwners", (.gogoproto.nullable) = false, (.gogoproto.moretags) = "yaml:&#92;"index_owners&#92;""];</code>
     */
    public Builder clearIndexOwners() {
      bitField0_ = (bitField0_ & ~0x00000002);
      indexOwners_ = null;
      if (indexOwnersBuilder_ != null) {
        indexOwnersBuilder_.dispose();
        indexOwnersBuilder_ = null;
      }
      onChanged();
      return this;
    }
    /**
     * <pre>
     * index_owners are the owners at the given index.
     * </pre>
     *
     * <code>.cosmos.capability.v1beta1.CapabilityOwners index_owners = 2 [json_name = "indexOwners", (.gogoproto.nullable) = false, (.gogoproto.moretags) = "yaml:&#92;"index_owners&#92;""];</code>
     */
    public com.cosmos.capability.v1beta1.CapabilityOwners.Builder getIndexOwnersBuilder() {
      bitField0_ |= 0x00000002;
      onChanged();
      return getIndexOwnersFieldBuilder().getBuilder();
    }
    /**
     * <pre>
     * index_owners are the owners at the given index.
     * </pre>
     *
     * <code>.cosmos.capability.v1beta1.CapabilityOwners index_owners = 2 [json_name = "indexOwners", (.gogoproto.nullable) = false, (.gogoproto.moretags) = "yaml:&#92;"index_owners&#92;""];</code>
     */
    public com.cosmos.capability.v1beta1.CapabilityOwnersOrBuilder getIndexOwnersOrBuilder() {
      if (indexOwnersBuilder_ != null) {
        return indexOwnersBuilder_.getMessageOrBuilder();
      } else {
        return indexOwners_ == null ?
            com.cosmos.capability.v1beta1.CapabilityOwners.getDefaultInstance() : indexOwners_;
      }
    }
    /**
     * <pre>
     * index_owners are the owners at the given index.
     * </pre>
     *
     * <code>.cosmos.capability.v1beta1.CapabilityOwners index_owners = 2 [json_name = "indexOwners", (.gogoproto.nullable) = false, (.gogoproto.moretags) = "yaml:&#92;"index_owners&#92;""];</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
        com.cosmos.capability.v1beta1.CapabilityOwners, com.cosmos.capability.v1beta1.CapabilityOwners.Builder, com.cosmos.capability.v1beta1.CapabilityOwnersOrBuilder> 
        getIndexOwnersFieldBuilder() {
      if (indexOwnersBuilder_ == null) {
        indexOwnersBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
            com.cosmos.capability.v1beta1.CapabilityOwners, com.cosmos.capability.v1beta1.CapabilityOwners.Builder, com.cosmos.capability.v1beta1.CapabilityOwnersOrBuilder>(
                getIndexOwners(),
                getParentForChildren(),
                isClean());
        indexOwners_ = null;
      }
      return indexOwnersBuilder_;
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


    // @@protoc_insertion_point(builder_scope:cosmos.capability.v1beta1.GenesisOwners)
  }

  // @@protoc_insertion_point(class_scope:cosmos.capability.v1beta1.GenesisOwners)
  private static final com.cosmos.capability.v1beta1.GenesisOwners DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new com.cosmos.capability.v1beta1.GenesisOwners();
  }

  public static com.cosmos.capability.v1beta1.GenesisOwners getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<GenesisOwners>
      PARSER = new com.google.protobuf.AbstractParser<GenesisOwners>() {
    @java.lang.Override
    public GenesisOwners parsePartialFrom(
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

  public static com.google.protobuf.Parser<GenesisOwners> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<GenesisOwners> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public com.cosmos.capability.v1beta1.GenesisOwners getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}
