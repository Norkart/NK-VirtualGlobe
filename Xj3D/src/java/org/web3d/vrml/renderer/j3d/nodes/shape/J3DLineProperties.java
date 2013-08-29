/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.j3d.nodes.shape;

// Standard imports
import java.util.Map;

import javax.media.j3d.LineAttributes;
import javax.media.j3d.SceneGraphObject;

// Application specific imports
import org.web3d.vrml.lang.InvalidFieldValueException;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.renderer.common.nodes.shape.BaseLineProperties;
import org.web3d.vrml.renderer.j3d.nodes.J3DLinePropertiesNodeType;

/**
 * Java3D renderer implementation of an LineAttributes node.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
public class J3DLineProperties extends BaseLineProperties
    implements J3DLinePropertiesNodeType {

    // The following pattern styles are in the J3D 16bit representation.
    // A 0 is off for that pixel, a 1 is on.

    /** Pattern style dash dot dot (pattern 5) 01110101 */
    private static final int J3D_DASH_DOT_DOT = 0x0075;

    /** Pattern style single dot (pattern 7) 01010101 */
    private static final int J3D_SINGLE_DOT = 0x55;

    /** Java3D line attribute handling */
    private LineAttributes j3dAttribs;

    /**
     * Empty constructor
     */
    public J3DLineProperties() {
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
    public J3DLineProperties(VRMLNodeType node) {
        super(node);
        init();
    }

    //----------------------------------------------------------
    // Methods required by the J3DLinePropertiesNodeType interface.
    //----------------------------------------------------------

    /**
     * Returns a J3D LineAttributes node representation of the contents
     *
     * @return The line attributes.
     */
    public LineAttributes getLineAttributes() {
        return j3dAttribs;
    }

    //----------------------------------------------------------
    // Methods required by the J3DVRMLNode interface.
    //----------------------------------------------------------

    /**
     * Provide the set of mappings that override anything that the loader
     * might set. Default implementation does nothing.
     * <p>
     *
     * If the key is set, but the value is null or zero length, then all
     * capabilities on that node will be disabled. If the key is set the
     * values override all settings that the loader may wish to normally
     * make. This can be very dangerous if the loader is used for a file
     * format that includes its own internal animation engine, so be very
     * careful with this request.
     *
     * @param capBits The capability bits to be set
     * @param freqBits The frequency bits to be set
     */
    public void setCapabilityOverrideMap(Map capBits, Map freqBits) {
    }

    /**
     * Set the mapping of capability bits that the user would like to
     * make sure is set. The end output is that the capabilities are the union
     * of what the loader wants and what the user wants. Default implementation
     * does nothing.
     * <p>
     * If the map contains a key, but the value is  null or zero length, the
     * request is ignored.
     *
     * @param capBits The capability bits to be set
     * @param freqBits The frequency bits to be set
     */
    public void setCapabilityRequiredMap(Map capBits, Map freqBits) {
    }

    /**
     * Get the Java3D scene graph object representation of this node. Scripts
     * always return null.
     *
     * @return null
     */
    public SceneGraphObject getSceneGraphObject() {
        return j3dAttribs;
    }

    //----------------------------------------------------------
    // Methods required by the VRMLNodeType interface.
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
        super.setVersion(major, minor, isStatic);

        if(isStatic)
            return;

        j3dAttribs.setCapability(LineAttributes.ALLOW_WIDTH_WRITE);
        j3dAttribs.setCapability(LineAttributes.ALLOW_PATTERN_WRITE);
    }

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

        j3dAttribs.setLineWidth(vfLinewidthScaleFactor);
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

        switch(value) {
            case 1:
                j3dAttribs.setLinePattern(LineAttributes.PATTERN_SOLID);
                break;

            case 2:
                j3dAttribs.setLinePattern(LineAttributes.PATTERN_DASH);
                break;

            case 3:
                j3dAttribs.setLinePattern(LineAttributes.PATTERN_DOT);
                break;

            case 4:
                j3dAttribs.setLinePattern(LineAttributes.PATTERN_DASH_DOT);
                break;

            case 5:
                j3dAttribs.setLinePattern(LineAttributes.PATTERN_USER_DEFINED);
                j3dAttribs.setPatternMask(J3D_DASH_DOT_DOT);
                break;

            case 6:
                System.out.println("J3D cannot handle this line style: 6");
                break;

            case 7:
                j3dAttribs.setLinePattern(LineAttributes.PATTERN_USER_DEFINED);
                j3dAttribs.setPatternMask(J3D_SINGLE_DOT);
                break;

            default:
                System.out.println("J3D cannot handle this line style: " + value);
        }
    }

    /**
     * Common initialisation routines for the Java3D code.
     */
    private void init() {
        j3dAttribs = new LineAttributes();
    }
}
