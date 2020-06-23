-- Create database and user scripts. To be run before evolutions are started.

CREATE USER "persistence" WITH PASSWORD 'persistence';

CREATE DATABASE "persistence" WITH OWNER = "persistence";

ALTER USER "persistence" SET SEARCH_PATH = "$user", BLOCKCHAIN, BLOCKCHAIN_TRANSACTION, MASTER, MASTER_TRANSACTION, WESTERN_UNION, DOCUSIGN, MEMBER_CHECK;