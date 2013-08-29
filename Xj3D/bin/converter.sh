#!/bin/bash
java  -Xmx450M -Xbootclasspath/p:./bin -Djava.library.path=./bin -classpath ".:apps/converter/xj3d_converter.jar:jars/xj3d-all.jar" xj3d.converter.Xj3DConv $1 $2 $3 $4 $5 $6
