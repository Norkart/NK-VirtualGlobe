#
# Top Level makefile for the Xj3D codebase
#
#  (C) 2001 Web3d Consortium
#    http://www.web3d.org/
#
# To use this, make sure that you have the PROJECT_ROOT environment variable
# set 
#
# This makefile is designed to build the entire library from scratch. It is
# not desigend as a hacking system. It is recommended that you use the normal
# javac/CLASSPATH setup for that. 
#
# The following commands are offered:
#
# - parsers:   Build the parser files
# - class:     Compile just the classes. Don't make JAR file 
# - bin:	   Build parsers and classes
# - examples:  Build the examples. Doesn't build the core libraries
# - jar:       Make the java JAR file 
# - parserdoc: Generate BNF documentation for the parser
# - javadoc:   Generate the javadoc information
# - docs:      Generate both parser and javadoc files
# - all:       Build everything (including docs)
# - install:   Build everything then put the JARs in the right place
# - clean:     Blow all the library classes away
# - nuke:      Blow both lib and example code away
#
# Author: Justin Couch
# Version: $Revision: 1.1 $

ifndef PROJECT_ROOT
export PROJECT_ROOT=/usr/local/src/projects/Xj3D/x3d
endif

# Always override the setting as we build multiple applications
export APP_ROOT=$(PROJECT_ROOT)/apps/installer

include $(PROJECT_ROOT)/make/Makefile.inc

VERSION=1.1

all: jar javadoc

bin: class properties

class:
	cd $(JAVA_DIR) && make buildall

properties:
	cd $(JAVA_DIR) && make properties

javadoc:
	cd $(JAVA_DIR) && make javadoc

jar: bin
	cd $(JAVA_DIR) && make jar

install: jar
	cd $(JAVA_DIR) && make install

clean:
	cd $(JAVA_DIR) && make clean
