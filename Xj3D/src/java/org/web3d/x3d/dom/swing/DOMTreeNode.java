/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2005
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.x3d.dom.swing;

// External imports
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;

import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

// Local imports
// None

/**
 * An implementation of the MutableTreeNode that represents a single DOM node
 * within a JTree.
 * <p>
 *
 * The model is specifically tailored to display DOM objects. The model is
 * configured so that it should always ask if it allows children as some
 * DOM nodes cannot have children. This class does not add event listeners
 * as it assumes that the TreeModel it is a part of will do that for us.
 * <p>
 * Our basic assumption is that this tree node is dumb. It only looks after
 * its tree node children. If the DOM node it represents changes underneath it
 * then someone needs to tell it that.
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
public class DOMTreeNode implements MutableTreeNode {

    /** The DOM Tree node this represents */
    private Node node;

    /** The user's stored data */
    private Object userData;

    /**
     * The direct children of this node. The list contains both attribute and
     * element children of this node. All attributes appear first in the list.
     */
    private ArrayList children;

    /** Mapping of nodes (key) to TreeNode (value) for reverse lookups */
    private HashMap nodeMap;

    /** Flag indicating this is a leaf node */
    private boolean leaf = false;

    /** The parent node of this one */
    private DOMTreeNode parent;

    /**
     * Flag to indicate we've loaded the kids for this node. Package access
     * because it is set directly by the DOMTreeModel
     */
    boolean kidsLoaded = false;

    /**
     * Create an instance of the tree node that represents the given DOM Node.
     * If the node allows events, then this will register itself as a
     * listener.
     *
     * @param node The DOM node this tree node represents
     */
    public DOMTreeNode(Node node, DOMTreeNode parent) {
        this.node = node;
        this.parent = parent;

        children = new ArrayList();
        nodeMap = new HashMap();

        checkLeaf();
    }

    //----------------------------------------------------------
    // Methods required by MutatableTreeNode.
    //----------------------------------------------------------

    /**
     * Insert the child at the given position.
     *
     * @param child The new child to insert
     * @param index The position to insert the child into
     */
    public void insert(MutableTreeNode child, int index) {
        if(index == children.size())
            children.add(child);
        else
            children.add(index, child);

        Node n = ((DOMTreeNode)child).getNode();
        nodeMap.put(n, child);
    }

    /**
     * Remove the child at the given index position. If there is no child
     * there it will do nothing.
     */
    public void remove(int index) {
        children.remove(index);
    }

    /**
     * Remove the given tree node from the list of children of this node.
     *
     * @param child The node to remove
     */
    public void remove(MutableTreeNode child) {
        children.remove(child);
    }

    /**
     * Remove this node from it's parent. If this is the root node then this
     * will ignore the request.
     */
    public void removeFromParent() {
        if(parent == null)
            return;

        parent.remove(this);
    }

    /**
     * Set the parent node of this node to the new value.
     *
     * @param parent The new node to use as a parent
     */
    public void setParent(MutableTreeNode parent) {
        this.parent = (DOMTreeNode)parent;
    }

    /**
     * Add some user data to this object.
     *
     * @param obj The data to be stored
     */
    public void setUserObject(Object obj) {
        userData = obj;
    }

    //----------------------------------------------------------
    // Methods required by TreeNode.
    //----------------------------------------------------------

    /**
     * Get the list of children of this node as an enumeration. If the node
     * could have children, but does not at the moment, it will return an
     * empty enumeration.
     *
     * @return An enumeration, possibly empty of the children
     */
    public Enumeration children() {
        return Collections.enumeration(children);
    }

    /**
     * Check to see if this node allows children. For the purposes of the DOM
     * view of the world, a leaf and allowing children are the same thing. We
     * do not consider whether the node is an X3D node type or not.
     *
     * @return true if this node allows children
     */
    public boolean getAllowsChildren() {
        return leaf;
    }

    /**
     * Get the child at the given index position. If there is no child there
     * it will return null.
     *
     * @param index The position to check
     * @return The tree node at the index
     */
    public TreeNode getChildAt(int index) {
        return (TreeNode)children.get(index);
    }

    /**
     * Get the number of children of this node. The children count is of the
     * tree node children, not the DOM children. Tree children includes the
     * attributes as well
     *
     * @return The number of children of this child
     */
    public int getChildCount() {
        return children.size();
    }

    /**
     * Get the index of the given tree node.
     *
     * @param node The node to find the index of
     * @return The index of the given node or -1 if not found
     */
    public int getIndex(TreeNode node) {
        return children.indexOf(node);
    }

    /**
     * Get the parent node of this node. If this is the root of the tree, the
     * return value is null.
     *
     * @return The parent node
     */
    public TreeNode getParent() {
        return parent;
    }

    /**
     * Check to see if this instance is a leaf node
     *
     * @return true if this is a leaf and cannot have children
     */
    public boolean isLeaf() {
        return leaf;
    }

    //----------------------------------------------------------
    // Miscellaneous local methods.
    //----------------------------------------------------------

    /**
     * Get the DOM node that this tree node represents. Used by the renderer
     * to build custom information about the node type.
     *
     * @return The DOM node
     */
    public Node getNode() {
        return node;
    }

    /**
     * Get the user data stored in this object.
     *
     * @return The currently set user data
     */
    public Object getUserData() {
        return userData;
    }

    /**
     * Do a reverse lookup of the children to find the tree node that
     * corresponds to the given Node instance.
     */
    DOMTreeNode getTreeNodeChild(Node child) {
        return (DOMTreeNode)nodeMap.get(child);
    }

    /**
     * Determine once whether this is a leaf node. Saves having to do it
     * dynamically each time it is asked
     */
    private void checkLeaf() {
        int type = node.getNodeType();

        // Nothing is a leaf unless we explicitly say it is
        switch(type) {
            case Node.ATTRIBUTE_NODE:
            case Node.COMMENT_NODE:
            case Node.DOCUMENT_TYPE_NODE:
            case Node.NOTATION_NODE:
            case Node.TEXT_NODE:
            case Node.CDATA_SECTION_NODE:
            case Node.PROCESSING_INSTRUCTION_NODE:
                leaf = true;
        }
    }
}
