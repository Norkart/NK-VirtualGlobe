/*****************************************************************************
 *                        Web3d.org Copyright (c) 2003
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.j3d.nodes;

// Standard imports
import javax.media.j3d.LineAttributes;

// Application specific imports

/**
 * Java3D specific methods for LineProperties.
 *
 * @author Alan Hudson
 * @version $Revision: 1.1 $
 */
public interface J3DLinePropertiesNodeType
    extends J3DVRMLNode {

    /**
     * Returns a J3D LineAttributes node representation of the contents
     *
     * @return The line attributes.
     */
    public LineAttributes getLineAttributes();

    // TODO: Needs dynamic notification listeners
}