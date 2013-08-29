#*********************************************************************
#
#  (C) 2001 Web3d Consortium
#    http://www.web3d.org/
#
# Author: Justin Couch
# Version: $Revision: 1.2 $
#
#*********************************************************************

# Names of packages to be compiled. These are declared in the order that
# they are to be compiled in. We create one at a time
BUILD_ORDER = web3d.install \
  web3d.install.applet

# Names of packages that we want included in the javadoc
JAVADOC_PACKAGES = web3d.install

# Names of JAR files we want to create
JARS = web3d-installer.jar \
  demo_applet.jar

# 3rd party libraries that need including.
JARS_3RDPARTY=

# Definitions of setup information if we are making a self-executable JAR
MAIN_CLASS     =
MAIN_PACKAGE   =
MAIN_JAR       =

#*********************************************************************
#
# Javadoc
#
#*********************************************************************

OVERVIEW    =
WINDOWTITLE = 'Web3D Installer Application'
DOCTITLE    = 'Web3D Installer Application'
HEADER      = 'Web3D Installer Application'
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
