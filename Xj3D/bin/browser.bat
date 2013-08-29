@echo off

if exist browser.bat goto launch
cd \Program Files\Xj3D

:launch

java  -Xmx450M -XX:MaxDirectMemorySize=350M -Xbootclasspath/p:./bin -Dsun.java2d.noddraw=true -Djava.library.path=./bin -classpath .;apps/browser/xj3d_browser_2.0.0.jar;jars/xj3d-all_2.0.0.jar;jars/disxml.jar;jars/smack.jar;jars/smackx.jar;jars/jhall.jar;jars/jxinput.jar xj3d.browser.Xj3DBrowser %1 %2 %3 %4 %5 %6
