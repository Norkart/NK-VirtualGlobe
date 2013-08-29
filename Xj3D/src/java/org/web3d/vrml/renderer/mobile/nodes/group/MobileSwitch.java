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

package org.web3d.vrml.renderer.mobile.nodes.group;

// Standard imports
import java.util.ArrayList;
import java.util.HashMap;

// Application specific imports
import org.web3d.vrml.lang.InvalidFieldValueException;

import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLPointingDeviceSensorNodeType;
import org.web3d.vrml.renderer.mobile.nodes.MobileVRMLNode;
import org.web3d.vrml.renderer.common.nodes.group.BaseSwitch;

import org.web3d.vrml.renderer.mobile.sg.Group;
import org.web3d.vrml.renderer.mobile.sg.Node;
import org.web3d.vrml.renderer.mobile.sg.SceneGraphObject;
import org.web3d.vrml.renderer.mobile.sg.SGManager;


/**
 * OpenGL version of a Switch node.
 * <p>
 *
 * The implementation works by doing the shuffling itself of the
 * internal scene graph structures.
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public class MobileSwitch extends BaseSwitch
    implements MobileVRMLNode {

    /** The renderable scenegraph node */
    private Group implGroup;

    /** Mapping of the VRMLNodeType to the Mobile Group instance */
    private HashMap oglChildMap;

    /** Array of the basic Mobile nodes that get used - in order */
    private ArrayList oglChildList;

    /**
     * Construct a default instance of this node. The defaults are set by the
     * VRML specification.
     */
    public MobileSwitch() {
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not a group node, an exception will be
     * thrown. It does not copy the children nodes, just this node.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not a Group node
     */
    public MobileSwitch(VRMLNodeType node) {
        super(node);
    }

    //----------------------------------------------------------
    // Methods required by the VRMLNodeType interface.
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

        // Free up unneeded vars
        inSetup = false;

        oglChildMap = new HashMap();
        oglChildList = new ArrayList();

        implGroup = new Group();

        for(int i = 0; i < childCount; i++) {
            MobileVRMLNode node = (MobileVRMLNode)vfChildren.get(i);

            Node ogl_node = (Node)node.getSceneGraphObject();
            implGroup.addChild(ogl_node);

            oglChildList.add(ogl_node);
            oglChildMap.put(node, ogl_node);
        }
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
        return implGroup;
    }

    //----------------------------------------------------------
    // Internal methods required by the class
    //----------------------------------------------------------

    /**
     * Set which child to display.  -1 = NONE.
     *
     * @param newChoice The new child to display
     * @throws InvalidFieldValueException
     */
    protected void setWhichChoice(int newChoice)
        throws InvalidFieldValueException {

        super.setWhichChoice(newChoice);

        implGroup.removeAllChildren();

        if(vfWhichChoice != -1) {
            Node n = (Node)oglChildList.get(vfWhichChoice);

            if(n != null)
                implGroup.addChild(n);
        }
    }

    /**
     * Clear the child node list of all children - both VRML and OpenGL.
     */
    protected void clearChildren() {

        implGroup.removeAllChildren();

        super.clearChildren();
    }

    /**
     * Add a single child node to the list of available children. This auto
     * matically deals with DEF/USE and adds links and branchgroups where
     * appropriate. When nodes are null, we do not add them to the GL
     * representation, only to the vfChildren list.
     *
     * @param node The node to view
     * @throws InvalidFieldValueException This is a bindable node shared
     */
    protected void addChildNode(VRMLNodeType node)
        throws InvalidFieldValueException {

        super.addChildNode(node);

        MobileVRMLNode n = (MobileVRMLNode)node;

        if(!inSetup) {
            Node ogl_node = (Node)n.getSceneGraphObject();
            implGroup.addChild(ogl_node);

            oglChildMap.put(node, ogl_node);
        }
    }

    /**
     * Remove the given node from this grouping node. If the node is not a
     * child of this node, the request is silently ignored.
     *
     * @param node The node to remove
     */
    protected void removeChildNode(VRMLNodeType node) {
        if(!oglChildMap.containsKey(node))
            return;

        if(!inSetup) {
            Node ogl_node = (Node)oglChildMap.get(node);

            implGroup.removeChild(ogl_node);
            oglChildMap.remove(node);
        }

        super.removeChildNode(node);
    }
}
