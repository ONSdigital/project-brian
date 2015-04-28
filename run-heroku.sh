#!/bin/bash

./build-js.sh

java $JAVA_OPTS -Drestolino.files="target/web" -Drestolino.classes="target/classes" -Drestolino.packageprefix=com.github.onsdigital -cp "target/dependency/*" com.github.davidcarboni.restolino.Main