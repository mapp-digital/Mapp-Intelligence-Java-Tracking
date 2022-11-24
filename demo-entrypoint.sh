#!/bin/bash

#MAVEN_VERSION=3.3.9
USER_HOME_DIR="/root"
USER_NAME=webtrekk
GROUP_NAME=webtrekk

# install packages
apt-get update && apt-get install -y --no-install-recommends curl tar bash make openjdk-8-jdk maven

export JAVA_COMPILE_VERSION="8"
export JAVA_TEST_COMPILE_VERSION="8"
export MAVEN_HOME=/usr/share/maven
export MAVEN_CONFIG="$USER_HOME_DIR/.m2"
export MAVEN_OPTS="-XX:+TieredCompilation -XX:TieredStopAtLevel=1"

# create user and user group and login as this user
addgroup --gid "${GROUP_ID}" "${USER_NAME}" || true
adduser --disabled-password --gecos '' --uid "${USER_ID}" --gid "${GROUP_ID}" "${USER_NAME}" || true
groupadd "${GROUP_NAME}" || true
usermod -aG "${GROUP_NAME}" "${USER_NAME}" || true

# goto /app directory
cd /app  || exit 1

# install maven dependency
su -c "mvn -T 1C clean install" -m "${USER_NAME}"

# goto shop example
cd ./shop-example || exit 1

# remove old shop example
rm -rf /usr/local/tomcat/webapps/ROOT

# copy cronjob file
cp ./../dist/mapp-intelligence-java-cronjob.jar /usr/local/tomcat/temp/
cp ./../dist/mapp-intelligence-java-tracking.jar /usr/local/tomcat/temp/

# copy shop example to tomcat root
cp ./target/shop-example-0.0.1.war /usr/local/tomcat/webapps/
mv /usr/local/tomcat/webapps/shop-example-0.0.1.war /usr/local/tomcat/webapps/ROOT.war

export JAVA_OPTS="-Xms1024m -Xmx1024m -XX:MaxPermSize=256m"

catalina.sh run
