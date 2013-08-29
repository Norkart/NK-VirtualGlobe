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

package org.web3d.vrml.renderer.j3d.nodes.geospatial;

// External imports
import javax.media.j3d.*;

import com.sun.j3d.utils.geometry.*;

import java.util.ArrayList;
import java.util.Map;

import org.j3d.geom.GeometryData;

import org.opengis.referencing.operation.TransformException;

// Local imports
import org.web3d.vrml.nodes.*;
import org.web3d.vrml.lang.*;

import org.web3d.util.ArrayUtils;

import org.web3d.vrml.renderer.common.nodes.geospatial.BaseGeoElevationGrid;
import org.web3d.vrml.renderer.common.nodes.geospatial.BaseGeoOrigin;
import org.web3d.vrml.renderer.common.nodes.geospatial.ElevationGridGenerator;
import org.web3d.vrml.renderer.j3d.nodes.J3DGeometryListener;
import org.web3d.vrml.renderer.j3d.nodes.J3DGeometryNodeType;
import org.web3d.vrml.renderer.j3d.nodes.J3DUserData;

/**
 * Java3D implementation of an GeoElevationGrid
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
 * @author Justin Couch
 * @version $Revision: 1.4 $
 */
public class J3DGeoElevationGrid extends BaseGeoElevationGrid
    implements J3DGeometryNodeType {

    /** The impl for this class */
    private TriangleStripArray implGeom;

    /** The generator used to construct nex e-grid parts */
    private ElevationGridGenerator generator;

    /** Describes the data for representation */
    private GeometryData data;

    /** The array of listeners registered with this node */
    private ArrayList listeners;

    // Scratch vars for speed
    private float[] v0;
    private float[] v1;
    private float[] v2;
    private float[] v3;

    /**
     * Construct a default instance of this node
     */
    public J3DGeoElevationGrid() {
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
    public J3DGeoElevationGrid(VRMLNodeType node) {
        super(node);

        init();
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
    // Methods required by the J3DVRMLNodeType interface.
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
        // not implemented yet.
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
        // not implemented yet.
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

        buildImpl();
    }

    //----------------------------------------------------------
    // Methods required by the VRMLNode interface.
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
            case FIELD_NORMAL :
                if(vfNormal != null) {
                    float[] newNormals =
                        new float[vfXDimension * (vfZDimension-1) * 2 * 3];

                    float[] normals = new float[vfNormal.getNumNormals()];
                    vfNormal.getVector(normals);
                    setupArray(normals, newNormals);
                    implGeom.setNormals(0, newNormals);
                } else {
                    implGeom.setNormals(0,data.normals);
                }
                break;

            case FIELD_COLOR :
                if(vfColor != null) {
                    float[] newColors =
                        new float[vfXDimension * (vfZDimension-1) * 2 * 3];

                    float[] colors = new float[vfColor.getNumColors()];
                    vfColor.getColor(colors);
                    setupArray(colors, newColors);
                    implGeom.setColors(0, newColors);
                } else {
                    // Bail and rebuild
                    buildImpl();
                }
                break;

            case FIELD_TEXCOORD :
                //  Alan: Could be optimized instead of calling buildImpl
                buildImpl();
                break;

            default:
                super.setValue(index, child);
        }
    }

    //----------------------------------------------------------
    // Methods required by VRMLComponentGeometryNodeType
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
            buildImpl();
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
            buildImpl();
    }

    //----------------------------------------------------------
    // Methods internal to J3DElevationGrid
    //----------------------------------------------------------

    /**
     * Build the J3D representation from the VRML field data
     */
    private void buildImpl() {

        if(vfHeight == null || vfHeight.length == 0)
            return;

        generator = new ElevationGridGenerator((vfXDimension - 1) * vfXSpacing,
                                               (vfZDimension - 1) * vfZSpacing,
                                               vfXDimension, vfZDimension, vfYScale,
                                               vfHeight);


        data.geometryType = GeometryData.TRIANGLE_STRIPS;
        int numSets = 0;

        if (vfTexCoord == null && pTexCoord == null) {
            data.geometryComponents = GeometryData.TEXTURE_2D_DATA;
            numSets=1;
        } else {
            VRMLTextureCoordinateNodeType coords;
            if (pTexCoord != null) {
                coords = (VRMLTextureCoordinateNodeType)
                    pTexCoord.getImplementationNode();
            }
            else {
                coords = vfTexCoord;
            }

            numSets = coords.getNumSets();
        }

        if(vfNormal == null && pNormal == null) {
            data.geometryComponents = data.geometryComponents |
                                      GeometryData.NORMAL_DATA;
        }

        double[] local_origin = null;
        if(vfGeoOrigin != null)
            local_origin = ((BaseGeoOrigin)vfGeoOrigin).getConvertedCoordRef();

        try {
            if(geoCoordSwap) {
                double tmp = vfGeoGridOrigin[0];
                vfGeoGridOrigin[0] = vfGeoGridOrigin[1];
                vfGeoGridOrigin[1] = tmp;
                generator.generateIndexedQuads(data,
                                   geoTransform,
                                   vfGeoGridOrigin,
                                   local_origin,
                                   false,
                                   vfCreaseAngle);
                tmp = vfGeoGridOrigin[0];
                vfGeoGridOrigin[0] = vfGeoGridOrigin[1];
                vfGeoGridOrigin[1] = tmp;
            } else {
                generator.generateIndexedQuads(data,
                                   geoTransform,
                                   vfGeoGridOrigin,
                                   local_origin,
                                   false,
                                   vfCreaseAngle);
            }
        } catch(TransformException te) {
            errorReporter.warningReport(TRANSFORM_ERR_MSG, te);
            return;
        }

        int texMap[] = { 0, 0, 0, 0, 0, 0, 0, 0 };

        int vertex_mask = GeometryArray.COORDINATES |
                          GeometryArray.NORMALS |
                          GeometryArray.TEXTURE_COORDINATE_2;

        if(vfColor != null || pColor != null)
            vertex_mask = vertex_mask | GeometryArray.COLOR_3;

        implGeom = new TriangleStripArray(data.vertexCount,
                                          vertex_mask,
                                          numSets,
                                          texMap,
                                          data.stripCounts);

        J3DUserData u_data = new J3DUserData();
        u_data.geometryData = data;
        implGeom.setUserData(u_data);

        implGeom.setCoordinates(0, data.coordinates);

        if((data.geometryComponents & GeometryData.TEXTURE_2D_DATA) != 0) {
            implGeom.setTextureCoordinates(0, 0, data.textureCoordinates);
        } else {
            VRMLTextureCoordinateNodeType coords;
            if (pTexCoord != null) {
                coords = (VRMLTextureCoordinateNodeType)
                    pTexCoord.getImplementationNode();
            } else {
                coords = vfTexCoord;
            }

            numSets = coords.getNumSets();

            int size;
            float[] lfTexCoord = null;
            float[] tcd_tmp = new float[coords.getSize(0) * 2];

            for(int i=0; i < numSets; i++) {
                int num_comps = coords.getNumTextureComponents();

                size = vfTexCoord.getSize(i);
                texMap[i] = vfTexCoord.isShared(i);

                int req_coord_num = data.vertexCount * num_comps;

                if(size < req_coord_num)
                    size = req_coord_num;

                if(lfTexCoord == null || lfTexCoord.length < size)
                    lfTexCoord = new float[size];

                if(tcd_tmp.length < size)
                    tcd_tmp = new float[size];

                coords.getPoint(i, tcd_tmp);

                // Now convert this to triangle strip values. 0
                int row_size = vfZDimension * num_comps;
                int ts_offset = req_coord_num - 1;
                int base_offset =
                    vfXDimension * vfZDimension * num_comps - 1;

                for(int j = 0; j < (vfXDimension - 1); j++) {
                    for(int k = 0; k < vfZDimension; k++) {

                        lfTexCoord[ts_offset--] = tcd_tmp[base_offset];
                        lfTexCoord[ts_offset--] = tcd_tmp[base_offset - 1];

                        lfTexCoord[ts_offset--] =
                            tcd_tmp[base_offset - row_size];
                        lfTexCoord[ts_offset--] =
                            tcd_tmp[base_offset - row_size - 1];

                        base_offset -= num_comps;
                    }
                }

                if(req_coord_num != 0)
                    implGeom.setTextureCoordinates(i,0,lfTexCoord);
            }
        }

        if(vfNormal != null || pNormal != null) {
            VRMLNormalNodeType normalNode;

            if(pNormal != null) {
                normalNode = (VRMLNormalNodeType)
                    pNormal.getImplementationNode();
            } else {
                normalNode = vfNormal;
            }

            float normals[] = new float[normalNode.getNumNormals()];
            normalNode.getVector(normals);

            float newNormals[] =
                new float[vfXDimension * (vfZDimension-1) * 2 * 3];

            setupArray(normals, newNormals);
            implGeom.setNormals(0, newNormals);
        } else {
            if(vfCcw)
                implGeom.setNormals(0,data.normals);
            else {
                // Bother, need to turn normals around
                int i = 0;
                while(i < data.normals.length / 3) {
                    i++;
                    data.normals[i] = -data.normals[i];
                    i += 2;
                }
                implGeom.setNormals(0,data.normals);
            }
        }

        if(vfColor != null || pColor != null) {
            VRMLColorNodeType colorNode;

            if(pColor != null) {
                colorNode = (VRMLColorNodeType)pColor.getImplementationNode();
            } else {
                colorNode = vfColor;
            }

            float colors[] = new float[colorNode.getNumColors()];
            colorNode.getColor(colors);

            float newColors[] =
                new float[vfXDimension * (vfZDimension-1) * 2 * 3];

            setupArray(colors, newColors);
            implGeom.setColors(0, newColors);
        }

        implGeom.setCapability(GeometryArray.ALLOW_COORDINATE_READ);
        implGeom.setCapability(GeometryArray.ALLOW_COORDINATE_WRITE);
        implGeom.setCapability(GeometryArray.ALLOW_NORMAL_READ);
        implGeom.setCapability(GeometryArray.ALLOW_TEXCOORD_READ);
        implGeom.setCapability(GeometryArray.ALLOW_COUNT_READ);
        implGeom.setCapability(GeometryArray.ALLOW_FORMAT_READ);
    }

    /**
     * Setup the array to feed to GeometryArray for colors and normals.  Uses
     * the data.geometryType to determine the correct format
     *
     * @param in The raw data
     * @parm out The data in the correct format
     */
    private void setupArray(float[] in, float[] out) {

        int count = 0;
        int i = 0;
        int base_count = 0;
        int width_inc = vfXDimension * 3;

        int total_points = vfXDimension * (vfZDimension - 1);

        for(i = total_points; --i >= 0; ) {
            v0[0] = in[base_count];
            v0[1] = in[base_count + 1];
            v0[2] = in[base_count + 2];

            if (vfColorPerVertex == true) {
                v1[0] = in[base_count + width_inc];
                v1[1] = in[base_count + width_inc + 1];
                v1[2] = in[base_count + width_inc + 2];

                if (vfCcw) {
                    out[count++] = v0[0];
                    out[count++] = v0[1];
                    out[count++] = v0[2];

                    out[count++] = v1[0];
                    out[count++] = v1[1];
                    out[count++] = v1[2];
                }
                else {
                    out[count++] = v1[0];
                    out[count++] = v1[1];
                    out[count++] = v1[2];

                    out[count++] = v0[0];
                    out[count++] = v0[1];
                    out[count++] = v0[2];
                }
                base_count += 3;
            }
            else {
                out[count++] = v0[0];
                out[count++] = v0[1];
                out[count++] = v0[2];
                out[count++] = v0[0];
                out[count++] = v0[1];
                out[count++] = v0[2];

                if (i % 2 != 0)
                    base_count += 3;
            }
        }
    }

    /**
     * Common internal initialisation.
     */
    private void init() {
        v0 = new float[3];
        v1 = new float[3];
        v2 = new float[3];
        v3 = new float[3];

        listeners = new ArrayList();
        data = new GeometryData();
    }
}
