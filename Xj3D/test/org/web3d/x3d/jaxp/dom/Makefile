#*********************************************************************
#
#  (C) 2001 Web3d Consortium
#    http://www.web3d.org/
# 
# Package makefile for the org.web3d.x3d.jaxp.dom directory
#
# Author: Justin Couch
# Version: $Revision: 1.1 $
#
#*********************************************************************

PACKAGE = org.web3d.x3d.jaxp.dom

# The source files to compile. These are declared in order that they must be
# compiled in
SOURCE = IdCollector.java \
    ProfileInfo.java \
    NodePath.java \
	ChildNodeList.java \
	X3DEventTarget.java \
	DOMEvent.java \
	DOMEventNames.java \
	DOMMutationEvent.java \
	DOMUIEvent.java \
	DOMMouseEvent.java \
	DOMEventHandler.java \
	X3DDOMEventGenerator.java\
	X3DNode.java \
	X3DNamedNodeMap.java \
	X3DDTDNodeMap.java \
	X3DDocumentType.java \
	X3DCharacterData.java \
	X3DText.java \
	X3DComment.java \
	X3DCharacterData.java \
	X3DCDATASection.java \
	X3DEntity.java \
	X3DEntityReference.java \
	X3DNotation.java \
	X3DProcessingInstruction.java \
	X3DDocumentFragment.java \
	X3DAttr.java \
	AttributeFactory.java \
	DefaultAttributeFactory.java \
	X3DElement.java \
	ElementFactory.java \
	DefaultElementFactory.java \
	X3DImplementation.java \
	X3DDocument.java \
	X3DSAXDocumentBuilder.java

# The list of other files we need to copy from this directory to the classes
# directory when we are making JAR files.
EXTRA = x3d.props

JARS_3RDPARTY=dtdparser113a.jar

include ${PROJECT_ROOT}/make/Makefile.jav
