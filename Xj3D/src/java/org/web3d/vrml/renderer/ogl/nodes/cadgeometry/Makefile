#*********************************************************************
#
#  (C) 2004 Web3d Consortium
#    http://www.web3d.org/
#
# Package makefile for the org.web3d.vrml.renderer.ogl.nodes.cadgeometry
# directory
#
# Author: Alan Hudson
# Version: $Revision: 1.7 $
#
#*********************************************************************

PACKAGE = org.web3d.vrml.renderer.ogl.nodes.cadgeometry

# The source files to compile. These are declared in order that they must be
# compiled in
SOURCE = OGLCADLayer.java \
     OGLCADAssembly.java \
     OGLCADPart.java \
     OGLCADFace.java \
     OGLQuadSet.java

# The list of other files we need to copy from this directory to the classes
# directory when we are making JAR files.
EXTRA =

# If the package requires extra classes outside the JDK, the JAR files that
# are required are listed here. These should be found in ${PROJECT_ROOT}/lib
JARS_3RDPARTY = aviatrix3d-all_2.0.0.jar j3d-org-all_0.9.0.jar vecmath.jar

include ${PROJECT_ROOT}/make/Makefile.jav
