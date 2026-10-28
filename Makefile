#!/usr/bin/make

USER_ID := $(shell id -u)
GROUP_ID := $(shell id -g)
JAVADOC_HOME := $(shell which javadoc)

export USER_ID
export GROUP_ID
export JAVADOC_HOME

build:
	BUILD_TYPE="build" JAVA_VERSION="8" bash -c "docker-compose build && docker-compose run java && docker-compose down --volumes"

test:
	BUILD_TYPE="test" JAVA_VERSION="$(JAVA_VERSION)" bash -c "docker-compose build && docker-compose run java && docker-compose down --volumes"

release:
	BUILD_TYPE="release" JAVA_VERSION="8" bash -c "docker-compose build && docker-compose run java && docker-compose down --volumes"

docs-sync:
	python3 scripts/docs-to-markdown.py

demo:
	bash -c "docker-compose build && docker-compose up shop"

demo-cron-fail:
	docker exec mapp-intelligence-java-tracking_shop_1 /bin/bash -c "java -jar /usr/local/tomcat/temp/mapp-intelligence-java-cronjob.jar -i 111111111111111 -d q3.webtrekk.net -f /usr/local/tomcat/temp/ --debug"

demo-cron-success:
	docker exec mapp-intelligence-java-tracking_shop_1 /bin/bash -c "java -jar /usr/local/tomcat/temp/mapp-intelligence-java-cronjob.jar -i 123451234512345 -d q3.webtrekk.net -f /usr/local/tomcat/temp/ --debug"

demo-log:
	docker exec -it -w /usr/local/tomcat/temp/ mapp-intelligence-java-tracking_shop_1 bash

demo-ssh:
	docker exec -it mapp-intelligence-java-tracking_shop_1 bash

# several JVMs write into the same request logging files while the cron job takes them over, e.g.
# make stress-test STRESS_ARGS="-Dstress.jvms=8 -Dstress.threads=4 -Dstress.requests=5000 -Dstress.maxFileLines=100"
stress-test:
	BUILD_TYPE="stress-test" JAVA_VERSION="8" STRESS_ARGS="$(STRESS_ARGS)" bash -c "docker-compose run java && docker-compose down --volumes"

test-all:
	make test-java8 && make test-java11 && make test-java17 && make test-java18

# newest JDK with an Ubuntu 22.04 (jammy) image, see docker-compose.yaml
test-latest:
	make test JAVA_VERSION=25

test-java8:
	make test JAVA_VERSION=8

test-java11:
	make test JAVA_VERSION=11

test-java17:
	make test JAVA_VERSION=17

test-java18:
	make test JAVA_VERSION=18

####################
### next version ###
####################
test-java19:
	make test JAVA_VERSION=19

test-java20:
	make test JAVA_VERSION=20

test-java21:
	make test JAVA_VERSION=21

test-java22:
	make test JAVA_VERSION=22

test-java25:
	make test JAVA_VERSION=25

.PHONY: test-all
