# --- !Ups

CREATE SCHEMA IF NOT EXISTS BLOCKCHAIN
    AUTHORIZATION "persistence";
CREATE SCHEMA IF NOT EXISTS BLOCKCHAIN_TRANSACTION
    AUTHORIZATION "persistence";
CREATE SCHEMA IF NOT EXISTS KEY_BASE
    AUTHORIZATION "persistence";
CREATE SCHEMA IF NOT EXISTS MASTER
    AUTHORIZATION "persistence";
CREATE SCHEMA IF NOT EXISTS MASTER_TRANSACTION
    AUTHORIZATION "persistence";
CREATE SCHEMA IF NOT EXISTS WESTERN_UNION
    AUTHORIZATION "persistence";
CREATE SCHEMA IF NOT EXISTS DOCUSIGN
    AUTHORIZATION "persistence";
CREATE SCHEMA IF NOT EXISTS MEMBER_CHECK
    AUTHORIZATION "persistence";

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

CREATE TABLE IF NOT EXISTS BLOCKCHAIN."SigningInfo"
(
    "consensusAddress"  VARCHAR NOT NULL,
    "startHeight"       INTEGER NOT NULL,
    "jailedUntil"       VARCHAR NOT NULL,
    "tombstoned"        BOOLEAN NOT NULL,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("consensusAddress")
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
    "ownerID"           VARCHAR NOT NULL,
    "status"            BOOLEAN,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS MASTER."Classification"
(
    "id"                VARCHAR NOT NULL,
    "entityType"        VARCHAR NOT NULL,
    "fromID"            VARCHAR NOT NULL,
    "label"             VARCHAR,
    "status"            BOOLEAN,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("id", "entityType")
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

CREATE TABLE IF NOT EXISTS MASTER."Property"
(
    "entityID"          VARCHAR NOT NULL,
    "entityType"        VARCHAR NOT NULL,
    "name"              VARCHAR NOT NULL,
    "value"             VARCHAR,
    "dataType"          VARCHAR NOT NULL,
    "isMeta"            BOOLEAN NOT NULL,
    "isRevealed"        BOOLEAN NOT NULL,
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


CREATE TABLE IF NOT EXISTS MASTER."Split"
(
    "entityID"          VARCHAR NOT NULL,
    "ownerID"           VARCHAR NOT NULL,
    "entityType"        VARCHAR NOT NULL,
    "label"             VARCHAR,
    "status"            BOOLEAN,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("entityID", "ownerID")
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

CREATE TABLE IF NOT EXISTS WESTERN_UNION."FiatRequest"
(
    "id"                VARCHAR NOT NULL,
    "traderID"          VARCHAR NOT NULL,
    "transactionAmount" INT     NOT NULL,
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
    "paidOutAmount"     INT       NOT NULL,
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

ALTER TABLE BLOCKCHAIN."Delegation"
    ADD CONSTRAINT Delegation_Validator_operatorAddress FOREIGN KEY ("validatorAddress") REFERENCES BLOCKCHAIN."Validator" ("operatorAddress");
ALTER TABLE BLOCKCHAIN."IdentityProvisioned_BC"
    ADD CONSTRAINT Identity_ID_Provisioned FOREIGN KEY ("id") REFERENCES BLOCKCHAIN."IdentityProperties_BC" ("id");
ALTER TABLE BLOCKCHAIN."IdentityUnprovisioned_BC"
    ADD CONSTRAINT Identity_ID_Unprovisioned FOREIGN KEY ("id") REFERENCES BLOCKCHAIN."IdentityProperties_BC" ("id");
ALTER TABLE BLOCKCHAIN."Redelegation"
    ADD CONSTRAINT Redelegation_Validator_validatorSourceAddress FOREIGN KEY ("validatorSourceAddress") REFERENCES BLOCKCHAIN."Validator" ("operatorAddress");
ALTER TABLE BLOCKCHAIN."Redelegation"
    ADD CONSTRAINT Redelegation_Validator_validatorDestinationAddress FOREIGN KEY ("validatorDestinationAddress") REFERENCES BLOCKCHAIN."Validator" ("operatorAddress");
ALTER TABLE BLOCKCHAIN."Transaction"
    ADD CONSTRAINT Transaction_Block_height FOREIGN KEY ("height") REFERENCES BLOCKCHAIN."Block" ("height");
ALTER TABLE BLOCKCHAIN."Undelegation"
    ADD CONSTRAINT Undelegation_Validator_validatorAddress FOREIGN KEY ("validatorAddress") REFERENCES BLOCKCHAIN."Validator" ("operatorAddress");

ALTER TABLE KEY_BASE."ValidatorAccount"
    ADD CONSTRAINT ValidatorAccount_Validator FOREIGN KEY ("address") REFERENCES BLOCKCHAIN."Validator" ("operatorAddress");

ALTER TABLE MASTER."AccountFile"
    ADD CONSTRAINT AccountFile_Account_id FOREIGN KEY ("id") REFERENCES MASTER."Account" ("id");
ALTER TABLE MASTER."AccountKYC"
    ADD CONSTRAINT AccountKYC_Account_id FOREIGN KEY ("id") REFERENCES MASTER."Account" ("id");
ALTER TABLE MASTER."Email"
    ADD CONSTRAINT Email_Account_id FOREIGN KEY ("id") REFERENCES MASTER."Account" ("id");
ALTER TABLE MASTER."Mobile"
    ADD CONSTRAINT Mobile_Account_id FOREIGN KEY ("id") REFERENCES MASTER."Account" ("id");
ALTER TABLE MASTER."Identification"
    ADD CONSTRAINT Identification_Account_id FOREIGN KEY ("accountID") REFERENCES MASTER."Account" ("id");
ALTER TABLE MASTER."Asset"
    ADD CONSTRAINT Asset_Owner_id FOREIGN KEY ("ownerID") REFERENCES MASTER."Identity" ("id");
ALTER TABLE MASTER."Order"
    ADD CONSTRAINT Order_Maker_id FOREIGN KEY ("makerID") REFERENCES MASTER."Identity" ("id");
ALTER TABLE MASTER."Split"
    ADD CONSTRAINT Split_Owner_id FOREIGN KEY ("ownerID") REFERENCES MASTER."Identity" ("id");

ALTER TABLE MASTER_TRANSACTION."Chat"
    ADD CONSTRAINT Chat_Account_accountID FOREIGN KEY ("accountID") REFERENCES MASTER."Account" ("id");
ALTER TABLE MASTER_TRANSACTION."Message"
    ADD CONSTRAINT Message_Chat_accountIDChatWindowID FOREIGN KEY ("fromAccountID", "chatID") REFERENCES MASTER_TRANSACTION."Chat" ("accountID", "id");
ALTER TABLE MASTER_TRANSACTION."Message"
    ADD CONSTRAINT Message_Message_replyToID FOREIGN KEY ("replyToID") REFERENCES MASTER_TRANSACTION."Message" ("id");
ALTER TABLE MASTER_TRANSACTION."MessageRead"
    ADD CONSTRAINT MessageRead_Message_messageID FOREIGN KEY ("messageID") REFERENCES MASTER_TRANSACTION."Message" ("id");
ALTER TABLE MASTER_TRANSACTION."EmailOTP"
    ADD CONSTRAINT EmailOTP_Account_id FOREIGN KEY ("id") REFERENCES MASTER."Account" ("id");
ALTER TABLE MASTER_TRANSACTION."Notification"
    ADD CONSTRAINT Notification_Account_id FOREIGN KEY ("accountID") REFERENCES MASTER."Account" ("id");
ALTER TABLE MASTER_TRANSACTION."PushNotificationToken"
    ADD CONSTRAINT PushNotificationToken_Account_id FOREIGN KEY ("id") REFERENCES MASTER."Account" ("id");
ALTER TABLE MASTER_TRANSACTION."SessionToken"
    ADD CONSTRAINT SessionToken_Account_id FOREIGN KEY ("id") REFERENCES MASTER."Account" ("id");
ALTER TABLE MASTER_TRANSACTION."SMSOTP"
    ADD CONSTRAINT SMSOTP_Account_id FOREIGN KEY ("id") REFERENCES MASTER."Account" ("id");

ALTER TABLE MEMBER_CHECK."MemberScanDecision"
    ADD CONSTRAINT MemberScanDecision_MemberScan_scanID FOREIGN KEY ("scanID") REFERENCES MEMBER_CHECK."MemberScan" ("scanID");
ALTER TABLE MEMBER_CHECK."MemberScanDecision"
    ADD CONSTRAINT MemberScanDecision_OrganizationUBO_uboID FOREIGN KEY ("id") REFERENCES MASTER."OrganizationUBO" ("id");
ALTER TABLE MEMBER_CHECK."CorporateScanDecision"
    ADD CONSTRAINT CorporateScanDecision_CorporateScan_scanID FOREIGN KEY ("scanID") REFERENCES MEMBER_CHECK."CorporateScan" ("scanID");
ALTER TABLE MEMBER_CHECK."VesselScanDecision"
    ADD CONSTRAINT VesselScanDecision_VesselScan_scanID FOREIGN KEY ("scanID") REFERENCES MEMBER_CHECK."VesselScan" ("scanID");

ALTER TABLE WESTERN_UNION."RTCB"
    ADD CONSTRAINT RTCB_FiatRequest_externalReference FOREIGN KEY ("externalReference") REFERENCES WESTERN_UNION."FiatRequest" ("id");

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
CREATE TRIGGER ORDER_BC_LOG
    BEFORE INSERT OR UPDATE
    ON BLOCKCHAIN."Order_BC"
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
CREATE TRIGGER SIGNING_INFO_LOG
    BEFORE INSERT OR UPDATE
    ON BLOCKCHAIN."SigningInfo"
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
CREATE TRIGGER SEND_COIN_LOG
    BEFORE INSERT OR UPDATE
    ON BLOCKCHAIN_TRANSACTION."SendCoin"
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
CREATE TRIGGER CLASSIFICATION_LOG
    BEFORE INSERT OR UPDATE
    ON MASTER."Classification"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_LOG();
CREATE TRIGGER EMAIL_LOG
    BEFORE INSERT OR UPDATE
    ON MASTER."Email"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_LOG();
CREATE TRIGGER IDENTIFICATION_LOG
    BEFORE INSERT OR UPDATE
    ON MASTER."Identification"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_LOG();
CREATE TRIGGER IDENTITY_LOG
    BEFORE INSERT OR UPDATE
    ON MASTER."Identity"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_LOG();
CREATE TRIGGER MOBILE_LOG
    BEFORE INSERT OR UPDATE
    ON MASTER."Mobile"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_LOG();
CREATE TRIGGER ORDER_LOG
    BEFORE INSERT OR UPDATE
    ON MASTER."Order"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_LOG();
CREATE TRIGGER ORGANIZATION_UBO_LOG
    BEFORE INSERT OR UPDATE
    ON MASTER."OrganizationUBO"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_LOG();
CREATE TRIGGER PROPERTY_LOG
    BEFORE INSERT OR UPDATE
    ON MASTER."Property"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_LOG();
CREATE TRIGGER SPLIT_LOG
    BEFORE INSERT OR UPDATE
    ON MASTER."Split"
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
CREATE TRIGGER TOKEN_PRICE
    BEFORE INSERT OR UPDATE
    ON MASTER_TRANSACTION."TokenPrice"
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

CREATE TRIGGER VALIDATOR_ACCOUNT_LOG
    BEFORE INSERT OR UPDATE
    ON KEY_BASE."ValidatorAccount"
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
DROP TRIGGER IF EXISTS SIGNING_INFO_LOG ON BLOCKCHAIN."SigningInfo" CASCADE;
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
DROP TRIGGER IF EXISTS ORDER_LOG ON MASTER."Order" CASCADE;
DROP TRIGGER IF EXISTS ORGANIZATION_UBO_LOG ON MASTER."OrganizationUBO" CASCADE;
DROP TRIGGER IF EXISTS PROPERTY_LOG ON MASTER."Property" CASCADE;
DROP TRIGGER IF EXISTS SPLIT_LOG ON MASTER."Split" CASCADE;

DROP TRIGGER IF EXISTS CHAT_LOG ON MASTER_TRANSACTION."Chat" CASCADE;
DROP TRIGGER IF EXISTS EMAIL_OTP_LOG ON MASTER_TRANSACTION."EmailOTP" CASCADE;
DROP TRIGGER IF EXISTS MESSAGE_LOG ON MASTER_TRANSACTION."Message" CASCADE;
DROP TRIGGER IF EXISTS MESSAGE_READ_LOG ON MASTER_TRANSACTION."MessageRead" CASCADE;
DROP TRIGGER IF EXISTS NOTIFICATION_LOG ON MASTER_TRANSACTION."Notification" CASCADE;
DROP TRIGGER IF EXISTS PUSH_NOTIFICATION_TOKEN_LOG ON MASTER_TRANSACTION."PushNotificationToken" CASCADE;
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

DROP TRIGGER IF EXISTS DELETE_VESSEL_SCAN_DECISION ON MEMBER_CHECK."VesselScanDecision" CASCADE;

DROP TABLE IF EXISTS BLOCKCHAIN."Account_BC" CASCADE;
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
DROP TABLE IF EXISTS BLOCKCHAIN."SigningInfo" CASCADE;
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
DROP TABLE IF EXISTS MASTER."Identification" CASCADE;
DROP TABLE IF EXISTS MASTER."Identity" CASCADE;
DROP TABLE IF EXISTS MASTER."Mobile" CASCADE;
DROP TABLE IF EXISTS MASTER."Order" CASCADE;
DROP TABLE IF EXISTS MASTER."OrganizationUBO" CASCADE;
DROP TABLE IF EXISTS MASTER."Property" CASCADE;
DROP TABLE IF EXISTS MASTER."Split" CASCADE;

DROP TABLE IF EXISTS MASTER_TRANSACTION."Chat" CASCADE;
DROP TABLE IF EXISTS MASTER_TRANSACTION."EmailOTP" CASCADE;
DROP TABLE IF EXISTS MASTER_TRANSACTION."Message" CASCADE;
DROP TABLE IF EXISTS MASTER_TRANSACTION."MessageRead" CASCADE;
DROP TABLE IF EXISTS MASTER_TRANSACTION."Notification" CASCADE;
DROP TABLE IF EXISTS MASTER_TRANSACTION."PushNotificationToken" CASCADE;
DROP TABLE IF EXISTS MASTER_TRANSACTION."SessionToken" CASCADE;
DROP TABLE IF EXISTS MASTER_TRANSACTION."SMSOTP" CASCADE;
DROP TABLE IF EXISTS MASTER_TRANSACTION."TokenPrice" CASCADE;

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
DROP SCHEMA IF EXISTS KEY_BASE CASCADE;
DROP SCHEMA IF EXISTS MASTER CASCADE;
DROP SCHEMA IF EXISTS MASTER_TRANSACTION CASCADE;
DROP SCHEMA IF EXISTS WESTERN_UNION CASCADE;
DROP SCHEMA IF EXISTS MEMBER_CHECK CASCADE;
