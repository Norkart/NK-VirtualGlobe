#*********************************************************************
#
#  (C) 2001 - 2006 Web3D Consortium
#    http://www.web3d.org/
#
# Package makefile for the org.xj3d.core.eventmodel directory
#
# Author: Justin Couch
# Version: $Revision: 1.7 $
#
#*********************************************************************

PACKAGE = org.xj3d.core.eventmodel

# The source files to compile. These are declared in order that they must be
# compiled in
SOURCE = \
  Router.java \
  RouterFactory.java \
  RouteManager.java \
  ScriptManager.java \
  UserInputHandler.java \
  DeviceFactory.java \
  InputDeviceManager.java \
  PickingManager.java \
  ExternalView.java \
  BindableNodeListener.java \
  BindableNodeManager.java \
  NodeManager.java \
  NetworkProtocolHandler.java \
  NetworkManager.java \
  ViewpointStatusListener.java \
  ViewpointStatusListenerMulticaster.java \
  ViewpointManager.java \
  NavigationStatusListener.java \
  NavigationStatusListenerMulticaster.java \
  NavigationManager.java \
  LayerSensorManager.java \
  KeyDeviceSensorManager.java \
  SensorManager.java \
  TimeSensorManager.java \
  CursorFilter.java \
  CursorManager.java \
  LayerManager.java \
  LayerManagerFactory.java \
  LayerRenderingManager.java \
  EventModelInitListener.java \
  EventModelEvaluator.java \

# The list of other files we need to copy from this directory to the classes
# directory when we are making JAR files.
EXTRA =

# If the package requires extra classes outside the JDK, the JAR files that
# are required are listed here. These should be found in ${PROJECT_ROOT}/lib
JARS_3RDPARTY = vecmath.jar j3d-org-all_0.9.0.jar odejava.jar

include ${PROJECT_ROOT}/make/Makefile.jav
