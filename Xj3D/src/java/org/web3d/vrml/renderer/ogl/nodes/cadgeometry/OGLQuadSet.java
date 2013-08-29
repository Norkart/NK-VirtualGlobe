/*****************************************************************************
 *                        Web3d.org Copyright (c) 2004 - 2005
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.ogl.nodes.cadgeometry;

// External imports
import org.j3d.aviatrix3d.Geometry;
import org.j3d.aviatrix3d.NodeUpdateListener;
import org.j3d.aviatrix3d.QuadArray;
import org.j3d.aviatrix3d.SceneGraphObject;

// Local imports
import org.web3d.vrml.nodes.*;

import org.web3d.vrml.lang.TypeConstants;
import org.web3d.vrml.lang.InvalidFieldValueException;

import org.web3d.vrml.renderer.ogl.nodes.OGLGeometryNodeType;
import org.web3d.vrml.renderer.common.nodes.cadgeometry.BaseQuadSet;

/**
 * OpenGL implementation of a QuadSet.
 * <p>
 *
 * The point set directly maps to Aviatrix3D's QuadArray class. When the
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
 * @author Alan Hudson
 * @version $Revision: 1.7 $
 */
public class OGLQuadSet extends BaseQuadSet
    implements OGLGeometryNodeType,
               NodeUpdateListener {

    /** The impl for this class */
    private QuadArray implGeom;

    /** temp array to copy values from the color field to the geometry */
    private float[] tmpColors;

    /** temp array to copy values from the texCoord field to the geometry */
    private float[][] tmpTexCoords;

    /** temp array to copy values from the texCoord field to the geometry */
    private int[] tmpTexSets;

    /** temp array to copy values from the texCoord field to the geometry */
    private int[] tmpTexTypes;

    /** Flag to indicate the colors changed */
    private boolean colorChanged;

    /** Flag to indicate the texture coords changed */
    private boolean texCoordChanged;

    /** Flag to indicate the normals changed */
    private boolean normalChanged;

    /**
     * Construct a new point set instance that contains no child nodes.
     */
    public OGLQuadSet() {
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
    public OGLQuadSet(VRMLNodeType node) {
        super(node);

        init();
    }

    //----------------------------------------------------------
    // Methods defined by FrameStateManagerListener
    //----------------------------------------------------------

    /**
     * Notification that the rendering of the event model is complete and that
     * rendering is about to begin. Used to update the scene graph with the
     * loaded scene structure at the end of the frame to avoid issues with
     * multiple access to the scen graph.
     */
    public void allEventsComplete() {
        if((changeFlags & COORDS_CHANGED) != 0) {
            if(implGeom.isLive())
                implGeom.boundsChanged(this);
            else
                updateNodeBoundsChanges(implGeom);
        }

        if(((changeFlags & COLORS_CHANGED) != 0) ||
           ((changeFlags & NORMALS_CHANGED) != 0) ||
           ((changeFlags & TEXCOORDS_CHANGED) != 0))
         {
            if(implGeom.isLive())
                implGeom.dataChanged(this);
            else
                updateNodeDataChanges(implGeom);
        }

        changeFlags = 0;
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
        int ret_val = 0;

        if(vfTexCoord != null)
            ret_val = vfTexCoord.getNumSets();

        return ret_val;
    }

    /**
     * Get the texture coordinate generation mode.  NULL is returned
     * if the texture coordinates are not generated.
     *
     * @param setNum The set which this tex gen mode refers
     * @return The mode or NULL
     */
    public String getTexCoordGenMode(int setNum) {
        String ret_val = null;

        if(vfTexCoord != null)
            ret_val = vfTexCoord.getTexCoordGenMode(setNum);

        return ret_val;
    }

    //----------------------------------------------------------
    // Methods defined by OGLVRMLNode
    //----------------------------------------------------------

    /**
     * Get the OGL scene graph object representation of this node. This will
     * need to be cast to the appropriate parent type when being used.
     *
     * @return The OGL representation.
     */
    public SceneGraphObject getSceneGraphObject() {
        return implGeom;
    }

    /**
     * Notify a node that an ExternProto has resolved. This will verify the
     * objects type and add it to the rendered scene graph.
     *
     * @param index The field index
     * @throws InvalidFieldValueException If the proto contains he wrong type
     */
    public synchronized void notifyExternProtoLoaded(int index,
                                                     VRMLNodeType node)
        throws InvalidFieldValueException {

        if(inSetup)
            return;

        super.setComponent(node);
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
        int num_normals = (vfNormal == null) ? 0 : vfNormal.getNumNormals();

        int num_tex_sets = (vfTexCoord == null) ? 0 : vfTexCoord.getNumSets();

        tmpTexCoords = new float[num_tex_sets][];
        tmpTexSets = new int[num_tex_sets];
        tmpTexTypes = new int[num_tex_sets];

        for(int i = 0; i < num_tex_sets; i++)
            tmpTexCoords[i] = new float[vfTexCoord.getSize(i)];

        tmpColors = new float[num_colors];

        colorChanged = true;
        normalChanged = true;
        texCoordChanged = true;

        // Cheat and call directly
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

        if(node instanceof VRMLCoordinateNodeType)
            if (implGeom.isLive())
                implGeom.boundsChanged(this);
            else
                updateNodeBoundsChanges(implGeom);
        else if(node instanceof VRMLNormalNodeType) {
            normalChanged = true;
            if (implGeom.isLive())
                implGeom.dataChanged(this);
            else
                updateNodeDataChanges(implGeom);
        } else if(node instanceof VRMLColorNodeType) {
            colorChanged = true;
            if (implGeom.isLive())
                implGeom.dataChanged(this);
            else
                updateNodeDataChanges(implGeom);
        } else if(node instanceof VRMLTextureCoordinateNodeType) {
            texCoordChanged = true;
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

        if(!inSetup)
            return;

        colorChanged = true;

        if (implGeom.isLive())
            implGeom.dataChanged(this);
        else
            updateNodeDataChanges(implGeom);
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

        texCoordChanged = true;
        if (implGeom.isLive())
            implGeom.dataChanged(this);
        else
            updateNodeDataChanges(implGeom);
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

        if(!inSetup)
            return;

        normalChanged = true;
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
            int num_points = vfCoord.getNumPoints();

            float[] tmp = vfCoord.getPointRef();

            implGeom.setVertices(QuadArray.COORDINATE_3, tmp, num_points / 3);
        }
    }

    /**
     * Notification that its safe to update the node now with any operations
     * that only change the node's properties, but do not change the bounds.
     *
     * @param src The node or Node Component that is to be updated.
     */
    public void updateNodeDataChanges(Object src) {
        if(colorChanged) {
            if(vfColor == null) {
                implGeom.setColors(false, null);
            } else {
                boolean alpha = (vfColor.getNumColorComponents() == 4);
                int num_colors = vfColor.getNumColors();

                if(tmpColors.length < num_colors)
                    tmpColors = new float[num_colors];

                vfColor.getColor(tmpColors);

                implGeom.setColors(alpha, tmpColors);
            }

            colorChanged = false;
        }

        if(texCoordChanged) {
            if(vfTexCoord == null) {
                implGeom.setTextureCoordinates(null, null, 0);
            } else {
                int num_tex_sets = vfTexCoord.getNumSets();

                for(int i = 0; i < num_tex_sets; i++) {
                    tmpTexCoords[i] = new float[vfTexCoord.getSize(i)];

                    int type = vfTexCoord.getNumTextureComponents();
                    switch(type) {
                        case 1:
                            tmpTexTypes[i] = QuadArray.TEXTURE_COORDINATE_1;
                            break;

                        case 2:
                            tmpTexTypes[i] = QuadArray.TEXTURE_COORDINATE_2;
                            break;

                        case 3:
                            tmpTexTypes[i] = QuadArray.TEXTURE_COORDINATE_3;
                            break;

                        case 4:
                            tmpTexTypes[i] = QuadArray.TEXTURE_COORDINATE_4;
                            break;

                    }

                    int num_texcoords = vfTexCoord.getSize(i);

                    if(tmpTexCoords[i].length < num_texcoords)
                        tmpTexCoords[i] = new float[num_texcoords];

                    tmpTexSets[i] = vfTexCoord.isShared(i);

                    if(tmpTexSets[i] == i)
                        vfTexCoord.getPoint(i, tmpTexCoords[i]);
                }

                implGeom.setTextureCoordinates(tmpTexTypes,
                                               tmpTexCoords,
                                               num_tex_sets);
                implGeom.setTextureSetMap(tmpTexSets);
            }

            texCoordChanged = false;
        }

        if(normalChanged) {
            if(vfNormal == null) {
                implGeom.setNormals(null);
            } else {
                int num_normals = vfNormal.getNumNormals();
                float[] tmp = vfNormal.getVectorRef();
                implGeom.setNormals(tmp);
            }

            normalChanged = false;
        }
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Common initialisation functionality.
     */
    private void init() {
        implGeom = new QuadArray();

        colorChanged = false;
        normalChanged = false;
        texCoordChanged = false;
    }
}
