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

package org.web3d.vrml.renderer.j3d.nodes;

// Standard imports
import javax.media.j3d.Geometry;

// Application specific imports
import org.web3d.vrml.nodes.VRMLGeometryNodeType;

/**
 * An abstract representation of any form of geometry.
 * <p>
 *
 * Geometry may exist in many forms and due to the nature of Java 3D, it may
 * need to be re-created at times, particularly when the user changes the
 * vertex count on the fly through a route (because the Java 3D geometry
 * classes require a vertexCount in the constructor and don't allow you to
 * change that on the fly). In order to deal with this, we can register a
 * listener for when the geometry object updates its contents. This listener
 * is for when new pieces of geometry change, not just when the coordinates
 * are morphed, but the vertex count stays the same.
 * <p>
 * As geometry can be DEF/USEd, the listener handling must be capable of
 * dealing with more than one listener instance. When the geometry is shared
 * it will end up with a bunch of different Shape3D's using the same instance
 * and all registering an update listener.
 *
 * @author Alan Hudson, Justin Couch
 * @version $Revision: 1.7 $
 */
public interface J3DGeometryNodeType
    extends VRMLGeometryNodeType, J3DVRMLNode {

    /**
     * Returns a J3D Geometry collection that represents this piece of
     * geometry. If there is only one piece of geometry this will return
     * an array of lenght 1. If no geometry is available, then return null.
     *
     * @return The geometry needed to represent this object
     */
    public Geometry[] getGeometry();

    /**
     * Add a listener for geometry changed events. If the listener is null or
     * already registered it will silently ignore the requests.
     *
     * @param l The listener to add
     */
    public void addGeometryListener(J3DGeometryListener l);

    /**
     * Remove a listener for geometry changed events. If the listener is null
     * or has not been registered it will silently ignore the requests.
     *
     * @param l The listener to remove
     */
    public void removeGeometryListener(J3DGeometryListener l);
}
