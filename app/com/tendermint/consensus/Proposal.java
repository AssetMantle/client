// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: tendermint/consensus/types.proto

package com.tendermint.consensus;

/**
 * <pre>
 * Proposal is sent when a new block is proposed.
 * </pre>
 *
 * Protobuf type {@code tendermint.consensus.Proposal}
 */
public final class Proposal extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:tendermint.consensus.Proposal)
    ProposalOrBuilder {
private static final long serialVersionUID = 0L;
  // Use Proposal.newBuilder() to construct.
  private Proposal(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private Proposal() {
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new Proposal();
  }

  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return com.tendermint.consensus.TypesProto.internal_static_tendermint_consensus_Proposal_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return com.tendermint.consensus.TypesProto.internal_static_tendermint_consensus_Proposal_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            com.tendermint.consensus.Proposal.class, com.tendermint.consensus.Proposal.Builder.class);
  }

  public static final int PROPOSAL_FIELD_NUMBER = 1;
  private com.tendermint.types.Proposal proposal_;
  /**
   * <code>.tendermint.types.Proposal proposal = 1 [json_name = "proposal", (.gogoproto.nullable) = false];</code>
   * @return Whether the proposal field is set.
   */
  @java.lang.Override
  public boolean hasProposal() {
    return proposal_ != null;
  }
  /**
   * <code>.tendermint.types.Proposal proposal = 1 [json_name = "proposal", (.gogoproto.nullable) = false];</code>
   * @return The proposal.
   */
  @java.lang.Override
  public com.tendermint.types.Proposal getProposal() {
    return proposal_ == null ? com.tendermint.types.Proposal.getDefaultInstance() : proposal_;
  }
  /**
   * <code>.tendermint.types.Proposal proposal = 1 [json_name = "proposal", (.gogoproto.nullable) = false];</code>
   */
  @java.lang.Override
  public com.tendermint.types.ProposalOrBuilder getProposalOrBuilder() {
    return proposal_ == null ? com.tendermint.types.Proposal.getDefaultInstance() : proposal_;
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
    if (proposal_ != null) {
      output.writeMessage(1, getProposal());
    }
    getUnknownFields().writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (proposal_ != null) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(1, getProposal());
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
    if (!(obj instanceof com.tendermint.consensus.Proposal)) {
      return super.equals(obj);
    }
    com.tendermint.consensus.Proposal other = (com.tendermint.consensus.Proposal) obj;

    if (hasProposal() != other.hasProposal()) return false;
    if (hasProposal()) {
      if (!getProposal()
          .equals(other.getProposal())) return false;
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
    if (hasProposal()) {
      hash = (37 * hash) + PROPOSAL_FIELD_NUMBER;
      hash = (53 * hash) + getProposal().hashCode();
    }
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static com.tendermint.consensus.Proposal parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.tendermint.consensus.Proposal parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.tendermint.consensus.Proposal parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.tendermint.consensus.Proposal parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.tendermint.consensus.Proposal parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.tendermint.consensus.Proposal parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.tendermint.consensus.Proposal parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.tendermint.consensus.Proposal parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.tendermint.consensus.Proposal parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static com.tendermint.consensus.Proposal parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.tendermint.consensus.Proposal parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.tendermint.consensus.Proposal parseFrom(
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
  public static Builder newBuilder(com.tendermint.consensus.Proposal prototype) {
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
   * Proposal is sent when a new block is proposed.
   * </pre>
   *
   * Protobuf type {@code tendermint.consensus.Proposal}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:tendermint.consensus.Proposal)
      com.tendermint.consensus.ProposalOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return com.tendermint.consensus.TypesProto.internal_static_tendermint_consensus_Proposal_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return com.tendermint.consensus.TypesProto.internal_static_tendermint_consensus_Proposal_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              com.tendermint.consensus.Proposal.class, com.tendermint.consensus.Proposal.Builder.class);
    }

    // Construct using com.tendermint.consensus.Proposal.newBuilder()
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
      proposal_ = null;
      if (proposalBuilder_ != null) {
        proposalBuilder_.dispose();
        proposalBuilder_ = null;
      }
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return com.tendermint.consensus.TypesProto.internal_static_tendermint_consensus_Proposal_descriptor;
    }

    @java.lang.Override
    public com.tendermint.consensus.Proposal getDefaultInstanceForType() {
      return com.tendermint.consensus.Proposal.getDefaultInstance();
    }

    @java.lang.Override
    public com.tendermint.consensus.Proposal build() {
      com.tendermint.consensus.Proposal result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public com.tendermint.consensus.Proposal buildPartial() {
      com.tendermint.consensus.Proposal result = new com.tendermint.consensus.Proposal(this);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartial0(com.tendermint.consensus.Proposal result) {
      int from_bitField0_ = bitField0_;
      if (((from_bitField0_ & 0x00000001) != 0)) {
        result.proposal_ = proposalBuilder_ == null
            ? proposal_
            : proposalBuilder_.build();
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
      if (other instanceof com.tendermint.consensus.Proposal) {
        return mergeFrom((com.tendermint.consensus.Proposal)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(com.tendermint.consensus.Proposal other) {
      if (other == com.tendermint.consensus.Proposal.getDefaultInstance()) return this;
      if (other.hasProposal()) {
        mergeProposal(other.getProposal());
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
                  getProposalFieldBuilder().getBuilder(),
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

    private com.tendermint.types.Proposal proposal_;
    private com.google.protobuf.SingleFieldBuilderV3<
        com.tendermint.types.Proposal, com.tendermint.types.Proposal.Builder, com.tendermint.types.ProposalOrBuilder> proposalBuilder_;
    /**
     * <code>.tendermint.types.Proposal proposal = 1 [json_name = "proposal", (.gogoproto.nullable) = false];</code>
     * @return Whether the proposal field is set.
     */
    public boolean hasProposal() {
      return ((bitField0_ & 0x00000001) != 0);
    }
    /**
     * <code>.tendermint.types.Proposal proposal = 1 [json_name = "proposal", (.gogoproto.nullable) = false];</code>
     * @return The proposal.
     */
    public com.tendermint.types.Proposal getProposal() {
      if (proposalBuilder_ == null) {
        return proposal_ == null ? com.tendermint.types.Proposal.getDefaultInstance() : proposal_;
      } else {
        return proposalBuilder_.getMessage();
      }
    }
    /**
     * <code>.tendermint.types.Proposal proposal = 1 [json_name = "proposal", (.gogoproto.nullable) = false];</code>
     */
    public Builder setProposal(com.tendermint.types.Proposal value) {
      if (proposalBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        proposal_ = value;
      } else {
        proposalBuilder_.setMessage(value);
      }
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <code>.tendermint.types.Proposal proposal = 1 [json_name = "proposal", (.gogoproto.nullable) = false];</code>
     */
    public Builder setProposal(
        com.tendermint.types.Proposal.Builder builderForValue) {
      if (proposalBuilder_ == null) {
        proposal_ = builderForValue.build();
      } else {
        proposalBuilder_.setMessage(builderForValue.build());
      }
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <code>.tendermint.types.Proposal proposal = 1 [json_name = "proposal", (.gogoproto.nullable) = false];</code>
     */
    public Builder mergeProposal(com.tendermint.types.Proposal value) {
      if (proposalBuilder_ == null) {
        if (((bitField0_ & 0x00000001) != 0) &&
          proposal_ != null &&
          proposal_ != com.tendermint.types.Proposal.getDefaultInstance()) {
          getProposalBuilder().mergeFrom(value);
        } else {
          proposal_ = value;
        }
      } else {
        proposalBuilder_.mergeFrom(value);
      }
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <code>.tendermint.types.Proposal proposal = 1 [json_name = "proposal", (.gogoproto.nullable) = false];</code>
     */
    public Builder clearProposal() {
      bitField0_ = (bitField0_ & ~0x00000001);
      proposal_ = null;
      if (proposalBuilder_ != null) {
        proposalBuilder_.dispose();
        proposalBuilder_ = null;
      }
      onChanged();
      return this;
    }
    /**
     * <code>.tendermint.types.Proposal proposal = 1 [json_name = "proposal", (.gogoproto.nullable) = false];</code>
     */
    public com.tendermint.types.Proposal.Builder getProposalBuilder() {
      bitField0_ |= 0x00000001;
      onChanged();
      return getProposalFieldBuilder().getBuilder();
    }
    /**
     * <code>.tendermint.types.Proposal proposal = 1 [json_name = "proposal", (.gogoproto.nullable) = false];</code>
     */
    public com.tendermint.types.ProposalOrBuilder getProposalOrBuilder() {
      if (proposalBuilder_ != null) {
        return proposalBuilder_.getMessageOrBuilder();
      } else {
        return proposal_ == null ?
            com.tendermint.types.Proposal.getDefaultInstance() : proposal_;
      }
    }
    /**
     * <code>.tendermint.types.Proposal proposal = 1 [json_name = "proposal", (.gogoproto.nullable) = false];</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
        com.tendermint.types.Proposal, com.tendermint.types.Proposal.Builder, com.tendermint.types.ProposalOrBuilder> 
        getProposalFieldBuilder() {
      if (proposalBuilder_ == null) {
        proposalBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
            com.tendermint.types.Proposal, com.tendermint.types.Proposal.Builder, com.tendermint.types.ProposalOrBuilder>(
                getProposal(),
                getParentForChildren(),
                isClean());
        proposal_ = null;
      }
      return proposalBuilder_;
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


    // @@protoc_insertion_point(builder_scope:tendermint.consensus.Proposal)
  }

  // @@protoc_insertion_point(class_scope:tendermint.consensus.Proposal)
  private static final com.tendermint.consensus.Proposal DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new com.tendermint.consensus.Proposal();
  }

  public static com.tendermint.consensus.Proposal getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<Proposal>
      PARSER = new com.google.protobuf.AbstractParser<Proposal>() {
    @java.lang.Override
    public Proposal parsePartialFrom(
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

  public static com.google.protobuf.Parser<Proposal> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<Proposal> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public com.tendermint.consensus.Proposal getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

