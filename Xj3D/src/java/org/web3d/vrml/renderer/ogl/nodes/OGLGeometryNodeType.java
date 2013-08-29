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

package org.web3d.vrml.renderer.ogl.nodes;

// Standard imports

// Application specific imports
import org.web3d.vrml.nodes.VRMLGeometryNodeType;
import org.j3d.aviatrix3d.Geometry;

/**
 * An abstract representation of any form of geometry.
 * <p>
 *
 * As geometry can be DEF/USEd, the listener handling must be capable of
 * dealing with more than one listener instance. When the geometry is shared
 * it will end up with a bunch of different Shape3D's using the same instance
 * and all registering an update listener.
 *
 * @author Alan Hudson, Justin Couch
 * @version $Revision: 1.7 $
 */
public interface OGLGeometryNodeType
    extends VRMLGeometryNodeType, OGLVRMLNode {

    /**
     * Returns a OGL Geometry node that represents this piece of
     * geometry. If no geometry is available, then return null.
     *
     * @return The geometry needed to represent this object
     */
    public Geometry getGeometry();
}
