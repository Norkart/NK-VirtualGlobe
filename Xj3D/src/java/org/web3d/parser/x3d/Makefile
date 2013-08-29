#*********************************************************************
#
#  (C) 2001 Web3d Consortium
#    http://www.web3d.org/
# 
# Package makefile for the org.web3d.parser.x3d directory
#
# Author: Justin Couch
# Version: $Revision: 1.8 $
#
#*********************************************************************

PACKAGE = org.web3d.parser.x3d

# The source files to compile. These are declared in order that they must be
# compiled in
SOURCE = JavaCharStream.java \
  Token.java \
  ParseException.java \
  TokenMgrError.java \
  X3DRelaxedParserConstants.java \
  X3DRelaxedParserTokenManager.java \
  X3DRelaxedParser.java \
  X3DFieldParserConstants.java \
  X3DFieldParserTokenManager.java \
  X3DFieldParser.java \
  X3DFieldReader.java \
  ByteEncodingAlgorithm.java \
  DeltazlibIntArrayAlgorithm.java \
  QuantizedzlibFloatArrayAlgorithm.java \
  X3DBinaryVocabulary.java \
  FastInfosetElementReader.java \
  X3DBinaryParser.java \
  VRMLEncodedReader.java \
  X3DReader.java

# The list of other files we need to copy from this directory to the classes
# directory when we are making JAR files.
EXTRA = 

# If the package requires extra classes outside the JDK, the JAR files that 
# are required are listed here. These should be found in ${PROJECT_ROOT}/lib
JARS_3RDPARTY= j3d-org-all_0.9.0.jar FastInfoset.jar

include ${PROJECT_ROOT}/make/Makefile.jav
