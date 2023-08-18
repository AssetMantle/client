// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: tendermint/types/canonical.proto

package com.tendermint.types;

public interface CanonicalProposalOrBuilder extends
    // @@protoc_insertion_point(interface_extends:tendermint.types.CanonicalProposal)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   * type alias for byte
   * </pre>
   *
   * <code>.tendermint.types.SignedMsgType type = 1 [json_name = "type"];</code>
   * @return The enum numeric value on the wire for type.
   */
  int getTypeValue();
  /**
   * <pre>
   * type alias for byte
   * </pre>
   *
   * <code>.tendermint.types.SignedMsgType type = 1 [json_name = "type"];</code>
   * @return The type.
   */
  com.tendermint.types.SignedMsgType getType();

  /**
   * <pre>
   * canonicalization requires fixed size encoding here
   * </pre>
   *
   * <code>sfixed64 height = 2 [json_name = "height"];</code>
   * @return The height.
   */
  long getHeight();

  /**
   * <pre>
   * canonicalization requires fixed size encoding here
   * </pre>
   *
   * <code>sfixed64 round = 3 [json_name = "round"];</code>
   * @return The round.
   */
  long getRound();

  /**
   * <code>int64 pol_round = 4 [json_name = "polRound", (.gogoproto.customname) = "POLRound"];</code>
   * @return The polRound.
   */
  long getPolRound();

  /**
   * <code>.tendermint.types.CanonicalBlockID block_id = 5 [json_name = "blockId", (.gogoproto.customname) = "BlockID"];</code>
   * @return Whether the blockId field is set.
   */
  boolean hasBlockId();
  /**
   * <code>.tendermint.types.CanonicalBlockID block_id = 5 [json_name = "blockId", (.gogoproto.customname) = "BlockID"];</code>
   * @return The blockId.
   */
  com.tendermint.types.CanonicalBlockID getBlockId();
  /**
   * <code>.tendermint.types.CanonicalBlockID block_id = 5 [json_name = "blockId", (.gogoproto.customname) = "BlockID"];</code>
   */
  com.tendermint.types.CanonicalBlockIDOrBuilder getBlockIdOrBuilder();

  /**
   * <code>.google.protobuf.Timestamp timestamp = 6 [json_name = "timestamp", (.gogoproto.nullable) = false, (.gogoproto.stdtime) = true];</code>
   * @return Whether the timestamp field is set.
   */
  boolean hasTimestamp();
  /**
   * <code>.google.protobuf.Timestamp timestamp = 6 [json_name = "timestamp", (.gogoproto.nullable) = false, (.gogoproto.stdtime) = true];</code>
   * @return The timestamp.
   */
  com.google.protobuf.Timestamp getTimestamp();
  /**
   * <code>.google.protobuf.Timestamp timestamp = 6 [json_name = "timestamp", (.gogoproto.nullable) = false, (.gogoproto.stdtime) = true];</code>
   */
  com.google.protobuf.TimestampOrBuilder getTimestampOrBuilder();

  /**
   * <code>string chain_id = 7 [json_name = "chainId", (.gogoproto.customname) = "ChainID"];</code>
   * @return The chainId.
   */
  java.lang.String getChainId();
  /**
   * <code>string chain_id = 7 [json_name = "chainId", (.gogoproto.customname) = "ChainID"];</code>
   * @return The bytes for chainId.
   */
  com.google.protobuf.ByteString
      getChainIdBytes();
}
