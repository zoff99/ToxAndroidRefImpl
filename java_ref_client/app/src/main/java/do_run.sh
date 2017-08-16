#! /bin/bash

java -Djava.library.path="." com.zoffcc.applications.trifa.MainActivity 2>&1 | tee trifa.log
