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
package org.web3d.vrml.nodes;

// External Imports
// None

// Local imports
import org.web3d.vrml.lang.InvalidFieldValueException;

/**
 * An interface for accessing the geometry data of an IndexedGeometry class.
 * <p>
 * These objects might be coord, color, normal, texCoord.  All will descend
 * from VRMLGeometricPropertyType or VRMLProtoInstance. There is no direct
 * provision through this interface to clear an individual component node.
 * That must be done directly through the lower level setValue() methods.
 *
 * @author Alan Hudson, Justin Couch
 * @version $Revision: 1.9 $
 */
public interface VRMLComponentGeometryNodeType extends VRMLGeometryNodeType {

    /**
     * Get the components that compose a geometry object.
     * <p>
     * This method will return either VRMLGeometricPropertyNodeType or
     * VRMLProtoInstance.  With a proto you can use getImplementationNode
     * to get a node conforming to the VRMLGeometricPropertyNodeType interface.
     * <p>
     * If there are no components then a zero length array will be returned.
     *
     * @return VRMLNodeType[] The components
     */
    public VRMLNodeType[] getComponents();

    /**
     * Set the components the compose a geometry object. To clear all the
     * components, pass a null parameter.
     *
     * @param comps An array of geometric properties
     * @throws InvalidFieldValueException The node is not a known or supported
     *   field for this node
     */
    public void setComponents(VRMLNodeType[] comps)
        throws InvalidFieldValueException;

    /**
     * Set a component that composes part of a geometry object.
     *
     * @param comp A geometric property
     * @throws InvalidFieldValueException The node is not a known or supported
     *   field for this node
     */
    public void setComponent(VRMLNodeType comp)
        throws InvalidFieldValueException;

    /**
     * Check to see if the colors are per vertex or per face.
     *
     * @return true The colors are per vertex
     */
    public boolean hasColorPerVertex();

    /**
     * Check to see if the normals are per vertex or per face.
     *
     * @return true The normals are per vertex
     */
    public boolean hasNormalPerVertex();

    /**
     * Check to see if this geometry implementation type requires unlit color
     * values to be set. For the most part this will always return false, but
     * some will need it (points and lines). This value should be constant for
     * the geometry regardless of whether a Color component has been provided
     * or not. It is up to the implementation to decide when to pass these
     * values on to the underlying rendering structures or not.
     *
     * @return true if we need unlit colour information
     */
    public boolean requiresUnlitColor();

    /**
     * Set the local colour override for this geometry. Typically used to set
     * the emissiveColor from the Material node into the geometry for the line
     * and point-type geometries which are unlit in the X3D/VRML model.
     *
     * @param color The colour value to use
     */
    public void setUnlitColor(float[] color);
}
