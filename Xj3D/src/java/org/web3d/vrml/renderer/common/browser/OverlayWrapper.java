/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.common.browser;

// Standard imports
import java.awt.Component;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.ArrayList;
import java.util.List;

// Application specific imports
import org.web3d.vrml.nodes.VRMLSurfaceNodeType;

/**
 * Simple utility class that interfaces between the drawing component and the
 * overlay node types to pass resize information.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class OverlayWrapper implements ComponentListener {

    /** The component we are listening to */
    private Component component;

    /** The list of root surfaces being managed */
    private ArrayList surfaces;

    /**
     * Construct a new wrapper that works with the given component. Assumes
     * the reference is non-null.
     *
     * @param comp The component to use
     */
    public OverlayWrapper(Component comp) {
        component = comp;
        component.addComponentListener(this);

        surfaces = new ArrayList();
    }

    //------------------------------------------------------------------------
    // Methods from the ComponentListener interface
    //------------------------------------------------------------------------

    /**
     * Notification that the component has been resized.
     *
     * @param e The event that caused this method to be called
     */
    public void componentResized(ComponentEvent e)
    {
        updateSize();
    }

    /**
     * Notification that the component has been moved.
     *
     * @param e The event that caused this method to be called
     */
    public void componentMoved(ComponentEvent e)
    {
    }

    /**
     * Notification that the component has been shown. This is the component
     * being shown, not the window that it is contained in.
     *
     * @param e The event that caused this method to be called
     */
    public void componentShown(ComponentEvent e)
    {
    }

    /**
     * Notification that the component has been hidden.
     *
     * @param e The event that caused this method to be called
     */
    public void componentHidden(ComponentEvent e)
    {
    }

    //------------------------------------------------------------------------
    // Public utility methods
    //------------------------------------------------------------------------

    /**
     * Reset the list of overlays to use. Clears the current list and replaces
     * it with the new list and tells them their dimensions.
     *
     * @param list The list of surfaces to add
     */
    public void setSurfaces(List list) {
        surfaces.clear();
        surfaces.addAll(list);
        updateSize();
    }


    /**
     * Convenience method to update the size information for all the surfaces.
     */
    private void updateSize() {
        int width = component.getWidth();
        int height = component.getHeight();

        int num_surfaces = surfaces.size();

        for(int i = 0; i < num_surfaces; i++)
        {
            VRMLSurfaceNodeType surf = (VRMLSurfaceNodeType)surfaces.get(i);
            surf.surfaceSizeChanged(width, height);
        }
    }
}
