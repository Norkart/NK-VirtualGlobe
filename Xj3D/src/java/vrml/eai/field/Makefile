#*********************************************************************
#
#  (C) 2001 Web3d Consortium
#    http://www.web3d.org/
# 
# Package makefile for the vrml.eai.field directory
#
# Author: Justin Couch
# Version: $Revision: 1.2 $
#
#*********************************************************************

PACKAGE = vrml.eai.field

# The source files to compile. These are declared in order that they must be
# compiled in
SOURCE = InvalidFieldException.java \
  InvalidEventInException.java \
  InvalidEventOutException.java \
  BaseField.java \
  EventIn.java \
  EventOut.java \
  EventInSFBool.java \
  EventInSFColor.java \
  EventInSFFloat.java \
  EventInSFImage.java \
  EventInSFInt32.java \
  EventInSFNode.java \
  EventInSFRotation.java \
  EventInSFString.java \
  EventInSFTime.java \
  EventInSFVec2f.java \
  EventInSFVec3f.java \
  EventInMFColor.java \
  EventInMFFloat.java \
  EventInMFInt32.java \
  EventInMFNode.java \
  EventInMFRotation.java \
  EventInMFString.java \
  EventInMFTime.java \
  EventInMFVec2f.java \
  EventInMFVec3f.java \
  EventOutSFBool.java \
  EventOutSFColor.java \
  EventOutSFFloat.java \
  EventOutSFImage.java \
  EventOutSFInt32.java \
  EventOutSFNode.java \
  EventOutSFRotation.java \
  EventOutSFString.java \
  EventOutSFTime.java \
  EventOutSFVec2f.java \
  EventOutSFVec3f.java \
  EventOutMField.java \
  EventOutMFColor.java \
  EventOutMFFloat.java \
  EventOutMFInt32.java \
  EventOutMFNode.java \
  EventOutMFRotation.java \
  EventOutMFString.java \
  EventOutMFTime.java \
  EventOutMFVec2f.java \
  EventOutMFVec3f.java

# The list of other files we need to copy from this directory to the classes
# directory when we are making JAR files.
EXTRA = 

# If the package requires extra classes outside the JDK, the JAR files that
# are required are listed here. These should be found in ${PROJECT_ROOT}/lib
JARS_3RDPARTY=

# Define IGNORE_CYCLES here because there is a known cyclic dependency in
# the EAI packages between vrml.eai, vrml.eai.event and vrml.eai.field
IGNORE_CYCLES=1

include ${PROJECT_ROOT}/make/Makefile.jav
