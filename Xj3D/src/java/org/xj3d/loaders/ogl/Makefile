#*********************************************************************
#
#  (C) 2001 Web3d Consortium
#    http://www.web3d.org/
# 
# Package makefile for the org.xj3d.loaders.ogl directory
#
# Author: Justin Couch
# Version: $Revision: 1.6 $
#
#*********************************************************************

PACKAGE = org.xj3d.loaders.ogl

# The source files to compile. These are declared in order that they must be
# compiled in
SOURCE = Xj3DModel.java \
  StaticClock.java \
  StaticBrowserCore.java \
  StaticRouteManager.java \
  Xj3DClockRuntime.java \
  NullLayerRenderingManager.java \
  NullLayerManager.java \
  NullLayerManagerFactory.java \
  BaseLoader.java \
  VRML97Loader.java \
  X3DLoader.java \
  Web3DLoader.java

# The list of other files we need to copy from this directory to the classes
# directory when we are making JAR files.
EXTRA = 

# If the package requires extra classes outside the JDK, the JAR files that 
# are required are listed here. These should be found in ${PROJECT_ROOT}/lib
JARS_3RDPARTY=uri.jar aviatrix3d-all_2.0.0.jar j3d-org-all_0.9.0.jar vecmath.jar

include ${PROJECT_ROOT}/make/Makefile.jav
