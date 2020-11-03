#!/bin/bash

printf "#########################################\n"
java -version
printf "#########################################\n"

MAVEN_VERSION=3.3.9
USER_HOME_DIR="/root"
USER_NAME=webtrekk
GROUP_NAME=webtrekk

if [ "${TEST_VERSION}" != "latest" ]; then
    TEST_VERSION="14"
fi

APT_GET="$(type -t apt-get)"
YUM="$(type -t yum)"

echo "install curl, tar, bash, make, sudo, which and maven"
if [ "${APT_GET}" = "file" ]; then
    apt-get update && apt-get install -y --no-install-recommends curl tar bash make
elif [ "${YUM}" = "file" ]; then
    yum install -y curl tar bash make sudo which
else
    microdnf update && microdnf install curl tar bash make sudo which
fi

mkdir -p /usr/share/maven
curl -fsSL http://apache.osuosl.org/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz | tar -xzC /usr/share/maven --strip-components=1
ln -s /usr/share/maven/bin/mvn /usr/bin/mvn

echo "add environment variables"
export MAVEN_HOME=/usr/share/maven
export MAVEN_CONFIG="${USER_HOME_DIR}/.m2"
export MAVEN_OPTS="-XX:+TieredCompilation -XX:TieredStopAtLevel=1"

echo "create user and user group"
if [ "${APT_GET}" = "file" ]; then
    addgroup --gid "${GROUP_ID}" "${USER_NAME}" || true
    adduser --disabled-password --gecos '' --uid "${USER_ID}" --gid "${GROUP_ID}" "${USER_NAME}" || true
    groupadd "${GROUP_NAME}" || true
else
    groupadd -g "${GROUP_ID}" "${USER_NAME}" || true
    useradd -u "${USER_ID}" -g "${GROUP_ID}" "${USER_NAME}" || true
fi
usermod -aG "${GROUP_NAME}" "${USER_NAME}" || true

# goto /app directory
cd /app || exit 1

echo "install maven dependency"
if [ "${APT_GET}" = "file" ]; then
    su -c "mvn -T 1C install && rm -rf target" -m "${USER_NAME}"
else
    sudo -su ${USER_NAME} mvn -T 1C install && rm -rf target
fi

if [ "${BUILD_TYPE}" != "build" ]; then
    echo "test java lib"

    cd ./tracking || exit 1
    if [ "${APT_GET}" = "file" ]; then
        su -c "mvn clean test jacoco:report" -m "${USER_NAME}"
    else
        sudo -su ${USER_NAME} mvn clean test jacoco:report
    fi

    cd ./../cronjob || exit 1
    if [ "${APT_GET}" = "file" ]; then
        su -c "mvn clean test jacoco:report" -m "${USER_NAME}"
    else
        sudo -su ${USER_NAME} mvn clean test jacoco:report
    fi

    exit 0
fi

echo "test, build and deploy java lib"
cd ./tracking || exit 1
if [ "${APT_GET}" = "file" ]; then
    su -c "mvn clean checkstyle:check package -B" -m "${USER_NAME}"
    su -c "mv ./target/tracking-0.0.1.jar ./../dist/mapp-intelligence-java-tracking-0.0.1.jar" -m "${USER_NAME}"
else
    sudo -su ${USER_NAME}  mvn clean checkstyle:check package -B
    sudo -su ${USER_NAME}  mv ./target/tracking-0.0.1.jar ./../dist/mapp-intelligence-java-tracking-0.0.1.jar
fi

cd ./../cronjob || exit 1
if [ "${APT_GET}" = "file" ]; then
    su -c "mvn clean checkstyle:check package -B" -m "${USER_NAME}"
    su -c "mv ./target/cronjob-0.0.1.jar ./../dist/mapp-intelligence-java-cronjob-0.0.1.jar" -m "${USER_NAME}"
else
    sudo -su ${USER_NAME}  mvn clean checkstyle:check package -B
    sudo -su ${USER_NAME}  mv ./target/cronjob-0.0.1.jar ./../dist/mapp-intelligence-java-cronjob-0.0.1.jar
fi
