#! /bin/bash

java \
-classpath ".:sqlite-jdbc-3.51.1.0.jar:sorma2.jar" \
com/zoffcc/applications/sorm/Generator "gen" || exit 1

cd gen/
javac \
-cp "sqlite-jdbc-3.51.1.0.jar" \
com/zoffcc/applications/sorm/*.java && \
jar cf sorma_generated.jar com/zoffcc/applications/sorm/*.class && \
cp sorma_generated.jar ../test/ && \
cd ../ || exit 1

# use generated custom jar
cd test/
javac \
-classpath ".:sqlite-jdbc-3.51.1.0.jar:sorma_generated.jar" \
org/example/TestSorma.java || exit 1

rm -f main.db

java \
-classpath ".:sqlite-jdbc-3.51.1.0.jar:sorma_generated.jar" \
org/example/TestSorma || exit 1

