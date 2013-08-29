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

package org.web3d.vrml.renderer.ogl.nodes;

// External imports
import org.j3d.aviatrix3d.picking.PickableObject;

// Local imports
import org.web3d.vrml.nodes.VRMLNodeType;

/**
 * An abstract representation of a node that can be used as a target for
 * picking within the Aviatrix3D system.
 * <p>
 *
 * Since picking requires an object to pick and something to pick against and
 * this interface provides the PickableObject instance to pick against as a
 * target for picking.
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public interface OGLPickableTargetNodeType extends VRMLNodeType {

    /**
     * Set the flag convertor that will be used to map the object type strings
     * to the internal pick masks. A value of null will clear the current
     * instance.
     *
     * @param conv The convertor instance to use, or null
     */
    public void setTypeConvertor(OGLPickingFlagConvertor conv);

    /**
     * Fetch the object that this target will pick against.
     *
     * @return The valid branchgroup to use
     */
    public PickableObject getPickableObject();
}
