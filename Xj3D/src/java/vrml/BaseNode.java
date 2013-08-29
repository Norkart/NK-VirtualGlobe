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
package vrml;

// Standard imports
import vrml.field.*;

// Application specific imports
import org.web3d.vrml.lang.FieldConstants;
import org.web3d.vrml.lang.InvalidFieldException;
import org.web3d.vrml.lang.VRMLFieldDeclaration;
import org.web3d.vrml.nodes.VRMLNodeType;

/**
 * Java VRML97 script binding for the BaseNode class.
 *
 * @author Alan Hudson
 * @version $Revision: 1.7 $
 */
public abstract class BaseNode {

    /** The browser reference for the user */
    protected Browser browser;

    /** The real name of this node */
    protected String nodeName;

    /** The Xj3D implementation node that this is */
    protected VRMLNodeType realNode;

    /**
     * Get the implementation node of this node, if set.
     *
     * @return The node reference or null
     */
    public VRMLNodeType getImplNode() {
        return realNode;
    }

    /**
     * Get the type name of the node. The name is the normal VRML name as you
     * would see it in a UTF8 file. If the node is a prototype, it will return
     * the proto name.
     *
     * @return A string representing the name
     */
    public String getType() {
        return nodeName;
    }

    /**
     * Get a reference to the browser object to allow access to the scene.
     *
     * @return A browser instance for public use
     */
    public Browser getBrowser() {
        return browser;
    }
}
