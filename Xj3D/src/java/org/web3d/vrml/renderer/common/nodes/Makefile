#*********************************************************************
#
#  (C) 2001 Web3d Consortium
#    http://www.web3d.org/
#
# Package makefile for the org.web3d.vrml.renderer.common.nodes directory
#
# Author: Justin Couch
# Version: $Revision: 1.22 $
#
#*********************************************************************

PACKAGE = org.web3d.vrml.renderer.common.nodes

# The source files to compile. These are declared in order that they must be
# compiled in
SOURCE = \
  AbstractNode.java \
  AbstractDynamicFieldNode.java \
  BaseInterpolatorNode.java \
  BaseGroupingNode.java \
  BaseBindableNode.java \
  BaseTimeDependentNode.java \
  BaseTimeControlledNode.java \
  BaseComponentGeometryNode.java \
  BaseIndexedGeometryNode.java \
  GeometryHolder.java \
  GeometryUtils.java \
  BaseGeometricPropertyNode.java \
  BaseIndexedTriangleGeometryNode.java \
  BaseTriangleGeometryNode.java \
  BaseSensorNode.java \
  BaseDragSensorNode.java \
  BaseLightNode.java\
  BaseTextureNode.java \
  BaseTexture2DNode.java \
  BaseTexture3DNode.java \
  BaseSequencerNode.java \
  BaseMetadataObjectNode.java \
  AreaListener.java \
  VisibilityListener.java

# The list of other files we need to copy from this directory to the classes
# directory when we are making JAR files.
EXTRA =

# If the package requires extra classes outside the JDK, the JAR files that
# are required are listed here. These should be found in ${PROJECT_ROOT}/lib
JARS_3RDPARTY = j3d-org-all_0.9.0.jar

include ${PROJECT_ROOT}/make/Makefile.jav
