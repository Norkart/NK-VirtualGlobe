/*****************************************************************************
 *                     Yumetech, Inc Copyright (c) 2004 - 2006
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
import javax.vecmath.Matrix4f;

// Local imports
import org.j3d.aviatrix3d.picking.*;

import org.j3d.aviatrix3d.rendering.BoundingVolume;
import org.j3d.aviatrix3d.rendering.Cullable;
import org.j3d.aviatrix3d.rendering.SingleCullable;

/**
 * A node that can have multiple parents, thus allowing a graph
 * structure to the scene graph.
 * <p>
 *
 * Normal nodes cannot have more than one parent, so this class provides
 * the ability to have more than one. In doing so, it overrides the normal
 * methods provided by Node to provide the shared functionality. It provides
 * a compliment to the SharedGroup for parts of the scene graph where you
 * want to share a common piece, but really don't need the grouping
 * functionality.
 * <p>
 *
 * Using this node in preference to SharedGroup has several performance
 * benefits. For example, when performing picking, the picking implementation
 * can just ignore this node altogether as it knows the bounds are identical
 * to it's child.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.20 $
 */
public class SharedNode extends Node
    implements PickableObject,
               SinglePickTarget,
               SingleCullable
{
    /** Message when attempting to pick the object at the wrong time */
    private static final String PICK_TIMING_MSG =
        "Picking not permitted right now. Picking is only permitted during " +
        "the update observer callbacks.";

    /**
     * You are attempting to pick a node you have marked as not-pickable, yet
     * called the pick method on this class anyway.
     */
    private static final String PICKABLE_FALSE_MSG =
        "This node has been marked as not pickable by the user";

    /** Message when the setParent does not receive a group */
    private static final String NOT_GROUP_MSG =
        "Attempting to set a shared node into a node that is not a Group " +
        "node or SharedNode instance";

    /** The initial size of the children list */
    private static final int LIST_START_SIZE = 5;

    /** The increment size of the list if it gets overflowed */
    private static final int LIST_INCREMENT = 5;

    /** Listing of all the parents of this node */
    private Node[] parentList;

    /** Index to the next place to add items in the nodeList */
    private int lastParentList;

    /**
     * Counter for how many times we've been marked as live so to know
     * when to notify the children of a change of state.
     */
    private int liveCount;

    /** The child node of this one */
    private Node sharedChild;

    /** Count for us and our children of how many dirty bounds we have */
    private int dirtyBoundsCount;

    /** Flag indicating if this object is pickable currently */
    private int pickFlags;

    /**
     * The default constructor
     */
    public SharedNode()
    {
        parentList = new Node[LIST_START_SIZE];
        lastParentList = 0;

        liveCount = 0;
        dirtyBoundsCount = 0;
        pickFlags = 0xFFFFFFFF;
    }

    //---------------------------------------------------------------
    // Methods defined by SingleCullable
    //---------------------------------------------------------------

    /**
     * Get the child renderable of this object.
     *
     * @return an array of nodes
     */
    public Cullable getCullableChild()
    {
        if(sharedChild instanceof Cullable)
            return (Cullable)sharedChild;
        else
            return null;
    }

    /**
     * Check to see if this cullable is mulitparented. If it is, then that
     * will cause problems for code that needs to know information like the
     * transformation to the root of the scene graph.
     *
     * @return true if there are multiple parents
     */
    public boolean hasMultipleParents()
    {
        return true;
    }

    /**
     * Get the parent cullable of this instance. If this node has multiple
     * direct parents, then this should return null.
     *
     * @return The parent instance or null if none
     */
    public Cullable getCullableParent()
    {
        return null;
    }

    //---------------------------------------------------------------
    // Methods defined by Node
    //---------------------------------------------------------------

    /**
     * Mark this node as having dirty bounds due to one of it's children having
     * their bounds changed.
     */
    protected void markBoundsDirty()
    {
        dirtyBoundsCount++;

        if(implicitBounds)
        {
            for(int i = 0; i < lastParentList; i++)
                parentList[i].markBoundsDirty();
        }
    }

    /**
     * Internal method to recalculate the implicit bounds of this Node. By
     * default the bounds are a point sphere, so derived classes should
     * override this method with something better.
     */
    protected void recomputeBounds()
    {
        if(!alive || !implicitBounds)
            return;

        if(sharedChild == null)
            bounds = INVALID_BOUNDS;
        else
        {
            // Just use the child's bounds!
            bounds = sharedChild.getBounds();
        }
    }

    /**
     * Request a recomputation of the bounds of this object. If this object is
     * not currently live, you can request a recompute of the bounds to get the
     * most current values. If this node is currently live, then the request is
     * ignored.
     * <p>
     * This will recurse down the children asking all of them to recompute the
     * bounds. If a child is found to be during this process, that branch will
     * not update, and thus the value used will be the last updated (ie from the
     * previous frame it was processed).
     */
    public void requestBoundsUpdate()
    {
        if(alive || !implicitBounds)
            return;

        sharedChild.requestBoundsUpdate();

        // Clear this as we are no longer dirty.
        recomputeBounds();
    }

    /**
     * Update this node's bounds and then call the parent to update it's
     * bounds. Used to propogate bounds changes from the leaves of the tree
     * to the root. A node implementation may decide when and where to tell
     * the parent(s)s that updates are ready.
     */
    protected void updateBounds()
    {
        if(--dirtyBoundsCount != 0 || !implicitBounds)
            return;

        recomputeBounds();

        for(int i = 0; i < lastParentList; i++)
            parentList[i].updateBounds();
    }

    //---------------------------------------------------------------
    // Methods defined by SceneGraphObject
    //---------------------------------------------------------------

    /**
     * Specify this nodes parent, overridden to provide behaviour that appends
     * the node to the list rather than replacing it. The parent must be a group
     * node in this case.
     *
     * @param p The new parent instance to add to the list
     * @throws AlreadyParentedException There is a valid parent already set
     * @throws InvalidNodeTypeException Not a group node
     * @throws CyclicSceneGraphStructureException Equal parent and child causing
     *   a cycle in the scene graph structure
     */
    protected void setParent(Node p)
        throws AlreadyParentedException,
               InvalidNodeTypeException,
               CyclicSceneGraphStructureException
    {
        if((p != null) && !(p instanceof TransformHierarchy))
            throw new InvalidNodeTypeException(NOT_GROUP_MSG);

        // Check to see that this parent isn't already in the list
        for(int i = 0; i < lastParentList; i++)
            if(parentList[i] == p)
                return;

        checkForCyclicParent(p);

        resizeList();
        parentList[lastParentList++] = p;
    }

    /**
     * Remove a parent from this shared group. Since setParent() cannot be
     * used to remove a parent from the graph, you'll need to use this method
     * to remove the parent.
     *
     * @param p The new parent instance to remove from the list
     */
    protected void removeParent(Node p)
    {
        // find the location, move everything down one
        for(int i = 0; i < lastParentList; i++)
        {
            if(parentList[i] == p)
            {
                int move_size = lastParentList - i;
                if(move_size != 0)
                    System.arraycopy(parentList,
                                     i,
                                     parentList,
                                     i + 1,
                                     move_size);
                break;
            }
        }
    }

    /**
     * Overridden to always return the current first parent in the list.
     *
     * @return parent[0] if there are any
     */
    public Node getParent()
    {
        return parentList[0];
    }

    /**
     * Notification that this object is live now.
     *
     * @param state true if this should be marked as live now
     */
    protected void setLive(boolean state)
    {
        if(state)
            liveCount++;
        else if(liveCount > 0)
            liveCount--;

        if((liveCount == 0) || !alive)
            sharedChild.setLive(state);

        alive = state;

        super.setLive(state);

        // if we're coming live now, update the bounds.
        if(state)
            recomputeBounds();
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

        if(sharedChild != null) {
            sharedChild.setUpdateHandler(handler);
            bounds = sharedChild.getBounds();
        }
    }

    /**
     * Check to see if this node is the same reference as the passed node.
     * This is the upwards check to ensure that there is no cyclic scene graph
     * structures at the point where someone adds a node to the scenegraph.
     * When the reference and this are the same, an exception is generated.
     * If not, then the code will find the parent of this class and invoke
     * this same method on the parent.
     *
     * @param child The reference to check against this class
     * @throws CyclicSceneGraphStructureException Equal parent and child
     */
    protected void checkForCyclicParent(SceneGraphObject child)
        throws CyclicSceneGraphStructureException
    {
        if(child == this)
            throw new CyclicSceneGraphStructureException();

        for(int i = 0; i < lastParentList; i++)
            parentList[i].checkForCyclicParent(child);
    }

    //---------------------------------------------------------------
    // Methods defined by PickableObject
    //---------------------------------------------------------------

    /**
     * Set the node as being pickable currently using the given bit mask.
     * A mask of 0 will completely disable picking.
     *
     * @param state A bit mask of available options to pick for
     */
    public void setPickMask(int state)
    {
        pickFlags = state;
    }

    /**
     * Get the current pickable state mask of this object. A value of zero
     * means it is completely unpickable.
     *
     * @return A bit mask of available options to pick for
     */
    public int getPickMask()
    {
        return pickFlags;
    }

    /**
     * Check for all intersections against this geometry and it's children to
     * see if there is an intersection with the given set of requests.
     *
     * @param reqs The list of picks to be made, starting at this object
     * @param numRequests The number of valid pick requests to process
     * @throws NotPickableException This object has been marked as non pickable,
     *   but you decided to try to call the method anyway
     * @throws InvalidPickTimingException An attempt was made to pick outside
     *   of the ApplicationUpdateObserver callback method
     */
    public void pickBatch(PickRequest[] reqs, int numRequests)
        throws NotPickableException, InvalidPickTimingException
    {
        if(updateHandler == null || !updateHandler.isPickingPermitted())
            throw new InvalidPickTimingException(PICK_TIMING_MSG);

        if(pickFlags == 0)
            throw new NotPickableException(PICKABLE_FALSE_MSG);

        PickingManager picker = updateHandler.getPickingManager();

        picker.pickBatch(this, reqs, numRequests);
    }

    /**
     * Check for all intersections against this geometry and it's children to
     * see if there is an intersection with the given set of requests.
     *
     * @param req The details of the pick to be made
     * @throws NotPickableException This object has been marked as non pickable,
     *   but you decided to try to call the method anyway
     * @throws InvalidPickTimingException An attempt was made to pick outside
     *   of the ApplicationUpdateObserver callback method
     */
    public void pickSingle(PickRequest req)
        throws NotPickableException, InvalidPickTimingException
    {
        if(updateHandler == null || !updateHandler.isPickingPermitted())
            throw new InvalidPickTimingException(PICK_TIMING_MSG);

        if(pickFlags == 0)
            throw new NotPickableException(PICKABLE_FALSE_MSG);

        PickingManager picker = updateHandler.getPickingManager();

        picker.pickSingle(this, req);
    }

    //---------------------------------------------------------------
    // Methods defined by SinglePickTarget
    //---------------------------------------------------------------

    /**
     * Return the child that is pickable of from this target. If there is none
     * then return null.
     *
     * @return The child pickable object or null
     */
    public PickTarget getPickableChild()
    {
        return (sharedChild instanceof PickTarget) ?
               (PickTarget)sharedChild : null;
    }

    //---------------------------------------------------------------
    // Methods defined by PickTarget
    //---------------------------------------------------------------

    /**
     * Return the type constant that represents the type of pick target this
     * is. Used to provided optimised picking implementations.
     *
     * @return One of the _PICK_TYPE constants
     */
    public final int getPickTargetType()
    {
        return SINGLE_PICK_TYPE;
    }

    /**
     * Check the given pick mask against the node's internal pick mask
     * representation. If there is a match in one or more bitfields then this
     * will return true, allowing picking to continue to process for this
     * target.
     *
     * @param mask The bit mask to check against
     * @return true if the mask has an overlapping set of bitfields
     */
    public boolean checkPickMask(int mask)
    {
        return ((pickFlags & mask) != 0);
    }

    /**
     * Get the bounds of this picking target so that testing can be performed
     * on the object.
     *
     * @return A representation of the volume representing the pickable objects
     */
    public BoundingVolume getPickableBounds()
    {
        return bounds;
    }

    //---------------------------------------------------------------
    // Local Methods
    //---------------------------------------------------------------

    /**
     * Request the number of parents this node currently contains
     *
     * @return a positive number
     */
    public int numParents()
    {
        return lastParentList;
    }

    /**
     * Get the listing of the number of parents that this node currently has.
     * The provided array must be at least big enough to copy all the values
     * into it.
     *
     * @param parents An array to copy the parent listing into
     */
    public void getParents(Node[] parents)
    {
        if(parents == null)
            throw new NullPointerException("Parent array is null");

        System.arraycopy(parentList, 0, parents, 0, lastParentList);
    }

    /**
     * Set the child to be the new value. If the existing child is set,
     * it is replaced by this current child. Setting a value of null will
     * remove the old one.
     *
     * @param child The new instance to set or null
     * @throws CyclicSceneGraphStructureException Equal parent and child causing
     *   a cycle in the scene graph structure
     */
    public void setChild(Node child)
        throws CyclicSceneGraphStructureException
    {
        if(sharedChild != null)
        {
            sharedChild.setLive(false);
            sharedChild.removeParent(this);
        }

        sharedChild = child;

        if(sharedChild != null)
        {
            sharedChild.setParent(this);
            sharedChild.setLive(alive);
            sharedChild.setUpdateHandler(updateHandler);
        }
        else
        {
            bounds = INVALID_BOUNDS;
        }
    }

    /**
     * Get the currently set child of this node. If there is none set, the
     * return null.
     *
     * @return The current child or null
     */
    public Node getChild()
    {
        return sharedChild;
    }

    /**
     * Resize the list if needed. Marked as final in order to encourage the
     * compiler to inline the code for faster execution
     */
    private final void resizeList()
    {
        if((lastParentList + 1) == parentList.length)
        {
            int old_size = parentList.length;
            int new_size = old_size + LIST_INCREMENT;

            Node[] tmp_nodes = new Node[new_size];

            System.arraycopy(parentList, 0, tmp_nodes, 0, old_size);

            parentList = tmp_nodes;
        }
    }
}
