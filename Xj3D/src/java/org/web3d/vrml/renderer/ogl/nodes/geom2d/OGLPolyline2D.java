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

package org.web3d.vrml.renderer.ogl.nodes.geom2d;

// External imports
import org.j3d.aviatrix3d.Geometry;
import org.j3d.aviatrix3d.LineStripArray;
import org.j3d.aviatrix3d.SceneGraphObject;

// Local imports
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.renderer.ogl.nodes.OGLGeometryNodeType;
import org.web3d.vrml.renderer.common.nodes.geom2d.BasePolyline2D;

/**
 * OpenGL implementation of an Polyline2D
 * <p>
 *
 * The point set directly maps to Aviatrix3D's LineArray class. When the
 * coordinates change to a different length than the current set, it will
 * notify the geometry listener to fetch the new information.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.5 $
 */
public class OGLPolyline2D extends BasePolyline2D
    implements OGLGeometryNodeType {

    /** The impl for this class */
    private LineStripArray implGeom;

    /**
     * Construct a new point set instance that contains no child nodes.
     */
    public OGLPolyline2D() {
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException Incorrect Node Type
     */
    public OGLPolyline2D(VRMLNodeType node) {
        super(node);
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
    // Methods defined by OGLVRMLNode
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

        implGeom = new LineStripArray();
        int num_lines = numLineSegments / 2;
        implGeom.setVertices(LineStripArray.COORDINATE_2,
                             vfLineSegments,
                             num_lines);

        implGeom.setStripCount(new int[] { num_lines }, 1);
    }
}
