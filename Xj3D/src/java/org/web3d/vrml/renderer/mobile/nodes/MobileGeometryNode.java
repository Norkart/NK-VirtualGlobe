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

package org.web3d.vrml.renderer.mobile.nodes;

// Standard imports
import java.util.ArrayList;

// Application specific imports
import org.web3d.vrml.nodes.VRMLGeometryNodeType;
import org.web3d.vrml.renderer.common.nodes.AbstractNode;
import org.web3d.vrml.renderer.mobile.sg.SceneGraphObject;
import org.web3d.vrml.renderer.mobile.sg.SGManager;

/**
 * An abstract implementation of any form of geometry.
 * <p>
 *
 * This implementation provides a number of the basic necessities when building
 * geometry information. This class does not define any extra fields over the
 * standard base node type.
 *
 * @author Alan Hudson, Justin Couch
 * @version $Revision: 1.1 $
 */
public abstract class MobileGeometryNode extends AbstractNode
    implements MobileGeometryNodeType {

    /**
     * Create a default instance of this class with an empty listener list
     * and the solid field set to true
     *
     * @param name The name of the type of node
     */
    public MobileGeometryNode(String name) {
        super(name);
    }

    //----------------------------------------------------------
    // Methods required by the MobileGeometryNodeType interface.
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
}
