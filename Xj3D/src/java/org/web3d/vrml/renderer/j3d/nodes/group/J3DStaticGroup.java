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

package org.web3d.vrml.renderer.j3d.nodes.group;

// Standard imports
import javax.media.j3d.*;

import java.util.HashMap;
import java.util.Map;

// Application specific imports
import org.web3d.vrml.lang.*;

import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLGroupingNodeType;
import org.web3d.vrml.renderer.common.nodes.group.BaseStaticGroup;
import org.web3d.vrml.renderer.j3d.nodes.J3DVRMLNode;

/**
 * Java3D implementation of a static group node.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.6 $
 */
public class J3DStaticGroup extends BaseStaticGroup
    implements J3DVRMLNode {

    /** The Java3D group node that we are using to place children in. */
    private BranchGroup implGroup;

    /**
     * Construct a new default instance.
     */
    public J3DStaticGroup() {
        implGroup = new BranchGroup();
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not a group node, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not a Group node
     */
    public J3DStaticGroup(VRMLNodeType node) {
        super(node);
    }

    //----------------------------------------------------------
    // Methods overriding VRMLNode class.
    //----------------------------------------------------------

    /**
     * Set the version of VRML that this node should represent. Different
     * versions have different capabilities, even within the same node.
     *
     * @param major The major version number of this scene
     * @param minor The minor version number of this scene
     * @param isStatic true if this node is under a static group and won't
     *    change after the setup is finished
     */
    public void setVersion(int major, int minor, boolean isStatic) {
        super.setVersion(major, minor, isStatic);

        if(isStatic)
            return;

        implGroup.setCapability(BranchGroup.ALLOW_DETACH);
    }


    /**
     * Get the Java3D scene graph object representation of this node. This will
     * need to be cast to the appropriate parent type when being used. This
     * should never be called before setupFinished() is called.
     *
     * @return The J3D representation of this grouping node
     */
    public SceneGraphObject getSceneGraphObject() {

        if(inSetup)
            throw new RuntimeException();

        return implGroup;
    }

    /**
     * Provide the set of mappings that override anything that the loader
     * might set. Default implementation does nothing.
     * <p>
     *
     * If the key is set, but the value is null or zero length, then all
     * capabilities on that node will be disabled. If the key is set the
     * values override all settings that the loader may wish to normally
     * make. This can be very dangerous if the loader is used for a file
     * format that includes its own internal animation engine, so be very
     * careful with this request.
     *
     * @param capBits The capability bits to be set
     * @param freqBits The frequency bits to be set
     */
    public void setCapabilityOverrideMap(Map capBits, Map freqBits) {
        // Not implemented yet
    }


    /**
     * Set the mapping of capability bits that the user would like to
     * make sure is set. The end output is that the capabilities are the union
     * of what the loader wants and what the user wants. Default implementation
     * does nothing.
     * <p>
     * If the map contains a key, but the value is  null or zero length, the
     * request is ignored.
     *
     * @param capBits The capability bits to be set
     * @param freqBits The frequency bits to be set
     */
    public void setCapabilityRequiredMap(Map capBits, Map freqBits) {
        // Not implemented yet
    }

    //----------------------------------------------------------
    // Methods required by VRMLNodeType
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

        implGroup.setCapability(Node.ENABLE_PICK_REPORTING);
        implGroup.setPickable(true);

        int num_kids = vfChildren.size();
        J3DVRMLNode kid;

        for(int i = 0; i < num_kids; i++) {
            kid = (J3DVRMLNode)vfChildren.get(i);

            // Make sure the child is finished first.
            Node j3d_node = (Node)kid.getSceneGraphObject();

            if(j3d_node == null)
                continue;

            // In the static case, it if is not a SharedGroup, we don't
            // need to do anything with it at all and just add it directly
            // to the grouping node parent.
            if(j3d_node instanceof SharedGroup) {
                j3d_node = new Link((SharedGroup)j3d_node);
            }

            implGroup.addChild(j3d_node);
        }

        implGroup.compile();
    }
}
