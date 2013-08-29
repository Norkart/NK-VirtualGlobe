#*********************************************************************
#
#  (C) 2001 Web3d Consortium
#    http://www.web3d.org/
# 
# Package makefile for the org.web3d.vrml.scripting.external.buffer directory
#
# Copied from makefile by Justin Couch
# Author: Brad Vender
# Version: $Revision: 1.11 $
#
#*********************************************************************

PACKAGE = org.web3d.vrml.scripting.external.buffer

# The source files to compile. These are declared in order that they must be
# compiled in
SOURCE = \
  NodeRealizationProcessor.java \
  ExternalOutputBuffer.java \
  ExternalOutputBufferReclaimer.java \
  ExternalEvent.java \
  ExternalEventAdapter.java \
  BaseExternalEventAdapter.java \
  EAIAddRoute.java \
  EAICreateVrmlFromUrl.java \
  EAILoadURL.java \
  EAIRemoveRoute.java \
  EAIReplaceWorld.java \
  SAIRealizeNode.java \
  ExternalEventQueue.java \
  NetworkEventQueue.java

# The list of other files we need to copy from this directory to the classes
# directory when we are making JAR files.
EXTRA = 

# If the package requires extra classes outside the JDK, the JAR files that
# are required are listed here. These should be found in ${PROJECT_ROOT}/lib
JARS_3RDPARTY=j3d-org-all_0.9.0.jar uri.jar

include ${PROJECT_ROOT}/make/Makefile.jav
