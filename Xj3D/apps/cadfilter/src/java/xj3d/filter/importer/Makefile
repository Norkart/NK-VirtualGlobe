#*********************************************************************
#
#  (C) 2001 Web3d Consortium
#    http://www.web3d.org/
#
# Package makefile for the xj3d.filter.importer directory
#
# Author: Alan Hudson
# Version: $Revision: 1.9 $
#
#*********************************************************************

PACKAGE = xj3d.filter.importer

# The source files to compile. These are declared in order that they must be
# compiled in
SOURCE = \
  ImportUtils.java \
  STLFileParser.java \
  ColladaStrings.java \
  InstanceMaterial.java \
  BindMaterial.java \
  Vcount.java \
  P.java \
  Param.java \
  Array.java \
  Accessor.java \
  Source.java \
  Input.java \
  Sampler.java \
  Channel.java \
  TransformElement.java \
  Lookat.java \
  Matrix.java \
  Rotate.java \
  Scale.java \
  Skew.java \
  Translate.java \
  TransformUtils.java \
  ColladaFileParser.java \

# The list of other files we need to copy from this directory to the classes
# directory when we are making JAR files.
EXTRA =

# If the package requires extra classes outside the JDK, the JAR files that
# are required are listed here. These should be found in ${PROJECT_ROOT}/lib
JARS_3RDPARTY=j3d-org-all_0.9.0.jar

include ${PROJECT_ROOT}/make/Makefile.jav
