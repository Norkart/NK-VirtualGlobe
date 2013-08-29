#*********************************************************************
#
#  (C) 2001-07 Web3d Consortium
#    http://www.web3d.org/
#
# Lowest level common makefile for both native and Java code
# 
# Author: Justin Couch
# Version: $Revision: 1.19 $
#
#*********************************************************************

ifndef APP_ROOT
  BUILD_ROOT_DIR=$(PROJECT_ROOT)
else
  BUILD_ROOT_DIR=$(APP_ROOT)
endif

SRC_DIR=src
DOCS_DIR=docs
MAKE_DIR=make

JAVA_DIR=$(SRC_DIR)/java
PARSER_DIR=$(SRC_DIR)/javacc
NATIVE_DIR=$(SRC_DIR)/native
INCLUDE_DIR=$(NATIVE_DIR)/include
EXAMPLES_DIR=$(PROJECT_ROOT)/examples
IMAGES_DIR=${SRC_DIR}/images
XML_DIR=${SRC_DIR}/xml
CONFIG_DIR=${SRC_DIR}/config
ECLIPSE_DIR=${SRC_DIR}/eclipse

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
