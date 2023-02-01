// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: ibc/core/client/v1/query.proto

package com.ibc.core.client.v1;

public interface QueryConsensusStateResponseOrBuilder extends
    // @@protoc_insertion_point(interface_extends:ibc.core.client.v1.QueryConsensusStateResponse)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   * consensus state associated with the client identifier at the given height
   * </pre>
   *
   * <code>.google.protobuf.Any consensus_state = 1 [json_name = "consensusState"];</code>
   * @return Whether the consensusState field is set.
   */
  boolean hasConsensusState();
  /**
   * <pre>
   * consensus state associated with the client identifier at the given height
   * </pre>
   *
   * <code>.google.protobuf.Any consensus_state = 1 [json_name = "consensusState"];</code>
   * @return The consensusState.
   */
  com.google.protobuf.Any getConsensusState();
  /**
   * <pre>
   * consensus state associated with the client identifier at the given height
   * </pre>
   *
   * <code>.google.protobuf.Any consensus_state = 1 [json_name = "consensusState"];</code>
   */
  com.google.protobuf.AnyOrBuilder getConsensusStateOrBuilder();

  /**
   * <pre>
   * merkle proof of existence
   * </pre>
   *
   * <code>bytes proof = 2 [json_name = "proof"];</code>
   * @return The proof.
   */
  com.google.protobuf.ByteString getProof();

  /**
   * <pre>
   * height at which the proof was retrieved
   * </pre>
   *
   * <code>.ibc.core.client.v1.Height proof_height = 3 [json_name = "proofHeight", (.gogoproto.nullable) = false];</code>
   * @return Whether the proofHeight field is set.
   */
  boolean hasProofHeight();
  /**
   * <pre>
   * height at which the proof was retrieved
   * </pre>
   *
   * <code>.ibc.core.client.v1.Height proof_height = 3 [json_name = "proofHeight", (.gogoproto.nullable) = false];</code>
   * @return The proofHeight.
   */
  com.ibc.core.client.v1.Height getProofHeight();
  /**
   * <pre>
   * height at which the proof was retrieved
   * </pre>
   *
   * <code>.ibc.core.client.v1.Height proof_height = 3 [json_name = "proofHeight", (.gogoproto.nullable) = false];</code>
   */
  com.ibc.core.client.v1.HeightOrBuilder getProofHeightOrBuilder();
}
