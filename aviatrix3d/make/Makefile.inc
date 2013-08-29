#*********************************************************************
#
#                         (C) 2003 - 2007 j3d.org
#                        http://aviatrix3d.j3d.org/
#
# Lowest level common makefile for both native and Java code
# 
# Author: Justin Couch
# Version: $Revision: 1.9 $
#
#*********************************************************************

SRC_DIR=src
DOCS_DIR=docs
MAKE_DIR=make

JAVA_DIR=$(SRC_DIR)/java
SHADER_DIR=$(SRC_DIR)/glsl
EXAMPLES_DIR=$(PROJECT_ROOT)/examples
ECLIPSE_DIR=$(SRC_DIR)/eclipse

DELETE    = rm -f
MAKEDIR   = mkdir -p
RMDIR     = rm -fr
PRINT     = @echo
COPY      = cp 
COPY_PATH = cp --parents
CHMOD     = chmod
CD        = cd

#JAVAC_FLAGS=-deprecation
#JAVADOC_FLAGS=

MAKEFLAGS=-s
