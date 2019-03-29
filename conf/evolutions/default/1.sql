# --- !Ups

CREATE SCHEMA IF NOT EXISTS BLOCKCHAIN AUTHORIZATION comdex;
CREATE SCHEMA IF NOT EXISTS BLOCKCHAIN_TRANSACTION AUTHORIZATION comdex;
CREATE SCHEMA IF NOT EXISTS MASTER AUTHORIZATION comdex;
CREATE SCHEMA IF NOT EXISTS MASTER_TRANSACTION AUTHORIZATION comdex;


CREATE TABLE IF NOT EXISTS BLOCKCHAIN."Zone_BC"
(
  "id"      VARCHAR NOT NULL,
  "address" VARCHAR NOT NULL,
  PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN."Organization_BC"
(
  "id"      VARCHAR NOT NULL,
  "address" VARCHAR NOT NULL,
  PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN."Account_BC"
(
  "address"       VARCHAR NOT NULL,
  "coins"         INT     NOT NULL,
  "publicKey"     VARCHAR NOT NULL,
  "accountNumber" INT     NOT NULL,
  "sequence"      INT     NOT NULL,
  PRIMARY KEY ("address")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN."ACLAccount_BC"
(
  "address"        VARCHAR NOT NULL,
  "zoneID"         VARCHAR NOT NULL,
  "organizationID" VARCHAR NOT NULL,
  "aclHash"        VARCHAR NOT NULL,
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
  PRIMARY KEY ("hash")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN."Fiat_BC"
(
  "pegHash"           VARCHAR NOT NULL,
  "transactionID"     VARCHAR NOT NULL,
  "transactionAmount" INT     NOT NULL,
  "redeemedAmount"    INT     NOT NULL,
  PRIMARY KEY ("pegHash")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN."Owner_BC"
(
  "pegHash"      VARCHAR NOT NULL,
  "ownerAddress" VARCHAR NOT NULL,
  "amount"       INT     NOT NULL,
  PRIMARY KEY ("ownerAddress", "pegHash")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN."Asset_BC"
(
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

CREATE TABLE IF NOT EXISTS BLOCKCHAIN."Negotiation_BC"
(
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

CREATE TABLE IF NOT EXISTS BLOCKCHAIN."Order_BC"
(
  "id"            VARCHAR NOT NULL,
  "fiatProofHash" VARCHAR NOT NULL,
  "awbProofHash"  VARCHAR NOT NULL,
  "executed"      BOOLEAN NOT NULL,
  PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN_TRANSACTION."AddOrganization"
(
  "from"           VARCHAR NOT NULL,
  "to"             VARCHAR NOT NULL,
  "organizationID" VARCHAR NOT NULL,
  "zoneID"         VARCHAR NOT NULL,
  "status"         BOOLEAN,
  "txHash"         VARCHAR,
  "ticketID"       VARCHAR NOT NULL,
  "responseCode"   VARCHAR,
  PRIMARY KEY ("ticketID")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN_TRANSACTION."AddZone"
(
  "from"         VARCHAR NOT NULL,
  "to"           VARCHAR NOT NULL,
  "zoneID"       VARCHAR NOT NULL,
  "status"       BOOLEAN,
  "txHash"       VARCHAR,
  "ticketID"     VARCHAR NOT NULL,
  "responseCode" VARCHAR,
  PRIMARY KEY ("ticketID")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN_TRANSACTION."BuyerExecuteOrder"
(
  "from"          VARCHAR NOT NULL,
  "buyerAddress"  VARCHAR NOT NULL,
  "sellerAddress" VARCHAR NOT NULL,
  "fiatProofHash" VARCHAR NOT NULL,
  "pegHash"       VARCHAR NOT NULL,
  "gas"           INT     NOT NULL,
  "status"        BOOLEAN,
  "txHash"        VARCHAR,
  "ticketID"      VARCHAR NOT NULL,
  "responseCode"  VARCHAR,
  PRIMARY KEY ("ticketID")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN_TRANSACTION."ChangeBuyerBid"
(
  "from"         VARCHAR NOT NULL,
  "to"           VARCHAR NOT NULL,
  "bid"          INT     NOT NULL,
  "time"         INT     NOT NULL,
  "pegHash"      VARCHAR NOT NULL,
  "gas"          INT     NOT NULL,
  "status"       BOOLEAN,
  "txHash"       VARCHAR,
  "ticketID"     VARCHAR NOT NULL,
  "responseCode" VARCHAR,
  PRIMARY KEY ("ticketID")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN_TRANSACTION."ChangeSellerBid"
(
  "from"         VARCHAR NOT NULL,
  "to"           VARCHAR NOT NULL,
  "bid"          INT     NOT NULL,
  "time"         INT     NOT NULL,
  "pegHash"      VARCHAR NOT NULL,
  "gas"          INT     NOT NULL,
  "status"       BOOLEAN,
  "txHash"       VARCHAR,
  "ticketID"     VARCHAR NOT NULL,
  "responseCode" VARCHAR,
  PRIMARY KEY ("ticketID")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN_TRANSACTION."ConfirmBuyerBid"
(
  "from"         VARCHAR NOT NULL,
  "to"           VARCHAR NOT NULL,
  "bid"          INT     NOT NULL,
  "time"         INT     NOT NULL,
  "pegHash"      VARCHAR NOT NULL,
  "gas"          INT     NOT NULL,
  "status"       BOOLEAN,
  "txHash"       VARCHAR,
  "ticketID"     VARCHAR NOT NULL,
  "responseCode" VARCHAR,
  PRIMARY KEY ("ticketID")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN_TRANSACTION."ConfirmSellerBid"
(
  "from"         VARCHAR NOT NULL,
  "to"           VARCHAR NOT NULL,
  "bid"          INT     NOT NULL,
  "time"         INT     NOT NULL,
  "pegHash"      VARCHAR NOT NULL,
  "gas"          INT     NOT NULL,
  "status"       BOOLEAN,
  "txHash"       VARCHAR,
  "ticketID"     VARCHAR NOT NULL,
  "responseCode" VARCHAR,
  PRIMARY KEY ("ticketID")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN_TRANSACTION."IssueAsset"
(
  "from"          VARCHAR NOT NULL,
  "to"            VARCHAR NOT NULL,
  "documentHash"  VARCHAR NOT NULL,
  "assetType"     VARCHAR NOT NULL,
  "assetPrice"    INT     NOT NULL,
  "quantityUnit"  VARCHAR NOT NULL,
  "assetQuantity" INT     NOT NULL,
  "gas"           INT     NOT NULL,
  "status"        BOOLEAN,
  "txHash"        VARCHAR,
  "ticketID"      VARCHAR NOT NULL,
  "responseCode"  VARCHAR,
  PRIMARY KEY ("ticketID")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN_TRANSACTION."IssueFiat"
(
  "from"              VARCHAR NOT NULL,
  "to"                VARCHAR NOT NULL,
  "transactionID"     VARCHAR NOT NULL,
  "transactionAmount" INT     NOT NULL,
  "gas"               INT     NOT NULL,
  "status"            BOOLEAN,
  "txHash"            VARCHAR,
  "ticketID"          VARCHAR NOT NULL,
  "responseCode"      VARCHAR,
  PRIMARY KEY ("ticketID")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN_TRANSACTION."RedeemAsset"
(
  "from"         VARCHAR NOT NULL,
  "to"           VARCHAR NOT NULL,
  "pegHash"      VARCHAR NOT NULL,
  "gas"          INT     NOT NULL,
  "status"       BOOLEAN,
  "txHash"       VARCHAR,
  "ticketID"     VARCHAR NOT NULL,
  "responseCode" VARCHAR,
  PRIMARY KEY ("ticketID")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN_TRANSACTION."RedeemFiat"
(
  "from"         VARCHAR NOT NULL,
  "to"           VARCHAR NOT NULL,
  "redeemAmount" INT     NOT NULL,
  "gas"          INT     NOT NULL,
  "status"       BOOLEAN,
  "txHash"       VARCHAR,
  "ticketID"     VARCHAR NOT NULL,
  "responseCode" VARCHAR,
  PRIMARY KEY ("ticketID")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN_TRANSACTION."ReleaseAsset"
(
  "from"         VARCHAR NOT NULL,
  "to"           VARCHAR NOT NULL,
  "pegHash"      VARCHAR NOT NULL,
  "gas"          INT     NOT NULL,
  "status"       BOOLEAN,
  "txHash"       VARCHAR,
  "ticketID"     VARCHAR NOT NULL,
  "responseCode" VARCHAR,
  PRIMARY KEY ("ticketID")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN_TRANSACTION."SellerExecuteOrder"
(
  "from"          VARCHAR NOT NULL,
  "buyerAddress"  VARCHAR NOT NULL,
  "sellerAddress" VARCHAR NOT NULL,
  "awbProofHash"  VARCHAR NOT NULL,
  "pegHash"       VARCHAR NOT NULL,
  "gas"           INT     NOT NULL,
  "status"        BOOLEAN,
  "txHash"        VARCHAR,
  "ticketID"      VARCHAR NOT NULL,
  "responseCode"  VARCHAR,
  PRIMARY KEY ("ticketID")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN_TRANSACTION."SendAsset"
(
  "from"         VARCHAR NOT NULL,
  "to"           VARCHAR NOT NULL,
  "pegHash"      VARCHAR NOT NULL,
  "gas"          INT     NOT NULL,
  "status"       BOOLEAN,
  "txHash"       VARCHAR,
  "ticketID"     VARCHAR NOT NULL,
  "responseCode" VARCHAR,
  PRIMARY KEY ("ticketID")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN_TRANSACTION."SendCoin"
(
  "from"         VARCHAR NOT NULL,
  "to"           VARCHAR NOT NULL,
  "amount"       INT     NOT NULL,
  "gas"          INT     NOT NULL,
  "status"       BOOLEAN,
  "txHash"       VARCHAR,
  "ticketID"     VARCHAR NOT NULL,
  "responseCode" VARCHAR,
  PRIMARY KEY ("ticketID")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN_TRANSACTION."SendFiat"
(
  "from"         VARCHAR NOT NULL,
  "to"           VARCHAR NOT NULL,
  "amount"       INT     NOT NULL,
  "pegHash"      VARCHAR NOT NULL,
  "gas"          INT     NOT NULL,
  "status"       BOOLEAN,
  "txHash"       VARCHAR,
  "ticketID"     VARCHAR NOT NULL,
  "responseCode" VARCHAR,
  PRIMARY KEY ("ticketID")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN_TRANSACTION."SetACL"
(
  "from"           VARCHAR NOT NULL,
  "aclAddress"     VARCHAR NOT NULL,
  "organizationID" VARCHAR NOT NULL,
  "zoneID"         VARCHAR NOT NULL,
  "aclHash"        VARCHAR NOT NULL,
  "status"         BOOLEAN,
  "txHash"         VARCHAR,
  "ticketID"       VARCHAR NOT NULL,
  "responseCode"   VARCHAR,
  PRIMARY KEY ("ticketID")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN_TRANSACTION."SetBuyerFeedback"
(
  "from"         VARCHAR NOT NULL,
  "to"           VARCHAR NOT NULL,
  "pegHash"      VARCHAR NOT NULL,
  "rating"       INT     NOT NULL,
  "gas"          INT     NOT NULL,
  "status"       BOOLEAN,
  "txHash"       VARCHAR,
  "ticketID"     VARCHAR NOT NULL,
  "responseCode" VARCHAR,
  PRIMARY KEY ("ticketID")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN_TRANSACTION."SetSellerFeedback"
(
  "from"         VARCHAR NOT NULL,
  "to"           VARCHAR NOT NULL,
  "pegHash"      VARCHAR NOT NULL,
  "rating"       INT     NOT NULL,
  "gas"          INT     NOT NULL,
  "status"       BOOLEAN,
  "txHash"       VARCHAR,
  "ticketID"     VARCHAR NOT NULL,
  "responseCode" VARCHAR,
  PRIMARY KEY ("ticketID")
);

CREATE TABLE IF NOT EXISTS MASTER."Zone"
(
  "id"         VARCHAR NOT NULL,
  "secretHash" VARCHAR NOT NULL,
  "name"       VARCHAR NOT NULL,
  "currency"   VARCHAR NOT NULL,
  "status"     BOOLEAN,
  PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS MASTER."Organization"
(
  "id"        VARCHAR NOT NULL,
  "accountID" VARCHAR NOT NULL UNIQUE,
  "name"      VARCHAR NOT NULL,
  "address"   VARCHAR NOT NULL,
  "phone"     VARCHAR NOT NULL,
  "email"     VARCHAR NOT NULL,
  "status"    BOOLEAN,
  PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS MASTER."Account"
(
  "id"             VARCHAR NOT NULL,
  "secretHash"     VARCHAR NOT NULL,
  "accountAddress" VARCHAR NOT NULL,
  "language"       VARCHAR NOT NULL,
  "userType"       VARCHAR,
  PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS MASTER."Contact"
(
  "id"                   VARCHAR NOT NULL,
  "mobileNumber"         VARCHAR NOT NULL,
  "mobileNumberVerified" BOOLEAN NOT NULL,
  "emailAddress"         VARCHAR NOT NULL,
  "emailAddressVerified" BOOLEAN NOT NULL,
  PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS MASTER."ZoneKYC"
(
  "id"           VARCHAR NOT NULL,
  "documentType" VARCHAR NOT NULL,
  "status"       BOOLEAN NOT NULL,
  "fileName"     VARCHAR NOT NULL,
  "file"         BYTEA   NOT NULL,
  PRIMARY KEY ("id", "documentType")
);

CREATE TABLE IF NOT EXISTS MASTER."OrganizationKYC"
(
  "id"           VARCHAR NOT NULL,
  "documentType" VARCHAR NOT NULL,
  "status"       BOOLEAN NOT NULL,
  "fileName"     VARCHAR NOT NULL,
  "file"         BYTEA   NOT NULL,
  PRIMARY KEY ("id", "documentType")
);

CREATE TABLE IF NOT EXISTS MASTER."AccountKYC"
(
  "id"           VARCHAR NOT NULL,
  "documentType" VARCHAR NOT NULL,
  "status"       BOOLEAN NOT NULL,
  "fileName"     VARCHAR NOT NULL,
  "file"         BYTEA   NOT NULL,
  PRIMARY KEY ("id", "documentType")
);

CREATE TABLE IF NOT EXISTS MASTER."OrgBankAccount"
(
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


CREATE TABLE IF NOT EXISTS MASTER_TRANSACTION."AccountToken"
(
  "id"                VARCHAR NOT NULL,
  "registrationToken" VARCHAR NOT NULL,
  "sessionTokenHash"  VARCHAR,
  PRIMARY KEY ("id")
);


CREATE TABLE IF NOT EXISTS MASTER_TRANSACTION."SMSOTP"
(
  "id"         VARCHAR NOT NULL,
  "secretHash" VARCHAR NOT NULL,
  PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS MASTER_TRANSACTION."EmailOTP"
(
  "id"         VARCHAR NOT NULL,
  "secretHash" VARCHAR NOT NULL,
  PRIMARY KEY ("id")
);

ALTER TABLE BLOCKCHAIN."ACLAccount_BC"
  ADD CONSTRAINT ACLAccount_Account_address FOREIGN KEY ("address") REFERENCES BLOCKCHAIN."Account_BC" ("address");
ALTER TABLE BLOCKCHAIN."ACLAccount_BC"
  ADD CONSTRAINT ACLAccount_Zone_zoneID FOREIGN KEY ("zoneID") REFERENCES BLOCKCHAIN."Zone_BC" ("id");
ALTER TABLE BLOCKCHAIN."ACLAccount_BC"
  ADD CONSTRAINT ACLAccount_ACL_hash FOREIGN KEY ("aclHash") REFERENCES BLOCKCHAIN."ACLHash_BC" ("hash");
ALTER TABLE BLOCKCHAIN."ACLAccount_BC"
  ADD CONSTRAINT ACLAccount_Organization_organizationID FOREIGN KEY ("organizationID") REFERENCES BLOCKCHAIN."Organization_BC" ("id");
ALTER TABLE BLOCKCHAIN."Owner_BC"
  ADD CONSTRAINT Owners_Account_ownerAddress FOREIGN KEY ("ownerAddress") REFERENCES BLOCKCHAIN."Account_BC" ("address");
ALTER TABLE BLOCKCHAIN."Owner_BC"
  ADD CONSTRAINT Owners_Fiat_pegHash FOREIGN KEY ("pegHash") REFERENCES BLOCKCHAIN."Fiat_BC" ("pegHash");
ALTER TABLE BLOCKCHAIN."Asset_BC"
  ADD CONSTRAINT Asset_Account_ownerAddress FOREIGN KEY ("ownerAddress") REFERENCES BLOCKCHAIN."Account_BC" ("address");
ALTER TABLE BLOCKCHAIN."Negotiation_BC"
  ADD CONSTRAINT Negotiation_Account_buyerAddress FOREIGN KEY ("buyerAddress") REFERENCES BLOCKCHAIN."Account_BC" ("address");
ALTER TABLE BLOCKCHAIN."Negotiation_BC"
  ADD CONSTRAINT Negotiation_Account_sellerAddress FOREIGN KEY ("sellerAddress") REFERENCES BLOCKCHAIN."Account_BC" ("address");
ALTER TABLE BLOCKCHAIN."Negotiation_BC"
  ADD CONSTRAINT Negotiation_Asset_pegHash FOREIGN KEY ("assetPegHash") REFERENCES BLOCKCHAIN."Asset_BC" ("pegHash");
ALTER TABLE BLOCKCHAIN."Order_BC"
  ADD CONSTRAINT Order_Negotiation_id FOREIGN KEY ("id") REFERENCES BLOCKCHAIN."Negotiation_BC" ("id");
ALTER TABLE BLOCKCHAIN."Organization_BC"
  ADD CONSTRAINT Organization_BC_Organization_id FOREIGN KEY ("id") REFERENCES Master."Organization" ("id");
ALTER TABLE BLOCKCHAIN."Zone_BC"
  ADD CONSTRAINT Zone_BC_Zone_id FOREIGN KEY ("id") REFERENCES Master."Zone" ("id");

ALTER TABLE BLOCKCHAIN_TRANSACTION."SetACL"
  ADD CONSTRAINT SetACL_ACL_hash FOREIGN KEY ("aclHash") REFERENCES BLOCKCHAIN."ACLHash_BC" ("hash");

ALTER TABLE MASTER."Organization"
  ADD CONSTRAINT Organization_Account_accountID FOREIGN KEY ("accountID") REFERENCES MASTER."Account" ("id");
ALTER TABLE MASTER."Account"
  ADD CONSTRAINT Account_BCAccount_address FOREIGN KEY ("accountAddress") REFERENCES BLOCKCHAIN."Account_BC" ("address");
ALTER TABLE MASTER."Contact"
  ADD CONSTRAINT Contact_Account_id FOREIGN KEY ("id") REFERENCES MASTER."Account" ("id");
ALTER TABLE MASTER."ZoneKYC"
  ADD CONSTRAINT ZoneKYC_Zone_id FOREIGN KEY ("id") REFERENCES MASTER."Zone" ("id");
ALTER TABLE MASTER."OrganizationKYC"
  ADD CONSTRAINT OrganizationKYC_Organization_id FOREIGN KEY ("id") REFERENCES MASTER."Organization" ("id");
ALTER TABLE MASTER."AccountKYC"
  ADD CONSTRAINT AccountKYC_Account_id FOREIGN KEY ("id") REFERENCES MASTER."Account" ("id");
ALTER TABLE MASTER."OrgBankAccount"
  ADD CONSTRAINT OrgBankAccount_Organization_id FOREIGN KEY ("id") REFERENCES MASTER."Organization" ("id");
ALTER TABLE MASTER_TRANSACTION."AccountToken"
  ADD CONSTRAINT AccountToken_Account_id FOREIGN KEY ("id") REFERENCES MASTER."Account" ("id");
ALTER TABLE MASTER_TRANSACTION."SMSOTP"
  ADD CONSTRAINT SMSOTP_Account_id FOREIGN KEY ("id") REFERENCES MASTER."Account" ("id");
ALTER TABLE MASTER_TRANSACTION."EmailOTP"
  ADD CONSTRAINT EmailOTP_Account_id FOREIGN KEY ("id") REFERENCES MASTER."Account" ("id");

# --- !Downs

DROP TABLE IF EXISTS BLOCKCHAIN."Zone_BC" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN."Organization_BC" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN."ACLAccount_BC" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN."ACLHash_BC" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN."Fiat_BC" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN."Owner_BC" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN."Asset_BC" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN."Negotiation_BC" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN."Order_BC" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN."Account_BC" CASCADE;

DROP TABLE IF EXISTS BLOCKCHAIN_TRANSACTION."AddKey" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN_TRANSACTION."AddOrganization" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN_TRANSACTION."AddZone" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN_TRANSACTION."BuyerExecuteOrder" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN_TRANSACTION."ChangeBuyerBid" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN_TRANSACTION."ChangeSellerBid" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN_TRANSACTION."ConfirmBuyerBid" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN_TRANSACTION."ConfirmSellerBid" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN_TRANSACTION."IssueAsset" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN_TRANSACTION."IssueFiat" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN_TRANSACTION."RedeemAsset" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN_TRANSACTION."RedeemFiat" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN_TRANSACTION."ReleaseAsset" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN_TRANSACTION."SellerExecuteOrder" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN_TRANSACTION."SendAsset" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN_TRANSACTION."SendCoin" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN_TRANSACTION."SendFiat" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN_TRANSACTION."SetACL" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN_TRANSACTION."SetBuyerFeedback" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN_TRANSACTION."SetSellerFeedback" CASCADE;

DROP TABLE IF EXISTS MASTER."Zone" CASCADE;
DROP TABLE IF EXISTS MASTER."Organization" CASCADE;
DROP TABLE IF EXISTS MASTER."Account" CASCADE;
DROP TABLE IF EXISTS MASTER."Contact" CASCADE;
DROP TABLE IF EXISTS MASTER."ZoneKYC" CASCADE;
DROP TABLE IF EXISTS MASTER."OrganizationKYC" CASCADE;
DROP TABLE IF EXISTS MASTER."AccountKYC" CASCADE;
DROP TABLE IF EXISTS MASTER."OrgBankAccount" CASCADE;
DROP TABLE IF EXISTS MASTER."BankAccount" CASCADE;

DROP TABLE IF EXISTS MASTER_TRANSACTION."AccountToken" CASCADE;
DROP TABLE IF EXISTS MASTER_TRANSACTION."SMSOTP" CASCADE;
DROP TABLE IF EXISTS MASTER_TRANSACTION."EmailOTP" CASCADE;

DROP SCHEMA IF EXISTS BLOCKCHAIN CASCADE;
DROP SCHEMA IF EXISTS BLOCKCHAIN_TRANSACTION CASCADE;
DROP SCHEMA IF EXISTS MASTER CASCADE;
DROP SCHEMA IF EXISTS MASTER_TRANSACTION CASCADE;
