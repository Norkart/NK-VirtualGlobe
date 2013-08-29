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
import org.j3d.aviatrix3d.VertexGeometry;
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

import org.web3d.vrml.renderer.common.nodes.render.BaseIndexedLineSet;

/**
 * OpenGL implementation of an IndexedLineSet.
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
 * @version $Revision: 1.17 $
 */
public class OGLIndexedLineSet extends BaseIndexedLineSet
    implements OGLGeometryNodeType,
               NodeUpdateListener {

    /** The impl for this class */
    private LineStripArray implGeom;

    /** temp array to copy values from the coordinates field to the geometry */
    private float[] tmpCoords;

    /** temp array to copy values from the color field to the geometry */
    private float[] tmpColors;

    /** The strip counts needed to create the line arrays */
    private int[] stripCounts;

    /** The number of strips in the strip counts array */
    private int numStripCounts;

    /** The de-indexed version of the coordinates */
    private float[] lfCoords;

    /** Number of valid coordinates in the unindexed form */
    private int numCoords;

    /** The de-indexed version of the color */
    private float[] lfColors;

    /** Number of valid colors in the unindexed form */
    private int numColors;

    /** Holder for the unlit line color. Only assigned when needed. */
    private float[] unlitColor;

    /** Flag indicating if there are 3 or 4 component colour */
    private boolean hasAlpha;

    /**
     * Construct a new point set instance that contains no child nodes.
     */
    public OGLIndexedLineSet() {
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
    public OGLIndexedLineSet(VRMLNodeType node) {
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

                if(implGeom.isLive())
                    implGeom.dataChanged(this);
                else
                    updateNodeDataChanges(implGeom);
                break;

            case FIELD_COORD:
                if(node.getPrimaryType() != TypeConstants.CoordinateNodeType)
                    throw new InvalidFieldValueException(BAD_PROTO_MSG);

                if(implGeom.isLive())
                    implGeom.boundsChanged(this);
                else
                    updateNodeBoundsChanges(implGeom);
                break;

            default:
                System.out.println("OGLIndexedLineSet: Unknown field for notifyExternProtoLoaded");
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

        int num_coords = (vfCoord == null) ? 0 : vfCoord.getNumPoints();
        int num_colors = (vfColor == null) ? 0 : vfColor.getNumColors();

        tmpCoords = new float[num_coords];
        tmpColors = new float[num_colors];

        // Cheat and call directly
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
        if(!inSetup) {
            if(node == vfCoord)
                changeFlags |= COORDS_CHANGED;
            else if(node == vfColor)
                changeFlags |= COLORS_CHANGED;
            else if(node == vfNormal)
                changeFlags |= NORMALS_CHANGED;
            else if(node == vfTexCoord)
                changeFlags |= TEXCOORDS_CHANGED;
            else
                System.out.println("BaseIndexedFaceSet: Unknown field fieldChanged");

            stateManager.addEndOfThisFrameListener(this);
        }
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
        if(!inSetup)
            stateManager.addEndOfThisFrameListener(this);
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
        if(!inSetup)
            stateManager.addEndOfThisFrameListener(this);
    }

    /**
     * Notification that the rendering of the event model is complete and that
     * rendering is about to begin. Used to update the j3d representation
     * only once per frame.
     */
    public void allEventsComplete() {
        buildImpl();
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
            implGeom.setVertices(LineStripArray.COORDINATE_3,
                                 lfCoords,
                                 numCoords / 3);

            implGeom.setStripCount(stripCounts, numStripCounts);
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
            implGeom.setColors(hasAlpha, lfColors);
        }
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Set the coordIndex field. Override to provide.renderer-specific behaviour,
     * but remember to also call this implementation too.
     *
     * @param node The node to use
     */
    protected void setCoordIndex(int[] value, int numValid) {
        super.setCoordIndex(value, numValid);

        if(!inSetup)
            stateManager.addEndOfThisFrameListener(this);
    }

    /**
     * Set the colorIndex field. Override to provide.renderer-specific behaviour,
     * but remember to also call this implementation too.
     *
     * @param value The new value
     */
    protected void setColorIndex(int[] value, int numValid) {
        super.setColorIndex(value, numValid);

        if(!inSetup)
            stateManager.addEndOfThisFrameListener(this);
    }

    /**
     * Common initialisation functionality.
     */
    private void init() {
        implGeom = new LineStripArray(true, VertexGeometry.VBO_HINT_STATIC);
        hasAlpha = false;
    }

    /**
     * Rebuild the internal arrays ready to be copied into the Aviatrix3D node
     * during the appropriate callback.
     */
    private void buildImpl() {

        if(((changeFlags & COORDS_INDEX_CHANGED) != 0) ||
           ((changeFlags & COORDS_CHANGED) != 0)) {

			// Start by fetching the raw info from the component nodes
			int num_items = vfCoord.getNumPoints();

			if (num_items < VBO_MIN_VERTICES)
				implGeom.setVBOEnabled(false);

            if((changeFlags & COORDS_CHANGED) != 0)
                tmpCoords = vfCoord.getPointRef();

            int num_strips = 0;

            for(int i = 0; i < numCoordIndex; i++) {
                if(vfCoordIndex[i] == -1)
                    num_strips++;
            }

            int num_flat_coords = numCoordIndex - num_strips;

            if((numCoordIndex != 0) && (vfCoordIndex[numCoordIndex - 1] != -1))
                num_strips++;


            if((stripCounts == null) || (stripCounts.length < num_strips))
                stripCounts = new int[numCoordIndex];
            else  {
                for(int i=0; i < stripCounts.length; i++) {
                    stripCounts[i] = 0;
                }
            }

            if((lfCoords == null) || (lfCoords.length < num_flat_coords * 3))
                lfCoords = new float[num_flat_coords * 3];

            int strip_idx = 0;
            int vtx_idx = 0;

            for(int i = 0; i < numCoordIndex; i++) {
                if(vfCoordIndex[i] == -1) {
                    // Hide a separate comparison here so that we only
                    // increment when the previous count was not 0. This saves
                    // putting in zero-length strips, which aviatrix does not
                    // like. Zero length strips happen when the user does
                    // something like coordIndex [ 0 1 -1 -1 0 2 ]
                    if(stripCounts[strip_idx] != 0)
                        strip_idx++;
                    else {
                        num_strips--;
                    }
                } else {
                    stripCounts[strip_idx]++;
                    int idx = vfCoordIndex[i] * 3;
                    lfCoords[vtx_idx++] = tmpCoords[idx++];
                    lfCoords[vtx_idx++] = tmpCoords[idx++];
                    lfCoords[vtx_idx++] = tmpCoords[idx];
                }
            }

            numCoords = vtx_idx;
            numStripCounts = num_strips;

            if(implGeom.isLive())
                implGeom.boundsChanged(this);
            else
                updateNodeBoundsChanges(implGeom);
        }

        if(((changeFlags & COLORS_INDEX_CHANGED) != 0) ||
           ((changeFlags & COLORS_CHANGED) != 0)) {

            if((changeFlags & COLORS_CHANGED) != 0) {
                numColors = vfColor.getNumColors();

                if(tmpColors.length < numColors)
                    tmpColors = new float[numColors];

                vfColor.getColor(tmpColors);
            }

            hasAlpha = (vfColor.getNumColorComponents() == 4);
            int num_flat_colors = numCoordIndex - numStripCounts + 1;

            num_flat_colors *= hasAlpha ? 4 : 3;

            if((lfColors == null) || (lfColors.length < num_flat_colors))
                lfColors = new float[num_flat_colors];

            int vtx_idx = 0;

            if(vfColorPerVertex) {
                if(numColorIndex == 0) {
                    if(!hasAlpha) {
                        for(int i = 0; i < numCoordIndex; i++) {
                            if(vfCoordIndex[i] == -1)
                                continue;

                            int idx = vfCoordIndex[i] * 3;
                            lfColors[vtx_idx++] = tmpColors[idx++];
                            lfColors[vtx_idx++] = tmpColors[idx++];
                            lfColors[vtx_idx++] = tmpColors[idx];
                        }
                    } else {
                        for(int i = 0; i < numCoordIndex; i++) {
                            if(vfCoordIndex[i] == -1)
                                continue;

                            int idx = vfCoordIndex[i] * 4;
                            lfColors[vtx_idx++] = tmpColors[idx++];
                            lfColors[vtx_idx++] = tmpColors[idx++];
                            lfColors[vtx_idx++] = tmpColors[idx++];
                            lfColors[vtx_idx++] = tmpColors[idx];
                        }
                    }
                } else {
                    if(!hasAlpha) {
                        for(int i = 0; i < numColorIndex; i++) {
                            if(vfColorIndex[i] == -1)
                                continue;

                            int idx = vfColorIndex[i] * 3;
                            lfColors[vtx_idx++] = tmpColors[idx++];
                            lfColors[vtx_idx++] = tmpColors[idx++];
                            lfColors[vtx_idx++] = tmpColors[idx];
                        }
                    } else {
                        for(int i = 0; i < numColorIndex; i++) {
                            if(vfColorIndex[i] == -1)
                                continue;

                            int idx = vfColorIndex[i] * 4;
                            lfColors[vtx_idx++] = tmpColors[idx++];
                            lfColors[vtx_idx++] = tmpColors[idx++];
                            lfColors[vtx_idx++] = tmpColors[idx++];
                            lfColors[vtx_idx++] = tmpColors[idx];
                        }
                    }
                }
            } else {
                if(numColorIndex == 0) {
                    // Colour per vertex is false, and there are no
                    // colour indices, so just copy the same colour value to
                    // each index until we get to the end of this line, then
                    // move to the next colour.
                    if(!hasAlpha) {
                        int idx = 0;
                        for(int i = 0; i < numCoordIndex; i++) {
                            if(vfCoordIndex[i] == -1) {
                                idx += 3;
                                continue;
                            }

                            lfColors[vtx_idx++] = tmpColors[idx];
                            lfColors[vtx_idx++] = tmpColors[idx + 1];
                            lfColors[vtx_idx++] = tmpColors[idx + 2];
                        }
                    } else {
                        int idx = 0;

                        for(int i = 0; i < numCoordIndex; i++) {
                            if(vfCoordIndex[i] == -1) {
                                idx += 4;
                                continue;
                            }

                            lfColors[vtx_idx++] = tmpColors[idx];
                            lfColors[vtx_idx++] = tmpColors[idx + 1];
                            lfColors[vtx_idx++] = tmpColors[idx + 2];
                            lfColors[vtx_idx++] = tmpColors[idx + 3];
                        }
                    }
                } else {
                    if(!hasAlpha) {
                        // Colour per vertex is false, one colour is used
                        // for each polyline of the IndexedLineSet

                        int idxx = 0;
                        int idx = 0;
                        for(int i = 0; i < numCoordIndex; i++) {
                            if(vfCoordIndex[i] == -1){
                                // next polyline -> next indexcolor
                                idxx ++;
                                continue;
                            }
                            idx = vfColorIndex[idxx] * 3;

                            lfColors[vtx_idx++] = tmpColors[idx];
                            lfColors[vtx_idx++] = tmpColors[idx+1];
                            lfColors[vtx_idx++] = tmpColors[idx+2];
                         }
                    } else {
                        int idxx = 0;
                        int idx = 0;
                        for(int i = 0; i < numCoordIndex; i++) {
                            if(vfCoordIndex[i] == -1){
                                // next polyline -> next indexcolor
                                idxx ++;
                                continue;
                            }
                            idx = vfColorIndex[idxx] * 4;

                            lfColors[vtx_idx++] = tmpColors[idx];
                            lfColors[vtx_idx++] = tmpColors[idx+1];
                            lfColors[vtx_idx++] = tmpColors[idx+2];
                            lfColors[vtx_idx++] = tmpColors[idx+3];
                         }
                    }
                }
            }

            if(implGeom.isLive())
                implGeom.dataChanged(this);
            else
                updateNodeDataChanges(implGeom);
        }

        changeFlags = 0;
    }
}

