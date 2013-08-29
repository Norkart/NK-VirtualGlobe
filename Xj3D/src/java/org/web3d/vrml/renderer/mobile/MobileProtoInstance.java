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

package org.web3d.vrml.renderer.mobile;

// Standard imports
// None

// Application specific imports
import org.web3d.vrml.lang.VRMLFieldDeclaration;
import org.web3d.vrml.renderer.CRProtoInstance;
import org.web3d.vrml.renderer.mobile.nodes.MobileVRMLNode;

import org.web3d.vrml.renderer.mobile.sg.Group;
import org.web3d.vrml.renderer.mobile.sg.SceneGraphObject;
import org.web3d.vrml.renderer.mobile.sg.SGManager;

/**
 * A concrete instance of a Prototype in the OpenGL realm.
 *<p>
 *
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
class MobileProtoInstance extends CRProtoInstance
    implements MobileVRMLNode {

    /**
     * The top implementation node of the root node. May be altered
     * depending on whether this is a DEF or not.
     */
    private SceneGraphObject mobileImplNode;

    /**
     * Create an instance for the proto with the number of fields. To set the
     * values of these fields, use the normal setValue methods. The fields are
     * list does not care if it contains null values.
     *
     * @param name The node name of the proto
     * @param vrml97 true if this is a VRML97 issue proto
     * @param The fields that need to be set here
     * @param numBodyNodes The number of nodes in the body of the proto
     */
    MobileProtoInstance(String name,
                     boolean vrml97,
                     VRMLFieldDeclaration[] fields,
                     int numBodyNodes) {

        super(name, vrml97, fields, numBodyNodes);
    }

    //----------------------------------------------------------
    // Methods required by the MobileVRMLNode interface.
    //----------------------------------------------------------

    /**
     * Get the Java3D scene graph object representation of this node. This will
     * need to be cast to the appropriate parent type when being used.
     *
     * @return The Mobile representation.
     */
    public SceneGraphObject getSceneGraphObject() {

        if((mobileImplNode == null) && (bodyNodeCount != 0)) {
            MobileVRMLNode rootNode = (MobileVRMLNode)bodyNodes[0];
            mobileImplNode = rootNode.getSceneGraphObject();
        }

        return mobileImplNode;
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

        // Always set the impl to be the default and only modify it when we
        // have to because DEF doesn't like us
        MobileVRMLNode root = (MobileVRMLNode)rootNode;

        // An Extern proto might not be loaded yet
        if (root != null) {
            mobileImplNode = root.getSceneGraphObject();
            inSetup = false;
        }
    }
}
