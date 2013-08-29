#*********************************************************************
#
#  (C) 2001 Web3d Consortium
#    http://www.web3d.org/
# 
# Package makefile for the org.web3d.net.content directory
#
# Author: Justin Couch
# Version: $Revision: 1.3 $
#
#*********************************************************************

PACKAGE = org.web3d.net.protocol

# The source files to compile. These are declared in order that they must be
# compiled in
SOURCE = JavascriptResourceConnection.java \
  JavascriptResourceStream.java \
  JavascriptResourceFactory.java \
  JarResourceConnection.java \
  JarResourceStream.java \
  JarResourceFactory.java \
  FileResourceConnection.java \
  FileResourceStream.java \
  X3DResourceFactory.java \
  VRML97ResourceFactory.java \
  Web3DResourceFactory.java \

# The list of other files we need to copy from this directory to the classes
# directory when we are making JAR files.
EXTRA = 

JARS_3RDPARTY=uri.jar vlc_uri.jar

include ${PROJECT_ROOT}/make/Makefile.jav
