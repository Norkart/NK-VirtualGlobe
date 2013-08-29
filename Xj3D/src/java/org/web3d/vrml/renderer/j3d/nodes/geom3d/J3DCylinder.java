/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001-2004
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.j3d.nodes.geom3d;

// External imports
import java.util.Map;

import javax.media.j3d.Geometry;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.SceneGraphObject;
import javax.media.j3d.TriangleStripArray;
//import javax.media.j3d.TriangleStripArray;

import org.j3d.geom.GeometryData;
import org.j3d.geom.CylinderGenerator;

// Local import
import org.web3d.vrml.lang.*;

import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.renderer.common.nodes.geom3d.BaseCylinder;
import org.web3d.vrml.renderer.j3d.nodes.J3DGeometryNodeType;
import org.web3d.vrml.renderer.j3d.nodes.J3DGeometryListener;
import org.web3d.vrml.renderer.j3d.nodes.J3DGlobalStatus;
import org.web3d.vrml.renderer.j3d.nodes.J3DUserData;

/**
 * Java3D implementation of a Cylinder.
 * <p>
 *
 * The current implementation does not allow the removal of sides.
 *
 * @author Russell Dodds
 * @version $Revision: 1.10 $
 */
public class J3DCylinder extends BaseCylinder implements J3DGeometryNodeType {

    /** The Java3D geometry implmentation */
    private TriangleStripArray impl;

    /** Temp var to hold the capability bits until setupFinished called */
    private int[] capReqdBits;
    private int[] freqReqdBits;

    /**
     * Construct a default cylinder.
     */
    public J3DCylinder() {
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not a Box node, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not a Group node
     */
    public J3DCylinder(VRMLNodeType node) {
        super(node);
    }

    //-------------------------------------------------------------
    // Methods defined by J3DGeometryNodeType
    //-------------------------------------------------------------

    /**
     * Returns a J3D Geometry node
     *
     * @return A Geometry node
     */
    public Geometry[] getGeometry() {
        Geometry[] geom = new Geometry[1];

        geom[0] = impl;
        return geom;
    }

    /**
     * Add a listener for geometry changed events. If the listener is null or
     * already registered it will silently ignore the requests.
     *
     * @param l The listener to add
     */
    public void addGeometryListener(J3DGeometryListener l) {
    }

    /**
     * Remove a listener for geometry changed events. If the listener is null
     * or has not been registered it will silently ignore the requests.
     *
     * @param l The listener to remove
     */
    public void removeGeometryListener(J3DGeometryListener l) {
    }

    //----------------------------------------------------------
    // Methods required by the J3DVRMLNode interface.
    //----------------------------------------------------------

    /**
     * Provide the set of mappings that override anything that the loader
     * might set. Default implementation does nothing.
     *
     * @param capBits The capability bits to be set
     * @param freqBits The frequency bits to be set
     */
    public void setCapabilityOverrideMap(Map capBits, Map freqBits) {
    }

    /**
     * Set the mapping of capability bits that the user would like to
     * make sure is set. The end output is that the capabilities are the union
     * of what the loader wants and what the user wants. Default implementation
     * does nothing.
     * <p>
     * If the map contains a key, but the value is  null or zero length, the
     * request is ignored.
     *
     * @param capBits The capability bits to be set
     * @param freqBits The frequency bits to be set
     */
    public void setCapabilityRequiredMap(Map capBits, Map freqBits) {

        if((capBits != null) &&  capBits.containsKey(TriangleStripArray.class))
            capReqdBits = (int[])capBits.get(TriangleStripArray.class);

        if(!J3DGlobalStatus.haveFreqBitsAPI || freqBits == null ||
           !freqBits.containsKey(TriangleStripArray.class))
            return;

        freqReqdBits = (int[])freqBits.get(TriangleStripArray.class);
    }

    /**
     * Get the Java3D scene graph object representation of this node. This will
     * need to be cast to the appropriate parent type when being used.
     *
     * @return The J3D representation.
     */
    public SceneGraphObject getSceneGraphObject() {
        return impl;
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

        if (vfTop == false && vfBottom == false && vfSide == false) {
            return;
        }

        CylinderGenerator generator =
            new CylinderGenerator(vfHeight, vfRadius, vfBottom, vfTop);

        GeometryData data = new GeometryData();
        data.geometryType = GeometryData.TRIANGLE_STRIPS;
        data.geometryComponents = GeometryData.NORMAL_DATA |
                                  GeometryData.TEXTURE_2D_DATA;

        generator.generate(data);

        int vertex_mask = TriangleStripArray.COORDINATES |
                          TriangleStripArray.NORMALS |
                          TriangleStripArray.TEXTURE_COORDINATE_2;

        int texMap[] = new int[] { 0,0,0,0,0,0,0,0 };

        impl = new TriangleStripArray(data.vertexCount,
                                 vertex_mask, 1, texMap,
                                data.stripCounts);

        impl.setCoordinates(0, data.coordinates);
        impl.setNormals(0, data.normals);
        impl.setTextureCoordinates(0, 0, data.textureCoordinates);

        J3DUserData u_data = new J3DUserData();
        u_data.geometryData = data;

        impl.setUserData(u_data);

        if(capReqdBits != null) {
            for(int i = 0; i < capReqdBits.length; i++)
                impl.setCapability(capReqdBits[i]);
        }

        if(J3DGlobalStatus.haveFreqBitsAPI && freqReqdBits != null) {
            for(int i = 0; i < freqReqdBits.length; i++)
                impl.setCapabilityIsFrequent(freqReqdBits[i]);
        }

        capReqdBits = null;
        freqReqdBits = null;
    }
}
