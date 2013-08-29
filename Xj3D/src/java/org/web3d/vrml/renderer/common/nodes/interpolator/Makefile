#*********************************************************************
#
#  (C) 2001 Web3d Consortium
#    http://www.web3d.org/
# 
# Package makefile for the org.web3d.vrml.renderer.common.nodes.interpolator
# directory
#
# Author: Justin Couch
# Version: $Revision: 1.6 $
#
#*********************************************************************

PACKAGE = org.web3d.vrml.renderer.common.nodes.interpolator

# The source files to compile. These are declared in order that they must be
# compiled in
SOURCE = BasePositionInterpolator.java \
         BasePositionInterpolator2D.java \
         BaseOrientationInterpolator.java \
         BaseCoordinateInterpolator.java \
         BaseCoordinateInterpolator2D.java \
         BaseScalarInterpolator.java \
         BaseColorInterpolator.java \
         BaseNormalInterpolator.java

# The list of other files we need to copy from this directory to the classes
# directory when we are making JAR files.
EXTRA = 

# If the package requires extra classes outside the JDK, the JAR files that 
# are required are listed here. These should be found in ${PROJECT_ROOT}/lib
JARS_3RDPARTY =j3d-org-all_0.9.0.jar vecmath.jar

include ${PROJECT_ROOT}/make/Makefile.jav
