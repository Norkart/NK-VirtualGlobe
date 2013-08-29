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

// Application specific imports
import org.web3d.vrml.lang.InvalidFieldValueException;

import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLMaterialNodeType;

import org.web3d.vrml.renderer.common.nodes.shape.BaseAppearance;
import org.web3d.vrml.renderer.mobile.nodes.MobileVRMLNode;
import org.web3d.vrml.renderer.mobile.nodes.MobileAppearanceNodeType;
import org.web3d.vrml.renderer.mobile.nodes.MobileMaterialNodeType;

import org.web3d.vrml.renderer.mobile.sg.Appearance;
import org.web3d.vrml.renderer.mobile.sg.Material;
import org.web3d.vrml.renderer.mobile.sg.SceneGraphObject;
import org.web3d.vrml.renderer.mobile.sg.SGManager;

/**
 * Mobile implementation of an Appearance node.
 * <p>
 *
 *
 * @author Alan Hudson
 * @version $Revision: 1.2 $
 */
public class MobileAppearance extends BaseAppearance
    implements MobileAppearanceNodeType {

    /** The OpenGL material node */
    private Appearance appearance;

    /**
     * Empty constructor
     */
    public MobileAppearance() {
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not a Appearance node, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not a Group node
     */
    public MobileAppearance(VRMLNodeType node) {
        super(node);
    }

    //----------------------------------------------------------
    // Methods from VRMLNodeType interface.
    //----------------------------------------------------------

    /**
     * Notification that the construction phase of this node has finished.
     * If the node would like to do any internal processing, such as setting
     * up geometry, then go for it now.
     */
    public void setupFinished() {
        if(!inSetup)
            return;

        super.setupFinished();

        appearance = new Appearance();

        if(vfMaterial != null) {
            MobileMaterialNodeType o_m =
                (MobileMaterialNodeType)vfMaterial;

            Material mat = o_m.getMaterial();
            appearance.setMaterial(mat);
        }

        inSetup = false;
    }

    //----------------------------------------------------------
    // Methods from MobileAppearanceNodeType interface.
    //----------------------------------------------------------

    /**
     * Returns the Appearance node representation used by this object
     *
     * @return The appearance to use in the parent Shape3D
     */
    public Appearance getAppearance() {
        return appearance;
    }

    //----------------------------------------------------------
    // Methods from MobileVRMLNode interface.
    //----------------------------------------------------------

    /**
     * Get the OpenGL scene graph object representation of this node. This will
     * need to be cast to the appropriate parent type when being used. Default
     * implementation returns null.
     *
     * @return The OpenGL representation.
     */
    public SceneGraphObject getSceneGraphObject() {
        return appearance;
    }

}
