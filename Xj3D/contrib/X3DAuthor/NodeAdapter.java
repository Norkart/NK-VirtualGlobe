//
//   NodeAdapter.java
//
//------------------------------------------------------------------------
//
//      Portions Copyright (c) 2000 SURVICE Engineering Company.
//      All Rights Reserved.
//      This file contains Original Code and/or Modifications of Original
//      Code as defined in and that are subject to the SURVICE Public
//      Source License (Version 1.3, dated March 12, 2002)
//
//      A copy of this license can be found in the doc directory
//------------------------------------------------------------------------
//
//      Developed by SURVICE Engineering Co. (www.survice.com)
//      April 2002
//
//      Authors:
//              Bob Parker
//------------------------------------------------------------------------
//
//      Description:
//              Adapts a DOM node for use with the DefaultTreeModel.

import java.util.*;

import javax.swing.tree.*;

import org.w3c.dom.*;

public class NodeAdapter implements TreeNode, Enumeration { 
    Node dnode;

    protected int nelements = 0;
    protected int currElement = 0;
    protected short type;

    static final short SCENEGRAPH_TYPE = 0;
    static final short CONTENT_REPOSITORY_TYPE = 1;

    static final short BEGINNER_MODE = 0;
    static final short EXPERT_MODE = 1;
    static short mode = EXPERT_MODE;

    // Construct an Adapter node from a DOM node
    public NodeAdapter(Node node) {
	this(node, SCENEGRAPH_TYPE);
    }

    public NodeAdapter(Node node, short _type) {
	dnode = node;
	type = _type;
    }

    public String toString() {
	switch (type) {
	case SCENEGRAPH_TYPE:
	    if (dnode == null)
		return "";

	    if (dnode.getNodeType() == Node.CDATA_SECTION_NODE)
		return "CDATA: javascript: ...";

	    switch (mode) {
	    case BEGINNER_MODE:
		String beginnerAttr = getBeginnerAttr();

		if (beginnerAttr.equals(""))
		    return dnode.getNodeName();

		return beginnerAttr;
	    case EXPERT_MODE:
	    default:
		String expertAttr = getExpertAttr();

		if (expertAttr.equals(""))
		    return dnode.getNodeName();

		return dnode.getNodeName() + ": " + expertAttr;
	    }
	case CONTENT_REPOSITORY_TYPE:
	    Node node;
	    NamedNodeMap nodeMap = dnode.getAttributes();

	    if (nodeMap == null)
		break;

	    if ((node = nodeMap.getNamedItem("name")) == null)
		break;

	    return node.getNodeValue();
	}

	return "Error in repository!";
    }

    public String getExpertAttr() {
	String s = "";
	Node node = null;

	if (dnode == null)
	    return s;

	NamedNodeMap nodeMap = dnode.getAttributes();

	if (nodeMap == null) {
	    return s;
	}

	for (int i = 0; i < nodeMap.getLength(); i++) {
	    if (0 < i)
		s += ", ";

	    node = nodeMap.item(i);
	    s += node.getNodeName();
	    s += ": ";
	    s += node.getNodeValue();
	}

	return s;
    }

    public String getBeginnerAttr() {
	String s = "";
	Node node;

	if (dnode == null)
	    return s;

	if (dnode.getNodeName().equals("ROUTE"))
	    return getRouteAttr();

	NamedNodeMap nodeMap = dnode.getAttributes();

	if (nodeMap == null) {
	    return s;
	}

	node = nodeMap.getNamedItem("DEF");
	if (node == null)
	    return s;

	s += node.getNodeValue();

	return s;
    }

    public String getRouteAttr() {
	String s = "";
	Node node;
	NamedNodeMap nodeMap = dnode.getAttributes();

	
	if ((node = nodeMap.getNamedItem("fromNode")) == null)
	    return s;

	s += "ROUTE: from " + node.getNodeValue();

	if ((node = nodeMap.getNamedItem("toNode")) == null)
	    return s;

	return s + " to " + node.getNodeValue();
    }

    public boolean hasMoreElements() {
	if (currElement < nelements)
	    return true;

	return false;
    }

    public Object nextElement() {
	if (dnode == null)
	    return null;

	if (currElement < nelements)
	    return new NodeAdapter(dnode.getChildNodes().item(currElement++), type);

	return null;
    }

    public Enumeration children() {
	if (dnode == null)
	    return this;

	nelements = dnode.getChildNodes().getLength();
	currElement = 0;
	return this;
    }

    public boolean getAllowsChildren() {
	return true;
    }

    /* 
     * Returns the n'th displayable child.
     */
    public TreeNode getChildAt(int n) {
	if (dnode == null)
	    return null;

	Node node = null;
	NodeList children = dnode.getChildNodes();
	int elementNodeIndex = 0;
	short type;

	for (int i = 0; i < children.getLength(); i++) {
	    node = children.item(i);
	    type = node.getNodeType();
	    if ((type == Node.ELEMENT_NODE ||
		 type == Node.CDATA_SECTION_NODE) &&
		elementNodeIndex++ == n) {
		break; 
	    }
	}

	return new NodeAdapter(node, this.type); 
    }

    /*
     * Returns the count of displayable child nodes.
     * Here we display only elements.
     */
    public int getChildCount() {
	if (dnode == null)
	    return 0;

	Node node;
	NodeList children = dnode.getChildNodes();
	int count = 0;
	short type;

	for (int i = 0; i < children.getLength(); i++) {
	    node = children.item(i); 
	    type = node.getNodeType();
	    if (type == Node.ELEMENT_NODE ||
		type == Node.CDATA_SECTION_NODE) {
		++count;
	    }
	}

	return count;
    }

    public int getIndex(TreeNode child) {
	int count = getChildCount();

	for (int i = 0; i < count; i++) {
	    TreeNode n = getChildAt(i);
	    if (((NodeAdapter)child).dnode == ((NodeAdapter)n).dnode)
		return i;
	}

	return -1;
    }

    public TreeNode getParent() {
	Node node = null;

	//return new NodeAdapter(dnode, type); 

	//XXX punting for now, please fix
	return null;
    }

    public boolean isLeaf() {
	if (getChildCount() > 0)
	    return false;

	return true;
    }
}

