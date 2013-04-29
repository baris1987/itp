#!/bin/sh
dir=$(dirname "$0")
java -cp "$dir/../libs/h2-1.3.170.jar:$H2DRIVERS:$CLASSPATH" org.h2.tools.Console "$@"
