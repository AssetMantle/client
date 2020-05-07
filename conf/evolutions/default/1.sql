# --- !Ups

CREATE SCHEMA IF NOT EXISTS BLOCKCHAIN
    AUTHORIZATION "commit";
CREATE SCHEMA IF NOT EXISTS BLOCKCHAIN_TRANSACTION
    AUTHORIZATION "commit";
CREATE SCHEMA IF NOT EXISTS MASTER
    AUTHORIZATION "commit";
CREATE SCHEMA IF NOT EXISTS MASTER_TRANSACTION
    AUTHORIZATION "commit";
CREATE SCHEMA IF NOT EXISTS WESTERN_UNION
    AUTHORIZATION "commit";
CREATE SCHEMA IF NOT EXISTS DOCUSIGN
    AUTHORIZATION "commit";


CREATE TABLE IF NOT EXISTS BLOCKCHAIN."Zone_BC"
(
    "id"                VARCHAR NOT NULL,
    "address"           VARCHAR NOT NULL,
    "dirtyBit"          BOOLEAN NOT NULL,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN."Organization_BC"
(
    "id"                VARCHAR NOT NULL,
    "address"           VARCHAR NOT NULL,
    "dirtyBit"          BOOLEAN NOT NULL,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN."Account_BC"
(
    "address"           VARCHAR NOT NULL,
    "username"          VARCHAR NOT NULL UNIQUE,
    "coins"             VARCHAR NOT NULL,
    "publicKey"         VARCHAR NOT NULL,
    "accountNumber"     VARCHAR NOT NULL,
    "sequence"          VARCHAR NOT NULL,
    "dirtyBit"          BOOLEAN NOT NULL,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("address")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN."ACLAccount_BC"
(
    "address"           VARCHAR NOT NULL,
    "zoneID"            VARCHAR NOT NULL,
    "organizationID"    VARCHAR NOT NULL,
    "aclHash"           VARCHAR NOT NULL,
    "dirtyBit"          BOOLEAN NOT NULL,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
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
    "createdBy"          VARCHAR,
    "createdOn"          TIMESTAMP,
    "createdOnTimeZone"  VARCHAR,
    "updatedBy"          VARCHAR,
    "updatedOn"          TIMESTAMP,
    "updatedOnTimeZone"  VARCHAR,
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
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("pegHash", "ownerAddress")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN."Asset_BC"
(
    "pegHash"           VARCHAR NOT NULL,
    "documentHash"      VARCHAR NOT NULL,
    "assetType"         VARCHAR NOT NULL,
    "assetQuantity"     VARCHAR NOT NULL,
    "assetPrice"        VARCHAR NOT NULL,
    "quantityUnit"      VARCHAR NOT NULL,
    "ownerAddress"      VARCHAR NOT NULL,
    "locked"            BOOLEAN NOT NULL,
    "moderated"         BOOLEAN NOT NULL,
    "takerAddress"      VARCHAR,
    "dirtyBit"          BOOLEAN NOT NULL,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
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
    "createdBy"          VARCHAR,
    "createdOn"          TIMESTAMP,
    "createdOnTimeZone"  VARCHAR,
    "updatedBy"          VARCHAR,
    "updatedOn"          TIMESTAMP,
    "updatedOnTimeZone"  VARCHAR,
    PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN."Order_BC"
(
    "id"                VARCHAR NOT NULL,
    "fiatProofHash"     VARCHAR,
    "awbProofHash"      VARCHAR,
    "dirtyBit"          BOOLEAN NOT NULL,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
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
    "createdBy"                    VARCHAR,
    "createdOn"                    TIMESTAMP,
    "createdOnTimeZone"            VARCHAR,
    "updatedBy"                    VARCHAR,
    "updatedOn"                    TIMESTAMP,
    "updatedOnTimeZone"            VARCHAR,
    PRIMARY KEY ("address")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN."TraderFeedbackHistory_BC"
(
    "address"           VARCHAR NOT NULL,
    "buyerAddress"      VARCHAR NOT NULL,
    "sellerAddress"     VARCHAR NOT NULL,
    "pegHash"           VARCHAR NOT NULL,
    "rating"            VARCHAR NOT NULL,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("address", "buyerAddress", "sellerAddress", "pegHash")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN_TRANSACTION."AddOrganization"
(
    "from"              VARCHAR NOT NULL,
    "to"                VARCHAR NOT NULL,
    "organizationID"    VARCHAR NOT NULL,
    "zoneID"            VARCHAR NOT NULL,
    "gas"               INT     NOT NULL,
    "status"            BOOLEAN,
    "txHash"            VARCHAR,
    "ticketID"          VARCHAR NOT NULL,
    "mode"              VARCHAR NOT NULL,
    "code"              VARCHAR,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("ticketID")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN_TRANSACTION."AddZone"
(
    "from"              VARCHAR NOT NULL,
    "to"                VARCHAR NOT NULL,
    "zoneID"            VARCHAR NOT NULL,
    "gas"               INT     NOT NULL,
    "status"            BOOLEAN,
    "txHash"            VARCHAR,
    "ticketID"          VARCHAR NOT NULL,
    "mode"              VARCHAR NOT NULL,
    "code"              VARCHAR,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("ticketID")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN_TRANSACTION."BuyerExecuteOrder"
(
    "from"              VARCHAR NOT NULL,
    "buyerAddress"      VARCHAR NOT NULL,
    "sellerAddress"     VARCHAR NOT NULL,
    "fiatProofHash"     VARCHAR NOT NULL,
    "pegHash"           VARCHAR NOT NULL,
    "gas"               INT     NOT NULL,
    "status"            BOOLEAN,
    "txHash"            VARCHAR,
    "ticketID"          VARCHAR NOT NULL,
    "mode"              VARCHAR NOT NULL,
    "code"              VARCHAR,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("ticketID")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN_TRANSACTION."ChangeBuyerBid"
(
    "from"              VARCHAR NOT NULL,
    "to"                VARCHAR NOT NULL,
    "bid"               INT     NOT NULL,
    "time"              INT     NOT NULL,
    "pegHash"           VARCHAR NOT NULL,
    "gas"               INT     NOT NULL,
    "status"            BOOLEAN,
    "txHash"            VARCHAR,
    "ticketID"          VARCHAR NOT NULL,
    "mode"              VARCHAR NOT NULL,
    "code"              VARCHAR,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("ticketID")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN_TRANSACTION."ChangeSellerBid"
(
    "from"              VARCHAR NOT NULL,
    "to"                VARCHAR NOT NULL,
    "bid"               INT     NOT NULL,
    "time"              INT     NOT NULL,
    "pegHash"           VARCHAR NOT NULL,
    "gas"               INT     NOT NULL,
    "status"            BOOLEAN,
    "txHash"            VARCHAR,
    "ticketID"          VARCHAR NOT NULL,
    "mode"              VARCHAR NOT NULL,
    "code"              VARCHAR,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
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
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
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
    "createdBy"          VARCHAR,
    "createdOn"          TIMESTAMP,
    "createdOnTimeZone"  VARCHAR,
    "updatedBy"          VARCHAR,
    "updatedOn"          TIMESTAMP,
    "updatedOnTimeZone"  VARCHAR,
    PRIMARY KEY ("ticketID")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN_TRANSACTION."IssueAsset"
(
    "from"              VARCHAR NOT NULL,
    "to"                VARCHAR NOT NULL,
    "documentHash"      VARCHAR NOT NULL,
    "assetType"         VARCHAR NOT NULL,
    "assetPrice"        INT     NOT NULL,
    "quantityUnit"      VARCHAR NOT NULL,
    "assetQuantity"     INT     NOT NULL,
    "moderated"         BOOLEAN NOT NULL,
    "gas"               INT     NOT NULL,
    "takerAddress"      VARCHAR,
    "status"            BOOLEAN,
    "txHash"            VARCHAR,
    "ticketID"          VARCHAR NOT NULL,
    "mode"              VARCHAR NOT NULL,
    "code"              VARCHAR,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
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
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("ticketID")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN_TRANSACTION."RedeemAsset"
(
    "from"              VARCHAR NOT NULL,
    "to"                VARCHAR NOT NULL,
    "pegHash"           VARCHAR NOT NULL,
    "gas"               INT     NOT NULL,
    "status"            BOOLEAN,
    "txHash"            VARCHAR,
    "ticketID"          VARCHAR NOT NULL,
    "mode"              VARCHAR NOT NULL,
    "code"              VARCHAR,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("ticketID")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN_TRANSACTION."RedeemFiat"
(
    "from"              VARCHAR NOT NULL,
    "to"                VARCHAR NOT NULL,
    "redeemAmount"      INT     NOT NULL,
    "gas"               INT     NOT NULL,
    "status"            BOOLEAN,
    "txHash"            VARCHAR,
    "ticketID"          VARCHAR NOT NULL,
    "mode"              VARCHAR NOT NULL,
    "code"              VARCHAR,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("ticketID")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN_TRANSACTION."ReleaseAsset"
(
    "from"              VARCHAR NOT NULL,
    "to"                VARCHAR NOT NULL,
    "pegHash"           VARCHAR NOT NULL,
    "gas"               INT     NOT NULL,
    "status"            BOOLEAN,
    "txHash"            VARCHAR,
    "ticketID"          VARCHAR NOT NULL,
    "mode"              VARCHAR NOT NULL,
    "code"              VARCHAR,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("ticketID")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN_TRANSACTION."SellerExecuteOrder"
(
    "from"              VARCHAR NOT NULL,
    "buyerAddress"      VARCHAR NOT NULL,
    "sellerAddress"     VARCHAR NOT NULL,
    "awbProofHash"      VARCHAR NOT NULL,
    "pegHash"           VARCHAR NOT NULL,
    "gas"               INT     NOT NULL,
    "status"            BOOLEAN,
    "txHash"            VARCHAR,
    "ticketID"          VARCHAR NOT NULL,
    "mode"              VARCHAR NOT NULL,
    "code"              VARCHAR,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("ticketID")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN_TRANSACTION."SendAsset"
(
    "from"              VARCHAR NOT NULL,
    "to"                VARCHAR NOT NULL,
    "pegHash"           VARCHAR NOT NULL,
    "gas"               INT     NOT NULL,
    "status"            BOOLEAN,
    "txHash"            VARCHAR,
    "ticketID"          VARCHAR NOT NULL,
    "mode"              VARCHAR NOT NULL,
    "code"              VARCHAR,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("ticketID")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN_TRANSACTION."SendCoin"
(
    "from"              VARCHAR NOT NULL,
    "to"                VARCHAR NOT NULL,
    "amount"            INT     NOT NULL,
    "gas"               INT     NOT NULL,
    "status"            BOOLEAN,
    "txHash"            VARCHAR,
    "ticketID"          VARCHAR NOT NULL,
    "mode"              VARCHAR NOT NULL,
    "code"              VARCHAR,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("ticketID")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN_TRANSACTION."SendFiat"
(
    "from"              VARCHAR NOT NULL,
    "to"                VARCHAR NOT NULL,
    "amount"            INT     NOT NULL,
    "pegHash"           VARCHAR NOT NULL,
    "gas"               INT     NOT NULL,
    "status"            BOOLEAN,
    "txHash"            VARCHAR,
    "ticketID"          VARCHAR NOT NULL,
    "mode"              VARCHAR NOT NULL,
    "code"              VARCHAR,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("ticketID")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN_TRANSACTION."SetACL"
(
    "from"              VARCHAR NOT NULL,
    "aclAddress"        VARCHAR NOT NULL,
    "organizationID"    VARCHAR NOT NULL,
    "zoneID"            VARCHAR NOT NULL,
    "aclHash"           VARCHAR NOT NULL,
    "gas"               INT     NOT NULL,
    "status"            BOOLEAN,
    "txHash"            VARCHAR,
    "ticketID"          VARCHAR NOT NULL,
    "mode"              VARCHAR NOT NULL,
    "code"              VARCHAR,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("ticketID")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN_TRANSACTION."SetBuyerFeedback"
(
    "from"              VARCHAR NOT NULL,
    "to"                VARCHAR NOT NULL,
    "pegHash"           VARCHAR NOT NULL,
    "rating"            INT     NOT NULL,
    "gas"               INT     NOT NULL,
    "status"            BOOLEAN,
    "txHash"            VARCHAR,
    "ticketID"          VARCHAR NOT NULL,
    "mode"              VARCHAR NOT NULL,
    "code"              VARCHAR,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("ticketID")
);

CREATE TABLE IF NOT EXISTS BLOCKCHAIN_TRANSACTION."SetSellerFeedback"
(
    "from"              VARCHAR NOT NULL,
    "to"                VARCHAR NOT NULL,
    "pegHash"           VARCHAR NOT NULL,
    "rating"            INT     NOT NULL,
    "gas"               INT     NOT NULL,
    "status"            BOOLEAN,
    "txHash"            VARCHAR,
    "ticketID"          VARCHAR NOT NULL,
    "mode"              VARCHAR NOT NULL,
    "code"              VARCHAR,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("ticketID")
);

CREATE TABLE IF NOT EXISTS DOCUSIGN."Envelope"
(
    "id"             VARCHAR NOT NULL,
    "envelopeID"     VARCHAR NOT NULL,
    "documentType"   VARCHAR NOT NULL,
    "status"         VARCHAR NOT NULL,
    PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS DOCUSIGN."OAuthToken"
(
    "id"               VARCHAR NOT NULL,
    "accessToken"      VARCHAR NOT NULL,
    "expiresAt"        VARCHAR NOT NULL,
    "refreshToken"     VARCHAR NOT NULL,
    PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS MASTER."Account"
(
    "id"                VARCHAR NOT NULL,
    "secretHash"        VARCHAR NOT NULL,
    "language"          VARCHAR NOT NULL,
    "userType"          VARCHAR NOT NULL,
    "partialMnemonic"   VARCHAR NOT NULL,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS MASTER."AccountFile"
(
    "id"                VARCHAR NOT NULL,
    "documentType"      VARCHAR NOT NULL,
    "fileName"          VARCHAR NOT NULL,
    "file"              BYTEA,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("id", "documentType")
);

CREATE TABLE IF NOT EXISTS MASTER."AccountKYC"
(
    "id"                VARCHAR NOT NULL,
    "documentType"      VARCHAR NOT NULL,
    "fileName"          VARCHAR NOT NULL UNIQUE,
    "file"              BYTEA,
    "status"            BOOLEAN,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("id", "documentType")
);

CREATE TABLE IF NOT EXISTS MASTER."Asset"
(
    "id"                VARCHAR NOT NULL,
    "ownerID"           VARCHAR NOT NULL,
    "pegHash"           VARCHAR UNIQUE,
    "assetType"         VARCHAR NOT NULL,
    "description"       VARCHAR NOT NULL,
    "documentHash"      VARCHAR NOT NULL UNIQUE,
    "quantity"          INT     NOT NULL,
    "quantityUnit"      VARCHAR NOT NULL,
    "price"             INT     NOT NULL,
    "moderated"         BOOLEAN NOT NULL,
    "takerID"           VARCHAR,
    "otherDetails"      VARCHAR NOT NULL,
    "status"            VARCHAR NOT NULL,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS MASTER."Email"
(
    "id"                VARCHAR NOT NULL,
    "emailAddress"      VARCHAR NOT NULL,
    "status"            BOOLEAN NOT NULL,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS MASTER."Fiat"
(
    "ownerID"           VARCHAR NOT NULL,
    "transactionID"     VARCHAR NOT NULL,
    "transactionAmount" INT     NOT NULL,
    "amountRedeemed"    INT,
    "status"            BOOLEAN,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("transactionID", "ownerID")
);

CREATE TABLE IF NOT EXISTS MASTER."Identification"
(
    "accountID"          VARCHAR NOT NULL,
    "firstName"          VARCHAR NOT NULL,
    "lastName"           VARCHAR NOT NULL,
    "dateOfBirth"        DATE    NOT NULL,
    "idNumber"           VARCHAR NOT NULL,
    "idType"             VARCHAR NOT NULL,
    "address"            VARCHAR NOT NULL,
    "completionStatus"   BOOLEAN NOT NULL,
    "verificationStatus" BOOLEAN,
    "createdBy"          VARCHAR,
    "createdOn"          TIMESTAMP,
    "createdOnTimeZone"  VARCHAR,
    "updatedBy"          VARCHAR,
    "updatedOn"          TIMESTAMP,
    "updatedOnTimeZone"  VARCHAR,
    PRIMARY KEY ("accountID")
);

CREATE TABLE IF NOT EXISTS MASTER."Mobile"
(
    "id"                VARCHAR NOT NULL,
    "mobileNumber"      VARCHAR NOT NULL,
    "status"            BOOLEAN NOT NULL,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS MASTER."Negotiation"
(
    "id"                             VARCHAR NOT NULL,
    "negotiationID"                  VARCHAR UNIQUE,
    "buyerTraderID"                  VARCHAR NOT NULL,
    "sellerTraderID"                 VARCHAR NOT NULL,
    "assetID"                        VARCHAR NOT NULL,
    "assetDescription"               VARCHAR NOT NULL,
    "price"                          INT     NOT NULL,
    "quantity"                       INT     NOT NULL,
    "quantityUnit"                   VARCHAR NOT NULL,
    "buyerAcceptedAssetDescription"  BOOLEAN NOT NULL,
    "buyerAcceptedPrice"             BOOLEAN NOT NULL,
    "buyerAcceptedQuantity"          BOOLEAN NOT NULL,
    "assetOtherDetails"              VARCHAR NOT NULL,
    "buyerAcceptedAssetOtherDetails" BOOLEAN NOT NULL,
    "time"                           INT,
    "paymentTerms"                   VARCHAR NOT NULL,
    "buyerAcceptedPaymentTerms"      BOOLEAN NOT NULL,
    "documentList"                   VARCHAR NOT NULL,
    "buyerAcceptedDocumentList"      BOOLEAN NOT NULL,
    "physicalDocumentsHandledVia"    VARCHAR,
    "chatID"                         VARCHAR UNIQUE,
    "status"                         VARCHAR NOT NULL,
    "comment"                        VARCHAR,
    "createdBy"                      VARCHAR,
    "createdOn"                      TIMESTAMP,
    "createdOnTimeZone"              VARCHAR,
    "updatedBy"                      VARCHAR,
    "updatedOn"                      TIMESTAMP,
    "updatedOnTimeZone"              VARCHAR,
    PRIMARY KEY ("id"),
    UNIQUE ("buyerTraderID", "sellerTraderID", "assetID")
);

CREATE TABLE IF NOT EXISTS MASTER."Order"
(
    "id"                VARCHAR NOT NULL,
    "orderID"           VARCHAR NOT NULL UNIQUE,
    "buyerTraderID"     VARCHAR NOT NULL,
    "sellerTraderID"    VARCHAR NOT NULL,
    "assetID"           VARCHAR NOT NULL,
    "status"            VARCHAR NOT NULL,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("id")
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
    "comment"            VARCHAR,
    "createdBy"          VARCHAR,
    "createdOn"          TIMESTAMP,
    "createdOnTimeZone"  VARCHAR,
    "updatedBy"          VARCHAR,
    "updatedOn"          TIMESTAMP,
    "updatedOnTimeZone"  VARCHAR,
    PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS MASTER."OrganizationBankAccountDetail"
(
    "id"                VARCHAR NOT NULL,
    "accountHolder"     VARCHAR NOT NULL,
    "nickName"          VARCHAR NOT NULL,
    "accountNumber"     VARCHAR NOT NULL,
    "bankName"          VARCHAR NOT NULL,
    "swiftAddress"      VARCHAR NOT NULL,
    "address"           VARCHAR NOT NULL,
    "country"           VARCHAR NOT NULL,
    "zipCode"           VARCHAR NOT NULL,
    "status"            VARCHAR,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS MASTER."OrganizationBackgroundCheck"
(
    "id"                VARCHAR NOT NULL,
    "documentType"      VARCHAR NOT NULL,
    "fileName"          VARCHAR NOT NULL UNIQUE,
    "file"              BYTEA,
    "status"            BOOLEAN,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("id", "documentType")
);

CREATE TABLE IF NOT EXISTS MASTER."OrganizationKYC"
(
    "id"                VARCHAR NOT NULL,
    "documentType"      VARCHAR NOT NULL,
    "fileName"          VARCHAR NOT NULL UNIQUE,
    "file"              BYTEA,
    "status"            BOOLEAN,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("id", "documentType")
);

CREATE TABLE IF NOT EXISTS MASTER."Trader"
(
    "id"                VARCHAR NOT NULL,
    "zoneID"            VARCHAR NOT NULL,
    "organizationID"    VARCHAR NOT NULL,
    "accountID"         VARCHAR NOT NULL UNIQUE,
    "name"              VARCHAR NOT NULL,
    "status"            BOOLEAN,
    "comment"           VARCHAR,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS MASTER."TraderBackgroundCheck"
(
    "id"                VARCHAR NOT NULL,
    "documentType"      VARCHAR NOT NULL,
    "fileName"          VARCHAR NOT NULL UNIQUE,
    "file"              BYTEA,
    "status"            BOOLEAN,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("id", "documentType")
);

CREATE TABLE IF NOT EXISTS MASTER."TraderRelation"
(
    "id"                VARCHAR NOT NULL,
    "fromID"            VARCHAR NOT NULL,
    "toID"              VARCHAR NOT NULL,
    "status"            BOOLEAN,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
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
    "createdBy"          VARCHAR,
    "createdOn"          TIMESTAMP,
    "createdOnTimeZone"  VARCHAR,
    "updatedBy"          VARCHAR,
    "updatedOn"          TIMESTAMP,
    "updatedOnTimeZone"  VARCHAR,
    PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS MASTER."ZoneKYC"
(
    "id"                VARCHAR NOT NULL,
    "documentType"      VARCHAR NOT NULL,
    "fileName"          VARCHAR NOT NULL UNIQUE,
    "file"              BYTEA,
    "status"            BOOLEAN,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("id", "documentType")
);

CREATE TABLE IF NOT EXISTS MASTER_TRANSACTION."AssetFile"
(
    "id"                  VARCHAR NOT NULL,
    "documentType"        VARCHAR NOT NULL,
    "fileName"            VARCHAR NOT NULL UNIQUE,
    "file"                BYTEA,
    "documentContentJson" VARCHAR,
    "status"              BOOLEAN,
    "createdBy"           VARCHAR,
    "createdOn"           TIMESTAMP,
    "createdOnTimeZone"   VARCHAR,
    "updatedBy"           VARCHAR,
    "updatedOn"           TIMESTAMP,
    "updatedOnTimeZone"   VARCHAR,
    PRIMARY KEY ("id", "documentType")
);

CREATE TABLE IF NOT EXISTS MASTER_TRANSACTION."Chat"
(
    "id"                VARCHAR NOT NULL,
    "accountID"         VARCHAR NOT NULL,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("id", "accountID")
);

CREATE TABLE IF NOT EXISTS MASTER_TRANSACTION."EmailOTP"
(
    "id"                VARCHAR NOT NULL,
    "secretHash"        VARCHAR NOT NULL,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS MASTER_TRANSACTION."FaucetRequest"
(
    "id"                VARCHAR NOT NULL,
    "accountID"         VARCHAR NOT NULL,
    "amount"            INT     NOT NULL,
    "gas"               INT,
    "status"            BOOLEAN,
    "ticketID"          VARCHAR,
    "comment"           VARCHAR,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS MASTER_TRANSACTION."Message"
(
    "id"                VARCHAR NOT NULL,
    "fromAccountID"     VARCHAR NOT NULL,
    "chatID"            VARCHAR NOT NULL,
    "text"              VARCHAR NOT NULL,
    "replyToID"         VARCHAR,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS MASTER_TRANSACTION."MessageRead"
(
    "messageID"         VARCHAR NOT NULL,
    "accountID"         VARCHAR NOT NULL,
    "read"            BOOLEAN NOT NULL ,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("messageID", "accountID")
);

CREATE TABLE IF NOT EXISTS MASTER_TRANSACTION."NegotiationFile"
(
    "id"                  VARCHAR NOT NULL,
    "documentType"        VARCHAR NOT NULL,
    "fileName"            VARCHAR NOT NULL UNIQUE,
    "file"                BYTEA,
    "documentContentJson" VARCHAR,
    "status"              BOOLEAN,
    "createdBy"           VARCHAR,
    "createdOn"           TIMESTAMP,
    "createdOnTimeZone"   VARCHAR,
    "updatedBy"           VARCHAR,
    "updatedOn"           TIMESTAMP,
    "updatedOnTimeZone"   VARCHAR,
    PRIMARY KEY ("id", "documentType")
);

CREATE TABLE IF NOT EXISTS MASTER_TRANSACTION."Notification"
(
    "id"                       VARCHAR NOT NULL,
    "accountID"                VARCHAR NOT NULL,
    "notificationTemplateJson" VARCHAR NOT NULL,
    "read"                     BOOLEAN NOT NULL,
    "createdBy"                VARCHAR,
    "createdOn"                TIMESTAMP,
    "createdOnTimeZone"        VARCHAR,
    "updatedBy"                VARCHAR,
    "updatedOn"                TIMESTAMP,
    "updatedOnTimeZone"        VARCHAR,
    PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS MASTER_TRANSACTION."PushNotificationToken"
(
    "id"                VARCHAR NOT NULL,
    "token"             VARCHAR NOT NULL,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS MASTER_TRANSACTION."SessionToken"
(
    "id"                VARCHAR NOT NULL,
    "sessionTokenHash"  VARCHAR NOT NULL,
    "sessionTokenTime"  BIGINT  NOT NULL,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS MASTER_TRANSACTION."SMSOTP"
(
    "id"                VARCHAR NOT NULL,
    "secretHash"        VARCHAR NOT NULL,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS MASTER_TRANSACTION."TradeActivity"
(
    "id"                        VARCHAR NOT NULL,
    "negotiationID"             VARCHAR NOT NULL,
    "tradeActivityTemplateJson" VARCHAR NOT NULL,
    "read"                      BOOLEAN NOT NULL,
    "createdBy"                 VARCHAR,
    "createdOn"                 TIMESTAMP,
    "createdOnTimeZone"         VARCHAR,
    "updatedBy"                 VARCHAR,
    "updatedOn"                 TIMESTAMP,
    "updatedOnTimeZone"         VARCHAR,
    PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS MASTER_TRANSACTION."TraderInvitation"
(
    "id"                  VARCHAR NOT NULL,
    "organizationID"      VARCHAR NOT NULL,
    "inviteeEmailAddress" VARCHAR NOT NULL,
    "status"              VARCHAR NOT NULL,
    "createdBy"           VARCHAR,
    "createdOn"           TIMESTAMP,
    "createdOnTimeZone"   VARCHAR,
    "updatedBy"           VARCHAR,
    "updatedOn"           TIMESTAMP,
    "updatedOnTimeZone"   VARCHAR,
    PRIMARY KEY ("id"),
    UNIQUE ("organizationID", "inviteeEmailAddress")
);

CREATE TABLE IF NOT EXISTS MASTER_TRANSACTION."ZoneInvitation"
(
    "id"                VARCHAR NOT NULL,
    "emailAddress"      VARCHAR NOT NULL UNIQUE,
    "accountID"         VARCHAR UNIQUE,
    "status"            BOOLEAN,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS WESTERN_UNION."FiatRequest"
(
    "id"                VARCHAR NOT NULL,
    "traderID"          VARCHAR NOT NULL,
    "transactionAmount" INT     NOT NULL,
    "status"            VARCHAR NOT NULL,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS WESTERN_UNION."RTCB"
(
    "id"                VARCHAR   NOT NULL,
    "reference"         VARCHAR   NOT NULL,
    "externalReference" VARCHAR   NOT NULL,
    "invoiceNumber"     VARCHAR   NOT NULL,
    "buyerBusinessId"   VARCHAR   NOT NULL,
    "buyerFirstName"    VARCHAR   NOT NULL,
    "buyerLastName"     VARCHAR   NOT NULL,
    "createdDate"       TIMESTAMP NOT NULL,
    "lastUpdatedDate"   TIMESTAMP NOT NULL,
    "status"            VARCHAR   NOT NULL,
    "dealType"          VARCHAR   NOT NULL,
    "paymentTypeId"     VARCHAR   NOT NULL,
    "paidOutAmount"     INT       NOT NULL,
    "requestSignature"  VARCHAR   NOT NULL,
    "createdBy"         VARCHAR,
    "createdOn"         TIMESTAMP,
    "createdOnTimeZone" VARCHAR,
    "updatedBy"         VARCHAR,
    "updatedOn"         TIMESTAMP,
    "updatedOnTimeZone" VARCHAR,
    PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS WESTERN_UNION."SFTPFileTransaction"
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
    "createdBy"            VARCHAR,
    "createdOn"            TIMESTAMP,
    "createdOnTimeZone"    VARCHAR,
    "updatedBy"            VARCHAR,
    "updatedOn"            TIMESTAMP,
    "updatedOnTimeZone"    VARCHAR,
    PRIMARY KEY ("transactionReference")
);

ALTER TABLE BLOCKCHAIN."Account_BC"
    ADD CONSTRAINT Account_BC_Master_Account_username FOREIGN KEY ("username") REFERENCES MASTER."Account" ("id");
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

ALTER TABLE DOCUSIGN."Envelope"
    ADD CONSTRAINT Envelope_MasterNegotiation_id FOREIGN KEY ("id") REFERENCES MASTER."Negotiation" ("id");

ALTER TABLE MASTER."AccountFile"
    ADD CONSTRAINT AccountFile_Account_id FOREIGN KEY ("id") REFERENCES MASTER."Account" ("id");
ALTER TABLE MASTER."AccountKYC"
    ADD CONSTRAINT AccountKYC_Account_id FOREIGN KEY ("id") REFERENCES MASTER."Account" ("id");
ALTER TABLE MASTER."Asset"
    ADD CONSTRAINT Asset_BCAsset_PegHash FOREIGN KEY ("pegHash") REFERENCES BLOCKCHAIN."Asset_BC" ("pegHash");
ALTER TABLE MASTER."Asset"
    ADD CONSTRAINT Asset_Trader_TakerID FOREIGN KEY ("takerID") REFERENCES MASTER."Trader" ("id");
ALTER TABLE MASTER."Email"
    ADD CONSTRAINT Email_Account_id FOREIGN KEY ("id") REFERENCES MASTER."Account" ("id");
ALTER TABLE MASTER."Mobile"
    ADD CONSTRAINT Mobile_Account_id FOREIGN KEY ("id") REFERENCES MASTER."Account" ("id");
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
ALTER TABLE MASTER."Order"
    ADD CONSTRAINT Order_MasterNegotiation_id FOREIGN KEY ("id") REFERENCES MASTER."Negotiation" ("id");
ALTER TABLE MASTER."Order"
    ADD CONSTRAINT Order_BCOrder_orderID FOREIGN KEY ("orderID") REFERENCES BLOCKCHAIN."Order_BC" ("id");
ALTER TABLE MASTER."Order"
    ADD CONSTRAINT Order_MasterTrader_buyerTraderID FOREIGN KEY ("buyerTraderID") REFERENCES MASTER."Trader" ("id");
ALTER TABLE MASTER."Order"
    ADD CONSTRAINT Order_MasterTrader_sellerTraderID FOREIGN KEY ("sellerTraderID") REFERENCES MASTER."Trader" ("id");
ALTER TABLE MASTER."Order"
    ADD CONSTRAINT Order_MasterAsset_assetID FOREIGN KEY ("assetID") REFERENCES MASTER."Asset" ("id");
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
ALTER TABLE MASTER."TraderRelation"
    ADD CONSTRAINT TraderRelation_Trader_fromID FOREIGN KEY ("fromID") REFERENCES MASTER."Trader" ("id");
ALTER TABLE MASTER."TraderRelation"
    ADD CONSTRAINT TraderRelation_Trader_toID FOREIGN KEY ("toID") REFERENCES MASTER."Trader" ("id");
ALTER TABLE MASTER."Zone"
    ADD CONSTRAINT Zone_Account_accountID FOREIGN KEY ("accountID") REFERENCES MASTER."Account" ("id");
ALTER TABLE MASTER."ZoneKYC"
    ADD CONSTRAINT ZoneKYC_Zone_id FOREIGN KEY ("id") REFERENCES MASTER."Zone" ("id");

ALTER TABLE MASTER_TRANSACTION."AssetFile"
    ADD CONSTRAINT AssetFile_Asset_id FOREIGN KEY ("id") REFERENCES MASTER."Asset" ("id");
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
    ADD CONSTRAINT TradeActivity_Negotiation_TradeRoomID FOREIGN KEY ("negotiationID") REFERENCES MASTER."Negotiation" ("id");
ALTER TABLE MASTER_TRANSACTION."TraderInvitation"
    ADD CONSTRAINT TraderInvitation_Organization_id FOREIGN KEY ("organizationID") REFERENCES MASTER."Organization" ("id");
ALTER TABLE MASTER_TRANSACTION."ZoneInvitation"
    ADD CONSTRAINT ZoneInvitation_Account_accountID FOREIGN KEY ("accountID") REFERENCES MASTER."Account" ("id");

ALTER TABLE WESTERN_UNION."FiatRequest"
    ADD CONSTRAINT FiatRequest_Trader_traderID FOREIGN KEY ("traderID") REFERENCES MASTER."Trader" ("id");
ALTER TABLE WESTERN_UNION."RTCB"
    ADD CONSTRAINT RTCB_FiatRequest_externalReference FOREIGN KEY ("externalReference") REFERENCES WESTERN_UNION."FiatRequest" ("id");

/*Initial State*/

CREATE OR REPLACE FUNCTION public.insert_or_update_log() RETURNS TRIGGER AS
$$
BEGIN
    IF new."createdOn" IS NULL THEN
        new."createdOn" = current_timestamp;;
        new."createdOnTimeZone" = current_setting('TIMEZONE');;
        new."createdBy" = current_user;;
    ELSE
        new."updatedOn" = current_timestamp;;
        new."updatedOnTimeZone" = current_setting('TIMEZONE');;
        new."updatedBy" = current_user;;
    END IF;;
    RETURN NEW;;
END;;
$$ LANGUAGE PLPGSQL;

CREATE TRIGGER Account_BC_Log BEFORE INSERT OR UPDATE ON BLOCKCHAIN."Account_BC" FOR EACH ROW EXECUTE PROCEDURE public.insert_or_update_log();
CREATE TRIGGER ACLAccount_BC_Log BEFORE INSERT OR UPDATE ON BLOCKCHAIN."ACLAccount_BC" FOR EACH ROW EXECUTE PROCEDURE public.insert_or_update_log();
CREATE TRIGGER ACLHash_BC_Log BEFORE INSERT OR UPDATE ON BLOCKCHAIN."ACLHash_BC" FOR EACH ROW EXECUTE PROCEDURE public.insert_or_update_log();
CREATE TRIGGER Asset_BC_Log BEFORE INSERT OR UPDATE ON BLOCKCHAIN."Asset_BC" FOR EACH ROW EXECUTE PROCEDURE public.insert_or_update_log();
CREATE TRIGGER Fiat_BC_Log BEFORE INSERT OR UPDATE ON BLOCKCHAIN."Fiat_BC" FOR EACH ROW EXECUTE PROCEDURE public.insert_or_update_log();
CREATE TRIGGER Negotiation_BC_Log BEFORE INSERT OR UPDATE ON BLOCKCHAIN."Negotiation_BC" FOR EACH ROW EXECUTE PROCEDURE public.insert_or_update_log();
CREATE TRIGGER Order_BC_Log BEFORE INSERT OR UPDATE ON BLOCKCHAIN."Order_BC" FOR EACH ROW EXECUTE PROCEDURE public.insert_or_update_log();
CREATE TRIGGER Organization_BC_Log BEFORE INSERT OR UPDATE ON BLOCKCHAIN."Organization_BC" FOR EACH ROW EXECUTE PROCEDURE public.insert_or_update_log();
CREATE TRIGGER TraderFeedbackHistory_BC_Log BEFORE INSERT OR UPDATE ON BLOCKCHAIN."TraderFeedbackHistory_BC" FOR EACH ROW EXECUTE PROCEDURE public.insert_or_update_log();
CREATE TRIGGER TransactionFeedBack_BC_Log BEFORE INSERT OR UPDATE ON BLOCKCHAIN."TransactionFeedBack_BC" FOR EACH ROW EXECUTE PROCEDURE public.insert_or_update_log();
CREATE TRIGGER Zone_BC_Log BEFORE INSERT OR UPDATE ON BLOCKCHAIN."Zone_BC" FOR EACH ROW EXECUTE PROCEDURE public.insert_or_update_log();

CREATE TRIGGER AddOrganization_Log BEFORE INSERT OR UPDATE ON BLOCKCHAIN_TRANSACTION."AddOrganization" FOR EACH ROW EXECUTE PROCEDURE public.insert_or_update_log();
CREATE TRIGGER AddZone_Log BEFORE INSERT OR UPDATE ON BLOCKCHAIN_TRANSACTION."AddZone" FOR EACH ROW EXECUTE PROCEDURE public.insert_or_update_log();
CREATE TRIGGER BuyerExecuteOrder_Log BEFORE INSERT OR UPDATE ON BLOCKCHAIN_TRANSACTION."BuyerExecuteOrder" FOR EACH ROW EXECUTE PROCEDURE public.insert_or_update_log();
CREATE TRIGGER ChangeBuyerBid_Log BEFORE INSERT OR UPDATE ON BLOCKCHAIN_TRANSACTION."ChangeBuyerBid" FOR EACH ROW EXECUTE PROCEDURE public.insert_or_update_log();
CREATE TRIGGER ChangeSellerBid_Log BEFORE INSERT OR UPDATE ON BLOCKCHAIN_TRANSACTION."ChangeSellerBid" FOR EACH ROW EXECUTE PROCEDURE public.insert_or_update_log();
CREATE TRIGGER ConfirmBuyerBid_Log BEFORE INSERT OR UPDATE ON BLOCKCHAIN_TRANSACTION."ConfirmBuyerBid" FOR EACH ROW EXECUTE PROCEDURE public.insert_or_update_log();
CREATE TRIGGER ConfirmSellerBid_Log BEFORE INSERT OR UPDATE ON BLOCKCHAIN_TRANSACTION."ConfirmSellerBid" FOR EACH ROW EXECUTE PROCEDURE public.insert_or_update_log();
CREATE TRIGGER IssueAsset_Log BEFORE INSERT OR UPDATE ON BLOCKCHAIN_TRANSACTION."IssueAsset" FOR EACH ROW EXECUTE PROCEDURE public.insert_or_update_log();
CREATE TRIGGER IssueFiat_Log BEFORE INSERT OR UPDATE ON BLOCKCHAIN_TRANSACTION."IssueFiat" FOR EACH ROW EXECUTE PROCEDURE public.insert_or_update_log();
CREATE TRIGGER RedeemAsset_Log BEFORE INSERT OR UPDATE ON BLOCKCHAIN_TRANSACTION."RedeemAsset" FOR EACH ROW EXECUTE PROCEDURE public.insert_or_update_log();
CREATE TRIGGER RedeemFiat_Log BEFORE INSERT OR UPDATE ON BLOCKCHAIN_TRANSACTION."RedeemFiat" FOR EACH ROW EXECUTE PROCEDURE public.insert_or_update_log();
CREATE TRIGGER ReleaseAsset_Log BEFORE INSERT OR UPDATE ON BLOCKCHAIN_TRANSACTION."ReleaseAsset" FOR EACH ROW EXECUTE PROCEDURE public.insert_or_update_log();
CREATE TRIGGER SellerExecuteOrder_Log BEFORE INSERT OR UPDATE ON BLOCKCHAIN_TRANSACTION."SellerExecuteOrder" FOR EACH ROW EXECUTE PROCEDURE public.insert_or_update_log();
CREATE TRIGGER SendAsset_Log BEFORE INSERT OR UPDATE ON BLOCKCHAIN_TRANSACTION."SendAsset" FOR EACH ROW EXECUTE PROCEDURE public.insert_or_update_log();
CREATE TRIGGER SendCoin_Log BEFORE INSERT OR UPDATE ON BLOCKCHAIN_TRANSACTION."SendCoin" FOR EACH ROW EXECUTE PROCEDURE public.insert_or_update_log();
CREATE TRIGGER SendFiat_Log BEFORE INSERT OR UPDATE ON BLOCKCHAIN_TRANSACTION."SendFiat" FOR EACH ROW EXECUTE PROCEDURE public.insert_or_update_log();
CREATE TRIGGER SetACL_Log BEFORE INSERT OR UPDATE ON BLOCKCHAIN_TRANSACTION."SetACL" FOR EACH ROW EXECUTE PROCEDURE public.insert_or_update_log();
CREATE TRIGGER SetBuyerFeedback_Log BEFORE INSERT OR UPDATE ON BLOCKCHAIN_TRANSACTION."SetBuyerFeedback" FOR EACH ROW EXECUTE PROCEDURE public.insert_or_update_log();
CREATE TRIGGER SetSellerFeedback_Log BEFORE INSERT OR UPDATE ON BLOCKCHAIN_TRANSACTION."SetSellerFeedback" FOR EACH ROW EXECUTE PROCEDURE public.insert_or_update_log();

CREATE TRIGGER Envelope_Log BEFORE INSERT OR UPDATE ON DOCUSIGN."Envelope" FOR EACH ROW EXECUTE PROCEDURE public.insert_or_update_log();
CREATE TRIGGER OAuthToken_Log BEFORE INSERT OR UPDATE ON DOCUSIGN."OAuthToken" FOR EACH ROW EXECUTE PROCEDURE public.insert_or_update_log();

CREATE TRIGGER Account_Log BEFORE INSERT OR UPDATE ON MASTER."Account" FOR EACH ROW EXECUTE PROCEDURE public.insert_or_update_log();
CREATE TRIGGER AccountFile_Log BEFORE INSERT OR UPDATE ON MASTER."AccountFile" FOR EACH ROW EXECUTE PROCEDURE public.insert_or_update_log();
CREATE TRIGGER AccountKYC_Log BEFORE INSERT OR UPDATE ON MASTER."AccountKYC" FOR EACH ROW EXECUTE PROCEDURE public.insert_or_update_log();
CREATE TRIGGER Asset_Log BEFORE INSERT OR UPDATE ON MASTER."Asset" FOR EACH ROW EXECUTE PROCEDURE public.insert_or_update_log();
CREATE TRIGGER Email_Log BEFORE INSERT OR UPDATE ON MASTER."Email" FOR EACH ROW EXECUTE PROCEDURE public.insert_or_update_log();
CREATE TRIGGER Fiat_Log BEFORE INSERT OR UPDATE ON MASTER."Fiat" FOR EACH ROW EXECUTE PROCEDURE public.insert_or_update_log();
CREATE TRIGGER Identification_Log BEFORE INSERT OR UPDATE ON MASTER."Identification" FOR EACH ROW EXECUTE PROCEDURE public.insert_or_update_log();
CREATE TRIGGER Mobile_Log BEFORE INSERT OR UPDATE ON MASTER."Mobile" FOR EACH ROW EXECUTE PROCEDURE public.insert_or_update_log();
CREATE TRIGGER Negotiation_Log BEFORE INSERT OR UPDATE ON MASTER."Negotiation" FOR EACH ROW EXECUTE PROCEDURE public.insert_or_update_log();
CREATE TRIGGER Order_Log BEFORE INSERT OR UPDATE ON MASTER."Order" FOR EACH ROW EXECUTE PROCEDURE public.insert_or_update_log();
CREATE TRIGGER Organization_Log BEFORE INSERT OR UPDATE ON MASTER."Organization" FOR EACH ROW EXECUTE PROCEDURE public.insert_or_update_log();
CREATE TRIGGER OrganizationBackgroundCheck_Log BEFORE INSERT OR UPDATE ON MASTER."OrganizationBackgroundCheck" FOR EACH ROW EXECUTE PROCEDURE public.insert_or_update_log();
CREATE TRIGGER OrganizationBankAccountDetail_Log BEFORE INSERT OR UPDATE ON MASTER."OrganizationBankAccountDetail" FOR EACH ROW EXECUTE PROCEDURE public.insert_or_update_log();
CREATE TRIGGER OrganizationKYC_Log BEFORE INSERT OR UPDATE ON MASTER."OrganizationKYC" FOR EACH ROW EXECUTE PROCEDURE public.insert_or_update_log();
CREATE TRIGGER Trader_Log BEFORE INSERT OR UPDATE ON MASTER."Trader" FOR EACH ROW EXECUTE PROCEDURE public.insert_or_update_log();
CREATE TRIGGER TraderBackgroundCheck_Log BEFORE INSERT OR UPDATE ON MASTER."TraderBackgroundCheck" FOR EACH ROW EXECUTE PROCEDURE public.insert_or_update_log();
CREATE TRIGGER TraderRelation_Log BEFORE INSERT OR UPDATE ON MASTER."TraderRelation" FOR EACH ROW EXECUTE PROCEDURE public.insert_or_update_log();
CREATE TRIGGER Zone_Log BEFORE INSERT OR UPDATE ON MASTER."Zone" FOR EACH ROW EXECUTE PROCEDURE public.insert_or_update_log();
CREATE TRIGGER ZoneKYC_Log BEFORE INSERT OR UPDATE ON MASTER."ZoneKYC" FOR EACH ROW EXECUTE PROCEDURE public.insert_or_update_log();

CREATE TRIGGER AssetFile_Log BEFORE INSERT OR UPDATE ON MASTER_TRANSACTION."AssetFile" FOR EACH ROW EXECUTE PROCEDURE public.insert_or_update_log();
CREATE TRIGGER Chat_Log BEFORE INSERT OR UPDATE ON MASTER_TRANSACTION."Chat" FOR EACH ROW EXECUTE PROCEDURE public.insert_or_update_log();
CREATE TRIGGER EmailOTP_Log BEFORE INSERT OR UPDATE ON MASTER_TRANSACTION."EmailOTP" FOR EACH ROW EXECUTE PROCEDURE public.insert_or_update_log();
CREATE TRIGGER FaucetRequest_Log BEFORE INSERT OR UPDATE ON MASTER_TRANSACTION."FaucetRequest" FOR EACH ROW EXECUTE PROCEDURE public.insert_or_update_log();
CREATE TRIGGER Message_Log BEFORE INSERT OR UPDATE ON MASTER_TRANSACTION."Message" FOR EACH ROW EXECUTE PROCEDURE public.insert_or_update_log();
CREATE TRIGGER MessageRead_Log BEFORE INSERT OR UPDATE ON MASTER_TRANSACTION."MessageRead" FOR EACH ROW EXECUTE PROCEDURE public.insert_or_update_log();
CREATE TRIGGER NegotiationFile_Log BEFORE INSERT OR UPDATE ON MASTER_TRANSACTION."NegotiationFile" FOR EACH ROW EXECUTE PROCEDURE public.insert_or_update_log();
CREATE TRIGGER Notification_Log BEFORE INSERT OR UPDATE ON MASTER_TRANSACTION."Notification" FOR EACH ROW EXECUTE PROCEDURE public.insert_or_update_log();
CREATE TRIGGER PushNotificationToken_Log BEFORE INSERT OR UPDATE ON MASTER_TRANSACTION."PushNotificationToken" FOR EACH ROW EXECUTE PROCEDURE public.insert_or_update_log();
CREATE TRIGGER SessionToken_Log BEFORE INSERT OR UPDATE ON MASTER_TRANSACTION."SessionToken" FOR EACH ROW EXECUTE PROCEDURE public.insert_or_update_log();
CREATE TRIGGER SMSOTP_Log BEFORE INSERT OR UPDATE ON MASTER_TRANSACTION."SMSOTP" FOR EACH ROW EXECUTE PROCEDURE public.insert_or_update_log();
CREATE TRIGGER TradeActivity_Log BEFORE INSERT OR UPDATE ON MASTER_TRANSACTION."TradeActivity" FOR EACH ROW EXECUTE PROCEDURE public.insert_or_update_log();
CREATE TRIGGER TraderInvitation_Log BEFORE INSERT OR UPDATE ON MASTER_TRANSACTION."TraderInvitation" FOR EACH ROW EXECUTE PROCEDURE public.insert_or_update_log();
CREATE TRIGGER ZoneInvitation_Log BEFORE INSERT OR UPDATE ON MASTER_TRANSACTION."ZoneInvitation" FOR EACH ROW EXECUTE PROCEDURE public.insert_or_update_log();

CREATE TRIGGER FiatRequest_Log BEFORE INSERT OR UPDATE ON WESTERN_UNION."FiatRequest" FOR EACH ROW EXECUTE PROCEDURE public.insert_or_update_log();
CREATE TRIGGER RTCB_Log BEFORE INSERT OR UPDATE ON WESTERN_UNION."RTCB" FOR EACH ROW EXECUTE PROCEDURE public.insert_or_update_log();
CREATE TRIGGER SFTPFileTransaction_Log BEFORE INSERT OR UPDATE ON WESTERN_UNION."SFTPFileTransaction" FOR EACH ROW EXECUTE PROCEDURE public.insert_or_update_log();

INSERT INTO master."Account" ("id", "secretHash", "partialMnemonic", "language", "userType")
VALUES ('main',
        '711213004',
        '["fluid","cereal","trash","miracle","casino","menu","true","method","exhaust","pen","fiber","rural","grape","purchase","rather","table","omit","youth","gain","cage","erase"]',
        'en',
        'GENESIS');

INSERT INTO blockchain."Account_BC" ("address", "username", "coins", "publicKey", "accountNumber", "sequence",
                                     "dirtyBit")
VALUES ('commit17jxmr4felwgeugmeu6c4gr4vq0hmeaxlamvxjg',
        'main',
        '1000',
        'commitpub1addwnpepqty3h2wuanwkjw5g2jn6p0rwcy7j7xm985t8kg8zpkp7ay83rrz2276x7qn',
        '0',
        '0',
        true);

# --- !Downs

DROP TABLE IF EXISTS BLOCKCHAIN."Account_BC" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN."ACLAccount_BC" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN."ACLHash_BC" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN."Asset_BC" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN."Fiat_BC" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN."Negotiation_BC" CASCADE;
DROP TABLE IF EXISTS BLOCKCHAIN."Order_BC" CASCADE;
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

DROP TABLE IF EXISTS DOCUSIGN."Envelope" CASCADE;
DROP TABLE IF EXISTS DOCUSIGN."OAuthToken" CASCADE;

DROP TABLE IF EXISTS MASTER."Account" CASCADE;
DROP TABLE IF EXISTS MASTER."AccountFile" CASCADE;
DROP TABLE IF EXISTS MASTER."AccountKYC" CASCADE;
DROP TABLE IF EXISTS MASTER."Asset" CASCADE;
DROP TABLE IF EXISTS MASTER."Email" CASCADE;
DROP TABLE IF EXISTS MASTER."Fiat" CASCADE;
DROP TABLE IF EXISTS MASTER."Identification" CASCADE;
DROP TABLE IF EXISTS MASTER."Mobile" CASCADE;
DROP TABLE IF EXISTS MASTER."Negotiation" CASCADE;
DROP TABLE IF EXISTS MASTER."Order" CASCADE;
DROP TABLE IF EXISTS MASTER."Organization" CASCADE;
DROP TABLE IF EXISTS MASTER."OrganizationBackgroundCheck" CASCADE;
DROP TABLE IF EXISTS MASTER."OrganizationBankAccountDetail" CASCADE;
DROP TABLE IF EXISTS MASTER."OrganizationKYC" CASCADE;
DROP TABLE IF EXISTS MASTER."Trader" CASCADE;
DROP TABLE IF EXISTS MASTER."TraderBackgroundCheck" CASCADE;
DROP TABLE IF EXISTS MASTER."TraderRelation" CASCADE;
DROP TABLE IF EXISTS MASTER."Zone" CASCADE;
DROP TABLE IF EXISTS MASTER."ZoneKYC" CASCADE;

DROP TABLE IF EXISTS MASTER_TRANSACTION."AssetFile" CASCADE;
DROP TABLE IF EXISTS MASTER_TRANSACTION."Chat" CASCADE;
DROP TABLE IF EXISTS MASTER_TRANSACTION."EmailOTP" CASCADE;
DROP TABLE IF EXISTS MASTER_TRANSACTION."FaucetRequest" CASCADE;
DROP TABLE IF EXISTS MASTER_TRANSACTION."Message" CASCADE;
DROP TABLE IF EXISTS MASTER_TRANSACTION."MessageRead" CASCADE;
DROP TABLE IF EXISTS MASTER_TRANSACTION."NegotiationFile" CASCADE;
DROP TABLE IF EXISTS MASTER_TRANSACTION."Notification" CASCADE;
DROP TABLE IF EXISTS MASTER_TRANSACTION."PushNotificationToken" CASCADE;
DROP TABLE IF EXISTS MASTER_TRANSACTION."SessionToken" CASCADE;
DROP TABLE IF EXISTS MASTER_TRANSACTION."SMSOTP" CASCADE;
DROP TABLE IF EXISTS MASTER_TRANSACTION."TradeActivity" CASCADE;
DROP TABLE IF EXISTS MASTER_TRANSACTION."TraderInvitation" CASCADE;
DROP TABLE IF EXISTS MASTER_TRANSACTION."ZoneInvitation" CASCADE;

DROP TABLE IF EXISTS WESTERN_UNION."FiatRequest" CASCADE;
DROP TABLE IF EXISTS WESTERN_UNION."RTCB" CASCADE;
DROP TABLE IF EXISTS WESTERN_UNION."SFTPFileTransaction" CASCADE;

DROP SCHEMA IF EXISTS BLOCKCHAIN CASCADE;
DROP SCHEMA IF EXISTS BLOCKCHAIN_TRANSACTION CASCADE;
DROP SCHEMA IF EXISTS DOCUSIGN CASCADE;
DROP SCHEMA IF EXISTS MASTER CASCADE;
DROP SCHEMA IF EXISTS MASTER_TRANSACTION CASCADE;
DROP SCHEMA IF EXISTS WESTERN_UNION CASCADE;
