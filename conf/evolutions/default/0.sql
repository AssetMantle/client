-- Create database and user scripts. To be run before evolutions are started.

CREATE USER "commit" WITH PASSWORD 'commit';

CREATE DATABASE "commit" WITH OWNER = "commit";

ALTER USER "commit" SET SEARCH_PATH = "$user", BLOCKCHAIN, BLOCKCHAIN_TRANSACTION, KEY_BASE, MASTER, MASTER_TRANSACTION, WESTERN_UNION, DOCUSIGN, MEMBER_CHECK;