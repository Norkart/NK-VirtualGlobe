#*********************************************************************
#
#  (C) 2001 Web3d Consortium
#    http://www.web3d.org/
#
# Package makefile for the org.web3d.vrml.renderer.ogl.nodes.geospatial
# directory
#
# Author: Justin Couch
# Version: $Revision: 1.12 $
#
#*********************************************************************

PACKAGE = org.web3d.vrml.renderer.ogl.nodes.geospatial

# The source files to compile. These are declared in order that they must be
# compiled in
SOURCE = OGLGeoCoordinate.java \
    OGLGeoLocation.java \
    OGLGeoOrigin.java \
    OGLGeoMetadata.java \
    OGLGeoViewpoint.java \
    OGLGeoElevationGrid.java \
    OGLGeoPositionInterpolator.java \
    OGLGeoTouchSensor.java \
    OGLGeoLOD.java \
    OGLGeoTransform.java \

# The list of other files we need to copy from this directory to the classes
# directory when we are making JAR files.
EXTRA =

# If the package requires extra classes outside the JDK, the JAR files that
# are required are listed here. These should be found in ${PROJECT_ROOT}/lib
JARS_3RDPARTY = aviatrix3d-all_2.0.0.jar j3d-org-all_0.9.0.jar geoapi-nogenerics-2.1-M2.jar vecmath.jar

include ${PROJECT_ROOT}/make/Makefile.jav
