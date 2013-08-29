/*****************************************************************************
 *                   Yumetech, Inc Copyright (c) 2004 - 2006
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
//import org.web3d.vecmath.Matrix4f;
import javax.vecmath.Matrix4f;

import javax.media.opengl.GL;

// Local imports
import org.j3d.aviatrix3d.rendering.Cullable;

/**
 * A grouping 2D node that can have multiple parents, thus allowing a graph
 * structure to the scene graph.
 *
 * Normal nodes cannot have more than one parent, so this class provides
 * the ability to have more than one. In doing so, it overrides the normal
 * methods provided by Node2D to provide the shared functionality.
 *
 * @author Justin Couch
 * @version $Revision: 2.2 $
 */
public class SharedGroup2D extends Group2D
{
    /** Message when the setParent does not receive a group */
    private static final String NOT_GROUP_MSG =
        "Attempting to set a shared group into a node that is not a group " +
        "node instance";

    /** Message when fetching the parents and the array provided is null */
    private static final String NULL_PARENT_ARRAY_MSG =
        "Cannot fetch parents as provided array is null";

    /** The initial size of the children list */
    private static final int LIST_START_SIZE = 5;

    /** The increment size of the list if it gets overflowed */
    private static final int LIST_INCREMENT = 5;

    /** Listing of all the parents of this node */
    private Group2D[] parentList;

    /** Index to the next place to add items in the nodeList */
    private int lastParentList;

    /**
     * Counter for how many times we've been marked as live so to know
     * when to notify the children of a change of state.
     */
    private int liveCount;

    /**
     * The default constructor
     */
    public SharedGroup2D()
    {
        parentList = new Group2D[LIST_START_SIZE];
        lastParentList = 0;

        liveCount = 0;
    }

    //----------------------------------------------------------
    // Methods defined by GroupCullable
    //----------------------------------------------------------

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
    // Methods defined by Node2D
    //---------------------------------------------------------------

    /**
     * Mark this node as having dirty bounds due to one of it's children having
     * their bounds changed.
     */
    protected void markBoundsDirty()
    {
        dirtyBoundsCount++;

        if(implicitBounds && dirtyBoundsCount == 1)
        {
            for(int i = 0; i < lastParentList; i++)
                parentList[i].markBoundsDirty();
        }
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

        // Need to set this up to set a point source bounds if there are no
        // children.
        if(lastChild == 0)
            bounds = INVALID_BOUNDS;
        else
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
     */
    protected void setParent(Node p)
        throws AlreadyParentedException, InvalidNodeTypeException
    {
        if((p != null) && !(p instanceof Group2D))
            throw new InvalidNodeTypeException(NOT_GROUP_MSG);

        // Check to see that this parent isn't already in the list
        for(int i = 0; i < lastParentList; i++)
            if(parentList[i] == p)
                return;

        resizeList();
        parentList[lastParentList++] = (Group2D)p;
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
            super.setLive(state);

        dirtyBoundsCount = 0;
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
    public void getParents(Node2D[] parents)
    {
        if(parents == null)
            throw new NullPointerException(NULL_PARENT_ARRAY_MSG);

        System.arraycopy(parentList, 0, parents, 0, lastParentList);
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

            Group2D[] tmp_nodes = new Group2D[new_size];

            System.arraycopy(parentList, 0, tmp_nodes, 0, old_size);

            parentList = tmp_nodes;
        }
    }
}
