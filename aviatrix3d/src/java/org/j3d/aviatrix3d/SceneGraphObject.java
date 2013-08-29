/*****************************************************************************
 *                     Yumetech, Inc Copyright (c) 2004-2005
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.j3d.aviatrix3d;

// External imports
// None

// Local imports
// None

/**
 * The SceneGraphObject is a common superclass for all scene graph objects.
 *
 * @author Alan Hudson
 * @version $Revision: 1.14 $
 */
public abstract class SceneGraphObject
{
    /**
     * Message when the user attempts to write a value to the node at the
     * wrong point in the update cycle.
     */
    protected static final String WRITE_TIMING_MSG =
        "Writing is not permitted right now. Changing scene graph object " +
        "values is only permitted during the update observer callbacks.";

    protected static final String LISTENER_SET_NOT_LIVE_MESSAGE =
        "Attempting to set a listener to this node when it is not live. " +
        "Listeners can only be set once the node is live. When it is not " +
        "live just directly set the values.";

    /** The scene this node belongs to */
    protected NodeUpdateHandler updateHandler;

    /** User-provided data */
    private Object userData;

    /** Current live state of the object */
    protected boolean alive;

    /**
     * Set the user data to the new object. Null will clear the existing
     * object.
     *
     * @param data The new piece of data to set
     */
    public void setUserData(Object data)
    {
        userData = data;
    }

    /**
     * Get the currently set user data object. If none set, null is returned.
     *
     * @return The current user data or null
     */
    public Object getUserData()
    {
        return userData;
    }

    /**
     * Check to see whether this object is alive or not.
     *
     * @return true if this object is currently live
     */
    public boolean isLive()
    {
        return alive;
    }

    /**
     * Notification that this object is live now.
     *
     * @param state true if this should be marked as live now
     */
    protected void setLive(boolean state)
    {
        alive = state;
    }

    /**
     * Check to see if this node is the same reference as the passed node.
     * This is the upwards check to ensure that there is no cyclic scene graph
     * structures at the point where someone adds a node to the scenegraph.
     * When the reference and this are the same, an exception is generated.
     *
     * @param child The reference to check against this class
     * @throws CyclicSceneGraphStructureException Equal parent and child
     */
    protected void checkForCyclicParent(SceneGraphObject child)
        throws CyclicSceneGraphStructureException
    {
        if(child == this)
            throw new CyclicSceneGraphStructureException();
    }

    /**
     * Check to see if this node is the same reference as the passed node that
     * is a parent of this node. This is the downwards check to ensure that
     * there is no cyclic scene graph structures at the point where someone
     * adds a node to the scenegraph. When the reference and this are the
     * same, an exception is generated. Since each class may have different
     * lists of child node setups, this should be overriden by any class that
     * can take children, and have the call passed along to the children.
     *
     * @param parent The reference to check against this class
     * @throws CyclicSceneGraphStructureException Equal parent and child
     */
    protected void checkForCyclicChild(SceneGraphObject parent)
        throws CyclicSceneGraphStructureException
    {
        if(parent == this)
            throw new CyclicSceneGraphStructureException();
    }

    /**
     * Set the scenegraph update handler for this node.  It will notify
     * all its children of the value. A null value will clear the current
     * handler.
     *
     * @param handler The instance to use as a handler
     */
    protected void setUpdateHandler(NodeUpdateHandler handler)
    {
        updateHandler = handler;
    }

    /**
     * Notify the node that you have updates to the node that will not
     * alter its bounds.
     *
     * @param l The change requestor
     * @throws InvalidListenerSetTimingException If called when the node is not live or
     *   if called during one of the bounds/data changed callbacks
     */
    public void dataChanged(NodeUpdateListener l)
        throws InvalidListenerSetTimingException
    {
        if(!isLive())
            throw new InvalidListenerSetTimingException(LISTENER_SET_NOT_LIVE_MESSAGE);

        // Ignore if we are not live
        if(updateHandler == null)
            return;

        updateHandler.dataChanged(l, this);
    }
}
