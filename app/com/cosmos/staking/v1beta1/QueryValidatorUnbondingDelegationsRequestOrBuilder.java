// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: cosmos/staking/v1beta1/query.proto

package com.cosmos.staking.v1beta1;

public interface QueryValidatorUnbondingDelegationsRequestOrBuilder extends
    // @@protoc_insertion_point(interface_extends:cosmos.staking.v1beta1.QueryValidatorUnbondingDelegationsRequest)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   * validator_addr defines the validator address to query for.
   * </pre>
   *
   * <code>string validator_addr = 1 [json_name = "validatorAddr"];</code>
   * @return The validatorAddr.
   */
  java.lang.String getValidatorAddr();
  /**
   * <pre>
   * validator_addr defines the validator address to query for.
   * </pre>
   *
   * <code>string validator_addr = 1 [json_name = "validatorAddr"];</code>
   * @return The bytes for validatorAddr.
   */
  com.google.protobuf.ByteString
      getValidatorAddrBytes();

  /**
   * <pre>
   * pagination defines an optional pagination for the request.
   * </pre>
   *
   * <code>.cosmos.base.query.v1beta1.PageRequest pagination = 2 [json_name = "pagination"];</code>
   * @return Whether the pagination field is set.
   */
  boolean hasPagination();
  /**
   * <pre>
   * pagination defines an optional pagination for the request.
   * </pre>
   *
   * <code>.cosmos.base.query.v1beta1.PageRequest pagination = 2 [json_name = "pagination"];</code>
   * @return The pagination.
   */
  com.cosmos.base.query.v1beta1.PageRequest getPagination();
  /**
   * <pre>
   * pagination defines an optional pagination for the request.
   * </pre>
   *
   * <code>.cosmos.base.query.v1beta1.PageRequest pagination = 2 [json_name = "pagination"];</code>
   */
  com.cosmos.base.query.v1beta1.PageRequestOrBuilder getPaginationOrBuilder();
}