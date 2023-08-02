#!/bin/bash

printf "#########################################\n"
java -version
printf "#########################################\n"

MAVEN_VERSION=3.3.9
USER_HOME_DIR="/root"
USER_NAME=webtrekk
GROUP_NAME=webtrekk
EXLUDE_SHOP_EXAMPLE="--projects=!com.mapp.intelligence.tracking:shop-example,!com.mapp.intelligence.tracking:mapp-intelligence-java-tracking"

if [ "${TEST_VERSION}" != "latest" ]; then
    TEST_VERSION="15"
fi

APT_GET="$(type -t apt-get)"
YUM="$(type -t yum)"

echo "install curl, tar, bash, make, sudo, which and maven"
if [ "${APT_GET}" = "file" ]; then
    apt-get update && apt-get install -y --no-install-recommends curl tar bash make
elif [ "${YUM}" = "file" ]; then
    yum install -y curl tar bash make sudo which
else
    microdnf install curl tar bash make sudo which
fi

mkdir -p /usr/share/maven
curl -fsSL https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/${MAVEN_VERSION}/apache-maven-${MAVEN_VERSION}-bin.tar.gz | tar -xzC /usr/share/maven --strip-components=1
ln -s /usr/share/maven/bin/mvn /usr/bin/mvn

echo "add environment variables"
export MAVEN_HOME=/usr/share/maven
export MAVEN_CONFIG="${USER_HOME_DIR}/.m2"
export MAVEN_OPTS="-XX:+TieredCompilation -XX:TieredStopAtLevel=1"
export HOME="/home/${USER_NAME}"

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

echo "write java version in 'java-version.txt'"
INSTALLED_JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}')
if [ "${APT_GET}" = "file" ]; then
    su -c "echo ${INSTALLED_JAVA_VERSION} > ./java-version.txt" -m "${USER_NAME}"
else
    sudo -su ${USER_NAME} echo "${INSTALLED_JAVA_VERSION}" > ./java-version.txt
fi

echo "ckeck for java security manager"
SECURITY_MANAGER_COMMAND="-Dwebtrekk.security.manager.key=foo"
SECURITY_MANAGER_VALUE="-Dwebtrekk.security.manager.value=bar"
if [[ "$INSTALLED_JAVA_VERSION" > "16" ]]; then
    SECURITY_MANAGER_COMMAND="-Dwebtrekk.security.manager.key=java.security.manager"
    SECURITY_MANAGER_VALUE="-Dwebtrekk.security.manager.value=allow"
fi
export SECURITY_MANAGER="$SECURITY_MANAGER_COMMAND $SECURITY_MANAGER_VALUE"

if [ "${BUILD_TYPE}" = "test" ]; then
    echo "test java lib"

    if [ "${APT_GET}" = "file" ]; then
        echo "mvn ${SECURITY_MANAGER} ${EXLUDE_SHOP_EXAMPLE} clean test jacoco:report"
        su -c "mvn ${SECURITY_MANAGER} ${EXLUDE_SHOP_EXAMPLE} clean test jacoco:report" -m "${USER_NAME}"
    else
        sudo -su ${USER_NAME} mvn ${SECURITY_MANAGER} ${EXLUDE_SHOP_EXAMPLE} clean test jacoco:report
    fi

    exit $?
fi

echo "install maven dependency"
if [ "${APT_GET}" = "file" ]; then
    su -c "mvn -T 1C clean install" -m "${USER_NAME}"
else
    sudo -su ${USER_NAME} mvn -T 1C clean install
fi

if [ "${BUILD_TYPE}" = "release" ]; then
    echo "release java lib"

    if [ "${APT_GET}" = "file" ]; then
        su -c "mvn release:clean" -m "${USER_NAME}"
        su -c "mvn --batch-mode release:prepare -Dresume=false" -m "${USER_NAME}"
        su -c "mvn release:perform" -m "${USER_NAME}"
        su -c "git push --tags" -m "${USER_NAME}"
        su -c "git push origin master" -m "${USER_NAME}"
    else
        sudo -su ${USER_NAME} mvn release:clean
        sudo -su ${USER_NAME} mvn --batch-mode release:prepare -Dresume=false
        sudo -su ${USER_NAME} mvn release:perform
        sudo -su ${USER_NAME} git push --tags
        sudo -su ${USER_NAME} git push origin master
    fi

    exit $?
fi

echo "test, build and deploy java lib"
if [ "${APT_GET}" = "file" ]; then
    su -c "mvn clean checkstyle:check package -B" -m "${USER_NAME}"
    su -c "cp ./tracking/target/mapp-intelligence-java-tracking.jar ./dist/mapp-intelligence-java-tracking.jar" -m "${USER_NAME}"
    su -c "cp ./cronjob/target/mapp-intelligence-java-cronjob.jar ./dist/mapp-intelligence-java-cronjob.jar" -m "${USER_NAME}"
else
    sudo -su ${USER_NAME} mvn clean checkstyle:check package -B
    sudo -su ${USER_NAME} cp ./tracking/target/mapp-intelligence-java-tracking.jar ./dist/mapp-intelligence-java-tracking.jar
    sudo -su ${USER_NAME} cp ./cronjob/target/mapp-intelligence-java-tracking.jar ./dist/mapp-intelligence-java-tracking.jar
fi

exit 0
