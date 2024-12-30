#! /bin/bash

javac \
-cp "sqlite-jdbc-3.46.1.2.jar" \
com/zoffcc/applications/sorm/OrmaDatabase.java \
com/zoffcc/applications/sorm/Column.java \
com/zoffcc/applications/sorm/Index.java \
com/zoffcc/applications/sorm/Log.java \
com/zoffcc/applications/sorm/Nullable.java \
com/zoffcc/applications/sorm/OnConflict.java \
com/zoffcc/applications/sorm/PrimaryKey.java \
com/zoffcc/applications/sorm/Table.java \
com/zoffcc/applications/sorm/Generator.java

jar cf sorma2.jar com/zoffcc/applications/sorm/*.class
