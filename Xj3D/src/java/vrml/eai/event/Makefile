#*********************************************************************
#
#  (C) 2001 Web3d Consortium
#    http://www.web3d.org/
# 
# Package makefile for the vrml.eai.event directory
#
# Author: Justin Couch
# Version: $Revision: 1.2 $
#
#*********************************************************************

PACKAGE = vrml.eai.event

# The source files to compile. These are declared in order that they must be
# compiled in
SOURCE = VrmlEvent.java \
  BrowserEvent.java \
  VrmlEventListener.java \
  BrowserListener.java

# The list of other files we need to copy from this directory to the classes
# directory when we are making JAR files.
EXTRA = 

# If the package requires extra classes outside the JDK, the JAR files that
# are required are listed here. These should be found in ${PROJECT_ROOT}/lib
JARS_3RDPARTY=

# Define IGNORE_CYCLES here because there is a known cyclic dependency in
# the EAI packages between vrml.eai, vrml.eai.event and vrml.eai.field
IGNORE_CYCLES=1

include ${PROJECT_ROOT}/make/Makefile.jav
