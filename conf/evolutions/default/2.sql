# --- !Ups
CREATE SCHEMA IF NOT EXISTS ARCHIVE
    AUTHORIZATION "assetMantle";

DROP TRIGGER IF EXISTS BLOCK_LOG ON BLOCKCHAIN."Block" CASCADE;
DROP TRIGGER IF EXISTS TRANSACTION_LOG ON BLOCKCHAIN."Transaction" CASCADE;
DROP TRIGGER IF EXISTS WALLET_TRANSACTION_LOG ON MASTER_TRANSACTION."WalletTransaction" CASCADE;

ALTER TABLE BLOCKCHAIN."Block"
    DROP COLUMN IF EXISTS "createdBy";
ALTER TABLE BLOCKCHAIN."Block"
    DROP COLUMN IF EXISTS "createdOnMillisEpoch";
ALTER TABLE BLOCKCHAIN."Block"
    DROP COLUMN IF EXISTS "updatedBy";
ALTER TABLE BLOCKCHAIN."Block"
    DROP COLUMN IF EXISTS "updatedOnMillisEpoch";

ALTER TABLE BLOCKCHAIN."Transaction"
    DROP COLUMN IF EXISTS "createdBy";
ALTER TABLE BLOCKCHAIN."Transaction"
    DROP COLUMN IF EXISTS "createdOnMillisEpoch";
ALTER TABLE BLOCKCHAIN."Transaction"
    DROP COLUMN IF EXISTS "updatedBy";
ALTER TABLE BLOCKCHAIN."Transaction"
    DROP COLUMN IF EXISTS "updatedOnMillisEpoch";

ALTER TABLE MASTER_TRANSaCTION."WalletTransaction"
    DROP COLUMN IF EXISTS "createdBy";
ALTER TABLE MASTER_TRANSaCTION."WalletTransaction"
    DROP COLUMN IF EXISTS "createdOnMillisEpoch";
ALTER TABLE MASTER_TRANSaCTION."WalletTransaction"
    DROP COLUMN IF EXISTS "updatedBy";
ALTER TABLE MASTER_TRANSaCTION."WalletTransaction"
    DROP COLUMN IF EXISTS "updatedOnMillisEpoch";

ALTER TABLE BLOCKCHAIN."Transaction"
    DROP CONSTRAINT IF EXISTS Transaction_Block_height;

ALTER TABLE BLOCKCHAIN."Transaction"
    ALTER COLUMN "log" DROP NOT NULL;

UPDATE BLOCKCHAIN."Transaction"
SET "log"=NULL
WHERE "height" <= 500000
  AND "code" = 0;

UPDATE BLOCKCHAIN."Transaction"
SET "log"=NULL
WHERE "height" >= 500000
  AND "height" <= 1000000
  AND "code" = 0;

UPDATE BLOCKCHAIN."Transaction"
SET "log"=NULL
WHERE "height" >= 1000000
  AND "code" = 0;

CREATE TABLE IF NOT EXISTS ARCHIVE."Block"
(
    "height"          INTEGER NOT NULL,
    "time"            BIGINT  NOT NULL,
    "proposerAddress" VARCHAR NOT NULL,
    "validators"      VARCHAR NOT NULL,
    PRIMARY KEY ("height")
);

CREATE TABLE IF NOT EXISTS ARCHIVE."Transaction"
(
    "hash"      VARCHAR NOT NULL,
    "height"    INTEGER NOT NULL,
    "code"      INTEGER NOT NULL,
    "log"       VARCHAR,
    "gasWanted" VARCHAR NOT NULL,
    "gasUsed"   VARCHAR NOT NULL,
    "txBytes"   BYTEA   NOT NULL,
    PRIMARY KEY ("hash")
);

CREATE TABLE IF NOT EXISTS ARCHIVE."WalletTransaction"
(
    "address" VARCHAR NOT NULL,
    "txHash"  VARCHAR NOT NULL,
    "height"  INTEGER NOT NULL,
    PRIMARY KEY ("address", "txHash")
);

INSERT INTO ARCHIVE."WalletTransaction" ("address", "txHash", "height")
    (SELECT "address", "txHash", "height"
     FROM MASTER_TRANSACTION."WalletTransaction"
     WHERE height <= 1000000);

DELETE
FROM MASTER_TRANSACTION."WalletTransaction"
WHERE height <= 1000000;

INSERT INTO ARCHIVE."Transaction" ("hash", "height", "code", "log", "gasWanted", "gasUsed", "txBytes")
    (SELECT "hash", "height", "code", "log", "gasWanted", "gasUsed", "txBytes"
     FROM BLOCKCHAIN."Transaction"
     WHERE height <= 1000000);

DELETE
FROM BLOCKCHAIN."Transaction"
WHERE height <= 1000000;

INSERT INTO ARCHIVE."Block" ("height", "time", "proposerAddress", "validators")
    (SELECT "height", "time", "proposerAddress", "validators"
     FROM BLOCKCHAIN."Block"
     WHERE height <= 1000000);

DELETE
FROM BLOCKCHAIN."Block"
WHERE height <= 1000000;

# --- !Downs

DROP TABLE IF EXISTS ARCHIVE."Block" CASCADE;
DROP TABLE IF EXISTS ARCHIVE."Transaction" CASCADE;
DROP TABLE IF EXISTS ARCHIVE."WalletTransaction" CASCADE;

DROP SCHEMA IF EXISTS MASTER_TRANSACTION CASCADE;