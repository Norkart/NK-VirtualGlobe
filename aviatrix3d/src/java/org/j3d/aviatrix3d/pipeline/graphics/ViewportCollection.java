/*****************************************************************************
 *                        Yumtech, Inc Copyright (c) 2001
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.j3d.aviatrix3d.pipeline.graphics;

// External imports
// None

// Local imports
// None

/**
 * Data holder that passes the information about what is to be rendered from a
 * single layer (with multiple viewports, potentially) from the output of the
 * {@link GraphicsCullStage} through to the {@link GraphicsSortStage}.
 *
 * @author Justin Couch
 * @version $Revision: 3.2 $
 */
public class ViewportCollection
{
    /** Message for the non-positive scene size */
    private static final String NEG_SIZE_MSG =
        "The argument given must be greater than zero.";

    /** Listing of valid viewports to render from this latyer */
    public ViewportLayerCollection[] viewports;

    /** The number of valid viewports to render in this layer */
    public int numViewports;

    /**
     * Create a default bucket for handling a layer that defaults to having a
     * single scene.
     */
    public ViewportCollection()
    {
        this(1);
    }

    /**
     * Create a bucket for handling a layer that can hold the given number of
     * scenes to be rendered. If the value is <= 0 an exception is generated
     *
     * @param layerCount The number of scenes to be handled
     * @throws IllegalArgumentException The size was non-positive
     */
    public ViewportCollection(int layerCount)
        throws IllegalArgumentException
    {
        if(layerCount < 0)
            throw new IllegalArgumentException(NEG_SIZE_MSG);

        viewports = new ViewportLayerCollection[layerCount];
        for(int i = 0; i < layerCount; i++)
            viewports[i] = new ViewportLayerCollection();
    }

    /**
     * Ensure that there are enough items in the scenes array for the given
     * number of scenes to be processed. This will resize the array and
     * initialise more bucket instances as needed.
     *
     * @param size The minimum size needed
     */
    public void ensureCapacity(int size)
    {
        if(viewports.length < size)
        {
            int old_size = viewports.length;

            ViewportLayerCollection[] tmp = new ViewportLayerCollection[size];
            System.arraycopy(viewports, 0, tmp, 0, viewports.length);
            viewports = tmp;

            for(int i = old_size; i < size; i++)
                viewports[i] = new ViewportLayerCollection();
        }
    }
}
