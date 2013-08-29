#*********************************************************************
#
#  (C) 2001 Web3d Consortium
#    http://www.web3d.org/
#
# Package makefile for the org.web3d.vrml.sav directory
#
# Author: Alan Hudson
# Version: $Revision: 1.13 $
#
#*********************************************************************

PACKAGE = org.web3d.vrml.export.compressors

# The source files to compile. These are declared in order that they must be
# compiled in
SOURCE = BitPacker.java \
        BitUnpacker.java \
        FloatPacker.java \
    HuffmanNode.java \
    IntegerHuffmanNode.java \
    ByteHuffmanNode.java \
    ShortHuffmanNode.java \
    HuffmanTable.java \
    CodeInputUnpacker.java \
    CodeOutputPacker.java \
    LZW.java \
    CompressionTools.java \
    FieldCompressor.java \
    FieldDecompressor.java \
    NodeCompressor.java \
    SceneGraphCompressor.java \
    BinaryFieldEncoder.java \
    RangeCompressor.java \
    GeometryHeader.java \

# The list of other files we need to copy from this directory to the classes
# directory when we are making JAR files.
EXTRA =

# If the package requires extra classes outside the JDK, the JAR files that
# are required are listed here. These should be found in ${PROJECT_ROOT}/lib
JARS_3RDPARTY =


include ${PROJECT_ROOT}/make/Makefile.jav
