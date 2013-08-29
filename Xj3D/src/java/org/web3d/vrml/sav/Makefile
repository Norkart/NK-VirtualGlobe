#*********************************************************************
#
#  (C) 2001 Web3d Consortium
#    http://www.web3d.org/
#
# Package makefile for the org.web3d.vrml.sav directory
#
# Author: Justin Couch
# Version: $Revision: 1.17 $
#
#*********************************************************************

PACKAGE = org.web3d.vrml.sav

# The source files to compile. These are declared in order that they must be
# compiled in
SOURCE = SAVException.java \
    SAVNotSupportedException.java \
    VRMLParseException.java \
    ImportFileFormatException.java \
    Locator.java \
    ErrorHandler.java \
    ProtoHandler.java \
    ContentHandler.java \
    StringContentHandler.java \
    BinaryContentHandler.java \
    ScriptHandler.java \
    RouteHandler.java \
    InputSource.java \
    VRMLReader.java \

# The list of other files we need to copy from this directory to the classes
# directory when we are making JAR files.
EXTRA =

# If the package requires extra classes outside the JDK, the JAR files that
# are required are listed here. These should be found in ${PROJECT_ROOT}/lib
JARS_3RDPARTY = uri.jar j3d-org-all_0.9.0.jar


include ${PROJECT_ROOT}/make/Makefile.jav
