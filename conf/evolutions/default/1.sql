# --- !Ups

CREATE SCHEMA IF NOT EXISTS ANALYTICS
    AUTHORIZATION "assetMantle";
CREATE SCHEMA IF NOT EXISTS BLOCKCHAIN
    AUTHORIZATION "assetMantle";
CREATE SCHEMA IF NOT EXISTS KEY_BASE
    AUTHORIZATION "assetMantle";
CREATE SCHEMA IF NOT EXISTS MASTER_TRANSACTION
    AUTHORIZATION "assetMantle";

CREATE TABLE IF NOT EXISTS ANALYTICS."TransactionCounter"
(
    "epoch"                BIGINT  NOT NULL,
    "totalTxs"             INTEGER NOT NULL,
    "createdBy"            VARCHAR,
    "createdOnMillisEpoch" BIGINT,
    "updatedBy"            VARCHAR,
    "updatedOnMillisEpoch" BIGINT,
    PRIMARY KEY ("epoch")
);

CREATE TABLE IF NOT EXISTS ANALYTICS."MessageCounter"
(
    "messageType"          VARCHAR NOT NULL,
    "counter"              INTEGER NOT NULL,
    "createdBy"            VARCHAR,
    "createdOnMillisEpoch" BIGINT,
    "updatedBy"            VARCHAR,
    "updatedOnMillisEpoch" BIGINT,
    PRIMARY KEY ("messageType")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN."Account"
(
    "address"              VARCHAR NOT NULL,
    "accountType"          VARCHAR NOT NULL,
    "accountNumber"        INTEGER NOT NULL,
    "sequence"             INTEGER NOT NULL,
    "vestingParameters"    VARCHAR,
    "publicKey"            BYTEA,
    "publicKeyType"        VARCHAR,
    "createdBy"            VARCHAR,
    "createdOnMillisEpoch" BIGINT,
    "updatedBy"            VARCHAR,
    "updatedOnMillisEpoch" BIGINT,
    PRIMARY KEY ("address")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN."Authorization"
(
    "granter"              VARCHAR NOT NULL,
    "grantee"              VARCHAR NOT NULL,
    "msgTypeURL"           VARCHAR NOT NULL,
    "grantedAuthorization" BYTEA   NOT NULL,
    "expiration"           BIGINT  NOT NULL,
    "createdBy"            VARCHAR,
    "createdOnMillisEpoch" BIGINT,
    "updatedBy"            VARCHAR,
    "updatedOnMillisEpoch" BIGINT,
    PRIMARY KEY ("granter", "grantee", "msgTypeURL")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN."Asset"
(
    "id"                   BYTEA NOT NULL,
    "classificationID"     BYTEA NOT NULL,
    "immutables"           BYTEA NOT NULL,
    "mutables"             BYTEA NOT NULL,
    "createdBy"            VARCHAR,
    "createdOnMillisEpoch" BIGINT,
    "updatedBy"            VARCHAR,
    "updatedOnMillisEpoch" BIGINT,
    PRIMARY KEY ("id"),
    UNIQUE ("id", "classificationID")
);


CREATE TABLE IF NOT EXISTS BLOCKCHAIN."Balance"
(
    "address"              VARCHAR NOT NULL,
    "coins"                VARCHAR NOT NULL,
    "createdBy"            VARCHAR,
    "createdOnMillisEpoch" BIGINT,
    "updatedBy"            VARCHAR,
    "updatedOnMillisEpoch" BIGINT,
    PRIMARY KEY ("address")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN."Block"
(
    "height"               INTEGER NOT NULL,
    "time"                 BIGINT  NOT NULL,
    "proposerAddress"      VARCHAR NOT NULL,
    "validators"           VARCHAR NOT NULL,
    "createdBy"            VARCHAR,
    "createdOnMillisEpoch" BIGINT,
    "updatedBy"            VARCHAR,
    "updatedOnMillisEpoch" BIGINT,
    PRIMARY KEY ("height")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN."Classification"
(
    "id"                   BYTEA NOT NULL,
    "immutables"           BYTEA NOT NULL,
    "mutables"             BYTEA NOT NULL,
    "createdBy"            VARCHAR,
    "createdOnMillisEpoch" BIGINT,
    "updatedBy"            VARCHAR,
    "updatedOnMillisEpoch" BIGINT,
    PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN."Delegation"
(
    "delegatorAddress"     VARCHAR NOT NULL,
    "validatorAddress"     VARCHAR NOT NULL,
    "shares"               NUMERIC NOT NULL,
    "createdBy"            VARCHAR,
    "createdOnMillisEpoch" BIGINT,
    "updatedBy"            VARCHAR,
    "updatedOnMillisEpoch" BIGINT,
    PRIMARY KEY ("delegatorAddress", "validatorAddress")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN."FeeGrant"
(
    "granter"              VARCHAR NOT NULL,
    "grantee"              VARCHAR NOT NULL,
    "allowance"            BYTEA   NOT NULL,
    "createdBy"            VARCHAR,
    "createdOnMillisEpoch" BIGINT,
    "updatedBy"            VARCHAR,
    "updatedOnMillisEpoch" BIGINT,
    PRIMARY KEY ("granter", "grantee")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN."Identity"
(
    "id"                   BYTEA NOT NULL,
    "classificationID"     BYTEA NOT NULL,
    "immutables"           BYTEA NOT NULL,
    "mutables"             BYTEA NOT NULL,
    "createdBy"            VARCHAR,
    "createdOnMillisEpoch" BIGINT,
    "updatedBy"            VARCHAR,
    "updatedOnMillisEpoch" BIGINT,
    PRIMARY KEY ("id"),
    UNIQUE ("id", "classificationID")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN."Maintainer"
(
    "id"                   BYTEA NOT NULL,
    "classificationID"     BYTEA NOT NULL,
    "immutables"           BYTEA NOT NULL,
    "mutables"             BYTEA NOT NULL,
    "createdBy"            VARCHAR,
    "createdOnMillisEpoch" BIGINT,
    "updatedBy"            VARCHAR,
    "updatedOnMillisEpoch" BIGINT,
    PRIMARY KEY ("id"),
    UNIQUE ("id", "classificationID")
);


CREATE TABLE IF NOT EXISTS BLOCKCHAIN."MetaData"
(
    "dataTypeID"           VARCHAR NOT NULL,
    "dataHashID"           BYTEA   NOT NULL,
    "dataBytes"            BYTEA   NOT NULL,
    "createdBy"            VARCHAR,
    "createdOnMillisEpoch" BIGINT,
    "updatedBy"            VARCHAR,
    "updatedOnMillisEpoch" BIGINT,
    PRIMARY KEY ("dataTypeID", "dataHashID")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN."Order"
(
    "id"                   BYTEA NOT NULL,
    "classificationID"     BYTEA NOT NULL,
    "immutables"           BYTEA NOT NULL,
    "mutables"             BYTEA NOT NULL,
    "createdBy"            VARCHAR,
    "createdOnMillisEpoch" BIGINT,
    "updatedBy"            VARCHAR,
    "updatedOnMillisEpoch" BIGINT,
    PRIMARY KEY ("id"),
    UNIQUE ("id", "classificationID")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN."Parameter"
(
    "parameterType"        VARCHAR NOT NULL,
    "value"                VARCHAR NOT NULL,
    "createdBy"            VARCHAR,
    "createdOnMillisEpoch" BIGINT,
    "updatedBy"            VARCHAR,
    "updatedOnMillisEpoch" BIGINT,
    PRIMARY KEY ("parameterType")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN."Proposal"
(
    "id"                   INTEGER NOT NULL,
    "content"              BYTEA   NOT NULL,
    "status"               VARCHAR NOT NULL,
    "finalTallyResult"     VARCHAR NOT NULL,
    "submitTime"           BIGINT  NOT NULL,
    "depositEndTime"       BIGINT  NOT NULL,
    "totalDeposit"         VARCHAR NOT NULL,
    "votingStartTime"      BIGINT  NOT NULL,
    "votingEndTime"        BIGINT  NOT NULL,
    "createdBy"            VARCHAR,
    "createdOnMillisEpoch" BIGINT,
    "updatedBy"            VARCHAR,
    "updatedOnMillisEpoch" BIGINT,
    PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN."ProposalDeposit"
(
    "proposalID"           INTEGER NOT NULL,
    "depositor"            VARCHAR NOT NULL,
    "amount"               VARCHAR NOT NULL,
    "createdBy"            VARCHAR,
    "createdOnMillisEpoch" BIGINT,
    "updatedBy"            VARCHAR,
    "updatedOnMillisEpoch" BIGINT,
    PRIMARY KEY ("proposalID", "depositor")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN."ProposalVote"
(
    "proposalID"           INTEGER NOT NULL,
    "voter"                VARCHAR NOT NULL,
    "option"               VARCHAR NOT NULL,
    "weight"               NUMERIC NOT NULL,
    "createdBy"            VARCHAR,
    "createdOnMillisEpoch" BIGINT,
    "updatedBy"            VARCHAR,
    "updatedOnMillisEpoch" BIGINT,
    PRIMARY KEY ("proposalID", "voter")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN."Redelegation"
(
    "delegatorAddress"            VARCHAR NOT NULL,
    "validatorSourceAddress"      VARCHAR NOT NULL,
    "validatorDestinationAddress" VARCHAR NOT NULL,
    "entries"                     VARCHAR NOT NULL,
    "createdBy"                   VARCHAR,
    "createdOnMillisEpoch"        BIGINT,
    "updatedBy"                   VARCHAR,
    "updatedOnMillisEpoch"        BIGINT,
    PRIMARY KEY ("delegatorAddress", "validatorSourceAddress", "validatorDestinationAddress")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN."Split"
(
    "ownerID"              BYTEA   NOT NULL,
    "ownableID"            BYTEA   NOT NULL,
    "value"                NUMERIC NOT NULL,
    "createdBy"            VARCHAR,
    "createdOnMillisEpoch" BIGINT,
    "updatedBy"            VARCHAR,
    "updatedOnMillisEpoch" BIGINT,
    PRIMARY KEY ("ownerID", "ownableID")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN."Token"
(
    "denom"                VARCHAR NOT NULL,
    "totalSupply"          NUMERIC NOT NULL,
    "bondedAmount"         NUMERIC NOT NULL,
    "notBondedAmount"      NUMERIC NOT NULL,
    "communityPool"        NUMERIC NOT NULL,
    "inflation"            NUMERIC NOT NULL,
    "createdBy"            VARCHAR,
    "createdOnMillisEpoch" BIGINT,
    "updatedBy"            VARCHAR,
    "updatedOnMillisEpoch" BIGINT,

    PRIMARY KEY ("denom")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN."Transaction"
(
    "hash"                 VARCHAR NOT NULL,
    "height"               INTEGER NOT NULL,
    "code"                 INTEGER NOT NULL,
    "log"                  VARCHAR NOT NULL,
    "gasWanted"            VARCHAR NOT NULL,
    "gasUsed"              VARCHAR NOT NULL,
    "txBytes"              BYTEA   NOT NULL,
    "createdBy"            VARCHAR,
    "createdOnMillisEpoch" BIGINT,
    "updatedBy"            VARCHAR,
    "updatedOnMillisEpoch" BIGINT,
    PRIMARY KEY ("hash")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN."Undelegation"
(
    "delegatorAddress"     VARCHAR NOT NULL,
    "validatorAddress"     VARCHAR NOT NULL,
    "entries"              VARCHAR NOT NULL,
    "createdBy"            VARCHAR,
    "createdOnMillisEpoch" BIGINT,
    "updatedBy"            VARCHAR,
    "updatedOnMillisEpoch" BIGINT,
    PRIMARY KEY ("delegatorAddress", "validatorAddress")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN."Validator"
(
    "operatorAddress"       VARCHAR NOT NULL,
    "hexAddress"            VARCHAR NOT NULL UNIQUE,
    "jailed"                BOOLEAN NOT NULL,
    "status"                VARCHAR NOT NULL,
    "tokens"                NUMERIC NOT NULL,
    "delegatorShares"       NUMERIC NOT NULL,
    "description"           VARCHAR NOT NULL,
    "unbondingHeight"       INTEGER NOT NULL,
    "unbondingTime"         VARCHAR NOT NULL,
    "commission"            VARCHAR NOT NULL,
    "minimumSelfDelegation" VARCHAR NOT NULL,
    "createdBy"             VARCHAR,
    "createdOnMillisEpoch"  BIGINT,
    "updatedBy"             VARCHAR,
    "updatedOnMillisEpoch"  BIGINT,
    PRIMARY KEY ("operatorAddress")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN."WithdrawAddress"
(
    "delegatorAddress"     VARCHAR NOT NULL,
    "withdrawAddress"      VARCHAR NOT NULL,
    "createdBy"            VARCHAR,
    "createdOnMillisEpoch" BIGINT,
    "updatedBy"            VARCHAR,
    "updatedOnMillisEpoch" BIGINT,

    PRIMARY KEY ("delegatorAddress")
);

CREATE TABLE IF NOT EXISTS KEY_BASE."ValidatorAccount"
(
    "address"              VARCHAR NOT NULL,
    "identity"             VARCHAR,
    "username"             VARCHAR,
    "pictureURL"           VARCHAR,
    "createdBy"            VARCHAR,
    "createdOnMillisEpoch" BIGINT,
    "updatedBy"            VARCHAR,
    "updatedOnMillisEpoch" BIGINT,

    PRIMARY KEY ("address")
);

CREATE TABLE IF NOT EXISTS MASTER_TRANSACTION."Notification"
(
    "id"                       VARCHAR NOT NULL,
    "notificationTemplateJson" VARCHAR NOT NULL,
    "jsRoute"                  VARCHAR,
    "read"                     BOOLEAN NOT NULL,
    "createdBy"                VARCHAR,
    "createdOnMillisEpoch"     BIGINT,
    "updatedBy"                VARCHAR,
    "updatedOnMillisEpoch"     BIGINT,
    PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS MASTER_TRANSACTION."TokenPrice"
(
    "serial"               SERIAL  NOT NULL,
    "denom"                VARCHAR NOT NULL,
    "price"                NUMERIC NOT NULL,
    "createdBy"            VARCHAR,
    "createdOnMillisEpoch" BIGINT,
    "updatedBy"            VARCHAR,
    "updatedOnMillisEpoch" BIGINT,
    PRIMARY KEY ("serial")
);

CREATE TABLE IF NOT EXISTS MASTER_TRANSACTION."ValidatorTransaction"
(
    "address"              VARCHAR NOT NULL,
    "txHash"               VARCHAR NOT NULL,
    "height"               INTEGER,
    "createdBy"            VARCHAR,
    "createdOnMillisEpoch" BIGINT,
    "updatedBy"            VARCHAR,
    "updatedOnMillisEpoch" BIGINT,
    PRIMARY KEY ("address", "txHash")
);

CREATE TABLE IF NOT EXISTS MASTER_TRANSACTION."WalletTransaction"
(
    "address"              VARCHAR NOT NULL,
    "txHash"               VARCHAR NOT NULL,
    "height"               INTEGER,
    "createdBy"            VARCHAR,
    "createdOnMillisEpoch" BIGINT,
    "updatedBy"            VARCHAR,
    "updatedOnMillisEpoch" BIGINT,
    PRIMARY KEY ("address", "txHash")
);

ALTER TABLE BLOCKCHAIN."Asset"
    ADD CONSTRAINT Asset_ClassificationID FOREIGN KEY ("classificationID") REFERENCES BLOCKCHAIN."Classification" ("id");
ALTER TABLE BLOCKCHAIN."Delegation"
    ADD CONSTRAINT Delegation_Validator_operatorAddress FOREIGN KEY ("validatorAddress") REFERENCES BLOCKCHAIN."Validator" ("operatorAddress");
ALTER TABLE BLOCKCHAIN."Identity"
    ADD CONSTRAINT Identity_ClassificationID FOREIGN KEY ("classificationID") REFERENCES BLOCKCHAIN."Classification" ("id");
ALTER TABLE BLOCKCHAIN."Maintainer"
    ADD CONSTRAINT Maintainer_ClassificationID FOREIGN KEY ("classificationID") REFERENCES BLOCKCHAIN."Classification" ("id");
ALTER TABLE BLOCKCHAIN."Order"
    ADD CONSTRAINT Order_ClassificationID FOREIGN KEY ("classificationID") REFERENCES BLOCKCHAIN."Classification" ("id");
ALTER TABLE BLOCKCHAIN."ProposalDeposit"
    ADD CONSTRAINT ProposalDeposit_Proposal_ID FOREIGN KEY ("proposalID") REFERENCES BLOCKCHAIN."Proposal" ("id");
ALTER TABLE BLOCKCHAIN."ProposalVote"
    ADD CONSTRAINT ProposalVote_Proposal_ID FOREIGN KEY ("proposalID") REFERENCES BLOCKCHAIN."Proposal" ("id");
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

/*Triggers*/

CREATE OR REPLACE FUNCTION PUBLIC.INSERT_OR_UPDATE_EPOCH_LOG() RETURNS TRIGGER AS
$$
BEGIN
    IF (TG_OP = 'INSERT') THEN
        new."createdOnMillisEpoch" = FLOOR(EXTRACT(EPOCH FROM NOW()) * 1000);;
        new."createdBy" = CURRENT_USER;;
    ELSEIF (TG_OP = 'UPDATE') THEN
--         values of created needs to be set here otherwise insertOrUpdate of slick will omit created details
        new."createdOnMillisEpoch" = old."createdOnMillisEpoch";;
        new."createdBy" = old."createdBy";;
        new."updatedOnMillisEpoch" = FLOOR(EXTRACT(EPOCH FROM NOW()) * 1000);;
        new."updatedBy" = CURRENT_USER;;
    END IF;;
    RETURN NEW;;
END;;
$$ LANGUAGE PLPGSQL;

CREATE TRIGGER TRANSACTION_COUNTER_LOG
    BEFORE INSERT OR UPDATE
    ON ANALYTICS."TransactionCounter"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_EPOCH_LOG();
CREATE TRIGGER MESSAGE_COUNTER_LOG
    BEFORE INSERT OR UPDATE
    ON ANALYTICS."MessageCounter"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_EPOCH_LOG();

CREATE TRIGGER ACCOUNT_LOG
    BEFORE INSERT OR UPDATE
    ON BLOCKCHAIN."Account"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_EPOCH_LOG();
CREATE TRIGGER AUTHORIZATION_LOG
    BEFORE INSERT OR UPDATE
    ON BLOCKCHAIN."Authorization"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_EPOCH_LOG();
CREATE TRIGGER ASSET_LOG
    BEFORE INSERT OR UPDATE
    ON BLOCKCHAIN."Asset"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_EPOCH_LOG();
CREATE TRIGGER BALANCE_LOG
    BEFORE INSERT OR UPDATE
    ON BLOCKCHAIN."Balance"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_EPOCH_LOG();
CREATE TRIGGER BLOCK_LOG
    BEFORE INSERT OR UPDATE
    ON BLOCKCHAIN."Block"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_EPOCH_LOG();
CREATE TRIGGER CLASSIFICATION_LOG
    BEFORE INSERT OR UPDATE
    ON BLOCKCHAIN."Classification"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_EPOCH_LOG();
CREATE TRIGGER IDENTITY_LOG
    BEFORE INSERT OR UPDATE
    ON BLOCKCHAIN."Identity"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_EPOCH_LOG();
CREATE TRIGGER DELEGATION_LOG
    BEFORE INSERT OR UPDATE
    ON BLOCKCHAIN."Delegation"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_EPOCH_LOG();
CREATE TRIGGER FEE_GRANT_LOG
    BEFORE INSERT OR UPDATE
    ON BLOCKCHAIN."FeeGrant"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_EPOCH_LOG();
CREATE TRIGGER MAINTAINER_LOG
    BEFORE INSERT OR UPDATE
    ON BLOCKCHAIN."Maintainer"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_EPOCH_LOG();
CREATE TRIGGER META_DATA_LOG
    BEFORE INSERT OR UPDATE
    ON BLOCKCHAIN."MetaData"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_EPOCH_LOG();
CREATE TRIGGER ORDER_LOG
    BEFORE INSERT OR UPDATE
    ON BLOCKCHAIN."Order"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_EPOCH_LOG();
CREATE TRIGGER PARAMETER_LOG
    BEFORE INSERT OR UPDATE
    ON BLOCKCHAIN."Parameter"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_EPOCH_LOG();
CREATE TRIGGER PROPOSAL_LOG
    BEFORE INSERT OR UPDATE
    ON BLOCKCHAIN."Proposal"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_EPOCH_LOG();
CREATE TRIGGER PROPOSAL_DEPOSIT_LOG
    BEFORE INSERT OR UPDATE
    ON BLOCKCHAIN."ProposalDeposit"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_EPOCH_LOG();
CREATE TRIGGER PROPOSAL_VOTE_LOG
    BEFORE INSERT OR UPDATE
    ON BLOCKCHAIN."ProposalVote"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_EPOCH_LOG();
CREATE TRIGGER REDELEGATION_LOG
    BEFORE INSERT OR UPDATE
    ON BLOCKCHAIN."Redelegation"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_EPOCH_LOG();
CREATE TRIGGER SPLIT_LOG
    BEFORE INSERT OR UPDATE
    ON BLOCKCHAIN."Split"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_EPOCH_LOG();
CREATE TRIGGER TOKEN_LOG
    BEFORE INSERT OR UPDATE
    ON BLOCKCHAIN."Token"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_EPOCH_LOG();
CREATE TRIGGER TRANSACTION_LOG
    BEFORE INSERT OR UPDATE
    ON BLOCKCHAIN."Transaction"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_EPOCH_LOG();
CREATE TRIGGER UNDELEGATION_LOG
    BEFORE INSERT OR UPDATE
    ON BLOCKCHAIN."Undelegation"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_EPOCH_LOG();
CREATE TRIGGER VALIDATOR_LOG
    BEFORE INSERT OR UPDATE
    ON BLOCKCHAIN."Validator"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_EPOCH_LOG();
CREATE TRIGGER WITHDRAW_ADDRESS_LOG
    BEFORE INSERT OR UPDATE
    ON BLOCKCHAIN."WithdrawAddress"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_EPOCH_LOG();

CREATE TRIGGER WALLET_TRANSACTION_LOG
    BEFORE INSERT OR UPDATE
    ON MASTER_TRANSACTION."WalletTransaction"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_EPOCH_LOG();
CREATE TRIGGER VALIDATOR_TRANSACTION_LOG
    BEFORE INSERT OR UPDATE
    ON MASTER_TRANSACTION."ValidatorTransaction"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_EPOCH_LOG();

CREATE TRIGGER NOTIFICATION_LOG
    BEFORE INSERT OR UPDATE
    ON MASTER_TRANSACTION."Notification"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_EPOCH_LOG();
CREATE TRIGGER TOKEN_PRICE_LOG
    BEFORE INSERT OR UPDATE
    ON MASTER_TRANSACTION."TokenPrice"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_EPOCH_LOG();

CREATE TRIGGER VALIDATOR_ACCOUNT_LOG
    BEFORE INSERT OR UPDATE
    ON KEY_BASE."ValidatorAccount"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_EPOCH_LOG();

# --- !Downs

/*Log Triggers*/
DROP TRIGGER IF EXISTS TRANSACTION_COUNTER_LOG ON ANALYTICS."TransactionCounter" CASCADE;
DROP TRIGGER IF EXISTS MESSAGE_COUNTER_LOG ON ANALYTICS."MessageCounter" CASCADE;

DROP TRIGGER IF EXISTS ACCOUNT_LOG ON BLOCKCHAIN."Account" CASCADE;
DROP TRIGGER IF EXISTS AUTHORIZATION_LOG ON BLOCKCHAIN."Authorization" CASCADE;
DROP TRIGGER IF EXISTS ASSET_LOG ON BLOCKCHAIN."Asset" CASCADE;
DROP TRIGGER IF EXISTS BALANCE_LOG ON BLOCKCHAIN."Balance" CASCADE;
DROP TRIGGER IF EXISTS BLOCK_LOG ON BLOCKCHAIN."Block" CASCADE;
DROP TRIGGER IF EXISTS CLASSIFICATION_LOG ON BLOCKCHAIN."Classification" CASCADE;
DROP TRIGGER IF EXISTS DELEGATION_LOG ON BLOCKCHAIN."Delegation" CASCADE;
DROP TRIGGER IF EXISTS FEE_GRANT_LOG ON BLOCKCHAIN."FeeGrant" CASCADE;
DROP TRIGGER IF EXISTS IDENTITY_LOG ON BLOCKCHAIN."Identity" CASCADE;
DROP TRIGGER IF EXISTS MAINTAINER_LOG ON BLOCKCHAIN."Maintainer" CASCADE;
DROP TRIGGER IF EXISTS META_DATA_LOG ON BLOCKCHAIN."MetaData" CASCADE;
DROP TRIGGER IF EXISTS ORDER_LOG ON BLOCKCHAIN."Order" CASCADE;
DROP TRIGGER IF EXISTS PARAMETER_LOG ON BLOCKCHAIN."Parameter" CASCADE;
DROP TRIGGER IF EXISTS PROPOSAL_LOG ON BLOCKCHAIN."Proposal" CASCADE;
DROP TRIGGER IF EXISTS PROPOSAL_DEPOSIT_LOG ON BLOCKCHAIN."ProposalDeposit" CASCADE;
DROP TRIGGER IF EXISTS PROPOSAL_VOTE_LOG ON BLOCKCHAIN."ProposalVote" CASCADE;
DROP TRIGGER IF EXISTS REDELEGATION_LOG ON BLOCKCHAIN."Redelegation" CASCADE;
DROP TRIGGER IF EXISTS SPLIT_LOG ON BLOCKCHAIN."Split" CASCADE;
DROP TRIGGER IF EXISTS TOKEN_LOG ON BLOCKCHAIN."Token" CASCADE;
DROP TRIGGER IF EXISTS TRANSACTION_LOG ON BLOCKCHAIN."Transaction" CASCADE;
DROP TRIGGER IF EXISTS UNDELEGATION_LOG ON BLOCKCHAIN."Undelegation" CASCADE;
DROP TRIGGER IF EXISTS VALIDATOR_LOG ON BLOCKCHAIN."Validator" CASCADE;
DROP TRIGGER IF EXISTS WITHDRAW_ADDRESS_LOG ON BLOCKCHAIN."WithdrawAddress" CASCADE;

DROP TRIGGER IF EXISTS VALIDATOR_ACCOUNT_LOG ON KEY_BASE."ValidatorAccount" CASCADE;

DROP TRIGGER IF EXISTS NOTIFICATION_LOG ON MASTER_TRANSACTION."Notification" CASCADE;
DROP TRIGGER IF EXISTS TOKEN_PRICE_LOG ON MASTER_TRANSACTION."TokenPrice" CASCADE;
DROP TRIGGER IF EXISTS VALIDATOR_TRANSACTION_LOG ON MASTER_TRANSACTION."ValidatorTransaction" CASCADE;
DROP TRIGGER IF EXISTS WALLET_TRANSACTION_LOG ON MASTER_TRANSACTION."WalletTransaction" CASCADE;

DROP TABLE IF EXISTS ANALYTICS."TransactionCounter" CASCADE;
DROP TABLE IF EXISTS ANALYTICS."MessageCounter" CASCADE;

DROP TABLE IF EXISTS BLOCKCHAIN."Authorization" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN."Account" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN."Asset" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN."Balance" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN."Block" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN."Classification" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN."Delegation" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN."FeeGrant" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN."Identity" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN."Maintainer" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN."MetaData" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN."Order" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN."Parameter" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN."Proposal" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN."ProposalDeposit" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN."ProposalVote" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN."Redelegation" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN."Split" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN."Token" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN."Transaction" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN."Undelegation" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN."Validator" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN."WithdrawAddress" CASCADE;

DROP TABLE IF EXISTS KEY_BASE."ValidatorAccount" CASCADE;

DROP TABLE IF EXISTS MASTER_TRANSACTION."WalletTransaction" CASCADE;
DROP TABLE IF EXISTS MASTER_TRANSACTION."Notification" CASCADE;
DROP TABLE IF EXISTS MASTER_TRANSACTION."TokenPrice" CASCADE;
DROP TABLE IF EXISTS MASTER_TRANSACTION."ValidatorTransaction" CASCADE;

DROP SCHEMA IF EXISTS ANALYTICS CASCADE;
DROP SCHEMA IF EXISTS BLOCKCHAIN CASCADE;
DROP SCHEMA IF EXISTS KEY_BASE CASCADE;
DROP SCHEMA IF EXISTS MASTER_TRANSACTION CASCADE;