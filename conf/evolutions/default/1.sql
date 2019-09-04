# --- !Ups

CREATE SCHEMA IF NOT EXISTS BLOCKCHAIN
    AUTHORIZATION "commit";
CREATE SCHEMA IF NOT EXISTS BLOCKCHAIN_TRANSACTION
    AUTHORIZATION "commit";
CREATE SCHEMA IF NOT EXISTS MASTER
    AUTHORIZATION "commit";
CREATE SCHEMA IF NOT EXISTS MASTER_TRANSACTION
    AUTHORIZATION "commit";


CREATE TABLE IF NOT EXISTS BLOCKCHAIN."Zone_BC"
(
    "id"       VARCHAR NOT NULL,
    "address"  VARCHAR NOT NULL,
    "dirtyBit" BOOLEAN NOT NULL,
    PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN."Organization_BC"
(
    "id"       VARCHAR NOT NULL,
    "address"  VARCHAR NOT NULL,
    "dirtyBit" BOOLEAN NOT NULL,
    PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN."Account_BC"
(
    "address"       VARCHAR NOT NULL,
    "coins"         VARCHAR NOT NULL,
    "publicKey"     VARCHAR NOT NULL,
    "accountNumber" VARCHAR NOT NULL,
    "sequence"      VARCHAR NOT NULL,
    "dirtyBit"      BOOLEAN NOT NULL,
    PRIMARY KEY ("address")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN."ACLAccount_BC"
(
    "address"        VARCHAR NOT NULL,
    "zoneID"         VARCHAR NOT NULL,
    "organizationID" VARCHAR NOT NULL,
    "aclHash"        VARCHAR NOT NULL,
    "dirtyBit"       BOOLEAN NOT NULL,
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
    "ownerAddress"      VARCHAR NOT NULL,
    "transactionID"     VARCHAR NOT NULL,
    "transactionAmount" VARCHAR NOT NULL,
    "redeemedAmount"    VARCHAR NOT NULL,
    "dirtyBit"          BOOLEAN NOT NULL,
    PRIMARY KEY ("pegHash", "ownerAddress")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN."Asset_BC"
(
    "pegHash"       VARCHAR NOT NULL,
    "documentHash"  VARCHAR NOT NULL,
    "assetType"     VARCHAR NOT NULL,
    "assetQuantity" VARCHAR NOT NULL,
    "assetPrice"    VARCHAR NOT NULL,
    "quantityUnit"  VARCHAR NOT NULL,
    "ownerAddress"  VARCHAR NOT NULL,
    "locked"        BOOLEAN NOT NULL,
    "moderated"     BOOLEAN NOT NULL,
    "takerAddress"  VARCHAR,
    "dirtyBit"      BOOLEAN,
    PRIMARY KEY ("pegHash")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN."Negotiation_BC"
(
    "id"                 VARCHAR NOT NULL,
    "buyerAddress"       VARCHAR NOT NULL,
    "sellerAddress"      VARCHAR NOT NULL,
    "assetPegHash"       VARCHAR NOT NULL,
    "bid"                VARCHAR NOT NULL,
    "time"               VARCHAR NOT NULL,
    "buyerSignature"     VARCHAR,
    "sellerSignature"    VARCHAR,
    "buyerBlockHeight"   VARCHAR,
    "sellerBlockHeight"  VARCHAR,
    "buyerContractHash"  VARCHAR,
    "sellerContractHash" VARCHAR,
    "dirtyBit"           BOOLEAN NOT NULL,
    PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN."Order_BC"
(
    "id"            VARCHAR NOT NULL,
    "fiatProofHash" VARCHAR,
    "awbProofHash"  VARCHAR,
    "dirtyBit"      BOOLEAN NOT NULL,
    PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN."TransactionFeedBack_BC"
(
    "address"                      VARCHAR NOT NULL,
    "sendAssetsPositiveTx"         VARCHAR NOT NULL,
    "sendAssetsNegativeTx"         VARCHAR NOT NULL,
    "sendFiatsPositiveTx"          VARCHAR NOT NULL,
    "sendFiatsNegativeTx"          VARCHAR NOT NULL,
    "ibcIssueAssetsPositiveTx"     VARCHAR NOT NULL,
    "ibcIssueAssetsNegativeTx"     VARCHAR NOT NULL,
    "ibcIssueFiatsPositiveTx"      VARCHAR NOT NULL,
    "ibcIssueFiatsNegativeTx"      VARCHAR NOT NULL,
    "buyerExecuteOrderPositiveTx"  VARCHAR NOT NULL,
    "buyerExecuteOrderNegativeTx"  VARCHAR NOT NULL,
    "sellerExecuteOrderPositiveTx" VARCHAR NOT NULL,
    "sellerExecuteOrderNegativeTx" VARCHAR NOT NULL,
    "changeBuyerBidPositiveTx"     VARCHAR NOT NULL,
    "changeBuyerBidNegativeTx"     VARCHAR NOT NULL,
    "changeSellerBidPositiveTx"    VARCHAR NOT NULL,
    "changeSellerBidNegativeTx"    VARCHAR NOT NULL,
    "confirmBuyerBidPositiveTx"    VARCHAR NOT NULL,
    "confirmBuyerBidNegativeTx"    VARCHAR NOT NULL,
    "confirmSellerBidPositiveTx"   VARCHAR NOT NULL,
    "confirmSellerBidNegativeTx"   VARCHAR NOT NULL,
    "negotiationPositiveTx"        VARCHAR NOT NULL,
    "negotiationNegativeTx"        VARCHAR NOT NULL,
    "dirtyBit"                     BOOLEAN NOT NULL,
    PRIMARY KEY ("address")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN."TraderFeedbackHistory_BC"
(
    "address"       VARCHAR NOT NULL,
    "buyerAddress"  VARCHAR NOT NULL,
    "sellerAddress" VARCHAR NOT NULL,
    "pegHash"       VARCHAR NOT NULL,
    "rating"        VARCHAR NOT NULL,
    PRIMARY KEY ("address", "buyerAddress", "sellerAddress", "pegHash")
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
    "mode"           VARCHAR NOT NULL,
    "code"           VARCHAR,
    PRIMARY KEY ("ticketID")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN_TRANSACTION."AddZone"
(
    "from"     VARCHAR NOT NULL,
    "to"       VARCHAR NOT NULL,
    "zoneID"   VARCHAR NOT NULL,
    "status"   BOOLEAN,
    "txHash"   VARCHAR,
    "ticketID" VARCHAR NOT NULL,
    "mode"     VARCHAR NOT NULL,
    "code"     VARCHAR,
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
    "mode"          VARCHAR NOT NULL,
    "code"          VARCHAR,
    PRIMARY KEY ("ticketID")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN_TRANSACTION."ChangeBuyerBid"
(
    "from"     VARCHAR NOT NULL,
    "to"       VARCHAR NOT NULL,
    "bid"      INT     NOT NULL,
    "time"     INT     NOT NULL,
    "pegHash"  VARCHAR NOT NULL,
    "gas"      INT     NOT NULL,
    "status"   BOOLEAN,
    "txHash"   VARCHAR,
    "ticketID" VARCHAR NOT NULL,
    "mode"     VARCHAR NOT NULL,
    "code"     VARCHAR,
    PRIMARY KEY ("ticketID")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN_TRANSACTION."ChangeSellerBid"
(
    "from"     VARCHAR NOT NULL,
    "to"       VARCHAR NOT NULL,
    "bid"      INT     NOT NULL,
    "time"     INT     NOT NULL,
    "pegHash"  VARCHAR NOT NULL,
    "gas"      INT     NOT NULL,
    "status"   BOOLEAN,
    "txHash"   VARCHAR,
    "ticketID" VARCHAR NOT NULL,
    "mode"     VARCHAR NOT NULL,
    "code"     VARCHAR,
    PRIMARY KEY ("ticketID")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN_TRANSACTION."ConfirmBuyerBid"
(
    "from"              VARCHAR NOT NULL,
    "to"                VARCHAR NOT NULL,
    "bid"               INT     NOT NULL,
    "time"              INT     NOT NULL,
    "pegHash"           VARCHAR NOT NULL,
    "buyerContractHash" VARCHAR NOT NULL,
    "gas"               INT     NOT NULL,
    "status"            BOOLEAN,
    "txHash"            VARCHAR,
    "ticketID"          VARCHAR NOT NULL,
    "mode"              VARCHAR NOT NULL,
    "code"              VARCHAR,
    PRIMARY KEY ("ticketID")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN_TRANSACTION."ConfirmSellerBid"
(
    "from"               VARCHAR NOT NULL,
    "to"                 VARCHAR NOT NULL,
    "bid"                INT     NOT NULL,
    "time"               INT     NOT NULL,
    "pegHash"            VARCHAR NOT NULL,
    "sellerContractHash" VARCHAR NOT NULL,
    "gas"                INT     NOT NULL,
    "status"             BOOLEAN,
    "txHash"             VARCHAR,
    "ticketID"           VARCHAR NOT NULL,
    "mode"               VARCHAR NOT NULL,
    "code"               VARCHAR,
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
    "moderated"     BOOLEAN NOT NULL,
    "gas"           INT     NOT NULL,
    "takerAddress"  VARCHAR,
    "status"        BOOLEAN,
    "txHash"        VARCHAR,
    "ticketID"      VARCHAR NOT NULL,
    "mode"          VARCHAR NOT NULL,
    "code"          VARCHAR,
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
    "mode"              VARCHAR NOT NULL,
    "code"              VARCHAR,
    PRIMARY KEY ("ticketID")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN_TRANSACTION."RedeemAsset"
(
    "from"     VARCHAR NOT NULL,
    "to"       VARCHAR NOT NULL,
    "pegHash"  VARCHAR NOT NULL,
    "gas"      INT     NOT NULL,
    "status"   BOOLEAN,
    "txHash"   VARCHAR,
    "ticketID" VARCHAR NOT NULL,
    "mode"     VARCHAR NOT NULL,
    "code"     VARCHAR,
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
    "mode"         VARCHAR NOT NULL,
    "code"         VARCHAR,
    PRIMARY KEY ("ticketID")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN_TRANSACTION."ReleaseAsset"
(
    "from"     VARCHAR NOT NULL,
    "to"       VARCHAR NOT NULL,
    "pegHash"  VARCHAR NOT NULL,
    "gas"      INT     NOT NULL,
    "status"   BOOLEAN,
    "txHash"   VARCHAR,
    "ticketID" VARCHAR NOT NULL,
    "mode"     VARCHAR NOT NULL,
    "code"     VARCHAR,
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
    "mode"          VARCHAR NOT NULL,
    "code"          VARCHAR,
    PRIMARY KEY ("ticketID")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN_TRANSACTION."SendAsset"
(
    "from"     VARCHAR NOT NULL,
    "to"       VARCHAR NOT NULL,
    "pegHash"  VARCHAR NOT NULL,
    "gas"      INT     NOT NULL,
    "status"   BOOLEAN,
    "txHash"   VARCHAR,
    "ticketID" VARCHAR NOT NULL,
    "mode"     VARCHAR NOT NULL,
    "code"     VARCHAR,
    PRIMARY KEY ("ticketID")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN_TRANSACTION."SendCoin"
(
    "from"     VARCHAR NOT NULL,
    "to"       VARCHAR NOT NULL,
    "amount"   INT     NOT NULL,
    "gas"      INT     NOT NULL,
    "status"   BOOLEAN,
    "txHash"   VARCHAR,
    "ticketID" VARCHAR NOT NULL,
    "mode"     VARCHAR NOT NULL,
    "code"     VARCHAR,
    PRIMARY KEY ("ticketID")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN_TRANSACTION."SendFiat"
(
    "from"     VARCHAR NOT NULL,
    "to"       VARCHAR NOT NULL,
    "amount"   INT     NOT NULL,
    "pegHash"  VARCHAR NOT NULL,
    "gas"      INT     NOT NULL,
    "status"   BOOLEAN,
    "txHash"   VARCHAR,
    "ticketID" VARCHAR NOT NULL,
    "mode"     VARCHAR NOT NULL,
    "code"     VARCHAR,
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
    "mode"           VARCHAR NOT NULL,
    "code"           VARCHAR,
    PRIMARY KEY ("ticketID")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN_TRANSACTION."SetBuyerFeedback"
(
    "from"     VARCHAR NOT NULL,
    "to"       VARCHAR NOT NULL,
    "pegHash"  VARCHAR NOT NULL,
    "rating"   INT     NOT NULL,
    "gas"      INT     NOT NULL,
    "status"   BOOLEAN,
    "txHash"   VARCHAR,
    "ticketID" VARCHAR NOT NULL,
    "mode"     VARCHAR NOT NULL,
    "code"     VARCHAR,
    PRIMARY KEY ("ticketID")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN_TRANSACTION."SetSellerFeedback"
(
    "from"     VARCHAR NOT NULL,
    "to"       VARCHAR NOT NULL,
    "pegHash"  VARCHAR NOT NULL,
    "rating"   INT     NOT NULL,
    "gas"      INT     NOT NULL,
    "status"   BOOLEAN,
    "txHash"   VARCHAR,
    "ticketID" VARCHAR NOT NULL,
    "mode"     VARCHAR NOT NULL,
    "code"     VARCHAR,
    PRIMARY KEY ("ticketID")
);

CREATE TABLE IF NOT EXISTS MASTER."Zone"
(
    "id"        VARCHAR NOT NULL,
    "accountID" VARCHAR NOT NULL UNIQUE,
    "name"      VARCHAR NOT NULL,
    "currency"  VARCHAR NOT NULL,
    "status"    BOOLEAN,
    PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS MASTER."Organization"
(
    "id"        VARCHAR NOT NULL,
    "zoneID"    VARCHAR NOT NULL,
    "accountID" VARCHAR NOT NULL UNIQUE,
    "name"      VARCHAR NOT NULL,
    "address"   VARCHAR NOT NULL,
    "phone"     VARCHAR NOT NULL,
    "email"     VARCHAR NOT NULL,
    "status"    BOOLEAN,
    PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS MASTER."Trader"
(
    "id"             VARCHAR NOT NULL,
    "zoneID"         VARCHAR NOT NULL,
    "organizationID" VARCHAR NOT NULL,
    "accountID"      VARCHAR NOT NULL UNIQUE,
    "name"           VARCHAR NOT NULL,
    "status"         BOOLEAN,
    PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS MASTER."Account"
(
    "id"             VARCHAR NOT NULL,
    "secretHash"     VARCHAR NOT NULL,
    "accountAddress" VARCHAR NOT NULL,
    "language"       VARCHAR NOT NULL,
    "userType"       VARCHAR NOT NULL,
    "status"         VARCHAR NOT NULL,
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
    "fileName"     VARCHAR NOT NULL,
    "file"         BYTEA,
    "status"       BOOLEAN,
    PRIMARY KEY ("id", "documentType")
);

CREATE TABLE IF NOT EXISTS MASTER."AccountFile"
(
    "id"           VARCHAR NOT NULL,
    "documentType" VARCHAR NOT NULL,
    "fileName"     VARCHAR NOT NULL,
    "file"         BYTEA,
    PRIMARY KEY ("id", "documentType")
);

CREATE TABLE IF NOT EXISTS MASTER."OrganizationKYC"
(
    "id"           VARCHAR NOT NULL,
    "documentType" VARCHAR NOT NULL,
    "fileName"     VARCHAR NOT NULL,
    "file"         BYTEA,
    "status"       BOOLEAN,
    PRIMARY KEY ("id", "documentType")
);

CREATE TABLE IF NOT EXISTS MASTER."AccountKYC"
(
    "id"           VARCHAR NOT NULL,
    "documentType" VARCHAR NOT NULL,
    "fileName"     VARCHAR NOT NULL,
    "file"         BYTEA,
    "status"       BOOLEAN,
    PRIMARY KEY ("id", "documentType")
);

CREATE TABLE IF NOT EXISTS MASTER."TraderKYC"
(
    "id"                 VARCHAR NOT NULL,
    "documentType"       VARCHAR NOT NULL,
    "fileName"           VARCHAR NOT NULL,
    "file"               BYTEA,
    "zoneStatus"         BOOLEAN,
    "organizationStatus" BOOLEAN,
    PRIMARY KEY ("id", "documentType")
);

CREATE TABLE IF NOT EXISTS MASTER."OrganizationBankAccount"
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
    "notificationToken" VARCHAR,
    "sessionTokenHash"  VARCHAR,
    "sessionTokenTime"  BIGINT,
    PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS MASTER_TRANSACTION."FaucetRequest"
(
    "id"        VARCHAR NOT NULL,
    "accountID" VARCHAR NOT NULL,
    "amount"    INT     NOT NULL,
    "gas"       INT,
    "status"    BOOLEAN,
    "ticketID"  VARCHAR,
    "comment"   VARCHAR,
    PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS MASTER_TRANSACTION."IssueAssetRequest"
(
    "id"                          VARCHAR NOT NULL,
    "ticketID"                    VARCHAR,
    "pegHash"                     VARCHAR,
    "accountID"                   VARCHAR NOT NULL,
    "documentHash"                VARCHAR NOT NULL,
    "assetType"                   VARCHAR NOT NULL,
    "quantityUnit"                VARCHAR NOT NULL,
    "assetQuantity"               INT     NOT NULL,
    "assetPrice"                  INT     NOT NULL,
    "takerAddress"                VARCHAR,
    "gas"                         INT,
    "shipmentDetails"             VARCHAR NOT NULL,
    "physicalDocumentsHandledVia" VARCHAR,
    "paymentTerms"                VARCHAR NOT NULL,
    "status"                      VARCHAR NOT NULL,
    "comment"                     VARCHAR,
    PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS MASTER_TRANSACTION."File"
(
    "id"           VARCHAR NOT NULL,
    "documentType" VARCHAR NOT NULL,
    "fileName"     VARCHAR NOT NULL,
    "file"         BYTEA,
    "context"      VARCHAR,
    "status"       BOOLEAN,
    PRIMARY KEY ("id", "documentType")
);

CREATE TABLE IF NOT EXISTS MASTER_TRANSACTION."IssueFiatRequest"
(
  "id"                VARCHAR NOT NULL,
  "accountID"         VARCHAR NOT NULL,
  "transactionID"     VARCHAR NOT NULL,
  "transactionAmount" INT     NOT NULL,
  "gas"               INT,
  "status"            BOOLEAN,
  "ticketID"          VARCHAR,
  "comment"           VARCHAR,
  PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS MASTER_TRANSACTION."Notification"
(
    "accountID"           VARCHAR NOT NULL,
    "notificationTitle"   VARCHAR NOT NULL,
    "notificationMessage" VARCHAR NOT NULL,
    "time"                BIGINT  NOT NULL,
    "read"                BOOLEAN NOT NULL,
    "id"                  VARCHAR NOT NULL UNIQUE,
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

ALTER TABLE BLOCKCHAIN."Asset_BC"
    ADD CONSTRAINT Asset_BC_Taker_Address FOREIGN KEY ("takerAddress") REFERENCES BLOCKCHAIN."Account_BC" ("address");
ALTER TABLE BLOCKCHAIN."ACLAccount_BC"
    ADD CONSTRAINT ACLAccount_Account_address FOREIGN KEY ("address") REFERENCES BLOCKCHAIN."Account_BC" ("address");
ALTER TABLE BLOCKCHAIN."ACLAccount_BC"
    ADD CONSTRAINT ACLAccount_Zone_zoneID FOREIGN KEY ("zoneID") REFERENCES BLOCKCHAIN."Zone_BC" ("id");
ALTER TABLE BLOCKCHAIN."ACLAccount_BC"
    ADD CONSTRAINT ACLAccount_ACL_hash FOREIGN KEY ("aclHash") REFERENCES BLOCKCHAIN."ACLHash_BC" ("hash");
ALTER TABLE BLOCKCHAIN."ACLAccount_BC"
    ADD CONSTRAINT ACLAccount_Organization_organizationID FOREIGN KEY ("organizationID") REFERENCES BLOCKCHAIN."Organization_BC" ("id");
ALTER TABLE BLOCKCHAIN."Negotiation_BC"
    ADD CONSTRAINT Negotiation_Account_buyerAddress FOREIGN KEY ("buyerAddress") REFERENCES BLOCKCHAIN."Account_BC" ("address");
ALTER TABLE BLOCKCHAIN."Negotiation_BC"
    ADD CONSTRAINT Negotiation_Account_sellerAddress FOREIGN KEY ("sellerAddress") REFERENCES BLOCKCHAIN."Account_BC" ("address");
ALTER TABLE BLOCKCHAIN."Organization_BC"
    ADD CONSTRAINT Organization_BC_Organization_id FOREIGN KEY ("id") REFERENCES Master."Organization" ("id");
ALTER TABLE BLOCKCHAIN."Zone_BC"
    ADD CONSTRAINT Zone_BC_Zone_id FOREIGN KEY ("id") REFERENCES Master."Zone" ("id");
ALTER TABLE BLOCKCHAIN."TransactionFeedBack_BC"
    ADD CONSTRAINT TransactionFeedBack_Account_address FOREIGN KEY ("address") REFERENCES BLOCKCHAIN."Account_BC" ("address");
ALTER TABLE BLOCKCHAIN."TraderFeedbackHistory_BC"
    ADD CONSTRAINT TraderFeedbackHistory_TransactionFeedBack_address FOREIGN KEY ("address") REFERENCES BLOCKCHAIN."TransactionFeedBack_BC" ("address");
ALTER TABLE BLOCKCHAIN."TraderFeedbackHistory_BC"
    ADD CONSTRAINT TraderFeedbackHistory_Account_address FOREIGN KEY ("address") REFERENCES BLOCKCHAIN."Account_BC" ("address");
ALTER TABLE BLOCKCHAIN."TraderFeedbackHistory_BC"
    ADD CONSTRAINT TraderFeedbackHistory_Account_buyerAddress FOREIGN KEY ("buyerAddress") REFERENCES BLOCKCHAIN."Account_BC" ("address");
ALTER TABLE BLOCKCHAIN."TraderFeedbackHistory_BC"
    ADD CONSTRAINT TraderFeedbackHistory_Account_sellerAddress FOREIGN KEY ("sellerAddress") REFERENCES BLOCKCHAIN."Account_BC" ("address");

ALTER TABLE BLOCKCHAIN_TRANSACTION."SetACL"
    ADD CONSTRAINT SetACL_ACL_hash FOREIGN KEY ("aclHash") REFERENCES BLOCKCHAIN."ACLHash_BC" ("hash");

ALTER TABLE MASTER."Trader"
    ADD CONSTRAINT Trader_Account_accountID FOREIGN KEY ("accountID") REFERENCES MASTER."Account" ("id");
ALTER TABLE MASTER."Trader"
    ADD CONSTRAINT Trader_Organization_organizationID FOREIGN KEY ("organizationID") REFERENCES MASTER."Organization" ("id");
ALTER TABLE MASTER."Trader"
    ADD CONSTRAINT Trader_Zone_zoneID FOREIGN KEY ("zoneID") REFERENCES MASTER."Zone" ("id");
ALTER TABLE MASTER."Organization"
    ADD CONSTRAINT Organization_Account_accountID FOREIGN KEY ("accountID") REFERENCES MASTER."Account" ("id");
ALTER TABLE MASTER."Organization"
    ADD CONSTRAINT Organization_Zone_zoneID FOREIGN KEY ("zoneID") REFERENCES MASTER."Zone" ("id");
ALTER TABLE MASTER."Account"
    ADD CONSTRAINT Account_BCAccount_address FOREIGN KEY ("accountAddress") REFERENCES BLOCKCHAIN."Account_BC" ("address");
ALTER TABLE MASTER."Contact"
    ADD CONSTRAINT Contact_Account_id FOREIGN KEY ("id") REFERENCES MASTER."Account" ("id");
ALTER TABLE MASTER."Zone"
    ADD CONSTRAINT Zone_Account_accountID FOREIGN KEY ("accountID") REFERENCES MASTER."Account" ("id");
ALTER TABLE MASTER."AccountKYC"
    ADD CONSTRAINT AccountKYC_Account_id FOREIGN KEY ("id") REFERENCES MASTER."Account" ("id");
ALTER TABLE MASTER."OrganizationBankAccount"
    ADD CONSTRAINT OrganizationBankAccount_Organization_id FOREIGN KEY ("id") REFERENCES MASTER."Organization" ("id");
ALTER TABLE MASTER."AccountFile"
    ADD CONSTRAINT AccountFile_Account_id FOREIGN KEY ("id") REFERENCES MASTER."Account" ("id");

ALTER TABLE MASTER_TRANSACTION."AccountToken"
    ADD CONSTRAINT AccountToken_Account_id FOREIGN KEY ("id") REFERENCES MASTER."Account" ("id");
ALTER TABLE MASTER_TRANSACTION."FaucetRequest"
    ADD CONSTRAINT FaucetRequest_MasterAccount_AccountID FOREIGN KEY ("accountID") REFERENCES MASTER."Account" ("id");
ALTER TABLE MASTER_TRANSACTION."IssueAssetRequest"
    ADD CONSTRAINT IssueAssetRequest_MasterAccount_AccountID FOREIGN KEY ("accountID") REFERENCES MASTER."Account" ("id");
ALTER TABLE MASTER_TRANSACTION."IssueAssetRequest"
    ADD CONSTRAINT IssueAssetRequest_Taker_Address FOREIGN KEY ("takerAddress") REFERENCES BLOCKCHAIN."Account_BC" ("address");
ALTER TABLE MASTER_TRANSACTION."IssueFiatRequest"
    ADD CONSTRAINT IssueFiatRequest_MasterAccount_AccountID FOREIGN KEY ("accountID") REFERENCES MASTER."Account" ("id");
ALTER TABLE MASTER_TRANSACTION."File"
    ADD CONSTRAINT File_IssueAssetRequest_id FOREIGN KEY ("id") REFERENCES MASTER_TRANSACTION."IssueAssetRequest" ("id");
ALTER TABLE MASTER_TRANSACTION."Notification"
    ADD CONSTRAINT Notification_Account_id FOREIGN KEY ("accountID") REFERENCES MASTER."Account" ("id");
ALTER TABLE MASTER_TRANSACTION."SMSOTP"
    ADD CONSTRAINT SMSOTP_Account_id FOREIGN KEY ("id") REFERENCES MASTER."Account" ("id");
ALTER TABLE MASTER_TRANSACTION."EmailOTP"
    ADD CONSTRAINT EmailOTP_Account_id FOREIGN KEY ("id") REFERENCES MASTER."Account" ("id");

/*Initial State*/

INSERT INTO blockchain."Account_BC" ("address", "coins", "publicKey", "accountNumber", "sequence", "dirtyBit")
VALUES ('commit17jxmr4felwgeugmeu6c4gr4vq0hmeaxlamvxjg',
        '1000',
        'commitpub1addwnpepqty3h2wuanwkjw5g2jn6p0rwcy7j7xm985t8kg8zpkp7ay83rrz2276x7qn',
        '0',
        '0',
        true);

INSERT INTO master."Account" ("id", "secretHash", "accountAddress", "language", "userType", "status")
VALUES ('main', '-233904575', 'commit17jxmr4felwgeugmeu6c4gr4vq0hmeaxlamvxjg', 'en', 'GENESIS', 'NO_CONTACT');

# --- !Downs

DROP TABLE IF EXISTS BLOCKCHAIN."Zone_BC" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN."Organization_BC" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN."ACLAccount_BC" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN."ACLHash_BC" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN."Fiat_BC" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN."Asset_BC" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN."Negotiation_BC" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN."Account_BC" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN."TransactionFeedBack_BC" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN."TraderFeedbackHistory_BC" CASCADE;

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
DROP TABLE IF EXISTS MASTER."Trader" CASCADE;
DROP TABLE IF EXISTS MASTER."Account" CASCADE;
DROP TABLE IF EXISTS MASTER."Contact" CASCADE;
DROP TABLE IF EXISTS MASTER."ZoneKYC" CASCADE;
DROP TABLE IF EXISTS MASTER."OrganizationKYC" CASCADE;
DROP TABLE IF EXISTS MASTER."TraderKYC" CASCADE;
DROP TABLE IF EXISTS MASTER."AccountKYC" CASCADE;
DROP TABLE IF EXISTS MASTER."OrganizationBankAccount" CASCADE;
DROP TABLE IF EXISTS MASTER."AccountFile" CASCADE;

DROP TABLE IF EXISTS MASTER_TRANSACTION."AccountToken" CASCADE;
DROP TABLE IF EXISTS MASTER_TRANSACTION."FaucetRequest" CASCADE;
DROP TABLE IF EXISTS MASTER_TRANSACTION."IssueAssetRequest" CASCADE;
DROP TABLE IF EXISTS MASTER_TRANSACTION."IssueFiatRequest" CASCADE;
DROP TABLE IF EXISTS MASTER_TRANSACTION."Notification" CASCADE;
DROP TABLE IF EXISTS MASTER_TRANSACTION."SMSOTP" CASCADE;
DROP TABLE IF EXISTS MASTER_TRANSACTION."EmailOTP" CASCADE;

DROP SCHEMA IF EXISTS BLOCKCHAIN CASCADE;
DROP SCHEMA IF EXISTS BLOCKCHAIN_TRANSACTION CASCADE;
DROP SCHEMA IF EXISTS MASTER CASCADE;
DROP SCHEMA IF EXISTS MASTER_TRANSACTION CASCADE;
