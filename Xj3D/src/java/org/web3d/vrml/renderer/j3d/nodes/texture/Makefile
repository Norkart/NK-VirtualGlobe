#*********************************************************************
#
#  (C) 2001 Web3d Consortium
#    http://www.web3d.org/
#
# Package makefile for the org.web3d.vrml.renderer.j3d.nodes.texture
# directory
#
# Author: Justin Couch
# Version: $Revision: 1.11 $
#
#*********************************************************************

PACKAGE = org.web3d.vrml.renderer.j3d.nodes.texture

# The source files to compile. These are declared in order that they must be
# compiled in
SOURCE = \
    J3DTextureCoordinate.java \
    J3DTextureTransform.java \
    J3DImageTexture.java \
    J3DPixelTexture.java \
    J3DMovieTexture.java \
    J3DMultiTexture.java \
    J3DMultiTextureTransform.java \
    J3DMultiTextureCoordinate.java  \
    J3DTextureCoordinateGenerator.java \
    J3DTextureCoordinate3D.java \
    J3DTextureCoordinate4D.java \
    J3DTextureTransform3D.java \
    J3DTextureProperties.java \
    J3DComposedTexture3D.java

# The list of other files we need to copy from this directory to the classes
# directory when we are making JAR files.
EXTRA =

# If the package requires extra classes outside the JDK, the JAR files that
# are required are listed here. These should be found in ${PROJECT_ROOT}/lib
JARS_3RDPARTY=j3d-org-all_0.9.0.jar

include ${PROJECT_ROOT}/make/Makefile.jav
