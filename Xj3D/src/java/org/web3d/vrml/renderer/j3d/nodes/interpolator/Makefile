#*********************************************************************
#
#  (C) 2001 Web3d Consortium
#    http://www.web3d.org/
# 
# Package makefile for the org.web3d.vrml.renderer.j3d.nodes.interpolator
# directory
#
# Author: Justin Couch
# Version: $Revision: 1.5 $
#
#*********************************************************************

PACKAGE = org.web3d.vrml.renderer.j3d.nodes.interpolator

# The source files to compile. These are declared in order that they must be
# compiled in
SOURCE = J3DPositionInterpolator.java \
         J3DPositionInterpolator2D.java \
         J3DOrientationInterpolator.java \
         J3DCoordinateInterpolator.java \
         J3DCoordinateInterpolator2D.java \
         J3DScalarInterpolator.java \
         J3DColorInterpolator.java \
         J3DNormalInterpolator.java

# The list of other files we need to copy from this directory to the classes
# directory when we are making JAR files.
EXTRA = 

# If the package requires extra classes outside the JDK, the JAR files that 
# are required are listed here. These should be found in ${PROJECT_ROOT}/lib
JARS_3RDPARTY=j3d-org-all_0.9.0.jar

include ${PROJECT_ROOT}/make/Makefile.jav
