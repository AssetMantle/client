// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: cosmos/tx/v1beta1/tx.proto

package com.cosmos.tx.v1beta1;

public interface TxOrBuilder extends
    // @@protoc_insertion_point(interface_extends:cosmos.tx.v1beta1.Tx)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   * body is the processable content of the transaction
   * </pre>
   *
   * <code>.cosmos.tx.v1beta1.TxBody body = 1 [json_name = "body"];</code>
   * @return Whether the body field is set.
   */
  boolean hasBody();
  /**
   * <pre>
   * body is the processable content of the transaction
   * </pre>
   *
   * <code>.cosmos.tx.v1beta1.TxBody body = 1 [json_name = "body"];</code>
   * @return The body.
   */
  com.cosmos.tx.v1beta1.TxBody getBody();
  /**
   * <pre>
   * body is the processable content of the transaction
   * </pre>
   *
   * <code>.cosmos.tx.v1beta1.TxBody body = 1 [json_name = "body"];</code>
   */
  com.cosmos.tx.v1beta1.TxBodyOrBuilder getBodyOrBuilder();

  /**
   * <pre>
   * auth_info is the authorization related content of the transaction,
   * specifically signers, signer modes and fee
   * </pre>
   *
   * <code>.cosmos.tx.v1beta1.AuthInfo auth_info = 2 [json_name = "authInfo"];</code>
   * @return Whether the authInfo field is set.
   */
  boolean hasAuthInfo();
  /**
   * <pre>
   * auth_info is the authorization related content of the transaction,
   * specifically signers, signer modes and fee
   * </pre>
   *
   * <code>.cosmos.tx.v1beta1.AuthInfo auth_info = 2 [json_name = "authInfo"];</code>
   * @return The authInfo.
   */
  com.cosmos.tx.v1beta1.AuthInfo getAuthInfo();
  /**
   * <pre>
   * auth_info is the authorization related content of the transaction,
   * specifically signers, signer modes and fee
   * </pre>
   *
   * <code>.cosmos.tx.v1beta1.AuthInfo auth_info = 2 [json_name = "authInfo"];</code>
   */
  com.cosmos.tx.v1beta1.AuthInfoOrBuilder getAuthInfoOrBuilder();

  /**
   * <pre>
   * signatures is a list of signatures that matches the length and order of
   * AuthInfo's signer_infos to allow connecting signature meta information like
   * public key and signing mode by position.
   * </pre>
   *
   * <code>repeated bytes signatures = 3 [json_name = "signatures"];</code>
   * @return A list containing the signatures.
   */
  java.util.List<com.google.protobuf.ByteString> getSignaturesList();
  /**
   * <pre>
   * signatures is a list of signatures that matches the length and order of
   * AuthInfo's signer_infos to allow connecting signature meta information like
   * public key and signing mode by position.
   * </pre>
   *
   * <code>repeated bytes signatures = 3 [json_name = "signatures"];</code>
   * @return The count of signatures.
   */
  int getSignaturesCount();
  /**
   * <pre>
   * signatures is a list of signatures that matches the length and order of
   * AuthInfo's signer_infos to allow connecting signature meta information like
   * public key and signing mode by position.
   * </pre>
   *
   * <code>repeated bytes signatures = 3 [json_name = "signatures"];</code>
   * @param index The index of the element to return.
   * @return The signatures at the given index.
   */
  com.google.protobuf.ByteString getSignatures(int index);
}
