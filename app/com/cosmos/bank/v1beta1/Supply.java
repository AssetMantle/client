// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: cosmos/bank/v1beta1/bank.proto

package com.cosmos.bank.v1beta1;

/**
 * <pre>
 * Supply represents a struct that passively keeps track of the total supply
 * amounts in the network.
 * This message is deprecated now that supply is indexed by denom.
 * </pre>
 *
 * Protobuf type {@code cosmos.bank.v1beta1.Supply}
 */
@java.lang.Deprecated public final class Supply extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:cosmos.bank.v1beta1.Supply)
    SupplyOrBuilder {
private static final long serialVersionUID = 0L;
  // Use Supply.newBuilder() to construct.
  private Supply(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private Supply() {
    total_ = java.util.Collections.emptyList();
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new Supply();
  }

  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return com.cosmos.bank.v1beta1.BankProto.internal_static_cosmos_bank_v1beta1_Supply_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return com.cosmos.bank.v1beta1.BankProto.internal_static_cosmos_bank_v1beta1_Supply_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            com.cosmos.bank.v1beta1.Supply.class, com.cosmos.bank.v1beta1.Supply.Builder.class);
  }

  public static final int TOTAL_FIELD_NUMBER = 1;
  @SuppressWarnings("serial")
  private java.util.List<com.cosmos.base.v1beta1.Coin> total_;
  /**
   * <code>repeated .cosmos.base.v1beta1.Coin total = 1 [json_name = "total", (.gogoproto.nullable) = false, (.gogoproto.castrepeated) = "github.com/cosmos/cosmos-sdk/types.Coins"];</code>
   */
  @java.lang.Override
  public java.util.List<com.cosmos.base.v1beta1.Coin> getTotalList() {
    return total_;
  }
  /**
   * <code>repeated .cosmos.base.v1beta1.Coin total = 1 [json_name = "total", (.gogoproto.nullable) = false, (.gogoproto.castrepeated) = "github.com/cosmos/cosmos-sdk/types.Coins"];</code>
   */
  @java.lang.Override
  public java.util.List<? extends com.cosmos.base.v1beta1.CoinOrBuilder> 
      getTotalOrBuilderList() {
    return total_;
  }
  /**
   * <code>repeated .cosmos.base.v1beta1.Coin total = 1 [json_name = "total", (.gogoproto.nullable) = false, (.gogoproto.castrepeated) = "github.com/cosmos/cosmos-sdk/types.Coins"];</code>
   */
  @java.lang.Override
  public int getTotalCount() {
    return total_.size();
  }
  /**
   * <code>repeated .cosmos.base.v1beta1.Coin total = 1 [json_name = "total", (.gogoproto.nullable) = false, (.gogoproto.castrepeated) = "github.com/cosmos/cosmos-sdk/types.Coins"];</code>
   */
  @java.lang.Override
  public com.cosmos.base.v1beta1.Coin getTotal(int index) {
    return total_.get(index);
  }
  /**
   * <code>repeated .cosmos.base.v1beta1.Coin total = 1 [json_name = "total", (.gogoproto.nullable) = false, (.gogoproto.castrepeated) = "github.com/cosmos/cosmos-sdk/types.Coins"];</code>
   */
  @java.lang.Override
  public com.cosmos.base.v1beta1.CoinOrBuilder getTotalOrBuilder(
      int index) {
    return total_.get(index);
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
    for (int i = 0; i < total_.size(); i++) {
      output.writeMessage(1, total_.get(i));
    }
    getUnknownFields().writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    for (int i = 0; i < total_.size(); i++) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(1, total_.get(i));
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
    if (!(obj instanceof com.cosmos.bank.v1beta1.Supply)) {
      return super.equals(obj);
    }
    com.cosmos.bank.v1beta1.Supply other = (com.cosmos.bank.v1beta1.Supply) obj;

    if (!getTotalList()
        .equals(other.getTotalList())) return false;
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
    if (getTotalCount() > 0) {
      hash = (37 * hash) + TOTAL_FIELD_NUMBER;
      hash = (53 * hash) + getTotalList().hashCode();
    }
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static com.cosmos.bank.v1beta1.Supply parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.cosmos.bank.v1beta1.Supply parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.cosmos.bank.v1beta1.Supply parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.cosmos.bank.v1beta1.Supply parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.cosmos.bank.v1beta1.Supply parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.cosmos.bank.v1beta1.Supply parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.cosmos.bank.v1beta1.Supply parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.cosmos.bank.v1beta1.Supply parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.cosmos.bank.v1beta1.Supply parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static com.cosmos.bank.v1beta1.Supply parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.cosmos.bank.v1beta1.Supply parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.cosmos.bank.v1beta1.Supply parseFrom(
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
  public static Builder newBuilder(com.cosmos.bank.v1beta1.Supply prototype) {
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
   * Supply represents a struct that passively keeps track of the total supply
   * amounts in the network.
   * This message is deprecated now that supply is indexed by denom.
   * </pre>
   *
   * Protobuf type {@code cosmos.bank.v1beta1.Supply}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:cosmos.bank.v1beta1.Supply)
      com.cosmos.bank.v1beta1.SupplyOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return com.cosmos.bank.v1beta1.BankProto.internal_static_cosmos_bank_v1beta1_Supply_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return com.cosmos.bank.v1beta1.BankProto.internal_static_cosmos_bank_v1beta1_Supply_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              com.cosmos.bank.v1beta1.Supply.class, com.cosmos.bank.v1beta1.Supply.Builder.class);
    }

    // Construct using com.cosmos.bank.v1beta1.Supply.newBuilder()
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
      if (totalBuilder_ == null) {
        total_ = java.util.Collections.emptyList();
      } else {
        total_ = null;
        totalBuilder_.clear();
      }
      bitField0_ = (bitField0_ & ~0x00000001);
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return com.cosmos.bank.v1beta1.BankProto.internal_static_cosmos_bank_v1beta1_Supply_descriptor;
    }

    @java.lang.Override
    public com.cosmos.bank.v1beta1.Supply getDefaultInstanceForType() {
      return com.cosmos.bank.v1beta1.Supply.getDefaultInstance();
    }

    @java.lang.Override
    public com.cosmos.bank.v1beta1.Supply build() {
      com.cosmos.bank.v1beta1.Supply result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public com.cosmos.bank.v1beta1.Supply buildPartial() {
      com.cosmos.bank.v1beta1.Supply result = new com.cosmos.bank.v1beta1.Supply(this);
      buildPartialRepeatedFields(result);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartialRepeatedFields(com.cosmos.bank.v1beta1.Supply result) {
      if (totalBuilder_ == null) {
        if (((bitField0_ & 0x00000001) != 0)) {
          total_ = java.util.Collections.unmodifiableList(total_);
          bitField0_ = (bitField0_ & ~0x00000001);
        }
        result.total_ = total_;
      } else {
        result.total_ = totalBuilder_.build();
      }
    }

    private void buildPartial0(com.cosmos.bank.v1beta1.Supply result) {
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
      if (other instanceof com.cosmos.bank.v1beta1.Supply) {
        return mergeFrom((com.cosmos.bank.v1beta1.Supply)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(com.cosmos.bank.v1beta1.Supply other) {
      if (other == com.cosmos.bank.v1beta1.Supply.getDefaultInstance()) return this;
      if (totalBuilder_ == null) {
        if (!other.total_.isEmpty()) {
          if (total_.isEmpty()) {
            total_ = other.total_;
            bitField0_ = (bitField0_ & ~0x00000001);
          } else {
            ensureTotalIsMutable();
            total_.addAll(other.total_);
          }
          onChanged();
        }
      } else {
        if (!other.total_.isEmpty()) {
          if (totalBuilder_.isEmpty()) {
            totalBuilder_.dispose();
            totalBuilder_ = null;
            total_ = other.total_;
            bitField0_ = (bitField0_ & ~0x00000001);
            totalBuilder_ = 
              com.google.protobuf.GeneratedMessageV3.alwaysUseFieldBuilders ?
                 getTotalFieldBuilder() : null;
          } else {
            totalBuilder_.addAllMessages(other.total_);
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
              com.cosmos.base.v1beta1.Coin m =
                  input.readMessage(
                      com.cosmos.base.v1beta1.Coin.parser(),
                      extensionRegistry);
              if (totalBuilder_ == null) {
                ensureTotalIsMutable();
                total_.add(m);
              } else {
                totalBuilder_.addMessage(m);
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

    private java.util.List<com.cosmos.base.v1beta1.Coin> total_ =
      java.util.Collections.emptyList();
    private void ensureTotalIsMutable() {
      if (!((bitField0_ & 0x00000001) != 0)) {
        total_ = new java.util.ArrayList<com.cosmos.base.v1beta1.Coin>(total_);
        bitField0_ |= 0x00000001;
       }
    }

    private com.google.protobuf.RepeatedFieldBuilderV3<
        com.cosmos.base.v1beta1.Coin, com.cosmos.base.v1beta1.Coin.Builder, com.cosmos.base.v1beta1.CoinOrBuilder> totalBuilder_;

    /**
     * <code>repeated .cosmos.base.v1beta1.Coin total = 1 [json_name = "total", (.gogoproto.nullable) = false, (.gogoproto.castrepeated) = "github.com/cosmos/cosmos-sdk/types.Coins"];</code>
     */
    public java.util.List<com.cosmos.base.v1beta1.Coin> getTotalList() {
      if (totalBuilder_ == null) {
        return java.util.Collections.unmodifiableList(total_);
      } else {
        return totalBuilder_.getMessageList();
      }
    }
    /**
     * <code>repeated .cosmos.base.v1beta1.Coin total = 1 [json_name = "total", (.gogoproto.nullable) = false, (.gogoproto.castrepeated) = "github.com/cosmos/cosmos-sdk/types.Coins"];</code>
     */
    public int getTotalCount() {
      if (totalBuilder_ == null) {
        return total_.size();
      } else {
        return totalBuilder_.getCount();
      }
    }
    /**
     * <code>repeated .cosmos.base.v1beta1.Coin total = 1 [json_name = "total", (.gogoproto.nullable) = false, (.gogoproto.castrepeated) = "github.com/cosmos/cosmos-sdk/types.Coins"];</code>
     */
    public com.cosmos.base.v1beta1.Coin getTotal(int index) {
      if (totalBuilder_ == null) {
        return total_.get(index);
      } else {
        return totalBuilder_.getMessage(index);
      }
    }
    /**
     * <code>repeated .cosmos.base.v1beta1.Coin total = 1 [json_name = "total", (.gogoproto.nullable) = false, (.gogoproto.castrepeated) = "github.com/cosmos/cosmos-sdk/types.Coins"];</code>
     */
    public Builder setTotal(
        int index, com.cosmos.base.v1beta1.Coin value) {
      if (totalBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureTotalIsMutable();
        total_.set(index, value);
        onChanged();
      } else {
        totalBuilder_.setMessage(index, value);
      }
      return this;
    }
    /**
     * <code>repeated .cosmos.base.v1beta1.Coin total = 1 [json_name = "total", (.gogoproto.nullable) = false, (.gogoproto.castrepeated) = "github.com/cosmos/cosmos-sdk/types.Coins"];</code>
     */
    public Builder setTotal(
        int index, com.cosmos.base.v1beta1.Coin.Builder builderForValue) {
      if (totalBuilder_ == null) {
        ensureTotalIsMutable();
        total_.set(index, builderForValue.build());
        onChanged();
      } else {
        totalBuilder_.setMessage(index, builderForValue.build());
      }
      return this;
    }
    /**
     * <code>repeated .cosmos.base.v1beta1.Coin total = 1 [json_name = "total", (.gogoproto.nullable) = false, (.gogoproto.castrepeated) = "github.com/cosmos/cosmos-sdk/types.Coins"];</code>
     */
    public Builder addTotal(com.cosmos.base.v1beta1.Coin value) {
      if (totalBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureTotalIsMutable();
        total_.add(value);
        onChanged();
      } else {
        totalBuilder_.addMessage(value);
      }
      return this;
    }
    /**
     * <code>repeated .cosmos.base.v1beta1.Coin total = 1 [json_name = "total", (.gogoproto.nullable) = false, (.gogoproto.castrepeated) = "github.com/cosmos/cosmos-sdk/types.Coins"];</code>
     */
    public Builder addTotal(
        int index, com.cosmos.base.v1beta1.Coin value) {
      if (totalBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureTotalIsMutable();
        total_.add(index, value);
        onChanged();
      } else {
        totalBuilder_.addMessage(index, value);
      }
      return this;
    }
    /**
     * <code>repeated .cosmos.base.v1beta1.Coin total = 1 [json_name = "total", (.gogoproto.nullable) = false, (.gogoproto.castrepeated) = "github.com/cosmos/cosmos-sdk/types.Coins"];</code>
     */
    public Builder addTotal(
        com.cosmos.base.v1beta1.Coin.Builder builderForValue) {
      if (totalBuilder_ == null) {
        ensureTotalIsMutable();
        total_.add(builderForValue.build());
        onChanged();
      } else {
        totalBuilder_.addMessage(builderForValue.build());
      }
      return this;
    }
    /**
     * <code>repeated .cosmos.base.v1beta1.Coin total = 1 [json_name = "total", (.gogoproto.nullable) = false, (.gogoproto.castrepeated) = "github.com/cosmos/cosmos-sdk/types.Coins"];</code>
     */
    public Builder addTotal(
        int index, com.cosmos.base.v1beta1.Coin.Builder builderForValue) {
      if (totalBuilder_ == null) {
        ensureTotalIsMutable();
        total_.add(index, builderForValue.build());
        onChanged();
      } else {
        totalBuilder_.addMessage(index, builderForValue.build());
      }
      return this;
    }
    /**
     * <code>repeated .cosmos.base.v1beta1.Coin total = 1 [json_name = "total", (.gogoproto.nullable) = false, (.gogoproto.castrepeated) = "github.com/cosmos/cosmos-sdk/types.Coins"];</code>
     */
    public Builder addAllTotal(
        java.lang.Iterable<? extends com.cosmos.base.v1beta1.Coin> values) {
      if (totalBuilder_ == null) {
        ensureTotalIsMutable();
        com.google.protobuf.AbstractMessageLite.Builder.addAll(
            values, total_);
        onChanged();
      } else {
        totalBuilder_.addAllMessages(values);
      }
      return this;
    }
    /**
     * <code>repeated .cosmos.base.v1beta1.Coin total = 1 [json_name = "total", (.gogoproto.nullable) = false, (.gogoproto.castrepeated) = "github.com/cosmos/cosmos-sdk/types.Coins"];</code>
     */
    public Builder clearTotal() {
      if (totalBuilder_ == null) {
        total_ = java.util.Collections.emptyList();
        bitField0_ = (bitField0_ & ~0x00000001);
        onChanged();
      } else {
        totalBuilder_.clear();
      }
      return this;
    }
    /**
     * <code>repeated .cosmos.base.v1beta1.Coin total = 1 [json_name = "total", (.gogoproto.nullable) = false, (.gogoproto.castrepeated) = "github.com/cosmos/cosmos-sdk/types.Coins"];</code>
     */
    public Builder removeTotal(int index) {
      if (totalBuilder_ == null) {
        ensureTotalIsMutable();
        total_.remove(index);
        onChanged();
      } else {
        totalBuilder_.remove(index);
      }
      return this;
    }
    /**
     * <code>repeated .cosmos.base.v1beta1.Coin total = 1 [json_name = "total", (.gogoproto.nullable) = false, (.gogoproto.castrepeated) = "github.com/cosmos/cosmos-sdk/types.Coins"];</code>
     */
    public com.cosmos.base.v1beta1.Coin.Builder getTotalBuilder(
        int index) {
      return getTotalFieldBuilder().getBuilder(index);
    }
    /**
     * <code>repeated .cosmos.base.v1beta1.Coin total = 1 [json_name = "total", (.gogoproto.nullable) = false, (.gogoproto.castrepeated) = "github.com/cosmos/cosmos-sdk/types.Coins"];</code>
     */
    public com.cosmos.base.v1beta1.CoinOrBuilder getTotalOrBuilder(
        int index) {
      if (totalBuilder_ == null) {
        return total_.get(index);  } else {
        return totalBuilder_.getMessageOrBuilder(index);
      }
    }
    /**
     * <code>repeated .cosmos.base.v1beta1.Coin total = 1 [json_name = "total", (.gogoproto.nullable) = false, (.gogoproto.castrepeated) = "github.com/cosmos/cosmos-sdk/types.Coins"];</code>
     */
    public java.util.List<? extends com.cosmos.base.v1beta1.CoinOrBuilder> 
         getTotalOrBuilderList() {
      if (totalBuilder_ != null) {
        return totalBuilder_.getMessageOrBuilderList();
      } else {
        return java.util.Collections.unmodifiableList(total_);
      }
    }
    /**
     * <code>repeated .cosmos.base.v1beta1.Coin total = 1 [json_name = "total", (.gogoproto.nullable) = false, (.gogoproto.castrepeated) = "github.com/cosmos/cosmos-sdk/types.Coins"];</code>
     */
    public com.cosmos.base.v1beta1.Coin.Builder addTotalBuilder() {
      return getTotalFieldBuilder().addBuilder(
          com.cosmos.base.v1beta1.Coin.getDefaultInstance());
    }
    /**
     * <code>repeated .cosmos.base.v1beta1.Coin total = 1 [json_name = "total", (.gogoproto.nullable) = false, (.gogoproto.castrepeated) = "github.com/cosmos/cosmos-sdk/types.Coins"];</code>
     */
    public com.cosmos.base.v1beta1.Coin.Builder addTotalBuilder(
        int index) {
      return getTotalFieldBuilder().addBuilder(
          index, com.cosmos.base.v1beta1.Coin.getDefaultInstance());
    }
    /**
     * <code>repeated .cosmos.base.v1beta1.Coin total = 1 [json_name = "total", (.gogoproto.nullable) = false, (.gogoproto.castrepeated) = "github.com/cosmos/cosmos-sdk/types.Coins"];</code>
     */
    public java.util.List<com.cosmos.base.v1beta1.Coin.Builder> 
         getTotalBuilderList() {
      return getTotalFieldBuilder().getBuilderList();
    }
    private com.google.protobuf.RepeatedFieldBuilderV3<
        com.cosmos.base.v1beta1.Coin, com.cosmos.base.v1beta1.Coin.Builder, com.cosmos.base.v1beta1.CoinOrBuilder> 
        getTotalFieldBuilder() {
      if (totalBuilder_ == null) {
        totalBuilder_ = new com.google.protobuf.RepeatedFieldBuilderV3<
            com.cosmos.base.v1beta1.Coin, com.cosmos.base.v1beta1.Coin.Builder, com.cosmos.base.v1beta1.CoinOrBuilder>(
                total_,
                ((bitField0_ & 0x00000001) != 0),
                getParentForChildren(),
                isClean());
        total_ = null;
      }
      return totalBuilder_;
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


    // @@protoc_insertion_point(builder_scope:cosmos.bank.v1beta1.Supply)
  }

  // @@protoc_insertion_point(class_scope:cosmos.bank.v1beta1.Supply)
  private static final com.cosmos.bank.v1beta1.Supply DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new com.cosmos.bank.v1beta1.Supply();
  }

  public static com.cosmos.bank.v1beta1.Supply getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<Supply>
      PARSER = new com.google.protobuf.AbstractParser<Supply>() {
    @java.lang.Override
    public Supply parsePartialFrom(
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

  public static com.google.protobuf.Parser<Supply> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<Supply> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public com.cosmos.bank.v1beta1.Supply getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

