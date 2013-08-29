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

package org.web3d.vrml.renderer.ogl.nodes;

// External imports
import org.j3d.aviatrix3d.Appearance;
import org.j3d.aviatrix3d.TriangleStripArray;
import org.j3d.aviatrix3d.VertexGeometry;

import javax.vecmath.Vector3f;

import org.j3d.geom.GeometryData;
import org.j3d.geom.BoxGenerator;
import org.j3d.geom.SphereGenerator;

// Local imports
// none

/**
 * Common, global status information about Aviatrix that needs to be found out
 * once during the entire lifetime of the renderer.
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public class OGLGlobalStatus {

    /** A common 1x1x1 box geometry for use in areas like sensors. */
    public static final TriangleStripArray boxGeometry;

    /** A common radius 1 sphere geometry for use in areas like sensors. */
    public static final TriangleStripArray sphereGeometry;

    /** Common Appearance used to turn off rendering objects like sensors */
    public static final Appearance invisibleAppearance;

    /**
     * The -z axis to get for the orientation initial direction that we can
     * then transform using the local to vworld matrix values.
     */
    public static final Vector3f defaultOrientation;

    /**
     * Static constructor to set the value of the haveFreqBitsAPI variable.
     * Do it once at startup.
     */
    static {
        BoxGenerator b_gen = new BoxGenerator(1, 1, 1);
        GeometryData data = new GeometryData();
        data.geometryType = GeometryData.TRIANGLE_STRIPS;

        b_gen.generate(data);

        boxGeometry = new TriangleStripArray(true, VertexGeometry.VBO_HINT_STATIC);
        boxGeometry.setVertices(TriangleStripArray.COORDINATE_3,
                                data.coordinates,
                                data.vertexCount);
        boxGeometry.setStripCount(data.stripCounts, data.numStrips);

        SphereGenerator s_gen = new SphereGenerator(1);
        data.coordinates = null;
        data.stripCounts = null;
        s_gen.generate(data);

        sphereGeometry = new TriangleStripArray(true, VertexGeometry.VBO_HINT_STATIC);
        sphereGeometry.setVertices(TriangleStripArray.COORDINATE_3,
                                   data.coordinates,
                                   data.vertexCount);
        sphereGeometry.setStripCount(data.stripCounts, data.numStrips);

        invisibleAppearance = new Appearance();
        invisibleAppearance.setVisible(false);

        defaultOrientation = new Vector3f(0, 0, -1);
    }
}
