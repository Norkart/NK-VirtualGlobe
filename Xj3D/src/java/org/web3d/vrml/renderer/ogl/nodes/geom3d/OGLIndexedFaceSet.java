/*****************************************************************************
 *                        Yumtech, Inc Copyright (c) 2001-2005
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.ogl.nodes.geom3d;

// External imports
import java.util.HashMap;

import org.j3d.aviatrix3d.*;

import org.j3d.geom.GeometryData;

// Local imports
import org.web3d.vrml.nodes.*;
import org.web3d.vrml.lang.*;

import org.web3d.vrml.renderer.ogl.nodes.*;

import org.web3d.vrml.renderer.common.nodes.geom3d.BaseIndexedFaceSet;
import org.web3d.vrml.renderer.common.nodes.GeometryHolder;
import org.web3d.vrml.renderer.common.nodes.GeometryUtils;

/**
 * OpenGL implementation of an IndexedFaceSet.
 * <p>
 *
 * @author Alan Hudson, Justin Couch
 * @version $Revision: 1.33 $
 */
public class OGLIndexedFaceSet extends BaseIndexedFaceSet
    implements OGLGeometryNodeType, NodeUpdateListener {

    /** The array holding triangles */
    private TriangleArray implGeom;

    /** Flag to say normals have changed when updating the geometry */
    private boolean normalsChanged;

    /** Flag to say texture coords have changed when updating the geometry */
    private boolean texCoordsChanged;

    /** Flag to say colors have changed when updating the geometry */
    private boolean colorsChanged;

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

    /** Did the vbo state change */
    private boolean vboChanged;

    /**
     * Default constructor to build an instance with default field values.
     */
    public OGLIndexedFaceSet() {
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
    public OGLIndexedFaceSet(VRMLNodeType node) {
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

    //----------------------------------------------------------
    // Methods required by the UpdateListener interface.
    //----------------------------------------------------------

    /**
     * Notification that its safe to update the node now with any operations
     * that could potentially effect the node's bounds.
     *
     * @param src The node or Node Component that is to be updated.
     */
    public void updateNodeBoundsChanges(Object src) {
        implGeom.setVertices(TriangleArray.COORDINATE_3,
                             geomData.coordinates,
                             geomData.vertexCount);
    }

    /**
     * Notification that its safe to update the node now with any operations
     * that only change the node's properties, but do not change the bounds.
     *
     * @param src The node or Node Component that is to be updated.
     */
    public void updateNodeDataChanges(Object src) {

        if(colorsChanged) {
            int num_comp =
                (vfColor == null) ? 0 : vfColor.getNumColorComponents();
            boolean has_alpha = num_comp == 4;

            implGeom.setColors(has_alpha, geomData.colors);
            colorsChanged = false;

            if (vfColor == null) {
                localColors = false;
            }
        }

        if(normalsChanged) {
            implGeom.setNormals(geomData.normals);
            normalsChanged = false;
        }

        if(texCoordsChanged) {
            implGeom.setTextureCoordinates(texTypes,
                                           texCoords,
                                           numUniqueTexSets);
            implGeom.setTextureSetMap(texSetMap, numTexSets);

            texCoordsChanged = false;
        }

        if (vboChanged) {
            // Only ever change to false after activity
            implGeom.setVBOEnabled(false);

            vboChanged = false;
        }
    }

    //-------------------------------------------------------------
    // Methods required by the OGLGeometryNodeType interface.
    //-------------------------------------------------------------

    /*
     * Returns a J3D Geometry collection that represents this piece of
     * geometry. If there is only one piece of geometry this will return
     * an array of lenght 1.
     *
     * @return The geometry needed to represent this object
     */
    public Geometry getGeometry() {
        return implGeom;
    }

    //----------------------------------------------------------
    // Methods required by the OGLVRMLNodeType interface.
    //----------------------------------------------------------

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

        implGeom = new TriangleArray(true, VertexGeometry.VBO_HINT_STATIC);

        OGLUserData u_data = new OGLUserData();
        u_data.geometryData = geomData;

        implGeom.setUserData(u_data);

        buildImpl();
    }

    //----------------------------------------------------------
    // Methods internal to OGLIndexedFaceSet
    //----------------------------------------------------------

    /**
     * Common initialisation routines used by the constructors.
     */
    private void init() {
        geomData = new GeometryData();
        geomData.geometryType = GeometryData.TRIANGLES;

        changeFlags = 0;
        initialBuild = true;

        normalsChanged = false;
        texCoordsChanged = false;
        colorsChanged = false;
        vboChanged = false;

        numBuilds = 0;
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

		if (initialBuild && num_items < VBO_MIN_VERTICES)
			implGeom.setVBOEnabled(false);

        if(num_items < 3)
            return;

        if (gutils == null)
            gutils = new GeometryUtils();

        GeometryHolder gholder = new GeometryHolder();

        gutils.generateTriangleArrays(changeFlags, true, true,
           vfCoord, vfColor, vfNormal, vfTexCoord,
           vfCoordIndex, numCoordIndex, vfColorIndex, vfNormalIndex,
           vfTexCoordIndex, vfCcw, vfConvex, vfColorPerVertex, vfNormalPerVertex,
           vfCreaseAngle, initialBuild, gholder);

        if (initialBuild && gholder.coordinates == null)
            return;

        if (!initialBuild) {
            vboChanged = true;

            if (implGeom.isLive())
                implGeom.dataChanged(this);
            else
                updateNodeDataChanges(implGeom);
        }

        gutils.copyData(gholder, geomData);

        texCoords = gholder.textureCoordinates;
        numTexSets = gholder.numTexSets;
        numUniqueTexSets = gholder.numUniqueTexSets;

        if((changeFlags & COORDS_CHANGED) != 0 || initialBuild) {
            if (implGeom.isLive())
                implGeom.boundsChanged(this);
            else
                updateNodeBoundsChanges(implGeom);
        }

        if((((changeFlags & COORDS_CHANGED) != 0) && (vfNormal == null)) ||
           ((changeFlags & NORMALS_CHANGED) != 0) || initialBuild) {

            normalsChanged = true;
            if (implGeom.isLive())
                implGeom.dataChanged(this);
            else
                updateNodeDataChanges(implGeom);
        }

        // Build stuff that we're missing in the texture department
        if(((changeFlags & TEXCOORDS_CHANGED) != 0) || initialBuild) {
            if(vfTexCoord == null) {
                numTexSets = 1;
                numUniqueTexSets = 1;

                if (texSetMap == null || texSetMap.length != numTexSets)
                    texSetMap = new int[numTexSets];

                texSetMap[0] = 0;

                if (texTypes == null || texTypes.length != numUniqueTexSets)
                    texTypes = new int[numUniqueTexSets];

                texTypes[0] = VertexGeometry.TEXTURE_COORDINATE_2;

                texCoordsChanged = true;

                if (implGeom.isLive())
                    implGeom.dataChanged(this);
                else
                    updateNodeDataChanges(implGeom);
            } else {
                int tex_format = 0;

                int num_tex_comp = vfTexCoord.getNumTextureComponents();

                switch(num_tex_comp) {
                    case 1:
                        tex_format = TriangleArray.TEXTURE_COORDINATE_1;
                        break;

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

                if (texTypes == null || texTypes.length != numUniqueTexSets)
                    texTypes = new int[numUniqueTexSets];

                for(int i = 0; i < numUniqueTexSets; i++) {
                    texTypes[i] = tex_format;
                }

                texCoordsChanged = true;

                if (implGeom.isLive())
                    implGeom.dataChanged(this);
                else
                    updateNodeDataChanges(implGeom);
            }
        }

        if(((changeFlags & COLORS_CHANGED) != 0) || initialBuild) {
            colorsChanged = true;
            if (implGeom.isLive())
                implGeom.dataChanged(this);
            else
                updateNodeDataChanges(implGeom);
        }

        initialBuild = false;

        // finally, clear the flags and notify it's all done
        changeFlags = 0;

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
}
