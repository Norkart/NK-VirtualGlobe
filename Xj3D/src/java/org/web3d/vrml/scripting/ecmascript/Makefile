#*********************************************************************
#
#  (C) 2001 Web3d Consortium
#    http://www.web3d.org/
#
# Package makefile for the org.web3d.vrml.scripting.jsai directory
#
# Author: Justin Couch
# Version: $Revision: 1.9 $
#
#*********************************************************************

PACKAGE = org.web3d.vrml.scripting.ecmascript

# The source files to compile. These are declared in order that they must be
# compiled in
SOURCE = \
  ECMABrowserFactory.java \
  X3DScriptContext.java \
  ReportAdapter.java \
  ECMAScriptWrapper.java \
  Global.java \
  ECMAScriptEngine.java \
  JavascriptBrowser.java \
  JavascriptBrowserFactory.java \
  JavascriptScriptContext.java \
  JavascriptScriptWrapper.java \
  JavascriptScriptEngine.java

# The list of other files we need to copy from this directory to the classes
# directory when we are making JAR files.
EXTRA =

# If the package requires extra classes outside the JDK, the JAR files that
# are required are listed here. These should be found in ${PROJECT_ROOT}/lib
JARS_3RDPARTY=j3d-org-all_0.9.0.jar js.jar


include ${PROJECT_ROOT}/make/Makefile.jav
