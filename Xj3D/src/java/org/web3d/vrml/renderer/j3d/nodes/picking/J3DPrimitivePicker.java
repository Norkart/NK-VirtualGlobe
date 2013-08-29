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

package org.web3d.vrml.renderer.j3d.nodes.picking;

// External imports
import java.util.Map;
import java.util.HashMap;

import javax.media.j3d.*;

// Local imports
import org.web3d.util.ObjectArray;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLProtoInstance;
import org.web3d.vrml.renderer.j3d.nodes.J3DPathAwareNodeType;
import org.web3d.vrml.renderer.j3d.nodes.J3DParentPathRequestHandler;
import org.web3d.vrml.renderer.j3d.nodes.J3DPickingSensorNodeType;
import org.web3d.vrml.renderer.j3d.nodes.J3DPickableTargetNodeType;
import org.web3d.vrml.renderer.common.nodes.picking.BasePrimitivePicker;

/**
 * Java3D -renderer implementation of a PrimitivePicker node.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.4 $
 */
public class J3DPrimitivePicker extends BasePrimitivePicker
    implements J3DPickingSensorNodeType, J3DPathAwareNodeType {

    /** Set used to hold the branchgroups from the target nodes */
    private HashMap targetSet;

    /** This is the current parent path pointer used to construct the path */
    private J3DParentPathRequestHandler parentPathHandler;

    /** A listing of all path handlers registered */
    private ObjectArray allParentPaths;

    // Temp arrays for copying stuff for the scene graph path
    private Object[] tmpPathArray;
    private Node[] tmpNodeArray;

    // Fake impl for get local to vworld
    private SharedGroup impl;

    /**
     * Construct a new time sensor object
     */
    public J3DPrimitivePicker() {
        targetSet = new HashMap();
        allParentPaths = new ObjectArray();
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not the same type
     */
    public J3DPrimitivePicker(VRMLNodeType node) {
        super(node);
        targetSet = new HashMap();
        allParentPaths = new ObjectArray();
    }

    //----------------------------------------------------------
    // Methods from the J3DPathAwareNodeType interface.
    //----------------------------------------------------------

    /**
     * Add a handler for the parent path requesting. If the request is made
     * more than once, extra copies should be added (for example a  DEF and USE
     * of the same node in the same children field of a Group).
     *
     * @param h The new handler to add
     */
    public void addParentPathListener(J3DParentPathRequestHandler h) {
        allParentPaths.add(h);
    }

    /**
     * Remove a handler for the parent path requesting. If there are multiple
     * copies of this handler registered, then the first one should be removed.
     *
     * @param h The new handler to add
     */
    public void removeParentPathListener(J3DParentPathRequestHandler h) {
        allParentPaths.remove(h);
        if(parentPathHandler == h)
            parentPathHandler = null;
    }

    //----------------------------------------------------------
    // Methods defined by J3DPickingSensorNodeType
    //----------------------------------------------------------

    /**
     * A check to see if the parent scene graph path has changed from last
     * time we checked for this node. Assumes that the call is being made on
     * a node that we checked on last frame. If this has been just changed with
     * a new binding call then the caller should just immediately request the
     * current path anyway.
     *
     * @return true if the parent path has changed since last frame
     */
    public boolean hasScenePathChanged() {
        if(parentPathHandler == null)
            return true;
        else
            return parentPathHandler.hasParentPathChanged();
    }

    /**
     * Fetch the scene graph path from the root of the scene to this node.
     * Typically used for the getLocalToVWorld transformation handling. If
     * the node returns null then there is no path to the root of the scene
     * ie this node is somehow orphaned during the last frame.
     *
     * @return The fully qualified path from the root to here or null
     */
    public SceneGraphPath getSceneGraphPath() {
        if(parentPathHandler == null) {
            if(allParentPaths.size() == 0)
                return null;
            else
                parentPathHandler =
                    (J3DParentPathRequestHandler)allParentPaths.get(0);
        }
        ObjectArray path_array = parentPathHandler.getParentPath(this);

        if((path_array == null) || (path_array.size() < 2))
            return null;

        int path_size = path_array.size();

        if((tmpPathArray == null) || tmpPathArray.length < path_size) {
            tmpPathArray = new Object[path_size];
            tmpNodeArray = new Node[path_size - 1];
        }

        path_array.toArray(tmpPathArray);
        Locale locale = (Locale)tmpPathArray[0];

        for(int i = 1; i < path_size; i++) {
            tmpNodeArray[i - 1] = (Node)tmpPathArray[i];
        }

        return new SceneGraphPath(locale,
                                  tmpNodeArray,
                                  impl);
    }


    /**
     * Get the set of target branchgroups that this sensor manages. If there
     * are none, return null.
     *
     * @return A set of J3D nodes or null
     */
    public Map getTargetGroups() {
        return targetSet;
    }

    //----------------------------------------------------------
    // Methods defined by J3DVRMLNode
    //----------------------------------------------------------

    /**
     * Get the Java3D scene graph object representation of this node. This will
     * need to be cast to the appropriate parent type when being used.
     *
     * @return The J3D representation.
     */
    public SceneGraphObject getSceneGraphObject() {
        if (impl == null) {
            impl = new SharedGroup();
            impl.setCapability(Node.ENABLE_PICK_REPORTING);
            impl.setCapability(Node.ALLOW_LOCAL_TO_VWORLD_READ);
            impl.setPickable(true);
        }

        return impl;
    }

    /**
     * Provide the set of mappings that override anything that the loader
     * might set.
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
    }

    /**
     * Set the mapping of capability bits that the user would like to
     * make sure is set. The end output is that the capabilities are the union
     * of what the loader wants and what the user wants.
     * <p>
     * If the map contains a key, but the value is  null or zero length, the
     * request is ignored.
     *
     * @param capBits The capability bits to be set
     * @param freqBits The frequency bits to be set
     */
    public void setCapabilityRequiredMap(Map capBits, Map freqBits) {
    }

    //----------------------------------------------------------
    // Methods defined by BasePickingNode
    //----------------------------------------------------------

    /**
     * Update the child list with the new nodes. This is called after all the
     * basic filtering has been complete and may be overridden by derived
     * classes if needed. The default implementation is empty.
     *
     * @param targets The list of current children
     * @param numValid The number of valid children to check
     */
    protected void updateChildren(VRMLNodeType[] targets, int numValid) {

        targetSet.clear();
        VRMLNodeType node;

        for(int i = 0; i < numValid; i++) {
            if (targets[i] instanceof VRMLProtoInstance) {
                node = ((VRMLProtoInstance)targets[i]).getImplementationNode();

                if (node instanceof VRMLProtoInstance) {
                    System.out.println("PROTO walk not implemented in J3DPrimitivePicker");
                }
                //targetSet.put(((J3DVRMLNode)node).getSceneGraphObject(), targets[i]);
                targetSet.put(((J3DPickableTargetNodeType)node).getPickableGroup(), targets[i]);
            } else {
                node = targets[i];
                J3DPickableTargetNodeType t = (J3DPickableTargetNodeType)node;
                targetSet.put(t.getPickableGroup(), targets[i]);
            }
        }
    }
}
