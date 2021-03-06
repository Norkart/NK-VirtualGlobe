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

package org.web3d.vrml.renderer.mobile.sg;

// Standard imports

// Application specific imports
import gl4java.drawable.GLDrawable;

/**
 * The NodeComponent class is the superclass for all non renderable nodes.
 * These nodes provide data for other nodes.
 *
 * @author Alan Hudson
 * @version $Revision: 1.1 $
 */

public abstract class NodeComponent extends SceneGraphObject {
    /**
     * Issue ogl commands needed for this component
     */
    public void renderState(GLDrawable gld) {}

    /**
     * Restore all openGL state
     */
    public void restoreState(GLDrawable gld) {}
}