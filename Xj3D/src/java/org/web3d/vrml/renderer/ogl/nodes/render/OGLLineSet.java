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

package org.web3d.vrml.renderer.ogl.nodes.render;

// External imports
import org.j3d.aviatrix3d.Geometry;
import org.j3d.aviatrix3d.NodeUpdateListener;
import org.j3d.aviatrix3d.LineStripArray;
import org.j3d.aviatrix3d.SceneGraphObject;

// Local imports
import org.web3d.vrml.lang.TypeConstants;
import org.web3d.vrml.lang.InvalidFieldValueException;
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLCoordinateNodeType;
import org.web3d.vrml.nodes.VRMLColorNodeType;

import org.web3d.vrml.renderer.ogl.nodes.OGLGeometryNodeType;

import org.web3d.vrml.renderer.common.nodes.render.BaseLineSet;

/**
 * OpenGL implementation of an LineSet.
 * <p>
 *
 * The point set directly maps to Aviatrix3D's LineArray class. When the
 * coordinates change to a different length than the current set, it will
 * notify the geometry listener to fetch the new information.
 * <p>
 * If the VRML file did not provide a Coordinate node, then this class will
 * not present any geometry from the {@link #getGeometry()} or
 * {@link #getSceneGraphObject()} calls. If the user later specifies the
 * renderety through an event, the listener(s) will be notified.
 * <p>
 * In this implementation, if the length of the color array is shorter that
 * the length of the coordinate array, colors will be ignored.
 *
 * @author Justin Couch
 * @version $Revision: 1.12 $
 */
public class OGLLineSet extends BaseLineSet
    implements OGLGeometryNodeType,
               NodeUpdateListener {

    /** The impl for this class */
    private LineStripArray implGeom;

    /** temp array to copy values from the color field to the geometry */
    private float[] tmpColors;

    /** Flag indicating coordinates changed this last time */
    private boolean coordsChanged;

    /** Flag indicating strip count changed this last time */
    private boolean stripsChanged;

    /** Holder for the unlit line color. Only assigned when needed. */
    private float[] unlitColor;

    /**
     * Construct a new point set instance that contains no child nodes.
     */
    public OGLLineSet() {
        init();
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException Incorrect Node Type
     */
    public OGLLineSet(VRMLNodeType node) {
        super(node);

        init();
    }

    //-------------------------------------------------------------
    // Methods defined by OGLGeometryNodeType
    //-------------------------------------------------------------

    /*
     * Returns a OGL Geometry collection that represents this piece of
     * geometry. If there is only one piece of geometry this will return
     * an array of lenght 1.
     *
     * @return The geometry needed to represent this object
     */
    public Geometry getGeometry() {
        return implGeom;
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
        return true;
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
        changeFlags |= UNLIT_COLORS_CHANGED;

        if(unlitColor == null)
            unlitColor = new float[3];

        unlitColor[0] = color[0];
        unlitColor[1] = color[1];
        unlitColor[2] = color[2];

        if(implGeom.isLive())
            implGeom.dataChanged(this);
        else
            updateNodeDataChanges(implGeom);
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
        return implGeom;
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
                    throw new InvalidFieldValueException(BAD_PROTO_MSG);

                if (implGeom.isLive())
                    implGeom.dataChanged(this);
                else
                    updateNodeDataChanges(implGeom);
                break;

            case FIELD_COORD:
                if(node.getPrimaryType() != TypeConstants.CoordinateNodeType)
                    throw new InvalidFieldValueException(BAD_PROTO_MSG);

                if (implGeom.isLive())
                    implGeom.boundsChanged(this);
                else
                    updateNodeBoundsChanges(implGeom);
                break;

            default:
                System.out.println("OGLLineSet: Unknown field for notifyExternProtoLoaded");
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

        int num_colors = (vfColor == null) ? 0 : vfColor.getNumColors();

        tmpColors = new float[num_colors];

        // Cheat and call directly
        coordsChanged = true;
        stripsChanged = true;
        updateNodeBoundsChanges(null);
        updateNodeDataChanges(null);
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
        int field;
        VRMLFieldData data;

        if(node instanceof VRMLCoordinateNodeType) {
            coordsChanged = true;

            if (implGeom.isLive())
                implGeom.boundsChanged(this);
            else
                updateNodeBoundsChanges(implGeom);
        } else {
            if (implGeom.isLive())
                implGeom.dataChanged(this);
            else
                updateNodeDataChanges(implGeom);
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

        coordsChanged = true;
        if (implGeom.isLive())
            implGeom.boundsChanged(this);
        else
            updateNodeBoundsChanges(implGeom);
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

        if (implGeom.isLive())
            implGeom.dataChanged(this);
        else
            updateNodeDataChanges(implGeom);
    }

    //----------------------------------------------------------
    // Methods defined by NodeUpdateListener
    //----------------------------------------------------------

    /**
     * Notification that its safe to update the node now with any operations
     * that could potentially effect the node's bounds.
     *
     * @param src The node or Node Component that is to be updated.
     */
    public void updateNodeBoundsChanges(Object src) {

        if(vfCoord == null) {
            implGeom.setValidVertexCount(0);
        } else {
            if(coordsChanged) {
                int num_points = 0;

                for(int i = 0; i < numVertexCount; i++)
                    num_points += vfVertexCount[i];

                float[] tmp = vfCoord.getPointRef();

                implGeom.setVertices(LineStripArray.COORDINATE_3,
                                     tmp,
                                     num_points);

                coordsChanged = false;
            }

            if(stripsChanged) {
                implGeom.setStripCount(vfVertexCount, numVertexCount);
                stripsChanged = false;
            }
        }
    }

    /**
     * Notification that its safe to update the node now with any operations
     * that only change the node's properties, but do not change the bounds.
     *
     * @param src The node or Node Component that is to be updated.
     */
    public void updateNodeDataChanges(Object src) {
        if(vfColor == null) {
            implGeom.setSingleColor(false, unlitColor);
        } else {
            boolean alpha = (vfColor.getNumColorComponents() == 4);
            int num_colors = vfColor.getNumColors();

            if(tmpColors.length < num_colors)
                tmpColors = new float[num_colors];

            vfColor.getColor(tmpColors);

            implGeom.setColors(alpha, tmpColors);
        }
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Set the value of the vertexCount field.
     *
     * @param counts The list of counts provided
     * @throw InvalidFieldValueException One or more values were < 2
     */
    protected void setVertexCount(int[] counts, int numValid)
        throws InvalidFieldValueException {

        super.setVertexCount(counts, numValid);

        if(!inSetup) {
            stripsChanged = true;
            if (implGeom.isLive())
                implGeom.boundsChanged(this);
            else
                updateNodeBoundsChanges(implGeom);
        }
    }

    /**
     * Common initialisation functionality.
     */
    private void init() {
        implGeom = new LineStripArray();

        stripsChanged = false;
        coordsChanged = false;
    }
}
