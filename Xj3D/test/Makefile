#*********************************************************************
#
#  (C) 2001 Web3d Consortium
#    http://www.web3d.org/
#
# Author: Justin Couch
# Version: $Revision: 1.3 $
#
#*********************************************************************

# Names of packages to be compiled. These are declared in the order that
# they are to be compiled in. We create one at a time
BUILD_ORDER = org.web3d.util \
  org.web3d.vrml.sav \
  org.web3d.vrml.nodes \
  org.web3d.vrml.parser.vrml97 \
  org.web3d.vrml.parser \
  org.web3d.vrml.nodes.loader \
  org.web3d.vrml.nodes.proto \
  org.web3d.vrml.renderer \
  org.web3d.vrml.renderer.j3d \
  org.web3d.x3d.dom.j3d \
  org.web3d.j3d.loaders \
  org.web3d.x3d.jaxp \
  org.web3d.x3d.jaxp.dom \

# Names of packages that we want included in the javadoc
JAVADOC_PACKAGES = \
  org.web3d.util \
  org.web3d.vrml.sav \
  org.web3d.vrml.nodes \
  org.web3d.vrml.parser.vrml97 \
  org.web3d.vrml.parser \
  org.web3d.vrml.nodes.loader \
  org.web3d.vrml.nodes.proto \
  org.web3d.vrml.renderer \
  org.web3d.vrml.renderer.j3d \
  org.web3d.x3d.dom.j3d \
  org.web3d.j3d.loaders \
  org.web3d.x3d.jaxp \
  org.web3d.x3d.jaxp.dom \

# Names of JAR files we want to create
JARS = xj3d-core.jar    \
  xj3d-sav.jar \
  xj3d-parser.jar \
  xj3d-script-base.jar \
  xj3d-net.jar \
  xj3d-runtime.jar \
  xj3d-jsai.jar \
  xj3d-ecmascript.jar \
  xj3d-eai.jar \
  xj3d-norender.jar \
  xj3d-j3d.jar \
  xj3d-xml-util.jar \
  xj3d-render.jar \
  xj3d-common.jar \
  xj3d-ogl-sg.jar \
  xj3d-ogl.jar \
  xj3d-mobile-sg.jar \
  xj3d-mobile.jar \
  xj3d-sai.jar \
  xj3d-jaxp.jar \
  xj3d-vecmath.jar \
# xj3d-all.jar

# 3rd party libraries that need including.
JARS_3RDPARTY=j3d-org-all_0.9.0.jar uri.jar

# The list of other files we need to copy from this directory to the classes
# directory when we are making JAR files.
EXTRA = vrml.properties vrml97.properties xj3d.properties interactive.properties \
    interchange.properties x3d.properties

# Definitions of setup information if we are making a self-executable JAR
MAIN_CLASS     =
MAIN_PACKAGE   =
MAIN_JAR       = xj3d.jar

#*********************************************************************
#
# Javadoc
#
#*********************************************************************

OVERVIEW    = overview.html
WINDOWTITLE = 'Xj3D Code Library'
DOCTITLE    = 'Xj3D VRML/X3D Code API'
HEADER      = 'Xj3D VRML/X3D Code API'
BOTTOM      = '<font size=2>Copyright &copy; 2001 - \
               <a href=\"http://www.web3d.org\">Web3D Consortium</a> \
               </font>'

# A list of the local directories that contain other Javadoc that we want
# to link to. For example we link to the JDK, JAXP, SAI and JUnit Javadocs
# here. Note that these *must* be full URLs and not just local directory
# references. If your directory path contains a space in it, you may be in
# trouble. We haven't tested it with that. Try using double quotes inside
# the single quotes to get the space treated in the file name
LINK_URLS   =

include $(PROJECT_ROOT)/make/Makefile.jav
