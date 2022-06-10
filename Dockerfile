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

FROM openjdk:11-jre-slim as zip
SHELL [ "/bin/bash", "-cx" ]
WORKDIR /app
RUN --mount=type=cache,target=/var/lib/apt/cache \
  --mount=type=cache,target=/var/lib/cache \
  apt update; \
  apt install unzip -y
COPY --from=build /app/target/universal/ /app
RUN cp *.zip assetmantle.zip; \
  ls -alt

FROM zip as extract
RUN unzip assetmantle.zip; \
  ls -alt; \
  rm *.zip; \
  ls -alt; \
  mv assetmantle* assetmantle; \
  ls -alt

FROM scratch as dist
WORKDIR /
COPY --from=zip /app/assetmantle.zip /assetmantle.zip

FROM openjdk:11-jre-slim
LABEL org.opencontainers.image.title=explorer
LABEL org.opencontainers.image.base.name=openjdk-11-jre-slim
LABEL org.opencontainers.image.description=explorer
LABEL org.opencontainers.image.source=https://github.com/assetmantle/client
LABEL org.opencontainers.image.documentation=https://github.com/assetmantle/client
WORKDIR /assetmantle
WORKDIR /
COPY --from=extract /app/assetmantle /assetmantle/explorer
ENTRYPOINT [ "/assetmantle/explorer/bin/assetmantle" ]
