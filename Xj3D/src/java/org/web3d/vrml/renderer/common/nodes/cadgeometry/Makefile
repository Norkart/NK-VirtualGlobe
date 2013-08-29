#*********************************************************************
#
#  (C) 2001 Web3d Consortium
#    http://www.web3d.org/
#
# Package makefile for the org.web3d.vrml.renderer.common.nodes.group
# directory
#
# Author: Alan Hudson
# Version: $Revision: 1.3 $
#
#*********************************************************************

PACKAGE = org.web3d.vrml.renderer.common.nodes.cadgeometry

# The source files to compile. These are declared in order that they must be
# compiled in
SOURCE = BaseCADLayer.java \
	 BaseCADAssembly.java \
	 BaseCADPart.java \
	 BaseCADFace.java \
	 BaseQuadSet.java

# The list of other files we need to copy from this directory to the classes
# directory when we are making JAR files.
EXTRA =

# If the package requires extra classes outside the JDK, the JAR files that
# are required are listed here. These should be found in ${PROJECT_ROOT}/lib
JARS_3RDPARTY = vecmath.jar

include ${PROJECT_ROOT}/make/Makefile.jav
