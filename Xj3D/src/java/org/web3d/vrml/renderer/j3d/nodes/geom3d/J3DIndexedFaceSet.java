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

package org.web3d.vrml.renderer.j3d.nodes.geom3d;

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

import org.web3d.vrml.renderer.common.nodes.geom3d.BaseIndexedFaceSet;
import org.web3d.vrml.renderer.common.nodes.GeometryHolder;
import org.web3d.vrml.renderer.common.nodes.GeometryUtils;

/**
 * Java3D implementation of an IndexedFaceSet.
 * <p>
 *
 * This implementation does it's own tesselation of the IFS to avoid letting
 * Java3D do it. J3D has a lot of garbage being generated inside it's
 * IndexedTriangleArray code.
 *
 * Proposed Optimization:
 *    Compact the tesselation arrays when inside a static group
 *    Compact them when max_polygon size > 5 as our estimate might be
 *       really off
 *    NOTE: It seems even with tris the array sizes are off.
 *
 * @author Justin Couch
 * @version $Revision: 2.21 $
 */
public class J3DIndexedFaceSet extends BaseIndexedFaceSet
    implements J3DGeometryNodeType {

    /** The array of listeners registered with this node */
    private ArrayList listeners;

    /** The array holding triangles */
    private TriangleArray implGeom;

    /** User data information at the higher level */
    private J3DUserData userData;

    /** Should we compact the arrays after tesselation */
    private boolean compact;

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

    /** The geometryUtils used */
    private GeometryUtils gutils;

    /** The number of geometry builds.  Optimize for static till proven dynamic */
    private int numBuilds;

    /** Is this the initial build */
    private boolean initialBuild;

    /**
     * Default constructor for a new instance of this node.
     */
    public J3DIndexedFaceSet() {
        super();

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
    public J3DIndexedFaceSet(VRMLNodeType node) {
        super(node);

        init();
    }

    /**
     * Notification that the rendering of the event model is complete and that
     * rendering is about to begin. Used to update the j3d representation
     * only once per frame.
     */
    public void allEventsComplete() {
        buildImpl();
    }

    //-------------------------------------------------------------
    // Methods required by the J3DGeometryNodeType interface.
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
    // Methods required by the J3DNodeType interface.
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

        geomData = new GeometryData();
        geomData.geometryType = GeometryData.TRIANGLES;

        userData = new J3DUserData();
        userData.geometryData = geomData;

        if (isStatic)
            compact = true;

        buildImpl();
        inSetup = false;
    }

    //----------------------------------------------------------
    // Methods internal to J3DIndexedFaceSet
    //----------------------------------------------------------

    /**
     * Common initialisation routines used by the constructors.
     */
    private void init() {
//        min = new float[3];
//        max = new float[3];

//        polygonCount = 0;
//        maxPolySize = 0;
//        maxIndexCount = 0;
//        maxIndexValue = 0;

        changeFlags = 0;
//        initialBuild = true;

        listeners = new ArrayList();
    }


    /**
     * Build the geometry structure used by Java3D from this input.
     */
    private void buildImpl() {
        // We really should do something here so that if the coords are
        // removed, it will clear the object geometry.
        if((vfCoordIndex == null) || (vfCoord == null) ||
           (!inSetup && (changeFlags == 0)))
            return;

        // Start by fetching the raw info from the component nodes
        int num_items = vfCoord.getNumPoints();

        if(num_items < 3)
            return;

        if (gutils == null)
            gutils = new GeometryUtils();

        GeometryHolder gholder = new GeometryHolder();

        // Do not use ccw as appearance does a backfaceNormalFlip

        boolean regen = gutils.generateTriangleArrays(changeFlags, true, true,
           vfCoord, vfColor, vfNormal, vfTexCoord,
           vfCoordIndex, numCoordIndex, vfColorIndex, vfNormalIndex,
           vfTexCoordIndex, true, vfConvex, vfColorPerVertex, vfNormalPerVertex,
           vfCreaseAngle, initialBuild, gholder);

        if (gholder.coordinates == null)
            return;

        gutils.copyData(gholder, geomData);

        if (regen) {
            int format = GeometryArray.COORDINATES |
                         GeometryArray.NORMALS;

            if(vfColor != null) {
                int numColorComponents = vfColor.getNumColorComponents();
                switch(numColorComponents) {
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

            if(vfTexCoord == null) {
                format |= GeometryArray.TEXTURE_COORDINATE_2;
            } else {
                int numTextureDimensions = vfTexCoord.getNumTextureComponents();
                switch(numTextureDimensions) {
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

            int[] counts = new int[4];

            gutils.getCounts(counts);

            int taSize;

            if (counts[2] == 0)
                taSize = 3 * counts[0] + 6 * counts[1];
            else
                taSize = counts[3] * 3;

            implGeom = new TriangleArray(taSize,
                                         format,
                                         num_tex_sets,
                                         tex_maps);

            if(!isStatic) {
                implGeom.setCapability(GeometryArray.ALLOW_COORDINATE_WRITE);
                implGeom.setCapability(GeometryArray.ALLOW_NORMAL_WRITE);
                implGeom.setCapability(GeometryArray.ALLOW_TEXCOORD_WRITE);
                implGeom.setCapability(GeometryArray.ALLOW_COUNT_WRITE);

                implGeom.clearCapabilityIsFrequent(GeometryArray.ALLOW_COUNT_WRITE);

                if(vfColor != null) {
                    implGeom.setCapability(GeometryArray.ALLOW_COLOR_WRITE);
                    implGeom.clearCapabilityIsFrequent(GeometryArray.ALLOW_COLOR_WRITE);
                }
            }

            implGeom.setUserData(userData);

            initialBuild = true;
        }

        texCoords = gholder.textureCoordinates;
        numTexSets = gholder.numTexSets;
        numUniqueTexSets = gholder.numUniqueTexSets;

        if((changeFlags & COORDS_CHANGED) != 0 || initialBuild) {
            implGeom.setCoordinates(0, geomData.coordinates);
        }

        if((((changeFlags & COORDS_CHANGED) != 0) && (vfNormal == null)) ||
           ((changeFlags & NORMALS_CHANGED) != 0) || initialBuild) {

            implGeom.setNormals(0, geomData.normals);
        }

        // Build stuff that we're missing in the texture department
        if(((changeFlags & TEXCOORDS_CHANGED) != 0) || initialBuild) {
            if(vfTexCoord == null) {
                implGeom.setTextureCoordinates(0,
                                               0,
                                               geomData.textureCoordinates);
            } else {
                int num_sets = texCoords.length;

                for(int i = 0; i < num_sets; i++) {
                    implGeom.setTextureCoordinates(i, 0, texCoords[i]);
                }
            }
        }

        if(((changeFlags & COLORS_CHANGED) != 0) || initialBuild) {
            if (vfColor != null)
                implGeom.setColors(0, geomData.colors);
        }

        // finally, clear the flags and notify it's all done
        changeFlags = 0;

        initialBuild = false;

        if (regen & !inSetup) {
            fireGeometryChanged(null);
        }

        // Clean up arrays of stuff not needed if we are a static piece of
        // geometry or in a loader.
        if(isStatic || numBuilds < 1) {
            gutils.reset();
            gutils = null;

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
