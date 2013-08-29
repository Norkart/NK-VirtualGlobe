#*********************************************************************
#
#  (C) 2001 Web3d Consortium
#    http://www.web3d.org/
#
# Package makefile for the xj3d.converter directory
#
# Author: Alan Hudson
# Version: $Revision: 1.20 $
#
#*********************************************************************

PACKAGE = xj3d.filter

# The source files to compile. These are declared in order that they must be
# compiled in
SOURCE = \
  FilterProcessingException.java \
  FilterErrorReporter.java \
  FieldValueHandler.java \
  NonWeb3DFileParser.java \
  FileParserReader.java \
  ParserNameMap.java \
  AbstractFilter.java \
  TwoPassFilter.java \
  TwoPassFilterWrapper.java \
  IdentityFilter.java \
  DEFUSEImageTextureFilter.java \
  IFSToTSFilter.java \
  IFSToITSFilter.java \
  MinimizeProfileFilter.java \
  TriangleCountInfoFilter.java \
  ShortenDEFFilter.java \
  ModifyViewpointFilter.java \
  AbsScaleFilter.java \
  CDFFilter.java \

# The list of other files we need to copy from this directory to the classes
# directory when we are making JAR files.
EXTRA =

# If the package requires extra classes outside the JDK, the JAR files that
# are required are listed here. These should be found in ${PROJECT_ROOT}/lib
JARS_3RDPARTY=uri.jar vlc.jar j3d-org-all_0.9.0.jar vecmath.jar

include ${PROJECT_ROOT}/make/Makefile.jav
