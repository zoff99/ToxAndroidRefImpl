#! /bin/bash

java \
-classpath ".:sqlite-jdbc-3.46.1.2.jar:sorma2.jar" \
com/zoffcc/applications/sorm/Generator "gen"

cd gen/
/usr/lib/jvm/temurin-8-jdk-amd64/bin/javac \
-cp "sqlite-jdbc-3.46.1.2.jar" \
com/zoffcc/applications/sorm/*.java && \
jar cf sorma_generated.jar com/zoffcc/applications/sorm/*.class && \
cp sorma_generated.jar ../test/ && \
cd ../

# use generated custom jar
cd test/
/usr/lib/jvm/temurin-8-jdk-amd64/bin/javac \
-classpath ".:sqlite-jdbc-3.46.1.2.jar:sorma_generated.jar" \
org/example/TestSorma.java

rm -f main.db

/usr/lib/jvm/temurin-8-jdk-amd64/bin/java \
-classpath ".:sqlite-jdbc-3.46.1.2.jar:sorma_generated.jar" \
org/example/TestSorma

