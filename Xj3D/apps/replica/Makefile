#
# Top Level makefile for Xj3D Replica
#
#  (C) 2001 - 2007 Web3d Consortium
#    http://www.web3d.org/
#
# This makefile is designed to build the entire library from scratch. It is
# not desigend as a hacking system. It is recommended that you use the normal
# javac/CLASSPATH setup for that.
#
# The following commands are offered:
#
# - class:      Compile just the classes. Don't make JAR file
# - jar:        Make the java JAR file
# - javadoc:    Generate the javadoc information
# - properties: Copy the properties into the classes area
# - docs:       Generate both parser and javadoc files
# - all:        Build everything (including docs)
# - clean:      Blow all the library classes away
#
# Author: Rex Melton
# Version: $Revision: 1.1 $

ifndef PROJECT_ROOT
export PROJECT_ROOT=../../
endif

# Always override the setting as we build multiple applications
export APP_ROOT=$(PWD)

include $(PROJECT_ROOT)/make/Makefile.inc

all: jar javadoc

bin: class properties

class:
	make -f $(JAVA_DIR)/Makefile buildall

properties:
	make -f $(JAVA_DIR)/Makefile properties

javadoc:
	make -f $(JAVA_DIR)/Makefile javadoc

jar: bin
	make -f $(JAVA_DIR)/Makefile jar

clean:
	make -f $(JAVA_DIR)/Makefile clean
