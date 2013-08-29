@echo off

SET OLDPATH=%PATH%
SET CP=".;apps/elumens;jars/JXInput.jar;jars/j3d-org-all_0.9.0.jar;jars/gnu-regexp-1.0.8.jar;jars/httpclient.jar;jars/j3d-org-images.jar;jars/js.jar;jars/uri.jar;jars/vlc_uri.jar;jars/xj3d-all.jar"

REM The Java path must be correct.  The most common one:
SET JAVA_PATH="c:\Program Files\Java\j2re1.4.2_03\bin"

REM The one developers have
REM SET JAVA_PATH="c:\j2sdk1.4.1_01\bin"

SET PATH=%JAVA_PATH%;%PATH%;.

echo Running Xj3D Elumens browser

copy opengl32.dll %JAVA_PATH%
copy jxinput.dll %JAVA_PATH%
java -classpath %CP% -Xmx550M -Xms550M ElumensBrowser -size1 -channels3 -fullscreen %3 %4

SET PATH=%OLDPATH%

del %JAVA_PATH%\opengl32.dll
pause 1
