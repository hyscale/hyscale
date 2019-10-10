FROM tomcat:8.5.0-jre8

ARG VERSION
ENV APPVERSION $VERSION
COPY artifacts/sampleapp/SampleApp.war /usr/local/tomcat/webapps/
