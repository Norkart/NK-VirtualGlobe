#*********************************************************************
#
#  (C) 2001 Web3d Consortium
#    http://www.web3d.org/
# 
# Package makefile for the org.xj3d.impl.core.loading directory
#
# Author: Justin Couch
# Version: $Revision: 1.4 $
#
#*********************************************************************

PACKAGE = org.xj3d.impl.core.loading

# The source files to compile. These are declared in order that they must be
# compiled in
SOURCE = \
  DefaultCacheDetails.java \
  DefaultFileCache.java \
  WeakRefFileCache.java \
  BaseLoadHandler.java \
  ScriptLoadDetails.java \
  ScriptLoadHandler.java \
  DefaultScriptLoader.java \
  ContentLoadDetails.java \
  ContentLoadHandler.java \
  AbstractLoadManager.java \
  DefaultLoadManager.java \
  MemCacheLoadManager.java \
  WorldLoadDetails.java \
  WorldLoadHandler.java \
  DefaultWorldLoader.java \
  DefaultWorldLoaderManager.java \
  FramerateThrottle.java


# If the package requires extra classes outside the JDK, the JAR files that
# are required are listed here. These should be found in ${PROJECT_ROOT}/lib
JARS_3RDPARTY=uri.jar j3d-org-all_0.9.0.jar

# The list of other files we need to copy from this directory to the classes
# directory when we are making JAR files.
EXTRA = 

include ${PROJECT_ROOT}/make/Makefile.jav
