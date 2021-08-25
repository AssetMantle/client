FROM postgres:latest
ENV POSTGRES_USER="persistence"
ENV POSTGRES_PASSWORD="persistence"
ENV POSTGRES_DB="persistence"
COPY init.sql /docker-entrypoint-initdb.d/init.sql