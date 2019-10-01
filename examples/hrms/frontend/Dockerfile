FROM tomcat:8.5.0-jre8
COPY artifacts/hrms.war /usr/local/tomcat/webapps/
ADD tomcat-configure.sh hyscale/init_scripts/
RUN chmod -R 755 hyscale/init_scripts/
RUN hyscale/init_scripts/tomcat-configure.sh
