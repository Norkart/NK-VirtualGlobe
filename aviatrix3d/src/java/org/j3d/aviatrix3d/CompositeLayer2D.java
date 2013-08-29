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
import java.util.ArrayList;

// Local imports
import org.j3d.aviatrix3d.rendering.ViewportCullable;

/**
 * A layer that allows the definition of multiple viewports to be rendered, but
 * is restricted to 2D rendering only.
 * <p>
 *
 * This layer allows for the provision of multiple viewports within it. Each
 * viewport is independently managed. Not provision is made for checking if
 * viewports overlap or do other visually unappealing things. A typical use of
 * this layer would be to provide multiple views onto a single shared scene
 * graph, reminiscent of 3D editor applications like AutoCAD, Maya et al.
 *
 * @author Justin Couch
 * @version $Revision: 2.4 $
 */
public class CompositeLayer2D extends Layer
{
    /** Message about code that is not valid parent */
    private static final String HAS_PARENT_MSG = "This viewport already has a " +
        "parent. Viewports cannot be shared amongst layers multiple times.";

    /** The list of viewports being managed. Only contains Viewport2D */
    private ArrayList<Viewport> viewports;

    /**
     * Construct a new layer instance.
     */
    public CompositeLayer2D()
    {
        super(COMPOSITE_2D);

        viewports = new ArrayList<Viewport>();
    }

    //---------------------------------------------------------------
    // Methods defined by LayerCullable
    //---------------------------------------------------------------

    /**
     * Get the cullable layer child that for the given layer index.
     *
     * @return The layer cullable at the given index or null
     */
    public ViewportCullable getCullableViewport(int viewportIndex)
    {
        if((viewportIndex < 0) || viewportIndex >= viewports.size())
            return null;

        Object vp = viewports.get(viewportIndex);

        return (vp instanceof ViewportCullable) ? (ViewportCullable)vp: null;
    }

    /**
     * Returns the number of valid cullable children to process. If there are
     * no valid cullable children, return 0.
     *
     * @return A number greater than or equal to zero
     */
    public int numCullableChildren()
    {
        return viewports.size();
    }

    //----------------------------------------------------------
    // Methods defined by SceneGraphObject
    //----------------------------------------------------------

    /**
     * Set the viewportgraph update handler for this node.  It will notify
     * all its children of the value. A null value will clear the current
     * handler.
     *
     * @param handler The instance to use as a handler
     */
    protected void setUpdateHandler(NodeUpdateHandler handler)
    {
        super.setUpdateHandler(handler);

        int num = viewports.size();

        for(int i = 0; i < num; i++)
            viewports.get(i).setUpdateHandler(handler);
    }

    /**
     * Notification that this object is live now. Overridden to make sure that
     * the live state of the nodes represents the same state as the parent
     * viewport graph.
     *
     * @param state true if this should be marked as live now
     */
    protected void setLive(boolean state)
    {
        super.setLive(state);

        int num = viewports.size();

        for(int i = 0; i < num; i++)
            viewports.get(i).setLive(state);
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Add a new viewport to be used by this layer. Adding null references
     * is silently ignored. The viewport is added to the end of the current
     * listing.
     * <p>
     * Note that a viewport cannot have more than one parent, so sharing it
     * between layers will result in an error.
     *
     * @param vp The viewport instance to use
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener callback method
     * @throws AlreadyParentedException This viewport already has a current parent
     *    preventing it from being used
     */
    public void addViewport(Viewport2D vp)
        throws InvalidWriteTimingException, AlreadyParentedException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(WRITE_TIMING_MSG);

        // No viewport? Ignore it.
        if(vp == null)
            return;

        if(vp.hasParent())
            throw new AlreadyParentedException(HAS_PARENT_MSG);

        vp.setUpdateHandler(updateHandler);
        vp.setLive(alive);

        viewports.add(vp);
    }

    /**
     * Remove the given viewport from this layer. If the viewport is not registered,
     * then the request is silently ignored.
     *
     * @param vp The viewport instance to be removed
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener callback method
     */
    public void removeViewport(Viewport2D vp)
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(WRITE_TIMING_MSG);

        // No viewport? Ignore it.
        if(vp == null)
            return;

        if(!viewports.remove(vp))
        {
            vp.setUpdateHandler(null);
            vp.setLive(false);
        }
    }

    /**
     * Remove the given viewport at the specified index from this layer. If the
     * index is out of bounds, null is returned and nothing happens. All
     * viewports at indices above this number are shifted down by 1.
     *
     * @param num The index of the viewport to remove
     * @return viewport The viewport that was at the given index
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener callback method
     */
    public Viewport2D removeViewport(int num)
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(WRITE_TIMING_MSG);

        // No viewport? Ignore it.
        if(num < 0 || num > viewports.size())
            return null;

        Viewport2D vp = (Viewport2D)viewports.remove(num);

        vp.setUpdateHandler(null);
        vp.setLive(false);

        return vp;
    }

    /**
     * Remove all the viewports from this layer.
     *
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener callback method
     */
    public void clearViewports()
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(WRITE_TIMING_MSG);

        int num = viewports.size();

        for(int i = num - 1; i >= 0; i--)
        {
            Viewport2D sc = (Viewport2D)viewports.remove(num);

            sc.setUpdateHandler(null);
            sc.setLive(false);
        }
    }

    /**
     * Get the currently set viewport instance at a specific index. If no viewport is
     * set at that index, null is returned.
     *
     * @param num The index of the viewport to fetch
     * @return The current viewport instance or null
     */
    public Viewport2D getViewport(int num)
    {
        return (Viewport2D)viewports.get(num);
    }

    /**
     * Return how many viewports this layer contains.
     *
     * @return A value >= 0
     */
    public int numViewports()
    {
        return viewports.size();
    }
}
