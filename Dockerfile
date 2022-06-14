# syntax=docker/dockerfile:latest
FROM openjdk:11-jdk as build
SHELL [ "/bin/bash", "-cx" ]
RUN --mount=type=cache,target=/var/lib/cache/ \
  --mount=type=cache,target=/var/lib/apt/cache \
  echo "deb https://repo.scala-sbt.org/scalasbt/debian all main" | tee /etc/apt/sources.list.d/sbt.list; \
  echo "deb https://repo.scala-sbt.org/scalasbt/debian /" | tee /etc/apt/sources.list.d/sbt_old.list; \
  curl -sL "https://keyserver.ubuntu.com/pks/lookup?op=get&search=0x2EE0EA64E40A89B84B2DF73499E82A75642AC823" | apt-key add; \
  apt-get update; \
  apt-get install -y sbt unzip
WORKDIR /app
COPY . .
RUN --mount=type=cache,target=/root/.sbt \
  --mount=type=cache,target=/root/.cache \
  --mount=type=cache,target=/root/.ivy2 \
  sbt dist

FROM openjdk:11-jre-slim as extract
SHELL [ "/bin/bash", "-cx" ]
WORKDIR /app
RUN --mount=type=cache,target=/var/lib/apt/cache \
  --mount=type=cache,target=/var/lib/cache \
  apt update; \
  apt install unzip -y
COPY --from=build /app/target/universal/ /app
RUN cp *.zip assetmantle.zip; \
  ls -alt; \
  unzip assetmantle.zip; \
  ls -alt; \
  rm *.zip; \
  ls -alt; \
  mv assetmantle* assetmantle; \
  ls -alt

FROM scratch as dist
WORKDIR /
COPY --from=build /app/target/universal/assetmantle*.zip /assetmantle.zip

FROM openjdk:11-jdk as usql
WORKDIR /workspace
ARG UNSQL_VERSION=0.10.0
RUN wget https://github.com/xo/usql/releases/download/v${UNSQL_VERSION}/usql_static-${UNSQL_VERSION}-linux-amd64.tar.bz2; \
  cp *tar.bz2 usql.tar.bz2; \
  tar -xf usql.tar.bz2; \
  ls -alt

FROM openjdk:11-jre-slim
LABEL org.opencontainers.image.title=explorer
LABEL org.opencontainers.image.base.name=openjdk-11-jre-slim
LABEL org.opencontainers.image.description=explorer
LABEL org.opencontainers.image.source=https://github.com/assetmantle/client
LABEL org.opencontainers.image.documentation=https://github.com/assetmantle/client
WORKDIR /assetmantle
WORKDIR /
COPY entrypoint.sh /entrypoint.sh
COPY --from=usql /workspace/usql_static /usr/local/bin/usql
COPY --from=extract /app/assetmantle /assetmantle/explorer
CMD [ "/entrypoint.sh" ]
