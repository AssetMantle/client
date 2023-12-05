# --- !Ups

ALTER TABLE BLOCKCHAIN."Split"
    RENAME COLUMN "ownableID" TO "assetID";

ALTER TABLE BLOCKCHAIN."Split"
    RENAME COLUMN "ownableIDString" TO "assetIDString";

ALTER TABLE BLOCKCHAIN."Split"
    DROP COLUMN IF EXISTS "protoOwnableID";

CREATE SCHEMA IF NOT EXISTS CAMPAIGN
    AUTHORIZATION "assetMantle";

CREATE TABLE IF NOT EXISTS CAMPAIGN."ClaimName"
(
    "claimTxHash"          VARCHAR NOT NULL,
    "name"                 VARCHAR NOT NULL UNIQUE,
    "height"               INTEGER NOT NULL,
    "address"              VARCHAR NOT NULL,
    "transferTxHash"       VARCHAR,
    "transferStatus"       BOOLEAN,
    "timeoutHeight"        INTEGER,
    "createdBy"            VARCHAR,
    "createdOnMillisEpoch" BIGINT,
    "updatedBy"            VARCHAR,
    "updatedOnMillisEpoch" BIGINT,
    PRIMARY KEY ("claimTxHash")
);

CREATE TABLE IF NOT EXISTS CAMPAIGN."RevertClaimName"
(
    "claimTxHash"          VARCHAR NOT NULL,
    "height"               INTEGER NOT NULL,
    "address"              VARCHAR NOT NULL,
    "coins"                VARCHAR NOT NULL,
    "returnTxHash"         VARCHAR,
    "returnStatus"         BOOLEAN,
    "timeoutHeight"        INTEGER,
    "createdBy"            VARCHAR,
    "createdOnMillisEpoch" BIGINT,
    "updatedBy"            VARCHAR,
    "updatedOnMillisEpoch" BIGINT,
    PRIMARY KEY ("claimTxHash")
);

ALTER TABLE CAMPAIGN."ClaimName"
    ADD CONSTRAINT ClaimName_claimTxHash FOREIGN KEY ("claimTxHash") REFERENCES BLOCKCHAIN."Transaction" ("hash");
ALTER TABLE CAMPAIGN."RevertClaimName"
    ADD CONSTRAINT RevertClaimName_claimTxHash FOREIGN KEY ("claimTxHash") REFERENCES BLOCKCHAIN."Transaction" ("hash");

CREATE TRIGGER CLAIM_NAME_LOG
    BEFORE INSERT OR UPDATE
    ON CAMPAIGN."ClaimName"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_EPOCH_LOG();
CREATE TRIGGER REVERT_CLAIM_NAME_LOG
    BEFORE INSERT OR UPDATE
    ON CAMPAIGN."RevertClaimName"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_EPOCH_LOG();

# --- !Downs