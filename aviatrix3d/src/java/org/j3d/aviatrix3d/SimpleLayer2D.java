/*****************************************************************************
 *                  Yumetech, Inc Copyright (c) 2004 - 2006
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
 * An layer definition that only allows a single viewport that covers the
 * entire area as it's child.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 2.2 $
 */
public class SimpleLayer2D extends Layer
{
    /** Message about code that is not valid parent */
    private static final String HAS_PARENT_MSG = "This Layer already has a " +
        "parent. Scenes cannot be shared amongst layers multiple times.";

    /** The viewport that this layer manages */
    private Viewport2D viewport;

    /** Flag to describe whether this is the currently active sound layer */
    private boolean activeSoundLayer;

    /** Update handler for the external code. Not created until needed. */
    private InternalUpdater internalUpdater;

    /**
     * Internal implementation of the InternalNodeUpdateListener. Done as an
     * inner class to hide the calls from public consumption.
     */
    private class InternalUpdater
        implements InternalLayerUpdateListener
    {

        /**
         * Notify this layer that it is no longer the active audio layer for
         * rendering purposes.
         */
        public void disableActiveAudioState()
        {
            activeSoundLayer = false;
        }
    }

    /**
     * Construct a new layer instance
     */
    public SimpleLayer2D()
    {
        super(SIMPLE_2D);

        activeSoundLayer = false;
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
        return (viewport instanceof ViewportCullable) ?
               (ViewportCullable)viewport: null;
    }

    /**
     * Returns the number of valid cullable children to process. If there are
     * no valid cullable children, return 0.
     *
     * @return A number greater than or equal to zero
     */
    public int numCullableChildren()
    {
        return viewport != null ? 1 : 0;
    }

    //----------------------------------------------------------
    // Methods defined by ScenegraphObject
    //----------------------------------------------------------

    /**
     * Set the scenegraph update handler for this node.  It will notify
     * all its children of the value. A null value will clear the current
     * handler.
     *
     * @param handler The instance to use as a handler
     */
    protected void setUpdateHandler(NodeUpdateHandler handler)
    {
        super.setUpdateHandler(handler);

        if(viewport != null)
            viewport.setUpdateHandler(handler);
    }

    /**
     * Notification that this object is live now. Overridden to make sure that
     * the live state of the nodes represents the same state as the parent
     * scene graph.
     *
     * @param state true if this should be marked as live now
     */
    protected void setLive(boolean state)
    {
        super.setLive(state);

        if(viewport != null)
            viewport.setLive(state);
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Set this layer to be the currently active sound layer. The previously
     * active layer will be disabled. This method can only be called during
     * the dataChanged() callback.
     */
    public void makeActiveSoundLayer()
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(WRITE_TIMING_MSG);

        activeSoundLayer = true;

        if(updateHandler != null)
        {
            if(internalUpdater == null)
                internalUpdater = new InternalUpdater();

            updateHandler.activeSoundLayerChanged(internalUpdater);
        }
    }

    /**
     * Check to see if this is the currently active layer for sound rendering.
     * This will only return true the frame after calling
     * {@link #makeActiveSoundLayer()}. The effects, however, will be rendered
     * starting the frame that this is set.
     *
     * @return true if this is the layer that will generate sound rendering
     */
    public boolean isActiveSoundLayer()
    {
        return activeSoundLayer;
    }

    /**
     * Set a new viewport instance to be used by this layer.
     * <p>
     * Note that a viewport cannot have more than one parent, so sharing it
     * between layers will result in an error.
     *
     * @param vp The viewport instance to use, or null to clear
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener callback method
     * @throws AlreadyParentedException This scene already has a current parent
     *    preventing it from being used
     */
    public void setViewport(Viewport2D vp)
        throws InvalidWriteTimingException, AlreadyParentedException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(WRITE_TIMING_MSG);

        viewport = vp;

        // No scene? Ignore it.
        if(vp == null)
            return;

        if(vp.hasParent())
            throw new AlreadyParentedException(HAS_PARENT_MSG);


        vp.setUpdateHandler(updateHandler);
        vp.setLive(alive);
    }

    /**
     * Get the currently set viewport instance. If no viewport is set, null
     * is returned.
     *
     * @return The current viewport instance or null
     */
    public Viewport2D getViewport()
    {
        return viewport;
    }
}
