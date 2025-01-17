// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: ibc/core/client/v1/genesis.proto

package com.ibc.core.client.v1;

public interface GenesisStateOrBuilder extends
    // @@protoc_insertion_point(interface_extends:ibc.core.client.v1.GenesisState)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   * client states with their corresponding identifiers
   * </pre>
   *
   * <code>repeated .ibc.core.client.v1.IdentifiedClientState clients = 1 [json_name = "clients", (.gogoproto.nullable) = false, (.gogoproto.castrepeated) = "IdentifiedClientStates"];</code>
   */
  java.util.List<com.ibc.core.client.v1.IdentifiedClientState> 
      getClientsList();
  /**
   * <pre>
   * client states with their corresponding identifiers
   * </pre>
   *
   * <code>repeated .ibc.core.client.v1.IdentifiedClientState clients = 1 [json_name = "clients", (.gogoproto.nullable) = false, (.gogoproto.castrepeated) = "IdentifiedClientStates"];</code>
   */
  com.ibc.core.client.v1.IdentifiedClientState getClients(int index);
  /**
   * <pre>
   * client states with their corresponding identifiers
   * </pre>
   *
   * <code>repeated .ibc.core.client.v1.IdentifiedClientState clients = 1 [json_name = "clients", (.gogoproto.nullable) = false, (.gogoproto.castrepeated) = "IdentifiedClientStates"];</code>
   */
  int getClientsCount();
  /**
   * <pre>
   * client states with their corresponding identifiers
   * </pre>
   *
   * <code>repeated .ibc.core.client.v1.IdentifiedClientState clients = 1 [json_name = "clients", (.gogoproto.nullable) = false, (.gogoproto.castrepeated) = "IdentifiedClientStates"];</code>
   */
  java.util.List<? extends com.ibc.core.client.v1.IdentifiedClientStateOrBuilder> 
      getClientsOrBuilderList();
  /**
   * <pre>
   * client states with their corresponding identifiers
   * </pre>
   *
   * <code>repeated .ibc.core.client.v1.IdentifiedClientState clients = 1 [json_name = "clients", (.gogoproto.nullable) = false, (.gogoproto.castrepeated) = "IdentifiedClientStates"];</code>
   */
  com.ibc.core.client.v1.IdentifiedClientStateOrBuilder getClientsOrBuilder(
      int index);

  /**
   * <pre>
   * consensus states from each client
   * </pre>
   *
   * <code>repeated .ibc.core.client.v1.ClientConsensusStates clients_consensus = 2 [json_name = "clientsConsensus", (.gogoproto.nullable) = false, (.gogoproto.moretags) = "yaml:&#92;"clients_consensus&#92;"", (.gogoproto.castrepeated) = "ClientsConsensusStates"];</code>
   */
  java.util.List<com.ibc.core.client.v1.ClientConsensusStates> 
      getClientsConsensusList();
  /**
   * <pre>
   * consensus states from each client
   * </pre>
   *
   * <code>repeated .ibc.core.client.v1.ClientConsensusStates clients_consensus = 2 [json_name = "clientsConsensus", (.gogoproto.nullable) = false, (.gogoproto.moretags) = "yaml:&#92;"clients_consensus&#92;"", (.gogoproto.castrepeated) = "ClientsConsensusStates"];</code>
   */
  com.ibc.core.client.v1.ClientConsensusStates getClientsConsensus(int index);
  /**
   * <pre>
   * consensus states from each client
   * </pre>
   *
   * <code>repeated .ibc.core.client.v1.ClientConsensusStates clients_consensus = 2 [json_name = "clientsConsensus", (.gogoproto.nullable) = false, (.gogoproto.moretags) = "yaml:&#92;"clients_consensus&#92;"", (.gogoproto.castrepeated) = "ClientsConsensusStates"];</code>
   */
  int getClientsConsensusCount();
  /**
   * <pre>
   * consensus states from each client
   * </pre>
   *
   * <code>repeated .ibc.core.client.v1.ClientConsensusStates clients_consensus = 2 [json_name = "clientsConsensus", (.gogoproto.nullable) = false, (.gogoproto.moretags) = "yaml:&#92;"clients_consensus&#92;"", (.gogoproto.castrepeated) = "ClientsConsensusStates"];</code>
   */
  java.util.List<? extends com.ibc.core.client.v1.ClientConsensusStatesOrBuilder> 
      getClientsConsensusOrBuilderList();
  /**
   * <pre>
   * consensus states from each client
   * </pre>
   *
   * <code>repeated .ibc.core.client.v1.ClientConsensusStates clients_consensus = 2 [json_name = "clientsConsensus", (.gogoproto.nullable) = false, (.gogoproto.moretags) = "yaml:&#92;"clients_consensus&#92;"", (.gogoproto.castrepeated) = "ClientsConsensusStates"];</code>
   */
  com.ibc.core.client.v1.ClientConsensusStatesOrBuilder getClientsConsensusOrBuilder(
      int index);

  /**
   * <pre>
   * metadata from each client
   * </pre>
   *
   * <code>repeated .ibc.core.client.v1.IdentifiedGenesisMetadata clients_metadata = 3 [json_name = "clientsMetadata", (.gogoproto.nullable) = false, (.gogoproto.moretags) = "yaml:&#92;"clients_metadata&#92;""];</code>
   */
  java.util.List<com.ibc.core.client.v1.IdentifiedGenesisMetadata> 
      getClientsMetadataList();
  /**
   * <pre>
   * metadata from each client
   * </pre>
   *
   * <code>repeated .ibc.core.client.v1.IdentifiedGenesisMetadata clients_metadata = 3 [json_name = "clientsMetadata", (.gogoproto.nullable) = false, (.gogoproto.moretags) = "yaml:&#92;"clients_metadata&#92;""];</code>
   */
  com.ibc.core.client.v1.IdentifiedGenesisMetadata getClientsMetadata(int index);
  /**
   * <pre>
   * metadata from each client
   * </pre>
   *
   * <code>repeated .ibc.core.client.v1.IdentifiedGenesisMetadata clients_metadata = 3 [json_name = "clientsMetadata", (.gogoproto.nullable) = false, (.gogoproto.moretags) = "yaml:&#92;"clients_metadata&#92;""];</code>
   */
  int getClientsMetadataCount();
  /**
   * <pre>
   * metadata from each client
   * </pre>
   *
   * <code>repeated .ibc.core.client.v1.IdentifiedGenesisMetadata clients_metadata = 3 [json_name = "clientsMetadata", (.gogoproto.nullable) = false, (.gogoproto.moretags) = "yaml:&#92;"clients_metadata&#92;""];</code>
   */
  java.util.List<? extends com.ibc.core.client.v1.IdentifiedGenesisMetadataOrBuilder> 
      getClientsMetadataOrBuilderList();
  /**
   * <pre>
   * metadata from each client
   * </pre>
   *
   * <code>repeated .ibc.core.client.v1.IdentifiedGenesisMetadata clients_metadata = 3 [json_name = "clientsMetadata", (.gogoproto.nullable) = false, (.gogoproto.moretags) = "yaml:&#92;"clients_metadata&#92;""];</code>
   */
  com.ibc.core.client.v1.IdentifiedGenesisMetadataOrBuilder getClientsMetadataOrBuilder(
      int index);

  /**
   * <code>.ibc.core.client.v1.Params params = 4 [json_name = "params", (.gogoproto.nullable) = false];</code>
   * @return Whether the params field is set.
   */
  boolean hasParams();
  /**
   * <code>.ibc.core.client.v1.Params params = 4 [json_name = "params", (.gogoproto.nullable) = false];</code>
   * @return The params.
   */
  com.ibc.core.client.v1.Params getParams();
  /**
   * <code>.ibc.core.client.v1.Params params = 4 [json_name = "params", (.gogoproto.nullable) = false];</code>
   */
  com.ibc.core.client.v1.ParamsOrBuilder getParamsOrBuilder();

  /**
   * <pre>
   * create localhost on initialization
   * </pre>
   *
   * <code>bool create_localhost = 5 [json_name = "createLocalhost", (.gogoproto.moretags) = "yaml:&#92;"create_localhost&#92;""];</code>
   * @return The createLocalhost.
   */
  boolean getCreateLocalhost();

  /**
   * <pre>
   * the sequence for the next generated client identifier
   * </pre>
   *
   * <code>uint64 next_client_sequence = 6 [json_name = "nextClientSequence", (.gogoproto.moretags) = "yaml:&#92;"next_client_sequence&#92;""];</code>
   * @return The nextClientSequence.
   */
  long getNextClientSequence();
}
