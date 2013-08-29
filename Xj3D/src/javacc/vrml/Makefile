#*********************************************************************
#
#  (C) 2001 Web3d Consortium
#    http://www.web3d.org/
# 
# Author: Justin Couch
# Version: $Revision: 1.4 $
#
#*********************************************************************

GRAMMAR=vrml

OUTPUT_PACKAGE=org.web3d.parser.vrml97

# Names of grammars to be compiled. These are declared in the order that
# they are to be generated in. We create one at a time
SOURCE = VRML97Relaxed.jj \
         VRML97Field.jj
#        VRML97Strict.jj

include $(PROJECT_ROOT)/make/Makefile.javacc
