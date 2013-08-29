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

package org.web3d.vrml.nodes.proto;

// External imports
import java.io.Reader;
import java.io.StringReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

// Local imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;

import org.web3d.util.ArrayUtils;
import org.web3d.util.IntArray;
import org.web3d.util.IntHashMap;
import org.web3d.vrml.parser.VRMLFieldReader;
import org.web3d.vrml.parser.FieldParserFactory;

/**
 * A placeholder instance of a Prototype used when declaring and then using a
 * nested proto instance.
 * <p>
 *
 * The idea of this class is to fake being a real prototype instance when the
 * code stubs a nested proto instance. A nested proto instance is of the form
 *
 * <pre>
 * PROTO Outer [] {
 *   PROTO Inner [] { Box {} }
 *   Inner {}
 * }
 * </pre>
 *
 * In this case, when we are storing the definition of <tt>Inner</tt> then we
 * really don't want to expand the internal prototype definition at this point.
 * The expansion should only take place when we create an instance of
 * <code>Outer</code>. This class is used as a placeholder in this definition
 * for this scenario. The idea is that when the code is traversing the
 * definition, it will be replaced by a real runtime version.
 *
 * @author Justin Couch
 * @version $Revision: 1.45 $
 */
public class ProtoInstancePlaceHolder extends AbstractProto
    implements VRMLProtoInstance {

    /** Message stub for unknown fields */
    private static final String FIELD_MSG =
        "Attempt to set unknown field index: ";

    /**
     * Message when the reference count attempts to decrement a layer ID that
     * does not currently refrence this node. Under correct implementation,
     * this message should never be seen.
     */
    private static final String NO_LAYER_REF_MSG =
        "An attempt was made to decrement a reference count to a layer that " +
        "is not currently referencing this node. Layer ID is ";

    /** The prototype declaration that we always use */
    private VRMLNodeTemplate proto;

    /** Field indicating the DEF status */
    private boolean hasDEF;

    /**
     * A map of the field indexes (Integer) to their values (VRMLFieldData).
     */
    private IntHashMap fieldValueMap;

    /** The scene description if needed */
    private BasicScene scene;

    /** The implementation node type, if available */
    private VRMLNodeType implNode;

    /** Flag indicating we are in setup mode currently */
    private boolean inSetup;

    /** Flag for the node being static */
    private boolean isStatic;

    /** Mapping of field index to user data object */
    private IntHashMap userData;

    /**
     * The current number of references to this node. This is for informational
     * purposes only and should never be touched by derived classes.
     */
    protected int[] refCounts;

    /**
     * The list of layer IDs that reference this node. Should correspond 1:1
     * to the counts in {@link #refCounts}. If none, this is null.
     */
    protected int[] layerIds;

    /** Actual array of node field indicies. Set in setupComplete */
    private int[] nodeFields;

    /**
     * Create a new instance place holder that represents the given proto
     * declaration.
     *
     * @param proto The prototype declaration to base this placeholder on
     * @param major The major version number of this scene
     * @param minor The minor version number of this scene
     * @param staticNodes Whether this node is will be modified
     * @param creator The node creator for generating instances of ourself
     */
    public ProtoInstancePlaceHolder(VRMLNodeTemplate proto,
                                    int major,
                                    int minor,
                                    boolean staticNodes,
                                    NodeTemplateToInstanceCreator creator) {
        super(proto.getVRMLNodeName(), major, minor, creator);

        this.proto = proto;
        hasDEF = false;
        inSetup = true;
        isStatic = staticNodes;

        userData = new IntHashMap();
        IntArray node_array = new IntArray();

        // Make the field declarations look like it is a real node.
        List fields = proto.getAllFields();
        Iterator itr = fields.iterator();

        while(itr.hasNext()) {
            VRMLFieldDeclaration decl = (VRMLFieldDeclaration)itr.next();
            try {
                appendField(decl);
            } catch(FieldExistsException fee) {
                // ignored because we should never get this
            }
        }

        fieldValueMap = new IntHashMap(fields.size());

        if(proto instanceof PrototypeDecl) {
            PrototypeDecl decl = (PrototypeDecl)proto;
            VRMLGroupingNodeType body = decl.getBodyGroup();
            VRMLNodeType[] children = body.getChildren();
            implNode = children[0];

            isVrml97 = decl.isVRML97();

            itr = fields.iterator();

            while(itr.hasNext()) {
                try {
                    VRMLFieldDeclaration field_decl =
                        (VRMLFieldDeclaration)itr.next();

                    int access = field_decl.getAccessType();

                    if((access == FieldConstants.EVENTIN) ||
                       (access == FieldConstants.EVENTOUT))
                       continue;

                    String field_name = field_decl.getName();
                    int field_index = decl.getFieldIndex(field_name);

                    VRMLFieldData data = decl.getFieldValue(field_index);
                    field_index = getFieldIndex(field_name);

                    if(data != null) {
                        VRMLFieldData local_data = new VRMLFieldData(data);

                        fieldValueMap.put(field_index, local_data);

                        int field_type = field_decl.getFieldType();

                        switch(field_type) {
                            case FieldConstants.SFNODE:
                                node_array.add(field_index);
                                break;

                            case FieldConstants.MFNODE:
                                node_array.add(field_index);
                                break;
                        }
                    } else {
                        // The field must be null. Probably a node field then.
                        // Let's make an educated guess and register a FieldData
                        // anyway
                        data = new VRMLFieldData();
                        data.numElements = 0;

                        int field_type = field_decl.getFieldType();

                        switch(field_type) {
                            case FieldConstants.SFNODE:
                                data.dataType = VRMLFieldData.NODE_DATA;
                                node_array.add(field_index);
                                break;

                            case FieldConstants.MFNODE:
                                data.dataType = VRMLFieldData.NODE_ARRAY_DATA;
                                data.nodeArrayValue = new VRMLNodeType[5];
                                node_array.add(field_index);
                                break;

                            case FieldConstants.SFINT32:
                                data.dataType = VRMLFieldData.INT_DATA;
                                break;

                            case FieldConstants.SFSTRING:
                                data.dataType = VRMLFieldData.STRING_DATA;
                                break;

                            case FieldConstants.MFSTRING:
                                data.dataType = VRMLFieldData.STRING_ARRAY_DATA;
                                break;

                            case FieldConstants.MFINT32:
                            case FieldConstants.SFIMAGE:
                            case FieldConstants.MFIMAGE:
                                data.dataType = VRMLFieldData.INT_ARRAY_DATA;
                                break;

                            case FieldConstants.SFFLOAT:
                                data.dataType = VRMLFieldData.FLOAT_DATA;
                                break;

                            case FieldConstants.SFTIME:
                            case FieldConstants.SFDOUBLE:
                                data.dataType = VRMLFieldData.DOUBLE_DATA;
                                break;

                            case FieldConstants.MFTIME:
                            case FieldConstants.MFDOUBLE:
                            case FieldConstants.SFVEC3D:
                            case FieldConstants.MFVEC3D:
                                data.dataType = VRMLFieldData.DOUBLE_ARRAY_DATA;
                                break;

                            case FieldConstants.SFLONG:
                                data.dataType = VRMLFieldData.LONG_DATA;
                                break;

                            case FieldConstants.MFLONG:
                                data.dataType = VRMLFieldData.LONG_ARRAY_DATA;
                                break;

                            case FieldConstants.SFBOOL:
                                data.dataType = VRMLFieldData.BOOLEAN_DATA;
                                break;

                            case FieldConstants.MFBOOL:
                                data.dataType = VRMLFieldData.BOOLEAN_ARRAY_DATA;
                                break;

                            case FieldConstants.MFFLOAT:
                            case FieldConstants.SFVEC2F:
                            case FieldConstants.SFVEC3F:
                            case FieldConstants.MFVEC2F:
                            case FieldConstants.MFVEC3F:
                            case FieldConstants.SFCOLOR:
                            case FieldConstants.MFCOLOR:
                            case FieldConstants.SFCOLORRGBA:
                            case FieldConstants.MFCOLORRGBA:
                                data.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                                break;
                        }

                        fieldValueMap.put(field_index, data);
                    }
                } catch(FieldException fe) {
                    // should never happen!
                    System.out.println("Waaaa! proto raw fields don't match!");
                    fe.printStackTrace();
                }
            }

        } else {
            // We got an externproto. Process it for the node fields at least
            // so that we can work everything correctly when building up a
            // complete node later on.
            itr = fields.iterator();
            ExternalPrototypeDecl decl = (ExternalPrototypeDecl)proto;

            while(itr.hasNext()) {
                try {
                    VRMLFieldDeclaration field_decl =
                        (VRMLFieldDeclaration)itr.next();

                    int access = field_decl.getAccessType();

                    if((access == FieldConstants.EVENTIN) ||
                       (access == FieldConstants.EVENTOUT))
                       continue;

                    String field_name = field_decl.getName();
                    int field_index = decl.getFieldIndex(field_name);

                    field_index = getFieldIndex(field_name);

                    int field_type = field_decl.getFieldType();

                    // Since this is an externproto declaration that we are
                    // wrapping, none of the fields have any values. However,
                    // when others are traversing the scene, particularly for
                    // node values, they expect to find at least a valid
                    // VRMLFieldData instance. So, for these, we're going to
                    // create one and drop it into the fieldValueMap so that the
                    // traverser doesn't have kittens.
                    VRMLFieldData data = new VRMLFieldData();

                    switch(field_type) {
                        case FieldConstants.SFNODE:
                            data.dataType = VRMLFieldData.NODE_DATA;
                            data.numElements = 0;
                            node_array.add(field_index);
                            break;

                        case FieldConstants.MFNODE:
                            data = new VRMLFieldData();
                            data.dataType = VRMLFieldData.NODE_ARRAY_DATA;
                            data.nodeArrayValue = new VRMLNodeType[5];
                            data.numElements = 0;

                            node_array.add(field_index);
                            break;

                        case FieldConstants.SFINT32:
                            data.dataType = VRMLFieldData.INT_DATA;
                            break;

                        case FieldConstants.SFSTRING:
                            data.dataType = VRMLFieldData.STRING_DATA;
                            break;

                        case FieldConstants.MFSTRING:
                            data.dataType = VRMLFieldData.STRING_ARRAY_DATA;
                            break;

                        case FieldConstants.MFINT32:
                        case FieldConstants.SFIMAGE:
                        case FieldConstants.MFIMAGE:
                            data.dataType = VRMLFieldData.INT_ARRAY_DATA;
                            break;

                        case FieldConstants.SFFLOAT:
                            data.dataType = VRMLFieldData.FLOAT_DATA;
                            break;

                        case FieldConstants.SFTIME:
                        case FieldConstants.SFDOUBLE:
                            data.dataType = VRMLFieldData.DOUBLE_DATA;
                            break;

                        case FieldConstants.MFTIME:
                        case FieldConstants.MFDOUBLE:
                        case FieldConstants.SFVEC3D:
                        case FieldConstants.MFVEC3D:
                            data.dataType = VRMLFieldData.DOUBLE_ARRAY_DATA;
                            break;

                        case FieldConstants.SFLONG:
                            data.dataType = VRMLFieldData.LONG_DATA;
                            break;

                        case FieldConstants.MFLONG:
                            data.dataType = VRMLFieldData.LONG_ARRAY_DATA;
                            break;

                        case FieldConstants.SFBOOL:
                            data.dataType = VRMLFieldData.BOOLEAN_DATA;
                            break;

                        case FieldConstants.MFBOOL:
                            data.dataType = VRMLFieldData.BOOLEAN_ARRAY_DATA;
                            break;

                        case FieldConstants.MFFLOAT:
                        case FieldConstants.SFVEC2F:
                        case FieldConstants.SFVEC3F:
                        case FieldConstants.MFVEC2F:
                        case FieldConstants.MFVEC3F:
                        case FieldConstants.SFCOLOR:
                        case FieldConstants.MFCOLOR:
                        case FieldConstants.SFCOLORRGBA:
                        case FieldConstants.MFCOLORRGBA:
                            data.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                            break;
                    }

                    fieldValueMap.put(field_index, data);
                } catch(FieldException fe) {
                    // should never happen!
                    System.out.println("Waaaa! externproto raw fields don't match!");
                    fe.printStackTrace();
                }
            }
        }

        nodeFields = new int[node_array.size()];
        node_array.toArray(nodeFields);
    }

    //----------------------------------------------------------
    // Methods defined by VRMLNodeTemplate
    //----------------------------------------------------------

    /**
     * Create a new instance of a real node from this template. This will
     * ensure that all the internals are created as needed, based on the
     * current state of the node. Note that sometimes, creating an instance
     * of this template may result in an invalid node construction. Cases
     * where this could occur is when there's no node definition yet loaded
     * or that the loaded definition does not match this template.
     *
     * @param root The node that represents the root node of the
     *   VRMLExecutionSpace that we're in.
     * @param isStatic true if this is created within a StaticGroup
     * @return A new node instance from this template
     * @throws InvalidNodeTypeException The root node is not a node capable
     *    of representing a root of a scene graph
     * @see org.web3d.vrml.nodes.VRMLProtoInstance
     * @see org.web3d.vrml.nodes.VRMLWorldRootNodeType
     */
    public VRMLNode createNewInstance(VRMLNode root, boolean isStatic)
        throws InvalidNodeTypeException {


        return null;
    }

    //----------------------------------------------------------
    // Methods defined by VRMLProtoInstance
    //----------------------------------------------------------

    /**
     * Get the first node declared in the proto as that defines just how we
     * we can add this into the scenegraph. If this is an empty prototype
     * implementation, or represents an Extern proto that has not been loaded
     * yet then this will return null.
     *
     * @return The node instance that represents the first node
     */
    public VRMLNodeType getImplementationNode() {
        return implNode;
    }

    /**
     * Get the PROTO/EXTERNPROTO definition used by this place holder.
     *
     * @return The proto definition used by the node
     */
    public VRMLNodeTemplate getProtoDefinition() {
        return proto;
    }

    /**
     * Get the list of all the body nodes in this proto instance. Nodes are
     * defined in declaration order. Index 0 is always the same value as
     * that returned by {@link #getImplementationNode()}. This should be called
     * sparingly. It is really only of use to something that needs to traverse
     * the entire scene graph or for scripting to provide access to the root of
     * the scene.
     *
     * @return The list of nodes from the body
     */
    public VRMLNodeType[] getBodyNodes() {
        // This is used by scripting, and thus this should never be called.
        // since this is a placeholder used during file parse time
        return null;
    }

    //----------------------------------------------------------
    // Methods defined by VRMLExecutionSpace
    //----------------------------------------------------------

    /**
     * Get the contained scene graph that this instance has. This represents
     * everything about the internal scene that the node declaration wraps.
     * This is a real-time representation so that if it the nodes contains a
     * script that changes the internal representation then this instance will
     * be updated to reflect and changes made.
     *
     * @return The scene contained by this node instance
     */
    public BasicScene getContainedScene() {
        // only create when we need it
        if(scene == null)
            scene = new ProtoScene(0, 0);

        return scene;
    }

    //----------------------------------------------------------
    // Methods defined by FrameStateListener
    //----------------------------------------------------------

    /**
     * Ignored by this implementation.
     */
    public void allEventsComplete() {
    }

    //----------------------------------------------------------
    // Methods defined by VRMLNodeType
    //----------------------------------------------------------

    /**
     * Notify a node that an ExternProto has resolved.  This will verify the objects
     * type and add it to the render sceneGraph.
     *
     * @param index The field index
     * @throws InvalidFieldValueException If the proto contains he wrong type
     */
     public void notifyExternProtoLoaded(int index, VRMLNodeType node)
        throws InvalidFieldValueException {

        System.out.println("ProtoInstancePlaceHolder notifyExtern ProtoLoaded not implemented.");
     }

    /**
     * Ask for the current number of references to this object in the given
     * layer. If this node represents a layer node itself, then the value
     * returned for that ID should always be one. If the layer requested does
     * not have a reference to this node, return zero.
     *
     * @param layer The id of the layer to get the ref count for
     * @return The number of references to this node
     */
    public int getRefCount(int layer) {
        if(layerIds == null)
            return 0;

        int ret_val = 0;

        for(int i = 0; i < layerIds.length; i++) {
            if(layerIds[i] == layer) {
                ret_val = refCounts[i];
                break;
            }
        }

        return ret_val;
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
    public synchronized void updateRefCount(int layer, boolean add) {

        // Go looking for the layer first to see if we have it.
        if(layerIds != null) {
            int ref_idx = -1;

            for(int i = 0; i < layerIds.length; i++) {
                if(layerIds[i] == layer) {
                    ref_idx = i;
                    break;
                }
            }

            if(ref_idx == -1) {
                // Decrementing a layer Id that doesn't exist? Something is
                // badly wrong, so let's toss an exception and debug WTF just
                // happened.
                if(!add)
                    throw new IllegalStateException(NO_LAYER_REF_MSG + layer);

                int[] tmp = new int[layerIds.length + 1];
                System.arraycopy(layerIds, 0, tmp, 0, layerIds.length);
                tmp[layerIds.length] = layer;
                layerIds = tmp;

                tmp = new int[refCounts.length + 1];
                System.arraycopy(refCounts, 0, tmp, 0, refCounts.length);
                tmp[refCounts.length] = 1;
                refCounts = tmp;
            } else {
                if(add) {
                    refCounts[ref_idx]++;
                } else {
                    refCounts[ref_idx]--;

                    if(refCounts[ref_idx] <= 0) {
                        int[] tmp1 = new int[refCounts.length - 1];
                        int[] tmp2 = new int[refCounts.length - 1];

                        for(int i = 0; i < ref_idx; i++) {
                            tmp1[i] = refCounts[i];
                            tmp2[i] = layerIds[i];
                        }

                        for(int i = ref_idx + 1; i < refCounts.length; i++) {
                            tmp1[i - 1] = refCounts[i];
                            tmp2[i - 1] = layerIds[i];
                        }

                        refCounts = tmp1;
                        layerIds = tmp2;
                    }

                    // Do we have no references left?
                    if(refCounts.length == 1 && refCounts[0] == 0) {
                        refCounts = null;
                        layerIds = null;
                    }
                }
            }
        } else {
            if(!add)
                throw new IllegalStateException(NO_LAYER_REF_MSG + layer);

            layerIds = new int[] { layer };
            refCounts = new int[] { 1 };
        }
    }

    /**
     * Get a listing of the current layer IDs that are directly or indirectly
     * referencing this node. If this layer is not part of a live scene graph
     * (eg held by a script code, but not by the script itself) then this will
     * will return a null value. The array will always be exactly equal in
     * length to the number of IDs that reference this node.
     *
     * @return An array of all the IDs referencing this node or null if none
     */
    public int[] getLayerIds() {
        return layerIds;
    }


    /**
     * Get the list of layer IDs that this node has just been removed from.
     * If it currently has not been removed from anything, this will return
     * null. This will remain updated until the next
     * {@link #clearRemovedLayerIds()} call.
     *
     * @return An array of all the IDs this is delete from or null if none
     */
    public int[] getRemovedLayerIds() {
        // Don't need removed IDs for placeholders as they are only there for
        // needed for runtime structures. This is a copy structure.
        return null;
    }


    /**
     * Clear the current removed layer ID list. If there is nothing removed,
     * this method does nothing.
     */
    public void clearRemovedLayerIds() {
    }

    /**
     * Ignored by this implementation.
     *
     * @param mgr The manager instance to use
     */
    public void setFrameStateManager(FrameStateManager mgr) {
    }

    /**
     * Notify this node that is has been DEFd. This method shall only be
     * called before setupFinished(). It is an error to call it any other
     * time. It is also guaranteed that this call will be made after
     * construction, but before any of the setValue() methods have been called.
     *
     * @throws IllegalStateException The setup is finished.
     */
    public void setDEF() {
        hasDEF = true;
    }

    /**
     * Check to see if this node has been DEFd. Returns true if it has and
     * the user should ask for the shared representation rather than the
     * normal one.
     *
     * @return true if this node has been DEFd
     */
    public boolean isDEF() {
        return hasDEF;
    }

    /**
     * Get the primary type of this node.  Replaces the instanceof mechanism
     * for use in switch statements.
     *
     * @return The primary type
     */
    public int getPrimaryType() {
        return TypeConstants.ProtoInstance;
    }

    /**
     * Get the secondary type of this node.  Replaces the instanceof mechanism
     * for use in switch statements.
     *
     * @return The secondary type
     */
    public int[] getSecondaryType() {
        return TypeConstants.NO_SECONDARY_TYPE;
    }

    /**
     * Check to see if the given field has changed since we last checked.
     * Always returns false as this is a placeholder node, not a runtime
     * version.
     *
     * @param index The index of the field to change.
     * @return true if the field has changed since last read
     */
    public boolean hasFieldChanged(int index) {
        return false;
    }

    /**
     * Get the list of indices that correspond to fields that contain nodes
     * ie MFNode and SFNode). Empty for the proto body group node.
     *
     * @return null
     */
    public int[] getNodeFieldIndices() {
        return nodeFields;
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

        if(fieldDeclList.get(index) == null)
            throw new InvalidFieldException("Invalid Index: " + index +
                                            " " + this);

        VRMLFieldData ret_val = (VRMLFieldData)fieldValueMap.get(index);

        return ret_val;
    }

    /**
     * Set the X3DMetadataObject that is associated with this node. Ignored
     * because proto body nodes don't take metadata.
     *
     * @param data The node to register as the metadata
     * @throws InvalidFieldValueException The object instance provided
     *     does not implment VRMLMetadataNodeType or is not a proto instance
     *     that encapsulates it as it's primary type
     */
    public void setMetadataObject(VRMLNodeType data)
        throws InvalidFieldValueException {
    }

    /**
     * Get the currently registered metadata object instance. If none is set
     * then return null. Ignored because because proto body nodes don't take
     * metadata.
     *
     * @return The current metadata object or null
     */
    public VRMLNodeType getMetadataObject() {
        return null;
    }

    /**
     * Send a routed value from this node to the given destination node. This
     * method always does nothing as it is a placeholder only, not a runtime
     * class.
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
        // Do nothing.
    }

    /**
     * Set the value of the field at the given index as an integer. This would
     * be used to set SFInt32 field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
     */
    public void setValue(int index, int value)
        throws InvalidFieldException, InvalidFieldValueException {

        // check for a valid index by getting the value of that index....
        Object old_value = fieldValueMap.get(index);

        VRMLFieldData data;

        if(!inSetup) {
            if(old_value == null)
                throw new InvalidFieldException("(int) " + FIELD_MSG + index);

            data = (VRMLFieldData)old_value;
            data.intValue = value;
        } else {
            data = (VRMLFieldData)old_value;
            data.intValue = value;
        }
    }

    /**
     * Set the value of the field at the given index as an array of integers.
     * This would be used to set MFInt32 field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @param numValid The number of valid values to copy from the array
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
     */
    public void setValue(int index, int[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException {
        // check for a valid index by getting the value of that index....
        Object old_value = fieldValueMap.get(index);

        VRMLFieldData data;

        if(!inSetup) {
            if(old_value == null)
                throw new InvalidFieldException("(int[]) " + FIELD_MSG + index);

            data = (VRMLFieldData)old_value;
            data.intArrayValue = value;
            data.numElements = numValid;
        } else {
            data = (VRMLFieldData)old_value;
            data.intArrayValue = value;
            data.numElements = numValid;
        }
    }

    /**
     * Set the value of the field at the given index as an booleaneger. This would
     * be used to set SFBool field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
     */
    public void setValue(int index, boolean value)
        throws InvalidFieldException, InvalidFieldValueException {
        // check for a valid index by getting the value of that index....
        Object old_value = fieldValueMap.get(index);

        VRMLFieldData data;

        if(!inSetup) {
            if(old_value == null)
                throw new InvalidFieldException("(boolean) " + FIELD_MSG + index);

            data = (VRMLFieldData)old_value;
            data.booleanValue = value;
        } else {
            data = (VRMLFieldData)old_value;
            data.booleanValue = value;
        }
    }

    /**
     * Set the value of the field at the given index as an array of booleanegers.
     * This would be used to set MFBool field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @param numValid The number of valid values to copy from the array
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
     */
    public void setValue(int index, boolean[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException {
        // check for a valid index by getting the value of that index....
        Object old_value = fieldValueMap.get(index);

        VRMLFieldData data;

        if(!inSetup) {
            if(old_value == null)
                throw new InvalidFieldException("(boolean[]) " + FIELD_MSG + index);

            data = (VRMLFieldData)old_value;
            data.booleanArrayValue = value;
            data.numElements = numValid;
        } else {
            data = (VRMLFieldData)old_value;
            data.booleanArrayValue = value;
            data.numElements = numValid;
        }
    }

    /**
     * Set the value of the field at the given index as a float. This would
     * be used to set SFFloat field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
     */
    public void setValue(int index, float value)
        throws InvalidFieldException, InvalidFieldValueException {
        // check for a valid index by getting the value of that index....
        Object old_value = fieldValueMap.get(index);

        VRMLFieldData data;

        if(!inSetup) {
            if(old_value == null)
                throw new InvalidFieldException("(float) " + FIELD_MSG + index);

            data = (VRMLFieldData)old_value;
            data.floatValue = value;
        } else {
            data = (VRMLFieldData)old_value;
            data.floatValue = value;
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
        throws InvalidFieldException, InvalidFieldValueException {
        // check for a valid index by getting the value of that index....
        Object old_value = fieldValueMap.get(index);

        VRMLFieldData data;

        int num_values = 0;

        VRMLFieldDeclaration decl =
            (VRMLFieldDeclaration)getFieldDeclaration(index);

        int size_mult = decl.getFieldSize();
        num_values = numValid / size_mult;

        if(!inSetup) {
            if(old_value == null)
                throw new InvalidFieldException("(float[]) " + FIELD_MSG + index);

            data = (VRMLFieldData)old_value;
            data.floatArrayValue = value;
            data.numElements = num_values;
        } else {
            data = (VRMLFieldData)old_value;
            data.floatArrayValue = value;
            data.numElements = num_values;
        }
    }

    /**
     * Set the value of the field at the given index as an long. This would
     * be used to set SFTime field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
     */
    public void setValue(int index, long value)
        throws InvalidFieldException, InvalidFieldValueException {

        Object old_value = fieldValueMap.get(index);

        VRMLFieldData data;

        if(!inSetup) {
            if(old_value == null)
                throw new InvalidFieldException("(long) " + FIELD_MSG + index);

            data = (VRMLFieldData)old_value;
            data.longValue = value;
        } else {
            data = (VRMLFieldData)old_value;
            data.longValue = value;
        }
    }

    /**
     * Set the value of the field at the given index as an array of longs.
     * This would be used to set MFTime field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @param numValid The number of valid values to copy from the array
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
     */
    public void setValue(int index, long[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException {

        Object old_value = fieldValueMap.get(index);

        VRMLFieldData data;

        if(!inSetup) {
            if(old_value == null)
                throw new InvalidFieldException("(long[]) " + FIELD_MSG + index);

            data = (VRMLFieldData)old_value;
            data.longArrayValue = value;
            data.numElements = numValid;
        } else {
            data = (VRMLFieldData)old_value;
            data.longArrayValue = value;
            data.numElements = numValid;
        }
    }

    /**
     * Set the value of the field at the given index as an double. This would
     * be used to set SFDouble field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
     */
    public void setValue(int index, double value)
        throws InvalidFieldException, InvalidFieldValueException {

        // check for a valid index by getting the value of that index....
        Object old_value = fieldValueMap.get(index);

        VRMLFieldData data;

        if(!inSetup) {
            if(old_value == null)
                throw new InvalidFieldException("(double) " + FIELD_MSG + index);

            data = (VRMLFieldData)old_value;
            data.doubleValue = value;
        } else {
            data = (VRMLFieldData)old_value;
            data.doubleValue = value;
        }
    }

    /**
     * Set the value of the field at the given index as an array of doubles.
     * This would be used to set MFDouble, SFVec2d and SFVec3d field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @param numValid The number of valid values to copy from the array
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
     */
    public void setValue(int index, double[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException {
        // check for a valid index by getting the value of that index....
        Object old_value = fieldValueMap.get(index);

        VRMLFieldData data;
        VRMLFieldDeclaration decl =
            (VRMLFieldDeclaration)getFieldDeclaration(index);

        int num_values = 0;

        int size_mult = decl.getFieldSize();
        num_values = numValid / size_mult;

        if(!inSetup) {
            if(old_value == null)
                throw new InvalidFieldException("(double[]) " + FIELD_MSG + index);

            data = (VRMLFieldData)old_value;
            data.doubleArrayValue = value;
            data.numElements = num_values;
        } else {
            data = (VRMLFieldData)old_value;
            data.doubleArrayValue = value;
            data.numElements = num_values;
        }
    }

    /**
     * Set the value of the field at the given index as a string. This would
     * be used to set SFString field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
     */
    public void setValue(int index, String value)
        throws InvalidFieldException, InvalidFieldValueException {
        // check for a valid index by getting the value of that index....
        Object old_value = fieldValueMap.get(index);

        VRMLFieldData data;

        if(!inSetup) {
            if(old_value == null)
                throw new InvalidFieldException("(String) " + FIELD_MSG + index);

            data = (VRMLFieldData)old_value;
            data.stringValue = value;
        } else {
            data = (VRMLFieldData)old_value;
            data.stringValue = value;
        }
    }

    /**
     * Set the value of the field at the given index as an array of strings.
     * This would be used to set MFString field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @param numValid The number of valid values to copy from the array
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
     */
    public void setValue(int index, String[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException {
        // check for a valid index by getting the value of that index....
        Object old_value = fieldValueMap.get(index);

        VRMLFieldData data;

        if(!inSetup) {
            if(old_value == null)
                throw new InvalidFieldException("(String[]) " + FIELD_MSG + index);

            data = (VRMLFieldData)old_value;
            data.stringArrayValue = value;
            data.numElements = numValid;
        } else {
            data = (VRMLFieldData)old_value;
            data.stringArrayValue = value;
            data.numElements = numValid;
        }
    }

    /**
     * Set the value of the field at the given index as a node. This would be
     * used to set SFNode field types.
     *
     * @param index The index of destination field to set
     * @param child The new value to use for the node
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
     */
    public void setValue(int index, VRMLNodeType child)
        throws InvalidFieldException, InvalidFieldValueException {
        // check for a valid index by getting the value of that index....
        Object old_value = fieldValueMap.get(index);

        VRMLFieldData data;

        if(!inSetup) {
            if(old_value == null)
                throw new InvalidFieldException("(child) " + FIELD_MSG + index);

            data = (VRMLFieldData)old_value;
            data.nodeValue = child;
        } else {

            VRMLFieldDeclaration decl = getFieldDeclaration(index);

            // Since we're inSetup, this could be the appending of
            // another field value onto the existing one (ie MFNode field).
            // Check for this and adjust as needed.
            if(old_value != null) {
                data = (VRMLFieldData)old_value;
                if(decl.getFieldType() == FieldConstants.MFNODE) {

                    // Reallocate array to bigger size if needed.
                    if(data.nodeArrayValue.length == data.numElements) {
                        VRMLNodeType[] tmp =
                            new VRMLNodeType[data.numElements + 5];

                        System.arraycopy(data.nodeArrayValue,
                                         0,
                                         tmp,
                                         0,
                                         data.numElements);
                        data.nodeArrayValue = tmp;
                    }

                    // Now just append it to the list
                    data.nodeArrayValue[data.numElements] = child;
                    data.numElements++;
                } else {
                    // just replace the existing value.
                    data.nodeValue = child;
                }
            } else {
                data = (VRMLFieldData)old_value;

                if(decl.getFieldType() == FieldConstants.MFNODE) {
                    data.dataType = VRMLFieldData.NODE_ARRAY_DATA;
                    data.nodeArrayValue = new VRMLNodeType[5];
                    data.nodeArrayValue[0] = child;
                    data.numElements = 1;
                } else {
                    data.dataType = VRMLFieldData.NODE_DATA;
                    data.nodeValue = child;
                }

                fieldValueMap.put(index, data);
            }
        }
    }

    /**
     * Set the value of the field at the given index as an array of nodes.
     * This would be used to set MFNode field types.
     *
     * @param index The index of destination field to set
     * @param children The new value to use for the node
     * @param numValid The number of valid values to copy from the array
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
     */
    public void setValue(int index, VRMLNodeType[] children, int numValid)
        throws InvalidFieldException, InvalidFieldValueException {
        // check for a valid index by getting the value of that index....
        Object old_value = fieldValueMap.get(index);

        VRMLFieldData data;

        if(!inSetup) {
            if(old_value == null)
                throw new InvalidFieldException("(child[]) " + FIELD_MSG + index);

            data = (VRMLFieldData)old_value;
            data.nodeArrayValue = children;
            data.numElements = numValid;
        } else {
            data = (VRMLFieldData)old_value;
            data.nodeArrayValue = children;
            data.numElements = numValid;
        }
    }

    /**
     * Set arbitrary data for a given field. Provided primarily to help the
     * EAI fullfil its requirements, but may be useful elsewhere.
     *
     * @param index The index of destination field to set
     * @param data The item to store for the field
     * @throws InvalidFieldException The field index is not known
     */
    public void setUserData(int index, Object data)
        throws InvalidFieldException {

        userData.put(index, data);
    }

    /**
     * Fetch the stored user data for a given field index. If nothing is
     * registered, null is returned.
     *
     * @param index The index of destination field to set
     * @return The item stored for the field or null
     * @throws InvalidFieldException The field index is not known
     */
    public Object getUserData(int index) throws InvalidFieldException {
        return userData.get(index);
    }

    /**
     * Check to see if setupFinished() has already been called on this node.
     *
     * @return true if setupFinished() has been called
     */
    public boolean isSetupFinished() {
        return !inSetup;
    }

    /**
     * Notification that the construction phase of this node has finished.
     * If the node would like to do any internal processing, such as setting
     * up geometry, then go for it now.
     */
    public void setupFinished() {
        if(!inSetup)
            return;

        inSetup = false;
    }

    //----------------------------------------------------------
    // Methods defined by VRMLNodeType
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
        this.isStatic = isStatic;
    }
}
