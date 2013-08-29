#*********************************************************************
#
#  (C) 2003 Yumetech, Inc
#    http://www.j3d.org/
#
# Package makefile for the org.j3d.aviatrix3d.output.graphics directory
#
# Author: Justin Couch
# Version: $Revision: 3.4 $
#
#*********************************************************************

PACKAGE = org.j3d.aviatrix3d.output.graphics

# The source files to compile. These are declared in order that they must be
# compiled in
SOURCE = RenderingProcessor.java \
  StereoRenderingProcessor.java \
  ElumensOutputDevice.java \
  BaseRenderingProcessor.java \
  StandardRenderingProcessor.java \
  DebugRenderingProcessor.java \
  ElumensRenderingProcessor.java \
  QuadBufferStereoProcessor.java \
  SingleEyeStereoProcessor.java \
  BaseSurface.java \
  AWTResizeHandler.java \
  BaseAWTSurface.java \
  SimpleAWTSurface.java \
  DebugAWTSurface.java \
  ElumensAWTSurface.java \
  StereoAWTSurface.java \
  SWTResizeHandler.java \
  BaseSWTSurface.java \
  SimpleSWTSurface.java \
  DebugSWTSurface.java \
  ElumensSWTSurface.java \
  StereoSWTSurface.java \
  PbufferUnavailableException.java \
  PbufferSurface.java

# The list of other files we need to copy from this directory to the classes
# directory when we are making JAR files.
EXTRA =

# If the package requires extra classes outside the JDK, the JAR files that
# are required are listed here. These should be found in ${PROJECT_ROOT}/lib
JARS_3RDPARTY = j3d-org-util.jar j3d-org-elumens.jar j3d-org-opengl-swt.jar

include ${PROJECT_ROOT}/make/Makefile.jav
