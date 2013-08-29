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

package org.web3d.vrml.renderer.ogl.browser;

// External imports
import org.j3d.aviatrix3d.Viewport;

// Local imports
// None

/**
 * Internal data holder used by viewport resize manager for proportional size
 * viewports.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
class ProportionalViewportData {

    /** The X position of the viewport, as a percentage of screen space */
    float viewX;

    /** The Y position of the viewport, as a percentage of screen space */
    float viewY;

    /** The width of the viewport, as a percentage of screen space */
    float viewWidth;

    /** The height of the viewport, as a percentage of screen space */
    float viewHeight;

    /** The AV3D viewport instance that is being managed */
    Viewport viewport;

    //----------------------------------------------------------
    // Methods defined by Object
    //----------------------------------------------------------

    /**
     * Generate a string representation of this data object.
     *
     * @return A formatted string
     */
    public String toString() {
        StringBuffer buf = new StringBuffer("ProportionalViewportData: ");
        buf.append("Viewport : ");
        buf.append(viewport.hashCode());
        buf.append(" x: ");
        buf.append(viewX);
        buf.append(" y: ");
        buf.append(viewY);
        buf.append(" width: ");
        buf.append(viewWidth);
        buf.append(" height: ");
        buf.append(viewHeight);

        return buf.toString();
    }
}
