#*********************************************************************
#
#  (C) 2001 Web3d Consortium
#    http://www.web3d.org/
#
# Package makefile for the org.web3d.vrml.renderer.j3d.nodes.geospatial
# directory
#
# Author: Justin Couch
# Version: $Revision: 1.6 $
#
#*********************************************************************

PACKAGE = org.web3d.vrml.renderer.j3d.nodes.geospatial

# The source files to compile. These are declared in order that they must be
# compiled in
SOURCE = J3DGeoCoordinate.java \
    J3DGeoLocation.java \
    J3DGeoOrigin.java \
    J3DGeoMetadata.java \
    J3DGeoViewpoint.java \
    J3DGeoPositionInterpolator.java \
    J3DGeoTouchSensor.java \
    J3DGeoLOD.java \
    J3DGeoElevationGrid.java \

# The list of other files we need to copy from this directory to the classes
# directory when we are making JAR files.
EXTRA =

# If the package requires extra classes outside the JDK, the JAR files that
# are required are listed here. These should be found in ${PROJECT_ROOT}/lib
JARS_3RDPARTY = j3d-org-all_0.9.0.jar geoapi.jar

include ${PROJECT_ROOT}/make/Makefile.jav
