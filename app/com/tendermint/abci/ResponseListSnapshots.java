// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: tendermint/abci/types.proto

package com.tendermint.abci;

/**
 * Protobuf type {@code tendermint.abci.ResponseListSnapshots}
 */
public final class ResponseListSnapshots extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:tendermint.abci.ResponseListSnapshots)
    ResponseListSnapshotsOrBuilder {
private static final long serialVersionUID = 0L;
  // Use ResponseListSnapshots.newBuilder() to construct.
  private ResponseListSnapshots(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private ResponseListSnapshots() {
    snapshots_ = java.util.Collections.emptyList();
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new ResponseListSnapshots();
  }

  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return com.tendermint.abci.TypesProto.internal_static_tendermint_abci_ResponseListSnapshots_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return com.tendermint.abci.TypesProto.internal_static_tendermint_abci_ResponseListSnapshots_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            com.tendermint.abci.ResponseListSnapshots.class, com.tendermint.abci.ResponseListSnapshots.Builder.class);
  }

  public static final int SNAPSHOTS_FIELD_NUMBER = 1;
  @SuppressWarnings("serial")
  private java.util.List<com.tendermint.abci.Snapshot> snapshots_;
  /**
   * <code>repeated .tendermint.abci.Snapshot snapshots = 1 [json_name = "snapshots"];</code>
   */
  @java.lang.Override
  public java.util.List<com.tendermint.abci.Snapshot> getSnapshotsList() {
    return snapshots_;
  }
  /**
   * <code>repeated .tendermint.abci.Snapshot snapshots = 1 [json_name = "snapshots"];</code>
   */
  @java.lang.Override
  public java.util.List<? extends com.tendermint.abci.SnapshotOrBuilder> 
      getSnapshotsOrBuilderList() {
    return snapshots_;
  }
  /**
   * <code>repeated .tendermint.abci.Snapshot snapshots = 1 [json_name = "snapshots"];</code>
   */
  @java.lang.Override
  public int getSnapshotsCount() {
    return snapshots_.size();
  }
  /**
   * <code>repeated .tendermint.abci.Snapshot snapshots = 1 [json_name = "snapshots"];</code>
   */
  @java.lang.Override
  public com.tendermint.abci.Snapshot getSnapshots(int index) {
    return snapshots_.get(index);
  }
  /**
   * <code>repeated .tendermint.abci.Snapshot snapshots = 1 [json_name = "snapshots"];</code>
   */
  @java.lang.Override
  public com.tendermint.abci.SnapshotOrBuilder getSnapshotsOrBuilder(
      int index) {
    return snapshots_.get(index);
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
    for (int i = 0; i < snapshots_.size(); i++) {
      output.writeMessage(1, snapshots_.get(i));
    }
    getUnknownFields().writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    for (int i = 0; i < snapshots_.size(); i++) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(1, snapshots_.get(i));
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
    if (!(obj instanceof com.tendermint.abci.ResponseListSnapshots)) {
      return super.equals(obj);
    }
    com.tendermint.abci.ResponseListSnapshots other = (com.tendermint.abci.ResponseListSnapshots) obj;

    if (!getSnapshotsList()
        .equals(other.getSnapshotsList())) return false;
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
    if (getSnapshotsCount() > 0) {
      hash = (37 * hash) + SNAPSHOTS_FIELD_NUMBER;
      hash = (53 * hash) + getSnapshotsList().hashCode();
    }
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static com.tendermint.abci.ResponseListSnapshots parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.tendermint.abci.ResponseListSnapshots parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.tendermint.abci.ResponseListSnapshots parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.tendermint.abci.ResponseListSnapshots parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.tendermint.abci.ResponseListSnapshots parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.tendermint.abci.ResponseListSnapshots parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.tendermint.abci.ResponseListSnapshots parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.tendermint.abci.ResponseListSnapshots parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.tendermint.abci.ResponseListSnapshots parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static com.tendermint.abci.ResponseListSnapshots parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.tendermint.abci.ResponseListSnapshots parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.tendermint.abci.ResponseListSnapshots parseFrom(
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
  public static Builder newBuilder(com.tendermint.abci.ResponseListSnapshots prototype) {
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
   * Protobuf type {@code tendermint.abci.ResponseListSnapshots}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:tendermint.abci.ResponseListSnapshots)
      com.tendermint.abci.ResponseListSnapshotsOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return com.tendermint.abci.TypesProto.internal_static_tendermint_abci_ResponseListSnapshots_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return com.tendermint.abci.TypesProto.internal_static_tendermint_abci_ResponseListSnapshots_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              com.tendermint.abci.ResponseListSnapshots.class, com.tendermint.abci.ResponseListSnapshots.Builder.class);
    }

    // Construct using com.tendermint.abci.ResponseListSnapshots.newBuilder()
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
      if (snapshotsBuilder_ == null) {
        snapshots_ = java.util.Collections.emptyList();
      } else {
        snapshots_ = null;
        snapshotsBuilder_.clear();
      }
      bitField0_ = (bitField0_ & ~0x00000001);
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return com.tendermint.abci.TypesProto.internal_static_tendermint_abci_ResponseListSnapshots_descriptor;
    }

    @java.lang.Override
    public com.tendermint.abci.ResponseListSnapshots getDefaultInstanceForType() {
      return com.tendermint.abci.ResponseListSnapshots.getDefaultInstance();
    }

    @java.lang.Override
    public com.tendermint.abci.ResponseListSnapshots build() {
      com.tendermint.abci.ResponseListSnapshots result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public com.tendermint.abci.ResponseListSnapshots buildPartial() {
      com.tendermint.abci.ResponseListSnapshots result = new com.tendermint.abci.ResponseListSnapshots(this);
      buildPartialRepeatedFields(result);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartialRepeatedFields(com.tendermint.abci.ResponseListSnapshots result) {
      if (snapshotsBuilder_ == null) {
        if (((bitField0_ & 0x00000001) != 0)) {
          snapshots_ = java.util.Collections.unmodifiableList(snapshots_);
          bitField0_ = (bitField0_ & ~0x00000001);
        }
        result.snapshots_ = snapshots_;
      } else {
        result.snapshots_ = snapshotsBuilder_.build();
      }
    }

    private void buildPartial0(com.tendermint.abci.ResponseListSnapshots result) {
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
      if (other instanceof com.tendermint.abci.ResponseListSnapshots) {
        return mergeFrom((com.tendermint.abci.ResponseListSnapshots)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(com.tendermint.abci.ResponseListSnapshots other) {
      if (other == com.tendermint.abci.ResponseListSnapshots.getDefaultInstance()) return this;
      if (snapshotsBuilder_ == null) {
        if (!other.snapshots_.isEmpty()) {
          if (snapshots_.isEmpty()) {
            snapshots_ = other.snapshots_;
            bitField0_ = (bitField0_ & ~0x00000001);
          } else {
            ensureSnapshotsIsMutable();
            snapshots_.addAll(other.snapshots_);
          }
          onChanged();
        }
      } else {
        if (!other.snapshots_.isEmpty()) {
          if (snapshotsBuilder_.isEmpty()) {
            snapshotsBuilder_.dispose();
            snapshotsBuilder_ = null;
            snapshots_ = other.snapshots_;
            bitField0_ = (bitField0_ & ~0x00000001);
            snapshotsBuilder_ = 
              com.google.protobuf.GeneratedMessageV3.alwaysUseFieldBuilders ?
                 getSnapshotsFieldBuilder() : null;
          } else {
            snapshotsBuilder_.addAllMessages(other.snapshots_);
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
              com.tendermint.abci.Snapshot m =
                  input.readMessage(
                      com.tendermint.abci.Snapshot.parser(),
                      extensionRegistry);
              if (snapshotsBuilder_ == null) {
                ensureSnapshotsIsMutable();
                snapshots_.add(m);
              } else {
                snapshotsBuilder_.addMessage(m);
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

    private java.util.List<com.tendermint.abci.Snapshot> snapshots_ =
      java.util.Collections.emptyList();
    private void ensureSnapshotsIsMutable() {
      if (!((bitField0_ & 0x00000001) != 0)) {
        snapshots_ = new java.util.ArrayList<com.tendermint.abci.Snapshot>(snapshots_);
        bitField0_ |= 0x00000001;
       }
    }

    private com.google.protobuf.RepeatedFieldBuilderV3<
        com.tendermint.abci.Snapshot, com.tendermint.abci.Snapshot.Builder, com.tendermint.abci.SnapshotOrBuilder> snapshotsBuilder_;

    /**
     * <code>repeated .tendermint.abci.Snapshot snapshots = 1 [json_name = "snapshots"];</code>
     */
    public java.util.List<com.tendermint.abci.Snapshot> getSnapshotsList() {
      if (snapshotsBuilder_ == null) {
        return java.util.Collections.unmodifiableList(snapshots_);
      } else {
        return snapshotsBuilder_.getMessageList();
      }
    }
    /**
     * <code>repeated .tendermint.abci.Snapshot snapshots = 1 [json_name = "snapshots"];</code>
     */
    public int getSnapshotsCount() {
      if (snapshotsBuilder_ == null) {
        return snapshots_.size();
      } else {
        return snapshotsBuilder_.getCount();
      }
    }
    /**
     * <code>repeated .tendermint.abci.Snapshot snapshots = 1 [json_name = "snapshots"];</code>
     */
    public com.tendermint.abci.Snapshot getSnapshots(int index) {
      if (snapshotsBuilder_ == null) {
        return snapshots_.get(index);
      } else {
        return snapshotsBuilder_.getMessage(index);
      }
    }
    /**
     * <code>repeated .tendermint.abci.Snapshot snapshots = 1 [json_name = "snapshots"];</code>
     */
    public Builder setSnapshots(
        int index, com.tendermint.abci.Snapshot value) {
      if (snapshotsBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureSnapshotsIsMutable();
        snapshots_.set(index, value);
        onChanged();
      } else {
        snapshotsBuilder_.setMessage(index, value);
      }
      return this;
    }
    /**
     * <code>repeated .tendermint.abci.Snapshot snapshots = 1 [json_name = "snapshots"];</code>
     */
    public Builder setSnapshots(
        int index, com.tendermint.abci.Snapshot.Builder builderForValue) {
      if (snapshotsBuilder_ == null) {
        ensureSnapshotsIsMutable();
        snapshots_.set(index, builderForValue.build());
        onChanged();
      } else {
        snapshotsBuilder_.setMessage(index, builderForValue.build());
      }
      return this;
    }
    /**
     * <code>repeated .tendermint.abci.Snapshot snapshots = 1 [json_name = "snapshots"];</code>
     */
    public Builder addSnapshots(com.tendermint.abci.Snapshot value) {
      if (snapshotsBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureSnapshotsIsMutable();
        snapshots_.add(value);
        onChanged();
      } else {
        snapshotsBuilder_.addMessage(value);
      }
      return this;
    }
    /**
     * <code>repeated .tendermint.abci.Snapshot snapshots = 1 [json_name = "snapshots"];</code>
     */
    public Builder addSnapshots(
        int index, com.tendermint.abci.Snapshot value) {
      if (snapshotsBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureSnapshotsIsMutable();
        snapshots_.add(index, value);
        onChanged();
      } else {
        snapshotsBuilder_.addMessage(index, value);
      }
      return this;
    }
    /**
     * <code>repeated .tendermint.abci.Snapshot snapshots = 1 [json_name = "snapshots"];</code>
     */
    public Builder addSnapshots(
        com.tendermint.abci.Snapshot.Builder builderForValue) {
      if (snapshotsBuilder_ == null) {
        ensureSnapshotsIsMutable();
        snapshots_.add(builderForValue.build());
        onChanged();
      } else {
        snapshotsBuilder_.addMessage(builderForValue.build());
      }
      return this;
    }
    /**
     * <code>repeated .tendermint.abci.Snapshot snapshots = 1 [json_name = "snapshots"];</code>
     */
    public Builder addSnapshots(
        int index, com.tendermint.abci.Snapshot.Builder builderForValue) {
      if (snapshotsBuilder_ == null) {
        ensureSnapshotsIsMutable();
        snapshots_.add(index, builderForValue.build());
        onChanged();
      } else {
        snapshotsBuilder_.addMessage(index, builderForValue.build());
      }
      return this;
    }
    /**
     * <code>repeated .tendermint.abci.Snapshot snapshots = 1 [json_name = "snapshots"];</code>
     */
    public Builder addAllSnapshots(
        java.lang.Iterable<? extends com.tendermint.abci.Snapshot> values) {
      if (snapshotsBuilder_ == null) {
        ensureSnapshotsIsMutable();
        com.google.protobuf.AbstractMessageLite.Builder.addAll(
            values, snapshots_);
        onChanged();
      } else {
        snapshotsBuilder_.addAllMessages(values);
      }
      return this;
    }
    /**
     * <code>repeated .tendermint.abci.Snapshot snapshots = 1 [json_name = "snapshots"];</code>
     */
    public Builder clearSnapshots() {
      if (snapshotsBuilder_ == null) {
        snapshots_ = java.util.Collections.emptyList();
        bitField0_ = (bitField0_ & ~0x00000001);
        onChanged();
      } else {
        snapshotsBuilder_.clear();
      }
      return this;
    }
    /**
     * <code>repeated .tendermint.abci.Snapshot snapshots = 1 [json_name = "snapshots"];</code>
     */
    public Builder removeSnapshots(int index) {
      if (snapshotsBuilder_ == null) {
        ensureSnapshotsIsMutable();
        snapshots_.remove(index);
        onChanged();
      } else {
        snapshotsBuilder_.remove(index);
      }
      return this;
    }
    /**
     * <code>repeated .tendermint.abci.Snapshot snapshots = 1 [json_name = "snapshots"];</code>
     */
    public com.tendermint.abci.Snapshot.Builder getSnapshotsBuilder(
        int index) {
      return getSnapshotsFieldBuilder().getBuilder(index);
    }
    /**
     * <code>repeated .tendermint.abci.Snapshot snapshots = 1 [json_name = "snapshots"];</code>
     */
    public com.tendermint.abci.SnapshotOrBuilder getSnapshotsOrBuilder(
        int index) {
      if (snapshotsBuilder_ == null) {
        return snapshots_.get(index);  } else {
        return snapshotsBuilder_.getMessageOrBuilder(index);
      }
    }
    /**
     * <code>repeated .tendermint.abci.Snapshot snapshots = 1 [json_name = "snapshots"];</code>
     */
    public java.util.List<? extends com.tendermint.abci.SnapshotOrBuilder> 
         getSnapshotsOrBuilderList() {
      if (snapshotsBuilder_ != null) {
        return snapshotsBuilder_.getMessageOrBuilderList();
      } else {
        return java.util.Collections.unmodifiableList(snapshots_);
      }
    }
    /**
     * <code>repeated .tendermint.abci.Snapshot snapshots = 1 [json_name = "snapshots"];</code>
     */
    public com.tendermint.abci.Snapshot.Builder addSnapshotsBuilder() {
      return getSnapshotsFieldBuilder().addBuilder(
          com.tendermint.abci.Snapshot.getDefaultInstance());
    }
    /**
     * <code>repeated .tendermint.abci.Snapshot snapshots = 1 [json_name = "snapshots"];</code>
     */
    public com.tendermint.abci.Snapshot.Builder addSnapshotsBuilder(
        int index) {
      return getSnapshotsFieldBuilder().addBuilder(
          index, com.tendermint.abci.Snapshot.getDefaultInstance());
    }
    /**
     * <code>repeated .tendermint.abci.Snapshot snapshots = 1 [json_name = "snapshots"];</code>
     */
    public java.util.List<com.tendermint.abci.Snapshot.Builder> 
         getSnapshotsBuilderList() {
      return getSnapshotsFieldBuilder().getBuilderList();
    }
    private com.google.protobuf.RepeatedFieldBuilderV3<
        com.tendermint.abci.Snapshot, com.tendermint.abci.Snapshot.Builder, com.tendermint.abci.SnapshotOrBuilder> 
        getSnapshotsFieldBuilder() {
      if (snapshotsBuilder_ == null) {
        snapshotsBuilder_ = new com.google.protobuf.RepeatedFieldBuilderV3<
            com.tendermint.abci.Snapshot, com.tendermint.abci.Snapshot.Builder, com.tendermint.abci.SnapshotOrBuilder>(
                snapshots_,
                ((bitField0_ & 0x00000001) != 0),
                getParentForChildren(),
                isClean());
        snapshots_ = null;
      }
      return snapshotsBuilder_;
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


    // @@protoc_insertion_point(builder_scope:tendermint.abci.ResponseListSnapshots)
  }

  // @@protoc_insertion_point(class_scope:tendermint.abci.ResponseListSnapshots)
  private static final com.tendermint.abci.ResponseListSnapshots DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new com.tendermint.abci.ResponseListSnapshots();
  }

  public static com.tendermint.abci.ResponseListSnapshots getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<ResponseListSnapshots>
      PARSER = new com.google.protobuf.AbstractParser<ResponseListSnapshots>() {
    @java.lang.Override
    public ResponseListSnapshots parsePartialFrom(
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

  public static com.google.protobuf.Parser<ResponseListSnapshots> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<ResponseListSnapshots> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public com.tendermint.abci.ResponseListSnapshots getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

