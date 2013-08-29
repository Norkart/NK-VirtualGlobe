@echo off

@echo Running Xj3D Format Converter
@echo converter.bat %1 %2 %3 %4 %5 %6 %7

IF NOT EXIST "%Xj3D_HOME%"\converter.bat (
   IF EXIST "c:\Program Files\Xj3D" ( SET Xj3D_HOME=c:\Program Files\Xj3D )
   IF EXIST "d:\Program Files\Xj3D" ( SET Xj3D_HOME=d:\Program Files\Xj3D )
   IF EXIST "e:\Program Files\Xj3D" ( SET Xj3D_HOME=e:\Program Files\Xj3D )
   IF EXIST "f:\Program Files\Xj3D" ( SET Xj3D_HOME=f:\Program Files\Xj3D )
   IF EXIST "g:\Program Files\Xj3D" ( SET Xj3D_HOME=g:\Program Files\Xj3D )
)
java  -Xmx450M -Xbootclasspath/p:./bin -Dsun.java2d.noddraw=true -Djava.library.path=./bin -classpath .;"%Xj3D_HOME%"/apps/converter/xj3d_converter_2.0.0.jar;"%Xj3D_HOME%"/apps/browser/xj3d_browser_2.0.0.jar;"%Xj3D_HOME%"/jars/xj3d-all_2.0.0.jar xj3d.converter.Xj3DConv %1 %2 %3 %4 %5 %6
