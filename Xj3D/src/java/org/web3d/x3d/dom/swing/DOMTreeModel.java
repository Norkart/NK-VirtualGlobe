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
import java.util.Iterator;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeModel;

import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.events.MutationEvent;

// Local imports
// None

/**
 * A complex implementation of the TreeModel that is used for dealing with DOM
 * tree in combination with the cell renderer and DOMTreeNode classes.
 * <p>
 * This implementation is for generic DOM representations. It does not have any
 * X3D specific handling. That is left to derived classes.
 * <p>
 * The model is specifically tailored to display DOM objects. The model is
 * configured so that it should always ask if it allows children as some
 * DOM nodes cannot have children. If the Document allows events then this
 * will automatically add listeners and build the correct tree as the
 * underlying DOM changes.
 * <p>
 * Each of the objects represented by this model will be the DOMTreeNode
 * class. Building of these objects is lazy for memory consumption purposes.
 * We do not build the child DOMTreeNode object until we have to.
 * <p>
 * The tree will also show attribute information as nodes in the tree. In order
 * to maintain consistency, we will need to make sure that the indexes are
 * always in the same order. To do this, we always put the attributes first
 * and follow with child nodes.
 * <p>
 * To keep itself consistent, we only register a listener at the root of the
 * document. This is for node add, remove and attribute modified. Because
 * events bubble for these types, there is no need to register an event
 * listener on every single node instance. We just listen at the document root
 * for the events that bubble up to us.
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
public class DOMTreeModel implements TreeModel, EventListener {

    /** The document representing the root of the tree */
    private DOMTreeNode domRoot;

    /** The root tree node of this model */
    /** The list of listeners registered with this model */
    private ArrayList listeners;

    /** Flag to indicate we support events */
    private boolean hasDOMEvents = false;

    /**
     * Create a new tree model that represents the given document root.
     *
     * @param root The DOM node representing the root of the tree
     */
    public DOMTreeModel(Node root) {
        domRoot = new DOMTreeNode(root, null);
        listeners = new ArrayList();

        hasDOMEvents = root.isSupported("MutationEvents", "2.0");

        buildChildren(domRoot);

        // Finally register events for the root node.
        if(hasDOMEvents) {
            EventTarget tgt = (EventTarget)root;
            tgt.addEventListener("DOMNodeInserted", this, false);
            tgt.addEventListener("DOMNodeRemoved", this, false);
            tgt.addEventListener("DOMAttrModified", this, false);
        }
    }

    //----------------------------------------------------------
    // Methods required by TreeModel.
    //----------------------------------------------------------

    /**
     * Get the child of the given parent at that index.
     *
     * @param parent The parent node to ask
     * @param index The position to get the child for
     * @return The TreeNode object at that position
     */
    public Object getChild(Object parent, int index) {
        DOMTreeNode tree_node = (DOMTreeNode)parent;
        DOMTreeNode kid = (DOMTreeNode)tree_node.getChildAt(index);

        // check to see if the kids have been built.
        if(!kid.kidsLoaded) {
            buildChildren(kid);
            kid.kidsLoaded = true;
        }

        return kid;
    }

    /**
     * Get the number of children the given parent contains. The number is
     * both child elements and attributes as this tree will show both.
     *
     * @param parent The parent to quiz for the number of children
     * @return The number of children of that parent
     */
    public int getChildCount(Object parent) {
        DOMTreeNode tree_node = (DOMTreeNode)parent;
        return tree_node.getChildCount();
    }

    /**
     * Get the index of the given child in the parent node.
     *
     * @param parent The parent node to check for
     * @param child The child to find the index of
     * @return The position of the child in the parent
     */
    public int getIndexOfChild(Object parent, Object child) {
        DOMTreeNode tree_node = (DOMTreeNode)parent;
        return tree_node.getIndex((TreeNode)child);
    }

    /**
     * Get the object that represents the root of this tree model.
     *
     * @return The root document object
     */
    public Object getRoot() {
        return domRoot;
    }

    /**
     * Check to see if the given node is a leaf node. Leaf nodes are
     * determined if the DOM object does not support children.
     *
     * @param child The child node to check
     * @return True if the DOM node is a leaf
     */
    public boolean isLeaf(Object child) {
        DOMTreeNode tree_node = (DOMTreeNode)child;
        Node kid = tree_node.getNode();
        short type = kid.getNodeType();

        boolean ret_val = false;

        // Nothing is a leaf unless we explicitly say it is
        switch(type) {
            case Node.ATTRIBUTE_NODE:
            case Node.COMMENT_NODE:
            case Node.DOCUMENT_TYPE_NODE:
            case Node.NOTATION_NODE:
            case Node.TEXT_NODE:
            case Node.CDATA_SECTION_NODE:
            case Node.PROCESSING_INSTRUCTION_NODE:
                ret_val = true;
        }

        return ret_val;
    }

    /**
     * Notification that the UI has changed the value to the destination
     * object to the new value. In this case, the value is always a string and
     * only useful if the destination object is an Attribute. Adding and
     * removing of whole subsections of the tree is not supported here.
     *
     * @param path The path to the object that changed
     * @param value The new value for the Node
     */
    public void valueForPathChanged(TreePath path, Object value) {
        DOMTreeNode tree_node = (DOMTreeNode)path.getLastPathComponent();
        Node node = tree_node.getNode();

        switch(node.getNodeType()) {
            case Node.ATTRIBUTE_NODE:
            case Node.TEXT_NODE:
            case Node.COMMENT_NODE:
            case Node.CDATA_SECTION_NODE:
                node.setNodeValue((String)value);
                break;

            case Node.DOCUMENT_FRAGMENT_NODE:
            case Node.DOCUMENT_NODE:
            case Node.DOCUMENT_TYPE_NODE:
            case Node.ENTITY_NODE:
            case Node.ENTITY_REFERENCE_NODE:
            case Node.NOTATION_NODE:
            case Node.PROCESSING_INSTRUCTION_NODE:
            case Node.ELEMENT_NODE:
            default:
                // do nothing
        }
    }

    /**
     * Add a tree listener to this model. Only one instance of the listener
     * can be added at a time. A second call to add the same instance will
     * silently ignore the request.
     *
     * @param l The listener to be added
     */
    public void addTreeModelListener(TreeModelListener l) {
        if((l != null) && !listeners.contains(l))
            listeners.add(l);
    }

    /**
     * Remove the tree listener from this model. If the instance is not
     * known about the request is silently ignored.
     *
     * @param l The listener to be removed
     */
    public void removeTreeModelListener(TreeModelListener l) {
        if((l != null) && listeners.contains(l))
            listeners.remove(l);
    }

    //----------------------------------------------------------
    // Methods required by EventListener
    //----------------------------------------------------------

    /**
     * Handle an incoming event. This will be for node insert/remove or
     * attribute modified. Will cause the appropriate tree model event to be
     * fired.
     *
     * @param evt The event to be processed
     */
    public void handleEvent(Event evt) {

        MutationEvent me = (MutationEvent)evt;

        String type = evt.getType();
        Node tgt = (Node)evt.getTarget();
        Node parent_node = null;

        if(tgt.getNodeType() == Node.ATTRIBUTE_NODE)
            parent_node = ((Attr)tgt).getOwnerElement();
        else if(tgt.getNodeType() == Node.ELEMENT_NODE &&
                ((me.getAttrChange() == MutationEvent.REMOVAL) ||
                 (me.getAttrChange() == MutationEvent.ADDITION)))
            parent_node = tgt;
        else
            parent_node = tgt.getParentNode();

        // Always build the tree path relative to the parent node as this is
        // what the JTree expects for events.
        TreePath path = buildTreePath(parent_node);
        DOMTreeNode parent = (DOMTreeNode)path.getLastPathComponent();
        DOMTreeNode child;
        TreeModelEvent tree_evt;
        Object[] children = new Object[1];
        int[] indicies = new int[1];
        int index;

        // If we're adding a node or attribute, we need to make sure they
        // get added or removed first because the tree path will point to
        // them.
        if(type.equals("DOMAttrModified")) {
            int mod_type = me.getAttrChange();

            switch(mod_type) {
                case MutationEvent.ADDITION:
                    child = new DOMTreeNode(me.getRelatedNode(), parent);
                    parent.insert(child, parent.getChildCount());

                    index = getIndexOfChild(parent, child);
                    indicies[0] = index ;
                    children[0] = child;

                    tree_evt = new TreeModelEvent(this, path, indicies, children);
                    fireTreeNodesInserted(tree_evt);
                    break;

                case MutationEvent.REMOVAL:
                    child = parent.getTreeNodeChild(tgt);
                    index = getIndexOfChild(parent, child);
                    indicies[0] = index ;
                    children[0] = child;

                    tree_evt = new TreeModelEvent(this, path, indicies, children);

                    parent.remove(child);
                    fireTreeNodesRemoved(tree_evt);
                    break;

                case MutationEvent.MODIFICATION:
                    child = parent.getTreeNodeChild(tgt);
                    index = getIndexOfChild(parent, child);
                    indicies[0] = index ;
                    children[0] = child;

                    tree_evt = new TreeModelEvent(this, path, indicies, children);
                    fireTreeNodesChanged(tree_evt);
            }
        } else if(type.equals("DOMNodeInserted")) {
            child = new DOMTreeNode(tgt, parent);
            parent.insert(child, parent.getChildCount());
            index = getIndexOfChild(parent, child);
            indicies[0] = index ;
            children[0] = child;

            tree_evt = new TreeModelEvent(this, path, indicies, children);

            fireTreeNodesInserted(tree_evt);
        } else if(type.equals("DOMNodeRemoved")) {
            child = parent.getTreeNodeChild(tgt);
            index = getIndexOfChild(parent, child);
            indicies[0] = index ;
            children[0] = child;

            tree_evt = new TreeModelEvent(this, path, indicies, children);
            parent.remove(child);

            fireTreeNodesRemoved(tree_evt);
        }
    }

    //----------------------------------------------------------
    // Miscellaneous local methods
    //----------------------------------------------------------

    /**
     * Build children objects for the requested node type. If the node is an
     * attribute then do nothing. Listeners will be added to the root item but
     * not the children. This is to prevent odd mixups if the children have
     * listeners but have not yet had their children built. The viewable tree
     * would get very mixed up then
     *
     * @param root The root object to add children for
     */
    private void buildChildren(DOMTreeNode root) {
        Node node = root.getNode();
        EventTarget tgt = null;

        if(hasDOMEvents)
            tgt = (EventTarget)node;

        if(node.getNodeType() != Node.ATTRIBUTE_NODE) {
            NamedNodeMap attrs = node.getAttributes();
            int size = (attrs != null) ? attrs.getLength() : 0;
            int index = size;
            int i;
            DOMTreeNode kid;

            for(i = 0; i < size; i++) {
                kid = new DOMTreeNode(attrs.item(i), root);
                root.insert(kid, i);
            }

            NodeList children = node.getChildNodes();
            size = children.getLength();

            for(i = 0; i < size; i++) {
                kid = new DOMTreeNode(children.item(i), root);
                root.insert(kid, index++);
            }
        }
    }

    /**
     * Build a TreePath object that represents the path to the given DOM node.
     * The path uses the Node.getParentNode() method to build a reverse order
     * tree. With this, we then find the TreeNode corresponding to each DOM
     * Node and create a TreePath from that.
     *
     * @param dest The end node
     * @return A array of objects representing the given node
     */
    private TreePath buildTreePath(Node dest) {
        ArrayList rev_list = new ArrayList();

        Node node = dest;

        // Build a node list working from this node back to the root
        while(node != null) {
            rev_list.add(node);
            node = node.getParentNode();
        }

        int i;
        int size = rev_list.size();
        Node[] node_path = new Node[size];

        for(i = 0; i < size; i++) {
            node_path[i] = (Node)rev_list.get(size - i - 1);
        }

        // Now, from the root, build the replacement tree map.
        DOMTreeNode[] path = new DOMTreeNode[size];

        path[0] = domRoot;

        for(i = 1; i < size; i++) {
            path[i] = path[i - 1].getTreeNodeChild(node_path[i]);
        }

        return new TreePath(path);
    }

    /**
     * Send an event to the listeners instructing that the collection of nodes
     * has changed.
     *
     * @param evt The event to be sent
     */
    private void fireTreeNodesChanged(TreeModelEvent evt) {
        Iterator itr = listeners.iterator();
        TreeModelListener l;

        while(itr.hasNext()) {
            l = (TreeModelListener)itr.next();
            try {
                l.treeNodesChanged(evt);
            } catch(Exception e) {
                System.err.println("Error during tree event processing");
                e.printStackTrace();
            }
        }
    }

    /**
     * Send an event to the listeners instructing that some nodes have been
     * added.
     *
     * @param evt The event to be sent
     */
    private void fireTreeNodesInserted(TreeModelEvent evt) {
        Iterator itr = listeners.iterator();
        TreeModelListener l;

        while(itr.hasNext()) {
            l = (TreeModelListener)itr.next();
            try {
                l.treeNodesInserted(evt);
            } catch(Exception e) {
                System.err.println("Error during tree event processing");
                e.printStackTrace();
            }
        }
    }

    /**
     * Send an event to the listeners instructing that some nodes have been
     * removed.
     *
     * @param evt The event to be sent
     */
    private void fireTreeNodesRemoved(TreeModelEvent evt) {
        Iterator itr = listeners.iterator();
        TreeModelListener l;

        while(itr.hasNext()) {
            l = (TreeModelListener)itr.next();
            try {
                l.treeNodesRemoved(evt);
            } catch(Exception e) {
                System.err.println("Error during tree event processing");
                e.printStackTrace();
            }
        }
    }

    /**
     * Send an event to the listeners instructing that the largescale structure
     * of nodes has changed.
     *
     * @param evt The event to be sent
     */
    private void fireTreeStructureChanged(TreeModelEvent evt) {
        Iterator itr = listeners.iterator();
        TreeModelListener l;

        while(itr.hasNext()) {
            l = (TreeModelListener)itr.next();
            try {
                l.treeStructureChanged(evt);
            } catch(Exception e) {
                System.err.println("Error during tree event processing");
                e.printStackTrace();
            }
        }
    }
}
