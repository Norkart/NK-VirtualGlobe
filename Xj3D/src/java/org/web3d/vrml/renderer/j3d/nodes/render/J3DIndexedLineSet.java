/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001-2004
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.j3d.nodes.render;

// External imports
import java.util.HashMap;
import java.util.ArrayList;

import javax.media.j3d.Geometry;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.IndexedLineStripArray;
import javax.media.j3d.SceneGraphObject;
import javax.media.j3d.TransparencyAttributes;
import javax.vecmath.Color3f;

// Local imports
import org.web3d.vrml.lang.*;

import org.web3d.vrml.nodes.VRMLColorNodeType;
import org.web3d.vrml.nodes.VRMLCoordinateNodeType;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLComponentGeometryNodeType;
import org.web3d.vrml.nodes.VRMLNodeComponentListener;
import org.web3d.vrml.nodes.VRMLProtoInstance;
import org.web3d.vrml.renderer.j3d.nodes.J3DIndexedGeometry;

/**
 * Java3D implementation of a VRML IndexedLineSet.
 * <p>
 *
 *
 * @author Justin Couch
 * @version $Revision: 1.21 $
 */
public class J3DIndexedLineSet extends J3DIndexedGeometry
    implements VRMLComponentGeometryNodeType,
               VRMLNodeComponentListener {

    /** Number of fields constant */
    private static final int NUM_FIELDS = LAST_INDEXEDGEOMETRY_INDEX + 1;


    // VRML Field declarations

    /** Array of VRMLFieldDeclarations */
    private static VRMLFieldDeclaration[] fieldDecl;

    /** Map between a field name and its index */
    private static HashMap fieldMap;

    /** Listing of field indexes that have nodes */
    private static int[] nodeFields;

    /** The impl for this class */
    private IndexedLineStripArray implGeom;

    /**
     * Static constructor builds the list of field declarations for this
     * node.
     */
    static {
        nodeFields = new int[] {
            FIELD_COORD,
            FIELD_COLOR
        };

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap(NUM_FIELDS*3);

        fieldDecl[FIELD_COLOR] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "color");
        fieldDecl[FIELD_COORD] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "coord");
        fieldDecl[FIELD_SET_COLORINDEX] =
            new VRMLFieldDeclaration(FieldConstants.EVENTIN,
                                     "MFInt32",
                                     "set_colorIndex");
        fieldDecl[FIELD_COLORINDEX] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "MFInt32",
                                     "colorIndex");
        fieldDecl[FIELD_COORDINDEX] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "MFInt32",
                                     "coordIndex");
        fieldDecl[FIELD_SET_COORDINDEX] =
            new VRMLFieldDeclaration(FieldConstants.EVENTIN,
                                     "MFInt32",
                                     "set_coordIndex");
        fieldDecl[FIELD_COLORPERVERTEX] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFBool",
                                     "colorPerVertex");


        Integer idx = new Integer(FIELD_COLOR);
        fieldMap.put("color", idx);
        fieldMap.put("set_color", idx);
        fieldMap.put("color_changed", idx);

        idx = new Integer(FIELD_COORD);
        fieldMap.put("coord", idx);
        fieldMap.put("set_coord", idx);
        fieldMap.put("coord_changed", idx);

        fieldMap.put("colorIndex",new Integer(FIELD_COLORINDEX));
        fieldMap.put("set_colorIndex",new Integer(FIELD_SET_COLORINDEX));

        fieldMap.put("colorPerVertex",new Integer(FIELD_COLORPERVERTEX));

        fieldMap.put("coordIndex",new Integer(FIELD_COORDINDEX));
        fieldMap.put("set_coordIndex",new Integer(FIELD_SET_COORDINDEX));
    }

    /**
     * Empty constructor
     */
    public J3DIndexedLineSet() {
        super("IndexedLineSet");

        hasChanged = new boolean[NUM_FIELDS];
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException Incorrect Node Type
     */
    public J3DIndexedLineSet(VRMLNodeType node) {
        this();

        checkNodeType(node);

        copy((VRMLComponentGeometryNodeType)node);
    }

    /**
     * Notification that the rendering of the event model is complete and that
     * rendering is about to begin. Used to update the j3d representation
     * only once per frame.
     */
    public void allEventsComplete() {
        buildImpl();
    }

    //-------------------------------------------------------------
    // Methods defined by J3DGeometryNodeType
    //-------------------------------------------------------------

    /**
     * Get the value of the creaseAngle field.
     *
     * @return The value of the create angle
     */
    public float getCreaseAngle() {
        return 0;
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
        return true; //vfNormalPerVertex;
    }

    /*
     * Returns a J3D Geometry collection that represents this piece of
     * geometry. If there is only one piece of geometry this will return
     * an array of lenght 1.
     *
     * @return The geometry needed to represent this object
     */
    public Geometry[] getGeometry() {
        return new Geometry[] { implGeom };
    }

    /**
     * Get the number of texture coordinate sets contained by this node
     *
     * @return the number of texture coordinate sets
     */
    public int getNumSets() {
        return 0;
    }

    /**
     * Get the texture coordinate generation mode.  NULL is returned
     * if the texture coordinates are not generated.
     *
     * @param setNum The set which this tex gen mode refers
     * @return The mode or NULL
     */
    public String getTexCoordGenMode(int setNum) {
        return null;
    }

    //----------------------------------------------------------
    // Methods defined by VRMLComponentGeometryNodeType
    //----------------------------------------------------------

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

    //-------------------------------------------------------------
    // Methods defined by VRMLNodeComponentListener
    //-------------------------------------------------------------

    /**
     * Notification that the field from the node has changed. Ignored by this
     * implementation, but required due to base class.
     *
     * @param node The component node that changed
     * @param index The index of the field that has changed
     */
    public void fieldChanged(VRMLNodeType node, int index) {
        stateManager.addEndOfThisFrameListener(this);
    }

    //----------------------------------------------------------
    // Methods defined by J3DVRMLNodeTypeType
    //----------------------------------------------------------

    /**
     * Get the Java3D scene graph object representation of this node. This will
     * need to be cast to the appropriate parent type when being used.
     *
     * @return The J3D representation.
     */
    public SceneGraphObject getSceneGraphObject() {
        return implGeom;
    }

    /**
     * Notify a node that an ExternProto has resolved.  This will verify the objects
     * type and add it to the render sceneGraph.
     *
     * @param index The field index
     * @throws InvalidFieldValueException If the proto contains he wrong type
     */
     public void notifyExternProtoLoaded(int index, VRMLNodeType node)
        throws InvalidFieldValueException {

        if (inSetup)
            return;

        switch(index) {
            case FIELD_COLOR:
                int[] alt_type = node.getSecondaryType();
                boolean found_type = false;

                for(int i = 0; i < alt_type.length && !found_type; i++) {
                    if(alt_type[i] == TypeConstants.ColorNodeType)
                        found_type = true;
                }

                if(!found_type)
                    throw new InvalidFieldValueException(COLOR_PROTO_MSG);

                if(!inSetup)
                    stateManager.addEndOfThisFrameListener(this);

                break;

            case FIELD_COORD:
                alt_type = node.getSecondaryType();
                found_type = false;

                for(int i = 0; i < alt_type.length && !found_type; i++) {
                    if(alt_type[i] == TypeConstants.CoordinateNodeType)
                        found_type = true;
                }

                if(!found_type)
                    throw new InvalidFieldValueException(COORD_PROTO_MSG);

                if(!inSetup)
                    stateManager.addEndOfThisFrameListener(this);

                break;

            default:
                System.out.println("J3DIndexedLineSet: Unknown field for notifyExternProtoLoaded");
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

    //----------------------------------------------------------
    // Methods defined by VRMLNodeType
    //----------------------------------------------------------

    /**
     * Get the index of the given field name. If the name does not exist for
     * this node then return a value of -1.
     *
     * @param fieldName The name of the field we want the index from
     * @return The index of the field name or -1
     */
    public int getFieldIndex(String fieldName) {
        Integer index = (Integer) fieldMap.get(fieldName);

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
     * Get the declaration of the field at the given index. This allows for
     * reverse lookup if needed. If the field does not exist, this will give
     * a value of null.
     *
     * @param index The index of the field to get information
     * @return A representation of this field's information
     */
    public VRMLFieldDeclaration getFieldDeclaration(int index) {
        if (index < 0 || index > LAST_INDEXEDGEOMETRY_INDEX) {
            return null;
        }

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
     * Get the primary type of this node.  Replaces the instanceof mechanism
     * for use in switch statements.
     *
     * @return The primary type
     */
    public int getPrimaryType() {
        return TypeConstants.ComponentGeometryNodeType;
    }

    /**
     * Set the value of the field at the given index as a node. This would be
     * used to set SFNode field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not know
     * @throws InvalidFieldValueException The node does not match the required
     *    type.
     */
    public void setValue(int index, VRMLNodeType child)
        throws InvalidFieldException, InvalidFieldValueException {

        VRMLNodeType node;

        node = child;
        switch(index) {
            case FIELD_COORD :
                if(child instanceof VRMLProtoInstance) {
                    pCoord = (VRMLProtoInstance) child;
                    node = pCoord.getImplementationNode();

                    if(node == null)
                        return;
                    else if(!(node instanceof VRMLCoordinateNodeType)) {
                        pCoord = null;
                        throw new InvalidFieldValueException(COORD_PROTO_MSG);
                    }
                } else if(!(node instanceof VRMLCoordinateNodeType)) {
                    throw new InvalidFieldValueException(COORD_NODE_MSG);
                } else {
                    pCoord = null;
                }

                vfCoord = (VRMLCoordinateNodeType)node;
                if (vfCoord != null)
                    vfCoord.addComponentListener(this);

                if (!inSetup)
                    stateManager.addEndOfThisFrameListener(this);
                break;
            case FIELD_COLOR :
                if(child instanceof VRMLProtoInstance) {
                    pColor = (VRMLProtoInstance) child;
                    node = pColor.getImplementationNode();

                    if(node == null)
                        return;
                    else if(!(node instanceof VRMLColorNodeType)) {
                        pColor = null;
                        throw new InvalidFieldValueException(COLOR_PROTO_MSG);
                    }
                } else if(!(node instanceof VRMLColorNodeType)) {
                    throw new InvalidFieldValueException(COLOR_NODE_MSG);
                } else {
                    pColor = null;
                }

                vfColor = (VRMLColorNodeType) node;
                if(vfColor != null)
                    vfColor.addComponentListener(this);

                if(!inSetup)
                    stateManager.addEndOfThisFrameListener(this);
                break;

            default:
                super.setValue(index, child);
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
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_COORDINDEX:
            case FIELD_COLORINDEX:
                super.setValue(index, value, numValid);
                break;

            case FIELD_SET_COORDINDEX:
            case FIELD_SET_COLORINDEX:
                super.setValue(index, value, numValid);
                stateManager.addEndOfThisFrameListener(this);
                break;

            default:
                super.setValue(index, value, numValid);
        }
    }

    //----------------------------------------------------------
    // Methods required by VRMLComponentGeometryNodeType
    //----------------------------------------------------------

    /**
     * Get the components that compose a geometry object.
     * <p>
     * If there are no components then a zero length array will be returned.
     * @return VRMLNodeType[] The components
     */
    public VRMLNodeType[] getComponents() {

        int cnt = 2;
        if(vfCoord == null && pCoord == null)
            cnt--;

        if(vfColor == null && pColor == null)
            cnt--;

        VRMLNodeType[] ret = new VRMLNodeType[cnt];

        cnt=0;
        if (pCoord != null)
            ret[cnt++] = pCoord;
        else if (vfCoord != null)
            ret[cnt++] = vfCoord;

        if (pColor != null)
            ret[cnt++] = pColor;
        else if (vfColor != null)
            ret[cnt++] = vfColor;

        return ret;
    }

    /**
     * Set the components that compose a geometry object.
     *
     * @param comps An array of geometric properties
     * @throws InvalidFieldValueException The node does not match the required
     *    type.
     */
    public void setComponents(VRMLNodeType[] comps)
        throws InvalidFieldValueException {

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
                    if (vfCoord != null)
                        vfCoord.addComponentListener(this);
                    break;
                case TypeConstants.ColorNodeType:
                    pColor = (VRMLProtoInstance) proto;
                    vfColor = (VRMLColorNodeType) node;
                    if (vfColor != null)
                        vfColor.addComponentListener(this);
                    break;
                default: throw new
                    InvalidFieldValueException("Unknown component type");
            }
        }

        if (!inSetup)
            stateManager.addEndOfThisFrameListener(this);
    }

    /**
     * Set a component that composes part of a geometry object.
     *
     * @param comp A geometric property
     * @throws InvalidFieldValueException The node does not match the required
     *    type.
     */
    public void setComponent(VRMLNodeType comp)  throws
        InvalidFieldValueException {

        VRMLProtoInstance proto;
        VRMLNodeType node = comp;

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
                if (vfCoord != null)
                    vfCoord.addComponentListener(this);
                break;
            case TypeConstants.ColorNodeType:
                pColor = (VRMLProtoInstance) proto;
                vfColor = (VRMLColorNodeType) node;
                if (vfColor != null)
                    vfColor.addComponentListener(this);
                break;
            default: throw new
                InvalidFieldValueException("Unknown component type");
        }

        if (!inSetup)
            stateManager.addEndOfThisFrameListener(this);    }

    //----------------------------------------------------------
    // Methods internal to J3DIndexedLineSet
    //----------------------------------------------------------

    /**
     * Build the impl for this geometry
     */
    private void buildImpl() {
        int num_items = 0;

        if(vfCoord != null) {
            num_items = vfCoord.getNumPoints();

            if(num_items == 0)
                return;

            lfCoord = vfCoord.getPointRef();

            vfCoord.addComponentListener(this);
        } else {
            // No coordinates so nothing to do
            return;
        }

        int vertexFormat = GeometryArray.COORDINATES | GeometryArray.NORMALS;
        countIndex();

        if(vfColor != null) {
            num_items = vfColor.getNumColors();
            lfColor = new float[num_items];
            vfColor.getColor(lfColor);
        }

        if(numPieces == 0)
            return;

        pieceSizes = new int[numPieces];
        lfCoordIndex = new int[numIndices];

        fillImplArrays(vfCoordIndex, numCoordIndex, pieceSizes, lfCoordIndex);
        lfColorIndex = setupIndex(vfColorIndex, numColorIndex, vfColorPerVertex);

        if(vfColor != null)
            vertexFormat |= GeometryArray.COLOR_3;

        implGeom = new IndexedLineStripArray(lfCoord.length,
                                             vertexFormat,
                                             numIndices,
                                             pieceSizes);

        implGeom.setCoordinates(0, lfCoord, 0, lfCoord.length / 3);
        implGeom.setCoordinateIndices(0, lfCoordIndex);

        float[] lfNormals = new float[] {1,0,0};
        // Initial value will be all zeros
        int[] lfNormalIndex = new int[lfCoordIndex.length];

        implGeom.setNormals(0, lfNormals);
        implGeom.setNormalIndices(0, lfNormalIndex);

        if(vfColor != null) {
            implGeom.setColors(0, lfColor);
            implGeom.setColorIndices(0, lfColorIndex);
        } else {
            // Allow the Shape node to write color
            implGeom.setCapability(IndexedLineStripArray.ALLOW_COLOR_WRITE);
            implGeom.setCapability(IndexedLineStripArray.ALLOW_COLOR_INDEX_WRITE);
        }

        if(!inSetup)
            fireGeometryChanged(null);

        // Cleanup variables
        lfCoord = null;
        lfCoordIndex = null;
        lfColor = null;
        lfColorIndex = null;
        pieceSizes = null;
    }
}
