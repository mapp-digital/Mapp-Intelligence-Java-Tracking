#!/usr/bin/make

USER_ID := $(shell id -u)
GROUP_ID := $(shell id -g)

export USER_ID
export GROUP_ID

build:
	BUILD_TYPE="build" JAVA_VERSION="8" bash -c "docker-compose build && docker-compose run java && docker-compose down --volumes"

test:
	BUILD_TYPE="test" JAVA_VERSION="$(JAVA_VERSION)" bash -c "docker-compose build && docker-compose run java && docker-compose down --volumes"

demo:
	bash -c "docker-compose build && docker-compose up shop"

demo-cron-fail:
	docker exec mapp-intelligence-java-tracking_shop_1 /bin/bash -c "java -jar /usr/local/tomcat/temp/mapp-intelligence-java-cronjob-0.0.1.jar -i 111111111111111 -d q3.webtrekk.net -f /usr/local/tomcat/temp/ --debug"

demo-cron-success:
	docker exec mapp-intelligence-java-tracking_shop_1 /bin/bash -c "java -jar /usr/local/tomcat/temp/mapp-intelligence-java-cronjob-0.0.1.jar -i 123451234512345 -d q3.webtrekk.net -f /usr/local/tomcat/temp/ --debug"

demo-log:
	docker exec -it -w /usr/local/tomcat/temp/ mapp-intelligence-java-tracking_shop_1 bash

demo-ssh:
	docker exec -it mapp-intelligence-java-tracking_shop_1 bash

test-all:
	make test-java8 && make test-java9 && make test-java10 && make test-java11 && make test-java12 && make test-java13 && make test-java14 && make test-java15 && make test-java16

test-latest:
	make test JAVA_VERSION=latest

test-java8:
	make test JAVA_VERSION=8

test-java9:
	make test JAVA_VERSION=9

test-java10:
	make test JAVA_VERSION=10

test-java11:
	make test JAVA_VERSION=11

test-java12:
	make test JAVA_VERSION=12

test-java13:
	make test JAVA_VERSION=13

test-java14:
	make test JAVA_VERSION=14

test-java15:
	make test JAVA_VERSION=15

test-java16:
	make test JAVA_VERSION=16

####################
### next version ###
####################
test-java17:
	make test JAVA_VERSION=17

test-java18:
	make test JAVA_VERSION=18

test-java19:
	make test JAVA_VERSION=19

test-java20:
	make test JAVA_VERSION=20

.PHONY: build build-java build-cron test demo demo-cron-fail demo-cron-success demo-log demo-ssh test-all test-latest test-java8 test-java9 test-java10 test-java11 test-java12 test-java13 test-java14 test-java15 test-java16 test-java17 test-java18 test-java19 test-java20
