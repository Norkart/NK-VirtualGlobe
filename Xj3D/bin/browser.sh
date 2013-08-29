#!/bin/bash
java  -Xmx450M -Xbootclasspath/p:./bin -Djava.library.path=./bin -classpath ".:apps/browser/xj3d_browser_2.0.0.jar:jars/xj3d-all_2.0.0.jar:jars/disxml.jar:jars/jhall.jar:jars/smack.jar:jars/smackx.jar" xj3d.browser.Xj3DBrowser $1 $2 $3 $4 $5 $6
