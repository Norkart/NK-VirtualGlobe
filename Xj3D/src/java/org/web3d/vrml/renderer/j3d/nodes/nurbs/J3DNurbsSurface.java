/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001-2004
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 ****************************************************************************/

package org.web3d.vrml.renderer.j3d.nodes.nurbs;

// External imports
import java.util.ArrayList;
import java.util.Map;

import javax.media.j3d.Geometry;
import javax.media.j3d.SceneGraphObject;
import javax.media.j3d.TriangleStripArray;

// Local imports
import org.web3d.vrml.nodes.VRMLNodeType;

import org.web3d.vrml.renderer.common.nodes.nurbs.BaseNurbsSurface;
import org.web3d.vrml.renderer.j3d.nodes.J3DGeometryListener;
import org.web3d.vrml.renderer.j3d.nodes.J3DGeometryNodeType;
import org.web3d.vrml.renderer.j3d.nodes.J3DGlobalStatus;
import org.web3d.vrml.renderer.j3d.nodes.J3DUserData;


/**
 * Java3D-renderer implementation of NurbsSurface.
 *
 * @author Justin Couch
 * @version $Revision: 1.4 $
 */
public class J3DNurbsSurface extends BaseNurbsSurface
    implements J3DGeometryNodeType {

    /** Texture set map shared by all instances */
    private static final int[] TEX_SET_MAP = { 0,0,0,0,0,0,0,0 };

    /** List of the geometry items that have changed. Always the same */
    private static final int[] CHANGED_GEOM_INDEX = { 0 };

    /** The array of listeners registered with this node */
    private ArrayList listeners;

    /** The implementation of the Java3D geometry */
    private TriangleStripArray impl;

    /** Convenience variable for returning the geometry list */
    private Geometry[] geomList;

    /** Settings for the capability required bits */
    private int[] capReqdBits;

    /** Settings for the capability frequency settting bits */
    private int[] freqReqdBits;

    /** The current max vertex count */
    private int maxVertexCount;

    /**
     * Create a new default instance of the node.
     */
    public J3DNurbsSurface() {
        init();
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not the same type
     */
    public J3DNurbsSurface(VRMLNodeType node) {
        super(node);

        init();
    }

    //----------------------------------------------------------
    // Methods required by the J3DGeometryNodeType interface.
    //----------------------------------------------------------

    /**
     * Notification that the rendering of the event model is complete and
     * that rendering is about to begin. Used as a trigger to regenerate the
     * curve.
     */
    public void allEventsComplete() {

        if(!regenerateSurface()) {
            System.err.println("Bad curve data. Surface removed");

            if(impl != null) {
                fireGeometryRemoved();
                impl = null;
                geomList[0] = null;
            }
        } else if((impl == null) || (maxVertexCount > geometryData.vertexCount)) {
            generateGeometry();
            geomList[0] = impl;

            if(impl != null) {
                if(maxVertexCount > geometryData.vertexCount)
                    fireGeometryChanged();
                else
                    fireGeometryAdded();
            }

        } else  {
            // update the existing coordinate list.
            impl.setCoordinates(0, geometryData.coordinates);
        }
    }

    //----------------------------------------------------------
    // Methods required by the J3DGeometryNodeType interface.
    //----------------------------------------------------------

    /**
     * Returns a J3D Geometry node
     *
     * @return A Geometry node
     */
    public Geometry[] getGeometry() {
        return geomList;
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
        if((l == null) || listeners.contains(l))
            return;

        listeners.add(l);
    }

    /**
     * Remove a listener for geometry changed events. If the listener is null
     * or has not been registered it will silently ignore the requests.
     *
     * @param l The listener to remove
     */
    public void removeGeometryListener(J3DGeometryListener l) {
        if((l == null) || !listeners.contains(l))
            return;

        listeners.remove(l);
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

        if(capBits != null && capBits.containsKey(TriangleStripArray.class))
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

        inSetup = false;

        if(!regenerateSurface())
            return;

        generateGeometry();

        geomList[0] = impl;
    }

    //----------------------------------------------------------
    // Internal Methods
    //----------------------------------------------------------

    /**
     * Common initialisation functionality for constructing the class.
     */
    private void init() {
        listeners = new ArrayList();
        geomList = new Geometry[1];
    }

    /**
     * Convenience method to generate new geometry.
     */
    private void generateGeometry() {
        maxVertexCount = geometryData.vertexCount;

        int format = TriangleStripArray.COORDINATES |
                     TriangleStripArray.NORMALS |
                     TriangleStripArray.TEXTURE_COORDINATE_2;

        impl = new TriangleStripArray(geometryData.vertexCount,
                                      format,
                                      1,
                                      TEX_SET_MAP,
                                      geometryData.stripCounts);

        impl.setCapability(TriangleStripArray.ALLOW_COUNT_WRITE);
        impl.setCapability(TriangleStripArray.ALLOW_COORDINATE_WRITE);
        impl.setCapability(TriangleStripArray.ALLOW_NORMAL_WRITE);
        impl.setCapability(TriangleStripArray.ALLOW_TEXCOORD_WRITE);

        impl.setCoordinates(0, geometryData.coordinates);
        impl.setNormals(0, geometryData.normals);
        impl.setTextureCoordinates(0, 0, geometryData.textureCoordinates);

        J3DUserData u_data = new J3DUserData();
        u_data.geometryData = geometryData;

        impl.setUserData(u_data);

        if(capReqdBits != null) {
            for(int i = 0; i < capReqdBits.length; i++)
                impl.setCapability(capReqdBits[i]);
        }

        if(J3DGlobalStatus.haveFreqBitsAPI && freqReqdBits != null) {
            for(int i = 0; i < freqReqdBits.length; i++)
                impl.setCapabilityIsFrequent(freqReqdBits[i]);
        }
    }

    /**
     * fire a geometry added event to the listeners.
     */
    protected void fireGeometryAdded() {
        int size = listeners.size();
        J3DGeometryListener l;

        for(int i = 0; i < size; i++) {
            try {
                l = (J3DGeometryListener)listeners.get(i);
                l.geometryAdded(CHANGED_GEOM_INDEX);
            } catch(Exception e) {
                System.out.println("Error sending geometry add message: "
                                   + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Fire a geometry changed event to the listeners.
     */
    protected void fireGeometryChanged() {
        int size = listeners.size();
        J3DGeometryListener l;

        for(int i = 0; i < size; i++) {
            try {
                l = (J3DGeometryListener)listeners.get(i);
                l.geometryChanged(CHANGED_GEOM_INDEX);
            } catch(Exception e) {
                System.out.println("Error sending geometry change message: "
                                   + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Fire a geometry removed event to the listeners.
     */
    protected void fireGeometryRemoved() {
        int size = listeners.size();
        J3DGeometryListener l;

        for(int i = 0; i < size; i++) {
            try {
                l = (J3DGeometryListener)listeners.get(i);
                l.geometryRemoved(CHANGED_GEOM_INDEX);
            } catch(Exception e) {
                System.out.println("Error sending geometry remove message: "
                                   + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
