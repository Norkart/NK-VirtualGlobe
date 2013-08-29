/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.xj3d.core.eventmodel;

// External imports
// None

// Local imports
import org.web3d.util.DefaultErrorReporter;
import org.web3d.util.ErrorReporter;
import org.web3d.util.HashSet;
import org.web3d.util.SimpleStack;
import org.web3d.vrml.nodes.VRMLBindableNodeType;
import org.web3d.vrml.nodes.VRMLBindableNodeListener;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLClock;
import org.web3d.vrml.nodes.VRMLTimeDependentNodeType;

/**
 * The manager of bindable/activatable nodes at runtime.
 * <p>
 *
 * This common manager is responsible for handling the details of a single
 * type of bindable or activatable node. The primary difference between the
 * two types is that bindable nodes also have a bindTime field that must be
 * set at the time they get bound.
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public class BindableNodeManager implements VRMLBindableNodeListener {

    /** The operating stack of all nodes */
    private SimpleStack nodeStack;

    /** The listener for the current top of stack */
    private BindableNodeListener listener;

    /** Clock for setting bindTime information */
    private VRMLClock clock;

    /** The collection of all nodes we are registered for */
    private HashSet nodeSet;

    /** Reporter instance for handing out errors */
    private ErrorReporter errorReporter;

    /** The first added node */
    private VRMLBindableNodeType firstNode;

    /**
     * Create and initialise a route manager instance
     */
    public BindableNodeManager() {
        nodeStack = new SimpleStack();
        nodeSet = new HashSet();

        errorReporter = DefaultErrorReporter.getDefaultReporter();
    }

    //----------------------------------------------------------
    // Methods defined by VRMLBindableNodeListener
    //----------------------------------------------------------

    /**
     * Notification that the environment has requested that this node be now
     * bound or removed as the active node in the stack.
     *
     * @param src The source node that is to be bound
     * @param yes true if the node is to becoming active
     */
    public void nodeIsBound(VRMLNodeType src, boolean yes) {

        if(yes) {
            // making the node active. Is it part of the current stack?
            if(nodeStack.contains(src))
                nodeStack.remove(src);

            VRMLNodeType dest = null;

            if(nodeStack.size() != 0)
                dest = (VRMLNodeType)nodeStack.peek();

            nodeStack.push(src);

            if(src instanceof VRMLBindableNodeType)  {
                VRMLBindableNodeType node = (VRMLBindableNodeType)src;
                node.setOnStack(true);

                // don't unset the isBound if we are re-binding the same node.
                if((dest != null) && (dest != src)) {
                    VRMLBindableNodeType old = (VRMLBindableNodeType)dest;
                    old.setBind(false, false, 0);
                }
            }

            if(listener != null) {
                try {
                    listener.newNodeBound((VRMLBindableNodeType)src);
                } catch(Exception e) {
                    errorReporter.errorReport("Error sending bind update ", e);
                }
            }
        } else {
            // So this node is no longer active, what we need to do is
            // find the next node on the top of the stack and set it to
            // have the right values again. set_bind=FALSE can be called on
            // a node that is not on the top of the stack, so first check to
            // see if we are removing a node on the top of the stack or one
            // that is embedded someone further down.

            VRMLNodeType top = (VRMLNodeType)nodeStack.peek();

            if(top != src) {
                // Embedded somewhere else.
                nodeStack.remove(src);
                if (src instanceof VRMLBindableNodeType) {
                    VRMLBindableNodeType node = (VRMLBindableNodeType)top;
                    node.setOnStack(false);
                }
            } else {
                // Top of the stack
                nodeStack.pop();
                top = (VRMLNodeType)nodeStack.peek();

                if(top instanceof VRMLBindableNodeType)  {
                    VRMLBindableNodeType node = (VRMLBindableNodeType)top;
                    node.setOnStack(false);
                }

                if(listener != null) {
                    try {
                        listener.newNodeBound((VRMLBindableNodeType)top);
                    } catch(Exception e) {
                        errorReporter.errorReport("Error sending bind update ", e);
                    }
                }
            }
        }
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Register an error reporter with the engine so that any errors generated
     * by the loading of script code can be reported in a nice, pretty fashion.
     * Setting a value of null will clear the currently set reporter. If one
     * is already set, the new value replaces the old.
     *
     * @param reporter The instance to use or null
     */
    public void setErrorReporter(ErrorReporter reporter) {
        errorReporter = reporter;

        // Reset the default only if we are not shutting down the system.
        if(reporter == null)
            errorReporter = DefaultErrorReporter.getDefaultReporter();
    }

    /**
     * Set the VRML clock instance that we are using so that we can set the
     * bind time information for nodes that require it. A value of null can
     * be used to clear the current clock instance.
     *
     * @param clk The new clock to set
     */
    public void setVRMLClock(VRMLClock clk) {
        clock = clk;
    }

    /**
     * Get the currently bound node. If there are no nodes being managed then
     * this will return null.
     *
     * @return The currently bound node
     */
    public VRMLBindableNodeType getBoundNode() {
        if (nodeStack.isEmpty())
            return null;

        return (VRMLBindableNodeType)nodeStack.peek();
    }

    /**
     * Fetch the first node that was added to the manager after it was
     * cleared. This is used to determine the default instance to use when
     * the scene is first loaded. The concept of the first node is a bit
     * grey and should really only be used during the scene setup phase.
     * For example, if the end user deletes the first node from the runtime
     * scene graph, does this call still make sense? We shouldn't really
     * need to keep an ordered list of the items as they are added.
     *
     * @return The first added node
     */
    public VRMLBindableNodeType getFirstNode() {
        return firstNode;
    }

    /**
     * Check to see if the bindable manager contains this node instance
     * already.
     *
     * @param node The node instance to check
     * @return true if the instance is already registered
     */
    public boolean contains(VRMLBindableNodeType node) {
        return nodeSet.contains(node);
    }

    /**
     * Add the default node to the bottom of the stack. This should be
     * called before any setBind() methods are called at the start of
     * of the world.
     */
    public void addDefaultBindable(VRMLBindableNodeType node) {
        nodeStack.push(node);
        node.setOnStack(true);
    }

    /**
     * Add a bindable node to the management system.
     *
     * @param node The instance to add to this manager
     * @param isDefault Is this a default bindable
     */
    public void addNode(VRMLBindableNodeType node, boolean isDefault) {
        node.addBindableNodeListener(this);
        nodeSet.add(node);

        if(node instanceof VRMLTimeDependentNodeType)
            ((VRMLTimeDependentNodeType)node).setVRMLClock(clock);

        if(firstNode == null)
            firstNode = node;

        if(listener != null) {
            try {
                listener.bindableAdded(node, isDefault);
            } catch(Exception e) {
                errorReporter.errorReport("Error sending bindableAdded update ", e);
            }
        }
    }

    /**
     * Remove a bindable node from the management system.
     *
     * @param node The instance to add to this manager
     */
    public void removeNode(VRMLBindableNodeType node) {
        node.removeBindableNodeListener(this);
        nodeSet.remove(node);

        if(node instanceof VRMLTimeDependentNodeType)
            ((VRMLTimeDependentNodeType)node).setVRMLClock(null);

        if(listener != null) {
            try {
                listener.bindableRemoved(node);
            } catch(Exception e) {
                errorReporter.errorReport("Error sending bindableRemoved update ", e);
            }
        }
    }

    /**
     * Clear all of the nodes that this manager is currently dealing
     * with. All listeners are removed and no watch is kept on the nodes.
     */
    public void clearAll() {
        Object[] node_list = nodeSet.toArray();

        for(int i = 0; i < node_list.length; i++) {
            VRMLBindableNodeType node = (VRMLBindableNodeType)node_list[i];
            node.removeBindableNodeListener(this);
        }

        nodeSet.clear();
        nodeStack.clear();

        firstNode = null;
    }

    /**
     * Set the handler for node bind change events. This will replace the
     * current listener. A value of null will de-register the current
     * listener instance.
     *
     * @param l The listener to be used or null
     */
    public void setNodeChangeListener(BindableNodeListener l) {
        listener = l;
    }

}
