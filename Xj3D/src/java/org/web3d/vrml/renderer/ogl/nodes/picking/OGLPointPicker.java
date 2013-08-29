/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.ogl.nodes.picking;

// External imports
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

import org.j3d.aviatrix3d.Group;
import org.j3d.aviatrix3d.SceneGraphObject;
import org.j3d.aviatrix3d.picking.PickableObject;

// Local imports
import org.web3d.util.ObjectArray;
import org.web3d.vrml.lang.InvalidFieldValueException;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLProtoInstance;
import org.web3d.vrml.renderer.ogl.nodes.OGLPickableTargetNodeType;
import org.web3d.vrml.renderer.ogl.nodes.OGLPickingSensorNodeType;
import org.web3d.vrml.renderer.ogl.nodes.OGLPickingFlagConvertor;
import org.web3d.vrml.renderer.common.nodes.picking.BasePointPicker;

/**
 * OpenGL renderer implementation of a PointPicker node.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
public class OGLPointPicker extends BasePointPicker
    implements OGLPickingSensorNodeType {

    /** The node does not implement the OGLPickingTargetNode type */
    private static final String TARGET_PROTO_MSG =
        "The node does not implement the OGLPickableTargetNodeType";

    /** Set used to hold the branchgroups from the target nodes */
    private HashMap targetSet;

    /** Used to map flags to types */
    private OGLPickingFlagConvertor typeConvertor;

    /** The converted bitmask of sensor masks */
    private int pickMask;

    /** The targets to use for picking */
    private PickableObject[] pickTargets;

    /** A local parent for working out where we are in space */
    private Group groupParent;

    /**
     * Construct a new time sensor object
     */
    public OGLPointPicker() {
        targetSet = new HashMap();
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not the same type
     */
    public OGLPointPicker(VRMLNodeType node) {
        super(node);
        targetSet = new HashMap();
    }

    //----------------------------------------------------------
    // Methods defined by OGLPickingSensorNodeType
    //----------------------------------------------------------

    /**
     * Set a parent to this sensor. Really doesn't matter if one overwrites
     * another. The parent should only be needed to work out where the sensor
     * is in world space coordinates, since the sensor itself never has an
     * underlying scene graph object representation. In additoin, if a picking
     * sensor has more than one parent in the transformation heirarchy, the
     * results are entirely bogus anyway.
     *
     * @param group The parent group of this sensor
     */
    public void setParentGroup(Group group) {
        groupParent = group;
    }

    /**
     * Fetch the parent grouping node from this sensor so that we can track
     * back up the stack for the world transformation.
     *
     * @return The currently set parent transform
     */
    public Group getParentGroup() {
        return groupParent;
    }

    /**
     * Get the set of target PickableObjects that this sensor manages. If there
     * are none, return an empty set.
     *
     * @return A set of OGL nodes mapped to their VRML wrapper
     */
    public Map getTargetMapping() {
        return targetSet;
    }

    /**
     * Get the collection of ra  target PickableObjects that this sensor manages.
     *
     * @return A set of OGL nodes for pick testing
     */
    public PickableObject[] getTargetObjects() {
        return pickTargets;
    }

    /**
     * Set the flag convertor that will be used to map the object type strings
     * to the internal pick masks. A value of null will clear the current
     * instance.
     *
     * @param conv The convertor instance to use, or null
     */
    public void setTypeConvertor(OGLPickingFlagConvertor conv) {
        // If the existing convertor is null, we need to regenerate the
        // mask value.
        if(typeConvertor == null && conv != null) {
        	pickMask = 0;
            for(int i = 0; i < vfObjectType.length; i++)
                pickMask |= conv.addObjectType(vfObjectType[i]);
        }

        typeConvertor = conv;
    }

    /**
     * Get the int mask used to perform picking with. This is the mask
     * generated after passing the collection of pick string flags through the
     * picking flag convertor.
     *
     * @return The pick bitmask to use
     */
    public int getPickMask() {
        return pickMask;
    }

    //----------------------------------------------------------
    // Methods defined by VRMLPickingSensorNodeType
    //----------------------------------------------------------

    /**
     * Set the list of picking targets that this object corresponds to.
     * These can be an array of strings.
     *
     * @param types The list of object type strings to use
     * @param numValid The number of valid values to read from the array
     */
    public void setObjectType(String[] types, int numValid) {

        // remove the old types first
        if(typeConvertor != null) {
            for(int i = 0; i < vfObjectType.length; i++)
                typeConvertor.removeObjectType(vfObjectType[i]);
        }

        pickMask = 0;

        super.setObjectType(types, numValid);

        if(typeConvertor != null) {
            for(int i = 0; i < vfObjectType.length; i++)
                pickMask |= typeConvertor.addObjectType(vfObjectType[i]);
        }
    }

    //----------------------------------------------------------
    // Methods defined by OGLVRMLNode
    //----------------------------------------------------------

    /**
     * Get the Java3D scene graph object representation of this node. This will
     * need to be cast to the appropriate parent type when being used.
     *
     * @return The OGL representation.
     */
    public SceneGraphObject getSceneGraphObject() {
        return null;
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
            if(targets[i] instanceof VRMLProtoInstance) {
                node = ((VRMLProtoInstance)targets[i]).getImplementationNode();

                while((node != null) && (node instanceof VRMLProtoInstance))
                    node = ((VRMLProtoInstance)node).getImplementationNode();

                if((node != null) && !(node instanceof OGLPickableTargetNodeType))
                    throw new InvalidFieldValueException(TARGET_PROTO_MSG);

                targetSet.put(((OGLPickableTargetNodeType)node).getPickableObject(),
                              targets[i]);
            } else if(targets[i] instanceof OGLPickableTargetNodeType) {
                node = targets[i];
                OGLPickableTargetNodeType t = (OGLPickableTargetNodeType)node;
                targetSet.put(t.getPickableObject(), targets[i]);
            } else
                throw new InvalidFieldValueException(TARGET_PROTO_MSG);
        }

        Set keys = targetSet.keySet();
        if(pickTargets == null || pickTargets.length != keys.size())
            pickTargets = new PickableObject[keys.size()];

        keys.toArray(pickTargets);
    }
}
