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

package org.web3d.vrml.renderer.common.nodes;

// External imports
import java.util.ArrayList;

// Local imports
import org.web3d.vrml.nodes.*;

import org.web3d.vrml.lang.TypeConstants;
import org.web3d.vrml.lang.InvalidFieldFormatException;
import org.web3d.vrml.lang.InvalidFieldException;
import org.web3d.vrml.lang.InvalidFieldValueException;

/**
 * An abstract implementation of any node that uses component nodes to provide
 * coordinate, normal and texture information.
 * <p>
 *
 *
 * @author Justin Couch
 * @version $Revision: 1.18 $
 */
public abstract class BaseComponentGeometryNode extends AbstractNode
    implements VRMLComponentGeometryNodeType,
               VRMLNodeComponentListener {

	/** The minimum number of vertices before using VBO's */
	public static final int VBO_MIN_VERTICES = 1000;

    /** Index of the coord exposedField */
    protected static final int FIELD_COORD = LAST_NODE_INDEX + 1;

    /** Index of the color exposedField */
    protected static final int FIELD_COLOR = LAST_NODE_INDEX + 2;

    /** Index of the normal exposedField */
    protected static final int FIELD_NORMAL = LAST_NODE_INDEX + 3;

    /** Index of the texture exposedField */
    protected static final int FIELD_TEXCOORD = LAST_NODE_INDEX + 4;

    /** Index of the solid field */
    protected static final int FIELD_SOLID = LAST_NODE_INDEX + 5;

    /** Index of the ccw field */
    protected static final int FIELD_CCW = LAST_NODE_INDEX + 6;

    /** Index of the colorPerVertex field */
    protected static final int FIELD_COLORPERVERTEX = LAST_NODE_INDEX + 7;

    /** Index of the normalPerVertex field */
    protected static final int FIELD_NORMALPERVERTEX = LAST_NODE_INDEX + 8;

    /** Index of the fogCoord exposedField */
    protected static final int FIELD_FOG_COORD = LAST_NODE_INDEX + 9;

    /** Index of the attribs exposedField */
    protected static final int FIELD_ATTRIBS = LAST_NODE_INDEX + 10;


    /** The last field index used by this class */
    protected static final int LAST_GEOMETRY_INDEX = FIELD_ATTRIBS;

    /** Flag value for the changeFlags coordinate node change */
    protected static final int COORDS_CHANGED = 0x01;

    /** Flag value for the changeFlags normal node change */
    protected static final int NORMALS_CHANGED = 0x02;

    /** Flag value for the changeFlags texCoords node change */
    protected static final int TEXCOORDS_CHANGED = 0x04;

    /** Flag value for the changeFlags color node change */
    protected static final int COLORS_CHANGED = 0x08;

    /** Flag value for the changeFlags color node change */
    protected static final int FOG_CHANGED = 0x10;

    /** Flag value for the changeFlags color node change */
    protected static final int ATTRIB_CHANGED = 0x20;

    /** Flag value for the changeFlags unlit color change */
    protected static final int UNLIT_COLORS_CHANGED = 0x40;

    /** Flag value for the changeFlags coordinate index change */
    protected static final int COORDS_INDEX_CHANGED = 0x100;

    /** Flag value for the changeFlags normal index change */
    protected static final int NORMALS_INDEX_CHANGED = 0x200;

    /** Flag value for the changeFlags texCoords index change */
    protected static final int TEXCOORDS_INDEX_CHANGED = 0x400;

    /** Flag value for the changeFlags color index change */
    protected static final int COLORS_INDEX_CHANGED = 0x800;

    /** Flag value for the changeFlags fog index change */
    protected static final int FOG_INDEX_CHANGED = 0x1000;

    /** Flag value for the changeFlags attrib index change */
    protected static final int ATTRIB_INDEX_CHANGED = 0x2000;

    /** Message for when the proto is not a Geometry */
    protected static final String BAD_PROTO_MSG =
        "Proto does not describe a GeometryComponent object";

    /** Message for when the node in setValue() is not a Geometry */
    protected static final String BAD_NODE_MSG =
        "Node does not describe a GeometryComponent object";

    /** Message for when the proto is not a Coord */
    protected static final String COORD_PROTO_MSG =
        "Proto does not describe a X3DCoordinateNode object";

    /** Message for when the node in setValue() is not a Coord */
    protected static final String COORD_NODE_MSG =
        "Node does not describe a X3DCoordinateNode object";

    /** Message for when the proto is not a Color */
    protected static final String COLOR_PROTO_MSG =
        "Proto does not describe a X3DColorNode object";

    /** Message for when the node in setValue() is not a Color */
    protected static final String COLOR_NODE_MSG =
        "Node does not describe a X3DColorNode object";

    /** Message for when the proto is not a FogCoordinate */
    protected static final String FOG_PROTO_MSG =
        "Proto does not describe a FogCoordinate object";

    /** Message for when the node in setValue() is not a FogCoordinate */
    protected static final String FOG_NODE_MSG =
        "Node does not describe a FogCoordinate object";

    /** Message for when the proto is not a vertex attribute */
    protected static final String ATTRIB_PROTO_MSG =
        "Proto does not describe a VertexAttributes object";

    /** Message for when the node in setValue() is not a vertex attribute */
    protected static final String ATTRIB_NODE_MSG =
        "Node does not describe a VertexAttributes object";


    /** Proto version of the color */
    protected VRMLProtoInstance pColor;

    /** exposedField SFNode color */
    protected VRMLColorNodeType vfColor;

    /** Proto version of the coord */
    protected VRMLProtoInstance pCoord;

    /** exposedField SFNode coord */
    protected VRMLCoordinateNodeType vfCoord;

    /** Proto version of the normal */
    protected VRMLProtoInstance pNormal;

    /** exposedField SFNode normal */
    protected VRMLNormalNodeType vfNormal;

    /** Proto version of the texCoord */
    protected VRMLProtoInstance pTexCoord;

    /** exposedField SFNode texCoord */
    protected VRMLTextureCoordinateNodeType vfTexCoord;

    /** Proto version of the fogCoord */
    protected VRMLProtoInstance pFogCoord;

    /** exposedField SFNode fogCoord */
    protected VRMLGeometricPropertyNodeType vfFogCoord;

    /** field SFBool colorPerVertex TRUE */
    protected boolean vfColorPerVertex;

    /** field SFBool normalPerVertex TRUE */
    protected boolean vfNormalPerVertex;

    /** field SFBool solid TRUE */
    protected boolean vfSolid;

    /** field SFBool ccw TRUE */
    protected boolean vfCcw;

    /** Bit-flags to work out what has changed during the last frame */
    protected int changeFlags;

    /** List of per-vertex attribute nodes provided */
    protected ArrayList vfAttribs;

    /** Should local color node be used for diffuse lighting */
    protected boolean localColors;

    /** The list of listeners for localColor changes */
    protected ArrayList localColorsListeners;

    /**
     * Construct a default instance of this class with the bind flag set to
     * false and no time information set (effective value of zero).
     *
     * @param name The name of the type of node
     */
    protected BaseComponentGeometryNode(String name) {
        super(name);

        vfSolid = true;
        vfCcw = true;
        vfColorPerVertex = true;
        vfNormalPerVertex = true;
        vfAttribs = new ArrayList(1);

        localColors = false;
        localColorsListeners = new ArrayList(1);
    }

    //----------------------------------------------------------
    // Methods overriding VRMLGeometryNodeType
    //----------------------------------------------------------

    /**
     * Specified whether this node has color information.  If so, then it
     * will be used for diffuse terms instead of materials.
     *
     * @return true Use local color information for diffuse lighting.
     */
    public boolean hasLocalColors() {
        return localColors;
    }

    /**
     * Specified whether this node has alpha values in the local colour
     * information. If so, then it will be used for to override the material's
     * transparency value.
     *
     * @return true when the local color value has inbuilt alpha
     */
    public boolean hasLocalColorAlpha() {
        return (vfColor != null) && (vfColor.getNumColorComponents() == 4);
    }

    /**
     * Add a listener for local color changes.  Nulls and duplicates will be ignored.
     *
     * @param l The listener.
     */
    public void addLocalColorsListener(LocalColorsListener l) {
        if (l != null)
            localColorsListeners.add(l);
    }

    /**
     * Remove a listener for local color changes.  Nulls will be ignored.
     *
     * @param l The listener.
     */
    public void removeLocalColorsListener(LocalColorsListener l) {
        localColorsListeners.remove(l);
    }

    /**
     * Add a listener for texture coordinate generation mode changes.
     * Nulls and duplicates will be ignored.
     *
     * @param l The listener.
     */
    public void addTexCoordGenModeChanged(TexCoordGenModeListener l) {
        System.out.println("TexCoordGenMode changes not implemented");
    }

    /**
     * Remove a listener for texture coordinate generation mode changes.
     * Nulls will be ignored.
     *
     * @param l The listener.
     */
    public void removeTexCoordGenModeChanged(TexCoordGenModeListener l) {
    }

    /**
     * Get the number of texture coordinate sets contained by this node
     *
     * @return the number of texture coordinate sets
     */
    public int getNumSets() {
        if (vfTexCoord == null)
            return 0;

        return vfTexCoord.getNumSets();
    }

    /**
     * Get the texture coordinate generation mode.  NULL is returned
     * if the texture coordinates are not generated.
     *
     * @param setNum The set which this tex gen mode refers
     * @return The mode or NULL
     */
    public String getTexCoordGenMode(int setNum) {
        if (vfTexCoord == null)
            return null;

        return vfTexCoord.getTexCoordGenMode(setNum);
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
        vfSolid = node.isSolid();
        vfCcw = node.isCCW();
        vfColorPerVertex = node.hasColorPerVertex();
        vfNormalPerVertex = node.hasNormalPerVertex();
    }

    /**
     * Get the value of the solid field.
     *
     * @return true This object is solid (ie single sided)
     */
    public boolean isSolid() {
        return vfSolid;
    }

    /**
     * Get the value of the CCW field.
     *
     * @return true Vertices are declared in counter-clockwise order
     */
    public boolean isCCW() {
        return vfCcw;
    }

    /**
     * Specifies whether this node requires lighting.
     *
     * @return Should lighting be enabled
     */
    public boolean isLightingEnabled() {
        return true;
    }

    //----------------------------------------------------------
    // Methods defined by VRMLComponentGeometryNodeType
    //----------------------------------------------------------

    /**
     * Get the components that compose a geometry object.
     * <p>
     * This method will return either VRMLGeometricPropertyNodeType or
     * VRMLProtoInstance.  With a proto you can use getImplementationNode
     * to get a node conforming to the VRMLGeometricPropertyNodeType interface.
     * <p>
     * If there are no components then a zero length array will be returned.
     *
     * @return VRMLNodeType[] The components
     */
    public VRMLNodeType[] getComponents() {
        int cnt = 5;
        if(vfCoord == null && pCoord == null)
            cnt--;
        if(vfNormal == null && pNormal == null)
            cnt--;
        if(vfTexCoord == null && pTexCoord == null)
            cnt--;
        if(vfColor == null && pColor == null)
            cnt--;
        if(vfFogCoord == null && pFogCoord == null)
            cnt--;

        VRMLNodeType[] ret = new VRMLNodeType[cnt];

        cnt=0;
        if(pCoord != null)
            ret[cnt++] = pCoord;
        else if(vfCoord != null)
            ret[cnt++] = vfCoord;

        if(pNormal != null)
            ret[cnt++] = pNormal;
        else if(vfNormal != null)
            ret[cnt++] = vfNormal;

        if(pTexCoord != null)
            ret[cnt++] = pTexCoord;
        else if(vfTexCoord != null)
            ret[cnt++] = vfTexCoord;

        if(pColor != null)
            ret[cnt++] = pColor;
        else if(vfColor != null)
            ret[cnt++] = vfColor;

        if(pFogCoord != null)
            ret[cnt++] = pFogCoord;
        else if(vfFogCoord != null)
            ret[cnt++] = vfFogCoord;

        return ret;
    }

    /**
     * Set the components the compose a geometry object. To clear all the
     * components, pass a null parameter.
     *
     * @param comps An array of geometric properties
     * @throws InvalidFieldValueException The node is not a known or supported
     *   field for this node
     */
    public void setComponents(VRMLNodeType[] comps)
        throws InvalidFieldValueException {

        if(comps == null) {
            pCoord = null;
            pColor = null;
            pNormal = null;
            pTexCoord = null;
            pFogCoord = null;

            vfCoord = null;
            vfColor = null;
            vfNormal = null;
            vfTexCoord = null;
            vfFogCoord = null;

            setCoordinateNode(null);
            setColorNode(null);
            setNormalNode(null);
            setTexCoordNode(null);
            setFogCoordinateNode(null);
        } else {

            VRMLProtoInstance proto;
            VRMLNodeType node;
            for(int i=0; i < comps.length; i++) {
                node = comps[i];

                if (node instanceof VRMLProtoInstance) {
                    proto = (VRMLProtoInstance) node;
                    node = proto.getImplementationNode();
                }
                else {
                    proto = null;
                }

                switch(node.getPrimaryType()) {
                    case TypeConstants.CoordinateNodeType:
                        pCoord = (VRMLProtoInstance) proto;
                        vfCoord = (VRMLCoordinateNodeType) node;
                        if(vfCoord != null)
                            vfCoord.addComponentListener(this);

                        changeFlags |= COORDS_CHANGED;
                        break;

                    case TypeConstants.NormalNodeType:
                        pNormal = (VRMLProtoInstance) proto;
                        vfNormal = (VRMLNormalNodeType) node;
                        if(vfNormal != null)
                            vfNormal.addComponentListener(this);

                        changeFlags |= NORMALS_CHANGED;
                        break;

                    case TypeConstants.TextureCoordinateNodeType:
                        pTexCoord = (VRMLProtoInstance) proto;
                        vfTexCoord = (VRMLTextureCoordinateNodeType) node;
                        if(vfTexCoord != null)
                            vfTexCoord.addComponentListener(this);

                        changeFlags |= TEXCOORDS_CHANGED;
                        break;

                    case TypeConstants.ColorNodeType:
                        pColor = (VRMLProtoInstance) proto;
                        vfColor = (VRMLColorNodeType) node;

                        if (vfColor != null) {
                            vfColor.addComponentListener(this);
                            if (!localColors)
                                fireLocalColorsChanged(true);
                            localColors = true;
                        } else {
                            if (localColors)
                                fireLocalColorsChanged(false);
                            localColors = false;
                        }

                        changeFlags |= COLORS_CHANGED;
                        break;

                    default:
                        pFogCoord = (VRMLProtoInstance) proto;
                        vfFogCoord = (VRMLGeometricPropertyNodeType) node;
                        if(vfFogCoord != null)
                            vfFogCoord.addComponentListener(this);

                        changeFlags |= FOG_CHANGED;
                        break;
                }
            }

            if(!inSetup)
                stateManager.addEndOfThisFrameListener(this);
        }
    }

    /**
     * Set a component that composes part of a geometry object.
     *
     * @param comp A geometric property
     * @throws InvalidFieldValueException The node is not a known or supported
     *   field for this node
     */
    public void setComponent(VRMLNodeType comp)
        throws InvalidFieldValueException {

        switch(comp.getPrimaryType()) {
            case TypeConstants.CoordinateNodeType:
                setComponent(FIELD_COORD, comp);
                break;

            case TypeConstants.NormalNodeType:
                setComponent(FIELD_NORMAL, comp);
                break;

            case TypeConstants.TextureCoordinateNodeType:
                setComponent(FIELD_TEXCOORD, comp);
                break;

            case TypeConstants.ColorNodeType:
                setComponent(FIELD_COLOR, comp);
                break;

            default:
                setComponent(FIELD_FOG_COORD, comp);
                break;
        }

        if(!inSetup)
            stateManager.addEndOfThisFrameListener(this);
    }

    /**
     * Check to see if the colors are per vertex or per face.
     *
     * @return true The colors are per vertex
     */
    public boolean hasColorPerVertex() {
        return vfColorPerVertex;
    }

    /**
     * Check to see if the normals are per vertex or per face.
     *
     * @return true The normals are per vertex
     */
    public boolean hasNormalPerVertex() {
        return vfNormalPerVertex;
    }

    /**
     * Check to see if this geometry implementation type requires unlit color
     * values to be set. For the most part this will always return false, but
     * some will need it (points and lines). This value should be constant for
     * the geometry regardless of whether a Color component has been provided
     * or not. It is up to the implementation to decide when to pass these
     * values on to the underlying rendering structures or not.
     * <p>
     *
     * The default implementation returns false. Override if different
     * behaviour is needed.
     *
     * @return true if we need unlit colour information
     */
    public boolean requiresUnlitColor() {
        return false;
    }

    /**
     * Set the local colour override for this geometry. Typically used to set
     * the emissiveColor from the Material node into the geometry for the line
     * and point-type geometries which are unlit in the X3D/VRML model.
     * <p>
     *
     * The default implementation does nothing. Override to do something useful.
     *
     * @param color The colour value to use
     */
    public void setUnlitColor(float[] color) {
    }

    //----------------------------------------------------------
    // Methods defined by VRMLNodeType
    //----------------------------------------------------------

    /**
     * Get the primary type of this node.  Replaces the instanceof mechanism
     * for use in switch statements.
     *
     * @return The primary type
     */
    public int getPrimaryType() {
        return TypeConstants.ComponentGeometryNodeType;
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

        if(pCoord != null)
            pCoord.setupFinished();
        else if (vfCoord != null)
            vfCoord.setupFinished();

        if(pColor != null)
            pColor.setupFinished();
        else if(vfColor != null)
            vfColor.setupFinished();

        if(pTexCoord != null)
            pTexCoord.setupFinished();
        else if(vfTexCoord != null)
            vfTexCoord.setupFinished();

        if(pNormal != null)
            pNormal.setupFinished();
        else if(vfNormal != null)
            vfNormal.setupFinished();

        if(pFogCoord != null)
            pFogCoord.setupFinished();
        else if (vfFogCoord != null)
            vfFogCoord.setupFinished();


        int num_kids = vfAttribs.size();
        VRMLNodeType kid;

        for(int i = 0; i < num_kids; i++) {
            kid = (VRMLNodeType)vfAttribs.get(i);

            // Make sure the child is finished first.
            kid.setupFinished();
        }
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

        fieldData.clear();

        switch(index) {
            case FIELD_SOLID:
                fieldData.booleanValue = vfSolid;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
                break;

            case FIELD_CCW:
                fieldData.booleanValue = vfCcw;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
                break;

            case FIELD_COLORPERVERTEX:
                fieldData.booleanValue = vfColorPerVertex;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
                break;

            case FIELD_NORMALPERVERTEX:
                fieldData.booleanValue = vfNormalPerVertex;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
                break;

            case FIELD_COORD:
                fieldData.clear();
                if(pCoord != null)
                    fieldData.nodeValue = pCoord;
                else
                    fieldData.nodeValue = vfCoord;

                fieldData.dataType = VRMLFieldData.NODE_DATA;
                break;

            case FIELD_COLOR:
                fieldData.clear();
                if(pColor != null)
                    fieldData.nodeValue = pColor;
                else
                    fieldData.nodeValue = vfColor;

                fieldData.dataType = VRMLFieldData.NODE_DATA;
                break;

            case FIELD_NORMAL:
                fieldData.clear();
                if (pNormal != null)
                    fieldData.nodeValue = pNormal;
                else
                    fieldData.nodeValue = vfNormal;

                fieldData.dataType = VRMLFieldData.NODE_DATA;
                break;

            case FIELD_TEXCOORD:
                fieldData.clear();
                if (pTexCoord != null)
                    fieldData.nodeValue = pTexCoord;
                else
                    fieldData.nodeValue = vfTexCoord;

                fieldData.dataType = VRMLFieldData.NODE_DATA;
                break;

            case FIELD_FOG_COORD:
                fieldData.clear();
                if(pFogCoord != null)
                    fieldData.nodeValue = pFogCoord;
                else
                    fieldData.nodeValue = vfFogCoord;

                fieldData.dataType = VRMLFieldData.NODE_DATA;
                break;

            case FIELD_ATTRIBS:
                VRMLNodeType kids[] = new VRMLNodeType[vfAttribs.size()];
                vfAttribs.toArray(kids);
                fieldData.clear();
                fieldData.nodeArrayValue = kids;
                fieldData.dataType = VRMLFieldData.NODE_ARRAY_DATA;
                fieldData.numElements = kids.length;
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
                case FIELD_COLOR:
                    if(pColor != null)
                        destNode.setValue(destIndex, pColor);
                    else
                        destNode.setValue(destIndex, vfColor);
                    break;

                case FIELD_COORD:
                    if(pCoord != null)
                        destNode.setValue(destIndex, pCoord);
                    else
                        destNode.setValue(destIndex, vfCoord);
                    break;

                case FIELD_NORMAL:
                    if(pNormal != null)
                        destNode.setValue(destIndex, pNormal);
                    else
                        destNode.setValue(destIndex, vfNormal);
                    break;

                case FIELD_TEXCOORD:
                    if(pTexCoord != null)
                        destNode.setValue(destIndex, pTexCoord);
                    else
                        destNode.setValue(destIndex, vfTexCoord);
                    break;

                case FIELD_FOG_COORD:
                    if(pFogCoord != null)
                        destNode.setValue(destIndex, pFogCoord);
                    else
                        destNode.setValue(destIndex, vfFogCoord);
                    break;

                case FIELD_ATTRIBS:
                    VRMLNodeType kids[] = new VRMLNodeType[vfAttribs.size()];
                    vfAttribs.toArray(kids);
                    destNode.setValue(destIndex, kids, vfAttribs.size());
                    break;

                case FIELD_SOLID:
                case FIELD_CCW:
                case FIELD_COLORPERVERTEX:
                case FIELD_NORMALPERVERTEX:
                    System.out.println("Cannot route initializeOnly fields");
                    break;

                default:
                    super.sendRoute(time, srcIndex, destNode, destIndex);
            }
        } catch(InvalidFieldException ife) {
            System.err.println("sendRoute: No field!" + ife.getFieldName());
        } catch(InvalidFieldValueException ifve) {
            System.err.println("sendRoute: Invalid field Value: " +
                ifve.getMessage());
        }
    }

    /**
     * Set the value of the field at the given index as an boolean. This would
     * be used to set SFBool field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not know
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
     */
    public void setValue(int index, boolean value)
        throws InvalidFieldException, InvalidFieldValueException {

        // Runtime semantics not yet implemented
        if(!inSetup)
            throw new InvalidFieldValueException(INIT_ONLY_WRITE_MSG);

        switch(index) {
            case FIELD_CCW:
                vfCcw = value;
                break;

            case FIELD_SOLID:
                vfSolid = value;
                break;

            case FIELD_NORMALPERVERTEX:
                vfNormalPerVertex = value;
                break;

            case FIELD_COLORPERVERTEX:
                vfColorPerVertex = value;
                break;

            default:
                super.setValue(index, value);
        }
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

// TODO:
// Need to check for vertex attributes here and route separately.
// Also need to add VRMLNodeType[] variant for the attribs field too.
        switch(index) {
            case FIELD_COORD:
            case FIELD_TEXCOORD:
            case FIELD_COLOR:
            case FIELD_NORMAL:
            case FIELD_FOG_COORD:
            case FIELD_ATTRIBS:
                setComponent(index, child);
                break;

            default:
                super.setValue(index, child);
        }
    }

    //-------------------------------------------------------------
    // Local methods
    //-------------------------------------------------------------

    /**
     * Notification of the coordinate node being set. If the passed value is
     * null then that clears the node. The node passed is the actual geometry,
     * not any proto wrapper, that will have been previously stripped. The
     * default implementation does nothing.
     *
     * @param node The node to use
     */
    protected void setCoordinateNode(VRMLCoordinateNodeType node) {
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
    }

    /**
     * Notification of the normal node being set. If the passed value is
     * null then that clears the node. The node passed is the actual normal,
     * not any proto wrapper, that will have been previously stripped. The
     * default implementation does nothing.
     *
     * @param node The node to use
     */
    protected void setNormalNode(VRMLNormalNodeType node) {
    }

    /**
     * Notification of the texture coordinate node being set. If the passed
     * value is null then that clears the node. The node passed is the actual
     * texCoord, not any proto wrapper, that will have been previously
     * stripped. The default implementation does nothing.
     *
     * @param node The node to use
     */
    protected void setTexCoordNode(VRMLTextureCoordinateNodeType node) {
    }

    /**
     * Notification of the coordinate node being set. If the passed value is
     * null then that clears the node. The node passed is the actual geometry,
     * not any proto wrapper, that will have been previously stripped. The
     * default implementation does nothing.
     *
     * @param node The node to use
     */
    protected void setFogCoordinateNode(VRMLGeometricPropertyNodeType node) {
    }


    //-------------------------------------------------------------
    // Local internal methods
    //-------------------------------------------------------------

    /**
     * Set a component that composes part of a geometry object.
     *
     * @param comp A geometric property
     * @throws InvalidFieldValueException The node is not a known or supported
     *   field for this node
     */
    private void setComponent(int field, VRMLNodeType comp)
        throws InvalidFieldValueException {
        VRMLProtoInstance proto;
        VRMLNodeType node = comp;

        if (node instanceof VRMLProtoInstance) {
            proto = (VRMLProtoInstance) node;
            node = proto.getImplementationNode();
        } else {
            proto = null;
        }

        switch(field) {
            case FIELD_COORD:
                if(vfCoord != null)
                    vfCoord.removeComponentListener(this);

                pCoord = (VRMLProtoInstance)proto;
                vfCoord = (VRMLCoordinateNodeType)node;
                if(vfCoord != null)
                    vfCoord.addComponentListener(this);

                changeFlags |= COORDS_CHANGED;
                setCoordinateNode(vfCoord);
                break;

            case FIELD_NORMAL:
                if(vfNormal != null)
                    vfNormal.removeComponentListener(this);

                pNormal = (VRMLProtoInstance)proto;
                vfNormal = (VRMLNormalNodeType)node;
                if(vfNormal != null)
                    vfNormal.addComponentListener(this);

                changeFlags |= NORMALS_CHANGED;
                setNormalNode(vfNormal);
                break;

            case FIELD_TEXCOORD:
                if(vfTexCoord != null)
                    vfTexCoord.removeComponentListener(this);

                pTexCoord = (VRMLProtoInstance)proto;
                vfTexCoord = (VRMLTextureCoordinateNodeType)node;
                if(vfTexCoord != null)
                    vfTexCoord.addComponentListener(this);

                changeFlags |= TEXCOORDS_CHANGED;
                setTexCoordNode(vfTexCoord);
                break;

            case FIELD_COLOR:
                if(vfColor != null)
                    vfColor.removeComponentListener(this);

                pColor = (VRMLProtoInstance)proto;
                vfColor = (VRMLColorNodeType)node;
                if (vfColor != null) {
                    vfColor.addComponentListener(this);
                    if (!localColors)
                        fireLocalColorsChanged(true);
                    localColors = true;
                } else {
                    if (localColors)
                        fireLocalColorsChanged(false);
                    localColors = false;
                }

                changeFlags |= COLORS_CHANGED;
                setColorNode(vfColor);
                break;

            default:
                if(vfFogCoord != null)
                    vfFogCoord.removeComponentListener(this);

                pFogCoord = (VRMLProtoInstance)proto;
                vfFogCoord = (VRMLGeometricPropertyNodeType)node;
                if(vfFogCoord != null)
                    vfFogCoord.addComponentListener(this);

                changeFlags |= FOG_CHANGED;
                setFogCoordinateNode(vfFogCoord);
                break;

        }

        if(!inSetup) {
            stateManager.addEndOfThisFrameListener(this);
            hasChanged[field] = true;
            fireFieldChanged(field);
        }
    }

    /**
     * Send the localColorsChanged event to LocalColorsListeners
     *
     * @param enabled Whether local colors are used.
     */
    protected void fireLocalColorsChanged(boolean enabled) {
        int size = localColorsListeners.size();
        LocalColorsListener l;

        boolean has_alpha = (vfColor != null) &&
                            (vfColor.getNumColorComponents() == 4);

        for(int i = 0; i < size; i++) {
            try {
                l = (LocalColorsListener)localColorsListeners.get(i);
                l.localColorsChanged(enabled, has_alpha);
            } catch(Exception e) {
                System.out.println("Error sending localColorsChanged message: "
                                   + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
