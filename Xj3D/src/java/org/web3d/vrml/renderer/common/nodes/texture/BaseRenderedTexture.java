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

package org.web3d.vrml.renderer.common.nodes.texture;

// Standard imports
import java.util.HashMap;
import java.util.ArrayList;

// Application specific imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;

import org.web3d.vrml.renderer.common.nodes.BaseTexture2DNode;

/**
 * RenderedTexture node implementation.
 * <p>
 *
 *
 * @author Justin Couch
 * @version $Revision: 1.10 $
 */
public abstract class BaseRenderedTexture extends BaseTexture2DNode
    implements VRMLTexture2DNodeType {

    /** Index of update field */
    protected static final int FIELD_UPDATE = LAST_TEXTURENODETYPE_INDEX + 1;

    /** Index of dimensions field */
    protected static final int FIELD_DIMENSIONS = LAST_TEXTURENODETYPE_INDEX + 2;

    /** Index of background field */
    protected static final int FIELD_BACKGROUND = LAST_TEXTURENODETYPE_INDEX + 3;

    /** Index of fog field */
    protected static final int FIELD_FOG = LAST_TEXTURENODETYPE_INDEX + 4;

    /** Index of viewpoint field */
    protected static final int FIELD_VIEWPOINT = LAST_TEXTURENODETYPE_INDEX + 5;

    /** Index of scene field */
    protected static final int FIELD_SCENE = LAST_TEXTURENODETYPE_INDEX + 6;

    /** The last field index used by this class */
    protected static final int LAST_RENDERED_INDEX = FIELD_SCENE;

    /** Number of fields implemented */
    protected static final int NUM_FIELDS = LAST_RENDERED_INDEX + 1;


    /** Message for when the proto is not a Background */
    protected static final String BACKGROUND_PROTO_MSG =
        "Proto does not describe a Background object";

    /** Message for when the node in setValue() is not a Background */
    protected static final String BACKGROUND_NODE_MSG =
        "Node does not describe a Background object";


    /** Message for when the proto is not a Fog */
    protected static final String FOG_PROTO_MSG =
        "Proto does not describe a Fog object";

    /** Message for when the node in setValue() is not a Fog */
    protected static final String FOG_NODE_MSG =
        "Node does not describe a Fog object";


    /** Message for when the proto is not a Viewpoint */
    protected static final String VIEWPOINT_PROTO_MSG =
        "Proto does not describe a Viewpoint object";

    /** Message for when the node in setValue() is not a Viewpoint */
    protected static final String VIEWPOINT_NODE_MSG =
        "Node does not describe a Viewpoint object";


    /** Message for when the proto is not a Scene */
    protected static final String SCENE_PROTO_MSG =
        "Proto does not describe a Scene object";

    /** Message for when the node in setValue() is not a Scene */
    protected static final String SCENE_NODE_MSG =
        "Node does not describe a Scene object";


    /** Internal ID for no updates */
    protected static final int UPDATE_NONE = 1;

    /** Internal ID for updating this next frame */
    protected static final int UPDATE_NEXT = 2;

    /** Internal ID for updating every frame */
    protected static final int UPDATE_ALWAYS = 3;

    /** Array of VRMLFieldDeclarations */
    protected static VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    protected static HashMap fieldMap;

    /** Listing of field indexes that have nodes */
    private static int[] nodeFields;

    /** Mapping of the justify field string to the internal int type */
    protected static HashMap updateMap;


    /** exposedField SFBool update FALSE */
    protected String vfUpdate;

    /** The current update type as a constant */
    protected int updateFlag;

    /** field MFInt32 dimensions */
    protected int[] vfDimensions;

    /** exposedField SFNode background NULL */
    protected VRMLBackgroundNodeType vfBackground;

    /** The proto version of the background */
    protected VRMLProtoInstance pBackground;

    /** exposedField SFNode viewpoint NULL */
    protected VRMLViewpointNodeType vfViewpoint;

    /** The proto version of the viewpoint */
    protected VRMLProtoInstance pViewpoint;

    /** exposedField SFNode fog NULL */
    protected VRMLFogNodeType vfFog;

    /** The proto version of the fog */
    protected VRMLProtoInstance pFog;

    /** exposedField SFNode scene NULL */
    protected VRMLGroupingNodeType vfScene;

    /** The proto version of the scene */
    protected VRMLProtoInstance pScene;

    // Static constructor
    static {
        nodeFields = new int[] {
            FIELD_METADATA,
            FIELD_BACKGROUND,
            FIELD_FOG,
            FIELD_VIEWPOINT,
            FIELD_SCENE
        };

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap(NUM_FIELDS * 3);

        fieldDecl[FIELD_METADATA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "metadata");
        fieldDecl[FIELD_REPEATS] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFBool",
                                     "repeatS");
        fieldDecl[FIELD_REPEATT] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFBool",
                                     "repeatT");
        fieldDecl[FIELD_BACKGROUND] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "background");
        fieldDecl[FIELD_VIEWPOINT] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "viewpoint");
        fieldDecl[FIELD_FOG] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "fog");
        fieldDecl[FIELD_SCENE] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "scene");
        fieldDecl[FIELD_UPDATE] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFString",
                                     "update");
        fieldDecl[FIELD_DIMENSIONS] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "MFInt32",
                                     "dimensions");

        Integer idx = new Integer(FIELD_METADATA);
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        idx = new Integer(FIELD_SCENE);
        fieldMap.put("scene", idx);
        fieldMap.put("set_scene", idx);
        fieldMap.put("scene_changed", idx);

        idx = new Integer(FIELD_BACKGROUND);
        fieldMap.put("background", idx);
        fieldMap.put("set_background", idx);
        fieldMap.put("background_changed", idx);

        idx = new Integer(FIELD_FOG);
        fieldMap.put("fog", idx);
        fieldMap.put("set_fog", idx);
        fieldMap.put("fog_changed", idx);

        idx = new Integer(FIELD_VIEWPOINT);
        fieldMap.put("viewpoint", idx);
        fieldMap.put("set_viewpoint", idx);
        fieldMap.put("viewpoint_changed", idx);

        idx = new Integer(FIELD_UPDATE);
        fieldMap.put("update", idx);
        fieldMap.put("set_update", idx);
        fieldMap.put("update_changed", idx);

        fieldMap.put("dimensions", new Integer(FIELD_DIMENSIONS));
        fieldMap.put("repeatS", new Integer(FIELD_REPEATS));
        fieldMap.put("repeatT", new Integer(FIELD_REPEATT));

        updateMap = new HashMap();
        updateMap.put("NONE", new Integer(UPDATE_NONE));
        updateMap.put("NEXT_FRAME_ONLY", new Integer(UPDATE_NEXT));
        updateMap.put("ALWAYS", new Integer(UPDATE_ALWAYS));
    }

    /**
     * Constructor creates a default movietexture node.
     */
    public BaseRenderedTexture() {
        super("RenderedTexture");

        hasChanged = new boolean[NUM_FIELDS];

        vfUpdate = "NONE";
        vfRepeatS = true;
        vfRepeatT = true;
        vfDimensions = new int[] { 128, 128, 3 };

        updateFlag = UPDATE_NONE;
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not a Box node, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not a Group node
     */
    public BaseRenderedTexture(VRMLNodeType node) {
        this();

        checkNodeType(node);

        copy((VRMLTexture2DNodeType)node);

        try {
            int index;
            VRMLFieldData field;

            index = node.getFieldIndex("update");
            field = node.getFieldValue(index);
            vfUpdate = field.stringValue;

            Integer id = (Integer)updateMap.get(vfUpdate);
            updateFlag = id.intValue();

            index = node.getFieldIndex("dimensions");
            field = node.getFieldValue(index);
            if(vfDimensions.length < field.numElements) {
                vfDimensions = new int[field.numElements];
                System.arraycopy(field.intArrayValue,
                                 0,
                                 vfDimensions,
                                 0,
                                 field.numElements);
            }

        } catch(VRMLException ve) {
            throw new IllegalArgumentException(ve.getMessage());
        }
    }

    //-------------------------------------------------------------
    // Methods required by the VRMLTexture2DNodeType interface.
    //-------------------------------------------------------------

    /**
     * Get the texture type of this texture.  Valid entries are defined
     * in the vrml.lang.TextureConstants.
     */
    public int getTextureType() {
        return TextureConstants.TYPE_PBUFFER;
    }

    //----------------------------------------------------------------------
    // Methods defined by VRMLNodeType
    //----------------------------------------------------------------------

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
     * Get the primary type of this node.  Replaces the instanceof mechanism
     * for use in switch statements.
     *
     * @return The primary type
     */
    public int getPrimaryType() {
        return TypeConstants.TextureNodeType;
    }

    /**
     * Notification that the construction phase of this node has finished.
     * If the node would like to do any internal processing, such as setting
     * up geometry, then go for it now.
     */
    public void setupFinished() {
        if(!inSetup)
            return;

        super.setupFinished();

        if(pBackground != null)
            pBackground.setupFinished();
        else if(vfBackground != null)
            vfBackground.setupFinished();

        if(pFog != null)
            pFog.setupFinished();
        else if(vfFog != null)
            vfFog.setupFinished();

        if(pViewpoint != null)
            pViewpoint.setupFinished();
        else if(vfViewpoint != null)
            vfViewpoint.setupFinished();

        if(pScene != null)
            pScene.setupFinished();
        else if(vfScene != null)
            vfScene.setupFinished();
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
     * Get the declaration of the field at the given index. This allows for
     * reverse lookup if needed. If the field does not exist, this will give
     * a value of null.
     *
     * @param index The index of the field to get information
     * @return A representation of this field's information
     */
    public VRMLFieldDeclaration getFieldDeclaration(int index) {
        if(index < 0  || index > LAST_RENDERED_INDEX)
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
            case FIELD_UPDATE:
                fieldData.clear();
                fieldData.stringValue = vfUpdate;
                fieldData.dataType = VRMLFieldData.STRING_DATA;
                break;

            case FIELD_DIMENSIONS:
                fieldData.clear();
                fieldData.intArrayValue = vfDimensions;
                fieldData.dataType = VRMLFieldData.INT_ARRAY_DATA;
                fieldData.numElements = vfDimensions.length;
                break;

            case FIELD_BACKGROUND:
                if(pBackground != null)
                    fieldData.nodeValue = pBackground;
                else
                    fieldData.nodeValue = vfBackground;
                fieldData.dataType = VRMLFieldData.NODE_DATA;
                break;

            case FIELD_FOG:
                if(pFog != null)
                    fieldData.nodeValue = pFog;
                else
                    fieldData.nodeValue = vfFog;
                fieldData.dataType = VRMLFieldData.NODE_DATA;
                break;

            case FIELD_VIEWPOINT:
                if(pViewpoint != null)
                    fieldData.nodeValue = pViewpoint;
                else
                    fieldData.nodeValue = vfViewpoint;
                fieldData.dataType = VRMLFieldData.NODE_DATA;
                break;

            case FIELD_SCENE:
                if(pScene != null)
                    fieldData.nodeValue = pScene;
                else
                    fieldData.nodeValue = vfScene;
                fieldData.dataType = VRMLFieldData.NODE_DATA;
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

        try {
            switch(srcIndex) {
                case FIELD_BACKGROUND:
                    if(pBackground != null)
                        destNode.setValue(destIndex, pBackground);
                    else
                        destNode.setValue(destIndex, vfBackground);
                    break;

                case FIELD_FOG:
                    if(pFog != null)
                        destNode.setValue(destIndex, pFog);
                    else
                        destNode.setValue(destIndex, vfFog);
                    break;

                case FIELD_VIEWPOINT:
                    if(pViewpoint != null)
                        destNode.setValue(destIndex, pViewpoint);
                    else
                        destNode.setValue(destIndex, vfViewpoint);
                    break;

                case FIELD_SCENE:
                    if(pScene != null)
                        destNode.setValue(destIndex, pScene);
                    else
                        destNode.setValue(destIndex, vfScene);
                    break;

                case FIELD_UPDATE:
                    destNode.setValue(destIndex, vfUpdate);
                    break;

                default:
                    super.sendRoute(time, srcIndex, destNode, destIndex);
            }
        } catch(InvalidFieldException ife) {
            System.err.println("Text sendRoute: No field!" + ife.getFieldName());
        } catch(InvalidFieldValueException ifve) {
            System.err.println("Text sendRoute: Invalid field Value: " +
                ifve.getMessage());
        }
    }

    /**
     * Set the value of the field at the given index as a float.
     * This would be used to set SFBool field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     */
    public void setValue(int index, String value)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_UPDATE:
                setUpdate(value);
                break;

            default:
                super.setValue(index,value);
        }
    }

    /**
     * Set the value of the field at the given index as a float.
     * This would be used to set MFInt32/SFImage/MFImage field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @param numValid The number of valid values to copy from the array
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
     * @throws InvalidFieldAccessException The call is attempting to write to
     *    a field that does not permit writing now
     */
    public void setValue(int index, int[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException,
               InvalidFieldAccessException {

        switch(index) {
            case FIELD_DIMENSIONS:
                if(!inSetup)
                    throw new InvalidFieldAccessException(
                        "The dimension field is initializeOnly");

                if(numValid > vfDimensions.length)
                    vfDimensions = new int[numValid];

                System.arraycopy(value, 0, vfDimensions, 0, numValid);
                break;

            default:
                super.setValue(index, value, numValid);
        }
    }

    /**
     * Set the value of the field at the given index as a node. This would be
     * used to set SFNode field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
     */
    public void setValue(int index, VRMLNodeType child)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_BACKGROUND:
                setBackground(child);
                break;

            case FIELD_FOG:
                setFog(child);
                break;

            case FIELD_VIEWPOINT:
                setViewpoint(child);
                break;

            case FIELD_SCENE:
                setScene(child);
                break;

            default:
                super.setValue(index, child);
        }
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * The update state has changed. Override if need be for any
     * renderer-specific needs.
     *
     * @param state The new state for updates
     */
    protected void setUpdate(String state) {

        Integer id = (Integer)updateMap.get(state);

        if(id == null)
            throw new InvalidFieldValueException("Bogus update flag " + state);

        vfUpdate = state;
        updateFlag = id.intValue();

        if(!inSetup) {
            hasChanged[FIELD_UPDATE] = true;
            fireFieldChanged(FIELD_UPDATE);
        }
    }

    /**
     * Set new value to be used for the background field.
     *
     * @param bg The new background.  null will act like delete
     * @throws InvalidFieldValueException The node does not match the required
     *    type.
     */
    protected void setBackground(VRMLNodeType bg)
        throws InvalidFieldValueException {

        VRMLBackgroundNodeType node;
        VRMLNodeType old_node;

        if(pBackground != null)
            old_node = pBackground;
        else
            old_node = vfBackground;

        if (bg instanceof VRMLProtoInstance) {
            node = (VRMLBackgroundNodeType)
                ((VRMLProtoInstance)bg).getImplementationNode();
            pBackground = (VRMLProtoInstance) bg;
            if ((node != null) && !(node instanceof VRMLBackgroundNodeType)) {
                throw new InvalidFieldValueException(BACKGROUND_PROTO_MSG);
            }
        } else if (bg != null &&
            (!(bg instanceof VRMLBackgroundNodeType))) {
            throw new InvalidFieldValueException(BACKGROUND_NODE_MSG);
        } else {
            pBackground = null;
            node = (VRMLBackgroundNodeType) bg;
        }

        vfBackground = (VRMLBackgroundNodeType)node;

        if(bg != null)
            updateRefs(bg, true);

        if(old_node != null)
            updateRefs(old_node, false);

        if (!inSetup) {
            if(old_node != null)
                stateManager.registerRemovedNode(old_node);

            if(bg != null)
                stateManager.registerAddedNode(bg);

            hasChanged[FIELD_BACKGROUND] = true;
            fireFieldChanged(FIELD_BACKGROUND);
        }
    }

    /**
     * Set new value to be used for the fog field.
     *
     * @param fog The new fog.  null will act like delete
     * @throws InvalidFieldValueException The node does not match the required
     *    type.
     */
    protected void setFog(VRMLNodeType fog)
        throws InvalidFieldValueException {

        VRMLFogNodeType node;
        VRMLNodeType old_node;

        if(pFog != null)
            old_node = pFog;
        else
            old_node = vfFog;

        if (fog instanceof VRMLProtoInstance) {
            node = (VRMLFogNodeType)
                ((VRMLProtoInstance)fog).getImplementationNode();
            pFog = (VRMLProtoInstance) fog;
            if ((node != null) && !(node instanceof VRMLFogNodeType)) {
                throw new InvalidFieldValueException(FOG_PROTO_MSG);
            }
        } else if (fog != null &&
            (!(fog instanceof VRMLFogNodeType))) {
            throw new InvalidFieldValueException(FOG_NODE_MSG);
        } else {
            pFog = null;
            node = (VRMLFogNodeType) fog;
        }

        vfFog = (VRMLFogNodeType)node;

        if(fog != null)
            updateRefs(fog, true);

        if(old_node != null)
            updateRefs(old_node, false);

        if (!inSetup) {
            if(old_node != null)
                stateManager.registerRemovedNode(old_node);

            if(fog != null)
                stateManager.registerAddedNode(fog);

            hasChanged[FIELD_FOG] = true;
            fireFieldChanged(FIELD_FOG);
        }
    }

    /**
     * Set new value to be used for the viewpoint field.
     *
     * @param vp The new viewpoint.  null will act like delete
     * @throws InvalidFieldValueException The node does not match the required
     *    type.
     */
    protected void setViewpoint(VRMLNodeType vp)
        throws InvalidFieldValueException {

        VRMLViewpointNodeType node;
        VRMLNodeType old_node;

        if(pViewpoint != null)
            old_node = pViewpoint;
        else
            old_node = vfViewpoint;

        if (vp instanceof VRMLProtoInstance) {
            node = (VRMLViewpointNodeType)
                ((VRMLProtoInstance)vp).getImplementationNode();
            pViewpoint = (VRMLProtoInstance) vp;
            if ((node != null) && !(node instanceof VRMLViewpointNodeType)) {
                throw new InvalidFieldValueException(VIEWPOINT_PROTO_MSG);
            }
        } else if (vp != null &&
            (!(vp instanceof VRMLViewpointNodeType))) {
            throw new InvalidFieldValueException(VIEWPOINT_NODE_MSG);
        } else {
            pViewpoint = null;
            node = (VRMLViewpointNodeType) vp;
        }

        vfViewpoint = (VRMLViewpointNodeType)node;

        if(vp != null)
            updateRefs(vp, true);

        if(old_node != null)
            updateRefs(old_node, false);

        if (!inSetup) {
            if(old_node != null)
                stateManager.registerRemovedNode(old_node);

            if(vp != null)
                stateManager.registerAddedNode(vp);

            hasChanged[FIELD_VIEWPOINT] = true;
            fireFieldChanged(FIELD_VIEWPOINT);
        }
    }

    /**
     * Set new value to be used for the scene field.
     *
     * @param scn The new scene.  null will act like delete
     * @throws InvalidFieldValueException The node does not match the required
     *    type.
     */
    protected void setScene(VRMLNodeType scn)
        throws InvalidFieldValueException {

        VRMLGroupingNodeType node;
        VRMLNodeType old_node;

        if(pScene != null)
            old_node = pScene;
        else
            old_node = vfScene;

        if (scn instanceof VRMLProtoInstance) {
            node = (VRMLGroupingNodeType)
                ((VRMLProtoInstance)scn).getImplementationNode();
            pScene = (VRMLProtoInstance) scn;
            if ((node != null) && !(node instanceof VRMLGroupingNodeType)) {
                throw new InvalidFieldValueException(SCENE_PROTO_MSG);
            }
        } else if (scn != null &&
            (!(scn instanceof VRMLGroupingNodeType))) {
            throw new InvalidFieldValueException(SCENE_NODE_MSG);
        } else {
            pScene = null;
            node = (VRMLGroupingNodeType) scn;
        }

        vfScene = (VRMLGroupingNodeType)node;

        if(scn != null)
            updateRefs(scn, true);

        if(old_node != null)
            updateRefs(old_node, false);

        if (!inSetup) {
            if(old_node != null)
                stateManager.registerRemovedNode(old_node);

            if(scn != null)
                stateManager.registerAddedNode(scn);

            hasChanged[FIELD_SCENE] = true;
            fireFieldChanged(FIELD_SCENE);
        }
    }
}
