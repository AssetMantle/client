-- Create database and user scripts. To be run before evolutions are started.


CREATE USER "commitCentral" WITH PASSWORD 'commitCentral';

CREATE DATABASE "commitCentral" WITH OWNER = "commitCentral";

ALTER USER "commitCentral" SET SEARCH_PATH = "$user", BLOCKCHAIN, BLOCKCHAIN_TRANSACTION, MASTER, MASTER_TRANSACTION;



