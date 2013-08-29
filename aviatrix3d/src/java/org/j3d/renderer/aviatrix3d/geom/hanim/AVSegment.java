/*****************************************************************************
 *                     Yumetech, Inc Copyright (c) 2004-2005-
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.renderer.aviatrix3d.geom.hanim;

// External imports
import java.util.ArrayList;

import javax.vecmath.Matrix4f;

// Local imports
import org.j3d.aviatrix3d.*;

import org.j3d.geom.hanim.HAnimSegment;

/**
 * Common AV3D implementation of the Segment object.
 * <p>
 *
 * Implements a group group to hold the geometry and groupation
 * required by the Segment object.
 * <p>
 *
 * The child of a Segment node is required to be an instance of
 * {@link org.j3d.aviatrix3d.Node}.
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
class AVSegment extends HAnimSegment
    implements AVHumanoidPart, NodeUpdateListener
{
    /** Error message saying that the children must be an AV3D Node */
    private static final String NODE_CHILD_ERR =
        "Child must be an Aviatrix3D Node instance";

    /** The real group group this occupies */
    private Group group;

    /** Remove the following list of children */
    private ArrayList removedChildren;

    /** Add the following list of children */
    private ArrayList addedChildren;

    /** Flag to indicate the bounds changed */
    private boolean boundsChanged;

    /** If there are explicit bounds, this is a valid object */
    private BoundingBox bounds;

    /**
     * Create a new, default instance of the site.
     */
    AVSegment()
    {
        group = new Group();

        boundsChanged = false;
    }

    //----------------------------------------------------------
    // Methods defined by AVHumanoidPart
    //----------------------------------------------------------

    /**
     * Get the implemented scene graph object for this part.
     *
     * @return The scene graph object to use
     */
    public Node getSceneGraphObject()
    {
        return group;
    }

    //----------------------------------------------------------
    // Methods defined by NodeUpdateListener
    //----------------------------------------------------------

    /**
     * Notification that its safe to update the node now with any operations
     * that could potentially effect the node's bounds.
     *
     * @param src The node or Node Component that is to be updated.
     */
    public void updateNodeBoundsChanges(Object src)
    {
        // How many objects changed this last round?
        if(removedChildren != null)
        {
            for(int i = 0; i < removedChildren.size(); i++)
                group.removeChild((Node)removedChildren.get(i));

            removedChildren.clear();
            removedChildren = null;
        }

        if(addedChildren != null)
        {
            for(int i = 0; i < addedChildren.size(); i++)
                group.addChild((Node)addedChildren.get(i));

            addedChildren.clear();
            addedChildren = null;
        }

        if(boundsChanged)
        {
            boundsChanged = false;
            group.setBounds(bounds);
        }
    }

    /**
     * Notification that its safe to update the node now with any operations
     * that only change the node's properties, but do not change the bounds.
     *
     * @param src The node or Node Component that is to be updated.
     */
    public void updateNodeDataChanges(Object src)
    {
    }


    //----------------------------------------------------------
    // Methods defined by HAnimSegment
    //----------------------------------------------------------

    /**
     * Set a new value for the bboxCenter of this segment. If the array is null or
     * not long enough an exception is generated. The array must be at least
     * 3 units long, and the bboxCenter is taken from the 1st three values.
     *
     * @param val The new bboxCenter value to use
     * @throws IllegalArgumentException The array is null or not long enough.
     */
    public void setBboxCenter(float[] val)
    {
        super.setBboxCenter(val);

        if(bboxSize[0] == -1 && bboxSize[1] == -1 && bboxSize[2] == 0)
            return;

        if(bounds == null)
            bounds = new BoundingBox();

        float x = bboxCenter[0] - bboxSize[0];
        float y = bboxCenter[1] - bboxSize[1];
        float z = bboxCenter[2] - bboxSize[2];

        bounds.setMinimum(x, y, z);

        x = bboxCenter[0] + bboxSize[0];
        y = bboxCenter[1] + bboxSize[1];
        z = bboxCenter[2] + bboxSize[2];

        bounds.setMaximum(x, y, z);

        if(group.isLive())
        {
            boundsChanged = true;
            group.boundsChanged(this);
        }
        else
            group.setBounds(bounds);
    }

    /**
     * Set a new value for the bboxSize of this segment. If the array is null or
     * not long enough an exception is generated. The array must be at least
     * 3 units long, and the bboxSize is taken from the 1st three values.
     *
     * @param val The new bboxSize value to use
     * @throws IllegalArgumentException The array is null or not long enough.
     */
    public void setBboxSize(float[] val)
    {
        super.setBboxSize(val);

        if(val[0] == -1 && val[1] == -1 && val[2] == 0)
            bounds = null;
        else if(bounds == null)
        {
            bounds = new BoundingBox();

            float x = bboxCenter[0] - bboxSize[0];
            float y = bboxCenter[1] - bboxSize[1];
            float z = bboxCenter[2] - bboxSize[2];

            bounds.setMinimum(x, y, z);

            x = bboxCenter[0] + bboxSize[0];
            y = bboxCenter[1] + bboxSize[1];
            z = bboxCenter[2] + bboxSize[2];

            bounds.setMaximum(x, y, z);
        }

        if(group.isLive())
        {
            boundsChanged = true;
            group.boundsChanged(this);
        }
        else
            group.setBounds(bounds);
    }

    /**
     * Replace the existing children with the new set of children.
     *
     * @param kids The collection of child objects to now use
     * @param numValid The number kids to copy from the given array
     */
    public void setChildren(Object[] kids, int numValid)
    {
        if(group.isLive())
        {
            if(addedChildren == null)
                addedChildren = new ArrayList();

            for(int i = 0; i < numValid; i++)
            {
                if(!(kids[i] instanceof Node))
                    throw new IllegalArgumentException(NODE_CHILD_ERR);

                addedChildren.add(kids[i]);
            }

            if(removedChildren == null)
                removedChildren = new ArrayList();

            for(int i = 0; i < numChildren; i++)
                removedChildren.add(children[i]);

            group.boundsChanged(this);
        }
        else
        {
            group.removeAllChildren();

            for(int i = 0; i < numValid; i++)
            {
                if(!(kids[i] instanceof Node))
                    throw new IllegalArgumentException(NODE_CHILD_ERR);

                group.addChild((Node)kids[i]);
            }
        }

        super.setChildren(kids, numValid);
    }

    /**
     * Add a child node to the existing collection. Duplicates and null values
     * are allowed.
     *
     * @param kid The new child instance to add
     */
    public void addChild(Object kid)
    {
        if(!(kid instanceof Node))
            throw new IllegalArgumentException(NODE_CHILD_ERR);

        super.addChild(kid);

        if(group.isLive())
        {
            if(addedChildren == null)
                addedChildren = new ArrayList();

            addedChildren.add(kid);

            group.boundsChanged(this);
        }
        else
        {
            group.addChild((Node)kid);
        }
    }

    /**
     * Remove a child node from the existing collection. If there are
     * duplicates, only the first instance is removed. Only reference
     * comparisons are used.
     *
     * @param kid The child instance to remove
     */
    public void removeChild(Object kid)
    {
        // run through the children list to see if we have it
        for(int i = 0; i < numChildren; i++)
        {
            if(children[i] == kid)
            {
                if(group.isLive())
                {
                    if(removedChildren == null)
                        removedChildren = new ArrayList();

                    removedChildren.add(kid);

                    group.boundsChanged(this);
                }
                else
                {
                    group.removeChild((Node)kid);
                }

                break;
            }
        }

        super.removeChild(kid);
    }
}
