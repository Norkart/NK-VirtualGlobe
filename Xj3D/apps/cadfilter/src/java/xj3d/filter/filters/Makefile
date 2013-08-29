#*********************************************************************
#
#  (C) 2001 Web3d Consortium
#    http://www.web3d.org/
#
# Package makefile for the xj3d.converter directory
#
# Author: Alan Hudson
# Version: $Revision: 1.8 $
#
#*********************************************************************

PACKAGE = xj3d.filter.filters

# The source files to compile. These are declared in order that they must be
# compiled in
SOURCE = \
  TriangulationGeometry.java \
  BoxGeometry.java \
  ConeGeometry.java \
  CylinderGeometry.java \
  SphereGeometry.java \
  ElevationGridGeometry.java \
  IndexedFaceSetGeometry.java \
  TriangulationFilter.java \
  TypeConversionFilter.java \
  IndexFilter.java \
  CoordinateProcessor.java \
  ReindexFilter.java \
  FlattenTransformFilter.java \
  GenNormalsFilter.java \
  DebugFilter.java \
  CombineShapeFilter.java \

# The list of other files we need to copy from this directory to the classes
# directory when we are making JAR files.
EXTRA =

# If the package requires extra classes outside the JDK, the JAR files that
# are required are listed here. These should be found in ${PROJECT_ROOT}/lib
JARS_3RDPARTY=j3d-org-all_0.9.0.jar vecmath.jar

include ${PROJECT_ROOT}/make/Makefile.jav
