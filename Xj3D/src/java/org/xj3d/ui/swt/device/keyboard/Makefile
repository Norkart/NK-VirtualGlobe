#*********************************************************************
#
#  (C) 2006 Web3d Consortium
#    http://www.web3d.org/
#
# Package makefile for the org.xj3d.ui.swt.device.keyboard directory
#
# Author: Rex Melton
# Version: $Revision: 1.4 $
#
#*********************************************************************

PACKAGE = org.xj3d.ui.swt.device.keyboard

# The source files to compile. These are declared in order that they must be
# compiled in
SOURCE = KeyboardTracker.java \
     KeyboardDevice.java \
     KeyboardManager.java

# If the package requires extra classes outside the JDK, the JAR files that
# are required are listed here. These should be found in ${PROJECT_ROOT}/lib
JARS_3RDPARTY =  \
  j3d-org-all_0.9.0.jar \
  org.eclipse.swt.win32.win32.x86_3.2.0.jar \
  uri.jar \

# The list of other files we need to copy from this directory to the classes
# directory when we are making JAR files.
EXTRA =

include ${PROJECT_ROOT}/make/Makefile.jav
