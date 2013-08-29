/*****************************************************************************
 *                        Web3d.org Copyright (c) 2004
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.j3d.nodes.geom2d;

// External imports
import java.util.Map;

import javax.media.j3d.Geometry;
import javax.media.j3d.PointArray;
import javax.media.j3d.SceneGraphObject;

// Local imports
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.renderer.j3d.nodes.J3DGeometryListener;
import org.web3d.vrml.renderer.j3d.nodes.J3DGeometryNodeType;
import org.web3d.vrml.renderer.common.nodes.geom2d.BasePolypoint2D;

/**
 * Java3D implementation of an Polypoint2D
 * <p>
 *
 * The point set directly maps to J3D's PointArray class.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
public class J3DPolypoint2D extends BasePolypoint2D
    implements J3DGeometryNodeType {

    /** The impl for this class */
    private PointArray implGeom;

    /**
     * Construct a new point set instance that contains no child nodes.
     */
    public J3DPolypoint2D() {
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException Incorrect Node Type
     */
    public J3DPolypoint2D(VRMLNodeType node) {
        super(node);
    }

    //-------------------------------------------------------------
    // Methods defined by J3DGeometryNodeType
    //-------------------------------------------------------------

    /*
     * Returns a J3D Geometry collection that represents this piece of
     * geometry. If there is only one piece of geometry this will return
     * an array of lenght 1.
     *
     * @return The geometry needed to represent this object
     */
    public Geometry[] getGeometry() {
        return new Geometry[] { implGeom };
    }

    /**
     * Get the number of texture coordinate sets contained by this node
     *
     * @return the number of texture coordinate sets
     */
    public int getNumSets() {
        return 0;
    }

    /**
     * Get the texture coordinate generation mode.  NULL is returned
     * if the texture coordinates are not generated.
     *
     * @param setNum The set which this tex gen mode refers
     * @return The mode or NULL
     */
    public String getTexCoordGenMode(int setNum) {
        return null;
    }

    /**
     * Add a listener for geometry changed events. If the listener is null or
     * already registered it will silently ignore the requests.
     *
     * @param l The listener to add
     */
    public void addGeometryListener(J3DGeometryListener l) {
        // ignored
    }

    /**
     * Remove a listener for geometry changed events. If the listener is null
     * or has not been registered it will silently ignore the requests.
     *
     * @param l The listener to remove
     */
    public void removeGeometryListener(J3DGeometryListener l) {
        // ignored
    }

    //----------------------------------------------------------
    // Methods defined by J3DVRMLNode
    //----------------------------------------------------------

    /**
     * Provide the set of mappings that override anything that the loader
     * might set.
     * <p>
     *
     * If the key is set, but the value is null or zero length, then all
     * capabilities on that node will be disabled. If the key is set the
     * values override all settings that the loader may wish to normally
     * make. This can be very dangerous if the loader is used for a file
     * format that includes its own internal animation engine, so be very
     * careful with this request.
     *
     * @param capBits The capability bits to be set
     * @param freqBits The frequency bits to be set
     */
    public void setCapabilityOverrideMap(Map capBits, Map freqBits) {
        // TODO:
    }

    /**
     * Set the mapping of capability bits that the user would like to
     * make sure is set. The end output is that the capabilities are the union
     * of what the loader wants and what the user wants.
     * <p>
     * If the map contains a key, but the value is  null or zero length, the
     * request is ignored.
     *
     * @param capBits The capability bits to be set
     * @param freqBits The frequency bits to be set
     */
    public void setCapabilityRequiredMap(Map capBits, Map freqBits) {
        // TODO:
    }

    /**
     * Get the Java3D scene graph object representation of this node. This will
     * need to be cast to the appropriate parent type when being used.
     *
     * @return The J3D representation.
     */
    public SceneGraphObject getSceneGraphObject() {
        return implGeom;
    }

    /**
     * Notification that the construction phase of this node has finished.
     * If the node would like to do any internal processing, such as setting
     * up geometry, then go for it now.
     */
    public void setupFinished() {
        if(!inSetup)
            return;

        super.setupFinished();

        int num_points = numPoint / 2;
        float[] coords = new float[num_points * 3];
        for(int i = 0; i < num_points; i++) {
            coords[i * 3] = vfPoint[i * 2];
            coords[i * 3 + 1] = vfPoint[i * 2 + 1];
            coords[i * 3 + 2] = 0;
        }

        implGeom = new PointArray(num_points, PointArray.COORDINATES);
        implGeom.setCoordinates(0, coords);
    }
}
