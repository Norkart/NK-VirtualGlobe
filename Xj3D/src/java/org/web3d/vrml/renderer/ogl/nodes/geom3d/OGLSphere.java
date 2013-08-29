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

package org.web3d.vrml.renderer.ogl.nodes.geom3d;

// External imports
import java.util.HashMap;
import java.util.Map;

import org.j3d.aviatrix3d.Geometry;
import org.j3d.aviatrix3d.SceneGraphObject;
import org.j3d.aviatrix3d.TriangleStripArray;
import org.j3d.aviatrix3d.VertexGeometry;

import org.j3d.geom.GeometryData;
import org.j3d.geom.SphereGenerator;

// Local import
import org.web3d.vrml.lang.*;

import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLGeometryNodeType;
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.renderer.common.nodes.geom3d.BaseSphere;
import org.web3d.vrml.renderer.ogl.nodes.OGLGeometryNodeType;
import org.web3d.vrml.renderer.ogl.nodes.OGLUserData;

/**
 * OpenGL implementation of a Sphere.
 *
 * For the moment it only generates 2D texture coordinates, rather than
 * 3D is the sibling appearance is using a 3D appearance.
 *
 * @author Alan Hudson
 * @version $Revision: 1.17 $
 */
public class OGLSphere extends BaseSphere implements OGLGeometryNodeType {

    /** The geometry implementation */
    private TriangleStripArray impl;

    /**
     * Construct a default sphere instance
     */
    public OGLSphere() {
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not a Box node, an exception will be
     * thrown.
     *
     * @param node The node to copy
     */
    public OGLSphere(VRMLNodeType node) {
        super(node);
    }

    //-------------------------------------------------------------
    // Methods required by the OGLGeometryNodeType interface.
    //-------------------------------------------------------------

    /**
     * Returns a OGL Geometry node
     *
     * @return A Geometry node
     */
    public Geometry getGeometry() {
        return impl;
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

    //----------------------------------------------------------
    // Methods from OGLVRMLNode class.
    //----------------------------------------------------------

    /**
     * Get the OpenGL scene graph object representation of this node. This will
     * need to be cast to the appropriate parent type when being used. Default
     * implementation returns null.
     *
     * @return The OpenGL representation.
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

        SphereGenerator generator = new SphereGenerator(vfRadius, 32);
        GeometryData data = new GeometryData();

        data.geometryType = GeometryData.TRIANGLE_STRIPS;
        data.geometryComponents = GeometryData.NORMAL_DATA |
                                  GeometryData.TEXTURE_2D_DATA;

        generator.generate(data);

        impl = new TriangleStripArray(false, VertexGeometry.VBO_HINT_STATIC);
        impl.setVertices(TriangleStripArray.COORDINATE_3,
                         data.coordinates,
                         data.vertexCount);
        impl.setStripCount(data.stripCounts, data.numStrips);
        impl.setNormals(data.normals);

        // Make an array of objects for the texture setting
        float[][] textures = { data.textureCoordinates };
        int[] tex_type = { TriangleStripArray.TEXTURE_COORDINATE_2 };
        impl.setTextureCoordinates(tex_type, textures, 1);

        // Setup 4 texture units
        int[] tex_maps = new int[4];

        for(int i=0; i < 4; i++)
            tex_maps[i] = 0;

        impl.setTextureSetMap(tex_maps,4);

        OGLUserData u_data = new OGLUserData();
        u_data.geometryData = data;

        impl.setUserData(u_data);
    }
}
