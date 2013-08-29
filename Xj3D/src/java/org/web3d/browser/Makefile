#*********************************************************************
#
#  (C) 2001 - 2006 Web3d Consortium
#    http://www.web3d.org/
#
# Package makefile for the org.web3d.browser directory
#
# Author: Justin Couch
# Version: $Revision: 1.21 $
#
#*********************************************************************

PACKAGE = org.web3d.browser

# The source files to compile. These are declared in order that they must be
# compiled in
SOURCE = Xj3DConstants.java \
    InvalidConfigurationException.java \
    BrowserCoreListener.java \
    SensorStatusListener.java \
    ViewpointStatusListener.java \
    EventModelStatusListener.java \
    NavigationStateListener.java \
    ProfilingInfo.java \
    AbstractProfilingInfo.java \
    ProfilingListener.java \
    NodeObserver.java \
    BrowserCore.java \
    BrowserComponent.java \
    VRMLBrowserFactoryImpl.java \
    X3DBrowserFactoryImpl.java

# If the package requires extra classes outside the JDK, the JAR files that
# are required are listed here. These should be found in ${PROJECT_ROOT}/lib
JARS_3RDPARTY = j3d-org-all_0.9.0.jar uri.jar

# The list of other files we need to copy from this directory to the classes
# directory when we are making JAR files.
EXTRA =


include ${PROJECT_ROOT}/make/Makefile.jav
