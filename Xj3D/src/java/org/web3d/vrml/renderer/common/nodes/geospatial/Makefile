#*********************************************************************
#
#  (C) 2001 Web3d Consortium
#    http://www.web3d.org/
#
# Package makefile for the org.web3d.vrml.renderer.common.nodes.geospatial
# directory
#
# Author: Justin Couch
# Version: $Revision: 1.19 $
#
#*********************************************************************

PACKAGE = org.web3d.vrml.renderer.common.nodes.geospatial

# The source files to compile. These are declared in order that they must be
# compiled in
SOURCE = BaseGeoOrigin.java \
  BaseGeoCoordinate.java \
  BaseGeoLocation.java \
  BaseGeoMetadata.java \
  BaseGeoViewpoint.java \
  GeoPosition.java \
  ElevationGridGenerator.java \
  BaseGeoElevationGrid.java \
  PositionInterpolator.java \
  BaseGeoPositionInterpolator.java \
  BaseGeoTouchSensor.java \
  SceneWrapper.java \
  BaseGeoLOD.java \
  BaseGeoTransform.java \


# The list of other files we need to copy from this directory to the classes
# directory when we are making JAR files.
EXTRA =

# If the package requires extra classes outside the JDK, the JAR files that
# are required are listed here. These should be found in ${PROJECT_ROOT}/lib
JARS_3RDPARTY= gt2-main-2.3.1.jar gt2-referencing-2.3.1.jar \
  geoapi-nogenerics-2.1-M2.jar jsr108-0.01.jar j3d-org-all_0.9.0.jar \
  vecmath.jar

include ${PROJECT_ROOT}/make/Makefile.jav
