// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: tendermint/types/block.proto

package com.tendermint.types;

/**
 * Protobuf type {@code tendermint.types.Block}
 */
public final class Block extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:tendermint.types.Block)
    BlockOrBuilder {
private static final long serialVersionUID = 0L;
  // Use Block.newBuilder() to construct.
  private Block(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private Block() {
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new Block();
  }

  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return com.tendermint.types.BlockProto.internal_static_tendermint_types_Block_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return com.tendermint.types.BlockProto.internal_static_tendermint_types_Block_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            com.tendermint.types.Block.class, com.tendermint.types.Block.Builder.class);
  }

  public static final int HEADER_FIELD_NUMBER = 1;
  private com.tendermint.types.Header header_;
  /**
   * <code>.tendermint.types.Header header = 1 [json_name = "header", (.gogoproto.nullable) = false];</code>
   * @return Whether the header field is set.
   */
  @java.lang.Override
  public boolean hasHeader() {
    return header_ != null;
  }
  /**
   * <code>.tendermint.types.Header header = 1 [json_name = "header", (.gogoproto.nullable) = false];</code>
   * @return The header.
   */
  @java.lang.Override
  public com.tendermint.types.Header getHeader() {
    return header_ == null ? com.tendermint.types.Header.getDefaultInstance() : header_;
  }
  /**
   * <code>.tendermint.types.Header header = 1 [json_name = "header", (.gogoproto.nullable) = false];</code>
   */
  @java.lang.Override
  public com.tendermint.types.HeaderOrBuilder getHeaderOrBuilder() {
    return header_ == null ? com.tendermint.types.Header.getDefaultInstance() : header_;
  }

  public static final int DATA_FIELD_NUMBER = 2;
  private com.tendermint.types.Data data_;
  /**
   * <code>.tendermint.types.Data data = 2 [json_name = "data", (.gogoproto.nullable) = false];</code>
   * @return Whether the data field is set.
   */
  @java.lang.Override
  public boolean hasData() {
    return data_ != null;
  }
  /**
   * <code>.tendermint.types.Data data = 2 [json_name = "data", (.gogoproto.nullable) = false];</code>
   * @return The data.
   */
  @java.lang.Override
  public com.tendermint.types.Data getData() {
    return data_ == null ? com.tendermint.types.Data.getDefaultInstance() : data_;
  }
  /**
   * <code>.tendermint.types.Data data = 2 [json_name = "data", (.gogoproto.nullable) = false];</code>
   */
  @java.lang.Override
  public com.tendermint.types.DataOrBuilder getDataOrBuilder() {
    return data_ == null ? com.tendermint.types.Data.getDefaultInstance() : data_;
  }

  public static final int EVIDENCE_FIELD_NUMBER = 3;
  private com.tendermint.types.EvidenceList evidence_;
  /**
   * <code>.tendermint.types.EvidenceList evidence = 3 [json_name = "evidence", (.gogoproto.nullable) = false];</code>
   * @return Whether the evidence field is set.
   */
  @java.lang.Override
  public boolean hasEvidence() {
    return evidence_ != null;
  }
  /**
   * <code>.tendermint.types.EvidenceList evidence = 3 [json_name = "evidence", (.gogoproto.nullable) = false];</code>
   * @return The evidence.
   */
  @java.lang.Override
  public com.tendermint.types.EvidenceList getEvidence() {
    return evidence_ == null ? com.tendermint.types.EvidenceList.getDefaultInstance() : evidence_;
  }
  /**
   * <code>.tendermint.types.EvidenceList evidence = 3 [json_name = "evidence", (.gogoproto.nullable) = false];</code>
   */
  @java.lang.Override
  public com.tendermint.types.EvidenceListOrBuilder getEvidenceOrBuilder() {
    return evidence_ == null ? com.tendermint.types.EvidenceList.getDefaultInstance() : evidence_;
  }

  public static final int LAST_COMMIT_FIELD_NUMBER = 4;
  private com.tendermint.types.Commit lastCommit_;
  /**
   * <code>.tendermint.types.Commit last_commit = 4 [json_name = "lastCommit"];</code>
   * @return Whether the lastCommit field is set.
   */
  @java.lang.Override
  public boolean hasLastCommit() {
    return lastCommit_ != null;
  }
  /**
   * <code>.tendermint.types.Commit last_commit = 4 [json_name = "lastCommit"];</code>
   * @return The lastCommit.
   */
  @java.lang.Override
  public com.tendermint.types.Commit getLastCommit() {
    return lastCommit_ == null ? com.tendermint.types.Commit.getDefaultInstance() : lastCommit_;
  }
  /**
   * <code>.tendermint.types.Commit last_commit = 4 [json_name = "lastCommit"];</code>
   */
  @java.lang.Override
  public com.tendermint.types.CommitOrBuilder getLastCommitOrBuilder() {
    return lastCommit_ == null ? com.tendermint.types.Commit.getDefaultInstance() : lastCommit_;
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
    if (header_ != null) {
      output.writeMessage(1, getHeader());
    }
    if (data_ != null) {
      output.writeMessage(2, getData());
    }
    if (evidence_ != null) {
      output.writeMessage(3, getEvidence());
    }
    if (lastCommit_ != null) {
      output.writeMessage(4, getLastCommit());
    }
    getUnknownFields().writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (header_ != null) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(1, getHeader());
    }
    if (data_ != null) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(2, getData());
    }
    if (evidence_ != null) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(3, getEvidence());
    }
    if (lastCommit_ != null) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(4, getLastCommit());
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
    if (!(obj instanceof com.tendermint.types.Block)) {
      return super.equals(obj);
    }
    com.tendermint.types.Block other = (com.tendermint.types.Block) obj;

    if (hasHeader() != other.hasHeader()) return false;
    if (hasHeader()) {
      if (!getHeader()
          .equals(other.getHeader())) return false;
    }
    if (hasData() != other.hasData()) return false;
    if (hasData()) {
      if (!getData()
          .equals(other.getData())) return false;
    }
    if (hasEvidence() != other.hasEvidence()) return false;
    if (hasEvidence()) {
      if (!getEvidence()
          .equals(other.getEvidence())) return false;
    }
    if (hasLastCommit() != other.hasLastCommit()) return false;
    if (hasLastCommit()) {
      if (!getLastCommit()
          .equals(other.getLastCommit())) return false;
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
    if (hasHeader()) {
      hash = (37 * hash) + HEADER_FIELD_NUMBER;
      hash = (53 * hash) + getHeader().hashCode();
    }
    if (hasData()) {
      hash = (37 * hash) + DATA_FIELD_NUMBER;
      hash = (53 * hash) + getData().hashCode();
    }
    if (hasEvidence()) {
      hash = (37 * hash) + EVIDENCE_FIELD_NUMBER;
      hash = (53 * hash) + getEvidence().hashCode();
    }
    if (hasLastCommit()) {
      hash = (37 * hash) + LAST_COMMIT_FIELD_NUMBER;
      hash = (53 * hash) + getLastCommit().hashCode();
    }
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static com.tendermint.types.Block parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.tendermint.types.Block parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.tendermint.types.Block parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.tendermint.types.Block parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.tendermint.types.Block parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.tendermint.types.Block parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.tendermint.types.Block parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.tendermint.types.Block parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.tendermint.types.Block parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static com.tendermint.types.Block parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.tendermint.types.Block parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.tendermint.types.Block parseFrom(
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
  public static Builder newBuilder(com.tendermint.types.Block prototype) {
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
   * Protobuf type {@code tendermint.types.Block}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:tendermint.types.Block)
      com.tendermint.types.BlockOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return com.tendermint.types.BlockProto.internal_static_tendermint_types_Block_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return com.tendermint.types.BlockProto.internal_static_tendermint_types_Block_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              com.tendermint.types.Block.class, com.tendermint.types.Block.Builder.class);
    }

    // Construct using com.tendermint.types.Block.newBuilder()
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
      header_ = null;
      if (headerBuilder_ != null) {
        headerBuilder_.dispose();
        headerBuilder_ = null;
      }
      data_ = null;
      if (dataBuilder_ != null) {
        dataBuilder_.dispose();
        dataBuilder_ = null;
      }
      evidence_ = null;
      if (evidenceBuilder_ != null) {
        evidenceBuilder_.dispose();
        evidenceBuilder_ = null;
      }
      lastCommit_ = null;
      if (lastCommitBuilder_ != null) {
        lastCommitBuilder_.dispose();
        lastCommitBuilder_ = null;
      }
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return com.tendermint.types.BlockProto.internal_static_tendermint_types_Block_descriptor;
    }

    @java.lang.Override
    public com.tendermint.types.Block getDefaultInstanceForType() {
      return com.tendermint.types.Block.getDefaultInstance();
    }

    @java.lang.Override
    public com.tendermint.types.Block build() {
      com.tendermint.types.Block result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public com.tendermint.types.Block buildPartial() {
      com.tendermint.types.Block result = new com.tendermint.types.Block(this);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartial0(com.tendermint.types.Block result) {
      int from_bitField0_ = bitField0_;
      if (((from_bitField0_ & 0x00000001) != 0)) {
        result.header_ = headerBuilder_ == null
            ? header_
            : headerBuilder_.build();
      }
      if (((from_bitField0_ & 0x00000002) != 0)) {
        result.data_ = dataBuilder_ == null
            ? data_
            : dataBuilder_.build();
      }
      if (((from_bitField0_ & 0x00000004) != 0)) {
        result.evidence_ = evidenceBuilder_ == null
            ? evidence_
            : evidenceBuilder_.build();
      }
      if (((from_bitField0_ & 0x00000008) != 0)) {
        result.lastCommit_ = lastCommitBuilder_ == null
            ? lastCommit_
            : lastCommitBuilder_.build();
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
      if (other instanceof com.tendermint.types.Block) {
        return mergeFrom((com.tendermint.types.Block)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(com.tendermint.types.Block other) {
      if (other == com.tendermint.types.Block.getDefaultInstance()) return this;
      if (other.hasHeader()) {
        mergeHeader(other.getHeader());
      }
      if (other.hasData()) {
        mergeData(other.getData());
      }
      if (other.hasEvidence()) {
        mergeEvidence(other.getEvidence());
      }
      if (other.hasLastCommit()) {
        mergeLastCommit(other.getLastCommit());
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
                  getHeaderFieldBuilder().getBuilder(),
                  extensionRegistry);
              bitField0_ |= 0x00000001;
              break;
            } // case 10
            case 18: {
              input.readMessage(
                  getDataFieldBuilder().getBuilder(),
                  extensionRegistry);
              bitField0_ |= 0x00000002;
              break;
            } // case 18
            case 26: {
              input.readMessage(
                  getEvidenceFieldBuilder().getBuilder(),
                  extensionRegistry);
              bitField0_ |= 0x00000004;
              break;
            } // case 26
            case 34: {
              input.readMessage(
                  getLastCommitFieldBuilder().getBuilder(),
                  extensionRegistry);
              bitField0_ |= 0x00000008;
              break;
            } // case 34
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

    private com.tendermint.types.Header header_;
    private com.google.protobuf.SingleFieldBuilderV3<
        com.tendermint.types.Header, com.tendermint.types.Header.Builder, com.tendermint.types.HeaderOrBuilder> headerBuilder_;
    /**
     * <code>.tendermint.types.Header header = 1 [json_name = "header", (.gogoproto.nullable) = false];</code>
     * @return Whether the header field is set.
     */
    public boolean hasHeader() {
      return ((bitField0_ & 0x00000001) != 0);
    }
    /**
     * <code>.tendermint.types.Header header = 1 [json_name = "header", (.gogoproto.nullable) = false];</code>
     * @return The header.
     */
    public com.tendermint.types.Header getHeader() {
      if (headerBuilder_ == null) {
        return header_ == null ? com.tendermint.types.Header.getDefaultInstance() : header_;
      } else {
        return headerBuilder_.getMessage();
      }
    }
    /**
     * <code>.tendermint.types.Header header = 1 [json_name = "header", (.gogoproto.nullable) = false];</code>
     */
    public Builder setHeader(com.tendermint.types.Header value) {
      if (headerBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        header_ = value;
      } else {
        headerBuilder_.setMessage(value);
      }
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <code>.tendermint.types.Header header = 1 [json_name = "header", (.gogoproto.nullable) = false];</code>
     */
    public Builder setHeader(
        com.tendermint.types.Header.Builder builderForValue) {
      if (headerBuilder_ == null) {
        header_ = builderForValue.build();
      } else {
        headerBuilder_.setMessage(builderForValue.build());
      }
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <code>.tendermint.types.Header header = 1 [json_name = "header", (.gogoproto.nullable) = false];</code>
     */
    public Builder mergeHeader(com.tendermint.types.Header value) {
      if (headerBuilder_ == null) {
        if (((bitField0_ & 0x00000001) != 0) &&
          header_ != null &&
          header_ != com.tendermint.types.Header.getDefaultInstance()) {
          getHeaderBuilder().mergeFrom(value);
        } else {
          header_ = value;
        }
      } else {
        headerBuilder_.mergeFrom(value);
      }
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <code>.tendermint.types.Header header = 1 [json_name = "header", (.gogoproto.nullable) = false];</code>
     */
    public Builder clearHeader() {
      bitField0_ = (bitField0_ & ~0x00000001);
      header_ = null;
      if (headerBuilder_ != null) {
        headerBuilder_.dispose();
        headerBuilder_ = null;
      }
      onChanged();
      return this;
    }
    /**
     * <code>.tendermint.types.Header header = 1 [json_name = "header", (.gogoproto.nullable) = false];</code>
     */
    public com.tendermint.types.Header.Builder getHeaderBuilder() {
      bitField0_ |= 0x00000001;
      onChanged();
      return getHeaderFieldBuilder().getBuilder();
    }
    /**
     * <code>.tendermint.types.Header header = 1 [json_name = "header", (.gogoproto.nullable) = false];</code>
     */
    public com.tendermint.types.HeaderOrBuilder getHeaderOrBuilder() {
      if (headerBuilder_ != null) {
        return headerBuilder_.getMessageOrBuilder();
      } else {
        return header_ == null ?
            com.tendermint.types.Header.getDefaultInstance() : header_;
      }
    }
    /**
     * <code>.tendermint.types.Header header = 1 [json_name = "header", (.gogoproto.nullable) = false];</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
        com.tendermint.types.Header, com.tendermint.types.Header.Builder, com.tendermint.types.HeaderOrBuilder> 
        getHeaderFieldBuilder() {
      if (headerBuilder_ == null) {
        headerBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
            com.tendermint.types.Header, com.tendermint.types.Header.Builder, com.tendermint.types.HeaderOrBuilder>(
                getHeader(),
                getParentForChildren(),
                isClean());
        header_ = null;
      }
      return headerBuilder_;
    }

    private com.tendermint.types.Data data_;
    private com.google.protobuf.SingleFieldBuilderV3<
        com.tendermint.types.Data, com.tendermint.types.Data.Builder, com.tendermint.types.DataOrBuilder> dataBuilder_;
    /**
     * <code>.tendermint.types.Data data = 2 [json_name = "data", (.gogoproto.nullable) = false];</code>
     * @return Whether the data field is set.
     */
    public boolean hasData() {
      return ((bitField0_ & 0x00000002) != 0);
    }
    /**
     * <code>.tendermint.types.Data data = 2 [json_name = "data", (.gogoproto.nullable) = false];</code>
     * @return The data.
     */
    public com.tendermint.types.Data getData() {
      if (dataBuilder_ == null) {
        return data_ == null ? com.tendermint.types.Data.getDefaultInstance() : data_;
      } else {
        return dataBuilder_.getMessage();
      }
    }
    /**
     * <code>.tendermint.types.Data data = 2 [json_name = "data", (.gogoproto.nullable) = false];</code>
     */
    public Builder setData(com.tendermint.types.Data value) {
      if (dataBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        data_ = value;
      } else {
        dataBuilder_.setMessage(value);
      }
      bitField0_ |= 0x00000002;
      onChanged();
      return this;
    }
    /**
     * <code>.tendermint.types.Data data = 2 [json_name = "data", (.gogoproto.nullable) = false];</code>
     */
    public Builder setData(
        com.tendermint.types.Data.Builder builderForValue) {
      if (dataBuilder_ == null) {
        data_ = builderForValue.build();
      } else {
        dataBuilder_.setMessage(builderForValue.build());
      }
      bitField0_ |= 0x00000002;
      onChanged();
      return this;
    }
    /**
     * <code>.tendermint.types.Data data = 2 [json_name = "data", (.gogoproto.nullable) = false];</code>
     */
    public Builder mergeData(com.tendermint.types.Data value) {
      if (dataBuilder_ == null) {
        if (((bitField0_ & 0x00000002) != 0) &&
          data_ != null &&
          data_ != com.tendermint.types.Data.getDefaultInstance()) {
          getDataBuilder().mergeFrom(value);
        } else {
          data_ = value;
        }
      } else {
        dataBuilder_.mergeFrom(value);
      }
      bitField0_ |= 0x00000002;
      onChanged();
      return this;
    }
    /**
     * <code>.tendermint.types.Data data = 2 [json_name = "data", (.gogoproto.nullable) = false];</code>
     */
    public Builder clearData() {
      bitField0_ = (bitField0_ & ~0x00000002);
      data_ = null;
      if (dataBuilder_ != null) {
        dataBuilder_.dispose();
        dataBuilder_ = null;
      }
      onChanged();
      return this;
    }
    /**
     * <code>.tendermint.types.Data data = 2 [json_name = "data", (.gogoproto.nullable) = false];</code>
     */
    public com.tendermint.types.Data.Builder getDataBuilder() {
      bitField0_ |= 0x00000002;
      onChanged();
      return getDataFieldBuilder().getBuilder();
    }
    /**
     * <code>.tendermint.types.Data data = 2 [json_name = "data", (.gogoproto.nullable) = false];</code>
     */
    public com.tendermint.types.DataOrBuilder getDataOrBuilder() {
      if (dataBuilder_ != null) {
        return dataBuilder_.getMessageOrBuilder();
      } else {
        return data_ == null ?
            com.tendermint.types.Data.getDefaultInstance() : data_;
      }
    }
    /**
     * <code>.tendermint.types.Data data = 2 [json_name = "data", (.gogoproto.nullable) = false];</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
        com.tendermint.types.Data, com.tendermint.types.Data.Builder, com.tendermint.types.DataOrBuilder> 
        getDataFieldBuilder() {
      if (dataBuilder_ == null) {
        dataBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
            com.tendermint.types.Data, com.tendermint.types.Data.Builder, com.tendermint.types.DataOrBuilder>(
                getData(),
                getParentForChildren(),
                isClean());
        data_ = null;
      }
      return dataBuilder_;
    }

    private com.tendermint.types.EvidenceList evidence_;
    private com.google.protobuf.SingleFieldBuilderV3<
        com.tendermint.types.EvidenceList, com.tendermint.types.EvidenceList.Builder, com.tendermint.types.EvidenceListOrBuilder> evidenceBuilder_;
    /**
     * <code>.tendermint.types.EvidenceList evidence = 3 [json_name = "evidence", (.gogoproto.nullable) = false];</code>
     * @return Whether the evidence field is set.
     */
    public boolean hasEvidence() {
      return ((bitField0_ & 0x00000004) != 0);
    }
    /**
     * <code>.tendermint.types.EvidenceList evidence = 3 [json_name = "evidence", (.gogoproto.nullable) = false];</code>
     * @return The evidence.
     */
    public com.tendermint.types.EvidenceList getEvidence() {
      if (evidenceBuilder_ == null) {
        return evidence_ == null ? com.tendermint.types.EvidenceList.getDefaultInstance() : evidence_;
      } else {
        return evidenceBuilder_.getMessage();
      }
    }
    /**
     * <code>.tendermint.types.EvidenceList evidence = 3 [json_name = "evidence", (.gogoproto.nullable) = false];</code>
     */
    public Builder setEvidence(com.tendermint.types.EvidenceList value) {
      if (evidenceBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        evidence_ = value;
      } else {
        evidenceBuilder_.setMessage(value);
      }
      bitField0_ |= 0x00000004;
      onChanged();
      return this;
    }
    /**
     * <code>.tendermint.types.EvidenceList evidence = 3 [json_name = "evidence", (.gogoproto.nullable) = false];</code>
     */
    public Builder setEvidence(
        com.tendermint.types.EvidenceList.Builder builderForValue) {
      if (evidenceBuilder_ == null) {
        evidence_ = builderForValue.build();
      } else {
        evidenceBuilder_.setMessage(builderForValue.build());
      }
      bitField0_ |= 0x00000004;
      onChanged();
      return this;
    }
    /**
     * <code>.tendermint.types.EvidenceList evidence = 3 [json_name = "evidence", (.gogoproto.nullable) = false];</code>
     */
    public Builder mergeEvidence(com.tendermint.types.EvidenceList value) {
      if (evidenceBuilder_ == null) {
        if (((bitField0_ & 0x00000004) != 0) &&
          evidence_ != null &&
          evidence_ != com.tendermint.types.EvidenceList.getDefaultInstance()) {
          getEvidenceBuilder().mergeFrom(value);
        } else {
          evidence_ = value;
        }
      } else {
        evidenceBuilder_.mergeFrom(value);
      }
      bitField0_ |= 0x00000004;
      onChanged();
      return this;
    }
    /**
     * <code>.tendermint.types.EvidenceList evidence = 3 [json_name = "evidence", (.gogoproto.nullable) = false];</code>
     */
    public Builder clearEvidence() {
      bitField0_ = (bitField0_ & ~0x00000004);
      evidence_ = null;
      if (evidenceBuilder_ != null) {
        evidenceBuilder_.dispose();
        evidenceBuilder_ = null;
      }
      onChanged();
      return this;
    }
    /**
     * <code>.tendermint.types.EvidenceList evidence = 3 [json_name = "evidence", (.gogoproto.nullable) = false];</code>
     */
    public com.tendermint.types.EvidenceList.Builder getEvidenceBuilder() {
      bitField0_ |= 0x00000004;
      onChanged();
      return getEvidenceFieldBuilder().getBuilder();
    }
    /**
     * <code>.tendermint.types.EvidenceList evidence = 3 [json_name = "evidence", (.gogoproto.nullable) = false];</code>
     */
    public com.tendermint.types.EvidenceListOrBuilder getEvidenceOrBuilder() {
      if (evidenceBuilder_ != null) {
        return evidenceBuilder_.getMessageOrBuilder();
      } else {
        return evidence_ == null ?
            com.tendermint.types.EvidenceList.getDefaultInstance() : evidence_;
      }
    }
    /**
     * <code>.tendermint.types.EvidenceList evidence = 3 [json_name = "evidence", (.gogoproto.nullable) = false];</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
        com.tendermint.types.EvidenceList, com.tendermint.types.EvidenceList.Builder, com.tendermint.types.EvidenceListOrBuilder> 
        getEvidenceFieldBuilder() {
      if (evidenceBuilder_ == null) {
        evidenceBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
            com.tendermint.types.EvidenceList, com.tendermint.types.EvidenceList.Builder, com.tendermint.types.EvidenceListOrBuilder>(
                getEvidence(),
                getParentForChildren(),
                isClean());
        evidence_ = null;
      }
      return evidenceBuilder_;
    }

    private com.tendermint.types.Commit lastCommit_;
    private com.google.protobuf.SingleFieldBuilderV3<
        com.tendermint.types.Commit, com.tendermint.types.Commit.Builder, com.tendermint.types.CommitOrBuilder> lastCommitBuilder_;
    /**
     * <code>.tendermint.types.Commit last_commit = 4 [json_name = "lastCommit"];</code>
     * @return Whether the lastCommit field is set.
     */
    public boolean hasLastCommit() {
      return ((bitField0_ & 0x00000008) != 0);
    }
    /**
     * <code>.tendermint.types.Commit last_commit = 4 [json_name = "lastCommit"];</code>
     * @return The lastCommit.
     */
    public com.tendermint.types.Commit getLastCommit() {
      if (lastCommitBuilder_ == null) {
        return lastCommit_ == null ? com.tendermint.types.Commit.getDefaultInstance() : lastCommit_;
      } else {
        return lastCommitBuilder_.getMessage();
      }
    }
    /**
     * <code>.tendermint.types.Commit last_commit = 4 [json_name = "lastCommit"];</code>
     */
    public Builder setLastCommit(com.tendermint.types.Commit value) {
      if (lastCommitBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        lastCommit_ = value;
      } else {
        lastCommitBuilder_.setMessage(value);
      }
      bitField0_ |= 0x00000008;
      onChanged();
      return this;
    }
    /**
     * <code>.tendermint.types.Commit last_commit = 4 [json_name = "lastCommit"];</code>
     */
    public Builder setLastCommit(
        com.tendermint.types.Commit.Builder builderForValue) {
      if (lastCommitBuilder_ == null) {
        lastCommit_ = builderForValue.build();
      } else {
        lastCommitBuilder_.setMessage(builderForValue.build());
      }
      bitField0_ |= 0x00000008;
      onChanged();
      return this;
    }
    /**
     * <code>.tendermint.types.Commit last_commit = 4 [json_name = "lastCommit"];</code>
     */
    public Builder mergeLastCommit(com.tendermint.types.Commit value) {
      if (lastCommitBuilder_ == null) {
        if (((bitField0_ & 0x00000008) != 0) &&
          lastCommit_ != null &&
          lastCommit_ != com.tendermint.types.Commit.getDefaultInstance()) {
          getLastCommitBuilder().mergeFrom(value);
        } else {
          lastCommit_ = value;
        }
      } else {
        lastCommitBuilder_.mergeFrom(value);
      }
      bitField0_ |= 0x00000008;
      onChanged();
      return this;
    }
    /**
     * <code>.tendermint.types.Commit last_commit = 4 [json_name = "lastCommit"];</code>
     */
    public Builder clearLastCommit() {
      bitField0_ = (bitField0_ & ~0x00000008);
      lastCommit_ = null;
      if (lastCommitBuilder_ != null) {
        lastCommitBuilder_.dispose();
        lastCommitBuilder_ = null;
      }
      onChanged();
      return this;
    }
    /**
     * <code>.tendermint.types.Commit last_commit = 4 [json_name = "lastCommit"];</code>
     */
    public com.tendermint.types.Commit.Builder getLastCommitBuilder() {
      bitField0_ |= 0x00000008;
      onChanged();
      return getLastCommitFieldBuilder().getBuilder();
    }
    /**
     * <code>.tendermint.types.Commit last_commit = 4 [json_name = "lastCommit"];</code>
     */
    public com.tendermint.types.CommitOrBuilder getLastCommitOrBuilder() {
      if (lastCommitBuilder_ != null) {
        return lastCommitBuilder_.getMessageOrBuilder();
      } else {
        return lastCommit_ == null ?
            com.tendermint.types.Commit.getDefaultInstance() : lastCommit_;
      }
    }
    /**
     * <code>.tendermint.types.Commit last_commit = 4 [json_name = "lastCommit"];</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
        com.tendermint.types.Commit, com.tendermint.types.Commit.Builder, com.tendermint.types.CommitOrBuilder> 
        getLastCommitFieldBuilder() {
      if (lastCommitBuilder_ == null) {
        lastCommitBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
            com.tendermint.types.Commit, com.tendermint.types.Commit.Builder, com.tendermint.types.CommitOrBuilder>(
                getLastCommit(),
                getParentForChildren(),
                isClean());
        lastCommit_ = null;
      }
      return lastCommitBuilder_;
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


    // @@protoc_insertion_point(builder_scope:tendermint.types.Block)
  }

  // @@protoc_insertion_point(class_scope:tendermint.types.Block)
  private static final com.tendermint.types.Block DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new com.tendermint.types.Block();
  }

  public static com.tendermint.types.Block getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<Block>
      PARSER = new com.google.protobuf.AbstractParser<Block>() {
    @java.lang.Override
    public Block parsePartialFrom(
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

  public static com.google.protobuf.Parser<Block> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<Block> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public com.tendermint.types.Block getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

