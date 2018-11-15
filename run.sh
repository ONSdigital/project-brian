#!/bin/bash

export JAVA_OPTS="-Xmx1024m -Xdebug -Xrunjdwp:transport=dt_socket,address=8008,server=y,suspend=n"
export PORT="8083"

# Restolino configuration
export RESTOLINO_STATIC="src/main/resources/files"
export RESTOLINO_CLASSES="target/classes"
export PACKAGE_PREFIX=com.github.onsdigital.brian.handler

export DP_COLOURED_LOGGING=true
export DP_LOGGING_FORMAT=pretty_json

#Development: reloadable
mvn test dependency:copy-dependencies  && \

mvn package -DskipTests=true && \
java $JAVA_OPTS \
 -jar target/*-jar-with-dependencies.jar
