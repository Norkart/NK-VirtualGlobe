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

/**
 * The SceneGraphObject is a common superclass for all scene graph objects.
 *
 * @author Alan Hudson
 * @version $Revision: 1.1 $
 */

public class SceneGraphObject {
    /** The scene this node belongs to */
    protected SGManager sgm;

    /**
     * Set the scenegraph manager for this node.  It will notify
     * all its children of the value.
     *
     *@param sgm The parent SG
     */

    public void setSGManager(SGManager sgm) {
        this.sgm = sgm;
    }
}