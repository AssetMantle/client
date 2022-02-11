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

CREATE TABLE IF NOT EXISTS MASTER."Profile"
(
    "identityID"        VARCHAR NOT NULL,
    "name"              VARCHAR NOT NULL,
    "description"       VARCHAR NOT NULL,
    "socialProfiles"    VARCHAR NOT NULL,
    "verified"          BOOLEAN NOT NULL,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("identityID")
);

CREATE TABLE IF NOT EXISTS MASTER."Watchlist"
(
    "identityID"         VARCHAR NOT NULL,
    "watching"          VARCHAR NOT NULL,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("identityID", "watching")
);

ALTER TABLE IF EXISTS BLOCKCHAIN."Account_BC"
    ALTER COLUMN "accountType" DROP NOT NULL;
ALTER TABLE IF EXISTS BLOCKCHAIN."IdentityProperties_BC"
    RENAME TO "Identity_BC";
ALTER TABLE IF EXISTS BLOCKCHAIN."IdentityProvisioned_BC"
    RENAME TO "IdentityProvision_BC";
ALTER TABLE IF EXISTS BLOCKCHAIN."IdentityUnprovisioned_BC"
    RENAME TO "IdentityUnprovision_BC";
ALTER TABLE IF EXISTS BLOCKCHAIN."Transaction"
    DROP COLUMN IF EXISTS "status";

ALTER TABLE IF EXISTS MASTER."Account"
    DROP COLUMN IF EXISTS "partialMnemonic";
ALTER TABLE IF EXISTS MASTER."Account"
    ADD COLUMN IF NOT EXISTS "salt" VARCHAR;
ALTER TABLE IF EXISTS MASTER."Account"
    ADD COLUMN IF NOT EXISTS "iterations" INTEGER;
ALTER TABLE IF EXISTS MASTER."Identity"
    DROP COLUMN IF EXISTS "label";
ALTER TABLE IF EXISTS MASTER."Identity"
    DROP COLUMN IF EXISTS "status";
ALTER TABLE IF EXISTS MASTER."Identity"
    ADD COLUMN "accountID" VARCHAR NOT NULL default '';
ALTER TABLE IF EXISTS MASTER."Identity"
    ADD COLUMN "nubID" VARCHAR;

ALTER TABLE MASTER."Identity"
    ADD CONSTRAINT Identity_BC_Identity_id FOREIGN KEY ("id") REFERENCES BLOCKCHAIN."Identity_BC" ("id");
ALTER TABLE MASTER."Identity"
    ADD CONSTRAINT Identity_Account_id FOREIGN KEY ("accountID") REFERENCES BLOCKCHAIN."Identity_BC" ("id");
ALTER TABLE MASTER."Profile"
    ADD CONSTRAINT Profile_Account_id FOREIGN KEY ("identityID") REFERENCES BLOCKCHAIN."Identity_BC" ("id");
ALTER TABLE MASTER."Watchlist"
    ADD CONSTRAINT Watchlist_Account_id FOREIGN KEY ("identityID") REFERENCES BLOCKCHAIN."Identity_BC" ("id");
ALTER TABLE MASTER."Watchlist"
    ADD CONSTRAINT Watchlist_Watched FOREIGN KEY ("watching") REFERENCES MASTER."Account" ("id");

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

CREATE TRIGGER PROFILE_LOG
    BEFORE INSERT OR UPDATE
    ON MASTER."Profile"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_LOG();
CREATE TRIGGER WATCHLIST_LOG
    BEFORE INSERT OR UPDATE
    ON MASTER."Watchlist"
    FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.INSERT_OR_UPDATE_LOG();

# --- !Downs
DROP TRIGGER IF EXISTS AUTHORIZATION_BC_LOG ON BLOCKCHAIN."Authorization_BC" CASCADE;
DROP TRIGGER IF EXISTS FEE_GRANT_LOG ON BLOCKCHAIN."FeeGrant_BC" CASCADE;

DROP TRIGGER IF EXISTS PROFILE_LOG ON MASTER."Profile" CASCADE;
DROP TRIGGER IF EXISTS WATCHLIST_LOG ON MASTER."Watchlist" CASCADE;

DROP TABLE IF EXISTS BLOCKCHAIN."Authorization_BC" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN."FeeGrant_BC" CASCADE;

DROP TABLE IF EXISTS MASTER."Profile" CASCADE;
DROP TABLE IF EXISTS MASTER."Watchlist" CASCADE;