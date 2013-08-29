#*********************************************************************
#
#  (C) 2001 - 2005 Web3d Consortium
#    http://www.web3d.org/
#
# Package makefile for the org.web3d.x3d.jaxp directory
#
# Author: Justin Couch
# Version: $Revision: 1.12 $
#
#*********************************************************************

PACKAGE = org.web3d.x3d.jaxp

# The source files to compile. These are declared in order that they must be
# compiled in
SOURCE = \
    X3DConstants.java \
    LocatorAdapter.java \
    X3DErrorHandler.java \
    X3DEntityResolver.java \
    X3DSAVAdapter.java \
    XMLReader.java \

# The list of other files we need to copy from this directory to the classes
# directory when we are making JAR files.
EXTRA = 

# If the package requires extra classes outside the JDK, the JAR files that
# are required are listed here. These should be found in ${PROJECT_ROOT}/lib
JARS_3RDPARTY = j3d-org-all_0.9.0.jar

include ${PROJECT_ROOT}/make/Makefile.jav
