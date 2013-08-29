/*****************************************************************************
 *                        Web3d.org Copyright (c) 2003 - 2005
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
import java.util.ArrayList;

import org.j3d.geom.GeometryData;

// Local imports
import org.web3d.vrml.nodes.*;

import org.web3d.vrml.lang.TypeConstants;
import org.web3d.vrml.lang.FieldConstants;
import org.web3d.vrml.lang.InvalidFieldException;
import org.web3d.vrml.lang.InvalidFieldAccessException;
import org.web3d.vrml.lang.InvalidFieldFormatException;
import org.web3d.vrml.lang.InvalidFieldValueException;
import org.web3d.vrml.lang.VRMLException;

/**
 * An abstract implementation of the IndexedTriangle* nodes.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.9 $
 */
public abstract class BaseIndexedTriangleGeometryNode
    extends BaseComponentGeometryNode {

    /** Index of the index MFInt32 field */
    protected static final int FIELD_INDEX = LAST_GEOMETRY_INDEX + 1;

    /** Index of the set_colorIndex MFInt32 eventIn */
    protected static final int FIELD_SET_INDEX = LAST_GEOMETRY_INDEX + 2;

    /** The last field index used by this class */
    protected static final int LAST_INDEXEDTRIANGLEGEOMETRY_INDEX = FIELD_SET_INDEX;

    /** Message for when the proto is not a Coord */
    protected static final String COORD_PROTO_MSG =
        "Proto does not describe a Coord object";

    /** Message for when the node in setValue() is not a Coord */
    protected static final String COORD_NODE_MSG =
        "Node does not describe a Coord object";

    /** Message for when the proto is not a Color */
    protected static final String COLOR_PROTO_MSG =
        "Proto does not describe a Color object";

    /** Message for when the node in setValue() is not a Color */
    protected static final String COLOR_NODE_MSG =
        "Node does not describe a Color object";

    /** Message for when the proto is not a Normal */
    protected static final String NORMAL_PROTO_MSG =
        "Proto does not describe a Normal object";

    /** Message for when the node in setValue() is not a Normal */
    protected static final String NORMAL_NODE_MSG =
        "Node does not describe a Normal object";

    /** Message for when the proto is not a TexCoord */
    protected static final String TEXCOORD_PROTO_MSG =
        "Proto does not describe a TexCoord object";

    /** Message for when the node in setValue() is not a TexCoord */
    protected static final String TEXCOORD_NODE_MSG =
        "Node does not describe a TexCoord object";

    /** field MFInt32 index */
    protected int[] vfIndex;

    /** Number of valid values in vfIndex */
    protected int numIndex;

    /** Userdata kept in the triangle geometry */
    protected GeometryData geomData;

    /**
     * Construct a default instance of this class with the bind flag set to
     * false and no time information set (effective value of zero).
     *
     * @param name The name of the type of node
     */
    protected BaseIndexedTriangleGeometryNode(String name) {
        super(name);

        vfIndex = FieldConstants.EMPTY_MFINT32;
        numIndex = 0;

        changeFlags = 0;
    }

    /**
     * Set the fields of the binadble node that has the fields set
     * based on the fields of the passed in node. This directly copies the
     * bind state, so could cause some interesting problems. Not sure what
     * we should do with this currently.
     *
     * @param node The bindable node to copy info from
     */
    protected void copy(VRMLComponentGeometryNodeType node) {

        super.copy(node);

        try {
            int index = node.getFieldIndex("index");
            VRMLFieldData field = node.getFieldValue(index);

            if(field.numElements != 0) {
                vfIndex = new int[field.numElements];
                numIndex = field.numElements;
                System.arraycopy(field.intArrayValue,
                                 0,
                                 vfIndex,
                                 0,
                                 field.numElements);
            }
        } catch(VRMLException ve) {
            throw new IllegalArgumentException(ve.getMessage());
        }
    }

    /**
     * Build the render specific implementation.
     */
    protected abstract void buildImpl();

    //----------------------------------------------------------
    // Methods defined by FrameStateListener
    //----------------------------------------------------------

    /**
     * Notification that the rendering of the event model is complete and that
     * rendering is about to begin. Used to update the transformation matrix
     * only once per frame. If the derived class needs to propogate the
     * changes then it should override the updateMatrix() method or this
     * and make sure this method is called first.
     */
    public void allEventsComplete() {
        buildImpl();
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
    public VRMLFieldData getFieldValue(int index)
        throws InvalidFieldException {

        VRMLFieldData fieldData = fieldLocalData.get();

        switch(index) {
            case FIELD_INDEX:
                fieldData.clear();
                fieldData.intArrayValue = vfIndex;
                fieldData.dataType = VRMLFieldData.INT_ARRAY_DATA;
                fieldData.numElements = numIndex;
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

        switch(srcIndex) {
            case FIELD_INDEX:
                if(inSetup)
                    throw new InvalidFieldAccessException("index is an inputOnly field");
                break;
            default:
                super.sendRoute(time, srcIndex, destNode, destIndex);
        }
    }

    /**
     * Set the value of the field at the given index as an array of integers.
     * This would be used to set MFInt32 field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @param numValid The number of valid values to copy from the array
     * @throws InvalidFieldException The index does not match a known field
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
     */
    public void setValue(int index, int[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException,
               InvalidFieldAccessException {

        if(!inSetup) {
            switch(index) {
                case FIELD_SET_INDEX:
                    setIndex(value, numValid);
                    break;

                case FIELD_INDEX:
                    throw new InvalidFieldAccessException(INIT_ONLY_WRITE_MSG + "index");

                default:
                    super.setValue(index, value, numValid);
            }

            stateManager.addEndOfThisFrameListener(this);
        } else {
            switch(index) {
                case FIELD_INDEX:
                    setIndex(value, numValid);
                    break;

                case FIELD_SET_INDEX:
                    throw new InvalidFieldAccessException(INIT_ONLY_WRITE_MSG + "index");

                default:
                    super.setValue(index, value, numValid);
            }
        }
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

        buildImpl();
    }

    /**
     * Notify a node that an ExternProto has resolved.  This will verify the objects
     * type and add it to the render sceneGraph.
     *
     * @param index The field index
     * @throws InvalidFieldValueException If the proto contains he wrong type
     */
     public synchronized void notifyExternProtoLoaded(int index, VRMLNodeType node)
        throws InvalidFieldValueException {

        if(inSetup)
            return;

        switch(index) {
            case FIELD_COLOR:
                if(node.getPrimaryType() != TypeConstants.ColorNodeType)
                    throw new InvalidFieldValueException(COLOR_PROTO_MSG);

                changeFlags |= COLORS_CHANGED;
                buildImpl();
                break;

            case FIELD_COORD:
                if(node.getPrimaryType() != TypeConstants.CoordinateNodeType)
                    throw new InvalidFieldValueException(COORD_PROTO_MSG);

                changeFlags |= COORDS_CHANGED;
                buildImpl();
                break;

            case FIELD_NORMAL:
                if (node.getPrimaryType() != TypeConstants.NormalNodeType)
                    throw new InvalidFieldValueException(NORMAL_PROTO_MSG);

                changeFlags |= NORMALS_CHANGED;
                buildImpl();
                break;

            case FIELD_TEXCOORD:
                if(node.getPrimaryType() != TypeConstants.TextureCoordinateNodeType)
                    throw new InvalidFieldValueException(TEXCOORD_PROTO_MSG);

                changeFlags |= TEXCOORDS_CHANGED;
                buildImpl();
                break;

            default:
                System.out.println("BaseIndexedTriangleFanSet: Unknown field for notifyExternProtoLoaded");
        }

        stateManager.addEndOfThisFrameListener(this);
    }


    /**
     * Set the value of the field at the given index as a node. This would be
     * used to set SFNode field types.
     *
     * @param index The index of destination field to set
     * @param child The new value to use for the node
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The node does not match the required
     *    type.
     */
    public void setValue(int index, VRMLNodeType child)
        throws InvalidFieldException, InvalidFieldValueException {

        VRMLNodeType node = child;
        boolean notif = false;

        switch(index) {
            case FIELD_COORD:
                if (child == null) {
                    pCoord = null;
                    if (vfCoord != null) {
                            vfCoord.removeComponentListener(this);
                    }
                } else if (child instanceof VRMLProtoInstance) {
                    pCoord = (VRMLProtoInstance) child;
                    node = pCoord.getImplementationNode();

                    if (!(node instanceof VRMLCoordinateNodeType)) {
                        pCoord = null;
                        throw new InvalidFieldValueException(COORD_PROTO_MSG);
                    }
                } else if (!(node instanceof VRMLCoordinateNodeType)) {
                    throw new InvalidFieldValueException(COORD_NODE_MSG);
                }

                vfCoord = (VRMLCoordinateNodeType) node;

                if (vfCoord != null)
                    vfCoord.addComponentListener(this);

                changeFlags |= COORDS_CHANGED;
                notif = true;
                break;

            case FIELD_NORMAL:
                if (child == null) {
                    pNormal = null;
                    if (vfNormal != null) {
                        vfNormal.removeComponentListener(this);
                    }
                } else if (child instanceof VRMLProtoInstance) {
                    pNormal = (VRMLProtoInstance) child;
                    node = pNormal.getImplementationNode();

                    if (!(node instanceof VRMLNormalNodeType)) {
                        pNormal = null;
                        throw new InvalidFieldValueException(NORMAL_PROTO_MSG);
                    }
                } else if (!(node instanceof VRMLNormalNodeType)) {
                    System.out.println("node: " + node);
                    throw new InvalidFieldValueException(NORMAL_NODE_MSG);
                }

                vfNormal = (VRMLNormalNodeType) node;
                if (vfNormal != null)
                    vfNormal.addComponentListener(this);

                changeFlags |= NORMALS_CHANGED;
                notif = true;
                break;

            case FIELD_COLOR :
                if (child == null) {
                    pColor = null;
                    if (vfColor != null)
                        vfColor.removeComponentListener(this);
                } else if (child instanceof VRMLProtoInstance) {
                    pColor = (VRMLProtoInstance) child;
                    node = pColor.getImplementationNode();

                    if (!(node instanceof VRMLColorNodeType)) {
                        pColor = null;
                        throw new InvalidFieldValueException(COLOR_PROTO_MSG);
                    }
                } else if (!(node instanceof VRMLColorNodeType)) {
                    throw new InvalidFieldValueException(COLOR_NODE_MSG);
                }

                vfColor = (VRMLColorNodeType) node;
                if (vfColor != null)
                    vfColor.addComponentListener(this);

                changeFlags |= COLORS_CHANGED;
                notif = true;
                break;

            case FIELD_TEXCOORD :
                if (child == null) {
                    pTexCoord = null;
                    if (vfTexCoord != null) {
                        vfTexCoord.removeComponentListener(this);
                    }
                } else if (child instanceof VRMLProtoInstance) {
                    pTexCoord = (VRMLProtoInstance) child;
                    node = pTexCoord.getImplementationNode();

                    if (!(node instanceof VRMLTextureCoordinateNodeType)) {
                        pTexCoord = null;
                        throw new
                            InvalidFieldValueException(TEXCOORD_PROTO_MSG);
                    }
                } else if (!(node instanceof VRMLTextureCoordinateNodeType)) {
                    throw new InvalidFieldValueException(TEXCOORD_NODE_MSG);
                }

                vfTexCoord = (VRMLTextureCoordinateNodeType) node;

                if (vfTexCoord != null)
                    vfTexCoord.addComponentListener(this);

                changeFlags |= TEXCOORDS_CHANGED;
                notif = true;
                break;

            default:
                super.setValue(index, child);
        }

        if(!inSetup && notif) {
            stateManager.addEndOfThisFrameListener(this);
            hasChanged[index] = true;
            fireFieldChanged(index);
        }
    }

    //----------------------------------------------------------
    // Methods defined by BaseComponentGeometryNode
    //----------------------------------------------------------

    /**
     * Notification of the coordinate node being set. If the passed value is
     * null then that clears the node. The node passed is the actual geometry,
     * not any proto wrapper, that will have been previously stripped. The
     * default implementation does nothing.
     *
     * @param node The node to use
     */
    protected void setCoordinateNode(VRMLCoordinateNodeType node) {
        if(inSetup)
            return;

        changeFlags |= COORDS_CHANGED;

        buildImpl();
    }

    /**
     * Notification of the color node being set. If the passed value is
     * null then that clears the node. The node passed is the actual color,
     * not any proto wrapper, that will have been previously stripped. The
     * default implementation does nothing.
     *
     * @param node The node to use
     */
    protected void setColorNode(VRMLColorNodeType node) {

        if(inSetup)
            return;

        changeFlags |= COLORS_CHANGED;

        buildImpl();
    }

    /**
     * Notification of the coordinate node being set. If the passed value is
     * null then that clears the node. The node passed is the actual geometry,
     * not any proto wrapper, that will have been previously stripped. The
     * default implementation does nothing.
     *
     * @param node The node to use
     */
    protected void setTextureCoordinateNode(VRMLTextureCoordinateNodeType node) {
        if(inSetup)
            return;

        changeFlags |= TEXCOORDS_CHANGED;

        buildImpl();
    }

    /**
     * Notification of the color node being set. If the passed value is
     * null then that clears the node. The node passed is the actual color,
     * not any proto wrapper, that will have been previously stripped. The
     * default implementation does nothing.
     *
     * @param node The node to use
     */
    protected void setNormalNode(VRMLNormalNodeType node) {

        changeFlags |= NORMALS_CHANGED;

        buildImpl();
    }



    //-------------------------------------------------------------
    // Methods defined by VRMLNodeComponentListener
    //-------------------------------------------------------------

    /**
     * Notification that the field from the node has changed.
     *
     * @param node The component node that changed
     * @param index The index of the field that has changed
     */
    public void fieldChanged(VRMLNodeType node, int index) {
        if(node == vfCoord) {
            changeFlags |= COORDS_CHANGED;
            buildImpl();
        } else if(node == vfColor) {
            changeFlags |= COLORS_CHANGED;
            buildImpl();
        } else if(node == vfNormal) {
            changeFlags |= NORMALS_CHANGED;
            buildImpl();
        } else if(node == vfTexCoord) {
            changeFlags |= TEXCOORDS_CHANGED;
            buildImpl();
        } else
            System.out.println("BaseIndexedTriangleSet: Unknown field for fieldChanged");

        stateManager.addEndOfThisFrameListener(this);
    }


    //----------------------------------------------------------
    // Local public methods
    //----------------------------------------------------------

    /**
     * Set the index field. Override to provide.renderer-specific behaviour,
     * but remember to also call this implementation too.
     *
     * @param value The list of index values to use
     * @param numValid The number of valid values to copy from the array
     */
    protected void setIndex(int[] value, int numValid) {
        if(vfIndex.length < numValid)
            vfIndex = new int[numValid];

        System.arraycopy(value, 0, vfIndex, 0, numValid);
        numIndex = numValid;

        changeFlags |= COORDS_INDEX_CHANGED;

        if(!inSetup)
            stateManager.addEndOfThisFrameListener(this);
    }

    /**
     * Update the coordinate array in geomData based on the coordinate data.
     */
    protected void updateCoordinateArray() {
        if(vfCoord == null)
            geomData.vertexCount = 0;
        else {
            geomData.vertexCount = vfCoord.getNumPoints() / 3;
            geomData.coordinates = vfCoord.getPointRef();
        }
    }

    /**
     * Update the normal array in geomData based on the normal node and
     * normalPerVertex flag.
     */
    protected void updateNormalArray() {
        if((vfNormal != null) && vfNormalPerVertex) {
            geomData.normals = vfNormal.getVectorRef();
        }
    }

    /**
     * Update the normal array in geomData based on the normal node and
     * normalPerVertex flag.
     */
    protected void updateColorArray() {
        if(vfColor != null) {
            int index = vfColor.getFieldIndex("color");
            VRMLFieldData data = vfColor.getFieldValue(index);

            if(data.numElements != 0)
                geomData.colors = data.floatArrayValue;
            else
                geomData.colors = null;
        }
    }
}
