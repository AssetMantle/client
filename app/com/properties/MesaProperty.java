// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: schema/properties/base/mesaProperty.v1.proto

package com.properties;

/**
 * Protobuf type {@code properties.MesaProperty}
 */
public final class MesaProperty extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:properties.MesaProperty)
    MesaPropertyOrBuilder {
private static final long serialVersionUID = 0L;
  // Use MesaProperty.newBuilder() to construct.
  private MesaProperty(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private MesaProperty() {
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new MesaProperty();
  }

  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return com.properties.MesaPropertyV1Proto.internal_static_properties_MesaProperty_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return com.properties.MesaPropertyV1Proto.internal_static_properties_MesaProperty_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            com.properties.MesaProperty.class, com.properties.MesaProperty.Builder.class);
  }

  public static final int I_D_FIELD_NUMBER = 1;
  private com.ids.PropertyID iD_;
  /**
   * <code>.ids.PropertyID i_d = 1 [json_name = "iD"];</code>
   * @return Whether the iD field is set.
   */
  @java.lang.Override
  public boolean hasID() {
    return iD_ != null;
  }
  /**
   * <code>.ids.PropertyID i_d = 1 [json_name = "iD"];</code>
   * @return The iD.
   */
  @java.lang.Override
  public com.ids.PropertyID getID() {
    return iD_ == null ? com.ids.PropertyID.getDefaultInstance() : iD_;
  }
  /**
   * <code>.ids.PropertyID i_d = 1 [json_name = "iD"];</code>
   */
  @java.lang.Override
  public com.ids.PropertyIDOrBuilder getIDOrBuilder() {
    return iD_ == null ? com.ids.PropertyID.getDefaultInstance() : iD_;
  }

  public static final int DATA_I_D_FIELD_NUMBER = 2;
  private com.ids.DataID dataID_;
  /**
   * <code>.ids.DataID data_i_d = 2 [json_name = "dataID"];</code>
   * @return Whether the dataID field is set.
   */
  @java.lang.Override
  public boolean hasDataID() {
    return dataID_ != null;
  }
  /**
   * <code>.ids.DataID data_i_d = 2 [json_name = "dataID"];</code>
   * @return The dataID.
   */
  @java.lang.Override
  public com.ids.DataID getDataID() {
    return dataID_ == null ? com.ids.DataID.getDefaultInstance() : dataID_;
  }
  /**
   * <code>.ids.DataID data_i_d = 2 [json_name = "dataID"];</code>
   */
  @java.lang.Override
  public com.ids.DataIDOrBuilder getDataIDOrBuilder() {
    return dataID_ == null ? com.ids.DataID.getDefaultInstance() : dataID_;
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
    if (iD_ != null) {
      output.writeMessage(1, getID());
    }
    if (dataID_ != null) {
      output.writeMessage(2, getDataID());
    }
    getUnknownFields().writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (iD_ != null) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(1, getID());
    }
    if (dataID_ != null) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(2, getDataID());
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
    if (!(obj instanceof com.properties.MesaProperty)) {
      return super.equals(obj);
    }
    com.properties.MesaProperty other = (com.properties.MesaProperty) obj;

    if (hasID() != other.hasID()) return false;
    if (hasID()) {
      if (!getID()
          .equals(other.getID())) return false;
    }
    if (hasDataID() != other.hasDataID()) return false;
    if (hasDataID()) {
      if (!getDataID()
          .equals(other.getDataID())) return false;
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
    if (hasID()) {
      hash = (37 * hash) + I_D_FIELD_NUMBER;
      hash = (53 * hash) + getID().hashCode();
    }
    if (hasDataID()) {
      hash = (37 * hash) + DATA_I_D_FIELD_NUMBER;
      hash = (53 * hash) + getDataID().hashCode();
    }
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static com.properties.MesaProperty parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.properties.MesaProperty parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.properties.MesaProperty parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.properties.MesaProperty parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.properties.MesaProperty parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.properties.MesaProperty parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.properties.MesaProperty parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.properties.MesaProperty parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.properties.MesaProperty parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static com.properties.MesaProperty parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.properties.MesaProperty parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.properties.MesaProperty parseFrom(
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
  public static Builder newBuilder(com.properties.MesaProperty prototype) {
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
   * Protobuf type {@code properties.MesaProperty}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:properties.MesaProperty)
      com.properties.MesaPropertyOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return com.properties.MesaPropertyV1Proto.internal_static_properties_MesaProperty_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return com.properties.MesaPropertyV1Proto.internal_static_properties_MesaProperty_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              com.properties.MesaProperty.class, com.properties.MesaProperty.Builder.class);
    }

    // Construct using com.properties.MesaProperty.newBuilder()
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
      iD_ = null;
      if (iDBuilder_ != null) {
        iDBuilder_.dispose();
        iDBuilder_ = null;
      }
      dataID_ = null;
      if (dataIDBuilder_ != null) {
        dataIDBuilder_.dispose();
        dataIDBuilder_ = null;
      }
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return com.properties.MesaPropertyV1Proto.internal_static_properties_MesaProperty_descriptor;
    }

    @java.lang.Override
    public com.properties.MesaProperty getDefaultInstanceForType() {
      return com.properties.MesaProperty.getDefaultInstance();
    }

    @java.lang.Override
    public com.properties.MesaProperty build() {
      com.properties.MesaProperty result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public com.properties.MesaProperty buildPartial() {
      com.properties.MesaProperty result = new com.properties.MesaProperty(this);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartial0(com.properties.MesaProperty result) {
      int from_bitField0_ = bitField0_;
      if (((from_bitField0_ & 0x00000001) != 0)) {
        result.iD_ = iDBuilder_ == null
            ? iD_
            : iDBuilder_.build();
      }
      if (((from_bitField0_ & 0x00000002) != 0)) {
        result.dataID_ = dataIDBuilder_ == null
            ? dataID_
            : dataIDBuilder_.build();
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
      if (other instanceof com.properties.MesaProperty) {
        return mergeFrom((com.properties.MesaProperty)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(com.properties.MesaProperty other) {
      if (other == com.properties.MesaProperty.getDefaultInstance()) return this;
      if (other.hasID()) {
        mergeID(other.getID());
      }
      if (other.hasDataID()) {
        mergeDataID(other.getDataID());
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
                  getIDFieldBuilder().getBuilder(),
                  extensionRegistry);
              bitField0_ |= 0x00000001;
              break;
            } // case 10
            case 18: {
              input.readMessage(
                  getDataIDFieldBuilder().getBuilder(),
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

    private com.ids.PropertyID iD_;
    private com.google.protobuf.SingleFieldBuilderV3<
        com.ids.PropertyID, com.ids.PropertyID.Builder, com.ids.PropertyIDOrBuilder> iDBuilder_;
    /**
     * <code>.ids.PropertyID i_d = 1 [json_name = "iD"];</code>
     * @return Whether the iD field is set.
     */
    public boolean hasID() {
      return ((bitField0_ & 0x00000001) != 0);
    }
    /**
     * <code>.ids.PropertyID i_d = 1 [json_name = "iD"];</code>
     * @return The iD.
     */
    public com.ids.PropertyID getID() {
      if (iDBuilder_ == null) {
        return iD_ == null ? com.ids.PropertyID.getDefaultInstance() : iD_;
      } else {
        return iDBuilder_.getMessage();
      }
    }
    /**
     * <code>.ids.PropertyID i_d = 1 [json_name = "iD"];</code>
     */
    public Builder setID(com.ids.PropertyID value) {
      if (iDBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        iD_ = value;
      } else {
        iDBuilder_.setMessage(value);
      }
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <code>.ids.PropertyID i_d = 1 [json_name = "iD"];</code>
     */
    public Builder setID(
        com.ids.PropertyID.Builder builderForValue) {
      if (iDBuilder_ == null) {
        iD_ = builderForValue.build();
      } else {
        iDBuilder_.setMessage(builderForValue.build());
      }
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <code>.ids.PropertyID i_d = 1 [json_name = "iD"];</code>
     */
    public Builder mergeID(com.ids.PropertyID value) {
      if (iDBuilder_ == null) {
        if (((bitField0_ & 0x00000001) != 0) &&
          iD_ != null &&
          iD_ != com.ids.PropertyID.getDefaultInstance()) {
          getIDBuilder().mergeFrom(value);
        } else {
          iD_ = value;
        }
      } else {
        iDBuilder_.mergeFrom(value);
      }
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <code>.ids.PropertyID i_d = 1 [json_name = "iD"];</code>
     */
    public Builder clearID() {
      bitField0_ = (bitField0_ & ~0x00000001);
      iD_ = null;
      if (iDBuilder_ != null) {
        iDBuilder_.dispose();
        iDBuilder_ = null;
      }
      onChanged();
      return this;
    }
    /**
     * <code>.ids.PropertyID i_d = 1 [json_name = "iD"];</code>
     */
    public com.ids.PropertyID.Builder getIDBuilder() {
      bitField0_ |= 0x00000001;
      onChanged();
      return getIDFieldBuilder().getBuilder();
    }
    /**
     * <code>.ids.PropertyID i_d = 1 [json_name = "iD"];</code>
     */
    public com.ids.PropertyIDOrBuilder getIDOrBuilder() {
      if (iDBuilder_ != null) {
        return iDBuilder_.getMessageOrBuilder();
      } else {
        return iD_ == null ?
            com.ids.PropertyID.getDefaultInstance() : iD_;
      }
    }
    /**
     * <code>.ids.PropertyID i_d = 1 [json_name = "iD"];</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
        com.ids.PropertyID, com.ids.PropertyID.Builder, com.ids.PropertyIDOrBuilder> 
        getIDFieldBuilder() {
      if (iDBuilder_ == null) {
        iDBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
            com.ids.PropertyID, com.ids.PropertyID.Builder, com.ids.PropertyIDOrBuilder>(
                getID(),
                getParentForChildren(),
                isClean());
        iD_ = null;
      }
      return iDBuilder_;
    }

    private com.ids.DataID dataID_;
    private com.google.protobuf.SingleFieldBuilderV3<
        com.ids.DataID, com.ids.DataID.Builder, com.ids.DataIDOrBuilder> dataIDBuilder_;
    /**
     * <code>.ids.DataID data_i_d = 2 [json_name = "dataID"];</code>
     * @return Whether the dataID field is set.
     */
    public boolean hasDataID() {
      return ((bitField0_ & 0x00000002) != 0);
    }
    /**
     * <code>.ids.DataID data_i_d = 2 [json_name = "dataID"];</code>
     * @return The dataID.
     */
    public com.ids.DataID getDataID() {
      if (dataIDBuilder_ == null) {
        return dataID_ == null ? com.ids.DataID.getDefaultInstance() : dataID_;
      } else {
        return dataIDBuilder_.getMessage();
      }
    }
    /**
     * <code>.ids.DataID data_i_d = 2 [json_name = "dataID"];</code>
     */
    public Builder setDataID(com.ids.DataID value) {
      if (dataIDBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        dataID_ = value;
      } else {
        dataIDBuilder_.setMessage(value);
      }
      bitField0_ |= 0x00000002;
      onChanged();
      return this;
    }
    /**
     * <code>.ids.DataID data_i_d = 2 [json_name = "dataID"];</code>
     */
    public Builder setDataID(
        com.ids.DataID.Builder builderForValue) {
      if (dataIDBuilder_ == null) {
        dataID_ = builderForValue.build();
      } else {
        dataIDBuilder_.setMessage(builderForValue.build());
      }
      bitField0_ |= 0x00000002;
      onChanged();
      return this;
    }
    /**
     * <code>.ids.DataID data_i_d = 2 [json_name = "dataID"];</code>
     */
    public Builder mergeDataID(com.ids.DataID value) {
      if (dataIDBuilder_ == null) {
        if (((bitField0_ & 0x00000002) != 0) &&
          dataID_ != null &&
          dataID_ != com.ids.DataID.getDefaultInstance()) {
          getDataIDBuilder().mergeFrom(value);
        } else {
          dataID_ = value;
        }
      } else {
        dataIDBuilder_.mergeFrom(value);
      }
      bitField0_ |= 0x00000002;
      onChanged();
      return this;
    }
    /**
     * <code>.ids.DataID data_i_d = 2 [json_name = "dataID"];</code>
     */
    public Builder clearDataID() {
      bitField0_ = (bitField0_ & ~0x00000002);
      dataID_ = null;
      if (dataIDBuilder_ != null) {
        dataIDBuilder_.dispose();
        dataIDBuilder_ = null;
      }
      onChanged();
      return this;
    }
    /**
     * <code>.ids.DataID data_i_d = 2 [json_name = "dataID"];</code>
     */
    public com.ids.DataID.Builder getDataIDBuilder() {
      bitField0_ |= 0x00000002;
      onChanged();
      return getDataIDFieldBuilder().getBuilder();
    }
    /**
     * <code>.ids.DataID data_i_d = 2 [json_name = "dataID"];</code>
     */
    public com.ids.DataIDOrBuilder getDataIDOrBuilder() {
      if (dataIDBuilder_ != null) {
        return dataIDBuilder_.getMessageOrBuilder();
      } else {
        return dataID_ == null ?
            com.ids.DataID.getDefaultInstance() : dataID_;
      }
    }
    /**
     * <code>.ids.DataID data_i_d = 2 [json_name = "dataID"];</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
        com.ids.DataID, com.ids.DataID.Builder, com.ids.DataIDOrBuilder> 
        getDataIDFieldBuilder() {
      if (dataIDBuilder_ == null) {
        dataIDBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
            com.ids.DataID, com.ids.DataID.Builder, com.ids.DataIDOrBuilder>(
                getDataID(),
                getParentForChildren(),
                isClean());
        dataID_ = null;
      }
      return dataIDBuilder_;
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


    // @@protoc_insertion_point(builder_scope:properties.MesaProperty)
  }

  // @@protoc_insertion_point(class_scope:properties.MesaProperty)
  private static final com.properties.MesaProperty DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new com.properties.MesaProperty();
  }

  public static com.properties.MesaProperty getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<MesaProperty>
      PARSER = new com.google.protobuf.AbstractParser<MesaProperty>() {
    @java.lang.Override
    public MesaProperty parsePartialFrom(
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

  public static com.google.protobuf.Parser<MesaProperty> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<MesaProperty> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public com.properties.MesaProperty getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

