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
 * Describes the appearance of an object.
 *
 * @author Alan Hudson
 * @version $Revision: 1.1 $
 */

public class Appearance extends NodeComponent {
    private Material mat;

    /**
     * The default constructor.
     */
    public Appearance() {}

    public void setMaterial(Material material) {
        mat = material;
    }

    /**
     * Issue ogl commands needed for this component
     */
    public void renderState(GLDrawable gld) {
        if(mat != null)
            mat.renderState(gld);
    }

    /**
     * Restore all openGL state
     */
    public void restoreState(GLDrawable gld) {
        if(mat != null)
            mat.restoreState(gld);
    }
}