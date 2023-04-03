// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: schema/data/base/anyData.proto

package com.data;

/**
 * Protobuf type {@code data.ListData}
 */
public final class ListData extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:data.ListData)
    ListDataOrBuilder {
private static final long serialVersionUID = 0L;
  // Use ListData.newBuilder() to construct.
  private ListData(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private ListData() {
    dataList_ = java.util.Collections.emptyList();
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new ListData();
  }

  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return com.data.AnyDataProto.internal_static_data_ListData_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return com.data.AnyDataProto.internal_static_data_ListData_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            com.data.ListData.class, com.data.ListData.Builder.class);
  }

  public static final int DATA_LIST_FIELD_NUMBER = 1;
  @SuppressWarnings("serial")
  private java.util.List<com.data.AnyData> dataList_;
  /**
   * <code>repeated .data.AnyData data_list = 1 [json_name = "dataList"];</code>
   */
  @java.lang.Override
  public java.util.List<com.data.AnyData> getDataListList() {
    return dataList_;
  }
  /**
   * <code>repeated .data.AnyData data_list = 1 [json_name = "dataList"];</code>
   */
  @java.lang.Override
  public java.util.List<? extends com.data.AnyDataOrBuilder> 
      getDataListOrBuilderList() {
    return dataList_;
  }
  /**
   * <code>repeated .data.AnyData data_list = 1 [json_name = "dataList"];</code>
   */
  @java.lang.Override
  public int getDataListCount() {
    return dataList_.size();
  }
  /**
   * <code>repeated .data.AnyData data_list = 1 [json_name = "dataList"];</code>
   */
  @java.lang.Override
  public com.data.AnyData getDataList(int index) {
    return dataList_.get(index);
  }
  /**
   * <code>repeated .data.AnyData data_list = 1 [json_name = "dataList"];</code>
   */
  @java.lang.Override
  public com.data.AnyDataOrBuilder getDataListOrBuilder(
      int index) {
    return dataList_.get(index);
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
    for (int i = 0; i < dataList_.size(); i++) {
      output.writeMessage(1, dataList_.get(i));
    }
    getUnknownFields().writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    for (int i = 0; i < dataList_.size(); i++) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(1, dataList_.get(i));
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
    if (!(obj instanceof com.data.ListData)) {
      return super.equals(obj);
    }
    com.data.ListData other = (com.data.ListData) obj;

    if (!getDataListList()
        .equals(other.getDataListList())) return false;
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
    if (getDataListCount() > 0) {
      hash = (37 * hash) + DATA_LIST_FIELD_NUMBER;
      hash = (53 * hash) + getDataListList().hashCode();
    }
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static com.data.ListData parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.data.ListData parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.data.ListData parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.data.ListData parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.data.ListData parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.data.ListData parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.data.ListData parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.data.ListData parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.data.ListData parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static com.data.ListData parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.data.ListData parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.data.ListData parseFrom(
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
  public static Builder newBuilder(com.data.ListData prototype) {
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
   * Protobuf type {@code data.ListData}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:data.ListData)
      com.data.ListDataOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return com.data.AnyDataProto.internal_static_data_ListData_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return com.data.AnyDataProto.internal_static_data_ListData_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              com.data.ListData.class, com.data.ListData.Builder.class);
    }

    // Construct using com.data.ListData.newBuilder()
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
      if (dataListBuilder_ == null) {
        dataList_ = java.util.Collections.emptyList();
      } else {
        dataList_ = null;
        dataListBuilder_.clear();
      }
      bitField0_ = (bitField0_ & ~0x00000001);
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return com.data.AnyDataProto.internal_static_data_ListData_descriptor;
    }

    @java.lang.Override
    public com.data.ListData getDefaultInstanceForType() {
      return com.data.ListData.getDefaultInstance();
    }

    @java.lang.Override
    public com.data.ListData build() {
      com.data.ListData result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public com.data.ListData buildPartial() {
      com.data.ListData result = new com.data.ListData(this);
      buildPartialRepeatedFields(result);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartialRepeatedFields(com.data.ListData result) {
      if (dataListBuilder_ == null) {
        if (((bitField0_ & 0x00000001) != 0)) {
          dataList_ = java.util.Collections.unmodifiableList(dataList_);
          bitField0_ = (bitField0_ & ~0x00000001);
        }
        result.dataList_ = dataList_;
      } else {
        result.dataList_ = dataListBuilder_.build();
      }
    }

    private void buildPartial0(com.data.ListData result) {
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
      if (other instanceof com.data.ListData) {
        return mergeFrom((com.data.ListData)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(com.data.ListData other) {
      if (other == com.data.ListData.getDefaultInstance()) return this;
      if (dataListBuilder_ == null) {
        if (!other.dataList_.isEmpty()) {
          if (dataList_.isEmpty()) {
            dataList_ = other.dataList_;
            bitField0_ = (bitField0_ & ~0x00000001);
          } else {
            ensureDataListIsMutable();
            dataList_.addAll(other.dataList_);
          }
          onChanged();
        }
      } else {
        if (!other.dataList_.isEmpty()) {
          if (dataListBuilder_.isEmpty()) {
            dataListBuilder_.dispose();
            dataListBuilder_ = null;
            dataList_ = other.dataList_;
            bitField0_ = (bitField0_ & ~0x00000001);
            dataListBuilder_ = 
              com.google.protobuf.GeneratedMessageV3.alwaysUseFieldBuilders ?
                 getDataListFieldBuilder() : null;
          } else {
            dataListBuilder_.addAllMessages(other.dataList_);
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
              com.data.AnyData m =
                  input.readMessage(
                      com.data.AnyData.parser(),
                      extensionRegistry);
              if (dataListBuilder_ == null) {
                ensureDataListIsMutable();
                dataList_.add(m);
              } else {
                dataListBuilder_.addMessage(m);
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

    private java.util.List<com.data.AnyData> dataList_ =
      java.util.Collections.emptyList();
    private void ensureDataListIsMutable() {
      if (!((bitField0_ & 0x00000001) != 0)) {
        dataList_ = new java.util.ArrayList<com.data.AnyData>(dataList_);
        bitField0_ |= 0x00000001;
       }
    }

    private com.google.protobuf.RepeatedFieldBuilderV3<
        com.data.AnyData, com.data.AnyData.Builder, com.data.AnyDataOrBuilder> dataListBuilder_;

    /**
     * <code>repeated .data.AnyData data_list = 1 [json_name = "dataList"];</code>
     */
    public java.util.List<com.data.AnyData> getDataListList() {
      if (dataListBuilder_ == null) {
        return java.util.Collections.unmodifiableList(dataList_);
      } else {
        return dataListBuilder_.getMessageList();
      }
    }
    /**
     * <code>repeated .data.AnyData data_list = 1 [json_name = "dataList"];</code>
     */
    public int getDataListCount() {
      if (dataListBuilder_ == null) {
        return dataList_.size();
      } else {
        return dataListBuilder_.getCount();
      }
    }
    /**
     * <code>repeated .data.AnyData data_list = 1 [json_name = "dataList"];</code>
     */
    public com.data.AnyData getDataList(int index) {
      if (dataListBuilder_ == null) {
        return dataList_.get(index);
      } else {
        return dataListBuilder_.getMessage(index);
      }
    }
    /**
     * <code>repeated .data.AnyData data_list = 1 [json_name = "dataList"];</code>
     */
    public Builder setDataList(
        int index, com.data.AnyData value) {
      if (dataListBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureDataListIsMutable();
        dataList_.set(index, value);
        onChanged();
      } else {
        dataListBuilder_.setMessage(index, value);
      }
      return this;
    }
    /**
     * <code>repeated .data.AnyData data_list = 1 [json_name = "dataList"];</code>
     */
    public Builder setDataList(
        int index, com.data.AnyData.Builder builderForValue) {
      if (dataListBuilder_ == null) {
        ensureDataListIsMutable();
        dataList_.set(index, builderForValue.build());
        onChanged();
      } else {
        dataListBuilder_.setMessage(index, builderForValue.build());
      }
      return this;
    }
    /**
     * <code>repeated .data.AnyData data_list = 1 [json_name = "dataList"];</code>
     */
    public Builder addDataList(com.data.AnyData value) {
      if (dataListBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureDataListIsMutable();
        dataList_.add(value);
        onChanged();
      } else {
        dataListBuilder_.addMessage(value);
      }
      return this;
    }
    /**
     * <code>repeated .data.AnyData data_list = 1 [json_name = "dataList"];</code>
     */
    public Builder addDataList(
        int index, com.data.AnyData value) {
      if (dataListBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureDataListIsMutable();
        dataList_.add(index, value);
        onChanged();
      } else {
        dataListBuilder_.addMessage(index, value);
      }
      return this;
    }
    /**
     * <code>repeated .data.AnyData data_list = 1 [json_name = "dataList"];</code>
     */
    public Builder addDataList(
        com.data.AnyData.Builder builderForValue) {
      if (dataListBuilder_ == null) {
        ensureDataListIsMutable();
        dataList_.add(builderForValue.build());
        onChanged();
      } else {
        dataListBuilder_.addMessage(builderForValue.build());
      }
      return this;
    }
    /**
     * <code>repeated .data.AnyData data_list = 1 [json_name = "dataList"];</code>
     */
    public Builder addDataList(
        int index, com.data.AnyData.Builder builderForValue) {
      if (dataListBuilder_ == null) {
        ensureDataListIsMutable();
        dataList_.add(index, builderForValue.build());
        onChanged();
      } else {
        dataListBuilder_.addMessage(index, builderForValue.build());
      }
      return this;
    }
    /**
     * <code>repeated .data.AnyData data_list = 1 [json_name = "dataList"];</code>
     */
    public Builder addAllDataList(
        java.lang.Iterable<? extends com.data.AnyData> values) {
      if (dataListBuilder_ == null) {
        ensureDataListIsMutable();
        com.google.protobuf.AbstractMessageLite.Builder.addAll(
            values, dataList_);
        onChanged();
      } else {
        dataListBuilder_.addAllMessages(values);
      }
      return this;
    }
    /**
     * <code>repeated .data.AnyData data_list = 1 [json_name = "dataList"];</code>
     */
    public Builder clearDataList() {
      if (dataListBuilder_ == null) {
        dataList_ = java.util.Collections.emptyList();
        bitField0_ = (bitField0_ & ~0x00000001);
        onChanged();
      } else {
        dataListBuilder_.clear();
      }
      return this;
    }
    /**
     * <code>repeated .data.AnyData data_list = 1 [json_name = "dataList"];</code>
     */
    public Builder removeDataList(int index) {
      if (dataListBuilder_ == null) {
        ensureDataListIsMutable();
        dataList_.remove(index);
        onChanged();
      } else {
        dataListBuilder_.remove(index);
      }
      return this;
    }
    /**
     * <code>repeated .data.AnyData data_list = 1 [json_name = "dataList"];</code>
     */
    public com.data.AnyData.Builder getDataListBuilder(
        int index) {
      return getDataListFieldBuilder().getBuilder(index);
    }
    /**
     * <code>repeated .data.AnyData data_list = 1 [json_name = "dataList"];</code>
     */
    public com.data.AnyDataOrBuilder getDataListOrBuilder(
        int index) {
      if (dataListBuilder_ == null) {
        return dataList_.get(index);  } else {
        return dataListBuilder_.getMessageOrBuilder(index);
      }
    }
    /**
     * <code>repeated .data.AnyData data_list = 1 [json_name = "dataList"];</code>
     */
    public java.util.List<? extends com.data.AnyDataOrBuilder> 
         getDataListOrBuilderList() {
      if (dataListBuilder_ != null) {
        return dataListBuilder_.getMessageOrBuilderList();
      } else {
        return java.util.Collections.unmodifiableList(dataList_);
      }
    }
    /**
     * <code>repeated .data.AnyData data_list = 1 [json_name = "dataList"];</code>
     */
    public com.data.AnyData.Builder addDataListBuilder() {
      return getDataListFieldBuilder().addBuilder(
          com.data.AnyData.getDefaultInstance());
    }
    /**
     * <code>repeated .data.AnyData data_list = 1 [json_name = "dataList"];</code>
     */
    public com.data.AnyData.Builder addDataListBuilder(
        int index) {
      return getDataListFieldBuilder().addBuilder(
          index, com.data.AnyData.getDefaultInstance());
    }
    /**
     * <code>repeated .data.AnyData data_list = 1 [json_name = "dataList"];</code>
     */
    public java.util.List<com.data.AnyData.Builder> 
         getDataListBuilderList() {
      return getDataListFieldBuilder().getBuilderList();
    }
    private com.google.protobuf.RepeatedFieldBuilderV3<
        com.data.AnyData, com.data.AnyData.Builder, com.data.AnyDataOrBuilder> 
        getDataListFieldBuilder() {
      if (dataListBuilder_ == null) {
        dataListBuilder_ = new com.google.protobuf.RepeatedFieldBuilderV3<
            com.data.AnyData, com.data.AnyData.Builder, com.data.AnyDataOrBuilder>(
                dataList_,
                ((bitField0_ & 0x00000001) != 0),
                getParentForChildren(),
                isClean());
        dataList_ = null;
      }
      return dataListBuilder_;
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


    // @@protoc_insertion_point(builder_scope:data.ListData)
  }

  // @@protoc_insertion_point(class_scope:data.ListData)
  private static final com.data.ListData DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new com.data.ListData();
  }

  public static com.data.ListData getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<ListData>
      PARSER = new com.google.protobuf.AbstractParser<ListData>() {
    @java.lang.Override
    public ListData parsePartialFrom(
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

  public static com.google.protobuf.Parser<ListData> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<ListData> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public com.data.ListData getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

