#*********************************************************************
#
#  (C) 2001 Web3d Consortium
#    http://www.web3d.org/
#
# Package makefile for the org.web3d.vrml.renderer.common.nodes.rigidphysics
# directory
#
# Author: Justin Couch
# Version: $Revision: 1.9 $
#
#*********************************************************************

PACKAGE = org.web3d.vrml.renderer.common.nodes.rigidphysics

# The source files to compile. These are declared in order that they must be
# compiled in
SOURCE = \
  BaseCollidableNode.java \
  BaseCollidableShape.java \
  BaseCollidableOffset.java \
  BaseCollisionCollection.java \
  BaseCollisionSpace.java \
  BaseContact.java \
  BaseCollisionSensor.java \
  BaseJointNode.java \
  BaseBallJoint.java \
  BaseDoubleAxisHingeJoint.java \
  BaseMotorJoint.java \
  BaseSingleAxisHingeJoint.java \
  BaseSliderJoint.java \
  BaseUniversalJoint.java \
  BaseRigidBody.java \
  BaseRigidBodyCollection.java \

# The list of other files we need to copy from this directory to the classes
# directory when we are making JAR files.
EXTRA =

# If the package requires extra classes outside the JDK, the JAR files that
# are required are listed here. These should be found in ${PROJECT_ROOT}/lib
JARS_3RDPARTY=odejava.jar j3d-org-all_0.9.0.jar vecmath.jar

include ${PROJECT_ROOT}/make/Makefile.jav
