-- Create database and user scripts. To be run before evolutions are started.

CREATE USER "assetMantle" WITH PASSWORD 'assetMantle';

CREATE DATABASE "assetMantle" WITH OWNER = "assetMantle";

ALTER USER "assetMantle" SET SEARCH_PATH = "$user", ANALYTICS, BLOCKCHAIN, KEY_BASE, MASTER_TRANSACTION, ARCHIVE;

