#*********************************************************************
#
#  (C) 2001 - 2006 Web3D Consortium
#    http://www.web3d.org/
#
# Package makefile for the org.xj3d.impl.core.eventmodel directory
#
# Author: Justin Couch
# Version: $Revision: 1.5 $
#
#*********************************************************************

PACKAGE = org.xj3d.impl.core.eventmodel

# The source files to compile. These are declared in order that they must be
# compiled in
SOURCE = EventOutHolder.java \
  RouteHolder.java \
  DefaultRouteManager.java \
  SimpleRouter.java \
  SimpleRouterFactory.java \
  ListsRouter.java \
  ListsRouterFactory.java \
  DefaultFrameStateManager.java \
  DefaultEventModelEvaluator.java \
  DefaultScriptManager.java \
  DefaultNetworkManager.java \
  DefaultHumanoidManager.java \
  DefaultParticleSystemManager.java \
  DefaultRigidBodyPhysicsManager.java \
  DefaultViewpointManager.java \
  DefaultNavigationManager.java \
  RealTimeSensorManager.java \
  VirtualTimeSensorManager.java \

# The list of other files we need to copy from this directory to the classes
# directory when we are making JAR files.
EXTRA =

# If the package requires extra classes outside the JDK, the JAR files that
# are required are listed here. These should be found in ${PROJECT_ROOT}/lib
JARS_3RDPARTY = vecmath.jar j3d-org-all_0.9.0.jar odejava.jar

include ${PROJECT_ROOT}/make/Makefile.jav
