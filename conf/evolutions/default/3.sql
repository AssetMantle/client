# --- !Ups

ALTER TABLE ANALYTICS."TransactionCounter"
    DROP COLUMN IF EXISTS "createdBy";
ALTER TABLE ANALYTICS."TransactionCounter"
    DROP COLUMN IF EXISTS "createdOnMillisEpoch";
ALTER TABLE ANALYTICS."TransactionCounter"
    DROP COLUMN IF EXISTS "updatedBy";
ALTER TABLE ANALYTICS."TransactionCounter"
    DROP COLUMN IF EXISTS "updatedOnMillisEpoch";

ALTER TABLE BLOCKCHAIN."Account"
    DROP COLUMN IF EXISTS "createdBy";
ALTER TABLE BLOCKCHAIN."Account"
    DROP COLUMN IF EXISTS "createdOnMillisEpoch";
ALTER TABLE BLOCKCHAIN."Account"
    DROP COLUMN IF EXISTS "updatedBy";
ALTER TABLE BLOCKCHAIN."Account"
    DROP COLUMN IF EXISTS "updatedOnMillisEpoch";

ALTER TABLE BLOCKCHAIN."WithdrawAddress"
    DROP COLUMN IF EXISTS "createdBy";
ALTER TABLE BLOCKCHAIN."WithdrawAddress"
    DROP COLUMN IF EXISTS "createdOnMillisEpoch";
ALTER TABLE BLOCKCHAIN."WithdrawAddress"
    DROP COLUMN IF EXISTS "updatedBy";
ALTER TABLE BLOCKCHAIN."WithdrawAddress"
    DROP COLUMN IF EXISTS "updatedOnMillisEpoch";

ALTER TABLE BLOCKCHAIN."ProposalVote"
    DROP COLUMN IF EXISTS "createdBy";
ALTER TABLE BLOCKCHAIN."ProposalVote"
    DROP COLUMN IF EXISTS "createdOnMillisEpoch";
ALTER TABLE BLOCKCHAIN."ProposalVote"
    DROP COLUMN IF EXISTS "updatedBy";
ALTER TABLE BLOCKCHAIN."ProposalVote"
    DROP COLUMN IF EXISTS "updatedOnMillisEpoch";

CREATE TABLE IF NOT EXISTS ARCHIVE."TransactionCounter"
(
    "epoch"    BIGINT  NOT NULL,
    "totalTxs" INTEGER NOT NULL,
    PRIMARY KEY ("epoch")
);

INSERT INTO ARCHIVE."TransactionCounter" ("epoch", "totalTxs")
    (SELECT "epoch", "totalTxs"
     FROM ANALYTICS."TransactionCounter"
     WHERE "epoch" <= 1680339600);

DELETE
FROM ANALYTICS."TransactionCounter"
WHERE "epoch" <= 1680339600;

# --- !Downs

DROP TABLE IF EXISTS ARCHIVE."TransactionCounter" CASCADE;