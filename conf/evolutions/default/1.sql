# --- !Ups


CREATE SCHEMA IF NOT EXISTS BLOCKCHAIN
  AUTHORIZATION comdex;

CREATE SCHEMA IF NOT EXISTS FACTORY
  AUTHORIZATION comdex;

CREATE SCHEMA IF NOT EXISTS MASTER
  AUTHORIZATION comdex;

CREATE SCHEMA IF NOT EXISTS BUSINESSTXN
  AUTHORIZATION comdex;

CREATE TABLE IF NOT EXISTS master.login (
  userName       VARCHAR      NOT NULL,
  address        varchar      NOT NULL UNIQUE,
  zoneID         INT          NOT NULL,
  organizationID varchar      NOT NULL,
  passwordHash   varchar      NOT NULL,
  phone          varchar      NOT NULL UNIQUE,
  email          varchar      NOT NULL UNIQUE,
  PRIMARY KEY (userName)
);

CREATE TABLE IF NOT EXISTS master.Zone (
  ZoneID   INT          NOT NULL,
  ZoneName VARCHAR NOT NULL,
  Country  VARCHAR NOT NULL,
  City     VARCHAR NOT NULL,
  PRIMARY KEY (ZoneID)
);

CREATE TABLE IF NOT EXISTS blockchain.Account (
  Address      varchar NOT NULL,
  PublicKey    varchar NOT NULL,
  Coins        INT     NOT NULL,
  FiatPegHash  varchar NOT NULL,
  AssetPegHash varchar NOT NULL,
  PRIMARY KEY (Address)
);

CREATE TABLE IF NOT EXISTS blockchain.Asset (
  PegHash       varchar      NOT NULL,
  DocumentHash  varchar      NOT NULL,
  AssetType     VARCHAR NOT NULL,
  AssetPrice    INT          NOT NULL,
  AssetQuantity INT          NOT NULL,
  QuantityUnit  VARCHAR NOT NULL,
  Address       varchar      NOT NULL,
  AssetLocked   BOOLEAN      NOT NULL,
  PRIMARY KEY (PegHash)
);

CREATE TABLE IF NOT EXISTS blockchain.Fiat (
  PegHash           varchar NOT NULL,
  TransactionID     varchar NOT NULL,
  TransactionAmount INT     NOT NULL,
  RedeemedAmount    INT     NOT NULL,
  PRIMARY KEY (PegHash)
);

CREATE TABLE IF NOT EXISTS master.Organization (
  OrganizationID   varchar      NOT NULL,
  OrganizationName VARCHAR NOT NULL,
  Address          VARCHAR NOT NULL,
  Zipcode          INT          NOT NULL,
  Phone            varchar      NOT NULL,
  Email            varchar      NOT NULL,
  IsComdexUser     BOOLEAN      NOT NULL,
  CreatedAt        TIMESTAMP    NOT NULL,
  Abbreviation     VARCHAR NOT NULL,
  PRIMARY KEY (OrganizationID)
);

CREATE TABLE IF NOT EXISTS master.BankDetails (
  OrganizationID varchar NOT NULL UNIQUE,
  BankAccNumber  VARCHAR NOT NULL,
  AccountHolder  VARCHAR NOT NULL,
  BankName       VARCHAR NOT NULL,
  NickName       VARCHAR NOT NULL,
  Country        VARCHAR NOT NULL,
  Swift          VARCHAR NOT NULL,
  Address        VARCHAR NOT NULL,
  ZIP            VARCHAR NOT NULL,
  Status         VARCHAR NOT NULL
);

CREATE TABLE IF NOT EXISTS master.OrganizationKYC (
  OrganizationID          varchar NOT NULL UNIQUE ,
  OrganizationKYCPath     VARCHAR NOT NULL,
  Url                     VARCHAR NOT NULL,
  DocStatus               VARCHAR NOT NULL,
  OrganizationKYCComments VARCHAR NOT NULL,
  DocType                 VARCHAR NOT NULL,
  Status                  BOOLEAN      NOT NULL,
  ApprovedBy              VARCHAR NOT NULL,
  ApprovedAt              TIMESTAMP    NOT NULL,
  FileType                VARCHAR NOT NULL,
  FileName                VARCHAR NOT NULL,
  CreatedAt               TIMESTAMP    NOT NULL,
  UpdatedAt               TIMESTAMP    NOT NULL,
  DeletedAt               TIMESTAMP    NOT NULL
);

CREATE TABLE IF NOT EXISTS master.UserKYC (
  userName        varchar      NOT NULL UNIQUE ,
  UserKYCPath     VARCHAR NOT NULL,
  Url             VARCHAR NOT NULL,
  DocStatus       VARCHAR NOT NULL,
  UserKYCComments VARCHAR NOT NULL,
  DocType         VARCHAR NOT NULL,
  Status          BOOLEAN      NOT NULL,
  ApprovedBy      VARCHAR NOT NULL,
  ApprovedAt      TIMESTAMP    NOT NULL,
  FileType        VARCHAR NOT NULL,
  FileName        VARCHAR NOT NULL,
  CreatedAt       TIMESTAMP    NOT NULL,
  UpdatedAt       TIMESTAMP    NOT NULL,
  DeletedAt       TIMESTAMP    NOT NULL
);

CREATE TABLE IF NOT EXISTS blockchain.Owners (
  PegHash      varchar NOT NULL UNIQUE,
  OwnerAddress varchar NOT NULL,
  Amount       INT     NOT NULL,
  PRIMARY KEY (OwnerAddress)
);

CREATE TABLE IF NOT EXISTS blockchain.Order (
  OrderReferenceID varchar      NOT NULL,
  NegotiationID   varchar      NOT NULL,
  BuyerAddress    varchar      NOT NULL,
  SellerAddress   varchar      NOT NULL,
  AssetPegHash    varchar      NOT NULL,
  Amount          INT          NOT NULL,
  FiatProofHash   VARCHAR NOT NULL,
  AWBProofHash    VARCHAR NOT NULL,
  ApprovedAt      TIMESTAMP    NOT NULL,
  ApprovedBy      VARCHAR NOT NULL,
  PaymentReceipt  varchar      NOT NULL,
  Executed        BOOLEAN      NOT NULL,
  OrderComments   varchar      NOT NULL,
  PRIMARY KEY (OrderReferenceID)
);

CREATE TABLE IF NOT EXISTS blockchain.Negotiation (
  NegotiationReferenceID varchar NOT NULL,
  NegotiationID          varchar NOT NULL,
  BuyerAddress           varchar NOT NULL,
  SellerAddress          varchar NOT NULL,
  AssetPegHash           varchar NOT NULL,
  Bid                    INT     NOT NULL,
  NegotiationTime        INT     NOT NULL,
  PRIMARY KEY (NegotiationReferenceID)
);
--
-- ALTER TABLE master.login
--   ADD CONSTRAINT Login_fk0 FOREIGN KEY (ZoneID) REFERENCES master.Zone (zoneID);
--
-- ALTER TABLE master.login
--   ADD CONSTRAINT Login_fk1 FOREIGN KEY (OrganizationID) REFERENCES master.Organization (organizationID);

ALTER TABLE blockchain.Account
  ADD CONSTRAINT Account_fk0 FOREIGN KEY (Address) REFERENCES master.login (address);

ALTER TABLE blockchain.Account
  ADD CONSTRAINT Account_fk1 FOREIGN KEY (FiatPegHash) REFERENCES blockchain.Fiat (PegHash);

ALTER TABLE blockchain.Account
  ADD CONSTRAINT Account_fk2 FOREIGN KEY (AssetPegHash) REFERENCES blockchain.Asset (PegHash);

ALTER TABLE blockchain.Asset
  ADD CONSTRAINT Asset_fk0 FOREIGN KEY (Address) REFERENCES blockchain.Account (Address);

ALTER TABLE master.BankDetails
  ADD CONSTRAINT BankDetails_fk0 FOREIGN KEY (OrganizationID) REFERENCES master.Organization (OrganizationID);

ALTER TABLE master.OrganizationKYC
  ADD CONSTRAINT OrganizationKYC_fk0 FOREIGN KEY (OrganizationID) REFERENCES master.Organization (OrganizationID);

ALTER TABLE master.UserKYC
  ADD CONSTRAINT UserKYC_fk0 FOREIGN KEY (userName) REFERENCES master.Login (userName);

ALTER TABLE blockchain.Owners
  ADD CONSTRAINT Owners_fk0 FOREIGN KEY (PegHash) REFERENCES blockchain.Fiat (PegHash);

ALTER TABLE blockchain.Order
  ADD CONSTRAINT Order_fk1 FOREIGN KEY (BuyerAddress) REFERENCES blockchain.Account (Address);

ALTER TABLE blockchain.Order
  ADD CONSTRAINT Order_fk2 FOREIGN KEY (SellerAddress) REFERENCES blockchain.Account (Address);

ALTER TABLE blockchain.Order
  ADD CONSTRAINT Order_fk3 FOREIGN KEY (AssetPegHash) REFERENCES blockchain.Asset (PegHash);

ALTER TABLE blockchain.Negotiation
  ADD CONSTRAINT Negotiation_fk0 FOREIGN KEY (BuyerAddress) REFERENCES blockchain.Account (Address);

ALTER TABLE blockchain.Negotiation
  ADD CONSTRAINT Negotiation_fk1 FOREIGN KEY (SellerAddress) REFERENCES blockchain.Account (Address);

# --- !Downs

DROP TABLE IF EXISTS master.Login CASCADE;

DROP TABLE IF EXISTS master.Zone CASCADE;

DROP TABLE IF EXISTS blockchain.Account CASCADE;

DROP TABLE IF EXISTS blockchain.Asset CASCADE;

DROP TABLE IF EXISTS blockchain.Fiat CASCADE;

DROP TABLE IF EXISTS master.Organization CASCADE;

DROP TABLE IF EXISTS master.BankDetails CASCADE;

DROP TABLE IF EXISTS master.OrganizationKYC CASCADE;

DROP TABLE IF EXISTS master.UserKYC CASCADE;

DROP TABLE IF EXISTS blockchain.Owners CASCADE;

DROP TABLE IF EXISTS blockchain.Order CASCADE;

DROP TABLE IF EXISTS blockchain.Negotiation CASCADE;


DROP SCHEMA IF EXISTS BLOCKCHAIN CASCADE;

DROP SCHEMA IF EXISTS FACTORY CASCADE;

DROP SCHEMA IF EXISTS MASTER CASCADE;

DROP SCHEMA IF EXISTS BUSINESSTXN CASCADE;


DROP TABLE IF EXISTS testmaster.logindata CASCADE;
DROP SCHEMA IF EXISTS TESTMASTER CASCADE;

