#*********************************************************************
#
#  (C) 2006 Web3D Consortium
#    http://www.web3d.org/
#
# Package makefile for the org.xj3d.ui.swt.device.ogl.mouse directory
#
# Author: Rex Melton
# Version: $Revision: 1.4 $
#
#*********************************************************************

PACKAGE = org.xj3d.ui.swt.device.ogl.mouse

# The source files to compile. These are declared in order that they must be
# compiled in
SOURCE = MouseTracker.java \
    MouseDevice.java \
    MouseManager.java

# The list of other files we need to copy from this directory to the classes
# directory when we are making JAR files.
EXTRA =

# If the package requires extra classes outside the JDK, the JAR files that
# are required are listed here. These should be found in ${PROJECT_ROOT}/lib
JARS_3RDPARTY = \
  aviatrix3d-all_2.0.0.jar \
  org.eclipse.swt.win32.win32.x86_3.2.0.jar \
  vecmath.jar

include ${PROJECT_ROOT}/make/Makefile.jav
