#!/bin/bash -x

if [[ ! -z "${DB_MIGRATION}" && "${DB_MIGRATION}" == "true" ]]; then
  DB_MIGRATION_PATH='/assetmantle/explorer/conf/evolutions/default/0.sql'
  echo "Migrating ${DB_MIGRATION_PATH}"
  usql "postgres://${POSTGRESQL_USERNAME}:${POSTGRESQL_PASSWORD}@${POSTGRES_HOST}:${POSTGRES_PORT:-"5432"}/?sslmode=disable" <"${DB_MIGRATION_PATH}"
fi

/assetmantle/explorer/bin/assetmantle
