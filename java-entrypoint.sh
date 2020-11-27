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
    microdnf install curl tar bash make sudo which
fi

mkdir -p /usr/share/maven
curl -fsSL http://apache.osuosl.org/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz | tar -xzC /usr/share/maven --strip-components=1
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

echo "install maven dependency"
if [ "${APT_GET}" = "file" ]; then
    su -c "mvn -T 1C ${EXLUDE_SHOP_EXAMPLE} install" -m "${USER_NAME}"
else
    sudo -su ${USER_NAME} mvn -T 1C "${EXLUDE_SHOP_EXAMPLE}" install
fi

if [ "${BUILD_TYPE}" = "test" ]; then
    echo "test java lib"

    if [ "${APT_GET}" = "file" ]; then
        su -c "mvn ${EXLUDE_SHOP_EXAMPLE} clean test jacoco:report" -m "${USER_NAME}"
    else
        sudo -su ${USER_NAME} mvn "${EXLUDE_SHOP_EXAMPLE}" clean test jacoco:report
    fi

    exit 0
fi

if [ "${BUILD_TYPE}" = "release" ]; then
    echo "release java lib"

    if [ "${APT_GET}" = "file" ]; then
        su -c "mvn ${EXLUDE_SHOP_EXAMPLE} release:clean" -m "${USER_NAME}"
        su -c "mvn ${EXLUDE_SHOP_EXAMPLE} --batch-mode release:prepare -Dresume=false" -m "${USER_NAME}"
#        su -c "mvn ${EXLUDE_SHOP_EXAMPLE} release:perform" -m "${USER_NAME}"
#        su -c "git push --tags" -m "${USER_NAME}"
#        su -c "git push origin master" -m "${USER_NAME}"
    else
        sudo -su ${USER_NAME} mvn release:clean
        sudo -su ${USER_NAME} mvn --batch-mode release:prepare -Dresume=false
        sudo -su ${USER_NAME} mvn release:perform
        sudo -su ${USER_NAME} git push --tags
        sudo -su ${USER_NAME} git push origin master
    fi

#    cd tracking  || exit 1
#    if [ "${APT_GET}" = "file" ]; then
#        su -c "mvn release:clean" -m "${USER_NAME}"
#        su -c "mvn --batch-mode release:prepare -Dresume=false" -m "${USER_NAME}"
#        su -c "mvn release:perform" -m "${USER_NAME}"
#        su -c "git push --tags" -m "${USER_NAME}"
#        su -c "git push origin master" -m "${USER_NAME}"
#    else
#        sudo -su ${USER_NAME} mvn release:clean
#        sudo -su ${USER_NAME} mvn --batch-mode release:prepare -Dresume=false
#        sudo -su ${USER_NAME} mvn release:perform
#        sudo -su ${USER_NAME} git push --tags
#        sudo -su ${USER_NAME} git push origin master
#    fi
#
#    cd ../cronjob  || exit 1
#    if [ "${APT_GET}" = "file" ]; then
#        su -c "mvn release:clean" -m "${USER_NAME}"
#        su -c "mvn --batch-mode release:prepare -Dresume=false" -m "${USER_NAME}"
#        su -c "mvn release:perform" -m "${USER_NAME}"
#        su -c "git push --tags" -m "${USER_NAME}"
#        su -c "git push origin master" -m "${USER_NAME}"
#    else
#        sudo -su ${USER_NAME} mvn release:clean
#        sudo -su ${USER_NAME} mvn --batch-mode release -Dresume=false
#        sudo -su ${USER_NAME} mvn release:perform
#        sudo -su ${USER_NAME} git push --tags
#        sudo -su ${USER_NAME} git push origin master
#    fi

    exit 0
fi

echo "test, build and deploy java lib"
if [ "${APT_GET}" = "file" ]; then
    su -c "mvn ${EXLUDE_SHOP_EXAMPLE} clean checkstyle:check package -B" -m "${USER_NAME}"
    su -c "cp ./tracking/target/mapp-intelligence-java-tracking.jar ./dist/mapp-intelligence-java-tracking.jar" -m "${USER_NAME}"
    su -c "cp ./cronjob/target/mapp-intelligence-java-cronjob.jar ./dist/mapp-intelligence-java-cronjob.jar" -m "${USER_NAME}"
else
    sudo -su ${USER_NAME} mvn "${EXLUDE_SHOP_EXAMPLE}" clean checkstyle:check package -B
    sudo -su ${USER_NAME} cp ./tracking/target/mapp-intelligence-java-tracking.jar ./dist/mapp-intelligence-java-tracking.jar
    sudo -su ${USER_NAME} cp ./cronjob/target/mapp-intelligence-java-tracking.jar ./dist/mapp-intelligence-java-tracking.jar
fi
