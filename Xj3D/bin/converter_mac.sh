#!/bin/bash
java  -Xmx450M -Xbootclasspath/p:./bin -Djava.library.path=./bin -classpath ".:Xj3DBrowser.app/Contents/Resources/Java/xj3d_converter.jar:Xj3DBrowser.app/Contents/Resources/Java/xj3d-all.jar" xj3d.converter.Xj3DConv $1 $2 $3 $4 $5 $6
