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
import org.j3d.aviatrix3d.LineAttributes;
import org.j3d.aviatrix3d.NodeUpdateListener;
import org.j3d.aviatrix3d.SceneGraphObject;

// Local imports
import org.web3d.vrml.lang.InvalidFieldValueException;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.renderer.common.nodes.shape.BaseLineProperties;
import org.web3d.vrml.renderer.ogl.nodes.OGLVRMLNode;

/**
 * Aviatrix3D renderer implementation of an LineProperties node.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.6 $
 */
public class OGLLineProperties extends BaseLineProperties
    implements OGLVRMLNode, NodeUpdateListener {

    /** Aviatrix3D line attribute handling */
    private LineAttributes oglAttribs;

    /** Default value of lineWidth */
    private float defLineWidth;

    /** Default value of the stipple pattern */
    private short defStipplePattern;

    /**
     * Default constructor.
     */
    public OGLLineProperties() {
        init();
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not a LineAttributes node, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not a Group node
     */
    public OGLLineProperties(VRMLNodeType node) {
        super(node);
        init();
    }

    //----------------------------------------------------------
    // Methods defined by OGLVRMLNode
    //----------------------------------------------------------

    /**
     * Get the Java3D scene graph object representation of this node. Scripts
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

        if (vfApplied == false) {
            oglAttribs.setLineWidth(defLineWidth);
            oglAttribs.setStipplePattern(defStipplePattern);
            return;
        }

        if (vfLinewidthScaleFactor <= 0)
            oglAttribs.setLineWidth(1);
        else
            oglAttribs.setLineWidth(vfLinewidthScaleFactor);

        switch(vfLinetype) {
            case 1:
                oglAttribs.setStipplePattern(LineAttributes.PATTERN_SOLID);
                break;

            case 2:
                oglAttribs.setStipplePattern(LineAttributes.PATTERN_DASH);
                break;

            case 3:
                oglAttribs.setStipplePattern(LineAttributes.PATTERN_DOT);
                break;

            case 4:
                oglAttribs.setStipplePattern(LineAttributes.PATTERN_DASH_DOT);
                break;

            case 5:
                oglAttribs.setStipplePattern(LineAttributes.PATTERN_DASH_DOT_DOT);
                break;

            case 10:
                oglAttribs.setStipplePattern(LineAttributes.PATTERN_CHAIN_LINE);
                break;

            case 11:
                oglAttribs.setStipplePattern(LineAttributes.PATTERN_CENTER_LINE);
                break;

            case 13:
                oglAttribs.setStipplePattern(LineAttributes.PATTERN_PHANTOM_LINE);
                break;
        }
    }

    //----------------------------------------------------------
    // Methods defined by BaseLineProperties
    //----------------------------------------------------------

    /**
     * Override the base setting to update the Java3D representation of
     * the line style.
     *
     * @param value The scale value to check
     * @throws InvalidFieldValueException One of the colour components are out
     *     of range
     */
    protected void setLineWidthScale(float value)
        throws InvalidFieldValueException {

        super.setLineWidthScale(value);

        if (inSetup)
            return;

        if (oglAttribs.isLive())
            oglAttribs.dataChanged(this);
        else
            updateNodeDataChanges(oglAttribs);
    }

    /**
     * Set the line type to the new value.
     *
     * @param value The new color value to check
     * @throws InvalidFieldValueException One of the colour components are out
     *     of range
     */
    protected void setLineType(int value)
        throws InvalidFieldValueException {

        super.setLineType(value);

        if (inSetup)
            return;

        switch(vfLinetype) {
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 10:
            case 11:
            case 13:
                if (oglAttribs.isLive())
                    oglAttribs.dataChanged(this);
                else
                    updateNodeDataChanges(oglAttribs);
                break;

            default:
                System.out.println("OGL cannot handle this line style: " + value);
        }
    }

    //----------------------------------------------------------
    // Local methods
    //----------------------------------------------------------

    /**
     * Common initialisation routines for the OpenGL code.
     */
    private void init() {
        oglAttribs = new LineAttributes();
        oglAttribs.setAntiAliased(true);

        defLineWidth = oglAttribs.getLineWidth();
        defStipplePattern = oglAttribs.getStipplePattern();
    }
}
