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

package org.web3d.vrml.renderer.common.nodes.rigidphysics;

// External imports
import org.odejava.*;

import java.util.ArrayList;
import java.util.HashMap;

// Local imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;

import org.web3d.vrml.renderer.common.nodes.AbstractNode;
import org.web3d.vrml.util.FieldValidator;

/**
 * Implementation of the CollidableSpace node.
 * <p>
 *
 *
 * The X3D definition of CollisionSpace is:
 * <pre>
 * CollisionSpace : X3DNode {
 *   MFNode  [in,out] collidables NULL      [X3DNBodyCollisionSpaceNode, X3DNBodyCollidableNode]
 *   SFBool  [in,out] enabled     TRUE
 *   SFNode  [in,out] metadata    NULL      [X3DMetadataObject]
 *   SFBool  [in,out] useGeometry FALSE
 *   SFVec3f []       bboxCenter  0 0 0     (-&#8734;,&#8734;)
 *   SFVec3f []       bboxSize    -1 -1 -1  [0,&#8734;) or -1 -1 -1
 * }
 * </pre>
 *
 * @author Justin Couch
 * @version $Revision: 1.5 $
 */
public abstract class BaseCollisionSpace extends AbstractNode
    implements VRMLNBodySpaceNodeType {

    // Field index constants

    /** The field index for bboxSize */
    protected static final int FIELD_BBOX_SIZE = LAST_NODE_INDEX + 1;

    /** The field index for bboxCenter */
    protected static final int FIELD_BBOX_CENTER = LAST_NODE_INDEX + 2;

    /** The field index for collidables */
    protected static final int FIELD_COLLIDABLES = LAST_NODE_INDEX + 3;

    /** The field index for useGeometry */
    protected static final int FIELD_USE_GEOMETRY = LAST_NODE_INDEX + 4;

    /** The field index for enabled */
    protected static final int FIELD_ENABLED = LAST_NODE_INDEX + 5;

    /** Last index used by this base node */
    protected static final int LAST_SPACE_INDEX = FIELD_ENABLED;

    /** Number of fields in this node */
    private static final int NUM_FIELDS = LAST_SPACE_INDEX + 1;

    /** Default depth of the quadtree used to represent a space */
    private static final int QUADTREE_DEPTH = 5;

    /** Message for when the node in setValue() is not a primitive */
    protected static final String COLLIDABLE_PROTO_MSG =
        "Collidables field proto value does not describe a CollisionSpace " +
        " node or NBodyCollidableNode type.";

    /** Message for when the node in setValue() is not a primitive */
    protected static final String COLLIDABLE_NODE_MSG =
        "Collidables field node value does not describe a CollisionSpace " +
        " node or NBodyCollidableNode type.";

    /** Listing of field indexes that have nodes */
    private static final int[] nodeFields;

    /** Array of VRMLFieldDeclarations */
    private static final VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    private static final HashMap fieldMap;

    // The VRML field values

    /** The value of the collidables field */
    private ArrayList vfCollidables;

    /** The value of the enabled field */
    protected boolean vfEnabled;

    /** value of the field bboxCenter 0, 0, 0 */
    protected float[] vfBboxCenter;

    /** value of the field bboxSize [-1, -1, -1] */
    protected float[] vfBboxSize;

    /** Value of the useGeometry field */
    protected boolean vfUseGeometry;

    // Other vars

    /** Internal scratch var for dealing with added/removed children */
    private VRMLNodeType[] nodeTmp;

    /** The space that ODE plays with */
    private Space odeSpace;

    /** List of nodes that we've determined are child spaces */
    private ArrayList spaceChildren;

    /** List of nodes that we've determined are child geometry */
    private ArrayList geomChildren;

    /**
     * Static constructor to initialise all the field values.
     */
    static {
        nodeFields = new int[] {
            FIELD_METADATA,
            FIELD_COLLIDABLES,
        };

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap(NUM_FIELDS * 3);

        fieldDecl[FIELD_METADATA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "metadata");
        fieldDecl[FIELD_ENABLED] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFBool",
                                     "enabled");
        fieldDecl[FIELD_COLLIDABLES] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFNode",
                                     "collidables");
        fieldDecl[FIELD_USE_GEOMETRY] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFBool",
                                     "useGeometry");
        fieldDecl[FIELD_BBOX_SIZE] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFVec3f",
                                     "bboxSize");
        fieldDecl[FIELD_BBOX_CENTER] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFVec3f",
                                     "bboxCenter");

        Integer idx = new Integer(FIELD_METADATA);
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        idx = new Integer(FIELD_ENABLED);
        fieldMap.put("enabled", idx);
        fieldMap.put("set_enabled", idx);
        fieldMap.put("enabled_changed", idx);

        idx = new Integer(FIELD_COLLIDABLES);
        fieldMap.put("collidables", idx);
        fieldMap.put("set_collidables", idx);
        fieldMap.put("collidables_changed", idx);

        idx = new Integer(FIELD_USE_GEOMETRY);
        fieldMap.put("useGeometry", idx);
        fieldMap.put("set_useGeometry", idx);
        fieldMap.put("useGeometry_changed", idx);

        fieldMap.put("bboxCenter",new Integer(FIELD_BBOX_CENTER));
        fieldMap.put("bboxSize",new Integer(FIELD_BBOX_SIZE));
    }

    /**
     * Construct a new default CollisionSpace node object.
     */
    public BaseCollisionSpace() {
        super("CollisionSpace");

        vfCollidables = new ArrayList();
        vfEnabled = true;
        vfUseGeometry = false;

        vfBboxSize = new float[] {-1, -1, -1};
        vfBboxCenter = new float[] {0, 0, 0};

        hasChanged = new boolean[NUM_FIELDS];

        spaceChildren = new ArrayList();
        geomChildren = new ArrayList();
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not the same type
     */
    public BaseCollisionSpace(VRMLNodeType node) {
        this();

        checkNodeType(node);

        try {
            int index = node.getFieldIndex("enabled");
            VRMLFieldData field = node.getFieldValue(index);
            vfEnabled = field.booleanValue;

            index = node.getFieldIndex("useGeometry");
            field = node.getFieldValue(index);
            vfUseGeometry = field.booleanValue;

            index = node.getFieldIndex("bboxSize");
            field = node.getFieldValue(index);
            vfBboxSize[0] = field.floatArrayValue[0];
            vfBboxSize[1] = field.floatArrayValue[1];
            vfBboxSize[2] = field.floatArrayValue[2];

            index = node.getFieldIndex("bboxCenter");
            field = node.getFieldValue(index);
            vfBboxCenter[0] = field.floatArrayValue[0];
            vfBboxCenter[1] = field.floatArrayValue[1];
            vfBboxCenter[2] = field.floatArrayValue[2];
        } catch(VRMLException ve) {
            throw new IllegalArgumentException(ve.getMessage());
        }
    }

    //----------------------------------------------------------
    // Methods defined by VRMLNBodySpaceNodeType
    //----------------------------------------------------------

    /**
     * Get the ODE object that represents the body to evaluate.
     *
     * @return The body object representing this node
     */
    public Space getODESpace() {
        return odeSpace;
    }

    /**
     * Set the parent space that this space belongs to. A null value clears
     * the world and indicates the physics model or space is no longer in use
     * by it's parent space (eg deletes it).
     *
     * @param parent The new parent of this object, or null
     */
    public void setParentODESpace(Space parent) {
        if(parent == null) {
            odeSpace.delete();
            odeSpace = null;
        } else {
            // Are we replacing this with a new space? If so, cleanup the
            // old one first.
            if(odeSpace != null)
                odeSpace.delete();

            if(vfBboxSize[0] != -1 && vfBboxSize[1] != -1 && vfBboxSize[2] != -1)
                odeSpace = new QuadTreeSpace(parent,
                                             vfBboxCenter,
                                             vfBboxSize,
                                             QUADTREE_DEPTH);
            else
                odeSpace = new HashSpace(parent);

            odeSpace.setChildCleanupMode(false);
        }
    }

    /**
     * Is this group enabled for use right now?
     *
     * @return true if this is enabled
     */
    public boolean isEnabled() {
        return vfEnabled;
    }

    //-------------------------------------------------------------
    // Methods defined by VRMLBoundedNodeType
    //-------------------------------------------------------------

    /**
     * Get the current value of the bboxCenter field. Default value is
     * <code>0 0 0</code>.
     *
     * @return The value of bboxCenter
     */
    public float[] getBboxCenter () {
        return vfBboxCenter;
    }

    /**
     * Get the current value of the bboxSize field. Default value is
     * <code>-1 -1 -1</code>.
     *
     * @return The size of the bounding box
     */
    public float[] getBboxSize () {
        return vfBboxSize;
    }

    //----------------------------------------------------------
    // Methods defined by VRMLNodeType
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

        // Set up the ODE side of the equation if it has not been aready. This
        // ensures that we know now whether we had a valid parent before trying
        // to finish the setup. If odeSpace is null, then we haven't had a
        // parent call us to tell us about a space. Therefore create that root
        // space now and call all the children to tell them about it.
        if(odeSpace == null) {
            if(vfBboxSize[0] != -1 && vfBboxSize[1] != -1 && vfBboxSize[2] != -1)
                odeSpace = new QuadTreeSpace(vfBboxCenter,
                                             vfBboxSize,
                                             QUADTREE_DEPTH);
            else
                odeSpace = new HashSpace();

            odeSpace.setChildCleanupMode(false);

            int size = spaceChildren.size();
            for(int i = 0; i < size; i++) {
                VRMLNBodySpaceNodeType n =
                    (VRMLNBodySpaceNodeType)spaceChildren.get(i);

                n.setParentODESpace(odeSpace);
            }
        }

        int size = vfCollidables.size();
        for(int i = 0; i < size; i++) {
            ((VRMLNodeType)vfCollidables.get(i)).setupFinished();
        }

        size = geomChildren.size();
        for(int i = 0; i < size; i++) {
            VRMLNBodyCollidableNodeType n =
                (VRMLNBodyCollidableNodeType)geomChildren.get(i);

            PlaceableGeom geom = n.getODEGeometry();
            odeSpace.addGeom(geom);
        }
    }

    //----------------------------------------------------------
    // Methods defined by VRMLNode
    //----------------------------------------------------------

    /**
     * Get the index of the given field name. If the name does not exist for
     * this node then return a value of -1.
     *
     * @param fieldName The name of the field we want the index from
     * @return The index of the field name or -1
     */
    public int getFieldIndex(String fieldName) {
        Integer index = (Integer)fieldMap.get(fieldName);

        return (index == null) ? -1 : index.intValue();
    }

    /**
     * Get the list of indices that correspond to fields that contain nodes
     * ie MFNode and SFNode). Used for blind scene graph traversal without
     * needing to spend time querying for all fields etc. If a node does
     * not have any fields that contain nodes, this shall return null. The
     * field list covers all field types, regardless of whether they are
     * readable or not at the VRML-level.
     *
     * @return The list of field indices that correspond to SF/MFnode fields
     *    or null if none
     */
    public int[] getNodeFieldIndices() {
        return nodeFields;
    }

    /**
     * Get the primary type of this node.  Replaces the instanceof mechanism
     * for use in switch statements.
     *
     * @return The primary type
     */
    public int getPrimaryType() {
        return TypeConstants.nBodyCollisionSpaceNodeType;
    }

    /**
     * Get the declaration of the field at the given index. This allows for
     * reverse lookup if needed. If the field does not exist, this will give
     * a value of null.
     *
     * @param index The index of the field to get information
     * @return A shape of this field's information
     */
    public VRMLFieldDeclaration getFieldDeclaration(int index) {
        if(index < 0  || index > LAST_SPACE_INDEX)
            return null;

        return fieldDecl[index];
    }

    /**
     * Get the number of fields.
     *
     * @param The number of fields.
     */
    public int getNumFields() {
        return fieldDecl.length;
    }

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
            case FIELD_COLLIDABLES:
                int num_kids = vfCollidables.size();

                if((nodeTmp == null) || (nodeTmp.length < num_kids))
                    nodeTmp = new VRMLNodeType[num_kids];
                vfCollidables.toArray(nodeTmp);
                fieldData.clear();
                fieldData.nodeArrayValue = nodeTmp;
                fieldData.dataType = VRMLFieldData.NODE_ARRAY_DATA;
                fieldData.numElements = num_kids;
                break;

            case FIELD_ENABLED:
                fieldData.clear();
                fieldData.booleanValue = vfEnabled;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
                break;

            case FIELD_USE_GEOMETRY:
                fieldData.clear();
                fieldData.booleanValue = vfUseGeometry;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
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
                case FIELD_COLLIDABLES:
                    int num_kids = vfCollidables.size();

                    if((nodeTmp == null) || (nodeTmp.length < num_kids))
                        nodeTmp = new VRMLNodeType[num_kids];
                    vfCollidables.toArray(nodeTmp);
                    destNode.setValue(destIndex, nodeTmp, num_kids);
                    break;

                case FIELD_ENABLED:
                    destNode.setValue(destIndex, vfEnabled);
                    break;

                case FIELD_USE_GEOMETRY:
                    destNode.setValue(destIndex, vfUseGeometry);
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
     * Set the value of the field at the given index as a single boolean.
     * This would be used to set SFBool field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not know
     */
    public void setValue(int index, boolean value)
        throws InvalidFieldValueException, InvalidFieldException {

        switch(index) {
            case FIELD_ENABLED:
                setEnabled(value);
                break;

            case FIELD_USE_GEOMETRY:
                setUseGeometry(value);
                break;

            default:
                super.setValue(index, value);
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
        }
    }

    /**
     * Set the value of the field at the given index as a node. This would be
     * used to set SFNode field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldValueException The node does not match the required
     *    type.
     */
    public void setValue(int index, VRMLNodeType child)
        throws InvalidFieldException, InvalidFieldValueException {

        VRMLNodeType node = child;

        switch(index) {
            case FIELD_COLLIDABLES:
                if(!inSetup)
                    clearCollidables();

                addCollidable(node);

                if(!inSetup) {
                    hasChanged[FIELD_COLLIDABLES] = true;
                    fireFieldChanged(FIELD_COLLIDABLES);
                }
                break;

            default:
                super.setValue(index, child);
        }
    }

    /**
     * Set the value of the field at the given index as an array of nodes.
     * This would be used to set MFNode field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
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
            case FIELD_COLLIDABLES:
                if(!inSetup)
                    clearCollidables();

                for(int i = 0; i < numValid; i++ )
                    addCollidable(children[i]);

                if(!inSetup) {
                    hasChanged[FIELD_COLLIDABLES] = true;
                    fireFieldChanged(FIELD_COLLIDABLES);
                }
                break;

            default:
                super.setValue(index, children, numValid);
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
     * Set the new state value of the autoDisable field.
     *
     * @param state True for false
     */
    private void setEnabled(boolean state) {
        vfEnabled = state;

        if(!inSetup) {
            hasChanged[FIELD_ENABLED] = true;
            fireFieldChanged(FIELD_ENABLED);
        }
    }

    /**
     * Set the new state value of the autoDisable field.
     *
     * @param state True for false
     */
    private void setUseGeometry(boolean state) {
        vfUseGeometry = state;

        if(!inSetup) {
            hasChanged[FIELD_USE_GEOMETRY] = true;
            fireFieldChanged(FIELD_USE_GEOMETRY);
        }
    }

    /**
     * Clear the child node list of all children in the VRML node. Override
     * to provide.renderer-specific behaviour, but remember to also call this
     * implementation too.
     */
    protected void clearCollidables() {
        int num_kids = vfCollidables.size();

        if((nodeTmp == null) || (nodeTmp.length < num_kids))
            nodeTmp = new VRMLNodeType[num_kids];

        vfCollidables.toArray(nodeTmp);

        for(int i = 0; i < num_kids; i++)
            updateRefs(nodeTmp[i], false);

        if(num_kids > 0)
            stateManager.registerRemovedNodes(nodeTmp);

        vfCollidables.clear();

// TODO:
// Need to fix API for handling deleting geoms in spaces. Not there right now
        spaceChildren.clear();
        geomChildren.clear();
    }

    /**
     * Add a single child node to the list of available children. Override
     * to provide.renderer-specific behaviour, but remember to also call this
     * implementation too.
     *
     * @param node The node to add
     * @throws InvalidFieldValueException This is a bindable node shared
     */
    protected void addCollidable(VRMLNodeType node)
        throws InvalidFieldValueException {

        if(node instanceof VRMLNBodyCollidableNodeType) {
            vfCollidables.add(node);
            geomChildren.add(node);
        } else if(node instanceof VRMLNBodySpaceNodeType) {
            vfCollidables.add(node);
            spaceChildren.add(node);
        } else if(node instanceof VRMLProtoInstance) {
            VRMLNodeType impl =
                ((VRMLProtoInstance)node).getImplementationNode();

            while((impl != null) && (impl instanceof VRMLProtoInstance))
                impl = ((VRMLProtoInstance)impl).getImplementationNode();

            if(impl instanceof VRMLNBodyCollidableNodeType) {
                vfCollidables.add(impl);
                geomChildren.add(impl);
            } else if(impl instanceof VRMLNBodySpaceNodeType) {
                vfCollidables.add(impl);
                spaceChildren.add(impl);
            } else
                throw new InvalidFieldValueException(COLLIDABLE_PROTO_MSG);
        } else if(node != null)
            throw new InvalidFieldValueException(COLLIDABLE_NODE_MSG);

        if(!inSetup)
            stateManager.registerAddedNode(node);
    }
}
