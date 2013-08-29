#*********************************************************************
#
#  (C) 2001 Web3d Consortium
#    http://www.web3d.org/
#
# Package makefile for the org.web3d.vrml.renderer.common.input directory
#
# Author: Justin Couch
# Version: $Revision: 1.6 $
#
#*********************************************************************

PACKAGE = org.web3d.vrml.renderer.common.input.dis

# The source files to compile. These are declared in order that they must be
# compiled in
SOURCE = DISId.java \
         DISConnectionId.java \
         SolverTarget.java \
         Solver.java \
         RungeKuttaSolver.java \
         Vector3dVariable.java \
         Quat4dVariable.java \
         OrderNQuat4dConverger.java \
         OrderNVector3dConverger.java \
         ListEntry.java \
         LiveListEntry.java \
         LiveListEntryDX.java \
         WriterListEntry.java \
         WriterListEntryDX.java \
         LinkedList.java \
         NodeMapEntry.java \
         DISXMLConnectionHandler.java \
         WriterMapEntry.java \
         WriterMapEntryDX.java \
         DISProtocolHandler.java \
         DISXMLProtocolHandler.java

# The list of other files we need to copy from this directory to the classes
# directory when we are making JAR files.
EXTRA =

# If the package requires extra classes outside the JDK, the JAR files that
# are required are listed here. These should be found in ${PROJECT_ROOT}/lib
JARS_3RDPARTY= dis.jar disxml.jar smack.jar smackx.jar vecmath.jar

include ${PROJECT_ROOT}/make/Makefile.jav
