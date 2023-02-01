// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: cosmos/genutil/v1beta1/genesis.proto

package com.cosmos.genutil.v1beta1;

public interface GenesisStateOrBuilder extends
    // @@protoc_insertion_point(interface_extends:cosmos.genutil.v1beta1.GenesisState)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   * gen_txs defines the genesis transactions.
   * </pre>
   *
   * <code>repeated bytes gen_txs = 1 [json_name = "genTxs", (.gogoproto.jsontag) = "gentxs", (.gogoproto.moretags) = "yaml:&#92;"gentxs&#92;"", (.gogoproto.casttype) = "encoding/json.RawMessage"];</code>
   * @return A list containing the genTxs.
   */
  java.util.List<com.google.protobuf.ByteString> getGenTxsList();
  /**
   * <pre>
   * gen_txs defines the genesis transactions.
   * </pre>
   *
   * <code>repeated bytes gen_txs = 1 [json_name = "genTxs", (.gogoproto.jsontag) = "gentxs", (.gogoproto.moretags) = "yaml:&#92;"gentxs&#92;"", (.gogoproto.casttype) = "encoding/json.RawMessage"];</code>
   * @return The count of genTxs.
   */
  int getGenTxsCount();
  /**
   * <pre>
   * gen_txs defines the genesis transactions.
   * </pre>
   *
   * <code>repeated bytes gen_txs = 1 [json_name = "genTxs", (.gogoproto.jsontag) = "gentxs", (.gogoproto.moretags) = "yaml:&#92;"gentxs&#92;"", (.gogoproto.casttype) = "encoding/json.RawMessage"];</code>
   * @param index The index of the element to return.
   * @return The genTxs at the given index.
   */
  com.google.protobuf.ByteString getGenTxs(int index);
}
