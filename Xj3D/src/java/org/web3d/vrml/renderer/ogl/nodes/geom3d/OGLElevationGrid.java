/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001-2007
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
import org.j3d.aviatrix3d.*;

// Local imports
import org.web3d.vrml.nodes.*;
import org.web3d.vrml.lang.*;

import org.j3d.geom.GeometryData;
import org.j3d.geom.terrain.ElevationGridGenerator;

import org.web3d.vrml.renderer.common.nodes.geom3d.BaseElevationGrid;
import org.web3d.vrml.renderer.common.nodes.BaseComponentGeometryNode;
import org.web3d.vrml.renderer.ogl.nodes.OGLGeometryNodeType;
import org.web3d.vrml.renderer.ogl.nodes.OGLUserData;

/**
 * OpenGL implementation of an ElevationGrid
 * <p>
 *
 * In order to handle colorPerVertex=FALSE and normalPerVertex=FALSE we
 * have to two representations.  Under normal conditions(TRUE,TRUE) we can
 * use TriStrips.  Otherwise we must revert back to Quads to get the right
 * coloring.
 *
 * Issues:
 *
 * If you specify normalPerVertex = FALSE but colorPerVertex = TRUE then
 * the colorInterpolation is different then Cosmo/Cortona.  Is it wrong, hard
 * to say.
 *
 * CreaseAngle is not supported.  All terrains are smooth shaded.
 *
 * Not sure ccw handling is correct.
 *
 * TODO:
 *    Needed Observers: color, normal, texCoord
 *
 * @author Alan Hudson
 * @version $Revision: 1.13 $
 */
public class OGLElevationGrid extends BaseElevationGrid
    implements OGLGeometryNodeType, NodeUpdateListener {

    /** The impl for this class */
    private TriangleStripArray implGeom;

    /** The generator used to construct nex e-grid parts */
    private ElevationGridGenerator generator;

    /** Describes the data for representation */
    private GeometryData data;

    /**
     * Empty constructor
     */
    public OGLElevationGrid() {
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
    public OGLElevationGrid(VRMLNodeType node) {
        super(node);

        init();
    }

    //-------------------------------------------------------------
    // Methods defined by OGLGeometryNodeType
    //-------------------------------------------------------------

    /*
     * Returns a OGL Geometry collection that represents this piece of
     * geometry. If there is only one piece of geometry this will return
     * an array of lenght 1.
     *
     * @return The geometry needed to represent this object
     */
    public Geometry getGeometry() {
        return implGeom;
    }

    //----------------------------------------------------------
    // Methods defined by OGLVRMLNodeType
    //----------------------------------------------------------

    /**
     * Get the Java3D scene graph object representation of this node. This will
     * need to be cast to the appropriate parent type when being used.
     *
     * @return The OGL representation.
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

        implGeom = new TriangleStripArray(true, VertexGeometry.VBO_HINT_STATIC);

        OGLUserData u_data = new OGLUserData();
        u_data.geometryData = data;
        implGeom.setUserData(u_data);

        buildImpl(true, true, true);
    }

    //----------------------------------------------------------
    // Methods defined by VRMLNode
    //----------------------------------------------------------

    /**
     * Set the value of the field at the given index as a node. This would be
     * used to set SFNode field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The node does not match the required
     *    type.
     */
    public void setValue(int index, VRMLNodeType child)
        throws InvalidFieldException, InvalidFieldValueException {

        super.setValue(index, child);

        if(inSetup)
            return;

        switch(index) {
        case FIELD_NORMAL:
        case FIELD_COLOR:
        case FIELD_TEXCOORD:

            if (implGeom.isLive())
                implGeom.dataChanged(this);
            else
                updateNodeDataChanges(implGeom);
            break;
        }
    }

    //----------------------------------------------------------
    // Methods defined by VRMLComponentGeometryNodeType
    //----------------------------------------------------------

    /**
     * Set the components that compose a geometry object.
     *
     * @param comps An array of geometric properties
     * @throws InvalidFieldValueException The node does not match the required
     *    type.
     */
    public void setComponents(VRMLNodeType[] comps)
        throws InvalidFieldValueException {

        super.setComponents(comps);

        if(!inSetup)
            if (implGeom.isLive())
                implGeom.dataChanged(this);
            else
                updateNodeDataChanges(implGeom);
    }

    /**
     * Set a component that composes part of a geometry object.
     *
     * @param comp A geometric property
     * @throws InvalidFieldValueException The node does not match the required
     *    type.
     */
    public void setComponent(VRMLNodeType comp)
        throws InvalidFieldValueException {

        super.setComponent(comp);

        if(!inSetup)
            if (implGeom.isLive())
                implGeom.dataChanged(this);
            else
                updateNodeDataChanges(implGeom);
    }

    //----------------------------------------------------------
    // Methods defined by UpdateListener
    //----------------------------------------------------------

    /**
     * Notification that its safe to update the node now with any operations
     * that could potentially effect the node's bounds.
     *
     * @param src The node or Node Component that is to be updated.
     */
    public void updateNodeBoundsChanges(Object src) {
        buildImpl(false, true, false);
    }

    /**
     * Notification that its safe to update the node now with any operations
     * that only change the node's properties, but do not change the bounds.
     *
     * @param src The node or Node Component that is to be updated.
     */
    public void updateNodeDataChanges(Object src) {
        buildImpl(false, false, true);
    }

    //----------------------------------------------------------
    // Internal Methods
    //----------------------------------------------------------

    /**
     * Set the height field.
     *
     * @param value The new value
     * @param numValid The number of valid entries
     */
    protected void setHeight(float[] value, int numValid) {
        super.setHeight(value, numValid);
        if (inSetup)
            return;

        // when the normals are being auto-generated (i.e. there is
        // not a Normal node), then they will also have to be reset
        // in the Geometry when the heights changed
        boolean resetNormals = (vfNormal == null && pNormal == null);

        if (implGeom.isLive()) {
            implGeom.boundsChanged(this);
            if ( resetNormals ) {
                implGeom.dataChanged(this);
            }
        } else {
            updateNodeBoundsChanges(implGeom);
            if ( resetNormals ) {
                updateNodeDataChanges(implGeom);
            }
        }
    }

    /**
     * Build the OGL representation from the VRML field data
     */
    private void buildImpl(boolean initial, boolean boundsChanged, boolean dataChanged) {

        if(vfHeight == null || vfHeight.length == 0)
            return;

        boolean generated_textures = false;

        data.geometryComponents = 0;
        if(vfTexCoord == null && pTexCoord == null) {
            generated_textures = true;
            data.geometryComponents = GeometryData.TEXTURE_2D_DATA;
        }

        if(vfNormal == null && pNormal == null) {
            data.geometryComponents = data.geometryComponents |
                GeometryData.NORMAL_DATA;
        }

        if(initial || boundsChanged) {
            // If there's not enough points to even make a full triangle, ignore
            // this step and just tell the geometry we have nothing.
            if(vfHeight.length > 2) {
                generator = new ElevationGridGenerator((vfXDimension - 1) * vfXSpacing,
                    (vfZDimension - 1) * vfZSpacing,
                    vfXDimension, vfZDimension,
                    vfHeight,
                    0, false);

                generator.generate(data);
            } else {
                data.numStrips = 0;
            }

            if (vfHeight.length < BaseComponentGeometryNode.VBO_MIN_VERTICES)
            	implGeom.setVBOEnabled(false);

            implGeom.setVertices(VertexGeometry.COORDINATE_3,
                data.coordinates,
                data.vertexCount);

            implGeom.setStripCount(data.stripCounts, data.numStrips);
        }

        // Don't bother passing anything else on. This is fine for most
        // applications, because nothing will render anyway. However, what
        // would shaders do that maybe are expecting to have something?
        // We know that aviatrix3d will not pass the geometry down the pipe,
        // but perhaps something in the shader spec may be expecting this?
        if(vfHeight.length <= 2)
            return;

        if(initial || dataChanged) {
            if(generated_textures) {
                int[] tex_types = new int[] { VertexGeometry.TEXTURE_COORDINATE_2 };
                float[][] tex_coords = { data.textureCoordinates };
                implGeom.setTextureCoordinates(tex_types,
                    tex_coords,
                    1);
            } else {
                VRMLTextureCoordinateNodeType coords;
                if(pTexCoord != null) {
                    coords = (VRMLTextureCoordinateNodeType)
                        pTexCoord.getImplementationNode();
                } else {
                    coords = vfTexCoord;
                }

                int size;
                int num_tex_sets = coords.getNumSets();
                float[][] lfTexCoord = new float[num_tex_sets][];
                int[] tex_set_map = new int[num_tex_sets];
                int[] tex_types = new int[num_tex_sets];

                for(int i = 0; i < num_tex_sets; i++) {
                    int num_comps = coords.getNumTextureComponents();

                    switch(num_comps) {
                    case 1:
                        tex_types[i] = VertexGeometry.TEXTURE_COORDINATE_1;
                        break;

                    case 2:
                        tex_types[i] = VertexGeometry.TEXTURE_COORDINATE_2;
                        break;

                    case 3:
                        tex_types[i] = VertexGeometry.TEXTURE_COORDINATE_3;
                        break;

                    case 4:
                        tex_types[i] = VertexGeometry.TEXTURE_COORDINATE_4;
                        break;
                    }

                    size = coords.getSize(i);

                    tex_set_map[i] = coords.isShared(i);

                    int req_coord_num = data.vertexCount * num_comps;

                    if(size < req_coord_num)
                        size = req_coord_num;

                    if(lfTexCoord[i] == null || lfTexCoord[i].length < size)
                        lfTexCoord[i] = new float[size];

                    coords.getPoint(i, lfTexCoord[i]);
                }

                implGeom.setTextureCoordinates(tex_types,
                    lfTexCoord,
                    num_tex_sets);
                implGeom.setTextureSetMap(tex_set_map, num_tex_sets);
            }

            if(vfNormal != null || pNormal != null) {
                VRMLNormalNodeType normalNode;

                if (pNormal != null) {
                    normalNode = (VRMLNormalNodeType)
                        pNormal.getImplementationNode();
                } else {
                    normalNode = vfNormal;
                }

                float[] normals = new float[normalNode.getNumNormals()];
                normalNode.getVector(normals);

                float[] newNormals = new float[data.vertexCount * 3];
                setupArray(normals, newNormals, vfNormalPerVertex);
                implGeom.setNormals(newNormals);
            } else {
                if(vfCcw)
                    implGeom.setNormals(data.normals);
                else {
                    // Bother, need to turn normals around
                    int i = 1;
                    while(i < data.normals.length / 3) {
                        data.normals[i] = -data.normals[i];
                        i += 3;
                    }

                    implGeom.setNormals(data.normals);
                }
            }

            if(vfColor != null || pColor != null) {
                VRMLColorNodeType color_node;

                if(pColor != null) {
                    color_node = (VRMLColorNodeType)pColor.getImplementationNode();
                } else {
                    color_node = vfColor;
                }

                int num_comps = color_node.getNumColorComponents();
                int num_cols = color_node.getNumColors();
                float[] colors = new float[num_cols];

                color_node.getColor(colors);

                float[] newColors = new float[data.vertexCount * num_comps];

                setupArray(colors, newColors, vfColorPerVertex);

                implGeom.setColors((num_comps == 4), newColors);
            }
        }
    }

    /**
     * Setup the array to feed to VertexGeometry for colors and normals.  Uses
     * the data.geometryType to determine the correct format
     *
     * @param in The raw data
     * @parm out The data in the correct format
     * @param perVertex true if this is per-vertex values
     */
    private void setupArray(float[] in, float[] out, boolean perVertex) {

        int count = 0;
        int base_count = 0;
        int width_inc = vfXDimension * 3;

        int total_points = vfXDimension * (vfZDimension - 1);

        if(perVertex) {
            if(vfCcw) {
                for(int i = total_points; --i >= 0; ) {
                    out[count++] = in[base_count];
                    out[count++] = in[base_count + 1];
                    out[count++] = in[base_count + 2];

                    out[count++] = in[base_count + width_inc];
                    out[count++] = in[base_count + width_inc + 1];
                    out[count++] = in[base_count + width_inc + 2];

                    base_count += 3;
                }
            } else {
                for(int i = total_points; --i >= 0; ) {
                    out[count++] = in[base_count + width_inc];
                    out[count++] = in[base_count + width_inc + 1];
                    out[count++] = in[base_count + width_inc + 2];

                    out[count++] = in[base_count];
                    out[count++] = in[base_count + 1];
                    out[count++] = in[base_count + 2];
                    base_count += 3;
                }
            }
        } else {
            for(int i = total_points; --i >= 0; ) {
                out[count++] = in[base_count];
                out[count++] = in[base_count + 1];
                out[count++] = in[base_count + 2];
                out[count++] = in[base_count];
                out[count++] = in[base_count + 1];
                out[count++] = in[base_count + 2];

                if(i % 2 != 0)
                    base_count += 3;
            }
        }
    }

    /**
     * Common internal initialisation.
     */
    private void init() {
        data = new GeometryData();

        data.geometryType = GeometryData.TRIANGLE_STRIPS;
        data.geometrySubType = ElevationGridGenerator.ABSOLUTE_HEIGHTS;
    }
}
