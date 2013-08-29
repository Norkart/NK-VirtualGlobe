#*********************************************************************
#
#  (C) 2001 Web3d Consortium
#    http://www.web3d.org/
#
# Package makefile for the org.web3d.vrml.renderer.j3d.input directory
#
# Author: Justin Couch
# Version: $Revision: 1.15 $
#
#*********************************************************************

PACKAGE = org.web3d.vrml.renderer.j3d.input

# The source files to compile. These are declared in order that they must be
# compiled in
SOURCE = CollisionListener.java \
    CollisionListenerAdapter.java \
    J3DUserInputHandler.java \
    J3DGlobalEffectsHandler.java \
    TerrainDataSource.java \
    StaticTerrainDataSource.java \
    J3DTerrainManager.java \
    J3DRendererEffectsHandler.java \
    J3DPickingManager.java \
    DefaultUserInputHandler.java \
    VisibilityManager.java \
    AreaManager.java \
    J3DSensorManager.java \
    DefaultSensorManager.java \
    DefaultPickingManager.java \

#   BehaviorIDConstants.java \
#   TimeScheduler.java \
#   RouteBehavior.java \
#   J3DProximityBehavior.java

# The list of other files we need to copy from this directory to the classes
# directory when we are making JAR files.
EXTRA =

# If the package requires extra classes outside the JDK, the JAR files that
# are required are listed here. These should be found in ${PROJECT_ROOT}/lib
JARS_3RDPARTY=j3d-org-all_0.9.0.jar

include ${PROJECT_ROOT}/make/Makefile.jav
