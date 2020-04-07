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
    "gas"            INT     NOT NULL,
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
    "gas"      INT     NOT NULL,
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
    "gas"            INT     NOT NULL,
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

CREATE TABLE IF NOT EXISTS MASTER."AccountFile"
(
    "id"           VARCHAR NOT NULL,
    "documentType" VARCHAR NOT NULL,
    "fileName"     VARCHAR NOT NULL,
    "file"         BYTEA,
    PRIMARY KEY ("id", "documentType")
);

CREATE TABLE IF NOT EXISTS MASTER."AccountKYC"
(
    "id"           VARCHAR NOT NULL,
    "documentType" VARCHAR NOT NULL,
    "fileName"     VARCHAR NOT NULL UNIQUE,
    "file"         BYTEA,
    "status"       BOOLEAN,
    PRIMARY KEY ("id", "documentType")
);

CREATE TABLE IF NOT EXISTS MASTER."Asset"
(
    "id"               VARCHAR NOT NULL,
    "ownerID"          VARCHAR NOT NULL,
    "ticketID"         VARCHAR,
    "pegHash"          VARCHAR,
    "assetType"        VARCHAR NOT NULL,
    "description"      VARCHAR NOT NULL,
    "documentHash"     VARCHAR NOT NULL UNIQUE,
    "quantity"         INT     NOT NULL,
    "quantityUnit"     VARCHAR NOT NULL,
    "price"            INT     NOT NULL,
    "moderated"        BOOLEAN NOT NULL,
    "shippingPeriod"   INT     NOT NULL,
    "portOfLoading"    VARCHAR NOT NULL,
    "portOfDischarge"  VARCHAR NOT NULL,
    "status"           VARCHAR NOT NULL,
    PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS MASTER."Contact"
(
    "id"                   VARCHAR NOT NULL,
    "mobileNumber"         VARCHAR NOT NULL UNIQUE,
    "mobileNumberVerified" BOOLEAN NOT NULL,
    "emailAddress"         VARCHAR NOT NULL UNIQUE,
    "emailAddressVerified" BOOLEAN NOT NULL,
    PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS MASTER."Identification"
(
    "accountID"          VARCHAR NOT NULL,
    "firstName"          VARCHAR NOT NULL,
    "lastName"           VARCHAR NOT NULL,
    "dateOfBirth"        DATE    NOT NULL,
    "idNumber"           VARCHAR NOT NULL,
    "idType"             VARCHAR NOT NULL,
    "completionStatus"   BOOLEAN NOT NULL,
    "verificationStatus" BOOLEAN,
    PRIMARY KEY ("accountID")
);

CREATE TABLE IF NOT EXISTS MASTER."Negotiation"
(
    "id"                            VARCHAR NOT NULL,
    "negotiationID"                 VARCHAR,
    "ticketID"                      VARCHAR,
    "buyerTraderID"                 VARCHAR NOT NULL,
    "sellerTraderID"                VARCHAR NOT NULL,
    "assetID"                       VARCHAR NOT NULL,
    "assetDescription"              VARCHAR NOT NULL,
    "price"                         INT     NOT NULL,
    "quantity"                      INT     NOT NULL,
    "quantityUnit"                  VARCHAR NOT NULL,
    "shippingPeriod"                INT     NOT NULL,
    "time"                          INT,
    "buyerAcceptedAssetDescription" BOOLEAN NOT NULL,
    "buyerAcceptedPrice"            BOOLEAN NOT NULL,
    "buyerAcceptedQuantity"         BOOLEAN NOT NULL,
    "buyerAcceptedShippingPeriod"   BOOLEAN NOT NULL,
    "advancePayment"                BOOLEAN,
    "advancePercentage"             DECIMAL(4, 2),
    "credit"                        BOOLEAN,
    "tenure"                        INT,
    "tentativeDate"                 DATE,
    "reference"                     VARCHAR,
    "buyerAcceptedAdvancePayment"   BOOLEAN NOT NULL,
    "buyerAcceptedCredit"           BOOLEAN NOT NULL,
    "billOfExchange"                BOOLEAN,
    "coo"                           BOOLEAN,
    "coa"                           BOOLEAN,
    "otherDocuments"                VARCHAR,
    "buyerAcceptedBillOfExchange"   BOOLEAN NOT NULL,
    "buyerAcceptedCOO"              BOOLEAN NOT NULL,
    "buyerAcceptedCOA"              BOOLEAN NOT NULL,
    "buyerAcceptedOtherDocuments"   BOOLEAN NOT NULL,
    "chatID"                        VARCHAR UNIQUE,
    "status"                        VARCHAR,
    "comment"                       VARCHAR,
    PRIMARY KEY ("id"),
    UNIQUE ("buyerTraderID", "sellerTraderID", "assetID")
);


CREATE TABLE IF NOT EXISTS MASTER."Organization"
(
    "id"                 VARCHAR NOT NULL,
    "zoneID"             VARCHAR NOT NULL,
    "accountID"          VARCHAR NOT NULL UNIQUE,
    "name"               VARCHAR NOT NULL,
    "abbreviation"       VARCHAR,
    "establishmentDate"  DATE    NOT NULL,
    "email"              VARCHAR NOT NULL,
    "registeredAddress"  VARCHAR NOT NULL,
    "postalAddress"      VARCHAR NOT NULL,
    "ubos"               VARCHAR,
    "completionStatus"   BOOLEAN NOT NULL,
    "verificationStatus" BOOLEAN,
    PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS MASTER."OrganizationBankAccountDetail"
(
    "id"            VARCHAR NOT NULL,
    "accountHolder" VARCHAR NOT NULL,
    "nickName"      VARCHAR NOT NULL,
    "accountNumber" VARCHAR NOT NULL,
    "bankName"      VARCHAR NOT NULL,
    "swiftAddress"  VARCHAR NOT NULL,
    "address"       VARCHAR NOT NULL,
    "country"       VARCHAR NOT NULL,
    "zipCode"       VARCHAR NOT NULL,
    "status"        VARCHAR,
    PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS MASTER."OrganizationBackgroundCheck"
(
    "id"           VARCHAR NOT NULL,
    "documentType" VARCHAR NOT NULL,
    "fileName"     VARCHAR NOT NULL UNIQUE,
    "file"         BYTEA,
    "status"       BOOLEAN,
    PRIMARY KEY ("id", "documentType")
);

CREATE TABLE IF NOT EXISTS MASTER."OrganizationKYC"
(
    "id"           VARCHAR NOT NULL,
    "documentType" VARCHAR NOT NULL,
    "fileName"     VARCHAR NOT NULL UNIQUE,
    "file"         BYTEA,
    "status"       BOOLEAN,
    PRIMARY KEY ("id", "documentType")
);

CREATE TABLE IF NOT EXISTS MASTER."Trader"
(
    "id"                 VARCHAR NOT NULL,
    "zoneID"             VARCHAR NOT NULL,
    "organizationID"     VARCHAR NOT NULL,
    "accountID"          VARCHAR NOT NULL UNIQUE,
    "name"               VARCHAR NOT NULL,
    "completionStatus"   BOOLEAN NOT NULL,
    "verificationStatus" BOOLEAN,
    PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS MASTER."TraderBackgroundCheck"
(
    "id"           VARCHAR NOT NULL,
    "documentType" VARCHAR NOT NULL,
    "fileName"     VARCHAR NOT NULL UNIQUE,
    "file"         BYTEA,
    "status"       BOOLEAN,
    PRIMARY KEY ("id", "documentType")
);

CREATE TABLE IF NOT EXISTS MASTER."TraderKYC"
(
    "id"                 VARCHAR NOT NULL,
    "documentType"       VARCHAR NOT NULL,
    "fileName"           VARCHAR NOT NULL UNIQUE,
    "file"               BYTEA,
    "zoneStatus"         BOOLEAN,
    "organizationStatus" BOOLEAN,
    PRIMARY KEY ("id", "documentType")
);

CREATE TABLE IF NOT EXISTS MASTER."TraderRelation"
(
    "id"     VARCHAR NOT NULL,
    "fromID" VARCHAR NOT NULL,
    "toID"   VARCHAR NOT NULL,
    "status" BOOLEAN,
    PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS MASTER."Zone"
(
    "id"                 VARCHAR NOT NULL,
    "accountID"          VARCHAR NOT NULL UNIQUE,
    "name"               VARCHAR NOT NULL,
    "currency"           VARCHAR NOT NULL,
    "address"            VARCHAR NOT NULL,
    "completionStatus"   BOOLEAN NOT NULL,
    "verificationStatus" BOOLEAN,
    PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS MASTER."ZoneKYC"
(
    "id"           VARCHAR NOT NULL,
    "documentType" VARCHAR NOT NULL,
    "fileName"     VARCHAR NOT NULL UNIQUE,
    "file"         BYTEA,
    "status"       BOOLEAN,
    PRIMARY KEY ("id", "documentType")
);

CREATE TABLE IF NOT EXISTS MASTER_TRANSACTION."AssetFile"
(
    "id"              VARCHAR NOT NULL,
    "documentType"    VARCHAR NOT NULL,
    "fileName"        VARCHAR NOT NULL UNIQUE,
    "file"            BYTEA,
    "documentContent" VARCHAR,
    "status"          BOOLEAN,
    PRIMARY KEY ("id", "documentType")
);

CREATE TABLE IF NOT EXISTS MASTER_TRANSACTION."Chat"
(
    "id"        VARCHAR NOT NULL,
    "accountID" VARCHAR NOT NULL,
    PRIMARY KEY ("id", "accountID")
);

CREATE TABLE IF NOT EXISTS MASTER_TRANSACTION."EmailOTP"
(
    "id"         VARCHAR NOT NULL,
    "secretHash" VARCHAR NOT NULL,
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
    "documentHash"                VARCHAR,
    "assetType"                   VARCHAR NOT NULL,
    "quantityUnit"                VARCHAR NOT NULL,
    "assetQuantity"               INT     NOT NULL,
    "assetPrice"                  INT     NOT NULL,
    "takerAddress"                VARCHAR,
    "physicalDocumentsHandledVia" VARCHAR,
    "paymentTerms"                VARCHAR NOT NULL,
    "completionStatus"            BOOLEAN NOT NULL,
    "verificationStatus"          BOOLEAN,
    "comment"                     VARCHAR,
    PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS MASTER_TRANSACTION."IssueFiatRequest"
(
    "id"                VARCHAR NOT NULL,
    "accountID"         VARCHAR NOT NULL,
    "transactionID"     VARCHAR NOT NULL,
    "transactionAmount" INT     NOT NULL,
    "gas"               INT,
    "status"            BOOLEAN,
    "rtcbStatus"        BOOLEAN NOT NULL,
    "ticketID"          VARCHAR,
    "comment"           VARCHAR,
    PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS MASTER_TRANSACTION."Message"
(
    "id"            VARCHAR   NOT NULL,
    "fromAccountID" VARCHAR   NOT NULL,
    "chatID"        VARCHAR   NOT NULL,
    "text"          VARCHAR   NOT NULL,
    "replyToID"     VARCHAR,
    "createdAt"     TIMESTAMP NOT NULL,
    PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS MASTER_TRANSACTION."MessageRead"
(
    "messageID" VARCHAR NOT NULL,
    "accountID" VARCHAR NOT NULL,
    "readAt"    TIMESTAMP,
    PRIMARY KEY ("messageID", "accountID")
);

CREATE TABLE IF NOT EXISTS MASTER_TRANSACTION."NegotiationFile"
(
    "id"              VARCHAR NOT NULL,
    "documentType"    VARCHAR NOT NULL,
    "fileName"        VARCHAR NOT NULL UNIQUE,
    "file"            BYTEA,
    "documentContent" VARCHAR,
    "status"          BOOLEAN,
    PRIMARY KEY ("id", "documentType")
);

CREATE TABLE IF NOT EXISTS MASTER_TRANSACTION."Notification"
(
    "id"                  VARCHAR   NOT NULL,
    "accountID"           VARCHAR   NOT NULL,
    "notificationTitle"   VARCHAR   NOT NULL,
    "notificationMessage" VARCHAR   NOT NULL,
    "time"                TIMESTAMP NOT NULL,
    "read"                BOOLEAN   NOT NULL,
    PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS MASTER_TRANSACTION."PushNotificationToken"
(
    "id"    VARCHAR NOT NULL,
    "token" VARCHAR NOT NULL,
    PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS MASTER_TRANSACTION."SessionToken"
(
    "id"               VARCHAR NOT NULL,
    "sessionTokenHash" VARCHAR NOT NULL,
    "sessionTokenTime" BIGINT  NOT NULL,
    PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS MASTER_TRANSACTION."SMSOTP"
(
    "id"         VARCHAR NOT NULL,
    "secretHash" VARCHAR NOT NULL,
    PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS MASTER_TRANSACTION."TradeActivity"
(
    "notificationID" VARCHAR NOT NULL,
    "negotiationID"    VARCHAR NOT NULL,
    PRIMARY KEY ("notificationID", "negotiationID")
);

CREATE TABLE IF NOT EXISTS MASTER_TRANSACTION."TraderInvitation"
(
    "id"                  VARCHAR NOT NULL,
    "organizationID"      VARCHAR NOT NULL,
    "inviteeEmailAddress" VARCHAR NOT NULL,
    "status"              VARCHAR NOT NULL,
    PRIMARY KEY ("id"),
    UNIQUE ("organizationID", "inviteeEmailAddress")
);

CREATE TABLE IF NOT EXISTS MASTER_TRANSACTION."WURTCBRequest"
(
    "id"      VARCHAR NOT NULL,
    "request" VARCHAR NOT NULL,
    PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS MASTER_TRANSACTION."WUSFTPFileTransaction"
(
    "payerID"              VARCHAR NOT NULL,
    "invoiceNumber"        VARCHAR NOT NULL,
    "customerFirstName"    VARCHAR NOT NULL,
    "customerLastName"     VARCHAR NOT NULL,
    "customerEmailAddress" VARCHAR NOT NULL,
    "settlementDate"       VARCHAR NOT NULL,
    "clientReceivedAmount" VARCHAR NOT NULL,
    "transactionType"      VARCHAR NOT NULL,
    "productType"          VARCHAR NOT NULL,
    "transactionReference" VARCHAR NOT NULL,
    PRIMARY KEY ("transactionReference")
);

CREATE TABLE IF NOT EXISTS MASTER_TRANSACTION."ZoneInvitation"
(
    "id"           VARCHAR NOT NULL,
    "emailAddress" VARCHAR NOT NULL UNIQUE,
    "status"       BOOLEAN,
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
ALTER TABLE BLOCKCHAIN."Zone_BC"
    ADD CONSTRAINT Zone_BC_Zone_id FOREIGN KEY ("id") REFERENCES Master."Zone" ("id");

ALTER TABLE BLOCKCHAIN_TRANSACTION."SetACL"
    ADD CONSTRAINT SetACL_ACL_hash FOREIGN KEY ("aclHash") REFERENCES BLOCKCHAIN."ACLHash_BC" ("hash");

ALTER TABLE MASTER."Account"
    ADD CONSTRAINT Account_BCAccount_address FOREIGN KEY ("accountAddress") REFERENCES BLOCKCHAIN."Account_BC" ("address");
ALTER TABLE MASTER."AccountFile"
    ADD CONSTRAINT AccountFile_Account_id FOREIGN KEY ("id") REFERENCES MASTER."Account" ("id");
ALTER TABLE MASTER."AccountKYC"
    ADD CONSTRAINT AccountKYC_Account_id FOREIGN KEY ("id") REFERENCES MASTER."Account" ("id");
ALTER TABLE MASTER."Asset"
    ADD CONSTRAINT Asset_BCAsset_PegHash FOREIGN KEY ("pegHash") REFERENCES BLOCKCHAIN."Asset_BC" ("pegHash");
ALTER TABLE MASTER."Contact"
    ADD CONSTRAINT Contact_Account_id FOREIGN KEY ("id") REFERENCES MASTER."Account" ("id");
ALTER TABLE MASTER."Identification"
    ADD CONSTRAINT Identification_Account_id FOREIGN KEY ("accountID") REFERENCES MASTER."Account" ("id");
ALTER TABLE MASTER."Negotiation"
    ADD CONSTRAINT Negotiation_BCNegotiation_negotiationID FOREIGN KEY ("negotiationID") REFERENCES BLOCKCHAIN."Negotiation_BC" ("id");
ALTER TABLE MASTER."Negotiation"
    ADD CONSTRAINT Negotiation_MasterTrader_buyerTraderID FOREIGN KEY ("buyerTraderID") REFERENCES MASTER."Trader" ("id");
ALTER TABLE MASTER."Negotiation"
    ADD CONSTRAINT Negotiation_MasterTrader_sellerTraderID FOREIGN KEY ("sellerTraderID") REFERENCES MASTER."Trader" ("id");
ALTER TABLE MASTER."Negotiation"
    ADD CONSTRAINT Negotiation_MasterAsset_assetID FOREIGN KEY ("assetID") REFERENCES MASTER."Asset" ("id");
ALTER TABLE MASTER."Organization"
    ADD CONSTRAINT Organization_Account_accountID FOREIGN KEY ("accountID") REFERENCES MASTER."Account" ("id");
ALTER TABLE MASTER."Organization"
    ADD CONSTRAINT Organization_Zone_zoneID FOREIGN KEY ("zoneID") REFERENCES MASTER."Zone" ("id");
ALTER TABLE MASTER."OrganizationBankAccountDetail"
    ADD CONSTRAINT OrganizationBankAccountDetail_Organization_id FOREIGN KEY ("id") REFERENCES MASTER."Organization" ("id");
ALTER TABLE MASTER."OrganizationKYC"
    ADD CONSTRAINT OrganizationKYC_Organization_id FOREIGN KEY ("id") REFERENCES MASTER."Organization" ("id");
ALTER TABLE MASTER."OrganizationBackgroundCheck"
    ADD CONSTRAINT OrganizationBackgroundCheck_Organization_id FOREIGN KEY ("id") REFERENCES MASTER."Organization" ("id");
ALTER TABLE MASTER."Trader"
    ADD CONSTRAINT Trader_Account_accountID FOREIGN KEY ("accountID") REFERENCES MASTER."Account" ("id");
ALTER TABLE MASTER."Trader"
    ADD CONSTRAINT Trader_Organization_organizationID FOREIGN KEY ("organizationID") REFERENCES MASTER."Organization" ("id");
ALTER TABLE MASTER."Trader"
    ADD CONSTRAINT Trader_Zone_zoneID FOREIGN KEY ("zoneID") REFERENCES MASTER."Zone" ("id");
ALTER TABLE MASTER."TraderBackgroundCheck"
    ADD CONSTRAINT TraderBackgroundCheck_Trader_id FOREIGN KEY ("id") REFERENCES MASTER."Trader" ("id");
ALTER TABLE MASTER."TraderKYC"
    ADD CONSTRAINT TraderKYC_Trader_id FOREIGN KEY ("id") REFERENCES MASTER."Trader" ("id");
ALTER TABLE MASTER."TraderRelation"
    ADD CONSTRAINT TraderRelation_Trader_fromID FOREIGN KEY ("fromID") REFERENCES MASTER."Trader" ("id");
ALTER TABLE MASTER."TraderRelation"
    ADD CONSTRAINT TraderRelation_Trader_toID FOREIGN KEY ("toID") REFERENCES MASTER."Trader" ("id");
ALTER TABLE MASTER."Zone"
    ADD CONSTRAINT Zone_Account_accountID FOREIGN KEY ("accountID") REFERENCES MASTER."Account" ("id");
ALTER TABLE MASTER."ZoneKYC"
    ADD CONSTRAINT ZoneKYC_Zone_id FOREIGN KEY ("id") REFERENCES MASTER."Zone" ("id");

ALTER TABLE MASTER_TRANSACTION."AssetFile"
    ADD CONSTRAINT AssetFile_IssueAssetRequest_id FOREIGN KEY ("id") REFERENCES MASTER_TRANSACTION."IssueAssetRequest" ("id");
ALTER TABLE MASTER_TRANSACTION."Chat"
    ADD CONSTRAINT Chat_Account_accountID FOREIGN KEY ("accountID") REFERENCES MASTER."Account" ("id");
ALTER TABLE MASTER_TRANSACTION."Message"
    ADD CONSTRAINT Message_Chat_accountIDChatWindowID FOREIGN KEY ("fromAccountID", "chatID") REFERENCES MASTER_TRANSACTION."Chat" ("accountID", "id");
ALTER TABLE MASTER_TRANSACTION."Message"
    ADD CONSTRAINT Message_Message_replyToID FOREIGN KEY ("replyToID") REFERENCES MASTER_TRANSACTION."Message" ("id");
ALTER TABLE MASTER_TRANSACTION."MessageRead"
    ADD CONSTRAINT MessageRead_Message_messageID FOREIGN KEY ("messageID") REFERENCES MASTER_TRANSACTION."Message" ("id");
ALTER TABLE MASTER_TRANSACTION."EmailOTP"
    ADD CONSTRAINT EmailOTP_Account_id FOREIGN KEY ("id") REFERENCES MASTER."Account" ("id");
ALTER TABLE MASTER_TRANSACTION."FaucetRequest"
    ADD CONSTRAINT FaucetRequest_MasterAccount_AccountID FOREIGN KEY ("accountID") REFERENCES MASTER."Account" ("id");
ALTER TABLE MASTER_TRANSACTION."IssueAssetRequest"
    ADD CONSTRAINT IssueAssetRequest_MasterAccount_AccountID FOREIGN KEY ("accountID") REFERENCES MASTER."Account" ("id");
ALTER TABLE MASTER_TRANSACTION."IssueFiatRequest"
    ADD CONSTRAINT IssueFiatRequest_MasterAccount_AccountID FOREIGN KEY ("accountID") REFERENCES MASTER."Account" ("id");
ALTER TABLE MASTER_TRANSACTION."NegotiationFile"
    ADD CONSTRAINT NegotiationFile_MasterNegotiation_id FOREIGN KEY ("id") REFERENCES MASTER."Negotiation" ("id");
ALTER TABLE MASTER_TRANSACTION."Notification"
    ADD CONSTRAINT Notification_Account_id FOREIGN KEY ("accountID") REFERENCES MASTER."Account" ("id");
ALTER TABLE MASTER_TRANSACTION."PushNotificationToken"
    ADD CONSTRAINT PushNotificationToken_Account_id FOREIGN KEY ("id") REFERENCES MASTER."Account" ("id");
ALTER TABLE MASTER_TRANSACTION."SessionToken"
    ADD CONSTRAINT SessionToken_Account_id FOREIGN KEY ("id") REFERENCES MASTER."Account" ("id");
ALTER TABLE MASTER_TRANSACTION."SMSOTP"
    ADD CONSTRAINT SMSOTP_Account_id FOREIGN KEY ("id") REFERENCES MASTER."Account" ("id");
ALTER TABLE MASTER_TRANSACTION."TradeActivity"
    ADD CONSTRAINT TradeActivity_Notification_NotificationID FOREIGN KEY ("notificationID") REFERENCES MASTER_TRANSACTION."Notification" ("id");
ALTER TABLE MASTER_TRANSACTION."TradeActivity"
    ADD CONSTRAINT TradeActivity_Negotiation_TradeRoomID FOREIGN KEY ("negotiationID") REFERENCES MASTER."Negotiation" ("id");
ALTER TABLE MASTER_TRANSACTION."TraderInvitation"
    ADD CONSTRAINT TraderInvitation_Organization_id FOREIGN KEY ("organizationID") REFERENCES MASTER."Organization" ("id");

/*Initial State*/

INSERT INTO blockchain."Account_BC" ("address", "coins", "publicKey", "accountNumber", "sequence", "dirtyBit")
VALUES ('commit17jxmr4felwgeugmeu6c4gr4vq0hmeaxlamvxjg',
        '1000',
        'commitpub1addwnpepqty3h2wuanwkjw5g2jn6p0rwcy7j7xm985t8kg8zpkp7ay83rrz2276x7qn',
        '0',
        '0',
        true);

INSERT INTO master."Account" ("id", "secretHash", "accountAddress", "language", "userType", "status")
VALUES ('main', '711213004', 'commit17jxmr4felwgeugmeu6c4gr4vq0hmeaxlamvxjg', 'en', 'GENESIS', 'NO_CONTACT');

# --- !Downs

DROP TABLE IF EXISTS BLOCKCHAIN."Account_BC" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN."ACLAccount_BC" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN."ACLHash_BC" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN."Asset_BC" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN."Fiat_BC" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN."Negotiation_BC" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN."Organization_BC" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN."TraderFeedbackHistory_BC" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN."TransactionFeedBack_BC" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN."Zone_BC" CASCADE;

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

DROP TABLE IF EXISTS MASTER."Account" CASCADE;
DROP TABLE IF EXISTS MASTER."AccountFile" CASCADE;
DROP TABLE IF EXISTS MASTER."AccountKYC" CASCADE;
DROP TABLE IF EXISTS MASTER."Asset" CASCADE;
DROP TABLE IF EXISTS MASTER."Contact" CASCADE;
DROP TABLE IF EXISTS MASTER."Identification" CASCADE;
DROP TABLE IF EXISTS MASTER."Negotiation" CASCADE;
DROP TABLE IF EXISTS MASTER."Organization" CASCADE;
DROP TABLE IF EXISTS MASTER."OrganizationKYC" CASCADE;
DROP TABLE IF EXISTS MASTER."OrganizationBackgroundCheck" CASCADE;
DROP TABLE IF EXISTS MASTER."OrganizationBankAccountDetail" CASCADE;
DROP TABLE IF EXISTS MASTER."Trader" CASCADE;
DROP TABLE IF EXISTS MASTER."TraderKYC" CASCADE;
DROP TABLE IF EXISTS MASTER."TraderBackgroundCheck" CASCADE;
DROP TABLE IF EXISTS MASTER."TraderRelation" CASCADE;
DROP TABLE IF EXISTS MASTER."ZoneKYC" CASCADE;
DROP TABLE IF EXISTS MASTER."Zone" CASCADE;

DROP TABLE IF EXISTS MASTER_TRANSACTION."AssetFile" CASCADE;
DROP TABLE IF EXISTS MASTER_TRANSACTION."Chat" CASCADE;
DROP TABLE IF EXISTS MASTER_TRANSACTION."EmailOTP" CASCADE;
DROP TABLE IF EXISTS MASTER_TRANSACTION."FaucetRequest" CASCADE;
DROP TABLE IF EXISTS MASTER_TRANSACTION."IssueAssetRequest" CASCADE;
DROP TABLE IF EXISTS MASTER_TRANSACTION."IssueFiatRequest" CASCADE;
DROP TABLE IF EXISTS MASTER_TRANSACTION."Message" CASCADE;
DROP TABLE IF EXISTS MASTER_TRANSACTION."MessageRead" CASCADE;
DROP TABLE IF EXISTS MASTER_TRANSACTION."NegotiationFile" CASCADE;
DROP TABLE IF EXISTS MASTER_TRANSACTION."Notification" CASCADE;
DROP TABLE IF EXISTS MASTER_TRANSACTION."PushNotificationToken" CASCADE;
DROP TABLE IF EXISTS MASTER_TRANSACTION."SessionToken" CASCADE;
DROP TABLE IF EXISTS MASTER_TRANSACTION."SMSOTP" CASCADE;
DROP TABLE IF EXISTS MASTER_TRANSACTION."TradeActivity" CASCADE;
DROP TABLE IF EXISTS MASTER_TRANSACTION."TraderInvitation" CASCADE;
DROP TABLE IF EXISTS MASTER_TRANSACTION."WURTCBRequest" CASCADE;
DROP TABLE IF EXISTS MASTER_TRANSACTION."WUSFTPFileTransaction" CASCADE;
DROP TABLE IF EXISTS MASTER_TRANSACTION."ZoneInvitation" CASCADE;

DROP SCHEMA IF EXISTS BLOCKCHAIN CASCADE;
DROP SCHEMA IF EXISTS BLOCKCHAIN_TRANSACTION CASCADE;
DROP SCHEMA IF EXISTS MASTER CASCADE;
DROP SCHEMA IF EXISTS MASTER_TRANSACTION CASCADE;
