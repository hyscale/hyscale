FROM maven:3.6.3-jdk-11 as base
ARG GPG_KEY=GPG_KEY_TO_PUBLISH_JAR
ARG MAVEN_USER=MAVEN_USER_TO_PUBLISH_JAR
ARG MAVEN_PASS=MAVEN_PASSWORD_TO_PUBLISH_JAR
ARG GPG_PASS=GPG_PASSPHRASE_TO_PUBLISH_JAR
ARG MAVEN_EXEC="clean install"
ENV server-id=ossrh
ENV server-username=$MAVEN_USER
ENV server-password=$MAVEN_PASS
ENV MAVEN_USERNAME=$MAVEN_USER
ENV MAVEN_PASSWORD=$MAVEN_PASS
ENV GPG_KEY_ENV=$GPG_KEY
ENV GPG_PASSPHRASE=$GPG_PASS
ENV MAVEN_EXEC_ENV=$MAVEN_EXEC
WORKDIR /hyscale
RUN apt-get install gpg -y \
    && apt-get clean
COPY . .
RUN mkdir -p ~/.gnupg/ && echo "$GPG_KEY_ENV"| base64 --decode > ~/.gnupg/private.key \
    && gpg --batch --import ~/.gnupg/private.key \
    && mvn $MAVEN_EXEC_ENV

FROM openjdk:11.0.12-jre-slim-buster
ENV DOCKERVERSION=18.06.2-ce
RUN apt update \
    && apt-get install -y --no-install-recommends wget \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*
RUN wget --no-verbose https://download.docker.com/linux/static/stable/x86_64/docker-$DOCKERVERSION.tgz -O /tmp/docker.tgz \
    && tar xf /tmp/docker.tgz -C /tmp/ \
    && cp /tmp/docker/docker /usr/bin/ \
    && rm -rf /tmp/docker.tgz /tmp/docker
COPY --from=base /hyscale/_dist/artifacts/hyscale.jar  /usr/local/bin/
WORKDIR /hyscale/app
HEALTHCHECK NONE
# TODO: specify heap max/min, metaspace
ENTRYPOINT ["java","-Xms216m","-Xmx512m","-Djdk.tls.client.protocols=TLSv1.2","-Duser.home=/hyscale","-jar","/usr/local/bin/hyscale.jar"]
CMD ["--help"]
