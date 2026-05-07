#! /bin/bash

java \
-classpath ".:sqlite-jdbc-3.53.1.0.jar:sorma2.jar" \
com/zoffcc/applications/sorm/Generator "gen" || exit 1
