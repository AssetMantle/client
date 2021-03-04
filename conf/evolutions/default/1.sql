# --- !Ups

CREATE SCHEMA IF NOT EXISTS BLOCKCHAIN
    AUTHORIZATION "commit";
CREATE SCHEMA IF NOT EXISTS BLOCKCHAIN_TRANSACTION
    AUTHORIZATION "commit";
CREATE SCHEMA IF NOT EXISTS KEY_BASE
    AUTHORIZATION "commit";
CREATE SCHEMA IF NOT EXISTS MASTER
    AUTHORIZATION "commit";
CREATE SCHEMA IF NOT EXISTS MASTER_TRANSACTION
    AUTHORIZATION "commit";
CREATE SCHEMA IF NOT EXISTS WESTERN_UNION
    AUTHORIZATION "commit";
CREATE SCHEMA IF NOT EXISTS DOCUSIGN
    AUTHORIZATION "commit";
CREATE SCHEMA IF NOT EXISTS MEMBER_CHECK
    AUTHORIZATION "commit";

CREATE TABLE IF NOT EXISTS BLOCKCHAIN."Account_BC"
(
    "address"           VARCHAR NOT NULL,
    "username"          VARCHAR NOT NULL UNIQUE,
    "coins"             VARCHAR NOT NULL,
    "publicKey"         VARCHAR NOT NULL,
    "accountNumber"     VARCHAR NOT NULL,
    "sequence"          VARCHAR NOT NULL,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("address")
);


CREATE TABLE IF NOT EXISTS BLOCKCHAIN."Asset_BC"
(
    "id"                VARCHAR NOT NULL,
    "immutables"        VARCHAR NOT NULL,
    "mutables"          VARCHAR NOT NULL,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN."AverageBlockTime"
(
    "id"                VARCHAR NOT NULL,
    "height"            INTEGER NOT NULL,
    "value"             BIGINT  NOT NULL,
    "time"              VARCHAR NOT NULL,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN."Block"
(
    "height"            INTEGER NOT NULL,
    "time"              VARCHAR NOT NULL,
    "proposerAddress"   VARCHAR NOT NULL,
    "validators"        VARCHAR NOT NULL,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("height")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN."ACLAccount_BC"
(
    "address"           VARCHAR NOT NULL,
    "zoneID"            VARCHAR NOT NULL,
    "organizationID"    VARCHAR NOT NULL,
    "aclHash"           VARCHAR NOT NULL,
    "dirtyBit"          BOOLEAN NOT NULL,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("address")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN."ACLHash_BC"
(
    "issueAssets"        BOOLEAN NOT NULL,
    "issueFiats"         BOOLEAN NOT NULL,
    "sendAssets"         BOOLEAN NOT NULL,
    "sendFiats"          BOOLEAN NOT NULL,
    "redeemAssets"       BOOLEAN NOT NULL,
    "redeemFiats"        BOOLEAN NOT NULL,
    "sellerExecuteOrder" BOOLEAN NOT NULL,
    "buyerExecuteOrder"  BOOLEAN NOT NULL,
    "changeBuyerBid"     BOOLEAN NOT NULL,
    "changeSellerBid"    BOOLEAN NOT NULL,
    "confirmBuyerBid"    BOOLEAN NOT NULL,
    "confirmSellerBid"   BOOLEAN NOT NULL,
    "negotiation"        BOOLEAN NOT NULL,
    "releaseAssets"      BOOLEAN NOT NULL,
    "hash"               VARCHAR NOT NULL,
    "createdBy"          VARCHAR,
    "createdOn"          TIMESTAMP,
    "createdOnTimeZone"  VARCHAR,
    "updatedBy"          VARCHAR,
    "updatedOn"          TIMESTAMP,
    "updatedOnTimeZone"  VARCHAR,
    PRIMARY KEY ("hash")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN."Fiat_BC"
(
    "pegHash"           VARCHAR NOT NULL,
    "ownerAddress"      VARCHAR NOT NULL,
    "transactionID"     VARCHAR NOT NULL,
    "transactionAmount" VARCHAR NOT NULL,
    "redeemedAmount"    VARCHAR NOT NULL,
    "dirtyBit"          BOOLEAN NOT NULL,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("pegHash", "ownerAddress")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN."Classification_BC"
(
    "id"                VARCHAR NOT NULL,
    "immutableTraits"   VARCHAR NOT NULL,
    "mutableTraits"     VARCHAR NOT NULL,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN."Delegation"
(
    "delegatorAddress"  VARCHAR NOT NULL,
    "validatorAddress"  VARCHAR NOT NULL,
    "shares"            NUMERIC NOT NULL,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("delegatorAddress", "validatorAddress")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN."IdentityProperties_BC"
(
    "id"                VARCHAR NOT NULL,
    "immutables"        VARCHAR NOT NULL,
    "mutables"          VARCHAR NOT NULL,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN."IdentityProvisioned_BC"
(
    "id"                VARCHAR NOT NULL,
    "address"           VARCHAR NOT NULL,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("id", "address")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN."IdentityUnprovisioned_BC"
(
    "id"                VARCHAR NOT NULL,
    "address"           VARCHAR NOT NULL,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("id", "address")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN."Maintainer_BC"
(
    "id"                VARCHAR NOT NULL,
    "maintainedTraits"  VARCHAR NOT NULL,
    "addMaintainer"     BOOLEAN NOT NULL,
    "removeMaintainer"  BOOLEAN NOT NULL,
    "mutateMaintainer"  BOOLEAN NOT NULL,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN."Meta_BC"
(
    "id"                VARCHAR NOT NULL,
    "dataType"          VARCHAR NOT NULL,
    "dataValue"         VARCHAR NOT NULL,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("id", "dataType")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN."Order_BC"
(
    "id"                VARCHAR NOT NULL,
    "immutables"        VARCHAR NOT NULL,
    "mutables"          VARCHAR NOT NULL,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("id")
);


CREATE TABLE IF NOT EXISTS BLOCKCHAIN."Parameter"
(
    "parameterType"     VARCHAR NOT NULL,
    "value"             VARCHAR NOT NULL,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("parameterType")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN."Redelegation"
(
    "delegatorAddress"            VARCHAR NOT NULL,
    "validatorSourceAddress"      VARCHAR NOT NULL,
    "validatorDestinationAddress" VARCHAR NOT NULL,
    "entries"                     VARCHAR NOT NULL,
    "createdBy"                   VARCHAR,
    "createdOn"                   TIMESTAMP,
    "createdOnTimeZone"           VARCHAR,
    "updatedBy"                   VARCHAR,
    "updatedOn"                   TIMESTAMP,
    "updatedOnTimeZone"           VARCHAR,
    PRIMARY KEY ("delegatorAddress", "validatorSourceAddress", "validatorDestinationAddress")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN."Split_BC"
(
    "ownerID"           VARCHAR NOT NULL,
    "ownableID"         VARCHAR NOT NULL,
    "split"             NUMERIC NOT NULL,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("ownerID", "ownableID")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN."Token"
(
    "denom"             VARCHAR NOT NULL,
    "totalSupply"       VARCHAR NOT NULL,
    "bondedAmount"      VARCHAR NOT NULL,
    "notBondedAmount"   VARCHAR NOT NULL,
    "communityPool"     VARCHAR NOT NULL,
    "inflation"         NUMERIC NOT NULL,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("denom")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN."Transaction"
(
    "hash"              VARCHAR NOT NULL,
    "height"            INTEGER NOT NULL,
    "code"              INTEGER,
    "rawLog"            VARCHAR NOT NULL,
    "status"            BOOLEAN NOT NULL,
    "gasWanted"         VARCHAR NOT NULL,
    "gasUsed"           VARCHAR NOT NULL,
    "messages"          VARCHAR NOT NULL,
    "fee"               VARCHAR NOT NULL,
    "timestamp"         VARCHAR NOT NULL,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("hash")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN."Undelegation"
(
    "delegatorAddress"  VARCHAR NOT NULL,
    "validatorAddress"  VARCHAR NOT NULL,
    "entries"           VARCHAR NOT NULL,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("delegatorAddress", "validatorAddress")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN."Validator"
(
    "operatorAddress"       VARCHAR NOT NULL,
    "hexAddress"            VARCHAR NOT NULL UNIQUE,
    "consensusPublicKey"    VARCHAR NOT NULL UNIQUE,
    "jailed"                BOOLEAN NOT NULL,
    "status"                INTEGER NOT NULL,
    "tokens"                VARCHAR NOT NULL,
    "delegatorShares"       NUMERIC NOT NULL,
    "description"           VARCHAR NOT NULL,
    "unbondingHeight"       INTEGER,
    "unbondingTime"         VARCHAR,
    "commission"            VARCHAR NOT NULL,
    "minimumSelfDelegation" VARCHAR NOT NULL,
    "createdBy"             VARCHAR,
    "createdOn"             TIMESTAMP,
    "createdOnTimeZone"     VARCHAR,
    "updatedBy"             VARCHAR,
    "updatedOn"             TIMESTAMP,
    "updatedOnTimeZone"     VARCHAR,
    PRIMARY KEY ("operatorAddress")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN."WithdrawAddress"
(
    "delegatorAddress"  VARCHAR NOT NULL,
    "withdrawAddress"   VARCHAR NOT NULL,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("delegatorAddress")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN_TRANSACTION."IssueFiat"
(
    "from"              VARCHAR NOT NULL,
    "to"                VARCHAR NOT NULL,
    "transactionID"     VARCHAR NOT NULL,
    "transactionAmount" VARCHAR NOT NULL,
    "gas"               VARCHAR NOT NULL,
    "status"            BOOLEAN,
    "txHash"            VARCHAR,
    "ticketID"          VARCHAR NOT NULL,
    "mode"              VARCHAR NOT NULL,
    "code"              VARCHAR,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("ticketID")
);


CREATE TABLE IF NOT EXISTS BLOCKCHAIN_TRANSACTION."RedeemFiat"
(
    "from"              VARCHAR NOT NULL,
    "to"                VARCHAR NOT NULL,
    "redeemAmount"      VARCHAR NOT NULL,
    "gas"               VARCHAR NOT NULL,
    "status"            BOOLEAN,
    "txHash"            VARCHAR,
    "ticketID"          VARCHAR NOT NULL,
    "mode"              VARCHAR NOT NULL,
    "code"              VARCHAR,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("ticketID")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN_TRANSACTION."SendCoin"
(
    "from"              VARCHAR NOT NULL,
    "to"                VARCHAR NOT NULL,
    "amount"            VARCHAR NOT NULL,
    "gas"               VARCHAR NOT NULL,
    "status"            BOOLEAN,
    "txHash"            VARCHAR,
    "ticketID"          VARCHAR NOT NULL,
    "mode"              VARCHAR NOT NULL,
    "code"              VARCHAR,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("ticketID")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN_TRANSACTION."SendFiat"
(
    "from"              VARCHAR NOT NULL,
    "to"                VARCHAR NOT NULL,
    "amount"            VARCHAR NOT NULL,
    "pegHash"           VARCHAR NOT NULL,
    "gas"               VARCHAR NOT NULL,
    "status"            BOOLEAN,
    "txHash"            VARCHAR,
    "ticketID"          VARCHAR NOT NULL,
    "mode"              VARCHAR NOT NULL,
    "code"              VARCHAR,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("ticketID")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN_TRANSACTION."SetACL"
(
    "from"              VARCHAR NOT NULL,
    "aclAddress"        VARCHAR NOT NULL,
    "organizationID"    VARCHAR NOT NULL,
    "zoneID"            VARCHAR NOT NULL,
    "aclHash"           VARCHAR NOT NULL,
    "gas"               VARCHAR NOT NULL,
    "status"            BOOLEAN,
    "txHash"            VARCHAR,
    "ticketID"          VARCHAR NOT NULL,
    "mode"              VARCHAR NOT NULL,
    "code"              VARCHAR,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("ticketID")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN_TRANSACTION."SetBuyerFeedback"
(
    "from"              VARCHAR NOT NULL,
    "to"                VARCHAR NOT NULL,
    "pegHash"           VARCHAR NOT NULL,
    "rating"            INT     NOT NULL,
    "gas"               VARCHAR NOT NULL,
    "status"            BOOLEAN,
    "txHash"            VARCHAR,
    "ticketID"          VARCHAR NOT NULL,
    "mode"              VARCHAR NOT NULL,
    "code"              VARCHAR,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("ticketID")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN_TRANSACTION."SetSellerFeedback"
(
    "from"              VARCHAR NOT NULL,
    "to"                VARCHAR NOT NULL,
    "pegHash"           VARCHAR NOT NULL,
    "rating"            INT     NOT NULL,
    "gas"               VARCHAR NOT NULL,
    "status"            BOOLEAN,
    "txHash"            VARCHAR,
    "ticketID"          VARCHAR NOT NULL,
    "mode"              VARCHAR NOT NULL,
    "code"              VARCHAR,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("ticketID")
);


CREATE TABLE IF NOT EXISTS BLOCKCHAIN_TRANSACTION."AssetDefine"
(
    "from"                VARCHAR NOT NULL,
    "fromID"              VARCHAR NOT NULL,
    "immutableMetaTraits" VARCHAR NOT NULL,
    "immutableTraits"     VARCHAR NOT NULL,
    "mutableMetaTraits"   VARCHAR NOT NULL,
    "mutableTraits"       VARCHAR NOT NULL,
    "gas"                 VARCHAR NOT NULL,
    "status"              BOOLEAN,
    "txHash"              VARCHAR,
    "ticketID"            VARCHAR NOT NULL,
    "mode"                VARCHAR NOT NULL,
    "code"                VARCHAR,
    "createdBy"           VARCHAR,
    "createdOn"           TIMESTAMP,
    "createdOnTimeZone"   VARCHAR,
    "updatedBy"           VARCHAR,
    "updatedOn"           TIMESTAMP,
    "updatedOnTimeZone"   VARCHAR,
    PRIMARY KEY ("ticketID")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN_TRANSACTION."AssetMint"
(
    "from"                    VARCHAR NOT NULL,
    "fromID"                  VARCHAR NOT NULL,
    "toID"                    VARCHAR NOT NULL,
    "classificationID"        VARCHAR NOT NULL,
    "immutableMetaProperties" VARCHAR NOT NULL,
    "immutableProperties"     VARCHAR NOT NULL,
    "mutableMetaProperties"   VARCHAR NOT NULL,
    "mutableProperties"       VARCHAR NOT NULL,
    "gas"                     VARCHAR NOT NULL,
    "status"                  BOOLEAN,
    "txHash"                  VARCHAR,
    "ticketID"                VARCHAR NOT NULL,
    "mode"                    VARCHAR NOT NULL,
    "code"                    VARCHAR,
    "createdBy"               VARCHAR,
    "createdOn"               TIMESTAMP,
    "createdOnTimeZone"       VARCHAR,
    "updatedBy"               VARCHAR,
    "updatedOn"               TIMESTAMP,
    "updatedOnTimeZone"       VARCHAR,
    PRIMARY KEY ("ticketID")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN_TRANSACTION."AssetMutate"
(
    "from"                  VARCHAR NOT NULL,
    "fromID"                VARCHAR NOT NULL,
    "assetID"               VARCHAR NOT NULL,
    "mutableMetaProperties" VARCHAR NOT NULL,
    "mutableProperties"     VARCHAR NOT NULL,
    "gas"                   VARCHAR NOT NULL,
    "status"                BOOLEAN,
    "txHash"                VARCHAR,
    "ticketID"              VARCHAR NOT NULL,
    "mode"                  VARCHAR NOT NULL,
    "code"                  VARCHAR,
    "createdBy"             VARCHAR,
    "createdOn"             TIMESTAMP,
    "createdOnTimeZone"     VARCHAR,
    "updatedBy"             VARCHAR,
    "updatedOn"             TIMESTAMP,
    "updatedOnTimeZone"     VARCHAR,
    PRIMARY KEY ("ticketID")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN_TRANSACTION."AssetBurn"
(
    "from"              VARCHAR NOT NULL,
    "fromID"            VARCHAR NOT NULL,
    "assetID"           VARCHAR NOT NULL,
    "gas"               VARCHAR NOT NULL,
    "status"            BOOLEAN,
    "txHash"            VARCHAR,
    "ticketID"          VARCHAR NOT NULL,
    "mode"              VARCHAR NOT NULL,
    "code"              VARCHAR,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("ticketID")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN_TRANSACTION."IdentityDefine"
(
    "from"                VARCHAR NOT NULL,
    "fromID"              VARCHAR NOT NULL,
    "immutableMetaTraits" VARCHAR NOT NULL,
    "immutableTraits"     VARCHAR NOT NULL,
    "mutableMetaTraits"   VARCHAR NOT NULL,
    "mutableTraits"       VARCHAR NOT NULL,
    "gas"                 VARCHAR NOT NULL,
    "status"              BOOLEAN,
    "txHash"              VARCHAR,
    "ticketID"            VARCHAR NOT NULL,
    "mode"                VARCHAR NOT NULL,
    "code"                VARCHAR,
    "createdBy"           VARCHAR,
    "createdOn"           TIMESTAMP,
    "createdOnTimeZone"   VARCHAR,
    "updatedBy"           VARCHAR,
    "updatedOn"           TIMESTAMP,
    "updatedOnTimeZone"   VARCHAR,
    PRIMARY KEY ("ticketID")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN_TRANSACTION."IdentityIssue"
(
    "from"                    VARCHAR NOT NULL,
    "to"                      VARCHAR NOT NULL,
    "fromID"                  VARCHAR NOT NULL,
    "classificationID"        VARCHAR NOT NULL,
    "immutableMetaProperties" VARCHAR NOT NULL,
    "immutableProperties"     VARCHAR NOT NULL,
    "mutableMetaProperties"   VARCHAR NOT NULL,
    "mutableProperties"       VARCHAR NOT NULL,
    "gas"                     VARCHAR NOT NULL,
    "status"                  BOOLEAN,
    "txHash"                  VARCHAR,
    "ticketID"                VARCHAR NOT NULL,
    "mode"                    VARCHAR NOT NULL,
    "code"                    VARCHAR,
    "createdBy"               VARCHAR,
    "createdOn"               TIMESTAMP,
    "createdOnTimeZone"       VARCHAR,
    "updatedBy"               VARCHAR,
    "updatedOn"               TIMESTAMP,
    "updatedOnTimeZone"       VARCHAR,
    PRIMARY KEY ("ticketID")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN_TRANSACTION."IdentityProvision"
(
    "from"              VARCHAR NOT NULL,
    "to"                VARCHAR NOT NULL,
    "identityID"        VARCHAR NOT NULL,
    "gas"               VARCHAR NOT NULL,
    "status"            BOOLEAN,
    "txHash"            VARCHAR,
    "ticketID"          VARCHAR NOT NULL,
    "mode"              VARCHAR NOT NULL,
    "code"              VARCHAR,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("ticketID")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN_TRANSACTION."IdentityUnprovision"
(
    "from"              VARCHAR NOT NULL,
    "to"                VARCHAR NOT NULL,
    "identityID"        VARCHAR NOT NULL,
    "gas"               VARCHAR NOT NULL,
    "status"            BOOLEAN,
    "txHash"            VARCHAR,
    "ticketID"          VARCHAR NOT NULL,
    "mode"              VARCHAR NOT NULL,
    "code"              VARCHAR,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("ticketID")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN_TRANSACTION."IdentityNub"
(
    "from"              VARCHAR NOT NULL,
    "nubID"             VARCHAR NOT NULL,
    "gas"               VARCHAR NOT NULL,
    "status"            BOOLEAN,
    "txHash"            VARCHAR,
    "ticketID"          VARCHAR NOT NULL,
    "mode"              VARCHAR NOT NULL,
    "code"              VARCHAR,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("ticketID")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN_TRANSACTION."MaintainerDeputize"
(
    "from"              VARCHAR NOT NULL,
    "fromID"            VARCHAR NOT NULL,
    "toID"              VARCHAR NOT NULL,
    "classificationID"  VARCHAR NOT NULL,
    "maintainedTraits"  VARCHAR NOT NULL,
    "addMaintainer"     BOOLEAN NOT NULL,
    "removeMaintainer"  BOOLEAN NOT NULL,
    "mutateMaintainer"  BOOLEAN NOT NULL,
    "gas"               VARCHAR NOT NULL,
    "status"            BOOLEAN,
    "txHash"            VARCHAR,
    "ticketID"          VARCHAR NOT NULL,
    "mode"              VARCHAR NOT NULL,
    "code"              VARCHAR,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("ticketID")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN_TRANSACTION."MetaReveal"
(
    "from"              VARCHAR NOT NULL,
    "metaFact"          VARCHAR NOT NULL,
    "gas"               VARCHAR NOT NULL,
    "status"            BOOLEAN,
    "txHash"            VARCHAR,
    "ticketID"          VARCHAR NOT NULL,
    "mode"              VARCHAR NOT NULL,
    "code"              VARCHAR,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("ticketID")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN_TRANSACTION."OrderDefine"
(
    "from"                VARCHAR NOT NULL,
    "fromID"              VARCHAR NOT NULL,
    "immutableMetaTraits" VARCHAR NOT NULL,
    "immutableTraits"     VARCHAR NOT NULL,
    "mutableMetaTraits"   VARCHAR NOT NULL,
    "mutableTraits"       VARCHAR NOT NULL,
    "gas"                 VARCHAR NOT NULL,
    "status"              BOOLEAN,
    "txHash"              VARCHAR,
    "ticketID"            VARCHAR NOT NULL,
    "mode"                VARCHAR NOT NULL,
    "code"                VARCHAR,
    "createdBy"           VARCHAR,
    "createdOn"           TIMESTAMP,
    "createdOnTimeZone"   VARCHAR,
    "updatedBy"           VARCHAR,
    "updatedOn"           TIMESTAMP,
    "updatedOnTimeZone"   VARCHAR,
    PRIMARY KEY ("ticketID")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN_TRANSACTION."OrderMake"
(
    "from"                    VARCHAR NOT NULL,
    "fromID"                  VARCHAR NOT NULL,
    "classificationID"        VARCHAR NOT NULL,
    "makerOwnableID"          VARCHAR NOT NULL,
    "takerOwnableID"          VARCHAR NOT NULL,
    "expiresIn"               INTEGER NOT NULL,
    "makerOwnableSplit"       NUMERIC NOT NULL,
    "immutableMetaProperties" VARCHAR NOT NULL,
    "immutableProperties"     VARCHAR NOT NULL,
    "mutableMetaProperties"   VARCHAR NOT NULL,
    "mutableProperties"       VARCHAR NOT NULL,
    "gas"                     VARCHAR NOT NULL,
    "status"                  BOOLEAN,
    "txHash"                  VARCHAR,
    "ticketID"                VARCHAR NOT NULL,
    "mode"                    VARCHAR NOT NULL,
    "code"                    VARCHAR,
    "createdBy"               VARCHAR,
    "createdOn"               TIMESTAMP,
    "createdOnTimeZone"       VARCHAR,
    "updatedBy"               VARCHAR,
    "updatedOn"               TIMESTAMP,
    "updatedOnTimeZone"       VARCHAR,
    PRIMARY KEY ("ticketID")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN_TRANSACTION."OrderTake"
(
    "from"              VARCHAR NOT NULL,
    "fromID"            VARCHAR NOT NULL,
    "takerOwnableSplit" NUMERIC NOT NULL,
    "orderID"           VARCHAR NOT NULL,
    "gas"               VARCHAR NOT NULL,
    "status"            BOOLEAN,
    "txHash"            VARCHAR,
    "ticketID"          VARCHAR NOT NULL,
    "mode"              VARCHAR NOT NULL,
    "code"              VARCHAR,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("ticketID")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN_TRANSACTION."OrderCancel"
(
    "from"              VARCHAR NOT NULL,
    "fromID"            VARCHAR NOT NULL,
    "orderID"           VARCHAR NOT NULL,
    "gas"               VARCHAR NOT NULL,
    "status"            BOOLEAN,
    "txHash"            VARCHAR,
    "ticketID"          VARCHAR NOT NULL,
    "mode"              VARCHAR NOT NULL,
    "code"              VARCHAR,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("ticketID")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN_TRANSACTION."SendCoin"
(
    "from"              VARCHAR NOT NULL,
    "to"                VARCHAR NOT NULL,
    "amount"            VARCHAR NOT NULL,
    "gas"               VARCHAR NOT NULL,
    "status"            BOOLEAN,
    "txHash"            VARCHAR,
    "ticketID"          VARCHAR NOT NULL,
    "mode"              VARCHAR NOT NULL,
    "code"              VARCHAR,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("ticketID")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN_TRANSACTION."SplitSend"
(
    "from"              VARCHAR NOT NULL,
    "fromID"            VARCHAR NOT NULL,
    "toID"              VARCHAR NOT NULL,
    "ownableID"         VARCHAR NOT NULL,
    "split"             NUMERIC NOT NULL,
    "gas"               VARCHAR NOT NULL,
    "status"            BOOLEAN,
    "txHash"            VARCHAR,
    "ticketID"          VARCHAR NOT NULL,
    "mode"              VARCHAR NOT NULL,
    "code"              VARCHAR,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("ticketID")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN_TRANSACTION."SplitWrap"
(
    "from"              VARCHAR NOT NULL,
    "fromID"            VARCHAR NOT NULL,
    "coins"             VARCHAR NOT NULL,
    "gas"               VARCHAR NOT NULL,
    "status"            BOOLEAN,
    "txHash"            VARCHAR,
    "ticketID"          VARCHAR NOT NULL,
    "mode"              VARCHAR NOT NULL,
    "code"              VARCHAR,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("ticketID")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN_TRANSACTION."SplitUnwrap"
(
    "from"              VARCHAR NOT NULL,
    "fromID"            VARCHAR NOT NULL,
    "ownableID"         VARCHAR NOT NULL,
    "split"             NUMERIC NOT NULL,
    "gas"               VARCHAR NOT NULL,
    "status"            BOOLEAN,
    "txHash"            VARCHAR,
    "ticketID"          VARCHAR NOT NULL,
    "mode"              VARCHAR NOT NULL,
    "code"              VARCHAR,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("ticketID")
);

CREATE TABLE IF NOT EXISTS DOCUSIGN."Envelope"
(
    "id"                VARCHAR NOT NULL,
    "envelopeID"        VARCHAR NOT NULL,
    "documentType"      VARCHAR NOT NULL,
    "status"            VARCHAR NOT NULL,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS DOCUSIGN."Envelope_History"
(
    "id"                VARCHAR   NOT NULL,
    "envelopeID"        VARCHAR   NOT NULL,
    "documentType"      VARCHAR   NOT NULL,
    "status"            VARCHAR   NOT NULL,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    "deletedBy"         VARCHAR   NOT NULL,
    "deletedOn"         TIMESTAMP NOT NULL,
    "deletedOnTimeZone" VARCHAR   NOT NULL,
    PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS DOCUSIGN."OAuthToken"
(
    "id"                VARCHAR NOT NULL,
    "accessToken"       VARCHAR NOT NULL,
    "expiresAt"         BIGINT  NOT NULL,
    "refreshToken"      VARCHAR NOT NULL,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS KEY_BASE."ValidatorAccount"
(
    "address"           VARCHAR NOT NULL,
    "identity"          VARCHAR,
    "username"          VARCHAR,
    "picture"           BYTEA,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("address")
);

CREATE TABLE IF NOT EXISTS MASTER."Account"
(
    "id"                VARCHAR NOT NULL,
    "secretHash"        VARCHAR,
    "language"          VARCHAR,
    "userType"          VARCHAR NOT NULL,
    "partialMnemonic"   VARCHAR,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS MASTER."AccountFile"
(
    "id"                VARCHAR NOT NULL,
    "documentType"      VARCHAR NOT NULL,
    "fileName"          VARCHAR NOT NULL,
    "file"              BYTEA,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("id", "documentType")
);

CREATE TABLE IF NOT EXISTS MASTER."AccountKYC"
(
    "id"                VARCHAR NOT NULL,
    "documentType"      VARCHAR NOT NULL,
    "fileName"          VARCHAR NOT NULL UNIQUE,
    "file"              BYTEA,
    "status"            BOOLEAN,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("id", "documentType")
);

CREATE TABLE IF NOT EXISTS MASTER."Asset"
(
    "id"                VARCHAR NOT NULL,
    "label"             VARCHAR,
    "status"            VARCHAR NOT NULL,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("id")
);


CREATE TABLE IF NOT EXISTS MASTER."Asset_History"
(
    "id"                VARCHAR NOT NULL,
    "label"             VARCHAR,
    "status"            VARCHAR NOT NULL,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    "deletedBy"         VARCHAR   NOT NULL,
    "deletedOn"         TIMESTAMP NOT NULL,
    "deletedOnTimeZone" VARCHAR   NOT NULL,
    PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS MASTER."Classification"
(
    "id"                VARCHAR NOT NULL,
    "maintainerID"      VARCHAR NOT NULL,
    "entityType"        VARCHAR NOT NULL,
    "label"             VARCHAR,
    "status"            BOOLEAN,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("id", "maintainerID")
);

CREATE TABLE IF NOT EXISTS MASTER."Email"
(
    "id"                VARCHAR NOT NULL,
    "emailAddress"      VARCHAR NOT NULL,
    "status"            BOOLEAN NOT NULL,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS MASTER."Fiat"
(
    "ownerID"           VARCHAR NOT NULL,
    "transactionID"     VARCHAR NOT NULL,
    "transactionAmount" VARCHAR NOT NULL,
    "amountRedeemed"    VARCHAR,
    "status"            BOOLEAN,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("transactionID", "ownerID")
);

CREATE TABLE IF NOT EXISTS MASTER."Identification"
(
    "accountID"          VARCHAR NOT NULL,
    "firstName"          VARCHAR NOT NULL,
    "lastName"           VARCHAR NOT NULL,
    "dateOfBirth"        DATE    NOT NULL,
    "idNumber"           VARCHAR NOT NULL,
    "idType"             VARCHAR NOT NULL,
    "address"            VARCHAR NOT NULL,
    "completionStatus"   BOOLEAN NOT NULL,
    "verificationStatus" BOOLEAN,
    "createdBy"          VARCHAR,
    "createdOn"          TIMESTAMP,
    "createdOnTimeZone"  VARCHAR,
    "updatedBy"          VARCHAR,
    "updatedOn"          TIMESTAMP,
    "updatedOnTimeZone"  VARCHAR,
    PRIMARY KEY ("accountID")
);

CREATE TABLE IF NOT EXISTS MASTER."Identity"
(
    "id"                VARCHAR NOT NULL,
    "label"             VARCHAR,
    "status"            BOOLEAN,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS MASTER."Mobile"
(
    "id"                VARCHAR NOT NULL,
    "mobileNumber"      VARCHAR NOT NULL,
    "status"            BOOLEAN NOT NULL,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS MASTER."Negotiation"
(
    "id"                             VARCHAR NOT NULL,
    "orderID"                        VARCHAR UNIQUE,
    "buyerTraderID"                  VARCHAR NOT NULL,
    "sellerTraderID"                 VARCHAR NOT NULL,
    "assetID"                        VARCHAR NOT NULL,
    "assetDescription"               VARCHAR NOT NULL,
    "price"                          VARCHAR NOT NULL,
    "quantity"                       VARCHAR NOT NULL,
    "quantityUnit"                   VARCHAR NOT NULL,
    "buyerAcceptedAssetDescription"  BOOLEAN NOT NULL,
    "buyerAcceptedPrice"             BOOLEAN NOT NULL,
    "buyerAcceptedQuantity"          BOOLEAN NOT NULL,
    "assetOtherDetails"              VARCHAR NOT NULL,
    "buyerAcceptedAssetOtherDetails" BOOLEAN NOT NULL,
    "time"                           INT,
    "paymentTerms"                   VARCHAR NOT NULL,
    "buyerAcceptedPaymentTerms"      BOOLEAN NOT NULL,
    "documentList"                   VARCHAR NOT NULL,
    "buyerAcceptedDocumentList"      BOOLEAN NOT NULL,
    "physicalDocumentsHandledVia"    VARCHAR,
    "chatID"                         VARCHAR UNIQUE,
    "status"                         VARCHAR NOT NULL,
    "comment"                        VARCHAR,
    "createdBy"                      VARCHAR,
    "createdOn"                      TIMESTAMP,
    "createdOnTimeZone"              VARCHAR,
    "updatedBy"                      VARCHAR,
    "updatedOn"                      TIMESTAMP,
    "updatedOnTimeZone"              VARCHAR,
    PRIMARY KEY ("id"),
    UNIQUE ("buyerTraderID", "sellerTraderID", "assetID")
);

CREATE TABLE IF NOT EXISTS MASTER."Negotiation_History"
(
    "id"                             VARCHAR   NOT NULL,
    "negotiationID"                  VARCHAR,
    "buyerTraderID"                  VARCHAR   NOT NULL,
    "sellerTraderID"                 VARCHAR   NOT NULL,
    "assetID"                        VARCHAR   NOT NULL,
    "assetDescription"               VARCHAR   NOT NULL,
    "price"                          VARCHAR   NOT NULL,
    "quantity"                       VARCHAR   NOT NULL,
    "quantityUnit"                   VARCHAR   NOT NULL,
    "buyerAcceptedAssetDescription"  BOOLEAN   NOT NULL,
    "buyerAcceptedPrice"             BOOLEAN   NOT NULL,
    "buyerAcceptedQuantity"          BOOLEAN   NOT NULL,
    "assetOtherDetails"              VARCHAR   NOT NULL,
    "buyerAcceptedAssetOtherDetails" BOOLEAN   NOT NULL,
    "time"                           INT,
    "paymentTerms"                   VARCHAR   NOT NULL,
    "buyerAcceptedPaymentTerms"      BOOLEAN   NOT NULL,
    "documentList"                   VARCHAR   NOT NULL,
    "buyerAcceptedDocumentList"      BOOLEAN   NOT NULL,
    "physicalDocumentsHandledVia"    VARCHAR,
    "chatID"                         VARCHAR,
    "status"                         VARCHAR   NOT NULL,
    "comment"                        VARCHAR,
    "createdBy"                      VARCHAR,
    "createdOn"                      TIMESTAMP,
    "createdOnTimeZone"              VARCHAR,
    "updatedBy"                      VARCHAR,
    "updatedOn"                      TIMESTAMP,
    "updatedOnTimeZone"              VARCHAR,
    "deletedBy"                      VARCHAR   NOT NULL,
    "deletedOn"                      TIMESTAMP NOT NULL,
    "deletedOnTimeZone"              VARCHAR   NOT NULL,
    PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS MASTER."Order"
(
    "id"                VARCHAR NOT NULL,
    "label"             VARCHAR,
    "makerID"           VARCHAR NOT NULL,
    "makerOwnableID"    VARCHAR NOT NULL,
    "takerOwnableID"    VARCHAR NOT NULL,
    "status"            BOOLEAN,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS MASTER."Order_History"
(
    "id"                VARCHAR   NOT NULL,
    "orderID"           VARCHAR   NOT NULL,
    "status"            VARCHAR   NOT NULL,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    "deletedBy"         VARCHAR   NOT NULL,
    "deletedOn"         TIMESTAMP NOT NULL,
    "deletedOnTimeZone" VARCHAR   NOT NULL,
    PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS MASTER."Organization"
(
    "id"                 VARCHAR NOT NULL,
    "zoneID"             VARCHAR NOT NULL,
    "accountID"          VARCHAR NOT NULL UNIQUE,
    "name"               VARCHAR NOT NULL,
    "abbreviation"       VARCHAR,
    "establishmentDate"  DATE    NOT NULL,
    "email"              VARCHAR NOT NULL,
    "registeredAddress"  VARCHAR NOT NULL,
    "postalAddress"      VARCHAR NOT NULL,
    "completionStatus"   BOOLEAN NOT NULL,
    "verificationStatus" BOOLEAN,
    "deputizeStatus"     BOOLEAN NOT NULL,
    "comment"            VARCHAR,
    "createdBy"          VARCHAR,
    "createdOn"          TIMESTAMP,
    "createdOnTimeZone"  VARCHAR,
    "updatedBy"          VARCHAR,
    "updatedOn"          TIMESTAMP,
    "updatedOnTimeZone"  VARCHAR,
    PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS MASTER."OrganizationUBO"
(
    "id"                VARCHAR          NOT NULL,
    "organizationID"    VARCHAR          NOT NULL,
    "firstName"         VARCHAR          NOT NULL,
    "lastName"          VARCHAR          NOT NULL,
    "sharePercentage"   DOUBLE PRECISION NOT NULL,
    "relationship"      VARCHAR          NOT NULL,
    "title"             VARCHAR          NOT NULL,
    "status"            BOOLEAN,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS MASTER."OrganizationBankAccountDetail"
(
    "id"                VARCHAR NOT NULL,
    "accountHolder"     VARCHAR NOT NULL,
    "nickName"          VARCHAR NOT NULL,
    "accountNumber"     VARCHAR NOT NULL,
    "bankName"          VARCHAR NOT NULL,
    "swiftAddress"      VARCHAR NOT NULL,
    "address"           VARCHAR NOT NULL,
    "country"           VARCHAR NOT NULL,
    "zipCode"           VARCHAR NOT NULL,
    "status"            BOOLEAN,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS MASTER."OrganizationKYC"
(
    "id"                VARCHAR NOT NULL,
    "documentType"      VARCHAR NOT NULL,
    "fileName"          VARCHAR NOT NULL UNIQUE,
    "file"              BYTEA,
    "status"            BOOLEAN,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("id", "documentType")
);

CREATE TABLE IF NOT EXISTS MASTER."Trader"
(
    "id"                VARCHAR NOT NULL,
    "zoneID"            VARCHAR NOT NULL,
    "organizationID"    VARCHAR NOT NULL,
    "accountID"         VARCHAR NOT NULL UNIQUE,
    "status"            BOOLEAN,
    "deputizeStatus"    BOOLEAN NOT NULL,
    "comment"           VARCHAR,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS MASTER."Property"
(
    "entityID"          VARCHAR NOT NULL,
    "entityType"        VARCHAR NOT NULL,
    "name"              VARCHAR NOT NULL,
    "value"             VARCHAR,
    "dataType"          VARCHAR NOT NULL,
    "isMeta"            BOOLEAN NOT NULL,
    "isMutable"         BOOLEAN NOT NULL,
    "hashID"            VARCHAR NOT NULL,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("entityID", "entityType", "name")
);

CREATE TABLE IF NOT EXISTS MASTER."TraderRelation"
(
    "id"                VARCHAR NOT NULL,
    "fromID"            VARCHAR NOT NULL,
    "toID"              VARCHAR NOT NULL,
    "status"            BOOLEAN,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS MASTER."Zone"
(
    "id"                 VARCHAR NOT NULL,
    "accountID"          VARCHAR NOT NULL UNIQUE,
    "name"               VARCHAR NOT NULL,
    "currency"           VARCHAR NOT NULL,
    "address"            VARCHAR NOT NULL,
    "completionStatus"   BOOLEAN NOT NULL,
    "verificationStatus" BOOLEAN,
    "deputizeStatus"     BOOLEAN NOT NULL,
    "createdBy"          VARCHAR,
    "createdOn"          TIMESTAMP,
    "createdOnTimeZone"  VARCHAR,
    "updatedBy"          VARCHAR,
    "updatedOn"          TIMESTAMP,
    "updatedOnTimeZone"  VARCHAR,
    PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS MASTER."Split"
(
    "ownableID"         VARCHAR NOT NULL,
    "ownerID"           VARCHAR NOT NULL,
    "entityType"        VARCHAR NOT NULL,
    "status"            BOOLEAN,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("ownableID", "ownerID")
);

CREATE TABLE IF NOT EXISTS MASTER."ZoneKYC"
(
    "id"                VARCHAR NOT NULL,
    "documentType"      VARCHAR NOT NULL,
    "fileName"          VARCHAR NOT NULL UNIQUE,
    "file"              BYTEA,
    "status"            BOOLEAN,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("id", "documentType")
);

CREATE TABLE IF NOT EXISTS MASTER_TRANSACTION."AssetFile"
(
    "id"                  VARCHAR NOT NULL,
    "documentType"        VARCHAR NOT NULL,
    "fileName"            VARCHAR NOT NULL UNIQUE,
    "file"                BYTEA,
    "documentContentJson" VARCHAR,
    "status"              BOOLEAN,
    "createdBy"           VARCHAR,
    "createdOn"           TIMESTAMP,
    "createdOnTimeZone"   VARCHAR,
    "updatedBy"           VARCHAR,
    "updatedOn"           TIMESTAMP,
    "updatedOnTimeZone"   VARCHAR,
    PRIMARY KEY ("id", "documentType")
);

CREATE TABLE IF NOT EXISTS MASTER_TRANSACTION."AssetFile_History"
(
    "id"                  VARCHAR   NOT NULL,
    "documentType"        VARCHAR   NOT NULL,
    "fileName"            VARCHAR   NOT NULL,
    "file"                BYTEA,
    "documentContentJson" VARCHAR,
    "status"              BOOLEAN,
    "createdBy"           VARCHAR,
    "createdOn"           TIMESTAMP,
    "createdOnTimeZone"   VARCHAR,
    "updatedBy"           VARCHAR,
    "updatedOn"           TIMESTAMP,
    "updatedOnTimeZone"   VARCHAR,
    "deletedBy"           VARCHAR   NOT NULL,
    "deletedOn"           TIMESTAMP NOT NULL,
    "deletedOnTimeZone"   VARCHAR   NOT NULL,
    PRIMARY KEY ("id", "documentType")
);

CREATE TABLE IF NOT EXISTS MASTER_TRANSACTION."Chat"
(
    "id"                VARCHAR NOT NULL,
    "accountID"         VARCHAR NOT NULL,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("id", "accountID")
);

CREATE TABLE IF NOT EXISTS MASTER_TRANSACTION."EmailOTP"
(
    "id"                VARCHAR NOT NULL,
    "secretHash"        VARCHAR NOT NULL,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS MASTER_TRANSACTION."FaucetRequest"
(
    "id"                VARCHAR NOT NULL,
    "accountID"         VARCHAR NOT NULL,
    "amount"            VARCHAR NOT NULL,
    "gas"               VARCHAR,
    "status"            BOOLEAN,
    "ticketID"          VARCHAR,
    "comment"           VARCHAR,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS MASTER_TRANSACTION."Message"
(
    "id"                VARCHAR NOT NULL,
    "fromAccountID"     VARCHAR NOT NULL,
    "chatID"            VARCHAR NOT NULL,
    "text"              VARCHAR NOT NULL,
    "replyToID"         VARCHAR,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS MASTER_TRANSACTION."MessageRead"
(
    "messageID"         VARCHAR NOT NULL,
    "accountID"         VARCHAR NOT NULL,
    "read"              BOOLEAN NOT NULL,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("messageID", "accountID")
);

CREATE TABLE IF NOT EXISTS MASTER_TRANSACTION."NegotiationFile"
(
    "id"                  VARCHAR NOT NULL,
    "documentType"        VARCHAR NOT NULL,
    "fileName"            VARCHAR NOT NULL UNIQUE,
    "file"                BYTEA,
    "documentContentJson" VARCHAR,
    "status"              BOOLEAN,
    "createdBy"           VARCHAR,
    "createdOn"           TIMESTAMP,
    "createdOnTimeZone"   VARCHAR,
    "updatedBy"           VARCHAR,
    "updatedOn"           TIMESTAMP,
    "updatedOnTimeZone"   VARCHAR,
    PRIMARY KEY ("id", "documentType")
);

CREATE TABLE IF NOT EXISTS MASTER_TRANSACTION."NegotiationFile_History"
(
    "id"                  VARCHAR   NOT NULL,
    "documentType"        VARCHAR   NOT NULL,
    "fileName"            VARCHAR   NOT NULL,
    "file"                BYTEA,
    "documentContentJson" VARCHAR,
    "status"              BOOLEAN,
    "createdBy"           VARCHAR,
    "createdOn"           TIMESTAMP,
    "createdOnTimeZone"   VARCHAR,
    "updatedBy"           VARCHAR,
    "updatedOn"           TIMESTAMP,
    "updatedOnTimeZone"   VARCHAR,
    "deletedBy"           VARCHAR   NOT NULL,
    "deletedOn"           TIMESTAMP NOT NULL,
    "deletedOnTimeZone"   VARCHAR   NOT NULL,
    PRIMARY KEY ("id", "documentType")
);

CREATE TABLE IF NOT EXISTS MASTER_TRANSACTION."Notification"
(
    "id"                       VARCHAR NOT NULL,
    "accountID"                VARCHAR,
    "notificationTemplateJson" VARCHAR NOT NULL,
    "jsRoute"                  VARCHAR,
    "read"                     BOOLEAN NOT NULL,
    "createdBy"                VARCHAR,
    "createdOn"                TIMESTAMP,
    "createdOnTimeZone"        VARCHAR,
    "updatedBy"                VARCHAR,
    "updatedOn"                TIMESTAMP,
    "updatedOnTimeZone"        VARCHAR,
    PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS MASTER_TRANSACTION."PushNotificationToken"
(
    "id"                VARCHAR NOT NULL,
    "token"             VARCHAR NOT NULL,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS MASTER_TRANSACTION."ReceiveFiat"
(
    "id"                VARCHAR NOT NULL,
    "traderID"          VARCHAR NOT NULL,
    "orderID"           VARCHAR NOT NULL UNIQUE,
    "amount"            VARCHAR NOT NULL,
    "status"            VARCHAR NOT NULL,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS MASTER_TRANSACTION."ReceiveFiat_History"
(
    "id"                VARCHAR   NOT NULL,
    "traderID"          VARCHAR   NOT NULL,
    "orderID"           VARCHAR   NOT NULL,
    "amount"            VARCHAR   NOT NULL,
    "status"            VARCHAR   NOT NULL,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    "deletedBy"         VARCHAR   NOT NULL,
    "deletedOn"         TIMESTAMP NOT NULL,
    "deletedOnTimeZone" VARCHAR   NOT NULL,
    PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS MASTER_TRANSACTION."RedeemFiatRequest"
(
    "id"                VARCHAR NOT NULL,
    "traderID"          VARCHAR NOT NULL,
    "ticketID"          VARCHAR NOT NULL UNIQUE,
    "amount"            VARCHAR NOT NULL,
    "status"            VARCHAR NOT NULL,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS MASTER_TRANSACTION."SendFiatRequest"
(
    "id"                VARCHAR NOT NULL,
    "traderID"          VARCHAR NOT NULL,
    "negotiationID"     VARCHAR NOT NULL,
    "ticketID"          VARCHAR NOT NULL UNIQUE,
    "amount"            VARCHAR NOT NULL,
    "status"            VARCHAR NOT NULL,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS MASTER_TRANSACTION."SendFiatRequest_History"
(
    "id"                VARCHAR   NOT NULL,
    "traderID"          VARCHAR   NOT NULL,
    "negotiationID"     VARCHAR   NOT NULL,
    "ticketID"          VARCHAR   NOT NULL,
    "amount"            VARCHAR   NOT NULL,
    "status"            VARCHAR   NOT NULL,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    "deletedBy"         VARCHAR   NOT NULL,
    "deletedOn"         TIMESTAMP NOT NULL,
    "deletedOnTimeZone" VARCHAR   NOT NULL,
    PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS MASTER_TRANSACTION."SessionToken"
(
    "id"                VARCHAR NOT NULL,
    "sessionTokenHash"  VARCHAR NOT NULL,
    "sessionTokenTime"  BIGINT  NOT NULL,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS MASTER_TRANSACTION."SMSOTP"
(
    "id"                VARCHAR NOT NULL,
    "secretHash"        VARCHAR NOT NULL,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS MASTER_TRANSACTION."TokenPrice"
(
    "serial"            SERIAL  NOT NULL,
    "denom"             VARCHAR NOT NULL,
    "price"             NUMERIC NOT NULL,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("denom")
);

CREATE TABLE IF NOT EXISTS MASTER_TRANSACTION."TradeActivity"
(
    "id"                        VARCHAR NOT NULL,
    "negotiationID"             VARCHAR NOT NULL,
    "tradeActivityTemplateJson" VARCHAR NOT NULL,
    "read"                      BOOLEAN NOT NULL,
    "createdBy"                 VARCHAR,
    "createdOn"                 TIMESTAMP,
    "createdOnTimeZone"         VARCHAR,
    "updatedBy"                 VARCHAR,
    "updatedOn"                 TIMESTAMP,
    "updatedOnTimeZone"         VARCHAR,
    PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS MASTER_TRANSACTION."TradeActivity_History"
(
    "id"                        VARCHAR   NOT NULL,
    "negotiationID"             VARCHAR   NOT NULL,
    "tradeActivityTemplateJson" VARCHAR   NOT NULL,
    "read"                      BOOLEAN   NOT NULL,
    "createdBy"                 VARCHAR,
    "createdOn"                 TIMESTAMP,
    "createdOnTimeZone"         VARCHAR,
    "updatedBy"                 VARCHAR,
    "updatedOn"                 TIMESTAMP,
    "updatedOnTimeZone"         VARCHAR,
    "deletedBy"                 VARCHAR   NOT NULL,
    "deletedOn"                 TIMESTAMP NOT NULL,
    "deletedOnTimeZone"         VARCHAR   NOT NULL,
    PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS MASTER_TRANSACTION."TraderInvitation"
(
    "id"                  VARCHAR NOT NULL,
    "organizationID"      VARCHAR NOT NULL,
    "inviteeEmailAddress" VARCHAR NOT NULL,
    "status"              VARCHAR NOT NULL,
    "createdBy"           VARCHAR,
    "createdOn"           TIMESTAMP,
    "createdOnTimeZone"   VARCHAR,
    "updatedBy"           VARCHAR,
    "updatedOn"           TIMESTAMP,
    "updatedOnTimeZone"   VARCHAR,
    PRIMARY KEY ("id"),
    UNIQUE ("organizationID", "inviteeEmailAddress")
);

CREATE TABLE IF NOT EXISTS MASTER_TRANSACTION."ZoneInvitation"
(
    "id"                VARCHAR NOT NULL,
    "emailAddress"      VARCHAR NOT NULL UNIQUE,
    "accountID"         VARCHAR UNIQUE,
    "status"            BOOLEAN,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS WESTERN_UNION."FiatRequest"
(
    "id"                VARCHAR NOT NULL,
    "traderID"          VARCHAR NOT NULL,
    "transactionAmount" VARCHAR NOT NULL,
    "status"            VARCHAR NOT NULL,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS WESTERN_UNION."RTCB"
(
    "id"                VARCHAR   NOT NULL,
    "reference"         VARCHAR   NOT NULL,
    "externalReference" VARCHAR   NOT NULL,
    "invoiceNumber"     VARCHAR   NOT NULL,
    "buyerBusinessId"   VARCHAR   NOT NULL,
    "buyerFirstName"    VARCHAR   NOT NULL,
    "buyerLastName"     VARCHAR   NOT NULL,
    "createdDate"       TIMESTAMP NOT NULL,
    "lastUpdatedDate"   TIMESTAMP NOT NULL,
    "status"            VARCHAR   NOT NULL,
    "dealType"          VARCHAR   NOT NULL,
    "paymentTypeId"     VARCHAR   NOT NULL,
    "paidOutAmount"     VARCHAR   NOT NULL,
    "requestSignature"  VARCHAR   NOT NULL,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS WESTERN_UNION."SFTPFileTransaction"
(
    "payerID"              VARCHAR NOT NULL,
    "invoiceNumber"        VARCHAR NOT NULL,
    "customerFirstName"    VARCHAR NOT NULL,
    "customerLastName"     VARCHAR NOT NULL,
    "customerEmailAddress" VARCHAR NOT NULL,
    "settlementDate"       VARCHAR NOT NULL,
    "clientReceivedAmount" VARCHAR NOT NULL,
    "transactionType"      VARCHAR NOT NULL,
    "productType"          VARCHAR NOT NULL,
    "transactionReference" VARCHAR NOT NULL,
    "createdBy"            VARCHAR,
    "createdOn"            TIMESTAMP,
    "createdOnTimeZone"    VARCHAR,
    "updatedBy"            VARCHAR,
    "updatedOn"            TIMESTAMP,
    "updatedOnTimeZone"    VARCHAR,
    PRIMARY KEY ("transactionReference")
);

CREATE TABLE IF NOT EXISTS MEMBER_CHECK."MemberScan"
(
    "id"                VARCHAR NOT NULL,
    "firstName"         VARCHAR NOT NULL,
    "lastName"          VARCHAR NOT NULL,
    "scanID"            INT     NOT NULL UNIQUE,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("id"),
    UNIQUE ("firstName", "lastName")
);

CREATE TABLE IF NOT EXISTS MEMBER_CHECK."MemberScanDecision"
(
    "id"                VARCHAR NOT NULL,
    "scanID"            INT     NOT NULL,
    "resultID"          INT,
    "status"            BOOLEAN NOT NULL,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("id")
);


CREATE TABLE IF NOT EXISTS MEMBER_CHECK."CorporateScan"
(
    "id"                VARCHAR NOT NULL,
    "companyName"       VARCHAR NOT NULL UNIQUE,
    "scanID"            INT     NOT NULL UNIQUE,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS MEMBER_CHECK."CorporateScanDecision"
(
    "id"                VARCHAR NOT NULL,
    "scanID"            INT     NOT NULL,
    "resultID"          INT,
    "status"            BOOLEAN NOT NULL,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS MEMBER_CHECK."VesselScan"
(
    "id"                VARCHAR NOT NULL,
    "vesselName"        VARCHAR NOT NULL UNIQUE,
    "scanID"            INT     NOT NULL UNIQUE,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS MEMBER_CHECK."VesselScanDecision"
(
    "id"                VARCHAR NOT NULL,
    "scanID"            INT     NOT NULL,
    "resultID"          INT,
    "status"            BOOLEAN NOT NULL,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS MEMBER_CHECK."VesselScanDecision_History"
(
    "id"                VARCHAR   NOT NULL,
    "scanID"            INT       NOT NULL,
    "resultID"          INT,
    "status"            BOOLEAN   NOT NULL,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    "deletedBy"         VARCHAR   NOT NULL,
    "deletedOn"         TIMESTAMP NOT NULL,
    "deletedOnTimeZone" VARCHAR   NOT NULL,
    PRIMARY KEY ("id")
);

/*Triggers*/

CREATE OR REPLACE FUNCTION PUBLIC.INSERT_OR_UPDATE_LOG() RETURNS TRIGGER AS
$$
BEGIN
    IF (TG_OP = 'INSERT') THEN
        new."createdOn" = CURRENT_TIMESTAMP;;
        new."createdOnTimeZone" = CURRENT_SETTING('TIMEZONE');;
        new."createdBy" = CURRENT_USER;;
    ELSEIF (TG_OP = 'UPDATE') THEN
--         values of created needs to be set here otherwise insertOrUpdate of slick will omit created details
        new."createdOn" = old."createdOn";;
        new."createdOnTimeZone" = old."createdOnTimeZone";;
        new."createdBy" = old."createdBy";;
        new."updatedOn" = CURRENT_TIMESTAMP;;
        new."updatedOnTimeZone" = CURRENT_SETTING('TIMEZONE');;
        new."updatedBy" = CURRENT_USER;;
    END IF;;
    RETURN NEW;;
END;;
$$ LANGUAGE PLPGSQL;

CREATE TRIGGER ACCOUNT_BC_LOG
    BEFORE INSERT OR UPDATE
    ON BLOCKCHAIN."Account_BC"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_LOG();
CREATE TRIGGER ACL_ACCOUNT_BC_LOG
    BEFORE INSERT OR UPDATE
    ON BLOCKCHAIN."ACLAccount_BC"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_LOG();
CREATE TRIGGER ACL_HASH_BC_LOG
    BEFORE INSERT OR UPDATE
    ON BLOCKCHAIN."ACLHash_BC"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_LOG();
CREATE TRIGGER FIAT_BC_LOG
    BEFORE INSERT OR UPDATE
    ON BLOCKCHAIN."Fiat_BC"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_LOG();
CREATE TRIGGER ORDER_BC_LOG
    BEFORE INSERT OR UPDATE
    ON BLOCKCHAIN."Order_BC"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_LOG();
CREATE TRIGGER ASSET_BC_LOG
    BEFORE INSERT OR UPDATE
    ON BLOCKCHAIN."Asset_BC"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_LOG();
CREATE TRIGGER AVERAGE_BLOCK_TIME_LOG
    BEFORE INSERT OR UPDATE
    ON BLOCKCHAIN."AverageBlockTime"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_LOG();
CREATE TRIGGER BLOCK_LOG
    BEFORE INSERT OR UPDATE
    ON BLOCKCHAIN."Block"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_LOG();
CREATE TRIGGER CLASSIFICATION_BC_LOG
    BEFORE INSERT OR UPDATE
    ON BLOCKCHAIN."Classification_BC"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_LOG();
CREATE TRIGGER DELEGATION_LOG
    BEFORE INSERT OR UPDATE
    ON BLOCKCHAIN."Delegation"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_LOG();
CREATE TRIGGER IDENTITY_PROPERTIES_BC_LOG
    BEFORE INSERT OR UPDATE
    ON BLOCKCHAIN."IdentityProperties_BC"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_LOG();
CREATE TRIGGER IDENTITY_PROVISIONED_BC_LOG
    BEFORE INSERT OR UPDATE
    ON BLOCKCHAIN."IdentityProvisioned_BC"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_LOG();
CREATE TRIGGER IDENTITY_UNPROVISIONED_BC_LOG
    BEFORE INSERT OR UPDATE
    ON BLOCKCHAIN."IdentityUnprovisioned_BC"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_LOG();
CREATE TRIGGER MAINTAINER_BC_LOG
    BEFORE INSERT OR UPDATE
    ON BLOCKCHAIN."Maintainer_BC"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_LOG();
CREATE TRIGGER META_BC_LOG
    BEFORE INSERT OR UPDATE
    ON BLOCKCHAIN."Meta_BC"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_LOG();
CREATE TRIGGER PARAMETER_LOG
    BEFORE INSERT OR UPDATE
    ON BLOCKCHAIN."Parameter"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_LOG();
CREATE TRIGGER REDELEGATION_LOG
    BEFORE INSERT OR UPDATE
    ON BLOCKCHAIN."Redelegation"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_LOG();
CREATE TRIGGER SPLIT_BC_LOG
    BEFORE INSERT OR UPDATE
    ON BLOCKCHAIN."Split_BC"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_LOG();
CREATE TRIGGER TOKEN_LOG
    BEFORE INSERT OR UPDATE
    ON BLOCKCHAIN."Token"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_LOG();
CREATE TRIGGER TRANSACTION_LOG
    BEFORE INSERT OR UPDATE
    ON BLOCKCHAIN."Transaction"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_LOG();
CREATE TRIGGER UNDELEGATION_LOG
    BEFORE INSERT OR UPDATE
    ON BLOCKCHAIN."Undelegation"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_LOG();
CREATE TRIGGER VALIDATOR_LOG
    BEFORE INSERT OR UPDATE
    ON BLOCKCHAIN."Validator"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_LOG();
CREATE TRIGGER WITHDRAW_ADDRESS_LOG
    BEFORE INSERT OR UPDATE
    ON BLOCKCHAIN."WithdrawAddress"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_LOG();

CREATE TRIGGER ISSUE_FIAT_LOG
    BEFORE INSERT OR UPDATE
    ON BLOCKCHAIN_TRANSACTION."IssueFiat"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_LOG();
CREATE TRIGGER REDEEM_FIAT_LOG
    BEFORE INSERT OR UPDATE
    ON BLOCKCHAIN_TRANSACTION."RedeemFiat"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_LOG();
CREATE TRIGGER SEND_COIN_LOG
    BEFORE INSERT OR UPDATE
    ON BLOCKCHAIN_TRANSACTION."SendCoin"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_LOG();
CREATE TRIGGER SEND_FIAT_LOG
    BEFORE INSERT OR UPDATE
    ON BLOCKCHAIN_TRANSACTION."SendFiat"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_LOG();
CREATE TRIGGER SET_ACL_LOG
    BEFORE INSERT OR UPDATE
    ON BLOCKCHAIN_TRANSACTION."SetACL"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_LOG();

CREATE TRIGGER ASSET_BURN_LOG
    BEFORE INSERT OR UPDATE
    ON BLOCKCHAIN_TRANSACTION."AssetBurn"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_LOG();
CREATE TRIGGER ASSET_DEFINE_LOG
    BEFORE INSERT OR UPDATE
    ON BLOCKCHAIN_TRANSACTION."AssetDefine"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_LOG();
CREATE TRIGGER ASSET_MINT_LOG
    BEFORE INSERT OR UPDATE
    ON BLOCKCHAIN_TRANSACTION."AssetMint"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_LOG();
CREATE TRIGGER ASSET_MUTATE_LOG
    BEFORE INSERT OR UPDATE
    ON BLOCKCHAIN_TRANSACTION."AssetMutate"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_LOG();
CREATE TRIGGER IDENTITY_DEFINE_LOG
    BEFORE INSERT OR UPDATE
    ON BLOCKCHAIN_TRANSACTION."IdentityDefine"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_LOG();
CREATE TRIGGER IDENTITY_ISSUE_LOG
    BEFORE INSERT OR UPDATE
    ON BLOCKCHAIN_TRANSACTION."IdentityIssue"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_LOG();
CREATE TRIGGER IDENTITY_NUB_LOG
    BEFORE INSERT OR UPDATE
    ON BLOCKCHAIN_TRANSACTION."IdentityNub"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_LOG();
CREATE TRIGGER IDENTITY_PROVISION_LOG
    BEFORE INSERT OR UPDATE
    ON BLOCKCHAIN_TRANSACTION."IdentityProvision"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_LOG();
CREATE TRIGGER IDENTITY_UNPROVISION_LOG
    BEFORE INSERT OR UPDATE
    ON BLOCKCHAIN_TRANSACTION."IdentityUnprovision"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_LOG();
CREATE TRIGGER MAINTAINER_DEPUTIZE_LOG
    BEFORE INSERT OR UPDATE
    ON BLOCKCHAIN_TRANSACTION."MaintainerDeputize"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_LOG();
CREATE TRIGGER META_REVEAL_LOG
    BEFORE INSERT OR UPDATE
    ON BLOCKCHAIN_TRANSACTION."MetaReveal"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_LOG();
CREATE TRIGGER ORDER_CANCEL_LOG
    BEFORE INSERT OR UPDATE
    ON BLOCKCHAIN_TRANSACTION."OrderCancel"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_LOG();
CREATE TRIGGER ORDER_DEFINE_LOG
    BEFORE INSERT OR UPDATE
    ON BLOCKCHAIN_TRANSACTION."OrderDefine"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_LOG();
CREATE TRIGGER ORDER_MAKE_LOG
    BEFORE INSERT OR UPDATE
    ON BLOCKCHAIN_TRANSACTION."OrderMake"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_LOG();
CREATE TRIGGER ORDER_TAKE_LOG
    BEFORE INSERT OR UPDATE
    ON BLOCKCHAIN_TRANSACTION."OrderTake"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_LOG();
CREATE TRIGGER SPLIT_SEND_LOG
    BEFORE INSERT OR UPDATE
    ON BLOCKCHAIN_TRANSACTION."SplitSend"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_LOG();
CREATE TRIGGER SPLIT_UNWRAP_LOG
    BEFORE INSERT OR UPDATE
    ON BLOCKCHAIN_TRANSACTION."SplitUnwrap"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_LOG();
CREATE TRIGGER SPLIT_WRAP_LOG
    BEFORE INSERT OR UPDATE
    ON BLOCKCHAIN_TRANSACTION."SplitWrap"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_LOG();


CREATE TRIGGER ENVELOPE_LOG
    BEFORE INSERT OR UPDATE
    ON DOCUSIGN."Envelope"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_LOG();
CREATE TRIGGER OAUTH_TOKEN_LOG
    BEFORE INSERT OR UPDATE
    ON DOCUSIGN."OAuthToken"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_LOG();

CREATE TRIGGER ACCOUNT_LOG
    BEFORE INSERT OR UPDATE
    ON MASTER."Account"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_LOG();
CREATE TRIGGER ACCOUNT_FILE_LOG
    BEFORE INSERT OR UPDATE
    ON MASTER."AccountFile"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_LOG();
CREATE TRIGGER ACCOUNT_KYC_LOG
    BEFORE INSERT OR UPDATE
    ON MASTER."AccountKYC"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_LOG();
CREATE TRIGGER ASSET_LOG
    BEFORE INSERT OR UPDATE
    ON MASTER."Asset"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_LOG();
CREATE TRIGGER EMAIL_LOG
    BEFORE INSERT OR UPDATE
    ON MASTER."Email"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_LOG();
CREATE TRIGGER FIAT_LOG
    BEFORE INSERT OR UPDATE
    ON MASTER."Fiat"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_LOG();
CREATE TRIGGER IDENTIFICATION_LOG
    BEFORE INSERT OR UPDATE
    ON MASTER."Identification"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_LOG();
CREATE TRIGGER MOBILE_LOG
    BEFORE INSERT OR UPDATE
    ON MASTER."Mobile"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_LOG();
CREATE TRIGGER NEGOTIATION_LOG
    BEFORE INSERT OR UPDATE
    ON MASTER."Negotiation"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_LOG();
CREATE TRIGGER ORDER_LOG
    BEFORE INSERT OR UPDATE
    ON MASTER."Order"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_LOG();
CREATE TRIGGER CLASSIFICATION_LOG
    BEFORE INSERT OR UPDATE
    ON MASTER."Classification"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_LOG();
CREATE TRIGGER IDENTITY_LOG
    BEFORE INSERT OR UPDATE
    ON MASTER."Identity"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_LOG();
CREATE TRIGGER ORGANIZATION_LOG
    BEFORE INSERT OR UPDATE
    ON MASTER."Organization"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_LOG();
CREATE TRIGGER ORGANIZATION_BANK_ACCOUNT_DETAIL_LOG
    BEFORE INSERT OR UPDATE
    ON MASTER."OrganizationBankAccountDetail"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_LOG();
CREATE TRIGGER ORGANIZATION_KYC_LOG
    BEFORE INSERT OR UPDATE
    ON MASTER."OrganizationKYC"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_LOG();
CREATE TRIGGER ORGANIZATION_UBO_LOG
    BEFORE INSERT OR UPDATE
    ON MASTER."OrganizationUBO"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_LOG();
CREATE TRIGGER TRADER_LOG
    BEFORE INSERT OR UPDATE
    ON MASTER."Trader"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_LOG();
CREATE TRIGGER TRADER_RELATION_LOG
    BEFORE INSERT OR UPDATE
    ON MASTER."TraderRelation"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_LOG();
CREATE TRIGGER ZONE_LOG
    BEFORE INSERT OR UPDATE
    ON MASTER."Zone"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_LOG();
CREATE TRIGGER ZONE_KYC_LOG
    BEFORE INSERT OR UPDATE
    ON MASTER."ZoneKYC"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_LOG();

CREATE TRIGGER ASSET_FILE_LOG
    BEFORE INSERT OR UPDATE
    ON MASTER_TRANSACTION."AssetFile"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_LOG();
CREATE TRIGGER CHAT_LOG
    BEFORE INSERT OR UPDATE
    ON MASTER_TRANSACTION."Chat"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_LOG();
CREATE TRIGGER EMAIL_OTP_LOG
    BEFORE INSERT OR UPDATE
    ON MASTER_TRANSACTION."EmailOTP"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_LOG();
CREATE TRIGGER FAUCET_REQUEST_LOG
    BEFORE INSERT OR UPDATE
    ON MASTER_TRANSACTION."FaucetRequest"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_LOG();
CREATE TRIGGER MESSAGE_LOG
    BEFORE INSERT OR UPDATE
    ON MASTER_TRANSACTION."Message"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_LOG();
CREATE TRIGGER MESSAGE_READ_LOG
    BEFORE INSERT OR UPDATE
    ON MASTER_TRANSACTION."MessageRead"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_LOG();
CREATE TRIGGER NEGOTIATION_FILE_LOG
    BEFORE INSERT OR UPDATE
    ON MASTER_TRANSACTION."NegotiationFile"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_LOG();
CREATE TRIGGER NOTIFICATION_LOG
    BEFORE INSERT OR UPDATE
    ON MASTER_TRANSACTION."Notification"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_LOG();
CREATE TRIGGER PUSH_NOTIFICATION_TOKEN_LOG
    BEFORE INSERT OR UPDATE
    ON MASTER_TRANSACTION."PushNotificationToken"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_LOG();
CREATE TRIGGER RECEIVE_FIAT_LOG
    BEFORE INSERT OR UPDATE
    ON MASTER_TRANSACTION."ReceiveFiat"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_LOG();
CREATE TRIGGER REDEEM_FIAT_REQUEST_LOG
    BEFORE INSERT OR UPDATE
    ON MASTER_TRANSACTION."RedeemFiatRequest"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_LOG();
CREATE TRIGGER SEND_FIAT_REQUEST_TOKEN_LOG
    BEFORE INSERT OR UPDATE
    ON MASTER_TRANSACTION."SendFiatRequest"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_LOG();
CREATE TRIGGER SESSION_TOKEN_LOG
    BEFORE INSERT OR UPDATE
    ON MASTER_TRANSACTION."SessionToken"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_LOG();
CREATE TRIGGER SMS_OTP_LOG
    BEFORE INSERT OR UPDATE
    ON MASTER_TRANSACTION."SMSOTP"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_LOG();
CREATE TRIGGER TRADE_ACTIVITY_LOG
    BEFORE INSERT OR UPDATE
    ON MASTER_TRANSACTION."TradeActivity"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_LOG();
CREATE TRIGGER TRADER_INVITATION_LOG
    BEFORE INSERT OR UPDATE
    ON MASTER_TRANSACTION."TraderInvitation"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_LOG();
CREATE TRIGGER ZONE_INVITATION_LOG
    BEFORE INSERT OR UPDATE
    ON MASTER_TRANSACTION."ZoneInvitation"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_LOG();

CREATE TRIGGER MEMBER_SCAN_LOG
    BEFORE INSERT OR UPDATE
    ON MEMBER_CHECK."MemberScan"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_LOG();
CREATE TRIGGER MEMBER_SCAN_DECISION_LOG
    BEFORE INSERT OR UPDATE
    ON MEMBER_CHECK."MemberScanDecision"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_LOG();
CREATE TRIGGER CORPORATE_SCAN_LOG
    BEFORE INSERT OR UPDATE
    ON MEMBER_CHECK."CorporateScan"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_LOG();
CREATE TRIGGER CORPORATE_SCAN_DECISION_LOG
    BEFORE INSERT OR UPDATE
    ON MEMBER_CHECK."CorporateScanDecision"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_LOG();
CREATE TRIGGER VESSEL_SCAN_LOG
    BEFORE INSERT OR UPDATE
    ON MEMBER_CHECK."VesselScan"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_LOG();
CREATE TRIGGER VESSEL_SCAN_DECISION_LOG
    BEFORE INSERT OR UPDATE
    ON MEMBER_CHECK."VesselScanDecision"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_LOG();

CREATE TRIGGER FIAT_REQUEST_LOG
    BEFORE INSERT OR UPDATE
    ON WESTERN_UNION."FiatRequest"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_LOG();
CREATE TRIGGER RTCB_LOG
    BEFORE INSERT OR UPDATE
    ON WESTERN_UNION."RTCB"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_LOG();
CREATE TRIGGER SFTP_FILE_TRANSACTION_LOG
    BEFORE INSERT OR UPDATE
    ON WESTERN_UNION."SFTPFileTransaction"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_LOG();

CREATE OR REPLACE FUNCTION DOCUSIGN.CREATE_ENVELOPE_HISTORY()
    RETURNS trigger
AS
$$
BEGIN
    INSERT INTO DOCUSIGN."Envelope_History"
    VALUES (old.*, CURRENT_USER, CURRENT_TIMESTAMP, CURRENT_SETTING('TIMEZONE'));;
    RETURN old;;
END ;;
$$ LANGUAGE PLPGSQL;

CREATE TRIGGER DELETE_ENVELOPE
    BEFORE DELETE
    ON DOCUSIGN."Envelope"
    FOR EACH ROW
EXECUTE PROCEDURE DOCUSIGN.CREATE_ENVELOPE_HISTORY();

CREATE OR REPLACE FUNCTION MASTER.CREATE_ASSET_HISTORY()
    RETURNS trigger
AS
$$
BEGIN
    INSERT INTO MASTER."Asset_History" VALUES (old.*, CURRENT_USER, CURRENT_TIMESTAMP, CURRENT_SETTING('TIMEZONE'));;
    RETURN old;;
END ;;
$$ LANGUAGE PLPGSQL;

CREATE TRIGGER DELETE_ASSET
    BEFORE DELETE
    ON MASTER."Asset"
    FOR EACH ROW
EXECUTE PROCEDURE MASTER.CREATE_ASSET_HISTORY();

CREATE OR REPLACE FUNCTION MASTER.CREATE_NEGOTIATION_HISTORY()
    RETURNS trigger
AS
$$
BEGIN
    INSERT INTO MASTER."Negotiation_History"
    VALUES (old.*, CURRENT_USER, CURRENT_TIMESTAMP, CURRENT_SETTING('TIMEZONE'));;
    RETURN old;;
END ;;
$$ LANGUAGE PLPGSQL;

CREATE TRIGGER DELETE_NEGOTIATION
    BEFORE DELETE
    ON MASTER."Negotiation"
    FOR EACH ROW
EXECUTE PROCEDURE MASTER.CREATE_NEGOTIATION_HISTORY();

CREATE OR REPLACE FUNCTION MASTER.CREATE_ORDER_HISTORY()
    RETURNS trigger
AS
$$
BEGIN
    INSERT INTO MASTER."Order_History"
    VALUES (old.*, CURRENT_USER, CURRENT_TIMESTAMP, CURRENT_SETTING('TIMEZONE'));;
    RETURN old;;
END ;;
$$ LANGUAGE PLPGSQL;

CREATE TRIGGER DELETE_ORDER
    BEFORE DELETE
    ON MASTER."Order"
    FOR EACH ROW
EXECUTE PROCEDURE MASTER.CREATE_ORDER_HISTORY();

CREATE OR REPLACE FUNCTION MASTER_TRANSACTION.CREATE_ASSET_FILE_HISTORY()
    RETURNS trigger
AS
$$
BEGIN
    INSERT INTO MASTER_TRANSACTION."AssetFile_History"
    VALUES (old.*, CURRENT_USER, CURRENT_TIMESTAMP, CURRENT_SETTING('TIMEZONE'));;
    RETURN old;;
END ;;
$$ LANGUAGE PLPGSQL;

CREATE TRIGGER DELETE_ASSET_FILE
    BEFORE DELETE
    ON MASTER_TRANSACTION."AssetFile"
    FOR EACH ROW
EXECUTE PROCEDURE MASTER_TRANSACTION.CREATE_ASSET_FILE_HISTORY();

CREATE OR REPLACE FUNCTION MASTER_TRANSACTION.CREATE_NEGOTIATION_FILE_HISTORY()
    RETURNS trigger
AS
$$
BEGIN
    INSERT INTO MASTER_TRANSACTION."NegotiationFile_History"
    VALUES (old.*, CURRENT_USER, CURRENT_TIMESTAMP, CURRENT_SETTING('TIMEZONE'));;
    RETURN old;;
END ;;
$$ LANGUAGE PLPGSQL;

CREATE TRIGGER DELETE_NEGOTIATION_FILE
    BEFORE DELETE
    ON MASTER_TRANSACTION."NegotiationFile"
    FOR EACH ROW
EXECUTE PROCEDURE MASTER_TRANSACTION.CREATE_NEGOTIATION_FILE_HISTORY();

CREATE OR REPLACE FUNCTION MASTER_TRANSACTION.CREATE_RECEIVE_FIAT_HISTORY()
    RETURNS trigger
AS
$$
BEGIN
    INSERT INTO MASTER_TRANSACTION."ReceiveFiat_History"
    VALUES (old.*, CURRENT_USER, CURRENT_TIMESTAMP, CURRENT_SETTING('TIMEZONE'));;
    RETURN old;;
END ;;
$$ LANGUAGE PLPGSQL;

CREATE TRIGGER DELETE_RECEIVE_FIAT
    BEFORE DELETE
    ON MASTER_TRANSACTION."ReceiveFiat"
    FOR EACH ROW
EXECUTE PROCEDURE MASTER_TRANSACTION.CREATE_RECEIVE_FIAT_HISTORY();

CREATE OR REPLACE FUNCTION MASTER_TRANSACTION.CREATE_SEND_FIAT_REQUEST_HISTORY()
    RETURNS trigger
AS
$$
BEGIN
    INSERT INTO MASTER_TRANSACTION."SendFiatRequest_History"
    VALUES (old.*, CURRENT_USER, CURRENT_TIMESTAMP, CURRENT_SETTING('TIMEZONE'));;
    RETURN old;;
END ;;
$$ LANGUAGE PLPGSQL;

CREATE TRIGGER DELETE_SEND_FIAT_REQUEST
    BEFORE DELETE
    ON MASTER_TRANSACTION."SendFiatRequest"
    FOR EACH ROW
EXECUTE PROCEDURE MASTER_TRANSACTION.CREATE_SEND_FIAT_REQUEST_HISTORY();

CREATE OR REPLACE FUNCTION MASTER_TRANSACTION.CREATE_TRADE_ACTIVITY_HISTORY()
    RETURNS trigger
AS
$$
BEGIN
    INSERT INTO MASTER_TRANSACTION."TradeActivity_History"
    VALUES (old.*, CURRENT_USER, CURRENT_TIMESTAMP, CURRENT_SETTING('TIMEZONE'));;
    RETURN old;;
END ;;
$$ LANGUAGE PLPGSQL;

CREATE TRIGGER DELETE_TRADE_ACTIVITY
    BEFORE DELETE
    ON MASTER_TRANSACTION."TradeActivity"
    FOR EACH ROW
EXECUTE PROCEDURE MASTER_TRANSACTION.CREATE_TRADE_ACTIVITY_HISTORY();

CREATE OR REPLACE FUNCTION MEMBER_CHECK.VESSEL_SCAN_DECISION_HISTORY()
    RETURNS trigger
AS
$$
BEGIN
    INSERT INTO MEMBER_CHECK."VesselScanDecision_History"
    VALUES (old.*, CURRENT_USER, CURRENT_TIMESTAMP, CURRENT_SETTING('TIMEZONE'));;
    RETURN old;;
END ;;
$$ LANGUAGE PLPGSQL;

CREATE TRIGGER DELETE_VESSEL_SCAN_DECISION
    BEFORE DELETE
    ON MEMBER_CHECK."VesselScanDecision"
    FOR EACH ROW
EXECUTE PROCEDURE MEMBER_CHECK.VESSEL_SCAN_DECISION_HISTORY();

# --- !Downs

/*Log Triggers*/
DROP TRIGGER IF EXISTS ACCOUNT_BC_LOG ON BLOCKCHAIN."Account_BC" CASCADE;
DROP TRIGGER IF EXISTS ACL_ACCOUNT_BC_LOG ON BLOCKCHAIN."ACLAccount_BC" CASCADE;
DROP TRIGGER IF EXISTS ACL_HASH_BC_LOG ON BLOCKCHAIN."ACLHash_BC" CASCADE;
DROP TRIGGER IF EXISTS ASSET_BC_LOG ON BLOCKCHAIN."Asset_BC" CASCADE;
DROP TRIGGER IF EXISTS AVERAGE_BLOCK_TIME_LOG ON BLOCKCHAIN."AverageBlockTime" CASCADE;
DROP TRIGGER IF EXISTS BLOCK_LOG ON BLOCKCHAIN."Block" CASCADE;
DROP TRIGGER IF EXISTS CLASSIFICATION_BC_LOG ON BLOCKCHAIN."Classification_BC" CASCADE;
DROP TRIGGER IF EXISTS DELEGATION_LOG ON BLOCKCHAIN."Delegation" CASCADE;
DROP TRIGGER IF EXISTS IDENTITY_PROPERTIES_BC_LOG ON BLOCKCHAIN."IdentityProperties_BC" CASCADE;
DROP TRIGGER IF EXISTS IDENTITY_PROVISIONED_BC_LOG ON BLOCKCHAIN."IdentityProvisioned_BC" CASCADE;
DROP TRIGGER IF EXISTS IDENTITY_UNPROVISIONED_BC_LOG ON BLOCKCHAIN."IdentityUnprovisioned_BC" CASCADE;
DROP TRIGGER IF EXISTS MAINTAINER_BC_LOG ON BLOCKCHAIN."Maintainer_BC" CASCADE;
DROP TRIGGER IF EXISTS META_BC_LOG ON BLOCKCHAIN."Meta_BC" CASCADE;
DROP TRIGGER IF EXISTS ORDER_BC_LOG ON BLOCKCHAIN."Order_BC" CASCADE;
DROP TRIGGER IF EXISTS PARAMETER_LOG ON BLOCKCHAIN."Parameter" CASCADE;
DROP TRIGGER IF EXISTS REDELEGATION_LOG ON BLOCKCHAIN."Redelegation" CASCADE;
DROP TRIGGER IF EXISTS SPLIT_BC_LOG ON BLOCKCHAIN."Split_BC" CASCADE;
DROP TRIGGER IF EXISTS TOKEN_LOG ON BLOCKCHAIN."Token" CASCADE;
DROP TRIGGER IF EXISTS TRANSACTION_LOG ON BLOCKCHAIN."Transaction" CASCADE;
DROP TRIGGER IF EXISTS UNDELEGATION_LOG ON BLOCKCHAIN."Undelegation" CASCADE;
DROP TRIGGER IF EXISTS VALIDATOR_LOG ON BLOCKCHAIN."Validator" CASCADE;
DROP TRIGGER IF EXISTS WITHDRAW_ADDRESS_LOG ON BLOCKCHAIN."WithdrawAddress" CASCADE;

DROP TRIGGER IF EXISTS ASSET_BURN_LOG ON BLOCKCHAIN_TRANSACTION."AssetBurn" CASCADE;
DROP TRIGGER IF EXISTS ASSET_DEFINE_LOG ON BLOCKCHAIN_TRANSACTION."AssetDefine" CASCADE;
DROP TRIGGER IF EXISTS ASSET_MINT_LOG ON BLOCKCHAIN_TRANSACTION."AssetMint" CASCADE;
DROP TRIGGER IF EXISTS ASSET_MUTATE_LOG ON BLOCKCHAIN_TRANSACTION."AssetMutate" CASCADE;
DROP TRIGGER IF EXISTS IDENTITY_DEFINE_LOG ON BLOCKCHAIN_TRANSACTION."IdentityDefine" CASCADE;
DROP TRIGGER IF EXISTS IDENTITY_ISSUE_LOG ON BLOCKCHAIN_TRANSACTION."IdentityIssue" CASCADE;
DROP TRIGGER IF EXISTS IDENTITY_NUB_LOG ON BLOCKCHAIN_TRANSACTION."IdentityNub" CASCADE;
DROP TRIGGER IF EXISTS IDENTITY_PROVISION_LOG ON BLOCKCHAIN_TRANSACTION."IdentityProvision" CASCADE;
DROP TRIGGER IF EXISTS IDENTITY_UNPROVISION_LOG ON BLOCKCHAIN_TRANSACTION."IdentityUnprovision" CASCADE;
DROP TRIGGER IF EXISTS MAINTAINER_DEPUTIZE_LOG ON BLOCKCHAIN_TRANSACTION."MaintainerDeputize" CASCADE;
DROP TRIGGER IF EXISTS META_REVEAL_LOG ON BLOCKCHAIN_TRANSACTION."MetaReveal" CASCADE;
DROP TRIGGER IF EXISTS ORDER_CANCEL_LOG ON BLOCKCHAIN_TRANSACTION."OrderCancel" CASCADE;
DROP TRIGGER IF EXISTS ORDER_DEFINE_LOG ON BLOCKCHAIN_TRANSACTION."OrderDefine" CASCADE;
DROP TRIGGER IF EXISTS ORDER_MAKE_LOG ON BLOCKCHAIN_TRANSACTION."OrderMake" CASCADE;
DROP TRIGGER IF EXISTS ORDER_TAKE_LOG ON BLOCKCHAIN_TRANSACTION."OrderTake" CASCADE;
DROP TRIGGER IF EXISTS SEND_COIN_LOG ON BLOCKCHAIN_TRANSACTION."SendCoin" CASCADE;
DROP TRIGGER IF EXISTS SPLIT_SEND_LOG ON BLOCKCHAIN_TRANSACTION."SplitSend" CASCADE;
DROP TRIGGER IF EXISTS SPLIT_UNWRAP_LOG ON BLOCKCHAIN_TRANSACTION."SplitUnwrap" CASCADE;
DROP TRIGGER IF EXISTS SPLIT_WRAP_LOG ON BLOCKCHAIN_TRANSACTION."SplitWrap" CASCADE;

DROP TRIGGER IF EXISTS ENVELOPE_LOG ON DOCUSIGN."Envelope" CASCADE;
DROP TRIGGER IF EXISTS OAUTH_TOKEN_LOG ON DOCUSIGN."OAuthToken" CASCADE;

DROP TRIGGER IF EXISTS VALIDATOR_ACCOUNT_LOG ON KEY_BASE."ValidatorAccount" CASCADE;

DROP TRIGGER IF EXISTS ACCOUNT_LOG ON MASTER."Account" CASCADE;
DROP TRIGGER IF EXISTS ACCOUNT_FILE_LOG ON MASTER."AccountFile" CASCADE;
DROP TRIGGER IF EXISTS ACCOUNT_KYC_LOG ON MASTER."AccountKYC" CASCADE;
DROP TRIGGER IF EXISTS ASSET_LOG ON MASTER."Asset" CASCADE;
DROP TRIGGER IF EXISTS CLASSIFICATION_LOG ON MASTER."Classification" CASCADE;
DROP TRIGGER IF EXISTS EMAIL_LOG ON MASTER."Email" CASCADE;
DROP TRIGGER IF EXISTS IDENTIFICATION_LOG ON MASTER."Identification" CASCADE;
DROP TRIGGER IF EXISTS IDENTITY_LOG ON MASTER."Identity" CASCADE;
DROP TRIGGER IF EXISTS MOBILE_LOG ON MASTER."Mobile" CASCADE;
DROP TRIGGER IF EXISTS NEGOTIATION_LOG ON MASTER."Negotiation" CASCADE;
DROP TRIGGER IF EXISTS ORDER_LOG ON MASTER."Order" CASCADE;
DROP TRIGGER IF EXISTS ORGANIZATION_LOG ON MASTER."Organization" CASCADE;
DROP TRIGGER IF EXISTS ORGANIZATION_BANK_ACCOUNT_DETAIL_LOG ON MASTER."OrganizationBankAccountDetail" CASCADE;
DROP TRIGGER IF EXISTS ORGANIZATION_KYC_LOG ON MASTER."OrganizationKYC" CASCADE;
DROP TRIGGER IF EXISTS ORGANIZATION_UBO_LOG ON MASTER."OrganizationUBO" CASCADE;
DROP TRIGGER IF EXISTS PROPERTY_LOG ON MASTER."Property" CASCADE;
DROP TRIGGER IF EXISTS SPLIT_LOG ON MASTER."Split" CASCADE;

DROP TRIGGER IF EXISTS ASSET_FILE_LOG ON MASTER_TRANSACTION."AssetFile" CASCADE;
DROP TRIGGER IF EXISTS CHAT_LOG ON MASTER_TRANSACTION."Chat" CASCADE;
DROP TRIGGER IF EXISTS EMAIL_OTP_LOG ON MASTER_TRANSACTION."EmailOTP" CASCADE;
DROP TRIGGER IF EXISTS FAUCET_REQUEST_LOG ON MASTER_TRANSACTION."FaucetRequest" CASCADE;
DROP TRIGGER IF EXISTS MESSAGE_LOG ON MASTER_TRANSACTION."Message" CASCADE;
DROP TRIGGER IF EXISTS MESSAGE_READ_LOG ON MASTER_TRANSACTION."MessageRead" CASCADE;
DROP TRIGGER IF EXISTS NEGOTIATION_FILE_LOG ON MASTER_TRANSACTION."NegotiationFile" CASCADE;
DROP TRIGGER IF EXISTS NOTIFICATION_LOG ON MASTER_TRANSACTION."Notification" CASCADE;
DROP TRIGGER IF EXISTS PUSH_NOTIFICATION_TOKEN_LOG ON MASTER_TRANSACTION."PushNotificationToken" CASCADE;
DROP TRIGGER IF EXISTS RECEIVE_FIAT_LOG ON MASTER_TRANSACTION."ReceiveFiat" CASCADE;
DROP TRIGGER IF EXISTS REDEEM_FIAT_REQUEST_LOG ON MASTER_TRANSACTION."RedeemFiatRequest" CASCADE;
DROP TRIGGER IF EXISTS SEND_FIAT_REQUEST_TOKEN_LOG ON MASTER_TRANSACTION."SendFiatRequest" CASCADE;
DROP TRIGGER IF EXISTS SESSION_TOKEN_LOG ON MASTER_TRANSACTION."SessionToken" CASCADE;
DROP TRIGGER IF EXISTS SMS_OTP_LOG ON MASTER_TRANSACTION."SMSOTP" CASCADE;
DROP TRIGGER IF EXISTS TOKEN_PRICE_LOG ON MASTER_TRANSACTION."TokenPrice" CASCADE;

DROP TRIGGER IF EXISTS MEMBER_SCAN_LOG ON MEMBER_CHECK."MemberScan" CASCADE;
DROP TRIGGER IF EXISTS MEMBER_SCAN_DECISION_LOG ON MEMBER_CHECK."MemberScanDecision" CASCADE;
DROP TRIGGER IF EXISTS CORPORATE_SCAN_LOG ON MEMBER_CHECK."CorporateScan" CASCADE;
DROP TRIGGER IF EXISTS CORPORATE_SCAN_DECISION_LOG ON MEMBER_CHECK."CorporateScanDecision" CASCADE;
DROP TRIGGER IF EXISTS VESSEL_SCAN_LOG ON MEMBER_CHECK."VesselScan" CASCADE;
DROP TRIGGER IF EXISTS VESSEL_SCAN_DECISION_LOG ON MEMBER_CHECK."VesselScanDecision" CASCADE;

DROP TRIGGER IF EXISTS FIAT_REQUEST_LOG ON WESTERN_UNION."FiatRequest" CASCADE;
DROP TRIGGER IF EXISTS RTCB_LOG ON WESTERN_UNION."RTCB" CASCADE;
DROP TRIGGER IF EXISTS SFTP_FILE_TRANSACTION_LOG ON WESTERN_UNION."SFTPFileTransaction" CASCADE;

/*Delete Triggers*/
DROP TRIGGER IF EXISTS DELETE_ENVELOPE ON DOCUSIGN."Envelope" CASCADE;

DROP TRIGGER IF EXISTS DELETE_ASSET ON MASTER."Asset" CASCADE;
DROP TRIGGER IF EXISTS DELETE_NEGOTIATION ON MASTER."Negotiation" CASCADE;
DROP TRIGGER IF EXISTS DELETE_ORDER ON MASTER."Order" CASCADE;

DROP TRIGGER IF EXISTS DELETE_ASSET_FILE ON MASTER_TRANSACTION."AssetFile" CASCADE;
DROP TRIGGER IF EXISTS DELETE_NEGOTIATION_FILE ON MASTER_TRANSACTION."NegotiationFile" CASCADE;
DROP TRIGGER IF EXISTS DELETE_RECEIVE_FIAT ON MASTER_TRANSACTION."ReceiveFiat" CASCADE;
DROP TRIGGER IF EXISTS DELETE_SEND_FIAT_REQUEST ON MASTER_TRANSACTION."SendFiatRequest" CASCADE;
DROP TRIGGER IF EXISTS DELETE_TRADE_ACTIVITY ON MASTER_TRANSACTION."TradeActivity" CASCADE;
DROP TRIGGER IF EXISTS DELETE_VESSEL_SCAN_DECISION ON MEMBER_CHECK."VesselScanDecision" CASCADE;

DROP TABLE IF EXISTS BLOCKCHAIN."Account_BC" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN."ACLAccount_BC" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN."ACLHash_BC" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN."Asset_BC" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN."AverageBlockTime" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN."Block" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN."Classification_BC" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN."Delegation" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN."IdentityProperties_BC" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN."IdentityProvisioned_BC" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN."IdentityUnprovisioned_BC" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN."Maintainer_BC" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN."Meta_BC" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN."Order_BC" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN."Parameter" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN."Redelegation" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN."Split_BC" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN."Token" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN."Transaction" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN."Undelegation" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN."Validator" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN."WithdrawAddress" CASCADE;

DROP TABLE IF EXISTS BLOCKCHAIN_TRANSACTION."AssetBurn" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN_TRANSACTION."AssetDefine" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN_TRANSACTION."AssetMint" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN_TRANSACTION."AssetMutate" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN_TRANSACTION."IdentityDefine" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN_TRANSACTION."IdentityIssue" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN_TRANSACTION."IdentityNub" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN_TRANSACTION."IdentityProvision" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN_TRANSACTION."IdentityUnprovision" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN_TRANSACTION."MaintainerDeputize" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN_TRANSACTION."MetaReveal" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN_TRANSACTION."OrderCancel" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN_TRANSACTION."OrderDefine" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN_TRANSACTION."OrderMake" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN_TRANSACTION."OrderTake" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN_TRANSACTION."SendCoin" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN_TRANSACTION."SplitSend" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN_TRANSACTION."SplitUnwrap" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN_TRANSACTION."SplitWrap" CASCADE;

DROP TABLE IF EXISTS DOCUSIGN."Envelope" CASCADE;
DROP TABLE IF EXISTS DOCUSIGN."Envelope_History" CASCADE;
DROP TABLE IF EXISTS DOCUSIGN."OAuthToken" CASCADE;

DROP TABLE IF EXISTS KEY_BASE."ValidatorAccount" CASCADE;

DROP TABLE IF EXISTS MASTER."Account" CASCADE;
DROP TABLE IF EXISTS MASTER."AccountFile" CASCADE;
DROP TABLE IF EXISTS MASTER."AccountKYC" CASCADE;
DROP TABLE IF EXISTS MASTER."Asset" CASCADE;
DROP TABLE IF EXISTS MASTER."Classification" CASCADE;
DROP TABLE IF EXISTS MASTER."Email" CASCADE;
DROP TABLE IF EXISTS MASTER."Fiat" CASCADE;
DROP TABLE IF EXISTS MASTER."Identification" CASCADE;
DROP TABLE IF EXISTS MASTER."Identity" CASCADE;
DROP TABLE IF EXISTS MASTER."Mobile" CASCADE;
DROP TABLE IF EXISTS MASTER."Negotiation" CASCADE;
DROP TABLE IF EXISTS MASTER."Negotiation_History" CASCADE;
DROP TABLE IF EXISTS MASTER."Order" CASCADE;
DROP TABLE IF EXISTS MASTER."Order_History" CASCADE;
DROP TABLE IF EXISTS MASTER."Organization" CASCADE;
DROP TABLE IF EXISTS MASTER."OrganizationBankAccountDetail" CASCADE;
DROP TABLE IF EXISTS MASTER."OrganizationKYC" CASCADE;
DROP TABLE IF EXISTS MASTER."OrganizationUBO" CASCADE;
DROP TABLE IF EXISTS MASTER."Property" CASCADE;
DROP TABLE IF EXISTS MASTER."Split" CASCADE;

DROP TABLE IF EXISTS MASTER_TRANSACTION."AssetFile" CASCADE;
DROP TABLE IF EXISTS MASTER_TRANSACTION."AssetFile_History" CASCADE;
DROP TABLE IF EXISTS MASTER_TRANSACTION."Chat" CASCADE;
DROP TABLE IF EXISTS MASTER_TRANSACTION."EmailOTP" CASCADE;
DROP TABLE IF EXISTS MASTER_TRANSACTION."FaucetRequest" CASCADE;
DROP TABLE IF EXISTS MASTER_TRANSACTION."Message" CASCADE;
DROP TABLE IF EXISTS MASTER_TRANSACTION."MessageRead" CASCADE;
DROP TABLE IF EXISTS MASTER_TRANSACTION."NegotiationFile" CASCADE;
DROP TABLE IF EXISTS MASTER_TRANSACTION."NegotiationFile_History" CASCADE;
DROP TABLE IF EXISTS MASTER_TRANSACTION."Notification" CASCADE;
DROP TABLE IF EXISTS MASTER_TRANSACTION."PushNotificationToken" CASCADE;
DROP TABLE IF EXISTS MASTER_TRANSACTION."ReceiveFiat" CASCADE;
DROP TABLE IF EXISTS MASTER_TRANSACTION."ReceiveFiat_History" CASCADE;
DROP TABLE IF EXISTS MASTER_TRANSACTION."RedeemFiatRequest" CASCADE;
DROP TABLE IF EXISTS MASTER_TRANSACTION."SendFiatRequest" CASCADE;
DROP TABLE IF EXISTS MASTER_TRANSACTION."SendFiatRequest_History" CASCADE;
DROP TABLE IF EXISTS MASTER_TRANSACTION."SessionToken" CASCADE;
DROP TABLE IF EXISTS MASTER_TRANSACTION."SMSOTP" CASCADE;
DROP TABLE IF EXISTS MASTER_TRANSACTION."TradeActivity" CASCADE;
DROP TABLE IF EXISTS MASTER_TRANSACTION."TradeActivity_History" CASCADE;
DROP TABLE IF EXISTS MASTER_TRANSACTION."TraderInvitation" CASCADE;
DROP TABLE IF EXISTS MASTER_TRANSACTION."ZoneInvitation" CASCADE;

DROP TABLE IF EXISTS WESTERN_UNION."FiatRequest" CASCADE;
DROP TABLE IF EXISTS WESTERN_UNION."RTCB" CASCADE;
DROP TABLE IF EXISTS WESTERN_UNION."SFTPFileTransaction" CASCADE;

DROP TABLE IF EXISTS MEMBER_CHECK."MemberScan" CASCADE;
DROP TABLE IF EXISTS MEMBER_CHECK."MemberScanDecision" CASCADE;
DROP TABLE IF EXISTS MEMBER_CHECK."CorporateScan" CASCADE;
DROP TABLE IF EXISTS MEMBER_CHECK."CorporateScanDecision" CASCADE;
DROP TABLE IF EXISTS MEMBER_CHECK."VesselScan" CASCADE;
DROP TABLE IF EXISTS MEMBER_CHECK."VesselScanDecision" CASCADE;
DROP TABLE IF EXISTS MEMBER_CHECK."VesselScanDecision_History" CASCADE;

DROP SCHEMA IF EXISTS BLOCKCHAIN CASCADE;
DROP SCHEMA IF EXISTS BLOCKCHAIN_TRANSACTION CASCADE;
DROP SCHEMA IF EXISTS DOCUSIGN CASCADE;
DROP SCHEMA IF EXISTS MASTER CASCADE;
DROP SCHEMA IF EXISTS MASTER_TRANSACTION CASCADE;
DROP SCHEMA IF EXISTS WESTERN_UNION CASCADE;
DROP SCHEMA IF EXISTS MEMBER_CHECK CASCADE;
