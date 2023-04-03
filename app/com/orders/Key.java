// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: modules/orders/internal/key/key.proto

package com.orders;

/**
 * Protobuf type {@code orders.Key}
 */
public final class Key extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:orders.Key)
    KeyOrBuilder {
private static final long serialVersionUID = 0L;
  // Use Key.newBuilder() to construct.
  private Key(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private Key() {
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new Key();
  }

  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return com.orders.KeyProto.internal_static_orders_Key_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return com.orders.KeyProto.internal_static_orders_Key_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            com.orders.Key.class, com.orders.Key.Builder.class);
  }

  public static final int ORDER_I_D_FIELD_NUMBER = 1;
  private com.ids.OrderID orderID_;
  /**
   * <code>.ids.OrderID order_i_d = 1 [json_name = "orderID"];</code>
   * @return Whether the orderID field is set.
   */
  @java.lang.Override
  public boolean hasOrderID() {
    return orderID_ != null;
  }
  /**
   * <code>.ids.OrderID order_i_d = 1 [json_name = "orderID"];</code>
   * @return The orderID.
   */
  @java.lang.Override
  public com.ids.OrderID getOrderID() {
    return orderID_ == null ? com.ids.OrderID.getDefaultInstance() : orderID_;
  }
  /**
   * <code>.ids.OrderID order_i_d = 1 [json_name = "orderID"];</code>
   */
  @java.lang.Override
  public com.ids.OrderIDOrBuilder getOrderIDOrBuilder() {
    return orderID_ == null ? com.ids.OrderID.getDefaultInstance() : orderID_;
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
    if (orderID_ != null) {
      output.writeMessage(1, getOrderID());
    }
    getUnknownFields().writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (orderID_ != null) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(1, getOrderID());
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
    if (!(obj instanceof com.orders.Key)) {
      return super.equals(obj);
    }
    com.orders.Key other = (com.orders.Key) obj;

    if (hasOrderID() != other.hasOrderID()) return false;
    if (hasOrderID()) {
      if (!getOrderID()
          .equals(other.getOrderID())) return false;
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
    if (hasOrderID()) {
      hash = (37 * hash) + ORDER_I_D_FIELD_NUMBER;
      hash = (53 * hash) + getOrderID().hashCode();
    }
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static com.orders.Key parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.orders.Key parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.orders.Key parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.orders.Key parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.orders.Key parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.orders.Key parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.orders.Key parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.orders.Key parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.orders.Key parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static com.orders.Key parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.orders.Key parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.orders.Key parseFrom(
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
  public static Builder newBuilder(com.orders.Key prototype) {
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
   * Protobuf type {@code orders.Key}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:orders.Key)
      com.orders.KeyOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return com.orders.KeyProto.internal_static_orders_Key_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return com.orders.KeyProto.internal_static_orders_Key_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              com.orders.Key.class, com.orders.Key.Builder.class);
    }

    // Construct using com.orders.Key.newBuilder()
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
      orderID_ = null;
      if (orderIDBuilder_ != null) {
        orderIDBuilder_.dispose();
        orderIDBuilder_ = null;
      }
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return com.orders.KeyProto.internal_static_orders_Key_descriptor;
    }

    @java.lang.Override
    public com.orders.Key getDefaultInstanceForType() {
      return com.orders.Key.getDefaultInstance();
    }

    @java.lang.Override
    public com.orders.Key build() {
      com.orders.Key result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public com.orders.Key buildPartial() {
      com.orders.Key result = new com.orders.Key(this);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartial0(com.orders.Key result) {
      int from_bitField0_ = bitField0_;
      if (((from_bitField0_ & 0x00000001) != 0)) {
        result.orderID_ = orderIDBuilder_ == null
            ? orderID_
            : orderIDBuilder_.build();
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
      if (other instanceof com.orders.Key) {
        return mergeFrom((com.orders.Key)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(com.orders.Key other) {
      if (other == com.orders.Key.getDefaultInstance()) return this;
      if (other.hasOrderID()) {
        mergeOrderID(other.getOrderID());
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
                  getOrderIDFieldBuilder().getBuilder(),
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

    private com.ids.OrderID orderID_;
    private com.google.protobuf.SingleFieldBuilderV3<
        com.ids.OrderID, com.ids.OrderID.Builder, com.ids.OrderIDOrBuilder> orderIDBuilder_;
    /**
     * <code>.ids.OrderID order_i_d = 1 [json_name = "orderID"];</code>
     * @return Whether the orderID field is set.
     */
    public boolean hasOrderID() {
      return ((bitField0_ & 0x00000001) != 0);
    }
    /**
     * <code>.ids.OrderID order_i_d = 1 [json_name = "orderID"];</code>
     * @return The orderID.
     */
    public com.ids.OrderID getOrderID() {
      if (orderIDBuilder_ == null) {
        return orderID_ == null ? com.ids.OrderID.getDefaultInstance() : orderID_;
      } else {
        return orderIDBuilder_.getMessage();
      }
    }
    /**
     * <code>.ids.OrderID order_i_d = 1 [json_name = "orderID"];</code>
     */
    public Builder setOrderID(com.ids.OrderID value) {
      if (orderIDBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        orderID_ = value;
      } else {
        orderIDBuilder_.setMessage(value);
      }
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <code>.ids.OrderID order_i_d = 1 [json_name = "orderID"];</code>
     */
    public Builder setOrderID(
        com.ids.OrderID.Builder builderForValue) {
      if (orderIDBuilder_ == null) {
        orderID_ = builderForValue.build();
      } else {
        orderIDBuilder_.setMessage(builderForValue.build());
      }
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <code>.ids.OrderID order_i_d = 1 [json_name = "orderID"];</code>
     */
    public Builder mergeOrderID(com.ids.OrderID value) {
      if (orderIDBuilder_ == null) {
        if (((bitField0_ & 0x00000001) != 0) &&
          orderID_ != null &&
          orderID_ != com.ids.OrderID.getDefaultInstance()) {
          getOrderIDBuilder().mergeFrom(value);
        } else {
          orderID_ = value;
        }
      } else {
        orderIDBuilder_.mergeFrom(value);
      }
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <code>.ids.OrderID order_i_d = 1 [json_name = "orderID"];</code>
     */
    public Builder clearOrderID() {
      bitField0_ = (bitField0_ & ~0x00000001);
      orderID_ = null;
      if (orderIDBuilder_ != null) {
        orderIDBuilder_.dispose();
        orderIDBuilder_ = null;
      }
      onChanged();
      return this;
    }
    /**
     * <code>.ids.OrderID order_i_d = 1 [json_name = "orderID"];</code>
     */
    public com.ids.OrderID.Builder getOrderIDBuilder() {
      bitField0_ |= 0x00000001;
      onChanged();
      return getOrderIDFieldBuilder().getBuilder();
    }
    /**
     * <code>.ids.OrderID order_i_d = 1 [json_name = "orderID"];</code>
     */
    public com.ids.OrderIDOrBuilder getOrderIDOrBuilder() {
      if (orderIDBuilder_ != null) {
        return orderIDBuilder_.getMessageOrBuilder();
      } else {
        return orderID_ == null ?
            com.ids.OrderID.getDefaultInstance() : orderID_;
      }
    }
    /**
     * <code>.ids.OrderID order_i_d = 1 [json_name = "orderID"];</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
        com.ids.OrderID, com.ids.OrderID.Builder, com.ids.OrderIDOrBuilder> 
        getOrderIDFieldBuilder() {
      if (orderIDBuilder_ == null) {
        orderIDBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
            com.ids.OrderID, com.ids.OrderID.Builder, com.ids.OrderIDOrBuilder>(
                getOrderID(),
                getParentForChildren(),
                isClean());
        orderID_ = null;
      }
      return orderIDBuilder_;
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


    // @@protoc_insertion_point(builder_scope:orders.Key)
  }

  // @@protoc_insertion_point(class_scope:orders.Key)
  private static final com.orders.Key DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new com.orders.Key();
  }

  public static com.orders.Key getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<Key>
      PARSER = new com.google.protobuf.AbstractParser<Key>() {
    @java.lang.Override
    public Key parsePartialFrom(
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

  public static com.google.protobuf.Parser<Key> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<Key> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public com.orders.Key getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

