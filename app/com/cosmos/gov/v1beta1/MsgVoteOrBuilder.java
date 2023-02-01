// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: cosmos/gov/v1beta1/tx.proto

package com.cosmos.gov.v1beta1;

public interface MsgVoteOrBuilder extends
    // @@protoc_insertion_point(interface_extends:cosmos.gov.v1beta1.MsgVote)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>uint64 proposal_id = 1 [json_name = "proposalId", (.gogoproto.jsontag) = "proposal_id", (.gogoproto.moretags) = "yaml:&#92;"proposal_id&#92;""];</code>
   * @return The proposalId.
   */
  long getProposalId();

  /**
   * <code>string voter = 2 [json_name = "voter"];</code>
   * @return The voter.
   */
  java.lang.String getVoter();
  /**
   * <code>string voter = 2 [json_name = "voter"];</code>
   * @return The bytes for voter.
   */
  com.google.protobuf.ByteString
      getVoterBytes();

  /**
   * <code>.cosmos.gov.v1beta1.VoteOption option = 3 [json_name = "option"];</code>
   * @return The enum numeric value on the wire for option.
   */
  int getOptionValue();
  /**
   * <code>.cosmos.gov.v1beta1.VoteOption option = 3 [json_name = "option"];</code>
   * @return The option.
   */
  com.cosmos.gov.v1beta1.VoteOption getOption();
}
