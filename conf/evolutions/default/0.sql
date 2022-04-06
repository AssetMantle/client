-- Create database and user scripts. To be run before evolutions are started.

CREATE USER "assetMantle" WITH PASSWORD 'assetMantle';

CREATE DATABASE "assetMantle" WITH OWNER = "assetMantle";

ALTER USER "assetMantle" SET SEARCH_PATH = "$user", BLOCKCHAIN, BLOCKCHAIN_TRANSACTION, KEY_BASE, MASTER, MASTER_TRANSACTION, WESTERN_UNION, DOCUSIGN, MEMBER_CHECK;