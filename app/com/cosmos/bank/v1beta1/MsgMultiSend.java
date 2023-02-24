// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: cosmos/bank/v1beta1/tx.proto

package com.cosmos.bank.v1beta1;

/**
 * <pre>
 * MsgMultiSend represents an arbitrary multi-in, multi-out send message.
 * </pre>
 *
 * Protobuf type {@code cosmos.bank.v1beta1.MsgMultiSend}
 */
public final class MsgMultiSend extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:cosmos.bank.v1beta1.MsgMultiSend)
    MsgMultiSendOrBuilder {
private static final long serialVersionUID = 0L;
  // Use MsgMultiSend.newBuilder() to construct.
  private MsgMultiSend(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private MsgMultiSend() {
    inputs_ = java.util.Collections.emptyList();
    outputs_ = java.util.Collections.emptyList();
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new MsgMultiSend();
  }

  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return com.cosmos.bank.v1beta1.TxProto.internal_static_cosmos_bank_v1beta1_MsgMultiSend_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return com.cosmos.bank.v1beta1.TxProto.internal_static_cosmos_bank_v1beta1_MsgMultiSend_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            com.cosmos.bank.v1beta1.MsgMultiSend.class, com.cosmos.bank.v1beta1.MsgMultiSend.Builder.class);
  }

  public static final int INPUTS_FIELD_NUMBER = 1;
  @SuppressWarnings("serial")
  private java.util.List<com.cosmos.bank.v1beta1.Input> inputs_;
  /**
   * <code>repeated .cosmos.bank.v1beta1.Input inputs = 1 [json_name = "inputs", (.gogoproto.nullable) = false];</code>
   */
  @java.lang.Override
  public java.util.List<com.cosmos.bank.v1beta1.Input> getInputsList() {
    return inputs_;
  }
  /**
   * <code>repeated .cosmos.bank.v1beta1.Input inputs = 1 [json_name = "inputs", (.gogoproto.nullable) = false];</code>
   */
  @java.lang.Override
  public java.util.List<? extends com.cosmos.bank.v1beta1.InputOrBuilder> 
      getInputsOrBuilderList() {
    return inputs_;
  }
  /**
   * <code>repeated .cosmos.bank.v1beta1.Input inputs = 1 [json_name = "inputs", (.gogoproto.nullable) = false];</code>
   */
  @java.lang.Override
  public int getInputsCount() {
    return inputs_.size();
  }
  /**
   * <code>repeated .cosmos.bank.v1beta1.Input inputs = 1 [json_name = "inputs", (.gogoproto.nullable) = false];</code>
   */
  @java.lang.Override
  public com.cosmos.bank.v1beta1.Input getInputs(int index) {
    return inputs_.get(index);
  }
  /**
   * <code>repeated .cosmos.bank.v1beta1.Input inputs = 1 [json_name = "inputs", (.gogoproto.nullable) = false];</code>
   */
  @java.lang.Override
  public com.cosmos.bank.v1beta1.InputOrBuilder getInputsOrBuilder(
      int index) {
    return inputs_.get(index);
  }

  public static final int OUTPUTS_FIELD_NUMBER = 2;
  @SuppressWarnings("serial")
  private java.util.List<com.cosmos.bank.v1beta1.Output> outputs_;
  /**
   * <code>repeated .cosmos.bank.v1beta1.Output outputs = 2 [json_name = "outputs", (.gogoproto.nullable) = false];</code>
   */
  @java.lang.Override
  public java.util.List<com.cosmos.bank.v1beta1.Output> getOutputsList() {
    return outputs_;
  }
  /**
   * <code>repeated .cosmos.bank.v1beta1.Output outputs = 2 [json_name = "outputs", (.gogoproto.nullable) = false];</code>
   */
  @java.lang.Override
  public java.util.List<? extends com.cosmos.bank.v1beta1.OutputOrBuilder> 
      getOutputsOrBuilderList() {
    return outputs_;
  }
  /**
   * <code>repeated .cosmos.bank.v1beta1.Output outputs = 2 [json_name = "outputs", (.gogoproto.nullable) = false];</code>
   */
  @java.lang.Override
  public int getOutputsCount() {
    return outputs_.size();
  }
  /**
   * <code>repeated .cosmos.bank.v1beta1.Output outputs = 2 [json_name = "outputs", (.gogoproto.nullable) = false];</code>
   */
  @java.lang.Override
  public com.cosmos.bank.v1beta1.Output getOutputs(int index) {
    return outputs_.get(index);
  }
  /**
   * <code>repeated .cosmos.bank.v1beta1.Output outputs = 2 [json_name = "outputs", (.gogoproto.nullable) = false];</code>
   */
  @java.lang.Override
  public com.cosmos.bank.v1beta1.OutputOrBuilder getOutputsOrBuilder(
      int index) {
    return outputs_.get(index);
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
    for (int i = 0; i < inputs_.size(); i++) {
      output.writeMessage(1, inputs_.get(i));
    }
    for (int i = 0; i < outputs_.size(); i++) {
      output.writeMessage(2, outputs_.get(i));
    }
    getUnknownFields().writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    for (int i = 0; i < inputs_.size(); i++) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(1, inputs_.get(i));
    }
    for (int i = 0; i < outputs_.size(); i++) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(2, outputs_.get(i));
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
    if (!(obj instanceof com.cosmos.bank.v1beta1.MsgMultiSend)) {
      return super.equals(obj);
    }
    com.cosmos.bank.v1beta1.MsgMultiSend other = (com.cosmos.bank.v1beta1.MsgMultiSend) obj;

    if (!getInputsList()
        .equals(other.getInputsList())) return false;
    if (!getOutputsList()
        .equals(other.getOutputsList())) return false;
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
    if (getInputsCount() > 0) {
      hash = (37 * hash) + INPUTS_FIELD_NUMBER;
      hash = (53 * hash) + getInputsList().hashCode();
    }
    if (getOutputsCount() > 0) {
      hash = (37 * hash) + OUTPUTS_FIELD_NUMBER;
      hash = (53 * hash) + getOutputsList().hashCode();
    }
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static com.cosmos.bank.v1beta1.MsgMultiSend parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.cosmos.bank.v1beta1.MsgMultiSend parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.cosmos.bank.v1beta1.MsgMultiSend parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.cosmos.bank.v1beta1.MsgMultiSend parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.cosmos.bank.v1beta1.MsgMultiSend parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.cosmos.bank.v1beta1.MsgMultiSend parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.cosmos.bank.v1beta1.MsgMultiSend parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.cosmos.bank.v1beta1.MsgMultiSend parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.cosmos.bank.v1beta1.MsgMultiSend parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static com.cosmos.bank.v1beta1.MsgMultiSend parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.cosmos.bank.v1beta1.MsgMultiSend parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.cosmos.bank.v1beta1.MsgMultiSend parseFrom(
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
  public static Builder newBuilder(com.cosmos.bank.v1beta1.MsgMultiSend prototype) {
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
   * MsgMultiSend represents an arbitrary multi-in, multi-out send message.
   * </pre>
   *
   * Protobuf type {@code cosmos.bank.v1beta1.MsgMultiSend}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:cosmos.bank.v1beta1.MsgMultiSend)
      com.cosmos.bank.v1beta1.MsgMultiSendOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return com.cosmos.bank.v1beta1.TxProto.internal_static_cosmos_bank_v1beta1_MsgMultiSend_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return com.cosmos.bank.v1beta1.TxProto.internal_static_cosmos_bank_v1beta1_MsgMultiSend_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              com.cosmos.bank.v1beta1.MsgMultiSend.class, com.cosmos.bank.v1beta1.MsgMultiSend.Builder.class);
    }

    // Construct using com.cosmos.bank.v1beta1.MsgMultiSend.newBuilder()
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
      if (inputsBuilder_ == null) {
        inputs_ = java.util.Collections.emptyList();
      } else {
        inputs_ = null;
        inputsBuilder_.clear();
      }
      bitField0_ = (bitField0_ & ~0x00000001);
      if (outputsBuilder_ == null) {
        outputs_ = java.util.Collections.emptyList();
      } else {
        outputs_ = null;
        outputsBuilder_.clear();
      }
      bitField0_ = (bitField0_ & ~0x00000002);
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return com.cosmos.bank.v1beta1.TxProto.internal_static_cosmos_bank_v1beta1_MsgMultiSend_descriptor;
    }

    @java.lang.Override
    public com.cosmos.bank.v1beta1.MsgMultiSend getDefaultInstanceForType() {
      return com.cosmos.bank.v1beta1.MsgMultiSend.getDefaultInstance();
    }

    @java.lang.Override
    public com.cosmos.bank.v1beta1.MsgMultiSend build() {
      com.cosmos.bank.v1beta1.MsgMultiSend result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public com.cosmos.bank.v1beta1.MsgMultiSend buildPartial() {
      com.cosmos.bank.v1beta1.MsgMultiSend result = new com.cosmos.bank.v1beta1.MsgMultiSend(this);
      buildPartialRepeatedFields(result);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartialRepeatedFields(com.cosmos.bank.v1beta1.MsgMultiSend result) {
      if (inputsBuilder_ == null) {
        if (((bitField0_ & 0x00000001) != 0)) {
          inputs_ = java.util.Collections.unmodifiableList(inputs_);
          bitField0_ = (bitField0_ & ~0x00000001);
        }
        result.inputs_ = inputs_;
      } else {
        result.inputs_ = inputsBuilder_.build();
      }
      if (outputsBuilder_ == null) {
        if (((bitField0_ & 0x00000002) != 0)) {
          outputs_ = java.util.Collections.unmodifiableList(outputs_);
          bitField0_ = (bitField0_ & ~0x00000002);
        }
        result.outputs_ = outputs_;
      } else {
        result.outputs_ = outputsBuilder_.build();
      }
    }

    private void buildPartial0(com.cosmos.bank.v1beta1.MsgMultiSend result) {
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
      if (other instanceof com.cosmos.bank.v1beta1.MsgMultiSend) {
        return mergeFrom((com.cosmos.bank.v1beta1.MsgMultiSend)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(com.cosmos.bank.v1beta1.MsgMultiSend other) {
      if (other == com.cosmos.bank.v1beta1.MsgMultiSend.getDefaultInstance()) return this;
      if (inputsBuilder_ == null) {
        if (!other.inputs_.isEmpty()) {
          if (inputs_.isEmpty()) {
            inputs_ = other.inputs_;
            bitField0_ = (bitField0_ & ~0x00000001);
          } else {
            ensureInputsIsMutable();
            inputs_.addAll(other.inputs_);
          }
          onChanged();
        }
      } else {
        if (!other.inputs_.isEmpty()) {
          if (inputsBuilder_.isEmpty()) {
            inputsBuilder_.dispose();
            inputsBuilder_ = null;
            inputs_ = other.inputs_;
            bitField0_ = (bitField0_ & ~0x00000001);
            inputsBuilder_ = 
              com.google.protobuf.GeneratedMessageV3.alwaysUseFieldBuilders ?
                 getInputsFieldBuilder() : null;
          } else {
            inputsBuilder_.addAllMessages(other.inputs_);
          }
        }
      }
      if (outputsBuilder_ == null) {
        if (!other.outputs_.isEmpty()) {
          if (outputs_.isEmpty()) {
            outputs_ = other.outputs_;
            bitField0_ = (bitField0_ & ~0x00000002);
          } else {
            ensureOutputsIsMutable();
            outputs_.addAll(other.outputs_);
          }
          onChanged();
        }
      } else {
        if (!other.outputs_.isEmpty()) {
          if (outputsBuilder_.isEmpty()) {
            outputsBuilder_.dispose();
            outputsBuilder_ = null;
            outputs_ = other.outputs_;
            bitField0_ = (bitField0_ & ~0x00000002);
            outputsBuilder_ = 
              com.google.protobuf.GeneratedMessageV3.alwaysUseFieldBuilders ?
                 getOutputsFieldBuilder() : null;
          } else {
            outputsBuilder_.addAllMessages(other.outputs_);
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
              com.cosmos.bank.v1beta1.Input m =
                  input.readMessage(
                      com.cosmos.bank.v1beta1.Input.parser(),
                      extensionRegistry);
              if (inputsBuilder_ == null) {
                ensureInputsIsMutable();
                inputs_.add(m);
              } else {
                inputsBuilder_.addMessage(m);
              }
              break;
            } // case 10
            case 18: {
              com.cosmos.bank.v1beta1.Output m =
                  input.readMessage(
                      com.cosmos.bank.v1beta1.Output.parser(),
                      extensionRegistry);
              if (outputsBuilder_ == null) {
                ensureOutputsIsMutable();
                outputs_.add(m);
              } else {
                outputsBuilder_.addMessage(m);
              }
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

    private java.util.List<com.cosmos.bank.v1beta1.Input> inputs_ =
      java.util.Collections.emptyList();
    private void ensureInputsIsMutable() {
      if (!((bitField0_ & 0x00000001) != 0)) {
        inputs_ = new java.util.ArrayList<com.cosmos.bank.v1beta1.Input>(inputs_);
        bitField0_ |= 0x00000001;
       }
    }

    private com.google.protobuf.RepeatedFieldBuilderV3<
        com.cosmos.bank.v1beta1.Input, com.cosmos.bank.v1beta1.Input.Builder, com.cosmos.bank.v1beta1.InputOrBuilder> inputsBuilder_;

    /**
     * <code>repeated .cosmos.bank.v1beta1.Input inputs = 1 [json_name = "inputs", (.gogoproto.nullable) = false];</code>
     */
    public java.util.List<com.cosmos.bank.v1beta1.Input> getInputsList() {
      if (inputsBuilder_ == null) {
        return java.util.Collections.unmodifiableList(inputs_);
      } else {
        return inputsBuilder_.getMessageList();
      }
    }
    /**
     * <code>repeated .cosmos.bank.v1beta1.Input inputs = 1 [json_name = "inputs", (.gogoproto.nullable) = false];</code>
     */
    public int getInputsCount() {
      if (inputsBuilder_ == null) {
        return inputs_.size();
      } else {
        return inputsBuilder_.getCount();
      }
    }
    /**
     * <code>repeated .cosmos.bank.v1beta1.Input inputs = 1 [json_name = "inputs", (.gogoproto.nullable) = false];</code>
     */
    public com.cosmos.bank.v1beta1.Input getInputs(int index) {
      if (inputsBuilder_ == null) {
        return inputs_.get(index);
      } else {
        return inputsBuilder_.getMessage(index);
      }
    }
    /**
     * <code>repeated .cosmos.bank.v1beta1.Input inputs = 1 [json_name = "inputs", (.gogoproto.nullable) = false];</code>
     */
    public Builder setInputs(
        int index, com.cosmos.bank.v1beta1.Input value) {
      if (inputsBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureInputsIsMutable();
        inputs_.set(index, value);
        onChanged();
      } else {
        inputsBuilder_.setMessage(index, value);
      }
      return this;
    }
    /**
     * <code>repeated .cosmos.bank.v1beta1.Input inputs = 1 [json_name = "inputs", (.gogoproto.nullable) = false];</code>
     */
    public Builder setInputs(
        int index, com.cosmos.bank.v1beta1.Input.Builder builderForValue) {
      if (inputsBuilder_ == null) {
        ensureInputsIsMutable();
        inputs_.set(index, builderForValue.build());
        onChanged();
      } else {
        inputsBuilder_.setMessage(index, builderForValue.build());
      }
      return this;
    }
    /**
     * <code>repeated .cosmos.bank.v1beta1.Input inputs = 1 [json_name = "inputs", (.gogoproto.nullable) = false];</code>
     */
    public Builder addInputs(com.cosmos.bank.v1beta1.Input value) {
      if (inputsBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureInputsIsMutable();
        inputs_.add(value);
        onChanged();
      } else {
        inputsBuilder_.addMessage(value);
      }
      return this;
    }
    /**
     * <code>repeated .cosmos.bank.v1beta1.Input inputs = 1 [json_name = "inputs", (.gogoproto.nullable) = false];</code>
     */
    public Builder addInputs(
        int index, com.cosmos.bank.v1beta1.Input value) {
      if (inputsBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureInputsIsMutable();
        inputs_.add(index, value);
        onChanged();
      } else {
        inputsBuilder_.addMessage(index, value);
      }
      return this;
    }
    /**
     * <code>repeated .cosmos.bank.v1beta1.Input inputs = 1 [json_name = "inputs", (.gogoproto.nullable) = false];</code>
     */
    public Builder addInputs(
        com.cosmos.bank.v1beta1.Input.Builder builderForValue) {
      if (inputsBuilder_ == null) {
        ensureInputsIsMutable();
        inputs_.add(builderForValue.build());
        onChanged();
      } else {
        inputsBuilder_.addMessage(builderForValue.build());
      }
      return this;
    }
    /**
     * <code>repeated .cosmos.bank.v1beta1.Input inputs = 1 [json_name = "inputs", (.gogoproto.nullable) = false];</code>
     */
    public Builder addInputs(
        int index, com.cosmos.bank.v1beta1.Input.Builder builderForValue) {
      if (inputsBuilder_ == null) {
        ensureInputsIsMutable();
        inputs_.add(index, builderForValue.build());
        onChanged();
      } else {
        inputsBuilder_.addMessage(index, builderForValue.build());
      }
      return this;
    }
    /**
     * <code>repeated .cosmos.bank.v1beta1.Input inputs = 1 [json_name = "inputs", (.gogoproto.nullable) = false];</code>
     */
    public Builder addAllInputs(
        java.lang.Iterable<? extends com.cosmos.bank.v1beta1.Input> values) {
      if (inputsBuilder_ == null) {
        ensureInputsIsMutable();
        com.google.protobuf.AbstractMessageLite.Builder.addAll(
            values, inputs_);
        onChanged();
      } else {
        inputsBuilder_.addAllMessages(values);
      }
      return this;
    }
    /**
     * <code>repeated .cosmos.bank.v1beta1.Input inputs = 1 [json_name = "inputs", (.gogoproto.nullable) = false];</code>
     */
    public Builder clearInputs() {
      if (inputsBuilder_ == null) {
        inputs_ = java.util.Collections.emptyList();
        bitField0_ = (bitField0_ & ~0x00000001);
        onChanged();
      } else {
        inputsBuilder_.clear();
      }
      return this;
    }
    /**
     * <code>repeated .cosmos.bank.v1beta1.Input inputs = 1 [json_name = "inputs", (.gogoproto.nullable) = false];</code>
     */
    public Builder removeInputs(int index) {
      if (inputsBuilder_ == null) {
        ensureInputsIsMutable();
        inputs_.remove(index);
        onChanged();
      } else {
        inputsBuilder_.remove(index);
      }
      return this;
    }
    /**
     * <code>repeated .cosmos.bank.v1beta1.Input inputs = 1 [json_name = "inputs", (.gogoproto.nullable) = false];</code>
     */
    public com.cosmos.bank.v1beta1.Input.Builder getInputsBuilder(
        int index) {
      return getInputsFieldBuilder().getBuilder(index);
    }
    /**
     * <code>repeated .cosmos.bank.v1beta1.Input inputs = 1 [json_name = "inputs", (.gogoproto.nullable) = false];</code>
     */
    public com.cosmos.bank.v1beta1.InputOrBuilder getInputsOrBuilder(
        int index) {
      if (inputsBuilder_ == null) {
        return inputs_.get(index);  } else {
        return inputsBuilder_.getMessageOrBuilder(index);
      }
    }
    /**
     * <code>repeated .cosmos.bank.v1beta1.Input inputs = 1 [json_name = "inputs", (.gogoproto.nullable) = false];</code>
     */
    public java.util.List<? extends com.cosmos.bank.v1beta1.InputOrBuilder> 
         getInputsOrBuilderList() {
      if (inputsBuilder_ != null) {
        return inputsBuilder_.getMessageOrBuilderList();
      } else {
        return java.util.Collections.unmodifiableList(inputs_);
      }
    }
    /**
     * <code>repeated .cosmos.bank.v1beta1.Input inputs = 1 [json_name = "inputs", (.gogoproto.nullable) = false];</code>
     */
    public com.cosmos.bank.v1beta1.Input.Builder addInputsBuilder() {
      return getInputsFieldBuilder().addBuilder(
          com.cosmos.bank.v1beta1.Input.getDefaultInstance());
    }
    /**
     * <code>repeated .cosmos.bank.v1beta1.Input inputs = 1 [json_name = "inputs", (.gogoproto.nullable) = false];</code>
     */
    public com.cosmos.bank.v1beta1.Input.Builder addInputsBuilder(
        int index) {
      return getInputsFieldBuilder().addBuilder(
          index, com.cosmos.bank.v1beta1.Input.getDefaultInstance());
    }
    /**
     * <code>repeated .cosmos.bank.v1beta1.Input inputs = 1 [json_name = "inputs", (.gogoproto.nullable) = false];</code>
     */
    public java.util.List<com.cosmos.bank.v1beta1.Input.Builder> 
         getInputsBuilderList() {
      return getInputsFieldBuilder().getBuilderList();
    }
    private com.google.protobuf.RepeatedFieldBuilderV3<
        com.cosmos.bank.v1beta1.Input, com.cosmos.bank.v1beta1.Input.Builder, com.cosmos.bank.v1beta1.InputOrBuilder> 
        getInputsFieldBuilder() {
      if (inputsBuilder_ == null) {
        inputsBuilder_ = new com.google.protobuf.RepeatedFieldBuilderV3<
            com.cosmos.bank.v1beta1.Input, com.cosmos.bank.v1beta1.Input.Builder, com.cosmos.bank.v1beta1.InputOrBuilder>(
                inputs_,
                ((bitField0_ & 0x00000001) != 0),
                getParentForChildren(),
                isClean());
        inputs_ = null;
      }
      return inputsBuilder_;
    }

    private java.util.List<com.cosmos.bank.v1beta1.Output> outputs_ =
      java.util.Collections.emptyList();
    private void ensureOutputsIsMutable() {
      if (!((bitField0_ & 0x00000002) != 0)) {
        outputs_ = new java.util.ArrayList<com.cosmos.bank.v1beta1.Output>(outputs_);
        bitField0_ |= 0x00000002;
       }
    }

    private com.google.protobuf.RepeatedFieldBuilderV3<
        com.cosmos.bank.v1beta1.Output, com.cosmos.bank.v1beta1.Output.Builder, com.cosmos.bank.v1beta1.OutputOrBuilder> outputsBuilder_;

    /**
     * <code>repeated .cosmos.bank.v1beta1.Output outputs = 2 [json_name = "outputs", (.gogoproto.nullable) = false];</code>
     */
    public java.util.List<com.cosmos.bank.v1beta1.Output> getOutputsList() {
      if (outputsBuilder_ == null) {
        return java.util.Collections.unmodifiableList(outputs_);
      } else {
        return outputsBuilder_.getMessageList();
      }
    }
    /**
     * <code>repeated .cosmos.bank.v1beta1.Output outputs = 2 [json_name = "outputs", (.gogoproto.nullable) = false];</code>
     */
    public int getOutputsCount() {
      if (outputsBuilder_ == null) {
        return outputs_.size();
      } else {
        return outputsBuilder_.getCount();
      }
    }
    /**
     * <code>repeated .cosmos.bank.v1beta1.Output outputs = 2 [json_name = "outputs", (.gogoproto.nullable) = false];</code>
     */
    public com.cosmos.bank.v1beta1.Output getOutputs(int index) {
      if (outputsBuilder_ == null) {
        return outputs_.get(index);
      } else {
        return outputsBuilder_.getMessage(index);
      }
    }
    /**
     * <code>repeated .cosmos.bank.v1beta1.Output outputs = 2 [json_name = "outputs", (.gogoproto.nullable) = false];</code>
     */
    public Builder setOutputs(
        int index, com.cosmos.bank.v1beta1.Output value) {
      if (outputsBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureOutputsIsMutable();
        outputs_.set(index, value);
        onChanged();
      } else {
        outputsBuilder_.setMessage(index, value);
      }
      return this;
    }
    /**
     * <code>repeated .cosmos.bank.v1beta1.Output outputs = 2 [json_name = "outputs", (.gogoproto.nullable) = false];</code>
     */
    public Builder setOutputs(
        int index, com.cosmos.bank.v1beta1.Output.Builder builderForValue) {
      if (outputsBuilder_ == null) {
        ensureOutputsIsMutable();
        outputs_.set(index, builderForValue.build());
        onChanged();
      } else {
        outputsBuilder_.setMessage(index, builderForValue.build());
      }
      return this;
    }
    /**
     * <code>repeated .cosmos.bank.v1beta1.Output outputs = 2 [json_name = "outputs", (.gogoproto.nullable) = false];</code>
     */
    public Builder addOutputs(com.cosmos.bank.v1beta1.Output value) {
      if (outputsBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureOutputsIsMutable();
        outputs_.add(value);
        onChanged();
      } else {
        outputsBuilder_.addMessage(value);
      }
      return this;
    }
    /**
     * <code>repeated .cosmos.bank.v1beta1.Output outputs = 2 [json_name = "outputs", (.gogoproto.nullable) = false];</code>
     */
    public Builder addOutputs(
        int index, com.cosmos.bank.v1beta1.Output value) {
      if (outputsBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureOutputsIsMutable();
        outputs_.add(index, value);
        onChanged();
      } else {
        outputsBuilder_.addMessage(index, value);
      }
      return this;
    }
    /**
     * <code>repeated .cosmos.bank.v1beta1.Output outputs = 2 [json_name = "outputs", (.gogoproto.nullable) = false];</code>
     */
    public Builder addOutputs(
        com.cosmos.bank.v1beta1.Output.Builder builderForValue) {
      if (outputsBuilder_ == null) {
        ensureOutputsIsMutable();
        outputs_.add(builderForValue.build());
        onChanged();
      } else {
        outputsBuilder_.addMessage(builderForValue.build());
      }
      return this;
    }
    /**
     * <code>repeated .cosmos.bank.v1beta1.Output outputs = 2 [json_name = "outputs", (.gogoproto.nullable) = false];</code>
     */
    public Builder addOutputs(
        int index, com.cosmos.bank.v1beta1.Output.Builder builderForValue) {
      if (outputsBuilder_ == null) {
        ensureOutputsIsMutable();
        outputs_.add(index, builderForValue.build());
        onChanged();
      } else {
        outputsBuilder_.addMessage(index, builderForValue.build());
      }
      return this;
    }
    /**
     * <code>repeated .cosmos.bank.v1beta1.Output outputs = 2 [json_name = "outputs", (.gogoproto.nullable) = false];</code>
     */
    public Builder addAllOutputs(
        java.lang.Iterable<? extends com.cosmos.bank.v1beta1.Output> values) {
      if (outputsBuilder_ == null) {
        ensureOutputsIsMutable();
        com.google.protobuf.AbstractMessageLite.Builder.addAll(
            values, outputs_);
        onChanged();
      } else {
        outputsBuilder_.addAllMessages(values);
      }
      return this;
    }
    /**
     * <code>repeated .cosmos.bank.v1beta1.Output outputs = 2 [json_name = "outputs", (.gogoproto.nullable) = false];</code>
     */
    public Builder clearOutputs() {
      if (outputsBuilder_ == null) {
        outputs_ = java.util.Collections.emptyList();
        bitField0_ = (bitField0_ & ~0x00000002);
        onChanged();
      } else {
        outputsBuilder_.clear();
      }
      return this;
    }
    /**
     * <code>repeated .cosmos.bank.v1beta1.Output outputs = 2 [json_name = "outputs", (.gogoproto.nullable) = false];</code>
     */
    public Builder removeOutputs(int index) {
      if (outputsBuilder_ == null) {
        ensureOutputsIsMutable();
        outputs_.remove(index);
        onChanged();
      } else {
        outputsBuilder_.remove(index);
      }
      return this;
    }
    /**
     * <code>repeated .cosmos.bank.v1beta1.Output outputs = 2 [json_name = "outputs", (.gogoproto.nullable) = false];</code>
     */
    public com.cosmos.bank.v1beta1.Output.Builder getOutputsBuilder(
        int index) {
      return getOutputsFieldBuilder().getBuilder(index);
    }
    /**
     * <code>repeated .cosmos.bank.v1beta1.Output outputs = 2 [json_name = "outputs", (.gogoproto.nullable) = false];</code>
     */
    public com.cosmos.bank.v1beta1.OutputOrBuilder getOutputsOrBuilder(
        int index) {
      if (outputsBuilder_ == null) {
        return outputs_.get(index);  } else {
        return outputsBuilder_.getMessageOrBuilder(index);
      }
    }
    /**
     * <code>repeated .cosmos.bank.v1beta1.Output outputs = 2 [json_name = "outputs", (.gogoproto.nullable) = false];</code>
     */
    public java.util.List<? extends com.cosmos.bank.v1beta1.OutputOrBuilder> 
         getOutputsOrBuilderList() {
      if (outputsBuilder_ != null) {
        return outputsBuilder_.getMessageOrBuilderList();
      } else {
        return java.util.Collections.unmodifiableList(outputs_);
      }
    }
    /**
     * <code>repeated .cosmos.bank.v1beta1.Output outputs = 2 [json_name = "outputs", (.gogoproto.nullable) = false];</code>
     */
    public com.cosmos.bank.v1beta1.Output.Builder addOutputsBuilder() {
      return getOutputsFieldBuilder().addBuilder(
          com.cosmos.bank.v1beta1.Output.getDefaultInstance());
    }
    /**
     * <code>repeated .cosmos.bank.v1beta1.Output outputs = 2 [json_name = "outputs", (.gogoproto.nullable) = false];</code>
     */
    public com.cosmos.bank.v1beta1.Output.Builder addOutputsBuilder(
        int index) {
      return getOutputsFieldBuilder().addBuilder(
          index, com.cosmos.bank.v1beta1.Output.getDefaultInstance());
    }
    /**
     * <code>repeated .cosmos.bank.v1beta1.Output outputs = 2 [json_name = "outputs", (.gogoproto.nullable) = false];</code>
     */
    public java.util.List<com.cosmos.bank.v1beta1.Output.Builder> 
         getOutputsBuilderList() {
      return getOutputsFieldBuilder().getBuilderList();
    }
    private com.google.protobuf.RepeatedFieldBuilderV3<
        com.cosmos.bank.v1beta1.Output, com.cosmos.bank.v1beta1.Output.Builder, com.cosmos.bank.v1beta1.OutputOrBuilder> 
        getOutputsFieldBuilder() {
      if (outputsBuilder_ == null) {
        outputsBuilder_ = new com.google.protobuf.RepeatedFieldBuilderV3<
            com.cosmos.bank.v1beta1.Output, com.cosmos.bank.v1beta1.Output.Builder, com.cosmos.bank.v1beta1.OutputOrBuilder>(
                outputs_,
                ((bitField0_ & 0x00000002) != 0),
                getParentForChildren(),
                isClean());
        outputs_ = null;
      }
      return outputsBuilder_;
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


    // @@protoc_insertion_point(builder_scope:cosmos.bank.v1beta1.MsgMultiSend)
  }

  // @@protoc_insertion_point(class_scope:cosmos.bank.v1beta1.MsgMultiSend)
  private static final com.cosmos.bank.v1beta1.MsgMultiSend DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new com.cosmos.bank.v1beta1.MsgMultiSend();
  }

  public static com.cosmos.bank.v1beta1.MsgMultiSend getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<MsgMultiSend>
      PARSER = new com.google.protobuf.AbstractParser<MsgMultiSend>() {
    @java.lang.Override
    public MsgMultiSend parsePartialFrom(
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

  public static com.google.protobuf.Parser<MsgMultiSend> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<MsgMultiSend> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public com.cosmos.bank.v1beta1.MsgMultiSend getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}
