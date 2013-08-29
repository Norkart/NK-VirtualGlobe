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

package org.web3d.vrml.renderer.mobile.nodes.shape;

// Standard imports
import java.util.HashMap;
import javax.vecmath.Vector4f;

// Application specific imports
import org.web3d.vrml.lang.InvalidFieldValueException;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLProtoInstance;

import org.web3d.vrml.renderer.common.nodes.shape.BaseShape;
import org.web3d.vrml.renderer.mobile.nodes.MobileAppearanceNodeType;
import org.web3d.vrml.renderer.mobile.nodes.MobileGeometryNodeType;
import org.web3d.vrml.renderer.mobile.nodes.MobileVRMLNode;

import org.web3d.vrml.renderer.mobile.sg.SceneGraphObject;
import org.web3d.vrml.renderer.mobile.sg.Shape3D;
import org.web3d.vrml.renderer.mobile.sg.Appearance;
import org.web3d.vrml.renderer.mobile.sg.Geometry;
import org.web3d.vrml.renderer.mobile.sg.SGManager;

/**
 * Mobile implementation of a shape node.
 * <p>
 *
 * @author Justin Couch, Alan Hudson
 * @version $Revision: 1.3 $
 */
public class MobileShape extends BaseShape
    implements MobileVRMLNode {

    /** Mobile impl */
    private Shape3D impl;

    /**
     * Construct a new default shape node implementation.
     */
    public MobileShape() {
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not a Shape node, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException Incorrect node type
     */
    public MobileShape(VRMLNodeType node) {
        super(node);
    }

    //----------------------------------------------------------
    // Methods overriding BaseShape.
    //----------------------------------------------------------

    /**
     * Set node content as replacement for <code>appearance</code>.
     *
     * @param newAppearance The new appearance.  null will act like delete
     * @throws InvalidFieldValueException The node does not match the required
     *    type.
     */
    public void setAppearance(VRMLNodeType newAppearance)
        throws InvalidFieldValueException {

        if(!(newAppearance instanceof MobileAppearanceNodeType) &&
           !(newAppearance instanceof VRMLProtoInstance))
            throw new InvalidFieldValueException(APPEARANCE_NODE_MSG);

        super.setAppearance(newAppearance);

        Appearance a = null;

        if(vfAppearance != null) {
            MobileAppearanceNodeType o_a =
                (MobileAppearanceNodeType)vfAppearance;

            a = o_a.getAppearance();
        }

        if (!inSetup)
            impl.setAppearance(a);
    }

    /**
     * Set node content as replacement for <code>geometry</code>.
     *
     * @param newGeomtry The new value for geometry.  null will act like delete
     * @throws InvalidFieldValueException The node does not match the required
     *    type.
     */
    public void setGeometry(VRMLNodeType newGeometry)
        throws InvalidFieldValueException {

        if(!(newGeometry instanceof MobileGeometryNodeType) &&
           !(newGeometry instanceof VRMLProtoInstance))
            throw new InvalidFieldValueException(GEOMETRY_NODE_MSG);

        super.setGeometry(newGeometry);

        Geometry g = null;

        if(vfGeometry != null) {
            MobileGeometryNodeType o_g =
                (MobileGeometryNodeType)vfGeometry;

            g = o_g.getGeometry();
        }

        if (!inSetup)
            impl.setGeometry(g);
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

        impl = new Shape3D();

        // Clear up unneeded memory references
        inSetup = false;

        if(vfAppearance != null) {
            MobileAppearanceNodeType o_a =
                (MobileAppearanceNodeType)vfAppearance;

            Appearance app = o_a.getAppearance();

            impl.setAppearance(app);
        }

        if(vfGeometry != null) {
            MobileGeometryNodeType o_g =
                (MobileGeometryNodeType)vfGeometry;

            // Fetch all of the geometry and build the shape with it.
            Geometry geom = o_g.getGeometry();

            impl.setGeometry(geom);
        }
    }
}
