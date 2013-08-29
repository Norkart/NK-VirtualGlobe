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

package org.web3d.vrml.renderer.mobile.nodes.geom3d;

// Standard imports
import java.util.HashMap;
import java.util.Map;

import org.j3d.geom.GeometryData;
import org.j3d.geom.SphereGenerator;

// Application specific import
import org.web3d.vrml.lang.*;

import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLGeometryNodeType;
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.renderer.common.nodes.geom3d.BaseSphere;
import org.web3d.vrml.renderer.mobile.nodes.MobileGeometryNodeType;
import org.web3d.vrml.renderer.mobile.sg.SceneGraphObject;
import org.web3d.vrml.renderer.mobile.sg.Geometry;
import org.web3d.vrml.renderer.mobile.sg.Sphere;
import org.web3d.vrml.renderer.mobile.sg.SGManager;

/**
 * OpenGL implementation of a Sphere
 *
 * @author Alan Hudson
 * @version $Revision: 1.1 $
 */
public class MobileSphere extends BaseSphere implements MobileGeometryNodeType {

    /** The oglSG geometry implmentation */
    private Sphere impl;

    /**
     * Construct a default sphere instance
     */
    public MobileSphere() {
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not a Box node, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not a Group node
     */
    public MobileSphere(VRMLNodeType node) {
        super(node);
    }

    //-------------------------------------------------------------
    // Methods required by the MobileGeometryNodeType interface.
    //-------------------------------------------------------------

    /**
     * Returns a Mobile Geometry node
     *
     * @return A Geometry node
     */
    public Geometry getGeometry() {
        return impl;
    }

    //----------------------------------------------------------
    // Methods from MobileVRMLNode class.
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

        impl = new Sphere(vfRadius);
/*
        SphereGenerator generator = new SphereGenerator(vfRadius, 12);
        GeometryData data = new GeometryData();
        data.geometryType = GeometryData.QUADS;
        data.geometryComponents = GeometryData.NORMAL_DATA |
                                  GeometryData.TEXTURE_2D_DATA;

        generator.generate(data);

        int vertex_mask = QuadArray.COORDINATES |
                          QuadArray.NORMALS |
                          QuadArray.TEXTURE_COORDINATE_2;

        impl = new QuadArray(data.vertexCount, vertex_mask);


        impl.setCoordinates(0, data.coordinates);
        impl.setNormals(0, data.normals);
        impl.setTextureCoordinates(0, 0, data.textureCoordinates);

        impl.setUserData(data);
*/
    }
}
