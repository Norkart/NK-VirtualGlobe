#*********************************************************************
#
#  (C) 2001 Web3d Consortium
#    http://www.web3d.org/
# 
# Package makefile for the org.web3d.vrml.parser.vrml97 directory
#
# Author: Justin Couch
# Version: $Revision: 1.5 $
#
#*********************************************************************

PACKAGE = org.web3d.parser.vrml97

# The source files to compile. These are declared in order that they must be
# compiled in
SOURCE = JavaCharStream.java \
  Token.java \
  ParseException.java \
  TokenMgrError.java \
  VRML97RelaxedParserConstants.java \
  VRML97RelaxedParserTokenManager.java \
  VRML97RelaxedParser.java \
  VRML97Reader.java \
  VRML97FieldParserConstants.java \
  VRML97FieldParserTokenManager.java \
  VRML97FieldParser.java \
  VRML97FieldReader.java 

# The list of other files we need to copy from this directory to the classes
# directory when we are making JAR files.
EXTRA = 

# If the package requires extra classes outside the JDK, the JAR files that 
# are required are listed here. These should be found in ${PROJECT_ROOT}/lib
JARS_3RDPARTY= j3d-org-all_0.9.0.jar

include ${PROJECT_ROOT}/make/Makefile.jav
