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

package org.web3d.vrml.nodes;

// External imports
import org.odejava.Space;


// Local imports
import org.web3d.vrml.lang.InvalidFieldValueException;


/**
 * Representation of single space that contains more spaces and/or explicity
 * local geometry for comparison.
 * component.
 * <p>
 *
 * See the specification definition at
 * http://www.xj3d.org/extensions/rigid_physics.html
 * <p>
 *
 * A space may contain other, nested spaces. Dividing geometry up by space can
 * make for more efficient algorithms for determining inter-object collisions.
 * It may also contain geometry from the X3DNBodyCollidable nodes that can be
 * directly tested against this space, or children of this space.
 * <p>
 *
 * The X3D definition of this abstract type is:
 * <pre>
 * X3DNBodyCollidableNode : X3DChildNode, X3DBoundedObject {
 *   SFNode     [in,out] metadata       NULL      [X3DMetadataObject]
 *   SFRotation [in,out] rotation       0 0 1 0   [0,1]
 *   SFVec3f    [in,out] translation    0 0 0     (-&#8734;,&#8734;)
 *   SFVec3f    []       bboxCenter     0 0 0     (-&#8734;,&#8734;)
 *   SFVec3f    []       bboxSize       -1 -1 -1  [0,&#8734;) or -1 -1 -1
 * }
 * </pre>
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public interface VRMLNBodySpaceNodeType
    extends VRMLNodeType, VRMLBoundedNodeType {

    /**
     * Get the ODE object that represents the body to evaluate.
     *
     * @return The body object representing this node
     */
    public Space getODESpace();

    /**
     * Set the parent space that this space belongs to. A null value clears
     * the world and indicates the physics model or space is no longer in use
     * by it's parent space (eg deletes it).
     *
     * @param parent The new parent of this object, or null
     */
    public void setParentODESpace(Space parent);

    /**
     * Is this group enabled for use right now?
     *
     * @return true if this is enabled
     */
    public boolean isEnabled();
}
