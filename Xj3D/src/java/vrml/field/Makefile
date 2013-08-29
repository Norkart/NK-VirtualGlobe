#*********************************************************************
#
#  (C) 2001 Web3d Consortium
#    http://www.web3d.org/
# 
# Package makefile for the org.web3d.vrml.j3d.nodes.core directory
#
# Author: Justin Couch
# Version: $Revision: 1.5 $
#
#*********************************************************************

PACKAGE = vrml.field

# The source files to compile. These are declared in order that they must be
# compiled in
SOURCE = ConstMFColor.java \
    ConstMFFloat.java \
    ConstMFInt32.java \
    ConstMFNode.java \
    ConstMFRotation.java \
    ConstMFString.java \
    ConstMFTime.java \
    ConstMFVec2f.java \
    ConstMFVec3f.java \
    ConstSFBool.java \
    ConstSFColor.java \
    ConstSFFloat.java \
    ConstSFImage.java \
    ConstSFInt32.java \
    ConstSFNode.java \
    ConstSFRotation.java \
    ConstSFString.java \
    ConstSFTime.java \
    ConstSFVec2f.java \
    ConstSFVec3f.java \
    MFColor.java \
    MFFloat.java \
    MFInt32.java \
    MFNode.java \
    MFRotation.java \
    MFString.java \
    MFTime.java \
    MFVec2f.java \
    MFVec3f.java \
    SFBool.java \
    SFColor.java \
    SFFloat.java \
    SFImage.java \
    SFInt32.java \
    SFNode.java \
    SFRotation.java \
    SFString.java \
    SFTime.java \
    SFVec2f.java \
    SFVec3f.java 

# The list of other files we need to copy from this directory to the classes
# directory when we are making JAR files.
EXTRA = 

# If the package requires extra classes outside the JDK, the JAR files that
# are required are listed here. These should be found in ${PROJECT_ROOT}/lib
JARS_3RDPARTY=

# Define IGNORE_CYCLES here because there is a known cyclic dependency in
# the EAI packages between vrml, vrml.field. and vrml.node
IGNORE_CYCLES=1

include ${PROJECT_ROOT}/make/Makefile.jav
