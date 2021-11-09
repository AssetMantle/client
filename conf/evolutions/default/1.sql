-- Create database and user scripts. To be run before evolutions are started.
# --- !Ups

CREATE USER "persistence" WITH PASSWORD 'persistence';

CREATE DATABASE "persistence" WITH OWNER = "persistence";

ALTER USER "persistence" SET SEARCH_PATH = "$user", BLOCKCHAIN, BLOCKCHAIN_TRANSACTION, KEY_BASE, MASTER, MASTER_TRANSACTION, WESTERN_UNION, DOCUSIGN, MEMBER_CHECK;

# --- !Downs

drop database "persistence";
drop user "persistence";