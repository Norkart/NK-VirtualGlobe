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

package org.web3d.vrml.renderer.j3d.nodes;

// Standard imports
import java.security.AccessController;
import java.security.PrivilegedAction;

import javax.media.j3d.Appearance;
import javax.media.j3d.RenderingAttributes;
import javax.media.j3d.TriangleStripArray;

import javax.vecmath.Vector3d;

import org.j3d.geom.GeometryData;
import org.j3d.geom.BoxGenerator;
import org.j3d.geom.SphereGenerator;

// Application specific imports
// none

/**
 * Common, global status information about Java3D that needs to be found out
 * once during the entire lifetime of the renderer.
 *
 * @author Justin Couch
 * @version $Revision: 1.4 $
 */
public class J3DGlobalStatus {

    /** Flag for the API being new enough to have frquency bit setting */
    public static final boolean haveFreqBitsAPI;

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
    public static final Vector3d defaultOrientation;

    /**
     * Static constructor to set the value of the haveFreqBitsAPI variable.
     * Do it once at startup.
     */
    static {
        Boolean bool = (Boolean)AccessController.doPrivileged (
            new PrivilegedAction() {
                public Object run() {
                    try {
                        Class cls =
                            Class.forName("javax.media.j3d.SceneGraphObject");
                        Package pkg = cls.getPackage();

                        return Boolean.valueOf(pkg.isCompatibleWith("1.3"));
                    } catch(ClassNotFoundException cnfe) {
                        return Boolean.FALSE;
                    }
                }
            }
        );

        haveFreqBitsAPI = bool.booleanValue();

        int vertex_mask = TriangleStripArray.COORDINATES;
        BoxGenerator b_gen = new BoxGenerator(1, 1, 1);
        GeometryData data = new GeometryData();
        data.geometryType = GeometryData.TRIANGLE_STRIPS;

        b_gen.generate(data);

        boxGeometry = new TriangleStripArray(data.vertexCount,
                                             vertex_mask,
                                             data.stripCounts);
        boxGeometry.setCoordinates(0, data.coordinates);

        SphereGenerator s_gen = new SphereGenerator(1);
        data.coordinates = null;
        data.stripCounts = null;
        s_gen.generate(data);

        sphereGeometry = new TriangleStripArray(data.vertexCount,
                                                vertex_mask,
                                                data.stripCounts);
        sphereGeometry.setCoordinates(0, data.coordinates);

        RenderingAttributes r_attr = new RenderingAttributes();
        r_attr.setVisible(false);

        invisibleAppearance = new Appearance();
        invisibleAppearance.setRenderingAttributes(r_attr);

        defaultOrientation = new Vector3d(0, 0, -1);
    }
}
