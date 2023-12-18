# syntax=docker/dockerfile:latest
ARG BUILD_IMAGE=adoptopenjdk:11-jdk-hotspot
ARG JRE_IMAGE=adoptopenjdk:11-jre-hotspot
ARG GITHUB_ACCESS_TOKEN
ARG GITHUB_USER
RUN git config --global credential.helper '!f() { echo "username=${GITHUB_USERNAME}"; echo "password=${GITHUB_ACCESS_TOKEN}"; }; f'


FROM ${BUILD_IMAGE} as build
SHELL [ "/bin/bash", "-cx" ]
WORKDIR /tmp
# Debugging commands
RUN pwd && ls -al
RUN apt update; apt install -yqq git curl wget ssh; \
  mkdir -p -m 0700 ~/.ssh && ssh-keyscan github.com >> ~/.ssh/known_hosts
COPY ./project/build.properties ./
COPY <<EOF /root/.ssh/id_rsa
PRIV KEY TO FETCH THE REPO
EOF
RUN --mount=type=secret,id=git,target=/root/.ssh/id_rsa \
  SBT_VERSION=$(grep 'sbt.version' build.properties | cut -d'=' -f2); \
  curl -sLo - https://github.com/sbt/sbt/releases/download/v$SBT_VERSION/sbt-$SBT_VERSION.tgz | tar -xvzf -; \
  mv sbt/bin/* /usr/local/bin/; \
  chmod 0400 -cR /root/.ssh/id_rsa; \
  rm -rf /tmp/*

WORKDIR /app
ENV JAVA_OPTS="-Xms4G -Xmx8G -Xss6M -XX:ReservedCodeCacheSize=256M -XX:+CMSClassUnloadingEnabled -XX:+UseG1GC"
ENV JVM_OPTS="-Xms4G -Xmx8G -Xss6M -XX:ReservedCodeCacheSize=256M -XX:+CMSClassUnloadingEnabled -XX:+UseG1GC"
ENV SBT_OPTS="-Xms4G -Xmx8G -Xss6M -XX:ReservedCodeCacheSize=256M -XX:+CMSClassUnloadingEnabled -XX:+UseG1GC"
ARG APP_VERSION
ENV APP_VERSION=$APP_VERSION
COPY . .
RUN --mount=type=cache,target=/root/.sbt \
  --mount=type=cache,target=/root/.cache \
  --mount=type=cache,target=/root/.ivy2 \
  sbt dist; \
  echo $APP_VERSION
# Debugging commands
RUN pwd && ls -al

FROM ${JRE_IMAGE} as extract
SHELL [ "/bin/bash", "-cx" ]
WORKDIR /app
# Debugging commands before 'sbt dist'
RUN pwd && ls -al
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

FROM ${JRE_IMAGE}
ARG APP_VERSION
ENV APP_VERSION=$APP_VERSION
LABEL org.opencontainers.image.title=explorer
LABEL org.opencontainers.image.base.name=${JRE_IMAGE}
LABEL org.opencontainers.image.description=explorer
LABEL org.opencontainers.image.source=https://github.com/assetmantle/client
LABEL org.opencontainers.image.documentation=https://github.com/assetmantle/client
WORKDIR /
COPY --from=extract /app/assetmantle /explorer
CMD [ "/explorer/bin/assetmantle" ]
