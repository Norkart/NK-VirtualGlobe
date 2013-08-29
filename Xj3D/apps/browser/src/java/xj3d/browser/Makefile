#*********************************************************************
#
#  (C) 2001 Web3d Consortium
#    http://www.web3d.org/
#
# Package makefile for the xj3d.browser directory
#
# Author: Alan Hudson
# Version: $Revision: 1.23 $
#
#*********************************************************************

PACKAGE = xj3d.browser

# The source files to compile. These are declared in order that they must be
# compiled in
SOURCE = SurfaceManager.java \
    MultisampleChooser.java \
    AntialiasingAction.java \
    PointsStyleAction.java \
    LinesStyleAction.java \
    ShadedStyleAction.java \
    NiceAction.java \
    ExitAction.java \
    SceneInfoAction.java \
    MovieAction.java \
    HelpAction.java \
    ProfilingInfoAction.java \
    ScreenSaver.java \
    ScreenShotAction.java \
    CaptureViewpointsAction.java \
    DeathTimer.java \
    STNodeWrapper.java \
    NodeEffects.java \
    NodeFilter.java \
    NullNodeFilter.java \
    GeometryNodeFilter.java \
    SceneTreeViewer.java \
    SceneTreeAction.java \
    Xj3DBrowser.java

# The list of other files we need to copy from this directory to the classes
# directory when we are making JAR files.
EXTRA =

# If the package requires extra classes outside the JDK, the JAR files that
# are required are listed here. These should be found in ${PROJECT_ROOT}/lib
JARS_3RDPARTY= aviatrix3d-all_2.0.0.jar uri.jar j3d-org-all_0.9.0.jar jhall.jar vecmath.jar

include ${PROJECT_ROOT}/make/Makefile.jav
