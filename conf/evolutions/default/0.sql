-- Create database and user scripts. To be run before evolutions are started.

CREATE USER comdex WITH PASSWORD 'comdex';

CREATE DATABASE comdex WITH OWNER = comdex;

ALTER USER comdex SET SEARCH_PATH = "$user", BLOCKCHAIN, BLOCKCHAIN_TRANSACTION, MASTER, MASTER_TRANSACTION;



