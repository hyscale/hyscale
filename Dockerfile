FROM maven:3.6.1-jdk-11 as base
WORKDIR /hyscale
COPY . .
