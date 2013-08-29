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

package org.web3d.vrml.renderer.j3d.nodes.interpolator;

// Standard imports
import java.util.Map;

import javax.media.j3d.SceneGraphObject;

// Application specific imports
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.renderer.common.nodes.interpolator.BaseCoordinateInterpolator2D;
import org.web3d.vrml.renderer.j3d.nodes.J3DVRMLNode;

/**
 * Abstract implementation of an interpolator so that specific instances can
 * derive from it.
 * <p>
 *
 * Interpolator2D nodes are designed for linear keyframed animation.
 * Interpolator2Ds are driven by an input key ranging [0..1] and produce
 * corresponding piecewise-linear output functions.
 * <p>
 *
 * As interpolators all have the same number and named fields, we perform all
 * of the setup here in this class. The handling of the basic fields are
 * performed where you see the overridden methods, however routing and sets
 * are not looked after.
 * <p>
 *
 * If the key and keyValue fields are not the same length then we take
 * the lesser of the two and only assign that many vertices to the
 * interpolator to handle.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class J3DCoordinateInterpolator2D extends BaseCoordinateInterpolator2D
    implements J3DVRMLNode {

    /**
     * Initialise the time dependent node and it's fields that are held
     * locally.
     */
    public J3DCoordinateInterpolator2D() {
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not an interpolator node, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException Incorrect Node Type
     */
    public J3DCoordinateInterpolator2D(VRMLNodeType node) {
        super(node);
    }

    //----------------------------------------------------------
    // Methods required by the J3DVRMLNodeTypeType interface.
    //----------------------------------------------------------

    /**
     * Get the Java3D scene graph object representation of this node. This will
     * need to be cast to the appropriate parent type when being used.
     *
     * @return The J3D representation.
     */
    public SceneGraphObject getSceneGraphObject() {
        return null;
    }

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
}
