// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: ibc/core/client/v1/tx.proto

package com.ibc.core.client.v1;

public interface MsgUpgradeClientOrBuilder extends
    // @@protoc_insertion_point(interface_extends:ibc.core.client.v1.MsgUpgradeClient)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   * client unique identifier
   * </pre>
   *
   * <code>string client_id = 1 [json_name = "clientId", (.gogoproto.moretags) = "yaml:&#92;"client_id&#92;""];</code>
   * @return The clientId.
   */
  java.lang.String getClientId();
  /**
   * <pre>
   * client unique identifier
   * </pre>
   *
   * <code>string client_id = 1 [json_name = "clientId", (.gogoproto.moretags) = "yaml:&#92;"client_id&#92;""];</code>
   * @return The bytes for clientId.
   */
  com.google.protobuf.ByteString
      getClientIdBytes();

  /**
   * <pre>
   * upgraded client state
   * </pre>
   *
   * <code>.google.protobuf.Any client_state = 2 [json_name = "clientState", (.gogoproto.moretags) = "yaml:&#92;"client_state&#92;""];</code>
   * @return Whether the clientState field is set.
   */
  boolean hasClientState();
  /**
   * <pre>
   * upgraded client state
   * </pre>
   *
   * <code>.google.protobuf.Any client_state = 2 [json_name = "clientState", (.gogoproto.moretags) = "yaml:&#92;"client_state&#92;""];</code>
   * @return The clientState.
   */
  com.google.protobuf.Any getClientState();
  /**
   * <pre>
   * upgraded client state
   * </pre>
   *
   * <code>.google.protobuf.Any client_state = 2 [json_name = "clientState", (.gogoproto.moretags) = "yaml:&#92;"client_state&#92;""];</code>
   */
  com.google.protobuf.AnyOrBuilder getClientStateOrBuilder();

  /**
   * <pre>
   * upgraded consensus state, only contains enough information to serve as a
   * basis of trust in update logic
   * </pre>
   *
   * <code>.google.protobuf.Any consensus_state = 3 [json_name = "consensusState", (.gogoproto.moretags) = "yaml:&#92;"consensus_state&#92;""];</code>
   * @return Whether the consensusState field is set.
   */
  boolean hasConsensusState();
  /**
   * <pre>
   * upgraded consensus state, only contains enough information to serve as a
   * basis of trust in update logic
   * </pre>
   *
   * <code>.google.protobuf.Any consensus_state = 3 [json_name = "consensusState", (.gogoproto.moretags) = "yaml:&#92;"consensus_state&#92;""];</code>
   * @return The consensusState.
   */
  com.google.protobuf.Any getConsensusState();
  /**
   * <pre>
   * upgraded consensus state, only contains enough information to serve as a
   * basis of trust in update logic
   * </pre>
   *
   * <code>.google.protobuf.Any consensus_state = 3 [json_name = "consensusState", (.gogoproto.moretags) = "yaml:&#92;"consensus_state&#92;""];</code>
   */
  com.google.protobuf.AnyOrBuilder getConsensusStateOrBuilder();

  /**
   * <pre>
   * proof that old chain committed to new client
   * </pre>
   *
   * <code>bytes proof_upgrade_client = 4 [json_name = "proofUpgradeClient", (.gogoproto.moretags) = "yaml:&#92;"proof_upgrade_client&#92;""];</code>
   * @return The proofUpgradeClient.
   */
  com.google.protobuf.ByteString getProofUpgradeClient();

  /**
   * <pre>
   * proof that old chain committed to new consensus state
   * </pre>
   *
   * <code>bytes proof_upgrade_consensus_state = 5 [json_name = "proofUpgradeConsensusState", (.gogoproto.moretags) = "yaml:&#92;"proof_upgrade_consensus_state&#92;""];</code>
   * @return The proofUpgradeConsensusState.
   */
  com.google.protobuf.ByteString getProofUpgradeConsensusState();

  /**
   * <pre>
   * signer address
   * </pre>
   *
   * <code>string signer = 6 [json_name = "signer"];</code>
   * @return The signer.
   */
  java.lang.String getSigner();
  /**
   * <pre>
   * signer address
   * </pre>
   *
   * <code>string signer = 6 [json_name = "signer"];</code>
   * @return The bytes for signer.
   */
  com.google.protobuf.ByteString
      getSignerBytes();
}
