#!/bin/sh

#	Program:	findLine.bat
#	Author: 	Don Brutzman
#	Revised:	18 SEP 2000
#	Purpose:	Find lines of code hidden deep in the recesses of Xj3D
#	Prerequisite:	grep

echo	Usage:
echo	findLine.sh someWord
echo	findLine.sh \"some phrase\"

grep --line-number $1 *.java
grep --line-number $1 */*.java
grep --line-number $1 */*/*.java
grep --line-number $1 */*/*/*.java
grep --line-number $1 */*/*/*/*.java
grep --line-number $1 */*/*/*/*/*.java

