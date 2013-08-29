#*********************************************************************
#
#  (C) 2001 Web3d Consortium
#    http://www.web3d.org/
#
# Package makefile for the org.web3d.util directory
#
# Author: Justin Couch
# Version: $Revision: 1.37 $
#
#*********************************************************************

PACKAGE = org.web3d.util

# The source files to compile. These are declared in order that they must be
# compiled in
SOURCE = ErrorReporter.java \
    DefaultErrorReporter.java \
    FileHandler.java \
    ShortHashMap.java \
    IntHashMap.java \
    SimpleStack.java \
    BooleanStack.java \
    IntStack.java \
    FloatArray.java \
    DoubleArray.java \
    IntArray.java \
    LongArray.java \
    StringArray.java \
    BooleanArray.java \
    ObjectArray.java \
    Queue.java \
    SFImageUtils.java \
    BlockingQueue.java \
    HashSet.java \
    ArrayUtils.java \
    ColorUtils.java \
    WeakValueHashMap.java \
    SoftValueHashMap.java \
    XMLTools.java \
    PropertyTools.java \
    MathUtils.java \

# The list of other files we need to copy from this directory to the classes
# directory when we are making JAR files.
EXTRA =

# If the package requires extra classes outside the JDK, the JAR files that
# are required are listed here. These should be found in ${PROJECT_ROOT}/lib
JARS_3RDPARTY = j3d-org-all_0.9.0.jar

include ${PROJECT_ROOT}/make/Makefile.jav
