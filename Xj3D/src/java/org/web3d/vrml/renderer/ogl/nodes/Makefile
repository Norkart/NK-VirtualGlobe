#*********************************************************************
#
#  (C) 2001 Web3d Consortium
#    http://www.web3d.org/
#
# Package makefile for the org.web3d.vrml.renderer.ogl.nodes directory
#
# Author: Justin Couch
# Version: $Revision: 1.25 $
#
#*********************************************************************

PACKAGE = org.web3d.vrml.renderer.ogl.nodes

# The source files to compile. These are declared in order that they must be
# compiled in
SOURCE = \
    TextureCache.java \
    OGLVRMLNode.java \
    OGLGlobalStatus.java \
    OGLAreaListener.java \
    OGLVisibilityListener.java \
    OGLTransformNodeType.java \
    OGLUserData.java \
    OGLBindableNodeType.java \
    OGLBackgroundNodeType.java \
    OGLViewpointNodeType.java \
    OGLNavigationInfoNodeType.java \
    OGLWorldRootNodeType.java \
    OGLMaterialNodeType.java \
    OGLTextureNodeType.java \
    OGLTextureConstConverter.java \
    OGLTextureTransformListener.java \
    OGLTextureCoordinateTransformNodeType.java \
    OGLAppearanceNodeType.java \
    OGLGeometryNodeType.java \
    OGLLightNodeType.java \
    OGLTextNodeType.java \
    OGLPickingFlagConvertor.java \
    OGLPickingSensorNodeType.java \
    OGLPickableTargetNodeType.java

# The list of other files we need to copy from this directory to the classes
# directory when we are making JAR files.
EXTRA =

# If the package requires extra classes outside the JDK, the JAR files that
# are required are listed here. These should be found in ${PROJECT_ROOT}/lib
JARS_3RDPARTY = j3d-org-all_0.9.0.jar aviatrix3d-all_2.0.0.jar vecmath.jar

include ${PROJECT_ROOT}/make/Makefile.jav
