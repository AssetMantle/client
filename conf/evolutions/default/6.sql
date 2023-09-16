# --- !Ups

ALTER TABLE BLOCKCHAIN."Split"
    RENAME COLUMN "ownableID" TO "assetID";

ALTER TABLE BLOCKCHAIN."Split"
    RENAME COLUMN "ownableIDString" TO "assetIDString";

ALTER TABLE BLOCKCHAIN."Split"
    DROP COLUMN IF EXISTS "protoOwnableID";

# --- !Downs