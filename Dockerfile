FROM maven:3.6.1-jdk-11 as base
WORKDIR /hyscale
COPY . .
RUN  mvn clean install

FROM openjdk:16-slim-buster
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
