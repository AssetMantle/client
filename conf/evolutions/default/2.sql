# --- !Ups

CREATE TABLE IF NOT EXISTS BLOCKCHAIN."Authorization_BC"
(
    "granter"              VARCHAR NOT NULL,
    "grantee"              VARCHAR NOT NULL,
    "msgTypeURL"           VARCHAR NOT NULL,
    "grantedAuthorization" VARCHAR NOT NULL,
    "expiration"           VARCHAR NOT NULL,
    "createdBy"            VARCHAR,
    "createdOn"            TIMESTAMP,
    "createdOnTimeZone"    VARCHAR,
    "updatedBy"            VARCHAR,
    "updatedOn"            TIMESTAMP,
    "updatedOnTimeZone"    VARCHAR,
    PRIMARY KEY ("granter", "grantee", "msgTypeURL")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN."FeeGrant_BC"
(
    "granter"           VARCHAR NOT NULL,
    "grantee"           VARCHAR NOT NULL,
    "allowance"         VARCHAR NOT NULL,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("granter", "grantee")
);

ALTER TABLE BLOCKCHAIN."Account_BC"
    ALTER COLUMN "accountType" DROP NOT NULL;
ALTER TABLE BLOCKCHAIN."Transaction"
    DROP COLUMN "status";
ALTER TABLE MASTER."Account"
    DROP COLUMN "partialMnemonic";
ALTER TABLE MASTER."Account"
    ADD COLUMN "salt" VARCHAR;
ALTER TABLE MASTER."Account"
    ADD COLUMN "iterations" INTEGER;

CREATE TRIGGER FEE_GRANT_LOG
    BEFORE INSERT OR UPDATE
    ON BLOCKCHAIN."FeeGrant_BC"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_LOG();

CREATE TRIGGER AUTHORIZATION_BC_LOG
    BEFORE INSERT OR UPDATE
    ON BLOCKCHAIN."Authorization_BC"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_LOG();

# --- !Downs
DROP TRIGGER IF EXISTS AUTHORIZATION_BC_LOG ON BLOCKCHAIN."Authorization_BC" CASCADE;
DROP TRIGGER IF EXISTS FEE_GRANT_LOG ON BLOCKCHAIN."FeeGrant_BC" CASCADE;

DROP TABLE IF EXISTS BLOCKCHAIN."Authorization_BC" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN."FeeGrant_BC" CASCADE;