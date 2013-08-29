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

package org.web3d.vrml.renderer.ogl.nodes.rigidphysics;

// External imports
import org.j3d.aviatrix3d.SceneGraphObject;

// Local imports
import org.web3d.vrml.nodes.VRMLNodeType;

import org.web3d.vrml.renderer.common.nodes.rigidphysics.BaseCollisionSensor;
import org.web3d.vrml.renderer.common.nodes.rigidphysics.BaseContact;
import org.web3d.vrml.renderer.ogl.nodes.OGLVRMLNode;

/**
 * Implementation of a CollisionSensor.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public class OGLCollisionSensor extends BaseCollisionSensor
    implements OGLVRMLNode {

    /**
     * Initialise the time dependent node and it's fields that are held
     * locally.
     */
    public OGLCollisionSensor() {
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not the same type
     */
    public OGLCollisionSensor(VRMLNodeType node) {
        super(node);
    }

    //----------------------------------------------------------
    // Methods defined by OGLVRMLNode
    //----------------------------------------------------------

    /**
     * Get the OpenGL scene graph object representation of this node. This will
     * need to be cast to the appropriate parent type when being used. Default
     * implementation returns null.
     *
     * @return The OpenGL representation.
     */
    public SceneGraphObject getSceneGraphObject() {
        return null;
    }

    //----------------------------------------------------------
    // Methods defined by BaseCollisionSensor
    //----------------------------------------------------------

    /**
     * Generate me a default renderer-specific instance of the Contact node
     * now.
     *
     * @return A new instance of the contact
     */
    protected BaseContact generateNewContact() {
        OGLContact contact = new OGLContact();
        contact.setVersion(vrmlMajorVersion, vrmlMinorVersion, isStatic);
        contact.setFrameStateManager(stateManager);
        contact.setupFinished();

        return contact;
    }
}
