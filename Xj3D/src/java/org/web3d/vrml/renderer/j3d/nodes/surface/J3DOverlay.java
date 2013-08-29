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

package org.web3d.vrml.renderer.j3d.nodes.surface;

// Standard imports
import java.util.Map;

import javax.media.j3d.SceneGraphObject;

import org.j3d.renderer.java3d.overlay.OverlayManager;

// Application specific imports
import org.web3d.vrml.lang.InvalidFieldValueException;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.renderer.j3d.nodes.J3DVRMLNode;
import org.web3d.vrml.renderer.j3d.nodes.J3DOverlayStructureNodeType;
import org.web3d.vrml.renderer.common.nodes.surface.BaseOverlay;

/**
 * Java3D renderer implementation of an Overlay node.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
public class J3DOverlay extends BaseOverlay
    implements J3DOverlayStructureNodeType {

    /** The manager for the overlay information */
    private OverlayManager overlayManager;

    /**
     * Construct a new default Overlay object
     */
    public J3DOverlay() {
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not the same type
     */
    public J3DOverlay(VRMLNodeType node) {
        super(node);
    }

    //----------------------------------------------------------
    // Methods required by the J3DOverlayStructureNodeType interface.
    //----------------------------------------------------------

    /**
     * Set the overlay manager to use.
     *
     * @param mgr The overlay manager instance to use
     */
    public void setOverlayManager(OverlayManager mgr) {
        overlayManager = mgr;

        // Set it in the children as well.
        if(vfLayout != null) {
            J3DOverlayStructureNodeType layout =
                (J3DOverlayStructureNodeType)vfLayout;

            layout.setOverlayManager(mgr);
        }
    }

    //----------------------------------------------------------
    // Methods required by the J3DVRMLNode interface.
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

    /**
     * Notification that the construction phase of this node has finished.
     * If the node would like to do any internal processing, such as setting
     * up geometry, then go for it now.
     */
     public void setupFinished() {
        super.setupFinished();

        // Now we know all children are parsed
        if (vfLayout != null) {
            vfLayout.setParentVisible(vfVisible);
        }
    }

    //-------------------------------------------------------------
    // Methods required by the VRMLOverlayNodeType interface.
    //-------------------------------------------------------------

    /**
     * Set the layout to the new value. Setting a value of null will
     * clear the current layout and leave nothing visible on-screen. The node
     * provided must be either {@link VRMLSurfaceLayoutNodeType} or
     * {@link VRMLProtoInstance}.
     *
     * @param layout The new layout to use
     * @throws InvalidFieldValueException The nodes are not one of the required
     *   types.
     */
    public void setLayout(VRMLNodeType layout)
        throws InvalidFieldValueException {

        if((layout != null) && !(layout instanceof J3DVRMLNode))
            throw new InvalidFieldValueException("Node not a J3D node");

        super.setLayout(layout);

        if((vfLayout != null) && (overlayManager != null)) {
            J3DOverlayStructureNodeType j_layout =
                (J3DOverlayStructureNodeType)vfLayout;

            j_layout.setOverlayManager(overlayManager);
        }
    }
}
