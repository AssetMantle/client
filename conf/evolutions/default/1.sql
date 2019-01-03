# --- !Ups

CREATE SCHEMA IF NOT EXISTS BLOCKCHAIN  AUTHORIZATION comdex;
CREATE SCHEMA IF NOT EXISTS BUSINESSTXN AUTHORIZATION comdex;
CREATE SCHEMA IF NOT EXISTS FACTORY     AUTHORIZATION comdex;
CREATE SCHEMA IF NOT EXISTS MASTER      AUTHORIZATION comdex;

-- MASTER

CREATE TABLE IF NOT EXISTS BLOCKCHAIN."Zone_BC" (
  "id"      VARCHAR NOT NULL,
  "address" VARCHAR NOT NULL,
  PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN."Organization_BC" (
  "id"      VARCHAR NOT NULL,
  "address" VARCHAR NOT NULL,
  PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN."Account_BC"(
  "address"       VARCHAR NOT NULL,
  "coins"         VARCHAR NOT NULL,
  "publicKey"     VARCHAR NOT NULL,
  "accountNumber" VARCHAR NOT NULL,
  "sequence"      VARCHAR NOT NULL,
  PRIMARY KEY ("address" )
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN."ACL_BC"(
  "address"        VARCHAR NOT NULL,
  "zoneID"         VARCHAR NOT NULL,
  "organizationID" VARCHAR NOT NULL,
  "transactions"   VARCHAR NOT NULL,
  PRIMARY KEY ("address" )
);
ALTER TABLE BLOCKCHAIN."ACL_BC" ADD CONSTRAINT ACL_Account_address             FOREIGN KEY ("address")        REFERENCES BLOCKCHAIN."Account_BC" ("address");
ALTER TABLE BLOCKCHAIN."ACL_BC" ADD CONSTRAINT ACL_Zone_zoneID                 FOREIGN KEY ("zoneID")         REFERENCES BLOCKCHAIN."Zone_BC" ("id");
ALTER TABLE BLOCKCHAIN."ACL_BC" ADD CONSTRAINT ACL_Organization_organizationID FOREIGN KEY ("organizationID") REFERENCES BLOCKCHAIN."Organization_BC" ("id");

CREATE TABLE IF NOT EXISTS BLOCKCHAIN."Fiat_BC"(
  "pegHash"           VARCHAR NOT NULL,
  "transactionID"     VARCHAR NOT NULL,
  "transactionAmount" INT     NOT NULL,
  "redeemedAmount"    INT     NOT NULL,
  PRIMARY KEY ("pegHash")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN."Owner_BC"(
  "pegHash"      VARCHAR NOT NULL,
  "ownerAddress" VARCHAR NOT NULL,
  "amount"       INT     NOT NULL,
  PRIMARY KEY ("ownerAddress","pegHash")
);
ALTER TABLE BLOCKCHAIN."Owner_BC" ADD CONSTRAINT Owners_Account_ownerAddress FOREIGN KEY ("ownerAddress" ) REFERENCES BLOCKCHAIN."Account_BC" ("address");
ALTER TABLE BLOCKCHAIN."Owner_BC" ADD CONSTRAINT Owners_Fiat_pegHash         FOREIGN KEY ("pegHash")       REFERENCES BLOCKCHAIN."Fiat_BC" ("pegHash");

CREATE TABLE IF NOT EXISTS BLOCKCHAIN."Asset_BC"(
  "pegHash"       VARCHAR NOT NULL,
  "documentHash"  VARCHAR NOT NULL,
  "assetType"     VARCHAR NOT NULL,
  "assetQuantity" INT     NOT NULL,
  "assetPrice"    INT     NOT NULL,
  "quantityUnit"  VARCHAR NOT NULL,
  "ownerAddress"  VARCHAR NOT NULL,
  "locked"        BOOLEAN NOT NULL,
  PRIMARY KEY ("pegHash")
);
ALTER TABLE BLOCKCHAIN."Asset_BC"ADD CONSTRAINT Asset_Account_ownerAddress FOREIGN KEY ("ownerAddress" ) REFERENCES BLOCKCHAIN."Account_BC" ("address");

CREATE TABLE IF NOT EXISTS BLOCKCHAIN."Negotiation_BC" (
  "id"              VARCHAR NOT NULL,
  "buyerAddress"    VARCHAR NOT NULL,
  "sellerAddress"   VARCHAR NOT NULL,
  "assetPegHash"    VARCHAR NOT NULL,
  "bid"             INT     NOT NULL,
  "time"            INT     NOT NULL,
  "buyerSignature"  VARCHAR NOT NULL,
  "sellerSignature" VARCHAR NOT NULL,
  PRIMARY KEY ("id")
);
ALTER TABLE BLOCKCHAIN."Negotiation_BC" ADD CONSTRAINT Negotiation_Account_buyerAddress  FOREIGN KEY ("buyerAddress")  REFERENCES BLOCKCHAIN."Account_BC"("address");
ALTER TABLE BLOCKCHAIN."Negotiation_BC" ADD CONSTRAINT Negotiation_Account_sellerAddress FOREIGN KEY ("sellerAddress") REFERENCES BLOCKCHAIN."Account_BC"("address");
ALTER TABLE BLOCKCHAIN."Negotiation_BC" ADD CONSTRAINT Negotiation_Asset_pegHash         FOREIGN KEY ("assetPegHash")  REFERENCES BLOCKCHAIN."Asset_BC"("pegHash");

CREATE TABLE IF NOT EXISTS BLOCKCHAIN."Order_BC" (
  "id"            VARCHAR      NOT NULL,
  "fiatProofHash" VARCHAR      NOT NULL,
  "awbProofHash"  VARCHAR      NOT NULL,
  "executed"      BOOLEAN      NOT NULL,
  PRIMARY KEY ("id")
);
ALTER TABLE BLOCKCHAIN."Order_BC" ADD CONSTRAINT Order_Negotiation_id FOREIGN KEY ("id") REFERENCES BLOCKCHAIN."Negotiation_BC"("id");

-- MASTER

CREATE TABLE IF NOT EXISTS MASTER."Zone" (
  "id"          VARCHAR NOT NULL,
  "secretHash" VARCHAR NOT NULL,
  "name"        VARCHAR NOT NULL,
  "currency"    VARCHAR NOT NULL,
  PRIMARY KEY ("id")
);
ALTER TABLE MASTER."Zone" ADD CONSTRAINT Zone_BCZone_id FOREIGN KEY ("id") REFERENCES BLOCKCHAIN."Zone_BC" ("id");

CREATE TABLE IF NOT EXISTS MASTER."Organization" (
  "id"          VARCHAR NOT NULL,
  "secretHash" VARCHAR NOT NULL,
  "name"        VARCHAR NOT NULL,
  "address"     VARCHAR NOT NULL,
  "phone"       VARCHAR NOT NULL,
  "email"       VARCHAR NOT NULL,
  PRIMARY KEY ("id")
);
ALTER TABLE MASTER."Organization" ADD CONSTRAINT Organization_BCOrganization_id FOREIGN KEY ("id") REFERENCES BLOCKCHAIN."Organization_BC" ("id");

CREATE TABLE IF NOT EXISTS MASTER."Account" (
  "id"             VARCHAR NOT NULL,
  "secretHash"    VARCHAR NOT NULL,
  "accountAddress" VARCHAR NOT NULL,
  PRIMARY KEY ("id")
);
ALTER TABLE MASTER."Account" ADD CONSTRAINT Account_BCAccount_address FOREIGN KEY ("accountAddress") REFERENCES BLOCKCHAIN."Account_BC" ("address");

CREATE TABLE IF NOT EXISTS MASTER."ZoneKYC" (
  "id"       VARCHAR NOT NULL,
  "documentType"  VARCHAR NOT NULL,
  "status"   BOOLEAN NOT NULL,
  "fileName" VARCHAR NOT NULL,
  "file"     BYTEA   NOT NULL,
  PRIMARY KEY ("id", "documentType")
);
ALTER TABLE MASTER."ZoneKYC" ADD CONSTRAINT ZoneKYC_Zone_id FOREIGN KEY ("id") REFERENCES MASTER."Zone"("id");

CREATE TABLE IF NOT EXISTS MASTER."OrganizationKYC" (
  "id"       VARCHAR NOT NULL,
  "documentType"     VARCHAR NOT NULL,
  "status"   BOOLEAN NOT NULL,
  "fileName" VARCHAR NOT NULL,
  "file"     BYTEA   NOT NULL,
  PRIMARY KEY ("id", "documentType")
);
ALTER TABLE MASTER."OrganizationKYC" ADD CONSTRAINT OrganizationKYC_Organization_id FOREIGN KEY ("id") REFERENCES MASTER."Organization" ("id");

CREATE TABLE IF NOT EXISTS MASTER."AccountKYC" (
  "id"       VARCHAR NOT NULL,
  "documentType"     VARCHAR NOT NULL,
  "status"   BOOLEAN NOT NULL,
  "fileName" VARCHAR NOT NULL,
  "file"     BYTEA   NOT NULL,
  PRIMARY KEY ("id", "documentType")
);
ALTER TABLE MASTER."AccountKYC" ADD CONSTRAINT AccountKYC_Account_id FOREIGN KEY ("id") REFERENCES MASTER."Account"("id");

CREATE TABLE IF NOT EXISTS MASTER."OrgBankAccount" (
  "id"            VARCHAR NOT NULL,
  "accountHolder" VARCHAR NOT NULL,
  "bankName"      VARCHAR NOT NULL,
  "nickName"      VARCHAR NOT NULL,
  "country"       VARCHAR NOT NULL,
  "swift"         VARCHAR NOT NULL,
  "address"       VARCHAR NOT NULL,
  "zipcode"       VARCHAR NOT NULL,
  "status"        VARCHAR NOT NULL,
  PRIMARY KEY ("id")
);
ALTER TABLE MASTER."OrgBankAccount" ADD CONSTRAINT OrgBankAccount_Organization_id FOREIGN KEY ("id") REFERENCES MASTER."Organization" ("id");

# --- !Downs

DROP TABLE IF EXISTS BLOCKCHAIN."Zone_BC" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN."Organization_BC" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN."ACL_BC" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN."Fiat_BC" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN."Owner_BC" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN."Asset_BC" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN."Negotiation_BC" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN."Order_BC" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN."Account_BC" CASCADE;

DROP TABLE IF EXISTS MASTER."Zone" CASCADE;
DROP TABLE IF EXISTS MASTER."Organization" CASCADE;
DROP TABLE IF EXISTS MASTER."Account" CASCADE;
DROP TABLE IF EXISTS MASTER."ZoneKYC" CASCADE;
DROP TABLE IF EXISTS MASTER."OrganizationKYC" CASCADE;
DROP TABLE IF EXISTS MASTER."AccountKYC" CASCADE;
DROP TABLE IF EXISTS MASTER."BankAccount" CASCADE;

DROP SCHEMA IF EXISTS BLOCKCHAIN CASCADE;
DROP SCHEMA IF EXISTS BUSINESSTXN CASCADE;
DROP SCHEMA IF EXISTS FACTORY CASCADE;
DROP SCHEMA IF EXISTS MASTER CASCADE;