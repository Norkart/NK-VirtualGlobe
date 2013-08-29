/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001-2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.j3d.nodes.render;

// External imports
import javax.media.j3d.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.j3d.geom.GeometryData;

// Local imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;
import org.web3d.vrml.renderer.j3d.nodes.*;

import org.web3d.vrml.renderer.common.nodes.render.BaseIndexedTriangleFanSet;
import org.web3d.vrml.renderer.common.nodes.GeometryHolder;
import org.web3d.vrml.renderer.common.nodes.GeometryUtils;

/**
 * Java3D implementation of an IndexedTriangleFanSet.
 * <p>
 *
 * @author Justin Couch, Alan Hudson
 * @version $Revision: 1.3 $
 */
public class J3DIndexedTriangleFanSet extends BaseIndexedTriangleFanSet
    implements J3DGeometryNodeType {

    /** The array of listeners registered with this node */
    private ArrayList listeners;

    /** The array holding triangles */
    private GeometryArray implGeom;

    /** Final processed version of the texture coordinates */
    private float[][] texCoords;

    /** Final processed version of the texture types of each stage */
    private int[] texTypes;

    /** Final processed version of the texture set mapping of each stage */
    private int[] texSetMap;

    /** Final number of texture sets to send to the graphics card */
    private int numTexSets;

    /** Final number of texture sets to send to the graphics card */
    private int numUniqueTexSets;

    /** Flag to indicate the coordinates changed, and not the index */
    private boolean coordChanged;

    /** Flag to indicate the colors changed */
    private boolean colorChanged;

    /** Flag to indicate the texture coords changed */
    private boolean texCoordChanged;

    /** Flag to indicate the normals changed */
    private boolean normalChanged;

    /** Flag to indicate the index list changed */
    private boolean indexChanged;

    /** Are we using indexed geometry */
    private boolean indexed;

    /** The geometryUtils used */
    private GeometryUtils gutils;

    /** The number of geometry builds.  Optimize for static till proven dynamic */
    private int numBuilds;

    /**
     * Construct a new point set instance that contains no child nodes.
     */
    public J3DIndexedTriangleFanSet() {
        init();
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException Incorrect Node Type
     */
    public J3DIndexedTriangleFanSet(VRMLNodeType node) {
        super(node);

        init();
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
        int ret_val = 0;

        if(vfTexCoord != null)
            ret_val = vfTexCoord.getNumSets();

        return ret_val;
    }

    /**
     * Get the texture coordinate generation mode.  NULL is returned
     * if the texture coordinates are not generated.
     *
     * @param setNum The set which this tex gen mode refers
     * @return The mode or NULL
     */
    public String getTexCoordGenMode(int setNum) {
        String ret_val = null;

        if(vfTexCoord != null)
            ret_val = vfTexCoord.getTexCoordGenMode(setNum);

        return ret_val;
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
    // Methods defined by J3DVRMLNode
    //----------------------------------------------------------

    /**
     * Provide the set of mappings that override anything that the loader
     * might set. Default implementation does nothing.
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
    }

    /**
     * Get the J3D scene graph object representation of this node. This will
     * need to be cast to the appropriate parent type when being used.
     *
     * @return The J3D representation.
     */
    public SceneGraphObject getSceneGraphObject() {
        return implGeom;
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Build the geometry structure used by Java3D from this input.
     */
    protected void buildImpl() {
        boolean newImpl = false;

        // Decide whether to rebuild
        if (vfNormal != null) {
            geomData.geometryType = GeometryData.INDEXED_TRIANGLE_FANS;

            if((changeFlags & COORDS_INDEX_CHANGED) != 0) {
                updateIndexMaps();
            }

            if (!indexed || implGeom == null) {
                int num_tex_sets =
                    (vfTexCoord == null) ? 1 : vfTexCoord.getNumSets();

                // Setup 4 texture units
                int[] tex_maps = new int[num_tex_sets >= 4 ? num_tex_sets : 4];

                if(vfTexCoord != null) {

                    for(int i=0; i < num_tex_sets; i++)
                        tex_maps[i] = vfTexCoord.isShared(i);

                    // Default non set units to use last defined tex coordinates
                    for(int i=num_tex_sets; i < 4; i++)
                        tex_maps[i] = num_tex_sets - 1;
                }

                implGeom = new IndexedTriangleFanArray(vfCoord.getNumPoints() / 3,
                                                    getFormat() | IndexedGeometryArray.USE_COORD_INDEX_ONLY,
                                                    num_tex_sets,
                                                    tex_maps,
                                                    numIndex,
                                                    fanCounts);

                setCapabilities();

                ((IndexedTriangleFanArray)implGeom).setCoordinateIndices(0, processedIndex);
                //((IndexedTriangleFanArray)implGeom).setValidIndexCount(numIndex);

                J3DUserData u_data = new J3DUserData();
                u_data.geometryData = geomData;

                implGeom.setUserData(u_data);

                indexed = true;
                newImpl = true;
            }

            if(((changeFlags & COORDS_CHANGED) != 0)) {
                updateCoordinateArray();
            }

            if(((changeFlags & COLORS_CHANGED) != 0)) {
                updateColorArray();
            }

            if(((changeFlags & NORMALS_CHANGED) != 0)) {
                if(!vfNormalPerVertex) {
                    errorReporter.warningReport("Normal per face not supported in IndexedTriangleFanSet", null);
                } else
                    updateNormalArray();
            }

            if(((changeFlags & TEXCOORDS_CHANGED) != 0)) {
                numTexSets = vfTexCoord.getNumSets();
                int numRealSets = numTexSets;
                int max_set_size = vfTexCoord.getSize(0);
                int num_tex_comp = vfTexCoord.getNumTextureComponents();
                int tex_format = 0;

                for(int i = 0; i < numTexSets; i++) {
                    if (i != vfTexCoord.isShared(i))
                        numRealSets--;
                }

                // first check for max required size

                for(int i = 1; i < numTexSets; i++) {
                    int size = vfTexCoord.getSize(i);
                    if(max_set_size > size)
                        max_set_size = size;
                }

                if(max_set_size < (geomData.vertexCount * num_tex_comp))
                    max_set_size = geomData.vertexCount * num_tex_comp;

                if (texCoords == null || texCoords.length != numRealSets)
                    texCoords = new float[numRealSets][];


                if (texSetMap == null || texSetMap.length != numTexSets)
                    texSetMap = new int[numTexSets];

                for(int i = 0; i < numTexSets; i++)
                    texSetMap[i] = vfTexCoord.isShared(i);

                if (texTypes == null || texTypes.length != numUniqueTexSets)
                    texTypes = new int[numUniqueTexSets];

                switch(num_tex_comp) {
                    case 2:
                        tex_format = TriangleArray.TEXTURE_COORDINATE_2;
                        break;

                    case 3:
                        tex_format = TriangleArray.TEXTURE_COORDINATE_3;
                        break;

                    case 4:
                        tex_format = TriangleArray.TEXTURE_COORDINATE_4;
                        break;

                    default:
                        // we should never get this, but just in case
                        System.out.println("Invalid number of texture " +
                                           "components " +
                                           vfTexCoord.getNumTextureComponents());
                }

                for(int i = 0; i < numUniqueTexSets; i++) {
                    texTypes[i] = tex_format;
                }

                int currSet = 0;

                for(int i = 0; i < numTexSets; i++) {
                    texSetMap[i] = vfTexCoord.isShared(i);

                    if(texSetMap[i] == currSet) {
                        texCoords[currSet] = new float[max_set_size];
                        vfTexCoord.getPoint(i, texCoords[currSet++]);
                    }
                }
            }
        } else {
            geomData.geometryType = GeometryData.TRIANGLES;

            // Turn into a flat triangle array
            if (gutils == null)
                gutils = new GeometryUtils();

            GeometryHolder gholder = new GeometryHolder();

            float creaseAngle;

            if (vfNormalPerVertex)
                creaseAngle = (float)Math.PI;
            else
                creaseAngle = 0;

            // TODO: Should this add -1's to indexes to avoid tesselation?
            // Do not use ccw as appearance does a backfaceNormalFlip

            boolean regen = gutils.generateTriangleArrays(changeFlags, false, true,
               vfCoord, vfColor, vfNormal, vfTexCoord,
               vfIndex, vfIndex.length, vfIndex, vfIndex,
               vfIndex, true, true, vfColorPerVertex, vfNormalPerVertex,
               creaseAngle, newImpl, gholder);

            if (gholder.coordinates == null)
                return;

            gutils.copyData(gholder, geomData);

            texCoords = gholder.textureCoordinates;
            numTexSets = gholder.numTexSets;
            numUniqueTexSets = gholder.numUniqueTexSets;

            if (indexed || regen || implGeom == null) {
                int num_tex_sets =
                    (vfTexCoord == null) ? 1 : vfTexCoord.getNumSets();

                // Setup 4 texture units
                int[] tex_maps = new int[num_tex_sets >= 4 ? num_tex_sets : 4];

                if(vfTexCoord != null) {

                    for(int i=0; i < num_tex_sets; i++)
                        tex_maps[i] = vfTexCoord.isShared(i);

                    // Default non set units to use last defined tex coordinates
                    for(int i=num_tex_sets; i < 4; i++)
                        tex_maps[i] = num_tex_sets - 1;
                }

                implGeom = new TriangleArray(geomData.coordinates.length / 3,
                                             getFormat(),
                                             num_tex_sets,
                                             tex_maps);
                J3DUserData u_data = new J3DUserData();
                u_data.geometryData = geomData;

                implGeom.setUserData(u_data);

                setCapabilities();

                indexed = false;
                newImpl = true;
            }

            if(vfTexCoord == null) {
                 // Clear tex coords
                if (implGeom.isLive()) {
                    texTypes = null;
                    texCoords = null;
                }
            } else {
                int tex_format = 0;

                int num_tex_comp = vfTexCoord.getNumTextureComponents();

                switch(num_tex_comp) {
                    case 2:
                        tex_format = TriangleArray.TEXTURE_COORDINATE_2;
                        break;

                    case 3:
                        tex_format = TriangleArray.TEXTURE_COORDINATE_3;
                        break;

                    case 4:
                        tex_format = TriangleArray.TEXTURE_COORDINATE_4;
                        break;

                    default:
                        // we should never get this, but just in case
                        System.out.println("Invalid number of texture " +
                                           "components " +
                                           vfTexCoord.getNumTextureComponents());
                }

                if (texSetMap == null || texSetMap.length != numTexSets)
                    texSetMap = new int[numTexSets];

                for(int i = 0; i < numTexSets; i++)
                    texSetMap[i] = vfTexCoord.isShared(i);

                if (texTypes == null || texTypes.length != numTexSets)
                    texTypes = new int[numTexSets];

                for(int i = 0; i < numTexSets; i++) {
                    texTypes[i] = tex_format;
                }
            }
        }

        if (newImpl) {
            indexChanged = true;
            coordChanged = true;
            colorChanged = true;
            normalChanged = true;
            texCoordChanged = true;

            implGeom.setCoordinates(0, geomData.coordinates);

            if (geomData.normals != null) {
                implGeom.setNormals(0, geomData.normals);
            }
            if(vfTexCoord != null) {
                int num_sets = texCoords.length;

                for(int i = 0; i < num_sets; i++) {
                    implGeom.setTextureCoordinates(i, 0, texCoords[i]);
                }
            }

            if(geomData.colors != null)
                implGeom.setColors(0, geomData.colors);

        } else {
            if((changeFlags & COORDS_CHANGED) != 0) {
                implGeom.setCoordinates(0, geomData.coordinates);
            }

            if((((changeFlags & COORDS_CHANGED) != 0) && (vfNormal == null)) ||
               ((changeFlags & NORMALS_CHANGED) != 0)) {

                if(geomData.colors != null)
                    implGeom.setColors(0, geomData.colors);
            }

            // Build stuff that we're missing in the texture department
            if(((changeFlags & TEXCOORDS_CHANGED) != 0)) {
                if(vfTexCoord == null) {
                    // TODO:
                    // There appears to be no way in Java3D to clear the previously set texture
                    // coordinates. Wonder what we should do here?
                } else {
                    int num_sets = texCoords.length;

                    for(int i = 0; i < num_sets; i++) {
                        implGeom.setTextureCoordinates(i, 0, texCoords[i]);
                    }
                }
            }

            if(((changeFlags & COLORS_CHANGED) != 0)) {
                if(geomData.colors != null)
                    implGeom.setColors(0, geomData.colors);
            }
        }

        changeFlags = 0;

        if(newImpl && !inSetup)
            fireGeometryChanged(null);

        if(isStatic || numBuilds < 1) {
            if (gutils != null) {
                gutils.reset();
                gutils = null;
            }

            if (inSetup) {
                // We can ditch the tex coords as well
                texCoords = null;
                texTypes = null;
                texSetMap = null;
            }
        }

        numBuilds++;
    }

    /**
     * Common initialisation functionality.
     */
    private void init() {
        geomData = new GeometryData();
        changeFlags = 0;

        listeners = new ArrayList();

        geomData = new GeometryData();
    }

    /**
     * Get the J3D format to use.
     *
     * @return the format
     */
    private int getFormat() {
        int format = GeometryArray.COORDINATES |
                     GeometryArray.NORMALS;

        if(vfColor != null) {
            switch(vfColor.getNumColorComponents()) {
                case 1:
                case 2:
                    System.out.println("Can't handle 1 or 2 component " +
                                       "colors right now");
                    // so fall through to 3 comp anyway....
                case 3:
                    format |= GeometryArray.COLOR_3;
                    break;

                case 4:
                    format |= GeometryArray.COLOR_4;

                default:
                    // we should never get this, but just in case
                    System.out.println("Invalid number of color " +
                                       "components " +
                                       vfColor.getNumColorComponents());
            }
        }

        if(vfTexCoord != null) {
            switch(vfTexCoord.getNumTextureComponents()) {
                case 2:
                    format |= GeometryArray.TEXTURE_COORDINATE_2;
                    break;

                case 3:
                    format |= GeometryArray.TEXTURE_COORDINATE_3;
                    break;

                case 4:
                    format |= GeometryArray.TEXTURE_COORDINATE_4;
                    break;

                default:
                    // we should never get this, but just in case
                    System.out.println("Invalid number of texture " +
                                       "components " +
                                       vfTexCoord.getNumTextureComponents());
            }
        }

        return format;
    }

    /**
     * Setup the capability bits.
     */
    private void setCapabilities() {
        if(!isStatic) {
            implGeom.setCapability(GeometryArray.ALLOW_COORDINATE_WRITE);
            implGeom.setCapability(GeometryArray.ALLOW_NORMAL_WRITE);
            implGeom.setCapability(GeometryArray.ALLOW_TEXCOORD_WRITE);
            implGeom.setCapability(GeometryArray.ALLOW_COUNT_WRITE);
            implGeom.setCapability(IndexedGeometryArray.ALLOW_COORDINATE_INDEX_WRITE);

            implGeom.clearCapabilityIsFrequent(GeometryArray.ALLOW_COUNT_WRITE);

            if(vfColor != null) {
                implGeom.setCapability(GeometryArray.ALLOW_COLOR_WRITE);
                implGeom.clearCapabilityIsFrequent(GeometryArray.ALLOW_COLOR_WRITE);
            }
        }
    }

    /**
     * fire a geometry added event to the listeners.
     *
     * @param items The geometry items that have been added
     */
    private void fireGeometryAdded(int[] items) {
        int size = listeners.size();
        J3DGeometryListener l;

        for(int i = 0; i < size; i++) {
            try {
                l = (J3DGeometryListener)listeners.get(i);
                l.geometryAdded(items);
            } catch(Exception e) {
                System.out.println("Error sending geometry add message: "
                                   + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * fire a geometry changed event to the listeners.
     *
     * @param items The geometry items that have changed or null for all
     */
    private void fireGeometryChanged(int[] items) {
        int size = listeners.size();
        J3DGeometryListener l;

        for(int i = 0; i < size; i++) {
            try {
                l = (J3DGeometryListener)listeners.get(i);
                l.geometryChanged(items);
            } catch(Exception e) {
                System.out.println("Error sending geometry change message: "
                                   + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * fire a geometry removed event to the listeners.
     *
     * @param items The geometry items that have removed or null for all
     */
    private void fireGeometryRemoved(int[] items) {
        int size = listeners.size();
        J3DGeometryListener l;

        for(int i = 0; i < size; i++) {
            try {
                l = (J3DGeometryListener)listeners.get(i);
                l.geometryRemoved(items);
            } catch(Exception e) {
                System.out.println("Error sending geometry remove message: "
                                   + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
