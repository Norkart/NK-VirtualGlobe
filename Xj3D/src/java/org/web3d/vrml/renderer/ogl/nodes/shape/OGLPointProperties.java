/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001-2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.ogl.nodes.shape;

// External imports
import org.j3d.aviatrix3d.PointAttributes;
import org.j3d.aviatrix3d.NodeUpdateListener;
import org.j3d.aviatrix3d.SceneGraphObject;

// Local imports
import org.web3d.vrml.lang.InvalidFieldValueException;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.renderer.common.nodes.shape.BasePointProperties;
import org.web3d.vrml.renderer.ogl.nodes.OGLVRMLNode;


/**
 * Aviatrix3D renderer implementation of an PointProperties node.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public class OGLPointProperties extends BasePointProperties
    implements OGLVRMLNode, NodeUpdateListener {

    /** Aviatrix3D point attribute handling */
    private PointAttributes oglAttribs;


    /** Default value of pointSize */
    private float defPointSize;

    /** Default value of minSize */
    private float defMinPointSize;

    /** Default value of maxSize */
    private float defMaxPointSize;

    /** Default values of attenuation */
    private float[] defAttenuationFactors;

    /** Default value of colorMode */
    public int defColorMode;

    /**
     * Default constructor.
     */
    public OGLPointProperties() {
        init();
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not a PointAttributes node, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not a Group node
     */
    public OGLPointProperties(VRMLNodeType node) {
        super(node);
        init();
    }

    //----------------------------------------------------------
    // Methods defined by OGLVRMLNode
    //----------------------------------------------------------

    /**
     * Get the OpenGL scene graph object representation of this node. Scripts
     * always return null.
     *
     * @return null
     */
    public SceneGraphObject getSceneGraphObject() {
        return oglAttribs;
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

        updateNodeDataChanges(oglAttribs);
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
    }

    /**
     * Notification that its safe to update the node now with any operations
     * that only change the node's properties, but do not change the bounds.
     *
     * @param src The node or Node Component that is to be updated.
     */
    public void updateNodeDataChanges(Object src) {

        if (vfPointsizeScaleFactor <= 0)
            oglAttribs.setPointSize(1);
        else
            oglAttribs.setPointSize(vfPointsizeScaleFactor);

        if (vfPointsizeMinValue >= 0)
            oglAttribs.setMinPointSize(vfPointsizeMinValue);

        if (vfPointsizeMaxValue >= 0);
            oglAttribs.setMaxPointSize(vfPointsizeMaxValue);

        oglAttribs.setAttenuationFactors(vfPointsizeAttenuation[0],
                                         vfPointsizeAttenuation[1],
                                         vfPointsizeAttenuation[2]);

    }

    //----------------------------------------------------------
    // Methods defined by BasePointProperties
    //----------------------------------------------------------

    /**
     * Override the base setting to update the Java3D representation of
     * the point style.
     *
     * @param value The scale value to check
     * @throws InvalidFieldValueException One of the colour components are out
     *     of range
     */
    protected void setPointSizeScale(float value)
        throws InvalidFieldValueException {

        super.setPointSizeScale(value);

        if (inSetup)
            return;

        if (oglAttribs.isLive())
            oglAttribs.dataChanged(this);
        else
            updateNodeDataChanges(oglAttribs);
    }

    protected void setPointSizeMin(float value)
        throws InvalidFieldValueException {

        super.setPointSizeMin(value);

        if (inSetup)
            return;

        if (oglAttribs.isLive())
            oglAttribs.dataChanged(this);
        else
            updateNodeDataChanges(oglAttribs);
    }

    protected void setPointSizeMax(float value)
        throws InvalidFieldValueException {

        super.setPointSizeMax(value);

        if (inSetup)
            return;

        if (oglAttribs.isLive())
            oglAttribs.dataChanged(this);
        else
            updateNodeDataChanges(oglAttribs);
    }

    protected void setPointSizeAttenuation(float[] factor)
        throws InvalidFieldValueException {

        super.setPointSizeAttenuation(factor);

        if (inSetup)
            return;

        if (oglAttribs.isLive())
            oglAttribs.dataChanged(this);
        else
            updateNodeDataChanges(oglAttribs);
    }

    public void setColorMode(String value)
        throws InvalidFieldValueException {

        super.setColorMode(value);

        if (inSetup)
            return;

        if (oglAttribs.isLive())
            oglAttribs.dataChanged(this);
        else
            updateNodeDataChanges(oglAttribs);
    }

    //----------------------------------------------------------
    // Local methods
    //----------------------------------------------------------

    /**
     * Common initialisation routines for the OpenGL code.
     */
    private void init() {

        oglAttribs = new PointAttributes();

        // TODO: Should this be an x3d param?
        oglAttribs.setAntiAliased(true);

        // TODO: These fields are never used?
        defPointSize = oglAttribs.getPointSize();
        defMinPointSize = oglAttribs.getMinPointSize();
        defMaxPointSize = oglAttribs.getMaxPointSize();
        oglAttribs.getAttenuationFactors(defAttenuationFactors);
        defColorMode = TEXTURE_COLOR_MODE;

    }
}
