// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: ibc/applications/fee/v1/fee.proto

package com.ibc.applications.fee.v1;

/**
 * <pre>
 * PacketFees contains a list of type PacketFee
 * </pre>
 *
 * Protobuf type {@code ibc.applications.fee.v1.PacketFees}
 */
public final class PacketFees extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:ibc.applications.fee.v1.PacketFees)
    PacketFeesOrBuilder {
private static final long serialVersionUID = 0L;
  // Use PacketFees.newBuilder() to construct.
  private PacketFees(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private PacketFees() {
    packetFees_ = java.util.Collections.emptyList();
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new PacketFees();
  }

  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return com.ibc.applications.fee.v1.FeeProto.internal_static_ibc_applications_fee_v1_PacketFees_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return com.ibc.applications.fee.v1.FeeProto.internal_static_ibc_applications_fee_v1_PacketFees_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            com.ibc.applications.fee.v1.PacketFees.class, com.ibc.applications.fee.v1.PacketFees.Builder.class);
  }

  public static final int PACKET_FEES_FIELD_NUMBER = 1;
  @SuppressWarnings("serial")
  private java.util.List<com.ibc.applications.fee.v1.PacketFee> packetFees_;
  /**
   * <pre>
   * list of packet fees
   * </pre>
   *
   * <code>repeated .ibc.applications.fee.v1.PacketFee packet_fees = 1 [json_name = "packetFees", (.gogoproto.nullable) = false, (.gogoproto.moretags) = "yaml:&#92;"packet_fees&#92;""];</code>
   */
  @java.lang.Override
  public java.util.List<com.ibc.applications.fee.v1.PacketFee> getPacketFeesList() {
    return packetFees_;
  }
  /**
   * <pre>
   * list of packet fees
   * </pre>
   *
   * <code>repeated .ibc.applications.fee.v1.PacketFee packet_fees = 1 [json_name = "packetFees", (.gogoproto.nullable) = false, (.gogoproto.moretags) = "yaml:&#92;"packet_fees&#92;""];</code>
   */
  @java.lang.Override
  public java.util.List<? extends com.ibc.applications.fee.v1.PacketFeeOrBuilder> 
      getPacketFeesOrBuilderList() {
    return packetFees_;
  }
  /**
   * <pre>
   * list of packet fees
   * </pre>
   *
   * <code>repeated .ibc.applications.fee.v1.PacketFee packet_fees = 1 [json_name = "packetFees", (.gogoproto.nullable) = false, (.gogoproto.moretags) = "yaml:&#92;"packet_fees&#92;""];</code>
   */
  @java.lang.Override
  public int getPacketFeesCount() {
    return packetFees_.size();
  }
  /**
   * <pre>
   * list of packet fees
   * </pre>
   *
   * <code>repeated .ibc.applications.fee.v1.PacketFee packet_fees = 1 [json_name = "packetFees", (.gogoproto.nullable) = false, (.gogoproto.moretags) = "yaml:&#92;"packet_fees&#92;""];</code>
   */
  @java.lang.Override
  public com.ibc.applications.fee.v1.PacketFee getPacketFees(int index) {
    return packetFees_.get(index);
  }
  /**
   * <pre>
   * list of packet fees
   * </pre>
   *
   * <code>repeated .ibc.applications.fee.v1.PacketFee packet_fees = 1 [json_name = "packetFees", (.gogoproto.nullable) = false, (.gogoproto.moretags) = "yaml:&#92;"packet_fees&#92;""];</code>
   */
  @java.lang.Override
  public com.ibc.applications.fee.v1.PacketFeeOrBuilder getPacketFeesOrBuilder(
      int index) {
    return packetFees_.get(index);
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
    for (int i = 0; i < packetFees_.size(); i++) {
      output.writeMessage(1, packetFees_.get(i));
    }
    getUnknownFields().writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    for (int i = 0; i < packetFees_.size(); i++) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(1, packetFees_.get(i));
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
    if (!(obj instanceof com.ibc.applications.fee.v1.PacketFees)) {
      return super.equals(obj);
    }
    com.ibc.applications.fee.v1.PacketFees other = (com.ibc.applications.fee.v1.PacketFees) obj;

    if (!getPacketFeesList()
        .equals(other.getPacketFeesList())) return false;
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
    if (getPacketFeesCount() > 0) {
      hash = (37 * hash) + PACKET_FEES_FIELD_NUMBER;
      hash = (53 * hash) + getPacketFeesList().hashCode();
    }
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static com.ibc.applications.fee.v1.PacketFees parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.ibc.applications.fee.v1.PacketFees parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.ibc.applications.fee.v1.PacketFees parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.ibc.applications.fee.v1.PacketFees parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.ibc.applications.fee.v1.PacketFees parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.ibc.applications.fee.v1.PacketFees parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.ibc.applications.fee.v1.PacketFees parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.ibc.applications.fee.v1.PacketFees parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.ibc.applications.fee.v1.PacketFees parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static com.ibc.applications.fee.v1.PacketFees parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.ibc.applications.fee.v1.PacketFees parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.ibc.applications.fee.v1.PacketFees parseFrom(
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
  public static Builder newBuilder(com.ibc.applications.fee.v1.PacketFees prototype) {
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
   * PacketFees contains a list of type PacketFee
   * </pre>
   *
   * Protobuf type {@code ibc.applications.fee.v1.PacketFees}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:ibc.applications.fee.v1.PacketFees)
      com.ibc.applications.fee.v1.PacketFeesOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return com.ibc.applications.fee.v1.FeeProto.internal_static_ibc_applications_fee_v1_PacketFees_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return com.ibc.applications.fee.v1.FeeProto.internal_static_ibc_applications_fee_v1_PacketFees_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              com.ibc.applications.fee.v1.PacketFees.class, com.ibc.applications.fee.v1.PacketFees.Builder.class);
    }

    // Construct using com.ibc.applications.fee.v1.PacketFees.newBuilder()
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
      if (packetFeesBuilder_ == null) {
        packetFees_ = java.util.Collections.emptyList();
      } else {
        packetFees_ = null;
        packetFeesBuilder_.clear();
      }
      bitField0_ = (bitField0_ & ~0x00000001);
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return com.ibc.applications.fee.v1.FeeProto.internal_static_ibc_applications_fee_v1_PacketFees_descriptor;
    }

    @java.lang.Override
    public com.ibc.applications.fee.v1.PacketFees getDefaultInstanceForType() {
      return com.ibc.applications.fee.v1.PacketFees.getDefaultInstance();
    }

    @java.lang.Override
    public com.ibc.applications.fee.v1.PacketFees build() {
      com.ibc.applications.fee.v1.PacketFees result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public com.ibc.applications.fee.v1.PacketFees buildPartial() {
      com.ibc.applications.fee.v1.PacketFees result = new com.ibc.applications.fee.v1.PacketFees(this);
      buildPartialRepeatedFields(result);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartialRepeatedFields(com.ibc.applications.fee.v1.PacketFees result) {
      if (packetFeesBuilder_ == null) {
        if (((bitField0_ & 0x00000001) != 0)) {
          packetFees_ = java.util.Collections.unmodifiableList(packetFees_);
          bitField0_ = (bitField0_ & ~0x00000001);
        }
        result.packetFees_ = packetFees_;
      } else {
        result.packetFees_ = packetFeesBuilder_.build();
      }
    }

    private void buildPartial0(com.ibc.applications.fee.v1.PacketFees result) {
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
      if (other instanceof com.ibc.applications.fee.v1.PacketFees) {
        return mergeFrom((com.ibc.applications.fee.v1.PacketFees)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(com.ibc.applications.fee.v1.PacketFees other) {
      if (other == com.ibc.applications.fee.v1.PacketFees.getDefaultInstance()) return this;
      if (packetFeesBuilder_ == null) {
        if (!other.packetFees_.isEmpty()) {
          if (packetFees_.isEmpty()) {
            packetFees_ = other.packetFees_;
            bitField0_ = (bitField0_ & ~0x00000001);
          } else {
            ensurePacketFeesIsMutable();
            packetFees_.addAll(other.packetFees_);
          }
          onChanged();
        }
      } else {
        if (!other.packetFees_.isEmpty()) {
          if (packetFeesBuilder_.isEmpty()) {
            packetFeesBuilder_.dispose();
            packetFeesBuilder_ = null;
            packetFees_ = other.packetFees_;
            bitField0_ = (bitField0_ & ~0x00000001);
            packetFeesBuilder_ = 
              com.google.protobuf.GeneratedMessageV3.alwaysUseFieldBuilders ?
                 getPacketFeesFieldBuilder() : null;
          } else {
            packetFeesBuilder_.addAllMessages(other.packetFees_);
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
              com.ibc.applications.fee.v1.PacketFee m =
                  input.readMessage(
                      com.ibc.applications.fee.v1.PacketFee.parser(),
                      extensionRegistry);
              if (packetFeesBuilder_ == null) {
                ensurePacketFeesIsMutable();
                packetFees_.add(m);
              } else {
                packetFeesBuilder_.addMessage(m);
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

    private java.util.List<com.ibc.applications.fee.v1.PacketFee> packetFees_ =
      java.util.Collections.emptyList();
    private void ensurePacketFeesIsMutable() {
      if (!((bitField0_ & 0x00000001) != 0)) {
        packetFees_ = new java.util.ArrayList<com.ibc.applications.fee.v1.PacketFee>(packetFees_);
        bitField0_ |= 0x00000001;
       }
    }

    private com.google.protobuf.RepeatedFieldBuilderV3<
        com.ibc.applications.fee.v1.PacketFee, com.ibc.applications.fee.v1.PacketFee.Builder, com.ibc.applications.fee.v1.PacketFeeOrBuilder> packetFeesBuilder_;

    /**
     * <pre>
     * list of packet fees
     * </pre>
     *
     * <code>repeated .ibc.applications.fee.v1.PacketFee packet_fees = 1 [json_name = "packetFees", (.gogoproto.nullable) = false, (.gogoproto.moretags) = "yaml:&#92;"packet_fees&#92;""];</code>
     */
    public java.util.List<com.ibc.applications.fee.v1.PacketFee> getPacketFeesList() {
      if (packetFeesBuilder_ == null) {
        return java.util.Collections.unmodifiableList(packetFees_);
      } else {
        return packetFeesBuilder_.getMessageList();
      }
    }
    /**
     * <pre>
     * list of packet fees
     * </pre>
     *
     * <code>repeated .ibc.applications.fee.v1.PacketFee packet_fees = 1 [json_name = "packetFees", (.gogoproto.nullable) = false, (.gogoproto.moretags) = "yaml:&#92;"packet_fees&#92;""];</code>
     */
    public int getPacketFeesCount() {
      if (packetFeesBuilder_ == null) {
        return packetFees_.size();
      } else {
        return packetFeesBuilder_.getCount();
      }
    }
    /**
     * <pre>
     * list of packet fees
     * </pre>
     *
     * <code>repeated .ibc.applications.fee.v1.PacketFee packet_fees = 1 [json_name = "packetFees", (.gogoproto.nullable) = false, (.gogoproto.moretags) = "yaml:&#92;"packet_fees&#92;""];</code>
     */
    public com.ibc.applications.fee.v1.PacketFee getPacketFees(int index) {
      if (packetFeesBuilder_ == null) {
        return packetFees_.get(index);
      } else {
        return packetFeesBuilder_.getMessage(index);
      }
    }
    /**
     * <pre>
     * list of packet fees
     * </pre>
     *
     * <code>repeated .ibc.applications.fee.v1.PacketFee packet_fees = 1 [json_name = "packetFees", (.gogoproto.nullable) = false, (.gogoproto.moretags) = "yaml:&#92;"packet_fees&#92;""];</code>
     */
    public Builder setPacketFees(
        int index, com.ibc.applications.fee.v1.PacketFee value) {
      if (packetFeesBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensurePacketFeesIsMutable();
        packetFees_.set(index, value);
        onChanged();
      } else {
        packetFeesBuilder_.setMessage(index, value);
      }
      return this;
    }
    /**
     * <pre>
     * list of packet fees
     * </pre>
     *
     * <code>repeated .ibc.applications.fee.v1.PacketFee packet_fees = 1 [json_name = "packetFees", (.gogoproto.nullable) = false, (.gogoproto.moretags) = "yaml:&#92;"packet_fees&#92;""];</code>
     */
    public Builder setPacketFees(
        int index, com.ibc.applications.fee.v1.PacketFee.Builder builderForValue) {
      if (packetFeesBuilder_ == null) {
        ensurePacketFeesIsMutable();
        packetFees_.set(index, builderForValue.build());
        onChanged();
      } else {
        packetFeesBuilder_.setMessage(index, builderForValue.build());
      }
      return this;
    }
    /**
     * <pre>
     * list of packet fees
     * </pre>
     *
     * <code>repeated .ibc.applications.fee.v1.PacketFee packet_fees = 1 [json_name = "packetFees", (.gogoproto.nullable) = false, (.gogoproto.moretags) = "yaml:&#92;"packet_fees&#92;""];</code>
     */
    public Builder addPacketFees(com.ibc.applications.fee.v1.PacketFee value) {
      if (packetFeesBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensurePacketFeesIsMutable();
        packetFees_.add(value);
        onChanged();
      } else {
        packetFeesBuilder_.addMessage(value);
      }
      return this;
    }
    /**
     * <pre>
     * list of packet fees
     * </pre>
     *
     * <code>repeated .ibc.applications.fee.v1.PacketFee packet_fees = 1 [json_name = "packetFees", (.gogoproto.nullable) = false, (.gogoproto.moretags) = "yaml:&#92;"packet_fees&#92;""];</code>
     */
    public Builder addPacketFees(
        int index, com.ibc.applications.fee.v1.PacketFee value) {
      if (packetFeesBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensurePacketFeesIsMutable();
        packetFees_.add(index, value);
        onChanged();
      } else {
        packetFeesBuilder_.addMessage(index, value);
      }
      return this;
    }
    /**
     * <pre>
     * list of packet fees
     * </pre>
     *
     * <code>repeated .ibc.applications.fee.v1.PacketFee packet_fees = 1 [json_name = "packetFees", (.gogoproto.nullable) = false, (.gogoproto.moretags) = "yaml:&#92;"packet_fees&#92;""];</code>
     */
    public Builder addPacketFees(
        com.ibc.applications.fee.v1.PacketFee.Builder builderForValue) {
      if (packetFeesBuilder_ == null) {
        ensurePacketFeesIsMutable();
        packetFees_.add(builderForValue.build());
        onChanged();
      } else {
        packetFeesBuilder_.addMessage(builderForValue.build());
      }
      return this;
    }
    /**
     * <pre>
     * list of packet fees
     * </pre>
     *
     * <code>repeated .ibc.applications.fee.v1.PacketFee packet_fees = 1 [json_name = "packetFees", (.gogoproto.nullable) = false, (.gogoproto.moretags) = "yaml:&#92;"packet_fees&#92;""];</code>
     */
    public Builder addPacketFees(
        int index, com.ibc.applications.fee.v1.PacketFee.Builder builderForValue) {
      if (packetFeesBuilder_ == null) {
        ensurePacketFeesIsMutable();
        packetFees_.add(index, builderForValue.build());
        onChanged();
      } else {
        packetFeesBuilder_.addMessage(index, builderForValue.build());
      }
      return this;
    }
    /**
     * <pre>
     * list of packet fees
     * </pre>
     *
     * <code>repeated .ibc.applications.fee.v1.PacketFee packet_fees = 1 [json_name = "packetFees", (.gogoproto.nullable) = false, (.gogoproto.moretags) = "yaml:&#92;"packet_fees&#92;""];</code>
     */
    public Builder addAllPacketFees(
        java.lang.Iterable<? extends com.ibc.applications.fee.v1.PacketFee> values) {
      if (packetFeesBuilder_ == null) {
        ensurePacketFeesIsMutable();
        com.google.protobuf.AbstractMessageLite.Builder.addAll(
            values, packetFees_);
        onChanged();
      } else {
        packetFeesBuilder_.addAllMessages(values);
      }
      return this;
    }
    /**
     * <pre>
     * list of packet fees
     * </pre>
     *
     * <code>repeated .ibc.applications.fee.v1.PacketFee packet_fees = 1 [json_name = "packetFees", (.gogoproto.nullable) = false, (.gogoproto.moretags) = "yaml:&#92;"packet_fees&#92;""];</code>
     */
    public Builder clearPacketFees() {
      if (packetFeesBuilder_ == null) {
        packetFees_ = java.util.Collections.emptyList();
        bitField0_ = (bitField0_ & ~0x00000001);
        onChanged();
      } else {
        packetFeesBuilder_.clear();
      }
      return this;
    }
    /**
     * <pre>
     * list of packet fees
     * </pre>
     *
     * <code>repeated .ibc.applications.fee.v1.PacketFee packet_fees = 1 [json_name = "packetFees", (.gogoproto.nullable) = false, (.gogoproto.moretags) = "yaml:&#92;"packet_fees&#92;""];</code>
     */
    public Builder removePacketFees(int index) {
      if (packetFeesBuilder_ == null) {
        ensurePacketFeesIsMutable();
        packetFees_.remove(index);
        onChanged();
      } else {
        packetFeesBuilder_.remove(index);
      }
      return this;
    }
    /**
     * <pre>
     * list of packet fees
     * </pre>
     *
     * <code>repeated .ibc.applications.fee.v1.PacketFee packet_fees = 1 [json_name = "packetFees", (.gogoproto.nullable) = false, (.gogoproto.moretags) = "yaml:&#92;"packet_fees&#92;""];</code>
     */
    public com.ibc.applications.fee.v1.PacketFee.Builder getPacketFeesBuilder(
        int index) {
      return getPacketFeesFieldBuilder().getBuilder(index);
    }
    /**
     * <pre>
     * list of packet fees
     * </pre>
     *
     * <code>repeated .ibc.applications.fee.v1.PacketFee packet_fees = 1 [json_name = "packetFees", (.gogoproto.nullable) = false, (.gogoproto.moretags) = "yaml:&#92;"packet_fees&#92;""];</code>
     */
    public com.ibc.applications.fee.v1.PacketFeeOrBuilder getPacketFeesOrBuilder(
        int index) {
      if (packetFeesBuilder_ == null) {
        return packetFees_.get(index);  } else {
        return packetFeesBuilder_.getMessageOrBuilder(index);
      }
    }
    /**
     * <pre>
     * list of packet fees
     * </pre>
     *
     * <code>repeated .ibc.applications.fee.v1.PacketFee packet_fees = 1 [json_name = "packetFees", (.gogoproto.nullable) = false, (.gogoproto.moretags) = "yaml:&#92;"packet_fees&#92;""];</code>
     */
    public java.util.List<? extends com.ibc.applications.fee.v1.PacketFeeOrBuilder> 
         getPacketFeesOrBuilderList() {
      if (packetFeesBuilder_ != null) {
        return packetFeesBuilder_.getMessageOrBuilderList();
      } else {
        return java.util.Collections.unmodifiableList(packetFees_);
      }
    }
    /**
     * <pre>
     * list of packet fees
     * </pre>
     *
     * <code>repeated .ibc.applications.fee.v1.PacketFee packet_fees = 1 [json_name = "packetFees", (.gogoproto.nullable) = false, (.gogoproto.moretags) = "yaml:&#92;"packet_fees&#92;""];</code>
     */
    public com.ibc.applications.fee.v1.PacketFee.Builder addPacketFeesBuilder() {
      return getPacketFeesFieldBuilder().addBuilder(
          com.ibc.applications.fee.v1.PacketFee.getDefaultInstance());
    }
    /**
     * <pre>
     * list of packet fees
     * </pre>
     *
     * <code>repeated .ibc.applications.fee.v1.PacketFee packet_fees = 1 [json_name = "packetFees", (.gogoproto.nullable) = false, (.gogoproto.moretags) = "yaml:&#92;"packet_fees&#92;""];</code>
     */
    public com.ibc.applications.fee.v1.PacketFee.Builder addPacketFeesBuilder(
        int index) {
      return getPacketFeesFieldBuilder().addBuilder(
          index, com.ibc.applications.fee.v1.PacketFee.getDefaultInstance());
    }
    /**
     * <pre>
     * list of packet fees
     * </pre>
     *
     * <code>repeated .ibc.applications.fee.v1.PacketFee packet_fees = 1 [json_name = "packetFees", (.gogoproto.nullable) = false, (.gogoproto.moretags) = "yaml:&#92;"packet_fees&#92;""];</code>
     */
    public java.util.List<com.ibc.applications.fee.v1.PacketFee.Builder> 
         getPacketFeesBuilderList() {
      return getPacketFeesFieldBuilder().getBuilderList();
    }
    private com.google.protobuf.RepeatedFieldBuilderV3<
        com.ibc.applications.fee.v1.PacketFee, com.ibc.applications.fee.v1.PacketFee.Builder, com.ibc.applications.fee.v1.PacketFeeOrBuilder> 
        getPacketFeesFieldBuilder() {
      if (packetFeesBuilder_ == null) {
        packetFeesBuilder_ = new com.google.protobuf.RepeatedFieldBuilderV3<
            com.ibc.applications.fee.v1.PacketFee, com.ibc.applications.fee.v1.PacketFee.Builder, com.ibc.applications.fee.v1.PacketFeeOrBuilder>(
                packetFees_,
                ((bitField0_ & 0x00000001) != 0),
                getParentForChildren(),
                isClean());
        packetFees_ = null;
      }
      return packetFeesBuilder_;
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


    // @@protoc_insertion_point(builder_scope:ibc.applications.fee.v1.PacketFees)
  }

  // @@protoc_insertion_point(class_scope:ibc.applications.fee.v1.PacketFees)
  private static final com.ibc.applications.fee.v1.PacketFees DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new com.ibc.applications.fee.v1.PacketFees();
  }

  public static com.ibc.applications.fee.v1.PacketFees getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<PacketFees>
      PARSER = new com.google.protobuf.AbstractParser<PacketFees>() {
    @java.lang.Override
    public PacketFees parsePartialFrom(
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

  public static com.google.protobuf.Parser<PacketFees> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<PacketFees> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public com.ibc.applications.fee.v1.PacketFees getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}
