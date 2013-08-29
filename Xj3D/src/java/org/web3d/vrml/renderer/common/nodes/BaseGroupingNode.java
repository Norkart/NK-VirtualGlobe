/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2007
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.common.nodes;

// External imports
import java.util.HashMap;
import java.util.ArrayList;

// Local imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;

import org.web3d.vrml.util.FieldValidator;

/**
 * Common basic implementation of any node that uses grouping functionality.
 * <p>
 *
 * The basic (X3D) definition of X3DGroupingNode is:
 * <pre>
 *  X3DGroupingNode : X3DChildNode, X3DBoundedObject {
 *    MFNode  [in]     addChildren
 *    MFNode  [in]     removeChildren
 *    MFNode  [in,out] children       []       [X3DChildNode]
 *    SFNode  [in,out] metadata       NULL     [X3DMetadataObject]
 *    SFVec3f []       bboxCenter     0 0 0    (-8,8)
 *    SFVec3f []       bboxSize       -1 -1 -1 [0,8) or -1 -1 -1
 *  }
 * </pre>
 *
 * @author Justin Couch
 * @version $Revision: 1.28 $
 */
public abstract class BaseGroupingNode extends AbstractNode
    implements VRMLGroupingNodeType, VRMLBoundedNodeType {

    /** Index of the children field */
    protected static final int FIELD_CHILDREN = LAST_NODE_INDEX + 1;

    /** Index of the addChildren field */
    protected static final int FIELD_ADDCHILDREN = LAST_NODE_INDEX + 2;

    /** Index of the removeChildren field */
    protected static final int FIELD_REMOVECHILDREN = LAST_NODE_INDEX + 3;

    /** Index of the Bounding box center bboxCenter field */
    protected static final int FIELD_BBOX_CENTER = LAST_NODE_INDEX + 4;

    /** Index of the Bounding box size bboxSize field */
    protected static final int FIELD_BBOX_SIZE = LAST_NODE_INDEX + 5;

    /** The last field index used by this class */
    protected static final int LAST_GROUP_INDEX = FIELD_BBOX_SIZE;

    /** Message when the USE is a bindable */
    protected static final String USE_BIND_MSG =
        "New node contains bindables when this grouping node is already " +
        "USEd. Ignoring the request";

    // VRML Field declarations

    /** MFNode children NULL */
    protected ArrayList vfChildren;

    /** SFVec3f bboxCenter NULL */
    protected float[] vfBboxCenter;

    /** SFVec3f bboxSize NULL */
    protected float[] vfBboxSize;

    /**
     * The number of children nodes we added, not the total. We don't
     * Add nodes to NR that have no scenegraph object (such as
     * interpolators and timesensors). This count is so that we don't have
     * to enable the ALLOW_CHILDREN_READ on the group (an optimisation step)
     */
    protected int childCount;

    /** Flag indicating if this node contains bindable/activatable nodes */
    protected boolean hasBindables;

    /** Counter for the number of sharing references this has */
    protected int shareCount;

    /** Internal scratch var for dealing with added/removed children */
    private VRMLNodeType[] nodeTmp;

    /**
     * Construct a default instance of the grouping node type.
     *
     * @param name The name of the type of node
     */
    protected BaseGroupingNode(String name) {
        super(name);

        childCount = 0;
        hasBindables = false;
        shareCount = 0;

        vfBboxSize = new float[] {-1, -1, -1};
        vfBboxCenter = new float[] {0, 0, 0};
        vfChildren = new ArrayList();
    }

    /**
     * Set the fields of the grouping node that has the fields set
     * based on the fields of the passed in node. This will not copy any
     * children nodes, only the local fields.
     *
     * @param node The grouping node to copy info from
     */
    protected void copy(VRMLGroupingNodeType node) {
        float[] field = node.getBboxCenter();

        vfBboxCenter[0] = field[0];
        vfBboxCenter[1] = field[1];
        vfBboxCenter[2] = field[2];

        field = node.getBboxSize();

        vfBboxSize[0] = field[0];
        vfBboxSize[1] = field[1];
        vfBboxSize[2] = field[2];
    }

    //-------------------------------------------------------------
    // Methods defined by VRMLBoundedNodeType
    //-------------------------------------------------------------

    /**
     * Accessor method to get current value of field <b>bboxCenter</b>
     * default value is <code>0 0 0</code>.
     *
     * @return Value of bboxCenter(SFVec3f)
     */
    public float[] getBboxCenter () {
        return vfBboxCenter;
    }

    /**
     * Accessor method to get current value of field <b>bboxSize</b>
     * default value is <code>-1 -1 -1</code>.
     *
     * @return The size of the bounding box(SFVec3f)
     */
    public float[] getBboxSize () {
        return vfBboxSize;
    }

    //-------------------------------------------------------------
    // Methods defined by VRMLGroupingNodeType
    //-------------------------------------------------------------

    /**
     * Get the children, provides a live reference not a copy
     *
     * @return An array of VRMLNodeTypes
     */
    public VRMLNodeType[] getChildren() {
        VRMLNodeType kids[] = new VRMLNodeType[vfChildren.size()];

        vfChildren.toArray(kids);

        return kids;
    }

    /**
     * Accesor method to set the children field
     * If passed null this method will act like removeChildren
     *
     * @param newChildren Array of new children
     */
    public void setChildren(VRMLNodeType[] newChildren) {
        clearChildren();

        if(newChildren == null)
            return;

        childCount = 0;

        for(int i = 0; i < newChildren.length; i++)
            addChildNode(newChildren[i]);

        hasChanged[FIELD_CHILDREN] = true;
        fireFieldChanged(FIELD_CHILDREN);
    }

    /**
     * Accessor method to set the children field
     * Creates an array containing only newChild
     * If passed null this method will act like removeChildren
     *
     * @param newChild The new child
     */
    public void setChildren(VRMLNodeType newChild) {
        clearChildren();

        childCount = 0;

        if(newChild != null)
            addChildNode(newChild);

        hasChanged[FIELD_CHILDREN] = true;
        fireFieldChanged(FIELD_CHILDREN);
    }

    /**
     * Append a new child node to the existing collection. Should be used
     * sparingly. It is really only provided for Proto handling purposes.
     *
     * @param newChild The new child
     */
    public void addChild(VRMLNodeType newChild) {
        if(newChild != null)
            addChildNode(newChild);

        hasChanged[FIELD_CHILDREN] = true;
        fireFieldChanged(FIELD_CHILDREN);
    }

    /**
     * Returns the number of children
     *
     * @return The number of children
     */
    public int getChildrenSize() {
        return vfChildren.size();
    }

    /**
     * A check to see if this grouping node contains any bindable nodes. This
     * does a dynamic check of all the children <i>now</i> to see if any of
     * them are bindable.
     *
     * @return true if this or any of its children contain bindable nodes
     */
    public boolean containsBindableNodes() {
        return hasBindables;
    }

    /**
     * Check to see if this node has been used more than once. If it has then
     * return true.
     *
     * @return true if this node is shared
     */
    public boolean isShared() {
        return (shareCount > 1);
    }

    /**
     * Adjust the sharing count up or down one increment depending on the flag.
     *
     * @param used true if this is about to have another reference added
     */
    public void setShared(boolean used) {

        if(used)
            shareCount++;
        else
            shareCount--;

        Object kid;
        int num_kids = vfChildren.size();

        for(int i = 0; i < num_kids; i++) {
            kid = vfChildren.get(i);

            if(kid instanceof VRMLGroupingNodeType)
                ((VRMLGroupingNodeType)kid).setShared(used);
        }
    }

    /**
     * Change the reference count up or down by one for a given layer ID.
     * If there is no reference to the given layer ID previously, add one
     * now. A listing of the layer IDs that reference this node can be
     * retrieved through {@link #getLayerIds()}.
     *
     * @param layer The id of the layer to modify the ref count on
     * @param add true to increment the reference count, false to decrement
     */
    public void updateRefCount(int layer, boolean add) {
        super.updateRefCount(layer, add);

        if(layerIds == null)
            return;

        int num_kids = vfChildren.size();

        for(int i = 0; i < num_kids; i++) {
            Object kid = vfChildren.get(i);

            if(kid != null)
                updateRefs((VRMLNodeType)kid, add);
        }
    }

    //----------------------------------------------------------
    // Methods defined by VRMLNode
    //----------------------------------------------------------

    /**
     * Notify this node that is has been DEFd.
     *
     * @throws IllegalStateException The setup is finished.
     */
    public void setDEF() {
        isDEF = true;
    }

    /**
     * Notification that the construction phase of this node has finished. This
     * will call setupFinished() on the child nodes.
     *
     * Derived classes that do not like this behaviour should override this
     * method or ensure that the implGroup has a parent before this method
     * is called.
     */
    public void setupFinished() {
        if(!inSetup)
            return;

        super.setupFinished();

        int num_kids = vfChildren.size();
        VRMLNodeType kid;

        for(int i = 0; i < num_kids; i++) {
            kid = (VRMLNodeType)vfChildren.get(i);

            // Make sure the child is finished first.
            kid.setupFinished();
        }
    }

    /**
     * Get the primary type of this node.  Replaces the instanceof mechanism
     * for use in switch statements.
     *
     * @return The primary type
     */
    public int getPrimaryType() {
        return TypeConstants.GroupingNodeType;
    }

    //----------------------------------------------------------
    // Methods defined by VRMLNodeType
    //----------------------------------------------------------

    /**
     * Get the value of a field. If the field is a primitive type, it will
     * return a class representing the value. For arrays or nodes it will
     * return the instance directly.
     *
     * @param index The index of the field to change.
     * @return The class representing the field value
     * @throws InvalidFieldException The field index is not known
     */
    public VRMLFieldData getFieldValue(int index) throws InvalidFieldException {
        VRMLFieldData fieldData = fieldLocalData.get();

        switch(index) {
            case FIELD_CHILDREN:
                int num_kids = vfChildren.size();

                if((nodeTmp == null) || (nodeTmp.length < num_kids))
                    nodeTmp = new VRMLNodeType[num_kids];
                vfChildren.toArray(nodeTmp);
                fieldData.clear();
                fieldData.nodeArrayValue = nodeTmp;
                fieldData.dataType = VRMLFieldData.NODE_ARRAY_DATA;
                fieldData.numElements = num_kids;
                break;

            case FIELD_BBOX_SIZE:
                fieldData.clear();
                fieldData.floatArrayValue = vfBboxSize;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = 1;
                break;

            case FIELD_BBOX_CENTER:
                fieldData.clear();
                fieldData.floatArrayValue = vfBboxCenter;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = 1;
                break;

            default:
                super.getFieldValue(index);
        }

        return fieldData;
    }

    /**
     * Send a routed value from this node to the given destination node. The
     * route should use the appropriate setValue() method of the destination
     * node. It should not attempt to cast the node up to a higher level.
     * Routing should also follow the standard rules for the loop breaking and
     * other appropriate rules for the specification.
     *
     * @param time The time that this route occurred (not necessarily epoch
     *   time. Should be treated as a relative value only)
     * @param srcIndex The index of the field in this node that the value
     *   should be sent from
     * @param destNode The node reference that we will be sending the value to
     * @param destIndex The index of the field in the destination node that
     *   the value should be sent to.
     */
    public void sendRoute(double time,
                          int srcIndex,
                          VRMLNodeType destNode,
                          int destIndex) {

        // Simple impl for now.  ignores time and looping

        try {
            switch(srcIndex) {
                case FIELD_CHILDREN:
                    int num_kids = vfChildren.size();

                    if((nodeTmp == null) || (nodeTmp.length < num_kids))
                        nodeTmp = new VRMLNodeType[num_kids];
                    vfChildren.toArray(nodeTmp);

                    destNode.setValue(destIndex, nodeTmp, num_kids);
                    break;

                default:
                    super.sendRoute(time, srcIndex, destNode, destIndex);
            }
        } catch(InvalidFieldException ife) {
            System.err.println("sendRoute: No field!" + ife.getFieldName());
        } catch(InvalidFieldValueException ifve) {
            System.err.println("sendRoute: Invalid field value: " +
                ifve.getMessage());
        }
    }

    /**
     * Set the value of the field at the given index as an array of floats.
     * This would be used to set MFFloat, SFVec2f, SFVec3f and SFRotation
     * field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @param numValid The number of valid values to copy from the array
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
     */
    public void setValue(int index, float[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException,
               InvalidFieldAccessException {

        switch(index) {
            case FIELD_BBOX_CENTER:
                setBboxCenter(value);
                break;

            case FIELD_BBOX_SIZE:
                setBboxSize(value);
                break;

            default:
               super.setValue(index, value, numValid);
        }
    }

    /**
     * Set the value of the field at the given index as an array of nodes.
     * This would be used to set MFNode field types.
     *
     * @param index The index of destination field to set
     * @param child The new value to use for the node
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
     * @throws InvalidFieldAccessException The call is attempting to write to
     *    a field that does not permit writing now
     */
    public void setValue(int index, VRMLNodeType child)
        throws InvalidFieldException, InvalidFieldValueException,
               InvalidFieldAccessException {

        boolean notif = false;

        switch(index) {
            case FIELD_CHILDREN:
                if(!inSetup)
                    clearChildren();

                if(child != null)
                    addChildNode(child);

                notif = true;
                break;

            case FIELD_ADDCHILDREN:
                if(inSetup)
                    throw new InvalidFieldAccessException(
                          "Cannot set an inputOnly field in a file: addChildren");

                if(child != null)
                    addChildNode(child);

                notif = true;
                break;

            case FIELD_REMOVECHILDREN:
                if(inSetup)
                    throw new InvalidFieldAccessException(
                          "Cannot set an inputOnly field in a file: removeChildren");

                if(child != null)
                    removeChildNode(child);

                notif = true;
                break;

            default:
                super.setValue(index, child);
        }

        if(!inSetup && notif) {
            // Send off events for the children field as that is what has
            // really been modified by the calls here. add/removeChildren
            // are eventIns, not the real value.
            hasChanged[FIELD_CHILDREN] = true;
            fireFieldChanged(FIELD_CHILDREN);
        }
    }

    /**
     * Set the value of the field at the given index as an array of nodes.
     * This would be used to set MFNode field types.
     *
     * @param index The index of destination field to set
     * @param children The new value to use for the node
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
     * @throws InvalidFieldAccessException The call is attempting to write to
     *    a field that does not permit writing now
     */
    public void setValue(int index, VRMLNodeType[] children, int numValid)
        throws InvalidFieldException, InvalidFieldValueException,
               InvalidFieldAccessException {

        switch(index) {
            case FIELD_CHILDREN:
                if(!inSetup)
                    clearChildren();

                for(int i = 0; i < numValid; i++ )
                    addChildNode(children[i]);

                break;

            case FIELD_ADDCHILDREN:
                if(inSetup)
                    throw new InvalidFieldAccessException(
                          "Cannot set an inputOnly field in a file: addChildren");

                for(int i = 0; i < numValid; i++ )
                    addChildNode(children[i]);

                break;

            case FIELD_REMOVECHILDREN:
                if(inSetup)
                    throw new InvalidFieldAccessException(
                          "Cannot set an inputOnly field in a file: removehildren");

                for(int i = 0; i < numValid; i++ )
                    removeChildNode(children[i]);

                break;

            default:
                super.setValue(index, children, numValid);
        }

        if(!inSetup) {
            // Send off events for the children field as that is what has
            // really been modified by the calls here. add/removeChildren
            // are eventIns, not the real value.
            hasChanged[FIELD_CHILDREN] = true;
            fireFieldChanged(FIELD_CHILDREN);
        }
    }

    //----------------------------------------------------------
    // Local methods
    //----------------------------------------------------------

    /**
     * Set the bounding box for this node. Only used by the field parser
     * at setup.
     *
     * @param val The box center to set
     */
    private void setBboxCenter(float[] val) {
        if(!inSetup)
            throw new InvalidFieldAccessException(INIT_ONLY_WRITE_MSG + " bboxCenter");

        vfBboxCenter[0] = val[0];
        vfBboxCenter[1] = val[1];
        vfBboxCenter[2] = val[2];
    }

    /**
     * Set the bounding box for this node. Only used by the field parser
     * at setup.
     *
     * @param val The box size to set
     * @throws InvalidFieldValueException The bounds is not valid
     */
    private void setBboxSize(float[] val) throws InvalidFieldValueException {
        if(!inSetup)
            throw new InvalidFieldAccessException(INIT_ONLY_WRITE_MSG + " bboxSize");

        FieldValidator.checkBBoxSize(getVRMLNodeName(),val);

        vfBboxSize[0] = val[0];
        vfBboxSize[1] = val[1];
        vfBboxSize[2] = val[2];
    }

    /**
     * Clear the child node list of all children in the VRML node. Override
     * to provide.renderer-specific behaviour, but remember to also call this
     * implementation too.
     */
    protected void clearChildren() {
        int num_kids = vfChildren.size();

        if((nodeTmp == null) || (nodeTmp.length < num_kids))
            nodeTmp = new VRMLNodeType[num_kids];

        vfChildren.toArray(nodeTmp);

        for(int i = 0; i < num_kids; i++) {
            if(nodeTmp[i] instanceof VRMLGroupingNodeType)
                ((VRMLGroupingNodeType)nodeTmp[i]).setShared(false);

            updateRefs(nodeTmp[i], false);
        }

        if (num_kids > 0)
            stateManager.registerRemovedNodes(nodeTmp);

        childCount = 0;
        vfChildren.clear();
        hasBindables = false;
    }

    /**
     * Add a single child node to the list of available children. Override
     * to provide.renderer-specific behaviour, but remember to also call this
     * implementation too.
     *
     * @param node The node to add
     * @throws InvalidFieldValueException This is a bindable node shared
     */
    protected void addChildNode(VRMLNodeType node)
        throws InvalidFieldValueException {

        boolean new_bindable =
            ((node instanceof VRMLBindableNodeType) ||
             ((node instanceof VRMLGroupingNodeType) &&
              ((VRMLGroupingNodeType)node).containsBindableNodes()));

        if((shareCount > 1)  && new_bindable)
            throw new InvalidFieldValueException(USE_BIND_MSG);

        if(node instanceof VRMLGroupingNodeType) {
            ((VRMLGroupingNodeType)node).setShared(true);
        } else if(node instanceof VRMLProtoInstance) {
            VRMLNodeType impl =
                ((VRMLProtoInstance)node).getImplementationNode();

            while((impl != null) && (impl instanceof VRMLProtoInstance))
                impl = ((VRMLProtoInstance)impl).getImplementationNode();

            if(impl instanceof VRMLGroupingNodeType)
                ((VRMLGroupingNodeType)impl).setShared(true);
        }

        if(new_bindable)
            hasBindables = true;

        childCount++;
        vfChildren.add(node);
        updateRefs(node, true);

        if(!inSetup && (node.getLayerIds( ) != null))
            stateManager.registerAddedNode(node);
    }

    /**
     * Remove the given node from this grouping node. If the node is not a
     * child of this node, the request is silently ignored.
     *
     * @param node The node to remove
     */
    protected void removeChildNode(VRMLNodeType node) {
        if(node instanceof VRMLGroupingNodeType) {
            ((VRMLGroupingNodeType)node).setShared(false);
        } else if(node instanceof VRMLProtoInstance) {
            VRMLNodeType impl =
                ((VRMLProtoInstance)node).getImplementationNode();

            while((impl != null) && (impl instanceof VRMLProtoInstance))
                impl = ((VRMLProtoInstance)impl).getImplementationNode();

            if(impl instanceof VRMLGroupingNodeType)
                ((VRMLGroupingNodeType)impl).setShared(false);
        }

        childCount--;
        updateRefs(node, false);

        // TODO: This looks dodgy
        if(!inSetup)
            vfChildren.remove(node);

        if(!inSetup)
            stateManager.registerRemovedNode(node);
    }
}
