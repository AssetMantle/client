// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: cosmos/tx/v1beta1/service.proto

package com.cosmos.tx.v1beta1;

public interface BroadcastTxResponseOrBuilder extends
    // @@protoc_insertion_point(interface_extends:cosmos.tx.v1beta1.BroadcastTxResponse)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   * tx_response is the queried TxResponses.
   * </pre>
   *
   * <code>.cosmos.base.abci.v1beta1.TxResponse tx_response = 1 [json_name = "txResponse"];</code>
   * @return Whether the txResponse field is set.
   */
  boolean hasTxResponse();
  /**
   * <pre>
   * tx_response is the queried TxResponses.
   * </pre>
   *
   * <code>.cosmos.base.abci.v1beta1.TxResponse tx_response = 1 [json_name = "txResponse"];</code>
   * @return The txResponse.
   */
  com.cosmos.base.abci.v1beta1.TxResponse getTxResponse();
  /**
   * <pre>
   * tx_response is the queried TxResponses.
   * </pre>
   *
   * <code>.cosmos.base.abci.v1beta1.TxResponse tx_response = 1 [json_name = "txResponse"];</code>
   */
  com.cosmos.base.abci.v1beta1.TxResponseOrBuilder getTxResponseOrBuilder();
}
